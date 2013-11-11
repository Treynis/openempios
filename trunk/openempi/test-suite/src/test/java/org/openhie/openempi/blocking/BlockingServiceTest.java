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
package org.openhie.openempi.blocking;
import java.util.List;

import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.RecordPair;
import org.openhie.openempi.service.BaseServiceTestCase;


public class BlockingServiceTest extends BaseServiceTestCase
{
	public void testGetRecordPairs() {
		try {
			List<Entity> entities = Context.getEntityDefinitionManagerService().loadEntities();
			assertTrue("No entities have been defined.", entities.size() > 0);
			Entity testEntity = entities.get(0);
			
			BlockingService blockingService = Context.getBlockingService();
			RecordPairSource recordPairSource = blockingService.getRecordPairSource(testEntity);
			int i=0;
			for (RecordPairIterator iter = recordPairSource.iterator(); iter.hasNext(); ) {
				RecordPair pair = iter.next();
				if (log.isTraceEnabled()) {
					log.trace("Comparing records " + pair.getLeftRecord().getRecordId() + " and " + pair.getRightRecord().getRecordId());
				}
				i++;
			}
			System.out.println("Loaded " + i + " record pairs.");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}
	}	

	/*
	public void testGetRecordPairsWithConfiguration() {
		try {
			List<Entity> entities = Context.getEntityDefinitionManagerService().loadEntities();
			assertTrue("No entities have been defined.", entities.size() > 0);
			Entity testEntity = entities.get(0);
			BlockingService blockingService = Context.getBlockingService();
			BlockingRound round = new BlockingRound();
			round.addField(new BaseField("firstName"));
			round.addField(new BaseField("lastName"));
			round.setName("round.0");
			List<BlockingRound> rounds = new ArrayList<BlockingRound>(1);
			rounds.add(round);
			RecordPairSource recordPairSource = blockingService.getRecordPairSource(testEntity, rounds);
			int i=0;
			for (RecordPairIterator iter = recordPairSource.iterator(); iter.hasNext(); ) {
				RecordPair pair = iter.next();
				log.info("Comparing records " + pair.getLeftRecord().getRecordId() + " and " + pair.getRightRecord().getRecordId());
				i++;
			}
			System.out.println("Loaded " + i + " record pairs.");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}
	}*/
}
