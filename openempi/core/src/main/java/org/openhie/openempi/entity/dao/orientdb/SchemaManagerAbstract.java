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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openhie.openempi.model.AttributeDatatype;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;

import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClassImpl;
import com.orientechnologies.orient.core.metadata.schema.OPropertyImpl;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.storage.OStorage;

public abstract class SchemaManagerAbstract implements SchemaManager
{
    protected final static String GRAPH_DATABASE_TYPE = "graph";
    protected final static String RECORD_LINK_TYPE = "recordLink";
    protected final static String IDENTIFIER_TYPE = "identifier";
    protected final static String VERTEX_CLASS_NAME = "V";
    protected final static String EDGE_CLASS_NAME = "E";

    protected Logger log = Logger.getLogger(getClass());
    protected static Map<String,Object> params = new HashMap<String,Object>();    
    protected Map<String, EntityStore> storeByName = new HashMap<String, EntityStore>();
    protected ConnectionManager connectionManager;

    private static Map<String, InternalAttribute> internalAttributeMap = new HashMap<String, InternalAttribute>();

    static {
        for (InternalAttribute attribute : INTERNAL_ATTRIBUTES) {
            internalAttributeMap.put(attribute.getName(), attribute);
        }
    }
   
    SchemaManagerAbstract(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    public void initializeSchema(Entity entity, EntityStore entityStore) {
        OGraphDatabase db = null;
        try {
            db = connectionManager.connect(entityStore);
            if (!isEntityClassDefined(db, entity.getName())) {
                initializeClasses(entity, entityStore, db);
            }
            storeClusterIds(entityStore, db);
        } catch (Exception e) {
            try {
                db = initializeStore(entity, entityStore);
                initializeClasses(entity, entityStore, db);
                storeClusterIds(entityStore, db);
            } catch (Exception ei) {
                log.error("Failed while initializing the store: " + ei, ei);
            }
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    private boolean isEntityClassDefined(OGraphDatabase db, String entityName) {
        Collection<OClass> classes = db.getMetadata().getSchema().getClasses();
        log.debug("The repository currently has " + classes.size() + " classes defined:");
        for (OClass oclass : classes) {
            log.info("Class name: " + oclass.getName() + " in cluster " + oclass.getDefaultClusterId());
            if (oclass.getName().equalsIgnoreCase(entityName)) {
                return true;
            }
        }
        return false;
    }

    private OGraphDatabase initializeStore(Entity entity, EntityStore entityStore) {
        log.info("Creating a store for entity " + entity.getName() + " in location " + entityStore.getStoreName());
        
        OGraphDatabase db = createDatabase(entityStore);
        return db;
    }
    
    public abstract OGraphDatabase createDatabase(EntityStore store);
    
    public void shutdownStore(Entity entity) {
        log.info("Shutting down store for entity: " + entity.getName());
        EntityStore entityStore = getEntityStoreByName(entity.getName());
        if (entityStore == null) {
            log.warn("Received request to shutdown an unknown store.");
            return;
        }

        try {
            connectionManager.shutdown(entityStore);
            final OStorage stg = Orient.instance().getStorage(entityStore.getStoreUrl());
            if (stg != null) {
                stg.close(true);
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
    
    private void initializeClasses(Entity entity, EntityStore entityStore, OGraphDatabase db) {
        int[] clusterIds = null;
        String className = entityStore.getEntityName();
        OClass vertexClass = findGraphSuperclass(db, VERTEX_CLASS_NAME);
        final OClassImpl sourceClass = (OClassImpl) ((OSchemaProxy) db.getMetadata().getSchema()).createClassInternal(
                className, vertexClass, clusterIds);
        log.info("Class " + className + " has been assigned cluster " + sourceClass.getDefaultClusterId());
        sourceClass.saveInternal();
        entityStore.setEntityClass(sourceClass);

        for (EntityAttribute attribute : entity.getAttributes()) {
            String fieldName = attribute.getName();
            OType type = getOrientdbType(attribute.getDatatype());
            addAttributeToClass(className, sourceClass, fieldName, type);
        }

        addAttributesToClass(className, sourceClass, INTERNAL_ATTRIBUTES);

        // Create schema for storing associated identifiers
        className = IDENTIFIER_TYPE;
        final OClassImpl idClass = (OClassImpl) ((OSchemaProxy) db.getMetadata().getSchema()).createClassInternal(
                className, vertexClass, null);
        log.info("Class " + className + " has been assigned cluster " + idClass.getDefaultClusterId());
        idClass.saveInternal();
        entityStore.setIdentifierClass(idClass);

        addAttributesToClass(className, idClass, IDENTIFIER_ATTRIBUTES);

        // Create schema for storing links
        className = RECORD_LINK_TYPE;
        OClass edgeClass = findGraphSuperclass(db, EDGE_CLASS_NAME);
        final OClassImpl linkClass = (OClassImpl) ((OSchemaProxy) db.getMetadata().getSchema()).createClassInternal(
                className, edgeClass, null);
        log.info("Class " + className + " has been assigned cluster " + linkClass.getDefaultClusterId());
        linkClass.saveInternal();

        addAttributesToClass(className, linkClass, LINK_ATTRIBUTES);

        createIndexes(entity, db, sourceClass, idClass);

        log.info("Finished initializing graph classes.");
    }

    private void addAttributesToClass(String className, final OClassImpl theClass, InternalAttribute[] attributes) {
        for (InternalAttribute attribute : attributes) {
            String fieldName = attribute.getName();
            addAttributeToClass(className, theClass, fieldName, attribute.getType());
        }
        log.info("Added attributes to graph entity: " + className);
    }

    public void createIndexes(Entity entity, OGraphDatabase db) {
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
        createIndexes(entity, db, (OClassImpl) findGraphSuperclass(db, entity.getName()),
                (OClassImpl) findGraphSuperclass(db, IDENTIFIER_TYPE));
    }

    private void listClasses(OGraphDatabase db) {
        final List<OClass> classes = new ArrayList<OClass>(db.getMetadata().getSchema().getClasses());
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

            count = db.countClass(cls.getName());
            totalElements += count;

            final String superClass = cls.getSuperClass() != null ? cls.getSuperClass().getName() : "";

            System.out.printf(" %-45s| %-35s| %-11s|%15d |\n", format(cls.getName(), 45), format(superClass, 35), clusters.toString(), count);
          } catch (Exception e) {
          }
        }
    }
    
    protected String format(final String iValue, final int iMaxSize) {
      if (iValue == null)
        return null;

      if (iValue.length() > iMaxSize)
        return iValue.substring(0, iMaxSize - 3) + "...";
      return iValue;
    }
    
    private void createIndexes(Entity entity, OGraphDatabase db, OClassImpl sourceClass, OClassImpl idClass) {
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
    }

    private void refreshSchema(OGraphDatabase db) {
        db.getStorage().reload();
        db.getMetadata().getSchema().reload();
        db.getMetadata().getIndexManager().reload();
    }

    public void removeIndexes(Entity entity, OGraphDatabase db) {
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
            db.getMetadata().getIndexManager().dropIndex(index.getName());
        }
        for (OIndex<?> index : entityStore.getIdentifierClass().getClassIndexes()) {
            log.warn("Droping index " + index.getName());
            db.getMetadata().getIndexManager().dropIndex(index.getName());
        }
        db.getStorage().reload();
        db.getMetadata().getSchema().reload();
        db.getMetadata().getIndexManager().reload();
    }

    private OClass findGraphSuperclass(OGraphDatabase db, String className) {
        Collection<OClass> classes = db.getMetadata().getSchema().getClasses();
        log.debug("The repository currently has " + classes.size() + " classes defined:");
        for (OClass oclass : classes) {
            if (oclass.getName().equalsIgnoreCase(className)) {
                return oclass;
            }
        }
        return null;
    }

    private void addAttributeToClass(String className, final OClassImpl sourceClass, String fieldName, OType type) {
        OPropertyImpl prop = (OPropertyImpl) sourceClass.getProperty(fieldName);
        if (prop != null) {
            log.warn("Property '" + className + "." + fieldName + "' already exists.");
        }

        prop = sourceClass.addPropertyInternal(fieldName, type, null, null);
        log.debug("Adding field " + fieldName + " to class " + className);
        sourceClass.saveInternal();
    }

    private void storeClusterIds(EntityStore entityStore, OGraphDatabase db) {
        Collection<OClass> classes = db.getMetadata().getSchema().getClasses();
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
