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

import org.openhie.openempi.model.AuditEvent;
import org.openhie.openempi.model.AuditEventEntry;
import org.openhie.openempi.model.AuditEventType;
import org.openhie.openempi.model.LoggedLink;

public interface AuditEventDao extends UniversalDao
{
	AuditEventType getAuditEventTypeByCode(String auditEventTypeCode);

	List<AuditEvent> getAuditEventByType(AuditEventType auditEventType);

	List<AuditEvent> filterAuditEvents(Date startDate, Date endDate, List<Integer> auditEventTypeCodes);

	List<AuditEvent> filterAuditEventsPaged(Date startDate, Date endDate, List<Integer> auditEventTypeCodes,
	        int firstResult, int maxResults);

	int getAuditEventCount(Date startDate, Date endDate, List<Integer> auditEventTypeCodes);

	// Audit event entry
	int getAuditEventEntryCount(Date startDate, Date endDate, List<Integer> auditEventTypeCodes);

	List<AuditEventEntry> filterAuditEventEntries(Date startDate, Date endDate, List<Integer> auditEventTypeCodes);

	List<AuditEventEntry> filterAuditEventEntriesPaged(Date startDate, Date endDate,
	        List<Integer> auditEventTypeCodes,
	        int firstResult, int maxResults);

	// link logs
	void clearLoggedLinks(int entityVersionId);
	
    LoggedLink getLoggedLink(Integer loggedLinkId);

    int getLoggedLinksCount(int entityVersionId, int vectorValue);

    List<LoggedLink> getLoggedLinks(final int entityVersionId, final int vectorValue,
            final int start, final int maxResults);

}
