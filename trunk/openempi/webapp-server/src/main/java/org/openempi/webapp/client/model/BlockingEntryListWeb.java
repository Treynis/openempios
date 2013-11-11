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
package org.openempi.webapp.client.model;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseModel;

public class BlockingEntryListWeb extends BaseModel implements Serializable
{
    public static final String ENTITY_NAME_KEY = "entityName";

	@SuppressWarnings("unused")
    private BaseFieldWeb unusedBaseFieldWeb;	
	
	@SuppressWarnings("unused")
    private UserWeb unusedUserWeb;
	
	public BlockingEntryListWeb() {
	}

	public java.lang.Integer getMaximumBlockSize() {
		return get("maximumBlockSize");
	}
	
	public void setMaximumBlockSize(java.lang.Integer maximumBlockSize) {
		set("maximumBlockSize", maximumBlockSize);
	}

	public java.util.List<BaseFieldWeb> getBlockingRoundEntries() {
		return get("blockingRoundEntries");
	}
	
	public void setBlockingRoundtEntries(java.util.List<BaseFieldWeb> blockingRoundEntries) {
		set("blockingRoundEntries", blockingRoundEntries);
	}
	
    public java.lang.String getEntityName() {
        return get(ENTITY_NAME_KEY);
    }

    public void setEntityName(java.lang.String entityName) {
        set(ENTITY_NAME_KEY, entityName);
    }
}
