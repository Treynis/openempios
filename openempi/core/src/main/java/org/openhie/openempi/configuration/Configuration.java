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
package org.openhie.openempi.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openhie.openempi.Constants;
import org.openhie.openempi.InitializationException;
import org.openhie.openempi.blocking.BlockingService;
import org.openhie.openempi.configuration.Component.ComponentType;
import org.openhie.openempi.configuration.Component.ExtensionInterface;
import org.openhie.openempi.configuration.xml.BlockingConfigurationType;
import org.openhie.openempi.configuration.xml.MatchingConfigurationType;
import org.openhie.openempi.configuration.xml.MpiConfigDocument;
import org.openhie.openempi.configuration.xml.MpiConfigDocument.MpiConfig;
import org.openhie.openempi.configuration.xml.ScheduledTask;
import org.openhie.openempi.configuration.xml.ScheduledTasks;
import org.openhie.openempi.configuration.xml.ShallowMatchingConfigurationType;
import org.openhie.openempi.configuration.xml.SingleBestRecordType;
import org.openhie.openempi.configuration.xml.UpdateNotificationEntry;
import org.openhie.openempi.configuration.xml.mpicomponent.ExtensionType;
import org.openhie.openempi.configuration.xml.mpicomponent.ExtensionType.Interface.Enum;
import org.openhie.openempi.configuration.xml.mpicomponent.MpiComponentDefinitionDocument;
import org.openhie.openempi.configuration.xml.mpicomponent.MpiComponentType;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.matching.MatchingService;
import org.openhie.openempi.matching.ShallowMatchingService;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.User;
import org.openhie.openempi.service.Parameterizable;
import org.openhie.openempi.service.impl.BaseServiceImpl;
import org.openhie.openempi.singlebestrecord.SingleBestRecordService;

public class Configuration extends BaseServiceImpl implements ConfigurationRegistry
{
	protected static final Log log = LogFactory.getLog(Configuration.class);

	private String configFile;
	private MpiConfigDocument configuration;
	private GlobalIdentifier globalIdentifier;
	private AdminConfiguration adminConfiguration;
	
	private Map<String, Object> defaultConfigurationRegistry;
	private Map<String, Component> extensionRegistry;
	private Map<String, ConfigurationLoader> loaderByEntity;

	public void init() {
		configureLoggingEnvironment();
		defaultConfigurationRegistry = new HashMap<String,Object>();
		extensionRegistry = new HashMap<String,Component>();
		loaderByEntity = new HashMap<String,ConfigurationLoader>();
		try {
			configuration = loadConfigurationFromSource();
			validateConfiguration(configuration);
			processConfiguration(configuration);
//			setupRecordCache();
			log.info("System configuration: " + this.toString());
		} catch (Exception e) {
			log.error("Failed while locating and parsing the configuration file. System is shutting down due to: " + e, e);
			throw new RuntimeException("Failed while locating and parsing the configuration file. System is shutting down.");
		}
	}

//    private void setupRecordCache() {
//        Object obj = Context.getApplicationContext().getBean(Constants.RECORD_CACHE_SERVICE);
//        if (obj == null) {
//            log.error("No record cache has been defined in the system.");
//            return;
//        }
//        if (!(obj instanceof RecordCacheService)) {
//            log.error("The configured record cache service does not support the required interface.");
//        }
//        RecordCacheService service = (RecordCacheService) obj;
//        Context.registerCustomRecordCacheService(service);
//    }

    private void configureLoggingEnvironment() {
		String openEmpiHome = Context.getOpenEmpiHome();
		if (openEmpiHome != null && openEmpiHome.length() > 0) {
			String loggingConfigurationFile = openEmpiHome + "/conf/log4j.properties";
			PropertyConfigurator.configure(loggingConfigurationFile);
			log.info("Set the logging configuration file to " + loggingConfigurationFile);
		}
	}

	public BlockingConfigurationType getBlockingConfiguration() {
		return configuration.getMpiConfig().getBlockingConfigurationArray(0);
	}

