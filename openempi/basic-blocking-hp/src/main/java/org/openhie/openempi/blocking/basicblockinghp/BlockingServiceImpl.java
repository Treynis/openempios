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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import net.sf.ehcache.CacheManager;

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
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordPair;
import org.openhie.openempi.notification.EventObservable;
import org.openhie.openempi.notification.ObservationEventType;
import org.openhie.openempi.util.ConvertUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class BlockingServiceImpl extends AbstractBlockingLifecycleObserver implements BlockingService, Observer
{
	private BlockingDao blockingDao;
	private List<BlockingRound> blockingRounds;
	private Integer maximumBlockSize;
	private Map<String,BlockingServiceCache> cacheByEntity = new HashMap<String,BlockingServiceCache>();
	private List<Entity> entities;
	private CacheManager cacheManager;
	private ThreadPoolTaskExecutor cacheTaskExecutor;
	
	public void startup() throws InitializationException {
		log.info("Starting the Traditional Blocking Service");
		loadBlockingConfiguration();
		entities = Context.getEntityDefinitionManagerService().loadEntities();
		for (Entity entity : entities) {
			BlockingServiceCache blockingServiceCache = new BlockingServiceCache(entity);
			blockingServiceCache.setCacheManager(cacheManager);
			blockingServiceCache.setBlockingDao(blockingDao);
			blockingServiceCache.setCacheTaskExecutor(cacheTaskExecutor);
			blockingServiceCache.setMaximumBlockSize(maximumBlockSize);
			blockingServiceCache.init(blockingRounds);
			log.info("Initialized cache for entity " + entity.getName());
			cacheByEntity.put(entity.getName(), blockingServiceCache);
		}
		Context.registerObserver(this, ObservationEventType.ENTITY_ADD_EVENT);
		Context.registerObserver(this, ObservationEventType.ENTITY_DELETE_EVENT);
		Context.registerObserver(this, ObservationEventType.ENTITY_UPDATE_EVENT);
		log.info("Finished building the index for the Traditional Blocking Service");
	}

	public void shutdown() {
		blockingRounds = getBlockingRounds();
        for (Entity entity : entities) {
            BlockingServiceCache blockingServiceCache = cacheByEntity.get(entity.getName());
            blockingServiceCache.removeCaches(blockingRounds);
            blockingServiceCache.shutdown();
        }
		log.info("Stopping the Traditional Blocking Service");
	}

	public void rebuildIndex() throws InitializationException {
		log.info("Rebuilding the indices for the Traditional Blocking Service");
		blockingRounds = getBlockingRounds();
		List<Entity> entities = Context.getEntityDefinitionManagerService().loadEntities();
		for (Entity entity : entities) {
			BlockingServiceCache blockingServiceCache = cacheByEntity.get(entity.getName());
			blockingServiceCache.removeCaches(blockingRounds);
			blockingServiceCache.init(blockingRounds);
		}
		log.info("Finished building the indices for the Traditional Blocking Service");
	}
	
	public void update(Observable o, Object eventData) {
		if (!(o instanceof EventObservable) || eventData == null || !(eventData instanceof Record)) {
			return;
		}
		EventObservable event = (EventObservable) o;
		if (event.getType() == ObservationEventType.ENTITY_ADD_EVENT) {
			Record record = (Record) eventData;
			log.debug("A new record was added; we need to update the index: " + record.getRecordId());
			blockingRounds = getBlockingRounds();
			for (BlockingRound round : blockingRounds) {
				List<BaseField> fields = round.getFields();
				String blockingKeyValue = BlockingKeyValueGenerator.generateBlockingKeyValue(fields, record);
				if (log.isDebugEnabled()) {
					log.debug("Adding to cache: (" + blockingKeyValue + "," + record.getRecordId().longValue() + ")");
				}
				
				for (BlockingServiceCache blockingServiceCache : cacheByEntity.values()) {
					blockingServiceCache.addRecordToIndex(round, blockingKeyValue, record.getRecordId().longValue());
				}
			}
			for (BlockingServiceCache blockingServiceCache : cacheByEntity.values()) {
				blockingServiceCache.updateRecordCache(event, record);
			}
		} else	if (event.getType() == ObservationEventType.ENTITY_DELETE_EVENT) {
			Record record = (Record) eventData;
			log.debug("A new record was deleted; we need to update the index: " + record.getRecordId());
			blockingRounds = getBlockingRounds();
			for (BlockingRound round : blockingRounds) {
				List<BaseField> fields = round.getFields();
				String blockingKeyValue = BlockingKeyValueGenerator.generateBlockingKeyValue(fields, record);
				if (log.isDebugEnabled()) {
					log.debug("Removing from cache: (" + blockingKeyValue + "," + record.getRecordId().longValue() + ")");
				}
				for (BlockingServiceCache blockingServiceCache : cacheByEntity.values()) {
					blockingServiceCache.deleteRecordFromIndex(round, blockingKeyValue, record.getRecordId().longValue());
				}
			}
			for (BlockingServiceCache blockingServiceCache : cacheByEntity.values()) {
				blockingServiceCache.updateRecordCache(event, record);
			}
		} else	if (event.getType() == ObservationEventType.ENTITY_UPDATE_EVENT) {
			Object[] records = (Object[]) eventData;
			Record beforeRecord = (Record) records[0];
			Record afterRecord = (Record) records[1];
			log.debug("A new record was updated; we need to update the index: " + afterRecord.getRecordId());
			blockingRounds = getBlockingRounds();
			for (BlockingRound round : blockingRounds) {
				List<BaseField> fields = round.getFields();
				String blockingKeyValue = BlockingKeyValueGenerator.generateBlockingKeyValue(fields, beforeRecord);
				if (log.isDebugEnabled()) {
					log.debug("Removing from cache: (" + blockingKeyValue + "," + beforeRecord.getRecordId().longValue() + ")");
				}
				for (BlockingServiceCache blockingServiceCache : cacheByEntity.values()) {
					blockingServiceCache.deleteRecordFromIndex(round, blockingKeyValue, beforeRecord.getRecordId().longValue());
					blockingKeyValue = BlockingKeyValueGenerator.generateBlockingKeyValue(fields, afterRecord);
					if (log.isDebugEnabled()) {
						log.debug("Adding to cache: (" + blockingKeyValue + "," + afterRecord.getRecordId().longValue() + ")");
					}
					blockingServiceCache.addRecordToIndex(round, blockingKeyValue, afterRecord.getRecordId().longValue());
				}
			}
			for (BlockingServiceCache blockingServiceCache : cacheByEntity.values()) {
				blockingServiceCache.updateRecordCache(event, afterRecord);
			}
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
		return getRecordPairSource(entity, getBlockingRounds());
	}
	
	/**
	 * Iterates over the list of blocking rounds that have been
	 * defined and accumulates patients that match the search criteria
	 * configured for the specific values present in the record
	 * provided.
	 * 
	 */
	public List<RecordPair> findCandidates(Record record) {
		blockingRounds = getBlockingRounds();
		Entity entity = record.getEntity();
		BlockingServiceCache blockingServiceCache = cacheByEntity.get(entity.getName());
		List<Record> records = new java.util.ArrayList<Record>();
		for (BlockingRound round : blockingRounds) {
			List<BaseField> fields = round.getFields();
			String blockingKeyValue = BlockingKeyValueGenerator.generateBlockingKeyValue(fields, record);
			log.debug("Blocking using key: " + blockingKeyValue);
			records.addAll(blockingServiceCache.loadCandidateRecords(blockingKeyValue));
		}
		log.debug("Found a total of " + records.size() + " candidate records.");
		return ConvertUtil.generateRecordPairs(record, records);
	}

	@Override
	public List<Long> getRecordPairCount(Entity entity) {
		blockingRounds = getBlockingRounds();
		List<Long> counts = new ArrayList<Long>(blockingRounds.size());
		for (BlockingRound round : blockingRounds) {
			counts.add(blockingDao.getRecordPairCount(entity, round));
		}
		return counts;
	}

	List<BlockingRound> getBlockingRounds() {
		if (blockingRounds == null) {
		    loadBlockingConfiguration();
		}
		return blockingRounds;
	}

	Integer getMaximumBlockSize() {
	    if (maximumBlockSize == null) {
	        loadBlockingConfiguration();
	    }
	    return maximumBlockSize;
	}
	
    @SuppressWarnings("unchecked")
	private void loadBlockingConfiguration() {
	    Map<String,Object> configurationData = (Map<String,Object>) Context.getConfiguration()
	            .lookupConfigurationEntry(ConfigurationRegistry.BLOCKING_CONFIGURATION);
        blockingRounds = (List<BlockingRound>) configurationData.get(BasicBlockingConstants.BLOCKING_ROUNDS_REGISTRY_KEY);
        maximumBlockSize = (Integer) configurationData.get(BasicBlockingConstants.MAXIMUM_BLOCK_SIZE);
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
