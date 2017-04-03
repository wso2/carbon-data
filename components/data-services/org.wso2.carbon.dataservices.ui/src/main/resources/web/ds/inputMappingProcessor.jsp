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
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Param" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Query" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Validator" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>
<jsp:useBean id="validators" class="java.util.ArrayList" scope="session" />
<%
    String serviceName = request.getParameter("serviceName");
    String queryId = request.getParameter("queryId");    
    String paramName = request.getParameter("inputMappingId");
    String sqlType = request.getParameter("inputMappingSqlType");
    String inOutType = request.getParameter("inputMappingInOutType");
    String ordinalStr = request.getParameter("inputMappingOrdinal");
    String paramType = request.getParameter("paramType");
    String defaultValue = request.getParameter("defaultValue");
    String flag = request.getParameter("flag");
    String oldParamName = request.getParameter("oldInputMappingId");
    String origin = request.getParameter("origin");
    String validateElementName = request.getParameter("validatorList");
    String valMin = request.getParameter("min");
    String valMax = request.getParameter("max");
    String valPattern = request.getParameter("pattern");
    String valCustomClass = request.getParameter("customClass");
    String structType = request.getParameter("structType");
    String dsValidatorProperties = request.getParameter("dsValidatorProperties");
    paramType = (paramType == null ) ? "SCALAR" : paramType;
    sqlType = (sqlType == null) ? "" : sqlType;
    paramName = (paramName == null) ? "" : paramName.trim();
    oldParamName = (oldParamName == null) ? "": oldParamName.trim();
    flag = (flag == null) ? "" : flag;
    origin = (origin == null) ? "" : origin;
    structType = (structType == null) ? "" : structType;
    Query query = dataService.getQuery(queryId);
    boolean addValidation = true;
   
    /* add validator button pressed */
    if (flag.equals("validate")) {
        Map<String, String> fields = new HashMap<String, String>();
        Map<String, String> customProperties = new HashMap<String, String>();
        
        
        if (validateElementName.equals("validateLongRange") ||
        		validateElementName.equals("validateDoubleRange") ||
                validateElementName.equals("validateLength")) {
        	fields.put("minimum", valMin);
            fields.put("maximum", valMax);
        } else if (validateElementName.equals("validatePattern")) {
        	fields.put("pattern", valPattern);
        } else if (validateElementName.equals("validateCustom")) {
            fields.put("class", valCustomClass);
            if (dsValidatorProperties != null) {
                String[] propsList = dsValidatorProperties.split("::");
                for (int i = 0; i < propsList.length; i++) {
                    String[] property = propsList[i].split(",");
                    if (property.length == 2) {
                        customProperties.put(property[0], property[1]);
                    }
                }
            }
        }
        
        Iterator<Validator> itr = (Iterator<Validator>) validators.iterator();
        Validator tmpVal;
        while (itr.hasNext()) {
           tmpVal = itr.next();
           if (tmpVal.getElementName().equals(validateElementName)) {
        	   /* edit validator */
               tmpVal.setValidatorElements(fields);
               tmpVal.setCustomProperties(customProperties);
               addValidation = false;
               break;
           }
        }
        if (addValidation) {
            validators.add(new Validator(validateElementName, fields, customProperties));
         }
    } else if (flag.equals("deleteValidator")) {
            Iterator<Validator> itr = (Iterator<Validator>) validators.iterator();
            Validator tmpVal;
            while (itr.hasNext()) {
                tmpVal = itr.next();
                if (tmpVal.getElementName().equals(validateElementName)) {
                    itr.remove();
                    break;
                }
            }
            Param param = query.getParam(paramName);
            if(param != null) {
                param.setValidarors(validators);
            }    
    }else { 	
        if(query != null){
            if(queryId != null){
                if(!paramName.equals("")){
                    if(!sqlType.equals("")){
                        Param param;
                        if(query.getParam(paramName) != null || query.getParam(oldParamName) != null){
                            if (flag.equals("deleterdf") || flag.equals("deletesql") || flag.equals("delete") ) {
                               if (query.getParam(paramName) != null)  {
                            	   param = query.getParam(paramName);
                                   ArrayList<Param> paramList = new ArrayList<Param>();
                                   Param[] oldParams = query.getParams();
                                   for (int a=0; a < oldParams.length; a++) {
                                       paramList.add(a,oldParams[a]);
                                   }
                                   Param[] params = new Param[paramList.size()-1];
                                   for(int a=0; a < paramList.size() ; a++){
                                     if(oldParams[a].getName().equals(paramName)){
                                        paramList.remove(a);
                                     }
                                   }
                                   paramList.toArray(params);
                                   query.setParams(params);   
                               }
                                                                              
                             }else{
                            	 if (query.getParam(oldParamName) != null)  {
                            		 param = query.getParam(oldParamName);
                            		 param.setName(paramName);
                                     param.setParamType(paramType);
                                     param.setSqlType(sqlType);
                                     param.setType(inOutType);
                                     /*if( flag.equals("deleteValidator")) {
                                         validators.remove(validateElementName);
                                     }*/
                                     param.setValidarors(validators); 
                                     if(defaultValue != null && defaultValue.trim().length() > 0){
                                      param.setDefaultValue(defaultValue);
                                     } else {
                                         if(param.getDefaultValue() != null) { //check whether default value has defined earlier
                                             param.setDefaultValue(null);
                                         }
                                     }

                                     if(structType != null && structType.trim().length() > 0){
                                      param.setStructType(structType);
                                     }
                                     if (ordinalStr != null && ordinalStr.trim().length() > 0) {
                                       param.setOrdinal(Integer.parseInt(ordinalStr));
                                     } else {
                                         param.setOrdinal(0);
                                     }
                            	 }
                            }
                         }else{
                            param = new Param();
                            param.setName(paramName);
                            param.setSqlType(sqlType);
                            param.setType(inOutType);
                            param.setParamType(paramType);
                            param.setValidarors(validators);
                            if(defaultValue != null && defaultValue.trim().length() > 0){
                                param.setDefaultValue(defaultValue);
                            }
                            if(structType != null && structType.trim().length() > 0){
                                param.setStructType(structType);
                            }
                            if (ordinalStr != null && ordinalStr.trim().length() > 0) {
                                param.setOrdinal(Integer.parseInt(ordinalStr));
                            }
                            query.addParam(param);
          
                        }


                    }
                }
            }
        }
    } 

    /* adding a new session object */
    if(flag.equals("add") || flag.equals("addrdf") ) {
       session.setAttribute("validators", new ArrayList()); 
    }

