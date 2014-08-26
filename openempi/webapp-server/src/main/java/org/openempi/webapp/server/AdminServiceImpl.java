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
package org.openempi.webapp.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.openempi.webapp.client.AdminService;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.server.util.ModelTransformer;
import org.openhealthtools.openpixpdq.common.PixPdqConfigurationLoader;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.jobqueue.JobEntryFactory;
import org.openhie.openempi.jobqueue.JobParameterConstants;
import org.openhie.openempi.jobqueue.JobTypeEnum;
import org.openhie.openempi.model.JobEntry;
import org.openhie.openempi.notification.ObservationEventType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class AdminServiceImpl extends AbstractRemoteServiceServlet implements AdminService
{
    private static final long serialVersionUID = 2707492638994310226L;
    
	private static boolean initialized = false;
	private PixPdqConfigurationLoader loader;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if (!Context.isInitialized()) {
			log.error("The context did not initialize properly.");
			return;
		}
		if (Context.getConfiguration().getAdminConfiguration().isAutoStartPIXPDQ()) {
			startPixPdqServerLocal();
		}
	}

	@Override
	public void destroy() {
		stopPixPdqServerLocal();
		super.destroy();
	}

	public String startPixPdqServer() {
		log.debug("Starting the PIX/PDQ Service");
		authenticateCaller();
		String message = startPixPdqServerLocal();
		return message;
	}

	private String startPixPdqServerLocal() {
		String message = null;
		loader = PixPdqConfigurationLoader.getInstance();
		loader.destroyAllActors();
		try {
			String[] propertyFiles = {"openpixpdq.properties"};
			loader.loadProperties(propertyFiles);
			File configurationFile = getConfigurationFile();
			log.debug("Using as configuration the file " + configurationFile.getAbsolutePath());
			loader.loadConfiguration(configurationFile.getAbsolutePath(), true);
			setInitialized(true);
		} catch (Exception e) {
			log.error("Unable to start the PIX/PDQ server due to : " + e, e);
			message = e.getMessage();
		}
		return message;
	}

	public String assignGlobalIdentifiers(EntityWeb entityWeb) {
		log.debug("Assign global identifiers for person entries without one.");
		
        authenticateCaller();
        org.openhie.openempi.model.Entity entity = ModelTransformer.mapToEntity(entityWeb,
                org.openhie.openempi.model.Entity.class);
        JobEntry jobEntry = JobEntryFactory.createJobEntry(entity, JobTypeEnum.ASSIGN_GLOBAL_IDENTIFIERS,
                "Job to assign global identifiers to all records for entity " + entity.getName()  + 
                " on behalf of user " + Context.getUserContext().getUser().getUsername());
        jobEntry.addJobParameter(JobParameterConstants.ENTITYNAME_PARAM, entity.getName());
        String msg = "Job to assign global identifiers to all records has been created.";
        try {
            jobEntry = Context.getJobQueueService().createJobEntry(jobEntry);
            Context.notifyObserver(ObservationEventType.JOB_QUEUED_EVENT, jobEntry);
        } catch (Throwable t) {
            log.error("Failed while trying to assign global identifiers to entries in the repository: " + t, t);
            msg = t.getMessage();
        }
        return msg;
	}

    public String createEntityIndexes(EntityWeb entityWeb) {
        log.debug("Create all the indexes for entity: " + entityWeb.getDisplayName());
        
        authenticateCaller();
        org.openhie.openempi.model.Entity entity = ModelTransformer.mapToEntity(entityWeb,
                org.openhie.openempi.model.Entity.class);
        String msg = "Succeeded in creating indexes for entity " + entityWeb.getDisplayName();
        try {
            Context.getEntityDefinitionManagerService().createEntityIndexes(entity.getEntityVersionId());
        } catch (Throwable t) {
            log.error("Failed while trying to create indexes for an entity: " + t, t);
            msg = t.getMessage();
        }
        return msg;
    }

    public String dropEntityIndexes(EntityWeb entityWeb) {
        log.debug("Drop all the indexes for entity: " + entityWeb.getDisplayName());
        
        authenticateCaller();
        org.openhie.openempi.model.Entity entity = ModelTransformer.mapToEntity(entityWeb,
                org.openhie.openempi.model.Entity.class);
        String msg = "Succeeded in dropping indexes for entity " + entityWeb.getDisplayName();
        try {
            Context.getEntityDefinitionManagerService().dropEntityIndexes(entity.getEntityVersionId());
        } catch (Throwable t) {
            log.error("Failed while trying to drop indexes for an entity: " + t, t);
            msg = t.getMessage();
        }
        return msg;
    }
    
	private File getConfigurationFile() throws IOException {
		String openEmpiHome = Context.getOpenEmpiHome();
		if (openEmpiHome != null && openEmpiHome.length() > 0) {
			String filename = openEmpiHome + "/conf/IheActors.xml";
			log.info("Looking for PIX/PDQ configuration file at " + filename);
			File configFile = new File(filename);
			if (configFile != null && configFile.exists() && configFile.canRead()) {
				log.info("Using for PIX/PDQ configuration file at " + configFile.getAbsolutePath());
				return configFile;
			}
		}
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource resource = resolver.getResource("classpath:/IheActors.xml");
		File configurationFile = resource.getFile();
		log.info("Using for PIX/PDQ configuration file at " + configurationFile.getAbsolutePath());
		return configurationFile;
	}

	public String stopPixPdqServer() {
		log.debug("Stopping the PIX/PDQ Service");

		authenticateCaller();
		String message = stopPixPdqServerLocal();
		return message;
	}

	private String stopPixPdqServerLocal() {
		String message = null;
		if (loader != null) {
			loader.destroyAllActors();
		}
		return message;
	}

	public String initializeRepository(EntityWeb entityWeb) {
		log.debug("Initialize Repository");

		authenticateCaller();
        org.openhie.openempi.model.Entity entity = ModelTransformer.mapToEntity(entityWeb,
                org.openhie.openempi.model.Entity.class);
        JobEntry jobEntry = JobEntryFactory.createJobEntry(entity, JobTypeEnum.MATCHING_INITIALIZATION,
                "Job to initalize the matching service for entity " + entity.getName()  + 
                " on behalf of user " + Context.getUserContext().getUser().getUsername());
        jobEntry.addJobParameter(JobParameterConstants.ENTITYNAME_PARAM, entity.getName());
        jobEntry.addJobParameter(JobParameterConstants.MATCHINGTASK_PARAM, JobParameterConstants.MATCHINGTASK_INITIALIZATION);
        String msg = "Job to initialize the matching service has been created.";
        try {
            jobEntry = Context.getJobQueueService().createJobEntry(jobEntry);
            Context.notifyObserver(ObservationEventType.JOB_QUEUED_EVENT, jobEntry);
        } catch (Throwable t) {
            log.error("Failed while trying to initialize the matching service: " + t, t);
            msg = t.getMessage();
        }
        return msg;
	}

	public String linkAllRecordPairs(EntityWeb entityWeb) {
		log.debug("Linking all record pairs in the repository");

		authenticateCaller();
        org.openhie.openempi.model.Entity entity = ModelTransformer.mapToEntity(entityWeb,
                org.openhie.openempi.model.Entity.class);
        JobEntry jobEntry = JobEntryFactory.createJobEntry(entity, JobTypeEnum.MATCH_REPOSITORY_DATA,
                "Job to link all record pairs using the matching service for entity " + entity.getName()  + 
                " on behalf of user " + Context.getUserContext().getUser().getUsername());
        jobEntry.addJobParameter(JobParameterConstants.ENTITYNAME_PARAM, entity.getName());
        jobEntry.addJobParameter(JobParameterConstants.MATCHINGTASK_PARAM, JobParameterConstants.MATCHINGTASK_LINKAGE);
        String msg = "Job to link all record pairs using the matching service has been created.";
        try {
            jobEntry = Context.getJobQueueService().createJobEntry(jobEntry);
            Context.notifyObserver(ObservationEventType.JOB_QUEUED_EVENT, jobEntry);
        } catch (Throwable t) {
            log.error("Failed while trying to link all record pairs using the matching service: " + t, t);
            msg = t.getMessage();
        }
		return msg;
	}

	public String initializeCustomConfiguration(EntityWeb entityWeb) {
		log.debug("Initialize Custom Configuration");

		authenticateCaller();
        org.openhie.openempi.model.Entity entity = ModelTransformer.mapToEntity(entityWeb,
                org.openhie.openempi.model.Entity.class);
        JobEntry jobEntry = JobEntryFactory.createJobEntry(entity, JobTypeEnum.GENERATE_CUSTOM_FIELDS,
                "Job to generate custom fields for all records for entity " + entity.getName()
                        + " on behalf of user " + Context.getUserContext().getUser().getUsername());
        jobEntry.addJobParameter(JobParameterConstants.ENTITYNAME_PARAM, entity.getName());
        String msg = "Job to generate custom fields for all records has been created.";
        try {
            jobEntry = Context.getJobQueueService().createJobEntry(jobEntry);
            Context.notifyObserver(ObservationEventType.JOB_QUEUED_EVENT, jobEntry);
        } catch (Throwable t) {
            log.error("Failed while trying to generate custom fields: " + t, t);            
            msg = t.getMessage();
        }
		return msg;
	}

	public String rebuildBlockingIndex(EntityWeb entityWeb) {
		log.debug("Rebuild Blocking Index");

		authenticateCaller();
		org.openhie.openempi.model.Entity entity = ModelTransformer.mapToEntity(entityWeb,
                org.openhie.openempi.model.Entity.class);
        JobEntry jobEntry = JobEntryFactory.createJobEntry(entity, JobTypeEnum.BLOCKING_INITIALIZATION,
                "Job to initalize the indexes for blocking service for entity " + entity.getName()  + 
                " on behalf of user " + Context.getUserContext().getUser().getUsername());
        jobEntry.addJobParameter(JobParameterConstants.ENTITYNAME_PARAM, entity.getName());
        String msg = "Job to rebuild the blocking service indexes has been created.";
        try {
            jobEntry = Context.getJobQueueService().createJobEntry(jobEntry);
            Context.notifyObserver(ObservationEventType.JOB_QUEUED_EVENT, jobEntry);
        } catch (Throwable t) {
            log.error("Failed while trying to rebuild the indexes for the blocking service: " + t, t);            
            msg = t.getMessage();
        }
        return msg;
	}
    
	public static boolean isInitialized() {
		return initialized;
	}

	public static void setInitialized(boolean initialized) {
		AdminServiceImpl.initialized = initialized;
	}
}
