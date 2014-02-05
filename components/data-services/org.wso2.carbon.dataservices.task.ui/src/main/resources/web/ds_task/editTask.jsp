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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.dataservices.task.ui.DSTaskClient" %>
<%@ page import="org.wso2.carbon.dataservices.task.ui.stub.xsd.DSTaskInfo" %>
<%@ page import="org.wso2.carbon.dataservices.task.ui.DSTaskManagementHelper" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<link href="css/task.css" rel="stylesheet" type="text/css" media="all"/>
<script type="text/javascript" src="js/taskcommon.js"></script>
<fmt:bundle basename="org.wso2.carbon.dataservices.task.ui.i18n.Resources">
    <carbon:breadcrumb label="dataservices.task.header.edit"
                       resourceBundle="org.wso2.carbon.dataservices.task.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
    <%
        DSTaskClient client;
        String backendServerUrl = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configurationContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        DSTaskInfo taskInfo;
        String taskName = request.getParameter("taskName");

        try {
            client = new DSTaskClient(cookie, backendServerUrl,
                    configurationContext);
            taskInfo = client.getTaskInfo(taskName);
            String taskClass = taskInfo.getDataTaskClassName();
    %>
    <form method="post" name="taskcreationform" id="taskcreationform" action="saveTask.jsp"
          onsubmit="return validateTaskInputs();">
        
        <input id="operationName" name="operationName" value="<%=taskInfo.getOperationName()%>"
               type="hidden"/>
        <input id="dataServiceName" name="dataServiceName" value="<%=taskInfo.getServiceName()%>"
               type="hidden"/>
        <input id="scheduleType" name="scheduleType" value="<%=(taskClass != null && !"".equals(taskClass)) ?  "DataService Task Class" : "DataService Operation"%>"
               type="hidden"/>
        <div id="middle">
            <h2><fmt:message key="dataservices.task.header.edit"/></h2>

            <div id="workArea">

                <table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
                    <thead>
                    <tr>
                        <th colspan="3"><fmt:message
                                key="dataservices.task.basic.information"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td style="width:150px"><fmt:message key="dataservices.task.name"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <input id="taskName" name="taskName" class="longInput" type="text"
                                   value="<%=taskInfo.getName()%>" readonly="readonly"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="middle-header"><fmt:message
                                key="dataservices.task.trigger.text"/></td>
                    </tr>
                    <input type="hidden" name="taskTrigger" id="taskTrigger" value="simple"/>

                    <tr id="triggerCountTR">
                        <td><fmt:message key="dataservices.task.trigger.count"/><span
                                class="required">*</span></td>
                        <td>
                            <input id="triggerCount" name="triggerCount" class="longInput"
                                   type="text" value="<%=taskInfo.getTaskCount()%>"/>
                            <fmt:message key="dataservices.task.repeat.count.infinite"/>
                        </td>
                    </tr>
                    <tr id="triggerIntervalTR">
                        <td><fmt:message key="dataservices.task.trigger.interval"/><span
                                class="required">*</span></td>
                        <td>
                            <input id="triggerInterval" name="triggerInterval" class="longInput"
                                   type="text"
                                   value="<%=taskInfo.getTaskInterval()%>"/>
                            <fmt:message key="dataservices.task.interval.units"/>
                        </td>
                    </tr>
                    <tr id="startTimeTR">
                        <td><fmt:message key="dataservices.task.trigger.start.time"/></td>
                        <td>
                            <input id="startTime" name="startTime" class="longInput"
                                   type="text"
                                   value="<%=DSTaskManagementHelper.formatStartTime(taskInfo.getStartTime())%>"/>
                            <fmt:message key="dataservices.task.start.time.format"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="middle-header"><fmt:message
                                key="dataservices.task.miscellaneous.information"/></td>
                    </tr>

                    <tr>
                        <td>Scheduling Type</td>
                        <td>
                            <select id="scheduleType" name="scheduleType" disabled="disabled"
                                    onchange="getScheduleType(this.value)">
                                <option value="">---SELECT---</option>
                                <option value="DataService Operation" <%=(taskClass == null) ? "selected=\"selected\"" : ""%>>DataService Operation</option>
                                <option value="DataService Task Class" <%=(taskClass != null && !"".equals(taskClass) ? "selected=\"selected\"" : "")%>>DataService Task Class</option>
                            </select>
                        </td>
                    </tr>

                    <% if (taskClass != null && !"".equals(taskClass)) { %>
                    <tr id="dssTaskClassRow" name="dssTaskClassRow" >
                        <td>DataService Task Class</td>
                        <td><input id="dssTaskClass" name="dssTaskClass" class="longInput"
                                   type="text" value="<%=taskClass%>" readonly/></td>
                    </tr>
                    <%} else {%>
                    <tr id="dsTaskService">
                        <td style="width:150px"><fmt:message
                                key="dataservices.task.service.name"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <select disabled="true" id="serviceList" name="serviceList"
                                    class="longInput"
                                    onchange="getOperations(this);">
                                <%
                                    String[] services = client.getAllSchedulableDataServices();
                                    if (services != null) {
                                        for (String service : services) { %>
                                <option value="<%=service%>" <%=(service.equals(taskInfo.getServiceName())) ? "selected=\"selected\"" : "" %>>
                                    <%=service%>
                                </option>
                                <% }
                                }%>
                            </select>
                        </td>
                    </tr>
                    <tr id="dsTaskOperation">
                        <td style="width:150px"><fmt:message
                                key="dataservices.operation.name"/><span
                                class="required">*</span></td>

                        <td align="left">
                            <select disabled="true" id="operationList" name="operationList"
                                    class="longInput"
                                    onchange="setOperationName()">
                                <%
                                    String[] operations = client.getNoParamDSOperations(taskInfo.getServiceName());
                                    if (operations != null) {
                                        for (String operation : operations) { %>
                                <option value="<%=operation%>" <%=((operation.equals(taskInfo.getOperationName())) ? "selected=\"selected\"" : "")%> >
                                    <%=operation%>
                                </option>
                                <% }
                                }%>
                            </select>
                        </td>
                    </tr>
                    <% } %>
                        <%--<input id="saveMode" name="saveMode" value="edit" type="hidden"/>--%>

                    <tr>
                        <td class="buttonRow" colspan="3">
                            <input type="hidden" name="saveMode" id="saveMode" value="edit"/>
                            <input class="button" type="submit"
                                   value="<fmt:message key="dataservices.task.button.schedule.text"/>"
                                   onclick="return validateTaskInputs();"/>
                            <input class="button" type="button"
                                   value="<fmt:message key="dataservices.task.cancel.button.text"/>"
                                   onclick="document.location.href='tasks.jsp?region=region1&item=ds_task_menu&ordinal=0';"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <div id="tdiv"></div>
        </div>

    </form>
    <%

    } catch (Throwable e) {
        Log log = LogFactory.getLog(this.getClass());
        log.error(e);
    %>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showErrorDialog('<%=e.getMessage()%>');
        });
    </script>
    <%
            return;
        }
    %>
</fmt:bundle>

