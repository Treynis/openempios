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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;

import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.recordcache.dao.RecordCacheDao;
import org.openhie.openempi.service.impl.BaseServiceImpl;

public class RecordCacheEntityInstance extends BaseServiceImpl
{
    private final static String RECORD_CACHE = "recordCache.cache";

    private Entity entity;
    private Cache recordCache;
    private CacheManager cacheManager;
    private RecordCacheDao recordCacheDao;

    public RecordCacheEntityInstance(Entity entity, CacheManager cacheManager, RecordCacheDao recordCacheDao) {
        this.entity = entity;
        this.cacheManager = cacheManager;
        recordCache = cacheManager.getCache(getCacheName());
        if (recordCache == null) {
            CacheConfiguration cacheConfiguration = cacheManager.getConfiguration().getDefaultCacheConfiguration();
            recordCache = new Cache(cacheConfiguration);
            recordCache.setName(getCacheName());
            cacheManager.addCache(recordCache);
        }
        this.recordCacheDao = recordCacheDao;
    }

    public void addRecord(Record record) {
        Element element = new Element(record.getRecordId(), record);
        recordCache.put(element);
    }

    public Record loadRecord(Entity entity, Long recordId) {
        Element element = recordCache.get(recordId);
        if (element == null) {
            log.debug("Did not find a record in the cache; loading from the database record with id: " + recordId);
            Record record = recordCacheDao.loadRecord(entity, recordId);
            element = new Element(recordId, record);
            recordCache.put(element);
        }
        return (Record) element.getObjectValue();
    }

    public void removeCache() {
        if (recordCache != null && recordCache.getStatus() != Status.STATUS_SHUTDOWN
                && cacheManager.getStatus() != Status.STATUS_SHUTDOWN) {
            recordCache.removeAll();
        }
    }

    private String getCacheName() {
        return RECORD_CACHE + "." + entity.getName();
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Cache getRecordCache() {
        return recordCache;
    }

    public void setRecordCache(Cache recordCache) {
        this.recordCache = recordCache;
    }
}
