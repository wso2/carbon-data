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
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Param" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Query" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Validator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ArrayList" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<!--
<carbon:breadcrumb
        label="Add/Edit Input Mapping"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
-->
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session">
</jsp:useBean>
<jsp:useBean id="validators" class="java.util.ArrayList" scope="session" />
<%
    String queryId = request.getParameter("queryId");
    String paramName = request.getParameter("paramName");

    String defaultValue = request.getParameter("defaultValue");
    String caption;
    Query query = null;

    String sparqlType = request.getParameter("sqlType");
    boolean disable;
    if (queryId != null) {
        query = dataService.getQuery(queryId);
        if (query.getParam(paramName) != null) {
            Param param = query.getParam(paramName);
            /* editing - set the validators in the session */
            session.setAttribute("validators", param.getValidators());
            validators = (ArrayList) session.getAttribute("validators");

            sparqlType = param.getSqlType();
           
           
            defaultValue = param.getDefaultValue();
        }
    }
    paramName = (paramName == null) ? "" : paramName;

    defaultValue = (defaultValue == null) ? "" : defaultValue;
    disable = (paramName != null);
    sparqlType = (sparqlType == null) ? "" : sparqlType;

  
    if(!paramName.equals("")){
        caption = "save";
    }else{
        caption = "add";
    }


%>
<script type="text/javascript" src="js/ui-validations.js"></script>
<div id="middle">
<h2><fmt:message key="add.input.mapping"/></h2>

<div id="workArea">
    <%--    <form method="post" id="inputMappings" name="inputMappings" action="inputMappingProcessor.jsp" ">--%>
<form method="post" id="sparqlInputMappings" name="sparqlInputMappings" action="sparqlInputMappingProcessor.jsp" onsubmit="return validateSparqlInputMappings();">
<input type="hidden" name="queryId" value="<%=queryId%>" id="<%=queryId%>"/>
<input type="hidden" id="dsValidatorProperties" name="dsValidatorProperties" class="longInput"/>
<table class="styledLeft">
<tr>
<td>
<table class="normal">
<tr id="addNewSparqlInputMappingRow">
<!--<tr>-->
<td>
<table>
<input value="<%=request.getParameter("data_source")%>" name="datasource" id="datasource" size="30" type="hidden">
    <%--<input value="<%=request.getParameter("query_id")%>" name="queryId" id="queryId" size="30" type="hidden">--%>
<input value="<%=request.getParameter("sql_stat")%>" name="sql" id="sql" size="30" type="hidden">

<tr>
    <td><fmt:message key="datasources.mapping.name"/><font color="red">*</font></td>
    <td><% if (paramName.equals("")) { %>
        <input value="<%=paramName%>" name="inputMappingId" id="inputMappingNameId" size="30" type="text"/>
        <% } else { %>
        <input value="<%=paramName%>" name="inputMappingId" id="inputMappingNameId" size="30" type="text"
               readonly="readonly"/>
        <% } %>
    </td>
</tr>

