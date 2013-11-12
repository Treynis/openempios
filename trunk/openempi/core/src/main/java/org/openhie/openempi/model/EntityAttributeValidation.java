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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@javax.persistence.Entity
@Table(name = "entity_attribute_validation")
@GenericGenerator(name = "entity_attribute_validation_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "entity_attribute_validation_seq"),
        @Parameter(name = "optimizer", value = "hilo")})
public class EntityAttributeValidation extends BaseObject implements Serializable
{
	private static final long serialVersionUID = 1755441952879179847L;
	
	private Integer entityAttributeValidationId;
	private String name;
	private String displayName;
	private EntityAttribute entityAttribute;
	private Set<EntityAttributeValidationParameter> parameters = new HashSet<EntityAttributeValidationParameter>();
	
	public EntityAttributeValidation() {
	}
	
	@Id
	@GeneratedValue(generator="entity_attribute_validation_gen")
	@Column(name = "entity_attribute_validation_id", unique = true, nullable = false)
	public Integer getEntityAttributeValidationId() {
		return entityAttributeValidationId;
	}

	public void setEntityAttributeValidationId(Integer entityAttributeValidationId) {
		this.entityAttributeValidationId = entityAttributeValidationId;
	}

	@Column(name = "name", nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "display_name", nullable = false)
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "entity_attribute_id")
	public EntityAttribute getEntityAttribute() {
		return entityAttribute;
	}

	public void setEntityAttribute(EntityAttribute entityAttribute) {
		this.entityAttribute = entityAttribute;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "entityAttributeValidation")
	@Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE,
        org.hibernate.annotations.CascadeType.DELETE,
        org.hibernate.annotations.CascadeType.MERGE,
        org.hibernate.annotations.CascadeType.PERSIST,
        org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
	public Set<EntityAttributeValidationParameter> getParameters() {
		return parameters;
	}

	public void setParameters(Set<EntityAttributeValidationParameter> parameters) {
		this.parameters = parameters;
	}

	public void addParameter(EntityAttributeValidationParameter parameter) {
		parameters.add(parameter);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityAttributeValidation other = (EntityAttributeValidation) obj;
		if (entityAttributeValidationId == null) {
			if (other.entityAttributeValidationId != null)
				return false;
		} else if (!entityAttributeValidationId.equals(other.entityAttributeValidationId))
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
		result = prime * result
				+ ((entityAttributeValidationId == null) ? 0 : entityAttributeValidationId.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "EntityAttributeValidation [entityAttributeVdalidationId=" + entityAttributeValidationId + ", name="
				+ name + ", displayName=" + displayName + ", entityAttribute=" + entityAttribute + "]";
	}
}
