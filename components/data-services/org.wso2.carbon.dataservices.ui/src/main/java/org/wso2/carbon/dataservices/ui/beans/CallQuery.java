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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

/**
 * 
 * Represents <call-query href="{value}"> <with-param name="{value}"
 * query-param="{value}" />+ </call-query>
 * 
 * element.
 * 
 * @see WithParam
 *@see Operation,Resource
 */
public class CallQuery extends DataServiceConfigurationElement {

	private String href;

	private List<WithParam> withParams = new ArrayList<WithParam>();

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public List<WithParam> getWithParams() {
		return withParams;
	}

	public void setWithParams(List<WithParam> withParams) {
		this.withParams = withParams;
	}

	public void addWithParam(WithParam withParam) {
		withParams.add(withParam);
	}
	
	public CallQuery() { }

	public CallQuery(String href, ArrayList<WithParam> withParams, String requiredRoles,
			String xsdType) {
		super(requiredRoles, xsdType);
		this.href = href;
		this.withParams = withParams;
	}

	public OMElement buildXML() {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement cqEl = fac.createOMElement("call-query", null);		
		if (this.getHref() != null) {
			cqEl.addAttribute("href", this.getHref(), null);
		}
		if (this.getWithParams().size() != 0) {
			Iterator<WithParam> iterator = this.getWithParams().iterator();
			while (iterator.hasNext()) {
				WithParam withParam = iterator.next();
				cqEl.addChild(withParam.buildXML());
			}
		}
		if (this.getRequiredRoles() != null) {
			cqEl.addAttribute("requiredRoles", this.getRequiredRoles(), null);
		}
		return cqEl;
	}

}
