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
package org.openempi.webapp.client.mvc.user;

import java.util.Date;
import java.util.List;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.domain.AuthenticationException;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.UserFileWeb;
import org.openempi.webapp.client.model.FileLoaderConfigurationWeb;
import org.openempi.webapp.client.model.UserWeb;
import org.openempi.webapp.client.mvc.Controller;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserFileController extends Controller
{
    private UserFileView userFileView;

    public UserFileController() {
        this.registerEventTypes(AppEvents.FileListView);
        this.registerEventTypes(AppEvents.FileListUpdate);
        this.registerEventTypes(AppEvents.FileEntryRemove);
        this.registerEventTypes(AppEvents.FileEntryImport);
    }

    public void initialize() {
        userFileView = new UserFileView(this);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type == AppEvents.FileListView) {
            // updateUserFileData();
            getFileLoaderConfigurations();
            forwardToView(userFileView, event);
        } else if (type == AppEvents.FileListUpdate) {
            updateUserFileData();
        } else if (type == AppEvents.FileListUpdateDataProfile) {
            updateUserFileDataProfile();
        } else if (type == AppEvents.FileEntryRemove) {
            List<UserFileWeb> fileList = event.getData();
            deleteUserFileEntries(fileList);
        } else if (type == AppEvents.FileEntryImport) {
            List<UserFileWeb> fileList = event.getData();
            importUserFileEntries(fileList);
        } else if (type == AppEvents.FileEntryDataProfile) {
            List<UserFileWeb> fileList = event.getData();
            dataProfileFileEntries(fileList);
        }
    }

    private void deleteUserFileEntries(List<UserFileWeb> fileEntries) {
        final int deleteTotal = fileEntries.size();
        final int[] count = {0};

        for (UserFileWeb userFile : fileEntries) {
            getUserFileDataService().removeUserFile(userFile.getUserFileId(), new AsyncCallback<Void>()
            {
                public void onFailure(Throwable caught) {
                    // Dispatcher.forwardEvent(AppEvents.Error, caught);

                    if (caught instanceof AuthenticationException) {
                        Dispatcher.get().dispatch(AppEvents.Logout);
                        return;
                    }
                    forwardToView(userFileView, AppEvents.Error, caught.getMessage());
                }

                public void onSuccess(Void value) {
                    count[0]++;
                    if (count[0] == deleteTotal) {
                        updateUserFileData();
                    }
                }
            });
        }
        // updateUserFileData();
    }

    private void importUserFileEntries(List<UserFileWeb> fileEntries) {
        // Info.display("Information", "importUserFileEntries");
        final EntityWeb currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
        if (currentEntity == null) {
            return;
        }

        final int fileEntrySize = fileEntries.size();
        final int[] failureCount = {0};
        final int[] processCount = {0};

        for (UserFileWeb userFile : fileEntries) {
            getUserFileDataService().importUserFile(userFile, new AsyncCallback<String>()
            {

                public void onFailure(Throwable caught) {
                    // forwardToView(userFileView, AppEvents.Error, caught.getMessage());

                    if (caught instanceof AuthenticationException) {
                        Dispatcher.get().dispatch(AppEvents.Logout);
                        return;
                    }

                    failureCount[0]++;
                    processCount[0]++;
                    if (processCount[0] == fileEntrySize && failureCount[0] > 0) {
                        String error;
                        if (failureCount[0] == 1) {
                            error = "There are one file import failure.";
                        } else {
                            error = "There are " + failureCount[0] + " files import failure.";
                        }
                        logInfoMessage(currentEntity.getName(), error, new Date());
                        forwardToView(userFileView, AppEvents.Error, error);
                    }
                }

                public void onSuccess(String value) {

                    updateUserFileData();

                    processCount[0]++;
                    if (processCount[0] == fileEntrySize) {
                        if (failureCount[0] > 0) {
                            if (processCount[0] == fileEntrySize && failureCount[0] > 0) {
                                String error;
                                if (failureCount[0] == 1) {
                                    error = "There are one file import failure.";
                                } else {
                                    error = "There are " + failureCount[0] + " files import failure.";
                                }
                                logInfoMessage(currentEntity.getName(), error, new Date());
                                forwardToView(userFileView, AppEvents.Error, error);
                            }
                        } else {
                            // Info.display("Information", value);
                            Info.display("Information", "Initiating the process of importing file.");
                            if (fileEntrySize == 1) {
                                logInfoMessage(currentEntity.getName(), "Initiating the process of importing file.", new Date());
                                forwardToView(userFileView, AppEvents.FileEntryImportSuccess, "Initiating the process of importing file");
                            } else {
                                logInfoMessage(currentEntity.getName(), "Initiating the process of importing files.", new Date());
                                forwardToView(userFileView, AppEvents.FileEntryImportSuccess, "Initiating the process of importing files");
                            }
                        }
                    }
                }
            });
        }
    }

    private void dataProfileFileEntries(List<UserFileWeb> fileEntries) {
        final UserFileWeb userFile = fileEntries.get(0);
        final EntityWeb currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
        if (currentEntity == null) {
            return;
        }
        userFile.setEntity(currentEntity);
        getUserFileDataService().dataProfileUserFile(userFile, new AsyncCallback<String>()
        {
            public void onFailure(Throwable caught) {
                if (caught instanceof AuthenticationException) {
                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                String error = "Launching the data profile operation failed due to: " + caught.getMessage();
                logInfoMessage(userFile.getEntity().getName(), error, new Date());
                forwardToView(userFileView, AppEvents.Error, caught.getMessage());
            }

            public void onSuccess(String value) {
                logInfoMessage(userFile.getEntity().getName(), "Initiated the data profile operation.", new Date());
                forwardToView(userFileView, AppEvents.FileEntryDataProfileSuccess,
                        "Initiated the data profile operation");
            }
        });
    }

    private void getFileLoaderConfigurations() {
        getUserFileDataService().getFileLoaderConfigurations(new AsyncCallback<List<FileLoaderConfigurationWeb>>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {
                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                forwardToView(userFileView, AppEvents.Error, caught.getMessage());
            }

            public void onSuccess(List<FileLoaderConfigurationWeb> result) {
                forwardToView(userFileView, AppEvents.FileLoaderConfigurations, result);
            }
        });

    }

    private void updateUserFileData() {
        UserWeb loginUser = Registry.get(Constants.LOGIN_USER);
        if (loginUser != null) {
            getUserFileDataService().getUserFiles(loginUser.getUsername(), false, new AsyncCallback<List<UserFileWeb>>()
            {
                public void onFailure(Throwable caught) {
                    // Dispatcher.forwardEvent(AppEvents.Error, caught);

                    if (caught instanceof AuthenticationException) {
                        Dispatcher.get().dispatch(AppEvents.Logout);
                        return;
                    }
                    forwardToView(userFileView, AppEvents.Error, caught.getMessage());
                }

                public void onSuccess(List<UserFileWeb> result) {
                    forwardToView(userFileView, AppEvents.FileListRender, result);
                }
            });
        }

    }

    private void updateUserFileDataProfile() {
        UserWeb loginUser = Registry.get(Constants.LOGIN_USER);
        if (loginUser != null) {
            getUserFileDataService().getUserFiles(loginUser.getUsername(), false, new AsyncCallback<List<UserFileWeb>>()
            {
                public void onFailure(Throwable caught) {

                    if (caught instanceof AuthenticationException) {
                        Dispatcher.get().dispatch(AppEvents.Logout);
                        return;
                    }
                    forwardToView(userFileView, AppEvents.Error, caught.getMessage());
                }

                public void onSuccess(List<UserFileWeb> result) {
                    forwardToView(userFileView, AppEvents.FileListRenderDataProfile, result);
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
