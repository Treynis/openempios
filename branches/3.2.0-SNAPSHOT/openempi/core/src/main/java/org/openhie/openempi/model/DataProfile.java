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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "data_profile")
@GenericGenerator(name = "data_profile_seq_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "data_profile_seq"),
        @Parameter(name = "increment_size", value = "10"),
        @Parameter(name = "optimizer", value = "hilo")})
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DataProfile extends BaseObject implements Serializable
{
    private static final long serialVersionUID = -5805814946755578692L;
    
    private Integer dataProfileId;
    private Date dateInitiated;
    private Date dateCompleted;
    private org.openhie.openempi.model.Entity entity;
    private Integer dataSourceId;
    
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="data_profile_seq_gen")     
    @Column(name = "data_profile_id", unique = true, nullable = false)
    public Integer getDataProfileId() {
        return dataProfileId;
    }

    public void setDataProfileId(Integer dataProfileId) {
        this.dataProfileId = dataProfileId;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_initiated", nullable = false, length = 8)
    public Date getDateInitiated() {
        return dateInitiated;
    }

    public void setDateInitiated(Date dateInitiated) {
        this.dateInitiated = dateInitiated;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_completed", length = 8)
    public Date getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(Date dateCompleted) {
        this.dateCompleted = dateCompleted;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entity_version_id")
    public org.openhie.openempi.model.Entity getEntity() {
        return entity;
    }

    public void setEntity(org.openhie.openempi.model.Entity entity) {
        this.entity = entity;
    }

    @Column(name = "data_source_id", nullable = false)
    public Integer getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Integer dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataProfileId == null) ? 0 : dataProfileId.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataProfile other = (DataProfile) obj;
        if (dataProfileId == null) {
            if (other.dataProfileId != null)
                return false;
        } else if (!dataProfileId.equals(other.dataProfileId))
            return false;
        return true;
    }

    public String toString() {
        return "DataProfile [dataProfileId=" + dataProfileId + ", dateInitiated=" + dateInitiated + ", dateCompleted="
                + dateCompleted + ", entity=" + entity + ", dataSourceId=" + dataSourceId + "]";
    }
}
