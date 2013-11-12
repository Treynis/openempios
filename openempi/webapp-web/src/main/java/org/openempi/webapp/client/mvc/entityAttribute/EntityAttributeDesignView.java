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
package org.openempi.webapp.client.mvc.entityAttribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.model.ModelPropertyWeb;
import org.openempi.webapp.client.model.EntityAttributeDatatypeWeb;
import org.openempi.webapp.client.model.EntityAttributeValidationParameterWeb;
import org.openempi.webapp.client.model.EntityAttributeValidationWeb;
import org.openempi.webapp.client.model.EntityAttributeWeb;
import org.openempi.webapp.client.model.EntityAttributeGroupWeb;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.EntityValidationRuleWeb;
import org.openempi.webapp.client.model.UserFileWeb;
import org.openempi.webapp.client.ui.util.InputFormat;
import org.openempi.webapp.client.ui.util.Utility;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.RowEditor;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;

public class EntityAttributeDesignView extends View
{
	public static final String ADD_ENTITY = "Add";
	public static final String EDIT_ENTITY = "Edit";
	public static final String DELETE_ENTITY = "Delete";

	public static final Integer BASE_VERSION = new Integer(1);
	public static final Integer BASE_DISPLY_ORDER = new Integer(1);

	private Grid<EntityWeb> grid;
	private GroupingStore<EntityWeb> store = new GroupingStore<EntityWeb>();

	private Dialog entityDesignDialog = null;
	private String addOrEditOrDeleteMode = "";

		private TextField<String> entityNameEdit = new TextField<String>();
		private TextField<String> entityDisplayNameEdit = new TextField<String>();
		private SpinnerField versionSpin = new SpinnerField();
		private TextArea entityDescriptionEdit = new TextArea();

		private Grid<EntityAttributeWeb> attributeGrid;
		private ListStore<EntityAttributeWeb> attributeStore = new GroupingStore<EntityAttributeWeb>();

	private Dialog manageGroupDialog = null;
		private Grid<EntityAttributeGroupWeb> groupGrid;
		private ListStore<EntityAttributeGroupWeb> groupStore = new GroupingStore<EntityAttributeGroupWeb>();
		private ListStore<EntityAttributeGroupWeb> editedGroups = new GroupingStore<EntityAttributeGroupWeb>();

		private Dialog groupDialog = null;
		private String addOrEditGroupMode = "";
		private TextField<String> groupNameEdit = new TextField<String>();
		private TextField<String> groupDisplayNameEdit = new TextField<String>();
		private NumberField groupDisplayOrderEdit = new NumberField();

	private Dialog attributeDialog = null;
	private String addOrEditAttributeMode = "";
		private ComboBox<EntityAttributeGroupWeb> groupCombo;
		private TextField<String> attributeNameEdit = new TextField<String>();
		private TextField<String> attributeDisplayNameEdit = new TextField<String>();
		private TextField<String> attributeDescriptionEdit = new TextField<String>();
		// private Slider attributeDispayOrderSlider;
	    private CheckBox attributeIndexedCheckBox;

		private ListStore<EntityAttributeDatatypeWeb> attributeDataTypeStore = new ListStore<EntityAttributeDatatypeWeb>();
		private ComboBox<EntityAttributeDatatypeWeb> attributeDataTypeCombo = new ComboBox<EntityAttributeDatatypeWeb>();


		private Grid<EntityAttributeValidationWeb> validationGrid;
		private ListStore<EntityAttributeValidationWeb> validationStore = new GroupingStore<EntityAttributeValidationWeb>();

	private Dialog validationDialog = null;
	private String addOrEditValidationMode = "";
			private String validationRuleName = "";
			private ComboBox<EntityValidationRuleWeb> validationRuleCombo;
			private ListStore<EntityValidationRuleWeb> validationRuleStore = new GroupingStore<EntityValidationRuleWeb>();

			private ComboBox<ModelPropertyWeb> validationParameterNameCombo;
			private ListStore<ModelPropertyWeb> validationParameterNameStore = new GroupingStore<ModelPropertyWeb>();

			private Grid<EntityAttributeValidationParameterWeb> validationParameterGrid;
			private ListStore<EntityAttributeValidationParameterWeb> validationParameterStore = new GroupingStore<EntityAttributeValidationParameterWeb>();

	private Dialog importDialog = null;
    private ListStore<UserFileWeb> importFileStore;
    private Grid<UserFileWeb> importFileGrid;
    private TextField<String> name;
    private FileUploadField importFile;

	private boolean entitiesReceived = false;
	private EntityWeb editedEntity;
	private EntityAttributeGroupWeb editedGroup;
	private EntityAttributeWeb editedAttribute;
	private EntityAttributeValidationWeb editedValidation;

	private LayoutContainer container;



	@SuppressWarnings("unchecked")
	public EntityAttributeDesignView(Controller controller) {
		super(controller);
	}

    static final Comparator<EntityAttributeWeb> DISPLAY_ORDER = new Comparator<EntityAttributeWeb>() {
    	public int compare(EntityAttributeWeb ea1, EntityAttributeWeb ea2) {
    			   int compareValue = 0;
    		       if (ea2.getDisplayOrder() < ea1.getDisplayOrder()) {
    		    	   compareValue = 1;
    		       }
    		       if (ea2.getDisplayOrder() > ea1.getDisplayOrder()) {
    		    	   compareValue = -1;
    		       }
    		       return compareValue;
    	}
    };

	@SuppressWarnings("unchecked")
	@Override
	protected void handleEvent(AppEvent event) {
		if (event.getType() == AppEvents.EntityAttributeView) {

			initUI();

			if (Registry.get(Constants.ENTITY_ATTRIBUTE_DATA_TYPES) != null ) {	
				List<EntityAttributeDatatypeWeb> dataTypes =  (List<EntityAttributeDatatypeWeb>)Registry.get(Constants.ENTITY_ATTRIBUTE_DATA_TYPES);
				// for (EntityAttributeDatatypeWeb type : dataTypes) {
				// 	Info.display("Information", "Data Types: "+ type.getDataTypeCd() + ", " + type.getName());			
				// }
				attributeDataTypeStore.removeAll();		
				attributeDataTypeStore.add(dataTypes);
			}

			if (Registry.get(Constants.ENTITY_VALIDATION_RULES) != null ) {	
				List<EntityValidationRuleWeb> rules =  (List<EntityValidationRuleWeb>)Registry.get(Constants.ENTITY_VALIDATION_RULES);
				// for (EntityValidationRuleWeb rule : rules) {
				// 	Info.display("Information", "Validation Rule: " + rule.getValidationRuleName() +", "+rule.getValidationRuleDisplayName());
				// }
				validationRuleStore.removeAll();
				validationRuleStore.add(rules);
			}

			entitiesReceived = false;

		} else if (event.getType() == AppEvents.EntitiesReceived) {

			// Info.display("Information", "ReportReceived.");
			store.removeAll();
			groupStore.removeAll();
			editedGroups.removeAll();
			attributeStore.removeAll();
			validationStore.removeAll();
			validationParameterStore.removeAll();

			List<EntityWeb> entities = (List<EntityWeb>) event.getData();
			store.add(entities);

			entitiesReceived = true;

			EntityWeb currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
			if (currentEntity != null) {

				// Info.display("Current Entity: ", currentEntity.getName()+currentEntity.getVersionId());
	        	for (EntityWeb field : grid.getStore().getModels()) {
	        		 if (field.getName() == currentEntity.getName() && field.getVersionId() == currentEntity.getVersionId()) {
	     				grid.getSelectionModel().select(field, true);
	        		 }
	        	}

			} else {
				// Info.display("Entity: ","null");
				grid.getSelectionModel().select(0, true);
			}

		} else if (event.getType() == AppEvents.EntityAddComplete) {

			EntityWeb entity = event.getData();
	      	store.add(entity);
	        MessageBox.alert("Information", "The entity was successfully added", null);

	        entityDesignDialog.close();

		} else if (event.getType() == AppEvents.EntityUpdateComplete) {
//			store.remove(editedEntity);
//			EntityWeb entity = event.getData();
//	      	store.add(entity);

			int index = store.indexOf(editedEntity);
			store.remove(editedEntity);
			EntityWeb entity = event.getData();
			store.insert(entity, index);

			// edited entity is selected entity
			grid.getSelectionModel().select(entity, true);


    		//for( EntityAttributeWeb entityAttribute : entity.getAttributes()) {
    		//	Info.display("Entity Attribute:", entityAttribute.getName());
     		//}

	        MessageBox.alert("Information", "The entity was successfully updated", null);

	        entityDesignDialog.close();

		} else if (event.getType() == AppEvents.EntityDeleteComplete) {

        	store.remove(editedEntity);

        	// selected entity is deleted
		    Registry.register(Constants.ENTITY_ATTRIBUTE_MODEL, null);
			Registry.register(Constants.ENTITY_SEARCH_ADVANCED, null);
			Registry.register(Constants.ENTITY_SEARCH_BASIC, null);

	        MessageBox.alert("Information", "The entity was successfully deleted", null);

	        entityDesignDialog.close();

		} else if (event.getType() == AppEvents.CustomFieldsConfigurationReceived) {

			EntityWeb entity = event.getData();
			setEntityAttribureNamesToRegistry(entity);

        } else if (event.getType() == AppEvents.FileListRender) {

            displayImportFileRecords((List<UserFileWeb>) event.getData());

            showDefaultCursor();

        } else if (event.getType() == AppEvents.FileEntryImportSuccess) {

            showDefaultCursor();

            String message = event.getData();
            MessageBox.alert("Information", "" + message, listenSuccessMsg);

		} else if (event.getType() == AppEvents.Logout) {
			// Info.display("Information", "Entity Attribute View Logout.");

			if (entityDesignDialog.isVisible()) {
				entityDesignDialog.close();
			}

  		    Dispatcher.get().dispatch(AppEvents.Logout);

		} else if (event.getType() == AppEvents.Error) {
			String message = event.getData();
	        MessageBox.alert("Information", "Failure: " + message, null);

			if (entityDesignDialog.isVisible()) {
				entityDesignDialog.close();
			}
		}
	}

