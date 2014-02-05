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
<%@page import="org.wso2.carbon.dataservices.common.DBConstants.DBSFields"%>
<%@ page import="java.util.Set" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.*" %>
<%@ page import="java.util.ArrayList" %>
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>
<%
    //retrieve form values set in addQuery.jsp page
    String forwardTo = "";
    String serviceName = request.getParameter("serviceName");
    String queryId = request.getParameter("queryId");
    String operationName = request.getParameter("operationName");
    String operationDesc = request.getParameter("operationDesc");
    String oldOperationName = request.getParameter("oldOperationName");
    String enableStreaming = request.getParameter("enableStreaming");
    String returnRequestStatusStr = request.getParameter("returnRequestStatus");
    String flag = request.getParameter("flag");
    String paramName = request.getParameter("paramName");
    
    String action = request.getParameter("action");
    action = (action == null) ? "" : action;
    oldOperationName = (oldOperationName == null) ? "" : oldOperationName;
    flag = (flag == null) ? "" : flag;
    Param[] params = null;

    Query query = dataService.getQuery(queryId);
    if(query != null) {
       params  = query.getParams();
    }
    //Check for duplicate operations
    Operation operation = dataService.getOperation(operationName);
    //Check for old operations when edit the operation
    Operation oldOperation = dataService.getOperation(oldOperationName);

    if (operation != null && action.equals("")) {
        String message = "Please enter a different operation name. An operation called " + operationName
                + " already exists.";
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "addOperation.jsp";

    } else {
        if (oldOperation == null) {
            if (action.equals("")) {
                //add new operation
                CallQuery callQuery = new CallQuery();
                ArrayList<WithParam> withParamsList = new ArrayList<WithParam>();
                if (params != null) {
                    for (int a = 0; a < params.length; a++) {
                        Param param = params[a];
                        if(!param.getType().equals("OUT")){
                            WithParam withParam = new WithParam(param.getName(), param.getName(), "with-param");
                            withParam.setName(param.getName());
                            withParam.setParamType("with-param");
                            withParam.setParamValue(param.getName());
                            withParamsList.add(withParam);
                        }
                    }
                    //when new operation is adding  set query param as with-params.
                    //If needs, facilitate to delete these default query params from with-params list..
                    if (flag.equals("delete")) {
                        ArrayList<WithParam> newWithParam = new ArrayList<WithParam>(withParamsList.size()-1);
                            for(int a=0; a < withParamsList.size() ; a++){
                              if(withParamsList.get(a).getName().equals(paramName)){
                                 withParamsList.remove(a);
                              }
                            }
                        forwardTo = "addOperation.jsp?operationName="+operationName+"&operationDesc="+operationDesc+"&enableStreaming="+enableStreaming+"&action=edit";
                    }
                    callQuery.setWithParams(withParamsList);
                    
                }
                    callQuery.setHref(queryId);
                    Operation newOperation = new Operation();
                    newOperation.setName(operationName);
                    newOperation.setDescription(operationDesc);
                    if (enableStreaming != null) {
                        newOperation.setDisableStreaming(false);
                    } else {
                        newOperation.setDisableStreaming(true);
                    }
                    if (returnRequestStatusStr != null) {
                        newOperation.setReturnRequestStatus(true);
                    } else {
                        newOperation.setReturnRequestStatus(false);
                    }
                    newOperation.setCallQuery(callQuery);
                    dataService.addOperation(newOperation);
                if (!flag.equals("delete")) {
                    forwardTo = "operations.jsp?serviceName=" + serviceName + "&ordinal=3";
                }

            } else if (action.equals("remove")) {
                //remove operations
                dataService.removeOperation(operation);

                forwardTo = "operations.jsp?serviceName=" + serviceName + "&ordinal=3";
            }
//            forwardTo = "operations.jsp?serviceName=" + serviceName + "&ordinal=3";
        } else {
            if (action.equals("edit") && !flag.equals("delete")) {
                //edit operation
                ArrayList<WithParam> withParamsList = new ArrayList<WithParam>();
                CallQuery newcallQuery = new CallQuery();
                withParamsList = (ArrayList<WithParam>) oldOperation.getCallQuery().getWithParams();

                newcallQuery.setWithParams(withParamsList);
                newcallQuery.setHref(queryId);
                oldOperation.setName(operationName);
                oldOperation.setDescription(operationDesc);
                if (enableStreaming != null) {
                	oldOperation.setDisableStreaming(false);
                } else {
                	oldOperation.setDisableStreaming(true);
                }
                if (returnRequestStatusStr != null) {
                	oldOperation.setReturnRequestStatus(true);
                } else {
                	oldOperation.setReturnRequestStatus(false);
                }
                oldOperation.setCallQuery(newcallQuery);

                forwardTo = "operations.jsp?serviceName=" + serviceName + "&ordinal=3";
            }
            else if (action.equals("edit") && flag.equals("delete") ) {
                    //delete operation params
                    if (query != null) {
                        ArrayList<WithParam> withParamsList = new ArrayList<WithParam>();
                        CallQuery newcallQuery = new CallQuery();
                        withParamsList = (ArrayList<WithParam>) oldOperation.getCallQuery().getWithParams();

                        ArrayList<WithParam> newWithParam = new ArrayList<WithParam>(withParamsList.size()-1);
                            for(int a=0; a < withParamsList.size() ; a++){
                              if(withParamsList.get(a).getName().equals(paramName)){
                                 withParamsList.remove(a);
                              }
                            }

                        newWithParam.addAll(withParamsList);
                        oldOperation.getCallQuery().setWithParams(newWithParam);
                    }
//                forwardTo = "addOperation.jsp?action=edit&operationName="+operation.getName()+"&operationDesc="+operationDesc+"&enableStreaming="+enableStreaming;
                forwardTo = "addOperation.jsp?action=edit&operationName="+operation.getName()+"&operationDesc="+operationDesc+"&enableStreaming="+enableStreaming+"&selectedQueryId="+queryId;
                  
                }

        }
    }
%>
<script type="text/javascript">
    location.href = "<%=forwardTo%>";
</script>