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

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.model.Entity;


public class GlobalIdentifierTest extends BaseServiceTestCase
{
    public void testAssignGlobalIdentifiers() {
        Entity entity = getTestEntity();
        
        RecordManagerService recordService = Context.getRecordManagerService();
        try {
            boolean status = recordService.assignGlobalIdentifier(entity);
            log.debug("Assigning global identifiers to the repository returned: " + status);
        } catch (ApplicationException e) {
            log.error("Unable to assign global identifiers to all records in the repository: " + e, e);
        }
    }
}
