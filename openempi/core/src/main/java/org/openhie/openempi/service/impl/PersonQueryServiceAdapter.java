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

import org.openhie.openempi.configuration.GlobalIdentifier;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.dao.IdentifierDomainDao;
import org.openhie.openempi.dao.PersonDao;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Gender;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.IdentifierDomainAttribute;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.PersonLink;
import org.openhie.openempi.model.Race;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.model.ReviewRecordPair;
import org.openhie.openempi.service.PersonQueryService;
import org.openhie.openempi.service.ValidationService;
import org.openhie.openempi.util.ConvertUtil;

public class PersonQueryServiceAdapter extends BaseServiceImpl implements PersonQueryService
{
	private String personEntityName;
	private Entity entityDef = null;
	
	private EntityDefinitionManagerService entityDefinitionService;
	private RecordQueryService recordQueryService;
	protected PersonDao personDao;
    private IdentifierDomainDao identifierDomainDao;
	
	private Entity getEntity() {
		if (entityDef == null) {
			List<Entity> entityDefs = entityDefinitionService.findEntitiesByName(personEntityName);
			if (entityDefs != null && entityDefs.size()>0) {
				entityDef = entityDefs.get(0);
			}
		}
		return entityDef;
	}
	
	public List<IdentifierDomain> getIdentifierDomains() {
		return identifierDomainDao.getIdentifierDomains();
	}

	public List<String> getIdentifierDomainTypeCodes() {
		return identifierDomainDao.getIdentifierDomainTypeCodes();
	}

	public IdentifierDomainAttribute getIdentifierDomainAttribute(IdentifierDomain identifierDomain,
			String attributeName) {
		ValidationService validationService = Context.getValidationService();
		validationService.validate(identifierDomain);
		
		return identifierDomainDao.getIdentifierDomainAttribute(identifierDomain, attributeName);
	}

	public List<IdentifierDomainAttribute> getIdentifierDomainAttributes(IdentifierDomain identifierDomain) {
		ValidationService validationService = Context.getValidationService();
		validationService.validate(identifierDomain);
		
		return identifierDomainDao.getIdentifierDomainAttributes(identifierDomain);
	}

	public Person findPersonById(PersonIdentifier identifier) {
		
		ValidationService validationService = Context.getValidationService();
		validationService.validate(identifier);
		
		if (getEntity() != null && identifier != null) {			
			
			Identifier recordIdentifier = ConvertUtil.buildIdentifier(identifier);
			List<Record> records = recordQueryService.findRecordsByIdentifier(getEntity(), recordIdentifier);	
			List<Person> persons = new java.util.ArrayList<Person>();
			for (Record record : records) {
				// convert Record to Person
				Person personInstance = ConvertUtil.getPersonFromRecord(personDao, record);
				persons.add(personInstance);
			}
			
			if (persons != null && persons.size() > 0) {
				return persons.get(0);
			}
		}		
		return null;
	}

	public List<Person> findPersonsById(PersonIdentifier identifier) {
		
		ValidationService validationService = Context.getValidationService();
		validationService.validate(identifier);
		
		if( getEntity() != null && identifier != null ) {			
			
			Identifier recordIdentifier = ConvertUtil.buildIdentifier(identifier);
			
			List<Record> records = recordQueryService.findRecordsByIdentifier(getEntity(), recordIdentifier);	
			
			List<Person> persons = new java.util.ArrayList<Person>();
			for (Record record : records) {
				// convert Record to Person
				Person personInstance = ConvertUtil.getPersonFromRecord(personDao, record);
				persons.add(personInstance);
			}
			return persons;
		}		
		return null;	
	}

