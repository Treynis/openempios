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
