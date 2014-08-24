package org.openhie.openempi.shallowmatching;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.Constants;
import org.openhie.openempi.InitializationException;
import org.openhie.openempi.configuration.ConfigurationRegistry;
import org.openhie.openempi.configuration.MatchField;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.matching.AbstractMatchingLifecycleObserver;
import org.openhie.openempi.matching.ShallowMatchingService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordPair;
import org.openhie.openempi.stringcomparison.StringComparisonService;

public class ShallowMatchingServiceImpl extends AbstractMatchingLifecycleObserver implements ShallowMatchingService
{    
    private Map<String,List<MatchField>> matchFieldsByEntityName = new HashMap<String,List<MatchField>>();
    private StringComparisonService comparisonService;
    private EntityDao entityDao;

    @Override
    public void startup() throws InitializationException {
        log.info("Straing the shallow matching service.");
        List<Entity> entities = Context.getEntityDefinitionManagerService().loadEntities();
        for (Entity entity : entities) {
            ShallowMatchingService service = Context.getShallowMatchingService(entity.getName());
            if (service == null || service.getMatchingServiceId() != getMatchingServiceId()) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String,Object> configurationData = (Map<String,Object>) Context.getConfiguration()
                    .lookupConfigurationEntry(entity.getName(), ConfigurationRegistry.SHALLOW_MATCH_CONFIGURATION);
            Object obj = configurationData.get(Constants.MATCHING_FIELDS_REGISTRY_KEY);
            if (obj == null) {
                log.warn("Shallow matching service has not been configured properly; no match fields have been defined.");
                continue;
            }

            @SuppressWarnings("unchecked")
            List<MatchField> fields = (List<MatchField>) obj;
            matchFieldsByEntityName.put(entity.getName(), fields);
            comparisonService = Context.getStringComparisonService();
            log.info("Loaded Shallow Matching configuration for entity " + entity.getName());
        }
    }

    @Override
    public int getMatchingServiceId() {
        return LinkSource.SHALLOW_MATCHING_ALGORITHM_SOURCE;
    }

    @Override
    public Set<RecordPair> match(Record record) throws ApplicationException {
        log.info("Evaluating the record for a shallow match.");
        Set<RecordPair> pairs = new HashSet<RecordPair>();
        if (record.getIdentifiers() == null || record.getIdentifiers().size() == 0) {
            return pairs;
        }
        Set<Record> candidates = getCandidates(record);
        if (candidates.size() == 0) {
            return pairs;
        }
        
        String entityName = record.getEntity().getName();
        List<MatchField> fields = (List<MatchField>) matchFieldsByEntityName.get(entityName);
        for (Record candidate : candidates) {
            RecordPair pair = new RecordPair(record, candidate);
            evaluateRecordPair(pair, fields, pairs);
        }
        return pairs;
    }

    private void evaluateRecordPair(RecordPair entry, List<MatchField> fields, Set<RecordPair> pairs) {
        // No need to compare a record pair that consists of two references to the same record.
        if (entry.getLeftRecord().getRecordId() != null && 
                entry.getRightRecord().getRecordId() != null &&
                entry.getLeftRecord().getRecordId().longValue() == entry.getRightRecord().getRecordId().longValue()) {
            return;
        }
        
        for (MatchField matchField : fields) {
            boolean fieldsMatch = isExactMatch(matchField, entry.getLeftRecord(), entry.getRightRecord());
            log.debug("Comparison of records on field " + matchField + " returned " + fieldsMatch);
            if (!fieldsMatch) {
                break;
            }
            log.debug("Adding to matches entry: " + entry);
            entry.setWeight(1.0);
            entry.setMatchOutcome(RecordPair.MATCH_OUTCOME_LINKED);
            entry.setLinkSource(new LinkSource(getMatchingServiceId()));
            entry.setVector((int) Math.pow(2.0, fields.size()) - 1);
            pairs.add(entry);
        }
    }

    private boolean isExactMatch(MatchField matchField, Record left, Record right) {
        String lVal = left.getAsString(matchField.getFieldName());
        String rVal = right.getAsString(matchField.getFieldName());
        if (lVal == null) {
            if (rVal == null) {
                return true;
            }
            return false;
        }
        if (matchField.getComparatorFunction() == null) {
            return lVal.equals(rVal);
        }
        String functionName = matchField.getComparatorFunction().getFunctionName();
        double distance = comparisonService.score(functionName, lVal, rVal);
        if (log.isTraceEnabled()) {
            log.debug("Distance between values " + lVal + " and " + rVal + " computed using comparison function " + 
                    functionName + " was found to be " + distance + " as compared to threshold " + 
                    matchField.getMatchThreshold());
        }
        if (distance > matchField.getMatchThreshold()) {
            return true;
        }
        return false;
    }
    
    private Set<Record> getCandidates(Record record) {
        Set<Record> candidates = new HashSet<Record>();
        for (Identifier id : record.getIdentifiers()) {
            List<Record> found = entityDao.findRecordsByIdentifier(record.getEntity(), id);
            candidates.addAll(found);
        }
        return candidates;
    }

    @Override
    public void shutdown() {
        log.info("Shutting down deterministic matching service.");
    }

    public EntityDao getEntityDao() {
        return entityDao;
    }

    public void setEntityDao(EntityDao entityDao) {
        this.entityDao = entityDao;
    }
}
