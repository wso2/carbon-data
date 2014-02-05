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
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.*" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<!--
<carbon:breadcrumb
        label="Add/Edit Output Mapping"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
-->
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session">
</jsp:useBean>
<script type="text/javascript" src="js/ui-validations.js"></script>

<%
    String queryId = request.getParameter("queryId");

    if (queryId != null) {
        Config c = dataService.getConfig(dataService.getQuery(queryId).getConfigToUse());
    }
    boolean isEdit = false;
    String rowelement = null;
    String rowName = null;
    String nameSpace = null;
    boolean isUseColomnNumbers;
    String name = request.getParameter("name");
    String complexElementName = request.getParameter("txtDataServiceComplexElementName");
    String complexElementNamespace = request.getParameter("txtDataServiceComplexElementNamespace");
    String mappingType = request.getParameter("mappingType");
    String datasourceType = request.getParameter("datasourceType");
    String datasourceValue = request.getParameter("datasourceValue");
    String selectedQuery = request.getParameter("selectedQuery");
    String exportName = request.getParameter("exportName");
    String exportType = request.getParameter("exportType");
    String flag = request.getParameter("flag");
    String complexMappingId = request.getParameter("complexMappingId");
    String elementNamespace = request.getParameter("txtDataServiceElementNamespace");
    String xsdType = request.getParameter("xsdType");
    String requiredRoles = request.getParameter("requiredRoles");
    String complexElementId = request.getParameter("complexElementId");
    String complexPath = request.getParameter("complexPath");
    String paramType = request.getParameter("paramType");
    String arrayName = request.getParameter("arrayName");
    String caption;
    Result result;
    String serviceName = dataService.getName();
    Query query = new Query();
    Query mainQuery = dataService.getQuery(queryId);
    complexPath = (complexPath == null) ? "" : complexPath;
    selectedQuery = (selectedQuery == null) ? "" : selectedQuery;
    datasourceType = (datasourceType == null) ? "column" : datasourceType;
    boolean optional = false;
    String enableOptional =  request.getParameter("optional");
    if (enableOptional != null) {
        optional = Boolean.parseBoolean(enableOptional);
    }
    
    if (complexElementNamespace != null && complexElementNamespace.equals("null")) {
        complexElementNamespace = "";
    }

    if (mainQuery != null) {
        result = mainQuery.getResult();
        if (result != null) {
            rowName = result.getRowName();
            rowelement = result.getResultWrapper();
            nameSpace = result.getNamespace();
        }
    }
    complexMappingId = (complexMappingId == null) ? "" : complexMappingId;
    exportName = (exportName == null) ? "" : exportName;
    complexElementName = (complexElementName == null) ? "" : complexElementName;
    elementNamespace = (elementNamespace == null) ? "" : elementNamespace;
    complexElementNamespace = (complexElementNamespace == null) ? "" : complexElementNamespace;
    exportType = (exportType == null) ? "SCALAR" : exportType;
    datasourceValue = (datasourceValue == null) ? "" : datasourceValue;
    queryId = (query == null) ? "" : queryId;
    rowelement = (rowelement == null) ? "" : rowelement;
    rowName = (rowName == null) ? "" : rowName;
    nameSpace = (nameSpace == null) ? "" : nameSpace;
    name = (name == null) ? "" : name;
    mappingType = (mappingType == null) ? "" : mappingType;
    flag = (flag == null) ? "add" : flag;
    xsdType = (xsdType == null) ? "" : xsdType;
    paramType = (paramType == null) ? "SCALAR" : paramType;
    arrayName = (arrayName == null) ? "" : arrayName;
    requiredRoles = (requiredRoles == null) ? "" : requiredRoles;
    List<Query> queries = dataService.getQueries();
    UserAdminClient client;

    FlaggedName[] userRoles = null;
    if (mappingType.equals("")) {
        caption = "add";
    } else {
        caption = "save";
    }
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    try {
        client = new UserAdminClient(cookie, backendServerURL, configContext);
        FlaggedName[] userRoleData = client.getAllRolesNames("*", -1);
        ArrayList<FlaggedName> userRoleDataList = new ArrayList<FlaggedName>(Arrays.asList(userRoleData));
        userRoleDataList.remove(userRoleDataList.size() - 1);
        userRoles = new FlaggedName[userRoleDataList.size()];
        userRoles = userRoleDataList.toArray(userRoles);
    } catch (Exception e) {
        e.printStackTrace();
    }
    if (requiredRoles.equals("N/A")) {
        requiredRoles = "";
    }
    // breaking comma seperated roles
    String[] selectedRoles = requiredRoles.split(",");

    if (mappingType != null && !mappingType.equals("")) {
        isEdit = true;
    }

    result = mainQuery.getResult();
    isUseColomnNumbers = Boolean.parseBoolean(result.getUseColumnNumbers());
%>

