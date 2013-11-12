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
package org.openhie.openempi.entity.dao.hibernate;

import java.util.List;

import org.openhie.openempi.DaoException;
import org.openhie.openempi.configuration.CustomField;
import org.openhie.openempi.dao.hibernate.UniversalDaoHibernate;
import org.openhie.openempi.entity.dao.EntityDefinitionDao;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.EntityAttributeGroup;
import org.openhie.openempi.model.EntityAttributeValidation;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.orm.hibernate3.HibernateAccessor;

public class EntityDefinitionDaoHibernate extends UniversalDaoHibernate implements EntityDefinitionDao
{
	private DataFieldMaxValueIncrementer incrementer;

	public Entity addEntity(Entity entity) throws DaoException {
		try {
			if(entity.getEntityId() == null) {
			   entity.setEntityId(incrementer.nextIntValue());
			}
			getHibernateTemplate().setFlushMode(HibernateAccessor.FLUSH_ALWAYS);
			getHibernateTemplate().saveOrUpdate(entity);
			getHibernateTemplate().flush();
			log.debug("Finished saving the entity.");
			return entity;
		} catch (Exception e) {
			log.error("Failed while attempting to save an entity. Error: " + e, e);
			throw new DaoException("Unable to save the entity definition in the repository.");
		}
	}

	public Entity updateEntity(Entity entity) throws DaoException {
		try {
			log.debug("Updating entity definition: " + entity);
			getHibernateTemplate().setFlushMode(HibernateAccessor.FLUSH_ALWAYS);
			getHibernateTemplate().merge(entity);
			getHibernateTemplate().flush();
			log.debug("Finished saving the entity.");
			return entity;
		} catch (Exception e) {
			log.error("Failed while attempting to update an entity. Error: " + e, e);
			throw new DaoException("Unable to update the entity definition in the repository.");
		}
	}

	@SuppressWarnings("unchecked")
	public List<EntityAttributeDatatype> getEntityAttributeDatatypes() {
		List<EntityAttributeDatatype> datatypes = (List<EntityAttributeDatatype>) getHibernateTemplate()
				.find("from EntityAttributeDatatype");
		log.trace("Obtained the list of entity attribute datatypes with " + datatypes.size() + " entries.");
		return datatypes;
	}

	@SuppressWarnings("unchecked")
	public List<Entity> findEntityVersions(Integer entityId) {
		List<Entity> entities = (List<Entity>) getHibernateTemplate()
				.find("from Entity e where e.entityId = " + entityId + " and dateVoided is null");
		log.trace("Obtained the list of entity versions with " + entities.size() + " entries.");
		for (Entity entity : entities) {
			getHibernateTemplate().evict(entity);
			removeDeletedAttributes(entity);
		}
		return entities;
	}

	@SuppressWarnings("unchecked")
	public List<Entity> findEntitiesByName(String name) {
		List<Entity> entities = (List<Entity>) getHibernateTemplate()
				.find("from Entity e where e.name = " + " '" +name + "'"+ " and dateVoided is null");
		log.trace("Obtained the list of entities with " + entities.size() + " entries.");
		for (Entity entity : entities) {
			getHibernateTemplate().evict(entity);
			removeDeletedAttributes(entity);
		}
		return entities;
	}
	
	@SuppressWarnings("unchecked")
	public List<Entity> loadEntities() {
		List<Entity> entities = (List<Entity>) getHibernateTemplate()
				.find("from Entity e where e.dateVoided is null");
		log.trace("Obtained a list of entities with " + entities.size() + " entries.");
		for (Entity entity : entities) {
			getHibernateTemplate().evict(entity);
			removeDeletedAttributes(entity);
		}
		return entities;
	}

	@SuppressWarnings("unchecked")
	public Entity loadEntity(Integer id) {
		List<Entity> entities = (List<Entity>) getHibernateTemplate()
				.find("from Entity e where e.dateVoided is null and e.entityVersionId = " + id);
		if (entities.size() == 0) {
			log.debug("Could not locate an entity using entity id " + id);
			return null;
		}
		Entity entity = entities.get(0);
		getHibernateTemplate().evict(entity);
		removeDeletedAttributes(entity);
		log.trace("Loaded the entity: " + entity);
		return entity;
	}
	
