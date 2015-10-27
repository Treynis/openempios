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

public class IdentifierWeb extends BaseModelData
{
	@SuppressWarnings("unused")
    private IdentifierDomainWeb unusedIdentifierDomainWeb;
	
	public IdentifierWeb() {
	}

	public java.util.Date getDateCreated() {
		return get("dateCreated");
	}

	public void setDateCreated(java.util.Date dateCreated) {
		set("dateCreated", dateCreated);
	}

	public java.util.Date getDateVoided() {
		return get("dateVoided");
	}

	public void setDateVoided(java.util.Date dateVoided) {
		set("dateVoided", dateVoided);
	}

	public java.lang.String getIdentifier() {
		return get("identifier");
	}

	public void setIdentifier(java.lang.String identifier) {
		set("identifier", identifier);
	}
	
	public java.lang.Long getIdentifierId() {
		return get("identifierId");
	}
	
	public void setIdentifierId(java.lang.Long identifierId) {
		set("identifierId", identifierId);
	}

	public java.lang.Integer getIdentifierDomainId() {
		return get("identifierDomainId");
	}
	
	public void setIdentifierDomainId(java.lang.Integer identifierDomainId) {
		set("identifierDomainId", identifierDomainId);
	}
	
	public void setIdentifierDomain(IdentifierDomainWeb identifierDomain) {
		set("identifierDomain", identifierDomain);
		set("identifierDomainName", identifierDomain.getIdentifierDomainName());
		set("identifierDomainDescription", identifierDomain.getIdentifierDomainDescription());
		set("namespaceIdentifier", identifierDomain.getNamespaceIdentifier());
		set("universalIdentifier", identifierDomain.getUniversalIdentifier());
		set("universalIdentifierTypeCode", identifierDomain.getUniversalIdentifierTypeCode());
	}
	
	public IdentifierDomainWeb getIdentifierDomain() {
		return get("identifierDomain");
	}

	public java.lang.String getIdentifierDomainName() {
		return get("identifierDomainName");
	}

	public java.lang.String getIdentifierDomainDescription() {
		return get("identifierDomainDescription");
	}
	
	public java.lang.String getNamespaceIdentifier() {
		return get("namespaceIdentifier");
	}
	
	public java.lang.String getUniversalIdentifier() {
		return get("universalIdentifier");
	}
	
	public java.lang.String getUniversalIdentifierTypeCode() {
		return get("universalIdentifierTypeCode");
	}
	
	public void setUniversalIdentifierTypeCode(java.lang.String identifierType) {
		set("universalIdentifierTypeCode", identifierType);
	}
}
