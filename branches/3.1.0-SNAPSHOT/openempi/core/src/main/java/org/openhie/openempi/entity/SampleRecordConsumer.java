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
package org.openhie.openempi.entity;

import java.util.concurrent.TimeUnit;

import org.openhie.openempi.entity.impl.AbstractRecordConsumer;
import org.openhie.openempi.model.Record;

public class SampleRecordConsumer extends AbstractRecordConsumer
{
	@Override
	public void run() {
		int count = 0;
		boolean done = false;
		try {
			while (!done) {
				Record record = getQueue().poll(5, TimeUnit.SECONDS);
				if (record != null) {
					count++;
					if (count % 10000 == 0) {
						System.out.println("Consumer " + Thread.currentThread().getName() + " processed " + count + " records.");
					}
				} else {
					done = true;
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Finished the while loop.");
			e.printStackTrace();
		} finally {
			getLatch().countDown();
		}
	}
}
