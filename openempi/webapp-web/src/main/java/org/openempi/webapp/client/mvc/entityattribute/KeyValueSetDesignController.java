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
package org.openempi.webapp.client.mvc.entityattribute;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.mvc.Controller;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;

public class KeyValueSetDesignController extends Controller
{
	private KeyValueSetDesignView keyValueSetDesignView;

	public KeyValueSetDesignController() {		
		this.registerEventTypes(AppEvents.KeyValueSetView);
	}

	@Override
	protected void initialize() {
		keyValueSetDesignView = new KeyValueSetDesignView(this);
	}

	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (type == AppEvents.KeyValueSetView) {
			forwardToView(keyValueSetDesignView, event);			
		} 
	}
}
