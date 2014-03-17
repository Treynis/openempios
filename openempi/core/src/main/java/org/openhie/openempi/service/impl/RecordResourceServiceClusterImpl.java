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

import java.io.Serializable;
import java.util.List;

import org.openhie.openempi.BadRequestException;
import org.openhie.openempi.ConflictException;
import org.openhie.openempi.NotFoundException;
import org.openhie.openempi.cluster.CommandRequest;
import org.openhie.openempi.cluster.ServiceName;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.service.RecordResourceService;

public class RecordResourceServiceClusterImpl extends BaseServiceImpl implements RecordResourceService
{
    public Record loadRecordById(String versionId, Integer entityId, Long id) throws BadRequestException, NotFoundException {
        if (entityId == null || id == null) {
            throw new BadRequestException();
        }
        CommandRequest request = new CommandRequest(ServiceName.RECORD_RESOURCE_SERVICE, "loadRecordById", true,
                versionId, entityId, id);
        request = Context.getClusterManager().executeRemoteRequest(request);
        if (request.isHasFailed()) {
            if (request.getException() instanceof BadRequestException) {
                throw (BadRequestException) request.getException();
            } else if (request.getException() instanceof NotFoundException) {
                throw (NotFoundException) request.getException();                
            }
            throw new RuntimeException(request.getException().getMessage());
        }
        Record record = (Record) request.getResponse();
        return record;
    }

    public List<Record> findByAttributes(String versionId, Integer entityId, List<String> keyVal, Integer firstResult,
            Integer maxResults) throws BadRequestException, NotFoundException {
        if (entityId == null) {
            throw new BadRequestException();
        }
        CommandRequest request = new CommandRequest(ServiceName.RECORD_RESOURCE_SERVICE, "findByAttributes", true,
                versionId, entityId, (Serializable) keyVal, firstResult, maxResults);
        request = Context.getClusterManager().executeRemoteRequest(request);
        if (request.isHasFailed()) {
            if (request.getException() instanceof BadRequestException) {
                throw (BadRequestException) request.getException();
            } else if (request.getException() instanceof NotFoundException) {
                throw (NotFoundException) request.getException();                
            }
            throw new RuntimeException(request.getException().getMessage());
        }
        @SuppressWarnings("unchecked")
        List<Record> records = (List<Record>) request.getResponse();
        return records;
    }

    public String recordCountByAttributes(String versionId, Integer entityId, List<String> keyVal)
            throws BadRequestException {
        if (entityId == null) {
            throw new BadRequestException();
        }
        CommandRequest request = new CommandRequest(ServiceName.RECORD_RESOURCE_SERVICE, "recordCountByAttributes", true,
                versionId, entityId, (Serializable) keyVal);
        request = Context.getClusterManager().executeRemoteRequest(request);
        if (request.isHasFailed()) {
            if (request.getException() instanceof BadRequestException) {
                throw (BadRequestException) request.getException();
            }
            throw new RuntimeException(request.getException().getMessage());
        }
        String count = (String) request.getResponse();
        return count;
    }

    public List<Record> findByIdentifier(String versionId, Integer entityId, String identifierName,
            Integer identifierDomainId, Integer firstResult, Integer maxResults)
                    throws BadRequestException, NotFoundException {
        if (entityId == null) {
            throw new BadRequestException();
        }
        CommandRequest request = new CommandRequest(ServiceName.RECORD_RESOURCE_SERVICE, "findByIdentifier", true,
                versionId, entityId, identifierName, identifierDomainId, firstResult, maxResults);
        request = Context.getClusterManager().executeRemoteRequest(request);
        if (request.isHasFailed()) {
            if (request.getException() instanceof BadRequestException) {
                throw (BadRequestException) request.getException();
            } else if (request.getException() instanceof NotFoundException) {
                throw (NotFoundException) request.getException();                
            }
            throw new RuntimeException(request.getException().getMessage());
        }
        @SuppressWarnings("unchecked")
        List<Record> records = (List<Record>) request.getResponse();
        return records;
    }

    public String recordCountByIdentifier(String versionId, Integer entityId, String identifierName,
            Integer identifierDomainId) throws BadRequestException {
        if (entityId == null) {
            throw new BadRequestException();
        }
        CommandRequest request = new CommandRequest(ServiceName.RECORD_RESOURCE_SERVICE, "recordCountByIdentifier", true,
                versionId, entityId, identifierName, identifierDomainId);
        request = Context.getClusterManager().executeRemoteRequest(request);
        if (request.isHasFailed()) {
            if (request.getException() instanceof BadRequestException) {
                throw (BadRequestException) request.getException();
            }
            throw new RuntimeException(request.getException().getMessage());
        }
        String count = (String) request.getResponse();
        return count.toString();
    }

    public List<Record> loadRecordByIds(String versionId, Integer entityId, List<Long> recordIds)
            throws BadRequestException, NotFoundException {
        if (entityId == null || recordIds == null) {
            throw new BadRequestException();
        }
        CommandRequest request = new CommandRequest(ServiceName.RECORD_RESOURCE_SERVICE, "loadRecordByIds", true,
                versionId, entityId, (Serializable) recordIds);
        request = Context.getClusterManager().executeRemoteRequest(request);
        if (request.isHasFailed()) {
            if (request.getException() instanceof BadRequestException) {
                throw (BadRequestException) request.getException();
            } else if (request.getException() instanceof NotFoundException) {
                throw (NotFoundException) request.getException();                
            }
            throw new RuntimeException(request.getException().getMessage());
        }
        @SuppressWarnings("unchecked")
        List<Record> records = (List<Record>) request.getResponse();
        return records;
    }

    public Record addRecord(String versionId, Record record) throws ConflictException {
        CommandRequest request = new CommandRequest(ServiceName.RECORD_RESOURCE_SERVICE, "addRecord", true,
                versionId, record);
        request = Context.getClusterManager().executeRemoteRequest(request);
        if (request.isHasFailed()) {
            if (request.getException() instanceof ConflictException) {
                throw (ConflictException) request.getException();
            }
            throw new RuntimeException(request.getException().getMessage());
        }
        record = (Record) request.getResponse();
        return record;
    }

    public Record updateRecord(String versionId, Record record) throws ConflictException {
        CommandRequest request = new CommandRequest(ServiceName.RECORD_RESOURCE_SERVICE, "updateRecord", true,
                versionId, record);
        request = Context.getClusterManager().executeRemoteRequest(request);
        if (request.isHasFailed()) {
            if (request.getException() instanceof ConflictException) {
                throw (ConflictException) request.getException();
            }
            throw new RuntimeException(request.getException().getMessage());
        }
        record = (Record) request.getResponse();
        return record;
    }

    public void deleteRecord(String versionId, Integer entityId, Long recordId, Boolean removeOption)
            throws ConflictException, NotFoundException {
        CommandRequest request = new CommandRequest(ServiceName.RECORD_RESOURCE_SERVICE, "deleteRecord", false,
                versionId, entityId, recordId, removeOption);
        request = Context.getClusterManager().executeRemoteRequest(request);
        if (request.isHasFailed()) {
            if (request.getException() instanceof ConflictException) {
                throw (ConflictException) request.getException();
            } else if (request.getException() instanceof NotFoundException) {
                throw (NotFoundException) request.getException();                
            }
            throw new RuntimeException(request.getException().getMessage());
        }
    }
}