	private void processConfiguration(MpiConfigDocument configuration) {
		globalIdentifier = processGlobalIdentifier(configuration);
		processScheduledTasks(configuration);

		try {
		    BlockingService service = null;
		    service = processBlockingConfiguration(configuration);
            if (service == null) {
                setupNaiveBlockingService("*");
            }
		} catch (Exception e) {
			log.error("Was unable to load the blocking configuration: " + e, e);
		}

		try {
		    processMatchConfiguration(configuration);
		} catch (Exception e) {
			log.warn("Was unable to load the matching service configuration: " + e, e);
		}

        try {
            processShallowMatchConfiguration(configuration);
        } catch (Exception e) {
            log.warn("Was unable to load the shallow matching service configuration: " + e, e);
        }

		try {
			processSingleBestRecordConfiguration(configuration);
		} catch (Exception e) {
			log.warn("Was unable to load the single best record configuration: " + e, e);
		}
		adminConfiguration = processAdminConfiguration(configuration);
	}

	private List<ScheduledTaskEntry> processScheduledTasks(MpiConfigDocument configuration) {
		checkConfiguration(configuration);

		List<ScheduledTaskEntry> list = new ArrayList<ScheduledTaskEntry>();
		ScheduledTasks tasks = configuration.getMpiConfig().getScheduledTasks();
		if (tasks == null) {
			log.warn("No scheduled tasks have been specified in the configuration.");
			return list;
		}

		for (int i = 0; i < tasks.sizeOfScheduledTaskArray(); i++) {
			org.openhie.openempi.configuration.xml.ScheduledTask taskXml = tasks.getScheduledTaskArray(i);
			ScheduledTaskEntry entry = buildScheduledTaskFromXml(taskXml);
			if (entry != null) {
				list.add(entry);
			}
		}
        registerConfigurationEntry(null, ConfigurationRegistry.SCHEDULED_TASK_LIST, list);
		return list;
	}

	private ScheduledTaskEntry buildScheduledTaskFromXml(ScheduledTask taskXml) {
		ScheduledTaskEntry taskEntry = new ScheduledTaskEntry();
		String entityName = taskXml.getEntityName();
		taskEntry.setTaskName(taskXml.getTaskName());
		taskEntry.setTaskImplementation(taskXml.getTaskImplementation());
		Object taskImplementation = Context.getApplicationContext().getBean(taskEntry.getTaskImplementation());
		if (taskImplementation == null || !(taskImplementation instanceof Runnable)) {
			log.error("Encounter an invalid scheduled task entry that will be ignored due to an unknown implementation classs.: " + taskXml);
			return null;
		}
		if (taskImplementation instanceof Parameterizable) {
		    Parameterizable takesParameters = (Parameterizable) taskImplementation;
		    Map<String,Object> params = new HashMap<String,Object>();
		    params.put(Constants.ENTITY_NAME_KEY, entityName);
		    takesParameters.setParameters(params);
		}
		taskEntry.setRunableTask((Runnable) taskImplementation);
		taskEntry.setTimeUnit(getTimeUnit(taskXml.getTimeUnit()));
		if (taskXml.getScheduleType() == null || taskXml.getTimeUnit() == null) {
			log.error("Encountered an invalid scheduled task entry that will be ignored due to missing required attributes: " + taskXml);
			return null;			
		}
		if (taskXml.getScheduleType().intValue() == ScheduledTask.ScheduleType.SCHEDULE.intValue()) {
			taskEntry.setDelay(taskXml.getDelay());
			taskEntry.setScheduleType(ScheduledTaskEntry.SCHEDULE_ENTRY_TYPE);
			if (taskEntry.getTaskName() == null || taskEntry.getTaskImplementation() == null || !taskXml.isSetDelay()) {
				log.error("Encountered an invalid scheduled task entry that will be ignored due to missing required elements: " + taskXml);
				return null;
			}
		} else if (taskXml.getScheduleType().intValue() == ScheduledTask.ScheduleType.SCHEDULE_AT_FIXED_RATE.intValue()) {
			taskEntry.setInitialDelay(taskXml.getInitialDelay());
			taskEntry.setPeriod(taskXml.getPeriod());
			taskEntry.setScheduleType(ScheduledTaskEntry.SCHEDULE_AT_FIXED_RATE_ENTRY_TYPE);
			if (taskEntry.getTaskName() == null || taskEntry.getTaskImplementation() == null || 
					!taskXml.isSetInitialDelay() || !taskXml.isSetPeriod()) {
				log.error("Encountered an invalid scheduled task entry that will be ignored due to missing required parameters.: " + taskXml);
				return null;
			}			
		} else if (taskXml.getScheduleType().intValue() == ScheduledTask.ScheduleType.SCHEDULE_WITH_FIXED_DELAY.intValue()) {
			taskEntry.setDelay(taskXml.getDelay());
			taskEntry.setInitialDelay(taskXml.getInitialDelay());
			taskEntry.setScheduleType(ScheduledTaskEntry.SCHEDULE_WITH_FIXED_DELAY_ENTRY_TYPE);
			if (taskEntry.getTaskName() == null || taskEntry.getTaskImplementation() == null || 
					!taskXml.isSetInitialDelay() || !taskXml.isSetDelay()) {
				log.error("Encountered an invalid scheduled task entry that will be ignored due to missing required parameters.: " + taskXml);
				return null;
			}			
		}
		return taskEntry;
	}

