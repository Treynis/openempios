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

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.model.EntityAttributeGroupWeb;
import org.openempi.webapp.client.model.EntityAttributeWeb;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.IdentifierWeb;
import org.openempi.webapp.client.model.RecordWeb;
import org.openempi.webapp.client.mvc.BaseEntityView;
import org.openempi.webapp.client.mvc.Controller;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;

import com.google.gwt.core.client.GWT;

public class DeleteEntityView extends BaseEntityView
{
    public static final String BASIC_SEARCH = "Basic Search";
    public static final String ADVANCED_SEARCH = "Advanced Search";

    private ContentPanel container;
    private LayoutContainer formButtonContainer;
    private LayoutContainer identifierContainer;
    private LayoutContainer topContainer;
    private LayoutContainer formContainer;
    private FormPanel topFormPanel;
    private FormPanel leftFormPanel;
    private FormPanel rightFormPanel;
    private FormPanel buttonPanel;

    private ListStore<IdentifierWeb> identifierStore = new ListStore<IdentifierWeb>();
    private Grid<IdentifierWeb> identifierGrid;

    private Status status;
    private Button submitButton;
    private Button cancelButton;

    private String fromPage;
    private EntityWeb currentEntity;
    private RecordWeb currentRecord;

    // Map<String, Field<?>> attributeFieldMap;

    public DeleteEntityView(Controller controller) {
        super(controller);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleEvent(AppEvent event) {

        if (event.getType() == AppEvents.EntityDeleteView || event.getType() == AppEvents.EntityBasicDeleteView) {

            if (event.getType() == AppEvents.EntityBasicDeleteView) {
                fromPage = BASIC_SEARCH;
            } else {
                fromPage = ADVANCED_SEARCH;
            }

            identifierStore.removeAll();

            currentEntity = event.getData("entityModel");
            initEntityUI(currentEntity);

            currentRecord = event.getData();
            displayEntityRecord(attributeFieldMap, currentRecord);
            displayEntityIdentifier(currentRecord);

        } else if (event.getType() == AppEvents.EntityDeleteComplete) {

            // Info.display("Information: ", "EntityUpdateComplete");
            status.hide();
            submitButton.unmask();

            MessageBox.alert("Information", "Entity was successfully deleted", listenInfoMsg);

        } else if (event.getType() == AppEvents.Error) {

            status.hide();
            submitButton.unmask();

            String message = event.getData();
            MessageBox.alert("Information", "Failure: " + message, listenFailureMsg);
        }
    }

    final Listener<MessageBoxEvent> listenInfoMsg = new Listener<MessageBoxEvent>()
    {
        public void handleEvent(MessageBoxEvent ce) {
            Button btn = ce.getButtonClicked();
            if (btn.getText().equals("OK")) {

                if (fromPage.equals(ADVANCED_SEARCH)) {
                    controller.handleEvent(new AppEvent(AppEvents.EntityDeleteFinished, null));
                } else {
                    controller.handleEvent(new AppEvent(AppEvents.EntityBasicDeleteFinished, null));
                }
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

    final Listener<MessageBoxEvent> listenConfirmDelete = new Listener<MessageBoxEvent>()
    {
        public void handleEvent(MessageBoxEvent ce) {
            Button btn = ce.getButtonClicked();
            if (btn.getText().equals("Yes")) {

                // RecordWeb record = getEntityFromGUI(currentRecord, attributeFieldMap);
                RecordWeb record = currentRecord;

                AppEvent event;
                if (fromPage.equals(ADVANCED_SEARCH)) {
                    event = new AppEvent(AppEvents.EntityDelete, record);
                } else {
                    event = new AppEvent(AppEvents.EntityBasicDelete, record);
                }
                event.setData("entityModel", currentEntity);
                controller.handleEvent(event);

                status.show();
                submitButton.mask();
            }
        }
    };

    private void displayEntityIdentifier(RecordWeb record) {
        if (record.getIdentifiers() == null) {
            return;
        }
        for (IdentifierWeb identifier : record.getIdentifiers()) {
            identifierStore.add(identifier);
        }
    }

    private FormPanel setupButtonPanel(int tabIndex) {
        FormPanel buttonPanel = new FormPanel();
        buttonPanel.setHeaderVisible(false);
        buttonPanel.setBodyBorder(false);
        buttonPanel.setWidth("960");
        buttonPanel.setStyleAttribute("paddingRight", "10px");
        buttonPanel.setButtonAlign(HorizontalAlignment.CENTER);

        submitButton = new Button("Confirm Delete", new SelectionListener<ButtonEvent>()
        {

            @Override
            public void componentSelected(ButtonEvent ce) {
                // Info.display("test:", "save componentSelected");

                MessageBox.confirm("Confirm",
                        "Delete operation cannot be undone. Are you sure you want to delete this entity?",
                        listenConfirmDelete);

            }
        });

        cancelButton = new Button("Cancel", new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                clearFormFields(attributeFieldMap);

                status.hide();
                submitButton.unmask();

                if (fromPage.equals(ADVANCED_SEARCH)) {
                    controller.handleEvent(new AppEvent(AppEvents.EntityDeleteCancel, null));
                } else {
                    controller.handleEvent(new AppEvent(AppEvents.EntityBasicDeleteCancel, null));
                }
            }
        });

        submitButton.setTabIndex(tabIndex++);
        cancelButton.setTabIndex(tabIndex++);

        status = new Status();
        status.setBusy("please wait...");
        buttonPanel.getButtonBar().add(status);
        status.hide();

        buttonPanel.getButtonBar().setSpacing(15);
        buttonPanel.addButton(submitButton);
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

        ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        cp.setWidth(widthPanel);
        cp.add(setupIdentifierGrid(identifierStore, widthPanel - 45, tabIndex));

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

        container = new ContentPanel();
        container.setLayout(new BorderLayout());
        container.setHeading("Delete Entity");

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

                    // create read only field
                    Field<?> field = createField(entityAttribute, true, true);
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
                            sortedEntityAttributes, true);

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

}
