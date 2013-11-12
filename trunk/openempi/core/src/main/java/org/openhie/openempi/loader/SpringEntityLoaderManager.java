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
package org.openhie.openempi.loader;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.DataAccessIntent;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.entity.IntentMassiveInsert;
import org.openhie.openempi.entity.RecordManagerService;
import org.openhie.openempi.entity.RecordQueryService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;

public class SpringEntityLoaderManager implements EntityLoaderManager
{
	protected Logger log = Logger.getLogger(SpringEntityLoaderManager.class);
	
	private String username;
	private String password;
	private boolean isImport = Boolean.FALSE;
	private boolean isMassiveInsert = Boolean.FALSE;
	private Entity entity = null;
	private boolean previewOnly = Boolean.FALSE;
	private RecordManagerService entityManagerService;
	private RecordQueryService entityQueryService;
	private Map<String, Object> propertyMap;
	
	public void setupConnection(Map<String, Object> properties) {
		
		if (entityManagerService == null) {
			entityManagerService = Context.getRecordManagerService();
		}
		if (entityQueryService == null) {
			entityQueryService = Context.getRecordQueryService();
		}
		propertyMap = properties;
		if (properties != null) {
			
			Object previewOnlyFlag = properties.get("previewOnly");
			if (previewOnlyFlag != null  && previewOnlyFlag instanceof Boolean && previewOnlyFlag.equals(Boolean.TRUE)) {
				previewOnly = true;
			}

			Object isImportFlag = properties.get("isImport");
			if (isImportFlag != null  && isImportFlag instanceof Boolean && isImportFlag.equals(Boolean.TRUE)) {
				log.info("Will be doing an import instead of an add");
				isImport = true;
			}
			
			Object entityName = properties.get("entityName");
            if (entityName != null  && entityName instanceof String) {
                log.info("Will be doing an import for entity " + entityName);
                entity = findLatestEntityVersionByName(Context.getEntityDefinitionManagerService(), (String) entityName);
            }
            if (entity == null) {
                log.error("Unable to setup the file loader due to missing entity name.");
                throw new RuntimeException("Unable to setup the file loader due to unspecified entity.");
            }
			
			Object isMassive = properties.get("isMassiveInsert");
			if (isMassive != null && isMassive instanceof Boolean && isMassive.equals(Boolean.TRUE)) {
                log.info("Will be doing a massive import");
			    isMassiveInsert = Boolean.TRUE;
                entityManagerService.declareIntent(entity, new IntentMassiveInsert());
			}
		}
	}

	public Map<String,Object> getPropertyMap() {
		return propertyMap;
	}
	
	public void shutdownConnection() {
	    if (isMassiveInsert) {
	        entityManagerService.declareIntent(entity, null);
	    }
	}

	public Record addRecord(Entity entity, Record record) throws ApplicationException {
		
		if (previewOnly) {
			log.debug("Importing record: " + record);
			return record;
		}
		
		if (isImport) {
			return entityManagerService.importRecord(entity, record);
		} else {
			return entityManagerService.addRecord(entity, record);
		}
	}

    public Set<Record> addRecords(Entity entity, Collection<Record> records) throws ApplicationException {
        
        if (records == null || records.size() == 0) {
            return new HashSet<Record>();
        }
        
        if (previewOnly) {
            log.debug("Importing " + records.size() + " records.");
            return new HashSet<Record>();
        }
        
        if (isImport) {
            return entityManagerService.importRecords(entity, records);
        } else {
            return entityManagerService.addRecords(entity, records);
        }
    }

    public void declareIntent(Entity entity, DataAccessIntent intent) {
        entityManagerService.declareIntent(entity, intent);
    }
    
    
    private Entity findLatestEntityVersionByName(EntityDefinitionManagerService entityDefService, String name) {
        List<Entity> entities = entityDefService.findEntitiesByName(name);
        if (entities.size() == 0) {
            return null;
        }
        if (entities.size() == 1) {
            return entities.get(0);
        }
        Entity latestEntity = entities.get(0);
        for (Entity entity : entities) {
            if (entity.getEntityVersionId() > latestEntity.getEntityVersionId()) {
                latestEntity = entity;
            }
        }
        return latestEntity;
    }
    
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public RecordManagerService getEntityManagerService() {
		return entityManagerService;
	}

	public void setEntityManagerService(RecordManagerService entityManagerService) {
		this.entityManagerService = entityManagerService;
	}

	public RecordQueryService getEntityQueryService() {
		return entityQueryService;
	}

	public void setEntityQueryService(RecordQueryService entityQueryService) {
		this.entityQueryService = entityQueryService;
	}
}
