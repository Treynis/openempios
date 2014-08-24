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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.Constants;
import org.openhie.openempi.model.AttributeDatatype;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.notification.EventObservable;
import org.openhie.openempi.notification.ObservationEventType;

import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.collate.OCaseInsensitiveCollate;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClassImpl;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OPropertyImpl;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.storage.OStorage;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;

public abstract class SchemaManagerAbstract extends Constants implements SchemaManager, Observer
{
    protected Logger log = Logger.getLogger(getClass());
    protected static Map<String,Object> params = new HashMap<String,Object>();    
    protected Map<String, EntityStore> storeByName = new HashMap<String, EntityStore>();
    protected ConnectionManager connectionManager;

    private static Map<String, InternalAttribute> internalAttributeMap = new HashMap<String, InternalAttribute>();

    static {
        for (InternalAttribute attribute : INTERNAL_ATTRIBUTES) {
            internalAttributeMap.put(attribute.getName(), attribute);
        }
        InternalAttribute clusterAttribute = new InternalAttribute(Constants.ORIENTDB_CLUSTER_ID_KEY, null, false); 
        internalAttributeMap.put(clusterAttribute.getName(), clusterAttribute);
    }
   
    SchemaManagerAbstract(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        Context.registerObserver(this, ObservationEventType.ENTITY_ADD_EVENT);
        Context.registerObserver(this, ObservationEventType.ENTITY_ATTRIBUTE_UPDATE_EVENT);        
    }
    
    public void initializeSchema(Entity entity, EntityStore entityStore) {
        OrientBaseGraph db = null;
        try {
            db = connectionManager.connectInitial(entityStore);            
            if (!isClassDefined(db, entity.getName())) {
                createDatabase(entityStore, db);
                initializeClasses(entity, entityStore, db);
            }
            storeClusterIds(entityStore, db);
        } catch (Exception e) {
            log.error("Failed while initializing the store: " + e, e);
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }
    }

    public boolean isClassDefined(OrientBaseGraph db, String className) {
        refreshSchema(db);
        Collection<OClass> classes = db.getRawGraph().getMetadata().getSchema().getClasses();
        log.debug("The repository currently has " + classes.size() + " classes defined:");
        for (OClass oclass : classes) {
            log.info("Class name: " + oclass.getName() + " in cluster " + oclass.getDefaultClusterId());
            if (oclass.getName().equalsIgnoreCase(className)) {
                return true;
            }
        }
        return false;
    }
    
    public void update(Observable o, Object eventData) {
        if (!(o instanceof EventObservable) || eventData == null || !(eventData instanceof Entity)) {
            log.warn("Received unexpected event with data of " + eventData);
            return;
        }
        Entity entity = (Entity) eventData;
        EventObservable event = (EventObservable) o;
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        if (event.getType() == ObservationEventType.ENTITY_ADD_EVENT) {
            log.debug("A new entity was added; we need to initialize the schema for it: " + entity);
            log.info("Initializing schema for new entity " + entity.getName());
            initializeSchema(entity, entityStore);
        } else if (event.getType() == ObservationEventType.ENTITY_ATTRIBUTE_UPDATE_EVENT) {
            log.debug("An entity was modified; we need to update the schema for it: " + entity);
            log.info("Synchronizing schema with updated entity " + entity.getName());
            synchronizeSchema(entity, entityStore);
        }
    }
    
