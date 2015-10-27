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
package org.openhie.openempi.matching;

import java.util.Date;
import java.util.List;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.Constants;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Gender;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.PersonLink;
import org.openhie.openempi.service.BaseServiceTestCase;

public class MatchingServiceDeterministicAlgorithmRulesTest extends BaseServiceTestCase
{
    private Gender gender;
    private Date birthDate;
    
	static {
		System.setProperty(Constants.OPENEMPI_EXTENSION_CONTEXTS_FILENAME, "openempi-extension-contexts.properties");
		System.setProperty(Constants.OPENEMPI_CONFIGURATION_FILENAME, "mpi-config-exact-rules.xml");
	}
	
	private static java.util.List<Integer> personIds = new java.util.ArrayList<Integer>();

	public void testAllRules() {
	    gender = new Gender(1, "Male");
	    gender.setGenderCode("M");
	    
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(1977, 2, 14);
        birthDate = calendar.getTime();

	    ruleOne();
        removeTestRecords();
        ruleTwo();
        removeTestRecords();
        ruleThree();
        removeTestRecords();
        ruleFour();
        removeTestRecords();
        ruleFive();
        removeTestRecords();
        ruleSix();
        removeTestRecords();
        ruleSeven();
        removeTestRecords();
        ruleEight();
        removeTestRecords();
        ruleNine();
        removeTestRecords();
        ruleTen();
        removeTestRecords();
        ruleEleven();
        removeTestRecords();
        ruleTwelve();
        removeTestRecords();
        ruleThirteen();
        removeTestRecords();
	}
	
	private void ruleOne() {
		System.out.println("Transactional support for this test has rollback set to " + this.isDefaultRollback());
		this.setDefaultRollback(true);
		
		log.debug("Testing the rule: dateOfBirth, familyName, gender, givenName, middleName, phoneNumber, ssn");
		Person personOne = new Person();
        personOne.setDateOfBirth(birthDate);
        personOne.setFamilyName("Martinez");
        personOne.setGender(gender);
        personOne.setGivenName("Javier");
        personOne.setMiddleName("Javi");
        personOne.setPhoneNumber("111-111-1111");
        personOne.setSsn("111-11-1111");

		PersonIdentifier pi = new PersonIdentifier();
		pi.setIdentifier("ABCDE");
		IdentifierDomain id = new IdentifierDomain();
		id.setNamespaceIdentifier("HIMSS2005");
		pi.setIdentifierDomain(id);
		personOne.addPersonIdentifier(pi);
		
		Person personTwo = new Person();
		personTwo.setDateOfBirth(birthDate);
		personTwo.setFamilyName("Martinez");
		personTwo.setGender(gender);
		personTwo.setGivenName("Javier");
		personTwo.setMiddleName("Javi");
		personTwo.setPhoneNumber("111-111-1111");
		personTwo.setSsn("111-11-1111");
		
		pi = new PersonIdentifier();
		pi.setIdentifier("AB12345");
		id = new IdentifierDomain();
		id.setNamespaceIdentifier("XREF2005");
		pi.setIdentifierDomain(id);
		personTwo.addPersonIdentifier(pi);		
		
		twoPersonMatch("Rule ONE", personOne, personTwo);
	}

