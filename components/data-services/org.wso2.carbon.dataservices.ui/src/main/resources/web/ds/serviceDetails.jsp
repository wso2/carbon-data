<!--
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
-->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="org.apache.axiom.om.impl.builder.StAXOMBuilder" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Data" %>
<%@page import="org.wso2.carbon.dataservices.ui.DataServiceAdminClient" %>
<%@page import="java.io.ByteArrayInputStream" %>
<%@ page import="org.wso2.carbon.CarbonError" %>
<script type="text/javascript" src="js/ui-validations.js"></script>
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data"
             scope="session"></jsp:useBean>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<carbon:breadcrumb
        label="Service Details"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<%!
    private String getDescription() {
        return "this is return from java method";
    }
%>


<%
    String serviceName = request.getParameter("serviceName");
    String flag = request.getParameter("flag");
    flag = (flag == null) ? "" : flag;
    String description = "";
    boolean boxcarring = false;
    boolean batchRequest = false;
    boolean enableDT = false;
    boolean enableStreaming = true;
    boolean finishButton = false;
    String txManagerJNDIName = "";
    String protectedTokens = "";
    String passwordProvider = "";
    String serviceNamespace = "";

    String detailedServiceName = null;

    if (serviceName != null && serviceName.trim().length() > 0) {
        if(serviceName != null) {
            detailedServiceName = serviceName;
            String[] path = serviceName.split("/");
            serviceName = path[path.length-1];
            dataService.setServiceHierarchy(detailedServiceName);
        }
        try {
            Data data;
            if (flag.equals("addXAData")) {
                serviceName = dataService.getName();
                serviceNamespace = dataService.getServiceNamespace();
                //txManagerClass = dataService.getTxManagerClass();
                txManagerJNDIName = dataService.getTxManagerName();
                description = dataService.getDescription();
                protectedTokens = dataService.getProtectedTokens();
                passwordProvider = dataService.getPasswordProvider();
                boxcarring = dataService.isBoxcarring();
                enableDT = dataService.isDTP();
                //useAppServerTS = dataService.isUseAppServerTS();
                batchRequest = dataService.isBatchRequest();
                enableStreaming = !dataService.isDisableStreaming();
                //txManagerCleanupMethod = dataService.getTxManagerCleanupMethod();
            }
            //TO DO: need to fix breadcrum issue
            else {
                String backendServerURL = CarbonUIUtil.getServerURL(
                        config.getServletContext(), session);
                ConfigurationContext configContext = (ConfigurationContext) config
                        .getServletContext().getAttribute(
                                CarbonConstants.CONFIGURATION_CONTEXT);
                String cookie = (String) session
                        .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                DataServiceAdminClient client = new DataServiceAdminClient(cookie,
                        backendServerURL, configContext);
                String serviceContents = "";
                serviceContents = client.getDataServiceContents(detailedServiceName);
                InputStream ins = new ByteArrayInputStream(
                        serviceContents.getBytes());
                OMElement configElement = (new StAXOMBuilder(ins))
                        .getDocumentElement();
                configElement.build();
                data = new Data();
                data.populate(configElement);
                serviceNamespace = data.getServiceNamespace();
                //txManagerClass = data.getTxManagerClass();
                txManagerJNDIName = data.getTxManagerName();
                description = data.getDescription();
                protectedTokens = data.getProtectedTokens();
                passwordProvider = data.getPasswordProvider();
                boxcarring = data.isBoxcarring();
                enableDT = data.isDTP();
                //useAppServerTS = data.isUseAppServerTS();
                batchRequest = data.isBatchRequest();
                data.setServiceHierarchy(detailedServiceName);
                //txManagerCleanupMethod = data.getTxManagerCleanupMethod();
                enableStreaming = !data.isDisableStreaming();
                request.getSession().setAttribute("dataService", data);
            }

            description = (description == null) ? "" : description;
            //txManagerClass = (txManagerClass == null) ? "" : txManagerClass;
            //txManagerCleanupMethod = (txManagerCleanupMethod == null) ? "" : txManagerCleanupMethod;
            txManagerJNDIName = (txManagerJNDIName == null) ? "" : txManagerJNDIName;
            serviceNamespace = (serviceNamespace == null) ? "" : serviceNamespace;
            protectedTokens = (protectedTokens == null) ? "" : protectedTokens;
            passwordProvider = (passwordProvider == null) ? "" : passwordProvider;

        } catch (Exception e) {
            CarbonError carbonError = new CarbonError();
            carbonError
                    .addError("Error occurred while saving data service configuration.");
            request.setAttribute(CarbonError.ID, carbonError);
            String errorMsg = e.getLocalizedMessage();
%>
<script type="text/javascript">
    location.href = "dsErrorPage.jsp?errorMsg=<%=errorMsg%>";

</script>
<%
        }
    } else {
        if (flag.equals("back")) {
            serviceName = dataService.getName();
            description = dataService.getDescription();
            description = (description == null) ? "" : description;
            serviceNamespace = dataService.getServiceNamespace();
            serviceNamespace = (serviceNamespace == null) ? "" : serviceNamespace;
            protectedTokens = dataService.getProtectedTokens();
            protectedTokens = (protectedTokens == null) ? "" : protectedTokens;
            passwordProvider = dataService.getPasswordProvider();
            passwordProvider = (passwordProvider == null) ? "" : passwordProvider;
            batchRequest = dataService.isBatchRequest();
            enableDT = dataService.isDTP();
            enableStreaming = !dataService.isDisableStreaming();
            boxcarring = dataService.isBoxcarring();
        } else {
            serviceName = "";
            request.getSession().setAttribute("dataService", null);
        }
    }
