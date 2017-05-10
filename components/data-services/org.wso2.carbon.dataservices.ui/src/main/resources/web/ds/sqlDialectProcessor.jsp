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
<%@ page import="org.wso2.carbon.dataservices.ui.beans.*" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>

<%
    String queryId = request.getParameter("queryId");
	String dialect = request.getParameter("txSQLDialect");
	String edit = request.getParameter("edit");
    String sql = request.getParameter("txtSQL");
    String flag = request.getParameter("flag");
	String datasource = request.getParameter("datasource");
	String mainSql = request.getParameter("mainSql");
    flag = (flag == null) ? "" : flag;
    sql = (sql == null) ? "" : sql;
    mainSql = (mainSql == null) ? "" : mainSql;
    datasource = (datasource == null) ? "" : datasource;
    dialect = (dialect == null) ? "" : dialect;
    boolean isDialectAvailable = false;

   
    Query query;
    if (queryId != null) {
    	query = dataService.getQuery(queryId);
    	if (query != null) {
           List<SQLDialect> sqlDialects = query.getSqlDialects();
            for (SQLDialect d : sqlDialects) {
                if (d.getDialect().equals(dialect)) {
                   isDialectAvailable = true;
                }
            }
    		 if (flag.equals("add")) {
                 if (!isDialectAvailable) {
                      query.addSqlDialects(dialect,sql);
                 } else {
                     String message = "SQLDialect " + dialect + " already exist.";
                     CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                 }

    		 } else if (flag.equals("edit")) { 
    			 query.updateSQLDialect(edit,dialect,sql);
    		 } else if (flag.equals("delete")) {
         		query.removeSQLDialect(dialect);
         	 }
    	} else {
    	    query = new Query();
            query.setId(queryId);
            query.setConfigToUse(datasource);
            query.setSql(mainSql);
            if (flag.equals("add")) { 
      		  query.addSqlDialects(dialect,sql);
 		    }
            dataService.getQueries().add(query);
    	 }
     
    } 
    
    
    

%>
<input type="hidden" id="flag" name="flag" value="<%=flag%>"/>
<input type="hidden" id="mainSql" name="mainSql" value="<%=mainSql%>"/>
<script type="text/javascript">
			 location.href = "addQuery.jsp?queryId=<%=queryId%>&ordinal=3";
</script>