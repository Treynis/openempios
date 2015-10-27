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
package org.openhie.openempi.entity.dao.orientdb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.dao.IdentifierDomainDao;
import org.openhie.openempi.entity.Constants;
import org.openhie.openempi.entity.dao.EntityDao;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.LinkSource;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.model.RecordLinkState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.orientechnologies.orient.core.Orient;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext-dao.xml",
									"/applicationContext-resources.xml",
                                    "/applicationContext-service.xml"})
public class EntityDaoOrientdbTest extends BaseEntityDaoTest
{
	private EntityDao entityDao;
	private IdentifierDomainDao identifierDomainDao;

    @BeforeClass
	public static void setUpBeforeClass() throws Exception {
		log.info("Setting up the test environment before the tests are run.");
		createTestEntity();
		setupUserContext();
	}

    @Test
	public void test00Init() {
		try {
			getEntityDao().initializeStore(entity, ".");
			IdentifierDomain domain = identifierDomainDao.findIdentifierDomainByName("OpenEMPI");
			assertNotNull("Unable to find the test domain OpenEMPI", domain);
	        createTestRecords(entity, domain);
		} catch (Exception e) {
			log.error("Failed during initialization: " + e, e);
			assertTrue("Failed during initialization: " + e.getMessage(), false);
		}
	}
	
	@Test
	public void test01SaveRecords() {
		for (Record record : testRecords) {
			getEntityDao().saveRecord(entity, record);
		}
	}
	
	@Test
	public void test01SaveRecordsBunch() {
	    getEntityDao().saveRecords(entity, testRecordLinks);
	}

	@Test
	public void test02RecordCount() {
	    Long recordCount = entityDao.getRecordCount(entity);
	    assertTrue("The record count is not correct.", recordCount.intValue() == testRecords.size());
	}
	
    @Test
    public void test03RecordCountByRecord() {
        Record record = testRecords.get(0);
        Long recordCount = entityDao.getRecordCount(entity, record);
        assertTrue("The record count is not correct.", recordCount.intValue() == 1);
    }
    
    @Test
    public void test04RecordCountByIdentifier() {
        Record record = testRecords.get(0);
        Identifier identifier = record.getIdentifiers().get(0);
        assertNotNull("The test record doesn't have an identifier", identifier);
        Long recordCount = entityDao.getRecordCount(entity, identifier);
        assertTrue("The record count is not correct: " + recordCount, recordCount.intValue() == 1);
    }
    
    @Test
    public void test04UpdateRecord() {
        IdentifierDomain domain = identifierDomainDao.findIdentifierDomainByName("OpenEMPI");
        assertNotNull("Unable to find the test domain OpenEMPI", domain);

        Record record = new Record(entity);
        record.set("firstName", "John.0");
        List<Record> records = getEntityDao().findRecordsByAttributes(entity, record);
        assertTrue("Was supposed to find a single record using firstName", records.size() > 0);
        
        Record found = records.get(0);
        
        List<Identifier> ids = found.getIdentifiers();
        assertTrue("Was supposed to have 2 ids: " + ids, (ids != null && ids.size() == 2));
        ids.remove(1);
        Identifier existing = ids.get(0);
        existing.setIdentifier("MRN-3000");
        
        Identifier identifier = new Identifier();
        identifier.setIdentifier("MRN-2000");
        identifier.setIdentifierDomain(domain);
        identifier.setRecord(found);
        found.addIdentifier(identifier);
        
        try {
            found = getEntityDao().updateRecord(entity, found);
            assertTrue("The identifier was not added properly.", found.getIdentifiers().size() == 2);
            for (Identifier id : found.getIdentifiers()) {
                log.debug("Identifier: " + id);
            };
        } catch (Exception e) {
            assertTrue("The update failed with an exception: " + e, false);
        }        
    }
    
    @Test
    public void test05FindRecordByAttributes() {
        Record record = new Record(entity);
        record.set("firstName", "John.0");
        List<Record> records = getEntityDao().findRecordsByAttributes(entity, record);
        assertTrue("Was supposed to find a single record using firstName", records.size() > 0);
        
        record = new Record(entity);
        record.set("lastName", "John.0");
        records = getEntityDao().findRecordsByAttributes(entity, record);
        assertTrue("Was supposed to find a single record using lastName", records.size() > 0);

        record = new Record(entity);
        record.set("age", new Integer(13));
        records = getEntityDao().findRecordsByAttributes(entity, record);
        assertTrue("Was supposed to find a single record using lastName", records.size() > 0);
    }

    @Test
    public void test06LoadRecord() {
        Record record = new Record(entity);
        record.set("firstName", "John.0");
        List<Record> records = getEntityDao().findRecordsByAttributes(entity, record);
        assertTrue("Was supposed to find a single record using firstName", records.size() > 0);
        
        record = records.get(0);
        Record found = getEntityDao().loadRecord(entity, record.getRecordId());
        assertTrue("Was supposed to load the record using the id but didn't", record.equals(found));
    }

