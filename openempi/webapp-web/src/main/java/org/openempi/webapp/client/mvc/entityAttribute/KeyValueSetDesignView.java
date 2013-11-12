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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.model.EntityKeyValuesWeb;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
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
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
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

public class KeyValueSetDesignView extends View
{
	
	private Grid<EntityKeyValuesWeb> grid;
	private GroupingStore<EntityKeyValuesWeb> store = new GroupingStore<EntityKeyValuesWeb>();
	
	private Dialog keyValueDesignDialog = null;	
	
		private TextField<String> keyNameEdit = new TextField<String>();
		private TextField<String> keyValueEdit = new TextField<String>();
		
		private Grid<EntityKeyValuesWeb> keyValueGrid;
		private ListStore<EntityKeyValuesWeb> keyValueStore = new GroupingStore<EntityKeyValuesWeb>(); 
		
	private LayoutContainer container;
	

	
	@SuppressWarnings("unchecked")
	public KeyValueSetDesignView(Controller controller) {
		super(controller);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void handleEvent(AppEvent event) {
		if (event.getType() == AppEvents.KeyValueSetView) {
			initUI();
		} else if (event.getType() == AppEvents.Logout) {
			// Info.display("Information", "Report View Logout.");		
			
			if( keyValueDesignDialog.isVisible() )
				keyValueDesignDialog.close();
			
  		    Dispatcher.get().dispatch(AppEvents.Logout);
		} else if (event.getType() == AppEvents.Error) {			
			String message = event.getData();
	        MessageBox.alert("Information", "Failure: " + message, null);  			
		}
	}

	private void initUI() {
		long time = new java.util.Date().getTime();
		GWT.log("Initializing the UI ", null);
		
		buildKeyValueSetDesignDialog();
		
		container = new LayoutContainer();
		container.setLayout(new CenterLayout());
		
		ColumnConfig keyName = new ColumnConfig("key", "Key Name", 180);
		List<ColumnConfig> config = new ArrayList<ColumnConfig>();
		config.add(keyName);

		
		final ColumnModel cm = new ColumnModel(config);
		grid = new Grid<EntityKeyValuesWeb>(store, cm);
		grid.setBorders(true);

		ContentPanel cp = new ContentPanel();
		cp.setHeading("Key Value Set Design");
		cp.setFrame(true);
		cp.setIcon(IconHelper.create("images/key.png"));
		cp.setLayout(new FillLayout());
		cp.setSize(500, 300);

		ToolBar toolBar = new ToolBar();
		toolBar.add(new Button(" Add Key Value Set ", IconHelper.create("images/key_add.png"), new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  // Make sure we are starting with a clean slate
	        	  keyValueDesignDialog.setIcon(IconHelper.create("images/key_add.png"));
	        	  keyValueDesignDialog.setHeading("Add Key Value Set");	      	
				  Button ok = keyValueDesignDialog.getButtonById("ok");
				  ok.setText("Add Key Value Set");

				  keyValueDesignDialog.show();
	      		  
				  readOnlyFields(false);
	      		  keyNameEdit.clear();
	      		  keyValueEdit.clear();
	      		  
	      		  keyValueStore.removeAll();	      		       		  
	          }
	    }));
		
		toolBar.add(new SeparatorToolItem());
		toolBar.add(new Button(" Edit Key Value Set ", IconHelper.create("images/key_edit.png"), new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  keyValueDesignDialog.setIcon(IconHelper.create("images/key_edit.png"));
	        	  keyValueDesignDialog.setHeading("Edit Key Value Set");
				  Button ok = keyValueDesignDialog.getButtonById("ok");
				  ok.setText("Update Key Value Set");
				 
				  EntityKeyValuesWeb editKeyValues = grid.getSelectionModel().getSelectedItem();
				  if (editKeyValues == null) {
						Info.display("Information", "You must first select a field to be edited before pressing the \"Edit Key Value Set\" button.");
						return;
				  }			
				  keyValueDesignDialog.show();
	      		  
	          }
	    }));
		
		toolBar.add(new SeparatorToolItem());
		toolBar.add(new Button(" Remove Key Value Set ", IconHelper.create("images/key_delete.png"), new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  keyValueDesignDialog.setIcon(IconHelper.create("images/key_delete.png"));
	        	  keyValueDesignDialog.setHeading("Delete Key Value Set");	      		  
				  Button ok = keyValueDesignDialog.getButtonById("ok");
				  ok.setText("Delete Key Value Set");
				  
				  EntityKeyValuesWeb removeKeyValues = grid.getSelectionModel().getSelectedItem();
				  if (removeKeyValues == null) {
						Info.display("Information", "You must first select a field before pressing the \"Remove Key Value Set\" button.");
						return;
				  }					  
				  keyValueDesignDialog.show();	   	
	      				  
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
		  keyNameEdit.setReadOnly(enable);
	}
	
	final Listener<MessageBoxEvent> listenConfirmDelete = new Listener<MessageBoxEvent>() {  
        public void handleEvent(MessageBoxEvent ce) {  
          Button btn = ce.getButtonClicked();  
          if( btn.getText().equals("Yes")) {
        	  
          }
        }  
	};

	
	// Add/Edit/Delete Key Value Set Dialog
	private void buildKeyValueSetDesignDialog() {		
		if( keyValueDesignDialog!= null)
			return;
		
		keyValueDesignDialog = new Dialog();
		keyValueDesignDialog.setBodyBorder(false);
		keyValueDesignDialog.setWidth(500);
		keyValueDesignDialog.setHeight(390);
		keyValueDesignDialog.setButtons(Dialog.OKCANCEL);
		keyValueDesignDialog.setModal(true);
		keyValueDesignDialog.getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  	        	  

	          }
	    });
		
		keyValueDesignDialog.getButtonById(Dialog.CANCEL).addSelectionListener(new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  keyValueDesignDialog.hide();
	          }
	    });
		
		ContentPanel cp = new ContentPanel();
		cp.setHeading("Key Value Set");
		cp.setFrame(true);
		cp.setIcon(IconHelper.create("images/key.png"));
			FormLayout formLayout = new FormLayout();
			formLayout.setLabelWidth(120);
			formLayout.setDefaultWidth(260);
		cp.setLayout(formLayout);
		cp.setSize(490, 320);
		
		keyNameEdit.setFieldLabel("Key Name");
		keyNameEdit.setAllowBlank(false);
		
		cp.add(keyNameEdit);

		LayoutContainer formContainer = new LayoutContainer();
			ColumnLayout columnLayout = new ColumnLayout();
			formContainer.setLayout(columnLayout);  
