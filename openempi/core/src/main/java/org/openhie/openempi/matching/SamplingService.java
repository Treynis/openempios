package org.openhie.openempi.matching;

import java.util.List;

import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.RecordPair;

public interface SamplingService
{
    public List<RecordPair> getRecordPairs(Entity entity);
}
