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

import java.util.List;

import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.DataProfileAttribute;
import org.openhie.openempi.model.DataProfileAttributeValue;
import org.openhie.openempi.service.BaseManagerTestCase;

public class DataProfilerTest extends BaseManagerTestCase
{
	public void testDataProfileView() {
		DataProfileService service = Context.getDataProfileService();
		List<DataProfileAttribute> dataList = service.getDataProfileAttributes(0);
		assertNotNull("Did not find any data profile attribute data.", dataList);
		assertTrue("The list of data profile attribute data is empty.", dataList.size() > 0);
		
		DataProfileAttribute givenNameAttribute=null;
		for (DataProfileAttribute attrib : dataList) {
			log.debug(attrib);
			if (attrib.getAttributeName() != null && attrib.getAttributeName().equalsIgnoreCase("givenName")) {
				givenNameAttribute = attrib;
			}
		}
		if (givenNameAttribute != null) {
			List<DataProfileAttributeValue> values = service.getTopDataProfileAttributeValues(givenNameAttribute.getAttributeId(), 10);
			assertNotNull("Did not find any data profile attribute data values.", values);
			assertTrue("The list of data profile attribute data values is empty.", values.size() > 0);
			for (DataProfileAttributeValue value : values) {
				log.debug("The value [" + value.getAttributeValue() + "] has a frequence of " + value.getFrequency());
			}
		}
	}
}
