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
package org.openhie.openempi.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.WrapDynaClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhie.openempi.Constants;
import org.openhie.openempi.configuration.xml.model.AttributeType;
import org.openhie.openempi.configuration.xml.model.AttributesType;
import org.openhie.openempi.configuration.xml.model.EntityModel;
import org.openhie.openempi.configuration.xml.model.GroupType;
import org.openhie.openempi.configuration.xml.model.GroupsType;
import org.openhie.openempi.configuration.xml.model.ValidationParameterType;
import org.openhie.openempi.configuration.xml.model.ValidationParametersType;
import org.openhie.openempi.configuration.xml.model.ValidationType;
import org.openhie.openempi.configuration.xml.model.ValidationsType;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.dao.PersonDao;
import org.openhie.openempi.model.AttributeDatatype;
import org.openhie.openempi.model.Criteria;
import org.openhie.openempi.model.Criterion;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.EntityAttributeGroup;
import org.openhie.openempi.model.EntityAttributeGroupAttribute;
import org.openhie.openempi.model.EntityAttributeValidation;
import org.openhie.openempi.model.EntityAttributeValidationParameter;
import org.openhie.openempi.model.ExtendedCriterion;
import org.openhie.openempi.model.Gender;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.LabelValue;
import org.openhie.openempi.model.Language;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Nationality;
import org.openhie.openempi.model.Operation;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.PersonLink;
import org.openhie.openempi.model.Race;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.model.RecordPair;
import org.openhie.openempi.model.Religion;
import org.openhie.openempi.model.ReviewRecordPair;
import org.openhie.openempi.model.User;


/**
 * Utility class to convert one object to another.
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 * Extended by <a href="mailto:odysseas@sysnetint.com">Odysseas Pentakalos</a>
 * to support conversion between POJO trees and Hashmap of properties.
 */
public final class ConvertUtil
{
    private static final Log log = LogFactory.getLog(ConvertUtil.class);
    public static Map<String, Method> methodByFieldName = new HashMap<String, Method>();
    public static DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Checkstyle rule: utility classes should not have public constructor
     */
    private ConvertUtil() {
    }

    /**
     * Method to convert a ResourceBundle to a Map object.
     * @param rb a given resource bundle
     * @return Map a populated map
     */
    public static Map<String, String> convertBundleToMap(ResourceBundle rb) {
        Map<String, String> map = new HashMap<String, String>();

        Enumeration<String> keys = rb.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            map.put(key, rb.getString(key));
        }

