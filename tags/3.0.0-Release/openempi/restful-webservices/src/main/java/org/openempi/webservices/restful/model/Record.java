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
package org.openempi.webservices.restful.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.openhie.openempi.model.Identifier;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Record
{
    private Integer entityId;
    private Long recordId;
    private List<FieldValue> fields;
    private List<Identifier> identifiers;

    public Record() {
        fields = new ArrayList<FieldValue>();
        identifiers = new ArrayList<Identifier>();
    }

    @XmlElement
    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    @XmlElement
    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    @XmlElement(name="field")
    public List<FieldValue> getFields() {
        return fields;
    }

    public void setFields(List<FieldValue> fields) {
        this.fields = fields;
    }

    public void addFieldValue(FieldValue fieldValue) {
        fields.add(fieldValue);
    }

    @XmlElement(name="identifier")
    public List<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public void addIdentifier(Identifier id) {
        identifiers.add(id);
    }
}
