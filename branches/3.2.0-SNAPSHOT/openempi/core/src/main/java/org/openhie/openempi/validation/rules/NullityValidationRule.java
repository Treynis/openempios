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
 * This validation rule validates a field to make sure that its value is not null. It does not
 * take any parameters.
 * 
 * @author
 * @version $Revision: $ $Date:  $
 */
public class NullityValidationRule extends AbstractValidationRule
{

	public boolean isValid(Object value, Map<String, String> parameterValueMap) throws ValidationException {

		if (value == null) {
			throw new ValidationException("The value is null.");	
		}
		return true;
	}
}
