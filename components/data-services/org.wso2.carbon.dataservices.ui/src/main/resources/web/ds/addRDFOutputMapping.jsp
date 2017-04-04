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
 <%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.*" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminClient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName" %>
<%@ page import="java.util.StringTokenizer" %>
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
<div id="middle">
<h2>Add/Edit Output Mapping</h2>

<div id="workArea">
<%  String edit = request.getParameter("edit");
    edit = (edit == null) ? "" : edit;
%>

<form method="post" action="OutputMappingProcessor.jsp?flag=outputMapping&edit=<%=edit+"rdf"%>" id="outputMapping"
      name="outputMapping"
      onsubmit="return validatRDFeOutputMappingMandatoryFields();">
<table class="styledLeft" id="outputmappingTable">
<%
    String queryId = request.getParameter("queryId");

    if (queryId != null) {
        Config c = dataService.getConfig(dataService.getQuery(queryId).getConfigToUse());
    }
    String rowelement = null;
    String rowName = null;
    String nameSpace = null;
    String datasourceType = request.getParameter("datasourceType");
    String datasourceValue = request.getParameter("datasourceValue");
    String name = request.getParameter("name");
    String mappingType = request.getParameter("mappingType");
    String selectedQuery = request.getParameter("selectedQuery");
    String flag = request.getParameter("flag");
    String xsdType = request.getParameter("xsdType");
    String requiredRoles = request.getParameter("requiredRoles");
    String rdfRefURI = request.getParameter("rdfRefURI");
    String exportName = request.getParameter("exportName");
    String exportType = request.getParameter("exportType");
    String caption;
    Result result;
    
    Query query = new Query();
    Query mainQuery = dataService.getQuery(queryId);
    datasourceType = (datasourceType == null) ? "column" : datasourceType;
    selectedQuery = (selectedQuery == null) ? "" : selectedQuery;

    if(!selectedQuery.equals("")){
        query = dataService.getQuery(selectedQuery);
    }

    if(mainQuery != null){
        result = mainQuery.getResult();
        if (result != null) {
            rowName = result.getRowName();
            rowelement = result.getResultWrapper();
            nameSpace = result.getNamespace();
        }
    }

    datasourceValue = (datasourceValue == null) ? "" : datasourceValue;
    rdfRefURI = (rdfRefURI == null) ? "" : rdfRefURI;
    exportName = (exportName == null) ? "" : exportName;
    exportType = (exportType == null) ? "SCALAR" : exportType;
    queryId = (query == null) ? "" : queryId;
    rowelement = (rowelement == null) ? "" : rowelement;
    rowName = (rowName == null) ? "" : rowName;
    nameSpace = (nameSpace == null) ? "" : nameSpace;
    name = (name == null) ? "" : name;
    mappingType = (mappingType == null) ? "" : mappingType;
    flag = (flag == null) ? "add" : flag;
    xsdType = (xsdType == null ) ? "" : xsdType;
    requiredRoles = (requiredRoles == null) ? "" : requiredRoles;
    ArrayList<Query> queries = dataService.getQueries();
    UserAdminClient client;

    FlaggedName[] userRoles = null;
    if(mappingType.equals("")){
        caption = "add";
    }else{
        caption = "save";
    }
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    try {
        client = new UserAdminClient(cookie, backendServerURL, configContext);
        userRoles = client.getAllRolesNames("*", -1);
    } catch ( Exception e) {
        e.printStackTrace();
    }
    if(requiredRoles.equals("N/A")){
        requiredRoles = "";
    }
    // breaking comma seperated roles
    String[] selectedRoles= requiredRoles.split(",");

%>
<tbody>

<tr>
    <td class="middle-header">
        <input value="<%=queryId%>" name="queryId" id="queryId" size="30" type="hidden"/>
<input value="<%=rowelement%>" name="txtDataServiceWrapElement" id="txtDataServiceWrapElement" size="30" type="hidden"/>
<input value="<%=rowName%>" name="txtDataServiceRowName" id="txtDataServiceRowName" size="30" type="hidden"/>
<input value="<%=nameSpace%>" name="txtDataServiceRDFRowNamespace" id="txtDataServiceRDFRowNamespace" size="30"
       type="hidden"/>