	private TimeUnit getTimeUnit(org.openhie.openempi.configuration.xml.ScheduledTask.TimeUnit.Enum timeUnit) {
		if (timeUnit == org.openhie.openempi.configuration.xml.ScheduledTask.TimeUnit.DAYS) {
			return TimeUnit.DAYS;
		} else if (timeUnit == org.openhie.openempi.configuration.xml.ScheduledTask.TimeUnit.HOURS) {
			return TimeUnit.HOURS;
		} else if (timeUnit == org.openhie.openempi.configuration.xml.ScheduledTask.TimeUnit.MICROSECONDS) {
			return TimeUnit.MICROSECONDS;
		} else if (timeUnit == org.openhie.openempi.configuration.xml.ScheduledTask.TimeUnit.MILLISECONDS) {
			return TimeUnit.MILLISECONDS;
		} else if (timeUnit == org.openhie.openempi.configuration.xml.ScheduledTask.TimeUnit.MINUTES) {
			return TimeUnit.MINUTES;
		} else if (timeUnit == org.openhie.openempi.configuration.xml.ScheduledTask.TimeUnit.NANOSECONDS) {
			return TimeUnit.NANOSECONDS;
		} else if (timeUnit == org.openhie.openempi.configuration.xml.ScheduledTask.TimeUnit.SECONDS) {
			return TimeUnit.SECONDS;
		}
		log.warn("An unknown time-unit was specified for a scheduled task of " + timeUnit + " so will resort to seconds as the default time unit.");
		return TimeUnit.SECONDS;
	}

	private void setupNaiveBlockingService(String entityName) {
		log.warn("Was unable to load the blocking service configuration; using the naive blocking service as a fallback mechanism.");
		BlockingService blockingService = (BlockingService) 
				Context.getApplicationContext().getBean(Constants.NAIVE_BLOCKING_SERVICE);
		Context.registerCustomBlockingService(entityName, blockingService);		
	}

	private AdminConfiguration processAdminConfiguration(MpiConfigDocument configuration) {
		adminConfiguration = new AdminConfiguration();
		if (configuration.getMpiConfig().getAdminConfiguration() != null) {
			adminConfiguration.setConfigFileDirectory(configuration.getMpiConfig().getAdminConfiguration().getFileRepositoryDirectory());
		}
		
		org.openhie.openempi.configuration.xml.AdminConfiguration adminConfig = configuration.getMpiConfig().getAdminConfiguration();
		if (adminConfig.getFileRepositoryDirectory() == null) {
			adminConfiguration.setConfigFileDirectory(Constants.DEFAULT_FILE_REPOSITORY_DIRECTORY);
		} else {
			adminConfiguration.setConfigFileDirectory(adminConfig.getFileRepositoryDirectory());
		}
		
		if (adminConfig.getDataDirectory() == null) {
			adminConfiguration.setDataDirectory(Context.getOpenEmpiHome() + "/data");
		} else {
			adminConfiguration.setDataDirectory(adminConfig.getDataDirectory());
		}
		
        if (configuration.getMpiConfig().getAdminConfiguration().getUpdateNotificationEntries() != null
                && configuration.getMpiConfig().getAdminConfiguration().getUpdateNotificationEntries()
                        .sizeOfUpdateNotificationEntryArray() > 0) {
            UpdateNotificationEntry[] entries = configuration.getMpiConfig().getAdminConfiguration()
                    .getUpdateNotificationEntries().getUpdateNotificationEntryArray();
            for (int i = 0; i < entries.length; i++) {
                UpdateNotificationRegistrationEntry entry = processUpdateNotificationEntry(entries[i]);
                if (entry != null) {
                    adminConfiguration.addUpdateNotificationRegistrationEntries(entry);
                }
            }
        }
		return adminConfiguration;
	}

