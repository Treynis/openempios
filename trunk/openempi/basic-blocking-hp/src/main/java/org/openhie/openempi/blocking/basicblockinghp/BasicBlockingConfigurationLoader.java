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
package org.openhie.openempi.blocking.basicblockinghp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhie.openempi.InitializationException;
import org.openhie.openempi.configuration.BaseField;
import org.openhie.openempi.configuration.BlockingRound;
import org.openhie.openempi.configuration.ConfigurationLoader;
import org.openhie.openempi.configuration.ConfigurationRegistry;
import org.openhie.openempi.configuration.Component.ComponentType;
import org.openhie.openempi.configuration.xml.BlockingConfigurationType;
import org.openhie.openempi.configuration.xml.MpiConfigDocument.MpiConfig;
import org.openhie.openempi.configuration.xml.basicblockinghp.BasicBlockingType;
import org.openhie.openempi.configuration.xml.basicblockinghp.BlockingField;
import org.openhie.openempi.configuration.xml.basicblockinghp.BlockingFields;
import org.openhie.openempi.configuration.xml.basicblockinghp.BlockingRounds;
import org.openhie.openempi.context.Context;

/**

 * @author Odysseas Pentakalos 
 * @version $Revision: $ $Date:  $
 */
public class BasicBlockingConfigurationLoader implements ConfigurationLoader
{
	private Log log = LogFactory.getLog(BasicBlockingConfigurationLoader.class);
	private static final int DEFAULT_MAX_BLOCK_SIZE = 0;
	private String entityName;
	
	public void loadAndRegisterComponentConfiguration(ConfigurationRegistry registry, Object configurationFragment) throws InitializationException {

		// This loader only knows how to process configuration information specifically
		// for the basic blocking service
		//
		if (!(configurationFragment instanceof BasicBlockingType)) {
			log.error("Custom configuration loader " + getClass().getName() + 
			        " is unable to process the configuration fragment " + configurationFragment);
			throw new InitializationException("Custom configuration loader is unable to load "
			        + "this configuration fragment.");
		}

		BasicBlockingType blockingConfig = (BasicBlockingType) configurationFragment;
		entityName = blockingConfig.getEntityName();
	    log.info("Loading blocking configuration for entity " + entityName);
	    Context.getConfiguration().registerConfigurationLoader(ComponentType.BLOCKING, entityName, this);

		// Register the configuration information with the Configuration Registry so that
		// it is available for the blocking service to use when needed.
		//
		ArrayList<BlockingRound> blockingRounds = new ArrayList<BlockingRound>();
		Map<String,Object> configurationData = new java.util.HashMap<String,Object>();
		configurationData.put(BasicBlockingConstants.BLOCKING_ROUNDS_REGISTRY_KEY, blockingRounds);
		configurationData.put(BasicBlockingConstants.ENTITY_NAME__KEY, entityName);
		
		log.debug("Received xml fragment to parse: " + blockingConfig);
		if (blockingConfig == null || blockingConfig.getBlockingRounds().sizeOfBlockingRoundArray() == 0) {
			log.warn("No blocking rounds were configured; probably a configuration issue.");
			return;
		}

		for (int i=0; i < blockingConfig.getBlockingRounds().sizeOfBlockingRoundArray(); i++) {
			org.openhie.openempi.configuration.xml.basicblockinghp.BlockingRound round = 
			        blockingConfig.getBlockingRounds().getBlockingRoundArray(i);
			BlockingRound blockingRound = new BlockingRound();
			blockingRound.setName("round." + i);
			for (int j=0; j < round.getBlockingFields().sizeOfBlockingFieldArray(); j++) {
				org.openhie.openempi.configuration.xml.basicblockinghp.BlockingField field = 
				        round.getBlockingFields().getBlockingFieldArray(j);
				log.trace("Looking for blocking field named " + field.getFieldName());
				blockingRound.addField(new BaseField(field.getFieldName()));
			}
			blockingRounds.add(blockingRound);
		}
		
		int maxBlockSize = blockingConfig.getMaximumBlockSize();
		if (maxBlockSize == 0) {
		    maxBlockSize = DEFAULT_MAX_BLOCK_SIZE;
		}
		configurationData.put(BasicBlockingConstants.MAXIMUM_BLOCK_SIZE, maxBlockSize);
		configurationData.put(BasicBlockingConstants.LAST_UPDATE_DATE, new java.util.Date());

        registry.registerConfigurationEntry(entityName, ConfigurationRegistry.BLOCKING_CONFIGURATION,
                configurationData);
		registry.registerConfigurationEntry(entityName, ConfigurationRegistry.BLOCKING_ALGORITHM_NAME_KEY,
		        BasicBlockingConstants.BLOCKING_ALGORITHM_NAME);
	}

