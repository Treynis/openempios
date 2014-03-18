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

public class EntityAttributeWeb extends BaseModelData
{ 
	
	@SuppressWarnings("unused")
    private EntityAttributeDatatypeWeb unusedEntityAttributeDatatypeWeb;

	@SuppressWarnings("unused")
    private UserWeb unusedUserWeb;
	
	@SuppressWarnings("unused")
    private EntityAttributeGroupWeb unusedEntityAttributeGroupWeb;
	
	@SuppressWarnings("unused")
    private EntityAttributeValidationWeb unusedEntityAttributeValidationWeb;
	
	public EntityAttributeWeb() {
	}

	public java.lang.Integer getEntityAttributeId() {
		return get("entityAttributeId");
	}

	public void setEntityAttributeId(java.lang.Integer entityAttributeId) {
		set("entityAttributeId", entityAttributeId);
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

	public EntityAttributeDatatypeWeb getDatatype() {
		return get("datatype");
	}

	public void setDatatype(EntityAttributeDatatypeWeb datatype) {
		set("datatype", datatype);
	}	
	
	public java.lang.Integer getDisplayOrder() {
		return get("displayOrder");
	}

	public void setDisplayOrder(java.lang.Integer displayOrder) {
		set("displayOrder", displayOrder);
	}

	public java.lang.Boolean getIndexed() {
		return get("indexed");
	}

	public void setIndexed(java.lang.Boolean indexed) {
		set("indexed", indexed);
	}

    public java.lang.Boolean getSearchable() {
        return get("searchable");
    }

    public void setSearchable(Boolean searchable) {
        set("searchable", searchable);
    }

    public java.lang.Boolean getCaseInsensitive() {
        return get("caseInsensitive");
    }

    public void setCaseInsensitive(Boolean caseInsensitive) {
        set("caseInsensitive", caseInsensitive);
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
	
	public EntityAttributeGroupWeb getEntityAttributeGroup() {
		return get("entityAttributeGroup");
	}

	public void setEntityAttributeGroup(EntityAttributeGroupWeb entityGroup) {
		set("entityAttributeGroup", entityGroup);
	}
	
	public java.util.Set<EntityAttributeValidationWeb> getEntityAttributeValidations() {
		return get("entityAttributeValidations");
	}

	public void setEntityAttributeValidations(java.util.Set<EntityAttributeValidationWeb> entityAttributeValidations) {
		set("entityAttributeValidations", entityAttributeValidations);
	}
}
