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

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.service.BaseServiceTestCase;
import org.openhie.openempi.service.PersonQueryService;

public class EntityUpdateDeleteTest extends BaseServiceTestCase
{	
	public void testUpdate() {
		try {
			RecordManagerService manager = Context.getRecordManagerService();
			RecordQueryService queryService = Context.getRecordQueryService();
			PersonQueryService personQueryService = Context.getPersonQueryService();
			EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
			List<Entity> entities = defService.loadEntities();
			if (entities.size() == 0) {
				log.debug("You need to define some entities in the database for this to work.");
				return;
			}
			Entity entity = entities.get(0);
			log.debug("Testing with entity " + entity);
			
			List<IdentifierDomain> domains = personQueryService.getIdentifierDomains();
			if (domains == null || domains.size() == 0) {
				assertTrue("There are no identifier domains in the system.", true);
				return;
			}
			IdentifierDomain testDomain = domains.get(0);
			
			Record record = new Record(entity);
			record.set("firstName", "TestUpdateIds");
			record.set("lastName", "DeleteIds");
			record.set("birthOrder", new Integer(1));
			for (int i=0; i < 3; i++) {
				addRecordIdentifiersAttribute(record, testDomain, i);
			}
			record = manager.addRecord(entity, record);
			log.debug("Created record: " + record.asString());
			log.debug("Before the update the record identifiers are: ");
			for (Identifier id : record.getIdentifiers()) {
				log.debug("Identifier is: " + id.getIdentifier() + " with dateVoided of " + id.getDateVoided());
			}
			
			record.set("birthOrder", new Integer(2));
			// Change the identifier of the first identifier entry.
			Identifier modId = record.getIdentifiers().get(0);
			modId.setIdentifier("12121212");
			// Remove the second identifier from the list of identifiers
			modId = record.getIdentifiers().get(1);
			record.getIdentifiers().remove(modId);
			// Add a brand new identifier
			addRecordIdentifiersAttribute(record, testDomain, 5);
			record = manager.updateRecord(entity, record);
			log.debug("Updated the record: " + record.asString());
			log.debug("After the update the record identifiers are: ");
			for (Identifier id : record.getIdentifiers()) {
				log.debug("Identifier is: " + id.getIdentifier() + " with dateVoided of " + id.getDateVoided());
			}
			
			manager.deleteRecord(entity, record);
			List<Record> records = queryService.findRecordsByAttributes(entity, record);
			
			record = new Record(entity);
			record.set("firstName", "TestUpdateIds");
			List<Record> list = queryService.findRecordsByAttributes(entity, record);
			for (Record rec : list) {
				manager.deleteRecord(entity, record);
			}
			log.debug("After delete the number of matched records is: " + records.size());			
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testUpdateDelete() {
		try {
			RecordManagerService manager = Context.getRecordManagerService();
			RecordQueryService queryService = Context.getRecordQueryService();
			EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
			List<Entity> entities = defService.loadEntities();
			if (entities.size() == 0) {
				log.debug("You need to define some entities in the database for this to work.");
				return;
			}
			Entity entity = entities.get(0);
			log.debug("Testing with entity " + entity);
			
			Record record = new Record(entity);
			record.set("firstName", "TestUpdate");
			record.set("lastName", "Delete");
			record.set("birthOrder", new Integer(1));
			record = manager.addRecord(entity, record);
			
			log.debug("Created record: " + record.asString());
			
			record.set("birthOrder", new Integer(2));
			record = manager.updateRecord(entity, record);
			log.debug("Updated the record: " + record.asString());
			
			manager.deleteRecord(entity, record);
			List<Record> records = queryService.findRecordsByAttributes(entity, record);
			
			log.debug("After delete the number of matched records is: " + records.size());			
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void addRecordIdentifiersAttribute(Record record, IdentifierDomain testDomain, int i) {
		Identifier id = new Identifier();
		id.setIdentifier("identifier-" + i);
		id.setRecord(record);
		id.setIdentifierDomain(testDomain);
		record.addIdentifier(id);
	}
}
