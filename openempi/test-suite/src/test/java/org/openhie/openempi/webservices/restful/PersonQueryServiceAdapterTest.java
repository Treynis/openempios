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
package org.openhie.openempi.webservices.restful;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.openempi.webservices.restful.model.PersonPagedRequest;
import org.openempi.webservices.restful.model.StringList;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonLink;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.ReviewRecordPair;
import org.openhie.openempi.model.User;
import org.openhie.openempi.util.ConvertUtil;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.header.MediaTypes;

public class PersonQueryServiceAdapterTest extends BaseRestfulServiceTestCase
{
    public void testApplicationWadl() {
        String serviceWadl = getWebResource()
        		.path("application.wadl")
        		.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        		.accept(MediaTypes.WADL)
        		.get(String.class);
        assertTrue("Looks like the expected wadl is not generated",
                serviceWadl.length() > 0);
    }
 
    public void testGetIdentifierDomainTypeCodes() {
        StringList data = 
                getWebResource().path("person-query-resource")
                    .path("getIdentifierDomainTypeCodes")
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    .accept(MediaType.APPLICATION_JSON)
                    .get(StringList.class); 
        log.debug("Response is : " + data);
        assertTrue("Failed to retrieve domain type code list.", data != null && data.getData() != null && data.getData().size() > 0);
    }
    
    public void testFindIdentifierDomainById() {
    	List<IdentifierDomain> domains = 
    			getWebResource().path("person-query-resource")
    				.path("getIdentifierDomains")
    				.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
    				.accept(MediaType.APPLICATION_JSON)
    				.get(new GenericType<List<IdentifierDomain>>(){});
    	assertTrue("Failed to retrieve domains list.",
    			domains != null && domains.size() > 0);
    	
    	Integer id = null;
    	for (IdentifierDomain domain : domains) {
    		if (domain.getIdentifierDomainName().startsWith("IHENA")) {
    			id = domain.getIdentifierDomainId();
    		}
    		if( id == null ) {
	    		if (domain.getIdentifierDomainName().startsWith("IHELOCAL")) {
	    			id = domain.getIdentifierDomainId();
	    		}
    		}
    	}
    	IdentifierDomain domain = new IdentifierDomain(id, null, null);
        IdentifierDomain identifierDomain = getWebResource().path("person-query-resource")
        			.path("findIdentifierDomain")
        			.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        			.accept(MediaType.APPLICATION_XML)
        			.post(IdentifierDomain.class, domain);
        assertNotNull("Unable to retrieve domain using the id: " + id, identifierDomain);
    }

    public void testFindIdentifierDomainByNamespaceIdentifier() {
    	List<IdentifierDomain> domains = 
    			getWebResource().path("person-query-resource")
    				.path("getIdentifierDomains")
    				.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
    				.accept(MediaType.APPLICATION_JSON)
    				.get(new GenericType<List<IdentifierDomain>>(){});
    	assertTrue("Failed to retrieve domains list.",
    			domains != null && domains.size() > 0);
    	
    	IdentifierDomain id = null;
    	for (IdentifierDomain domain : domains) {
    		if (domain.getIdentifierDomainName().startsWith("NIST2010")) {
    			id = domain;
    		}
    	}
    	if (id == null) {
    		return;
    	}
    	
    	IdentifierDomain domain = new IdentifierDomain();
    	domain.setNamespaceIdentifier(id.getNamespaceIdentifier());
        IdentifierDomain identifierDomain = getWebResource().path("person-query-resource")
        			.path("findIdentifierDomain")
        			.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        			.accept(MediaType.APPLICATION_JSON)
        			.post(IdentifierDomain.class, domain);
        assertNotNull("Unable to retrieve domain using the namespace identifier: " + id.getNamespaceIdentifier(),
        		identifierDomain);
    }

