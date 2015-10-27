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

import java.util.ArrayList;
import java.util.List;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.ValidationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.dao.IdentifierDomainDao;
import org.openhie.openempi.dao.PersonDao;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.AuditEventType;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.IdentifierDomainAttribute;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.PersonLink;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.model.ReviewRecordPair;
import org.openhie.openempi.service.PersonManagerService;
import org.openhie.openempi.service.ValidationService;
import org.openhie.openempi.util.ConvertUtil;

import com.eaio.uuid.UUID;

public class PersonManagerServiceAdapter extends BaseServiceImpl implements PersonManagerService
{
	private String personEntityName;
	private Entity entityDef = null;
	
	private EntityDefinitionManagerService entityDefinitionService;
    private RecordQueryService recordQueryService;
	private RecordManagerService recordManagerService;
	protected PersonDao personDao;
    private IdentifierDomainDao identifierDomainDao;
    
	private Entity getEntity() {
		if (entityDef == null) {
			List<Entity> entityDefs = entityDefinitionService.findEntitiesByName(personEntityName);
			if( entityDefs != null && entityDefs.size()>0 ) {
				entityDef = entityDefs.get(0);
			}
		}
		return entityDef;
	}

	public Person addPerson(Person person) throws ApplicationException {
		if (getEntity() != null && person != null) {			
			// convert Person to Record
			Record theRecord = ConvertUtil.getRecordFromPerson(getEntity(), person);
			if (theRecord == null) {
				return null;
			}
			
			Record record = recordManagerService.addRecord(getEntity(), theRecord);
			Person personInstance = null;
			if( record != null) {
				personInstance = ConvertUtil.getPersonFromRecord(personDao, record);
			}
			return personInstance;
		}
		throw new RuntimeException("The entity to support the Person Data model has not been configured properly.");

	}

	public void updatePerson(Person person) throws ApplicationException {
		if (getEntity() != null && person != null) {
			// convert Person to Record
			Record theRecord = ConvertUtil.getRecordFromPerson(getEntity(), person);
			if (theRecord == null) {
				return;
			}
			recordManagerService.updateRecord(getEntity(), theRecord);	
			return;
		}
	    throw new RuntimeException("The entity to support the Person Data model has not been configured properly.");
	}

	public Person updatePersonById(Person person) throws ApplicationException {
		if (getEntity() != null && person != null) {
			// convert Person to Record
			Record theRecord = ConvertUtil.getRecordFromPerson(getEntity(), person);
			if (theRecord == null) {
				return null;
			}
			Record record = recordManagerService.updateRecord(getEntity(), theRecord);			

			Person personInstance = null;
			if( record != null) {
				personInstance = ConvertUtil.getPersonFromRecord(personDao, record);
			}
			return personInstance;
		}
		throw new RuntimeException("The entity to support the Person Data model has not been configured properly.");
	}

	public void deletePerson(PersonIdentifier personIdentifier) throws ApplicationException {
		if (getEntity() != null && personIdentifier != null) {
			
			Identifier recordIdentifier = ConvertUtil.buildIdentifier(personIdentifier);
			recordManagerService.deleteRecordByIdentifier(getEntity(), recordIdentifier);	
			return;
		}		
		throw new RuntimeException("The entity to support the Person Data model has not been configured properly.");
	}

	public void deletePersonById(Person person) throws ApplicationException {
		if (getEntity() != null && person != null) {
			// convert Person to Record
			Record theRecord = ConvertUtil.getRecordFromPerson(getEntity(), person);
			if (theRecord == null) {
				return;
			}
			recordManagerService.deleteRecord(getEntity(), theRecord);	
			return;
		}			
		throw new RuntimeException("The entity to support the Person Data model has not been configured properly.");
	}

	public void removePerson(Integer personId) throws ApplicationException {
		if (getEntity() != null && personId != null) {	
			Record record = new Record(getEntity());
			record.setRecordId(personId.longValue());
			recordManagerService.removeRecord(getEntity(), record);
			return;
		}
	    throw new RuntimeException("The entity to support the Person Data model has not been configured properly.");
	}

    private Person getOtherPerson(PersonLink originalLink, Person person) {
        if (originalLink.getPersonLeft().getPersonId().equals(person.getPersonId())) {
            return originalLink.getPersonRight();
        }
        return originalLink.getPersonLeft();
    }
	   
