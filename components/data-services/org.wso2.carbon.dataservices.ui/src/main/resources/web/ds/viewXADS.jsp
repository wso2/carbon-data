<!--
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
 -->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.XADataSource" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Data" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Iterator" %>

<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<carbon:breadcrumb 
		label="XA Datasources"
		resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
		topPage="false" 
		request="<%=request%>" />
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"></jsp:useBean>
<script type="text/javascript" src="js/ui-validations.js"></script>    

<div id="middle">
<h2><fmt:message key="view.xa.datasources.heading"/></h2>
	<div id="workArea">
	<form method="post" action="serviceDetails.jsp?flag=addXAData&ordinal=0" name="dataForm">      
	
        <input type="hidden" name="serviceName" id="serviceName" value="<%=request.getParameter("serviceName") %>" />	
        <input type="hidden" name="description" id="description" value="<%=request.getParameter("description") %>" />
        <input type="hidden" name="txManagerClass" id="txManagerClass" value="<%=request.getParameter("txManagerClass") %>" />
        <input type="hidden" name="txManagerName" id="txManagerName" value="<%=request.getParameter("txManagerName") %>" />
        <input type="hidden" name="batchResponse" id="batchResponse" value="<%=request.getParameter("batchResponse") %>" />
        <input type="hidden" name="enableXA" id="enableXA" value="<%=request.getParameter("enableXA") %>" />
        <input type="hidden" name="useAppServerTS" id="isUseAppServerTS" value="<%=request.getParameter("useAppServerTS") %>" />
        <input type="hidden" name="enableBoxcarring" id="enableBoxcarring" value="<%=request.getParameter("enableBoxcarring") %>" />
        <input type="hidden" name="protectedTokens" id="protectedTokens" value="<%=request.getParameter("protectedTokens") %>" />
        <input type="hidden" name="passwordProvider" id="passwordProvider" value="<%=request.getParameter("passwordProvider") %>" />
        <input type="hidden" name="serviceNamespace" id="protectedTokens" value="<%=request.getParameter("serviceNamespace") %>" />
	 
        <table class="styledLeft" id="xa-datasource-table">
            <%
            String description = request.getParameter("description");
        	String serviceName = request.getParameter("serviceName");
            String batchRequest = request.getParameter("batchResponse");
            String isUseAppServerTS = request.getParameter("useAppServerTS");
            String boxcarring = request.getParameter("enableBoxcarring");
            String protectedTokens = request.getParameter("protectedTokens");
            String passwordProvider = request.getParameter("passwordProvider");
            String serviceNamespace = request.getParameter("serviceNamespace");
            String txManagerClass = request.getParameter("txManagerClass");
            String txManagerName = request.getParameter("txManagerName");
            String enableXA = request.getParameter("enableXA"); 
            String disableStreaming = request.getParameter("disableStreaming");
            String txManagerCleanupMethod = request.getParameter("txManagerCleanupMethod");
            description = (description == null) ? "" : description;
            txManagerCleanupMethod = (txManagerCleanupMethod == null) ? "" : txManagerCleanupMethod;
            serviceNamespace = (serviceNamespace == null) ? "" : serviceNamespace;
            txManagerClass = (txManagerClass == null) ? "" : txManagerClass;
            txManagerName = (txManagerName == null) ? "" : txManagerName;
            protectedTokens = (protectedTokens == null) ? "" : protectedTokens;
            passwordProvider = (passwordProvider == null) ? "" : passwordProvider;
            
            if (serviceName != null && serviceName.trim().length() > 0) {
            	dataService.setName(serviceName);
            }
            if (batchRequest != null && batchRequest.trim().length() > 0) {
            	dataService.setBatchRequest(Boolean.parseBoolean(batchRequest));
            }
            if (enableXA != null && enableXA.trim().length() > 0) {
            	dataService.setEnableXA(Boolean.parseBoolean(enableXA));
            }
            if (isUseAppServerTS != null && isUseAppServerTS.trim().length() > 0) {
            	dataService.setIsUseAppServerTS(Boolean.parseBoolean(isUseAppServerTS));
            }
            if (description != null && description.trim().length() > 0) {
            	dataService.setDescription(description);
            }
            if (serviceNamespace != null  && serviceNamespace.trim().length() > 0) {
            	dataService.setServiceNamespace(serviceNamespace);
            }
            if (txManagerClass != null && txManagerClass.trim().length() > 0) {
            	dataService.setTxManagerClass(txManagerClass);
            }
            if (txManagerName != null && txManagerName.trim().length() > 0) {
            	dataService.setTxManagerName(txManagerName);
            }
            if (boxcarring != null && boxcarring.trim().length() > 0) {
            	dataService.setBoxcarring(Boolean.parseBoolean(boxcarring));
            }  
            if (disableStreaming != null && disableStreaming.trim().length() > 0) {
            	dataService.setDisableStreaming(true);
            } 
            if (protectedTokens != null && protectedTokens.trim().length() > 0) {
            	dataService.setProtectedTokens(protectedTokens);
            }
            if (passwordProvider != null && passwordProvider.trim().length() > 0) {
            	dataService.setPasswordProvider(passwordProvider);
            }
            if (txManagerCleanupMethod != null && txManagerCleanupMethod.trim().length() > 0) {
            	dataService.setTxManagerCleanupMethod(txManagerCleanupMethod);
            }
            List<XADataSource> xaDSList = dataService.getXADataSources();
            if (xaDSList != null && xaDSList.size() > 0) {
                 String xaId = null;
            %>
            <thead>
            <tr>
                <th width="20%"><fmt:message key="datasource.name"/></th>
                <th width="60%"><fmt:message key="actions"/></th>
            </tr>
            </thead>
            <tbody>

            <%
                Iterator<XADataSource> iterator = xaDSList.iterator();
                while (iterator.hasNext()) {
                    XADataSource xaDS = iterator.next();
                    if (xaDS != null) {
                        xaId = xaDS.getId();
            %>

            <tr>
                <td><%=xaId%>
                </td>
                <td>
                    <a class="icon-link" style="background-image:url(../admin/images/edit.gif);" href="manageXADS.jsp?xaId=<%=xaId%>"><fmt:message key="edit"/></a>
                    <a class="icon-link" style="background-image:url(../admin/images/delete.gif);" onclick="deleteXADatasource('<%=xaId%>');" href="#"><fmt:message key="delete"/></a>
                </td>
            </tr>

            <%

                        }
                    }
                }               
            %>
            <tr>
                <td colspan="2">
                    <a class="icon-link" style="background-image:url(../admin/images/add.gif);"
                       href="manageXADS.jsp"><fmt:message
                            key="add.new.xa.datasource"/></a>
                </td>
            </tr>
            <tr>
                <td class="buttonRow" colspan="2">
                <input class="button" type="submit" value="<fmt:message key="done"/>"/> 
            </tr>
            </tbody>
        </table>
    </form>
	</div>
	</div>
</fmt:bundle>