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

import org.openempi.webservices.restful.Constants;
import org.openhie.openempi.model.IdentifierDomain;

import com.sun.jersey.api.client.GenericType;

public class IdentifierDomainResourceTest extends BaseRestfulServiceTestCase
{

    public void testGetAllIdentifierDomains() {

        // get all identifier domains
        List<IdentifierDomain> identifierDomains = getWebResource()
                .path(Constants.VERSION)
                .path("identifier-domains")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<IdentifierDomain>>() { });
        assertTrue("Failed to retrieve identifier domain list.", identifierDomains != null && identifierDomains.size() > 0);
    }

    public void testGetIdentifierDomainById() {

        List<IdentifierDomain> identifierDomains = getWebResource()
                .path(Constants.VERSION)
                .path("identifier-domains")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<IdentifierDomain>>() { });
        assertTrue("Failed to retrieve identifier domain list.", identifierDomains != null && identifierDomains.size() > 0);

        IdentifierDomain identifierDomain = identifierDomains.get(0);

        // get an identifier domain by id
        identifierDomain = getWebResource()
                .path(Constants.VERSION)
                .path("identifier-domains")
                .path(identifierDomain.getIdentifierDomainId().toString())
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .get(IdentifierDomain.class);
        assertTrue("Failed to retrieve identifier domain.", identifierDomain != null);

    }

    public void testAddUpdateIdentifierDomain() {
        IdentifierDomain identifierDomain = new IdentifierDomain();
        identifierDomain.setNamespaceIdentifier("Test");
        identifierDomain.setIdentifierDomainName("domainTest");

        // add an new identifier domain
        identifierDomain = getWebResource()
                .path(Constants.VERSION)
                .path("identifier-domains")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .post(IdentifierDomain.class, identifierDomain);
        assertTrue("Failed to add an new identifier domain.", identifierDomain != null);

        // get an identifier domain by id
        identifierDomain = getWebResource()
                .path(Constants.VERSION)
                .path("identifier-domains")
                .path(identifierDomain.getIdentifierDomainId().toString())
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .get(IdentifierDomain.class);
        assertTrue("Failed to retrieve identifier domain.", identifierDomain != null);

        // update identifier domain
        identifierDomain.setIdentifierDomainName("domainUpdate");

        identifierDomain = getWebResource()
                .path(Constants.VERSION)
                .path("identifier-domains")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .put(IdentifierDomain.class, identifierDomain);
        assertTrue("Failed to update identifier domain.", identifierDomain != null);
        assertTrue("Failed to update identifier domain.", identifierDomain.getIdentifierDomainName().equals("domainUpdate"));

    }
}
