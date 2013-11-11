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
package org.openempi.webapp.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.IdentifierDomainWeb;
import org.openempi.webapp.client.model.IdentifierWeb;
import org.openempi.webapp.client.model.LinkedPersonWeb;
import org.openempi.webapp.client.model.PersonWeb;
import org.openempi.webapp.client.model.PersonIdentifierWeb;
import org.openempi.webapp.client.model.RecordListWeb;
import org.openempi.webapp.client.model.RecordSearchCriteriaWeb;
import org.openempi.webapp.client.model.RecordWeb;
import org.openempi.webapp.client.model.UserFileWeb;
import org.openempi.webapp.server.util.ModelTransformer;
import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.model.ReviewRecordPair;
import org.openhie.openempi.model.User;
import org.openhie.openempi.service.IdentifierDomainService;

public class EntityInstanceDataServiceTest extends BaseServiceTestCase
{
    private static List<EntityAttributeDatatype> datatypes;
    private static Map<String,EntityAttributeDatatype> datatypeByName = new HashMap<String,EntityAttributeDatatype>();

    private boolean newDatabase;
    
    @Override
    protected void onSetUp() throws Exception {
    }

    @Override
    protected void onTearDown() throws Exception {
    }
    
    public void testAddEntityDefinition() {
        Context.startup();
        Context.authenticate("admin", "admin");
        
        EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
        RecordManagerService manager = Context.getRecordManagerService();

        datatypes = entityManager.getEntityAttributeDatatypes();
        for (EntityAttributeDatatype datatype : datatypes) {
            datatypeByName.put(datatype.getName(), datatype);
        }        
  
        try {
            List<Entity> entities = entityManager.findEntitiesByName("person");
            // assertTrue("Not found entities.", entities.size() > 0);
            
            newDatabase = false;
            Entity entity = buildTestEntity("person");    
            if (entities == null || entities.size() == 0) {
               
                // Add entity model
                entity = entityManager.addEntity(entity);
     
                // Create Test Database
                manager.initializeStore(entity);
                newDatabase = true;
            }
            
            log.debug("Added entity model and added a record instance to the entity model" + entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testAddEntityInstances() {
        EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
        IdentifierDomainService identifierDomainService = Context.getIdentifierDomainService();
        
        // EntityInstanceDataServiceImpl entityInstanceService = new EntityInstanceDataServiceImpl();
        RecordManagerService manager = Context.getRecordManagerService();
        
        Entity entity = null;
        EntityWeb entityModel = null;
        try {
            List<Entity> entities = entityManager.findEntitiesByName("person");
            assertTrue("Not found entities.", entities.size() > 0);
            
            entity = entities.get(0);
            entityModel = ModelTransformer.mapToEntity(entity, EntityWeb.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
                    
        try {
            // Add record instance           
            List<IdentifierDomain> domains = identifierDomainService.getIdentifierDomains();
            if (domains == null || domains.size() == 0) {
                assertTrue("There are no identifier domains in the system.", true);
                return;
            }
            IdentifierDomain domain = domains.get(0);
            
            Record record = new Record(entity);
            record.set("givenName", "John");       
            record.set("familyName", "Smith");       
            record.set("dateOfBirth", new java.util.Date());
            addRecordIdentifiersAttribute(record, domain, "identifier-john");
            
            // RecordWeb newRecord = ModelTransformer.mapToRecord(record, RecordWeb.class);
            // newRecord = entityInstanceService.addEntity(entityModel, newRecord);
            // assertNull(newRecord);
           record = manager.addRecord(entity, record);            
           assertNotNull(record);
           
           
           record = new Record(entity);
           record.set("givenName", "Albert");       
           record.set("familyName", "Smith");       
           record.set("dateOfBirth", new java.util.Date());
           addRecordIdentifiersAttribute(record, domain, "identifier-albert");
           
           record = manager.addRecord(entity, record);            
           assertNotNull(record);
            
        } catch (Exception e) {
            log.error("Test failed: " + e);
        }

    }
    
	public void testGetEntityInstancesByIdentifier() {
        EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
        IdentifierDomainService identifierDomainService = Context.getIdentifierDomainService();
        //EntityInstanceDataServiceImpl entityInstanceService = new EntityInstanceDataServiceImpl();
        RecordQueryService queryService = Context.getRecordQueryService();
        
        Entity entity = null;
        // EntityWeb entityModel = null;
        try {
            List<Entity> entities = entityManager.findEntitiesByName("person");
            assertTrue("Not found entities.", entities.size() > 0);
            
            entity = entities.get(0);
            // entityModel = ModelTransformer.mapToEntity( entity, EntityWeb.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
            	    
        List<IdentifierDomain> domains = identifierDomainService.getIdentifierDomains();
        if (domains == null || domains.size() == 0) {
            assertTrue("There are no identifier domains in the system.", true);
            return;
        }
        IdentifierDomain domain = domains.get(0);
		Identifier identifier = new Identifier();
		identifier.setIdentifier("identifier-john");
		
		try {
			// List<RecordWeb> records = entityInstanceService.findEntitiesByIdentifier(entityModel, identifier);
			List<Record> records = queryService.findRecordsByIdentifier(entity, identifier);
            assertTrue("Not found records.", records.size() > 0);
            
			System.out.println("List of record has " + records.size() + " entries");
			for (Record record : records) {
				for (Identifier id : record.getIdentifiers()) {
					System.out.println("Found identifier: " + id.getIdentifier() + " " + id.getIdentifierDomain().getNamespaceIdentifier() + " " + id.getIdentifierDomain().getUniversalIdentifier() + " " + id.getIdentifierDomain().getUniversalIdentifierTypeCode());
				}
			}
		} catch (Exception e) {
			log.error("Test failed: " + e);
		}

	}

	public void testGetPersonsByAttributes() {
        EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
        RecordQueryService queryService = Context.getRecordQueryService();
        
        Entity entity = null;
        // EntityWeb entityModel = null;
        try {
            List<Entity> entities = entityManager.findEntitiesByName("person");
            assertTrue("Not found entities.", entities.size() > 0);
            
            entity = entities.get(0);
            // entityModel = ModelTransformer.mapToEntity( entity, EntityWeb.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
		Record searchRecord = new Record(entity);
		searchRecord.set("givenName", "John%");		
		searchRecord.set("familyName", "Smith");       
        
		try {
            List<Record> records = queryService.findRecordsByAttributes(entity, searchRecord, 0, 1);
            assertTrue("Not found records.", records.size() == 1);
            
			for (Record record : records) {
                for (Identifier id : record.getIdentifiers()) {
                    System.out.println("Found identifier: " + id.getIdentifier() + " " + id.getIdentifierDomain().getNamespaceIdentifier() + " " + id.getIdentifierDomain().getUniversalIdentifier() + " " + id.getIdentifierDomain().getUniversalIdentifierTypeCode());
                }
			}		
            assertTrue("Not found entities.", records.size() == 1);
            
		} catch (Exception e) {
			log.error("Test failed: " + e);
		}

	}

    public void testAddRecordLink() {
        EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
        RecordQueryService queryService = Context.getRecordQueryService();
        RecordManagerService managerService = Context.getRecordManagerService();
        
        Entity entity = null;        
        try {
            List<Entity> entities = entityManager.findEntitiesByName("person");
            assertTrue("Not found entities.", entities.size() > 0);
            entity = entities.get(0);
            
            Record searchRecord = new Record(entity);
            searchRecord.set("givenName", "John");     
            searchRecord.set("familyName", "Smith");      
            List<Record> records = queryService.findRecordsByAttributes(entity, searchRecord);
            Record recordLeft = records.get(0);
            
            searchRecord.set("givenName", "Albert");     
            searchRecord.set("familyName", "Smith");      
            records = queryService.findRecordsByAttributes(entity, searchRecord);
            Record recordRight = records.get(0);            
            
            RecordLink recordLink = new RecordLink();
            recordLink.setLeftRecord(recordLeft);
            recordLink.setRightRecord(recordRight);
            
            recordLink.setDateCreated(new java.util.Date());
      
            User user = new User();
            user.setId(new Long(-1));
            recordLink.setUserCreatedBy(user);
            recordLink.setUserReviewedBy(user);     
            recordLink.setLinkSource(new LinkSource(1));
            recordLink.setWeight(1.0);
            recordLink.setState(RecordLinkState.POSSIBLE_MATCH);
            
            recordLink = managerService.addRecordLink(recordLink);            
            assertNotNull(recordLink);
            
        } catch (ApplicationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }        
    }

    public void testUpdateRecordLink() {
        EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
        RecordQueryService queryService = Context.getRecordQueryService();
        RecordManagerService managerService = Context.getRecordManagerService();
        
        Entity entity = null;        
        try {
            List<Entity> entities = entityManager.findEntitiesByName("person");
            assertTrue("Not found entities.", entities.size() > 0);
            entity = entities.get(0);
           
            List<RecordLink> recordLinks = queryService.loadRecordLinks(entity, RecordLinkState.POSSIBLE_MATCH, 0, 10);
            assertTrue("Not found record links.", recordLinks.size() > 0);      

            for (RecordLink link : recordLinks) {
                
                RecordLink recordLink = queryService.loadRecordLink(entity, link.getRecordLinkId());   
                if ((recordLink.getLeftRecord().get("familyName").equals("Smith") || recordLink.getRightRecord().get("familyName").equals("Smith")) &&
                     recordLink.getLeftRecord().get("givenName").equals("John") || recordLink.getRightRecord().get("givenName").equals("John")   ) {
                    
                    recordLink.setState(RecordLinkState.NON_MATCH);
                    recordLink = managerService.updateRecordLink(recordLink);   
                    
                    assertTrue("Not match.", recordLink.getState().equals(RecordLinkState.NON_MATCH));
                }
            }  
            
        } catch (ApplicationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }        
    }
 
    public void testDeleteRecordLink() {
        EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
        RecordQueryService queryService = Context.getRecordQueryService();
        RecordManagerService managerService = Context.getRecordManagerService();
        
        Entity entity = null;        
        try {
            List<Entity> entities = entityManager.findEntitiesByName("person");
            assertTrue("Not found entities.", entities.size() > 0);
            entity = entities.get(0);
           
            List<RecordLink> recordLinks = queryService.loadRecordLinks(entity, RecordLinkState.NON_MATCH, 0, 10);
            assertTrue("Not found record links.", recordLinks.size() > 0);      

            for (RecordLink link : recordLinks) {
                
                RecordLink recordLink = queryService.loadRecordLink(entity, link.getRecordLinkId());  
                if ((recordLink.getLeftRecord().get("familyName").equals("Smith") || recordLink.getRightRecord().get("familyName").equals("Smith")) &&
                     recordLink.getLeftRecord().get("givenName").equals("John") || recordLink.getRightRecord().get("givenName").equals("John")   ) {
                    managerService.removeRecordLink(recordLink); 
                }
            }  
            
        } catch (ApplicationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }        
    }
    
    public void testDeleteAllEntityInstances() {
        RecordManagerService manager = Context.getRecordManagerService();
        RecordQueryService queryService = Context.getRecordQueryService();
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();

        try {
                List<Entity> entities = defService.findEntitiesByName("person");        
                assertTrue("Not found entities.", entities.size() > 0);
                Entity entity = entities.get(0);

                Record searchRecord = new Record(entity);
                searchRecord.set("givenName", "John");     
                searchRecord.set("familyName", "Smith");     
                    
                List<Record> list = queryService.findRecordsByAttributes(entity, searchRecord); 
                assertTrue("Not found record.", list.size() > 0);
                    
                for (Record record : list) {                    
                     manager.deleteRecord(entity, record);
                }  
                
                searchRecord = new Record(entity);
                searchRecord.set("givenName", "Albert");     
                searchRecord.set("familyName", "Smith");     
                    
                list = queryService.findRecordsByAttributes(entity, searchRecord); 
                assertTrue("Not found record.", list.size() > 0);
                    
                for (Record record : list) {                    
                     manager.deleteRecord(entity, record);
                }                   
        } catch (ApplicationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
    }
	   
    public void testDeleteEntityDefinition() {
        EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
        try {
            if (newDatabase) {
                List<Entity> entities = entityManager.findEntitiesByName("person");
                assertTrue("Not found entities.", entities.size() > 0);
                
                for (Entity entity : entities) {
                    log.debug("Deleting entity " + entity);
                    entityManager.deleteEntity(entity);
                }
                entities = entityManager.loadEntities();
                log.debug("After delete found " + entities.size() + " entities.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
    public Entity buildTestEntity(String entityName) {
        Entity entity = new Entity();
        entity.setName(entityName);
        entity.setDescription("Entity that represents a person and his or her demographic attributes");
        entity.setDisplayName("Person Test");
        entity.setVersionId(1);
        
        Set<EntityAttribute> attributes = new HashSet<EntityAttribute>();
        entity.setAttributes(attributes);
        
        EntityAttribute attrib = new EntityAttribute();
        attrib.setName("givenName");
        attrib.setDisplayName("Given Name");
        attrib.setDescription("Given name");
        attrib.setIndexed(true);
        attrib.setDisplayOrder(1);
        attrib.setDatatype(datatypeByName.get("string"));
        attrib.setIsCustom(false);
        attributes.add(attrib);
        
        attrib = new EntityAttribute();
        attrib.setName("familyName");
        attrib.setDisplayName("Family Name");
        attrib.setDescription("Family name");
        attrib.setIndexed(true);
        attrib.setDisplayOrder(2);
        attrib.setDatatype(datatypeByName.get("string"));
        attrib.setIsCustom(false);
        attributes.add(attrib);
        
        attrib = new EntityAttribute();
        attrib.setName("age");
        attrib.setDisplayName("Age");
        attrib.setDescription("Age");
        attrib.setIndexed(false);
        attrib.setDisplayOrder(3);
        attrib.setDatatype(datatypeByName.get("short"));
        attrib.setIsCustom(false);
        attributes.add(attrib);     
        
        attrib = new EntityAttribute();
        attrib.setName("dateOfBirth");
        attrib.setDisplayName("Date of Birth");
        attrib.setDescription("Date of Birth");
        attrib.setIndexed(false);
        attrib.setDisplayOrder(4);
        attrib.setDatatype(datatypeByName.get("date"));
        attrib.setIsCustom(false);
        attributes.add(attrib);     
        return entity;
    }
    
    private void addRecordIdentifiersAttribute(Record record, IdentifierDomain testDomain, String identifier) {
        Identifier id = new Identifier();
        id.setIdentifier(identifier);
        id.setRecord(record);
        id.setIdentifierDomain(testDomain);
        record.addIdentifier(id);
    }
}
