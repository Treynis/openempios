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
package org.openempi.webapp.client;

import java.util.List;

import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.UserFileWeb;

import com.google.gwt.user.client.rpc.RemoteService;

public interface EntityDefinitionDataService extends RemoteService
{	
	public List<EntityWeb> loadEntities() throws Exception;	
	
	public EntityWeb addEntity(EntityWeb entity) throws Exception;	
	
	public EntityWeb updateEntity(EntityWeb entity) throws Exception;	
	
	public String deleteEntity(EntityWeb entity) throws Exception;
	
	public String importEntity(UserFileWeb userFile) throws Exception;
    
	public String exportEntity(EntityWeb entityModel, String fileName) throws Exception;	
}
