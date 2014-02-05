<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@page import="org.wso2.carbon.dataservices.common.DBConstants.DataSourceTypes"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.*" %>
<%@ page import="org.wso2.carbon.dataservices.common.DBConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.dataservices.ui.DataServiceAdminClient" %>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<jsp:include page="../dialog/display_messages.jsp"/>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<%--<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"></jsp:useBean>--%>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>

<carbon:breadcrumb
        label="Add Query"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data"
             scope="session"></jsp:useBean>
<script type="text/javascript" src="js/ui-validations.js"></script>

<script type="text/javascript">
    function setValueConf() {
        var elementId = 'xsltPath';
        $(elementId).value = $(elementId).value.replace("/_system/config", "conf:");
    }
    function setValueGov() {
        var elementId = 'xsltPath';
        $(elementId).value = $(elementId).value.replace("/_system/governance", "gov:");
    }
    function setValue() {
        var elementId = 'rdfXsltPath';
        $(elementId).value = $(elementId).value.replace("/_system/config", "conf:");
    }
</script>
<%
    String queryId = null;
    Query query = null;
    String useConfig = "#";
    String datasourceType = "";
    String customDSType = "";
    String wrapperElementName = "";
    String rdfBaseURI = "";
    String rowName = "";
    String resultNamespace = "";
    String rdfResultNamespace = "";
    String sql = null;
    String expression = null;
    String cql = null;
    String sparql = null;
    String workBookName = null;
    String xsltPath = "";
    String rdfXsltPath = "";
    String queryTimeout = "";
    String fetchDirection = "";
    String forceStoredProc = "";
    String forceJDBCBatchRequests = "";
    String fetchSize = "";
    String maxFieldSize = "";
    String maxRows = "";
    String scraperVaribale = null;
    int workSheetNumber = 1;
    boolean returnGeneratedKeys = false;
    boolean isUseColumnNumbers = true;
    String keyColumns = "";
    boolean isEmptyReturnGeneratedKeys = true;
    boolean headersAvailable = false;
    int startingRow = 1;
    int maxRowCount = -1;
    boolean readOnly = false;
    String outputType = "";
    List<SQLDialect> sqlDialects = null;
    String serviceName = dataService.getName();
    boolean useColumnNumbers = false;
    boolean escapeNonPrintableChar = false;
    boolean isUseColomnNumbers;
//    if (request.getParameter("useColumnNumbers") != null
//            && request.getParameter("useColumnNumbers").trim().length() > 0) {
//        useColumnNumbers = Boolean.parseBoolean(request.getParameter("useColumnNumbers"));
//    } else {
//        useColumnNumbers = false;
//    }
    String enableUseColumnNumbers = request.getParameter("useColumnNumbers");
    String enableEscapeNonPrintableChar = request.getParameter("escapeNonPrintableChar");
    //String enableReturnGeneratedKeys = request.getParameter("returnGeneratedKeys");

    //useColNumbers=Boolean.parseBoolean(request.getParameter("useColumnNumbers"));

    if (enableUseColumnNumbers != null) {
        useColumnNumbers = Boolean.parseBoolean(enableUseColumnNumbers);
    }
    if (enableEscapeNonPrintableChar != null) {
        escapeNonPrintableChar = Boolean.parseBoolean(enableEscapeNonPrintableChar);
    }
//    if(enableReturnGeneratedKeys != null) {
//        returnGeneratedKeys = Boolean.parseBoolean(enableReturnGeneratedKeys);
//    }
    try {
        queryId = request.getParameter("queryId");
        if (queryId != null) {
            readOnly = true;
            query = dataService.getQuery(queryId);
            useConfig = query.getConfigToUse();
            sql = query.getSql();
            expression = query.getExpression();
            cql = query.getSql();
            sparql = query.getSparql();
            scraperVaribale = query.getScraperVariable();
            Config c = dataService.getConfig(useConfig);
            returnGeneratedKeys = query.isReturnGeneratedKeys();
            keyColumns = query.getKeyColumns();
            if (c != null) {
                datasourceType = c.getDataSourceType();
                if (datasourceType != null && c.getPropertyValue(DBConstants.RDBMS.URL) instanceof String 
                		&& ((String)c.getPropertyValue(DBConstants.RDBMS.URL)).trim().length() != 0 ) {
                	if (datasourceType.equals(DataSourceTypes.EXCEL)) {
                		datasourceType = DataSourceTypes.RDBMS;
                	} else if (datasourceType.equals(DataSourceTypes.GDATA_SPREADSHEET)) {
                		datasourceType = DataSourceTypes.RDBMS;
                	}
                } 
                if (datasourceType != null && c.getPropertyValue(DBConstants.Excel.DATASOURCE) instanceof String &&
            			((String)c.getPropertyValue(DBConstants.Excel.DATASOURCE)).trim().length() != 0) {
                	datasourceType = DataSourceTypes.EXCEL;
                } 
            	if (datasourceType != null && c.getPropertyValue(DBConstants.GSpread.DATASOURCE) instanceof String &&
            			((String)c.getPropertyValue(DBConstants.GSpread.DATASOURCE)).trim().length() != 0) {
            		datasourceType = DataSourceTypes.GDATA_SPREADSHEET;
                } 
                if (c.getPropertyValue(DBConstants.CustomDataSource.DATA_SOURCE_QUERY_CLASS) instanceof String) {
            		customDSType = DBConstants.DataSourceTypes.CUSTOM_QUERY;
            	} else if (c.getPropertyValue(DBConstants.CustomDataSource.DATA_SOURCE_TABULAR_CLASS) instanceof String) {
            		customDSType = DBConstants.DataSourceTypes.CUSTOM_TABULAR;
            	}
                if (c.getPropertyValue(DBConstants.CarbonDatasource.NAME) != null &&
                		((String)c.getPropertyValue(DBConstants.CarbonDatasource.NAME)).trim().length() != 0) {
            		String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                    ConfigurationContext configContext =
                            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                    DataServiceAdminClient client = new DataServiceAdminClient(cookie, backendServerURL, configContext);
                    String type = client.getCarbonDataSourceType((String)c.getPropertyValue(DBConstants.CarbonDatasource.NAME));
                    if (type != null && !type.equals(DataSourceTypes.RDBMS)) {
                    	datasourceType = DataSourceTypes.CUSTOM;
                    	if (type.equals("DS_CUSTOM_QUERY")) {
                    		customDSType = DBConstants.DataSourceTypes.CUSTOM_QUERY;
                    	} else if (type.equals("DS_CUSTOM_TABULAR")) {
                    		customDSType = DBConstants.DataSourceTypes.CUSTOM_TABULAR;
                    	}
                    }
            	}
            }
            Result result = query.getResult();
            if (result != null) {
                outputType = result.getOutputType();
                wrapperElementName = result.getResultWrapper();
                rdfBaseURI = result.getRdfBaseURI();
                rowName = result.getRowName();
                resultNamespace = result.getNamespace();
                rdfResultNamespace = result.getNamespace();
                xsltPath = result.getXsltPath();
                rdfXsltPath = result.getXsltPath();
            }
            if (query.getExcel() != null) {
                ExcelQuery excel = query.getExcel();
                workBookName = excel.getWorkBookName();
                startingRow = excel.getStartingRow();
                maxRowCount = excel.getMaxRowCount();
                headersAvailable = excel.hasHeaders();
            } else if (query.getGSpread() != null) {
                GSpreadQuery gspread = query.getGSpread();
                workSheetNumber = gspread.getWorkSheetNumber();
                startingRow = gspread.getStartingRow();
                maxRowCount = gspread.getMaxRowCount();
                headersAvailable = gspread.hasHeaders();
            }
            Iterator<Property> propItr = query.getProperties().iterator();
            Property tmpProp;
            while (propItr.hasNext()) {
                tmpProp = propItr.next();
                if (tmpProp.getName().equals(DBConstants.RDBMS.QUERY_TIMEOUT)) {
                    queryTimeout = tmpProp.getValue().toString();
                } else if (tmpProp.getName().equals(DBConstants.RDBMS.FETCH_DIRECTION)) {
                    fetchDirection = tmpProp.getValue().toString();
                } else if (tmpProp.getName().equals(DBConstants.RDBMS.FORCE_STORED_PROC)) {
                    forceStoredProc = tmpProp.getValue().toString();
                } else if (tmpProp.getName().equals(DBConstants.RDBMS.FORCE_JDBC_BATCH_REQUESTS)) {
                    forceJDBCBatchRequests = tmpProp.getValue().toString();
                } else if (tmpProp.getName().equals(DBConstants.RDBMS.FETCH_SIZE)) {
                    fetchSize = tmpProp.getValue().toString();
                } else if (tmpProp.getName().equals(DBConstants.RDBMS.MAX_FIELD_SIZE)) {
                    maxFieldSize = tmpProp.getValue().toString();
                } else if (tmpProp.getName().equals(DBConstants.RDBMS.MAX_ROWS)) {
                    maxRows = tmpProp.getValue().toString();
                }
            }
            result = query.getResult();
            useColumnNumbers = Boolean.parseBoolean(result.getUseColumnNumbers());
            escapeNonPrintableChar = Boolean.parseBoolean(result.getEscapeNonPrintableChar());
        }
        outputType = (outputType == null) ? "xml" : outputType;
        sql = (sql == null) ? "" : sql;
        cql = (cql == null) ? "" : cql;
        keyColumns = (keyColumns == null) ? "" : keyColumns;
        sparql = (sparql == null) ? "" : sparql;
        scraperVaribale = (scraperVaribale == null) ? "" : scraperVaribale;
        wrapperElementName = (wrapperElementName == null) ? "" : wrapperElementName;
        rdfBaseURI = (rdfBaseURI == null) ? "" : rdfBaseURI;
        rowName = (rowName == null) ? "" : rowName;
        resultNamespace = (resultNamespace == null) ? "" : resultNamespace;
        rdfResultNamespace = (rdfResultNamespace == null) ? "" : rdfResultNamespace;
        xsltPath = (xsltPath == null) ? "" : xsltPath;
        rdfXsltPath = (rdfXsltPath == null) ? "" : rdfXsltPath;
        workBookName = (workBookName == null) ? "" : workBookName;
    } catch (Exception e) {
%>

<%
    }