%>
<form action="addQuery.jsp" method="post" >
   <input type="hidden" id="queryId" name="queryId" value="<%=queryId%>" /> 
   <input type="hidden" id="serviceName" name="serviceName" value="<%=serviceName%>" />
   <input type="hidden" id="flag" name="flag" value="<%=flag%>" />
   <input type="hidden" id="origin" name="origin" value="<%=origin%>" />
   <input type="hidden" id="paramName" name="paramName" value="<%=paramName%>"/>
   <input type ="hidden" id="paramType" name="paramType" value="<%=paramType%>"/>
   <input type ="hidden" id="defaultValue" name="defaultValue" value="<%=defaultValue%>" />
    <input type="hidden" id="structType" name="structType" value="<%=structType%>"/>
   <input type ="hidden" id="sqlType" name="sqlType" value="<%=sqlType%>"/>
   <input type ="hidden" id="inoutType" name="inoutType" value="<%=inOutType%>"/>
</form>
<script type="text/javascript">
     if(document.getElementById('flag').value == 'save'){
        location.href= "addQuery.jsp?queryId="+document.getElementById('queryId').value;
     } else if(document.getElementById('flag').value == 'add'){
        location.href= "addInputMapping.jsp?queryId=<%=queryId%>";
     } else if(document.getElementById('flag').value == 'addrdf'){
        location.href= "addSparqlInputMapping.jsp?queryId=<%=queryId%>";
     } else if(document.getElementById('flag').value == 'deleterdf'){
         if(document.getElementById('origin').value == 'add'){
             location.href= "addSparqlInputMapping.jsp?queryId=<%=queryId%>";
         } else if(document.getElementById('origin').value == 'save'){
            location.href= "addQuery.jsp?queryId="+document.getElementById('queryId').value;
         }    
     }  else if(document.getElementById('flag').value == 'deletesql' || document.getElementById('flag').value == 'delete'){
         if(document.getElementById('origin').value == 'add'){
             location.href= "addInputMapping.jsp?queryId=<%=queryId%>";
         }else if(document.getElementById('origin').value == 'save'){
            location.href= "addQuery.jsp?queryId=" + document.getElementById('queryId').value;
         }    
     } else if(document.getElementById('flag').value == 'validate' || document.getElementById('flag').value == 'deleteValidator'){          
          location.href= "addInputMapping.jsp?queryId=" + document.getElementById('queryId').value + "&paramName=" + document.getElementById('paramName').value + "&paramType=" + document.getElementById('paramType').value + "&defaultValue=" + document.getElementById('defaultValue').value + "&structType=" + document.getElementById('structType') + "&sqlType=" + document.getElementById('sqlType').value + "&inOutType=" + document.getElementById('inoutType').value;
     }

</script>