<div id="middle">
<%
    if (isEdit) {
%>
<h2> Edit Output Mapping
    <%
        out.write(" (" + serviceName + "/" + queryId + "/" + name + ")");
    %>
</h2>
<%} else { %>
<h2> Add Output Mapping
    <%
        out.write(" (" + serviceName + "/" + queryId + ")");
    %>
</h2>
<%} %>

    <%--<h2>Add/Edit Output Mapping</h2>--%>

<div id="workArea">
<% String edit = request.getParameter("edit");
    edit = (edit == null) ? "" : edit;
%>
<%!
    private List<String> getSubComplexElementList(String path) {
        String children[] = path.split("/");
        List<String> pathTokens = new ArrayList<String>();
        for (int i = 0; i < children.length - 1; i++) {
            if (children[i].length() > 0) {
                pathTokens.add(children[i]);
            }
        }
        return pathTokens;
    }


    private String getSubComplexPath(String path) {
        String children[] = path.split("/");
        String subPath = "";
        for (int i = 0; i < children.length; i++) {
            if (i < children.length - 1 && !children[i].equals("")) {
                subPath = subPath + "/" + children[i];
            }
        }
        return subPath;

    }
%>

<form method="post" action="OutputMappingProcessor.jsp?flag=outputMapping&edit=<%=edit%>"
      id="outputMapping"
      name="outputMapping">


<table class="styledLeft" id="outputmappingTable" width="100%">
<tbody>
<tr>
    <td class="middle-header">
        <input value="<%=queryId%>" name="queryId" id="queryId" size="30" type="hidden"/>
        <input value="<%=complexPath%>" name="complexPath" id="complexPath" size="30"
               type="hidden"/>
        <input value="<%=complexElementId%>" name="complexElementId" id="complexElementId" size="30"
               type="hidden"/>
        <input value="<%=rowelement%>" name="txtDataServiceWrapElement"
               id="txtDataServiceWrapElement" size="30" type="hidden"/>
        <input value="<%=rowName%>" name="txtDataServiceRowName" id="txtDataServiceRowName"
               size="30" type="hidden"/>
        <input value="<%=nameSpace%>" name="txtDataServiceRowNamespace"
               id="txtDataServiceRowNamespace" size="30"
               type="hidden"/>
        <input value="<%=requiredRoles%>" id="requiredRoles" name="requiredRoles" type="hidden"/>
        <% if (mappingType != null && !mappingType.equals("")) {%>
        <input type="hidden" name="cmbDataServiceOMType" value="<%=mappingType%>"/>
        <% } %>
        
        <%
            if (isEdit) {
        %>
        Edit Output Mapping
        <%} else { %>
        Add Output Mapping
        <%} %>

        <% if (flag.equals("complexError")) { %>
        <input value="" id="mappingType" name="mappingType" type="hidden"/>
        <% } else { %>
        <input value="<%=mappingType%>" id="mappingType" name="mappingType" type="hidden"/>
        <% } %>

    </td>
</tr>
<tr>
<td>
<table class="normal" width="100%">
    <tr>
        <td>
            <table>
                <a name="outputMapping"></a>
                <tr>
                    <td class="leftCol-med"><fmt:message key="data.services.mapping.type"/><font
                            color="red">*</font>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    </td>
                    <% if (mappingType != null && !mappingType.equals("")) {%>
                    <td><select onchange="changeToNextMapping(this, document);return false;"
                                name="cmbDataServiceOMType"
                                id="cmbDataServiceOMType" disabled="disabled">
                     <% } else {%>   
                    <td><select onchange="changeToNextMapping(this, document);return false;"
                                name="cmbDataServiceOMType"
                                id="cmbDataServiceOMType">
                        <% } %>
                        <% if (mappingType.equals("")) { %>
                        <option selected="selected" value="">--Select--</option>
                        <% } else { %>
                        <option value="">--Select--</option>
                        <% }
                            if (mappingType.equals("element")) { %>
                        <option selected="true" value="element">element</option>
                        <% } else { %>
                        <option value="element">element</option>
                        <% }
                            if (mappingType.equals("attribute")) { %>
                        <option selected="selected" value="attribute">attribute</option>
                        <% } else { %>
                        <option value="attribute">attribute</option>
                        <% }
                            if (mappingType.equals("query")) { %>
                        <option selected="selected" value="query">query</option>
                        <% } else { %>
                        <option value="query">query</option>
                        <% }
                            if (mappingType.equals("complexType")) { %>
                        <option selected="selected" value="complexType">complex element</option>
                        <% } else { %>
                        <option value="complexType">complex element</option>
                        <% } %>
                    </select></td>
                </tr>
            </table>
        </td>
    </tr>