%>
<div id="middle">
<h2><%if (readOnly) {%>
    <fmt:message key="edit.query"/>
    <%
        out.write(" (" + serviceName + "/" + queryId + ")");
    %>
    <%} else {%>
    <fmt:message key="add.new.query"/>
    <%
        out.write(" (" + serviceName + ")");
    %>
    <%}%></h2>

<div id="workArea">
<form method="post" action="" name="dataForm"
      id="dataForm">

<input type="hidden" id="buttonAction"/>

<%

    List<Config> configs = dataService.getConfigs();
    if (configs != null && configs.size() > 0) {
        //iterate through all the configs & store config's datasource type as
        //a hidden field
        Iterator<Config> itrConfigs = configs.iterator();
        while (itrConfigs.hasNext()) {
            Config c = itrConfigs.next();
            String customDatasourceType = "";
            String dsType = c.getDataSourceType();
            if (c != null) {
            	if (c.getPropertyValue(DBConstants.CustomDataSource.DATA_SOURCE_QUERY_CLASS) instanceof String) {
            		customDatasourceType = DBConstants.DataSourceTypes.CUSTOM_QUERY;
            	}
            	if ( c.getPropertyValue(DBConstants.CarbonDatasource.NAME) !=null
            			&& ((String)c.getPropertyValue(DBConstants.CarbonDatasource.NAME)).trim().length() != 0) {
            		String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                    ConfigurationContext configContext =
                            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                    DataServiceAdminClient client = new DataServiceAdminClient(cookie, backendServerURL, configContext);
                    String type = client.getCarbonDataSourceType((String)c.getPropertyValue(DBConstants.CarbonDatasource.NAME));
                    if (!type.equals(DataSourceTypes.RDBMS)) {
                    	dsType = DataSourceTypes.CUSTOM;
                    	customDatasourceType = type;
                    }
            	}
            	if (dsType != null && c.getPropertyValue(DBConstants.RDBMS.URL) instanceof String &&
            			((String)c.getPropertyValue(DBConstants.RDBMS.URL)).trim().length() != 0) {
                	if (dsType.equals(DataSourceTypes.EXCEL)) {
                		dsType = DataSourceTypes.RDBMS;
                	} else if (dsType.equals(DataSourceTypes.GDATA_SPREADSHEET)) {
                		dsType = DataSourceTypes.RDBMS;
                	}
                } 
            	if (dsType != null && c.getPropertyValue(DBConstants.Excel.DATASOURCE) instanceof String &&
            			((String)c.getPropertyValue(DBConstants.Excel.DATASOURCE)).trim().length() != 0) {
                	dsType = DataSourceTypes.EXCEL;
                } 
            	if (dsType != null && c.getPropertyValue(DBConstants.GSpread.DATASOURCE) instanceof String &&
            			((String)c.getPropertyValue(DBConstants.GSpread.DATASOURCE)).trim().length() != 0) {
                	dsType = DataSourceTypes.GDATA_SPREADSHEET;
                } 
%>
<input type="hidden" id="<%=c.getId()%>" value="<%=dsType%>"/>
<input type="hidden" id="customDatasourceType<%=c.getId()%>" value="<%=customDatasourceType%>"/>
<%
            }
        }
    }
%>
<%
    if (query != null) {
        sqlDialects = query.getSqlDialects();
    }
%>
<table class="styledLeft noBorders" id="addQuery" cellspacing="0" width="100%">
<thead>
<tr>
    <th colspan="2"><fmt:message key="query.details"/></th>
</tr>
</thead>
<tr>
    <td colspan="2">
        <table>
            <tr>
                <td><fmt:message key="query.id"/><font color="red">*</font></td>
                <td>
                    <%if (readOnly) {%>
                    <input type="text" name="queryId" id="queryId" readonly="readonly"
                           value="<%=(queryId != null)?queryId:""%>"/>
                    <%} else {%>
                    <input type="text" name="queryId" id="queryId"
                           value="<%=(queryId != null)?queryId:""%>"/>
                    <%}%>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="query.datasource"/><font color="red">*</font></td>
                <td><select id="datasource" name="datasource"
                            onchange="showTables(this,document);return false;"/>
                    <option value="#">--SELECT--</option>
                    <%
                        if (configs != null && configs.size() > 0) {
                            Iterator iterator = configs.iterator();
                            Config conf = new Config();
                            while (iterator.hasNext()) {
                                Config dsConfig = (Config) iterator.next();
                                if (dsConfig != null) {

                                    if (dsConfig.getId().equals(useConfig)) {
                    %>
                    <option value="<%=dsConfig.getId()%>" selected="selected">
                        <%=dsConfig.getId()%>
                    </option>
                    <%} else {%>
                    <option value="<%=dsConfig.getId()%>">
                        <%=dsConfig.getId()%>
                    </option>
                    <%
                                    }
                                }
                            }
                        }
                    %>

                </td>
            </tr>
            <tr id="scraperRow"
                style="<%=datasourceType.equals("WEB_CONFIG") ? "" : "display:none"%>">
                <td><fmt:message key="scraper.variable"/><font color="red">*</font></td>
                <td><input type="text" size="30" id="scraperVariable" name="scraperVariable"
                           value="<%=scraperVaribale%>"/></td>
            </tr>
        </table>
    </td>
