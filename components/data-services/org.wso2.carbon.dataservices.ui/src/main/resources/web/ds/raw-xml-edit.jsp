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
<%@ page import="org.wso2.carbon.CarbonError" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.dataservices.ui.DataServiceAdminClient"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.apache.axis2.AxisFault"%>

<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<!--EditArea javascript syntax hylighter -->
<script language="javascript" type="text/javascript" src="../editarea/edit_area_full.js"></script>
<script type="text/javascript" src="js/jquery.flot.js"></script>

<%
String serviceName = request.getParameter("serviceName");
String serviceContents = "";
try{
	String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
	ConfigurationContext configContext =
        (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	DataServiceAdminClient client = new DataServiceAdminClient(cookie,backendServerURL,configContext);
	serviceContents = client.getDataServiceContents(serviceName);
   }catch(AxisFault e){
	   CarbonError carbonError = new CarbonError();
	   carbonError.addError("Error occurred while saving data service configuration.");
	   request.setAttribute(CarbonError.ID, carbonError);
		String errorMsg = e.getLocalizedMessage();
		%>
		<script type="text/javascript">
			location.href = "dsErrorPage.jsp?errorMsg=<%=errorMsg%>";
		</script>
		<%
   }
%>
<script type="text/javascript">

    function cancelSaveHandler() {
        document.location.href = "handler.jsp?region=region3&item=registry_handler_menu";
    }
    YAHOO.util.Event.onDOMReady(function() {
        editAreaLoader.init({
            id : "dsConfig"        // textarea id
            ,syntax: "xml"            // syntax to be uses for highgliting
            ,start_highlight: true        // to display with highlight mode on start-up
            ,allow_resize: "both"
            ,min_height:250
        });
    })

</script>

<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<carbon:breadcrumb
		label="dataservice.xml.editor"
		resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />
		
<div id="middle">
    <h2><fmt:message key="dataservice.xml.editor"/>(<%=serviceName%>)</h2>
    
<div id="workArea">
	<form method="post"
		<%--action="<%= "./rawXMLProcessor.jsp?saveConfig=true&caller=../ds/raw-xml-edit.jsp&serviceName="+serviceName%>">--%>
            action="<%= "./rawXMLProcessor.jsp?saveConfig=true&caller=../service-mgt/index.jsp&serviceName="+serviceName%>">
<table class="styledLeft">
<tr><td>
            <textarea id ="dsConfig" name="dsConfig"
                      style="background-color:lavender; width:99%;height:470px;*height:500px;
                      font-family:verdana;
                      font-size:11px;
                      color: darkblue;
                      border:solid 1px #9fc2d5;
                      overflow-x:auto;
                      overflow-y:auto"><%=serviceContents.replaceAll("&", "&amp;")%></textarea>
                     
        </td></tr>
                <tr>
                    <td class="buttonRow">
            <input class="button" type="submit" name="save" value="Save"/>
            <input class="button" type="reset" name="cancel" value="Cancel" onclick="javascript:location.href='../service-mgt/service_info.jsp?serviceName=<%=serviceName %>';"/>
	</td>
         </tr>
       </table>
    </form>
</div>
</div>
</fmt:bundle>
<script>
$('dsConfig').innerHTML = format_xml($('dsConfig').value);
</script>

