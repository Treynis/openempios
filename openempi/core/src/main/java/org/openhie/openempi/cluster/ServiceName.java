package org.openhie.openempi.cluster;

public enum ServiceName
{
    RECORD_LINK_RESOURCE_SERVICE("recordLinkResourceService",
            "recordLinkResourceServiceImpl",
            "recordLinkResourceServiceClusterImpl"),
    RECORD_RESOURCE_SERVICE("recordResourceService",
            "recordResourceServiceImpl",
            "recordResourceServiceClusterImpl"),
    SECURITY_RESOURCE_SERVICE("securityResourceService",
            "securityResourceServiceImpl",
            "securityResourceServiceClusterImpl");

    private final String serviceName;
    private final String implementation;
    private final String clusterImplementation;
    
    ServiceName(String serviceName, String implementation, String clusterImplementation) {
        this.serviceName= serviceName;
        this.implementation = implementation;
        this.clusterImplementation = clusterImplementation;
    }
    
    public String serviceName() {
        return serviceName;
    }
    
    public String implementation() {
        return implementation;
    }

    public String clusterImplementation() {
        return clusterImplementation;
    }
}
