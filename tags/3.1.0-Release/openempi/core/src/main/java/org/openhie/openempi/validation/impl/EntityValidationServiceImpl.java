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
package org.openhie.openempi.validation.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openhie.openempi.service.impl.BaseServiceImpl;
import org.openhie.openempi.validation.ValidationRule;
import org.openhie.openempi.validation.EntityValidationService;

public class EntityValidationServiceImpl extends BaseServiceImpl implements EntityValidationService
{
	private Map<String,ValidationRule> validationRuleMap;
	
	public EntityValidationServiceImpl() {
		super();
		validationRuleMap = new HashMap<String,ValidationRule>();
	}
	
	public Set<ValidationRule> getValidationRules() {
		Set<ValidationRule> rules = new HashSet<ValidationRule>();
		rules.addAll(validationRuleMap.values());
		return rules;
	}

	public ValidationRule getValidationRule(String ruleName) {
		return validationRuleMap.get(ruleName);
	}

	public Map<String, ValidationRule> getValidationRuleMap() {
		return validationRuleMap;
	}

	public void setValidationRuleMap(Map<String, ValidationRule> validationRuleMap) {
		this.validationRuleMap = validationRuleMap;
	}
}
