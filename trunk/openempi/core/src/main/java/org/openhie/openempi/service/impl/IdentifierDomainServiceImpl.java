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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.ValidationException;
import org.openhie.openempi.configuration.UpdateNotificationRegistrationEntry;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.dao.IdentifierDomainDao;
import org.openhie.openempi.model.AuditEventType;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.IdentifierDomainAttribute;
import org.openhie.openempi.model.IdentifierUpdateEvent;
import org.openhie.openempi.model.User;
import org.openhie.openempi.notification.ObservationEventType;
import org.openhie.openempi.service.IdentifierDomainService;
import org.openhie.openempi.service.ValidationService;

import com.eaio.uuid.UUID;

public class IdentifierDomainServiceImpl extends BaseServiceImpl implements IdentifierDomainService
{
    private IdentifierDomainDao identifierDomainDao;

    // Query Identifier Domain
    public IdentifierDomain findIdentifierDomain(IdentifierDomain identifierDomain) {
        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomain);

        if (identifierDomain == null
                || (identifierDomain.getIdentifierDomainId() == null
                        && identifierDomain.getNamespaceIdentifier() == null && (identifierDomain
                        .getUniversalIdentifier() == null || identifierDomain.getUniversalIdentifierTypeCode() == null))) {
            return null;
        }
        IdentifierDomain idFound = identifierDomainDao.findIdentifierDomain(identifierDomain);
        return idFound;
    }

    public IdentifierDomain findIdentifierDomainByName(String identifierDomainName) {
        return identifierDomainDao.findIdentifierDomainByName(identifierDomainName);
    }

    public IdentifierDomain findIdentifierDomainById(Integer id) {
        return identifierDomainDao.findIdentifierDomainById(id);
    }

    public IdentifierDomainAttribute getIdentifierDomainAttribute(IdentifierDomain identifierDomain,
            String attributeName) {

        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomain);

        return identifierDomainDao.getIdentifierDomainAttribute(identifierDomain, attributeName);
    }

    public List<IdentifierDomainAttribute> getIdentifierDomainAttributes(IdentifierDomain identifierDomain) {

        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomain);

        return identifierDomainDao.getIdentifierDomainAttributes(identifierDomain);
    }

    public List<String> getIdentifierDomainTypeCodes() {
        return identifierDomainDao.getIdentifierDomainTypeCodes();
    }

    public List<IdentifierDomain> getIdentifierDomains() {
        return identifierDomainDao.getIdentifierDomains();
    }

    // Manage Identifier Domain
    public IdentifierDomain addIdentifierDomain(IdentifierDomain identifierDomain) throws ApplicationException {

        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomain);

        if (identifierDomain == null
                || (identifierDomain.getNamespaceIdentifier() == null && (identifierDomain.getUniversalIdentifier() == null || identifierDomain
                        .getUniversalIdentifierTypeCode() == null))) {
            log.warn("Attempted to add an identifier domain with insufficient attributes: " + identifierDomain);
            throw new ApplicationException("The identifier domain to be added is invalid.");
        }

        IdentifierDomain idFound = findIdentifierDomain(identifierDomain);
        if (idFound != null) {
            log.warn("While attempting to add an identifier domain, found an existing record in the repository: "
                    + idFound);
            throw new ApplicationException("Identifier domain record to be added already exists in the system.");
        }
        saveIdentifierDomain(identifierDomain);
        Context.notifyObserver(ObservationEventType.IDENTIFIER_DOMAIN_UPDATED_EVENT, identifierDomain);

        return identifierDomain;
    }

    public void deleteIdentifierDomain(IdentifierDomain identifierDomain) throws ApplicationException {

        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomain);

        if (identifierDomain == null
                || (identifierDomain.getIdentifierDomainId() == null
                        && identifierDomain.getNamespaceIdentifier() == null && (identifierDomain
                        .getUniversalIdentifier() == null || identifierDomain.getUniversalIdentifierTypeCode() == null))) {
            log.warn("Attempted to delete an identifier domain with insufficient attributes: " + identifierDomain);
            throw new ApplicationException("The identifier domain to be deleted is invalid.");
        }

        IdentifierDomain idFound = findIdentifierDomain(identifierDomain);
        if (idFound == null) {
            return;
        }
        try {
            identifierDomainDao.removeIdentifierDomain(idFound);
            Context.notifyObserver(ObservationEventType.IDENTIFIER_DOMAIN_UPDATED_EVENT, idFound);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("Attempted to delete an identifier domain that is still referenced by identifiers: " + e, e);
            throw new ApplicationException("Cannot delete identifier domain as it is still in use.");
        }
    }

    public IdentifierDomain updateIdentifierDomain(IdentifierDomain identifierDomain) throws ApplicationException {

        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomain);

        if (identifierDomain == null || identifierDomain.getIdentifierDomainId() == null) {
            log.warn("Attempted to update an identifier domain with insufficient attributes: " + identifierDomain);
            throw new ApplicationException("The identifier domain to be updated is invalid.");
        }

        if (identifierDomain.getNamespaceIdentifier() == null
                && (identifierDomain.getUniversalIdentifier() == null || identifierDomain
                        .getUniversalIdentifierTypeCode() == null)) {
            log.warn("Attempted to update an identifier domain with insufficient attributes: " + identifierDomain);
            throw new ApplicationException("The identifier domain to be update is invalid.");
        }

        IdentifierDomain idFound = findIdentifierDomain(identifierDomain);
        if (idFound != null && idFound.getIdentifierDomainId().intValue() != identifierDomain.getIdentifierDomainId().intValue()) {
            log.warn("While attempting to update an identifier domain, found an existing record in the repository with same identifiying attributes: "
                    + idFound);
            throw new ApplicationException(
                    "Identifier domain record cannot be updated to match another entry in the repository.");
        }
        idFound.setIdentifierDomainDescription(identifierDomain.getIdentifierDomainDescription());
        idFound.setIdentifierDomainName(identifierDomain.getIdentifierDomainName());
        idFound.setNamespaceIdentifier(identifierDomain.getNamespaceIdentifier());
        idFound.setUniversalIdentifier(identifierDomain.getUniversalIdentifier());
        idFound.setUniversalIdentifierTypeCode(identifierDomain.getUniversalIdentifierTypeCode());
        saveIdentifierDomain(idFound);
        Context.notifyObserver(ObservationEventType.IDENTIFIER_DOMAIN_UPDATED_EVENT, idFound);
        return idFound;
    }

    public IdentifierDomainAttribute addIdentifierDomainAttribute(IdentifierDomain identifierDomain,
            String attributeName, String attributeValue) {

        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomain);

        IdentifierDomainAttribute attribute = identifierDomainDao.addIdentifierDomainAttribute(identifierDomain,
                attributeName, attributeValue);

        Context.getAuditEventService().saveAuditEvent(
                AuditEventType.ADD_IDENTIFIER_DOMAIN_ATTRIBUTE_EVENT_TYPE,
                "Added attribute " + attributeName + " to identifier domain with ID "
                        + identifierDomain.getIdentifierDomainId());

        return attribute;
    }

    public void updateIdentifierDomainAttribute(IdentifierDomainAttribute identifierDomainAttribute) {

        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomainAttribute);

        identifierDomainDao.updateIdentifierDomainAttribute(identifierDomainAttribute);

        Context.getAuditEventService().saveAuditEvent(
                AuditEventType.UPDATE_IDENTIFIER_DOMAIN_ATTRIBUTE_EVENT_TYPE,
                "Updated attribute " + identifierDomainAttribute.getAttributeName() + " of identifier domain with ID "
                        + identifierDomainAttribute.getIdentifierDomainId());
    }

    public void removeIdentifierDomainAttribute(IdentifierDomainAttribute identifierDomainAttribute) {

        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomainAttribute);

        identifierDomainDao.removeIdentifierDomainAttribute(identifierDomainAttribute);
        Context.getAuditEventService().saveAuditEvent(
                AuditEventType.DELETE_IDENTIFIER_DOMAIN_ATTRIBUTE_EVENT_TYPE,
                "Deleted attribute " + identifierDomainAttribute.getAttributeName() + " of identifier domain with ID "
                        + identifierDomainAttribute.getIdentifierDomainId());
    }

    public IdentifierDomain obtainUniqueIdentifierDomain(String universalIdentifierTypeCode) {
        boolean isKnown = identifierDomainDao.isKnownUniversalIdentifierTypeCode(universalIdentifierTypeCode);
        log.trace("The universlIdentifierTypeCode " + universalIdentifierTypeCode
                + " is known to the system exptresson is " + isKnown);
        if (!isKnown) {
            throw new ValidationException("The universalIdentifierTypeCode " + universalIdentifierTypeCode
                    + " is not known to the system.");
        }
        IdentifierDomain identifierDomain = generateIdentifierDomainForUniversalIdentifierTypeCode(universalIdentifierTypeCode);
        identifierDomainDao.addIdentifierDomain(identifierDomain);
        log.trace("Created new identifier domain " + identifierDomain);
        Context.getAuditEventService().saveAuditEvent(AuditEventType.OBTAIN_UNIQUE_IDENTIFIER_DOMAIN_EVENT_TYPE,
                "Obtained unique identifier domain type for type code " + universalIdentifierTypeCode);
        return identifierDomain;
    }

    private void saveIdentifierDomain(IdentifierDomain identifierDomain) {
        java.util.Date now = new java.util.Date();
        identifierDomain.setDateCreated(now);
        identifierDomain.setUserCreatedBy(Context.getUserContext().getUser());
        identifierDomainDao.addIdentifierDomain(identifierDomain);
    }

    private IdentifierDomain generateIdentifierDomainForUniversalIdentifierTypeCode(String universalIdentifierTypeCode) {
        UUID uuid = new UUID();
        IdentifierDomain id = new IdentifierDomain();
        java.util.Date now = new java.util.Date();
        id.setDateCreated(now);
        id.setUserCreatedBy(Context.getUserContext().getUser());
        id.setIdentifierDomainName(uuid.toString());
        id.setNamespaceIdentifier(uuid.toString());
        id.setUniversalIdentifier(uuid.toString());
        id.setUniversalIdentifierTypeCode(universalIdentifierTypeCode);
        return id;
    }

    public int getNotificationCount(User user) {
        int count = identifierDomainDao.getIdentifierUpdateEventCount(user);
        return count;
    }

    public IdentifierUpdateEvent findIdentifierUpdateEvent(long identifierUpdateEventId) {
        return identifierDomainDao.findIdentifierUpdateEvent(identifierUpdateEventId);
    }

    public IdentifierUpdateEvent removeIdentifierUpdateEvent(IdentifierUpdateEvent identifierUpdateEvent) {
        IdentifierUpdateEvent identifierUpdate = identifierDomainDao.findIdentifierUpdateEvent(identifierUpdateEvent.getIdentifierUpdateEventId());
        if (identifierUpdate != null) {
            identifierDomainDao.removeIdentifierUpdateEvent(identifierUpdate);
        }
        return identifierUpdate;
    }

    public List<IdentifierUpdateEvent> retrieveNotifications(int startIndex, int maxEvents, Boolean removeRecords,
            User eventRecipient) {

        List<IdentifierUpdateEvent> identifierUpdateEvents = identifierDomainDao.getIdentifierUpdateEvents(startIndex,
                maxEvents, eventRecipient);

        if (removeRecords) {
            for (IdentifierUpdateEvent identifierUpdateEvent : identifierUpdateEvents) {
                log.trace("Deleting IdentifierUpdateEvent : " + identifierUpdateEvent.getIdentifierUpdateEventId());
                identifierDomainDao.removeIdentifierUpdateEvent(identifierUpdateEvent);
            }
        }
        return identifierUpdateEvents;
    }

    public List<IdentifierUpdateEvent> retrieveNotifications(Boolean removeRecords, User eventRecipient) {
        List<IdentifierUpdateEvent> identifierUpdateEvents = identifierDomainDao
                .getIdentifierUpdateEvents(eventRecipient);

        if (removeRecords) {
            for (IdentifierUpdateEvent identifierUpdateEvent : identifierUpdateEvents) {
                log.trace("Deleting IdentifierUpdateEvent : " + identifierUpdateEvent.getIdentifierUpdateEventId());
                identifierDomainDao.removeIdentifierUpdateEvent(identifierUpdateEvent);
            }
        }
        return identifierUpdateEvents;
    }

    public List<IdentifierUpdateEvent> retrieveNotificationsByDate(Date startDate, Boolean removeRecords,
            User eventRecipient) {
        List<IdentifierUpdateEvent> identifierUpdateEvents =
                identifierDomainDao.getIdentifierUpdateEventsByDate(startDate, eventRecipient);

        if (removeRecords) {
            for (IdentifierUpdateEvent identifierUpdateEvent : identifierUpdateEvents) {
                log.trace("Deleting IdentifierUpdateEvent : " + identifierUpdateEvent.getIdentifierUpdateEventId());
                identifierDomainDao.removeIdentifierUpdateEvent(identifierUpdateEvent);
            }
        }
        return identifierUpdateEvents;
    }

    public int removeNotifications(List<IdentifierUpdateEvent> identifierUpdateEvents) {
        int count = 0;
        for (IdentifierUpdateEvent identifierUpdateEvent : identifierUpdateEvents) {
            log.trace("Deleting the IdentifierUpdateEvent");
            identifierDomainDao.removeIdentifierUpdateEvent(identifierUpdateEvent);
            count++;
        }
        return count;
    }

    public void cleanupUpdateNotifications() {
        List<UpdateNotificationRegistrationEntry> updateNotificationEntries =
                Context.getConfiguration().getAdminConfiguration().getUpdateNotificationRegistrationEntries();
        Calendar cal = Calendar.getInstance();

        for (UpdateNotificationRegistrationEntry entry : updateNotificationEntries) {
            cal.add(Calendar.DATE, -(entry.getTimeToLive()));

            List<IdentifierUpdateEvent> identifierUpdateEvents =
                    identifierDomainDao.getIdentifierUpdateEventsBeforeDate(cal.getTime(), entry.getUser());

            for (IdentifierUpdateEvent identifierUpdateEvent : identifierUpdateEvents) {
                log.trace("Deleting IdentifierUpdateEvent : " + identifierUpdateEvent.getIdentifierUpdateEventId());
                identifierDomainDao.removeIdentifierUpdateEvent(identifierUpdateEvent);
            }
         }
    }

    public IdentifierDomainDao getIdentifierDomainDao() {
        return identifierDomainDao;
    }

    public void setIdentifierDomainDao(IdentifierDomainDao identifierDomainDao) {
        this.identifierDomainDao = identifierDomainDao;
    }
}
