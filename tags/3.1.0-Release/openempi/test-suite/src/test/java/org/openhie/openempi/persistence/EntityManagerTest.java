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
import org.openhie.openempi.service.IdentifierDomainService;

public class EntityManagerTest extends BaseServiceTestCase
{
	public void testInitialization() {
		RecordManagerService manager = Context.getRecordManagerService();
		RecordQueryService queryManager = Context.getRecordQueryService();
		IdentifierDomainService identifierDomainService = Context.getIdentifierDomainService();
		EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
		List<Entity> entities = defService.loadEntities();
		if (entities.size() == 0) {
			log.debug("You need to define some entities in the database for this to work.");
			return;
		}
		Entity entity = entities.get(0);
		log.debug("Testing with entity " + entity);
		try {
			List<IdentifierDomain> domains = identifierDomainService.getIdentifierDomains();
			if (domains == null || domains.size() == 0) {
				assertTrue("There are no identifier domains in the system.", true);
				return;
			}
			IdentifierDomain testDomain = domains.get(0);
			int start=0;
			int count = 50;
			long startTime = new java.util.Date().getTime();
			Record savedRecord = null;
			for (int i=start; i < start+count; i++) {
				Record record = new Record(entity);
				record.set("firstName", "John" + i);
				record.set("lastName", "Smith" + i);
				record.set("dateOfBirth", new java.util.Date());
				record.set("birthOrder", i);
				addRecordIdentifiersAttribute(record, testDomain, i);
				record = manager.addRecord(entity, record);
				if (i == 0) {
					savedRecord = record;
				}
			}
			long endTime = new java.util.Date().getTime();
			log.debug("inserted " + count + " records in " + (endTime-startTime)/1000 + " secs.");
			
			Record template = new Record(entity);
			template.set("firstName", "John%");
			List<Record> records = queryManager.findRecordsByAttributes(entity, template);
			for (Record record : records) {
				manager.deleteRecord(entity, record);
				log.debug("Deleted record with ID " + record.getRecordId());
			}
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
