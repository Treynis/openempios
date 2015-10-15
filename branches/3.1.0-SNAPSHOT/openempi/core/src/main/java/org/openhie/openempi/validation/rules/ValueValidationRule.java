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

import java.util.ArrayList;
import java.util.List;


/**
 * This is a base type for a more complex validation rule that validates a field to ensure that
 * it is in the list of valid values. There are going to be various other rules that build on this
 * one and vary depending on where the values for the validation come from.
 * 
 * @author odysseas 
 * @version $Revision: $ $Date:  $
 */
public abstract class ValueValidationRule extends AbstractValidationRule
{
	private List<String> validValues = new ArrayList<String>();
	
	protected List<String> getValidValues() {
		return validValues;
	}
	
	protected void addValidValue(String value) {
		validValues.add(value);
	}
	
	protected void setValidValues(List<String> validValues) {
		this.validValues = validValues;
	}
}
