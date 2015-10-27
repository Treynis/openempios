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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.dao.IdentifierDomainDao;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.IdentifierDomainAttribute;
import org.openhie.openempi.model.IdentifierUpdateEntry;
import org.openhie.openempi.model.IdentifierUpdateEvent;
import org.openhie.openempi.model.User;
import org.openhie.openempi.notification.EventObservable;
import org.openhie.openempi.notification.ObservationEventType;
import org.springframework.orm.hibernate3.HibernateAccessor;
import org.springframework.orm.hibernate3.HibernateCallback;

public class IdentifierDomainDaoHibernate extends UniversalDaoHibernate implements IdentifierDomainDao, Observer
{
    private boolean registeredUpdateListener = false;
    private static List<IdentifierDomain> identifierDomainCache = new ArrayList<IdentifierDomain>();

	public List<IdentifierDomain> getIdentifierDomains() {
        return getDomainsFromCache();
	}

	@SuppressWarnings("unchecked")
	public List<String> getIdentifierDomainTypeCodes() {
		String sql = "select distinct i.universalIdentifierTypeCode from IdentifierDomain i where i.universalIdentifierTypeCode is not null order by i.universalIdentifierTypeCode";
		List<String> codes = (List<String>) getHibernateTemplate().find(sql);
		log.trace("Obtained the list of universal identifier type codes of size " + codes.size() + " entries.");
		return codes;
	}

	public IdentifierDomain findIdentifierDomain(final IdentifierDomain identifierDomain) {
	    List<IdentifierDomain> domains = getDomainsFromCache();
	    if (identifierDomain.getIdentifierDomainId() != null) {
	        for (IdentifierDomain domain : domains) {
	            if (domain.getIdentifierDomainId().intValue() == identifierDomain.getIdentifierDomainId()) {
	                return domain;
	            }
	        }
	    }

	    // Search for it by name, namespace identifier, or universal identifier and type code.
        for (IdentifierDomain domain : domains) {
            if (identifierDomain.getIdentifierDomainName() != null &&
                    identifierDomain.getIdentifierDomainName().equals(domain.getIdentifierDomainName())) {
                return domain;
            }
            if (identifierDomain.getNamespaceIdentifier() != null &&
                    identifierDomain.getNamespaceIdentifier().equals(domain.getNamespaceIdentifier())) {
                return domain;
            }
            if (identifierDomain.getUniversalIdentifier() != null &&
                    identifierDomain.getUniversalIdentifier().equals(domain.getUniversalIdentifier()) &&
                    identifierDomain.getUniversalIdentifierTypeCode() != null &&
                    identifierDomain.getUniversalIdentifierTypeCode().equals(domain.getUniversalIdentifierTypeCode())) {
                return domain;
            }
        }
        return null;
	}

	@SuppressWarnings("unchecked")
	public IdentifierDomain findIdentifierDomainByName(final String identifierDomainName) {
           return (IdentifierDomain) getHibernateTemplate().execute(new HibernateCallback() {
                @SuppressWarnings("unchecked")
                public Object doInHibernate(Session session) throws HibernateException, SQLException {

                    Criteria criteria = session.createCriteria(IdentifierDomain.class);
                    criteria.add(Restrictions.eq("identifierDomainName", identifierDomainName));
                    List<IdentifierDomain> list = criteria.list();
                    log.debug("Query by identifier returned: " + list.size() + " elements.");
                    if (list.size() == 0) {
                        return null;
                    }
                    IdentifierDomain entry = list.get(0);
                    Hibernate.initialize(entry);
                    return entry;
                }
           });
    }

    @SuppressWarnings("unchecked")
	public IdentifierDomain findIdentifierDomainById(final Integer id) {
           return (IdentifierDomain) getHibernateTemplate().execute(new HibernateCallback() {
                public Object doInHibernate(Session session) throws HibernateException, SQLException {

                    Criteria criteria = session.createCriteria(IdentifierDomain.class);
                    criteria.add(Restrictions.eq("identifierDomainId", id));
                    List<IdentifierDomain> list = criteria.list();
                    log.debug("Query by identifier returned: " + list.size() + " elements.");
                    if (list.size() == 0) {
                        return null;
                    }
                    IdentifierDomain entry = list.get(0);
                    Hibernate.initialize(entry);
                    return entry;
                }
           });
    }

    public void update(Observable o, Object eventData) {
        if (!(o instanceof EventObservable)) {
            return;
        }
        EventObservable event = (EventObservable) o;
        if (event.getType() != ObservationEventType.IDENTIFIER_DOMAIN_UPDATE_EVENT) {
            return;
        }
        // Reload the cache from the disk since something has changed.
        identifierDomainCache.clear();
        getDomainsFromCache();
    }

