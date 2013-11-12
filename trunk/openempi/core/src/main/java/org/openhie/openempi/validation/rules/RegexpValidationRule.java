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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.openhie.openempi.ValidationException;

/**
 * This validation rule ensures that a string value matches the regular
 * expression specified by the regexp parameter. This allows the user
 * to specify the valid values for a field based on a regular expression. 
 * 
 * For example, to validate an email address field, the user could specify
 * ^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$
 * as the regular expression for validating the field.
 * @author 
 * @version $Revision: $ $Date:  $
 */
public class RegexpValidationRule extends AbstractValidationRule
{
	public final static String REGEXP_PARAMETER = "regularExpression";
	private Pattern regexPattern;
	
	@Override
	public String[] getParameters() {
		return new String[] { REGEXP_PARAMETER };
	}
	
	public boolean isValid(Object value, Map<String, String> parameterValueMap) throws ValidationException {
		// TODO Auto-generated method stub		
		if (value == null) {
			throw new ValidationException("The value of the expressiion is null.");
		}

		if (!parsedParameters) {
			validateParameters(parameterValueMap);
		}

		if (regexPattern == null) {
			log.debug("The regular expression was absent or invalid so the validation step was skipped.");
			return true;
		}
		
		String expression = value.toString();
		Matcher matcher = regexPattern.matcher(expression);
		boolean result = matcher.matches();
		if (!result) {
			throw new ValidationException("The expressiion is invalid.");	
		}
		
		return true;
	}

	private void validateParameters(Map<String, String> parameterValueMap) {
		String regexp = parameterValueMap.get(REGEXP_PARAMETER);
		if (regexp == null || regexp.length() == 0) {
			return;
		}
		try {
			regexPattern = Pattern.compile(regexp);
		} catch (PatternSyntaxException e) {
			log.warn("The regular expression pattern " + regexp + " is invalid: " + e, e);
			regexPattern = null;
		}
		parsedParameters = true;
	}
}
