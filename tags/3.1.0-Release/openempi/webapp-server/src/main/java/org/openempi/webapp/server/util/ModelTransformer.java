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
package org.openempi.webapp.server.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.openempi.webapp.client.model.AuditEventEntryWeb;
import org.openempi.webapp.client.model.AuditEventTypeWeb;
import org.openempi.webapp.client.model.AuditEventWeb;
import org.openempi.webapp.client.model.DataProfileWeb;
import org.openempi.webapp.client.model.IdentifierWeb;
import org.openempi.webapp.client.model.JobEntryEventLogWeb;
import org.openempi.webapp.client.model.JobStatusWeb;
import org.openempi.webapp.client.model.JobTypeWeb;
import org.openempi.webapp.client.model.MessageLogEntryWeb;
import org.openempi.webapp.client.model.MessageTypeWeb;
import org.openempi.webapp.client.model.PersonIdentifierWeb;
import org.openempi.webapp.client.model.PersonWeb;
import org.openempi.webapp.client.model.RecordLinkWeb;
import org.openempi.webapp.client.model.RecordPairWeb;
import org.openempi.webapp.client.model.ReportParameterWeb;
import org.openempi.webapp.client.model.ReportQueryParameterWeb;
import org.openempi.webapp.client.model.ReportQueryWeb;
import org.openempi.webapp.client.model.ReportRequestEntryWeb;
import org.openempi.webapp.client.model.ReportRequestParameterWeb;
import org.openempi.webapp.client.model.ReportRequestWeb;
import org.openempi.webapp.client.model.ReportWeb;
import org.openempi.webapp.client.model.UserWeb;
import org.openempi.webapp.client.model.RoleWeb;
import org.openempi.webapp.client.model.PermissionWeb;
import org.openempi.webapp.client.model.ParameterTypeWeb;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.EntityAttributeWeb;
import org.openempi.webapp.client.model.EntityAttributeGroupWeb;
import org.openempi.webapp.client.model.EntityAttributeValidationWeb;
import org.openempi.webapp.client.model.EntityAttributeValidationParameterWeb;
import org.openempi.webapp.client.model.EntityAttributeDatatypeWeb;
import org.openempi.webapp.client.model.RecordWeb;
import org.openempi.webapp.client.model.LinkSourceWeb;
import org.openempi.webapp.client.model.JobEntryWeb;

import org.openhie.openempi.entity.Constants;
import org.openhie.openempi.model.AttributeDatatype;
import org.openhie.openempi.model.AuditEvent;
import org.openhie.openempi.model.AuditEventEntry;
import org.openhie.openempi.model.AuditEventType;
import org.openhie.openempi.model.DataProfile;
import org.openhie.openempi.model.EntityAttributeGroupAttribute;
import org.openhie.openempi.model.EntityAttributeValidationParameter;
import org.openhie.openempi.model.Gender;
import org.openhie.openempi.model.JobEntryEventLog;
import org.openhie.openempi.model.JobStatus;
import org.openhie.openempi.model.JobType;
import org.openhie.openempi.model.MessageLogEntry;
import org.openhie.openempi.model.MessageType;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.model.RecordPair;
import org.openhie.openempi.model.User;
import org.openhie.openempi.model.Role;
import org.openhie.openempi.model.Permission;
import org.openhie.openempi.model.Report;
import org.openhie.openempi.model.ReportParameter;
import org.openhie.openempi.model.ReportQuery;
import org.openhie.openempi.model.ReportQueryParameter;
import org.openhie.openempi.model.ReportRequest;
import org.openhie.openempi.model.ReportRequestEntry;
import org.openhie.openempi.model.ReportRequestParameter;
import org.openhie.openempi.model.FormEntryDisplayType;
import org.openhie.openempi.model.ParameterType;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.EntityAttributeGroup;
import org.openhie.openempi.model.EntityAttributeValidation;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.JobEntry;

public class ModelTransformer
{
	public static Logger log = Logger.getLogger(ModelTransformer.class);

