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
package org.wso2.carbon.dataservices.core.description.operation;

import javax.xml.stream.XMLStreamWriter;

import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.engine.CallQueryGroup;
import org.wso2.carbon.dataservices.core.engine.CallableRequest;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.dataservices.core.engine.ExternalParamCollection;

/**
 * Represents an operation within a data service.
 */
public class Operation extends CallableRequest {

	private DataService dataService;
	
	private String name;
	
	private Operation parentOperation;
	
	public Operation(DataService dataService, String name, String description, 
			CallQueryGroup callQueryGroup, boolean batchRequest, Operation parentOperation,
			boolean disableStreamingRequest, boolean disableStreamingEffective) {
		super(name, description, callQueryGroup, batchRequest, disableStreamingRequest,
				disableStreamingEffective);
		this.dataService = dataService;
		this.name = name;
		this.parentOperation = parentOperation;
	}
	
	/**
	 * This method returns the parent operation, provided that this is a batch operation.
	 */
	public Operation getParentOperation() {
		return parentOperation;
	}

	public DataService getDataService() {
		return dataService;
	}

	public String getName() {
		return name;
	}
	
	/**
	 * This executes the operation, by retrieving the call query group associated with it,
	 * and executing the query group.
	 */
	public void execute(XMLStreamWriter xmlWriter, ExternalParamCollection params) 
			throws DataServiceFault {
		this.getCallQueryGroup().execute(xmlWriter, params, 0, false);
	}
	
}
