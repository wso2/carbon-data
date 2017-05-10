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
<%@ page import="java.io.*,org.wso2.carbon.CarbonError"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.dataservices.ui.DataServiceAdminClient"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.apache.axis2.AxisFault"%>
<%
	String serviceName = request.getParameter("serviceName");
	String dsConfigContent = request.getParameter("dsConfig");
	String saveConfig = request.getParameter("saveConfig");
	String caller = request.getParameter("caller");
	if (saveConfig != null && serviceName != null && dsConfigContent != null) {
		String backendServerURL = 
			CarbonUIUtil.getServerURL(config.getServletContext(),session);
		ConfigurationContext configContext = 
			(ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
		String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
		DataServiceAdminClient client = new DataServiceAdminClient(cookie,
		backendServerURL, configContext);
		try {
			client.saveDataService(serviceName, "", dsConfigContent);
			String returnPath = "../service-mgt/index.jsp?ordinal=1"; //= caller + "?serviceName="+serviceName;
			
			%>
<script type="text/javascript">
    location.href = "<%=returnPath%>";
</script>
			<%
		} catch (AxisFault e) {
			e.printStackTrace();
        	CarbonError carbonError = new CarbonError();        
        	carbonError.addError("Error occurred while saving data service configuration.");         
        	request.setAttribute(CarbonError.ID, carbonError);
			String errorMsg = e.getLocalizedMessage();
%>
<script type="text/javascript">
	location.href = "dsErrorPage.jsp?errorMsg=<%=errorMsg%>";
</script>
<%
		}
	}
%>