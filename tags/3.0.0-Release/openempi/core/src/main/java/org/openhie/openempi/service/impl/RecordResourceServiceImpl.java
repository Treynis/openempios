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

import java.util.List;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.BadRequestException;
import org.openhie.openempi.ConflictException;
import org.openhie.openempi.NotFoundException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.service.IdentifierDomainService;
import org.openhie.openempi.service.RecordResourceService;
import org.openhie.openempi.util.ConvertUtil;

public class RecordResourceServiceImpl extends BaseServiceImpl implements RecordResourceService
{
    public Record loadRecordById(String versionId, Integer entityId, Long id) throws BadRequestException, NotFoundException {
        if (entityId == null || id == null) {
            throw new BadRequestException();
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new BadRequestException();
        }
        RecordQueryService queryService = Context.getRecordQueryService();
        Record record = queryService.loadRecordById(entity, id);
        if (record == null) {
            throw new NotFoundException();
        }
        return record;
    }

    public List<Record> findByAttributes(String versionId, Integer entityId, List<String> keyVal, Integer firstResult,
            Integer maxResults) throws BadRequestException, NotFoundException {
        if (entityId == null) {
            throw new BadRequestException();
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new BadRequestException();
        }
        // findRecordsByAttributes
        Record record = ConvertUtil.convertKeyValListToRecord(entity, keyVal);
        RecordQueryService queryService = Context.getRecordQueryService();
        List<Record> records = queryService.findRecordsByAttributes(entity, record, firstResult, maxResults);
        if (records == null || records.size() == 0) {
            throw new NotFoundException();
        }
        return records;
    }

    public String recordCountByAttributes(String versionId, Integer entityId, List<String> keyVal)
            throws BadRequestException {
        if (entityId == null) {
            throw new BadRequestException();
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new BadRequestException();
        }

        // getRecordCount
        Record record = ConvertUtil.convertKeyValListToRecord(entity, keyVal);
        RecordQueryService queryService = Context.getRecordQueryService();
        final Long count = queryService.getRecordCount(entity, record);
        return count.toString();
    }

    public List<Record> findByIdentifier(String versionId, Integer entityId, String identifierName,
            Integer identifierDomainId, Integer firstResult, Integer maxResults)
                    throws BadRequestException, NotFoundException {
        if (entityId == null) {
            throw new BadRequestException();
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new BadRequestException();
        }
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
                throw new BadRequestException();
            }
            identifier.setIdentifierDomain(identifierDomain);
        }

        // findRecordsByIdentifier
        List<Record> records = queryService.findRecordsByIdentifier(entity, identifier, firstResult, maxResults);
        if (records == null || records.size() == 0) {
            throw new NotFoundException();
        }
        return records;
    }

    public String recordCountByIdentifier(String versionId, Integer entityId, String identifierName,
            Integer identifierDomainId) throws BadRequestException {
        if (entityId == null) {
            throw new BadRequestException();
        }
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        Entity entity = defService.loadEntity(entityId);
        if (entity == null) {
            throw new BadRequestException();
        }
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
                return "0";
            }
            identifier.setIdentifierDomain(identifierDomain);
        }
        // getRecordCount
        Long count = queryService.getRecordCount(entity, identifier);
        return count.toString();
    }

    public List<Record> loadRecordByIds(String versionId, Integer entityId, List<Long> recordIds)
            throws BadRequestException, NotFoundException {
        if (entityId == null || recordIds == null) {
            throw new BadRequestException();
        }
        Entity entity = Context.getEntityDefinitionManagerService().loadEntity(entityId);
        if (entity == null) {
            throw new BadRequestException();
        }
        List<Record> records = Context.getRecordQueryService().loadRecordsById(entity, recordIds);
        if (records == null || records.size() == 0) {
            throw new NotFoundException();
        }
        return records;
    }

    public Record addRecord(String versionId, Record record) throws ConflictException {
        Entity entity = Context.getEntityDefinitionManagerService().loadEntity(record.getEntity().getEntityVersionId());
        try {
            record = Context.getRecordManagerService().addRecord(entity, record);
            return record;
        } catch (ApplicationException e) {
            throw new ConflictException();
        }
    }

    public Record updateRecord(String versionId, Record record) throws ConflictException {
        Entity entity = Context.getEntityDefinitionManagerService().loadEntity(record.getEntity().getEntityVersionId());
        try {
            record =  Context.getRecordManagerService().updateRecord(entity, record);
            return record;
        } catch (ApplicationException e) {
            log.error("Failed while processing update request: " + e, e);
            throw new ConflictException();
        }
    }

    public void deleteRecord(String versionId, Integer entityId, Long recordId, Boolean removeOption)
            throws ConflictException, NotFoundException {
        Entity entity = Context.getEntityDefinitionManagerService().loadEntity(entityId);
        Record record = Context.getRecordQueryService().loadRecordById(entity, recordId);
        if (record == null) {
            throw new NotFoundException();
        }

        try {
            if (removeOption) {
                Context.getRecordManagerService().removeRecord(entity, record);
            } else {
                Context.getRecordManagerService().deleteRecord(entity, record);
            }
        } catch (ApplicationException e) {
            throw new ConflictException();
        }
    }
}
