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

import java.io.IOException;

import org.openhie.openempi.context.Context;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.db.ODatabase;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;

public class SchemaManagerRemote extends SchemaManagerAbstract
{
    public SchemaManagerRemote(ConnectionManager connectionManager) {
        super(connectionManager);
    }
    
    public EntityStore getStoreByName(String entityName) {
        String storeName = entityName;
        String storageName = buildStoreName(entityName);
        String dataDirectory = Context.getConfiguration().getAdminConfiguration().getDataDirectory();
        String storeUrl = REMOTE_STORAGE_MODE + ":localhost/" + dataDirectory + "/" + storageName;
        return new EntityStore(entityName, storeName, storeUrl, storageName);
    }

    private String buildStoreName(String entityName) {
        if (getConnectionManager().getStorageMode().equalsIgnoreCase(REMOTE_STORAGE_MODE)) {
            return entityName + "-db";
        }
        String storeName =  entityName + "-db";
        log.debug("The store for entity " + entityName + " is at: " + storeName);
        return storeName;
    }

    public void createDatabase(EntityStore store, OrientBaseGraph db) {
        log.info("Creating a store for entity " + store.getEntityName() + " in location " + store.getStoreName());
        String databaseUrl = store.getStoreUrl();
        try {
            OServerAdmin admin = new OServerAdmin(databaseUrl);
            admin.connect(connectionManager.getServerUsername(), connectionManager.getServerPassword());
            if (!admin.existsDatabase(PLOCAL_STORAGE_MODE)) {
                admin.createDatabase(GRAPH_DATABASE_TYPE, PLOCAL_STORAGE_MODE);
            }            
            db.getRawGraph().getMetadata().getSecurity()
                .createUser(connectionManager.getUsername(), connectionManager.getPassword(),
                    new String[] { "admin" });
            log.debug("Created user: " + connectionManager.getUsername());
//            db.getRawGraph().setInternal(ODatabase.ATTRIBUTES.CUSTOM, "useLightweightEdges=false");
//            db.getRawGraph().setInternal(ODatabase.ATTRIBUTES.CUSTOM, "useClassForEdgeLabel=false");
//            db.getRawGraph().setInternal(ODatabase.ATTRIBUTES.CUSTOM, "useClassForVertexLabel=false");
//            db.getRawGraph().setInternal(ODatabase.ATTRIBUTES.CUSTOM, "useVertexFieldsForEdgeLabels=false");
            Object props = db.getRawGraph().get(ODatabase.ATTRIBUTES.CUSTOM);
            log.debug("Database custom attributes " + props + " of type " + props.getClass());
        } catch (IOException e) {
            log.error("Unable to connect to the remote server: " + e, e);
            throw new RuntimeException("Unable to connect to the remote server; ensure that the server is up and running.");
        }
    }
}
