package org.openhie.openempi.jobqueue.impl;

import org.openhie.openempi.jobqueue.dao.JobEntryDao;
import org.openhie.openempi.model.JobEntry;

public interface JobTypeHandler extends Runnable
{
    public void setJobEntry(JobEntry jobEntry);

    public void setJobEntryDao(JobEntryDao jobEntryDao);
}
