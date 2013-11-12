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
package org.openhie.openempi.entity.dao.orientdb;

import org.apache.log4j.Logger;
import org.openhie.openempi.entity.DataAccessIntent;
import org.openhie.openempi.model.Entity;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;

public class IntentMassiveInsertImpl implements DataAccessIntent
{
    private Logger log = Logger.getLogger(getClass());
    private EntityDaoOrientdb dao;
    private Entity entity;
    
    public IntentMassiveInsertImpl(EntityDaoOrientdb dao) {
        this.dao = dao;
    }
    
    @Override
    public void begin(Entity entity, DataAccessIntent param) {
        OGraphDatabase db = getConnection(entity);
        if (db == null) {
            return;
        }
        db.declareIntent(new OIntentMassiveInsert());
        this.entity = entity;
        log.warn("Removing all indexes.");
        dao.getSchemaManager(entity).removeIndexes(entity, db);
        db.close();
    }

    @Override
    public void end() {
        OGraphDatabase db = getConnection(entity);
        if (db == null) {
            return;
        }
        db.declareIntent(null);
        log.warn("Recreating all indexes.");
        dao.getSchemaManager(entity).createIndexes(entity, db);
        db.close();
    }

    private OGraphDatabase getConnection(Entity entity) {
        OGraphDatabase db = dao.getConnection(entity);
        if (db == null) {
            log.warn("Unable to implement massive insert intent due to no connection.");
            return null;
        }
        return db;
    }
}
