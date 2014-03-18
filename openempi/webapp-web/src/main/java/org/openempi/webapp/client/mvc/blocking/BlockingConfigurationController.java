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
package org.openempi.webapp.client.mvc.blocking;

import java.util.List;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.BlockingDataServiceAsync;
import org.openempi.webapp.client.domain.AuthenticationException;
import org.openempi.webapp.client.model.BlockingEntryListWeb;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.mvc.Controller;
import org.openempi.webapp.client.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class BlockingConfigurationController extends Controller
{
	private BlockingConfigurationView blockingConfigurationView;

	public BlockingConfigurationController() {
		this.registerEventTypes(AppEvents.BlockingConfigurationReceived);
		this.registerEventTypes(AppEvents.BlockingConfigurationRequest);
		this.registerEventTypes(AppEvents.BlockingConfigurationSave);
		this.registerEventTypes(AppEvents.BlockingConfigurationView);
	}

	@Override
	protected void initialize() {
		blockingConfigurationView = new BlockingConfigurationView(this);
	}

	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (type == AppEvents.BlockingConfigurationView) {
			forwardToView(blockingConfigurationView, event);
		} else if (type == AppEvents.BlockingConfigurationRequest) {
			requestBlockingConfigurationData();
		} else if (type == AppEvents.BlockingConfigurationSave) {
			saveBlockingConfiguration(event);
		}
	}

	@SuppressWarnings("unchecked")
	private void saveBlockingConfiguration(AppEvent event) {
		BlockingDataServiceAsync blockingService = getBlockingDataService();
		BlockingEntryListWeb configuration = (BlockingEntryListWeb) event.getData();
        EntityWeb currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
        if (currentEntity != null) {
            configuration.setEntityName(currentEntity.getName());
        }
		blockingService.saveTraditionalBlockingConfigurationData(configuration, (new AsyncCallback<String>() {
	      public void onFailure(Throwable caught) {
//		        Dispatcher.forwardEvent(AppEvents.Error, caught);

				if (caught instanceof AuthenticationException) {
					Dispatcher.get().dispatch(AppEvents.Logout);
					return;
				}
		    	forwardToView(blockingConfigurationView, AppEvents.Error, caught.getMessage());
	      }

	      public void onSuccess(String message) {
/*	    	  if (message == null || message.length() == 0) {
	    		  Info.display("Information", "Blocking configuration data was saved successfully.");
	    	  } else {
	    		  Info.display("Information", "Failed to save blocking configuration data: " + message);
	    	  }
*/
	    	  forwardToView(blockingConfigurationView, AppEvents.BlockingConfigurationSaveComplete, message);
	      }
	    }));
	}

	private void requestBlockingConfigurationData() {
		BlockingDataServiceAsync blockingService = getBlockingDataService();
        EntityWeb entity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
        String entityName = "";
        if (entity != null) {
            entityName = entity.getName();
        }
		blockingService.loadTraditionalBlockingConfigurationData(entityName, new AsyncCallback<BlockingEntryListWeb>() {
	      public void onFailure(Throwable caught) {

				if (caught instanceof AuthenticationException) {
					Dispatcher.get().dispatch(AppEvents.Logout);
					return;
				}
				Dispatcher.forwardEvent(AppEvents.Error, caught);
	      }

	      public void onSuccess(BlockingEntryListWeb result) {
	        forwardToView(blockingConfigurationView, AppEvents.BlockingConfigurationReceived, result);
	      }
	    });
	}
}
