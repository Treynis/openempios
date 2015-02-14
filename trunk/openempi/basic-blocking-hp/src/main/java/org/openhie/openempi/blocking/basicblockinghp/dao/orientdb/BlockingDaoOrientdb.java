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
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openhie.openempi.blocking.basicblockinghp.dao.BlockingDao;
import org.openhie.openempi.blocking.basicblockinghp.dao.BlockingRoundClass;
import org.openhie.openempi.configuration.BaseField;
import org.openhie.openempi.configuration.BlockingRound;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.entity.dao.orientdb.OrientdbConverter;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class BlockingDaoOrientdb implements BlockingDao
{
    public final static String BLOCKINGKEYVALUE_FIELD = "blockingKeyValue";
    public final static String RECORDIDS_FIELD = "rids";
	private Logger log = Logger.getLogger(getClass());
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
		List<Map<String,Object>> results = getEntityDao(entity).executeQuery(entity, query);
		if (results == null || results.size() == 0) {
			return 0L;
		}
		for (Map<String,Object> odoc : results) {
			if (odoc.get("c") != null && ((Long) odoc.get("c")) > 0) {
				valueCounts.add((Long) odoc.get("c"));
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
		return entityDao.getAllRecordIds(entity);
	}

	/*
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
	*/
	
	public Long loadBlockDataCount(Entity entity, BlockingRoundClass roundClass) {
	    StringBuilder sb = new StringBuilder("select count(*) from ");
	    sb.append(roundClass.getRoundClass().getName());
	    log.debug("Retrieving the count of blocking data using: " + sb.toString());
	    List<Map<String,Object>> countDoc = getEntityDao(entity).executeQuery(entity, sb.toString());
	    Long count = (Long) countDoc.get(0).get("count");
	    return count;
	}
	
	@SuppressWarnings("unchecked")
    public Set<Long> loadBlockData(Entity entity, String blockRecordId) {
	    Map<String,Object> odoc = getEntityDao(entity).loadObject(entity, blockRecordId);
        Set<Long> rids = (Set<Long>) odoc.get(RECORDIDS_FIELD);
        return rids;
	}

    @SuppressWarnings("unchecked")
    public Record loadBlockData(Entity entity, BlockingRoundClass roundClass, String blockingKeyValue) {
        StringBuilder sb = new StringBuilder("select ");
        sb.append(" from ")
            .append(roundClass.getRoundClass().getName())
            .append(" where ")
            .append(BLOCKINGKEYVALUE_FIELD)
            .append(" = '")
            .append(blockingKeyValue)
            .append("'");
        String query = sb.toString();
        log.debug("Retrieving block data using: " + query);
        List<Map<String,Object>> list = getEntityDao(entity).executeQuery(entity, query);
        Record record=null;
        if (list != null && list.size() > 0) {
            // There should only be one of these
            for (Map<String,Object> odoc : list) {
                String key = (String) odoc.get(BLOCKINGKEYVALUE_FIELD);
                Set<Long> rids = (Set<Long>) odoc.get(RECORDIDS_FIELD);
                record = new Record(roundClass.getRoundClass());
                OrientdbConverter.extractIdAndCluster(odoc, record);
                record.set(BLOCKINGKEYVALUE_FIELD, key);
                record.set(RECORDIDS_FIELD, rids);
                break;
            }
        }
        return record;
    }
    
    public Set<String> loadBlockRecordIds(Entity entity, BlockingRoundClass roundClass) {
        StringBuilder sb = new StringBuilder("select @rid from ");
        sb.append(roundClass.getRoundClass().getName())
            .append(" where ")
            .append(RECORDIDS_FIELD)
            .append(".size() > 1");
        String query = sb.toString();
        log.debug("Retrieving block data using: " + query);
        List<Map<String,Object>> list = getEntityDao(entity).executeQuery(entity, query);
        Set<String> rids = new HashSet<String>();
        if (list != null && list.size() > 0) {
            // There should only be one of these
            for (Map<String,Object> record : list) {
//                ODocument doc = odoc.field("rid");
                ODocument doc = (ODocument) record.get("rid");
                String key = doc.getIdentity().toString();
                rids.add(key);
            }
        }
        return rids;
    }

    public void saveBlockData(Entity entity, BlockingRoundClass roundClass, Record record) {
        getEntityDao(entity).saveData(entity, roundClass.getRoundClass().getName(), record);
    }
    
	private synchronized EntityDao getEntityDao(Entity entity) {
		boolean found = initializedEntities.contains(entity);
		if (!found) {
			entityDao.initializeStore(entity);
			initializedEntities.add(entity);
		}
		return entityDao;
	}
    
	public EntityDao getEntityDao() {
		return entityDao;
	}

	public void setEntityDao(EntityDao entityDao) {
		this.entityDao = entityDao;
	}
}