</tr>

<tr id="RDBMSnJNDIRow" style="<%=(datasourceType.equals("RDBMS") ||
                            datasourceType.equals("JNDI") ||
                            datasourceType.equals("CARBON_DATASOURCE") ||
                            customDSType.equals("CUSTOM_TABULAR"))?"":"display:none"%>">
    <td colspan="2">
        <table>
            <tr>
                <td align="left" class="leftCol-small"><fmt:message key="datasources.query.sql"/><font
                        color='red'>*</font></td>
                <td><textarea cols="50" rows="8" id="sql"
                              name="sql"><%=(query != null) ? sql : ""%></textarea></td>
            </tr>
           
            <tr>
                <td>
                    <a href="javascript: document.dataForm.submit();"
                       onclick="document.dataForm.action='queryProcessor.jsp?flag=sqlDialect';return validateQueryId()"
                       id="newSqlDialect"
                       style="background-image:url(images/add.gif);" class="icon-link"
                       type="submit"><fmt:message key="add.new.sql.dialect"/></a>

                    
                </td>
                <td>
                    <a href="#" id="addAutoInputMappings"
                       onclick="var validated=validateQueryId();if(validated){document.dataForm.action='queryProcessor.jsp?addAutoInputMappings=true&flag=autoInputMappings';document.dataForm.submit();showSQLDialects();}return validated;"
                       class="icon-link"
                       style="background-image: url(images/generate_input_mappings.png);"><fmt:message
                            key="generate.input.mappings"/></a>
                    <a href="#" id="addAutoResponse"
                       onclick="var validated=validateQueryId();if(validated){document.dataForm.action='queryProcessor.jsp?addAutoResponse=true&flag=autoResponse';document.dataForm.submit();showSQLDialects();}return validated;"
                       class="icon-link"
                       style="background-image: url(images/create_svc.gif);"><fmt:message
                            key="generate.response"/></a>
                </td>
            </tr>
        </table>
    </td>
</tr>

<tr id="CustomQueryRow" style="<%=(customDSType.equals("CUSTOM_QUERY"))?"":"display:none"%>">
    <td colspan="2">
        <table>
            <tr>
                <td align="left" class="leftCol-small"><fmt:message key="datasources.query.expression"/><font
                        color='red'>*</font></td>
                <td><textarea cols="50" rows="8" id="expression"
                              name="expression"><%=(query != null) ? expression : ""%></textarea></td>
            </tr>
           
           </table>
    </td>
</tr>

<tr id="CASSANDRARow" style="<%=(datasourceType.equals("Cassandra"))?"":"display:none"%>">
    <td colspan="2">
        <table>
            <tr>
                <td align="left" class="leftCol-small">CQL<font
                        color='red'>*</font></td>
                <td><textarea cols="50" rows="8" id="cql"
                              name="cql"><%=(query != null) ? cql : ""%></textarea></td>
            </tr>
            <tr>
                <td>
                    <a href="#" id="addAutoResponse"
                       onclick="document.dataForm.action='queryProcessor.jsp?addAutoResponse=true&flag=autoResponse';document.dataForm.submit();showSQLDialects"
                       class="icon-link"
                       style="background-image: url(images/create_svc.gif);"><fmt:message
                            key="generate.response"/></a>

                </td>
            </tr>
        </table>
    </td>
</tr>

<tr id="sqlDialectHeader" style="<%=(datasourceType.equals("RDBMS") ||
                            datasourceType.equals("JNDI") || datasourceType.equals("CARBON_DATASOURCE"))?"":"display:none"%>">
    <td colspan="2" class="middle-header"><a href="javascript:showSQLDialects()" class="icon-link"
                                             style="background-image:url(images/plus.gif);"
                                             id="sqlDialectSymbolMax"></a><fmt:message
            key="sql.dialects"/></td>
</tr>

<tr id="SQLDialectTable" style="display:none">
    <td colspan="2">
        <table class="styledInner" cellspacing="0" id="existingSQLDialects">
            <thead>
            <%if (sqlDialects != null && sqlDialects.size() > 0) {%>
            <tr>
                <th><b><fmt:message key="sql.dialects"/></b></th>
                <th><b><fmt:message key="actions1"/></b></th>
            </tr>
            </thead>
            <tbody>

            <% for (SQLDialect sqlDialect : sqlDialects) {
                String dialects = sqlDialect.getDialect();
                String sqlQuery = sqlDialect.getSql();
            %>
            <tr>
                <td><%=dialects%>
                </td>

                <td>
                    <a class="icon-link"
                       style="background-image:url(../admin/images/edit.gif);"
                       href="addSQLDialect.jsp?queryId=<%=queryId%>&txSQLDialect=<%=dialects%>&txtSQL=<%=sqlQuery%>&flag=<%="edit"%>"><fmt:message
                            key="edit"/></a>

                    <a class="icon-link" style="background-image: url(../admin/images/delete.gif);"
                       onclick="deleteSQLDialectAddQuery('<%=queryId%>','<%=dialects%>');"
                       href="#"><fmt:message
                            key="delete"/></a>
                </td>
            </tr>
            </tbody>
            <% }
            } else {%>
            <tr id="noSqlDialects">
                <td colspan="2"><fmt:message key="sql.dialect.no.sql.dialects"/></td>
                </thead>
            </tr>
            <% }
            %>
        </table>
    </td>
</tr>

    <%--<tr id="autoResponseRow" style="<%=(datasourceType.equals("RDBMS") ||datasourceType.equals("JNDI")
                    || datasourceType.equals("CARBON_DATASOURCE"))?"":"display:none"%>">
        <td>
            &lt;%&ndash;<input type="submit" id="addAutoResponse" value="Generate Response"
                   onclick="document.dataForm.action='queryProcessor.jsp?addAutoResponse=true&flag=autoResponse';document.dataForm.submit();">&ndash;%&gt;
            <a href="#" id="addAutoResponse" onclick="document.dataForm.action='queryProcessor.jsp?addAutoResponse=true&flag=autoResponse';document.dataForm.submit();"
               class="icon-link" style="background-image: url(images/create_svc.gif);"><fmt:message key="generate.response"/></

        </td>
    </tr>--%>
<tr id="addQueryProperties" style="<%=(datasourceType.equals("RDBMS") ||
                            datasourceType.equals("JNDI") || datasourceType.equals("CARBON_DATASOURCE"))?"":"display:none"%>">
        <%--<td colspan="2"><h3 class="mediator"><fmt:message key="dataservices.query.properties"/></h3></td>--%>
    <td class="middle-header" colspan="2"><a class="icon-link"
                                             style="background-image:url(images/plus.gif);"
                                             href="javascript:showQueryProperties()" id="propertySymbolMax"></a><fmt:message
            key="dataservices.query.advanced.query.properties"/></td>
</tr>

    <%--<tr id="addQueryProperties" style="<%=(datasourceType.equals("RDBMS") ||
                            datasourceType.equals("JNDI") || datasourceType.equals("CARBON_DATASOURCE"))?"":"display:none"%>">
    <td colspan="2">
        <table width="100%">
           &lt;%&ndash; <tr id="propertySymbolMax">
                &lt;%&ndash;<td colspan="2"><h3 class="mediator"><fmt:message key="dataservices.query.properties"/></h3></td>&ndash;%&gt;
                <td class="middle-header" colspan="2"><fmt:message key="dataservices.query.advanced.query.properties"/> </td>
            </tr>
            <tr>
                <td colspan="2"><a href="#" class="icon-link" style="background-image:url(images/add.gif);" type="button" onclick="showQueryProperties();">
                    <fmt:message key="dataservices.query.properties"/></a></td>
            </tr>&ndash;%&gt;
              &lt;%&ndash;<tr id="propertySymbolMin" style="display:none">
                &lt;%&ndash;<td colspan="2"><h3 class="mediator"><fmt:message key="dataservices.query.properties"/></h3></td>&ndash;%&gt;
                &lt;%&ndash;<td><input class="button" type="button" onclick="showQueryProperties()" value="-"></td>&ndash;%&gt;
            </tr>&ndash;%&gt;
        </table>
    </td>--%>
