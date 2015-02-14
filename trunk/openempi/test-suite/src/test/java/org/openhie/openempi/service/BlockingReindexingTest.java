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
package org.openhie.openempi.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openhie.openempi.blocking.BlockingLifecycleObserver;
import org.openhie.openempi.blocking.BlockingService;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.ForEachRecordConsumer;
import org.openhie.openempi.entity.RecordConsumer;
import org.openhie.openempi.entity.SampleRecordConsumer;
import org.openhie.openempi.model.Entity;

public class BlockingReindexingTest extends BaseServiceTestCase
{
    public void testBlockingReindexing() {
        EntityDefinitionManagerService entityManager = Context.getEntityDefinitionManagerService();
        try {
            List<Entity> entities = entityManager.loadEntities();
            if (entities.size() == 0) {
                return;
            }
            Entity entity = entities.get(0);
            BlockingService blockingService = Context.getBlockingService(entity.getName());
            
            BlockingLifecycleObserver blockingLifecycle = (BlockingLifecycleObserver) blockingService;
            blockingLifecycle.rebuildIndex();
            
            BufferedReader bufRead = new BufferedReader(new InputStreamReader(System.in)) ;
            try {
                System.out.println("Press a character to shutdown.");
                bufRead.readLine();
            }
            catch (IOException err) {
                 System.out.println("Error reading line");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
