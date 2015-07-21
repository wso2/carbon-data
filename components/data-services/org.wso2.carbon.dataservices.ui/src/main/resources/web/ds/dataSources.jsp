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
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Config" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<carbon:breadcrumb
		label="Datasources"
		resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"></jsp:useBean>
<script type="text/javascript" src="js/ui-validations.js"></script>
<%
    boolean finishEnable = false;

%>


<div id="middle">
<h2><fmt:message key="datasources.heading"/></h2>
	<div id="workArea">
	<form method="post" action="queries.jsp" name="dataForm"
		onsubmit="return validateDataSourcesForm();">
        <table class="styledLeft" id="datasource-table">
            <%
                ArrayList<Config> configs = dataService.getConfigs();
                if (configs != null && configs.size() > 0) {
                    String configId = null;
            %>
            <thead>
            <tr>
                <th width="20%"><fmt:message key="datasource.name"/></th>
                <%--<th width="20%"><fmt:message key="datasource.type"/></th>--%>
                <th width="60%"><fmt:message key="actions"/></th>
            </tr>
            </thead>
            <tbody>

            <%
                Iterator iterator = configs.iterator();
                while (iterator.hasNext()) {
                    Config dsConfig = (Config) iterator.next();
                    if (dsConfig != null) {
                        finishEnable = true;
                        configId = dsConfig.getId();
            %>

            <tr>
                <td><%=configId%>
                </td>
                <input type="hidden" id="<%=configId%>" name="configId" value="<%=configId%>" />
                <%--<td><%=dsConfig.getDataSourceType()%>--%>
                <%--</td>--%>
                <td>
                    <a class="icon-link" style="background-image:url(../admin/images/edit.gif);" href="addDataSource.jsp?flag=edit&configId=<%=configId%>"><fmt:message
                            key="edit.datasource"/></a>
                    <a class="icon-link" style="background-image:url(../admin/images/delete.gif);" onclick="deleteDatasource(document.getElementById('<%=configId%>').value);" href="#"><fmt:message
                            key="delete.datasource"/></a>
                </td>
            </tr>

            <%
                        }
                    }
                }
            %>
            <tr>
                <td colspan="2">
                    <a class="icon-link" style="background-image:url(../admin/images/add.gif);"
                       href="addDataSource.jsp"><fmt:message
                            key="add.new.datasource"/></a>
                </td>
            </tr>
            <tr>
                <td class="buttonRow" colspan="2"><input class="button" type="button" value="< <fmt:message key="back"/>" onclick="location.href = 'serviceDetails.jsp?flag=back' "/>
                                                         <%--value="< <fmt:message key="back"/>" onclick="location.href = 'serviceDetails.jsp?serviceName=<%=serviceName%>&description=<%=description%>&txManagerClass=<%=txManagerClass%>&txManagerName=<%=txManagerName%>&batchResponse=<%=batchRequest%>&enableXA=<%=enableXA%>&isUseAppServerTS=<%=isUseAppServerTS%>&enableBoxcarring=<%=boxcarring%>&enableStreaming=<%=enableStreaming%>&protectedTokens=<%=protectedTokens%>&passwordProvider=<%=passwordProvider%>&txManagerCleanupMethod=<%=txManagerCleanupMethod%>&serviceNamespace=<%=serviceNamespace%>&flag=back&ordinal=1' "/> --%>
                    <input class="button" type="submit" value="<fmt:message key="next"/> >"/>
                    <% if(finishEnable){ %>
                        <input class="button" type="button" value="<fmt:message key="finish"/>" onclick="location.href = 'wizardDoneProcessor.jsp'"/>
                        <input class="button" type="button" value="<fmt:message key="save.as.draft"/>" onclick="location.href = 'wizardDoneProcessor.jsp?flag=wip'"/>
                    <% }else{ %>
                        <input class="button" type="button" value="<fmt:message key="finish"/>" disabled="disabled"  onclick="location.href = 'wizardDoneProcessor.jsp'"/>
                    <% } %>
                    <input
                            class="button" type="button" value="<fmt:message key="cancel"/>"
                            onclick="location.href = '../service-mgt/index.jsp'"/></td>
            </tr>
            </tbody>
        </table>
    </form>
	</div>
	</div>
</fmt:bundle>