<tr id="propertyTable" style="display:none">
    <td>
        <table>
            <tr>
                <td><fmt:message key="dataservices.query.timeout"/></td>
                <td>
                    <input type="text" size="30" id="timeout" name="timeout"
                           value="<%=queryTimeout%>"/> <fmt:message key="dataservices.seconds"/>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="dataservices.query.fetch.direction"/></td>
                <td><select id="fetchDirection" name="fetchDirection">
                    <% if (fetchDirection.equals("forward")) { %>
                    <option value=""></option>
                    <option value="forward" selected="true">Forward</option>
                    <option value="reverse">Reverse</option>
                    <% } else if (fetchDirection.equals("reverse")) { %>
                    <option value=""></option>
                    <option value="forward">Forward</option>
                    <option value="reverse" selected="true">Reverse</option>
                    <% } else if (fetchDirection.equals("")) { %>
                    <option value="" selected="true"></option>
                    <option value="forward">Forward</option>
                    <option value="reverse">Reverse</option>
                    <% } %>
                </select>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="dataservices.fetch.size"/></td>
                <td><input type="text" id="fetchSize" name="fetchSize" value="<%=fetchSize%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="dataservices.max.field.size"/></td>
                <td><input type="text" id="maxFieldSize" name="maxFieldSize"
                           value="<%=maxFieldSize%>"/> Bytes
                </td>
            </tr>
            <tr>
                <td><fmt:message key="dataservices.max.rows"/></td>
                <td><input type="text" id="maxRows" name="maxRows" value="<%=maxRows%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="dataservices.query.force.stored.proc"/></td>
                <td><select id="forceStoredProc" name="forceStoredProc">
                    <% if (forceStoredProc.equals("true")) { %>
                    <option value=""></option>
                    <option value="true" selected="true">Yes</option>
                    <option value="false">No</option>
                    <% } else if (forceStoredProc.equals("false")) { %>
                    <option value=""></option>
                    <option value="true">Yes</option>
                    <option value="false" selected="true">No</option>
                    <% } else if (forceStoredProc.equals("")) { %>
                    <option value="" selected="true"></option>
                    <option value="true">Yes</option>
                    <option value="false">No</option>
                    <% } %>
                </select>
                </td>
            </tr>            
            <tr>
                <td><fmt:message key="dataservices.query.force.jdbc.batch.requests"/></td>
                <td><select id="forceJDBCBatchRequests" name="forceJDBCBatchRequests">
                    <% if (forceJDBCBatchRequests.equals("true")) { %>
                    <option value=""></option>
                    <option value="true" selected="true">Yes</option>
                    <option value="false">No</option>
                    <% } else if (forceJDBCBatchRequests.equals("false")) { %>
                    <option value=""></option>
                    <option value="true">Yes</option>
                    <option value="false" selected="true">No</option>
                    <% } else if (forceJDBCBatchRequests.equals("")) { %>
                    <option value="" selected="true"></option>
                    <option value="true">Yes</option>
                    <option value="false">No</option>
                    <% } %>
                </select>
                </td>
            </tr>            
        </table>
    </td>
</tr>

<tr id="RDFRow" style="<%= (datasourceType.equals("RDF") || datasourceType.equals("SPARQL")) ?"":"display:none"%>">
    <td>
        <table>
            <tr>
                <td align="left"><fmt:message key="datasources.query.sparql"/><font
                        color='red'>*</font></td>
                <td><textarea cols="50" rows="8" id="sparql" name="sparql"><%=(sparql != null) ? sparql.trim() : ""%></textarea></td>
            </tr>
        </table>
    </td>
</tr>

<% boolean inputMappingsSupported = (datasourceType.equals("RDBMS") || datasourceType.equals("JNDI") ||
                                     datasourceType.equals("CARBON_DATASOURCE") || datasourceType.equals("RDF") || datasourceType.equals("SPARQL") || datasourceType.equals("Cassandra") || datasourceType.equals("CUSTOM")); %>
<tr style="<%=inputMappingsSupported ? "" : "display:none"%>">
   <td colspan="7" />                                                           
</tr>                                                          

<tr id="inputHeading" style="<%=inputMappingsSupported ? "" : "display:none"%>">
    <td colspan="2" class="middle-header"><fmt:message key="dataservices.input.mapping"/></td>
</tr>

<tr id="InputMappingRow" style="<%=inputMappingsSupported ? "" : "display:none"%>">
    <td>
        <table class="styledInner" cellspacing="0" id="existingInputMappingsTable">
            <%
                if (query != null) {
                    Param[] params = query.getParams();
                    if (params != null && params.length > 0) {
                        String inputParamName = "";
                        String sqlType = "";
                        String paramType = "";
            %>
            <tr>
                <td><b><fmt:message key="datasources.mapping.name"/></b></td>
                <td><b><fmt:message key="dataservices.param.type"/></b></td>
                <td><b><fmt:message key="datasources.type"/></b></td>
                <td><b><fmt:message key="datasources.action"/></b></td>
            </tr>
            <%
                for (int a = 0; a < params.length; a++) {
                    inputParamName = (params[a].getName() == null) ? "" : params[a].getName();
                    sqlType = (params[a].getSqlType() == null) ? "" : params[a].getSqlType();
                    paramType = (params[a].getParamType() == null) ? "" : params[a].getParamType();
            %>
            <tr>
                <input type="hidden" id="<%=inputParamName%>" name="<%=inputParamName%>"
                       value="<%=inputParamName%>"/>
                <input type="hidden" id="<%=sqlType%>" name="<%=sqlType%>" value="<%=sqlType%>"/>
                <input type="hidden" id="<%=paramType%>" name="<%=paramType%>"
                       value="<%=paramType%>"/>
                <td><%=inputParamName%>
                </td>
                <td><%=paramType%>
                </td>
                <td><%=sqlType%>
                </td>
                <%
                    if (datasourceType.equals("RDF") || datasourceType.equals("SPARQL")) {
                %>
                <td><a class="icon-link"
                       style="background-image: url(../admin/images/edit.gif);"
                       href="addSparqlInputMapping.jsp?paramName=<%=inputParamName%>&queryId=<%=queryId%>&paramType=<%=paramType%>"><fmt:message
                        key="edit"/></a> <a class="icon-link"
                                            style="background-image: url(../admin/images/delete.gif);"
                                            href="javascript:deleteInputMappingsFromAddQuery(document.getElementById('<%=inputParamName%>').value,document.getElementById('<%=sqlType%>').value,document.getElementById('queryId').value,'rdf');"><fmt:message
                        key="delete"/></a></td>

                <%
                } else {

                %>
                <td><a class="icon-link"
                       style="background-image: url(../admin/images/edit.gif);"
                       href="addInputMapping.jsp?paramName=<%=inputParamName%>&queryId=<%=queryId%>&paramType=<%=paramType%>"><fmt:message
                        key="edit"/></a> <a class="icon-link"
                                            style="background-image: url(../admin/images/delete.gif);"
                                            onclick="deleteInputMappingsFromAddQuery(document.getElementById('<%=inputParamName%>').value,document.getElementById('<%=sqlType%>').value,document.getElementById('queryId').value,'sql');"
                                            href="#"><fmt:message
                        key="delete"/></a></td>
                <%
                    }

                %>
                <%
                    }
                } else {
                %>
            </tr>
            <tr>
                <td colspan="3"><label><fmt:message
                        key="datasources.no.inputmapping"/></label></td>
            </tr>
            <%
                }
            } else {
                //new query, hence no input mappings
            %>
            <tr>
                <td colspan="3"><label><fmt:message
                        key="datasources.no.inputmapping"/></label></td>
            </tr>
            <%
                }
            %>
        </table>
    </td>
