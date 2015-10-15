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
package org.openhie.openempi.recordcache.dao.orientdb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openhie.openempi.entity.dao.AsyncQueryCallback;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.entity.dao.orientdb.OrientdbConverter;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.recordcache.RecordCacheEntityInstance;
import org.openhie.openempi.recordcache.dao.RecordCacheDao;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class RecordCacheDaoImpl implements RecordCacheDao
{
    private static final int DEFAULT_BLOCK_SIZE = 10000;

    private EntityDao entityDao;
    private Logger log = Logger.getLogger(getClass());

    private Set<Entity> initializedEntities = new HashSet<Entity>();

    @Override
    public Record loadRecord(Entity entity, Long recordId) {
        Record record = getEntityDao(entity).loadRecord(entity, recordId);
        if (record == null) {
            if (log.isDebugEnabled()) {
                log.debug("Record with id: " + recordId + " was not found in the database.");
            }
        }
        return record;
    }

    /*
    public void loadAllRecordsSync(RecordCacheEntityInstance cache) {
        Entity entity = cache.getEntity();
        StringBuffer sb = new StringBuffer("select");
        sb.append(" from ").append(entity.getName()).append(" where dateVoided is null");
        boolean done = false;
        int firstResult = 0;
        int count = 0;
        int maxResults = DEFAULT_BLOCK_SIZE;
        while (!done) {
            String query = addPagingModifiersToQuery(firstResult, maxResults, sb);
            log.debug("Retrieving all distinct key value pairs using query: " + query);
            List<ODocument> list = getEntityDao(entity).executeQuery(entity, query);
            if (list == null || list.size() == 0) {
                done = true;
                continue;
            }

            for (ODocument odoc : list) {
                ORID orid = odoc.getIdentity();
                Record record = OrientdbConverter.convertODocumentToRecord(getEntityDao(entity).getEntityCacheManager(), entity, odoc);
                record.setRecordId(orid.getClusterPosition());
                cache.addRecord(record);
                count++;
            }
            firstResult += list.size();
            log.info("Loaded a block of " + list.size() + " records into the cache.");
        }
        log.info("Loaded " + count + " records into the cache.");
    }*/

    public void loadAllRecords(final RecordCacheEntityInstance cache) {
        final Entity entity = cache.getEntity();
        StringBuffer sb = new StringBuffer("select");
        sb.append(" from ").append(entity.getName()).append(" where dateVoided is null");
        getEntityDao(entity).executeQueryAsync(entity, sb.toString(), new AsyncQueryCallback() {
            int resultCount=0;
            @Override
            public boolean result(Object iRecord) {
                resultCount++;
                ODocument odoc = (ODocument) iRecord;
                ORID orid = odoc.getIdentity();
                Record record = OrientdbConverter.convertODocumentToRecord(getEntityDao(entity).getEntityCacheManager(), entity, odoc);
                record.setRecordId(orid.getClusterPosition());
                cache.addRecord(record);                
                return false;
            }
            
            @Override
            public void end() {
                log.info("Loaded " + resultCount + " records into the cache for entity " + entity.getName() + ".");
            }
        });
    }
    
    private synchronized EntityDao getEntityDao(Entity entity) {
        boolean found = initializedEntities.contains(entity);
        if (!found) {
            entityDao.initializeStore(entity);
            initializedEntities.add(entity);
        }
        return entityDao;
    }

    private static String addPagingModifiersToQuery(int firstResult, int maxResults, StringBuffer queryOriginal) {
        String query = queryOriginal.toString(); 
        // Add paging modifiers
        if (firstResult > 0) {
            query += " skip " + firstResult;
        }
        if (maxResults > 0) {
            query += " limit " + maxResults;
        }
        return query;
    } 
    
    public EntityDao getEntityDao() {
        return entityDao;
    }

    public void setEntityDao(EntityDao entityDao) {
        this.entityDao = entityDao;
    }
}
