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

import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.openempi.webapp.client.EntityDefinitionDataService;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.EntityAttributeWeb;
import org.openempi.webapp.client.model.EntityAttributeValidationWeb;
import org.openempi.webapp.client.model.UserFileWeb;
import org.openempi.webapp.server.util.ModelTransformer;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeGroup;
import org.openhie.openempi.model.EntityAttributeValidation;
import org.openhie.openempi.service.UserManager;

public class EntityDefinitionDataServiceImpl extends AbstractRemoteServiceServlet implements EntityDefinitionDataService
{
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	public List<EntityWeb> loadEntities() throws Exception {
		log.debug("Received request to return list of EntityWeb");

		authenticateCaller();

		try {
			EntityDefinitionManagerService entityManagerService = Context.getEntityDefinitionManagerService();
			List<org.openhie.openempi.model.Entity> entities = entityManagerService.loadEntities();

			List<EntityWeb> dtos = new java.util.ArrayList<EntityWeb>(entities.size());
			for (Entity entity : entities) {
				EntityWeb dto = ModelTransformer.mapToEntity(entity, EntityWeb.class);

				// set groups to EntityWeb and group info to EntityAttributeWeb
				List<EntityAttributeGroup> groups = entityManagerService.loadEntityAttributeGroups(entity);
				dto =  ModelTransformer.setGroupInfoToEntity(groups, dto);

				// set validation info to entity attribute
				for (EntityAttribute attribute : entity.getAttributes()) {
					// Exclude custom fields.
					 if (attribute.getIsCustom()) {
						continue;
					 }
				     List<EntityAttributeValidation> validations = entityManagerService.loadEntityAttributeValidations(attribute);

				     Set<EntityAttributeValidationWeb> validationsWeb =  ModelTransformer.mapToEntityValidations(validations);	

				     EntityAttributeWeb attributeWeb = dto.findEntityAttributeByName(attribute.getName());
				     attributeWeb.setEntityAttributeValidations(validationsWeb);
				}
				dtos.add(dto);
			}
			return dtos;
		} catch (Throwable t) {
			log.error("Failed to execute: " + t.getMessage(), t);
			throw new RuntimeException(t);
		}
	}

	public EntityWeb addEntity(EntityWeb entityWeb) throws Exception {
		log.debug("Received request to add an new entity entry to the repository");

		authenticateCaller();

		try {
			EntityDefinitionManagerService entityManagerService = Context.getEntityDefinitionManagerService();
			org.openhie.openempi.model.Entity entity = ModelTransformer.mapToEntity(entityWeb, org.openhie.openempi.model.Entity.class);

			// 1. add entity and attributes
			Entity newEntity = entityManagerService.addEntity(entity);

			// 2. add entity groups with attribute
			List<EntityAttributeGroup> groups = ModelTransformer.mapToEntityGroup(entityWeb, newEntity, org.openhie.openempi.model.EntityAttributeGroup.class);				
			for (EntityAttributeGroup group : groups) {

				entityManagerService.addEntityAttributeGroup(group);
			}

			// 3. add entity attribute validations
			for (EntityAttributeWeb attributeWeb : entityWeb.getAttributes()) {
				 List<EntityAttributeValidation> validations = ModelTransformer.mapToEntityValidations(attributeWeb, newEntity, org.openhie.openempi.model.EntityAttributeValidation.class);						
				 for (EntityAttributeValidation validation : validations) {
					 entityManagerService.addEntityAttributeValidation(validation);
				 }
			}

			// return:
			// 1. map entity
			entityWeb = ModelTransformer.mapToEntity(newEntity, EntityWeb.class);

			// 2. set group info to entity
			entityWeb =  ModelTransformer.setGroupInfoToEntity(groups, entityWeb);

			// 3. set validation info to entity	attribute
			for (EntityAttribute attribute : newEntity.getAttributes()) {
			     List<EntityAttributeValidation> validations = entityManagerService.loadEntityAttributeValidations(attribute);

			     Set<EntityAttributeValidationWeb> validationsWeb =  ModelTransformer.mapToEntityValidations(validations);

			     entityWeb.findEntityAttributeByName(attribute.getName()).setEntityAttributeValidations(validationsWeb);
			}

			return entityWeb;

		} catch (Throwable t) {
			log.error("Failed while adding an entity entry: " + t, t);
			throw new Exception(t.getMessage());
		}
	}

