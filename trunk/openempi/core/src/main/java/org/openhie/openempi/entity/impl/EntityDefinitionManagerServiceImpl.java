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
package org.openhie.openempi.entity.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.DaoException;
import org.openhie.openempi.InitializationException;
import org.openhie.openempi.configuration.ConfigurationRegistry;
import org.openhie.openempi.configuration.CustomField;
import org.openhie.openempi.configuration.xml.model.AttributeType;
import org.openhie.openempi.configuration.xml.model.AttributesType;
import org.openhie.openempi.configuration.xml.model.EntityModel;
import org.openhie.openempi.configuration.xml.model.ValidationsType;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.PersistenceLifecycleObserver;
import org.openhie.openempi.entity.dao.EntityDefinitionDao;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.EntityAttributeGroup;
import org.openhie.openempi.model.EntityAttributeValidation;
import org.openhie.openempi.notification.EventObservable;
import org.openhie.openempi.notification.ObservationEventType;
import org.openhie.openempi.service.impl.BaseServiceImpl;
import org.openhie.openempi.util.ConvertUtil;

public class EntityDefinitionManagerServiceImpl extends BaseServiceImpl
	implements EntityDefinitionManagerService, PersistenceLifecycleObserver, Observer
{
	private static final String ENTITY_DEFINITION_CACHE = "entityDefinitionCache";
	private static final int MAX_ENTITY_DEFINITION_CACHE_ELEMENTS = 100;
	
	private static boolean initialized = false;
	private EntityDefinitionDao entityDao;
	private static CacheManager cacheManager;
	private static Cache entityDefinitionCache;
	private static List<EntityAttributeDatatype> datatypes;
	
	public EntityDefinitionManagerServiceImpl() {
	}
	
	public Entity addEntity(Entity entity) throws ApplicationException {
		if (entity == null) {
			return null;
		}
		
		if (entity.getEntityVersionId() != null) {
			throw new ApplicationException("This entity definition already exists so it can only be updated.");
		}
		
		validateNameUniqueness(entity);
		try {
			entity.setUserCreatedBy(Context.getUserContext().getUser());
			Date dateCreated = new Date();
			entity.setDateCreated(dateCreated);
			for (EntityAttribute attribute : entity.getAttributes()) {
				attribute.setDateCreated(dateCreated);
				attribute.setUserCreatedBy(Context.getUserContext().getUser());
				attribute.setEntity(entity);
			}
			entityDao.addEntity(entity);
			addToCache(entity);
			return entity;
		} catch (DaoException e) {
			throw new ApplicationException(e.getMessage());
		}
	}

	public EntityAttributeGroup addEntityAttributeGroup(EntityAttributeGroup entityAttributeGroup) throws ApplicationException {
		if (entityAttributeGroup == null || entityAttributeGroup.getEntity() == null || 
				entityAttributeGroup.getEntityAttributes() == null) {
			throw new ApplicationException("This entity attribute group is invalid.");
		}
		if (entityAttributeGroup.getEntityAttributeGroupId() != null) {
			throw new ApplicationException("This entity attribute group already exists so it can only be updated.");
		}
		try {
			entityDao.addEntityAttributeGroup(entityAttributeGroup);
			return entityAttributeGroup;
		} catch (DaoException e) {
			throw new ApplicationException(e.getMessage());
		}
	}

	public EntityAttributeValidation addEntityAttributeValidation(EntityAttributeValidation validation) throws ApplicationException {
		if (validation == null || validation.getEntityAttribute() == null || validation.getParameters() == null) {
			throw new ApplicationException("This entity attribute validation is invalid.");
		}
		if (validation.getEntityAttributeValidationId() != null) {
			throw new ApplicationException("This entity attribute validation already exists so it can only be updated.");
		}
		try {
			entityDao.addEntityAttributeValidation(validation);
			return validation;
		} catch (DaoException e) {
			throw new ApplicationException(e.getMessage());
		}
	}

	public Entity updateEntity(Entity entity) throws ApplicationException  {
		if (entity == null) {
			return entity;
		}
		if (entity.getEntityVersionId() == null) {
			throw new ApplicationException("An entity definition must first be created before it is updated.");
		}
		try {
			Date currentDate = new Date();
			entity.setDateChanged(currentDate);
			entity.setUserChangedBy(Context.getUserContext().getUser());
			for (EntityAttribute attrib : entity.getAttributes()) {
				if (attrib.getDateCreated() == null) {
					attrib.setDateCreated(currentDate);
					attrib.setUserCreatedBy(Context.getUserContext().getUser());
					// attrib.setEntity(entity);
				}
				attrib.setEntity(entity);
			}
			
			// Mark deletion attributes
			handleAttributeUpdates(entity);
			
			for (EntityAttribute attrib : entity.getAttributes()) {
				System.out.println(attrib);
			}	
			
			// remove validations for marked attribute
			for (EntityAttribute attrib : entity.getAttributes()) {			
				if (attrib.getUserVoidedBy() != null) {
					
				    // remove validations
				    List<EntityAttributeValidation> existingValidations = loadEntityAttributeValidations(attrib);	
					for (EntityAttributeValidation validation : existingValidations) {	
						deleteEntityAttributeValidation(validation);				
					}				    
				}
			}					
			
			Entity updatedEntity = entityDao.updateEntity(entity);			
			updatedEntity = entityDao.loadEntity(entity.getEntityVersionId());
			
			addToCache(updatedEntity);
			return updatedEntity;
		} catch (DaoException e) {
			throw new ApplicationException(e.getMessage());
		}
	}
	
	public EntityAttributeValidation updateEntityAttributeValidation(EntityAttributeValidation validation) throws ApplicationException {
		if (validation == null) {
			return null; 
		}
		if (validation.getEntityAttributeValidationId() == null || validation.getParameters() == null) {
			throw new ApplicationException("An entity attribute validation must first be created before it is updated.");
		}
		try {
			entityDao.updateEntityAttributeValidation(validation);
			return validation;
		} catch (DaoException e) {
			throw new ApplicationException(e.getMessage());
		}
	}

	public void updateEntityAttributeValidations(List<EntityAttributeValidation> validations, Entity updatedEntity, String attributeName) throws ApplicationException {
		if (validations == null) {
			return; 
		}
		
		Entity existing = entityDao.loadEntity(updatedEntity.getEntityVersionId());		
		EntityAttribute existingAttribute = existing.findAttributeByName(attributeName);	
	    List<EntityAttributeValidation> existingValidations = loadEntityAttributeValidations(existingAttribute);
	     
		Map<Integer,EntityAttributeValidation> newValidationById = new HashMap<Integer,EntityAttributeValidation>();			
		for (EntityAttributeValidation validation : validations) {
			if( validation.getEntityAttributeValidationId() != null ){
				newValidationById.put(validation.getEntityAttributeValidationId(), validation);
			}
		}

		// remove deleted validations
		for (EntityAttributeValidation validation : existingValidations) {
			if (newValidationById.get(validation.getEntityAttributeValidationId()) == null) {				
				deleteEntityAttributeValidation(validation);				
			}
		}
		
		for (EntityAttributeValidation newValidation : validations) {		
			if( newValidation.getEntityAttributeValidationId() != null ) {
				updateEntityAttributeValidation(newValidation);
			} else {			
				addEntityAttributeValidation(newValidation);
			}		 
		}	
	}
	
	public EntityAttributeGroup updateEntityAttributeGroup(EntityAttributeGroup entityAttributeGroup) throws ApplicationException {
		if (entityAttributeGroup == null) {
			return null; 
		}

		if (entityAttributeGroup.getEntityAttributeGroupId() == null || entityAttributeGroup.getEntityAttributes() == null) {
			throw new ApplicationException("An entity attribute group must first be created before it is updated.");
		}
		
		try {
			entityDao.updateEntityAttributeGroup(entityAttributeGroup);
			return entityAttributeGroup;
		} catch (DaoException e) {
			throw new ApplicationException(e.getMessage());
		}
	}

	public void updateEntityAttributeGroups(List<EntityAttributeGroup> groups, Entity updatedEntity) throws ApplicationException {
		if (groups == null) {
			return; 
		}
		
		Entity existing = entityDao.loadEntity(updatedEntity.getEntityVersionId());		
		List<EntityAttributeGroup> existingGroups = loadEntityAttributeGroups(existing);
		
		Map<Integer,EntityAttributeGroup> newGroupById = new HashMap<Integer,EntityAttributeGroup>();		
		for (EntityAttributeGroup group : groups) {
			newGroupById.put(group.getEntityAttributeGroupId(), group);
		}

		// remove deleted groups
		for (EntityAttributeGroup group : existingGroups) {
			if (newGroupById.get(group.getEntityAttributeGroupId()) == null) {				
				deleteEntityAttributeGroup(group);				
			}
		}

		for (EntityAttributeGroup newgroup : groups) {
			if( newgroup.getEntityAttributeGroupId() != null ) {
				updateEntityAttributeGroup(newgroup);
			} else {			
				addEntityAttributeGroup(newgroup);
			}
		} 					
	}
	
	public void deleteEntity(Entity entity) throws ApplicationException {
		if (entity == null) {
			return;
		}
		if (entity.getEntityVersionId() == null) {
			throw new ApplicationException("An entity definition must first be created before it is deleted.");
		}
		try {
			Date dateVoided = new Date();
			entity.setDateVoided(dateVoided);
			entity.setUserVoidedBy(Context.getUserContext().getUser());
			for (EntityAttribute attribute : entity.getAttributes()) {
				attribute.setDateVoided(dateVoided);
				attribute.setUserVoidedBy(Context.getUserContext().getUser());
				attribute.setEntity(entity);
			}
			entityDao.updateEntity(entity);
			addToCache(entity);
		} catch (DaoException e) {
			throw new ApplicationException(e.getMessage());
		}
	}
	
	public void deleteEntityAttributeGroup(EntityAttributeGroup entityAttributeGroup) throws ApplicationException {
		if (entityAttributeGroup == null) {
			return;
		}
		if (entityAttributeGroup.getEntityAttributeGroupId() == null) {
			throw new ApplicationException("An entity attribute group must first be created before it is deleted.");
		}
		try {
			entityDao.deleteEntityAttributeGroup(entityAttributeGroup);
		} catch (DaoException e) {
			throw new ApplicationException(e.getMessage());
		}
	}
	
	public void deleteEntityAttributeValidation(EntityAttributeValidation validation) throws ApplicationException {
		if (validation == null) {
			return;
		}
		if (validation.getEntityAttributeValidationId() == null) {
			throw new ApplicationException("An entity attribute validation must first be created before it is deleted.");
		}
		try {
			entityDao.deleteEntityAttributeValidation(validation);
		} catch (DaoException e) {
			throw new ApplicationException(e.getMessage());
		}
	}

	public Entity loadEntity(Integer id) {
		Entity entity = getFromCache(id);
		if (entity != null) {
			return entity;
		}
		entity = entityDao.loadEntity(id);
		return entity;
	}

	public List<Entity> findEntitiesByName(String name) {
		List<Entity> entities =  entityDao.findEntitiesByName(name);
		for (Entity entity : entities) {
			addToCache(entity);
		}
		return entities;
	}

	/**
	 * Will export the entity passed in as the first parameter in the method into a file.
	 * The filename is assumed to be relative to the data-directory defined in the mpi-config.xml
	 * file and is not supposed to be a fully qualified filename. Ultimately this will be
	 * invoked by the client and we don't want the client to be able to export files anywhere
	 * on the server.
	 *
	 */
	public String exportEntity(Entity entity, String filename) throws ApplicationException {
        if (entity == null) {
            return "";
        }

        try {
            // 1. convert Entity to EntityModel
            EntityModel entityModel = ConvertUtil.mapToEntityModel(entity, EntityModel.class);

            // 2. set group info to EntityModel
            List<EntityAttributeGroup> groups = loadEntityAttributeGroups(entity);
            entityModel = ConvertUtil.setGroupInfoToEntityModel(groups, entityModel);

            // 3. set validation info to entity attribute
            for (EntityAttribute attribute : entity.getAttributes()) {
                 List<EntityAttributeValidation> validations = loadEntityAttributeValidations(attribute);

                 ValidationsType validationsType = ConvertUtil.mapToEntityValidations(validations);

                 // set validations info to attributeType
                 AttributesType attributesType = entityModel.getAttributes();
                 if (attributesType != null) {
                     for (AttributeType attributeType: attributesType.getAttribute()) {
                         String name = attributeType.getName();

                         if (attribute.getName().equals(name)) {
                             attributeType.setValidations(validationsType);
                         }
                     }
                 }
            }

            // export to xml format
            JAXBContext jaxbContext = JAXBContext.newInstance(EntityModel.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            OutputStream outputStream = new ByteArrayOutputStream();
            jaxbMarshaller.marshal(entityModel, outputStream);

            // write to file
            File fileOut = new File(filename);
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(fileOut));

                writer.write(outputStream.toString());
                writer.close();

                return outputStream.toString();

            } catch (IOException e) {
                throw new RuntimeException("Unable to write new output file: " + filename);
            }
        } catch (JAXBException e) {
            log.warn("Unable to serialize a Entity into a string: " + e, e);
            throw new RuntimeException(e);
        }
	}

	/**
	 * Will import the entity definition specified in the relative filename of the parameter.
	 */
	public void importEntity(String filename) throws ApplicationException {
        if (filename.isEmpty()) {
            return;
        }

        try {
            File file = new File(filename);
            log.debug("Loading file " + file.getAbsolutePath());
            if (!file.isFile() || !file.canRead()) {
                log.error("Input file is not available.");
                throw new RuntimeException("Input file " + filename + " is not readable.");
            }

            // Import from file
            JAXBContext jaxbContext = JAXBContext.newInstance(EntityModel.class);
            Unmarshaller unJaxbMarshaller = jaxbContext.createUnmarshaller();

            EntityModel entityModel = (EntityModel) unJaxbMarshaller.unmarshal(new FileInputStream(file));

            // convert EntityModel to Entity, EntityAttributeGroups, EntityAttributeValidations
            Entity entity = ConvertUtil.mapToEntityModel(entityModel, getEntityAttributeDatatypes(), Entity.class);
            
            // check existing entity
            List<Entity> entities = entityDao.findEntitiesByName(entity.getName());
            for (Entity en : entities) {
                if (en.getVersionId().intValue() == entity.getVersionId().intValue()) {
                    log.error("Entity definition already exists: " + entity.getName());
                    throw new RuntimeException("Entity definition in the file " + filename + " already exists.");
                }
            }

            // add entity
            Entity newEntity = addEntity(entity);

            // 2. add entity groups with attribute
            List<EntityAttributeGroup> groups = ConvertUtil.mapToEntityGroup(entityModel, newEntity, org.openhie.openempi.model.EntityAttributeGroup.class);
            for (EntityAttributeGroup group : groups) {
                addEntityAttributeGroup(group);
            }

            // 3. add entity attribute validations
            for (AttributeType attributeType : entityModel.getAttributes().getAttribute()) {
                 List<EntityAttributeValidation> validations = ConvertUtil.mapToEntityValidations(attributeType, newEntity, org.openhie.openempi.model.EntityAttributeValidation.class);
                 for (EntityAttributeValidation validation : validations) {
                     addEntityAttributeValidation(validation);
                 }
            }
            return;
        } catch (Exception e) {
            log.warn("Unable to Unmarshal xml to a EntityModel: " + e, e);
            throw new RuntimeException(e);
        }
	}

	private Entity findLatestEntityVersionByName(String name) {
		List<Entity> entities = findEntitiesByName(name);
		if (entities.size() == 0) {
			return null;
		}
		if (entities.size() == 1) {
			return entities.get(0);
		}
		Entity latestEntity = entities.get(0);
		for (Entity entity : entities) {
			if (entity.getEntityVersionId() > latestEntity.getEntityVersionId()) {
				latestEntity = entity;
			}
		}
		return latestEntity;
	}

	public List<Entity> loadEntities() {
		List<Entity> entities =  entityDao.loadEntities();
		// Since we got all of them again, we might as well update the cache
		for (Entity entity : entities) {
			addToCache(entity);
		}
		return entities;
	}

	public EntityAttributeGroup loadEntityAttributeGroup(Integer id) {
		return entityDao.loadEntityAttributeGroup(id);
	}

	public List<EntityAttributeGroup> loadEntityAttributeGroups(Entity entity) {
		return entityDao.loadEntityAttributeGroups(entity);
	}

	public EntityAttributeValidation loadEntityAttributeValidation(Integer id) {
		return entityDao.loadEntityAttributeValidation(id);
	}

	public List<EntityAttributeValidation> loadEntityAttributeValidations(EntityAttribute entityAttribute) {
		return entityDao.loadEntityAttributeValidations(entityAttribute);
	}

	public List<CustomField> loadCustomFields(String entityName) {
		Entity entity = findLatestEntityVersionByName(entityName);
		if (entity == null) {
			return new ArrayList<CustomField>();
		}
		List<EntityAttribute> attribs = entityDao.loadCustomFields(entity);
		List<CustomField> fields = new ArrayList<CustomField>(attribs.size());
		if (attribs.size() == 0) {
			return fields;
		}
		for (EntityAttribute attrib : attribs) {
			CustomField field = buildCustomFieldFromAttribute(attrib);
			fields.add(field);
		}
		return fields;
	}

	public CustomField findCustomField(String entityName, String fieldName) {
		Entity entity = findLatestEntityVersionByName(entityName);
		if (entity == null) {
			return null;
		}
		EntityAttribute attrib = entityDao.findCustomField(entity, fieldName);
		if (attrib == null) {
			return null;
		}
		CustomField field = buildCustomFieldFromAttribute(attrib);
		if (log.isDebugEnabled()) {
			log.debug("Found custom field: " + field);
		}
		return field;
	}

	public CustomField addCustomField(CustomField field) throws ApplicationException {
		if (field == null ||
			ConvertUtil.isNullOrEmpty(field.getEntityName()) ||
			ConvertUtil.isNullOrEmpty(field.getFieldName()) ||
			ConvertUtil.isNullOrEmpty(field.getSourceFieldName())) {
			log.info("The custom field to be added is invalid: " + field);
			throw new ApplicationException("Unable to add invalid custom field definition.");
		}

		// Validate that a custom field by the same name does not already exist.
		List<CustomField> fields = loadCustomFields(field.getEntityName());
		for (CustomField custom : fields) {
			if (custom.getFieldName().equalsIgnoreCase(field.getFieldName())) {
				log.warn("User attempted to add a custom field " + field + " that already exists in the system.");
				throw new ApplicationException("Unable to add a custom field that already exists in the system.");
			}
		}
		
		Entity entity = findLatestEntityVersionByName(field.getEntityName());
		try {
			EntityAttribute attribute = buildAttributeFromCustomField(entity, field);
			entity.addAttribute(attribute);
			//TODO: Validate that the source field is known
			//TODO: Validate that the transformation function is known
			attribute = entityDao.addCustomField(entity, attribute);
			// Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
			Context.notifyObserver(ObservationEventType.CUSTOM_FIELD_ADD_EVENT, field);
			return field;
		} catch (DaoException e) {
			log.error("Failed while saving a custom field: " + e, e);
			throw new ApplicationException("Failed while saving the custom field: " + e.getMessage());
		}
	}

	public void updateCustomField(CustomField field) throws ApplicationException {
		if (field == null ||
				ConvertUtil.isNullOrEmpty(field.getEntityName()) ||
				ConvertUtil.isNullOrEmpty(field.getFieldName()) ||
				ConvertUtil.isNullOrEmpty(field.getSourceFieldName())) {
			log.info("The custom field to be updated is invalid: " + field);
			throw new ApplicationException("Unable to update custom field with an invalid custom field definition.");
		}
		Entity entity = findLatestEntityVersionByName(field.getEntityName());
		EntityAttribute attrib = null;
		for (EntityAttribute item : entity.getAttributes()) {
			if (item.getName().equalsIgnoreCase(field.getFieldName()) && 
					item.getDateVoided() == null) {
				attrib = item;
			}
		}
		if (attrib == null) {
			log.info("The user attempted to update a field that does not exist.");
			throw new ApplicationException("Unable to update an unknown custom field definition.");
		}
		attrib.setFunctionParameters(serializeParameters(field.getConfigurationParameters()));
		attrib.setDateChanged(new Date());
		attrib.setSourceName(field.getSourceFieldName());
		attrib.setTransformationFunction(field.getTransformationFunctionName());
		attrib.setUserChangedBy(Context.getUserContext().getUser());
		try {
			entityDao.updateEntity(entity);
			// Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
			Context.notifyObserver(ObservationEventType.CUSTOM_FIELD_UPDATE_EVENT, field);
		} catch (DaoException e) {
			log.error("Failed while updating a custom field: " + e, e);
			throw new ApplicationException("Failed while updating the custom field: " + e.getMessage());
		}			
	}
	
	public void deleteCustomField(CustomField field) throws ApplicationException {
		if (field == null ||
				ConvertUtil.isNullOrEmpty(field.getEntityName()) ||
				ConvertUtil.isNullOrEmpty(field.getFieldName())) {
			log.warn("The custom field to be deleted is invalid: " + field);
			throw new ApplicationException("Unable to add invalid custom field definition.");
		}
		Entity entity = findLatestEntityVersionByName(field.getEntityName());
		EntityAttribute attrib = null;
		for (EntityAttribute item : entity.getAttributes()) {
			if (item.getName().equalsIgnoreCase(field.getFieldName()) && 
					item.getDateVoided() == null) {
				attrib = item;
			}
		}
		if (attrib == null) {
			log.info("The user attempted to delete a custom field that does not exist.");
			throw new ApplicationException("Unable to delete an unknown custom field definition.");
		}
		attrib.setDateVoided(new Date());
		attrib.setUserVoidedBy(Context.getUserContext().getUser());
		try {
			entityDao.updateEntity(entity);
			// Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
			Context.notifyObserver(ObservationEventType.CUSTOM_FIELD_DELETE_EVENT, field);			
		} catch (DaoException e) {
			log.error("Failed while deleting a custom field: " + e, e);
			throw new ApplicationException("Failed while deleting the custom field: " + e.getMessage());
		}	
	}

	public void startup() throws InitializationException {
		if (initialized) {
			return;
		}		
		cacheManager = CacheManager.create();

		//Create a Cache for storing entity definitions
		entityDefinitionCache = new Cache(
				new CacheConfiguration(ENTITY_DEFINITION_CACHE, MAX_ENTITY_DEFINITION_CACHE_ELEMENTS)
		    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
		    .eternal(false)
		    .timeToLiveSeconds(0)
		    .timeToIdleSeconds(0));
		cacheManager.addCache(entityDefinitionCache);
		preloadCache();
		Context.registerObserver(this, ObservationEventType.CUSTOM_FIELD_ADD_EVENT);
		Context.registerObserver(this, ObservationEventType.CUSTOM_FIELD_DELETE_EVENT);
		Context.registerObserver(this, ObservationEventType.CUSTOM_FIELD_UPDATE_EVENT);
		initialized = true;	
	}

	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	public boolean isDown() {
		// TODO Auto-generated method stub
		return false;
	}

	private void preloadCache() {
		List<Entity> entities = loadEntities();
		log.info("Pre-loaded configuration for " + entities.size() + " entities.");
		Map<String,List<CustomField>> customFieldsListByEntityName = new HashMap<String,List<CustomField>>();
		Map<String,Map<String,CustomField>> customFieldsMapByEntityName = new HashMap<String,Map<String,CustomField>>();
		for (Entity entity : entities) {
			List<CustomField> customFields = loadCustomFields(entity.getName());
			customFieldsListByEntityName.put(entity.getName(), customFields);
			log.info("Pre-loaded a list of " + customFields.size() + " custom fields for entity " + entity.getName());
			Map<String,CustomField> customFieldMap = new HashMap<String,CustomField>();
			for (CustomField field : customFields) {
				customFieldMap.put(field.getFieldName(), field);
			}
			customFieldsMapByEntityName.put(entity.getName(), customFieldMap);
		}
		Context.getConfiguration()
			.registerConfigurationEntry(ConfigurationRegistry.CUSTOM_FIELD_LIST_BY_ENTITY_NAME_MAP, customFieldsListByEntityName);
		Context.getConfiguration()
			.registerConfigurationEntry(ConfigurationRegistry.CUSTOM_FIELD_MAP_BY_ENTITY_NAME_MAP, customFieldsMapByEntityName);
	}

	@SuppressWarnings("unchecked")
	public void update(Observable o, Object eventData) {
		if (!(o instanceof EventObservable) || eventData == null || !(eventData instanceof CustomField)) {
			log.warn("Received unexpected event with data of " + eventData);
			return;
		}
		EventObservable event = (EventObservable) o;
		CustomField customField = (CustomField) eventData;
		Map<String,List<CustomField>> customFieldsListByEntityName = (Map<String, List<CustomField>>) Context
				.getConfiguration().lookupConfigurationEntry(ConfigurationRegistry.CUSTOM_FIELD_LIST_BY_ENTITY_NAME_MAP);
		Map<String,Map<String,CustomField>> customFieldsMapByEntityName = (Map<String, Map<String, CustomField>>) Context
				.getConfiguration().lookupConfigurationEntry(ConfigurationRegistry.CUSTOM_FIELD_MAP_BY_ENTITY_NAME_MAP);
		List<CustomField> fieldList = customFieldsListByEntityName.get(customField.getEntityName());
		Map<String,CustomField> fieldMap = customFieldsMapByEntityName.get(customField.getEntityName());
		if (event.getType() == ObservationEventType.CUSTOM_FIELD_ADD_EVENT) {
			log.debug("A new custom field was added; we need to update the in-memory registry: " + customField);
			fieldList.add(customField);
			fieldMap.put(customField.getFieldName(), customField);
		} else if (event.getType() == ObservationEventType.CUSTOM_FIELD_UPDATE_EVENT) {
			log.debug("A custom field was updated; we need to update the in-memory registry: " + customField);
			boolean found = false;
			for (CustomField item : fieldList) {
				if (item.getFieldName().equals(customField.getFieldName())) {
					found = true;
					item.setConfigurationParameters(customField.getConfigurationParameters());
					item.setSourceFieldName(customField.getSourceFieldName());
					item.setTransformationFunctionName(customField.getTransformationFunctionName());
					log.debug("As a result of an update custom field event, update field " + item + " in the list.");
				}
			}
			if (!found) {
				log.warn("Received an event to update an existing custom field but the field is not found in the registry.");
			}
			fieldMap.put(customField.getFieldName(), customField);
		} else if (event.getType() == ObservationEventType.CUSTOM_FIELD_DELETE_EVENT) {
			log.debug("A new custom field was deleted; we need to update the in-memory registry: " + customField);
			CustomField foundField=null;
			for (CustomField item : fieldList) {
				if (item.getFieldName().equals(customField.getFieldName())) {
					foundField = item;
				}
			}
			if (foundField != null) {
				fieldList.remove(foundField);
				log.debug("As a result of a delete custom field event, deleted field " + foundField + " from the list.");
			}
			fieldMap.remove(customField.getFieldName());
		}
	}

	public List<EntityAttributeDatatype> getEntityAttributeDatatypes() {
		if (datatypes == null) {
			datatypes = entityDao.getEntityAttributeDatatypes();
		}
		return datatypes;
	}
	
	private void validateNameUniqueness(Entity entity) throws ApplicationException {
		@SuppressWarnings("rawtypes")
		List keys = entityDefinitionCache.getKeys();
		for (int i=0; i < keys.size(); i++) {
			Object key = keys.get(i);
			Element elem = entityDefinitionCache.get(key);
			if( elem != null ) {
				Entity item = (Entity) elem.getObjectValue();
				if (item.getDateVoided() != null) {
				    continue;
				}
				
				if (entity.getName().equalsIgnoreCase(item.getName()) && entity.getVersionId() == item.getVersionId() ) {
					throw new ApplicationException("Cannot add entity because another entity by the same name already exists.");
				}
			}
		}
	}

	private void handleAttributeUpdates(Entity entity) {
		Entity existing = entityDao.loadEntity(entity.getEntityVersionId());
		
		Map<Integer,EntityAttribute> newAttribById = new HashMap<Integer,EntityAttribute>();
		for (EntityAttribute attribute : entity.getAttributes()) {
			newAttribById.put(attribute.getEntityAttributeId(), attribute);
		}
		// Mark for deletion attributes that were removed
		for (EntityAttribute attrib : existing.getAttributes()) {			
			if (newAttribById.get(attrib.getEntityAttributeId()) == null) {
				attrib.setDateVoided(entity.getDateChanged());
				attrib.setUserVoidedBy(Context.getUserContext().getUser());
				entity.addAttribute(attrib);
			}
		}		
	}
	
	private Entity getFromCache(Integer id) {
		if (id == null) {
			return null;
		}
		Element element = entityDefinitionCache.get(id);
		if (element == null) {
			return null;
		}
		return (Entity) element.getObjectValue();
	}

	private void addToCache(Entity entity) {
	    if (!entityDefinitionCache.getStatus().equals(Status.STATUS_ALIVE)) {
	        return;
	    }
	    
		Element element = entityDefinitionCache.get(entity.getEntityVersionId());
		if (element != null) {
			log.debug("Updating an entity definition entry in the cache: " + entity);
		} else {
			log.debug("Adding an entity definition entry in the cache: " + entity);
		}
		element = new Element(entity.getEntityVersionId(), entity);			
		entityDefinitionCache.put(element);
	}
	
	private CustomField buildCustomFieldFromAttribute(EntityAttribute attrib) {
		CustomField field = new CustomField();
		field.setEntityName(attrib.getEntity().getName());
		field.setFieldName(attrib.getName());
		field.setSourceFieldName(attrib.getSourceName());
		field.setTransformationFunctionName(attrib.getTransformationFunction());
		if (attrib.getFunctionParameters() != null) {
			field.setConfigurationParameters(deserializeParameters(attrib.getFunctionParameters()));
		}
		return field;
	}

	private EntityAttribute buildAttributeFromCustomField(Entity entity, CustomField field) {
		EntityAttribute attrib = new EntityAttribute();
		EntityAttributeDatatype type = getDatatypeByCode(EntityAttributeDatatype.STRING_DATATYPE_CD);
		attrib.setDatatype(type);
		attrib.setDateCreated(new Date());
		attrib.setDescription(field.getFieldName());
		attrib.setDisplayName(field.getFieldName());
		attrib.setDisplayOrder(1000);
		attrib.setEntity(entity);
		attrib.setFunctionParameters(serializeParameters(field.getConfigurationParameters()));
		attrib.setIndexed(false);
		attrib.setIsCustom(true);
		attrib.setName(field.getFieldName());
		attrib.setSourceName(field.getSourceFieldName());
		attrib.setTransformationFunction(field.getTransformationFunctionName());
		attrib.setUserCreatedBy(Context.getUserContext().getUser());
		return attrib;
	}

	private String serializeParameters(Map<String, String> params) {		
		if( params == null) {
			return null;
		}
		
		int count = params.keySet().size();
		if (count == 0) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		int index = 0;
		for (String key : params.keySet()) {
			String value = params.get(key);
			sb.append(key).append(":").append(value);
			if (index < count-1) {
				sb.append(",");
			}
			index++;
		}
		if (log.isDebugEnabled()) {
			log.debug("Serialized custom field params as: " + sb.toString());
		}
		return sb.toString();
	}

	private Map<String,String> deserializeParameters(String params) {
		Map<String,String> map = new HashMap<String,String>();
		String[] items = params.split(",");
		for (String item : items) {
			String[] keyValue = item.split(":");
			if (log.isDebugEnabled()) {
				log.debug("Deserialized custom field param <" + keyValue[0] + "," + keyValue[1] + ">");
			}
			map.put(keyValue[0], keyValue[1]);
		}
		return map;
	}
	private EntityAttributeDatatype getDatatypeByCode(int code) {
		List<EntityAttributeDatatype> types = getEntityAttributeDatatypes();
		for (EntityAttributeDatatype type : types) {
			if (type.getDatatypeCd() == code) {
				return type;
			}
		}
		return null;
	}

	public EntityDefinitionDao getEntityDefinitionDao() {
		return entityDao;
	}

	public void setEntityDefinitionDao(EntityDefinitionDao entityDao) {
		this.entityDao = entityDao;
	}
}
