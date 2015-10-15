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
import org.openhie.openempi.model.IdentifierUpdateEvent;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

public class NotificationResourceTest extends BaseRestfulServiceTestCase
{
    public void testGetNotificationCount() {

        // get all entities
        String count = getWebResource().path(Constants.VERSION).path("notifications/getNotificationCount")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey()).accept(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertTrue("Failed to get notification count.", count != null);
        System.out.println("testGetNotificationCount: " + count);
    }

    public void testRetrieveNotifications() {
        List<IdentifierUpdateEvent> identifierUpdateEvents = getWebResource().path(Constants.VERSION)
                .path("notifications")
                .queryParam("firstRecord", "0")
                .queryParam("maxRecords", "1")
                .queryParam("removeRecords", "false")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey()).accept(MediaType.APPLICATION_XML)
                .get(new GenericType<List<IdentifierUpdateEvent>>() { });

        assertTrue("Failed to retrieve identifierUpdateEvents list paged.", identifierUpdateEvents != null);
        System.out.println("testRetrieveNotifications:");
        for (IdentifierUpdateEvent identifierUpdateEvent : identifierUpdateEvents) {
            System.out.println(identifierUpdateEvent.toString());
        }
    }

    public void testRetrieveNotificationsByDate() {
        List<IdentifierUpdateEvent> identifierUpdateEvents = getWebResource().path(Constants.VERSION)
                .path("notifications/retrieveNotificationsByDate").queryParam("date", "2013-10-15")
                // YYYY-MM-DD
                .queryParam("removeRecords", "false")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_XML).get(new GenericType<List<IdentifierUpdateEvent>>() { });

        assertTrue("Failed to retrieve identifierUpdateEvents list paged.", identifierUpdateEvents != null);
        System.out.println("testRetrieveNotificationsByDate:");
        for (IdentifierUpdateEvent identifierUpdateEvent : identifierUpdateEvents) {
            System.out.println(identifierUpdateEvent.toString());
        }
    }

    public void testDeleteNotifications() {

        List<IdentifierUpdateEvent> identifierUpdateEvents = getWebResource().path(Constants.VERSION)
                .path("notifications")
                .queryParam("firstRecord", "0")
                .queryParam("maxRecords", "1")
                .queryParam("removeRecords", "false")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey()).accept(MediaType.APPLICATION_XML)
                .get(new GenericType<List<IdentifierUpdateEvent>>() { });

        assertTrue("Failed to retrieve identifierUpdateEvents list paged.", identifierUpdateEvents != null);
        System.out.println("testDeleteNotifications:");
        for (IdentifierUpdateEvent identifierUpdateEvent : identifierUpdateEvents) {
            System.out.println(identifierUpdateEvent.toString());

            ClientResponse response = getWebResource()
                    .path(Constants.VERSION)
                    .path("notifications")
                    .path(identifierUpdateEvent.getIdentifierUpdateEventId().toString())
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    .accept(MediaType.APPLICATION_JSON)
                    .delete(ClientResponse.class);

            if (response.getStatus() != Status.OK.getStatusCode()) {
                assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.BAD_REQUEST.getStatusCode() ||
                                                                             response.getStatus() == Status.NOT_FOUND.getStatusCode() ||
                                                                             response.getStatus() == Status.CONFLICT.getStatusCode());
            }
        }
    }
}
