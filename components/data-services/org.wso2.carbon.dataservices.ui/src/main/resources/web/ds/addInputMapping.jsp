<%--
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
--%>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Param" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Query" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Validator" %>
<%@ page import="java.util.ArrayList" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<!--
<carbon:breadcrumb
        label="Add/Edit Input Mapping"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
-->
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session">
</jsp:useBean>
<jsp:useBean id="validators" class="java.util.ArrayList" scope="session"/>
<%
    String queryId = request.getParameter("queryId");
    String paramName = request.getParameter("paramName");
    String paramType = request.getParameter("paramType");
    String defaultValue = request.getParameter("defaultValue");
    String structType = request.getParameter("structType");
    String ordinalVal = request.getParameter("inputMappingOrdinalId");
    String optional = request.getParameter("optional");
    String caption;
    boolean isEdit = false;
    Query query = null;
    int ordinal = 0;
    String sqlType = request.getParameter("sqlType");
    String inOutType = null;
    String ordinalStr = null;
    boolean disable;
    String serviceName = dataService.getName();
    
    if (queryId != null) {
        query = dataService.getQuery(queryId);
        if (query.getParam(paramName) != null) {
            isEdit = true;
            Param param = query.getParam(paramName);
            /* editing - set the validators in the session */
            session.setAttribute("validators", param.getValidators());
            validators = (ArrayList) session.getAttribute("validators");
            ordinal = param.getOrdinal();
            sqlType = param.getSqlType();
            inOutType = param.getType();
            paramType = param.getParamType();
            defaultValue = param.getDefaultValue();
            structType = param.getStructType();
            optional = param.getOptional();
            if (ordinal != 0) { //ordinal=0 when user not given a ordinal.
                ordinalStr = Integer.toString(ordinal);
            }
        }
    }

    paramName = (paramName == null) ? "" : paramName;
    paramType = (paramType == null) ? "" : paramType;
    defaultValue = (defaultValue == null) ? "" : defaultValue;
    structType = (structType == null) ? "" : structType;
    disable = (paramName != null);
    sqlType = (sqlType == null) ? "" : sqlType;
    inOutType = (inOutType == null) ? "" : inOutType;
    ordinalStr = (ordinalStr == null) ? "" : ordinalStr;
    optional = (optional == null) ? "" : optional;
    if (!paramName.equals("")) {
        caption = "save";
    } else {
        caption = "add";
    }


%>
<script type="text/javascript" src="js/ui-validations.js"></script>
<div id="middle">
<%
    if (isEdit) {%>
<h2>
    <fmt:message key="edit.input.mapping"/>
    <%
        out.write(" (" + serviceName + "/" + queryId + "/" + paramName + ")");
    %>
</h2>
<%
} else { %>
<h2>
    <fmt:message key="add.input.mapping"/>
    <%
        out.write(" (" + serviceName + "/" + queryId + ")");
    %>
</h2>
<%
    }
%>


<div id="workArea">
<table class="styledLeft noBorders" id="dataSources" cellspacing="0" width="100%">
<thead>
<tr>
    <th colspan="2"><fmt:message key="datasources.input.mappings"/></th>
</tr>
</thead>
    <%--    <form method="post" id="inputMappings" name="inputMappings" action="inputMappingProcessor.jsp" ">--%>
<form method="post" id="inputMappings" name="inputMappings" action="inputMappingProcessor.jsp"
      onsubmit="return validateInputMappings();">
<input type="hidden" name="queryId" value="<%=queryId%>" id="<%=queryId%>"/>
<input type="hidden" name="oldInputMappingId" value="<%=paramName%>" id="oldInputMappingId"/>
<input type="hidden" id="dsValidatorProperties" name="dsValidatorProperties" class="longInput"/>
    <%--<table class="styledLeft">--%>
    <%--<table class="normal">--%>

    <tr id="addNewInputMappingRow">
    <td>
    <input value="<%=request.getParameter("data_source")%>" name="datasource" id="datasource" type="hidden">
    <%--<input value="<%=request.getParameter("query_id")%>" name="queryId" id="queryId" size="30" type="hidden">--%>
    <input value="<%=request.getParameter("sql_stat")%>" name="sql" id="sql" type="hidden">
    </td>
    </tr>

<tr>
    <td class="leftCol-small"><fmt:message key="datasources.mapping.name"/><font color="red">*</font></td>
    <td>
        <input value="<%=paramName%>" name="inputMappingId" id="inputMappingNameId" size="30"
               type="text"/>

    </td>
