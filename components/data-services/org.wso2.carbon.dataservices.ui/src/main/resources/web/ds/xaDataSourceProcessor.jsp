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
<%@page import="org.wso2.carbon.dataservices.common.RDBMSUtils"%>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Config" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Property" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.XADataSource" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.dataservices.common.DBConstants" %>
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>
<jsp:useBean id="newConfig" class="org.wso2.carbon.dataservices.ui.beans.Config" scope="session"/>
<%!private void updateConfiguration(XADataSource xaDatasource, String propertyName, String value) {
		if (value != null && value.trim().length() != 0) {
			xaDatasource.updateProperty(propertyName, value);
		} 
	}%>
<%
	String xaDatasourceId = request.getParameter("xaId");
	String propertyName = request.getParameter("txPropertyName");
	String propertyValue = request.getParameter("txPropertyValue");
	String action = request.getParameter("action");
	String flag = request.getParameter("flag");
	action = (action == null) ? "" : action;
	if ("cancel".equals(flag)) {
		action = "cancel";
	}
	
	XADataSource xaDatasource = dataService.getXADataSource(xaDatasourceId);
	if (action.equals("add")) {
		ArrayList<Property> props = xaDatasource.getProperties();
		for (int i = 0; i < props.size(); i++) {
			String propValue = request.getParameter(props.get(i).getName());
			updateConfiguration(xaDatasource, props.get(i).getName(), propValue);
		}
	} else if (action.equals("addProperty")) {
		ArrayList<Property> props = xaDatasource.getProperties();
		for (int i = 0; i < props.size(); i++) {
			String propValue = request.getParameter(props.get(i).getName());
			updateConfiguration(xaDatasource, props.get(i).getName(), propValue);
		}
		Property property = new Property();
		property.setName(propertyName);
		property.setValue(propertyValue);
		xaDatasource.addProperty(property);
	} else if (action.equals("delete")) {
		dataService.removeXADataSource(xaDatasource);
	} else if (action.equals("cancel")) {
		ArrayList<Property> props = xaDatasource.getProperties();
		for (int i = 0; i < props.size(); i++) {
			String propValue = request.getParameter(props.get(i).getName());
			updateConfiguration(xaDatasource, props.get(i).getName(), propValue);
		}
	} else if (action.equals("deleteAddProp") && xaDatasource != null) {
		xaDatasource.removeProperty(propertyName);
	}
%>
<table>
    <input type="hidden" id="xaDatasourceId" value="<%=xaDatasourceId%>"/>
    <input type="hidden" id="action" value="<%=action%>"/>
</table>

<script type="text/javascript">
    function forward() {
    	var action = document.getElementById('action').value;
    	if (action == 'add' || action == 'delete') {
    		location.href = "viewXADS.jsp";
    	} else if (action == 'addProperty' || action == 'cancel' || action == 'deleteAddProp') {
    		var configId =  document.getElementById('xaDatasourceId').value;
    		location.href = 'manageXADS.jsp?xaId='+configId;
    	}
    }
</script>

<script type="text/javascript">
    forward();
</script>
