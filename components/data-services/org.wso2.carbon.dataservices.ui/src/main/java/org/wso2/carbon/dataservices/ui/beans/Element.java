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

/*
Represents result elements for attributes result section.
<result element="test" rowName="test">
<attribute name="test" column="test" />
</result>
*/

public class Element extends DataServiceConfigurationElement{

    private String dataSourceType;
    private String name;
    private String dataSourceValue;
    private String export;
    private String exportType;
    private String namespace;
    private String arrayName;
    private String optional;
    
	public Element(String dataSourceType, String dataSourceValue, String name,
			String requiredRoles, String xsdType, String export,
			String exportType, String namespace, String arrayName, String optional) {
        super(requiredRoles, xsdType);
        this.dataSourceType = dataSourceType;
        this.dataSourceValue = dataSourceValue;
        this.name = name;
        this.export = export;
        this.exportType = exportType;
        this.namespace = namespace;
        this.arrayName = arrayName;
        this.optional = optional;
    }
    
	public Element() { 
    }

    
    public OMElement buildXML() {
    	OMFactory fac = OMAbstractFactory.getOMFactory();
    	OMElement elementEl = fac.createOMElement("element", null);
    	if (this.getName() != null) {
    	    elementEl.addAttribute("name", this.getName(), null);
    	}
    	if (this.getDataSourceValue() != null) {
    		if (this.getDataSourceType().equals("column")) {
    			elementEl.addAttribute("column", this.getDataSourceValue(), null);
    		} else if (this.getDataSourceType().equals("query-param")) {
    			elementEl.addAttribute("query-param", this.getDataSourceValue(), null);
    		}		
    	}
        if (this.getRequiredRoles() != null && this.getRequiredRoles().trim().length() > 0) {
            elementEl.addAttribute("requiredRoles", this.getRequiredRoles().trim(), null);
        }
        if (this.getExport() != null && this.getExport().trim().length() > 0) {
            elementEl.addAttribute("export", this.getExport().trim(), null);
        }
        if (this.getExportType() != null  && !(this.getExportType().equals("SCALAR"))) {
            elementEl.addAttribute("exportType", this.getExportType(), null);
        }
        if (this.getXsdType() != null && this.getXsdType().trim().length() > 0) {
            elementEl.addAttribute("xsdType", this.getXsdType().trim(), null);
        }
        if (this.getNamespace() != null && this.getNamespace().trim().length() > 0) {
            elementEl.addAttribute("namespace", this.getNamespace().trim(), null);
        }
        if (this.getArrayName() != null && this.getArrayName().trim().length() > 0) {
            elementEl.addAttribute("arrayName", this.getArrayName().trim(), null);
        }
        if (this.getOptional() != null && this.getOptional().trim().equals("true")) {
            elementEl.addAttribute("optional", this.getOptional().trim(), null);
        }
        return elementEl;
    }


	public String getDataSourceType() {
		return dataSourceType;
	}


	public void setDataSourceType(String dataSourceType) {
		this.dataSourceType = dataSourceType;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getDataSourceValue() {
		return dataSourceValue;
	}


	public void setDataSourceValue(String dataSourceValue) {
		this.dataSourceValue = dataSourceValue;
	}


	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getExport() {
		return export;
	}


	public void setExport(String export) {
		this.export = export;
	}


	public String getExportType() {
		return exportType;
	}


	public void setExportType(String exportType) {
		this.exportType = exportType;
	}

    public String getArrayName() {
        return arrayName;
    }

    public void setArrayName(String arrayName) {
        this.arrayName = arrayName;
    }

    public String getOptional() {
        return this.optional;
    }

    public void setOptional(String optional) {
        this.optional = optional;
    }

    public boolean equals(Object o) {
        if ((o instanceof Element) && (((Element) o).getName().equals(this.getName()))) {
            return true;
        } else {
            return false;
        }
    }
    
    public int hashCode() {
    	return this.getName().hashCode();
    }
}
