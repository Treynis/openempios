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

public class EntityAttributeGroupWeb extends BaseModelData
{ 
	@SuppressWarnings("unused")
    private EntityWeb unusedEntityWeb;
	
	@SuppressWarnings("unused")
    private EntityAttributeGroupAttributeWeb unusedEntityAttributeGroupAttributeWeb;
	
	public EntityAttributeGroupWeb() {
	}

	public java.lang.Integer getEntityGroupId() {
		return get("entityGroupId");
	}

	public void setEntityGroupId(java.lang.Integer entityGroupId) {
		set("entityGroupId", entityGroupId);
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

	public java.lang.Integer getDisplayOrder() {
		return get("displayOrder");
	}

	public void setDisplayOrder(java.lang.Integer displayOrder) {
		set("displayOrder", displayOrder);
	}

	public EntityWeb getEntity() {
		return get("entity");
	}
	
	public void setEntity(EntityWeb entity) {
		set("entity", entity);
	}

	public java.util.Set<EntityAttributeGroupAttributeWeb> getEntityAttributeGroupAttributes() {
		return get("EntityAttributeGroupAttribute");
	}

	public void setEntityAttributeGroupAttributes(java.util.Set<EntityAttributeGroupAttributeWeb> attributes) {
		set("EntityAttributeGroupAttribute", attributes);
	}
}
