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

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.context.UserContext;
import org.openhie.openempi.service.RecordResourceService;
import org.openhie.openempi.service.SecurityResourceService;

import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

public class ClusterManager implements MembershipListener
{
    private static final String TASK_EXECUTOR = "taskExecutor";
    private Logger log = Logger.getLogger(getClass());
    private IMap<String,Object> configurationRegistry;
    private IQueue<Long> recordIdQueue;
    private HazelcastInstance cluster;
    private IExecutorService clusterExecutor;
    private List<Member> memberList = new ArrayList<Member>();    
    private Map<ServiceName,Object> serviceCache;
    private Map<ServiceName,Map<String,Method>> serviceMethodCache;
    private int lastMember = 0;

    public void start() {
        cluster = Hazelcast.newHazelcastInstance(getConfig());
        cluster.getCluster().addMembershipListener(this);
        memberList.add(cluster.getCluster().getLocalMember());
        configurationRegistry = cluster.getMap(Constants.CONFIGURATION_REGISTRY_KEY);
        recordIdQueue = cluster.getQueue(Constants.RECORD_ID_QUEUE_KEY);
        clusterExecutor = cluster.getExecutorService(TASK_EXECUTOR);
        cacheServiceMethods();
    }

    private Config getConfig() {
        String openEmpiHome = Context.getOpenEmpiHome();
        if (openEmpiHome != null && openEmpiHome.length() > 0) {
            String configFile = openEmpiHome + "/conf/hazelcast.xml";
            try {
                Config config = new FileSystemXmlConfig(configFile);
                return config;
            } catch (FileNotFoundException e) {
                log.error("Unable to find the configuration file for the cluster named " + configFile);
                return null;
            }
        } else {
            log.error("Unable to find the configuration file for the cluster as the OPENEMPI_HOME is not set.");
            return null;
        }                
    }

    public Map<String, Object> getConfigurationRegistry() {
        return configurationRegistry;
    }

    public Object lookupConfigurationEntry(String entityName, String key) {
        @SuppressWarnings("unchecked")
        Map<String,Object> entityRegistry = (Map<String, Object>) configurationRegistry.get(entityName);
        if (entityRegistry == null) {
            log.warn("There is no configuration registry for entity " + entityName);
            return  null;
        }
        return entityRegistry.get(key);
    }

    public void registerConfigurationeEntry(String entityName, String key, Object entry) {
        @SuppressWarnings("unchecked")
        Map<String,Object> entityRegistry = (Map<String, Object>) configurationRegistry.get(entityName);
        if (entityRegistry == null) {
            entityRegistry = new HashMap<String,Object>();
        }
        log.info("Registering configuration entry " + entry + " with key " + key +
                " in the registry for entity: " + entityName);
        entityRegistry.put(key, entry);
        configurationRegistry.put(entityName, entityRegistry);
    }

    public void stop() {
        if (cluster != null) {
            cluster.shutdown();
        }
    }    

    public CommandRequest executeRemoteRequest(CommandRequest request) {
        RemoteRequestTask task = new RemoteRequestTask(request);
        try {
            if (memberList.size() == 0) {
                log.info("Assigning task to local node: " + cluster.getCluster().getLocalMember());
                request = clusterExecutor.submit(task).get();
            } else {
                lastMember = (lastMember + 1) % memberList.size();
                log.info("Last member index is set to " + lastMember);
                Member member = memberList.get(lastMember);
                log.info("Assigning task to node: " + member);
                request = clusterExecutor.submitToMember(task, member).get();
            }
        } catch (InterruptedException e) {
            log.error("Submitting the request " + request + " was interrupted: " + e, e);
            request.setException(e);
        } catch (ExecutionException e) {
            log.error("Submitting the request " + request + " failed during execution: " + e, e);
            request.setException(e);
        }
        log.info("Response from request is " + request.getResponse());
        return request;
    }

    public void processRequest(CommandRequest request) {
        Object service = getService(request);
        Method method = lookupServiceMethod(request.getServiceName(), request.getRequestName());
        UserContext context = request.getUserContext();
        log.info("Restored the user context: " + context);
        Context.setUserContext(context);
        try {
            if (request.isHasResponse()) {
                Object obj = method.invoke(service, request.getParams());
                request.setResponse((Serializable) obj);
            } else {
                method.invoke(service, request.getParams());
            }
        } catch (IllegalArgumentException e) {
            log.error("Failed while invoking method " + method.getName() + " of interface " +
                    request.getServiceName().serviceName() + ": " + e, e);
            request.setException(e);
        } catch (IllegalAccessException e) {
            log.error("Failed while invoking method " + method.getName() + " of interface " +
                    request.getServiceName().serviceName() + ": " + e, e);
            request.setException(e);
        } catch (InvocationTargetException e) {
            log.error("Failed while invoking method " + method.getName() + " of interface " +
                    request.getServiceName().serviceName() + ": " + e, e);
            request.setException(e);
        }
    }

    public BlockingQueue<Long> getRecordIdQueue() {
        return recordIdQueue;
    }

    private Object getService(CommandRequest request) {
        Object service = serviceCache.get(request.getServiceName());
        if (service == null) {
            log.error("Unable to find an instance of service " + request.getServiceName().serviceName());
            throw new RuntimeException("Unable to find an instance of service " + request.getServiceName().serviceName());
        }
        return service;
    }
    
    private void cacheServiceMethods() {
        serviceMethodCache = new HashMap<ServiceName,Map<String,Method>>();
        cacheServiceMethods(ServiceName.SECURITY_RESOURCE_SERVICE, SecurityResourceService.class, serviceMethodCache);
        cacheServiceMethods(ServiceName.RECORD_RESOURCE_SERVICE, RecordResourceService.class, serviceMethodCache);

        serviceCache = new HashMap<ServiceName,Object>();
        Object service = Context.getApplicationContext().getBean(ServiceName.SECURITY_RESOURCE_SERVICE.implementation());
        serviceCache.put(ServiceName.SECURITY_RESOURCE_SERVICE, service);
        service = Context.getApplicationContext().getBean(ServiceName.RECORD_RESOURCE_SERVICE.implementation());
        serviceCache.put(ServiceName.RECORD_RESOURCE_SERVICE, service);
    }

    private void cacheServiceMethods(ServiceName serviceName, Class<?> serviceClass,
            Map<ServiceName, Map<String, Method>> cache) {
        Map<String,Method> serviceMethodCache = new HashMap<String,Method>();
        for (Method method : serviceClass.getDeclaredMethods()) {
            log.warn("Caching method: " + method.getName() + " of interface " + serviceName);
            serviceMethodCache.put(method.getName(), method);
        }
        cache.put(serviceName, serviceMethodCache);
    }
    
    private Method lookupServiceMethod(ServiceName serviceName, String methodName) {
        Map<String,Method> methodMap = serviceMethodCache.get(serviceName);
        Method method = methodMap.get(methodName);
        if (method == null) {
            log.error("Unable to find method " + methodName + " in service " + serviceName);
            throw new RuntimeException("Unable to find method " + methodName + " in service " + serviceName);
        }
        return method;
    }

    public void memberAdded(MembershipEvent event) {
        log.info("A new member was added to the cluster: " + event.getMember());
        memberList.add(event.getMember());
    }

    public void memberRemoved(MembershipEvent event) {
        log.info("A member was removed from the cluster: " + event.getMember());
        //TODO: This needs to be handled properly
    }
}