    final Listener<MessageBoxEvent> listenSuccessMsg = new Listener<MessageBoxEvent>()
    {
        public void handleEvent(MessageBoxEvent ce) {
            Button btn = ce.getButtonClicked();
            if (btn.getText().equals("OK")) {
                if (importDialog.isVisible()) {
                    importDialog.close();  

                    controller.handleEvent(new AppEvent(AppEvents.EntitiesRequest));
                }
            }
        }
    };

	private void displayImportFileRecords(List<UserFileWeb> userFiles) {
	    importFileStore.removeAll();
	    importFileStore.add(userFiles);
	}

	private void setEntityAttribureNamesToRegistry(EntityWeb entity) {
		if (entity == null) {
			Registry.register(Constants.PERSON_MODEL_ATTRIBUTE_NAMES, null);
			Registry.register(Constants.PERSON_MODEL_ALL_ATTRIBUTE_NAMES, null);
			return;
		}

        List<ModelPropertyWeb> personModelAttributeNames = new ArrayList<ModelPropertyWeb>();
        List<ModelPropertyWeb> personModelAllAttributeNames = new ArrayList<ModelPropertyWeb>();

        // Get attribute names from entity model
        if (entity.getAttributes() != null) {
            for (EntityAttributeWeb entityAttribute : entity.getAttributes()) {

                String name = entityAttribute.getName();
                personModelAttributeNames.add(new ModelPropertyWeb(name, Utility.convertToDescription(name)));
                personModelAllAttributeNames.add(new ModelPropertyWeb(name, Utility.convertToDescription(name)));
            }
        }
        Registry.register(Constants.PERSON_MODEL_ATTRIBUTE_NAMES, personModelAttributeNames);

        // Get custom field names
		List<ModelPropertyWeb> customFieldNames = (List<ModelPropertyWeb>) Registry.get(Constants.PERSON_MODEL_CUSTOM_FIELD_NAMES);
		if (customFieldNames != null) {
	  		for (ModelPropertyWeb customFieldName : customFieldNames) {
				String name = customFieldName.getName();
				personModelAllAttributeNames.add(new ModelPropertyWeb(name, Utility.convertToDescription(name)));
	  		}
		}
		Registry.register(Constants.PERSON_MODEL_ALL_ATTRIBUTE_NAMES, personModelAllAttributeNames);
	}

