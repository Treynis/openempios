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
package org.openhie.openempi.openpixpdq.fhir;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openhealthtools.openexchange.actorconfig.IheActor;
import org.openhealthtools.openpixpdq.api.IPdSupplierAdapter;
import org.openhealthtools.openpixpdq.common.PatientBroker;
import org.openhealthtools.openpixpdq.common.PixPdqFactory;

import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;

public class PdqFhirServer extends RestfulServer
{
	private static final long serialVersionUID = 1L;
	private static final String PDQM_ACTOR_NAME = "pdsupfhir";
    private Logger log = Logger.getLogger(getClass());

    private IPdSupplierAdapter pdAdapter;
	private PdqSupplierFhir pdqSupplier;

	/**
     * Constructor
     */
    public PdqFhirServer() {
        List<IResourceProvider> resourceProviders = new ArrayList<IResourceProvider>();
        resourceProviders.add(new RestfulPatientResourceProvider(this));
        setResourceProviders(resourceProviders);
        log.info("Started the PDQm FHIR Server");
    }

	@Override
	protected void initialize() throws ServletException {
    	pdAdapter = PixPdqFactory.getPdSupplierAdapter();
    	log.info("Looking up the PDQ Supplier using key " + PDQM_ACTOR_NAME);
    	IheActor iheActor = PatientBroker.getInstance().getActorByName(PDQM_ACTOR_NAME);
    	log.info("Obtained an actor: " + iheActor);
    	if  (iheActor instanceof PdqSupplierFhir) {
    		pdqSupplier = (PdqSupplierFhir) iheActor;
    		log.info("Initialized the supplier: " + pdqSupplier);
    	} else {
    		log.warn("Service has not been configured properly since the ihe actor is either undefined or "
    				+ "is of incorrect type.");
    	}
	}
	
    public PdqSupplierFhir getPdqSupplier() {
		return pdqSupplier;
	}
    
	public IPdSupplierAdapter getPdAdapter() {
		return pdAdapter;
	}
}