    private UpdateNotificationRegistrationEntry processUpdateNotificationEntry(UpdateNotificationEntry xmlEntry) {
        String userName = xmlEntry.getUser();
        if (userName == null ||  userName.length() == 0) {
            log.error("Encountered update notification registration entry without a user account. Entry will be ignored: " + xmlEntry.toString());
            return null;
        }
        User user = Context.getUserManager().getUserByUsername(userName);
        if (user == null) {
            log.error("Encountered update notification registration entry with an unknown user account. Entry will be ignored: " + xmlEntry.toString());
            return null;
        }
        String identifierDomainName = xmlEntry.getIdentifierDomainName();
        if (identifierDomainName == null || identifierDomainName.length() == 0) {
            log.error("Encountered update notification registration entry without an identifier domain. Entry will be ignored: " + xmlEntry.toString());
            return null;
        }
        IdentifierDomain domain = Context.getPersonQueryService().findIdentifierDomainByName(identifierDomainName);
        if (domain == null) {
            log.error("Encountered update notification registration entry with an unknown domain. Entry will be ignored: " + xmlEntry.toString());
            return null;
        }
        UpdateNotificationRegistrationEntry entry = new UpdateNotificationRegistrationEntry(user, domain, xmlEntry.getTimeToLive());
        log.warn("Adding update notification registration entry: " + entry);
        return entry;
    }
    
	private GlobalIdentifier processGlobalIdentifier(MpiConfigDocument configuration) {
		globalIdentifier = new GlobalIdentifier();
		if (!configuration.getMpiConfig().isSetGlobalIdentifier()) {
			globalIdentifier.setAssignGlobalIdentifier(false);
			return globalIdentifier;
		}
		globalIdentifier.setAssignGlobalIdentifier(configuration.getMpiConfig().getGlobalIdentifier().getAssignGlobalIdentifier());
		globalIdentifier.setIdentifierDomainName(configuration.getMpiConfig().getGlobalIdentifier().getIdentifierDomainName());
		globalIdentifier.setIdentifierDomainDescription(configuration.getMpiConfig().getGlobalIdentifier().getIdentifierDomainDescription());
		globalIdentifier.setNamespaceIdentifier(configuration.getMpiConfig().getGlobalIdentifier().getNamespaceIdentifier());
		globalIdentifier.setUniversalIdentifier(configuration.getMpiConfig().getGlobalIdentifier().getUniversalIdentifier());
		globalIdentifier.setUniversalIdentifierType(configuration.getMpiConfig().getGlobalIdentifier().getUniversalIdentifierType());
        IdentifierDomain domain = Context.getPersonQueryService().findIdentifierDomainByName(globalIdentifier.getIdentifierDomainName());
        if (domain == null) {
            log.error("Global identifier domain not found; correct system configuration and start system again.");
            System.exit(-1);            
        }
        globalIdentifier.setIdentifierDomain(domain);
		return globalIdentifier;
	}
	
	public IdentifierDomain getGlobalIdentifierDomain() {
		if (globalIdentifier == null) {
			throw new RuntimeException("The global identifier configuration has not been specified in the configuration file.");
		}
        return globalIdentifier.getIdentifierDomain();
	}

    public MpiConfig getMpiConfig() {
        return configuration.getMpiConfig();
    }