        return map;
    }

	public static Record getRecordFromPerson(Person person) {
		Record record = new Record(person);
		if (person.getPersonId() != null) {
		   record.setRecordId(new Long(person.getPersonId()));
		}
		return record;
	}

	/**
     * Convert a java.util.List of LabelValue objects to a LinkedHashMap.
     * @param list the list to convert
     * @return the populated map with the label as the key
     */
    public static Map<String, String> convertListToMap(List<LabelValue> list) {
        Map<String, String> map = new LinkedHashMap<String, String>();

        for (LabelValue option : list) {
            map.put(option.getLabel(), option.getValue());
        }

        return map;
    }

    /**
     * Method to convert a ResourceBundle to a Properties object.
     * @param rb a given resource bundle
     * @return Properties a populated properties object
     */
    public static Properties convertBundleToProperties(ResourceBundle rb) {
        Properties props = new Properties();

        for (Enumeration<String> keys = rb.getKeys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            props.put(key, rb.getString(key));
        }

        return props;
    }

    /**
     * Convenience method used by tests to populate an object from a
     * ResourceBundle
     * @param obj an initialized object
     * @param rb a resource bundle
     * @return a populated object
     */
    public static Object populateObject(Object obj, ResourceBundle rb) {
        try {
            Map<String, String> map = convertBundleToMap(rb);
            BeanUtils.copyProperties(obj, map);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception occurred populating object: " + e.getMessage());
        }

        return obj;
    }

    public static Object cloneBean(Object obj) {
    		Object clone = null;
			try {
				clone = BeanUtils.cloneBean(obj);
			} catch (Exception e) {
				log.warn("Unable to clone object: " + obj + ". Error: " + e, e);
			}
    		return clone;
    }

    public static List<String> extractProperties(Object obj) {
    	List<String> properties = new ArrayList<String>();
    	Map<String, String> visitMap = new HashMap<String, String>();
    	visitMap.put(obj.getClass().getName(), obj.getClass().getName());
    	WrapDynaClass topClass = WrapDynaClass.createDynaClass(obj.getClass());
    	extractClassProperties(topClass, visitMap, properties, "");
    	return properties;
    }

    private static void extractClassProperties(WrapDynaClass theClass, Map<String,String> visitMap, List<String> properties, String parent) {
    	if (theClass == null) {
    		return;
    	}
    	for (DynaProperty property : theClass.getDynaProperties()) {
    		boolean visitedAlready = (visitMap.get(property.getType().getName()) != null);
			log.debug("Checking to see if type " + property.getType().getName() + " has been visited already returns " + visitedAlready);
    		if (!property.getType().getName().startsWith("java") && !visitedAlready) {
    			WrapDynaClass dynaClass = WrapDynaClass.createDynaClass(property.getType());
    			extractClassProperties(dynaClass, visitMap, properties, parent + property.getName() + ".");
    		} else {
    			if (!property.getType().getName().equalsIgnoreCase("java.lang.Class")) {
    				log.debug("Adding type " + property.getType().getName() + " to the list of types visited already.");
    				visitMap.put(property.getType().getName(), property.getType().getName());
    				properties.add(parent + property.getName());
    			}
    		}
    	}
    }

	public static String getModifiedFieldName(String fieldName, String prefix) {
		StringBuilder modifiedName = new StringBuilder(fieldName);
		modifiedName.setCharAt(0, Character.toUpperCase(modifiedName.charAt(0)));
		modifiedName.insert(0, prefix);
		return modifiedName.toString();
	}

	public static String getSetMethodName(String fieldName) {
		return getModifiedFieldName(fieldName, "set");
	}

	public static String getSerializedFieldName(String fieldName) {
		return getModifiedFieldName(fieldName, "serialized");
	}

	public static String byteArrayToHexString(byte[] b) {
		StringBuffer sb = new StringBuffer(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}

	public static void serializeObject(String configDirectory, String fileName, Object o) {
		String fullFilename = configDirectory + "/" + fileName;
		log.debug("Attempting to serialize object into file: " + fullFilename);
		try {
			ObjectOutputStream ois = new ObjectOutputStream(
					new BufferedOutputStream(new FileOutputStream(fullFilename)));
			ois.writeObject(o);
			ois.flush();
			ois.close();
		} catch (Exception e) {
			log.error("Failed while serializing object (into the file" + fullFilename + " ): " + e.getMessage(), e);
			throw new RuntimeException("Failed while serializing object (into the file" + fullFilename + " ): " + e.getMessage());
		}
	}

	public static Object deserializeObject(String configDirectory, String fileName) {
		Object obj;
		String fullFilename = configDirectory + "/" + fileName;
		log.debug("Attempting to deserialize object from file: " + fullFilename);
		try {
			ObjectInputStream ois = new ObjectInputStream(
					new BufferedInputStream(new FileInputStream(fullFilename)));
			obj = ois.readObject();
			ois.close();
		} catch (Exception e) {
			log.error("Failed while deserializing object (from file " + fullFilename + "): " + e.getMessage(), e);
			throw new RuntimeException("Failed while deserializing object (from file " + fullFilename + "): " + e.getMessage());
		}
		return obj;
	}

	public static boolean isValidCustomFieldName(String fieldName) {
		if (!fieldName.startsWith("custom") && !fieldName.startsWith("bcustom")) {
			return false;
		} else {
			String fieldNumberStr = null;
			if (fieldName.startsWith("custom")) {
				fieldNumberStr = fieldName.substring(6);
			} else if (fieldName.startsWith("bcustom")) {
				fieldNumberStr = fieldName.substring(7);
			}
			Integer fieldNumber = Integer.valueOf(fieldNumberStr);	// That allows signs and other stuff also
			if (fieldNumber <= 0 || fieldNumber > Constants.CUSTOM_FIELD_MAX_NUMBER) {
				return false;
			}
		}
		return true;
	}

	public static boolean isNullOrEmpty(String value) {
		if (value == null || value.length() == 0) {
			return true;
		}
		return false;
	}

	public static List<String> getAllModelAttributeNames(Record record) {
		java.util.Set<String> propertySet = record.getPropertyNames();
		java.util.List<String> sortedList = new java.util.ArrayList<String>(propertySet.size());
		for (String property : propertySet) {
			sortedList.add(property);
		}
		Collections.sort(sortedList);
		return sortedList;
	}

	public static List<String> getModelAttributeNames(Record record, boolean needCustomFields) {
		java.util.Set<String> propertySet = record.getPropertyNames();
		java.util.List<String> sortedList = new java.util.ArrayList<String>(propertySet.size());
		for (String property : propertySet) {
			if (!(needCustomFields ^ isValidCustomFieldName(property))) {
				sortedList.add(property);
			}
		}
		Collections.sort(sortedList);
		return sortedList;
	}

	public static Criteria buildCriteriaFromProperties(Record record) {
		Set<String> propertyNames = record.getPropertyNames();
		Criteria criteria = new Criteria();
		for (String property : propertyNames) {
			Object value = record.get(property);
			if (value != null &&
					(value instanceof java.lang.String || value instanceof java.util.Date)) {
				Criterion criterion = new Criterion();
				criterion.setName(property);
				criterion.setOperation(Operation.LIKE);
				criterion.setValue(value);
				criteria.addCriterion(criterion);
			}
		}
		return criteria;
	}

	public static void addIndirectCriteria(Person person, Criteria criteria) {
		if (person.getGender() != null && person.getGender().getGenderCode() != null) {
			ExtendedCriterion criterion = new ExtendedCriterion();
			criterion.setAlias("gender");
			criterion.setAssociationPath("gender");
			criterion.setName("gender.genderCode");
			criterion.setOperation(Operation.EQ);
			criterion.setValue(person.getGender().getGenderCode());
			criteria.addCriterion(criterion);
		}
	}

	public static List<RecordPair> generateRecordPairs(Record record, Collection<Record> records) {
		List<RecordPair> pairs = new java.util.ArrayList<RecordPair>(records.size());
		for (Record entry : records) {
		    log.debug("record=" + record + ", entry=" + entry);
			if (record.getRecordId() != null && record.getRecordId().longValue() == entry.getRecordId().longValue()) {
				// Skip the record itself if it happens to be one of the entries in the repository
				continue;
			}
			pairs.add(new RecordPair(record, entry));
		}
		return pairs;
	}

	// For PersonQueryServiceAdapter
	public static Record getRecordFromPerson(Entity entityDef, Person person) {
		Record theRecord = new Record(entityDef);
		if (person.getPersonId() != null) {
			theRecord.setRecordId(person.getPersonId().longValue());
		}

		for (EntityAttribute attrib : entityDef.getAttributes()) {
			String key = attrib.getName();
			String methodName = "get" + Character.toUpperCase(key.charAt(0)) + key.substring(1);

			Object obj;
			try {
				Method method = person.getClass().getDeclaredMethod(methodName);
				if (method == null) {
				   continue;
				}

				obj = method.invoke(person);
			} catch (Exception e) {
				return theRecord;
			}

			if (obj == null) {
				continue;
			}

			if (obj instanceof Gender) {
				theRecord.set(key, ((Gender) obj).getGenderCode());
			} else if (obj instanceof Race) {
				theRecord.set(key, ((Race) obj).getRaceCode());
            } else if (obj instanceof Nationality) {
                theRecord.set(key, ((Nationality) obj).getNationalityCode());
            } else if (obj instanceof Language) {
                theRecord.set(key, ((Language) obj).getLanguageCode());
            } else if (obj instanceof Religion) {
                theRecord.set(key, ((Religion) obj).getReligionCode());
            } else {
				theRecord.set(key, obj);
			}
		}

		// Identifiers
		if (person.getPersonIdentifiers() != null) {
			for (PersonIdentifier personIdentifier : person.getPersonIdentifiers()) {
				Identifier identifier = buildIdentifier(personIdentifier);
				identifier.setRecord(theRecord);
				theRecord.addIdentifier(identifier);
			}
		}
		return theRecord;
	}

	public static Record getRecordFromPersonForSearch(Entity entityDef, Person person) {
		Record theRecord = new Record(entityDef);
		if (person.getPersonId() != null) {
			theRecord.setRecordId(person.getPersonId().longValue());
		}

		for (EntityAttribute attrib : entityDef.getAttributes()) {
			String key = attrib.getName();
			String methodName = "get" + Character.toUpperCase(key.charAt(0)) + key.substring(1);

			Object obj;
			try {
				Method method = person.getClass().getDeclaredMethod(methodName);
				if (method == null) {
				   continue;
				}

				obj = method.invoke(person);
			} catch (Exception e) {
				return null;
			}

			if (obj == null) {
				continue;
			}

			if (obj instanceof Date) {
				switch(AttributeDatatype.getById(attrib.getDatatype().getDatatypeCd())) {
				case DATE:
					theRecord.set(key, dateToString((Date) obj));
					break;
				case TIMESTAMP:
					theRecord.set(key, dateTimeToString((Date) obj));
					break;
				default:
					break;
				}
			} else if (obj instanceof Gender) {
				theRecord.set(key, ((Gender) obj).getGenderCode());
			} else if (obj instanceof Race) {
				theRecord.set(key, ((Race) obj).getRaceCode());
	        } else if (obj instanceof Nationality) {
	            theRecord.set(key, ((Nationality) obj).getNationalityCode());
	        } else if (obj instanceof Language) {
	            theRecord.set(key, ((Language) obj).getLanguageCode());
	        } else if (obj instanceof Religion) {
	            theRecord.set(key, ((Religion) obj).getReligionCode());
	        } else {
				theRecord.set(key, obj);
			}
		}
		return theRecord;
	}

	public static Person getPersonFromRecord(PersonDao personDao, Record record) {
		Person person = new Person();
		if (record.getRecordId() != null) {
			person.setPersonId(new Integer(record.getRecordId().intValue()));
		}

		// Attributes
		Set<String> propertyNames = record.getPropertyNames();
		for (String property : propertyNames) {
			if (property.equals("gender")) {
				Object code = record.get(property);
				if (code != null) {
					Gender gender = personDao.findGenderByCode(code.toString());
					if (gender == null) {
						gender = personDao.findGenderByName(code.toString());
					}
					person.setGender(gender);
				}
			} else if (property.equals("race")) {
				Object code = record.get(property);
				if (code != null) {
					Race race = personDao.findRaceByCode(code.toString());
					if (race == null) {
						race = personDao.findRaceByName(code.toString());
					}
					person.setRace(race);
				}
			} else {
				Object value = record.get(property);
				if (value != null) {
					setEntityValue(value, property, person);
				}
			}
		}

		// Person identifiers
		for (Identifier identifier : record.getIdentifiers()) {
			PersonIdentifier personIdentifier = buildIdentifier(person, identifier);
			person.addPersonIdentifier(personIdentifier);
		}
		return person;
	}

	public static PersonIdentifier buildIdentifier(Person person, Identifier identifier) {
		if (identifier == null) {
			return null;
		}
		org.openhie.openempi.model.PersonIdentifier id = new org.openhie.openempi.model.PersonIdentifier();
			if (identifier.getIdentifierId() != null) {
				id.setPersonIdentifierId(identifier.getIdentifierId().intValue());
			}
			id.setIdentifier(identifier.getIdentifier());
			id.setPerson(person);

			IdentifierDomain idDomain = new IdentifierDomain();
			if (identifier.getIdentifierDomain() != null) {
				id.setIdentifierDomain(idDomain);
				idDomain.setIdentifierDomainId(identifier.getIdentifierDomain().getIdentifierDomainId());
				idDomain.setIdentifierDomainName(identifier.getIdentifierDomain().getIdentifierDomainName());
				idDomain.setNamespaceIdentifier(identifier.getIdentifierDomain().getNamespaceIdentifier());
				idDomain.setUniversalIdentifier(identifier.getIdentifierDomain().getUniversalIdentifier());
				idDomain.setUniversalIdentifierTypeCode(identifier.getIdentifierDomain().getUniversalIdentifierTypeCode());
			}
		return id;
	}

	public static Identifier buildIdentifier(PersonIdentifier personIdentifier) {
		if (personIdentifier == null) {
			return null;
		}
		org.openhie.openempi.model.Identifier id = new org.openhie.openempi.model.Identifier();
			if (personIdentifier.getPersonIdentifierId() != null) {
				id.setIdentifierId(personIdentifier.getPersonIdentifierId().longValue());
			}
			id.setIdentifier(personIdentifier.getIdentifier());
			//id.setRecord(record);
			IdentifierDomain idDomain = personIdentifier.getIdentifierDomain();
			if (idDomain != null) {
				id.setIdentifierDomain(idDomain);
			}
		return id;
	}


	public static Integer generateReviewRecordPairIntegerIdFromRecordLinkStringId(String recordLinkId) {
/*        String[] result = recordLinkId.split(":");
        String leftId = result[1];
	        int index = leftId.indexOf('.');
	        leftId = leftId.substring(0, index);
*/
        String[] result = recordLinkId.split(":");
        String leftId = result[0];
        leftId = leftId.substring(1);
        String rightId = result[1];
        int  left = Integer.parseInt(leftId);
        int  right = Integer.parseInt(rightId);
        Integer reviewRecordPairId = new Integer((left << 16) + right);
        return reviewRecordPairId;
	}

	public static ReviewRecordPair getReviewRecordPairFromRecordLink(PersonDao personDao, RecordLink recordLink) {
		ReviewRecordPair reviewRecordPair = new ReviewRecordPair();
		if (recordLink != null) {

			Integer recordLinkId = generateReviewRecordPairIntegerIdFromRecordLinkStringId(recordLink.getRecordLinkId());
			reviewRecordPair.setReviewRecordPairId( recordLinkId);

			if (recordLink.getLeftRecord() != null && recordLink.getRightRecord() != null) {
				reviewRecordPair.setPersonLeft( getPersonFromRecord(personDao, recordLink.getLeftRecord()));
				reviewRecordPair.setPersonRight( getPersonFromRecord(personDao, recordLink.getRightRecord()));
			}

			reviewRecordPair.setDateCreated(recordLink.getDateCreated());
			reviewRecordPair.setDateReviewed(recordLink.getDateReviewed());
			// reviewRecordPair.setLinkSource(recordLink.getLinkSource());
			LinkSource linkSource = recordLink.getLinkSource();
			if (linkSource != null) {
				switch (linkSource.getLinkSourceId()) {
				case 1:
					linkSource.setSourceName("Manual Matching");
					break;
				case 2:
					linkSource.setSourceName("Exact Matching Algorithm");
					break;
				case 3:
					linkSource.setSourceName("Probabilistic Matching Algorithm");
					break;
				}
			}
			reviewRecordPair.setLinkSource(linkSource);
			reviewRecordPair.setWeight(recordLink.getWeight());

			reviewRecordPair.setUserCreatedBy(recordLink.getUserCreatedBy());
			reviewRecordPair.setUserReviewedBy(recordLink.getUserReviewedBy());
		}
		return reviewRecordPair;
	}

    public static RecordLink getRecordLinkFromReviewRecordPair(Entity entityDef, ReviewRecordPair recordPair) {
        if (recordPair != null) {
            RecordLink recordLink;
            Integer reviewRecordPairId = recordPair.getReviewRecordPairId();

            if (reviewRecordPairId == null) {
                // new instance
                recordLink = new RecordLink();
            } else {
                int left = reviewRecordPairId.intValue() >> 16;
                int right = reviewRecordPairId.intValue() & 0xFF;

                recordLink = new RecordLink("#"+Integer.toString(left)+":"+Integer.toString(right));
            }

            if (recordPair.getPersonLeft() != null && recordPair.getPersonRight() != null) {
                recordLink.setLeftRecord(getRecordFromPerson(entityDef, recordPair.getPersonLeft()));
                recordLink.setRightRecord(getRecordFromPerson(entityDef, recordPair.getPersonRight()));
            }

            recordLink.setDateCreated(recordPair.getDateCreated());
            recordLink.setDateReviewed(recordPair.getDateReviewed());
            recordLink.setLinkSource(recordPair.getLinkSource());
            recordLink.setWeight(recordPair.getWeight());

            recordLink.setUserCreatedBy(recordPair.getUserCreatedBy());
            recordLink.setUserReviewedBy(recordPair.getUserReviewedBy());
            return recordLink;
        }
        return null;
    }

    public static RecordLink createRecordLinkFromPersons(Entity entityDef, PersonLink personLink, Person personLeft, Person personRight) {
        if (personLink != null && personLeft != null && personRight != null) {
            RecordLink recordLink = new RecordLink();;

            recordLink.setLeftRecord(getRecordFromPerson(entityDef, personLeft));
            recordLink.setRightRecord(getRecordFromPerson(entityDef, personRight));

            Date date = personLink.getDateCreated();
            recordLink.setDateCreated(date);
            recordLink.setDateReviewed(date);

            recordLink.setLinkSource(personLink.getLinkSource());
            recordLink.setWeight(personLink.getWeight());

            User user = personLink.getUserCreatedBy();
            recordLink.setUserCreatedBy(user);
            recordLink.setUserReviewedBy(user);

            recordLink.setState(RecordLinkState.MATCH);
            return recordLink;
        }
        return null;
    }

    public static PersonLink getPersonLinkFromReviewRecordPair(ReviewRecordPair reviewRecordPair) {
        if (reviewRecordPair != null) {
            PersonLink personLink = new PersonLink();
            personLink.setPersonLinkId(reviewRecordPair.getReviewRecordPairId());
            if (reviewRecordPair.getPersonLeft() != null && reviewRecordPair.getPersonRight() != null) {
                personLink.setPersonLeft(reviewRecordPair.getPersonLeft());
                personLink.setPersonRight(reviewRecordPair.getPersonRight());
            }

            personLink.setDateCreated(reviewRecordPair.getDateCreated());
            personLink.setLinkSource(reviewRecordPair.getLinkSource());
            personLink.setWeight(reviewRecordPair.getWeight());

            personLink.setUserCreatedBy(reviewRecordPair.getUserCreatedBy());

            return personLink;
        }
        return null;
    }

    public static RecordLink getRecordLinkFromPersonLink(Entity entityDef, PersonLink personLink) {
        if (personLink != null) {
            RecordLink recordLink;
            Integer personbLinkId = personLink.getPersonLinkId();

            if (personbLinkId == null) {
                // new instance
                recordLink = new RecordLink();
            } else {
                int left = personbLinkId.intValue() >> 16;
                int right = personbLinkId.intValue() & 0xFF;

                recordLink = new RecordLink("#"+Integer.toString(left)+":"+Integer.toString(right));
            }

            if (personLink.getPersonLeft() != null && personLink.getPersonRight() != null) {
                recordLink.setLeftRecord(getRecordFromPerson(entityDef, personLink.getPersonLeft()));
                recordLink.setRightRecord(getRecordFromPerson(entityDef, personLink.getPersonRight()));
            }

            Date date = personLink.getDateCreated();
            recordLink.setDateCreated(date);
            recordLink.setDateReviewed(date);

            recordLink.setLinkSource(personLink.getLinkSource());
            recordLink.setWeight(personLink.getWeight());

            User user = personLink.getUserCreatedBy();
            recordLink.setUserCreatedBy(user);
            recordLink.setUserReviewedBy(user);
            return recordLink;
        }
        return null;
    }

    public static PersonLink getPersonLinkFromRecordLink(PersonDao personDao, RecordLink recordLink) {
        PersonLink personLink = new PersonLink();
        if (recordLink != null) {

            Integer recordLinkId = generateReviewRecordPairIntegerIdFromRecordLinkStringId(recordLink.getRecordLinkId());
            personLink.setPersonLinkId(recordLinkId);

            if (recordLink.getLeftRecord() != null && recordLink.getRightRecord() != null) {
                personLink.setPersonLeft(getPersonFromRecord(personDao, recordLink.getLeftRecord()));
                personLink.setPersonRight(getPersonFromRecord(personDao, recordLink.getRightRecord()));
            }

            personLink.setDateCreated(recordLink.getDateCreated());
            personLink.setLinkSource(recordLink.getLinkSource());
            personLink.setWeight(recordLink.getWeight());

            personLink.setUserCreatedBy(recordLink.getUserCreatedBy());
            return personLink;
        }
        return null;
    }

	public static void setEntityValue(Object value, String fieldName, Object entityInstance) {
		Method method = getMethod(entityInstance.getClass(), fieldName, value.getClass());
		if (method == null) {
			return;
		}
		try {
			method.invoke(entityInstance, value);
		} catch (Exception e) {
			log.warn("Unable to set the value of attribute " + fieldName + " to a value " + value + 
					" of type " + value.getClass());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Method getMethod(Class theClass, String fieldName, Class paramClass) {
		Method method = methodByFieldName.get(fieldName);
		if (method != null) {
			return method;
		}
		String methodName = getMethodName(fieldName);
		try {
			method = theClass.getDeclaredMethod(methodName, paramClass);
			methodByFieldName.put(fieldName, method);
		} catch (Exception e) {
			log.warn("Unable to field entity method for setting field " + fieldName + " to a value of type " + paramClass);
			return null;
		}
		return method;
	}

	public static String getMethodName(String fieldName) {
		return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
	}

    public static Record convertKeyValListToRecord(Entity entity, List<String> keyValList) {
        Record record = new Record(entity);
        for (String entry : keyValList) {
            String[] keyValue = entry.split(",");

            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                EntityAttribute attribute = entity.findAttributeByName(key);

                switch(AttributeDatatype.getById(attribute.getDatatype().getDatatypeCd())) {
                case DATE:
                    record.set(key, stringToDate(value));
                    break;
                case TIMESTAMP:
                    record.set(key, stringToDateTime(value));
                    break;
                case LONG:
                    record.set(key, Long.parseLong(value));
                    break;
                case INTEGER:
                    record.set(key, Integer.parseInt(value));
                    break;
                case DOUBLE:
                    record.set(key, Double.parseDouble(value));
                    break;
                case FLOAT:
                    record.set(key, Float.parseFloat(value));
                    break;
                case BOOLEAN:
                    record.set(key, Boolean.parseBoolean(value));
                    break;
                default:
                    // String
                    record.set(key, value);
                    break;
                }
            }
        }
        return record;
    }

	public static String dateToString(Date date)
	{
		if (date == null) {
			return "";
		}
		return dateFormat.format(date);
	}

	public static String dateTimeToString(Date date)
	{
		if (date == null) {
			return "";
		}
		return dateTimeFormat.format(date);
	}

    public static Date stringToDate(String strDate) {
        Date date = null;
        if (strDate == null || strDate.isEmpty()) {
            return date;
        }
        try {
            date = dateFormat.parse(strDate);
        } catch (ParseException e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to parse string to date: " + strDate);
            }
        }
        return date;
    }

    public static Date stringToDateTime(String strDate) {
        Date date = null;
        if (strDate == null || strDate.isEmpty()) {
            return date;
        }
        try {
            date = dateTimeFormat.parse(strDate);
        } catch (ParseException e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to parse string to date: " + strDate);
            }
        }
        return date;
    }

	public static RecordLink createRecordLinkFromRecordPair(RecordPair pair) {
		RecordLink link = new RecordLink();
		link.setDateCreated(new Date());
		link.setLeftRecord(pair.getLeftRecord());
		link.setRightRecord(pair.getRightRecord());
		link.setLinkSource(pair.getLinkSource());
		RecordLinkState state = null;
		if (pair.getMatchOutcome() == RecordPair.MATCH_OUTCOME_LINKED) {
			state = RecordLinkState.MATCH;
		} else if (pair.getMatchOutcome() == RecordPair.MATCH_OUTCOME_POSSIBLE) {
			state = RecordLinkState.POSSIBLE_MATCH;
		} else {
			state = RecordLinkState.NON_MATCH;
		}
		link.setState(state);
		link.setUserCreatedBy(Context.getUserContext().getUser());
		link.setVector(pair.getVector());
		link.setWeight(pair.getWeight());
		return link;
	}



    // entity to EntityModel mapping
    public static EntityModel mapToEntityModel(Entity entity, Class<EntityModel> entityClass) {
        if (log.isDebugEnabled()) {
            log.debug("Transforming object of type " + entity.getClass() + " to type " + entityClass);
        }

        EntityModel entityModel = new EntityModel();
        entityModel.setName(entity.getName());
        entityModel.setDisplayName(entity.getDisplayName());
        entityModel.setDescription(entity.getDescription());
        entityModel.setVersionId(entity.getVersionId());

        if (entity.getAttributes() != null && entity.getAttributes().size() > 0) {

            AttributesType entityAttributes = new AttributesType();
            for (EntityAttribute entityAttribute : entity.getAttributes()) {

            // Keep Custom field for export
                 // Attribute is Custom field
                 // if (entityAttribute.getIsCustom()) {
                 //     continue;
                 // }

                 AttributeType attributeType = new AttributeType();
                 attributeType.setName(entityAttribute.getName());
                 attributeType.setDisplayName(entityAttribute.getDisplayName());
                 attributeType.setDescription(entityAttribute.getDescription());
                 attributeType.setDisplayOrder(entityAttribute.getDisplayOrder());
                 attributeType.setIndexed(entityAttribute.getIndexed());
                 attributeType.setSearchable(entityAttribute.getSearchable());
                 attributeType.setCaseInsensitive(entityAttribute.getCaseInsensitive());
                 attributeType.setIsCustom(entityAttribute.getIsCustom());

                 attributeType.setDataTypeCode(entityAttribute.getDatatype().getDatatypeCd());

                 entityAttributes.getAttribute().add(attributeType);
            }
            entityModel.setAttributes(entityAttributes);
        }
        return entityModel;
    }

    public static EntityModel setGroupInfoToEntityModel(List<EntityAttributeGroup> groups, EntityModel entityModel) {

        // set groups to EntityModel
        GroupsType attributeGroups = new GroupsType();
        for (EntityAttributeGroup group : groups) {
            GroupType groupType = new GroupType();
            groupType.setName(group.getName());
            groupType.setDisplayName(group.getDisplayName());
            groupType.setDisplayOrder(group.getDisplayOrder());

            attributeGroups.getGroup().add(groupType);
        }
        entityModel.setGroups(attributeGroups);

        // set group info to attributeType
        AttributesType attributes = entityModel.getAttributes();
        if (attributes != null) {
            for (AttributeType attributeType: attributes.getAttribute()) {
                String name = attributeType.getName();
                for (EntityAttributeGroup group : groups) {

                    if (group.findEntityAttributeByName(name) != null) {
                        attributeType.setGroupName(group.getName());
                    }
                }
            }
        }
        return entityModel;
    }

    public static ValidationsType mapToEntityValidations(List<EntityAttributeValidation> validations) {

        ValidationsType validationsType = new ValidationsType();
        for (EntityAttributeValidation validation : validations) {

             ValidationType validationType = new ValidationType();
             validationType.setName(validation.getName());
             validationType.setDisplayName(validation.getDisplayName());

             ValidationParametersType validationParametersType = new ValidationParametersType();
             for (EntityAttributeValidationParameter parameter : validation.getParameters()) {

                  ValidationParameterType validationParameterType = new ValidationParameterType();
                  validationParameterType.setName(parameter.getName());
                  validationParameterType.setValue(parameter.getValue());

                  validationParametersType.getParameter().add(validationParameterType);
             }
             validationType.setParameters(validationParametersType);

             validationsType.getValidation().add(validationType);
        }
        return validationsType;
    }

    public static Entity mapToEntityModel(EntityModel entityModel, List<EntityAttributeDatatype> types, Class<Entity> entityClass) {
        if (log.isDebugEnabled()) {
            log.debug("Transforming object of type " + entityModel.getClass() + " to type " + entityClass);
        }

        Entity entity = new Entity();
        entity.setName(entityModel.getName());
        entity.setDisplayName(entityModel.getDisplayName());
        entity.setDescription(entityModel.getDescription());
        entity.setVersionId(entityModel.getVersionId());

        if (entityModel.getAttributes() != null && entityModel.getAttributes().getAttribute().size() > 0) {
            for (AttributeType attributeType : entityModel.getAttributes().getAttribute()) {
                 EntityAttribute entityAttribute = new EntityAttribute();

                 entityAttribute.setName(attributeType.getName());
                 entityAttribute.setDisplayName(attributeType.getDisplayName());
                 entityAttribute.setDescription(attributeType.getDescription());
                 entityAttribute.setDisplayOrder(attributeType.getDisplayOrder());
                 entityAttribute.setIndexed(attributeType.isIndexed());
                 entityAttribute.setSearchable(attributeType.isSearchable());
                 entityAttribute.setCaseInsensitive(attributeType.isCaseInsensitive());
                 entityAttribute.setIsCustom(attributeType.isIsCustom());

                 EntityAttributeDatatype entityAttributeDatatype = null;
                 for (EntityAttributeDatatype type : types) {
                     if (type.getDatatypeCd() == attributeType.getDataTypeCode()) {
                         entityAttributeDatatype = type;
                     }
                 }
                 entityAttribute.setDatatype(entityAttributeDatatype);

                 entity.addAttribute(entityAttribute);
            }
        }
        return entity;
    }

    public static List<EntityAttributeGroup> mapToEntityGroup(EntityModel entityModel, Entity newEntity, Class<EntityAttributeGroup> groupsClass) {
        if (log.isDebugEnabled()) {
            log.debug("Transforming object of type " + entityModel.getClass() + " to list of type " + groupsClass);
        }

        List<EntityAttributeGroup> attributeGroups = new ArrayList<EntityAttributeGroup>(entityModel.getGroups().getGroup().size());
        for (GroupType groupType : entityModel.getGroups().getGroup()) {
             EntityAttributeGroup group = new EntityAttributeGroup();
             group.setName(groupType.getName());
             group.setDisplayName(groupType.getDisplayName());
             group.setDisplayOrder(groupType.getDisplayOrder());
             group.setEntity(newEntity);

             for (AttributeType attributeType : entityModel.getAttributes().getAttribute()) {
                  if (attributeType.getGroupName() != null && attributeType.getGroupName().equals(group.getName())) {

                      EntityAttributeGroupAttribute groupAttribute = new EntityAttributeGroupAttribute();
                      groupAttribute.setEntityAttributeGroup(group);
                      groupAttribute.setEntityAttribute(newEntity.findAttributeByName(attributeType.getName()));

                      group.addEntityAttributeGroupAttribute(groupAttribute);
                  }
             }
             attributeGroups.add(group);
        }

        return attributeGroups;
    }

    public static List<EntityAttributeValidation> mapToEntityValidations(AttributeType attributeType, Entity newEntity, Class<EntityAttributeValidation> validationsClass) {
        if (log.isDebugEnabled()) {
            log.debug("Transforming object of type " + attributeType.getClass() + " to list of type " + validationsClass);
        }

        List<EntityAttributeValidation> attributeValidations = new ArrayList<EntityAttributeValidation>(attributeType.getValidations().getValidation().size());
        EntityAttribute attribute = newEntity.findAttributeByName(attributeType.getName());

        for (ValidationType validationType : attributeType.getValidations().getValidation()) {

             EntityAttributeValidation validation = new EntityAttributeValidation();
             validation.setName(validationType.getName());
             validation.setDisplayName(validationType.getDisplayName());
             validation.setEntityAttribute(attribute);

             for (ValidationParameterType parameterType : validationType.getParameters().getParameter()) {

                  EntityAttributeValidationParameter param = new EntityAttributeValidationParameter();
                  param.setName(parameterType.getName());
                  param.setValue(parameterType.getValue());
                  param.setEntityAttributeValidation(validation);
                  validation.addParameter(param);
             }
             attributeValidations.add(validation);
        }
        return attributeValidations;
    }
}
