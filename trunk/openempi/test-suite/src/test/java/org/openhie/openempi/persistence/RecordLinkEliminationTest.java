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
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.service.BaseServiceTestCase;

public class RecordLinkEliminationTest extends BaseServiceTestCase
{
    /**
     * This test case validates that when a record which has a link to 
     * another record is deleted (de-activated in OpenEMPI) all links
     * to this record are deleted.
     */
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
            recordLeft.set("postalCode", "20170");
            recordLeft.set("dateOfBirth", dateOfBirth);
            recordLeft = manager.addRecord(entity, recordLeft);

            recordRight = new Record(entity);
            recordRight.set("givenName", "John");
            recordRight.set("familyName", "Smith");
            recordRight.set("city", "Herndon");
            recordRight.set("postalCode", "20170");
            recordRight.set("dateOfBirth", dateOfBirth);
            recordRight = manager.addRecord(entity, recordRight);
            
            List<RecordLink> links = Context.getRecordQueryService().loadRecordLinks(entity,
                    recordLeft.getRecordId(), null);
            log.warn("Found: " + links.size() + " links.");
            printLinks(links);
            
            manager.removeRecord(entity, recordLeft);
            links = Context.getRecordQueryService().loadRecordLinks(entity,recordLeft.getRecordId(), null);
            log.warn("After removing record " + recordLeft.getRecordId() + " found " + links.size() + " links.");
            printLinks(links);
            
            manager.removeRecord(entity, recordRight);
            
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

    private void printLinks(List<RecordLink> links) {
        for (RecordLink link : links) {
            log.warn("Link: " + link);
        }
    }		
}
