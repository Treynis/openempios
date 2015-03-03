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

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.InitializationException;
import org.openhie.openempi.blocking.BlockingService;
import org.openhie.openempi.blocking.RecordPairIterator;
import org.openhie.openempi.blocking.RecordPairSource;
import org.openhie.openempi.configuration.GlobalIdentifier;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.dao.UniversalDao;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.PersistenceLifecycleObserver;
import org.openhie.openempi.entity.RecordCacheManager;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.matching.MatchingService;
import org.openhie.openempi.matching.ShallowMatchingService;
import org.openhie.openempi.model.AuditEventType;
import org.openhie.openempi.model.DataAccessIntent;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.IdentifierUpdateEvent;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.model.RecordPair;
import org.openhie.openempi.notification.EventType;
import org.openhie.openempi.notification.NotificationEvent;
import org.openhie.openempi.notification.NotificationEventFactory;
import org.openhie.openempi.notification.ObservationEventType;
import org.openhie.openempi.util.ConvertUtil;

public class RecordManagerServiceImpl extends RecordCommonServiceImpl implements RecordManagerService, PersistenceLifecycleObserver
{
	private static final int TRANSACTION_BLOCK_SIZE = 10000;
    private static final int RECORD_BLOCK_SIZE = 10000;
	private static boolean initialized = false;

	private EntityDefinitionManagerService entityDefinitionService;
	private RecordCacheManager entityCacheManager;
	private UniversalDao universalDao;
    
	public RecordManagerServiceImpl() {
	}

    public Record loadRecord(Entity entity, Long id) {
        try {
            Record recordFound = getEntityDao().loadRecord(entity, id);
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
			getIdentifierDomainDao().saveIdentifierDomain(identifier.getIdentifierDomain());
		}

        validateRecord(entity, record);
        validateNoNewGlobalIdentifier(record, null);
        
		// Before we save the entry we need to generate any custom
		// fields that have been requested through configuration
		populateCustomFields(entity, record);

		Set<RecordPair> shallowMatchedPairs = null;
		try {
		    if (entity.getSynchronousMatching()) {
		        record.setDirty(false);
		        record = getEntityDao().saveRecord(entity, record);
		    } else {
                RecordState state = new RecordState(record.getRecordId(), IdentifierUpdateEvent.ADD_SOURCE);
		        ShallowMatchingService matching = Context.getShallowMatchingService(entity.getName());
		        if (matching == null) {
		            record.setDirty(true);
		        } else {
		            shallowMatchedPairs = matching.match(record);
		            if (shallowMatchedPairs.size() == 0) {
		                record.setDirty(true);
		            } else {
		                record.setDirty(false);
		            }
		        }
                record = getEntityDao().saveRecord(entity, record);
                persistRecordPairs(record, shallowMatchedPairs, state);
                getUpdateEventNotificationGenerator().generateEvents(state);
		    }

			// Audit the event that a new record entry was created.
			Context.getAuditEventService().saveAuditEventEntry(AuditEventType.ADD_RECORD_EVENT_TYPE, "Added a new record", entity.getName(), record);

            if (entity.getSynchronousMatching()) {
                if (log.isTraceEnabled()) {
                    log.trace("Evaluating the match status of record in synchronous mode.");
                }
                // Now we need to check for matches and if any are found, establish links among the aliases
                RecordState state = new RecordState(record.getRecordId(), IdentifierUpdateEvent.ADD_SOURCE);
                findAndProcessAddRecordLinks(entity, record, state);
                getUpdateEventNotificationGenerator().generateEvents(state);
            }

			// Generate a notification event to inform interested listeners that this event has occurred.
			NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.ADD_EVENT_TYPE, record);
			Context.getNotificationService().fireNotificationEvent(event);

			// Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
			Context.notifyObserver(ObservationEventType.RECORD_ADD_EVENT, record);

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
                getIdentifierDomainDao().saveIdentifierDomain(identifier.getIdentifierDomain());
            }
            validateRecord(entity, record);
            validateNoNewGlobalIdentifier(record, null);

