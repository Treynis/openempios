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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.context.UserContext;
import org.openhie.openempi.model.AttributeDatatype;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.EntityAttribute;
import org.openhie.openempi.model.EntityAttributeDatatype;
import org.openhie.openempi.model.FormEntryDisplayType;
import org.openhie.openempi.model.Identifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.ParameterType;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.Record;

public class ConcurrentFileLoader extends AbstractFileLoader
{
    public static final String LOADER_ALIAS = "concurrentDataLoader";
    public static final String SKIP_HEADER_LINE = "skipHeaderLine";
    public static final String IS_IMPORT = "isImport";
    public static final String SKIP_HEADER_LINE_DISPLAY = "Skip Header Line";
    public static final String IS_IMPORT_DISPLAY = "Is Import";

    private static final int MAX_FIELD_COUNT = 32;

    private String[] attributeFields = new String[MAX_FIELD_COUNT];

    private UserContext userContext;

    public ConcurrentFileLoader() {
        userContext = Context.getUserContext();
    }

    @Override
    public void init() {
        log.info("Initializing the Concurrent File Loader.");
    }

    @Override
    protected boolean processLine(Entity entity, String line, int lineIndex) {
        RecordParseTask parser = new RecordParseTask(Context.getUserContext(), entity, line, lineIndex);
        try {
            Future<Object> future = Context.scheduleRunnable(parser);
            future.get();
            return true;
        } catch (InterruptedException e) {
            log.warn("Interrupted while launching a line processing task from a file: " + e, e);
            return false;
        } catch (ExecutionException e) {
            return false;
        }
    }

