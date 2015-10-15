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

import org.apache.log4j.Logger;
import org.openhie.openempi.cluster.ServiceName;
import org.openhie.openempi.context.Context;

public class ResourceServiceFactory
{
    public static Logger log = Logger.getLogger(ResourceServiceFactory.class);

    public static Object createResourceService(ServiceName serviceName, Class<?> serviceInterface) {
        if (log.isDebugEnabled()) {
            log.debug("Creating an reference to the resource service implementation for resource service "
                    + serviceName.serviceName());
        }
        String beanName = null;
        if (Context.isInClusterMode()) {
            beanName = serviceName.clusterImplementation();
        } else {
            beanName = serviceName.implementation();
        }
        Object obj = Context.getApplicationContext().getBean(beanName);
        if (log.isDebugEnabled()) {
            log.debug("While looking for " + serviceName.serviceName() + " using bean name " + beanName + 
                    " found " + obj);
        }
        if (obj == null || !(serviceInterface.isAssignableFrom(obj.getClass()))) {
            String msg = "Implementation of " + serviceName.serviceName() + " hasn't been configured properly.";
            log.error(msg);
            throw new RuntimeException(msg);
        }
        return obj;
    }
}
