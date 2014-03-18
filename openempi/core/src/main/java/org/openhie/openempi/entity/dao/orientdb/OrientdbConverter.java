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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openhie.openempi.entity.Constants;
import org.openhie.openempi.entity.RecordCacheManager;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.model.User;

import com.orientechnologies.common.collection.OCollection;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.OClusterPositionLong;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

public class OrientdbConverter
{
    private static Logger log = Logger.getLogger(OrientdbConverter.class);

    public static List<Record> convertODocumentToRecord(RecordCacheManager cache, Entity entity, Collection<ODocument> result) {
        List<Record> list = new ArrayList<Record>(result.size());
        for (ODocument odoc : result) {
            list.add(convertODocumentToRecord(cache, entity, odoc));
        }
        if (log.isTraceEnabled()) {
            log.trace("Converted " + list.size() + " odocs to records.");
        }
        return list;
    }
    
    public static List<Record> convertVertexToRecord(RecordCacheManager cache, Entity entity, List<Vertex> result) {
        List<Record> list = new ArrayList<Record>(result.size());
        for (Vertex vertex : result) {
            list.add(convertVertexToRecord(cache, entity, vertex));
        }
        if (log.isTraceEnabled()) {
            log.trace("Converted " + list.size() + " odocs to records.");
        }
        return list;
    }
/*    
    public static Set<Record> convertVertexToRecord(RecordCacheManager cache, Entity entity, Set<ODocument> result) {
        Set<Record> recordSet = new HashSet<Record>(result.size());
        for (ODocument odoc : result) {
            recordSet.add(convertODocumentToRecord(cache, entity, odoc));
        }
        if (log.isTraceEnabled()) {
            log.trace("Converted " + recordSet.size() + " odocs to records.");
        }
        return recordSet;
    }*/

    public static Record convertVertexToRecord(RecordCacheManager cache, Entity entity, Vertex vertex) {
        Record record = new Record(entity);
        for (String property : record.getPropertyNames()) {
            Object value = vertex.getProperty(property);
            record.set(property, value);
        }
        extractIdAndCluster(vertex, record);
        if (vertex.getProperty(Constants.IDENTIFIER_OUT_PROPERTY) != null) {
            extractIdentifiers(cache, vertex, record);
        }
        return record;
    }

    public static List<RecordLink> convertODocumentToRecordLink(RecordCacheManager cache, Entity entity,
            List<ODocument> result) {
        List<RecordLink> links = new ArrayList<RecordLink>();
        for (ODocument odoc : result) {
            String recordLinkId = odoc.getIdentity().toString();
            RecordLink link = createRecordLinkFromEdge(cache, odoc, recordLinkId);
            links.add(link);
        }
        return links;
    }

    public static List<Record> convertODocumentToLinkedRecords(RecordCacheManager cache, Entity entity, Long recordId,
            List<ODocument> result) {
        List<Record> links = new ArrayList<Record>();

        for (ODocument odoc : result) {
            ODocument lDoc = odoc.field((String) Constants.EDGE_IN_PROPERTY);
            Record record = convertODocumentToRecord(cache, entity, lDoc);
            if (recordId != record.getRecordId()) {
                if (!links.contains(record)) {
                    links.add(record);
                }
            }
            lDoc = odoc.field((String) Constants.EDGE_OUT_PROPERTY);
            record = convertODocumentToRecord(cache, entity, lDoc);
            if (recordId != record.getRecordId()) {
                if (!links.contains(record)) {
                    links.add(record);
                }
            }
        }
        return links;
    }

