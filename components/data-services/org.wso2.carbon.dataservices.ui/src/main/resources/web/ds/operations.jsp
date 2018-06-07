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
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Operation" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.CallQuery" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="org.owasp.encoder.Encode" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<carbon:breadcrumb 
		label="SOAP Operations"
		resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
		topPage="false" 
		request="<%=request%>" />
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>
<script type="text/javascript" src="js/ui-validations.js"></script>
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
    }
%>

<%
    ArrayList<Operation> operations = dataService.getOperations();
    boolean hasRecords = false;
    Iterator iterator = null;
    if(operations != null && operations.size() > 0){
        hasRecords = true;
        iterator = operations.iterator();
    }

%>

<div id="middle">
<h2><fmt:message key="service.operations"/></h2>
	<div id="workArea">
	<form method="post" action="resources.jsp" name="dataForm"
		onsubmit="return validateQueriesForm();">

    <table class="styledLeft" id="operation-table">
        <%if(hasRecords){%>
		<thead>
			<tr>
				<th width="20%"><fmt:message key="operation.name" /></th>
				<th width="20%"><fmt:message key="query" /></th>
				<th width="60%"><fmt:message key="actions" /></th>
			</tr>
		</thead>
        <%}%>
		<tbody>
            <tr style="display:none">
                <td><input type="text" name="serviceName" value="<%=Encode.forHtmlAttribute(serviceName)%>" /> </td>
            </tr>
            <%
                if(hasRecords){
                    while(iterator.hasNext()){
                        Operation operation = (Operation)iterator.next();
                        if(operation != null){
                        	String queryList = "";
                        	if (operation.getCallQuery() == null && operation.getCallQueryGroup() != null) {
                        		List<CallQuery> callQueries = operation.getCallQueryGroup().getCallQueries();
                        		if (callQueries != null) {
                        			for(CallQuery callQuery : callQueries) {
                        				if (queryList.equals("")) {
                        					queryList = queryList + callQuery.getHref();
                        				} else {
                        					queryList = queryList + "," + callQuery.getHref();
                        				}
                        			}
                        		}
                        	}
                            %>
            <tr>
                <input type="hidden" id="<%=operation.getName()%>" name="<%=operation.getName()%>" value="<%=operation.getName()%>" />
                <td><%=operation.getName()%></td>
                <%if(operation.getCallQuery() == null) { %>
                	<td><%=Encode.forHtmlContent(queryList)%></td>
                <%} else { %>
                	<td><%=Encode.forHtmlContent(operation.getCallQuery().getHref())%></td>
                <%} %>
                <td>
                   <%
                        String operationsDesc = operation.getDescription();
                   		boolean disableStreaming = operation.isDisableStreaming();
                        operationsDesc = (operationsDesc == null) ? "" : operationsDesc;
                        String editURI = "addOperation.jsp?action=edit&operationName="+operation.getName()+"&operationDesc="+operationsDesc+"&disableStreaming="+disableStreaming;
                    %>
                    <a class="icon-link" style="background-image:url(../admin/images/edit.gif);" href="<%=editURI%>"><fmt:message key="edit.operation" /></a>
                    <a class="icon-link" style="background-image:url(../admin/images/delete.gif);" href="#" onclick="deleteOperations(document.getElementById('<%=operation.getName()%>').value);"><fmt:message key="delete.operation" /></a>
                </td>
            </tr>
            <%
                        }
                    }
                }
			%>
			<tr>
				<td colspan="3">
					<a class="icon-link" style="background-image:url(../admin/images/add.gif);" href="addOperation.jsp"><fmt:message
				key="add.new.operation" /></a>	
				</td>
			</tr>
			<tr>
				<td class="buttonRow" colspan="3"><input class="button" type="button"
                        value="< <fmt:message key="back"/>" onclick="location.href = 'queries.jsp?ordinal=2'" /> <input class="button" type="submit"
							value="<fmt:message key="next"/> >" /> <input class="button"
							type="button" value="<fmt:message key="finish"/>" onclick="location.href = 'wizardDoneProcessor.jsp'" /> <input class="button"
                            type="button" value="<fmt:message key="save.as.draft"/>" onclick="location.href = 'wizardDoneProcessor.jsp?flag=wip'"/> <input
							class="button" type="button" value="<fmt:message key="cancel"/>"
							onclick="location.href = '../service-mgt/index.jsp'" /></td>
			</tr>
		</tbody>
	</table>
	</form>
	</div>
</div>
</fmt:bundle>
