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

import java.util.List;

import org.openhie.openempi.configuration.CustomField;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.service.BaseServiceTestCase;

public class CustomFieldTest extends BaseServiceTestCase
{
	
	public void testCustomFieldAdd() {
		EntityDefinitionManagerService entityDef = Context.getEntityDefinitionManagerService();
		List<Entity> entities = entityDef.loadEntities();
		if (entities.size() == 0) {
			log.debug("You need to define some entities in the database for this to work.");
			assertTrue("No entities have been defined in the system.", false);
		}
		Entity entity = entities.get(0);
		
		CustomField field = buildCustomField(entity);
		try {
			entityDef.addCustomField(field);
			assertTrue("We should be able to add the field just fine.", true);
			
			List<CustomField> fields = entityDef.loadCustomFields(entity.getName());
			assertTrue("We must be able to find this custom field we just added.", fields.size() > 0);
			
			entityDef.deleteCustomField(field);
			assertTrue("We should be able to get here successufully.", true);
		} catch (Exception e) {
			log.debug("Failed while testing custom field configuration:" +e, e);
			assertTrue("This should all be working,", false);
		}
	}

	private CustomField buildCustomField(Entity entity) {
		CustomField field = new CustomField();
		field.setFieldName("monthAndYear");
		field.setSourceFieldName("dateOfBirth");
		field.setEntityName(entity.getName());
		field.setTransformationFunctionName("DateTransformationFunction");
		field.addConfigurationParameter("dateFormat", "MM.yyyy");
		return field;
	}
}
