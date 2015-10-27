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
package org.openhie.openempi.model;

public enum RecordLinkState
{
	MATCH("M"), NON_MATCH("N"), POSSIBLE_MATCH("P");
	 
	private String state;
 
	private RecordLinkState(String linkState) {
		state = linkState;
	}
 
	public String getState() {
		return state;
	}

	public static RecordLinkState fromString(String text) {
		if (text != null) {
			for (RecordLinkState state : RecordLinkState.values()) {
				if (text.equalsIgnoreCase(state.toString())) {
					return state;
				}
			}
	    }
	    return null;
	}
	
	public String toString() {
		return state;
	}
}