</tr>
                                                           
<tr id="InputMappingButtonRow" style="<%=inputMappingsSupported ? "" : "display:none"%>">
    <td colspan="2">
        <a href="javascript: document.dataForm.submit();"
           onclick="document.dataForm.action='queryProcessor.jsp?flag=inputMapping';return validateQueryId()"
           id="newInputMapping"
           style="background-image:url(images/add.gif);" class="icon-link"
           type="submit"><fmt:message key="add.new.input.mapping"/></a>
            <%--<input type="submit" id="newInputMapping" value="Add New Input Mapping" onclick="document.dataForm.action='queryProcessor.jsp?flag=inputMapping'">--%>
    </td>
</tr>

<tr id="SparqlInputMappingButtonRow" style="<%=( datasourceType.equals("RDF") || datasourceType.equals("SPARQL") 
                                                           )?"":"display:none"%>">
    <td>
        <input type="submit" id="newSparqlInputMapping" value="Add New Input Mapping"
               onclick="document.dataForm.action='queryProcessor.jsp?flag=sparqlInputMapping'" /></td>
</tr>

<tr id="ExcelRow" style="<%=datasourceType.equals("EXCEL") ? "":"display:none"%>">
    <td>
        <table class="normal">
            <tr>
                <td colspan="2"><h3 class="mediator"><b><fmt:message
                        key="dataservices.header.for.excel"/></b></h3></td>
            </tr>
            <tr>
                <td><label><fmt:message key="dataservicesworkbook.name"/><font color="red">*</font></label>
                </td>
                <td><input value="<%=workBookName%>" id="txtExcelWorkbookName"
                           name="txtExcelWorkbookName" size="30" type="text" /></td>
            </tr>
            <tr>
                <td><label><fmt:message key="dataservices.start.reading.from"/><font
                        color="red">*</font></label></td>
                <td><input value="<%=startingRow%>" id="txtExcelStartingRow"
                           name="txtExcelStartingRow" size="30" type="text" /></td>
            </tr>
            <tr>
                <td><label><fmt:message key="dataservices.rows.to.read"/><font color="red">*</font></label>
                </td>
                <td><input value="<%=maxRowCount%>" id="txtExcelMaxRowCount"
                           name="txtExcelMaxRowCount" size="30" type="text" /></td>
            </tr>
            <tr>
                <td><label><fmt:message key="dataservices.headers.available"/><font
                        color="red">*</font></label></td>
                <td><select id="txtExcelHeaderColumns" name="txtExcelHeaderColumns">
                    <% if (headersAvailable) { %>
                    <option value="true" selected="selected">true</option>
                    <option value="false">false</option>
                    <% } else { %>
                    <option value="true">true</option>
                    <option value="false" selected="selected">false</option>
                    <% } %>
                </select></td>
            </tr>
        </table>
    </td>
</tr>

<tr id="GSpreadRow" style="<%=datasourceType.equals("GDATA_SPREADSHEET") ? "":"display:none"%>">
    <td>
        <table class="normal">
            <tr>
                <td colspan="2"><b><fmt:message key="dataservices.header.for.gspread"/></b></td>
            </tr>
            <tr>
                <td><label><fmt:message key="dataservices.gspread.worksheet.number"/><font
                        color="red">*</font></label></td>
                <td><input value="<%=workSheetNumber%>" id="txtGSpreadWorksheetNumber"
                           name="txtGSpreadWorksheetNumber" size="30" type="text" /></td>
            </tr>
            <tr>
                <td><label><fmt:message key="dataservices.start.reading.from"/><font
                        color="red">*</font></label></td>
                <td><input value="<%=startingRow%>" id="txtGSpreadStartingRow"
                           name="txtGSpreadStartingRow" size="30" type="text" /></td>
            </tr>
            <tr>
                <td><label><fmt:message key="dataservices.rows.to.read"/><font color="red">*</font></label>
                </td>
                <td><input value="<%=maxRowCount%>" id="txtGSpreadMaxRowCount"
                           name="txtGSpreadMaxRowCount" size="30" type="text" /></td>
            </tr>
            <tr>
                <td><label><fmt:message key="dataservices.headers.available"/><font
                        color="red">*</font></label></td>
                <td><select id="txtGSpreadHeaderColumns" name="txtGSpreadHeaderColumns">
                    <% if (headersAvailable) { %>
                    <option value="true" selected="selected">true</option>
                    <option value="false">false</option>
                    <% } else { %>
                    <option value="true">true</option>
                    <option value="false" selected="selected">false</option>
                    <% } %>
                </select></td>
            </tr>
        </table>
    </td>
</tr>

<tr>

    <td>
        <table class="normal">

            <tr>
                <%--<td><label><fmt:message key="datasources.return.generated.keys"/></label></td>--%>
                     <%--<td><select id="returnGeneratedKeys" name="returnGeneratedKeys" onchange="document.dataForm.action='queryProcessor.jsp?setReturnGeneratedKeys=true&flag=ReturnRowChanged';document.dataForm.submit();">--%>
				        <%--<% if(returnGeneratedKeys){ %>--%>
				        <%--<option value="true" selected="selected">Yes</option>--%>
				        <%--<% }else{ %>--%>
				        <%--<option value="true">Yes</option>--%>
				        <%--<% } %>--%>
				        <%--<% if(!returnGeneratedKeys) { %>--%>
				        <%--<option value="false" selected="selected">No</option>--%>
				        <%--<% } else { %>--%>
				        <%--<option value="false">No</option>--%>
				        <%--<% } %>--%>
				        <%--</select> </td>--%>
                <%
                    if (!(datasourceType.equals("GDATA_SPREADSHEET") || datasourceType.equals("EXCEL"))) {
                %>
                <td>
                    <%
                        if (returnGeneratedKeys) {
                    %>
                    <input type="checkbox" id="returnGeneratedKeys" name="returnGeneratedKeys"
                           checked="checked" value="<%=returnGeneratedKeys%>" onclick="var validated=validateClickOnReturnGeneratedKeys();if(validated){document.dataForm.action='queryProcessor.jsp?setReturnGeneratedKeys=false&flag=ReturnRowChanged';document.dataForm.submit();} return validated;"  />
                    <% } else {
                    %>
                    <input type="checkbox" id="returnGeneratedKeys" name="returnGeneratedKeys" value="<%=returnGeneratedKeys%>"
                           onclick="var validated=validateClickOnReturnGeneratedKeys();if(validated){document.dataForm.action='queryProcessor.jsp?setReturnGeneratedKeys=true&flag=ReturnRowChanged';document.dataForm.submit();} return validated;" />
                    <% } %>
                    <label for="returnGeneratedKeys"><fmt:message
                        key="datasources.return.generated.keys"/></label>                    
                </td>
                    
            </tr>
            <% if (returnGeneratedKeys) { %>
            <tr>
                <td><label><fmt:message key="datasources.key.columns"/></label></td>
                <td><input type="text" name="keyColumns" id="keyColumns" value="<%=keyColumns%>"/>
                </td>
            </tr>
            <% } %>
            <% } %>
        </table>
    </td>
</tr>

<tr>
    <td class="middle-header"><fmt:message key="datasources.result.output.mapping"/></td>
</tr>