<tr>
    <td><fmt:message key="datasources.sparql.type"/><font color="red">*</font></td>
    <td><select id="inputMappingSqlTypeId" name="inputMappingSqlType">
       <% if (sparqlType.equals("")) { %>
        <option value="" selected="selected">--SELECT--</option>
        <% } else { %>
        <option value="">--SELECT--</option>
        <% }
            if (sparqlType.equals("STRING")) { %>
        <option value="STRING" selected="selected">STRING</option>
        <% } else { %>
        <option value="STRING">STRING</option>
        <% }
            if (sparqlType.equals("INTEGER")) { %>
        <option value="INTEGER" selected="selected">INTEGER</option>
        <% } else { %>
        <option value="INTEGER">INTEGER</option>
        <% } 
           if (sparqlType.equals("FLOAT")) { %>
            <option value="FLOAT" selected="selected">FLOAT</option>
        <% } else { %>
            <option value="FLOAT">FLOAT</option>
        <% }
            if (sparqlType.equals("DOUBLE")) { %>
        <option value="DOUBLE" selected="selected">DOUBLE</option>
        <% } else { %>
        <option value="DOUBLE">DOUBLE</option>
        <% }
            if (sparqlType.equals("DECIMAL")) { %>
            <option value="DECIMAL" selected="selected">NUMERIC</option>
        <% } else { %>
            <option value="DECIMAL">DECIMAL</option>
        <% }
            if (sparqlType.equals("LONG")) { %>
            <option value="LONG" selected="selected">LONG</option>
        <% } else { %>
            <option value="LONG">LONG</option>
        <% }
            if (sparqlType.equals("BOOLEAN")) { %>
            <option value="BOOLEAN" selected="selected">BOOLEAN</option>
        <% } else { %>
            <option value="BOOLEAN">BOOLEAN</option>
        <% }
            if (sparqlType.equals("DATE")) { %>
        <option value="DATE" selected="selected">DATE[yyyy-mm-dd]</option>
        <% } else { %>
        <option value="DATE">DATE[yyyy-mm-dd]</option>
        <% }
            if (sparqlType.equals("TIME")) { %>
        <option value="TIME" selected="selected">TIME[hh:mm:ss]</option>
        <% } else { %>
        <option value="TIME">TIME[hh:mm:ss]</option>
        <% }
            if (sparqlType.equals("DATETIME")) { %>
        <option value="DATETIME" selected="selected">DATETIME</option>
        <% } else { %>
        <option value="DATETIME">DATETIME</option>
        <% } 
            if (sparqlType.equals("GYEARMONTH")) { %>
        <option value="GYEARMONTH" selected="selected">GYEARMONTH</option>
        <% } else { %>
        <option value="GYEARMONTH">GYEARMONTH</option>
        <% }
            if (sparqlType.equals("GYEAR")) { %>
        <option value="GYEAR" selected="selected">GYEAR</option>
        <% } else { %>
        <option value="GYEAR">GYEAR</option>
        <% }
            if (sparqlType.equals("GMONTHDAY")) { %>
        <option value="GMONTHDAY" selected="selected">GMONTHDAY</option>
        <% } else { %>
        <option value="GMONTHDAY">GMONTHDAY</option>
        <% }            
            if (sparqlType.equals("GDAY")) { %>
        <option value="GDAY" selected="selected">GDAY</option>
        <% } else { %>
        <option value="GDAY">GDAY</option>
        <% }                    
            if (sparqlType.equals("GMONTH")) { %>
        <option value="GMONTH" selected="selected">GMONTH</option>
        <% } else { %>
        <option value="GMONTH">GMONTH</option>
        <% }            
            if (sparqlType.equals("BASE64BINARY")) { %>
        <option value="BASE64BINARY" selected="selected">BASE64BINARY</option>
        <% } else { %>
        <option value="BASE64BINARY">BASE64BINARY</option>
        <% } 
           if (sparqlType.equals("HEXBINARY")) { %>
        <option value="HEXBINARY" selected="selected">HEXBINARY</option>
        <% } else { %>
        <option value="HEXBINARY">HEXBINARY</option>
        <% } 
            if (sparqlType.equals("ANYURI")) { %>
        <option value="ANYURI" selected="selected">URI</option>
        <% } else { %>
        <option value="ANYURI">URI</option>
        <% }
           if (sparqlType.equals("QNAME")) { %>
        %>
        <option value="QNAME" selected="selected">QNAME</option>
        <% } else { %>
        <option value="QNAME">QNAME</option>
        <% } %>
    </select></td>
</tr>



    <td><b><fmt:message key="dataservices.add.validations"/></b></td>
</tr>
<tr>
    <td><fmt:message key="dataservices.validator"/> </td>
    <td><select id="validatorList" name="validatorList" onchange="changeAddValidatorFields(this,document);">
        <option value="">--Select--</option>
        <option value="validateLongRange">Long Range Validator</option>
        <option value="validateDoubleRange">Double Range Validator</option>
        <option value="validateLength">Length Validator</option>
        <option value="validatePattern">Pattern Validator</option>
        <option value="validateCustom">Custom Validator</option>
        </select> </td>
</tr>
<div id="validators" style="display:none">
    <table>
        <tr id="maxRangeValidatorElementsRow" style="display:none">
            <td><fmt:message key="dataservice.range.validator.max"/></td>
            <td><input type="text" id="max" name="max" size="15"></td>
        </tr>
        <tr id="minRangeValidatorElementsRow" style="display:none">
            <td><fmt:message key="dataservice.range.validator.min"/></td>
            <td><input type="text" id="min" name="min" size="15"></td>
        </tr>
        <tr id="patternValidatorElementsRow" style="display:none">
            <td><fmt:message key="dataservice.validator.pattern"/></td>
            <td><input type="text" id="pattern" name="pattern" size="30"></td>
        </tr>
        <tr id="customValidatorElementsRow" style="display:none">
            <td><fmt:message key="dataservice.validator.custom.class"/></td>
            <td><input type="text" id="customClass" name="customClass" size="30"></td>
        </tr>
        <tr id="customValidatorPropertyElementsRow" style="display:none">
            <td>
                <fmt:message key="custom.properties"/>
            </td>
            <td>
                <div id="nameValueAdd">
                    <a class="icon-link"
                       href="#addNameLink"
                       onclick="addValidatorProperties();"
                       style="background-image: url(../admin/images/add.gif);"><fmt:message
                            key="add.new.validator.properties"/></a>

                    <div style="clear:both;"></div>
                </div>
                <div>
                    <table cellpadding="0" cellspacing="0" border="0" class="styledLeft"
                           id="dsValidatorPropertyTable"
                           style="display:none;">
                        <thead>
                        <tr>
                            <th style="width:40%"><fmt:message key="validator.prop.name"/></th>
                            <th style="width:40%"><fmt:message key="validator.prop.value"/></th>
                            <th style="width:20%"><fmt:message key="validator.prop.action"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </td>
        </tr>
        <tr>
            <td><input type="submit" style="display:none" id="addValidator"
                       value="<fmt:message key="dataservices.addValidator"/>" class="button"
                       onclick="addValidatorsForSparqlInput();"/>
                        <%--onclick="validateValidators(this,document);"/>--%>
            </td>
        </tr>
    </table>
