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
package org.openempi.webapp.client.mvc.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.domain.AuthenticationException;
import org.openempi.webapp.client.model.EntityAttributeValidationParameterWeb;
import org.openempi.webapp.client.model.EntityAttributeValidationWeb;
import org.openempi.webapp.client.model.EntityAttributeWeb;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.ModelPropertyWeb;
import org.openempi.webapp.client.model.RecordLinkWeb;
import org.openempi.webapp.client.model.RecordListWeb;
import org.openempi.webapp.client.model.RecordSearchCriteriaWeb;
import org.openempi.webapp.client.model.RecordWeb;
import org.openempi.webapp.client.mvc.Controller;
import org.openempi.webapp.client.ui.util.AttributeDatatype;
import org.openempi.webapp.client.ui.util.Utility;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.RowExpander;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.core.XTemplate;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FixedSearchEntityView extends BaseSearchEntityView
{
    public static final Integer PAGE_SIZE = new Integer(10);

    // private LayoutContainer container;
    private ContentPanel container;
    private LayoutContainer formButtonContainer;
    private LayoutContainer topContainer;
    private LayoutContainer formContainer;

    private FormPanel topFormPanel;
    private FormPanel leftFormPanel;
    private FormPanel rightFormPanel;
    private FormPanel buttonPanel;
    private ContentPanel gridPanel;

    private Status status;
    private Button searchButton;
    private Button resetButton;

    private RpcProxy<PagingLoadResult<RecordWeb>> proxy;
    private BasePagingLoader<PagingLoadResult<RecordWeb>> pagingLoader;
    private PagingToolBar pagingToolBar;

    private RecordSearchCriteriaWeb searchCriteria;

    private EntityWeb currentEntity;
    private ListStore<EntityWeb> entityStore = new GroupingStore<EntityWeb>();


    private List<EntityAttributeWeb> sortedEntityAttributes;
    private List<EntityAttributeWeb> selectedSearchAttributes;

    private Grid<RecordWeb> grid;
    private ListStore<RecordWeb> store = new ListStore<RecordWeb>();

    private RecordWeb selectedRecord;
    private Dialog recordLinksDialog = null;

    private Grid<RecordWeb> gridLinkList;
    private ListStore<RecordWeb> storeLinkList = new ListStore<RecordWeb>();

    public FixedSearchEntityView(Controller controller) {
        super(controller);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleEvent(AppEvent event) {

        if (event.getType() == AppEvents.EntityFixedSearchView) {

            searchCriteria = null;

            if (Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL) != null) {

                currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
                initUI(currentEntity);

                // Attributes
                sortedEntityAttributes = new ArrayList<EntityAttributeWeb>();
                selectedSearchAttributes = new ArrayList<EntityAttributeWeb>();
                for (EntityAttributeWeb entityAttribute : currentEntity.getAttributes()) {
                    sortedEntityAttributes.add(entityAttribute);
                    if (entityAttribute.getSearchable()) {
                        selectedSearchAttributes.add(entityAttribute);
                    }
                }
                // sort by display order
                Collections.sort(sortedEntityAttributes, ATTRIBUTE_DISPLAY_ORDER);
                Collections.sort(selectedSearchAttributes, ATTRIBUTE_DISPLAY_ORDER);

                if (selectedSearchAttributes.size() > 0) {
                    initSearchFieldUI(selectedSearchAttributes);

                    Map<String, Object> entityMap = Registry.get(Constants.ENTITY_SEARCH_BASIC);
                    if (entityMap != null) {
                        RecordWeb record = (RecordWeb) entityMap.get("entitySearch");
                        displayEntityRecord(attributeFieldMap, record);
                    }
                    searchButton.enable();
                }
            }

        } else if (event.getType() == AppEvents.EntitiesReceived) {

            // Info.display("Information", "EntitiesReceived.");
            List<EntityWeb> entities = (List<EntityWeb>) event.getData();
            entityStore.removeAll();
            entityStore.add(entities);

        } else if (event.getType() == AppEvents.EntityInstancesReceived) {

            List<RecordWeb> entities = (List<RecordWeb>) event.getData();

            /*
             * Info.display("Information", "EntityInstancesReceived. "+entities.size()); for( RecordWeb record :
             * entities) {
             *
             * Info.display("Information", "record id: "+record.getRecordId());
             *
             * Map<String, Object> map = record.getProperties(); for (Object key : map.keySet()) {
             * Info.display("Information", "attribute: " +key.toString() +"; "+map.get(key)); } }
             */

            store.removeAll();
            store.add(entities);

            status.hide();
            searchButton.unmask();

        } else if (event.getType() == AppEvents.EntityLinksReceived) {
            List<RecordWeb> entityLinks = (List<RecordWeb>) event.getData();
            // Info.display("Information", "EnttyLinks: "+entityLinks.size());

            buildRecordLinksDialog();

            List<RecordWeb> dtos = new java.util.ArrayList<RecordWeb>();
            for (RecordWeb linkedRecord : entityLinks) {
                dtos.add(linkedRecord);
            }
            storeLinkList.removeAll();
            storeLinkList.add(dtos);

            // Dialog view to show record links
            recordLinksDialog.show();

        } else if (event.getType() == AppEvents.EntityBasicUpdateFinished) {

            initUI(currentEntity);

            initSearchFieldUI(selectedSearchAttributes);

            // Search Attribute display
            Map<String, Object> entityMap = Registry.get(Constants.ENTITY_SEARCH_BASIC);
            if (entityMap != null) {
                RecordWeb record = (RecordWeb) entityMap.get("entitySearch");
                displayEntityRecord(attributeFieldMap, record);
                searchButton.enable();
            }

            PagingLoadConfig config = new BasePagingLoadConfig();
            config.setOffset(searchCriteria.getFirstResult());
            config.setLimit(PAGE_SIZE);

            pagingLoader.load(config);

        } else if (event.getType() == AppEvents.EntityBasicDeleteFinished) {

            initUI(currentEntity);

            initSearchFieldUI(selectedSearchAttributes);

            // Search Attribute display
            Map<String, Object> entityMap = Registry.get(Constants.ENTITY_SEARCH_BASIC);
            if (entityMap != null) {
                RecordWeb record = (RecordWeb) entityMap.get("entitySearch");
                displayEntityRecord(attributeFieldMap, record);
                searchButton.enable();
            }

            PagingLoadConfig config = new BasePagingLoadConfig();
            config.setOffset(searchCriteria.getFirstResult());
            config.setLimit(PAGE_SIZE);

            // reduced the total count by 1
            searchCriteria.setTotalCount(searchCriteria.getTotalCount() - 1);
            pagingLoader.load(config);

        } else if (event.getType() == AppEvents.EntityBasicUpdateCancel || event.getType() == AppEvents.EntityBasicDeleteCancel) {

            // Model
            initUI(currentEntity);

            initSearchFieldUI(selectedSearchAttributes);

            // Search Attribute display
            Map<String, Object> entityMap = Registry.get(Constants.ENTITY_SEARCH_BASIC);
            if (entityMap != null) {
                RecordWeb record = (RecordWeb) entityMap.get("entitySearch");
                displayEntityRecord(attributeFieldMap, record);
                searchButton.enable();
            }

            // reload
            PagingLoadConfig config = new BasePagingLoadConfig();
            config.setOffset(searchCriteria.getFirstResult());
            config.setLimit(PAGE_SIZE);

            pagingLoader.load(config);

        } else if (event.getType() == AppEvents.EntityBasicLinkPairReceived) {
            unlinkedPair = (RecordLinkWeb) event.getData();
            // Info.display("Information", "Link Pair: "+ linkedPair.getLeftRecord().getRecordId() +", "+linkedPair.getRightRecord().getRecordId());

            buildRecordLinkPairDialog(Constants.BASIC_SEARCH, currentEntity);
            linkPairDialog.setHeading("Unlink Record Pair");
            Button ok = linkPairDialog.getButtonById("ok");
            ok.setIcon(IconHelper.create("images/link_break.png"));
            ok.setText("Confirm Unlink");
            linkPairDialog.show();


            leftIdentifierStore.removeAll();
            rightIdentifierStore.removeAll();
            linkPairStore.removeAll();

            displayLeftEntityIdentifier(unlinkedPair.getLeftRecord());
            displayRightEntityIdentifier(unlinkedPair.getRightRecord());

            for (String key : recordFieldMap.keySet()) {
                BaseModelData  model = new BaseModelData();
                // model.set("attribute", key);
                model.set("attribute", recordFieldMap.get(key));

                EntityAttributeWeb attribute = currentEntity.findEntityAttributeByName(key);
                AttributeDatatype type = AttributeDatatype.getById(attribute.getDatatype().getDataTypeCd());

                if (type == AttributeDatatype.DATE) {
                    String date = Utility.DateToString((Date) unlinkedPair.getLeftRecord().get(key));
                    model.set("leftRecord",  date);
                    date = Utility.DateToString((Date) unlinkedPair.getRightRecord().get(key));
                    model.set("rightRecord",  date);
                } else {
                    model.set("leftRecord", unlinkedPair.getLeftRecord().get(key));
                    model.set("rightRecord", unlinkedPair.getRightRecord().get(key));
                }
                linkPairStore.add(model);
           }
        } else if (event.getType() == AppEvents.ProcessPairUnlinkedView) {
            storeLinkList.remove(selectedUnlinkedRecord);

            if (linkPairDialog != null && linkPairDialog.isVisible()) {
                linkPairDialog.close();
            }

            Info.display("Confirm :", " Successfully unlinked");
        } else if (event.getType() == AppEvents.Error) {

            if (linkPairDialog != null && linkPairDialog.isVisible()) {
                linkPairDialog.close();
            }
            String message = event.getData();
            MessageBox.alert("Information", "Failure: " + message, listenFailureMsg);
        }
    }

    final Listener<MessageBoxEvent> listenFailureMsg = new Listener<MessageBoxEvent>()
    {
        public void handleEvent(MessageBoxEvent ce) {
            Button btn = ce.getButtonClicked();
            if (btn.getText().equals("OK")) {
            }
        }
    };

    private PagingToolBar setupRpcProxy() {
        // Rpc Proxy
        proxy = new RpcProxy<PagingLoadResult<RecordWeb>>()
        {

            @Override
            public void load(final Object loadConfig, final AsyncCallback<PagingLoadResult<RecordWeb>> callback) {

                if (searchCriteria == null) {
                    callback.onSuccess(new BasePagingLoadResult<RecordWeb>(null, 0, 0));
                    return;
                }
                // set page offset for searchCriteria
                searchCriteria.setFirstResult(((PagingLoadConfig) loadConfig).getOffset());

                String searchMode = searchCriteria.getSearchMode();
                if (searchMode.equals("Demographic")) {
                    getController().getEntityInstanceDataService().getEntityRecordsBySearch(searchCriteria,
                            new AsyncCallback<RecordListWeb>()
                            {
                                public void onFailure(Throwable caught) {

                                    if (caught instanceof AuthenticationException) {
                                        Dispatcher.get().dispatch(AppEvents.Logout);
                                        return;
                                    }
                                    searchCriteria.setTotalCount(new Long(0));
                                    Dispatcher.forwardEvent(AppEvents.Error, caught);
                                }

                                public void onSuccess(RecordListWeb result) {
                                    // Info.display("Information", "The records: "
                                    // +((PagingLoadConfig)loadConfig).getOffset());

                                    // PagingLoadConfig configuration
                                    searchCriteria.setTotalCount(result.getTotalCount());
                                    callback.onSuccess(new BasePagingLoadResult<RecordWeb>(result.getRecords(),
                                            ((PagingLoadConfig) loadConfig).getOffset(), result.getTotalCount()
                                                    .intValue()));
                                }
                            });
                }
            }
        };

        // Page loader
        pagingLoader = new BasePagingLoader<PagingLoadResult<RecordWeb>>(proxy);
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

        store = new ListStore<RecordWeb>(pagingLoader);

        PagingToolBar pagingToolBar = new PagingToolBar(PAGE_SIZE);
        pagingToolBar.bind(pagingLoader);
        return pagingToolBar;
    }

    @Override
    protected void setStTextFieldValidation(TextField<?> fieldString, EntityAttributeWeb attribute) {
        for (EntityAttributeValidationWeb validation : attribute.getEntityAttributeValidations()) {

            if (validation.getValidationName().equals("nullityValidationRule")) {
                fieldString.setAllowBlank(false);
            } else {
                for (EntityAttributeValidationParameterWeb validationParameter : validation.getValidationParameters()) {

                    if (validationParameter.getParameterName().equals("valueSet")) {
                        String valueList = validationParameter.getParameterValue();
                        String[] values = valueList.split(",");

                        ListStore<ModelPropertyWeb> modelStore = new ListStore<ModelPropertyWeb>();
                        for (String name : values) {
                            modelStore.add(new ModelPropertyWeb(name));
                        }
                        ((ComboBox) fieldString).setStore(modelStore);
                    }

                }
            }
        }
    }

    private FormPanel setupButtonPanel(int tabIndex) {
        FormPanel buttonPanel = new FormPanel();
        buttonPanel.setHeaderVisible(false);
        buttonPanel.setBodyBorder(false);
        buttonPanel.setSize(1000, 20);
        buttonPanel.setStyleAttribute("paddingRight", "10px");
        buttonPanel.setButtonAlign(HorizontalAlignment.CENTER);

        searchButton = new Button("Search", IconHelper.create("images/search_icon_16x16.png"),
                new SelectionListener<ButtonEvent>()
                {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        // Info.display("test:", "Search componentSelected");
                        Map<String, Object> entityMap = Registry.get(Constants.ENTITY_SEARCH_BASIC);
                        if (entityMap == null) {
                            entityMap = new java.util.HashMap<String, Object>();
                        }

                        if (!leftFormPanel.isValid() || !rightFormPanel.isValid()) {
                            Info.display("Warning: ", "Invalid fields");
                            return;
                        }

                        if (isAllNullValueFromGUI(attributeFieldMap)) {
                            Info.display("Warning: ",
                                        "You must enter a value for at least one search field before pressing the Search button.");
                            return;
                        }

                        searchCriteria = new RecordSearchCriteriaWeb();
                        searchCriteria.setEntityModel(currentEntity);
                        searchCriteria.setFirstResult(new Integer(0));
                        searchCriteria.setMaxResults(PAGE_SIZE);
                        searchCriteria.setTotalCount(new Long(0));
                        searchCriteria.setSearchMode("Demographic");

                        RecordWeb record = getEntityFromGUI(attributeFieldMap);
                        searchCriteria.setRecord(record);

                        entityMap.put("searchMode", "Demographic");
                        entityMap.put("entitySearch", record);

                        Registry.register(Constants.ENTITY_SEARCH_BASIC, entityMap);

                        /*
                         * AppEvent event = new AppEvent(AppEvents.EntityInstancesRequest, record);
                         * event.setData("entityModel", currentEntity); controller.handleEvent(event);
                         */

                        PagingLoadConfig config = new BasePagingLoadConfig();
                        config.setOffset(0);
                        config.setLimit(PAGE_SIZE);

                        pagingLoader.load(config);

                        status.show();
                        searchButton.mask();
                    }
                });

        resetButton = new Button("Reset", new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                clearFormFields(attributeFieldMap);

                status.hide();
                searchButton.unmask();
            }
        });

        searchButton.setTabIndex(tabIndex++);
        resetButton.setTabIndex(tabIndex++);
        searchButton.disable();

        status = new Status();
        status.setBusy("please wait...");
        buttonPanel.getButtonBar().add(status);
        status.hide();

        buttonPanel.getButtonBar().setSpacing(15);
        buttonPanel.addButton(searchButton);
        buttonPanel.addButton(resetButton);

        return buttonPanel;
    }

    private String getTemplate() {
        return "<p class=\"identifierBlock\">" +
                "<table class=\"identifierTable\">" +
                "<tr>" +
                    "<th class=\"identifierColumn\">Identifier</th>" +
                    "<th class=\"identifierDomainNameColumn\">Domain Name</th>" +
                    "<th class=\"namespaceColumn\">Namespace Identifier</th>" +
                    "<th class=\"universalIdentifierColumn\">Universal Identifier</th>" +
                    "<th class=\"universalIdentifierTypeColumn\" >Universal Identifier Type</th>" +
                "</tr>" +
                "<tpl for=\"identifiers\">" +
                    "<tr>" +
                    "<td>{identifier}</td><td>{identifierDomainName}</td><td>{namespaceIdentifier}</td><td>{universalIdentifier}</td><td>{universalIdentifierTypeCode}</td>" +
                    "</tr>" +
                "</tpl>" +
                "</table>"+
                "</p>";
    }

    private Grid<RecordWeb> setupGrid(EntityWeb entity, List<EntityAttributeWeb> sortedEntityAttributes) {

        // setup column configuration
        List<ColumnConfig> columnConfig = new ArrayList<ColumnConfig>();
        XTemplate tpl = XTemplate.create(getTemplate());
        GWT.log("Maximum depth is " + tpl.getMaxDepth(), null);
        RowExpander expander = new RowExpander();
        expander.setTemplate(tpl);

        columnConfig.add(expander);

        for (EntityAttributeWeb entityAttribute : sortedEntityAttributes) {
            // Info.display("Attribute: ", entityAttribute.getDisplayName());

            AttributeDatatype type = AttributeDatatype.getById(entityAttribute.getDatatype().getDataTypeCd());
            if (type == AttributeDatatype.BOOLEAN) {
                CheckColumnConfig column = new CheckColumnConfig(entityAttribute.getName(),
                        entityAttribute.getDisplayName(), 60);
                CellEditor checkBoxEditor = new CellEditor(new CheckBox());
                column.setEditor(checkBoxEditor);
                columnConfig.add(column);
                continue;
            }

            ColumnConfig column = new ColumnConfig(entityAttribute.getName(), entityAttribute.getDisplayName(), 120);
            if (type == AttributeDatatype.DATE) {
                column.setDateTimeFormat(DateTimeFormat.getShortDateFormat());
            }
            if (type == AttributeDatatype.TIMESTAMP) {
                column.setDateTimeFormat(DateTimeFormat.getShortDateTimeFormat());
            }
            columnConfig.add(column);
        }

        ColumnModel cm = new ColumnModel(columnConfig);
        Grid<RecordWeb> gridList = new Grid<RecordWeb>(store, cm);
        gridList.setBorders(true);
        gridList.setAutoWidth(true);
        gridList.setStripeRows(true);
        gridList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        gridList.setHeight(300);

        gridList.addPlugin(expander);


        Menu linkedMenu = new Menu();
        MenuItem menuItemlinks = new MenuItem("Show Linked Records", IconHelper.create("images/link.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {

                        selectedRecord = grid.getSelectionModel().getSelectedItem();
                        if (selectedRecord == null) {
                            Info.display("Information",
                                    "You must first select a field before pressing the \"Show Linked Records\" button.");
                            return;
                        }

                        // Get Linked Records
                        AppEvent event = new AppEvent(AppEvents.EntityLinksForFixedSearchRequest, selectedRecord);
                        event.setData("entityModel", currentEntity);
                        controller.handleEvent(event);
                    }
                });

        linkedMenu.add(menuItemlinks);
        gridList.setContextMenu(linkedMenu);
        return gridList;
    }

    private Grid<RecordWeb> setupGridLinkList(EntityWeb entity, List<EntityAttributeWeb> sortedEntityAttributes) {

        // setup column configuration
        List<ColumnConfig> columnConfig = new ArrayList<ColumnConfig>();
        for (EntityAttributeWeb entityAttribute : sortedEntityAttributes) {
            // Info.display("Attribute: ", entityAttribute.getDisplayName());

            AttributeDatatype type = AttributeDatatype.getById(entityAttribute.getDatatype().getDataTypeCd());
            if (type == AttributeDatatype.BOOLEAN) {
                CheckColumnConfig column = new CheckColumnConfig(entityAttribute.getName(),
                        entityAttribute.getDisplayName(), 60);
                CellEditor checkBoxEditor = new CellEditor(new CheckBox());
                column.setEditor(checkBoxEditor);
                columnConfig.add(column);
                continue;
            }

            ColumnConfig column = new ColumnConfig(entityAttribute.getName(), entityAttribute.getDisplayName(), 120);
            if (type == AttributeDatatype.DATE) {
                column.setDateTimeFormat(DateTimeFormat.getShortDateFormat());
            }
            if (type == AttributeDatatype.TIMESTAMP) {
                column.setDateTimeFormat(DateTimeFormat.getShortDateTimeFormat());
            }
            columnConfig.add(column);
        }

        ColumnModel cm = new ColumnModel(columnConfig);
        Grid<RecordWeb> gridLinkList = new Grid<RecordWeb>(storeLinkList, cm);
        gridLinkList.setBorders(true);
        gridLinkList.setAutoWidth(true);
        gridLinkList.setStripeRows(true);
        gridLinkList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        gridLinkList.setHeight(150);

        return gridLinkList;
    }

    private void initSearchFieldUI(List<EntityAttributeWeb> attributes) {
        GWT.log("Initializing the Search Field UI ", null);

        attributeFieldMap.clear();
        leftFormPanel.removeAll();
        rightFormPanel.removeAll();

        // Selected attribute fields
        boolean leftForm = true;
        for (EntityAttributeWeb entityAttribute : attributes) {

            // create field without validation
            Field<?> field = createField(entityAttribute, false, false);
            if (field != null) {
                attributeFieldMap.put(entityAttribute.getName(), field);

                if (leftForm) {
                    leftFormPanel.add(field);
                    leftForm = false;
                } else {
                    rightFormPanel.add(field);
                    leftForm = true;
                }
            }
        }

        LayoutContainer wrapper = (LayoutContainer) Registry.get(Constants.CENTER_PANEL);
        wrapper.removeAll();
        wrapper.add(container);
        wrapper.layout();
    }

    @SuppressWarnings("unchecked")
    private void initUI(EntityWeb entity) {
        long time = new java.util.Date().getTime();
        GWT.log("Initializing the UI ", null);

        if (entity == null) {
            return;
        }

        // Remove
        // container.remove(formButtonContainer);
        container = new ContentPanel();
        container.setLayout(new BorderLayout());
        container.setHeading("Fixed Entity Search");

        // Rpc Proxy setup
        pagingToolBar = setupRpcProxy();

        // Entity
        formButtonContainer = new LayoutContainer();
        formButtonContainer.setScrollMode(Scroll.AUTO);

        FormLayout toplayout = new FormLayout();
        topContainer = new LayoutContainer();
        topContainer.setLayout(toplayout);
        // topFormPanel = setupForm("", 150, 800);
        FormLayout topFormLayout = new FormLayout();
        topFormLayout.setLabelWidth(155);
        topFormLayout.setDefaultWidth(820);
        topFormPanel = new FormPanel();
        topFormPanel.setHeaderVisible(false);
        topFormPanel.setBodyBorder(false);
        topFormPanel.setLayout(topFormLayout);

        TableLayout formlayout = new TableLayout(2);
        formlayout.setWidth("1000"); // 100%
        formlayout.setCellSpacing(5);
        formlayout.setCellVerticalAlign(VerticalAlignment.TOP);
        formContainer = new LayoutContainer();
        formContainer.setLayout(formlayout);
        leftFormPanel = setupForm("", 150, 400);
        rightFormPanel = setupForm("", 150, 400);

        gridPanel = new ContentPanel();
        gridPanel.setHeaderVisible(false);
        gridPanel.setLayout(new FillLayout());
        gridPanel.setSize(1000, 320);
        ToolBar toolBar = new ToolBar();
        toolBar.add(new Button(" Update Entity ", IconHelper.create("images/entity_edit.png"),
                new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        RecordWeb editEntity = grid.getSelectionModel().getSelectedItem();
                        if (editEntity == null) {
                            Info.display("Information",
                                    "You must first select a field to be edited before pressing the \"Update Entity\" button.");
                            return;
                        }

                        selectedRecord = editEntity;
                        AppEvent event = new AppEvent(AppEvents.EntityBasicUpdateView, editEntity);
                        event.setData("entityModel", currentEntity);
                        controller.handleEvent(event);
                    }
                }));

        toolBar.add(new SeparatorToolItem());
        toolBar.add(new Button(" Remove Entity ", IconHelper.create("images/entity_delete.png"),
                new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        RecordWeb editEntity = grid.getSelectionModel().getSelectedItem();
                        if (editEntity == null) {
                            Info.display("Information",
                                    "You must first select a field to be removed before pressing the \"Remove Entity\" button.");
                            return;
                        }

                        selectedRecord = editEntity;
                        AppEvent event = new AppEvent(AppEvents.EntityBasicDeleteView, editEntity);
                        event.setData("entityModel", currentEntity);
                        controller.handleEvent(event);
                    }
                }));

        if (entity.getAttributes() != null) {

            // Attributes
            List<EntityAttributeWeb> sortedEntityAttributes = new ArrayList<EntityAttributeWeb>(entity.getAttributes().size());
            for (EntityAttributeWeb entityAttribute : entity.getAttributes()) {
                sortedEntityAttributes.add(entityAttribute);
            }
            // sort by display order
            Collections.sort(sortedEntityAttributes, ATTRIBUTE_DISPLAY_ORDER);

            // Grid
            grid = setupGrid(entity, sortedEntityAttributes);

        }

        attributeFieldMap = new HashMap<String, Field<?>>();
        if (entity.getAttributes() != null) {
            SetRecordFieldMap(entity);
        }

        topContainer.add(topFormPanel);

        formContainer.add(leftFormPanel);
        formContainer.add(rightFormPanel);

        buttonPanel = setupButtonPanel(1);

        gridPanel.setTopComponent(toolBar);
        gridPanel.add(grid);
        gridPanel.setBottomComponent(pagingToolBar);

        formButtonContainer.add(topContainer);
        formButtonContainer.add(formContainer);
        formButtonContainer.add(buttonPanel);
        formButtonContainer.add(gridPanel);

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        data.setMargins(new Margins(4, 2, 4, 2));
        container.add(formButtonContainer, data);

        LayoutContainer wrapper = (LayoutContainer) Registry.get(Constants.CENTER_PANEL);
        wrapper.removeAll();
        wrapper.add(container);
        wrapper.layout();

        GWT.log("Done Initializing the UI in " + (new java.util.Date().getTime() - time), null);
    }

    private void buildRecordLinksDialog() {
        if (recordLinksDialog != null) {
            return;
        }

        recordLinksDialog = new Dialog();
        recordLinksDialog.setBodyBorder(false);
        recordLinksDialog.setWidth(810);
        recordLinksDialog.setHeight(250);
        recordLinksDialog.setButtons(Dialog.OK);
        recordLinksDialog.setModal(true);
        recordLinksDialog.getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                recordLinksDialog.close();

            }
        });

        ContentPanel cp = new ContentPanel();
        cp.setHeading("Linked Records");
        cp.setFrame(true);
        cp.setIcon(IconHelper.create("images/link.png"));
        cp.setLayout(new FillLayout());
        cp.setSize(800, 180);

        ToolBar toolBar = new ToolBar();
        toolBar.add(new Button(" Unlink ", IconHelper.create("images/link_break.png"),
                new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        selectedUnlinkedRecord = gridLinkList.getSelectionModel().getSelectedItem();
                        if (selectedUnlinkedRecord == null) {
                            Info.display("Information",
                                    "You must first select a field to be unlinked before pressing the \"Unlink\" button.");
                            return;
                        }

                        AppEvent event = new AppEvent(AppEvents.EntityBasicLinkedPair, selectedUnlinkedRecord);
                        event.setData("entityModel", currentEntity);
                        event.setData("linkedRecord", selectedRecord);
                        controller.handleEvent(event);
                    }
                }));
        cp.setTopComponent(toolBar);


        // Grid
        gridLinkList = setupGridLinkList(currentEntity, sortedEntityAttributes);
        cp.add(gridLinkList);

        recordLinksDialog.add(cp);
    }
}
