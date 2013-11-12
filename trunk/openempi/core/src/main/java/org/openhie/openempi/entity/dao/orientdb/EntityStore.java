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
package org.openhie.openempi.entity.dao.orientdb;

import java.util.HashMap;
import java.util.Map;

import com.orientechnologies.orient.core.metadata.schema.OClassImpl;

public class EntityStore
{
    private String entityName;
    private String storeName;
    private String storeUrl;
    private OClassImpl entityClass;
    private OClassImpl identifierClass;
    private Map<String, Integer> clusterIdByClassName = new HashMap<String, Integer>();

    public EntityStore(String entityName, String storeName, String storeUrl) {
        this.entityName = entityName;
        this.storeName = storeName;
        this.storeUrl = storeUrl;
    }

    public void put(String className, Integer clusterId) {
        clusterIdByClassName.put(className, clusterId);
    }

    public Integer getClusterId(String className) {
        return clusterIdByClassName.get(className);
    }

    public String getEntityName() {
        return entityName;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getStoreUrl() {
        return storeUrl;
    }

    public String toString() {
        return "EntityStore [entityName=" + entityName + ", storeName=" + storeName + ", storeUrl=" + storeUrl
                + ", clusterIdByClassName=" + clusterIdByClassName + "]";
    }

    public OClassImpl getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(OClassImpl entityClass) {
        this.entityClass = entityClass;
    }

    public OClassImpl getIdentifierClass() {
        return identifierClass;
    }

    public void setIdentifierClass(OClassImpl identifierClass) {
        this.identifierClass = identifierClass;
    }
}
