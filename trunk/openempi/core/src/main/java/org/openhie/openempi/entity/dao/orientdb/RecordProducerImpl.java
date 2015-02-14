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

import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.entity.impl.AbstractRecordProducer;

public class RecordProducerImpl extends AbstractRecordProducer
{
    private EntityDao entityDao;
    private int blockSize;

    public RecordProducerImpl() {

    }

    @Override
    public void run() {
        entityDao.loadRecords(getEntity(), getQueueList(), getBlockSize());
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public EntityDao getEntityDao() {
        return entityDao;
    }

    public void setEntityDao(EntityDao entityDao) {
        this.entityDao = entityDao;
    }
}
