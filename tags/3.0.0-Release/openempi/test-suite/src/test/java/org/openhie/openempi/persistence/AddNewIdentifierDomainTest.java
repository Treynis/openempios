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

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.service.BaseServiceTestCase;

public class AddNewIdentifierDomainTest extends BaseServiceTestCase
{
	public void testInitialization() {
		RecordManagerService manager = Context.getRecordManagerService();
		RecordQueryService queryManager = Context.getRecordQueryService();
		Entity entity = getTestEntity();
		log.debug("Testing with entity " + entity);
		try {
			IdentifierDomain testDomain = createNewDomain();
			int start=0;
			int count = 50;
			long startTime = new java.util.Date().getTime();
			Record savedRecord = null;
            Record record = new Record(entity);
			for (EntityAttribute attrib : entity.getAttributes()) {
			    if (attrib.getDatatype().getDatatypeCd() == EntityAttributeDatatype.STRING_DATATYPE_CD) {
			        record.set(attrib.getName(), "Test");
			    } else if (attrib.getDatatype().getDatatypeCd() == EntityAttributeDatatype.INTEGER_DATATYPE_CD) {
                    record.set(attrib.getName(), new Integer(1));
                } else if (attrib.getDatatype().getDatatypeCd() == EntityAttributeDatatype.DATE_DATATYPE_CD) {
                    record.set(attrib.getName(), new Date());
                }
			}
            addRecordIdentifiersAttribute(record, testDomain, 99);
            record = manager.addRecord(entity, record);
			long endTime = new java.util.Date().getTime();
			log.debug("inserted " + count + " records in " + (endTime-startTime)/1000 + " secs.");
			
			manager.deleteRecord(entity, record);
			log.debug("Deleted record with ID " + record.getRecordId());
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private IdentifierDomain createNewDomain() {
        IdentifierDomain domain = new IdentifierDomain();
        String name = "domain" + new Date().getTime();
        domain.setIdentifierDomainName(name);
        domain.setIdentifierDomainDescription(name);
        domain.setNamespaceIdentifier(name);
        domain.setUniversalIdentifier(name);
        domain.setUniversalIdentifierTypeCode(name);
        return domain;
    }

    private void addRecordIdentifiersAttribute(Record record, IdentifierDomain testDomain, int i) {
		Identifier id = new Identifier();
		id.setIdentifier("identifier-" + i);
		id.setRecord(record);
		id.setIdentifierDomain(testDomain);
		record.addIdentifier(id);
	}
}