	public static <T> T map(Object sourceObject, Class<T> destClass) {
		Mapper mapper = new DozerBeanMapper();
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + sourceObject.getClass() + " to type " + destClass);
		}
		return mapper.map(sourceObject, destClass);
	}

	public static <T> Set<T> mapSet(Set<?> sourceObjects, Class<T> destClass) {
		if (sourceObjects == null || sourceObjects.size() == 0) {
			return new HashSet<T>();
		}
		if (log.isDebugEnabled()) {
			log.debug("Transforming collection of objects of type " + sourceObjects.iterator().next().getClass() + " to type " + destClass);
		}
		Mapper mapper = new DozerBeanMapper();
		Set<T> collection = new HashSet<T>(sourceObjects.size());
		for (Object o : sourceObjects) {
			T mo = mapper.map(o, destClass);
			collection.add(mo);
		}
		return collection;
	}

	public static <T> List<T> mapList(List<?> sourceObjects, Class<T> destClass) {
		if (sourceObjects == null || sourceObjects.size() == 0) {
			return new ArrayList<T>();
		}
		if (log.isDebugEnabled()) {
			log.debug("Transforming collection of objects of type " + sourceObjects.get(0).getClass() + " to type " + destClass);
		}
		Mapper mapper = new DozerBeanMapper();
		List<T> collection = new ArrayList<T>(sourceObjects.size());
		for (Object o : sourceObjects) {
			T mo = mapper.map(o, destClass);
			collection.add(mo);
		}
		return collection;
	}

	// Person mapping
	public static Person mapToPerson(PersonWeb personWeb, Class<Person> personClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + personWeb.getClass() + " to type " + personClass);
		}
		Person thePerson = new Person();
		thePerson.setPersonId(personWeb.getPersonId());
		thePerson.setGivenName(personWeb.getGivenName());
		thePerson.setMiddleName(personWeb.getMiddleName());
		thePerson.setFamilyName(personWeb.getFamilyName());
		thePerson.setPrefix(personWeb.getPrefix());
		thePerson.setSuffix(personWeb.getSuffix());
		thePerson.setDateOfBirth(StringToDate(personWeb.getDateOfBirth()));
		thePerson.setBirthPlace(personWeb.getBirthPlace());
		thePerson.setDeathInd(personWeb.getDeathInd());
		thePerson.setDeathTime(StringToDateTime(personWeb.getDeathTime()));
		thePerson.setMultipleBirthInd(personWeb.getMultipleBirthInd());
		thePerson.setBirthOrder(personWeb.getBirthOrder());
		thePerson.setMothersMaidenName(personWeb.getMothersMaidenName());
		thePerson.setMotherName(personWeb.getMotherName());
		thePerson.setFatherName(personWeb.getFatherName());
		thePerson.setSsn(personWeb.getSsn());
		thePerson.setMaritalStatusCode(personWeb.getMaritalStatusCode());
		thePerson.setDegree(personWeb.getDegree());
		thePerson.setEmail(personWeb.getEmail());
		thePerson.setCountry(personWeb.getCountry());
		thePerson.setCountryCode(personWeb.getCountryCode());
		thePerson.setPhoneCountryCode(personWeb.getPhoneCountryCode());
		thePerson.setPhoneAreaCode(personWeb.getPhoneAreaCode());
		thePerson.setPhoneExt(personWeb.getPhoneExt());
		thePerson.setPhoneNumber(personWeb.getPhoneNumber());
		String genderCode = personWeb.getGender();
		if (genderCode != null) {
			Gender gender = new Gender();
			gender.setGenderCode(genderCode);
			thePerson.setGender(gender);
		}
		thePerson.setAddress1(personWeb.getAddress1());
		thePerson.setAddress2(personWeb.getAddress2());
		thePerson.setCity(personWeb.getCity());
		thePerson.setState(personWeb.getState());
		thePerson.setPostalCode(personWeb.getPostalCode());
		thePerson.setVillage(personWeb.getVillage());
		thePerson.setVillageId(personWeb.getVillageId());
		thePerson.setSector(personWeb.getSector());
		thePerson.setSectorId(personWeb.getSectorId());
		thePerson.setCell(personWeb.getCell());
		thePerson.setCellId(personWeb.getCellId());
		thePerson.setDistrict(personWeb.getDistrict());
		thePerson.setDistrictId(personWeb.getDistrictId());
		thePerson.setProvince(personWeb.getProvince());
		thePerson.setDateChanged(StringToDateTime(personWeb.getDateChanged()));
		thePerson.setDateCreated(StringToDateTime(personWeb.getDateCreated()));

		if (personWeb.getPersonIdentifiers() != null && personWeb.getPersonIdentifiers().size() > 0) {
			for (PersonIdentifierWeb personIdentifier : personWeb.getPersonIdentifiers()) {
				org.openhie.openempi.model.PersonIdentifier personId = ModelTransformer.map(personIdentifier, org.openhie.openempi.model.PersonIdentifier.class);
				thePerson.addPersonIdentifier(personId);
			}
		}
		return thePerson;
	}

	public static PersonWeb mapToPerson(Person person, Class<PersonWeb> personClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + person.getClass() + " to type " + personClass);
		}
		if (person == null) {
			return null;
		}

		PersonWeb thePerson = new PersonWeb();
		thePerson.setPersonId(person.getPersonId());
		thePerson.setGivenName(person.getGivenName());
		thePerson.setFamilyName(person.getFamilyName());
		thePerson.setMiddleName(person.getMiddleName());
		thePerson.setDateOfBirth(DateToString(person.getDateOfBirth()));
		thePerson.setDeathInd(person.getDeathInd());
		thePerson.setDeathTime(DateTimeToString(person.getDeathTime()));
		Gender gender = person.getGender();
		if (gender != null) {
			thePerson.setGender(gender.getGenderName());
		}
		thePerson.setAddress1(person.getAddress1());
		thePerson.setAddress2(person.getAddress2());
		thePerson.setCity(person.getCity());
		thePerson.setState(person.getState());
		thePerson.setPostalCode(person.getPostalCode());
		thePerson.setVillage(person.getVillage());
		thePerson.setVillageId(person.getVillageId());
		thePerson.setSector(person.getSector());
		thePerson.setSectorId(person.getSectorId());
		thePerson.setCell(person.getCell());
		thePerson.setCellId(person.getCellId());
		thePerson.setDistrict(person.getDistrict());
		thePerson.setDistrictId(person.getDistrictId());
		thePerson.setProvince(person.getProvince());
		thePerson.setSuffix(person.getSuffix());
		thePerson.setPrefix(person.getPrefix());
		thePerson.setEmail(person.getEmail());
		thePerson.setBirthPlace(person.getBirthPlace());
		thePerson.setMultipleBirthInd(person.getMultipleBirthInd());
		thePerson.setBirthOrder(person.getBirthOrder());
		thePerson.setMothersMaidenName(person.getMothersMaidenName());
		thePerson.setMotherName(person.getMotherName());
		thePerson.setFatherName(person.getFatherName());
		thePerson.setMaritalStatusCode(person.getMaritalStatusCode());
		thePerson.setSsn(person.getSsn());
		thePerson.setDegree(person.getDegree());
		thePerson.setCountry(person.getCountry());
		thePerson.setPhoneAreaCode(person.getPhoneAreaCode());
		thePerson.setPhoneCountryCode(person.getPhoneCountryCode());
		thePerson.setPhoneExt(person.getPhoneExt());
		thePerson.setPhoneNumber(person.getPhoneNumber());
		thePerson.setDateChanged(DateTimeToString(person.getDateChanged()));
		thePerson.setDateCreated(DateTimeToString(person.getDateCreated()));

		if (person.getPersonIdentifiers() != null && person.getPersonIdentifiers().size() > 0) {
			Set<PersonIdentifierWeb> identifiers = new java.util.HashSet<PersonIdentifierWeb>(person.getPersonIdentifiers().size());
			for (org.openhie.openempi.model.PersonIdentifier personIdentifier : person.getPersonIdentifiers()) {
				identifiers.add(ModelTransformer.map(personIdentifier, PersonIdentifierWeb.class));
			}
			thePerson.setPersonIdentifiers(identifiers);
		}
		return thePerson;
	}

	//------------------------------------------------------------------------------------------------

		// Role mapping
		public static Role mapToRole(RoleWeb roleWeb, Class<Role> roleClass) {
			if (log.isDebugEnabled()) {
				log.debug("Transforming object of type " + roleWeb.getClass() + " to type " + roleClass);
			}
			Role theRole = new Role();
			theRole.setId(roleWeb.getId());
			theRole.setName(roleWeb.getName());
			theRole.setDescription(roleWeb.getDescription());

			if (roleWeb.getPermissions() != null && roleWeb.getPermissions().size() > 0) {
				Set<Permission> permissions= new java.util.HashSet<Permission>(roleWeb.getPermissions().size());
				for (PermissionWeb permissionWeb : roleWeb.getPermissions()) {
					org.openhie.openempi.model.Permission  permission = ModelTransformer.map(permissionWeb, org.openhie.openempi.model.Permission.class);					
					permissions.add(permission);
				}
				theRole.setPermissions(permissions);
			}
			return theRole;
		}

		public static RoleWeb mapToRole(Role role, Class<RoleWeb> roleClass, boolean withPermissions) {
			if (log.isDebugEnabled()) {
				log.debug("Transforming object of type " + role.getClass() + " to type " + roleClass);
			}
			RoleWeb theRole = new RoleWeb();
			theRole.setId(role.getId());
			theRole.setName(role.getName());
			theRole.setDescription(role.getDescription());

			if (withPermissions) {
				if (role.getPermissions() != null && role.getPermissions().size() > 0) {
					Set<PermissionWeb> permissionsWeb = new java.util.HashSet<PermissionWeb>(role.getPermissions().size());
					for (org.openhie.openempi.model.Permission permission: role.getPermissions()) {
						permissionsWeb.add( ModelTransformer.map(permission, PermissionWeb.class));
					}
					theRole.setPermissions(permissionsWeb);
				}
			}

			return theRole;
		}

