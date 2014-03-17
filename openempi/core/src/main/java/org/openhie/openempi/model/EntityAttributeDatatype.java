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
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "entity_attribute_datatype")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EntityAttributeDatatype extends BaseObject implements Serializable
{
	private static final long serialVersionUID = 1755441952879179847L;
	public static final int INTEGER_DATATYPE_CD = 1;
	public static final int SHORT_DATATYPE_CD = 2;
	public static final int LONG_DATATYPE_CD = 3;
	public static final int DOUBLE_DATATYPE_CD = 4;
	public static final int FLOAT_DATATYPE_CD = 5;
	public static final int STRING_DATATYPE_CD = 6;
	public static final int BOOLEAN_DATATYPE_CD = 7;
	public static final int DATE_DATATYPE_CD = 8;
	public static final int TIMESTAMP_DATATYPE_CD = 9;
	public static final int LINKSET_DATATYPE_CO = 10;
	public static final int EMBEDDEDSET_DATATYPE_CD = 11;
	
	private Integer datatypeCd;
	private String name;
	private String displayName;

	public EntityAttributeDatatype() {
	}
	
	public EntityAttributeDatatype(Integer datatypeCd) {
		this.datatypeCd = datatypeCd;
	}
	
	public EntityAttributeDatatype(Integer datatypeCd, String name, String displayName) {
		this.datatypeCd = datatypeCd;
		this.name = name;
		this.displayName = displayName;
	}

	@Id
	@Column(name = "datatype_cd", unique = true, nullable = false)
	public Integer getDatatypeCd() {
		return datatypeCd;
	}

	public void setDatatypeCd(Integer datatypeCd) {
		this.datatypeCd = datatypeCd;
	}

	@Column(name = "name", nullable = false, length = 64)
	@XmlElement
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "display_name", nullable = false, length = 64)
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datatypeCd == null) ? 0 : datatypeCd.hashCode());
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
		EntityAttributeDatatype other = (EntityAttributeDatatype) obj;
		if (datatypeCd == null) {
			if (other.datatypeCd != null)
				return false;
		} else if (!datatypeCd.equals(other.datatypeCd))
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
		return "EntityAttributeDatatype [datatypeCd=" + datatypeCd + ", name=" + name + ", displayName=" + displayName
				+ "]";
	}
}
