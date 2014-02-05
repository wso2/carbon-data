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
<carbon:breadcrumb label="contract.first.dataservice"
		resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />
    <script type="text/javascript" src="js/ui-validations.js"></script>
    <script type="text/javascript">
        function validateWSDL() {
            var jarinput = document.wsdlUpload.wsdlUrl.value;
            var jarinputFile = document.wsdlUpload.wsdlFile.value;

            if (jarinput == '' && jarinputFile == '') {
                CARBON.showErrorDialog('<fmt:message key="select.wsdl.service"/>');
                return false;
            } else if (jarinput == '' && jarinputFile.lastIndexOf(".wsdl") == -1) {
                CARBON.showErrorDialog('<fmt:message key="select.wsdl.file"/>');
                return false;
            } else if (jarinput == '' && jarinputFile.endsWith('.wsdl')) {
                document.wsdlUpload.submit();
                return false;
            } else if (jarinput.length > 0) {               
                    location.href = 'wsdlUploadProcessor.jsp?wsdlUrl=' + document.getElementById('wsdlUrl').value;
                    return false;             
                //}
            }
            return false;
        }
    </script>
	<div id="middle">
        <h2><fmt:message key="dataservices.add.contract.first.url"/></h2>            
        <div id="workArea">
            <form method="post" name="wsdlUpload" id="wsdlUpload" action="../../fileupload/contractFirst"
                  enctype="multipart/form-data" target="_self" onsubmit="return validateWSDL();">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="dataservices.add.contract.first.url"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRow">
                            <table class="normal">
                                <tr><td><fmt:message key="file.type" /> : </td>
                                    <td><input type="radio" name="fileType"  value="file" checked="checked" onchange="changeFileType(this, document);"><fmt:message key="file"/>
                                        <input type="radio" name="fileType"  value="url" onchange="changeFileType(this,document);"><fmt:message key="url"/>
                                    </td>
                                </tr>
                                <tr id="fileRow">
                                    <td>
                                        <label><fmt:message key="dataservices.contract.first.wsdl.file"/></label>
                                    </td>
                                    <td>
                                        <input type="file" id="wsdlFile" name="wsdlFile" size="75"/>
                                    </td>
                                </tr>
                                <tr style="display:none" id="urlRow" >
                                    <td>
                                        <label><fmt:message key="dataservices.contract.first.wsdl.uri"/> </label>
                                    </td>
                                    <td>
                                        <input type="text" id="wsdlUrl" name="wsdlUrl" size="75">
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input name="upload" type="submit" class="button"
                                   value=" <fmt:message key="upload"/> "/>
                            <input type="button" class="button" onclick="location.href = '../service-mgt/index.jsp'"
                                   value=" <fmt:message key="cancel"/> "/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>

</fmt:bundle>