	public void addIdentifierDomain(IdentifierDomain identifierDomain) {
		getHibernateTemplate().saveOrUpdate(identifierDomain);
		getHibernateTemplate().flush();
		log.debug("Finished saving the identifier domain.");
		addIdentifierDomainToCache(identifierDomain);
	}

	public void removeIdentifierDomain(IdentifierDomain identifierDomain) {
		getHibernateTemplate().delete(identifierDomain);
		getHibernateTemplate().flush();
		log.debug("Removed an identifier domain instance.");
	}

    public void saveIdentifierDomain(IdentifierDomain identifierDomain) {
        log.debug("Looking for existing identifier domain " + identifierDomain);
        IdentifierDomain idFound = findIdentifierDomain(identifierDomain);
        if (idFound != null) {
            identifierDomain.setIdentifierDomainId(idFound.getIdentifierDomainId());
            identifierDomain.setIdentifierDomainName(idFound.getIdentifierDomainName());
            log.debug("Identifier domain already exists: " + identifierDomain);
            return;
        }
 
        if (identifierDomain.getDateCreated() == null) {
            identifierDomain.setDateCreated(new Date());
            identifierDomain.setUserCreatedBy(Context.getUserContext().getUser());
        }

        if (identifierDomain.getIdentifierDomainName() == null ) {
            String domainName = (identifierDomain.getNamespaceIdentifier() != null) ? 
                    identifierDomain.getNamespaceIdentifier() : identifierDomain.getUniversalIdentifier();
           identifierDomain.setIdentifierDomainName(domainName);
        }
        addIdentifierDomain(identifierDomain);
    }

