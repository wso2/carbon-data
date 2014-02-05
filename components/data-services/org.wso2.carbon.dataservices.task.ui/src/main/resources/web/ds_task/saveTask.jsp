<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.dataservices.task.ui.stub.xsd.DSTaskInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.wso2.carbon.dataservices.task.ui.DSTaskClient" %>
<%@ page import="org.wso2.carbon.dataservices.task.ui.DSTaskManagementHelper" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<link href="css/task.css" rel="stylesheet" type="text/css" media="all"/>
<script type="text/javascript" src="js/taskcommon.js"></script>

<carbon:breadcrumb label=""
                   resourceBundle="org.wso2.carbon.dataservices.task.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>
<fmt:bundle basename="org.wso2.carbon.dataservices.task.ui.i18n.Resources">
    <%
        try {
            DSTaskClient client;
            Map<String, String> jobDataMap;
            String saveMode = request.getParameter("saveMode");
            String taskName = request.getParameter("taskName");

            String backendServerUrl = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configurationContext = (ConfigurationContext) config.
                    getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            client = new DSTaskClient(cookie, backendServerUrl, configurationContext);

            if ("add".equals(saveMode)) {
                DSTaskInfo dsTaskInfo = DSTaskManagementHelper.createTaskInfo(request);
                if (client.isTaskScheduled(dsTaskInfo.getName())) {
                    throw new Exception("The task '" + dsTaskInfo.getName() + "' is already scheduled");
                }
                //client.scheduleTask(dsTaskInfo);
                client.rescheduleTask(dsTaskInfo);
            } else if ("edit".equals(saveMode)) {
                DSTaskInfo dsTaskInfo = DSTaskManagementHelper.createTaskInfo(request);
                //if (client.isTaskScheduled(dsTaskInfo.getName())) {
                //	client.rescheduleTask(dsTaskInfo);
                //} else {
                //	client.scheduleTask(dsTaskInfo);
                //}
                client.rescheduleTask(dsTaskInfo);
            } else if ("delete".equals(saveMode)) {
                client.deleteTask(taskName);
            }
    %>
    <script type="text/javascript">
        forward("tasks.jsp?region=region1&item=ds_task_menu");
    </script>
    <%
    } catch (Exception e) {
    %>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showErrorDialog('<%=e.getMessage()%>', function () {
                goBackOnePage();
            }, function () {
                goBackOnePage();
            });
        });
    </script>
    <%}%>
</fmt:bundle>