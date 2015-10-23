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
<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.*" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp"/>


<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">

<carbon:breadcrumb
        label="Add Operation"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<%
    String serviceName = request.getParameter("serviceName");
    if (serviceName != null && serviceName.trim().length() > 0) {
        String backendServerURL = CarbonUIUtil.getServerURL(config
                .getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session
                .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        try {
        } catch (Exception e) {
            String errorMsg = e.getLocalizedMessage();
%>
<script type="text/javascript">
    location.href = "dsErrorPage.jsp?errorMsg=<%=errorMsg%>";
</script>
<%
        }
        //return;
    }
%>

<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session">
</jsp:useBean>
<script type="text/javascript" src="js/ui-validations.js"></script>
<%
    ArrayList<Query> queries = dataService.getQueries();
    ArrayList<Query> xmlTypequeries = new ArrayList<Query>();
    ArrayList<WithParam> withParamsList = new ArrayList<WithParam>();
    boolean enableStreaming = true;
    boolean returnRequestStatus = false;
    boolean showReturnRequestStatus = false;
    for (int i = 0; i < queries.size(); i++) {
        if (queries.get(i).getResult() != null) {
            String outputType;
            if ((queries.get(i).getResult().getOutputType() == null) || (queries.get(i).getResult().getOutputType() == "")) {
                outputType = "xml";
            } else {
                outputType = queries.get(i).getResult().getOutputType();
            }
            if (!outputType.equals("rdf")) {
                xmlTypequeries.add(queries.get(i));
            }
        } else {
            xmlTypequeries.add(queries.get(i));
        }

    }
    String operationName = request.getParameter("operationName");
    String operationDesc = request.getParameter("operationDesc");
    String selectedQueryId = request.getParameter("selectedQueryId");
    String flag = request.getParameter("flag");
    String param = request.getParameter("param");
    String action = request.getParameter("action");
    String editParam = request.getParameter("editparam");
    Operation operation = null;
    if (operationName != null && operationName.trim().length() > 0) {
        operation = dataService.getOperation(operationName);
        if (operation != null) {
            CallQuery callQuery = operation.getCallQuery();
            if (callQuery != null) {
                if (selectedQueryId == null) {
                    //perhaps user is trying to change the associated query of an existing
                    //operation
                    selectedQueryId = callQuery.getHref();
                }
            }
        }
        if (selectedQueryId != null) {
            Query query = dataService.getQuery(selectedQueryId);
            if (query != null && query.getResult() == null) {
                showReturnRequestStatus = true;
            }
        }

    }
    flag = (flag == null) ? "" : flag;
    param = (param == null) ? "" : param;
    operationName = (operationName == null) ? "" : operationName;
    operationDesc = (operationDesc == null) ? "" : operationDesc;
    selectedQueryId = (selectedQueryId == null) ? "" : selectedQueryId;
    action = (action == null) ? "" : action;
    if (operation != null) {
        enableStreaming = !operation.isDisableStreaming();
        returnRequestStatus = operation.isReturnRequestStatus();
    }
    serviceName = dataService.getName();
    
    //Set Display Parameters
    if (editParam != null && editParam.equals("editparam") && selectedQueryId.equalsIgnoreCase(operation.getCallQuery().getHref())
        && operation.getCallQuery().getWithParams().size() > 0) {
        CallQuery callQuery = operation.getCallQuery();
            if (callQuery != null) {
                withParamsList = (ArrayList<WithParam>) callQuery.getWithParams();
            }
    } else if (selectedQueryId != null && selectedQueryId.trim().length() > 0) {
        Query query = dataService.getQuery(selectedQueryId);
        if (action.equals("")) {
            // when adding a new operation load all query params to operation params
            Param[] params = query.getParams();
            if (params != null) {
                if (params.length > 0) {
                    for (int a = 0; a < params.length; a++) {
                        Param qParam = params[a];
                        if(!qParam.getType().equals("OUT")){
                            WithParam withParam = new WithParam(qParam.getName(), qParam.getName(), "with-param");
                            withParam.setName(qParam.getName());
                            withParam.setParamType("with-param");
                            withParam.setParamValue(qParam.getName());
                            withParamsList.add(withParam);
                        }
                    }
                }
            }
        } else if (query != null && operation != null && action.equals("edit")) {
            // when editing a operation load only with-params in callQuery
            CallQuery callQuery = operation.getCallQuery();
//            if (!param.isEmpty() && !param.equals("qparam")) {
            if (callQuery != null && selectedQueryId.equalsIgnoreCase(callQuery.getHref()) && operation.getCallQuery().getWithParams().size() > 0) {
                withParamsList = (ArrayList<WithParam>) callQuery.getWithParams();
//                }
            } else {
                // change the selected query and click on load query params link
                Param[] params = query.getParams();
                if (params != null) {
                    if (params.length > 0) {
                        for (int a = 0; a < params.length; a++) {
                            Param qParam = params[a];
                            if(!qParam.getType().equals("OUT")){
                                WithParam withParam = new WithParam(qParam.getName(), qParam.getName(), "with-param");
                                withParam.setName(qParam.getName());
                                withParam.setParamType("with-param");
                                withParam.setParamValue(qParam.getName());
                                withParamsList.add(withParam);
                            }
                        }
                    }
                }
                callQuery.setHref(query.getId());
                callQuery.setWithParams(withParamsList);
            }
        }
    }
%>

<div id="middle">
    <h2>
        <%
            if (!selectedQueryId.equals("")) {
                if (!flag.equals("true")) {
        %>
        <fmt:message key="edit.operation"/><%out.write("(" + serviceName + "/" +operationName + ")");%>
        <% } else { %>
        <fmt:message key="add.new.operation"/> <%out.write("(" + serviceName + ")");%>
        <%
            }
        } else {%>
        <fmt:message key="add.new.operation"/> <%out.write("(" + serviceName + ")");%>
        <%}%></h2>

    <div id="workArea">
        <table class="styledLeft noBorders" id="dataSources" cellspacing="0" width="100%">
            <thead>
            <tr>
                <th colspan="2"><fmt:message key="service.operations"/></th>
            </tr>
            </thead>
            <form method="post" action="operationProcessor.jsp?action=<%=action%>" name="dataForm"
                  onsubmit="return validateAddOperationForm();">
                <!--hidden fields -->
                <input type="hidden" name="oldOperationName" value="<%=operationName%>">
                <input type="hidden" name="disableStreaming" value="<%=enableStreaming%>">

                <table class="styledLeft">
                    <tr>
                        <td>
                            <table class="normal">
                                <tr>
                                    <td><fmt:message key="operation.name"/><font
                                            color="red">*</font></td>
                                    <td><input type="text" name="operationName" id="operationName"
                                               value="<%=operationName%>"/></td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="opeation.description"/></td>

                                    <td><textarea cols="40" rows="5" id="operationDesc"
                                                  name="operationDesc"><%=operationDesc%>
                                    </textarea>
                                    </td>
                                </tr>

                                <tr id="addDistributedTransaction">
                                        <%--<td><carbon:tooltips image="magnifier.gif" description='aaa bbb ccc ddd eee fff' noOfWordsPerLine='10' > </carbon:tooltips></td>--%>
                                </tr>
                                <tr>
                                    <td><fmt:message key="query.id"/><font color="red">*</font></td>
                                    <td>
                                        <select name="queryId" id="queryId"
                                                onchange="javascript:location.href = 'addOperation.jsp?selectedQueryId='+this.options[this.selectedIndex].value+'&operationName='+document.getElementById('operationName').value+'&operationDesc='+document.getElementById('operationDesc').value+'&enableStreaming='+document.getElementById('enableStreaming').value+'&returnRequestStatus='+document.getElementById('returnRequestStatus').value+'&flag=true&action=<%=action%>';">
                                                <%--onchange="javascript:location.href = 'addOperation.jsp?selectedQueryId='+this.options[this.selectedIndex].value+'&operationName='+document.getElementById('operationName').value+'&operationDesc='+document.getElementById('operationDesc').value+'enableStreaming'+document.getElementById('enableStreaming').value+'&flag=true&action=<%=action%>';">--%>
                                            <% if (selectedQueryId != null && selectedQueryId.trim().equals("")) {%>
                                            <option value="" selected="selected"></option>
                                            <% } else {%>
                                            <option value=""></option>
                                            <% }%>

                                            <%
                                                if (xmlTypequeries != null && xmlTypequeries.size() > 0) {
                                                    Iterator iterator = xmlTypequeries.iterator();
                                                    while (iterator.hasNext()) {
                                                        Query query = (Query) iterator.next();
                                                        if (selectedQueryId != null && selectedQueryId.trim().equals(query.getId())) {
                                            %>
                                            <option value="<%=query.getId()%>"
                                                    selected="selected"><%=query.getId()%>
                                            </option>
                                            <%
                                            } else {
                                            %>
                                            <option value="<%=query.getId()%>"><%=query.getId()%>
                                            </option>
                                            <%
                                                        }
                                                    }
                                                }
                                            %>

                                        </select>
                                    </td>
                                </tr>
                                <!-- Display Parameters -->
                                <%
                                    if (withParamsList != null && withParamsList.size() > 0) {
                                            //Params exist.Draw column headers
                                %>
                                <tr>
                                    <td colspan="2"><b><fmt:message key="operation.parameters"/></b>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="2">
                                        <table class="styledInner" cellspacing="0"
                                               id="operationParametersTable">
                                            <tr>
                                                <td>
                                                    <b><fmt:message key="query.parameter.name"/></b>
                                                </td>
                                                <td>
                                                    <b><fmt:message key="operation.parameter.name"/></b>
                                                </td>
                                                <td>
                                                    <b><fmt:message key="actions" /></b>
                                                </td>
                                            </tr>

                                            <%
                                                for (WithParam aWithParamsList : withParamsList) {
                                                    //if (!params[a].getType().equals("OUT")) {
                                            %>
                                            <tr>
                                                <td><%=aWithParamsList.getName()%>
                                                </td>
                                                <td><%=(aWithParamsList.getParamValue())%>
                                                </td>
                                                <td>
                                                    <a class="icon-link"
                                                       style="background-image:url(../admin/images/edit.gif);"
                                                       href='addOperationParameter.jsp?editparam=editparam&operationName=<%=operationName%>&queryId=<%=selectedQueryId%>&paramNameId=<%=aWithParamsList.getName()%>&operationParamId=<%=(aWithParamsList.getParamValue())%>'><fmt:message key="edit"/> </a>
                                                    <a class="icon-link"
                                                       style="background-image:url(../admin/images/delete.gif);"
                                                       href="#"
                                                       onclick="deleteOperationParameters('<%=aWithParamsList.getName()%>','<%=operationName %>', '<%=operationName %>', '<%=selectedQueryId %>', '<%=operationDesc %>', '<%=enableStreaming %>', '<%=action%>');"><fmt:message
                                                            key="delete"/></a>
                                                </td>
                                            </tr>
                                            <%
                                                    //}
                                                }
                                            %>
                                        </table>
                                        <%
                                                    }
                                        %>
                                    </td>
                                </tr>

                                <tr>
                                    <td colspan="3">
                                        <a class="icon-link"
                                           style="background-image:url(../admin/images/add.gif);"
                                           href='addOperation.jsp?param=qparam&operationName=<%=operationName%>&operationDesc=<%=operationDesc %>&action=<%=action%>&selectedQueryId=<%=selectedQueryId%>' >Add Query Params as Operation Params</a>
                                    </td>
                                </tr>
                                <%
                                    if (!action.equals("")) { %>
                                <tr>
                                    <td colspan="3">
                                        <a class="icon-link"
                                           style="background-image:url(../admin/images/add.gif);"
                                           href='addOperationParameter.jsp?operationName=<%=operationName%>&action=<%=action%>&queryId=<%=selectedQueryId%>' ><fmt:message
                                                key="add.new.operation.parameter"/></a>
                                    </td>
                                </tr>
                                <% } %>

                                <tr>
                                    <td>
                                        <input type="checkbox" id="enableStreaming"
                                               name="enableStreaming"  <%=(enableStreaming) ? "checked=\"checked\"" : ""%>
                                               value=<%=enableStreaming%>/>
                                        <label for="enableStreaming"><fmt:message
                                                key="service.enable.streaming"/></label>
                                    </td>
                                </tr>
                                <tr <%= showReturnRequestStatus ? "" : "style='display:none'" %> >
                                    <td>
                                        <input type="checkbox" id="returnRequestStatus"
                                               name="returnRequestStatus"  <%=(returnRequestStatus) ? "checked=\"checked\"" : ""%>
                                               value=<%=returnRequestStatus%>/>
                                        <label for="returnRequestStatus"><fmt:message
                                                key="service.return.request.status"/></label>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>

                    <tr>
                        <td class="buttonRow" colspan="2"><input class="button"
                                                                 type="submit"
                                                                 value="<fmt:message key="save"/>"/>
                            <input class="button" type="button" value="<fmt:message key="cancel"/>"
                                    onclick="location.href = 'operations.jsp?ordinal=3'"/></td>
                    </tr>
                </table>
            </form>
        </table>
    </div>
</div>
<script type="text/javascript">
    alternateTableRows('operationParametersTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>