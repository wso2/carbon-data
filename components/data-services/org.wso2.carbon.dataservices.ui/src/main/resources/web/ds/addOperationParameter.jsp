<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>

<carbon:breadcrumb
        label="add.operation.param"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="js/ui-validations.js"></script>
<%
    String paramName = request.getParameter("paramNameId");
    String operationParamName = request.getParameter("operationParamId");
    String editparam = request.getParameter("editparam");
    String editParamName = request.getParameter("paramNameId");
    String operationName = request.getParameter("operationName");
    String action = request.getParameter("action");
    String queryId = request.getParameter("queryId");
    editparam = (editparam == null) ? "add" : editparam;
    String serviceName = dataService.getName();
    String operationsDesc = request.getParameter("operationsDesc");
    String disableStreaming = request.getParameter("disableStreaming");
    //boolean isEdit = false;
    paramName = (paramName == null) ? "" : paramName;
    operationParamName = (operationParamName == null) ? "" : operationParamName;
    action = (action == null) ? "" : action;
    editParamName = (editParamName == null) ? "" : editParamName;
    operationsDesc = (operationsDesc == null) ? "" : operationsDesc;
    disableStreaming = (disableStreaming == null) ? "" : disableStreaming;

%>

<div id="middle">
    <%
    if (editparam.equals("editparam")) {%>
    <h2>
        <fmt:message key="edit.operation.parameter"/>
        <% out.write(" (" + serviceName + "/" + operationName + ")"); %>
    </h2>
    <% } else { %>
    <h2>
        <fmt:message key="add.new.operation.parameter"/>
        <% out.write(" (" + serviceName +"/" + operationName + ")");%>
    </h2>
        <%
    } %>
<div id="workArea">
    <table class="styledLeft noBorders" id="dataSources" cellspacing="0" width="100%">
        <%--<input type="hidden" id="flag" name="flag" value="<%=flag%>"/>--%>
        <input type="hidden" id="editParamName" name="editParamName" value="<%=editParamName%>"/>
        <thead>
        <tr>
            <th colspan="2"><fmt:message key="operation.parameters"/></th>
        </tr>
        </thead>

        <form method="post" id="dataForm" name="dataForm"
              action="operationParamsProcessor.jsp"
              onsubmit="return validateOperationParamForm()">

            <input type="hidden" name="operationName" value="<%=operationName%>">
            <input type="hidden" name="queryId" value="<%=queryId%>">

            <tr>
                <td class="leftCol-small">Param Name<font
                        color="red">*</font></td>
                <td>
                    <input value="<%=paramName%>" name="paramNameId" id="paramNameId"
                           size="30"
                           type="text"/>

                </td>
            </tr>
            <tr>
                <td class="leftCol-small">Operation Param Name<font
                        color="red">*</font></td>
                <td>
                    <input value="<%=operationParamName%>" name="operationParamId" id="operationParamNameId"
                           size="30"
                           type="text"/>

                </td>
            </tr>
            <tr>
                <td class="buttonRow" colspan="2">
                    <%--<input class="button" type="button" value="Cancel"--%>
                    <%--onclick="location.href='operations.jsp'"/>--%>
                    <input class="button" type="submit" value="Add"
               onclick="document.dataForm.action = 'operationParamsProcessor.jsp?flag=add&operationName=<%=operationName%>&action=<%=action%>&editParamName=<%=editParamName%>'"/>
               		<input class="button" type="button" value="Cancel"
                           onclick="location.href='addOperation.jsp?action=edit&operationName=<%=operationName%>&operationDesc=<%=operationsDesc%>&disableStreaming=<%=disableStreaming%>'"/>
                </td>
            </tr>

            <%--onclick="document.dataForm.action = 'operationParamsProcessor.jsp?flag=add&operationName='+<%=operationName%> + '&action=<%=action%>'"--%>
            <%--onclick="document.dataForm.action = 'operationParamsProcessor.jsp?operationName='<%=operationName%> + '&paramNameId=<%= paramName%>' + '&operationParamNameId=<%= operationParamName%>>';return validateOperationParamForm();"/>--%>
            </form>
        </table>
    </div>
</div>
</fmt:bundle>