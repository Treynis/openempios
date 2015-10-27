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
package org.openempi.webapp.client;

import java.util.List;

import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.IdentifierWeb;
import org.openempi.webapp.client.model.LoggedLinkListWeb;
import org.openempi.webapp.client.model.LoggedLinkSearchCriteriaWeb;
import org.openempi.webapp.client.model.RecordLinkWeb;
import org.openempi.webapp.client.model.RecordLinksListWeb;
import org.openempi.webapp.client.model.RecordListWeb;
import org.openempi.webapp.client.model.RecordSearchCriteriaWeb;
import org.openempi.webapp.client.model.RecordWeb;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface EntityInstanceDataServiceAsync
{
    void getMatchingEntities(EntityWeb entityModel, RecordWeb entity, AsyncCallback<List<RecordWeb>> callback);

    void getEntityRecordsBySearch(RecordSearchCriteriaWeb searchCriteria, AsyncCallback<RecordListWeb> callback);

    void findEntitiesByIdentifier(RecordSearchCriteriaWeb searchCriteria, AsyncCallback<RecordListWeb> callback);

    void findEntitiesByIdentifier(EntityWeb entityModel, IdentifierWeb identifier,
            AsyncCallback<List<RecordWeb>> callback);

    void loadEntityById(EntityWeb entityModel, Long recordId, AsyncCallback<RecordWeb> callback);

    void addEntity(EntityWeb entityModel, RecordWeb entity, AsyncCallback<RecordWeb> callback);

    void updateEntity(EntityWeb entityModel, RecordWeb entity, AsyncCallback<RecordWeb> callback);

    void deleteEntity(EntityWeb entityModel, RecordWeb entity, AsyncCallback<String> callback);

    void loadRecordLinksPaged(RecordSearchCriteriaWeb searchCriteria, AsyncCallback<RecordLinksListWeb> callback);

    void loadRecordLinks(EntityWeb entityModel, Long leftRecordId, Long rightRecordId, String state, AsyncCallback<RecordLinkWeb> callback);

    void loadRecordLinks(EntityWeb entityModel, String state, int firstResult, int maxResults,
            AsyncCallback<List<RecordLinkWeb>> callback);

    void loadRecordLink(EntityWeb entityModel, RecordLinkWeb linkPair, AsyncCallback<RecordLinkWeb> callback);

    void updateRecordLink(EntityWeb entityModel, RecordLinkWeb linkPair, AsyncCallback<RecordLinkWeb> callback);

    void loadLinksFromRecord(EntityWeb entityModel, RecordWeb entity, AsyncCallback<List<RecordWeb>> callback);

    void getLoggedLinks(LoggedLinkSearchCriteriaWeb search, AsyncCallback<LoggedLinkListWeb> callback);
}