//------------------------------------------------------------------------------------------------

	// User mapping
	public static User mapToUser(UserWeb userWeb, Class<User> userClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + userWeb.getClass() + " to type " + userClass);
		}
		User theUser = new User();
		theUser.setId(userWeb.getId());
		theUser.setUsername(userWeb.getUsername());
		theUser.setPassword(userWeb.getPassword());
		theUser.setConfirmPassword(userWeb.getConfirmPassword());
		theUser.setPasswordHint(userWeb.getPasswordHint());
		theUser.setFirstName(userWeb.getFirstName());
		theUser.setLastName(userWeb.getLastName());
		theUser.setPhoneNumber(userWeb.getPhoneNumber());
		theUser.setEmail(userWeb.getEmail());
		theUser.setWebsite(userWeb.getWebsite());

		theUser.getAddress().setAddress(userWeb.getAddress());
		theUser.getAddress().setCity(userWeb.getCity());
		theUser.getAddress().setProvince(userWeb.getState());
		theUser.getAddress().setPostalCode(userWeb.getPostalCode());
		theUser.getAddress().setCountry(userWeb.getCountry());

		if (userWeb.getVersion() != null) {
		   theUser.setVersion(Integer.parseInt(userWeb.getVersion()));
		}
		theUser.setEnabled(userWeb.getEnabled());
		theUser.setAccountExpired(userWeb.getAccountExpired());
		theUser.setAccountLocked(userWeb.getAccountLocked());
		theUser.setCredentialsExpired(userWeb.getCredentialsExpired());

		if (userWeb.getRoles() != null && userWeb.getRoles().size() > 0) {
			for (RoleWeb roleWeb : userWeb.getRoles()) {
				org.openhie.openempi.model.Role  role = ModelTransformer.mapToRole(roleWeb, org.openhie.openempi.model.Role.class);
				theUser.addRole(role);
			}
		}
		return theUser;
	}

	public static UserWeb mapToUser(User user, Class<UserWeb> userClass, boolean withRoles) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + user.getClass() + " to type " + userClass);
		}
		UserWeb theUser = new UserWeb();
		theUser.setId(user.getId());
		theUser.setUsername(user.getUsername());
		theUser.setPassword(user.getPassword());
		theUser.setConfirmPassword(user.getConfirmPassword());
		theUser.setPasswordHint(user.getPasswordHint());
		theUser.setFirstName(user.getFirstName());
		theUser.setLastName(user.getLastName());
		theUser.setPhoneNumber(user.getPhoneNumber());
		theUser.setEmail(user.getEmail());
		theUser.setWebsite(user.getWebsite());

		theUser.setAddress(user.getAddress().getAddress());
		theUser.setCity(user.getAddress().getCity());
		theUser.setState(user.getAddress().getProvince());
		theUser.setPostalCode(user.getAddress().getPostalCode());
		theUser.setCountry(user.getAddress().getCountry());

		theUser.setVersion(user.getVersion().toString());
		theUser.setEnabled(user.isEnabled());
		theUser.setAccountExpired(user.isAccountExpired());
		theUser.setAccountLocked(user.isAccountLocked());
		theUser.setCredentialsExpired(user.isCredentialsExpired());

		if (withRoles) {
			if (user.getRoles() != null && user.getRoles().size() > 0) {
				Set<RoleWeb> rolesWeb = new java.util.HashSet<RoleWeb>(user.getRoles().size());
				for (org.openhie.openempi.model.Role role : user.getRoles()) {
					rolesWeb.add(ModelTransformer.mapToRole(role, RoleWeb.class, false));
				}
				theUser.setRoles(rolesWeb);
			}
		}

		return theUser;
	}

	// Report mapping
	public static Report mapToReport(ReportWeb reportWeb, Class<Report> reportClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + reportWeb.getClass() + " to type " + reportClass);
		}
		Report theReport = new Report();
		theReport.setReportId(reportWeb.getReportId());
		theReport.setName(reportWeb.getName());
		theReport.setNameDisplayed(reportWeb.getNameDisplayed());
		theReport.setDescription(reportWeb.getDescription());
		theReport.setTemplateName(reportWeb.getTemplateName());
		theReport.setDataGenerator(reportWeb.getDataGenerator());

		java.util.Map<String,ReportParameter> reportParamByName = new java.util.HashMap<String,ReportParameter>();
		if (reportWeb.getReportParameters() != null && reportWeb.getReportParameters().size() > 0) {
			for (ReportParameterWeb reportParameterWeb : reportWeb.getReportParameters()) {
				org.openhie.openempi.model.ReportParameter reportParameter = ModelTransformer.map(reportParameterWeb, org.openhie.openempi.model.ReportParameter.class);
				theReport.addReportParameter(reportParameter);
				reportParameter.setReport(theReport);
				reportParamByName.put(reportParameter.getName(), reportParameter);
			}
		}

		if (reportWeb.getReportQueries() != null && reportWeb.getReportQueries().size() > 0) {
			for (ReportQueryWeb reportQueryWeb : reportWeb.getReportQueries()) {
				// org.openhie.openempi.model.ReportQuery reportQuery = ModelTransformer.map(reportQueryWeb, org.openhie.openempi.model.ReportQuery.class);
				 ReportQuery theReportQuery = new ReportQuery();
				 theReportQuery.setReportQueryId(reportQueryWeb.getReportQueryId());
				 theReportQuery.setName(reportQueryWeb.getName());
				 theReportQuery.setQuery(reportQueryWeb.getQuery());

				 // Query Parameters
				 if (reportQueryWeb.getReportQueryParameters() != null && reportQueryWeb.getReportQueryParameters().size() > 0) {
					Set<ReportQueryParameter> reportQueryParameters = new java.util.HashSet<ReportQueryParameter>(reportQueryWeb.getReportQueryParameters().size());
					for (ReportQueryParameterWeb reportQueryParameterWeb : reportQueryWeb.getReportQueryParameters()) {
						org.openhie.openempi.model.ReportQueryParameter reportQueryParameter = ModelTransformer.map(reportQueryParameterWeb, ReportQueryParameter.class);
						reportQueryParameters.add(reportQueryParameter);
						reportQueryParameter.setReportQuery(theReportQuery);
						log.debug("Looking up the report parameter by name: " + reportQueryParameterWeb.getReportParameter().getName());
						ReportParameter reportParameter = reportParamByName.get(reportQueryParameterWeb.getReportParameter().getName());
						if (reportParameter == null) {
							log.warn("Found a report query parameter that points to a report parameter that is not known. Parameter name is: " +
									reportQueryParameterWeb.getReportParameter().getName());
						}
						reportQueryParameter.setReportParameter(reportParameter);
					}
					theReportQuery.setReportQueryParameters(reportQueryParameters);
				 }
				 theReport.addReportQuery(theReportQuery);

				// set report back to reportQuery
				 theReportQuery.setReport(theReport);
			}
		}
		return theReport;
	}

	public static ReportWeb mapToReport(Report report, Class<ReportWeb> reportClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + report.getClass() + " to type " + reportClass);
		}

		ReportWeb theReport = new ReportWeb();
		theReport.setReportId(report.getReportId());
		theReport.setName(report.getName());
		theReport.setNameDisplayed(report.getNameDisplayed());
		theReport.setDescription(report.getDescription());
		theReport.setTemplateName(report.getTemplateName());
		theReport.setDataGenerator(report.getDataGenerator());

		// Parameters
		if (report.getReportParameters() != null && report.getReportParameters().size() > 0) {
			Set<ReportParameterWeb> reportParameterWebs = new java.util.HashSet<ReportParameterWeb>(report.getReportParameters().size());
			for (ReportParameter reportParameter : report.getReportParameters()) {
				reportParameterWebs.add( ModelTransformer.map(reportParameter, ReportParameterWeb.class));
			}
			theReport.setReportParameters(reportParameterWebs);
		}

		//Queries
		if (report.getReportQueries() != null && report.getReportQueries().size() > 0) {
			Set<ReportQueryWeb> reportQueryWebs = new java.util.HashSet<ReportQueryWeb>(report.getReportQueries().size());
			for (ReportQuery reportQuery : report.getReportQueries()) {

				 // reportQueryWebs.add( ModelTransformer.map(reportQuery, ReportQueryWeb.class));
				 ReportQueryWeb theReportQuery = new ReportQueryWeb();
				 theReportQuery.setReportQueryId(reportQuery.getReportQueryId());
				 theReportQuery.setName(reportQuery.getName());
				 theReportQuery.setQuery(reportQuery.getQuery());

				 // Query Parameters
				 if (reportQuery.getReportQueryParameters() != null && reportQuery.getReportQueryParameters().size() > 0) {
					Set<ReportQueryParameterWeb> reportQueryParameterWebs = new java.util.HashSet<ReportQueryParameterWeb>(reportQuery.getReportQueryParameters().size());
					for (ReportQueryParameter reportQueryParameter : reportQuery.getReportQueryParameters() ) {

						reportQueryParameterWebs.add(ModelTransformer.map(reportQueryParameter, ReportQueryParameterWeb.class));
					}
					theReportQuery.setReportQueryParameters(reportQueryParameterWebs);
				 }

				 reportQueryWebs.add(theReportQuery);
			}
			theReport.setReportQueries(reportQueryWebs);
		}
		return theReport;
	}

	// Report request mapping
	public static ReportRequest mapToReportRequest(ReportRequestWeb reportRequestWeb, Class<ReportRequest> reportRequestClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + reportRequestWeb.getClass() + " to type " + reportRequestClass);
		}
		ReportRequest theReportRequest = new ReportRequest();
		theReportRequest.setReportId(reportRequestWeb.getReportId());
		theReportRequest.setRequestDate(reportRequestWeb.getRequestDate());

		if (reportRequestWeb.getReportParameters() != null && reportRequestWeb.getReportParameters().size() > 0) {
			List<ReportRequestParameter> reportRequestParameters = new ArrayList<ReportRequestParameter>(reportRequestWeb.getReportParameters().size());

			int count = reportRequestWeb.getReportParameters().size();
			for (int i = 0; i < count; i++) {
				ReportRequestParameterWeb reportRequestParameterWeb = reportRequestWeb.getReportParameters().get(i);

				reportRequestParameters.add(ModelTransformer.map(reportRequestParameterWeb, ReportRequestParameter.class));
			}
			theReportRequest.setReportParameters(reportRequestParameters);
		}
		return theReportRequest;
	}

	public static ReportRequestWeb mapToReportRequest(ReportRequest reportRequest, Class<ReportRequestWeb> reportRequestClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + reportRequest.getClass() + " to type " + reportRequestClass);
		}

		ReportRequestWeb theReportRequest = new ReportRequestWeb();
		theReportRequest.setReportId(reportRequest.getReportId());
		theReportRequest.setRequestDate(reportRequest.getRequestDate());

		// Parameters
		if (reportRequest.getReportParameters() != null && reportRequest.getReportParameters().size() > 0) {
			List<ReportRequestParameterWeb> reportParameterWebs = new ArrayList<ReportRequestParameterWeb>(reportRequest.getReportParameters().size());

			int count = reportRequest.getReportParameters().size();
			for (int i = 0; i < count; i++) {
				ReportRequestParameter reportRequestParameter = reportRequest.getReportParameters().get(i);

				reportParameterWebs.add( ModelTransformer.map(reportRequestParameter, ReportRequestParameterWeb.class));
			}
			theReportRequest.setReportParameters(reportParameterWebs);
		}
		return theReportRequest;
	}

	// Report request entry mapping
	public static ReportRequestEntryWeb mapToReportRequestEntry(ReportRequestEntry reportRequestEntry, Class<ReportRequestEntryWeb> reportRequestEntryClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + reportRequestEntry.getClass() + " to type " + reportRequestEntryClass);
		}

		ReportRequestEntryWeb theReportRequestEntryWeb = new ReportRequestEntryWeb();

		theReportRequestEntryWeb.setReportRequestId(reportRequestEntry.getReportRequestId());
