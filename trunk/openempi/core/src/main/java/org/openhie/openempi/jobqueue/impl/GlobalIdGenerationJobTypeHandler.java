package org.openhie.openempi.jobqueue.impl;

import java.util.Map;

import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.JobEntry;
import org.openhie.openempi.model.JobEntryEventLog;
import org.openhie.openempi.model.JobStatus;

public class GlobalIdGenerationJobTypeHandler extends AbstractJobTypeHandler
{

    public GlobalIdGenerationJobTypeHandler() {
        super();
    }

    public void run() {
        authenticateUser();
        JobEntry jobEntry = getJobEntry();
        Map<String,Object> params = extractParameters(jobEntry);
        logJobEntryParameters(params);        
        try {
            Entity entity = jobEntry.getEntity();
            Context.getRecordManagerService().assignGlobalIdentifier(entity);
            updateJobEntry(true, "Successfully generated global identifiers for all records.");
        } catch (Exception e) {
            log.warn("Failed while generating global identifiers: " + e, e);
            updateJobEntry(false, "Failed to generate global identifiers due to: " + e.getMessage());
        }
    }
    
    public void updateJobEntry(boolean success, String message) {
        java.util.Date completed = new java.util.Date();
        JobEntryEventLog event = new JobEntryEventLog();
        event.setDateCreated(completed);
        event.setLogMessage(message);

        JobEntry jobEntry = getJobEntry();
        jobEntry.setDateCompleted(completed);
        jobEntry.setItemsErrored(0);
        jobEntry.setItemsProcessed(0);
        jobEntry.setItemsSuccessful(0);
        jobEntry.setJobStatus(JobStatus.JOB_STATUS_COMPLETED);
        JobEntry updatedJob = getJobEntryDao().updateJobEntry(jobEntry);        
        getJobEntryDao().logJobEntryEvent(updatedJob,  event);
    }

}