	public EntityAttribute addCustomField(Entity entity, EntityAttribute field) throws DaoException {
		try {
			if(entity.getEntityId() == null) {
			   entity.setEntityId(incrementer.nextIntValue());
			}
			entity.addAttribute(field);
			getHibernateTemplate().setFlushMode(HibernateAccessor.FLUSH_ALWAYS);
			getHibernateTemplate().saveOrUpdate(entity);
			getHibernateTemplate().flush();
			log.debug("Finished saving the entity.");
			return field;
		} catch (Exception e) {
			log.error("Failed while attempting to save a custom field. Error: " + e, e);
			throw new DaoException("Unable to save the custom field in the repository.");
		}
	}

	
	public EntityAttributeGroup addEntityAttributeGroup(EntityAttributeGroup entityAttributeGroup) throws DaoException {
		try {
			getHibernateTemplate().setFlushMode(HibernateAccessor.FLUSH_ALWAYS);
			getHibernateTemplate().saveOrUpdate(entityAttributeGroup);
			getHibernateTemplate().flush();
			log.debug("Finished saving the entity attribute group.");
			return entityAttributeGroup;
		} catch (Exception e) {
			log.error("Failed while attempting to save an entity attribute group. Error: " + e, e);
			throw new DaoException("Unable to save the entity attribute group in the repository.");
		}		
	}
	
	public EntityAttributeGroup updateEntityAttributeGroup(EntityAttributeGroup entityAttributeGroup) throws DaoException {
		try {
			log.debug("Updating entity attribute group: " + entityAttributeGroup);
			getHibernateTemplate().setFlushMode(HibernateAccessor.FLUSH_ALWAYS);
			getHibernateTemplate().merge(entityAttributeGroup);
			getHibernateTemplate().flush();
			log.debug("Finished saving the entity attribute group.");
			return entityAttributeGroup;
		} catch (Exception e) {
			log.error("Failed while attempting to update an entity attribute group. Error: " + e, e);
			throw new DaoException("Unable to update the entity attribute group in the repository.");
		}
	}
	
	public EntityAttributeGroup loadEntityAttributeGroup(Integer id) {
		@SuppressWarnings("unchecked")
		List<EntityAttributeGroup> entityGroups = (List<EntityAttributeGroup>) getHibernateTemplate()
				.find("from EntityAttributeGroup e where e.entityAttributeGroupId = " + id);
		if (entityGroups.size() == 0) {
			log.debug("Could not locate an entity attribute group using id " + id);
			return null;
		}
		EntityAttributeGroup entityAttributeGroup = entityGroups.get(0);
		getHibernateTemplate().evict(entityAttributeGroup);
		log.debug("Loaded the entity attribute group: " + entityAttributeGroup);
		return entityAttributeGroup;
	}
	
	public void deleteEntityAttributeGroup(EntityAttributeGroup entityAttributeGroup) throws DaoException {
		EntityAttributeGroup group = loadEntityAttributeGroup(entityAttributeGroup.getEntityAttributeGroupId());
		if (group == null) {
			return;
		}
		try {
			getHibernateTemplate().delete(group);
			log.debug("Deleted the entity attribute group: " + entityAttributeGroup);
			getHibernateTemplate().flush();
		} catch (Exception e) {
			log.error("Failed while attempting to delete an entity attribute group. Error: " + e, e);
			throw new DaoException("Unable to delete the entity attribute group in the repository.");
		}
	}
	
	public List<EntityAttributeGroup> loadEntityAttributeGroups(Entity entity) {
		@SuppressWarnings("unchecked")
		List<EntityAttributeGroup> entityAttributeGroups = (List<EntityAttributeGroup>) getHibernateTemplate()
				.find("from EntityAttributeGroup e where e.entity.entityVersionId = ?", entity.getEntityVersionId());
		log.trace("Obtained a list of entity attribute groups with " + entityAttributeGroups.size() + " entries.");
		for (EntityAttributeGroup entityAttributeGroup : entityAttributeGroups) {
			getHibernateTemplate().evict(entityAttributeGroup);
		}
		return entityAttributeGroups;
	}
	
	private void removeDeletedAttributes(Entity entity) {
		List<EntityAttribute> toBeRemoved = new java.util.ArrayList<EntityAttribute>();
		for (EntityAttribute attrib : entity.getAttributes()) {
			if (attrib.getDateVoided() != null) {
				toBeRemoved.add(attrib);
				log.trace("Removing attribute " + attrib.getName() + " because it has been deleted.");
			}
//			if (attrib.getIsCustom()) {
//				toBeRemoved.add(attrib);
//				log.trace("Removing attribute " + attrib.getName() + " because it is a custom field.");
//			}
		}
		entity.getAttributes().removeAll(toBeRemoved);
	}
	
