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
package org.openempi.webapp.client.mvc.manage;

import java.util.List;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.IdentifierDomainDataServiceAsync;
import org.openempi.webapp.client.domain.AuthenticationException;
import org.openempi.webapp.client.model.IdentifierDomainWeb;
import org.openempi.webapp.client.mvc.Controller;

public class ManageIdentifierDomainController extends Controller
{
    private ManageIdentifierDomainView manageIdentifierDomainView;

    public ManageIdentifierDomainController() {
        this.registerEventTypes(AppEvents.ManageIdentifierDomainView);
    }

    @Override
    protected void initialize() {
        manageIdentifierDomainView = new ManageIdentifierDomainView(this);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type == AppEvents.ManageIdentifierDomainView) {

            forwardToView(manageIdentifierDomainView, event);
        } else if (type == AppEvents.ManageIdentifierDomainRequest) {

            requestIdentifierDomainData();
        } else if (type == AppEvents.ManageIdentifierDomainAdd) {

            addIdentifierDomain(event);
        } else if (type == AppEvents.ManageIdentifierDomainUpdate) {

            updateIdentifierDomain(event);
        } else if (type == AppEvents.ManageIdentifierDomainDelete) {

            deleteIdentifierDomain(event);
        }
    }

    private void requestIdentifierDomainData() {
        IdentifierDomainDataServiceAsync identifierDomainDataService = getIdentifierDomainDataService();
        identifierDomainDataService.getIdentifierDomains(new AsyncCallback<List<IdentifierDomainWeb>>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {
                    // Dispatcher.get().dispatch(AppEvents.Logout);
                    forwardToView(manageIdentifierDomainView, AppEvents.Logout, null);
                    return;
                }
                Dispatcher.forwardEvent(AppEvents.Error, caught);
            }

            public void onSuccess(List<IdentifierDomainWeb> result) {

                Registry.register(Constants.IDENTITY_DOMAINS, result);

                forwardToView(manageIdentifierDomainView, AppEvents.ManageIdentifierDomainReceived, result);
            }
        });
    }

    private void addIdentifierDomain(AppEvent event) {
        IdentifierDomainWeb domain = (IdentifierDomainWeb) event.getData();

        IdentifierDomainDataServiceAsync identifierDomainDataService = getIdentifierDomainDataService();
        identifierDomainDataService.addIdentifierDomain(domain, (new AsyncCallback<IdentifierDomainWeb>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {
                    // Dispatcher.get().dispatch(AppEvents.Logout);
                    forwardToView(manageIdentifierDomainView, AppEvents.Logout, null);
                    return;
                }
                forwardToView(manageIdentifierDomainView, AppEvents.Error, caught.getMessage());
            }

            public void onSuccess(IdentifierDomainWeb domain) {
                forwardToView(manageIdentifierDomainView, AppEvents.ManageIdentifierDomainAddComplete, domain);
            }
        }));

    }

    private void updateIdentifierDomain(AppEvent event) {
        IdentifierDomainWeb domain = (IdentifierDomainWeb) event.getData();

        IdentifierDomainDataServiceAsync identifierDomainDataService = getIdentifierDomainDataService();
        identifierDomainDataService.updateIdentifierDomain(domain, (new AsyncCallback<IdentifierDomainWeb>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {
                    // Dispatcher.get().dispatch(AppEvents.Logout);
                    forwardToView(manageIdentifierDomainView, AppEvents.Logout, null);
                    return;
                }
                forwardToView(manageIdentifierDomainView, AppEvents.Error, caught.getMessage());
            }

            public void onSuccess(IdentifierDomainWeb domain) {
                forwardToView(manageIdentifierDomainView, AppEvents.ManageIdentifierDomainUpdateComplete, domain);
            }
        }));

    }

    private void deleteIdentifierDomain(AppEvent event) {
        IdentifierDomainWeb domain = (IdentifierDomainWeb) event.getData();

        IdentifierDomainDataServiceAsync identifierDomainDataService = getIdentifierDomainDataService();
        identifierDomainDataService.deleteIdentifierDomain(domain, (new AsyncCallback<String>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {
                    // Dispatcher.get().dispatch(AppEvents.Logout);
                    forwardToView(manageIdentifierDomainView, AppEvents.Logout, null);
                    return;
                }
                forwardToView(manageIdentifierDomainView, AppEvents.Error, caught.getMessage());
            }

            public void onSuccess(String message) {
                forwardToView(manageIdentifierDomainView, AppEvents.ManageIdentifierDomainDeleteComplete, message);
            }
        }));

    }
}
