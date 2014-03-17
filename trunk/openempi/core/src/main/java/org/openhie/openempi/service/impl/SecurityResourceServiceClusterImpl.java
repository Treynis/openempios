package org.openhie.openempi.service.impl;

import org.openhie.openempi.AuthenticationException;
import org.openhie.openempi.cluster.CommandRequest;
import org.openhie.openempi.cluster.ServiceName;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.service.SecurityResourceService;

public class SecurityResourceServiceClusterImpl extends BaseServiceImpl implements SecurityResourceService
{
    public String authenticate(String username, String password) throws AuthenticationException {
        if (log.isDebugEnabled()) {
            log.debug("Received authentication request for user " + username);
        }
        CommandRequest request = new CommandRequest(ServiceName.SECURITY_RESOURCE_SERVICE, "authenticate", true,
                username, password);
        request = Context.getClusterManager().executeRemoteRequest(request);
        if (request.isHasFailed()) {
            throw new AuthenticationException(request.getException().getMessage());
        }
        String session = (String) request.getResponse();
        return session;
    }
}
