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
package org.openhie.openempi.entity.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.openhie.openempi.dao.IdentifierDomainDao;
import org.openhie.openempi.dao.UserDao;
import org.openhie.openempi.entity.RecordCacheManager;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.User;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class EntityCacheManagerImpl implements RecordCacheManager
{
	private static final String IDENTIFIER_DOMAIN_CACHE = "identifierDomainCache";
	private static final String USER_CACHE = "userCache";
	private static final int MAX_IDENTIFIER_DOMAIN_ELEMENTS = 5000;
	private static final int MAX_USER_ELEMENTS = 5000;

	private Logger log = Logger.getLogger(getClass());
	private static CacheManager cacheManager;
	private static Cache identifierDomainCache;
	private static Cache userCache;
	private static boolean initialized = false;
	private IdentifierDomainDao identifierDomainDao;
	private UserDao userDao;
	private String cacheConfigLocation;
	
	public synchronized void initialize() {
		if (!initialized) {
			cacheManager = CacheManager.create(getCacheConfigLocation());
	
			// Create a Cache for storing identifier domains
			identifierDomainCache = new Cache(
					new CacheConfiguration(IDENTIFIER_DOMAIN_CACHE, MAX_IDENTIFIER_DOMAIN_ELEMENTS)
			    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
			    .eternal(false)
			    .timeToLiveSeconds(0)
			    .timeToIdleSeconds(0));
			cacheManager.addCache(identifierDomainCache);
			populateIdentifierDomainCache(identifierDomainCache);
			
			// Create a Cache for storing users
			userCache = new Cache(
					new CacheConfiguration(USER_CACHE, MAX_USER_ELEMENTS)
			    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
			    .eternal(false)
			    .timeToLiveSeconds(0)
			    .timeToIdleSeconds(0));
			cacheManager.addCache(userCache);
			populateUserCache(userCache);
			initialized = true;
		}
	}

	public IdentifierDomain getIdentifierDomain(Integer identifierDomainId) {
		Element element = identifierDomainCache.get(identifierDomainId);
		if (element == null) {
		    IdentifierDomain domain = identifierDomainDao.findIdentifierDomainById(identifierDomainId);
		    if (domain != null) {
		        addOrUpdateCacheEntry(identifierDomainCache, domain.getIdentifierDomainId(), domain);
		        return domain;
		    }
			log.warn("Unable to find identifier domain with id: " + identifierDomainId);
			return null;
		}
		return (IdentifierDomain) element.getValue();
	}

	public User getUser(Long userId) {
		Element element = userCache.get(userId);
		if (element == null) {
			log.warn("Unable to find user with id: " + userId);
			return null;
		}
		return (User) element.getValue();
	}
	
	private void populateUserCache(Cache cache) {
		List<User> users = userDao.getUsers();
		for (User user : users) {
			addOrUpdateCacheEntry(cache, user.getId(), user);
		}
	}

	private void populateIdentifierDomainCache(Cache cache) {
		List<IdentifierDomain> domains = identifierDomainDao.getIdentifierDomains();
		for (IdentifierDomain domain : domains) {
			addOrUpdateCacheEntry(cache, domain.getIdentifierDomainId(), domain);
		}
	}
	
	private void addOrUpdateCacheEntry(Cache cache, Object key, Object value) {
		Element element = cache.get(key);
		if (element != null) {
			if (log.isDebugEnabled()) {
				log.debug("Updating entry in the cache " + cache.getName() + " with key: " + key + " and value: " + value);
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Adding entry in the cache " + cache.getName() + " with key: " + key + " and value: " + value);
			}
		}
		element = new Element(key, value);			
		cache.put(element);
	}

	public IdentifierDomainDao getIdentifierDomainDao() {
		return identifierDomainDao;
	}

	public void setIdentifierDomainDao(IdentifierDomainDao identifierDomainDao) {
		this.identifierDomainDao = identifierDomainDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	public String getCacheConfigLocation() {
	    return cacheConfigLocation;
	}
	
	public void setCacheConfigLocation(String cacheConfigLocation) {
	    this.cacheConfigLocation = cacheConfigLocation;
	}
}
