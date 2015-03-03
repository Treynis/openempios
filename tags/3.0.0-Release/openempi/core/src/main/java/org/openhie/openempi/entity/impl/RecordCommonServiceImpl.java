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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.configuration.ConfigurationRegistry;
import org.openhie.openempi.configuration.CustomField;
import org.openhie.openempi.configuration.UpdateNotificationRegistrationEntry;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.dao.IdentifierDomainDao;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.matching.MatchingService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.model.RecordPair;
import org.openhie.openempi.service.impl.BaseServiceImpl;
import org.openhie.openempi.service.impl.UpdateEventNotificationGenerator;
import org.openhie.openempi.transformation.TransformationService;

import com.eaio.uuid.UUID;

public class RecordCommonServiceImpl extends BaseServiceImpl
{
    private EntityDao entityDao;
    private UpdateEventNotificationGenerator notificationGenerator;
    private IdentifierDomainDao identifierDomainDao;
    
    private  List<UpdateNotificationRegistrationEntry> updateNotificationEntries =
         new java.util.ArrayList<UpdateNotificationRegistrationEntry>();

	protected void populateCustomFields(Entity entity, Record record) {
		@SuppressWarnings("unchecked")
		Map<String,List<CustomField>> customFieldsListByEntityName = (Map<String, List<CustomField>>) Context
				.getConfiguration().lookupConfigurationEntry(entity.getName(),
				        ConfigurationRegistry.CUSTOM_FIELD_LIST_BY_ENTITY_NAME_MAP);
		List<CustomField> customFields = customFieldsListByEntityName.get(entity.getName());
		if (customFields == null) {
			if (log.isDebugEnabled()) {
				log.debug("No custom fields have been defined for entity " + entity.getName());
			}
			return;
		}
		
		TransformationService transformationService = Context.getTransformationService();
		for (CustomField customField : customFields) {
			log.trace("Need to generate a value for field " + customField.getSourceFieldName() + " using function " +
					customField.getTransformationFunctionName() + " and save it as field " + customField.getFieldName());
			try {
				Object value = record.get(customField.getSourceFieldName());
				if (log.isTraceEnabled()) {
				    log.trace("Obtained a value of " + value + " for field " + customField.getSourceFieldName());
				}
				if (value != null) {
					Object transformedValue = transformationService.transform(customField.getTransformationFunctionName(), value, customField.getConfigurationParameters());
					record.set(customField.getFieldName(), transformedValue);
					if (log.isTraceEnabled()) {
					    log.debug("Custom field " + customField.getFieldName() + " has value " + record.get(customField.getFieldName()));
					}
				}
			} catch (Exception e) {
				log.error("Failed while trying to obtain property for field " + customField.getSourceFieldName() + ":" + e.getMessage(), e);
			}
		}
	}

    protected void findAndUpdateRecordLinks(Entity entity, Record record, RecordState state) throws ApplicationException {
        List<RecordLink> currLinks = entityDao.loadRecordLinks(record.getEntity(), record.getRecordId());
        Set<RecordLink> preLinks = new HashSet<RecordLink>();
        state.setPreLinks(preLinks);
        for (RecordLink link : currLinks) {
            if (log.isDebugEnabled()) {
                log.debug("Deleting the record link during an update; " + link);
            }
            link = entityDao.loadRecordLink(record.getEntity(), link.getRecordLinkId());
            preLinks.add(link);
            entityDao.removeRecordLink(link);
        }
        findAndProcessAddRecordLinks(entity, record, state);
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
    
    protected Set<RecordLink> findAndProcessAddRecordLinks(Entity entity, Record record, RecordState state) throws ApplicationException {

        // Call the matching service to find any record pairs that must be linked
        MatchingService matchingService = Context.getMatchingService(entity.getName());
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

    protected void persistRecordPairs(Record record, Set<RecordPair> pairs, RecordState state) throws ApplicationException {
        Set<RecordLink> links = new HashSet<RecordLink>();
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
            if (log.isDebugEnabled()) {
                log.debug("Creating record link: " + link);
            }
            entityDao.saveRecordLink(link);
        }
        state.getPostLinks().addAll(links);
    }

    // The matching algorithm has identified a number of pairs that need be persisted.
    // Each record pair points to two entities, the one that was used to identify other matches
    // and the matching records. This method extracts from each record pair the other record
    // (not the one that initiated the matching process).
    //
    protected Set<RecordInPair> getTargetRecords(Record record, Set<RecordPair> pairs) {

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

    protected RecordState capturePostIdentifiers(Record record, RecordState state) {
        Set<Identifier> postIdentifiers = copyIdentifiers(record);
        state.setPostIdentifiers(postIdentifiers);
        return state;
    }

    protected RecordLink createRecordLink(Record record, RecordInPair recordInPair) {
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
    
    protected Identifier cloneGlobalIdentifier(Identifier identifier) {
        Identifier id = new Identifier();
        id.setDateCreated(identifier.getDateCreated());
        id.setIdentifier(identifier.getIdentifier());
        id.setIdentifierDomain(identifier.getIdentifierDomain());
        id.setUserCreatedBy(identifier.getUserCreatedBy());
        return id;
    }
    
    protected Set<Identifier> copyIdentifiers(Record record) {
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
    
    protected Identifier extractGlobalIdentifier(Record record) {
        IdentifierDomain globalIdentifierDomain = Context.getConfiguration().getGlobalIdentifierDomain();
        for (Identifier identifier : record.getIdentifiers()) {
            if (identifier.getIdentifierDomain().getIdentifierDomainName()
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
    
    protected Identifier generateGlobalIdentifier(IdentifierDomain globalIdentifierDomain, Record record) {
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

    protected void removeGlobalIdentifier(Record record, IdentifierDomain globalIdentifierDomain) {
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
    
    protected boolean hasGlobalIdentifier(Record record, IdentifierDomain globalIdentifierDomain) {
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

    protected class RecordInPair
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
