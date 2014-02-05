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
 * 
 * Represents,
 *  
 * <resource path="{value}" method="GET|POST|PUT|DELETE">
 *   <call-query href="{value}">
 *     <with-param name="{value}" query-param="{value}" />+
 *   </call-query> || <call-query-group> ..
 * </resource>
 * 
 * element.
 *
 */
public class Resource extends CallableRequest {
	
	private String path;
	
	private String method;
		
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
    public OMElement buildXML() {
    	OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement resEl = fac.createOMElement("resource", null);
		if (this.getPath() != null) {
		    resEl.addAttribute("path", this.getPath(), null);
		}
		if (this.getMethod() != null) {
		    resEl.addAttribute("method", this.getMethod(), null);
		}		
		this.populateGenericRequestProps(resEl);		
		return resEl;
    }
	
}
