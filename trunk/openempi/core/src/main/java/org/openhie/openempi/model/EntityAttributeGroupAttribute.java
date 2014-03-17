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
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@javax.persistence.Entity
@Table(name = "entity_attribute_group_attribute")
@GenericGenerator(name = "entity_attribute_group_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "entity_attribute_group_seq"),
        @Parameter(name = "increment_size", value = "10"),
        @Parameter(name = "optimizer", value = "hilo")})
public class EntityAttributeGroupAttribute extends BaseObject implements Serializable
{
	private static final long serialVersionUID = 1755441952879179847L;
	
	private Integer entityAttributeGroupAttributeId;
	private EntityAttribute entityAttribute;
	private EntityAttributeGroup entityAttributeGroup;
	
	@Id
	@GeneratedValue(generator="entity_attribute_group_gen")
	@Column(name = "entity_attribute_group_attribute_id", unique = true, nullable = false)
	public Integer getEntityAttributeGroupAttributeId() {
		return entityAttributeGroupAttributeId;
	}

	public void setEntityAttributeGroupAttributeId(Integer entityAttributeGroupAttributeId) {
		this.entityAttributeGroupAttributeId = entityAttributeGroupAttributeId;
	}
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "entity_attribute_id")
	public EntityAttribute getEntityAttribute() {
		return entityAttribute;
	}
	
	public void setEntityAttribute(EntityAttribute entityAttribute) {
		this.entityAttribute = entityAttribute;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "entity_attribute_group_id", nullable = false)
	public EntityAttributeGroup getEntityAttributeGroup() {
		return entityAttributeGroup;
	}

	public void setEntityAttributeGroup(EntityAttributeGroup entityAttributeGroup) {
		this.entityAttributeGroup = entityAttributeGroup;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityAttribute == null) ? 0 : entityAttribute.hashCode());
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
		EntityAttributeGroupAttribute other = (EntityAttributeGroupAttribute) obj;
		if (entityAttribute == null) {
			if (other.entityAttribute != null)
				return false;
		} else if (!entityAttribute.equals(other.entityAttribute))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EntityAttributeGroupAttribute [entityAttributeGroupAttributeId=" + entityAttributeGroupAttributeId
				+ ", entityAttribute=" + entityAttribute + ", entityAttributeGroup=" + entityAttributeGroup + "]";
	}
}
