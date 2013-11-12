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
package org.openempi.webapp.client.mvc;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.openempi.webapp.client.model.EntityAttributeGroupWeb;
import org.openempi.webapp.client.model.EntityAttributeValidationParameterWeb;
import org.openempi.webapp.client.model.EntityAttributeValidationWeb;
import org.openempi.webapp.client.model.EntityAttributeWeb;
import org.openempi.webapp.client.model.ModelPropertyWeb;
import org.openempi.webapp.client.model.RecordWeb;
import org.openempi.webapp.client.ui.util.Utility;
import org.openempi.webapp.client.ui.util.AttributeDatatype;

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.store.ListStore;

import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.Info;

public class BaseEntityView extends View
{
    protected Map<String, Field<?>> attributeFieldMap;

    public BaseEntityView(Controller controller) {
        super(controller);
    }

    protected static final Comparator<EntityAttributeWeb> ATTRIBUTE_DISPLAY_ORDER = new Comparator<EntityAttributeWeb>()
    {
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

    protected static final Comparator<EntityAttributeGroupWeb> GROUP_DISPLAY_ORDER = new Comparator<EntityAttributeGroupWeb>()
    {
        public int compare(EntityAttributeGroupWeb ea1, EntityAttributeGroupWeb ea2) {
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

    @Override
    protected void handleEvent(AppEvent event) {

    }

    protected void displayEntityRecord(Map<String, Field<?>> fieldMap, RecordWeb record) {
        Map<String, Object> map = record.getProperties();

        /*
         * for (Object key : map.keySet()) { Info.display("Information", "attribute: " +key.toString()
         * +"; "+map.get(key)); }
         */
        for (Object key : map.keySet()) {

            Field<?> field = fieldMap.get(key.toString());

            if (field != null) {
                if (field instanceof ComboBox) {

                    ComboBox combo = (ComboBox) field;
                    if (map.get(key) != null) {
                        combo.setValue(new ModelPropertyWeb(map.get(key).toString()));
                    }
                    continue;
                }

                if (field instanceof CheckBoxGroup) {
                    // Info.display("Information", "attribute: " +key.toString() +"; "+map.get(key));
                    CheckBoxGroup checkGroup = (CheckBoxGroup) field;
                    ((CheckBox) checkGroup.get(0)).setValue((Boolean) map.get(key));
                    continue;
                }

                // DateField, NumberField, TextField
                if (field instanceof TextField) {
                    TextField text = (TextField) field;
                    text.setValue(map.get(key));
                }
            }
        }
    }

    protected boolean isAllNullValueFromGUI(Map<String, Field<?>> fieldMap) {

        boolean isAllNull = true;
        for (Map.Entry<String, Field<?>> entry : fieldMap.entrySet()) {

            Field<?> field = entry.getValue();
            if (field.getValue() != null) {
                isAllNull = false;
            }
        }

        return isAllNull;
    }

    protected RecordWeb getEntityFromGUI(Map<String, Field<?>> fieldMap) {

        RecordWeb record = new RecordWeb();

        java.util.HashMap<String, Object> map = new java.util.HashMap<String, Object>();
        for (Map.Entry<String, Field<?>> entry : fieldMap.entrySet()) {

            String attributeName = entry.getKey();
            Field<?> field = entry.getValue();
            if (field instanceof ComboBox) {
                if (field.getValue() != null) {
                    // Info.display("Attribute:", attributeName +"; "+((ModelPropertyWeb)field.getValue()).getName());
                    map.put(attributeName, ((ModelPropertyWeb) field.getValue()).getName());
                } else {
                    map.put(attributeName, null);
                }
            } else if (field instanceof CheckBoxGroup) {
                if (field.getValue() != null) {
                    map.put(attributeName, true);
                } else {
                    map.put(attributeName, false);
                }
            } else {
                // Info.display("Attribute:", attributeName +"; "+field.getValue());
                map.put(attributeName, field.getValue());
            }
        }
        record.setProperties(map);

        return record;
    }

    protected RecordWeb getEntityFromGUI(RecordWeb record, Map<String, Field<?>> fieldMap) {

        java.util.Map<String, Object> map = record.getProperties();
        for (Map.Entry<String, Field<?>> entry : fieldMap.entrySet()) {

            String attributeName = entry.getKey();
            Field<?> field = entry.getValue();
            if (field instanceof ComboBox) {
                if (field.getValue() != null) {
                    // Info.display("Attribute:", attributeName +"; "+((ModelPropertyWeb)field.getValue()).getName());
                    map.put(attributeName, ((ModelPropertyWeb) field.getValue()).getName());
                } else {
                    map.put(attributeName, null);
                }
            } else if (field instanceof CheckBoxGroup) {
                if (field.getValue() != null) {
                    map.put(attributeName, true);
                } else {
                    map.put(attributeName, false);
                }
            } else {
                // Info.display("Attribute:", attributeName +"; "+field.getValue());
                map.put(attributeName, field.getValue());
            }
        }
        record.setProperties(map);
        return record;
    }

    protected void clearFormFields(Map<String, Field<?>> fieldMap) {
        for (Map.Entry<String, Field<?>> entry : fieldMap.entrySet()) {
            Field<?> field = entry.getValue();
            field.clear();
        }
    }

    protected FormPanel setupForm(String title, int labelWidth, int width) {

        FormPanel formPanel = new FormPanel();
        formPanel.setHeaderVisible(false);
        formPanel.setBodyBorder(false);
        formPanel.setLabelWidth(labelWidth);
        formPanel.setWidth(width);

        formPanel.setTitle(title);

        return formPanel;
    }

    protected void setStTextFieldValidation(TextField<?> fieldString, EntityAttributeWeb attribute) {
        for (EntityAttributeValidationWeb validation : attribute.getEntityAttributeValidations()) {

            if (validation.getValidationName().equals("nullityValidationRule")) {
                fieldString.setAllowBlank(false);
            } else {
                for (EntityAttributeValidationParameterWeb validationParameter : validation.getValidationParameters()) {

                    // Info.display("Information", "Validation: " + validationParameter.getParameterName()
                    // +", "+validationParameter.getParameterValue());
                    if (validationParameter.getParameterName().equals("minimumLength")) {
                        fieldString.setMinLength(Integer.parseInt(validationParameter.getParameterValue()));
                    }
                    if (validationParameter.getParameterName().equals("maximumLength")) {
                        fieldString.setMaxLength(Integer.parseInt(validationParameter.getParameterValue()));
                    }

                    if (validationParameter.getParameterName().equals("valueSet")) {
                        String valueList = validationParameter.getParameterValue();
                        String[] values = valueList.split(",");

                        ListStore<ModelPropertyWeb> modelStore = new ListStore<ModelPropertyWeb>();
                        for (String name : values) {
                            modelStore.add(new ModelPropertyWeb(name, Utility.convertToDescription(name)));
                        }
                        ((ComboBox) fieldString).setStore(modelStore);
                    }

                    if (validationParameter.getParameterName().equals("regularExpression")) {
                        fieldString.setRegex(validationParameter.getParameterValue());
                        fieldString.getMessages().setRegexText("Invalid " + attribute.getDisplayName() + " format");
                    }

                    if (validationParameter.getParameterName().equals("lowerRange")) {
                        ((NumberField) fieldString).setMinValue(Integer.parseInt(validationParameter
                                .getParameterValue()));
                    }
                    if (validationParameter.getParameterName().equals("upperRange")) {
                        ((NumberField) fieldString).setMaxValue(Integer.parseInt(validationParameter
                                .getParameterValue()));
                    }
                }
            }
        }
    }

    protected Field<?> createField(EntityAttributeWeb attribute, boolean fieldValidation, boolean readonly) {
        AttributeDatatype type = AttributeDatatype.getById(attribute.getDatatype().getDataTypeCd());
        Field<?> field = null;

        switch (type) {
        case INTEGER:
        case SHORT:
        case LONG:
        case DOUBLE:
        case FLOAT:
            NumberField fieldNumber = new NumberField();
            fieldNumber.setFieldLabel(attribute.getDisplayName());

            if (fieldValidation) {
                setStTextFieldValidation(fieldNumber, attribute);
            }

            field = fieldNumber;
            break;

        case STRING:
            // Info.display("field Type: ","String" );
            boolean hasValueSet = false;
            for (EntityAttributeValidationWeb validation : attribute.getEntityAttributeValidations()) {
                if (validation.getValidationName().equals("valueSetValidationRule")) {
                    hasValueSet = true;
                }
            }

            if (hasValueSet) {
                ComboBox<ModelPropertyWeb> fieldCombo = new ComboBox<ModelPropertyWeb>();
                fieldCombo.setFieldLabel(attribute.getDisplayName());
                fieldCombo.setEmptyText("Select a " + attribute.getDisplayName() + "...");
                fieldCombo.setDisplayField("name");
                fieldCombo.setTypeAhead(true);
                fieldCombo.setTriggerAction(TriggerAction.ALL);

                // value set
                setStTextFieldValidation(fieldCombo, attribute);

                field = fieldCombo;

            } else {
                TextField<String> fieldString = new TextField<String>();
                fieldString.setFieldLabel(attribute.getDisplayName());

                if (fieldValidation) {
                    setStTextFieldValidation(fieldString, attribute);
                }
                field = fieldString;
            }
            break;

        case BOOLEAN:
            CheckBox checkBox = new CheckBox();
            checkBox.setBoxLabel("");
            CheckBoxGroup checkGroup = new CheckBoxGroup();
            checkGroup.setFieldLabel(attribute.getDisplayName());
            checkGroup.add(checkBox);
            field = checkGroup;
            break;

        case DATE:
        case TIMESTAMP:
            // Info.display("field Type: ","Date" );
            DateField fieldDate = new DateField();
            fieldDate.setFieldLabel(attribute.getDisplayName());

            field = fieldDate;
            break;

        default:
            // Info.display("field Type: ","error" );
            throw new RuntimeException("Unable to handle creation of a widget field of this type: " + type);
        }

        if (field != null) {
            field.setReadOnly(readonly);
        }
        return field;
    }

    protected FieldSet createGroupFields(Map<String, Field<?>> fieldMap, EntityAttributeGroupWeb group,
            List<EntityAttributeWeb> sortedEntityAttributes, boolean readonly) {

        FieldSet fieldSet = new FieldSet();
        fieldSet.setHeading(group.getDisplayName());
        fieldSet.setCollapsible(true);
        fieldSet.setBorders(false);
        FormLayout grouplayout = new FormLayout();
        grouplayout.setLabelWidth(150);
        fieldSet.setLayout(grouplayout);

        boolean hasAttribute = false;
        for (EntityAttributeWeb entityAttribute : sortedEntityAttributes) {
            // Info.display("Attribute:", entityAttribute.getName()
            // +"; "+entityAttribute.getDatatype().getDataTypeCd());

            if (entityAttribute.getEntityAttributeGroup() != null
                    && entityAttribute.getEntityAttributeGroup().getName().equals(group.getName())) {
                Field<?> field = createField(entityAttribute, true, readonly);
                if (field != null) {
                    hasAttribute = true;
                    fieldMap.put(entityAttribute.getName(), field);
                    fieldSet.add(field);
                }
            }
        }
        if (hasAttribute) {
            return fieldSet;
        }
        return null;
    }
}