            // Before we save the entry we need to generate any custom
            // fields that have been requested through configuration
            populateCustomFields(entity, record);
        }

        try {
            Set<Record> savedRecords = null;
            if (entity.getSynchronousMatching()) {
                for (Record record : records) {
                    record.setDirty(false);
                }
                savedRecords = getEntityDao().saveRecords(entity, records);
            } else {
                ShallowMatchingService matching = Context.getShallowMatchingService(entity.getName());
                if (matching == null) {
                    for (Record record : records) {
                        record.setDirty(true);
                    }
                } else {
                    for (Record record : records) {
                        RecordState state = new RecordState(record.getRecordId(), IdentifierUpdateEvent.ADD_SOURCE);                        
                        Set<RecordPair> pairs = matching.match(record);
                        if (pairs.size() == 0) {
                            record.setDirty(true);
                        } else {
                            record.setDirty(false);
                        }
                        persistRecordPairs(record, pairs, state);
                        getUpdateEventNotificationGenerator().generateEvents(state);
                    }
                }
                savedRecords = getEntityDao().saveRecords(entity, records);
            }

            // Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
            Context.notifyObserver(ObservationEventType.RECORDS_ADD_EVENT, records);
            
            for (Record record : savedRecords) {
                if (entity.getSynchronousMatching()) {
                    if (log.isTraceEnabled()) {
                        log.trace("Evaluating the match status of record in synchronous mode.");
                    }

                    // Now we need to check for matches and if any are found, establish links among the aliases
                    RecordState state = new RecordState(record.getRecordId(), IdentifierUpdateEvent.ADD_SOURCE);
                    findAndProcessAddRecordLinks(entity, record, state);
                    getUpdateEventNotificationGenerator().generateEvents(state);
                }

                // Generate a notification event to inform interested listeners that this event has occurred.
                NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.ADD_EVENT_TYPE, record);
                Context.getNotificationService().fireNotificationEvent(event);
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
            getIdentifierDomainDao().saveIdentifierDomain(identifier.getIdentifierDomain());
        }

		validateRecord(entity, record);
        validateNoNewGlobalIdentifier(record, null);

        // Before we save the entry we need to generate any custom
        // fields that have been requested through configuration
        populateCustomFields(entity, record);
		try {
		    record.setDirty(true);
			record = getEntityDao().saveRecord(entity, record);

			// Audit the event that record entry was imported.
			Context.getAuditEventService().saveAuditEventEntry(AuditEventType.IMPORT_RECORD_EVENT_TYPE, "Imported record", entity.getName(), record);

			// Generate a notification event to inform interested listeners that this event has occurred.
			NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.ADD_EVENT_TYPE, record);
			Context.getNotificationService().fireNotificationEvent(event);

			// Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
			Context.notifyObserver(ObservationEventType.RECORD_ADD_EVENT, record);

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
                getIdentifierDomainDao().saveIdentifierDomain(identifier.getIdentifierDomain());
            }
            validateRecord(entity, record);
            // Before we save the entry we need to generate any custom
            // fields that have been requested through configuration
            populateCustomFields(entity, record);
            
            // Imported records are always marked dirty
            record.setDirty(true);
        }

        try {
            Set<Record> savedRecords = getEntityDao().saveRecords(entity, records);

            // Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
            Context.notifyObserver(ObservationEventType.RECORDS_ADD_EVENT, savedRecords);
            for (Record record : savedRecords) {
                // Generate a notification event to inform interested listeners that this event has occurred.
                NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.ADD_EVENT_TYPE, record);
                Context.getNotificationService().fireNotificationEvent(event);
            }
            return savedRecords;
        } catch (Exception e) {
            log.error("Failed while importing a set of records: " + e, e);
            throw new ApplicationException(e.getMessage());
        }
    }

    public RecordLink loadRecordLink(Entity entity, String id) {
        try {
            RecordLink recordFound = getEntityDao().loadRecordLink(entity, id);
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
			link = getEntityDao().saveRecordLink(link);
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

		RecordLink loadedLink = getEntityDao().loadRecordLink(entityDef, link.getRecordLinkId());
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
			link = getEntityDao().saveRecordLink(link);

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
            getEntityDao().removeRecordLink(link);
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
			Record recordFound = getEntityDao().loadRecord(entity, record.getRecordId());
			if (recordFound == null) {
				log.debug("Attempted to update a record that is is not known to the system.");
				throw new ApplicationException("Failed to update a record that is not known to the system.");
			}
			Record recordOriginal = (Record) ConvertUtil.cloneBean(recordFound);
			recordOriginal.setEntity(entity);

			// Before we save the entry we need to generate any custom
			// fields that have been requested through configuration
			populateCustomFields(entity, record);
			
			Set<RecordPair> shallowMatchedPairs = null;
            if (entity.getSynchronousMatching()) {
                record.setDirty(false);
                record = getEntityDao().updateRecord(entity, record);
            } else {
                RecordState state = new RecordState(record.getRecordId(), IdentifierUpdateEvent.UPDATE_SOURCE);
                ShallowMatchingService matching = Context.getShallowMatchingService(entity.getName());
                if (matching == null) {
                    record.setDirty(true);
                } else {
                    shallowMatchedPairs = matching.match(record);
                    if (shallowMatchedPairs.size() == 0) {
                        record.setDirty(true);
                    } else {
                        record.setDirty(false);
                    }
                }
                record = getEntityDao().updateRecord(entity, record);
                persistRecordPairs(record, shallowMatchedPairs, state);
                getUpdateEventNotificationGenerator().generateEvents(state);
            }
            
			if (entity.getSynchronousMatching()) {
			    if (log.isTraceEnabled()) {
			        log.trace("Evaluating the match status of record in synchronous mode.");
			    }
	            RecordState state = new RecordState(record.getRecordId(), IdentifierUpdateEvent.UPDATE_SOURCE);
    			findAndUpdateRecordLinks(entity, record, state);
    			getUpdateEventNotificationGenerator().generateEvents(state);
			}
			
			// Audit the event that an existing record entry was updated.
			Context.getAuditEventService().saveAuditEventEntry(AuditEventType.UPDATE_RECORD_EVENT_TYPE, "Updated an existing person record", entity.getName(), record);

			// Generate a notification event to inform interested listeners that this event has occurred.
			NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.UPDATE_EVENT_TYPE, record);
			Context.getNotificationService().fireNotificationEvent(event);

			// Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
			Context.notifyObserver(ObservationEventType.RECORD_UPDATE_EVENT, new Object[]{recordOriginal, record});

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
			getEntityDao().deleteRecord(entity, record);

			// Audit the event that a record entry was deleted.
			Context.getAuditEventService().saveAuditEventEntry(AuditEventType.DELETE_RECORD_EVENT_TYPE, "Deleted a record", entity.getName(), record);

			// Generate a notification event to inform interested listeners that this event has occurred.
			NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.DELETE_EVENT_TYPE, record);
			Context.getNotificationService().fireNotificationEvent(event);

			// Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
			Context.notifyObserver(ObservationEventType.RECORD_DELETE_EVENT, record);

			return record;
		} catch (Exception e) {
			throw new ApplicationException(e.getMessage());
		}
	}

	public List<Record> deleteRecordByIdentifier(Entity entity, Identifier identifier) throws ApplicationException {
		if (identifier == null) {
			return null;
		}

		List<Record> records = getEntityDao().findRecordsByIdentifier(entity, identifier);

		for (Record record : records) {
			 validateRecord(entity, record);
			 getEntityDao().deleteRecord(entity, record);

			 // Audit the event that a record entry was deleted.
			 Context.getAuditEventService().saveAuditEventEntry(AuditEventType.DELETE_RECORD_EVENT_TYPE, "Deleted a record", entity.getName(), record);

			 // Generate a notification event to inform interested listeners that this event has occurred.
			 NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.DELETE_EVENT_TYPE, record);
			 Context.getNotificationService().fireNotificationEvent(event);

			 // Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
			 Context.notifyObserver(ObservationEventType.RECORD_DELETE_EVENT, record);
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

		Record recordToDelete = getEntityDao().loadRecord(entity, record.getRecordId());
		if (recordToDelete == null) {
			log.debug("Attempted to remove a record that is not known to the system: " + record.getRecordId());
			throw new ApplicationException("The record to be deleted is not in the repository.");
		}
		getEntityDao().removeRecord(entity, recordToDelete);

		// Generate a notification event to inform interested listeners that this event has occurred.
		NotificationEvent event = NotificationEventFactory.createNotificationEvent(EventType.DELETE_EVENT_TYPE, record);
		Context.getNotificationService().fireNotificationEvent(event);

		// Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
		Context.notifyObserver(ObservationEventType.RECORD_DELETE_EVENT, record);

		return recordToDelete;
	}

    public void mergeRecords(Entity entity, Identifier retiredIdentifier, Identifier survivingIdentifier)
            throws ApplicationException {
    }
    
	public void initializeRepository(Entity entity) throws ApplicationException {
		if (entity == null || entity.getName() == null) {
			log.debug("Attempted to initialize the repository for an entity without specifying the entity type.");
			throw new ApplicationException("Failed to initialize the repository for an entity of unspecified type.");
		}

		log.info("Initializing the repository for entity " + entity.getName() + 
				" from the beginning using the underlying matching algorithm to do so.");
		MatchingService matchingService = Context.getMatchingService(entity.getName());
		matchingService.initializeRepository(entity.getName());
        clearAllLinks(entity);
	}

	public void linkAllRecordPairs(Entity entity) throws ApplicationException {
        clearAllLinks(entity);
		MatchingService matchingService = Context.getMatchingService(entity.getName());
		BlockingService blockingService = Context.getBlockingService(entity.getName());
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
			    if (log.isDebugEnabled()) {
			        log.debug("Record link " + link + " does not exist yet so it will be persisted.");
			    }
				links.add(link);
				mapOfLinks.put(link, link);
			} else {
			    if (log.isDebugEnabled()) {
			        log.debug("Record link " + link + " already exists so it will not be persisted.");
			    }
			}

			if (links.size() == 10000) {
				log.info("Finished persisting a block of " + links.size() + " links out of a total of: " + mapOfLinks.keySet().size());
				getEntityDao().saveRecordLinks(links);
				links.clear();
			}

			if (pairCount % TRANSACTION_BLOCK_SIZE == 0) {
				log.info("Finished linking " + pairCount + " record pairs.");
			}
		}
		log.info("Finished persisting a block of " + links.size() + " links out of a total of " + mapOfLinks.size());
		getEntityDao().saveRecordLinks(links);
		log.info("In initializing the repository, we evaluated " + pairCount + " record pairs.");
	}

	public void clearAllLinks(Entity entity) {
		MatchingService matchingService = Context.getMatchingService(entity.getName());
		// Remove all the current links in the system by the current matching algorithm.
		LinkSource linkSource = new LinkSource(matchingService.getMatchingServiceId());
		getEntityDao().removeRecordLinksBySource(entity, linkSource, null);
	}

	public void declareIntent(Entity entity, DataAccessIntent intent) {
        if (entity == null || entity.getEntityVersionId() == null) {
            log.warn("Attempted to declare intent without specifying the entity type.");
            return;
        }
        getEntityDao().declareIntent(entity, intent);
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
        int maxCount = RECORD_BLOCK_SIZE;
        while (!done) {
            List<Record> recordBlock = getEntityDao().findRecordsWithoutIdentifierInDomain(entity, domain, false, 0, maxCount);
            if (recordBlock.size() == 0) {
                done = true;
                continue;
            }
            log.warn("Assigning global ids to block of size " + recordBlock.size());
            for (Record record : recordBlock) {
                assignGlobalIdentifier(domain, entity, record, idsToProcess, false);
            }
            if (recordBlock.size() <  RECORD_BLOCK_SIZE) {
                done = true;
            }
        }
        
        done = false;
        while (!done) {
            List<Record> recordBlock = getEntityDao().findRecordsWithoutIdentifierInDomain(entity, domain, true, 0, maxCount);
            if (recordBlock.size() == 0) {
                done = true;
                continue;
            }
            log.warn("Assigning global ids to block with links of size " + recordBlock.size());
            for (Record record : recordBlock) {
                assignGlobalIdentifier(domain, entity, record, idsToProcess, true);
            }
        }
        return true;
    }
    
    private void assignGlobalIdentifier(IdentifierDomain domain, Entity entity, Record record, Map<Long, Long> ids,
            boolean hasLinks) throws ApplicationException {
        // If already processed through a linked entry then skip it
        if (ids.get(record.getRecordId()) != null) {
                return;
        }
        ids.put(record.getRecordId(), record.getRecordId());
        if (log.isDebugEnabled()) {
            log.debug("Assigning global identifier to record " + record.getRecordId());
        }
        log.debug("Assigning global identifier to record " + record.getRecordId());
        Identifier globalIdentifier=null;
        if (!hasLinks) {
            globalIdentifier = generateGlobalIdentifier(domain, record);
            globalIdentifier.setDateCreated(new java.util.Date());
            globalIdentifier.setUserCreatedBy(Context.getUserContext().getUser());
            if (log.isDebugEnabled()) {
                log.debug("Assigning newly generated global identifier " + globalIdentifier.getIdentifier() +
                        " to record " + record.getRecordId());
            }
            getEntityDao().updateRecord(entity, record);
            ids.put(record.getRecordId(), record.getRecordId());
        } else {
            List<Record> linkedRecords = getEntityDao().loadRecordLinksById(entity, record.getRecordId());
            globalIdentifier = getGlobalIdentifierFromLinks(domain, linkedRecords);
            if (globalIdentifier != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Assigning global identifier " + globalIdentifier.getIdentifier() + " to record " +
                            record.getRecordId() + " obtained from linked entry.");
                }
                globalIdentifier = cloneGlobalIdentifier(globalIdentifier);
                globalIdentifier.setRecord(record);
                record.addIdentifier(globalIdentifier);
                getEntityDao().updateRecord(entity, record);
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
                getEntityDao().updateRecord(entity, record);
                ids.put(record.getRecordId(), record.getRecordId());
                for (Record linkedRecord : linkedRecords) {
                    Identifier gid = cloneGlobalIdentifier(globalIdentifier);
                    gid.setRecord(linkedRecord);
                    linkedRecord.addIdentifier(gid);
                    getEntityDao().updateRecord(entity, linkedRecord);
                    ids.put(linkedRecord.getRecordId(), linkedRecord.getRecordId());
                }
            }
        }
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
        IdentifierDomain identifierDomainFound = getIdentifierDomainDao().findIdentifierDomain(identifierDomain);
        if (identifierDomainFound == null) {
            identifierDomain.setDateCreated(new Date());
            identifierDomain.setUserCreatedBy(Context.getUserContext().getUser());
            getIdentifierDomainDao().addIdentifierDomain(identifierDomain);
            return identifierDomain;
        }
        return identifierDomainFound;
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

	public void generateCustomFields(Entity entity) throws ApplicationException {
		if (entity == null || entity.getName() == null) {
			log.debug("Attempted to generate custom fields for an entity without specifying the entity type.");
			throw new ApplicationException("Failed to generate custom fields for an entity of unspecified type.");
		}
		log.info("Re-generating all the custom fields in the repository for entity: " + entity.getName());
		long recordCount = getEntityDao().getRecordCount(entity);
		if (recordCount == 0) {
			log.info("Finished generating custom fields for " + recordCount + " records.");
			return;
		}
		log.info("Will generate custom fields for " + recordCount + " records.");
		int start=0;
		int blockSize = TRANSACTION_BLOCK_SIZE;
		while (start < recordCount) {
			List<Record> records = getEntityDao().loadRecords(entity, start, blockSize);
			generateCustomFields(entity, records);
			start += records.size();
			log.info("Finished generating custom fields for " + start + " records.");
		}
	}

	private void generateCustomFields(Entity entity, final List<Record> records) throws ApplicationException {
		for (Record record : records) {
			populateCustomFields(entity, record);
		}
		getEntityDao().updateRecords(entity, records);
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
	    link.setLinkSource(new LinkSource(LinkSource.MANUAL_MATCHING_SOURCE));
	    if (link.getState() == null) {
	        link.setState(RecordLinkState.MATCH);
	    }
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
			getEntityDao().initializeStore(entity);
		}
		initialized = true;
	}

	public synchronized void initializeStore(Entity entity) throws InitializationException {
			getEntityDao().initializeStore(entity);
	}

	public void shutdownStore(Entity entity) {
		getEntityDao().shutdownStore(entity);
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
			getEntityDao().shutdownStore(entity);
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
}
