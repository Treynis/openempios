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
