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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.graph.OGraphDatabasePool;

public class ConnectionManager
{
    private static Map<String,OGraphDatabasePool> connectionPoolByEntity = new HashMap<String,OGraphDatabasePool>();
    protected Logger log = Logger.getLogger(getClass());

    private int minPoolConnections;
    private int maxPoolConnections;
    private String username;
    private String password;
    private String serverUsername;
    private String serverPassword;
    private String storageMode;
    
    public ConnectionManager() {
    }
    
    OGraphDatabase connectInitial(EntityStore entityStore) {
        try {
            long startTime = new Date().getTime();
            OGraphDatabase connection = new OGraphDatabase(entityStore.getStoreUrl());
            connection.open(getServerUsername(), getServerPassword());
            long endTime = new Date().getTime();
            if (log.isTraceEnabled()) {
                log.trace("Obtained a connection from the pool in " + (endTime - startTime) + " msec.");
            }
            return connection;
        } catch (RuntimeException e) {
            throw e;
        }
        
    }
    OGraphDatabase connect(EntityStore entityStore) {
        try {
            long startTime = new Date().getTime();
            OGraphDatabasePool pool = getConnectionPool(entityStore, getUsername(), getPassword());
            OGraphDatabase connection = pool.acquire();
            long endTime = new Date().getTime();
            if (log.isTraceEnabled()) {
                log.trace("Obtained a connection from the pool in " + (endTime - startTime) + " msec.");
            }
            return connection;
        } catch (RuntimeException e) {
            throw e;
        }
    }
    
    void shutdown(EntityStore store) {
        OGraphDatabasePool pool = connectionPoolByEntity.get(store.getEntityName());
        if (pool != null) {
            log.info("Shutting down the connection pool to OrientDB for entity " + store.getEntityName());
            pool.close();
        }        
    }
    
    private synchronized OGraphDatabasePool getConnectionPool(EntityStore store, String username, String password) {
        OGraphDatabasePool connectionPool = connectionPoolByEntity.get(store.getEntityName());
        if (connectionPool == null) {
            OGlobalConfiguration.CLIENT_CHANNEL_MAX_POOL.setValue(120);
            OGlobalConfiguration.MVRBTREE_TIMEOUT.setValue(20000);
            OGlobalConfiguration.STORAGE_RECORD_LOCK_TIMEOUT.setValue(20000);
            connectionPool = new OGraphDatabasePool(store.getStoreUrl(), username, password);
            connectionPool.setup(minPoolConnections, maxPoolConnections);
            connectionPoolByEntity.put(store.getEntityName(), connectionPool);
        }
        return connectionPool;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServerUsername() {
        return serverUsername;
    }

    public void setServerUsername(String serverUsername) {
        this.serverUsername = serverUsername;
    }

    public String getServerPassword() {
        return serverPassword;
    }

    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }

    public int getMinPoolConnections() {
        return minPoolConnections;
    }
    public void setMinPoolConnections(int minPoolConnections) {
        this.minPoolConnections = minPoolConnections;
    }
    public int getMaxPoolConnections() {
        return maxPoolConnections;
    }
    public void setMaxPoolConnections(int maxPoolConnections) {
        this.maxPoolConnections = maxPoolConnections;
    }

    public String getStorageMode() {
        return storageMode;
    }

    public void setStorageMode(String storageMode) {
        this.storageMode = storageMode;
    }
}