<tr>
    <td>
        <table class="normal">
            <tr>
                <td><label><fmt:message key="dataservice.output.type"/></label></td>
                <td><select id="outputTypeId" name="outputType"
                            onchange="outputTypeVisibilityOnChange(this,document)">
                    <% if (outputType.equals("") || outputType.equals("xml")) { %>
                    <option value="xml" selected="selected">XML</option>
                    <% } else { %>
                    <option value="xml">XML</option>
                    <% } %>
                    <% if (outputType.equals("rdf")) { %>
                    <option value="rdf" selected="selected">RDF</option>
                    <% } else { %>
                    <option value="rdf">RDF</option>
                    <% } %>
                </select></td>
            </tr>

            <tr>
                <td>
                    <%--<input type="checkbox" id="useColumnNumbers"--%>
                           <%--name="useColumnNumbers"  <%=(useColumnNumbers) ? "checked=\"checked\"" : ""%>--%>
                           <%--value=<%=useColumnNumbers%>/>--%>

                      <input type="checkbox" id = "useColumnNumbers"  name="useColumnNumbers"  <%=(useColumnNumbers) ? "checked=\"checked\"" : ""%> value="<%=useColumnNumbers%>" />

                    <label for="useColumnNumbers"><fmt:message
                            key="datasources.use.column.numbers"/></label>
                </td>
                    <%--<td colspan="2">--%>
                    <%--<input type="checkbox" tabindex="3" id="useColumnNumbers" name="useColumnNumbers">--%>
                    <%--<input type="checkbox" tabindex="3" id="useColumnNumbers"  name="useColumnNumbers"  <%=(useColumnNumbers) ? "checked=\"checked\"" : ""%>  value=<%=useColumnNumbers%>  ></td>--%>
                    <%--<td  align="left"><label for="useColumnNumbers"><fmt:message key="datasources.use.column.numbers"/></label></td>--%>

            </tr>
            <tr>
                <td>
                    <input type="checkbox" id = "escapeNonPrintableChar"  name="escapeNonPrintableChar"  <%=(escapeNonPrintableChar) ? "checked=\"checked\"" : ""%> value="<%=escapeNonPrintableChar%>" />
                    <label for="escapeNonPrintableChar"><fmt:message
                                                    key="datasources.escape.non.printable.char"/></label>
                </td>
            </tr>

        </table>
    </td>
</tr>

<tr style="<%=outputType.equals("xml") || outputType.equals("") ? "" : "display:none"%>"
    id="xmlResultTypeRow">

    <td>
        <table class="normal">

            <tr>
                <td><label><fmt:message key="datasources.grouped.by.element"/></label></td>
                <td>
                    <input value="<%=wrapperElementName%>" id="txtDataServiceWrapElement"
                           name="txtDataServiceWrapElement" size="30" type="text" /></td>
            </tr>

            <tr>
                <td><label><fmt:message key="datasources.row.name"/></label></td>
                <td><input value="<%=rowName%>" id="txtDataServiceRowName"
                           name="txtDataServiceRowName" size="30" type="text" /></td>
            </tr>
            <tr>
                <td><label><fmt:message key="datasources.row.namespace"/></label></td>
                <td><input value="<%=resultNamespace%>" id="txtDataServiceRowNamespace"
                           name="txtDataServiceRowNamespace" size="30" type="text" /></td>
            </tr>
            <tr>
                <td><label><fmt:message key="data.services.xslt.path"/></label></td>
                <td><input type="text" size="30" id="xsltPath" name="xsltPath"
                           value="<%=xsltPath%>"/></td>
                <td><a onclick="showResourceTree('xsltPath', setValueConf , '/_system/config')"
                       style="background-image:url(images/registry_picker.gif);" class="icon-link"
                       href="#"> Configuration Registry </a></td>
                <td><a onclick="showResourceTree('xsltPath', setValueGov , '/_system/governance')"
                       style="background-image:url(images/registry_picker.gif);" class="icon-link"
                       href="#"> Govenance Registry </a></td>

            </tr>

        </table>
    </td>
</tr>

<tr style="<%=outputType.equals("rdf")  ? "" : "display:none"%>" id="rdfResultTypeRow">
    <td>
        <table class="normal">
            <tr>
                <td><label><fmt:message key="datasources.rdf.base.uri"/></label></td>
                <td>
                    <input value="<%=rdfBaseURI%>" id="txtrdfBaseURI"
                           name="txtrdfBaseURI" size="30" type="text" /></td>
            </tr>
            <tr>
                <td><label><fmt:message key="datasources.row.namespace"/></label></td>
                <td><input value="<%=rdfResultNamespace%>" id="txtDataServiceRDFRowNamespace"
                           name="txtDataServiceRDFRowNamespace" size="30" type="text" /></td>
            </tr>
            <tr>
                <td><label><fmt:message key="data.services.xslt.path"/></label></td>
                <td><input type="text" size="30" id="rdfXsltPath" name="rdfXsltPath"
                           value="<%=rdfXsltPath%>" readonly="readonly"/></td>
                <%--<td><a onclick="showResourceTree('rdfXsltPath', setValue , '/_system/config')"
                       style="background-image:url(images/registry_picker.gif);" class="icon-link"
                       href="#">Registry path</a></td>--%>
                <td><a onclick="showResourceTree('rdfXsltPath', setValueConf , '/_system/config')"
                       style="background-image:url(images/registry_picker.gif);" class="icon-link"
                       href="#"> Configuration Registry </a></td>
                <td><a onclick="showResourceTree('rdfXsltPath', setValueGov , '/_system/governance')"
                       style="background-image:url(images/registry_picker.gif);" class="icon-link"
                       href="#"> Governance Registry </a></td>
            </tr>
        </table>
    </td>
</tr>


