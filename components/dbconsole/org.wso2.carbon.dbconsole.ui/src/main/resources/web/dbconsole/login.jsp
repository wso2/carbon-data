<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.owasp.encoder.Encode" %>
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

<!--

This is the UI component of the database console.
-->

<script type="text/javascript">
    function onPageLoad() {
        var url = document.getElementById("url").value;
        var driver = document.getElementById("driver").value;
        var user = document.getElementById("user").value;
        var password = document.getElementById("password").value;
        var flag = document.getElementById('flag').value;

        if (url == null) {
            url = '';
        }
        if (driver == null) {
            driver = '';
        }
        if (user == null) {
            user = '';
        }
        if (password == null) {
            password = '';
        }
        if (flag == null) {
            flag = '';
        }

        var doc = getDocFromIFrame();
        if (doc.forms['login'] != undefined & flag == 0) {
            doc.forms['login'].url.value = decodeURIComponent(url);
            doc.forms['login'].user.value = user;
            doc.forms['login'].password.value = password;
            doc.forms['login'].driver.value = decodeURIComponent(driver);
            document.getElementById('flag').value = 1;
        }
        init();
    }

    function init() {
        changeBackgroundColour();
        removePreferencesLink();
    }

    function getDocFromIFrame() {
        var page = document.getElementById("page1");
        if (navigator.userAgent.indexOf("MSIE 6.") != -1 ||
                navigator.userAgent.indexOf("MSIE 5.5") != -1) {
            return page.contentWindow;
        } else {
            return page.contentDocument;
        }
    }

    function changeBackgroundColour() {
        var doc = getDocFromIFrame();
        doc.body.style.background = "#FFFFFF";
    }

    function removePreferencesLink() {
        var doc = getDocFromIFrame();
        var links = doc.links;
        for (var i = 0; i < links.length; i++) {
            if (links[i].innerHTML == "Preferences") {
                links[i].innerHTML = "";
                break;
            }
        }
    }
</script>

<%
    String url = request.getParameter("url");
    String driver = request.getParameter("driver");
    String userName = request.getParameter("userName");

    url = (url != null) ? url : "";
    driver = (driver != null) ? driver : "";
    userName = (userName != null) ? userName : "";

    String password = "";
%>

<div id="middle">
    <h2>Database Console</h2>

    <div id="workArea" style="padding:0">
        <form action="#">
            <input type="hidden" id="url" name="url" value="<%=Encode.forHtmlContent(url)%>"/>
            <input type="hidden" id="user" name="user" value="<%=Encode.forHtmlContent(userName)%>"/>
            <input type="hidden" id="password" autocomplete="off" name="password" value="<%=Encode.forHtmlContent(password)%>"/>
            <input type="hidden" id="driver" name="driver" value="<%=Encode.forHtmlContent(driver)%>"/>
            <input type="hidden" id="flag" name="flag" value=0>
            <iframe onload='javascript: onPageLoad();' id="page1" name="inlineframe"
                    src="../../dbconsole/login.jsp" frameborder="0" scrolling="no" width="1063"
                    height="1000" marginwidth="5" marginheight="5"></iframe>

        </form>
    </div>
</div>
