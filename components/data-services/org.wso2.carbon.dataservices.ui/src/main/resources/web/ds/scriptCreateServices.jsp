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
<%@ page import="org.wso2.carbon.CarbonError" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.apache.axis2.AxisFault"%>
<%@ page import="org.wso2.carbon.dataservices.ui.DataServiceAdminClient" %>
           <%@ page import="java.util.ArrayList" %>
           <carbon:breadcrumb
        label="Deployed Services"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<%
       String sourceId = (String)session.getAttribute("datasource");
       String dbName  = (String)session.getAttribute("dbName");
       String[] schemaList = (String[])session.getAttribute("schemaList");
       String[] tableList  = (String[])session.getAttribute("tableList");
       String dataserviceName = request.getParameter("txtServiceName");
       String serviceNamespace = request.getParameter("txtNamespace");
       String mode = request.getParameter("mode");
    boolean serviceMode;
    if("Single".equals(mode)){
        serviceMode = true;
    }else{
        serviceMode = false;
    }
    String[]servicesList = null;
    String serviceContents = "";
    if ( (tableList!=null)) {
		  for (int i=0;i<tableList.length;i++) {
  	  }
	 }
	 String error = "";
    try{
	String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
	ConfigurationContext configContext =(ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);
	DataServiceAdminClient client = new DataServiceAdminClient(cookie,backendServerURL,configContext);

        if(serviceMode){
        	//single service
            servicesList = new String[1];
            servicesList[0] = client.getDSService(sourceId,dbName,schemaList,tableList,dataserviceName,serviceNamespace);

        }else{
        	 //multiple services
             servicesList = client.getDSServiceList(sourceId,dbName,schemaList,tableList,serviceNamespace);
        }

    

   }catch(Exception e){
       servicesList = null;
	   error = e.getMessage();
    }
%>

<script type="text/javascript" src="js/ui-validations.js"></script>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">

    <div id="middle">
        <h2><fmt:message key="generated.services"/> </h2>
        <div id="workArea">
            <form>
                <table class="styledLeft">
                         <thead>
                            <tr>
                                <th colspan="2"><fmt:message key="deployed.services"/> </th>
                            </tr>
                            </thead>
                            <tr><td>
                                <table class="normal">
                                   <%
                                       if (servicesList != null) {
                                            for(String serviceName:servicesList){
                                    %>
                                    <tr>
                                        <td><%= serviceName%></td>
                                    </tr>
                                    <% }
                                     %>
                                   <h3>Following Service(s) are Deployed Sucessfully</h3>
                                </table>
                            </td></tr>
                        </table>
                    </td>
                    </tr>
                    <% } else {%>
                    <h3>Service Deployment Unsuccessful due to </h3>
                    <tr>
                                                            <td><%= error%></td>
                                                        </tr>
                    </td>

                    <% }%>
                    <tr>
                        <td class="buttonRow">
                           <input
                        class="button" type="button" value="<fmt:message key="finish"/>"
                        onclick="location.href = '../service-mgt/index.jsp'" /></td>
                        </td>
                    </tr>

                </table>
            </form>
        </div>
    </div>
</fmt:bundle>