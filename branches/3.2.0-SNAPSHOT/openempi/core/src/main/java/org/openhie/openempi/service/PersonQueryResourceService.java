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
package org.openhie.openempi.service;

import java.util.List;

import org.openhie.openempi.model.Gender;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.IdentifierDomainAttribute;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.PersonLink;
import org.openhie.openempi.model.Race;
import org.openhie.openempi.model.ReviewRecordPair;

public interface PersonQueryResourceService
{
    public Gender findGenderByCode(String genderCode);

    public Gender findGenderByName(String genderName);
    
    public Race findRaceByCode(String raceCode);
    
    public Race findRaceByName(String raceName);
    
    public List<String> getIdentifierDomainTypeCodes();
 
    public IdentifierDomain findIdentifierDomain(IdentifierDomain identifierDomain);

    public List<IdentifierDomain> getIdentifierDomains();
    
    public List<IdentifierDomainAttribute> getIdentifierDomainAttributes(IdentifierDomain identifierDomain);
    
    public IdentifierDomainAttribute getIdentifierDomainAttribute(IdentifierDomain domain, String attributeName);

    public List<String> getPersonModelAllAttributeNames();
    
    public List<String> getPersonModelAttributeNames();
    
    public List<String> getPersonModelCustomAttributeNames();
    
    public Person findPersonById(PersonIdentifier identifier);

    public PersonIdentifier getGlobalIdentifierById(PersonIdentifier identifier);

    public Person getSingleBestRecord(Integer personId);
    
    public List<Person> getSingleBestRecords(List<Integer> personIds);
    
    public List<Person> findLinkedPersons(PersonIdentifier identifier);

    public List<PersonLink> getPersonLinks(Person person);
    
    public List<Person> findPersonsByAttributes(Person person);
    
    public List<Person> findPersonsByAttributesPaged(Person person, int first, int last);
    
    public List<Person> findMatchingPersonsByAttributes(Person person);
    
    public Person loadPerson(Integer personId);
    
    public List<Person> loadAllPersonsPaged(Integer firstRecord, Integer maxRecords);
    
    public List<Person> loadPersons(List<Integer> personIds);
    
    public List<ReviewRecordPair> loadAllUnreviewedPersonLinks();
    
    public List<ReviewRecordPair> loadUnreviewedPersonLinks(Integer maxRecords);
    
    public ReviewRecordPair loadReviewRecordPair(Integer personLinkReviewId);
}
