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
import java.util.concurrent.BlockingQueue;

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

import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLAsynchQuery;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
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
            db.commit();
            return OrientdbConverter.convertVertexToRecord(getEntityCacheManager(), entity, instance);
        } catch (Exception e) {
            log.error("Failed while trying to save an instance of entity: " + entityStore.getEntityName() + " due to "
                    + e, e);
            throw new RuntimeException("Failed to save an entity: " + e.getMessage());
        } finally {
        	close(entityStore, db);
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
            db.commit();
            return saved;
        } catch (Exception e) {
            log.error("Failed while trying to save a set of records of entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            throw new RuntimeException("Failed to save a set of records: " + e.getMessage());
        } finally {
        	close(entityStore, db);
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
     * @param commit 
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
        instance.save();
        for (Identifier identifier : record.getIdentifiers()) {
            OrientVertex vertex = (OrientVertex) saveIdentifier(db, instance, record, identifier);
            OrientEdge edge = db.addEdge(null, instance, vertex, Constants.IDENTIFIER_EDGE_TYPE);
            edge.save();
            if (log.isTraceEnabled()) {
                log.trace("Created vertex for identifier " + vertex + " and edge " + edge);
            }
        }
        instance.save();
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

    public void updateRecords(Entity entity, List<Record> records) throws ApplicationException {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            User changedBy = Context.getUserContext().getUser();
            Date now = new Date();
            for (Record record : records) {
                ORID orid = extractORID(entityStore, record);
                OrientVertex vertex = db.getVertex(orid);
                if (vertex == null) {
                    log.debug("Attempted to update an object that is not known in the database: " + orid);
                    continue;
                }
                updateIdentifiers(db, vertex, record, now);
                updateVertexWithRecord(vertex, record);
                boolean dirty = record.isDirty();
                if (dirty == false) {
                    vertex.setProperty(Constants.DIRTY_RECORD_PROPERTY, Boolean.FALSE);
                } else {
                    vertex.setProperty(Constants.DIRTY_RECORD_PROPERTY, Boolean.TRUE);
                }                
                vertex.setProperty(Constants.DATE_CHANGED_PROPERTY, now);
                vertex.setProperty(Constants.USER_CHANGED_BY_PROPERTY, changedBy.getId());
                vertex.save();
            }
            db.commit();
        } catch (Exception e) {
            log.error("Failed while trying to update a batch of records of entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            throw new RuntimeException("Failed to update an entity: " + e.getMessage());
        } finally {
        	close(entityStore, db);
        }
    }

    public void deleteRecord(Entity entity, Record record) throws ApplicationException {
        validateRecordIdentity(record);
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            ORID orid = extractORID(entityStore, record);
            OrientVertex vertex = db.getVertex(orid);
            if (vertex == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Attempted to delete a record that is not known in the database. Operation was ignored.");
                }
                return;
            }
            User deletedBy = Context.getUserContext().getUser();
            Date now = new Date();
            vertex.setProperty(Constants.DATE_VOIDED_PROPERTY, now);
            vertex.setProperty(Constants.USER_VOIDED_BY_PROPERTY, deletedBy.getId());
            
            Iterable<Edge> edges = vertex.getEdges(Direction.OUT, Constants.IDENTIFIER_EDGE_TYPE);
            for (Edge edge : edges) {
                OrientEdge theEdge = (OrientEdge) edge;
                OrientVertex ivertex = theEdge.getVertex(Direction.IN);
                ivertex.setProperty(Constants.DATE_VOIDED_PROPERTY, now);
                ivertex.setProperty(Constants.USER_VOIDED_BY_PROPERTY, deletedBy.getId());
                ivertex.save();
            }

            edges = vertex.getEdges(Direction.BOTH, Constants.RECORD_LINK_TYPE);
            for (Edge edge : edges) {
                db.removeEdge(edge);
            }
            vertex.save();
            db.commit();
        } catch (Exception e) {
            log.error("Failed while trying to delete a record of entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            throw new RuntimeException("Failed to delete the record: " + e.getMessage());
        } finally {
            close(entityStore, db);
        }
    }

    public void removeRecord(Entity entity, Record record) throws ApplicationException {
        validateRecordIdentity(record);
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            ORID orid = extractORID(entityStore, record);
            OrientVertex vertex = db.getVertex(orid);
            if (vertex == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Attempted to remove a record that is not known in the database. Operation was ignored.");
                }
                return;
            }
            Iterable<Edge> edges = vertex.getEdges(Direction.OUT, Constants.IDENTIFIER_EDGE_TYPE);
            for (Edge edge : edges) {
                OrientEdge theEdge = (OrientEdge) edge;
                OrientVertex ivertex = theEdge.getVertex(Direction.IN);
                db.removeVertex(ivertex);
            }
            
            edges = vertex.getEdges(Direction.BOTH, Constants.RECORD_LINK_TYPE);
            for (Edge edge : edges) {
                db.removeEdge(edge);
            }
            db.removeVertex(vertex);
            db.commit();
        } catch (Exception e) {
            log.error("Failed while trying to remove an instance of entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            throw new RuntimeException("Failed to remove an entity: " + e.getMessage());
        } finally {
            close(entityStore, db);
        }
    }
    
    public Record updateRecord(Entity entity, Record record) throws ApplicationException {
        validateRecordIdentity(record);
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        OrientGraph db = null;
        try {
            db = connect(entityStore);
            ORID orid = extractORID(entityStore, record);
            OrientVertex vertex = db.getVertex(orid);
            if (vertex == null) {
                log.debug("Attempted to update an object that is not known in the database.");
                return null;
            }
            User changedBy = Context.getUserContext().getUser();
            Date now = new Date();
            updateIdentifiers(db, vertex, record, now);
            updateVertexWithRecord(vertex, record);
            boolean dirty = record.isDirty();
            if (dirty == false) {
                vertex.setProperty(Constants.DIRTY_RECORD_PROPERTY, Boolean.FALSE);
            } else {
                vertex.setProperty(Constants.DIRTY_RECORD_PROPERTY, Boolean.TRUE);
            }
            vertex.setProperty(Constants.DATE_CHANGED_PROPERTY, now);
            vertex.setProperty(Constants.USER_CHANGED_BY_PROPERTY, changedBy.getId());
            vertex.save();
            db.commit();
            return OrientdbConverter.convertVertexToRecord(getEntityCacheManager(), entity, vertex);
        } catch (Exception e) {
            log.error("Failed while trying to update an instance of entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            throw new RuntimeException("Failed to update an entity: " + e.getMessage());
        } finally {
        	close(entityStore, db);
        }
    }
    
    private void updateVertexWithRecord(OrientVertex vertex, Record record) {
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
            if (value == null) {
                continue;
            }
            vertex.setProperty(fieldName, value);
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
        	close(entityStore, db);
        }
    }

    public Map<String,Object> loadObject(Entity entity, String recordId) {
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        String query = "select from " + recordId;
        try {
            db = connect(entityStore);
            List<ODocument> result = db.getRawGraph().query(new OSQLSynchQuery<ODocument>(query));
            if (result == null || result.size() == 0) {
                return null;
            }
            ODocument odoc = result.get(0);
            Map<String,Object> record = convertODocumentToMap(odoc);   
            return record;
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return null;
        } finally {
        	close(entityStore, db);
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
        	close(entityStore, db);
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
        	close(entityStore, db);
        }
    }

    public List<Record> loadRecords(Entity entity, int firstResult, int maxResults) {
        String query = QueryGenerator.generateRecordQueryPaged(entity, firstResult, maxResults);
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            @SuppressWarnings("unchecked")
            Iterable<Vertex> result = (Iterable<Vertex>) db.command(new OSQLSynchQuery<ODocument>(query));
            return OrientdbConverter.convertVertexToRecord(getEntityCacheManager(), entity, result);
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
        	close(entityStore, db);
        }
    }

    public List<Map<String,Object>> executeQuery(Entity entity, String query) {
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
            List<Map<String,Object>> records = new ArrayList<Map<String,Object>>(result.size());
            for (ODocument odoc : result) {
                Map<String, Object> record = convertODocumentToMap(odoc);
                records.add(record);
            }
            return records;
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return null;
        } finally {
        	close(entityStore, db);
        }
    }

    private Map<String, Object> convertODocumentToMap(ODocument odoc) {
        Map<String,Object> record = new HashMap<String,Object>();
        record.put(Constants.RECORDID_KEY, odoc.getIdentity().toString());
        record.put(Constants.CLUSTERID_KEY, odoc.getIdentity().getClusterId());
        record.put(Constants.CLUSTERPOSITION_KEY, odoc.getIdentity().getClusterPosition());
        for (String fieldName : odoc.fieldNames()) {
            record.put(fieldName, odoc.field(fieldName));
        }
        return record;
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
        	close(entityStore, db);
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
            return OrientdbConverter.convertVertexToRecord(getEntityCacheManager(), entity, result);
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
        	close(entityStore, db);
        }
    }

    public List<Record> findRecordsByAttributes(Entity entity, Record record) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateQueryFromRecord(entity, record, params);
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            @SuppressWarnings("unchecked")
            Iterable<Vertex> result = (Iterable<Vertex>) db.command(new OSQLSynchQuery<ODocument>(query)).execute(params);
            return OrientdbConverter.convertVertexToRecord(getEntityCacheManager(), entity, result);
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
        	close(entityStore, db);
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
            Set<Vertex> records = OrientdbConverter.extractRecordsFromIdentifiers(entity, result);
            return  new ArrayList<Record>(OrientdbConverter.convertVertexToRecord(getEntityCacheManager(), entity, records));
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
        	close(entityStore, db);
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
            Set<Vertex> records = OrientdbConverter.extractRecordsFromIdentifiers(entity, result);
            return  new ArrayList<Record>(OrientdbConverter.convertVertexToRecord(getEntityCacheManager(), entity, records));
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
        	close(entityStore, db);
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
        	close(entityStore, db);
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
        	close(entityStore, db);
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
        	close(entityStore, db);
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
            Set<Vertex> records = OrientdbConverter.extractRecordsFromIdentifiers(entity, result);
            return new Long(records.size());

        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new Long(0);
        } finally {
        	close(entityStore, db);
        }
    }
    
    public List<Long> getRecordIds(Entity entity, int firstResult, int maxResults) {
        String query = "select @rid from " + entity.getName() + " where dateVoided is null skip " + firstResult + 
                " limit " + maxResults;
        return loadRecordIds(entity, query);
    }

    public List<Long> getAllRecordIds(Entity entity) {
        String query = "select @rid from " + entity.getName() + " where dateVoided is null";
        return loadRecordIds(entity, query);
    }

    public void loadRecords(Entity entity, List<BlockingQueue<Record>> queues, int blockSize) {
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            ORID last = new ORecordId();
            db = connect(entityStore);
            String queryString = "select from " + entity.getName() + " where @rid > ? limit " + blockSize;
            final OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(queryString);
            List<ODocument> resultset = db.getRawGraph().query(query, last);
            int totalCount=0;
            while (!resultset.isEmpty()) {
                last = resultset.get(resultset.size() - 1).getIdentity();
                List<Record> records = OrientdbConverter.
                        convertODocumentToRecord(getEntityCacheManager(), entity, resultset);
                addToQueues(queues, records);
                totalCount += records.size();
                if (totalCount % 10000 == 0) {
                    log.warn("Producer loaded " + totalCount + " records.");
                }
                resultset = db.getRawGraph().query(query, last);
            }
        } catch (Exception e) {
            log.error("Failed while trying to load all record for entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
        } finally {
            close(entityStore, db);
        }
    }
    
    private void addToQueues(List<BlockingQueue<Record>> queues, List<Record> records) throws InterruptedException {
        for (Record record : records) {
            for (BlockingQueue<Record> queue : queues) {
                queue.put(record);
            }
        }
    }

    private List<Long> loadRecordIds(Entity entity, String query) {
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
        	close(entityStore, db);
        }
    }

    public List<Record> findRecordsByAttributes(Entity entity, Record record, int firstResult, int maxResults) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = QueryGenerator.generateQueryFromRecord(entity, record, params, firstResult, maxResults);
        OrientGraph db = null;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        try {
            db = connect(entityStore);
            @SuppressWarnings("unchecked")
            Iterable<Vertex> result = (Iterable<Vertex>) db.command(new OSQLSynchQuery<ODocument>(query)).execute(params);
            return OrientdbConverter.convertVertexToRecord(getEntityCacheManager(), entity, result);
        } catch (Exception e) {
            log.error("Failed while trying to query the system using entity: " + entityStore.getEntityName()
                    + " due to " + e, e);
            return new ArrayList<Record>();
        } finally {
        	close(entityStore, db);
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
        	close(entityStore, db);
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
        	close(entityStore, db);
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
        	close(entityStore, db);
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
        	close(entityStore, db);
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
        	close(entityStore, db);
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
        	close(entityStore, db);
        }
    }

    public void createIndexes(Entity entity) throws ApplicationException {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        if (entityStore == null) {
            log.warn("The entity against which indexes are to be created is unknown: " + entity);
            throw new ApplicationException("The entity against which indexes are to be created is unknown.");
        }
        log.info("Creating indexes for class " + entity.getName());
        OrientBaseGraph db = null;
        try {
            db = connectionManager.connectInitial(entityStore);
            getSchemaManager(entity).createIndexes(entity, db);
        } catch (Exception e) {
            log.error("Failed while trying to create indexes for class: " + entity.getName() + " due to " + e, e);
            throw new ApplicationException("Failed while creating indexes for an entity: " + e.getMessage());
        } finally {
        	closeInitial(entityStore, db);
        }
    }

    public void dropIndexes(Entity entity) throws ApplicationException {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        if (entityStore == null) {
            log.warn("The entity whose indexes are to be dropped is unknown: " + entity);
            throw new ApplicationException("The entity whose indexes are to be dropped is unknown.");
        }
        log.info("Dropping indexes for class " + entity.getName());
        OrientBaseGraph db = null;
        try {
            db = connectionManager.connectInitial(entityStore);
            getSchemaManager(entity).removeIndexes(entity, db);
        } catch (Exception e) {
            log.error("Failed while trying to drop indexes for class: " + entity.getName() + " due to " + e, e);
            throw new ApplicationException("Failed while drop indexes for an entity: " + e.getMessage());
        } finally {
        	closeInitial(entityStore, db);
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
        	closeInitial(entityStore, db);
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
        	closeInitial(entityStore, db);
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
        	close(entityStore, db);
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
        	close(entityStore, db);
        }
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
        	close(entityStore, db);
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
        	close(entityStore, db);
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
        	close(entityStore, db);
        }
    }

    public synchronized void initializeStore(Entity entity) {
    	initializeStore(entity, Context.getConfiguration().getAdminConfiguration().getDataDirectory());
    }
    
    public synchronized void initializeStore(Entity entity, String dataDirectory) {
        ConnectionManager connectionMgr = getConnectionManager();
        if (schemaManager == null) {
            schemaManager = SchemaManagerFactory.createSchemaManager(connectionMgr);
            schemaManager.setParameter(Constants.DATA_DIRECTORY_KEY, dataDirectory);
        }
        EntityStore store = getEntityStoreByName(entity.getName());
        schemaManager.initializeSchema(entity, store);
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
    
    private OrientVertex saveIdentifier(OrientGraph db, Vertex owner, Record record, Identifier identifier) {
        Map<String,Object> properties = new HashMap<String,Object>();
        if (identifier.getRecord() == null) {
            identifier.setRecord(record);
        }
        properties.put(Constants.IDENTIFIER_PROPERTY, identifier.getIdentifier());
        properties.put(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY, identifier.getIdentifierDomainId());
        populateInternalIdentifierPropertiesForCreate(properties, record);
        return db.addVertex(getClassName(IDENTIFIER_TYPE), properties);
   }

    private void updateIdentifiers(OrientGraph db, OrientVertex vertex, Record record, Date now) {
        List<Identifier> newIds = record.getIdentifiers();
        Iterable<Edge> edges = vertex.getEdges(Direction.OUT, Constants.IDENTIFIER_EDGE_TYPE);
        Set<OrientVertex> oldIds = new HashSet<OrientVertex>();
        for (Edge edge : edges) {
            log.warn("Found edge: " + (OrientEdge) edge);
            OrientEdge theEdge = (OrientEdge) edge;
            oldIds.add(theEdge.getVertex(Direction.IN));
        }
        
        Map<Long, OrientVertex> oldIdMap = buildMapFromIdentifierVertexSet(oldIds);
        Map<Long, Identifier> newIdMap = buildMapFromList(newIds);

        // Case 1. An element from the old identifier list was deleted
        if (oldIds.size() > 0) {
            for (OrientVertex ivertex : oldIds) {
                Long id = OrientdbConverter.extractId(ivertex);
                if (newIdMap.get(id) == null) {
                    ivertex.setProperty(Constants.DATE_VOIDED_PROPERTY, now);
                    ivertex.setProperty(Constants.USER_VOIDED_BY_PROPERTY, Context.getUserContext().getUser().getId());
                    log.debug("An identifier with identifier " + ivertex.getProperty(Constants.IDENTIFIER_PROPERTY)
                            + " was deleted.");
                    ivertex.save();
                }
            }
        }

        record.set(Constants.DATE_CREATED_PROPERTY, vertex.getProperty(Constants.DATE_CREATED_PROPERTY));
        record.set(Constants.USER_CREATED_BY_PROPERTY, vertex.getProperty(Constants.USER_CREATED_BY_PROPERTY));

        // Case 2. An new identifier was added
        for (Identifier id : newIds) {
            if (id.getIdentifierId() == null || oldIdMap.get(id.getIdentifierId()) == null) {
                OrientVertex ivertex = saveIdentifier(db, vertex, record, id);
                ivertex.save();
                OrientEdge edge = db.addEdge(null, vertex, ivertex, Constants.IDENTIFIER_EDGE_TYPE);
                edge.save();
                if (log.isTraceEnabled()) {
                    log.trace("Created vertex for identifier " + vertex + " and edge " + edge);
                }
                oldIds.add(ivertex);
            }
        }

        // Case 3. An element was updated
        for (Identifier id : newIds) {
            OrientVertex ivertex = oldIdMap.get(id.getIdentifierId());
            if (ivertex != null) {
                ivertex.setProperty(Constants.IDENTIFIER_PROPERTY, id.getIdentifier());
                if (id.getIdentifierDomain() != null) {
                    ivertex.setProperty(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY, id.getIdentifierDomain().getIdentifierDomainId());
                } else {
                    ivertex.setProperty(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY, id.getIdentifierDomainId());                    
                }
                log.debug("An identifier with identifier " + ivertex.getProperty(Constants.IDENTIFIER_PROPERTY)
                        + " was updated.");
                ivertex.save();
            }
        }
    }

    private Map<Long, OrientVertex> buildMapFromIdentifierVertexSet(Set<OrientVertex> ids) {
        Map<Long, OrientVertex> map = new HashMap<Long, OrientVertex>();
        if (ids != null && ids.size() > 0) {
            for (OrientVertex v : ids) {
                Long id = OrientdbConverter.extractId(v);
                map.put(id, v);
            }
        }
        return map;
    }
    
    private Map<Long, Identifier> buildMapFromList(List<Identifier> ids) {
        Map<Long, Identifier> map = new HashMap<Long, Identifier>();
        if (ids == null || ids.size() == 0) {
            return map;
        }
        for (Identifier id : ids) {
            if (id.getIdentifierId() != null) {
                map.put(id.getIdentifierId(), id);
            }
        }
        return map;
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

    public EntityStore getEntityStoreByName(String entityName) {
        return schemaManager.getEntityStoreByName(entityName);
    }
    
    private OrientGraph connect(EntityStore entityStore) {
        return getConnectionManager().connect(entityStore);
    }

    private void close(EntityStore entityStore, OrientBaseGraph db) {
    	if (db != null) {
    		getConnectionManager().close(entityStore, db);
    	}
    }

    private void closeInitial(EntityStore entityStore, OrientBaseGraph db) {
    	if (db != null) {
    		getConnectionManager().closeInternal(entityStore, db);
    	}
    }
    
    private String getClassName(String name) {
        return "class:" + name;
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