	public PersonIdentifier getGlobalIdentifierById(PersonIdentifier personIdentifier) {
		ValidationService validationService = Context.getValidationService();
		validationService.validate(personIdentifier);

		GlobalIdentifier globalIdentifier = Context.getConfiguration().getGlobalIdentifier();
		if (globalIdentifier == null || !globalIdentifier.isAssignGlobalIdentifier()) {
			log.warn("The system is not configured to assign global identifiers.");
			return null;
		}
		
		IdentifierDomain globalDomain = globalIdentifier.getIdentifierDomain();
		// Check to see if the person already has a global identifier
		Person person = findPersonById(personIdentifier);
		
		if (person != null) {
			for (PersonIdentifier identifier : person.getPersonIdentifiers()) {
				IdentifierDomain identifierDomain = identifier.getIdentifierDomain();
				if (identifierDomain != null && identifierDomain.equals(globalDomain)) {
					if (log.isTraceEnabled()) {
						log.trace("Person has a global identifier assigned: " + identifier);
					}
					return identifier;
				}
			}
		}
		return null;
	}

	public Person loadPerson(Integer personId) {
		if (getEntity() != null && personId != null) {
			Long recordId = personId.longValue();
			Record record = recordQueryService.loadRecordById(getEntity(), recordId);
			if (record == null) {
				return null;
			}
			Person person = ConvertUtil.getPersonFromRecord(personDao, record);
			return person;
		}		
		return null;	
	}

	public List<Person> loadPersons(List<Integer> personIds) {
		List<Person> persons = new ArrayList<Person>();
		for (Integer personId : personIds) {
			Person person = loadPerson(personId);
			if (person != null) {
				persons.add(person);
			}
		}
		return persons;
	}

	public List<Person> loadAllPersonsPaged(int firstRecord, int maxRecords) {		

		if( getEntity() != null) {			

			// convert Person to Record
			Record theRecord = ConvertUtil.getRecordFromPersonForSearch(getEntity(), new Person());
			if (theRecord == null) {
				return null;
			}
			
			List<Record> records = recordQueryService.loadAllRecordsPaged(getEntity(), theRecord, firstRecord, maxRecords);			
			List<Person> persons = new java.util.ArrayList<Person>();
			for (Record record : records) {
				// convert Record to Person
				Person personInstance = ConvertUtil.getPersonFromRecord(personDao, record);
				persons.add(personInstance);
			}				
			return persons;
		}		
		return null;
	}

	public List<ReviewRecordPair> loadAllUnreviewedPersonLinks() {
		if( getEntity() != null) {			
			
			List<RecordLink> recordLinks = recordQueryService.loadRecordLinks(getEntity(), RecordLinkState.POSSIBLE_MATCH, 0, 0);	
			
			RecordLink record;
			List<ReviewRecordPair> reviewRecordPairs = new java.util.ArrayList<ReviewRecordPair>();
			for (RecordLink recordLink : recordLinks) {				
				record = recordQueryService.loadRecordLink(getEntity(), recordLink.getRecordLinkId());
				
				// convert RecordLink to ReviewRecordPair
				ReviewRecordPair reviewRecordPair = ConvertUtil.getReviewRecordPairFromRecordLink(personDao, record);
				reviewRecordPairs.add(reviewRecordPair);
			}				
			return reviewRecordPairs;
		}		
		return null;
	}

	public List<ReviewRecordPair> loadUnreviewedPersonLinks(int maxResults) {
		if( getEntity() != null) {			
			
			List<RecordLink> recordLinks = recordQueryService.loadRecordLinks(getEntity(), RecordLinkState.POSSIBLE_MATCH, 0, maxResults);
			
			RecordLink record;
			List<ReviewRecordPair> reviewRecordPairs = new java.util.ArrayList<ReviewRecordPair>();
			for (RecordLink recordLink : recordLinks) {				
					
				record = recordQueryService.loadRecordLink(getEntity(), recordLink.getRecordLinkId());
				
				// convert RecordLink to ReviewRecordPair
				ReviewRecordPair reviewRecordPair = ConvertUtil.getReviewRecordPairFromRecordLink(personDao, record);
				reviewRecordPairs.add(reviewRecordPair);
			}				
			return reviewRecordPairs;
		}		
		return null;
	}

