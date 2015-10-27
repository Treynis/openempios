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
package org.openempi.webservices.restful.util;

import java.util.Date;

import org.openempi.webservices.restful.model.FieldValue;
import org.openhie.openempi.model.AttributeDatatype;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.util.ConvertUtil;

public class Converter
{
    public static org.openempi.webservices.restful.model.Record convertRecordToRestfulRecord(Record record) {
        org.openempi.webservices.restful.model.Record restRec = new org.openempi.webservices.restful.model.Record();
        restRec.setEntityId(record.getEntity().getEntityVersionId());
        restRec.setRecordId(record.getRecordId());
        restRec.setIdentifiers(record.getIdentifiers());
        for (EntityAttribute attrib : record.getEntity().getAttributes()) {
            // filter out custom attributes
            if (attrib.getIsCustom()) {
                continue;
            }

            String fieldName = attrib.getName();
            if (record.get(fieldName) == null) {
                continue;
            }

            Object obj = record.get(fieldName);
            String value;
            switch(AttributeDatatype.getById(attrib.getDatatype().getDatatypeCd())) {
            case DATE:
                value = ConvertUtil.dateToString((Date) obj);
                break;
            case TIMESTAMP:
                value = ConvertUtil.dateToString((Date) obj);
                break;
            default:
                value = obj.toString();
                break;

            }
            restRec.addFieldValue(new FieldValue(fieldName, value));
        }
        return restRec;
    }

    public static Record convertRestfulRecordToRecord(Entity entity, org.openempi.webservices.restful.model.Record restRec) {
        Record record = new Record(entity);
        record.setRecordId(restRec.getRecordId());
        record.setIdentifiers(restRec.getIdentifiers());
        for (FieldValue attrib : restRec.getFields()) {

            if (attrib.getValue() == null) {
                continue;
            }
            String fieldName = attrib.getName();
            fieldName = fieldName.trim();
            fieldName = fieldName.replace("\n", "");
            EntityAttribute attribute = entity.findAttributeByName(fieldName);
            if (attribute == null) {
                continue;
            }

            switch(AttributeDatatype.getById(attribute.getDatatype().getDatatypeCd())) {
            case DATE:
                record.set(fieldName, ConvertUtil.stringToDate(attrib.getValue().trim()));
                break;
            case TIMESTAMP:
                record.set(fieldName, ConvertUtil.stringToDateTime(attrib.getValue().trim()));
                break;
            case LONG:
                record.set(fieldName, Long.valueOf(attrib.getValue().trim()));
                break;
            case INTEGER:
                record.set(fieldName, Integer.valueOf(attrib.getValue().trim()));
                break;
            case DOUBLE:
                record.set(fieldName, Double.valueOf(attrib.getValue().trim()));
                break;
            case FLOAT:
                record.set(fieldName, Float.valueOf(attrib.getValue().trim()));
                break;
            case BOOLEAN:
                record.set(fieldName, Boolean.valueOf(attrib.getValue().trim()));
                break;
            default:
                // String
                record.set(fieldName, attrib.getValue().trim());
                break;
            }
        }
        return record;
    }

    public static org.openempi.webservices.restful.model.RecordLink convertRecordToRestfulRecordLink(RecordLink recordLink) {
        org.openempi.webservices.restful.model.RecordLink restRecLink = new org.openempi.webservices.restful.model.RecordLink();

        Integer entityId = recordLink.getLeftRecord().getEntity().getEntityVersionId();

        String[] result = recordLink.getRecordLinkId().split(":");
        // String entityClusterId = result[0];
        // entityClusterId = entityClusterId.substring(1);
        String recordLinkId = result[1];

        restRecLink.setEntityId(entityId);
        restRecLink.setRecordLinkId(recordLinkId);

        restRecLink.setLeftRecord(convertRecordToRestfulRecord(recordLink.getLeftRecord()));
        restRecLink.setRightRecord(convertRecordToRestfulRecord(recordLink.getRightRecord()));
        restRecLink.setState(recordLink.getState().getState());
        restRecLink.setWeight(recordLink.getWeight());
        restRecLink.setVector(recordLink.getVector());
        if (recordLink.getLinkSource() != null) {
        	restRecLink.setSource(recordLink.getLinkSource().getSourceName());
        }

        return restRecLink;
    }

    public static RecordLink convertRestfulRecordLinkToRecordLink(Entity entity, org.openempi.webservices.restful.model.RecordLink restRecordLink) {
        RecordLink recordLink = new RecordLink();

        if (restRecordLink.getRecordLinkId() != null) {
            recordLink.setRecordLinkId(restRecordLink.getRecordLinkId().toString());
        }

        if (restRecordLink.getLeftRecord() != null) {
            recordLink.setLeftRecord(convertRestfulRecordToRecord(entity, restRecordLink.getLeftRecord()));
        }
        if (restRecordLink.getRightRecord() != null) {
            recordLink.setRightRecord(convertRestfulRecordToRecord(entity, restRecordLink.getRightRecord()));
        }

        recordLink.setState(RecordLinkState.fromString(restRecordLink.getState()));
        recordLink.setWeight(restRecordLink.getWeight());
        recordLink.setVector(restRecordLink.getVector());
        if (restRecordLink.getVector() == null) {
            recordLink.setVector(0);
        }

        return recordLink;
    }
}