	@SuppressWarnings("unchecked")
	public boolean isKnownUniversalIdentifierTypeCode(String universalIdentifierTypeCode) {
		String queryString = "from IdentifierDomain i where i.universalIdentifierTypeCode = ?";
		List<IdentifierDomain> domains = getHibernateTemplate().find(queryString, universalIdentifierTypeCode);
		if (domains.size() == 0) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public IdentifierDomainAttribute addIdentifierDomainAttribute(IdentifierDomain identifierDomain, String attributeName, String attributeValue) {
		if (identifierDomain == null || identifierDomain.getIdentifierDomainId() == null) {
			log.debug("User attempted to add identifier domain attribute for an unknown identifier domain: " + identifierDomain);
			return null;
		}
		IdentifierDomain foundIdentifierDomain = (IdentifierDomain) getHibernateTemplate().get(IdentifierDomain.class, identifierDomain.getIdentifierDomainId());
		if (foundIdentifierDomain == null) {
			log.debug("User attempted to add identifier domain attribute for an unknown identifier domain: " + identifierDomain);
			return null;
		}
		// Now check to see if this attribute already exists in which case this should be an update operation
		String queryString = "from IdentifierDomainAttribute i where i.identifierDomainId = ? and i.attributeName = ?";
		List<IdentifierDomainAttribute> attribs = (List<IdentifierDomainAttribute>) getHibernateTemplate().find(queryString,
				new Object[] {foundIdentifierDomain.getIdentifierDomainId(), attributeName});
		if (attribs.size() > 0) {
			IdentifierDomainAttribute attribute = attribs.get(0);
			log.debug("User attempted to add an attribute that already exists in the repository: " + attribute);
			throw new RuntimeException("This attribute already exists in the repository.");
		}
		IdentifierDomainAttribute attribute = new IdentifierDomainAttribute(foundIdentifierDomain.getIdentifierDomainId(), attributeName, attributeValue);
		getHibernateTemplate().saveOrUpdate(attribute);
		getHibernateTemplate().flush();
		log.debug("Finished saving the identifier domain attribute: " + attribute);
		return attribute;
	}

	@SuppressWarnings("unchecked")
	public IdentifierDomainAttribute getIdentifierDomainAttribute(IdentifierDomain identifierDomain, String attributeName) {
		if (identifierDomain == null || identifierDomain.getIdentifierDomainId() == null || attributeName == null) {
			log.debug("User attempted to retrieve identifier domain attribute without providing the appropriate query criteria.");
			return null;
		}
		String queryString = "from IdentifierDomainAttribute i where i.identifierDomainId = ? and i.attributeName = ?";
		List<IdentifierDomainAttribute> attribs = (List<IdentifierDomainAttribute>) getHibernateTemplate().find(queryString,
				new Object[] {identifierDomain.getIdentifierDomainId(), attributeName});
		if (attribs.size() == 0) {
			return null;
		}
		IdentifierDomainAttribute attrib = attribs.get(0);
		log.trace("Loaded the identifier domain attribute: " + attrib);
		return attrib;
	}

	@SuppressWarnings("unchecked")
	public List<IdentifierDomainAttribute> getIdentifierDomainAttributes(IdentifierDomain identifierDomain) {
		if (identifierDomain == null || identifierDomain.getIdentifierDomainId() == null) {
			log.debug("User attempted to retrieve list of identifier domain attributes without providing the appropriate query criteria.");
			return null;
		}
		String queryString = "from IdentifierDomainAttribute i where i.identifierDomainId = ?";
		List<IdentifierDomainAttribute> attribs = (List<IdentifierDomainAttribute>) getHibernateTemplate().find(queryString,
				new Object[] {identifierDomain.getIdentifierDomainId()});
		log.trace("Loaded list of identifier domain attributes: " + attribs);
		return attribs;
	}

	public void updateIdentifierDomainAttribute(IdentifierDomainAttribute identifierDomainAttribute) {
		if (identifierDomainAttribute == null || 
				identifierDomainAttribute.getIdentifierDomainAttributeId() == null) {
			log.debug("User attempted to update identifier domain attribute without providing the appropriate query criteria.");
			return;
		}
		getHibernateTemplate().update(identifierDomainAttribute);
		getHibernateTemplate().flush();
		log.trace("Updated the identifier domain attribute: " + identifierDomainAttribute);
	}

	public void removeIdentifierDomainAttribute(IdentifierDomainAttribute identifierDomainAttribute) {
		if (identifierDomainAttribute == null || identifierDomainAttribute.getIdentifierDomainAttributeId() == null) {
			log.debug("User attempted to delete an identifier domain attribute without providing the appropriate query criteria.");
			return;
		}
		getHibernateTemplate().delete(identifierDomainAttribute);
		getHibernateTemplate().flush();
		log.debug("Removed an identifier domain instance.");
	}

    private List<IdentifierDomain> getDomainsFromCache() {
        if (identifierDomainCache.size() == 0) {
            @SuppressWarnings("unchecked")
            List<IdentifierDomain> domains = (List<IdentifierDomain>) getHibernateTemplate().find("from IdentifierDomain");
            log.info("Obtained the list of identifier domains with " + domains.size() + " entries.");
            synchronized(identifierDomainCache) {
                identifierDomainCache.clear();
                identifierDomainCache.addAll(domains);
                if (!registeredUpdateListener) {
                    Context.registerObserver(this, ObservationEventType.IDENTIFIER_DOMAIN_UPDATE_EVENT);
                    log.info("Registered the listener for the event: " + ObservationEventType.IDENTIFIER_DOMAIN_UPDATE_EVENT);
                    registeredUpdateListener = true;
                }
            }
        }
        return identifierDomainCache;
    }

    private void addIdentifierDomainToCache(IdentifierDomain identifierDomain) {
        synchronized(identifierDomainCache) {
            identifierDomainCache.add(identifierDomain);
        }
    }

    public int getIdentifierUpdateEventCount(User eventRecipient) {
        Query query = getSession()
                .createQuery("select count(*) from IdentifierUpdateEvent as i where i.updateRecipient = :updateRecipient");
        query.setParameter("updateRecipient", eventRecipient);

        int count = ((Long) query.uniqueResult()).intValue();
        return count;
    }

    public IdentifierUpdateEvent addIdentifierUpdateEvent(IdentifierUpdateEvent identifierUpdateEvent) {
        getHibernateTemplate().merge(identifierUpdateEvent);
        getHibernateTemplate().flush();
        return null;
    }

    public void removeIdentifierUpdateEvent(IdentifierUpdateEvent identifierUpdateEvent) {
        if (identifierUpdateEvent == null || identifierUpdateEvent.getIdentifierUpdateEventId() == null) {
            log.debug("User attempted to delete an identifier update event without providing the appropriate query criteria.");
            return;
        }

        getHibernateTemplate().setFlushMode(HibernateAccessor.FLUSH_ALWAYS);
        IdentifierUpdateEvent deleteIdentifierUpdateEvent = findIdentifierUpdateEvent(identifierUpdateEvent
                .getIdentifierUpdateEventId());

        if (deleteIdentifierUpdateEvent == null) {
            return;
        }
/*
        Set<IdentifierUpdateEntry> preUpdateIdentifierEntries = deleteIdentifierUpdateEvent.getPreUpdateIdentifiers();
        Set<IdentifierUpdateEntry> postUpdateIdentifierEntries = deleteIdentifierUpdateEvent.getPostUpdateIdentifiers();

        for (IdentifierUpdateEntry preUpdateIdentifierEntry : preUpdateIdentifierEntries) {
            getHibernateTemplate().delete(preUpdateIdentifierEntry);
        }

        for (IdentifierUpdateEntry postUpdateIdentifierEntry : postUpdateIdentifierEntries) {
            getHibernateTemplate().delete(postUpdateIdentifierEntry);
        }
*/
        getHibernateTemplate().delete(deleteIdentifierUpdateEvent);
        getHibernateTemplate().flush();
        log.debug("Removed identifierUpdateEvent instance.");
    }

    public IdentifierUpdateEvent findIdentifierUpdateEvent(long identifierUpdateEventId) {
        IdentifierUpdateEvent identifierUpdateEvent = (IdentifierUpdateEvent) getHibernateTemplate().get(
                IdentifierUpdateEvent.class, identifierUpdateEventId);
        return identifierUpdateEvent;
    }

    public List<IdentifierUpdateEvent> getIdentifierUpdateEvents(final int startIndex, final int maxEvents,
            final User eventRecipient) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<IdentifierUpdateEvent> identifierUpdateEvents = (List<IdentifierUpdateEvent>) getHibernateTemplate()
                .execute(new HibernateCallback()
                {
                    public Object doInHibernate(Session session) throws HibernateException, SQLException {
                        Query query = session
                                .createQuery("from IdentifierUpdateEvent i where i.updateRecipient = :updateRecipient order by i.identifierUpdateEventId");
                        query.setParameter("updateRecipient", eventRecipient);
                        query.setFirstResult(startIndex);
                        query.setMaxResults(maxEvents);
                        log.debug("Querying using " + query.toString());
                        List<IdentifierUpdateEvent> list = (List<IdentifierUpdateEvent>) query.setResultTransformer(
                                Criteria.DISTINCT_ROOT_ENTITY).list();
                        log.debug("Query returned: " + list.size() + " elements.");
                        return list;
                    }
                });
        return identifierUpdateEvents;
    }

