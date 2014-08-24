package org.openhie.openempi.matching;

import java.util.Set;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordPair;

public interface ShallowMatchingService
{
    /**
     * The getMatchingServiceId method returns a unique identifier for the matching algorithm.
     * The purpose of this identifier is to allow a site to identify which matching algorithm
     * was responsible for each of the links between records in the repository. 
     * 
     * @return The unique identifier of the particular Matching Algorithm
     */
    public int getMatchingServiceId();

    /**
     * This match method takes a record as a parameter and returns all the records that the
     * given record is linked to by returning them in the form of record pairs. The first record
     * in each record pair returned is the record passed into the call.
     * 
     * @param record
     * @return
     */
    public Set<RecordPair> match(Record record) throws ApplicationException;    
}
