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
import org.openempi.webapp.client.model.IdentifierDomainWeb;
import org.openempi.webapp.client.model.IdentifierWeb;
import org.openempi.webapp.client.model.ModelPropertyWeb;
import org.openempi.webapp.client.model.RecordListWeb;
import org.openempi.webapp.client.model.RecordSearchCriteriaWeb;
import org.openempi.webapp.client.model.RecordWeb;
import org.openempi.webapp.client.mvc.BaseEntityView;
import org.openempi.webapp.client.mvc.Controller;
import org.openempi.webapp.client.ui.util.AttributeDatatype;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
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
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
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
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
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

public class SearchEntityView extends BaseEntityView
{
    public static final Integer PAGE_SIZE = new Integer(10);

    // private LayoutContainer container;
    private ContentPanel container;
    private LayoutContainer formButtonContainer;
    private LayoutContainer searchSelectionContainer;
    private LayoutContainer topContainer;
    private LayoutContainer formContainer;

    private FormPanel searchSelectionPanel;
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

    private TextField<String> identifier;
    private ComboBox<IdentifierDomainWeb> identifierDomains;

    private String searchMode;
    private Radio basicRadio;
    private Radio advancedRadio;
    private List<EntityAttributeWeb> sortedEntityAttributes;
    private TextField<String> selectedSearchAttributeNames;
    private List<EntityAttributeWeb> selectedSearchAttributes;
    private ListView<EntityAttributeWeb> searchAttributes;
    private ListStore<EntityAttributeWeb> searchAttributeStore = new ListStore<EntityAttributeWeb>();

    private Grid<RecordWeb> grid;
    private ListStore<RecordWeb> store = new ListStore<RecordWeb>();

    private RecordWeb selectedRecord;
    private Dialog recordLinksDialog = null;

    private Grid<RecordWeb> gridLinkList;
    private ListStore<RecordWeb> storeLinkList = new ListStore<RecordWeb>();

