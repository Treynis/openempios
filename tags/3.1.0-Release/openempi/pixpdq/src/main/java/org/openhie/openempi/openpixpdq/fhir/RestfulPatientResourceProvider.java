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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openhealthtools.openpixpdq.api.IPdSupplierAdapter;
import org.openhealthtools.openpixpdq.api.PdSupplierException;
import org.openhealthtools.openpixpdq.api.PdqQuery;
import org.openhealthtools.openpixpdq.api.PdqResult;

import ca.uhn.fhir.model.dstu.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu.resource.Patient;
import ca.uhn.fhir.model.dstu.valueset.AdministrativeGenderCodesEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;

/**
 * All resource providers must implement IResourceProvider
 */
public class RestfulPatientResourceProvider implements IResourceProvider
{
    private Logger log = Logger.getLogger(getClass());

	PdqFhirServer pdqFhirServer;
	
    public RestfulPatientResourceProvider(PdqFhirServer pdqFhirServer) {
		this.pdqFhirServer = pdqFhirServer;
	}
    
	/**
     * The getResourceType method comes from IResourceProvider, and must
     * be overridden to indicate what type of resource this provider
     * supplies.
     */
	public Class<Patient> getResourceType() {
		return Patient.class;
	}
    /**
     * The "@Read" annotation indicates that this method supports the
     * read operation. Read operations should return a single resource
     * instance.
     *
     * @param theId
     *    The read operation takes one parameter, which must be of type
     *    IdDt and must be annotated with the "@Read.IdParam" annotation.
     * @return
     *    Returns a resource matching this identifier, or null if none exists.
     */
    @Read()
    public Patient getResourceById(@IdParam IdDt theId) {
        Patient patient = new Patient();
        patient.addIdentifier();
        patient.getIdentifier().get(0).setSystem(new UriDt("urn:hapitest:mrns"));
        patient.getIdentifier().get(0).setValue("00002");
        patient.addName().addFamily("Test");
        patient.getName().get(0).addGiven("PatientOne");
        patient.setGender(AdministrativeGenderCodesEnum.F);
        return patient;
    }
 
    /**
     * The "@Search" annotation indicates that this method supports the
     * search operation. You may have many different method annotated with
     * this annotation, to support many different search criteria. This
     * example searches by family name.
     *
     * @param theIdentifier
     *    This operation takes one parameter which is the search criteria. It is
     *    annotated with the "@Required" annotation. This annotation takes one argument,
     *    a string containing the name of the search criteria. The datatype here
     *    is StringDt, but there are other possible parameter types depending on the
     *    specific search criteria.
     * @return
     *    This method returns a list of Patients. This list may contain multiple
     *    matching resources, or it may also be empty.
     */
    @Search()
    public List<Patient> getPatient(@OptionalParam(name = Patient.SP_FAMILY) StringDt familyName,
    		@OptionalParam(name = Patient.SP_GIVEN) StringDt givenName,
    		@OptionalParam(name = Patient.SP_IDENTIFIER) TokenAndListParam identifiers) {
    	
    	IPdSupplierAdapter pdAdapter = pdqFhirServer.getPdAdapter();
        List<Patient> patients = new ArrayList<Patient>();
    	try {
    		Map<String,Object> queryParams = new HashMap<String,Object>();
    		queryParams.put(FhirConversionHelper.FAMILY_NAME, familyName);
    		queryParams.put(FhirConversionHelper.GIVEN_NAME, givenName);
    		queryParams.put(FhirConversionHelper.IDENTIFIERS, identifiers);
    		
    		PdqQuery query = FhirConversionHelper.
    				extractQueryPerson(pdqFhirServer.getPdqSupplier().getActorDescription(), queryParams);
        	PdqResult result = pdAdapter.findPatients(query, null);
        	log.debug("Result of query is: " + result);
        	if (result.getPatients().size() == 0) {
        		return patients;
        	}
        	for (Iterator<List<org.openhealthtools.openexchange.datamodel.Patient>> iter = 
        			result.getPatients().iterator(); iter.hasNext(); ) {
        		List<org.openhealthtools.openexchange.datamodel.Patient> innerList = iter.next();
        		for (org.openhealthtools.openexchange.datamodel.Patient innerPatient : innerList) {
        			Patient fhirPatient = FhirConversionHelper.innerPatientToFhirPatient(innerPatient);
        			patients.add(fhirPatient);
        		}
        	}	    	
	    } catch (PdSupplierException e) {
	    	log.error("Encountered a problem while processing find patients: " + e, e);
	    	
	    } catch (Throwable e) {
	    	log.error("Encountered a problem while processing the patient discovery request: " + e, e);
	    }
        return patients;
    }
}

