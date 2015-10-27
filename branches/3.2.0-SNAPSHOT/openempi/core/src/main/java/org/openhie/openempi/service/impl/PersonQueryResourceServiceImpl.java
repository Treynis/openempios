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
package org.openhie.openempi.service.impl;

import java.util.List;

import org.openhie.openempi.model.Gender;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.IdentifierDomainAttribute;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.PersonLink;
import org.openhie.openempi.model.Race;
import org.openhie.openempi.model.ReviewRecordPair;
import org.openhie.openempi.service.IdentifierDomainService;
import org.openhie.openempi.service.PersonQueryResourceService;
import org.openhie.openempi.service.PersonQueryService;

public class PersonQueryResourceServiceImpl extends BaseServiceImpl implements PersonQueryResourceService
{

    @Override
    public Gender findGenderByCode(String genderCode) {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.findGenderByCode(genderCode);
    }

    @Override
    public Gender findGenderByName(String genderName) {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.findGenderByName(genderName);
    }

    @Override
    public Race findRaceByCode(String raceCode) {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.findRaceByCode(raceCode);
    }

    @Override
    public Race findRaceByName(String raceName) {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.findRaceByName(raceName);
    }

    @Override
    public List<String> getIdentifierDomainTypeCodes() {
        IdentifierDomainService identifierDomainService = getIdentifierDomainService();
        List<String> typeCodes = identifierDomainService.getIdentifierDomainTypeCodes();
        return typeCodes;
    }

    @Override
    public IdentifierDomain findIdentifierDomain(IdentifierDomain identifierDomain) {
        IdentifierDomainService identifierDomainService = getIdentifierDomainService();
        identifierDomain = identifierDomainService.findIdentifierDomain(identifierDomain);
        return identifierDomain;
    }

    @Override
    public List<IdentifierDomain> getIdentifierDomains() {
        IdentifierDomainService identifierDomainService = getIdentifierDomainService();
        return identifierDomainService.getIdentifierDomains();
    }

    @Override
    public List<IdentifierDomainAttribute> getIdentifierDomainAttributes(IdentifierDomain identifierDomain) {
        IdentifierDomainService identifierDomainService = getIdentifierDomainService();
        return identifierDomainService.getIdentifierDomainAttributes(identifierDomain);
    }

    @Override
    public IdentifierDomainAttribute getIdentifierDomainAttribute(IdentifierDomain domain, String attributeName) {
        IdentifierDomainService identifierDomainService = getIdentifierDomainService();
        return identifierDomainService.getIdentifierDomainAttribute(domain, attributeName);
    }

    @Override
    public List<String> getPersonModelAllAttributeNames() {
        PersonQueryService personQueryService = getPersonQueryService(); 
        return personQueryService.getPersonModelAllAttributeNames();
    }

    @Override
    public List<String> getPersonModelAttributeNames() {
        PersonQueryService personQueryService = getPersonQueryService(); 
        List<String> personAttributes = personQueryService.getPersonModelAttributeNames();
        return personAttributes;
    }

    @Override
    public List<String> getPersonModelCustomAttributeNames() {
        PersonQueryService personQueryService = getPersonQueryService(); 
        List<String> personCustomAttributes = personQueryService.getPersonModelCustomAttributeNames();
        return personCustomAttributes;
    }

    @Override
    public Person findPersonById(PersonIdentifier identifier) {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.findPersonById(identifier);
    }

    @Override
    public PersonIdentifier getGlobalIdentifierById(PersonIdentifier identifier) {
        PersonQueryService personQueryService = getPersonQueryService();
        PersonIdentifier globalIdentifier = personQueryService.getGlobalIdentifierById(identifier);
        return globalIdentifier;
    }

    @Override
    public Person getSingleBestRecord(Integer personId) {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.getSingleBestRecord(personId);
    }

    @Override
    public List<Person> getSingleBestRecords(List<Integer> personIds) {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.getSingleBestRecords(personIds);
    }

    @Override
    public List<Person> findLinkedPersons(PersonIdentifier identifier) {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.findLinkedPersons(identifier);
    }

    @Override
    public List<PersonLink> getPersonLinks(Person person) {
        PersonQueryService personQueryService = getPersonQueryService();
        List<PersonLink> links = personQueryService.getPersonLinks(person);
        for (PersonLink link : links) {
            Person leftPerson = personQueryService.loadPerson(link.getPersonLeft().getPersonId());
            Person rightPerson = personQueryService.loadPerson(link.getPersonRight().getPersonId());
            link.setPersonLeft(leftPerson);
            link.setPersonRight(rightPerson);
        }
        return links;
    }

    @Override
    public List<Person> findPersonsByAttributes(Person person) {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.findPersonsByAttributes(person);
    }

    @Override
    public List<Person> findPersonsByAttributesPaged(Person person, int first, int last) {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.findPersonsByAttributesPaged(person, first, last);
    }

    @Override
    public List<Person> findMatchingPersonsByAttributes(Person person) {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.findMatchingPersonsByAttributes(person);
    }

    @Override
    public Person loadPerson(Integer personId) {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.loadPerson(personId);
    }

    @Override
    public List<Person> loadAllPersonsPaged(Integer firstRecord, Integer maxRecords) {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.loadAllPersonsPaged(firstRecord,maxRecords);
    }

    @Override
    public List<Person> loadPersons(List<Integer> personIds) {
        PersonQueryService personQueryService = getPersonQueryService();
        List<Person> persons = personQueryService.loadPersons(personIds);
        return persons;
    }

    @Override
    public List<ReviewRecordPair> loadAllUnreviewedPersonLinks() {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.loadAllUnreviewedPersonLinks();
    }

    @Override
    public List<ReviewRecordPair> loadUnreviewedPersonLinks(Integer maxRecords) {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.loadUnreviewedPersonLinks(maxRecords);
    }

    @Override
    public ReviewRecordPair loadReviewRecordPair(Integer personLinkReviewId) {
        PersonQueryService personQueryService = getPersonQueryService();
        return personQueryService.loadReviewRecordPair(personLinkReviewId);
    }
    
    private PersonQueryService getPersonQueryService() {
        return org.openhie.openempi.context.Context.getPersonQueryService();
    }
    
    private IdentifierDomainService getIdentifierDomainService() {
        return org.openhie.openempi.context.Context.getIdentifierDomainService();
    }
}