</table>
<div id="complexTypeRowId" style="<%=(mappingType.equals("complexType"))  ? "" : "display:none"%>">
    <table class="normal">
        <tr>
            <td>
                <table>
                    <tr>
                        <td class="leftCol-med"><fmt:message
                                key="dataservice.complex.element.name"/></td>
                        <td><input value="<%=complexElementName%>"
                                   id="txtDataServiceComplexElementName"
                                   name="txtDataServiceComplexElementName"
                                   size="30" type="text"></td>
                    </tr>
                    <tr>
                        <td class="leftCol-med"><fmt:message
                                key="dataservice.complex.element.namespace"/></td>
                        <td><input value="<%=complexElementNamespace%>"
                                   id="txtDataServiceComplexElementNamespace"
                                   name="txtDataServiceComplexElementNamespace"
                                   size="30" type="text"></td>
                    </tr>
                    <input value="<%=mappingType.equals("complexType")%>" name="flag" id="flag"
                           size="30" type="hidden"/>
                    <tr>
                        <td class="leftCol-small"><fmt:message key="dataservices.param.type"/></td>
                        <td><select id="paramTypeId1" name="paramType"
                                    onchange="arrayNameVisibilityOnChange(this,document)">
                            <% if ("".equals(arrayName)) { %>
                            <option value="SCALAR" selected="selected">SCALAR</option>
                            <% } else { %>
                            <option value="SCALAR">SCALAR</option>
                            <% } %>
                            <% if (!"".equals(arrayName)) { %>
                            <option value="ARRAY" selected="selected">ARRAY</option>
                            <% } else { %>
                            <option value="ARRAY">ARRAY</option>
                            <% } %>
                        </select></td>
                    </tr>
                    <tr id="arrayNameRow1"
                        style="<%=!"".equals(arrayName) ? "" : "display:none"%>">
                        <td class="leftCol-med"><fmt:message
                                key="dataservice.output.array.name"/></td>
                        <td><input value="<%=arrayName%>" id="arrayName1"
                                   name="arrayName1"
                                   size="30" type="text"></td>
                    </tr>
                    <tr>
                        <td><input class="button" type="submit" id="newOutputMapping"
                                   onclick="return validateComplexElement();"
                                   value="Add Nested Element">
                        </td>
                    </tr>

                </table>
            </td>
        </tr>
    </table>

</div>



<div id="queryRow" style="<%=mappingType.equals("query") ? "" : "display:none"%>">

    <table class="normal">
        <tr>
            <td>
                <table>
                    <tr>
                        <td class="leftCol-med"><fmt:message key="dataservices.select.query"/><font
                                color="red">*</font>
                        </td>
                        <td><select id="cmbDataServiceQueryId" name="cmbDataServiceQueryId"
                                    onchange="javascript:location.href = 'addOutputMapping.jsp?queryId='+
                            document.getElementById('queryId').value+'&selectedQuery='+
                            this.options[this.selectedIndex].value+'&rowName='+
                            document.getElementById('txtDataServiceRowName').value+'&element='+
                            document.getElementById('txtDataServiceWrapElement').value+'&ns='+
                            document.getElementById('txtDataServiceRowNamespace').value+'&requiredRoles='+
                            document.getElementById('requiredRoles').value+'&xsdType='+
                            document.getElementById('xsdType').value+
                            '&complexElementId='+document.getElementById('complexElementId').value +
                            '&complexPath='+document.getElementById('complexPath').value+
                            '&mappingType=query&edit=<%=edit%>&flag=<%=flag%>';">

                            <% if (queryId != null && queryId.trim().equals("")) {%>
                            <option value="" selected="selected"></option>
                            <% } else {%>
                            <option value=""></option>
                            <% }%>

                            <%
                                if (queries != null && queries.size() > 0) {
                                    Iterator iterator = queries.iterator();
                                    while (iterator.hasNext()) {
                                        query = (Query) iterator.next();
                                        if (selectedQuery != null && selectedQuery.trim().equals(query.getId())) {
                            %>
                            <option value="<%=query.getId()%>"
                                    selected="selected"><%=query.getId()%>
                            </option>
                            <%
                            } else {
                                if (!queryId.equals(query.getId())) {
                            %>
                            <option value="<%=query.getId()%>"><%=query.getId()%>
                            </option>
                            <%
                                            }
                                        }
                                    }
                                }
                            %>
                        </select></td>
                    </tr>

                    <%
                        if (selectedQuery != null && selectedQuery.trim().length() > 0) {
                            query = dataService.getQuery(selectedQuery);

                            if (query != null) {
                                Param[] params = query.getParams();
                                if (params != null) {
                                    if (params.length > 0) {
                                        result = mainQuery.getResult();
                                        List<CallQuery> callQueries = result.getCallQueries();
                                        //get relavent call query to get their with params
                                        List<WithParam> withParams = null;
                                        Iterator itrCallQueries = callQueries.iterator();
                                        while (itrCallQueries.hasNext()) {
                                            CallQuery callQuery = (CallQuery) itrCallQueries.next();
                                            if (callQuery.getHref().equals(query.getId())) {
                                                withParams = callQuery.getWithParams();
                                            }
                                        }

                                        //Params exist.Draw column headers

                    %>


                    <tr>
                        <td colspan="2"><b><fmt:message
                                key="dataservice.query.parameter.mapping"/></b></td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <table class="styledInner" cellspacing="0" id="queryMappingParameter">
                                <tr>
                                    <td><b><fmt:message key="dataservice.query.parameter.name"/></b>
                                    </td>
                                    <td><b><fmt:message key="dataservices.mapping.name"/></b></td>
                                    <td><b><fmt:message key="dataservices.output.mapping.type"/></b>
                                    </td>
                                </tr>
                                <% for (int a = 0; a < params.length; a++) {
                                    String paramMappingType = null;
                                    String paramMappingValue = null;
                                    if (withParams != null) {
                                        paramMappingType = withParams.get(a).getParamType();
                                        paramMappingValue = withParams.get(a).getParamValue();
                                    }

                                    paramMappingValue = (paramMappingValue == null) ? params[a].getName() : paramMappingValue;
                                    paramMappingType = (paramMappingType == null) ? "column" : paramMappingType;
                                %>
                                <tr>
                                    <td><%=params[a].getName()%>
                                    </td>
                                    <td><input type="text" size="50" id="<%= params[a].getName()%>"
                                               name="<%= params[a].getName()%>"
                                               value="<%=paramMappingValue%>"/>
                                    </td>
                                    <td><select id="<%="MappingType"+ params[a].getName()%>"
                                                name="<%="MappingType"+ params[a].getName()%>">
                                        <%if (paramMappingType.equals("") || paramMappingType.equals("column")) { %>
                                        <option value="column" selected="selected">column</option>
                                        <% } else { %>
                                        <option value="column">column</option>
                                        <% } %>
                                        <% if (paramMappingType.equals("query-param")) { %>
                                        <option value="query-param" selected="selected">
                                            query-param
                                        </option>
                                        <% } else { %>
                                        <option value="query-param">query-param</option>
                                        <% } %>
                                    </select>
                                    </td>
                                </tr>
                                <% } %>
                            </table>
                            <%
                                            }
                                        }
                                    }
                                }
                            %>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>