	private BlockingService processBlockingConfiguration(MpiConfigDocument configuration) {
		checkConfiguration(configuration);
		
		if (configuration.getMpiConfig().getBlockingConfigurationArray() == null) {
		    return null;
		}
		
		int count = configuration.getMpiConfig().getBlockingConfigurationArray().length;
		BlockingService blockingService=null;
		for (int i=0; i < count; i++) {
    		BlockingConfigurationType obj = configuration.getMpiConfig().getBlockingConfigurationArray(i);
    		if (obj == null) {
    			log.warn("No blocking service configuration has been specified.");
    			return null;
    		}
    		log.debug("Object is of type: " + obj.getDomNode().getNamespaceURI());
    		String namespaceUriStr = obj.getDomNode().getNamespaceURI();
    		URI namespaceURI = getNamespaceURI(namespaceUriStr);
    
    		String resourcePath = generateComponentResourcePath(namespaceURI);
    		Component component = loadAndRegisterComponentFromNamespaceUri(resourcePath);
    		
    		String configurationLoaderBean = getExtensionBeanNameFromComponent(component);
    		
    		ConfigurationLoader loader = (ConfigurationLoader) Context.getApplicationContext().getBean(configurationLoaderBean);
    		loader.loadAndRegisterComponentConfiguration(this, obj);
    		
    		Component.Extension extension = component.getExtensionByExtensionInterface(ExtensionInterface.IMPLEMENTATION);
    		if (extension == null) {
    			log.error("Encountered a custom blocking component with no implementation extension: " + component);
    			throw new InitializationException("Unable to locate an implementation component for custom blocking component " + component.getName());
    		}
    		log.debug("Registering implementation of blocking component named " + extension.getName() + " and implementation key " +
    				extension.getImplementationKey());
    		blockingService = (BlockingService) Context.getApplicationContext().getBean(extension.getImplementationKey());
    		
    		String entity = loader.getComponentEntity();
    		log.info("Registering blocking service " + blockingService + " for entity " + entity);
            Context.registerCustomBlockingService(entity, blockingService);
		}
		return blockingService;
	}
	
	private void processSingleBestRecordConfiguration(MpiConfigDocument configuration) {
		checkConfiguration(configuration);
		if (configuration.getMpiConfig().sizeOfSingleBestRecordArray() == 0) {
			log.warn("No single best record configuration has been specified.");
			return;
		}
		SingleBestRecordType obj = configuration.getMpiConfig().getSingleBestRecordArray(0);
		log.debug("Object is of type: " + obj.getDomNode().getNamespaceURI());
		String namespaceUriStr = obj.getDomNode().getNamespaceURI();
		URI namespaceURI = getNamespaceURI(namespaceUriStr);

		String resourcePath = generateComponentResourcePath(namespaceURI);
		Component component = loadAndRegisterComponentFromNamespaceUri(resourcePath);
		
		String configurationLoaderBean = getExtensionBeanNameFromComponent(component);
		
		ConfigurationLoader loader = (ConfigurationLoader) Context.getApplicationContext().getBean(configurationLoaderBean);
		loader.loadAndRegisterComponentConfiguration(this, obj);
		
		Component.Extension extension = component.getExtensionByExtensionInterface(ExtensionInterface.IMPLEMENTATION);
		if (extension == null) {
			log.error("Encountered a custom single best record component with no implementation extension: " + component);
			throw new InitializationException("Unable to locate an implementation component for custom single best record component " + component.getName());
		}
		log.debug("Registering implementation of single best record component named " + extension.getName() + " and implementation key " +
				extension.getImplementationKey());
		SingleBestRecordService singleBestRecordService = (SingleBestRecordService) 
			Context.getApplicationContext().getBean(extension.getImplementationKey());
		Context.registerCustomSingleBestRecordService(singleBestRecordService);
	}

	public ConfigurationLoader getBlockingConfigurationLoader(String entityName) {
		ConfigurationLoader loader = lookupConfigurationLoader(ComponentType.BLOCKING, entityName);
		return loader;
	}

	public ConfigurationLoader getMatchingConfigurationLoader(String entityName) {
        ConfigurationLoader loader = lookupConfigurationLoader(ComponentType.MATCHING, entityName);
		return loader;
	}

