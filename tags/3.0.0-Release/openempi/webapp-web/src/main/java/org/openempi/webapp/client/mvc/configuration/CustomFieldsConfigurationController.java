/**
 *
 * Copyright (C) 2002-2012 "SYSNET International, Inc."
 * support@sysnetint.com [http://www.sysnetint.com]
 *
 * This file is part of OpenEMPI.
 *
 * OpenEMPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openempi.webapp.client.mvc.configuration;

import java.util.ArrayList;
import java.util.List;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.ConfigurationDataServiceAsync;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.domain.AuthenticationException;
import org.openempi.webapp.client.model.CustomFieldWeb;
import org.openempi.webapp.client.model.EntityAttributeWeb;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.ModelPropertyWeb;
import org.openempi.webapp.client.mvc.Controller;
import org.openempi.webapp.client.ui.util.Utility;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CustomFieldsConfigurationController extends Controller
{
	private CustomFieldsConfigurationView customFieldsConfigurationView;

	public CustomFieldsConfigurationController() {
		this.registerEventTypes(AppEvents.CustomFieldsConfigurationReceived);
		this.registerEventTypes(AppEvents.CustomFieldsConfigurationRequest);
		this.registerEventTypes(AppEvents.CustomFieldsConfigurationSave);
		this.registerEventTypes(AppEvents.CustomFieldsConfigurationView);
	}

	@Override
	protected void initialize() {
		customFieldsConfigurationView = new CustomFieldsConfigurationView(this);
	}

	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (type == AppEvents.CustomFieldsConfigurationView) {
			forwardToView(customFieldsConfigurationView, event);
		} else if (type == AppEvents.CustomFieldsConfigurationRequest) {
			requestCustomFieldsConfigurationData();
		} else if (type == AppEvents.CustomFieldsConfigurationSave) {
			saveCustomFieldsConfiguration(event);
		}
	}

	@SuppressWarnings("unchecked")
	private void saveCustomFieldsConfiguration(AppEvent event) {
		ConfigurationDataServiceAsync configurationDataService = getConfigurationDataService();
		final List<CustomFieldWeb> customFields = (List<CustomFieldWeb>) event.getData();
    	EntityWeb entityModel = event.getData("entityModel");

		configurationDataService.saveCustomFieldsConfiguration(entityModel, customFields, (new AsyncCallback<String>() {
	      public void onFailure(Throwable caught) {

				if (caught instanceof AuthenticationException) {
					Dispatcher.get().dispatch(AppEvents.Logout);
					return;
				}
		    	forwardToView(customFieldsConfigurationView, AppEvents.Error, caught.getMessage());
	      }

	      public void onSuccess(String message) {

	          // set newly changed custom field to Register for updating the selection list of Blocking and Matching Configuration
	          setAllAttributeNamesToRegistry(customFields);


	    	  forwardToView(customFieldsConfigurationView, AppEvents.CustomFieldsConfigurationSaveComplete, message);
	      }
	    }));
	}

	private void requestCustomFieldsConfigurationData() {
		ConfigurationDataServiceAsync configurationDataService = getConfigurationDataService();
		EntityWeb currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
		String entityName = "";
		if (currentEntity != null) {
			entityName = currentEntity.getName();
		}
		configurationDataService.loadCustomFieldsConfiguration(entityName, new AsyncCallback<List<CustomFieldWeb>>() {
	      public void onFailure(Throwable caught) {

				if (caught instanceof AuthenticationException) {
					Dispatcher.get().dispatch(AppEvents.Logout);
					return;
				}
				Dispatcher.forwardEvent(AppEvents.Error, caught);
	      }

	      public void onSuccess(List<CustomFieldWeb> result) {
	        forwardToView(customFieldsConfigurationView, AppEvents.CustomFieldsConfigurationReceived, result);
	      }
	    });
	}

    private void setAllAttributeNamesToRegistry(List<CustomFieldWeb> customFiels) {
        EntityWeb entity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
        if (entity == null) {
            Registry.register(Constants.PERSON_MODEL_ALL_ATTRIBUTE_NAMES, null);
            return;
        }

        // Get ALL_ATTRIBUTE_NAMES from entity model and customFieldNames
        List<ModelPropertyWeb> allAttributeNames = new ArrayList<ModelPropertyWeb>();
        for (EntityAttributeWeb entityAttribute : entity.getAttributes()) {
            String name = entityAttribute.getName();
            allAttributeNames.add(new ModelPropertyWeb(name, Utility.convertToDescription(name)));
        }
        for (CustomFieldWeb customField : customFiels) {
            String name = customField.getFieldName();
            allAttributeNames.add(new ModelPropertyWeb(name, Utility.convertToDescription(name)));
        }
        Registry.register(Constants.PERSON_MODEL_ALL_ATTRIBUTE_NAMES, allAttributeNames);
    }
}
