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
package org.openempi.webapp.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.openempi.webapp.client.UserFileDataService;
import org.openempi.webapp.client.model.UserFileWeb;
import org.openempi.webapp.client.model.FileLoaderConfigurationWeb;
import org.openempi.webapp.client.model.ParameterTypeWeb;
import org.openempi.webapp.server.util.ModelTransformer;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.jobqueue.JobEntryFactory;
import org.openhie.openempi.jobqueue.JobParameterConstants;
import org.openhie.openempi.jobqueue.JobTypeEnum;
import org.openhie.openempi.loader.FileLoader;
import org.openhie.openempi.loader.FileLoaderFactory;
import org.openhie.openempi.loader.FileLoaderManager;
import org.openhie.openempi.loader.FileLoaderResults;
import org.openhie.openempi.model.JobEntry;
import org.openhie.openempi.model.ParameterType;
import org.openhie.openempi.model.User;
import org.openhie.openempi.notification.ObservationEventType;
import org.openhie.openempi.service.UserManager;

public class UserFileDataServiceImpl extends AbstractRemoteServiceServlet implements UserFileDataService
{
    private static final long serialVersionUID = -1633270535015082684L;

    private final static String NOMINAL_FILE_LOADER = "nominalSetDataLoader";
    private final static String CONCURRENT_FILE_LOADER = "concurrentDataLoader";
    private final static String FLEXIBLE_FILE_LOADER = "flexibleDataLoader";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public UserFileWeb addUserFile(UserFileWeb userFile) throws Exception {
        log.debug("Received request to add a new user file entry: " + userFile);

        authenticateCaller();
        try {
            UserManager userService = Context.getUserManager();
            org.openhie.openempi.model.UserFile theFile = ModelTransformer.map(userFile,
                    org.openhie.openempi.model.UserFile.class);
            theFile = userService.saveUserFile(theFile);
            return ModelTransformer.map(theFile, UserFileWeb.class);
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    public List<UserFileWeb> getUserFiles(String username) throws Exception {
        log.debug("Received request to return list of files for user: " + username);

        authenticateCaller();
        try {
            UserManager userService = Context.getUserManager();
            User user = userService.getUserByUsername(username);
            List<org.openhie.openempi.model.UserFile> files = userService.getUserFiles(user);

            return ModelTransformer.mapList(files, UserFileWeb.class);
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    public List<UserFileWeb> getUserFiles(String username, boolean isEntity) throws Exception {
        log.debug("Received request to return list of files for user: " + username);

        authenticateCaller();
        try {
            UserManager userService = Context.getUserManager();
            User user = userService.getUserByUsername(username);
            List<org.openhie.openempi.model.UserFile> files = userService.getUserFiles(user);
            List<org.openhie.openempi.model.UserFile> filteredFiles = new ArrayList<org.openhie.openempi.model.UserFile>();
            for (org.openhie.openempi.model.UserFile file : files) {
                if (file.getIsEntity() == isEntity) {
                    filteredFiles.add(file);
                }
            }
            return ModelTransformer.mapList(filteredFiles, UserFileWeb.class);
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    public void removeUserFile(Integer userFileId) {
        log.debug("Received request to remove user file entry: " + userFileId);

        authenticateCaller();
        try {
            UserManager userService = Context.getUserManager();
            org.openhie.openempi.model.UserFile userFile = new org.openhie.openempi.model.UserFile();
            userFile.setUserFileId(userFileId);
            userService.removeUserFile(userFile);
        } catch (Throwable t) {
            log.error("Failed to execute: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    public String importUserFile(UserFileWeb userFile) throws Exception {

        authenticateCaller();
        String msg = "";
        // log.debug("Received request to import the contents of file entry: " + userFile);
        if (userFile == null || userFile.getUserFileId() == null || userFile.getFilename() == null) {
            msg = "Unable to import file with insufficient identifying attributes.";
            throw new Exception(msg);
        }

//        try {
//            String number = importFileEntry(userFile);
//            int dashIndex = number.indexOf("-");
//            String rowsProcessedStr = number.substring(0, dashIndex);
//            String rowsImportedStr = number.substring(dashIndex + 1, number.length());
//
//            UserManager userService = Context.getUserManager();
//            org.openhie.openempi.model.UserFile userFileFound = userService.getUserFile(userFile.getUserFileId());
//            userFileFound.setImported("Y");
//            userFileFound.setRowsImported(Integer.parseInt(rowsImportedStr));
//            userFileFound.setRowsProcessed(Integer.parseInt(rowsProcessedStr));
//            userService.saveUserFile(userFileFound);
//            msg = "File successfully imported";
//        } catch (Throwable t) {
//            msg = t.getMessage();
//            throw new Exception(msg);
//        }
        org.openhie.openempi.model.Entity entity = ModelTransformer.mapToEntity(userFile.getEntity(),
                org.openhie.openempi.model.Entity.class);
        JobEntry jobEntry = JobEntryFactory.createJobEntry(entity, JobTypeEnum.FILE_IMPORT, "Job to import file " + 
                userFile.getName() + " on behalf of user " + Context.getUserContext().getUser().getUsername());
        addParameters(userFile, jobEntry);
        try {
            jobEntry = Context.getJobQueueService().createJobEntry(jobEntry);
            Context.notifyObserver(ObservationEventType.JOB_QUEUED_EVENT, jobEntry);
            return "File import job has been created.";
        } catch (Throwable t) {
            msg = t.getMessage();
            throw new Exception(msg);
        }
    }

    private void addParameters(UserFileWeb userFile, JobEntry jobEntry) {
        java.util.HashMap<String, Object> fileLoaderMap = userFile.getFileLoaderMap();
        fileLoaderMap.put(JobParameterConstants.FILENAME_PARAM, userFile.getFilename());
        fileLoaderMap.put(JobParameterConstants.FILELOADER_PARAM, userFile.getFileLoaderName());
        fileLoaderMap.put(JobParameterConstants.USERFILEID_PARAM, userFile.getUserFileId());
        for (String key : fileLoaderMap.keySet()) {
            String value = fileLoaderMap.get(key).toString();
            log.debug("Adding job entry parameter: <" + key + "," + value + ">");
            jobEntry.addJobParameter(key, value);
        }
    }

    /*
    private String importFileEntry(UserFileWeb userFile) throws Exception {

        authenticateCaller();
        String msg = "";
        try {
            FileLoaderManager fileLoaderManager = new FileLoaderManager();
            java.util.HashMap<String, Object> fileLoaderMap = userFile.getFileLoaderMap();

            // set map values for importOnly and skipHeaderLine
            java.util.HashMap<String, Object> map = new java.util.HashMap<String, Object>();
            map.put("context", Context.getApplicationContext());

//             * // map.put("isImport", true);
//             *
//             * if( userFile.getImportOnly() ) { fileLoaderManager.setUp(map); } else { fileLoaderManager.setUp(); }
//             *
//             * fileLoaderManager.setSkipHeaderLine(userFile.getSkipHeaderLine()); return
//             * fileLoaderManager.loadFile(userFile.getFilename(), getFileLoaderAlias());

            // file loader configuration
//            fileLoaderManager.setSkipHeaderLine(false);
//            for (String key : fileLoaderMap.keySet()) {
//                if (key.equals("skipHeaderLine")) {
//                    fileLoaderManager.setSkipHeaderLine((Boolean) fileLoaderMap.get(key));
//                }
//                map.put(key, fileLoaderMap.get(key));
//            }

            org.openhie.openempi.model.Entity entity = ModelTransformer.mapToEntity(userFile.getEntity(),
                    org.openhie.openempi.model.Entity.class);
            map.put("entityName", entity.getName());

            if (map.size() > 1) {
                fileLoaderManager.setUp(map);
            } else {
                fileLoaderManager.setUp();
            }

            FileLoaderResults results = fileLoaderManager.loadFile(entity, userFile.getFilename(), userFile.getFileLoaderName());
            return "";
        } catch (Exception e) {
            log.error("Failed to parse and upload the file " + userFile.getFilename() + " due to " + e.getMessage());
            msg = "Failed to import the file " + userFile.getFilename() + " due to file format or file does not exist";
            throw new Exception(msg);
        }
    }
*/

    @Override
    public String dataProfileUserFile(UserFileWeb userFile) throws Exception {

        authenticateCaller();
        String msg = "";
        try {
            FileLoaderManager fileLoaderManager = new FileLoaderManager();

            org.openhie.openempi.model.Entity entity = null;
            if (userFile.getEntity() != null) {
                entity = ModelTransformer.mapToEntity(userFile.getEntity(), org.openhie.openempi.model.Entity.class);
            }

            fileLoaderManager.dataProfile(entity, userFile.getFilename(), userFile.getUserFileId());

            UserManager userService = Context.getUserManager();
            org.openhie.openempi.model.UserFile userFileFound = userService.getUserFile(userFile.getUserFileId());
            userFileFound.setProfiled("Y");
            userFileFound.setProfileProcessed("In Processing");
            userService.saveUserFile(userFileFound);
            msg = "Data profile operation successfully launched";

            return msg;
        } catch (Exception e) {
            log.error("Failed to process data profile  " + userFile.getFilename() + " due to " + e.getMessage());
            msg = "Failed to process data profile" + userFile.getFilename()
                    + " due to file format or file does not exist";
            throw new Exception(msg);
        }
    }

    public List<FileLoaderConfigurationWeb> getFileLoaderConfigurations() {
        Object obj;
        FileLoader fileLoader;

        List<FileLoaderConfigurationWeb> fileLoaderConfigurations = new ArrayList<FileLoaderConfigurationWeb>();

        try {
            obj = Context.getApplicationContext().getBean(NOMINAL_FILE_LOADER); // file loader
            fileLoader = FileLoaderFactory.getFileLoader(Context.getApplicationContext(), NOMINAL_FILE_LOADER);

            FileLoaderConfigurationWeb theLoader = new FileLoaderConfigurationWeb();
            theLoader.setLoaderName(NOMINAL_FILE_LOADER);

            ParameterType[] types = fileLoader.getParameterTypes();
            List<ParameterType> list = Arrays.asList(types);

            List<ParameterTypeWeb> dtos = new java.util.ArrayList<ParameterTypeWeb>(list.size());
            for (ParameterType pt : list) {
                ParameterTypeWeb pw = ModelTransformer.mapToParameterType(pt, ParameterTypeWeb.class);
                dtos.add(pw);
            }
            theLoader.setParameterTypes(dtos);
            fileLoaderConfigurations.add(theLoader);

        } catch (Throwable t) {
        }

        try {
            obj = Context.getApplicationContext().getBean(CONCURRENT_FILE_LOADER); // file loader hp
            fileLoader = FileLoaderFactory.getFileLoader(Context.getApplicationContext(), CONCURRENT_FILE_LOADER);

            FileLoaderConfigurationWeb theLoader = new FileLoaderConfigurationWeb();
            theLoader.setLoaderName(CONCURRENT_FILE_LOADER);

            ParameterType[] types = fileLoader.getParameterTypes();
            List<ParameterType> list = Arrays.asList(types);

            List<ParameterTypeWeb> dtos = new java.util.ArrayList<ParameterTypeWeb>(list.size());
            for (ParameterType pt : list) {
                ParameterTypeWeb pw = ModelTransformer.mapToParameterType(pt, ParameterTypeWeb.class);
                dtos.add(pw);
            }
            theLoader.setParameterTypes(dtos);
            fileLoaderConfigurations.add(theLoader);

        } catch (Throwable t) {
        }

        try {
            obj = Context.getApplicationContext().getBean(FLEXIBLE_FILE_LOADER); // file loader map
            fileLoader = FileLoaderFactory.getFileLoader(Context.getApplicationContext(), FLEXIBLE_FILE_LOADER);

            FileLoaderConfigurationWeb theLoader = new FileLoaderConfigurationWeb();
            theLoader.setLoaderName(FLEXIBLE_FILE_LOADER);

            ParameterType[] types = fileLoader.getParameterTypes();
            List<ParameterType> list = Arrays.asList(types);

            List<ParameterTypeWeb> dtos = new java.util.ArrayList<ParameterTypeWeb>(list.size());
            for (ParameterType pt : list) {
                ParameterTypeWeb pw = ModelTransformer.mapToParameterType(pt, ParameterTypeWeb.class);
                dtos.add(pw);
            }
            theLoader.setParameterTypes(dtos);
            fileLoaderConfigurations.add(theLoader);

        } catch (Throwable t) {
        }

        return fileLoaderConfigurations;
    }

    private String getFileLoaderAlias() {
        try {
            Object obj = Context.getApplicationContext().getBean(CONCURRENT_FILE_LOADER);
            if (obj != null) {
                return CONCURRENT_FILE_LOADER;
            }
            return NOMINAL_FILE_LOADER;
        } catch (Throwable t) {
            return NOMINAL_FILE_LOADER;
        }
    }
}