</tr>
<tr>
    <td class="leftCol-small"><fmt:message key="dataservices.param.type"/></td>
    <td><select id="paramTypeId" name="paramType">
        <% if (paramType.equals("") || paramType.equals("SCALAR")) { %>
        <option value="SCALAR" selected="selected">SCALAR</option>
        <% } else { %>
        <option value="SCALAR">SCALAR</option>
        <% } %>
        <% if (paramType.equals("ARRAY")) { %>
        <option value="ARRAY" selected="selected">ARRAY</option>
        <% } else {
            if (!sqlType.equals("QUERY_STRING")) {%>
        <option id="paramTypeArrayOptionId" value="ARRAY">ARRAY</option>
        <%} else {%>
        <option id="paramTypeArrayOptionId" disabled value="ARRAY">ARRAY</option>
        <% } %>
        <% } %>
    </select></td>
</tr>
<tr>
    <td class="leftCol-small"><fmt:message key="dataservices.param.optional"/></td>
    <td><select id="optionalId" name="optional">
        <% if (optional.equalsIgnoreCase("false") || optional.equalsIgnoreCase("")) { %>
        <option value="false" selected="selected">False</option>
        <option value="true">True</option>
        <% } else if (optional.equalsIgnoreCase("true")) { %>
        <option value="true" selected="selected">True</option>
        <option value="false">False</option>
        <% } %>
    </select></td>
</tr>
<tr>
    <td class="leftCol-small"><fmt:message key="datasources.sql.type"/><font color="red">*</font></td>
    <td><select id="inputMappingSqlTypeId" name="inputMappingSqlType" onchange="changeVisiblityOnTypeSelection(this, document); adjustParameterType(this, document);">
        <% if (sqlType.equals("")) { %>
        <option value="" selected="selected">--SELECT--</option>
        <% } else { %>
        <option value="">--SELECT--</option>
        <% }
            if (sqlType.equals("STRING")) { %>
        <option value="STRING" selected="selected">STRING</option>
        <% } else { %>
        <option value="STRING">STRING</option>
        <% }
            if (sqlType.equals("INTEGER")) { %>
        <option value="INTEGER" selected="selected">INTEGER</option>
        <% } else { %>
        <option value="INTEGER">INTEGER</option>
        <% }
            if (sqlType.equals("REAL")) { %>
        <option value="REAL" selected="selected">REAL</option>
        <% } else { %>
        <option value="REAL">REAL</option>
        <% }
            if (sqlType.equals("DOUBLE")) { %>
        <option value="DOUBLE" selected="selected">DOUBLE</option>
        <% } else { %>
        <option value="DOUBLE">DOUBLE</option>
        <% }
            if (sqlType.equals("NUMERIC")) { %>
        <option value="NUMERIC" selected="selected">NUMERIC</option>
        <% } else { %>
        <option value="NUMERIC">NUMERIC</option>
        <% }
            if (sqlType.equals("TINYINT")) { %>
        <option value="TINYINT" selected="selected">TINYINT</option>
        <% } else { %>
        <option value="TINYINT">TINYINT</option>
        <% }
            if (sqlType.equals("SMALLINT")) { %>
        <option value="SMALLINT" selected="selected">SMALLINT</option>
        <% } else { %>
        <option value="SMALLINT">SMALLINT</option>
        <% }
            if (sqlType.equals("BIGINT")) { %>
        <option value="BIGINT" selected="selected">BIGINT</option>
        <% } else { %>
        <option value="BIGINT">BIGINT</option>
        <% }
            if (sqlType.equals("DATE")) { %>
        <option value="DATE" selected="selected">DATE[yyyy-mm-dd]</option>
        <% } else { %>
        <option value="DATE">DATE[yyyy-mm-dd]</option>
        <% }
            if (sqlType.equals("TIME")) { %>
        <option value="TIME" selected="selected">TIME[hh:mm:ss]</option>
        <% } else { %>
        <option value="TIME">TIME[hh:mm:ss]</option>
        <% }
            if (sqlType.equals("TIMESTAMP")) { %>
        <option value="TIMESTAMP" selected="selected">TIMESTAMP</option>
        <% } else { %>
        <option value="TIMESTAMP">TIMESTAMP</option>
        <% }
            if (sqlType.equals("BIT")) { %>
        <option value="BIT" selected="selected">BIT</option>
        <% } else { %>
        <option value="BIT">BIT</option>
        <% }
            if (sqlType.equals("ORACLE_REF_CURSOR")) { %>
        <option value="ORACLE_REF_CURSOR" selected="selected">ORACLE REF CURSOR</option>
        <% } else { %>
        <option value="ORACLE_REF_CURSOR">ORACLE REF CURSOR</option>
        <% }
            if (sqlType.equals("BINARY")) { %>
        <option value="BINARY" selected="selected">BINARY</option>
        <% } else { %>
        <option value="BINARY">BINARY</option>
        <% }
            if (sqlType.equals("BLOB")) { %>
        <option value="BLOB" selected="selected">BLOB</option>
        <% } else { %>
        <option value="BLOB">BLOB</option>
        <% } if (sqlType.equals("CLOB")) { %>
        <option value="CLOB" selected="selected">CLOB</option>
        <% } else { %>
        <option value="CLOB">CLOB</option>
         <% } if (sqlType.equals("STRUCT")) { %>
        <option value="STRUCT" selected="selected">STRUCT</option>
        <% } else { %>
        <option value="STRUCT">STRUCT</option>
        <% }  if (sqlType.equals("ARRAY")) { %>
        <option value="ARRAY" selected="selected">ARRAY</option>
        <% } else { %>
        <option value="ARRAY">ARRAY</option>
        <% }  if (sqlType.equals("UUID")) { %>
        <option value="UUID" selected="selected">UUID</option>
        <% } else { %>
        <option value="UUID">UUID</option>
        <% }  if (sqlType.equals("VARINT")) { %>
        <option value="VARINT" selected="selected">VARINT</option>
        <% } else { %>
        <option value="VARINT">VARINT</option>
        <% }  if (sqlType.equals("INETADDRESS")) { %>
        <option value="INETADDRESS" selected="selected">INETADDRESS</option>
        <% } else { %>
        <option value="INETADDRESS">INETADDRESS</option>
        <% } if (sqlType.equals("QUERY_STRING")) { %>
        <option value="QUERY_STRING" selected="selected">QUERY_STRING</option>
        <% } else { %>
        <option value="QUERY_STRING">QUERY_STRING</option>
        <% } %>
    </select>
    </td>
