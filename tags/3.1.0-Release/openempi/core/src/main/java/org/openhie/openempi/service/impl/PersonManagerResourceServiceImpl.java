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

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.IdentifierDomainAttribute;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.PersonLink;
import org.openhie.openempi.model.ReviewRecordPair;
import org.openhie.openempi.service.IdentifierDomainService;
import org.openhie.openempi.service.PersonManagerResourceService;
import org.openhie.openempi.service.PersonManagerService;

public class PersonManagerResourceServiceImpl extends BaseServiceImpl implements PersonManagerResourceService
{
    @Override
    public Person addPerson(Person person) throws ApplicationException {
        PersonManagerService manager = getPersonManagerService();
        setPersonReferencesOnPersonIdentifier(person);
        return manager.addPerson(person);
    }

    @Override
    public void updatePerson(Person person) throws ApplicationException {
        setPersonReferencesOnPersonIdentifier(person);
        PersonManagerService manager = getPersonManagerService();
        manager.updatePerson(person);
    }

    @Override
    public Person updatePersonById(Person person) throws ApplicationException {
        PersonManagerService manager = getPersonManagerService();
        setPersonReferencesOnPersonIdentifier(person);
        person = manager.updatePersonById(person);
        return person;
    }

    @Override
    public void deletePerson(PersonIdentifier personIdentifier) throws ApplicationException {
        PersonManagerService manager = getPersonManagerService();
        manager.deletePerson(personIdentifier);
    }

    @Override
    public void removePersonById(Integer personId) throws ApplicationException {
        PersonManagerService manager = getPersonManagerService();
        manager.removePerson(personId);
    }

    @Override
    public void mergePersons(PersonIdentifier retiredIdentifier, PersonIdentifier survivingIdentifier)
            throws ApplicationException {
        PersonManagerService manager = getPersonManagerService();
        manager.mergePersons(retiredIdentifier, survivingIdentifier);
    }

    @Override
    public void deletePersonById(Person person) throws ApplicationException {
        setPersonReferencesOnPersonIdentifier(person);
        PersonManagerService manager = getPersonManagerService();
        manager.deletePersonById(person);
    }

    @Override
    public Person importPerson(Person person) throws ApplicationException {
        PersonManagerService manager = getPersonManagerService();
        setPersonReferencesOnPersonIdentifier(person);
        return manager.importPerson(person);
    }

    @Override
    public IdentifierDomain addIdentifierDomain(IdentifierDomain identifierDomain) throws ApplicationException {
        IdentifierDomainService manager = getIdentifierDomainService();
        return manager.addIdentifierDomain(identifierDomain);
    }

    @Override
    public IdentifierDomain updateIdentifierDomain(IdentifierDomain identifierDomain) throws ApplicationException {
        IdentifierDomainService manager = getIdentifierDomainService();
        return manager.updateIdentifierDomain(identifierDomain);
    }

    @Override
    public void deleteIdentifierDomain(IdentifierDomain identifierDomain) throws ApplicationException {
        IdentifierDomainService manager = getIdentifierDomainService();
        manager.deleteIdentifierDomain(identifierDomain);
    }

    @Override
    public IdentifierDomain obtainUniqueIdentifierDomain(String universalIdentifierTypeCode) {
        IdentifierDomainService manager = getIdentifierDomainService();
        return manager.obtainUniqueIdentifierDomain(universalIdentifierTypeCode);
    }

    @Override
    public IdentifierDomainAttribute addIdentifierDomainAttribute(IdentifierDomain identifierDomain,
            String attributeName, String attributeValue) {
        return getPersonManagerService().addIdentifierDomainAttribute(identifierDomain,
                attributeName, attributeValue);
    }

    @Override
    public void updateIdentifierDomainAttribute(IdentifierDomainAttribute identifierDomainAttribute) {
        getPersonManagerService().updateIdentifierDomainAttribute(identifierDomainAttribute);
    }

    @Override
    public void removeIdentifierDomainAttribute(IdentifierDomainAttribute identifierDomainAttribute) {
        getPersonManagerService().removeIdentifierDomainAttribute(identifierDomainAttribute);
    }

    @Override
    public void addReviewRecordPair(ReviewRecordPair recordPair) throws ApplicationException {
        getPersonManagerService().addReviewRecordPair(recordPair);
    }

    @Override
    public void matchReviewRecordPair(ReviewRecordPair recordPair) throws ApplicationException {
        getPersonManagerService().matchReviewRecordPair(recordPair);
    }

    @Override
    public void deleteReviewRecordPair(ReviewRecordPair recordPair) {
        getPersonManagerService().deleteReviewRecordPair(recordPair);
    }

    @Override
    public void deleteReviewRecordPairs() {
        getPersonManagerService().deleteReviewRecordPairs();
    }

    @Override
    public void linkPersons(PersonLink personLink) throws ApplicationException {
        getPersonManagerService().linkPersons(personLink);
    }

    @Override
    public void unlinkPersons(PersonLink personLink) throws ApplicationException {
        getPersonManagerService().unlinkPersons(personLink);
    }

    /**
     * During marshalling/unmarshalling the reference from PersonIdentifier to Person is lost since otherwise a cycle is
     * created in the serialization graph that Jaxb doesn't know how to deal with We need to fix the reference here
     * before we go any deeper.
     * 
     * @param person
     */
    private void setPersonReferencesOnPersonIdentifier(Person person) {
        if (person != null && person.getPersonIdentifiers() != null) {
            for (PersonIdentifier pi : person.getPersonIdentifiers()) {
                pi.setPerson(person);
            }
        }
    }

    private PersonManagerService getPersonManagerService() {
        return org.openhie.openempi.context.Context.getPersonManagerService();
    }

    private IdentifierDomainService getIdentifierDomainService() {
        return org.openhie.openempi.context.Context.getIdentifierDomainService();
    }
}
