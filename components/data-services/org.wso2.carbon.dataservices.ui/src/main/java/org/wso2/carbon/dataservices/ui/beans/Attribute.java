/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
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

public class Attribute extends DataServiceConfigurationElement{
    private String dataSourceType;
    private String dataSourceValue;
    private String name;
    private String export;
    private String exportType;
    private String arrayName;
    private String optional;

    
    public Attribute(String dataSourceType,String dataSourceValue, String name,
                     String requiredRoles, String xsdType, String export, String exportType,
                     String optional){
        super(requiredRoles, xsdType);
        this.dataSourceType = dataSourceType;
        this.dataSourceValue = dataSourceValue;
        this.name = name;
        this.export = export;
        this.exportType = exportType;
        this.optional = optional;
    }

    public Attribute(String dataSourceType,String dataSourceValue, String name,
                     String requiredRoles, String xsdType, String export, String exportType,
                     String arrayName, String optional){
        super(requiredRoles, xsdType);
        this.dataSourceType = dataSourceType;
        this.dataSourceValue = dataSourceValue;
        this.name = name;
        this.export = export;
        this.exportType = exportType;
        this.arrayName = arrayName;
        this.optional = optional;
    }

    public Attribute(){
        
    }
    
    public OMElement buildXML() {
    	OMFactory fac = OMAbstractFactory.getOMFactory();
    	OMElement attrEl = fac.createOMElement("attribute", null);
    	if (this.getName() != null) {
    	    attrEl.addAttribute("name", this.getName(), null);
    	}
    	if (this.getDataSourceValue() != null) {
    		if (this.getDataSourceType().equals("column")) {
    			attrEl.addAttribute("column", this.getDataSourceValue(), null);
    		} else if (this.getDataSourceType().equals("query-param")) {
    			attrEl.addAttribute("query-param", this.getDataSourceValue(), null);
    		}		
    	}
    	if (this.getRequiredRoles() != null && this.getRequiredRoles().trim().length() > 0) {
            attrEl.addAttribute("requiredRoles", this.getRequiredRoles(), null);
        } 
    	if (this.getExport() != null && this.getExport().trim().length() > 0) {
    		attrEl.addAttribute("export", this.getExport(), null);
        }
        if (this.getExportType() != null && !(this.getExportType().equals("SCALAR"))) {
        	attrEl.addAttribute("exportType", this.getExportType(), null);
        }
        if (this.getXsdType() != null && this.getXsdType().trim().length() > 0) {
            attrEl.addAttribute("xsdType", this.getXsdType(), null);
        }
        if (this.getArrayName() != null && this.getArrayName().trim().length() > 0) {
            attrEl.addAttribute("arrayName", this.getArrayName(), null);
        }
        if (this.getOptional() != null && this.getOptional().trim().equals("true")) {
            attrEl.addAttribute("optional", this.getOptional().trim(), null);
        }
    	return attrEl;
    }

    public String getArrayName() {
        return arrayName;
    }

    public void setArrayName(String arrayName) {
        this.arrayName = arrayName;
    }

    public String getDataSourceType() {
		return dataSourceType;
	}

	public void setDataSourceType(String dataSourceType) {
		this.dataSourceType = dataSourceType;
	}

	public String getDataSourceValue() {
		return dataSourceValue;
	}

	public void setDataSourceValue(String dataSourceValue) {
		this.dataSourceValue = dataSourceValue;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

    public String getOptional() {
        return this.optional;
    }

    public void setOptional(String optional) {
        this.optional = optional;
    }
    
	public boolean equals(Object o) {
        if ((o instanceof Attribute) && (((Attribute) o).getName().equals(this.getName()))) {
            return true;
        } else {
            return false;
        }
    }
	
	public int hashCode() {
		return this.getName().hashCode();
	}
    
}