    /*
    public static Record convertODocumentToRecord(RecordCacheManager cache, Entity entity, ODocument odoc) {
        Record record = new Record(entity);
        for (String property : record.getPropertyNames()) {
            Object value = odoc.field(property);
            record.set(property, value);
        }
        extractIdAndCluster(odoc, record);
        if (odoc.field(Constants.IDENTIFIER_SET_PROPERTY) != null) {
            extractIdentifiers(cache, odoc, record);
        }
        return record;
    }
    */
    public static Record convertODocumentToRecord(RecordCacheManager cache, Entity entity, ODocument odoc) {
        Record record = new Record(entity);
        for (String property : record.getPropertyNames()) {
            Object value = odoc.field(property);
            record.set(property, value);
        }
        extractIdAndCluster(odoc, record);
        extractIdentifiers(cache, odoc, record);
        return record;
    }

    public static RecordLink convertODocumentToRecordLink(RecordCacheManager cache, Entity entity, ODocument odoc) {
        String recordLinkId = odoc.getIdentity().toString();
        RecordLink link = createRecordLinkFromEdge(cache, odoc, recordLinkId);
        ODocument lDoc = odoc.field((String) Constants.EDGE_IN_PROPERTY);
        Record record = convertODocumentToRecord(cache, entity, lDoc);
        link.setLeftRecord(record);
        lDoc = odoc.field((String) Constants.EDGE_OUT_PROPERTY);
        record = convertODocumentToRecord(cache, entity, lDoc);
        link.setRightRecord(record);
        return link;
    }

    private static RecordLink createRecordLinkFromEdge(RecordCacheManager cache, ODocument odoc, String recordLinkId) {
        RecordLink link = new RecordLink();
        link.setDateCreated((Date) odoc.field(Constants.DATE_CREATED_PROPERTY));
        if (odoc.field(Constants.DATE_REVIEWED_PROPERTY) != null) {
            link.setDateReviewed((Date) odoc.field(Constants.DATE_REVIEWED_PROPERTY));
        }
        link.setLinkSource(new LinkSource((Integer) odoc.field(Constants.LINK_SOURCE_PROPERTY)));
        link.setRecordLinkId(recordLinkId);
        link.setState(RecordLinkState.fromString((String) odoc.field(Constants.LINK_STATE_PROPERTY)));
        link.setUserCreatedBy(lookupUserById(cache, odoc, Constants.USER_CREATED_BY_PROPERTY));
        if (odoc.field(Constants.USER_REVIEWED_BY_PROPERTY) != null) {
            link.setUserReviewedBy(lookupUserById(cache, odoc, Constants.USER_REVIEWED_BY_PROPERTY));
        }
        link.setVector((Integer) odoc.field(Constants.LINK_VECTOR_PROPERTY));
        link.setWeight((Double) odoc.field(Constants.LINK_WEIGHT_PROPERTY));
        return link;
    }
    
//    private static RecordLink createRecordLinkFromEdge(RecordCacheManager cache, OrientVertex vertex, String recordLinkId) {
//        RecordLink link = new RecordLink();
//        link.setDateCreated((Date) vertex.getProperty(Constants.DATE_CREATED_PROPERTY));
//        if (vertex.getProperty(Constants.DATE_REVIEWED_PROPERTY) != null) {
//            link.setDateReviewed((Date) vertex.getProperty(Constants.DATE_REVIEWED_PROPERTY));
//        }
//        link.setLinkSource(new LinkSource((Integer) vertex.getProperty(Constants.LINK_SOURCE_PROPERTY)));
//        link.setRecordLinkId(recordLinkId);
//        link.setState(RecordLinkState.fromString((String) vertex.getProperty(Constants.LINK_STATE_PROPERTY)));
//        link.setUserCreatedBy(lookupUserById(cache, vertex, Constants.USER_CREATED_BY_PROPERTY));
//        ODocument odoc = vertex.getRecord();
//        if (odoc.field(Constants.USER_REVIEWED_BY_PROPERTY) != null) {
//            link.setUserReviewedBy(lookupUserById(cache, odoc, Constants.USER_REVIEWED_BY_PROPERTY));
//        }
//        link.setVector((Integer) odoc.field(Constants.LINK_VECTOR_PROPERTY));
//        link.setWeight((Double) odoc.field(Constants.LINK_WEIGHT_PROPERTY));
//        return link;
//    }