    public List<PersonLink> getPersonLinks(Person person) {
        ValidationService validationService = Context.getValidationService();
        validationService.validate(person);

        List<PersonLink> personLinks = new ArrayList<PersonLink>();              
        if( getEntity() != null && person != null ) {           
            // convert Person to Record
            Record theRecord = ConvertUtil.getRecordFromPersonForSearch(getEntity(), person);   
            if (theRecord == null) {
                return personLinks;
            }
            List<RecordLink> recordLinks = recordQueryService.loadRecordLinks(getEntity(), theRecord.getRecordId());
            if (recordLinks == null) {
                return personLinks;
            }
                
            for (RecordLink recordLink : recordLinks) {
                
                // loadRecordLink with left and right
                RecordLink link = recordQueryService.loadRecordLink(getEntity(), recordLink.getRecordLinkId());
                // convert RecordLink to PersonLink
                PersonLink personLink = ConvertUtil.getPersonLinkFromRecordLink(personDao, link);  
                
                if (personLink != null && (personLink.getPersonLeft().getPersonId().intValue() == person.getPersonId().intValue() ||
                                           personLink.getPersonRight().getPersonId().intValue() == person.getPersonId().intValue()) ){
                    personLinks.add(personLink);
                }
            }               
        }               
        return personLinks;        
    }

    public boolean isLinkedPersons(Person person, Person personOther) {
        ValidationService validationService = Context.getValidationService();
        validationService.validate(person);
        validationService.validate(personOther);
        
        if( getEntity() != null && person != null && personOther != null) {           
            // convert Person to Record
            Record theRecord = ConvertUtil.getRecordFromPersonForSearch(getEntity(), person);
            if (theRecord == null) {
                return false;
            }
            
            List<Record> records = recordQueryService.loadRecordLinksByRecordId(getEntity(), theRecord.getRecordId());
            for (Record record : records) {
                // convert Record to Person
                Person personlinked = ConvertUtil.getPersonFromRecord(personDao, record);
                if(personlinked.getPersonId().intValue() == personOther.getPersonId().intValue()) {
                    return true;
                }
            }               
        }               
        return false;        
    }
    
	public void mergePersons(PersonIdentifier retiredIdentifier, PersonIdentifier survivingIdentifier)
			throws ApplicationException {
        ValidationService validationService = Context.getValidationService();
        validationService.validate(retiredIdentifier);
        validationService.validate(survivingIdentifier);
        
        // Surviving
        Person personSurviving = null;
        Identifier recordIdentifier = ConvertUtil.buildIdentifier(survivingIdentifier);
        List<Record> recordFound = recordQueryService.findRecordsByIdentifier(getEntity(), recordIdentifier);  
        if (recordFound != null && recordFound.size() > 0) {            
            personSurviving = ConvertUtil.getPersonFromRecord(personDao, recordFound.get(0));
        }        
        if (personSurviving == null) {
            log.warn("While attempting to merge two persons was not able to locate a record with the given identifier: " + survivingIdentifier);
            throw new ApplicationException("Person record to be the survivor of a merge does not exist in the system.");
        }

        // Retiring
        Person personRetiring = null;
        recordIdentifier = ConvertUtil.buildIdentifier(retiredIdentifier);
        recordFound = recordQueryService.findRecordsByIdentifier(getEntity(), recordIdentifier);  
        if (recordFound != null && recordFound.size() > 0) {            
            personRetiring = ConvertUtil.getPersonFromRecord(personDao, recordFound.get(0));
        }        
        if (personRetiring == null) {
            log.warn("While attempting to merge two persons was not able to locate a record with the given identifier: " + retiredIdentifier);
            throw new ApplicationException("Person record to be deleted as part of a merge does not exist in the system.");
        }

        List<PersonLink> links = getPersonLinks(personRetiring);
        for (PersonLink originalLink : links) {
            
            Person otherPerson = getOtherPerson(originalLink, personRetiring);
            
            // has already been linked
            if (!isLinkedPersons(otherPerson, personSurviving )) { 
                
                // add an new link for personSurviving
                RecordLink newRecorkLink = ConvertUtil.createRecordLinkFromPersons(getEntity(), originalLink, personSurviving, otherPerson);
                if (newRecorkLink != null) {
                    recordManagerService.addRecordLink(newRecorkLink);
                }
            }
            
            //remove the originalLink
            RecordLink recorkLink = ConvertUtil.getRecordLinkFromPersonLink(getEntity(), originalLink);
            recordManagerService.removeRecordLink(recorkLink);
        }
        // Delete the retired person record
        deletePersonById(personRetiring);                
	}

