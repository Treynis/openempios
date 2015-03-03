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
package org.openempi.webapp.client.mvc.blocking;

import java.util.ArrayList;
import java.util.List;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.model.BaseFieldWeb;
import org.openempi.webapp.client.model.BlockingEntryListWeb;
import org.openempi.webapp.client.model.ModelPropertyWeb;
import org.openempi.webapp.client.ui.util.Utility;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
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
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;

public class BlockingHPConfigurationView extends View
{
	private Grid<BaseFieldWeb> roundGrid;
	private GroupingStore<BaseFieldWeb> roundStore = new GroupingStore<BaseFieldWeb>();

	private Dialog addBlockingRoundDialog = null;
	private Boolean addOrEditRoundMode = true;
	private int editedMatchRound = 0;
	private ComboBox<ModelPropertyWeb> listCombo = null;
	private ListStore<ModelPropertyWeb> listStore = new ListStore<ModelPropertyWeb>();
	private Grid<BaseFieldWeb> addRoundGrid;
    private SpinnerField maximumBlockSizeSpin;

	private LayoutContainer container;

	@SuppressWarnings("unchecked")
	public BlockingHPConfigurationView(Controller controller) {
		super(controller);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void handleEvent(AppEvent event) {
		if (event.getType() == AppEvents.BlockingHPConfigurationView) {
			initUI();
		} else if (event.getType() == AppEvents.BlockingHPConfigurationReceived) {
		    roundStore.removeAll();

			BlockingEntryListWeb configuration = (BlockingEntryListWeb) event.getData();
			List<BaseFieldWeb> fields = configuration.getBlockingRoundEntries();

			// Blocking rounds
			for (BaseFieldWeb baseField : fields) {
				baseField.setFieldDescription(Utility.convertToDescription(baseField.getFieldName()));
				roundStore.add(baseField);
			}

	         // Maximum block size
            maximumBlockSizeSpin.setValue(0);
	        Integer maximumBlockSize = configuration.getMaximumBlockSize();
	        if (maximumBlockSize != null) {
    			maximumBlockSizeSpin.setValue(maximumBlockSize);
	        }
		} else if (event.getType() == AppEvents.BlockingHPConfigurationSaveComplete) {
			// String message = event.getData();
			// Info.display("Information", "Person was successfully added with message " + message);
	        MessageBox.alert("Information", "Traditional Blocking Configuration (High Performance) was successfully saved", null);

		} else if (event.getType() == AppEvents.Error) {
			String message = event.getData();
	        MessageBox.alert("Information", "Failure: " + message, null);
		}
	}

	@SuppressWarnings("unchecked")
	private void initUI() {
		long time = new java.util.Date().getTime();
		GWT.log("Initializing the UI ", null);

		listStore.removeAll();
		List<ModelPropertyWeb> attributeNames = (List<ModelPropertyWeb>) Registry.get(Constants.PERSON_MODEL_ALL_ATTRIBUTE_NAMES);
		if (attributeNames != null) {
			listStore.add(attributeNames);
		}

		roundStore.groupBy("blockingRound");
		controller.handleEvent(new AppEvent(AppEvents.BlockingHPConfigurationRequest));

		buildAddBlockingRoundDialog();
		container = new LayoutContainer();
		container.setLayout(new CenterLayout());

		ColumnConfig blockingRound = new ColumnConfig("blockingRound", "Blocking Round", 60);
		ColumnConfig fieldIndex = new ColumnConfig("fieldIndex", "Field Index", 60);
//		ColumnConfig fieldName = new ColumnConfig("fieldName", "Field Name", 100);
		ColumnConfig fieldName = new ColumnConfig("fieldDescription", "Field Name", 100);
		List<ColumnConfig> config = new ArrayList<ColumnConfig>();
		config.add(blockingRound);
		config.add(fieldIndex);
		config.add(fieldName);

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

		roundGrid = new Grid<BaseFieldWeb>(roundStore, cm);
		roundGrid.setView(view);
		roundGrid.setBorders(true);

		ContentPanel cp = new ContentPanel();
		cp.setHeading("Traditional Blocking Configuration (High Performance)");
		cp.setFrame(true);
		cp.setIcon(IconHelper.create("images/folder.png"));
		cp.setLayout(new FillLayout());
		cp.setSize(500, 350);

		ToolBar toolBar = new ToolBar();
	    LabelField maximumBlockSizeSpinLabel = new LabelField("Maximum Block Size ");
        maximumBlockSizeSpin = new SpinnerField();
        maximumBlockSizeSpin.setAllowDecimals(false);
        maximumBlockSizeSpin.setAllowNegative(false);
        maximumBlockSizeSpin.setFieldLabel("Maximum Block Size: ");
        maximumBlockSizeSpin.setMinValue(0);
        maximumBlockSizeSpin.setMaxValue(1000);
        maximumBlockSizeSpin.setWidth(60);

        // add some space for error icon
        Status emptyStatus = new Status();
        emptyStatus.setText("");
        emptyStatus.setWidth(15);

        toolBar.add(maximumBlockSizeSpinLabel);
        toolBar.add(maximumBlockSizeSpin);
        toolBar.add(emptyStatus);

		toolBar.add(new Button("Add Round", IconHelper.create("images/folder_go.png"), new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  // Make sure we are starting with a clean slate

	              addBlockingRoundDialog.setIcon(IconHelper.create("images/folder_go.png"));
	              addBlockingRoundDialog.setHeading("Add Blocking Round");
                  Button ok = addBlockingRoundDialog.getButtonById("ok");
         //         ok.setText("Add Round");

	              addOrEditRoundMode = true;
	        	  addRoundGrid.getStore().removeAll();
	        	  addBlockingRoundDialog.show();

                  listCombo.clearSelections();
	          }
	    }));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new Button("Edit Round", IconHelper.create("images/folder_edit.png"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                BaseFieldWeb editField = roundGrid.getSelectionModel().getSelectedItem();
                if (editField == null) {
                    Info.display("Information","You must first select a field in the Round to be edited before pressing the \"Edit Round\" button.");
                    return;
                }

                addBlockingRoundDialog.setIcon(IconHelper.create("images/folder_edit.png"));
                addBlockingRoundDialog.setHeading("Edit Blocking Round");
                Button ok = addBlockingRoundDialog.getButtonById("ok");
         //       ok.setText("Edit Round");

                addOrEditRoundMode = false;
                addRoundGrid.getStore().removeAll();
                editedMatchRound = editField.getBlockingRound();
                for (BaseFieldWeb field : roundGrid.getStore().getModels()) {
                    if (field.getBlockingRound() == editField.getBlockingRound()) {

                        BaseFieldWeb roundFieldWeb = new BaseFieldWeb();
                        roundFieldWeb.setFieldIndex(field.getFieldIndex());
                        roundFieldWeb.setFieldName(field.getFieldName());
                        roundFieldWeb.setFieldDescription(field.getFieldDescription());
                        addRoundGrid.getStore().add(roundFieldWeb);
                    }
                }
                addBlockingRoundDialog.show();

                listCombo.clearSelections();

            }
        }));
		toolBar.add(new SeparatorToolItem());
		toolBar.add(new Button("Remove Round", IconHelper.create("images/folder_delete.png"), new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  BaseFieldWeb removeField = roundGrid.getSelectionModel().getSelectedItem();
	        	  if (removeField == null) {
	        		  Info.display("Information","You must first select a field in the round to be deleted before pressing the \"Remove Round\" button.");
	        		  return;
	        	  }
	        	  for (BaseFieldWeb field : roundGrid.getStore().getModels()) {
	        		  if (field.getBlockingRound() == removeField.getBlockingRound()) {
	        			  roundStore.remove(field);
	        		  } else if (field.getBlockingRound() > removeField.getBlockingRound()) {
	        			  BaseFieldWeb theField = field;
	        			  roundStore.remove(field);
	        			  theField.setBlockingRound(theField.getBlockingRound() - 1);
	        			  roundStore.add(theField);
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

	              BlockingEntryListWeb configuration = new BlockingEntryListWeb();

	              configuration.setMaximumBlockSize(maximumBlockSizeSpin.getValue().intValue());
	              configuration.setBlockingRoundtEntries(roundGrid.getStore().getModels());

	        	  controller.handleEvent(new AppEvent(AppEvents.BlockingHPConfigurationSave, configuration));
	          }
	    }), layoutData);
		cp.setBottomComponent(c);
		cp.add(roundGrid);

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

	private void buildAddBlockingRoundDialog() {
		if (addBlockingRoundDialog != null) {
			return;
		}

		addBlockingRoundDialog = new Dialog();
		addBlockingRoundDialog.setBodyBorder(false);
		addBlockingRoundDialog.setIcon(IconHelper.create("images/folder_go.png"));
		addBlockingRoundDialog.setHeading("Add Blocking Round");
		addBlockingRoundDialog.setWidth(382);
		addBlockingRoundDialog.setHeight(300);
		addBlockingRoundDialog.setButtons(Dialog.OKCANCEL);
//		addBlockingRoundDialog.setHideOnButtonClick(true);
		addBlockingRoundDialog.setModal(true);
		addBlockingRoundDialog.getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	            if (addOrEditRoundMode) { // Add  Round
	        	  // check duplicate Round
				  int roundCount = getCurrentRoundCount(roundStore);
				  for (int i = 1; i <= roundCount; i++) {
					  // get round with index i
					  List<BaseFieldWeb> round = new ArrayList<BaseFieldWeb>();
					  for (BaseFieldWeb fieldInRound : roundStore.getModels()) {
						   if (fieldInRound.getBlockingRound() == i) {
							   round.add(fieldInRound);
						   }
					  }

					  // check round same as round added in Grid
					  boolean sameRound = true;
					  if (round.size() ==  addRoundGrid.getStore().getModels().size()) {
						  int roundIndex = 0;
			        	  for (BaseFieldWeb field : addRoundGrid.getStore().getModels()) {
			        		  if (!field.getFieldName().equals(round.get(roundIndex).getFieldName())) {
			        			  sameRound = false;
			        		  }
			        		  roundIndex++;
			        	  }
					  } else {
						  sameRound = false;
					  }

					  if (sameRound) {
		        		  // Info.display("Information", "same round.");
		        	      MessageBox.alert("Information", "There is a duplicate blocking round in Blocking Configuration", listenInfoMsg);
		        	      return;
					  }
				  }

	        	  int roundIndex = getCurrentRoundCount(roundStore) + 1;
	        	  for (BaseFieldWeb field : addRoundGrid.getStore().getModels()) {
	        	      roundStore.add(new BaseFieldWeb(roundIndex, field.getFieldIndex(), field.getFieldName(), field.getFieldDescription()));
	        	  }
	            } else { // Edit Round
                    // check duplicate Round
                    int roundCount = getCurrentRoundCount(roundStore);
                    for (int i = 1; i <= roundCount; i++) {
                        if (editedMatchRound == i) {
                            continue;
                        }

                        // get round with index i
                        List<BaseFieldWeb> round = new ArrayList<BaseFieldWeb>();
                        for (BaseFieldWeb fieldInRound : roundStore.getModels()) {
                             if (fieldInRound.getBlockingRound() == i) {
                                 round.add(fieldInRound);
                             }
                        }

                        // check round same as round added in Grid
                        boolean sameRound = true;
                        if (round.size() ==  addRoundGrid.getStore().getModels().size()) {
                            int roundIndex = 0;
                            for (BaseFieldWeb field : addRoundGrid.getStore().getModels()) {
                                if (!field.getFieldName().equals(round.get(roundIndex).getFieldName())) {
                                    sameRound = false;
                                }
                                roundIndex++;
                            }
                        } else {
                            sameRound = false;
                        }

                        if (sameRound) {
                            // Info.display("Information", "same round.");
                            MessageBox.alert("Information", "There is a duplicate blocking round in Blocking Configuration", listenInfoMsg);
                            return;
                        }
                    }

                    // remove old rounds
                    for (BaseFieldWeb field : roundGrid.getStore().getModels()) {
                        if (field.getBlockingRound() == editedMatchRound) {
                            roundStore.remove(field);
                        }
                    }
                    // add new rounds
                    for (BaseFieldWeb field : addRoundGrid.getStore().getModels()) {
                        roundStore.add(new BaseFieldWeb(editedMatchRound, field.getFieldIndex(), field.getFieldName(), field.getFieldDescription()));
                    }
	            }

	        	addBlockingRoundDialog.hide();
	          }

			  private int getCurrentRoundCount(GroupingStore<BaseFieldWeb> store) {
				int roundCount = 0;
				for (BaseFieldWeb field : store.getModels()) {
					if (field.getBlockingRound() > roundCount) {
						roundCount = field.getBlockingRound();
					}
				}
				return roundCount;
			  }
	    });

		addBlockingRoundDialog.getButtonById(Dialog.CANCEL).addSelectionListener(new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  addBlockingRoundDialog.hide();
	          }
	    });

		ContentPanel cp = new ContentPanel();
		cp.setHeading("Blocking Round");
		cp.setFrame(true);
		cp.setIcon(IconHelper.create("images/folder.png"));
		cp.setLayout(new FillLayout());
		cp.setSize(370, 300);

		ToolBar toolBar = new ToolBar();

		listCombo = new ComboBox<ModelPropertyWeb>();
		listCombo.setEmptyText("Select a field...");
		listCombo.setForceSelection(true);