	public ReviewRecordPair loadReviewRecordPair(int reviewRecordPairId) {
		if (getEntity() != null) {
		    int left = reviewRecordPairId >> 16; 
		    int right = reviewRecordPairId & 0xFF;  

			RecordLink recordLink = new RecordLink("#"+Integer.toString(left)+":"+Integer.toString(right));
			recordLink = recordQueryService.loadRecordLink(getEntity(), recordLink.getRecordLinkId());
					
			// convert RecordLink to ReviewRecordPair
			ReviewRecordPair reviewRecordPair = ConvertUtil.getReviewRecordPairFromRecordLink(personDao, recordLink);

			return reviewRecordPair;
		}		
		return null;
	}

	public List<Person> findPersonsByAttributes(Person person) {

		ValidationService validationService = Context.getValidationService();
		validationService.validate(person);
		
		if( getEntity() != null && person != null ) {			
			// convert Person to Record
			Record theRecord = ConvertUtil.getRecordFromPersonForSearch(getEntity(), person);
			if (theRecord == null) {
				return null;
			}
			
			List<Record> records = recordQueryService.findRecordsByAttributes(getEntity(), theRecord);			
			List<Person> persons = new java.util.ArrayList<Person>();
			for (Record record : records) {
				// convert Record to Person
				Person personInstance = ConvertUtil.getPersonFromRecord(personDao, record);
				persons.add(personInstance);
			}				
			return persons;
		}		
		return null;
	}

	public IdentifierDomain findIdentifierDomain(IdentifierDomain identifierDomain) {
		ValidationService validationService = Context.getValidationService();
		validationService.validate(identifierDomain);
		if (identifierDomain == null ||
				(identifierDomain.getIdentifierDomainId() == null &&
				 identifierDomain.getNamespaceIdentifier() == null &&
				(identifierDomain.getUniversalIdentifier() == null || identifierDomain.getUniversalIdentifierTypeCode() == null))) {
			return null;
		}
		IdentifierDomain idFound = identifierDomainDao.findIdentifierDomain(identifierDomain);
		return idFound;		
	}

    public IdentifierDomain findIdentifierDomainByName(String identifierDomainName) {
        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifierDomainName);
        