</tr>
<tr style="<%=(paramType.equals("SCALAR") && (!"STRUCT".equals(sqlType)) || !"ARRAY".equals(sqlType)) ||
paramType.equals("") ? "" : "display:none"%>"
    id="defaultValueRow">
    <td class="leftCol-small"><fmt:message key="dataservices.default.value"/></td>
    <td><input type="text" size="30" value="<%=defaultValue%>" name="defaultValue"
               id="defaultValue"/></td>
</tr>
<tr id="structTypeRow" style="<%=("STRUCT".equals(sqlType)) && !"".equals(structType) && (structType != null) ? "" :
"display:none"%>">
    <td class="leftCol-small"><fmt:message key="dataservices.input.struct.type"/><font color="red">*</font></td>
    <td><input type="text" size="30" value="<%=structType%>" name="structType"
               id="structType"/></td>
</tr>
<tr>
    <td class="leftCol-small"><fmt:message key="datasources.in.out.type"/></td>
    <td><select id="inputMappingInOutTypeId" name="inputMappingInOutType"
                onchange="inOutVisibilityOnChange(this,document)">
        <% if (inOutType.equals("IN") || inOutType.equals("")) { %>
        <option value="IN" selected="selected">IN</option>
        <option value="OUT">OUT</option>
        <option value="INOUT">INOUT</option>
        <% }
            if (inOutType.equals("OUT")) { %>
        <option value="IN">IN</option>
        <option value="OUT" selected="selected">OUT</option>
        <option value="INOUT">INOUT</option>
        <% }
            if (inOutType.equals("INOUT")) { %>
        <option value="IN">IN</option>
        <option value="OUT">OUT</option>
        <option value="INOUT" selected="selected">INOUT</option>
        <% }%>
    </select></td>
</tr>
<tr>
    <td class="leftCol-small"><fmt:message key="datasources.ordinal"/></td>
    <td><input value="<%=ordinalStr%>" id="inputMappingOrdinalId" name="inputMappingOrdinal"
               size="5" type="text"></td>
</tr>

<tr>
        <%--<table  style="<%=inOutType.equals("OUT") ? "display:none" : ""%>" id="validatorRow">--%>
<tr>
    <td class="middle-header" colspan="2"><fmt:message key="dataservices.add.validations"/></td>