    @Test
    public void test07LoadObject() {
        Record record = new Record(entity);
        record.set("firstName", "John.0");
        List<Record> records = getEntityDao().findRecordsByAttributes(entity, record);
        assertTrue("Was supposed to find a single record using firstName", records.size() > 0);
        
        record = records.get(0);
        EntityStore entityStore = getEntityDao().getEntityStoreByName(entity.getName());
        assertNotNull("Can't find the entity store for the test entity.", entityStore);
        Integer clusterId = entityStore.getClusterId(entityStore.getEntityName());
        assertNotNull("Can't find the cluster id for the test entity.", clusterId);
        
        Map<String,Object> odoc = (Map<String,Object>) getEntityDao().loadObject(entity, "#" + clusterId + ":" + record.getRecordId());
        assertTrue("Was supposed to load the object using the id but didn't", odoc.get(Constants.RECORDID_KEY) != null);
    }
    
    @Test
    public void test08CreateLink() {
        Record record = new Record(entity);
        record.set("firstName", "John.%");
        List<Record> records = getEntityDao().findRecordsByAttributes(entity, record);
        log.warn("Found " + records.size() + " records.");
        assertTrue("Was supposed to find a few records using firstName with wildcard.", records.size() == 10);
        
        Record prev = records.get(0);
        for (int i=1; i < records.size(); i++) {
            Record next = records.get(i);
            RecordLink link = createRecordLink(prev, next);
            link = getEntityDao().saveRecordLink(link);
            log.warn("Stored link is: " + link.getRecordLinkId() + " between " + link.getLeftRecord().getRecordId() + " and " + link.getRightRecord().getRecordId());
            prev = next;
        }
    }

    @Test
    public void test09LoadLinks() {
        Record record = new Record(entity);
        record.set("firstName", "John.0");
        List<Record> records = getEntityDao().findRecordsByAttributes(entity, record);
        assertTrue("Was supposed to find a single record using firstName", records.size() > 0);
        
        record = records.get(0);
        List<RecordLink> links = getEntityDao().loadRecordLinks(entity, record.getRecordId());
        log.warn("Found " + links.size() + " links.");
        assertTrue("Was supposed to find 9 record links", links.size() == 9);
    }
    
    @Test
    public void test10LoadLinks() {
        Record record = new Record(entity);
        record.set("firstName", "John.0");
        List<Record> records = getEntityDao().findRecordsByAttributes(entity, record);
        assertTrue("Was supposed to find a single record using firstName", records.size() > 0);
        
        record = records.get(0);
        List<RecordLink> links = getEntityDao().loadRecordLinks(entity, record.getRecordId(), RecordLinkState.MATCH);
        log.warn("Found " + links.size() + " links.");
        assertTrue("Was supposed to find 9 record links in match state", links.size() == 9);
        
        links = getEntityDao().loadRecordLinks(entity, record.getRecordId());
        log.warn("Found " + links.size() + " links.");
        assertTrue("Was supposed to find 9 record links in match state", links.size() == 9);
        
        long count = getEntityDao().getRecordLinkCount(entity, RecordLinkState.MATCH);
        assertTrue("Was supposed to be greater than zero", count > 0);
        
        for (int i=0; i < count; i++) {
            links = getEntityDao().loadRecordLinks(entity, RecordLinkState.MATCH, i, 1);
            assertTrue("Was supposed to get one record link at a time", links.size() == 1);
            log.warn("Found the single link " + links.get(0));
        }
    }
    
    private RecordLink createRecordLink(Record first, Record second) {
        RecordLink link = new RecordLink();
        link.setLeftRecord(first);
        link.setRightRecord(second);
        link.setLinkSource(new LinkSource(LinkSource.MANUAL_MATCHING_SOURCE));
        link.setState(RecordLinkState.MATCH);
        link.setVector(0xff);
        link.setWeight(1.0);
        link.setUserCreatedBy(Context.getUserContext().getUser());
        link.setDateCreated(new Date());
        return link;
    }
    
    @Test
    public void test20RemoveRecord() {
        Record record = new Record(entity);
        record.set("firstName", "John.0");
        List<Record> records = getEntityDao().findRecordsByAttributes(entity, record);
        assertTrue("Was supposed to find a single record using firstName", records.size() > 0);
        
        Record found = records.get(0);
        Long recordId = found.getRecordId();
        try {
            getEntityDao().removeRecord(entity, found);
            found = getEntityDao().loadRecord(entity, recordId);
            assertNull("Found the record even though it had been deleted.", found);
        } catch (ApplicationException e) {
            assertTrue("Failed while removing the record: " + found, false);
        }        
    }
    
	@AfterClass
	public static void setUpAfterClass() throws Exception {
		log.info("Cleaning up the test environment after the tests have run.");
		Orient.instance().shutdown();
		cleanDatabaseDirectory();
	}

	public EntityDao getEntityDao() {
		return entityDao;
	}

	@Autowired
	public void setEntityDao(EntityDao entityDao) {
		this.entityDao = entityDao;
	}
    
    public IdentifierDomainDao getIdentifierDomainDao() {
        return identifierDomainDao;
    }
    
    @Autowired
    public void setIdentifierDomainDao(IdentifierDomainDao identifierDomainDao) {
        this.identifierDomainDao = identifierDomainDao;
    }
}
