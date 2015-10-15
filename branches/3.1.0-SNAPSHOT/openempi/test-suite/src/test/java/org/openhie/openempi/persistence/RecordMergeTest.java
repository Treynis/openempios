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
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Nationality;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.service.BaseServiceTestCase;
import org.openhie.openempi.service.PersonManagerService;

public class RecordMergeTest extends BaseServiceTestCase
{
	private RecordManagerService recordManagerService;
	private Entity entity;
	private Record firstPerson;
	private Record secondPerson;
	private Record thirdPerson;
    private Record fourthPerson;
	
	/**
MSH|^~\\&|NIST_SENDER^^|NIST^^|NIST_RECEIVER^^|NIST^^|20101101161058||ADT^A04^ADT_A01|NIST-101101161058473|P|2.3.1||||||||
EVN||20101020||||
PID|||MW-10001^^^NIST2010&2.16.840.1.113883.3.72.5.9.1&ISO||WASHINGTON^MARY^^^^^L||19771208|F|||100 JORIE BLVD^^CHICAGO^IL^60523||||||||100-09-1234||||||||||||||||||||
PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||

MSH|^~\\&|NIST_SENDER^^|NIST^^|NIST_RECEIVER^^|NIST^^|20101101161108||ADT^A04^ADT_A01|NIST-101101161108875|P|2.3.1||||||||
EVN||20101020||||
PID|||MW-20002^^^IHE2010&1.3.6.1.4.1.21367.2010.1.1&ISO||WASHINGTON^MARY^^^^^L||19771208|F|||100 JORIE BLVD^^CHICAGO^IL^60523||||||||100-09-1234||||||||||||||||||||
PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||

MSH|^~\\&|NIST_SENDER^^|NIST^^|NIST_RECEIVER^^|NIST^^|20101101161119||ADT^A04^ADT_A01|NIST-101101161119698|P|2.3.1||||||||
EVN||20101020||||
PID|||ML-30003^^^NIST2010&2.16.840.1.113883.3.72.5.9.1&ISO||LINCOLN^MARY^^^^^L||19771208|F|||JEAN JAQUES BLVD^^NEW YORK^NY^60001||||||||100-09-1234||||||||||||||||||||
PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||

MSH|^~\\&|NIST_SENDER^^|NIST^^|NIST_RECEIVER^^|NIST^^|20101101161119||ADT^A04^ADT_A01|NIST-101101161119699|P|2.3.1||||||||
EVN||20101020||||
PID|||ML-40004^^^IHE2010&1.3.6.1.4.1.21367.2010.1.1&ISO||LINCOLN^MARY^^^^^L||19771208|F|||JEAN JAQUES BLVD^^NEW YORK^NY^60001||||||||100-09-1234||||||||||||||||||||
PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||

MSH|^~\\&|NIST_SENDER^^|NIST^^|NIST_RECEIVER^^|NIST^^|20101101161122||ADT^A40^ADT_A39|NIST-101101161122806|P|2.3.1||||||||
EVN|A40|20060919004624||||20060919004340
PID|||ML-30003^^^NIST2010&2.16.840.1.113883.3.72.5.9.1&ISO||LINCOLN^MARY^^^^^L||19771208|F|||JEAN JAQUES BLVD^^NEW YORK^NY^60001||||||||100-09-1234||||||||||||||||||||
MRG|MW-10001^^^NIST2010&2.16.840.1.113883.3.72.5.9.1&ISO|

	 */
	protected void setupTest() {
		recordManagerService = Context.getRecordManagerService();
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        List<Entity> entities = defService.loadEntities();
        if (entities.size() == 0) {
            assertTrue("You need to define person entity in the database for this to work.", false);
        }
        entity = entities.get(0);

		Date dob = new Date();
		// PID|||MW-10001^^^NIST2010&2.16.840.1.113883.3.72.5.9.1&ISO||WASHINGTON^MARY^^^^^L||19771208|F|||100 JORIE BLVD^^CHICAGO^IL^60523||||||||100-09-1234
		firstPerson = buildTestPerson(entity, "MARY", "WASHINGTON", dob, "MW-10001", "NIST2010", "703-1212", "100 JORIE BLVD", "CHICAGO", "IL", "60523");
		// PID|||MW-20002^^^IHE2010&1.3.6.1.4.1.21367.2010.1.1&ISO||WASHINGTON^MARY^^^^^L||19771208|F|||100 JORIE BLVD^^CHICAGO^IL^60523||||||||100-09-1234
		secondPerson = buildTestPerson(entity, "MARY", "WASHINGTON", dob, "MW-20002", "IHE2010", "703-1212", "100 JORIE BLVD", "CHICAGO", "IL", "60523");
		// PID|||ML-30003^^^NIST2010&2.16.840.1.113883.3.72.5.9.1&ISO||LINCOLN^MARY^^^^^L||19771208|F|||JEAN JAQUES BLVD^^NEW YORK^NY^60001||||||||100-09-1234
		thirdPerson = buildTestPerson(entity, "MARY", "LINCOLN", dob, "ML-30003", "NIST2010", "703-1212", "JEAN JAQUES BLVD", "NEW YORK", "NY", "60001");
		// PID|||ML-40004^^^IHE2010&1.3.6.1.4.1.21367.2010.1.1&ISO||LINCOLN^MARY^^^^^L||19771208|F|||JEAN JAQUES BLVD^^NEW YORK^NY^60001||||||||100-09-1234
        fourthPerson = buildTestPerson(entity, "MARY", "LINCOLN", dob, "ML-40004", "IHE2010", "703-1212", "JEAN JAQUES BLVD", "NEW YORK", "NY", "60001");
	}
	
