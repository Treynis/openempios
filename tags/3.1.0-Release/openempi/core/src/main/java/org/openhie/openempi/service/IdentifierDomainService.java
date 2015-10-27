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

import java.util.Date;
import java.util.List;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.IdentifierDomainAttribute;
import org.openhie.openempi.model.IdentifierUpdateEvent;
import org.openhie.openempi.model.User;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface IdentifierDomainService
{
	/**
	 * This method returns an identifier domain located using whichever key is provided
	 * for looking up the entity in the repository. The keys and the order in which they are used
	 * in locating the entry are as follows:
	 * <ul>
	 * <li>The internal key for the entry identifierDomainId.</li>
	 * <li>The namespace identifier alternate key</li>
	 * <li>The pair universal identifier/universal identifier type code</li>
	 * </ul>
	 *
	 * @param identifierDomain
	 * @return
	 */
	public IdentifierDomain findIdentifierDomain(IdentifierDomain identifierDomain);

    public IdentifierDomain findIdentifierDomainByName(String identifierDomainName);

    public IdentifierDomain findIdentifierDomainById(Integer id);

	/**
	 * Returns the list of identifier domains known by the system
	 *
	 * @return
	 */
	public List<IdentifierDomain> getIdentifierDomains();

	/**
	 * Returns the list of distinct identifier domain type codes
	 *
	 */
	public List<String> getIdentifierDomainTypeCodes();

	/**
	 * Returns an instance of an IdentifierDomainAttribute associated with the identifier domain passed in and with the
	 * name passed in as attributeName.
	 *
	 * @param identifierDomain This must be an instance of an identifier domain that has been obtained
	 * either from a prior call to getIdentifierDomains() or via an association between an identifier
	 * domain and a person identifier. The only attribute that must be present is the identifierDomainId
	 * attribute which is only useful internally to an OpenEMPI instance.
	 * @param attributeName The name of the attribute.
	 * @return
	 */
	public IdentifierDomainAttribute getIdentifierDomainAttribute(IdentifierDomain identifierDomain, String attributeName);

	/**
	 * This method returns a list of all the identifier domain attributes associated with a given identifier domain.
	 *
	 * @param identifierDomain This must be an instance of an identifier domain that has been obtained
	 * either from a prior call to getIdentifierDomains() or via an association between an identifier
	 * domain and a person identifier. The only attribute that must be present is the identifierDomainId
	 * attribute which is only useful internally to an OpenEMPI instance.
	 * @return List of IdentifierDomainAttributes found.
	 */
	public List<IdentifierDomainAttribute> getIdentifierDomainAttributes(IdentifierDomain identifierDomain);

	/**
	 * Adds a new identifier domain to the EMPI repository. The system will first check to see if the identifier domain is already known to the
	 * system. If the identifier domain is known already then nothing further will be done. If the identifier domain is new, then the new identifier
	 * domain will be added to the repository.
	 *
	 * @param identifierDomain
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public IdentifierDomain addIdentifierDomain(IdentifierDomain identifierDomain) throws ApplicationException;

	/**
	 * Update an existing identifier domain. The existing entry is located using the internal identifier.
	 *
	 * @param identifierDomain
	 */
	public IdentifierDomain updateIdentifierDomain(IdentifierDomain identifierDomain) throws ApplicationException;

	/**
	 * Deletes an identifier domain from the repository. The caller must provide the internal primary key that identifier the identifier domain,
	 * the namespace identifier (another unique identifier for an identifier domain), or the pair universal identifier/university identifier type code.
	 *
	 *  @param identifierDomain
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void deleteIdentifierDomain(IdentifierDomain identifierDomain) throws ApplicationException;

	/**
	 * This method generates a unique identifier domain identifier within the given universalIdentifierTypeCode. This method
	 * is intended for cases where new affinity domains are registering with OpenEMPI to join the group of affinity domains
	 * for which OpenEMPI collects person identity information for.
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public IdentifierDomain obtainUniqueIdentifierDomain(String universalIdentifierTypeCode);

	/**
	 * The addIdentifierDomainAttribute method allows the caller to associate with a given identifier
	 * domain an attribute. The attribute consists of a name-value pair. This functionality is useful
	 * when OpenEMPI is used to support a Record Locator Service-type EHR and then each identifier domain
	 * corresponds to a site that provides patient services so it is useful to be able to associate
	 * arbitrary attributes to an identifier domain. Those attributes can be used to provide
	 * more user-friendly information about a health care provider site or institution.
	 *
	 * @param identifierDomain This must be an instance of an identifier domain that has been obtained
	 * either from a prior call to getIdentifierDomains() or via an association between an identifier
	 * domain and a person identifier. The only attribute that must be present is the identifierDomainId
	 * attribute which is only useful internally to an OpenEMPI instance.
	 *
	 * @param attributeName The name portion of the attribute
	 * @param attributeValue The value portion of the attribute
	 * @return Returns an instance of an IdentifierDomainAttribute object that was persisted after a successful
	 * add operation completion or null if the add operation fails.
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public IdentifierDomainAttribute addIdentifierDomainAttribute(IdentifierDomain identifierDomain, String attributeName, String attributeValue);

	/**
	 * This method updates an existing identifier domain attribute with the name and value specified.
	 *
	 * @param identifierDomainAttribute
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void updateIdentifierDomainAttribute(IdentifierDomainAttribute identifierDomainAttribute);

	/**
	 * This method removes an existing identifier domain attribute. The identifier domain attribute id
	 * attribute must be populated.
	 *
	 * @param identifierDomainAttribute
	 */
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void removeIdentifierDomainAttribute(IdentifierDomainAttribute identifierDomainAttribute);

    public int getNotificationCount(User user);

    public IdentifierUpdateEvent findIdentifierUpdateEvent(long identifierUpdateEventId);

    public IdentifierUpdateEvent removeIdentifierUpdateEvent(IdentifierUpdateEvent identifierUpdateEvent);
    
    @Transactional(propagation=Propagation.REQUIRED, readOnly=false)
    public List<IdentifierUpdateEvent> retrieveNotifications(int startIndex, int maxEvents, Boolean removeRecords, User eventRecipient);
         
    @Transactional(propagation=Propagation.REQUIRED, readOnly=false)
    public List<IdentifierUpdateEvent> retrieveNotifications(Boolean removeRecords, User eventRecipient);
    
    @Transactional(propagation=Propagation.REQUIRED, readOnly=false)     
    public List<IdentifierUpdateEvent> retrieveNotificationsByDate(Date startDate, Boolean removeRecords, User eventRecipient);
    
    /**
     *The removeNotification method is used for permanently removing update notification entries from the system.
     */
    @Transactional(propagation=Propagation.REQUIRED, readOnly=false)
     public int removeNotifications(List<IdentifierUpdateEvent> identifierUpdateEvents);
    
    /**
     *The cleanupUpdateNotifications method is used for permanently removing update notification entries from the system as per configured 
     *  <time-to-live> value.
     */
    @Transactional(propagation=Propagation.REQUIRED, readOnly=false)
    public void cleanupUpdateNotifications();
}