        if (identifierDomainName == null ||
                identifierDomainName.length() == 0) {
            return null;
        }
        return identifierDomainDao.findIdentifierDomainByName(identifierDomainName);
    }
    
    public List<Person> findLinkedPersons(PersonIdentifier identifier) {
        ValidationService validationService = Context.getValidationService();
        validationService.validate(identifier);

        List<Person> linkedPersons = new ArrayList<Person>();  
        if (getEntity() != null && identifier != null) {            
            
            Identifier recordIdentifier = ConvertUtil.buildIdentifier(identifier);
            List<Record> records = recordQueryService.findRecordsByIdentifier(getEntity(), recordIdentifier);  
            if (records == null || records.size() == 0) {
                return linkedPersons;
            }
            
            List<Record> linkedRecords = recordQueryService.loadRecordLinksByRecordId(getEntity(),
                        records.get(0).getRecordId());
            for (Record record : linkedRecords) {
                // convert Record to Person
                Person personInstance = ConvertUtil.getPersonFromRecord(personDao, record);
                linkedPersons.add(personInstance);
            }               
        }       
        return linkedPersons;
    }

    public List<Person> findLinkedPersons(Person person) {
        ValidationService validationService = Context.getValidationService();
        validationService.validate(person);
        
        List<Person> linkedPersons = new ArrayList<Person>();              
        if( getEntity() != null && person != null ) {           
            // convert Person to Record
            Record theRecord = ConvertUtil.getRecordFromPersonForSearch(getEntity(), person);
            if (theRecord == null) {
                return linkedPersons;
            }
            
            List<Record> records = recordQueryService.loadRecordLinksByRecordId(getEntity(), theRecord.getRecordId());
            for (Record record : records) {
                // convert Record to Person
                Person personInstance = ConvertUtil.getPersonFromRecord(personDao, record);
                linkedPersons.add(personInstance);
            }               
        }               
        return linkedPersons;        
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
	              if (personLink != null) {
	                  personLinks.add(personLink);
	              }
	          }               
	      }               
	      return personLinks;        
	}

	public List<PersonLink> getPersonLinksByLinkSource(Integer linkSourceId) {  
        if (linkSourceId == null) {
            return new java.util.ArrayList<PersonLink>();
        }	    	    
        List<PersonLink> personLinks = new ArrayList<PersonLink>();              
        if (getEntity() != null) {        
            List<RecordLink> recordLinks = recordQueryService.findRecordLinksBySource(getEntity(), new LinkSource(linkSourceId), RecordLinkState.MATCH);
            for (RecordLink recordLink : recordLinks) {
                // loadRecordLink with left and right
                RecordLink link = recordQueryService.loadRecordLink(getEntity(), recordLink.getRecordLinkId());
                
                // convert RecordLink to PersonLink
                PersonLink personLink = ConvertUtil.getPersonLinkFromRecordLink(personDao, link);  
                if (personLink != null) {
                    personLinks.add(personLink);
                }
            }                                       
        }
        return personLinks;
	}
	   
	public List<Person> findMatchingPersonsByAttributes(Person person) {
        ValidationService validationService = Context.getValidationService();
        validationService.validate(person);
        
        if( getEntity() != null && person != null ) {           
            // convert Person to Record
            Record theRecord = ConvertUtil.getRecordFromPersonForSearch(getEntity(), person);
            if (theRecord == null) {
                return null;
            }
            
            //TODO: This needs to be fixed.
            List<Record> records = recordQueryService.findRecordsByAttributes(getEntity(), theRecord);          
            List<Person> persons = new java.util.ArrayList<Person>();
            for (Record record : records) {
                // convert Record to Person
                Person personInstance = ConvertUtil.getPersonFromRecord(personDao, record);
                persons.add(personInstance);
            }               
            return persons;
        }       
        return null;
    }
	

    public List<Person> getSingleBestRecords(List<Integer> personIds) {
        // TODO Auto-generated method stub
        return null;
    }

    public Person getSingleBestRecord(Integer personId) {
        // TODO Auto-generated method stub
        return null;
    }
    
	public List<Person> findPersonsByAttributesPaged(Person person, int firstResult, int maxResults) {
		ValidationService validationService = Context.getValidationService();
		validationService.validate(person);
		
		if( getEntity() != null && person != null ) {			
			// convert Person to Record
			Record theRecord = ConvertUtil.getRecordFromPersonForSearch(getEntity(), person);
			if (theRecord == null) {
				return null;
			}
			
			List<Record> records = recordQueryService.findRecordsByAttributes(getEntity(), theRecord, firstResult, maxResults);			
			List<Person> persons = new java.util.ArrayList<Person>();
			for (Record record : records) {
				// convert Record to Person
				Person personInstance = ConvertUtil.getPersonFromRecord(personDao, record);
				persons.add(personInstance);
			}				
			return persons;
		}		
		return null;
	}
    
	public List<String> getPersonModelAllAttributeNames() {
		if( getEntity() != null) {		
			Record record = new Record( getEntity() );	
			return ConvertUtil.getAllModelAttributeNames(record);
		}
		return new ArrayList<String>();
	}

	public List<String> getPersonModelAttributeNames() {
		if( getEntity() != null) {	
			Record record = new Record(getEntity());
			return ConvertUtil.getModelAttributeNames(record, false);
		}
		return new ArrayList<String>();
	}

	public List<String> getPersonModelCustomAttributeNames() {
		// Custom field from person
		Record record = new Record(new Person());
		return ConvertUtil.getModelAttributeNames(record, true);
	}

	public Race findRaceByCode(String raceCode) {
		return personDao.findRaceByCode(raceCode);
	}

	public Race findRaceByName(String raceName) {
		return personDao.findRaceByName(raceName);
	}

	public Gender findGenderByCode(String genderCode) {
		return personDao.findGenderByCode(genderCode);
	}

	public Gender findGenderByName(String genderName) {
		return personDao.findGenderByName(genderName);
	}

    public IdentifierDomainDao getIdentifierDomainDao() {
        return identifierDomainDao;
    }

    public void setIdentifierDomainDao(IdentifierDomainDao identifierDomainDao) {
        this.identifierDomainDao = identifierDomainDao;
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
}
