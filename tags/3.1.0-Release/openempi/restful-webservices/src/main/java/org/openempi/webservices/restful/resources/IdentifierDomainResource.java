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
package org.openempi.webservices.restful.resources;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.service.IdentifierDomainService;

@Path("/{versionId}/identifier-domains")
public class IdentifierDomainResource extends BaseResource
{
    @GET
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<IdentifierDomain> getIdentifierDomains(@PathParam("versionId") String versionId) {
        validateVersion(versionId);
        IdentifierDomainService domainService = Context.getIdentifierDomainService();
        List<IdentifierDomain> identifierDomains = domainService.getIdentifierDomains();
        if (identifierDomains == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        if (identifierDomains.size() == 0) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return identifierDomains;
    }

    @GET
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public IdentifierDomain loadIdentifierDomainById(@PathParam("versionId") String versionId,
            @PathParam("id") Integer domainId) {
        validateVersion(versionId);
        if (domainId == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        IdentifierDomainService domainService = Context.getIdentifierDomainService();
        IdentifierDomain identifierDomain = domainService.findIdentifierDomainById(domainId);
        if (identifierDomain == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return identifierDomain;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public IdentifierDomain addIdentifierDomain(@PathParam("versionId") String versionId,
                                                IdentifierDomain identifierDomain) {
        IdentifierDomainService domainService = Context.getIdentifierDomainService();
        try {
            identifierDomain = domainService.addIdentifierDomain(identifierDomain);
        } catch (ApplicationException e) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
        return identifierDomain;
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public IdentifierDomain updateIdentifierDomain(@PathParam("versionId") String versionId,
                                                   IdentifierDomain identifierDomain) {
        IdentifierDomainService domainService = Context.getIdentifierDomainService();
        try {
            identifierDomain = domainService.updateIdentifierDomain(identifierDomain);
        } catch (ApplicationException e) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
        return identifierDomain;
    }
}