    public List<IdentifierUpdateEvent> getIdentifierUpdateEvents(final User eventRecipient) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<IdentifierUpdateEvent> identifierUpdateEvents = (List<IdentifierUpdateEvent>) getHibernateTemplate()
                .execute(new HibernateCallback()
                {
                    public Object doInHibernate(Session session) throws HibernateException, SQLException {
                        Query query = session
                                .createQuery("from IdentifierUpdateEvent i where i.updateRecipient = :updateRecipient"
                                        + " order by i.identifierUpdateEventId");
                        query.setParameter("updateRecipient", eventRecipient);
                        log.debug("Querying using " + query.toString());
                        List<IdentifierUpdateEvent> list = (List<IdentifierUpdateEvent>) query.setResultTransformer(
                                Criteria.DISTINCT_ROOT_ENTITY).list();
                        log.debug("Query returned: " + list.size() + " elements.");
                        return list;
                    }
                });
        return identifierUpdateEvents;
    }

    public List<IdentifierUpdateEvent> getIdentifierUpdateEventsByDate(final Date startDate, final User eventRecipient) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<IdentifierUpdateEvent> identifierUpdateEvents = (List<IdentifierUpdateEvent>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from IdentifierUpdateEvent i where i.dateCreated > :startDate and i.updateRecipient = :updateRecipient " +
                        "order by i.dateCreated");
                query.setParameter("startDate", startDate);
                query.setParameter("updateRecipient", eventRecipient);
                log.debug("Querying using " + query.toString());
                List<IdentifierUpdateEvent> list = (List<IdentifierUpdateEvent>) query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                        .list();

                log.debug("Query returned: " + list.size() + " elements.");
                return list;
            }
        });
        return identifierUpdateEvents;
    }

    public List<IdentifierUpdateEvent> getIdentifierUpdateEventsBeforeDate(final Date startDate,
            final User eventRecipient) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<IdentifierUpdateEvent> identifierUpdateEvents = (List<IdentifierUpdateEvent>) getHibernateTemplate()
                .execute(new HibernateCallback()
                {
                    public Object doInHibernate(Session session) throws HibernateException, SQLException {
                        Query query = session
                                .createQuery("from IdentifierUpdateEvent i where i.dateCreated < :startDate and i.updateRecipient = :updateRecipient "
                                        + "order by i.dateCreated");
                        query.setParameter("startDate", startDate);
                        query.setParameter("updateRecipient", eventRecipient);
                        log.debug("Querying using " + query.toString());
                        List<IdentifierUpdateEvent> list = (List<IdentifierUpdateEvent>) query.setResultTransformer(
                                Criteria.DISTINCT_ROOT_ENTITY).list();

                        log.debug("Query returned: " + list.size() + " elements.");
                        return list;
                    }
                });
        return identifierUpdateEvents;
    }
}