    public static String getRidFromRecordId(int clusterId, Long recordId) {
        return "" + clusterId + ":" + recordId;
    }

    public static Long getRecordIdFromOrid(ORID orid) {
        if (orid == null) {
            return null;
        }
        return orid.getClusterPosition().longValueHigh();
    }

    public static ORID getORIDFromRecordId(int clusterId, long recordId) {
        OClusterPositionLong clusterPosition = new OClusterPositionLong(recordId);
        ORID orid = new ORecordId(clusterId, clusterPosition);
        return orid;
    }

    public static List<Identifier> convertODocumentToIdentifier(RecordCacheManager cache,
            Collection<ODocument> identifiers) {
        List<Identifier> list = new ArrayList<Identifier>(identifiers.size());
        for (ODocument odoc : identifiers) {
            list.add(convertODocumentToIdentifier(cache, odoc));
        }
        return list;
    }

    public static Set<ODocument> extractEntitiesFromIdentifiers(Entity entityDef, List<ODocument> results) {
        Set<ODocument> entities = new HashSet<ODocument>();
        int entityVersionId = entityDef.getEntityVersionId().intValue();

        for (ODocument idoc : results) {
            ODocument entity = idoc.field(Constants.ENTITY_PROPERTY);

            Long id = (Long) entity.field(Constants.ENTITY_VERSION_ID_PROPERTY);
            if (entity.field(Constants.DATE_VOIDED_PROPERTY) == null && entityVersionId == id.intValue()) {
                entities.add(entity);
            }
        }
        return entities;
    }

    private static Identifier convertODocumentToIdentifier(RecordCacheManager cache, ODocument odoc) {
        Identifier id = new Identifier();
        id.setIdentifierId(extractIdAndCluster(odoc));
        convertODocumentToIdentifier(cache, odoc, id);
        return id;
    }

    private static void extractIdentifiers(RecordCacheManager cache, Vertex vertex, Record record) {
        Iterable<Vertex> identifiers = vertex.getVertices(Direction.OUT, Constants.IDENTIFIER_EDGE_TYPE);
        if (identifiers == null) {
            return;
        }
        for (Vertex idVertex : identifiers) {
            if (idVertex.getProperty(Constants.DATE_VOIDED_PROPERTY) != null) {
                continue;
            }
            Identifier identifier = new Identifier();
            identifier.setIdentifierId(extractId(idVertex));
            convertVertexToIdentifier(cache, (OrientVertex) idVertex, identifier);
            identifier.setRecord(record);
            record.addIdentifier(identifier);
        }
        /*
        Set<Object> obj = (Set<Object>) ((OrientVertex) vertex).getRecord().field(Constants.IDENTIFIER_OUT_PROPERTY);
        if (obj != null && obj.size() > 0 && obj.iterator().next() instanceof ODocument) {
            Set<ODocument> docs = (Set<ODocument>) ((OrientVertex) vertex).getRecord().field(Constants.IDENTIFIER_OUT_PROPERTY);
            for (ODocument odoc : docs) {
                if (odoc.field(Constants.DATE_VOIDED_PROPERTY) != null) {
                    continue;
                }
                Identifier identifier = new Identifier();
                identifier.setIdentifierId(extractId(odoc));
                convertODocumentToIdentifier(cache, odoc, identifier);
                identifier.setRecord(record);
                record.addIdentifier(identifier);
            }
            return;
        }
        Set<OrientVertex> docs = (Set<OrientVertex>) ((OrientVertex) vertex).getRecord().field(Constants.IDENTIFIER_OUT_PROPERTY);
        for (OrientVertex odoc: docs) {
            if (odoc.getProperty(Constants.DATE_VOIDED_PROPERTY) != null) {
                continue;
            }
            Identifier identifier = new Identifier();
            identifier.setIdentifierId(extractId(odoc));
            convertVertexToIdentifier(cache, odoc, identifier);
            identifier.setRecord(record);
            record.addIdentifier(identifier);
        }*/
    }

//    private static void convertVertexToIdentifier(RecordCacheManager cache, Vertex vertex, Identifier identifier) {
//        identifier.setIdentifier((String) vertex.getProperty(Constants.IDENTIFIER_PROPERTY));
//        identifier.setDateCreated((Date) vertex.getProperty(Constants.DATE_CREATED_PROPERTY));
//        identifier.setDateVoided((Date) vertex.getProperty(Constants.DATE_VOIDED_PROPERTY));
//        Integer identifierDomainId = (Integer) vertex.getProperty(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY);
//        IdentifierDomain domain = cache.getIdentifierDomain(identifierDomainId);
//        if (domain != null) {
//            identifier.setIdentifierDomain(domain);
//        } else {
//            identifier.setIdentifierDomainId(identifierDomainId);
//        }
//        identifier.setUserCreatedBy(lookupUserById(cache, vertex, Constants.USER_CREATED_BY_PROPERTY));
//        identifier.setUserVoidedBy(lookupUserById(cache, vertex, Constants.USER_VOIDED_BY_PROPERTY));
//    }

