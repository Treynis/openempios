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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;


@javax.persistence.Entity
@Table(name = "entity_attribute_group")
@GenericGenerator(name = "entity_attribute_group_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "entity_attribute_group_seq"),
        @Parameter(name = "optimizer", value = "hilo")})
public class EntityAttributeGroup extends BaseObject implements Serializable
{
	private static final long serialVersionUID = 1755441952879179847L;
	
	private Integer entityAttributeGroupId;
	private String name;
	private String displayName;
	private Integer displayOrder;
	private Entity entity;
	private Set<EntityAttributeGroupAttribute> entityAttributes = new HashSet<EntityAttributeGroupAttribute>();

	public EntityAttributeGroup() {
	}
	
	@Id
	@GeneratedValue(generator="entity_attribute_group_gen")
	@Column(name = "entity_attribute_group_id", unique = true, nullable = false)
	public Integer getEntityAttributeGroupId() {
		return entityAttributeGroupId;
	}

	public void setEntityAttributeGroupId(Integer entityAttributeGroupId) {
		this.entityAttributeGroupId = entityAttributeGroupId;
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

	@Column(name = "display_order", nullable = false)
	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "entity_version_id", nullable = false)
	public Entity getEntity() {
		return entity;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "entityAttributeGroup")
	@Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE,
        org.hibernate.annotations.CascadeType.DELETE,
        org.hibernate.annotations.CascadeType.MERGE,
        org.hibernate.annotations.CascadeType.PERSIST,
        org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
	public Set<EntityAttributeGroupAttribute> getEntityAttributes() {
		return entityAttributes;
	}

	public void addEntityAttributeGroupAttribute(EntityAttributeGroupAttribute attribute) {
		entityAttributes.add(attribute);
	}
	
	public void setEntityAttributes(Set<EntityAttributeGroupAttribute> entityAttributes) {
		this.entityAttributes = entityAttributes;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public EntityAttribute findEntityAttributeByName(String name) {
		for (EntityAttributeGroupAttribute attributeGroup : entityAttributes) {
			if (attributeGroup.getEntityAttribute().getName().equals(name)) {
				return attributeGroup.getEntityAttribute();
			}
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityAttributeGroupId == null) ? 0 : entityAttributeGroupId.hashCode());
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
		EntityAttributeGroup other = (EntityAttributeGroup) obj;
		if (entityAttributeGroupId == null) {
			if (other.entityAttributeGroupId != null)
				return false;
		} else if (!entityAttributeGroupId.equals(other.entityAttributeGroupId))
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
		return "EntityAttributeGroup [entityAttributeGroupId=" + entityAttributeGroupId + ", name=" + name
				+ ", displayName=" + displayName + ", displayOrder=" + displayOrder + ", entity=" + entity + "]";
	}
}
