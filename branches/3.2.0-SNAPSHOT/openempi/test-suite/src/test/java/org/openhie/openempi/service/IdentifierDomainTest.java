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

import java.util.List;

import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.IdentifierDomain;

public class IdentifierDomainTest extends BaseServiceTestCase
{
	private final static String universalIdentifierTypeCode = "OpenMRS";
	
	public void testGetIdentifierDomains() {
		IdentifierDomainService identifierDomainService = Context.getIdentifierDomainService();
		try {
			List<IdentifierDomain> domains = identifierDomainService.getIdentifierDomains();
			for (IdentifierDomain domain : domains) {
				System.out.println("Domain: " + domain);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public void testGetUniversalIdentifierTypeCodes() {
		IdentifierDomainService identifierDomainService = Context.getIdentifierDomainService();
		try {
			List<String> codes = identifierDomainService.getIdentifierDomainTypeCodes();
			for (String code : codes) {
				System.out.println("Universal Identifier Type Code: " + code);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public void testObtainIdentifierDomain() {
		IdentifierDomainService identifierDomainService = Context.getIdentifierDomainService();
		try {
			IdentifierDomain identifierDomain = identifierDomainService.obtainUniqueIdentifierDomain(universalIdentifierTypeCode);
			System.out.println("Identifier Domain is : " + identifierDomain);
		} catch (Throwable t) {
			t.printStackTrace();
		}		
	}
}