	private void processMatchConfiguration(MpiConfigDocument configuration) {
        if (configuration.getMpiConfig().getMatchingConfigurationArray() == null) {
            return;
        }

        int count = configuration.getMpiConfig().getMatchingConfigurationArray().length;
        for (int i = 0; i < count; i++) {
    		MatchingConfigurationType obj = configuration.getMpiConfig().getMatchingConfigurationArray(i);
    		if (obj == null) {
    			log.warn("No matching service configuration has been specified.");
    			return;
    		}
    		log.debug("Object is of type: " + obj.getDomNode().getNamespaceURI());
    		String namespaceUriStr = obj.getDomNode().getNamespaceURI();
    		URI namespaceURI = getNamespaceURI(namespaceUriStr);

    		String resourcePath = generateComponentResourcePath(namespaceURI);
    		Component component = loadAndRegisterComponentFromNamespaceUri(resourcePath);

    		String configurationLoaderBean = getExtensionBeanNameFromComponent(component);

    		ConfigurationLoader loader = (ConfigurationLoader) Context.getApplicationContext().getBean(configurationLoaderBean);
    		loader.loadAndRegisterComponentConfiguration(this, obj);

    		Component.Extension extension = component.getExtensionByExtensionInterface(ExtensionInterface.IMPLEMENTATION);
    		if (extension == null) {
    			log.error("Encountered a custom matching component with no implementation extension: " + component);
    			throw new InitializationException("Unable to locate an implementation component for custom matching component " + component.getName());
    		}
    		log.debug("Registering implementation of matching component named " + extension.getName() + " and implementation key " +
    				extension.getImplementationKey());

            String entity = loader.getComponentEntity();

    		MatchingService matchingService = (MatchingService)
    			Context.getApplicationContext().getBean(extension.getImplementationKey());
            log.info("Registering matching service " + matchingService + " for entity " + entity);
    		Context.registerCustomMatchingService(entity, matchingService);
        }
	}

    private void processShallowMatchConfiguration(MpiConfigDocument configuration) {
        if (configuration.getMpiConfig().getShallowMatchingConfigurationArray() == null) {
            return;
        }

        int count = configuration.getMpiConfig().getShallowMatchingConfigurationArray().length;
        for (int i = 0; i < count; i++) {
            ShallowMatchingConfigurationType obj = configuration.getMpiConfig().getShallowMatchingConfigurationArray(i);
            if (obj == null) {
                log.warn("No shallow matching service configuration has been specified.");
                return;
            }
            log.debug("Object is of type: " + obj.getDomNode().getNamespaceURI());
            String namespaceUriStr = obj.getDomNode().getNamespaceURI();
            URI namespaceURI = getNamespaceURI(namespaceUriStr);

            String resourcePath = generateComponentResourcePath(namespaceURI);
            Component component = loadAndRegisterComponentFromNamespaceUri(resourcePath);

            String configurationLoaderBean = getExtensionBeanNameFromComponent(component);

            ConfigurationLoader loader = (ConfigurationLoader) Context.getApplicationContext().getBean(configurationLoaderBean);
            loader.loadAndRegisterComponentConfiguration(this, obj);

            Component.Extension extension = component.getExtensionByExtensionInterface(ExtensionInterface.IMPLEMENTATION);
            if (extension == null) {
                log.error("Encountered a custom matching component with no implementation extension: " + component);
                throw new InitializationException("Unable to locate an implementation component for custom matching component " + component.getName());
            }
            log.debug("Registering implementation of matching component named " + extension.getName() + " and implementation key " +
                    extension.getImplementationKey());

            String entity = loader.getComponentEntity();

            ShallowMatchingService matchingService = (ShallowMatchingService)
                Context.getApplicationContext().getBean(extension.getImplementationKey());
            log.info("Registering shallow matching service " + matchingService + " for entity " + entity);
            Context.registerCustomShallowMatchingService(entity, matchingService);
        }
    }

	public String getExtensionBeanNameFromComponent(Component component) {
		Component.Extension extension = component.getExtensionByExtensionInterface(ExtensionInterface.CONFIGURATION_LOADER);
		if (extension == null) {
			log.error("Encountered a custom component with no configuration loader extension: " + component);
			throw new InitializationException("Unable to load configuration for custom component " + component.getName());
		}
		return extension.getImplementationKey();
	}

	public Component lookupExtensionComponentByComponentType(Component.ComponentType type) {
		if (type == null || type.componentTypeName() == null || type.componentTypeName().length() == 0) {
			log.warn("Looked up extension component with blank type name: " + type);
			return null;
		}
		
		log.debug("Looking up extension component of type: " + type.componentTypeName());
		return extensionRegistry.get(type.componentTypeName());
	}
	