<input value="<%=mappingType%>" id="mappingType" name="mappingType" type="hidden"/>
<input value="<%=requiredRoles%>" id="requiredRoles" name="requiredRoles" type="hidden"/>
        Output Mappings
    </td>
</tr>
<tr><td>
<table class="normal">
            <tr>
            <td>  
  		     <table>
    		  <tr>
                <td class="leftCol-med"><fmt:message key="data.services.mapping.type"/><font color="red">*</font>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                </td>
                <td><select onchange="changeToNextRDFMapping(this, document);return false;" name="cmbDataServiceOMType"
                            id="cmbDataServiceOMType">
                    <% if (mappingType.equals("")) { %>
                    <option selected="selected" value="">--Select--</option>
                    <% }else{ %>
                    <option value="">--Select--</option>
                    <% }if (mappingType.equals("element")) { %>
                    <option selected="true" value="element">element</option>
                    <% }else{ %>
                    <option value="element">element</option>
                    <% }if (mappingType.equals("resource")) { %>
                    <option selected="selected" value="resource">resource</option>
                    <% }else{ %>
                    <option value="resource">resource</option>
                    <% } %>
                </select></td>
            </tr>
             
              </table>
              </td>
             </tr>
        </table>


<div id="resourceRow" style="<%=mappingType.equals("resource") ? "" : "display:none"%>">
        <table class="normal">
            <tr>
                <td class="leftCol-med"><fmt:message key="dataservices.resource.uri"/><font color="red">*</font>
                </td>
                 <td>
                    <input value="<%=rdfRefURI%>" id="txtrdfRefURI" name="txtrdfRefURI" size="30" type="text"></td>
            </tr>
            <tr>
                <td class="leftCol-med"><fmt:message key="dataservice.resource.mapping.field.name"/></td>
                <td><input value="<%=name%>" id="txtDataServiceResourceName" name="txtDataServiceResourceName"
                           size="30" type="text"></td>
            </tr>
        </table>
