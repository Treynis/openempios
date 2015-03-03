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
package org.openhie.openempi.validation;

import java.util.HashMap;
import java.util.Map;

import org.openhie.openempi.ValidationException;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.service.BaseServiceTestCase;
import org.openhie.openempi.validation.rules.StringLengthValidationRule;
import org.openhie.openempi.validation.rules.RangeValidationRule;
import org.openhie.openempi.validation.rules.RegexpValidationRule;
import org.openhie.openempi.validation.rules.ValueSetValueValidationRule;

public class EntityValidationTest extends BaseServiceTestCase
{
	public void testValueSetValidationRule() {
		EntityValidationService validationService = Context.getEntityValidationService();
		ValidationRule rule = validationService.getValidationRule("valueSetValidationRule");
		assertNotNull("Unable to locate the validation rule.", rule);
		
		String name=rule.getValidationRuleName();
		assertEquals("valueSetValidationRule", name);
		
		String displayName=rule.getValidationRuleDisplayName();
		assertEquals("Value Set Validation Rule", displayName);
		
		String[] parameters = rule.getParameters();
		assertTrue("Validation accepts one parameter.", parameters.length == 1);
		for (String param : parameters) {
			log.debug("Validator " + rule.getClass().getName() + " accepts parameter " + param);
		}
		
		log.debug("Will validate the values next.");
		Map<String,String> params = new HashMap<String,String>();
		String validValues = "true,false,maybe";
		params.put(ValueSetValueValidationRule.VALUE_SET_PARAMETER, validValues);
		rule.setParameterValues(params);
		
		String[] testValues = { "TRUE", "false", "MayBe" };
		for (String value : testValues) {
			boolean response = rule.isValid(value);
			assertTrue("This should be a valid value: " + value, response);
		}
		String invalid = "invalid";
		try {
			rule.isValid(invalid);
		} catch (ValidationException e) {
			assertTrue("This value is invalid so test should had thrown an exception.", true);			
		}
	}

	public void testBooleanValidationRule() {
		EntityValidationService validationService = Context.getEntityValidationService();
		ValidationRule rule = validationService.getValidationRule("booleanValueValidationRule");
		assertNotNull("Unable to locate the validation rule.", rule);
		
		String name=rule.getValidationRuleName();
		assertEquals("booleanValueValidationRule", name);
		
		String displayName=rule.getValidationRuleDisplayName();
		assertEquals("Boolean Value Validation Rule", displayName);
		
		String[] parameters = rule.getParameters();
		assertTrue("Validation accepts no parameters.", parameters.length == 0);
		
		try {
			rule.isValid("true");
			rule.isValid("false");
			rule.isValid(Boolean.FALSE);
			rule.isValid(Boolean.TRUE);
		} catch (ValidationException e) {
			assertFalse("This value is valid so test should not had thrown an exception.", true);
		}
		try {
			rule.isValid("testing");
			assertTrue("This value is invalid so test should had thrown an exception.", true);
		} catch (ValidationException e) {
			log.debug("Got message of " + e.getMessage());
		}
	}
	
	public void testStringLengthValidationRule() {
		EntityValidationService validationService = Context.getEntityValidationService();
		ValidationRule rule = validationService.getValidationRule("stringLengthValidationRule");
		assertNotNull("Unable to locate the validation rule.", rule);
		
		String name=rule.getValidationRuleName();
		assertEquals("stringLengthValidationRule", name);
		
		String displayName=rule.getValidationRuleDisplayName();
		assertEquals("String Length Validation Rule", displayName);
		
		String[] parameters = rule.getParameters();
		assertTrue("Validation accepts two parameters.", parameters.length > 0);
		for (String param : parameters) {
			log.debug("Validator " + rule.getClass().getName() + " accepts parameter " + param);
		}
		
		Map<String,String> params = new HashMap<String,String>();
		params.put(StringLengthValidationRule.MIN_LENGTH_PARAMETER, "2");
		params.put(StringLengthValidationRule.MAX_LENGTH_PARAMETER, "4");
		rule.setParameterValues(params);
		assertTrue(rule.isValid("te"));
		assertTrue(rule.isValid("tes"));
		assertTrue(rule.isValid("test"));
		try {
			rule.isValid("t");
			assertTrue("This value is invalid so test should had thrown an exception.", true);
		} catch (ValidationException e) {
			log.debug("Got message of " + e.getMessage());
		}
		try {
			rule.isValid("testing");
			assertTrue("This value is invalid so test should had thrown an exception.", true);
		} catch (ValidationException e) {
			log.debug("Got message of " + e.getMessage());
		}
	}
	
