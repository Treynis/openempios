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
package org.openhie.openempi.service;

import java.util.Date;
import java.util.List;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Record;


public class GlobalIdentifierAddUpdateTest extends BaseServiceTestCase
{
    public void testAddPerson() {
        Entity entity = getTestEntity();
        log.debug("Testing with entity " + entity);

        Date dateOfBirth = new Date();
        Record recordLeft=null,recordRight=null;
        RecordManagerService manager = Context.getRecordManagerService();

        IdentifierDomain domain = getGlobalIdentifierDomain();
        if (domain == null) {
            assertTrue("Cannot test global identifier domain functionality without it being configured.", true);
            return;
        }
        try {
            recordLeft = createRecord(entity, dateOfBirth);
            recordLeft = manager.addRecord(entity, recordLeft);
            
            Identifier globalIdentifier = extractGlobalIdentifier(recordLeft, domain);
            assertFalse("The global identifier was not assigned.", globalIdentifier == null);
            log.info("Assigned global identifier of " + globalIdentifier.getIdentifier());

            recordLeft.set("dateOfBirth", new Date());
            recordLeft = manager.updateRecord(entity, recordLeft);
            
            Identifier globalIdentifierAfterUpdate= extractGlobalIdentifier(recordLeft, domain);
            assertFalse("The global identifier was not assigned.", globalIdentifierAfterUpdate == null);
            log.info("Assigned global identifier after update of " + globalIdentifierAfterUpdate.getIdentifier());
            
            recordRight = createRecord(entity, dateOfBirth);
            recordRight = manager.addRecord(entity, recordRight);
            
            Identifier globalIdentifierTwo = extractGlobalIdentifier(recordRight, domain);
            assertFalse("The global identifier was not assigned.", globalIdentifierTwo == null);
            log.info("Assigned global identifier of " + globalIdentifierTwo.getIdentifier());
            
            assertTrue("The global identifier assigned to this new record must be identical to "
                    + "that assigned to the first record.", 
                    globalIdentifier.getIdentifier().equalsIgnoreCase(globalIdentifierTwo.getIdentifier()));
            
            // Now let us modify the second record so that they will be separated.
            modifyRecord(recordRight);
            recordRight = manager.updateRecord(entity, recordRight);
            
            Identifier globalIdentifierThree = extractGlobalIdentifier(recordRight, domain);
            assertFalse("The global identifier was not assigned.", globalIdentifierThree == null);
            log.info("Assigned global identifier to updated record of " + globalIdentifierThree.getIdentifier());

            assertTrue("The global identifier assigned to this updated record must not be identical to "
                    + "that assigned to the first record.", 
                    !globalIdentifier.getIdentifier().equalsIgnoreCase(globalIdentifierThree.getIdentifier()));
            
        } catch (ApplicationException e) {
            log.error("Failed due to: " + e, e);
            assertTrue("Failed due to " + e.getMessage(), false);
        } finally {
            if (recordLeft != null) {
                try {
                    manager.removeRecord(entity, recordLeft);
                } catch (ApplicationException e) {
                    e.printStackTrace();
                }
            }
            if (recordRight!= null) {
                try {
                    manager.removeRecord(entity, recordRight);
                } catch (ApplicationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Identifier extractGlobalIdentifier(Record recordLeft, IdentifierDomain domain) {
        Identifier globalIdentifier = null;
        for (Identifier identifier : recordLeft.getIdentifiers()) {
            if (identifier.getIdentifierDomain().getIdentifierDomainId() == domain.getIdentifierDomainId() &&
                    identifier.getDateVoided() == null) {
                globalIdentifier = identifier;
                break;
            }
        }
        return globalIdentifier;
    }

    private Record createRecord(Entity entity, Date dateOfBirth) {
        Record record;
        record = new Record(entity);
        record.set("givenName", "John");
        record.set("familyName", "Smith");
        record.set("city", "Herndon");
        record.set("postalCode", "20170");
        record.set("dateOfBirth", dateOfBirth);
        return record;
    }

    private void modifyRecord(Record record) {
        record.set("givenName", "Frank");
        record.set("familyName", "McCourt");
        record.set("city", "Limerick");
        record.set("postalCode", "11111");
    }
    
    private IdentifierDomain getGlobalIdentifierDomain() {
        IdentifierDomain domain = Context.getConfiguration().getGlobalIdentifierDomain();
        return domain;
    }
}
