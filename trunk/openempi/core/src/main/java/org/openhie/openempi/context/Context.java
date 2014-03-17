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
package org.openhie.openempi.context;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhie.openempi.AuthenticationException;
import org.openhie.openempi.Constants;
import org.openhie.openempi.blocking.BlockingLifecycleObserver;
import org.openhie.openempi.blocking.BlockingService;
import org.openhie.openempi.cluster.ClusterManager;
import org.openhie.openempi.configuration.Configuration;
import org.openhie.openempi.configuration.ScheduledTaskEntry;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.PersistenceLifecycleObserver;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.loader.FileLoaderConfigurationService;
import org.openhie.openempi.matching.MatchingLifecycleObserver;
import org.openhie.openempi.matching.MatchingService;
import org.openhie.openempi.model.DataAccessIntent;
import org.openhie.openempi.model.User;
import org.openhie.openempi.notification.EventObservable;
import org.openhie.openempi.notification.NotificationService;
import org.openhie.openempi.notification.ObservationEventType;
import org.openhie.openempi.profiling.DataProfileService;
import org.openhie.openempi.report.ReportService;
import org.openhie.openempi.service.AuditEventService;
import org.openhie.openempi.service.IdentifierDomainService;
import org.openhie.openempi.service.PersonManagerService;
import org.openhie.openempi.service.PersonQueryService;
import org.openhie.openempi.service.UserManager;
import org.openhie.openempi.service.ValidationService;
import org.openhie.openempi.service.JobQueueService;
import org.openhie.openempi.singlebestrecord.SingleBestRecordService;
import org.openhie.openempi.stringcomparison.StringComparisonService;
import org.openhie.openempi.transformation.TransformationService;
import org.openhie.openempi.validation.EntityValidationService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Context implements ApplicationContextAware
{
	protected static final Log log = LogFactory.getLog(Context.class);
	private static final int THREAD_POOL_SIZE = 5;
	private static final int SCHEDULER_THREAD_POOL_SIZE = 5;
	
	private static final ThreadLocal<Object[] /* UserContext */> userContextHolder = new ThreadLocal<Object[] /* UserContext */>();
	private static ApplicationContext applicationContext;
	private static ClusterManager clusterManager;
	private static UserManager userManager;
	private static EntityDefinitionManagerService entityDefinitionManagerService;
	private static JobQueueService jobQueueService;
	private static RecordManagerService recordManagerService;
	private static RecordQueryService recordQueryService;
	private static PersonManagerService personService;
	private static PersonQueryService personQueryService;
	private static IdentifierDomainService identifierDomainService;
	private static ValidationService validationService;
	private static Configuration configuration;
	private static List<MatchingService> matchingServiceList = new ArrayList<MatchingService>();
	private static Map<String,MatchingService> matchingServiceMap = new HashMap<String,MatchingService>();
    private static List<BlockingService> blockingServiceList = new ArrayList<BlockingService>();
    private static Map<String,BlockingService> blockingServiceMap = new HashMap<String,BlockingService>();
	private static AuditEventService auditEventService;
	private static StringComparisonService stringComparisonService;
	private static TransformationService transformationService;
	private static FileLoaderConfigurationService fileLoaderConfigurationService;
	private static NotificationService notificationService;
	private static DataProfileService dataProfileService;
	private static ReportService reportService;
	private static SingleBestRecordService singleBestRecordService;
	private static EntityValidationService entityValidationService;
	private static ExecutorService threadPool;
	private static ScheduledExecutorService scheduler;
	private static DataAccessIntent currentIntent;
	private static boolean isInitialized = false;
	private static Map<ObservationEventType,EventObservable> observableByType = new HashMap<ObservationEventType,EventObservable>(); 
	
	static {
		for (ObservationEventType type : ObservationEventType.values()) {
			EventObservable observable = new EventObservable(type);
			observableByType.put(type, observable);
		}
	}
	
	public static void startup() {
		if (isInitialized) {
			return;
		}
		try {
			applicationContext = new ClassPathXmlApplicationContext(getConfigLocationsAsArray());
			applicationContext.getBean("context");
			startCluster();
			configuration.init();
			threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
			scheduler = Executors.newScheduledThreadPool(SCHEDULER_THREAD_POOL_SIZE);
			
			startPersistenceService();
			for (BlockingService service : blockingServiceList) {
			    startBlockingService(service);
			}
			for (MatchingService service : matchingServiceList) {
			    startMatchingService(service);
			}
			startNotificationService();
			startScheduledTasks();
			isInitialized = true;
		} catch (Throwable t) {
			log.error("Failed while setting up the context for OpenEMPI: " + t, t);
		}
	}
	
	private static void startCluster() {
	    clusterManager = new ClusterManager();
	    clusterManager.start();
    }

    public static void shutdown() {
        for (MatchingService service : matchingServiceList) {
            stopMatchingService(service);
        }
        for (BlockingService service : blockingServiceList) {
            stopBlockingService(service);
        }
        stopRecordCacheService(null);
		stopPersistenceService(null);
		stopScheduledTasks();
		stopThreadPool();
		clusterManager.stop();
		isInitialized = false;		
	}
	
	public static void shutdownAll() {
        for (MatchingService service : matchingServiceList) {
            stopMatchingService(service);
        }        
        for (BlockingService service : blockingServiceList) {
            stopBlockingService(service);
        }
		if (notificationService != null) {
			try {
				notificationService.shutdown();
			} catch (Exception e) {
				log.error("Failed while shutting down the notification service for OpenEMPI: " + e, e);
			}
		}
		stopThreadPool();
		isInitialized = false;		
	}
	
	public static void notifyObserver(ObservationEventType eventType, Object eventData) {
		EventObservable observable = observableByType.get(eventType);
		if (observable == null) {
			log.warn("Received a notification event of an unknown event type: " + eventType);
			return;
		}
		observable.setChanged();
		observable.notifyObservers(eventData);
		log.info("Notified observers of the occurence of an " + eventType.name() + " event.");
	}
	
	public static void registerObserver(Observer observer, ObservationEventType eventType) {
		EventObservable observable = observableByType.get(eventType);
		if (observable == null) {
			log.warn("Received event observer registration request for an unknown event type: " + eventType);
			return;
		}
		observable.addObserver(observer);
		log.info("Added event observer: " + observer + " for event of type " + eventType.name());
	}
	
	public static void unregisterObserver(Observer observer, ObservationEventType eventType) {
		EventObservable observable = observableByType.get(eventType);
		if (observable == null) {
			log.warn("Received event observer unregistration request for an unknown event type: " + eventType);
			return;
		}
		observable.deleteObserver(observer);
		log.info("Removed event observer: " + observer + " for event of type " + eventType.name());
	}
	
	private static void stopScheduledTasks() {
		scheduler.shutdown();
		try {
			if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
				scheduler.shutdownNow();
				if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
					log.error("Scheduler did not terminate");
				}
			}
		} catch (InterruptedException ie) {
			scheduler.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
	
	private static void stopThreadPool() {
		threadPool.shutdown();
		try {
			if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
				threadPool.shutdownNow();
				if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
					log.error("Pool did not terminate");
				}
			}
		} catch (InterruptedException ie) {
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	public static boolean isInitialized() {
		return isInitialized;
	}
	
	public static String[] getConfigLocationsAsArray() {
		ArrayList<String> configLocations = getConfigLocations();
		return (String[]) configLocations.toArray(new String[]{});
	}

	public static ArrayList<String> getConfigLocations() {
		if (getOpenEmpiHome() == null) {
			log.error("The OPENEMPI_HOME environment variable has not been configured; shutting down.");
			throw new RuntimeException("The OPENEMPI_HOME environment variable has not been configured.");
		}
		ArrayList<String> configFiles = generateConfigFileList();
		
		addExtensionContextsFromFile(configFiles);
		addExtensionContextsFromSystemProperty(configFiles);
		return configFiles;
	}

	public static String getOpenEmpiHome() {
		String openEmpiHome = Constants.OPENEMPI_HOME_ENV_VALUE;
		if (openEmpiHome == null || openEmpiHome.length() == 0) {
			openEmpiHome = Constants.OPENEMPI_HOME_VALUE;
		} else {
			System.setProperty(Constants.OPENEMPI_HOME, openEmpiHome);
		}
		log.debug("OPENEMPI_HOME is set to " + openEmpiHome);
		return openEmpiHome;
	}

	public static boolean isInCLusterMode() {
	    String value = System.getProperty(Constants.OPENEMPI_CLUSTER_MODE);
	    if (value == null || value.isEmpty()) {
	        return false;
	    }
	    if (value.equalsIgnoreCase("true")) {
	        return true;
	    }
	    return false;
	}

	private static ArrayList<String> generateConfigFileList() {
		ArrayList<String> configFiles = new ArrayList<String>();
		String openEmpiHome = getOpenEmpiHome();
		if (openEmpiHome != null && openEmpiHome.length() > 0) {
			configFiles.add("file:" + openEmpiHome + "/conf/applicationContext-resources.xml");
			configFiles.add("file:" + openEmpiHome + "/conf/applicationContext-dao.xml");
			configFiles.add("file:" + openEmpiHome + "/conf/applicationContext-service.xml");
			configFiles.add("file:" + openEmpiHome + "/conf/applicationContext-resources-*.xml");
		} else {
			configFiles.add("classpath:/applicationContext-resources.xml");
			configFiles.add("classpath:/applicationContext-dao.xml");
			configFiles.add("classpath:/applicationContext-service.xml");
		}
		configFiles.add("classpath:/applicationContext-resources-*.xml");
		return configFiles;
	}

	private static void addExtensionContextsFromSystemProperty(ArrayList<String> configFiles) {
		String extensionContexts = System.getProperty(Constants.OPENEMPI_EXTENSION_CONTEXTS);
		addExtensionContextsFromCommaSeparatedList(configFiles, extensionContexts);
	}

	private static void addExtensionContextsFromFile(ArrayList<String> configFiles) {
		try {
			String filename = getOpenEmpiHome() + "/conf/" + getExtensionsContextsPropertiesFilename();
			log.debug("Attempting to load extension contexts from " + filename);
			Scanner scanner = new Scanner(new File(filename));
			while (scanner.hasNext()) {
			    String line = scanner.nextLine();
			    if (line != null && line.startsWith("#")) {
			        continue;
			    }
			    log.info("Adding extenstion application context from location: " + line);
			    configFiles.add(line);
			}
		} catch (Exception e) {
			log.warn("Unable to load the extension contexts properties file; will resort to System property. Error: " + e, e);
			return;
		}
	}

	private static String getExtensionsContextsPropertiesFilename() {
		String filename = System.getProperty(Constants.OPENEMPI_EXTENSION_CONTEXTS_FILENAME);
		if (filename != null) {
			return filename;
		}
		return Constants.OPENEMPI_EXTENSION_CONTEXTS_PROPERTY_FILENAME;
	}

	private static void addExtensionContextsFromCommaSeparatedList(ArrayList<String> configFiles, String extensionContexts) {
		if (extensionContexts != null && extensionContexts.length() > 0) {
			String[] extContexts = extensionContexts.split(",");
			for (String extContext : extContexts) {
				log.debug("Adding extension application context from location: " + extContext);
				configFiles.add(extContext);
			}
		}
	}	

	private static void startNotificationService() {
		try {
			log.info("Starting the notification service...");
			notificationService.startup();
			log.info("Notification service was started successfuly.");
		} catch (Exception e) {
			log.error("Failed while trying to start the notification service. Error: " + e, e);
			throw new RuntimeException("Unable to start the notification service due to: " + e.getMessage());
		}
	}
	
	private static void startScheduledTasks() {
		try {
			log.info("Scheduling tasks...");
			@SuppressWarnings("unchecked")
			List<ScheduledTaskEntry> list = (List<ScheduledTaskEntry>)
					configuration.lookupConfigurationEntry(null, Configuration.SCHEDULED_TASK_LIST);
			if (list != null && list.size() > 0) {
				startScheduledTasks(list);
			}
			log.info("Tasks have been scheduled.");
		} catch (Exception e) {
			log.error("Failed while trying to schedule tasks. Error: " + e, e);
			throw new RuntimeException("Unable to schedule tasks: " + e.getMessage());
		}
	}

	private static void startScheduledTasks(List<ScheduledTaskEntry> list) {
		for (ScheduledTaskEntry task : list) {
			if (task.getScheduleType() == ScheduledTaskEntry.SCHEDULE_ENTRY_TYPE) {
				log.info("Scheduling the task: " + task);
				scheduler.schedule(task.getRunableTask(), task.getDelay(), task.getTimeUnit());
			} else if (task.getScheduleType() == ScheduledTaskEntry.SCHEDULE_AT_FIXED_RATE_ENTRY_TYPE) {
				log.info("Scheduling the task: " + task);
				scheduler.scheduleAtFixedRate(task.getRunableTask(), task.getInitialDelay(), task.getPeriod(), task.getTimeUnit());
			} else if (task.getScheduleType() == ScheduledTaskEntry.SCHEDULE_WITH_FIXED_DELAY_ENTRY_TYPE) {
				log.info("Scheduling the task: " + task);
				scheduler.scheduleWithFixedDelay(task.getRunableTask(), task.getInitialDelay(), task.getDelay(), task.getTimeUnit());
			}
		}
	}

	public static String authenticate(String username, String password)
			throws AuthenticationException {
		return getUserContext().authenticate(username, password);
	}
	
	public static User authenticate(String sessionKey)
			throws AuthenticationException {
		return getUserContext().authenticate(sessionKey);
	}

    public static Map<String, Object> getConfigurationRegistry() {
        return clusterManager.getConfigurationRegistry();
    }

    public static Object lookupConfigurationEntry(String entityName, String key) {
        return clusterManager.lookupConfigurationEntry(entityName, key);
    }


    public static void registerConfigurationEntry(String entityName, String key, Object entry) {
        clusterManager.registerConfigurationeEntry(entityName, key, entry);
    }

	public static UserContext getUserContext() {
		UserContext userContext = null;
		Object[] arr = userContextHolder.get();
		if (arr == null) {
			log.trace("userContext is null. Creating new userContext");
			userContext = new UserContext();
			userContext.setUserManager(userManager);
			setUserContext(userContext);
		}
		return (UserContext) userContextHolder.get()[0];
	}

	public static void setUserContext(UserContext ctx) {
		log.trace("Setting user context " + ctx);
		Object[] arr = new Object[] { ctx };
		userContextHolder.set(arr);
	}

	private static void startBlockingService(Object service) {
		Callable<Object> task = new ServiceStarterStopper("Starting the blocking service at startup.",
				ServiceStarterStopper.START_SERVICE, ServiceStarterStopper.BLOCKING_SERVICE, service);
		Future<Object> future = threadPool.submit(task);
		try {
			future.get();
		} catch (InterruptedException e) {
			log.error("Failed while starting up the blocking service: " + e, e);
			throw new RuntimeException("Initialization failed while starting the blocking service.");
		} catch (ExecutionException e) {
			log.error("Failed while starting up the blocking service: " + e, e);
			throw new RuntimeException("Initialization failed while starting the blocking service.");
		}
	}

	private static void stopBlockingService(Object service) {
		Callable<Object> task = new ServiceStarterStopper("Shutting down the blocking service before system shutdown.",
				ServiceStarterStopper.STOP_SERVICE, ServiceStarterStopper.BLOCKING_SERVICE, service);
		try {
			Future<Object> future = threadPool.submit(task);
			future.get();
		} catch (RejectedExecutionException e) {
			log.warn("Was unable to initiate a stop request on the matching service: " + e, e);			
		} catch (InterruptedException e) {
			log.error("Failed while shutting down the blocking service: " + e, e);
			throw new RuntimeException("Initialization failed while shutting down the blocking service.");
		} catch (ExecutionException e) {
			log.error("Failed while shutting up the blocking service: " + e, e);
			throw new RuntimeException("Initialization failed while shutting down the blocking service.");
		}		
	}

	public static Future<Object> scheduleTask(Callable<Object> task) {
		Future<Object> future = threadPool.submit(task);
//		try {
//			future.get();
//		} catch (InterruptedException e) {
//			log.error("Failed while scheduling a caller provided task: " + e, e);
//			throw new RuntimeException("Failed while scheduling a caller provided task.");
//		} catch (ExecutionException e) {
//			log.error("Failed while scheduling a caller provided task: " + e, e);
//			throw new RuntimeException("Failed while scheduling a caller provided task.");
//		}
		return future;
	}
	
	private static void startPersistenceService() {
		try {
			PersistenceLifecycleObserver persistenceService = (PersistenceLifecycleObserver) Context.getEntityDefinitionManagerService();
			if (persistenceService != null) {
				persistenceService.startup();
			}
			persistenceService = (PersistenceLifecycleObserver) Context.getRecordManagerService();
			if (persistenceService != null) {
				persistenceService.startup();
			}
		} catch (Exception e) {
			log.error("Unable to start the persistence service due to: " + e, e);
			System.exit(-1);
		}		
	}

	private static void stopPersistenceService(Object service) {
		Callable<Object> task = new ServiceStarterStopper("Shutting down the persistence service before system shutdown.",
				ServiceStarterStopper.STOP_SERVICE, ServiceStarterStopper.PERSISTENCE_SERVICE, service);
		try {
			Future<Object> future = threadPool.submit(task);
			future.get();
		} catch (RejectedExecutionException e) {
			log.warn("Was unable to initiate a stop request on the persistence service: " + e, e);
		} catch (InterruptedException e) {
			log.error("Failed while shutting down the blocking service: " + e, e);
			throw new RuntimeException("Initialization failed while shutting down the persistence service.");
		} catch (ExecutionException e) {
			log.error("Failed while shutting down the blocking service: " + e, e);
			throw new RuntimeException("Initialization failed while shutting down the persistence service.");
		}		
	}
    
//    private static void startRecordCacheService() {
//        try {
//            RecordCacheLifecycleObserver recordCacheService = (RecordCacheLifecycleObserver) Context.getRecordCacheService();
//            if (recordCacheService != null) {
//                recordCacheService.startup();
//            }
//        } catch (Exception e) {
//            log.error("Unable to start the record cache service due to: " + e, e);
//            System.exit(-1);
//        }       
//    }

    private static void stopRecordCacheService(Object service) {
        Callable<Object> task = new ServiceStarterStopper("Shutting down the record cache service before system shutdown.",
                ServiceStarterStopper.STOP_SERVICE, ServiceStarterStopper.RECORD_CACHE_SERVICE, service);
        try {
            Future<Object> future = threadPool.submit(task);
            future.get();
        } catch (RejectedExecutionException e) {
            log.warn("Was unable to initiate a stop request on the record service: " + e, e);
        } catch (InterruptedException e) {
            log.error("Failed while shutting down the record cache service: " + e, e);
            throw new RuntimeException("Initialization failed while shutting down the record cache service.");
        } catch (ExecutionException e) {
            log.error("Failed while shutting down the record cache service: " + e, e);
            throw new RuntimeException("Initialization failed while shutting down the record cache service.");
        }
    }
	
	private static void startMatchingService(Object service) {
		Callable<Object> task = new ServiceStarterStopper("Starting the matching service at startup.",
				ServiceStarterStopper.START_SERVICE, ServiceStarterStopper.MATCHING_SERVICE, service);
		Future<Object> future = threadPool.submit(task);
		try {
			future.get();
		} catch (InterruptedException e) {
			log.error("Failed while starting up the blocking service: " + e, e);
			throw new RuntimeException("Initialization failed while starting the blocking service.");
		} catch (ExecutionException e) {
			log.error("Failed while starting up the blocking service: " + e, e);
			throw new RuntimeException("Initialization failed while starting the blocking service.");
		}		
	}

	private static void stopMatchingService(Object service) {
		Callable<Object> task = new ServiceStarterStopper("Shutting down the matching service before system shutdown.",
				ServiceStarterStopper.STOP_SERVICE, ServiceStarterStopper.MATCHING_SERVICE, service);
		try {
			Future<Object> future = threadPool.submit(task);
			future.get();
		} catch (RejectedExecutionException e) {
			log.warn("Was unable to initiate a stop request on the matching service: " + e, e);
		} catch (InterruptedException e) {
			log.error("Failed while shutting down the blocking service: " + e, e);
			throw new RuntimeException("Initialization failed while shutting down the blocking service.");
		} catch (ExecutionException e) {
			log.error("Failed while shutting down the blocking service: " + e, e);
			throw new RuntimeException("Initialization failed while shutting down the blocking service.");
		}		
	}

	public static PersonManagerService getPersonManagerService() {
		return personService;
	}
	
	public void setPersonManagerService(PersonManagerService personService) {
		Context.personService = personService;
	}

	public static IdentifierDomainService getIdentifierDomainService() {
		return identifierDomainService;
	}
	
	public void setIdentifierDomainService(IdentifierDomainService identifierDomainService) {
		Context.identifierDomainService = identifierDomainService;
	}
	
	public static EntityDefinitionManagerService getEntityDefinitionManagerService() {
		return entityDefinitionManagerService;
	}
	
	public void setEntityDefinitionManagerService(EntityDefinitionManagerService entityDefinitionManagerService) {
		Context.entityDefinitionManagerService = entityDefinitionManagerService;
	}
	
	public static RecordManagerService getRecordManagerService() {
		return recordManagerService;
	}
	
	public void setRecordManagerService(RecordManagerService entityManagerService) {
		Context.recordManagerService = entityManagerService;
	}
	
	public static RecordQueryService getRecordQueryService() {
		return recordQueryService;
	}
	
	public void setRecordQueryService(RecordQueryService entityQueryService) {
		Context.recordQueryService = entityQueryService;
	}

    public static JobQueueService getJobQueueService() {
        return jobQueueService;
    }

    public void setJobQueueService(JobQueueService jobQueueService) {
        Context.jobQueueService = jobQueueService;
    }

	public void setApplicationContext(ApplicationContext applicationContext) {
		Context.applicationContext = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
	public static UserManager getUserManager() {
		return Context.userManager;
	}
	
	public void setUserManager(UserManager userManager) {
	    Context.userManager = userManager;
	}

	public static Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		Context.configuration = configuration;
	}

	public static ValidationService getValidationService() {
		return validationService;
	}

	public void setValidationService(ValidationService validationService) {
		Context.validationService = validationService;
	}
    
	public synchronized static MatchingService getMatchingService(String entityName) {
        return matchingServiceMap.get(entityName);
	}

	public static void registerCustomMatchingService(String entityName, MatchingService matchingService) {
	    if (matchingServiceMap.get(entityName) == null) {
	        matchingServiceMap.put(entityName, matchingService);
	    }
	    for (MatchingService service : matchingServiceList) {
	        if (service.getMatchingServiceId() == matchingService.getMatchingServiceId()) {
	            return;
	        }
	    }
	    matchingServiceList.add(matchingService);
	}

	public static void registerCustomBlockingService(String entityName, BlockingService blockingService) {
        if (blockingServiceMap.get(entityName) == null) {
            blockingServiceMap.put(entityName, blockingService);
        }
        for (BlockingService service : blockingServiceList) {
            if (service.getBlockingServiceId() == blockingService.getBlockingServiceId()) {
                return;
            }
        }
        blockingServiceList.add(blockingService);
	}
	
	public static ClusterManager getClusterManager() {
	    return clusterManager;
	}
	
	public static BlockingService getBlockingService(String entityName) {
        return blockingServiceMap.get(entityName);
	}
	
	public static SingleBestRecordService getSingleBestRecordService() {
		return singleBestRecordService;
	}

	public void setSingleBestRecordService(SingleBestRecordService singleBestRecordService) {
		Context.singleBestRecordService = singleBestRecordService;
	}

	public static void registerCustomSingleBestRecordService(SingleBestRecordService singleBestRecordService) {
		Context.singleBestRecordService = singleBestRecordService;
	}

	public static PersonQueryService getPersonQueryService() {
		return personQueryService;
	}

	public void setPersonQueryService(PersonQueryService personQueryService) {
		Context.personQueryService = personQueryService;
	}
	
	public static StringComparisonService getStringComparisonService() {
		return stringComparisonService;
	}
	
	public void setStringComparisonService(StringComparisonService stringComparisonService) {
		Context.stringComparisonService = stringComparisonService;
	}

	public static TransformationService getTransformationService() {
		return transformationService;
	}
	
	public void setTransformationService(TransformationService transformationService) {
		Context.transformationService = transformationService;
	}

	public static AuditEventService getAuditEventService() {
		return auditEventService;
	}

	public void setAuditEventService(AuditEventService auditEventService) {
		Context.auditEventService = auditEventService;
	}
	public static void registerCustomFileLoaderConfigurationService(FileLoaderConfigurationService fileLoaderConfigurationService) {
		Context.fileLoaderConfigurationService = fileLoaderConfigurationService;
	}
	
	public static FileLoaderConfigurationService getFileLoaderConfigurationService() {
		return fileLoaderConfigurationService;
	}

	public void setFileLoaderConfigurationService(FileLoaderConfigurationService fileLoaderConfigurationService) {
		Context.fileLoaderConfigurationService = fileLoaderConfigurationService;
	}

	public static NotificationService getNotificationService() {
		return notificationService;
	}

	public void setNotificationService(NotificationService notificationService) {
		Context.notificationService = notificationService;
	}
	
	public static ReportService getReportService() {
		return reportService;
	}

	public void setReportService(ReportService reportService) {
		Context.reportService = reportService;
	}
	
	public static DataProfileService getDataProfileService() {
		return dataProfileService;
	}

	public void setDataProfileService(DataProfileService dataProfileService) {
		Context.dataProfileService = dataProfileService;
	}
	
	public static EntityValidationService getEntityValidationService() {
		return entityValidationService;
	}
	
	public void setEntityValidationService(EntityValidationService entityValidationService) {
		Context.entityValidationService = entityValidationService;
	}
	
	public static ScheduledExecutorService getScheduler() {
		return scheduler;
	}

    public static DataAccessIntent getDataAccessIntent() {
        return Context.currentIntent;
    }

    public static void registerDataAccessIntent(DataAccessIntent dataAccessIntent) {
        Context.currentIntent = dataAccessIntent;
    }

	private static class ServiceStarterStopper implements Callable<Object>
	{
		private final static int START_SERVICE = 0;
		private final static int STOP_SERVICE = 1;
		private final static int BLOCKING_SERVICE = 2;
		private final static int MATCHING_SERVICE = 3;
		private final static int PERSISTENCE_SERVICE = 4;
        private final static int RECORD_CACHE_SERVICE = 5;
		
		private String message;
		private int operation;
		private int serviceType;
		private Object service;
		
		public ServiceStarterStopper(String message, int operation, int serviceType, Object service) {
			this.message = message;
			this.operation = operation;
			this.serviceType = serviceType;
			this.service = service;
		}
		
		public Object call() throws Exception {
			log.info(message);
			if (operation == START_SERVICE) {
				return startService();
			} else {
				return stopService();
			}
		}
		
		public Object startService() {
			if (serviceType == BLOCKING_SERVICE) {
				BlockingLifecycleObserver blockingService = (BlockingLifecycleObserver) service;
				if (blockingService != null) {
					blockingService.startup();
				}
				return blockingService;
			} else if (serviceType == MATCHING_SERVICE) {
				MatchingLifecycleObserver matchingService = (MatchingLifecycleObserver) service;
				if (matchingService != null) {
					matchingService.startup();
				}
				return matchingService;	
			} else if (serviceType == PERSISTENCE_SERVICE) {
				PersistenceLifecycleObserver persistenceService = (PersistenceLifecycleObserver)
				        Context.getEntityDefinitionManagerService();
				if (persistenceService != null) {
					persistenceService.startup();
				}
				persistenceService = (PersistenceLifecycleObserver) Context.getRecordManagerService();
				if (persistenceService != null) {
					persistenceService.startup();
				}
				return persistenceService;
//            } else if (serviceType == RECORD_CACHE_SERVICE) {
//                RecordCacheLifecycleObserver recordCacheService = (RecordCacheLifecycleObserver)
//                        Context.getRecordCacheService();
//                if (recordCacheService != null) {
//                    recordCacheService.startup();
//                }
//                return recordCacheService;
			} else {
				return null;
			}
		}
		
		public Object stopService() {
			if (serviceType == BLOCKING_SERVICE) {
				BlockingLifecycleObserver blockingService = (BlockingLifecycleObserver) service;
				if (blockingService != null) {
					blockingService.shutdown();
				}
				return blockingService;
			} else if (serviceType == MATCHING_SERVICE) {
				MatchingLifecycleObserver matchingService = (MatchingLifecycleObserver) service;
				if (matchingService != null) {
					matchingService.shutdown();
				}
				return matchingService;
			} else if (serviceType == PERSISTENCE_SERVICE) {
				PersistenceLifecycleObserver persistenceService = (PersistenceLifecycleObserver) 
				        Context.getRecordManagerService();
				if (persistenceService != null) {
					persistenceService.shutdown();
				}
				persistenceService = (PersistenceLifecycleObserver) Context.getEntityDefinitionManagerService();
				if (persistenceService != null) {
					persistenceService.shutdown();
				}
				return persistenceService;
//            } else if (serviceType == RECORD_CACHE_SERVICE) {
//                RecordCacheLifecycleObserver recordCacheService = (RecordCacheLifecycleObserver)
//                        Context.getRecordCacheService();
//                if (recordCacheService != null) {
//                    recordCacheService.shutdown();
//                }
//                return recordCacheService;
			} else {
				return null;
			}
		}
	}
}
