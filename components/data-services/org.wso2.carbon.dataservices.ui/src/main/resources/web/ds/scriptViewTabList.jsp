<%--
~ /*
~  *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~  *
~  *  WSO2 Inc. licenses this file to you under the Apache License,
~  *  Version 2.0 (the "License"); you may not use this file except
~  *  in compliance with the License.
~  *  You may obtain a copy of the License at
~  *
~  *  http://www.apache.org/licenses/LICENSE-2.0
~  *
~  *  Unless required by applicable law or agreed to in writing,
~  *  software distributed under the License is distributed on an
~  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~  *  KIND, either express or implied.  See the License for the
~  *  specific language governing permissions and limitations
~  *  under the License.
~  *
~  */
--%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.CarbonError"%>
<%@ page import="org.wso2.carbon.dataservices.ui.DataServiceAdminClient"%>
<%@ page import="org.wso2.carbon.dataservices.ui.UIutils"%>
<%@ page import="org.wso2.carbon.dataservices.ui.stub.admin.core.xsd.PaginatedTableInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.owasp.encoder.Encode" %>
<carbon:breadcrumb
        label="Select the tables"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
 <script type="text/javascript">

     function get_check_tableList() {
        var maxChecked =0;
        var selectedTables ="";
         if (document.getElementById("tableList")) {
             for (var i=0; i < document.dataForm.tableList.length; i++) {
               if (document.dataForm.tableList[i].checked) {
                       selectedTables=selectedTables+":" +document.dataForm.tableList[i].value;
               }
            }
         }
        return selectedTables;
     }

     function set_check_tableList(obj){
         var checkstate = false;
        if (obj.checked){
             checkstate = true;
        }
        jQuery.ajax({
        data: "checkedValue="+obj.value+"&checked="+checkstate,
        url: "setSession.jsp",
        context: document.body,
        success: function(){

        }
        });
    }

     var allServicesSelected = false;
    function selectAll(isSelected) {
        allServicesSelected = false;
        if (document.dataForm.tableList != null &&
            document.dataForm.tableList[0] != null) { // there is more than 1 service
            if (isSelected) {
                for (var j = 0; j < document.dataForm.tableList.length; j++) {
                    document.dataForm.tableList[j].checked = true;
                }
            } else {
                for (j = 0; j < document.dataForm.tableList.length; j++) {
                    document.dataForm.tableList[j].checked = false;
                }
            }
        } else if (document.dataForm.tableList != null) { // only 1 service
            document.dataForm.tableList.checked = isSelected;
        }
        //return false;
        var flag = "";
        if (isSelected) {
            flag = "selectAllTables";
        } else {
            flag = "selectNoneTables";
        }
        jQuery.ajax({
        data: "flag="+flag,
        url: "setSession.jsp",
        context: document.body,
        success: function(){

        }
        });
    }
</script>
<%
     String[] schemaList = null;
	 boolean isBack = request.getParameter("flag") != null;
     String tableNames="";
     String sourceId = (String)session.getAttribute("datasource");
     String dbName  = (String)session.getAttribute("dbName");
     String parameters = "";
     String[]tableList = null;
     String tableListSession[] = null;
    if (session.getAttribute("selectedTables") != null && !session.getAttribute("selectedTables").equals("")) {
       tableListSession = session.getAttribute("selectedTables").toString().split(":");
    }
     String[]totalSchemaList = null;
     String[]totalSchemaListNew = null;

    String pageNumberStr = request.getParameter("pageNumber");
    if (pageNumberStr == null) {
        pageNumberStr = "0";
    }
    int pageNumber = 0;
    int numberOfPages = 0;
    try {
        pageNumber = Integer.parseInt(pageNumberStr);
    } catch (NumberFormatException ignored) {
        // page number format exception
    }
    PaginatedTableInfo paginatedTableInfo;

    //selectedTableList = (String[])session.getAttribute("tableList");
     if ((isBack)) {
         
    	   tableListSession = (String[])session.getAttribute("tableList");
    	   schemaList = (String[])session.getAttribute("schemaList");
    	   String[] newschemaList = request.getParameterValues("schemaList");

         String schemaName = request.getParameter("schemaName"); // schema name has provided through text field.
             if (schemaName != null && !schemaName.equals("")) {
                 newschemaList[0] = schemaName.trim() ;
             }

    	   // check if schema is changed by going back
    	   boolean isChanged = true;
    	   if( (newschemaList != null ) && (schemaList.length == newschemaList.length)) {
    		   for (int i = 0; i< schemaList.length; i++) {
    			   if(schemaList[i].equals(newschemaList[i])) {
    				   isChanged = false;
    			   }
    		   }
    	   }
    	   // if direction is comming from service mode
    	   if (request.getParameter("flag").equals("back")) {
    		   isChanged = false;
    	   }
    	   //if schema is changed
    	   if (isChanged) {
    		   tableListSession = null;
    		   schemaList = newschemaList;
               session.setAttribute("schemaList", schemaList);
    	   }

    } else {
         schemaList = request.getParameterValues("schemaList");
         if (schemaList == null && session.getAttribute("schemaList") != null && !session.getAttribute("schemaList").equals("")) {   //click paginate next
             schemaList = (String[]) session.getAttribute("schemaList");
         }
         String schemaName = request.getParameter("schemaName"); // schema name has provided through text field.
         if (schemaName != null && !schemaName.equals("")) {
             schemaList[0] = schemaName.trim() ;
         }
         session.setAttribute("schemaList", schemaList);
    }

    try {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);
        DataServiceAdminClient client = new DataServiceAdminClient(cookie, backendServerURL, configContext);
        paginatedTableInfo = client.getPaginatedTableInfo(pageNumber, sourceId, dbName, schemaList);
        totalSchemaList = paginatedTableInfo.getTableInfo();
        totalSchemaListNew = client.getTableInfo(sourceId, dbName, schemaList);
        numberOfPages = paginatedTableInfo.getNumberOfPages();
        session.setAttribute("TotalList", totalSchemaListNew);
        //session.setAttribute("selectedTables",totalSchemaListNew); //initially all tables are selected by default.
    } catch (Exception e) {
        CarbonError carbonError = new CarbonError();
        carbonError.addError("Error occurred while saving data service configuration.");
        request.setAttribute(CarbonError.ID, carbonError);
    }


