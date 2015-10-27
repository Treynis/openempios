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
package org.openhie.openempi.service.impl;

import org.openhie.openempi.AuthenticationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.service.SecurityResourceService;
import org.openhie.openempi.util.ConvertUtil;

public class SecurityResourceServiceImpl extends BaseServiceImpl implements SecurityResourceService
{
    public String authenticate(String username, String password) throws AuthenticationException {
        if (log.isDebugEnabled()) {
            log.debug("Received authentication request for user " + username);
        }
        return Context.authenticate(username, password);
    }

	@Override
	public String isSessionValid(String sessionKey) {
		if (ConvertUtil.isNullOrEmpty(sessionKey)) {
			return Boolean.FALSE.toString();
		}
		
		try {
			Context.authenticate(sessionKey);
			return Boolean.TRUE.toString();
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Invalid session key was passed in for authentication.");
			}
		}
		return Boolean.FALSE.toString();
	}
}
