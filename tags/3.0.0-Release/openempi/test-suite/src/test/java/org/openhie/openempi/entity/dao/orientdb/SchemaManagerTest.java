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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.orientechnologies.orient.core.Orient;

public class SchemaManagerTest extends BaseEntityDaoTest
{
	private static ConnectionManager connectionManager;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		log.info("Setting up the test before the tests are run.");		
		connectionManager = getConnectionManager("plocal", 10);
		createTestEntity();
	}

	@Test
	public void testInitializeSchema() {
		EntityStore store = getStoreByName(entity.getName());
		SchemaManager schemaManager = SchemaManagerFactory.createSchemaManager(connectionManager);
		schemaManager.initializeSchema(entity, store);
		schemaManager.dropSchema(entity, store);
	}

    @AfterClass
    public static void setUpAfterClass() throws Exception {
        log.info("Cleaning up the test environment after the tests have run.");
        Orient.instance().shutdown();
        cleanDatabaseDirectory();
    }
}
