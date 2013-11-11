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
package org.openhie.openempi.blocking.basicblockinghp.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhie.openempi.Constants;
import org.openhie.openempi.blocking.basicblockinghp.dao.BlockingDao;
import org.openhie.openempi.configuration.BaseField;
import org.openhie.openempi.configuration.BlockingRound;
import org.openhie.openempi.configuration.ConfigurationRegistry;
import org.openhie.openempi.configuration.MatchField;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.NameValuePair;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.notification.EventObservable;
import org.openhie.openempi.notification.ObservationEventType;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class BlockingServiceCache
{
    protected final Log log = LogFactory.getLog(getClass());
    private final static String RECORD_CACHE = "recordCache.cache";

    private BlockingDao blockingDao;
    private CacheManager cacheManager;
    private Cache recordCache;
    private List<BlockingRound> blockingRounds;
    private Integer maximumBlockSize;
    private List<String> fieldList;
    private Map<String, Cache> cacheByRound = new HashMap<String, Cache>();
    private Entity entity;
    private ThreadPoolTaskExecutor cacheTaskExecutor;

    public BlockingServiceCache() {

    }

    public BlockingServiceCache(Entity entity) {
        this.entity = entity;
    }

    public void init(List<BlockingRound> blockingRounds) {
        this.blockingRounds = blockingRounds;

        long startTime = System.currentTimeMillis();
        log.info("Initializing the cache using " + blockingRounds.size() + " blocking rounds.");
        fieldList = buildFieldList();
        List<Future<Object>> futures = new LinkedList<Future<Object>>();
        Future<Object> future;
        LoadCacheTask recordTask = new LoadCacheTask(true, RECORD_CACHE, null);
        try {
            future = cacheTaskExecutor.getThreadPoolExecutor().submit(recordTask, new Object());
            futures.add(future);
        } catch (TaskRejectedException e) {
            log.error("Interrupted while loading blocking Round Cache: " + e, e);
            try {
                Thread.sleep(5000);
            } catch (Exception exe) {
            }
        }
        for (BlockingRound round : blockingRounds) {
            log.info("Building cache for round: " + round.getName());
            LoadCacheTask task = new LoadCacheTask(false, round.getName(), round);

            try {
                future = cacheTaskExecutor.getThreadPoolExecutor().submit(task, new Object());
                futures.add(future);
            } catch (TaskRejectedException e) {
                log.error("Interrupted while loading blocking Round Cache: " + e, e);
                try {
                    Thread.sleep(5000);
                } catch (Exception exe) {
                }
            }
        }

        try {
            // now wait until all jobs have completed
            for (Future<Object> futureObj : futures) {
                futureObj.get();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while loading blocking Round Cache: " + e, e);
        } catch (ExecutionException e) {
            log.error("Interrupted while loading blocking Round Cache: " + e, e);
        }

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        log.info("BlockingServiceCache init time: " + elapsedTime);
    }

    private class LoadCacheTask implements Runnable
    {

        private Boolean isRecordCache;
        private String cacheName;
        private BlockingRound round;

        public LoadCacheTask(Boolean isRecordCache, String cacheName, BlockingRound round) {
            this.isRecordCache = isRecordCache;
            this.cacheName = cacheName;
            this.round = round;
        }

        public void run() {
            if (isRecordCache == true) {
                try {
                    recordCache = cacheManager.getCache(RECORD_CACHE);
                    log.info("BlockingServiceCache: cacheManagerRecordCache is " + cacheManager);
                    cacheAllRecords(fieldList);
                } catch (Exception e) {
                    log.error("Interrupted while loading blocking Record Cache: " + e, e);
                }
            } else {
                Cache cache = cacheManager.getCache(cacheName + ".cache");
                if (cache == null) {
                    cacheManager.addCache(cacheName + ".cache");
                    cache = cacheManager.getCache(cacheName + ".cache");
                }
                cacheByRound.put(cacheName, cache);
                initializeCacheForRound(round, cache);
            }
        }
    }

    private void cacheAllRecords(List<String> blockingFieldList) {
        blockingDao.loadAllRecords(entity, recordCache, blockingFieldList);
    }

    @SuppressWarnings("unchecked")
    private List<String> buildFieldList() {
        Map<String, Object> configurationData = (Map<String, Object>) Context.getConfiguration()
                .lookupConfigurationEntry(ConfigurationRegistry.MATCH_CONFIGURATION);
        if (configurationData == null) {
            log.error("No blocking configuration has been provided.");
            throw new RuntimeException("The blocking configuration is invalid.");
        }
        List<MatchField> fields = (List<MatchField>) configurationData.get(Constants.MATCHING_FIELDS_REGISTRY_KEY);
        Set<String> fieldNames = new HashSet<String>();
        for (MatchField field : fields) {
            fieldNames.add(field.getFieldName());
        }
        for (BlockingRound round : blockingRounds) {
            for (BaseField field : round.getFields()) {
                fieldNames.add(field.getFieldName());
            }
        }
        List<String> fieldNameList = new java.util.ArrayList<String>();
        fieldNameList.add("personId");
        fieldNameList.addAll(fieldNames);
        log.info("Obtained a cached field list of: " + new ToStringBuilder(this).append("Field List", fieldNameList));
        return fieldNameList;
    }

    public void removeCaches(List<BlockingRound> blockingRounds) {
        for (BlockingRound round : blockingRounds) {
            Cache cache = cacheByRound.get(round.getName());
            log.info("Removing cache " + cache + " for round: " + round.getName());
            if (cache != null) {
                cacheManager.removeCache(cache.getName());
            }
        }
        recordCache.removeAll();
    }

    public void shutdown() {
        cacheManager.shutdown();
        cacheTaskExecutor.shutdown();
    }

    public int getCandidateRecordCount(BlockingRound round, String blockingKeyValue) {
        Cache cache = cacheByRound.get(round.getName());
        if (cache == null) {
            log.error("Unexpected error occured; nable to locate a cache for blocking round " + round.getName());
            throw new RuntimeException(
                    "Unable to locate a cache to retrieve blocking records from implying a system configuration issue.");
        }
        Element elem = cache.get(blockingKeyValue);
        if (elem == null) {
            return 0;
        }
        @SuppressWarnings("unchecked")
        List<Long> pointers = (List<Long>) elem.getObjectValue();
        return pointers.size();
    }

    public List<Record> loadCandidateRecords(BlockingRound round, String blockingKeyValue) {
        List<Record> records = new java.util.ArrayList<Record>();
        Cache cache = cacheByRound.get(round.getName());
        if (cache == null) {
            log.error("Unexpected error occured; nable to locate a cache for blocking round " + round.getName());
            throw new RuntimeException(
                    "Unable to locate a cache to retrieve blocking records from implying a system configuration issue.");
        }
        Element elem = cache.get(blockingKeyValue);
        if (elem == null) {
            return records;
        }
        @SuppressWarnings("unchecked")
        List<Long> pointers = (List<Long>) elem.getObjectValue();
        log.debug("Using key " + blockingKeyValue + " found " + pointers.size() + " candidate records.");
        return blockRecords(pointers);
    }

    public Record loadRecord(Long recordId) {
        Element element = recordCache.get(recordId);
        if (element == null) {
            log.debug("Did not find a record in the cache; loading from the database record with id: " + recordId);
            Record record = blockingDao.loadRecord(entity, recordCache, recordId);
            element = new Element(recordId, record);
            recordCache.put(element);
        }
        return (Record) element.getObjectValue();
    }

    private List<Record> blockRecords(List<Long> pointers) {
        List<Record> records = new java.util.ArrayList<Record>();
        for (Long key : pointers) {
            Record record = loadRecord(new Long(key));
            log.trace("Located the record " + key + " in the cache.");
            records.add(record);
        }
        return records;
    }

    public void updateRecordCache(EventObservable o, Record record) {
        if (o.getType() == ObservationEventType.ENTITY_ADD_EVENT) {
            Element element = new Element(record.getRecordId(), record);
            recordCache.put(element);
            log.trace("Added a new record to the record cache with id: " + record.getRecordId());
        } else if (o.getType() == ObservationEventType.ENTITY_DELETE_EVENT) {
            Element found = recordCache.get(record.getRecordId());
            if (found == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Couldn't find record entry in cache with key " + record.getRecordId()
                            + " to be removed.");
                }
                return;
            }
            recordCache.remove(record.getRecordId());
            log.trace("Daleted a record from the record cache with id: " + record.getRecordId());
        } else if (o.getType() == ObservationEventType.ENTITY_UPDATE_EVENT) {
            Element element = new Element(record.getRecordId(), record);
            recordCache.put(element);
            log.trace("Updated a record in the record cache with id: " + record.getRecordId());
        }
    }

    public void addRecordToIndex(BlockingRound round, String blockingKeyValue, Long value) {
        Cache cache = cacheByRound.get(round.getName());
        if (cache == null) {
            log.error("Unexpected error occured; unable to locate a cache for blocking round " + round.getName());
            throw new RuntimeException(
                    "Unable to locate a cache to retrieve blocking records from implying a system configuration issue.");
        }
        if (log.isTraceEnabled()) {
            log.trace("Adding new index value: (" + blockingKeyValue + "," + value + ")");
        }
        addEntryToCache(cache, blockingKeyValue, value);
    }

    @SuppressWarnings("unchecked")
    private void addEntryToCache(Cache cache, String key, Long value) {
        Element found = cache.get(key);
        List<Long> pointers = null;
        if (found == null) {
            pointers = new java.util.ArrayList<Long>();
        } else {
            pointers = (List<Long>) found.getObjectValue();
            if (maximumBlockSize > 0 && pointers.size() > maximumBlockSize) {
                log.info("Skipping adding value to block with key " + key + " and size " + pointers.size()
                        + " because it exceeds the maximum block size.");
                return;
            }
        }
        pointers.add(value);
        found = new Element(key, pointers);
        cache.put(found);
    }

    public void deleteRecordFromIndex(BlockingRound round, String key, Long value) {
        Cache cache = cacheByRound.get(round.getName());
        if (cache == null) {
            log.error("Unexpected error occured; unable to locate a cache for blocking round " + round.getName());
            throw new RuntimeException(
                    "Unable to locate a cache to retrieve blocking records from implying a system configuration issue.");
        }
        if (log.isTraceEnabled()) {
            log.trace("Removing index value: (" + key + "," + value + ")");
        }
        deleteEntryFromCache(cache, key, value);
    }

    @SuppressWarnings("unchecked")
    private void deleteEntryFromCache(Cache cache, String key, Long value) {
        Element found = cache.get(key);
        List<Long> pointers = null;
        if (found == null) {
            if (log.isTraceEnabled()) {
                log.trace("Couldn't find index entry with key: " + key + " in the cache " + cache + " to be removed.");
            }
            return;
        } else {
            pointers = (List<Long>) found.getObjectValue();
        }
        @SuppressWarnings("rawtypes")
        List<Long> newPointers = new java.util.ArrayList();
        for (Long pointer : pointers) {
            if (pointer.longValue() != value.longValue()) {
                newPointers.add(pointer);
            }
        }
        found = new Element(key, newPointers);
        cache.put(found);
    }

    @SuppressWarnings("unchecked")
    public List<Record> loadCandidateRecords(String blockingKeyValue) {
        List<Record> records = new java.util.ArrayList<Record>();
        for (BlockingRound round : blockingRounds) {
            // log.debug("Loading records for round: " + round.getName());
            Cache cache = cacheByRound.get(round.getName());
            if (cache == null) {
                log.error("Unexpected error occured; unable to locate a cache for blocking round " + round.getName());
                throw new RuntimeException(
                        "Unable to locate a cache to retrieve blocking records from implying a system configuration issue.");
            }
            Element elem = cache.get(blockingKeyValue);
            if (elem == null) {
                continue;
            }
            List<Long> pointers = new CopyOnWriteArrayList<Long>();
            // List<Integer> pointers = (List<Integer>) elem.getValue();
            pointers = (List<Long>) elem.getObjectValue();
            log.debug("Using key " + blockingKeyValue + " found " + pointers.size() + " candidate records.");
            records.addAll(blockRecords(pointers));
        }
        return records;
    }

    public List<String> getKeyListByRound(BlockingRound round) {
        if (round == null) {
            return new java.util.ArrayList<String>();
        }
        Cache cache = cacheByRound.get(round.getName());
        if (cache == null) {
            return new java.util.ArrayList<String>();
        }
        @SuppressWarnings("unchecked")
        List<String> keyList = (List<String>) cache.getKeys();
        return keyList;
    }

    private void initializeCacheForRound(BlockingRound round, Cache cache) {
        List<BaseField> fields = round.getFields();
        List<NameValuePair> pairs = blockingDao.getDistinctKeyValuePairs(entity, getBlockingFieldList(fields));
        populateCache(cache, pairs);
    }

    private void populateCache(Cache cache, List<NameValuePair> pairs) {
        for (NameValuePair pair : pairs) {
            addEntryToCache(cache, pair.getName(), (Long) pair.getValue());
        }
    }

    private List<String> getBlockingFieldList(List<BaseField> fields) {
        List<String> fieldList = new java.util.ArrayList<String>(fields.size());
        for (BaseField field : fields) {
            fieldList.add(field.getFieldName());
        }
        return fieldList;
    }

    public Integer getMaximumBlockSize() {
        return maximumBlockSize;
    }

    public void setMaximumBlockSize(Integer maximumBlockSize) {
        this.maximumBlockSize = maximumBlockSize;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public BlockingDao getBlockingDao() {
        return blockingDao;
    }

    public void setBlockingDao(BlockingDao blockingDao) {
        this.blockingDao = blockingDao;
    }

    public void setCacheTaskExecutor(ThreadPoolTaskExecutor cacheTaskExecutor) {
        this.cacheTaskExecutor = cacheTaskExecutor;
    }

    public ThreadPoolTaskExecutor getCacheTaskExecutor() {
        return cacheTaskExecutor;
    }
}
