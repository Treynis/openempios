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
package org.openhie.openempi.loader.concurrent;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.context.UserContext;
import org.openhie.openempi.loader.EntityLoaderManager;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;

public class RecordLoaderTask implements Runnable
{
	private Logger log = Logger.getLogger(getClass());
	protected EntityLoaderManager entityLoaderManager;
	private UserContext userContext;
	private Entity entity;
	private Record record;
	private Serializable key;
	
	public RecordLoaderTask(EntityLoaderManager entityLoaderManager, Entity entity, Record record, UserContext userContext) {
		this.entityLoaderManager = entityLoaderManager;
		this.entity = entity;
		this.record = record;
		this.userContext = userContext;
	}
	
	public void run() {
		log.debug("With User Context: " + userContext + " attempting to load entry record" + record);
		Context.setUserContext(userContext);
		try {
			synchronized(userContext) {
				Record theRecord = entityLoaderManager.addRecord(entity, record);
				if (key != null) {
//					generateKnownLinks(key, record);
				}
			}
		} catch (Exception e) {
			log.error("Failed while adding entity entry to the system. Error: " + e, e);
			if (e.getCause() instanceof org.hibernate.exception.SQLGrammarException) {
				org.hibernate.exception.SQLGrammarException sge = (org.hibernate.exception.SQLGrammarException) e;
				log.error("Cause is: " + sge.getSQL());
			}
		}
	}

	public Serializable getKey() {
		return key;
	}

	public void setKey(Serializable key) {
		this.key = key;
	}
}