//		combo.setDisplayField("name");
		listCombo.setDisplayField("description");
		listCombo.setWidth(150);
		listCombo.setStore(listStore);
		listCombo.setTypeAhead(true);
		listCombo.setTriggerAction(TriggerAction.ALL);

		toolBar.add(listCombo);
		toolBar.add(new Button("Add Field", IconHelper.create("images/folder_go.png"), new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  List<ModelPropertyWeb> selection = listCombo.getSelection();
	        	  if (selection == null || selection.size() == 0) {
	        		  Info.display("Information", "Please select a field before pressing the \"Add Field\" button.");
	        		  return;
	        	  }
	        	  ModelPropertyWeb field = selection.get(0);
	        	  if (!fieldInList(field, addRoundGrid.getStore())) {
	        		  addRoundGrid.getStore().add(new BaseFieldWeb(1, addRoundGrid.getStore().getCount()+1, field.getName(), field.getDescription()));
	        	  }
	          }

			private boolean fieldInList(ModelPropertyWeb field, ListStore<BaseFieldWeb> listStore) {
				for (BaseFieldWeb item : listStore.getModels()) {
					if (item.getFieldName().equalsIgnoreCase(field.getName())) {
						return true;
					}
				}
				return false;
			}
	    }));
		toolBar.add(new SeparatorToolItem());
		toolBar.add(new Button("Remove Field", IconHelper.create("images/folder_delete.png"), new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  BaseFieldWeb field = addRoundGrid.getSelectionModel().getSelectedItem();
	        	  if (field == null) {
	        		  Info.display("Information","You must first select a field before pressing the \"Remove Field\" button.");
	        		  return;
	        	  }
                  Integer removedFieldIndex = field.getFieldIndex();
                  addRoundGrid.getStore().remove(field);

                  // switch indexes
                  for (BaseFieldWeb item : addRoundGrid.getStore().getModels()) {
                       Integer fieldIndex = item.getFieldIndex();
                       if (fieldIndex > removedFieldIndex) {
                           item.setFieldIndex(fieldIndex-1);
                       }
                  }
                  addRoundGrid.getView().refresh(false);
	          }
	    }));
		cp.setTopComponent(toolBar);

		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig column = new ColumnConfig();
		column.setId("fieldIndex");
		column.setHeader("Field Index");
		column.setAlignment(HorizontalAlignment.RIGHT);
		column.setWidth(100);
		configs.add(column);

		column = new ColumnConfig();
//		column.setId("fieldName");
		column.setId("fieldDescription");
		column.setHeader("Field Name");
		column.setAlignment(HorizontalAlignment.RIGHT);
		column.setWidth(150);
		configs.add(column);

		ListStore<BaseFieldWeb> store = new ListStore<BaseFieldWeb>();

		ColumnModel cm = new ColumnModel(configs);

		addRoundGrid = new Grid<BaseFieldWeb>(store, cm);
		addRoundGrid.setStyleAttribute("borderTop", "none");
//		addRoundGrid.setAutoExpandColumn("fieldName");
		addRoundGrid.setBorders(true);
		addRoundGrid.setStripeRows(true);
		cp.add(addRoundGrid);

		addBlockingRoundDialog.add(cp);
	}


}
