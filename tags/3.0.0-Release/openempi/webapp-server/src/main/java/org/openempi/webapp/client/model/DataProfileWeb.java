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
package org.openempi.webapp.client.model;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class DataProfileWeb extends BaseModelData
{
    @SuppressWarnings("unused")
    private EntityWeb unusedEntityWeb;
    
	public DataProfileWeb() {
	}
	
	public java.lang.Integer getDataProfileId() {
		return get("dataProfileId");
	}

	public void setDataProfileId(java.lang.Integer dataProfileId) {
		set("dataProfileId", dataProfileId);
	}
    public java.util.Date getDateInitiated() {
        return get("dateInitiated");
    }
    
    public void setDateInitiated(java.util.Date dateInitiated) {
        set("dateInitiated", dateInitiated);
    }
    
    public java.util.Date getDateCompleted() {
        return get("dateCompleted");
    }
    
    public void setDateCompleted(java.util.Date dateCompleted) {
        set("dateCompleted", dateCompleted);
    }
    
    public EntityWeb getEntity() {
        return get("entity");
    }
    
    public void setEntity(EntityWeb entity) {
        set("entity", entity);
    }
	    	
	public java.lang.Integer getDataSourceId() {
		return get("dataSourceId");
	}

	public void setDataSourceId(java.lang.Integer dataSourceId) {
		set("dataSourceId", dataSourceId);
	}
	
    public String getDataSource() {
        return get("dataSource");
    }

    public void setDataSource(String dataSource) {
        set("dataSource", dataSource);
    }
}