    public void loadEntityRecord(Serializable key, Entity entity, Record record) {

        try {
            synchronized(userContext) {
                Record theRecord = getEntityLoaderManager().addRecord(entity, record);
                if (log.isTraceEnabled()) {
                    log.trace("Loaded the record: " + theRecord);
                }
                if (key != null) {
    //              generateKnownLinks(key, record);
                }
            }
        } catch (ApplicationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shutdown() {
        log.info("Shutting down the concurrent file loader.");
    }

    private class RecordParseTask implements Runnable
    {

        private static final String IGNORE_ATTRIBUTE_VALUE = "<ignore>";
        private String lineRecord;
        private int lineIndex;
        private Entity entityModel;
        private Person person;
        private Record record;
        private UserContext userContext;
        
        public RecordParseTask(UserContext userContext, Entity entity, String lineRecord, int lineIndex) {
            this.entityModel = entity;
            this.lineRecord = lineRecord;
            this.lineIndex = lineIndex;
            this.userContext = userContext;
        }

        public void run() {
            log.info("Processing record: " + lineRecord);
            boolean skipHeaderLine = false;
            Context.setUserContext(userContext);
            
            if (getParameter(SKIP_HEADER_LINE) != null) {
                skipHeaderLine = (Boolean) getParameter(SKIP_HEADER_LINE);
            }

            if ((!skipHeaderLine && lineIndex == 0) || (skipHeaderLine && lineIndex == 1)) {
                attributeFields = loadFieldValues(lineRecord);
                attributeFields = fixAttributeFields(attributeFields);
                return;
            }

            record = processLine(lineRecord, lineIndex);
            if (record == null) {
                log.warn("Unable to process record at line: " + lineIndex);
                throw new RuntimeException("Invalid record at line " + lineIndex);
            }

            Serializable key = null;
            loadEntityRecord(key, entityModel, record);

            if (log.isDebugEnabled()) {
                log.debug("Created the person: " + person);
            }
        }

        private String[] fixAttributeFields(String[] fields) {
            for (int i=0; i < fields.length; i++) {
                log.warn("Fields[" + i + "]=" + fields[i]);
                if (fields[i] == null || fields[i].trim().length() == 0) {
                    fields[i] = IGNORE_ATTRIBUTE_VALUE;
                }
            }
            return fields;
        }

        protected String[] loadFieldValues(String line) {
            String[] fields = new String[MAX_FIELD_COUNT];
            int length = line.length();
            int begin = 0;
            int end = 0;
            int fieldIndex = 0;
            // If the line ends with the delimiter then the splitter misses the last field.
            if (line.endsWith(",")) {
                line = line + " ";
            }
            while (end < length) {
                while (end < length - 1 && line.charAt(end) != ',') {
                    end++;
                }
                if (end == length - 1) {
                    break;
                }
                String fieldValue = line.substring(begin, end);
                if (fieldValue != null && fieldValue.length() > 0) {
                    fieldValue = fieldValue.trim();
                }
                fields[fieldIndex++] = fieldValue;
                end++;
                begin = end;
            }
            fields[fieldIndex] = line.substring(begin, end + 1);
            return fields;
        }

        protected Record processLine(String line, int lineIndex) {
            log.debug("Needs to parse the line " + line);
            try {
                Record record = getEntityRecord(line);
                return record;
            } catch (ParseException e) {
                log.warn("Failed to parse file line: " + line + " due to " + e);
                return null;
            }
        }

        private Record getEntityRecord(String line) throws ParseException {
            if (line == null || line.length() == 0) {
                return null;
            }
            String[] fields = loadFieldValues(line);

            // create Record
            Record record = new Record(entityModel);
            for (int i = 0; i < fields.length; i++) {
                if (fields[i] != null) {
                    String attributeName = attributeFields[i];
                    if (attributeName.equals(IGNORE_ATTRIBUTE_VALUE)) {
                        if (log.isTraceEnabled()) {
                            log.trace("Skipping column " + i + " since metadata specified that it should be ignore.");
                        }
                        continue;
                    }
                    String field = fields[i];

                    if (attributeName.equals("identifier")) {
                        Identifier identifier = extractIdentifier(field, ":");
                        if (identifier != null) {
                            identifier.setRecord(record);
                            record.addIdentifier(identifier);
                        }
                        continue;
                    }

                    EntityAttribute attribute = entityModel.findAttributeByName(attributeName);
                    if (attribute != null) {
                        EntityAttributeDatatype type = attribute.getDatatype();
                        AttributeDatatype attributeDatatype = AttributeDatatype.getById(type.getDatatypeCd());

                        Object value = buildFieldValue(attributeName, attributeDatatype, field);
                        record.set(attributeName, value);
                    }
                }
            }
            return record;
        }

        private Object buildFieldValue(String name, AttributeDatatype datatype, String token) {
            if (datatype == AttributeDatatype.STRING) {
                return token;
            }
            if (datatype == AttributeDatatype.INTEGER) {
                try {
                    Integer value = Integer.parseInt(token);
                    return value;
                } catch (NumberFormatException e) {
                    log.error("For field in position " + name + " expected integer but found a value of: '" + token
                            + "'");
                    return null;
                }
            }
            if (datatype == AttributeDatatype.SHORT) {
                try {
                    Short value = Short.parseShort(token);
                    return value;
                } catch (NumberFormatException e) {
                    log.error("For field in position " + name + " expected short but found a value of: '"
                            + token + "'");
                    return null;
                }
            }
            if (datatype == AttributeDatatype.LONG) {
                try {
                    Long value = Long.parseLong(token);
                    return value;
                } catch (NumberFormatException e) {
                    log.error("For field in position " + name + " expected long but found a value of: '" + token + "'");
                    return null;
                }
            }
            if (datatype == AttributeDatatype.DOUBLE) {
                try {
                    Double value = Double.parseDouble(token);
                    return value;
                } catch (NumberFormatException e) {
                    log.error("For field in position " + name + " expected double but found a value of: '" + token
                            + "'");
                    return null;
                }
            }
            if (datatype == AttributeDatatype.FLOAT) {
                try {
                    Float value = Float.parseFloat(token);
                    return value;
                } catch (NumberFormatException e) {
                    log.error("For field in position " + name + " expected float but found a value of: '"
                            + token + "'");
                    return null;
                }
            }
            if (datatype == AttributeDatatype.BOOLEAN) {
                try {
                    Boolean value = Boolean.parseBoolean(token);
                    return value;
                } catch (NumberFormatException e) {
                    log.error("For field in position " + name + " expected blloean but found a value of: '" + token
                            + "'");
                    return null;
                }
            }
            if (datatype == AttributeDatatype.DATE || datatype == AttributeDatatype.TIMESTAMP) {
                String dateFormatString = "yyyyMMdd";
                if (datatype == AttributeDatatype.TIMESTAMP) {
                    dateFormatString = "yyyyMMddHHmmss";
                }

                try {
                    SimpleDateFormat format = new SimpleDateFormat(dateFormatString);
                    Date date = format.parse(token);
                    return date;
                } catch (ParseException e) {
                    log.error("For field in position " + name + " expected a date with format " + dateFormatString
                            + " but found a value of: '" + token + "'");
                    return null;
                }
            }
            log.error("Cannot handle token with value '" + token + "' and of type: " + datatype);
            return null;
        }

        /**
         * The assumption is that the identifier has the following format:
         * identifier:namespaceIdentifier:universalIdentifier:universalIdentifierTypeCode
         *
         * @param identifier
         *            ,delimeter
         * @return
         */
        private Identifier extractIdentifier(String identifier, String delimeter) {
            if (identifier == null || identifier.trim().length() == 0) {
                return null;
            }
            String[] idComponents = identifier.split(delimeter);
            // We handle two cases here.
            // 1. if there is one component then we set the identifier domain name to 'NID';
            // 2. if there are two components or more components then we assume the second component is the identifier domain name
            //
            Identifier id = new Identifier();
            IdentifierDomain idDomain = new IdentifierDomain();
            id.setIdentifierDomain(idDomain);
            if (idComponents.length == 1) {
                id.setIdentifier(idComponents[0]);
                idDomain.setIdentifierDomainName("NID");
            } else {
                id.setIdentifier(idComponents[0]);
                idDomain.setIdentifierDomainName(idComponents[0]);                
            }
            return id;
        }

    }

    public ParameterType[] getParameterTypes() {
        List<ParameterType> types = new ArrayList<ParameterType>();
        Boolean[] trueOrFalse = {Boolean.TRUE, Boolean.FALSE};
        types.add(new ParameterType(SKIP_HEADER_LINE, SKIP_HEADER_LINE_DISPLAY, FormEntryDisplayType.CHECK_BOX,
                trueOrFalse));
        types.add(new ParameterType(IS_IMPORT, IS_IMPORT_DISPLAY, FormEntryDisplayType.CHECK_BOX, trueOrFalse));
        return types.toArray(new ParameterType[] {});
    }
}
