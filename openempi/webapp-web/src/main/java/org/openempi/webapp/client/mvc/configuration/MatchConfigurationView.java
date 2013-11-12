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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.domain.AuthenticationException;
import org.openempi.webapp.client.mvc.BaseEntityView;
import org.openempi.webapp.client.mvc.Controller;
import org.openempi.webapp.client.model.EntityAttributeGroupWeb;
import org.openempi.webapp.client.model.EntityAttributeWeb;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.IdentifierWeb;
import org.openempi.webapp.client.model.LoggedLinkListWeb;
import org.openempi.webapp.client.model.LoggedLinkSearchCriteriaWeb;
import org.openempi.webapp.client.model.MatchConfigurationWeb;
import org.openempi.webapp.client.model.MatchFieldWeb;
import org.openempi.webapp.client.model.ModelPropertyWeb;
import org.openempi.webapp.client.model.RecordLinkWeb;
import org.openempi.webapp.client.model.RecordWeb;
import org.openempi.webapp.client.model.VectorConfigurationWeb;
import org.openempi.webapp.client.model.VectorWeb;
import org.openempi.webapp.client.ui.util.AttributeDatatype;
import org.openempi.webapp.client.ui.util.Utility;

import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.Registry;
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
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.FieldSetEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
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
import com.extjs.gxt.ui.client.widget.form.DualListField;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.ListField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.HeaderGroupConfig;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowEditor;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class MatchConfigurationView extends BaseEntityView
{
    public static final Integer PAGE_SIZE = new Integer(10);

    // keep six digit decimal numbers
    private static final NumberFormat nfc = NumberFormat.getFormat("#,###.######");
    private static final NumberFormat percent = NumberFormat.getFormat("#0%;(#0%)");

    private Grid<MatchFieldWeb> grid1;
    private Grid<MatchFieldWeb> grid2;
    private ListStore<MatchFieldWeb> store = new ListStore<MatchFieldWeb>();
    private Dialog addEditMatchFieldDialog = null;
    private Boolean addOrEditFieldMode = true;
    private int editedFieldIndex = 0;
    private MatchFieldWeb editedField;
    private String entityName;

    // Basic
    private TextField<String> configFileDirectoryEdit = new TextField<String>();

    private ComboBox<ModelPropertyWeb> attributeNameCombo = new ComboBox<ModelPropertyWeb>();
    private ComboBox<ModelPropertyWeb> comparatorFuncNameCombo = new ComboBox<ModelPropertyWeb>();
    private NumberField matchThresholdEdit = new NumberField();

    private List<ModelPropertyWeb> attributeNames;
    private ListStore<ModelPropertyWeb> attributeNameStore = new ListStore<ModelPropertyWeb>();
    private ListStore<ModelPropertyWeb> comparatorFuncNameStore = new ListStore<ModelPropertyWeb>();

    // Advanced
    private NumberField lowerBoundEdit;
    private NumberField upperBoundEdit;
    private NumberField pValueEdit;
    private NumberField initialMValueEdit;
    private NumberField initialUValueEdit;
    private NumberField initialPValueEdit;
    private NumberField maxIterationsEdit;
    private NumberField convergenceErrorEdit;

    // Logging
    private FieldSet loggingVectorfieldSet;
    private FieldSet loggingWeightfieldSet;
    private NumberField weightUpperBoundEdit;
    private NumberField weightLowerBoundEdit;
    private SpinnerField vectorsFractionSpin;
    private SpinnerField weightFractionSpin;
    private ComboBox<ModelPropertyWeb> loggingDestinationCombo;
    private ListStore<ModelPropertyWeb> loggingDestinationStore = new ListStore<ModelPropertyWeb>();

    private ListStore<VectorWeb> dualListFrom = new ListStore<VectorWeb>();
    private ListStore<VectorWeb> dualListTo = new ListStore<VectorWeb>();

    // vector
    private Grid<BaseModelData> gridVector;
    private ListStore<BaseModelData> vectorStore; // = new ListStore<BaseModelData>();
    private BaseModelData selectedVector;

    private LayoutContainer container;

    private Button addFieldButton;
    private Button editFieldButton;
    private Button removeFieldButton;
    private Button moveUpFieldButton;
    private Button moveDownFieldButton;
    private Button moveUpFieldAdvanButton;
    private Button moveDownFieldAdvanButton;

    private MatchFieldWeb currentSelection;
    private MatchConfigurationWeb currentConfig;
    private int currentFieldSize;

    // Vector Detail Dialog
    private Dialog vectorDetailInfoDialog;
    private Grid<RecordLinkWeb> pairGrid;
    private ListStore<RecordLinkWeb> pairStore = new ListStore<RecordLinkWeb>();

    public RpcProxy<PagingLoadResult<RecordLinkWeb>> proxy;
    public BasePagingLoader<PagingLoadResult<RecordLinkWeb>> pagingLoader;
    public PagingToolBar pagingToolBar;
    public LoggedLinkSearchCriteriaWeb searchCriteria;

    private EntityWeb currentEntity;
    private Map<String, String> recordFieldMap;
    private Map<String, String> nonmatchFieldMap = new HashMap<String, String>();

    private ListStore<IdentifierWeb> leftIdentifierStore = new ListStore<IdentifierWeb>();
    private ListStore<IdentifierWeb> rightIdentifierStore = new ListStore<IdentifierWeb>();
    private ListStore<BaseModelData> linkPairStore = new ListStore<BaseModelData>();

    @SuppressWarnings("unchecked")
    public MatchConfigurationView(Controller controller) {
        super(controller);

        List<ModelPropertyWeb> comparatorFuncNames = (List<ModelPropertyWeb>) Registry
                .get(Constants.COMPARATOR_FUNCTION_NAMES);

        try {
            // attributeNameStore.add(names);
            comparatorFuncNameStore.add(comparatorFuncNames);
        } catch (Exception e) {
            Info.display("Message", e.getMessage());
        }

        ModelPropertyWeb logToFile = new ModelPropertyWeb(MatchConfigurationWeb.LOG_TO_FILE_DESTINATION,
                MatchConfigurationWeb.LOG_TO_FILE_DESTINATION);
        ModelPropertyWeb logToDB = new ModelPropertyWeb(MatchConfigurationWeb.LOG_TO_DB_DESTINATION,
                MatchConfigurationWeb.LOG_TO_DB_DESTINATION);
        loggingDestinationStore.add(logToFile);
        loggingDestinationStore.add(logToDB);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        if (event.getType() == AppEvents.MatchConfigurationView) {
            // initUI();
            controller.handleEvent(new AppEvent(AppEvents.MatchConfigurationRequest));

        } else if (event.getType() == AppEvents.MatchConfigurationReceived) {

            MatchConfigurationWeb config = (MatchConfigurationWeb) event.getData();
            currentConfig = config;
            controller.handleEvent(new AppEvent(AppEvents.MatchVectorConfigurationRequest));

        } else if (event.getType() == AppEvents.MatchVectorConfigurationReceived) {

            currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);

            initUI();

            // sort by display order
            List<VectorConfigurationWeb> vectorConfs = (List<VectorConfigurationWeb>) event.getData();
            Collections.sort(vectorConfs, VECTOR_WEIGHT_DISPLAY_ORDER);

            store.removeAll();
            vectorStore.removeAll();

            // Basic
            configFileDirectoryEdit.setValue(currentConfig.getConfigFileDirectory());

            // Advanced
            lowerBoundEdit.setValue(Float.parseFloat(nfc.format(currentConfig.getLowerBound())));
            upperBoundEdit.setValue(Float.parseFloat(nfc.format(currentConfig.getUpperBound())));
            pValueEdit.setValue(Float.parseFloat(nfc.format(currentConfig.getPValue())));
            initialMValueEdit.setValue(Float.parseFloat(nfc.format(currentConfig.getInitialMValue())));
            initialUValueEdit.setValue(Float.parseFloat(nfc.format(currentConfig.getInitialUValue())));
            initialPValueEdit.setValue(Float.parseFloat(nfc.format(currentConfig.getInitialPValue())));
            maxIterationsEdit.setValue(Float.parseFloat(nfc.format(currentConfig.getMaxIterations())));
            convergenceErrorEdit.setValue(Float.parseFloat(nfc.format(currentConfig.getConvergenceError())));

            // store.add(config.getMatchFields());
            List<MatchFieldWeb> fields = (List<MatchFieldWeb>) currentConfig.getMatchFields();
            for (MatchFieldWeb matchField : fields) {
                // Info.display("Information", "MatchThreshold: "+matchField.getMatchThreshold());

//                matchField.setFieldDescription(Utility.convertToDescription(matchField.getFieldName()));
                EntityAttributeWeb attribute = currentEntity.findEntityAttributeByName(matchField.getFieldName());
                if (attribute != null) {
                    matchField.setFieldDescription(attribute.getDisplayName());
                }
                matchField.setComparatorFunctionNameDescription(Utility.convertToDescription(matchField
                        .getComparatorFunctionName()));
                matchField.setAgreementProbability(Float.parseFloat(nfc.format(matchField.getAgreementProbability())));
                matchField.setDisagreementProbability(Float.parseFloat(nfc.format(matchField
                        .getDisagreementProbability())));
                matchField.setMatchThreshold(Float.parseFloat(nfc.format(matchField.getMatchThreshold())));

                matchField.setMValue(Double.parseDouble(nfc.format(matchField.getMValue())));
                matchField.setUValue(Double.parseDouble(nfc.format(matchField.getUValue())));
            }
            store.add(fields);

            grid1.getSelectionModel().select(0, true);
            grid1.getSelectionModel().deselect(0);

            // Logging Vectors
            currentFieldSize = fields.size();
            int numberOfVectors = 0;
            if (currentFieldSize > 0) {
                numberOfVectors = (int) Math.pow(2, currentFieldSize);
            }
            // Info.display("numberOfVectors", ""+numberOfVectors);

            dualListFrom.removeAll();
            dualListTo.removeAll();
            loggingVectorfieldSet.collapse();
            if (currentConfig.getLoggingByVectors() != null && currentConfig.getLoggingByVectors()) {
                loggingVectorfieldSet.expand();

                Set<Integer> vectors = currentConfig.getLoggingVectors();
                for (int i = 0; i < numberOfVectors; i++) {
                    if (vectors.contains(i)) {
                        dualListTo.add(new VectorWeb(Utility.prefixWithZeros(Integer.toBinaryString(i),
                                currentFieldSize), i));
                    } else {
                        dualListFrom.add(new VectorWeb(Utility.prefixWithZeros(Integer.toBinaryString(i),
                                currentFieldSize), i));
                    }
                }

                vectorsFractionSpin
                        .setValue(Double.parseDouble(nfc.format(currentConfig.getLoggingByVectorsFraction())) * 100);
            } else {
                for (int i = 0; i < numberOfVectors; i++) {
                    dualListFrom.add(new VectorWeb(
                            Utility.prefixWithZeros(Integer.toBinaryString(i), currentFieldSize), i));
                }
                vectorsFractionSpin.setValue(20);
            }

            // Logging Weight
            loggingWeightfieldSet.collapse();
            if (currentConfig.getLoggingByWeight() != null && currentConfig.getLoggingByWeight()) {
                loggingWeightfieldSet.expand();

                weightLowerBoundEdit.setValue(Double.parseDouble(nfc.format(currentConfig
                        .getLoggingByWeightLowerBound())));
                weightUpperBoundEdit.setValue(Double.parseDouble(nfc.format(currentConfig
                        .getLoggingByWeightUpperBound())));
                weightFractionSpin
                        .setValue(Double.parseDouble(nfc.format(currentConfig.getLoggingByWeightFraction())) * 100);
            } else {
                weightLowerBoundEdit.setValue(lowerBoundEdit.getValue().doubleValue());
                weightUpperBoundEdit.setValue(upperBoundEdit.getValue().doubleValue());
                weightFractionSpin.setValue(20);
            }
            String loggingDestination = currentConfig.getLoggingDestination();
            // Info.display("loggingDestination: ", loggingDestination);
            loggingDestinationCombo.setValue(new ModelPropertyWeb(loggingDestination, loggingDestination));

            // Vector Selection
            fields = (List<MatchFieldWeb>) currentConfig.getMatchFields();
            // Info.display("Number Of Vectors", ""+vectorConfs.size());
            for (VectorConfigurationWeb vectorConf : vectorConfs) {
                BaseModelData vector = new BaseModelData();
                java.util.HashMap<String, Object> map = new java.util.HashMap<String, Object>();
                map.put("matchDefault", vectorConf.getAlgorithmClassification());
                switch (vectorConf.getManualClassification()) {
                case Constants.MATCH_CLASSIFICATION:
                    map.put("match", new Boolean(true));
                    map.put("probable", new Boolean(false));
                    map.put("nonmatch", new Boolean(false));
                    break;
                case Constants.PROBABLE_MATCH_CLASSIFICATION:
                    map.put("match", new Boolean(false));
                    map.put("probable", new Boolean(true));
                    map.put("nonmatch", new Boolean(false));
                    break;
                case Constants.NON_MATCH_CLASSIFICATION:
                    map.put("match", new Boolean(false));
                    map.put("probable", new Boolean(false));
                    map.put("nonmatch", new Boolean(true));
                    break;
                }

/*                int j = 1;
                for (MatchFieldWeb matchField : fields) {
                    int bit = (int) Math.pow(2, currentFieldSize - j);
                    String value = matchField.getFieldName() + ":" + Integer.toString(bit);
                    map.put(matchField.getFieldName(), value);
                    j++;
                }
*/
                int j = 0;
                for (MatchFieldWeb matchField : fields) {
                    // set bit such as 1, 10, 100, 1000
                    int bit = (int) Math.pow(2, j);
//                    String value = matchField.getFieldName() + ":" + Integer.toString(bit);
                    String value = matchField.getFieldDescription() + ":" + Integer.toString(bit);
                    map.put(matchField.getFieldName(), value);
                    j++;
                }

                map.put("weight", nfc.format(vectorConf.getWeight()));
                map.put("vector", vectorConf.getVectorValue());
                vector.setProperties(map);
                // Info.display("Vector value", ""+vector.get("vector")+"; "+vector.get("givenName"));
                vectorStore.add(vector);
            }

        } else if (event.getType() == AppEvents.MatchConfigurationSaveComplete) {
            // String message = event.getData();

            // Clear logging Vector fields
            if (!loggingVectorfieldSet.isExpanded()) {
                dualListFrom.removeAll();
                dualListTo.removeAll();

                int numberOfVectors = 0;
                if (currentFieldSize > 0) {
                    numberOfVectors = (int) Math.pow(2, currentFieldSize);
                }
                for (int i = 0; i < numberOfVectors; i++) {
                    dualListFrom.add(new VectorWeb(
                            Utility.prefixWithZeros(Integer.toBinaryString(i), currentFieldSize), i));
                }
                vectorsFractionSpin.setValue(1);
            }

            // Clear logging Weight fields
            if (!loggingWeightfieldSet.isExpanded()) {
                weightLowerBoundEdit.setValue(lowerBoundEdit.getValue().doubleValue());
                weightUpperBoundEdit.setValue(upperBoundEdit.getValue().doubleValue());
                weightFractionSpin.setValue(20);
            }

            MessageBox.alert("Information", "Probabilistic Match Configuration was successfully saved", null);

            // refresh
            controller.handleEvent(new AppEvent(AppEvents.MatchConfigurationRequest));

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
        if (attributeNames != null) {
            attributeNameStore.add(attributeNames);
        }

        buildAddEditFieldDialog();
        container = new LayoutContainer();
        container.setLayout(new CenterLayout());

        // Columns for grid1
        // ColumnConfig fieldNameColumn = new ColumnConfig("fieldName", "Field Name", 130);
        ColumnConfig fieldNameColumn = new ColumnConfig("fieldDescription", "Field Name", 220);
        ColumnConfig compFuncNameColumn = new ColumnConfig("comparatorFunctionNameDescription", "Comparator Name", 220);
        ColumnConfig matchThresholdColumn = new ColumnConfig("matchThreshold", "Match Threshold", 180);
        matchThresholdColumn.setNumberFormat(nfc);

        // Columns for grid2
        ColumnConfig mValueColumn = new ColumnConfig("mValue", "m-Value", 150);
        ColumnConfig uValueColumn = new ColumnConfig("uValue", "u-Value", 150);
        mValueColumn.setNumberFormat(nfc);
        uValueColumn.setNumberFormat(nfc);

        NumberField mValueNumber = new NumberField();
        mValueNumber.setMinValue(0);
        mValueNumber.setMaxValue(1);
        mValueNumber.setFormat(nfc);
        // mValueNumber.getPropertyEditor().setType(Float.class); // important:To use the GXT NumberField with the float
        // value, we should use the setType function.
        mValueColumn.setEditor(new CellEditor(mValueNumber));

        NumberField uValueNumber = new NumberField();
        uValueNumber.setMinValue(0);
        uValueNumber.setMaxValue(1);
        uValueNumber.setFormat(nfc);
        // uValueNumber.getPropertyEditor().setType(Float.class);
        uValueColumn.setEditor(new CellEditor(uValueNumber));

        // For Basic Tab
        List<ColumnConfig> config = new ArrayList<ColumnConfig>();
        config.add(fieldNameColumn);
        config.add(compFuncNameColumn);
        config.add(matchThresholdColumn);

        final ColumnModel cm = new ColumnModel(config);

        grid1 = new Grid<MatchFieldWeb>(store, cm);
        grid1.setBorders(true);
        grid1.setAutoWidth(true);
        grid1.setStripeRows(true);
        grid1.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid1.setHeight(320);

        // For Advanced Tab
        List<ColumnConfig> config2 = new ArrayList<ColumnConfig>();
        config2.add(fieldNameColumn);
        config2.add(mValueColumn);
        config2.add(uValueColumn);

        final ColumnModel cm2 = new ColumnModel(config2);
        RowEditor<MatchFieldWeb> rowEditor = new RowEditor<MatchFieldWeb>();
        rowEditor.setClicksToEdit(ClicksToEdit.TWO);

        grid2 = new Grid<MatchFieldWeb>(store, cm2);
        grid2.setBorders(true);
        grid2.setAutoWidth(true);
        grid2.setStripeRows(true);
        grid2.addPlugin(rowEditor);
        grid2.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid2.setHeight(180);

        // Render to display the chec kbox
        GridCellRenderer<BaseModelData> matchRenderer = new GridCellRenderer<BaseModelData>()
        {
            public Object render(BaseModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                    ListStore<BaseModelData> store, Grid<BaseModelData> grid) {

                // get cell value which is manualClassification setting
                Boolean result = (Boolean) model.get(property);
                CheckBox checkBox = new CheckBox();
                if (result) {
                    checkBox.setValue(true);
                } else {
                    checkBox.setValue(false);
                }

                // 1. match, 2. probable , 3. non-match
                int matchDefault = (Integer) model.get("matchDefault");
                if (matchDefault == colIndex + 1) {
                    // algorithmClassification is default setting
                    checkBox.setValue(true);
                    checkBox.disable();
                }
                return checkBox;
            }
        };

        List<ColumnConfig> configVector = new ArrayList<ColumnConfig>();
        CheckColumnConfig matchColumn = new CheckColumnConfig("match", "Match", 50);
        matchColumn.setRenderer(matchRenderer);
        configVector.add(matchColumn);

        CheckColumnConfig probabilityColumn = new CheckColumnConfig("probable", "Probable", 60);
        probabilityColumn.setRenderer(matchRenderer);
        configVector.add(probabilityColumn);

        CheckColumnConfig nonmatchColumn = new CheckColumnConfig("nonmatch", "Non-Match", 70);
        nonmatchColumn.setRenderer(matchRenderer);
        configVector.add(nonmatchColumn);

        // Render to display the color and stripe
        GridCellRenderer<BaseModelData> colorRenderer = new GridCellRenderer<BaseModelData>()
        {
            public String render(BaseModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                    ListStore<BaseModelData> store, Grid<BaseModelData> grid) {

                // get cell value: name : bit
                String value = (String) model.get(property);
                String[] result = value.split(":");
                String name = result[0];
                int bit = Integer.parseInt(result[1]);

                // get vector value
                int val = (Integer) model.get("vector");
                int color = val & bit;

                String backgroundColor = "#E79191"; // "orangered";
                String decoration = "line-through";
                String fontWeight = "bold";

                if (color != 0) {
                    backgroundColor = "lightgreen";
                    decoration = "none";
                }
                return "<span style='background-color:" + backgroundColor + " ; text-decoration: " + decoration
                        + " ; font-weight:" + fontWeight + "'>" + name + "</span>";
            }
        };

        List<MatchFieldWeb> fields = (List<MatchFieldWeb>) currentConfig.getMatchFields();
        for (MatchFieldWeb matchField : fields) {
            ColumnConfig fieldColumn = new ColumnConfig(matchField.getFieldName(), matchField.getFieldDescription(),
                    matchField.getFieldName().length() * 9);
            fieldColumn.setRenderer(colorRenderer);
            configVector.add(fieldColumn);
        }
        ColumnConfig fieldColumn = new ColumnConfig("weight", "Match Scroe", 80);
        configVector.add(fieldColumn);
        // fieldColumn = new ColumnConfig("vector", "Vector", 100);
        // configVector.add(fieldColumn);

        final ColumnModel cmVector = new ColumnModel(configVector);
        vectorStore = new ListStore<BaseModelData>();
        gridVector = new Grid<BaseModelData>(vectorStore, cmVector);
        gridVector.setBorders(true);
        gridVector.setAutoWidth(true);
        gridVector.setStripeRows(true);
        gridVector.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        gridVector.addPlugin(matchColumn);
        gridVector.addPlugin(nonmatchColumn);
        gridVector.addPlugin(probabilityColumn);
        gridVector.setHeight(450);

        grid1.getSelectionModel().addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent<MatchFieldWeb>>()
                {
                    public void handleEvent(SelectionChangedEvent<MatchFieldWeb> be) {
                        currentSelection = be.getSelectedItem();
                        Boolean editFieldEnabled = false;
                        Boolean removeFieldEnabled = false;
                        Boolean moveUpEnabled = false;
                        Boolean moveDownEnabled = false;

                        if (currentSelection != null) {
                            if (attributeNames != null) {
                                editFieldEnabled = true;
                                removeFieldEnabled = true;
                            }
                            int selectionIndex = grid1.getStore().indexOf(currentSelection);
                            moveUpEnabled = (selectionIndex > 0);
                            moveDownEnabled = (selectionIndex < grid1.getStore().getCount() - 1);

                            grid2.getSelectionModel().select(currentSelection, false);
                        } else {
                            grid2.getSelectionModel().deselectAll();
                        }
                        editFieldButton.setEnabled(editFieldEnabled);
                        removeFieldButton.setEnabled(removeFieldEnabled);
                        moveUpFieldButton.setEnabled(moveUpEnabled);
                        moveDownFieldButton.setEnabled(moveDownEnabled);
                    }
                });

        grid1.addListener(Events.SortChange, new Listener<GridEvent>()
        {
            public void handleEvent(GridEvent be) {
                // Info.display("Information", "SortChange.");
                MatchFieldWeb selectField = grid1.getSelectionModel().getSelectedItem();

                int selectionIndex = grid1.getStore().indexOf(selectField);
                Boolean moveUpEnabled = (selectionIndex > 0);
                Boolean moveDownEnabled = (selectionIndex < grid1.getStore().getCount() - 1);
                moveUpFieldButton.setEnabled(moveUpEnabled);
                moveDownFieldButton.setEnabled(moveDownEnabled);
            }
        });

        grid2.getSelectionModel().addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent<MatchFieldWeb>>()
                {
                    public void handleEvent(SelectionChangedEvent<MatchFieldWeb> be) {
                        currentSelection = be.getSelectedItem();
                        Boolean moveUpEnabled = false;
                        Boolean moveDownEnabled = false;
                        if (currentSelection != null) {
                            int selectionIndex = grid2.getStore().indexOf(currentSelection);
                            moveUpEnabled = (selectionIndex > 0);
                            moveDownEnabled = (selectionIndex < grid2.getStore().getCount() - 1);
                            grid1.getSelectionModel().select(currentSelection, false);
                        } else {
                            grid1.getSelectionModel().deselectAll();
                        }
                        moveUpFieldAdvanButton.setEnabled(moveUpEnabled);
                        moveDownFieldAdvanButton.setEnabled(moveDownEnabled);
                    }
                });

        grid2.addListener(Events.SortChange, new Listener<GridEvent>()
        {
            public void handleEvent(GridEvent be) {
                // Info.display("Information", "SortChange.");
                MatchFieldWeb selectField = grid2.getSelectionModel().getSelectedItem();

                int selectionIndex = grid2.getStore().indexOf(selectField);
                Boolean moveUpEnabled = (selectionIndex > 0);
                Boolean moveDownEnabled = (selectionIndex < grid2.getStore().getCount() - 1);
                moveUpFieldAdvanButton.setEnabled(moveUpEnabled);
                moveDownFieldAdvanButton.setEnabled(moveDownEnabled);
            }
        });

        gridVector.addListener(Events.CellMouseDown, new Listener<GridEvent<BaseModelData>>()
        {
            public void handleEvent(GridEvent<BaseModelData> be) {
                int index = be.getColIndex();
                boolean val = false;
                if (index < 3) {
                    // Info.display("Cell clicked: ",be.getRowIndex()+" "+be.getColIndex());
                    BaseModelData selection = be.getModel();
                    int matchDefault = (Integer) selection.get("matchDefault");
                    switch (index) {
                    case 0:
                        if (matchDefault != Constants.MATCH_CLASSIFICATION) {
                            setMatchValue(selection, "match", "probable", "nonmatch", 2, 3);
                        }
                        break;
                    case 1:
                        if (matchDefault != Constants.PROBABLE_MATCH_CLASSIFICATION) {
                            setMatchValue(selection, "probable", "match", "nonmatch", 1, 3);
                        }
                        break;
                    case 2:
                        if (matchDefault != Constants.NON_MATCH_CLASSIFICATION) {
                            setMatchValue(selection, "nonmatch", "match", "probable", 1, 2);
                        }
                        break;
                    }
                    // refresh the selection to let GridCellRenderer triggered
                    vectorStore.update(selection);

                    // Info.display("Cell clicked: ", ""+val);
                }
            }
        });

        gridVector.addListener(Events.RowDoubleClick, new Listener<GridEvent<BaseModelData>>()
        {
            public void handleEvent(GridEvent<BaseModelData> be) {

                selectedVector = be.getGrid().getSelectionModel().getSelectedItem();
                if (selectedVector != null) {
                    Integer val = (Integer) selectedVector.get("vector");

                    if (Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL) != null) {

                        leftIdentifierStore.removeAll();
                        rightIdentifierStore.removeAll();
                        linkPairStore.removeAll();

                        buildVectorDetailInfoDialog(currentEntity);
                        vectorDetailInfoDialog.show();

                        // controller.handleEvent(new AppEvent(AppEvents.MatchConfigurationGetLoggedLinksForVector,
                        // val));
                        searchCriteria = new LoggedLinkSearchCriteriaWeb();
                        searchCriteria.setVector(val);
                        searchCriteria.setFirstResult(new Integer(0));
                        searchCriteria.setMaxResults(PAGE_SIZE);
                        searchCriteria.setEntityModel(currentEntity);

                        PagingLoadConfig config = new BasePagingLoadConfig();
                        config.setOffset(0);
                        config.setLimit(PAGE_SIZE);

                        pagingLoader.load(config);
                    }
                }
            }
        });

        Menu contextMenu = new Menu();
        MenuItem menuItemDetail = new MenuItem("Details for the vector", IconHelper.create("images/page.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        selectedVector = gridVector.getSelectionModel().getSelectedItem();
                        if (selectedVector != null) {
                            Integer val = (Integer) selectedVector.get("vector");

                            if (Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL) != null) {

                                leftIdentifierStore.removeAll();
                                rightIdentifierStore.removeAll();
                                linkPairStore.removeAll();

                                buildVectorDetailInfoDialog(currentEntity);
                                vectorDetailInfoDialog.show();

                                // controller.handleEvent(new
                                // AppEvent(AppEvents.MatchConfigurationGetLoggedLinksForVector, val));
                                searchCriteria = new LoggedLinkSearchCriteriaWeb();
                                searchCriteria.setVector(val);
                                searchCriteria.setFirstResult(new Integer(0));
                                searchCriteria.setMaxResults(PAGE_SIZE);
                                searchCriteria.setEntityModel(currentEntity);

                                PagingLoadConfig config = new BasePagingLoadConfig();
                                config.setOffset(0);
                                config.setLimit(PAGE_SIZE);

                                pagingLoader.load(config);
                            }
                        }
                    }
                });
        contextMenu.add(menuItemDetail);
        gridVector.setContextMenu(contextMenu);

        ContentPanel cp = new ContentPanel();
        cp.setHeading("Probabilistic Matching Field Configuration");
        cp.setFrame(true);
        cp.setIcon(IconHelper.create("images/table_gear.png"));
        cp.setSize(810, 530);

        // Tabs
        TabPanel tabPanel = new TabPanel();
        tabPanel.setBorders(false);
        tabPanel.setBodyBorder(false);
        tabPanel.setHeight(490);

        TabItem attributeDataPart1Tab = new TabItem("Basic");
        attributeDataPart1Tab.setLayout(new FitLayout());
        attributeDataPart1Tab.add(basicPanel());
        attributeDataPart1Tab.addListener(Events.Select, new Listener<ComponentEvent>()
        {
            public void handleEvent(ComponentEvent be) {
                if (currentSelection != null) {
                    grid1.getView().getRow(currentSelection).scrollIntoView();
                }
            }
        });

        TabItem attributeDataPart2Tab = new TabItem("Advanced");
        attributeDataPart2Tab.setLayout(new FitLayout());
        attributeDataPart2Tab.add(advancedPanel());
        attributeDataPart2Tab.addListener(Events.Select, new Listener<ComponentEvent>()
        {
            public void handleEvent(ComponentEvent be) {
                if (currentSelection != null) {
                    grid2.getView().getRow(currentSelection).scrollIntoView();
                }
            }
        });

        TabItem attributeDataPart3Tab = new TabItem("Logging");
        attributeDataPart3Tab.setLayout(new FitLayout());
        attributeDataPart3Tab.add(loggingPanel());
        attributeDataPart3Tab.addListener(Events.Select, new Listener<ComponentEvent>()
        {
            public void handleEvent(ComponentEvent be) {
                if (currentSelection != null) {
                }
            }
        });

        TabItem attributeDataPart4Tab = new TabItem("Vector Selection");
        attributeDataPart4Tab.setLayout(new FitLayout());
        attributeDataPart4Tab.add(VectorSelectionPanel());
        attributeDataPart4Tab.addListener(Events.Select, new Listener<ComponentEvent>()
        {
            public void handleEvent(ComponentEvent be) {
                if (currentSelection != null) {
                    gridVector.getView().getRow(currentSelection).scrollIntoView();
                }
            }
        });

        tabPanel.add(attributeDataPart1Tab);
        tabPanel.add(attributeDataPart2Tab);
        tabPanel.add(attributeDataPart3Tab);
        tabPanel.add(attributeDataPart4Tab);

        cp.add(tabPanel);
        container.add(cp);

        LayoutContainer wrapper = (LayoutContainer) Registry.get(Constants.CENTER_PANEL);
        wrapper.removeAll();
        wrapper.add(container);
        wrapper.layout();
        GWT.log("Done Initializing the UI in " + (new java.util.Date().getTime() - time), null);
    }

    private void setMatchValue(BaseModelData selection, String match, String matchOther1, String matchOther2,
            int other1, int other2) {

        int matchDefault = (Integer) selection.get("matchDefault");
        boolean val = (Boolean) selection.get(match);

        if (val == true) {
            selection.set(match, false);
        } else {
            selection.set(match, true);
            if (matchDefault != other1) {
                selection.set(matchOther1, false);
            }
            if (matchDefault != other2) {
                selection.set(matchOther2, false);
            }
        }
    }

    private ContentPanel basicPanel() {
        ContentPanel cpBasic = new ContentPanel();
        cpBasic.setFrame(true);
        cpBasic.setHeaderVisible(false);
        FormLayout formLayoutBasic = new FormLayout();
        formLayoutBasic.setLabelWidth(160);
        formLayoutBasic.setDefaultWidth(280);
        cpBasic.setLayout(formLayoutBasic);
        cpBasic.setSize(750, 480);

        LayoutContainer buttonContainer = new LayoutContainer();
        buttonContainer.setHeight(24);
        buttonContainer.setLayout(new ColumnLayout());
        addFieldButton = new Button("Add", IconHelper.create("images/folder_go.png"),
                new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        // Make sure we are starting with a clean slate
                        addOrEditFieldMode = true;
                        addEditMatchFieldDialog.show();

                        attributeNameCombo.clearSelections();
                        comparatorFuncNameCombo.clearSelections();
                        matchThresholdEdit.clear();
                    }
                });
        buttonContainer.add(addFieldButton);
        editFieldButton = new Button("Edit", IconHelper.create("images/folder_edit.png"),
                new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        addOrEditFieldMode = false;
                        MatchFieldWeb editField = grid1.getSelectionModel().getSelectedItem();
                        if (editField == null) {
                            Info.display("Information",
                                    "You must first select a field to be edited before pressing the \"Edit Field\" button.");
                            return;
                        }
                        addEditMatchFieldDialog.show();
                        editedFieldIndex = grid1.getStore().indexOf(editField);
                        editedField = editField;

                        attributeNameCombo.setValue(new ModelPropertyWeb(editField.getFieldName(), editField
                                .getFieldDescription()));
                        comparatorFuncNameCombo.setValue(new ModelPropertyWeb(editField.getComparatorFunctionName(),
                                editField.getComparatorFunctionNameDescription()));
                        matchThresholdEdit.setValue(editField.getMatchThreshold());
                    }
                });

        buttonContainer.add(editFieldButton);
        removeFieldButton = new Button("Remove", IconHelper.create("images/folder_delete.png"),
                new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        MatchFieldWeb removeField = grid1.getSelectionModel().getSelectedItem();
                        if (removeField == null) {
                            Info.display("Information",
                                    "You must first select a field to be deleted before pressing the \"Remove Round\" button.");
                            return;
                        }
                        grid1.getStore().remove(removeField);

                        // Reset Vectors after remove a field
                        dualListFrom.removeAll();
                        dualListTo.removeAll();
                        currentFieldSize--;

                        int numberOfVectors = 0;
                        if (currentFieldSize > 0) {
                            numberOfVectors = (int) Math.pow(2, currentFieldSize);
                        }
                        Set<Integer> vectors = currentConfig.getLoggingVectors();
                        for (int i = 0; i < numberOfVectors; i++) {
                            if (vectors != null && vectors.contains(i)) {
                                dualListTo.add(new VectorWeb(Utility.prefixWithZeros(Integer.toBinaryString(i),
                                        currentFieldSize), i));
                            } else {
                                dualListFrom.add(new VectorWeb(Utility.prefixWithZeros(Integer.toBinaryString(i),
                                        currentFieldSize), i));
                            }
                        }
                    }
                });
        buttonContainer.add(removeFieldButton);
        moveUpFieldButton = new Button("Move Up", IconHelper.create("images/arrow_up.png"),
                new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        if (grid1.getStore().getCount() > 1) {
                            MatchFieldWeb field = grid1.getSelectionModel().getSelectedItem();
                            if (field == null) {
                                Info.display("Information",
                                        "You must first select a field before pressing the \"Move Up\" button.");
                                return;
                            }
                            grid1.getSelectionModel().selectPrevious(false);
                        }
                    }
                });
        buttonContainer.add(moveUpFieldButton);
        moveDownFieldButton = new Button("Move Down", IconHelper.create("images/arrow_down.png"),
                new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        if (grid1.getStore().getCount() > 1) {
                            MatchFieldWeb field = grid1.getSelectionModel().getSelectedItem();
                            if (field == null) {
                                Info.display("Information",
                                        "You must first select a field before pressing the \"Move Down\" button.");
                                return;
                            }
                            grid1.getSelectionModel().selectNext(false);
                        }
                    }
                });
        buttonContainer.add(moveDownFieldButton);

        LayoutContainer c = new LayoutContainer();
        HBoxLayout layout = new HBoxLayout();
        layout.setPadding(new Padding(5));
        layout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
        layout.setPack(BoxLayoutPack.CENTER);
        c.setLayout(layout);

        HBoxLayoutData layoutData = new HBoxLayoutData(new Margins(0, 5, 0, 0));

        c.add(new Button("Save Settings", IconHelper.create("images/folder_go.png"),
                new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce) {

                        // Basic Tab
                        // ** if ( falseNegativeProbabilityEdit.isValid() && falsePositiveProbabilityEdit.isValid() ) {

                        // Advanced Tab
                        if (lowerBoundEdit.isValid() && upperBoundEdit.isValid() && pValueEdit.isValid()
                                && maxIterationsEdit.isValid() && convergenceErrorEdit.isValid() 
                                && initialMValueEdit.isValid() && initialUValueEdit.isValid() && initialPValueEdit.isValid()) {

                            // Logging Tab
                            if (!loggingVectorfieldSet.isExpanded() || loggingVectorfieldSet.isExpanded()
                                    && vectorsFractionSpin.isValid()) {

                                if (!loggingWeightfieldSet.isExpanded()
                                        || loggingWeightfieldSet.isExpanded()
                                        && weightUpperBoundEdit.isValid()
                                        && weightLowerBoundEdit.isValid()
                                        && weightFractionSpin.isValid()
                                        && weightUpperBoundEdit.getValue().doubleValue() >= weightLowerBoundEdit
                                                .getValue().doubleValue()) {

                                    // Basic
                                    MatchConfigurationWeb matchConfig = new MatchConfigurationWeb();
                                    matchConfig.setEntityName(entityName);
                                    matchConfig.setFalseNegativeProbability(new Float(0.1));
                                    matchConfig.setFalsePositiveProbability(new Float(0.9));
                                    matchConfig.setConfigFileDirectory(configFileDirectoryEdit.getValue());

                                    // Advanced
                                    matchConfig.setLowerBound(lowerBoundEdit.getValue().doubleValue());
                                    matchConfig.setUpperBound(upperBoundEdit.getValue().doubleValue());
                                    matchConfig.setPValue(pValueEdit.getValue().doubleValue());
                                    matchConfig.setInitialMValue(initialMValueEdit.getValue().doubleValue());
                                    matchConfig.setInitialUValue(initialUValueEdit.getValue().doubleValue());
                                    matchConfig.setInitialPValue(initialPValueEdit.getValue().doubleValue());
                                    matchConfig.setMaxIterations(maxIterationsEdit.getValue().intValue());
                                    matchConfig.setConvergenceError(convergenceErrorEdit.getValue().doubleValue());

                                    List<MatchFieldWeb> matchFieldsConfig = grid1.getStore().getModels();
                                    /*
                                     * for (MatchFieldWeb field : matchFieldsConfig) { Info.display("Information name",
                                     * field.getFieldName()); Info.display("Information c function",
                                     * field.getComparatorFunctionName()); Info.display("Information m Value",
                                     * ""+field.getMValue()); }
                                     */
                                    matchConfig.setMatchFields(matchFieldsConfig);

                                    // Logging
                                    if (loggingVectorfieldSet.isExpanded()) {
                                        matchConfig.setLoggingByVectors(true);

                                        Set<Integer> vectors = new HashSet<Integer>();
                                        for (VectorWeb vector : dualListTo.getModels()) {
                                            Integer value = vector.getValue();
                                            // Info.display("Information vector", value.toString());
                                            vectors.add(value);
                                        }
                                        matchConfig.setLoggingVectors(vectors);

                                        matchConfig.setLoggingByVectorsFraction(vectorsFractionSpin.getValue()
                                                .doubleValue() / 100);
                                    }

                                    if (loggingWeightfieldSet.isExpanded()) {

                                        matchConfig.setLoggingByWeight(true);

                                        matchConfig.setLoggingByWeightLowerBound(weightLowerBoundEdit.getValue()
                                                .doubleValue());
                                        matchConfig.setLoggingByWeightUpperBound(weightUpperBoundEdit.getValue()
                                                .doubleValue());
                                        matchConfig.setLoggingByWeightFraction(weightFractionSpin.getValue()
                                                .doubleValue() / 100);
                                    }

                                    List<ModelPropertyWeb> loggingDestinations = loggingDestinationCombo.getSelection();
                                    if (loggingDestinations.size() > 0) {
                                        ModelPropertyWeb loggingDestination = loggingDestinations.get(0);
                                        matchConfig.setLoggingDestination(loggingDestination.getName());
                                    }

                                    // Vector Selection
                                    List<VectorConfigurationWeb> vectorConfs = new ArrayList<VectorConfigurationWeb>();
                                    ;
                                    List<BaseModelData> vectorModelConfigs = gridVector.getStore().getModels();

                                    // Info.display("Number Of Vectors", ""+vectorModelConfigs.size());
                                    for (BaseModelData vectorModelConf : vectorModelConfigs) {

                                        VectorConfigurationWeb vectorConf = new VectorConfigurationWeb();
                                        Integer algorithmClassification = vectorModelConf.get("matchDefault");
                                        vectorConf.setAlgorithmClassification(algorithmClassification);

                                        Integer manualClassification = new Integer(algorithmClassification.intValue());
                                        Boolean match = vectorModelConf.get("match");
                                        Boolean probable = vectorModelConf.get("probable");
                                        Boolean nonmatch = vectorModelConf.get("nonmatch");

                                        switch (algorithmClassification) {
                                        case Constants.MATCH_CLASSIFICATION:
                                            if (probable) {
                                                manualClassification = new Integer(2);
                                            }
                                            if (nonmatch) {
                                                manualClassification = new Integer(3);
                                            }
                                            break;
                                        case Constants.PROBABLE_MATCH_CLASSIFICATION:
                                            if (match) {
                                                manualClassification = new Integer(1);
                                            }
                                            if (nonmatch) {
                                                manualClassification = new Integer(3);
                                            }
                                            break;
                                        case Constants.NON_MATCH_CLASSIFICATION:

                                            if (match) {
                                                manualClassification = new Integer(1);
                                            }
                                            if (probable) {
                                                manualClassification = new Integer(2);
                                            }
                                            break;
                                        }
                                        vectorConf.setManualClassification(manualClassification);

                                        String weight = vectorModelConf.get("weight");
                                        vectorConf.setWeight(Double.parseDouble(weight));

                                        vectorConf.setVectorValue((Integer) vectorModelConf.get("vector"));

                                        vectorConfs.add(vectorConf);
                                    }
                                    matchConfig.setVectorConfigurations(vectorConfs);

                                    currentConfig = matchConfig;
                                    controller.handleEvent(new AppEvent(AppEvents.MatchConfigurationSave, matchConfig));
                                } else {
                                    if (!weightUpperBoundEdit.isValid() || !weightLowerBoundEdit.isValid()
                                            || !weightFractionSpin.isValid()) {

                                        MessageBox.alert("Information",
                                                "Some fields in Logging By Weight Values are not valid", null);

                                    } else if (weightUpperBoundEdit.getValue().doubleValue() < weightLowerBoundEdit
                                            .getValue().doubleValue()) {

                                        MessageBox
                                                .alert("Information",
                                                        "Weight Upper Bound should be great than or equal to Weight Lower Bound",
                                                        null);
                                    }
                                }
                            } else {
                                MessageBox.alert("Information",
                                        "Vectors Fraction field in Logging By Vector Values are not valid", null);
                            }
                        } else {
                            MessageBox.alert("Information", "Some fields in Advanced Tab are not valid", null);
                        }
                    }
                    // ** }
                }), layoutData);

        cpBasic.setBottomComponent(c);

        configFileDirectoryEdit.setFieldLabel("Configure File Directory");
        cpBasic.add(configFileDirectoryEdit);

        LabelField space = new LabelField("");
        cpBasic.add(space);

        cpBasic.add(buttonContainer);

        cpBasic.add(grid1);

        Status status = new Status();
        status.setText("");
        status.setWidth(20);
        cpBasic.add(status);

        return cpBasic;
    }

    private ContentPanel advancedPanel() {
        ContentPanel cpAdvan = new ContentPanel();
        cpAdvan.setFrame(true);
        cpAdvan.setHeaderVisible(false);
        FormLayout formLayoutAdvan = new FormLayout();
        formLayoutAdvan.setLabelWidth(160);
        formLayoutAdvan.setDefaultWidth(280);
        cpAdvan.setLayout(formLayoutAdvan);
        cpAdvan.setSize(750, 450);

        lowerBoundEdit = new NumberField();
        lowerBoundEdit.setFieldLabel("Lower Bound");
        lowerBoundEdit.setMinValue(-50);
        lowerBoundEdit.setMaxValue(50);
        HBoxLayoutData dataLayoutFirst = new HBoxLayoutData(new Margins(5, 0, 0, 0));
        cpAdvan.add(lowerBoundEdit, dataLayoutFirst);

        upperBoundEdit = new NumberField();
        upperBoundEdit.setFieldLabel("Upper Bound");
        upperBoundEdit.setAllowBlank(false);
        upperBoundEdit.setMinValue(-50);
        upperBoundEdit.setMaxValue(50);
        cpAdvan.add(upperBoundEdit);

        pValueEdit = new NumberField();
        pValueEdit.setFieldLabel("p-Value");
        pValueEdit.setAllowBlank(false);
        pValueEdit.setMinValue(0);
        pValueEdit.setMaxValue(1);
        cpAdvan.add(pValueEdit);

        initialMValueEdit = new NumberField();
        initialMValueEdit.setFieldLabel("Initial m-Value");
        initialMValueEdit.setAllowBlank(false);
        initialMValueEdit.setMinValue(-100);
        initialMValueEdit.setMaxValue(100);
        cpAdvan.add(initialMValueEdit);

        initialUValueEdit = new NumberField();
        initialUValueEdit.setFieldLabel("Initial u-Value");
        initialUValueEdit.setAllowBlank(false);
        initialUValueEdit.setMinValue(-100);
        initialUValueEdit.setMaxValue(100);
        cpAdvan.add(initialUValueEdit);

        initialPValueEdit = new NumberField();
        initialPValueEdit.setFieldLabel("Initial p-Value");
        initialPValueEdit.setAllowBlank(false);
        initialPValueEdit.setMinValue(0);
        initialPValueEdit.setMaxValue(1);
        cpAdvan.add(initialPValueEdit);

        maxIterationsEdit = new NumberField();
        maxIterationsEdit.setAllowBlank(false);
        maxIterationsEdit.setFieldLabel("Maximum Iterations");
        maxIterationsEdit.setMinValue(0);
        maxIterationsEdit.setMaxValue(100);
        cpAdvan.add(maxIterationsEdit);

        convergenceErrorEdit = new NumberField();
        convergenceErrorEdit.setFieldLabel("Convergence Error");
        convergenceErrorEdit.setAllowBlank(false);
        convergenceErrorEdit.setMinValue(0.000001);
        convergenceErrorEdit.setMaxValue(0.9);
        HBoxLayoutData dataLayoutLast = new HBoxLayoutData(new Margins(0, 0, 2, 0));
        cpAdvan.add(convergenceErrorEdit, dataLayoutLast);

        ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        cp.setLayout(new FormLayout());
        cp.setSize(580, 205);

        LayoutContainer buttonContainer = new LayoutContainer();
        buttonContainer.setHeight(24);
        buttonContainer.setLayout(new ColumnLayout());

        moveUpFieldAdvanButton = new Button("Move Up", IconHelper.create("images/arrow_up.png"),
                new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        if (grid2.getStore().getCount() > 1) {
                            MatchFieldWeb field = grid2.getSelectionModel().getSelectedItem();
                            if (field == null) {
                                Info.display("Information",
                                        "You must first select a field before pressing the \"Move Up\" button.");
                                return;
                            }
                            grid2.getSelectionModel().selectPrevious(false);
                        }
                    }
                });
        buttonContainer.add(moveUpFieldAdvanButton);
        moveDownFieldAdvanButton = new Button("Move Down", IconHelper.create("images/arrow_down.png"),
                new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        if (grid2.getStore().getCount() > 1) {
                            MatchFieldWeb field = grid2.getSelectionModel().getSelectedItem();
                            if (field == null) {
                                Info.display("Information",
                                        "You must first select a field before pressing the \"Move Down\" button.");
                                return;
                            }
                            grid2.getSelectionModel().selectNext(false);
                        }
                    }
                });
        buttonContainer.add(moveDownFieldAdvanButton);

        cp.add(buttonContainer);
        cp.add(grid2);

        cpAdvan.setBottomComponent(cp);
        return cpAdvan;
    }

    private ContentPanel loggingPanel() {
        ContentPanel cpLogging = new ContentPanel();
        cpLogging.setFrame(true);
        cpLogging.setHeaderVisible(false);
        FormLayout formLayoutAdvan = new FormLayout();
        formLayoutAdvan.setLabelWidth(140);
        formLayoutAdvan.setDefaultWidth(180);
        cpLogging.setLayout(formLayoutAdvan);
        cpLogging.setSize(750, 450);

        cpLogging.add(setupLoggingVectorfieldSet(0));

        // LabelField space = new LabelField("");
        // cpLogging.add(space);

        cpLogging.add(setupLoggingWeightfieldSet(2));

        loggingDestinationCombo = new ComboBox<ModelPropertyWeb>();
        loggingDestinationCombo.setEmptyText("Select attribute...");
        loggingDestinationCombo.setForceSelection(true);
        // attributeNameCombo.setDisplayField("name");
        loggingDestinationCombo.setDisplayField("description");
        loggingDestinationCombo.setStore(loggingDestinationStore);
        loggingDestinationCombo.setTypeAhead(true);
        loggingDestinationCombo.setTriggerAction(TriggerAction.ALL);
        loggingDestinationCombo.setFieldLabel("Logging Destination");
        cpLogging.add(loggingDestinationCombo);

        return cpLogging;
    }

    private ContentPanel VectorSelectionPanel() {
        ContentPanel cpVectorSelection = new ContentPanel();
        cpVectorSelection.setFrame(true);
        cpVectorSelection.setHeaderVisible(false);
        FormLayout formLayoutAdvan = new FormLayout();
        formLayoutAdvan.setLabelWidth(60);
        formLayoutAdvan.setDefaultWidth(280);
        cpVectorSelection.setLayout(formLayoutAdvan);
        cpVectorSelection.setSize(750, 480);

        cpVectorSelection.add(gridVector);

        return cpVectorSelection;
    }

    private FieldSet setupLoggingVectorfieldSet(int tabIndex) {
        loggingVectorfieldSet = new FieldSet();
        loggingVectorfieldSet.setHeading(" Logging By Vector Values ");
        loggingVectorfieldSet.setCheckboxToggle(true);
        loggingVectorfieldSet.setCollapsible(true);
        loggingVectorfieldSet.setBorders(true);

        FormLayout loggingVectorlayout = new FormLayout();
        loggingVectorlayout.setLabelWidth(130);
        loggingVectorlayout.setDefaultWidth(380); // It is the real function to set the textField width
        loggingVectorfieldSet.setLayout(loggingVectorlayout);

        // Field listeners
        loggingVectorfieldSet.addListener(Events.Collapse, new Listener<FieldSetEvent>()
        {
            public void handleEvent(FieldSetEvent be) {
                // Info.display("test", "Check Collapse");
            }
        });

        loggingVectorfieldSet.addListener(Events.Expand, new Listener<FieldSetEvent>()
        {
            public void handleEvent(FieldSetEvent be) {
                // Info.display("test", "Check Expand");
            }
        });

        DualListField lists = new DualListField();
        lists.setFieldLabel("Vectors");
        lists.setHeight(200);

        ListField from = lists.getFromList();
        from.setDisplayField("name");
        from.setStore(dualListFrom);

        ListField to = lists.getToList();
        to.setDisplayField("name");
        to.setStore(dualListTo);

        loggingVectorfieldSet.add(lists);

        ContentPanel spinPanel = new ContentPanel();
        spinPanel.setFrame(false);
        spinPanel.setHeaderVisible(false);
        FormLayout formLayoutAdvan = new FormLayout();
        formLayoutAdvan.setLabelWidth(130);
        formLayoutAdvan.setDefaultWidth(60);
        spinPanel.setLayout(formLayoutAdvan);

        vectorsFractionSpin = new SpinnerField();
        vectorsFractionSpin.setAllowDecimals(false);
        vectorsFractionSpin.setAllowNegative(false);
        vectorsFractionSpin.setFieldLabel("% of Pairs Logged ");
        vectorsFractionSpin.setMinValue(1);
        vectorsFractionSpin.setMaxValue(100);
        vectorsFractionSpin.setWidth(20);

        spinPanel.add(vectorsFractionSpin);

        loggingVectorfieldSet.add(spinPanel);

        return loggingVectorfieldSet;
    }

    private FieldSet setupLoggingWeightfieldSet(int tabIndex) {
        loggingWeightfieldSet = new FieldSet();
        loggingWeightfieldSet.setHeading(" Logging By Weight Values ");
        loggingWeightfieldSet.setCheckboxToggle(true);
        loggingWeightfieldSet.setCollapsible(true);
        loggingWeightfieldSet.setBorders(true);

        FormLayout loggingWeightlayout = new FormLayout();
        loggingWeightlayout.setLabelWidth(130);
        loggingWeightlayout.setDefaultWidth(380); // It is the real function to set the textField width
        loggingWeightfieldSet.setLayout(loggingWeightlayout);

        weightLowerBoundEdit = new NumberField();
        weightLowerBoundEdit.setFieldLabel("Weight Lower Bound");
        weightLowerBoundEdit.setMinValue(-50);
        weightLowerBoundEdit.setMaxValue(50);
        loggingWeightfieldSet.add(weightLowerBoundEdit);

        weightUpperBoundEdit = new NumberField();
        weightUpperBoundEdit.setFieldLabel("Weight Upper Bound");
        weightUpperBoundEdit.setMinValue(-50);
        weightUpperBoundEdit.setMaxValue(50);
        loggingWeightfieldSet.add(weightUpperBoundEdit);

        ContentPanel spinPanel = new ContentPanel();
        spinPanel.setFrame(false);
        spinPanel.setHeaderVisible(false);
        FormLayout formLayoutAdvan = new FormLayout();
        formLayoutAdvan.setLabelWidth(130);
        formLayoutAdvan.setDefaultWidth(60);
        spinPanel.setLayout(formLayoutAdvan);

        weightFractionSpin = new SpinnerField();
        weightFractionSpin.setAllowDecimals(false);
        weightFractionSpin.setAllowNegative(false);
        weightFractionSpin.setFieldLabel("% of Pairs Logged ");
        weightFractionSpin.setMinValue(1);
        weightFractionSpin.setMaxValue(100);
        weightFractionSpin.setWidth(20);

        spinPanel.add(weightFractionSpin);

        loggingWeightfieldSet.add(spinPanel);

        return loggingWeightfieldSet;
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
        addEditMatchFieldDialog.setHeight(330);
        addEditMatchFieldDialog.setButtons(Dialog.OKCANCEL);
        // addEditMatchFieldDialog.setHideOnButtonClick(true);
        addEditMatchFieldDialog.setModal(true);
        addEditMatchFieldDialog.getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                List<ModelPropertyWeb> attribNameSelection = attributeNameCombo.getSelection();
                List<ModelPropertyWeb> compFuncNameSelection = comparatorFuncNameCombo.getSelection();

                if (matchThresholdEdit.getValue() != null // **&& agreementProbabilityEdit.getValue() != null &&
                                                          // disagreementProbabilityEdit.getValue() != null
                        && attribNameSelection.size() > 0 && compFuncNameSelection.size() > 0) {

                    if ( /** agreementProbabilityEdit.isValid() && disagreementProbabilityEdit.isValid() && **/
                    matchThresholdEdit.isValid()) {
                        ModelPropertyWeb attribNameField = attribNameSelection.get(0);
                        ModelPropertyWeb compFuncNameField = compFuncNameSelection.get(0);

                        MatchFieldWeb matchFieldWeb = new MatchFieldWeb();
                        matchFieldWeb.setFieldName(attribNameField.getName());
                        matchFieldWeb.setFieldDescription(attribNameField.getDescription());
                        matchFieldWeb.setAgreementProbability(new Float(0.9));
                        matchFieldWeb.setDisagreementProbability(new Float(0.1));
                        matchFieldWeb.setComparatorFunctionName(compFuncNameField.getName());
                        matchFieldWeb.setComparatorFunctionNameDescription(compFuncNameField.getDescription());
                        matchFieldWeb.setMatchThreshold(matchThresholdEdit.getValue().floatValue());

                        if (addOrEditFieldMode) { // Add
                            // check duplicate Field
                            for (MatchFieldWeb field : grid1.getStore().getModels()) {
                                if (field.getFieldName().equals(matchFieldWeb.getFieldName())) {
                                    MessageBox.alert("Information",
                                            "There is a duplicate matching field in the Configuration", null);
                                    return;
                                }
                            }

                            // set default values for m-Value and u-Value
                            if (initialMValueEdit.isValid()) {
                                matchFieldWeb.setMValue(initialMValueEdit.getValue().doubleValue());
                            } else {
                                matchFieldWeb.setMValue(new Double(0));
                            }

                            if (initialUValueEdit.isValid()) {
                                matchFieldWeb.setUValue(initialUValueEdit.getValue().doubleValue());
                            } else {
                                matchFieldWeb.setUValue(new Double(0));
                            }

                            grid1.getStore().add(matchFieldWeb);

                            // Reset Vectors after add a new field
                            dualListFrom.removeAll();
                            dualListTo.removeAll();
                            currentFieldSize++;

                            int numberOfVectors = 0;
                            if (currentFieldSize > 0) {
                                numberOfVectors = (int) Math.pow(2, currentFieldSize);
                            }
                            Set<Integer> vectors = currentConfig.getLoggingVectors();
                            for (int i = 0; i < numberOfVectors; i++) {
                                if (vectors != null && vectors.contains(i)) {
                                    dualListTo.add(new VectorWeb(Utility.prefixWithZeros(Integer.toBinaryString(i),
                                            currentFieldSize), i));
                                } else {
                                    dualListFrom.add(new VectorWeb(Utility.prefixWithZeros(Integer.toBinaryString(i),
                                            currentFieldSize), i));
                                }
                            }

                        } else { // Edit
                            for (MatchFieldWeb field : grid1.getStore().getModels()) {
                                if (field.getFieldName() != editedField.getFieldName()) {
                                    if (field.getFieldName().equals(matchFieldWeb.getFieldName())) {
                                        MessageBox.alert("Information",
                                                "There is a duplicate matching field in the Configuration", null);
                                        return;
                                    }
                                }
                            }

                            // keep values for m-Value and u-Value
                            matchFieldWeb.setMValue(editedField.getMValue());
                            matchFieldWeb.setUValue(editedField.getUValue());

                            grid1.getStore().remove(editedField);
                            grid1.getStore().insert(matchFieldWeb, editedFieldIndex);
                        }

                        addEditMatchFieldDialog.close();
                    }

                } else {
                    if (attribNameSelection.size() == 0) {
                        MessageBox.alert("Information", "Please select Attribute Name", null);
                        return;
                    }
                    if (matchThresholdEdit.getValue() == null) {
                        MessageBox.alert("Information", "Match Threshold is required.", null);
                        return;
                    }
                    if (compFuncNameSelection.size() == 0) {
                        MessageBox.alert("Information", "Please select Comparator Name", null);
                        return;
                    }
                }
            }
        });

        addEditMatchFieldDialog.getButtonById(Dialog.CANCEL).addSelectionListener(new SelectionListener<ButtonEvent>()
        {
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
        formLayout.setDefaultWidth(260);
        cp.setLayout(formLayout);
        cp.setSize(450, 220);

        attributeNameCombo.setEmptyText("Select attribute...");
        attributeNameCombo.setForceSelection(true);
        // attributeNameCombo.setDisplayField("name");
        attributeNameCombo.setDisplayField("description");
        attributeNameCombo.setStore(attributeNameStore);
        attributeNameCombo.setTypeAhead(true);
        attributeNameCombo.setTriggerAction(TriggerAction.ALL);
        attributeNameCombo.setFieldLabel("Attribute Name");
        cp.add(attributeNameCombo);

        matchThresholdEdit.setFieldLabel("Match Threshold");
        matchThresholdEdit.setMinValue(0);
        matchThresholdEdit.setMaxValue(1);
        matchThresholdEdit.setFormat(nfc);
        cp.add(matchThresholdEdit);

        comparatorFuncNameCombo.setEmptyText("Select function...");
        comparatorFuncNameCombo.setForceSelection(true);
        // comparatorFuncNameCombo.setDisplayField("name");
        comparatorFuncNameCombo.setDisplayField("description");
        comparatorFuncNameCombo.setStore(comparatorFuncNameStore);
        comparatorFuncNameCombo.setTypeAhead(true);
        comparatorFuncNameCombo.setTriggerAction(TriggerAction.ALL);
        comparatorFuncNameCombo.setFieldLabel("Comparator Name");
        cp.add(comparatorFuncNameCombo);

        addEditMatchFieldDialog.add(cp);
    }

    // messagedetailInfoDialog
    private void buildVectorDetailInfoDialog(EntityWeb entity) {

        vectorDetailInfoDialog = new Dialog();
        vectorDetailInfoDialog.setBodyBorder(false);
        // vectorDetailInfoDialog.setScrollMode(Scroll.AUTO);
        vectorDetailInfoDialog.setWidth(965);
        vectorDetailInfoDialog.setHeight(750);
        vectorDetailInfoDialog.setIcon(IconHelper.create("images/information.png"));
        vectorDetailInfoDialog.setHeading("Vector Detail Information");
        vectorDetailInfoDialog.setButtons(Dialog.OK);
        vectorDetailInfoDialog.setModal(true);
        vectorDetailInfoDialog.getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                // messagedetailInfoDialog.hide();
                vectorDetailInfoDialog.close();
            }
        });

        ContentPanel cp = new ContentPanel();
        cp.setFrame(true);
        cp.setHeaderVisible(false);
        FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(120);
        formLayout.setDefaultWidth(260);
        cp.setLayout(formLayout);
        cp.setSize(950, 720);

        // Rpc Proxy setup
        pagingToolBar = setupRpcProxy();

        // link logs
        pairStore.removeAll();
        pairGrid = setupGrid();

        // Form for display link pair
        LayoutContainer formContainer = new LayoutContainer();
        formContainer.setScrollMode(Scroll.AUTO);

        LayoutContainer pairGridContainer = new LayoutContainer();
        pairGridContainer.setBorders(false);
        TableLayout layout = new TableLayout(2);
        layout.setWidth("900"); // "100%"
        layout.setCellSpacing(5);
        layout.setCellVerticalAlign(VerticalAlignment.TOP);
        pairGridContainer.setLayout(layout);

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

            recordFieldMap = new HashMap<String, String>();
            // Attributes with no group
            for (EntityAttributeWeb entityAttribute : sortedEntityAttributes) {
                if (entityAttribute.getEntityAttributeGroup() == null) {
                    recordFieldMap.put(entityAttribute.getName(), entityAttribute.getDisplayName());
                }
            }

            // Attributes with group
            if (sortedAttributeGroups != null) {
                for (EntityAttributeGroupWeb attributeGroup : sortedAttributeGroups) {
                    for (EntityAttributeWeb entityAttribute : sortedEntityAttributes) {

                        if (entityAttribute.getEntityAttributeGroup() != null
                                && entityAttribute.getEntityAttributeGroup().getName().equals(attributeGroup.getName())) {
                            recordFieldMap.put(entityAttribute.getName(), entityAttribute.getDisplayName());
                        }
                    }

                }
            }
        }

        pairGridContainer.add(setupIdentifierGrid(leftIdentifierStore, "Left Record"));
        pairGridContainer.add(setupIdentifierGrid(rightIdentifierStore, "Right Record"));
        formContainer.add(pairGridContainer);
        formContainer.add(setupLinkPairPanel());

        cp.add(pairGrid);
        cp.add(pagingToolBar);

        LabelField space = new LabelField("");
        cp.add(space);
        cp.add(formContainer);

        vectorDetailInfoDialog.add(cp);
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
        GridCellRenderer<BaseModelData> colorRenderer = new GridCellRenderer<BaseModelData>()
        {
            public String render(BaseModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                    ListStore<BaseModelData> store, Grid<BaseModelData> grid) {

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

                if (valueLeft.equals(valueRight)) {

                    // non match field and no values
                    if (valueLeft.isEmpty()) {
                        backgroundColor = "lightgrey;";
                        if (nonmatchFieldMap.get(attributeName) != null) {
                            config.style = "background-color:" + backgroundColor + ";";
                            return null;
                        }
                    }
                    backgroundColor = "lightgreen";
                }

                // <span: background is set as long as the value is. <div background is set whole length.
                return "<div style='background-color:" + backgroundColor + "'>" + value + "</div>";
                // config.style = "background-color:"+backgroundColor+";";
                // return value;
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

    private PagingToolBar setupRpcProxy() {
        // Rpc Proxy
        proxy = new RpcProxy<PagingLoadResult<RecordLinkWeb>>()
        {

            @Override
            public void load(final Object loadConfig, final AsyncCallback<PagingLoadResult<RecordLinkWeb>> callback) {
                if (searchCriteria == null) {
                    callback.onSuccess(new BasePagingLoadResult<RecordLinkWeb>(null, 0, 0));
                    return;
                }

                // set page offset for searchCriteria
                searchCriteria.setFirstResult(((PagingLoadConfig) loadConfig).getOffset());

                getController().getEntityInstanceDataService().getLoggedLinks(searchCriteria,
                        new AsyncCallback<LoggedLinkListWeb>()
                        {
                            public void onFailure(Throwable caught) {

                                if (caught instanceof AuthenticationException) {

                                    if (vectorDetailInfoDialog.isVisible()) {
                                        vectorDetailInfoDialog.close();
                                    }

                                    Dispatcher.get().dispatch(AppEvents.Logout);
                                    return;
                                }
                                Dispatcher.forwardEvent(AppEvents.Error, caught);
                            }

                            public void onSuccess(LoggedLinkListWeb result) {
                                // Info.display("Information", "The records: " +result.getRecordPairs().size()
                                // +"; "+((PagingLoadConfig)loadConfig).getOffset());

                                // PagingLoadConfig configuration
                                callback.onSuccess(new BasePagingLoadResult<RecordLinkWeb>(result.getRecordPairs(),
                                        ((PagingLoadConfig) loadConfig).getOffset(), result.getTotalCount()));
                            }
                        });

            }
        };

        // Page loader
        pagingLoader = new BasePagingLoader<PagingLoadResult<RecordLinkWeb>>(proxy);
        pagingLoader.setRemoteSort(true);
        pagingLoader.addLoadListener(new LoadListener()
        {
            // After the loader be completely filled, remove the mask
            public void loaderLoad(LoadEvent le) {

                pairGrid.unmask();
            }
        });

        pairStore = new ListStore<RecordLinkWeb>(pagingLoader);

        PagingToolBar pagingToolBar = new PagingToolBar(PAGE_SIZE);
        pagingToolBar.bind(pagingLoader);
        return pagingToolBar;
    }

    private Grid<RecordLinkWeb> setupGrid() {

        // setup column configuration
        List<ColumnConfig> columnConfig = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig("dateCreated", "Date Created", 150);
        column.setDateTimeFormat(DateTimeFormat.getShortDateTimeFormat());
        columnConfig.add(column);

        // Weight
        column = new ColumnConfig("weight", "Weight", 100);
        column.setNumberFormat(nfc);
        columnConfig.add(column);

        // Link Source
        // column = new ColumnConfig( "linkSource", "Link Source", 120);

        // Created By
        column = new ColumnConfig("userCreatedBy.username", "Created By", 150);
        columnConfig.add(column);

        ColumnModel cm = new ColumnModel(columnConfig);
        cm.addHeaderGroup(0, 0, new HeaderGroupConfig("Vector Pairs For Vector " + selectedVector.get("vector"), 1, 4));
        Grid<RecordLinkWeb> grid = new Grid<RecordLinkWeb>(pairStore, cm);
        grid.setBorders(true);
        grid.setAutoWidth(true);
        grid.setStripeRows(true);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.setHeight(220);

        // selection event
        grid.getSelectionModel().addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent<RecordLinkWeb>>()
                {
                    public void handleEvent(SelectionChangedEvent<RecordLinkWeb> be) {
                        leftIdentifierStore.removeAll();
                        rightIdentifierStore.removeAll();
                        linkPairStore.removeAll();

                        RecordLinkWeb field = be.getSelectedItem();
                        if (field != null) {
                            // Info.display("Information: ", field.getLeftRecord().getEntityDefinitionName());

                            // non match fields
                            List<MatchFieldWeb> fields = (List<MatchFieldWeb>) currentConfig.getMatchFields();
                            nonmatchFieldMap.clear();
                            int val = (Integer) field.getVector();
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

                            displayLeftEntityIdentifier(field.getLeftRecord());
                            displayRightEntityIdentifier(field.getRightRecord());

                            for (String key : recordFieldMap.keySet()) {
                                BaseModelData model = new BaseModelData();
                                // model.set("attribute", key);
                                model.set("attribute", recordFieldMap.get(key));

                                EntityAttributeWeb attribute = currentEntity.findEntityAttributeByName(key);
                                AttributeDatatype type = AttributeDatatype.getById(attribute.getDatatype()
                                        .getDataTypeCd());

                                if (type == AttributeDatatype.DATE) {
                                    String date = Utility.DateToString((Date) field.getLeftRecord().get(key));
                                    model.set("leftRecord", date);
                                    date = Utility.DateToString((Date) field.getRightRecord().get(key));
                                    model.set("rightRecord", date);
                                } else {
                                    model.set("leftRecord", field.getLeftRecord().get(key));
                                    model.set("rightRecord", field.getRightRecord().get(key));
                                }
                                linkPairStore.add(model);
                            }
                        }
                    }
                });

        return grid;
    }

    static final Comparator<VectorConfigurationWeb> VECTOR_WEIGHT_DISPLAY_ORDER = new Comparator<VectorConfigurationWeb>()
    {
        public int compare(VectorConfigurationWeb ea1, VectorConfigurationWeb ea2) {
            int compareValue = 0;
            if (ea2.getWeight() > ea1.getWeight()) {
                compareValue = 1;
            }
            if (ea2.getWeight() < ea1.getWeight()) {
                compareValue = -1;
            }
            return compareValue;
        }
    };
}
