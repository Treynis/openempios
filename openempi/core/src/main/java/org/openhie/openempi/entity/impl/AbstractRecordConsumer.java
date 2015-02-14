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
package org.openhie.openempi.entity.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import org.openhie.openempi.entity.RecordConsumer;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;

public abstract class AbstractRecordConsumer implements RecordConsumer
{
	private BlockingQueue<Record> queue;
	private CountDownLatch latch;
	private Entity entity;
	
	public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public BlockingQueue<Record> getQueue() {
		return queue;
	}

	public void setQueue(BlockingQueue<Record> queue) {
		this.queue = queue;
	}

	public CountDownLatch getLatch() {
		return latch;
	}

	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}
}
