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
package org.wso2.carbon.dataservices.core.dispatch;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.XSLTTransformer;
import org.wso2.carbon.dataservices.core.description.event.EventTrigger;
import org.wso2.carbon.dataservices.core.description.query.Query;
import org.wso2.carbon.dataservices.core.engine.DSOMDataSource;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.dataservices.core.engine.ParamValue;
import org.wso2.carbon.dataservices.core.engine.Result;

/**
 * Represents a single data service request.
 */
public class SingleDataServiceRequest extends DataServiceRequest {
		
	/**
	 * Request parameters
	 */
	private Map<String, ParamValue> params;
	
	public SingleDataServiceRequest(DataService dataService, String requestName, 
			Map<String, ParamValue> params) throws DataServiceFault {
		super(dataService, requestName);
		this.params = params;
	}
	
	public Map<String, ParamValue> getParams() {
		return params;
	}
	
	/**
	 * @see DataServiceRequest#processRequest()
	 */
	@Override
	public OMElement processRequest() throws DataServiceFault {
		DataService dataService = this.getDataService();
		boolean inTx = false;
		/* not inside a nested transaction, i.e. boxcarring/batch-requests  */
		if (!dataService.isInTransaction()) { 
			/* an active transaction has already started by the transaction manager,
			 * e.g. external JMS transaction */
			if (dataService.isEnableXA() && !dataService.getDSSTxManager().hasNoActiveTransaction()) {
				/* signal we are inside a transaction */
				dataService.beginTransaction();
				inTx = true;
			}
		}
		try {
			OMElement result = processSingleRequest();
			if (inTx) {
				/* build the result immediately, if we are in a transaction */
				if (result != null) {
					result = DBUtils.cloneAndReturnBuiltElement(result);
				}
				/* signal the end of transaction, this wont necessarily commit the 
			 	* transaction, it will be done by the external transaction creator */
				dataService.endTransaction();
			}
			return result;
		} catch (DataServiceFault e) {
			if (inTx && dataService.getDSSTxManager().hasNoActiveTransaction()) {
			    dataService.rollbackTransaction();
			}
			throw e;
		}
	}
	
	private OMElement processSingleRequest() throws DataServiceFault {
		DataService dataService = this.getDataService();
		String requestName = this.getRequestName();
		/* set the operation name to invoke and the parameters */
		DSOMDataSource ds = new DSOMDataSource(dataService, requestName, this.getParams());

		/* check if the current request has a result, if so, return the OMElement */
		if (dataService.hasResultForRequest(this.getRequestName())) {
			String resultWrapper = dataService.getResultWrapperForRequest(requestName);
			String ns = dataService.getNamespaceForRequest(requestName);
			OMElement responseElement = new OMSourcedElementImpl(new QName(ns,
					resultWrapper), DBUtils.getOMFactory(), ds);
			Query defQuery = dataService.getCallableRequest(
					requestName).getCallQueryGroup().getDefaultCallQuery().getQuery();
			/*
			 * Checks if the result has to be pre-built, because in situations like having an
			 * output-event-trigger, for XPath expression evaluations, the following operation
			 * must be done, or it wont work. 
			 */
			if (defQuery.isPreBuildResult()) {
				responseElement = DBUtils.cloneAndReturnBuiltElement(responseElement);
			}
			
			/* do XSLT transformation if available */
			responseElement = this.executeXsltTranformation(responseElement, defQuery);
			
			/* process events */
			this.processOutputEvents(responseElement, defQuery);
			
			return responseElement;
		} else { /* if no response i.e. in-only, execute the request now */
			try {
				ds.executeInOnly();
			} catch (XMLStreamException e) {
				throw new DataServiceFault(e, "Error in DS non result invoke.");
			}
			return null;
		}
	}

	private OMElement executeXsltTranformation(OMElement input, Query query)
			throws DataServiceFault {
		Result result = query.getResult();
		XSLTTransformer transformer = result.getXsltTransformer();
		if (transformer == null) {
			return input;
		} else {
			try {
				return transformer.transform(input);
			} catch (Exception e) {
				throw new DataServiceFault(e,
						"Error in result XSLT transformation");
			}
		}
	}
	
	private void processOutputEvents(OMElement input, Query query)
			throws DataServiceFault {
		EventTrigger trigger = query.getOutputEventTrigger();
		/* if output event trigger is available, execute it */
		if (trigger != null) {
			trigger.execute(input, query.getQueryId());
		}
	}

}

