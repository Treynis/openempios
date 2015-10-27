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
package org.openempi.webapp.client.mvc.admin;

import java.util.ArrayList;
import java.util.List;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.filters.DateFilter;
import com.extjs.gxt.ui.client.widget.grid.filters.GridFilters;
import com.extjs.gxt.ui.client.widget.grid.filters.StringFilter;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.Style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

public class InformationPanelView extends View
{

	private ContentPanel container;

    private Grid<BaseModelData> gridInfo;
    private ListStore<BaseModelData> gridInfoStore = new ListStore<BaseModelData>();

	@SuppressWarnings("unchecked")
	public InformationPanelView(Controller controller) {
		super(controller);

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void handleEvent(AppEvent event) {
		if (event.getType() == AppEvents.InformationPanelView) {
            //Info.display("Information", "Information Panel View.");
			initUI();

		} else if (event.getType() == AppEvents.InformationMessage) {
		    // Info.display("Information", "Information Message.");

	        BaseModelData message = (BaseModelData) event.getData();
	        gridInfoStore.add(message);

		} else if (event.getType() == AppEvents.Logout) {

  		    Dispatcher.get().dispatch(AppEvents.Logout);

		} else if (event.getType() == AppEvents.Error) {

			String message = event.getData();
	        MessageBox.alert("Information", "Failure: " + message, null);
		}
	}

	private void initUI() {
		long time = new java.util.Date().getTime();
		GWT.log("Initializing the UI ", null);

        container = new ContentPanel();
        container.setLayout(new FitLayout());
        container.setHeaderVisible(false);

//		infoStore.removeAll();

        GridFilters filters = new GridFilters();
        filters.setLocal(true);

        StringFilter entityFilter = new StringFilter("entity");
        StringFilter messageFilter = new StringFilter("message");
        DateFilter timeFilter = new DateFilter("time");

        filters.addFilter(entityFilter);
        filters.addFilter(messageFilter);
        filters.addFilter(timeFilter);

        ColumnConfig entityColumn = new ColumnConfig("entity", "Entity", 150);
        ColumnConfig messageColumn = new ColumnConfig("message", "Message", 600);
        ColumnConfig timeColumn = new ColumnConfig("time", "Time", 150);
        // timeColumn.setDateTimeFormat(DateTimeFormat.getShortDateTimeFormat());
        DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
        timeColumn.setDateTimeFormat(dtf);

        List<ColumnConfig> config = new ArrayList<ColumnConfig>();
        config.add(entityColumn);
        config.add(messageColumn);
        config.add(timeColumn);

        final ColumnModel cm = new ColumnModel(config);
        gridInfoStore.sort("time", Style.SortDir.DESC);

        gridInfo = new Grid<BaseModelData>(gridInfoStore, cm);
        gridInfo.setBorders(false);
        gridInfo.setAutoWidth(true);
        gridInfo.setStripeRows(true);
        gridInfo.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        gridInfo.addPlugin(filters);
        gridInfo.setHeight(200);


        BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        data.setMargins(new Margins(4, 2, 4, 2));
        container.add(gridInfo, data);

		LayoutContainer wrapper = (LayoutContainer) Registry.get(Constants.SOUTH_PANEL);
		wrapper.removeAll();
		wrapper.add(container);
		wrapper.layout();
		GWT.log("Done Initializing the UI in " + (new java.util.Date().getTime() - time), null);
	}

}
