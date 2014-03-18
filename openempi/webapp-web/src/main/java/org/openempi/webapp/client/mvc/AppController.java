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
package org.openempi.webapp.client.mvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.ConfigurationDataServiceAsync;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.EntityDefinitionDataServiceAsync;
import org.openempi.webapp.client.ReferenceDataServiceAsync;
import org.openempi.webapp.client.UserDataServiceAsync;
import org.openempi.webapp.client.domain.AuthenticationException;
import org.openempi.webapp.client.model.AuditEventTypeWeb;
import org.openempi.webapp.client.model.CustomFieldWeb;
import org.openempi.webapp.client.model.EntityAttributeWeb;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.IdentifierDomainTypeCodeWeb;
import org.openempi.webapp.client.model.IdentifierDomainWeb;
import org.openempi.webapp.client.model.JobStatusWeb;
import org.openempi.webapp.client.model.JobTypeWeb;
import org.openempi.webapp.client.model.ModelPropertyWeb;
import org.openempi.webapp.client.model.RoleWeb;
import org.openempi.webapp.client.model.SystemConfigurationWeb;
import org.openempi.webapp.client.model.UserWeb;
import org.openempi.webapp.client.model.PermissionWeb;
import org.openempi.webapp.client.model.EntityAttributeDatatypeWeb;
import org.openempi.webapp.client.model.EntityValidationRuleWeb;
import org.openempi.webapp.client.ui.util.Utility;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
//import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AppController extends Controller {

	private AppView appView;
	private ReferenceDataServiceAsync referenceDataService;
	private UserDataServiceAsync userDataService;

	public AppController() {
		registerEventTypes(AppEvents.Error);
		registerEventTypes(AppEvents.ComparatorFunctionNamesReceived);
	    registerEventTypes(AppEvents.GlobalIdentifierDomainReceived);
		registerEventTypes(AppEvents.IdentifierDomainsReceived);
		registerEventTypes(AppEvents.IdentifierDomainTypeCodesReceived);
		registerEventTypes(AppEvents.AuditEventTypeCodesReceived);
		registerEventTypes(AppEvents.Init);
		registerEventTypes(AppEvents.Login);
		registerEventTypes(AppEvents.Logout);
		registerEventTypes(AppEvents.PersonModelAllAttributeNamesReceived);
		registerEventTypes(AppEvents.PersonModelAttributeNamesReceived);
		registerEventTypes(AppEvents.PersonModelCustomFieldNamesReceived);
		registerEventTypes(AppEvents.SystemConfigurationInfoReceived);
		registerEventTypes(AppEvents.RoleListReceived);
		registerEventTypes(AppEvents.PermissionListReceived);
		registerEventTypes(AppEvents.TransformationFunctionNamesReceived);
		registerEventTypes(AppEvents.EntityAttributeDatatypesReceived);
		registerEventTypes(AppEvents.ValidationRuleReceived);
        registerEventTypes(AppEvents.JobTypesReceived);
        registerEventTypes(AppEvents.JobStatusesReceived);
	}

	@SuppressWarnings("unchecked")
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (type == AppEvents.Init) {
			onInit(event);
		} else if (type == AppEvents.Login) {
			onLogin(event);
		} else if (type == AppEvents.Logout) {
			onLogin(event);
		} else if (type == AppEvents.UserAuthenticate) {
	    	UserWeb user = event.getData();
			authenticate(user);
		} else if (type == AppEvents.Error) {
			onError(event);
        } else if (type == AppEvents.GlobalIdentifierDomainReceived) {
            IdentifierDomainWeb domain = event.getData();
            Registry.register(Constants.GLOBAL_IDENTITY_DOMAIN, domain);
		} else if (type == AppEvents.IdentifierDomainsReceived) {
			List<IdentifierDomainWeb> domains = event.getData();
			Registry.register(Constants.IDENTITY_DOMAINS, domains);
		} else if (type == AppEvents.IdentifierDomainTypeCodesReceived) {
			List<IdentifierDomainTypeCodeWeb> codes = event.getData();
			Registry.register(Constants.IDENTITY_DOMAIN_TYPE_CODES, codes);
		} else if (type == AppEvents.AuditEventTypeCodesReceived) {
			List<AuditEventTypeWeb> codes = event.getData();
			Registry.register(Constants.AUDIT_EVENT_TYPE_CODES, codes);
		} else if (type == AppEvents.PersonModelAllAttributeNamesReceived) {
			List<String> allAttributeNames = event.getData();
			List<ModelPropertyWeb> personModelPropertyNames = new ArrayList<ModelPropertyWeb>(allAttributeNames.size());
			for (String name : allAttributeNames) {
				personModelPropertyNames.add(new ModelPropertyWeb(name, Utility.convertToDescription(name)));
			}
			Registry.register(Constants.PERSON_MODEL_ALL_ATTRIBUTE_NAMES, personModelPropertyNames);
		} else if (type == AppEvents.PersonModelAttributeNamesReceived) {
			List<String> attributeNames = event.getData();
			List<ModelPropertyWeb> personModelPropertyNames = new ArrayList<ModelPropertyWeb>(attributeNames.size());
			for (String name : attributeNames) {
				personModelPropertyNames.add(new ModelPropertyWeb(name, Utility.convertToDescription(name)));
			}
			Registry.register(Constants.PERSON_MODEL_ATTRIBUTE_NAMES, personModelPropertyNames);
		} else if (type == AppEvents.PersonModelCustomFieldNamesReceived) {
			List<String> customFieldNames = event.getData();
			List<ModelPropertyWeb> personModelCustomFieldNames = new ArrayList<ModelPropertyWeb>(customFieldNames.size());
			for (String name : customFieldNames) {
				personModelCustomFieldNames.add(new ModelPropertyWeb(name, Utility.convertToDescription(name)));
			}
			Registry.register(Constants.PERSON_MODEL_CUSTOM_FIELD_NAMES, personModelCustomFieldNames);
		} else if (type == AppEvents.ComparatorFunctionNamesReceived) {
			List<String> comparatorFunctionNames = event.getData();
			List<ModelPropertyWeb> compFuncNames = new ArrayList<ModelPropertyWeb>(comparatorFunctionNames.size());
			for (String name : comparatorFunctionNames) {
				compFuncNames.add(new ModelPropertyWeb(name, Utility.convertToDescription(name)));
			}
			Registry.register(Constants.COMPARATOR_FUNCTION_NAMES, compFuncNames);
		} else if (type == AppEvents.TransformationFunctionNamesReceived) {
			List<String> transformationFunctionNames = event.getData();
			List<ModelPropertyWeb> trafoFuncNames = new ArrayList<ModelPropertyWeb>(transformationFunctionNames.size());
			for (String name : transformationFunctionNames) {
				trafoFuncNames.add(new ModelPropertyWeb(name, Utility.convertToDescription(name)));
			}
			Registry.register(Constants.TRANSFORMATION_FUNCTION_NAMES, trafoFuncNames);
		} else if (type == AppEvents.RoleListReceived) {
			List<RoleWeb> roleList = (List<RoleWeb>) event.getData();
			Registry.register(Constants.ROLE_LIST, roleList);
		} else if (type == AppEvents.PermissionListReceived) {
			List<PermissionWeb> roleList = (List<PermissionWeb>) event.getData();
			Registry.register(Constants.PERMISSION_LIST, roleList);
		} else if (type == AppEvents.EntityAttributeDatatypesReceived) {
			List<EntityAttributeDatatypeWeb> dataTypes = event.getData();
			Registry.register(Constants.ENTITY_ATTRIBUTE_DATA_TYPES, dataTypes);
		} else if (type == AppEvents.ValidationRuleReceived) {
			List<EntityValidationRuleWeb> rules = event.getData();
			Registry.register(Constants.ENTITY_VALIDATION_RULES, rules);
        } else if (type == AppEvents.SystemConfigurationInfoReceived) {
            SystemConfigurationWeb systemConfigInfo = event.getData();
            Log.debug("Received the system configuration information.");
            Log.info("The system is configured with blocking algorithm: " + systemConfigInfo.getBlockingAlgorithmName());
            Log.info("The system is configured with matching algorithm: " + systemConfigInfo.getMatchingAlgorithmName());
            Registry.register(Constants.SYSTEM_CONFIGURATION_INFO, systemConfigInfo);

	        Dispatcher.forwardEvent(AppEvents.InitMenu);
        } else if (type == AppEvents.JobTypesReceived) {
            List<JobTypeWeb> jobTypes = event.getData();
            Registry.register(Constants.JOB_TYPES, jobTypes);
        } else if (type == AppEvents.JobStatusesReceived) {
            List<JobStatusWeb> jobStatuses = event.getData();
            Registry.register(Constants.JOB_STATUS, jobStatuses);
		} else if (type == AppEvents.EntitiesRequest) {

	    	loadEntities();
		}

	}

	public void initialize() {
		appView = new AppView(this);
	}

	protected void onError(AppEvent ae) {
		System.out.println("error: " + ae.<Object>getData());
	}

	private void onInit(AppEvent event) {
		forwardToView(appView, event);
		userDataService = (UserDataServiceAsync) Registry.get(Constants.USER_DATA_SERVICE);

		referenceDataService = (ReferenceDataServiceAsync) Registry.get(Constants.REF_DATA_SERVICE);
		referenceDataService.getIdentifierDomainTypeCodes(new AsyncCallback<List<IdentifierDomainTypeCodeWeb>>() {
			public void onFailure(Throwable caught) {

				if (caught instanceof AuthenticationException) {
					Dispatcher.get().dispatch(AppEvents.Logout);
					return;
				}
				Dispatcher.forwardEvent(AppEvents.Error, caught);
				Info.display("Information", "Error: " + caught.getMessage());
			}

			public void onSuccess(List<IdentifierDomainTypeCodeWeb> result) {
				Dispatcher.forwardEvent(AppEvents.IdentifierDomainTypeCodesReceived, result);
				// Info.display("Information", "We've got the codes: " + result);
			}
		});

		referenceDataService.getAuditEventTypeCodes(new AsyncCallback<List<AuditEventTypeWeb>>() {
			public void onFailure(Throwable caught) {

				if (caught instanceof AuthenticationException) {
					Dispatcher.get().dispatch(AppEvents.Logout);
					return;
				}
				Dispatcher.forwardEvent(AppEvents.Error, caught);
				Info.display("Information", "Error: " + caught.getMessage());
			}

			public void onSuccess(List<AuditEventTypeWeb> result) {
				Dispatcher.forwardEvent(AppEvents.AuditEventTypeCodesReceived, result);
				// Info.display("Information", "We've got the audit event type codes: " + result);
			}
		});

        referenceDataService.getGlobalIdentifierDomain(new AsyncCallback<IdentifierDomainWeb>() {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {
                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                Dispatcher.forwardEvent(AppEvents.Error, caught);
                Info.display("Information", "Error: " + caught.getMessage());
            }

            public void onSuccess(IdentifierDomainWeb result) {
                Dispatcher.forwardEvent(AppEvents.GlobalIdentifierDomainReceived, result);
                // Info.display("Information", "We've got the codes: " + result);
            }
        });

		referenceDataService.getIdentifierDomains(new AsyncCallback<List<IdentifierDomainWeb>>() {
			public void onFailure(Throwable caught) {

				if (caught instanceof AuthenticationException) {
					Dispatcher.get().dispatch(AppEvents.Logout);
					return;
				}
				Dispatcher.forwardEvent(AppEvents.Error, caught);
				Info.display("Information", "Error: " + caught.getMessage());
			}

			public void onSuccess(
					List<IdentifierDomainWeb> result) {
				Dispatcher.forwardEvent(AppEvents.IdentifierDomainsReceived, result);
				// Info.display("Information", "We've got the codes: " + result);
			}
		});

		referenceDataService.getTransformationFunctionNames(new AsyncCallback<List<String>>() {
			public void onFailure(Throwable caught) {

				if (caught instanceof AuthenticationException) {
					Dispatcher.get().dispatch(AppEvents.Logout);
					return;
				}
				Dispatcher.forwardEvent(AppEvents.Error, caught);
			}

			public void onSuccess(List<String> result) {
				Dispatcher.forwardEvent(AppEvents.TransformationFunctionNamesReceived, result);
				// Info.display("Information", "We've got the transformation function names: " + result);
			}
		});

		referenceDataService.getComparatorFunctionNames(new AsyncCallback<List<String>>() {
			public void onFailure(Throwable caught) {

				if (caught instanceof AuthenticationException) {
					Dispatcher.get().dispatch(AppEvents.Logout);
					return;
				}
				Dispatcher.forwardEvent(AppEvents.Error, caught);
			}

			public void onSuccess(List<String> result) {
				Dispatcher.forwardEvent(AppEvents.ComparatorFunctionNamesReceived, result);
				// Info.display("Information", "We've got the comparator function names: " + result);
			}
		});

		userDataService.getRoles(new AsyncCallback<List<RoleWeb>>() {
		      public void onFailure(Throwable caught) {

					if (caught instanceof AuthenticationException) {
						Dispatcher.get().dispatch(AppEvents.Logout);
						return;
					}
					Dispatcher.forwardEvent(AppEvents.Error, caught);
		      }

		      public void onSuccess(List<RoleWeb> result) {
		    	    //Info.display("Information", "onSuccess:"+result.size());
					Dispatcher.forwardEvent(AppEvents.RoleListReceived, result);
		      }
		});

		userDataService.getPermissions(new AsyncCallback<List<PermissionWeb>>() {
		      public void onFailure(Throwable caught) {

					if (caught instanceof AuthenticationException) {
						Dispatcher.get().dispatch(AppEvents.Logout);
						return;
					}
					Dispatcher.forwardEvent(AppEvents.Error, caught);
		      }

		      public void onSuccess(List<PermissionWeb> result) {
		    	    //Info.display("Information", "onSuccess:"+result.size());
					Dispatcher.forwardEvent(AppEvents.PermissionListReceived, result);
		      }
		});

		referenceDataService.getEntityAttributeDatatypes(new AsyncCallback<List<EntityAttributeDatatypeWeb>>() {
			public void onFailure(Throwable caught) {

				if (caught instanceof AuthenticationException) {
					Dispatcher.get().dispatch(AppEvents.Logout);
					return;
				}
				Dispatcher.forwardEvent(AppEvents.Error, caught);
			}

			public void onSuccess(List<EntityAttributeDatatypeWeb> result) {
				Dispatcher.forwardEvent(AppEvents.EntityAttributeDatatypesReceived, result);
			}
		});

		referenceDataService.getValidationRules(new AsyncCallback<List<EntityValidationRuleWeb>>() {
			public void onFailure(Throwable caught) {

				if (caught instanceof AuthenticationException) {
					Dispatcher.get().dispatch(AppEvents.Logout);
					return;
				}
				Dispatcher.forwardEvent(AppEvents.Error, caught);
			}

			public void onSuccess(List<EntityValidationRuleWeb> result) {
				Dispatcher.forwardEvent(AppEvents.ValidationRuleReceived, result);
			}
	    });
/*
        referenceDataService.getSystemConfigurationInfo(new AsyncCallback<SystemConfigurationWeb>() {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {
                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                Dispatcher.forwardEvent(AppEvents.Error, caught);
            }

            public void onSuccess(SystemConfigurationWeb result) {
                Dispatcher.forwardEvent(AppEvents.SystemConfigurationInfoReceived, result);
            }
        });
*/

        referenceDataService.getJobTypes(new AsyncCallback<List<JobTypeWeb>>() {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {
                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                Dispatcher.forwardEvent(AppEvents.Error, caught);
            }

            public void onSuccess(List<JobTypeWeb> result) {
                Dispatcher.forwardEvent(AppEvents.JobTypesReceived, result);
            }
        });

        referenceDataService.getJobStatuses(new AsyncCallback<List<JobStatusWeb>>() {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {
                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                Dispatcher.forwardEvent(AppEvents.Error, caught);
            }

            public void onSuccess(List<JobStatusWeb> result) {
                Dispatcher.forwardEvent(AppEvents.JobStatusesReceived, result);
            }
        });

		loadEntities();
	}

	private void onLogin(AppEvent event) {
		forwardToView(appView, event);
	}

	private void authenticate(UserWeb user) {
		getUserDataService().authenticateUser(user.getUsername(), user.getPassword(), false, new AsyncCallback<UserWeb>() {
		      public void onFailure(Throwable caught) {

		    	  // Dispatcher.forwardEvent(AppEvents.Error, caught);
		    	  forwardToView(appView, AppEvents.UserAuthenticateFailure, caught.getMessage());
		      }

		      public void onSuccess(UserWeb result) {
		    	  //Info.display("Information", "onSuccess:"+result.getId());
		    	  // forwardToView(appView, AppEvents.UserAuthenticateSuccess, result);

		    	  Registry.register(Constants.LOGIN_USER, result);
				  getUserPermisions(result);
		      }
		    });
	}

	private void getUserPermisions(UserWeb user) {
		getUserDataService().getUserPermissions(user, new AsyncCallback<Map<String, PermissionWeb>>() {
		      public void onFailure(Throwable caught) {

		    	  // Dispatcher.forwardEvent(AppEvents.Error, caught);
		    	  forwardToView(appView, AppEvents.UserAuthenticateFailure, caught.getMessage());
		      }

		      public void onSuccess(Map<String, PermissionWeb> result) {
		    	  //Info.display("Information", "onSuccess:"+result.getId());
		    	  forwardToView(appView, AppEvents.UserAuthenticateSuccess, result);
		      }
		    });
	}

	private void loadEntities() {
	  	// Info.display("Information", "Submitting request to load entities");

		EntityDefinitionDataServiceAsync entityDataService = getEntityDefinitionDataService();

		entityDataService.loadEntities(new AsyncCallback<List<EntityWeb>>() {
			public void onFailure(Throwable caught) {

		    	  if (caught instanceof AuthenticationException) {

			    	  forwardToView(appView, AppEvents.Logout, null);
		    		  return;
		    	  }
		    	  forwardToView(appView, AppEvents.Error, caught.getMessage());
		      }

			public void onSuccess(List<EntityWeb> result) {
				if (result != null && result.size() > 0) {
					EntityWeb entity =  result.get(0);
				    Registry.register(Constants.ENTITY_ATTRIBUTE_MODEL, entity);

                    loadSystemConfigurationInfo(entity);

                    // setEntityAttribureNamesToRegistry(entity);
				    loadCustomFieldsConfigurationData(entity);
				}
		    }
		});
	}

    private void loadSystemConfigurationInfo(EntityWeb entity) {
        String entityName = "";
        if (entity != null) {
            entityName = entity.getName();
        }

        referenceDataService.getSystemConfigurationInfo(entityName, new AsyncCallback<SystemConfigurationWeb>() {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {
                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                Dispatcher.forwardEvent(AppEvents.Error, caught);
            }

            public void onSuccess(SystemConfigurationWeb result) {
                Dispatcher.forwardEvent(AppEvents.SystemConfigurationInfoReceived, result);
            }
        });
    }

	private void loadCustomFieldsConfigurationData(EntityWeb entity) {
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

				 EntityWeb currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
				 setEntityAttribureNamesToRegistry(currentEntity);
	    	  }
	      }
	    });
	}

	@SuppressWarnings("unchecked")
	private void setEntityAttribureNamesToRegistry(EntityWeb entity) {
		if (entity == null) {
			Registry.register(Constants.PERSON_MODEL_ATTRIBUTE_NAMES, null);
			Registry.register(Constants.PERSON_MODEL_ALL_ATTRIBUTE_NAMES, null);
			return;
		}

		// Get ATTRIBUTE_NAMES from entity model
		List<ModelPropertyWeb> personModelPropertyNames = new ArrayList<ModelPropertyWeb>(entity.getAttributes().size());
  		for (EntityAttributeWeb entityAttribute : entity.getAttributes()) {
  			String name = entityAttribute.getName();
			personModelPropertyNames.add(new ModelPropertyWeb(name, Utility.convertToDescription(name)));
  		}
		Registry.register(Constants.PERSON_MODEL_ATTRIBUTE_NAMES, personModelPropertyNames);


		// Get ALL_ATTRIBUTE_NAMES from entity model and customFieldNames
		List<ModelPropertyWeb> allAttributeNames = new ArrayList<ModelPropertyWeb>();
  		for (EntityAttributeWeb entityAttribute : entity.getAttributes()) {
  			String name = entityAttribute.getName();
  			allAttributeNames.add(new ModelPropertyWeb(name, Utility.convertToDescription(name)));
  		}

		List<ModelPropertyWeb> customFieldNames = (List<ModelPropertyWeb>) Registry.get(Constants.PERSON_MODEL_CUSTOM_FIELD_NAMES);
  		if (customFieldNames != null) {
	  		for (ModelPropertyWeb customFieldName : customFieldNames) {
				String name = customFieldName.getName();
				allAttributeNames.add(new ModelPropertyWeb(name, Utility.convertToDescription(name)));
	  		}
  		}
		Registry.register(Constants.PERSON_MODEL_ALL_ATTRIBUTE_NAMES, allAttributeNames);

	}

}
