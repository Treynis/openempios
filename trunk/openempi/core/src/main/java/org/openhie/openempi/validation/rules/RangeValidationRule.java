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
 * The RangeValidationRule ensures that the value of a numeric field falls within a given range. The 
 * configuration parameters are lowerRange and upperRange and both of them are optional although the
 * caller should specify one of them, otherwise the validator will not provide any value.

 * @author
 * @version $Revision: $ $Date:  $
 */
public class RangeValidationRule extends AbstractValidationRule
{
	public final static String LOWER_RANGE = "lowerRange";
	public final static String UPPER_RANGE = "upperRange";
	private double lowerRange;
	private double upperRange;

	@Override
	public String[] getParameters() {
		return new String[] { LOWER_RANGE, UPPER_RANGE };
	}
	
	public boolean isValid(Object value, Map<String, String> parameterValueMap) throws ValidationException {
		if (value == null ) {
			throw new ValidationException("The range of the value is not valid.");
		}

		if (parameterValueMap == null ) {
			// no parameter value Map
			return true;
		}

		if (!parsedParameters) {
			validateParameters(parameterValueMap);
		}
		
		double range = Double.parseDouble(value.toString());
		if (parameterValueMap.get(LOWER_RANGE) != null) {
			if (range < lowerRange) {
				throw new ValidationException("The value of the range is below the lowerRange value expected.");	
			}
		}
		
		if (parameterValueMap.get(UPPER_RANGE) != null) {		
			if (range > upperRange) {
				throw new ValidationException("The value of the range is above the upperRange value expected.");
			}
		}
		return true;
	}
	
	private void validateParameters(Map<String, String> parameterValueMap) {
		if (parameterValueMap == null) {
			return;
		}
		
		if (parameterValueMap.get(LOWER_RANGE) != null) {
			String rangeStr = parameterValueMap.get(LOWER_RANGE);

			lowerRange = validateRange(rangeStr, LOWER_RANGE, Double.MIN_VALUE);			
		}
		
		if (parameterValueMap.get(UPPER_RANGE) != null) {
			String rangeStr = parameterValueMap.get(UPPER_RANGE);
			upperRange = validateRange(rangeStr, UPPER_RANGE, Double.MAX_VALUE);
		}
		parsedParameters = true;
	}
	
	private double validateRange(String rangeStr, String paramName, double defaultValue) {
		double range = defaultValue;
		
		try {
			range = Double.parseDouble(rangeStr);
		} catch (NumberFormatException e) {
			log.warn("The required parameter '" + paramName + "' is invalid; will use default value of " + defaultValue);
			range = defaultValue;
		}
		return range;
	}
}
