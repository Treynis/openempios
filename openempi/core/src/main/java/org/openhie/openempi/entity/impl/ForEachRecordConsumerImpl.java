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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.openhie.openempi.entity.ForEachRecordConsumer;
import org.openhie.openempi.entity.RecordConsumer;
import org.openhie.openempi.entity.RecordProducer;
import org.openhie.openempi.model.Record;

public class ForEachRecordConsumerImpl implements ForEachRecordConsumer
{
	private RecordConsumer[] children;
	private CountDownLatch latch;
	private List<BlockingQueue<Record>> queueList = new ArrayList<BlockingQueue<Record>>();
	private int queueCapacity = 1000;
	private boolean queuePerConsumer;
	private RecordProducer producer;
	
	public ForEachRecordConsumerImpl() {
	}
	
	public void startProcess(Set<RecordConsumer> consumers, boolean queuePerConsumer) {
		initialize(consumers, queuePerConsumer);
		createQueues();
		for (int i=0; i < children.length; i++) {
			children[i].setQueue(getQueue(i));
			children[i].setLatch(latch);
		}
		producer.setQueueList(queueList);
		Thread producerThread = new Thread(producer);
		producerThread.start();
		
		Thread[] threads = new Thread[children.length];
		for (int i=0; i < children.length; i++) {
			threads[i] = new Thread(children[i]);
			threads[i].start();
		}
		
		try {
			producerThread.join();
			System.out.println("Producer finished its work.");
			latch.await();
			System.out.println("All consumers finished their work.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void initialize(Set<RecordConsumer> consumers, boolean queuePerConsumer) {
        this.children = consumers.toArray(new RecordConsumer[]{});
        this.latch = new CountDownLatch(consumers.size());
        this.queuePerConsumer = queuePerConsumer;
        RecordConsumer consumer = consumers.iterator().next();
        producer.setEntity(consumer.getEntity());
    }

    private BlockingQueue<Record> getQueue(int consumerIndex) {
		if (queuePerConsumer) {
			return queueList.get(consumerIndex);
		}
		return queueList.get(0);
	}

	private void createQueues() {
		if (queuePerConsumer) {
			for (int i=0; i < children.length; i++) {
				queueList.add(new LinkedBlockingQueue<Record>(getQueueCapacity()));
			}
		} else {
			queueList.add(new LinkedBlockingQueue<Record>(getQueueCapacity()));
		}
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}
	
	public RecordProducer getRecordProducer() {
        return producer;
    }

    public void setRecordProducer(RecordProducer producer) {
        this.producer = producer;
    }

//    public static void main(String[] args) {
//		Set<AbstractRecordConsumer> consumers = new HashSet<AbstractRecordConsumer>();
//		consumers.add(new SampleRecordConsumer());
//		consumers.add(new SampleRecordConsumer());
//		ForEachRecordConsumer process = new ForEachRecordConsumer(consumers, true);
//		process.startProcess();
//	}	
}
