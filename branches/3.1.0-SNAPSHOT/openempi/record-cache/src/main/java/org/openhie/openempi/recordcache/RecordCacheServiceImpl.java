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
package org.openhie.openempi.recordcache;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Status;

import org.openhie.openempi.InitializationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.notification.EventObservable;
import org.openhie.openempi.notification.ObservationEventType;
import org.openhie.openempi.recordcache.dao.RecordCacheDao;
import org.openhie.openempi.service.impl.BaseServiceImpl;

public class RecordCacheServiceImpl extends BaseServiceImpl implements RecordCacheLifecycleObserver, RecordCacheService
{
    private CacheManager cacheManager;
    private RecordCacheDao recordCacheDao;
    private boolean preloadAllRecords = false;
    private Map<String,RecordCacheEntityInstance> cacheByEntity = new HashMap<String,RecordCacheEntityInstance>();

    public void startup() throws InitializationException {
        log.info("Starting the record cache service.");
        try {
            List<Entity> entities = Context.getEntityDefinitionManagerService().loadEntities();
            for (Entity entity : entities) {
                RecordCacheEntityInstance entityRecordCache = new RecordCacheEntityInstance(entity, cacheManager, recordCacheDao);
                if (preloadAllRecords) {
                    cacheAllRecords(entityRecordCache);
                }
                cacheByEntity.put(entity.getName(), entityRecordCache);
            }
            Context.registerObserver(this, ObservationEventType.RECORD_ADD_EVENT);
            Context.registerObserver(this, ObservationEventType.RECORDS_ADD_EVENT);
            Context.registerObserver(this, ObservationEventType.RECORD_DELETE_EVENT);
            Context.registerObserver(this, ObservationEventType.RECORD_UPDATE_EVENT);
            log.info("Finished setting up the Record Cache Service");
        } catch (Exception e) {
            log.error("Encountered error while loading Record Cache: " + e, e);
        }
    }

    public void shutdown() {
        for (RecordCacheEntityInstance cache : cacheByEntity.values()) {
            cache.removeCache();
        }

        if (cacheManager.getStatus() != Status.STATUS_SHUTDOWN) {
            cacheManager.shutdown();
        }
    }

    public Record getRecord(Entity entity, Long recordId) {
        RecordCacheEntityInstance cache = getCacheForEntity(entity);
        if (cache == null) {
            log.warn("There is no cache setup for entity: " + entity);
            return null;
        }
        return cache.loadRecord(entity, recordId);
    }

    public void putRecord(Record record) {
        RecordCacheEntityInstance cache = getCacheForEntity(record.getEntity());
        if (cache == null) {
            log.warn("There is no cache setup for entity: " + record.getEntity());
            return;
        }
        cache.addRecord(record);
    }

    private RecordCacheEntityInstance getCacheForEntity(Entity entity) {
        RecordCacheEntityInstance cache = cacheByEntity.get(entity.getName());
        return cache;
    }

    public void update(Observable observable, Object eventData) {
        if (!(observable instanceof EventObservable) || eventData == null) {
            return;
        }
        EventObservable o = (EventObservable) observable;
        Record record = null;
        Collection<Record> records = null;
        if (o.getType() == ObservationEventType.RECORDS_ADD_EVENT) {
            
        } else {
            record = (Record) eventData;
        }
        Entity entity = record.getEntity();
        RecordCacheEntityInstance cache = getCacheForEntity(record.getEntity());
        if (cache == null) {
            log.warn("There is no cache setup for entity: " + record.getEntity());
            return;
        }

        if (o.getType() == ObservationEventType.RECORD_ADD_EVENT) {
            cache.addRecord(record);
            log.trace("Added a new record to the record cache with id: " + record.getRecordId());
        } else if (o.getType() == ObservationEventType.RECORDS_ADD_EVENT) {
            
        } else if (o.getType() == ObservationEventType.RECORD_DELETE_EVENT) {
            Record found = cache.loadRecord(entity, record.getRecordId());
            if (found == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Couldn't find record entry in cache with key " + record.getRecordId()
                            + " to be removed.");
                }
                return;
            }
            log.trace("Daleted a record from the record cache with id: " + record.getRecordId());
        } else if (o.getType() == ObservationEventType.RECORD_UPDATE_EVENT) {
            cache.addRecord(record);
            log.trace("Updated a record in the record cache with id: " + record.getRecordId());
        }
    }

    private void cacheAllRecords(final RecordCacheEntityInstance entityRecordCache) {
        new Thread() {
            @Override
            public void run() {
                recordCacheDao.loadAllRecords(entityRecordCache);
            }
        }.start();
    }

    public boolean isReady() {
        return false;
    }

    public boolean isDown() {
        return false;
    }

    public RecordCacheDao getRecordCacheDao() {
        return recordCacheDao;
    }

    public void setRecordCacheDao(RecordCacheDao recordCacheDao) {
        this.recordCacheDao = recordCacheDao;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public boolean isPreloadAllRecords() {
        return preloadAllRecords;
    }

    public void setPreloadAllRecords(boolean preloadAllRecords) {
        this.preloadAllRecords = preloadAllRecords;
    }
}