</div>

<div id="omElementRowId"
     style="<%=(mappingType.equals("element") || mappingType.equals("attribute")) ? "" : "display:none"%>">

    <table class="normal">
        <tr>
            <td>
                <table>
                    <tr>
                        <td class="leftCol-med"><fmt:message
                                key="data.services.datasource.type"/><font color="red">*</font>
                        <td><select onchange="changeToDataSourceType(this, document);return false;"
                                    id="datasourceTypeId" name="datasourceType">
                            <%if (datasourceType.equals("") || datasourceType.equals("column")) { %>
                            <option value="column" selected="selected">column</option>
                            <% } else { %>
                            <option value="column">column</option>
                            <% } %>
                            <% if (datasourceType.equals("query-param")) { %>
                            <option value="query-param" selected="selected">query-param</option>
                            <% } else { %>
                            <option value="query-param">query-param</option>
                            <% } %>
                        </select></td>
                    </tr>
                    <tr>
                        <td class="leftCol-med"><fmt:message
                                key="dataservice.output.field.name"/></td>
                        <td><input value="<%=name%>" id="txtDataServiceOMElementName"
                                   name="txtDataServiceOMElementName"
                                   size="30" type="text"></td>
                    </tr>
                    <tr id="elementNameSpaceRow"
                        style="<%=(mappingType.equals("") || mappingType.equals("element")) ? "" : "display:none"%>">
                        <td class="leftCol-med"><fmt:message
                                key="dataservice.element.namespace"/></td>
                        <td><input value="<%=elementNamespace%>" id="txtDataServiceElementNamespace"
                                   name="txtDataServiceElementNamespace"
                                   size="30" type="text"></td>
                    </tr>
                    <tr id="columnRow"
                        style="<%=datasourceType.equals("column") ? "" : "display:none"%>">
                        <% if (isUseColomnNumbers) { %>
                        <td class="leftCol-med"><fmt:message key="dataservice.datasource.column.number"/></td>
                        <td><input value="<%=datasourceValue%>" id="datasourceValue1"
                                   name="datasourceValue1"
                                   size="30" type="text"></td>
                        <% } else { %>
                        <td class="leftCol-med"><fmt:message key="dataservice.datasource.column.name"/></td>
                        <td><input value="<%=datasourceValue%>" id="datasourceValue1"
                                   name="datasourceValue1"
                                   size="30" type="text"></td>
                        <% }%>
                    </tr>
                    <tr id="queryParamnRow"
                        style="<%=datasourceType.equals("query-param") ? "" : "display:none"%>">
                        <td class="leftCol-med"><fmt:message
                                key="dataservice.datasource.query.param.name"/></td>
                        <td><input value="<%=datasourceValue%>" id="datasourceValue2"
                                   name="datasourceValue2"
                                   size="30" type="text"></td>
                    </tr>
                    <tr>
                        <td class="leftCol-small"><fmt:message key="dataservices.param.type"/></td>
                        <td><select id="paramTypeId" name="paramType"
                                    onchange="arrayNameVisibilityOnChange(this, document)">
                            <% if ("".equals(arrayName)) { %>
                            <option value="SCALAR" selected="selected">SCALAR</option>
                            <% } else { %>
                            <option value="SCALAR">SCALAR</option>
                            <% } %>
                            <% if (!"".equals(arrayName)) { %>
                            <option value="ARRAY" selected="selected">ARRAY</option>
                            <% } else { %>
                            <option value="ARRAY">ARRAY</option>
                            <% } %>
                        </select></td>
                    </tr>
                    <tr id="arrayNameRow"
                        style="<%=!"".equals(arrayName) ? "" : "display:none"%>">
                        <td class="leftCol-med"><fmt:message
                                key="dataservice.output.array.name"/></td>
                        <td><input value="<%=arrayName%>" id="arrayName"
                                   name="arrayName"
                                   size="30" type="text"></td>
                    </tr>
                    <tr>
                        <td class="leftCol-med"><fmt:message key="data.services.xsdType"/></td>
                        <td>
                            <select id="xsdType" name="xsdType">
                                <% if (xsdType.equals("string")) { %>
                                <option value="string" selected="selected">string</option>
                                <% } else { %>
                                <option value="string">string</option>
                                <% }
                                    if (xsdType.equals("integer")) { %>
                                <option value="integer" selected="selected">integer</option>
                                <% } else { %>
                                <option value="integer">integer</option>
                                <% }
                                    if (xsdType.equals("boolean")) { %>
                                <option value="boolean" selected="selected">boolean</option>
                                <% } else { %>
                                <option value="boolean">boolean</option>
                                <% }
                                    if (xsdType.equals("float")) { %>
                                <option value="float" selected="selected">float</option>
                                <% } else { %>
                                <option value="float">float</option>
                                <% }
                                    if (xsdType.equals("double")) { %>
                                <option value="double" selected="selected">double</option>
                                <% } else { %>
                                <option value="double">double</option>
                                <% }
                                    if (xsdType.equals("decimal")) { %>
                                <option value="decimal" selected="selected">decimal</option>
                                <% } else { %>
                                <option value="decimal">decimal</option>
                                <% }
                                    if (xsdType.equals("dateTime")) { %>
                                <option value="dateTime" selected="selected">dateTime</option>
                                <% } else { %>
                                <option value="dateTime">dateTime</option>
                                <% }
                                    if (xsdType.equals("time")) { %>
                                <option value="time" selected="selected">time</option>
                                <% } else { %>
                                <option value="time">time</option>
                                <% }
                                    if (xsdType.equals("date")) { %>
                                <option value="date" selected="selected">date</option>
                                <% } else { %>
                                <option value="date">date</option>
                                <% }
                                    if (xsdType.equals("long")) { %>
                                <option value="long" selected="selected">long</option>
                                <% } else { %>
                                <option value="long">long</option>
                                <% }
                                    if (xsdType.equals("base64Binary")) { %>
                                <option value="base64Binary" selected="selected">
                                    base64Binary
                                </option>
                                <% } else { %>
                                <option value="base64Binary">base64Binary</option>
                                <% } %>
                            </select></td>
                    </tr>
                    <tr>
                        <td>
                            <label for="optional"><fmt:message key="dataservice.optional"/></label>
                            <% if(optional) {%>
                                <input type="checkbox" id="optional" name="optional"
                                       checked="checked" value=<%=optional%>/>
                            <% } else {%>
                                <input type="checkbox" id="optional" name="optional"
                                       value=<%=optional%>/>
                            <% } %>

                        </td>

                        <td>
                        </td>

                    </tr>
                </table>
        </tr>
        </td>
        <tr id="addExport">
            <td>
                <table>
                    <tr id="exportSymbolMax">
                        <td><fmt:message key="dataservices.output.mapping.export.option"/></td>
                        <td><input type="button" onclick="showExportOption()" value="+"></td>
                    </tr>
                    <tr id="exportSymbolMin" style="display:none">
                        <td><fmt:message key="dataservices.output.mapping.export.option"/></td>
                        <td><input type="button" onclick="showExportOption()" value="-"></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr id="exportTable" style="display:none">
            <td>
                <table>
                    <tr>
                        <td><fmt:message key="dataservices.output.mapping.export.name"/></td>
                        <td><% if (!exportName.equals("")) { %>
                            <input type="text" size="35" name="exportName" id="exportName"
                                   value="<%=exportName%>"/>
                            <% } else { %>
                            <input type="text" size="35" name="exportName" id="exportName"
                                   value="<%=exportName%>"/>
                            <% } %>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="dataservices.output.mapping.export.type"/></td>
                        <td><select id="exportType" name="exportType">
                            <% if (exportType.equals("") || exportType.equals("SCALAR")) { %>
                            <option value="SCALAR" selected="selected">SCALAR</option>
                            <% } else { %>
                            <option value="SCALAR">SCALAR</option>
                            <% } %>
                            <% if (exportType.equals("ARRAY")) { %>
                            <option value="ARRAY" selected="selected">ARRAY</option>
                            <% } else { %>
                            <option value="ARRAY">ARRAY</option>
                            <% } %>
                        </select></td>
                    </tr>
                </table>
    </table>
