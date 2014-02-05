/*
 *  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.DataServiceUser;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.dataservices.core.engine.ParamValue;
import org.wso2.carbon.dataservices.core.tools.DSTools;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * This class represents a context class used in data tasks.
 */
public class DataTaskContext {
	
	private AxisConfiguration axisConfig;
			
	public DataTaskContext(int tid) {
		this.axisConfig = DSTaskUtils.lookupAxisConfig(tid);
	}

	private DataService getDataService(String serviceName) {
		AxisService axisService = DSTaskUtils.lookupAxisService(this.axisConfig, serviceName);
		if (axisService == null) {
			return null;
		}
		DataService dataService = (DataService) axisService.getParameterValue(
				DBConstants.DATA_SERVICE_OBJECT);
		return dataService;
	}
	
	/**
	 * Checks if the given services are available.
	 * @param services An array of service names to be checked
	 * @return Returns true if all the given services are available
	 */
	public boolean checkServices(String... services) {
		for (String service : services) {
			if (this.getDataService(service) == null) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Invokes a single data service operation.
	 * @param serviceName The name of the data service
	 * @param operationName The data service operation name
	 * @param params The parameters to be passed into the operation
	 * @return The result from the service call, if any available, or returns null,
	 * if it is an in-only operation
	 * @throws DataServiceFault If an error occurs in the service invocation
	 */
	public OMElement invokeOperation(String serviceName, String operationName, 
			Map<String, ParamValue> params) throws DataServiceFault {
		DataService dataService = this.getDataService(serviceName);
		if (dataService == null) {
			throw new DataServiceFault("The service '" + serviceName + "' does not exist");
		}
		return DSTools.invokeOperation(dataService, operationName, params);
	}
	
	/**
	 * Invokes a batch data service operation
	 * @param serviceName The name of the data service
	 * @param operationName The data service batch operation name
	 * @param batchParams The batch parameters to be passed into the operation
	 * @throws DataServiceFault If an error occurs in the service invocation
	 */
	public void invokeOperation(String serviceName, String operationName, 
			List<Map<String, ParamValue>> batchParams) throws DataServiceFault {
		DataService dataService = this.getDataService(serviceName);
		if (dataService == null) {
			throw new DataServiceFault("The service '" + serviceName + "' does not exist");
		}
		DSTools.invokeOperation(dataService, operationName, batchParams);
	}
	
	/**
	 * Accesses a data services resource.
	 * @param serviceName The name of the data service
	 * @param resourcePath The resource path used to access the resource
	 * @param params The parameters passed into the resource access
	 * @param accessMethod The HTTP access method
	 * @return The result from the resource access, if any available, or else null
	 * @throws DataServiceFault If an error occurs in the service invocation
	 */
	public OMElement accessResource(String serviceName, String resourcePath, 
			Map<String, ParamValue> params, String accessMethod) throws DataServiceFault {
		DataService dataService = this.getDataService(serviceName);
		if (dataService == null) {
			throw new DataServiceFault("The service '" + serviceName + "' does not exist");
		}
		return DSTools.accessResource(dataService, resourcePath, params, accessMethod);
	}
	
	/**
	 * Sets the Carbon user for the current thread of execution.
	 * @param username The username
	 * @param password The password
	 * @throws DataServiceFault If an error occurs in the service invocation
	 */
	public void setDataServicesUser(String username, String password) throws DataServiceFault {
		if (DBUtils.authenticate(username, password)) {
			DSTools.setDataServicesUser(new DataServiceUser(username, 
					new HashSet<String>((Arrays.asList(DBUtils.getUserRoles(username))))));
		} else {
			throw new DataServiceFault("Invalid credentials for user '" + username + "'");
		}
	}
	
}
