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
package org.openhie.openempi.profiling;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.model.AttributeDatatype;
import org.openhie.openempi.model.DataProfileAttribute;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.Record;

public class EntityRecordDataSource extends AbstractRecordDataSource
{
    public static final int REPOSITORY_RECORD_DATA_SOURCE_ID = -1;

    private Entity entity;
    private EntityDao entityDao;
    private Set<Entity> initializedEntities = new HashSet<Entity>();
    private Entity entityDef = null;

    private EntityDefinitionManagerService entityDefinitionService;

    private Integer recordBlockSize;

    public EntityRecordDataSource() {
    }

    public void init(Entity entity) {
        this.entity = entity;
    }

    public Iterator<Record> iterator() {
        return new RecordIterator(recordBlockSize);
    }

    public int getRecordDataSourceId() {
        return REPOSITORY_RECORD_DATA_SOURCE_ID;
    }

    private int getDataProfileAttributeType(EntityAttribute attrib) {
        switch (AttributeDatatype.getById(attrib.getDatatype().getDatatypeCd())) {
        case INTEGER:
            return DataProfileAttribute.INTEGER_DATA_TYPE;
        case LONG:
            return DataProfileAttribute.LONG_DATA_TYPE;
        case DOUBLE:
            return DataProfileAttribute.DOUBLE_DATA_TYPE;
        case FLOAT:
            return DataProfileAttribute.FLOAT_DATA_TYPE;
        case DATE:
            return DataProfileAttribute.DATE_DATA_TYPE;
        case STRING:
            return DataProfileAttribute.STRING_DATA_TYPE;
        case TIMESTAMP:
            return DataProfileAttribute.TIMESTAMP_DATA_TYPE;
        case SHORT:
            return DataProfileAttribute.SHORT_DATA_TYPE;
        case BOOLEAN:
            return DataProfileAttribute.BOOLEAN_DATA_TYPE;
        }
        return 0;
    }

    public List<AttributeMetadata> getAttributeMetadata() {
        java.util.List<AttributeMetadata> metadata = new java.util.ArrayList<AttributeMetadata>();
        entityDef = getEntity();
        if (entityDef != null) {
            for (EntityAttribute attrib : entityDef.getAttributes()) {
                if (attrib.getDateVoided() == null) {
                    metadata.add(new AttributeMetadata(attrib.getName(), getDataProfileAttributeType(attrib)));
                }
            }
        }
        return metadata;
    }

    private Entity getEntity() {
        return entityDefinitionService.getEntityByName(entity.getName());
    }

    public EntityDefinitionManagerService getEntityDefinitionService() {
        return entityDefinitionService;
    }

    public void setEntityDefinitionService(EntityDefinitionManagerService entityDefinitionService) {
        this.entityDefinitionService = entityDefinitionService;
    }

    public Integer getRecordBlockSize() {
        return recordBlockSize;
    }

    public void setRecordBlockSize(Integer recordBlockSize) {
        this.recordBlockSize = recordBlockSize;
    }

    public boolean isEmpty() {
        boolean isEmpty = false;

        if ((getEntityDao(entity).getRecordCount(getEntity())).longValue() == 0) {
            isEmpty = true;
        }

        return isEmpty;
    }

    private class RecordIterator implements Iterator<Record>
    {
        private int blockSize;
        java.util.List<Record> records;
        int currentIndex;
        int startIndex;

        public RecordIterator(int blockSize) {
            this.blockSize = blockSize;
            startIndex = 0;
            currentIndex = -1;
        }

        public boolean hasNext() {
            if (records != null && currentIndex < records.size()) {
                return true;
            }
            return loadBlockOfRecords(blockSize);
        }

        private boolean loadBlockOfRecords(int blockSize) {
            try {
                log.debug("Loading records from " + startIndex + " to " + (startIndex + blockSize));
                records = getEntityDao(entity).findRecordsByAttributes(entityDef, new Record(new Object()), startIndex, blockSize);
                if (records.size() == 0) {
                    return false;
                }
                currentIndex = 0;
                startIndex += blockSize;
                return true;
            } catch (Exception e) {
                log.error("Failed while loading a block of records from the repository: " + e, e);
                return false;
            }
        }

        public Record next() {
            return records.get(currentIndex++);
        }

        public void remove() {
        }
    }

    private synchronized EntityDao getEntityDao(Entity entity) {
        boolean found = initializedEntities.contains(entity);
        if (!found) {
            entityDao.initializeStore(entity);
            initializedEntities.add(entity);
        }
        return entityDao;
    }

    public void close(String message) {
        // TODO Auto-generated method stub
    }

    public EntityDao getEntityDao() {
        return entityDao;
    }

    public void setEntityDao(EntityDao entityDao) {
        this.entityDao = entityDao;
    }
}
