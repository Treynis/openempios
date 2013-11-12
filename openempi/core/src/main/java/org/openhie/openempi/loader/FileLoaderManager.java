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

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;
import org.openhie.openempi.model.Entity;
import org.openhie.openempi.profiling.DataProfilerLoader;
import org.openhie.openempi.util.BaseSpringApp;

public class FileLoaderManager extends BaseSpringApp
{
	protected static Logger log = Logger.getLogger(FileLoaderManager.class);
	private EntityLoaderManager entityLoaderMgr;
	
	private FileLoader loader;

	private boolean skipHeaderLine = Boolean.FALSE;
	
	public FileLoaderManager() {
		
	}
	
	public void setUp() {
		setUp(null);
	}
	
	public void setUp(java.util.Map<String,Object> map) {
		startup();
		entityLoaderMgr = (EntityLoaderManager) Context.getApplicationContext().getBean("entityLoaderManager");
		entityLoaderMgr.setupConnection(map);
	}
	
	public String loadFile(Entity entity, String filename, String loaderAlias) {
		 File file = new File(filename);
		 log.debug("Loading file " + file.getAbsolutePath());
		 if (!file.isFile() || !file.canRead()) {
			 log.error("Input file is not available.");
			 throw new RuntimeException("Input file " + filename + " is not readable.");
		 }
		 loader = FileLoaderFactory.getFileLoader(Context.getApplicationContext(), loaderAlias);
		 loader.setLoaderAlias(loaderAlias);
		 loader.setEntityLoaderManager(entityLoaderMgr);
		 loader.init();
		 return loader.parseFile(skipHeaderLine, entity, file);
	}
	
	public String dataProfile(Entity entity, String filename, Integer userFileId) {
		 File file = new File(filename);
		 log.debug("Loading file " + file.getAbsolutePath());
		 if (!file.isFile() || !file.canRead()) {
			 log.error("Input file is not available.");
			 throw new RuntimeException("Input file " + filename + " is not readable.");
		 }
		
		 DataProfilerLoader dataProfilerLoader = new DataProfilerLoader();			
		 return dataProfilerLoader.parseFile(entity, file, userFileId);
	}
	
	public static void main(String[] args) {
		if (args.length < 3) {
			usage();
			System.exit(-1);
		}
		String entityname = args[0];
		String filename = args[1];
		String loaderAlias = args[2];
		Boolean isImport = Boolean.FALSE;
		if (args.length > 3 && args[3] != null && args[3].equalsIgnoreCase("true")) {
			isImport = Boolean.TRUE;
		}
		String mappingFile = "";
		if (args.length > 4 && args[4] != null) {
			mappingFile = args[4];
		}
		Boolean previewOnly = Boolean.FALSE;
		if (args.length > 5 && args[5] != null && args[5].equalsIgnoreCase("true")) {
			previewOnly = Boolean.TRUE;
		}
		
		log.info("Loading the data file " + filename + " using loader " + loaderAlias);	
		
		java.util.HashMap<String,Object> map = new java.util.HashMap<String,Object>();
		Context.startup();
		Context.authenticate("admin", "admin");

		EntityDefinitionManagerService entityDefinitionManagerService = Context.getEntityDefinitionManagerService();		
		List<Entity> entities = entityDefinitionManagerService.findEntitiesByName(entityname);
		if( entities == null || entities.size() == 0) {
			log.error("Entity model is not found");
			return;
		}
		
		map.put("context", Context.getApplicationContext());
		map.put("isImport", isImport);
		map.put("mappingFile", mappingFile);
		map.put("previewOnly", previewOnly);
		try {
			FileLoaderManager fileLoaderManager = new FileLoaderManager();
			fileLoaderManager.setUp(map);
			fileLoaderManager.loadFile(entities.get(0), filename, loaderAlias);
			fileLoaderManager.shutdown();
		} catch (Throwable t) {
			log.error("Got an exception: " + t);
		}
	}

	public java.lang.Boolean getSkipHeaderLine() {
		return skipHeaderLine;
	}

	public void setSkipHeaderLine(java.lang.Boolean skipHeaderLine) {
		this.skipHeaderLine = skipHeaderLine;
	}	
	
	public void shutdown() {
		loader.shutdown();
		super.shutdown();
	}

	public static void usage() {
		System.out.println("Usage: " + FileLoaderManager.class.getName() + " <entity-model> <file-to-load> <loader-alias> [<import-data-boolean-flag>] [mapping-file] [preview-only]");
	}
}
