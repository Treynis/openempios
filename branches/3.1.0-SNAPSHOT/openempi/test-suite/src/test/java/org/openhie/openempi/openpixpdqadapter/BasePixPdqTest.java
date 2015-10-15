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
package org.openhie.openempi.openpixpdqadapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.openhealthtools.openexchange.actorconfig.IheConfigurationException;
import org.openhealthtools.openpixpdq.common.PixPdqConfigurationLoader;
import org.openhealthtools.openpixpdq.impl.v2.MessageValidation;
import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.service.PersonManagerService;
import org.openhie.openempi.service.PersonQueryService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionHub;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;

public abstract class BasePixPdqTest extends TestCase
{
    private final static String PIX_MANAGER_PORT_PROPERTY = "pix-manager-port";
    private final static String PDQ_SUPPLIER_PORT_PROPERTY = "pdq-supplier-port";

    public enum Profile {
		PIX, PDQ
	};
	protected Logger log = Logger.getLogger(BasePixPdqTest.class);
	protected String hostname = "localhost";
	protected int port = 3600;
	protected static boolean initialized = false;
	protected int pixManagerPort;
	protected int pdqSupplierPort;
	protected HapiContext ctx;
	
	private Connection pdqConnection;
	private Connection pixConnection;
	private Connection defaultConnection;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Properties prop = new Properties();
		String propFileName = "pixpdqtest.properties";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		
		if (inputStream == null) {
		    log.error("Unable to find the configuration file: "  + propFileName);
		    throw new RuntimeException("Configuration file for tests is missing: " + propFileName);
		}
		prop.load(inputStream);
		
		try {
    		pixManagerPort = Integer.parseInt(prop.getProperty(PIX_MANAGER_PORT_PROPERTY));
    		pdqSupplierPort = Integer.parseInt(prop.getProperty(PDQ_SUPPLIER_PORT_PROPERTY));
		} catch (Exception e) {
            log.error("Configuration parameters are missing or invalid: "  + e, e);
            throw new RuntimeException("Configuration parameters are missing or invalid: " + e.getMessage());		    
		}
		
        // The connection hub connects to listening servers
        ctx = new DefaultHapiContext();
        ctx.setValidationContext(new MessageValidation());
        MinLowerLayerProtocol mllp = new MinLowerLayerProtocol(true);
        ctx.setLowerLayerProtocol(mllp);

		// A connection object represents a socket attached to an HL7 server
		if (supportsPdqProfile()) {
		    pdqConnection = ctx.newClient("localhost", pdqSupplierPort, false);
			defaultConnection = pdqConnection;
		}
		if (supportsPixProfile()) {
			pixConnection = ctx.newClient("localhost", pixManagerPort, false);
			defaultConnection = pixConnection;
		}
	}

	public Message sendMessage(Connection connection, String message) {
		log.debug("Sending message to " + connection.toString() + ":\n"
				+ message);
		Parser parser = ctx.getPipeParser();
		Message adt;
		try {
			adt = parser.parse(message);

			// The initiator is used to transmit unsolicited messages
			Initiator initiator = connection.getInitiator();
			initiator.setTimeout(360, TimeUnit.SECONDS);

			Message response = initiator.sendAndReceive(adt);

			String responseString = parser.encode(response);
			log.debug("Received response:\n" + responseString);

			return response;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public Message sendMessage(String message) {
		log.debug("Sending message to " + hostname + " aBasePixPdqTestt port " + port + ":\n"
				+ message);
		Parser parser = ctx.getPipeParser();
		Message adt;
		try {
			adt = parser.parse(message);

			// The initiator is used to transmit unsolicited messages
			Initiator initiator = defaultConnection.getInitiator();
            initiator.setTimeout(360, TimeUnit.SECONDS);

			Message response = initiator.sendAndReceive(adt);

			String responseString = parser.encode(response);
			log.debug("Received response:\n" + responseString);

			return response;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Connection getPdqConnection() {
		return pdqConnection;
	}

	public void setPdqConnection(Connection pdqConnection) {
		this.pdqConnection = pdqConnection;
	}

	public Connection getPixConnection() {
		return pixConnection;
	}

	public void setPixConnection(Connection pixConnection) {
		this.pixConnection = pixConnection;
	}

	protected abstract boolean supportsPixProfile();
	
	protected abstract boolean supportsPdqProfile();
	
	protected abstract String getHostname(Profile profile);
	
	protected abstract int getPort(Profile profile);
	
	@Override
	protected void tearDown() throws Exception {
	    if (pdqConnection != null) {
	        pdqConnection.close();
	    }
        if (pixConnection != null) {
            pixConnection.close();
        }
        if (ctx != null) {
            ctx.close();
        }
	}
	
	protected String getResponseString(Message response) throws HL7Exception {
		Parser parser = new PipeParser();
		return parser.encode(response);
	}

	protected void deletePerson(Person person) throws ApplicationException {
	    //TODO: Need to add support for deleting patient records after the test is done
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource resource = resolver.getResource("classpath:/IheActors.xml");
		PixPdqConfigurationLoader loader = PixPdqConfigurationLoader.getInstance();
		try {
			loader.loadConfiguration(resource.getFile().getAbsolutePath(), true);
			loader.destroyAllActors();
		} catch (IheConfigurationException e) {
			e.printStackTrace();
		}
	}
}
