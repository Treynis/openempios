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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhie.openempi.blocking.RecordPairIterator;
import org.openhie.openempi.configuration.BlockingRound;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordPair;

public class BasicRecordPairIterator implements RecordPairIterator
{
	protected final Log log = LogFactory.getLog(getClass());

	private BasicRecordPairSource recordPairSource;
	private boolean initialized = false;
	private List<String> blockRecordIds;
	private List<RecordPair> recordPairs;
	private List<BlockingRound> blockingRounds;

	private int currentRecordId;
	private int currentRecordPair;
	private RecordPair nextPair;

	public BasicRecordPairIterator(BasicRecordPairSource recordPairSource) {
		this.recordPairSource = recordPairSource;
	}
	
	private synchronized void initialize() {
		blockingRounds = recordPairSource.getBlockingRounds();
		// If there are not records then there is nothing to do
		if (blockRecordIds == null || blockRecordIds.size() == 0) {
	        recordPairs = new ArrayList<RecordPair>();
			return;
		}
		currentRecordId = 0;
		recordPairs = new ArrayList<RecordPair>();
        loadRecordPairsForBlock();
		initialized = true;
	}

	public boolean hasNext() {
		RecordPair pair = null;
		if (!isInitialized()) {
			initialize();
		}
		if (currentRecordPair < recordPairs.size()) {
			pair = recordPairs.get(currentRecordPair);
			currentRecordPair++;
			nextPair = pair;			
			return true;
		}
		if (currentRecordId < blockRecordIds.size()) {
	        recordPairs.clear();
	        loadRecordPairsForBlock();
		}
		if (currentRecordId < blockRecordIds.size() && recordPairs.size() > 0) {
		    return true;
		}
		return false;
	}

    public RecordPair next() {
        return nextPair;
    }

    private void loadRecordPairsForBlock() {
        Set<RecordPair> allPairs = new HashSet<RecordPair>();
        boolean foundRecordPairs = false;
        do {
            foundRecordPairs = loadRecordPairsForBlock(allPairs, currentRecordId);
            currentRecordId++;
        } while (!foundRecordPairs && currentRecordId < blockRecordIds.size());
        recordPairs = new ArrayList<RecordPair>(allPairs.size());
        recordPairs.addAll(allPairs);
        currentRecordPair = 0;
    }
    
	private boolean loadRecordPairsForBlock(Set<RecordPair> allPairs, int blockIndex) {
	    Set<Long> recordIds = recordPairSource.getBlockingDao().loadBlockData(recordPairSource.getEntity(), blockRecordIds.get(blockIndex));
	    List<Record> records = recordPairSource.getCache().loadCandidateRecords(recordIds);
	    List<RecordPair> pairs = generateRecordPairs(records);
	    if (pairs.size() > 0) {
	        allPairs.addAll(pairs);
	    }
		return true;
	}

	private List<RecordPair> generateRecordPairs(List<Record> records) {
		List<RecordPair> pairs = new ArrayList<RecordPair>();
		for (int i=0; i < records.size()-1; i++) {
			for (int j=i+1; j < records.size(); j++) {
				RecordPair recordPair = new RecordPair(records.get(i), records.get(j));
				if (records.get(i) == null) {
				    log.warn("Record pair with null records: " + records.get(i) + records.get(j));
				}
				pairs.add(recordPair);
			}
		}
		return pairs;
	}
	
	public void remove() {
		// This is an optional method of the interface and doesn't do
		// anything in this implementation. This is a read-only iterator.
	}
	
	public List<String> getBlockRecordIds() {
        return blockRecordIds;
    }

    public void setBlockRecordIds(List<String> blockRecordIds) {
        this.blockRecordIds = blockRecordIds;
    }

    public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
	public List<BlockingRound> getBlockingRounds() {
		return blockingRounds;
	}

	public void setBlockingRounds(List<BlockingRound> blockingRounds) {
		this.blockingRounds = blockingRounds;
	}	
}
