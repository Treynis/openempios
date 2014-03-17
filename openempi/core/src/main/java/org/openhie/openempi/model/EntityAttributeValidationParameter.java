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

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@javax.persistence.Entity
@Table(name = "entity_attribute_validation_param")
@GenericGenerator(name = "entity_attribute_validation_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "entity_attribute_validation_seq"),
        @Parameter(name = "increment_size", value = "10"),
        @Parameter(name = "optimizer", value = "hilo")})
public class EntityAttributeValidationParameter extends BaseObject implements Serializable
{
	private static final long serialVersionUID = 1755441952879179847L;

	private Integer entityAttributeValidationParamId;
	private String name;
	private String value;
	private EntityAttributeValidation entityAttributeValidation;
	
	public EntityAttributeValidationParameter() {
	}
	
	@Id	
	@GeneratedValue(generator="entity_attribute_validation_gen")
	@Column(name = "entity_attribute_validation_param_id", unique = true, nullable = false)
	public Integer getEntityAttributeValidationParamId() {
		return entityAttributeValidationParamId;
	}

	public void setEntityAttributeValidationParamId(Integer entityAttributeValidationParamId) {
		this.entityAttributeValidationParamId = entityAttributeValidationParamId;
	}

	@Column(name = "name", nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "value", nullable = false)
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "entity_attribute_validation_id", nullable = false)
	public EntityAttributeValidation getEntityAttributeValidation() {
		return entityAttributeValidation;
	}

	public void setEntityAttributeValidation(EntityAttributeValidation entityAttributeValidation) {
		this.entityAttributeValidation = entityAttributeValidation;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityAttributeValidationParameter other = (EntityAttributeValidationParameter) obj;
		if (entityAttributeValidationParamId == null) {
			if (other.entityAttributeValidationParamId != null)
				return false;
		} else if (!entityAttributeValidationParamId.equals(other.entityAttributeValidationParamId))
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
				+ ((entityAttributeValidationParamId == null) ? 0 : entityAttributeValidationParamId.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "EntityAttributeValidationParameter [entityAttributeValidationParamId="
				+ entityAttributeValidationParamId + ", name=" + name + ", value=" + value + ", entityAttributeValidation="
				+ entityAttributeValidation + "]";
	}
}
