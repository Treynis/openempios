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
package org.openempi.webapp.client.mvc.dataprofile;

import java.util.ArrayList;
import java.util.List;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.model.DataProfileWeb;
import org.openempi.webapp.client.mvc.Controller;
import org.openempi.webapp.client.mvc.View;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;

public class DataProfileListView extends View
{
    // private LayoutContainer container;
    private ContentPanel container;
    private LayoutContainer gridContainer;

    private Grid<DataProfileWeb> grid;
    private ListStore<DataProfileWeb> dataProfileStore = new ListStore<DataProfileWeb>();

    private Status status;
    private Button viewButton;
    private Button removeButton;

    private DataProfileWeb currentProfile;

    @SuppressWarnings("unchecked")
    public DataProfileListView(Controller controller) {
        super(controller);
        // fieldNames = (List<ModelPropertyWeb>) Registry.get(Constants.PERSON_MODEL_ALL_ATTRIBUTE_NAMES);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleEvent(AppEvent event) {
        if (event.getType() == AppEvents.DataProfileListView) {
            grid = null;

            initUI();

            showWaitCursor();

        } else if (event.getType() == AppEvents.DataProfileListReceived) {

            List<DataProfileWeb> dataProfileList = (List<DataProfileWeb>) event.getData();
            /*
             * for (DataProfileWeb dataProfileWeb : dataProfileList) { Info.display("Information",
             * ""+dataProfileWeb..getEntity().getName()); }
             */

            displayRecords(dataProfileList);

            showDefaultCursor();

        } else if (event.getType() == AppEvents.DataProfileDeleteComplete) {

            dataProfileStore.remove(currentProfile);

            status.hide();
            removeButton.unmask();

            MessageBox.alert("Information", "The data profile was successfully removed", null);

        } else if (event.getType() == AppEvents.Error) {

            String message = event.getData();
            MessageBox.alert("Information", "Failure: " + message, null);

            showDefaultCursor();

        } else if (event.getType() == AppEvents.Logout) {

            Dispatcher.get().dispatch(AppEvents.Logout);
        }

        container.layout();
    }

    private void displayRecords(List<DataProfileWeb> dataProfiles) {
        if (grid == null) {
            setupProfileListGrid();
        }

        dataProfileStore.removeAll();
        dataProfileStore.add(dataProfiles);
        container.layout();
    }

    private void setupProfileListGrid() {

        ContentPanel cp = new ContentPanel();
        cp.setHeading("Profiles");
        cp.setHeaderVisible(false);
        cp.setBodyBorder(false);
        cp.setLayout(new FillLayout());

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        // Columns

        // Profile Name
        ColumnConfig column = new ColumnConfig("dataProfileId", "Profile Id", 100);
        configs.add(column);

        column = new ColumnConfig("entity.displayName", "Entity", 180);
        configs.add(column);

        column = new ColumnConfig("dateInitiated", "Date Initiated", 150);
        column.setDateTimeFormat(DateTimeFormat.getShortDateTimeFormat());
        configs.add(column);


        column = new ColumnConfig("dateCompleted", "Date Completed", 150);
        column.setDateTimeFormat(DateTimeFormat.getShortDateTimeFormat());
        configs.add(column);

        column = new ColumnConfig("dataSource", "Data Source", 360);
        configs.add(column);

        ColumnModel cm = new ColumnModel(configs);
        grid = new Grid<DataProfileWeb>(dataProfileStore, cm);
        grid.setStyleAttribute("borderTop", "none");
        grid.setBorders(true);
        grid.setStripeRows(true);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // selection event
        grid.getSelectionModel().addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent<DataProfileWeb>>()
                {
                    public void handleEvent(SelectionChangedEvent<DataProfileWeb> be) {
 
                    }
                });

        LayoutContainer buttonContainer = new LayoutContainer();
        HBoxLayout layout = new HBoxLayout();
        layout.setPadding(new Padding(5));
        layout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
        layout.setPack(BoxLayoutPack.START);
        buttonContainer.setLayout(layout);

        HBoxLayoutData layoutData = new HBoxLayoutData(new Margins(0, 3, 0, 0));

        viewButton = new Button(" View Profile", IconHelper.create("images/script_go.png"), new SelectionListener<ButtonEvent>() {
              @Override
              public void componentSelected(ButtonEvent ce) {
                  DataProfileWeb profile = grid.getSelectionModel().getSelectedItem();

                  if (profile == null) {
                      Info.display("Information", "You must first select a frofile to be displayed before pressing the \"View Profile\" button.");
                      return;
                  } else {
                      controller.handleEvent(new AppEvent(AppEvents.DataProfileView, profile.getDataProfileId()));
                  }
              }
        });

        removeButton = new Button(" Remove Profile", IconHelper.create("images/script_delete.png"), new SelectionListener<ButtonEvent>() {
              @Override
              public void componentSelected(ButtonEvent ce) {
                  currentProfile = grid.getSelectionModel().getSelectedItem();

                    if (currentProfile == null) {
                        Info.display("Information", "You must first select a frofile to be removed before pressing the \"Remove Profile\" button.");
                        return;
                    } else {
                        MessageBox.confirm("Confirm", "Are you sure you want to remove this data profile and with all its attributes and values?", listenRemoveProfile);     
                    }
              }
        });
        status = new Status();
        status.setBusy("please wait...");
        status.hide();
        buttonContainer.add(viewButton, layoutData);
        buttonContainer.add(removeButton, layoutData);

        cp.setTopComponent(buttonContainer);
        cp.add(grid);
        gridContainer.add(cp);
    }

    private final Listener<MessageBoxEvent> listenRemoveProfile = new Listener<MessageBoxEvent>() {
        public void handleEvent(MessageBoxEvent ce) {
          Button btn = ce.getButtonClicked();
          // Info.display("MessageBox1 ", "The '{0}' button was pressed", btn.getText());
          if (btn.getText().equals("Yes")) {

              status.show();
              removeButton.mask();

              controller.handleEvent(new AppEvent(AppEvents.DataProfileDelete, currentProfile.getDataProfileId()));
          }

        }
    };

    private void initUI() {
        long time = new java.util.Date().getTime();
        GWT.log("Initializing the UI ", null);

        controller.handleEvent(new AppEvent(AppEvents.DataProfileListRequest));

        // container = new LayoutContainer();
        container = new ContentPanel();
        container.setLayout(new BorderLayout());
        container.setHeading("Data Profiles");

        gridContainer = new LayoutContainer();
        gridContainer.setBorders(false);
        gridContainer.setLayout(new FitLayout());

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        data.setMargins(new Margins(2, 2, 2, 2));
        container.add(gridContainer, data);

        LayoutContainer wrapper = (LayoutContainer) Registry.get(Constants.CENTER_PANEL);
        wrapper.removeAll();
        wrapper.add(container);
        wrapper.layout();
        GWT.log("Done Initializing the UI in " + (new java.util.Date().getTime() - time), null);
    }

    public static void showWaitCursor() {
        DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "wait");
    }

    public static void showDefaultCursor() {
        DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "default");
    }
}