</tr>
    <%--<table class="styledLeft noBorders" id="validatorRow" cellspacing="0" width="100%">--%>
    <%--<thead>--%>
    <%--<tr>--%>
    <%--<td class="middle-header" colspan="2"><fmt:message key="dataservices.add.validations"/></td>--%>
    <%--</tr>--%>
    <%--</thead>--%>
    <%--<tr>--%>
    <%--<td class="middle-header" colspan="2"><fmt:message key="dataservices.add.validations"/></td>--%>
    <%--&lt;%&ndash;<td><b><fmt:message key="dataservices.add.validations"/></b></td>&ndash;%&gt;--%>
    <%--</tr>--%>
<tr>
    <td class="leftCol-small"><fmt:message key="dataservices.validator"/></td>
    <td><select id="validatorList" name="validatorList"
                onchange="changeAddValidatorFields(this,document);">
        <option value="#">--Select--</option>
        <option value="validateLongRange">Long Range Validator</option>
        <option value="validateDoubleRange">Double Range Validator</option>
        <option value="validateLength">Length Validator</option>
        <option value="validatePattern">Pattern Validator</option>
        <option value="validateCustom">Custom Validator</option>
    </select></td>
</tr>

<div id="validators" style="display:none">
        <%--<table>--%>
    <tr id="maxRangeValidatorElementsRow" style="display:none">
        <td class="leftCol-small"><fmt:message key="dataservice.range.validator.max"/><font color="red">*</font></td>
        <td><input type="text" id="max" name="max" size="15"></td>
    </tr>
    <tr id="minRangeValidatorElementsRow" style="display:none">
        <td class="leftCol-small"><fmt:message key="dataservice.range.validator.min"/><font color="red">*</font></td>
        <td><input type="text" id="min" name="min" size="15"></td>
    </tr>
    <tr id="patternValidatorElementsRow" style="display:none">
        <td class="leftCol-small"><fmt:message key="dataservice.validator.pattern"/><font color="red">*</font></td>
        <td><input type="text" id="pattern" name="pattern" size="30"></td>
    </tr>
    <tr id="customValidatorElementsRow" style="display:none">
        <td class="leftCol-small"><fmt:message key="dataservice.validator.custom.class"/><font color="red">*</font></td>
        <td><input type="text" id="customClass" name="customClass" size="30"></td>
    </tr>
    <tr id="customValidatorPropertyElementsRow" style="display:none">
        <td>
            <fmt:message key="custom.properties"/>
        </td>
        <td>
            <div id="nameValueAdd">
                <a class="icon-link"
                   href="#addNameLink"
                   onclick="addValidatorProperties();"
                   style="background-image: url(../admin/images/add.gif);"><fmt:message
                        key="add.new.validator.properties"/></a>

                <div style="clear:both;"></div>
            </div>
            <div>
                <table cellpadding="0" cellspacing="0" border="0" class="styledLeft"
                       id="dsValidatorPropertyTable"
                       style="display:none;">
                    <thead>
                    <tr>
                        <th style="width:40%"><fmt:message key="validator.prop.name"/></th>
                        <th style="width:40%"><fmt:message key="validator.prop.value"/></th>
                        <th style="width:20%"><fmt:message key="validator.prop.action"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
            </div>
        </td>
    </tr>
    <tr>
        <td class="leftCol-small"><input type="submit" style="display:none" id="addValidator"
                   value="<fmt:message key="dataservices.addValidator"/>" class="button"
                   onclick="var val = validateValidators(this,document); if (val) {addValidators()} else return false;"/>
                <%--onclick="validateValidators(this,document);"/>--%>
        </td>
    </tr>
        <%--</table>--%>
</div>
    <%--</table>--%>
</tr>

