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
package org.openhie.openempi.blocking.basicblockinghp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.openhie.openempi.InitializationException;
import org.openhie.openempi.blocking.AbstractBlockingLifecycleObserver;
import org.openhie.openempi.blocking.BlockingService;
import org.openhie.openempi.blocking.RecordPairSource;
import org.openhie.openempi.blocking.basicblockinghp.cache.BlockingServiceCache;
import org.openhie.openempi.blocking.basicblockinghp.dao.BlockingDao;
import org.openhie.openempi.configuration.BaseField;
import org.openhie.openempi.configuration.BlockingRound;
import org.openhie.openempi.configuration.ConfigurationRegistry;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordPair;
import org.openhie.openempi.notification.EventObservable;
import org.openhie.openempi.notification.ObservationEventType;
import org.openhie.openempi.util.ConvertUtil;

public class BlockingServiceImpl extends AbstractBlockingLifecycleObserver implements BlockingService, Observer
{
    private final static int TRADITIONAL_BLOCKING_ALGORITHM_ID = 2;

	private BlockingDao blockingDao;
	private EntityDao entityDao;
	private Map<String,BlockingConfiguration> configByEntity = new HashMap<String,BlockingConfiguration>();
	private Map<String,BlockingServiceCache> cacheByEntity = new HashMap<String,BlockingServiceCache>();
	private List<Entity> entities;

	public void startup() throws InitializationException {
		log.info("Starting the Traditional Blocking Service");
		entities = Context.getEntityDefinitionManagerService().loadEntities();
		for (Entity entity : entities) {
		    BlockingService service = Context.getBlockingService(entity.getName());
		    if (service.getBlockingServiceId() != getBlockingServiceId()) {
		        continue;
		    }
	        BlockingConfiguration config = loadBlockingConfiguration(entity.getName(), true);
			BlockingServiceCache blockingServiceCache = new BlockingServiceCache(entity);
			blockingServiceCache.setBlockingDao(blockingDao);
			blockingServiceCache.setEntityDao(entityDao);
			blockingServiceCache.setMaximumBlockSize(config.getMaximumBlockSize());
			blockingServiceCache.init(config.getBlockingRounds(), false);
			log.info("Initialized cache for entity " + entity.getName());
			cacheByEntity.put(entity.getName(), blockingServiceCache);
		}
		Context.registerObserver(this, ObservationEventType.RECORD_ADD_EVENT);
        Context.registerObserver(this, ObservationEventType.RECORDS_ADD_EVENT);
		Context.registerObserver(this, ObservationEventType.RECORD_DELETE_EVENT);
		Context.registerObserver(this, ObservationEventType.RECORD_UPDATE_EVENT);
		log.info("Finished building the index for the Traditional Blocking Service");
	}

	public void shutdown() {
        for (Entity entity : entities) {
            BlockingService service = Context.getBlockingService(entity.getName());
            if (service.getBlockingServiceId() != getBlockingServiceId()) {
                continue;
            }
            BlockingServiceCache blockingServiceCache = cacheByEntity.get(entity.getName());
            blockingServiceCache.shutdown();
        }
		log.info("Stopping the Traditional Blocking Service");
	}

	public void rebuildIndex() throws InitializationException {
		log.info("Rebuilding the indices for the Traditional Blocking Service");
		List<Entity> entities = Context.getEntityDefinitionManagerService().loadEntities();
		for (Entity entity : entities) {
            BlockingService service = Context.getBlockingService(entity.getName());
            if (service.getBlockingServiceId() != getBlockingServiceId()) {
                continue;
            }
            BlockingConfiguration config = loadBlockingConfiguration(entity.getName(), true);
			BlockingServiceCache blockingServiceCache = cacheByEntity.get(entity.getName());
			blockingServiceCache.init(config.getBlockingRounds(), true);
		}
		log.info("Finished building the indices for the Traditional Blocking Service");
	}

