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
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.IdentifierUpdateEvent;
import org.openhie.openempi.service.IdentifierDomainService;

@Path("/{versionId}/notifications")
public class NotificationResource extends BaseResource
{
    @GET
    @Path("/getNotificationCount")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public String getNotificationCount(@PathParam("versionId") String versionId) {
        validateVersion(versionId);

        RecordQueryService queryService = Context.getRecordQueryService();

        Integer count = queryService.getNotificationCount(Context.getUserContext().getUser());

        return Integer.toString(count);
    }

    @GET
    // @Path("/retrieveNotifications")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<IdentifierUpdateEvent> retrieveNotifications(
            @PathParam("versionId") String versionId,
            @DefaultValue("0") @QueryParam("firstRecord") Integer firstRecord,
            @DefaultValue("0") @QueryParam("maxRecords") Integer maxRecords,
            @DefaultValue("false") @QueryParam("removeRecords") Boolean removeRecords) {
        validateVersion(versionId);

        RecordQueryService queryService = Context.getRecordQueryService();
        if (firstRecord == 0 && maxRecords == 0) {
            return queryService.retrieveNotifications(removeRecords, Context.getUserContext().getUser());
        } else {
            return queryService.retrieveNotifications(firstRecord, maxRecords, removeRecords, Context.getUserContext()
                    .getUser());
        }
    }

    @GET
    @Path("/retrieveNotificationsByDate")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<IdentifierUpdateEvent> retrieveNotificationsByDate(
            @PathParam("versionId") String versionId,
            @QueryParam("date") String date,
            @DefaultValue("false") @QueryParam("removeRecords") Boolean removeRecords) {
        validateVersion(versionId);

        Date dateObj;

        try {
            Calendar cal = javax.xml.bind.DatatypeConverter.parseDateTime(date);
            dateObj = cal.getTime();

        } catch (java.lang.IllegalArgumentException e) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        RecordQueryService queryService = Context.getRecordQueryService();
        return queryService.retrieveNotificationsByDate(dateObj, removeRecords, Context.getUserContext().getUser());
    }

    @DELETE
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void deleteNotification(
            @PathParam("versionId") String versionId,
            @PathParam("id") Long notificationId) {
        validateVersion(versionId);
        if (notificationId == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        IdentifierDomainService domainService = Context.getIdentifierDomainService();
        try {
            IdentifierUpdateEvent notification = domainService.findIdentifierUpdateEvent(notificationId);
            if (notification == null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            domainService.removeIdentifierUpdateEvent(notification);

        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }
}
