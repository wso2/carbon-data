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
<%@ page import="org.wso2.carbon.dataservices.ui.beans.*" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.dataservices.ui.DataServiceAdminClient" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Config" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Property" %>
<%@ page import="org.wso2.carbon.dataservices.common.DBConstants" %>
<%@ page import="org.wso2.carbon.dataservices.common.RDBMSUtils" %>

<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="js/ui-validations.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<!--
<carbon:breadcrumb
        label="Add Data Source"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
-->
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session" />

<div id="middle">

<h2><fmt:message key="manage.xa.data.sources"/></h2>
<%
	String xaDatasourceId = request.getParameter("xaId");
    String action ="add";
    String xaClass = request.getParameter("xaClass");
    String sqlDialect = request.getParameter("txSQLDialect");
    String rdbmsEngineType = request.getParameter("databaseEngine");
    String sql = request.getParameter("txtSQL");
    String edit = request.getParameter("txSQLDialect");
	String flag = request.getParameter("flag");
    String xaDatasourceClass ="";
    XADataSource xaDataSource = null;
    if(xaDatasourceId != null && !xaDatasourceId.equals("")) {
    	xaDataSource = dataService.getXADataSource(xaDatasourceId);
    }
    if (xaDataSource != null && xaClass == null) {
    	xaDatasourceClass = xaDataSource.getClassName();
    } else {
    	xaDatasourceClass = xaClass;
    }
   
    if (xaDatasourceClass != null && rdbmsEngineType == null) {
    	rdbmsEngineType = RDBMSUtils.getRDBMSEngine4XADataSource(xaDatasourceClass);
    }
    sqlDialect = (sqlDialect == null) ? "" : sqlDialect;
    sql = (sql == null) ? "" : sql;
    xaDatasourceClass = (xaDatasourceClass == null) ? "":xaDatasourceClass;
    xaDatasourceId = (xaDatasourceId == null) ? "":xaDatasourceId;
    rdbmsEngineType = (rdbmsEngineType == null) ? "#" :rdbmsEngineType;
%>
<div id="workArea">

<form method="post" action="" name="dataForm"
      onsubmit="return validateManageXADSForm();">
      
<table class="styledLeft">
  <tr>
    	  <td colspan="2" class="middle-header"><fmt:message key="add.edit.data.source"/></td>
  </tr>
  <tr>
    <td>
      <table class="normal">
      <input type="hidden" id="action" name="action" value="<%=action%>" />
      <tr>
          <td><fmt:message key="xa.datasource.id"/><font color="red">*</font></td>
           <td><input value="<%=xaDatasourceId%>" id="xaId"
                           name="xaId" size="30" type="text"></td>
        </tr>
      <tr>
    <td><label><fmt:message key="datasource.database.engine"/><font
            color="red">*</font></label></td>
    <td>
        <select name="databaseEngine" id="databaseEngine"
                    onchange="changeXADataSourceEngine(this,document)"> 
            <%if (("#".equals(rdbmsEngineType)|| rdbmsEngineType.equals(""))) {%>
            <option value="#" selected="selected">--SELECT--</option>
            <%} else {%>
            <option value="#">--SELECT--</option>
            <%}%>

            <%if ("mysql".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="<%=DBConstants.XAJDBCDriverClasses.MYSQL+"#mysql"%>"> MySQL
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.MYSQL+"#mysql"%>">  MySQL
            </option>
            <%}%>

            <%if ("derby".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="<%=DBConstants.XAJDBCDriverClasses.DERBY+"#derby"%>"> Apache Derby
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.DERBY+"#derby"%>"> Apache Derby
            </option>
            <%}%>

            <%if ("mssqlserver".equals(rdbmsEngineType)) {%>
            <option selected="selected"   value="<%=DBConstants.XAJDBCDriverClasses.MSSQL+"#mssqlserver"%>">
                Microsoft SQL Server
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.MSSQL+"#mssqlserver"%>">
                Microsoft SQL Server
            </option>
            <%}%>

            <%if ("oracle".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="<%=DBConstants.XAJDBCDriverClasses.ORACLE+"#oracle"%>">Oracle
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.ORACLE+"#oracle"%>">Oracle
            </option>
            <%}%>

            <%if ("db2".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="<%=DBConstants.XAJDBCDriverClasses.DB2+"#db2"%>">IBM DB2
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.DB2+"#db2"%>">IBM DB2</option>
            <%}%>

            <%if ("hsqldb".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="<%=DBConstants.XAJDBCDriverClasses.HSQLDB+"#hsqldb"%>">HSQLDB
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.HSQLDB+"#hsqldb"%>">HSQLDB</option>
            <%}%>

            <%if ("informix-sqli".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="<%=DBConstants.XAJDBCDriverClasses.INFORMIX+"#informix-sqli"%>"> Informix
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.INFORMIX+"#informix-sqli"%>"> Informix
            </option>
            <%}%>

            <%if ("postgresql".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="<%=DBConstants.XAJDBCDriverClasses.POSTGRESQL+"#postgresql"%>"> PostgreSQL
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.POSTGRESQL+"#postgresql"%>"> PostgreSQL
            </option>
            <%}%>

            <%if ("sybase".equals(rdbmsEngineType)) {%>
            <option selected="selected"  value="<%=DBConstants.XAJDBCDriverClasses.SYBASE+"#sybase"%>">  Sybase ASE
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.SYBASE+"#sybase"%>">   Sybase ASE
            </option>
            <%}%>

            <%if ("h2".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="<%=DBConstants.XAJDBCDriverClasses.H2+"#h2"%>">   H2
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.H2+"#h2"%>">H2</option>
            <%}%>

            <%if ("Generic".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="Generic">Generic</option>
            <%} else {%>
            <option value="Generic#Generic">Generic</option>
            <%}%>
        </select>
    </td>