	@SuppressWarnings("unchecked")
    public void update(Observable o, Object eventData) {
		if (!(o instanceof EventObservable) || eventData == null) {
			return;
		}
		EventObservable event = (EventObservable) o;
		if (event.getType() == ObservationEventType.RECORD_ADD_EVENT) {
			Record record = (Record) eventData;
			Entity entity = record.getEntity();
            if (Context.getBlockingService(entity.getName()).getBlockingServiceId() != getBlockingServiceId()) {
                return;
            }
			log.debug("A new record was added; we need to update the index: " + record.getRecordId());
			BlockingServiceCache blockingServiceCache = cacheByEntity.get(entity.getName());
			blockingServiceCache.addRecordToIndex(record);
		} else if (event.getType() == ObservationEventType.RECORDS_ADD_EVENT) {
            Set<Record> records = (Set<Record>) eventData;
            if (records == null || records.size() == 0) {
                return;
            }
            Record record = records.iterator().next();
            Entity entity = record.getEntity();
            if (Context.getBlockingService(entity.getName()).getBlockingServiceId() != getBlockingServiceId()) {
                return;
            }
            log.debug("A new set of records was added; we need to update the index: " + record.getRecordId());
            BlockingServiceCache blockingServiceCache = cacheByEntity.get(entity.getName());
            blockingServiceCache.addRecordsToIndex(records);
		} else	if (event.getType() == ObservationEventType.RECORD_DELETE_EVENT) {
			Record record = (Record) eventData;
            Entity entity = record.getEntity();
            if (Context.getBlockingService(entity.getName()).getBlockingServiceId() != getBlockingServiceId()) {
                return;
            }
			log.debug("A new record was deleted; we need to update the index: " + record.getRecordId());
            BlockingServiceCache blockingServiceCache = cacheByEntity.get(entity.getName());
            blockingServiceCache.deleteRecordFromIndex(record);
		} else	if (event.getType() == ObservationEventType.RECORD_UPDATE_EVENT) {
			Object[] records = (Object[]) eventData;
			Record beforeRecord = (Record) records[0];
			Record afterRecord = (Record) records[1];
            Entity entity = beforeRecord.getEntity();
            if (Context.getBlockingService(entity.getName()).getBlockingServiceId() != getBlockingServiceId()) {
                return;
            }
			log.debug("A new record was updated; we need to update the index: " + afterRecord.getRecordId());
            BlockingServiceCache blockingServiceCache = cacheByEntity.get(entity.getName());
            blockingServiceCache.updateRecordInIndex(beforeRecord, afterRecord);
		}
	}

	public RecordPairSource getRecordPairSource(Entity entity, Object parameters) {
		if (!(parameters instanceof List<?>)) {
			log.trace("Invalid parameter for this implementation of the blocking interface: " + parameters.getClass());
			throw new RuntimeException("Invalid parameter type provided for this blocking algorithm implementation.");
		}
		@SuppressWarnings("unchecked")
		List<BlockingRound> blockingRounds = (List<BlockingRound>) parameters;
		BasicRecordPairSource recordPairSource = new BasicRecordPairSource(entity);
		recordPairSource.setBlockingDao(blockingDao);
		recordPairSource.setCache(cacheByEntity.get(entity.getName()));
		recordPairSource.setBlockingRounds(blockingRounds);
		recordPairSource.init();
		return recordPairSource;
	}


	public RecordPairSource getRecordPairSource(Entity entity) {
		return getRecordPairSource(entity, loadBlockingConfiguration(entity.getName(), false).getBlockingRounds());
	}

	public List<BlockingRound> getBlockingRounds(String entityName) {
	    BlockingConfiguration config = loadBlockingConfiguration(entityName, false);
	    if (config == null) {
	        log.warn("No blocking rounds configuration has been registered for entity: " + entityName);
	        return null;
	    }
	    return config.getBlockingRounds();
	}

