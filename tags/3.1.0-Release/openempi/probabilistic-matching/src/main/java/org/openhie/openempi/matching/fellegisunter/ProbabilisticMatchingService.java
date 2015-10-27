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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.Constants;
import org.openhie.openempi.InitializationException;
import org.openhie.openempi.blocking.RecordPairIterator;
import org.openhie.openempi.blocking.RecordPairSource;
import org.openhie.openempi.configuration.BaseField;
import org.openhie.openempi.configuration.BlockingRound;
import org.openhie.openempi.configuration.ConfigurationRegistry;
import org.openhie.openempi.configuration.MatchField;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.dao.UniversalDao;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.matching.AbstractMatchingLifecycleObserver;
import org.openhie.openempi.matching.MatchingService;
import org.openhie.openempi.matching.SamplingService;
import org.openhie.openempi.model.ComparisonVector;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.LoggedLink;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.openhie.openempi.model.RecordPair;
import org.openhie.openempi.notification.EventObservable;
import org.openhie.openempi.notification.ObservationEventType;
import org.openhie.openempi.stringcomparison.StringComparisonService;
import org.openhie.openempi.util.ConvertUtil;

public class ProbabilisticMatchingService extends AbstractMatchingLifecycleObserver implements MatchingService,
        Observer
{
    private static final String BLOCKING_ROUNDS_REGISTRY_KEY = "blockingRounds";
    private static final Double MIN_MARGINAL_VALUE = 0.0000001;
    private static final Double MAX_MARGINAL_VALUE = 0.9999999;
    private EntityDao entityDao;
    private UniversalDao universalDao;
    private boolean useSampling;
    private SamplingService samplingService;
    private Map<String,MatchingConfiguration> configByEntity = new HashMap<String,MatchingConfiguration>();

    public void startup() throws InitializationException {
        List<Entity> entities = Context.getEntityDefinitionManagerService().loadEntities();
        for (Entity entity : entities) {
            MatchingService service = Context.getMatchingService(entity.getName());
            if (service.getMatchingServiceId() != getMatchingServiceId()) {
                continue;
            }
            
            MatchingConfiguration config = new MatchingConfiguration();
            try {
                log.info("Loading configuration for entity " + entity.getName());
                loadConfiguration(entity, config);
            } catch (Throwable t) {
                configByEntity.put(entity.getName(), config);
                if (t.getCause() == null || !(t.getCause() instanceof FileNotFoundException)) {
                    log.error("Failed while initializing the probabilistic matching service: " + t, t);
                } else {
                    log.warn("Didn't find an existing configuration of the Fellegi Sunter matching algorithm; it will now be regenerated.");
                }
                config.setInitialized(false);
                try {
                    linkRecords(entity);
                    loadConfiguration(entity, config);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> configurationData = (Map<String, Object>) Context.getConfiguration()
                            .lookupConfigurationEntry(entity.getName(), ConfigurationRegistry.MATCH_CONFIGURATION);
                    updateRegistryWithNewModel(configurationData, config.getFellegiSunterParams());
                    VectorConfigurationHelper.updateVectorConfiguration(configurationData, config.getFellegiSunterParams());
                } catch (Exception e) {
                    log.error("Failed while initializing the probabilistic matching service: " + e, e);
                    config.setInitialized(false);
                }
            }
            configByEntity.put(entity.getName(), config);
        }
        Context.registerObserver(this, ObservationEventType.MATCHING_CONFIGURATION_UPDATE_EVENT);
    }

    public Set<RecordPair> match(Record record) throws ApplicationException {
        log.trace("Looking for matches on record " + record);
        String entityName = record.getEntity().getName();
        MatchingConfiguration config = getConfiguration(entityName);
        loadLogFields(record.getEntity().getName(), config);
        if (!config.isInitialized()) {
            log.warn("The matching service has not been initialized yet for entity " + entityName);
            throw new ApplicationException("Matching service has not been initialized yet for entity " + entityName);
        }
        List<RecordPair> pairs = Context.getBlockingService(entityName).findCandidates(record);
        Set<RecordPair> matches = new java.util.HashSet<RecordPair>();
        scoreRecordPairs(config, pairs);
        calculateRecordPairWeights(pairs, config.getFellegiSunterParams());

        // Apply Fellegi-Sunter classification rule to each pair
        for (RecordPair pair : pairs) {
            classifyRecordPair(config, pair);
            if (pair.getMatchOutcome() == RecordPair.MATCH_OUTCOME_LINKED) {
                matches.add(pair);
            } else if (pair.getMatchOutcome() == RecordPair.MATCH_OUTCOME_POSSIBLE) {
                // This is a possible match; need to add it to the list for
                // review
                addReviewRecordPair(pair);
            }
        }

        if (isLoggingEnabled(config)) {
            for (RecordPair pair : pairs) {
                logRecordPair(config, pair);
            }
        }
        return matches;
    }

    public RecordPair match(RecordPair recordPair) throws ApplicationException {
        if (log.isTraceEnabled()) {
            log.trace("Looking for matches on record pair " + recordPair);
        }
        String entityName = recordPair.getLeftRecord().getEntity().getName();
        MatchingConfiguration config = getConfiguration(entityName);
        loadLogFields(entityName, config);
        if (!config.isInitialized()) {
            throw new ApplicationException("Matching service has not been initialized yet for entity " + entityName);
        }
        scoreRecordPair(config, recordPair);
        calculateRecordPairWeight(recordPair, config.getFellegiSunterParams());

        log.debug("Record pair weight (" + recordPair.getLeftRecord().getRecordId() + ","
                + recordPair.getRightRecord().getRecordId() + ") is: " + recordPair.getWeight());
        classifyRecordPair(config, recordPair);
        if (isLoggingEnabled(config)) {
            logRecordPair(config, recordPair);
        }
        return recordPair;
    }

	public void linkRecords(Entity entity) {
		log.info("Retrieving all record pairs before initializing the classification model.");
		List<RecordPair> pairs = getRecordPairs(entity);
        MatchingConfiguration config = getConfiguration(entity.getName());

        FellegiSunterParameters fellegiSunterParams = new FellegiSunterParameters(config.getFields().size());
        config.setFellegiSunterParams(fellegiSunterParams);

        ProbabilisticMatchingConfigurationLoader.loadDefaultValues(fellegiSunterParams);
        fellegiSunterParams.setMatchingFieldNames(config.getMatchFieldNames());
        fellegiSunterParams.setMu(config.getFalsePositiveProbability());
        fellegiSunterParams.setLambda(config.getFalseNegativeProbability());
		if (pairs.size() > 0) {
			log.info("Scoring all record pairs retrieved from the blocking service.");
			scoreRecordPairs(config, pairs);
            log.info("Calculating vector frequencies.");
            calculateVectorFrequencies(pairs, config);
            log.info("Estimating model conditional distributions.");
            estimateMarginalProbabilities(config);
            adjustProbabilityValues(fellegiSunterParams);
            log.info("Calculating weights for all record pairs.");
            calculateRecordPairWeights(pairs, fellegiSunterParams);
            log.info("Ordering record pairs using weight value.");
            orderRecordPairsByWeight(pairs);
            calculateMarginalProbabilities(config, pairs, fellegiSunterParams);
            // calculateBoundsOnVectors(fellegiSunterParams);
            log.info("Calculating classification model lower and upper bounds.");
            calculateBounds(pairs, fellegiSunterParams);
		} else {
			createDefaultConfiguration(fellegiSunterParams);
		}
        FellegiSunterConfigurationManager.saveParameters(config.getConfigurationDirectory(), entity.getName(),
                fellegiSunterParams);
        loadLogFields(entity.getName(), config);
        config.setInitialized(true);
    }

	public FellegiSunterParameters getFellegiSunterParameters(String entityName) {
	    MatchingConfiguration config = getConfiguration(entityName);
	    if (config == null) {
	        log.warn("Could not find a configuration for entity " + entityName);
	        return null;
	    }
	    return config.getFellegiSunterParams();
	}

    public Set<String> getMatchFields(String entityName) {
        Set<String> matchFields = new HashSet<String>();
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) Context.getConfiguration()
                .lookupConfigurationEntry(entityName, ConfigurationRegistry.MATCH_CONFIGURATION);
        if (config == null) {
            log.warn("The matching service has not been initialized yet for entity " + entityName);
            return matchFields;
        }
        @SuppressWarnings("unchecked")
        List<MatchField> fieldList = (List<MatchField>) config.get(Constants.MATCHING_FIELDS_REGISTRY_KEY);
        if (fieldList == null) {
            log.warn("The matching service has not been initialized yet for entity " + entityName);
            return matchFields;
        }
        for (MatchField field : fieldList) {
            matchFields.add(field.getFieldName());
        }
        return matchFields;
    }

    private void classifyRecordPair(MatchingConfiguration config, RecordPair recordPair) {
        recordPair.setLinkSource(new LinkSource(getMatchingServiceId()));
        Integer manualClassification = lookupManualConfigurationValue(recordPair);
        if (manualClassification == null) {
            recordPair.setLinkSource(new LinkSource(getMatchingServiceId()));
            if (recordPair.getWeight() >= config.getFellegiSunterParams().getUpperBound()) {
                recordPair.setMatchOutcome(RecordPair.MATCH_OUTCOME_LINKED);
            } else if (recordPair.getWeight() <= config.getFellegiSunterParams().getLowerBound()) {
                recordPair.setMatchOutcome(RecordPair.MATCH_OUTCOME_UNLINKED);
            } else {
                recordPair.setMatchOutcome(RecordPair.MATCH_OUTCOME_POSSIBLE);
            }
            if (log.isTraceEnabled()) {
                log.trace("Vector with value " + recordPair.getComparisonVector().getBinaryVectorString()
                        + " was classified as " + decodeClassification(recordPair.getMatchOutcome()));
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Used manual override to classify vector "
                        + recordPair.getComparisonVector().getBinaryVectorString() + " as "
                        + decodeClassification(manualClassification));
            }
            recordPair.setMatchOutcome(manualClassification);
        }
    }

    private String decodeClassification(Integer value) {
        if (value == RecordPair.MATCH_OUTCOME_LINKED) {
            return "Link";
        } else if (value == RecordPair.MATCH_OUTCOME_POSSIBLE) {
            return "Possible Link";
        } else {
            return "Non-link";
        }
    }

    @SuppressWarnings("unchecked")
    private Integer lookupManualConfigurationValue(RecordPair recordPair) {
        String entityName = recordPair.getLeftRecord().getEntity().getName();
        Map<String, Object> configurationData = (Map<String, Object>) Context.getConfiguration()
                .lookupConfigurationEntry(entityName, ConfigurationRegistry.MATCH_CONFIGURATION);
        Map<Integer, Integer> vectorClassifications = (Map<Integer, Integer>) configurationData
                .get(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_VECTOR_CLASSIFICATIONS);
        if (vectorClassifications == null) {
            return null;
        }
        Integer manualClassification = vectorClassifications.get(recordPair.getComparisonVector()
                .getBinaryVectorValue());
        return manualClassification;
    }

    private void addReviewRecordPair(RecordPair pair) {
        RecordLink link = ConvertUtil.createRecordLinkFromRecordPair(pair);
        link.setState(RecordLinkState.POSSIBLE_MATCH);
        if (log.isTraceEnabled()) {
            log.trace("Adding probable link: " + link);
        }
        entityDao.saveRecordLink(link);
    }

    private void adjustProbabilityValues(FellegiSunterParameters params) {
        for (int i = 0; i < params.getFieldCount(); i++) {
            if (params.getMValue(i) > MAX_MARGINAL_VALUE) {
                logAdjustment("m", i, MAX_MARGINAL_VALUE, params.getMValue(i));
                params.setMValue(i, MAX_MARGINAL_VALUE);
            }
            if (params.getMValue(i) < MIN_MARGINAL_VALUE) {
                logAdjustment("m", i, MIN_MARGINAL_VALUE, params.getMValue(i));
                params.setMValue(i, MIN_MARGINAL_VALUE);
            }
            if (params.getUValue(i) > MAX_MARGINAL_VALUE) {
                logAdjustment("u", i, MAX_MARGINAL_VALUE, params.getUValue(i));
                params.setUValue(i, MAX_MARGINAL_VALUE);
            }
            if (params.getUValue(i) < MIN_MARGINAL_VALUE) {
                logAdjustment("u", i, MIN_MARGINAL_VALUE, params.getUValue(i));
                params.setUValue(i, MIN_MARGINAL_VALUE);
            }
        }
    }

    private void logAdjustment(String prob, int index, double newValue, double oldValue) {
        log.info("Adjusting " + prob + "-value for field index " + index + " to " + newValue + " from " + oldValue);
    }

    public int getMatchingServiceId() {
        return LinkSource.PROBABILISTIC_MATCHING_ALGORITHM_SOURCE;
    }

	public List<RecordPair> getRecordPairs(Entity entity) {
	    if (getSamplingService() != null && isUseSampling()) {
	        return getSamplingService().getRecordPairs(entity);
	    } else {
    		RecordPairSource source = Context.getBlockingService(entity.getName()).getRecordPairSource(entity);
            List<RecordPair> pairs = new ArrayList<RecordPair>();
            int pairCount = 0;
            for (RecordPairIterator iter = source.iterator(); iter.hasNext();) {
                pairs.add(iter.next());
                pairCount++;
                if (pairCount % 10000 == 0) {
                    log.info("Loaded " + pairCount + " pairs.");
                }
            }
            return pairs;
	    }
    }

    public void scoreRecordPair(MatchingConfiguration config, RecordPair pair) {
        StringComparisonService comparisonService = Context.getStringComparisonService();
        final MatchField[] matchFields = config.getMatchFields();
        final String[] matchFieldNames = config.getMatchFieldNames();
        pair.setComparisonVector(new ComparisonVector(matchFields));
        for (int i = 0; i < matchFieldNames.length; i++) {
            String fieldName = matchFieldNames[i];
            Object value1 = pair.getLeftRecord().get(fieldName);
            Object value2 = pair.getRightRecord().get(fieldName);
            double distance = comparisonService.score(matchFields[i].getComparatorFunction().getFunctionName(),
                    matchFields[i].getComparatorFunction().getParameterMap(), value1, value2);
            pair.getComparisonVector().setScore(i, distance);
        }
        log.debug("Comparing records " + pair.getLeftRecord().getRecordId() + " and "
                + pair.getRightRecord().getRecordId() + " got vector "
                + pair.getComparisonVector().getBinaryVectorString());
    }

    public void scoreRecordPairs(MatchingConfiguration config, List<RecordPair> pairs) {
        StringComparisonService comparisonService = Context.getStringComparisonService();
        final MatchField[] matchFields = config.getMatchFields();
        final String[] matchFieldNames = config.getMatchFieldNames();
        for (RecordPair pair : pairs) {
            pair.setComparisonVector(new ComparisonVector(matchFields));
            for (int i = 0; i < matchFieldNames.length; i++) {
                String fieldName = matchFieldNames[i];
                Object value1 = pair.getLeftRecord().get(fieldName);
                Object value2 = pair.getRightRecord().get(fieldName);
                double distance = comparisonService.score(matchFields[i].getComparatorFunction().getFunctionName(),
                        matchFields[i].getComparatorFunction().getParameterMap(), value1, value2);
                pair.getComparisonVector().setScore(i, distance);
            }
            log.debug("Comparing records " + pair.getLeftRecord().getRecordId() + " and "
                    + pair.getRightRecord().getRecordId() + " got vector "
                    + pair.getComparisonVector().getBinaryVectorString());
        }
    }

    public void calculateVectorFrequencies(List<RecordPair> pairs, MatchingConfiguration config) {
        FellegiSunterParameters params = config.getFellegiSunterParams();
        for (int i = 0; i < params.getVectorCount(); i++) {
            params.setVectorFrequency(i, 0);
        }
        for (RecordPair pair : pairs) {
            ComparisonVector vector = pair.getComparisonVector();
            if (vector.getBinaryVectorValue() == 2) {
                log.debug(vector.getBinaryVectorString() + "=>" + vector.getScoreVectorString() + "=>"
                        + getRecordPairMatchFields(config, pair));
            }
            params.incrementVectorFrequency(vector.getBinaryVectorValue());
        }
        if (log.isDebugEnabled()) {
            params.logVectorFrequencies(log);
        }
    }

    public void calculateRecordPairWeights(List<RecordPair> pairs, FellegiSunterParameters fellegiSunterParams) {
        // First calculate the weight by vector configuration; this way we won't
        // have to
        // repeat the calculation for every record pair; there are only 2^n
        // vector configurations
        // where n is the number of match fields.
        VectorConfigurationHelper.calculateAllVectorWeights(fellegiSunterParams);
        for (RecordPair pair : pairs) {
            calculateRecordPairWeight(pair, fellegiSunterParams);
        }
    }

    private void calculateRecordPairWeight(RecordPair pair, FellegiSunterParameters fellegiSunterParams) {
        ComparisonVector vector = pair.getComparisonVector();
        int vectorValue = vector.getBinaryVectorValue();
        pair.setWeight(fellegiSunterParams.getVectorWeight(vectorValue));
        pair.setVector(vectorValue);
    }

    public List<RecordPair> orderRecordPairsByWeight(List<RecordPair> pairs) {
        Collections.sort(pairs, new RecordPairComparator());
        return pairs;
    }

    public void calculateMarginalProbabilities(MatchingConfiguration config, List<RecordPair> pairs,
            FellegiSunterParameters fellegiSunterParams) {
        for (RecordPair pair : pairs) {
            ComparisonVector vector = pair.getComparisonVector();
            Integer binaryVectorValue = vector.getBinaryVectorValue();
            if (config.getVectorByValueMap().get(binaryVectorValue) == null) {
                log.trace("Added the vector " + vector.getBinaryVectorString() + " in map keyed by "
                        + binaryVectorValue);
                config.getVectorByValueMap().put(binaryVectorValue, vector);
            }
            vector.calculateProbabilityGivenMatch(fellegiSunterParams.getMValues());
            vector.calculateProbabilityGivenNonmatch(fellegiSunterParams.getUValues());
        }
    }

    public void estimateMarginalProbabilities(MatchingConfiguration config) {
        FellegiSunterParameters fellegiSunterParameters = config.getFellegiSunterParams();
        ExpectationMaximizationEstimator estimator = new ExpectationMaximizationEstimator();
        estimator.estimateMarginalProbabilities(fellegiSunterParameters, getInitialMValue(fellegiSunterParameters),
                getInitialUValue(fellegiSunterParameters), getInitialPValue(fellegiSunterParameters),
                getMaxIterations(fellegiSunterParameters), getConvergenceError(fellegiSunterParameters));
    }

    private double getConvergenceError(FellegiSunterParameters fellegiSunterParams) {
        if (fellegiSunterParams == null || fellegiSunterParams.getConvergenceError() == 0) {
            return ProbabilisticMatchingConfigurationLoader.DEFAULT_CONVERGENCE_ERROR;
        }
        return fellegiSunterParams.getConvergenceError();
    }

    private int getMaxIterations(FellegiSunterParameters fellegiSunterParams) {
        if (fellegiSunterParams == null || fellegiSunterParams.getMaxIterations() == 0) {
            return ProbabilisticMatchingConfigurationLoader.DEFAULT_MAX_EM_ITERATIONS;
        }
        return fellegiSunterParams.getMaxIterations();
    }

    private double getInitialPValue(FellegiSunterParameters fellegiSunterParams) {
        if (fellegiSunterParams == null || fellegiSunterParams.getPInitialValue() == 0) {
            return ProbabilisticMatchingConfigurationLoader.DEFAULT_P_INITIAL_VALUE;
        }
        return fellegiSunterParams.getPInitialValue();
    }

    private double getInitialUValue(FellegiSunterParameters fellegiSunterParams) {
        if (fellegiSunterParams == null || fellegiSunterParams.getUInitialValue() == 0) {
            return ProbabilisticMatchingConfigurationLoader.DEFAULT_U_INITIAL_VALUE;
        }
        return fellegiSunterParams.getUInitialValue();
    }

    private double getInitialMValue(FellegiSunterParameters fellegiSunterParams) {
        if (fellegiSunterParams == null || fellegiSunterParams.getMInitialValue() == 0) {
            return ProbabilisticMatchingConfigurationLoader.DEFAULT_M_INITIAL_VALUE;
        }
        return fellegiSunterParams.getMInitialValue();
    }

    public String getRecordPairMatchFields(MatchingConfiguration config, RecordPair pair) {
        StringBuffer sb = new StringBuffer("{ ");
        String[] matchFieldNames = config.getMatchFieldNames();
        for (int i = 0; i < matchFieldNames.length; i++) {
            String fieldName = matchFieldNames[i];
            Object value1 = pair.getLeftRecord().get(fieldName);
            Object value2 = pair.getRightRecord().get(fieldName);
            sb.append("[").append(value1).append(",").append(value2).append("]");
            if (i < matchFieldNames.length - 1) {
                sb.append(",");
            }
        }
        return sb.append(" }").toString();
    }

