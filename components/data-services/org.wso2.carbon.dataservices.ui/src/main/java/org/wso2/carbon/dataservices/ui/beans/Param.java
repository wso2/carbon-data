/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.dataservices.ui.beans;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Object for query/param element
 */
public class Param extends DataServiceConfigurationElement {
	
	private String name;
	
	private String sqlType;
	
	private String type;
	
	private int ordinal;
	
	private String operationParamName;
	
	private String columnName;

    private String paramType;

    private String defaultValue;

    private String structType;

    private List<Validator> validators;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSqlType() {
		return sqlType;
	}
	
	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public int getOrdinal() {
		return ordinal;
	}
	
	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getParamType() {
        return paramType;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getStructType() {
        return structType;
    }

    public void setStructType(String structType) {
        this.structType = structType;
    }

    public Param(String name, String paramType, String sqlType, String type, String ordinal,
                 String defaultValue, String structType, List<Validator> validators){
		this.name = name;
		this.sqlType = sqlType;
		this.type = type;
        this.paramType = paramType;
		/* default type is "IN" */
		if (this.type == null) {
			this.type = "IN";
		}
        /* default paramType is "SCALAR" */
        if(this.paramType == null) {
            this.paramType = "SCALAR";
        }
		this.ordinal = Integer.parseInt(ordinal);
        this.defaultValue = defaultValue;
        this.structType = structType;
        this.validators = validators;
	}
	
	public Param(String name, String paramType, String sqlType, String type, String defaultValue,
                 List<Validator> validators){
		this(name, paramType, sqlType, type, "0", defaultValue, null, validators);
	}

    public Param() {
        this.validators = new ArrayList<Validator>();
    }

	public String getOperationParamName() {
		return operationParamName;
	}
	
	public void setOperationParamName(String operationParamName) {
		this.operationParamName = operationParamName;
	}

    public List<Validator> getValidators() {
        return validators;
    }

    public void setValidarors(List<Validator> validators) {
        this.validators = validators;
    }

    public OMElement buildXML() {
    	OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement paramEl = fac.createOMElement("param", null);
		if (this.getName() != null) {
		    paramEl.addAttribute("name", this.getName(), null);
		}
        if (this.getParamType() != null && !(this.getParamType().equals("SCALAR"))){
            paramEl.addAttribute("paramType", this.getParamType(), null);
        }
		if (this.getSqlType() != null) {
		    paramEl.addAttribute("sqlType", this.getSqlType(), null);
		}
		if (this.getType() != null && !(this.getType().equals("IN"))) {
		    paramEl.addAttribute("type", this.getType(), null);
		}
        if (this.getOrdinal() > 0) {
            paramEl.addAttribute("ordinal", String.valueOf(this.getOrdinal()), null);
        }
        if (this.getDefaultValue() != null) {
            paramEl.addAttribute("defaultValue", this.getDefaultValue(), null);
        }
        if (this.getStructType() != null) {
            paramEl.addAttribute("structType", this.getStructType(), null);
        }
        if (this.getValidators() != null) {
            for (Validator val : this.getValidators()) {
                paramEl.addChild(val.buildXML());
            }
        }
		return paramEl;
    }
    
}