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
package org.openhie.openempi.openpixpdqadapter;

import java.util.List;

import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v231.segment.MSA;

public class PixFeedDontAddExistingIdentifier extends AbstractPixTest
{

    public PixFeedDontAddExistingIdentifier() {
    }

    public void testMultipleAdds() {
        
        try {
            //Step 1: PIX Feed one patient in HIMSS2005
            String message = "MSH|^~\\&|TEST_HARNESS^^|TEST^^|OpenEMPI|OpenHIE|20141014||ADT^A01^ADT_A01|TEST-FAKE|P|2.3.1|1234||AL|NE\r" +
              "EVN||20141014|||||OpenEMPI\r" +
              "PID|||JD-123^^^&2.16.840.1.113883.3.72.5.9.1&ISO||DOE^JOHN^^^^^L||19000101|M|||123 Fake Road^^INDIANAPOLIS^IN^46254|\r" +
              "PV1||I||||||||||||||||||||||||||||||||||||||||||||||||||";
            Message response = sendMessage(message);
            System.out.println("Received response:\n" + getResponseString(response));
            MSA msa = (MSA)response.get("MSA");
            assertEquals("AA", msa.getAcknowledgementCode().getValue());

            message = "MSH|^~\\&|TEST_HARNESS^^|TEST^^|OpenEMPI|OpenHIE|20141014||ADT^A01^ADT_A01|TEST-FAKE|P|2.3.1|1234||AL|NE\r" +
                    "EVN||20141014|||||OpenEMPI\r" +
                    "PID|||JD-123^^^&2.16.840.1.113883.3.72.5.9.1&ISO||DOE^JON^^^^^L||19000101|M|||123 Fake Road^^INDIANAPOLIS^IN^46254O|\r" +
                    "PV1||I||||||||||||||||||||||||||||||||||||||||||||||||||";
            response = sendMessage(message);
            System.out.println("Received response:\n" + getResponseString(response));
            msa = (MSA)response.get("MSA");
            assertEquals("AA", msa.getAcknowledgementCode().getValue());
           
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
            record.set("familyName", "DOE");
            List<Record> found = queryService.findRecordsByAttributes(entity, record);
            log.debug("Found " + found.size() + " records.");
            assertEquals("There should only be one such record.", found.size(), 1);
        } catch(Exception e) {
            e.printStackTrace();
            fail("Fail to test PIX Mesa 10501 PIX Feed and Query");
        }
    }
    
    
    
    @Override
    protected void tearDown() throws Exception {
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
            record.set("familyName", "DOE");
            List<Record> found = queryService.findRecordsByAttributes(entity, record);
            log.debug("Found " + found.size() + " records.");
            for (Record rec : found) {
                rec = manager.removeRecord(entity, rec);
                log.debug("Removed the record: " + record);
            }
         } catch (Exception e) {
            e.printStackTrace();
        }
        super.tearDown();
    }
}
