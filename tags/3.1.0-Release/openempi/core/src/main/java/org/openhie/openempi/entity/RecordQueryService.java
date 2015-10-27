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
package org.openhie.openempi.entity;

import java.util.List;
import java.util.Date;

import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.model.User;
import org.openhie.openempi.model.IdentifierUpdateEvent;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


public interface RecordQueryService
{
    List<Record> findRecordsByAttributes(Entity entity, Record record);

    List<Record> findRecordsByAttributes(Entity entity, Record record, int firstResult, int maxResults);

    Long getRecordCount(Entity entity, Record record);

    List<Record> findRecordsByIdentifier(Entity entity, Identifier identifier);

    List<Record> findRecordsByIdentifier(Entity entity, Identifier identifier, int firstResult, int maxResults);

    List<Record> findRecordsByBlocking(Entity entity, Record record);
    
    Long getRecordCount(Entity entity, Identifier identifier);

    Record loadRecordById(Entity entity, Long recordId);

    List<Record> loadRecordsById(Entity entity, List<Long> recordIds);

    List<Record> loadRecordLinksByRecordId(Entity entity, Long recordId);

    List<Record> loadAllRecordsPaged(Entity entity, Record record, int firstResult, int maxResults);

    List<RecordLink> loadRecordLinks(Entity entity, Long recordId, RecordLinkState state);

    List<RecordLink> loadRecordLinks(Entity entity, RecordLinkState state, int firstResult, int maxResults);

    List<RecordLink> findRecordLinksBySource(Entity entity, LinkSource linkSource, RecordLinkState state);

    Long getRecordLinkCount(Entity entity, RecordLinkState state);

    List<RecordLink> loadRecordLinks(Entity entity, Long recordId);

    RecordLink loadRecordLink(Entity entity, String recordLinkId);

    int getNotificationCount(User user);
    
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    List<IdentifierUpdateEvent> retrieveNotifications(int startIndex, int maxEvents, Boolean removeRecords, User eventRecipient);

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    List<IdentifierUpdateEvent> retrieveNotifications(Boolean removeRecords, User eventRecipient);

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    List<IdentifierUpdateEvent> retrieveNotificationsByDate(Date startDate, Boolean removeRecords, User eventRecipient);
}
