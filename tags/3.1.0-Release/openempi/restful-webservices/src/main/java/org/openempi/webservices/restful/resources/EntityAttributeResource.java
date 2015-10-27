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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;

@Path("/{versionId}/entity-attributes")
public class EntityAttributeResource extends BaseResource
{
    @GET
    @Path("/{id}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EntityAttribute> loadEntityAttributesById(@PathParam("versionId") String versionId,
                                                          @PathParam("id") Integer entityId) {
        validateVersion(versionId);
        if (entityId == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        List<EntityAttribute> attributes = new ArrayList<EntityAttribute>();
        attributes.addAll(entity.getAttributes());
        return attributes;
    }
}
