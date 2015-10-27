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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class RecordLink
{
    private Integer entityId;
    private String recordLinkId;
    private Record leftRecord;
    private Record rightRecord;
    private Double weight;
    private Integer vector;
    private String source;
    private String state;

    public RecordLink() {
    }
    
    @XmlElement
    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }
    
    public RecordLink(Integer entityId, String recordLinkId) {
        this.entityId = entityId;
        this.recordLinkId = recordLinkId;
    }

    @XmlElement
    public String getRecordLinkId() {
        return recordLinkId;
    }

    public void setRecordLinkId(String recordLinkId) {
        this.recordLinkId = recordLinkId;
    }

    @XmlElement
    public Record getLeftRecord() {
        return leftRecord;
    }

    public void setLeftRecord(Record leftRecord) {
        this.leftRecord = leftRecord;
    }

    @XmlElement
    public Record getRightRecord() {
        return rightRecord;
    }

    public void setRightRecord(Record rightRecord) {
        this.rightRecord = rightRecord;
    }

    @XmlElement
    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    @XmlElement
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @XmlElement
	public Integer getVector() {
		return vector;
	}

	public void setVector(Integer vector) {
		this.vector = vector;
	}

    @XmlElement
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
