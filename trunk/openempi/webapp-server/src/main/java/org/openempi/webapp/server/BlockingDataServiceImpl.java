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

import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.openempi.webapp.client.BlockingDataService;
import org.openempi.webapp.client.model.BaseFieldWeb;
import org.openempi.webapp.client.model.BlockingEntryListWeb;
import org.openempi.webapp.client.model.SortedNeighborhoodConfigurationWeb;
import org.openempi.webapp.client.model.SuffixArrayBlockingConfigurationWeb;
import org.openhie.openempi.configuration.BlockingRound;
import org.openhie.openempi.configuration.Configuration;
import org.openhie.openempi.configuration.ConfigurationLoader;
import org.openhie.openempi.configuration.ConfigurationRegistry;
import org.openhie.openempi.context.Context;

public class BlockingDataServiceImpl extends AbstractRemoteServiceServlet implements BlockingDataService
{
    private static final long serialVersionUID = 2707492638994310226L;
    
    public final static String BLOCKING_ROUNDS_REGISTRY_KEY = "blockingRounds";
    public final static String MAXIMUM_BLOCK_SIZE = "maxBlockSize";
    public final static String SN_BLOCKING_ROUNDS_REGISTRY_KEY = "sortedNeighborhood.blockingRounds";
    public final static String WINDOW_SIZE_REGISTRY_KEY = "sortedNeighborhood.windowSize";
    public final static String SA_BLOCKING_ROUNDS_REGISTRY_KEY = "suffixArray.blockingRounds";
    public final static String MINIMUM_SUFFIX_LENGTH_REGISTRY_KEY = "suffixArray.minimumSuffixLength";
    public final static String MAXIMUM_BLOCK_SIZE_REGISTRY_KEY = "suffixArray.maximumBlockSize";
    public final static String SIMILARITY_METRIC_REGISTRY_KEY = "suffixArray.similarityMetric";
    public final static String THRESHOLD_REGISTRY_KEY = "suffixArray.threshold";
    public final static String ENTITY_NAME_KEY = "entityName";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @SuppressWarnings("unchecked")
    public SortedNeighborhoodConfigurationWeb loadSortedNeighborhoodBlockingConfigurationData(String entityName) throws Exception {
        log.debug("Received request to load the sorted neighborhood blocking configuration data.");

        authenticateCaller();
        try {
            Map<String, Object> configurationData = (Map<String, Object>) Context.getConfiguration()
                    .lookupConfigurationEntry(entityName, ConfigurationRegistry.BLOCKING_CONFIGURATION);
            List<BlockingRound> rounds = (List<BlockingRound>) configurationData.get(SN_BLOCKING_ROUNDS_REGISTRY_KEY);
            List<BaseFieldWeb> webRounds = convertToClientModel(rounds);
            Integer windowSize = (Integer) configurationData.get(WINDOW_SIZE_REGISTRY_KEY);
            SortedNeighborhoodConfigurationWeb webConfig = new SortedNeighborhoodConfigurationWeb(webRounds, windowSize);
            // String entityName = (String) configurationData.get(ENTITY_NAME_KEY);
            webConfig.setEntityName(entityName);
            return webConfig;
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public SuffixArrayBlockingConfigurationWeb loadSuffixArrayBlockingConfigurationData(String entityName) throws Exception {
        log.debug("Received request to load the sorted neighborhood blocking configuration data.");

        authenticateCaller();
        try {
            Map<String, Object> configurationData = (Map<String, Object>) Context.getConfiguration()
                    .lookupConfigurationEntry(entityName, ConfigurationRegistry.BLOCKING_CONFIGURATION);
            List<BlockingRound> rounds = (List<BlockingRound>) configurationData.get(SA_BLOCKING_ROUNDS_REGISTRY_KEY);
            List<BaseFieldWeb> webRounds = convertToClientModel(rounds);
            Integer minimumSuffixLength = (Integer) configurationData.get(MINIMUM_SUFFIX_LENGTH_REGISTRY_KEY);
            Integer maximumBlockSize = (Integer) configurationData.get(MAXIMUM_BLOCK_SIZE_REGISTRY_KEY);
            String similarityMetric = (String) configurationData.get(SIMILARITY_METRIC_REGISTRY_KEY);
            Float similarityThreshold = (Float) configurationData.get(THRESHOLD_REGISTRY_KEY);
            SuffixArrayBlockingConfigurationWeb webConfig = new SuffixArrayBlockingConfigurationWeb(webRounds,
                    minimumSuffixLength, maximumBlockSize, similarityMetric, similarityThreshold);
            // String entityName = (String) configurationData.get(ENTITY_NAME_KEY);
            webConfig.setEntityName(entityName);
            return webConfig;
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public BlockingEntryListWeb loadTraditionalBlockingConfigurationData(String entityName) throws Exception {
        log.debug("Received request to load the blocking configuration data.");

        authenticateCaller();
        try {
            BlockingEntryListWeb blockingEntry = new BlockingEntryListWeb();

            Map<String, Object> configurationData = (Map<String, Object>) Context.getConfiguration()
                    .lookupConfigurationEntry(entityName, ConfigurationRegistry.BLOCKING_CONFIGURATION);
            List<BlockingRound> rounds = (List<BlockingRound>) configurationData.get(BLOCKING_ROUNDS_REGISTRY_KEY);
            Integer maximumBlockSize = (Integer) configurationData.get(MAXIMUM_BLOCK_SIZE);

            blockingEntry.setMaximumBlockSize(maximumBlockSize);
            blockingEntry.setBlockingRoundtEntries(convertToClientModel(rounds));
            // String entityName = (String) configurationData.get(ENTITY_NAME_KEY);
            blockingEntry.setEntityName(entityName);
            return blockingEntry;
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    private List<BaseFieldWeb> convertToClientModel(List<BlockingRound> rounds) {
        List<BaseFieldWeb> fields = new java.util.ArrayList<BaseFieldWeb>(rounds.size());
        int blockingRoundIndex = 1;
        for (BlockingRound blockingRound : rounds) {
            int blockingFieldIndex = 1;
            for (org.openhie.openempi.configuration.BaseField baseField : blockingRound.getFields()) {
                BaseFieldWeb clientField = new BaseFieldWeb(blockingRoundIndex, blockingFieldIndex,
                        baseField.getFieldName());
                fields.add(clientField);
                blockingFieldIndex++;
            }
            blockingRoundIndex++;
        }
        return fields;
    }

    public String saveTraditionalBlockingConfigurationData(BlockingEntryListWeb blockingConfiguration) throws Exception {

        authenticateCaller();

        Configuration configuration = Context.getConfiguration();
        String returnMessage = "";
        try {
            ConfigurationLoader loader = configuration
                    .getBlockingConfigurationLoader(blockingConfiguration.getEntityName());
            if (loader == null) {
                log.error("Unable to find a loader for saving the configuration of the blocking algorithm.");
                returnMessage = "No loader has been configured for storing the configuration information.";
                return returnMessage;
            }            
            List<BlockingRound> rounds = convertFromClientModel(blockingConfiguration.getBlockingRoundEntries());
            Integer maximumBlockSize = blockingConfiguration.getMaximumBlockSize();

            Map<String, Object> configurationData = new java.util.HashMap<String, Object>();
            configurationData.put(BLOCKING_ROUNDS_REGISTRY_KEY, rounds);
            configurationData.put(MAXIMUM_BLOCK_SIZE, maximumBlockSize);
            configurationData.put(ENTITY_NAME_KEY, blockingConfiguration.getEntityName());
            loader.saveAndRegisterComponentConfiguration(configuration, configurationData);
        } catch (Exception e) {
            log.warn("Failed while saving the blocking configuration: " + e, e);
            returnMessage = e.getMessage();
            throw new Exception(returnMessage);
        }
        return returnMessage;
    }

    public String saveSortedNeighborhoodBlockingConfigurationData(
            SortedNeighborhoodConfigurationWeb blockingConfiguration) throws Exception {

        authenticateCaller();

        Configuration configuration = Context.getConfiguration();
        String returnMessage = "";
        try {
            ConfigurationLoader loader = configuration
                    .getBlockingConfigurationLoader(blockingConfiguration.getEntityName());
            if (loader == null) {
                log.error("Unable to find a loader for saving the configuration of the blocking algorithm.");
                returnMessage = "No loader has been configured for storing the configuration information.";
                return returnMessage;
            }                 
            List<BlockingRound> rounds = convertFromClientModel(blockingConfiguration.getBlockingRounds());
            Map<String, Object> configurationData = new java.util.HashMap<String, Object>();
            configurationData.put(SN_BLOCKING_ROUNDS_REGISTRY_KEY, rounds);
            configurationData.put(WINDOW_SIZE_REGISTRY_KEY, blockingConfiguration.getWindowSize());
            configurationData.put(ENTITY_NAME_KEY, blockingConfiguration.getEntityName());
            loader.saveAndRegisterComponentConfiguration(configuration, configurationData);
        } catch (Exception e) {
            log.warn("Failed while saving the blocking configuration: " + e, e);
            returnMessage = e.getMessage();
            throw new Exception(returnMessage);
        }
        return returnMessage;
    }

    public String saveSuffixArrayBlockingConfigurationData(SuffixArrayBlockingConfigurationWeb blockingConfiguration)
            throws Exception {

        authenticateCaller();

        Configuration configuration = Context.getConfiguration();
        String returnMessage = "";
        try {
            ConfigurationLoader loader = configuration
                    .getBlockingConfigurationLoader(blockingConfiguration.getEntityName());
            if (loader == null) {
                log.error("Unable to find a loader for saving the configuration of the blocking algorithm.");
                returnMessage = "No loader has been configured for storing the configuration information.";
                return returnMessage;
            }            
            List<BlockingRound> rounds = convertFromClientModel(blockingConfiguration.getBlockingRounds());
            Map<String, Object> configurationData = new java.util.HashMap<String, Object>();
            configurationData.put(SA_BLOCKING_ROUNDS_REGISTRY_KEY, rounds);
            configurationData.put(MINIMUM_SUFFIX_LENGTH_REGISTRY_KEY, blockingConfiguration.getMinimumSuffixLength());
            configurationData.put(MAXIMUM_BLOCK_SIZE_REGISTRY_KEY, blockingConfiguration.getMaximumBlockSize());
            configurationData.put(SIMILARITY_METRIC_REGISTRY_KEY, blockingConfiguration.getSimilarityMetric());
            configurationData.put(THRESHOLD_REGISTRY_KEY, blockingConfiguration.getSimilarityThreshold());
            configurationData.put(ENTITY_NAME_KEY, blockingConfiguration.getEntityName());
            loader.saveAndRegisterComponentConfiguration(configuration, configurationData);
        } catch (Exception e) {
            log.warn("Failed while saving the blocking configuration: " + e, e);
            returnMessage = e.getMessage();
            throw new Exception(returnMessage);
        }
        return returnMessage;
    }

    private List<BlockingRound> convertFromClientModel(List<BaseFieldWeb> blockingConfiguration) {
        int roundsCount = 0;
        for (BaseFieldWeb baseField : blockingConfiguration) {
            if (baseField.getBlockingRound() > roundsCount) {
                roundsCount = baseField.getBlockingRound();
            }
        }
        List<BlockingRound> rounds = new java.util.ArrayList<BlockingRound>(roundsCount);
        for (int currRound = 1; currRound <= roundsCount; currRound++) {
            BlockingRound round = new BlockingRound();
            for (BaseFieldWeb baseField : blockingConfiguration) {
                if (baseField.getBlockingRound() == currRound) {
                    round.addField(new org.openhie.openempi.configuration.BaseField(baseField.getFieldName()));
                }
            }
            rounds.add(round);
        }
        return rounds;
    }

}