<tr>
<td>
<table class="styledLeft" cellspacing="0" id="existingOutputMappingsTable" width="100%">
<% if (query != null) {
    Result result = query.getResult();
    if (result != null) {
        List<Element> elements = result.getElements();
        List<RDFResource> resources = result.getResources();
        List<Attribute> attributes = result.getAttributes();
        List<CallQuery> callQueries = result.getCallQueries();
        List<ComplexElement> complexElements = result.getComplexElements();
        Iterator itrElements = elements.iterator();
        Iterator itrResources = resources.iterator();
        useColumnNumbers = Boolean.parseBoolean(result.getUseColumnNumbers());
        if (itrElements.hasNext()) {
%>
<thead>
<tr>
    <th><b><fmt:message key="dataservices.element.name"/></b></th>
    <th><b><fmt:message key="data.services.datasource.type"/></b></th>
    <% if (useColumnNumbers) { %>
    <th><b><fmt:message key="dataservice.datasource.column.number"/></b></th >
    <% } else { %>
    <th><b><fmt:message key="dataservice.datasource.column.name"/></b></th >
    <% }%>
    <th><b><fmt:message key="data.services.mapping.type"/></b></th>
    <th><b><fmt:message key="data.services.user.roles"/></b></th>
    <th><b><fmt:message key="data.services.xsdType"/></b></th>
    <th><b><fmt:message key="actions1"/></b></th>
</tr>
</thead>
    <%--<tr>--%>
    <%--<td><b><fmt:message key="dataservices.element.name"/></b></td>--%>
    <%--<td><b><fmt:message key="data.services.datasource.type"/></b></td>--%>
    <%--<td><b><fmt:message key="dataservice.datasource.column.name"/></b></td>--%>
    <%--<td><b><fmt:message key="data.services.mapping.type"/></b></td>--%>
    <%--<td><b><fmt:message key="data.services.user.roles"/></b></td>--%>
    <%--<td><b><fmt:message key="data.services.xsdType"/></b></td>--%>
    <%--&lt;%&ndash;<td><b><fmt:message key="dataservices.output.mapping.export.nexistingOutputMappingsTableame"/></b></td>--%>
    <%--<td><b><fmt:message key="dataservices.output.mapping.export.type"/></b></td>&ndash;%&gt;--%>
    <%--<td><b><fmt:message key="actions1"/></b></td>--%>
    <%--</tr>--%>

<% } %>
  
<tbody>
  
<%

    if (elements != null) {
        while (itrElements.hasNext()) {
            Element element = (Element) itrElements.next();
            if (element.getDataSourceValue() != null && (element.getDataSourceValue().equals("ROW_ID"))) {
                isEmptyReturnGeneratedKeys = false;
            }
            String roles = "";
            String xType = "";
            String xportName = "";
            String xportType = "";
            String namespace = "";
            String arrayName = "";
            String optional = "";

            if (element.getRequiredRoles() != null && element.getRequiredRoles().trim().length() > 0) {
                roles = element.getRequiredRoles();
            } else {
                roles = "N/A";
            }
            if (element.getExport() != null) {
                xportName = element.getExport();
            } else {
                xportName = "";
            }
            if (element.getExportType() != null && !xportName.equals("")) {
                xportType = element.getExportType();
            } else {
                xportType = "";
            }
            if (element.getXsdType() != null) {
                xType = element.getXsdType();
            }
            if (element.getNamespace() != null) {
                namespace = element.getNamespace();
            }
            if (element.getArrayName() != null) {
                arrayName = element.getArrayName();
            }
            if (element.getOptional() != null) {
                optional = element.getOptional();
            }
%>
<tr>
    <input type="hidden" id="<%=element.getName()%>" name="<%=element.getName()%>"
           value="<%=element.getName()%>"/>
    <td><%=element.getName()%>
    </td>
    <td><%=element.getDataSourceType()%>
    </td>
    <td><%=element.getDataSourceValue()%>
    </td>
    <td>element
    </td>
    <td><%=roles%>
    </td>
    <td><%=xType%>
    </td>
        <%--<td><%=xportName%>--%>
        <%--</td>--%>
        <%--<td><%=xportType%>--%>
        <%--</td>--%>
    <td>
        <%
            if (outputType.equals("rdf") || outputType.equals("RDF")) {

        %>
        <a class="icon-link" style="background-image:url(../admin/images/edit.gif);"
           href="addRDFOutputMapping.jsp?queryId=<%=queryId%>&name=<%=element.getName()%>&datasourceType=<%=element.getDataSourceType()%>&datasourceValue=<%=element.getDataSourceValue()%>&requiredRoles=<%=roles%>&xsdType=<%=xType%>&exportName=<%=xportName%>&exportType=<%=xportType%>&edit=<%=element.getName()%>&mappingType=element&flag=save"><fmt:message
                key="edit"/></a>
        <%
        } else {
        %>
        <a class="icon-link" style="background-image:url(../admin/images/edit.gif);"
           href="addOutputMapping.jsp?queryId=<%=queryId%>&name=<%=element.getName()%>&datasourceType=<%=element.getDataSourceType()%>&datasourceValue=<%=element.getDataSourceValue()%>&txtDataServiceElementNamespace=<%=namespace%>&requiredRoles=<%=roles%>&xsdType=<%=xType%>&exportName=<%=xportName%>&exportType=<%=xportType%>&edit=<%=element.getName()%>&mappingType=element&flag=save&arrayName=<%=arrayName%>&optional=<%=optional%>"><fmt:message
                key="edit"/></a>

        <%
            }
        %>
        <a class="icon-link" style="background-image:url(../admin/images/delete.gif);"
           href="javascript:deleteOutputMappingsFromAddQuery(document.getElementById('queryId').value,document.getElementById('<%=element.getName()%>').value,'element');">
            <fmt:message key="delete"/></a>
    </td>
</tr>

<%
        }
    }

    if (attributes != null) {
        Iterator itrAttributes = attributes.iterator();
        while (itrAttributes.hasNext()) {
            Attribute attribute = (Attribute) itrAttributes.next();
            String roles = "";
            String xType = "";
            String xportName = "";
            String xportType = "";
            String arrayName = "";
            String optional = "";
            if (attribute.getRequiredRoles() != null && 
            		attribute.getRequiredRoles().trim().length() > 0) {
                roles = attribute.getRequiredRoles();
            } else {
                roles = "N/A";
            }
            if (attribute.getExport() != null) {
                xportName = attribute.getExport();
            } else {
                xportName = "";
            }
            if (attribute.getExportType() != null) {
                xportType = attribute.getExportType();
            } else {
                xportType = "";
            }
            if (attribute.getXsdType() != null) {
                xType = attribute.getXsdType();
            }
            if (attribute.getArrayName() != null) {
                arrayName = attribute.getArrayName();
            }
            if (attribute.getOptional() != null) {
                optional = attribute.getOptional();
            }
%>
<tr>
    <input type="hidden" id="<%=attribute.getName()%>" name="<%=attribute.getName()%>"
           value="<%=attribute.getName()%>"/>
    <td><%=attribute.getName()%>
    </td>
    <td><%=attribute.getDataSourceType()%>
    </td>
    <td><%=attribute.getDataSourceValue()%>
    </td>
    <td>attribute</td>
    <td><%=roles%>
    </td>
    <td><%=xType%>
    </td>
        <%--<td><%=xportName%>--%>
        <%--</td>--%>
        <%--<td><%=xportType%>--%>
        <%--</td>--%>
    <td>
        <a class="icon-link" style="background-image:url(../admin/images/edit.gif);"
           href="addOutputMapping.jsp?queryId=<%=queryId%>&name=<%=attribute.getName()%>&datasourceType=<%=attribute.getDataSourceType()%>&datasourceValue=<%=attribute.getDataSourceValue()%>&requiredRoles=<%=roles%>&xsdType=<%=xType%>&exportName=<%=xportName%>&exportType=<%=xportType%>&edit=<%=attribute.getName()%>&mappingType=attribute&flag=save&arrayName=<%=arrayName%>&optional=<%=optional%>"><fmt:message
                key="edit"/></a>
        <a class="icon-link" style="background-image:url(../admin/images/delete.gif);"
           href="#"
           onclick="deleteOutputMappingsFromAddQuery(document.getElementById('queryId').value,document.getElementById('<%=attribute.getName()%>').value,'attribute');"><fmt:message
                key="delete"/></a>
    </td>
</tr>
<%
        }
    }

    if (resources != null) {
        while (itrResources.hasNext()) {
            RDFResource resource = (RDFResource) itrResources.next();
            String roles = "";
            String xType = "";
            if (resource.getRequiredRoles() != null && 
            		resource.getRequiredRoles().trim().length() > 0) {
                roles = resource.getRequiredRoles();
            } else {
                roles = "N/A";
            }
            if (resource.getXsdType() != null) {
                xType = resource.getXsdType();
            }
%>
<tr>
    <input type="hidden" id="<%=resource.getName()%>" name="<%=resource.getName()%>"
           value="<%=resource.getName()%>"/>
    <td><%=resource.getName()%>
    </td>
    <td><%=resource.getRdfRefURI()%>
    </td>
    <td>resource
    </td>
    <td><%=roles%>
    </td>
    <td><%=xType%>
    </td>
    <td>
        <a class="icon-link" style="background-image:url(../admin/images/edit.gif);"
           href="addRDFOutputMapping.jsp?queryId=<%=queryId%>&name=<%=resource.getName()%>&rdfRefURI=<%=resource.getRdfRefURI()%>&requiredRoles=<%=roles%>&xsdType=<%=xType%>&edit=<%=resource.getName()%>&mappingType=resource&flag=save"><fmt:message
                key="edit"/></a>
        <a class="icon-link" style="background-image:url(../admin/images/delete.gif);"
           href="#"
           onclick="deleteOutputMappingsFromAddQuery(document.getElementById('queryId').value,document.getElementById('<%=resource.getName()%>').value,'resource');"><fmt:message
                key="delete"/></a>
    </td>
</tr>
<%
        }
    }
    if (complexElements != null) {
        Iterator itrComplexElements = complexElements.iterator();
        if (itrComplexElements.hasNext()) {
%>
<tr>
    <td colspan="7"></td>
</tr>
<tr>
    <td colspan="3"><b><fmt:message key="complex.element"/></b></td>
    <td colspan="3"><b><fmt:message key="dataservice.element.namespace"/></b></td>
    <td><b><fmt:message key="actions1"/></b></td>
</tr>
<%
    }
    while (itrComplexElements.hasNext()) {
        ComplexElement complexElement = (ComplexElement) itrComplexElements.next();
        String elementNameSpace = "";
        String arrayName = "";
        if (complexElement.getNamespace() != null && complexElement.getNamespace().trim().length() > 0) {
            elementNameSpace = complexElement.getNamespace();
        } else {
            elementNameSpace = "N/A";
        }
        if (complexElement.getArrayName() != null) {
            arrayName = complexElement.getArrayName();
        }
%>
<tr>
    <input type="hidden" id="<%=complexElement.getName()%>" name="<%=complexElement.getName()%>"
           value="<%=complexElement.getName()%>"/>
    <td colspan="3"><%=complexElement.getName()%>
    </td>
    <td colspan="3"><%=elementNameSpace%>
    </td>
    <td>
        <a class="icon-link"
           style="background-image:url(../admin/images/edit.gif);"
           href="addOutputMapping.jsp?queryId=<%=queryId%>&txtDataServiceComplexElementName=<%=complexElement.getName()%>&edit=<%=complexElement.getName()%>&txtDataServiceComplexElementNamespace=<%=elementNameSpace%>&mappingType=complexType&flag=edit&arrayName=<%=arrayName%>"><fmt:message
                key="edit"/></a>
        <a class="icon-link" style="background-image:url(../admin/images/delete.gif);"
           href="#"
           onclick="deleteOutputMappingsFromAddQuery(document.getElementById('queryId').value,document.getElementById('<%=complexElement.getName()%>').value,'complexType');"><fmt:message
                key="delete"/></a>

    </td>
</tr>
<%
        }
    }
    if (callQueries != null) {
        Iterator itrCallQueries = callQueries.iterator();
        if (itrCallQueries.hasNext()) {
%>
<tr>
    <td colspan="7"></td>
</tr>
<tr>
    <td colspan="3"><b><fmt:message key="query.id"/></b></td>
    <td colspan="3"><b><fmt:message key="data.services.user.roles"/></b></td>
    <td><b><fmt:message key="actions1"/></b></td>
</tr>
<%
    }
    while (itrCallQueries.hasNext()) {
        CallQuery callQuery = (CallQuery) itrCallQueries.next();
        String roles = "";
        if (callQuery.getRequiredRoles() != null && 
        		callQuery.getRequiredRoles().trim().length() > 0) {
            roles = callQuery.getRequiredRoles();
        } else {
            roles = "N/A";
        }
%>
<tr>
    <input type="hidden" id="<%=callQuery.getHref()%>" name="<%=callQuery.getHref()%>"
           value="<%=callQuery.getHref()%>"/>
    <td colspan="3"><%=callQuery.getHref()%>
    </td>
    <td colspan="3"><%=roles%>
    </td>
    <td>
        <a class="icon-link"
           style="background-image:url(../admin/images/edit.gif);"
           href="addOutputMapping.jsp?queryId=<%=queryId%>&selectedQuery=<%=callQuery.getHref()%>&edit=<%=callQuery.getHref()%>&requiredRoles=<%=roles%>&mappingType=query&flag=edit"><fmt:message
                key="edit"/></a>
        <a class="icon-link"
           style="background-image:url(../admin/images/delete.gif);"
           onclick="deleteOutputMappings(document.getElementById('queryId').value,
					           document.getElementById('<%=callQuery.getHref()%>').value,'query');"
           href="#"><fmt:message
                key="delete"/></a>
    </td>
</tr>

<%
                }
            }
        }

    }

    if (query == null || query.getResult() == null || ((query.getResult().getElements() == null || query.getResult().getElements().size() == 0) && (query.getResult().getAttributes() == null || query.getResult().getAttributes().size() == 0) && (query.getResult().getResources() == null || query.getResult().getResources().size() == 0) && (query.getResult().getCallQueries() == null || query.getResult().getCallQueries().size() == 0))) {
%>
<tr id="noOutputmappings">
    <td colspan="2"><fmt:message
            key="datasources.currently.there.are.no.output.mappings.present.for.this.query"/></td>
</tr>
<%
    }
