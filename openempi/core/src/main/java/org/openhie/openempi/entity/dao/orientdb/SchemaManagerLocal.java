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

import org.openhie.openempi.entity.Constants;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;

public class SchemaManagerLocal extends SchemaManagerAbstract
{
    public SchemaManagerLocal(ConnectionManager connectionManager) {
        super(connectionManager);
    }
    
    public EntityStore getStoreByName(String entityName) {
        String storeName = entityName;
        String storageName = buildStoreName(entityName);
        String dataDirectory = (String) getParameter(Constants.DATA_DIRECTORY_KEY);
        if (dataDirectory == null) {
            dataDirectory = ".";
        }
        String storeUrl = PLOCAL_STORAGE_MODE + ":" + dataDirectory + "/" + storageName;
        if (log.isInfoEnabled()) {
            log.info("Connecting to database using URL: " + storeUrl);
        }
        return new EntityStore(entityName, storeName, storeUrl, storageName);
    }

    private String buildStoreName(String entityName) {
        String storeName = entityName + "-db";
        log.debug("The store for entity " + entityName + " is at: " + storeName);
        return storeName;
    }

    public void createDatabase(EntityStore store, OrientBaseGraph db) {
        log.info("Creating a store for entity " + store.getEntityName() + " in location " + store.getStoreName());
        OUser user = db.getRawGraph().getMetadata().getSecurity().getUser(connectionManager.getUsername());
        if (user == null) {
	        db.getRawGraph().getMetadata().getSecurity()
	                .createUser(connectionManager.getUsername(), connectionManager.getPassword(),
	                        new String[] { "admin" });
	        log.debug("Created user: " + connectionManager.getUsername());
        }
        Object props = db.getRawGraph().get(ODatabase.ATTRIBUTES.CUSTOM);
        log.debug("Database custom attributes " + props + " of type " + props.getClass());
    }
    
    public void dropDatabase(EntityStore store, OrientBaseGraph db) {
        log.info("Dropping a store for entity " + store.getEntityName() + " in location " + store.getStoreName());
        db.getRawGraph().getMetadata().getSecurity()
                .dropUser(connectionManager.getUsername());
        log.debug("Dropped user: " + connectionManager.getUsername());
        db.drop();
    }
}
