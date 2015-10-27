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
package org.openhie.openempi.persistence;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class CreateOrientDbSchemaTest extends TestCase
{
    private Logger log = Logger.getLogger(getClass());
 /*   
    public final static String GRAPH_DATABASE_TYPE = "graph";
    public final static String PLOCAL_STORAGE_MODE = "plocal";
    public final static String VERTEX_CLASS_NAME = "V";
    
    private OGraphDatabasePool connectionPool;
    
    public void testCreateDbSchema() {
        String url = "remote:localhost/testdb";
        try {
            OServerAdmin admin = new OServerAdmin("remote:localhost/testdb");
            admin.connect("admin", "admin");
            admin.createDatabase(GRAPH_DATABASE_TYPE, PLOCAL_STORAGE_MODE);
            OGraphDatabase db = getConnectionPool(url).acquire();
            db.getMetadata().getSecurity()
                .createUser("openempi", "openempi", new String[] { "admin" });
            db.setInternal(ODatabase.ATTRIBUTES.CUSTOM, "useLightweightEdges=false");
            db.setInternal(ODatabase.ATTRIBUTES.CUSTOM, "useClassForEdgeLabel=false");
            db.setInternal(ODatabase.ATTRIBUTES.CUSTOM, "useClassForVertexLabel=false");
            db.setInternal(ODatabase.ATTRIBUTES.CUSTOM, "useVertexFieldsForEdgeLabels=false");
            
            OClass vertexClass = findGraphSuperclass(db, VERTEX_CLASS_NAME);
            int[] clusterIds = null;
            String className = "testclass";
            final OClassImpl sourceClass = (OClassImpl) ((OSchemaProxy) db.getMetadata().getSchema())
                    .createClassInternal(className, vertexClass, clusterIds);

            OType type = OType.STRING;
            addAttributeToClass(className, sourceClass, "givenName", type);
            addAttributeToClass(className, sourceClass, "familyName", type);
            addAttributeToClass(className, sourceClass, "zipCode", type);
            addAttributeToClass(className, sourceClass, "city", type);

            String indexNamePrefix = "idx-test";

            StringBuilder sql = new StringBuilder("CREATE INDEX ");
            sql.append(indexNamePrefix)
                .append(" ON ")
                .append(className)
                .append(" (givenName, familyName) NOTUNIQUE");
            log.warn("Creating index " + sql);
            db.command(new OCommandSQL(sql.toString())).execute(new Object[] {});

        } catch (Exception e) {
            System.out.println("Got the error:  " + e);
            e.printStackTrace();
            log.error("Encountered an exception: " + e, e);
        }
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
    
    private synchronized OGraphDatabasePool getConnectionPool(String url) {
        OGraphDatabasePool pool = getConnectionPool();
        if (pool == null) {
            OGlobalConfiguration.CLIENT_CHANNEL_MAX_POOL.setValue(120);
            OGlobalConfiguration.MVRBTREE_TIMEOUT.setValue(20000);
            OGlobalConfiguration.STORAGE_RECORD_LOCK_TIMEOUT.setValue(20000);
            pool = new OGraphDatabasePool(url, "admin", "admin");
        }
        return pool;
    }

    private OGraphDatabasePool getConnectionPool() {
        return connectionPool;
    }
    */
}