</div>
<tr>
    <td colspan="2">
       <table class="styledInner" cellspacing="0" id="existingValidators">
           <%
               if (validators.size() == 0) {
           %>
           <tr>
               <td colspan="3"><fmt:message key="dataservices.there.are.no.validations"/></td>
           </tr>
           <%   } else { %>
           <tr>
               <td><b><fmt:message key="dataservices.validator"/></b></td>
               <td><b><fmt:message key="dataservices.validation.value"/></b></td>
               <td><b><fmt:message key="datasources.action"/></b></td>
           </tr>
               <% for (Object tmpVal : validators) {
                      Validator valObj = (Validator) tmpVal;
               %>
               <tr>
                   <td><%=valObj.getName()%></td>
                   <td><%=valObj.getPropertiesString()%></td>
                   <td>
                       <a class="icon-link" style="background-image:url(../admin/images/delete.gif);"
                          href="inputMappingProcessor.jsp?queryId=<%=queryId%>&validatorList=<%=valObj.getElementName()%>&inputMappingId=<%=paramName%>&inputMappingSqlType=<%=sparqlType%>&defaultValue=<%=defaultValue%>&flag=deleteValidator&origin=addrdf">
                           <fmt:message key="delete"/></a>
                   </td>
               </tr>
           <%    }
             }
           %>
       </table>
    </td>
</tr>

<tr>
    <td colspan="2">
        <table class="styledInner" cellspacing="0" id="existinginputMappingsTable">
            <% if (paramName.equals("")) {
                if (queryId == null) {
            %>
            <tr>
                <td colspan="3"><fmt:message key="datasources.no.inputmapping"/></td>
            </tr>
            <% } else {
                Param[] params = query.getParams();
                if (params != null) {
            %>
            <tr>
                <td><b><fmt:message key="datasources.mapping.name"/></b></td>
                <td><b><fmt:message key="datasources.sparql.type"/></b></td>
                <td><b><fmt:message key="dataservices.default.value"/></b></td>
                <td><b><fmt:message key="datasources.action"/></b></td>
            </tr>
            <% for (int a = 0; a <= params.length - 1; a++) { %>
            <tr>
                <input type="hidden" id="<%=params[a].getName()%>" name="<%=params[a].getName()%>"
                       value="<%=params[a].getName()%>"/>
                <input type="hidden" id="<%=params[a].getSqlType()%>" name="<%=params[a].getSqlType()%>"
                       value="<%=params[a].getSqlType()%>"/>
                <td><%=params[a].getName()%>
                </td>
                <td><%=params[a].getSqlType()%>
                </td>
                <td><% if(params[a].getDefaultValue() != null){ %>
                <%=params[a].getDefaultValue()%>
                <% }
                %>
                  
                </td>
                <td>
                    <a class="icon-link" style="background-image:url(../admin/images/edit.gif);"
                       href="addSparqlInputMapping.jsp?paramName=<%=params[a].getName()%>&queryId=<%=queryId%>&paramType=<%="SCALAR"%>"><fmt:message
                            key="edit"/></a>
                            
                   <a class="icon-link" style="background-image:url(../admin/images/delete.gif);" href="#"
                       onclick="deleteInputMappings(document.getElementById('<%=params[a].getName()%>').value,
                       document.getElementById('<%=params[a].getSqlType()%>').value,
                       document.getElementById('<%=queryId%>').value, 'rdf');"><fmt:message
                            key="delete"/></a>
                </td>
            </tr>
            <% //  }
            }

            } else {
            %>
            <tr>
                <td colspan="3">
                    <fmt:message key="datasources.no.inputmapping"/>
                </td>
            </tr>
            <%
                        }
                    }
                }
            %>
        </table>
    </td>
</tr>
</table>
</td>
</tr>
</table>
</td>
</tr>
<tr>
    <td class="buttonRow" colspan="2"><input class="button" type="button" value="<fmt:message key="mainConfiguration"/>"
                                             onclick="redirectToMainConfiguration(document.getElementById('<%=queryId%>').value);"/>
        <input class="button" type="submit" value="<fmt:message key="<%=caption%>"/>"
               onclick="document.sparqlInputMappings.action = 'sparqlInputMappingProcessor.jsp?flag=add'"/>
    </td>
</tr>
</table>

</form>
</div>
</div>
<script type="text/javascript">
    alternateTableRows('existinginputMappingsTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>