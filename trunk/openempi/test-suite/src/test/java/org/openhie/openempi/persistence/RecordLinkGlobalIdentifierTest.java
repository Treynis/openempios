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

import java.util.Date;
import java.util.List;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Gender;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.service.BaseServiceTestCase;

public class RecordLinkGlobalIdentifierTest extends BaseServiceTestCase
{
	public void testAddPerson() {
		RecordManagerService manager = Context.getRecordManagerService();
		EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
		List<Entity> entities = defService.loadEntities();
		if (entities.size() == 0) {
			assertTrue("You need to define some entities in the database for this to work.", false);
		}
		Entity entity = entities.get(0);
		log.debug("Testing with entity " + entity);
		Date dateOfBirth = new Date();
		Record recordLeft, recordRight;
        try {
            recordLeft = new Record(entity);
            recordLeft.set("givenName", "John");
            recordLeft.set("familyName", "Smith");
            recordLeft.set("city", "Herndon");
            recordLeft.set("state", "Virginia");
            recordLeft.set("postalCode", "20170");
            recordLeft.set("dateOfBirth", dateOfBirth);
            Gender gender = new Gender(2, "Male");
            recordLeft.set("gender", gender);
            recordLeft = manager.addRecord(entity, recordLeft);
            log.info("Imported record with id " + recordLeft.getRecordId());
            Identifier globalId = null;
            globalId = getGlobalIdentifier(recordLeft);
            assertNotNull("Global is should not be null.", globalId);
            log.info("Record has global identifier " + globalId);

            recordRight = new Record(entity);
            recordRight.set("givenName", "John");
            recordRight.set("familyName", "Smith");
            recordRight.set("city", "Herndon");
            recordRight.set("state", "Maryland");
            recordRight.set("postalCode", "22102");
            recordRight.set("dateOfBirth", dateOfBirth);
            gender = new Gender(1, "Female");
            recordRight.set("gender", gender);
            recordRight = manager.addRecord(entity, recordRight);
            log.info("Imported record with id " + recordRight.getRecordId());
            Identifier otherGlobalId = null;
            otherGlobalId = getGlobalIdentifier(recordRight);
            assertNotNull("Global is should not be null.", otherGlobalId);
            log.info("Record has global identifier " + otherGlobalId);
            
            assertTrue("The two records shouls not be linked together at this point.", !globalId.getIdentifier().equals(otherGlobalId.getIdentifier()));

            RecordLink link = new RecordLink();
            link.setRightRecord(recordRight);
            link.setLeftRecord(recordLeft);
            link.setLinkSource(new LinkSource(LinkSource.MANUAL_MATCHING_SOURCE));
            link.setState(RecordLinkState.MATCH);
            link.setWeight(1.0);
            link.setVector(128);
            manager.addRecordLink(link);
            log.info("Adding the link: " + link);
            
            recordLeft = Context.getRecordQueryService().loadRecordById(entity, recordLeft.getRecordId());
            recordRight = Context.getRecordQueryService().loadRecordById(entity, recordRight.getRecordId());
            globalId = getGlobalIdentifier(recordLeft);
            otherGlobalId = getGlobalIdentifier(recordRight);
            
            assertTrue("The two records shouls now be linked together at this point.", globalId.getIdentifier().equals(otherGlobalId.getIdentifier()));
            log.info("Records have the same global identifier of " + globalId.getIdentifier() + " and " + otherGlobalId.getIdentifier());
            
            Record query = new Record(entity);
            query.set("givenName", "John");
            query.set("familyName", "Smith");
            query.set("city", "Herndon");
            List<Record> records = Context.getRecordQueryService().findRecordsByAttributes(entity, query);
            log.info("Found " + records.size() + " matching records.");
            for (Record rec : records) {
                manager.removeRecord(entity, rec);
                log.info("Removed record " + rec);
            }

            /*
            recordRight = new Record(entity);
            recordRight.set("givenName", "John");
            recordRight.set("familyName", "Smith");
            recordRight.set("city", "Herndon");
            recordRight.set("postalCode", "20170");
            recordRight.set("dateOfBirth", dateOfBirth);
            recordRight = manager.addRecord(entity, recordRight);
            log.info("Added record with id " + recordRight.getRecordId());
            

            List<RecordLink> links = Context.getRecordQueryService().loadRecordLinks(entity, recordRight.getRecordId());
            log.debug("Found: " + links.size() + " links.");
            
            manager.removeRecord(entity, recordLeft);
            manager.removeRecord(entity, recordRight);
            */
        } catch (ApplicationException e) {
            log.error("Failed due to: " + e, e);
            assertTrue("Failed due to " + e.getMessage(), false);
        }
	}
	/**

	public void testAnotherPerson() {
		EntityManagerService manager = Context.getEntityManagerService();
		EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
		List<Entity> entities = defService.loadEntities();
		if (entities.size() == 0) {
			assertTrue("You need to define some entities in the database for this to work.", false);
		}
		Entity entity = entities.get(0);
		log.debug("Testing with entity " + entity);
		try {
			Record recordRight = new Record(entity);
			recordRight.set("firstName", "Jon");
			recordRight.set("lastName", "Smith");
			recordRight = manager.addEntity(entity, recordRight);

		} catch (ApplicationException e) {
			log.error("Failed due to: " + e, e);
			assertTrue("Failed due to " + e.getMessage(), false);
		}
	}
			Record recordRight = new Record(entity);
			recordRight.set("firstName", "Jon");
			recordRight.set("lastName", "Smith");
			recordRight = manager.addEntity(entity, recordRight);

			Record recordNext = new Record(entity);
			recordNext.set("firstName", "Johnny");
			recordNext.set("lastName", "Smith");
			recordNext = manager.addEntity(entity, recordNext);

			EntityLink link = new EntityLink();
			link.setLeftRecord(recordLeft);
			link.setRightRecord(recordRight);
			link.setWeight(1.0);
			link.setVector(8);
			link.setState(EntityLinkState.MATCH);
			link.setLinkSource(new LinkSource(LinkSource.MANUAL_MATCHING_SOURCE));
			manager.addEntityLink(link);
			
			link = new EntityLink();
			link.setLeftRecord(recordRight);
			link.setRightRecord(recordNext);
			link.setWeight(1.0);
			link.setVector(8);
			link.setState(EntityLinkState.MATCH);
			link.setLinkSource(new LinkSource(LinkSource.MANUAL_MATCHING_SOURCE));
			manager.addEntityLink(link);
	*/

    private Identifier getGlobalIdentifier(Record recordRight) {
        for (Identifier id : recordRight.getIdentifiers()) {
            if (id.getIdentifierDomain().getIdentifierDomainName().equals("ECID")) {
                return id;
            }
        }
        return null;
    }		
}
