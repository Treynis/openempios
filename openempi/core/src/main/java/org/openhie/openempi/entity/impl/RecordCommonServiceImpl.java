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

import java.util.List;
import java.util.Map;

import org.openhie.openempi.configuration.ConfigurationRegistry;
import org.openhie.openempi.configuration.CustomField;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.service.impl.BaseServiceImpl;
import org.openhie.openempi.transformation.TransformationService;

public class RecordCommonServiceImpl extends BaseServiceImpl
{
	protected void populateCustomFields(Record record) {
		@SuppressWarnings("unchecked")
		Map<String,List<CustomField>> customFieldsListByEntityName = (Map<String, List<CustomField>>) Context
				.getConfiguration().lookupConfigurationEntry(ConfigurationRegistry.CUSTOM_FIELD_LIST_BY_ENTITY_NAME_MAP);
		List<CustomField> customFields = customFieldsListByEntityName.get(record.getEntity().getName());
		if (customFields == null) {
			if (log.isDebugEnabled()) {
				log.debug("No custom fields have been defined for entity " + record.getEntity().getName());
			}
			return;
		}
		
		TransformationService transformationService = Context.getTransformationService();
		for (CustomField customField : customFields) {
			log.trace("Need to generate a value for field " + customField.getSourceFieldName() + " using function " +
					customField.getTransformationFunctionName() + " and save it as field " + customField.getFieldName());
			try {
				Object value = record.get(customField.getSourceFieldName());
				log.debug("Obtained a value of " + value + " for field " + customField.getSourceFieldName());
				if (value != null) {
					Object transformedValue = transformationService.transform(customField.getTransformationFunctionName(), value, customField.getConfigurationParameters());
					record.set(customField.getFieldName(), transformedValue);
					log.debug("Custom field " + customField.getFieldName() + " has value " + record.get(customField.getFieldName()));
				}
			} catch (Exception e) {
				log.error("Failed while trying to obtain property for field " + customField.getSourceFieldName() + ":" + e.getMessage(), e);
			}
		}
	}
}
