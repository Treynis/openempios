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
package org.openempi.webapp.server;

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.openempi.webapp.client.IdentifierDomainDataService;
import org.openempi.webapp.client.model.IdentifierDomainWeb;
import org.openempi.webapp.server.util.ModelTransformer;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.service.IdentifierDomainService;

public class IdentifierDomainDataServiceImpl extends AbstractRemoteServiceServlet implements IdentifierDomainDataService
{

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}
	
	public List<IdentifierDomainWeb> getIdentifierDomains() throws Exception {
		log.debug("Received request to retrieve the list of identifier domains.");
		
		authenticateCaller();
		try {
			IdentifierDomainService identifierDomainService = Context.getIdentifierDomainService();
			List<org.openhie.openempi.model.IdentifierDomain> domains = identifierDomainService.getIdentifierDomains();
			List<IdentifierDomainWeb> dtos = ModelTransformer.mapList(domains, IdentifierDomainWeb.class);
			return dtos;
		} catch (Throwable t) {
			log.error("Failed to execute: " + t.getMessage(), t);
			throw new RuntimeException(t);
		}		
	}

	public IdentifierDomainWeb addIdentifierDomain(IdentifierDomainWeb identifierDomain) throws Exception {
		log.debug("Received request to add a new Identifier Domain entry to the repository.");
		
		authenticateCaller();
		String msg = "";
		try {
			IdentifierDomainService identifierDomainService = Context.getIdentifierDomainService();
			org.openhie.openempi.model.IdentifierDomain domain = ModelTransformer.map(identifierDomain, org.openhie.openempi.model.IdentifierDomain.class);
			
			IdentifierDomain newDomain = identifierDomainService.addIdentifierDomain(domain);
			
			return ModelTransformer.map( newDomain, IdentifierDomainWeb.class);
		} catch (Throwable t) {
			// log.error("Failed to add person entry: " + t, t);
			msg = t.getMessage();			
			throw new Exception(msg);
		}
	}
	
	public IdentifierDomainWeb updateIdentifierDomain(IdentifierDomainWeb identifierDomain) throws Exception {
		log.debug("Received request to add a new Identifier Domain entry to the repository.");
		
		authenticateCaller();
		String msg = "";
		try {
			IdentifierDomainService identifierDomainService = Context.getIdentifierDomainService();
			org.openhie.openempi.model.IdentifierDomain domain = ModelTransformer.map(identifierDomain, org.openhie.openempi.model.IdentifierDomain.class);
			
			IdentifierDomain updateDomain = identifierDomainService.updateIdentifierDomain(domain);
			
			return ModelTransformer.map( updateDomain, IdentifierDomainWeb.class);
		} catch (Throwable t) {
			// log.error("Failed to add person entry: " + t, t);
			msg = t.getMessage();			
			throw new Exception(msg);
		}
	}
	
	public String deleteIdentifierDomain(IdentifierDomainWeb identifierDomain) throws Exception {
		log.debug("Received request to add a new Identifier Domain entry to the repository.");
		
		authenticateCaller();
		String msg = "";
		try {
			IdentifierDomainService identifierDomainService = Context.getIdentifierDomainService();
			org.openhie.openempi.model.IdentifierDomain domain = ModelTransformer.map(identifierDomain, org.openhie.openempi.model.IdentifierDomain.class);
			identifierDomainService.deleteIdentifierDomain(domain);
		} catch (Throwable t) {
			// log.error("Failed to add person entry: " + t, t);
			msg = t.getMessage();			
			throw new Exception(msg);
		}
		return msg;
	}
}
