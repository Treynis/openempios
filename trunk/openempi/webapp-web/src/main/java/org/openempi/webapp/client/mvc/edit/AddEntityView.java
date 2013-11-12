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
package org.openempi.webapp.client.mvc.edit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.model.EntityAttributeGroupWeb;
import org.openempi.webapp.client.model.EntityAttributeWeb;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.IdentifierDomainWeb;
import org.openempi.webapp.client.model.IdentifierWeb;
import org.openempi.webapp.client.model.RecordWeb;
import org.openempi.webapp.client.mvc.Controller;
import org.openempi.webapp.client.mvc.BaseEntityView;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import com.google.gwt.core.client.GWT;

public class AddEntityView extends BaseEntityView
{
    // private LayoutContainer container;
    private ContentPanel container;
    private LayoutContainer formButtonContainer;
    private LayoutContainer identifierContainer;
    private LayoutContainer topContainer;
    private LayoutContainer formContainer;
    private FormPanel topFormPanel;
    private FormPanel leftFormPanel;
    private FormPanel rightFormPanel;
    private FormPanel buttonPanel;

    private TextField<String> identifier;
    private ComboBox<IdentifierDomainWeb> identifierDomains;

    private ListStore<IdentifierWeb> identifierStore = new ListStore<IdentifierWeb>();
    private Grid<IdentifierWeb> identifierGrid;
    private Button addIdentifierButton;
    private Button removeIdentifierButton;

    private Status status;
    private Button checkDuplicateButton;
    private Button submitButton;
    private Button cancelButton;

    private EntityWeb currentEntity;
    private ComboBox<EntityWeb> entityCombo;
    private ListStore<EntityWeb> entityStore = new GroupingStore<EntityWeb>();

    // Map<String, Field<?>> attributeFieldMap;

    public AddEntityView(Controller controller) {
        super(controller);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleEvent(AppEvent event) {

        if (event.getType() == AppEvents.EntityAddView) {

            // initUI();
            if (Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL) != null) {

                identifierStore.removeAll();

                currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
                initEntityUI(currentEntity);
            }

        } else if (event.getType() == AppEvents.EntitiesReceived) {

            // Info.display("Information", "EntitiesReceived.");
            List<EntityWeb> entities = (List<EntityWeb>) event.getData();
            entityStore.removeAll();
            entityStore.add(entities);

        } else if (event.getType() == AppEvents.EntityAddComplete) {

            MessageBox.alert("Information", "Entity was successfully added", listenInfoMsg);

        } else if (event.getType() == AppEvents.Error) {
            String message = event.getData();
            MessageBox.alert("Information", "Failure: " + message, listenFailureMsg);
        }
    }

    final Listener<MessageBoxEvent> listenInfoMsg = new Listener<MessageBoxEvent>()
    {
        public void handleEvent(MessageBoxEvent ce) {
            Button btn = ce.getButtonClicked();
            if (btn.getText().equals("OK")) {

                identifier.clear();
                identifierDomains.clear();
                identifierStore.removeAll();
                clearFormFields(attributeFieldMap);

                status.hide();
                submitButton.unmask();
            }
        }
    };

    final Listener<MessageBoxEvent> listenFailureMsg = new Listener<MessageBoxEvent>()
    {
        public void handleEvent(MessageBoxEvent ce) {
            Button btn = ce.getButtonClicked();
            if (btn.getText().equals("OK")) {
            }
        }
    };

    private FormPanel setupButtonPanel(int tabIndex) {
        FormPanel buttonPanel = new FormPanel();
        buttonPanel.setHeaderVisible(false);
        buttonPanel.setBodyBorder(false);
        buttonPanel.setWidth("960");
        buttonPanel.setStyleAttribute("paddingRight", "10px");
        buttonPanel.setButtonAlign(HorizontalAlignment.CENTER);

        submitButton = new Button("Save", new SelectionListener<ButtonEvent>()
        {

            @Override
            public void componentSelected(ButtonEvent ce) {
                // Info.display("test:", "save componentSelected");

                // Info.display("test:", "save componentSelected");
                if (!topFormPanel.isValid() || !leftFormPanel.isValid() || !rightFormPanel.isValid()) {

                    // Info.display("test:", "Invalid fields");
                    return;
                }

                RecordWeb record = getEntityFromGUI(attributeFieldMap);
                Set<IdentifierWeb> ids = new HashSet<IdentifierWeb>();
                for (int i = 0; i < identifierStore.getCount(); i++) {
                    IdentifierWeb identifier = identifierStore.getAt(i);
                    ids.add(identifier);
                }
                record.setIdentifiers(ids);

                AppEvent event = new AppEvent(AppEvents.EntityAdd, record);
                event.setData("entityModel", currentEntity);
                controller.handleEvent(event);

                status.show();
                submitButton.mask();

            }
        });

        checkDuplicateButton = new Button("Check Duplicate", new SelectionListener<ButtonEvent>()
        {

            @Override
            public void componentSelected(ButtonEvent ce) {
                // Info.display("test:", "Check Duplicate componentSelected");

            }
        });

        cancelButton = new Button("Reset", new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {

                identifier.clear();
                identifierDomains.clear();
                identifierStore.removeAll();
                clearFormFields(attributeFieldMap);

                status.hide();
                submitButton.unmask();
            }
        });

