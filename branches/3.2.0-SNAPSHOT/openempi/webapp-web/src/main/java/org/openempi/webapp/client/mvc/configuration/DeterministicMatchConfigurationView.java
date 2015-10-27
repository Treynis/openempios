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
package org.openempi.webapp.client.mvc.configuration;

import java.util.ArrayList;
import java.util.List;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.model.MatchFieldWeb;
import org.openempi.webapp.client.model.MatchRuleEntryListWeb;
import org.openempi.webapp.client.model.MatchRuleWeb;
import org.openempi.webapp.client.model.ModelPropertyWeb;
import org.openempi.webapp.client.ui.util.Utility;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

public class DeterministicMatchConfigurationView extends View
{
	private static final NumberFormat nfc = NumberFormat.getFormat("#,##0.0000");

	private Grid<MatchRuleWeb> ruleGrid;
	private GroupingStore<MatchRuleWeb> ruleStore = new GroupingStore<MatchRuleWeb>();
	private Grid<MatchFieldWeb> grid;
	private ListStore<MatchFieldWeb> store = new ListStore<MatchFieldWeb>();

	private Dialog addMatchRuleDialog = null;
	private Boolean addOrEditRuleMode = true;
	private int editedMatchRule = 0;

	private Dialog addEditMatchFieldDialog = null;
	private Boolean addOrEditFieldMode = true;
	private int editedFieldIndex = 0;
	private MatchFieldWeb editedField;
	private List<ModelPropertyWeb> attributeNames;
	private ListStore<ModelPropertyWeb> attributeNameStore = new ListStore<ModelPropertyWeb>();
	private ListStore<ModelPropertyWeb> comparatorFuncNameStore = new ListStore<ModelPropertyWeb>();

	private ComboBox<ModelPropertyWeb> attributeNameCombo = new ComboBox<ModelPropertyWeb>();
	private ComboBox<ModelPropertyWeb> comparatorFuncNameCombo = new ComboBox<ModelPropertyWeb>();
	private NumberField matchThresholdEdit = new NumberField();

	private LayoutContainer container;

	@SuppressWarnings("unchecked")
	public DeterministicMatchConfigurationView(Controller controller) {
		super(controller);

		List<ModelPropertyWeb> comparatorFuncNames = (List<ModelPropertyWeb>) Registry.get(Constants.COMPARATOR_FUNCTION_NAMES);

		try {
			comparatorFuncNameStore.add(comparatorFuncNames);
		} catch (Exception e) {
			Info.display("Message", e.getMessage());
		}
	}

	@Override
	protected void handleEvent(AppEvent event) {
		if (event.getType() == AppEvents.DeterministicMatchConfigurationView) {
			initUI();
		} else if (event.getType() == AppEvents.DeterministicMatchConfigurationReceived) {
			ruleStore.removeAll();

			MatchRuleEntryListWeb config = (MatchRuleEntryListWeb) event.getData();
            List<MatchRuleWeb> fields = config.getMatchRuleEntries();

            // Matching rules
            for (MatchRuleWeb rule : fields) {
                rule.setFieldDescription(Utility.convertToDescription(rule.getFieldName()));
                rule.setComparatorFunctionNameDescription(Utility.convertToDescription(rule.getComparatorFunctionName()));
                rule.setMatchThreshold(Float.parseFloat(nfc.format(rule.getMatchThreshold())));
                ruleStore.add(rule);
            }

			ruleGrid.getSelectionModel().select(0, true);
			ruleGrid.getSelectionModel().deselect(0);

		} else if (event.getType() == AppEvents.DeterministicMatchConfigurationSaveComplete) {
			// String message = event.getData();
	        MessageBox.alert("Information", "Deterministic Match Configuration was successfully saved", null);

		} else if (event.getType() == AppEvents.Error) {
			String message = event.getData();
	        MessageBox.alert("Information", "Failure: " + message, null);
		}
	}

