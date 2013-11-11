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

import com.google.gwt.user.client.rpc.RemoteService;

public interface EntityInstanceDataService extends RemoteService
{
    List<RecordWeb> getMatchingEntities(EntityWeb entityModel, RecordWeb entity) throws Exception;

    RecordListWeb getEntityRecordsBySearch(RecordSearchCriteriaWeb searchCriteria) throws Exception;

    RecordListWeb findEntitiesByIdentifier(RecordSearchCriteriaWeb searchCriteria) throws Exception;

    List<RecordWeb> findEntitiesByIdentifier(EntityWeb entityModel, IdentifierWeb identifier) throws Exception;

    RecordWeb loadEntityById(EntityWeb entityModel, Long recordId) throws Exception;

    RecordWeb addEntity(EntityWeb entityModel, RecordWeb entity) throws Exception;

    RecordWeb updateEntity(EntityWeb entityModel, RecordWeb entity) throws Exception;

    String deleteEntity(EntityWeb entityModel, RecordWeb entity) throws Exception;

    RecordLinksListWeb loadRecordLinksPaged(RecordSearchCriteriaWeb searchCriteria) throws Exception;

    List<RecordLinkWeb> loadRecordLinks(EntityWeb entityModel, String state, int firstResult, int maxResults)
            throws Exception;

    RecordLinkWeb loadRecordLink(EntityWeb entityModel, RecordLinkWeb linkPair) throws Exception;

    RecordLinkWeb updateRecordLink(EntityWeb entityModel, RecordLinkWeb linkPair) throws Exception;

    List<RecordWeb> loadLinksFromRecord(EntityWeb entityModel, RecordWeb entity) throws Exception;

    LoggedLinkListWeb getLoggedLinks(LoggedLinkSearchCriteriaWeb search) throws Exception;
}