//		theReportRequestEntryWeb.setReport( ModelTransformer.mapToReport( reportRequestEntry.getReport(), ReportWeb.class) );
//		theReportRequestEntryWeb.setUserRequested( ModelTransformer.map( reportRequestEntry.getUserRequested(), UserWeb.class) );

		theReportRequestEntryWeb.setUserName(reportRequestEntry.getUserRequested().getUsername());
		theReportRequestEntryWeb.setReportName(reportRequestEntry.getReport().getName());
		theReportRequestEntryWeb.setReportDescription(reportRequestEntry.getReport().getDescription());


		theReportRequestEntryWeb.setDateRequested(reportRequestEntry.getDateRequested());
		theReportRequestEntryWeb.setDateCompleted(reportRequestEntry.getDateCompleted());

		theReportRequestEntryWeb.setCompleted(reportRequestEntry.getCompleted());
		theReportRequestEntryWeb.setReportHandle(reportRequestEntry.getReportHandle());

		return theReportRequestEntryWeb;
	}

	// ParameterType mapping
	public static ParameterTypeWeb mapToParameterType(ParameterType parameterType, Class<ParameterTypeWeb> parameterTypeClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + parameterType.getClass() + " to type " + parameterTypeClass);
		}

		ParameterTypeWeb theParameterTypeWeb = new ParameterTypeWeb();
		theParameterTypeWeb.setName(parameterType.getName());
		theParameterTypeWeb.setDisplayName(parameterType.getDisplayName());

		if (parameterType.getDisplayType() == FormEntryDisplayType.CHECK_BOX) {
			theParameterTypeWeb.setDisplayType("CHECKBOX");
		} else if (parameterType.getDisplayType() == FormEntryDisplayType.TEXT_FIELD) {
			theParameterTypeWeb.setDisplayType("TEXTFIELD");
		} else if (parameterType.getDisplayType() == FormEntryDisplayType.DATE_FIELD) {
			theParameterTypeWeb.setDisplayType("DATEFIELD");
		} else if (parameterType.getDisplayType() == FormEntryDisplayType.DROP_DOWN) {
			theParameterTypeWeb.setDisplayType("DROPDOWN");
		} else if (parameterType.getDisplayType() == FormEntryDisplayType.RADIO) {
			theParameterTypeWeb.setDisplayType("RADIO");
		}
		return theParameterTypeWeb;
	}

	// auditEvent mapping
	public static AuditEventWeb mapAuditEvent(AuditEvent auditEvent, Class<AuditEventWeb> auditEventClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + auditEvent.getClass() + " to type " + auditEventClass);
		}

		AuditEventWeb auditEventWeb = new AuditEventWeb();

		AuditEventType type = auditEvent.getAuditEventType();
		AuditEventTypeWeb typeWeb = new AuditEventTypeWeb();
			typeWeb.setAuditEventTypeCd(type.getAuditEventTypeCd());
			typeWeb.setAuditEventTypeCode(type.getAuditEventTypeCode());
			typeWeb.setAuditEventTypeName(type.getAuditEventTypeName());
			typeWeb.setAuditEventTypeDescription(type.getAuditEventTypeDescription());

		auditEventWeb.setAuditEventType(typeWeb);
		auditEventWeb.setAuditEventDescription(auditEvent.getAuditEventDescription());
		auditEventWeb.setDateCreated(auditEvent.getDateCreated());

		if (auditEvent.getRefPerson() != null) {
			PersonWeb refPerson = mapToPerson(auditEvent.getRefPerson(), PersonWeb.class);
			auditEventWeb.setRefPerson(refPerson);
		}
		if (auditEvent.getAltRefPerson() != null) {
			PersonWeb altPerson = mapToPerson(auditEvent.getAltRefPerson(), PersonWeb.class);
			auditEventWeb.setAltRefPerson(altPerson);
		}

		if (auditEvent.getUserCreatedBy() != null) {
			UserWeb user = mapToUser(auditEvent.getUserCreatedBy(), UserWeb.class, false);
			auditEventWeb.setUserCreatedBy(user);
		}

		return auditEventWeb;
	}

	// auditEventEntry mapping
	public static AuditEventEntryWeb mapAuditEventEntry(AuditEventEntry auditEventEntry, Class<AuditEventEntryWeb> auditEventEntryClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + auditEventEntry.getClass() + " to type " + auditEventEntryClass);
		}

		AuditEventEntryWeb auditEventWeb = new AuditEventEntryWeb();

		AuditEventType type = auditEventEntry.getAuditEventType();
		AuditEventTypeWeb typeWeb = new AuditEventTypeWeb();
			typeWeb.setAuditEventTypeCd(type.getAuditEventTypeCd());
			typeWeb.setAuditEventTypeCode(type.getAuditEventTypeCode());
			typeWeb.setAuditEventTypeName(type.getAuditEventTypeName());
			typeWeb.setAuditEventTypeDescription(type.getAuditEventTypeDescription());

		auditEventWeb.setAuditEventType(typeWeb);
		auditEventWeb.setAuditEventDescription(auditEventEntry.getAuditEventDescription());
		auditEventWeb.setDateCreated(auditEventEntry.getDateCreated());


		auditEventWeb.setRefRecordId(auditEventEntry.getRefRecordId());
		auditEventWeb.setAltRefRecordId(auditEventEntry.getAltRefRecordId());

		if (auditEventEntry.getUserCreatedBy() != null) {
			UserWeb user = mapToUser(auditEventEntry.getUserCreatedBy(), UserWeb.class, false);
			auditEventWeb.setUserCreatedBy(user);
		}

		return auditEventWeb;
	}

	// entity mapping
	public static EntityWeb mapToEntity(Entity entity, Class<EntityWeb> entityClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + entity.getClass() + " to type " + entityClass);
		}

		EntityWeb entityWeb = new EntityWeb();

		entityWeb.setEntityVersionId(entity.getEntityVersionId());
		entityWeb.setEntityId(entity.getEntityId());
		entityWeb.setVersionId(entity.getVersionId());
		entityWeb.setName(entity.getName());
		entityWeb.setDisplayName(entity.getDisplayName());
		entityWeb.setDescription(entity.getDescription());
		entityWeb.setDateCreated(entity.getDateCreated());
		entityWeb.setSynchronousMatching(entity.getSynchronousMatching());

		if (entity.getUserCreatedBy() != null) {
			UserWeb user = mapToUser(entity.getUserCreatedBy(), UserWeb.class, false);
			entityWeb.setUserCreatedBy(user);
		}

		if (entity.getAttributes() != null && entity.getAttributes().size() > 0) {
			Set<EntityAttributeWeb> entityAttributeWebs = new java.util.HashSet<EntityAttributeWeb>(entity.getAttributes().size());
			for (EntityAttribute entityAttribute : entity.getAttributes()) {

				 // Attribute is Custom field
				 if (entityAttribute.getIsCustom()) {
					continue;
				 }
				 // org.openempi.webapp.client.model.EntityAttributeWeb entityAttributeWeb = ModelTransformer.map(entityAttribute, org.openempi.webapp.client.model.EntityAttributeWeb.class);
				 EntityAttributeWeb entityAttributeWeb = new EntityAttributeWeb();

				 entityAttributeWeb.setEntityAttributeId(entityAttribute.getEntityAttributeId());
				 entityAttributeWeb.setName(entityAttribute.getName());
				 entityAttributeWeb.setDisplayName(entityAttribute.getDisplayName());
				 entityAttributeWeb.setDescription(entityAttribute.getDescription());
				 entityAttributeWeb.setDisplayOrder(entityAttribute.getDisplayOrder());
				 entityAttributeWeb.setIndexed(entityAttribute.getIndexed());
	             entityAttributeWeb.setSearchable(entityAttribute.getSearchable());
	             entityAttributeWeb.setCaseInsensitive(entityAttribute.getCaseInsensitive());
				 entityAttributeWeb.setDateCreated(entityAttribute.getDateCreated());

				 EntityAttributeDatatypeWeb entityAttributeDatatypeWeb = ModelTransformer.map(entityAttribute.getDatatype(), org.openempi.webapp.client.model.EntityAttributeDatatypeWeb.class);
				 entityAttributeWeb.setDatatype(entityAttributeDatatypeWeb);

				 if (entityAttribute.getUserCreatedBy() != null) {
				    UserWeb user = mapToUser(entityAttribute.getUserCreatedBy(), UserWeb.class, false);
				    entityAttributeWeb.setUserCreatedBy(user);
				 }

				 entityAttributeWebs.add(entityAttributeWeb);
			}
			entityWeb.setAttributes(entityAttributeWebs);
		}

		return entityWeb;
	}

	public static EntityWeb setGroupInfoToEntity(List<EntityAttributeGroup> groups, EntityWeb entity) {

		// set groups to EntityWeb
		Set<EntityAttributeGroupWeb> attributeGroups = new HashSet<EntityAttributeGroupWeb>(groups.size());
		for (EntityAttributeGroup group : groups) {
			 EntityAttributeGroupWeb groupWeb = new EntityAttributeGroupWeb();
			 groupWeb.setEntityGroupId(group.getEntityAttributeGroupId());
			 groupWeb.setName(group.getName());
			 groupWeb.setDisplayName(group.getDisplayName());
			 groupWeb.setDisplayOrder(group.getDisplayOrder());

			 attributeGroups.add(groupWeb);
		}
		entity.setEntityAttributeGroups(attributeGroups);

		// set group info to EntityAttributeWeb
		Set<EntityAttributeWeb> attributesWeb = entity.getAttributes();
		if (attributesWeb != null) {
			for (EntityAttributeWeb attributeWeb : attributesWeb) {
				String name = attributeWeb.getName();
				for (EntityAttributeGroup group : groups) {
					if (group.findEntityAttributeByName(name) != null) {

						attributeWeb.setEntityAttributeGroup(entity.findEntityGroupByName(group.getName()));
					}
				}
			}
		}
		return entity;
	}

	public static Set<EntityAttributeValidationWeb> mapToEntityValidations(List<EntityAttributeValidation> validations) {

		Set<EntityAttributeValidationWeb> validationsWeb = new java.util.HashSet<EntityAttributeValidationWeb>(validations.size());
		for (EntityAttributeValidation validation : validations) {

			 EntityAttributeValidationWeb validationWeb = new EntityAttributeValidationWeb();
			 validationWeb.setEntityAttributeValidationId(validation.getEntityAttributeValidationId());
			 validationWeb.setValidationName(validation.getName());
			 validationWeb.setDisplayName(validation.getDisplayName());

			 Set<EntityAttributeValidationParameterWeb> avp = new HashSet<EntityAttributeValidationParameterWeb>();
			 for (EntityAttributeValidationParameter parameter : validation.getParameters()) {

				  EntityAttributeValidationParameterWeb paramWeb = new EntityAttributeValidationParameterWeb();
				  paramWeb.setEntityAttributeValidationParamId(parameter.getEntityAttributeValidationParamId());
				  paramWeb.setParameterName(parameter.getName());
				  paramWeb.setParameterValue(parameter.getValue());
				  avp.add(paramWeb);
			 }
			 validationWeb.setValidationParameters(avp);

			 validationsWeb.add(validationWeb);
		}

		return validationsWeb;
	}

	public static Entity mapToEntity(EntityWeb entityWeb, Class<Entity> entityClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + entityWeb.getClass() + " to type " + entityClass);
		}

		Entity entity = new Entity();

		entity.setEntityVersionId(entityWeb.getEntityVersionId());
		entity.setEntityId(entityWeb.getEntityId());
		entity.setVersionId(entityWeb.getVersionId());
		entity.setName(entityWeb.getName());
		entity.setDisplayName(entityWeb.getDisplayName());
		entity.setDescription(entityWeb.getDescription());
		entity.setDateCreated(entityWeb.getDateCreated());
		entity.setSynchronousMatching(entityWeb.getSynchronousMatching());

		if (entityWeb.getUserCreatedBy() != null) {
			User user = mapToUser(entityWeb.getUserCreatedBy(), User.class);
			entity.setUserCreatedBy(user);
		}

		if (entityWeb.getAttributes() != null && entityWeb.getAttributes().size() > 0) {
			for (EntityAttributeWeb entityAttributeWeb : entityWeb.getAttributes()) {
				 // org.openhie.openempi.model.EntityAttribute entityAttribute = ModelTransformer.map(entityAttributeWeb, org.openhie.openempi.model.EntityAttribute.class);
				 EntityAttribute entityAttribute = new EntityAttribute();

				 entityAttribute.setEntityAttributeId(entityAttributeWeb.getEntityAttributeId());
				 entityAttribute.setName(entityAttributeWeb.getName());
				 entityAttribute.setDisplayName(entityAttributeWeb.getDisplayName());
				 entityAttribute.setDescription(entityAttributeWeb.getDescription());
				 entityAttribute.setDisplayOrder(entityAttributeWeb.getDisplayOrder());
				 entityAttribute.setIndexed(entityAttributeWeb.getIndexed());
	             entityAttribute.setSearchable(entityAttributeWeb.getSearchable());
	             entityAttribute.setCaseInsensitive(entityAttributeWeb.getCaseInsensitive());
				 entityAttribute.setDateCreated(entityAttributeWeb.getDateCreated());
				 entityAttribute.setIsCustom(false);

				 EntityAttributeDatatype entityAttributeDatatype = ModelTransformer.map(entityAttributeWeb.getDatatype(), org.openhie.openempi.model.EntityAttributeDatatype.class);
				 entityAttribute.setDatatype(entityAttributeDatatype);

				 if (entityAttributeWeb.getUserCreatedBy() != null) {
					User user = mapToUser(entityAttributeWeb.getUserCreatedBy(), User.class);
					entityAttribute.setUserCreatedBy(user);
				 }

				 entity.addAttribute(entityAttribute);
			}
		}
		return entity;
	}

	public static List<EntityAttributeGroup> mapToEntityGroup(EntityWeb entityWeb, Entity newEntity, Class<EntityAttributeGroup> groupsClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + entityWeb.getClass() + " to list of type " + groupsClass);
		}

		List<EntityAttributeGroup> attributeGroups = new ArrayList<EntityAttributeGroup>(entityWeb.getEntityAttributeGroups().size());
		for (EntityAttributeGroupWeb attributeGroupWeb : entityWeb.getEntityAttributeGroups()) {

			 EntityAttributeGroup group = new EntityAttributeGroup();
			 group.setEntityAttributeGroupId(attributeGroupWeb.getEntityGroupId());
			 group.setName(attributeGroupWeb.getName());
			 group.setDisplayName(attributeGroupWeb.getDisplayName());
			 group.setDisplayOrder(attributeGroupWeb.getDisplayOrder());
			 group.setEntity(newEntity);

			 for (EntityAttributeWeb attributeWeb : entityWeb.getAttributes()) {
				  if (attributeWeb.getEntityAttributeGroup() != null && 
					  attributeWeb.getEntityAttributeGroup().getName().equals(group.getName())) {

					  EntityAttributeGroupAttribute groupAttribute = new EntityAttributeGroupAttribute();
					  groupAttribute.setEntityAttributeGroup(group);
					  groupAttribute.setEntityAttribute( newEntity.findAttributeByName(attributeWeb.getName()));

					  group.addEntityAttributeGroupAttribute(groupAttribute);
				  }
			 }
			 attributeGroups.add(group);
		}

		return attributeGroups;
	}

	public static List<EntityAttributeValidation> mapToEntityValidations(EntityAttributeWeb attributeWeb, Entity newEntity, Class<EntityAttributeValidation> validationsClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + attributeWeb.getClass() + " to list of type " + validationsClass);
		}

		List<EntityAttributeValidation> attributeValidations = new ArrayList<EntityAttributeValidation>(attributeWeb.getEntityAttributeValidations().size());
		EntityAttribute attribute = newEntity.findAttributeByName(attributeWeb.getName());

		for (EntityAttributeValidationWeb validationWeb : attributeWeb.getEntityAttributeValidations()) {

			 EntityAttributeValidation validation = new EntityAttributeValidation();
			 validation.setEntityAttributeValidationId(validationWeb.getEntityAttributeValidationId());
			 validation.setName(validationWeb.getValidationName());
			 validation.setDisplayName(validationWeb.getDisplayName());
			 validation.setEntityAttribute(attribute);

			 for (EntityAttributeValidationParameterWeb parameterWeb : validationWeb.getValidationParameters()) {

				  EntityAttributeValidationParameter param = new EntityAttributeValidationParameter();
				  param.setEntityAttributeValidationParamId(parameterWeb.getEntityAttributeValidationParamId());
				  param.setName(parameterWeb.getParameterName());
				  param.setValue(parameterWeb.getParameterValue());
				  param.setEntityAttributeValidation(validation);
				  validation.addParameter(param);
			 }
			 attributeValidations.add(validation);
		}
		return attributeValidations;
	}

	// Record mapping
	public static Record mapToRecord(Entity entityDef, RecordWeb recordWeb, Class<Record> recordClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + recordWeb.getClass() + " to type " + recordClass);
		}

		Record theRecord = new Record(entityDef);
		theRecord.setRecordId(recordWeb.getRecordId());

		if (recordWeb.getProperties() != null) {

			// mapping the cluster ID
			theRecord.set(Constants.RECORD_ID_PROPERTY, recordWeb.getRecordId());
			theRecord.set(Constants.ORIENTDB_CLUSTER_ID_KEY, recordWeb.get(Constants.ORIENTDB_CLUSTER_ID_KEY));

			// mapping the attributes
			for (EntityAttribute attrib : entityDef.getAttributes()) {
				String key = attrib.getName();
				Object obj = recordWeb.get(key);
				theRecord.set(key, obj);
			}
		}

		// mapping the identifiers
		if (recordWeb.getIdentifiers() != null && recordWeb.getIdentifiers().size() > 0) {
			for (IdentifierWeb identifierWeb : recordWeb.getIdentifiers()) {
				org.openhie.openempi.model.Identifier identifier = ModelTransformer.map(identifierWeb, org.openhie.openempi.model.Identifier.class);
				identifier.setRecord(theRecord);
				theRecord.addIdentifier(identifier);
			}
		}
		return theRecord;
	}

	public static Record mapToRecordForSearch(Entity entityDef, RecordWeb recordWeb, Class<Record> recordClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + recordWeb.getClass() + " to type " + recordClass);
		}

		Record theRecord = new Record(entityDef);
		theRecord.setRecordId(recordWeb.getRecordId());

		if (recordWeb.getProperties() != null) {
			for (EntityAttribute attrib : entityDef.getAttributes()) {
				String key = attrib.getName();
				Object obj = recordWeb.get(key);
				if (obj instanceof Date) {
					switch(AttributeDatatype.getById(attrib.getDatatype().getDatatypeCd())) {
					case DATE:
						theRecord.set(key, DateToString((Date) obj));
						break;
					case TIMESTAMP:
						theRecord.set(key, DateTimeToString((Date) obj));
						break;
					}
				} else {
					theRecord.set(key, obj);
				}
			}
		}
		return theRecord;
	}

	public static RecordWeb mapToRecord(Record record, Class<RecordWeb> recordClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + record.getClass() + " to type " + recordClass);
		}

		RecordWeb theRecord = new RecordWeb();
		theRecord.setRecordId(record.getRecordId());

		HashMap<String, Object> map  = new java.util.HashMap<String, Object>();
		if (record.getPropertyNames() != null) {
			for (String attributeName : record.getPropertyNames()) {
			    Object value = record.get(attributeName);
				theRecord.set(attributeName, value);
			}
		}

		// mapping the identifiers
		if (record.getIdentifiers() != null && record.getIdentifiers().size() > 0) {
			Set<IdentifierWeb> identifiers = new java.util.HashSet<IdentifierWeb>(record.getIdentifiers().size());
			for (org.openhie.openempi.model.Identifier identifier : record.getIdentifiers()) {
				identifiers.add(ModelTransformer.map(identifier, IdentifierWeb.class));
			}
			theRecord.setIdentifiers(identifiers);
		}

		return theRecord;
	}

	public static RecordLinkWeb mapToRecordLink(RecordLink recordLink, Class<RecordLinkWeb> recordClass, boolean withRecords ) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + recordLink.getClass() + " to type " + recordClass);
		}

		RecordLinkWeb theRecord = new RecordLinkWeb();
		theRecord.setRecordLinkId(recordLink.getRecordLinkId());

		if (withRecords) {
			theRecord.setLeftRecord(ModelTransformer.mapToRecord(recordLink.getLeftRecord(), RecordWeb.class));
			theRecord.setRightRecord(ModelTransformer.mapToRecord(recordLink.getRightRecord(), RecordWeb.class));
		}

		if (recordLink.getUserCreatedBy() != null) {
			theRecord.setUserCreatedBy(mapToUser(recordLink.getUserCreatedBy(), UserWeb.class, false));
		}

		if (recordLink.getLinkSource() != null) {
			LinkSourceWeb linkSource = ModelTransformer.map(recordLink.getLinkSource(), LinkSourceWeb.class);
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
			theRecord.setLinkSource(linkSource);
		}

		theRecord.setDateCreated(recordLink.getDateCreated());
		theRecord.setWeight(recordLink.getWeight());
