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
package org.openhie.openempi.entity.dao.orientdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.Constants;
import org.openhie.openempi.entity.DataAccessIntent;
import org.openhie.openempi.entity.RecordCacheManager;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.model.AttributeDatatype;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.model.User;

import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.graph.OGraphDatabasePool;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClassImpl;
import com.orientechnologies.orient.core.metadata.schema.OPropertyImpl;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.storage.OStorage;
import com.orientechnologies.orient.core.tx.OTransaction.TXTYPE;

public class EntityDaoOrientdb implements EntityDao
{
    private Logger log = Logger.getLogger(getClass());
    private final static String RECORD_LINK_TYPE = "recordLink";
    private final static String IDENTIFIER_TYPE = "identifier";
    private static ThreadLocal<DataAccessIntent> currentIntent = new ThreadLocal<DataAccessIntent>();
    private static ConnectionManager connectionManager;
    private static SchemaManager schemaManager;
    private RecordCacheManager entityCacheManager;

    public Record saveRecord(Entity entity, Record record) {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OGraphDatabase db = null;
        try {
            db = connect(entityStore);
            populateInternalPropertiesForCreate(entity, record);
            db.begin(TXTYPE.OPTIMISTIC);
            ODocument odoc = db.createVertex(entity.getName());
            for (String property : record.getPropertyNames()) {
                odoc.field(property, record.get(property));
            }
            odoc = odoc.save();
            Set<ODocument> ids = new HashSet<ODocument>();
            for (Identifier identifier : record.getIdentifiers()) {
                ODocument idoc = createODocumentFromIdentifier(db, odoc, identifier);
                populateInternalIdentifierPropertiesForCreate(idoc, record);
                idoc = idoc.save();
                ids.add(idoc);
            }
            odoc.field(Constants.IDENTIFIER_SET_PROPERTY, ids);
            odoc = odoc.save();
            db.commit();
            return OrientdbConverter.convertODocumentToRecord(getEntityCacheManager(), entity, odoc);
        } catch (Exception e) {
            log.error("Failed while trying to save an instance of entity: " + entityStore.getEntityName() + " due to "
                    + e, e);
            throw new RuntimeException("Failed to save an entity: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void declareIntent(Entity entity, DataAccessIntent intent) {
        if (currentIntent.get() != null) {
            if (intent != null) {
                // TODO: if its the same just skip it
                return;
            }
            currentIntent.get().end();
        }

        IntentMassiveInsertImpl implementer = new IntentMassiveInsertImpl(this);
        currentIntent.set(implementer);

        if (intent != null) {
            implementer.begin(entity, null);
        }
    }

    public Set<Record> saveRecords(Entity entity, Collection<Record> records) {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OGraphDatabase db = null;
        try {
            db = connect(entityStore);
            db.declareIntent(new OIntentMassiveInsert());
            db.begin(TXTYPE.OPTIMISTIC);
            Set<Record> saved = new HashSet<Record>();
            for (Record record : records) {
                populateInternalPropertiesForCreate(entity, record);
                ODocument odoc = db.createVertex(entity.getName());
                for (String property : record.getPropertyNames()) {
                    odoc.field(property, record.get(property));
                }
                odoc = odoc.save();
                Set<ODocument> ids = new HashSet<ODocument>();
                for (Identifier identifier : record.getIdentifiers()) {
                    ODocument idoc = createODocumentFromIdentifier(db, odoc, identifier);
                    populateInternalIdentifierPropertiesForCreate(idoc, record);
                    idoc = idoc.save();
                    ids.add(idoc);
                }
                odoc.field(Constants.IDENTIFIER_SET_PROPERTY, ids);
                odoc = odoc.save();
                saved.add(OrientdbConverter.convertODocumentToRecord(getEntityCacheManager(), entity, odoc));
            }
            db.commit();
            db.declareIntent(null);
            return saved;
        } catch (Exception e) {
            log.error("Failed while trying to save a set of records of entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            throw new RuntimeException("Failed to save a set of records: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    OGraphDatabase getConnection(Entity entity) {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        if (entityStore == null) {
            return null;
        }
        OGraphDatabase db = null;
        try {
            db = connect(entityStore);
        } catch (Exception e) {
            log.warn("Unable to obtain a database connection: " + e, e);
        }
        return db;
    }
    
    private ODocument createODocumentFromIdentifier(OGraphDatabase db, ODocument odoc, Identifier identifier) {
        ODocument idoc = db.createVertex(IDENTIFIER_TYPE);
        idoc.field(Constants.IDENTIFIER_PROPERTY, identifier.getIdentifier());
        idoc.field(Constants.ENTITY_PROPERTY, odoc);
        idoc.field(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY, identifier.getIdentifierDomainId());
        return idoc;
    }

    public void deleteRecord(Entity entity, Record record) throws ApplicationException {
        validateRecordIdentity(record);
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OGraphDatabase db = null;
        try {
            db = connect(entityStore);
            db.begin(TXTYPE.OPTIMISTIC);
            ORID orid = extractORID(entityStore, record);
            Object obj = db.load(orid);
            if (obj == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Attempted to delete an object that is not known in the database. Operation was ignored.");
                }
                return;
            }
            User deletedBy = Context.getUserContext().getUser();
            Date now = new Date();
            ODocument odoc = (ODocument) obj;
            odoc.field(Constants.DATE_VOIDED_PROPERTY, now);
            odoc.field(Constants.USER_VOIDED_BY_PROPERTY, deletedBy.getId());
            Set<ODocument> identifiers = odoc.field(Constants.IDENTIFIER_SET_PROPERTY);
            if (identifiers != null && identifiers.size() > 0) {
                for (ODocument idoc : identifiers) {
                    idoc.field(Constants.DATE_VOIDED_PROPERTY, now);
                    idoc.field(Constants.USER_VOIDED_BY_PROPERTY, deletedBy.getId());
                    idoc.save();
                }
            }
            removeEdgesToNode(db, odoc);
            odoc.save();
            db.commit();
        } catch (Exception e) {
            log.error("Failed while trying to delete an instance of entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            throw new RuntimeException("Failed to delete an entity: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void removeRecord(Entity entity, Record record) throws ApplicationException {
        validateRecordIdentity(record);
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OGraphDatabase db = null;
        try {
            db = connect(entityStore);
            db.begin(TXTYPE.OPTIMISTIC);
            ORID orid = extractORID(entityStore, record);
            Object obj = db.load(orid);
            if (obj == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Attempted to remove an object that is not known in the database. Operation was ignored.");
                }
                return;
            }
            ODocument odoc = (ODocument) obj;
            Set<ODocument> identifiers = odoc.field(Constants.IDENTIFIER_SET_PROPERTY);
            if (identifiers != null && identifiers.size() > 0) {
                for (ODocument idoc : identifiers) {
                    idoc.delete();
                }
            }
            removeEdgesToNode(db, odoc);
            odoc.delete();
            db.commit();
        } catch (Exception e) {
            log.error("Failed while trying to remove an instance of entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            throw new RuntimeException("Failed to remove an entity: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void updateRecords(Entity entity, List<Record> records) throws ApplicationException {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OGraphDatabase db = null;
        try {
            db = connect(entityStore);
            User changedBy = Context.getUserContext().getUser();
            Date now = new Date();
            db.begin(TXTYPE.OPTIMISTIC);
            for (Record record : records) {
                ORID orid = extractORID(entityStore, record);
                Object obj = db.load(orid);
                if (obj == null) {
                    log.debug("Attempted to update an object that is not known in the database: " + orid);
                    continue;
                }
                ODocument odoc = (ODocument) obj;
                updateIdentifiers(db, odoc, record.getIdentifiers(), now);
                updateDocumentWithRecord(odoc, record);
                odoc.field(Constants.DATE_CHANGED_PROPERTY, now);
                odoc.field(Constants.USER_CHANGED_BY_PROPERTY, changedBy.getId());
                odoc.save();
            }
            db.commit();
        } catch (Exception e) {
            log.error("Failed while trying to update a batch of records of entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            throw new RuntimeException("Failed to update an entity: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public Record updateRecord(Entity entity, Record record) throws ApplicationException {
        validateRecordIdentity(record);
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OGraphDatabase db = null;
        try {
            db = connect(entityStore);
            db.begin(TXTYPE.OPTIMISTIC);
            ORID orid = extractORID(entityStore, record);
            Object obj = db.load(orid);
            if (obj == null) {
                log.debug("Attempted to update an object that is not known in the database.");
                return null;
            }
            User changedBy = Context.getUserContext().getUser();
            Date now = new Date();
            ODocument odoc = (ODocument) obj;
            updateIdentifiers(db, odoc, record.getIdentifiers(), now);
            updateDocumentWithRecord(odoc, record);
            odoc.field(Constants.DATE_CHANGED_PROPERTY, now);
            odoc.field(Constants.USER_CHANGED_BY_PROPERTY, changedBy.getId());
            odoc.save();
            db.commit();
            return OrientdbConverter.convertODocumentToRecord(getEntityCacheManager(), entity, odoc);
        } catch (Exception e) {
            log.error("Failed while trying to update an instance of entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            throw new RuntimeException("Failed to update an entity: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public Record loadRecord(Entity entity, Long id) {
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        Integer clusterId = entityStore.getClusterId(entity.getName());
        if (clusterId == null) {
            log.error("Unable to identify cluster ID for class " + entity.getName() + " in entity store " + entityStore);
            throw new RuntimeException("Unable to identify cluster ID for class " + entity.getName());
        }
        String query = QueryGenerator
                .generateLoadQueryFromRecordId(OrientdbConverter.getRidFromRecordId(clusterId, id));
        try {
            db = connect(entityStore);
            List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return null;
            }
            return OrientdbConverter.convertODocumentToRecord(getEntityCacheManager(), entity, result.get(0));
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<Record> loadRecords(Entity entity, int firstResult, int maxResults) {
        String query = QueryGenerator.generateRecordQueryPaged(entity, firstResult, maxResults);
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return new ArrayList<Record>();
            }
            return OrientdbConverter.convertODocumentToRecord(getEntityCacheManager(), entity, result);
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<ODocument> executeQuery(Entity entity, String query) {
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        log.warn("Looking for clusterID  of entity " + entity + " using entity store " + entityStore);
        Integer clusterId = entityStore.getClusterId(entity.getName());
        if (clusterId == null) {
            log.error("Unable to identify cluster ID for class " + entity.getName() + " in entity store " + entityStore);
            throw new RuntimeException("Unable to identify cluster ID for class " + entity.getName());
        }
        try {
            db = connect(entityStore);
            List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return null;
            }
            return result;
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<Record> findRecordsById(Entity entity, Integer clusterId, List<Integer> recordIds) {
        String query = QueryGenerator.generateQueryFromRecordIds(entity, clusterId, recordIds);
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);

            List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return new ArrayList<Record>();
            }
            return OrientdbConverter.convertODocumentToRecord(getEntityCacheManager(), entity, result);
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<Record> findRecordsByAttributes(Entity entity, Record record) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateQueryFromRecord(entity, record, params);
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> result = db.command(new OSQLSynchQuery<ODocument>(query)).execute(params);
            if (result == null || result.size() == 0) {
                return new ArrayList<Record>();
            }
            return OrientdbConverter.convertODocumentToRecord(getEntityCacheManager(), entity, result);
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<Record> findRecordsByIdentifier(Entity entity, Identifier identifier) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateRecordQueryByIdentifier(entity, identifier, params);
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> result = db.command(new OSQLSynchQuery<ODocument>(query)).execute(params);
            if (result == null || result.size() == 0) {
                return new ArrayList<Record>();
            }
            Set<ODocument> entities = OrientdbConverter.extractEntitiesFromIdentifiers(entity, result);

            return  new ArrayList<Record>(OrientdbConverter.convertODocumentToRecord(getEntityCacheManager(), entity, entities));
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<Record> findRecordsByIdentifier(Entity entity, Identifier identifier, int firstResult, int maxResults) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateRecordQueryByIdentifier(entity, identifier, params, firstResult, maxResults);
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> result = db.command(new OSQLSynchQuery<ODocument>(query)).execute(params);
            if (result == null || result.size() == 0) {
                return new ArrayList<Record>();
            }
            Set<ODocument> entities = OrientdbConverter.extractEntitiesFromIdentifiers(entity, result);

            return  new ArrayList<Record>(OrientdbConverter.convertODocumentToRecord(getEntityCacheManager(), entity, entities));
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<Record> findRecordsWithoutIdentifierInDomain(Entity entity, IdentifierDomain domain, boolean hasLinks,
            int firstResult, int maxResults) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateRecordQueryNotInIdentifierDomain(entity, domain.getIdentifierDomainId(),
                hasLinks, params, firstResult, maxResults);
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> records = db.command(new OSQLSynchQuery<ODocument>(query)).execute(params);
            if (records == null || records.size() == 0) {
                return new ArrayList<Record>();
            }
            return  new ArrayList<Record>(OrientdbConverter.convertODocumentToRecord(getEntityCacheManager(), entity, records));
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public Long getRecordCount(Entity entity) {
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> list = db.query(new OSQLSynchQuery<ODocument>("select count(*) as count from "
                    + entity.getName() + " where dateVoided is null"));
            Long count = list.get(0).field("count");
            return count;
        } catch (Exception e) {
            log.error("Failed while trying to count the number of instances of entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new Long(0);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public Long getRecordCount(Entity entity, Record record) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateCountQueryFromRecord(entity, record, params);
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> list = db.command(new OSQLSynchQuery<ODocument>(query)).execute(params);
            return list.get(0).field("count");

        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new Long(0);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public Long getRecordCount(Entity entity, Identifier identifier) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateCountQueryFromRecord(entity, identifier, params);
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> list = db.command(new OSQLSynchQuery<ODocument>(query)).execute(params);

            Set<ODocument> entities = OrientdbConverter.extractEntitiesFromIdentifiers(entity, list);

            return new Long(entities.size());

        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new Long(0);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<Long> getAllRecordIds(Entity entity) {
        String query = QueryGenerator.generateRecordIdQuery(entity);
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return new ArrayList<Long>();
            }
            List<Long> ids = new ArrayList<Long>();
            for (ODocument odoc : result) {
                ids.add(OrientdbConverter.extractId(odoc));
            }
            return ids;
        } catch (Exception e) {
            log.error("Failed while trying to load all record IDs for entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Long>();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<Record> findRecordsByAttributes(Entity entity, Record record, int firstResult, int maxResults) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateQueryFromRecord(entity, record, params, firstResult, maxResults);
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> result = db.command(new OSQLSynchQuery<ODocument>(query)).execute(params);
            if (result == null || result.size() == 0) {
                return new ArrayList<Record>();
            }
            return OrientdbConverter.convertODocumentToRecord(getEntityCacheManager(), entity, result);
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public RecordLink loadRecordLink(Entity entity, String recordLinkId) {
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        recordLinkId = addClusterIdIfMissing(entityStore, RECORD_LINK_TYPE, recordLinkId);
        String query = QueryGenerator.generateQueryForRecordLink(recordLinkId);
        try {
            db = connect(entityStore);
            List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return null;
            }
            return OrientdbConverter.convertODocumentToRecordLink(getEntityCacheManager(), entity, result.get(0));
        } catch (Exception e) {
            log.error("Failed while trying to query the system for links using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    private String addClusterIdIfMissing(EntityStore entityStore, String entityName, String recordLinkId) {
        try {
            Integer.parseInt(recordLinkId);
            // If we were able to parse it as a number then it is missing the cluster ID
            String recordId = "#" + entityStore.getClusterId(entityName) + ":" + recordLinkId;
            return recordId;
        } catch (NumberFormatException e) {
            return recordLinkId;
        }
    }

    public Long getRecordLinkCount(Entity entity, RecordLinkState state) {
        String query = QueryGenerator.generateCountQueryFromRecordLinks(state);
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> list = db.query(new OSQLSynchQuery<ODocument>(query));
            Long count = list.get(0).field("count");

            return count / 2;

        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new Long(0);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<RecordLink> loadRecordLinks(Entity entity, RecordLinkState state, int firstResult, int maxResults) {
        // We store two edges for every link so the offset and maxResults must be adjusted appropriately.
        firstResult *= 2;
        maxResults *= 2;

        String query = QueryGenerator.generateQueryForRecordLinks(state, firstResult, maxResults);
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return new ArrayList<RecordLink>();
            }
            return OrientdbConverter.convertODocumentToRecordLink(getEntityCacheManager(), entity, result);
        } catch (Exception e) {
            log.error("Failed while trying to query the system for links using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<RecordLink>();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<RecordLink> loadRecordLinks(Entity entity, Long recordId) {
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        String recordLinkId = addClusterIdIfMissing(entityStore, entity.getName(), recordId.toString());
        String query = QueryGenerator.generateQueryForRecordLinks(entity, recordLinkId);
        log.debug("Searching for links associated with record using query " + query);
        try {
            db = connect(entityStore);
            List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return new ArrayList<RecordLink>();
            }
            return OrientdbConverter.convertODocumentToRecordLink(getEntityCacheManager(), entity, result);
        } catch (Exception e) {
            log.error("Failed while trying to query the system for links using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<RecordLink>();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<Record> loadRecordLinksById(Entity entity, Long recordId) {
        OGraphDatabase db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        String recordStringId = addClusterIdIfMissing(entityStore, entity.getName(), recordId.toString());
        String query = QueryGenerator.generateQueryForRecordLinks(entity, recordStringId);
        log.debug("Searching for links associated with record using query " + query);
        try {
            db = connect(entityStore);
            List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return new ArrayList<Record>();
            }
            return OrientdbConverter.convertODocumentToLinkedRecords(getEntityCacheManager(), entity, recordId, result);
        } catch (Exception e) {
            log.error(
                    "Failed while trying to query the system for linked records using entity: "
                            + entityStore.getEntityName() + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<RecordLink> saveRecordLinks(List<RecordLink> links) {
        List<RecordLink> savedLinks = new ArrayList<RecordLink>();
        for (RecordLink link : links) {
            RecordLink savedLink = saveRecordLink(link);
            savedLinks.add(savedLink);
        }
        return savedLinks;
    }

    public RecordLink saveRecordLink(RecordLink link) {
        Entity entity = link.getLeftRecord().getEntity();
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OGraphDatabase db = null;
        try {
            db = connect(entityStore);
            if (link.getRecordLinkId() == null) {
                saveLinkAsEdge(db, entityStore, link);
            } else {
                updateLinkEdges(db, entityStore, link);
            }
            return link;
        } catch (Exception e) {
            log.error("Failed while trying to save an instance of entity link: " + link + " due to " + e, e);
            throw new RuntimeException("Failed to save a record link: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void removeRecordLink(RecordLink link) {
        Entity entity = link.getLeftRecord().getEntity();
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        String query = QueryGenerator.generateQueryForRecordLink(link.getRecordLinkId());
        OGraphDatabase db = null;
        try {
            db = connect(entityStore);
            List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return;
            }
            ODocument edge = result.get(0);
            ODocument edgeOther = edge.field(SchemaManager.OTHER_EDGE_PROPERTY.getName());
            db.delete(edge);
            db.delete(edgeOther);
        } catch (Exception e) {
            log.error("Failed while trying to remove a record link: " + link + " due to " + e, e);
            throw new RuntimeException("Failed to remove a record link: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public List<RecordLink> getRecordLinksBySource(Entity entity, LinkSource linkSource, RecordLinkState state) {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        String query = QueryGenerator.generateQueryForRecordLink(linkSource, state);
        OGraphDatabase db = null;
        try {
            db = connect(entityStore);
            List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return new ArrayList<RecordLink>();
            }
            return OrientdbConverter.convertODocumentToRecordLink(getEntityCacheManager(), entity, result);
        } catch (Exception e) {
            log.error("Failed while trying to get record links due to " + e, e);
            throw new RuntimeException("Failed to get record links due to: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public int removeRecordLinksBySource(Entity entity, LinkSource linkSource, RecordLinkState state) {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        String query = QueryGenerator.generateQueryForRecordLink(linkSource, state);
        OGraphDatabase db = null;
        try {
            db = connect(entityStore);
            List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return 0;
            }
            int count = 0;
            for (ODocument edge : result) {
                db.delete(edge);
                count++;
            }
            return count / 2;
        } catch (Exception e) {
            log.error("Failed while trying to remove all record links due to " + e, e);
            throw new RuntimeException("Failed to remove all record links due to: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    private void updateLinkEdges(OGraphDatabase db, EntityStore entityStore, RecordLink link) {
        String recordLinkId = addClusterIdIfMissing(entityStore, RECORD_LINK_TYPE, link.getRecordLinkId());
        String query = QueryGenerator.generateQueryForRecordLink(recordLinkId);
        try {
            db.begin(TXTYPE.OPTIMISTIC);
            List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return;
            }
            ODocument edge = result.get(0);
            ODocument edgeOther = edge.field(SchemaManager.OTHER_EDGE_PROPERTY.getName());
            populateAndStoreEdge(db, edge, link);
            populateAndStoreEdge(db, edgeOther, link);
            db.commit();
        } catch (Exception e) {
            log.error("Failed while trying to update the record link using link: " + link + " due to " + e, e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    private void saveLinkAsEdge(OGraphDatabase db, EntityStore entityStore, RecordLink link) {
        db.begin(TXTYPE.OPTIMISTIC);
        ORID leftOrid = extractORID(entityStore, link.getLeftRecord());
        ORID rightOrid = extractORID(entityStore, link.getRightRecord());
        ODocument leftNode = db.load(leftOrid);
        ODocument rightNode = db.load(rightOrid);
        ODocument odocLeft = db.createEdge(leftNode, rightNode, RECORD_LINK_TYPE);
        populateAndStoreEdge(db, odocLeft, link);
        ODocument odocRight = db.createEdge(rightNode, leftNode, RECORD_LINK_TYPE);
        populateAndStoreEdge(db, odocRight, link);
        odocLeft.field(SchemaManager.OTHER_EDGE_PROPERTY.getName(), odocRight);
        odocRight.field(SchemaManager.OTHER_EDGE_PROPERTY.getName(), odocLeft);
        leftNode.save();
        rightNode.save();
        odocLeft.save();
        odocRight.save();
        db.commit();
        link.setRecordLinkId(odocLeft.getIdentity().toString());
    }

    private void populateAndStoreEdge(OGraphDatabase db, ODocument odoc, RecordLink link) {
        if (link.getRecordLinkId() == null) {
            odoc.field(SchemaManager.DATE_CREATED_PROPERTY.getName(), link.getDateCreated());
            odoc.field(SchemaManager.USER_CREATED_BY_PROPERTY.getName(), link.getUserCreatedBy().getId());
        }
        if (link.getDateReviewed() != null) {
            odoc.field(SchemaManager.DATE_REVIEWED_PROPERTY.getName(), link.getDateReviewed());
            odoc.field(SchemaManager.USER_REVIEWED_BY_PROPERTY.getName(), link.getUserReviewedBy().getId());
        }
        odoc.field(SchemaManager.LINK_WEIGHT_PROPERTY.getName(), link.getWeight());
        odoc.field(SchemaManager.LINK_VECTOR_PROPERTY.getName(), link.getVector());
        odoc.field(SchemaManager.LINK_SOURCE_PROPERTY.getName(), link.getLinkSource().getLinkSourceId());
        odoc.field(SchemaManager.LINK_STATE_PROPERTY.getName(), link.getState().getState());
        odoc.save();
    }

    private void removeEdgesToNode(OGraphDatabase db, ODocument node) {
        ORID identity = node.getIdentity();
        Object obj = node.field("in");
        if (obj == null) {
            // Record doesn't have any links
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Set<OIdentifiable> set = (Set<OIdentifiable>) obj;
            for (OIdentifiable oid : set) {
                // We need to check because OrientDB returns null entries in the
                // set sometimes instead of having an empty set.
                if (oid != null) {
                    ODocument edge = (ODocument) oid;
                    ODocument edgeOther = edge.field(SchemaManager.OTHER_EDGE_PROPERTY.getName());
                    db.delete(edge);
                    db.delete(edgeOther);
                }
            }
        } catch (Exception e) {
            log.error("Failed while trying to remove record links to node: " + identity + " due to " + e, e);
        }
    }

    private ORID extractORID(EntityStore entityStore, Record record) {
        Integer clusterId = (Integer) record.get(Constants.ORIENTDB_CLUSTER_ID_KEY);
        if (clusterId == null || clusterId < 0) {
            clusterId = entityStore.getClusterId(record.getEntity().getName());
            if (clusterId == null) {
                log.error("Unable to identify cluster ID for class " + record.getEntity().getName()
                        + " in entity store " + entityStore);
                throw new RuntimeException("Unable to identify cluster ID for class " + record.getEntity().getName());
            }
        }
        return OrientdbConverter.getORIDFromRecordId(clusterId, record.getRecordId());
    }

    private void updateIdentifiers(OGraphDatabase db, ODocument odoc, List<Identifier> newIds, Date now) {
        Set<ODocument> oldIdentifiers = odoc.field(Constants.IDENTIFIER_SET_PROPERTY);
        Map<Long, ODocument> oldIdMap = buildMapFromList(oldIdentifiers);
        Map<Long, Identifier> newIdMap = buildMapFromList(newIds);

        // Case 1. An element from the old identifier list was deleted
        if (oldIdentifiers != null) {
            for (ODocument idoc : oldIdentifiers) {
                Long id = OrientdbConverter.extractId(idoc);
                if (newIdMap.get(id) == null) {
                    idoc.field(Constants.DATE_VOIDED_PROPERTY, now);
                    idoc.field(Constants.USER_VOIDED_BY_PROPERTY, Context.getUserContext().getUser().getId());
                    log.debug("An identifier with identifier " + idoc.field(Constants.IDENTIFIER_PROPERTY)
                            + " was deleted.");
                    db.save(idoc);
                }
            }
        }

        // Case 2. An new identifier was added
        for (Identifier id : newIds) {
            if (id.getIdentifierId() == null || oldIdMap.get(id.getIdentifierId()) == null) {
                ODocument idoc = createODocumentFromIdentifier(db, odoc, id);
                idoc.field(Constants.DATE_CREATED_PROPERTY, now);
                idoc.field(Constants.USER_CREATED_BY_PROPERTY, Context.getUserContext().getUser().getId());
                log.debug("An identifier with identifier " + id.getIdentifier() + " was added.");
                db.save(idoc);
                oldIdentifiers.add(idoc);
            }
        }

        // Case 3. An element was updated
        for (Identifier id : newIds) {
            ODocument idoc = oldIdMap.get(id.getIdentifierId());
            if (idoc != null) {
                idoc.field(Constants.IDENTIFIER_PROPERTY, id.getIdentifier());
                if (id.getIdentifierDomain() != null) {
                    idoc.field(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY, id.getIdentifierDomain().getIdentifierDomainId());
                } else {
                    idoc.field(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY, id.getIdentifierDomainId());                    
                }
                log.debug("An identifier with identifier " + idoc.field(Constants.IDENTIFIER_PROPERTY)
                        + " was updated.");
                db.save(idoc);
            }
        }
        odoc.field(Constants.IDENTIFIER_SET_PROPERTY, oldIdentifiers);
    }

    private Map<Long, ODocument> buildMapFromList(Set<ODocument> ids) {
        Map<Long, ODocument> map = new HashMap<Long, ODocument>();
        if (ids == null || ids.size() == 0) {
            return map;
        }
        for (ODocument idoc : ids) {
            Long id = OrientdbConverter.extractId(idoc);
            map.put(id, idoc);
        }
        return map;
    }

    private Map<Long, Identifier> buildMapFromList(List<Identifier> ids) {
        Map<Long, Identifier> map = new HashMap<Long, Identifier>();
        if (ids == null || ids.size() == 0) {
            return map;
        }
        for (Identifier id : ids) {
            map.put(id.getIdentifierId(), id);
        }
        return map;
    }

    private void updateDocumentWithRecord(ODocument odoc, Record record) {
        for (String fieldName : odoc.fieldNames()) {
            // We don't allow clients to modify internal attributes
            if (schemaManager.isInternalAttribute(fieldName)) {
                if (log.isTraceEnabled()) {
                    log.trace("Skipping internal field " + fieldName + " during an update.");
                }
                continue;
            }
            Object value = record.get(fieldName);
            if (log.isTraceEnabled()) {
                log.trace("Updating the value of field " + fieldName + " with the value " + value);
            }
            odoc.field(fieldName, value);
        }
    }

    private void validateRecordIdentity(Record record) throws ApplicationException {
        /*
         * if (record.getRecordId() == null || record.get(Constants.ORIENTDB_CLUSTER_ID_KEY) == null) {
         * log.debug("Received request to modify an entity instance with missing record or cluster identifiers."); throw
         * new ApplicationException("Unable to modify entity instance without identifying information."); }
         */

        if (record.getRecordId() == null) {
            log.debug("Received request to modify an entity instance with missing record Id");
            throw new ApplicationException("Unable to modify entity instance with missing record Id.");
        }
    }

    public synchronized void initializeStore(Entity entity) {
        if (schemaManager == null) {
            ConnectionManager connectionMgr = getConnectionManager();
            schemaManager = SchemaManagerFactory.createSchemaManager(connectionMgr);
            schemaManager.setParameter(SchemaManager.DATA_DIRECTORY_KEY,
                    Context.getConfiguration().getAdminConfiguration().getDataDirectory());
        }
        EntityStore store = getEntityStoreByName(entity.getName());
        schemaManager.initializeSchema(entity, store);
    }

    private EntityStore getEntityStoreByName(String entityName) {
        return schemaManager.getEntityStoreByName(entityName);
    }
    
    private OGraphDatabase connect(EntityStore entityStore) {
        return getConnectionManager().connect(entityStore);
    }
    
    SchemaManager getSchemaManager(Entity entity) {
        if (schemaManager == null) {
            initializeStore(entity);
        }
        return schemaManager;
    }

    public static ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public static void setConnectionManager(ConnectionManager connectionManager) {
        EntityDaoOrientdb.connectionManager = connectionManager;
    }

    private void populateInternalPropertiesForCreate(Entity entity, Record record) {
        Date now = new Date();
        User user = Context.getUserContext().getUser();
        if (user == null) {
            throw new RuntimeException("User has not been authenticated.");
        }
        record.set(Constants.DATE_CREATED_PROPERTY, now);
        record.set(Constants.DATE_CHANGED_PROPERTY, now);
        record.set(Constants.ENTITY_VERSION_ID_PROPERTY, entity.getEntityVersionId());
        record.set(Constants.RECORD_ID_PROPERTY, record.getRecordId());
        record.set(Constants.USER_CREATED_BY_PROPERTY, user.getId());
        record.set(Constants.USER_CHANGED_BY_PROPERTY, user.getId());
    }

    private void populateInternalIdentifierPropertiesForCreate(ODocument idoc, Record record) {
        idoc.field(Constants.DATE_CREATED_PROPERTY, record.get(Constants.DATE_CREATED_PROPERTY));
        idoc.field(Constants.USER_CREATED_BY_PROPERTY, record.get(Constants.USER_CREATED_BY_PROPERTY));
    }

    public void shutdownStore(Entity entity) {
        schemaManager.shutdownStore(entity);
    }

    public RecordCacheManager getEntityCacheManager() {
        return entityCacheManager;
    }

    public void setEntityCacheManager(RecordCacheManager entityCacheManager) {
        this.entityCacheManager = entityCacheManager;
    }
}
