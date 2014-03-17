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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhie.openempi.InitializationException;
import org.openhie.openempi.configuration.ComparatorFunction;
import org.openhie.openempi.configuration.Component.ComponentType;
import org.openhie.openempi.configuration.ConfigurationLoader;
import org.openhie.openempi.configuration.ConfigurationRegistry;
import org.openhie.openempi.configuration.MatchField;
import org.openhie.openempi.configuration.MatchRule;
import org.openhie.openempi.configuration.xml.MatchingConfigurationType;
import org.openhie.openempi.configuration.xml.MpiConfigDocument.MpiConfig;
import org.openhie.openempi.configuration.xml.exactmatching.ExactMatchingType;
import org.openhie.openempi.configuration.xml.exactmatching.MatchFields;
import org.openhie.openempi.configuration.xml.exactmatching.MatchRules;
import org.openhie.openempi.context.Context;

public class ExactMatchingConfigurationLoader implements ConfigurationLoader
{
	private Log log = LogFactory.getLog(ExactMatchingConfigurationLoader.class);
	private String entityName;

	public void loadAndRegisterComponentConfiguration(ConfigurationRegistry registry, Object configurationFragment)
	        throws InitializationException {

		// This loader only knows how to process configuration information specifically
		// for the exact matching service
		//
		if (!(configurationFragment instanceof ExactMatchingType)) {
			log.error("Custom configuration loader " + getClass().getName()
			        + " is unable to process the configuration fragment " + configurationFragment);
			throw new
			    InitializationException("Custom configuration loader is unable to load this configuration fragment.");
		}

        ExactMatchingType matchingConfig = (ExactMatchingType) configurationFragment;
        entityName = matchingConfig.getEntityName();
        Context.getConfiguration().registerConfigurationLoader(ComponentType.MATCHING, entityName, this);

		// Register the configuration information with the Configuration Registry so that
		// it is available for the matching service to use when needed.
		//
        ArrayList<MatchRule> matchRules = new ArrayList<MatchRule>();
        Map<String, Object> configurationData = new java.util.HashMap<String,Object>();
        configurationData.put(ExactMatchingConstants.EXACT_MATCHING_RULES_REGISTRY_KEY, matchRules);

		log.debug("Received xml fragment to parse: " + matchingConfig);
		if (matchingConfig == null || matchingConfig.getMatchRules().sizeOfMatchRuleArray() == 0) {
			log.warn("No matching rules were configured; probably a configuration issue.");
			return;
		}

        for (int i = 0; i < matchingConfig.getMatchRules().sizeOfMatchRuleArray(); i++) {
            org.openhie.openempi.configuration.xml.exactmatching.MatchRule rule = matchingConfig.getMatchRules().getMatchRuleArray(i);
            MatchRule matchRule = new MatchRule();
            for (int j = 0; j < rule.getMatchFields().sizeOfMatchFieldArray(); j++) {
                org.openhie.openempi.configuration.xml.exactmatching.MatchField field = rule.getMatchFields().getMatchFieldArray(j);
                log.trace("Looking for blocking field named " + field.getFieldName());
                matchRule.addField(buildMatchFieldFromXml(field));
            }
            matchRules.add(matchRule);
        }
        registry.registerConfigurationEntry(matchingConfig.getEntityName(),
                ConfigurationRegistry.MATCH_CONFIGURATION, configurationData);
		registry.registerConfigurationEntry(matchingConfig.getEntityName(), ConfigurationRegistry.MATCHING_ALGORITHM_NAME_KEY,
		        ExactMatchingConstants.EXACT_MATCHING_ALGORITHM_NAME);
	}

	@SuppressWarnings("unchecked")
    public void saveAndRegisterComponentConfiguration(ConfigurationRegistry registry,
            Map<String, Object> configurationData) throws InitializationException {
        Object obj = configurationData.get(ExactMatchingConstants.EXACT_MATCHING_RULES_REGISTRY_KEY);
        if (obj == null || !(obj instanceof List<?>)) {
            log.warn("Invalid configuration data passed to exact matching algorithm.");
            throw new InitializationException("Unable to save valid configuration data for "
                    + "the exact matching algorithm.");
        }
        List<MatchRule> rules = (List<MatchRule>) obj;
        String entityName = (String) configurationData.get(ConfigurationRegistry.ENTITY_NAME);
        ExactMatchingType fragment = buildMatchingConfigurationFragment(rules);
        fragment.setEntityName(entityName);
        log.debug("Saving matching info xml configuration fragment: " + fragment);
        updateConfigurationInFile(entityName, fragment);
        Context.getConfiguration().saveConfiguration();
        log.debug("Storing updated matching configuration in configuration registry: " + rules);
        registry.registerConfigurationEntry(entityName, ConfigurationRegistry.MATCH_CONFIGURATION,
                configurationData);
    }

