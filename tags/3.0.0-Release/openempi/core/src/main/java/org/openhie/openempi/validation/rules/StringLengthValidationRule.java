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
 * Validates a String value to ensure that its length ranges between the 
 * minimum and maximum lengths specified as parameters of the validator.
 * Each of the individual parameters are optional with default values
 * assumed if the parameter is not explicitly specified. If the minimum
 * length is left off, then the miminum length is set to 0 whereas if
 * the maximum length is left off, then the maximum length is set to
 * the greatest integer value available.
 *  
 * @author Odysseas
 * @version $Revision: $ $Date:  $
 */
public class StringLengthValidationRule extends AbstractValidationRule
{
	public final static String MIN_LENGTH_PARAMETER = "minimumLength";
	public final static String MAX_LENGTH_PARAMETER = "maximumLength";
	private int minLength;
	private int maxLength;
	
	@Override
	public String[] getParameters() {
		return new String[] { MIN_LENGTH_PARAMETER, MAX_LENGTH_PARAMETER };
	}

	public boolean isValid(Object value, Map<String, String> parameterValueMap) throws ValidationException {
		if (!parsedParameters) {
			validateParameters(parameterValueMap);
		}
		if ((value == null || value.toString().length() == 0) && minLength > 0) {
			log.debug("Value has length of 0 but was expected to have a minimum length of " + minLength);
			throw new ValidationException("The length of the value is below the minimum value expected.");
		}
		int stringLength = value.toString().length();
		if (stringLength < minLength) {
			log.debug("Value has length of " + stringLength + " but was expected to have a minimum length of " + minLength);
			throw new ValidationException("The length of the value is below the minimum value expected.");			
		}
		if (stringLength > maxLength) {
			log.debug("Value has length of " + stringLength + " but was expected to have a maximum length of " + maxLength);
			throw new ValidationException("The length of the value is above the maximum value expected.");
		}
		return true;
	}

	private void validateParameters(Map<String, String> parameterValueMap) {
		if (parameterValueMap == null) {
			minLength = 0;
			maxLength = Integer.MAX_VALUE;
			parsedParameters = true;
			return;
		}
		
		if (parameterValueMap.get(MIN_LENGTH_PARAMETER) == null) {
			minLength = 0;
		} else {
			String lengthStr = parameterValueMap.get(MIN_LENGTH_PARAMETER);
			minLength = validateLength(lengthStr, MIN_LENGTH_PARAMETER, 0);			
		}
		
		if (parameterValueMap.get(MAX_LENGTH_PARAMETER) == null) {
			maxLength = Integer.MAX_VALUE;
		} else {
			String lengthStr = parameterValueMap.get(MAX_LENGTH_PARAMETER);
			maxLength = validateLength(lengthStr, MAX_LENGTH_PARAMETER, Integer.MAX_VALUE);
		}
		parsedParameters = true;
	}

	private int validateLength(String lengthStr, String paramName, int defaultValue) {
		int length = defaultValue;
		if (lengthStr.length() == 0) {
			log.warn("The parameter '" + paramName + "' is invalid; will use default value of " + defaultValue);
			length = defaultValue;
		} else {
			try {
				length = Integer.parseInt(lengthStr);
			} catch (NumberFormatException e) {
				log.warn("The required parameter '" + paramName + "' is invalid; will use default value of " + defaultValue);
				length = defaultValue;
			}
		}
		return length;
	}
}
