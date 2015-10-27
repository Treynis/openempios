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
package org.openhie.openempi.matching;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.Constants;
import org.openhie.openempi.configuration.ComparatorFunction;
import org.openhie.openempi.configuration.Configuration;
import org.openhie.openempi.configuration.ConfigurationLoader;
import org.openhie.openempi.configuration.MatchField;
import org.openhie.openempi.configuration.MatchRule;
import org.openhie.openempi.configuration.xml.MatchingConfigurationType;
import org.openhie.openempi.configuration.xml.MpiConfigDocument;
import org.openhie.openempi.configuration.xml.exactmatching.ExactMatchingType;
import org.openhie.openempi.configuration.xml.impl.MpiConfigDocumentImpl.MpiConfigImpl;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Nationality;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordLink;
import org.openhie.openempi.service.BaseServiceTestCase;
import org.openhie.openempi.service.IdentifierDomainService;

public class MatchingServiceWithMatchingRulesTest extends BaseServiceTestCase
{
	static {
		System.setProperty(Constants.OPENEMPI_EXTENSION_CONTEXTS_FILENAME, "openempi-extension-contexts.properties");
		System.setProperty(Constants.OPENEMPI_CONFIGURATION_FILENAME, "mpi-config.xml");
	}
	
	private static java.util.List<Record> persons = new java.util.ArrayList<Record>();


	   
	public void testMatchingService() {
		System.out.println("Transactional support for this test has rollback set to " + this.isDefaultRollback());
		this.setDefaultRollback(true);
		
		loadingOfExactMatchingConfigurationFromFile();
		
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        IdentifierDomainService identifierDomainService = Context.getIdentifierDomainService();
        
        RecordManagerService manager = Context.getRecordManagerService();
        RecordQueryService queryManager = Context.getRecordQueryService();
        
        List<Entity> entities = defService.findEntitiesByName("person");
        if (entities.size() == 0) {
            log.debug("person entity not defined in the database for this to work.");
            return;
        }
        Entity entity = entities.get(0);

        IdentifierDomain domain = identifierDomainService.findIdentifierDomainByName("NIST2010");
        if (domain == null) {
            assertTrue("There are no NIST2010 identifier domain in the system.", true);
            return;
        }
        
        try {       
             
             // rec1: John, Smith, Reston 
             Record record1 = new Record(entity);
             record1.set("givenName", "John");
             record1.set("familyName", "Smith");
             record1.set("city", "Reston");
             record1.set("address1", "1");
             addRecordIdentifier(record1, domain, "test1");
             record1 = manager.addRecord(entity, record1);
             persons.add(record1);  
             
             assertNotNull(record1);
             
             // rec2: John, Smith, Herndon              
             Record record2 = new Record(entity);
             record2.set("givenName", "John");
             record2.set("familyName", "Smith");
             record2.set("city", "Herndon");
             record2.set("address1", "2");
             addRecordIdentifier(record2, domain, "test2");
             record2 = manager.addRecord(entity, record2);
             persons.add(record2); 
             
             assertNotNull(record2);

             List<Record> links = queryManager.loadRecordLinksByRecordId(entity, record2.getRecordId());
             assertNotNull("The list of links must not be null.", links);
             for (Record record : links) {
                 if (record.getRecordId() == record1.getRecordId()) {
                     log.info("record2 found link with record1: " + record.getRecordId());
                 }
             }
             
             // rec3: Robert, Jones, Reston             
             Record record3 = new Record(entity);
             record3.set("givenName", "Robert");
             record3.set("familyName", "Jones");
             record3.set("city", "Reston");
             record3.set("address1", "3");
             addRecordIdentifier(record3, domain, "test3");
             record3 = manager.addRecord(entity, record3);
             persons.add(record3); 
             
             assertNotNull(record3);
             
             // rec4: Bob, Jones, Reston             
             Record record4 = new Record(entity);
             record4.set("givenName", "Bob");
             record4.set("familyName", "Jones");
             record4.set("city", "Reston");
             record4.set("address1", "4");
             addRecordIdentifier(record4, domain, "test4");
             record4 = manager.addRecord(entity, record4);
             persons.add(record4); 
             
             assertNotNull(record4);

             links = queryManager.loadRecordLinksByRecordId(entity, record4.getRecordId());
             assertNotNull("The list of links must not be null.", links);
             for (Record record : links) {
                 if (record.getRecordId() == record3.getRecordId()) {
                     log.info("record4 found link with record3: " + record.getRecordId());
                 }
             }
             
             // delete records
             clearData();
                
        } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
        }
	}

    public void loadingOfExactMatchingConfigurationFromFile() {
        try {
            MpiConfigDocument configDocument = loadConfigurationFromSource();
            MpiConfigImpl mpiconfig = (MpiConfigImpl) configDocument.getMpiConfig();
            // log.debug("Matching Configuration is: " + configDocument.getMpiConfig().getMatchingConfigurationArray());

            EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
            List<Entity> entities = defService.findEntitiesByName("person");
            if (entities.size() == 0) {
                log.debug("person entity not defined in the database for this to work.");
                return;
            }
            Entity entity = entities.get(0);

            
            int count = mpiconfig.getMatchingConfigurationArray().length;
            ExactMatchingType matchingConfig = null;
            for (int i = 0; i < count; i++) {
                MatchingConfigurationType type = mpiconfig.getMatchingConfigurationArray(i);
                if (type instanceof ExactMatchingType) {
                    ExactMatchingType matchingType = (ExactMatchingType) type;
                    String entityName = matchingType.getEntityName();
                    if (entityName.equals(entity.getName())) {
                        matchingConfig = (ExactMatchingType) type;
                        break;
                    }
                }  
            }
            if (matchingConfig == null || matchingConfig.getMatchRules().sizeOfMatchRuleArray() == 0) {
                log.debug("No matching rules were configured; probably a configuration issue.");
                return;
            }
              
            for (int i = 0; i < matchingConfig.getMatchRules().sizeOfMatchRuleArray(); i++) {
                org.openhie.openempi.configuration.xml.exactmatching.MatchRule rule = matchingConfig.getMatchRules().getMatchRuleArray(i);
                MatchRule matchRule = new MatchRule();
                log.debug("Rule: ");
                for (int j = 0; j < rule.getMatchFields().sizeOfMatchFieldArray(); j++) {
                    org.openhie.openempi.configuration.xml.exactmatching.MatchField field = rule.getMatchFields().getMatchFieldArray(j);
                    log.debug("field named " + field.getFieldName());
                }
            }
                        
        } catch (XmlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void clearData() throws Exception {
        RecordManagerService manager = Context.getRecordManagerService();
        
        EntityDefinitionManagerService defService = Context.getEntityDefinitionManagerService();
        List<Entity> entities = defService.findEntitiesByName("person");
        if (entities.size() == 0) {
            log.debug("person entity not defined in the database for this to work.");
            return;
        }
        Entity entity = entities.get(0);
        
        try {
            for (Record person : persons) {
                log.debug("Deleting person: " + person);
                manager.deleteRecord(entity, person);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            persons.clear();
        }
    }
    
    private void addRecordIdentifier(Record record, IdentifierDomain domain, String identifier) {
        Identifier id = new Identifier();
        id.setIdentifier(identifier);
        id.setRecord(record);
        id.setIdentifierDomain(domain);
        record.addIdentifier(id);
    }  
 
    private MpiConfigDocument loadConfigurationFromSource() throws XmlException, IOException {
        File file = getDefaultConfigurationFile();
        log.debug("Checking for presence of the configuration in file: " + file.getAbsolutePath());
        if (file.exists() && file.isFile()) {
            log.info("Loading configuration from file: " + file.getAbsolutePath());
            return MpiConfigDocument.Factory.parse(file);
        }
        
        URL fileUrl = Configuration.class.getResource("mpi-config.xml");
        if (fileUrl != null) {
            log.info("Loading configuration from URL: " + fileUrl);
            return MpiConfigDocument.Factory.parse(fileUrl);
        }
        return null;
    }
    
    private File getDefaultConfigurationFile() {
        File dir = new File(Context.getOpenEmpiHome() + "/conf");
        System.out.println(System.getProperty("OPENEMPI_HOME"));
        File file = new File(dir, "mpi-config.xml");
        return file;
    }
}