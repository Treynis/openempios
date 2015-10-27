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
package org.openhie.openempi.blocking.naive;

import java.util.ArrayList;
import java.util.List;

import org.openhie.openempi.InitializationException;
import org.openhie.openempi.blocking.AbstractBlockingLifecycleObserver;
import org.openhie.openempi.blocking.BlockingService;
import org.openhie.openempi.blocking.RecordPairSource;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordPair;

public class NaiveBlockingServiceImpl extends AbstractBlockingLifecycleObserver implements BlockingService
{
    private final static int NAIVE_BLOCKING_ALGORITHM_ID = 0;
	private EntityDao entityDao;

	public void startup() throws InitializationException {
		log.info("Starting the Naive Blocking Service");
	}

	public void shutdown() {
		log.info("Stopping the Naive Blocking Service");
	}

	public void rebuildIndex() throws InitializationException {
		log.debug("Rebuilding the index.");	
	}
	
	public RecordPairSource getRecordPairSource(Entity entity) {
		RecordPairSource source = new NaiveBlockingRecordPairSource(entity, entityDao);
		source.init();
		return source;
	}

	public RecordPairSource getRecordPairSource(Entity entity, Object parameters) {
		return getRecordPairSource(entity);
	}

	// The naive blocking service pairs up the record selected with every other record in the system
	public List<RecordPair> findCandidates(Record record) {
		List<RecordPair> pairs = new ArrayList<RecordPair>();
		if (record == null) {
			return pairs;
		}
		List<Record> firstRecords = loadFirstRecords(record);
		if (firstRecords == null || firstRecords.size() == 0) {
			return pairs;
		}
		List<Long> recordIds = entityDao.getAllRecordIds(record.getEntity());
		for (Record firstRecord : firstRecords) {
			for (Long id : recordIds) {
				if (id == firstRecord.getRecordId()) {
					continue;
				}
				Record secondRecord = entityDao.loadRecord(record.getEntity(), id);
				if (secondRecord == null) {
					continue;
				}
				RecordPair pair = new RecordPair(firstRecord, secondRecord);
				pairs.add(pair);
			}
		}
		return pairs;
	}

	private List<Record> loadFirstRecords(Record record) {
		List<Record> records = entityDao.findRecordsByAttributes(record.getEntity(), record);
		return records;
	}

	public List<Long> getRecordPairCount(Entity entity) {
		double recordCount = (double) entityDao.getRecordCount(entity);
		double recordPairCount = recordCount * (recordCount-1.0) / 2.0;
		List<Long> counts = new ArrayList<Long>(1);
		counts.add((long) recordPairCount);
		return counts;
	}
	
    public int getBlockingServiceId() {
        return NAIVE_BLOCKING_ALGORITHM_ID;
    }

    public 	EntityDao getEntityDao() {
		return entityDao;
	}

	public void setEntityDao(EntityDao entityDao) {
		this.entityDao = entityDao;
	}
}