//		theRecord.setState(recordLink.getState().toString() );
		switch (recordLink.getState().toString().charAt(0)) {
		case 'M':
			theRecord.setState("Match");
			break;
		case 'N':
			theRecord.setState("Non-Match");
			break;
		case 'P':
			theRecord.setState("Probable Match");
			break;
		}
		theRecord.setVector(recordLink.getVector());

		return theRecord;
	}

	public static RecordLink mapToRecordLink(RecordLinkWeb recordLink, Class<RecordLink> recordClass ) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + recordLink.getClass() + " to type " + recordClass);
		}

		RecordLink theRecord = new RecordLink();
		theRecord.setRecordLinkId(recordLink.getRecordLinkId());

		theRecord.setDateCreated(recordLink.getDateCreated());
		theRecord.setWeight(recordLink.getWeight());
		theRecord.setVector(recordLink.getVector());

		switch(recordLink.getState().charAt(0)) {
		case 'M':
			theRecord.setState(RecordLinkState.MATCH);
			break;
		case 'N':
			theRecord.setState(RecordLinkState.NON_MATCH);
			break;
		case 'P':
			theRecord.setState(RecordLinkState.POSSIBLE_MATCH);
			break;
		}

		return theRecord;
	}

	// messageLog mapping
	public static MessageLogEntryWeb mapMessage(MessageLogEntry messageLog, Class<MessageLogEntryWeb> messageLogClass) {
		if (log.isDebugEnabled()) {
			log.debug("Transforming object of type " + messageLog.getClass() + " to type " + messageLogClass);
		}

		MessageLogEntryWeb messageLogWeb = new MessageLogEntryWeb();
		MessageTypeWeb outgoingTypeWeb = null;
		MessageTypeWeb incomingTypeWeb = null;

		messageLogWeb.setMessageLogId(messageLog.getMessageLogId());

		MessageType type = messageLog.getOutgoingMessageType();
		if (type != null) {
			outgoingTypeWeb = new MessageTypeWeb();
				outgoingTypeWeb.setMessageTypeCd(type.getMessageTypeCd());
				outgoingTypeWeb.setMessageTypeCode(type.getMessageTypeCode());
				outgoingTypeWeb.setMessageTypeName(type.getMessageTypeName());
				outgoingTypeWeb.setMessageTypeDescription(type.getMessageTypeDescription());
		}
		type = messageLog.getIncomingMessageType();
		if (type != null) {
			incomingTypeWeb = new MessageTypeWeb();
				incomingTypeWeb.setMessageTypeCd(type.getMessageTypeCd());
				incomingTypeWeb.setMessageTypeCode(type.getMessageTypeCode());
				incomingTypeWeb.setMessageTypeName(type.getMessageTypeName());
				incomingTypeWeb.setMessageTypeDescription(type.getMessageTypeDescription());
		}
		messageLogWeb.setIncomingMessage(messageLog.getIncomingMessage());
		messageLogWeb.setOutgoingMessage(messageLog.getOutgoingMessage());
		messageLogWeb.setIncomingMessageType(incomingTypeWeb);
		messageLogWeb.setOutgoingMessageType(outgoingTypeWeb);
		messageLogWeb.setDateReceived(messageLog.getDateReceived());

		return messageLogWeb;
	}

    // recordPair mapping
    public static RecordPairWeb mapRecordPair(RecordPair recordPair, Class<RecordPairWeb> recordPairClass) {
        if (log.isDebugEnabled()) {
            log.debug("Transforming object of type " + recordPair.getClass() + " to type " + recordPairClass);
        }

        RecordPairWeb recordPairWeb = new RecordPairWeb();

        recordPairWeb.setWeight(recordPair.getWeight());
        recordPairWeb.setMatchOutcome(recordPair.getMatchOutcome());
        if (recordPairWeb.getLinkSource() != null) {
            recordPairWeb.setLinkSource(ModelTransformer.map(recordPair.getLinkSource(), LinkSourceWeb.class));
        }
        recordPairWeb.setLeftRecord(mapToRecord(recordPair.getLeftRecord(), RecordWeb.class));
        recordPairWeb.setRightRecord(mapToRecord(recordPair.getRightRecord(), RecordWeb.class));
        return recordPairWeb;
    }

    //  DataProfile mapping
    public static DataProfileWeb mapToDataProfile(DataProfile dataProfile, Class<DataProfileWeb> dataProfileClass) {
        if (log.isDebugEnabled()) {
            // log.debug("Transforming object of type " + dataProfile.getClass() + " to type " + dataProfileClass);
        }

        DataProfileWeb dataProfileWeb = new DataProfileWeb();

        dataProfileWeb.setDataProfileId(dataProfile.getDataProfileId());
        dataProfileWeb.setDataSourceId(dataProfile.getDataSourceId());
        dataProfileWeb.setDateInitiated(dataProfile.getDateInitiated());
        dataProfileWeb.setDateCompleted(dataProfile.getDateCompleted());
        if (dataProfile.getEntity() != null) {
            dataProfileWeb.setEntity(mapToEntity(dataProfile.getEntity(), EntityWeb.class));
        }

        return dataProfileWeb;
    }

    public static DataProfile mapToDataProfile(DataProfileWeb dataProfileWeb, Class<DataProfile> dataProfileClass) {
        if (log.isDebugEnabled()) {
            // log.debug("Transforming object of type " + dataProfileWeb.getClass() + " to type " + dataProfileClass);
        }

        DataProfile dataProfile = new DataProfile();

        dataProfile.setDataProfileId(dataProfileWeb.getDataProfileId());
        dataProfile.setDataSourceId(dataProfileWeb.getDataSourceId());
        dataProfile.setDateInitiated(dataProfileWeb.getDateInitiated());
        dataProfile.setDateCompleted(dataProfileWeb.getDateCompleted());
        if (dataProfileWeb.getEntity() != null) {
            dataProfile.setEntity(mapToEntity(dataProfileWeb.getEntity(), Entity.class));
        }

        return dataProfile;
    }

    // job entry mapping
    public static JobEntry mapToJobEntry(JobEntryWeb entryWeb, Class<JobEntry> entryClass) {
        if (log.isDebugEnabled()) {
            log.debug("Transforming object of type " + entryWeb.getClass() + " to type " + entryClass);
        }

        JobEntry entry = new JobEntry();

        entry.setJobEntryId(entryWeb.getJobEntryId());
        entry.setJobDescription(entryWeb.getJobDescription());
        entry.setDateCreated(entryWeb.getDateCreated());
        entry.setDateStarted(entryWeb.getDateStarted());
        entry.setDateCompleted(entryWeb.getDateCompleted());
        entry.setItemsProcessed(entryWeb.getItemsProcessed());
        entry.setItemsSuccessful(entryWeb.getItemsSuccessful());
        entry.setItemsErrored(entryWeb.getItemsErrored());

        JobTypeWeb typeWeb = entryWeb.getJobType();
        JobType type = new JobType();
        type.setJobTypeCd(typeWeb.getJobTypeCd());
        type.setJobTypeHandler(typeWeb.getJobTypeHandler());
        type.setJobTypeName(typeWeb.getJobTypeName());
        type.setJobTypeDescription(typeWeb.getJobTypeDescription());

        JobStatusWeb statusWeb = entryWeb.getJobStatus();
        JobStatus status = new JobStatus();
        status.setJobStatusCd(statusWeb.getJobStatusCd());
        status.setJobStatusName(statusWeb.getJobStatusName());
        status.setJobStatusDescription(statusWeb.getJobStatusDescription());

        entry.setJobType(type);
        entry.setJobStatus(status);
        return entry;
    }

    // job entry mapping
    public static JobEntryWeb mapToJobEntry(JobEntry entry, Class<JobEntryWeb> entryClass) {
        if (log.isDebugEnabled()) {
            log.debug("Transforming object of type " + entry.getClass() + " to type " + entryClass);
        }

        JobEntryWeb entryWeb = new JobEntryWeb();

        entryWeb.setJobEntryId(entry.getJobEntryId());
        entryWeb.setJobDescription(entry.getJobDescription());
        entryWeb.setDateCreated(entry.getDateCreated());
        entryWeb.setDateStarted(entry.getDateStarted());
        entryWeb.setDateCompleted(entry.getDateCompleted());
        entryWeb.setItemsProcessed(entry.getItemsProcessed());
        entryWeb.setItemsSuccessful(entry.getItemsSuccessful());
        entryWeb.setItemsErrored(entry.getItemsErrored());

        JobType type = entry.getJobType();
        JobTypeWeb typeWeb = new JobTypeWeb();
            typeWeb.setJobTypeCd(type.getJobTypeCd());
            typeWeb.setJobTypeHandler(type.getJobTypeHandler());
            typeWeb.setJobTypeName(type.getJobTypeName());
            typeWeb.setJobTypeDescription(type.getJobTypeDescription());

        JobStatus status = entry.getJobStatus();
        JobStatusWeb statusWeb = new JobStatusWeb();
            statusWeb.setJobStatusCd(status.getJobStatusCd());
            statusWeb.setJobStatusName(status.getJobStatusName());
            statusWeb.setJobStatusDescription(status.getJobStatusDescription());

        entryWeb.setJobType(typeWeb);
        entryWeb.setJobStatus(statusWeb);
        return entryWeb;
    }

    // job entry event log mapping
    public static JobEntryEventLogWeb mapToJobEntryEventLog(JobEntryEventLog eventLog, Class<JobEntryEventLogWeb> entryClass) {
        if (log.isDebugEnabled()) {
            log.debug("Transforming object of type " + eventLog.getClass() + " to type " + entryClass);
        }

        JobEntryEventLogWeb jobEntryEventLogWeb = new JobEntryEventLogWeb();

        jobEntryEventLogWeb.setEventEntryEventLogId(eventLog.getEventEntryEventLogId());
        jobEntryEventLogWeb.setLogMessage(eventLog.getLogMessage());
        jobEntryEventLogWeb.setDateCreated(eventLog.getDateCreated());

        return jobEntryEventLogWeb;
    }

	public static String DateToString(Date date)
	{
		if (date == null) {
			return "";
		}

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = df.format(date);

		// System.out.println("Report Date: " + strDate);
		return strDate;
	}

	public static Date StringToDate(String strDate) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		if (strDate != null && !strDate.isEmpty()) {
			try {
				date = df.parse(strDate);
				// System.out.println("Today = " + df.format(date));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return date;
	}

	public static String DateTimeToString(Date date) {
		if (date == null) {
			return "";
		}

//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = df.format(date);

		// System.out.println("Report Date: " + strDate);
		return strDate;
	}

	public static Date StringToDateTime(String strDate) {
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		if (strDate != null && !strDate.isEmpty()) {
			try {
				date = df.parse(strDate);
				// System.out.println("Today = " + df.format(date));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return date;
	}
}
