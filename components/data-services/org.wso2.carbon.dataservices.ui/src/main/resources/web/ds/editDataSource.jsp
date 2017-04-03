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
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Config" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Data" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Property" %>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<!-- 
<carbon:breadcrumb
		label="Add Datasource"
		resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />
		-->
<%
    //retrieve value from serviceDetails.jsp
    String name = request.getParameter("name");
    String description = request.getParameter("description");
	String serviceName = request.getParameter("serviceName");
    String configId = request.getParameter("configId");
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
<%
    //ArrayList<Config> configs = dataService.getConfigs();
    Config dsConfig = dataService.getConfig(configId);
    ArrayList configProperties = dsConfig.getProperties();
    Iterator iterator = configProperties.iterator();
    String propertyName = null;
    String propertyValue = null;
    String dsType = dsConfig.getDataSourceType();
    String dsName = dsConfig.getId();

%>
<div id="middle">
    <h2><fmt:message key="datasource.edit.datasource"/></h2>
    <div id="workArea">
    <form method="post" action="queryProcessor.jsp" name="dataForm"
          onsubmit="return validateAddQueryForm();">

      <table>
          <tr><td><fmt:message key="dataservices.data.source.type"/></td>
              <td><input type="text" name="dsType" value="<%=dsType%>"/> </td>
          </tr>
          <tr>
              <td><fmt:message key="data.source.id"/></td>
              <td><input name="dsName" value="<%=dsName%>" type="text" /> </td>
          </tr>
          <tr>
              <%
                   while(iterator.hasNext()){
                    Property property = (Property)iterator.next();
                    propertyName = property.getName().toString();
                    propertyValue = property.getValue();
              %>
              <td><%=propertyName%></td>
              <td><input type="text" id="<%=propertyValue%>" name="<%=propertyValue%>" value="<%=propertyValue%>" /></td>
          </tr>
          <%
              }
          %>
          <tr>
              <td>
                  <input type="submit" value="OK">
              </td>
          </tr>
      </table>
   </form>
  </div>
 </div>
</fmt:bundle>
    

