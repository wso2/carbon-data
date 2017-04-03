<%--
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
--%>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<link rel="stylesheet" type="text/css" href="../js_scraper/yui/build/grids/grids.css">
<link rel="stylesheet" type="text/css" href="../js_scraper/yui/build/menu/assets/menu.css">
<link rel="stylesheet" type="text/css" href="../js_scraper/yui/build/menu/assets/skins/sam/menu.css">

<link rel="stylesheet" type="text/css" href="../js_scraper/js/yui/grids/grids.css">
<link rel="stylesheet" type="text/css" href="../js_scraper/js/yui/menu/assets/menu.css">

<style type="text/css">
    .scraper-config-section {
        background-color: #ffffff;
        text-align: center;
        height: 60%;
        border-color: darkgray;
        color: #333333;
        font-family: "Lucida Grande", "Lucida Sans Unicode", sans-serif;
        font-size: 0.9em;
        font-size-adjust: none;
        font-style: normal;
        font-variant: normal;
        font-weight: normal;
        line-height: 150%;
        cursor: default;
        direction: ltr;
    }

    .scraper-config-section1 {
        background-color: #ffffff;
        text-align: center;
        height: 60%;
        border-color: darkgray;
        color: #333333;
        font-family: "Lucida Grande", "Lucida Sans Unicode", sans-serif;
        font-size: 0.9em;
        font-size-adjust: none;
        font-style: normal;
        font-variant: normal;
        font-weight: normal;
        line-height: 150%;
        cursor: default;
        direction: ltr;
    }

    #main-menubar .bd {
        background: url( ../admin/images/table-header.gif ) bottom left repeat-x;
        border: 1px solid #CCCCCC;
        font-panchapancha
        weight: normal;
        height: 22px;
        width: auto;
    }

    #main-menubar .bd .bd {
        background-image: none;
        border: none;
        height: auto;
    }

    #main-menubar a:hover {
        color: #FFFFFF;
    }

    #scraper-config {
        height: 400px;
        width: 99.8%;
        border: 1px solid #CCCCCC;
        margin: 0px;
    }

    #page {
        padding: 10px;
        background-color: #FFFFFF;
    }

    #page-container {
        border: 1px solid #CCCCCC;
        padding: 10px;
        margin-top: 10px;
    }

    /* --------------- scraping assistant drop-down menu styles -------------------- */
    div.yuimenu li.selected, div.yuimenubar li.selected {
        background-color: #B5121B !important;
    }
</style>


<!-- YUI Dependencies-->
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu.js"></script>

<script type="text/javascript" src="../js_scraper/js/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../js_scraper/js/yui/container/container_core.js"></script>
<script type="text/javascript" src="../js_scraper/js/yui/menu/menu.js"></script>

<!-- XML Parser -->
<script type="text/javascript" src="../js_scraper/js/xml-for-script/tinyxmlsax.js"></script>
<script type="text/javascript" src="../js_scraper/js/xml-for-script/tinyxmlw3cdom.js"></script>

<!--WSO2 Dependencies-->
<%--<script type="text/javascript">
    var path = "<%=new URL(CarbonUIUtil.getServerURL(config.getServletContext(),session)).getPath()%>";
    var mashupServerURL = self.location.protocol + "//" + self.location.hostname + ":" +
                          self.location.port + path;
</script>--%>

<script type="text/javascript" src="../js_scraper/js/mashup-main.js"></script>
<script type="text/javascript" src="../js_scraper/js/services.js"></script>
<script type="text/javascript" src="../js_scraper/js/mashup.js"></script>
<script type="text/javascript" src="../js_scraper/js/mashup-utils.js"></script>
<script type="text/javascript" src="../js_scraper/js/common.js"></script>
<script type="text/javascript" src="../js_scraper/js/scraper.js"></script>
<script type="text/javascript">
    $("#page").ready(function() {
        wso2.mashup.Scraper.init();
    });
</script>

<jsp:include page="../dialog/display_messages.jsp"/>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">

    <carbon:breadcrumb
            label="scraper.headertext"
            resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <%
          String content = (String) session.getAttribute("web_harvest_config");
    %>
    <div id="middle">
        <h2><fmt:message key="scraper.headertext"/></h2>
              <form method="post" action="addDataSource.jsp" name="dataForm">
                    <div id="page">
                        <div id="simple-content">

                            <!-- Menu Bar -->
                            <div id="menu-bar"></div>
                            <!-- Scraper Config Generation Section -->
                            <div id="config-section" class="scraper-config-section"><span
                                    class="scraper-config-section1">
                                <textarea id="scraper-config" name="scraper-config" value="<%=content%>"></textarea>
                                </span>
                            </div>
                            <!-- XPath expression gathering section-->
                            <div id="page-container"></div>
                        </div>
                    </div>
          <table>
          <tr style="display:none">
             <td><input type="text" name="selectedType" id="selectedType" value="<%=request.getParameter("selectedType")%>"/> </td>
             <td><input type="text" name="configId" id="configId" value="<%=request.getParameter("configId")%>"/></td>
          </tr>
          <tr></tr>    
          <tr>
              <td class="buttonRow">
                <input class="button" name="save_button" type="submit" value="<fmt:message key="save"/>"/>
                <input class="button" name="cancel_button" type="button" value="<fmt:message key="cancel"/>"
                 onclick="location.href = 'addDataSource.jsp?configId='+document.getElementById('configId').value"/>
              </td>
          </tr>
        </table>
       </form>           
    </div>
</fmt:bundle>

