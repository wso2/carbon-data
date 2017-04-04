<%--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 --%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Query" %>
<%@ page import="java.util.Iterator" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<carbon:breadcrumb 
		label="Queries"
		resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
		topPage="false" 
		request="<%=request%>" />
<%
    String serviceName = request.getParameter("serviceName");
    if (serviceName != null && serviceName.trim().length() > 0) {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
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
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>
<script type="text/javascript" src="js/ui-validations.js"></script>
<div id="middle">
<h2><fmt:message key="service.queries"/></h2>
	<div id="workArea">
        <form method="post" action="operations.jsp" name="dataForm"
            onsubmit="return validateQueriesForm();">
            <table class="styledLeft" id="query-table">
                    <%
                        ArrayList<Query> queries = dataService.getQueries();
                        if(queries != null && queries.size() > 0){
                            %>
                <thead>
                    <tr>
                        <th width="20%"><fmt:message key="query" /></th>
                        <th width="20%"><fmt:message key="datasource.type" /></th>
                        <th width="60%"><fmt:message key="actions" /></th>
                    </tr>
                </thead>
                <tbody>                    
                    <%
                            Iterator iterator = queries.iterator();
                            while(iterator.hasNext()){
                                Query query = (Query)iterator.next();
                                if(query != null){
                                    %>
                    <tr>
                        <td><%=query.getId()%></td>
                        <td><%=query.getConfigToUse()%></td>
                        <input type="hidden" id="<%=query.getId()%>" name="<%=query.getId()%>" value="<%=query.getId()%>" />
                        <td>
                            <a class="icon-link" style="background-image:url(../admin/images/edit.gif);" href="addQuery.jsp?queryId=<%=query.getId()%>"><fmt:message key="edit.query" /></a>
                            <a class="icon-link" style="background-image:url(../admin/images/delete.gif);" onclick="deleteQuery(document.getElementById('<%=query.getId()%>').value);" href="#"><fmt:message key="delete.query" /></a>
                        </td>
                    </tr>

                    <%
                                }
                            }
                        }
                    %>
                    <tr>
                        <td class="addNewQuery" colspan="3">
                            <a class="icon-link" style="background-image:url(../admin/images/add.gif);" href="addQuery.jsp"><fmt:message
                            key="add.new.query" /></a>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="3"><input class="button" type="button"
                        value="< <fmt:message key="back"/>" onclick="location.href = 'dataSources.jsp?ordinal=1'" /> <input class="button" type="submit"
                        value="<fmt:message key="next"/> >" /> <input class="button"
                        type="button" value="<fmt:message key="finish"/>" onclick="location.href = 'wizardDoneProcessor.jsp'" />
                        <input class="button" type="button" value="<fmt:message key="save.as.draft"/>" onclick="location.href = 'wizardDoneProcessor.jsp?flag=wip'"/> <input
                        class="button" type="button" value="<fmt:message key="cancel"/>"
                        onclick="location.href = '../service-mgt/index.jsp'" /></td>
                    </tr>
                </tbody>
            </table>
        </form>
	</div>
</div>
</fmt:bundle>