    public void testGetPersonModelAllAttributeNames() {
    	StringList data = 
    			getWebResource().path("person-query-resource")
    				.path("getPersonModelAllAttributeNames")
    				.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
    				.accept(MediaType.APPLICATION_JSON)
    				.get(StringList.class);	
    	log.debug("Response is : " + data);
    	assertTrue("Failed to retrieve All Attribute Names.", data != null && data.getData() != null && data.getData().size() > 0);
    }

    public void testGetPersonModelAttributeNames() {
    	StringList data = 
    			getWebResource().path("person-query-resource")
    				.path("getPersonModelAttributeNames")
    				.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
    				.accept(MediaType.APPLICATION_JSON)
    				.get(StringList.class);	
    	log.debug("Response is : " + data);
    	assertTrue("Failed to retrieve Attribute Names.", data != null && data.getData() != null && data.getData().size() > 0);
    }
    
    public void testGetPersonModelCustomAttributeNames() {
    	StringList data = 
    			getWebResource().path("person-query-resource")
    				.path("getPersonModelCustomAttributeNames")
    				.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
    				.accept(MediaType.APPLICATION_JSON)
    				.get(StringList.class);	
    	log.debug("Response is : " + data);
    	assertTrue("Failed to retrieve Custom Attribute Names.", data != null && data.getData() != null && data.getData().size() > 0);
    }  

    public void testLoadAllPersonsPaged() {
        // add persons
        Person person = buildTestPerson("111-11-1111");
        person.setAddress1("TEMP1");
        ClientResponse response = getWebResource().path("person-manager-resource")
            .path("addPerson")
            .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
            .accept(MediaType.APPLICATION_JSON)
            .put(ClientResponse.class, person);
        if (response.getStatus() != Status.OK.getStatusCode()) {
            assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.CONFLICT.getStatusCode());
        }

