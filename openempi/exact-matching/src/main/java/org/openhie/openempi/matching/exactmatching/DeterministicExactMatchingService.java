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
package org.openhie.openempi.matching.exactmatching;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.Constants;
import org.openhie.openempi.InitializationException;
import org.openhie.openempi.configuration.ComparatorFunction;
import org.openhie.openempi.configuration.ConfigurationRegistry;
import org.openhie.openempi.configuration.MatchField;
import org.openhie.openempi.configuration.MatchRule;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.matching.AbstractMatchingLifecycleObserver;
import org.openhie.openempi.matching.MatchingService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordPair;
import org.openhie.openempi.stringcomparison.StringComparisonService;

public class DeterministicExactMatchingService extends AbstractMatchingLifecycleObserver implements MatchingService
{
    private Map<String,List<MatchRule>> matchRulesByEntityName = new HashMap<String,List<MatchRule>>();
	private StringComparisonService comparisonService;

	public void startup() throws InitializationException {
	    List<Entity> entities = Context.getEntityDefinitionManagerService().loadEntities();
	    for (Entity entity : entities) {
	        MatchingService service = Context.getMatchingService(entity.getName());
	        if (service.getMatchingServiceId() != getMatchingServiceId()) {
	            continue;
	        }

            @SuppressWarnings("unchecked")
            Map<String,Object> configurationData = (Map<String,Object>) Context.getConfiguration()
                    .lookupConfigurationEntry(entity.getName(), ConfigurationRegistry.MATCH_CONFIGURATION);
            Object obj = configurationData.get(ExactMatchingConstants.EXACT_MATCHING_RULES_REGISTRY_KEY);
        	if (obj == null) {
        		log.error("Deterministic exact matching service has not been configured properly; no match rules have been defined.");
        		throw new RuntimeException("Deterministic exact maching service has not been configured properly.");
        	}

        	@SuppressWarnings("unchecked")
        	List<MatchRule> rules = (List<MatchRule>) obj;
            matchRulesByEntityName.put(entity.getName(), rules);
            comparisonService = Context.getStringComparisonService();
        	log.info("Loaded Exact Matching configuration for entity " + entity.getName());
	    }
	}

	public int getMatchingServiceId() {
		return LinkSource.EXACT_MATCHING_ALGORITHM_SOURCE;
	}

	public Set<RecordPair> match(Record record) {
		log.debug("Looking for matches on record " + record);
		List<RecordPair> candidates = Context.getBlockingService(record.getEntity().getName()).findCandidates(record);
		Set<RecordPair> matches = new java.util.HashSet<RecordPair>();
		for (RecordPair entry : candidates) {
			// No need to compare a record pair that consists of two references to the same record.
			if (entry.getLeftRecord().getRecordId() != null &&
			        entry.getLeftRecord().getRecordId().longValue() ==
			        entry.getRightRecord().getRecordId().longValue()) {
				continue;
			}
			log.debug("Potential matching record pair found: " + entry);

	        String entityName = record.getEntity().getName();
	        final List<MatchRule> rules = matchRulesByEntityName.get(entityName);
	        if (rules == null) {
	            log.error("There is no match fule configuration for entity " + entityName);
	            throw new RuntimeException("No match rules have been configured for entity " + entityName);
	        }

	        for (MatchRule rule : rules) {
	            List<MatchField> matchFieldList = rule.getFields();
	            boolean overallMatch = true;
	            for (MatchField matchField : matchFieldList) {
	                boolean fieldMatch = isExactMatch(matchField, entry.getLeftRecord(), entry.getRightRecord());
	                if (log.isDebugEnabled()) {
	                    log.debug("Comparison of records on field " + matchField + " returned " + fieldMatch);
	                }
	                overallMatch &= fieldMatch;
    				if (!fieldMatch) {
    				    break;
    				}
	            }
	            if (overallMatch) {
	                if (log.isDebugEnabled()) {
	                    log.debug("Matched records using rule " + rule.toStringShort() + " adding to matches entry: " + entry);
	                }
                    entry.setWeight(1.0);
                    entry.setMatchOutcome(RecordPair.MATCH_OUTCOME_LINKED);
                    entry.setLinkSource(new LinkSource(getMatchingServiceId()));
                    entry.setVector((int) Math.pow(2.0, matchFieldList.size()) - 1);
                    matches.add(entry);
	            }
			}
		}
		return matches;
	}

