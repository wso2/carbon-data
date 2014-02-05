/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dataservices.task;

import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.solutions.webservice.WebServiceCallTask;

/**
 * This class represents the data services scheduled tasks functionality.
 */
public class DSTask extends WebServiceCallTask {

	private final Log log = LogFactory.getLog(DSTask.class);
	
	private boolean serviceInit = false;
	
	public DSTask() {
	}
	
	public boolean isServiceInit() {
		return serviceInit;
	}
	
	private boolean checkServiceInit() {
		if (this.isServiceInit()) {
			return true;
		}
		String serviceName = this.getProperties().get(DSTaskConstants.DATA_SERVICE_NAME);
		String opName = this.getProperties().get(DSTaskConstants.DATA_SERVICE_OPERATION_NAME);
                String tidProp = this.getProperties().get(TaskInfo.TENANT_ID_PROP);
		if (tidProp == null) {
			throw new RuntimeException("Cannot determine the tenant domain for the scheduled service: " +
		            serviceName);
		}
                int tid = Integer.parseInt(tidProp);
		AxisService axisService = DSTaskUtils.lookupAxisService(tid, serviceName);
		if (axisService == null) {
		    return false;
		}
    	if (axisService == null || !axisService.isActive()) {
    		return false;
    	}
    	String httpEPR = DSTaskUtils.extractHTTPEPR(axisService);
    	if (httpEPR == null) {
    		throw new RuntimeException("No HTTP endpoint found for service: " +
    	        serviceName + " for scheduling service calls");
    	}
    	this.getProperties().put(WebServiceCallTask.SERVICE_TARGET_EPR, httpEPR);
    	String mep;
    	if (DSTaskUtils.isInOutMEPInOperation(axisService, opName)) {
    		mep = WebServiceCallTask.SERVICE_MEP_IN_OUT;
    	} else {
    		mep = WebServiceCallTask.SERVICE_MEP_IN_ONLY;
    	}
    	this.getProperties().put(WebServiceCallTask.SERVICE_MEP, mep);
    	this.serviceInit = true;
    	super.init();
		return this.isServiceInit();
	}
    
	@Override
	public void init() {
		this.checkServiceInit();
	}
	
	@Override
	public void execute() {
		if (this.checkServiceInit()) {
			super.execute();
		} else {
			log.info("Target service '" + this.getProperties().get(
					DSTaskConstants.DATA_SERVICE_NAME) + "' is not active.");
		}
	}
	
}
