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
package org.openhie.openempi.entity.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.entity.DataAccessIntent;
import org.openhie.openempi.entity.RecordCacheManager;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLinkState;

import com.orientechnologies.orient.core.record.impl.ODocument;

public interface EntityDao
{
    void initializeStore(Entity entity);

    void shutdownStore(Entity entity);

    List<Record> loadRecords(Entity entity, int firstResult, int maxResults);

    Record loadRecord(Entity entity, Long id);

    List<Long> getAllRecordIds(Entity entity);

    Record updateRecord(Entity entity, Record record) throws ApplicationException;

    void updateRecords(Entity entity, List<Record> records) throws ApplicationException;

    void deleteRecord(Entity entity, Record record) throws ApplicationException;

    void removeRecord(Entity entity, Record record) throws ApplicationException;

    Record saveRecord(Entity entity, Record record);

    Set<Record> saveRecords(Entity entity, Collection<Record> record);

    List<Record> findRecordsByAttributes(Entity entityDef, Record record);

    Long getRecordCount(Entity entity);

    Long getRecordCount(Entity entity, Record record);

    Long getRecordLinkCount(Entity entity, RecordLinkState state);

    Long getRecordCount(Entity entity, Identifier identifier);

    List<Record> findRecordsByAttributes(Entity entity, Record record, int firstResult, int maxResults);

    List<Record> findRecordsByIdentifier(Entity entity, Identifier identifier);

    List<Record> findRecordsByIdentifier(Entity entity, Identifier identifier, int firstResult, int maxResults);
    
    List<Record> findRecordsWithoutIdentifierInDomain(Entity entity, IdentifierDomain domain, boolean hasLinks,
            int firstResult, int maxResult);

    List<ODocument> executeQuery(Entity entity, String query);

    RecordCacheManager getEntityCacheManager();

    RecordLink saveRecordLink(RecordLink link);

    List<RecordLink> saveRecordLinks(List<RecordLink> link);

    RecordLink loadRecordLink(Entity entityDef, String recordLinkId);

    List<RecordLink> loadRecordLinks(Entity entity, RecordLinkState state, int firstResult, int maxResults);

    List<RecordLink> loadRecordLinks(Entity entity, Long recordId);
    
    List<Record> loadRecordLinksById(Entity entity, Long recordId);
    
    List<RecordLink> getRecordLinksBySource(Entity entity, LinkSource linkSource, RecordLinkState state);

    void removeRecordLink(RecordLink link);
    
    int removeRecordLinksBySource(Entity entity, LinkSource linkSource, RecordLinkState state);
    
    void declareIntent(Entity entity, DataAccessIntent intent);
}
