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
package org.openhie.openempi.entity;

import java.util.List;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.configuration.CustomField;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.EntityAttributeGroup;
import org.openhie.openempi.model.EntityAttributeValidation;

public interface EntityDefinitionManagerService
{
	public List<Entity> loadEntities();
	
	public Entity loadEntity(Integer id);
	
    public Entity loadEntityByName(String name);

    public Entity getEntityByName(String name);
    
    public void createEntityIndexes(Integer entityVersionId) throws ApplicationException;

    public void dropEntityIndexes(Integer entityVersionId) throws ApplicationException;

    public List<Entity> findEntitiesByName(String name);
	
	public List<EntityAttributeDatatype> getEntityAttributeDatatypes();
	
	public Entity addEntity(Entity entity) throws ApplicationException;
	
	public Entity updateEntity(Entity entity) throws ApplicationException;
	
	public void deleteEntity(Entity entity) throws ApplicationException;

	public String exportEntity(Entity entity, String filename) throws ApplicationException;
	
	public String exportEntity(Integer entityVersionId) throws ApplicationException;
	
	public void importEntity(String filename) throws ApplicationException;

	public EntityAttributeGroup addEntityAttributeGroup(EntityAttributeGroup entityAttributeGroup) throws ApplicationException;
	
	public EntityAttributeGroup updateEntityAttributeGroup(EntityAttributeGroup entityAttributeGroup) throws ApplicationException;
	
	public void updateEntityAttributeGroups(List<EntityAttributeGroup> groups, Entity updatedEntity) throws ApplicationException;
	
	public void deleteEntityAttributeGroup(EntityAttributeGroup entityAttributeGroup) throws ApplicationException;
	
	public EntityAttributeGroup loadEntityAttributeGroup(Integer id);
	
	public List<EntityAttributeGroup> loadEntityAttributeGroups(Entity entity);
	
	public EntityAttributeValidation addEntityAttributeValidation(EntityAttributeValidation validation) throws ApplicationException;
	
	public EntityAttributeValidation updateEntityAttributeValidation(EntityAttributeValidation validation) throws ApplicationException;
	
	public void updateEntityAttributeValidations(List<EntityAttributeValidation> validations, Entity updatedEntity, String attributeName) throws ApplicationException;
	
	public void deleteEntityAttributeValidation(EntityAttributeValidation validation) throws ApplicationException;
	
	public EntityAttributeValidation loadEntityAttributeValidation(Integer id);
	
	public List<EntityAttributeValidation> loadEntityAttributeValidations(EntityAttribute entityAttribute);
	
	public List<CustomField> loadCustomFields(String entityName);
	
	public CustomField findCustomField(String entityName, String fieldName);
	
	public CustomField addCustomField(CustomField field) throws ApplicationException;

	public void updateCustomField(CustomField field) throws ApplicationException;
	
	public void deleteCustomField(CustomField field) throws ApplicationException;
}
