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

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.openhie.openempi.entity.Constants;
import org.openhie.openempi.entity.dao.orientdb.ConnectionManager;
import org.openhie.openempi.entity.dao.orientdb.EntityStore;
import org.openhie.openempi.entity.dao.orientdb.SchemaManager;
import org.openhie.openempi.entity.dao.orientdb.SchemaManagerFactory;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;

public class SchemaManagerTest extends TestCase
{
    public void testInitialization() {
        Entity entity = getTestEntity();
        
        SchemaManager local = SchemaManagerFactory.createSchemaManager(getConnectionManager());
        local.setParameter(Constants.DATA_DIRECTORY_KEY, "/tmp");
        EntityStore store = local.getEntityStoreByName(entity.getName());
        local.initializeSchema(entity, store);
        
        local.shutdownStore(entity);
    }
    
    public Entity getTestEntity() {
        Entity entity = new Entity();
        entity.setName("personTest");
        entity.setDescription("Entity that represents a person and his or her demographic attributes");
        entity.setDisplayName("Person Test");
        entity.setVersionId(1);
        
        Set<EntityAttribute> attributes = new HashSet<EntityAttribute>();
        entity.setAttributes(attributes);
        
        EntityAttribute attrib = new EntityAttribute();
        attrib.setName("firstName");
        attrib.setDisplayName("First Name");
        attrib.setDescription("Given name");
        attrib.setIndexed(true);
        attrib.setDisplayOrder(1);
        attrib.setDatatype(new EntityAttributeDatatype(EntityAttributeDatatype.STRING_DATATYPE_CD));
        attrib.setIsCustom(false);
        attributes.add(attrib);
        
        attrib = new EntityAttribute();
        attrib.setName("lastName");
        attrib.setDisplayName("Last Name");
        attrib.setDescription("Family name");
        attrib.setIndexed(true);
        attrib.setDisplayOrder(2);
        attrib.setDatatype(new EntityAttributeDatatype(EntityAttributeDatatype.STRING_DATATYPE_CD));
        attrib.setIsCustom(false);
        attributes.add(attrib);
        
        attrib = new EntityAttribute();
        attrib.setName("age");
        attrib.setDisplayName("Age");
        attrib.setDescription("Age");
        attrib.setIndexed(false);
        attrib.setDisplayOrder(3);
        attrib.setDatatype(new EntityAttributeDatatype(EntityAttributeDatatype.STRING_DATATYPE_CD));
        attrib.setIsCustom(false);
        attributes.add(attrib);     
        
        attrib = new EntityAttribute();
        attrib.setName("dateOfBirth");
        attrib.setDisplayName("Date of Birth");
        attrib.setDescription("Date of Birth");
        attrib.setIndexed(false);
        attrib.setDisplayOrder(4);
        attrib.setDatatype(new EntityAttributeDatatype(EntityAttributeDatatype.DATE_DATATYPE_CD));
        attrib.setIsCustom(false);
        attributes.add(attrib);     
        return entity;
    }
    
    private ConnectionManager getConnectionManager() {
        ConnectionManager connectionManager = new ConnectionManager();
        connectionManager.setUsername("openempi");
        connectionManager.setPassword("openempi");
        connectionManager.setServerUsername("admin");
        connectionManager.setServerPassword("admin");
        connectionManager.setMaxPoolConnections(20);
        connectionManager.setMinPoolConnections(1);
        connectionManager.setStorageMode(Constants.PLOCAL_STORAGE_MODE);
        return connectionManager;
    }
}