	private void initUI() {
		long time = new java.util.Date().getTime();
		GWT.log("Initializing the UI ", null);

		controller.handleEvent(new AppEvent(AppEvents.EntitiesRequest));

		buildEntityDesignDialog();

		container = new LayoutContainer();
		container.setLayout(new CenterLayout());

		CheckBoxSelectionModel<EntityWeb> sm = new CheckBoxSelectionModel<EntityWeb>();
		sm.setSelectionMode(SelectionMode.SINGLE);

		sm.addListener(Events.SelectionChange, new Listener<SelectionChangedEvent<EntityWeb>>(){
			public void handleEvent(SelectionChangedEvent<EntityWeb> sce)
			{
				if (!entitiesReceived) {
					return;
				}

				EntityWeb currentEntity = sce.getSelectedItem();

				if (currentEntity != null) {
					/*Info.display("Entity: ", currentEntity.getDisplayName());
			   		for( EntityAttributeWeb entityAttribute : currentEntity.getAttributes()) {
		    			Info.display("Entity Attribute:", entityAttribute.getDisplayName());
		     		}*/


					EntityWeb previousEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
					if (previousEntity != null) {

						// switch the entity model
						if (previousEntity.getName() != currentEntity.getName() || previousEntity.getVersionId() != currentEntity.getVersionId()) {
							// clean the search criteria
							Registry.register(Constants.ENTITY_SEARCH_ADVANCED, null);
							Registry.register(Constants.ENTITY_SEARCH_BASIC, null);
						}
					}
					// Info.display("Entity: ", currentEntity.getName()+currentEntity.getVersionId());
					Registry.register(Constants.ENTITY_ATTRIBUTE_MODEL, currentEntity);

					// setEntityAttribureNamesToRegistry(currentEntity);
					controller.handleEvent(new AppEvent(AppEvents.CustomFieldsConfigurationRequest, currentEntity));

				}  else {
					// Info.display("Entity: ","null");
//				    Registry.register(Constants.ENTITY_ATTRIBUTE_MODEL, null);
//					setEntityAttribureNamesToRegistry(null);

				    // Force at least to select a entity model
				    List<EntityWeb> selections = sce.getSelection();
				    if (selections.isEmpty()) {
				        currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
				        grid.getSelectionModel().select(currentEntity, true);
				    }
				}
			}});

		ColumnConfig entityName = new ColumnConfig("name", "Name", 180);
		ColumnConfig entityDisplayName = new ColumnConfig("displayName", "Display Name", 180);
		ColumnConfig entityVersion = new ColumnConfig("versionId", "version", 120);
		ColumnConfig entityDescription = new ColumnConfig("description", "Description", 260);
		//ColumnConfig entityDateCreated = new ColumnConfig("dateCreated", "Date Created", 180);
		//ColumnConfig entityCreatedBy = new ColumnConfig("created By", "Created By", 160);
		List<ColumnConfig> config = new ArrayList<ColumnConfig>();
		config.add(sm.getColumn());
		config.add(entityName);
		config.add(entityDisplayName);
		config.add(entityVersion);
		config.add(entityDescription);
		//config.add(entityDateCreated);
		//config.add(entityCreatedBy);

		final ColumnModel cm = new ColumnModel(config);
		grid = new Grid<EntityWeb>(store, cm);
		grid.setSelectionModel(sm);
		grid.addPlugin(sm);
		grid.setBorders(true);

		ContentPanel cp = new ContentPanel();
		cp.setHeading("Entity Attribute Design");
		cp.setFrame(true);
		cp.setIcon(IconHelper.create("images/entity.png"));
		cp.setLayout(new FillLayout());
		cp.setSize(900, 350);

		ToolBar toolBar = new ToolBar();
		toolBar.add(new Button(" Add Entity ", IconHelper.create("images/entity_add.png"), new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  // Make sure we are starting with a clean slate
	        	  entityDesignDialog.setIcon(IconHelper.create("images/entity_add.png"));
	        	  entityDesignDialog.setHeading("Add Entity Design");
				  Button ok = entityDesignDialog.getButtonById("ok");
				  ok.setText("Add Entity");

				  addOrEditOrDeleteMode = ADD_ENTITY;
				  entityDesignDialog.show();

				  readOnlyFields(false);
	      		  entityNameEdit.clear();
	      		  entityDisplayNameEdit.clear();
	      		  entityDescriptionEdit.clear();

	      		  groupStore.removeAll();
	      		  editedGroups.removeAll();
	      		  attributeStore.removeAll();

	      		  editedEntity = new EntityWeb();
	      		  editedEntity.setVersionId(1);

	      		  versionSpin.hide();
	          }
	    }));

		toolBar.add(new SeparatorToolItem());
		toolBar.add(new Button(" Edit Entity ", IconHelper.create("images/entity_edit.png"), new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  entityDesignDialog.setIcon(IconHelper.create("images/entity_edit.png"));
	        	  entityDesignDialog.setHeading("Edit Entity Design");
				  Button ok = entityDesignDialog.getButtonById("ok");
				  ok.setText("Update Entity");

				  EntityWeb editEntity = grid.getSelectionModel().getSelectedItem();
				  if (editEntity == null) {
						Info.display("Information", "You must first select a field to be edited before pressing the \"Edit Entity\" button.");
						return;
				  }
				  addOrEditOrDeleteMode = EDIT_ENTITY;
				  entityDesignDialog.show();

				  editedEntity = editEntity;

				  readOnlyFields(false);
	      		  entityNameEdit.setValue(editEntity.getName());
	      		  entityDisplayNameEdit.setValue(editEntity.getDisplayName());
	      		  if (!versionSpin.isVisible()) {
	      			  versionSpin.show();
	      		  }

		      	  versionSpin.setMinValue(editEntity.getVersionId());
		    	  versionSpin.setMaxValue(editEntity.getVersionId() + 1);
	      		  versionSpin.setValue(editEntity.getVersionId());
	      		  entityDescriptionEdit.setValue(editEntity.getDescription());

	      		  groupStore.removeAll();
	      		  editedGroups.removeAll();
	      		  if (editEntity.getEntityAttributeGroups() != null) {
		      		  for (EntityAttributeGroupWeb entityGroup : editEntity.getEntityAttributeGroups()) {
		      			  	// Info.display("Entity Group:", entityGroup.getName());
			    			groupStore.add(entityGroup);
		      		  }
	      		  }

	      		  attributeStore.removeAll();
	      		  if (editEntity.getAttributes() != null ) {

	      			  List<EntityAttributeWeb> sortedEntityAttributes = new ArrayList<EntityAttributeWeb>(editEntity.getAttributes().size());
		      		  for (EntityAttributeWeb entityAttribute : editEntity.getAttributes()) {
			      		   sortedEntityAttributes.add(entityAttribute);
		      		  }
		      		  // sort by display order
	      			  Collections.sort(sortedEntityAttributes, DISPLAY_ORDER);
	      			  
//		      		  for( EntityAttributeWeb entityAttribute : editEntity.getAttributes()) {
			      	  for( EntityAttributeWeb entityAttribute : sortedEntityAttributes) {
			      			  
			      		    // for( EntityAttributeValidationWeb validation : entityAttribute.getEntityAttributeValidations()) {
				      	    //      Info.display("Attribute Validation: ", validation.getEntityAttributeValidationId()+"; "+validation.getValidationName());	
			      		    // }
		      			   // Info.display("Entity Attribute:", entityAttribute.getName());	
			      		   attributeStore.add(entityAttribute);			
		      		  }		
	      		  }
	      		  attributeGrid.getSelectionModel().select(0, true);
	      		  attributeGrid.getSelectionModel().deselect(0);	   
	      		  
	          }
	    }));
		
		toolBar.add(new SeparatorToolItem());
		toolBar.add(new Button(" Remove Entity ", IconHelper.create("images/entity_delete.png"), new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  entityDesignDialog.setIcon(IconHelper.create("images/entity_delete.png"));
	        	  entityDesignDialog.setHeading("Delete Entity Design");	      		  
				  Button ok = entityDesignDialog.getButtonById("ok");
				  ok.setText("Delete Entity");
				  
				  EntityWeb removeEntity = grid.getSelectionModel().getSelectedItem();
				  if (removeEntity == null) {
						Info.display("Information", "You must first select a field before pressing the \"Remove Entity\" button.");
						return;
				  }					  
				  addOrEditOrDeleteMode = DELETE_ENTITY;
				  entityDesignDialog.show();	 
				  
				  editedEntity = removeEntity;
				  
				  readOnlyFields(true);	

	      		  entityNameEdit.setValue(removeEntity.getName());
	      		  entityDisplayNameEdit.setValue(removeEntity.getDisplayName());
	      		  if( !versionSpin.isVisible() ) {
	      			  versionSpin.show();
	      		  }
	      		  
		      	  versionSpin.setMinValue(removeEntity.getVersionId());
		    	  versionSpin.setMaxValue(removeEntity.getVersionId()+1);
	      		  versionSpin.setValue(removeEntity.getVersionId());
	      		  entityDescriptionEdit.setValue(removeEntity.getDescription());

	      		  groupStore.removeAll();
	      		  editedGroups.removeAll();
	      		  if( removeEntity.getEntityAttributeGroups() != null) {	   
		      		  for( EntityAttributeGroupWeb entityGroup : removeEntity.getEntityAttributeGroups()) {	      			  
			    			// Info.display("Entity Group:", entityGroup.getName());	
			    			groupStore.add(entityGroup);			
		      		  }		
	      		  }

	      		  attributeStore.removeAll();
	      		  if( removeEntity.getAttributes() != null ) {	
	      			  
	      			  List<EntityAttributeWeb> sortedEntityAttributes = new ArrayList<EntityAttributeWeb>(removeEntity.getAttributes().size());	      			  
		      		  for( EntityAttributeWeb entityAttribute : removeEntity.getAttributes()) {
			      		   sortedEntityAttributes.add(entityAttribute);
		      		  }	
		      		  // sort by display order
	      			  Collections.sort(sortedEntityAttributes, DISPLAY_ORDER);
	      			  
//		      		  for( EntityAttributeWeb entityAttribute : removeEntity.getAttributes()) {
		      		  for( EntityAttributeWeb entityAttribute : sortedEntityAttributes) {
			      		   attributeStore.add(entityAttribute);			
		      		  }		
	      		  }
	      		  attributeGrid.getSelectionModel().select(0, true);
	      		  attributeGrid.getSelectionModel().deselect(0);	   
	          }
	    }));

        toolBar.add(new SeparatorToolItem());
        toolBar.add(new Button(" Import Entity ", IconHelper.create("images/import.png"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {

                buildImportDialog();               
                importDialog.show();
                
                controller.handleEvent(new AppEvent(AppEvents.FileListUpdate, null));                
                name.clear();
                importFile.clear();
            }
        }));

        toolBar.add(new SeparatorToolItem());
        toolBar.add(new Button(" Export Entity ", IconHelper.create("images/export.png"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                
                EntityWeb removeEntity = grid.getSelectionModel().getSelectedItem();
                if (removeEntity == null) {
                      Info.display("Information", "You must first select a field before pressing the \"Export Entity\" button.");
                      return;
                }                   
            }
        }));
        
		cp.setTopComponent(toolBar);		
		cp.add(grid);

		container.add(cp);

		LayoutContainer wrapper = (LayoutContainer) Registry.get(Constants.CENTER_PANEL);
		wrapper.removeAll();
		wrapper.add(container);
		wrapper.layout();
		GWT.log("Done Initializing the UI in " + (new java.util.Date().getTime()-time), null);
	}
 
	private void readOnlyFields(boolean enable) {
		  entityNameEdit.setReadOnly(enable);
		  entityDisplayNameEdit.setReadOnly(enable);
		  versionSpin.setReadOnly(enable);
		  entityDescriptionEdit.setReadOnly(enable);
	}
	
	final Listener<MessageBoxEvent> listenConfirmDelete = new Listener<MessageBoxEvent>() {  
        public void handleEvent(MessageBoxEvent ce) {  
          Button btn = ce.getButtonClicked();  
          if( btn.getText().equals("Yes")) {
        	  
			  controller.handleEvent(new AppEvent(AppEvents.EntityDelete, editedEntity));	
          }
        }  
	};

	// Manage Group Dialog
	private void buildManageGroupDialog() {		
		if(manageGroupDialog != null)
			return;
		
		manageGroupDialog = new Dialog();
		manageGroupDialog.setBodyBorder(false);
		manageGroupDialog.setWidth(510);
		manageGroupDialog.setHeight(365);
		manageGroupDialog.setButtons(Dialog.OKCANCEL);
		manageGroupDialog.setModal(true);
		manageGroupDialog.getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  
	        	  	// added or edited groups already in the groupStore
	        	    manageGroupDialog.hide();	        	  	        	  
	          }
	    });
		
		manageGroupDialog.getButtonById(Dialog.CANCEL).addSelectionListener(new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  
	        	  // retrieve the groups from previous managed groups
	        	  groupStore.removeAll();	        	  
	        	  for (int i=0;i<editedGroups.getCount();i++){
		    			EntityAttributeGroupWeb group = editedGroups.getAt(i);	
		    			groupStore.add(group);
	        	  }
										
	        	  manageGroupDialog.hide();
	          }
	    });
		
		ContentPanel cp = new ContentPanel();
		cp.setHeading("Manage Group");
		cp.setFrame(true);
		cp.setIcon(IconHelper.create("images/basket.png"));
			FormLayout formLayout = new FormLayout();
			formLayout.setLabelWidth(120);
			formLayout.setDefaultWidth(260);
		cp.setLayout(formLayout);
		cp.setSize(500, 300);
			
		LayoutContainer formContainer = new LayoutContainer();
			ColumnLayout columnLayout = new ColumnLayout();
			formContainer.setLayout(columnLayout);  
			formContainer.add( setupGroupPanel(""), new ColumnData(1.0));			
			
		cp.add( formContainer );
	
		manageGroupDialog.add(cp);
	}
	
	// Entity Design Dialog
	private void buildEntityDesignDialog() {		
		if(entityDesignDialog != null)
			return;
		
		entityDesignDialog = new Dialog();
		entityDesignDialog.setBodyBorder(false);
		entityDesignDialog.setWidth(960);
		entityDesignDialog.setHeight(565);
		entityDesignDialog.setButtons(Dialog.OKCANCEL);
		entityDesignDialog.setModal(true);
		entityDesignDialog.getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  	        	  
				if (addOrEditOrDeleteMode.equals(ADD_ENTITY)) {  // Add	
					
					if ( !entityNameEdit.isValid() || !entityDisplayNameEdit.isValid()) {
						 Info.display("Add Entity:", "Invalid fields");	
						 return;
					}
												
					// check duplicate entity name
					for (int i=0;i<store.getCount();i++){
						EntityWeb entity = store.getAt(i);
					     if( entityNameEdit.getValue().equals(entity.getName())) {						    	 						    	 
					    	 entityNameEdit.markInvalid("Duplicate entity name");
					    	 return;
					     }
					}

					EntityWeb addingEntity = copyEntityFromGUI( new EntityWeb() );
							
					controller.handleEvent(new AppEvent(AppEvents.EntityAdd, addingEntity));		
									
				} else if (addOrEditOrDeleteMode.equals(EDIT_ENTITY)) { // Edit
					if ( !entityNameEdit.isValid() || !entityDisplayNameEdit.isValid() ) {
						 Info.display("Edit Entity:", "Invalid fields");	
						 return;
					}
					
					// check duplicate entity name with version for editing
					for (int i=0;i<store.getCount();i++){
						 EntityWeb entity = store.getAt(i);
			        	 if( entity.getName() != editedEntity.getName() ) { // not itself
						     if( entityNameEdit.getValue().equals(entity.getName()) && versionSpin.getValue().intValue() == entity.getVersionId().intValue()) {							    	 
						    	 entityNameEdit.markInvalid("Duplicate entity name in the same version");
						    	 versionSpin.markInvalid("Duplicate entity name in the same version");
						    	 return;
						     }
			        	 }
					}
					
					EntityWeb updatingEntity = copyEntityFromGUI( editedEntity );
					
					
					if( editedEntity.getVersionId() == updatingEntity.getVersionId()) {
						controller.handleEvent(new AppEvent(AppEvents.EntityUpdate, updatingEntity));
					} else {
						// if version increase means to add a new entity definition
						updatingEntity.setEntityVersionId(null);
						for (EntityAttributeGroupWeb group : updatingEntity.getEntityAttributeGroups()) {
							group.setEntityGroupId(null);
						}
						for (EntityAttributeWeb attribute : updatingEntity.getAttributes()) {
							 attribute.setEntityAttributeId(null);
							 for (EntityAttributeValidationWeb validation : attribute.getEntityAttributeValidations()) {		
								 validation.setEntityAttributeValidationId(null);
								 for (EntityAttributeValidationParameterWeb parameter : validation.getValidationParameters()) {		
									 parameter.setEntityAttributeValidationParamId(null);
								 }							
							 }							
						}		
						controller.handleEvent(new AppEvent(AppEvents.EntityAdd, updatingEntity));
					}
					
																							
				} else if (addOrEditOrDeleteMode.equals(DELETE_ENTITY)) { // Delete
					
	        	  	MessageBox.confirm("Confirm", "Delete operation cannot be undone. Are you sure you want to delete this entity?", listenConfirmDelete); 		
	        	  	return;										
				}				
	          }
	    });
		
		entityDesignDialog.getButtonById(Dialog.CANCEL).addSelectionListener(new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  entityDesignDialog.hide();
	          }
	    });
		
		ContentPanel cp = new ContentPanel();
		cp.setHeading("Entity Design");
		cp.setFrame(true);
		cp.setIcon(IconHelper.create("images/entity.png"));
		// cp.setLayout(new FillLayout());
			FormLayout formLayout = new FormLayout();
			formLayout.setLabelWidth(120);
			formLayout.setDefaultWidth(260);
		cp.setLayout(formLayout);
		cp.setSize(950, 510);
		
		entityNameEdit.setFieldLabel("Name");
		entityNameEdit.setAllowBlank(false);
		entityNameEdit.setRegex(InputFormat.ONE_WORD);
		entityNameEdit.getMessages().setRegexText("Should be one word");
		
		entityDisplayNameEdit.setFieldLabel("Display Name");
		entityDisplayNameEdit.setAllowBlank(false);

		ContentPanel spinPanel = new ContentPanel();
		spinPanel.setFrame(false);
		spinPanel.setHeaderVisible(false);
		FormLayout formLayoutAdvan = new FormLayout();
		formLayoutAdvan.setLabelWidth(120);
		formLayoutAdvan.setDefaultWidth(60);
		spinPanel.setLayout(formLayoutAdvan);
		
		versionSpin.setFieldLabel("Version");
		versionSpin.setAllowDecimals(false);
		versionSpin.setAllowNegative(false);
		versionSpin.setAllowBlank(false);
		versionSpin.setWidth(20);		
		spinPanel.add(versionSpin);
		
		entityDescriptionEdit.setFieldLabel("Description");	
		entityDescriptionEdit.setHeight(60);
		
		cp.add(entityNameEdit);
		cp.add(entityDisplayNameEdit);
		cp.add(spinPanel);
		cp.add(entityDescriptionEdit);
			
		LayoutContainer formContainer = new LayoutContainer();
			ColumnLayout columnLayout = new ColumnLayout();
			formContainer.setLayout(columnLayout);  
