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
package org.openempi.webapp.server.util;

public enum AttributeDatatype
{
	INTEGER("integer", 1) {
	},
	SHORT("short", 2) {
	},
	LONG("long", 3) {
	},
	DOUBLE("double", 4) {
	},
	FLOAT("float", 5) {
	},
	STRING("string", 6) {
	},
	BOOLEAN("boolean", 7) {
	},
	DATE("date", 8) {
	},
	TIMESTAMP("timestamp", 9) {
	};
	
	private static final AttributeDatatype[] TYPES = new AttributeDatatype[] { 
		INTEGER, SHORT, LONG, DOUBLE, FLOAT, STRING, BOOLEAN, DATE, TIMESTAMP
	};
	private String name;
	private int id;
	
	private AttributeDatatype(String iName, int iId) {
		name = iName;
		id = iId;
	}
	
	/**
	 * Return the type by ID.
	 * 
	 * @param iId
	 *            The id to search
	 * @return The type if any, otherwise null
	 */
	public static AttributeDatatype getById(final int iId) {
		for (AttributeDatatype t : TYPES) {
			if (iId == t.id)
				return t;
		}
		return null;
	}
	
	public static AttributeDatatype getByName(final String iName) {
		for (AttributeDatatype t : TYPES) {
			if (iName.equalsIgnoreCase(t.name))
				return t; 
		}
		return null;
	}
}
