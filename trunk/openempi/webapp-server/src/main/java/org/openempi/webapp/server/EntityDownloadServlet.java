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

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.entity.EntityDefinitionManagerService;

public class EntityDownloadServlet extends HttpServlet
{
	private Logger log = Logger.getLogger(getClass());

	private EntityDefinitionManagerService entityDefinitionManagerService;

    @Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if (!Context.isInitialized()) {
			log.error("The context did not initialize properly.");
			return;
		}
		entityDefinitionManagerService = (EntityDefinitionManagerService) Context.getApplicationContext().getBean("entityDefinitionManagerService");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// get the 'file' parameter
		String entityName = (String) request.getParameter("entityName");
		String entityVersionId = (String) request.getParameter("entityVersionId");
		if (entityName == null || entityName.equals("") || entityVersionId == null) {
			log.error("Received request to retrieve entity using invalid entity name: " + entityName);
			throw new ServletException("Invalid or non-existent entity name.");
		}

		ServletOutputStream stream = null;
		BufferedInputStream buf = null;
		try {
			stream = response.getOutputStream();
			String entity = entityDefinitionManagerService.exportEntity(Integer.parseInt(entityVersionId));
			if (entity.isEmpty()) {
				log.error("Received request to retrieve non-existant entity " + entityName);
				throw new ServletException("Entity " + entityName + " does not exist on the server.");
			}
			response.setContentType("application/x-download");
	        response.addHeader("Content-Disposition", "filename=\"" + entityName + "_entity_definition.xml\"");
			response.setContentLength((int) entity.length());

			stream.write(entity.getBytes());

		} catch (Exception e) {
			log.error("Failed while attempting to stream the report data to the client: " + e, e);
			throw new ServletException(e.getMessage());
		} finally {
			if (stream != null) {
				stream.close();
			}
			if (buf != null) {
				buf.close();
			}
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}
