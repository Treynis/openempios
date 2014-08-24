package org.openhie.openempi.entity.impl;

import java.util.Map;
import java.util.Set;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.Constants;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.IdentifierUpdateEvent;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.service.Parameterizable;

public class DirtyRecordMatcher extends RecordCommonServiceImpl implements Runnable, Parameterizable
{
    private java.util.Map<String, Object> parameters;
    private String entityName;
    private Entity entity;
    private Integer maxRecordsPerRun;
    private String username;
    private String password;
    private boolean initialized = false;

    public void run() {
        if (!initialized) {
            try {
                initialize();
            } catch (Exception e) {
                log.error("Unable to initialize the dirty record matcher.");
                return;
            }
        }
        
        Set<Record> records = getEntityDao().loadDirtyRecords(entity, maxRecordsPerRun);
        if (records.size() == 0) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Performing asynchronous matching on " + records.size() + " records.");
        }
        for (Record record : records) {
            Object dateChanged = record.get(org.openhie.openempi.entity.Constants.DATE_CHANGED_PROPERTY);
            if (log.isDebugEnabled()) {
                log.debug("Asynchronous match processing of record: " + record.getRecordId());
            }
            String eventSource = null;
            try {
                if (dateChanged == null) {
                    eventSource = IdentifierUpdateEvent.ADD_SOURCE;
                    RecordState state = new RecordState(record.getRecordId(), eventSource);
                    findAndProcessAddRecordLinks(entity, record, state);
                    getUpdateEventNotificationGenerator().generateEvents(state);
                } else {
                    eventSource = IdentifierUpdateEvent.UPDATE_SOURCE;
                    RecordState state = new RecordState(record.getRecordId(), eventSource);
                    findAndUpdateRecordLinks(entity, record, state);
                    getUpdateEventNotificationGenerator().generateEvents(state);
                }
                record.setDirty(false);
                if (log.isDebugEnabled()) {
                    log.debug("Updating the dirty flag on the record to false: " + record.getRecordId());
                }
                getEntityDao().updateRecord(entity, record);
            } catch (ApplicationException e) {
                log.error("Failed while trying to match a dirty record: " + e, e);
            } catch (Throwable t) {
                log.error("Failed while trying to match a dirty record: " + t, t);
            }
        }
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    private synchronized void initialize() {
        if (parameters == null || parameters.keySet().size() == 0) {
            throw new RuntimeException("Configuration parameters have not been provided.");
        }

        entityName = (String) parameters.get(Constants.ENTITY_NAME_KEY);
        if (entityName == null || entityName.length() == 0) {
            throw new RuntimeException("Entity name configuration parameter has not been provided.");            
        }
        
        try {
            entity = Context.getEntityDefinitionManagerService().getEntityByName(entityName);
            
            Context.authenticate(getUsername(), getPassword());
        } catch (Exception e) {
            log.error("Failed while initializing the service: " + e, e);
            throw new RuntimeException("Entity name configuration parameter is invalid.");
        }
        initialized = true;
    }

    public Integer getMaxRecordsPerRun() {
        return maxRecordsPerRun;
    }

    public void setMaxRecordsPerRun(Integer maxRecordsPerRun) {
        this.maxRecordsPerRun = maxRecordsPerRun;
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
}
