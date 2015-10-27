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

import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.openhie.openempi.model.Person;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.segment.MSA;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.parser.PipeParser;

public class PixISO8859EncodingTestCase extends AbstractPixTest
{
	public void testPixISO8859Encoding() {
		try {
		    byte[] buffer = IOUtils.toByteArray(getClass().getClassLoader()
		            .getResourceAsStream("iso8859-1-message.txt"));
		    String message = new String(buffer, Charset.forName("ISO-8859-1"));
			PipeParser parser = ctx.getPipeParser();
			Message msg = parser.parse(message);
			PID pid = (PID) msg.get("PID");
			System.out.println("PID is " + pid);
			Message response = sendMessage(message);
			System.out.println("Received response:\n" + getResponseString(response));
			MSA msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgmentCode().getValue());
			assertEquals("19616641", msa.getMessageControlID().getValue());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Fail to test PIX Mesa 10502 PIX Feed and Query for unregistered patient.");
		}		
	}
	
	   
	   @Override
	protected void tearDown() throws Exception {
		try {
			Person person = new Person();
			person.setGivenName("BETTY");
			person.setFamilyName("BETA");
			deletePerson(person);
			
			person = new Person();
			person.setGivenName("KEN");
			person.setFamilyName("CROSS");
			deletePerson(person);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.tearDown();
	}	
}
