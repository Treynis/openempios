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
import java.util.Map;

import org.apache.log4j.Logger;
import org.openhealthtools.openexchange.actorconfig.IActorDescription;
import org.openhealthtools.openexchange.datamodel.Identifier;
import org.openhealthtools.openexchange.datamodel.PatientIdentifier;
import org.openhealthtools.openexchange.datamodel.PersonName;
import org.openhealthtools.openexchange.datamodel.SharedEnums.SexType;
import org.openhealthtools.openexchange.utils.StringUtil;
import org.openhealthtools.openpixpdq.api.PdqQuery;

import ca.uhn.fhir.model.dstu.resource.Patient;
import ca.uhn.fhir.model.dstu.valueset.AdministrativeGenderCodesEnum;
import ca.uhn.fhir.model.dstu.valueset.IdentifierUseEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;

public class FhirConversionHelper
{    
	private static Logger log = Logger.getLogger(FhirConversionHelper.class);
    public static final String FAMILY_NAME = "family";
	public static final String GIVEN_NAME = "given";
	public static final String BIRTH_DATE = "birthdate";
	public static final String ADDRESS = "address";
	public static final String GENDER = "gender";
	public static final String IDENTIFIERS = "identifiers";

	public static PdqQuery extractQueryPerson(IActorDescription actorDescription, Map<String, Object> queryParams) {
		PdqQuery query = new PdqQuery();
		log.debug("Extracting query criteria from parameter map:" + queryParams);
		StringDt value = (StringDt) queryParams.get(FAMILY_NAME);
		if (value != null) {
			addFamilyNameQueryParam(query, value);
		}
		value = (StringDt) queryParams.get(GIVEN_NAME);
		if (value != null) {
			addGivenNameQueryParam(query, value);
		}
		TokenAndListParam objValue = (TokenAndListParam) queryParams.get(IDENTIFIERS);
		if (objValue != null) {
			List<TokenOrListParam> params = objValue.getValuesAsQueryTokens();
			for (TokenOrListParam param : params) {
				List<TokenParam> tokens = param.getValuesAsQueryTokens();
				log.debug("Return domain list is " + query.getReturnDomains());
				for (TokenParam token : tokens) {
					 addIdentifierQueryParam(query, token);
				}
			}
		}
		return query;
	}

	private static void addIdentifierQueryParam(PdqQuery query, TokenParam value) {
		// If the identifier is not specified then this is intended as a domain filter as opposed to a query parameter
		if (value.getValue() == null || value.getValue().isEmpty()) {
			List<Identifier> filterDomains = new ArrayList<Identifier>();
			Identifier id = new Identifier(value.getSystem(), null, null);
			filterDomains.add(id);
			query.addReturnDomains(filterDomains);
			return;
		}
		Identifier id = new Identifier(value.getSystem(), null, null);
		PatientIdentifier pid = new PatientIdentifier(value.getValue(), id);
		query.setPatientIdentifier(pid);
	}

	private static void addGivenNameQueryParam(PdqQuery query, StringDt value) {
		PersonName personName = new PersonName();
		personName.setFirstName((String) value.getValue());
		query.setPersonName(personName);
	}

	private static void addFamilyNameQueryParam(PdqQuery query, StringDt value) {
		PersonName personName = new PersonName();
		personName.setLastName((String) value.getValue());
		query.setPersonName(personName);
	}

	public static Patient innerPatientToFhirPatient(org.openhealthtools.openexchange.datamodel.Patient innerPatient) {
        Patient patient = new Patient();
        convertNamesToFhir(innerPatient, patient);
        convertIdentifiersToFhir(innerPatient, patient);
        convertGenderToFhir(innerPatient, patient);
        patient.setId(new IdDt("Patient", "" + innerPatient.getRecordId()));
		return patient;
	}

	private static void convertGenderToFhir(org.openhealthtools.openexchange.datamodel.Patient innerPatient,
			Patient patient) {
		SexType sexType = innerPatient.getAdministrativeSex();
		switch (sexType) {
			case MALE: 
		        patient.setGender(AdministrativeGenderCodesEnum.M);
		        break;
			case FEMALE:
				patient.setGender(AdministrativeGenderCodesEnum.F);
				break;
			case UNKNOWN:
				patient.setGender(AdministrativeGenderCodesEnum.UNK);
				break;
			case OTHER:
				patient.setGender(AdministrativeGenderCodesEnum.UN);
				break;
		}
	}

	private static void convertIdentifiersToFhir(org.openhealthtools.openexchange.datamodel.Patient innerPatient,
			Patient patient) {
		for (PatientIdentifier id : innerPatient.getPatientIds()) {
	        patient.addIdentifier();
	        patient.getIdentifier().get(0).setUse(IdentifierUseEnum.OFFICIAL);
	        if (id.getAssigningAuthority() != null) {
	        	if (!StringUtil.isNullString(id.getAssigningAuthority().getAuthorityNameString())) {
			        patient.getIdentifier().get(0).getAssigner()
			        	.setDisplay(id.getAssigningAuthority().getAuthorityNameString());
	        	}
	        	if (!StringUtil.isNullString(id.getAssigningAuthority().getNamespaceId())) {
	    	        patient.getIdentifier().get(0)
	    	        	.setSystem(new UriDt("urn:oid:" + id.getAssigningAuthority().getNamespaceId()));
	        	} else if (!StringUtil.isNullString(id.getAssigningAuthority().getUniversalId())) {
	        		patient.getIdentifier().get(0)
	        			.setSystem(new UriDt("urn:oid:" + id.getAssigningAuthority().getUniversalId() + ":" +
	        					id.getAssigningAuthority().getUniversalIdType()));
	        	}
	        }
	        patient.getIdentifier().get(0).setValue(id.getId());
		}		
	}

	private static void convertNamesToFhir(org.openhealthtools.openexchange.datamodel.Patient innerPatient,
			Patient patient) {
        patient.addName();
        PersonName personName = innerPatient.getPatientName();
        patient.getName().get(0).addFamily(personName.getLastName());
        patient.getName().get(0).addGiven(personName.getFirstName());		
	}

}