    private void ruleTwo() {
        System.out.println("Transactional support for this test has rollback set to " + this.isDefaultRollback());
        this.setDefaultRollback(true);
        
        log.debug("Testing the rule: dateOfBirth, familyName, gender, givenName, middleName, phoneNumber, last 4 of SSN");
        Person personOne = new Person();
        personOne.setDateOfBirth(birthDate);
        personOne.setFamilyName("Martinez");
        personOne.setGender(gender);
        personOne.setGivenName("Javier");
        personOne.setMiddleName("Javi");
        personOne.setPhoneNumber("111-111-1111");
        personOne.setSsn("111-11-1111");

        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier("ABCDE-2");
        IdentifierDomain id = new IdentifierDomain();
        id.setNamespaceIdentifier("HIMSS2005");
        pi.setIdentifierDomain(id);
        personOne.addPersonIdentifier(pi);
                
        Person personTwo = new Person();
        personTwo.setDateOfBirth(birthDate);
        personTwo.setFamilyName("Martinez");
        personTwo.setGender(gender);
        personTwo.setGivenName("Javier");
        personTwo.setMiddleName("Javi");
        personTwo.setPhoneNumber("111-111-1111");
        personTwo.setSsn("222-22-1111");
        
        pi = new PersonIdentifier();
        pi.setIdentifier("AB12345-2");
        id = new IdentifierDomain();
        id.setNamespaceIdentifier("XREF2005");
        pi.setIdentifierDomain(id);
        personTwo.addPersonIdentifier(pi);      
        
        twoPersonMatch("Rule TWO", personOne, personTwo);
    }   

    private void ruleThree() {
        System.out.println("Transactional support for this test has rollback set to " + this.isDefaultRollback());
        this.setDefaultRollback(true);
        
        log.debug("Testing the rule: address1, dateOfBirth, familyName, gender, givenName, middleName, ssn");
        Person personOne = new Person();
        personOne.setAddress1("1010 Sometown street");
        personOne.setDateOfBirth(birthDate);
        personOne.setFamilyName("Martinez");
        personOne.setGender(gender);
        personOne.setGivenName("Javier");
        personOne.setMiddleName("Javi");
        personOne.setSsn("111-11-1111");

        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier("ABCDE-3");
        IdentifierDomain id = new IdentifierDomain();
        id.setNamespaceIdentifier("HIMSS2005");
        pi.setIdentifierDomain(id);
        personOne.addPersonIdentifier(pi);
                
        Person personTwo = new Person();
        personTwo.setAddress1("1010 Sometown street");
        personTwo.setDateOfBirth(birthDate);
        personTwo.setFamilyName("Martinez");
        personTwo.setGender(gender);
        personTwo.setGivenName("Javier");
        personTwo.setMiddleName("Javi");
        personTwo.setSsn("111-11-1111");
        
        pi = new PersonIdentifier();
        pi.setIdentifier("AB12345-3");
        id = new IdentifierDomain();
        id.setNamespaceIdentifier("XREF2005");
        pi.setIdentifierDomain(id);
        personTwo.addPersonIdentifier(pi);      
        
        twoPersonMatch("Rule THREE", personOne, personTwo);
    }   

    private void ruleFour() {
        System.out.println("Transactional support for this test has rollback set to " + this.isDefaultRollback());
        this.setDefaultRollback(true);
        
        log.debug("Testing the rule: address1, dateOfBirth, familyName, gender, givenName, middleName, Last4ofSSN");
        Person personOne = new Person();
        personOne.setAddress1("1010 Sometown street");
        personOne.setDateOfBirth(birthDate);
        personOne.setFamilyName("Martinez");
        personOne.setGender(gender);
        personOne.setGivenName("Javier");
        personOne.setMiddleName("Javi");
        personOne.setSsn("111-11-1111");

        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier("ABCDE-4");
        IdentifierDomain id = new IdentifierDomain();
        id.setNamespaceIdentifier("HIMSS2005");
        pi.setIdentifierDomain(id);
        personOne.addPersonIdentifier(pi);
                
        Person personTwo = new Person();
        personTwo.setAddress1("1010 Sometown street");
        personTwo.setDateOfBirth(birthDate);
        personTwo.setFamilyName("Martinez");
        personTwo.setGender(gender);
        personTwo.setGivenName("Javier");
        personTwo.setMiddleName("Javi");
        personTwo.setSsn("222-22-1111");
        
        pi = new PersonIdentifier();
        pi.setIdentifier("AB12345-4");
        id = new IdentifierDomain();
        id.setNamespaceIdentifier("XREF2005");
        pi.setIdentifierDomain(id);
        personTwo.addPersonIdentifier(pi);      
        
        twoPersonMatch("Rule FOUR", personOne, personTwo);
    }

