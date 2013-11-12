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
package org.openempi.webapp.client.mvc.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.domain.AuthenticationException;
import org.openempi.webapp.client.model.EntityAttributeGroupWeb;
import org.openempi.webapp.client.model.EntityAttributeWeb;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.ExactMatchingConfigurationWeb;
import org.openempi.webapp.client.model.IdentifierWeb;
import org.openempi.webapp.client.model.MatchConfigurationWeb;
import org.openempi.webapp.client.model.MatchFieldWeb;
import org.openempi.webapp.client.model.RecordLinkWeb;
import org.openempi.webapp.client.model.RecordLinksListWeb;
import org.openempi.webapp.client.model.RecordSearchCriteriaWeb;
import org.openempi.webapp.client.model.RecordWeb;
import org.openempi.webapp.client.model.SystemConfigurationWeb;
import org.openempi.webapp.client.mvc.BaseEntityView;
import org.openempi.webapp.client.mvc.Controller;
import org.openempi.webapp.client.ui.util.AttributeDatatype;
import org.openempi.webapp.client.ui.util.Utility;

import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.HeaderGroupConfig;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.Info;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;


public class EntityLinkView extends BaseEntityView
{
    public static final Integer PAGE_SIZE = new Integer(10);

    // keep six digit decimal numbers
    private static final NumberFormat nfc = NumberFormat.getFormat("#,###.######");

    private ContentPanel container;
    private LayoutContainer gridContainer;
    private LayoutContainer formButtonContainer;

    private LayoutContainer formContainer;
    private FormPanel linkPairPanel;
    private FormPanel buttonPanel;
    private Status status;
    private Button linkButton;
    private Button unlinkButton;

    private RpcProxy<PagingLoadResult<RecordLinkWeb>> proxy;
	private BasePagingLoader<PagingLoadResult<RecordLinkWeb>> pagingLoader;
	private PagingToolBar pagingToolBar;

	private RecordSearchCriteriaWeb searchCriteria;

	private EntityWeb currentEntity;

	private RecordLinkWeb selectedPair;
	private Grid<RecordLinkWeb> grid;
	private ListStore<RecordLinkWeb> pairStore = new ListStore<RecordLinkWeb>();
	private List<MatchFieldWeb> fields = null;

	private ListStore<IdentifierWeb> leftIdentifierStore = new ListStore<IdentifierWeb>();
	private ListStore<IdentifierWeb> rightIdentifierStore = new ListStore<IdentifierWeb>();

	private Grid<BaseModelData> gridLinkPair;
	private ListStore<BaseModelData> linkPairStore = new ListStore<BaseModelData>();

	private Map<String, String> recordFieldMap;
	private Map<String, String> nonmatchFieldMap = new HashMap<String, String>();

