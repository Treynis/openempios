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
package org.openhie.openempi.dao;

import java.util.Date;
import java.util.List;

import org.openhie.openempi.model.UserSession;

public interface UserSessionDao extends UniversalDao
{
	public void saveUserSession(UserSession session);
	
	public List<UserSession> findAll();
    
    public List<UserSession> findByUser(Long userId);
	
	public UserSession findById(Integer sessionId);
	
	public List<UserSession> findExpiredSessions(Date cutoff);
	
	public UserSession findBySessionKey(String sessionKey);
	
	public void removeUserSession(String sessionKey);
}
