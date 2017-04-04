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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.dataservices.ui.DataServiceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %><%

    String[] totalSchemaList;
    String sourceId = (String)session.getAttribute("datasource");

    String schemaFlag = request.getParameter("schemaFlag");
    if (schemaFlag != null && !schemaFlag.equals("")) {
        session.setAttribute("schemaFlag",schemaFlag);
        if (schemaFlag.equals("selectNoneTables")) {
            session.setAttribute("totalSchemaList","");
        } else if (schemaFlag.equals("selectAllSchemas")) {
            try {
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
	            ConfigurationContext configContext =
	                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
	            String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);
	            DataServiceAdminClient client = new DataServiceAdminClient(cookie, backendServerURL, configContext);
                totalSchemaList = client.getdbSchemaList(sourceId);
                session.setAttribute("totalSchemaList",totalSchemaList);
            } catch (Exception e) {
                 
            }
        }
    }
    
%>