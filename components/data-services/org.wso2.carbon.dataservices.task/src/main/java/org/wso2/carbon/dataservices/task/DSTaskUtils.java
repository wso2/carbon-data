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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskInfo.TriggerInfo;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.ntask.solutions.webservice.WebServiceCallTask;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a utility class for scheduled tasks.  
 */
public class DSTaskUtils {

	public static DSTaskInfo convert(TaskInfo taskInfo) {
		DSTaskInfo dsTaskInfo = new DSTaskInfo();
		dsTaskInfo.setName(taskInfo.getName());
		Map<String, String> taskProps = taskInfo.getProperties();
		dsTaskInfo.setDataTaskClassName(taskProps.get(DSTaskConstants.DATA_TASK_CLASS_NAME));
		dsTaskInfo.setServiceName(taskProps.get(DSTaskConstants.DATA_SERVICE_NAME));
		dsTaskInfo.setOperationName(taskProps.get(DSTaskConstants.DATA_SERVICE_OPERATION_NAME));
		TriggerInfo triggerInfo = taskInfo.getTriggerInfo();
		dsTaskInfo.setCronExpression(triggerInfo.getCronExpression());
		dsTaskInfo.setStartTime(dateToCal(triggerInfo.getStartTime()));
		dsTaskInfo.setEndTime(dateToCal(triggerInfo.getEndTime()));
		dsTaskInfo.setTaskCount(triggerInfo.getRepeatCount());
		dsTaskInfo.setTaskInterval(triggerInfo.getIntervalMillis());
		return dsTaskInfo;
	}
	
	public static TaskInfo convert(DSTaskInfo dsTaskInfo) {
		TriggerInfo triggerInfo = new TriggerInfo();
		triggerInfo.setCronExpression(dsTaskInfo.getCronExpression());
		if (dsTaskInfo.getStartTime() != null) {
		    triggerInfo.setStartTime(dsTaskInfo.getStartTime().getTime());
		}
		if (dsTaskInfo.getEndTime() != null) {
		    triggerInfo.setEndTime(dsTaskInfo.getEndTime().getTime());
		}
		triggerInfo.setIntervalMillis(dsTaskInfo.getTaskInterval());
		triggerInfo.setRepeatCount(dsTaskInfo.getTaskCount());
		Map<String, String> props = new HashMap<String, String>();
		if (dsTaskInfo.getDataTaskClassName() != null) {
		    props.put(DSTaskConstants.DATA_TASK_CLASS_NAME, dsTaskInfo.getDataTaskClassName());
		    triggerInfo.setDisallowConcurrentExecution(true);
		    return new TaskInfo(
					dsTaskInfo.getName(), DSTaskExt.class.getName(), props, triggerInfo);
		} else {
		    props.put(DSTaskConstants.DATA_SERVICE_NAME, dsTaskInfo.getServiceName());
		    props.put(DSTaskConstants.DATA_SERVICE_OPERATION_NAME, dsTaskInfo.getOperationName());
		    props.put(WebServiceCallTask.SERVICE_ACTION, "urn:" + dsTaskInfo.getOperationName());
	        return new TaskInfo(dsTaskInfo.getName(), DSTask.class.getName(), props, triggerInfo);
		}
	}
	
	private static Calendar dateToCal(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

    public static String extractHTTPEPR(AxisService axisService) {
        for (String epr : axisService.getEPRs()) {
            if (epr.startsWith("http:")) {
                return epr;
            }
        }
        return null;
    }

    public static boolean isInOutMEPInOperation(AxisService axisService, String opName) {
        Parameter param = axisService.getParameter(DSTaskConstants.DATA_SERVICE_OBJECT);
        if (param != null) {
            AxisOperation operation = axisService.getOperation(new QName(opName));
            if (WSDL2Constants.MEP_URI_IN_OUT.equals(operation.getMessageExchangePattern()) ||
                    WSDL2Constants.MEP_URI_OUT_ONLY.equals(operation.getMessageExchangePattern())) {
                return true;
            }
        }
        return false;
    }

    public static AxisConfiguration lookupAxisConfig(int tid) {
    	ConfigurationContext mainConfigCtx = TasksDSComponent.getConfigurationContextService().
				getServerConfigContext();
		AxisConfiguration tenantAxisConf;
		if (tid == MultitenantConstants.SUPER_TENANT_ID) {
			tenantAxisConf = mainConfigCtx.getAxisConfiguration();
		} else {
                    String tenantDomain = DBUtils.getTenantDomainFromId(tid);
		    tenantAxisConf = TenantAxisUtils.getTenantAxisConfiguration(tenantDomain, 
		    		mainConfigCtx);
		}
		return tenantAxisConf;
    }
    
	public static AxisService lookupAxisService(int tid, String serviceName) {
		return lookupAxisService(lookupAxisConfig(tid), serviceName);
	}
	
	public static AxisService lookupAxisService(AxisConfiguration tenantAxisConf, 
			String serviceName) {
		try {
			if (tenantAxisConf != null) {
			    return tenantAxisConf.getService(serviceName);
			} else {
				return null;
			}
		} catch (AxisFault e) {
			return null;
		}
	}
	
}