	public PersonLink linkPersons(PersonLink personLink) throws ApplicationException {
	    RecordLink recordLink = null;
	    if (personLink.getPersonLinkId() == null) {
	        recordLink = ConvertUtil.getRecordLinkFromPersonLink(getEntity(), personLink);
	        recordManagerService.addRecordLink(recordLink);
	    } else {
            int personLinkId = personLink.getPersonLinkId();
            int left = personLinkId >> 16; 
            int right = personLinkId & 0xFF;      
            recordLink = new RecordLink("#"+Integer.toString(left)+":"+Integer.toString(right));
            recordLink = recordQueryService.loadRecordLink(getEntity(), recordLink.getRecordLinkId());
            recordLink.setState(RecordLinkState.MATCH);            
            recordLink = recordManagerService.updateRecordLink(recordLink);
	    }
        return ConvertUtil.getPersonLinkFromRecordLink(personDao, recordLink);       
	}

	public void unlinkPersons(PersonLink personLink) throws ApplicationException {
        int personLinkId = personLink.getPersonLinkId();
        int left = personLinkId >> 16; 
        int right = personLinkId & 0xFF;      
        RecordLink recordLink = new RecordLink("#"+Integer.toString(left)+":"+Integer.toString(right));
        recordLink = recordQueryService.loadRecordLink(getEntity(), recordLink.getRecordLinkId());
        recordLink.setState(RecordLinkState.NON_MATCH);      

        recordLink = recordManagerService.updateRecordLink(recordLink);    
	}

	public Person getPerson(List<PersonIdentifier> personIdentifiers) {
        ValidationService validationService = Context.getValidationService();
        for (PersonIdentifier identifier : personIdentifiers) {
            validationService.validate(identifier);
            
            Identifier recordIdentifier = ConvertUtil.buildIdentifier(identifier);
            List<Record> recordFound = recordQueryService.findRecordsByIdentifier(getEntity(), recordIdentifier);  
            if (recordFound != null && recordFound.size()>0) {
                
                Person personInstance = ConvertUtil.getPersonFromRecord(personDao, recordFound.get(0));
                return personInstance;
            }
        }
        return null;
	}
    
	public Person importPerson(Person person) throws ApplicationException {
        if (getEntity() != null && person != null) {            
            // convert Person to Record
            Record theRecord = ConvertUtil.getRecordFromPerson(getEntity(), person);
            if (theRecord == null) {
                return null;
            }
            
            Record record = recordManagerService.importRecord(getEntity(), theRecord);
            
            Person personInstance = null;
            if( record != null) {
                personInstance = ConvertUtil.getPersonFromRecord(personDao, record);
            }
            return personInstance;
        }
        throw new RuntimeException("The entity to support the Person Data model has not been configured properly.");
	}

	public IdentifierDomain addIdentifierDomain(IdentifierDomain identifierDomain) throws ApplicationException {
        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomain);
        
        if (identifierDomain == null || 
                (identifierDomain.getNamespaceIdentifier() == null &&
                (identifierDomain.getUniversalIdentifier() == null || identifierDomain.getUniversalIdentifierTypeCode() == null))) {
            log.warn("Attempted to add an identifier domain with insufficient attributes: " + identifierDomain);
            throw new ApplicationException("The identifier domain to be added is invalid.");
        }
        
