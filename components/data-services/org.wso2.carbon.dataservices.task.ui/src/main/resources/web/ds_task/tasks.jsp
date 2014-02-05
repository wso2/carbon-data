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
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.dataservices.task.ui.DSTaskClient" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    boolean disableAddTask = Boolean.valueOf((String) config.getServletContext().getAttribute(
            CarbonConstants.PRODUCT_XML_WSO2CARBON + DSTaskClient.DISABLE_ADD_TASK));
%>

<link href="css/task.css" rel="stylesheet" type="text/css" media="all"/>
<script type="text/javascript" src="js/taskcommon.js"></script>

<fmt:bundle basename="org.wso2.carbon.dataservices.task.ui.i18n.Resources">
    <carbon:breadcrumb resourceBundle="org.wso2.carbon.dataservices.task.ui.i18n.Resources"
                       topPage="true" request="<%=request%>" label="dataservices.task.header"/>
    <div id="middle">
        <h2><fmt:message key="dataservices.task.header"/></h2>

        <div id="workArea">
            <%
                DSTaskClient client;
                try {
                    String backendServerUrl = CarbonUIUtil.getServerURL(config.getServletContext(),
                            session);
                    ConfigurationContext configurationContext = (ConfigurationContext) config.
                            getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                    String cookie = (String) session.getAttribute(
                            ServerConstants.ADMIN_SERVICE_COOKIE);

                    client = new DSTaskClient(cookie, backendServerUrl,
                            configurationContext);
                    String[] taskNames = client.getAllTaskNames();
                    if (taskNames != null && taskNames.length != 0) {

            %>
            <p><fmt:message key="available.defined.scheduled.tasks"/></p>
            <br/>
            <table id="myTable" class="styledLeft">
                <thead>
                <tr>
                    <th><fmt:message key="dataservices.task.name"/></th>
                    <th><fmt:message key="dataservices.task.action"/></th>
                </tr>
                </thead>
                <tbody>

                <%
                    for (String taskName : taskNames) {
                        if (taskName != null) {
                %>
                <tr id="tr_<%=taskName%>">

                    <td>
                        <%=taskName%>
                    </td>
                    <td>
                        <a href="javascript:editRow('<%=taskName%>')" id="config_link"
                           class="edit-icon-link"><fmt:message key="dataservices.task.edit"/></a>
                        <a href="javascript:deleteRow('<%=taskName%>', '<fmt:message key="dataservices.task.delete.waring"/>')"
                           id="delete_link" class="delete-icon-link"><fmt:message
                                key="dataservices.task.property.delete"/></a>
                    </td>

                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
            <%} else {%>
            <p><fmt:message key="dataservices.task.list.empty.text"/></p>
            <br/>
            <%}%>
            <%
                if (!disableAddTask) {
            %>
            <div style="height:30px;">
                <a href="javascript:document.location.href='addNewTask.jsp?ordinal=1'"
                   class="add-icon-link"><fmt:message key="dataservices.task.button.add.text"/></a>
            </div>
            <%
                }
            %>
            <%

            } catch (Throwable e) {
                request.getSession().setAttribute(DSTaskClient.EXCEPTION, e);
            %>
            <script type="text/javascript">
                jQuery(document).ready(function() {
                    CARBON.showErrorDialog('<%=e.getMessage()%>');
                });
            </script>
            <%
                }
            %>
        </div>
    </div>
</fmt:bundle>
<script type="text/javascript">
    alternateTableRows('myTable', 'tableEvenRow', 'tableOddRow');
</script>
