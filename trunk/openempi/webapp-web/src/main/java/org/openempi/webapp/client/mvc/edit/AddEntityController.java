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
package org.openempi.webapp.client.mvc.edit;

import java.util.List;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.EntityDefinitionDataServiceAsync;
import org.openempi.webapp.client.EntityInstanceDataServiceAsync;
import org.openempi.webapp.client.domain.AuthenticationException;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.PersonWeb;
import org.openempi.webapp.client.model.RecordWeb;
import org.openempi.webapp.client.mvc.Controller;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.extjs.gxt.ui.client.widget.Info;

public class AddEntityController extends Controller
{
    private AddEntityView addEntityView;

    public AddEntityController() {
        this.registerEventTypes(AppEvents.EntityAddView);
    }

    @Override
    protected void initialize() {
        addEntityView = new AddEntityView(this);
    }

    public void addPersonToRepository(PersonWeb person) {

    }

    public void checkEntityDuplicate(PersonWeb person) {

    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type == AppEvents.EntityAddView) {
            forwardToView(addEntityView, event);
        } else if (type == AppEvents.EntitiesRequest) {
            loadEntities();
        } else if (type == AppEvents.EntityAdd) {
            EntityWeb entityModel = event.getData("entityModel");
            RecordWeb entity = event.getData();
            addEntity(entityModel, entity);
        }
    }

    private void loadEntities() {
        // Info.display("Information", "Submitting request to load entities");

        EntityDefinitionDataServiceAsync entityDataService = getEntityDefinitionDataService();

        entityDataService.loadEntities(new AsyncCallback<List<EntityWeb>>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {

                    // forwardToView(addEntityView, AppEvents.Logout,null);
                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                forwardToView(addEntityView, AppEvents.Error, caught.getMessage());
            }

            public void onSuccess(List<EntityWeb> result) {

                forwardToView(addEntityView, AppEvents.EntitiesReceived, result);
            }
        });
    }

    private void addEntity(EntityWeb entityModel, RecordWeb entity) {
        // Info.display("Information", "Submitting request to add entity instance");

        EntityInstanceDataServiceAsync entityDataService = getEntityInstanceDataService();

        entityDataService.addEntity(entityModel, entity, new AsyncCallback<RecordWeb>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {

                    forwardToView(addEntityView, AppEvents.Logout, null);
                    return;
                }
                forwardToView(addEntityView, AppEvents.Error, caught.getMessage());
            }

            public void onSuccess(RecordWeb result) {

                forwardToView(addEntityView, AppEvents.EntityAddComplete, result);
            }
        });
    }
}
