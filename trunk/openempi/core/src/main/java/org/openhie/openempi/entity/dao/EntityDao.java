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
import org.openhie.openempi.entity.RecordCacheManager;
import org.openhie.openempi.model.DataAccessIntent;
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
    public void initializeStore(Entity entity);

    public void shutdownStore(Entity entity);

    public List<Record> loadRecords(Entity entity, int firstResult, int maxResults);

    public Record loadRecord(Entity entity, Long id);
    
    public Object loadObject(Entity entity, String recordId);
    
    public List<Long> getAllRecordIds(Entity entity);

    public Record updateRecord(Entity entity, Record record) throws ApplicationException;

    public void updateRecords(Entity entity, List<Record> records) throws ApplicationException;

    public void deleteRecord(Entity entity, Record record) throws ApplicationException;

    public void removeRecord(Entity entity, Record record) throws ApplicationException;

    public Record saveRecord(Entity entity, Record record);

    public Set<Record> saveRecords(Entity entity, Collection<Record> record);

    public List<Record> findRecordsByAttributes(Entity entity, Record record);

    public Long getRecordCount(Entity entity);

    public Long getRecordCount(Entity entity, Record record);

    public Long getRecordLinkCount(Entity entity, RecordLinkState state);

    public Long getRecordCount(Entity entity, Identifier identifier);

    public Set<Record> loadDirtyRecords(Entity entity, int maxResults);
    
    public List<Record> findRecordsByAttributes(Entity entity, Record record, int firstResult, int maxResults);

    public List<Record> findRecordsByIdentifier(Entity entity, Identifier identifier);

    public List<Record> findRecordsByIdentifier(Entity entity, Identifier identifier, int firstResult, int maxResults);
    
    public List<Record> findRecordsWithoutIdentifierInDomain(Entity entity, IdentifierDomain domain, boolean hasLinks,
            int firstResult, int maxResult);

    public void saveData(Entity entity, String className, Record record);
    
    public List<ODocument> executeQuery(Entity entity, String query);

    public void executeQueryAsync(Entity entity, String query, AsyncQueryCallback callback);

    public RecordCacheManager getEntityCacheManager();

    public RecordLink saveRecordLink(RecordLink link);

    public List<RecordLink> saveRecordLinks(List<RecordLink> link);

    public RecordLink loadRecordLink(Entity entityDef, String recordLinkId);

    public List<RecordLink> loadRecordLinks(Entity entity, RecordLinkState state, int firstResult, int maxResults);

    public List<RecordLink> loadRecordLinks(Entity entity, Long recordId);

    public List<RecordLink> loadRecordLinks(Entity entity, Long recordId, RecordLinkState state);
    
    public List<Record> loadRecordLinksById(Entity entity, Long recordId);
    
    public List<RecordLink> getRecordLinksBySource(Entity entity, LinkSource linkSource, RecordLinkState state);

    public void removeRecordLink(RecordLink link);
    
    public int removeRecordLinksBySource(Entity entity, LinkSource linkSource, RecordLinkState state);
    
    public void declareIntent(Entity entity, DataAccessIntent intent);
    
    public void createClass(Entity baseEntity, Entity classEntity, String baseClass) throws ApplicationException;
    
    public boolean classExists(Entity baseEntity, String className) throws ApplicationException;
    
    public void dropClass(Entity baseEntity, String className) throws ApplicationException;
    
    public void createIndexes(Entity entity) throws ApplicationException;
    
    public void dropIndexes(Entity entity) throws ApplicationException;
}
