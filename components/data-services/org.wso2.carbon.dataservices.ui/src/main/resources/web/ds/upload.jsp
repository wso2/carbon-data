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
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<carbon:breadcrumb label="upload.dataservice"
		resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />

	<script type="text/javascript">
        function validate() {
            var jarinput = document.dbsUpload.dbsFilename.value;
            if (jarinput == '') {
                CARBON.showErrorDialog('<fmt:message key="select.dbs.service"/>');
            } else if (jarinput.lastIndexOf(".dbs") == -1) {
                CARBON.showErrorDialog('<fmt:message key="select.dbs.file"/>');
            } else {
                document.dbsUpload.submit();
            }
        }
    </script>

    <div id="middle">
        <h2><fmt:message key="upload.dataservice"/></h2>

        <div id="workArea">
            <form method="post" name="dbsUpload" action="../../fileupload/dbs"
                  enctype="multipart/form-data" target="_self">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="upload.dataservice"/> (.dbs)</th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRow">
                            <table class="normal">
                                <tr>
                                    <td>
                                        <label><fmt:message key="path.to.dataservice.config"/> (.dbs) :</label>
                                    </td>
                                    <td>
                                        <input type="file" id="dbsFilename" name="dbsFilename"
                                               size="75"/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input name="upload" type="button" class="button"
                                   value=" <fmt:message key="upload"/> "
                                   onclick="validate();"/>
                            <input type="button" class="button" onclick="location.href = '../service-mgt/index.jsp'"
                                   value=" <fmt:message key="cancel"/> "/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>