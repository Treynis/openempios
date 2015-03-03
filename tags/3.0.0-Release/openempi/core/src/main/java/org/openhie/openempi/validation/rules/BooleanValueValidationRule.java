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
package org.openhie.openempi.validation.rules;

import java.util.Map;

import org.openhie.openempi.ValidationException;


/**
 * This is value set validation rule that validates a field to ensure that it takes
 * on a boolean value.
 * 
 * @author odysseas 
 * @version $Revision: $ $Date:  $
 */
public class BooleanValueValidationRule extends ValueValidationRule
{
	private final static String TRUE_VALUE = "true";
	private final static String FALSE_VALUE = "false";
	
	public BooleanValueValidationRule() {
		this.addValidValue(TRUE_VALUE);
		this.addValidValue(FALSE_VALUE);
	}
	
	public boolean isValid(Object value, Map<String, String> parameterValueMap) throws ValidationException {
		if (value == null) {
			return true;
		}
		String strValue = value.toString();
		if (strValue.equalsIgnoreCase(TRUE_VALUE) || strValue.equalsIgnoreCase(FALSE_VALUE)) {
			return true;
		}
		log.debug("Value " + strValue + " is not a valid boolean value.");
		throw new ValidationException("The value is not a valid boolean value.");
	}
}
