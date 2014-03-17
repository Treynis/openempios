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

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.openhie.openempi.configuration.CustomField;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.service.BaseServiceTestCase;


public class CustomFieldAddUpdateTest extends BaseServiceTestCase
{
    private Logger log = Logger.getLogger(getClass());
    
    public void testAddUpdateCustomFieldValue() {
        Entity entity = findEntityWithCustomFields();
        if (entity == null) {
            log.info("Need to define some custom fields for this test to work.");
            return;
        }
        
        List<CustomField> customFields = 
                Context.getEntityDefinitionManagerService().loadCustomFields(entity.getName());
        CustomField field = customFields.get(0);        
        Record record = buildRecord(entity);
        try {
            record = Context.getRecordManagerService().addRecord(entity, record);
            log.warn("Field value is " + record.get(field.getSourceFieldName()) + " and custom field is " +
                    record.get(field.getFieldName()));
            
            record.set(field.getSourceFieldName(), "Updated" + record.get(field.getSourceFieldName()));
            Context.getRecordManagerService().updateRecord(entity, record);
            log.warn("After update field value is " + record.get(field.getSourceFieldName()) + " and custom field is " +
                    record.get(field.getFieldName()));
            
            Context.getRecordManagerService().removeRecord(entity, record);
        } catch (Exception e) {
            log.error("Failed during the test: " + e, e);
        }
    }

    private Record buildRecord(Entity entity) {
        Record record = new Record(entity);
        Date theDate = new Date();
        Long value = theDate.getTime();
        for (EntityAttribute attrib : entity.getAttributes()) {
            if (attrib.getDatatype().getDatatypeCd() == EntityAttributeDatatype.STRING_DATATYPE_CD) {
                record.set(attrib.getName(), "test" + value);
            } else if (attrib.getDatatype().getDatatypeCd() == EntityAttributeDatatype.LONG_DATATYPE_CD) {
                record.set(attrib.getName(), value);
            } else if (attrib.getDatatype().getDatatypeCd() == EntityAttributeDatatype.INTEGER_DATATYPE_CD) {
                record.set(attrib.getName(), value.intValue());
            } else if (attrib.getDatatype().getDatatypeCd() == EntityAttributeDatatype.FLOAT_DATATYPE_CD) {
                record.set(attrib.getName(), value.floatValue());
            } else if (attrib.getDatatype().getDatatypeCd() == EntityAttributeDatatype.DOUBLE_DATATYPE_CD) {
                record.set(attrib.getName(), value.doubleValue());
            } else if (attrib.getDatatype().getDatatypeCd() == EntityAttributeDatatype.SHORT_DATATYPE_CD) {
                record.set(attrib.getName(), value.shortValue());
            } else if (attrib.getDatatype().getDatatypeCd() == EntityAttributeDatatype.DATE_DATATYPE_CD) {
                record.set(attrib.getName(), value.shortValue());
            }
        }
        return record;
    }

    private Entity findEntityWithCustomFields() {
        for (Entity entity : Context.getEntityDefinitionManagerService().loadEntities()) {
            List<CustomField> customFields = Context.getEntityDefinitionManagerService().loadCustomFields(entity.getName());
            if (customFields.size() > 0) {
                return entity;
            }
        }
        return null;
    }
}
