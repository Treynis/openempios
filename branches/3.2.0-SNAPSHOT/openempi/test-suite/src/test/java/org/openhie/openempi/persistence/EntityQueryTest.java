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
package org.openhie.openempi.persistence;

import java.util.List;

import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.service.BaseServiceTestCase;

public class EntityQueryTest extends BaseServiceTestCase
{
	public void testFindByAttributes() {
		try {
			RecordQueryService manager = Context.getRecordQueryService();
			
			EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
			List<Entity> entities = defService.loadEntities();
			if (entities.size() == 0) {
				log.debug("You need to define some entities in the database for this to work.");
				return;
			}
			Entity entity = entities.get(0);
			log.debug("Testing with entity " + entity);
			Record record = new Record(entity);
			record.set("firstName", "John12");
			record.set("dateOfBirth", "2013-06%");
			long startTime = new java.util.Date().getTime();
			List<Record> records = manager.findRecordsByAttributes(entity, record);
			long endTime = new java.util.Date().getTime();
			log.debug("Using exact query located " + records.size() + " records in " + (endTime-startTime)/1000 + " secs.");
			for (Record rec : records) {
				log.debug("Record is " + rec.asString());
			}

			record = new Record(entity);
			record.set("firstName", "John15");
			record.set("dateOfBirth", "2013-06-15");
			startTime = new java.util.Date().getTime();
			records = manager.findRecordsByAttributes(entity, record);
			endTime = new java.util.Date().getTime();
			log.debug("Using fuzzy query located " + records.size() + " records in " + (endTime-startTime)/1000 + " secs.");
			for (Record rec : records) {
//				log.debug("Record is " + rec.asString());
			}
			
			record = new Record(entity);
			record.set("firstName", "John123%");
			startTime = new java.util.Date().getTime();
			records = manager.findRecordsByAttributes(entity, record);
			endTime = new java.util.Date().getTime();
			log.debug("Using fuzzy query located " + records.size() + " records in " + (endTime-startTime)/1000 + " secs.");
			
			if (records.size() > 2) {
				startTime = new java.util.Date().getTime();
				records = manager.findRecordsByAttributes(entity, record, 1, 5);
				endTime = new java.util.Date().getTime();
				log.debug("Using fuzzy query and paging located " + records.size() + " records in " + (endTime-startTime)/1000 + " secs.");
			}
			
			Identifier id = new Identifier();
			id.setIdentifier("identifier-10");
			startTime = new java.util.Date().getTime();
			records = manager.findRecordsByIdentifier(entity, id);
			endTime = new java.util.Date().getTime();
			log.debug("Using identifiers located " + records.size() + " records in " + (endTime-startTime)/1000 + " secs.");
			for (Record rec : records) {
				log.debug("Record is " + rec.asString());
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
