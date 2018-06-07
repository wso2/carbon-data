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
        label="schema-list"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<%
   // String[] selectedSchema = ()
    boolean isBack =  request.getParameter("flag") == null ? false: true; 
    String sourceId = null;
    String dbName = null;
    String[] schemaList = null;
    String[] totalSchemaList = null;
    String serviceContents = "";
    int numberOfPages = 0;
    int pageNumberInt = 0;
    String parameters = "";
    String schemaNames="";
    String schemaListSession[] = null;
    String pageNumberStr = request.getParameter("pageNumber");
    String[] allSchemaList = null;
    
    //sourceId = (String)session.getAttribute("datasource");
//    dbName = request.getParameter("dbName");
//    session.setAttribute("dbName", dbName);
    if (pageNumberStr == null) {
        pageNumberStr = "0";
    }
    int pageNumber = 0;
    try {
        pageNumber = Integer.parseInt(pageNumberStr);
    } catch (NumberFormatException ignored) {
        // page number format exception
    }
    PaginatedTableInfo paginatedTableInfo;
    String schemaName = request.getParameter("schemaName");
    schemaName = (schemaName == null) ? "" : schemaName.trim();

    if ((isBack)) {
    	String tableListValues = request.getParameter("tableList");
        String tableList[] = tableListValues.split(":");
    	session.setAttribute("tableList", tableList);
    	sourceId =  (String)session.getAttribute("datasource");
        dbName = (String)session.getAttribute("dbName");
	    schemaListSession = (String[])session.getAttribute("schemaList");
	    totalSchemaList = (String[]) session.getAttribute("totalSchemaList");
        schemaNames = (String) session.getAttribute("schemaNames");
	} else {
        if (session.getAttribute("datasource") == null || session.getAttribute("datasource").equals("")) {
            sourceId = request.getParameter("datasource");
		    session.setAttribute("datasource", sourceId);
            dbName = request.getParameter("dbName");
		    session.setAttribute("dbName", dbName);
        } else {
             sourceId = (String)session.getAttribute("datasource");
            dbName =  (String)session.getAttribute("dbName");
        }


		    try {
	            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
	            ConfigurationContext configContext =
	                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
	            String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);
	            DataServiceAdminClient client = new DataServiceAdminClient(cookie, backendServerURL, configContext);
                paginatedTableInfo = client.getPaginatedSchemaInfo(pageNumber, sourceId);
                //schemaList = client.getdbSchemaList(sourceId);
                totalSchemaList = paginatedTableInfo.getTableInfo();
                numberOfPages = paginatedTableInfo.getNumberOfPages();
	            session.setAttribute("totalSchemaList", totalSchemaList);

                allSchemaList = client.getdbSchemaList(sourceId);
                if (allSchemaList != null) {
                    for (int i = 0; i < allSchemaList.length; i++) {
                        schemaNames=schemaNames+allSchemaList[i]+":";
                    }
                    session.setAttribute("schemaNames", schemaNames);
                }
	        } catch (Exception e) {
	            CarbonError carbonError = new CarbonError();
	            carbonError.addError("Error occurred while saving data service configuration.");
	            request.setAttribute(CarbonError.ID, carbonError);
	        }
	}
 
    //redirecting page
    if (totalSchemaList == null || totalSchemaList[0] == null) {
    	%>                                                      
    	<script type="text/javascript">
    	    location.href = "scriptViewTabList.jsp";
    	</script>
    	<%
            }

%>
 <carbon:paginator pageNumber="<%=1%>" numberOfPages="<%=1%>"
                  page="scriptViewSchemas.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%=parameters%>"/>

