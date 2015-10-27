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

import org.openempi.webservices.restful.model.IdentifierDomainAttributeRequest;
import org.openempi.webservices.restful.model.MergePersonsRequest;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.IdentifierDomainAttribute;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.PersonLink;
import org.openhie.openempi.model.ReviewRecordPair;
import org.openhie.openempi.model.User;
import org.openhie.openempi.util.ConvertUtil;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

public class PersonManagerServiceAdapterTest extends BaseRestfulServiceTestCase
{

	public void testAddPerson() {
		Person person = buildTestPerson("555-66-7777");
		ClientResponse response = getWebResource().path("person-manager-resource")
		.path("addPerson")
		.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
		.accept(MediaType.APPLICATION_JSON)
		.put(ClientResponse.class, person);
		// If this is not a success then the only other correct behavior is that
		// the person already existed and was not added, in which case we should
		// get back a NOT_MODIFIED status code.
		if (response.getStatus() != Status.OK.getStatusCode()) {
			assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.CONFLICT.getStatusCode());
		}
	}
	
	public void testUpdatePerson() {
    	List<Person> persons = null;
       	Person person = new Person();
       	person.setGivenName("Odysseas");
        
    	persons = getWebResource().path("person-query-resource")
        			.path("findPersonsByAttributes")
        			.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        			.accept(MediaType.APPLICATION_XML)
        			.post(new GenericType<List<Person>>(){}, person);

       	if (persons.size() == 0) {
       		assertFalse("Unable to get a person to update", persons.size() == 0);
    		return;
    	}
       	
       	person = persons.get(0);           	
       	person.setAddress1("TEMP1");
       	
		ClientResponse response = getWebResource().path("person-manager-resource")
		.path("updatePerson")
		.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
		.accept(MediaType.APPLICATION_JSON)
		.put(ClientResponse.class, person);

		if (response.getStatus() != Status.OK.getStatusCode()) {
			assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.NOT_MODIFIED.getStatusCode());
		}
	}
	
	public void testUpdatePersonById() {
       	Person person = findTestPerson("Odysseas", "TEMP1");   
       	person.setAddress1("TEMP2");
       	
		ClientResponse response = getWebResource().path("person-manager-resource")
		.path("updatePersonById")
		.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
		.accept(MediaType.APPLICATION_JSON)
		.put(ClientResponse.class, person);

		if (response.getStatus() != Status.OK.getStatusCode()) {
			assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.NOT_MODIFIED.getStatusCode());
		}
	}
	
	public void testImportPerson() {
		  
		Person person = buildTestPerson("111-22-3333");
        person.setAddress1("TEMP3");
		
		ClientResponse response = getWebResource().path("person-manager-resource")
		.path("importPerson")
		.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
		.accept(MediaType.APPLICATION_JSON)
		.put(ClientResponse.class, person);

		if (response.getStatus() != Status.OK.getStatusCode()) {
			assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.CONFLICT.getStatusCode());
		}
		
        person = buildTestPerson("222-33-4444");
        person.setAddress1("TEMP4");
        
        response = getWebResource().path("person-manager-resource")
        .path("importPerson")
        .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        .accept(MediaType.APPLICATION_JSON)
        .put(ClientResponse.class, person);

        if (response.getStatus() != Status.OK.getStatusCode()) {
            assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.CONFLICT.getStatusCode());
        }
	}
 
     public void testAddReviewRecordPair() {
         // person left                 
        Person personLeft = findTestPerson("Odysseas", "TEMP2");         
        
        // person right
        Person personRight = findTestPerson("Odysseas", "TEMP3");  
        
        if( personLeft == null || personRight == null ) {
            assertNotNull("Unable to find test person", null);  
            return;
        }
              
        ClientResponse response = addReviewRecordPair(personLeft, personRight);
        
        if (response.getStatus() != Status.OK.getStatusCode()) {
            assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.BAD_REQUEST.getStatusCode());
        }
    }
     
     public void testDeleteReviewRecordPair() {
         // get n unreviewed pair
         List<ReviewRecordPair> reviewRecordPairs = 
                 getWebResource().path("person-query-resource")
                     .path("loadAllUnreviewedPersonLinks")
                     .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                     .accept(MediaType.APPLICATION_JSON)
                     .get(new GenericType<List<ReviewRecordPair>>(){});
         assertTrue("Failed to retrieve ReviewRecordPairs list.", reviewRecordPairs != null && reviewRecordPairs.size() > 0);
         
         ReviewRecordPair reviewRecordPairFound = reviewRecordPairs.get(0);
         for (ReviewRecordPair pair : reviewRecordPairs) {
             Person left = pair.getPersonLeft();
             Person right = pair.getPersonRight();
             if (left.getAddress1().equals("TEMP2") || right.getAddress1().equals("TEMP2")) {
                 reviewRecordPairFound = pair;
             }
         }     
         
         ClientResponse response = getWebResource().path("person-manager-resource")
                 .path("deleteReviewRecordPair")
                 .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                 .accept(MediaType.APPLICATION_JSON)
                 .put(ClientResponse.class, reviewRecordPairFound);

         if (response.getStatus() != Status.OK.getStatusCode()) {
             assertFalse("Incorrect status code received of " + response, false);
         } 
    }
    
    public void testLinkPersons() {
 

        // person One                 
       Person personOne = findTestPerson("Odysseas", "TEMP2");         
       
       // person Two
       Person personTwo = findTestPerson("Odysseas", "TEMP3");  
       
       // person Three
       Person personThree = findTestPerson("Odysseas", "TEMP4");  
        
       // add an unreviewed pair for One Two
       ClientResponse response = addReviewRecordPair(personOne, personTwo);      
       if (response.getStatus() != Status.OK.getStatusCode()) {
           assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.BAD_REQUEST.getStatusCode());
       }
       
       // add an unreviewed pair for One Three
       response = addReviewRecordPair(personOne, personThree);      
       if (response.getStatus() != Status.OK.getStatusCode()) {
           assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.BAD_REQUEST.getStatusCode());
       }      
       
        // get a Unreviewed pair
        List<ReviewRecordPair> reviewRecordPairs = 
                getWebResource().path("person-query-resource")
                    .path("loadAllUnreviewedPersonLinks")
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    .accept(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<ReviewRecordPair>>(){});
        assertTrue("Failed to retrieve ReviewRecordPairs list.", reviewRecordPairs != null && reviewRecordPairs.size() > 0);
        
        for (ReviewRecordPair pair : reviewRecordPairs) {
            Person left = pair.getPersonLeft();
            Person right = pair.getPersonRight();
            if (left.getAddress1().equals("TEMP3") || right.getAddress1().equals("TEMP3") ||
                left.getAddress1().equals("TEMP4") || right.getAddress1().equals("TEMP4")) {
            
                PersonLink personLink = ConvertUtil.getPersonLinkFromReviewRecordPair(pair);

                // link persons
                response = getWebResource().path("person-manager-resource")
                .path("linkPersons")
                .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                .accept(MediaType.APPLICATION_JSON)
                .put(ClientResponse.class, personLink);
            
                if (response.getStatus() != Status.OK.getStatusCode()) {
                    assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.BAD_REQUEST.getStatusCode());
                }   
            }
        }                
    }   

    public void testMergePersons() {
        
        // person surviving
        Person personSurviving = findTestPerson("Odysseas", "TEMP3");      
 
        // person retired
        Person personRetired = findTestPerson("Odysseas", "TEMP4");        
        

        if( personRetired == null || personSurviving == null ) {
            assertNotNull("Unable to find test person", null);   
            return;
        }
        
        PersonIdentifier personIdentifierRetired = personRetired.getPersonIdentifiers().iterator().next();; 
        PersonIdentifier personIdentifierSurviving = personSurviving.getPersonIdentifiers().iterator().next();; 
        
        MergePersonsRequest mergePersonsRequest = new MergePersonsRequest(personIdentifierRetired, personIdentifierSurviving);
        
        ClientResponse response = getWebResource().path("person-manager-resource")
        .path("mergePersons")
        .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        .accept(MediaType.APPLICATION_JSON)
        .put(ClientResponse.class, mergePersonsRequest);

        if (response.getStatus() != Status.OK.getStatusCode()) {
            assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.NOT_FOUND.getStatusCode());
        }
    }
    
    public void testUnlinkPersons() {
        Person person = findTestPerson("Odysseas", "TEMP2");   
 
        // get a person link
        List<PersonLink> personLinks = getWebResource().path("person-query-resource")
                    .path("getPersonLinks")
                    .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
                    .accept(MediaType.APPLICATION_XML)
                    .post(new GenericType<List<PersonLink>>(){}, person);
        assertNotNull("Unable to retrieve person links using the person: ", personLinks);    
        PersonLink personLink = personLinks.get(0);

        // unlink persons
        ClientResponse response = getWebResource().path("person-manager-resource")
        .path("unlinkPersons")
        .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        .accept(MediaType.APPLICATION_JSON)
        .put(ClientResponse.class, personLink);
    
        if (response.getStatus() != Status.OK.getStatusCode()) {
            assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.BAD_REQUEST.getStatusCode());
        }          
    }
 
    public void testDeletePerson() {
        Person person = findTestPerson("Odysseas", "TEMP2");  
        // PersonIdentifier personIdentifier = person.getPersonIdentifiers().iterator().next();; 

        // remove person completely
        ClientResponse response = getWebResource().path("person-manager-resource")
        .path("removePersonById")
        .queryParam("personId", person.getPersonId().toString())
        .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        .accept(MediaType.APPLICATION_JSON)
        .post(ClientResponse.class);

        if (response.getStatus() != Status.OK.getStatusCode()) {
            assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.NOT_FOUND.getStatusCode());
        }
    }
    
    public void testDeletePersonById() {
        Person person = findTestPerson("Odysseas", "TEMP3");  
        
        ClientResponse response = getWebResource().path("person-manager-resource")
        .path("deletePersonById")
        .header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        .accept(MediaType.APPLICATION_JSON)
        .put(ClientResponse.class, person);

        if (response.getStatus() != Status.OK.getStatusCode()) {
            assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.NOT_FOUND.getStatusCode());
        }
    }

	public void testAddIdentifierDomain() {
		IdentifierDomain identifierDomain = new IdentifierDomain();
						identifierDomain.setNamespaceIdentifier("TEST");
						identifierDomain.setIdentifierDomainName("TEMP-TEST"); 
						
		ClientResponse response = getWebResource().path("person-manager-resource")
		.path("addIdentifierDomain")
		.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
		.accept(MediaType.APPLICATION_JSON)
		.put(ClientResponse.class, identifierDomain);

		if (response.getStatus() != Status.OK.getStatusCode()) {
			assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.CONFLICT.getStatusCode());
		}
	}
	
	public void testUpdateIdentifierDomain() {
    	List<IdentifierDomain> domains = 
    			getWebResource().path("person-query-resource")
    				.path("getIdentifierDomains")
    				.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
    				.accept(MediaType.APPLICATION_JSON)
    				.get(new GenericType<List<IdentifierDomain>>(){});
    	assertTrue("Failed to retrieve domains list.",
    			domains != null && domains.size() > 0);
    	
    	IdentifierDomain identifierDomain = null;
    	for (IdentifierDomain domain : domains) {
    		if (domain.getIdentifierDomainName().startsWith("TEMP-TEST")) {
    			identifierDomain = domain;
    		}
    	}
    	if (identifierDomain == null) {
    		return;
    	}
			
    	identifierDomain.setIdentifierDomainName("TEMP1-TEST");
    	
		ClientResponse response = getWebResource().path("person-manager-resource")
		.path("updateIdentifierDomain")
		.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
		.accept(MediaType.APPLICATION_JSON)
		.put(ClientResponse.class, identifierDomain);

		if (response.getStatus() != Status.OK.getStatusCode()) {
			assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.BAD_REQUEST.getStatusCode());
		}
	}
	
	public void testDeleteIdentifierDomain() {
    	List<IdentifierDomain> domains = 
    			getWebResource().path("person-query-resource")
    				.path("getIdentifierDomains")
    				.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
    				.accept(MediaType.APPLICATION_JSON)
    				.get(new GenericType<List<IdentifierDomain>>(){});
    	assertTrue("Failed to retrieve domains list.",
    			domains != null && domains.size() > 0);
    	
    	IdentifierDomain identifierDomain = null;
    	for (IdentifierDomain domain : domains) {
    		if (domain.getIdentifierDomainName().startsWith("TEMP1-TEST")) {
    			identifierDomain = domain;
    		}
    	}
    	if (identifierDomain == null) {
    		return;
    	}
    	
		ClientResponse response = getWebResource().path("person-manager-resource")
		.path("deleteIdentifierDomain")
		.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
		.accept(MediaType.APPLICATION_JSON)
		.put(ClientResponse.class, identifierDomain);

		if (response.getStatus() != Status.OK.getStatusCode()) {
			assertFalse("Incorrect status code received of " + response, response.getStatus() == Status.BAD_REQUEST.getStatusCode());
		}
	}
	
	public void testAddIdentifierDomainAttribute() {
      	List<IdentifierDomain> domains = 
    			getWebResource().path("person-query-resource")
    				.path("getIdentifierDomains")
    				.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
    				.accept(MediaType.APPLICATION_JSON)
    				.get(new GenericType<List<IdentifierDomain>>(){});
    	assertTrue("Failed to retrieve domains list.", domains != null && domains.size() > 0);

       	IdentifierDomain id = null;
    	for (IdentifierDomain domain : domains) {
    		if (domain.getIdentifierDomainName().startsWith("IHENA")) {
    			id = domain;
    		}
    	}
    	if (id == null) {
    		// no this kind of IdentifierDomain
    		return;
    	}   
    	
    	IdentifierDomainAttributeRequest identifierDomainAttributeRequest = new IdentifierDomainAttributeRequest(id, "IHENA", "200");	
    	
		ClientResponse response = getWebResource().path("person-manager-resource")
		.path("addIdentifierDomainAttribute")
		.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
		.accept(MediaType.APPLICATION_JSON)
		.put(ClientResponse.class, identifierDomainAttributeRequest);

		if (response.getStatus() != Status.OK.getStatusCode()) {
			assertFalse("Incorrect status code received of " + response, false);
		}
	}
	
	public void testUpdateIdentifierDomainAttribute() {
       	List<IdentifierDomain> domains = 
    			getWebResource().path("person-query-resource")
    				.path("getIdentifierDomains")
    				.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
    				.accept(MediaType.APPLICATION_JSON)
    				.get(new GenericType<List<IdentifierDomain>>(){});
    	assertTrue("Failed to retrieve domains list.", domains != null && domains.size() > 0);

       	IdentifierDomain id = null;
    	for (IdentifierDomain domain : domains) {
    		if (domain.getIdentifierDomainName().startsWith("IHENA")) {
    			id = domain;
    		}
    	}
    	if (id == null) {
    		return;
    	}
    
    	IdentifierDomainAttributeRequest identifierDomainAttributeRequest= new IdentifierDomainAttributeRequest(id, "IHENA");
    	
    	IdentifierDomainAttribute dentifierDomainAttribute = 
    			getWebResource().path("person-query-resource")
        			.path("getIdentifierDomainAttribute")
        			.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        			.accept(MediaType.APPLICATION_JSON)
        			.post(IdentifierDomainAttribute.class, identifierDomainAttributeRequest);
    	
    	if(dentifierDomainAttribute == null) {
    		return;
    	}
    	
    	dentifierDomainAttribute.setAttributeName("IHENA1");
    	
		ClientResponse response = getWebResource().path("person-manager-resource")
		.path("updateIdentifierDomainAttribute")
		.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
		.accept(MediaType.APPLICATION_JSON)
		.put(ClientResponse.class, dentifierDomainAttribute);

		if (response.getStatus() != Status.OK.getStatusCode()) {
			assertFalse("Incorrect status code received of " + response, false);
		}
	}
	
	public void testRemoveIdentifierDomainAttribute() {
       	List<IdentifierDomain> domains = 
    			getWebResource().path("person-query-resource")
    				.path("getIdentifierDomains")
    				.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
    				.accept(MediaType.APPLICATION_JSON)
    				.get(new GenericType<List<IdentifierDomain>>(){});
    	assertTrue("Failed to retrieve domains list.", domains != null && domains.size() > 0);

       	IdentifierDomain id = null;
    	for (IdentifierDomain domain : domains) {
    		if (domain.getIdentifierDomainName().startsWith("IHENA")) {
    			id = domain;
    		}
    	}
    	if (id == null) {
    		return;
    	}
    
    	IdentifierDomainAttributeRequest identifierDomainAttributeRequest= new IdentifierDomainAttributeRequest(id, "IHENA1");
    	
    	IdentifierDomainAttribute dentifierDomainAttribute = 
    			getWebResource().path("person-query-resource")
        			.path("getIdentifierDomainAttribute")
        			.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
        			.accept(MediaType.APPLICATION_JSON)
        			.post(IdentifierDomainAttribute.class, identifierDomainAttributeRequest);
    	
    	if(dentifierDomainAttribute == null) {
    		return;
    	}
    	
    	
		ClientResponse response = getWebResource().path("person-manager-resource")
		.path("removeIdentifierDomainAttribute")
		.header(OPENEMPI_SESSION_KEY_HEADER, getSessionKey())
		.accept(MediaType.APPLICATION_JSON)
		.put(ClientResponse.class, dentifierDomainAttribute);

		if (response.getStatus() != Status.OK.getStatusCode()) {
			assertFalse("Incorrect status code received of " + response, false);
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
    
    public ClientResponse addReviewRecordPair(Person personLeft, Person personRight) {           
       
       // ReviewRecordPair
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

       return response;
   }
}
