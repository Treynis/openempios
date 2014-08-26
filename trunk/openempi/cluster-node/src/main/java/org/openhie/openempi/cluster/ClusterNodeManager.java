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
package org.openhie.openempi.cluster;

import org.apache.log4j.Logger;
import org.openhie.openempi.context.Context;


public class ClusterNodeManager
{
    private Logger log = Logger.getLogger(getClass());
    
    public ClusterNodeManager() {
    }
    
    public void run() {
        log.info("Starting the node manager.");
        System.out.println("Node manager has now started.");
        try {
            Context.startup();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    shutdown();
                }
             });
        } catch (Exception e) {
            log.error("Failed to start the node manager: " + e, e);
            shutdown();
        }
    }
    
    public void shutdown() {
        try {
            log.info("Shutting down the node manager.");
            Context.shutdown();
            log.info("The node manager has been shutdown.");
        } catch (Exception e) {
            log.error("Failed while shutting down the node manager: " + e, e);
            System.exit(-1);
        }
    }
    
    public static void main(String[] args) {
        ClusterNodeManager nodeManager = new ClusterNodeManager();
        nodeManager.run();
    }
}
