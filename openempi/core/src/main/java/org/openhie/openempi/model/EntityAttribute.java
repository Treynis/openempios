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

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@javax.persistence.Entity
@Table(name = "entity_attribute")
@GenericGenerator(name = "entity_attribute_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "entity_attribute_seq"),
        @Parameter(name = "increment_size", value = "10"),
        @Parameter(name = "optimizer", value = "hilo")})
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EntityAttribute extends BaseObject implements Serializable
{
	private static final long serialVersionUID = 1755441952879179847L;

	private Integer entityAttributeId;
	private Entity entity;
	private EntityAttributeDatatype datatype;
	private String name;
	private String description;
	private String displayName;
	private Integer displayOrder;
	private Boolean indexed;
    private Boolean searchable;
    private Boolean caseInsensitive;
	private Boolean isCustom;
	private String sourceName;
	private String transformationFunction;
	private String functionParameters;
	private Date dateCreated;
	private User userCreatedBy;
	private Date dateChanged;
	private User userChangedBy;
	private Date dateVoided;
	private User userVoidedBy; 

	@Id
	@GeneratedValue(generator="entity_attribute_gen") 	
	@Column(name = "entity_attribute_id", unique = true, nullable = false)
	public Integer getEntityAttributeId() {
		return entityAttributeId;
	}

	public void setEntityAttributeId(Integer entityAttributeId) {
		this.entityAttributeId = entityAttributeId;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "entity_version_id", nullable = false)
	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "datatype_cd")
    @XmlElement
	public EntityAttributeDatatype getDatatype() {
		return datatype;
	}

	public void setDatatype(EntityAttributeDatatype datatype) {
		this.datatype = datatype;
	}

	@Column(name = "name", nullable = false)
    @XmlElement
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "description", nullable = true)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "display_name", nullable = false)
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Column(name = "display_order", nullable = false)
	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	@Column(name = "indexed", nullable = false)
	public Boolean getIndexed() {
		return indexed;
	}

	public void setIndexed(Boolean indexed) {
		this.indexed = indexed;
	}

    @Column(name = "searchable", nullable = false)
    public Boolean getSearchable() {
        return searchable;
    }

    public void setSearchable(Boolean searchable) {
        this.searchable = searchable;
    }

    @Column(name = "case_insensitive", nullable = false)
    public Boolean getCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(Boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

	@Column(name = "is_custom", nullable = false)
	public Boolean getIsCustom() {
		return isCustom;
	}

	public void setIsCustom(Boolean isCustom) {
		this.isCustom = isCustom;
	}

	@Column(name = "source_name")
	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	@Column(name = "transformation_function")
	public String getTransformationFunction() {
		return transformationFunction;
	}

	public void setTransformationFunction(String transformationFunction) {
		this.transformationFunction = transformationFunction;
	}

	@Column(name = "function_parameters")
	public String getFunctionParameters() {
		return functionParameters;
	}

	public void setFunctionParameters(String functionParameters) {
		this.functionParameters = functionParameters;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityAttributeId == null) ? 0 : entityAttributeId.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		EntityAttribute other = (EntityAttribute) obj;
		if (entityAttributeId == null) {
			if (other.entityAttributeId != null)
				return false;
		} else if (!entityAttributeId.equals(other.entityAttributeId))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EntityAttribute [entityAttributeId=" + entityAttributeId + ", entity=" + entity + ", datatype="
				+ datatype + ", name=" + name + ", description=" + description
				+ ", displayName=" + displayName + ", displayOrder=" + displayOrder+ ", indexed=" + indexed + ", dateCreated=" + dateCreated
				+ ", userCreatedBy=" + userCreatedBy + ", dateChanged=" + dateChanged + ", userChangedBy="
				+ userChangedBy + ", dateVoided=" + dateVoided + ", userVoidedBy=" + userVoidedBy + "]";
	}
}