	public void testLink() {
	    setupTest();
		assertNotNull("Unable to find first person.", firstPerson);
		assertNotNull("Unable to find second person.", secondPerson);
        assertNotNull("Unable to find third person.", thirdPerson);
        assertNotNull("Unable to find fourth person.", fourthPerson);
		try {
		    Identifier identifier = firstPerson.getIdentifiers().iterator().next();
		    Identifier retiredIdentifier = new Identifier();
		    retiredIdentifier.setIdentifier(identifier.getIdentifier());
		    
    		firstPerson = recordManagerService.addRecord(entity, firstPerson);
    		System.out.println("Added first Person");
    		System.out.println(printRecord(firstPerson));

    		secondPerson = recordManagerService.addRecord(entity, secondPerson);
            System.out.println("Added second Person");
            System.out.println(printRecord(secondPerson));

            identifier = thirdPerson.getIdentifiers().iterator().next();
            Identifier survivingIdentifer = new Identifier();
            survivingIdentifer.setIdentifier(identifier.getIdentifier());
            
            thirdPerson = recordManagerService.addRecord(entity, thirdPerson);
            System.out.println("Added third Person");
            System.out.println(printRecord(thirdPerson));

            fourthPerson = recordManagerService.addRecord(entity, fourthPerson);
            System.out.println("Added fourth Person");
            System.out.println(printRecord(fourthPerson));
            
            System.out.println("Will merge two person, retiring " + retiredIdentifier + " and surviving " + survivingIdentifer);
            recordManagerService.mergeRecords(entity, retiredIdentifier, survivingIdentifer);

		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    /*
		    if (firstPerson != null) {
	            try {
	                personManagerService.removePerson(firstPerson.getPersonId());
	            } catch (Exception e) {
	            }
		    }
            if (secondPerson != null) {
                try {
                    personManagerService.removePerson(secondPerson.getPersonId());
                } catch (Exception e) {
                }
            }
            if (thirdPerson != null) {
                try {
                    personManagerService.removePerson(thirdPerson.getPersonId());
                } catch (Exception e) {
                }
            }
            if (fourthPerson != null) {
                try {
                    personManagerService.removePerson(fourthPerson.getPersonId());
                } catch (Exception e) {
                }
            }*/
		}
		
	}
	
	private String printRecord(Record person) {
	    StringBuffer sb = new StringBuffer();
	    sb.append("First Name: ").append(person.get("givenName")).append("\n");
        sb.append("Last Name: ").append(person.get("familyName")).append("\n");
        sb.append("Date of Birth: ").append(person.get("dateOfBirth")).append("\n");
        sb.append("Address: ").append(person.get("address1")).append("\n");
        sb.append("City: ").append(person.get("city")).append("\n");
        sb.append("Zip Code: ").append(person.get("postalCode")).append("\n");
        for (Identifier id : person.getIdentifiers()) {
            sb.append("\tIdentifier: ").append(id.getIdentifier()).append("->")
                .append(id.getIdentifierDomain().getIdentifierDomainName())
                .append("\n");
        }
        return sb.toString();
    }

	protected Record buildTestPerson(Entity entity, String firstName, String lastName, Date dateOfBirth, String identifier,
	        String domainName, String phone, String address, String city, String state, String zipCode) {
        Record record = new Record(entity);
        record.set("givenName", firstName);
        record.set("familyName", lastName);
        record.set("dateOfBirth", dateOfBirth);
        record.set("phoneNumber", phone);
        record.set("address1", address);
        record.set("city", city);
        record.set("state", state);
        record.set("postalCode", zipCode);
        
        Identifier id = new Identifier();
        id.setIdentifier(identifier);
        IdentifierDomain domain = Context.getIdentifierDomainService().findIdentifierDomainByName(domainName);
        id.setIdentifierDomain(domain);
        id.setIdentifierDomainId(domain.getIdentifierDomainId());
        id.setRecord(record);
        record.addIdentifier(id);

        return record;
    }
}
