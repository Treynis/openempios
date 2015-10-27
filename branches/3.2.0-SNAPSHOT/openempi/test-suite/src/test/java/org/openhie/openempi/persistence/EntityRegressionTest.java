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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.EntityAttributeGroup;
import org.openhie.openempi.model.EntityAttributeGroupAttribute;
import org.openhie.openempi.model.EntityAttributeValidation;
import org.openhie.openempi.model.EntityAttributeValidationParameter;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.service.BaseServiceTestCase;
import org.openhie.openempi.service.PersonQueryService;

public class EntityRegressionTest extends BaseServiceTestCase
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
	    
	public Entity buildTestEntity(String entityName) {
		Entity entity = new Entity();
		entity.setName(entityName);
		entity.setDescription("Entity that represents a person and his or her demographic attributes");
		entity.setDisplayName("Person Test");
		entity.setVersionId(1);
		
		// Attributes
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
		
		attrib = new EntityAttribute();
		attrib.setName("address");
		attrib.setDisplayName("Address");
		attrib.setDescription("Address");
		attrib.setIndexed(true);
		attrib.setDisplayOrder(5);
		attrib.setDatatype(datatypeByName.get("string"));
        attrib.setIsCustom(false);
		attributes.add(attrib);
		
		attrib = new EntityAttribute();
		attrib.setName("state");
		attrib.setDisplayName("State");
		attrib.setDescription("State");
		attrib.setIndexed(true);
		attrib.setDisplayOrder(6);
		attrib.setDatatype(datatypeByName.get("string"));
        attrib.setIsCustom(false);
		attributes.add(attrib);		
		
		attrib = new EntityAttribute();
		attrib.setName("email");
		attrib.setDisplayName("Email");
		attrib.setDescription("Email");
		attrib.setIndexed(true);
		attrib.setDisplayOrder(7);
		attrib.setDatatype(datatypeByName.get("string"));
        attrib.setIsCustom(false);
		attributes.add(attrib);		
		return entity;
	}

	public List<EntityAttributeGroup> getTestEntityGroups(Entity entity) {
		
		List<EntityAttributeGroup> attributeGroups = new ArrayList<EntityAttributeGroup>();
		
		 EntityAttributeGroup group = new EntityAttributeGroup();
		 group.setName("name");
		 group.setDisplayName("Name");
		 group.setDisplayOrder(1);	
		 group.setEntity(entity);	
		 
		  EntityAttributeGroupAttribute groupAttribute = new EntityAttributeGroupAttribute();	
		  groupAttribute.setEntityAttributeGroup(group);
		  groupAttribute.setEntityAttribute( entity.findAttributeByName("givenName") );			  
		  group.addEntityAttributeGroupAttribute(groupAttribute);

		  groupAttribute = new EntityAttributeGroupAttribute();					  
		  groupAttribute.setEntityAttributeGroup(group);
		  groupAttribute.setEntityAttribute( entity.findAttributeByName("familyName") );			  
		  group.addEntityAttributeGroupAttribute(groupAttribute);
		  
		 attributeGroups.add(group);
	
		 group = new EntityAttributeGroup();
		 group.setName("birth");
		 group.setDisplayName("Birth");
		 group.setDisplayOrder(2);	
		 group.setEntity(entity);	
		 
		  groupAttribute = new EntityAttributeGroupAttribute();					  
		  groupAttribute.setEntityAttributeGroup(group);
		  groupAttribute.setEntityAttribute( entity.findAttributeByName("age") );			  
		  group.addEntityAttributeGroupAttribute(groupAttribute);

		  groupAttribute = new EntityAttributeGroupAttribute();					  
		  groupAttribute.setEntityAttributeGroup(group);
		  groupAttribute.setEntityAttribute( entity.findAttributeByName("dateOfBirth") );			  
		  group.addEntityAttributeGroupAttribute(groupAttribute);
		  
		 attributeGroups.add(group);
		 
		 group = new EntityAttributeGroup();
		 group.setName("address");
		 group.setDisplayName("Address");
		 group.setDisplayOrder(3);	
		 group.setEntity(entity);	
		 
		  groupAttribute = new EntityAttributeGroupAttribute();					  
		  groupAttribute.setEntityAttributeGroup(group);
		  groupAttribute.setEntityAttribute( entity.findAttributeByName("address") );			  
		  group.addEntityAttributeGroupAttribute(groupAttribute);

		  groupAttribute = new EntityAttributeGroupAttribute();					  
		  groupAttribute.setEntityAttributeGroup(group);
		  groupAttribute.setEntityAttribute( entity.findAttributeByName("state") );			  
		  group.addEntityAttributeGroupAttribute(groupAttribute);
		  
		 attributeGroups.add(group);
	
		 group = new EntityAttributeGroup();
		 group.setName("other");
		 group.setDisplayName("Other");
		 group.setDisplayOrder(3);	
		 group.setEntity(entity);	
		 
		  groupAttribute = new EntityAttributeGroupAttribute();					  
		  groupAttribute.setEntityAttributeGroup(group);
		  groupAttribute.setEntityAttribute( entity.findAttributeByName("email") );			  
		  group.addEntityAttributeGroupAttribute(groupAttribute);
		  
		 attributeGroups.add(group);
		 
		 return attributeGroups;
	}
	
	public List<EntityAttributeValidation> getTestAttributeValidations(EntityAttribute attribute, Entity entity) {
		List<EntityAttributeValidation> attributeValidations = new ArrayList<EntityAttributeValidation>();	

		EntityAttributeValidation validation = new EntityAttributeValidation();
		EntityAttributeValidationParameter param = new EntityAttributeValidationParameter();
		
		if( attribute.getName().equals("givenName") || attribute.getName().equals("familyName")) {
			validation.setName("nullityValidationRule");
			validation.setDisplayName("Nullity Validation Rule");
			validation.setEntityAttribute(attribute);
			
			attributeValidations.add(validation);
		}
		
		if( attribute.getName().equals("state") ) {
			validation.setName("valueSetValidationRule");
			validation.setDisplayName("Value Set Validation Rule");
			validation.setEntityAttribute(attribute);

			param.setEntityAttributeValidation(validation);
			param.setName("valueSet");
			param.setValue("Virginia,Washington,West Virginia,Wisconsin,Wyoming");
			validation.addParameter(param);	
			
			attributeValidations.add(validation);
		}

		if( attribute.getName().equals("email") ) {
			validation.setName("regexpValidationRule");
			validation.setDisplayName("Regular Expression Validation Rule");
			validation.setEntityAttribute(attribute);
			param = new EntityAttributeValidationParameter();
			param.setEntityAttributeValidation(validation);
			param.setName("regularExpression");
			param.setValue("^([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4}$");
			validation.addParameter(param);		
			
			attributeValidations.add(validation);
		}			
		
		return attributeValidations;
	}

	private void addRecordIdentifiersAttribute(Record record, IdentifierDomain testDomain, int i) {
		Identifier id = new Identifier();
		id.setIdentifier("identifier-" + i);
		id.setRecord(record);
		id.setIdentifierDomain(testDomain);
		record.addIdentifier(id);
	}	
	
	
	//******************** Tests **********************
    public void testGetEntityAttributeDatatypes() {
        Context.startup();
        Context.authenticate("admin", "admin");
        
        EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
        try {
            long startTime = new java.util.Date().getTime();
            datatypes = entityManager.getEntityAttributeDatatypes();
            assertTrue("Not found datatypes.", datatypes.size() > 0);
            long endTime = new java.util.Date().getTime();
            log.debug("Obtained a list of " + datatypes.size() + " entries in " + (endTime-startTime) + " msec.");
    
            startTime = new java.util.Date().getTime();
            datatypes = entityManager.getEntityAttributeDatatypes();
            assertTrue("Not found datatypes in cache.", datatypes.size() > 0);
            endTime = new java.util.Date().getTime();
            log.debug("Obtained a list of " + datatypes.size() + " entries from the cache in " + (endTime-startTime) + " msec.");
            
            for (EntityAttributeDatatype datatype : datatypes) {
                datatypeByName.put(datatype.getName(), datatype);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public void testAddEntityDefinition() {
		EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
		
        List<Entity> entities = entityManager.findEntitiesByName("person");
        // assertTrue("Not found entities.", entities.size() > 0);
        
        newDatabase = false;
        Entity entity = buildTestEntity("person");    
        if (entities == null || entities.size() == 0) {
    		try {
    			// 1. add entity and attributes
    			entity = entityManager.addEntity(entity);
    			
    			// 2. add entity groups with attribute
    			List<EntityAttributeGroup> groups = getTestEntityGroups(entity);
    			for (EntityAttributeGroup group : groups) {				
    				entityManager.addEntityAttributeGroup(group);
    			}
    			
    			// 3. add entity attribute validations
    			for (EntityAttribute attribute : entity.getAttributes()) {
    				 List<EntityAttributeValidation> validations = getTestAttributeValidations(attribute, entity);						
    				 for (EntityAttributeValidation validation : validations) {							 
    					  entityManager.addEntityAttributeValidation(validation);
    				 }							
    			}	
    			
                newDatabase = true;
    			
    		} catch (ApplicationException e) {
    			e.printStackTrace();
    		}
    		log.debug("Added entity " + entity);
	    }
	}
	
	public void testUpdateEntityDefinition() {
		EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
		if (!newDatabase) {
            log.debug("Do not update entity model if already exist");
            return;
		}
		
		try {
//			List<Entity> entities = entityManager.loadEntities();
			List<Entity> entities = entityManager.findEntitiesByName("person");
		    assertTrue("Not found entities.", entities.size() > 0);
				
	
					Entity entity = entities.get(0);
				
					entity.setDescription("Updated the description of the entity");
					
					// First we remove one of the existing attributes
					EntityAttribute attributeToDelete=null;
					for (EntityAttribute attrib : entity.getAttributes()) {
						if (attrib.getName().equals("age")) {
							attributeToDelete = attrib;
						}
					}
					if (attributeToDelete != null) {
						entity.getAttributes().remove(attributeToDelete);
					}
					
					EntityAttribute attrib = new EntityAttribute();
					attrib.setName("new");
					attrib.setDisplayName("New");
					attrib.setDescription("New");
					attrib.setIndexed(false);
					attrib.setDisplayOrder(3);
					attrib.setDatatype(datatypeByName.get("short"));
			        attrib.setIsCustom(false);
					entity.addAttribute(attrib);
					
					// 1. update entity and attributes
					entity = entityManager.updateEntity(entity);
                    assertNotNull(entity);
	
					
					// found group to add new attribute
					EntityAttributeGroup group=null;
					List<EntityAttributeGroup> groups = entityManager.loadEntityAttributeGroups(entity);	
					for (EntityAttributeGroup attributeGroup : groups) {
						if (attributeGroup.getName().equals("birth")) {
							group = attributeGroup;
						}
					}
					
					EntityAttributeGroupAttribute groupAttribute = new EntityAttributeGroupAttribute();					  
					groupAttribute.setEntityAttributeGroup(group);
					groupAttribute.setEntityAttribute( entity.findAttributeByName("new") );			  
					group.addEntityAttributeGroupAttribute(groupAttribute);
					  
					groups.add(group);
					
					// 2. update entity groups with attribute
					entityManager.updateEntityAttributeGroups(groups, entity);
					
					

					EntityAttribute attribute = entity.findAttributeByName("new");
					
					List<EntityAttributeValidation> attributeValidations = new ArrayList<EntityAttributeValidation>();						
					EntityAttributeValidation validation = new EntityAttributeValidation();
					EntityAttributeValidationParameter param = new EntityAttributeValidationParameter();
					
					validation.setName("nullityValidationRule");
					validation.setDisplayName("Nullity Validation Rule");
					validation.setEntityAttribute(attribute);
						
					attributeValidations.add(validation);
					
					// 3. update entity attribute validations										
					entityManager.updateEntityAttributeValidations(attributeValidations, entity, attribute.getName());										
								
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testInitializationOrientalDB() {	
		RecordManagerService manager = Context.getRecordManagerService();
		EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
		try {	
			List<Entity> entities = defService.findEntitiesByName("person");		
		    assertTrue("Not found entities.", entities.size() > 0);
		    
			Entity entity = entities.get(0);

					
			// initialize Entity Model database
			manager.initializeStore(entity);
					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testInitializationOrientalDBRecords() {	
		
		RecordManagerService manager = Context.getRecordManagerService();
		PersonQueryService personQueryService = Context.getPersonQueryService();
		EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
		
		try {		
				List<Entity> entities = defService.findEntitiesByName("person");		
			    assertTrue("Not found entities.", entities.size() > 0);
	    
				Entity entity = entities.get(0);
				

					List<IdentifierDomain> domains = personQueryService.getIdentifierDomains();
					assertTrue("No identifier domains in the system.", domains.size() > 0);

					IdentifierDomain testDomain = domains.get(0);
					int start=0;
					int count = 100;
					long startTime = new java.util.Date().getTime();
					Record savedRecord = null;
					for (int i=start; i < start+count; i++) {
						Record record = new Record(entity);
						record.set("givenName", "John" + i);
						record.set("familyName", "Smith" + i);
						record.set("dateOfBirth", new java.util.Date());
						record.set("birthOrder", i);
						addRecordIdentifiersAttribute(record, testDomain, i);
						record = manager.addRecord(entity, record);
						if (i == 0) {
							savedRecord = record;
						}
					}
					long endTime = new java.util.Date().getTime();
					log.debug("inserted " + count + " records in " + (endTime-startTime)/1000 + " secs.");
					
					if(start==0 ){
						manager.deleteRecord(entity, savedRecord);
						log.debug("Deleted record with ID " + savedRecord.getRecordId());
					}
					
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
		}
	}

	public void testAddEntityInstance() {
		RecordManagerService manager = Context.getRecordManagerService();
		PersonQueryService personQueryService = Context.getPersonQueryService();
		EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();

		try {		
				List<Entity> entities = defService.findEntitiesByName("person");	
			    assertTrue("Not found entities.", entities.size() > 0);

				Entity entity = entities.get(0);

					List<IdentifierDomain> domains = personQueryService.getIdentifierDomains();
					assertTrue("No identifier domains in the system.", domains.size() > 0);

					IdentifierDomain testDomain = domains.get(0);
					
					Record record = new Record(entity);
					record.set("givenName", "John123");
					record.set("familyName", "Smith");
					record.set("dateOfBirth", new java.util.Date());
					record.set("birthOrder", 1);
					addRecordIdentifiersAttribute(record, testDomain, 123);
					record = manager.addRecord(entity, record);
					
			        assertNotNull(record);
					
		} catch (ApplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
		}
	}
	
	public void testUpdateEntityInstance() {
		RecordManagerService manager = Context.getRecordManagerService();
		PersonQueryService personQueryService = Context.getPersonQueryService();
		RecordQueryService queryService = Context.getRecordQueryService();
		EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();

		try {	
				List<Entity> entities = defService.findEntitiesByName("person");	
			    assertTrue("Not found entities.", entities.size() > 0);

				Entity entity = entities.get(0);

					Record recordSearch = new Record(entity);
					recordSearch.set("givenName", "John123");
					recordSearch.set("familyName", "Smith");
					List<Record> list = queryService.findRecordsByAttributes(entity, recordSearch);		
					assertTrue("Not found record.", list.size() > 0);

					Record updateRecord = list.get(0);
	
					List<IdentifierDomain> domains = personQueryService.getIdentifierDomains();
					assertTrue("No identifier domains in the system.", domains.size() > 0);

					IdentifierDomain testDomain = domains.get(1);
					
					addRecordIdentifiersAttribute(updateRecord, testDomain, 123321);		
					updateRecord.set("familyName", "SmithUpdate");					
					updateRecord = manager.updateRecord(entity, updateRecord);
					
			        assertNotNull(updateRecord);
					
		} catch (ApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	}

	public void testDeleteEntityInstance() {
		RecordManagerService manager = Context.getRecordManagerService();
		RecordQueryService queryService = Context.getRecordQueryService();
		EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
		
		try {			
			List<Entity> entities = defService.findEntitiesByName("person");	
			assertTrue("Not found entities.", entities.size() > 0);
			
			Entity entity = entities.get(0);

					
			Identifier id = new Identifier();
			id.setIdentifier("identifier-123321");
			List<Record> list = queryService.findRecordsByIdentifier(entity, id);
			assertTrue("Not found record.", list.size() > 0);					

			Record deleteRecord = list.get(0);
	
			manager.deleteRecord(entity, deleteRecord);
					
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testFindEntitiesByAttributes() {
		RecordQueryService queryService = Context.getRecordQueryService();
		EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();

		try {			
			List<Entity> entities = defService.findEntitiesByName("person");	
			assertTrue("Not found entities.", entities.size() > 0);
			
			Entity entity = entities.get(0);

			Record record = new Record(entity);
			record.set("givenName", "John1%");

			Long count = queryService.getRecordCount(entity, record);
			
			long startTime = new java.util.Date().getTime();
			List<Record> records = queryService.findRecordsByAttributes(entity, record);
			long endTime = new java.util.Date().getTime();
			log.debug("Using exact query located " + records.size() + " records in " + (endTime-startTime)/1000 + " secs.");
			for (Record rec : records) {
				log.debug("Record is " + rec.asString());
			}	
			
			assertTrue("Not found entities.", records.size() == count.intValue());
					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void testFindEntitiesByAttributesPaged() {
		RecordQueryService queryService = Context.getRecordQueryService();
		EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();

		try {			
			List<Entity> entities = defService.findEntitiesByName("person");	
			assertTrue("Not found entities.", entities.size() > 0);
			
			Entity entity = entities.get(0);

			Record record = new Record(entity);
			record.set("familyName", "Smith2%");
			
			long startTime = new java.util.Date().getTime();
			List<Record> records = queryService.findRecordsByAttributes(entity, record, 0, 5);
			long endTime = new java.util.Date().getTime();
			log.debug("Using exact query located " + records.size() + " records in " + (endTime-startTime)/1000 + " secs.");
			for (Record rec : records) {
				log.debug("Record is " + rec.asString());
			}	
			
			assertTrue("Not found entities.", records.size() == 5);
					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void testFindEntitiesByIdentifier() {
		RecordQueryService queryService = Context.getRecordQueryService();
		EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();

		try {			
			List<Entity> entities = defService.findEntitiesByName("person");	
			assertTrue("Not found entities.", entities.size() > 0);
			
			Entity entity = entities.get(0);

			Identifier id = new Identifier();
			id.setIdentifier("identifier-1%");

			Long count = queryService.getRecordCount(entity, id);
			
			long startTime = new java.util.Date().getTime();
			List<Record> records = queryService.findRecordsByIdentifier(entity, id);
			long endTime = new java.util.Date().getTime();
			log.debug("Using exact query located " + records.size() + " records in " + (endTime-startTime)/1000 + " secs.");
			for (Record rec : records) {
				log.debug("Record is " + rec.asString());
			}	
			
			assertTrue("Not found entities.", records.size() == count.intValue());
					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void testFindEntitiesByIdentifierPaged() {
		RecordQueryService queryService = Context.getRecordQueryService();
		EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();

		try {			
			List<Entity> entities = defService.findEntitiesByName("person");	
			assertTrue("Not found entities.", entities.size() > 0);
			
			Entity entity = entities.get(0);

			Identifier id = new Identifier();
			id.setIdentifier("identifier-2%");
			
			long startTime = new java.util.Date().getTime();
			List<Record> records = queryService.findRecordsByIdentifier(entity, id, 2, 7);
			long endTime = new java.util.Date().getTime();
			log.debug("Using exact query located " + records.size() + " records in " + (endTime-startTime)/1000 + " secs.");
			for (Record rec : records) {
				log.debug("Record is " + rec.asString());
			}	
			
			assertTrue("Not found entities.", records.size() == 7);
					
		} catch (Exception e) {
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

					Record recordSearch = new Record(entity);
					recordSearch.set("givenName", "John%");
					recordSearch.set("familyName", "Smith%");
					
					List<Record> list = queryService.findRecordsByAttributes(entity, recordSearch);	
					assertTrue("Not found record.", list.size() > 0);
					
					for (Record record : list) {					
						manager.deleteRecord(entity, record);
					}					
		} catch (ApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	}
		
	public void testShutdownOrientalDB() {	
		RecordManagerService manager = Context.getRecordManagerService();
		EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
		try {		
		List<Entity> entities = defService.findEntitiesByName("person");
		assertTrue("Not found entities.", entities.size() > 0);
		
				Entity entity = entities.get(0);
					
				// Shutdown Entity Model database
				manager.shutdownStore(entity);
					
		} catch (Exception e) {
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
    			
    			Entity entity = entities.get(0);
    			log.debug("Deleting entity " + entity);
    					
    			// delete groups
    			List<EntityAttributeGroup> groups = entityManager.loadEntityAttributeGroups(entity);				
    			for (EntityAttributeGroup group : groups) {				
    				entityManager.deleteEntityAttributeGroup(group);
    			}					
    					
    			// delete validations
    			for (EntityAttribute attribute : entity.getAttributes()) {
    				List<EntityAttributeValidation> validations = entityManager.loadEntityAttributeValidations(attribute);
    				for (EntityAttributeValidation validation : validations) {		
    							 
    					entityManager.deleteEntityAttributeValidation(validation);
    				}										     
    						 
    			}	
    			// delete entity
    			entityManager.deleteEntity(entity);	
    			
    			entities = entityManager.loadEntities();
    			log.debug("After delete found " + entities.size() + " entities.");
            }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        // Context.shutdown();
	}
}
