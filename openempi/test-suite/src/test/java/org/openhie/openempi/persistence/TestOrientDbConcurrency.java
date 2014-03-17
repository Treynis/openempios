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

        final ExecutorService executorService = Executors.newCachedThreadPool();

        List<Future> futures = new ArrayList<Future>();
        
        for (int j=0; j < THREAD_COUNT; j++) {
            final Future inserter = executorService.submit(new Callable<Void>()
            {
    
                @Override
                public Void call() throws Exception {
                    OrientGraph graph = new OrientGraph(DB_URL);
    
                    int counter = 0;
                    graph.getRawGraph().begin();
                    while (!shutdownFlag.get()) {
                        OrientVertex vertex = graph.addVertex("class:persons");
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
                graph.getRawGraph().begin();
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
