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


import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Gender;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.service.BaseServiceTestCase;
import org.openhie.openempi.service.PersonManagerService;
import org.openhie.openempi.service.PersonQueryService;

public class EntityManagerServiceAdapterTest extends BaseServiceTestCase
{

	public void testAddPerson() {
		PersonManagerService adapterManager = (PersonManagerService) Context.getApplicationContext().getBean("personManagerServiceAdapter");		
		PersonQueryService adapterQuery = (PersonQueryService) Context.getApplicationContext().getBean("personQueryServiceAdapter");
		
		try {	
		    assertNotNull("The manager adapter is invalid. Please check with your system administrator.", adapterManager);	
		    assertNotNull("The query adapter is invalid. Please check with your system administrator.", adapterQuery);
		    
			// Add person
			Person person = buildTestPerson(adapterQuery, "Albert","Test","1981-05-05","M","555-55-5555");
			adapterManager.addPerson(person);

			List<Person> persons = adapterQuery.findPersonsByAttributes(person);
			assertTrue("Not found Added person.", persons.size() > 0);	
			
			return;									
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void testUpdatePersonById() {
		PersonManagerService adapterManager = (PersonManagerService) Context.getApplicationContext().getBean("personManagerServiceAdapter");		
		PersonQueryService adapterQuery = (PersonQueryService) Context.getApplicationContext().getBean("personQueryServiceAdapter");
		
		try {
			
		    assertNotNull("The manager adapter is invalid. Please check with your system administrator.", adapterManager);	
		    assertNotNull("The query adapter is invalid. Please check with your system administrator.", adapterQuery);
		    
			// Find person
			Person person = buildTestPerson(adapterQuery, "Albert","Test","1981-05-05","M","555-55-5555");
			List<Person> persons = adapterQuery.findPersonsByAttributes(person);
			assertTrue("Not found person.", persons.size() > 0);	
			
			Person foundPerson = null;
			if( persons != null && persons.size() > 0 ) {
				foundPerson = adapterQuery.loadPerson(persons.get(0).getPersonId());
			}	
			
			// Update person
			if( foundPerson != null ) {
				foundPerson.setFamilyName("Updated");
				
				adapterManager.updatePersonById(foundPerson);
			}

			persons = adapterQuery.findPersonsByAttributes(foundPerson);
			assertTrue("Not found Updated person.", persons.size() > 0);	
			
			return;			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testDeletePersonById() {
		PersonManagerService adapterManager = (PersonManagerService) Context.getApplicationContext().getBean("personManagerServiceAdapter");		
		PersonQueryService adapterQuery = (PersonQueryService) Context.getApplicationContext().getBean("personQueryServiceAdapter");
		
		try {
			
		    assertNotNull("The manager adapter is invalid. Please check with your system administrator.", adapterManager);	
		    assertNotNull("The query adapter is invalid. Please check with your system administrator.", adapterQuery);
		    
			// Find person
			Person person = buildTestPerson(adapterQuery, "Albert","Updated","1981-05-05","M","555-55-5555");
			List<Person> persons = adapterQuery.findPersonsByAttributes(person);
			assertTrue("Not found person.", persons.size() > 0);	
			
			Person foundPerson = null;
			for(Person p: persons ) {
				foundPerson = adapterQuery.loadPerson(p.getPersonId());	
				
				// Delete person			
				adapterManager.deletePersonById(foundPerson);
			}			

			persons = adapterQuery.findPersonsByAttributes(person);
			assertTrue("Found deleted persons.", persons.size() == 0);	
			
			return;			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testRemovePerson() {
		PersonManagerService adapterManager = (PersonManagerService) Context.getApplicationContext().getBean("personManagerServiceAdapter");		
		PersonQueryService adapterQuery = (PersonQueryService) Context.getApplicationContext().getBean("personQueryServiceAdapter");
		
		try {
			
		    assertNotNull("The manager adapter is invalid. Please check with your system administrator.", adapterManager);	
		    assertNotNull("The query adapter is invalid. Please check with your system administrator.", adapterQuery);
		    
		    
			Person person = buildTestPerson(adapterQuery, "Albert","Remove","1981-05-05","M","555-55-5555");
			
			// Add person
			adapterManager.addPerson(person);
			
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
			
			return;			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected Person buildTestPerson(PersonQueryService personQueryService, String givenName, String familyName, String dob, String genderCode, String ssn) {
		Person person = new Person();
		person.setGivenName(givenName);
		person.setFamilyName(familyName);
		person.setAddress1("1000 Openempi Drive");
		person.setCity("SYSNET");
		person.setState("Virginia");
		person.setSsn(ssn);
		
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
			if(domain.getIdentifierDomainName().equals("IHENA") ) {
				personDomain = domain;
			}
		}
		
		PersonIdentifier pi = new PersonIdentifier();
		pi.setIdentifier(ssn);
		pi.setIdentifierDomain(personDomain);

		person.addPersonIdentifier(pi);
				
		return person;
	}
}