        submitButton.setTabIndex(tabIndex++);
        checkDuplicateButton.setTabIndex(tabIndex++);
        cancelButton.setTabIndex(tabIndex++);

        status = new Status();
        status.setBusy("please wait...");
        buttonPanel.getButtonBar().add(status);
        status.hide();

        buttonPanel.getButtonBar().setSpacing(15);
        buttonPanel.addButton(submitButton);
        buttonPanel.addButton(checkDuplicateButton);
        buttonPanel.addButton(cancelButton);

        return buttonPanel;
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

        identifier = new TextField<String>();
        identifier.setFieldLabel("Identifier");
        identifier.setTabIndex(tabIndex++);

        // Field listeners
        identifier.addListener(Events.Change, new Listener<FieldEvent>()
        {
            public void handleEvent(FieldEvent fe) {
                // Clear invalid mark
                identifier.clearInvalid();
            }
        });

        ListStore<IdentifierDomainWeb> domains = new ListStore<IdentifierDomainWeb>();
        List<IdentifierDomainWeb> domainEntries = Registry.get(Constants.IDENTITY_DOMAINS);
        domains.add(domainEntries);

        identifierDomains = new ComboBox<IdentifierDomainWeb>();
        identifierDomains.setEmptyText("Select a identifier domain...");
        identifierDomains.setForceSelection(true);
        identifierDomains.setFieldLabel("Identifier Domain");
        identifierDomains.setDisplayField("identifierDomainName");
        identifierDomains.setToolTip("Identifier Domain");
        identifierDomains.setStore(domains);
        identifierDomains.setTypeAhead(true);
        identifierDomains.setTriggerAction(TriggerAction.ALL);
        identifierDomains.setTabIndex(tabIndex++);

        // Field listeners
        identifierDomains.addListener(Events.Change, new Listener<FieldEvent>()
        {
            public void handleEvent(FieldEvent fe) {
                // Clear invalid mark
                identifierDomains.clearInvalid();
            }
        });

        ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        cp.setWidth(widthPanel);

