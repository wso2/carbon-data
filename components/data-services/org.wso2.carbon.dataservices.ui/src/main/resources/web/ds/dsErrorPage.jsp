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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
prefix="carbon"%>

<%
String message = request.getParameter("errorMsg");
%>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<carbon:breadcrumb
		label="dataservice.xml.editor"
		resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />
<div id="workArea">
<form>
<tr><td>
            <textarea id ="dsConfig" name="dsConfig"
                      style="background-color:lavender; width:99%;height:70px;*height:500px;
                      font-family:verdana;
                      font-size:15px;
                      color: red;
                      border:solid 1px #9fc2d5;
                      overflow-x:auto;
                      overflow-y:auto"><fmt:message>(<%=message%>)</fmt:message>
            </textarea>

        </td></tr>
</div>
</form>
</fmt:bundle>