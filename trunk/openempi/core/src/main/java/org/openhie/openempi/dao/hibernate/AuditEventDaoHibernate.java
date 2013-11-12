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
package org.openhie.openempi.dao.hibernate;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.openhie.openempi.dao.AuditEventDao;
import org.openhie.openempi.model.AuditEvent;
import org.openhie.openempi.model.AuditEventEntry;
import org.openhie.openempi.model.AuditEventType;
import org.openhie.openempi.model.LoggedLink;
import org.springframework.orm.hibernate3.HibernateCallback;

public class AuditEventDaoHibernate extends UniversalDaoHibernate implements AuditEventDao
{
	public List<AuditEvent> getAuditEventByType(AuditEventType auditEventType) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public AuditEventType getAuditEventTypeByCode(String auditEventTypeCode) {
		if (auditEventTypeCode == null || auditEventTypeCode.length() == 0) {
			return null;
		}
		String query = "from AuditEventType aet where aet.auditEventTypeCode = '" + auditEventTypeCode + "'";
        List<AuditEventType> values = getHibernateTemplate().find(query);
        log.trace("Search for audit event types by type: " + auditEventTypeCode + " found " + values.size() + " entries.");
        return values.get(0);
	}

	public int getAuditEventCount(final Date startDate, final Date endDate, final List<Integer> auditEventTypeCodes) {
		return (Integer) getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = buildFilterQuery(session, startDate, endDate, auditEventTypeCodes, "select count(*) from AuditEvent where 1=1 ", "");
				int eventCount = ((Long) query.uniqueResult()).intValue();

				return eventCount;
			}
		});
	}

	public List<AuditEvent> filterAuditEvents(final Date startDate, final Date endDate, final List<Integer> auditEventTypeCodes) {
		return this.filterAuditEventsPaged(startDate, endDate, auditEventTypeCodes, 0, 0);
	}

	@SuppressWarnings("unchecked")
	public List<AuditEvent> filterAuditEventsPaged(final Date startDate, final Date endDate, final List<Integer> auditEventTypeCodes, final int firstResult, final int maxResults) {
		if (startDate == null && endDate == null && auditEventTypeCodes == null) {
			return new java.util.ArrayList<AuditEvent>();
		}

		return (List<AuditEvent>) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {

				Query query = buildFilterQuery(session, startDate, endDate, auditEventTypeCodes, "from AuditEvent where 1=1 ", " order by dateCreated desc");
		        List<AuditEvent> events = (List<AuditEvent>)
		        		query
		        			.setFirstResult(firstResult)
		        			.setMaxResults(maxResults)
		        			.list();
		        log.trace("Search for audit event found " + events.size() + " entries.");

		        return events;
			}
		});
	}

	private Query buildFilterQuery(Session session, final Date startDate, final Date endDate, final List<Integer> auditEventTypeCodes, String prefix, String postfix) {
		StringBuffer query = new StringBuffer(prefix);
		if (startDate != null) {
			query.append(" and dateCreated >= :startDate");
		}
		if (endDate != null) {
			query.append(" and dateCreated <= :endDate");
		}
		if (auditEventTypeCodes != null && auditEventTypeCodes.size() > 0) {
			query.append(" and auditEventType.auditEventTypeCd IN (:codes)");
		}
		query.append(postfix);
		Query q = session.createQuery(query.toString());
		if (startDate != null) {
			q.setTimestamp("startDate", startDate);
		}
		if (endDate != null) {
			q.setTimestamp("endDate", endDate);
		}
		if (auditEventTypeCodes != null && auditEventTypeCodes.size() > 0) {
//			StringBuffer sb = new StringBuffer();
//			for (int i=0; i < auditEventTypeCodes.size(); i++) {
//				sb.append(auditEventTypeCodes.get(i));
//				if (i < auditEventTypeCodes.size()-1) {
//					sb.append(",");
//				}
//			}
			q.setParameterList("codes", auditEventTypeCodes);
		}
		return q;
	}


	// Audit event entry
	public List<AuditEventEntry> filterAuditEventEntries(final Date startDate, final Date endDate, final List<Integer> auditEventTypeCodes) {
		return this.filterAuditEventEntriesPaged(startDate, endDate, auditEventTypeCodes, 0, 0);
	}

	@SuppressWarnings("unchecked")
	public List<AuditEventEntry> filterAuditEventEntriesPaged(final Date startDate, final Date endDate, final List<Integer> auditEventTypeCodes, final int firstResult, final int maxResults) {
		if (startDate == null && endDate == null && auditEventTypeCodes == null) {
			return new java.util.ArrayList<AuditEventEntry>();
		}

		return (List<AuditEventEntry>) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {

				Query query = buildFilterQuery(session, startDate, endDate, auditEventTypeCodes, "from AuditEventEntry where 1=1 ", " order by dateCreated desc");
		        List<AuditEvent> events = (List<AuditEvent>)
		        		query
		        			.setFirstResult(firstResult)
		        			.setMaxResults(maxResults)
		        			.list();
		        log.trace("Search for audit event found " + events.size() + " entries.");

		        return events;
			}
		});
	}

	public int getAuditEventEntryCount(final Date startDate, final Date endDate, final List<Integer> auditEventTypeCodes) {
		return (Integer) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = buildFilterQuery(session, startDate, endDate, auditEventTypeCodes, "select count(*) from AuditEventEntry where 1=1 ", "");
				int eventCount = ((Long) query.uniqueResult()).intValue();

				return eventCount;
			}
		});
	}

    public LoggedLink getLoggedLink(final Integer loggedLinkId) {
        log.trace("Loading Logged link with id " + loggedLinkId);
        LoggedLink loggedLink =
                (LoggedLink) getHibernateTemplate().execute(new HibernateCallback() {
                    public Object doInHibernate(Session session) throws HibernateException, SQLException {
                        LoggedLink pair = (LoggedLink) session.load(LoggedLink.class, loggedLinkId);
                        if (pair == null || pair.getRightRecordId() == null || pair.getLeftRecordId() == null) {
                            return pair;
                        }
                        if (pair instanceof HibernateProxy) {
                            pair = (LoggedLink) ((HibernateProxy) pair).getHibernateLazyInitializer().getImplementation();
                        }
                        return pair;
                    }
                });
        return loggedLink;
    }

    public int getLoggedLinksCount(int entityVersionId, int vectorValue) {
        int count = ((Long) getSession()
                .createQuery("select count(*) from LoggedLink where vectorValue = " + vectorValue + " and entityId = " + entityVersionId)
                .uniqueResult()).intValue();
        return count;
    }

    public List<LoggedLink> getLoggedLinks(final int entityVersionId, final int vectorValue, final int start, final int maxResults) {
        log.trace("Retrieving logged links.");
        @SuppressWarnings("unchecked")
        List<LoggedLink> links = (List<LoggedLink>) getHibernateTemplate()
                .execute(new HibernateCallback() {
                    public Object doInHibernate(Session session) throws HibernateException, SQLException {
                        Query query = session.createQuery("from LoggedLink where vectorValue = " + vectorValue + " and entityId = " + entityVersionId);
                        query.setFirstResult(start);
                        query.setMaxResults(maxResults);
                        List<LoggedLink> list = query.list();
                        return list;
                    }
                });
        log.trace("Found " + links.size() + " logged links.");
        return links;
    }
}