%>
<!-- logic to have multiple pages -->
 <carbon:paginator pageNumber="<%=1%>" numberOfPages="<%=1%>"
                  page="scriptViewTabList.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%=parameters%>"/>

<script type="text/javascript" src="js/ui-validations.js"></script>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">

    <div id="middle">
        <h2>Customize Service Generation</h2>
        <form action="scriptServiceGeneratingMode.jsp" method="post" name="dataForm">
           <div id="workArea">
               <table>
                <tr>
                <td>
                    <a href="#" onclick="selectAll(true);return false;"  style="cursor:pointer">Select all</a>&nbsp<b>|</b>&nbsp;
                </td>
                <td>
                    <a href="#" onclick="selectAll(false);return false; "  style="cursor:pointer">Select none</a>
                </td>
                </tr>
                </table>
                <table class="styledLeft">
                    <thead>
                        <tr>
                            <th colspan="2">Select Table(s)</th>
                        </tr>
                        </thead>
                        <%     if (totalSchemaListNew!= null){%>
                        <tr><td>
                            <table class="normal">
                                <div style="overflow: auto; height: 10px; width: 10%;">
                                    <%
                                        int columns = 3;
                                        int rows = totalSchemaList.length / columns + 1;
                                        int y = 0;
                                        int x = totalSchemaList.length;

                                        for (int i=0; i < rows ; i++){
                                            int count=0;
                                            for (int j=0; j < columns; j++) {
                                                boolean isSelected = false;
                                                tableNames = tableNames + totalSchemaList[y] + ":";
                                                if (tableListSession != null){
                                                    for (String sessionTableName:tableListSession) {
                                                        if(sessionTableName.equals(totalSchemaList[y])) {
                                                             isSelected = true;
                                                         }
                                                    }
                                                    if (isSelected) {
                                            %>
                                            <td></td>
                                            <td></td>
                                            <td><input type="checkbox" id="<%= totalSchemaList[y]%>"
                                                       name="tableList" onclick="set_check_tableList(this)"
                                                       value=<%= totalSchemaList[y]%>  CHECKED></td>
                                            <td><%= totalSchemaList[y]%>
                                            </td>
                                            <% } else { %>
                                               <td></td>
                                                <td></td>
                                                <td><input type="checkbox" id="<%= totalSchemaList[y]%>"
                                                           name="tableList" onclick="set_check_tableList(this)"
                                                           value=<%= totalSchemaList[y]%>></td>
                                                <td><%= totalSchemaList[y]%>
                                                </td>
                                            <% } %>
                                            <% } else {
                                                 %>
                                               <% if(totalSchemaList[y] != null) {%>
                                                    <td></td>
                                                    <td></td>
                                                    <td><input type="checkbox" id="<%= totalSchemaList[y]%>"
                                                               name="tableList" onclick="set_check_tableList(this)"
                                                               value=<%= totalSchemaList[y]%> CHECKED></td>
                                                    <td><%= totalSchemaList[y]%>
                                                    </td>
                                                <% } else { %>
                                                    <td colspan="2"><fmt:message key="empty.database"/></td>
                                                    <% } }%>
                                            <%
                                                count++;
                                                if (count == columns) {
                                                    count = 1;
                                            %>
                                            <tr></tr>
                                            <%
                                                }
                                                y++;
                                                if (y == x) {
                                                    break;
                                                }
                                            }
                                            if (y == x){
                                                break;
                                            }
                                        }
                                    %>
                                    </div>
                                 </table>
                            </td></tr>
                              <% } else { %>
                              <tr><td>
                                   <fmt:message key="carbon.datasource.error"/>
                              </td></tr>
                              <% } %>
                    <carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                  page="scriptViewTabList.jsp" pageNumberParameterName="pageNumber"/>

                    <tr>
                        <td class="buttonRow">
                             <% if (schemaList!= null){%>
                             <input class="button" type="button" value="< <fmt:message key="back"/>"  onclick="javascript:location.href = 'scriptViewSchemas.jsp?ordinal=1&tableList='+get_check_tableList()+'&flag=back';" />
           					 <% } else { %>
           					 <input class="button" type="button" value="< <fmt:message key="back"/>" onclick="location.href = 'scriptAddSource.jsp?ordinal=1&flag=back&datasource=<%=Encode.forHtmlAttribute(sourceId)%>&dbName=<%=Encode.forHtmlAttribute(dbName)%>' "/>
                             <% } %>
                              <% if (totalSchemaList!= null){
                              %>

                             <input class="button" type="button"  value="<fmt:message key="next"/> >"  onclick="gotonext()"/>
                             <% }
                             %>

                           <input class="button" type="button" value="<fmt:message key="cancel"/>"
							onclick="location.href = '../service-mgt/index.jsp'" />
                        </td>
                    </tr>
                </table>
               <script type="text/javascript">
    function gotonext(){
        if (validateTableSelection('<%=tableNames%>')) {
              location.href = 'scriptServiceGeneratingMode.jsp';
        }
    }
</script>

         </div>
       </form>
    </div>
</fmt:bundle>