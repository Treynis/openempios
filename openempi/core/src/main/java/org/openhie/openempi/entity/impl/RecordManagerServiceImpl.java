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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.InitializationException;
import org.openhie.openempi.blocking.BlockingService;
import org.openhie.openempi.blocking.RecordPairIterator;
import org.openhie.openempi.blocking.RecordPairSource;
import org.openhie.openempi.configuration.CustomField;
import org.openhie.openempi.configuration.GlobalIdentifier;
import org.openhie.openempi.configuration.UpdateNotificationRegistrationEntry;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.dao.IdentifierDomainDao;
import org.openhie.openempi.dao.UniversalDao;
import org.openhie.openempi.entity.DataAccessIntent;
import org.openhie.openempi.entity.RecordCacheManager;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.entity.PersistenceLifecycleObserver;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.matching.MatchingService;
import org.openhie.openempi.model.AuditEventType;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.IdentifierUpdateEvent;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.model.RecordPair;
import org.openhie.openempi.notification.EventType;
import org.openhie.openempi.notification.NotificationEvent;
import org.openhie.openempi.notification.NotificationEventFactory;
import org.openhie.openempi.notification.ObservationEventType;
import org.openhie.openempi.service.impl.UpdateEventNotificationGenerator;
import org.openhie.openempi.transformation.TransformationService;
import org.openhie.openempi.util.ConvertUtil;

import com.eaio.uuid.UUID;

public class RecordManagerServiceImpl extends RecordCommonServiceImpl implements RecordManagerService, PersistenceLifecycleObserver
{
	private static final int TRANSACTION_BLOCK_SIZE = 10000;
    private static final int RECORD_BLOCK_SIZE = 10000;
	private static boolean initialized = false;

	private EntityDefinitionManagerService entityDefinitionService;
	private UpdateEventNotificationGenerator notificationGenerator;
	private RecordCacheManager entityCacheManager;
	private IdentifierDomainDao identifierDomainDao;
	private EntityDao entityDao;
	private UniversalDao universalDao;
	   
    private  List<UpdateNotificationRegistrationEntry> updateNotificationEntries =
            new java.util.ArrayList<UpdateNotificationRegistrationEntry>();
    
	public RecordManagerServiceImpl() {
	}

    public Record loadRecord(Entity entity, Long id) {
        try {
            Record recordFound = entityDao.loadRecord(entity, id);
            return recordFound;
        } catch (Exception e) {
            log.error("Failed while trying to load record by id: " + " due to " + e, e);
            return null;
        }
    }

