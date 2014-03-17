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

import java.util.Date;
import java.util.List;

import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.JobEntry;
import org.openhie.openempi.model.JobEntryEventLog;
import org.openhie.openempi.model.JobStatus;
import org.openhie.openempi.model.JobType;


public class JobQueueServiceTest extends BaseServiceTestCase
{

	public void testCreateJobEntry() {
	    JobQueueService jobQuereService = Context.getJobQueueService();
		try {
	        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
            List<Entity> entities = defService.findEntitiesByName("person");
            assertTrue("Not found entities.", entities.size() > 0);

            Entity entity = entities.get(0);

            JobType jobType = new JobType();
            jobType.setJobTypeCd(1);

            JobStatus jobStatus = new JobStatus();
            jobStatus.setJobStatusCd(2);

            JobEntry jobEntry = createJobEntry(entity, jobType, jobStatus, "test job");
		    jobEntry = jobQuereService.createJobEntry(jobEntry);
            assertNotNull(jobEntry);

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

    public void testGetJobEntries() {
        JobQueueService jobQuereService = Context.getJobQueueService();
        try {

            // get job entries
            List<JobEntry> entries = jobQuereService.getJobEntries();

            assertTrue("Not found job entries.", entries.size() > 0);

            JobEntry jobEntry = entries.get(0);

            // get a job entry
            jobEntry = jobQuereService.getJobEntry(jobEntry);

            assertNotNull(jobEntry);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void testlogJobEntryEvent() {
        JobQueueService jobQuereService = Context.getJobQueueService();
        try {

            // get job entries
            List<JobEntry> entries = jobQuereService.getJobEntries();

            assertTrue("Not found job entries.", entries.size() > 0);

            JobEntry jobEntry = entries.get(0);

            JobEntryEventLog eventLog = new JobEntryEventLog();
            eventLog.setDateCreated(new Date());
            eventLog.setLogMessage("test log message");

            jobQuereService.logJobEntryEvent(jobEntry, eventLog);


            // get job entry with Event Log
            jobEntry = jobQuereService.getJobEntry(jobEntry);
            assertNotNull(jobEntry);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    public void testUpdateJobEntry() {
        JobQueueService jobQuereService = Context.getJobQueueService();
        try {

            // get job entries
            List<JobEntry> entries = jobQuereService.getJobEntries();

            assertTrue("Not found job entries.", entries.size() > 0);

            JobEntry jobEntry = entries.get(0);

            Date currentDate = new Date();
            jobEntry.setDateCompleted(currentDate);


            jobEntry = jobQuereService.updateJobEntry(jobEntry);

            assertNotNull(jobEntry);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    public void testDeleteJobEntry() {
        JobQueueService jobQuereService = Context.getJobQueueService();
        try {

            // get job entries
            List<JobEntry> entries = jobQuereService.getJobEntries();

            assertTrue("Not found job entries.", entries.size() > 0);

            for (JobEntry entry : entries) {
                jobQuereService.deleteJobEntry(entry);
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    private JobEntry createJobEntry(Entity entity, JobType jobType, JobStatus jobStatus, String description) {
        JobEntry jobEntry;
        jobEntry = new JobEntry();
        jobEntry.setDateCreated(new Date());
        jobEntry.setDateStarted(new Date());
        jobEntry.setEntity(entity);
        jobEntry.setJobType(jobType);
        jobEntry.setJobStatus(jobStatus);
        jobEntry.setJobDescription(description);

        return jobEntry;
    }
}
