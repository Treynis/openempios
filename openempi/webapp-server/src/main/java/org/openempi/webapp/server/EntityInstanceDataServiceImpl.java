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
package org.openempi.webapp.server;

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.openempi.webapp.client.EntityInstanceDataService;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.IdentifierWeb;
import org.openempi.webapp.client.model.LoggedLinkListWeb;
import org.openempi.webapp.client.model.LoggedLinkSearchCriteriaWeb;
import org.openempi.webapp.client.model.RecordLinkWeb;
import org.openempi.webapp.client.model.RecordLinksListWeb;
import org.openempi.webapp.client.model.RecordListWeb;
import org.openempi.webapp.client.model.RecordSearchCriteriaWeb;
import org.openempi.webapp.client.model.RecordWeb;
import org.openempi.webapp.client.model.UserWeb;
import org.openempi.webapp.server.util.ModelTransformer;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.LoggedLink;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.service.AuditEventService;

public class EntityInstanceDataServiceImpl extends AbstractRemoteServiceServlet implements EntityInstanceDataService
{
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public List<RecordWeb> getMatchingEntities(EntityWeb entityModel, RecordWeb entity) throws Exception {
        log.debug("Received request to retrieve a list of entity instance records that match the entity specified as a parameter.");

        authenticateCaller();
        try {
            RecordQueryService entityInstanceService = Context.getRecordQueryService();
            EntityDefinitionManagerService entityDefService = Context.getEntityDefinitionManagerService();
            Entity entityDef = entityDefService.loadEntity(entityModel.getEntityVersionId());

            // pass the entity definition model for formatting the Date search string
            org.openhie.openempi.model.Record recordForMatch = ModelTransformer.mapToRecordForSearch(entityDef, entity,
                    Record.class);

            List<Record> records = entityInstanceService.findRecordsByAttributes(entityDef, recordForMatch);

            List<RecordWeb> dtos = new java.util.ArrayList<RecordWeb>(records.size());
            for (Record record : records) {
                RecordWeb dto = ModelTransformer.mapToRecord(record, RecordWeb.class);
                dtos.add(dto);
            }

            return dtos;
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public RecordListWeb getEntityRecordsBySearch(RecordSearchCriteriaWeb searchCriteria) throws Exception {
        log.debug("Get Entity Records By Search");

        authenticateCaller();
        try {
            RecordQueryService entityInstanceService = Context.getRecordQueryService();
            EntityDefinitionManagerService entityDefService = Context.getEntityDefinitionManagerService();

            EntityWeb entityModel = searchCriteria.getEntityModel();
            Entity entityDef = entityDefService.loadEntity(entityModel.getEntityVersionId());

            RecordWeb entity = searchCriteria.getRecord();
            int offset = searchCriteria.getFirstResult();
            int pageSize = searchCriteria.getMaxResults();

            org.openhie.openempi.model.Record recordForMatch = ModelTransformer.mapToRecordForSearch(entityDef, entity,
                    Record.class);

            // Get total count
            Long totalCount = searchCriteria.getTotalCount();
            if (totalCount == 0) {
                totalCount = entityInstanceService.getRecordCount(entityDef, recordForMatch);
            }

            // get List of Record
            List<Record> records = entityInstanceService.findRecordsByAttributes(entityDef, recordForMatch, offset,
                    pageSize);

            List<RecordWeb> dtos = new java.util.ArrayList<RecordWeb>(records.size());
            for (Record record : records) {
                RecordWeb dto = ModelTransformer.mapToRecord(record, RecordWeb.class);
                dtos.add(dto);
            }

            RecordListWeb recordList = new RecordListWeb();
            recordList.setTotalCount(totalCount);
            recordList.setRecords(dtos);

            return recordList;

        } catch (Exception e) {
            log.error("Failed while trying to get audit events: " + e, e);
        }
        return null;
    }

    public RecordListWeb findEntitiesByIdentifier(RecordSearchCriteriaWeb searchCriteria) throws Exception {
        log.debug("Received entity records by identifier.");

        authenticateCaller();
        try {
            RecordQueryService entityInstanceService = Context.getRecordQueryService();
            EntityDefinitionManagerService entityDefService = Context.getEntityDefinitionManagerService();

            EntityWeb entityModel = searchCriteria.getEntityModel();
            Entity entityDef = entityDefService.loadEntity(entityModel.getEntityVersionId());

            IdentifierWeb identifierWeb = searchCriteria.getIdentifier();
            int offset = searchCriteria.getFirstResult();
            int pageSize = searchCriteria.getMaxResults();

            org.openhie.openempi.model.Identifier identifier = ModelTransformer.map(identifierWeb,
                    org.openhie.openempi.model.Identifier.class);

            // Get total count
            Long totalCount = searchCriteria.getTotalCount();
            if (totalCount == 0) {
                totalCount = entityInstanceService.getRecordCount(entityDef, identifier);
            }

            // get List of Record
            List<Record> records = entityInstanceService.findRecordsByIdentifier(entityDef, identifier, offset,
                    pageSize);

            List<RecordWeb> dtos = new java.util.ArrayList<RecordWeb>(records.size());
            for (Record record : records) {
                RecordWeb dto = ModelTransformer.mapToRecord(record, RecordWeb.class);
                dtos.add(dto);
            }

            RecordListWeb recordList = new RecordListWeb();
            recordList.setTotalCount(totalCount);
            recordList.setRecords(dtos);

            return recordList;
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    public List<RecordWeb> findEntitiesByIdentifier(EntityWeb entityModel, IdentifierWeb identferWeb) throws Exception {
        log.debug("Received request to retrieve a list of entity instance records that match the entity specified as a parameter.");

        authenticateCaller();
        try {
            RecordQueryService entityInstanceService = Context.getRecordQueryService();
            EntityDefinitionManagerService entityDefService = Context.getEntityDefinitionManagerService();
            Entity entityDef = entityDefService.loadEntity(entityModel.getEntityVersionId());

            org.openhie.openempi.model.Identifier identifier = ModelTransformer.map(identferWeb,
                    org.openhie.openempi.model.Identifier.class);

            List<Record> records = entityInstanceService.findRecordsByIdentifier(entityDef, identifier);

            List<RecordWeb> dtos = new java.util.ArrayList<RecordWeb>(records.size());
            for (Record record : records) {
                RecordWeb dto = ModelTransformer.mapToRecord(record, RecordWeb.class);
                dtos.add(dto);
            }

            return dtos;
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    public RecordWeb loadEntityById(EntityWeb entityModel, Long recordId) {
        log.debug("Received request to load entity instance record by Id.");

        authenticateCaller();
        try {
            EntityDefinitionManagerService entityDefService = Context.getEntityDefinitionManagerService();
            Entity entityDef = entityDefService.loadEntity(entityModel.getEntityVersionId());
            RecordQueryService entityInstanceService = Context.getRecordQueryService();

            Record record = entityInstanceService.loadRecordById(entityDef, recordId);
            if (record != null) {
                return ModelTransformer.mapToRecord(record, RecordWeb.class);
            }
            return null;
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    public RecordWeb addEntity(EntityWeb entityModel, RecordWeb entity) throws Exception {
        log.debug("Received request to add entity instance record.");

        authenticateCaller();
        try {
            EntityDefinitionManagerService entityDefService = Context.getEntityDefinitionManagerService();
            Entity entityDef = entityDefService.loadEntity(entityModel.getEntityVersionId());
            RecordManagerService entityInstanceService = Context.getRecordManagerService();

            org.openhie.openempi.model.Record record = ModelTransformer.mapToRecord(entityDef, entity, Record.class);

            Record addedRecord = entityInstanceService.addRecord(entityDef, record);

            return ModelTransformer.mapToRecord(addedRecord, RecordWeb.class);

        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    public RecordWeb updateEntity(EntityWeb entityModel, RecordWeb entity) throws Exception {
        log.debug("Received request to update entity instance record.");

        authenticateCaller();
        try {
            RecordManagerService entityInstanceService = Context.getRecordManagerService();
            EntityDefinitionManagerService entityDefService = Context.getEntityDefinitionManagerService();
            Entity entityDef = entityDefService.loadEntity(entityModel.getEntityVersionId());
            org.openhie.openempi.model.Record record = ModelTransformer.mapToRecord(entityDef, entity, Record.class);

            Record updatedRecord = entityInstanceService.updateRecord(entityDef, record);

            return ModelTransformer.mapToRecord(updatedRecord, RecordWeb.class);

        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    public String deleteEntity(EntityWeb entityModel, RecordWeb entity) throws Exception {
        log.debug("Received request to delete entity instance record.");

        authenticateCaller();
        String msg = "";
        try {
            RecordManagerService entityInstanceService = Context.getRecordManagerService();
            EntityDefinitionManagerService entityDefService = Context.getEntityDefinitionManagerService();
            Entity entityDef = entityDefService.loadEntity(entityModel.getEntityVersionId());
            org.openhie.openempi.model.Record record = ModelTransformer.mapToRecord(entityDef, entity, Record.class);

            entityInstanceService.deleteRecord(entityDef, record);

        } catch (Throwable t) {
            log.error("Failed to delete entry instance: " + t, t);
            msg = t.getMessage();
        }
        return msg;
    }

    @Override
    public RecordLinksListWeb loadRecordLinksPaged(RecordSearchCriteriaWeb searchCriteria) throws Exception {
        log.debug("Get Record Links");

        authenticateCaller();
        try {
            RecordQueryService entityInstanceService = Context.getRecordQueryService();
            EntityDefinitionManagerService entityDefService = Context.getEntityDefinitionManagerService();

            EntityWeb entityModel = searchCriteria.getEntityModel();
            Entity entityDef = entityDefService.loadEntity(entityModel.getEntityVersionId());

            int offset = searchCriteria.getFirstResult();
            int pageSize = searchCriteria.getMaxResults();
            String state = searchCriteria.getSearchMode().substring(0, 1);

            // Get total count
            Long totalCount = searchCriteria.getTotalCount();
            if (totalCount == 0) {
                totalCount = entityInstanceService.getRecordLinkCount(entityDef, RecordLinkState.fromString(state));
            }

            // get List of Record
            List<RecordLink> recordLinks = entityInstanceService.loadRecordLinks(entityDef,
                    RecordLinkState.fromString(state), offset, pageSize);

            List<RecordLinkWeb> dtos = new java.util.ArrayList<RecordLinkWeb>(recordLinks.size());
            for (RecordLink record : recordLinks) {

                // loadRecordLinks without left and right Records
                RecordLinkWeb dto = ModelTransformer.mapToRecordLink(record, RecordLinkWeb.class, false);
                dtos.add(dto);
            }

            RecordLinksListWeb recordList = new RecordLinksListWeb();
            recordList.setTotalCount(totalCount);
            recordList.setRecordLinks(dtos);

            return recordList;

        } catch (Exception e) {
            log.error("Failed while trying to get audit events: " + e, e);
        }
        return null;
    }

    public List<RecordLinkWeb> loadRecordLinks(EntityWeb entityModel, String state, int firstResult, int maxResults)
            throws Exception {
        log.debug("Received request to retrieve a list of entity record links.");

        authenticateCaller();
        try {
            RecordQueryService entityInstanceService = Context.getRecordQueryService();
            EntityDefinitionManagerService entityDefService = Context.getEntityDefinitionManagerService();
            Entity entityDef = entityDefService.loadEntity(entityModel.getEntityVersionId());

            List<RecordLink> records = entityInstanceService.loadRecordLinks(entityDef,
                    RecordLinkState.fromString(state), firstResult, maxResults);

            List<RecordLinkWeb> dtos = new java.util.ArrayList<RecordLinkWeb>(records.size());
            for (RecordLink record : records) {

                // loadRecordLinks without left and right Records
                RecordLinkWeb dto = ModelTransformer.mapToRecordLink(record, RecordLinkWeb.class, false);
                dtos.add(dto);
            }

            return dtos;
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    public RecordLinkWeb loadRecordLink(EntityWeb entityModel, RecordLinkWeb linkPair) throws Exception {
        log.debug("Received request to retrieve a list of entity record links.");

        authenticateCaller();
        try {
            RecordQueryService entityInstanceService = Context.getRecordQueryService();
            EntityDefinitionManagerService entityDefService = Context.getEntityDefinitionManagerService();
            Entity entityDef = entityDefService.loadEntity(entityModel.getEntityVersionId());

            RecordLink recordLink = ModelTransformer.mapToRecordLink(linkPair, RecordLink.class);

            RecordLink record = entityInstanceService.loadRecordLink(entityDef, recordLink.getRecordLinkId());

            RecordLinkWeb recordWeb = ModelTransformer.mapToRecordLink(record, RecordLinkWeb.class, true);

            return recordWeb;
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    public RecordLinkWeb updateRecordLink(EntityWeb entityModel, RecordLinkWeb linkPair) throws Exception {
        log.debug("Received request to updaty an entity record link.");

        authenticateCaller();
        try {
            RecordQueryService entityInstanceService = Context.getRecordQueryService();
            RecordManagerService entityInstanceManagerService = Context.getRecordManagerService();
            EntityDefinitionManagerService entityDefService = Context.getEntityDefinitionManagerService();
            Entity entityDef = entityDefService.loadEntity(entityModel.getEntityVersionId());

            RecordLink recordLink = ModelTransformer.mapToRecordLink(linkPair, RecordLink.class);

            RecordLink record = entityInstanceService.loadRecordLink(entityDef, recordLink.getRecordLinkId());
            record.setState(recordLink.getState());
            record = entityInstanceManagerService.updateRecordLink(record);

            RecordLinkWeb recordWeb = ModelTransformer.mapToRecordLink(record, RecordLinkWeb.class, true);

            return recordWeb;
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    public List<RecordWeb> loadLinksFromRecord(EntityWeb entityModel, RecordWeb entity) throws Exception {
        log.debug("Received request to retrieve a list of entity record links.");

        authenticateCaller();
        try {
            RecordQueryService entityInstanceService = Context.getRecordQueryService();
            EntityDefinitionManagerService entityDefService = Context.getEntityDefinitionManagerService();
            Entity entityDef = entityDefService.loadEntity(entityModel.getEntityVersionId());

            // loadRecordLinks without left and right Records
            List<Record> records = entityInstanceService.loadRecordLinksByRecordId(entityDef, entity.getRecordId());

            List<RecordWeb> dtos = new java.util.ArrayList<RecordWeb>(records.size());
            for (Record record : records) {
                RecordWeb dto = ModelTransformer.mapToRecord(record, RecordWeb.class);
                dtos.add(dto);
            }

            return dtos;
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public LoggedLinkListWeb getLoggedLinks(LoggedLinkSearchCriteriaWeb search) throws Exception {
        authenticateCaller();
        try {
            AuditEventService auditEventService = Context.getAuditEventService();
            EntityDefinitionManagerService entityDefService = Context.getEntityDefinitionManagerService();
            RecordQueryService entityInstanceService = Context.getRecordQueryService();

            LoggedLinkListWeb loggedLinkList = new LoggedLinkListWeb();

            // entity model
            EntityWeb entityModel = search.getEntityModel();
            Entity entityDef = entityDefService.loadEntity(entityModel.getEntityVersionId());
            if (entityDef == null) {
                loggedLinkList.setTotalCount(0);
                loggedLinkList.setRecordPairs(new java.util.ArrayList<RecordLinkWeb>());
                return loggedLinkList;
            }

            //  total count
            int totalCount = auditEventService.getLoggedLinksCount(entityDef.getEntityVersionId(), search.getVector());
            if (totalCount == 0) {
                loggedLinkList.setTotalCount(0);
                loggedLinkList.setRecordPairs(new java.util.ArrayList<RecordLinkWeb>());
                return loggedLinkList;
            }

            // link logs
            List<LoggedLink> links = auditEventService.getLoggedLinks(entityDef.getEntityVersionId(),
                    search.getVector(),
                    search.getFirstResult(),
                    search.getMaxResults());

            List<RecordLinkWeb> recordLinks = new java.util.ArrayList<RecordLinkWeb>();
            for (LoggedLink link : links) {

                Record leftRecord = entityInstanceService.loadRecordById(entityDef, link.getLeftRecordId());
                Record rightRecord = entityInstanceService.loadRecordById(entityDef, link.getRightRecordId());

                if (leftRecord != null && rightRecord != null) {
                    RecordLinkWeb recordLinkWeb = new RecordLinkWeb();
                    recordLinkWeb.setWeight(link.getWeight());
                    recordLinkWeb.setVector(link.getVectorValue());
                    recordLinkWeb.setDateCreated(link.getDateCreated());
                    recordLinkWeb.setLeftRecord(ModelTransformer.mapToRecord(leftRecord, RecordWeb.class));
                    recordLinkWeb.setRightRecord(ModelTransformer.mapToRecord(rightRecord, RecordWeb.class));

                    if (link.getUserCreatedBy() != null) {
                        UserWeb user = ModelTransformer.mapToUser(link.getUserCreatedBy(), UserWeb.class, false);
                        recordLinkWeb.setUserCreatedBy(user);
                    }

                    recordLinks.add(recordLinkWeb);
                }
            }

            loggedLinkList.setTotalCount(totalCount);
            loggedLinkList.setRecordPairs(recordLinks);

            return loggedLinkList;
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }
}
