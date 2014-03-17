package org.openhie.openempi.blocking.basicblockinghp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.blocking.basicblockinghp.dao.BlockingDao;
import org.openhie.openempi.blocking.basicblockinghp.dao.BlockingRoundClass;
import org.openhie.openempi.configuration.BlockingRound;
import org.openhie.openempi.configuration.ConfigurationRegistry;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.service.BaseServiceTestCase;

public class RecordPairGenerationTest extends BaseServiceTestCase
{
    public final static String BLOCKINGKEYVALUE_FIELD = "blockingKeyValue";
    public final static String RECORDIDS_FIELD = "rids";
    
    private EntityDao entityDao;
    private BlockingDao blockingHpDao;
    
    @SuppressWarnings("unchecked")
    public void testRecordPairGeneration() {
        try {
            // 1. Get the entity that we will work with.
            Entity entity = getTestEntity();
            log.info("Working with entity " + entity.getName());
            
            Map<String,Object> config = (Map<String, Object>) Context.lookupConfigurationEntry(entity.getName(),
                    ConfigurationRegistry.BLOCKING_CONFIGURATION);
            assertNotNull("The configuration data is blank.", config);
            log.info("Obtained configuration:\n" + config);
            List<BlockingRound> blockingRounds = (List<BlockingRound>)
                    config.get(BasicBlockingConstants.BLOCKING_ROUNDS_REGISTRY_KEY);
            assertNotNull("The blocking rounds data is blank.", config);
            List<BlockingRoundClass> roundClasses = new ArrayList<BlockingRoundClass>();
            long startTime = new Date().getTime();
            for (BlockingRound round : blockingRounds) {
                log.info("The round is called: " + round);
                Entity roundEntity = buildEntityFromRound(round);
                try {
                    entityDao.createClass(entity, roundEntity, "V");
                    BlockingRoundClass roundClass = new BlockingRoundClass(round, roundEntity);
                    roundClasses.add(roundClass);                    
                    Long count = blockingHpDao.loadBlockDataCount(roundEntity, roundClass);
                } catch (ApplicationException e) {
                    assertTrue("Failed while creating the class: " + e, false);
                }
            }
            
            List<Long> recordIds = entityDao.getAllRecordIds(entity);
            if (recordIds != null || recordIds.size() > 0) {
                int count=0;
                for (Long rid : recordIds) {
                    count++;
                    Record record = entityDao.loadRecord(entity, rid);
                    if (record == null) {
                        log.warn("Couldn't find record with iD " + rid);
                        continue;
                    }
                    if (count % 1000 == 0) {
                        log.info("Working on record " + count);
                    }
                    for (BlockingRoundClass roundClass : roundClasses) {
                        String blockingKeyValue = BlockingKeyValueGenerator
                                .generateBlockingKeyValue(roundClass.getBlockingRound().getFields(), record);
                        long startLoadTime = new Date().getTime();
                        Record blockRecord = getBlockingHpDao().loadBlockData(entity, roundClass, blockingKeyValue);
                        if (blockRecord == null) {
                            blockRecord = new Record(roundClass.getRoundClass());
                            Set<Long> rids = new HashSet<Long>();
                            rids.add(record.getRecordId());
                            populateBlockData(blockRecord, blockingKeyValue, rids);
                        } else {
                            Set<Long> rids = (Set<Long>) blockRecord.get(RECORDIDS_FIELD);
                            rids.add(record.getRecordId());
                        }
                        getBlockingHpDao().saveBlockData(entity, roundClass, blockRecord);
                        long endLoadTime = new Date().getTime();
                        log.info("Created a block: " + blockRecord.get(RECORDIDS_FIELD) + " in " + (endLoadTime-startLoadTime) + " msec.");
                    }
                }
            }
            long endTime = new Date().getTime();
            log.info("Generated all data for " + recordIds.size() + " records in " + (endTime-startTime) + " msec.");
            for (BlockingRoundClass roundClassPair : roundClasses) {
                try {
                    entityDao.dropClass(entity, "someClass");
                } catch (ApplicationException e) {
                    assertTrue("Failed while dropping the class: " + e, false);
                }
            }
        } catch (Exception e) {
            log.error("Failed due to : " + e, e);
        }
    }

    private void populateBlockData(Record record, String blockingKeyValue, Set<Long> rids) {
        record.set(BLOCKINGKEYVALUE_FIELD, blockingKeyValue);
        record.set(RECORDIDS_FIELD, rids);
    }

    private Entity buildEntityFromRound(BlockingRound round) {
        Entity entity = new Entity();
        entity.setName("Blocking" + getEntityNameFromRoundName(round.getName()));
        EntityAttribute attrib = new EntityAttribute();
        attrib.setName("blockingKeyValue");
        attrib.setDatatype(new EntityAttributeDatatype(EntityAttributeDatatype.STRING_DATATYPE_CD));
        attrib.setIndexed(true);
        entity.addAttribute(attrib);
        
        attrib = new EntityAttribute();
        attrib.setName("rids");
        attrib.setDatatype(new EntityAttributeDatatype(EntityAttributeDatatype.EMBEDDEDSET_DATATYPE_CD));
        attrib.setIndexed(false);
        entity.addAttribute(attrib);
        return entity;
    }

    private String getEntityNameFromRoundName(String name) {
        // TODO OrientDB cannot handle '.' in the name
        name = name.replace('.', '-');
        return name;
    }

    public EntityDao getEntityDao() {
        return entityDao;
    }

    public void setEntityDao(EntityDao entityDao) {
        this.entityDao = entityDao;
    }

    public BlockingDao getBlockingHpDao() {
        return blockingHpDao;
    }

    public void setBlockingHpDao(BlockingDao blockingDao) {
        this.blockingHpDao = blockingDao;
    }
}
