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
package org.openhie.openempi.profiling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.openhie.openempi.context.Context;
import org.openhie.openempi.dao.UserDao;
import org.openhie.openempi.model.DataProfileAttribute;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Record;

public class FileRecordDataSource extends AbstractRecordDataSource
{
	private final static String DELIMETER = ":";
	private final static int MAX_FIELD_COUNT = 32;
	private Integer recordBlockSize = 1000;

	private File file;
	private RandomAccessFile randomFile = null;
	private long offset = 0;
	private Integer userFileId = null;
	private List<AttributeMetadata> attribMetadata;

	public FileRecordDataSource() {
	}

	public FileRecordDataSource(String filename, Integer userFileId) {
		file = new File(filename);
		if (!file.isFile() || !file.canRead()) {
			log.error("Input file is not available.");
			throw new RuntimeException("Input file " + filename + " is not readable.");
		}
		this.userFileId = userFileId;

		setRandomAccess();
	}

	public void setRandomAccess() {
		try {
			if (randomFile != null) {
				randomFile.close();
			}

			randomFile = new RandomAccessFile(file, "r");
			String line = randomFile.readLine();
			if (line != null) {
				offset = randomFile.getFilePointer();
			}

		} catch (IOException e) {
			throw new RuntimeException("Input file " + file.getName() + " cannot random access.");
		}
	}

	public boolean isEmpty() {
		Boolean isEmpty = false;
		try {
			if (randomFile.length() == 0) {
				isEmpty = true;
			}
		} catch (Exception e) {
			log.warn("RandomAccessFile.length() : " + e);
		}
		return isEmpty;
	}

	private void saveUserFileInfo(String profileProcessed) {
		UserDao userDao = (UserDao) Context.getApplicationContext().getBean("userDao");
		if (userFileId != null) {
			// UserManager userService = Context.getUserManager();
			org.openhie.openempi.model.UserFile userFileFound = userDao.getUserFile(userFileId);
			userFileFound.setProfiled("Y");
			userFileFound.setProfileProcessed(profileProcessed);
			userDao.saveUserFile(userFileFound);
		}
	}

	public List<Record> getRecordsFromFile(long start, int blockSize) {
		List<Record> records = new java.util.ArrayList<Record>(blockSize);

		try {
			if (randomFile == null) {
				return new java.util.ArrayList<Record>(0);
			}

			boolean done = false;
			int lineCount = 0;
			randomFile.seek(start);
			while (!done) {
				String line = randomFile.readLine();
				if (line == null) {
					done = true;
					randomFile.close();
					randomFile = null;
					continue;
				}
				Record record = processLine(line);
				records.add(record);
				lineCount++;
				if (lineCount == blockSize) {
					done = true;
					offset = randomFile.getFilePointer();
				}
			}

			if (lineCount == 0) {
				return new java.util.ArrayList<Record>(0);
			} else {
				return records.subList(0, lineCount);
			}

		} catch (IOException e) {
			log.error("Failed while loading the input file. Error: " + e);
			throw new RuntimeException("Failed while loading the input file.");
		}
	}

