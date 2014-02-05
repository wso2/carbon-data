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

<%@ page import="java.util.Arrays" %>
<carbon:breadcrumb
        label="service.generation"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="js/ui-validations.js"></script>
<%
    String[] tableList;
    if (session.getAttribute("selectedTables") == null
            || session.getAttribute("selectedTables").equals("")) {  //This happens if user does not click any of the checkbox.So by default all tables should be selected.
         tableList = (String[]) session.getAttribute("TotalList");
    } else {
        if (session.getAttribute("selectedTables").toString().charAt(0) == ':') {
            tableList = session.getAttribute("selectedTables").toString().substring(1).split(":");
        } else {
            tableList = session.getAttribute("selectedTables").toString().split(":");
        }
    }
    session.setAttribute("tableList", tableList);
  boolean multipleMode = false;
%>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">

    <div id="middle">
        <h2><fmt:message key="service.generation"/> </h2>

        <form action="scriptCreateServices.jsp?ordinal=3" method="post" onsubmit="return validateDSGenerator();">
           <div id="workArea">
                       <table class="styledLeft">
                        <thead>
                            <tr>
                                <th colspan="2"><fmt:message key="service.generation.mode"/> </th>
                            </tr>
                       </thead>
                            <tr><td>
                                <table class="normal" >
                                   <tr>
                                     <td> <input type="radio" name="mode" value="Single" id="mode" <%= (multipleMode) ? "checked=\"checked\"" : "" %> onchange="onModeChange(document);"> Single Service - Creates one service for all selected tables</td>
                                   </tr>
                                    <br>
                                    <tr>
                                      <td> <input type="radio" name="mode" value="Multiple" id="mode" <%= (!multipleMode) ? "checked=\"checked\"" : "" %> onchange="onModeChange(document);"> Multiple Services - Creates a service per table</td>
                                   </tr>
                                 </table>
                            </td></tr>
                            <tr><td>
                               <table class="normal" title="Service Details">
                                <tr>
				                    <td><label><fmt:message key="service.namespace"/></label></td>
				                    <td>
				                    <input value="" id="txtNamespace"
				                           name="txtNamespace" size="30" type="text"></td>
				                </tr>    
				                <tr id ="txServiceNameRow"  style="<%=(multipleMode)  ? "" : "display:none"%>">
					                <td><label><fmt:message key="service.name"/><font color="red">*</font></label></td>
					                <td><input value="" id="txtServiceName"
					                           name="txtServiceName" size="30" type="text"></td>
				               </tr>
	                         </table>
	                     </td></tr>
	                     </table>

                    <tr>
                        <td class="buttonRow">
                             <input class="button" type="button" value="< <fmt:message key="back"/>" onclick="location.href = 'scriptViewTabList.jsp?flag=back&ordinal=1' "/>
                             <input class="button" type="submit" value="<fmt:message key="next"/> >"/>
                            <input class="button" type="button" value="<fmt:message key="cancel"/>"
								onclick="location.href = '../service-mgt/index.jsp'" />
                        </td>
                    </tr>

                </table>

         </div>
       </form>
    </div>
</fmt:bundle>