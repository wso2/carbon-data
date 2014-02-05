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
package org.wso2.carbon.dataservices.task.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.task.ui.stub.DSTaskAdminStub;
import org.wso2.carbon.dataservices.task.ui.stub.xsd.DSTaskInfo;

public class DSTaskClient {

    private DSTaskAdminStub stub;
    public static final String EXCEPTION = "task.exception";
    public static final String DISABLE_ADD_TASK = "disableAddTask";
    private static final Log log = LogFactory.getLog(DSTaskClient.class);

    public DSTaskClient(String cookie, String backendUrl,
                        ConfigurationContext ctxt) throws AxisFault {
        String serviceEPR = null;
        try {
            serviceEPR = backendUrl + "DSTaskAdmin";
            stub = new DSTaskAdminStub(ctxt, serviceEPR);
            ServiceClient client = stub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);
            options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            log.error("Error occurred while connecting via stub to : " + serviceEPR, e);
            throw e;
        }
    }

    public String[] getAllTaskNames() throws Exception {
    	return this.stub.getAllTaskNames();
    }

    public DSTaskInfo getTaskInfo(String taskName) throws Exception {
    	return this.stub.getTaskInfo(taskName);
    }

    public void scheduleTask(DSTaskInfo dsTaskInfo) throws Exception {
    	this.stub.scheduleTask(dsTaskInfo);
    }

    public boolean rescheduleTask(DSTaskInfo dsTaskInfo) throws Exception {
    	return this.stub.rescheduleTask(dsTaskInfo);
    }

    public void deleteTask(String taskName) throws Exception {
    	this.stub.deleteTask(taskName);
    }

    public boolean isTaskScheduled(String taskName) throws Exception {
    	return this.stub.isTaskScheduled(taskName);
    }

    public String[] getAllSchedulableDataServices() throws Exception {
    	return this.stub.getAllSchedulableDataServices();
    }

    public String[] getNoParamDSOperations(String serviceName) throws Exception {
    	return this.stub.getNoParamDSOperations(serviceName);
    }

}
