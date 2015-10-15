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

import com.extjs.gxt.ui.client.data.BaseModel;

public class RecordLinkWeb extends BaseModel
{
	@SuppressWarnings("unused")
    private UserWeb unusedUserWeb;
	
	@SuppressWarnings("unused")
    private RecordWeb unusedRecordWeb;

	@SuppressWarnings("unused")
    private LinkSourceWeb unusedLinkSourceWeb;
	
	public RecordLinkWeb() {
	}
	
	public String getRecordLinkId() {
		return get("recordLinkId");
	}

	public void setRecordLinkId(String recordLinkId) {
		set("recordLinkId", recordLinkId);
	}
	
	public java.util.Date getDateCreated() {
		return get("dateCreated");
	}
	
	public void setDateCreated(java.util.Date dateCreated) {
		set("dateCreated", dateCreated);
	}
	
	
	public RecordWeb getLeftRecord() {
		return get("leftRecord");
	}

	public void setLeftRecord(RecordWeb leftRecord) {
		set("leftRecord", leftRecord);
	}

	public RecordWeb getRightRecord() {
		return get("rightRecord");
	}

	public void setRightRecord(RecordWeb rightRecord) {
		set("rightRecord", rightRecord);
	}
	
	public LinkSourceWeb getLinkSource() {
		return get("linkSource");
	}

	public void setLinkSource(LinkSourceWeb linkSource) {
		set("linkSource", linkSource);
	}
	
	public UserWeb getUserCreatedBy() {
		return get("userCreatedBy");
	}
	
	public void setUserCreatedBy(UserWeb userCreatedBy) {
		set("userCreatedBy", userCreatedBy);
	}

	public java.lang.Double getWeight() {
		return get("weight");
	}
	
	public void setWeight(java.lang.Double weight) {
		set("weight", weight);
	}

	public java.lang.Integer getVector() {
		return get("vector");
	}

	public void setVector(java.lang.Integer vector) {
		set("vector", vector);
	}
	
	public String getState() {
		return get("state");
	}

	public void setState(String state) {
		set("state", state);
	}
}
