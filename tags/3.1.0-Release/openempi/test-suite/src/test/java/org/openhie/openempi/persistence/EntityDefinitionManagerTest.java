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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.EntityAttributeGroup;
import org.openhie.openempi.model.EntityAttributeGroupAttribute;
import org.openhie.openempi.model.EntityAttributeValidation;
import org.openhie.openempi.model.EntityAttributeValidationParameter;
import org.openhie.openempi.service.BaseServiceTestCase;

public class EntityDefinitionManagerTest extends BaseServiceTestCase
{
	private static List<EntityAttributeDatatype> datatypes;
	private static Map<String,EntityAttributeDatatype> datatypeByName = new HashMap<String,EntityAttributeDatatype>();
	
	public void testGetEntityAttributeDatatypes() {
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

	public Entity getTestEntity() {
		Entity entity = new Entity();
		entity.setName("personTest");
		entity.setDescription("Entity that represents a person and his or her demographic attributes");
		entity.setDisplayName("Person Test");
		entity.setVersionId(1);
		
		Set<EntityAttribute> attributes = new HashSet<EntityAttribute>();
		entity.setAttributes(attributes);
		
		EntityAttribute attrib = new EntityAttribute();
		attrib.setName("firstName");
		attrib.setDisplayName("First Name");
		attrib.setDescription("Given name");
		attrib.setIndexed(true);
		attrib.setDisplayOrder(1);
		attrib.setDatatype(datatypeByName.get("string"));
        attrib.setIsCustom(false);
		attributes.add(attrib);
		
		attrib = new EntityAttribute();
		attrib.setName("lastName");
		attrib.setDisplayName("Last Name");
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

	public void testAddEntityDefinition() {
		EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
		Entity entity = getTestEntity();
		try {
			entity = entityManager.addEntity(entity);
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
		log.debug("Added entity " + entity);
	}
	
	public void testGetEntityDefinitions() {
		EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
		try {
			List<Entity> entities = entityManager.loadEntities();
		    assertTrue("Not found entities.", entities.size() > 0);
		    
			for (Entity entity : entities) {
				log.debug("Found entity " + entity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testEntityNameValidation() {
		EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
		try {
			List<Entity> entities = entityManager.loadEntities();
			if (entities.size() == 0) {
				return;
			}
			Entity entity = entities.get(0);
			entity.setEntityVersionId(null);
			entity.setVersionId(1);
			entity.setEntityId(null);
			entity = entityManager.addEntity(entity);
			assertTrue("The system should not allow you to create an entity by the same name", false);
		} catch (Exception e) {
			log.debug("Expected exception of type ApplicationException due to duplicate name error." +e.getMessage());
		}
	}
	
	public void testEntityAttributeGroup() {
		EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
		try {
			List<Entity> entities = entityManager.findEntitiesByName("personTest");
		    assertTrue("Not found entities.", entities.size() > 0);
		   
			Entity entity = entities.get(0);
			EntityAttributeGroup group = new EntityAttributeGroup();
			group.setName("testGroup");
			group.setDisplayName("Test Group");
			group.setDisplayOrder(1);
			group.setEntity(entity);
			EntityAttributeGroupAttribute groupAttribute = new EntityAttributeGroupAttribute();
			group.addEntityAttributeGroupAttribute(groupAttribute);
			groupAttribute.setEntityAttributeGroup(group);
			groupAttribute.setEntityAttribute(entity.findAttributeByName("firstName"));
			
			groupAttribute = new EntityAttributeGroupAttribute();
			group.addEntityAttributeGroupAttribute(groupAttribute);
			groupAttribute.setEntityAttributeGroup(group);
			groupAttribute.setEntityAttribute(entity.findAttributeByName("lastName"));
			
			entityManager.addEntityAttributeGroup(group);
			
			List<EntityAttributeGroup> groups = entityManager.loadEntityAttributeGroups(entity);
			for (EntityAttributeGroup fgroup : groups) {
				log.debug("Found attribute group: " + fgroup);
			}
			entityManager.deleteEntityAttributeGroup(group);
			groups = entityManager.loadEntityAttributeGroups(entity);
		    assertTrue("Found groups. Not deleted", groups.size() == 0);
		    
			log.debug("After delete of attribute group, groups is: " + groups.size());
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void testEntityAttributeValidation() {
		EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
		try {
			List<Entity> entities = entityManager.findEntitiesByName("personTest");
		    assertTrue("Not found entities.", entities.size() > 0);
		    
			Entity entity = entities.get(0);
			EntityAttribute attribute = entity.findAttributeByName("firstName");
			EntityAttributeValidation validation = new EntityAttributeValidation();
			validation.setName("maxLength");
			validation.setDisplayName("Maximum Length");
			validation.setEntityAttribute(attribute);
			EntityAttributeValidationParameter param = new EntityAttributeValidationParameter();
			param.setEntityAttributeValidation(validation);
			param.setName("length");
			param.setValue("10");
			validation.addParameter(param);
			validation = entityManager.addEntityAttributeValidation(validation);			
			log.debug("The entity attribute validation is: " + validation);	
			
			attribute = entity.findAttributeByName("firstName");
			List<EntityAttributeValidation> validations = entityManager.loadEntityAttributeValidations(attribute);
		    assertTrue("Not found validations.", validations.size() > 0);
		    
		    validation = validations.get(0);
			entityManager.deleteEntityAttributeValidation(validation);		    
			log.debug("The entity attribute validation is: " + validation);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void testUpdateEntityDefinition() {
		EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
		try {
			List<Entity> entities = entityManager.findEntitiesByName("personTest");
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
			entity.addAttribute(attrib);
			entity = entityManager.updateEntity(entity);
			for (EntityAttribute attribute : entity.getAttributes()) {
				log.debug("Attibute: " + attribute);
			}
			log.debug("After update found " + entity + " entities.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testDeleteEntityDefinition() {
		EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
		try {
			List<Entity> entities = entityManager.findEntitiesByName("personTest");
		    assertTrue("Not found entities.", entities.size() > 0);
		    
			for (Entity entity : entities) {
				log.debug("Deleting entity " + entity);
				entityManager.deleteEntity(entity);
			}
			entities = entityManager.loadEntities();
			log.debug("After delete found " + entities.size() + " entities.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