	public RecordPair match(RecordPair recordPair) {
		if (log.isTraceEnabled()) {
			log.trace("Looking for matches on record pair " + recordPair);
		}
		if (recordPair == null || recordPair.getLeftRecord() == null || recordPair.getRightRecord() == null) {
			return recordPair;
		}

		recordPair.setLinkSource(new LinkSource(getMatchingServiceId()));

		// No need to compare a record pair that consists of two references to the same record.
		if (recordPair.getLeftRecord().getRecordId() != null && 
				recordPair.getLeftRecord().getRecordId().longValue() == recordPair.getRightRecord().getRecordId().longValue()) {
				recordPair.setMatchOutcome(RecordPair.MATCH_OUTCOME_LINKED);
				return recordPair;
		}
		
		String entityName = recordPair.getLeftRecord().getEntity().getName();
		final List<MatchRule> rules = matchRulesByEntityName.get(entityName);
        if (rules == null) {
            log.error("There is no match fule configuration for entity " + entityName);
            throw new RuntimeException("No match rules have been configured for entity " + entityName);
        }
        
        boolean ruleMatch = true;
		for (MatchRule rule : rules) {
	        List<MatchField> matchFieldList = rule.getFields();
    		for (MatchField matchField : matchFieldList) {
    			boolean fieldsMatch = isExactMatch(matchField, recordPair.getLeftRecord(), recordPair.getRightRecord());
    			if (log.isTraceEnabled()) {
    				log.trace("Comparison of records on field " + matchField + " returned " + fieldsMatch);
    			}
    			if (!fieldsMatch) {
    			    ruleMatch = false;
    			    break;
    			}
    		}
    		if (ruleMatch) {
    		    if (log.isDebugEnabled()) {
    		        log.debug("Record pair should create a link: " + recordPair + " based on rule " + rule);
    		    }
                recordPair.setWeight(1.0);
                recordPair.setMatchOutcome(RecordPair.MATCH_OUTCOME_LINKED);
                recordPair.setVector((int) Math.pow(2.0, matchFieldList.size()) - 1);
                return recordPair;  
    		}
    		ruleMatch = true;
    	}
		if (log.isTraceEnabled()) {
		    log.trace("Record pair should not create a link: " + recordPair);
		}
		recordPair.setWeight(0.0);
		recordPair.setMatchOutcome(RecordPair.MATCH_OUTCOME_UNLINKED);
		// TODO: We need to fix this; it should calculate the vector even if it is not a match
		recordPair.setVector(0);
		return recordPair;
	}

	private boolean isExactMatch(MatchField matchField, Record left, Record right) {
		String lVal = left.getAsString(matchField.getFieldName());
		String rVal = right.getAsString(matchField.getFieldName());
		if (matchField.getComparatorFunction() == null) {
		    matchField.setComparatorFunction(new ComparatorFunction(Constants.EXACT_COMPARATOR_FUNCTION));
		}
		String functionName = matchField.getComparatorFunction().getFunctionName();
		double distance = comparisonService.score(functionName, lVal, rVal);
		if (distance >= matchField.getMatchThreshold()) {
	        if (log.isTraceEnabled()) {
	            log.trace("Distance between values " + lVal + " and " + rVal + " computed using comparison function " + 
	                    functionName + " was found to be " + distance + " as compared to threshold " + 
	                    matchField.getMatchThreshold());
	        }
			return true;
		}
		return false;
	}

	public Set<String> getMatchFields(String entityName) {
        Set<String> matchFields = new HashSet<String>();
        @SuppressWarnings("unchecked")
        Map<String,Object> config = (Map<String,Object>) Context.getConfiguration()
                .lookupConfigurationEntry(entityName, ConfigurationRegistry.MATCH_CONFIGURATION);
        if (config == null) {
            log.error("Deterministic exact matching service has not been configured properly; "
                    + "no match rules have been defined.");
            return matchFields;
        }
        @SuppressWarnings("unchecked")
        List<MatchRule> rules = (List<MatchRule>) config.get(ExactMatchingConstants.EXACT_MATCHING_RULES_REGISTRY_KEY);
        if (rules == null) {
            log.error("Deterministic exact matching service has not been configured properly; "
                    + "no match rules have been defined.");
            return matchFields;
        }
        for (MatchRule rule : rules) {
            for (MatchField field : rule.getFields()) {
                matchFields.add(field.getFieldName());
            }
        }
		return matchFields;
	}

	public void shutdown() {
		log.info("Shutting down deterministic matching service.");
	}

	public void initializeRepository(String entityName) throws ApplicationException {
        log.info("The deterministic matching service finished initializing the repository for entity " + entityName);
	}
}
