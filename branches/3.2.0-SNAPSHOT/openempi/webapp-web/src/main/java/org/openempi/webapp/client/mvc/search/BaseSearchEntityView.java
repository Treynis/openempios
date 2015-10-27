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
import org.openempi.webapp.client.model.EntityAttributeGroupWeb;
import org.openempi.webapp.client.model.EntityAttributeWeb;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.IdentifierWeb;
import org.openempi.webapp.client.model.RecordLinkWeb;
import org.openempi.webapp.client.model.RecordWeb;
import org.openempi.webapp.client.mvc.BaseEntityView;
import org.openempi.webapp.client.mvc.Controller;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.HeaderGroupConfig;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;

public class BaseSearchEntityView extends BaseEntityView
{
    protected Dialog linkPairDialog = null;
    protected Map<String, String> recordFieldMap;

    protected ListStore<BaseModelData> linkPairStore = new ListStore<BaseModelData>();
    protected ListStore<IdentifierWeb> leftIdentifierStore = new ListStore<IdentifierWeb>();
    protected ListStore<IdentifierWeb> rightIdentifierStore = new ListStore<IdentifierWeb>();

    protected RecordWeb selectedUnlinkedRecord;
    protected RecordLinkWeb unlinkedPair;


    public BaseSearchEntityView(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {

    }

    protected void buildRecordLinkPairDialog(final String view, EntityWeb entity) {
        if (linkPairDialog != null) {
            return;
        }

        linkPairDialog = new Dialog();
        linkPairDialog.setBodyBorder(false);
        linkPairDialog.setWidth(910);
        linkPairDialog.setHeight(500);
        linkPairDialog.setButtons(Dialog.OKCANCEL);
        linkPairDialog.setModal(true);
        linkPairDialog.getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                EntityWeb currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
                AppEvent event = null;
                if (view.equals(Constants.BASIC_SEARCH)) {
                    event = new AppEvent(AppEvents.ProcessBasicUnlink, unlinkedPair);

                } else {
                    event = new AppEvent(AppEvents.ProcessUnlink, unlinkedPair);
                }
                event.setData("entityModel", currentEntity);
                controller.handleEvent(event);

                linkPairDialog.close();
            }
        });

        linkPairDialog.getButtonById(Dialog.CANCEL).addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                linkPairDialog.hide();
            }
        });

        ContentPanel cp = new ContentPanel();
        cp.setHeading("Linked Pair");
        cp.setFrame(true);
        cp.setIcon(IconHelper.create("images/link.png"));
        cp.setLayout(new FillLayout());
        cp.setSize(900, 530);

        LayoutContainer formGridContainer = new LayoutContainer();
        formGridContainer.setScrollMode(Scroll.AUTO);

        // forms
        LayoutContainer formContainer = new LayoutContainer();
        formContainer.setBorders(false);
        TableLayout layout = new TableLayout(2);
        layout.setWidth("900"); //"100%"
        layout.setCellSpacing(5);
        layout.setCellVerticalAlign(VerticalAlignment.TOP);
        formContainer.setLayout(layout);

        formContainer.add(setupIdentifierGrid(leftIdentifierStore, "Left Record"));
        formContainer.add(setupIdentifierGrid(rightIdentifierStore, "Right Record"));

        formGridContainer.add(formContainer);
        formGridContainer.add(setupLinkPairPanel());
        cp.add(formGridContainer);

        linkPairDialog.add(cp);
    }

    protected void displayLeftEntityIdentifier(RecordWeb record) {
        if (record.getIdentifiers() == null) {
            return;
        }
        for (IdentifierWeb identifier : record.getIdentifiers()) {
            leftIdentifierStore.add(identifier);
        }
    }

    protected void displayRightEntityIdentifier(RecordWeb record) {
        if (record.getIdentifiers() == null) {
            return;
        }
        for (IdentifierWeb identifier : record.getIdentifiers()) {
            rightIdentifierStore.add(identifier);
        }
    }

    private Grid<IdentifierWeb> setupIdentifierGrid(ListStore<IdentifierWeb> identifierStore, String record) {
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        // Columns
        ColumnConfig column;

        column = new ColumnConfig();
        column.setId("identifier");
        column.setHeader("Identifier");
        column.setWidth(200);
        configs.add(column);

        column = new ColumnConfig();
        column.setId("identifierDomainName");
        column.setHeader("Identifier Domain Name");
        column.setWidth(200);
        configs.add(column);

        ColumnModel cm = new ColumnModel(configs);
        cm.addHeaderGroup(0, 0, new HeaderGroupConfig(record, 1, 4));
        Grid<IdentifierWeb> identifierGrid = new Grid<IdentifierWeb>(identifierStore, cm);

        identifierGrid.setStyleAttribute("borderTop", "none");
        identifierGrid.setBorders(true);
        identifierGrid.setBorders(true);
        identifierGrid.setStripeRows(true);
        identifierGrid.setWidth(430);
        identifierGrid.setHeight(86);

        return identifierGrid;
    }

    private FormPanel setupLinkPairPanel() {
        FormPanel linkPairPanel = new FormPanel();
        linkPairPanel.setHeaderVisible(false);
        linkPairPanel.setBodyBorder(false);
        linkPairPanel.setWidth("900");
        linkPairPanel.setHeight("300");
        linkPairPanel.setLayout(new CenterLayout());

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        // Columns
        ColumnConfig column;

        column = new ColumnConfig();
        column.setId("attribute");
        column.setHeader("Attribute Name");
        column.setWidth(260);
        configs.add(column);

        // Render to display the color and stripe
        GridCellRenderer<BaseModelData> colorRenderer = new GridCellRenderer<BaseModelData>() {
            public String render(BaseModelData model, String property, ColumnData config, int rowIndex,
                      int colIndex, ListStore<BaseModelData> store, Grid<BaseModelData> grid) {

                      // get cell value
                      String value = (String) model.get(property);
                      // String attributeName = (String) model.get("attribute");
                      String valueLeft = (String) model.get("leftRecord");
                      String valueRight = (String) model.get("rightRecord");

                      if (valueLeft == null && valueRight == null) {
                          return null;
                      }

                      String backgroundColor = "#E79191"; // "lightpink";
                      if (valueLeft == null || valueRight == null) {
                          if (value == null) {
                              return null;
                          }
                          return "<div style='background-color:" + backgroundColor + "'>" + value + "</div>";

                      }

                      if (valueLeft.equals(valueRight))  {
                          backgroundColor = "lightgreen";
                      }

                      // <span: background is set as long as the value is.  <div background is set whole length.
                      return "<div style='background-color:" + backgroundColor + "'>" + value + "</div>";
//                    config.style = "background-color:"+backgroundColor+";";
//                    return value;
                  }
                };

        column = new ColumnConfig();
        column.setId("leftRecord");
        column.setHeader("Left Record");
        column.setRenderer(colorRenderer);
        column.setWidth(260);
        configs.add(column);

        column = new ColumnConfig();
        column.setId("rightRecord");
        column.setHeader("Right Record");
        column.setRenderer(colorRenderer);
        column.setWidth(260);
        configs.add(column);

        ColumnModel cm = new ColumnModel(configs);
        Grid<BaseModelData> linkPairGrid = new Grid<BaseModelData>(linkPairStore, cm);

        linkPairGrid.setStyleAttribute("borderTop", "none");
        linkPairGrid.setBorders(true);
        linkPairGrid.setBorders(true);
        linkPairGrid.setStripeRows(true);
        linkPairGrid.setWidth(820);
        linkPairGrid.setHeight(300);

        linkPairPanel.add(linkPairGrid);

        return linkPairPanel;
    }

    protected void SetRecordFieldMap(EntityWeb entity) {
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
        List<EntityAttributeWeb> sortedEntityAttributes = new ArrayList<EntityAttributeWeb>(entity.getAttributes().size());
        if (entity.getAttributes() != null) {
            for (EntityAttributeWeb entityAttribute : entity.getAttributes()) {
                 sortedEntityAttributes.add(entityAttribute);
            }
            // sort by display order
            Collections.sort(sortedEntityAttributes, ATTRIBUTE_DISPLAY_ORDER);
        }


        recordFieldMap  =  new HashMap<String, String>();
        // Attributes with no group
        for (EntityAttributeWeb entityAttribute : sortedEntityAttributes) {
            if (entityAttribute.getEntityAttributeGroup() == null) {
               recordFieldMap.put(entityAttribute.getName() , entityAttribute.getDisplayName());
            }
        }

        // Attributes with group
        if (sortedAttributeGroups != null) {
            for (EntityAttributeGroupWeb attributeGroup: sortedAttributeGroups) {
                for (EntityAttributeWeb entityAttribute : sortedEntityAttributes) {

                    if (entityAttribute.getEntityAttributeGroup() != null
                        && entityAttribute.getEntityAttributeGroup().getName().equals(attributeGroup.getName())) {
                        recordFieldMap.put(entityAttribute.getName(), entityAttribute.getDisplayName());
                    }
                }

            }
        }
    }
}