</div>
</td>
</tr>
<tr>
    <td class="middle-header"><fmt:message key="data.services.user.roles"/></td>
</tr>
<tr>
    <td>
        <div style="margin-bottom:20px;">
            <table class="styledInner" id="userRolesTab">
                <thead>
                <tr>
                    <th>User Roles</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (userRoles != null) {
                        boolean selectRole;

                        for (FlaggedName roles : userRoles) {
                            String roleName = null;
                            if (!CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equals(roles.getItemName())) {
                                roleName = roles.getItemName();
                            }
                            selectRole = false;
                            if (roleName != null) {
                %>
                <tr>
                    <td class="leftCol-med"><%=roleName%>
                    </td>
                    <td><%
                        if (selectedRoles.length != 0) {
                            for (String selectedRole : selectedRoles) {
                                if (selectedRole.equals(roleName)) {
                                    selectRole = true;
                                }
                            }
                        }
                        if (selectRole && roleName != null) {
                    %>
                        <input type="checkbox" id="<%=roleName%>" name="<%=roleName%>"
                               value="<%=roleName%>" checked="checked"/>
                        <%
                        } else {
                        %>
                        <input type="checkbox" id="<%=roleName%>" name="<%=roleName%>"
                               value="<%=roleName%>"/>
                        <%
                            }
                        %>
                    </td>
                </tr>
                <% }
                }
                }
                %>
                </tbody>
            </table>
        </div>
    </td>
