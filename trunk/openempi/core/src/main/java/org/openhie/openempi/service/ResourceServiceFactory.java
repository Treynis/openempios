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
        if (Context.isInCLusterMode()) {
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
