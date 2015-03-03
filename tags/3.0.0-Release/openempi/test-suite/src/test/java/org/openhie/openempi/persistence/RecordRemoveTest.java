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

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.service.BaseServiceTestCase;

public class RecordRemoveTest extends BaseServiceTestCase
{

    public void testRemove() {
        try {
            RecordManagerService manager = Context.getRecordManagerService();
            RecordQueryService queryService = Context.getRecordQueryService();
            EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
            List<Entity> entities = defService.loadEntities();
            if (entities.size() == 0) {
                log.debug("You need to define some entities in the database for this to work.");
                return;
            }
            Entity entity = entities.get(0);
            log.debug("Testing with entity " + entity);
            
            Record record = new Record(entity);
            record.set("familyName", "DOE");
            List<Record> found = queryService.findRecordsByAttributes(entity, record);
            log.debug("Found " + found.size() + " records.");
            for (Record rec : found) {
                rec = manager.removeRecord(entity, rec);
                log.debug("Removed the record: " + record);
            }
        } catch (ApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
