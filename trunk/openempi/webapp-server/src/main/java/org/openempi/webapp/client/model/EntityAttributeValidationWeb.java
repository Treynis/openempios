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

public class EntityAttributeValidationWeb extends BaseModelData
{ 
	@SuppressWarnings("unused")
    private EntityAttributeValidationParameterWeb unusedEntityAttributeValidationParameterWeb;
	
	public EntityAttributeValidationWeb() {
	}

	public java.lang.Integer getEntityAttributeValidationId() {
		return get("entityAttributeValidationId");
	}

	public void setEntityAttributeValidationId(java.lang.Integer entityAttributeValidationId) {
		set("entityAttributeValidationId", entityAttributeValidationId);
	}
	
	public java.lang.String getValidationName() {
		return get("validationName");
	}

	public void setValidationName(java.lang.String name) {
		set("validationName", name);
	}

	public java.lang.String getDisplayName() {
		return get("displayName");
	}

	public void setDisplayName(java.lang.String displayName) {
		set("displayName", displayName);
	}
	
	public java.util.Set<EntityAttributeValidationParameterWeb> getValidationParameters() {
		return get("validationParameters");
	}

	public void setValidationParameters(java.util.Set<EntityAttributeValidationParameterWeb> validationParameter) {
		set("validationParameters", validationParameter);
	}

}
