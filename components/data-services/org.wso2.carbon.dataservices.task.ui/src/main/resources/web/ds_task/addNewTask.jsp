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
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.dataservices.task.ui.DSTaskClient" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<link href="css/task.css" rel="stylesheet" type="text/css" media="all"/>
<script type="text/javascript" src="js/taskcommon.js"></script>
<fmt:bundle basename="org.wso2.carbon.dataservices.task.ui.i18n.Resources">
    <carbon:breadcrumb label="dataservices.task.header.new"
                       resourceBundle="org.wso2.carbon.dataservices.task.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
    <%
        DSTaskClient client;
        String backendServerUrl = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configurationContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        try {
            client = new DSTaskClient(cookie, backendServerUrl,
                    configurationContext);
    %>
    <form method="post" name="taskcreationform" id="taskcreationform" action="saveTask.jsp"
          onsubmit="return validateTaskInputs();">

        <input type="hidden" name="taskTrigger" id="taskTrigger" value="simple"/>
        <input id="dataServiceName" name="dataServiceName" value="" type="hidden"/>
        <input id="operationName" name="operationName" value="" type="hidden"/>
        <input id="saveMode" name="saveMode" value="add" type="hidden"/>

        <div id="middle">
            <h2><fmt:message key="dataservices.task.header.new"/></h2>

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
                            <input id="taskName" name="taskName" class="longInput" type="text"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="middle-header"><fmt:message
                                key="dataservices.task.trigger.text"/></td>
                    </tr>

                    <tr id="triggerCountTR">
                        <td><fmt:message key="dataservices.task.trigger.count"/><span
                                class="required">*</span></td>
                        <td>
                            <input id="triggerCount" name="triggerCount" class="longInput"
                                   type="text" value=""/>
                            <fmt:message key="dataservices.task.repeat.count.infinite"/>
                        </td>
                    </tr>
                    <tr id="triggerIntervalTR">
                        <td><fmt:message key="dataservices.task.trigger.interval"/><span
                                class="required">*</span></td>
                        <td>
                            <input id="triggerInterval" name="triggerInterval" class="longInput"
                                   type="text"
                                   value=""/>
                            <fmt:message key="dataservices.task.interval.units"/>
                        </td>
                    </tr>
                    <tr id="startTimeTR">
                        <td><fmt:message key="dataservices.task.trigger.start.time"/></td>
                        <td>
                            <input id="startTime" name="startTime" class="longInput"
                                   type="text"
                                   value=""/>
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
                            <select id="scheduleType" name="scheduleType" onchange="getScheduleType(this.value)">
                                <option value="">---SELECT---</option>
                                <option value="DataService Operation">DataService Operation</option>
                                <option value="DataService Task Class">DataService Task Class</option>
                            </select>
                        </td>
                    </tr>
                    <tr id="dsTaskService" style="display:none;">
                        <td style="width:150px"><fmt:message
                                key="dataservices.task.service.name"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <select id="serviceList" name="serviceList" class="longInput"
                                    onchange="getOperations(this);">
                                <option value="">--------------------SELECT-----------------------
                                </option>
                                <%
                                    String[] services = client.getAllSchedulableDataServices();
                                    if (services != null) {
                                        for (String service : services) { %>
                                <option value="<%=service%>">
                                    <%=service%>
                                </option>
                                <% }
                                }%>
                            </select>
                        </td>
                    </tr>
                    <tr id="dsTaskOperation" style="display:none;">
                        <td style="width:150px"><fmt:message
                                key="dataservices.operation.name"/><span
                                class="required">*</span></td>

                        <td align="left">
                            <select id="operationList" name="operationList" class="longInput"
                                    onchange="setOperationName()">
                            </select>
                        </td>
                    </tr>
                    <tr id="dssTaskClassRow" name="dssTaskClassRow" style="display:none;">
                        <td>DataService Task Class</td>
                        <td><input id="dssTaskClass" name="dssTaskClass" class="longInput" type="text" value=""/></td>
                    </tr>

                    <tr>
                        <td class="buttonRow" colspan="3">
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