    private void updateConfigurationInFile(String thisEntityName, ExactMatchingType fragment) {
        log.debug("Saving matching xml configuration fragment: " + fragment);
        MpiConfig config = Context.getConfiguration().getMpiConfig();
        int count = config.getMatchingConfigurationArray().length;
        int index = -1;
        for (int i = 0; i < count; i++) {
            MatchingConfigurationType type = config.getMatchingConfigurationArray(i);
            if (type instanceof ExactMatchingType) {
                ExactMatchingType matchingType = (ExactMatchingType) type;
                String entityName = matchingType.getEntityName();
                if (entityName.equals(thisEntityName)) {
                    index = i;
                    break;
                }
            }
        }
        if (index >= 0) {
            config.setMatchingConfigurationArray(index, fragment);
        } else {
            log.error("Unable to save the matching configuration since no such section currently "
                    + "exists in the configuration file:\n" + fragment);
        }
    }

    public String getComponentEntity() {
        return entityName;
    }

	private MatchField buildMatchFieldFromXml(org.openhie.openempi.configuration.xml.exactmatching.MatchField field) {
		MatchField matchField = new MatchField();
		matchField.setFieldName(field.getFieldName());
		if (field.getComparatorFunction() != null) {
			matchField.setComparatorFunction(buildComparatorFunctionFromXml(field.getComparatorFunction()));
			matchField.setMatchThreshold(field.getMatchThreshold());
		}
		return matchField;
	}

	private ComparatorFunction buildComparatorFunctionFromXml(org.openhie.openempi.configuration.xml.ComparatorFunction comparatorFunction) {
		ComparatorFunction function = new ComparatorFunction();
		function.setFunctionName(comparatorFunction.getFunctionName());
		if (comparatorFunction.isSetParameters() && comparatorFunction.getParameters().sizeOfParameterArray() > 0) {
			for (org.openhie.openempi.configuration.xml.Parameter parameter : comparatorFunction.getParameters().getParameterArray()) {
				log.debug("Adding parameter (" + parameter.getName() + "," + parameter.getValue() + ") to comparator function " + 
						function.getFunctionName());
				function.addParameter(parameter.getName(), parameter.getValue());
			}
		}
		return function;
	}

    private org.openhie.openempi.configuration.xml.ComparatorFunction buildComparatorFunctionFragment(ComparatorFunction comparatorFunction) {
        org.openhie.openempi.configuration.xml.ComparatorFunction function =
            org.openhie.openempi.configuration.xml.ComparatorFunction.Factory.newInstance();
        function.setFunctionName(comparatorFunction.getFunctionName());
        if (comparatorFunction.getParameterMap().size() == 0) {
            return function;
        }

        org.openhie.openempi.configuration.xml.Parameters parameters = 
            org.openhie.openempi.configuration.xml.Parameters.Factory.newInstance();
        for (String parameterName : comparatorFunction.getParameterMap().keySet()) {
            org.openhie.openempi.configuration.xml.Parameter parameter = parameters.addNewParameter();
            parameter.setName(parameterName);
            parameter.setValue(comparatorFunction.getParameterMap().get(parameterName));
        }
        return function;
    }

    private org.openhie.openempi.configuration.xml.exactmatching.ExactMatchingType buildMatchingConfigurationFragment(List<MatchRule> rules) {
        org.openhie.openempi.configuration.xml.exactmatching.ExactMatchingType matchingType =
            org.openhie.openempi.configuration.xml.exactmatching.ExactMatchingType.Factory.newInstance();

        MatchRules rulesNode = matchingType.addNewMatchRules();
        for (MatchRule matchRule : rules) {
            org.openhie.openempi.configuration.xml.exactmatching.MatchRule ruleNode = rulesNode.addNewMatchRule();
            MatchFields matchFields = ruleNode.addNewMatchFields();
            for (org.openhie.openempi.configuration.MatchField field : matchRule.getFields()) {
                org.openhie.openempi.configuration.xml.exactmatching.MatchField xmlField = matchFields.addNewMatchField();
                xmlField.setFieldName(field.getFieldName());
                xmlField.setComparatorFunction(buildComparatorFunctionFragment(field.getComparatorFunction()));
                xmlField.setMatchThreshold(field.getMatchThreshold());
            }
        }
        return matchingType;
    }
}