    public void shutdownStore(Entity entity) {
        log.info("Shutting down store for entity: " + entity.getName());
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        if (entityStore == null) {
            log.warn("Received request to shutdown an unknown store.");
            return;
        }

        try {
            connectionManager.shutdown(entityStore);
            final OStorage stg = Orient.instance().getStorage(entityStore.getStorageName());
            if (stg != null) {
                stg.close();
                boolean done = false;
                int count = 0;
                while (!done) {
                    done = stg.getStatus().equals(OStorage.STATUS.CLOSED);
                    log.info("Status of storage " + stg.getClass() + " after close is: " + stg.getStatus());
                    try {
                        count++;
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                    if (count > 5) {
                        log.info("Storage has not shutdown after " + count + " retries.");
//                        stg.close(true, true);
                        done = true;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed while shutting down the store: " + e, e);
        }
    }
    
    public void setParameter(String key, Object value) {
        params.put(key, value);
    }
    
    public Object getParameter(String key) {
        return params.get(key);
    }

    public boolean isInternalAttribute(String fieldName) {
        if (internalAttributeMap.get(fieldName) == null) {
            return false;
        }
        return true;
    }
    
    private void synchronizeSchema(Entity entity, EntityStore entityStore) {
        OrientBaseGraph db = null;
        try { 
            db = connectionManager.connectInitial(entityStore);
            String className = entityStore.getEntityName();
            OClassImpl vertexClass = (OClassImpl) findGraphClass(db, className);
            if (vertexClass == null) {
                log.error("Unable to find the class that corresponds to the entity " + entity.getName() + 
                        " during an update.");
                return;
            }
            
            Map<String,OPropertyImpl> classPropertiesByName = new HashMap<String,OPropertyImpl>();
            
            for (OProperty property : vertexClass.declaredProperties()) {
                classPropertiesByName.put(property.getName(), (OPropertyImpl) property);
            }
            
            for (EntityAttribute attribute : entity.getAttributes()) {
                OPropertyImpl property = classPropertiesByName.get(attribute.getName());
                if (property != null) {
                    continue;
                }
                OType type = getOrientdbType(attribute.getDatatype());
                vertexClass.addPropertyInternal(attribute.getName(), type, null, null);
                log.debug("Adding field " + attribute.getName() + " to class " + className);
                vertexClass.saveInternal();

                if (attribute.getIndexed()) {
                    refreshSchema(db);
                    Collection<String> attributes = new ArrayList<String>();
                    attributes.add(attribute.getName());
                    String indexNamePrefix = INDEX_NAME_PREFIX + entity.getName();
                    createIndex(db, indexNamePrefix, entity.getName(), attributes);
                }
            }
            vertexClass.saveInternal();
            
        } catch (Exception e) {
            log.error("Failed while initializing the store: " + e, e);
        } finally {
            if (db != null) {
                db.getRawGraph().close();
            }
        }        
    }

    public void dropClass(OrientBaseGraph db, String className) {
        if (className == null) {
            return;
        }
        OClass classInternal = findGraphClass(db, className);
        if (classInternal == null)  {
            log.warn("An attempt was made to drop a class that does not exist: " + className);
            return;
        }
        db.command( new OCommandSQL("drop class " + className) ).execute();
//        ((OSchemaProxy) db.getMetadata().getSchema()).dropClassInternal(className);
        log.info("Class " + className + " was dropped.");
    }
    
    public void createClass(OrientBaseGraph db, Entity entity, String baseClassName) {
        if (baseClassName == null) {
            baseClassName = VERTEX_CLASS_NAME;
        }
        if (findGraphClass(db, entity.getName()) != null) {
            log.warn("The class " + entity.getName() + " already exists.");
            return;
        }
        OClass baseClass = findGraphClass(db, baseClassName);
        if (baseClass == null) {
            log.error("Unable to find base class named: " + baseClassName);
            throw new RuntimeException("Invalid base class name " + baseClassName);
        }
        String className = entity.getName();
        int[] clusterIds = null;
        final OClassImpl sourceClass = (OClassImpl) ((OSchemaProxy) db.getRawGraph().getMetadata().getSchema())
                .createClass(className, baseClass, clusterIds);
        log.info("Class " + className + " has been assigned cluster " + sourceClass.getDefaultClusterId());
        sourceClass.saveInternal();
        String indexNamePrefix = INDEX_NAME_PREFIX + entity.getName();
        for (EntityAttribute attribute : entity.getAttributes()) {
            String fieldName = attribute.getName();
            OType type = getOrientdbType(attribute.getDatatype());
            boolean isCaseInsensitive = (attribute.getCaseInsensitive() == null) ? false : attribute.getCaseInsensitive();
            addAttributeToClass(className, sourceClass, fieldName, type, isCaseInsensitive);
        }
        sourceClass.saveInternal();
        refreshSchema(db);
        for (EntityAttribute attribute : entity.getAttributes()) {
            String fieldName = attribute.getName();
            if (attribute.getIndexed().booleanValue()) {
                List<String> attribs = new ArrayList<String>();
                attribs.add(fieldName);
                createIndex(db, indexNamePrefix, className, attribs);
            }
        }
    }
    
    private void initializeClasses(Entity entity, EntityStore entityStore, OrientBaseGraph db) {
        int[] clusterIds = null;
        String className = entityStore.getEntityName();
        OClass vertexClass = findGraphClass(db, VERTEX_CLASS_NAME);
        final OClassImpl sourceClass = (OClassImpl) ((OSchemaProxy) db.getRawGraph().getMetadata().getSchema())
                .createClass(className, vertexClass, clusterIds);
        log.info("Class " + className + " has been assigned cluster " + sourceClass.getDefaultClusterId());
        sourceClass.saveInternal();
        entityStore.setEntityClass(sourceClass);

        for (EntityAttribute attribute : entity.getAttributes()) {
            String fieldName = attribute.getName();
            OType type = getOrientdbType(attribute.getDatatype());
            boolean isCaseInsensitive = (attribute.getCaseInsensitive() == null) ? false : attribute.getCaseInsensitive();
            addAttributeToClass(className, sourceClass, fieldName, type, isCaseInsensitive);
        }

        addAttributesToClass(className, sourceClass, INTERNAL_ATTRIBUTES);

        // Create schema for storing associated identifiers
        className = IDENTIFIER_TYPE;
        clusterIds = null;
        final OClassImpl idClass = (OClassImpl) ((OSchemaProxy) db.getRawGraph().getMetadata().getSchema())
                .createClass(className, vertexClass, clusterIds);
        log.info("Class " + className + " has been assigned cluster " + idClass.getDefaultClusterId());
        idClass.saveInternal();
        entityStore.setIdentifierClass(idClass);

        addAttributesToClass(className, idClass, IDENTIFIER_ATTRIBUTES);

        OClass edgeClass = findGraphClass(db, EDGE_CLASS_NAME);
        // Create schema for storing links to identifiers
//        className = IDENTIFIER_EDGE_TYPE;
//        clusterIds = null;
//        final OClassImpl identifierEdgeClass = (OClassImpl) ((OSchemaProxy) db.getRawGraph().getMetadata().getSchema())
//                .createClass(className, edgeClass, clusterIds);
//        log.info("Class " + className + " has been assigned cluster " + identifierEdgeClass.getDefaultClusterId());
//        identifierEdgeClass.saveInternal();
        
        // Create schema for storing links
        className = RECORD_LINK_TYPE;
        clusterIds = null;
        final OClassImpl linkClass = (OClassImpl) ((OSchemaProxy) db.getRawGraph().getMetadata().getSchema())
                .createClass(className, edgeClass, clusterIds);
        log.info("Class " + className + " has been assigned cluster " + linkClass.getDefaultClusterId());
        linkClass.saveInternal();

        addAttributesToClass(className, linkClass, LINK_ATTRIBUTES);

        Collection<OClass> classes = db.getRawGraph().getMetadata().getSchema().getClasses();
        StringBuffer sb = new StringBuffer();
        for (OClass clazz : classes) {
            sb.append("Class: " + clazz.getName() + "\n");
        }
        log.debug("Before creating the indexes the list of classes is: " + sb.toString());
        createIndexes(entity, db);

        log.info("Finished initializing graph classes.");
    }

    private void addAttributesToClass(String className, final OClassImpl theClass, InternalAttribute[] attributes) {
        for (InternalAttribute attribute : attributes) {
            String fieldName = attribute.getName();
            addAttributeToClass(className, theClass, fieldName, attribute.getType(), false);
        }
        log.info("Added attributes to graph entity: " + className);
    }

    public void createIndexes(Entity entity, OrientBaseGraph db) {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        if (entityStore == null) {
            log.warn("Unable to create indexes because the entity information is not known.");
            return;
        }
        if (entityStore.getEntityClass() == null) {
            log.warn("Unable to create indexes because the entity class information is not known.");
            return;
        }
        if (entityStore.getIdentifierClass() == null) {
            log.warn("Unable to create indexes because the identifier class information is not known.");
            return;
        }
//        createCompoundIndexes(entity, db);
        createIndexPerAttribute(entity, db);
    }

    /*
    private void listClasses(OrientGraph db) {
        final List<OClass> classes = new ArrayList<OClass>(db.getRawGraph().getMetadata().getSchema().getClasses());
        long count = 0, totalElements = 0;
        Collections.sort(classes, new Comparator<OClass>() {
          public int compare(OClass o1, OClass o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
          }
        });

        for (OClass cls : classes) {
          try {
            final StringBuilder clusters = new StringBuilder();
            if (cls.isAbstract())
              clusters.append("-");
            else
              for (int i = 0; i < cls.getClusterIds().length; ++i) {
                if (i > 0)
                  clusters.append(", ");
                clusters.append(cls.getClusterIds()[i]);
              }

            count = db.getRawGraph().countClass(cls.getName());
            totalElements += count;

            final String superClass = cls.getSuperClass() != null ? cls.getSuperClass().getName() : "";

            System.out.printf(" %-45s| %-35s| %-11s|%15d |\n", format(cls.getName(), 45), format(superClass, 35), clusters.toString(), count);
          } catch (Exception e) {
          }
        }
    }
    */
    
    protected String format(final String iValue, final int iMaxSize) {
      if (iValue == null)
        return null;

      if (iValue.length() > iMaxSize)
        return iValue.substring(0, iMaxSize - 3) + "...";
      return iValue;
    }
    
    private void createIndexPerAttribute(Entity entity, OrientBaseGraph db) {
        refreshSchema(db);
        String indexNamePrefix = INDEX_NAME_PREFIX + entity.getName();
        for (EntityAttribute attribute : entity.getAttributes()) {
            if (attribute.getIndexed()) {
                Collection<String> attribs = new ArrayList<String>();
                attribs.add(attribute.getName());
                createIndex(db, indexNamePrefix, entity.getName(), attribs);
            }
        }
        for (InternalAttribute attribute : INTERNAL_ATTRIBUTES) {
            if (attribute.getIndexed()) {
                Collection<String> attribs = new ArrayList<String>();
                attribs.add(attribute.getName());
                createIndex(db, indexNamePrefix, entity.getName(), attribs);
            }
        }
        indexNamePrefix = INDEX_NAME_PREFIX + IDENTIFIER_TYPE;        
        for (InternalAttribute attribute : IDENTIFIER_ATTRIBUTES) {
            if (attribute.getIndexed()) {
                Collection<String> attribs = new ArrayList<String>();
                attribs.add(attribute.getName());
                createIndex(db, indexNamePrefix, IDENTIFIER_TYPE, attribs);
            }
        }
    }
            
            
    private void createIndex(OrientBaseGraph db, String indexNamePrefix, String entityName, Collection<String> attribs) {
        StringBuilder sql = new StringBuilder("CREATE INDEX ");
        sql.append(indexNamePrefix)
            .append("-")
            .append(nameFromAttributes(attribs))
            .append(" ON ")
            .append(entityName)
            .append(" (");
        
        boolean firstAttribute = true;
        for (String attribute : attribs) {
            if (!firstAttribute) {
                sql.append(", ");
             } else {
                 firstAttribute = false;
             }
            sql.append(attribute);
        }
        sql.append(")  NOTUNIQUE");
        log.warn("Creating index: " + sql);
        db.command(new OCommandSQL(sql.toString())).execute(new Object[] {});
    }

    private String nameFromAttributes(Collection<String> attribs) {
        StringBuffer sb = new StringBuffer();
        int index = 0;
        for (String attrib : attribs) {
            sb.append(attrib);
            if (index < attribs.size()-1) {
                sb.append("-");
            }
        }
        return sb.toString();
    }

    /**
    private void createCompoundIndexes(Entity entity, OrientGraph db) {
        refreshSchema(db);
        String indexNamePrefix = SchemaManager.INDEX_NAME_PREFIX + entity.getName();

        StringBuilder sql = new StringBuilder("CREATE INDEX ");
        sql.append(indexNamePrefix)
            .append(" ON ")
            .append(entity.getName())
            .append(" (");
        
        boolean firstAttribute = true;
        boolean hasIndexedAttribute = false;
        for (EntityAttribute attribute : entity.getAttributes()) {
            if (attribute.getIndexed()) {
                hasIndexedAttribute = true;
                if (!firstAttribute) {
                    sql.append(", ");
                } else {
                    firstAttribute = false;
                }
                sql.append(attribute.getName());
            }
        }
        for (InternalAttribute attribute : INTERNAL_ATTRIBUTES) {
            if (attribute.getIndexed()) {
                hasIndexedAttribute = true;                
                if (!firstAttribute) {
                    sql.append(", ");
                } else {
                    firstAttribute = false;
                }
                sql.append(attribute.getName());
            }
        }
        sql.append(")  NOTUNIQUE");
        log.warn("Creating index " + sql);
        if (hasIndexedAttribute) {
            db.command(new OCommandSQL(sql.toString())).execute(new Object[] {});
        }
        
        hasIndexedAttribute = false;
        indexNamePrefix = SchemaManager.INDEX_NAME_PREFIX + IDENTIFIER_TYPE;        
        sql = new StringBuilder("CREATE INDEX ");
        sql.append(indexNamePrefix)
            .append(" ON ")
            .append(IDENTIFIER_TYPE)
            .append(" (");
        
        firstAttribute = true;
        for (InternalAttribute attribute : IDENTIFIER_ATTRIBUTES) {
            if (attribute.getIndexed()) {
                hasIndexedAttribute = true;                
                if (!firstAttribute) {
                    sql.append(", ");
                } else {
                    firstAttribute = false;
                }
                sql.append(attribute.getName());
            }
        }
        sql.append(")  NOTUNIQUE");
        log.warn("Creating index " + sql);
        if (hasIndexedAttribute) {
            db.command(new OCommandSQL(sql.toString())).execute(new Object[] {});
        }
    }*/

    private void refreshSchema(OrientBaseGraph db) {
        db.getRawGraph().getStorage().reload();
        db.getRawGraph().getMetadata().getSchema().reload();
        db.getRawGraph().getMetadata().getIndexManager().reload();
    }

    public void removeIndexes(Entity entity, OrientBaseGraph db) {
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        if (entityStore == null) {
            log.warn("Unable to remove indexes because the entity information is not known.");
            return;
        }
        if (entityStore.getEntityClass() == null) {
            log.warn("Unable to remove indexes because the entity class information is not known.");
            return;
        }
        if (entityStore.getIdentifierClass() == null) {
            log.warn("Unable to remove indexes because the identifier class information is not known.");
            return;
        }
        for (OIndex<?> index : entityStore.getEntityClass().getClassIndexes()) {
            log.warn("Droping index " + index.getName());
            db.getRawGraph().getMetadata().getIndexManager().dropIndex(index.getName());
        }
        for (OIndex<?> index : entityStore.getIdentifierClass().getClassIndexes()) {
            log.warn("Droping index " + index.getName());
            db.getRawGraph().getMetadata().getIndexManager().dropIndex(index.getName());
        }
        db.getRawGraph().getStorage().reload();
        db.getRawGraph().getMetadata().getSchema().reload();
        db.getRawGraph().getMetadata().getIndexManager().reload();
    }

    private OClass findGraphClass(OrientBaseGraph db, String className) {
        Collection<OClass> classes = db.getRawGraph().getMetadata().getSchema().getClasses();
        log.debug("The repository currently has " + classes.size() + " classes defined:");
        for (OClass oclass : classes) {
            if (oclass.getName().equalsIgnoreCase(className)) {
                return oclass;
            }
        }
        return null;
    }

    private void addAttributeToClass(String className, final OClassImpl sourceClass, String fieldName, OType type, Boolean isCaseSensitive) {
        OPropertyImpl prop = (OPropertyImpl) sourceClass.getProperty(fieldName);
        if (prop != null) {
            log.warn("Property '" + className + "." + fieldName + "' already exists.");
            return;
        }

        prop = (OPropertyImpl) sourceClass.createProperty(fieldName, type);
        if (isCaseSensitive) {
        	prop.setCollate(new OCaseInsensitiveCollate());
        }
        log.debug("Adding field " + fieldName + " to class " + className);
        sourceClass.saveInternal();
    }

    private void storeClusterIds(EntityStore entityStore, OrientBaseGraph db) {
        Collection<OClass> classes = db.getRawGraph().getMetadata().getSchema().getClasses();
        log.debug("The repository currently has " + classes.size() + " classes defined:");
        for (OClass oclass : classes) {
            log.info("Class name: " + oclass.getName() + " in cluster " + oclass.getDefaultClusterId());
            entityStore.put(oclass.getName(), oclass.getDefaultClusterId());
            if (oclass.getName().equalsIgnoreCase(entityStore.getEntityName())) {
                entityStore.setEntityClass((OClassImpl) oclass);
            }
            if (oclass.getName().equalsIgnoreCase(IDENTIFIER_TYPE)) {
                entityStore.setIdentifierClass((OClassImpl) oclass);
            }
        }
    }
    
    private OType getOrientdbType(EntityAttributeDatatype datatype) {
        AttributeDatatype attribDatatype = AttributeDatatype.getById(datatype.getDatatypeCd());
        if (attribDatatype == null) {
            log.warn("The attribute datatype: " + datatype.getName() + " is not supported yet.");
            return null;
        }
        switch (attribDatatype) {
        case INTEGER:
            return OType.INTEGER;
        case SHORT:
            return OType.SHORT;
        case LONG:
            return OType.LONG;
        case DOUBLE:
            return OType.DOUBLE;
        case FLOAT:
            return OType.FLOAT;
        case STRING:
            return OType.STRING;
        case BOOLEAN:
            return OType.BOOLEAN;
        case DATE:
            return OType.DATE;
        case TIMESTAMP:
            return OType.DATETIME;
        case LINKSET:
            return OType.LINKSET;
        case EMBEDDEDSET:
            return OType.EMBEDDEDSET;
        }
        return null;
    }
    
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public EntityStore getEntityStoreByName(String entityName) {
        EntityStore store = storeByName.get(entityName);
        if (store != null) {
            return store;
        }
        store = getStoreByName(entityName);
        storeByName.put(entityName, store);
        return store;
    }
    
    public abstract EntityStore getStoreByName(String entityName);
}
