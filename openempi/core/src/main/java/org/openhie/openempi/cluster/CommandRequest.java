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
package org.openhie.openempi.cluster;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.context.UserContext;


public class CommandRequest implements Serializable
{
    private static final long serialVersionUID = 2925735103911208737L;
    private final Logger log = Logger.getLogger(getClass());
    private ServiceName serviceName;
    private String requestName;
    private boolean hasResponse;
    private Serializable response;
    private Throwable exception;
    private boolean hasFailed;
    private Object[] params;
    private UserContext userContext;

    public CommandRequest(ServiceName serviceName, String requestName, boolean hasResponse, final Serializable... args)
    {
        this.serviceName = serviceName;
        this.requestName = requestName;
        this.hasResponse = hasResponse;
        if (args.length > 0) {
            params = args;
        }
        this.hasFailed = false;
        this.userContext = Context.getUserContext();
        log.warn("Saved the user context : " + userContext);
    }

    public ServiceName getServiceName() {
        return serviceName;
    }

    public void setServiceName(ServiceName serviceName) {
        this.serviceName = serviceName;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public boolean isHasResponse() {
        return hasResponse;
    }

    public void setHasResponse(boolean hasResponse) {
        this.hasResponse = hasResponse;
    }

    public Serializable getResponse() {
        return response;
    }

    public void setResponse(Serializable response) {
        this.response = response;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
        setHasFailed(true);
    }

    public boolean isHasFailed() {
        return hasFailed;
    }

    public void setHasFailed(boolean hasFailed) {
        this.hasFailed = hasFailed;
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }
}
