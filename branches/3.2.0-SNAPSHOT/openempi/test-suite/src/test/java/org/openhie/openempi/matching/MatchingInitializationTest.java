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
package org.openhie.openempi.matching;

import java.util.List;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.Constants;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.service.BaseServiceTestCase;

public class MatchingInitializationTest extends BaseServiceTestCase
{
	static {
//		System.setProperty(Constants.OPENEMPI_EXTENSION_CONTEXTS_FILENAME, "openempi-extension-contexts-probabilistic-matching.properties");
		System.setProperty(Constants.OPENEMPI_EXTENSION_CONTEXTS_FILENAME, "openempi-extension-contexts.properties");
		System.setProperty(Constants.OPENEMPI_CONFIGURATION_FILENAME, "mpi-config.xml");
	}

	public void testInitialization() {
		List<Entity> entities = Context.getEntityDefinitionManagerService().loadEntities();
		assertTrue("No entities have been defined.", entities.size() > 0);
		Entity testEntity = entities.get(0);
		
		RecordManagerService service = Context.getRecordManagerService();
		try {
			log.info("Testing initialization of the repository using entity: " + testEntity.getDisplayName());
			service.initializeRepository(testEntity);
		} catch (ApplicationException e) {
			log.error("Failed while initializing the repository: " + e, e);
		}
	}
}
