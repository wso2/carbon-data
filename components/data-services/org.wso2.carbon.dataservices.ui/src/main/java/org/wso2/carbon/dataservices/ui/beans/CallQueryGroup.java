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
import java.util.List;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

/**
 * 
 * Represents 
 * <call-query-group">
 *   <call-query href="{value}" />+
 * </call-query>
 * 
 * @see CallQuery
 */
public class CallQueryGroup extends DataServiceConfigurationElement {

	private List<CallQuery> callQueries;
	
	public CallQueryGroup() {
		this.callQueries = new ArrayList<CallQuery>();
	}

	public void addCallQuery(CallQuery callQuery) {
		this.getCallQueries().add(callQuery);
	}
	
	public List<CallQuery> getCallQueries() {
		return callQueries;
	}
	
	@Override
	public OMElement buildXML() {
		/* if only 1 query, then just add the "call-query" element */
		if (this.getCallQueries().size() == 1) {
			return this.getCallQueries().get(0).buildXML();
		} else { /* add call-query-group element */
			OMFactory fac = OMAbstractFactory.getOMFactory();
	    	OMElement cqgEl = fac.createOMElement("call-query-group", null);
	    	for (CallQuery callQuery : this.getCallQueries()) {
	    		cqgEl.addChild(callQuery.buildXML());
	    	}
	    	return cqgEl;
		}
	}
	

}
