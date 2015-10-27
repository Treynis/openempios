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
package org.openhie.openempi.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Gender;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.model.ReviewRecordPair;
import org.openhie.openempi.service.BaseServiceTestCase;
import org.openhie.openempi.service.PersonManagerService;
import org.openhie.openempi.service.PersonQueryService;
import org.openhie.openempi.service.impl.PersonQueryServiceAdapter;
import org.openhie.openempi.util.ConvertUtil;

public class EntityQueryServiceAdapterTest extends BaseServiceTestCase
{

	public void testGetPersonModelAllAttributeNames() {		
		PersonQueryServiceAdapter adapter = (PersonQueryServiceAdapter) Context.getApplicationContext().getBean("personQueryServiceAdapter");		
		try {
		    assertNotNull("The adapter is invalid. Please check with your system administrator.", adapter);
		    
			List<String> names = adapter.getPersonModelAllAttributeNames();		
		    assertTrue("Not found Model Attribute Names.", names.size() > 0);
		    
			return;					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testAddPerson() {
		PersonManagerService adapterManager = (PersonManagerService) Context.getApplicationContext().getBean("personManagerServiceAdapter");		
		PersonQueryService adapterQuery = (PersonQueryService) Context.getApplicationContext().getBean("personQueryServiceAdapter");
		
		try {	
		    assertNotNull("The manager adapter is invalid. Please check with your system administrator.", adapterManager);	
		    assertNotNull("The query adapter is invalid. Please check with your system administrator.", adapterQuery);
		    
			// Add person Albert Hon
			Person person = buildTestPerson(adapterQuery, "Albert","Hon","1961-01-01","M", "IHERED", "IHERED-999");
			adapterManager.addPerson(person);

			List<Person> persons = adapterQuery.findPersonsByAttributes(person);
			assertTrue("Not found Added person.", persons.size() > 0);	
			
			// Add person Charles Smith
			person = buildTestPerson(adapterQuery, "Charles","Smith","1961-02-01","M", "IHERED", "IHERED-1004");
			adapterManager.addPerson(person);

			persons = adapterQuery.findPersonsByAttributes(person);
			assertTrue("Not found Added person.", persons.size() > 0);	
			
			return;									
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void testAddAnotherPerson() {
		PersonManagerService adapterManager = (PersonManagerService) Context.getApplicationContext().getBean("personManagerServiceAdapter");		
		PersonQueryService adapterQuery = (PersonQueryService) Context.getApplicationContext().getBean("personQueryServiceAdapter");
		
		try {	
		    assertNotNull("The manager adapter is invalid. Please check with your system administrator.", adapterManager);	
		    assertNotNull("The query adapter is invalid. Please check with your system administrator.", adapterQuery);
		    
			// Add person another Albert Hon
			Person person = buildTestPerson(adapterQuery, "Albert","Hon","1961-01-01","M", "IHERED", "IHERED-888");
			adapterManager.addPerson(person);

			List<Person> persons = adapterQuery.findPersonsByAttributes(person);
			assertTrue("Not found Added person.", persons.size() > 0);	
			
			return;									
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void testLoadPerson() {		
		PersonQueryServiceAdapter adapter = (PersonQueryServiceAdapter) Context.getApplicationContext().getBean("personQueryServiceAdapter");
		
		try {
		    assertNotNull("The adapter is invalid. Please check with your system administrator.", adapter);
		    
			Person person = new Person();
			person.setGivenName("Albert");
			person.setFamilyName("Hon");
			
			Gender gender = new Gender();
			gender.setGenderCode("M");
			person.setGender(gender);
			
			String dob = "1961-01-01";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date dateOfBirth = sdf.parse(dob);
			person.setDateOfBirth(dateOfBirth);
			List<Person> persons = adapter.findPersonsByAttributes(person);

			Person loadedPerson = null;
			if( persons != null && persons.size() > 0 ) {
				loadedPerson = adapter.loadPerson(persons.get(0).getPersonId());
				log.debug("Person is " + loadedPerson.getGivenName()+ " "+loadedPerson.getFamilyName());		
			}
	
			return;		
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testLoadPersons() {
		PersonQueryServiceAdapter adapter = (PersonQueryServiceAdapter) Context.getApplicationContext().getBean("personQueryServiceAdapter");
		
		try {
		    assertNotNull("The adapter is invalid. Please check with your system administrator.", adapter);
		    
			Person person = new Person();
			person.setGivenName("Charles");
			person.setFamilyName("Smith");
			
			Gender gender = new Gender();
			gender.setGenderCode("M");
			person.setGender(gender);
			
			String dob = "1961-02-01";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date dateOfBirth = sdf.parse(dob);
			person.setDateOfBirth(dateOfBirth);
			List<Person> persons = adapter.findPersonsByAttributesPaged(person, 0, 5);
		
			List<Integer> listIds = new ArrayList<Integer>();
			for( Person personFound: persons) {
				listIds.add(personFound.getPersonId());
			}
			List<Person> loadedPersons = adapter.loadPersons(listIds);
			for( Person p : loadedPersons) {
				 log.debug("Person is " + p.getGivenName()+ " "+p.getFamilyName());		
			}							
			return;					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testFindPersonById() {
		PersonQueryService personQueryService = Context.getPersonQueryService();
		
		PersonQueryServiceAdapter adapter = (PersonQueryServiceAdapter) Context.getApplicationContext().getBean("personQueryServiceAdapter");
				
		try {
		    assertNotNull("The adapter is invalid. Please check with your system administrator.", adapter);
		    
			List<IdentifierDomain> domains = personQueryService.getIdentifierDomains(); 
			assertTrue("No identifier domains in the system.", domains.size() > 0);
			
			IdentifierDomain personDomain = null;
			for(IdentifierDomain domain: domains ) {
				if(domain.getIdentifierDomainName().equals("IHERED") ) {
					personDomain = domain;
				}
			}
		
			org.openhie.openempi.model.PersonIdentifier identifier = new org.openhie.openempi.model.PersonIdentifier();
			identifier.setIdentifier("IHERED-999");
			identifier.setIdentifierDomain(personDomain);
						
			Person person = adapter.findPersonById(identifier);
			if( person != null ) {
				log.debug("Person is " + person.getGivenName()+ " "+person.getFamilyName());			
			}
			return;					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testFindPersonsById() {
		PersonQueryService personQueryService = Context.getPersonQueryService();
		
		PersonQueryServiceAdapter adapter = (PersonQueryServiceAdapter) Context.getApplicationContext().getBean("personQueryServiceAdapter");
		
		try {
		    assertNotNull("The adapter is invalid. Please check with your system administrator.", adapter);
		    
			List<IdentifierDomain> domains = personQueryService.getIdentifierDomains(); 
			assertTrue("No identifier domains in the system.", domains.size() > 0);
			
			IdentifierDomain personDomain = null;
			for(IdentifierDomain domain: domains ) {
				if(domain.getIdentifierDomainName().equals("IHERED") ) {
					personDomain = domain;
				}
			}
			
			org.openhie.openempi.model.PersonIdentifier identifier = new org.openhie.openempi.model.PersonIdentifier();
			identifier.setIdentifier("IHERED-1004");
			identifier.setIdentifierDomain(personDomain);
						
			List<Person> persons = adapter.findPersonsById(identifier);
			for( Person person : persons) {
				 log.debug("Person is " + person.getGivenName()+ " "+person.getFamilyName());		
			}					
			return;					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testLoadAllPersonsPaged() {		
		PersonQueryServiceAdapter adapter = (PersonQueryServiceAdapter) Context.getApplicationContext().getBean("personQueryServiceAdapter");

		try {
		    assertNotNull("The adapter is invalid. Please check with your system administrator.", adapter);		
		    
			List<Person> persons = adapter.loadAllPersonsPaged(2, 5);
			for( Person person : persons) {
				 log.debug("Person is " + person.getGivenName()+ " "+person.getFamilyName());		
			}							
			return;					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testFindPersonsByAttributes() {		
		PersonQueryServiceAdapter adapter = (PersonQueryServiceAdapter) Context.getApplicationContext().getBean("personQueryServiceAdapter");

		try {
		    assertNotNull("The adapter is invalid. Please check with your system administrator.", adapter);		
		    
			Person person = new Person();
			person.setGivenName("Albert");
			person.setFamilyName("Hon");
			
			Gender gender = new Gender();
			gender.setGenderCode("M");
			person.setGender(gender);
			
			String dob = "1961-01-01";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date dateOfBirth = sdf.parse(dob);
			person.setDateOfBirth(dateOfBirth);
			List<Person> persons = adapter.findPersonsByAttributes(person);
			for( Person p : persons) {
				 log.debug("Person is " + p.getGivenName()+ " "+p.getFamilyName());		
			}							
			return;					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testFindPersonsByAttributesPaged() {		
		PersonQueryServiceAdapter adapter = (PersonQueryServiceAdapter) Context.getApplicationContext().getBean("personQueryServiceAdapter");

		try {
		    assertNotNull("The adapter is invalid. Please check with your system administrator.", adapter);		
		    
			Person person = new Person();
			person.setGivenName("Charles");
			person.setFamilyName("Smith");
			
			Gender gender = new Gender();
			gender.setGenderCode("M");
			person.setGender(gender);
			
			String dob = "1961-02-01";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date dateOfBirth = sdf.parse(dob);
			person.setDateOfBirth(dateOfBirth);
			List<Person> persons = adapter.findPersonsByAttributesPaged(person, 0, 5);
			for( Person p : persons) {
				 log.debug("Person is " + p.getGivenName()+ " "+p.getFamilyName());		
			}							
			return;					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testLoadUnreviewedPersonLinks() {		
		PersonQueryServiceAdapter adapter = (PersonQueryServiceAdapter) Context.getApplicationContext().getBean("personQueryServiceAdapter");
		
		try {
		    assertNotNull("The adapter is invalid. Please check with your system administrator.", adapter);
		    
		    List<ReviewRecordPair> reviewRecordPairs = adapter.loadUnreviewedPersonLinks(2);

			assertTrue("No Review Record Pairs in the system.", reviewRecordPairs.size() > 0);
			
			
			for (ReviewRecordPair reviewRecordPair : reviewRecordPairs) {	
				
				ReviewRecordPair pair = adapter.loadReviewRecordPair(reviewRecordPair.getReviewRecordPairId());
				
			    assertNotNull("Review Record Pair not found", pair);
			}	
			
			return;		
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
/*	
	public void testRemovePerson() {
		PersonManagerService adapterManager = (PersonManagerService) Context.getApplicationContext().getBean("personManagerServiceAdapter");		
		PersonQueryService adapterQuery = (PersonQueryService) Context.getApplicationContext().getBean("personQueryServiceAdapter");
		
		try {
			
		    assertNotNull("The manager adapter is invalid. Please check with your system administrator.", adapterManager);	
		    assertNotNull("The query adapter is invalid. Please check with your system administrator.", adapterQuery);
		    
			// person Albert Hon
			Person person = buildTestPerson(adapterQuery, "Albert","Hon","1961-01-01","M", "IHERED", "IHERED-999");
						
			// Find person
			List<Person> persons = adapterQuery.findPersonsByAttributes(person);

			Person foundPerson = null;
			for(Person p: persons ) {
				foundPerson = adapterQuery.loadPerson(p.getPersonId());	
				
				// Remove person			
				adapterManager.removePerson(foundPerson.getPersonId());
			}			

			persons = adapterQuery.findPersonsByAttributes(person);
			assertTrue("Found removed persons.", persons.size() == 0);	
			
			// person Charles Smith
			person = buildTestPerson(adapterQuery, "Charles","Smith","1961-02-01","M", "IHERED", "IHERED-1004");
			
			// Find person
			persons = adapterQuery.findPersonsByAttributes(person);

			foundPerson = null;
			for(Person p: persons ) {
				foundPerson = adapterQuery.loadPerson(p.getPersonId());	
				
				// Remove person			
				adapterManager.removePerson(foundPerson.getPersonId());
			}			

			persons = adapterQuery.findPersonsByAttributes(person);
			assertTrue("Found removed persons.", persons.size() == 0);	
			
			return;			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/
	
	protected Person buildTestPerson(PersonQueryService personQueryService, String givenName, String familyName, String dob, String genderCode, String domainName, String identifier) {
		Person person = new Person();
		person.setGivenName(givenName);
		person.setFamilyName(familyName);
		person.setAddress1("1000 Openempi Drive");
		person.setCity("SYSNET");
		person.setState("Virginia");
		
		Gender gender = new Gender();
		gender.setGenderCode(genderCode);
		person.setGender(gender);

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date dateOfBirth = sdf.parse(dob);
			person.setDateOfBirth(dateOfBirth);			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		List<IdentifierDomain> domains = personQueryService.getIdentifierDomains();
		if (domains == null || domains.size() == 0) {
			assertTrue("There are no identifier domains in the system.", true);
		}

		IdentifierDomain personDomain = null;
		for(IdentifierDomain domain: domains ) {
			if(domain.getIdentifierDomainName().equals(domainName) ) {
				personDomain = domain;
			}
		}
		
		if( personDomain == null) {
			personDomain = new IdentifierDomain();
			personDomain.setIdentifierDomainName(domainName);
			personDomain.setNamespaceIdentifier(domainName);
		}
		
		PersonIdentifier pi = new PersonIdentifier();
		pi.setIdentifier(identifier);
		pi.setIdentifierDomain(personDomain);

		person.addPersonIdentifier(pi);
				
		return person;
	}
}
