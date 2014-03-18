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
package org.openempi.webapp.client.mvc.admin;

import java.util.Date;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.domain.AuthenticationException;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.mvc.Controller;
import org.openempi.webapp.client.ui.util.Utility;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AdminController extends Controller
{

	public AdminController() {
		this.registerEventTypes(AppEvents.AssignGlobalIdentifier);
		this.registerEventTypes(AppEvents.AdminStartPixPdqServer);
		this.registerEventTypes(AppEvents.AdminStopPixPdqServer);
		this.registerEventTypes(AppEvents.InitializeRepository);
		this.registerEventTypes(AppEvents.LinkAllRecordPairs);
		this.registerEventTypes(AppEvents.InitializeCustomConfiguration);
		this.registerEventTypes(AppEvents.RebuildBlockingIndex);
	}

	@Override
	protected void initialize() {
		super.initialize();
	}


	@Override
	public void handleEvent(AppEvent event) {
		if (event.getType() == AppEvents.AdminStartPixPdqServer) {
            final EntityWeb currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);

			Info.display("Information", "Attempting to start the PIX/PDQ server; please wait...");
            logInfoMessage(currentEntity.getName(), "Attempting to start the PIX/PDQ server.", new Date());

			getAdminService().startPixPdqServer(new AsyncCallback<String>() {
				public void onFailure(Throwable caught) {

					if (caught instanceof AuthenticationException) {
						Dispatcher.get().dispatch(AppEvents.Logout);
						return;
					}
			        Dispatcher.forwardEvent(AppEvents.Error, caught);
				}

				public void onSuccess(String message) {
					if (message == null) {
						Info.display("Information", "The PIX/PDQ server started successfuly.");
			            logInfoMessage(currentEntity.getName(), "The PIX/PDQ server started successfuly.", new Date());
					} else {
						Info.display("Warning", "The PIX/PDQ server did not start successfuly: " + message);
	                    logInfoMessage(currentEntity.getName(), "The PIX/PDQ server did not start successfuly: " + message, new Date());
					}
				}
			});
		} else if (event.getType() == AppEvents.AssignGlobalIdentifier) {
	            final EntityWeb currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
	            if (currentEntity == null) {
	                MessageBox.alert("Information", "No Entity Model selected.  Please select an Entity Model from Entity Model Design.", null);
	                return;
	            }

                Info.display("Information", "Initiating the process of assigning global identifiers; please wait...");
                logInfoMessage(currentEntity.getName(), "Initiating the process of assigning global identifiers.", new Date());

				getAdminService().assignGlobalIdentifiers(currentEntity, new AsyncCallback<String>() {
					public void onFailure(Throwable caught) {

						if (caught instanceof AuthenticationException) {
							Dispatcher.get().dispatch(AppEvents.Logout);
							return;
						}
				        Dispatcher.forwardEvent(AppEvents.Error, caught);
					}

					public void onSuccess(String message) {
						if (message == null) {
							Info.display("Information", "Finished assigning global identifiers successfully.");
                            logInfoMessage(currentEntity.getName(), "Finished assigning global identifiers successfully.", new Date());

						} else {
							Info.display("Warning", "Encountered a problem while assigning global identifiers: " + message);
	                        logInfoMessage(currentEntity.getName(), "Encountered a problem while assigning global identifiers: " + message, new Date());
						}
					}
				});
		} else if (event.getType() == AppEvents.AdminStopPixPdqServer) {
            final EntityWeb currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);

			getAdminService().stopPixPdqServer(new AsyncCallback<String>() {
				public void onFailure(Throwable caught) {

					if (caught instanceof AuthenticationException) {
						Dispatcher.get().dispatch(AppEvents.Logout);
						return;
					}
			        Dispatcher.forwardEvent(AppEvents.Error, caught);
				}

				public void onSuccess(String message) {
					if (message == null) {
						Info.display("Information", "The PIX/PDQ server stopped successfuly.");
	                    logInfoMessage(currentEntity.getName(), "The PIX/PDQ server stopped successfuly.", new Date());
					} else {
						Info.display("Warning", "The PIX/PDQ server did not stop successfuly: " + message);
                        logInfoMessage(currentEntity.getName(), "The PIX/PDQ server did not stop successfuly: " + message, new Date());
					}
				}
			});
		} else if (event.getType() == AppEvents.InitializeRepository) {
			final EntityWeb currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
	      	if (currentEntity == null) {
	      		MessageBox.alert("Information", "No Entity Model selected.  Please select an Entity Model from Entity Model Design.", null);
	      		return;
	      	}

            Info.display("Information", "Initiating the process of initializing links; please wait...");
            logInfoMessage(currentEntity.getName(), "Initiating the process of initializing links.", new Date());

//			getAdminService().initializeRepository(new AsyncCallback<String>() {
			getAdminService().initializeRepository(currentEntity, new AsyncCallback<String>() {
				public void onFailure(Throwable caught) {

					if (caught instanceof AuthenticationException) {
						Dispatcher.get().dispatch(AppEvents.Logout);
						return;
					}
			        Dispatcher.forwardEvent(AppEvents.Error, caught);
				}

				public void onSuccess(String message) {
					if (message == null) {
						Info.display("Information", "Links were initialized successfuly.");
	                    logInfoMessage(currentEntity.getName(), "Links were initialized successfuly.", new Date());
					} else {
						Info.display("Warning", "Links did not initialize successfuly: " + message);
                        logInfoMessage(currentEntity.getName(), "Links did not initialize successfuly: " + message, new Date());
					}
				}
			});
		} else if (event.getType() == AppEvents.LinkAllRecordPairs) {
			final EntityWeb currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
	      	if (currentEntity == null) {
	      		MessageBox.alert("Information", "No Entity Model selected.  Please select an Entity Model from Entity Model Design.", null); 
	      		return;
	      	}

            Info.display("Information", "Initiating the process of regenerating links; please wait...");
            logInfoMessage(currentEntity.getName(), "Initiating the process of regenerating links.", new Date());

			getAdminService().linkAllRecordPairs(currentEntity, new AsyncCallback<String>() {
				public void onFailure(Throwable caught) {

					if (caught instanceof AuthenticationException) {
						Dispatcher.get().dispatch(AppEvents.Logout);
						return;
					}
			        Dispatcher.forwardEvent(AppEvents.Error, caught);
				}

				public void onSuccess(String message) {
					if (message == null) {
						Info.display("Information", "All record pairs were regenerated successfuly.");
                        logInfoMessage(currentEntity.getName(), "All record pairs were regenerated successfuly.", new Date());
					} else {
						Info.display("Warning", "All record pairs did not regenerate successfuly: " + message);
                        logInfoMessage(currentEntity.getName(), "All record pairs did not regenerate successfuly: " + message, new Date());
					}
				}
			});
		} else if (event.getType() == AppEvents.InitializeCustomConfiguration) {
			final EntityWeb currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
	      	if (currentEntity == null) {
	      		MessageBox.alert("Information", "No Entity Model selected.  Please select an Entity Model from Entity Model Design.", null);
	      		return;
	      	}

            Info.display("Information", "Initiating the process of regenerating custom fields; please wait...");
            logInfoMessage(currentEntity.getName(), "Initiating the process of regenerating custom fields.", new Date());

			getAdminService().initializeCustomConfiguration(currentEntity, new AsyncCallback<String>() {
				public void onFailure(Throwable caught) {

					if (caught instanceof AuthenticationException) {
						Dispatcher.get().dispatch(AppEvents.Logout);
						return;
					}
			        Dispatcher.forwardEvent(AppEvents.Error, caught);
				}

				public void onSuccess(String message) {
					if (message == null) {
						Info.display("Information", "Custom fields were regenerated successfuly.");
                        logInfoMessage(currentEntity.getName(), "Custom fields were regenerated successfuly.", new Date());
					} else {
						Info.display("Warning", "Custom fields did not regenerate successfuly: " + message);
                        logInfoMessage(currentEntity.getName(), "Custom fields did not regenerate successfuly: " + message, new Date());
					}
				}
			});
		} else if (event.getType() == AppEvents.RebuildBlockingIndex) {
	        final EntityWeb currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
            if (currentEntity == null) {
                MessageBox.alert("Information", "No Entity Model selected.  Please select an Entity Model from Entity Model Design.", null);
                return;
            }

            Info.display("Information", "Initiating the process of rebuilding blocking indexes; please wait...");
            logInfoMessage(currentEntity.getName(), "Initiating the process of rebuilding blocking indexes.", new Date());

			getAdminService().rebuildBlockingIndex(currentEntity, new AsyncCallback<String>() {
				public void onFailure(Throwable caught) {

					if (caught instanceof AuthenticationException) {
						Dispatcher.get().dispatch(AppEvents.Logout);
						return;
					}
			        Dispatcher.forwardEvent(AppEvents.Error, caught);
				}

				public void onSuccess(String message) {
					if (message == null) {
						Info.display("Information", "Blocking indexes were rebuilt successfuly.");
	                    logInfoMessage(currentEntity.getName(), "Blocking indexes were rebuilt successfuly.", new Date());
					} else {
						Info.display("Warning", "Rebuild blocking indexes did not build successfuly: " + message);
                        logInfoMessage(currentEntity.getName(), "Rebuild blocking indexes did not build successfuly: " + message, new Date());
					}
				}
			});
		}
	}

	public void logInfoMessage(String entity, String message, Date time) {
        BaseModelData infoMessage = new BaseModelData();
        infoMessage.set("entity", entity);
        infoMessage.set("message", message);
        // infoMessage.set("time", Utility.DateTimeToString(time));
        infoMessage.set("time", time);
        Dispatcher.forwardEvent(AppEvents.InformationMessage, infoMessage);
	}

}
