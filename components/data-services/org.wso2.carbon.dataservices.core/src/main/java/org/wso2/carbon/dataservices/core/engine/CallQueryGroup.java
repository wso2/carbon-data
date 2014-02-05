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

import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.description.query.Query;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents a collection of call queries,
 * that is used to execute in succession.
 */
public class CallQueryGroup extends OutputElement {

	private List<CallQuery> callQueries;
		
	private boolean hasResult;
	
	private String resultWrapper;
	
	private boolean isInit;
	
	public CallQueryGroup(List<CallQuery> callQueries) {
		/* set namespace at init() */
		super(null, new HashSet<String>());
		this.callQueries = callQueries;		
	}
		
	public void init() throws DataServiceFault {
		if (!this.isInit) {
			for (CallQuery callQuery : this.getCallQueries()) {
				callQuery.init();
			}			
			CallQuery defaultCQ = this.getDefaultCallQuery();
			if (defaultCQ != null) {
				Query defaultQuery = defaultCQ.getQuery();				
				this.setNamespace(defaultQuery.getNamespace());
				this.hasResult = defaultQuery.hasResult();
				if (defaultQuery.hasResult()) {
				    this.resultWrapper = defaultQuery.getResult().getElementName();
				    /* if empty element, set it to null */
				    if (this.resultWrapper != null && this.resultWrapper.trim().length() == 0) {
				    	this.resultWrapper = null;
				    }
				}
			}
			this.isInit = true;
		}
	}
	
	/**
	 * Returns the default call query in this group - the first call query.
	 */
	public CallQuery getDefaultCallQuery() {
		if (this.getCallQueries().size() > 0) {
			return this.getCallQueries().get(0);
		}
		return null;
	}
	
	public boolean isHasResult() {
		return hasResult;
	}

	public String getResultWrapper() {
		return resultWrapper;
	}

	public List<CallQuery> getCallQueries() {
		return callQueries;
	}
	
	/**
	 * Execute all the call queries in this group.
	 */
    @Override
    protected void executeElement(XMLStreamWriter xmlWriter, ExternalParamCollection params,
                                  int queryLevel, boolean escapeNonPrintableChar) throws DataServiceFault {
        try {
			/* start write result wrapper */
			if (this.isHasResult()) {
				this.startWrapperElement(xmlWriter, this.getNamespace(), this.getResultWrapper(),
					    this.getDefaultCallQuery().getQuery().getResult().getResultType());
			}

			/* write query results */
			List<CallQuery> callQueries = this.getCallQueries();
			for (CallQuery callQuery : callQueries) {
				callQuery.executeElement(xmlWriter, params, queryLevel, escapeNonPrintableChar);
			}

			/* end write result wrapper */
			if (this.isHasResult() && this.getResultWrapper() != null) {
			    this.endElement(xmlWriter);
			}
		} catch (XMLStreamException e) {
			throw new DataServiceFault(e, "Error in CallQueryGroup.execute");
		}
    }

    public boolean isOptional() {
		CallQuery defaultCQ = this.getDefaultCallQuery();
		if (defaultCQ != null) {
			return defaultCQ.isOptional();
		}
		return false;
	}

	public Set<String> getRequiredRoles() {
		CallQuery defaultCQ = this.getDefaultCallQuery();
		if (defaultCQ != null) {
			return defaultCQ.getRequiredRoles();
		}
		return null;
	}
	
}
