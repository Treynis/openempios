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
package org.openhie.openempi.service;

import java.util.List;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.AuthorizationException;
import org.openhie.openempi.BadRequestException;
import org.openhie.openempi.ConflictException;
import org.openhie.openempi.NotFoundException;
import org.openhie.openempi.model.Record;

public interface RecordResourceService
{
    public Record addRecord(String versionId, Record record) throws ConflictException;

    public void deleteRecord(String versionId, Integer entityId, Long recordId, Boolean removeOption)
            throws ConflictException, NotFoundException;

    public List<Record> findByAttributes(String versionId, Integer entityId, List<String> keyVal, Integer firstResult,
            Integer maxResults) throws BadRequestException, NotFoundException;

    public List<Record> findByBlocking(String versionId, Integer entityId, List<String> keyVal)
            throws BadRequestException, NotFoundException;

    public List<Record> findByIdentifier(String versionId, Integer entityId, String identifierName,
            Integer identifierDomainId, Integer firstResult, Integer maxResults) throws BadRequestException,
            NotFoundException;

    public Record loadRecordById(String versionId, Integer entityId, Long id) throws BadRequestException,
            NotFoundException;

    public List<Record> loadRecordByIds(String versionId, Integer entityId, List<Long> recordIds)
            throws BadRequestException, NotFoundException;

    public String recordCountByAttributes(String versionId, Integer entityId, List<String> keyVal)
            throws BadRequestException;

    public String recordCountByIdentifier(String versionId, Integer entityId, String identifierName,
            Integer identifierDomainId) throws BadRequestException;

    public Record updateRecord(String versionId, Record record) throws ConflictException;
    
    public void assignGlobalIdentifiers(String versionId, Integer entityId)
            throws ApplicationException, AuthorizationException, BadRequestException;

    public void generateCustomFields(String versionId, Integer entityId)
            throws ApplicationException, AuthorizationException, BadRequestException;

    public void generateRecordLinks(String versionId, Integer entityId)
            throws ApplicationException, AuthorizationException, BadRequestException;

    public void initializeMatchingAlgorithm(String versionId, Integer entityId)
            throws ApplicationException, AuthorizationException, BadRequestException;
    
    public void rebuildBlockingIndexes(String versionId, Integer entityId)
            throws ApplicationException, AuthorizationException, BadRequestException;
}
