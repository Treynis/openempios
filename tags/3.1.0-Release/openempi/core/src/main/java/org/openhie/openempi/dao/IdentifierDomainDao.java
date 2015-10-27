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
package org.openhie.openempi.dao;

import java.util.Date;
import java.util.List;

import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.IdentifierDomainAttribute;
import org.openhie.openempi.model.IdentifierUpdateEvent;
import org.openhie.openempi.model.User;

public interface IdentifierDomainDao extends UniversalDao
{

	public List<IdentifierDomain> getIdentifierDomains();

	public List<String> getIdentifierDomainTypeCodes();

	public IdentifierDomain findIdentifierDomain(final IdentifierDomain identifierDomain);

	public IdentifierDomain findIdentifierDomainByName(final String identifierDomainName);

	public IdentifierDomain findIdentifierDomainById(final Integer id);

	public void addIdentifierDomain(IdentifierDomain identifierDomain);

	public void removeIdentifierDomain(IdentifierDomain identifierDomain);

	public void saveIdentifierDomain(IdentifierDomain identifierDomain);

	public boolean isKnownUniversalIdentifierTypeCode(String universalIdentifierTypeCode);

	public IdentifierDomainAttribute addIdentifierDomainAttribute(IdentifierDomain identifierDomain, String attributeName, String attributeValue);

	public IdentifierDomainAttribute getIdentifierDomainAttribute(IdentifierDomain identifierDomain, String attributeName);

	public List<IdentifierDomainAttribute> getIdentifierDomainAttributes(IdentifierDomain identifierDomain);

	public void updateIdentifierDomainAttribute(IdentifierDomainAttribute identifierDomainAttribute);

	public void removeIdentifierDomainAttribute(IdentifierDomainAttribute identifierDomainAttribute);

    public int getIdentifierUpdateEventCount(User eventRecipient);

    public IdentifierUpdateEvent findIdentifierUpdateEvent(long identifierUpdateEventId);

    public IdentifierUpdateEvent addIdentifierUpdateEvent(IdentifierUpdateEvent identifierUpdateEvent);

    public void removeIdentifierUpdateEvent(IdentifierUpdateEvent identifierUpdateEvent);

    public List<IdentifierUpdateEvent> getIdentifierUpdateEvents(int startIndex, int maxEvents, User eventRecipient);

    public List<IdentifierUpdateEvent> getIdentifierUpdateEvents(User eventRecipient);

    public List<IdentifierUpdateEvent> getIdentifierUpdateEventsByDate(final Date startDate, final User eventRecipient);

    public List<IdentifierUpdateEvent> getIdentifierUpdateEventsBeforeDate(final Date startDate, final User eventRecipient);
}