</tr>
   
        <tr>
          <td><fmt:message key="xa.datasource.class"/><font color="red">*</font></td>
           <td><input value="<%=xaDatasourceClass%>" id="txXADatasourceClass"
                           name="txXADatasourceClass" size="35" type="text"></td>
        </tr>
      </table>
    </td>
  </tr>
  <tr><td>
  <%
 
// entering for the first time add functionality
if ( xaDataSource == null || "changed".equals(flag)) {
	if (xaDataSource != null) {
		dataService.removeXADataSource(xaDataSource);
	}
	xaDataSource = new XADataSource();
	xaDataSource.setClassName(xaDatasourceClass);
	xaDataSource.setId(xaDatasourceId);
	if (rdbmsEngineType.equals("mysql")) {
		xaDataSource.addProperty("URL", "");
		xaDataSource.addProperty("User", "");
		xaDataSource.addProperty("Password", "");
	 } else if (rdbmsEngineType.equals("derby")) {
		xaDataSource.addProperty("CreateDatabase", "");
		xaDataSource.addProperty("DatabaseName", "");
		xaDataSource.addProperty("User", "");
		xaDataSource.addProperty("Password", "");
	 } else if (rdbmsEngineType.equals("mssqlserver")) {
		xaDataSource.addProperty("URL", "");
		xaDataSource.addProperty("User", "");
		xaDataSource.addProperty("Password", "");
    } else if (rdbmsEngineType.equals("oracle")) {
    	xaDataSource.addProperty("URL", "");
		xaDataSource.addProperty("User", "");
		xaDataSource.addProperty("Password", "");
    } else if (rdbmsEngineType.equals("db2")) {
		xaDataSource.addProperty("ServerName", "");
		xaDataSource.addProperty("PortNumber", "");
		xaDataSource.addProperty("DatabaseName", "");
		xaDataSource.addProperty("User", "");
		xaDataSource.addProperty("Password","");
    } else if (rdbmsEngineType.equals("hsqldb")) {
    	xaDataSource.addProperty("URL", "");
		xaDataSource.addProperty("User", "");
		xaDataSource.addProperty("Password", "");
    } else if (rdbmsEngineType.equals("informix-sqli")) {
		
		
    } else if (rdbmsEngineType.equals("postgresql")) {
		xaDataSource.addProperty("ServerName", "");
		xaDataSource.addProperty("PortNumber", "");
		xaDataSource.addProperty("DatabaseName", "");
		xaDataSource.addProperty("User", "");
		xaDataSource.addProperty("Password","");
    } else if (rdbmsEngineType.equals("sybase")) {
		xaDataSource.addProperty("ServerName", "");
		xaDataSource.addProperty("PortNumber", "");
		xaDataSource.addProperty("DatabaseName", "");
		xaDataSource.addProperty("User", "");
		xaDataSource.addProperty("Password","");
    } else if (rdbmsEngineType.equals("h2")) {
    	xaDataSource.addProperty("URL", "");
		xaDataSource.addProperty("User", "");
		xaDataSource.addProperty("Password", "");
    } else if (rdbmsEngineType.equals("Generic")) {
	}
    if ( xaDatasourceId != null && !xaDatasourceId.equals("")) {
    	dataService.addXADataSource(xaDataSource);
    }
   
	//for all the db engines
}
Iterator propertyIterator = null;
ArrayList configProperties = xaDataSource.getProperties();
propertyIterator = configProperties.iterator();

%>
   <table class="normal">
<%
    if (propertyIterator != null) {
        while (propertyIterator.hasNext()) {
            Property property = (Property) propertyIterator.next();
            String propertyName = property.getName().toString();
            String propertyValue = property.getValue().toString();
%>
<tr> 
<td><label><%=propertyName%></label></td>
<td>
 <% if (propertyName.equalsIgnoreCase("Password")) {%> 
<input type="password" size="50" id="<%=propertyName%>" name="<%=propertyName%>" value="<%=propertyValue%>" /> 
<% }else {%> 
<input type="text" size="50" id="<%=propertyName%>" name="<%=propertyName%>" value="<%=propertyValue%>" /> 
</td>
<% }%> 
<td>
 <a class="icon-link" style="background-image:url(../admin/images/delete.gif);" onclick="deleteXADSProperty('<%=propertyName%>','<%=xaDatasourceId%>');" href="#"><fmt:message key="delete"/>
</td>
</tr>
<%
        }
    }
%>

                     
 <tr>
                <td colspan="2">
                    <a class="icon-link" style="background-image:url(../admin/images/add.gif);"
                      a href=" javascript: document.dataForm.action = 'addNewXADataSourceProperty.jsp';document.dataForm.submit();"><fmt:message
                           key="add.new.xa.datasource.properties"/></a>
                </td>
            </tr>
</table>
  </td></tr>
  
 <tr>
			<td class="buttonRow">
			<input class="button" type="submit" onclick="document.dataForm.action='xaDataSourceProcessor.jsp'" value="<fmt:message key="save"/>" />
	
       
			<input class="button" type="button" value="<fmt:message key="cancel"/>"
				onclick="location.href = 'viewXADS.jsp'" />
			</td>
		</tr>
</table>

</form>

</div>

</div>          

</fmt:bundle>