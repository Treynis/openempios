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
package org.openhie.openempi.webservices.restful;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;

import org.openempi.webservices.restful.Constants;

import com.sun.jersey.api.client.GenericType;

public class EntityResourceTest extends BaseRestfulServiceTestCase
{

    public void testGetAllEntities() {

        // get all entities
        List<Entity> entities = getWebResource()
                .path(Constants.VERSION)
                .path("entities")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Entity>>() { });
        assertTrue("Failed to retrieve entity list.", entities != null && entities.size() > 0);
    }

    public void testGetEntityById() {

        // get all entities
        List<Entity> entities = getWebResource()
                .path(Constants.VERSION)
                .path("entities")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Entity>>() { });
        assertTrue("Failed to retrieve entity list.", entities != null && entities.size() > 0);

        Entity entity = entities.get(0);

        // get an entity by id
        entity = getWebResource()
                .path(Constants.VERSION)
                .path("entities")
                .path(entity.getEntityVersionId().toString())
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .get(Entity.class);
        assertTrue("Failed to retrieve entity.", entity != null);
    }

    public void testGetEntityAttributes() {

        // get all entities
        List<Entity> entities = getWebResource()
                .path(Constants.VERSION)
                .path("entities")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Entity>>() { });
        assertTrue("Failed to retrieve entity list.", entities != null && entities.size() > 0);

        Entity entity = entities.get(0);

        // get an entity by id
        List<EntityAttribute> entityAttributes = getWebResource()
                .path(Constants.VERSION)
                .path("entity-attributes")
                .path(entity.getEntityVersionId().toString())
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<EntityAttribute>>() { });
        assertTrue("Failed to retrieve entity attributes.", entityAttributes != null);
        assertTrue("Not found entity attributes.", entityAttributes.size() > 0);
    }
}
