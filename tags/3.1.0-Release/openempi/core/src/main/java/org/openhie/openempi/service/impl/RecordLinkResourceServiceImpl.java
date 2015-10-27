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
package org.openhie.openempi.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.BadRequestException;
import org.openhie.openempi.ConflictException;
import org.openhie.openempi.NotFoundException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.service.RecordLinkResourceService;

public class RecordLinkResourceServiceImpl extends BaseServiceImpl implements RecordLinkResourceService
{
    public List<RecordLink> getRecordLinks(String versionId, Integer entityId, String linkState,
            Integer firstResult, Integer maxResults) throws BadRequestException, NotFoundException {
        if (entityId == null) {
            throw new BadRequestException();
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new BadRequestException();
        }

        RecordQueryService queryService = Context.getRecordQueryService();
        List<RecordLink> recordLinks = queryService.loadRecordLinks(entity, RecordLinkState.fromString((linkState)),
                firstResult, maxResults);
        if (recordLinks == null || recordLinks.size() == 0) {
            throw new NotFoundException();
        }
        List<RecordLink> populatedRecordLinks = new ArrayList<RecordLink>(recordLinks.size());
        for (RecordLink link : recordLinks) {
            populatedRecordLinks.add(queryService.loadRecordLink(entity, link.getRecordLinkId()));
        }
        return populatedRecordLinks;
    }

    public RecordLink loadByRecordLinkId(String versionId, Integer entityId, String recordLinkId)
            throws BadRequestException, NotFoundException {
        if (entityId == null || recordLinkId == null) {
            throw new BadRequestException();
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new BadRequestException();
        }
        RecordQueryService queryService = Context.getRecordQueryService();
        RecordLink recordLink = queryService.loadRecordLink(entity, recordLinkId);
        if (recordLink == null) {
            throw new NotFoundException();
        }
        return recordLink;
    }

    public List<RecordLink> loadByRecordId(String versionId, Integer entityId, Long recordId)
            throws BadRequestException, NotFoundException {
        if (entityId == null || recordId == null) {
            throw new BadRequestException();
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new BadRequestException();
        }
        RecordQueryService queryService = Context.getRecordQueryService();
        List<RecordLink> recordLinks = queryService.loadRecordLinks(entity, recordId);
        if (recordLinks == null || recordLinks.size() == 0) {
            throw new NotFoundException();
        }
        List<RecordLink> populatedRecordLinks = new ArrayList<RecordLink>(recordLinks.size());
        for (RecordLink link : recordLinks) {
        	populatedRecordLinks.add(queryService.loadRecordLink(entity, link.getRecordLinkId()));
        }
        return populatedRecordLinks;
    }

    public RecordLink addRecordLink(String versionId, Integer entityId, RecordLink recordLink)
            throws BadRequestException, ConflictException {
        RecordManagerService managerService = Context.getRecordManagerService();
        if (recordLink == null || entityId == null) {
            throw new BadRequestException();
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new BadRequestException();
        }

        try {
            LinkSource linkSource = new LinkSource();
            linkSource.setLinkSourceId(LinkSource.MANUAL_MATCHING_SOURCE);
            recordLink.setLinkSource(linkSource);
            recordLink.setUserCreatedBy(Context.getUserContext().getUser());
            recordLink = managerService.addRecordLink(recordLink);
            return recordLink;
       } catch (ApplicationException e) {
           throw new ConflictException();
       }
    }

    public RecordLink updateRecordLink(String versionId, Integer entityId, RecordLink recordLink)
            throws BadRequestException, ConflictException {
        RecordManagerService managerService = Context.getRecordManagerService();
        if (recordLink == null || entityId == null) {
            throw new BadRequestException();
        }

        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new BadRequestException();
        }

        try {
            RecordLink recordLinkFound = Context.getRecordQueryService()
                    .loadRecordLink(entity, recordLink.getRecordLinkId());
            if (recordLinkFound == null) {
                throw new NotFoundException();
            }
            recordLinkFound.setState(RecordLinkState.fromString(recordLink.getState().getState()));
            recordLinkFound.setWeight(recordLink.getWeight());
            recordLink =  managerService.updateRecordLink(recordLinkFound);
            return recordLink;
       } catch (ApplicationException e) {
           throw new ConflictException();
       }
    }

    public void removeRecordLink(String versionId, Integer entityId, Long recordLinkId)
            throws BadRequestException, ConflictException {
        RecordManagerService managerService = Context.getRecordManagerService();
        if (entityId == null || recordLinkId == null) {
            throw new BadRequestException();
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new BadRequestException();
        }

        try {
            RecordLink recordLink = Context.getRecordQueryService().loadRecordLink(entity,
                    recordLinkId.toString());
            if (recordLink == null) {
                throw new NotFoundException();
            }
            managerService.removeRecordLink(recordLink);
        } catch (ApplicationException e) {
            throw new ConflictException();
        }
    }
}
