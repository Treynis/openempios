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
package org.openhie.openempi.blocking.basicblockinghp.dao.orientdb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.openhie.openempi.blocking.basicblockinghp.BlockingKeyValueGenerator;
import org.openhie.openempi.blocking.basicblockinghp.dao.BlockingDao;
import org.openhie.openempi.configuration.BaseField;
import org.openhie.openempi.configuration.BlockingRound;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.entity.dao.orientdb.OrientdbConverter;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.NameValuePair;
import org.openhie.openempi.model.Record;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class BlockingDaoOrientdb implements BlockingDao
{
	private Logger log = Logger.getLogger(getClass());
	private static final int DEFAULT_BLOCK_SIZE = 10000;
	private Set<Entity> initializedEntities = new HashSet<Entity>();
	private EntityDao entityDao;
	
	public Long getRecordPairCount(Entity entity, BlockingRound round) {
		StringBuffer sb = new StringBuffer();
		List<BaseField> fields = round.getFields();
		for (int i=0; i < fields.size(); i++) {
			sb.append(fields.get(i).getFieldName());
			if (i < fields.size()-1) {
				sb.append(",");
			}
		}
		final String query = "select count(*) as c, " + sb.toString() + " from " + entity.getName() + 
				" where dateVoided is null group by " + sb.toString();
		log.debug("Counting block sizes for round " + round.getName() + " using query:\n" + query);
		
		List<Long> valueCounts = new ArrayList<Long>();
		List<ODocument> results = getEntityDao(entity).executeQuery(entity, query);
		if (results == null || results.size() == 0) {
			return 0L;
		}
		for (ODocument odoc : results) {
			if (odoc.field("c") != null && ((Long) odoc.field("c")) > 0) {
				valueCounts.add((Long) odoc.field("c"));
			}
		}
		long totalRecordPairCount=0;
		for (Long valueCount : valueCounts) {
			totalRecordPairCount += calculateRecordPairsByBlockSize(valueCount.longValue());
		}
		return totalRecordPairCount;
	}
	
	public long calculateRecordPairsByBlockSize(long valueCount) {
		double dValueCount = (double) valueCount;
		return (long) (dValueCount * (dValueCount-1.0) / 2.0);
	}	

	public List<Long> getAllRecordIds(Entity entity) {
		final String query = "select @rid as rid from " + entity.getName() + " where dateVoided is null";
		log.debug("Retrieving all record ids using query: " + query);
		List<ODocument> list = getEntityDao(entity).executeQuery(entity, query);
		List<Long> recordIds = new java.util.ArrayList<Long>();
		if (list == null || list.size() == 0) {
			return recordIds;
		}
		for (ODocument odoc : list) {
			ODocument obj = (ODocument) odoc.field("rid");
			ORID orid = obj.getIdentity();
			Long recordId = OrientdbConverter.getRecordIdFromOrid(orid);
			if (recordId != null) {
				recordIds.add(recordId);
			}
		}
		return recordIds;
	}
	
	public List<NameValuePair> getDistinctKeyValuePairs(Entity entity, List<String> fields) {
		StringBuffer sb = new StringBuffer("select @rid, ");
		for (int i=0; i < fields.size(); i++) {
			sb.append(fields.get(i));
			if (i < fields.size()-1) {
				sb.append(",");
			}
		}
		sb.append(" from ").append(entity.getName()).append(" where dateVoided is null");
		for (int i=0; i < fields.size(); i++) {
			sb.append(" and ").append(fields.get(i)).append(" is not null");
		}
		String query = sb.toString();
		log.debug("Retrieving all distinct key value pairs using query: " + query);
		List<ODocument> list = getEntityDao(entity).executeQuery(entity, query);
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		if (list == null || list.size() == 0) {
			return pairs;
		}

		for (ODocument odoc : list) {
			ODocument obj = (ODocument) odoc.field("rid");
			ORID orid = obj.getIdentity();
			Long recordId = OrientdbConverter.getRecordIdFromOrid(orid);
			Object[] values = new Object[fields.size()];
			for (int i=0; i < fields.size(); i++) {
				values[i] = odoc.field(fields.get(i));
			}
			String blockingKeyValue = BlockingKeyValueGenerator.generateBlockingKeyValue(values);
			NameValuePair pair = new NameValuePair(blockingKeyValue, recordId);
			pairs.add(pair);
		}
		return pairs;
	}

	public Record loadRecord(Entity entity, Cache recordCache, Long recordId) {
	    Record record = getEntityDao(entity).loadRecord(entity, recordId);
	    if (record != null) {
            Element element = new Element(record.getRecordId(), record);
            recordCache.put(element);
	    } else {
	        if (log.isDebugEnabled()) {
	            log.debug("Record with id: " + recordId + " was not found in the database.");
	        }
	    }
	    return record;
	}
	
	public void loadAllRecords(Entity entity, Cache recordCache, List<String> fields) {
		StringBuffer sb = new StringBuffer("select @rid as rid, ");
		for (int i=0; i < fields.size(); i++) {
			sb.append(fields.get(i));
			if (i < fields.size()-1) {
				sb.append(",");
			}
		}
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
	            ODocument obj = odoc.field("rid");
	            ORID orid = obj.getIdentity();
	            Record record = OrientdbConverter.convertODocumentToRecord(getEntityDao(entity).getEntityCacheManager(), entity, odoc);
	            record.setRecordId(orid.getClusterPosition().longValueHigh());
	            Element element = new Element(record.getRecordId(), record);
	            recordCache.put(element);
	            count++;
	        }
	        firstResult += list.size();
	        log.info("Loaded a block of " + list.size() + " records into the cache.");
		}
		log.info("Loaded " + count + " records into the cache.");
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
