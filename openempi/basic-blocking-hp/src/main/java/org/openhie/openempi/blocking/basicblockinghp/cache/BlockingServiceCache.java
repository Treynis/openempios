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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.blocking.basicblockinghp.BlockingKeyValueGenerator;
import org.openhie.openempi.blocking.basicblockinghp.dao.BlockingDao;
import org.openhie.openempi.blocking.basicblockinghp.dao.BlockingRoundClass;
import org.openhie.openempi.configuration.BlockingRound;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.ForEachRecordConsumer;
import org.openhie.openempi.entity.RecordConsumer;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.entity.dao.orientdb.ConcurrentModificationException;
import org.openhie.openempi.entity.impl.AbstractRecordConsumer;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.Record;

public class BlockingServiceCache
{
    public final static String BLOCKINGKEYVALUE_FIELD = "blockingKeyValue";
    public final static String RECORDIDS_FIELD = "rids";
    protected final Log log = LogFactory.getLog(getClass());

    private BlockingDao blockingDao;
    private EntityDao entityDao;
    private List<BlockingRound> blockingRounds;
    private Integer maximumBlockSize;
    private Map<String, BlockingRoundClass> roundClassByRound = new HashMap<String, BlockingRoundClass>();
    private Entity entity;

    public BlockingServiceCache() {

    }

    public BlockingServiceCache(Entity entity) {
        this.entity = entity;
    }

