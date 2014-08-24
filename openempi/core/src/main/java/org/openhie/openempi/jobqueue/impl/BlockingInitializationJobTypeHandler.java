package org.openhie.openempi.jobqueue.impl;

import java.util.Map;

import org.openhie.openempi.blocking.BlockingLifecycleObserver;
import org.openhie.openempi.blocking.BlockingService;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.JobEntry;
import org.openhie.openempi.model.JobEntryEventLog;
import org.openhie.openempi.model.JobStatus;

public class BlockingInitializationJobTypeHandler extends AbstractJobTypeHandler
{

    public BlockingInitializationJobTypeHandler() {
        super();
    }

    public void run() {
        authenticateUser();
        Map<String,Object> params = extractParameters(getJobEntry());
        logJobEntryParameters(params);
        try {
            JobEntry jobEntry = getJobEntry();
            Entity entity = jobEntry.getEntity();
            BlockingService blockingService = Context.getBlockingService(entity.getName());
            BlockingLifecycleObserver blockingLifecycle = (BlockingLifecycleObserver) blockingService;
            blockingLifecycle.rebuildIndex();
            updateJobEntry(true, null);
        } catch (Exception e) {
            log.warn("Failed while trying to initialize the blocking service: " + e, e);
            updateJobEntry(false, e.getMessage());
        }
    }
    
    public void updateJobEntry(boolean success, String message) {
        java.util.Date completed = new java.util.Date();
        JobEntryEventLog event = new JobEntryEventLog();
        event.setDateCreated(completed);
        if (!success) {
            event.setLogMessage("Failed to initialize the blocking service indexes due to: " + message);
        } else {
            event.setLogMessage("Successfully completed initalizing the blocking service indexes.");
        }
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
