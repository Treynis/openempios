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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;

public class TestOrientDbConcurrency extends TestCase
{
    private final static String DB_URL = "plocal://mnt/sysnet/person-db";
    private static final int THREAD_COUNT = 1;

    public void testDirtyTxQuery() throws Exception {
        OrientGraph graph = new OrientGraph(DB_URL);

//        OrientVertexType personType = graph.createVertexType("persons");
//        OrientVertexType addressType = graph.createVertexType("addresses");
        

        final CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        final AtomicBoolean shutdownFlag = new AtomicBoolean(false);
        OrientVertex vertex = graph.addVertex("class:person");
        vertex.setProperty("firstName", "John");
        vertex.setProperty("lastName", "Orientdb");
        System.out.println(vertex.getRecord().getIdentity());
        graph.commit();
        System.out.println(vertex.getRecord().getIdentity());

        
        final ExecutorService executorService = Executors.newCachedThreadPool();

        List<Future> futures = new ArrayList<Future>();
        
        for (int j=0; j < THREAD_COUNT; j++) {
            final Future inserter = executorService.submit(new Callable<Void>()
            {
    
                @Override
                public Void call() throws Exception {
                    OrientGraph graph = new OrientGraph(DB_URL);
    
                    int counter = 0;
//                    graph.getRawGraph().begin();
                    while (!shutdownFlag.get()) {
                        OrientVertex vertex = graph.addVertex("class:person");
                        vertex.setProperty("firstName", "John"+counter);
                        vertex.setProperty("lastName", "Orientdb" + counter);
                        Set<OrientVertex> addresses = new HashSet<OrientVertex>();
                        for (int i=0; i < 5; i++) {
                            OrientVertex aVertex = graph.addVertex("class:addresses");
                            aVertex.setProperty("city", "Baltimore");
                            aVertex.getRecord().field("person", vertex, com.orientechnologies.orient.core.metadata.schema.OType.LINK);
                            addresses.add(aVertex);
                        }
                        vertex.getRecord().field("addresses", addresses, com.orientechnologies.orient.core.metadata.schema.OType.LINKSET);
//                      OrientVertex aVertex = graph.addVertex("class:addresses");
//                      aVertex.setProperty("city", "Baltimore");
//                      aVertex.getRecord().field("person", vertex,  com.orientechnologies.orient.core.metadata.schema.OType.LINK);
//                      aVertex.setProperty("person", vertex);
//                      vertex.setProperty("addresses", aVertex);
                        counter++;
                        executorService.submit(new BlockingUpdate("John", vertex.getIdentity().toString()));
                        if (counter % 100 == 0) {
                            System.out.println("Saved 100 records by thread: " + Thread.currentThread().getName());
                            graph.commit();
                        }
                    }
                    graph.commit();
                    return null;
                }
            });
            futures.add(inserter);
        }
        
        final Future fetcher = executorService.submit(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception {
                OrientGraph graph = new OrientGraph(DB_URL);

                while (!shutdownFlag.get())
                    graph.command(new OCommandSQL("select count(*) from persons")).execute();

                return null;
            }
        });

        Thread.sleep(30000);

        shutdownFlag.set(true);

        for (Future future : futures) {
            future.get();
        }
        fetcher.get();
    }
    
    public class BlockingUpdate implements Callable<Void> {
        private String blockingKeyValue;
        private String rid;
        
        public BlockingUpdate(String blockingKeyValue, String rid) {
            super();
            this.blockingKeyValue = blockingKeyValue;
            this.rid = rid;
        }

        @Override
        public Void call() {
            try {
                OrientGraph graph = new OrientGraph(DB_URL);
//                graph.getRawGraph().begin();
                List<ODocument> docs = graph.getRawGraph().command(new OCommandSQL("select rids from Blockinground-0 where blockingKeyValue = " + blockingKeyValue)).execute();
                if (docs.size() == 0) {
                    Set<String> rids = new HashSet<String>();
                    rids.add(rid);
                    OrientVertex vertex = graph.addVertex("class:Blockinground-0");
                    vertex.setProperty("blockingKeyValue", blockingKeyValue);
                    vertex.getRecord().field("rids", rids, OType.EMBEDDEDSET);
                    vertex.getRecord().save();
                    System.out.println("Vertex is now " + vertex);
                } else {
                    ODocument doc = docs.get(0);
                    Set<String> rids = doc.field("rids");
                    rids.add(rid);
                    doc.save();
                    System.out.println("Doc is now " + doc);
                }
                
                graph.commit();
            } catch (Exception e) {
                System.err.println("Got an exception: " + e);
            }
            return null;
        }
    }
}
