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
package org.openempi.webapp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.openempi.webapp.client.ReferenceDataService;
import org.openempi.webapp.client.model.IdentifierDomainTypeCodeWeb;
import org.openempi.webapp.client.model.IdentifierDomainWeb;
import org.openempi.webapp.client.model.SystemConfigurationWeb;
import org.openempi.webapp.client.model.AuditEventTypeWeb;
import org.openempi.webapp.client.model.EntityAttributeDatatypeWeb;
import org.openempi.webapp.client.model.EntityValidationRuleWeb;
import org.openempi.webapp.client.model.JobTypeWeb;
import org.openempi.webapp.client.model.JobStatusWeb;
import org.openhie.openempi.jobqueue.JobQueueService;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.AuditEventType;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.JobStatus;
import org.openhie.openempi.model.JobType;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.service.IdentifierDomainService;
import org.openhie.openempi.service.PersonQueryService;
import org.openhie.openempi.service.AuditEventService;
import org.openhie.openempi.configuration.ConfigurationRegistry;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.stringcomparison.StringComparisonService;
import org.openhie.openempi.transformation.TransformationService;
import org.openhie.openempi.validation.EntityValidationService;
import org.openhie.openempi.validation.ValidationRule;

public class ReferenceDataServiceImpl extends AbstractRemoteServiceServlet implements ReferenceDataService
{
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

