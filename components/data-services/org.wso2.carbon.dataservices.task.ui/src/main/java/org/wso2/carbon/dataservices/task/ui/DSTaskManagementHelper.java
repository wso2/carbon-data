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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.task.ui.stub.xsd.DSTaskInfo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This class represents a helper class for scheduled tasks related functions.
 */
public class DSTaskManagementHelper {
    
    private static final Log log = LogFactory.getLog(DSTaskManagementHelper.class);

    public static DSTaskInfo createTaskInfo(
            HttpServletRequest request) throws ServletException, AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Creating the task description corresponds to the task");
        }

        DSTaskInfo dsTaskInfo = new DSTaskInfo();

        String taskName = request.getParameter("taskName");
        if (taskName == null && "".equals(taskName)) {
            taskName = request.getParameter("taskName_hidden");
            if (taskName == null && "".equals(taskName)) {
                handleException("Task Name cannot be empty");
            }
        }

        dsTaskInfo.setName(taskName.trim());
        
        String startTimeAsString = request.getParameter("startTime");
        if (startTimeAsString != null && !"".equals(startTimeAsString.trim())) {
        	dsTaskInfo.setStartTime(getProcessedStartTime(startTimeAsString));
        }

		String interval = request.getParameter("triggerInterval");
		if (interval != null && !"".equals(interval)) {
			try {
				dsTaskInfo.setTaskInterval(Integer.parseInt(interval.trim()));
			} catch (NumberFormatException e) {
				handleException("Invalid value for interval (Expected type is integer) : "
						+ interval);
			}
		}

		String count = request.getParameter("triggerCount");
		if (count != null && !"".equals(count)) {
			try {
				dsTaskInfo.setTaskCount(Integer.parseInt(count.trim()));
			} catch (NumberFormatException e) {
				handleException("Invalid value for Count (Expected type is int) : "
						+ count);
			}
		}

        String scheduleType = request.getParameter("scheduleType");
        scheduleType = (scheduleType == null) ? "" : scheduleType;

        if (scheduleType.equals("DataService Operation")) {
            String cron = request.getParameter("triggerCron");
            if (cron != null && !"".equals(cron)) {
                dsTaskInfo.setCronExpression(cron.trim());
            }

            String dataServiceName = request.getParameter("dataServiceName");
            if (dataServiceName == null || "".equals(dataServiceName)) {
                handleException("Service Name cannot be null");
            }

            dsTaskInfo.setServiceName(dataServiceName);

            String operationName = request.getParameter("operationName");
            if (operationName == null || "".equals(operationName)) {
                handleException("Operation name cannot be null");
            }

            dsTaskInfo.setOperationName(operationName);
        } else if (scheduleType.equals("DataService Task Class")) {
            String taskClass = request.getParameter("dssTaskClass");
            if (taskClass != null && !"".equals(taskClass)) {
                dsTaskInfo.setDataTaskClassName(taskClass.trim());
            }
        }
        
        return dsTaskInfo;
    }

    private static void handleException(String msg) throws ServletException {
        log.error(msg);
        throw new ServletException(msg);
    }

    private static Calendar getProcessedStartTime(String startTimeAsString) throws AxisFault {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date startTime;
        try {
            startTime = df.parse(startTimeAsString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(startTime);
            return cal;
        } catch (ParseException e) {
            throw new AxisFault("Invalid DateTime format", e);
        }
    }

    public static String formatStartTime(Calendar startTime) throws AxisFault {
        if (startTime == null) {
            return "";
        }
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return df.format(startTime.getTime());
    }

}
