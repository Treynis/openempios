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
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openempi.webservices.restful.util.Converter;
import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;

@Path("{versionId}/record-links")
public class RecordLinkResource extends BaseResource
{
    @GET
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<org.openempi.webservices.restful.model.RecordLink> getRecordLinks(
                                           @PathParam("versionId") String versionId,
                                           @QueryParam("entityId") Integer entityId,
                                           @DefaultValue("M") @QueryParam("linkState") String linkState,
                                           @DefaultValue("0") @QueryParam("firstResult") Integer firstResult,
                                           @DefaultValue("10") @QueryParam("maxResults") Integer maxResults) {
        validateVersion(versionId);
        if (entityId == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        RecordQueryService queryService = Context.getRecordQueryService();
        List<RecordLink> recordLinks = queryService.loadRecordLinks(entity, RecordLinkState.fromString((linkState)), firstResult, maxResults);
        if (recordLinks == null || recordLinks.size() == 0) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // convert to list of restful model Record
        List<org.openempi.webservices.restful.model.RecordLink> restRecordLinks = new ArrayList<org.openempi.webservices.restful.model.RecordLink>();
        for (RecordLink recLink : recordLinks) {

            // load info for left and right records
            RecordLink link = queryService.loadRecordLink(entity, recLink.getRecordLinkId());

            restRecordLinks.add(Converter.convertRecordToRestfulRecordLink(link));
        }
        return restRecordLinks;
    }

    @GET
    @Path("{recordLinkId}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public org.openempi.webservices.restful.model.RecordLink loadByRecordLinkId(
                                         @PathParam("versionId") String versionId,
                                         @QueryParam("entityId") Integer entityId,
                                         @PathParam("recordLinkId") String recordLinkId) {
        validateVersion(versionId);
        if (entityId == null || recordLinkId == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        RecordQueryService queryService = Context.getRecordQueryService();
        RecordLink recordLink = queryService.loadRecordLink(entity, recordLinkId);
        if (recordLink == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return Converter.convertRecordToRestfulRecordLink(recordLink);
    }

    @GET
    @Path("/loadByRecordId")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<org.openempi.webservices.restful.model.RecordLink> loadByRecordId(
                                          @PathParam("versionId") String versionId,
                                          @QueryParam("entityId") Integer entityId,
                                          @QueryParam("recordId") Long recordId) {
        validateVersion(versionId);
        if (entityId == null || recordId == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        RecordQueryService queryService = Context.getRecordQueryService();
        List<RecordLink> recordLinks = queryService.loadRecordLinks(entity, recordId);
        if (recordLinks == null || recordLinks.size() == 0) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // convert to list of restful model Record
        List<org.openempi.webservices.restful.model.RecordLink> restRecordLinks = new ArrayList<org.openempi.webservices.restful.model.RecordLink>();
        for (RecordLink recLink : recordLinks) {

            // load info for left and right records
            RecordLink link = queryService.loadRecordLink(entity, recLink.getRecordLinkId());

            restRecordLinks.add(Converter.convertRecordToRestfulRecordLink(link));
        }
        return restRecordLinks;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public org.openempi.webservices.restful.model.RecordLink addRecordLink(
                                                    @PathParam("versionId") String versionId,
                                                    org.openempi.webservices.restful.model.RecordLink restRecordLink) {
        validateVersion(versionId);
        RecordManagerService managerService = Context.getRecordManagerService();
        if (restRecordLink == null || restRecordLink.getEntityId() == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(restRecordLink.getEntityId());
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            RecordLink recordLink =  Converter.convertRestfulRecordLinkToRecordLink(entity, restRecordLink);

            LinkSource linkSource = new LinkSource();
            linkSource.setLinkSourceId(LinkSource.MANUAL_MATCHING_SOURCE);
            recordLink.setLinkSource(linkSource);
            recordLink.setUserCreatedBy(Context.getUserContext().getUser());

            recordLink = managerService.addRecordLink(recordLink);

            return Converter.convertRecordToRestfulRecordLink(recordLink);
       } catch (ApplicationException e) {
           throw new WebApplicationException(Response.Status.CONFLICT);
       }
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public org.openempi.webservices.restful.model.RecordLink updateRecordLink(
                                        @PathParam("versionId") String versionId,
                                        org.openempi.webservices.restful.model.RecordLink restRecordLink) {
        validateVersion(versionId);
        RecordManagerService managerService = Context.getRecordManagerService();
        if (restRecordLink == null || restRecordLink.getEntityId() == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(restRecordLink.getEntityId());
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            RecordLink recordLink = Context.getRecordQueryService()
                    .loadRecordLink(entity, restRecordLink.getRecordLinkId().toString());
            if (recordLink == null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            recordLink.setState(RecordLinkState.fromString(restRecordLink.getState()));
            recordLink.setWeight(restRecordLink.getWeight());

            recordLink =  managerService.updateRecordLink(recordLink);
            return Converter.convertRecordToRestfulRecordLink(recordLink);
       } catch (ApplicationException e) {
           throw new WebApplicationException(Response.Status.CONFLICT);
       }
    }

    @DELETE
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void removeRecordLink(
                                 @PathParam("versionId") String versionId,
                                 @QueryParam("entityId") Integer entityId,
                                 @PathParam("id") Long recordLinkId) {
        validateVersion(versionId);
        RecordManagerService managerService = Context.getRecordManagerService();
        if (entityId == null || recordLinkId == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            RecordLink recordLink = Context.getRecordQueryService()
                    .loadRecordLink(entity, recordLinkId.toString());
            if (recordLink == null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            managerService.removeRecordLink(recordLink);
        } catch (ApplicationException e) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }
}
