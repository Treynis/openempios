package org.openhie.openempi.service.impl;

import org.openhie.openempi.AuthenticationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.service.SecurityResourceService;

public class SecurityResourceServiceImpl extends BaseServiceImpl implements SecurityResourceService
{
    public String authenticate(String username, String password) throws AuthenticationException {
        if (log.isDebugEnabled()) {
            log.debug("Received authentication request for user " + username);
        }
        return Context.authenticate(username, password);
    }
}