    private static User lookupUserById(RecordCacheManager cache, Vertex vertex, String fieldName) {
        User user = null;
        Long userId = (Long) vertex.getProperty(fieldName);
        if (userId != null) {
            user = cache.getUser(userId);
        }
        return user;
    }

    private static User lookupUserById(RecordCacheManager cache, ODocument idoc, String fieldName) {
        User user = null;
        Long userId = (Long) idoc.field(fieldName);
        if (userId != null) {
            user = cache.getUser(userId);
        }
        return user;
    }
    
    public static void extractIdAndCluster(Vertex vertex, Record record) {
        OrientVertex oVertex = (OrientVertex) vertex;
        ORecordId orid = (ORecordId) oVertex.getIdentity();
        record.set(Constants.ORIENTDB_CLUSTER_ID_KEY, orid.getClusterId());
        record.setRecordId(orid.getClusterPosition().longValueHigh());
        if (log.isTraceEnabled()) {
            log.trace("Extracted the values " + record.get(Constants.ORIENTDB_CLUSTER_ID_KEY) + " and "
                    + record.getRecordId() + " from ODocument " + orid.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public static Set<ODocument> getIdentifiers(Object obj) {
        Set<ODocument> idoc = new HashSet<ODocument>();
        if (obj == null) {
            return idoc;
        }
        if (obj instanceof OCollection<?>) {
            OCollection<OIdentifiable> col = (OCollection<OIdentifiable>) obj;
            for (OIdentifiable iobj : col) {
                idoc.add((ODocument) iobj);
            }
            return idoc;
        } else if (obj instanceof ODocument) {
            idoc.add((ODocument) obj);
        }
        log.debug("Obj is of type: " + obj.getClass());
        return idoc;
    }
    
    private static void extractIdentifiers(RecordCacheManager cache, ODocument odoc, Record record) {
        Set<ODocument> ids = getIdentifiers(odoc.field(Constants.IDENTIFIER_OUT_PROPERTY));
        for (ODocument idoc : ids) {
            if (idoc.field(Constants.DATE_VOIDED_PROPERTY) != null) {
                continue;
            }
            Identifier identifier = new Identifier();
            identifier.setIdentifierId(extractIdAndCluster(idoc));
            convertODocumentToIdentifier(cache, idoc, identifier);
            identifier.setRecord(record);
            record.addIdentifier(identifier);
        }
    }

    private static void convertVertexToIdentifier(RecordCacheManager cache, OrientVertex vertex, Identifier identifier) {
        identifier.setIdentifier((String) vertex.getProperty(Constants.IDENTIFIER_PROPERTY));
        identifier.setDateCreated((Date) vertex.getProperty(Constants.DATE_CREATED_PROPERTY));
        identifier.setDateVoided((Date) vertex.getProperty(Constants.DATE_VOIDED_PROPERTY));
        Integer identifierDomainId = (Integer) vertex.getProperty(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY);
        IdentifierDomain domain = cache.getIdentifierDomain(identifierDomainId);
        if (domain != null) {
            identifier.setIdentifierDomain(domain);
        } else {
            identifier.setIdentifierDomainId(identifierDomainId);
        }
        identifier.setUserCreatedBy(lookupUserById(cache, vertex, Constants.USER_CREATED_BY_PROPERTY));
        identifier.setUserVoidedBy(lookupUserById(cache, vertex, Constants.USER_VOIDED_BY_PROPERTY));
    }

    private static void convertODocumentToIdentifier(RecordCacheManager cache, ODocument idoc, Identifier identifier) {
        identifier.setIdentifier((String) idoc.field(Constants.IDENTIFIER_PROPERTY));
        identifier.setDateCreated((Date) idoc.field(Constants.DATE_CREATED_PROPERTY));
        identifier.setDateVoided((Date) idoc.field(Constants.DATE_VOIDED_PROPERTY));
        Integer identifierDomainId = (Integer) idoc.field(Constants.IDENTIFIER_DOMAIN_ID_PROPERTY);
        IdentifierDomain domain = cache.getIdentifierDomain(identifierDomainId);
        if (domain != null) {
            identifier.setIdentifierDomain(domain);
        } else {
            identifier.setIdentifierDomainId(identifierDomainId);
        }
        identifier.setUserCreatedBy(lookupUserById(cache, idoc, Constants.USER_CREATED_BY_PROPERTY));
        identifier.setUserVoidedBy(lookupUserById(cache, idoc, Constants.USER_VOIDED_BY_PROPERTY));
    }
    
    public static Long extractId(Vertex vertex) {
        OrientVertex oVertex = (OrientVertex) vertex;
        ORID orid = oVertex.getIdentity();
        return orid.getClusterPosition().longValueHigh();
    }

    public static void extractIdAndCluster(ODocument odoc, Record record) {
        ORID orid = odoc.getIdentity();
        record.set(Constants.ORIENTDB_CLUSTER_ID_KEY, orid.getClusterId());
        record.setRecordId(orid.getClusterPosition().longValueHigh());
        if (log.isTraceEnabled()) {
            log.trace("Extracted the values " + record.get(Constants.ORIENTDB_CLUSTER_ID_KEY) + " and "
                    + record.getRecordId() + " from ODocument " + orid.toString());
        }
    }
    
    private static Long extractIdAndCluster(ODocument odoc) {
        ORID orid = odoc.getIdentity();
        return orid.getClusterPosition().longValueHigh();
    }
    
    public static Long extractId(ODocument odoc) {
        ORID orid = odoc.getIdentity();
        return orid.getClusterPosition().longValueHigh();
    }
    
//    private static void extractIdAndCluster(Vertex vertex, Identifier identifier) {
//        OrientVertex oVertex = (OrientVertex) vertex;
//        ORID orid = oVertex.getIdentity();
//        identifier.setIdentifierId(orid.getClusterPosition().longValueHigh());
//    }

    public static ORID extractOrid(Record record) {
        int clusterId = (Integer) record.get(Constants.ORIENTDB_CLUSTER_ID_KEY);
        long clusterPosition = record.getRecordId();
        OClusterPositionLong clusterPos = new OClusterPositionLong(clusterPosition);
        ORID orid = new ORecordId(clusterId, clusterPos);
        return orid;
    }

    public static String createLinkId(String leftEdgeId, String rightEdgeId) {
        return leftEdgeId + "." + rightEdgeId;
    }
}