	public List<AttributeMetadata> getAttributeMetadata() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			log.error("Unable to read the input file for data profiling. Error: " + e, e);
			throw new RuntimeException("Unable to read the file for data profiling.");
		}
		
		try {
			String line = reader.readLine();
			if (line == null) {					
				log.error("Unable to read the blank input file for data profiling.");
				throw new RuntimeException("Unable to read the file for data profiling.");
			}
			attribMetadata = loadFieldNameAndType(line, DELIMETER);
			return attribMetadata;
		} catch (IOException e) {
			log.error("Failed while loading the input file for data profiling. Error: " + e, e);
			throw new RuntimeException("Failed while loading the input file for data profiling.");
		} finally {
		    if (reader != null) {
		        try {
                    reader.close();
                } catch (IOException e) {
                }
		    }
		}
	}

	protected List<AttributeMetadata> loadFieldNameAndType(String line, String delimeter) {
		String[] fields = loadFieldValues(line);
		List<AttributeMetadata> metadata = new ArrayList<AttributeMetadata>();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i] != null) {
				String field = fields[i];
				AttributeMetadata attributeMetadata = extractAttributeMetadata(field, delimeter);
				metadata.add(attributeMetadata);
			}
		}
		return metadata;
	}

	private AttributeMetadata extractAttributeMetadata(String attribute, String delimeter) {
		StringTokenizer idTokenizer = new StringTokenizer(attribute, delimeter);
		int count=0;
		String attributeName="";
		int type = -1;
		while (idTokenizer.hasMoreTokens()) {
			String field = idTokenizer.nextToken();
			switch (count) {
			case 0:
				attributeName = field;
				break;
			case 1:
				type = extractDatatypeFromName(attributeName, field);
				break;
			}
			count++;
		}	
		return new AttributeMetadata(attributeName, type);
	}

	private int extractDatatypeFromName(String attribute, String typeName) {
		int datatype = -1;
		if (typeName.equals("String")) {
			datatype = DataProfileAttribute.STRING_DATA_TYPE;
		} else if (typeName.equals("Integer")) {
			datatype = DataProfileAttribute.INTEGER_DATA_TYPE;
		} else if (typeName.equals("Long")) {
			datatype = DataProfileAttribute.LONG_DATA_TYPE;
		} else if (typeName.equals("Float")) {
			datatype = DataProfileAttribute.FLOAT_DATA_TYPE;
		} else if (typeName.equals("Double")) {
			datatype = DataProfileAttribute.DOUBLE_DATA_TYPE;
		} else if (typeName.equals("Date")) {
			datatype = DataProfileAttribute.DATE_DATA_TYPE;
		} else {
			log.warn("Attribute " + attribute + " is of unknown data type " + typeName + " and will be ignored.");
		}
		return datatype;
	}
	
	protected Record processLine(String line) {
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
		Record record = new Record(new Entity());
		for (int i = 0; i < fields.length; i++) {
			if (fields[i] != null) {

				String attributeName = attribMetadata.get(i).getAttributeName();
				String stringValue = fields[i];
				if (!attributeName.contains("<null>")) {
					Object value = buildFieldValue(attributeName, attribMetadata.get(i).getDatatype(), stringValue);
					record.set(attributeName, value);
				}
			}
		}
		return record;
	}

	protected String[] loadFieldValues(String line) {
		String[] fields = new String[MAX_FIELD_COUNT];
		int length = line.length();
		int begin = 0;
		int end = 0;
		int fieldIndex = 0;
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
			if (fieldIndex == MAX_FIELD_COUNT) {
				return fields;				
			}			
		}
		fields[fieldIndex] = line.substring(begin, end + 1);
		return fields;
	}
	
	private Object buildFieldValue(String name, int datatype, String token) {
		if (datatype == DataProfileAttribute.STRING_DATA_TYPE) {
			return token;
		}
		if (datatype == DataProfileAttribute.INTEGER_DATA_TYPE) {
			try {
				Integer value = Integer.parseInt(token);
				return value;
			} catch (NumberFormatException e) {
				log.error("For field in position " + name + " expected integer but found a value of: '" + token + "'");
				return null;
			}
		}
		if (datatype == DataProfileAttribute.LONG_DATA_TYPE) {
			try {
				Long value = Long.parseLong(token);
				return value;
			} catch (NumberFormatException e) {
				log.error("For field in position " + name + " expected long but found a value of: '" + token + "'");
				return null;
			}
		}
		if (datatype == DataProfileAttribute.DOUBLE_DATA_TYPE) {
			try {
				Double value = Double.parseDouble(token);
				return value;
			} catch (NumberFormatException e) {
				log.error("For field in position " + name + " expected double but found a value of: '" + token + "'");
				return null;
			}
		}
		if (datatype == DataProfileAttribute.FLOAT_DATA_TYPE) {
			try {
				Float value = Float.parseFloat(token);
				return value;
			} catch (NumberFormatException e) {
				log.error("For field in position " + name + " expected float but found a value of: '" + token + "'");
				return null;
			}
		}
		if (datatype == DataProfileAttribute.BOOLEAN_DATA_TYPE) {
			try {
				Boolean value = Boolean.parseBoolean(token);
				return value;
			} catch (NumberFormatException e) {
				log.error("For field in position " + name + " expected boolean but found a value of: '" + token + "'");
				return null;
			}
		}
		if (datatype == DataProfileAttribute.DATE_DATA_TYPE) {
			String dateFormatString = "yyyyMMdd";
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
		if (datatype == DataProfileAttribute.TIMESTAMP_DATA_TYPE) {
			String dateFormatString = "yyyyMMddHHmmss";
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

	@Override
	public Iterator<Record> iterator() {
		return new RecordIterator(recordBlockSize);
	}

	public int getRecordDataSourceId() {
		return userFileId;
	}

	public Integer getRecordBlockSize() {
		return recordBlockSize;
	}

	public void setRecordBlockSize(Integer recordBlockSize) {
		this.recordBlockSize = recordBlockSize;
	}

	private class RecordIterator implements Iterator<Record>
	{
		private int blockSize;
		java.util.List<Record> records;
		int currentIndex;
		int startIndex;

		public RecordIterator(int blockSize) {
			this.blockSize = blockSize;
			startIndex = 0;
			currentIndex = -1;

			setRandomAccess();
		}

		public boolean hasNext() {
			if (records != null && currentIndex < records.size()) {
				return true;
			}
			return loadBlockOfRecords(blockSize);
		}

		private boolean loadBlockOfRecords(int blockSize) {
			try {
				log.debug("Loading records from " + startIndex + " to " + (startIndex + blockSize));
				records = getRecordsFromFile(offset, blockSize);
				if (records.size() == 0) {
					return false;
				}
				currentIndex = 0;
				startIndex += blockSize;
				return true;
			} catch (Exception e) {
				log.error("Failed while loading a block of records from the repository: " + e, e);
				return false;
			}
		}

		public Record next() {
			return records.get(currentIndex++);
		}

		public void remove() {
		}
	}

	public void close(String message) {
		saveUserFileInfo(message);
	}
}
