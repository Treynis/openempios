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

import com.extjs.gxt.ui.client.data.BaseModelData;

public class RecordSearchCriteriaWeb extends BaseModelData implements Serializable
{
	
	@SuppressWarnings("unused")
    private EntityWeb unusedEntityWeb;
	
	@SuppressWarnings("unused")
    private RecordWeb unusedRecordWeb;

	@SuppressWarnings("unused")
    private IdentifierWeb unusedIdentifierWeb;
	
	public RecordSearchCriteriaWeb() {
	}
	
	public EntityWeb getEntityModel() {
		return get("entityModel");
	}
	
	public void setEntityModel(EntityWeb entityModel) {
		set("entityModel", entityModel);
	}		

	public String getSearchMode() {
		return get("searchMode");
	}
	
	public void setSearchMode(String searchMode) {
		set("searchMode", searchMode);
	}
	
	public RecordWeb getRecord() {
		return get("record");
	}
	
	public void setRecord(RecordWeb record) {
		set("record", record);
	}		
	
	public IdentifierWeb getIdentifier() {
		return get("identifier");
	}

	public void setIdentifier(IdentifierWeb identifier) {
		set("identifier", identifier);
	}
	
	public java.lang.Integer getFirstResult() {
		return get("firstResult");
	}

	public void setFirstResult(java.lang.Integer firstResult) {
		set("firstResult", firstResult);
	}
	
	public java.lang.Integer getMaxResults() {
		return get("maxResults");
	}

	public void setMaxResults(java.lang.Integer maxResults) {
		set("maxResults", maxResults);
	}
	
	public java.lang.Long getTotalCount() {
		return get("totalCount");
	}

	public void setTotalCount(java.lang.Long totalCount) {
		set("totalCount", totalCount);
	}
}
