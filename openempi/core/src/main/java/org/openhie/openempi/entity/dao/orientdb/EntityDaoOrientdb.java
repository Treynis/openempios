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
import org.openhie.openempi.entity.RecordCacheManager;
import org.openhie.openempi.entity.dao.AsyncQueryCallback;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.model.DataAccessIntent;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.model.User;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLAsynchQuery;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

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
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            Vertex instance = saveRecordAndIdentifiers(entity, record, db);
            return OrientdbConverter.convertVertexToRecord(getEntityCacheManager(), entity, instance);
        } catch (Exception e) {
            log.error("Failed while trying to save an instance of entity: " + entityStore.getEntityName() + " due to "
                    + e, e);
            throw new RuntimeException("Failed to save an entity: " + e.getMessage());
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    private String getClassName(String name) {
        return "class:" + name;
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
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            Set<Record> saved = new HashSet<Record>();
            Set<Vertex> savedVertices = new HashSet<Vertex>();
            for (Record record : records) {
                Vertex vertex = saveRecordAndIdentifiers(entity, record, db);
                savedVertices.add(vertex);
            }
            for (Vertex vertex : savedVertices) {
                saved.add(OrientdbConverter.convertVertexToRecord(getEntityCacheManager(), entity, vertex));
            }
            return saved;
        } catch (Exception e) {
            log.error("Failed while trying to save a set of records of entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            throw new RuntimeException("Failed to save a set of records: " + e.getMessage());
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    /**
    private Vertex saveRecordAndIdentifiers(Entity entity, Record record, OrientGraph db) {
        db.getRawGraph().begin();
        populateInternalPropertiesForCreate(entity, record);
        Map<String, Object> properties = new HashMap<String, Object>();
        for (String property : record.getPropertyNames()) {
            Object value = record.get(property);
            if (value != null) {
                properties.put(property, value);
            }
        }
        Vertex instance = db.addVertex(getClassName(entity.getName()), properties);
        ODocument doc = ((OrientVertex) instance).getRecord().save(true);
        if (doc.getIdentity().isTemporary()) {
//            log.warn("Node is temporary: " + doc);
        }
        Set<Vertex> ids = new HashSet<Vertex>();
        for (Identifier identifier : record.getIdentifiers()) {
            Vertex vertex = saveIdentifier(db, instance, record, identifier);
            ODocument idoc = ((OrientVertex) vertex).getRecord().save(true);
            if (idoc.getIdentity().isTemporary()) {
//                log.warn("Node is temporary: " + idoc);
            }
            ids.add(vertex);
        }
        instance.setProperty(Constants.IDENTIFIER_SET_PROPERTY, ids);
        doc = ((OrientVertex) instance).getRecord().save(true);
//        if (doc.getIdentity().isTemporary()) {
//            log.warn("At set property node is temporary: " + doc);
//        }
        db.commit();
        return instance;
    }
    */
    private Vertex saveRecordAndIdentifiers(Entity entity, Record record, OrientGraph db) {
        populateInternalPropertiesForCreate(entity, record);
        Map<String, Object> properties = new HashMap<String, Object>();
        for (String property : record.getPropertyNames()) {
            Object value = record.get(property);
            if (value != null) {
                properties.put(property, value);
            }
        }
        if (record.isDirty()) {
            properties.put(Constants.DIRTY_RECORD_PROPERTY, Boolean.TRUE);
        } else {
            properties.put(Constants.DIRTY_RECORD_PROPERTY, Boolean.FALSE);
        }
        OrientVertex instance = (OrientVertex) db.addVertex(getClassName(entity.getName()), properties);
        for (Identifier identifier : record.getIdentifiers()) {
            OrientVertex vertex = (OrientVertex) saveIdentifier(db, instance, record, identifier);
            OrientEdge edge = db.addEdge(null, instance, vertex, Constants.IDENTIFIER_EDGE_TYPE);
            log.info("Created vertex for identifier " + vertex + " and edge " + edge);
//            instance.addEdge(getClassName(Constants.IDENTIFIER_EDGE_TYPE), vertex);
        }
        db.getRawGraph().commit(true);
        return instance;
    }
    
    OrientBaseGraph getConnectionInternal(Entity entity) {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        if (entityStore == null) {
            return null;
        }
        OrientBaseGraph db = null;
        try {
            db = connectionManager.connectInitial(entityStore);
        } catch (Exception e) {
            log.warn("Unable to obtain a database connection: " + e, e);
        }
        return db;
    }
    
    OrientGraph getConnection(Entity entity) {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        if (entityStore == null) {
            return null;
        }
        OrientGraph db = null;
        try {
            db = connect(entityStore);
        } catch (Exception e) {
            log.warn("Unable to obtain a database connection: " + e, e);
        }
        return db;
    }
    
    private Vertex saveIdentifier(OrientGraph db, Vertex owner, Record record, Identifier identifier) {
        Map<String,Object> properties = new HashMap<String,Object>();
        if (identifier.getRecord() == null) {
            identifier.setRecord(record);
        }
        properties.put(Constants.IDENTIFIER_PROPERTY, identifier.getIdentifier());
        properties.put(Constants.ENTITY_PROPERTY, owner);
        properties.put(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY, identifier.getIdentifierDomainId());
        populateInternalIdentifierPropertiesForCreate(properties, record);
        return db.addVertex(getClassName(IDENTIFIER_TYPE), properties);
   }

    public void deleteRecord(Entity entity, Record record) throws ApplicationException {
        validateRecordIdentity(record);
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            ORID orid = extractORID(entityStore, record);
            Object obj = db.getRawGraph().load(orid);
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
            Set<ODocument> identifiers = OrientdbConverter.getIdentifiers(odoc.field(Constants.IDENTIFIER_OUT_PROPERTY));
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
                db.getRawGraph().close();
            }
        }
    }

    public void removeRecord(Entity entity, Record record) throws ApplicationException {
        validateRecordIdentity(record);
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            ORID orid = extractORID(entityStore, record);
            Object obj = db.getRawGraph().load(orid);
            if (obj == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Attempted to remove an object that is not known in the database. Operation was ignored.");
                }
                return;
            }
            ODocument odoc = (ODocument) obj;
            Set<ODocument> identifiers = OrientdbConverter.getIdentifiers(odoc.field(Constants.IDENTIFIER_OUT_PROPERTY));
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
                db.getRawGraph().close();
            }
        }
    }

    public void updateRecords(Entity entity, List<Record> records) throws ApplicationException {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            User changedBy = Context.getUserContext().getUser();
            Date now = new Date();
            for (Record record : records) {
                ORID orid = extractORID(entityStore, record);
                Object obj = db.getRawGraph().load(orid);
                if (obj == null) {
                    log.debug("Attempted to update an object that is not known in the database: " + orid);
                    continue;
                }
                ODocument odoc = (ODocument) obj;
                updateIdentifiers(db, odoc, record.getIdentifiers(), now);
                updateDocumentWithRecord(odoc, record);
                boolean dirty = record.isDirty();
                if (dirty == false) {
                    odoc.field(Constants.DIRTY_RECORD_PROPERTY, Boolean.FALSE);
                } else {
                    odoc.field(Constants.DIRTY_RECORD_PROPERTY, Boolean.TRUE);
                }                
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
                db.getRawGraph().close();
            }
        }
    }

    public Record updateRecord(Entity entity, Record record) throws ApplicationException {
        validateRecordIdentity(record);
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            ORID orid = extractORID(entityStore, record);
            ODocument obj = db.getRawGraph().load(orid);
            if (obj == null) {
                log.debug("Attempted to update an object that is not known in the database.");
                return null;
            }
            User changedBy = Context.getUserContext().getUser();
            Date now = new Date();
            ODocument odoc = (ODocument) obj;
            updateIdentifiers(db, obj, record.getIdentifiers(), now);
            updateDocumentWithRecord(odoc, record);
            boolean dirty = record.isDirty();
            if (dirty == false) {
                odoc.field(Constants.DIRTY_RECORD_PROPERTY, Boolean.FALSE);
            } else {
                odoc.field(Constants.DIRTY_RECORD_PROPERTY, Boolean.TRUE);
            }
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
                db.getRawGraph().close();
            }
        }
    }

    public void saveData(Entity entity, String className, Record record) {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            className = getClassName(className);
            if (record.getRecordId() == null) {
                Map<String,Object> props = new HashMap<String,Object>();
                for (String property : record.getPropertyNames()) {
                    props.put(property, record.get(property));
                }
                OrientVertex vertex = (OrientVertex) db.addVertex(className, props);
                vertex.getBaseClassName();
            } else {
                ORID orid = extractORID(entityStore, record);
                Object obj = db.getRawGraph().load(orid);
                if (obj == null) {
                    if (log.isDebugEnabled()) { 
                        log.debug("Attempted to update an object " + record + " that is not known in the database.");
                    }
                    return;
                }
                ODocument odoc = (ODocument) obj;
                updateDocumentWithRecord(odoc, record);
                odoc.save();
            }
            db.commit();
        } catch (OConcurrentModificationException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to save " + record.getRecordId() + " because another user/thread modified the record; you should retry.");
            }
            throw new ConcurrentModificationException(e);
        } catch (Exception e) {
            log.error("Failed while trying to save a record of type: " + className + " due to "
                    + e, e);
            throw new RuntimeException("Failed to save a record due to: " + e.getMessage());
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    public Object loadObject(Entity entity, String recordId) {
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        String query = "select from " + recordId;
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return null;
            }
            return result.get(0);
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return null;
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Record loadRecord(Entity entity, Long id) {
        OrientGraph db = null;
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
            Iterable<Vertex> result = (Iterable<Vertex>) db.command(new OCommandSQL(query)).execute();
            if (result == null || !result.iterator().hasNext()) {
                return null;
            }
            log.debug("Result is " + result);
            
            return OrientdbConverter.convertVertexToRecord(getEntityCacheManager(), entity, result.iterator().next());

        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return null;
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    public Set<Record> loadDirtyRecords(Entity entity, int maxResults) {
        String query = QueryGenerator.generateDirtyRecordQueryPaged(entity, maxResults);
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        Set<Record> recordSet = new HashSet<Record>();
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return recordSet;
            }
            List<Record> records = OrientdbConverter.convertODocumentToRecord(getEntityCacheManager(), entity, result);
            recordSet.addAll(records);
            return recordSet;
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return recordSet;
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    public List<Record> loadRecords(Entity entity, int firstResult, int maxResults) {
        String query = QueryGenerator.generateRecordQueryPaged(entity, firstResult, maxResults);
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
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
                db.getRawGraph().close();
            }
        }
    }

    public List<ODocument> executeQuery(Entity entity, String query) {
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        if (log.isDebugEnabled()) {
            log.debug("Looking for clusterID  of entity " + entity + " using entity store " + entityStore);
        }
        Integer clusterId = entityStore.getClusterId(entity.getName());
        if (clusterId == null) {
            log.error("Unable to identify cluster ID for class " + entity.getName() + " in entity store " + entityStore);
            throw new RuntimeException("Unable to identify cluster ID for class " + entity.getName());
        }
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
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
                db.getRawGraph().close();
            }
        }
    }

    public void executeQueryAsync(Entity entity, String query, AsyncQueryCallback callback) {
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        log.warn("Looking for clusterID  of entity " + entity + " using entity store " + entityStore);
        Integer clusterId = entityStore.getClusterId(entity.getName());
        if (clusterId == null) {
            log.error("Unable to identify cluster ID for class " + entity.getName() + " in entity store " + entityStore);
            throw new RuntimeException("Unable to identify cluster ID for class " + entity.getName());
        }
        try {
            db = connect(entityStore);
            db.getRawGraph().command(new OSQLAsynchQuery<ODocument>(query, callback)).execute();
            return;
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return;
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<Record> findRecordsById(Entity entity, Integer clusterId, List<Integer> recordIds) {
        String query = QueryGenerator.generateQueryFromRecordIds(entity, clusterId, recordIds);
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);

            Iterable<Vertex> result = (Iterable<Vertex>) db.command(new OSQLSynchQuery<ODocument>(query)).execute();
            if (result == null) {
                return new ArrayList<Record>();
            }
            List<Vertex> records = new ArrayList<Vertex>();
            for (Vertex v : result) {
                records.add(v);
            }
            return OrientdbConverter.convertVertexToRecord(getEntityCacheManager(), entity, records);
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    public List<Record> findRecordsByAttributes(Entity entity, Record record) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateQueryFromRecord(entity, record, params);
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().command(new OSQLSynchQuery<ODocument>(query)).execute(params);
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
                db.getRawGraph().close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<Record> findRecordsByIdentifier(Entity entity, Identifier identifier) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateRecordQueryByIdentifier(entity, identifier, params);
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            Iterable<Vertex> result = (Iterable<Vertex>) db.command(new OSQLSynchQuery<ODocument>(query)).execute(params);
            if (result == null) {
                return new ArrayList<Record>();
            }
            Set<ODocument> records = OrientdbConverter.extractEntitiesFromIdentifiers(entity, result);
            return  new ArrayList<Record>(OrientdbConverter.convertODocumentToRecord(getEntityCacheManager(), entity, records));
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<Record> findRecordsByIdentifier(Entity entity, Identifier identifier, int firstResult, int maxResults) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateRecordQueryByIdentifier(entity, identifier, params, firstResult, maxResults);
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            Iterable<Vertex> result = (Iterable<Vertex>) db.command(new OSQLSynchQuery<ODocument>(query)).execute(params);
            if (result == null) {
                return new ArrayList<Record>();
            }
            Set<ODocument> records = OrientdbConverter.extractEntitiesFromIdentifiers(entity, result);
            return  new ArrayList<Record>(OrientdbConverter.convertODocumentToRecord(getEntityCacheManager(), entity, records));
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    public List<Record> findRecordsWithoutIdentifierInDomain(Entity entity, IdentifierDomain domain, boolean hasLinks,
            int firstResult, int maxResults) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateRecordQueryNotInIdentifierDomain(entity, domain.getIdentifierDomainId(),
                hasLinks, params, firstResult, maxResults);
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            
            List<ODocument> records = db.getRawGraph().command(new OSQLSynchQuery<ODocument>(query)).execute(params);
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
                db.getRawGraph().close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Long getRecordCount(Entity entity) {
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            Object obj = db.getRawGraph().query(new OSQLSynchQuery<ODocument>("select count(*) as count from "
                    + entity.getName() + " where dateVoided is null"));
            List<ODocument> list = (List<ODocument>) obj;
            Long count = list.get(0).field("count");
            return count;
        } catch (Exception e) {
            log.error("Failed while trying to count the number of instances of entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new Long(0);
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    public Long getRecordCount(Entity entity, Record record) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateCountQueryFromRecord(entity, record, params);
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> list = db.getRawGraph().command(new OSQLSynchQuery<ODocument>(query)).execute(params);
            return list.get(0).field("count");

        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new Long(0);
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Long getRecordCount(Entity entity, Identifier identifier) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateCountQueryFromRecord(entity, identifier, params);
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            Iterable<Vertex> result = (Iterable<Vertex>) db.command(new OSQLSynchQuery<ODocument>(query)).execute(params);
            if (result == null) {
                return 0L;
            }
            Set<ODocument> records = OrientdbConverter.extractEntitiesFromIdentifiers(entity, result);
            return new Long(records.size());

        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new Long(0);
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }
    
    public List<Long> getAllRecordIds(Entity entity) {
        String query = "select @rid from " + entity.getName() + " where dateVoided is null";
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return new ArrayList<Long>();
            }
            List<Long> ids = new ArrayList<Long>();
            for (ODocument odoc : result) {
                ODocument doc = odoc.field("rid");
                ids.add(OrientdbConverter.extractId(doc));
            }
            return ids;
        } catch (Exception e) {
            log.error("Failed while trying to load all record IDs for entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Long>();
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    public List<Record> findRecordsByAttributes(Entity entity, Record record, int firstResult, int maxResults) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateQueryFromRecord(entity, record, params, firstResult, maxResults);
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().command(new OSQLSynchQuery<ODocument>(query)).execute(params);
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
                db.getRawGraph().close();
            }
        }
    }

    public RecordLink loadRecordLink(Entity entity, String recordLinkId) {
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        recordLinkId = addClusterIdIfMissing(entityStore, RECORD_LINK_TYPE, recordLinkId);
        String query = QueryGenerator.generateQueryForRecordLink(recordLinkId);
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
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
                db.getRawGraph().close();
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
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> list = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
            Long count = list.get(0).field("count");

            return count;

        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new Long(0);
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    public List<RecordLink> loadRecordLinks(Entity entity, RecordLinkState state, int firstResult, int maxResults) {

        String query = QueryGenerator.generateQueryForRecordLinks(state, firstResult, maxResults);
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
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
                db.getRawGraph().close();
            }
        }
    }

    public List<RecordLink> loadRecordLinks(Entity entity, Long recordId, RecordLinkState state) {
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        String recordLinkId = addClusterIdIfMissing(entityStore, entity.getName(), recordId.toString());
        String query = QueryGenerator.generateQueryForRecordLinks(entity, recordLinkId, state);
        log.debug("Searching for links associated with record using query " + query);
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
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
                db.getRawGraph().close();
            }
        }
    }

    public List<RecordLink> loadRecordLinks(Entity entity, Long recordId) {
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        String recordLinkId = addClusterIdIfMissing(entityStore, entity.getName(), recordId.toString());
        String query = QueryGenerator.generateQueryForRecordLinks(entity, recordLinkId);
        log.debug("Searching for links associated with record using query " + query);
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
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
                db.getRawGraph().close();
            }
        }
    }

    public boolean classExists(Entity baseEntity, String className) throws ApplicationException {
        if (baseEntity == null || baseEntity.getName() == null || baseEntity.getName().isEmpty()) {
            log.warn("The base entity used to locate the class is invalid: " + baseEntity);
            throw new ApplicationException("The base entity used to locate the class is invalid.");
        }
        EntityStore entityStore = getEntityStoreByName(baseEntity.getName());
        if (entityStore == null) {
            log.warn("The base entity used to locate the class is unknown: " + baseEntity);
            throw new ApplicationException("The base entity used to locate the class is unknown.");
        }
        if (className == null || className.isEmpty()) {
            log.warn("The name of the class to locate is invalid: "  + className);
            throw new ApplicationException("The name of the class to locate is invalid.");
        }
        log.info("Locating in base class " + baseEntity.getName() + " the class name: " + className);
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            return getSchemaManager(baseEntity).isClassDefined(db, className);
        } catch (Exception e) {
            log.error("Failed while trying to locate a class: " + className + " due to " + e, e);
            throw new ApplicationException("Failed while trying to locate a class: " + e.getMessage());
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }
    

    public void createClass(Entity baseEntity, Entity classEntity, String baseClass)
            throws ApplicationException {
        if (baseEntity == null || baseEntity.getName() == null || baseEntity.getName().isEmpty()) {
            log.warn("The base entity used to create a new class is invalid: " + baseEntity);
            throw new ApplicationException("The base entity used to create the new class is invalid.");
        }
        EntityStore entityStore = getEntityStoreByName(baseEntity.getName());
        if (entityStore == null) {
            log.warn("The base entity used to create a new class is unknown: " + baseEntity);
            throw new ApplicationException("The base entity used to create the new class is unknown.");
        }
        if (classEntity == null
                || classEntity.getName() == null
                || classEntity.getName().isEmpty()
                || classEntity.getAttributes() == null
                || classEntity.getAttributes().size() == 0) {
            log.warn("The request to create a new class is not well defined: " + classEntity);
            throw new ApplicationException("The request to create a new class is not well defined.");
        }
        log.info("Creating in base class " + baseEntity.getName() + " the class using definition: " + classEntity);
        OrientBaseGraph db = null;
        try {
            db = connectionManager.connectInitial(entityStore);
            getSchemaManager(baseEntity).createClass(db, classEntity, baseClass);
        } catch (Exception e) {
            log.error("Failed while trying to create a class: " + classEntity + " due to " + e, e);
            throw new ApplicationException("Failed while creating a new class: " + e.getMessage());
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }
    
    public void dropClass(Entity baseEntity, String className)
            throws ApplicationException {
        if (baseEntity == null || baseEntity.getName() == null || baseEntity.getName().isEmpty()) {
            log.warn("The base entity used to drop a class is invalid: " + baseEntity);
            throw new ApplicationException("The base entity used to drop a class is invalid.");
        }
        EntityStore entityStore = getEntityStoreByName(baseEntity.getName());
        if (entityStore == null) {
            log.warn("The base entity used to drop a class is unknown: " + baseEntity);
            throw new ApplicationException("The base entity used to drop a class is unknown.");
        }
        log.info("Dropping in base class " + baseEntity.getName() + " the class: " + className);
        OrientBaseGraph db = null;
        try {
            db = connectionManager.connectInitial(entityStore);
            getSchemaManager(baseEntity).dropClass(db, className);
        } catch (Exception e) {
            log.error("Failed while dropping class " + className + " due to " + e, e);
            throw new ApplicationException("Failed while dropping a class: " + e.getMessage());
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }        
    }
    
    public List<Record> loadRecordLinksById(Entity entity, Long recordId) {
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        String recordStringId = addClusterIdIfMissing(entityStore, entity.getName(), recordId.toString());
        String query = QueryGenerator.generateQueryForRecordLinks(entity, recordStringId);
        log.debug("Searching for links associated with record using query " + query);
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
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
                db.getRawGraph().close();
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
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            if (link.getRecordLinkId() == null) {
                boolean exists = checkIfLinkExists(db, entityStore, link);
                if (exists) {
                    log.warn("Record link already exists in the repository: " + link);
                }
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
                db.getRawGraph().close();
            }
        }
    }

    private boolean checkIfLinkExists(OrientGraph db, EntityStore entityStore, RecordLink link) {
        String leftRid = addClusterIdIfMissing(entityStore, entityStore.getEntityName(),
                link.getLeftRecord().getRecordId().toString());
        String rightRid = addClusterIdIfMissing(entityStore, entityStore.getEntityName(),
                link.getRightRecord().getRecordId().toString());
        String query = "select from (traverse bothe() from " + leftRid + ") where source = " + 
                link.getLinkSource().getLinkSourceId() + " and (out = '" + rightRid + "' or in = '" + rightRid + "')";
        List<ODocument> result = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
        if (result.isEmpty()) {
            return false;
        }
        return true;
    }

    public void removeRecordLink(RecordLink link) {
        Entity entity = link.getLeftRecord().getEntity();
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        String query = QueryGenerator.generateQueryForRecordLink(link.getRecordLinkId());
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return;
            }
            ODocument edge = result.get(0);
            db.getRawGraph().delete(edge);
        } catch (Exception e) {
            log.error("Failed while trying to remove a record link: " + link + " due to " + e, e);
            throw new RuntimeException("Failed to remove a record link: " + e.getMessage());
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    public List<RecordLink> getRecordLinksBySource(Entity entity, LinkSource linkSource, RecordLinkState state) {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        String query = QueryGenerator.generateQueryForRecordLink(linkSource, state);
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return new ArrayList<RecordLink>();
            }
            return OrientdbConverter.convertODocumentToRecordLink(getEntityCacheManager(), entity, result);
        } catch (Exception e) {
            log.error("Failed while trying to get record links due to " + e, e);
            throw new RuntimeException("Failed to get record links due to: " + e.getMessage());
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    public int removeRecordLinksBySource(Entity entity, LinkSource linkSource, RecordLinkState state) {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        String query = QueryGenerator.generateQueryForRecordLink(linkSource, state);
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return 0;
            }
            int count = 0;
            for (ODocument edge : result) {
                db.getRawGraph().delete(edge);
                count++;
            }
            return count;
        } catch (Exception e) {
            log.error("Failed while trying to remove all record links due to " + e, e);
            throw new RuntimeException("Failed to remove all record links due to: " + e.getMessage());
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    private void updateLinkEdges(OrientGraph db, EntityStore entityStore, RecordLink link) {
        String recordLinkId = addClusterIdIfMissing(entityStore, RECORD_LINK_TYPE, link.getRecordLinkId());
        try {
            ORecordId orid = new ORecordId(recordLinkId);
            OrientEdge edge  = db.getEdge(orid);
            if (edge == null) {
                return;
            }
            Map<String,Object> props = prepareEdgeProperties(link);
            for (String key : props.keySet()) {
                edge.setProperty(key, props.get(key));
            }
            db.commit();
        } catch (Exception e) {
            log.error("Failed while trying to update the record link using link: " + link + " due to " + e, e);
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    private void saveLinkAsEdge(OrientGraph db, EntityStore entityStore, RecordLink link) {
        ORID leftOrid = extractORID(entityStore, link.getLeftRecord());
        ORID rightOrid = extractORID(entityStore, link.getRightRecord());
        OrientVertex leftNode = db.getVertex(leftOrid);
        OrientVertex rightNode = db.getVertex(rightOrid);
        Map<String,Object> props = prepareEdgeProperties(link);
        String edgeClassName = getClassName(RECORD_LINK_TYPE);
        OrientEdge edge = leftNode.addEdge(RECORD_LINK_TYPE, rightNode, edgeClassName, null, props);
        db.commit();
        link.setRecordLinkId(edge.getIdentity().toString());
    }

    private Map<String,Object> prepareEdgeProperties(RecordLink link) {
        Map<String,Object> props = new HashMap<String,Object>();
        if (link.getRecordLinkId() == null) {
            props.put(SchemaManager.DATE_CREATED_PROPERTY.getName(), link.getDateCreated());
            props.put(SchemaManager.USER_CREATED_BY_PROPERTY.getName(), link.getUserCreatedBy().getId());
        }
        if (link.getDateReviewed() != null) {
            props.put(SchemaManager.DATE_REVIEWED_PROPERTY.getName(), link.getDateReviewed());
            props.put(SchemaManager.USER_REVIEWED_BY_PROPERTY.getName(), link.getUserReviewedBy().getId());
        }
        props.put(SchemaManager.LINK_WEIGHT_PROPERTY.getName(), link.getWeight());
        props.put(SchemaManager.LINK_VECTOR_PROPERTY.getName(), link.getVector());
        props.put(SchemaManager.LINK_SOURCE_PROPERTY.getName(), link.getLinkSource().getLinkSourceId());
        props.put(SchemaManager.LINK_STATE_PROPERTY.getName(), link.getState().getState());
        return props;
    }

    private void removeEdgesToNode(OrientGraph db, ODocument node) {
        ORID identity = node.getIdentity();
        
        Object outSet = node.field(Constants.VERTEX_IN_PROPERTY);
        Object inSet = node.field(Constants.VERTEX_OUT_PROPERTY);

        if (outSet == null && inSet == null) {
            // Record doesn't have any links
            return;
        }

        removeEdgeOrSetOfEdges(db, outSet, identity);
        removeEdgeOrSetOfEdges(db, inSet, identity);
    }

    @SuppressWarnings("unchecked")
    private void removeEdgeOrSetOfEdges(OrientGraph db, Object nodeOrSet, ORID identity) {
        if (nodeOrSet == null) {
            return;
        }
        try {
            if (nodeOrSet instanceof Set<?>) {
                Set<OIdentifiable> set = (Set<OIdentifiable>) nodeOrSet;            
                for (OIdentifiable oid : set) {
                    if (oid != null) {
                        ODocument edge = (ODocument) oid;
                        db.getRawGraph().delete(edge);
                    }
                }
            } else if (nodeOrSet instanceof ODocument) {
                ODocument edge = (ODocument) nodeOrSet;
                db.getRawGraph().delete(edge);
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


    private void updateIdentifiers(OrientGraph db, ODocument odoc, List<Identifier> newIds, Date now) {
        Set<ODocument> oldIdentifiers = OrientdbConverter.getIdentifiers(odoc.field(Constants.IDENTIFIER_EDGE_TYPE));
        Map<Long, ODocument> oldIdMap = buildMapFromDocumentList(oldIdentifiers);
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
                    db.getRawGraph().save(idoc);
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
                db.getRawGraph().save(idoc);
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
                db.getRawGraph().save(idoc);
            }
        }
        odoc.field(Constants.IDENTIFIER_EDGE_TYPE, oldIdentifiers);
    }

    private ODocument createODocumentFromIdentifier(OrientGraph db, ODocument odoc, Identifier identifier) {
        ODocument idoc = db.getRawGraph().newInstance(IDENTIFIER_TYPE);
        idoc.field(Constants.IDENTIFIER_PROPERTY, identifier.getIdentifier());
        idoc.field(Constants.ENTITY_PROPERTY, odoc);
        idoc.field(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY, identifier.getIdentifierDomainId());
        return idoc;
    }

    private Map<Long, ODocument> buildMapFromDocumentList(Set<ODocument> ids) {
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
        for (String fieldName : record.getPropertyNames()) {
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
        ConnectionManager connectionMgr = getConnectionManager();
        if (schemaManager == null) {
            schemaManager = SchemaManagerFactory.createSchemaManager(connectionMgr);
            schemaManager.setParameter(Constants.DATA_DIRECTORY_KEY,
                    Context.getConfiguration().getAdminConfiguration().getDataDirectory());
        }
        EntityStore store = getEntityStoreByName(entity.getName());
        schemaManager.initializeSchema(entity, store);
        connectionMgr.connect(store);
    }

        
    private EntityStore getEntityStoreByName(String entityName) {
        return schemaManager.getEntityStoreByName(entityName);
    }
    
    private OrientGraph connect(EntityStore entityStore) {
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

    private void populateInternalIdentifierPropertiesForCreate(Map<String,Object> props, Record record) {
        props.put(Constants.DATE_CREATED_PROPERTY, record.get(Constants.DATE_CREATED_PROPERTY));
        props.put(Constants.USER_CREATED_BY_PROPERTY, record.get(Constants.USER_CREATED_BY_PROPERTY));
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
