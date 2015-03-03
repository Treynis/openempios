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
import org.openhie.openempi.BadRequestException;
import org.openhie.openempi.ConflictException;
import org.openhie.openempi.NotFoundException;
import org.openhie.openempi.cluster.ServiceName;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.service.RecordLinkResourceService;
import org.openhie.openempi.service.ResourceServiceFactory;

@Path("{versionId}/record-links")
public class RecordLinkResource extends BaseResource
{
    private RecordLinkResourceService recordLinkService;
    
    public RecordLinkResource() {
        recordLinkService = (RecordLinkResourceService)
                ResourceServiceFactory.createResourceService(ServiceName.RECORD_LINK_RESOURCE_SERVICE,
                        RecordLinkResourceService.class);
    }
    
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
        try {
            List<RecordLink> recordLinks = recordLinkService.getRecordLinks(versionId, entityId, linkState,
                    firstResult, maxResults);
            List<org.openempi.webservices.restful.model.RecordLink> restRecordLinks = new
                    ArrayList<org.openempi.webservices.restful.model.RecordLink>();
            for (RecordLink recLink : recordLinks) {
                restRecordLinks.add(Converter.convertRecordToRestfulRecordLink(recLink));
            }
            return restRecordLinks;
        } catch (BadRequestException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (NotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }        
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
        try {
            RecordLink recordLink = recordLinkService.loadByRecordLinkId(versionId, entityId, recordLinkId);
            return Converter.convertRecordToRestfulRecordLink(recordLink);
        } catch (BadRequestException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (NotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }        
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
        try {
            List<RecordLink> recordLinks = recordLinkService.loadByRecordId(versionId, entityId, recordId);
            List<org.openempi.webservices.restful.model.RecordLink> restRecordLinks = new
                    ArrayList<org.openempi.webservices.restful.model.RecordLink>();
            for (RecordLink recLink : recordLinks) {
                restRecordLinks.add(Converter.convertRecordToRestfulRecordLink(recLink));
            }
            return restRecordLinks;
        } catch (BadRequestException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (NotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }        
    }

    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public org.openempi.webservices.restful.model.RecordLink addRecordLink(
                                                    @PathParam("versionId") String versionId,
                                                    org.openempi.webservices.restful.model.RecordLink restRecordLink) {
        validateVersion(versionId);
        if (restRecordLink.getEntityId() == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        Entity entity = Context.getEntityDefinitionManagerService().loadEntity(restRecordLink.getEntityId());
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        try {
            RecordLink recordLink = recordLinkService.addRecordLink(versionId, entity.getEntityId(),
                    Converter.convertRestfulRecordLinkToRecordLink(entity, restRecordLink));
            return Converter.convertRecordToRestfulRecordLink(recordLink);
       } catch (BadRequestException e) {
           throw new WebApplicationException(Response.Status.BAD_REQUEST);
       } catch (ConflictException e) {
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
        if (restRecordLink == null || restRecordLink.getEntityId() == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        Entity entity = Context.getEntityDefinitionManagerService().loadEntity(restRecordLink.getEntityId());
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        try {
            RecordLink recordLink = recordLinkService.updateRecordLink(versionId, entity.getEntityId(),
                    Converter.convertRestfulRecordLinkToRecordLink(entity, restRecordLink));
            return Converter.convertRecordToRestfulRecordLink(recordLink);
       } catch (BadRequestException e) {
           throw new WebApplicationException(Response.Status.BAD_REQUEST);
       } catch (ConflictException e) {
           throw new WebApplicationException(Response.Status.CONFLICT);
       } catch (NotFoundException e) {
           throw new WebApplicationException(Response.Status.NOT_FOUND);
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
        try {
            recordLinkService.removeRecordLink(versionId, entityId, recordLinkId);
        } catch (BadRequestException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (ConflictException e) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        } catch (NotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }
}
