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
package org.openhie.openempi.entity.dao;

import java.util.List;

import org.openhie.openempi.DaoException;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.EntityAttributeGroup;
import org.openhie.openempi.model.EntityAttributeValidation;

public interface EntityDefinitionDao
{
	public Entity addEntity(Entity entity) throws DaoException;
	
	public Entity updateEntity(Entity entity) throws DaoException;

	public Entity loadEntity(Integer id);
	
	public List<Entity> findEntitiesByName(String name);
	
	public List<Entity> loadEntities();
	
	public List<Entity> findEntityVersions(Integer entityId);

	public List<EntityAttributeDatatype> getEntityAttributeDatatypes();
	
	public EntityAttributeGroup addEntityAttributeGroup(EntityAttributeGroup entityAttributeGroup) throws DaoException;
	
	public EntityAttributeGroup updateEntityAttributeGroup(EntityAttributeGroup entityAttributeGroup) throws DaoException;
	
	public EntityAttributeGroup loadEntityAttributeGroup(Integer id);
	
	public List<EntityAttributeGroup> loadEntityAttributeGroups(Entity entity);
	
	public void deleteEntityAttributeGroup(EntityAttributeGroup entityAttributeGroup) throws DaoException;
	
	public EntityAttributeValidation addEntityAttributeValidation(EntityAttributeValidation validation) throws DaoException;
	
	public EntityAttributeValidation updateEntityAttributeValidation(EntityAttributeValidation validation) throws DaoException;
	
	public void deleteEntityAttributeValidation(EntityAttributeValidation validation) throws DaoException;
	
	public EntityAttributeValidation loadEntityAttributeValidation(Integer id);
	
	public List<EntityAttributeValidation> loadEntityAttributeValidations(EntityAttribute entityAttribute);

	public List<EntityAttribute> loadCustomFields(Entity entity);

	public EntityAttribute addCustomField(Entity entity, EntityAttribute field) throws DaoException;
	
	public EntityAttribute findCustomField(Entity entity, String fieldName);
}
