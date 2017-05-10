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
<%@ page import="org.wso2.carbon.dataservices.ui.beans.AuthProvider" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Property" %>
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
    boolean editingDataService = false;
    boolean boxcarring = false;
    boolean batchRequest = false;
    boolean enableStreaming = true;
    boolean disableLegacyBoxcarringMode = false;
    boolean finishButton = false;
    boolean enableHTTP = true;
    boolean enableHTTPS = true;
    boolean enableLocal = true;
    boolean enableJMS = false;
    String txManagerJNDIName = "";
    String protectedTokens = "";
    String passwordProvider = "";
    String serviceNamespace = "";
    AuthProvider authProvider = null;

    String detailedServiceName = null;

    if (serviceName != null && serviceName.trim().length() > 0) {
        if(serviceName != null) {
            detailedServiceName = serviceName;
            String[] path = serviceName.split("/");
            serviceName = path[path.length-1];
            editingDataService = true;
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
                disableLegacyBoxcarringMode = dataService.isDisableLegacyBoxcarringMode();
                //useAppServerTS = dataService.isUseAppServerTS();
                batchRequest = dataService.isBatchRequest();
                enableStreaming = !dataService.isDisableStreaming();
                enableHTTP = dataService.isEnableHTTP();
                enableHTTPS = dataService.isEnableHTTPS();
                enableLocal = dataService.isEnableLocal();
                enableJMS = dataService.isEnableJMS();
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
                disableLegacyBoxcarringMode = dataService.isDisableLegacyBoxcarringMode();
                //useAppServerTS = data.isUseAppServerTS();
                batchRequest = data.isBatchRequest();
                data.setServiceHierarchy(detailedServiceName);
                //txManagerCleanupMethod = data.getTxManagerCleanupMethod();
                enableStreaming = !data.isDisableStreaming();
                enableHTTP = data.isEnableHTTP();
                enableHTTPS = data.isEnableHTTPS();
                enableLocal = data.isEnableLocal();
                enableJMS = data.isEnableJMS();
                request.getSession().setAttribute("dataService", data);
                authProvider = data.getAuthProvider();
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
            e.printStackTrace();
%>
<script type="text/javascript">
    location.href = "dsErrorPage.jsp?errorMsg=<%=errorMsg%>";

</script>
<%
        }
    } else {
        if (flag.equals("back")) {
            serviceName = dataService.getName();
            detailedServiceName = serviceName;
            editingDataService = true;
            description = dataService.getDescription();
            description = (description == null) ? "" : description;
            serviceNamespace = dataService.getServiceNamespace();
            serviceNamespace = (serviceNamespace == null) ? "" : serviceNamespace;
            protectedTokens = dataService.getProtectedTokens();
            protectedTokens = (protectedTokens == null) ? "" : protectedTokens;
            passwordProvider = dataService.getPasswordProvider();
            passwordProvider = (passwordProvider == null) ? "" : passwordProvider;
            batchRequest = dataService.isBatchRequest();
            enableStreaming = !dataService.isDisableStreaming();
            disableLegacyBoxcarringMode = dataService.isDisableLegacyBoxcarringMode();
            boxcarring = dataService.isBoxcarring();
            enableHTTP = dataService.isEnableHTTP();
            enableHTTPS = dataService.isEnableHTTPS();
            enableLocal = dataService.isEnableLocal();
            enableJMS = dataService.isEnableJMS();
            authProvider = dataService.getAuthProvider();
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
                                           value="<%=detailedServiceName%>" readonly="readonly"/>
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
                                           onclick="viewdisableLegacyBoxcarringMode();"
                                           onload="onLoadPage();"
                                           name="enableBoxcarring" <%=(boxcarring) ? "checked=\"checked\"" : ""%>>
                                </td>
                                <td align="left"><label for="enableBoxcarring"><fmt:message
                                        key="service.boxcarring"/></label></td>
                                <td/>
                                <td id="disableLegacyBoxcarringModeCheckbox" hidden>
                                    <input type="checkbox"
                                           id="disableLegacyBoxcarringMode"
                                           name="disableLegacyBoxcarringMode"  <%=(disableLegacyBoxcarringMode) ? "checked=\"checked\"" : ""%>
                                           value=<%=disableLegacyBoxcarringMode%>>
                                </td>
                                <td align="left" id="disableLegacyBoxcarringModeLabel" hidden><label for="disableLegacyBoxcarringMode"><fmt:message
                                        key="service.boxcarring.disable.legacy.mode"/></label></td>
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
                        </table>
                    </td>
                </tr>

                <tr>
                    <td class="middle-header" colspan="2"><fmt:message
                            key="transport.setting.configurations"/></td>
                </tr>

                <tr>
                    <td colspan="2">
                        <table>
                            <tr>
                                <td>
                                    <input type="checkbox" id="enableHTTP"
                                           name="enableHTTP"  <%=(enableHTTP) ? "checked=\"checked\"" : ""%>
                                           value=<%=enableHTTP%>></td>
                                </td>
                                <td align="left"><label for="enableHTTP"><fmt:message
                                        key="enable.http"/></label></td>
                            </tr>
                            <tr>
                                <td>
                                    <input type="checkbox" id="enableHTTPS"
                                           name="enableHTTPS"  <%=(enableHTTPS) ? "checked=\"checked\"" : ""%>
                                           value=<%=enableHTTPS%>></td>
                                </td>
                                <td align="left"><label for="enableHTTPS"><fmt:message
                                        key="enable.https"/></label></td>
                            </tr>
                            <tr>
                                <td>
                                    <input type="checkbox" id="enableLocal"
                                           name="enableLocal"  <%=(enableLocal) ? "checked=\"checked\"" : ""%>
                                           value=<%=enableLocal%>></td>
                                </td>
                                <td align="left"><label for="enableLocal"><fmt:message
                                        key="enable.local"/></label></td>
                            </tr>
                            <tr>
                                <td>
                                    <input type="checkbox" id="enableJMS"
                                           name="enableJMS"  <%=(enableJMS) ? "checked=\"checked\"" : ""%>
                                           value=<%=enableJMS%>></td>
                                </td>
                                <td align="left"><label for="enableJMS"><fmt:message
                                        key="enable.jms"/></label></td>
                            </tr>
                        </table>
                    </td>
                </tr>
                
                <tr id="txManager">
                    <td>
                        <table>
                           <tr>
                            <td colspan="2">
                                <a id="txManagerJNDINameMax" onclick="showAdvancedServiceDetailsConfigurations()" style="background-image: none;" href="#" >Show Advanced Distributed Transactions Settings</a>
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

                <tr id="authProv">
                    <td>
                        <table id="authorizationProviderConfigTable" class="styledLeft noBorders" cellspacing="0" width="100%">
                            <tr>
                                <td colspan="2" class="middle-header">
                                    <a onclick="showDynamicAuthorizationProviderConfigurations()" class="icon-link" style="background-image:url(images/plus.gif);"
                                       href="#symbolMax" id="symbolMax"></a>
                                    <fmt:message key="authorization.provider.config.root"/>
                                </td>
                            </tr>
                            <tr id="authorizationProviderConfigFields" style="display:none">
                                <td>
                                    <table id="authorizationProviderConfigFieldsTable" cellspacing="0" width="100%">

                                        <table id="authorizationProviderClassMappingTable"  >
                                            <tr>
                                                <td><label><fmt:message key="authorization.provider.config.className"/></label></td>
                                                <td><input type="text" name="authorizationProviderClass" id="authorizationProviderClass" size="50" value="<%=authProvider != null ? authProvider.getClassName() : ""%>"/></td>
                                            </tr>
                                        </table>

                                        <table id="authorizationProviderParametersTable" border="0" style="border-width: 1px; border-color:#000000; border-style: solid;">
                                            <tr>
                                                <td colspan="2"><a class="icon-link"
                                                                   style="background-image:url(../admin/images/add.gif);"
                                                                   onclick=" addAuthorizationProviderParameter(document,document.getElementById('authorizationProviderParamCount').value);">
                                                    <fmt:message key="authorization.provider.config.new.parameter"/></a></td>
                                            </tr>
                                            <%
                                                if (authProvider != null) {
                                                    int i = 0;
                                                    for (Property property : authProvider.getProperties()) {
                                            %>

                                            <tr id="authProviderParameterRow<%=i%>">
                                                <td><label><fmt:message key="authorization.provider.config.parameter.name"/></label></td>
                                                <td><input type="text" name="authProviderParameterName<%=i%>"
                                                           id="authProviderParameterName<%=i%>" size="15"
                                                           value="<%=property.getName()%>"/></td>

                                                <td><label><fmt:message key="authorization.provider.config.parameter.value"/></label></td>
                                                <td><input type="text" name="authProviderParameterValue<%=i%>"
                                                           id="authProviderParameterValue<%=i%>" size="15" value="<%=property.getValue()%>"/>
                                                </td>

                                                </td>
                                                <td><a class="icon-link"
                                                       style="background-image:url(../admin/images/delete.gif);"
                                                       href="javascript:deleteAuthParamField('<%=i%>')"> <fmt:message
                                                        key="delete"/></a></td>
                                            </tr>

                                            <%
                                                        i++;
                                                    }
                                                }
                                            %>

                                        </table>
                                    </table>
                                </td>
                            </tr>
                            <label hidden id="paramNameLabel" name="paramNameLabel"><fmt:message key="authorization.provider.config.parameter.name"/></label>
                            <label hidden id="paramValueLabel" name="paramValueLabel"><fmt:message key="authorization.provider.config.parameter.value"/></label>
                            <input type="hidden" id="authorizationProviderParamCount" name="authorizationProviderParamCount" value="<%= authProvider != null ? authProvider.getProperties().size() : "0"%>"/>
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
var editingDataService = <%=editingDataService %> ;
window.onload = onLoadPage();
    function showAdvancedServiceDetailsConfigurations() {
        var symbolMax = document.getElementById('txManagerJNDINameMax');
        var advancedConfigFields = document.getElementById('txManagerNameRow');
        if (advancedConfigFields.style.display == 'none') {
            // symbolMax.setAttribute('style','background-image:url(images/minus.gif);');
            symbolMax.innerHTML = 'Hide Advanced Distributed Transactions Settings';
            advancedConfigFields.style.display = '';
        } else {
            //symbolMax.setAttribute('style','background-image:url(images/plus.gif);');
            symbolMax.innerHTML = 'Show Advanced Distributed Transactions Settings';
            advancedConfigFields.style.display = 'none';
        }
    }
    function viewdisableLegacyBoxcarringMode() {
        var chboxEnableBoxcarring = document.getElementById("enableBoxcarring");
        if (chboxEnableBoxcarring.checked) {
            document.getElementById("disableLegacyBoxcarringModeCheckbox").hidden = false;
            document.getElementById("disableLegacyBoxcarringModeLabel").hidden = false;
        } else {
            document.getElementById("disableLegacyBoxcarringModeCheckbox").hidden = true;
            document.getElementById("disableLegacyBoxcarringModeLabel").hidden = true;
        }
    }
    function onLoadPage() {
        viewdisableLegacyBoxcarringMode();
    }
</script>

</fmt:bundle>
