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
<%@ page import="org.wso2.carbon.dataservices.ui.beans.CallQuery" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Operation" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Param" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.WithParam" %>
<%@ page import="java.util.ArrayList" %>
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>
<%
    String paramName = request.getParameter("paramNameId");
    String operationParamName = request.getParameter("operationParamId");
    String editParamName = request.getParameter("editParamName");
    String flag = request.getParameter("flag");
    String operationName = request.getParameter("operationName");
    String action = request.getParameter("action");
    action = (action == null) ? "" : action;
    editParamName = (editParamName == null) ? "" : editParamName;

    operationName = (operationName == null) ? "" : operationName;
    operationParamName = (operationParamName == null) ? "" : operationParamName;
    Operation oldOperation = dataService.getOperation(operationName);
    
    ArrayList<WithParam> withParamsList = new ArrayList<WithParam>();
    CallQuery newcallQuery = new CallQuery();
    //newcallQuery.setWithParams(withParamsList);

    if (!action.equals("")) {
        if (editParamName.equals("")) {
            //add operation params to exising operation.
            withParamsList = (ArrayList<WithParam>) oldOperation.getCallQuery().getWithParams();
            WithParam newparam = new WithParam();
            newparam.setName(paramName);
            newparam.setParamType("query-param");
            newparam.setParamValue(operationParamName);
            withParamsList.add(newparam);

            oldOperation.getCallQuery().setWithParams(withParamsList);
        } else {
            WithParam withParam = new WithParam();
            withParamsList = (ArrayList<WithParam>) oldOperation.getCallQuery().getWithParams();
            for (int a = 0; a < withParamsList.size(); a++) {
                withParam = withParamsList.get(a);
                if (withParam.getName().equals(editParamName)) {
                    withParamsList.get(a).setName(paramName);
                    withParamsList.get(a).setParamValue(operationParamName);
                    withParamsList.get(a).setParamType("query-param");
                }
            }
            oldOperation.getCallQuery().setWithParams(withParamsList);
        }
    }

    
    %>
<form action="addOperationParameter.jsp" method="post" >
   <input type="hidden" id="flag" name="flag" value="<%=flag%>" />
</form>
<script type="text/javascript">
     if(document.getElementById('flag').value == 'add'){
        location.href= "addOperation.jsp?operationName=<%=operationName%>&action=<%=action%>";
     } else {
        location.href = "operations.jsp";
     }

</script>