	public Record addRecord(Entity entity, Record record) throws ApplicationException {
		if (record == null) {
			return null;
		}

		if (entity == null || entity.getEntityVersionId() == null) {
			log.debug("Attempted to store an entity instance without specifying the entity type.");
			throw new ApplicationException("Failed to store an entity instance of unspecified type.");
		}

		Entity entityDef = entityDefinitionService.loadEntity(entity.getEntityVersionId());
		if (entityDef == null) {
			log.debug("Attempted to store an entity instance of an unknown entity type.");
			throw new ApplicationException("Failed to store an entity instance of unknown type.");
		}
        
		// Identifier domain already exists, setIdentifierDomainId to identifier, otherwise save a new IdentifierDomain
		for (Identifier identifier : record.getIdentifiers()) {
			identifierDomainDao.saveIdentifierDomain(identifier.getIdentifierDomain());
		}

        validateRecord(entity, record);
        validateNoNewGlobalIdentifier(record, null);
        
		// Before we save the entry we need to generate any custom
		// fields that have been requested through configuration
		populateCustomFields(record);

		try {
			record = entityDao.saveRecord(entity, record);

			// Audit the event that a new record entry was created.
			Context.getAuditEventService().saveAuditEventEntry(AuditEventType.ADD_RECORD_EVENT_TYPE, "Added a new record", entity.getName(), record);

			// Now we need to check for matches and if any are found, establish links among the aliases
			RecordState state = new RecordState(record.getRecordId(), IdentifierUpdateEvent.ADD_SOURCE);
			findAndProcessAddRecordLinks(record, state);
            getUpdateEventNotificationGenerator().generateEvents(state);

			// Generate a notification event to inform interested listeners that this event has occurred.
			NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.ADD_EVENT_TYPE, record);
			Context.getNotificationService().fireNotificationEvent(event);

			// Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
			Context.notifyObserver(ObservationEventType.ENTITY_ADD_EVENT, record);

			return record;
		} catch (Exception e) {
            log.error("Failed while adding a record: " + e, e);
			throw new ApplicationException(e.getMessage());
		}
	}

    public Set<Record> addRecords(Entity entity, Collection<Record> records) throws ApplicationException {
        if (records == null || records.size() == 0) {
            return new HashSet<Record>();
        }

        if (entity == null || entity.getEntityVersionId() == null) {
            log.debug("Attempted to store a set of records without specifying the entity type.");
            throw new ApplicationException("Failed to store a set of records of unspecified type.");
        }

        Entity entityDef = entityDefinitionService.loadEntity(entity.getEntityVersionId());
        if (entityDef == null) {
            log.debug("Attempted to store an entity instance of an unknown entity type.");
            throw new ApplicationException("Failed to store an entity instance of unknown type.");
        }

        // Identifier domain already exists, setIdentifierDomainId to identifier, otherwise save a new IdentifierDomain
        for (Record record : records) {
            for (Identifier identifier : record.getIdentifiers()) {
                identifierDomainDao.saveIdentifierDomain(identifier.getIdentifierDomain());
            }
            validateRecord(entity, record);
            validateNoNewGlobalIdentifier(record, null);

            // Before we save the entry we need to generate any custom
            // fields that have been requested through configuration
            populateCustomFields(record);
        }


        try {
            Set<Record> savedRecords = entityDao.saveRecords(entity, records);

            for (Record record : savedRecords) {

                // Now we need to check for matches and if any are found, establish links among the aliases
                RecordState state = new RecordState(record.getRecordId(), IdentifierUpdateEvent.ADD_SOURCE);
                findAndProcessAddRecordLinks(record, state);
                getUpdateEventNotificationGenerator().generateEvents(state);

                // Generate a notification event to inform interested listeners that this event has occurred.
                NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.ADD_EVENT_TYPE, record);
                Context.getNotificationService().fireNotificationEvent(event);

                // Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
                Context.notifyObserver(ObservationEventType.ENTITY_ADD_EVENT, record);
            }
            return savedRecords;
        } catch (Exception e) {
            log.error("Failed while adding a record: " + e, e);
            throw new ApplicationException(e.getMessage());
        }
    }

	public Record importRecord(Entity entity, Record record) throws ApplicationException {
		if (record == null) {
			return null;
		}

		if (entity == null || entity.getEntityVersionId() == null) {
			log.debug("Attempted to store an entity instance without specifying the entity type.");
			throw new ApplicationException("Failed to store an entity instance of unspecified type.");
		}

		Entity entityDef = entityDefinitionService.loadEntity(entity.getEntityVersionId());
		if (entityDef == null) {
			log.debug("Attempted to store an entity instance of an unknown entity type.");
			throw new ApplicationException("Failed to store an entity instance of unknown type.");
		}

        // Identifier domain already exists, setIdentifierDomainId to identifier, otherwise save a new IdentifierDomain
        for (Identifier identifier : record.getIdentifiers()) {
            identifierDomainDao.saveIdentifierDomain(identifier.getIdentifierDomain());
        }

		validateRecord(entity, record);
        validateNoNewGlobalIdentifier(record, null);

		try {
			record = entityDao.saveRecord(entity, record);

			// Audit the event that record entry was imported.
			Context.getAuditEventService().saveAuditEventEntry(AuditEventType.IMPORT_RECORD_EVENT_TYPE, "Imported record", entity.getName(), record);

			// Generate a notification event to inform interested listeners that this event has occurred.
			NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.ADD_EVENT_TYPE, record);
			Context.getNotificationService().fireNotificationEvent(event);

			// Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
			Context.notifyObserver(ObservationEventType.ENTITY_ADD_EVENT, record);

			return record;
		} catch (Exception e) {
            log.error("Failed while importing a record: " + e, e);
			throw new ApplicationException(e.getMessage());
		}
	}

    public Set<Record> importRecords(Entity entity, Collection<Record> records) throws ApplicationException {
        if (records == null || records.size() == 0) {
            return new HashSet<Record>();
        }

        if (entity == null || entity.getEntityVersionId() == null) {
            log.debug("Attempted to store a set of records without specifying the entity type.");
            throw new ApplicationException("Failed to store a set of records instance of unspecified type.");
        }

        Entity entityDef = entityDefinitionService.loadEntity(entity.getEntityVersionId());
        if (entityDef == null) {
            log.debug("Attempted to store a set of records of an unknown entity type.");
            throw new ApplicationException("Failed to store a set of records of unknown type.");
        }

        // Identifier domain already exists, setIdentifierDomainId to identifier, otherwise save a new IdentifierDomain
        for (Record record : records) {
            for (Identifier identifier : record.getIdentifiers()) {
                identifierDomainDao.saveIdentifierDomain(identifier.getIdentifierDomain());
            }
            validateRecord(entity, record);
        }

        try {
            Set<Record> savedRecords = entityDao.saveRecords(entity, records);

            for (Record record : savedRecords) {
                // Generate a notification event to inform interested listeners that this event has occurred.
                NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.ADD_EVENT_TYPE, record);
                Context.getNotificationService().fireNotificationEvent(event);

                // Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
                Context.notifyObserver(ObservationEventType.ENTITY_ADD_EVENT, record);
            }
            return savedRecords;
        } catch (Exception e) {
            log.error("Failed while importing a set of records: " + e, e);
            throw new ApplicationException(e.getMessage());
        }
    }

    public RecordLink loadRecordLink(Entity entity, String id) {
        try {
            RecordLink recordFound = entityDao.loadRecordLink(entity, id);
            return recordFound;
        } catch (Exception e) {
            log.error("Failed while trying to load record link by id: " + " due to " + e, e);
            return null;
        }
    }

	public RecordLink addRecordLink(RecordLink link) throws ApplicationException {
		if (link == null || link.getLeftRecord() == null || link.getRightRecord() == null) {
			return null;
		}

		if (link.getLeftRecord().getEntity() == null || link.getLeftRecord().getEntity().getEntityVersionId() == null) {
			log.debug("Attempted to store an entity link without specifying the entity type of the associated entities.");
			throw new ApplicationException("Failed to store a record link of unspecified type.");
		}

		Entity entityDef = entityDefinitionService.loadEntity(link.getLeftRecord().getEntity().getEntityVersionId());
		if (entityDef == null) {
			log.debug("Attempted to store an entity link of an unknown entity type in the associated entities.");
			throw new ApplicationException("Failed to store a record link of unknown type.");
		}

		validateRecordLink(entityDef, link);
		try {
			link = entityDao.saveRecordLink(link);
			return link;
		} catch (Exception e) {
			throw new ApplicationException(e.getMessage());
		}
	}

	public RecordLink updateRecordLink(RecordLink link) throws ApplicationException {
		if (link == null || link.getLeftRecord() == null || link.getRightRecord() == null || link.getRecordLinkId() == null) {
			log.debug("Attempted to update an unknown record link: " + link);
			throw new ApplicationException("Failed to update an unknown record link.");
		}

		if (link.getLeftRecord().getEntity() == null || link.getLeftRecord().getEntity().getEntityVersionId() == null) {
			log.debug("Attempted to store an entity link without specifying the entity type of the associated entities.");
			throw new ApplicationException("Failed to store a record link of unspecified type.");
		}

		Entity entityDef = entityDefinitionService.loadEntity(link.getLeftRecord().getEntity().getEntityVersionId());
		if (entityDef == null) {
			log.debug("Attempted to store an entity link of an unknown entity type in the associated entities.");
			throw new ApplicationException("Failed to store a record link of unknown type.");
		}

		RecordLink loadedLink = entityDao.loadRecordLink(entityDef, link.getRecordLinkId());
		if (loadedLink == null) {
			log.debug("Attempted to update an unknown record link: " + link);
			throw new ApplicationException("Failed to update an unknown record link.");
		}

		// A request can only modify the state of the record link so, if the
		// states are the same, there is nothing to do
		if (loadedLink.getState().getState().equalsIgnoreCase(link.getState().getState())) {
			return link;
		}
		loadedLink.setState(link.getState());
		loadedLink.setDateReviewed(new Date());
		loadedLink.setUserReviewedBy(Context.getUserContext().getUser());
		try {
			link = entityDao.saveRecordLink(link);

			if( link.getState() == RecordLinkState.MATCH ) {

				// Audit the event that two record entries were linked.
				Context.getAuditEventService().saveAuditEventEntry(AuditEventType.LINK_RECORD_EVENT_TYPE, "Linked two records", entityDef.getName(), link.getLeftRecord(), link.getRightRecord() );				
			}

			if( link.getState() == RecordLinkState.NON_MATCH ) {

				// Audit the event that two record entries were unlinked.
				Context.getAuditEventService().saveAuditEventEntry(AuditEventType.UNLINK_RECORD_EVENT_TYPE, "Unlinked two records", entityDef.getName(), link.getLeftRecord(), link.getRightRecord() );				
			}

			return link;
		} catch (Exception e) {
			throw new ApplicationException(e.getMessage());
		}
	}

    public void removeRecordLink(RecordLink link) throws ApplicationException {
        if (link == null || link.getLeftRecord() == null || link.getRightRecord() == null) {
            return;
        }

        if (link.getLeftRecord().getEntity() == null || link.getLeftRecord().getEntity().getEntityVersionId() == null) {
            log.debug("Attempted to remove an entity link without specifying the entity type of the associated entities.");
            throw new ApplicationException("Failed to remove a record link of unspecified type.");
        }

        Entity entityDef = entityDefinitionService.loadEntity(link.getLeftRecord().getEntity().getEntityVersionId());
        if (entityDef == null) {
            log.debug("Attempted to remove an entity link of an unknown entity type in the associated entities.");
            throw new ApplicationException("Failed to remove a record link of unknown type.");
        }

        validateRecordLink(entityDef, link);
        try {
            entityDao.removeRecordLink(link);
        } catch (Exception e) {
            throw new ApplicationException(e.getMessage());
        }
    }

	public Record updateRecord(Entity entity, Record record) throws ApplicationException {
		if (record == null) {
			return null;
		}

		if (entity == null || entity.getEntityVersionId() == null) {
			log.debug("Attempted to update a record without specifying the entity type.");
			throw new ApplicationException("Failed to update a record of unspecified type.");
		}

		Entity entityDef = entityDefinitionService.loadEntity(entity.getEntityVersionId());
		if (entityDef == null) {
			log.debug("Attempted to update a record of an unknown entity type.");
			throw new ApplicationException("Failed to update a record of unknown type.");
		}

		if (record.getRecordId() == null) {
			log.debug("Attempted to update a record that is is not known to the system; no record ID was specified.");
			throw new ApplicationException("Failed to update a record that is not known to the system.");
		}

		validateRecord(entity, record);
		try {
			Record recordFound = entityDao.loadRecord(entity, record.getRecordId());
			if (recordFound == null) {
				log.debug("Attempted to update a record that is is not known to the system.");
				throw new ApplicationException("Failed to update a record that is not known to the system.");
			}
			Record recordOriginal = (Record) ConvertUtil.cloneBean(recordFound);

			// Before we save the entry we need to generate any custom
			// fields that have been requested through configuration
			populateCustomFields(record);

            RecordState state = new RecordState(record.getRecordId(), IdentifierUpdateEvent.UPDATE_SOURCE);
			record = entityDao.updateRecord(entity, record);
	
			findAndUpdateRecordLinks(record, state);
			getUpdateEventNotificationGenerator().generateEvents(state);

			// Audit the event that an existing record entry was updated.
			Context.getAuditEventService().saveAuditEventEntry(AuditEventType.UPDATE_RECORD_EVENT_TYPE, "Updated an existing person record", entity.getName(), record);

			// Generate a notification event to inform interested listeners that this event has occurred.
			NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.UPDATE_EVENT_TYPE, record);
			Context.getNotificationService().fireNotificationEvent(event);

			// Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
			Context.notifyObserver(ObservationEventType.ENTITY_UPDATE_EVENT, new Object[]{recordOriginal, record});

			return record;
		} catch (Exception e) {
			throw new ApplicationException(e.getMessage());
		}
	}

	public Record deleteRecord(Entity entity, Record record) throws ApplicationException {
		if (record == null || record.getRecordId() == null) {
			return null;
		}

		if (entity == null || entity.getEntityVersionId() == null) {
			log.debug("Attempted to delete an entity instance without specifying the entity type.");
			throw new ApplicationException("Failed to update an entity instance of unspecified type.");
		}

		Entity entityDef = entityDefinitionService.loadEntity(entity.getEntityVersionId());
		if (entityDef == null) {
			log.debug("Attempted to delete an entity instance of an unknown entity type.");
			throw new ApplicationException("Failed to update an entity instance of unknown type.");
		}

		validateRecord(entity, record);
		try {
			entityDao.deleteRecord(entity, record);

			// Audit the event that a record entry was deleted.
			Context.getAuditEventService().saveAuditEventEntry(AuditEventType.DELETE_RECORD_EVENT_TYPE, "Deleted a record", entity.getName(), record);

			// Generate a notification event to inform interested listeners that this event has occurred.
			NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.DELETE_EVENT_TYPE, record);
			Context.getNotificationService().fireNotificationEvent(event);

			// Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
			Context.notifyObserver(ObservationEventType.ENTITY_DELETE_EVENT, record);

			return record;
		} catch (Exception e) {
			throw new ApplicationException(e.getMessage());
		}
	}

	public List<Record> deleteRecordByIdentifier(Entity entity, Identifier identifier) throws ApplicationException {
		if (identifier == null) {
			return null;
		}

		List<Record> records = entityDao.findRecordsByIdentifier(entity, identifier);

		for (Record record : records) {
			 validateRecord(entity, record);
			 entityDao.deleteRecord(entity, record);

			 // Audit the event that a record entry was deleted.
			 Context.getAuditEventService().saveAuditEventEntry(AuditEventType.DELETE_RECORD_EVENT_TYPE, "Deleted a record", entity.getName(), record);

			 // Generate a notification event to inform interested listeners that this event has occurred.
			 NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.DELETE_EVENT_TYPE, record);
			 Context.getNotificationService().fireNotificationEvent(event);

			 // Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
			 Context.notifyObserver(ObservationEventType.ENTITY_DELETE_EVENT, record);
		}
		return records;
	}

	public Record removeRecord(Entity entity, Record record) throws ApplicationException {

	    if (record == null || record.getRecordId() == null) {
			return null;
		}

		if (entity == null || entity.getEntityVersionId() == null) {
			log.debug("Attempted to remove an entity instance without specifying the entity type.");
			throw new ApplicationException("Failed to remove an entity instance of unspecified type.");
		}

		Record recordToDelete = entityDao.loadRecord(entity, record.getRecordId());
		if (recordToDelete == null) {
			log.debug("Attempted to remove a record that is not known to the system: " + record.getRecordId());
			throw new ApplicationException("The record to be deleted is not in the repository.");
		}
		entityDao.removeRecord(entity, recordToDelete);

		// Generate a notification event to inform interested listeners that this event has occurred.
		NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.DELETE_EVENT_TYPE, record);
		Context.getNotificationService().fireNotificationEvent(event);

		// Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
		Context.notifyObserver(ObservationEventType.ENTITY_DELETE_EVENT, record);

		return recordToDelete;
	}

	public void initializeRepository(Entity entity) throws ApplicationException {
		if (entity == null || entity.getName() == null) {
			log.debug("Attempted to initialize the repository for an entity without specifying the entity type.");
			throw new ApplicationException("Failed to initialize the repository for an entity of unspecified type.");
		}

		log.info("Initializing the repository for entity " + entity.getName() + 
				" from the beginning using the underlying matching algorithm to do so.");
		MatchingService matchingService = Context.getMatchingService();
		matchingService.initializeRepository();
		linkAllRecordPairs(entity);
	}

	public void linkAllRecordPairs(Entity entity) throws ApplicationException {
		clearAllLinks(entity);
		MatchingService matchingService = Context.getMatchingService();
		BlockingService blockingService = Context.getBlockingService();
		RecordPairSource recordPairSource = blockingService.getRecordPairSource(entity);
		int pairCount=0;
		List<RecordLink> links = new java.util.ArrayList<RecordLink>();
		Map<RecordLink,RecordLink> mapOfLinks = new HashMap<RecordLink,RecordLink>();
		for (RecordPairIterator iter = recordPairSource.iterator(); iter.hasNext(); ) {
			RecordPair pair = iter.next();
			pairCount++;
			pair = matchingService.match(pair);
			RecordLink link = ConvertUtil.createRecordLinkFromRecordPair(pair);
			if  (!(pair.getMatchOutcome() == RecordPair.MATCH_OUTCOME_UNLINKED) && (!mapOfLinks.containsKey(link))) {
				log.warn("Record link " + link + " does not exist yet so it will be persisted.");
				links.add(link);
				mapOfLinks.put(link, link);
			} else {
				log.warn("Record link " + link + " already exists so it will not be persisted.");
			}

			if (links.size() == 10000) {
				log.warn("Finished persisting a block of " + links.size() + " links out of a total of: " + mapOfLinks.keySet().size());
				entityDao.saveRecordLinks(links);
				links.clear();
			}

			if (pairCount % TRANSACTION_BLOCK_SIZE == 0) {
				log.warn("Finished linking " + pairCount + " record pairs.");
			}
		}
		log.warn("Finished persisting a block of " + links.size() + " links out of a total of " + mapOfLinks.size());
		entityDao.saveRecordLinks(links);
		log.warn("In initializing the repository, we evaluated " + pairCount + " record pairs.");
	}

	public void clearAllLinks(Entity entity) {
		MatchingService matchingService = Context.getMatchingService();
		// Remove all the current links in the system by the current matching algorithm.
		LinkSource linkSource = new LinkSource(matchingService.getMatchingServiceId());
		entityDao.removeRecordLinksBySource(entity, linkSource, null);
	}

	public void declareIntent(Entity entity, DataAccessIntent intent) {
        if (entity == null || entity.getEntityVersionId() == null) {
            log.warn("Attempted to declare intent without specifying the entity type.");
            return;
        }
        entityDao.declareIntent(entity, intent);
	}
    
    public boolean assignGlobalIdentifier(Entity entity) throws ApplicationException {
        GlobalIdentifier globalIdentifier = Context.getConfiguration().getGlobalIdentifier();
        if (!globalIdentifier.isAssignGlobalIdentifier()) {
            log.info("The system is not configured for global identifiers so global identifiers will not be assigned.");
            return true;
        }
        
        if (entity == null || entity.getEntityVersionId() == null) {
            log.debug("Attempted to assign global identifiers without specifying the entity type.");
            throw new ApplicationException("Failed to assign global identifiers to records of an "
                    + "entity instance of unspecified type.");         
        }
        
        IdentifierDomain domain = getPersistedIdentifierDomain(globalIdentifier.getIdentifierDomain());
        boolean done = false;
        java.util.Map<Long,Long> idsToProcess = new java.util.HashMap<Long,Long>();
        int start = 0;
        int maxCount = RECORD_BLOCK_SIZE;
        while (!done) {
            log.warn("Assigning global ids for block of size " + maxCount + " starting at index " + start);
            List<Record> recordBlock = entityDao.findRecordsWithoutIdentifierInDomain(entity, domain, false, start, maxCount);
            if (recordBlock.size() == 0) {
                done = true;
                continue;
            }
            for (Record record : recordBlock) {
                assignGlobalIdentifier(domain, entity, record, idsToProcess, false);
            }
            start += recordBlock.size();
            if (recordBlock.size() <  RECORD_BLOCK_SIZE) {
                done = true;
            }
        }
        
        start = 0;
        while (!done) {
            log.warn("Assigning global ids with links for block of size " + maxCount + " starting at index " + start);
            List<Record> recordBlock = entityDao.findRecordsWithoutIdentifierInDomain(entity, domain, true, start, maxCount);
            if (recordBlock.size() == 0) {
                done = true;
                continue;
            }
            for (Record record : recordBlock) {
                assignGlobalIdentifier(domain, entity, record, idsToProcess, true);
            }
            start += recordBlock.size();
        }
        return true;
    }
    
    private void assignGlobalIdentifier(IdentifierDomain domain, Entity entity, Record record, Map<Long, Long> ids,
            boolean hasLinks) throws ApplicationException {
        // If already processed through a linked entry then skip it
        log.info("Assigning global id to record " + record.getRecordId());
        if (ids.get(record.getRecordId()) != null) {
                return;
        }
        ids.put(record.getRecordId(), record.getRecordId());
        if (log.isDebugEnabled()) {
            log.debug("Assigning global identifier to record " + record.getRecordId());
        }
        Identifier globalIdentifier=null;
        if (!hasLinks) {
            globalIdentifier = generateGlobalIdentifier(domain, record);
            globalIdentifier.setDateCreated(new java.util.Date());
            globalIdentifier.setUserCreatedBy(Context.getUserContext().getUser());
            if (log.isDebugEnabled()) {
                log.debug("Assigning newly generated global identifier " + globalIdentifier.getIdentifier() +
                        " to record " + record.getRecordId());
            }
            entityDao.updateRecord(entity, record);
            ids.put(record.getRecordId(), record.getRecordId());
        } else {
            List<Record> linkedRecords = entityDao.loadRecordLinksById(entity, record.getRecordId());
            globalIdentifier = getGlobalIdentifierFromLinks(domain, linkedRecords);
            if (globalIdentifier != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Assigning global identifier " + globalIdentifier.getIdentifier() + " to record " +
                            record.getRecordId() + " obtained from linked entry.");
                }
                globalIdentifier = cloneGlobalIdentifier(globalIdentifier);
                globalIdentifier.setRecord(record);
                record.addIdentifier(globalIdentifier);
                entityDao.updateRecord(entity, record);
                ids.put(record.getRecordId(), record.getRecordId());
             // None of them have a global identifier so generate one and assign it to every entry in the cluster
            } else {
                globalIdentifier = generateGlobalIdentifier(domain, record);
                globalIdentifier.setDateCreated(new java.util.Date());
                globalIdentifier.setUserCreatedBy(Context.getUserContext().getUser());
                if (log.isDebugEnabled()) {
                    log.debug("Assigning newly generated global identifier " + globalIdentifier.getIdentifier() +
                        " to record" + record.getRecordId());
                }
                entityDao.updateRecord(entity, record);
                ids.put(record.getRecordId(), record.getRecordId());
                for (Record linkedRecord : linkedRecords) {
                    Identifier gid = cloneGlobalIdentifier(globalIdentifier);
                    gid.setRecord(linkedRecord);
                    linkedRecord.addIdentifier(gid);
                    entityDao.updateRecord(entity, linkedRecord);
                    ids.put(linkedRecord.getRecordId(), linkedRecord.getRecordId());
                }
            }
        }
    }

    private Identifier extractGlobalIdentifier(Record record) {
        IdentifierDomain globalIdentifierDomain = Context.getConfiguration().getGlobalIdentifierDomain();
        for (Identifier identifier : record.getIdentifiers()) {
            if (identifier.getIdentifierDomain() != null &&
                    identifier.getIdentifierDomain().getIdentifierDomainName() != null &&
                    identifier.getIdentifierDomain().getIdentifierDomainName()
                        .equalsIgnoreCase(globalIdentifierDomain.getIdentifierDomainName())) {
                if (log.isDebugEnabled()) {
                    log.debug("Found global identifier in linked record of :" + identifier);                    
                }
                return identifier;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Unable to find global identifier in existing record to be linked.");
        }
        return null;
    }

    private Identifier cloneGlobalIdentifier(Identifier identifier) {
        Identifier id = new Identifier();
        id.setDateCreated(identifier.getDateCreated());
        id.setIdentifier(identifier.getIdentifier());
        id.setIdentifierDomain(identifier.getIdentifierDomain());
        id.setUserCreatedBy(identifier.getUserCreatedBy());
        return id;
    }

    private Identifier getGlobalIdentifierFromLinks(IdentifierDomain domain, List<Record> records) {
        for (Record record: records) {
            for (Identifier identifier : record.getIdentifiers()) {
                if (identifier.getIdentifierDomain().equals(domain)) {
                    return identifier;
                }
            }
        }
        return null;
    }
    
    private void validateNoNewGlobalIdentifier(Record record, Record recordFound) throws ApplicationException {
        // If we are not handling global identifier assignment, then there is nothing to do
        if (!Context.getConfiguration().getGlobalIdentifier().isAssignGlobalIdentifier()) {
            return;
        }

        String identifier = getGlobalIdentifier(record, Context.getConfiguration().getGlobalIdentifierDomain());
        if (identifier == null) {
            return;
        }

        if (recordFound == null) {
            log.warn("Caller attempted to add a global identifier to a record.");
            throw new ApplicationException(
                    "Identifiers in the global identifier domain must be assigned only by the system itself.");
        }

        String identifierExisting = getGlobalIdentifier(recordFound, Context.getConfiguration()
                .getGlobalIdentifierDomain());
        if (identifierExisting == null || !identifier.equals(identifierExisting)) {
            log.warn("Caller attempted to add a global identifier to a record.");
            throw new ApplicationException(
                    "Identifiers in the global identifier domain must be assigned only by the system itself.");
        }
    }

    private String getGlobalIdentifier(Record record, IdentifierDomain domain) {
        for (Identifier identifier : record.getIdentifiers()) {
            if (domain.getIdentifierDomainName().equals(identifier.getIdentifierDomain().getIdentifierDomainName())) {
                return identifier.getIdentifier();
            }
        }
        return null;
    }

    private IdentifierDomain getPersistedIdentifierDomain(IdentifierDomain identifierDomain) {
        IdentifierDomain identifierDomainFound = identifierDomainDao.findIdentifierDomain(identifierDomain);
        if (identifierDomainFound == null) {
            identifierDomain.setDateCreated(new Date());
            identifierDomain.setUserCreatedBy(Context.getUserContext().getUser());
            identifierDomainDao.addIdentifierDomain(identifierDomain);
            return identifierDomain;
        }
        return identifierDomainFound;
    }
    
    private Identifier generateGlobalIdentifier(IdentifierDomain globalIdentifierDomain, Record record) {
        UUID uuid = new UUID();
        Identifier identifier = new Identifier();
        identifier.setIdentifier(uuid.toString());
        identifier.setIdentifierDomain(globalIdentifierDomain);
        identifier.setRecord(record);
        record.addIdentifier(identifier);
        identifier.setDateCreated(new java.util.Date());
        identifier.setUserCreatedBy(Context.getUserContext().getUser());
        return identifier;
    }

    private void removeGlobalIdentifier(Record record, IdentifierDomain globalIdentifierDomain) {
        List<Identifier> toBeRemoved = new java.util.ArrayList<Identifier>();
        for (Identifier id : record.getIdentifiers()) {
            if (id.getIdentifierDomain() != null &&
                    id.getIdentifierDomain().getIdentifierDomainName() != null &&
                    id.getIdentifierDomain().getIdentifierDomainName()
                        .equalsIgnoreCase(globalIdentifierDomain.getIdentifierDomainName())) {
                toBeRemoved.add(id);
            }
        } 
        record.getIdentifiers().removeAll(toBeRemoved);
    }

    private boolean hasGlobalIdentifier(Record record, IdentifierDomain globalIdentifierDomain) {
        for (Identifier id : record.getIdentifiers()) {
            if (id.getIdentifierDomain() != null &&
                    id.getIdentifierDomain().getIdentifierDomainName() != null &&
                    id.getIdentifierDomain().getIdentifierDomainName()
                        .equalsIgnoreCase(globalIdentifierDomain.getIdentifierDomainName())) {
                return true;
            }
        }
        return false;
    }

	private boolean isValidIdentifier(Identifier identifier) throws ApplicationException {
		if (identifier == null) {
			throw new ApplicationException("The identifier value is null.");
		}

		if (identifier.getIdentifier() == null || identifier.getIdentifier().length() == 0 ||
				identifier.getIdentifierDomain() == null || 
				(identifier.getIdentifierDomain().getIdentifierDomainId() == null && 
				        identifier.getIdentifierDomain().getIdentifierDomainName() == null)) {
			throw new ApplicationException("The identifier value is not valid.");
		}

		IdentifierDomain domain = entityCacheManager.getIdentifierDomain(identifier.getIdentifierDomain().getIdentifierDomainId());
		if (domain == null) {
//		    throw new ApplicationException("The identifier domain is not known.");
		    // Has a domain but it is not known; we will need to add it.
		    return true;
		}
		identifier.setIdentifierDomain(domain);
		return true;
	}

	private void findAndUpdateRecordLinks(Record record, RecordState state) throws ApplicationException {
	    List<RecordLink> currLinks = entityDao.loadRecordLinks(record.getEntity(), record.getRecordId());
	    Set<RecordLink> preLinks = new HashSet<RecordLink>();
	    state.setPreLinks(preLinks);
	    for (RecordLink link : currLinks) {
	        if (log.isDebugEnabled()) {
	            log.debug("Deleting the record link during an update; " + link);
	            preLinks.add(link);
	            entityDao.removeRecordLink(link);
	        }
	    }
	    findAndProcessAddRecordLinks(record, state);
	}
	
	private Set<RecordLink> findAndProcessAddRecordLinks(Record record, RecordState state) throws ApplicationException {

		// Call the matching service to find any record pairs that must be linked
		MatchingService matchingService = Context.getMatchingService();
		Set<RecordPair> pairs = matchingService.match(record);

		// If no matching records are found then return an empty list
		Set<RecordLink> links = new HashSet<RecordLink>();
		if (pairs.size() == 0) {
            if (Context.getConfiguration().getGlobalIdentifier().isAssignGlobalIdentifier() &&
                    !hasGlobalIdentifier(record, Context.getConfiguration().getGlobalIdentifierDomain())) {                
                generateGlobalIdentifier(Context.getConfiguration().getGlobalIdentifierDomain(), record);
                entityDao.updateRecord(record.getEntity(), record);
            }		    
		    capturePostIdentifiers(record, state);
			return links;
		}

		if (log.isDebugEnabled()) {
			log.debug("While adding node " + record.getRecordId() + " found links to nodes " + pairs);
		}

		Set<RecordInPair> targetRecords = getTargetRecords(record, pairs);

		Identifier globalIdentifier = null;
		for (RecordInPair recordInPair : targetRecords) {
            if (Context.getConfiguration().getGlobalIdentifier().isAssignGlobalIdentifier() &&
                    globalIdentifier == null) {
                globalIdentifier = extractGlobalIdentifier(recordInPair.getRecord());
                if (globalIdentifier != null) {
                    removeGlobalIdentifier(record, Context.getConfiguration().getGlobalIdentifierDomain());
                    globalIdentifier = cloneGlobalIdentifier(globalIdentifier);
                    record.addIdentifier(globalIdentifier);
                    globalIdentifier.setRecord(record);
                    entityDao.updateRecord(record.getEntity(), record);
                } else {
                    log.warn("Found an existing record that doesn't have a global identifier: " + 
                            recordInPair.getRecord().getEntity() + "," + recordInPair.getRecord().getRecordId());
                }
            }
			RecordLink link = createRecordLink(record, recordInPair);
			links.add(link);
			if (log.isDebugEnabled()) {
				log.debug("Creating record link: " + link);
			}
			entityDao.saveRecordLink(link);
		}
        state.getPostLinks().addAll(links);
		return links;
	}

    private RecordLink createRecordLink(Record record, RecordInPair recordInPair) {
		RecordLink link = new RecordLink();
		link.setLeftRecord(record);
		link.setRightRecord(recordInPair.getRecord());
		link.setWeight(recordInPair.getPair().getWeight());
		link.setVector(recordInPair.getPair().getVector());
		link.setDateCreated(new Date());
		link.setUserCreatedBy(Context.getUserContext().getUser());
		if (recordInPair.getPair().getMatchOutcome() == RecordPair.MATCH_OUTCOME_LINKED) {
			link.setState(RecordLinkState.MATCH);
		} else if (recordInPair.getPair().getMatchOutcome() == RecordPair.MATCH_OUTCOME_POSSIBLE) {
			link.setState(RecordLinkState.POSSIBLE_MATCH);
		} else {
			log.error("A link was encountered in an invalid state: " + recordInPair.getPair());
		}
		link.setLinkSource(recordInPair.getPair().getLinkSource());
		log.info("Create entity link: " + link);
		return link;
	}

	// The matching algorithm has identified a number of pairs that need be persisted.
	// Each record pair points to two entities, the one that was used to identify other matches
	// and the matching records. This method extracts from each record pair the other record
	// (not the one that initiated the matching process).
	//
	private Set<RecordInPair> getTargetRecords(Record record, Set<RecordPair> pairs) {

		Set<RecordInPair> targets = new HashSet<RecordInPair>();
		for (RecordPair pair : pairs) {
			if (pair.getLeftRecord().getRecordId().equals(record.getRecordId())) {
				RecordInPair target = new RecordInPair(pair.getRightRecord(), pair);
				targets.add(target);
			} else if (pair.getRightRecord().getRecordId().equals(record.getRecordId())) {
				RecordInPair target = new RecordInPair(pair.getLeftRecord(), pair);
				targets.add(target);
			} else {
				log.warn("THIS SHOULD NOT HAPPEN: We found a record pair where neither of the records are the initiating one: " + pair);
			}
		}
		return targets;
	}

    private RecordState capturePostIdentifiers(Record record, RecordState state) {
        Set<Identifier> postIdentifiers = copyIdentifiers(record);
        state.setPostIdentifiers(postIdentifiers);
        return state;
    }
    
    private Set<Identifier> copyIdentifiers(Record record) {
        Set<Identifier> ids = new HashSet<Identifier>();
        for (Identifier id : record.getIdentifiers()) {
            if (id.getDateVoided() != null) {
                continue;
            }
            Identifier newId = new Identifier();
            newId.setIdentifier(id.getIdentifier());
            newId.setIdentifierDomain(id.getIdentifierDomain());            
            ids.add(newId);
        }
        return ids;
    }
    
    public UpdateEventNotificationGenerator getUpdateEventNotificationGenerator() {
        if (notificationGenerator == null) {
            notificationGenerator = new UpdateEventNotificationGenerator(getIdentifierDomainDao(),
                    getUpdateNotificationRegistrationEntries());
        }
        return notificationGenerator;
    }
    
    public List<UpdateNotificationRegistrationEntry> getUpdateNotificationRegistrationEntries() {
        updateNotificationEntries = Context.getConfiguration().getAdminConfiguration()
                .getUpdateNotificationRegistrationEntries();
        return updateNotificationEntries;
    }
    
	private void validateRecord(Entity entity, Record record) throws ApplicationException {
		// TODO: Need to apply the validation rules that are attached to the entity
		// on the record attributes.

		// Validate the identifiers if there are any present
		if (record.getIdentifiers() == null || record.getIdentifiers().size() == 0) {
			return;
		}

		for (Identifier identifier : record.getIdentifiers()) {
			isValidIdentifier(identifier);
		}
	}

	public void populateCustomFields(Person person) {
		List<CustomField> customFields = Context.getConfiguration().getCustomFields();
		TransformationService transformationService = Context.getTransformationService();
		for (CustomField customField : customFields) {
			log.trace("Need to generate a value for field " + customField.getSourceFieldName() + " using function " +
					customField.getTransformationFunctionName() + " and save it as field " + customField.getFieldName());
			try {
				Object value = PropertyUtils.getProperty(person, customField.getSourceFieldName());
				log.debug("Obtained a value of " + value + " for field " + customField.getSourceFieldName());
				if (value != null) {
					Object transformedValue = transformationService.transform(customField.getTransformationFunctionName(), value, customField.getConfigurationParameters());
					PropertyUtils.setProperty(person, customField.getFieldName(), transformedValue);
					log.debug("Custom field " + customField.getFieldName() + " has value " + BeanUtils.getProperty(person,
							customField.getFieldName()));
				}
			} catch (Exception e) {
				log.error("Failed while trying to obtain property for field " + customField.getSourceFieldName() + ":" + e.getMessage(), e);
			}
		}
	}

	public void generateCustomFields(Entity entity) throws ApplicationException {
		if (entity == null || entity.getName() == null) {
			log.debug("Attempted to generate custom fields for an entity without specifying the entity type.");
			throw new ApplicationException("Failed to generate custom fields for an entity of unspecified type.");
		}
		log.info("Re-generating all the custom fields in the repository for entity: " + entity.getName());
		long recordCount = entityDao.getRecordCount(entity);
		if (recordCount == 0) {
			log.info("Finished generating custom fields for " + recordCount + " records.");
			return;
		}
		log.info("Will generate custom fields for " + recordCount + " records.");
		int start=0;
		int blockSize = TRANSACTION_BLOCK_SIZE;
		while (start < recordCount) {
			List<Record> records = entityDao.loadRecords(entity, start, blockSize);
			generateCustomFields(entity, records);
			start += records.size();
			log.info("Finished generating custom fields for " + start + " records.");
		}
	}

	private void generateCustomFields(Entity entity, final List<Record> records) throws ApplicationException {
		for (Record record : records) {
			populateCustomFields(record);
		}
		entityDao.updateRecords(entity, records);
	}

	private void validateRecordLink(Entity entityDef, RecordLink link) {
		// TODO: Need to make sure that the link has all the required
		// fields: weight, date created, etc. and that the state of it is
		// in one of the proper values based on the EntityLinkState
		// enumeration.
		if (link.getRecordLinkId() == null) {
			link.setDateCreated(new Date());
		}
	    if (link.getUserCreatedBy() == null) {
	        link.setUserCreatedBy(Context.getUserContext().getUser());
	    }
	}

	public IdentifierDomainDao getIdentifierDomainDao() {
		return identifierDomainDao;
	}

	public void setIdentifierDomainDao(IdentifierDomainDao identifierDomainDao) {
		this.identifierDomainDao = identifierDomainDao;
	}

	public EntityDao getEntityDao() {
		return entityDao;
	}

	public void setEntityDao(EntityDao entityDao) {
		this.entityDao = entityDao;
	}

	public EntityDefinitionManagerService getEntityDefinitionService() {
		return entityDefinitionService;
	}

	public void setEntityDefinitionService(EntityDefinitionManagerService entityDefinitionService) {
		this.entityDefinitionService = entityDefinitionService;
	}

	public synchronized void startup() throws InitializationException {
		if (initialized) {
			return;
		}
		List<Entity> entities = entityDefinitionService.loadEntities();
		for (Entity entity : entities) {
			entityDao.initializeStore(entity);
		}
		initialized = true;
	}

	public synchronized void initializeStore(Entity entity) throws InitializationException {
			entityDao.initializeStore(entity);
	}

	public void shutdownStore(Entity entity) {
		entityDao.shutdownStore(entity);
	}

	public boolean isReady() {
		return true;
	}

	public void shutdown() {
		if (!initialized) {
			return;
		}
		List<Entity> entities = entityDefinitionService.loadEntities();
		for (Entity entity : entities) {
			entityDao.shutdownStore(entity);
		}
		initialized = true;
	}

	public boolean isDown() {
		// TODO Auto-generated method stub
		return false;
	}

	public UniversalDao getUniversalDao() {
		return universalDao;
	}

	public void setUniversalDao(UniversalDao universalDao) {
		this.universalDao = universalDao;
	}

	public RecordCacheManager getEntityCacheManager() {
		return entityCacheManager;
	}

	public void setEntityCacheManager(RecordCacheManager entityCacheManager) {
		this.entityCacheManager = entityCacheManager;
	}

	private class RecordInPair
	{
		private RecordPair pair;
		private Record record;

		public RecordInPair(Record record, RecordPair pair) {
			this.pair = pair;
			this.record = record;
		}

		public RecordPair getPair() {
			return pair;
		}

		public Record getRecord() {
			return record;
		}
	}	
}
