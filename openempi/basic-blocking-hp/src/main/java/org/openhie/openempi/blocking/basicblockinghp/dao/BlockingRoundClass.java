package org.openhie.openempi.blocking.basicblockinghp.dao;

import org.openhie.openempi.configuration.BlockingRound;
import org.openhie.openempi.model.Entity;

public class BlockingRoundClass
{
    private BlockingRound blockingRound;
    private Entity roundClass;

    public BlockingRoundClass(BlockingRound blockingRound, Entity roundClass) {
        this.blockingRound = blockingRound;
        this.roundClass = roundClass;
    }

    public BlockingRound getBlockingRound() {
        return blockingRound;
    }

    public void setBlockingRound(BlockingRound blockingRound) {
        this.blockingRound = blockingRound;
    }

    public Entity getRoundClass() {
        return roundClass;
    }

    public void setRoundClass(Entity roundClass) {
        this.roundClass = roundClass;
    }

    public String toString() {
        return "BlockingRoundClass [blockingRound=" + blockingRound + ", roundClass=" + roundClass + "]";
    }
}
