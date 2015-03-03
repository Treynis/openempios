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
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.service.BaseServiceTestCase;

public class RecordLinkQueryTest extends BaseServiceTestCase
{
    public void testLoadingRecordLinks() {
        Integer PAGE_SIZE = new Integer(10);

        try {
            RecordQueryService manager = Context.getRecordQueryService();

            EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
            List<Entity> entities = defService.loadEntities();
            if (entities.size() == 0) {
                log.debug("You need to define some entities in the database for this to work.");
                return;
            }

            Long totalCount = manager.getRecordLinkCount(entities.get(0), RecordLinkState.POSSIBLE_MATCH);
            if (totalCount > 0) {
                log.info("Found probable match link count: " + totalCount);

                if (totalCount < PAGE_SIZE) {
                    PAGE_SIZE = totalCount.intValue();
                }

                List<RecordLink> links = manager.loadRecordLinks(entities.get(0), RecordLinkState.POSSIBLE_MATCH, 0,
                        PAGE_SIZE);
                for (RecordLink link : links) {
                    link = manager.loadRecordLink(entities.get(0), link.getRecordLinkId());
                    assertNotNull("Link should not be empty", link);
                    log.info("Found link: " + link);
                }

                RecordLink link = new RecordLink();
                link.setRecordLinkId("1");
                link = manager.loadRecordLink(entities.get(0), link.getRecordLinkId());
                log.info("Found link by ID: " + link);
            }

            Record record = new Record(entities.get(0));
            record.set("lastName", "Faull");
            record.set("firstName", "Thomas");
            List<Record> records = manager.findRecordsByAttributes(entities.get(0), record);
            if (records != null && records.size() > 0) {
                record = records.get(0);
                List<RecordLink> links = manager.loadRecordLinks(entities.get(0), record.getRecordId());
                assertNotNull("The list of links must not be null.", links);
                for (RecordLink entry : links) {
                    log.info("Found link: " + entry);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("This query for record links should be working.", false);
        }
    }

    public void testUpdatedRecordLinks() {
        Integer PAGE_SIZE = new Integer(10);

        try {
            RecordQueryService queryService = Context.getRecordQueryService();
            RecordManagerService managerService = Context.getRecordManagerService();

            EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
            List<Entity> entities = defService.loadEntities();
            if (entities.size() == 0) {
                log.debug("You need to define some entities in the database for this to work.");
                return;
            }

            Long totalCount = queryService.getRecordLinkCount(entities.get(0), RecordLinkState.POSSIBLE_MATCH);
            if (totalCount > 0) {
                log.info("Found probable match link count: " + totalCount);

                if (totalCount < PAGE_SIZE) {
                    PAGE_SIZE = totalCount.intValue();
                }

                List<RecordLink> links = queryService.loadRecordLinks(entities.get(0), RecordLinkState.POSSIBLE_MATCH,
                        0, PAGE_SIZE);
                for (RecordLink link : links) {

                    link = queryService.loadRecordLink(entities.get(0), link.getRecordLinkId());

                    link.setState(RecordLinkState.MATCH);
                    link = managerService.updateRecordLink(link);
                    assertEquals("State must be match", link.getState(), RecordLinkState.MATCH);

                    link.setState(RecordLinkState.NON_MATCH);
                    link = managerService.updateRecordLink(link);
                    assertEquals("State must be non-match", link.getState(), RecordLinkState.NON_MATCH);

                    link.setState(RecordLinkState.POSSIBLE_MATCH);
                    link = managerService.updateRecordLink(link);
                    assertEquals("State must be probable match", link.getState(), RecordLinkState.POSSIBLE_MATCH);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("This update state for record links should be working.", false);
        }
    }
}