    private void ruleFive() {
        System.out.println("Transactional support for this test has rollback set to " + this.isDefaultRollback());
        this.setDefaultRollback(true);
        
        log.debug("Testing the rule: address1, dateOfBirth, familyName, givenName, phoneNumber, ssn");
        Person personOne = new Person();
        personOne.setAddress1("1010 Sometown street");
        personOne.setDateOfBirth(birthDate);
        personOne.setFamilyName("Martinez");
        personOne.setGivenName("Javier");
        personOne.setPhoneNumber("111-111-1111");
        personOne.setSsn("111-11-1111");

        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier("ABCDE-5");
        IdentifierDomain id = new IdentifierDomain();
        id.setNamespaceIdentifier("HIMSS2005");
        pi.setIdentifierDomain(id);
        personOne.addPersonIdentifier(pi);
                
        Person personTwo = new Person();
        personTwo.setAddress1("1010 Sometown street");
        personTwo.setDateOfBirth(birthDate);
        personTwo.setFamilyName("Martinez");
        personTwo.setGivenName("Javier");
        personTwo.setPhoneNumber("111-111-1111");
        personTwo.setSsn("111-11-1111");
        
        pi = new PersonIdentifier();
        pi.setIdentifier("AB12345-5");
        id = new IdentifierDomain();
        id.setNamespaceIdentifier("XREF2005");
        pi.setIdentifierDomain(id);
        personTwo.addPersonIdentifier(pi);      
        
        twoPersonMatch("Rule FIVE", personOne, personTwo);
    }   

    private void ruleSix() {
        System.out.println("Transactional support for this test has rollback set to " + this.isDefaultRollback());
        this.setDefaultRollback(true);
        
        log.debug("Testing the rule: address1, dateOfBirth, familyName, givenName, phoneNumber, Last4ofSSN");
        Person personOne = new Person();
        personOne.setAddress1("1010 Sometown street");
        personOne.setDateOfBirth(birthDate);
        personOne.setFamilyName("Martinez");
        personOne.setGivenName("Javier");
        personOne.setPhoneNumber("111-111-1111");
        personOne.setSsn("111-11-1111");

        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier("ABCDE-6");
        IdentifierDomain id = new IdentifierDomain();
        id.setNamespaceIdentifier("HIMSS2005");
        pi.setIdentifierDomain(id);
        personOne.addPersonIdentifier(pi);
                
        Person personTwo = new Person();
        personTwo.setAddress1("1010 Sometown street");
        personTwo.setDateOfBirth(birthDate);
        personTwo.setFamilyName("Martinez");
        personTwo.setGivenName("Javier");
        personTwo.setPhoneNumber("111-111-1111");
        personTwo.setSsn("222-22-1111");
        
        pi = new PersonIdentifier();
        pi.setIdentifier("AB12345-6");
        id = new IdentifierDomain();
        id.setNamespaceIdentifier("XREF2005");
        pi.setIdentifierDomain(id);
        personTwo.addPersonIdentifier(pi);      
        
        twoPersonMatch("Rule SIX", personOne, personTwo);
    }
    
