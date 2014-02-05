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
 <%--This page is invoked when cancel is clicked.
 It Removes the query from the dataservice bean.--%>
 <%@ page import="org.wso2.carbon.dataservices.ui.beans.*" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>

<%

String queryId = request.getParameter("queryId");
Query query;
String forwardTo = null;
String flag = null;

if (queryId != null) {
	query = dataService.getQuery(queryId);
	if (query != null) {
		if (query.getStatus().equals("remove")) {
			dataService.removeQuery(query);
			forwardTo = "queries.jsp";
		} else {
			forwardTo = "queries.jsp";
		}
		
	} else {
		forwardTo = "queries.jsp";
	}
}


%>


<script type="text/javascript">
    location.href = "<%=forwardTo%>";
</script>