//			formContainer.add( setupGroupPanel(""), new ColumnData(0.4));			
			formContainer.add( setupAttributePanel(""), new ColumnData(1.0));
			
		cp.add( formContainer );
	
		entityDesignDialog.add(cp);
	}
	
	private EntityWeb copyEntity(EntityWeb entity) {
		EntityWeb updatingEntity = new EntityWeb();	
		
		updatingEntity.setEntityVersionId(entity.getEntityVersionId());	
		updatingEntity.setEntityId(entity.getEntityId());
				
		updatingEntity.setDateCreated(entity.getDateCreated());	
		updatingEntity.setUserCreatedBy(entity.getUserCreatedBy());	
		
		return updatingEntity;
	}
	
	private EntityWeb copyEntityFromGUI(EntityWeb updateEntity) {	    
		EntityWeb entity = copyEntity(updateEntity);	

		entity.setName(entityNameEdit.getValue());
		entity.setDisplayName(entityDisplayNameEdit.getValue());
		if( versionSpin.isVisible() ) {
			entity.setVersionId(versionSpin.getValue().intValue());
		} else {
			entity.setVersionId(BASE_VERSION);			
		}
		entity.setDescription(entityDescriptionEdit.getValue());

		// Groups
		Set<EntityAttributeGroupWeb> egs = new HashSet<EntityAttributeGroupWeb>();
		for (int i=0;i<groupStore.getCount();i++){
			EntityAttributeGroupWeb group = groupStore.getAt(i);
			egs.add(group);
		}
		entity.setEntityAttributeGroups(egs);
		
		// Attributes
		Set<EntityAttributeWeb> eas = new HashSet<EntityAttributeWeb>();
		for (int i=0;i<attributeStore.getCount();i++){
			EntityAttributeWeb attribute = attributeStore.getAt(i);
			attribute.setDisplayOrder(i+1);
			
			// Info.display("Entity Attribute:", attribute.getName()+"; "+attribute.getDisplayOrder());				
			eas.add(attribute);
		}
		entity.setAttributes(eas);
		
		return entity;
	}

	//	Add/Edit/Delete View Group Panel
	private ContentPanel setupGroupPanel(String title) {
		ContentPanel cp = new ContentPanel(); 
		
		cp.setFrame(true);
		cp.setHeaderVisible(false);
		cp.setLayout(new FillLayout());
		cp.setSize(470, 260);
		
		ToolBar toolBar = new ToolBar();

		toolBar.add(new Button("Add Group", IconHelper.create("images/basket_add.png"), new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				if (addOrEditOrDeleteMode.equals(DELETE_ENTITY)) {
					return;					
				}
				
				buildAddGroupDialog();
				addOrEditGroupMode = ADD_ENTITY;
				  
	        	groupDialog.setIcon(IconHelper.create("images/basket_add.png"));
	        	groupDialog.setHeading("Add Attribute Group");
	        	Button ok = groupDialog.getButtonById("ok");
	        	ok.setText("Add Group");

	        	groupNameEdit.clear();
	        	groupDisplayNameEdit.clear();
	        	groupDisplayOrderEdit.clear();		
				
	        	groupDialog.show();
			}
	    }));