</div>
<div id="omElementRowId" style="<%=(mappingType.equals("query")) ? "display:none":""%>">
   
        <table class="normal" style="<%=rdfRefURI == null || rdfRefURI.equals("") ? "" : "display:none"%>">
        <tr>
        <td>  
  		 <table>
             <tr>
             <td class="leftCol-med"><fmt:message key="data.services.datasource.type"/><font color="red">*</font>
				<td><select onchange="changeToDataSourceType(this, document);return false;" id="datasourceTypeId" name="datasourceType" >
				        <% if(datasourceType.equals("") || datasourceType.equals("column")){ %>
				        <option value="column" selected="selected">column</option>
				        <% }else{ %>
				        <option value="column">column</option>
				        <% } %>
				        <% if(datasourceType.equals("query-param")) { %>
				        <option value="query-param" selected="selected">query-param</option>
				        <% } else { %>
				        <option value="query-param">query-param</option>
				        <% } %>
				        </select> </td>   
            </tr>
              <tr   id="columnRow" style="<%=datasourceType.equals("column") ? "" : "display:none"%>">
                <td class="leftCol-med"><fmt:message key="dataservice.datasource.column.name"/></td>
                <td><input value="<%=datasourceValue%>" id="datasourceValue" name="datasourceValue"
                           size="30" type="text"></td>
            </tr>
             <tr   id="queryParamnRow" style="<%=datasourceType.equals("query-param") ? "" : "display:none"%>">
                <td class="leftCol-med"><fmt:message key="dataservice.datasource.query.param.name"/></td>
                <td><input value="<%=datasourceValue%>" id="datasourceValue" name="datasourceValue"
                           size="30" type="text"></td>
            </tr>
            <tr>
                <td class="leftCol-med"><fmt:message key="dataservice.output.field.name"/></td>
                <td><input value="<%=name%>" id="txtDataServiceOMElementName" name="txtDataServiceOMElementName"
                           size="30" type="text"></td>
            </tr>
           <tr><td class="leftCol-med"><fmt:message key="data.services.xsdType"/></td><td>
                <select id="xsdType" name="xsdType">
                    <% if (xsdType.equals("")) { %>
                    <option value="" selected="selected">--SELECT--</option>
                    <% } else { %>--%>
                    <option value="">--SELECT--</option>
                    <% } if (xsdType.equals("string")) { %>
                    <option value="string" selected="selected">string</option>
                    <% } else { %>
                    <option value="string">string</option>
                    <% } if (xsdType.equals("integer")) { %>
                    <option value="integer" selected="selected">integer</option>
                    <% } else { %>
                    <option value="integer">integer</option>
                    <% } if (xsdType.equals("boolean")) { %>
                    <option value="boolean" selected="selected">boolean</option>
                    <% } else { %>
                    <option value="boolean">boolean</option>
                    <% } if (xsdType.equals("float")) { %>
                    <option value="float" selected="selected">float</option>
                    <% } else { %>
                    <option value="float">float</option>
                    <% } if (xsdType.equals("double")) { %>
                    <option value="double" selected="selected">double</option>
                    <% } else{ %>
                    <option value="double">double</option>
                    <% } if (xsdType.equals("decimal")) { %>
                    <option value="decimal" selected="selected">decimal</option>
                    <% } else { %>
                    <option value="decimal">decimal</option>
                    <% } if (xsdType.equals("dateTime")) { %>
                    <option value="dateTime" selected="selected">dateTime</option>
                    <% } else { %>
                    <option value="dateTime">dateTime</option>
                    <% } if (xsdType.equals("time")) { %>
                    <option value="time" selected="selected">time</option>
                    <% } else { %>
                    <option value="time">time</option>
                    <% } if (xsdType.equals("date")) { %>
                    <option value="date" selected="selected">date</option>
                    <% } else { %>
                    <option value="date">date</option>
                    <% } if (xsdType.equals("long")) { %>
                    <option value="long" selected="selected">long</option>
                    <% } else { %>
                    <option value="long">long</option>
                    <% } if (xsdType.equals("base64Binary")) { %>
                    <option value="base64Binary" selected="selected">base64Binary</option>
                    <% } else { %>
                    <option value="base64Binary">base64Binary</option>
                    <% } %>
                 </select></td></tr> 
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
				                <td ><fmt:message key="dataservices.output.mapping.export.option"/></td>
				                <td><input type="button" onclick="showExportOption()" value="-"></td>
				            </tr>
				        </table>
				     </td>
				</tr>
				<tr id="exportTable" style="display:none">
				    <td>
				      <table>
				       <tr>
						<td><fmt:message key="dataservices.output.mapping.export.name" /></td>
						<td><% if(!exportName.equals("")){ %>
				                        <input type="text" size="35" name="exportName" id="exportName" value="<%=exportName%>" />
				                        <% }else{ %>
				                        <input type="text" size="35" name="exportName" id="exportName" value="<%=exportName%>"/>
				                        <% } %>
				        </td>
				       </tr>
					   <tr>
						<td><fmt:message key="dataservices.output.mapping.export.type" /></td>
						 <td><select id="exportType" name="exportType" >
							<% if(exportType.equals("") || exportType.equals("SCALAR")){ %>
							<option value="SCALAR" selected="selected">SCALAR</option>
							<% }else{ %>
							<option value="SCALAR">SCALAR</option>
							<% } %>
							<% if(exportType.equals("ARRAY")) { %>
							<option value="ARRAY" selected="selected">ARRAY</option>
							<% } else { %>
							<option value="ARRAY">ARRAY</option>
							<% } %>
							</select> </td>
					  </tr>
				    </table>
        </table>
</div>
</td>
</tr>
<tr>
    <td class="middle-header"><fmt:message key="data.services.user.roles"/></td>
</tr>
<tr><td>
<div style="margin-bottom:20px;">
        <table class="styledInner" id="userRolesTab">
            <thead>
                <tr>
                    <th>User Roles</th><th></th>
                </tr>
            </thead>
           <tbody>