    private void ruleSeven() {
        System.out.println("Transactional support for this test has rollback set to " + this.isDefaultRollback());
        this.setDefaultRollback(true);
        
        log.debug("Testing the rule: dateOfBirth, familyName, gender, givenName, phoneNumber, ssn");
        Person personOne = new Person();
        personOne.setDateOfBirth(birthDate);
        personOne.setFamilyName("Martinez");
        personOne.setGender(gender);
        personOne.setGivenName("Javier");
        personOne.setPhoneNumber("111-111-1111");
        personOne.setSsn("111-11-1111");

        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier("ABCDE-7");
        IdentifierDomain id = new IdentifierDomain();
        id.setNamespaceIdentifier("HIMSS2005");
        pi.setIdentifierDomain(id);
        personOne.addPersonIdentifier(pi);
        
        Person personTwo = new Person();
        personTwo.setDateOfBirth(birthDate);
        personTwo.setFamilyName("Martinez");
        personTwo.setGender(gender);
        personTwo.setGivenName("Javier");
        personTwo.setPhoneNumber("111-111-1111");
        personTwo.setSsn("111-11-1111");
        
        pi = new PersonIdentifier();
        pi.setIdentifier("AB12345-7");
        id = new IdentifierDomain();
        id.setNamespaceIdentifier("XREF2005");
        pi.setIdentifierDomain(id);
        personTwo.addPersonIdentifier(pi);      
        
        twoPersonMatch("Rule SEVEN", personOne, personTwo);
    }

    private void ruleEight() {
        System.out.println("Transactional support for this test has rollback set to " + this.isDefaultRollback());
        this.setDefaultRollback(true);
        
        log.debug("Testing the rule: dateOfBirth, familyName, gender, givenName, phoneNumber, Last4ofSSN");
        Person personOne = new Person();
        personOne.setDateOfBirth(birthDate);
        personOne.setFamilyName("Martinez");
        personOne.setGender(gender);
        personOne.setGivenName("Javier");
        personOne.setPhoneNumber("111-111-1111");
        personOne.setSsn("111-11-1111");

        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier("ABCDE-8");
        IdentifierDomain id = new IdentifierDomain();
        id.setNamespaceIdentifier("HIMSS2005");
        pi.setIdentifierDomain(id);
        personOne.addPersonIdentifier(pi);
        
        Person personTwo = new Person();
        personTwo.setDateOfBirth(birthDate);
        personTwo.setFamilyName("Martinez");
        personTwo.setGender(gender);
        personTwo.setGivenName("Javier");
        personTwo.setPhoneNumber("111-111-1111");
        personTwo.setSsn("222-22-1111");
        
        pi = new PersonIdentifier();
        pi.setIdentifier("AB12345-8");
        id = new IdentifierDomain();
        id.setNamespaceIdentifier("XREF2005");
        pi.setIdentifierDomain(id);
        personTwo.addPersonIdentifier(pi);      
        
        twoPersonMatch("Rule EIGHT", personOne, personTwo);
    }

    private void ruleNine() {
        System.out.println("Transactional support for this test has rollback set to " + this.isDefaultRollback());
        this.setDefaultRollback(true);
        
        log.debug("Testing the rule: address1, dateOfBirth, familyName, gender, givenName, ssn");
        Person personOne = new Person();
        personOne.setAddress1("1010 Sometown street");
        personOne.setDateOfBirth(birthDate);
        personOne.setFamilyName("Martinez");
        personOne.setGender(gender);
        personOne.setGivenName("Javier");
        personOne.setSsn("111-11-1111");

        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier("ABCDE-9");
        IdentifierDomain id = new IdentifierDomain();
        id.setNamespaceIdentifier("HIMSS2005");
        pi.setIdentifierDomain(id);
        personOne.addPersonIdentifier(pi);
                
        Person personTwo = new Person();
        personTwo.setAddress1("1010 Sometown street");
        personTwo.setDateOfBirth(birthDate);
        personTwo.setFamilyName("Martinez");
        personTwo.setGender(gender);
        personTwo.setGivenName("Javier");
        personTwo.setSsn("111-11-1111");
        
        pi = new PersonIdentifier();
        pi.setIdentifier("AB12345-9");
        id = new IdentifierDomain();
        id.setNamespaceIdentifier("XREF2005");
        pi.setIdentifierDomain(id);
        personTwo.addPersonIdentifier(pi);      
        
        twoPersonMatch("Rule NINE", personOne, personTwo);
    }   


