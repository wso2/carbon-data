/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.core.engine;

import org.apache.axis2.databinding.types.NCName;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.XSLTTransformer;

import java.util.Set;

/**
 * Represents the result element in a query.
 */
public class Result {
	
	private String elementName;
	
	private String rowName;
	
	private String namespace;

    private XSLTTransformer xsltTransformer;
    
    private int resultType;
    
    private String rdfBaseURI;
    
    private OutputElementGroup defaultElementGroup;
    
    private boolean xsAny;
    
    private boolean useColumnNumbers;

    private boolean escapeNonPrintableChar;
    
	public Result(String elementName, String rowName, String namespace, String xsltPath, int resultType)
            throws DataServiceFault {
		this.elementName = elementName;
		this.rowName = rowName;
		
		if (resultType != DBConstants.ResultTypes.RDF) {
			/* validate element name */
			if (this.elementName != null && this.elementName.trim().length() > 0
					&& !NCName.isValid(this.elementName)) {
				throw new DataServiceFault("Invalid wrapper element name: '"
							+ this.elementName + "', must be an NCName.");
			}
			/* validate row name */
			if (this.rowName != null && this.rowName.length() != 0 &&
					!NCName.isValid(this.rowName)) {
				throw new DataServiceFault("Invalid row name: '" + this.rowName
						+ "', must be an NCName.");
			}
		}
		this.namespace = namespace;
		if (xsltPath != null) {
            try {
                xsltTransformer = new XSLTTransformer(xsltPath);
            } catch (Exception e) {
                throw new DataServiceFault(e,
                        "Error in XSLT Transformation initialization in Result");
            }
        }
        this.resultType = resultType;
	}
	
	public boolean isUseColumnNumbers() {
		return useColumnNumbers;
	}

	public void setUseColumnNumbers(boolean useColumnNumbers) {
		this.useColumnNumbers = useColumnNumbers;
	}

	public boolean isXsAny() {
		return xsAny;
	}

	public void setXsAny(boolean xsAny) {
		this.xsAny = xsAny;
	}
	
	public void setDefaultElementGroup(OutputElementGroup defaultElementGroup) {
		this.defaultElementGroup = defaultElementGroup;
	}
	
	public OutputElementGroup getDefaultElementGroup() {
		return defaultElementGroup;
	}
	
	public void setRDFBaseURI(String rdfBaseURI) {
		this.rdfBaseURI = rdfBaseURI;
	}
	
	public String getRDFBaseURI() {
		return rdfBaseURI;
	}
	
	public int getResultType() {
		return resultType;
	}
	
	public void applyUserRoles(Set<String> userRoles) {
		this.getDefaultElementGroup().applyUserRoles(userRoles);
	}
	
    public XSLTTransformer getXsltTransformer() {
        return xsltTransformer;
    }
	
	public String getNamespace() {
		return namespace;
	}
	
	public String getElementName() {
		return elementName;
	}

	public String getRowName() {
		return rowName;
	}

    public boolean isEscapeNonPrintableChar() {
        return escapeNonPrintableChar;
    }

    public void setEscapeNonPrintableChar(boolean escapeNonPrintableChar) {
        this.escapeNonPrintableChar = escapeNonPrintableChar;
    }
}
