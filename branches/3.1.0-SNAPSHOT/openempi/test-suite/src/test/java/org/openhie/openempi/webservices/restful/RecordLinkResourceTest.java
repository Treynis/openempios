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
import javax.ws.rs.core.Response.Status;

import org.openempi.webservices.restful.Constants;
import org.openempi.webservices.restful.model.FieldValue;
import org.openempi.webservices.restful.model.Record;
import org.openempi.webservices.restful.model.RecordLink;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.header.MediaTypes;

public class RecordLinkResourceTest extends BaseRestfulServiceTestCase
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

    public void testLoadRecordLinkById() {
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

        // get record links
        List<RecordLink> recordLinks = getWebResource()
                .path(Constants.VERSION)
                .path("record-links")
                .queryParam("entityId", entity.getEntityVersionId().toString())
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                //.header("Accept", "application/xml")
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<RecordLink>>() { });
        assertTrue("Failed to retrieve record links.", recordLinks != null);
        assertTrue("Not found record links.", recordLinks.size() > 0);


        // get by record link id
        RecordLink recordLink = recordLinks.get(0);
        recordLink = getWebResource()
                .path(Constants.VERSION)
                .path("record-links")
                .path(recordLink.getRecordLinkId().toString())
                .queryParam("entityId", entity.getEntityVersionId().toString())
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                //.header("Accept", "application/xml")
                .accept(MediaType.APPLICATION_JSON)
                .get(RecordLink.class);
        assertTrue("Failed to retrieve record link.", recordLink != null);

        // load by record id
        Record record = recordLink.getLeftRecord();
        recordLinks = getWebResource()
                .path(Constants.VERSION)
                .path("record-links")
                .path("/loadByRecordId")
                .queryParam("entityId", entity.getEntityVersionId().toString())
                .queryParam("recordId", record.getRecordId().toString())
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                //.header("Accept", "application/xml")
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<RecordLink>>() { });
        assertTrue("Failed to retrieve record links.", recordLinks != null);
        assertTrue("Not found record links.", recordLinks.size() > 0);
    }

    public void testAddUpdateAndRemoveRecordLink() {
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

        // add record link
        Record leftRecord = addRecord(entity, identifierDomain, "identifierLeft", "recordLeft", "Test");
        Record rightRecord = addRecord(entity, identifierDomain, "identifierRight", "recordRight", "Test");

        RecordLink recordLink = new RecordLink();
        recordLink.setEntityId(entity.getEntityVersionId());
        recordLink.setLeftRecord(leftRecord);
        recordLink.setRightRecord(rightRecord);
        recordLink.setState("M");
        recordLink.setWeight(2.0);

        recordLink = getWebResource()
                .path(Constants.VERSION)
                .path("record-links")
//                .path("addRecordLink")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                //.header("Accept", "application/xml")
                .accept(MediaType.APPLICATION_JSON)
                .post(RecordLink.class, recordLink);
        assertTrue("Failed to add new record link.", recordLink != null);
        assertTrue("Failed to add new record link.", recordLink.getState().equals("M"));


        // update record link
        recordLink.setState("P");
        recordLink = getWebResource()
                .path(Constants.VERSION)
                .path("record-links")
//                .path("updateRecordLink")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                //.header("Accept", "application/xml")
                .accept(MediaType.APPLICATION_JSON)
                .put(RecordLink.class, recordLink);
        assertTrue("Failed to update record link.", recordLink != null);


        // get by record link id
        recordLink = getWebResource()
                .path(Constants.VERSION)
                .path("record-links")
                .path(recordLink.getRecordLinkId().toString())
                .queryParam("entityId", entity.getEntityVersionId().toString())
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                //.header("Accept", "application/xml")
                .accept(MediaType.APPLICATION_JSON)
                .get(RecordLink.class);
        assertTrue("Failed to retrieve record link.", recordLink != null);
        assertTrue("Failed to retrieve record link.", recordLink.getState().equals("P"));

        // remove record link
        ClientResponse response = getWebResource()
                .path(Constants.VERSION)
                .path("record-links")
//                .path("removeRecordLink")
                .queryParam("entityId", recordLink.getEntityId().toString())
                .path(recordLink.getRecordLinkId().toString())
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                //.header("Accept", "application/xml")
                .accept(MediaType.APPLICATION_JSON)
                .delete(ClientResponse.class);

        if (response.getStatus() != Status.OK.getStatusCode()) {
            assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.BAD_REQUEST.getStatusCode() ||
                                                                         response.getStatus() == Status.NOT_FOUND.getStatusCode() ||
                                                                         response.getStatus() == Status.CONFLICT.getStatusCode());
        }

        // remove record left
        response = getWebResource()
                .path(Constants.VERSION)
                .path("records")
//                .path("removeRecord")
                .queryParam("entityId", leftRecord.getEntityId().toString())
                .queryParam("recordId", leftRecord.getRecordId().toString())
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                //.header("Accept", "application/xml")
                .accept(MediaType.APPLICATION_JSON)
                .delete(ClientResponse.class);

        if (response.getStatus() != Status.OK.getStatusCode()) {
            assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.BAD_REQUEST.getStatusCode() ||
                                                                         response.getStatus() == Status.NOT_FOUND.getStatusCode() ||
                                                                         response.getStatus() == Status.CONFLICT.getStatusCode());
        }

        // remove record right
        response = getWebResource()
                .path(Constants.VERSION)
                .path("records")
//                .path("removeRecord")
                .queryParam("entityId", rightRecord.getEntityId().toString())
                .queryParam("recordId", rightRecord.getRecordId().toString())
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

    public Record addRecord(Entity entity, IdentifierDomain identifierDomain, String identifier, String givenName, String familyName) {
        // new restful model Record
        Record record = new Record();
        record.setEntityId(entity.getEntityVersionId());
        record.addFieldValue(new FieldValue("familyName", familyName));
        record.addFieldValue(new FieldValue("givenName", givenName));

        Identifier id = new Identifier();
        id.setIdentifier(identifier);
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
        return record;
    }
}
