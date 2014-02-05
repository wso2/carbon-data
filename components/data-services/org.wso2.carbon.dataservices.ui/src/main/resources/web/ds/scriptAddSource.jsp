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
<%@page import="org.wso2.carbon.dataservices.common.DBConstants"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.CarbonError" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.apache.axis2.AxisFault"%>
<%@ page import="org.wso2.carbon.dataservices.ui.DataServiceAdminClient" %>
<carbon:breadcrumb
        label="datasource.details"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<%
    session.setAttribute("selectedTables", ""); // when adding a new service generation, remove previously set tables.
    session.setAttribute("flag","");//// when adding a new service generation, remove previously set select all & select none flags..
    session.setAttribute("datasource", "");
    session.setAttribute("dbName", "");
    session.setAttribute("schemaFlag","");
    session.setAttribute("totalSchemaList","");
    session.setAttribute("schemaList", "");
    String sourceId = (String)session.getAttribute("datasource");
    String[] sourceList = null;
    String datasource = request.getParameter("datasource");
    String dbName = request.getParameter("dbName");
    dbName = (dbName == null) ? "" : dbName;
    try{
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =(ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);
        DataServiceAdminClient client = new DataServiceAdminClient(cookie,backendServerURL,configContext);
        String[] types = {DBConstants.DataSourceTypes.RDBMS};
        sourceList = client.getCarbonDataSourceNamesForTypes(types);
    }catch(AxisFault e){
        CarbonError carbonError = new CarbonError();
        carbonError.addError("Error occurred while saving data service configuration.");
        request.setAttribute(CarbonError.ID, carbonError);
 	 
 	     }
 	  %>
<script type="text/javascript" src="js/ui-validations.js"></script>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">

    <div id="middle">
        <h2><fmt:message key="select.the.source"/></h2>

        <div id="workArea">
            <form action="scriptViewSchemas.jsp?ordinal=1" method="post" onsubmit="return validateDatabaseSelection();">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="select.carbon.dataSource"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td>
                            <table class="normal">
                                <tr>
                                    <td><fmt:message key="available.carbon.source.names"/><font color="red">*</font></td>
                                    <td>
                                        <select name="datasource" id="datasource">
                                           <option value="" selected="selected">--SELECT--</option>
                                            <% if (sourceList != null) {%>
                                            <% for (String name : sourceList) {%>
                                              <%  if(name.equals(datasource)) { %>
                                                     <option  value=<%= name%> selected><%= name%> </option>
                                                  <%} else {%>
                                                        <option  value=<%= name%>><%= name%> </option>
                                                   <%}%>
                                            <% } %>

                                            <% } else { %>
                                             <font color="red"><fmt:message key="empty.source"/></font>
                                            <% } %>
                                        </select>
                                    </td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="name.database"/><font color="red">*</font>
                                    </td>
                                    <td><% if(!dbName.equals("")){ %>
				                        <input type="text" size="35" name="dbName" id="dbName" value="<%=dbName%>" />
				                        <% }else{ %>
				                        <input type="text" size="35" name="dbName" id="dbName" value="<%=dbName%>"/>
				                        <% } %>
				        			</td>
                                </tr>
                                <tr>
                                    <td class="buttonRow">
                                        <input class="button" type="submit" value="<fmt:message key="next"/> >"/>
                                        <input class="button" type="button" value="<fmt:message key="cancel"/>"
										onclick="location.href = '../service-mgt/index.jsp'" />
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>    