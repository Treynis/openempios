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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhie.openempi.ValidationException;
import org.openhie.openempi.validation.ValidationRule;

public abstract class AbstractValidationRule implements ValidationRule
{
	protected final Log log = LogFactory.getLog(getClass());

	private Map<String,String> parameterValueMap;
	private String validationRuleName;
	private String validationRuleDisplayName;
	protected boolean parsedParameters = false;

	public AbstractValidationRule() {
		parameterValueMap = new HashMap<String,String>();
	}
	
	public String[] getParameters() {
		return new String[] {};
	}

	public Map<String, String> getParameterValues() {
		return parameterValueMap;
	}

	public void setParameterValues(Map<String, String> parameterValueMap) {
		this.parameterValueMap = parameterValueMap;
	}

	public boolean isValid(Object value) throws ValidationException {
		return isValid(value, parameterValueMap);
	}

	public String getValidationRuleName() {
		return validationRuleName;
	}

	public void setValidationRuleName(String validationRuleName) {
		this.validationRuleName = validationRuleName;
	}

	public String getValidationRuleDisplayName() {
		return validationRuleDisplayName;
	}

	public void setValidationRuleDisplayName(String validationRuleDisplayName) {
		this.validationRuleDisplayName = validationRuleDisplayName;
	}
}
