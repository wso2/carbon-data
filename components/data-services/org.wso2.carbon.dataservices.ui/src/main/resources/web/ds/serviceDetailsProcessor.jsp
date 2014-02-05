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

<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<carbon:breadcrumb
		label="Data Sources"
		resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"></jsp:useBean>
<script type="text/javascript" src="js/ui-validations.js"></script>

    <%
 //retrieve value from serviceDetails.jsp
    String description = request.getParameter("description");
	String serviceName = request.getParameter("serviceName");
    String batchRequest = request.getParameter("enableBatchReq");
    String isUseAppServerTS = request.getParameter("useAppServerTS");
    String boxcarring = request.getParameter("enableBoxcarring");
    String protectedTokens = request.getParameter("protectedTokens");
    String passwordProvider = request.getParameter("passwordProvider");
    String serviceNamespace = request.getParameter("serviceNamespace");
    String txManagerClass = request.getParameter("txManagerClass");
    String txManagerJNDIName = request.getParameter("txManagerJNDIName");
    String enableDT = request.getParameter("enableDT");
    String flag = request.getParameter("flag");
    String enableStreaming = request.getParameter("enableStreaming");
    String txManagerCleanupMethod = request.getParameter("txManagerCleanupMethod");
    boolean finishEnable = false;
    String forwardTo;
    try {
        if (serviceName != null && serviceName.trim().length() > 0) {
            dataService.setName(serviceName);
        }else{
            serviceName = dataService.getName();
        }
        if (batchRequest != null && batchRequest.trim().length() > 0) {
            dataService.setBatchRequest(true);
        } else {
            dataService.setBatchRequest(false);
        }
        if (enableDT != null && enableDT.trim().length() > 0) {
            //dataService.se
            dataService.setDTP(true);
        } else {
            dataService.setDTP(false);
        }
        if (isUseAppServerTS != null && isUseAppServerTS.trim().length() > 0) {
            dataService.setIsUseAppServerTS(Boolean.parseBoolean(isUseAppServerTS));
        }
        if (description != null && description.trim().length() > 0) {
            dataService.setDescription(description);
        }
        if (enableStreaming != null && enableStreaming.trim().length() > 0) {
        	dataService.setDisableStreaming(false);
        } else {
        	dataService.setDisableStreaming(true);
        }
        if (boxcarring != null && boxcarring.trim().length() > 0) {
            dataService.setBoxcarring(true);
        } else {
            dataService.setBoxcarring(false);
        }

        if (serviceNamespace != null) {
            dataService.setServiceNamespace(serviceNamespace);
        }
        if (txManagerClass != null) {
            dataService.setTxManagerClass(txManagerClass);
        }
        if (txManagerJNDIName != null) {
            dataService.setTxManagerName(txManagerJNDIName);
        }
        if (protectedTokens != null) {
            dataService.setProtectedTokens(protectedTokens);
        }
        if (passwordProvider != null) {
            dataService.setPasswordProvider(passwordProvider);
        }
        if (txManagerCleanupMethod != null) {
        	dataService.setTxManagerCleanupMethod(txManagerCleanupMethod);
        }
        description = (description == null) ? "" : description;
        txManagerCleanupMethod = (txManagerCleanupMethod == null) ? "" : txManagerCleanupMethod;
        serviceNamespace = (serviceNamespace == null) ? "" : serviceNamespace;
        txManagerClass = (txManagerClass == null) ? "" : txManagerClass;
        txManagerJNDIName = (txManagerJNDIName == null) ? "" : txManagerJNDIName;
        protectedTokens = (protectedTokens == null) ? "" : protectedTokens;
        passwordProvider = (passwordProvider == null) ? "" : passwordProvider;
        forwardTo = "dataSources.jsp?ordinal=1";
        %>
    <script type="text/javascript">
        location.href = "<%=forwardTo%>";
    </script>
    <%
     } catch (Exception e) {
			String errorMsg = e.getLocalizedMessage();
%>
<script type="text/javascript">
	location.href = "dsErrorPage.jsp?errorMsg=<%=errorMsg%>";
</script>
<%
	}
%>

<%
 if (flag != null ) {
    	%>
    	<script type="text/javascript">
    	    location.href = "manageXADS.jsp";
    	</script>
    	<%
    }%>
</fmt:bundle>