	private Component loadAndRegisterComponentFromNamespaceUri(String resourcePath) {
		Component component;
		try {
			InputStream stream = Configuration.class.getResourceAsStream(resourcePath);
			MpiComponentDefinitionDocument componentDoc = MpiComponentDefinitionDocument.Factory.parse(stream);
			MpiComponentType componentXml = componentDoc.getMpiComponentDefinition().getMpiComponent();
			component = buildComponentFromXml(componentXml);
			log.debug("Loaded component: " + component);
			extensionRegistry.put(component.getComponentType().componentTypeName(), component);
			return component;
		} catch (IOException e) {
			log.error("Failed while loading component configuration file: " + resourcePath, e);
			throw new InitializationException("Failed while loading component configuration file " + resourcePath);
		} catch (XmlException e) {
			log.error("Failed while parsing component configuration file: " + resourcePath, e);
			throw new InitializationException("Failed while parsing component configuration file " + resourcePath);
		}
	}

	private Component buildComponentFromXml(MpiComponentType componentXml) {
		Component component = new Component(componentXml.getName());
		if (componentXml.getDescription() != null) {
			component.setDescription(componentXml.getDescription());
		}
		
		if (componentXml.getComponentType().intValue() == MpiComponentType.ComponentType.INT_BLOCKING) {
			component.setComponentType(ComponentType.BLOCKING);
		} else if (componentXml.getComponentType().intValue() == MpiComponentType.ComponentType.INT_MATCHING) {
			component.setComponentType(ComponentType.MATCHING);
		} else if (componentXml.getComponentType().intValue() == MpiComponentType.ComponentType.INT_FILELOADER) {
			component.setComponentType(ComponentType.FILELOADER);
		}

		log.debug("Component configuration: " + component.getName() + " of type " + component.getComponentType());
		for (int i=0; i < componentXml.getExtensions().sizeOfExtensionArray(); i++) {
			ExtensionType extension = componentXml.getExtensions().getExtensionArray(i);
			log.debug("Extension definition is " + extension);
			component.addExtension(extension.getName(), extension.getImplementation(), getExtensionInterfaceTypeById(extension.getInterface()));
		}
		return component;
	}

	private ExtensionInterface getExtensionInterfaceTypeById(Enum extensionInterface) {
		if (extensionInterface.intValue() == ExtensionType.Interface.INT_CONFIGURATION_LOADER) {
			return ExtensionInterface.CONFIGURATION_LOADER;
		} else if (extensionInterface.intValue() == ExtensionType.Interface.INT_CONFIGURATION_GUI) {
			return ExtensionInterface.CONFIGURATION_GUI;
		} else if (extensionInterface.intValue() == ExtensionType.Interface.INT_IMPLEMENTATION) {
			return ExtensionInterface.IMPLEMENTATION;
		}
		log.error("Unknown extension interface type encountered: " + extensionInterface);
		throw new RuntimeException("Unknown extension interface type encountered: " + extensionInterface);
	}

	private String generateComponentResourcePath(URI namespaceURI) {
        String resourcePath = "/META-INF" + namespaceURI.getPath() + "-openempi.xml";
//        File resourceFile = new File(resourcePath);
//        if (!resourceFile.exists() || !resourceFile.canRead()) {
//        	log.error("Unable to load component configuration file: " + resourcePath);
//        	throw new RuntimeException("Component configuration file " + resourcePath + " must be readable and present in the classpath");
//        }
//        String baseURI = resourceFile.getParent().replace('\\', '/');
        log.debug("Will locate configuration information for namespace from: " + resourcePath);
        return resourcePath;
	}

	private URI getNamespaceURI(String namespaceUriStr) {
		log.debug("Generating namespace URI for namespace " + namespaceUriStr);
		try {
			URI namespaceURI = new URI(namespaceUriStr);
			return namespaceURI;
		} catch (URISyntaxException e) {
			log.error("Failed to construct a namespace URI for namespace " + namespaceUriStr, e);
			throw new InitializationException("Unable to parse extended config namespace URI '" + namespaceUriStr + "'.", e);
		}
	}

	private void checkConfiguration(MpiConfigDocument configuration) {
		if (configuration == null) {
			log.error("The configuration of the system has not been initialized.");
			throw new RuntimeException("The configuration of the system has not been properly initialized.");
		}
	}
	