	public EntityAttributeValidation addEntityAttributeValidation(EntityAttributeValidation validation) throws DaoException {
		try {
			getHibernateTemplate().setFlushMode(HibernateAccessor.FLUSH_ALWAYS);
			getHibernateTemplate().saveOrUpdate(validation);
			getHibernateTemplate().flush();
			log.debug("Finished saving the entity attribute validation.");
			return validation;
		} catch (Exception e) {
			log.error("Failed while attempting to save an entity attribute validation. Error: " + e, e);
			throw new DaoException("Unable to save the entity attribute validation in the repository.");
		}
	}
	
	public EntityAttributeValidation updateEntityAttributeValidation(EntityAttributeValidation validation) throws DaoException {
		try {
			log.debug("Updating entity attribute validation: " + validation);
			getHibernateTemplate().setFlushMode(HibernateAccessor.FLUSH_ALWAYS);
			getHibernateTemplate().merge(validation);
			getHibernateTemplate().flush();
			log.debug("Finished saving the entity attribute validation.");
			return validation;
		} catch (Exception e) {
			log.error("Failed while attempting to update an entity attribute validation. Error: " + e, e);
			throw new DaoException("Unable to update the entity attribute validation in the repository.");
		}
	}
	
	public void deleteEntityAttributeValidation(EntityAttributeValidation validation) throws DaoException {
		EntityAttributeValidation valFromDb = loadEntityAttributeValidation(validation.getEntityAttributeValidationId());
		if (valFromDb == null) {
			return;
		}
		try {
			getHibernateTemplate().delete(valFromDb);
			log.debug("Deleted the entity attribute validation: " + validation);
			getHibernateTemplate().flush();
		} catch (Exception e) {
			log.error("Failed while attempting to delete an entity attribute validation. Error: " + e, e);
			throw new DaoException("Unable to delete the entity attribute validation in the repository.");
		}
	}
	
	public EntityAttributeValidation loadEntityAttributeValidation(Integer id) {
		@SuppressWarnings("unchecked")
		List<EntityAttributeValidation> validations = (List<EntityAttributeValidation>) getHibernateTemplate()
				.find("from EntityAttributeValidation e where e.entityAttributeValidationId = ?", id);
		if (validations.size() == 0) {
			log.debug("Could not locate an entity attribute validations using id " + id);
			return null;
		}
		EntityAttributeValidation validation = validations.get(0);
		getHibernateTemplate().evict(validation);
		log.debug("Loaded the entity attribute validation: " + validation);
		return validation;
	}
	
	public List<EntityAttributeValidation> loadEntityAttributeValidations(EntityAttribute entityAttribute) {
		@SuppressWarnings("unchecked")
		List<EntityAttributeValidation> validations = (List<EntityAttributeValidation>) getHibernateTemplate()
		.find("from EntityAttributeValidation e where e.entityAttribute.entityAttributeId = ?", entityAttribute.getEntityAttributeId());
		log.debug("Obtained a list of entity attribute validations with " + validations.size() + " entries.");
		for (EntityAttributeValidation validation : validations) {
			getHibernateTemplate().evict(validation);
		}
		return validations;		
	}

	public List<EntityAttribute> loadCustomFields(Entity entity) {
		@SuppressWarnings("unchecked")
		List<EntityAttribute> attribs = (List<EntityAttribute>) getHibernateTemplate()
			.find("from EntityAttribute ea where ea.dateVoided is null and ea.entity.name = ? and ea.isCustom = ?",
					new Object[] { entity.getName(), Boolean.TRUE });
		log.debug("Obtained a list of custom fields with " + attribs.size() + " entries.");
		return attribs;
	}

	public EntityAttribute findCustomField(Entity entity, String fieldName) {
		@SuppressWarnings("unchecked")
		List<EntityAttribute> attribs = (List<EntityAttribute>) getHibernateTemplate()
			.find("from EntityAttribute ea where ea.dateVoided is null and ea.entity.name = ? and ea.isCustom = ? and ea.name = ?",
					new Object[] { entity.getName(), Boolean.TRUE, fieldName });
		log.debug("Obtained a list of custom fields with " + attribs.size() + " entries.");
		if (attribs.size() == 0) {
			return null;
		}
		return attribs.get(0);
	}
	
	public DataFieldMaxValueIncrementer getIncrementer() {
		return incrementer;
	}

	public void setIncrementer(DataFieldMaxValueIncrementer incrementer) {
		this.incrementer = incrementer;
	}
}