/*		toolBar.add(new SeparatorToolItem());
		toolBar.add(new Button("Edit Group", IconHelper.create("images/basket_edit.png"), new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				if (addOrEditOrDeleteMode.equals(DELETE_ENTITY)) {
					return;					
				}
				
				EntityAttributeGroupWeb editField = groupGrid.getSelectionModel().getSelectedItem();
	        	if (editField == null) {
	        		Info.display("Information","You must first select a field before pressing the \"Edit Group\" button.");
	        		return;
	        	}
				buildAddGroupDialog();				
	        	addOrEditGroupMode = EDIT_ENTITY;
	        	
	        	groupDialog.setHeading("Edit Attribute Group");
	        	Button ok = groupDialog.getButtonById("ok");
	        	ok.setText("Update Group");
	        	groupDialog.show();
	        	
	        	editedGroup = editField;
	        	
				groupNameEdit.setValue(editField.getName());
				groupDisplayNameEdit.setValue(editField.getDisplayName());
				groupDisplayOrderEdit.setValue(editField.getDisplayOrder());
					
			}
	    }));		
*/
		toolBar.add(new SeparatorToolItem());
		toolBar.add(new Button("Remove Group", IconHelper.create("images/basket_delete.png"), new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  if (addOrEditOrDeleteMode.equals(DELETE_ENTITY)) {
	        		  return;					
	        	  }
	        	  
	        	  EntityAttributeGroupWeb field = groupGrid.getSelectionModel().getSelectedItem();
	        	  if (field == null) {
	        		  Info.display("Information","You must first select a field before pressing the \"Remove Group\" button.");
	        		  return;
	        	  }
	        	  	
	        	  // check if group is used by attribute
	        	  for (int i=0; i<attributeStore.getCount(); i++){
					   EntityAttributeWeb entityAttribute = attributeStore.getAt(i);
					   EntityAttributeGroupWeb group = entityAttribute.getEntityAttributeGroup();
					   if( group != null )  {
					       if( group.getName().equals( field.getName()) ) {					      	  		
							   MessageBox.alert("Information", "This group is used by Attribute '"+entityAttribute.getName() +"' and cannot be removed", null); 
							   return;
						   }
					   }
	        	  }	        	  
	        	  groupGrid.getStore().remove(field);
	          }
	    }));
		cp.setTopComponent(toolBar);
		
		ColumnConfig pName = new ColumnConfig("name", "Name", 80);
		ColumnConfig pNameDisplayed = new ColumnConfig("displayName", "Display Name", 150);		
		ColumnConfig pDisplayOrder = new ColumnConfig("displayOrder", "Display Order", 100);
	      
		List<ColumnConfig> config = new ArrayList<ColumnConfig>();
		config.add(pName);
		config.add(pNameDisplayed);
		config.add(pDisplayOrder);

		

		ColumnModel cm = new ColumnModel(config);	    
		groupGrid = new Grid<EntityAttributeGroupWeb>(groupStore, cm);
		groupGrid.setStyleAttribute("borderTop", "none");
		groupGrid.setBorders(true);
		groupGrid.setStripeRows(true); 
		cp.add(groupGrid);
	
		return cp;
	}
	
	//	Add/Edit/Delete View Attribute Panel
	private ContentPanel setupAttributePanel(String title) {
		ContentPanel cp = new ContentPanel(); 
		
		cp.setFrame(true);
		cp.setHeaderVisible(false);
		cp.setLayout(new FillLayout());
		cp.setSize(470, 320);
		
		ToolBar toolBar = new ToolBar();
		toolBar.add(new Button("Manage Group", IconHelper.create("images/basket.png"), new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				
				buildManageGroupDialog();	
				
				// keep the previous managed groups info
				editedGroups.removeAll();
	    		for (int i=0;i<groupStore.getCount();i++){
	    			EntityAttributeGroupWeb group = groupStore.getAt(i);	
	    			editedGroups.add(group);
	    		}
	    		
				manageGroupDialog.show();
			}
	    }));

		
		// add some spaces
		for (int i=0;i<15;i++){
			toolBar.add(new Label(""));
		}
		
		toolBar.add(new SeparatorToolItem());		
		toolBar.add(new Button("Add Attribute", IconHelper.create("images/database_add.png"), new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				if (addOrEditOrDeleteMode.equals(DELETE_ENTITY)) {
					return;					
				}
				
				buildAddAttributeDialog();
				addOrEditAttributeMode = ADD_ENTITY;
	        	
	        	attributeDialog.setIcon(IconHelper.create("images/database_add.png"));
	        	attributeDialog.setHeading("Add Entity Attribute");
	        	Button ok = attributeDialog.getButtonById("ok");
	        	ok.setText("Add Attribute");
	        	  
	        	groupCombo.clear();
	        	attributeNameEdit.clear();
	        	attributeDisplayNameEdit.clear();
	        	attributeDescriptionEdit.clear();				
	        	attributeDataTypeCombo.clear();
	        	// attributeDispayOrderSlider.setValue(BASE_DISPLY_ORDER);	
			    attributeIndexedCheckBox.setValue(false);	;	
			    
			    
				validationStore.removeAll();
				
				attributeDialog.show();
			}
	    }));

		toolBar.add(new SeparatorToolItem());
		toolBar.add(new Button("Edit Attribute", IconHelper.create("images/database_edit.png"), new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				if (addOrEditOrDeleteMode.equals(DELETE_ENTITY)) {
					return;					
				}
				
	        	EntityAttributeWeb editField = attributeGrid.getSelectionModel().getSelectedItem();
	        	if (editField == null) {
	        		Info.display("Information","You must first select a field before pressing the \"Edit Attribute\" button.");
	        		return;
	        	}
	        	buildAddAttributeDialog();				
	        	addOrEditAttributeMode = EDIT_ENTITY;
	        	
	        	attributeDialog.setHeading("Edit Entity Attribute");
	        	Button ok = attributeDialog.getButtonById("ok");
	        	ok.setText("Update Attribute");
	        	attributeDialog.show();
	        	
	        	editedAttribute = editField;
	        	    
		        groupCombo.clear(); 
	        	if( editField.getEntityAttributeGroup() != null) {
					for( EntityAttributeGroupWeb group : groupStore.getModels()) {
						if(group.getName().equals(editField.getEntityAttributeGroup().getName()))
							groupCombo.setValue(group);
					}
	        	}

	        	attributeNameEdit.setValue(editField.getName());
	        	attributeDisplayNameEdit.setValue(editField.getDisplayName());
	        	attributeDescriptionEdit.setValue(editField.getDescription());	
	        	// attributeDispayOrderSlider.setValue(editField.getDisplayOrder());
	        	attributeIndexedCheckBox.setValue(editField.getIndexed());	
	        	
				for( EntityAttributeDatatypeWeb attributeType : attributeDataTypeStore.getModels()) {
					if(attributeType.getDataTypeCd().equals(editField.getDatatype().getDataTypeCd()))
						attributeDataTypeCombo.setValue(attributeType);
				}

				validationStore.removeAll();
	      		for( EntityAttributeValidationWeb attributeValidation : editField.getEntityAttributeValidations()) {
	      			 validationStore.add(attributeValidation);
	      		}
	      		
			}
	    }));
		

		toolBar.add(new SeparatorToolItem());
		toolBar.add(new Button("Remove Attribute", IconHelper.create("images/database_delete.png"), new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  if (addOrEditOrDeleteMode.equals(DELETE_ENTITY)) {
	        		  return;					
	        	  }
	        	  
	        	  EntityAttributeWeb field = attributeGrid.getSelectionModel().getSelectedItem();
	        	  if (field == null) {
	        		  Info.display("Information","You must first select a field before pressing the \"Remove Attribute\" button.");
	        		  return;
	        	  }
	        	  
	        	  attributeGrid.getStore().remove(field);
	          }
	    }));
		toolBar.add(new SeparatorToolItem());		
		
		// add some spaces
		for (int i=0;i<15;i++){
			toolBar.add(new Label(""));
		}
		
		toolBar.add(new Button("Move Up", IconHelper.create("images/arrow_up.png"), new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				
				if (attributeGrid.getStore().getCount() > 1) {
					EntityAttributeWeb field = attributeGrid.getSelectionModel().getSelectedItem();
					if (field == null) {
						Info.display("Information", "You must first select a field before pressing the \"Move Up\" button.");
						return;
					}
					
					int selectionIndex = attributeGrid.getStore().indexOf(field);
					
					// Info.display("Entity Attribute index: ", ""+selectionIndex);
					if (selectionIndex > 0) {
						attributeGrid.getStore().remove(field);
						attributeGrid.getStore().insert(field, selectionIndex - 1);
					} else {
						Info.display("Information", "Cannot move up the first field.");
					}
					
					attributeGrid.getSelectionModel().select(field, true);			
				}
			}
	    }));		
		
		toolBar.add(new Button("Move Down", IconHelper.create("images/arrow_down.png"), new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				
				if (attributeGrid.getStore().getCount() > 1) {
					EntityAttributeWeb field = attributeGrid.getSelectionModel().getSelectedItem();
					if (field == null) {
						Info.display("Information", "You must first select a field before pressing the \"Move Down\" button.");
						return;
					}
					int selectionIndex = attributeGrid.getStore().indexOf(field);
					
					// Info.display("Entity Attribute index: ", ""+selectionIndex);
					if (selectionIndex >= 0 && selectionIndex < attributeGrid.getStore().getCount() - 1) {
						attributeGrid.getStore().remove(field);
						attributeGrid.getStore().insert(field, selectionIndex + 1);
					} else {
						Info.display("Information", "Cannot move down the last field.");
					}
					
					attributeGrid.getSelectionModel().select(field, true);
				}
			}
	    }));		
		
		cp.setTopComponent(toolBar);

		ColumnConfig pGroup = new ColumnConfig("entityAttributeGroup.name", "Group", 120);
		ColumnConfig pName = new ColumnConfig("name", "Name", 150);
		ColumnConfig pNameDisplayed = new ColumnConfig("displayName", "Display Name", 150);		
		ColumnConfig pDescription = new ColumnConfig("description", "Description", 200);	
		ColumnConfig pDataType = new ColumnConfig("datatype.displayName", "Data Type", 120);		
		// ColumnConfig pDisplayOrder = new ColumnConfig("displayOrder", "Display Order", 100);
		
	    CheckColumnConfig pIndexed = new CheckColumnConfig("indexed", "Indexed", 80);
	    CellEditor checkBoxEditor = new CellEditor(new CheckBox());
	    pIndexed.setEditor(checkBoxEditor);
	    
	    // disable column sorting for Move Up and Move Down 
	    pGroup.setSortable(false);
	    pName.setSortable(false);
	    pNameDisplayed.setSortable(false);
	    pDescription.setSortable(false);
	    pDataType.setSortable(false);
	    // pDisplayOrder.setSortable(false);
	    pIndexed.setSortable(false);
	    
		List<ColumnConfig> config = new ArrayList<ColumnConfig>();
		config.add(pGroup);
		config.add(pName);
		config.add(pNameDisplayed);
		config.add(pDescription);
		config.add(pDataType);
		// config.add(pDisplayOrder);
		config.add(pIndexed);		

		ColumnModel cm = new ColumnModel(config);	    
		attributeGrid = new Grid<EntityAttributeWeb>(attributeStore, cm);
		attributeGrid.setStyleAttribute("borderTop", "none");
		attributeGrid.setBorders(true);
		attributeGrid.setStripeRows(true); 
		attributeGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);	
		attributeGrid.addPlugin(pIndexed);
		cp.add(attributeGrid);
	
		return cp;
	}
	
	//  Add Group Dialog
	private void buildAddGroupDialog() {		
		if(groupDialog != null)
			return;
		
		groupDialog = new Dialog();
		groupDialog.setBodyBorder(false);
		groupDialog.setWidth(500);
		groupDialog.setHeight(220);
		groupDialog.setIcon(IconHelper.create("images/basket_add.png"));
		groupDialog.setHeading("Add Group");
		groupDialog.setButtons(Dialog.OKCANCEL);
		groupDialog.setModal(true);
		groupDialog.getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {

					if ( !groupNameEdit.isValid() || !groupDisplayNameEdit.isValid() || !groupDisplayOrderEdit.isValid()) {
						 Info.display("Group:", "Invalid fields");	
						 return;
					}
					
					if (addOrEditGroupMode.equals(ADD_ENTITY)) {  // Add													
						// check duplicate group
						for (int i=0;i<groupStore.getCount();i++){
							EntityAttributeGroupWeb group = groupStore.getAt(i);
						     if( groupNameEdit.getValue().equals(group.getName())) {						    	 						    	 
						    	 groupNameEdit.markInvalid("Duplicate group name");
						    	 return;
						     }
						}
						
						EntityAttributeGroupWeb entityGroup = new EntityAttributeGroupWeb();
			        	entityGroup.setName(groupNameEdit.getValue());
			        	entityGroup.setDisplayName(groupDisplayNameEdit.getValue());	   
			        	entityGroup.setDisplayOrder(groupDisplayOrderEdit.getValue().intValue());	 
		        	 		        	  
			        	groupStore.add(entityGroup);
		     
					} else if (addOrEditGroupMode.equals(EDIT_ENTITY)) { // Edit	
						// check duplicate group
						for (int i=0;i<groupStore.getCount();i++){
							EntityAttributeGroupWeb group = groupStore.getAt(i);
				        	 if( group.getName() != editedGroup.getName()) {
							     if( groupNameEdit.getValue().equals(group.getName())) {						    	 						    	 
							    	 groupNameEdit.markInvalid("Duplicate group name");
							    	 return;
							     }
				        	 }
						}
						
						EntityAttributeGroupWeb entityGroup = new EntityAttributeGroupWeb();
						entityGroup.setName(groupNameEdit.getValue());
						entityGroup.setDisplayName(groupDisplayNameEdit.getValue());	   
			        	entityGroup.setDisplayOrder(groupDisplayOrderEdit.getValue().intValue());
			        	
			    		 // check if name is changed
			        	 if( !editedGroup.getName().equals(entityGroup.getName()) ) { 
			        		 
				        	  //	  
			        	 }
			        	 
			    		 groupStore.remove(editedGroup);	
			        	 groupStore.add(entityGroup);
			        	 
					}		        	  	 
					groupDialog.hide();					        	  	 
				}
	    });
		
		groupDialog.getButtonById(Dialog.CANCEL).addSelectionListener(new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {	        	  
	        	  groupDialog.hide();
	          }
	    });
		
		ContentPanel cp = new ContentPanel();
		cp.setHeading("Group");
		cp.setFrame(true);
		cp.setIcon(IconHelper.create("images/basket.png"));
			FormLayout formLayout = new FormLayout();
			formLayout.setLabelWidth(120);
			formLayout.setDefaultWidth(260);
		cp.setLayout(formLayout);
		cp.setSize(500, 220);
		
		groupNameEdit.setFieldLabel("Name");
		groupNameEdit.setAllowBlank(false);
		groupNameEdit.setRegex(InputFormat.ONE_WORD);
		groupNameEdit.getMessages().setRegexText("Should be one word");
		groupDisplayNameEdit.setFieldLabel("Display Name");
		groupDisplayOrderEdit.setFieldLabel("Display Order");		
		groupNameEdit.setAllowBlank(false);
		groupDisplayNameEdit.setAllowBlank(false);
		groupDisplayOrderEdit.setAllowBlank(false);
		
		cp.add(groupNameEdit);
		cp.add(groupDisplayNameEdit);
		cp.add(groupDisplayOrderEdit);
		
		groupDialog.add(cp);
	}

	//	Add/Edit/Delete View Validation Parameter Panel
	private ContentPanel setupValidationParameterPanel(String title) {
		ContentPanel cp = new ContentPanel(); 
		
		cp.setFrame(true);
		cp.setHeaderVisible(false);
		cp.setLayout(new FillLayout());
		cp.setSize(525, 220);
		
		ToolBar toolBar = new ToolBar();
		validationParameterNameCombo = new ComboBox<ModelPropertyWeb>();
		validationParameterNameCombo.setEmptyText("Select a parameter...");
		validationParameterNameCombo.setForceSelection(true);
		validationParameterNameCombo.setDisplayField("name");
		validationParameterNameCombo.setWidth(150);
		validationParameterNameCombo.setStore(validationParameterNameStore);
		validationParameterNameCombo.setTypeAhead(true);
		validationParameterNameCombo.setTriggerAction(TriggerAction.ALL);
		
		toolBar.add(validationParameterNameCombo);

		// add some spaces
		toolBar.add(new Label(""));
		toolBar.add(new Label(""));
		toolBar.add(new SeparatorToolItem());
		
		toolBar.add(new Button("Add Validation Parameter", IconHelper.create("images/database_add.png"), new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
	        	  List<ModelPropertyWeb> selection = validationParameterNameCombo.getSelection();
	        	  if (selection == null || selection.size() == 0) {	        		
	        		  Info.display("Information", "Please select a field before pressing the \"Add Validation Parameter\" button.");
	        		  return;
	        	  }
	        	  
	        	  ModelPropertyWeb field = selection.get(0);

	        	  // check duplicate report parameter
	        	  if (!fieldInList(field.getName(), validationParameterGrid.getStore())) {
					  EntityAttributeValidationParameterWeb parameter = new EntityAttributeValidationParameterWeb();
					  parameter.setParameterName(field.getName());
		        	  
		        	  validationParameterGrid.getStore().add(parameter);	
		          } else {
		        	  Info.display("Add Validation Parameter:", "Selected parameter is already added to the validation"); 
	        	  }
	        	         	  
			}

			private boolean fieldInList(String name, ListStore<EntityAttributeValidationParameterWeb> listStore) {
				for (EntityAttributeValidationParameterWeb item : listStore.getModels()) {
					if (item.getParameterName().equalsIgnoreCase(name)) {
						return true;
					}
				}
				return false;
			}
			
	    }));
		
		toolBar.add(new SeparatorToolItem());
		toolBar.add(new Button("Remove Validation Parameter", IconHelper.create("images/database_delete.png"), new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  EntityAttributeValidationParameterWeb field = validationParameterGrid.getSelectionModel().getSelectedItem();
	        	  if (field == null) {
	        		  Info.display("Information","You must first select a field before pressing the \"Remove Validation Parameter\" button.");
	        		  return;
	        	  }
	        	  validationParameterGrid.getStore().remove(field);
	          }
	    }));
		cp.setTopComponent(toolBar);
		
		ColumnConfig pParameter = new ColumnConfig("parameterName", "Parameter Name", 160);
		ColumnConfig pValueParameter = new ColumnConfig("parameterValue", "Parameter Value", 200);		

		TextField<String> validationParameterText = new TextField<String>(); 
		validationParameterText.setAllowBlank(false);
		validationParameterText.setValidator(new Validator() {	    	
				public String validate(Field<?> field, String value) {	
					 return null;
				}
	    });		
		pValueParameter.setEditor(new CellEditor(validationParameterText)); 
		
		
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		configs.add(pParameter);
		configs.add(pValueParameter);
		
		ColumnModel cm = new ColumnModel(configs);
	    RowEditor<EntityAttributeValidationParameterWeb> rowEditor = new RowEditor<EntityAttributeValidationParameterWeb>(); 
	    rowEditor.setClicksToEdit(ClicksToEdit.TWO);
	    
		validationParameterGrid = new Grid<EntityAttributeValidationParameterWeb>(validationParameterStore, cm);
		validationParameterGrid.setStyleAttribute("borderTop", "none");
		validationParameterGrid.setBorders(true);
		validationParameterGrid.setStripeRows(true);
		validationParameterGrid.addPlugin(rowEditor); 
		cp.add(validationParameterGrid);
		
		return cp;
	}

	private EntityAttributeValidationWeb copyEntityAttributeValidation(EntityAttributeValidationWeb entityAttributeValidation) {
		EntityAttributeValidationWeb updatingValidation = new EntityAttributeValidationWeb();
		
		updatingValidation.setEntityAttributeValidationId(entityAttributeValidation.getEntityAttributeValidationId());			
		updatingValidation.setValidationName(entityAttributeValidation.getValidationName());   
		updatingValidation.setDisplayName(entityAttributeValidation.getDisplayName());					
		
		return updatingValidation;
	}
	
	private EntityAttributeValidationWeb copyEntityAttributeValidationFromGUI(EntityAttributeValidationWeb updateEntityAttributeValidation) {	    
		EntityAttributeValidationWeb attributeValidation = copyEntityAttributeValidation(updateEntityAttributeValidation);	
		
		attributeValidation.setValidationName(validationRuleCombo.getValue().getValidationRuleName());   
		attributeValidation.setDisplayName(validationRuleCombo.getValue().getValidationRuleDisplayName());	
		
		Set<EntityAttributeValidationParameterWeb> avps = new HashSet<EntityAttributeValidationParameterWeb>();
		for (int i=0;i<validationParameterStore.getCount();i++){
			EntityAttributeValidationParameterWeb parameter = validationParameterStore.getAt(i);
			avps.add(parameter);
		}
		attributeValidation.setValidationParameters(avps);

		return attributeValidation;
	}
	
	
	//  Add Validation Dialog
	private void buildAddValidationDialog() {		
		if(validationDialog != null)
			return;
		
		validationDialog = new Dialog();
		validationDialog.setBodyBorder(false);
		validationDialog.setWidth(550);
		validationDialog.setHeight(380);
		validationDialog.setIcon(IconHelper.create("images/validation.png"));
		validationDialog.setHeading("Add Validation");
		validationDialog.setButtons(Dialog.OKCANCEL);
		validationDialog.setModal(true);
		validationDialog.getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
					if ( validationRuleCombo.getValue() == null ) {
						 Info.display("Validation:", "Invalid fields");	
						 return;
					}	
					
					if (addOrEditValidationMode.equals(ADD_ENTITY)) {  // Add		
						// check duplicate validation
						for (int i=0;i<validationStore.getCount();i++){
							EntityAttributeValidationWeb validation = validationStore.getAt(i);
							EntityValidationRuleWeb validationRule = validationRuleCombo.getValue();
						    if( validationRule.getValidationRuleName().equals(validation.getValidationName())) {	
						    	
								Info.display("Validation:", "Duplicate validation name");
						    	validationRuleCombo.markInvalid("Duplicate validation name");
						    	return;
						    }
						}
						
/*						EntityAttributeValidationWeb validation = new EntityAttributeValidationWeb();
						validation.setValidationName(validationRuleCombo.getValue().getValidationRuleName());   
						validation.setDisplayName(validationRuleCombo.getValue().getValidationRuleDisplayName());	
						
						Set<EntityAttributeValidationParameterWeb> avps = new HashSet<EntityAttributeValidationParameterWeb>();
						for (int i=0;i<validationParameterStore.getCount();i++){
							EntityAttributeValidationParameterWeb parameter = validationParameterStore.getAt(i);
							avps.add(parameter);
						}
						validation.setValidationParameters(avps);
		        	 		        	  
			    		validationStore.add(validation);
*/		
						// check empty for Parameter Value
						for (int i=0;i<validationParameterStore.getCount();i++){
							EntityAttributeValidationParameterWeb parameter = validationParameterStore.getAt(i);
						     if( parameter.getParameterValue() == null || parameter.getParameterValue().isEmpty() ) {
							    MessageBox.alert("Information", "'Parameter Value' cannot be empty\n. " +
							    				 				"Please double click the Parameter in the list grid and add value to the field", null); 
						    	return;
						     }
						}
						
						EntityAttributeValidationWeb validation = copyEntityAttributeValidationFromGUI(new EntityAttributeValidationWeb());										
			    		validationStore.add(validation);
						
					} else if (addOrEditValidationMode.equals(EDIT_ENTITY)) { // Edit	
						 // check duplicate validation
						for (int i=0;i<validationStore.getCount();i++){
							EntityAttributeValidationWeb validation = validationStore.getAt(i);
							EntityValidationRuleWeb validationRule = validationRuleCombo.getValue();
				        	if( validation.getValidationName() != editedValidation.getValidationName()) {
							    if( validationRule.getValidationRuleName().equals(validation.getValidationName())) {	
							    	
									Info.display("Validation:", "Duplicate validation name");
							    	validationRuleCombo.markInvalid("Duplicate validation name");
							    	return;
							    }
				        	}
						}
	
/*						EntityAttributeValidationWeb validation = new EntityAttributeValidationWeb();
						validation.setValidationName(validationRuleCombo.getValue().getValidationRuleName());
						validation.setDisplayName(validationRuleCombo.getValue().getValidationRuleDisplayName());	
						
						Set<EntityAttributeValidationParameterWeb> avps = new HashSet<EntityAttributeValidationParameterWeb>();
						for (int i=0;i<validationParameterStore.getCount();i++){
							EntityAttributeValidationParameterWeb parameter = validationParameterStore.getAt(i);
							avps.add(parameter);
						}
						validation.setValidationParameters(avps);
*/
						// check empty for Parameter Value
						for (int i=0;i<validationParameterStore.getCount();i++){
							EntityAttributeValidationParameterWeb parameter = validationParameterStore.getAt(i);
						     if( parameter.getParameterValue() == null || parameter.getParameterValue().isEmpty() ) {
							    MessageBox.alert("Information", "'Parameter Value' cannot be empty\n. " +
							    				 				"Please double click the Parameter in the list grid and add value to the field", null); 
						    	return;
						     }
						}
						
						EntityAttributeValidationWeb validation = copyEntityAttributeValidationFromGUI(editedValidation);																
						validationStore.remove(editedValidation);						
						validationStore.add(validation);	
					}
					
					validationDialog.hide();
	          }
	    });
		validationDialog.getButtonById(Dialog.CANCEL).addSelectionListener(new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  
	        	  validationDialog.hide();
	          }
	    });
		
		ContentPanel cp = new ContentPanel();
		cp.setHeading("Validation");
		cp.setFrame(true);
		cp.setIcon(IconHelper.create("images/validation.png"));
			FormLayout formLayout = new FormLayout();
			formLayout.setLabelWidth(80);
			formLayout.setDefaultWidth(320);
		cp.setLayout(formLayout);
		cp.setSize(545, 310);
		
		validationRuleCombo = new ComboBox<EntityValidationRuleWeb>();
		validationRuleCombo.setEmptyText("Select a validation...");
		validationRuleCombo.setForceSelection(true);
		validationRuleCombo.setDisplayField("displayName");
		validationRuleCombo.setToolTip("Validation Rule");
		validationRuleCombo.setWidth(150);
		validationRuleCombo.setStore(validationRuleStore);
		validationRuleCombo.setTypeAhead(true);
		validationRuleCombo.setTriggerAction(TriggerAction.ALL);
		validationRuleCombo.setFieldLabel("Validation");	

		// Select change
		validationRuleCombo.addListener(Events.SelectionChange, new Listener<SelectionChangedEvent<EntityValidationRuleWeb>>(){
			public void handleEvent(SelectionChangedEvent<EntityValidationRuleWeb> sce)
			{
				EntityValidationRuleWeb item = sce.getSelectedItem();
        		
				// Fill the parameter names to the combo
	        	String ruleName = item.getValidationRuleName();

	        	if( !validationRuleName.equals(ruleName)) {	  

	        		validationRuleName = ruleName;      				        			        	
	        		
	        		// reset the ParameterNameCombo
	        		validationParameterNameCombo.clear();
	        		validationParameterNameStore.removeAll();	        		
					for (String name: item.getParameters()){					
						ModelPropertyWeb parameterName = new ModelPropertyWeb(name);
						validationParameterNameStore.add(parameterName);
					}						
					  
					// remove the parameters because validation rule changed
					validationParameterStore.removeAll();
	        	}
			}});
	
		
		cp.add(validationRuleCombo);
		
		cp.add(setupValidationParameterPanel(""));
	
		validationDialog.add(cp);
	}
	
	// Add/Edit/Delete View Validation Panel 
	private ContentPanel setupValidatePanel(String title) {
		ContentPanel cp = new ContentPanel(); 
		
		cp.setFrame(true);
		cp.setHeaderVisible(false);
		cp.setLayout(new FillLayout());
		cp.setSize(460, 185);
		
		ToolBar toolBar = new ToolBar();
		
		toolBar.add(new Button("Add Validation", IconHelper.create("images/validation_add.png"), new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
	        	if (addOrEditOrDeleteMode.equals(DELETE_ENTITY)) {
	        		return;					
	        	}
	        	
				buildAddValidationDialog();	
	        	addOrEditValidationMode = ADD_ENTITY;
	        	
				validationDialog.setIcon(IconHelper.create("images/validation_add.png"));
		        validationDialog.setHeading("Add Attribute Validation");
		        Button ok = validationDialog.getButtonById("ok");
		        ok.setText("Add Validation");
		        
		        validationRuleName = "";
			    validationRuleCombo.clearSelections();
		        
				validationParameterNameCombo.clear();
				validationParameterNameStore.removeAll();
				
				validationParameterStore.removeAll();
								
		        validationDialog.show();
				
			}
	    }));
		
		toolBar.add(new SeparatorToolItem());
		toolBar.add(new Button("Edit Validation", IconHelper.create("images/validation_edit.png"), new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
	        	  if (addOrEditOrDeleteMode.equals(DELETE_ENTITY)) {
	        		  return;					
	        	  }				
	        	  EntityAttributeValidationWeb editField = validationGrid.getSelectionModel().getSelectedItem();
	        	  if (editField == null) {
	        		  Info.display("Information","You must first select a field before pressing the \"Edit Validation\" button.");
	        		  return;
	        	  }
	        	  
				  buildAddValidationDialog();	
		          addOrEditValidationMode = EDIT_ENTITY;

		          validationDialog.setIcon(IconHelper.create("images/validation_edit.png"));
		          validationDialog.setHeading("Edit Attribute Validation");
			      Button ok = validationDialog.getButtonById("ok");
			      ok.setText("Update Validation");
		      
			      editedValidation = editField;	

			      validationRuleName = editField.getValidationName();
				  for( EntityValidationRuleWeb rule : validationRuleStore.getModels()) {
					  
					    // find validation rule
						if(rule.getValidationRuleName().equals(editField.getValidationName())) {
							validationRuleCombo.setValue(rule);

							// for parameter combo
			        		validationParameterNameCombo.clear();
			        		validationParameterNameStore.removeAll();	        		
							for (String name: rule.getParameters()){					
								ModelPropertyWeb parameterName = new ModelPropertyWeb(name);
								validationParameterNameStore.add(parameterName);
							}						
						}
				  }
			      				
				  // for parameter grid
				  validationParameterStore.removeAll();
		      	  for( EntityAttributeValidationParameterWeb validationParameter : editField.getValidationParameters()) {
		      		   validationParameterStore.add(validationParameter);
		      	  }	  
		      	  
			      validationDialog.show();
			}
	    }));
		
		toolBar.add(new SeparatorToolItem());
		toolBar.add(new Button("Remove Validation", IconHelper.create("images/validation_delete.png"), new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  if (addOrEditOrDeleteMode.equals(DELETE_ENTITY)) {
	        		  return;					
	        	  }
	        	  
	        	  EntityAttributeValidationWeb field = validationGrid.getSelectionModel().getSelectedItem();
	        	  if (field == null) {
	        		  Info.display("Information","You must first select a field before pressing the \"Remove Validation\" button.");
	        		  return;
	        	  }
	        	  
	        	  validationGrid.getStore().remove(field);
	          }
	    }));
		cp.setTopComponent(toolBar);
		
		ColumnConfig pName = new ColumnConfig("displayName", "Name", 220);
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		configs.add(pName);

		ColumnModel cm = new ColumnModel(configs);
		
		validationGrid = new Grid<EntityAttributeValidationWeb>(validationStore, cm);
		validationGrid.setStyleAttribute("borderTop", "none");
		validationGrid.setBorders(true);
		validationGrid.setStripeRows(true);
		cp.add(validationGrid);
		
		return cp;
	}

	private EntityAttributeWeb copyEntityAttribute(EntityAttributeWeb entityAttribute) {
		EntityAttributeWeb updaytingEntityAttribute = new EntityAttributeWeb();	
		
		updaytingEntityAttribute.setEntityAttributeId(entityAttribute.getEntityAttributeId());	
				
		updaytingEntityAttribute.setDateCreated(entityAttribute.getDateCreated());	
		updaytingEntityAttribute.setUserCreatedBy(entityAttribute.getUserCreatedBy());	
		
		return updaytingEntityAttribute;
	}
	
	private EntityAttributeWeb copyEntityAttributeFromGUI(EntityAttributeWeb updateEntityAttribute) {	    
		EntityAttributeWeb entityAttribute = copyEntityAttribute(updateEntityAttribute);	

		entityAttribute.setName(attributeNameEdit.getValue());
		entityAttribute.setDisplayName(attributeDisplayNameEdit.getValue());
		entityAttribute.setDescription(attributeDescriptionEdit.getValue());

		// entityAttribute.setDisplayOrder(attributeDispayOrderSlider.getValue());						
		entityAttribute.setIndexed(attributeIndexedCheckBox.getValue());

		// data type
		EntityAttributeDatatypeWeb attributeType = attributeDataTypeCombo.getValue();
		if (attributeType != null) {
			entityAttribute.setDatatype(attributeType);
		}

		// group
		EntityAttributeGroupWeb selectedEntityGroup = groupCombo.getValue();
		if (selectedEntityGroup != null) {				
			entityAttribute.setEntityAttributeGroup(selectedEntityGroup);
		}

		// validations
		Set<EntityAttributeValidationWeb> avp = new HashSet<EntityAttributeValidationWeb>();
		for (int i=0;i<validationStore.getCount();i++){
			EntityAttributeValidationWeb parameter = validationStore.getAt(i);
			 avp.add(parameter);
		}
		entityAttribute.setEntityAttributeValidations(avp);
		return entityAttribute;
	}
	
	//  Add Attribute Dialog
	private void buildAddAttributeDialog() {		
		if(attributeDialog != null)
			return;
		
		attributeDialog = new Dialog();
		attributeDialog.setBodyBorder(false);
		attributeDialog.setWidth(500);
		attributeDialog.setHeight(440);
		attributeDialog.setIcon(IconHelper.create("images/database_add.png"));
		attributeDialog.setHeading("Add Attribute");
		attributeDialog.setButtons(Dialog.OKCANCEL);
		attributeDialog.setModal(true);
		attributeDialog.getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {

					if ( !attributeNameEdit.isValid() || !attributeDisplayNameEdit.isValid() || !attributeDataTypeCombo.isValid()) {
						 Info.display("Entity Attribute:", "Invalid fields");	
						 return;
					}
					
					if (addOrEditAttributeMode.equals(ADD_ENTITY)) {  // Add	
						
						// check duplicate attribute
						for (int i=0;i<attributeStore.getCount();i++){
						     EntityAttributeWeb attribute = attributeStore.getAt(i);
						     if( attributeNameEdit.getValue().equals(attribute.getName())) {						    	 						    	 
						    	 attributeNameEdit.markInvalid("Duplicate attribute name");
						    	 return;
						     }
						}
						
						EntityAttributeWeb attribute = copyEntityAttributeFromGUI(new EntityAttributeWeb());										
			    		attributeStore.add(attribute);
		     
					} else if (addOrEditAttributeMode.equals(EDIT_ENTITY)) { // Edit	

						// check duplicate attribute parameter
						for (int i=0;i<attributeStore.getCount();i++){
							EntityAttributeWeb attribute = attributeStore.getAt(i);
				        	 if( attribute.getName() != editedAttribute.getName()) {
							     if( attributeNameEdit.getValue().equals(attribute.getName())) {						    	 						    	 
							    	 attributeNameEdit.markInvalid("Duplicate attribute name");
							    	 return;
							     }
				        	 }
						}
						
						EntityAttributeWeb attribute = copyEntityAttributeFromGUI(editedAttribute);									

//			        	attributeStore.remove(editedAttribute);	
//			        	attributeStore.add(attribute);			
			        	
						int index = attributeStore.indexOf(editedAttribute);
						attributeStore.remove(editedAttribute);				
						attributeStore.insert(attribute, index);	
					}
		        	  	 
					attributeDialog.hide();					        	  	 
				}
	    });
		attributeDialog.getButtonById(Dialog.CANCEL).addSelectionListener(new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {	        	  
	        	  attributeDialog.hide();
	          }
	    });
		
		ContentPanel cp = new ContentPanel();
		cp.setHeading("Attribute");
		cp.setFrame(true);
		cp.setIcon(IconHelper.create("images/database.png"));
			FormLayout formLayout = new FormLayout();
			formLayout.setLabelWidth(120);
			formLayout.setDefaultWidth(260);
		cp.setLayout(formLayout);
		cp.setSize(490, 400);

		groupCombo = new ComboBox<EntityAttributeGroupWeb>();
		groupCombo.setEmptyText("Select a group...");
		groupCombo.setForceSelection(true);
		groupCombo.setDisplayField("name");
		groupCombo.setToolTip("Attribute group");
		groupCombo.setStore(groupStore);
		groupCombo.setTypeAhead(true);
		groupCombo.setTriggerAction(TriggerAction.ALL);
		groupCombo.setFieldLabel("Attribute Group");

		attributeNameEdit.setFieldLabel("Name");
		attributeNameEdit.setAllowBlank(false);
		attributeNameEdit.setRegex(InputFormat.ONE_WORD);
		attributeNameEdit.getMessages().setRegexText("Should be one word");
		attributeDisplayNameEdit.setFieldLabel("Display Name");
		attributeDisplayNameEdit.setAllowBlank(false);
		attributeDescriptionEdit.setFieldLabel("Description");

		attributeDataTypeCombo.setEmptyText("Select data type field...");
		attributeDataTypeCombo.setForceSelection(true);
		attributeDataTypeCombo.setDisplayField("displayName");
		attributeDataTypeCombo.setStore(attributeDataTypeStore);
		attributeDataTypeCombo.setTypeAhead(true);
		attributeDataTypeCombo.setTriggerAction(TriggerAction.ALL);
		attributeDataTypeCombo.setFieldLabel("Data Type");
		attributeDataTypeCombo.setAllowBlank(false);

		// attributeDispayOrderSlider = new Slider();
		// attributeDispayOrderSlider.setMinValue(1);
		// attributeDispayOrderSlider.setMaxValue(10);
		// attributeDispayOrderSlider.setIncrement(1);
	    // SliderField sliderField = new SliderField(attributeDispayOrderSlider);
	    // sliderField.setFieldLabel("Display Order");


		attributeIndexedCheckBox = new CheckBox();
		attributeIndexedCheckBox.setBoxLabel("");
	    CheckBoxGroup checkEnableGroup = new CheckBoxGroup();
	    checkEnableGroup.setFieldLabel("Indexed");
	    checkEnableGroup.add(attributeIndexedCheckBox);

		LayoutContainer formContainer = new LayoutContainer();
		ColumnLayout columnLayout = new ColumnLayout();
		formContainer.setLayout(columnLayout);
		formContainer.add(setupValidatePanel(""), new ColumnData(1));

		cp.add(groupCombo);
		cp.add(attributeNameEdit);
		cp.add(attributeDisplayNameEdit);
		cp.add(attributeDescriptionEdit);
		cp.add(attributeDataTypeCombo);
		// cp.add(sliderField);
		cp.add(checkEnableGroup);
		cp.add(formContainer);

		attributeDialog.add(cp);
	}

    //  Import Dialog
    private void buildImportDialog() {
        if (importDialog != null) {
            return;
        }

        importDialog = new Dialog();
        importDialog.setBodyBorder(false);
        importDialog.setWidth(610);
        importDialog.setHeight(400);
        importDialog.setIcon(IconHelper.create("images/import.png"));
        importDialog.setHeading("Import Entity");
        importDialog.setButtons(Dialog.CANCEL);
        importDialog.setModal(true);

        importDialog.getButtonById(Dialog.CANCEL).addSelectionListener(new SelectionListener<ButtonEvent>() {
              @Override
              public void componentSelected(ButtonEvent ce) {
                  importDialog.hide();
              }
        });

        ContentPanel cp = new ContentPanel();
        cp.setFrame(false);
        cp.setHeaderVisible(false);
        cp.setLayout(new FillLayout());
        cp.setSize(600, 340);

        // Tool bar
        ToolBar toolBar = new ToolBar();

        toolBar.add(new Button("Import Entity", IconHelper.create("images/import.png"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                List<UserFileWeb> importFileEntries = importFileGrid.getSelectionModel().getSelectedItems();
                if (importFileEntries == null || importFileEntries.size() == 0) {
                    Info.display("Information","You must first select an entry before pressing the Import Entity button.");
                    return;
                }

                controller.handleEvent(new AppEvent(AppEvents.FileEntryImport, importFileEntries));
            }
        }));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new Button("Remove", IconHelper.create("images/folder_delete.png"), new SelectionListener<ButtonEvent>() {
              @Override
              public void componentSelected(ButtonEvent ce) {
                  List<UserFileWeb> importFileEntries = importFileGrid.getSelectionModel().getSelectedItems();
                  if (importFileEntries == null || importFileEntries.size() == 0) {
                      Info.display("Information", "You must first select an entry before pressing the Remove button.");
                      return;
                  }
                  controller.handleEvent(new AppEvent(AppEvents.FileEntryRemove, importFileEntries));
              }
        }));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new Button("Refresh", IconHelper.create("images/arrow_refresh.png"), new SelectionListener<ButtonEvent>() {
              @Override
              public void componentSelected(ButtonEvent ce) {
                      showWaitCursor();
                      controller.handleEvent(new AppEvent(AppEvents.FileListUpdate, null));
              }
        }));
        cp.setTopComponent(toolBar);

        // Grid
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig();
        column.setId("name");
        column.setHeader("File Name");
        column.setWidth(150);
        configs.add(column);

        column = new ColumnConfig("dateCreated", "Date Created", 250);
        column.setDateTimeFormat(DateTimeFormat.getFullDateTimeFormat());
        configs.add(column);

        column = new ColumnConfig("imported", "Imported?", 70);
        column.setAlignment(HorizontalAlignment.RIGHT);
        configs.add(column);

        ColumnModel cm = new ColumnModel(configs);

        importFileStore = new ListStore<UserFileWeb>();
        importFileGrid = new Grid<UserFileWeb>(importFileStore, cm);
        importFileGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        importFileGrid.setAutoExpandColumn("name");
        importFileGrid.setBorders(true);
        cp.add(importFileGrid);

        // File upload
        final FormPanel importPanel = new FormPanel();
        importPanel.setFrame(true);
        String url = GWT.getModuleBaseURL() + "upload";
        importPanel.setAction(url);
        importPanel.setEncoding(Encoding.MULTIPART);
        importPanel.setMethod(Method.POST);
        importPanel.setButtonAlign(HorizontalAlignment.CENTER);
        importPanel.setWidth(580);
        importPanel.setHeight(100);

        name = new TextField<String>();
        name.setFieldLabel("Name");
        name.setName("entity");
        name.setAllowBlank(false);
        importPanel.add(name);

        importFile = new FileUploadField();
        importFile.setAllowBlank(false);
        importFile.setFieldLabel("File");
        importFile.setName("filename");
        importPanel.add(importFile);

        importPanel.addButton(new Button("Upload", new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                if (name.getValue() == null || importFile.getValue() == null) {
                    Info.display("Information", "You must enter a name and filename before pressing the 'Upload' button");
                    return;
                }
                if (existsFileName(name.getValue())) {
                    Info.display("Information", "You already have a file with this name. Please choose another name.");
                    return;
                }

                importPanel.submit();
            }
        }));

        importPanel.addListener(Events.Submit, new Listener<FormEvent>() {
            public void handleEvent(FormEvent be) {
                GWT.log("Event is " + be, null);
                if (!be.getResultHtml().equals("success")) {
                    Info.display("Information", be.getResultHtml());
                    return;
                }

                controller.handleEvent(new AppEvent(AppEvents.FileListUpdate, be.getResultHtml()));
            }
        });

        cp.add(importPanel);

        importDialog.add(cp);
    }

    protected boolean existsFileName(String value) {
        List<UserFileWeb> fileList = importFileStore.getModels();
        for (UserFileWeb userFile : fileList) {
            if (userFile.getName().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static void showWaitCursor() {
        DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "wait");
    }

    public static void showDefaultCursor() {
        DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "default");
    }
}
