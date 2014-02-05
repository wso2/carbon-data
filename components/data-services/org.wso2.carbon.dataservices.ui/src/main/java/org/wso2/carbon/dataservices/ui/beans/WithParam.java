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

/**
 * 
 * Represents <with-param name="{value}" query-param="{value}" /> element in the
 * configuration file
 * 
 */
public class WithParam extends DataServiceConfigurationElement {
	private String name;
	private String paramType;
	private String paramValue;

	public WithParam() {
	}

	public WithParam(String name, String paramType, String paramValue) {
		this.name = name;
		this.paramType = paramType;
		this.paramValue = paramValue;
	}

	public OMElement buildXML() {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement wpEl = fac.createOMElement("with-param", null);
		if (this.getName() != null) {
			wpEl.addAttribute("name", this.getName(), null);
		}
		if (this.getParamValue() != null) {
			if (this.getParamType().equals("column")) {
				wpEl.addAttribute("column", this.getParamValue(), null);
			} else {
				wpEl.addAttribute("query-param", this.getParamValue(), null);
			}
		}
		return wpEl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParamType() {
		return paramType;
	}

	public void setParamType(String paramType) {
		this.paramType = paramType;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

}
