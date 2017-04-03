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


<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Property" %>
<%@ page import="java.util.ArrayList" %>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="js/ui-validations.js"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"></jsp:useBean>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.XADataSource" %>
<%
   
	String xaDatasourceId = request.getParameter("xaId");

%>

<div id="middle">
<h2><fmt:message key="add.xa.datasource.property"/></h2>
	<div id="workArea">
	<form method="post" action="" name="dataForm">      
	 <table class="styledLeft" id="addpropertytable"> 
	   <tr>
    	  <td colspan="2" class="middle-header"><fmt:message key="xa.add.property"/></td>
        </tr>
     <table class="normal">
     <%
     XADataSource xaDatasource = dataService.getXADataSource(xaDatasourceId);
     ArrayList<Property> props = xaDatasource.getProperties();
 	for (int i=0; i<props.size(); i++) {
 		String propName = props.get(i).getName();
 		String propValue = request.getParameter(propName);
 		%>
      <input type="hidden" id="<%=propName%>" name="<%=propName%>" value="<%=propValue%>" />
 	 <%}
	%>
       <input type="hidden" id="xaId" name="xaId" value="<%=xaDatasourceId%>" />
	  <tr>
                <td class="leftCol-med"><fmt:message key="xa.configuration.property.name"/><font
                        color='red'>*</font></td>
                <td><input value="" id="txPropertyName" name="txPropertyName"
                           size="30" type="text"></td>
            </tr>
         <tr>
                <td class="leftCol-med"><fmt:message key="xa.configuration.property.value"/></td>
                <td><input value="" id="txPropertyValue" name="txPropertyValue"
                           size="30" type="text"></td>
            </tr>
        </table>
        <table class="normal">
        <tr>
			<td class="buttonRow">
			<input class="button" type="submit"  onclick="document.dataForm.action='xaDataSourceProcessor.jsp?action=addProperty'"  value="<fmt:message key="save"/>" />
			<input class="button" type="submit" value="<fmt:message key="cancel"/>"	onclick="document.dataForm.action='xaDataSourceProcessor.jsp?flag=cancel'" />
			</td>
		</tr>
	 </table>
	 </table>
	</form>
	</div>
	</div>
</fmt:bundle>