    public void init(List<BlockingRound> blockingRounds, boolean isReinitialize) {
        this.blockingRounds = blockingRounds;

        long startTime = System.currentTimeMillis();
        if (isReinitialize) {
            dropBlockingClasses(blockingRounds);
        }
        createBlockingClasses(blockingRounds);
        
        List<BlockingRoundClass> blockingRoundsNeedData= new ArrayList<BlockingRoundClass>();
        for (BlockingRound round : blockingRounds) {
            BlockingRoundClass roundClass = roundClassByRound.get(round.getName());
            long blockCount = blockingDao.loadBlockDataCount(entity, roundClass);
            if (blockCount == 0) {
                log.info("Need to load data for this round.");
                blockingRoundsNeedData.add(roundClass);
            }
        }
        if (blockingRoundsNeedData.size() > 0) {
            loadBlockingRoundData(entity, blockingRoundsNeedData, isReinitialize);
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        log.info("BlockingServiceCache init time: " + elapsedTime);
    }

    private void loadBlockingRoundData(Entity entity, List<BlockingRoundClass> blockingRounds, boolean isReinitialize) {
        ForEachRecordConsumer forEach = Context.getForEachRecordConsumer();
        Set<RecordConsumer> consumers = new HashSet<RecordConsumer>();
        for (BlockingRoundClass roundClass : blockingRounds) {
            RecordConsumerForBlocking consumer = new RecordConsumerForBlocking(roundClass);
            consumer.setEntity(entity);
            consumers.add(consumer);
        }
        forEach.startProcess(consumers, true);
    }
    
    private class RecordConsumerForBlocking extends AbstractRecordConsumer {
        private BlockingRoundClass blockingRoundClass;
        
        public RecordConsumerForBlocking(BlockingRoundClass blockingRoundClass) {
            this.blockingRoundClass = blockingRoundClass;
        }
        
        @Override
        public void run() {
            int count = 0;
            boolean done = false;
            try {
                while (!done) {
                    Record record = getQueue().poll(5, TimeUnit.SECONDS);
                    if (record != null) {
                        count++;
                        String blockingKeyValue = BlockingKeyValueGenerator
                                .generateBlockingKeyValue(blockingRoundClass.getBlockingRound().getFields(), record);
                        addRecordToBlock(record, blockingRoundClass, blockingKeyValue);            
                        if (count % 10000 == 0) {
                            System.out.println("Consumer " + Thread.currentThread().getName() + " processed " + count + " records.");
                        }
                    } else {
                        done = true;
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Finished the while loop.");
                e.printStackTrace();
            } finally {
                getLatch().countDown();
            }
        }
        
        @SuppressWarnings("unchecked")
        private void addRecordToBlock(Record record, BlockingRoundClass roundClass, String blockingKeyValue) {
            long startLoadTime = new Date().getTime();
            boolean done=true;
            Record blockRecord = null;
            do {
                try {
                    blockRecord = getBlockingDao().loadBlockData(entity, roundClass, blockingKeyValue);
                    if (blockRecord == null) {
                        blockRecord = new Record(roundClass.getRoundClass());
                        Set<Long> rids = new HashSet<Long>();
                        rids.add(record.getRecordId());
                        populateBlockData(blockRecord, blockingKeyValue, rids);
                    } else {
                        Set<Long> rids = (Set<Long>) blockRecord.get(RECORDIDS_FIELD);
                        if (maximumBlockSize > 0 && rids.size() >= maximumBlockSize) {
                            if (log.isTraceEnabled()) {
                                log.trace("Block with key " + blockingKeyValue + " exceeded the maximum block size of " + maximumBlockSize);
                            }
                            break;
                        }
                        rids.add(record.getRecordId());
                    }
                    getBlockingDao().saveBlockData(entity, roundClass, blockRecord);
                    done=true;
                } catch (ConcurrentModificationException e) {
                    done=false;
                }
            } while (!done);
            long loadTime = new Date().getTime() - startLoadTime;
            if (log.isTraceEnabled()) {
                log.trace("Added a record to block: " + blockRecord.get(RECORDIDS_FIELD) + " in " + loadTime + " msec.");
            }
        }

        private void populateBlockData(Record record, String blockingKeyValue, Set<Long> rids) {
            record.set(BLOCKINGKEYVALUE_FIELD, blockingKeyValue);
            record.set(RECORDIDS_FIELD, rids);
        }
    }
    
    private void createBlockingClasses(List<BlockingRound> blockingRounds) {
        long startTime = new Date().getTime();
        for (BlockingRound round : blockingRounds) {
            log.info("The round is called: " + round);
            Entity roundEntity = buildEntityFromRound(round);
            try {
                if (!entityDao.classExists(entity, roundEntity.getName())) {
                    entityDao.createClass(entity, roundEntity, "V");
                }
                roundClassByRound.put(round.getName(), new BlockingRoundClass(round, roundEntity));
            } catch (ApplicationException e) {
                log.error("Failed while creating the class " + roundEntity.getName() + " due to " + e, e);
                throw new RuntimeException("Initialization of the blocking algorithm failed.");
            }
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        log.info("BlockingServiceCache created classes in time: " + elapsedTime);
    }
    
    private void dropBlockingClasses(List<BlockingRound> blockingRounds) {
        long startTime = new Date().getTime();
        for (BlockingRound round : blockingRounds) {
            log.info("The round is called: " + round);
            Entity roundEntity = buildEntityFromRound(round);
            try {
                entityDao.dropClass(entity, roundEntity.getName());
            } catch (ApplicationException e) {
                log.error("Failed while creating the class " + roundEntity.getName() + " due to " + e, e);
                throw new RuntimeException("Initialization of the blocking algorithm failed.");
            }
        }
        roundClassByRound.clear();
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        log.info("BlockingServiceCache dropped classes in time: " + elapsedTime);
    }

    private Entity buildEntityFromRound(BlockingRound round) {
        Entity entity = new Entity();
        entity.setName("Blocking" + getEntityNameFromRoundName(round.getName()));
        EntityAttribute attrib = new EntityAttribute();
        attrib.setName("blockingKeyValue");
        attrib.setDatatype(new EntityAttributeDatatype(EntityAttributeDatatype.STRING_DATATYPE_CD));
        attrib.setIndexed(true);
        entity.addAttribute(attrib);
        
        attrib = new EntityAttribute();
        attrib.setName("rids");
        attrib.setDatatype(new EntityAttributeDatatype(EntityAttributeDatatype.EMBEDDEDSET_DATATYPE_CD));
        attrib.setIndexed(false);
        entity.addAttribute(attrib);
        return entity;
    }

    private String getEntityNameFromRoundName(String name) {
        // TODO OrientDB cannot handle '.' in the name
        name = name.replace('.', '-');
        return name;
    }

    public void shutdown() {
    }

    @SuppressWarnings("unchecked")
    public int getCandidateRecordCount(BlockingRound round, String blockingKeyValue) {
        BlockingRoundClass roundClass = roundClassByRound.get(round.getName());
        Record blockRecord = getBlockingDao().loadBlockData(entity, roundClass, blockingKeyValue);
        Set<Long> pointers = (Set<Long>) blockRecord.get(RECORDIDS_FIELD);
        return pointers.size();
    }

    private List<Record> blockRecords(Collection<Long> pointers) {
        List<Record> records = new java.util.ArrayList<Record>();
        for (Long key : pointers) {
            Record record = loadRecord(new Long(key));
            if (record == null) {
                log.warn("Unable to find record with id " + key + " in the db.");
                continue;
            }
            log.trace("Located the record " + key + " in the cache.");
            records.add(record);
        }
        return records;
    }

    public Record loadRecord(Long recordId) {
        return entityDao.loadRecord(entity, recordId);
    }

    public void addRecordToIndex(Record record) {
        if (Context.getDataAccessIntent() != null) {
            if (log.isTraceEnabled()) {
                log.trace("Ignoring updating the blocking index due to a data access intent in-place.");
            }
            return;
        }
        Context.executeTask(new BlockingDataGenerator(entity, record, BlockingTaskWorkType.ADD_RECORD_WORK));
    }

    public void addRecordsToIndex(Set<Record> records) {
        if (Context.getDataAccessIntent() != null) {
            if (log.isTraceEnabled()) {
                log.trace("Ignoring updating the blocking index due to a data access intent in-place.");
            }
            return;
        }
        Context.executeTask(new BlockingDataGenerator(entity, records, BlockingTaskWorkType.ADD_RECORDS_WORK));
    }

    public void deleteRecordFromIndex(Record record) {
        if (Context.getDataAccessIntent() != null) {
            if (log.isTraceEnabled()) {
                log.trace("Ignoring updating the blocking index due to a data access intent in-place.");
            }
            return;
        }
        Context.executeTask(new BlockingDataGenerator(entity, record, BlockingTaskWorkType.DELETE_RECORD_WORK));
    }

    public void updateRecordInIndex(Record preUpdateRecord, Record postUpdateRecord) {
        if (Context.getDataAccessIntent() != null) {
            if (log.isTraceEnabled()) {
                log.trace("Ignoring updating the blocking index due to a data access intent in-place.");
            }
            return;
        }
        Context.executeTask(new BlockingDataGenerator(entity, preUpdateRecord, postUpdateRecord, BlockingTaskWorkType.UPDATE_RECORD_WORK));
    }

    @SuppressWarnings("unchecked")
    public List<Record> loadCandidateRecords(BlockingRound round, String blockingKeyValue) {
        List<Record> records = new java.util.ArrayList<Record>();
        BlockingRoundClass roundClass = roundClassByRound.get(round.getName());
        Record blockRecord = getBlockingDao().loadBlockData(entity, roundClass, blockingKeyValue);
        if (blockRecord == null) {
            return records;
        }
        Set<Long> pointers = (Set<Long>) blockRecord.get(RECORDIDS_FIELD);
        if (log.isDebugEnabled()) {
            log.debug("Using key " + blockingKeyValue + " found " + pointers.size() + " candidate records.");
        }
        return blockRecords(pointers);
    }

    public List<Record> loadCandidateRecords(Set<Long> pointers) {
        List<Record> records = new java.util.ArrayList<Record>();
        records.addAll(blockRecords(pointers));
        return records;
    }

    public Set<String> loadBlockRecordIds() {
        Set<String> allBlockRecordIds = new HashSet<String>();
        for (BlockingRound round : blockingRounds) {
            BlockingRoundClass roundClass = roundClassByRound.get(round.getName());
            Set<String> blockRecordIds = getBlockingDao().loadBlockRecordIds(entity, roundClass);
            allBlockRecordIds.addAll(blockRecordIds);
        }
        return allBlockRecordIds;
    }

    public Integer getMaximumBlockSize() {
        return maximumBlockSize;
    }

    public void setMaximumBlockSize(Integer maximumBlockSize) {
        this.maximumBlockSize = maximumBlockSize;
    }

    public BlockingDao getBlockingDao() {
        return blockingDao;
    }

    public void setBlockingDao(BlockingDao blockingDao) {
        this.blockingDao = blockingDao;
    }

    public EntityDao getEntityDao() {
        return entityDao;
    }

    public void setEntityDao(EntityDao entityDao) {
        this.entityDao = entityDao;
    }
    
    enum BlockingTaskWorkType {
        LOAD_WORK, ADD_RECORD_WORK, ADD_RECORDS_WORK, UPDATE_RECORD_WORK, DELETE_RECORD_WORK
    }
    
    public class BlockingDataGenerator implements Runnable
    {
        private Entity entity;
        private List<BlockingRoundClass> blockingClasses;
        private BlockingTaskWorkType workType;
        private Record record;
        private Record newRecord;
        private Set<Record> records;
        
        public BlockingDataGenerator(Entity entity, List<BlockingRoundClass> blockingClasses, BlockingTaskWorkType workType) {
            this.entity = entity;
            this.blockingClasses = blockingClasses;
            this.workType = workType;
        }

        public BlockingDataGenerator(Entity entity, Set<Record> records, BlockingTaskWorkType workType) {
            this.entity = entity;
            this.records = records;
            this.workType = workType;
        }

        public BlockingDataGenerator(Entity entity, Record record, BlockingTaskWorkType workType) {
            this.entity = entity;
            this.record = record;
            this.workType = workType;
        }

        public BlockingDataGenerator(Entity entity, Record record, Record newRecord, BlockingTaskWorkType workType) {
            this.entity = entity;
            this.record = record;
            this.newRecord = newRecord;
            this.workType = workType;
        }

        @Override
        public void run() {
            switch(workType) {
            case LOAD_WORK:
                loadBlocks();
                break;
            case ADD_RECORD_WORK:
                addRecordToIndex();
                break;
            case ADD_RECORDS_WORK:
                addRecordsToIndex();
                break;
            case DELETE_RECORD_WORK:
                deleteRecordFromIndex();
                break;
            case UPDATE_RECORD_WORK:
                updateRecordInIndex();
                break;
            default:
                log.warn("Task has now known work to do: " + workType);
                break;
            }
        }
        
        @SuppressWarnings("unchecked")
        private void updateRecordInIndex() {
            for (BlockingRound round : blockingRounds) {
                BlockingRoundClass roundClass = roundClassByRound.get(round.getName());
                String blockingKeyValue = BlockingKeyValueGenerator
                        .generateBlockingKeyValue(roundClass.getBlockingRound().getFields(), record);
                String newBlockingKeyValue = BlockingKeyValueGenerator
                        .generateBlockingKeyValue(roundClass.getBlockingRound().getFields(), newRecord);
                if (blockingKeyValue.equalsIgnoreCase(newBlockingKeyValue)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Record update of record " + record.getRecordId() + 
                                " resulted in the same blockingKey so there is nothing to do." + blockingKeyValue);
                        return;
                    }
                }
                long startLoadTime = new Date().getTime();
                boolean done = true;
                Record blockRecord = null;
                do {
                    try {
                        blockRecord = getBlockingDao().loadBlockData(getEntity(), roundClass, blockingKeyValue);
                        if (blockRecord == null) {
                            log.warn("Received a request to delete a record from the index that is not in the index: " + record.getRecordId());
                            return;
                        }
                        Set<Long> rids = (Set<Long>) blockRecord.get(RECORDIDS_FIELD);
                        rids.remove(record.getRecordId());
                        getBlockingDao().saveBlockData(getEntity(), roundClass, blockRecord);
                        
                        blockRecord = getBlockingDao().loadBlockData(getEntity(), roundClass, newBlockingKeyValue);
                        if (blockRecord == null) {
                            blockRecord = new Record(roundClass.getRoundClass());
                            rids = new HashSet<Long>();
                            rids.add(getNewRecord().getRecordId());
                            populateBlockData(blockRecord, blockingKeyValue, rids);
                        } else {
                            rids = (Set<Long>) blockRecord.get(RECORDIDS_FIELD);
                            rids.add(getNewRecord().getRecordId());
                        }
                        getBlockingDao().saveBlockData(getEntity(), roundClass, blockRecord);
                        done = true;
                    } catch (ConcurrentModificationException e) {
                        done = false;
                    }
                } while (!done);
                long endLoadTime = new Date().getTime();
                if (log.isDebugEnabled()) {
                    log.debug("Updated a record in a block: " + blockRecord.get(RECORDIDS_FIELD) + " in " + (endLoadTime-startLoadTime) + " msec.");
                }
            }
        }
        
        @SuppressWarnings("unchecked")
        private void deleteRecordFromIndex() {
            for (BlockingRound round : blockingRounds) {
                BlockingRoundClass roundClass = roundClassByRound.get(round.getName());
                String blockingKeyValue = BlockingKeyValueGenerator
                        .generateBlockingKeyValue(roundClass.getBlockingRound().getFields(), record);
                long startLoadTime = new Date().getTime();
                boolean done = true;
                Record blockRecord = null;
                do {
                    try {
                        blockRecord = getBlockingDao().loadBlockData(getEntity(), roundClass, blockingKeyValue);
                        if (blockRecord == null) {
                            log.warn("Received a request to delete a record from the index that is not in the index: " + record.getRecordId());
                            return;
                        }
                        Set<Long> rids = (Set<Long>) blockRecord.get(RECORDIDS_FIELD);
                        rids.remove(record.getRecordId());
                        getBlockingDao().saveBlockData(getEntity(), roundClass, blockRecord);
                        done = true;
                    } catch (ConcurrentModificationException e) {
                        done = false;
                    }
                } while (!done);
                long endLoadTime = new Date().getTime();
                if (log.isDebugEnabled()) {
                    log.debug("Deleted a record from a block: " + blockRecord.get(RECORDIDS_FIELD) + " in " + (endLoadTime-startLoadTime) + " msec.");
                }
            }
        }
        
        private void addRecordToIndex() {
            for (BlockingRound round : blockingRounds) {
                BlockingRoundClass roundClass = roundClassByRound.get(round.getName());
                String blockingKeyValue = BlockingKeyValueGenerator
                        .generateBlockingKeyValue(roundClass .getBlockingRound().getFields(), record);
                if (log.isDebugEnabled()) {
                    log.debug("Adding record to index: " + record);
                }
                addRecordToBlock(record, roundClass, blockingKeyValue);
            }
        }
        
        private void addRecordsToIndex() {
            for (BlockingRound round : blockingRounds) {
                BlockingRoundClass roundClass = roundClassByRound.get(round.getName());
                if (log.isDebugEnabled()) {
                    log.debug("Adding records to index: " + records.size());
                }
                for (Record aRecord : records) {
                    String blockingKeyValue = BlockingKeyValueGenerator
                            .generateBlockingKeyValue(roundClass .getBlockingRound().getFields(), aRecord);
                    addRecordToBlock(aRecord, roundClass, blockingKeyValue);
                }
            }
        }

        private void loadBlocks() {
            List<Long> recordIds = entityDao.getAllRecordIds(entity);
            if (recordIds != null && recordIds.size() > 0) {
                int count=0;
                for (Long rid : recordIds) {
                    count++;
                    Record record = entityDao.loadRecord(entity, rid);
                    if (record == null) {
                        log.warn("Couldn't find record with iD " + rid);
                        continue;
                    }
                    if (count % 1000 == 0) {
                        log.info("Working on record " + count);
                    }
                    for (BlockingRoundClass roundClass : blockingClasses) {
                        String blockingKeyValue = BlockingKeyValueGenerator
                                .generateBlockingKeyValue(roundClass.getBlockingRound().getFields(), record);
                        addRecordToBlock(record, roundClass, blockingKeyValue);
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void addRecordToBlock(Record record, BlockingRoundClass roundClass, String blockingKeyValue) {
            long startLoadTime = new Date().getTime();
            boolean done=true;
            Record blockRecord = null;
            do {
                try {
                    blockRecord = getBlockingDao().loadBlockData(entity, roundClass, blockingKeyValue);
                    if (blockRecord == null) {
                        blockRecord = new Record(roundClass.getRoundClass());
                        Set<Long> rids = new HashSet<Long>();
                        rids.add(record.getRecordId());
                        populateBlockData(blockRecord, blockingKeyValue, rids);
                    } else {
                        Set<Long> rids = (Set<Long>) blockRecord.get(RECORDIDS_FIELD);
                        if (maximumBlockSize > 0 && rids.size() >= maximumBlockSize) {
                            if (log.isTraceEnabled()) {
                                log.trace("Block with key " + blockingKeyValue + " exceeded the maximum block size of " + maximumBlockSize);
                            }
                            break;
                        }
                        rids.add(record.getRecordId());
                    }
                    getBlockingDao().saveBlockData(entity, roundClass, blockRecord);
                    done=true;
                } catch (ConcurrentModificationException e) {
                    done=false;
                }
            } while (!done);
            long loadTime = new Date().getTime() - startLoadTime;
            if (log.isTraceEnabled()) {
                log.trace("Added a record to block: " + blockRecord.get(RECORDIDS_FIELD) + " in " + loadTime + " msec.");
            }
        }
        
        public Record getNewRecord() {
            return newRecord;
        }

        public void setNewRecord(Record newRecord) {
            this.newRecord = newRecord;
        }

        public Entity getEntity() {
            return entity;
        }

        public void setEntity(Entity entity) {
            this.entity = entity;
        }

        public Record getRecord() {
            return record;
        }

        public void setRecord(Record record) {
            this.record = record;
        }

        public BlockingTaskWorkType getWorkType() {
            return workType;
        }

        public void setWorkType(BlockingTaskWorkType workType) {
            this.workType = workType;
        }

        private void populateBlockData(Record record, String blockingKeyValue, Set<Long> rids) {
            record.set(BLOCKINGKEYVALUE_FIELD, blockingKeyValue);
            record.set(RECORDIDS_FIELD, rids);
        }
    }
}