//			formContainer.add( setupGroupPanel(""), new ColumnData(0.4));			
			formContainer.add( setupKeyValuePanel(""), new ColumnData(1.0));
			
		cp.add( formContainer );
	
		keyValueDesignDialog.add(cp);
	}
		
	//	Add/Edit/Delete Key Panel
	private ContentPanel setupKeyValuePanel(String title) {
		ContentPanel cp = new ContentPanel(); 
		
		cp.setFrame(true);
		cp.setHeaderVisible(false);
		cp.setLayout(new FillLayout());
		cp.setSize(470, 255);
		
		ToolBar toolBar = new ToolBar();

		Label label = new Label("Key Value:");
		toolBar.add(label);		
		toolBar.add(keyValueEdit);

		// add some spaces
		toolBar.add(new Label(""));
		toolBar.add(new Label(""));
		toolBar.add(new SeparatorToolItem());
		
		toolBar.add(new Button("Add Key Value", IconHelper.create("images/database_add.png"), new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				if ( !keyNameEdit.isValid() || !keyValueEdit.isValid()) {
					 Info.display("Key Name:", "Invalid field");	
					 return;
				}
				
				EntityKeyValuesWeb field = new EntityKeyValuesWeb();
				field.setName(keyNameEdit.getValue());
				
				keyValueGrid.getStore().add(field);	 			
			}
	    }));

		toolBar.add(new SeparatorToolItem());
		toolBar.add(new Button("Remove Key Value", IconHelper.create("images/database_delete.png"), new SelectionListener<ButtonEvent>() {
	          @Override
	          public void componentSelected(ButtonEvent ce) {
	        	  
	        	  EntityKeyValuesWeb field = keyValueGrid.getSelectionModel().getSelectedItem();
	        	  if (field == null) {
	        		  Info.display("Information","You must first select a field before pressing the \"Remove Key Value\" button.");
	        		  return;
	        	  }
	        	  
	        	  keyValueGrid.getStore().remove(field);
	          }
	    }));
		cp.setTopComponent(toolBar);
		
		ColumnConfig pName = new ColumnConfig("name", "Key Name", 120);
		ColumnConfig pValue = new ColumnConfig("nameDisplayed", "Key Value", 120);		

		TextField<String> keyValueText = new TextField<String>();  
		keyValueText.setValidator(new Validator() {	    	
				public String validate(Field<?> field, String value) {	
					   return null;
				}
	    });		
		pValue.setEditor(new CellEditor(keyValueText)); 
		
		List<ColumnConfig> config = new ArrayList<ColumnConfig>();
		config.add(pName);
		config.add(pValue);		

		ColumnModel cm = new ColumnModel(config);	  
	    RowEditor<EntityKeyValuesWeb> rowEditor = new RowEditor<EntityKeyValuesWeb>(); 
	    rowEditor.setClicksToEdit(ClicksToEdit.TWO);
		keyValueGrid = new Grid<EntityKeyValuesWeb>(keyValueStore, cm);
		keyValueGrid.setStyleAttribute("borderTop", "none");
		keyValueGrid.setBorders(true);
		keyValueGrid.setStripeRows(true); 
		keyValueGrid.addPlugin(rowEditor); 
		cp.add(keyValueGrid);
	
		return cp;
	}
}
