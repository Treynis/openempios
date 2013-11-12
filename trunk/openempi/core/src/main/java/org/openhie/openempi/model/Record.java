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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openhie.openempi.util.ConvertingWrapDynaBean;
import org.openhie.openempi.util.DateUtil;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Record implements Serializable
{
	private static final long serialVersionUID = -370897935825063946L;
	private Long recordId;
	private Object object;
	private ConvertingWrapDynaBean dynaBean;
	private RecordTypeDef recordTypeDefinition;
	private Entity entity;
	private List<Identifier> identifiers;
	private Map<String, Object> propertyMap;
	
	public Record() {
	}
	
	public Record(Object object) {
		this.object = object;
		this.dynaBean = new ConvertingWrapDynaBean(object);
	}
	
	public Record(Entity entity) {
		this.entity = entity;
		this.propertyMap = new HashMap<String,Object>();
		this.identifiers = new ArrayList<Identifier>();
		for (EntityAttribute attribute : entity.getAttributes()) {
			propertyMap.put(attribute.getName(), null);
		}
	}
	
	public synchronized RecordTypeDef getRecordDef() {
		if (recordTypeDefinition == null) {
			recordTypeDefinition = new RecordTypeDef(object);
		}
		return recordTypeDefinition;
	}
	
	public String getAsString(String fieldName) {
		Object obj = getPropertyValue(fieldName);
		if (obj == null) {
			return null;
		}
		if (obj instanceof java.util.Date) {
			return DateUtil.getDate((java.util.Date) obj);
		}
		return obj.toString();
	}

	public Object get(String fieldName) {
		return getPropertyValue(fieldName);
	}
	
	public void set(String fieldName, Object value) {
		setPropertyValue(fieldName, value);
	}
	
	public Object getObject() {
		if (object != null) {
			return dynaBean.getInstance();
		}
		return null;
	}
	
    @XmlElement
	public Long getRecordId() {
		return recordId;
	}

	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}
	
	public void addIdentifier(Identifier id) {
		identifiers.add(id);
	}
	
	@XmlElement(name="items")
	@XmlJavaTypeAdapter(MapAdapter.class)
	public Map<String,Object> getPropertyMap() {
	    return propertyMap;
	}
	
	public Set<String> getPropertyNames() {
		if (object != null) {
			return dynaBean.getPropertyNames();
		}
		return propertyMap.keySet();
	}

   @XmlElement
	public List<Identifier> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(List<Identifier> identifiers) {
		this.identifiers = identifiers;
	}

	private void setPropertyValue(String fieldName, Object value) {
		if (object != null) {
			dynaBean.set(fieldName, value);
		} else {
			propertyMap.put(fieldName, value);
		}
	}
	
	private Object getPropertyValue(String fieldName) {
		Object obj = null;
		if (object != null) {
			obj = dynaBean.get(fieldName);
		} else {
			obj = propertyMap.get(fieldName);
		}
		return obj;
	}

	public Entity getEntity() {
		return entity;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Record))
			return false;
		Record castOther = (Record) other;
		return new EqualsBuilder().append(recordId, castOther.recordId).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(recordId).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("recordId", recordId).append("dynaBean", dynaBean).append(
				"recordTypeDefinition", recordTypeDefinition).toString();
	}
	
	public String asString() {
		StringBuffer buf = new StringBuffer(getClass().getName());
		buf.append("[");
		int index=0;
		for (String property : getPropertyNames()) {
			Object value = get(property);
			if (value != null) {
				if (index > 0) {
					buf.append(",");
				}
				buf.append(property).append("=").append(value);
			}
			index++;
		}
		if (getIdentifiers().size() > 0) {
			buf.append("identifiers: " + getIdentifiers().toString());
		}
		buf.append("]");
		return buf.toString();
	}
}
