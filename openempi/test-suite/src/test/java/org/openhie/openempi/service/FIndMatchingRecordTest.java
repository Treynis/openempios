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

import org.junit.Test;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.Person;

public class FIndMatchingRecordTest extends BaseServiceTestCase
{
    @Test
    public void test() {
        try {
            Person personOne = new Person();
//            personOne.setFamilyName("Brown");
            personOne.setGivenName("James");
            PersonQueryService personService = Context.getPersonQueryService();
            List<Person> list = personService.findMatchingPersonsByAttributes(personOne);
            for (Person person : list) {
                log.debug("Found person: " + person);
            }
            log.info("Found " + list.size() + " similar records by blocking.");
        } catch (Throwable t) {
            assertTrue("Failed while running the findMatchingPersonsByAttributes method: " + t, false);
            t.printStackTrace();
        }
    }
}
