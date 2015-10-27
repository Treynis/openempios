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

public enum ServiceName
{
    PERSON_MANAGER_RESOURCE_SERVICE("personManagerResourceService",
            "personManagerResourceServiceImpl",
            "personManagerResourceServiceClusterImpl"),
    PERSON_QUERY_RESOURCE_SERVICE("personQueryResourceService",
            "personQueryResourceServiceImpl",
            "personQueryResourceServiceClusterImpl"),
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
