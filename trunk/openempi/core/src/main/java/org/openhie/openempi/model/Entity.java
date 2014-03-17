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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@javax.persistence.Entity
@Table(name = "entity")
@GenericGenerator(name = "entity_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "entity_seq"),
        @Parameter(name = "increment_size", value = "10"),
        @Parameter(name = "optimizer", value = "hilo")})
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Entity extends BaseObject implements Serializable
{
	private static final long serialVersionUID = 1755441952879179847L;
	
	private Integer entityVersionId;
	private Integer entityId;
	private Integer versionId;
	private String name;
	private String description;
	private String displayName;
	private Date dateCreated;
	private User userCreatedBy;
	private Date dateChanged;
	private User userChangedBy;
	private Date dateVoided;
	private User userVoidedBy;
	private Set<EntityAttribute> attributes = new HashSet<EntityAttribute>();
	private Map<String,EntityAttribute> attributeMapByName;
	
	public Entity() {
	}
	
	@Id
	@GeneratedValue(generator="entity_gen")
	@Column(name = "entity_version_id", unique = true, nullable = false)
	@XmlElement
	public Integer getEntityVersionId() {
		return entityVersionId;
	}

	public void setEntityVersionId(Integer entityVersionId) {
		this.entityVersionId = entityVersionId;
	}

	@Column(name = "entity_id", unique = true, nullable = false)
	public Integer getEntityId() {
		return entityId;
	}

	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}

	@Column(name = "version_id", unique = true, nullable = false)
	public Integer getVersionId() {
		return versionId;
	}

	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}

	@Column(name = "name")
	@XmlElement
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "description")
    @XmlElement
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "display_name")
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_created", nullable = false, length = 8)
	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "created_by_id", nullable = false)
	public User getUserCreatedBy() {
		return userCreatedBy;
	}

	public void setUserCreatedBy(User userCreatedBy) {
		this.userCreatedBy = userCreatedBy;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_changed", length = 8)
	public Date getDateChanged() {
		return dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "changed_by_id")
	public User getUserChangedBy() {
		return userChangedBy;
	}

	public void setUserChangedBy(User userChangedBy) {
		this.userChangedBy = userChangedBy;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_voided", length = 8)
	public Date getDateVoided() {
		return dateVoided;
	}

	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "voided_by_id")
	public User getUserVoidedBy() {
		return userVoidedBy;
	}

	public void setUserVoidedBy(User userVoidedBy) {
		this.userVoidedBy = userVoidedBy;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "entity")
	@Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE,
        org.hibernate.annotations.CascadeType.DELETE,
        org.hibernate.annotations.CascadeType.MERGE,
        org.hibernate.annotations.CascadeType.PERSIST,
        org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
	@XmlElementWrapper
    @XmlElement(name = "attribute")
	public Set<EntityAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Set<EntityAttribute> attributes) {
		this.attributes = attributes;
	}

	public void addAttribute(EntityAttribute attribute) {
		attributes.add(attribute);
	}
	
	public EntityAttribute findAttributeByName(String name) {
		if (attributeMapByName == null) {
			attributeMapByName = new HashMap<String,EntityAttribute>();
			for (EntityAttribute attribute : attributes) {
				attributeMapByName.put(attribute.getName(), attribute);
			}
		}
		return attributeMapByName.get(name);
	}
	
	public boolean hasCustomFields() {
	    if (attributes == null || attributes.size() == 0) {
	        return false;
	    }
	    for (EntityAttribute attribute : attributes) {
	        if (attribute.getIsCustom() == true) {
	            return true;
	        }
	    }
	    return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (entityVersionId == null) {
			if (other.entityVersionId != null)
				return false;
		} else if (!entityVersionId.equals(other.entityVersionId))
			return false;

		if (entityId == null) {
			if (other.entityId != null)
				return false;
		}
		if (versionId == null) {
			if (other.versionId != null)
				return false;
		}
		if (entityId != null && versionId != null &&
				other.entityId != null && other.versionId != null &&
				entityId.equals(other.entityId) && versionId.equals(other.versionId)) {
			return true;
		}
		if (entityId != null && other.entityId != null && !entityId.equals(other.entityId))
			return false;
		if (versionId != null && other.versionId != null && !versionId.equals(other.versionId))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
		result = prime * result + ((entityVersionId == null) ? 0 : entityVersionId.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((versionId == null) ? 0 : versionId.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "Entity [entityVersionId=" + entityVersionId + ", entityId=" + entityId + ", versionId=" + versionId
				+ ", name=" + name + ", description=" + description + ", displayName=" + displayName + ", dateCreated="
				+ dateCreated + ", userCreatedBy=" + userCreatedBy + ", dateChanged=" + dateChanged
				+ ", userChangedBy=" + userChangedBy + ", dateVoided=" + dateVoided + ", userVoidedBy=" + userVoidedBy
				+ "]";
	}
}