    public SearchEntityView(Controller controller) {
        super(controller);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleEvent(AppEvent event) {

        if (event.getType() == AppEvents.EntitySearchView) {

            searchCriteria = null;

            if (Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL) != null) {

                currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
                initUI(currentEntity);

                // Attributes
                sortedEntityAttributes = new ArrayList<EntityAttributeWeb>(currentEntity.getAttributes().size());
                for (EntityAttributeWeb entityAttribute : currentEntity.getAttributes()) {
                    sortedEntityAttributes.add(entityAttribute);
                }
                // sort by display order
                Collections.sort(sortedEntityAttributes, ATTRIBUTE_DISPLAY_ORDER);

                Map<String, Object> entityMap = Registry.get(Constants.ENTITY_SEARCH_ADVANCED);
                if (entityMap != null) {
                    String searchMode = (String) entityMap.get("searchMode");
                    if (searchMode.equals("Identifier")) {
                        basicRadio.setValue(true);
                        initSearchIdentifiersUI();

                        IdentifierWeb identifierValue = (IdentifierWeb) entityMap.get("identifier");
                        if (identifierValue != null) {
                            displayIdentifier(identifierValue);
                            searchButton.enable();
                        }

                    } else {
                        advancedRadio.setValue(true);
                        initSearchAttributeUI(sortedEntityAttributes);

                        // Selected Attributes
                        List<EntityAttributeWeb> attributes = (List<EntityAttributeWeb>) entityMap.get("entitySearchArttributes");

                        if (attributes != null && attributes.size() > 0) {
                            initSearchFieldUI(attributes);
                            displaySelectedSearchAttributes(attributes);

                            // Search Attribute display
                            RecordWeb record = (RecordWeb) entityMap.get("entitySearch");
                            displayEntityRecord(attributeFieldMap, record);
                            searchButton.enable();
                        }
                    }
                } else {
                    basicRadio.setValue(true);
                    initSearchIdentifiersUI();
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

        } else if (event.getType() == AppEvents.EntityUpdateFinished) {

            initUI(currentEntity);

            Map<String, Object> entityMap = Registry.get(Constants.ENTITY_SEARCH_ADVANCED);
            String searchMode = (String) entityMap.get("searchMode");
            if (searchMode.equals("Identifier")) {
                basicRadio.setValue(true);
                initSearchIdentifiersUI();

                IdentifierWeb identifier = (IdentifierWeb) entityMap.get("identifier");
                if (identifier != null) {
                    displayIdentifier(identifier);
                    searchButton.enable();
                }
            } else {
                advancedRadio.setValue(true);
                initSearchAttributeUI(sortedEntityAttributes);

                // Selected Attributes
                List<EntityAttributeWeb> attributes = (List<EntityAttributeWeb>) entityMap
                        .get("entitySearchArttributes");
                initSearchFieldUI(attributes);

                displaySelectedSearchAttributes(attributes);

                // Search Attribute display
                RecordWeb record = (RecordWeb) entityMap.get("entitySearch");
                displayEntityRecord(attributeFieldMap, record);
            }

            PagingLoadConfig config = new BasePagingLoadConfig();
            config.setOffset(searchCriteria.getFirstResult());
            config.setLimit(PAGE_SIZE);

            pagingLoader.load(config);

        } else if (event.getType() == AppEvents.EntityDeleteFinished) {

            initUI(currentEntity);

            Map<String, Object> entityMap = Registry.get(Constants.ENTITY_SEARCH_ADVANCED);
            String searchMode = (String) entityMap.get("searchMode");
            if (searchMode.equals("Identifier")) {
                basicRadio.setValue(true);
                initSearchIdentifiersUI();

                IdentifierWeb identifier = (IdentifierWeb) entityMap.get("identifier");
                if (identifier != null) {
                    displayIdentifier(identifier);
                    searchButton.enable();
                }
            } else {
                advancedRadio.setValue(true);
                initSearchAttributeUI(sortedEntityAttributes);

                // Selected Attributes
                List<EntityAttributeWeb> attributes = (List<EntityAttributeWeb>) entityMap
                        .get("entitySearchArttributes");
                initSearchFieldUI(attributes);

                displaySelectedSearchAttributes(attributes);

                // Search Attribute display
                RecordWeb record = (RecordWeb) entityMap.get("entitySearch");
                displayEntityRecord(attributeFieldMap, record);
            }

            PagingLoadConfig config = new BasePagingLoadConfig();
            config.setOffset(searchCriteria.getFirstResult());
            config.setLimit(PAGE_SIZE);

            // reduced the total count by 1
            searchCriteria.setTotalCount(searchCriteria.getTotalCount() - 1);
            pagingLoader.load(config);

        } else if (event.getType() == AppEvents.EntityUpdateCancel || event.getType() == AppEvents.EntityDeleteCancel) {

            // Model
            initUI(currentEntity);

            Map<String, Object> entityMap = Registry.get(Constants.ENTITY_SEARCH_ADVANCED);
            String searchMode = (String) entityMap.get("searchMode");
            if (searchMode.equals("Identifier")) {
                basicRadio.setValue(true);
                initSearchIdentifiersUI();

                IdentifierWeb identifier = (IdentifierWeb) entityMap.get("identifier");
                if (identifier != null) {
                    displayIdentifier(identifier);
                }

            } else {
                advancedRadio.setValue(true);
                initSearchAttributeUI(sortedEntityAttributes);

                // Selected Attributes
                List<EntityAttributeWeb> attributes = (List<EntityAttributeWeb>) entityMap
                        .get("entitySearchArttributes");
                initSearchFieldUI(attributes);

                displaySelectedSearchAttributes(attributes);

                // Search Attribute display
                RecordWeb record = (RecordWeb) entityMap.get("entitySearch");
                displayEntityRecord(attributeFieldMap, record);
            }

            // reload
            PagingLoadConfig config = new BasePagingLoadConfig();
            config.setOffset(searchCriteria.getFirstResult());
            config.setLimit(PAGE_SIZE);

            pagingLoader.load(config);

        } else if (event.getType() == AppEvents.Error) {
            String message = event.getData();
            MessageBox.alert("Information", "Failure: " + message, listenFailureMsg);
        }
    }

    protected void clearIdentifierFields() {
        identifier.clear();
        identifierDomains.clear();
        // listIdentifierTypes.clear();
    }

    protected void displayIdentifier(IdentifierWeb identifierWeb) {
        identifier.setValue(identifierWeb.getIdentifier());
        identifierDomains.setValue(identifierWeb.getIdentifierDomain());
    }

    private void displaySelectedSearchAttributes(List<EntityAttributeWeb> attributes) {
        // Text
        String attributeNames = "";
        for (EntityAttributeWeb attribute : attributes) {
            if (attributeNames.isEmpty()) {
                attributeNames = attribute.getDisplayName();
            } else {
                attributeNames = attributeNames + ", " + attribute.getDisplayName();
            }
        }

        selectedSearchAttributeNames.setValue(attributeNames);

        // ListView
        searchAttributes.getSelectionModel().select(attributes, true);
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
                } else {

                    getController().getEntityInstanceDataService().findEntitiesByIdentifier(searchCriteria,
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

        searchButton = new Button("Search Entity", IconHelper.create("images/search_icon_16x16.png"),
                new SelectionListener<ButtonEvent>()
                {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        // Info.display("test:", "Search componentSelected");

                        searchCriteria = new RecordSearchCriteriaWeb();
                        searchCriteria.setEntityModel(currentEntity);
                        searchCriteria.setFirstResult(new Integer(0));
                        searchCriteria.setMaxResults(PAGE_SIZE);
                        searchCriteria.setTotalCount(new Long(0));

                        Map<String, Object> entityMap = Registry.get(Constants.ENTITY_SEARCH_ADVANCED);
                        if (entityMap == null) {
                            entityMap = new java.util.HashMap<String, Object>();
                        }

                        if (searchMode.equals("Identifier")) {
                            searchCriteria.setSearchMode("Identifier");

                            String id = identifier.getValue();
                            if (id == null) {
                                Info.display("Warning",
                                        "You must enter at least a partial identifier before pressing the Search Entity button.");
                                return;
                            }
                            if (id.trim().equals("%")) {
                                Info.display("Warning", "You must provide at least a partial identifier in a search.");
                                return;
                            }

                            IdentifierWeb identifier = new IdentifierWeb();
                            identifier.setIdentifier(id);

                            IdentifierDomainWeb domain = null;
                            if (identifierDomains.getSelection().size() > 0) {
                                domain = identifierDomains.getSelection().get(0);

                                identifier.setIdentifierDomain(domain);
                            }
                            searchCriteria.setIdentifier(identifier);

                            entityMap.put("searchMode", "Identifier");
                            entityMap.put("identifier", identifier);

                            Registry.register(Constants.ENTITY_SEARCH_ADVANCED, entityMap);

                        } else {
                            searchCriteria.setSearchMode("Demographic");

                            if (!leftFormPanel.isValid() || !rightFormPanel.isValid()) {
                                Info.display("Warning: ", "Invalid fields");
                                return;
                            }

                            if (isAllNullValueFromGUI(attributeFieldMap)) {
                                Info.display("Warning: ",
                                        "You must enter at least a search field before pressing the Search Entity button.");
                                return;
                            }

                            RecordWeb record = getEntityFromGUI(attributeFieldMap);
                            searchCriteria.setRecord(record);

                            entityMap.put("searchMode", "Demographic");
                            entityMap.put("entitySearchArttributes", selectedSearchAttributes);
                            entityMap.put("entitySearch", record);

                            Registry.register(Constants.ENTITY_SEARCH_ADVANCED, entityMap);

                            /*
                             * AppEvent event = new AppEvent(AppEvents.EntityInstancesRequest, record);
                             * event.setData("entityModel", currentEntity); controller.handleEvent(event);
                             */
                        }

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
                clearIdentifierFields();
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
                        AppEvent event = new AppEvent(AppEvents.EntityLinksRequest, selectedRecord);
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

    private void initSearchIdentifiersUI() {
        GWT.log("Initializing the Search Identifiers UI ", null);

        leftFormPanel.removeAll();
        rightFormPanel.removeAll();

        // Identifier fields
        identifier = new TextField<String>();
        identifier.setFieldLabel("Identifier");

        KeyListener keyListener = new KeyListener()
        {
            public void componentKeyUp(ComponentEvent event) {
                String value = identifier.getValue();
                if (value != null && value.length() > 0 && !value.trim().equals("%")) {
                    searchButton.enable();
                } else {
                    searchButton.disable();
                }

            }

        };
        identifier.addKeyListener(keyListener);
        /*
         * identifier.addListener(Events.Change, new Listener<FieldEvent>() { public void handleEvent(FieldEvent fe) {
         * TextField t = (TextField) fe.getField(); String value = (String)t.getValue();
         *
         * if(value == null || value.trim().equals("%") ) { searchButton.disable(); } else { searchButton.enable(); } }
         * });
         */

        ListStore<IdentifierDomainWeb> domains = new ListStore<IdentifierDomainWeb>();
        List<IdentifierDomainWeb> domainEntries = Registry.get(Constants.IDENTITY_DOMAINS);
        domains.add(domainEntries);
        identifierDomains = new ComboBox<IdentifierDomainWeb>();
        identifierDomains.setFieldLabel("Identifier Domain");
        identifierDomains.setEmptyText("Select a identifier domain name...");
        identifierDomains.setDisplayField("identifierDomainName");
        identifierDomains.setValueField("identifierDomainName");
        identifierDomains.setTypeAhead(true);
        identifierDomains.setTriggerAction(TriggerAction.ALL);
        identifierDomains.setStore(domains);

        leftFormPanel.add(identifier);
        rightFormPanel.add(identifierDomains);

        LayoutContainer wrapper = (LayoutContainer) Registry.get(Constants.CENTER_PANEL);
        wrapper.removeAll();
        wrapper.add(container);
        wrapper.layout();
    }

    private void initSearchAttributeUI(List<EntityAttributeWeb> sortedEntityAttributes) {
        GWT.log("Initializing the Search Attribute UI ", null);

        topFormPanel.removeAll();

        // Text Field
        selectedSearchAttributeNames = new TextField<String>();
        selectedSearchAttributeNames.setFieldLabel("Selected Search Attributes");
        selectedSearchAttributeNames.setReadOnly(true);

        // List Box
        searchAttributeStore.removeAll();
        searchAttributeStore.add(sortedEntityAttributes);

        searchAttributes = new ListView<EntityAttributeWeb>();
        searchAttributes.setDisplayProperty("displayName");
        searchAttributes.setWidth(220);
        searchAttributes.setHeight(110);
        searchAttributes.setStore(searchAttributeStore);

        searchAttributes.getSelectionModel().addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent<EntityAttributeWeb>>()
                {
                    public void handleEvent(SelectionChangedEvent<EntityAttributeWeb> be) {
                        // List<EntityAttributeWeb> selections = be.getSelection();
                        selectedSearchAttributes = be.getSelection();
                        String attributes = "";
                        for (EntityAttributeWeb attribute : selectedSearchAttributes) {
                            if (attributes.isEmpty()) {
                                attributes = attribute.getDisplayName();
                            } else {
                                attributes = attributes + ", " + attribute.getDisplayName();
                            }
                        }
                        selectedSearchAttributeNames.setValue(attributes);
                        initSearchFieldUI(selectedSearchAttributes);

                        if (attributes.isEmpty()) {
                            searchButton.disable();
                        } else {
                            searchButton.enable();
                        }
                    }
                });

        HBoxLayoutData dataSelectedTypes = new HBoxLayoutData(new Margins(5, 0, 0, 0));
        topFormPanel.add(selectedSearchAttributeNames, dataSelectedTypes);
        HBoxLayoutData dataTypes = new HBoxLayoutData(new Margins(5, 0, 5, 160));
        topFormPanel.add(searchAttributes, dataTypes);

        LayoutContainer wrapper = (LayoutContainer) Registry.get(Constants.CENTER_PANEL);
        wrapper.removeAll();
        wrapper.add(container);
        wrapper.layout();
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
        container.setHeading("Entity Search");

        // Rpc Proxy setup
        pagingToolBar = setupRpcProxy();

        // Entity
        formButtonContainer = new LayoutContainer();
        formButtonContainer.setScrollMode(Scroll.AUTO);

        searchSelectionContainer = new LayoutContainer();
        searchSelectionContainer.setLayout(new FormLayout());
        FormLayout searchSelectionLayout = new FormLayout();
        searchSelectionLayout.setLabelWidth(155);
        searchSelectionLayout.setDefaultWidth(820);
        searchSelectionPanel = new FormPanel();
        searchSelectionPanel.setHeaderVisible(false);
        searchSelectionPanel.setBodyBorder(false);
        searchSelectionPanel.setLayout(searchSelectionLayout);

        searchMode = "Identifier";

        basicRadio = new Radio();
        basicRadio.setName("Identifier");
        basicRadio.setBoxLabel("Identifier");
        basicRadio.setValue(true);

        advancedRadio = new Radio();
        advancedRadio.setName("Demographic");
        advancedRadio.setBoxLabel("Demographic");

        final RadioGroup radioGroup = new RadioGroup();
        radioGroup.setOrientation(Orientation.HORIZONTAL);
        radioGroup.setFieldLabel("Entity Search");
        radioGroup.add(basicRadio);
        radioGroup.add(advancedRadio);
        radioGroup.addListener(Events.Change, new Listener<BaseEvent>()
        {
            public void handleEvent(BaseEvent be) {
                // Info.display("Information: ", ""+radioGroup.getValue().getBoxLabel());

                searchButton.disable();

                store.removeAll();

                topFormPanel.removeAll();
                leftFormPanel.removeAll();
                rightFormPanel.removeAll();

                Map<String, Object> entityMap = Registry.get(Constants.ENTITY_SEARCH_ADVANCED);

                String search = radioGroup.getValue().getBoxLabel();
                if (search.equals("Demographic")) {

                    searchMode = "Demographic";
                    List<EntityAttributeWeb> sortedEntityAttributes = new ArrayList<EntityAttributeWeb>(currentEntity
                            .getAttributes().size());
                    for (EntityAttributeWeb entityAttribute : currentEntity.getAttributes()) {
                        sortedEntityAttributes.add(entityAttribute);
                    }
                    // sort by display order
                    Collections.sort(sortedEntityAttributes, ATTRIBUTE_DISPLAY_ORDER);
                    initSearchAttributeUI(sortedEntityAttributes);

                    if (entityMap != null) {
                        List<EntityAttributeWeb> attributes = (List<EntityAttributeWeb>) entityMap.get("entitySearchArttributes");
                        if (attributes != null && attributes.size() > 0) {
                            initSearchFieldUI(attributes);
                            displaySelectedSearchAttributes(attributes);

                            // Search Attribute display
                            RecordWeb record = (RecordWeb) entityMap.get("entitySearch");
                            displayEntityRecord(attributeFieldMap, record);
                            searchButton.enable();
                        } else {
                            initSearchFieldUI(new ArrayList<EntityAttributeWeb>(0));
                        }
                    } else {
                        initSearchFieldUI(new ArrayList<EntityAttributeWeb>(0));
                    }

                } else {
                    searchMode = "Identifier";
                    initSearchIdentifiersUI();

                    if (entityMap != null) {
                        IdentifierWeb identifierValue = (IdentifierWeb) entityMap.get("identifier");
                        if (identifierValue != null) {
                            displayIdentifier(identifierValue);
                            searchButton.enable();
                        }
                    }
                }
            }
        });
        searchSelectionPanel.add(radioGroup);
        searchSelectionContainer.add(searchSelectionPanel);

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
                        AppEvent event = new AppEvent(AppEvents.EntityUpdateView, editEntity);
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
                        AppEvent event = new AppEvent(AppEvents.EntityDeleteView, editEntity);
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

        topContainer.add(topFormPanel);

        formContainer.add(leftFormPanel);
        formContainer.add(rightFormPanel);

        buttonPanel = setupButtonPanel(1);

        gridPanel.setTopComponent(toolBar);
        gridPanel.add(grid);
        gridPanel.setBottomComponent(pagingToolBar);

        formButtonContainer.add(searchSelectionContainer);
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
        recordLinksDialog.setHeight(230);
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
        cp.setSize(800, 160);

        // Grid
        gridLinkList = setupGridLinkList(currentEntity, sortedEntityAttributes);
        cp.add(gridLinkList);

        recordLinksDialog.add(cp);
    }
}
