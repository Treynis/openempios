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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.orientechnologies.orient.core.Orient;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConnectionManagerTest extends BaseEntityDaoTest
{
	static final int THREAD_COUNT = 10;

	@Test
	public void test00ConnectInitial() {
		EntityStore store = getStoreByName("test");
		ConnectionManager connectionManager = new ConnectionManager();
		OrientBaseGraph connection = null;
		long start = EntityDaoTestUtil.startTimer();
		try {
			connection = connectionManager.connectInitial(store);
			assertNotNull("Failed to get a connection from the pool",
					connection);
			log.debug("Obtained an initial connection in "
					+ EntityDaoTestUtil.endTimer(start) + " msec.");
		} catch (Exception e) {
			assertTrue("Opening the connection threw an exception: " + e, false);
		}

		try {
			start = EntityDaoTestUtil.startTimer();
			connection.shutdown();
			log.debug("Shutdown the initial connection in "
					+ EntityDaoTestUtil.endTimer(start) + " msec.");
		} catch (Exception e) {
			assertTrue("Closing the connection threw an exception: " + e, false);
		}
	}

	@Test
	public void test01Connect() {
		EntityStore store = getStoreByName("test");
		ConnectionManager connectionManager = getConnectionManager(THREAD_COUNT);
		OrientBaseGraph connection = null;

		long start = EntityDaoTestUtil.startTimer();
		try {
			connection = connectionManager.connect(store);
			assertNotNull("Failed to get a connection from the pool",
					connection);
			log.debug("Obtained a connection in "
					+ EntityDaoTestUtil.endTimer(start) + " msec.");
		} catch (Exception e) {
			assertTrue("Opening the connection threw an exception: " + e, false);
		}

		try {
			start = EntityDaoTestUtil.startTimer();
			connection.shutdown();
			log.debug("Shutdown the connection in "
					+ EntityDaoTestUtil.endTimer(start) + " msec.");
		} catch (Exception e) {
			assertTrue("Closing the connection threw an exception: " + e, false);
		}
	}

	@Test
	public void test02ConnectPoolResponse() {
		EntityStore store = getStoreByName("test");
		ConnectionManager connectionManager = getConnectionManager(THREAD_COUNT);

		Thread[] threads = new Thread[THREAD_COUNT];
		for (int i = 0; i < THREAD_COUNT; i++) {
			threads[i] = new Thread(
					new ConnectionTest(store, connectionManager),
					"Test Thread " + i);
			threads[i].start();
		}
		try {
			for (int i = 0; i < THREAD_COUNT; i++) {
				threads[i].join(30000);
			}
			connectionManager.shutdown(store);
		} catch (Exception e) {
			assertTrue("Waiting for the connection failed: " + e, false);
		}
	}

    @AfterClass
    public static void setUpAfterClass() throws Exception {
        log.info("Cleaning up the test environment after the tests have run.");
        Orient.instance().shutdown();
        cleanDatabaseDirectory();
    }
    
	public class ConnectionTest implements Runnable {
		private EntityStore store;
		private ConnectionManager connectionManager;

		public ConnectionTest(EntityStore store,
				ConnectionManager connectionManager) {
			this.store = store;
			this.connectionManager = connectionManager;
		}

		@Override
		public void run() {
			long start = EntityDaoTestUtil.startTimer();
			OrientBaseGraph connection = null;

			try {
				connection = connectionManager.connect(store);
				assertNotNull("Failed to get a connection from the pool",
						connection);
				log.debug("Obtained a connection in "
						+ EntityDaoTestUtil.endTimer(start) + " msec.");
			} catch (Exception e) {
				assertTrue("Opening the connection threw an exception: " + e,
						false);
			}

			try {
				Thread.sleep(5000);
			} catch (Exception e) {
			}

			try {
				start = EntityDaoTestUtil.startTimer();
				connection.shutdown();
				log.debug("Shutdown the connection in "
						+ EntityDaoTestUtil.endTimer(start) + " msec.");
			} catch (Exception e) {
				assertTrue("Closing the connection threw an exception: " + e,
						false);
			}
			try {
				connection = connectionManager.connect(store);
				assertNotNull("Failed to get a connection from the pool",
						connection);
				log.debug("Obtained a connection in "
						+ EntityDaoTestUtil.endTimer(start) + " msec.");
			} catch (Exception e) {
				assertTrue("Opening the connection threw an exception: " + e,
						false);
			}

			try {
				Thread.sleep(5000);
			} catch (Exception e) {
			}

			try {
				start = EntityDaoTestUtil.startTimer();
				connection.shutdown();
				log.debug("Shutdown the connection in "
						+ EntityDaoTestUtil.endTimer(start) + " msec.");
			} catch (Exception e) {
				assertTrue("Closing the connection threw an exception: " + e,
						false);
			}
		}
	}
}