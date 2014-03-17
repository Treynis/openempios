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

import org.apache.log4j.Logger;
import org.openhie.openempi.entity.Constants;

public class SchemaManagerFactory
{
    private static Logger log = Logger.getLogger(SchemaManagerFactory.class);
    
    public static SchemaManager createSchemaManager(ConnectionManager connectionManager) {
        if (Constants.REMOTE_STORAGE_MODE.equalsIgnoreCase(connectionManager.getStorageMode())) {
            return new SchemaManagerRemote(connectionManager);
        } else if (Constants.PLOCAL_STORAGE_MODE.equalsIgnoreCase(connectionManager.getStorageMode()) ||
                Constants.LOCAL_STORAGE_MODE.equalsIgnoreCase(connectionManager.getStorageMode())) {
            return new SchemaManagerLocal(connectionManager);
        }
        log.info("Received request to create a schema manager of unknown storage mode: " + 
                connectionManager.getStorageMode());
        throw new RuntimeException("Unable to create a schema manager of unknown storage mode");
    }
}
