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
package org.openhie.openempi.entity.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import org.openhie.openempi.entity.RecordCacheManager;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.dao.IdentifierDomainDao;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.model.User;
import org.openhie.openempi.model.IdentifierUpdateEvent;

public class RecordQueryServiceImpl extends RecordCommonServiceImpl implements RecordQueryService
{
    private EntityDefinitionManagerService entityDefinitionService;
    private RecordCacheManager entityCacheManager;
    private EntityDao entityDao;
    private IdentifierDomainDao identifierDomainDao;

    public RecordQueryServiceImpl() {
    }

    public List<Record> findRecordsByAttributes(Entity entity, Record record) {
        Entity entityDef = getEntityDefinition(entity);
        if (entityDef == null) {
            return new ArrayList<Record>();
        }

        boolean criteriaSpecified = validateCriteriaPresent(record);
        if (!criteriaSpecified) {
            log.debug("Attempted to query the system without specifying any criteria.");
            return new ArrayList<Record>();
        }

        return entityDao.findRecordsByAttributes(entityDef, record);
    }

    public Long getRecordCount(Entity entity, Record record) {
        Entity entityDef = getEntityDefinition(entity);
        if (entityDef == null) {
            return new Long(0);
        }

        boolean criteriaSpecified = validateCriteriaPresent(record);
        if (!criteriaSpecified) {
            return new Long(0);
        }
        return entityDao.getRecordCount(entityDef, record);
    }

    public Long getRecordCount(Entity entity, Identifier identifier) {
        Entity entityDef = getEntityDefinition(entity);
        if (entityDef == null) {
            return new Long(0);
        }
        if (identifier == null || identifier.getIdentifier() == null) {
            return new Long(0);
        }
        return entityDao.getRecordCount(entityDef, identifier);
    }

    public List<Record> loadAllRecordsPaged(Entity entity, Record record, int firstResult, int maxResults) {
        Entity entityDef = getEntityDefinition(entity);
        if (entityDef == null) {
            return new ArrayList<Record>();
        }
        return entityDao.findRecordsByAttributes(entityDef, record, firstResult, maxResults);
    }

    public Long getRecordLinkCount(Entity entity, RecordLinkState state) {
        Entity entityDef = getEntityDefinition(entity);
        if (entityDef == null) {
            return new Long(0);
        }
        return entityDao.getRecordLinkCount(entityDef, state);
    }

    public List<RecordLink> loadRecordLinks(Entity entity, RecordLinkState state, int firstResult, int maxResults) {
        Entity entityDef = getEntityDefinition(entity);
        if (entityDef == null) {
            return new ArrayList<RecordLink>();
        }
        return entityDao.loadRecordLinks(entityDef, state, firstResult, maxResults);
    }

    public RecordLink loadRecordLink(Entity entity, String recordLinkId) {
        Entity entityDef = getEntityDefinition(entity);
        if (entityDef == null || recordLinkId == null) {
            return null;
        }
        return entityDao.loadRecordLink(entityDef, recordLinkId);
    }

    public List<RecordLink> loadRecordLinks(Entity entity, Long recordId) {
        Entity entityDef = getEntityDefinition(entity);
        if (entityDef == null || recordId == null) {
            return null;
        }
        return entityDao.loadRecordLinks(entityDef, recordId);
    }

    public List<RecordLink> findRecordLinksBySource(Entity entity, LinkSource linkSource, RecordLinkState state) {
        Entity entityDef = getEntityDefinition(entity);
        if (entityDef == null || linkSource == null) {
            return null;
        }
        return entityDao.getRecordLinksBySource(entityDef, linkSource, state);
    }
    
    public List<Record> loadRecordLinksByRecordId(Entity entity, Long recordId) {
        Entity entityDef = getEntityDefinition(entity);
        if (entityDef == null || recordId == null) {
            return null;
        }
        return entityDao.loadRecordLinksById(entityDef, recordId);
    }

    public List<Record> findRecordsByAttributes(Entity entity, Record record, int firstResult, int maxResults) {
        Entity entityDef = getEntityDefinition(entity);
        if (entityDef == null) {
            return new ArrayList<Record>();
        }
        boolean criteriaSpecified = validateCriteriaPresent(record);
        if (!criteriaSpecified) {
            log.debug("Attempted to query the system without specifying any criteria.");
            return new ArrayList<Record>();
        }

        return entityDao.findRecordsByAttributes(entityDef, record, firstResult, maxResults);
    }

    public List<Record> loadRecordsById(Entity entity, List<Long> recordIds) {
        Entity entityDef = getEntityDefinition(entity);
        if (entityDef == null) {
            return new ArrayList<Record>();
        }
        List<Record> records = new ArrayList<Record>(recordIds.size());
        for (Long recordId : recordIds) {
            Record record = entityDao.loadRecord(entityDef, recordId);
            records.add(record);
        }
        return records;
    }

