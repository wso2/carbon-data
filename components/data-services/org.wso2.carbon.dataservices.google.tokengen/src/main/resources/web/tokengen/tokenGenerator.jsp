<!--
~
~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
~
-->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.dataservices.google.tokengen.ui.TokenGenClient" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="com.google.api.client.auth.oauth2.Credential" %>
<%@ page import="org.wso2.carbon.dataservices.common.DBConstants" %>
<script type="text/javascript" src="js/tokenGenUtil.js"></script>
<fmt:bundle basename="org.wso2.carbon.dataservices.google.tokengen.ui.i18n.Resources">

    <carbon:breadcrumb label="dataservices.google.tokenGen.header"
                       resourceBundle="org.wso2.carbon.dataservices.google.tokengen.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <form method="post" name="tokenGenForm" id="tokenGenForm" action="tokenGenerator.jsp"
          onsubmit="return reDirectToConsent();">

        <input type="hidden" name="firstRequest" id="firstRequest" value="true"/>

        <div id="middle">
            <h2><fmt:message key="dataservices.tokengen.header"/></h2>

            <div id="workArea">

                <table id="clientDataTable" class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
                    <thead>
                    <tr>
                        <th colspan="3"><fmt:message
                                key="dataservices.tokengen.credentials"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td style="width:150px"><fmt:message key="dataservices.tokengen.clientId"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <input id="<%=DBConstants.GSpread.CLIENT_ID%>" name="<%=DBConstants.GSpread.CLIENT_ID%>"
                                   value="" type="text"/>
                        </td>
                    </tr>
                    <tr>
                        <td style="width:150px"><fmt:message key="dataservices.tokengen.clientSecret"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <input id="<%=DBConstants.GSpread.CLIENT_SECRET%>"
                                   name="<%=DBConstants.GSpread.CLIENT_SECRET%>" value="" type="text"/>
                        </td>
                    </tr>
                    <tr>
                        <td style="width:150px"><fmt:message key="dataservices.tokengen.redirectURIs"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <input id="<%=DBConstants.GSpread.REDIRECT_URIS%>"
                                   name="<%=DBConstants.GSpread.REDIRECT_URIS%>"
                                   value="<%=new TokenGenClient().getDefaultRedirectURL()%>"
                                   type="text"/>
                        </td>
                    </tr>

                    </tbody>
                </table>
                <table id="tokenTable" class="styledLeft noBorders" style="display:none" cellspacing="0" cellpadding="0"
                       border="0">

                    <tbody>

                    <tr>
                        <td style="width:150px"><fmt:message key="dataservices.tokengen.accessToken"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <input type="text" id="<%=DBConstants.GSpread.ACCESS_TOKEN%>"
                                   name="<%=DBConstants.GSpread.ACCESS_TOKEN%>" value=""/>
                        </td>
                    </tr>
                    <tr>
                        <td style="width:150px"><fmt:message key="dataservices.tokengen.refreshToken"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <input type="text" id="<%=DBConstants.GSpread.REFRESH_TOKEN%>"
                                   name="<%=DBConstants.GSpread.REFRESH_TOKEN%>" value=""/>
                        </td>
                    </tr>
                    </tbody>
                </table>

                <table id="tokenTable" class="styledLeft noBorders" cellspacing="0" cellpadding="0"
                       border="0">

                    <tbody>
                    <tr>
                        <td class="buttonRow" colspan="3">
                            <input class="button" type="submit"
                                   value="<fmt:message key="dataservices.tokengen.button.text"/>"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <div id="tdiv"></div>
        </div>

    </form>

    </div>


</fmt:bundle>