	public EntityLinkView(Controller controller) {
		super(controller);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void handleEvent(AppEvent event) {

		if (event.getType() == AppEvents.EntityLinkView) {

			SystemConfigurationWeb systemInfo = (SystemConfigurationWeb) Registry.get(Constants.SYSTEM_CONFIGURATION_INFO);
			if (systemInfo != null && systemInfo.getMatchingAlgorithmName().contains("Deterministic Matching Algorithm")){

				// Info.display("Information", "Deterministic Matching Algorithm");
				controller.handleEvent(new AppEvent(AppEvents.DeterministicMatchConfigurationRequest));

			} else if (systemInfo != null && systemInfo.getMatchingAlgorithmName().contains("Probabilistic Matching Algorithm")){
				// Info.display("Information", "Probabilistic Matching Algorithm");
				controller.handleEvent(new AppEvent(AppEvents.MatchConfigurationRequest));
		    }

		} else if (event.getType() == AppEvents.DeterministicMatchConfigurationReceived) {
	    	// Info.display("Information", "DeterministicMatchConfigurationReceived");

			ExactMatchingConfigurationWeb config = (ExactMatchingConfigurationWeb) event.getData();
			fields = (List<MatchFieldWeb>) config.getMatchFields();

			if (Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL) != null) {

				currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
				initEntityUI(currentEntity);

				leftIdentifierStore.removeAll();
			    rightIdentifierStore.removeAll();
			    linkPairStore.removeAll();

				searchCriteria = new RecordSearchCriteriaWeb();
				searchCriteria.setEntityModel(currentEntity);
				searchCriteria.setSearchMode(Constants.PROBABLE_MATCH);
				searchCriteria.setFirstResult(new Integer(0));
				searchCriteria.setMaxResults(PAGE_SIZE);
				searchCriteria.setTotalCount(new Long(0));

			    PagingLoadConfig pagingLoadConfig = new BasePagingLoadConfig();
			    pagingLoadConfig.setOffset(0);
			    pagingLoadConfig.setLimit(PAGE_SIZE);

			    pagingLoader.load(pagingLoadConfig);
			}


		} else if (event.getType() == AppEvents.MatchConfigurationReceived) {
	    	// Info.display("Information", "MatchConfigurationReceived");

			MatchConfigurationWeb config = (MatchConfigurationWeb) event.getData();

			if (Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL) != null) {

				currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
	            fields = (List<MatchFieldWeb>) config.getMatchFields();
	            for (MatchFieldWeb matchField : fields) {
	                EntityAttributeWeb attribute = currentEntity.findEntityAttributeByName(matchField.getFieldName());
	                if (attribute != null) {
	                    matchField.setFieldDescription(attribute.getDisplayName());
	                }
	            }

				initEntityUI(currentEntity);

				leftIdentifierStore.removeAll();
				rightIdentifierStore.removeAll();
			    linkPairStore.removeAll();

				searchCriteria = new RecordSearchCriteriaWeb();
				searchCriteria.setEntityModel(currentEntity);
				searchCriteria.setSearchMode(Constants.PROBABLE_MATCH);
				searchCriteria.setFirstResult(new Integer(0));
				searchCriteria.setMaxResults(PAGE_SIZE);
				searchCriteria.setTotalCount(new Long(0));

			    PagingLoadConfig pagingLoadConfig = new BasePagingLoadConfig();
			    pagingLoadConfig.setOffset(0);
			    pagingLoadConfig.setLimit(PAGE_SIZE);
			    pagingLoader.load(pagingLoadConfig);
			}

		} else if (event.getType() == AppEvents.EntityLinkPairsRequest) {

			// Info.display("Information", "RecordLinks Received.");
/*			List<RecordLinkWeb> entityPairs = (List<RecordLinkWeb>) event.getData();

			for (RecordLinkWeb  entityPair: entityPairs) {

				 int currentFieldSize = fields.size();
				 int j = 1;
				 for (MatchFieldWeb matchField : fields) {
					// set bit such as 1000, 100, 10, 1
				    int bit = (int) Math.pow(2,currentFieldSize-j);
				    String value = matchField.getFieldName()+":"+Integer.toString(bit);
				    entityPair.set(matchField.getFieldName(), value);
					j++;
				 }
			}

			pairStore.removeAll();
			pairStore.add(entityPairs);
*/
		} else if (event.getType() == AppEvents.EntityOneLinkPairRequest) {
			// Info.display("Information", "RecordLink Received.");
			RecordLinkWeb entityPair = (RecordLinkWeb) event.getData();

			leftIdentifierStore.removeAll();
		    rightIdentifierStore.removeAll();
		    linkPairStore.removeAll();

		    displayLeftEntityIdentifier(entityPair.getLeftRecord());
		    displayRightEntityIdentifier(entityPair.getRightRecord());

			for (String key : recordFieldMap.keySet()) {
    			 BaseModelData	model = new	BaseModelData();
                 // model.set("attribute", key);
      			 model.set("attribute", recordFieldMap.get(key));

      			 EntityAttributeWeb attribute = currentEntity.findEntityAttributeByName(key);
     			 AttributeDatatype type = AttributeDatatype.getById(attribute.getDatatype().getDataTypeCd());

     			 if (type == AttributeDatatype.DATE) {
     				 String date = Utility.DateToString((Date) entityPair.getLeftRecord().get(key));
	     			 model.set("leftRecord",  date);
	     			 date = Utility.DateToString((Date) entityPair.getRightRecord().get(key));
	     			 model.set("rightRecord",  date);
     			 } else {
	     			 model.set("leftRecord", entityPair.getLeftRecord().get(key));
	     			 model.set("rightRecord", entityPair.getRightRecord().get(key));
     			 }
     			 linkPairStore.add(model);
			}

	    	linkButton.enable();
	    	unlinkButton.enable();

		} else if (event.getType() == AppEvents.ProcessPairLinkedView) {

		    PagingLoadConfig config = new BasePagingLoadConfig();
		    config.setOffset(searchCriteria.getFirstResult());
		    config.setLimit(PAGE_SIZE);

		    // reduced the total count by 1 for Match
		    searchCriteria.setTotalCount(searchCriteria.getTotalCount() - 1);
		    pagingLoader.load(config);


			linkButton.enable();
			unlinkButton.enable();

			status.hide();
			linkButton.unmask();
			unlinkButton.unmask();
	        Info.display("Confirm :", " Successfully linked");
		} else if (event.getType() == AppEvents.ProcessPairUnlinkedView) {

		    PagingLoadConfig config = new BasePagingLoadConfig();
		    config.setOffset(searchCriteria.getFirstResult());
		    config.setLimit(PAGE_SIZE);

		    // reduced the total count by 1 for Non-Match
		    searchCriteria.setTotalCount(searchCriteria.getTotalCount() - 1);
		    pagingLoader.load(config);

			linkButton.enable();
			unlinkButton.enable();

			status.hide();
			linkButton.unmask();
			unlinkButton.unmask();
	        Info.display("Confirm :", " Successfully unlinked");
		} else if (event.getType() == AppEvents.Error) {
			String message = event.getData();
	        MessageBox.alert("Information", "Failure: " + message, listenFailureMsg);
		}
	}

	private final Listener<MessageBoxEvent> listenFailureMsg = new Listener<MessageBoxEvent>() {
        public void handleEvent(MessageBoxEvent ce) {
          Button btn = ce.getButtonClicked();
          if (btn.getText().equals("OK")) {

			  leftIdentifierStore.removeAll();
			  rightIdentifierStore.removeAll();
			  linkPairStore.removeAll();

  			  status.hide();
  			  linkButton.unmask();
          }
        }
    };

    private final Listener<MessageBoxEvent> listenLink = new Listener<MessageBoxEvent>() {
        public void handleEvent(MessageBoxEvent ce) {
          Button btn = ce.getButtonClicked();
          // Info.display("MessageBox1 ", "The '{0}' button was pressed", btn.getText());
          if (btn.getText().equals("Yes")) {

			  status.show();
			  linkButton.mask();

			  AppEvent event = new AppEvent(AppEvents.ProcessLink);
			  event.setData("entityModel", currentEntity);
			  event.setData("linkPair", selectedPair);
			  controller.handleEvent(event);
          }

        }
    };

    private final Listener<MessageBoxEvent> listenUnlink = new Listener<MessageBoxEvent>() {
        public void handleEvent(MessageBoxEvent ce) {
          Button btn = ce.getButtonClicked();
          // Info.display("MessageBox2 ", "The '{0}' button was pressed", btn.getText());
          if (btn.getText().equals("Yes")) {

			  status.show();
			  unlinkButton.mask();

			  AppEvent event = new AppEvent(AppEvents.ProcessUnlink);
			  event.setData("entityModel", currentEntity);
			  event.setData("linkPair", selectedPair);
			  controller.handleEvent(event);
          }
        }
    };


	private PagingToolBar setupRpcProxy() {
		// Rpc Proxy
		proxy = new RpcProxy<PagingLoadResult<RecordLinkWeb>>() {

            @Override
            public void load(final Object loadConfig, final AsyncCallback<PagingLoadResult<RecordLinkWeb>> callback) {

            	if (searchCriteria == null) {
  		            callback.onSuccess(new BasePagingLoadResult<RecordLinkWeb>(null, 0, 0));
            		return;
            	}
                // set page offset for searchCriteria
             	searchCriteria.setFirstResult(((PagingLoadConfig) loadConfig).getOffset());

             	getController().getEntityInstanceDataService().loadRecordLinksPaged(searchCriteria, new AsyncCallback<RecordLinksListWeb>() {
	 	    		      public void onFailure(Throwable caught) {

	 	    		    	  if (caught instanceof AuthenticationException) {
	 	    		    		  Dispatcher.get().dispatch(AppEvents.Logout);
	 	    		    		  return;
	 	    		    	  }
	 	    		    	  searchCriteria.setTotalCount(new Long(0));
	 	    		    	  Dispatcher.forwardEvent(AppEvents.Error, caught);
	 	    		      }

	 	    		      public void onSuccess(RecordLinksListWeb result) {
	 	    		    	  //Info.display("Information", "The offset: " +((PagingLoadConfig)loadConfig).getOffset());

	 	    		    	  // color vector fields
	 	    				  List<RecordLinkWeb> entityPairs = result.getRecordLinks();
	 	    				  for (RecordLinkWeb  entityPair: entityPairs) {

	 	    				       // display six digit decimal numbers format for Weight
	 	    				       entityPair.setWeight(Double.parseDouble(nfc.format(entityPair.getWeight())));

	 	    					   int j = 0;
	 	    					   for (MatchFieldWeb matchField : fields) {

	 	    					       // set bit such as 1, 10, 100, 1000
	 	    						   int bit = (int) Math.pow(2, j);
//	 	    						   String value = matchField.getFieldName() + ":" + Integer.toString(bit);
	                                   String value = matchField.getFieldDescription() + ":" + Integer.toString(bit);
	 	    						   entityPair.set(matchField.getFieldName(), value);
	 	    						   j++;
	 	    					   }
	 	    				  }

	 	    				  // PagingLoadConfig configuration
	 	    		    	  searchCriteria.setTotalCount(result.getTotalCount());
	 	    		    	  callback.onSuccess(new BasePagingLoadResult<RecordLinkWeb>(entityPairs, ((PagingLoadConfig) loadConfig).getOffset(), result.getTotalCount().intValue()));
	 	    		      }
	 	    	});

	        }
	    };

	    // Page loader
	    pagingLoader = new BasePagingLoader<PagingLoadResult<RecordLinkWeb>>(proxy);
	    pagingLoader.setRemoteSort(true);
	    pagingLoader.addLoadListener(new LoadListener() {
			// After the loader be completely filled, remove the mask
			public void loaderLoad(LoadEvent le) {

				grid.unmask();
			}
		});

	    pairStore = new ListStore<RecordLinkWeb>(pagingLoader);

	    PagingToolBar pagingToolBar = new PagingToolBar(PAGE_SIZE);
	    pagingToolBar.bind(pagingLoader);
	    return pagingToolBar;
	}

	private void setupPairRecordGrid(EntityWeb entity) {
		ContentPanel cp = new ContentPanel();
		cp.setHeading("Link Entities");
		cp.setHeaderVisible(false);
		cp.setBodyBorder(false);
		cp.setLayout(new FillLayout());

		grid = setupGrid(entity);

		cp.add(grid);
		cp.setBottomComponent(pagingToolBar);

		gridContainer.add(cp);
	}

	private Grid<RecordLinkWeb> setupGrid(EntityWeb entity) {

  		// setup column configuration
		List<ColumnConfig> columnConfig = new ArrayList<ColumnConfig>();

		// Date Created
		ColumnConfig column = new ColumnConfig("dateCreated", "Date Created", 120);
		column.setDateTimeFormat(DateTimeFormat.getShortDateTimeFormat());
		columnConfig.add(column);

		// Weight
		column = new ColumnConfig("weight", "Weight", 120);
		columnConfig.add(column);

		// Vector
		//column = new ColumnConfig( "vector", "vector", 80);
		//columnConfig.add(column);


		// Render to display the color and stripe
		GridCellRenderer<BaseModelData> colorRenderer = new GridCellRenderer<BaseModelData>() {
			public String render(BaseModelData model, String property, ColumnData config, int rowIndex,
			          int colIndex, ListStore<BaseModelData> store, Grid<BaseModelData> grid) {

			    	  // get cell value:  name : bit
			          String value = (String) model.get(property);
			          String[] result = value.split(":");
			          String name = result[0];
			          int bit = Integer.parseInt(result[1]);

			          // get vector value and use the bit to set color
			          int color = 0;
			          int val = 0;
			          if (model.get("vector") != null) {
	                      val = (Integer) model.get("vector");
	                      color = val & bit;
			          }
			          String backgroundColor = "#E79191"; //"orangered";
			          String decoration = "line-through";
			          String fontWeight = "bold";

			          if (color != 0)  {
			        	  backgroundColor = "lightgreen";
			              decoration = "none";
			          }
			          return "<span style='background-color:" + backgroundColor + " ; text-decoration: " + decoration + " ; font-weight:" + fontWeight+"'>" + name + "</span>";
			      }
			    };

		// color fields for vector
		for (MatchFieldWeb matchField : fields) {
			ColumnConfig fieldColumn = new ColumnConfig(matchField.getFieldName(), matchField.getFieldDescription(), matchField.getFieldName().length()*9);
			fieldColumn.setRenderer(colorRenderer);
			columnConfig.add(fieldColumn);
		}

		// State
/*		column = new ColumnConfig( "state", "State", 100);
		columnConfig.add(column);
*/
		// Link Source
		column = new ColumnConfig("linkSource.sourceName", "Link Source", 180);
		columnConfig.add(column);

		column = new ColumnConfig("userCreatedBy.username", "Created By", 180);
		columnConfig.add(column);

		ColumnModel cm = new ColumnModel(columnConfig);
	    //  cm.addHeaderGroup(0, 3, new HeaderGroupConfig("Vector", 1, fields.size()));
		Grid<RecordLinkWeb>  grid = new Grid<RecordLinkWeb>(pairStore, cm);
		grid.setBorders(true);
		grid.setAutoWidth(true);
		grid.setStripeRows(true);
		grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		grid.setHeight(300);

		// selection event
		grid.getSelectionModel().addListener(Events.SelectionChange,
				new Listener<SelectionChangedEvent<RecordLinkWeb>>() {
					public void handleEvent(SelectionChangedEvent<RecordLinkWeb> be) {
						RecordLinkWeb field = be.getSelectedItem();
						if (field != null) {
							// Info.display("Information: ", field.getLeftRecord().getEntityDefinitionName());
						    selectedPair = field;

				      		// non match fields
				      		nonmatchFieldMap.clear();
		                    int val = 0;
		                    if (field.getVector() != null) {
		                        val = (Integer) field.getVector();
		                    }
	 	    				int j = 0;
	 	    				for (MatchFieldWeb matchField : fields) {
	 	    					// set bit such as 1, 10, 100, 1000
	 	    					int bit = (int) Math.pow(2, j);
	 	    					int match = val & bit;

	 	    					if (match == 0) {
	 	    						// Info.display("Information", "The non match field: " +matchField.getFieldName());
	 	    						nonmatchFieldMap.put(matchField.getFieldName(), matchField.getFieldName()); 
	 	    					}
	 	    					j++;
	 	    				}

	 	    				AppEvent event = new AppEvent(AppEvents.EntityOneLinkPairRequest);
						    event.setData("entityModel", currentEntity);
						    event.setData("linkPair", selectedPair);
						    controller.handleEvent(event);

						} else {
						    selectedPair = null;

						    leftIdentifierStore.removeAll();
						    rightIdentifierStore.removeAll();
						    linkPairStore.removeAll();

							linkButton.disable();
							unlinkButton.disable();
						}
					}
				});

		return grid;
	}

    private void displayLeftEntityIdentifier(RecordWeb record) {
    	if (record.getIdentifiers() == null) {
    		return;
    	}
		for (IdentifierWeb identifier : record.getIdentifiers()) {
			leftIdentifierStore.add(identifier);
		}
    }

    private void displayRightEntityIdentifier(RecordWeb record) {
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

/*		column = new ColumnConfig();
		column.setId("namespaceIdentifier");
		column.setHeader("Namespace Identifier");
		column.setWidth(120);
		configs.add(column);

		column = new ColumnConfig();
		column.setId("universalIdentifier");
		column.setHeader("Universal Identifier");

		column.setWidth(170);
		configs.add(column);

		column = new ColumnConfig();
		column.setId("universalIdentifierTypeCode");
		column.setHeader("Universal Identifier Type");
		column.setWidth(130);
		configs.add(column);
*/
		ColumnModel cm = new ColumnModel(configs);
		cm.addHeaderGroup(0, 0, new HeaderGroupConfig(record, 1, 4));
		Grid<IdentifierWeb> identifierGrid = new Grid<IdentifierWeb>(identifierStore, cm);

		identifierGrid.setStyleAttribute("borderTop", "none");
		identifierGrid.setBorders(true);
		identifierGrid.setBorders(true);
		identifierGrid.setStripeRows(true);
		identifierGrid.setWidth(445);
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
			          String attributeName = (String) model.get("attribute");
			          String valueLeft = (String) model.get("leftRecord");
			          String valueRight = (String) model.get("rightRecord");

			          String backgroundColor = "lightgrey";
			          if (valueLeft == null && valueRight == null) {

			        	  // non match field and null values
			        	  if (nonmatchFieldMap.get(attributeName) != null) {
					          config.style = "background-color:" + backgroundColor + ";";
	 	    			  }
			        	  return null;
			          }

			          backgroundColor = "#E79191"; // "lightpink";
			          if (valueLeft == null || valueRight == null) {
			        	  if (value == null) {
					          return null;
			        	  }
			        	  return "<div style='background-color:" + backgroundColor + "'>" + value + "</div>";

			          }

			          if (valueLeft.equals(valueRight))  {

                          // non match field and no values
			              if (valueLeft.isEmpty()) {
	                          backgroundColor = "lightgrey;";
	                          if (nonmatchFieldMap.get(attributeName) != null) {
	                              config.style = "background-color:" + backgroundColor+";";
	                              return null;
	                          }
			              }
			        	  backgroundColor = "lightgreen";
			          }

			          // <span: background is set as long as the value is.  <div background is set whole length.
			          return "<div style='background-color:" + backgroundColor + "'>" + value + "</div>";
//			          config.style = "background-color:"+backgroundColor+";";
//			          return value;
			      }
			    };

		column = new ColumnConfig();
		column.setId("leftRecord");
		column.setHeader("Left Record");
		column.setWidth(260);
		column.setRenderer(colorRenderer);
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
		linkPairGrid.setWidth(785);
		linkPairGrid.setHeight(300);

		linkPairPanel.add(linkPairGrid);

		return linkPairPanel;
	}

	private FormPanel setupButtonPanel() {
		FormPanel buttonPanel = new FormPanel();
		buttonPanel.setHeaderVisible(false);
		buttonPanel.setBodyBorder(false);
		buttonPanel.setWidth("900");
		buttonPanel.setButtonAlign(HorizontalAlignment.CENTER);

		// Buttons
		linkButton = new Button("Link", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
        	  	if (selectedPair != null) {
        	  	    MessageBox.confirm("Confirm", "Are you sure you want to link those two entities?", listenLink);
        	  	}
			}
		});
		unlinkButton = new Button("Unlink", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
        	  	if (selectedPair != null) {
        	  	    MessageBox.confirm("Confirm", "Are you sure you want to unlink those two entities?", listenUnlink);
        	  	}
			}
		});
		status = new Status();
		status.setBusy("please wait...");
		status.hide();
		linkButton.disable();
		unlinkButton.disable();
		buttonPanel.getButtonBar().setSpacing(5);
		buttonPanel.getButtonBar().add(new FillToolItem());
		buttonPanel.getButtonBar().add(status);
		buttonPanel.getButtonBar().add(linkButton);
		buttonPanel.getButtonBar().add(unlinkButton);

		return buttonPanel;
	}

	private void initEntityUI(EntityWeb entity) {
		long time = new java.util.Date().getTime();
		GWT.log("Initializing the UI ", null);

/*		AppEvent event = new AppEvent(AppEvents.EntityLinkPairsRequest);
	    event.setData("entityModel", currentEntity);
	    controller.handleEvent(event);
*/
		container = new ContentPanel();
		container.setLayout(new BorderLayout());
		container.setHeading("Entity Review Links");

		// Grid
		gridContainer = new LayoutContainer();
		gridContainer.setBorders(true);
		gridContainer.setLayout(new FitLayout());


		// Rpc Proxy setup
		pagingToolBar = setupRpcProxy();


		// Record pair grid
		setupPairRecordGrid(entity);

		// forms and buttons
		formButtonContainer = new LayoutContainer();
		formButtonContainer.setScrollMode(Scroll.AUTO);

		// forms
		formContainer = new LayoutContainer();
		formContainer.setBorders(false);
		TableLayout layout = new TableLayout(2);
		layout.setWidth("900"); //"100%"
	    layout.setCellSpacing(5);
	    layout.setCellVerticalAlign(VerticalAlignment.TOP);
		formContainer.setLayout(layout);

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

		formContainer.add(setupIdentifierGrid(leftIdentifierStore, "Left Record"));
		formContainer.add(setupIdentifierGrid(rightIdentifierStore, "Right Record"));

		linkPairPanel = setupLinkPairPanel();

		buttonPanel = setupButtonPanel();

		formButtonContainer.add(formContainer);
		formButtonContainer.add(linkPairPanel);
		formButtonContainer.add(buttonPanel);

		BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
		data.setMargins(new Margins(4, 2, 4, 2));
		container.add(formButtonContainer, data);
		container.add(gridContainer, new BorderLayoutData(LayoutRegion.NORTH, 250));


		LayoutContainer wrapper = (LayoutContainer) Registry.get(Constants.CENTER_PANEL);
		wrapper.removeAll();
		wrapper.add(container);
		wrapper.layout();
		GWT.log("Done Initializing the UI in " + (new java.util.Date().getTime() - time), null);

	}
}