    public IdentifierDomainWeb getGlobalIdentifierDomain() {

        authenticateCaller();
        try {
            IdentifierDomain globalIdentifierDomain = Context.getConfiguration().getGlobalIdentifierDomain();
            if (globalIdentifierDomain != null) {
                IdentifierDomainWeb domainWeb = new IdentifierDomainWeb(globalIdentifierDomain.getIdentifierDomainId(),
                        globalIdentifierDomain.getIdentifierDomainName(), globalIdentifierDomain.getIdentifierDomainDescription(),
                        globalIdentifierDomain.getNamespaceIdentifier(), globalIdentifierDomain.getUniversalIdentifier(),
                        globalIdentifierDomain.getUniversalIdentifierTypeCode());
                return domainWeb;
            } else {
                return null;
            }
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

	public List<IdentifierDomainWeb> getIdentifierDomains() {
		log.debug("Received request to retrieve the list of identifier domains.");

		authenticateCaller();
		try {
			IdentifierDomainService identifierDomainService = Context.getIdentifierDomainService();
			List<IdentifierDomain> domains = identifierDomainService.getIdentifierDomains();
			List<IdentifierDomainWeb> domainsWeb = new ArrayList<IdentifierDomainWeb>(domains.size());
			for (IdentifierDomain domain : domains) {
				domainsWeb.add( new IdentifierDomainWeb(domain.getIdentifierDomainId(),
						domain.getIdentifierDomainName(), domain.getIdentifierDomainDescription(),
						domain.getNamespaceIdentifier(), domain.getUniversalIdentifier(),
						domain.getUniversalIdentifierTypeCode()));
			}
			return domainsWeb;
		} catch (Throwable t) {
			log.error("Failed to execute: " + t.getMessage(), t);
			throw new RuntimeException(t);
		}
	}

	public List<IdentifierDomainTypeCodeWeb> getIdentifierDomainTypeCodes() {
		log.debug("Received request to retrieve the list of identifier domain type codes.");

		authenticateCaller();
		try {
			IdentifierDomainService identifierDomainService = Context.getIdentifierDomainService();
			List<String> types = identifierDomainService.getIdentifierDomainTypeCodes();
			List<IdentifierDomainTypeCodeWeb> codes = new ArrayList<IdentifierDomainTypeCodeWeb>(types.size());
			for (String type : types) {
				codes.add(new IdentifierDomainTypeCodeWeb(type));
			}
			return codes;
		} catch (Throwable t) {
			log.error("Failed to execute: " + t.getMessage(), t);
			throw new RuntimeException(t);
		}
	}

	public List<AuditEventTypeWeb> getAuditEventTypeCodes() {
		log.debug("Received request to retrieve the list of audit event type codes.");

		authenticateCaller();
		try {
			AuditEventService auditEventService = Context.getAuditEventService();
			List<AuditEventType> types = auditEventService.getAllAuditEventTypes();
			List<AuditEventTypeWeb> codes = new ArrayList<AuditEventTypeWeb>(types.size());
			if (types != null) {
				for (AuditEventType type : types) {
					codes.add( new AuditEventTypeWeb(type.getAuditEventTypeCd(), type.getAuditEventTypeName(),type.getAuditEventTypeDescription(), type.getAuditEventTypeCode()));			
				}
			}
			return codes;
		} catch (Throwable t) {
			log.error("Failed to execute: " + t.getMessage(), t);
			throw new RuntimeException(t);
		}
	}

	public List<String> getPersonModelAllAttributeNames() {
		log.debug("Received request to retrieve the list of person model all attribute names.");

		authenticateCaller();
		try {
			PersonQueryService personQueryService = Context.getPersonQueryService();
			List<String> attributeNames = personQueryService.getPersonModelAllAttributeNames();
			return attributeNames;
		} catch (Throwable t) {
			log.error("Failed to execute: " + t.getMessage(), t);
			throw new RuntimeException(t);
		}
	}

	public List<String> getPersonModelAttributeNames() {
		log.debug("Received request to retrieve the list of person model attribute names.");

		authenticateCaller();
		try {
			PersonQueryService personQueryService = Context.getPersonQueryService();
			List<String> attributeNames = personQueryService.getPersonModelAttributeNames();
			return attributeNames;
		} catch (Throwable t) {
			log.error("Failed to execute: " + t.getMessage(), t);
			throw new RuntimeException(t);
		}
	}

	public List<String> getPersonModelCustomFieldNames() {
		log.debug("Received request to retrieve the list of person model custom field names.");

		authenticateCaller();
		try {
			PersonQueryService personQueryService = Context.getPersonQueryService();
			List<String> customFieldNames = personQueryService.getPersonModelCustomAttributeNames();
			return customFieldNames;
		} catch (Throwable t) {
			log.error("Failed to execute: " + t.getMessage(), t);
			throw new RuntimeException(t);
		}
	}

	public List<String> getTransformationFunctionNames() {
		log.debug("Received request to retrieve the list of transformation function names.");

		authenticateCaller();
		try {
			TransformationService transformationService = Context.getTransformationService();
			List<String> transformationFunctionNames = transformationService.getTransformationFunctionNames();
			return transformationFunctionNames;
		} catch (Throwable t) {
			log.error("Failed to execute: " + t.getMessage(), t);
			throw new RuntimeException(t);
		}
	}

	public List<String> getComparatorFunctionNames() {
		log.debug("Received request to retrieve the list of comparator function names.");

		authenticateCaller();
		try {
			StringComparisonService stringComparisonService = Context.getStringComparisonService();
			List<String> comparisonFunctionNames = stringComparisonService.getComparisonFunctionNames();
			return comparisonFunctionNames;
		} catch (Throwable t) {
			log.error("Failed to execute: " + t.getMessage(), t);
			throw new RuntimeException(t);
		}
	}

	public SystemConfigurationWeb getSystemConfigurationInfo(String entityName) {
		log.debug("Received request to retrieve the system configuration information.");
		SystemConfigurationWeb systemConfig = new SystemConfigurationWeb();
		String blockingAlgorithmName = (String) Context.getConfiguration()
				.lookupConfigurationEntry(entityName, ConfigurationRegistry.BLOCKING_ALGORITHM_NAME_KEY);
		log.info("The system is configured with blocking algorithm: " + blockingAlgorithmName);
		String matchingAlgorithmName = (String) Context.getConfiguration()
				.lookupConfigurationEntry(entityName, ConfigurationRegistry.MATCHING_ALGORITHM_NAME_KEY);
		log.info("The system is configured with matching algorithm: " + matchingAlgorithmName);
		systemConfig.setBlockingAlgorithmName(blockingAlgorithmName);
		systemConfig.setMatchingAlgorithmName(matchingAlgorithmName);
		return systemConfig;
	}

	public List<EntityAttributeDatatypeWeb> getEntityAttributeDatatypes() {
		log.debug("Received request to retrieve the list of entity attribute data types.");

		authenticateCaller();
		try {
			EntityDefinitionManagerService entityManagerService = Context.getEntityDefinitionManagerService();
			List<EntityAttributeDatatype> datatypes = entityManagerService.getEntityAttributeDatatypes();
			List<EntityAttributeDatatypeWeb> typesWeb = new ArrayList<EntityAttributeDatatypeWeb>(datatypes.size());
			for (EntityAttributeDatatype type : datatypes) {
				typesWeb.add( new EntityAttributeDatatypeWeb(type.getDatatypeCd(),type.getName(), type.getDisplayName()));
			}
			return typesWeb;
		} catch (Throwable t) {
			log.error("Failed to execute: " + t.getMessage(), t);
			throw new RuntimeException(t);
		}
	}

	public List<EntityValidationRuleWeb> getValidationRules() {
		log.debug("Received request to retrieve the list of Validation Rules.");

		authenticateCaller();
		try {
			EntityValidationService entityValidationService = Context.getEntityValidationService();
			Set<ValidationRule> validationRules = entityValidationService.getValidationRules();
			List<EntityValidationRuleWeb> rulesWeb = new ArrayList<EntityValidationRuleWeb>(validationRules.size());
			for (ValidationRule rule : validationRules) {

				EntityValidationRuleWeb validationRule = new EntityValidationRuleWeb();
				validationRule.setValidationRuleName(rule.getValidationRuleName());
				validationRule.setValidationRuleDisplayName(rule.getValidationRuleDisplayName());
				validationRule.setParameters(rule.getParameters());

				rulesWeb.add(validationRule);
			}
			return rulesWeb;
		} catch (Throwable t) {
			log.error("Failed to execute: " + t.getMessage(), t);
			throw new RuntimeException(t);
		}
	}

    public List<JobTypeWeb> getJobTypes() {
        log.debug("Received request to retrieve the list of Job Types.");

        authenticateCaller();
        try {
            JobQueueService jobQueueService = Context.getJobQueueService();

            List<JobType> jobtypes = jobQueueService.getJobTypes();
            List<JobTypeWeb> jobTypesWeb = new ArrayList<JobTypeWeb>(jobtypes.size());
            for (JobType type : jobtypes) {
                jobTypesWeb.add( new JobTypeWeb(type.getJobTypeCd(),type.getJobTypeName(), type.getJobTypeDescription(), type.getJobTypeHandler()));
            }
            return jobTypesWeb;
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    public List<JobStatusWeb> getJobStatuses() {
        log.debug("Received request to retrieve the list of Job Statuses.");

        authenticateCaller();
        try {
            JobQueueService jobQueueService = Context.getJobQueueService();

            List<JobStatus> jobStatuses = jobQueueService.getJobStatuses();
            List<JobStatusWeb> jobStatusesWeb = new ArrayList<JobStatusWeb>(jobStatuses.size());
            for (JobStatus status : jobStatuses) {
                jobStatusesWeb.add( new JobStatusWeb(status.getJobStatusCd(),status.getJobStatusName(), status.getJobStatusDescription()));
            }
            return jobStatusesWeb;
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }
}