	@SuppressWarnings("unchecked")
	public void saveAndRegisterComponentConfiguration(ConfigurationRegistry registry,
	        Map<String,Object> configurationData) throws InitializationException {
		Object obj = configurationData.get(BasicBlockingConstants.BLOCKING_ROUNDS_REGISTRY_KEY);
		if (obj == null || !(obj instanceof List<?>)) {
			log.warn("Invalid configuration data passed to traditional blocking algorithm.");
			throw new InitializationException("Unable to save nvalid configuration data for"
			        + " the traditional blocking algorithm.");
		}
		List<BlockingRound> rounds = (List<BlockingRound>) obj;
		BasicBlockingType xmlConfigurationFragment = buildConfigurationFileFragment(rounds);
		String entityName = (String) configurationData.get(BasicBlockingConstants.ENTITY_NAME__KEY);
		xmlConfigurationFragment.setEntityName(entityName);
		setMaximumBlockSize(configurationData, xmlConfigurationFragment);
		log.debug("Saving blocking info xml configuration fragment: " + xmlConfigurationFragment);
		updateConfigurationInFile(entityName, xmlConfigurationFragment);
		Context.getConfiguration().saveConfiguration();
		log.debug("Storing updated blocking configuration in configuration registry: " + rounds);
		configurationData.put(BasicBlockingConstants.LAST_UPDATE_DATE, new java.util.Date());
		registry.registerConfigurationEntry(entityName, ConfigurationRegistry.BLOCKING_CONFIGURATION,
		        configurationData);
	}

    private void updateConfigurationInFile(String thisEntityName, BasicBlockingType fragment) {
        log.debug("Saving blocking info xml configuration fragment: " + fragment);
        MpiConfig config = Context.getConfiguration().getMpiConfig();
        int count = config.getBlockingConfigurationArray().length;
        int index = -1;
        for (int i = 0; i < count; i++) {
            BlockingConfigurationType type = config.getBlockingConfigurationArray(i);
            if (type instanceof BasicBlockingType) {
                BasicBlockingType blockingType = (BasicBlockingType) type;
                String entityName = blockingType.getEntityName();
                if (entityName.equals(thisEntityName)) {
                    index = i;
                    break;
                }
            }
        }
        if (index >= 0) {
            config.setBlockingConfigurationArray(index, fragment);
        } else {
            log.error("Unable to save the blocking configuration since no such section currently "
                    + "exists in the configuration file:\n" + fragment);
        }
    }
    
    private void setMaximumBlockSize(Map<String, Object> configurationData,
            BasicBlockingType xmlConfigurationFragment) {
        Integer maximumBlockSize = (Integer) configurationData.get(BasicBlockingConstants.MAXIMUM_BLOCK_SIZE);
		if (maximumBlockSize == null || maximumBlockSize <= 0) {
		    maximumBlockSize = DEFAULT_MAX_BLOCK_SIZE;
		}
		xmlConfigurationFragment.setMaximumBlockSize(maximumBlockSize.intValue());
    }
    
    public String getComponentEntity() {
        return entityName;
    }
    
	private BasicBlockingType buildConfigurationFileFragment(List<BlockingRound> rounds) {
		BasicBlockingType newBasicBlocking = BasicBlockingType.Factory.newInstance();
		BlockingRounds roundsNode = newBasicBlocking.addNewBlockingRounds();
		for (BlockingRound blockingRound : rounds) {
			org.openhie.openempi.configuration.xml.basicblockinghp.BlockingRound roundNode = 
			        roundsNode.addNewBlockingRound();
			BlockingFields blockingFields = roundNode.addNewBlockingFields();
			for (org.openhie.openempi.configuration.BaseField field : blockingRound.getFields()) {
				BlockingField xmlField = blockingFields.addNewBlockingField();
				xmlField.setFieldName(field.getFieldName());
			}
		}
		return newBasicBlocking;
	}
}
