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
package org.openhie.openempi.jobqueue.impl;

import java.util.Map;

import org.openhie.openempi.context.Context;
import org.openhie.openempi.jobqueue.JobParameterConstants;
import org.openhie.openempi.model.JobEntry;
import org.openhie.openempi.model.JobEntryEventLog;
import org.openhie.openempi.model.JobStatus;
import org.openhie.openempi.profiling.DataProfiler;
import org.openhie.openempi.profiling.FileRecordDataSource;
import org.openhie.openempi.util.ConvertUtil;

public class DataProfilingJobTypeHandler extends AbstractJobTypeHandler
{

    public DataProfilingJobTypeHandler() {
        super();
    }

    public void run() {
        authenticateUser();
        JobEntry jobEntry = getJobEntry();
        Map<String,Object> params = extractParameters(jobEntry);
        logJobEntryParameters(params);
        String fileName = (String) params.get(JobParameterConstants.FILENAME_PARAM);
        if (ConvertUtil.isNullOrEmpty(fileName)) {
            log.warn("Unable to process the data profiling job with id " + jobEntry.getJobEntryId() + 
                    " since the name of the file to be profiled has not been specified.");
            return;
        }
        String userFileIdStr = (String) params.get(JobParameterConstants.USERFILEID_PARAM);
        if (ConvertUtil.isNullOrEmpty(userFileIdStr)) {
            log.warn("Unable to process the data profiling job with id " + jobEntry.getJobEntryId() + 
                    " since the user file entry has not been specified.");
            return;
        }
        Integer userFileId = Integer.parseInt(userFileIdStr);
        
		DataProfiler dataProfiler = (DataProfiler) Context.getApplicationContext().getBean("dataProfiler");
		if (dataProfiler == null) {
			log.warn("The data profiler has not been configured properly through the Spring configuration file.");
			throw new RuntimeException("The Data Profiler has not been configured properly. Please check with your system administrator.");
		}
		
		try {
			FileRecordDataSource fileRecordDataSource = new FileRecordDataSource(fileName, userFileId);
			dataProfiler.setRecordDataSource(fileRecordDataSource);
			dataProfiler.run();
	        updateJobEntry(true, "Successfully completed the data profile of file: " + fileName);
	        updateUserFileEntry(userFileId);
	    } catch (Exception e) {
	        log.warn("Failed while profiling the data in a file: " + e, e);
	        updateJobEntry(false, "Failed while profiling the data in the file due to: " + e.getMessage());
	    }
    }

    private void updateUserFileEntry(Integer userFileId) {
        org.openhie.openempi.model.UserFile userFileFound = Context.getUserManager().getUserFile(userFileId);
        if (userFileFound == null) {
            log.warn("The user file cannot be found in the system so, we can't update the status.");
            return;
        }
        try {
          userFileFound.setProfiled("Y");
          userFileFound.setProfileProcessed("Completed.");
          userFileFound.setRowsImported(0);
          userFileFound.setRowsProcessed(0);
          Context.getUserManager().saveUserFile(userFileFound);
        } catch (Exception e) {
            log.warn("Failed to update the user file with the results of a successful import: " + e);
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
