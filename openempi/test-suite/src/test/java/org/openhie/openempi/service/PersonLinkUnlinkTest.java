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
package org.openhie.openempi.service;

import org.openhie.openempi.ApplicationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.PersonLink;

public class PersonLinkUnlinkTest extends BaseServiceTestCase
{
	private PersonManagerService personManagerService;
	private PersonQueryService personQueryService;
	private Person leftPerson;
	private Person rightPerson;
	
	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();
		personManagerService = Context.getPersonManagerService();
		personQueryService = Context.getPersonQueryService();
		
		PersonIdentifier pi = new PersonIdentifier();
		pi.setIdentifier("9e4539b0-a045-11e1-a160-005056c00008");
		IdentifierDomain id = new IdentifierDomain();
		id.setNamespaceIdentifier("2.16.840.1.113883.4.357");
		pi.setIdentifierDomain(id);
		leftPerson = personQueryService.findPersonById(pi);
		
		pi = new PersonIdentifier();
		pi.setIdentifier("b5195770-a045-11e1-a160-005056c00008");
		pi.setIdentifierDomain(id);
		rightPerson = personQueryService.findPersonById(pi);
	}
	
	public void testLink() {
		System.out.println(leftPerson);
		System.out.println(rightPerson);
		assertNotNull("Unable to find left person.", leftPerson);
		assertNotNull("Unable to find right person.", rightPerson);
		PersonLink link = new PersonLink();
		link.setPersonLeft(leftPerson);
		link.setPersonRight(rightPerson);
		try {
			link = personManagerService.linkPersons(link);
			System.out.println(link);
			
			personManagerService.unlinkPersons(link);
			System.out.println(link);
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