        ToolBar toolBar = new ToolBar();
        // Buttons
        addIdentifierButton = new Button(" Add ", IconHelper.create("images/add.png"),
                new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        if (identifier.getValue() != null && identifierDomains.getValue() != null) {

                            // check duplicate identifier
                            for (int i = 0; i < identifierStore.getCount(); i++) {
                                IdentifierWeb identifierInStore = identifierStore.getAt(i);
                                if (identifier.getValue().equals(identifierInStore.getIdentifier())) {
                                    identifier.markInvalid("Duplicate identifier");
                                    return;
                                }

                                // Info.display("identifierDomains:",
                                // ""+identifierDomains.getValue().getIdentifierDomainName());
                                // Info.display("identifierInStore:",
                                // ""+identifierInStore.getIdentifierDomain().getIdentifierDomainName());
                                if (identifierDomains.getValue().getIdentifierDomainId()
                                        .equals(identifierInStore.getIdentifierDomain().getIdentifierDomainId())) {
                                    identifierDomains.markInvalid("Duplicate identifier domain");
                                    return;
                                }
                            }

                            IdentifierWeb entityIdentifier = new IdentifierWeb();
                            entityIdentifier.setIdentifier(identifier.getValue());
                            entityIdentifier.setIdentifierDomain(identifierDomains.getValue());
                            identifierStore.add(entityIdentifier);
                        }

                    }
                });
        removeIdentifierButton = new Button(" Remove ", IconHelper.create("images/delete.png"),
                new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        IdentifierWeb item = identifierGrid.getSelectionModel().getSelectedItem();
                        String removedIdentifier = item.getIdentifier();
                        if (item != null) {
                            identifierStore.remove(item);
                        }

                        // Clear invalid mark
                        if (identifier.getValue().equals(removedIdentifier)) {
                            identifier.clearInvalid();
                        }

                        if (identifierDomains.getValue().getIdentifierDomainId()
                                .equals(item.getIdentifierDomain().getIdentifierDomainId())) {
                            identifierDomains.clearInvalid();
                        }
                    }
                });
        addIdentifierButton.setTabIndex(tabIndex++);
        removeIdentifierButton.setTabIndex(tabIndex++);

        toolBar.add(addIdentifierButton);
        toolBar.add(new SeparatorToolItem());
        toolBar.add(removeIdentifierButton);

        cp.add(toolBar);
        cp.add(setupIdentifierGrid(identifierStore, widthPanel - 45, tabIndex));

        identifierfieldSet.add(identifier);
        identifierfieldSet.add(identifierDomains);
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

    private void initEntityUI(EntityWeb entity) {
        long time = new java.util.Date().getTime();
        GWT.log("Initializing the UI ", null);

        // Remove
        // container.remove(formButtonContainer);
        container = new ContentPanel();
        container.setLayout(new BorderLayout());
        container.setHeading("Add New Entity");

        // Entity
        formButtonContainer = new LayoutContainer();
        formButtonContainer.setScrollMode(Scroll.AUTO);

        TableLayout identlayout = new TableLayout(2);
        identlayout.setWidth("100%");
        identlayout.setCellSpacing(5);
        identlayout.setCellVerticalAlign(VerticalAlignment.TOP);

        FormLayout toplayout = new FormLayout();

        TableLayout formlayout = new TableLayout(2);
        formlayout.setWidth("960"); // "100%"
        formlayout.setCellSpacing(5);
        formlayout.setCellVerticalAlign(VerticalAlignment.TOP);

        identifierContainer = new LayoutContainer();
        ;
        identifierContainer.setLayout(identlayout);
        FormPanel identifierPanel = setupForm("", 150, 850);
        identifierPanel.add(setupIdentifierfieldSet(875, 1));
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

        if (entity == null) {
            return;
        }

        if (entity.getAttributes() != null) {

            // Groups
            List<EntityAttributeGroupWeb> sortedAttributeGroups = null;
            if (entity.getEntityAttributeGroups() != null) {
                sortedAttributeGroups = new ArrayList<EntityAttributeGroupWeb>(entity.getEntityAttributeGroups().size());
                for (EntityAttributeGroupWeb entityGroup : entity.getEntityAttributeGroups()) {
                    // Info.display("Entity Group:", entityGroup.getName()+ "; "+entityGroup.getDisplayOrder());
                    sortedAttributeGroups.add(entityGroup);
                }
                Collections.sort(sortedAttributeGroups, GROUP_DISPLAY_ORDER);
            }

            // Attributes
            List<EntityAttributeWeb> sortedEntityAttributes = new ArrayList<EntityAttributeWeb>(entity.getAttributes()
                    .size());
            if (entity.getAttributes() != null) {
                for (EntityAttributeWeb entityAttribute : entity.getAttributes()) {
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

        topContainer.add(topFormPanel);
        formContainer.add(leftFormPanel);
        formContainer.add(rightFormPanel);
        buttonPanel = setupButtonPanel(33);

        formButtonContainer.add(identifierContainer);
        formButtonContainer.add(topContainer);
        formButtonContainer.add(formContainer);
        formButtonContainer.add(buttonPanel);

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        data.setMargins(new Margins(4, 2, 4, 2));
        container.add(formButtonContainer, data);

        LayoutContainer wrapper = (LayoutContainer) Registry.get(Constants.CENTER_PANEL);
        wrapper.removeAll();
        wrapper.add(container);
        wrapper.layout();

        GWT.log("Done Initializing the UI in " + (new java.util.Date().getTime() - time), null);
    }

    private void initUI() {
        long time = new java.util.Date().getTime();
        GWT.log("Initializing the UI ", null);

        controller.handleEvent(new AppEvent(AppEvents.EntitiesRequest));

        // container = new LayoutContainer();
        container = new ContentPanel();
        container.setLayout(new BorderLayout());
        container.setHeading("Add New Entity");

        ToolBar toolBar = new ToolBar();

        entityCombo = new ComboBox<EntityWeb>();
        entityCombo.setEmptyText("Select a entity definition...");
        entityCombo.setForceSelection(true);
        entityCombo.setFieldLabel("Entity");
        entityCombo.setDisplayField("displayName");
        entityCombo.setToolTip("Entities");
        entityCombo.setWidth(200);
        entityCombo.setStore(entityStore);
        entityCombo.setTypeAhead(true);
        entityCombo.setTriggerAction(TriggerAction.ALL);

        // Select change
        entityCombo.addListener(Events.SelectionChange, new Listener<SelectionChangedEvent<EntityWeb>>()
        {
            public void handleEvent(SelectionChangedEvent<EntityWeb> sce) {
                currentEntity = sce.getSelectedItem();

                initEntityUI(currentEntity);

            }
        });

        Label displayLabel = new Label("Current Entity Model: ");
        displayLabel.setStyleAttribute("font-size", "11px");
        toolBar.add(displayLabel);
        toolBar.add(entityCombo);
        container.setTopComponent(toolBar);

        LayoutContainer wrapper = (LayoutContainer) Registry.get(Constants.CENTER_PANEL);
        wrapper.removeAll();
        wrapper.add(container);
        wrapper.layout();
        GWT.log("Done Initializing the UI in " + (new java.util.Date().getTime() - time), null);
    }

}
