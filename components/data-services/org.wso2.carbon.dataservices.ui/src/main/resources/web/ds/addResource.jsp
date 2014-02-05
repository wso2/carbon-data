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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Query" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Param" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Resource" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.CallQuery" %>

<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">

<carbon:breadcrumb
        label="Add Resources"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>


<%
    String serviceName = request.getParameter("serviceName");
    if (serviceName != null && serviceName.trim().length() > 0) {
        String backendServerURL = CarbonUIUtil.getServerURL(config
                .getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session
                .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        try {
        } catch (Exception e) {
            String errorMsg = e.getLocalizedMessage();
%>
<script type="text/javascript">
    location.href = "dsErrorPage.jsp?errorMsg=<%=errorMsg%>";
</script>
<%
        }
        //return;
    }
%>

<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session">
</jsp:useBean>
<script type="text/javascript" src="js/ui-validations.js"></script>
<%
    ArrayList<Query> queries = dataService.getQueries();
    boolean enableStreaming = true;
    boolean returnRequestStatus = false;
    boolean showReturnRequestStatus = false;
    Resource resource = null;
    String resourcePath = request.getParameter("resourcePath");
    String selectedQueryId = request.getParameter("selectedQueryId");
    String description = request.getParameter("resourceDesc");
    String resourceMethod = request.getParameter("resourceMethod");
    String flag = request.getParameter("flag");
    String existingResource = null;    
    if (resourcePath != null && resourcePath.trim().length() > 0) {
        resource = dataService.getResource(resourcePath);
        if (resource != null) {
            description = resource.getDescription();
            existingResource = "true";
            CallQuery callQuery = resource.getCallQuery();
            resourceMethod = resource.getMethod();
            if (callQuery != null) {
                if (selectedQueryId == null) {
                    //perhaps user is trying to change the associated query of an existing
                    //operation
                    selectedQueryId = callQuery.getHref();
                }
                Query query = dataService.getQuery(callQuery.getHref());
                if (query != null && query.getResult() == null) {
                	showReturnRequestStatus = true;
                }
            }
        } else {
            existingResource = "false";
        }
    }
    if (resource != null) {
        enableStreaming = !resource.isDisableStreaming();
        returnRequestStatus = resource.isReturnRequestStatus();
    }
    flag = (flag == null) ? "" : flag;
    resourcePath = (resourcePath == null) ? "" : resourcePath;
    description = (description == null) ? "" : description;
    selectedQueryId = (selectedQueryId == null) ? "" : selectedQueryId;
    resourceMethod = (resourceMethod == null ? "" : resourceMethod);
    serviceName = dataService.getName();

%>

<div id="middle">
    <h2>
        <%
            if (!selectedQueryId.equals("")) {
                if (!flag.equals("true")) {
        %>
        <fmt:message key="edit.resources"/><%out.write("(" + serviceName + "/" +resourcePath + ")");%>
        <% } else { %>
        <fmt:message key="datasources.add.resources"/><%out.write("(" + serviceName + ")");%>
        <%
            }
        } else {%>
        <fmt:message key="datasources.add.resources"/> <%out.write("(" + serviceName + ")");%>
        <%}%>
    </h2>

    <div id="workArea">
        <table class="styledLeft noBorders" id="dataSources" cellspacing="0" width="100%">
            <thead>
            <tr>
                <th colspan="2"><fmt:message key="service.resources"/></th>
            </tr>
            </thead>
            <form method="post" action="resourceProcessor.jsp" name="dataForm"
                  onsubmit="return validateAddResourceForm();">
                <input type="hidden" name="existingResource" value="<%=existingResource%>"/>
                <table class="styledLeft">
                    <tr>
                        <td>
                            <table class="normal">
                                <input type="hidden" value="<%=serviceName%>" name="serviceName">
                                <input type="hidden" value="<%=resourcePath%>" id="oldResourcePath"
                                       name="oldResourcePath"/>
                                <tr>
                                    <td><fmt:message key="datasources.resource.path"/><font
                                            color="red">*</font></td>
                                    <td><input type="text" name="resourcePath" id="resourcePath"
                                               value="<%=resourcePath%>"/></td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="datasources.resource.description"/></td>

                                    <td><textarea cols="40" rows="5" id="resourceDesc"
                                                  name="resourceDesc"><%=description%>
                                    </textarea></td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="datasources.resource.method"/><font
                                            color="red">*</font></td>
                                    <td><select type="text" name="resourceMethod"
                                                id="resourceMethod">
                                        <% if (resourceMethod.equals("")) { %>
                                        <option value="">--SELECT--</option>
                                        <option value="GET">GET</option>
                                        <option value="PUT">PUT</option>
                                        <option value="POST">POST</option>
                                        <option value="DELETE">DELETE</option>
                                        <% } else {
                                            if (resourceMethod.equals("GET")) {
                                        %>
                                        <option selected="selected" value="GET">GET</option>
                                        <% } else { %>
                                        <option value="GET">GET</option>

                                        <% }
                                            if (resourceMethod.equals("PUT")) { %>
                                        <option selected="selected" value="PUT">PUT</option>
                                        <% } else { %>
                                        <option value="PUT">PUT</option>

                                        <% }
                                            if (resourceMethod.equals("POST")) { %>
                                        <option selected="selected" value="POST">POST</option>
                                        <% } else { %>
                                        <option value="POST">POST</option>

                                        <% }
                                            if (resourceMethod.equals("DELETE")) { %>
                                        <option selected="selected" value="DELETE">DELETE</option>
                                        <% } else { %>
                                        <option value="DELETE">DELETE</option>

                                        <% }
                                        } %>
                                    </select>
                                    </td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="query.id"/><font color="red">*</font></td>
                                    <td>
                                        <select name="queryId" id="queryId"
                                                onchange="javascript:location.href = 'addResource.jsp?selectedQueryId='+this.options[this.selectedIndex].value+'&resourcePath='+document.getElementById('resourcePath').value+'&resourceMethod='+document.getElementById('resourceMethod').value+'&resourceDesc='+document.getElementById('resourceDesc').value+'&flag=true';">


                                            <% if (selectedQueryId != null && selectedQueryId.trim().equals("")) {%>
                                            <option value="" selected="selected"></option>
                                            <% } else {%>
                                            <option value=""></option>
                                            <% }%>

                                            <%
                                                if (queries != null && queries.size() > 0) {
                                                    Iterator iterator = queries.iterator();
                                                    while (iterator.hasNext()) {
                                                        Query query = (Query) iterator.next();
                                                        if (selectedQueryId != null && selectedQueryId.trim().equals(query.getId())) {
                                            %>
                                            <option value="<%=query.getId()%>"
                                                    selected="selected"><%=query.getId()%>
                                            </option>
                                            <%
                                            } else {
                                            %>
                                            <option value="<%=query.getId()%>"><%=query.getId()%>
                                            </option>
                                            <%
                                                        }
                                                    }
                                                }
                                            %>

                                        </select>
                                    </td>
                                </tr>
                                <!-- Display Parameters -->
                                <%
                                    if (selectedQueryId != null && selectedQueryId.trim().length() > 0) {
                                        Query query = dataService.getQuery(selectedQueryId);
                                        if (query != null) {
                                            Param[] params = query.getParams();
                                            if (params != null) {
                                                if (params.length > 0) {
                                                    //Params exist.Draw column headers
                                %>
                                <tr>
                                    <td colspan="2"><b><fmt:message
                                            key="data.services.resource.parameters"/></b></td>
                                </tr>
                                <tr>
                                    <td colspan="2">
                                        <table class="styledInner" cellspacing="0"
                                               id="resourceParametersTable">
                                            <tr>
                                                <td><b><fmt:message key="query.parameter.name"/></b>
                                                </td>
                                                <td><b><fmt:message
                                                        key="data.services.resource.parameter.name"/></b>
                                                </td>
                                            </tr>

                                            <%
                                                for (int a = 0; a < params.length; a++) {
                                            %>
                                            <tr>
                                                <td><%=params[a].getName()%>
                                                </td>
                                                <td><%=(params[a].getOperationParamName() == null) ? params[a].getName() : params[a].getOperationParamName()%>
                                                </td>
                                            </tr>
                                            <%
                                                }
                                            %>
                                        </table>
                                    </td>
                                </tr>
                                <%

                                                }
                                            }
                                        }
                                    }
                                %>
                                <tr>
                                    <td align="left">
                                        <input type="checkbox" id="enableStreaming"
                                               name="enableStreaming"  <%=(enableStreaming) ? "checked=\"checked\"" : ""%>
                                               value=<%=enableStreaming%>/>
                                        <label for="enableStreaming"><fmt:message
                                                key="service.enable.streaming"/></label>
                                    </td>
                                </tr>
                                <tr <%= showReturnRequestStatus ? "" : "style='display:none'" %> >
                                    <td>
                                        <input type="checkbox" id="returnRequestStatus"
                                               name="returnRequestStatus"  <%=(returnRequestStatus) ? "checked=\"checked\"" : ""%>
                                               value=<%=returnRequestStatus%>/>
                                        <label for="returnRequestStatus"><fmt:message
                                                key="service.return.request.status"/></label>
                                    </td>
                                </tr>                                
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="2"><input class="button"
                                                                 type="submit"
                                                                 value="<fmt:message key="save"/>"/>
                            <input
                                    class="button" type="button" value="<fmt:message key="cancel"/>"
                                    onclick="location.href = 'resources.jsp?ordinal=4'"/></td>
                    </tr>
                </table>
            </form>
        </table>
    </div>
</div>
<script type="text/javascript">
    alternateTableRows('resourceParametersTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>