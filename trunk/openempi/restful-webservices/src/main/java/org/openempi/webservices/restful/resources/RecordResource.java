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
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.service.IdentifierDomainService;

@Path("/{versionId}/records")
public class RecordResource extends BaseResource
{
    @GET
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public org.openempi.webservices.restful.model.Record loadRecordById(@PathParam("versionId") String versionId,
                                                                        @QueryParam("entityId") Integer entityId,
                                                                        @PathParam("id") Long id) {
        validateVersion(versionId);
        if (entityId == null || id == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        RecordQueryService queryService = Context.getRecordQueryService();
        Record record = queryService.loadRecordById(entity, id);
        if (record == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return Converter.convertRecordToRestfulRecord(record);
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
        if (entityId == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        // findRecordsByAttributes
        Record record = Converter.convertKeyValListToRecord(entity, keyVal);
        RecordQueryService queryService = Context.getRecordQueryService();
        List<Record> records = queryService.findRecordsByAttributes(entity, record, firstResult, maxResults);
        if (records == null || records.size() == 0) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // convert to list of restful model Record
        List<org.openempi.webservices.restful.model.Record> restRecords = new ArrayList<org.openempi.webservices.restful.model.Record>();
        for (Record rec : records) {
            restRecords.add(Converter.convertRecordToRestfulRecord(rec));
        }
        return restRecords;
    }

    @GET
    @Path("/recordCountByAttributes")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    //@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    @Produces({ MediaType.TEXT_PLAIN })
    public String recordCountByAttributes(@PathParam("versionId") String versionId,
                                          @QueryParam("entityId") Integer entityId,
                                          @QueryParam("keyVal") List<String> keyVal) {
        validateVersion(versionId);
        if (entityId == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        // getRecordCount
        Record record = Converter.convertKeyValListToRecord(entity, keyVal);
        RecordQueryService queryService = Context.getRecordQueryService();
        final Long count = queryService.getRecordCount(entity, record);
        return count.toString();

        // if @Produces MediaType is not TEXT_PLAIN
        // return "<Count>"+count.toString()+"</Count>";
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
        if (entityId == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }


        List<org.openempi.webservices.restful.model.Record> restRecords = new ArrayList<org.openempi.webservices.restful.model.Record>();

        // identifier and identifier domain
        RecordQueryService queryService = Context.getRecordQueryService();
        IdentifierDomainService domainService = Context.getIdentifierDomainService();
        Identifier identifier = new Identifier();
        identifier.setIdentifier(identifierName);
        IdentifierDomain identifierDomain = null;
        if (identifierDomainId != null) {
            identifierDomain = domainService.findIdentifierDomainById(identifierDomainId);
            if (identifierDomain == null) {
                // cannot find the identifier domain by id
                // return restRecords;
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            identifier.setIdentifierDomain(identifierDomain);
        }

        // findRecordsByIdentifier
        List<Record> records = queryService.findRecordsByIdentifier(entity, identifier, firstResult, maxResults);
        if (records == null || records.size() == 0) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // convert to list of restful model Record
        for (Record rec : records) {
            restRecords.add(Converter.convertRecordToRestfulRecord(rec));
        }
        return restRecords;
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
        if (entityId == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        Long count = new Long(0);

        // identifier and identifier domain
        RecordQueryService queryService = Context.getRecordQueryService();
        IdentifierDomainService domainService = Context.getIdentifierDomainService();
        Identifier identifier = new Identifier();
        identifier.setIdentifier(identifierName);
        IdentifierDomain identifierDomain = null;
        if (identifierDomainId != null) {
            identifierDomain = domainService.findIdentifierDomainById(identifierDomainId);
            if (identifierDomain == null) {
                // cannot find the identifier domain by id
                return count.toString();
            }
            identifier.setIdentifierDomain(identifierDomain);
        }

        // getRecordCount
        count = queryService.getRecordCount(entity, identifier);
        return count.toString();

        // if @Produces MediaType is not TEXT_PLAIN
        // return "<Count>"+count.toString()+"</Count>";
    }

    @GET
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<org.openempi.webservices.restful.model.Record> loadRecordByIds(@PathParam("versionId") String versionId,
                                                 @QueryParam("entityId") Integer entityId,
                                                 @QueryParam("recordId") List<Long> recordIds) {
        validateVersion(versionId);
        if (entityId == null || recordIds == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        RecordQueryService queryService = Context.getRecordQueryService();
        List<Record> records = queryService.loadRecordsById(entity, recordIds);
        if (records == null || records.size() == 0) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // convert to list of restful model Record
        List<org.openempi.webservices.restful.model.Record> restRecords = new ArrayList<org.openempi.webservices.restful.model.Record>();
        for (Record rec : records) {
            restRecords.add(Converter.convertRecordToRestfulRecord(rec));
        }
        return restRecords;
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

        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(restRec.getEntityId());
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        RecordManagerService managerService = Context.getRecordManagerService();
        try {
             Record record =  managerService.addRecord(entity, Converter.convertRestfulRecordToRecord(entity, restRec));
             return Converter.convertRecordToRestfulRecord(record);
        } catch (ApplicationException e) {
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

        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(restRec.getEntityId());
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        RecordManagerService managerService = Context.getRecordManagerService();
        try {
             Record record =  managerService.updateRecord(entity, Converter.convertRestfulRecordToRecord(entity, restRec));
             return Converter.convertRecordToRestfulRecord(record);
        } catch (ApplicationException e) {
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

        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        RecordManagerService managerService = Context.getRecordManagerService();
        try {
            Record record = Context.getRecordQueryService().loadRecordById(entity, recordId);
            if (record == null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            if (removeOption) {
                managerService.removeRecord(entity, record);
            } else {
                managerService.deleteRecord(entity, record);
            }

        } catch (ApplicationException e) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }
}
