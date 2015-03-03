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

import java.util.List;

import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.service.BaseServiceTestCase;

public class EntityDefinitionExportTest extends BaseServiceTestCase
{

	public void testExportEntityDefinition() {
        EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
        try {
            List<Entity> entities = entityManager.findEntitiesByName("person");
            assertTrue("Not found entities.", entities.size() > 0);
            
            Entity entity = entities.get(0);
            
            String openEmpiHome = Context.getOpenEmpiHome();
            String filename = openEmpiHome + "/conf/" + "entity_definition.xml";
            
            String entityDefinition = entityManager.exportEntity(entity, filename);
      
            assertFalse("Export failure.", entityDefinition.isEmpty() );
            
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
