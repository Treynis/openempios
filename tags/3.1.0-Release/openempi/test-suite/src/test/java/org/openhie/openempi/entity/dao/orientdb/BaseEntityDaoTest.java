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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.context.UserContext;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.User;

import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;

public class BaseEntityDaoTest
{
	protected static final int TEST_RECORD_COUNT = 10;
	protected static final String PLOCAL_STORAGE_MODE = "plocal";
    protected static final String DATA_DIRECTORY = ".";
    
	protected static Logger log = Logger.getLogger(BaseEntityDaoTest.class);
	protected static List<Record> testRecords = new ArrayList<Record>();
	protected static List<Record> testRecordLinks = new ArrayList<Record>();
    protected static Entity entity;
	
	protected static void addAttribute(Entity entity, Date dateCreated,
			String name, EntityAttributeDatatype datatype) {
		EntityAttribute attribute = new EntityAttribute();
		attribute.setCaseInsensitive(true);
		attribute.setDatatype(datatype);
		attribute.setDateCreated(dateCreated);
		attribute.setDisplayName(name);
		attribute.setEntity(entity);
		attribute.setIndexed(true);
		attribute.setName(name);
		entity.addAttribute(attribute);
	}

	protected static ConnectionManager getConnectionManager(String storageMode,
			int poolSize) {
		ConnectionManager connectionManager = new ConnectionManager();
		connectionManager.setUsername("openempi");
		connectionManager.setPassword("openempi");
		connectionManager.setMaxPoolConnections(poolSize);
		connectionManager.setStorageMode(storageMode);
		return connectionManager;
	}
	
	protected static void close(EntityStore store, ConnectionManager connectionManager, OrientBaseGraph db) {
		if (db != null) {
			connectionManager.close(store, db);
		}
	}
	
	protected static ConnectionManager getConnectionManager(int poolSize) {
		ConnectionManager connectionManager = new ConnectionManager();
		connectionManager.setUsername("admin");
		connectionManager.setPassword("admin");
		connectionManager.setStorageMode("plocal");
		connectionManager.setMaxPoolConnections(poolSize);
		return connectionManager;
	}

	public static EntityStore getStoreByName(String entityName) {
        String storeName = entityName;
        String storageName = buildStoreName(entityName);
        String dataDirectory = DATA_DIRECTORY;
        String storeUrl = PLOCAL_STORAGE_MODE + ":" + dataDirectory + "/" + storageName;
        EntityStore store = new EntityStore(entityName, storeName, storeUrl, storageName);
        return store;
    }

	private static String buildStoreName(String entityName) {
        String storeName = entityName + "-db";
        log.debug("The store for entity " + entityName + " is at: " + storeName);
        return storeName;
    }
	
	protected static void createTestRecords(Entity entity, IdentifierDomain domain) {
        domain.setIdentifierDomainName("OpenEMPI");
		for (int i=0; i < TEST_RECORD_COUNT; i++) {
			Record record = new Record(entity);
			for (EntityAttribute attrib : entity.getAttributes()) {
				if (attrib.getDatatype().getDatatypeCd() == EntityAttributeDatatype.STRING_DATATYPE_CD) {
					record.set(attrib.getName(), "John." + i);
				} else if (attrib.getDatatype().getDatatypeCd() == EntityAttributeDatatype.INTEGER_DATATYPE_CD) {
					record.set(attrib.getName(), new Integer(i*5+13));
				}
			}
			for (int j=0; j < 2; j++) {
    			Identifier identifier = new Identifier();
    			identifier.setIdentifier("MRN-100" + i + j);
    			identifier.setIdentifierDomain(domain);
    			identifier.setRecord(record);
    			record.addIdentifier(identifier);
			}
			testRecords.add(record);
		}
	}
    
    protected static void createTestRecordLinks(Entity entity, IdentifierDomain domain) {
        domain.setIdentifierDomainName("OpenEMPI");
        for (int i=0; i < TEST_RECORD_COUNT; i++) {
            Record record = new Record(entity);
            for (EntityAttribute attrib : entity.getAttributes()) {
                if (attrib.getDatatype().getDatatypeCd() == EntityAttributeDatatype.STRING_DATATYPE_CD) {
                    record.set(attrib.getName(), "Test." + i);
                } else if (attrib.getDatatype().getDatatypeCd() == EntityAttributeDatatype.INTEGER_DATATYPE_CD) {
                    record.set(attrib.getName(), new Integer(i*10+1));
                }
            }
            Identifier identifier = new Identifier();
            identifier.setIdentifier("MRN-200" + i);
            identifier.setIdentifierDomain(domain);
            identifier.setRecord(record);
            record.addIdentifier(identifier);
            testRecordLinks.add(record);
        }
    }
	
	protected static void cleanDatabaseDirectory() {
		File directory = new File("test-db");
		// make sure directory exists
		if (!directory.exists()) {
			log.debug("The directory does not exist.");
		} else {
			try {
				delete(directory);
			} catch (IOException e) {
				log.error("Failed to delete the directory: " + e, e);
			}
		}
	}

	public static void delete(File file) throws IOException {
		if (file.isDirectory()) {
			// directory is empty, then delete it
			if (file.list().length == 0) {
				file.delete();
				log.debug("Directory is deleted : " + file.getAbsolutePath());
			} else {
				// list all the directory contents
				String files[] = file.list();
				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);
					// recursive delete
					delete(fileDelete);
				}
				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
					log.debug("Directory is deleted : " + file.getAbsolutePath());
				}
			}
		} else {
			// if file, then delete it
			file.delete();
			log.debug("File is deleted : " + file.getAbsolutePath());
		}
	}

	protected static void setupUserContext() {
		User testUser = new User();
		testUser.setId(-1L);
		UserContext userContext = new UserContext();
		userContext.setUser(testUser);
		Context.setUserContext(userContext);
	}

    protected static void createTestEntity() {
        entity = new Entity();
    	entity.setName("test");
    	entity.setEntityId(1);
    	entity.setEntityVersionId(5);
    	Date now = new Date();
    	addAttribute(entity, now, "firstName", EntityAttributeDatatype.STRING_DATATYPE);
    	addAttribute(entity, now, "lastName", EntityAttributeDatatype.STRING_DATATYPE);
    	addAttribute(entity, now, "age", EntityAttributeDatatype.INTEGER_DATATYPE);
    }
}
