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

import com.extjs.gxt.ui.client.data.BaseModelData;

public class EntityWeb extends BaseModelData
{ 
	@SuppressWarnings("unused")
    private EntityAttributeWeb unusedEntityAttributeWeb;

	@SuppressWarnings("unused")
    private EntityAttributeGroupWeb unusedEntityAttributeGroupWeb;
	
	@SuppressWarnings("unused")
    private UserWeb unusedUserWeb;
	
	public EntityWeb() {
	}
	
	public java.lang.Integer getEntityVersionId() {
		return get("entityVersionId");
	}

	public void setEntityVersionId(java.lang.Integer entityVersionId) {
		set("entityVersionId", entityVersionId);
	}
	
	public java.lang.Integer getVersionId() {
		return get("versionId");
	}

	public void setVersionId(java.lang.Integer versionId) {
		set("versionId", versionId);
	}
	
	public java.lang.Integer getEntityId() {
		return get("entityId");
	}

	public void setEntityId(java.lang.Integer entityId) {
		set("entityId", entityId);
	}
	
	public java.lang.String getName() {
		return get("name");
	}

	public void setName(java.lang.String name) {
		set("name", name);
	}

	public java.lang.String getDisplayName() {
		return get("displayName");
	}

	public void setDisplayName(java.lang.String displayName) {
		set("displayName", displayName);
	}
	
	public java.lang.String getDescription() {
		return get("description");
	}

	public void setDescription(java.lang.String description) {
		set("description", description);
	}

	public java.util.Date getDateCreated() {
		return get("dateCreated");
	}
	
	public void setDateCreated(java.util.Date dateCreated) {
		set("dateCreated", dateCreated);
	}

	public UserWeb getUserCreatedBy() {
		return get("userCreatedBy");
	}
	
	public void setUserCreatedBy(UserWeb userCreatedBy) {
		set("userCreatedBy", userCreatedBy);
	}
	
/*
	public java.util.Date getDateChanged() {
		return get("dateChanged");
	}
	
	public void setDateChanged(java.util.Date dateChanged) {
		set("dateChanged", dateChanged);
	}
		
	public UserWeb getUserChangedBy() {
		return get("userChangedBy");
	}
	
	public void setUserChangedBy(UserWeb userChangedBy) {
		set("userChangedBy", userChangedBy);
	}
*/
	public java.util.Set<EntityAttributeGroupWeb> getEntityAttributeGroups() {
		return get("entityAttributeGroups");
	}

	public void setEntityAttributeGroups(java.util.Set<EntityAttributeGroupWeb> entityGroups) {
		set("entityAttributeGroups", entityGroups);
	}
	
	public java.util.Set<EntityAttributeWeb> getAttributes() {
		return get("attributes");
	}

	public void setAttributes(java.util.Set<EntityAttributeWeb> attributes) {
		set("attributes", attributes);
	}

	public EntityAttributeGroupWeb findEntityGroupByName(String name) {		
		for (EntityAttributeGroupWeb attributeGroup : getEntityAttributeGroups()) {
			if (attributeGroup.getName().equals(name)) {
				return attributeGroup;
			}
		}
		return null;
	}
	
	public EntityAttributeWeb findEntityAttributeByName(String name) {		
		for (EntityAttributeWeb attribute : getAttributes()) {
			if (attribute.getName().equals(name)) {
				return attribute;
			}
		}
		return null;
	}
}