%>
<div id="middle">
    <h2>
        <%
            if (serviceName != null && !(serviceName.trim().equals(""))) {
                finishButton = true;
        %>
        <fmt:message key="service.edit.heading"/> <%
        out.write(" (" + serviceName + ")");
    %>
        <%
        } else {
        %>
        <fmt:message key="service.create.heading"/>
        <%
            }
        %>
    </h2>

    <div id="workArea">
        <form method="post" action="serviceDetailsProcessor.jsp" name="dataForm"
              onsubmit="return validateServiceDetailsForm();">
            <table class="styledLeft noBorders" id="dataSources" cellspacing="0" width="100%">
                <thead>
                <tr>
                    <th colspan="2"><fmt:message key="service.details"/></th>
                </tr>
                </thead>
                <tr>
                    <td colspan="2">
                        <table>
                            <tr>
                                <td><fmt:message key="service.name"/><font color="red">*</font></td>
                                <td align="left"><%
                                    if (!serviceName.equals("")) {
                                %>
                                    <input type="text" name="serviceName" id="serviceName" size="35"
                                           value="<%=serviceName%>" readonly="readonly"/>
                                    <%
                                    } else {
                                    %>
                                    <input type="text" name="serviceName" id="serviceName" size="35"
                                           value="<%=serviceName%>"/>
                                    <%
                                        }
                                    %>
                                </td>

                            <tr>
                                <td class="leftCol-small" style="white-space: nowrap;"><fmt:message
                                        key="service.namespace"/></td>
                                <td align="left"><%
                                    if (!serviceNamespace.equals("")) {
                                %>
                                    <input type="text" name="serviceNamespace" size="35"
                                           id="serviceNamespace" value="<%=serviceNamespace%>"/>
                                    <%
                                    } else {
                                    %>
                                    <input type="text" name="serviceNamespace" size="35"
                                           id="serviceNamespace" value="<%=serviceNamespace%>"/>
                                    <%
                                        }
                                    %>
                                </td>
                            </tr>
                            <tr>
                                <td><fmt:message key="service.description"/></td>
                                <td align="left"><textarea cols="40" rows="5"
                                                           name="description"><%=description%>
                                </textarea></td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="middle-header" colspan="2"><fmt:message
                            key="advance.configurations"/></td>
                </tr>
                <tr>
                    <td colspan="2">
                        <table>
                            <tr>
                                <td>
                                    <input type="checkbox" id="enableBatchReq"
                                           name="enableBatchReq"  <%=(batchRequest) ? "checked=\"checked\"" : ""%>
                                           value=<%=batchRequest%>></td>
                                </td>
                                <td align="left"><label for="enableBatchReq"><fmt:message
                                        key="service.batch.request"/></label></td>
                            </tr>

                            <tr>
                                    <%--<td><carbon:tooltips   image="images/help.gif"  key='dataservices.task.classname.cannotfound.msg' noOfWordsPerLine='11'> </carbon:tooltips> </td>--%>
                                <td>
                                    <input type="checkbox" value="<%=boxcarring%>"
                                           id="enableBoxcarring"
                                           name="enableBoxcarring" <%=(boxcarring) ? "checked=\"checked\"" : ""%>>
                                </td>
                                <td align="left"><label for="enableBoxcarring"><fmt:message
                                        key="service.boxcarring"/></label></td>
                            </tr>
                            <tr>
                                <td>
                                    <input type="checkbox" id="enableStreaming"
                                           name="enableStreaming"  <%=(enableStreaming) ? "checked=\"checked\"" : ""%>
                                           value=<%=enableStreaming%>>
                                </td>
                                <td align="left"><label for="enableStreaming"><fmt:message
                                        key="service.enable.streaming"/></label></td>
                                    <%--<td><carbon:tooltips   image="images/help.gif" key='dataservices.task.interval.cannotfound.msg' noOfWordsPerLine='11'> </carbon:tooltips> </td>--%>
                            </tr>

                            <tr id="addDistributedTransaction">

                                    <%--<td><carbon:tooltips image="magnifier.gif" description='<%= getDescription() %>' noOfWordsPerLine='10' > </carbon:tooltips></td>--%>
                                <td align="left">
                                    <input type="checkbox" value="true" name="enableDT"
                                           id="enableDT" <%=(enableDT) ? "checked=\"checked\"" : ""%>
                                           onclick="onEnableXAChange(document);">
                                </td>
                                <td align="left"><label for="enableDT"><fmt:message
                                        key="service.distributed.transactions"/></label></td>
                                <%--<td colspan="2">--%>
                                    <%--<a class="icon-link"--%>
                                       <%--href="#" onclick="document.getElementById('txManagerNameRow').style.display='' "> Advanced</a>--%>
                                <%--</td>--%>
                            </tr>
                        </table>
                    </td>
                </tr>
                
                <tr id="txManager" style="<%=(enableDT) ? "" : "display:none"%>">
                    <td>
                        <table style="margin-left: 40px;">
                           <tr>
                            <td colspan="2">
                                <a id="txManagerJNDINameMax" onclick="showAdvancedServiceDetailsConfigurations()" style="background-image: none;" href="#" >Show Advanced</a>
                            </td>
                            </tr>
                            <tr id="txManagerNameRow" style="display:none">
                                <td><fmt:message key="service.transaction.manager.name"/></td>
                                <td>
                                    <input type="text" name="txManagerJNDIName" size="35"
                                           id="txManagerJNDIName" value="<%=txManagerJNDIName%>"/>
                                </td>
                            </tr>

                        </table>
                    </td>
                </tr>

                <tr>
                    <td class="buttonRow">
                        <input class="button" type="submit" value="<fmt:message key="next"/> >"/>
                            <%--<% if(finishButton) { %>
                              <input class="button" type="button" value="<fmt:message key="finish"/>" onclick="location.href = 'wizardDoneProcessor.jsp'"/>
                          <% } %>--%>
                        <input class="button" type="button" value="<fmt:message key="cancel"/>"
                               onclick="location.href = '../service-mgt/index.jsp'"/>

                    </td>
                </tr>

            </table>
        </form>
    </div>
</div>

<script type="text/javascript">
    function showAdvancedServiceDetailsConfigurations() {
        var symbolMax = document.getElementById('txManagerJNDINameMax');
        var advancedConfigFields = document.getElementById('txManagerNameRow');
        if (advancedConfigFields.style.display == 'none') {
            // symbolMax.setAttribute('style','background-image:url(images/minus.gif);');
            symbolMax.innerHTML = 'Hide Advanced';
            advancedConfigFields.style.display = '';
        } else {
            //symbolMax.setAttribute('style','background-image:url(images/plus.gif);');
            symbolMax.innerHTML = 'Show Advanced';
            advancedConfigFields.style.display = 'none';
        }
    }
</script>

</fmt:bundle>