	private void validateConfiguration(MpiConfigDocument configuration) {

		// Set up the validation error listener.
		ArrayList<XmlError> validationErrors = new ArrayList<XmlError>();
		XmlOptions validationOptions = new XmlOptions();
		validationOptions.setErrorListener(validationErrors);

		// During validation, errors are added to the ArrayList for
		// retrieval and printing by the printErrors method.
		boolean isValid = configuration.validate(validationOptions);

		// Print the errors if the XML is invalid.
		if (!isValid)
		{
		    java.util.Iterator<XmlError> iter = validationErrors.iterator();
		    StringBuffer sb = new StringBuffer("MPI Configuration validation errors:\n");
		    while (iter.hasNext())
		    {
		    	sb.append(">> ").append(iter.next()).append("\n");
		    }
		}			
	}
	
	private MpiConfigDocument loadConfigurationFromSource() throws XmlException, IOException {
		File file = getDefaultConfigurationFile();
		log.debug("Checking for presence of the configuration in file: " + file.getAbsolutePath());
		if (file.exists() && file.isFile()) {
			log.info("Loading configuration from file: " + file.getAbsolutePath());
			return MpiConfigDocument.Factory.parse(file);
		}

		URL fileUrl = Configuration.class.getResource(configFile);
		if (fileUrl != null) {
			log.info("Loading configuration from URL: " + fileUrl);
			return MpiConfigDocument.Factory.parse(fileUrl);
		}
		
		log.error("Unable to load configuration information.");
		throw new RuntimeException("Unable to load configuration information.");
	}

	public void saveConfiguration() {
		File file = getDefaultConfigurationFile();
		log.info("Storing current configuration in file: " + file.getAbsolutePath());
		try {
			XmlOptions opts = new XmlOptions();
			opts.setSavePrettyPrint();
			opts.setSavePrettyPrintIndent(4);
			configuration.save(file, opts);
		} catch (IOException e) {
			log.error("Unable to save the updated configuration in file: " + file.getAbsolutePath());
			throw new RuntimeException("Unable to save the updated configuration: " + e.getMessage());
		}
	}

	private File getDefaultConfigurationFile() {
		File dir = new File(Context.getOpenEmpiHome() + "/conf");
		log.info("OPENEMPI_HOME = " + System.getProperty("OPENEMPI_HOME"));
		File file = new File(dir, getConfigurationFilename());
		return file;
	}
	
	private String getConfigurationFilename() {
		String filename = System.getProperty(Constants.OPENEMPI_CONFIGURATION_FILENAME);
		if (filename != null) {
			return filename;
		}
		return configFile;
	}
	
	public Object lookupConfigurationEntry(String entityName, String key) {
	    if (entityName == null) {
	        log.info("Looking up configuration entry with key " + key + " in default registry.");
	        return defaultConfigurationRegistry.get(key);	        
	    }
	    return Context.lookupConfigurationEntry(entityName, key);
	}
    
    public void registerConfigurationEntry(String entityName, String key, Object entry) {
        if (entityName == null) {
            log.info("Registering configuration entry " + entry + " with key " + key +
                    " in the default registry.");
            defaultConfigurationRegistry.put(key, entry);
        } else {
            Context.registerConfigurationEntry(entityName, key, entry);
        }
    }

	public void registerConfigurationLoader(ComponentType type, String entityName, ConfigurationLoader loader) {
	    log.info("Registering configuration loader " + loader + " of component type " + type + 
	            " for entity " + entityName);
	    String key = generateLoaderKey(type, entityName);
	    loaderByEntity.put(key,  loader);	    
	}

	public ConfigurationLoader lookupConfigurationLoader(ComponentType type, String entityName) {
        log.info("Looking up configuration loader of component type " + type + 
                " for entity " + entityName);
        String key = generateLoaderKey(type, entityName);
        ConfigurationLoader loader = loaderByEntity.get(key);
        log.info("Found configuration loader " + loader + " of component type " + type + 
                " for entity " + entityName);
	    return loader;
	}
	
    private String generateLoaderKey(ComponentType type, String entityName) {
        String key = type.componentTypeName() + "-" + entityName;
        return key;
    }
	
	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public AdminConfiguration getAdminConfiguration() {
		return adminConfiguration;
	}

	public void setAdminConfiguration(AdminConfiguration adminConfiguration) {
		this.adminConfiguration = adminConfiguration;
	}

	public GlobalIdentifier getGlobalIdentifier() {
		return globalIdentifier;
	}
}
