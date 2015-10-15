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
import org.openhie.openempi.model.Record;
import org.openhie.openempi.service.RecordResourceService;
import org.openhie.openempi.service.ResourceServiceFactory;

@Path("/{versionId}/records")
public class RecordResource extends BaseResource
{
    private RecordResourceService recordService;
    
    public RecordResource() {
        recordService = (RecordResourceService)
                ResourceServiceFactory.createResourceService(ServiceName.RECORD_RESOURCE_SERVICE,
                        RecordResourceService.class);
    }
    
    @GET
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public org.openempi.webservices.restful.model.Record loadRecordById(@PathParam("versionId") String versionId,
                                                                        @QueryParam("entityId") Integer entityId,
                                                                        @PathParam("id") Long id) {
        validateVersion(versionId);
        try {
            Record record = recordService.loadRecordById(versionId, entityId, id);
            return Converter.convertRecordToRestfulRecord(record);
        } catch (BadRequestException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (NotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Path("/findByAttributes")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<org.openempi.webservices.restful.model.Record> findByAttributes(@PathParam("versionId") String versionId,
                                                 @QueryParam("entityId") Integer entityId,
                                                 @QueryParam("keyVal") List<String> keyVal,
                                                 @DefaultValue("0") @QueryParam("firstResult") Integer firstResult,
                                                 @DefaultValue("10") @QueryParam("maxResults") Integer maxResults) {
        validateVersion(versionId);
        try {
            List<Record> records = recordService.findByAttributes(versionId, entityId, keyVal, firstResult, maxResults);
            // convert to list of restful model Record
            
            List<org.openempi.webservices.restful.model.Record> restRecords = new ArrayList<org.openempi.webservices.restful.model.Record>();
            for (Record rec : records) {
                restRecords.add(Converter.convertRecordToRestfulRecord(rec));
            }
            return restRecords;
        } catch (BadRequestException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (NotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Path("/recordCountByAttributes")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.TEXT_PLAIN })
    public String recordCountByAttributes(@PathParam("versionId") String versionId,
                                          @QueryParam("entityId") Integer entityId,
                                          @QueryParam("keyVal") List<String> keyVal) {
        validateVersion(versionId);
        try {
            String count = recordService.recordCountByAttributes(versionId, entityId, keyVal);
            return count;
        } catch (BadRequestException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("/findByIdentifier")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<org.openempi.webservices.restful.model.Record> findByIdentifier(@PathParam("versionId") String versionId,
                                                 @QueryParam("entityId") Integer entityId,
                                                 @QueryParam("identifier") String identifierName,
                                                 @QueryParam("identifierDomainId") Integer identifierDomainId,
                                                 @DefaultValue("0") @QueryParam("firstResult") Integer firstResult,
                                                 @DefaultValue("10") @QueryParam("maxResults") Integer maxResults) {
        validateVersion(versionId);
        try {
            List<Record> records = recordService.findByIdentifier(versionId, entityId, identifierName, 
                    identifierDomainId, firstResult, maxResults);
            // convert to list of restful model Record
            
            List<org.openempi.webservices.restful.model.Record> restRecords = new ArrayList<org.openempi.webservices.restful.model.Record>();
            for (Record rec : records) {
                restRecords.add(Converter.convertRecordToRestfulRecord(rec));
            }
            return restRecords;
        } catch (BadRequestException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (NotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Path("/recordCountByIdentifier")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    //@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    @Produces({ MediaType.TEXT_PLAIN })
    public String recordCountByIdentifier(@PathParam("versionId") String versionId,
                                          @QueryParam("entityId") Integer entityId,
                                          @QueryParam("identifier") String identifierName,
                                          @QueryParam("identifierDomainId") Integer identifierDomainId) {
        validateVersion(versionId);
        try {
            String count = recordService.recordCountByIdentifier(versionId, entityId, identifierName,
                    identifierDomainId);
            return count;
        } catch (BadRequestException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<org.openempi.webservices.restful.model.Record> loadRecordByIds(@PathParam("versionId") String versionId,
                                                 @QueryParam("entityId") Integer entityId,
                                                 @QueryParam("recordId") List<Long> recordIds) {
        validateVersion(versionId);

        try {
            List<Record> records = recordService.loadRecordByIds(versionId, entityId, recordIds);
            List<org.openempi.webservices.restful.model.Record> restRecords = new ArrayList<org.openempi.webservices.restful.model.Record>();
            for (Record rec : records) {
                restRecords.add(Converter.convertRecordToRestfulRecord(rec));
            }
            return restRecords;
        } catch (BadRequestException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (NotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public org.openempi.webservices.restful.model.Record addRecord(@PathParam("versionId") String versionId,
                                                                   org.openempi.webservices.restful.model.Record restRec) {
        validateVersion(versionId);        
        if (restRec == null || restRec.getEntityId() == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        try {
            Entity entity = Context.getEntityDefinitionManagerService().loadEntity(restRec.getEntityId());
            if (entity == null) {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            Record record =  recordService.addRecord(versionId, Converter.convertRestfulRecordToRecord(entity, restRec));
            return Converter.convertRecordToRestfulRecord(record);
        } catch (ConflictException e) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public org.openempi.webservices.restful.model.Record updateRecord(@PathParam("versionId") String versionId,
                                                                      org.openempi.webservices.restful.model.Record restRec) {
        validateVersion(versionId);
        if (restRec == null || restRec.getEntityId() == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        Entity entity = Context.getEntityDefinitionManagerService().loadEntity(restRec.getEntityId());
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            Record record =  recordService.updateRecord(versionId, Converter.convertRestfulRecordToRecord(entity, restRec));
            return Converter.convertRecordToRestfulRecord(record);
        } catch (ConflictException e) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    @DELETE
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void deleteRecord(@PathParam("versionId") String versionId,
                             @QueryParam("entityId") Integer entityId,
                             @PathParam("id") Long recordId,
                             @DefaultValue("false") @QueryParam("remove") Boolean removeOption) {
        validateVersion(versionId);
        if (entityId == null || recordId == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        Entity entity = Context.getEntityDefinitionManagerService().loadEntity(entityId);
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            recordService.deleteRecord(versionId, entityId, recordId, removeOption);
        } catch (NotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } catch (ConflictException e) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }
}
