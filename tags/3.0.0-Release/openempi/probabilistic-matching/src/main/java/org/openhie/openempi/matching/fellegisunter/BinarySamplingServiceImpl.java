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
package org.openhie.openempi.matching.fellegisunter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.openhie.openempi.blocking.RecordPairIterator;
import org.openhie.openempi.blocking.RecordPairSource;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.matching.SamplingService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.RecordPair;
import org.openhie.openempi.service.impl.BaseServiceImpl;

public class BinarySamplingServiceImpl extends BaseServiceImpl implements SamplingService
{
    private double selectionProbability;
    private Random random;
    
    public BinarySamplingServiceImpl() {
        random = new Random(new Date().getTime());
    }
    
    @Override
    public List<RecordPair> getRecordPairs(Entity entity) {
        RecordPairSource source = Context.getBlockingService(entity.getName()).getRecordPairSource(entity);
        List<RecordPair> pairs = new ArrayList<RecordPair>();
        int pairCount = 0, totalCount = 0;
        for (RecordPairIterator iter = source.iterator(); iter.hasNext();) {
            double randomValue = random.nextDouble();
            totalCount++;
            if (randomValue < selectionProbability) {
                pairs.add(iter.next());
                pairCount++;
                if (pairCount % 10000 == 0) {
                    log.info("Binary Sampling Service Loaded " + pairCount + " pairs.");
                }
            }
        }
        log.info("The sampling algorithm selected " + pairCount + " out of a total of " + totalCount + " pairs.");
        return pairs;
    }

    public double getSelectionProbability() {
        return selectionProbability;
    }

    public void setSelectionProbability(double selectionProbability) {
        this.selectionProbability = selectionProbability;
    }
}
