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
package org.openhie.openempi.shallowmatching;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhie.openempi.Constants;
import org.openhie.openempi.InitializationException;
import org.openhie.openempi.configuration.ComparatorFunction;
import org.openhie.openempi.configuration.Component.ComponentType;
import org.openhie.openempi.configuration.ConfigurationLoader;
import org.openhie.openempi.configuration.ConfigurationRegistry;
import org.openhie.openempi.configuration.MatchField;
import org.openhie.openempi.configuration.xml.MatchingConfigurationType;
import org.openhie.openempi.configuration.xml.MpiConfigDocument.MpiConfig;
import org.openhie.openempi.configuration.xml.shallowmatching.ShallowMatchingType;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.notification.ObservationEventType;

public class ShallowMatchingConfigurationLoader implements ConfigurationLoader
{
	private Log log = LogFactory.getLog(ShallowMatchingConfigurationLoader.class);
	
	private String entityName;
	
	public void loadAndRegisterComponentConfiguration(ConfigurationRegistry registry, Object configurationFragment) throws InitializationException {

		// This loader only knows how to process configuration information specifically
		// for the probabilistic matching service
		//
		if (!(configurationFragment instanceof ShallowMatchingType)) {
			log.error("Custom configuration loader " + getClass().getName() + 
			        " is unable to process the configuration fragment " + configurationFragment);
			throw new InitializationException("Custom configuration loader is unable to load this configuration fragment.");
		}
		
		ShallowMatchingType matchConfigXml = (ShallowMatchingType) configurationFragment;
        entityName = matchConfigXml.getEntityName();
        Context.getConfiguration().registerConfigurationLoader(ComponentType.SHALLOWMATCHING, entityName, this);

		// Register the configuration information with the Configuration registry so that
		// it is available for the matching service to use when needed.
		//
		ArrayList<MatchField> matchFields = new ArrayList<MatchField>();
		Map<String,Object> configurationData = new java.util.HashMap<String,Object>();
		
		log.debug("Received xml fragment to parse: " + matchConfigXml);
		for (int i=0; i < matchConfigXml.getMatchFields().sizeOfMatchFieldArray(); i++) {
			org.openhie.openempi.configuration.xml.shallowmatching.MatchField field = 
			        matchConfigXml.getMatchFields().getMatchFieldArray(i);
			MatchField matchField = buildMatchFieldFromXml(field);
			matchFields.add(matchField);
		}
        configurationData.put(Constants.MATCHING_FIELDS_REGISTRY_KEY, matchFields);
		configurationData.put(ConfigurationRegistry.ENTITY_NAME, matchConfigXml.getEntityName());
		
        registry.registerConfigurationEntry(matchConfigXml.getEntityName(), ConfigurationRegistry.SHALLOW_MATCH_CONFIGURATION,
                configurationData);
		registry.registerConfigurationEntry(matchConfigXml.getEntityName(),
		        ConfigurationRegistry.SHALLOW_MATCHING_ALGORITHM_NAME_KEY, 
				ShallowMatchingConstants.SHALLOW_MATCHING_ALGORITHM_NAME);
	}

    public void saveAndRegisterComponentConfiguration(ConfigurationRegistry registry, Map<String,Object> configurationData)
            throws InitializationException {
        @SuppressWarnings("unchecked")
        List<MatchField> matchFields = (List<MatchField>) configurationData.get(Constants.MATCHING_FIELDS_REGISTRY_KEY);
        String entityName = (String) configurationData.get(ConfigurationRegistry.ENTITY_NAME);
        if (entityName == null) {
            log.error("The entity name is not registered in the configuration data.");
            throw new RuntimeException("The matching algorithm has not been properly configured.");
        }
        ShallowMatchingType xmlConfigurationFragment = buildMatchingConfigurationFragment(matchFields, configurationData);
        log.debug("Saving matching info xml configuration fragment: " + xmlConfigurationFragment);
        updateConfigurationInFile(entityName, xmlConfigurationFragment);
        Context.getConfiguration().saveConfiguration();
        log.debug("Storing updated matching configuration in configuration registry: " + configurationData);
        registry.registerConfigurationEntry(entityName, ConfigurationRegistry.SHALLOW_MATCH_CONFIGURATION,
                configurationData);

        // Generate a notification event to inform interested listeners via the lightweight mechanism that this event has occurred.
        Context.notifyObserver(ObservationEventType.MATCHING_CONFIGURATION_UPDATE_EVENT, configurationData);
    }

    private void updateConfigurationInFile(String thisEntityName, ShallowMatchingType fragment) {
        log.debug("Saving matching xml configuration fragment: " + fragment);
        MpiConfig config = Context.getConfiguration().getMpiConfig();
        int count = config.getShallowMatchingConfigurationArray().length;
        int index = -1;
        for (int i = 0; i < count; i++) {
            MatchingConfigurationType type = config.getMatchingConfigurationArray(i);
            if (type instanceof ShallowMatchingType) {
                ShallowMatchingType matchingType = (ShallowMatchingType) type;
                String entityName = matchingType.getEntityName();
                if (entityName.equals(thisEntityName)) {
                    index = i;
                    break;
                }
            }
        }
        if (index >= 0) {
            config.setShallowMatchingConfigurationArray(index, fragment);
        } else {
            log.error("Unable to save the matching configuration since no such section currently "
                    + "exists in the configuration file:\n" + fragment);
        }
    }
    
    public String getComponentEntity() {
        return entityName;
    }
	
	private org.openhie.openempi.configuration.xml.shallowmatching.ShallowMatchingType buildMatchingConfigurationFragment(List<MatchField> fields,
	        Map<String, Object> data) {
		org.openhie.openempi.configuration.xml.shallowmatching.ShallowMatchingType matchingConfigurationType =
			org.openhie.openempi.configuration.xml.shallowmatching.ShallowMatchingType.Factory.newInstance();
		matchingConfigurationType.setEntityName((String) data.get(ConfigurationRegistry.ENTITY_NAME));
		org.openhie.openempi.configuration.xml.shallowmatching.MatchFields matchFieldsXml = matchingConfigurationType.addNewMatchFields();
		for (MatchField matchField : fields) {
			org.openhie.openempi.configuration.xml.shallowmatching.MatchField matchFieldXml =
			matchFieldsXml.addNewMatchField();
			matchFieldXml.setFieldName(matchField.getFieldName());
			matchFieldXml.setComparatorFunction(buildComparatorFunctionFragment(matchField.getComparatorFunction()));
			matchFieldXml.setMatchThreshold(matchField.getMatchThreshold());
		}
		return matchingConfigurationType;
	}

	private MatchField buildMatchFieldFromXml(org.openhie.openempi.configuration.xml.shallowmatching.MatchField field) {
		MatchField matchField = new MatchField();
		matchField.setFieldName(field.getFieldName());
		matchField.setMatchThreshold(field.getMatchThreshold());
		matchField.setComparatorFunction(buildComparatorFunctionFromXml(field.getComparatorFunction()));
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
}
