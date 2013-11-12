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

import java.util.List;

import org.apache.log4j.Logger;
import org.openhie.openempi.blocking.RecordPairIterator;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordPair;

public class NaiveBlockingRecordPairIterator implements RecordPairIterator
{
	private Logger log = Logger.getLogger(getClass());
	
	private Entity entity;
	private EntityDao entityDao;
	private List<Long> recordIds;
	private Record firstRecord;
	private int indexFirst;
	private int indexSecond;
	
	public NaiveBlockingRecordPairIterator(Entity entity, EntityDao entityDao, List<Long> recordIds) {
		this.entity = entity;
		this.entityDao = entityDao;
		this.recordIds = recordIds;
		indexFirst = 0;
		indexSecond = 1;
		Long id = recordIds.get(indexFirst);
		firstRecord = entityDao.loadRecord(entity, id);
		if (firstRecord == null) {
			log.warn("Unable to locate entity in record pair iterator with identifier: " + id);
			throw new RuntimeException("Unable to load the referenced record from the repository.");
		}
	}
	
	public boolean hasNext() {
		return indexFirst < (recordIds.size()-2);
	}

	public RecordPair next() {
		Long id = recordIds.get(indexSecond);
		Record secondRecord = entityDao.loadRecord(entity, id);
		RecordPair pair = new RecordPair(firstRecord, secondRecord);
		// Prepare the pointers for the next iteration
		indexSecond++;
		if (indexSecond == recordIds.size()-1) {
			indexFirst++;
			id = recordIds.get(indexFirst);
			firstRecord = entityDao.loadRecord(entity, id);
			indexSecond = indexFirst + 1;
		}
		return pair;
	}

	public void remove() {
		// This is an optional method of the interface and doesn't do
		// anything in this implementation. This is a read-only iterator.
	}
}
