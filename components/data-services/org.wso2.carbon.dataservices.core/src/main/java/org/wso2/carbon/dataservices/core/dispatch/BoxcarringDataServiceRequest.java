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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.dataservices.common.DBConstants.BoxcarringOps;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DSSessionManager;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.boxcarring.TLParamStore;
import org.wso2.carbon.dataservices.core.engine.DataService;

/**
 * Boxcarring data service request for service call grouping.
 */
public class BoxcarringDataServiceRequest extends DataServiceRequest {
	
	/**
	 * The data service request which is inside the boxcarring session
	 */
	private DataServiceRequest dsRequest;
	
	public BoxcarringDataServiceRequest(DataServiceRequest dsRequest) throws DataServiceFault {
		super(dsRequest.getDataService(), dsRequest.getRequestName());
		this.dsRequest = dsRequest;
	}
	
	public DataServiceRequest getDSRequest() {
		return dsRequest;
	}
	
	/**
	 * @see DataServiceRequest#processRequest()
	 */
	@Override
	public OMElement processRequest() throws DataServiceFault {
		if (BoxcarringOps.BEGIN_BOXCAR.equals(this.getRequestName())) {
			/* clear earlier boxcarring sessions */
			DSSessionManager.getCurrentRequestBox().clear();
			/* set the status to boxcarring */
			DSSessionManager.setBoxcarring(true);
		} else if (BoxcarringOps.END_BOXCAR.equals(this.getRequestName())) {
			/* execute all the stored requests */
			DataService dataService = this.getDSRequest().getDataService();
			try {
				dataService.beginTransaction();
			    OMElement lastRequestResult = DSSessionManager.getCurrentRequestBox().execute();
			    dataService.endTransaction();
			    return lastRequestResult;
			} catch (DataServiceFault e) {
				dataService.rollbackTransaction();
				throw new DataServiceFault(e, "Error in boxcarring end");
			} finally {
				DSSessionManager.getCurrentRequestBox().clear();
				DSSessionManager.setBoxcarring(false);
				TLParamStore.clear();
			}			
		} else if (BoxcarringOps.ABORT_BOXCAR.equals(this.getRequestName())) {
			DSSessionManager.getCurrentRequestBox().clear();
			DSSessionManager.setBoxcarring(false);
		} else {
			DSSessionManager.getCurrentRequestBox().addRequest(this.getDSRequest());
			/* return an empty wrapper element result for each out/in-out boxcarring request,
			 * so the caller will get a valid result for out operations */
			return this.createBoxcarringRequestResultWrapper();
		}
		return null;
	}
	
	private OMElement createBoxcarringRequestResultWrapper() {
		String resultWrapper = this.getDataService().getResultWrapperForRequest(
				this.getRequestName());
		if (resultWrapper == null) {
			/* in-only request */
			return null;
		}
		String ns = this.getDataService().getNamespaceForRequest(this.getRequestName());
		OMFactory fac = DBUtils.getOMFactory();
		OMElement ele = fac.createOMElement(new QName(ns, resultWrapper));
		return ele;
	}
	
}

