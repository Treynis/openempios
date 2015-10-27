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
package org.openhie.openempi.webservices.restful;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.openempi.webservices.restful.Constants;
import org.openempi.webservices.restful.model.FieldValue;
import org.openempi.webservices.restful.model.Record;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class RecordResourceTest extends BaseRestfulServiceTestCase
{
    public void testApplicationWadl() {
        String serviceWadl = getWebResource()
        		.path("application.wadl")
        		.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        		.accept(MediaTypes.WADL)
        		.get(String.class);
        assertTrue("Looks like the expected wadl is not generated",
                serviceWadl.length() > 0);
    }
/**
    public void testLoadRecordById() {

        // get all entities
        List<Entity> entities = getWebResource()
                .path(Constants.VERSION)
                .path("entities")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Entity>>() { });
        assertTrue("Failed to retrieve entity list.", entities != null && entities.size() > 0);

        Entity entity = entities.get(0);
        for (Entity en: entities) {
            if (en.getName().equals("person")) {
                entity = en;
            }
        }

        // get an entity by id
        entity = getWebResource()
                .path(Constants.VERSION)
                .path("entities")
                .path(entity.getEntityVersionId().toString())
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .get(Entity.class);
        assertTrue("Failed to retrieve entity.", entity != null);


        // create a map of keyVals for search
        MultivaluedMap<String, String> nameQuery = new MultivaluedMapImpl();
        nameQuery.add("entityId", entity.getEntityVersionId().toString());
        nameQuery.add("keyVal", "givenName, John");
        //nameQuery.add("keyVal", "familyName, Hon");
        //nameQuery.add("keyVal", "dateOfBirth, 1961-01-01");
        //nameQuery.add("keyVal", "city,Tucson");

        // recordCountByAttributes
        String  recordCount = getWebResource()
                .path(Constants.VERSION)
                .path("records")
                .path("/recordCountByAttributes")
                .queryParams(nameQuery)
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                //.header("Accept", "application/xml")
                .accept(MediaType.TEXT_PLAIN)
                .get(String.class);
        assertTrue("record conut is 0.", !recordCount.isEmpty() && Long.parseLong(recordCount) > 0);


        // findByAttributes
        List<Record> records = getWebResource()
                .path(Constants.VERSION)
                .path("records")
                .path("/findByAttributes")
                .queryParams(nameQuery)
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                //.header("Accept", "application/xml")
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Record>>() { });
        assertTrue("Failed to retrieve records.", records != null);
        assertTrue("Not found records.", records.size() > 0);


        // recordCountByIdentifier
        recordCount = getWebResource()
                .path(Constants.VERSION)
                .path("records")
                .path("/recordCountByIdentifier")
                 .queryParam("entityId", entity.getEntityVersionId().toString())
                 .queryParam("identifier", records.get(0).getIdentifiers().get(0).getIdentifier())
                 .queryParam("identifierDomainId", records.get(0).getIdentifiers().get(0).getIdentifierDomainId().toString())
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                //.header("Accept", "application/xml")
                .accept(MediaType.TEXT_PLAIN)
                .get(String.class);
        assertTrue("record conut is 0.", !recordCount.isEmpty() && Long.parseLong(recordCount) > 0);


        // findByIdentifier
        records = getWebResource()
                .path(Constants.VERSION)
                .path("records")
                .path("/findByIdentifier")
                 .queryParam("entityId", entity.getEntityVersionId().toString())
                 .queryParam("identifier", records.get(0).getIdentifiers().get(0).getIdentifier())
                 .queryParam("identifierDomainId", records.get(0).getIdentifiers().get(0).getIdentifierDomainId().toString())
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                //.header("Accept", "application/xml")
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Record>>() { });
        assertTrue("Failed to retrieve records.", records != null);
        assertTrue("Not found records.", records.size() > 0);


        // loadRecordById
        Record record = getWebResource()
                    .path(Constants.VERSION)
                    .path("records")
                    .path(records.get(0).getRecordId().toString())
                    .queryParam("entityId", entity.getEntityVersionId().toString())
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    //.header("Accept", "application/xml")
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Record.class);
        assertTrue("Failed to retrieve record.", record != null);


        // loadRecordByIds
        nameQuery.clear();
        nameQuery.add("entityId", entity.getEntityVersionId().toString());
        for (Record rec :records) {
            nameQuery.add("recordId", rec.getRecordId().toString());
        }
        records = getWebResource()
                    .path(Constants.VERSION)
                    .path("records")
                    .queryParams(nameQuery)
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    //.header("Accept", "application/xml")
                    .accept(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<Record>>() { });
        assertTrue("Failed to retrieve records.", records != null);
        assertTrue("Not found records.", records.size() > 0);
    }

    public void testAddUpdateAndRemoveRecord() {
        // get all entities
        List<Entity> entities = getWebResource()
                .path(Constants.VERSION)
                .path("entities")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Entity>>() { });
        assertTrue("Failed to retrieve entity list.", entities != null && entities.size() > 0);

        Entity entity = entities.get(0);
        for (Entity en: entities) {
            if (en.getName().equals("person")) {
                entity = en;
            }
        }

        // get all identifier domains
        List<IdentifierDomain> identifierDomains = getWebResource()
                .path(Constants.VERSION)
                .path("identifier-domains")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<IdentifierDomain>>() { });
        assertTrue("Failed to retrieve identifier domain list.", identifierDomains != null && identifierDomains.size() > 0);
        IdentifierDomain identifierDomain = identifierDomains.get(0);


        // new restful model Record
        Record record = new Record();
        record.setEntityId(entity.getEntityVersionId());
        record.addFieldValue(new FieldValue("familyName", "Test"));
        record.addFieldValue(new FieldValue("givenName", "AddTest"));

        Identifier id = new Identifier();
        id.setIdentifier("identifier");
        id.setIdentifierDomain(identifierDomain);
        record.addIdentifier(id);

        // add new record
        record = getWebResource()
                .path(Constants.VERSION)
                .path("records")
//                .path("addRecord")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                //.header("Accept", "application/xml")
                .accept(MediaType.APPLICATION_JSON)
                .post(Record.class, record);
        assertTrue("Failed to add new record.", record != null);


        // update record
        for (FieldValue field: record.getFields()) {
            if (field.getName().equals("givenName")) {
                field.setValue("UpdateTest");
            }
        }
        record = getWebResource()
                .path(Constants.VERSION)
                .path("records")
//                .path("updateRecord")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                //.header("Accept", "application/xml")
                .accept(MediaType.APPLICATION_JSON)
                .put(Record.class, record);
        assertTrue("Failed to update record.", record != null);


        // remove record
        ClientResponse response = getWebResource()
                .path(Constants.VERSION)
                .path("records")
//                .path("removeRecord")
                .queryParam("entityId", record.getEntityId().toString())
                .path(record.getRecordId().toString())
                .queryParam("remove", new Boolean(true).toString())
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                //.header("Accept", "application/xml")
                .accept(MediaType.APPLICATION_JSON)
                .delete(ClientResponse.class);

        if (response.getStatus() != Status.OK.getStatusCode()) {
            assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.BAD_REQUEST.getStatusCode() ||
                                                                         response.getStatus() == Status.NOT_FOUND.getStatusCode() ||
                                                                         response.getStatus() == Status.CONFLICT.getStatusCode());
        }
    }
    **/
}
