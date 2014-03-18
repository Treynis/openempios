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
package org.openempi.webapp.client.mvc.search;

import java.util.List;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.EntityDefinitionDataServiceAsync;
import org.openempi.webapp.client.EntityInstanceDataServiceAsync;
import org.openempi.webapp.client.domain.AuthenticationException;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.RecordWeb;
import org.openempi.webapp.client.mvc.Controller;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SearchEntityController extends Controller
{
    private FixedSearchEntityView fixedSearchEntityView;
    private SearchEntityView searchEntityView;
    private UpdateEntityView updateEntityView;
    private DeleteEntityView deleteEntityView;

    public SearchEntityController() {
        this.registerEventTypes(AppEvents.EntityFixedSearchView);
        this.registerEventTypes(AppEvents.EntitySearchView);
        this.registerEventTypes(AppEvents.EntityUpdateView);
        this.registerEventTypes(AppEvents.EntityDeleteView);
    }

    @Override
    protected void initialize() {
        fixedSearchEntityView = new FixedSearchEntityView(this);
        searchEntityView = new SearchEntityView(this);
        updateEntityView = new UpdateEntityView(this);
        deleteEntityView = new DeleteEntityView(this);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type == AppEvents.EntityFixedSearchView) {

            forwardToView(fixedSearchEntityView, event);

        } else if (type == AppEvents.EntitySearchView) {

            forwardToView(searchEntityView, event);

        } else if (type == AppEvents.EntitiesRequest) {

            loadEntities();

        } else if (type == AppEvents.EntityInstancesRequest) {

            EntityWeb entityModel = event.getData("entityModel");
            RecordWeb entity = event.getData();
            search(entityModel, entity);

        } else if (type == AppEvents.EntityUpdateView || type == AppEvents.EntityBasicUpdateView) {

            forwardToView(updateEntityView, event);

        } else if (type == AppEvents.EntityUpdate || type == AppEvents.EntityBasicUpdate) {

            EntityWeb entityModel = event.getData("entityModel");
            RecordWeb entity = event.getData();
            updateEntity(entityModel, entity);

        } else if (type == AppEvents.EntityLinksForFixedSearchRequest) {

            EntityWeb entityModel = event.getData("entityModel");
            RecordWeb entity = event.getData();
            getEntityLinksForFixedSearch(entityModel, entity);

        } else if (type == AppEvents.EntityLinksRequest) {

            EntityWeb entityModel = event.getData("entityModel");
            RecordWeb entity = event.getData();
            getEntityLinks(entityModel, entity);

        } else if (type == AppEvents.EntityBasicUpdateFinished) {

            forwardToView(fixedSearchEntityView, event);

        } else if (type == AppEvents.EntityUpdateFinished) {

            forwardToView(searchEntityView, event);

        } else if (type == AppEvents.EntityBasicUpdateCancel) {

            forwardToView(fixedSearchEntityView, event);

        } else if (type == AppEvents.EntityUpdateCancel) {

            forwardToView(searchEntityView, event);

        } else if (type == AppEvents.EntityDeleteView || type == AppEvents.EntityBasicDeleteView) {

            forwardToView(deleteEntityView, event);

        } else if (type == AppEvents.EntityDelete || type == AppEvents.EntityBasicDelete) {

            EntityWeb entityModel = event.getData("entityModel");
            RecordWeb entity = event.getData();
            deleteEntity(entityModel, entity);

        } else if (type == AppEvents.EntityBasicDeleteFinished) {

            forwardToView(fixedSearchEntityView, event);

        } else if (type == AppEvents.EntityDeleteFinished) {

            forwardToView(searchEntityView, event);

        } else if (type == AppEvents.EntityBasicDeleteCancel) {

            forwardToView(fixedSearchEntityView, event);

        } else if (type == AppEvents.EntityDeleteCancel) {

            forwardToView(searchEntityView, event);
        }
    }

    private void loadEntities() {
        EntityDefinitionDataServiceAsync entityDataService = getEntityDefinitionDataService();

        // Info.display("Information: ", "Submitting request to load entities");

        entityDataService.loadEntities(new AsyncCallback<List<EntityWeb>>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {

                    // forwardToView(searchEntityView, AppEvents.Logout,null);
                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                forwardToView(searchEntityView, AppEvents.Error, caught.getMessage());
            }

            public void onSuccess(List<EntityWeb> result) {

                forwardToView(searchEntityView, AppEvents.EntitiesReceived, result);
            }
        });
    }

    private void search(EntityWeb entityModel, RecordWeb entity) {
        EntityInstanceDataServiceAsync entityDataService = getEntityInstanceDataService();

        // Info.display("Information: ", "Submitting request to search entity instances");

        entityDataService.getMatchingEntities(entityModel, entity, new AsyncCallback<List<RecordWeb>>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {

                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                forwardToView(searchEntityView, AppEvents.Error, caught.getMessage());
            }

            public void onSuccess(List<RecordWeb> result) {

                forwardToView(searchEntityView, AppEvents.EntityInstancesReceived, result);
            }
        });
    }

    private void updateEntity(EntityWeb entityModel, RecordWeb entity) {
        EntityInstanceDataServiceAsync entityDataService = getEntityInstanceDataService();

        // Info.display("Information: ", "Submitting request to update entity instances");

        entityDataService.updateEntity(entityModel, entity, new AsyncCallback<RecordWeb>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {

                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                forwardToView(updateEntityView, AppEvents.Error, caught.getMessage());
            }

            public void onSuccess(RecordWeb result) {

                forwardToView(updateEntityView, AppEvents.EntityUpdateComplete, result);
            }
        });

    }

    private void deleteEntity(EntityWeb entityModel, RecordWeb entity) {
        EntityInstanceDataServiceAsync entityDataService = getEntityInstanceDataService();

        // Info.display("Information: ", "Submitting request to delete entity instances");

        entityDataService.deleteEntity(entityModel, entity, new AsyncCallback<String>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {

                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                forwardToView(deleteEntityView, AppEvents.Error, caught.getMessage());
            }

            public void onSuccess(String result) {

                forwardToView(deleteEntityView, AppEvents.EntityDeleteComplete, result);
            }
        });
    }

    private void getEntityLinks(EntityWeb entityModel, RecordWeb entity) {
        EntityInstanceDataServiceAsync entityDataService = getEntityInstanceDataService();

        // Info.display("Information: ", "Submitting request to get entity links");

        entityDataService.loadLinksFromRecord(entityModel, entity, new AsyncCallback<List<RecordWeb>>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {

                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                forwardToView(searchEntityView, AppEvents.Error, caught.getMessage());
            }

            public void onSuccess(List<RecordWeb> result) {

                forwardToView(searchEntityView, AppEvents.EntityLinksReceived, result);
            }
        });
    }

    private void getEntityLinksForFixedSearch(EntityWeb entityModel, RecordWeb entity) {
        EntityInstanceDataServiceAsync entityDataService = getEntityInstanceDataService();

        // Info.display("Information: ", "Submitting request to get entity links");

        entityDataService.loadLinksFromRecord(entityModel, entity, new AsyncCallback<List<RecordWeb>>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {

                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                forwardToView(fixedSearchEntityView, AppEvents.Error, caught.getMessage());
            }

            public void onSuccess(List<RecordWeb> result) {

                forwardToView(fixedSearchEntityView, AppEvents.EntityLinksReceived, result);
            }
        });
    }
}
