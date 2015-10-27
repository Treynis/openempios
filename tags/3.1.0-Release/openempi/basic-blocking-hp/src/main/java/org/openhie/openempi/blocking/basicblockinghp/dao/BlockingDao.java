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
/**
 *
5 *  Copyright (C) 2010 SYSNET International, Inc. <support@sysnetint.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
package org.openhie.openempi.blocking.basicblockinghp.dao;

import java.util.List;
import java.util.Set;

import org.openhie.openempi.configuration.BlockingRound;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;
 
public interface BlockingDao
{
	public Long getRecordPairCount(Entity entity, BlockingRound round);

	public List<Long> getAllRecordIds(Entity entity);
	
	public Long loadBlockDataCount(Entity entity, BlockingRoundClass roundClass);
	
	public Set<Long> loadBlockData(Entity entity, String blockRecordId);
	
	public Set<String> loadBlockRecordIds(Entity entity, BlockingRoundClass roundClass);
	
	public Record loadBlockData(Entity entity, BlockingRoundClass roundClass, String blockingKeyValue);
	
	public void saveBlockData(Entity entity, BlockingRoundClass roundClass, Record record);
}
