package org.openhie.openempi.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.service.BaseServiceTestCase;
import org.openhie.openempi.util.RandomString;

public class RecordAddLoadTest extends BaseServiceTestCase
{
    private final static int THREAD_COUNT = 10;
    private RandomString randomStringGenerator;
    
    public void testAddPerson() {
        
        final ExecutorService executorService = Executors.newCachedThreadPool();

        randomStringGenerator = new RandomString(8);
        List<Future<Void>> futures = new ArrayList<Future<Void>>();
        
        for (int j=0; j < THREAD_COUNT; j++) {
            final Future<Void> inserter = executorService.submit(new LoadRecord());
            futures.add(inserter);            
        }
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public class LoadRecord implements Callable<Void> {
        
        @Override
        public Void call() throws Exception {
            for (int i=0; i < 10; i++) {
                Context.authenticate("admin", "admin");
                RecordManagerService manager = Context.getRecordManagerService();
                EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
                List<Entity> entities = defService.loadEntities();
                if (entities.size() == 0) {
                    assertTrue("You need to define some entities in the database for this to work.", false);
                }
                Entity entity = entities.get(0);
                log.debug("Testing with entity " + entity);
                Date dateOfBirth = new Date();
                Record recordLeft, recordRight, recordFree;
                try {
                    recordLeft = new Record(entity);
                    recordLeft.set("givenName", randomStringGenerator.nextString());
                    recordLeft.set("familyName", randomStringGenerator.nextString());
                    recordLeft.set("city", randomStringGenerator.nextString());
                    recordLeft.set("postalCode", randomStringGenerator.nextString());
                    recordLeft.set("dateOfBirth", dateOfBirth);
                    
                    Long timeId = dateOfBirth.getTime();
                    
                    Identifier id = new Identifier();
                    id.setIdentifier(timeId.toString());
                    id.setIdentifierDomain(Context.getIdentifierDomainService().findIdentifierDomainById(9991));
                    id.setIdentifierDomainId(9991);
                    id.setRecord(recordLeft);
                    recordLeft.addIdentifier(id);
                    
                    id = new Identifier();
                    id.setIdentifier(new Long(timeId.longValue()+1000).toString());
                    id.setIdentifierDomain(Context.getIdentifierDomainService().findIdentifierDomainById(9991));
                    id.setIdentifierDomainId(9991);
                    id.setRecord(recordLeft);
                    recordLeft.addIdentifier(id);
                    
                    recordLeft = manager.importRecord(entity, recordLeft);
                    log.info("Added record with id " + recordLeft.getRecordId());
    
                    Long recordId = recordLeft.getRecordId();
                    Record byId = Context.getRecordQueryService().loadRecordById(entity, recordId);
                    log.info("Found record: " + byId + " using record id " + id);
                    
                    List<Record> found = Context.getRecordQueryService().findRecordsByIdentifier(entity, id);
                    for (Record rec : found) {
                        log.info("Found record: " + rec + " using identifier " + id);
                    }
                    
                    Record recordFound = Context.getRecordQueryService().loadRecordById(entity, recordLeft.getRecordId());
                    /*
    
                    recordRight = new Record(entity);
                    recordRight.set("givenName", "John");
                    recordRight.set("familyName", "Smith");
                    recordRight.set("city", "Herndon");
                    recordRight.set("postalCode", "20170");
                    recordRight.set("dateOfBirth", dateOfBirth);
    
                    id = new Identifier();
                    id.setIdentifier("34567");
                    id.setIdentifierDomain(Context.getIdentifierDomainService().findIdentifierDomainById(9991));
                    id.setIdentifierDomainId(9991);
                    id.setRecord(recordLeft);
                    recordRight.addIdentifier(id);
                    
                    id = new Identifier();
                    id.setIdentifier("45678");
                    id.setIdentifierDomain(Context.getIdentifierDomainService().findIdentifierDomainById(9991));
                    id.setIdentifierDomainId(9991);
                    id.setRecord(recordLeft);
                    recordRight.addIdentifier(id);
                    
                    recordRight = manager.importRecord(entity, recordRight);
                    log.info("Added record with id " + recordRight.getRecordId());
    
                    List<Long> ids = new ArrayList<Long>();
                    ids.add(recordLeft.getRecordId());
                    ids.add(recordRight.getRecordId());
                    found = Context.getRecordQueryService().loadRecordsById(entity, ids);
                    recordFound = Context.getRecordQueryService().loadRecordById(entity, recordRight.getRecordId());
    
    //                manager.removeRecord(entity, recordLeft);
    //                manager.removeRecord(entity, recordRight);
    //                
    //                try {
    //                    Thread.sleep(5000);
    //                } catch (InterruptedException e) {
    //                    
    //                }
                    recordRight = new Record(entity);
                    recordRight.set("givenName", "John");
                    recordRight.set("familyName", "Smith");
                    recordRight.set("city", "Herndon");
                    recordRight.set("postalCode", "20170");
                    recordRight.set("dateOfBirth", dateOfBirth);
                    recordRight = manager.addRecord(entity, recordRight);
                    log.info("Added record with id " + recordRight.getRecordId());
                    
                    recordFree = new Record(entity);
                    recordFree.set("givenName", "Josh");
                    recordFree.set("familyName", "Franklin");
                    recordFree.set("city", "Jerusalem");
                    recordFree.set("postalCode", "20150");
                    recordFree.set("dateOfBirth", dateOfBirth);
                    recordFree = manager.addRecord(entity, recordFree);
                    log.info("Added record with id " + recordFree.getRecordId());
    
                    List<RecordLink> links = Context.getRecordQueryService().loadRecordLinks(entity, recordRight.getRecordId());
                    log.debug("Found: " + links.size() + " links.");
                    
                    manager.removeRecord(entity, recordFree);
                    manager.removeRecord(entity, recordLeft);
                    manager.removeRecord(entity, recordRight);
          */
                } catch (ApplicationException e) {
                    log.error("Failed due to: " + e, e);
                    assertTrue("Failed due to " + e.getMessage(), false);
                }
            }
            return null;
        }
    }

}