    @SuppressWarnings("unchecked")
    private void initUI() {
        long time = new java.util.Date().getTime();
        GWT.log("Initializing the UI ", null);

        attributeNameStore.removeAll();
        attributeNames = (List<ModelPropertyWeb>) Registry.get(Constants.PERSON_MODEL_ALL_ATTRIBUTE_NAMES);
        if (attributeNames!= null) {
           attributeNameStore.add(attributeNames);
        }

        ruleStore.groupBy("matchRule");
        controller.handleEvent(new AppEvent(AppEvents.DeterministicMatchConfigurationRequest));

        buildAddMatchRuleDialog();
        container = new LayoutContainer();
        container.setLayout(new CenterLayout());

        ColumnConfig matchRule = new ColumnConfig("matchRule", "Rule", 60);
//      ColumnConfig fieldName = new ColumnConfig("fieldName", "Field Name", 100);
        ColumnConfig fieldName = new ColumnConfig("fieldDescription", "Field Name", 150);
        ColumnConfig compFuncName = new ColumnConfig("comparatorFunctionNameDescription", "Comparator Name", 180);
        ColumnConfig matchThreshold = new ColumnConfig("matchThreshold", "Match Threshold", 120);
        List<ColumnConfig> config = new ArrayList<ColumnConfig>();
        config.add(matchRule);
        config.add(fieldName);
        config.add(compFuncName);
        config.add(matchThreshold);

        final ColumnModel cm = new ColumnModel(config);

        GroupingView view = new GroupingView();
        view.setShowGroupedColumn(false);
        view.setForceFit(true);
        view.setGroupRenderer(new GridGroupRenderer() {
            public String render(GroupColumnData data) {
                String f = cm.getColumnById(data.field).getHeader();
                String l = data.models.size() == 1 ? "Field" : "Fields";
                return f + ": " + data.group + " (" + data.models.size() + " " + l + ")";
            } });

        ruleGrid = new Grid<MatchRuleWeb>(ruleStore, cm);
        ruleGrid.setView(view);
        ruleGrid.setBorders(true);

        ContentPanel cp = new ContentPanel();
        cp.setHeading("Deterministic Match Rule Configuration");
        cp.setFrame(true);
        cp.setIcon(IconHelper.create("images/table_gear.png"));
        cp.setLayout(new FillLayout());
        cp.setSize(500, 380);

        ToolBar toolBar = new ToolBar();
        toolBar.add(new Button("Add Rule", IconHelper.create("images/folder_go.png"), new SelectionListener<ButtonEvent>() {
              @Override
              public void componentSelected(ButtonEvent ce) {
                  // Make sure we are starting with a clean slate
                  addMatchRuleDialog.setIcon(IconHelper.create("images/folder_go.png"));
                  addMatchRuleDialog.setHeading("Add Match Rule");
                  Button ok = addMatchRuleDialog.getButtonById("ok");
                  ok.setText("Add Rule");

                  addOrEditRuleMode = true;
                  grid.getStore().removeAll();
                  addMatchRuleDialog.show();
              }
        }));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new Button("Edit Rule", IconHelper.create("images/folder_edit.png"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                MatchRuleWeb editField = ruleGrid.getSelectionModel().getSelectedItem();
                if (editField == null) {
                    Info.display("Information","You must first select a field in the rule to be edited before pressing the \"Edit Rule\" button.");
                    return;
                }

                addMatchRuleDialog.setIcon(IconHelper.create("images/folder_edit.png"));
                addMatchRuleDialog.setHeading("Edit Match Rule");
                Button ok = addMatchRuleDialog.getButtonById("ok");
                ok.setText("Edit Rule");

                addOrEditRuleMode = false;
                grid.getStore().removeAll();
                editedMatchRule = editField.getMatchRule();
                for (MatchRuleWeb field : ruleGrid.getStore().getModels()) {
                    if (field.getMatchRule() == editField.getMatchRule()) {

                        MatchFieldWeb matchFieldWeb = new MatchFieldWeb();
                        matchFieldWeb.setFieldName(field.getFieldName());
                        matchFieldWeb.setFieldDescription(field.getFieldDescription());
                        matchFieldWeb.setComparatorFunctionName(field.getComparatorFunctionName());
                        matchFieldWeb.setComparatorFunctionNameDescription(field.getComparatorFunctionNameDescription());
                        matchFieldWeb.setMatchThreshold(field.getMatchThreshold());
                        grid.getStore().add(matchFieldWeb);
                    }
                }
                addMatchRuleDialog.show();

            }
        }));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new Button("Remove Rule", IconHelper.create("images/folder_delete.png"), new SelectionListener<ButtonEvent>() {
              @Override
              public void componentSelected(ButtonEvent ce) {
                  MatchRuleWeb removeField = ruleGrid.getSelectionModel().getSelectedItem();
                  if (removeField == null) {
                      Info.display("Information","You must first select a field in the rule to be deleted before pressing the \"Remove Rule\" button.");
                      return;
                  }
                  for (MatchRuleWeb field : ruleGrid.getStore().getModels()) {
                      if (field.getMatchRule() == removeField.getMatchRule()) {
                          ruleStore.remove(field);
                      } else if (field.getMatchRule() > removeField.getMatchRule()) {
                          MatchRuleWeb theField = field;
                          ruleStore.remove(field);
                          theField.setMatchRule(theField.getMatchRule() - 1);
                          ruleStore.add(theField);
                      }
                  }
              }
        }));
        cp.setTopComponent(toolBar);

        LayoutContainer c = new LayoutContainer();
        HBoxLayout layout = new HBoxLayout();
        layout.setPadding(new Padding(5));
        layout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
        layout.setPack(BoxLayoutPack.CENTER);
        c.setLayout(layout);

        HBoxLayoutData layoutData = new HBoxLayoutData(new Margins(0, 5, 0, 0));
        c.add(new Button("Save Settings", IconHelper.create("images/folder_go.png"), new SelectionListener<ButtonEvent>() {
              @Override
              public void componentSelected(ButtonEvent ce) {

                  MatchRuleEntryListWeb configuration = new MatchRuleEntryListWeb();
                  configuration.setMatchRuleEntries(ruleGrid.getStore().getModels());

                  controller.handleEvent(new AppEvent(AppEvents.DeterministicMatchConfigurationSave, configuration));
              }
        }), layoutData);
        cp.setBottomComponent(c);
        cp.add(ruleGrid);

        container.add(cp);

        LayoutContainer wrapper = (LayoutContainer) Registry.get(Constants.CENTER_PANEL);
        wrapper.removeAll();
        wrapper.add(container);
        wrapper.layout();
        GWT.log("Done Initializing the UI in " + (new java.util.Date().getTime() - time), null);
    }

    final Listener<MessageBoxEvent> listenInfoMsg = new Listener<MessageBoxEvent>() {
        public void handleEvent(MessageBoxEvent ce) {
          Button btn = ce.getButtonClicked();
          if (btn.getText().equals("OK")) {
              return;
          }
        }
    };

	private void buildAddMatchRuleDialog() {
        if (addMatchRuleDialog != null) {
            return;
        }

        buildAddEditFieldDialog();

        addMatchRuleDialog = new Dialog();
        addMatchRuleDialog.setBodyBorder(false);
        addMatchRuleDialog.setIcon(IconHelper.create("images/folder_go.png"));
        addMatchRuleDialog.setHeading("Add Match Rule");
        addMatchRuleDialog.setWidth(480);
        addMatchRuleDialog.setHeight(300);
        addMatchRuleDialog.setButtons(Dialog.OKCANCEL);
        addMatchRuleDialog.setModal(true);
        addMatchRuleDialog.getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>() {
              @Override
              public void componentSelected(ButtonEvent ce) {
                  if (addOrEditRuleMode) { // Add  Rule
                      // check duplicate Rule
                      int ruleCount = getCurrentRuleCount(ruleStore);
                      for (int i = 1; i <= ruleCount; i++) {
                          // get round with index i
                          List<MatchRuleWeb> rule = new ArrayList<MatchRuleWeb>();
                          for (MatchRuleWeb fieldInRule : ruleStore.getModels()) {
                               if (fieldInRule.getMatchRule() == i) {
                                   rule.add(fieldInRule);
                               }
                          }

                          // check round same as round added in Grid
                          boolean sameRule = true;
                          if (rule.size() ==  grid.getStore().getModels().size()) {
                              int roundIndex = 0;
                              for (MatchFieldWeb field : grid.getStore().getModels()) {
                                  if (!field.getFieldName().equals(rule.get(roundIndex).getFieldName())) {
                                      sameRule = false;
                                  }
                                  roundIndex++;
                              }
                          } else {
                              sameRule = false;
                          }

                          if (sameRule) {
                              // Info.display("Information", "same round.");
                              MessageBox.alert("Information", "There is a duplicate match rule in Blocking Configuration", listenInfoMsg);
                              return;
                          }
                      }

                      int ruleIndex = getCurrentRuleCount(ruleStore) + 1;
                      for (MatchFieldWeb field : grid.getStore().getModels()) {
                          MatchRuleWeb newRule = new MatchRuleWeb(ruleIndex, field.getFieldIndex(), field.getFieldName());
                          newRule.setFieldDescription(Utility.convertToDescription(field.getFieldName()));
                          newRule.setComparatorFunctionName(field.getComparatorFunctionName());
                          newRule.setComparatorFunctionNameDescription(Utility.convertToDescription(field.getComparatorFunctionName()));
                          newRule.setMatchThreshold(Float.parseFloat(nfc.format(field.getMatchThreshold())));
                          ruleStore.add(newRule);
                      }
                  } else { // Edit Rule
                      // check duplicate Rule
                      int ruleCount = getCurrentRuleCount(ruleStore);
                      for (int i = 1; i <= ruleCount; i++) {
                          if (editedMatchRule == i) {
                              continue;
                          }

                          // get round with index i
                          List<MatchRuleWeb> rule = new ArrayList<MatchRuleWeb>();
                          for (MatchRuleWeb fieldInRule : ruleStore.getModels()) {
                               if (fieldInRule.getMatchRule() == i) {
                                   rule.add(fieldInRule);
                               }
                          }

                          // check round same as round added in Grid
                          boolean sameRule = true;
                          if (rule.size() ==  grid.getStore().getModels().size()) {
                              int roundIndex = 0;
                              for (MatchFieldWeb field : grid.getStore().getModels()) {
                                  if (!field.getFieldName().equals(rule.get(roundIndex).getFieldName())) {
                                      sameRule = false;
                                  }
                                  roundIndex++;
                              }
                          } else {
                              sameRule = false;
                          }

                          if (sameRule) {
                              // Info.display("Information", "same round.");
                              MessageBox.alert("Information", "There is a duplicate match rule in Blocking Configuration", listenInfoMsg);
                              return;
                          }
                      }

                      // remove old rules
                      for (MatchRuleWeb field : ruleGrid.getStore().getModels()) {
                          if (field.getMatchRule() == editedMatchRule) {
                              ruleStore.remove(field);
                          }
                      }
                      // add new rules
                      for (MatchFieldWeb field : grid.getStore().getModels()) {
                          MatchRuleWeb newRule = new MatchRuleWeb(editedMatchRule, field.getFieldIndex(), field.getFieldName());
                          newRule.setFieldDescription(Utility.convertToDescription(field.getFieldName()));
                          newRule.setComparatorFunctionName(field.getComparatorFunctionName());
                          newRule.setComparatorFunctionNameDescription(Utility.convertToDescription(field.getComparatorFunctionName()));
                          newRule.setMatchThreshold(Float.parseFloat(nfc.format(field.getMatchThreshold())));
                          ruleStore.add(newRule);
                      }
                  }

                  addMatchRuleDialog.hide();
              }

              private int getCurrentRuleCount(GroupingStore<MatchRuleWeb> store) {
                int ruleCount = 0;
                for (MatchRuleWeb field : store.getModels()) {
                    if (field.getMatchRule() > ruleCount) {
                        ruleCount = field.getMatchRule();
                    }
                }
                return ruleCount;
              }
        });

        addMatchRuleDialog.getButtonById(Dialog.CANCEL).addSelectionListener(new SelectionListener<ButtonEvent>() {
              @Override
              public void componentSelected(ButtonEvent ce) {
                  addMatchRuleDialog.hide();
              }
        });

        ContentPanel cp = new ContentPanel();
        cp.setHeading("Match Rule");
        cp.setFrame(true);
        cp.setIcon(IconHelper.create("images/folder.png"));
        cp.setLayout(new FillLayout());
        cp.setSize(470, 230);

        ToolBar toolBar = new ToolBar();

        toolBar.add(new Button("Add Field", IconHelper.create("images/folder_go.png"), new SelectionListener<ButtonEvent>() {
              @Override
              public void componentSelected(ButtonEvent ce) {
                  // Make sure we are starting with a clean slate
                  addOrEditFieldMode = true;
                  addEditMatchFieldDialog.show();

                  attributeNameCombo.clearSelections();
                  comparatorFuncNameCombo.clearSelections();
                  matchThresholdEdit.clear();
              }
        }));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new Button("Edit Field", IconHelper.create("images/folder_edit.png"), new SelectionListener<ButtonEvent>() {
              @Override
              public void componentSelected(ButtonEvent ce) {
                  addOrEditFieldMode = false;
                  MatchFieldWeb editField = grid.getSelectionModel().getSelectedItem();
                  if (editField == null) {
                      Info.display("Information", "You must first select a field to be edited before pressing the \"Edit Field\" button.");
                      return;
                  }
                  addEditMatchFieldDialog.show();
                  editedFieldIndex = grid.getStore().indexOf(editField);
                  editedField = editField;

                  attributeNameCombo.setValue(new ModelPropertyWeb(editField.getFieldName(), editField.getFieldDescription()));
                  comparatorFuncNameCombo.setValue(new ModelPropertyWeb(editField.getComparatorFunctionName(), editField.getComparatorFunctionNameDescription()));

                  matchThresholdEdit.setValue(editField.getMatchThreshold());
              }
        }));

        toolBar.add(new SeparatorToolItem());
        toolBar.add(new Button("Remove Field", IconHelper.create("images/folder_delete.png"), new SelectionListener<ButtonEvent>() {
              @Override
              public void componentSelected(ButtonEvent ce) {
                  MatchFieldWeb removeField = grid.getSelectionModel().getSelectedItem();
                  if (removeField == null) {
                      Info.display("Information", "You must first select a field to be deleted before pressing the \"Remove Round\" button.");
                      return;
                  }
                  grid.getStore().remove(removeField);
              }
        }));
        cp.setTopComponent(toolBar);

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig fieldNameColumn = new ColumnConfig("fieldDescription", "Field Name", 150);
        ColumnConfig compFuncNameColumn = new ColumnConfig("comparatorFunctionNameDescription", "Comparator Name", 180);
        ColumnConfig matchThresholdColumn = new ColumnConfig("matchThreshold", "Match Threshold", 120);
        configs.add(fieldNameColumn);
        configs.add(compFuncNameColumn);
        configs.add(matchThresholdColumn);

        ColumnModel cm = new ColumnModel(configs);

        grid = new Grid<MatchFieldWeb>(store, cm);
        grid.setStyleAttribute("borderTop", "none");
        grid.setBorders(true);
        grid.setStripeRows(true);
        cp.add(grid);

        addMatchRuleDialog.add(cp);

	}

	private void buildAddEditFieldDialog() {
		if (addEditMatchFieldDialog != null) {
			return;
		}

		addEditMatchFieldDialog = new Dialog();
		addEditMatchFieldDialog.setBodyBorder(false);
		addEditMatchFieldDialog.setIcon(IconHelper.create("images/folder_go.png"));
		addEditMatchFieldDialog.setHeading("Add/Edit Match Field");
		addEditMatchFieldDialog.setWidth(460);
		addEditMatchFieldDialog.setHeight(300);
		addEditMatchFieldDialog.setButtons(Dialog.OKCANCEL);
//		addEditMatchFieldDialog.setHideOnButtonClick(true);
		addEditMatchFieldDialog.setModal(true);
		addEditMatchFieldDialog.getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				List<ModelPropertyWeb> attribNameSelection = attributeNameCombo.getSelection();
				List<ModelPropertyWeb> compFuncNameSelection = comparatorFuncNameCombo.getSelection();
				if (matchThresholdEdit.getValue() != null && attribNameSelection.size() > 0 && compFuncNameSelection.size() > 0) {
						ModelPropertyWeb attribNameField = attribNameSelection.get(0);
						ModelPropertyWeb compFuncNameField = compFuncNameSelection.get(0);

						MatchFieldWeb matchFieldWeb = new MatchFieldWeb();
						matchFieldWeb.setFieldName(attribNameField.getName());
						matchFieldWeb.setFieldDescription(attribNameField.getDescription());
						matchFieldWeb.setComparatorFunctionName(compFuncNameField.getName());
						matchFieldWeb.setComparatorFunctionNameDescription(compFuncNameField.getDescription());
						matchFieldWeb.setMatchThreshold(matchThresholdEdit.getValue().floatValue());

						if (addOrEditFieldMode) {  // Add
				        	// check duplicate Field
				        	for (MatchFieldWeb field : grid.getStore().getModels()) {
				        		  if (field.getFieldName().equals(matchFieldWeb.getFieldName())) {
					        	      MessageBox.alert("Information", "There is a duplicate match field in Deterministic Match Configuration", null);
					        	      return;
				        		  }
				        	}
							grid.getStore().add(matchFieldWeb);

						} else { // Edit
				        	for (MatchFieldWeb field : grid.getStore().getModels()) {
				        		if (field.getFieldName() != editedField.getFieldName()) {
					        		if (field.getFieldName().equals(matchFieldWeb.getFieldName())) {
						        	    MessageBox.alert("Information", "There is a duplicate match field in Deterministic Match Configuration", null);
						        	    return;
					        		}
				        		}
				        	}
							grid.getStore().remove(editedField);
							grid.getStore().insert(matchFieldWeb, editedFieldIndex);
						}

						addEditMatchFieldDialog.close();

				} else {
					if (attribNameSelection.size() == 0) {
						MessageBox.alert("Information", "Please select Attribute Name", null);
						return;
					}
					if (compFuncNameSelection.size() == 0) {
		        	    MessageBox.alert("Information", "Please select Comparator Name", null);
						return;
					}
					if (matchThresholdEdit.getValue() == null) {
		        	    MessageBox.alert("Information", "Match Threshold is required.", null);
						return;
					}
				}
			}
	    });

		addEditMatchFieldDialog.getButtonById(Dialog.CANCEL).addSelectionListener(new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {

	        	  addEditMatchFieldDialog.close();
	          }
	    });

		ContentPanel cp = new ContentPanel();
		cp.setHeading("Match Field");
		cp.setFrame(true);
		cp.setIcon(IconHelper.create("images/folder.png"));
			FormLayout formLayout = new FormLayout();
			formLayout.setLabelWidth(150);
			formLayout.setDefaultWidth(280);
		cp.setLayout(formLayout);
		cp.setSize(450, 240);

		attributeNameCombo.setEmptyText("Select attribute...");
		attributeNameCombo.setForceSelection(true);
//		attributeNameCombo.setDisplayField("name");
		attributeNameCombo.setDisplayField("description");
		attributeNameCombo.setStore(attributeNameStore);
		attributeNameCombo.setTypeAhead(true);
		attributeNameCombo.setTriggerAction(TriggerAction.ALL);
		attributeNameCombo.setFieldLabel("Attribute Name");
		cp.add(attributeNameCombo);

		comparatorFuncNameCombo.setEmptyText("Select function...");
		comparatorFuncNameCombo.setForceSelection(true);
//		comparatorFuncNameCombo.setDisplayField("name");
		comparatorFuncNameCombo.setDisplayField("description");
		comparatorFuncNameCombo.setStore(comparatorFuncNameStore);
		comparatorFuncNameCombo.setTypeAhead(true);
		comparatorFuncNameCombo.setTriggerAction(TriggerAction.ALL);
		comparatorFuncNameCombo.setFieldLabel("Comparator Name");
		cp.add(comparatorFuncNameCombo);

		matchThresholdEdit.setFieldLabel("Match Threshold");
		cp.add(matchThresholdEdit);

		addEditMatchFieldDialog.add(cp);
	}

}