<%
    if(userRoles != null){
        boolean selectRole;

        for (FlaggedName roles : userRoles){
            String roleName = null;
            if(!CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equals(roles.getItemName())) {
                roleName = roles.getItemName();
            }
            selectRole = false;
            if(roleName != null) {
%>                     <tr>
                <td class="leftCol-med"><%=roleName%></td>
                <td><%
                        if(selectedRoles.length != 0){
                           for(String selectedRole : selectedRoles){
                                if(selectedRole.equals(roleName)){
                                    selectRole = true;
                                }
                           }
                        }
                        if (selectRole && roleName != null) {
                    %>
                     <input type="checkbox" id="<%=roleName%>" name="<%=roleName%>"
                            value="<%=roleName%>" checked="checked" />
                    <%
                        } else {
                    %>
                    <input type="checkbox" id="<%=roleName%>" name="<%=roleName%>" value="<%=roleName%>" />
                    <%
                        }
                    %>
                </td>
            </tr>
<%         }
        }
    }
%>
            </tbody>
        </table>
    </div>
</td>
</tr>
<tr>
    <td class="middle-header">Existing Output Mappings</td>
</tr>
<tr><td>
<table class="styledLeft" cellspacing="0" id="existingMappingsTable">
<% if (mainQuery != null) {
    if (mappingType.equals("")) {
        result = mainQuery.getResult();
        if (result.getRowName() != null) {
            List<Element> elements = result.getElements();
            List<RDFResource> resources = result.getResources();
            List<Attribute> attributes = result.getAttributes();
            List<CallQuery> callQueries = result.getCallQueries();
            if (elements.size() != 0 || resources.size() !=0) {
%>

    <thead>
    <tr>
        <th><b><fmt:message key="dataservices.element.name"/></b></th>
        <th><b><fmt:message key="data.services.datasource.type"/></b></th>
        <th><b><fmt:message key="dataservice.datasource.column.name"/></b></th>
        <th><b><fmt:message key="data.services.mapping.type"/></b></th>
        <th><b><fmt:message key="data.services.user.roles"/></b></th>
        <th><b><fmt:message key="data.services.xsdType"/></b></th>
        <th><b><fmt:message key="dataservices.output.mapping.export.name"/></b></th>
        <th><b><fmt:message key="dataservices.output.mapping.export.type"/></b></th>
        <th><b><fmt:message key="actions1"/></b></th>
    </tr>
    </thead>
    <tbody>
    <%

    if (elements != null) {
                    Iterator itrElements = elements.iterator();
                    if (itrElements.hasNext()) {
    }
    while (itrElements.hasNext()) {
        Element element = (Element) itrElements.next();
        String roles = "";
        String xType = "";
        String xportName ="";
        String xportType ="";
        if(element.getRequiredRoles() != null ){
           roles = element.getRequiredRoles();
        }else{
            roles = "N/A";
        }
        if (element.getExport() != null) {
        	xportName = element.getExport();
        } else {
        	 xportName = "";
        }
        if (element.getExportType() != null) {
        	xportType = element.getExportType();
        }else{
        	xportType = "";
        }
        if(element.getXsdType() != null){
            xType = element.getXsdType();
        }
%>
<tr>
    <td> <input type="hidden" id="<%=element.getName()%>" name="<%=element.getName()%>"
                value="<%=element.getName()%>" /><%=element.getName()%>
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
    <td><%=xportName%>
    </td>
    <td><%=xportType%>
    </td>     
    <td>
        <a class="icon-link"
           style="background-image:url(../admin/images/edit.gif);"
           href="addRDFOutputMapping.jsp?queryId=<%=queryId%>&name=<%=element.getName()%>&datasourceType=<%=element.getDataSourceType()%>&datasourceValue=<%=element.getDataSourceValue()%>&requiredRoles=<%=roles%>&xsdType=<%=xType%>&exportName=<%=xportName%>&exportType=<%=xportType%>
           &edit=<%=element.getName()%>&mappingType=element&flag=<%=flag+"rdf"%>"><fmt:message
                key="edit"/></a>
        <a class="icon-link"
           style="background-image:url(../admin/images/delete.gif);"
           onclick="deleteRDFOutputMappings(document.getElementById('queryId').value,
           document.getElementById('<%=element.getName()%>').value,'element')"
           href="#"><fmt:message
                key="delete"/></a>
    </td>
</tr>

<%
        }
    }
    if (resources != null) {
        Iterator itrResources = resources.iterator();
        while (itrResources.hasNext()) {
            RDFResource resource = (RDFResource) itrResources.next();
            String roles = "";
            String xType = "";
            if(resource.getRequiredRoles() != null){
               roles = resource.getRequiredRoles();
            }else{
                roles = "N/A";
            }
            if(resource.getXsdType() != null){
                xType = resource.getXsdType();
            }
%>
<tr>
    <input type="hidden" id="<%=resource.getName()%>" name="<%=resource.getName()%>"
           value="<%=resource.getName()%>" />
    <td><%=resource.getName()%>
    </td>
    <td>rdf-ref-uri
    </td>
    <td><%=resource.getRdfRefURI()%>
    </td>
    <td>resource
    </td>
    <td><%=roles%>
    </td>
    <td><%=xType%>        
    </td>
    </td>
    <td>
    </td>
    <td>
    </td>
    <td>
        <a class="icon-link"
           style="background-image:url(../admin/images/edit.gif);"
           href="addRDFOutputMapping.jsp?queryId=<%=queryId%>&name=<%=resource.getName()%>
           &rdfRefURI=<%=resource.getRdfRefURI()%>&edit=<%=resource.getName()%>&requiredRoles=<%=roles%>
           &xsdType=<%=xType%>&mappingType=resource&flag=<%=flag+"rdf"%>"><fmt:message
                key="edit"/></a>
        <a class="icon-link"
           style="background-image:url(../admin/images/delete.gif);"
           onclick="deleteRDFOutputMappings(document.getElementById('queryId').value,
           document.getElementById('<%=resource.getName()%>').value,'resource')"
           href="#"><fmt:message
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
    <td colspan="4"></td>
</tr>
<tr>
    <td colspan="3"><b><fmt:message key="query.id"/></b></td>
    <td><b><fmt:message key="data.services.user.roles"/></b></td>
    <td><b><fmt:message key="actions1"/></b></td>
</tr>
<%
    }
    while (itrCallQueries.hasNext()) {
        CallQuery callQuery = (CallQuery) itrCallQueries.next();
        String roles = "";
        if(callQuery.getRequiredRoles() != null){
            roles = callQuery.getRequiredRoles();
        }else{
            roles = "N/A";
        }
%>
<tr>
    <input type="hidden" id="<%=callQuery.getHref()%>" name="<%=callQuery.getHref()%>"
           value="<%=callQuery.getHref()%>" />
    <td colspan="3"><%=callQuery.getHref()%>
    </td>
    <td><%=roles%>
    </td>
    <td>
        <a class="icon-link"
           style="background-image:url(../admin/images/edit.gif);"
           href="addRDFOutputMapping.jsp?queryId=<%=queryId%>&selectedQuery=<%=callQuery.getHref()%>
           &edit=<%=callQuery.getHref()%>&requiredRoles=<%=roles%>&mappingType=query&flag=edit"><fmt:message
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
        }
    }
%>
</tbody>
</table>


</td>
</tr>
<%
   if (caption.equals("add")){
	   flag= flag+"rdf";
   } 

	   
%>
<tr>
    <td class="buttonRow" colspan="2">
        <input class="button" type="button" value="<fmt:message key="mainConfiguration"/>"
               onclick="redirectToMainConfiguration(document.getElementById('queryId').value)"/>
        <input class="button" type="submit" value="<fmt:message key="<%=caption%>"/>"
               onclick="document.outputMapping.action = 'OutputMappingProcessor.jsp?flag=<%=flag%>&edit=<%=edit%>'"/>
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