	public void testRangeValidationRule() {
		EntityValidationService validationService = Context.getEntityValidationService();
		ValidationRule rule = validationService.getValidationRule("rangeValidationRule");

		assertNotNull("Unable to locate the validation rule.", rule);
		
		String name=rule.getValidationRuleName();
		assertEquals("rangeValidationRule", name);
		
		String displayName=rule.getValidationRuleDisplayName();
		assertEquals("Range Validation Rule", displayName);
		
		String[] parameters = rule.getParameters();
		assertTrue("Validation accepts two parameters.", parameters.length > 0);
		for (String param : parameters) {
			log.debug("Validator " + rule.getClass().getName() + " accepts parameter " + param);
		}
		
		Map<String,String> params = new HashMap<String,String>();
		params.put(RangeValidationRule.LOWER_RANGE, "5");
		params.put(RangeValidationRule.UPPER_RANGE, "20");
		rule.setParameterValues(params);
		assertTrue(rule.isValid("5"));
		assertTrue(rule.isValid("10"));
		assertTrue(rule.isValid("20"));
		
		try {
			rule.isValid("2");
			assertTrue("This value is invalid so test should had thrown an exception.", true);
		} catch (ValidationException e) {
			log.debug("Got message of " + e.getMessage());
		}
		try {
			rule.isValid("25");
			assertTrue("This value is invalid so test should had thrown an exception.", true);
		} catch (ValidationException e) {
			log.debug("Got message of " + e.getMessage());
		}		
	}
	
	public void testNullValidationRule() {
		EntityValidationService validationService = Context.getEntityValidationService();
		ValidationRule rule = validationService.getValidationRule("nullityValidationRule");

		assertNotNull("Unable to locate the validation rule.", rule);
		
		String name=rule.getValidationRuleName();
		assertEquals("nullityValidationRule", name);
		
		String displayName=rule.getValidationRuleDisplayName();
		assertEquals("Nullity Validation Rule", displayName);
		
		assertTrue(rule.isValid(new Integer(0)));

		try {
			rule.isValid(null);
			assertTrue("This value is invalid so test should had thrown an exception.", true);
		} catch (ValidationException e) {
			log.debug("Got message of " + e.getMessage());
		}
	}
	
	public void testRegexpValidationRule() {
		EntityValidationService validationService = Context.getEntityValidationService();
		ValidationRule rule = validationService.getValidationRule("regexpValidationRule");
		assertNotNull("Unable to locate the validation rule.", rule);
		
		String name=rule.getValidationRuleName();
		assertEquals("regexpValidationRule", name);
		
		String displayName=rule.getValidationRuleDisplayName();
		assertEquals("Regular Expression Validation Rule", displayName);
		
		String[] parameters = rule.getParameters();
		assertTrue("Validation accepts two parameters.", parameters.length > 0);
		for (String param : parameters) {
			log.debug("Validator " + rule.getClass().getName() + " accepts parameter " + param);
		}
		
		Map<String,String> params = new HashMap<String,String>();
		params.put(RegexpValidationRule.REGEXP_PARAMETER, "^([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4}$");
		rule.setParameterValues(params);
		
		assertTrue(rule.isValid("test@test.com"));
		
		try {
			rule.isValid("test@test.c");
			
			assertFalse("This value is invalid so test should had thrown an exception.", true);
			
		} catch (ValidationException e) {
			log.debug("Got message of " + e.getMessage());
		}
	}
}