<%
    if (validators.size() > 0) { %>
<tr>
    <td colspan="2">
        <table class="styledInner" cellspacing="0" id="existingValidators">
            <%--<%--%>
                <%--if (validators.size() == 0) {--%>
            <%--%>--%>
            <%--<tr>--%>
                <%--<td colspan="3"><fmt:message key="dataservices.there.are.no.validations"/></td>--%>
            <%--</tr>--%>
            <%--<% }--%>
            <%
                if (validators.size() > 0) { 
                int i = 0;
                %>
            <thead>
            <tr>
                <th><fmt:message key="dataservices.validator"/></th>
                <th><fmt:message key="dataservices.validation.value"/></th>
                <th><fmt:message key="datasources.action"/></th>
            </tr>
            </thead>
            <% for (Object tmpVal : validators) {
                Validator valObj = (Validator) tmpVal;
            %>
            <tr>
                <td><%=valObj.getName()%>
                </td>
                <td><%=valObj.getPropertiesString()%>
                <%if (valObj.getName().equals("Pattern Validator")) { %>
                	<input type="hidden" id="propString<%=i %>" name="propString<%=i %>" size="30" value=<%=valObj.getPropertiesString()%>>
                <%} else { %>
                	<input type="hidden" id="propString<%=i %>" name="propString<%=i %>" size="30" value='<%=valObj.getPropertiesString()%>'>
                <%} %>
                </td>
                <td>
                    <a class="icon-link" style="background-image:url(../admin/images/delete.gif);"
                       href="inputMappingProcessor.jsp?queryId=<%=queryId%>&validatorList=<%=valObj.getElementName()%>&inputMappingId=<%=paramName%>&inputMappingSqlType=<%=sqlType%>&defaultValue=<%=defaultValue%>&structType=<%=(structType == null ? "" : structType)%>&flag=deleteValidator&origin=add">
                        <fmt:message key="delete"/></a>
                    <a class="icon-link" style="background-image:url(../admin/images/edit.gif);"
                       href="#"
                       onclick="toggleValidators('<%=valObj.getName()%>', <%=i%>, document);">
                        <fmt:message key="edit"/></a>
                </td>
            </tr>
            <% 
            	i++;
            }
            
            }
            %>
        </table>
    </td>
</tr>
<% } %>

<tr>
    <td colspan="2">
        <table class="styledInner" cellspacing="0" id="existinginputMappingsTable">
            <% if (paramName.equals("")) {
                if (queryId == null) {
            %>
            <tr>
                <td colspan="3"><fmt:message key="datasources.no.inputmapping"/></td>
            </tr>
            <% } else {
                Param[] params = query.getParams();
                if (params != null) {
            %>
            <tr>
                <td><b><fmt:message key="datasources.mapping.name"/></b></td>
                <td><b><fmt:message key="dataservices.param.type"/></b></td>
                <td><b><fmt:message key="datasources.sql.type"/></b></td>
                <td><b><fmt:message key="dataservices.default.value"/></b></td>
                <td><b><fmt:message key="datasources.action"/></b></td>
            </tr>
            <% for (int a = 0; a <= params.length - 1; a++) { %>
            <tr>
                <input type="hidden" id="<%=params[a].getName()%>" name="<%=params[a].getName()%>"
                       value="<%=params[a].getName()%>"/>
                <input type="hidden" id="<%=params[a].getSqlType()%>"
                       name="<%=params[a].getSqlType()%>"
                       value="<%=params[a].getSqlType()%>"/>
                <td><%=params[a].getName()%>
                </td>
                <td><%=params[a].getParamType()%>
                </td>
                <td><%=params[a].getSqlType()%>
                </td>
                <td><% if (params[a].getDefaultValue() != null) { %>
                    <%=params[a].getDefaultValue()%>
                    <% } %>
                </td>
                <td>
                    <a class="icon-link" style="background-image:url(../admin/images/edit.gif);"
                       href="addInputMapping.jsp?paramName=<%=params[a].getName()%>&queryId=<%=queryId%>&paramType=<%=params[a].getParamType()%>&structType=<%=params[a].getStructType()%>"><fmt:message
                            key="edit"/></a>
                    <a class="icon-link" style="background-image:url(../admin/images/delete.gif);"
                       href="#"
                       onclick="deleteInputMappings(document.getElementById('<%=params[a].getName()%>').value,
                       document.getElementById('<%=params[a].getSqlType()%>').value,
                       document.getElementById('<%=queryId%>').value,'sql');"><fmt:message
                            key="delete"/></a>
                </td>
            </tr>
            <% //  }
            }

            } else {
            %>
            <tr>
                <td colspan="3">
                    <fmt:message key="datasources.no.inputmapping"/>
                </td>
            </tr>
            <%
                        }
                    }
                }
            %>
        </table>
    </td>
</tr>
<tr>
    <td class="buttonRow" colspan="2">
        <input class="button" type="button" value="<fmt:message key="mainConfiguration"/>"
               onclick="redirectToMainConfiguration(document.getElementById('<%=queryId%>').value);"/>
        <input class="button" type="submit" value="<fmt:message key="<%=caption%>"/>"
               onclick="document.inputMappings.action = 'inputMappingProcessor.jsp?flag=add'"/>
    </td>
</tr>


</form>
</table>
</div>
</div>
<script type="text/javascript">
    alternateTableRows('existinginputMappingsTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>