    public Record loadRecordById(Entity entity, Long recordId) {
        Entity entityDef = getEntityDefinition(entity);
        if (entityDef == null) {
            return null;
        }
        Record record = entityDao.loadRecord(entityDef, recordId);
        return record;
    }

    public List<Record> findRecordsByIdentifier(Entity entity, Identifier identifier) {
        Entity entityDef = getEntityDefinition(entity);
        if (entityDef == null) {
            return new ArrayList<Record>();
        }
        if (identifier == null || identifier.getIdentifier() == null) {
            return new ArrayList<Record>();
        }

        return entityDao.findRecordsByIdentifier(entity, identifier);
    }

    public List<Record> findRecordsByIdentifier(Entity entity, Identifier identifier, int firstResult, int maxResults) {
        Entity entityDef = getEntityDefinition(entity);
        if (entityDef == null) {
            return new ArrayList<Record>();
        }
        if (identifier == null || identifier.getIdentifier() == null) {
            return new ArrayList<Record>();
        }

        return entityDao.findRecordsByIdentifier(entity, identifier, firstResult, maxResults);
    }

    private boolean validateCriteriaPresent(Record record) {
        for (String property : record.getPropertyNames()) {
            if (record.get(property) != null) {
                return true;
            }
        }
        return false;
    }

    private Entity getEntityDefinition(Entity entity) {
        if (entity == null || entity.getEntityVersionId() == null) {
            log.debug("Received a request to query the system for entities with insufficient criteria specified.");
            return null;
        }

        Entity entityDef = entityDefinitionService.loadEntity(entity.getEntityVersionId());
        if (entityDef == null) {
            log.debug("Attempted to query the system with an unknown entity type.");
            return null;
        }
        return entityDef;
    }

    public int getNotificationCount(User user) {
        int count =  identifierDomainDao.getIdentifierUpdateEventCount(user);
         return count;
    }

    public List<IdentifierUpdateEvent> retrieveNotifications(int startIndex, int maxEvents, Boolean removeRecords, User eventRecipient) {

         List<IdentifierUpdateEvent> identifierUpdateEvents = identifierDomainDao.getIdentifierUpdateEvents(startIndex, maxEvents, eventRecipient);

         if (removeRecords) {
            for (IdentifierUpdateEvent identifierUpdateEvent : identifierUpdateEvents) {
                log.trace("Deleting IdentifierUpdateEvent : " + identifierUpdateEvent.getIdentifierUpdateEventId());
                identifierDomainDao.removeIdentifierUpdateEvent(identifierUpdateEvent);
            }
         }

         return identifierUpdateEvents;
    }

    public List<IdentifierUpdateEvent> retrieveNotifications(Boolean removeRecords, User eventRecipient) {
         List<IdentifierUpdateEvent> identifierUpdateEvents = identifierDomainDao.getIdentifierUpdateEvents(eventRecipient);

         if (removeRecords) {
            for (IdentifierUpdateEvent identifierUpdateEvent : identifierUpdateEvents) {
                log.trace("Deleting IdentifierUpdateEvent : " + identifierUpdateEvent.getIdentifierUpdateEventId());
                identifierDomainDao.removeIdentifierUpdateEvent(identifierUpdateEvent);
            }
         }

         return identifierUpdateEvents;
    }

    public List<IdentifierUpdateEvent> retrieveNotificationsByDate(Date startDate, Boolean removeRecords, User eventRecipient) {
         List<IdentifierUpdateEvent> identifierUpdateEvents = identifierDomainDao.getIdentifierUpdateEventsByDate(startDate, eventRecipient);

         if (removeRecords) {
            for (IdentifierUpdateEvent identifierUpdateEvent : identifierUpdateEvents) {
                log.trace("Deleting IdentifierUpdateEvent : " + identifierUpdateEvent.getIdentifierUpdateEventId());
                identifierDomainDao.removeIdentifierUpdateEvent(identifierUpdateEvent);
            }
         }

         return identifierUpdateEvents;
    }

    public EntityDefinitionManagerService getEntityDefinitionService() {
        return entityDefinitionService;
    }

    public void setEntityDefinitionService(EntityDefinitionManagerService entityDefinitionService) {
        this.entityDefinitionService = entityDefinitionService;
    }

    public IdentifierDomainDao getIdentifierDomainDao() {
        return identifierDomainDao;
    }

    public void setIdentifierDomainDao(IdentifierDomainDao identifierDomainDao) {
        this.identifierDomainDao = identifierDomainDao;
    }

    public EntityDao getEntityDao() {
        return entityDao;
    }

    public void setEntityDao(EntityDao entityDao) {
        this.entityDao = entityDao;
    }

    public RecordCacheManager getEntityCacheManager() {
        return entityCacheManager;
    }

    public void setEntityCacheManager(RecordCacheManager entityCacheManager) {
        this.entityCacheManager = entityCacheManager;
    }
}
