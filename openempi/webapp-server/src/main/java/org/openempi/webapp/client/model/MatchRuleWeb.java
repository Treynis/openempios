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
package org.openempi.webapp.client.model;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class MatchRuleWeb extends BaseModelData
{
	public static final String FIELD_NAME = "fieldName";
	public static final String FIELD_DESCRIPTION = "fieldDescription";
	public static final String COMPARATOR_FUNCTION_NAME = "comparatorFunctionName";
	public static final String COMPARATOR_FUNCTION_NAME_DESCRIPTION = "comparatorFunctionNameDescription";
	public static final String MATCH_THRESHOLD = "matchThreshold";
	public static final String FIELD_INDEX = "fieldIndex";
	public static final String MATCH_RULE = "matchRule";

	public MatchRuleWeb() {
	}

	public MatchRuleWeb(Integer matchRule, Integer fieldIndex, String fieldName) {
		set(MATCH_RULE, matchRule);
		set(FIELD_INDEX, fieldIndex);
		set(FIELD_NAME, fieldName);
	}

	public MatchRuleWeb(Integer matchRule, Integer fieldIndex, String fieldName, String fieldDescription) {
		set(MATCH_RULE, matchRule);
		set(FIELD_INDEX, fieldIndex);
		set(FIELD_NAME, fieldName);
		set(FIELD_DESCRIPTION, fieldDescription);
	}

    public MatchRuleWeb(Integer matchRule, Integer fieldIndex, String fieldName, String fieldDescription, 
                        String comparatorFunctionName,String comparatorFunctionNameDescription, Float matchThreshold) {
        set(MATCH_RULE, matchRule);
        set(FIELD_INDEX, fieldIndex);
        set(FIELD_NAME, fieldName);
        set(FIELD_DESCRIPTION, fieldDescription);
        set(COMPARATOR_FUNCTION_NAME, comparatorFunctionName);
        set(COMPARATOR_FUNCTION_NAME_DESCRIPTION, comparatorFunctionName);
        set(MATCH_THRESHOLD, matchThreshold);
    }
	   
	public Integer getMatchRule() {
		return get(MATCH_RULE);
	}

	public void setMatchRule(Integer matchRule) {
		set(MATCH_RULE, matchRule);
	}
	
	public Integer getFieldIndex() {
		return get(FIELD_INDEX);
	}

	public void setFieldIndex(Integer fieldIndex) {
		set(FIELD_INDEX, fieldIndex);
	}
	
	public String getFieldName() {
		return get(FIELD_NAME);
	}

	public void setFieldName(String fieldName) {
		set(FIELD_NAME, fieldName);
	}
	
	public String getFieldDescription() {
		return get(FIELD_DESCRIPTION);
	}

	public void setFieldDescription(String fieldDescription) {
		set(FIELD_DESCRIPTION, fieldDescription);
	}

    public java.lang.String getComparatorFunctionName() {
        return get(COMPARATOR_FUNCTION_NAME);
    }

    public void setComparatorFunctionName(java.lang.String comparatorFunctionName) {
        set(COMPARATOR_FUNCTION_NAME, comparatorFunctionName);
    }

    public java.lang.String getComparatorFunctionNameDescription() {
        return get(COMPARATOR_FUNCTION_NAME_DESCRIPTION);
    }

    public void setComparatorFunctionNameDescription(java.lang.String comparatorFunctionNameDescription) {
        set(COMPARATOR_FUNCTION_NAME_DESCRIPTION, comparatorFunctionNameDescription);
    }
    
    public java.lang.Float getMatchThreshold() {
        return get(MATCH_THRESHOLD);
    }

    public void setMatchThreshold(java.lang.Float matchThreshold) {
        set(MATCH_THRESHOLD, matchThreshold);
    }
}