	/**
	 * Iterates over the list of blocking rounds that have been
	 * defined and accumulates patients that match the search criteria
	 * configured for the specific values present in the record
	 * provided.
	 *
	 */
	public List<RecordPair> findCandidates(Record record) {
	    BlockingConfiguration config = loadBlockingConfiguration(record.getEntity().getName(), false);
	    List<BlockingRound> blockingRounds = config.getBlockingRounds();
		Entity entity = record.getEntity();
		BlockingServiceCache blockingServiceCache = cacheByEntity.get(entity.getName());
		List<Record> records = new java.util.ArrayList<Record>();
		for (BlockingRound round : blockingRounds) {
			List<BaseField> fields = round.getFields();
			String blockingKeyValue = BlockingKeyValueGenerator.generateBlockingKeyValue(fields, record);
			log.debug("Blocking using key: " + blockingKeyValue);
			records.addAll(blockingServiceCache.loadCandidateRecords(round, blockingKeyValue));
		}
		log.debug("Found a total of " + records.size() + " candidate records.");
		return ConvertUtil.generateRecordPairs(record, records);
	}

	@Override
	public List<Long> getRecordPairCount(Entity entity) {
	    BlockingConfiguration config = loadBlockingConfiguration(entity.getName(), false);
		List<BlockingRound> blockingRounds = config.getBlockingRounds();
		List<Long> counts = new ArrayList<Long>(blockingRounds.size());
		for (BlockingRound round : blockingRounds) {
			counts.add(blockingDao.getRecordPairCount(entity, round));
		}
		return counts;
	}

    @SuppressWarnings("unchecked")
	private BlockingConfiguration loadBlockingConfiguration(String entityName, boolean isInit) {
        BlockingConfiguration config = configByEntity.get(entityName);
        Map<String,Object> configurationData = (Map<String,Object>) Context.getConfiguration()
                .lookupConfigurationEntry(entityName, ConfigurationRegistry.BLOCKING_CONFIGURATION);
        Date lastUpdateDate = (Date) configurationData.get(BasicBlockingConstants.LAST_UPDATE_DATE);
        if (config == null || lastUpdateDate == null ||
                config.getLastUpdateDate().getTime() < lastUpdateDate.getTime()) {
    	    
    	    config = new BlockingConfiguration(new Date());
            List<BlockingRound> blockingRounds = (List<BlockingRound>)
                    configurationData.get(BasicBlockingConstants.BLOCKING_ROUNDS_REGISTRY_KEY);
            Integer maximumBlockSize = (Integer)
                    configurationData.get(BasicBlockingConstants.MAXIMUM_BLOCK_SIZE);
            config.setBlockingRounds(blockingRounds);
            config.setMaximumBlockSize(maximumBlockSize);
            if (!isInit) {
                BlockingServiceCache blockingServiceCache = cacheByEntity.get(entityName);
                blockingServiceCache.init(config.getBlockingRounds(), false);
            }
            log.info("Initialized cache for entity " + entityName);
            configByEntity.put(entityName, config);
        }
        return config;
	}

    public int getBlockingServiceId() {
        return TRADITIONAL_BLOCKING_ALGORITHM_ID;
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
    
    private class BlockingConfiguration
    {
        private List<BlockingRound> blockingRounds;
        private Integer maximumBlockSize;
        private Date lastUpdateDate;

        public BlockingConfiguration(Date now) {
            lastUpdateDate = now;
        }
        
        List<BlockingRound> getBlockingRounds() {
            return blockingRounds;
        }

        public void setBlockingRounds(List<BlockingRound> blockingRounds) {
            this.blockingRounds = blockingRounds;
            int i=0;
            for (BlockingRound blockingRound : blockingRounds) {
                blockingRound.setName("round." + i);
                i++;
            }
        }

        public void setMaximumBlockSize(Integer maximumBlockSize) {
            this.maximumBlockSize = maximumBlockSize;
        }

        Integer getMaximumBlockSize() {
            return maximumBlockSize;
        }

        public Date getLastUpdateDate() {
            return lastUpdateDate;
        }
    }
}
