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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openhie.openempi.configuration.UpdateNotificationRegistrationEntry;
import org.openhie.openempi.dao.IdentifierDomainDao;
import org.openhie.openempi.entity.impl.RecordState;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.IdentifierUpdateEntry;
import org.openhie.openempi.model.IdentifierUpdateEvent;
import org.openhie.openempi.model.RecordLink;

public class UpdateEventNotificationGenerator
{
	private Logger log = Logger.getLogger(getClass());
	
	private IdentifierDomainDao identifierDao;
	List<UpdateNotificationRegistrationEntry> configurationEntries;
	
	public UpdateEventNotificationGenerator(IdentifierDomainDao identifierDao, List<UpdateNotificationRegistrationEntry> configurationEntries) {
		this.identifierDao = identifierDao;
		this.configurationEntries = configurationEntries;
	}

	public void generateEvents(RecordState state) {
		
		if (configurationEntries == null || configurationEntries.size() == 0) {
			return;
		}
		
		// Add to person record identifiers, identifiers from the linked records
		expandIdentifiersWithLinkData(state);
		log.trace("Updated the state to: " + state);
		
		for (UpdateNotificationRegistrationEntry entry : configurationEntries) {
			// Only need to generate update notifications if there has been a change
			// in the domain of interest of the user expecting notifications.
			if (!changeInDomainOfInterest(entry, state)) {
				continue;
			}
			IdentifierUpdateEvent event = new IdentifierUpdateEvent();
			if (state.getPreIdentifiers().size() > state.getPostIdentifiers().size()) {
				event.setTransition(IdentifierUpdateEvent.LEAVE_TRANSITION);
			} else {
				event.setTransition(IdentifierUpdateEvent.JOIN_TRANSITION);
			}
			event.setSource(state.getSource());
			event.setPreUpdateIdentifiers(generateEntries(state.getPreIdentifiers()));
			event.setPostUpdateIdentifiers(generateEntries(state.getPostIdentifiers()));
			event.setDateCreated(new Date());
			event.setUpdateRecipient(entry.getUser());
			log.debug("Saving identifier update event: " + event);
			identifierDao.addIdentifierUpdateEvent(event);
		}
	}

	private Set<IdentifierUpdateEntry> generateEntries(Set<Identifier> identifiers) {
		Set<IdentifierUpdateEntry> entries = new HashSet<IdentifierUpdateEntry>();
		for (Identifier identifier : identifiers) {
			IdentifierUpdateEntry entry = new IdentifierUpdateEntry();
			entry.setIdentifier(identifier.getIdentifier());
			entry.setIdentifierDomain(identifier.getIdentifierDomain());
			entries.add(entry);
		}
		return entries;
	}

	private void expandIdentifiersWithLinkData(RecordState state) {
		Set<Identifier> preIdentifiers = expandIdentifiers(state.getRecordId(), state.getPreIdentifiers(), state.getPreLinks());
		state.setPreIdentifiers(preIdentifiers);
		Set<Identifier> postIdentifiers = expandIdentifiers(state.getRecordId(), state.getPostIdentifiers(), state.getPostLinks());
		state.setPostIdentifiers(postIdentifiers);
	}

	private Set<Identifier> expandIdentifiers(long recordId, Set<Identifier> ids, Set<RecordLink> links) {
		if (ids == null) {
			ids = new HashSet<Identifier>();
		}
		if (links == null || links.size() == 0) {
			return ids;
		}
		for (RecordLink link : links) {
			if (link.getLeftRecord().getRecordId() != recordId && link.getLeftRecord().getIdentifiers() != null 
					&& link.getLeftRecord().getIdentifiers().size() > 0) {
				for (Identifier identifier : link.getLeftRecord().getIdentifiers()) {
					if (!containsIdentifier(identifier, ids)) {
						ids.add(identifier);
					}
				}
			}
			if (link.getRightRecord().getRecordId() != recordId && link.getRightRecord().getIdentifiers() != null
					&& link.getRightRecord().getIdentifiers().size() > 0) {
				for (Identifier identifier : link.getRightRecord().getIdentifiers()) {
					if (!containsIdentifier(identifier, ids)) {
						ids.add(identifier);
					}
				}
			}
		}
		return ids;
	}

	private boolean containsIdentifier(Identifier identifier, Set<Identifier> ids) {
		if (ids == null || ids.size() == 0) {
			return false;
		}
		for (Identifier id : ids) {
			if (identifier.getIdentifier().equalsIgnoreCase(id.getIdentifier()) && 
					identifier.getIdentifierDomain().getIdentifierDomainName()
						.equalsIgnoreCase(id.getIdentifierDomain().getIdentifierDomainName())) {
				return true;
			}
		}
		return false;
	}

	private boolean changeInDomainOfInterest(UpdateNotificationRegistrationEntry entry, RecordState state) {
		Integer domainOfInterest = entry.getIdentifierDomain().getIdentifierDomainId();
		String beforeIdentifier = getIdentifierInDomainOfInterest(domainOfInterest, state.getPreIdentifiers());
		String afterIdentifier = getIdentifierInDomainOfInterest(domainOfInterest, state.getPostIdentifiers());
		// There are two cases:
		// 1. no identifier in the domain of interest before but one in it afterwards
		// 2. an identifier in the domain of interest before but different one afterwards
		//
		if (beforeIdentifier == null && afterIdentifier != null) {
			return true;
		}
		
		
		if (beforeIdentifier == null && afterIdentifier == null) {
			return false;
		}
		
		
		if (!beforeIdentifier.equalsIgnoreCase(afterIdentifier)) {
			return true;
		}
		return false;
	}

	private String getIdentifierInDomainOfInterest(Integer domainOfInterest, Set<Identifier> identifiers) {
		for (Identifier identifier : identifiers) {		    
			if (identifier.getIdentifierDomain() != null &&
			        identifier.getIdentifierDomain().getIdentifierDomainId() != null && 
			        identifier.getIdentifierDomain().getIdentifierDomainId().intValue() == domainOfInterest.intValue()) {
				return identifier.getIdentifier();
			}
		}
		return null;
	}

	public IdentifierDomainDao getIdentifierDomainDao() {
		return identifierDao;
	}

	public void setIdentifierDomainDao(IdentifierDomainDao identifierDao) {
		this.identifierDao = identifierDao;
	}
}