/*
    public void calculateBoundsOnVectors(FellegiSunterParameters fellegiSunterParams) {
        List<ComparisonVector> list = new java.util.ArrayList<ComparisonVector>(vectorByValueMap.values().size());
        for (ComparisonVector vector : vectorByValueMap.values()) {
            double ratio = vector.getVectorProbGivenM() / vector.getVectorProbGivenU();
            vector.setOrderingRatio(ratio);
            double weight = Math.log(ratio) / Math.log(2.0);
            vector.setVectorWeight(weight);
            list.add(vector);
        }
        // We now order the vectors based on the decreasing ratio of agreement
        // to disagreement probability
        Collections.sort(list, new Comparator<ComparisonVector>()
        {
            @Override
            public int compare(ComparisonVector v1, ComparisonVector v2) {
                double rank = v2.getOrderingRatio() - v1.getOrderingRatio();
                int order;
                if (rank > 0) {
                    order = 1;
                } else if (rank < 0) {
                    order = -1;
                } else {
                    order = 0;
                }
                return order;
            }
        });

        double sum = 0;
        int index = 0;
        for (ComparisonVector vector : list) {
            log.trace("V[" + vector.getBinaryVectorString() + "], m(g)=" + vector.getVectorProbGivenM() + ", u(g)="
                    + vector.getVectorProbGivenU() + ", ratio=" + vector.getOrderingRatio() + ", weight="
                    + vector.getVectorWeight());
            sum += vector.getVectorProbGivenU();
            if (sum > fellegiSunterParams.getMu()) {
                break;
            }
            index++;
        }
        ComparisonVector theOne = list.get(index);
        fellegiSunterParams.setUpperBound(theOne.getVectorWeight());
        log.trace("Set the upper bound to: " + theOne.getVectorWeight());

        // We now order the vectors based on the increasing ratio of agreement
        // to disagreement probability
        Collections.sort(list, new Comparator<ComparisonVector>()
        {
            @Override
            public int compare(ComparisonVector v1, ComparisonVector v2) {
                double rank = v2.getOrderingRatio() - v1.getOrderingRatio();
                int order;
                if (rank > 0) {
                    order = -1;
                } else if (rank < 0) {
                    order = 1;
                } else {
                    order = 0;
                }
                return order;
            }
        });

        sum = 0;
        index = 0;
        for (ComparisonVector vector : list) {
            log.trace("V[" + vector.getBinaryVectorString() + "], m(g)=" + vector.getVectorProbGivenM() + ", u(g)="
                    + vector.getVectorProbGivenU() + ", ratio=" + vector.getOrderingRatio() + ", weight="
                    + vector.getVectorWeight());
            sum += vector.getVectorProbGivenM();
            if (sum > fellegiSunterParams.getLambda()) {
                break;
            }
            index++;
        }
        theOne = list.get(index);
        fellegiSunterParams.setLowerBound(theOne.getVectorWeight());
        log.trace("Set the lower bound to: " + theOne.getVectorWeight());
    }
*/
    public void calculateBounds(List<RecordPair> pairs, FellegiSunterParameters fellegiSunterParams) {
        double sum = 0;
        int index = 0;
        for (RecordPair pair : pairs) {
            sum += pair.getComparisonVector().getVectorProbGivenM();
            if (log.isTraceEnabled()) {
                log.trace("Lambda sum for vector " + pair.getComparisonVector().getBinaryVectorString() + " is: " + sum);
            }
            index++;
            if (sum > fellegiSunterParams.getLambda()) {
                break;
            }
        }
        log.trace("Sum: " + sum + ", lambda: " + fellegiSunterParams.getLambda() + ", index: " + (index - 1)
                + ", value: " + pairs.get(index - 1).getWeight());
        fellegiSunterParams.setLowerBound(pairs.get(index - 1).getWeight());

        sum = 0;
        index = pairs.size() - 1;
        for (int i = index; i >= 0; i--) {
            sum += pairs.get(i).getComparisonVector().getVectorProbGivenU();
            if (log.isTraceEnabled()) {
                log.trace("Mu sum for vector " + pairs.get(i).getComparisonVector().getBinaryVectorString() + " is: "
                        + sum);
            }
            if (sum > fellegiSunterParams.getMu()) {
                break;
            }
        }
        log.trace("Sum: " + sum + ", mu: " + fellegiSunterParams.getMu() + ", index: " + index + ", value: "
                + pairs.get(index).getWeight());
        fellegiSunterParams.setUpperBound(pairs.get(index).getWeight());
    }

    @SuppressWarnings("unchecked")
    private void loadConfiguration(Entity entity, MatchingConfiguration config) {
        Map<String, Object> configurationData = (Map<String, Object>) Context.getConfiguration()
                .lookupConfigurationEntry(entity.getName(), ConfigurationRegistry.MATCH_CONFIGURATION);
        loadConfiguration(configurationData, config);
        loadLogFields(entity.getName(), config);
        Context.getConfiguration().registerConfigurationEntry(entity.getName(),
                ConfigurationRegistry.MATCH_CONFIGURATION, configurationData);
        configByEntity.put(entity.getName(), config);
    }

    @SuppressWarnings("unchecked")
    private void loadLogFields(String entityName, MatchingConfiguration config) {
        Map<String, Object> blockingConfData = (Map<String, Object>) Context.getConfiguration()
                .lookupConfigurationEntry(entityName, ConfigurationRegistry.BLOCKING_CONFIGURATION);
        Object obj = blockingConfData.get(BLOCKING_ROUNDS_REGISTRY_KEY);
        Set<String> blockingFieldList = new java.util.HashSet<String>();
        if (obj != null) {
            List<BlockingRound> blockingRounds = (List<BlockingRound>) obj;
            for (BlockingRound round : blockingRounds) {
                for (BaseField field : round.getFields()) {
                    blockingFieldList.add(field.getFieldName());
                }
            }
            if (log.isTraceEnabled()) {
                log.trace("Loaded blocking fields for logging of " + blockingFieldList.toString());
            }
        }
        config.setBlockingFieldList(blockingFieldList);

        List<MatchField> matchFields = config.getFields();
        List<String> logFieldList = new ArrayList<String>();
        for (MatchField field : matchFields) {
            logFieldList.add(field.getFieldName());
        }
        if (log.isTraceEnabled()) {
            log.trace("Loaded matching fields for logging of " + logFieldList.toString());
        }
        config.setLogFieldList(logFieldList);
    }

    private void updateRegistryWithNewModel(Map<String, Object> data, FellegiSunterParameters params) {
        data.put(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_M_VALUES_KEY, params.getMValues());
        data.put(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_U_VALUES_KEY, params.getUValues());
        data.put(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_P_VALUE_KEY, params.getPValue());
        data.put(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_LOWER_BOUND_KEY, params.getLowerBound());
        data.put(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_UPPER_BOUND_KEY, params.getUpperBound());        
    }

    @SuppressWarnings("unchecked")
    private MatchingConfiguration loadConfiguration(Map<String, Object> configurationData, MatchingConfiguration config) {
        config.setFields((List<MatchField>) configurationData.get(Constants.MATCHING_FIELDS_REGISTRY_KEY));
        config.setFalseNegativeProbability((Float) configurationData
                .get(ProbabilisticMatchingConstants.FALSE_NEGATIVE_PROBABILITY_REGISTRY_KEY));
        config.setFalsePositiveProbability((Float) configurationData
                .get(ProbabilisticMatchingConstants.FALSE_POSITIVE_PROBABILITY_REGISTRY_KEY));
        config.setConfigurationDirectory((String) configurationData
                .get(ProbabilisticMatchingConstants.CONFIGURATION_DIRECTORY_REGISTRY_KEY));
        if (config.getFields() == null || config.getFields().size() == 0) {
            log.error("Probabilistic matching service has not been configured properly; no match fields have been defined.");
            throw new RuntimeException("Probabilistic maching service has not been configured properly.");
        }
        if (config.getFalseNegativeProbability() == null) {
            log.warn("The false negative probability has not been configured; using default value of: "
                    + ProbabilisticMatchingConstants.DEFAULT_FALSE_NEGATIVE_PROBABILITY);
            config.setFalseNegativeProbability(ProbabilisticMatchingConstants.DEFAULT_FALSE_NEGATIVE_PROBABILITY);
        }
        if (config.getFalsePositiveProbability() == null) {
            log.warn("The false positive probability has not been configured; using default value of: "
                    + ProbabilisticMatchingConstants.DEFAULT_FALSE_POSITIVE_PROBABILITY);
            config.setFalsePositiveProbability(ProbabilisticMatchingConstants.DEFAULT_FALSE_POSITIVE_PROBABILITY);
        }
        if (config.getConfigurationDirectory() == null) {
            log.warn("The configuration directory has not been configured; using default value of: "
                    + Context.getOpenEmpiHome());
            config.setConfigurationDirectory(Context.getOpenEmpiHome());
        }
        Map<String,MatchField> matchFieldByName = new HashMap<String, MatchField>();

        String[] matchFieldNames = new String[config.getFields().size()];
        MatchField[] matchFields = new MatchField[config.getFields().size()];
        int index = 0;
        for (MatchField field : config.getFields()) {
            matchFieldNames[index] = field.getFieldName();
            matchFields[index] = field;
            matchFieldByName.put(field.getFieldName(), field);
            index++;
        }
        config.setMatchFieldNames(matchFieldNames);
        config.setMatchFieldByName(matchFieldByName);
        config.setMatchFields(matchFields);
        log.debug("Matching service " + getClass().getName() + " will perform matching using " + toString());

        if (configurationData.containsKey(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_LOGGING_BY_VECTORS_KEY)
                && (Boolean) configurationData
                        .get(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_LOGGING_BY_VECTORS_KEY)) {
            config.setLogByVectors(true);
            config.setVectorsToLog((Set<Integer>) configurationData
                    .get(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_LOGGING_BY_VECTORS_VECTORS_KEY));
            config.setLogByVectorsFraction(((Double) configurationData
                    .get(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_LOGGING_BY_VECTORS_FRACTION_KEY))
                    .doubleValue());
        } else {
            config.setLogByVectors(false);
        }

        if (configurationData.containsKey(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_LOGGING_BY_WEIGHT_KEY)
                && (Boolean) configurationData
                        .get(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_LOGGING_BY_WEIGHT_KEY)) {
            config.setLogByWeight(true);
            config.setLogWeightLowerBound(((Double) configurationData
                    .get(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_LOGGING_BY_WEIGHT_LOWER_BOUND_KEY))
                    .doubleValue());
            config.setLogWeightUpperBound(((Double) configurationData
                    .get(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_LOGGING_BY_WEIGHT_UPPER_BOUND_KEY))
                    .doubleValue());
            config.setLogByWeightFraction(((Double) configurationData
                    .get(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_LOGGING_BY_WEIGHT_FRACTION_KEY))
                    .doubleValue());

        } else {
            config.setLogByWeight(false);
        }

        String entityName = (String) configurationData.get(ConfigurationRegistry.ENTITY_NAME);
		if (entityName == null) {
			log.error("The entity that the configuration of the matching algorithm applies to has not been defined.");
			throw new RuntimeException("The entity that the configuration of the matching algorithm applies to has not been defined.");
		}
		config.setEntityName(entityName);

		List<Entity> entityList = Context.getEntityDefinitionManagerService().findEntitiesByName(entityName);
		if (entityList == null || entityList.size() == 0) {
			log.error("The entity that the configuration of the matching algorithm points to is unknown.");
			throw new RuntimeException("The entity that the configuration of the matching algorithm points to is unknown.");
		}

		if (configurationData.containsKey(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_LOGGING_BY_VECTORS_KEY)
                || configurationData
                        .containsKey(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_LOGGING_BY_WEIGHT_KEY)) {
            config.setLogDestination(((String) configurationData
                    .get(ProbabilisticMatchingConstants.PROBABILISTIC_MATCHING_LOGGING_DESTINATION)));
            if (config.getLogDestination() == null) {
                config.setLogDestination(Constants.PROBABILISTIC_MATCHING_LOGGING_DESTINATION_TO_FILE);
            } else {
                config.setLogDestination(Constants.PROBABILISTIC_MATCHING_LOGGING_DESTINATION_TO_DB);
            }
        }
        config.setFellegiSunterParams(FellegiSunterConfigurationManager
                .loadParameters(config.getConfigurationDirectory(), entityName));
        ProbabilisticMatchingConfigurationLoader.loadDefaultValues(config.getFellegiSunterParams());

        VectorConfigurationHelper.updateVectorConfiguration(configurationData, config.getFellegiSunterParams());
        config.setInitialized(true);
        return config;
    }

    private boolean isLoggingEnabled(MatchingConfiguration config) {
        if (config.isLogByVectors() || config.isLogByWeight()) {
            return true;
        }
        return false;
    }

    private void logRecordPair(MatchingConfiguration config, RecordPair pair) {
        double randomValue = Math.random();
        if (config.isLogByVectors() == true && randomValue < config.getLogByVectorsFraction()
                && config.getVectorsToLog().contains(new Integer(pair.getComparisonVector().getBinaryVectorValue()))) {
            logRecordPairToDestination(config, pair);
        }
        if (config.isLogByWeight() == true && randomValue < config.getLogByWeightFraction() 
                && pair.getWeight() >= config.getLogWeightLowerBound()
                && pair.getWeight() <= config.getLogWeightUpperBound()) {
            if (log.isDebugEnabled()) {
                log.debug("logRecordPair: " + logMatchStatus(pair) + "Weight -> " + pair.getWeight() + " -> "
                        + pair.getComparisonVector().getBinaryVectorString() + " -> " + getPairValues(config, pair));
            }
            logRecordPairToDestination(config, pair);
        }
    }

    private void logRecordPairToDestination(MatchingConfiguration config, RecordPair pair) {
        if (config.getLogDestination().equalsIgnoreCase(Constants.PROBABILISTIC_MATCHING_LOGGING_DESTINATION_TO_FILE)) {
            if (log.isDebugEnabled()) {
                log.debug("logRecordPair: " + logMatchStatus(pair) + "Weight -> " + pair.getWeight() + "Vector -> "
                        + pair.getComparisonVector().getBinaryVectorString() + " -> " + getPairValues(config, pair));
            }
        } else {
            LoggedLink loggedLink = getLoggedLinkFromRecordPair(pair);
            universalDao.save(loggedLink);
        }
    }

    private LoggedLink getLoggedLinkFromRecordPair(RecordPair pair) {
        LoggedLink link = new LoggedLink();
        link.setDateCreated(new java.util.Date());
        link.setEntityId(pair.getLeftRecord().getEntity().getEntityVersionId());
        link.setLeftRecordId(pair.getLeftRecord().getRecordId());
        link.setRightRecordId(pair.getRightRecord().getRecordId());
        link.setUserCreatedBy(Context.getUserContext().getUser());
        link.setVectorValue(pair.getComparisonVector().getBinaryVectorValue());
        link.setWeight(pair.getWeight());
        return link;
    }

    private String logMatchStatus(RecordPair pair) {
        if (pair.getMatchOutcome() == RecordPair.MATCH_OUTCOME_LINKED) {
            return "[L] ";
        } else if (pair.getMatchOutcome() == RecordPair.MATCH_OUTCOME_UNLINKED) {
            return "[U] ";
        } else if (pair.getMatchOutcome() == RecordPair.MATCH_OUTCOME_POSSIBLE) {
            return "[P] ";
        }
        return "[ ] ";
    }

    private String getPairValues(MatchingConfiguration config, RecordPair pair) {
        StringBuffer sb = new StringBuffer();
        logRecordBlockingFields(sb, pair.getLeftRecord(), config.getBlockingFieldList());
        sb.append("[");
        logRecordMatchFields(sb, pair.getLeftRecord(), config.getLogFieldList());
        sb.append("] versus [");
        // logRecordBlockingFields(sb, pair.getRightRecord(),
        // getBlockingLoggingFields());
        // sb.append("[");
        logRecordMatchFields(sb, pair.getRightRecord(), config.getLogFieldList());
        sb.append("]");
        return sb.toString();
    }

    private void logRecordBlockingFields(StringBuffer sb, Record rec, Set<String> fields) {
        if (fields != null && fields.size() == 0) {
            return;
        }
        sb.append("{");
        int index = 0;
        for (String field : fields) {
            Object value = rec.get(field);
            if (value == null) {
                sb.append("null");
            } else {
                sb.append("'").append(value.toString()).append("'");
            }
            if (index < fields.size() - 1) {
                sb.append(",");
            }
            index++;
        }
        sb.append("}");
    }

    private void logRecordMatchFields(StringBuffer sb, Record rec, List<String> fields) {
        for (int i = 0; i < fields.size(); i++) {
            Object value = rec.get(fields.get(i));
            if (value == null) {
                sb.append("null");
            } else {
                sb.append("'").append(value.toString()).append("'");
            }
            if (i < fields.size() - 1) {
                sb.append(",");
            }
        }
    }

    private MatchingConfiguration getConfiguration(String entityName) {
        return configByEntity.get(entityName);
    }

    public EntityDao getEntityDao() {
        return entityDao;
    }

    public void setEntityDao(EntityDao entityDao) {
        this.entityDao = entityDao;
    }

    public UniversalDao getUniversalDao() {
        return universalDao;
    }

    public void setUniversalDao(UniversalDao universalDao) {
        this.universalDao = universalDao;
    }

    public boolean isUseSampling() {
        return useSampling;
    }

    public void setUseSampling(boolean useSampling) {
        this.useSampling = useSampling;
    }

    public SamplingService getSamplingService() {
        return samplingService;
    }

    public void setSamplingService(SamplingService samplingService) {
        this.samplingService = samplingService;
    }

    public static void main(String[] args) {
        String baseDirectory = Context.getOpenEmpiHome();
        System.out.println(FellegiSunterConfigurationManager.loadParameters(baseDirectory + "/" + "conf", "person"));
    }

    public void shutdown() {
        log.info("Shutting down the probabilistic matching service.");
    }

    public void initializeRepository(String entityName) throws ApplicationException {
        MatchingConfiguration config = configByEntity.get(entityName);
        FellegiSunterConfigurationManager.removeParametersFile(config.getConfigurationDirectory(), entityName);
        startup();
    }

    @Override
    public void update(Observable o, Object eventData) {
        if (!(o instanceof EventObservable) || eventData == null || !(eventData instanceof Map) || 
                ((EventObservable) o).getType() != ObservationEventType.MATCHING_CONFIGURATION_UPDATE_EVENT) {
            log.trace("Received notification for event that was not expected.");
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) eventData;
        log.info("The configuration of the matching algorithm was changed by the user.");

        MatchingConfiguration config = new MatchingConfiguration();
        loadConfiguration(data, config);
        Entity entity = Context.getEntityDefinitionManagerService().loadEntityByName(config.getEntityName());
        if (entity == null) {
            log.error("Received an update request for a change in the configuration for an unknown entity: "
                    + config.getEntityName());
            return;
        }
        configByEntity.put(entity.getName(), config);

        try {
            FellegiSunterConfigurationManager.loadParameters(config.getConfigurationDirectory(),
                    config.getEntityName());
            ProbabilisticMatchingConfigurationLoader.loadDefaultValues(config.getFellegiSunterParams());
            config.setInitialized(true);
        } catch (Throwable t) {
            log.error("Failed while initializing the probabilistic matching service: " + t, t);
            config.setInitialized(false);
            try {
                linkRecords(entity);
                updateRegistryWithNewModel(data, config.getFellegiSunterParams());
            } catch (Exception e) {
                log.error("Failed while initializing the probabilistic matching service: " + e, e);
                config.setInitialized(false);
            }
        }
    }

	/**
	 * Use of these parameters is not a good practice but it is needed for when we initially start
	 * an instance with an inproper configuration of the blocking and matching algorithm. This configuration
	 * will allow the system to start-up (get past the initialization of the probabilistic algorithm)
	 * so, that we can load the data, analyze the statistics, and re-run the probabilistic algorithm to
	 * get  a more reasonable estimation of the probabilistic model.
	 *
	 * @param params
	 */
	private void createDefaultConfiguration(FellegiSunterParameters params) {
		params.setUpperBound(ProbabilisticMatchingConstants.DEFAULT_UPPER_BOUND);
		params.setLowerBound(ProbabilisticMatchingConstants.DEFAULT_LOWER_BOUND);
		for (int i=0; i < params.getFieldCount(); i++) {
			params.setMValue(i, 0.5);
			params.setUValue(i,  0.5);
		}
	}

	private class MatchingConfiguration
	{
	    private String[] matchFieldNames;
	    private MatchField[] matchFields;
	    private boolean initialized = false;
	    private FellegiSunterParameters fellegiSunterParams;
	    private HashMap<Integer, ComparisonVector> vectorByValueMap = new HashMap<Integer, ComparisonVector>();
	    private List<MatchField> fields = new ArrayList<MatchField>();
	    private Float falseNegativeProbability;
	    private Float falsePositiveProbability;
	    private String configurationDirectory;
	    private boolean logByVectors;
	    private double logByVectorsFraction;
	    private boolean logByWeight;
	    private String logDestination;
	    private Set<Integer> vectorsToLog;
	    private Set<String> blockingFieldList;
	    private List<String> logFieldList;
	    private double logWeightLowerBound;
	    private double logWeightUpperBound;
	    private double logByWeightFraction;
	    private String entityName;

        public void setMatchFieldByName(Map<String, MatchField> matchFieldByName) {
        }
        public String[] getMatchFieldNames() {
            return matchFieldNames;
        }
        public void setMatchFieldNames(String[] matchFieldNames) {
            this.matchFieldNames = matchFieldNames;
        }
        public MatchField[] getMatchFields() {
            return matchFields;
        }
        public void setMatchFields(MatchField[] matchFields) {
            this.matchFields = matchFields;
        }
        public boolean isInitialized() {
            return initialized;
        }
        public void setInitialized(boolean initialized) {
            this.initialized = initialized;
        }
        public FellegiSunterParameters getFellegiSunterParams() {
            return fellegiSunterParams;
        }
        public void setFellegiSunterParams(FellegiSunterParameters fellegiSunterParams) {
            this.fellegiSunterParams = fellegiSunterParams;
        }
        public HashMap<Integer, ComparisonVector> getVectorByValueMap() {
            return vectorByValueMap;
        }
        public List<MatchField> getFields() {
            return fields;
        }
        public void setFields(List<MatchField> fields) {
            this.fields = fields;
        }
        public Float getFalseNegativeProbability() {
            return falseNegativeProbability;
        }
        public void setFalseNegativeProbability(Float falseNegativeProbability) {
            this.falseNegativeProbability = falseNegativeProbability;
        }
        public Float getFalsePositiveProbability() {
            return falsePositiveProbability;
        }
        public void setFalsePositiveProbability(Float falsePositiveProbability) {
            this.falsePositiveProbability = falsePositiveProbability;
        }
        public String getConfigurationDirectory() {
            return configurationDirectory;
        }
        public void setConfigurationDirectory(String configurationDirectory) {
            this.configurationDirectory = configurationDirectory;
        }
        public boolean isLogByVectors() {
            return logByVectors;
        }
        public void setLogByVectors(boolean logByVectors) {
            this.logByVectors = logByVectors;
        }
        public double getLogByVectorsFraction() {
            return logByVectorsFraction;
        }
        public void setLogByVectorsFraction(double logByVectorsFraction) {
            this.logByVectorsFraction = logByVectorsFraction;
        }
        public boolean isLogByWeight() {
            return logByWeight;
        }
        public void setLogByWeight(boolean logByWeight) {
            this.logByWeight = logByWeight;
        }
        public String getLogDestination() {
            return logDestination;
        }
        public void setLogDestination(String logDestination) {
            this.logDestination = logDestination;
        }
        public Set<Integer> getVectorsToLog() {
            return vectorsToLog;
        }
        public void setVectorsToLog(Set<Integer> vectorsToLog) {
            this.vectorsToLog = vectorsToLog;
        }
        public Set<String> getBlockingFieldList() {
            return blockingFieldList;
        }
        public void setBlockingFieldList(Set<String> blockingFieldList) {
            this.blockingFieldList = blockingFieldList;
        }
        public List<String> getLogFieldList() {
            return logFieldList;
        }
        public void setLogFieldList(List<String> logFieldList) {
            this.logFieldList = logFieldList;
        }
        public double getLogWeightLowerBound() {
            return logWeightLowerBound;
        }
        public void setLogWeightLowerBound(double logWeightLowerBound) {
            this.logWeightLowerBound = logWeightLowerBound;
        }
        public double getLogWeightUpperBound() {
            return logWeightUpperBound;
        }
        public void setLogWeightUpperBound(double logWeightUpperBound) {
            this.logWeightUpperBound = logWeightUpperBound;
        }
        public double getLogByWeightFraction() {
            return logByWeightFraction;
        }
        public void setLogByWeightFraction(double logByWeightFraction) {
            this.logByWeightFraction = logByWeightFraction;
        }
        public String getEntityName() {
            return entityName;
        }
        public void setEntityName(String entityName) {
            this.entityName = entityName;
        }
	}
}
