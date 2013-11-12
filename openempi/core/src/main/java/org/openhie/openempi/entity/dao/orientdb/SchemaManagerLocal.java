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

import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Entity;

import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.metadata.security.OUser;

public class SchemaManagerLocal extends SchemaManagerAbstract
{
    public SchemaManagerLocal(ConnectionManager connectionManager) {
        super(connectionManager);
    }
    
    public EntityStore getStoreByName(String entityName) {
        String storeName = entityName;
        String storeUrl = PLOCAL_STORAGE_MODE + ":" + buildStoreName(entityName);
        return new EntityStore(entityName, storeName, storeUrl);
    }

    private String buildStoreName(String entityName) {
        String dataDirectory = (String) getParameter(SchemaManager.DATA_DIRECTORY_KEY);
        if (dataDirectory == null) {
            dataDirectory = ".";
        }
        String storeName = dataDirectory + "/" + entityName + "-db";
        log.debug("The store for entity " + entityName + " is at: " + storeName);
        return storeName;
    }

    @Override
    public OGraphDatabase createDatabase(EntityStore store) {
        log.info("Creating a store for entity " + store.getEntityName() + " in location " + store.getStoreName());
        String databaseUrl = store.getStoreUrl();
        OGraphDatabase db = (OGraphDatabase) Orient.instance().getDatabaseFactory()
                .createDatabase(GRAPH_DATABASE_TYPE, databaseUrl);
        db.create();
        db.getMetadata().getSecurity()
                .createUser(connectionManager.getUsername(), connectionManager.getPassword(),
                        new String[] { "admin" });
        log.debug("Created user: " + connectionManager.getUsername());
        db.setInternal(ODatabase.ATTRIBUTES.CUSTOM, "useLightweightEdges=false");
        db.setInternal(ODatabase.ATTRIBUTES.CUSTOM, "useClassForEdgeLabel=false");
        db.setInternal(ODatabase.ATTRIBUTES.CUSTOM, "useClassForVertexLabel=false");
        db.setInternal(ODatabase.ATTRIBUTES.CUSTOM, "useVertexFieldsForEdgeLabels=false");
        Object props = db.get(ODatabase.ATTRIBUTES.CUSTOM);
        log.debug("Database custom attributes " + props + " of type " + props.getClass());
        return db;
    }
}
