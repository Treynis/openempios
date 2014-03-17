package org.openhie.openempi.persistence;

import java.util.Date;
import java.util.List;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.service.BaseServiceTestCase;

public class RecordAddLoadTest extends BaseServiceTestCase
{
    public void testAddPerson() {
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
            recordLeft.set("givenName", "John");
            recordLeft.set("familyName", "Smith");
            recordLeft.set("city", "Herndon");
            recordLeft.set("postalCode", "20170");
            recordLeft.set("dateOfBirth", dateOfBirth);
            
            Identifier id = new Identifier();
            id.setIdentifier("12345");
            id.setIdentifierDomain(Context.getIdentifierDomainService().findIdentifierDomainById(9991));
            id.setIdentifierDomainId(9991);
            id.setRecord(recordLeft);
            recordLeft.addIdentifier(id);
            
            id = new Identifier();
            id.setIdentifier("23456");
            id.setIdentifierDomain(Context.getIdentifierDomainService().findIdentifierDomainById(9991));
            id.setIdentifierDomainId(9991);
            id.setRecord(recordLeft);
            recordLeft.addIdentifier(id);
            
            recordLeft = manager.importRecord(entity, recordLeft);
            log.info("Added record with id " + recordLeft.getRecordId());

            Record recordFound = Context.getRecordQueryService().loadRecordById(entity, recordLeft.getRecordId());

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
            
            recordRight = manager.importRecord(entity, recordRight);
            log.info("Added record with id " + recordRight.getRecordId());

            recordFound = Context.getRecordQueryService().loadRecordById(entity, recordRight.getRecordId());

            manager.removeRecord(entity, recordLeft);
            manager.removeRecord(entity, recordRight);
            
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                
            }
/*
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

}
