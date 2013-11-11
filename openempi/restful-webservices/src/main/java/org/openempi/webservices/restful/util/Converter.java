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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.openempi.webservices.restful.model.FieldValue;
import org.openhie.openempi.model.AttributeDatatype;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;

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
                value = DateToString((Date) obj);
                break;
            case TIMESTAMP:
                value = DateTimeToString((Date) obj);
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
            EntityAttribute attribute = entity.findAttributeByName(fieldName);

            switch(AttributeDatatype.getById(attribute.getDatatype().getDatatypeCd())) {
            case DATE:
                record.set(fieldName, StringToDate(attrib.getValue()));
                break;
            case TIMESTAMP:
                record.set(fieldName, StringToDateTime(attrib.getValue()));
                break;
            case LONG:
                record.set(fieldName, Long.valueOf(attrib.getValue()));
                break;
            case INTEGER:
                record.set(fieldName, Integer.valueOf(attrib.getValue()));
                break;
            case DOUBLE:
                record.set(fieldName, Double.valueOf(attrib.getValue()));
                break;
            case FLOAT:
                record.set(fieldName, Float.valueOf(attrib.getValue()));
                break;
            case BOOLEAN:
                record.set(fieldName, Boolean.valueOf(attrib.getValue()));
                break;
            default:
                // String
                record.set(fieldName, attrib.getValue());
                break;
            }
        }
        return record;
    }

    public static Record convertKeyValListToRecord(Entity entity, List<String> keyValList) {
        Record record = new Record(entity);
        for (String entry : keyValList) {
            // entry:  "givenName, Albert"
            String[] keyValue = entry.split(",");

            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                EntityAttribute attribute = entity.findAttributeByName(key);

                switch(AttributeDatatype.getById(attribute.getDatatype().getDatatypeCd())) {
                case DATE:
                    record.set(key, StringToDate(value));
                    break;
                case TIMESTAMP:
                    record.set(key, StringToDateTime(value));
                    break;
                case LONG:
                    record.set(key, Long.parseLong(value));
                    break;
                case INTEGER:
                    record.set(key, Integer.parseInt(value));
                    break;
                case DOUBLE:
                    record.set(key, Double.parseDouble(value));
                    break;
                case FLOAT:
                    record.set(key, Float.parseFloat(value));
                    break;
                case BOOLEAN:
                    record.set(key, Boolean.parseBoolean(value));
                    break;
                default:
                    // String
                    record.set(key, value);
                    break;
                }
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
        restRecLink.setRecordLinkId(Long.parseLong(recordLinkId));

        restRecLink.setLeftRecord(convertRecordToRestfulRecord(recordLink.getLeftRecord()));
        restRecLink.setRightRecord(convertRecordToRestfulRecord(recordLink.getRightRecord()));
        restRecLink.setState(recordLink.getState().getState());
        restRecLink.setWeight(recordLink.getWeight());

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

        return recordLink;
    }

    public static String DateToString(Date date)
    {
        if( date == null)
            return "";
        
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = df.format(date);

        // System.out.println("Report Date: " + strDate);
        return strDate;
    }
    
    public static Date StringToDate(String strDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        if (strDate != null && !strDate.isEmpty()) {
            try {
                date = df.parse(strDate);
                // System.out.println("Today = " + df.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    public static String DateTimeToString(Date date)
    {
        if( date == null)
            return "";
        
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        String strDate = df.format(date);

        // System.out.println("Report Date: " + strDate);
        return strDate;
    }
    
    public static Date StringToDateTime(String strDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        Date date = null;
        if (strDate != null && !strDate.isEmpty()) {
            try {
                date = df.parse(strDate);
                // System.out.println("Today = " + df.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }
}
