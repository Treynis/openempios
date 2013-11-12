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
 * only values within the set of values that are provided as parameters to the instance.
 * 
 * @author odysseas 
 * @version $Revision: $ $Date:  $
 */
public class ValueSetValueValidationRule extends ValueValidationRule
{
	public final static String VALUE_SET_PARAMETER = "valueSet";

	@Override
	public String[] getParameters() {
		return new String[] { VALUE_SET_PARAMETER };
	}
	
	public boolean isValid(Object value, Map<String, String> parameterValueMap) throws ValidationException {
		if (value == null) {
			return true;
		}
		
		if (!parsedParameters) {
			validateParameters(parameterValueMap);
		}
		
		String strValue = value.toString();
		for (String validValue : getValidValues()) {
			if (validValue.equalsIgnoreCase(strValue)) {
				return true;
			}
		}
		log.debug("Value " + strValue + " is not valid.");
		throw new ValidationException("The value is " + strValue + " is not valid.");
	}

	private void validateParameters(Map<String, String> parameterValueMap) {
		if (parameterValueMap == null) {
			parsedParameters = true;
			return;
		}
		
		if (parameterValueMap.get(VALUE_SET_PARAMETER) == null) {
			parsedParameters = true;
			return;
		}
		String valueList = parameterValueMap.get(VALUE_SET_PARAMETER);
		if (valueList == null || valueList.length() == 0) {
			log.warn("The value list for the value set validator is empty; all validation requests will fail.");
			parsedParameters = true;
			return;
		}
		
		String[] values = valueList.split(",");
		for (String value : values) {
			addValidValue(value.trim());
		}
		parsedParameters = true;
	}
}
