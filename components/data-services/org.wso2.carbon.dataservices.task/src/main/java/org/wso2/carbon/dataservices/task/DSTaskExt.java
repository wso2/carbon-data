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

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.core.AbstractTask;

/**
 * This class represents the data services extended scheduled tasks functionality.
 */
public class DSTaskExt extends AbstractTask {

	private DataTask taskInstance;
	
	private DataTaskContext dataTaskContext;
	
	@Override
	public void init() {
		try {
		    this.taskInstance = (DataTask) Class.forName(this.getProperties().get(
		    		DSTaskConstants.DATA_TASK_CLASS_NAME)).newInstance();
		    this.dataTaskContext = new DataTaskContext(
		    		PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true));
            this.dataTaskContext.setDataTaskProperties(this.getProperties());
		} catch (Exception e) {
			throw new RuntimeException("Error in initializing Data Task: " + e.getMessage(), e);
		}
	}
	
	public DataTask getTaskInstance() {
		return taskInstance;
	}

	public DataTaskContext getDataTaskContext() {
		return dataTaskContext;
	}
	
	@Override
	public void execute() {
		this.getTaskInstance().execute(this.getDataTaskContext());
	}

}
