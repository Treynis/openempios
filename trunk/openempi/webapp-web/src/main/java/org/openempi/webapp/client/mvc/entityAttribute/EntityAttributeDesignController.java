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
package org.openempi.webapp.client.mvc.entityAttribute;

import java.util.ArrayList;
import java.util.List;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.ConfigurationDataServiceAsync;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.domain.AuthenticationException;
import org.openempi.webapp.client.mvc.Controller;
import org.openempi.webapp.client.model.CustomFieldWeb;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.ModelPropertyWeb;
import org.openempi.webapp.client.model.UserFileWeb;
import org.openempi.webapp.client.model.UserWeb;
import org.openempi.webapp.client.ui.util.Utility;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;

import org.openempi.webapp.client.EntityDefinitionDataServiceAsync;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.extjs.gxt.ui.client.widget.Info;

public class EntityAttributeDesignController extends Controller
{
	private EntityAttributeDesignView entityAttributeDesignView;

	public EntityAttributeDesignController() {
		this.registerEventTypes(AppEvents.EntityAttributeView);
        this.registerEventTypes(AppEvents.FileListUpdate);
        this.registerEventTypes(AppEvents.FileEntryRemove);
        this.registerEventTypes(AppEvents.FileEntryImport);
	}

	@Override
	protected void initialize() {
		entityAttributeDesignView = new EntityAttributeDesignView(this);
	}

	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (type == AppEvents.EntityAttributeView) {
			forwardToView(entityAttributeDesignView, event);

		} else if (type == AppEvents.EntitiesRequest) {

	    	loadEntities();

		} else if (type == AppEvents.EntityAdd) {

			addEntityData(event);

		} else if (type == AppEvents.EntityUpdate) {

			updateEntityData(event);

		} else if (type == AppEvents.EntityDelete) {

			deleteEntityData(event);

		} else if (type == AppEvents.CustomFieldsConfigurationRequest) {
			EntityWeb entity = (EntityWeb) event.getData();

			loadCustomFieldsConfigurationData(entity);

        } else if (type == AppEvents.FileListUpdate) {

            updateUserFileData();

        } else if (type == AppEvents.FileEntryRemove) {

            List<UserFileWeb> fileList = event.getData();
            deleteUserFileEntries(fileList);

        } else if (type == AppEvents.FileEntryImport) {

            List<UserFileWeb> fileList = event.getData();
            importUserFileEntries(fileList);

        }
	}

	private void loadEntities() {
		EntityDefinitionDataServiceAsync entityDataService = getEntityDefinitionDataService();

	  	// Info.display("Information", "Submitting request to load entities");

		entityDataService.loadEntities(new AsyncCallback<List<EntityWeb>>() {
			public void onFailure(Throwable caught) {

		    	  if (caught instanceof AuthenticationException) {

			    	  forwardToView(entityAttributeDesignView, AppEvents.Logout, null);
		    		  return;
		    	  }
		    	  forwardToView(entityAttributeDesignView, AppEvents.Error, caught.getMessage());
		      }

			public void onSuccess(List<EntityWeb> result) {

		    	  forwardToView(entityAttributeDesignView, AppEvents.EntitiesReceived, result);
		    }
		});
	}

	private void loadCustomFieldsConfigurationData(final EntityWeb entity) {
		String entityName = "";
		if (entity != null) {
			entityName = entity.getName();
		}

		ConfigurationDataServiceAsync configurationDataService = getConfigurationDataService();
		configurationDataService.loadCustomFieldsConfiguration(entityName, new AsyncCallback<List<CustomFieldWeb>>() {
	      public void onFailure(Throwable caught) {

				if (caught instanceof AuthenticationException) {
					Dispatcher.get().dispatch(AppEvents.Logout);
					return;
				}
				Dispatcher.forwardEvent(AppEvents.Error, caught);
	      }

	      public void onSuccess(List<CustomFieldWeb> result) {
	    	  if (result != null) {
				 List<ModelPropertyWeb> customFieldNames = new ArrayList<ModelPropertyWeb>(result.size());
				 for (CustomFieldWeb customField : result) {
					 String name = customField.getFieldName();
					 customFieldNames.add(new ModelPropertyWeb(name, Utility.convertToDescription(name)));
				 }
				 Registry.register(Constants.PERSON_MODEL_CUSTOM_FIELD_NAMES, customFieldNames);

		         forwardToView(entityAttributeDesignView, AppEvents.CustomFieldsConfigurationReceived, entity);
	    	  }
	      }
	    });
	}

	private void addEntityData(AppEvent event) {
		EntityDefinitionDataServiceAsync entityDataService = getEntityDefinitionDataService();

		EntityWeb entity = (EntityWeb) event.getData();

		entityDataService.addEntity(entity, new AsyncCallback<EntityWeb>() {

	      public void onFailure(Throwable caught) {

	    	  if (caught instanceof AuthenticationException) {

		    	  forwardToView(entityAttributeDesignView, AppEvents.Logout, null);
	    		  return;
	    	  }
	    	  forwardToView(entityAttributeDesignView, AppEvents.Error, caught.getMessage());
	      }

	      public void onSuccess(EntityWeb entity) {
			  // Info.display("Information", "add onSuccess.");
	          forwardToView(entityAttributeDesignView, AppEvents.EntityAddComplete, entity);
	      }
	    });
	}

	private void updateEntityData(AppEvent event) {
		EntityDefinitionDataServiceAsync entityDataService = getEntityDefinitionDataService();

		EntityWeb entity = (EntityWeb) event.getData();

		entityDataService.updateEntity(entity, new AsyncCallback<EntityWeb>() {

	      public void onFailure(Throwable caught) {

	    	  if (caught instanceof AuthenticationException) {

		    	  forwardToView(entityAttributeDesignView, AppEvents.Logout, null);
	    		  return;
	    	  }
	    	  forwardToView(entityAttributeDesignView, AppEvents.Error, caught.getMessage());
	      }

	      public void onSuccess(EntityWeb entity) {
			// Info.display("Information", "edit onSuccess.");
	        forwardToView(entityAttributeDesignView, AppEvents.EntityUpdateComplete, entity);
	      }
	    });
	}

	private void deleteEntityData(AppEvent event) {
		EntityDefinitionDataServiceAsync entityDataService = getEntityDefinitionDataService();

		EntityWeb entity = (EntityWeb) event.getData();

		entityDataService.deleteEntity(entity, new AsyncCallback<String>() {

	      public void onFailure(Throwable caught) {

	    	  if (caught instanceof AuthenticationException) {

		    	  forwardToView(entityAttributeDesignView, AppEvents.Logout, null);
	    		  return;
	    	  }
		    forwardToView(entityAttributeDesignView, AppEvents.Error, caught.getMessage());
	      }

	      public void onSuccess(String message) {
			// Info.display("Information", "delete onSuccess.");
	        forwardToView(entityAttributeDesignView, AppEvents.EntityDeleteComplete, message);
	      }
	    });
	}

    private void updateUserFileData() {
        UserWeb loginUser = Registry.get(Constants.LOGIN_USER);
        if (loginUser != null) {
            getUserFileDataService().getUserFiles(loginUser.getUsername(), true, new AsyncCallback<List<UserFileWeb>>()
            {
                public void onFailure(Throwable caught) {
                    // Dispatcher.forwardEvent(AppEvents.Error, caught);

                    if (caught instanceof AuthenticationException) {
                        Dispatcher.get().dispatch(AppEvents.Logout);
                        return;
                    }
                    forwardToView(entityAttributeDesignView, AppEvents.Error, caught.getMessage());
                }

                public void onSuccess(List<UserFileWeb> result) {
                    forwardToView(entityAttributeDesignView, AppEvents.FileListRender, result);
                }
            });
        }
    }

    private void deleteUserFileEntries(List<UserFileWeb> fileEntries) {
        final int deleteTotal = fileEntries.size();
        final int[] count = {0};

        for (UserFileWeb userFile : fileEntries) {
            getUserFileDataService().removeUserFile(userFile.getUserFileId(), new AsyncCallback<Void>()
            {
                public void onFailure(Throwable caught) {

                    if (caught instanceof AuthenticationException) {
                        Dispatcher.get().dispatch(AppEvents.Logout);
                        return;
                    }
                    forwardToView(entityAttributeDesignView, AppEvents.Error, caught.getMessage());
                }

                public void onSuccess(Void value) {
                    count[0]++;
                    if (count[0] == deleteTotal) {
                        updateUserFileData();
                    }
                }
            });
        }
    }

    private void importUserFileEntries(List<UserFileWeb> fileEntries) {
        // Info.display("Information", "importUserFileEntries");
        for (UserFileWeb userFile : fileEntries) {
            getEntityDefinitionDataService().importEntity(userFile, new AsyncCallback<String>()
            {

                public void onFailure(Throwable caught) {
                    // forwardToView(userFileView, AppEvents.Error, caught.getMessage());

                    if (caught instanceof AuthenticationException) {
                        Dispatcher.get().dispatch(AppEvents.Logout);
                        return;
                    }
                    forwardToView(entityAttributeDesignView, AppEvents.Error, caught.getMessage());
                }

                public void onSuccess(String value) {
                    forwardToView(entityAttributeDesignView, AppEvents.FileEntryImportSuccess, "File successfully imported");
                }
            });
        }
    }
}
