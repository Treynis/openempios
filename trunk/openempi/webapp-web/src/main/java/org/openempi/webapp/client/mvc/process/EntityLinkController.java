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
package org.openempi.webapp.client.mvc.process;

import java.util.List;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.ConfigurationDataServiceAsync;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.EntityDefinitionDataServiceAsync;
import org.openempi.webapp.client.EntityInstanceDataServiceAsync;
import org.openempi.webapp.client.domain.AuthenticationException;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.ExactMatchingConfigurationWeb;
import org.openempi.webapp.client.model.MatchConfigurationWeb;
import org.openempi.webapp.client.model.RecordLinkWeb;
import org.openempi.webapp.client.mvc.Controller;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.extjs.gxt.ui.client.widget.Info;

public class EntityLinkController extends Controller
{
    private EntityLinkView entityLinkView;

    private Integer maxRecords = new Integer(15);

    public EntityLinkController() {
        this.registerEventTypes(AppEvents.EntityLinkView);
    }

    @Override
    protected void initialize() {
        entityLinkView = new EntityLinkView(this);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type == AppEvents.EntityLinkView) {

            forwardToView(entityLinkView, event);

        } else if (type == AppEvents.DeterministicMatchConfigurationRequest) {

            // Deterministic Match Configuration
            requestMatchConfigurationData();

        } else if (type == AppEvents.MatchConfigurationRequest) {

            // Probabilistic Match Configuration
            requestProbabilisticMatchConfigurationData();

        } else if (type == AppEvents.EntitiesRequest) {

            loadEntities();

        } else if (type == AppEvents.EntityLinkPairsRequest) {

            EntityWeb entityModel = event.getData("entityModel");

            if (Registry.get(Constants.MAX_RECORD_DISPLAING) != null) {
                int maxRecords = ((Integer) Registry.get(Constants.MAX_RECORD_DISPLAING)).intValue();
                if (maxRecords == 0) {
                    maxRecords = 1;
                }
                loadEntityRecordLinks(entityModel, "M", 0, maxRecords);
            } else {
                Registry.register(Constants.MAX_RECORD_DISPLAING, maxRecords);
                loadEntityRecordLinks(entityModel, "M", 0, maxRecords.intValue());
            }

        } else if (type == AppEvents.EntityOneLinkPairRequest) {

            EntityWeb entityModel = event.getData("entityModel");
            RecordLinkWeb linkPair = event.getData("linkPair");
            loadRecordLink(entityModel, linkPair);

        } else if (type == AppEvents.ProcessLink) {
            EntityWeb entityModel = event.getData("entityModel");
            RecordLinkWeb linkPair = event.getData("linkPair");
            processLink(entityModel, linkPair);

        } else if (type == AppEvents.ProcessUnlink) {
            EntityWeb entityModel = event.getData("entityModel");
            RecordLinkWeb linkPair = event.getData("linkPair");
            processUnlink(entityModel, linkPair);
        }
    }

    private void loadEntities() {
        // Info.display("Information", "Submitting request to load entities");

        EntityDefinitionDataServiceAsync entityDataService = getEntityDefinitionDataService();

        entityDataService.loadEntities(new AsyncCallback<List<EntityWeb>>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {

                    // forwardToView(entityLinkView, AppEvents.Logout,null);
                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                forwardToView(entityLinkView, AppEvents.Error, caught.getMessage());
            }

            public void onSuccess(List<EntityWeb> result) {

                forwardToView(entityLinkView, AppEvents.EntitiesReceived, result);
            }
        });
    }

    private void requestMatchConfigurationData() {
        ConfigurationDataServiceAsync configurationDataService = getConfigurationDataService();
        configurationDataService.loadExactMatchingConfiguration(new AsyncCallback<ExactMatchingConfigurationWeb>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {
                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                Dispatcher.forwardEvent(AppEvents.Error, caught);
            }

            public void onSuccess(ExactMatchingConfigurationWeb result) {
                /*
                 * Log.debug("Received the exact matching configuration data: " + result); for (MatchFieldWeb field :
                 * result.getMatchFields()) { Log.debug("Match Field: " + field.getFieldName() + "," +
                 * field.getComparatorFunctionName() + "," + field.getMatchThreshold()); }
                 */
                forwardToView(entityLinkView, AppEvents.DeterministicMatchConfigurationReceived, result);
            }
        });
    }

    private void requestProbabilisticMatchConfigurationData() {
        ConfigurationDataServiceAsync configurationDataService = getConfigurationDataService();
        configurationDataService.loadProbabilisticMatchingConfiguration(new AsyncCallback<MatchConfigurationWeb>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {
                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                Dispatcher.forwardEvent(AppEvents.Error, caught);
            }

            public void onSuccess(MatchConfigurationWeb result) {
                forwardToView(entityLinkView, AppEvents.MatchConfigurationReceived, result);
            }
        });
    }

    private void loadEntityRecordLinks(EntityWeb entityModel, String state, int firstResult, int maxResults) {
        // Info.display("Information", "Submitting request to load entityRecordLinks");

        EntityInstanceDataServiceAsync entityDataService = getEntityInstanceDataService();

        entityDataService.loadRecordLinks(entityModel, "M", firstResult, maxResults,
                new AsyncCallback<List<RecordLinkWeb>>()
                {
                    public void onFailure(Throwable caught) {

                        if (caught instanceof AuthenticationException) {

                            Dispatcher.get().dispatch(AppEvents.Logout);
                            return;
                        }
                        forwardToView(entityLinkView, AppEvents.Error, caught.getMessage());
                    }

                    public void onSuccess(List<RecordLinkWeb> result) {

                        forwardToView(entityLinkView, AppEvents.EntityLinkPairsRequest, result);
                    }
                });
    }

    private void loadRecordLink(EntityWeb entityModel, RecordLinkWeb linkPair) {
        // Info.display("Information", "Submitting request to load entityRecordLinks");

        EntityInstanceDataServiceAsync entityDataService = getEntityInstanceDataService();

        entityDataService.loadRecordLink(entityModel, linkPair, new AsyncCallback<RecordLinkWeb>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {

                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                forwardToView(entityLinkView, AppEvents.Error, caught.getMessage());
            }

            public void onSuccess(RecordLinkWeb result) {

                forwardToView(entityLinkView, AppEvents.EntityOneLinkPairRequest, result);
            }
        });
    }

    public void processLink(EntityWeb entityModel, RecordLinkWeb pair) {
        pair.setState(Constants.MATCH);
        EntityInstanceDataServiceAsync entityDataService = getEntityInstanceDataService();

        entityDataService.updateRecordLink(entityModel, pair, new AsyncCallback<RecordLinkWeb>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {
                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                Dispatcher.forwardEvent(AppEvents.Error, caught);
            }

            public void onSuccess(RecordLinkWeb value) {
                forwardToView(entityLinkView, AppEvents.ProcessPairLinkedView, value);
            }
        });
    }

    public void processUnlink(EntityWeb entityModel, RecordLinkWeb pair) {
        pair.setState(Constants.NON_MATCH);
        EntityInstanceDataServiceAsync entityDataService = getEntityInstanceDataService();

        entityDataService.updateRecordLink(entityModel, pair, new AsyncCallback<RecordLinkWeb>()
        {
            public void onFailure(Throwable caught) {

                if (caught instanceof AuthenticationException) {
                    Dispatcher.get().dispatch(AppEvents.Logout);
                    return;
                }
                Dispatcher.forwardEvent(AppEvents.Error, caught);
            }

            public void onSuccess(RecordLinkWeb value) {
                forwardToView(entityLinkView, AppEvents.ProcessPairUnlinkedView, value);
            }
        });
    }
}