	public EntityWeb updateEntity(EntityWeb entityWeb) throws Exception {
		log.debug("Received request to update an entity entry to the repository");

		authenticateCaller();

		try {
			EntityDefinitionManagerService entityManagerService = Context.getEntityDefinitionManagerService();
			org.openhie.openempi.model.Entity entity = ModelTransformer.mapToEntity(entityWeb, org.openhie.openempi.model.Entity.class);

			// Put Custom Attributes back and keeping No Change
			insertCustomFields(entityManagerService, entity);

			// 1. update entity
			Entity updatedEntity = entityManagerService.updateEntity(entity);

			// 2. update entity groups with attribute
			List<EntityAttributeGroup> groups = ModelTransformer.mapToEntityGroup(entityWeb, updatedEntity, org.openhie.openempi.model.EntityAttributeGroup.class);
			entityManagerService.updateEntityAttributeGroups(groups, updatedEntity);

			// 3. update entity attribute validations
			for (EntityAttributeWeb attributeWeb : entityWeb.getAttributes()) {
				 List<EntityAttributeValidation> validations = ModelTransformer.mapToEntityValidations(attributeWeb, updatedEntity, org.openhie.openempi.model.EntityAttributeValidation.class);
				 entityManagerService.updateEntityAttributeValidations(validations, updatedEntity, attributeWeb.getName());
			}

			// return:
			// updatedEntity = entityManagerService.loadEntity(entityWeb.getEntityVersionId());
			groups = entityManagerService.loadEntityAttributeGroups(updatedEntity);

			// 1. map entity
			entityWeb = ModelTransformer.mapToEntity( updatedEntity, EntityWeb.class);

			// 2. set group info to entity
			entityWeb =  ModelTransformer.setGroupInfoToEntity(groups, entityWeb);

			// 3. set validation info to entity	attribute
			for (EntityAttribute attribute : updatedEntity.getAttributes()) {

				 // Attribute is Custom field
				 if (attribute.getIsCustom()) {
					continue;
				 }
			     List<EntityAttributeValidation> validations = entityManagerService.loadEntityAttributeValidations(attribute);

			     Set<EntityAttributeValidationWeb> validationsWeb =  ModelTransformer.mapToEntityValidations(validations);

			     entityWeb.findEntityAttributeByName(attribute.getName()).setEntityAttributeValidations(validationsWeb);

			}

			return entityWeb;

		} catch (Throwable t) {
			log.error("Failed while updating an entity entry: " + t, t);
			throw new Exception(t.getMessage());
		}
	}

	public String deleteEntity(EntityWeb entityWeb) throws Exception {
		log.debug("Received request to delete the entity entry in the repository");

		authenticateCaller();

		String msg = "";
		try {
			EntityDefinitionManagerService entityManagerService = Context.getEntityDefinitionManagerService();
			org.openhie.openempi.model.Entity entity = ModelTransformer.mapToEntity(entityWeb, org.openhie.openempi.model.Entity.class);

			// delete groups
			List<EntityAttributeGroup> groups = ModelTransformer.mapToEntityGroup(entityWeb, entity, org.openhie.openempi.model.EntityAttributeGroup.class);
			for (EntityAttributeGroup group : groups) {
				entityManagerService.deleteEntityAttributeGroup(group);
			}

			// delete validations
			for (EntityAttribute attribute : entity.getAttributes()) {
			     List<EntityAttributeValidation> validations = entityManagerService.loadEntityAttributeValidations(attribute);
				 for (EntityAttributeValidation validation : validations) {
					  entityManagerService.deleteEntityAttributeValidation(validation);
				 }
			}

			// Put Custom Attributes back
			insertCustomFields(entityManagerService, entity);

			entityManagerService.deleteEntity(entity);

			return msg;

		} catch (Throwable t) {
			log.error("Failed while delete an entity entry: " + t, t);
			msg = t.getMessage();
			throw new Exception(t.getMessage());
		}
	}

    public String importEntity(UserFileWeb userFile) throws Exception {
        log.debug("Received request to import entity entry from a file");

        authenticateCaller();

        String msg = "";
        try {
            EntityDefinitionManagerService entityManagerService = Context.getEntityDefinitionManagerService();
            entityManagerService.importEntity(userFile.getFilename());

            UserManager userService = Context.getUserManager();
            org.openhie.openempi.model.UserFile userFileFound = userService.getUserFile(userFile.getUserFileId());
            userFileFound.setImported("Y");
            userService.saveUserFile(userFileFound);
            msg = "File successfully imported";

            return msg;
        } catch (Throwable t) {
            log.error("Failed while import an entity entry: " + t, t);
            msg = t.getMessage();
            throw new Exception(t.getMessage());
        }
    }

    public String exportEntity(EntityWeb entityWeb, String fileName) throws Exception {
        log.debug("Received request to export entity entry to a file");

        authenticateCaller();

        String msg = "";
        try {
            EntityDefinitionManagerService entityManagerService = Context.getEntityDefinitionManagerService();
            org.openhie.openempi.model.Entity entity = ModelTransformer.mapToEntity(entityWeb, org.openhie.openempi.model.Entity.class);

            entityManagerService.exportEntity(entity, fileName);

            return msg;
        } catch (Throwable t) {
            log.error("Failed while export an entity entry: " + t, t);
            msg = t.getMessage();
            throw new Exception(t.getMessage());
        }
    }

	private void insertCustomFields(EntityDefinitionManagerService entityDefService, Entity entity) {
		Entity entityFound = findLatestEntityVersionByName(entityDefService, entity.getName());
		if (entityFound == null) {
			log.warn("Was not able to find the entity " + entity + " so, custom fields were not inserted.");
			return;
		}
		for (EntityAttribute attribute : entityFound.getAttributes()) {
			if (attribute.getIsCustom()) {
				entity.addAttribute(attribute);
			}
		}
	}

	private Entity findLatestEntityVersionByName(EntityDefinitionManagerService entityDefService, String name) {
		List<Entity> entities = entityDefService.findEntitiesByName(name);
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
}
