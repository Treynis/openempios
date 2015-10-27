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
package org.openhie.openempi.matching.fellegisunter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openhie.openempi.Constants;
import org.openhie.openempi.model.VectorConfiguration;

public class VectorConfigurationHelper
{
	private static Logger log = Logger.getLogger(VectorConfigurationHelper.class);
	
	/**
	 * At startup, this method loads into the configuration registry the default list of vectors
	 * that are valid for the number of matching fields specified.
	 * 
	 * @param configurationData
	 * @param size
	 */
	public static void loadVectorConfiguration(Map<String, Object> configurationData, int size) {
		int numVectors = (int) Math.pow(2.0, size);
		List<VectorConfiguration> vectors = new ArrayList<VectorConfiguration>();
		for (int i=0; i < numVectors; i++) {
			VectorConfiguration vectorConfig = new VectorConfiguration();
			vectorConfig.setVectorValue(i);
			vectors.add(vectorConfig);
			if (log.isDebugEnabled()) {
				log.debug("Loaded vector configuration:" + vectorConfig);
			}
		}
		configurationData.put(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_VECTOR_CONFIGURATION, vectors);
	}

	/**
	 * Whenever the configuration of the weights changes, the vector configuration in the registry
	 * must be updated.
	 * 
	 * @param params
	 */
	public static void updateVectorConfiguration(Map<String, Object> configurationData, FellegiSunterParameters params) {
		loadVectorConfiguration(configurationData, params.fieldCount);
		@SuppressWarnings("unchecked")
		List<VectorConfiguration> vectors = (List<VectorConfiguration>) configurationData.
				get(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_VECTOR_CONFIGURATION);
		if (vectors == null) {
			log.warn("The default vector configuration has not been loaded into the system so it cannot be updated.");
			return;
		}
		
		for (VectorConfiguration vector : vectors) {
			double weight = params.getVectorWeight(vector.getVectorValue());
			vector.setWeight(weight);
			classifyVector(vector, params);
		}
		
		// Now update the explicitly overridden vector classifications
		@SuppressWarnings("unchecked")
		Map<Integer,Integer> vectorClassifications = (Map<Integer,Integer>) configurationData.
				get(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_VECTOR_CLASSIFICATIONS);
		if (vectorClassifications != null) {
    		for (VectorConfiguration vector : vectors) {
    			Integer classification = (Integer) vectorClassifications.get(vector.getVectorValue());
    			if (classification == null) {
    				continue;
    			}
    			if (classification.intValue() == Constants.MATCH_CLASSIFICATION) {
    				vector.setManualClassification(Constants.MATCH_CLASSIFICATION);
    			} else if (classification.intValue() == Constants.NON_MATCH_CLASSIFICATION) {
    				vector.setManualClassification(Constants.NON_MATCH_CLASSIFICATION);
    			} else if (classification.intValue() == Constants.PROBABLE_MATCH_CLASSIFICATION) {
    				vector.setManualClassification(Constants.PROBABLE_MATCH_CLASSIFICATION);
    			}
    		}
		}
		configurationData.put(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_VECTOR_CONFIGURATION,
		        vectors);
		configurationData.put(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_VECTOR_CLASSIFICATIONS,
		        vectorClassifications);
	}
    
    static void calculateAllVectorWeights(FellegiSunterParameters fellegiSunterParams) {
        int vectorCount = fellegiSunterParams.getVectorCount();
        for (int index = 0; index < vectorCount; index++) {
            VectorConfigurationHelper.calculateVectorWeight(index, fellegiSunterParams);
        }       
    }
    
    static void calculateVectorWeight(int vectorValue, FellegiSunterParameters fellegiSunterParams) {
        int bitPositions = fellegiSunterParams.getFieldCount();
        double weight = 0;
        for (int i = 0; i < bitPositions; i++) {
            int bitPosValue = getBitValueAtPosition(vectorValue, i);
            if (bitPosValue == 1) {
                Double numerator = fellegiSunterParams.getMValue(i);
                Double denominator = fellegiSunterParams.getUValue(i);
                numerator = adjustMinimumValue(numerator);
                denominator = adjustMinimumValue(denominator);
                weight += Math.log(numerator / denominator) / Math.log(2.0);
            } else {
                Double numerator = (1.0 - fellegiSunterParams.getMValue(i));
                Double denominator = (1.0 - fellegiSunterParams.getUValue(i));
                numerator = adjustMinimumValue(numerator);
                denominator = adjustMinimumValue(denominator);
                weight += Math.log(numerator / denominator) / Math.log(2.0);
            }
        }
        fellegiSunterParams.setVectorWeight(vectorValue, weight);
        if (log.isTraceEnabled()) {
            log.trace("Set the weight of vector " + vectorValue + " to " + weight);
        }
    }

    static Double adjustMinimumValue(Double numerator) {
        if (numerator.doubleValue() < ProbabilisticMatchingConstants.MIN_MARGINAL_VALUE.doubleValue()) {
            numerator = ProbabilisticMatchingConstants.MIN_MARGINAL_VALUE;
        }
        return numerator;
    }
    
    static int getBitValueAtPosition(int value, int pos) {
        int mask = 1;
        for (int i = 1; i <= pos; i++) {
            mask = mask << 1;
        }
        int valueAtPosition = mask & value;
        if (valueAtPosition > 0) {
            valueAtPosition = 1;
        }
        return valueAtPosition;
    }

	private static void classifyVector(VectorConfiguration vector, FellegiSunterParameters params) {
		if (vector.getWeight() >= params.getUpperBound()) {
			vector.setAlgorithmClassification(Constants.MATCH_CLASSIFICATION);
			vector.setManualClassification(Constants.MATCH_CLASSIFICATION);
		} else if (vector.getWeight() > params.getLowerBound()) {
			vector.setAlgorithmClassification(Constants.PROBABLE_MATCH_CLASSIFICATION);
			vector.setManualClassification(Constants.PROBABLE_MATCH_CLASSIFICATION);
		} else {
			vector.setAlgorithmClassification(Constants.NON_MATCH_CLASSIFICATION);
			vector.setManualClassification(Constants.NON_MATCH_CLASSIFICATION);
		}
	}
}