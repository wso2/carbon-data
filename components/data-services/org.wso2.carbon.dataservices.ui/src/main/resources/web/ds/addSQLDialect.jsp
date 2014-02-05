
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
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Query" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="js/ui-validations.js"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>

<carbon:breadcrumb
        label="add.sql.dialect"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session" />

<%  String serviceName = dataService.getName();
    String queryId = request.getParameter("queryId");
%>

<div id="middle">

<h2><fmt:message key="sql.dialect.add.new.dialect"/><%out.write(" (" + serviceName + "/" + queryId + ")");%></h2>

<div id="workArea">
<table class="styledLeft noBorders" cellspacing="0" width="100%">
            <thead>
            <tr>
                <th colspan="2"><fmt:message key="sql.dialect.add.new.dialect"/></th>
            </tr>
            </thead>
<form method="post" action="sqlDialectProcessor.jsp" name="dataForm" id="dataForm"
      onsubmit="return validateSQLDialectForm();">

<%
	String datasource = request.getParameter("datasource");
	String mainSql = request.getParameter("sql");
    String flag = request.getParameter("flag");
    
    String sqlDialect = request.getParameter("txSQLDialect");
    String sql = request.getParameter("txtSQL");
    String edit = request.getParameter("txSQLDialect");
    sqlDialect = (sqlDialect == null) ? "" : sqlDialect;
    sql = (sql == null) ? "" : sql;
    flag = (flag == null) ? "add" : flag;
    mainSql = (mainSql == null) ? "" : mainSql;
    datasource = (datasource == null) ? "add" : datasource;
%>

    <table class="styledLeft">
        <tr>
            <td>
                <table class="normal">
                    <input value="<%=queryId%>" name="queryId" id="queryId" size="30"
                           type="hidden"/>
                    <input type="hidden" id="flag" name="flag" value="<%=flag%>"/>
                    <input type="hidden" id="edit" name="edit" value="<%=edit%>"/>
                    <input type="hidden" id="mainSql" name="mainSql" value="<%=mainSql%>"/>
                    <input type="hidden" id="datasource" name="datasource" value="<%=datasource%>"/>

                    <tr>
                        <td class="leftCol-small"><fmt:message key="sql.dialect.supported.drivers"/><font
                                color='red'>*</font></td>
                        <td colspan="2"><select id="sqlDialectId" name="sqlDialect"
                                                multiple="multiple" size="10"
                                                onclick="setSQLDialectDriverPrefix()">
                            <% if (sqlDialect.matches("(?i).*mysql,.*") || sqlDialect.matches("(?i).*,mysql.*") || sqlDialect.matches("(?i)mysql.*")) { %>
                            <option value="mysql" selected="selected">MySQL</option>
                            <% } else { %>
                            <option value="mysql">MySQL</option>
                            <% } %>
                            <% if (sqlDialect.matches("(?i).*derby,.*") || sqlDialect.matches("(?i).*,derby.*") || sqlDialect.matches("(?i)derby.*")) { %>
                            <option value="derby" selected="selected">Apache Derby</option>
                            <% } else { %>
                            <option value="derby">Apache Derby</option>
                            <% } %>
                            <% if (sqlDialect.matches("(?i).*mssqlserver,.*") || sqlDialect.matches("(?i).*,mssqlserver.*") || sqlDialect.matches("(?i)mssqlserver.*")) { %>
                            <option value="mssqlserver" selected="selected">Microsoft SQL Server
                            </option>
                            <% } else { %>
                            <option value="mssqlserver">Microsoft SQL Server</option>
                            <% } %>
                            <% if (sqlDialect.matches("(?i).*oracle,.*") || sqlDialect.matches("(?i).*,oracle.*") || sqlDialect.matches("(?i)oracle.*")) { %>
                            <option value="oracle" selected="selected">Oracle</option>
                            <% } else { %>
                            <option value="oracle">Oracle</option>
                            <% } %>
                            <% if (sqlDialect.matches("(?i).*db2,.*") || sqlDialect.matches("(?i).*,db2.*") || sqlDialect.matches("(?i)db2.*")) { %>
                            <option value="db2" selected="selected">IBM DB2</option>
                            <% } else { %>
                            <option value="db2">IBM DB2</option>
                            <% } %>
                            <% if (sqlDialect.matches("(?i).*hsqldb,.*") || sqlDialect.matches("(?i).*,hsqldb.*") || sqlDialect.matches("(?i)hsqldb.*")) { %>
                            <option value="hsqldb" selected="selected">HSQLDB</option>
                            <% } else { %>
                            <option value="hsqldb">HSQLDB</option>
                            <% } %>
                            <% if (sqlDialect.matches("(?i).*informix-sqli,.*") || sqlDialect.matches("(?i).*,informix-sqli.*") || sqlDialect.matches("(?i)informix-sqli.*")) { %>
                            <option value="informix-sqli" selected="selected">Informix</option>
                            <% } else { %>
                            <option value="informix-sqli">Informix</option>
                            <% } %>
                            <% if (sqlDialect.matches("(?i).*postgresql,.*") || sqlDialect.matches("(?i).*,postgresql.*") || sqlDialect.matches("(?i)postgresql.*")) { %>
                            <option value="postgresql" selected="selected">PostgreSQL</option>
                            <% } else { %>
                            <option value="postgresql">PostgreSQL</option>
                            <% } %>
                            <% if (sqlDialect.matches("(?i).*sybase,.*") || sqlDialect.matches("(?i).*,sybase.*") || sqlDialect.matches("(?i)sybase.*")) { %>
                            <option value="sybase" selected="selected">Sybase ASE</option>
                            <% } else { %>
                            <option value="sybase">Sybase ASE</option>
                            <% } %>
                            <% if (sqlDialect.matches("(?i).*h2,.*") || sqlDialect.matches("(?i).*,h2.*") || sqlDialect.matches("(?i)h2.*")) { %>
                            <option value="h2" selected="selected">H2</option>
                            <% } else { %>
                            <option value="h2">H2</option>
                            <% } %>
                        </select></td>
                    </tr>

                    <tr>
                        <td></td>
                        <td><input value="<%=sqlDialect%>" id="txSQLDialect" name="txSQLDialect"
                                   size="30" type="text"></td>
                    </tr>
                    <tr>
                        <td align="left"><fmt:message key="datasources.query.sql"/><font
                                color='red'>*</font></td>
                        <td><textarea cols="50" rows="8" id="txtSQL"
                                      name="txtSQL"><%=(sql != null) ? sql.trim() : ""%></textarea></td>
                    </tr>
                </table>
            </td>
        </tr>
  <tr>
    <td class="buttonRow" colspan="2">
              
        <input class="button" type="submit" value="<fmt:message key="save"/>"
               onclick="document.dataForm.action = 'sqlDialectProcessor.jsp';return validateSQLDialectForm();"/>
               
        <input class="button" type="button" value="<fmt:message key="cancel"/>"
               onclick="redirectToMainConfiguration(document.getElementById('queryId').value)"/>
    </td>
  </tr>
</table>
</form>
</table>

</div>
</div>
</fmt:bundle>