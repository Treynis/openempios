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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.model.Gender;
import org.openhie.openempi.model.Race;

public abstract class AbstractFileLoader implements FileLoader
{
	protected Logger log = Logger.getLogger(AbstractFileLoader.class);

	public final static String MAPPING_FILE = "mappingFile";
	public final static String LOADER_ALIAS = "flexibleDataLoader";
	public final static String SKIP_HEADER_LINE = "skipHeaderLine";
	public final static String IS_IMPORT = "isImport";
	public final static String IS_MASSIVE_INSERT = "isMassiveInsert";
	public final static String PREVIEW_ONLY = "previewOnly";

	public final static String SKIP_HEADER_LINE_DISPLAY = "Skip Header Line";
	public final static String IS_IMPORT_DISPLAY = "Is Import";
	public final static String IS_MASSIVE_INSERT_DISPLAY = "Bulk Import";
	public final static String MAPPING_FILE_DISPLAY = "Mapping File Name";
	public final static String PREVIEW_ONLY_DISPLAY = "Only Preview Import";

	private String loaderAlias;

	private PersonLoaderManager personManager;
	private EntityLoaderManager entityManager;

	private Map<String,Race> raceCacheByName = new HashMap<String,Race>();
	private Map<String,Race> raceCacheByCode = new HashMap<String,Race>();
	private Map<String,Gender> genderCacheByCode = new HashMap<String,Gender>();
	private Map<String,Gender> genderCacheByName = new HashMap<String,Gender>();

	public void init() {
        log.info("Initializing the file loader.");
	}

	public void shutdown() {
	}

	public Race findRaceByCode(String raceCode) {
		Race race = raceCacheByCode.get(raceCode);
		if (race != null) {
			return race;
		}
		race = personManager.getPersonQueryService().findRaceByCode(raceCode);
		if (race != null) {
			raceCacheByCode.put(raceCode, race);
		}
		return race;
	}

	public Race findRaceByName(String raceName) {
		log.trace("Looking up race by race name: " + raceName);
		Race race = raceCacheByName.get(raceName);
		if (race != null) {
			log.trace("Looking up race by race name: " + raceName);
			return race;
		}
		race = personManager.getPersonQueryService().findRaceByName(raceName);
		if (race != null) {
			raceCacheByName.put(raceName, race);
		}
		return race;
	}

	public Gender findGenderByCode(String genderCode) {
		Gender gender = genderCacheByCode.get(genderCode);
		if (gender != null) {
			return gender;
		}
		gender = personManager.getPersonQueryService().findGenderByCode(genderCode);
		if (gender != null) {
			genderCacheByCode.put(genderCode, gender);
		}
		return gender;
	}

	public Gender findGenderByName(String genderName) {
		Gender gender = genderCacheByName.get(genderName);
		if (gender != null) {
			return gender;
		}
		gender = personManager.getPersonQueryService().findGenderByName(genderName);
		if (gender != null) {
			genderCacheByName.put(genderName, gender);
		}
		return gender;
	}

	public String getParameterAsString(String parameterName) {
		Object value = getParameter(parameterName);
		if (value != null) {
			if (value instanceof String) {
				return (String) value;
			}
			return value.toString();
		}
		return "";
	}

	public void setParameter(String parameterName, Object value) {
		if (getEntityLoaderManager() == null) {
			log.warn("Unable to set file loader parameter because the loader manager has not been injected yet.");
			return;
		}
		log.info("Setting the file loader parameter: " + parameterName + " to value: " + value);
		getEntityLoaderManager().getPropertyMap().put(parameterName, value);
	}

	public Object getParameter(String parameterName) {
		if (getEntityLoaderManager() == null) {
			log.warn("Unable to get file loader parameter because the loader manager has not been injected yet.");
			return null;
		}
		return getEntityLoaderManager().getPropertyMap().get(parameterName);
	}

	public FileLoaderResults parseFile(Entity entity, File file) {
		BufferedReader reader = null;
		FileLoaderResults results = new FileLoaderResults();
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			log.error("Unable to read the input file. Error: " + e);
			throw new RuntimeException("Unable to read the input file.");
		}

		boolean skipHeaderLine = false;
		if (getParameter(SKIP_HEADER_LINE) != null) {
			skipHeaderLine = (Boolean) getParameter(SKIP_HEADER_LINE);
		}
		try {
			boolean done = false;
			int lineIndex = 0;
			int rowsImported = 0;
			int rowsErrored = 0;
			while (!done) {
				String line = reader.readLine();
				if (line == null) {
					done = true;
					continue;
				}
				
				// Skip the first line since its a header.
				if (lineIndex == 0 && skipHeaderLine) {
	                lineIndex++;
					continue;
				}

				boolean imported = processLine(entity, line, lineIndex++);
				if (imported) {
					rowsImported++;
				} else {
				    rowsErrored++;
				}
			}
			reader.close();
			results.setRecordProcessed(rowsImported+rowsErrored);
			results.setRecordsLoaded(rowsImported);
			results.setRecordsErrored(rowsErrored);
		} catch (IOException e) {
			log.error("Failed while loading the input file. Error: " + e);
			results.setLoadingFailed(true);
			results.setErrorMessage(e.getMessage());
		}
        return results;
	}

	public String getloaderAlias() {
		return this.loaderAlias;
	}

	public void setLoaderAlias(String loaderAlias) {
		this.loaderAlias = loaderAlias;
	}

	public PersonLoaderManager getPersonLoaderManager() {
		return this.personManager;
	}

	public void setPersonLoaderManager(PersonLoaderManager personManager) {
		this.personManager = personManager;
	}

	public EntityLoaderManager getEntityLoaderManager() {
		return this.entityManager;
	}

	public void setEntityLoaderManager(EntityLoaderManager entityManager) {
		this.entityManager = entityManager;
	}

	protected abstract boolean processLine(Entity entity, String line, int lineIndex);
}