    private void ruleTen() {
        System.out.println("Transactional support for this test has rollback set to " + this.isDefaultRollback());
        this.setDefaultRollback(true);
        
        log.debug("Testing the rule: address1, dateOfBirth, familyName, gender, givenName, Last4ofSSN");
        Person personOne = new Person();
        personOne.setAddress1("1010 Sometown street");
        personOne.setDateOfBirth(birthDate);
        personOne.setFamilyName("Martinez");
        personOne.setGender(gender);
        personOne.setGivenName("Javier");
        personOne.setSsn("111-11-1111");

        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier("ABCDE-10");
        IdentifierDomain id = new IdentifierDomain();
        id.setNamespaceIdentifier("HIMSS2005");
        pi.setIdentifierDomain(id);
        personOne.addPersonIdentifier(pi);
                
        Person personTwo = new Person();
        personTwo.setAddress1("1010 Sometown street");
        personTwo.setDateOfBirth(birthDate);
        personTwo.setFamilyName("Martinez");
        personTwo.setGender(gender);
        personTwo.setGivenName("Javier");
        personTwo.setSsn("222-22-1111");
        
        pi = new PersonIdentifier();
        pi.setIdentifier("AB12345-10");
        id = new IdentifierDomain();
        id.setNamespaceIdentifier("XREF2005");
        pi.setIdentifierDomain(id);
        personTwo.addPersonIdentifier(pi);      
        
        twoPersonMatch("Rule TEN", personOne, personTwo);
    }   
    
    private void ruleEleven() {
        System.out.println("Transactional support for this test has rollback set to " + this.isDefaultRollback());
        this.setDefaultRollback(true);
        
        log.debug("Testing the rule: dateOfBirth, familyName, gender, givenName, middleName, phoneNumber");
        Person personOne = new Person();
        personOne.setDateOfBirth(birthDate);
        personOne.setFamilyName("Martinez");
        personOne.setGender(gender);
        personOne.setGivenName("Javier");
        personOne.setMiddleName("Javi");
        personOne.setPhoneNumber("111-111-1111");

        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier("ABCDE-11");
        IdentifierDomain id = new IdentifierDomain();
        id.setNamespaceIdentifier("HIMSS2005");
        pi.setIdentifierDomain(id);
        personOne.addPersonIdentifier(pi);
        
        Person personTwo = new Person();
        personTwo.setDateOfBirth(birthDate);
        personTwo.setFamilyName("Martinez");
        personTwo.setGender(gender);
        personTwo.setGivenName("Javier");
        personTwo.setMiddleName("Javi");
        personTwo.setPhoneNumber("111-111-1111");
        
        pi = new PersonIdentifier();
        pi.setIdentifier("AB12345-11");
        id = new IdentifierDomain();
        id.setNamespaceIdentifier("XREF2005");
        pi.setIdentifierDomain(id);
        personTwo.addPersonIdentifier(pi);      
        
        twoPersonMatch("Rule ELEVEN", personOne, personTwo);
    }

