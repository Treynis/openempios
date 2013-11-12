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

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Identifier extends BaseObject implements Serializable
{
	private static final long serialVersionUID = 1755441952879179847L;
	
	private Long identifierId;
	private Integer identifierDomainId;
	private IdentifierDomain identifierDomain;
	private User userCreatedBy;
	private User userVoidedBy;
	private Record record;
	private String identifier;
	private Date dateCreated;
	private Date dateVoided;
	
	public Identifier() {
	}

	public Identifier(Long identifierId, IdentifierDomain identifierDomain, User userByCreatorId,
			Record record, String identifier, Date dateCreated) {
		this.identifierId = identifierId;
		this.identifierDomainId = identifierDomain.getIdentifierDomainId();
		this.identifierDomain = identifierDomain;
		this.userCreatedBy = userByCreatorId;
		this.record = record;
		this.identifier = identifier;
		this.dateCreated = dateCreated;
	}

	@XmlElement
	public Long getIdentifierId() {
		return identifierId;
	}

	public void setIdentifierId(Long identifierId) {
		this.identifierId = identifierId;
	}

	@XmlElement
	public Integer getIdentifierDomainId() {
		return identifierDomainId;
	}

	public void setIdentifierDomainId(Integer identifierDomainId) {
		this.identifierDomainId = identifierDomainId;
	}

	@XmlElement
	public IdentifierDomain getIdentifierDomain() {
		return identifierDomain;
	}

	public void setIdentifierDomain(IdentifierDomain identifierDomain) {
		this.identifierDomain = identifierDomain;
		this.identifierDomainId = identifierDomain.getIdentifierDomainId();
	}

	public User getUserCreatedBy() {
		return userCreatedBy;
	}

	public void setUserCreatedBy(User userCreatedBy) {
		this.userCreatedBy = userCreatedBy;
	}

	public User getUserVoidedBy() {
		return userVoidedBy;
	}

	public void setUserVoidedBy(User userVoidedBy) {
		this.userVoidedBy = userVoidedBy;
	}
	
//    @XmlElement
	public Record getRecord() {
		return record;
	}

	public void setRecord(Record record) {
		this.record = record;
	}

	@XmlElement
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@XmlElement
	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	@XmlElement
	public Date getDateVoided() {
		return dateVoided;
	}

	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((identifierDomain == null) ? 0 : identifierDomain.hashCode());
		result = prime * result + ((identifierId == null) ? 0 : identifierId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Identifier other = (Identifier) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		if (identifierDomain == null) {
			if (other.identifierDomain != null)
				return false;
		} else if (!identifierDomain.equals(other.identifierDomain))
			return false;
		if (identifierId == null) {
			if (other.identifierId != null)
				return false;
		} else if (!identifierId.equals(other.identifierId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Identifier [identifierId=" + identifierId + ", identifierDomainId=" + identifierDomainId
				+ ", identifierDomain=" + identifierDomain + ", userCreatedBy=" + userCreatedBy + ", userVoidedBy="
				+ userVoidedBy + ", record=" + record + ", identifier=" + identifier + ", dateCreated=" + dateCreated
				+ ", dateVoided=" + dateVoided + "]";
	}
}
