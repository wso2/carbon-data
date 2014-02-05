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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.dataservices.ui.DataServiceAdminClient"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.AxisFault"%>
<%@ page import="org.wso2.carbon.CarbonError" %>
<%@ page import="java.io.PrintWriter" %>
<%
	String userName = request.getParameter("userName");
	String password = request.getParameter("password");
	String documentURL = request.getParameter("documentURL");
   	String visibility = request.getParameter("visibility");
    String passwordAlias = request.getParameter("passwordAlias");
    
	String backendServerURL = CarbonUIUtil
			.getServerURL(config.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
			.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	DataServiceAdminClient client = new DataServiceAdminClient(cookie, backendServerURL,
			configContext);
	String message = "";
	try {
		message = client.testGSpreadConnection(userName,password,visibility,documentURL, passwordAlias) ;
		response.setContentType("text/xml; charset=UTF-8");
		// Set standard HTTP/1.1 no-cache headers.
		response.setHeader("Cache-Control",
		"no-store, max-age=0, no-cache, must-revalidate");
		// Set IE extended HTTP/1.1 no-cache headers.
		response.addHeader("Cache-Control", "post-check=0, pre-check=0");
		// Set standard HTTP/1.0 no-cache header.
		response.setHeader("Pragma", "no-cache");

		PrintWriter pw = response.getWriter();
		pw.write(message);
		pw.flush();
	} catch (AxisFault e) {
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
        	        	%>