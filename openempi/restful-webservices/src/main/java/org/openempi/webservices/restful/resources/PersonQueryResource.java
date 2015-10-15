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
package org.openempi.webservices.restful.resources;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openempi.webservices.restful.model.IdentifierDomainAttributeRequest;
import org.openempi.webservices.restful.model.PersonPagedRequest;
import org.openempi.webservices.restful.model.StringList;
import org.openhie.openempi.cluster.ServiceName;
import org.openhie.openempi.model.Gender;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.IdentifierDomainAttribute;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.PersonLink;
import org.openhie.openempi.model.Race;
import org.openhie.openempi.model.ReviewRecordPair;
import org.openhie.openempi.service.PersonQueryResourceService;
import org.openhie.openempi.service.ResourceServiceFactory;

@Path("/person-query-resource")
public class PersonQueryResource
{
    private PersonQueryResourceService service;
    
    public PersonQueryResource() {
        service = (PersonQueryResourceService)
                ResourceServiceFactory.createResourceService(ServiceName.PERSON_QUERY_RESOURCE_SERVICE,
                        PersonQueryResourceService.class);
    }
    
	@GET
	@Path("/findGenderByCode")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Gender findGenderByCode(@QueryParam("genderCode") String genderCode) {
		return service.findGenderByCode(genderCode);
	}

	@GET
	@Path("/findGenderByName")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Gender findGenderByName(@QueryParam("genderName") String genderName) {
		return service.findGenderByName(genderName);
	}
	
	@GET
	@Path("/findRaceByCode")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Race findRaceByCode(@QueryParam("raceCode") String raceCode) {
		return service.findRaceByCode(raceCode);
	}

	@GET
	@Path("/findRaceByName")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Race findRaceByName(@QueryParam("raceName") String raceName) {
		return service.findRaceByName(raceName);
	}
	
	@GET
	@Path("/getIdentifierDomainTypeCodes")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public StringList getIdentifierDomainTypeCodes() {
		List<String> typeCodes = service.getIdentifierDomainTypeCodes();
		return new StringList(typeCodes);
	}
 
	@POST
	@Path("/findIdentifierDomain")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public IdentifierDomain findIdentifierDomain(IdentifierDomain identifierDomain) {
		return service.findIdentifierDomain(identifierDomain);
	}	

	@GET
	@Path("/getIdentifierDomains")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<IdentifierDomain> getIdentifierDomains() {
		return service.getIdentifierDomains();
	}
	
	@POST
	@Path("/getIdentifierDomainAttributes")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<IdentifierDomainAttribute> getIdentifierDomainAttributes(IdentifierDomain identifierDomain) {
		return service.getIdentifierDomainAttributes(identifierDomain);
	}	
	
	@POST
	@Path("/getIdentifierDomainAttribute")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public IdentifierDomainAttribute getIdentifierDomainAttribute(IdentifierDomainAttributeRequest identifierDomainAttributeRequest) {
		return service.getIdentifierDomainAttribute(identifierDomainAttributeRequest.getIdentifierDomain(),
				identifierDomainAttributeRequest.getAttributeName());
	}	

	@GET
	@Path("/getPersonModelAllAttributeNames")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public StringList getPersonModelAllAttributeNames() {
		List<String> personAllAttributes = service.getPersonModelAllAttributeNames();
		return new StringList(personAllAttributes);
	}
	
	@GET
	@Path("/getPersonModelAttributeNames")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public StringList getPersonModelAttributeNames() {
		List<String> personAttributes = service.getPersonModelAttributeNames();
		return new StringList(personAttributes);
	}
	
	@GET
	@Path("/getPersonModelCustomAttributeNames")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public StringList getPersonModelCustomAttributeNames() {
		List<String> personCustomAttributes = service.getPersonModelCustomAttributeNames();
		return new StringList(personCustomAttributes);
	}
	
	@POST
	@Path("/findPersonById")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Person findPersonById(PersonIdentifier identifier) {
		return service.findPersonById(identifier);
	}

	@POST
	@Path("/getGlobalIdentifierById")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public PersonIdentifier getGlobalIdentifierById(PersonIdentifier identifier) {
		PersonIdentifier globalIdentifier = service.getGlobalIdentifierById(identifier);
		if( globalIdentifier == null )		
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		else
			return globalIdentifier;
	}

	@GET
	@Path("/getSingleBestRecord")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Person getSingleBestRecord(@QueryParam("personId") Integer personId) {
		return service.getSingleBestRecord(personId);
	}
	
	@GET
	@Path("/getSingleBestRecords")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Person> getSingleBestRecords(@QueryParam("personId") List<Integer> personIds) {
		return service.getSingleBestRecords(personIds);
	}
	
	@POST
	@Path("/findLinkedPersons")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Person> findLinkedPersons(PersonIdentifier identifier) {
		return service.findLinkedPersons(identifier);
	}

	@POST
	@Path("/getPersonLinks")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<PersonLink> getPersonLinks(Person person) {
		return service.getPersonLinks(person); 
	}
	
	// search 
	@POST
	@Path("/findPersonsByAttributes")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Person> findPersonsByAttributes(Person person) {
		return service.findPersonsByAttributes(person);
	}
	
	@POST
	@Path("/findPersonsByAttributesPaged")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Person> findPersonsByAttributesPaged(PersonPagedRequest request) {
		return service.findPersonsByAttributesPaged(request.getPerson(),
				request.getFirstResult(), request.getMaxResults());
	}
	
	// matching search
	@POST
	@Path("/findMatchingPersonsByAttributes")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Person> findMatchingPersonsByAttributes(Person person) {
		return service.findMatchingPersonsByAttributes(person);
	}
	
	@GET
	@Path("/loadPerson")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Person loadPerson(@QueryParam("personId") Integer personId) {
		return service.loadPerson(personId);
	}
	
	@GET
	@Path("/loadAllPersonsPaged")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Person> loadAllPersonsPaged(@QueryParam("firstRecord") Integer firstRecord, @QueryParam("maxRecords") Integer maxRecords) {
		return service.loadAllPersonsPaged(firstRecord, maxRecords);
	}
	
	@GET
	@Path("/loadPersons")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Person> loadPersons(@QueryParam("personId") List<Integer> personIds) {
		List<Person> persons = service.loadPersons(personIds);
		return persons;
	}
	
	@GET
	@Path("/loadAllUnreviewedPersonLinks")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<ReviewRecordPair> loadAllUnreviewedPersonLinks() {
		return service.loadAllUnreviewedPersonLinks();
	}
	
	@GET
	@Path("/loadUnreviewedPersonLinks")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<ReviewRecordPair> loadUnreviewedPersonLinks( @QueryParam("maxRecords") Integer maxRecords) {
		return service.loadUnreviewedPersonLinks(maxRecords);
	}
	
	@GET
	@Path("/loadReviewRecordPair")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public ReviewRecordPair loadReviewRecordPair(@QueryParam("personLinkReviewId") Integer personLinkReviewId) {
		return service.loadReviewRecordPair(personLinkReviewId);
	}
}