    private void ruleTwelve() {
        System.out.println("Transactional support for this test has rollback set to " + this.isDefaultRollback());
        this.setDefaultRollback(true);
        
        log.debug("Testing the rule: address1, dateOfBirth, familyName, gender, givenName, middleName");
        Person personOne = new Person();
        personOne.setAddress1("1010 Sometown street");
        personOne.setDateOfBirth(birthDate);
        personOne.setFamilyName("Martinez");
        personOne.setGender(gender);
        personOne.setGivenName("Javier");
        personOne.setMiddleName("Javi");

        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier("ABCDE-12");
        IdentifierDomain id = new IdentifierDomain();
        id.setNamespaceIdentifier("HIMSS2005");
        pi.setIdentifierDomain(id);
        personOne.addPersonIdentifier(pi);
                
        Person personTwo = new Person();
        personTwo.setAddress1("1010 Sometown street");
        personTwo.setDateOfBirth(birthDate);
        personTwo.setFamilyName("Martinez");
        personTwo.setGender(gender);
        personTwo.setGivenName("Javier");
        personTwo.setMiddleName("Javi");
        
        pi = new PersonIdentifier();
        pi.setIdentifier("AB12345-12");
        id = new IdentifierDomain();
        id.setNamespaceIdentifier("XREF2005");
        pi.setIdentifierDomain(id);
        personTwo.addPersonIdentifier(pi);      
        
        twoPersonMatch("Rule TWELVE", personOne, personTwo);
    }   

    
    private void ruleThirteen() {
        System.out.println("Transactional support for this test has rollback set to " + this.isDefaultRollback());
        this.setDefaultRollback(true);
        
        log.debug("Testing the rule: dateOfBirth, familyName, gender, givenName, middleName, ssn");
        Person personOne = new Person();
        personOne.setDateOfBirth(birthDate);
        personOne.setFamilyName("Martinez");
        personOne.setGender(gender);
        personOne.setGivenName("Javier");
        personOne.setMiddleName("Javi");
        personOne.setSsn("111-11-1111");

        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier("ABCDE-13");
        IdentifierDomain id = new IdentifierDomain();
        id.setNamespaceIdentifier("HIMSS2005");
        pi.setIdentifierDomain(id);
        personOne.addPersonIdentifier(pi);
        
        Person personTwo = new Person();
        personTwo.setDateOfBirth(birthDate);
        personTwo.setFamilyName("Martinez");
        personTwo.setGender(gender);
        personTwo.setGivenName("Javier");
        personTwo.setMiddleName("Javi");
        personTwo.setSsn("111-11-1111");
        
        pi = new PersonIdentifier();
        pi.setIdentifier("AB12345-13");
        id = new IdentifierDomain();
        id.setNamespaceIdentifier("XREF2005");
        pi.setIdentifierDomain(id);
        personTwo.addPersonIdentifier(pi);      
        
        twoPersonMatch("Rule THIRTEEN", personOne, personTwo);
    }

    private void twoPersonMatch(String rule, Person personOne, Person personTwo) {
        
        log.info("Testing exact matching rule " + rule);
        try {
            personOne = Context.getPersonManagerService().addPerson(personOne);
            personIds.add(personOne.getPersonId());
        } catch (ApplicationException e) {
            System.out.println("Person record already exists; Skip this.");
        }

        try {
            personTwo = Context.getPersonManagerService().addPerson(personTwo);
            personIds.add(personTwo.getPersonId());
        } catch (ApplicationException e) {
            System.out.println("Person record already exists; Skip this.");
        }
        
        try {
            List<PersonLink> links = Context.getPersonQueryService().getPersonLinks(personOne);
            assertTrue("Match was not made as there is no link.", links != null && links.size() > 0);
            for (PersonLink link : links) {
                log.info("For " + rule + ", after adding " + personOne.getPersonId() + " and " + personTwo.getPersonId() + 
                        " found link <" + link.getPersonRight().getPersonId() + "," + 
                        link.getPersonLeft().getPersonId() + ">");
                int leftPersonId = link.getPersonLeft().getPersonId();
                int rightPersonId = link.getPersonRight().getPersonId();
                int onePersonId = personOne.getPersonId();
                int twoPersonId = personTwo.getPersonId();
                boolean poss1 = leftPersonId == onePersonId && rightPersonId == twoPersonId;
                boolean poss2 = leftPersonId == twoPersonId && rightPersonId == onePersonId;
                assertTrue("Match was not made using rule " + rule + ": "+ personOne.getPersonId() + " and " + personTwo.getPersonId(),
                        poss1 || poss2);
                log.info("Test " + rule + " was SUCCESSFUL!");
                break;
            }
        } catch (Exception e) {
            System.out.println("Person record already exists; Skip this.");
        }
    }

    private void removeTestRecords() {
        try {
            for (Integer personId : personIds) {
                log.debug("Deleting person with id: " + personId);
                Context.getPersonManagerService().removePerson(personId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            personIds.clear();
        }        
    }
}