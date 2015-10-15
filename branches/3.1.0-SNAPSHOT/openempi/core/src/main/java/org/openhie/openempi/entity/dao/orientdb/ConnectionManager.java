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

import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

public class ConnectionManager
{
    private static Map<String,OrientGraphFactory> connectionPoolByEntity = new HashMap<String,OrientGraphFactory>();
    protected Logger log = Logger.getLogger(getClass());

    private int minPoolConnections;
    private int maxPoolConnections;
    private String username;
    private String password;
    private String serverUsername;
    private String serverPassword;
    private String storageMode;
    private String server;
    private String port;
    
    public ConnectionManager() {
    }
    
    OrientBaseGraph connectInitial(EntityStore entityStore) {
        try {
            long startTime = new Date().getTime();
            OrientBaseGraph db = new OrientGraphNoTx(entityStore.getStoreUrl());
            if (log.isTraceEnabled()) {
                log.trace("Getting connection via connectInitial, users is " + db.getRawGraph().getStorage().getUsers());
            }
            long endTime = new Date().getTime();
            if (log.isTraceEnabled()) {
                log.trace("Obtained a connection from the pool in " + (endTime - startTime) + " msec.");
            }
            if (db.isClosed()) {
                db.getRawGraph().open(getServerUsername(), getServerPassword());
            }
            return db;
        } catch (RuntimeException e) {
            throw e;
        }   
    }
    
    OrientGraph connect(EntityStore entityStore) {
        try {
            long startTime = new Date().getTime();
            OrientGraphFactory pool = getConnectionPool(entityStore, getUsername(), getPassword());
            OrientGraph connection = pool.getTx();
            if (log.isTraceEnabled()) {
                log.trace("Getting connection via connect, users is " + connection.getRawGraph().getStorage().getUsers());
            }
            long endTime = new Date().getTime();
            if (log.isTraceEnabled()) {
                log.trace("Obtained a connection from the pool in " + (endTime - startTime) + " msec.");
            }
            return connection;
        } catch (RuntimeException e) {
            throw e;
        }
    }
    
    void close(EntityStore store, OrientBaseGraph db) {
    	if (log.isDebugEnabled()) {
    		log.debug("Closing pool connection for entity " + store.getEntityName());
    	}
    	db.shutdown();
    }
    
    void closeInternal(EntityStore store, OrientBaseGraph db) {
    	if (log.isDebugEnabled()) {
    		log.debug("Closing internal connection for entity " + store.getEntityName());
    	}
    	db.shutdown();
    }
    
    void shutdown(EntityStore store) {
        OrientGraphFactory pool = connectionPoolByEntity.get(store.getEntityName());
        if (pool != null) {
            log.info("Shutting down the connection pool to OrientDB for entity " + store.getEntityName());
            pool.close();
        }
        try {
            Orient.instance().shutdown();
            log.info("OrientDB instance has been shutdown.");
        } catch (Exception e) {
            log.info("While shutting down the connection to OrientDB, we encountered a problem: " + e, e);
        }
    }
    
    private synchronized OrientGraphFactory getConnectionPool(EntityStore store, String username, String password) {
        OrientGraphFactory connectionPool = connectionPoolByEntity.get(store.getEntityName());
        if (connectionPool == null) {
            OGlobalConfiguration.CLIENT_CHANNEL_MAX_POOL.setValue(120);
            OGlobalConfiguration.STORAGE_RECORD_LOCK_TIMEOUT.setValue(20000);
            connectionPool = new OrientGraphFactory(store.getStoreUrl(), username, password);
            connectionPool.setupPool(minPoolConnections, maxPoolConnections);
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

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
/*    
    public static void main(String[] args) {
        ConnectionManager connMgr = new ConnectionManager();
        connMgr.setServerUsername("admin");
        connMgr.setServerPassword("admin");
        connMgr.setUsername("admin");
        connMgr.setPassword("admin");
        connMgr.setMaxPoolConnections(10);
        connMgr.setMinPoolConnections(1);
        EntityStore store = new EntityStore("person", "person-db", "plocal:/mnt/sysnet/person-db", "person-db");
        OrientGraph graph = connMgr.connect(store);
        graph.getRawGraph().begin();
        Map<String,Object> properties = new HashMap<String,Object>();
        
        Set<Vertex> ids = new HashSet<Vertex>();
        properties.put("identifier", "101");
        properties.put("identifierDomainId", 10);
        ids.add(graph.addVertex("class:identifier", properties));
        properties.put("identifier", "102");
        properties.put("identifierDomainId", 10);
        ids.add(graph.addVertex("class:identifier", properties));
        properties.clear();
        properties.put("identifierSet", ids);
        properties.put("givenName", "Odysseas");
        properties.put("familyName", "Pentakalos");
        Vertex vertex = graph.addVertex("class:person", properties);
        OrientVertex oVertex = (OrientVertex) vertex;
        
        ORID orid = oVertex.getIdentity();
        Object result = graph.getVertex(orid);
        Object id = graph.getRawGraph().load(oVertex.getIdentity());
        
        graph.getRawGraph().commit();
        Map<String,String> params = new HashMap<String,String>();
        params.put("givenName", "Robert");
        Object obj = graph.getRawGraph()
                .command(new OSQLSynchQuery<ODocument>("select count(*) from person where dateVoided is null and givenName = :givenName"))
                .execute(params);
        List<ODocument> odocs = (List<ODocument>) obj;
        for (ODocument odoc : odocs) {
            System.out.println(odoc);
        }
        List<ODocument> results = graph.getRawGraph().query(new OSQLSynchQuery<ODocument>("select from person where dateVoided is null and givenName = 'Robert'"));
        System.out.println("Results are: " + results.size());
        graph.getRawGraph().close();
    }*/
}