<script type="text/javascript" src="js/ui-validations.js"></script>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">

    <div id="middle">
        <h2><fmt:message key="schema.list"/></h2>

        <form action="scriptViewTabList.jsp" method="post" name="dataForm">
           <div id="workArea">
                <table class="styledLeft noBorders" cellspacing="0" width="100%">
                    <thead>
                       <tr>
                          <th colspan="2"><fmt:message key="select.schema"/></th>
                       </tr>
                    </thead>
                    <% if ( totalSchemaList != null){%>
                    <tr>
                        <td>
                          <table>
                            <tr>
                               <td colspan="2">
                                   <table>
                                       <tr><td></td> <td>Schema Name</td>
                                    <td><input type="text" name="schemaName" id="schemaName"
                                               value="<%=schemaName%>"/></td></tr>
                                   </table>
                                  <table>
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
                                                //schemaNames=schemaNames+totalSchemaList[y]+":";
                                                if (schemaListSession != null){
                                                for (String sessionTableName:schemaListSession) {
                                                    if(sessionTableName.equals(totalSchemaList[y])) {
                                                        isSelected = true;
                                                    }
                                                }
                                                if (isSelected) {
                                               %>

                                                <td></td>
                                                <td><input type="radio" id = "<%=totalSchemaList[y]%>"  name="schemaList"   value=<%=totalSchemaList[y]%> CHECKED ></td>
                                                <td><%=totalSchemaList[y]%></td>
                                         <%} else {  %>

                                                <td></td>
                                                <td><input type="radio" id = "<%=totalSchemaList[y]%>"  name="schemaList"  value=<%=totalSchemaList[y]%> ></td>
                                                <td><%=totalSchemaList[y]%></td>
                                         <% }
                                               } else {
                                         %>
                                                
                                                <td></td>
                                                <td><input type="radio" id = "<%=totalSchemaList[y]%>"  name="schemaList"  value=<%=totalSchemaList[y]%> CHECKED ></td>
                                                <td><%=totalSchemaList[y]%></td>
                                         <%
                                            } %>
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
                                                }
                                    %>
                                      </div>
                                   </table>
                                </td>
                             </tr>
                                <carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                  page="scriptViewSchemas.jsp" pageNumberParameterName="pageNumber"/>



                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input class="button" type="button" value="< <fmt:message key="back"/>"
                                   onclick="location.href = 'scriptAddSource.jsp?ordinal=0&flag=back&datasource=<%=Encode.forHtmlAttribute(sourceId)%>&dbName=<%=Encode.forHtmlAttribute(dbName)%>' "/>
                            
                         <% if (isBack ){%>
                            <input class="button" type="submit" value="<fmt:message key="next"/> > " onclick = "document.dataForm.action='scriptViewTabList.jsp?ordinal=2&flag=fwd';return validateSchemaTextField('<%=schemaNames%>');" />
                             
                          <% } else {%>
                            <input class="button" type="submit" value="<fmt:message key="next"/> > " onclick = "document.dataForm.action='scriptViewTabList.jsp?ordinal=2';return validateSchemaTextField('<%=schemaNames%>');" />
                            
                           <% } %>
                           <input class="button" type="button" value="<fmt:message key="cancel"/>"
				               onclick="location.href = '../service-mgt/index.jsp'" />
                            
                        </td>
                    </tr>
                </table>
            <script type="text/javascript">
                
                function set_check_schemaList(obj){
                    var checkstate = false;
                    if (obj.checked){
                         checkstate = true;
                    }
                    jQuery.ajax({
                    data: "checkedValue="+obj.value+"&checked="+checkstate,
                    url: "setSchemaSession.jsp",
                    context: document.body,
                    success: function(){

                    }
                    });
                }

                function validateSchemaTextField(schema) {
                    var schemaText = document.getElementById('schemaName').value;
                    var schemaSet= schema.split(":");
                    var isAvailable = false;
                    if (schemaText != null && schemaText != "") {
                        for (var i = 0; i < schemaSet.length; i ++) {
                          if ( schemaText.trim() == schemaSet[i]) {
                              isAvailable = true;
                          }
                         }
                        if (!isAvailable) {
                                CARBON.showWarningDialog("You have entered schema which not exist");
                                return false;
                        }
                    } else {
                        return validateTableSelection(schema);
                    }
                    return true;
                }

            </script>
         </div>
       </form>
    </div>
</fmt:bundle>
