package org.openhie.openempi.matching.fellegisunter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.openhie.openempi.blocking.RecordPairIterator;
import org.openhie.openempi.blocking.RecordPairSource;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.matching.SamplingService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.RecordPair;
import org.openhie.openempi.service.impl.BaseServiceImpl;

public class BinarySamplingServiceImpl extends BaseServiceImpl implements SamplingService
{
    private double selectionProbability;
    private Random random;
    
    public BinarySamplingServiceImpl() {
        random = new Random(new Date().getTime());
    }
    
    @Override
    public List<RecordPair> getRecordPairs(Entity entity) {
        RecordPairSource source = Context.getBlockingService(entity.getName()).getRecordPairSource(entity);
        List<RecordPair> pairs = new ArrayList<RecordPair>();
        int pairCount = 0, totalCount = 0;
        for (RecordPairIterator iter = source.iterator(); iter.hasNext();) {
            double randomValue = random.nextDouble();
            totalCount++;
            if (randomValue < selectionProbability) {
                pairs.add(iter.next());
                pairCount++;
                if (pairCount % 10000 == 0) {
                    log.info("Binary Sampling Service Loaded " + pairCount + " pairs.");
                }
            }
        }
        log.info("The sampling algorithm selected " + pairCount + " out of a total of " + totalCount + " pairs.");
        return pairs;
    }

    public double getSelectionProbability() {
        return selectionProbability;
    }

    public void setSelectionProbability(double selectionProbability) {
        this.selectionProbability = selectionProbability;
    }
}
