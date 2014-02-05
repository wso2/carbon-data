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

/**
 * Object mapping for operation
 *   <operation name="TestName">                
 *       <call-query href="testSQL" /> || <call-query-group"> ..       
 *   </operation>      
 */
public class Operation extends CallableRequest {
	
	private String name;
		
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
		
    public OMElement buildXML() {
    	OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement opEl = fac.createOMElement("operation", null);
		if (this.getName() != null) {
		    opEl.addAttribute("name", this.getName(), null);
		}
		this.populateGenericRequestProps(opEl);
        return opEl;
    }    
}
