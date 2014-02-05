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
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Resource" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<carbon:breadcrumb 
		label="Resources"
		resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
		topPage="false" 
		request="<%=request%>" />
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>
<script type="text/javascript" src="js/ui-validations.js"></script>    
<%
    ArrayList<Resource> resources = dataService.getResources();
    boolean hasRecords = false;
    Iterator iterator = null;
    String path = null;
    String query = null;
    String resourceMethod = null;
    if(resources != null && resources.size() > 0){
        hasRecords = true;
        iterator = resources.iterator();
    }

%>
<div id="middle">
<h2><fmt:message key="service.resources"/></h2>
	<div id="workArea">
	<form method="post" action="resourceProcessor.jsp" name="dataForm"
		onsubmit="return validateResourcesForm();">
    <table class="styledLeft" id="resource-table">
        <% if(hasRecords){%>
        <thead>
			<tr>
				<th width="20%"><fmt:message key="resource.path" /></th>
				<th width="20%"><fmt:message key="query" /></th>
				<th width="60%"><fmt:message key="actions" /></th>
			</tr>
		</thead>
        <tbody>
             <%
                      while(iterator.hasNext()){
                         Resource resource = (Resource)iterator.next();
                            if(resource != null){
                                path = resource.getPath();
                                query = resource.getCallQuery().getHref();
                                boolean disableStreaming = resource.isDisableStreaming();
                                resourceMethod =  resource.getMethod();
                %>
            <tr>
                <td><%=path%></td>
				<td><%=query%></td>
                <input type="hidden" value="<%=resourceMethod%>" id="resourceMethod" name="resourceMethod" />
                <input type="hidden" value="<%=path%>" id="<%=path%>" name="<%=path%>" />
                <td>
                    <%
                        String editURI = "addResource.jsp?action=edit&resourcePath="+resource.getPath()+"&disableStreaming="+disableStreaming;                        
                    %>                    
                    <a class="icon-link" style="background-image:url(../admin/images/edit.gif);" href="<%=editURI%>"><fmt:message key="edit.resource" /></a>
					<a class="icon-link" style="background-image:url(../admin/images/delete.gif);" onclick="deleteResources(document.getElementById('<%=path%>').value);" href="#"><fmt:message key="delete.resource" /></a>
				</td>
            </tr>
             <%
                    }
                    }                
                }
            %>
            <tr>
				<td colspan="3">
					<a class="icon-link" style="background-image:url(../admin/images/add.gif);" href="addResource.jsp"><fmt:message
				key="add.new.resource" /></a>	
				</td>
			</tr>
			<tr>
			<td class="buttonRow" colspan="3"><input class="button" type="button"
                        value="< <fmt:message key="back"/>" onclick="location.href = 'operations.jsp?ordinal=3'" /> <input class="button"
				type="button" value="<fmt:message key="finish"/>" onclick="location.href = 'wizardDoneProcessor.jsp'"/>
                <input class="button" type="button" value="<fmt:message key="save.as.draft"/>" onclick="location.href = 'wizardDoneProcessor.jsp?flag=wip'"/><input
				class="button" type="button" value="<fmt:message key="cancel"/>"
				onclick="location.href = '../service-mgt/index.jsp'" /></td>
			</tr>				
		</tbody>
	</table>
	</form>
	</div>
</div>
</fmt:bundle>