</tr>

<tr>
    <td class="buttonRow" colspan="2">

        <input class="button" type="button" value="<fmt:message key="mainConfiguration"/>"
               onclick="redirectToMainConfiguration(document.getElementById('queryId').value)"/>

        <input class="button" type="submit" value="<fmt:message key="<%=caption%>"/>"
               onclick="document.outputMapping.action='OutputMappingProcessor.jsp?flag=<%=flag%>&edit=<%=edit%>&action=addbutton'
               ;return validateOutputMappingMandatoryFields();"/>

    </td>


</tr>


        <%if (mainQuery != null  ) {
	    if (mappingType.equals("") || mappingType.equals("complexType")) {
	        result = mainQuery.getResult();
	        isUseColomnNumbers = Boolean.parseBoolean(result.getUseColumnNumbers());
	        if (result.getRowName() != null) {
	        	
	            List<Element> elements;
	            List<Attribute> attributes;
	            List<CallQuery> callQueries;
	            List<ComplexElement> complexElements;
	            //root level Edit
	            if (result.getComplexElements() != null ) {
	            	ComplexElement complexElement = result.getComplexElement(complexElementName);
	            	if (complexElement != null && (complexPath == null || complexPath.equals(""))) {
	            		elements = complexElement.getElements();
		            	attributes = complexElement.getAttributes();
		            	callQueries = complexElement.getCallQueries();

		            	if (complexElementName != null && !complexElementName.equals("")) {
	            			complexPath = "/"+complexElementName;
	            		}
		            	complexElements = complexElement.getComplexElements();
	            	} else if ((complexElementName == null || complexElementName.equals("")) && complexMappingId.equals("complexType") ) {
	            		//root level editing a complex element; should display elements under tht complexElement
	            		complexElement = result.getChild(result.getComplexElements(), 
	            				getSubComplexElementList(complexPath));
	            		complexPath = getSubComplexPath(complexPath);
	            		if (complexPath.equals("")) {
	            			elements = result.getElements();
			            	attributes = result.getAttributes();
			            	callQueries =  result.getCallQueries();
			            	complexElements = result.getComplexElements();

	            		} else {
	            			elements = complexElement.getElements();
			            	attributes = complexElement.getAttributes();
			            	callQueries = complexElement.getCallQueries();
			            	complexElements = complexElement.getComplexElements();

	            		}
	            		
	            	} else if (complexPath != null && !complexPath.equals("")) {
	            		complexElement = result.getChild(complexPath+"/"+complexElementName);
	            		if (complexElementName != null && !complexElementName.equals("")) {
	            			complexPath = complexPath+"/"+complexElementName;
	            		}
	            	    if (complexElement == null) {
	            	    	complexElement = result.getChild(result.getComplexElements(), 
	            	    			getSubComplexElementList(complexPath));
	            	    	elements = complexElement.getElements();
			            	attributes = complexElement.getAttributes();
			            	callQueries = complexElement.getCallQueries();
			            	complexElements = complexElement.getComplexElements();

	            	    } else {
	            	    	elements = complexElement.getElements();
			            	attributes = complexElement.getAttributes();
			            	callQueries = complexElement.getCallQueries();
			            	complexElements = complexElement.getComplexElements();

	            	    }
		            	
		            } else {
		            	elements = result.getElements();
		            	attributes = result.getAttributes();
		            	callQueries =  result.getCallQueries();
		            	complexElements = result.getComplexElements();

		            }
	            } else {
	            	elements = result.getElements();
	            	attributes = result.getAttributes();
	            	callQueries =  result.getCallQueries();
	            	complexElements = result.getComplexElements();

	            }
	          
	            //if (elements.size() != 0 || attributes != null || complexElements != null ||  callQueries != null) {
	            if (elements.size() != 0 || attributes.size() != 0 || complexElements.size() != 0 ||  callQueries.size() != 0) {
	                %>
<tr>
    <td class="middle-header">Existing Output Mappings</td>
</tr>
<tr>
<td>
<table class="styledLeft" cellspacing="0" id="existingMappingsTable">
<thead>
<tr>
    <th><b><fmt:message key="dataservices.element.name"/></b></th>
    <th><b><fmt:message key="data.services.datasource.type"/></b></th>
     <% if (isUseColomnNumbers) { %>
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
<tbody>
<%
    if (elements != null) {
        Iterator itrElements = elements.iterator();
        if (itrElements.hasNext()) {
%>

<%
    }
    while (itrElements.hasNext()) {
        Element element = (Element) itrElements.next();
        String roles = "";
        String xType = "";
        String xportName = "";
        String xportType = "";
        String namespace = "";
        if (element.getRequiredRoles() != null) {
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
%>
<tr>
    <td><input type="hidden" id="<%=element.getName()%>" name="<%=element.getName()%>"
               value="<%=element.getName()%>"/><%=element.getName()%>
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
    </td>
    <% if ((complexPath != null && !complexPath.equals(""))) { %>
    <td>
        <a class="icon-link"
           style="background-image:url(../admin/images/edit.gif);"
           href="addOutputMapping.jsp?queryId=<%=queryId%>&name=<%=element.getName()%>&datasourceType=<%=element.getDataSourceType()%>&datasourceValue=<%=element.getDataSourceValue()%>&txtDataServiceElementNamespace=<%=namespace%>&requiredRoles=<%=roles%>&xsdType=<%=xType%>&exportName=<%=xportName%>&exportType=<%=xportType%>
           &edit=<%=element.getName()%>&complexPath=<%=complexPath%>&mappingType=element&optional=<%=optional%>&flag=<%=flag%>&arrayName=<%=(element.getArrayName()) == null ? "" : element.getArrayName()%>"><fmt:message
                key="edit"/></a>
        <a class="icon-link"
           style="background-image:url(../admin/images/delete.gif);"
           onclick="deleteComplexOutputMappings(document.getElementById('queryId').value,'<%=complexPath%>',
           document.getElementById('<%=element.getName()%>').value,'element')"
           href="#"><fmt:message
                key="delete"/></a>
    </td>
    <%} else {%>
    <td>
        <a class="icon-link"
           style="background-image:url(../admin/images/edit.gif);"
           href="addOutputMapping.jsp?queryId=<%=queryId%>&name=<%=element.getName()%>&datasourceType=<%=element.getDataSourceType()%>&txtDataServiceElementNamespace=<%=namespace%>&datasourceValue=<%=element.getDataSourceValue()%>&requiredRoles=<%=roles%>&xsdType=<%=xType%>&exportName=<%=xportName%>&exportType=<%=xportType%>
           &edit=<%=element.getName()%>&mappingType=element&optional=<%=optional%>&flag=<%=flag%>&arrayName=<%=(element.getArrayName()) == null ? "" : element.getArrayName()%>"><fmt:message
                key="edit"/></a>
        <a class="icon-link"
           style="background-image:url(../admin/images/delete.gif);"
           onclick="deleteOutputMappings(document.getElementById('queryId').value,
           document.getElementById('<%=element.getName()%>').value,'element')"
           href="#"><fmt:message
                key="delete"/></a>
    </td>
    <%} %>
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
            if (attribute.getRequiredRoles() != null) {
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
    <td>attribute
    </td>
    <td><%=roles%>
    </td>
    <td><%=xType%>
    </td>
    <% if (complexPath != null && !complexPath.equals("")) { %>
    <td>
        <a class="icon-link"
           style="background-image:url(../admin/images/edit.gif);"
           href="addOutputMapping.jsp?queryId=<%=queryId%>&name=<%=attribute.getName()%>&datasourceType=<%=attribute.getDataSourceType()%>&datasourceValue=<%=attribute.getDataSourceValue()%>&requiredRoles=<%=roles%>&xsdType=<%=xType%>&exportName=<%=xportName%>&exportType=<%=xportType%>
           &edit=<%=attribute.getName()%>&complexPath=<%=complexPath%>&mappingType=attribute&optional=<%=optional%>&flag=<%=flag%>&arrayName=<%=attribute.getArrayName()%>"><fmt:message
                key="edit"/></a>
        <a class="icon-link"
           style="background-image:url(../admin/images/delete.gif);"
           onclick="deleteComplexOutputMappings(document.getElementById('queryId').value,'<%=complexPath%>',
           document.getElementById('<%=attribute.getName()%>').value,'attribute')"
           href="#"><fmt:message key="delete"/></a>
    </td>
    <%} else {%>
    <td>
        <a class="icon-link"
           style="background-image:url(../admin/images/edit.gif);"
           href="addOutputMapping.jsp?queryId=<%=queryId%>&name=<%=attribute.getName()%>&datasourceType=<%=attribute.getDataSourceType()%>&datasourceValue=<%=attribute.getDataSourceValue()%>&edit=<%=attribute.getName()%>&requiredRoles=<%=roles%>
           &xsdType=<%=xType%>&exportName=<%=xportName%>&exportType=<%=xportType%>&mappingType=attribute&optional=<%=optional%>&flag=<%=flag%>&arrayName=<%=attribute.getArrayName()%>"><fmt:message
                key="edit"/></a>
        <a class="icon-link"
           style="background-image:url(../admin/images/delete.gif);"
           onclick="deleteOutputMappings(document.getElementById('queryId').value,
           document.getElementById('<%=attribute.getName()%>').value,'attribute')"
           href="#"><fmt:message key="delete"/></a>
    </td>
    <%} %>

</tr>
<%
        }
    }
    if (complexElements != null) {
        Iterator itrComplexElements = complexElements.iterator();
        if (itrComplexElements.hasNext()) {
%>
<% if (!elements.iterator().hasNext()) {%>
<tr>
    <td class="middle-header">Existing Output Mappings</td>
</tr>
<tr>
    <td>
        <table class="styledLeft" cellspacing="0" id="existingMappingsTable">
            <thead>
            <tr>
                <th colspan="3"><b><fmt:message key="complex.element"/></b></th>
                <th colspan="3"><b><fmt:message key="dataservice.element.namespace"/></b></th>
                <th><b><fmt:message key="actions1"/></b></th>
            </tr>
            </thead>
            <% } else { %>
            <tbody>

            <tr>
                <td colspan="7"></td>
            </tr>
            <tr>
                <td colspan="3"><b><fmt:message key="complex.element"/></b></td>
                <td colspan="3"><b><fmt:message key="dataservice.element.namespace"/></b></td>
                <td><b><fmt:message key="actions1"/></b></td>
            </tr>
            <% } %>
            <%
                }
                while (itrComplexElements.hasNext()) {
                    ComplexElement complexElement = (ComplexElement) itrComplexElements.next();
                    String elementNameSpace = "";
                    if (complexElement.getNamespace() != null) {
                        elementNameSpace = complexElement.getNamespace();
                    } else {
                        elementNameSpace = "N/A";
                    }
            %>
            <tr>
                <input type="hidden" id="<%=complexElement.getName()%>"
                       name="<%=complexElement.getName()%>"
                       value="<%=complexElement.getName()%>"/>
                <td colspan="3"><%=complexElement.getName()%>
                </td>
                <td colspan="3"><%=elementNameSpace%>
                </td>

                <td>
                    <a class="icon-link"
                       style="background-image:url(../admin/images/edit.gif);"
                       href="addOutputMapping.jsp?queryId=<%=queryId%>&txtDataServiceComplexElementName=<%=complexElement.getName()%>&txtDataServiceComplexElementNamespace=<%=complexElement.getNamespace()%>&edit=<%=complexElement.getName()%>&complexPath=<%=complexPath%>&mappingType=complexType&flag=<%=flag%>&arrayName=<%=complexElement.getArrayName()%>"><fmt:message
                            key="edit"/></a>
                    <a class="icon-link"
                       style="background-image:url(../admin/images/delete.gif);"
                       onclick="deleteComplexOutputMappings(document.getElementById('queryId').value,'<%=complexPath%>',
           document.getElementById('<%=complexElement.getName()%>').value,'complexType')"
                       href="#"><fmt:message key="delete"/></a>
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
                    if (callQuery.getRequiredRoles() != null) {
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
                <% if (complexPath != null && !complexPath.equals("")) { %>
                <td>

                    <a class="icon-link"
                       style="background-image:url(../admin/images/edit.gif);"
                       href="addOutputMapping.jsp?queryId=<%=queryId%>&complexPath=<%=complexPath%>&selectedQuery=<%=callQuery.getHref()%>&edit=<%=callQuery.getHref()%>&requiredRoles=<%=roles%>&mappingType=query&flag=edit"><fmt:message
                            key="edit"/></a>
                    <a class="icon-link"
                       style="background-image:url(../admin/images/delete.gif);"
                       onclick="deleteComplexOutputMappings(document.getElementById('queryId').value,'<%=complexPath%>',
           document.getElementById('<%=callQuery.getHref()%>').value,'query')"
                       href="#"><fmt:message
                            key="delete"/></a>
                </td>
                <%} else {%>
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
                <%} %>

            </tr>
            <%
                                    }
                                }
                            }
                        }
                    }
                }

            %>

            </tbody>
        </table>

<tr>
    <td>
    </td>
</tr>

</tbody>
</table>
</form>
</div>
</div>
<script type="text/javascript">
    alternateTableRows('existingMappingsTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('queryMappingParameter', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('userRolesTab', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>