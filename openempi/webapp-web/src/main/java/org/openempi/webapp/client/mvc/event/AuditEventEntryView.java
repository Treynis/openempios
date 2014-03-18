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
package org.openempi.webapp.client.mvc.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.mvc.BaseEntityView;
import org.openempi.webapp.client.mvc.Controller;
import org.openempi.webapp.client.domain.AuthenticationException;

import org.openempi.webapp.client.model.AuditEventEntryListWeb;
import org.openempi.webapp.client.model.AuditEventEntryWeb;
import org.openempi.webapp.client.model.AuditEventTypeWeb;
import org.openempi.webapp.client.model.AuditEventSearchCriteriaWeb;
import org.openempi.webapp.client.model.EntityAttributeGroupWeb;
import org.openempi.webapp.client.model.EntityAttributeWeb;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.IdentifierWeb;
import org.openempi.webapp.client.model.RecordWeb;
import org.openempi.webapp.client.model.UserWeb;
import org.openempi.webapp.client.ui.util.Utility;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AuditEventEntryView extends BaseEntityView
{
    public static final Integer PAGE_SIZE = new Integer(10);

    private EntityWeb currentEntity;

    private Grid<AuditEventEntryWeb> grid;
    private ListStore<AuditEventEntryWeb> store = new ListStore<AuditEventEntryWeb>();

    private TextField<String> selectedEvenyTypes;
    private ListView<AuditEventTypeWeb> evenyTypes;
    private ListStore<AuditEventTypeWeb> eventTypesStore = new ListStore<AuditEventTypeWeb>();
    private DateField startDate;
    private DateField endDate;

    private Status status;
    private Button searchButton;
    private Button cancelButton;

    private RpcProxy<PagingLoadResult<AuditEventEntryWeb>> proxy;
    private BasePagingLoader<PagingLoadResult<AuditEventEntryWeb>> pagingLoader;
    private PagingToolBar pagingToolBar;

    private AuditEventSearchCriteriaWeb searchCriteria;

    private Dialog refRecordInfoDialog = null;
    private LayoutContainer formButtonContainer;
    private LayoutContainer identifierContainer;
    private LayoutContainer topContainer;
    private LayoutContainer formContainer;
    private FormPanel topFormPanel;
    private FormPanel leftFormPanel;
    private FormPanel rightFormPanel;

    private ListStore<IdentifierWeb> identifierStore = new ListStore<IdentifierWeb>();
    private Grid<IdentifierWeb> identifierGrid;

    private LayoutContainer container;

    public AuditEventEntryView(Controller controller) {
        super(controller);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleEvent(AppEvent event) {
        if (event.getType() == AppEvents.AuditEventEntryView) {

            searchCriteria = null;
            currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);

            initUI();

            if (Registry.get(Constants.AUDIT_EVENT_TYPE_CODES) != null) {
                List<AuditEventTypeWeb> auditEventTypes = (List<AuditEventTypeWeb>) Registry
                        .get(Constants.AUDIT_EVENT_TYPE_CODES);
                /*
                 * for (AuditEventTypeWeb type : auditEventTypes) { Info.display("Information", "Event Types: "+
                 * type.getAuditEventTypeCd() + ", " + type.getAuditEventTypeName()); }
                 */
                eventTypesStore.removeAll();
                eventTypesStore.add(auditEventTypes);
            }

        } else if (event.getType() == AppEvents.Logout) {

            Dispatcher.get().dispatch(AppEvents.Logout);

        } else if (event.getType() == AppEvents.AuditEventReceived) {

            // Info.display("Information", "EventReceived");
            store.removeAll();

            AuditEventEntryListWeb events = (AuditEventEntryListWeb) event.getData();
            if (events.getAuditEventEntries() != null) {
                store.add(events.getAuditEventEntries());
            }

            grid.getSelectionModel().select(0, true);
            grid.getSelectionModel().deselect(0);

            status.hide();
            searchButton.unmask();

        } else if (event.getType() == AppEvents.EntityByIdRequest) {

            RecordWeb record = (RecordWeb) event.getData();

            if (record != null) {
                identifierStore.removeAll();

                buildRefRecordInfoDialog();
                refRecordInfoDialog.show();

                displayEntityRecord(attributeFieldMap, record);
                displayEntityIdentifier(record);
            }

        } else if (event.getType() == AppEvents.Error) {
            String message = event.getData();
            MessageBox.alert("Information", "Failure: " + message, null);
        }
    }

    private FormPanel setupButtonPanel(int tabIndex) {
        FormPanel buttonPanel = new FormPanel();
        buttonPanel.setHeaderVisible(false);
        buttonPanel.setBodyBorder(false);
        buttonPanel.setStyleAttribute("paddingRight", "10px");
        buttonPanel.setButtonAlign(HorizontalAlignment.CENTER);

        searchButton = new Button("Search Event", IconHelper.create("images/search_icon_16x16.png"),
                new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce) {

                        searchCriteria = new AuditEventSearchCriteriaWeb();
                        if (!startDate.validate() || !endDate.validate()) {
                            Info.display("Warning",
                                    "You must enter valid format of date before pressing the search button.");
                            return;
                        }
                        searchCriteria.setStartDateTime(Utility.DateTimeToString(startDate.getValue()));
                        searchCriteria.setEndDateTime(Utility.DateTimeToString(endDate.getValue()));

                        Set<AuditEventTypeWeb> ids = new HashSet<AuditEventTypeWeb>();
                        if (evenyTypes.getSelectionModel().getSelection().size() > 0) {
                            List<AuditEventTypeWeb> types = evenyTypes.getSelectionModel().getSelection();
                            for (AuditEventTypeWeb type : types) {
                                // Info.display("Information", "Event Type: "+type.getAuditEventTypeName());
                                ids.add(type);
                            }
                            searchCriteria.setAuditEventTypes(ids);
                        }

                        searchCriteria.setFirstResult(new Integer(0));
                        searchCriteria.setMaxResults(PAGE_SIZE);

                        // // controller.handleEvent(new AppEvent(AppEvents.AuditEventRequest, searchCriteria));

                        PagingLoadConfig config = new BasePagingLoadConfig();
                        config.setOffset(0);
                        config.setLimit(PAGE_SIZE);

                        pagingLoader.load(config);

                        status.show();
                        searchButton.mask();
                    }
                });

        cancelButton = new Button("Reset", new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                selectedEvenyTypes.setValue("");
                evenyTypes.getSelectionModel().deselectAll();
                startDate.clear();
                endDate.clear();
                searchButton.disable();
            }
        });

        searchButton.setTabIndex(tabIndex++);
        cancelButton.setTabIndex(tabIndex++);
        searchButton.disable();

        status = new Status();
        status.setBusy("please wait...");
        buttonPanel.getButtonBar().add(status);
        status.hide();

        buttonPanel.getButtonBar().setSpacing(15);
        buttonPanel.addButton(searchButton);
        buttonPanel.addButton(cancelButton);

        return buttonPanel;
    }

    private PagingToolBar setupRpcProxy() {
        // Rpc Proxy
        proxy = new RpcProxy<PagingLoadResult<AuditEventEntryWeb>>()
        {

            @Override
            public void load(final Object loadConfig, final AsyncCallback<PagingLoadResult<AuditEventEntryWeb>> callback) {

                if (searchCriteria == null) {
                    callback.onSuccess(new BasePagingLoadResult<AuditEventEntryWeb>(null, 0, 0));
                    return;
                }

                // set page offset for searchCriteria
                searchCriteria.setFirstResult(((PagingLoadConfig) loadConfig).getOffset());

                getController().getAuditEventDataService().getAuditEventEntriesBySearch(searchCriteria,
                        new AsyncCallback<AuditEventEntryListWeb>()
                        {
                            public void onFailure(Throwable caught) {

                                if (caught instanceof AuthenticationException) {
                                    Dispatcher.get().dispatch(AppEvents.Logout);
                                    return;
                                }
                                Dispatcher.forwardEvent(AppEvents.Error, caught);
                            }

                            public void onSuccess(AuditEventEntryListWeb result) {
                                // Info.display("Information", "The persons: "
                                // +((PagingLoadConfig)loadConfig).getOffset());

                                // PagingLoadConfig configuration
                                callback.onSuccess(new BasePagingLoadResult<AuditEventEntryWeb>(result
                                        .getAuditEventEntries(), ((PagingLoadConfig) loadConfig).getOffset(), result
                                        .getTotalCount()));
                            }
                        });
            }
        };

        // Page loader
        pagingLoader = new BasePagingLoader<PagingLoadResult<AuditEventEntryWeb>>(proxy);
        pagingLoader.setRemoteSort(true);
        pagingLoader.addLoadListener(new LoadListener()
        {
            // After the loader be completely filled, remove the mask
            public void loaderLoad(LoadEvent le) {
                status.hide();
                searchButton.unmask();

                grid.unmask();
            }
        });

        store = new ListStore<AuditEventEntryWeb>(pagingLoader);

        PagingToolBar pagingToolBar = new PagingToolBar(PAGE_SIZE);
        pagingToolBar.bind(pagingLoader);
        return pagingToolBar;
    }

    private Grid<AuditEventEntryWeb> setupGrid() {

        GridCellRenderer<AuditEventEntryWeb> refButtonRenderer = new GridCellRenderer<AuditEventEntryWeb>()
        {
            private boolean init;

            @Override
            public Object render(final AuditEventEntryWeb model, String property,
                    com.extjs.gxt.ui.client.widget.grid.ColumnData config, int rowIndex, int colIndex,
                    ListStore<AuditEventEntryWeb> store, Grid<AuditEventEntryWeb> grid) {

                if (!init) {
                    init = true;
                    grid.addListener(Events.ColumnResize, new Listener<GridEvent<AuditEventEntryWeb>>()
                    {

                        public void handleEvent(GridEvent<AuditEventEntryWeb> be) {
                            for (int i = 0; i < be.getGrid().getStore().getCount(); i++) {
                                if (be.getGrid().getView().getWidget(i, be.getColIndex()) != null
                                        && be.getGrid().getView().getWidget(i, be.getColIndex()) instanceof BoxComponent) {
                                    ((BoxComponent) be.getGrid().getView().getWidget(i, be.getColIndex())).setWidth(be
                                            .getWidth() - 10);
                                }
                            }
                        }
                    });
                }

                AuditEventEntryWeb auditEvent = model;
                Long refRecordId = auditEvent.getRefRecordId();
                Button b = null;
                if (refRecordId != null) {
                    b = new Button("" + refRecordId.longValue(), new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce) {

                            AuditEventEntryWeb auditEvent = model;
                            // Info.display("Information", "Ref Record: "+ auditEvent.getRefRecordId());
                            if (currentEntity != null) {
                                AppEvent event = new AppEvent(AppEvents.EntityByIdRequest);
                                event.setData("entityModel", currentEntity);
                                event.setData("recordId", auditEvent.getRefRecordId());
                                controller.handleEvent(event);
                            }
                        }
                    });
                    b.setWidth(grid.getColumnModel().getColumnWidth(colIndex) - 10);
                    b.setToolTip("Click for record detail information");

                }

                return b;
            }
        };

        GridCellRenderer<AuditEventEntryWeb> altButtonRenderer = new GridCellRenderer<AuditEventEntryWeb>()
        {
            private boolean init;

            @Override
            public Object render(final AuditEventEntryWeb model, String property,
                    com.extjs.gxt.ui.client.widget.grid.ColumnData config, int rowIndex, int colIndex,
                    ListStore<AuditEventEntryWeb> store, Grid<AuditEventEntryWeb> grid) {

                if (!init) {
                    init = true;
                    grid.addListener(Events.ColumnResize, new Listener<GridEvent<AuditEventEntryWeb>>()
                    {

                        public void handleEvent(GridEvent<AuditEventEntryWeb> be) {
                            for (int i = 0; i < be.getGrid().getStore().getCount(); i++) {
                                if (be.getGrid().getView().getWidget(i, be.getColIndex()) != null
                                        && be.getGrid().getView().getWidget(i, be.getColIndex()) instanceof BoxComponent) {
                                    ((BoxComponent) be.getGrid().getView().getWidget(i, be.getColIndex())).setWidth(be
                                            .getWidth() - 10);
                                }
                            }
                        }
                    });
                }

                AuditEventEntryWeb auditEvent = model;
                Long refRecordId = auditEvent.getAltRefRecordId();
                Button b = null;
                if (refRecordId != null) {
                    b = new Button("" + refRecordId.longValue(), new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce) {

                            AuditEventEntryWeb auditEvent = model;
                            // Info.display("Information", "Alt Ref Record: "+ auditEvent.getAltRefRecordId());
                            if (currentEntity != null) {
                                AppEvent event = new AppEvent(AppEvents.EntityByIdRequest);
                                event.setData("entityModel", currentEntity);
                                event.setData("recordId", auditEvent.getAltRefRecordId());
                                controller.handleEvent(event);
                            }
                        }
                    });
                    b.setWidth(grid.getColumnModel().getColumnWidth(colIndex) - 10);
                    b.setToolTip("Click for record detail information");
                }

                return b;
            }
        };

        // Render to display "Created By" as firstName LastName (Account)
        GridCellRenderer<AuditEventEntryWeb> adminRenderer = new GridCellRenderer<AuditEventEntryWeb>()
        {
            @Override
            public Object render(final AuditEventEntryWeb model, String property,
                    com.extjs.gxt.ui.client.widget.grid.ColumnData config, int rowIndex, int colIndex,
                    ListStore<AuditEventEntryWeb> store, Grid<AuditEventEntryWeb> grid) {

                AuditEventEntryWeb auditEvent = model;
                UserWeb user = auditEvent.getUserCreatedBy();
                String createdBy = "";
                if (user != null) {
                    createdBy = user.getFirstName() + " " + user.getLastName() + " (" + user.getUsername() + ")";
                }
                return createdBy;
            }
        };

        // Audit Event Grid
        ColumnConfig typeColumn = new ColumnConfig("auditEventType.auditEventTypeName", "Event Type", 150);
        ColumnConfig descriptionColumn = new ColumnConfig("auditEventDescription", "Description", 350);
        ColumnConfig dateTimeColumn = new ColumnConfig("dateCreated", "Date Created", 120);
        dateTimeColumn.setDateTimeFormat(DateTimeFormat.getShortDateTimeFormat());
        ColumnConfig refPersonColumn = new ColumnConfig("refRecordId", "Reference Record", 150);
        refPersonColumn.setRenderer(refButtonRenderer);
        ColumnConfig altRefPersonColumn = new ColumnConfig("altRefRecordId", "Alt Reference Record", 150);
        altRefPersonColumn.setRenderer(altButtonRenderer);
        ColumnConfig userCreatedByColumn = new ColumnConfig("userCreatedBy.username", "Created By", 150);
        userCreatedByColumn.setRenderer(adminRenderer);
        List<ColumnConfig> config = new ArrayList<ColumnConfig>();
        config.add(typeColumn);
        config.add(descriptionColumn);
        config.add(dateTimeColumn);
        config.add(refPersonColumn);
        config.add(altRefPersonColumn);
        config.add(userCreatedByColumn);

        final ColumnModel cm = new ColumnModel(config);

        Grid<AuditEventEntryWeb> grid = new Grid<AuditEventEntryWeb>(store, cm);
        grid.setBorders(true);
        grid.setAutoWidth(true);
        grid.setStripeRows(true);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.setHeight(330);

        grid.getSelectionModel().addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent<AuditEventEntryWeb>>()
                {

                    public void handleEvent(SelectionChangedEvent<AuditEventEntryWeb> be) {
                        List<AuditEventEntryWeb> selection = be.getSelection();
                    }
                });

        grid.addListener(Events.SortChange, new Listener<GridEvent<AuditEventEntryWeb>>()
        {
            public void handleEvent(GridEvent<AuditEventEntryWeb> be) {
                AuditEventEntryWeb selectField = be.getGrid().getSelectionModel().getSelectedItem();
            }
        });

        return grid;
    }

    private void initUI() {
        long time = new java.util.Date().getTime();
        GWT.log("Initializing the UI ", null);

        container = new LayoutContainer();
        container.setLayout(new CenterLayout());

        // Rpc Proxy setup
        pagingToolBar = setupRpcProxy();

        // Audit event grid setup
        grid = setupGrid();

        // Panel
        ContentPanel cp = new ContentPanel();
        cp.setHeading("Entity Event Viewer");
        cp.setFrame(true);
        cp.setIcon(IconHelper.create("images/search_icon_16x16.png"));
        cp.setLayout(new FormLayout());
        cp.setSize(1100, 630);

        // Search Container
        ContentPanel searchContainer = new ContentPanel();
        searchContainer.setHeaderVisible(false);
        FormLayout searchFormLayout = new FormLayout();
        searchFormLayout.setLabelWidth(130);
        searchFormLayout.setDefaultWidth(770);

        searchContainer.setLayout(searchFormLayout);

        selectedEvenyTypes = new TextField<String>();
        selectedEvenyTypes.setFieldLabel("Selected Event Types");
        selectedEvenyTypes.setReadOnly(true);

        evenyTypes = new ListView<AuditEventTypeWeb>();
        evenyTypes.setDisplayProperty("auditEventTypeName");
        evenyTypes.setWidth(220);
        evenyTypes.setHeight(110);
        evenyTypes.setStore(eventTypesStore);
        evenyTypes.getSelectionModel().addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent<AuditEventTypeWeb>>()
                {

                    public void handleEvent(SelectionChangedEvent<AuditEventTypeWeb> be) {
                        List<AuditEventTypeWeb> selections = be.getSelection();
                        String selectedTypes = "";
                        for (AuditEventTypeWeb type : selections) {
                            if (selectedTypes.isEmpty()) {
                                selectedTypes = type.getAuditEventTypeName();
                            } else {
                                selectedTypes = selectedTypes + ", " + type.getAuditEventTypeName();
                            }
                        }
                        selectedEvenyTypes.setValue(selectedTypes);

                        if (selectedTypes.isEmpty()) {
                            searchButton.disable();
                        } else {
                            searchButton.enable();
                        }
                    }
                });

        DateTimePropertyEditor dateFormat = new DateTimePropertyEditor("yyyy-MM-dd HH:mm");
        startDate = new DateField();
        startDate.setFieldLabel("Start Date Time");
        startDate.setToolTip("yyyy-MM-dd HH:mm");
        startDate.setPropertyEditor(dateFormat);

        endDate = new DateField();
        endDate.setFieldLabel("End Date Time");
        endDate.setToolTip("yyyy-MM-dd HH:mm");
        endDate.setPropertyEditor(dateFormat);

        LayoutContainer main = new LayoutContainer();
        main.setLayout(new ColumnLayout());

        LayoutContainer left = new LayoutContainer();
        left.setStyleAttribute("paddingRight", "10px");
        FormLayout layout = new FormLayout();
        layout.setLabelWidth(130);
        layout.setDefaultWidth(220);
        // layout.setLabelAlign(LabelAlign.TOP);
        left.setLayout(layout);
        left.add(startDate);

        LayoutContainer right = new LayoutContainer();
        right.setStyleAttribute("paddingLeft", "10px");
        layout = new FormLayout();
        // layout.setLabelAlign(LabelAlign.TOP);
        layout.setLabelWidth(130);
        layout.setDefaultWidth(220);
        right.setLayout(layout);
        right.add(endDate);

        main.add(left, new ColumnData(.5));
        main.add(right, new ColumnData(.5));

        HBoxLayoutData dataSelectedTypes = new HBoxLayoutData(new Margins(5, 0, 0, 0));
        searchContainer.add(selectedEvenyTypes, dataSelectedTypes);
        HBoxLayoutData dataTypes = new HBoxLayoutData(new Margins(5, 0, 5, 135));
        searchContainer.add(evenyTypes, dataTypes);

        searchContainer.add(main);
        HBoxLayoutData dataButtons = new HBoxLayoutData(new Margins(0, 0, 5, 0));
        searchContainer.add(setupButtonPanel(3), dataButtons);

        cp.add(searchContainer);
        cp.add(grid);
        cp.setBottomComponent(pagingToolBar);

        container.add(cp);

        LayoutContainer wrapper = (LayoutContainer) Registry.get(Constants.CENTER_PANEL);
        wrapper.removeAll();
        wrapper.add(container);
        wrapper.layout();
        GWT.log("Done Initializing the UI in " + (new java.util.Date().getTime() - time), null);
    }

    // RefRecordInfoDialog
    private void buildRefRecordInfoDialog() {

        if (refRecordInfoDialog != null) {
            return;
        }
        refRecordInfoDialog = new Dialog();
        refRecordInfoDialog.setBodyBorder(false);
        refRecordInfoDialog.setWidth(940);
        refRecordInfoDialog.setHeight(540);
        refRecordInfoDialog.setIcon(IconHelper.create("images/information.png"));
        refRecordInfoDialog.setHeading("Reference Record Information");
        refRecordInfoDialog.setButtons(Dialog.OK);
        refRecordInfoDialog.setModal(true);
        refRecordInfoDialog.getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {

                // refPersonInfoDialog.hide();
                refRecordInfoDialog.close();
            }
        });

        ContentPanel cp = new ContentPanel();
        cp.setFrame(false);
        cp.setLayout(new BorderLayout());
        cp.setSize(930, 500);

        formButtonContainer = new LayoutContainer();
        formButtonContainer.setScrollMode(Scroll.AUTOY);

        TableLayout identlayout = new TableLayout(2);
        identlayout.setWidth("100%");
        identlayout.setCellSpacing(5);
        identlayout.setCellVerticalAlign(VerticalAlignment.TOP);

        FormLayout toplayout = new FormLayout();

        TableLayout formlayout = new TableLayout(2);
        formlayout.setWidth("930"); // "100%"
        formlayout.setCellSpacing(5);
        formlayout.setCellVerticalAlign(VerticalAlignment.TOP);

        identifierContainer = new LayoutContainer();
        ;
        identifierContainer.setLayout(identlayout);
        FormPanel identifierPanel = setupForm("", 150, 854);
        identifierPanel.add(setupIdentifierfieldSet(865, 1));
        identifierContainer.add(identifierPanel);

        topContainer = new LayoutContainer();
        ;
        topContainer.setLayout(toplayout);
        topFormPanel = setupForm("", 150, 400);
        topFormPanel.setStyleAttribute("padding-left", "15px");

        formContainer = new LayoutContainer();
        formContainer.setLayout(formlayout);
        leftFormPanel = setupForm("", 150, 400);
        rightFormPanel = setupForm("", 150, 400);

        if (currentEntity != null) {

            if (currentEntity.getAttributes() != null) {

                // Groups
                List<EntityAttributeGroupWeb> sortedAttributeGroups = null;
                if (currentEntity.getEntityAttributeGroups() != null) {
                    sortedAttributeGroups = new ArrayList<EntityAttributeGroupWeb>(currentEntity
                            .getEntityAttributeGroups().size());
                    for (EntityAttributeGroupWeb entityGroup : currentEntity.getEntityAttributeGroups()) {
                        // Info.display("Entity Group:", entityGroup.getName()+ "; "+entityGroup.getDisplayOrder());
                        sortedAttributeGroups.add(entityGroup);
                    }
                    Collections.sort(sortedAttributeGroups, GROUP_DISPLAY_ORDER);
                }

                // Attributes
                List<EntityAttributeWeb> sortedEntityAttributes = new ArrayList<EntityAttributeWeb>(currentEntity
                        .getAttributes().size());
                if (currentEntity.getAttributes() != null) {
                    for (EntityAttributeWeb entityAttribute : currentEntity.getAttributes()) {
                        sortedEntityAttributes.add(entityAttribute);
                    }
                    // sort by display order
                    Collections.sort(sortedEntityAttributes, ATTRIBUTE_DISPLAY_ORDER);
                }

                attributeFieldMap = new HashMap<String, Field<?>>();

                // Attributes with no group
                for (EntityAttributeWeb entityAttribute : sortedEntityAttributes) {
                    // Info.display("Attribute:", entityAttribute.getName()
                    // +"; "+entityAttribute.getDatatype().getDataTypeCd());

                    if (entityAttribute.getEntityAttributeGroup() == null) {
                        Field<?> field = createField(entityAttribute, true, false);
                        if (field != null) {
                            attributeFieldMap.put(entityAttribute.getName(), field);
                            topFormPanel.add(field);
                        }
                    }
                }

                // Attributes with group
                if (sortedAttributeGroups != null) {
                    boolean leftForm = true;
                    for (EntityAttributeGroupWeb attributeGroup : sortedAttributeGroups) {

                        FieldSet groupfieldSet = createGroupFields(attributeFieldMap, attributeGroup,
                                sortedEntityAttributes, false);

                        if (groupfieldSet != null) {
                            if (leftForm) {
                                leftFormPanel.add(groupfieldSet);
                                leftForm = false;
                            } else {
                                rightFormPanel.add(groupfieldSet);
                                leftForm = true;
                            }
                        }
                    }
                }
            }
        }

        topContainer.add(topFormPanel);
        formContainer.add(leftFormPanel);
        formContainer.add(rightFormPanel);

        formButtonContainer.add(identifierContainer);
        formButtonContainer.add(topContainer);
        formButtonContainer.add(formContainer);

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        data.setMargins(new Margins(4, 2, 4, 2));

        cp.add(formButtonContainer, data);

        refRecordInfoDialog.add(cp);
    }

    private FieldSet setupIdentifierfieldSet(int widthPanel, int tabIndex) {
        FieldSet identifierfieldSet = new FieldSet();
        identifierfieldSet.setHeading("Identifiers");
        identifierfieldSet.setCollapsible(true);
        identifierfieldSet.setBorders(false);
        FormLayout identifierlayout = new FormLayout();
        identifierlayout.setLabelWidth(150);
        identifierlayout.setDefaultWidth(390); // It is the real function to set the textField width
        identifierfieldSet.setLayout(identifierlayout);

        ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        cp.setWidth(widthPanel);
        cp.add(setupIdentifierGrid(identifierStore, widthPanel - 33, tabIndex));

        identifierfieldSet.add(cp);

        return identifierfieldSet;
    }

    private Grid<IdentifierWeb> setupIdentifierGrid(ListStore<IdentifierWeb> identifierStore, int widthGrid,
            int tabIndex) {
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        // Columns
        ColumnConfig column;

        column = new ColumnConfig("identifier", "identifier", 200);
        configs.add(column);

        column = new ColumnConfig("identifierDomainName", "Domain Name", 160);
        configs.add(column);

        column = new ColumnConfig("namespaceIdentifier", "Namespace Identifier", 160);
        configs.add(column);

        column = new ColumnConfig("universalIdentifier", "Universal Identifier", 160);
        configs.add(column);

        column = new ColumnConfig("universalIdentifierTypeCode", "Universal Identifier Type", 130);
        configs.add(column);

        ColumnModel cm = new ColumnModel(configs);
        identifierGrid = new Grid<IdentifierWeb>(identifierStore, cm);

        identifierGrid.setStyleAttribute("borderTop", "none");
        identifierGrid.setBorders(true);
        identifierGrid.setStripeRows(true);
        identifierGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        identifierGrid.setWidth(widthGrid);
        identifierGrid.setHeight(86);
        identifierGrid.setTabIndex(tabIndex);

        return identifierGrid;
    }

    private void displayEntityIdentifier(RecordWeb record) {
        if (record.getIdentifiers() == null) {
            return;
        }
        for (IdentifierWeb identifier : record.getIdentifiers()) {
            identifierStore.add(identifier);
        }
    }
}