        IdentifierDomain idFound = identifierDomainDao.findIdentifierDomain(identifierDomain);
        if (idFound != null) {
            log.warn("While attempting to add an identifier domain, found an existing record in the repository: " + idFound);
            throw new ApplicationException("Identifier domain record to be added already exists in the system.");
        }
        identifierDomainDao.saveIdentifierDomain(identifierDomain);
        return identifierDomain;
	}

	public IdentifierDomain updateIdentifierDomain(IdentifierDomain identifierDomain) throws ApplicationException {
        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomain);
        
        if (identifierDomain == null || identifierDomain.getIdentifierDomainId() == null) {
            log.warn("Attempted to update an identifier domain with insufficient attributes: " + identifierDomain);
            throw new ApplicationException("The identifier domain to be updated is invalid.");
        }
        
        if (identifierDomain.getNamespaceIdentifier() == null &&
            (identifierDomain.getUniversalIdentifier() == null || identifierDomain.getUniversalIdentifierTypeCode() == null)) {
            log.warn("Attempted to update an identifier domain with insufficient attributes: " + identifierDomain);
            throw new ApplicationException("The identifier domain to be update is invalid.");
        }
        
        IdentifierDomain idFound = identifierDomainDao.findIdentifierDomain(identifierDomain);
        if (idFound != null && idFound.getIdentifierDomainId() != identifierDomain.getIdentifierDomainId()) {
            log.warn("While attempting to update an identifier domain, found an existing record in the repository with same identifiying attributes: " + idFound);
            throw new ApplicationException("Identifier domain record cannot be updated to match another entry in the repository.");
        }
        idFound.setIdentifierDomainDescription(identifierDomain.getIdentifierDomainDescription());
        idFound.setIdentifierDomainName(identifierDomain.getIdentifierDomainName());
        idFound.setNamespaceIdentifier(identifierDomain.getNamespaceIdentifier());
        idFound.setUniversalIdentifier(identifierDomain.getUniversalIdentifier());
        idFound.setUniversalIdentifierTypeCode(identifierDomain.getUniversalIdentifierTypeCode());
        identifierDomainDao.saveIdentifierDomain(idFound);
        return idFound;
	}

	public void deleteIdentifierDomain(IdentifierDomain identifierDomain) throws ApplicationException {
        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomain);
        
        if (identifierDomain == null ||
                (identifierDomain.getIdentifierDomainId() == null &&
                 identifierDomain.getNamespaceIdentifier() == null &&
                (identifierDomain.getUniversalIdentifier() == null || identifierDomain.getUniversalIdentifierTypeCode() == null))) {
            log.warn("Attempted to delete an identifier domain with insufficient attributes: " + identifierDomain);
            throw new ApplicationException("The identifier domain to be deleted is invalid.");
        }

        IdentifierDomain idFound = identifierDomainDao.findIdentifierDomain(identifierDomain);
        if (idFound == null) {
            return;
        }
        try {
            identifierDomainDao.removeIdentifierDomain(idFound);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("Attempted to delete an identifier domain that is still referenced by identifiers: " + e, e);
            throw new ApplicationException("Cannot delete identifier domain as it is still in use.");
        }
	}

	public boolean assignGlobalIdentifier() {
		// TODO Auto-generated method stub
		return false;
	}

    private IdentifierDomain generateIdentifierDomainForUniversalIdentifierTypeCode(String universalIdentifierTypeCode) {
        UUID uuid = new UUID();
        IdentifierDomain id = new IdentifierDomain();
        java.util.Date now = new java.util.Date();
        id.setDateCreated(now);
        id.setUserCreatedBy(Context.getUserContext().getUser());
        id.setNamespaceIdentifier(uuid.toString());
        id.setUniversalIdentifier(uuid.toString());
        id.setUniversalIdentifierTypeCode(universalIdentifierTypeCode);
        return id;
    }
	   
	public IdentifierDomain obtainUniqueIdentifierDomain(String universalIdentifierTypeCode) {
        boolean isKnown = personDao.isKnownUniversalIdentifierTypeCode(universalIdentifierTypeCode);
        log.trace("The universlIdentifierTypeCode " + universalIdentifierTypeCode + " is known to the system exptresson is " + isKnown);
        if (!isKnown) {
            throw new ValidationException("The universalIdentifierTypeCode " + universalIdentifierTypeCode + " is not known to the system.");
        }
        IdentifierDomain identifierDomain = generateIdentifierDomainForUniversalIdentifierTypeCode(universalIdentifierTypeCode);
        identifierDomainDao.addIdentifierDomain(identifierDomain);
        log.trace("Created new identifier domain " + identifierDomain);
        Context.getAuditEventService().saveAuditEvent(AuditEventType.OBTAIN_UNIQUE_IDENTIFIER_DOMAIN_EVENT_TYPE, "Obtained unique identifier domain type for type code " + universalIdentifierTypeCode);
        return identifierDomain;
	}

	public IdentifierDomainAttribute addIdentifierDomainAttribute(IdentifierDomain identifierDomain,
			String attributeName, String attributeValue) {
        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomain);
        
        IdentifierDomainAttribute attribute = identifierDomainDao.addIdentifierDomainAttribute(identifierDomain, attributeName, attributeValue);
        
        Context.getAuditEventService().saveAuditEventEntry(AuditEventType.ADD_IDENTIFIER_DOMAIN_ATTRIBUTE_EVENT_TYPE, "Added attribute " + attributeName + 
                " to identifier domain with ID " + identifierDomain.getIdentifierDomainId(), getEntity().getName());
        
        return attribute;
	}

	public void updateIdentifierDomainAttribute(IdentifierDomainAttribute identifierDomainAttribute) {
        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomainAttribute);
        
        identifierDomainDao.updateIdentifierDomainAttribute(identifierDomainAttribute);
        
        Context.getAuditEventService().saveAuditEventEntry(AuditEventType.UPDATE_IDENTIFIER_DOMAIN_ATTRIBUTE_EVENT_TYPE, 
                "Updated attribute " + identifierDomainAttribute.getAttributeName() + 
                " of identifier domain with ID " + identifierDomainAttribute.getIdentifierDomainId(),
                getEntity().getName());
	}

	public void removeIdentifierDomainAttribute(IdentifierDomainAttribute identifierDomainAttribute) {
        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomainAttribute);
        
        identifierDomainDao.removeIdentifierDomainAttribute(identifierDomainAttribute);
        Context.getAuditEventService().saveAuditEventEntry(AuditEventType.DELETE_IDENTIFIER_DOMAIN_ATTRIBUTE_EVENT_TYPE,
                "Deleted attribute " + identifierDomainAttribute.getAttributeName() + 
                " of identifier domain with ID " + identifierDomainAttribute.getIdentifierDomainId(),
                getEntity().getName());
	}

	public void linkAllRecordPairs() throws ApplicationException {
        recordManagerService.linkAllRecordPairs(getEntity());     
	}

	public void initializeRepository(Entity entity) throws ApplicationException {
        recordManagerService.initializeRepository(entity);
	}

	public void generateCustomFields() throws ApplicationException {
		// TODO Auto-generated method stub
		
	}

	public void clearCustomFields() {
		// TODO Auto-generated method stub
		
	}

	public void clearAllLinks() {
	    recordManagerService.clearAllLinks(getEntity());      
		
	}

	public void generateCustomFields(List<Person> records) {
		// TODO Auto-generated method stub
		
	}

	public void addReviewRecordPair(ReviewRecordPair recordPairs) throws ApplicationException {
	    RecordLink link = ConvertUtil.getRecordLinkFromReviewRecordPair(getEntity(), recordPairs);
	    if (link != null) {
    	    link.setState(RecordLinkState.POSSIBLE_MATCH);
    	    recordManagerService.addRecordLink(link);
	    }
	}

	public void addReviewRecordPairs(List<ReviewRecordPair> recordPairs) throws ApplicationException {
        for (ReviewRecordPair reviewRecordPair : recordPairs) {
            RecordLink recordLink = ConvertUtil.getRecordLinkFromReviewRecordPair(getEntity(), reviewRecordPair);
            if (recordLink != null) {
                recordLink.setState(RecordLinkState.POSSIBLE_MATCH);
                recordManagerService.addRecordLink(recordLink);
            }
        }		
	}

	public void matchReviewRecordPair(ReviewRecordPair recordPair) throws ApplicationException {
		// TODO Auto-generated method stub
		
	}

	public void deleteReviewRecordPair(ReviewRecordPair recordPair) {
        RecordLink recordLink = ConvertUtil.getRecordLinkFromReviewRecordPair(getEntity(), recordPair);
        if (recordLink != null) {
            try {
                recordManagerService.removeRecordLink(recordLink);
            } catch (ApplicationException e) {
                log.warn("Attempted to delete a record link: " + e, e);
            }
 
        }
	}

	public void deleteReviewRecordPairs() {
        List<RecordLink> recordLinks = recordQueryService.loadRecordLinks(getEntity(), RecordLinkState.POSSIBLE_MATCH, 0, 0); 
        for (RecordLink recordLink : recordLinks) {
            try {
                recordManagerService.removeRecordLink(recordLink);
            } catch (ApplicationException e) {
                log.warn("Attempted to delete a record link: " + e, e);
            }
        }
	}
	
	public String getPersonEntityName() {
		return personEntityName;
	}

	public void setPersonEntityName(String personEntityName) {
		this.personEntityName = personEntityName;
	}

	public PersonDao getPersonDao() {
		return personDao;
	}

	public void setPersonDao(PersonDao personDao) {
		this.personDao = personDao;
	}

    public IdentifierDomainDao getIdentifierDomainDao() {
        return identifierDomainDao;
    }

    public void setIdentifierDomainDao(IdentifierDomainDao identifierDomainDao) {
        this.identifierDomainDao = identifierDomainDao;
    }
    
	public EntityDefinitionManagerService getEntityDefinitionService() {
		return entityDefinitionService;
	}
	public void setEntityDefinitionService(EntityDefinitionManagerService entityDefinitionService) {
		this.entityDefinitionService = entityDefinitionService;
	}
    
    public RecordQueryService getRecordQueryService() {
        return recordQueryService;
    }
    
    public void setRecordQueryService(RecordQueryService entityQueryService) {
        this.recordQueryService = entityQueryService;
    }
    
	public RecordManagerService getRecordManagerService() {
		return recordManagerService;
	}
	
	public void setRecordManagerService(RecordManagerService entityManagerService) {
		this.recordManagerService = entityManagerService;
	}
}