%>
</tbody>
</table>
</td>
</tr>
<tr>
    <td>
        <a href="javascript:document.dataForm.submit();"
           onclick="return validateOutputMappingFields(<%=readOnly%>);"
           style="background-image: url(images/add.gif);" class="icon-link">
            <fmt:message key="add.output.mappings"/> </a>
    </td>
</tr>

<tr>
    <td colspan="2" class="middle-header"><fmt:message key="events.header"/></td>
</tr>
<tr>
    <td>
        <table>
            <tr>
                <td><fmt:message key="input.event.trigger"/></td>
                <td><Select id="inputEventTrigger" name="inputEventTrigger">
                    <option value="">--Select--</option>
                    <%
                        if (dataService.getEvents() != null) {
                            Iterator<Event> eventItr = dataService.getEvents().iterator();
                            while (eventItr.hasNext()) {
                                String eventId = eventItr.next().getId();
                                if (query != null) {
                                    if (query.getInputEventTrigger() != null) {
                                        if (query.getInputEventTrigger().equals(eventId)) {
                    %>
                    <option value="<%=eventId%>" selected="selected"><%=eventId%>
                    </option>
                    <%
                                }
                            }
                        }
                    %>
                    <option value="<%=eventId%>"><%=eventId%>
                    </option>
                    <%
                            }
                        }
                    %>
                </Select></td>
            </tr>
            <tr>
                <td><fmt:message key="output.event.trigger"/></td>
                <td><Select id="outputEventTrigger" name="outputEventTrigger">
                    <option value="">--Select--</option>
                    <%
                        if (dataService.getEvents() != null) {
                            Iterator<Event> eventItr = dataService.getEvents().iterator();
                            while (eventItr.hasNext()) {
                                String eventId = eventItr.next().getId();
                                if (query != null) {
                                    if (query.getOutputEventTrigger() != null) {
                                        if (query.getOutputEventTrigger().equals(eventId)) {
                    %>
                    <option value="<%=eventId%>" selected="selected"><%=eventId%>
                    </option>
                    <%
                                }
                            }

                        }
                    %>
                    <option value="<%=eventId%>"><%=eventId%>
                    </option>
                    <%
                            }
                        }
                    %>
                </Select></td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td>
        <a href="javascript: document.dataForm.action = 'queryProcessor.jsp?flag=event';document.dataForm.submit();"
           class="icon-link" style="background-image: url(images/event-sources.gif);"><fmt:message
                key="manage.events"/></a>
    </td>
</tr>

<tr>
    <td class="buttonRow"><input class="button"
                                 type="submit" value="<fmt:message key="save"/>"
                                 onclick="document.dataForm.action='queryProcessor.jsp?flag=save&edit=<%=readOnly%>';return validateAddQueryFormSave('<%=datasourceType%>');"/>
        <input
                class="button" type="submit" value="<fmt:message key="cancel"/>"
                onclick="document.dataForm.action='removeQuery.jsp'"/></td>
</tr>
</table>
</form>
</div>
</div>
<script type="text/javascript">
    alternateTableRows('existingInputMappingsTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('existingOutputMappingsTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>