        person = buildTestPerson("222-22-2222");
        person.setAddress1("TEMP2");
        response = getWebResource().path("person-manager-resource")
            .path("addPerson")
            .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
            .accept(MediaType.APPLICATION_JSON)
            .put(ClientResponse.class, person);
        if (response.getStatus() != Status.OK.getStatusCode()) {
            assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.CONFLICT.getStatusCode());
        }

        List<Person> persons = 
                getWebResource().path("person-query-resource")
                    .path("loadAllPersonsPaged")
                    .queryParam("firstRecord", "1")
                    .queryParam("maxRecords", "20")
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    .accept(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<Person>>(){});
        assertTrue("Failed to retrieve person list paged.", persons != null && persons.size() > 0);
    }
    
    public void testLoadPerson() {
        Person person = new Person();
        person.setGivenName("Leane");
        List<Person> persons = getWebResource().path("person-query-resource")
                .path("findPersonsByAttributes")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_XML)
                .post(new GenericType<List<Person>>(){}, person);

        assertNotNull("Unable to retrieve persons using the person as search: "+person, persons);
        
        person = persons.get(0);        
        Person personFound = getWebResource().path("person-query-resource")
                    .path("loadPerson")
                    .queryParam("personId", person.getPersonId().toString())
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Person.class);
        assertNotNull("Unable to retrieve person using the id: " + personFound);
    }    
    
    public void testFindPersonsByAttributes() {
        List<Person> persons = null;
        Person person = new Person();
        person.setGivenName("Robert");
        
        persons = getWebResource().path("person-query-resource")
                    .path("findPersonsByAttributes")
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    .accept(MediaType.APPLICATION_XML)
                    .post(new GenericType<List<Person>>(){}, person);
        assertNotNull("Unable to retrieve persons using the person as search: "+person, persons);
    }

    public void testFindPersonsByAttributesPaged() {
        List<Person> persons = null;
        Person person = new Person();
        person.setGivenName("Robert");
   
        PersonPagedRequest personPagedRequest= new PersonPagedRequest(person, 1, 10);

        persons = getWebResource().path("person-query-resource")
                    .path("findPersonsByAttributesPaged")
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    .accept(MediaType.APPLICATION_XML)
                    .post(new GenericType<List<Person>>(){}, personPagedRequest);
        
        assertNotNull("Unable to retrieve paged persons using the person as search: "+person, persons);
    }
    
    public void testFindPersonById() {
        Person person = new Person();
        person.setGivenName("Leane");
        person.setFamilyName("Neh");
        List<Person> persons = getWebResource().path("person-query-resource")
                .path("findPersonsByAttributes")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_XML)
                .post(new GenericType<List<Person>>(){}, person);

        assertNotNull("Unable to retrieve persons using the person as search: "+person, persons);
        
       	person = persons.get(0);       	
    	PersonIdentifier personIdentifier = person.getPersonIdentifiers().iterator().next();; 
 
        person = getWebResource().path("person-query-resource")
        			.path("findPersonById")
        			.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        			.accept(MediaType.APPLICATION_XML)
        			.post(Person.class, personIdentifier);
        assertNotNull("Unable to retrieve person using the personIdentifier: "+personIdentifier, person);
    }
    
    public void testLoadAllUnreviewedPersonLinks() {        
        // get person person right
        Person personLeft = findTestPersonByName("Leane", "Neh");         
        Person personRight = findTestPersonByName("Leane", "Ned");          
        if( personLeft == null || personRight == null ) {
            assertNotNull("Unable to find test person", null);  
            return;
        }
        
        // add ReviewRecordPair
        ReviewRecordPair recordPair = new ReviewRecordPair();
        recordPair.setPersonLeft(personLeft);
        recordPair.setPersonRight(personRight);
        
        recordPair.setDateCreated(new java.util.Date());
        User user = new User();
        user.setId(new Long(-1));
        recordPair.setUserCreatedBy(user);
        recordPair.setUserReviewedBy(user);
        
        recordPair.setLinkSource(new LinkSource(1));
        recordPair.setWeight(1.0);
        
        ClientResponse response = getWebResource().path("person-manager-resource")
            .path("addReviewRecordPair")
            .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
            .accept(MediaType.APPLICATION_JSON)
            .put(ClientResponse.class, recordPair);

        if (response.getStatus() != Status.OK.getStatusCode()) {
            assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.BAD_REQUEST.getStatusCode());
        }
        
        // load all Unreviewed person links
        List<ReviewRecordPair> reviewRecordPairs = 
                getWebResource().path("person-query-resource")
                    .path("loadAllUnreviewedPersonLinks")
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    .accept(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<ReviewRecordPair>>(){});
        assertTrue("Failed to retrieve ReviewRecordPairs list.", reviewRecordPairs != null && reviewRecordPairs.size() > 0);
    }
 
    public void testLoadReviewRecordPair() {
        List<ReviewRecordPair> reviewRecordPairs = 
                getWebResource().path("person-query-resource")
                    .path("loadAllUnreviewedPersonLinks")
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    .accept(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<ReviewRecordPair>>(){});
        assertTrue("Failed to retrieve ReviewRecordPairs list.", reviewRecordPairs != null && reviewRecordPairs.size() > 0);
        
        ReviewRecordPair reviewRecordPair = reviewRecordPairs.get(0);
        ReviewRecordPair reviewRecordPairFound = 
                getWebResource().path("person-query-resource")
                    .path("loadReviewRecordPair")
                    .queryParam("personLinkReviewId", reviewRecordPair.getReviewRecordPairId().toString())
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    .accept(MediaType.APPLICATION_JSON)
                    .get(ReviewRecordPair.class);
        
        assertNotNull("Unable to retrieve reviewRecordPair using the id: " + reviewRecordPairFound);    
    }
    
    public void testFindLinkedPersons() {
        // get a Unreviewed pair
        List<ReviewRecordPair> reviewRecordPairs = 
                getWebResource().path("person-query-resource")
                    .path("loadAllUnreviewedPersonLinks")
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    .accept(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<ReviewRecordPair>>(){});
        assertTrue("Failed to retrieve ReviewRecordPairs list.", reviewRecordPairs != null && reviewRecordPairs.size() > 0);
        
        ReviewRecordPair reviewRecordPair = reviewRecordPairs.get(0);
        for (ReviewRecordPair pair : reviewRecordPairs) {
            Person left = pair.getPersonLeft();
            Person right = pair.getPersonRight();
            if (left.getAddress1().equals("TEMP1") || right.getAddress1().equals("TEMP1")) {
                reviewRecordPair = pair;
            }
        }        

        PersonLink personLink = ConvertUtil.getPersonLinkFromReviewRecordPair(reviewRecordPair);

        // link persons
        ClientResponse response = getWebResource().path("person-manager-resource")
        .path("linkPersons")
        .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        .accept(MediaType.APPLICATION_JSON)
        .put(ClientResponse.class, personLink);
    
        if (response.getStatus() != Status.OK.getStatusCode()) {
            assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.BAD_REQUEST.getStatusCode());
        }   
        
        // findLinkedPersons
        Person person = findTestPersonByName("Leane", "Neh");       
    	PersonIdentifier personIdentifier = person.getPersonIdentifiers().iterator().next();; 
 
    	List<Person> persons = getWebResource().path("person-query-resource")
        			.path("findLinkedPersons")
        			.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        			.accept(MediaType.APPLICATION_XML)
        			.post(new GenericType<List<Person>>(){}, personIdentifier);
        assertNotNull("Unable to retrieve persons using the personIdentifier: "+personIdentifier, persons);
    }
    
    public void testGetPersonLinks() {
        Person person = findTestPerson("Odysseas", "TEMP1");      
 
       	List<PersonLink> personLinks = getWebResource().path("person-query-resource")
        			.path("getPersonLinks")
        			.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        			.accept(MediaType.APPLICATION_XML)
        			.post(new GenericType<List<PersonLink>>(){}, person);
        assertNotNull("Unable to retrieve person links using the person: ", personLinks);
    }

    public void testDeletePerson() {
        Person person = findTestPerson("Master", "TEMP1");  

        // remove person completely
        ClientResponse response = getWebResource().path("person-manager-resource")
            .path("removePersonById")
            .queryParam("personId", person.getPersonId().toString())
            .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
            .accept(MediaType.APPLICATION_JSON)
            .post(ClientResponse.class);

        if (response.getStatus() != Status.NO_CONTENT.getStatusCode()) {
            assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.NO_CONTENT.getStatusCode());
        }
        
        person = findTestPerson("Master", "TEMP2");  

        // remove person completely
        response = getWebResource().path("person-manager-resource")
            .path("removePersonById")
            .queryParam("personId", person.getPersonId().toString())
            .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
            .accept(MediaType.APPLICATION_JSON)
            .post(ClientResponse.class);

        if (response.getStatus() != Status.OK.getStatusCode()) {
            assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.NOT_FOUND.getStatusCode());
        }
    }
    
    public Person findTestPerson(String givenName, String address) {
        
        List<Person> persons = null;
        Person person = new Person();
        person.setGivenName(givenName);
        person.setAddress1(address);
        
        persons = getWebResource().path("person-query-resource")
                    .path("findPersonsByAttributes")
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    .accept(MediaType.APPLICATION_XML)
                    .post(new GenericType<List<Person>>(){}, person);
 
        if (persons == null || persons.size() == 0) {
            return null;
        }        
        return persons.get(0);              
    }
    
    public Person findTestPersonByName(String givenName, String familyName) {
        
        List<Person> persons = null;
        Person person = new Person();
        person.setGivenName(givenName);
        person.setFamilyName(familyName);
        
        persons = getWebResource().path("person-query-resource")
                    .path("findPersonsByAttributes")
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    .accept(MediaType.APPLICATION_XML)
                    .post(new GenericType<List<Person>>(){}, person);
 
        if (persons == null || persons.size() == 0) {
            return null;
        }        
        return persons.get(0);              
    }
}
