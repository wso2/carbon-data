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
<%@ page import="java.util.Set" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.*" %>
<%@ page import="java.util.ArrayList" %>
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>
<%
    //retrieve form values set in addQuery.jsp page
    String serviceName = request.getParameter("serviceName");
    String queryId = request.getParameter("queryId");
    String resourcePath = request.getParameter("resourcePath");
    String resourceMethod = request.getParameter("resourceMethod");
    String description = request.getParameter("resourceDesc");
    String isExistingResource = request.getParameter("existingResource");
    String oldResourcePath = request.getParameter("oldResourcePath");
    String action = request.getParameter("action");
    String enableStreaming = request.getParameter("enableStreaming");
    String returnRequestStatusStr = request.getParameter("returnRequestStatus");
    action  = (action == null) ? "" : action;
    oldResourcePath = (oldResourcePath == null) ? "" : oldResourcePath;

    Query query = dataService.getQuery(queryId);
    Param[] params = null;
    if (query != null) {
    	/* check to see if the query is null - i.e. remove resource */
        params = query.getParams();
    }

    if(action.equals("remove")){
        isExistingResource = "true";
    }
    Resource resource;
    if(isExistingResource != null){
        if(isExistingResource.equals("false")){
            //add resource
            CallQuery callQuery = new CallQuery();
            ArrayList<WithParam> withParamsList = new ArrayList();
            if (params != null) {
              for (int a = 0; a < params.length; a++) {
                  Param param = params[a];
                  WithParam withParam = new WithParam(param.getName(), param.getName(), "with-param");
                  withParam.setName(param.getName());
                  withParam.setParamType("with-param");
                  withParam.setParamValue(param.getName());
                  withParamsList.add(withParam);
               }
               callQuery.setWithParams(withParamsList);
            }                
            callQuery.setHref(queryId);
            resource = new Resource();
            resource.setPath(resourcePath);
            resource.setDescription(description);
            resource.setCallQuery(callQuery);
            resource.setMethod(resourceMethod);
            if (enableStreaming != null) {
            	resource.setDisableStreaming(false);
            } else {
        	    resource.setDisableStreaming(true);
            }
            if (returnRequestStatusStr != null) {
            	resource.setReturnRequestStatus(true);
            } else {
            	resource.setReturnRequestStatus(false);
            }
            dataService.addResource(resource);
        }else if(isExistingResource.equals("true")){
            //edit resource
            resource = dataService.getResource(oldResourcePath);
            if(resource != null){
                if(action.equals("remove")){
                    dataService.removeResource(resource);
                }else{
                    ArrayList<WithParam> withParamsList = new ArrayList();
                    CallQuery newcallQuery = new CallQuery();
                    if (params != null) {
                        for (int a = 0; a < params.length; a++) {
                            Param param = params[a];
                            WithParam withParam = new WithParam(param.getName(), param.getName(), "with-param");
                            withParam.setName(param.getName());
                            withParam.setParamType("with-param");
                            withParam.setParamValue(param.getName());
                            withParamsList.add(withParam);
                        }
                        newcallQuery.setWithParams(withParamsList);
                    }
                    newcallQuery.setHref(queryId);
                    resource.setDescription(description);
                    resource.setPath(resourcePath);
                    if (enableStreaming != null) {
                    	resource.setDisableStreaming(false);
                    } else {
                	    resource.setDisableStreaming(true);
                    }
                    if (returnRequestStatusStr != null) {
                    	resource.setReturnRequestStatus(true);
                    } else {
                    	resource.setReturnRequestStatus(false);
                    }
                    //CallQuery callQuery = resource.getCallQuery();
                    //callQuery.setHref(queryId);
                    resource.setCallQuery(newcallQuery);
                    resource.setMethod(resourceMethod);
                }    
            }
        }
    }  
 %>
<script type="text/javascript">
    location.href="resources.jsp?ordinal=4";
</script>