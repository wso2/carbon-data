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
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Event" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Query" %>
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>
<script type="text/javascript">

</script>
<%
    String id = request.getParameter("id");
    String expression = request.getParameter("expression");
    String topic = request.getParameter("targetTopic");
    String queryId = request.getParameter("queryId");
    String flag = request.getParameter("flag");
    String previousId = request.getParameter("eventId");
    int subscriptionCount = -1;
    if (request.getParameter("subscriptionCount") != null) {
        subscriptionCount = Integer.parseInt(request.getParameter("subscriptionCount"));
    }
    flag = (flag == null) ? "" : flag;
    previousId = (previousId == null) ? "" : previousId;
    List<String> subs = new ArrayList<String>();

    String subscription;
    int count = 0;
    for (int i = 0; true; i++) {
        subscription = request.getParameter("subscription" + i);
        if (subscription != null) {
            subs.add(subscription);
        } else if (subscriptionCount > count ) {
            continue;
        } else {
            break;
        }
        count++;
    }    
    if(flag.equals("delete")) {
        dataService.removeEvent(dataService.getEvent(id));
    } else if(flag.equals("edit")) {
        dataService.removeEvent(dataService.getEvent(previousId));
        Event event = new Event();
        event.setId(id);
        event.setExpression(expression);
        event.setTargetTopic(topic);
        event.setSubscriptionsList(subs);
        dataService.addEvent(event);
    } else {
        if(!id.equals("") && id.trim().length() > 0) {
            Event event = new Event();
            event.setId(id);
            event.setExpression(expression);
            event.setTargetTopic(topic);
            event.setSubscriptionsList(subs);

            dataService.addEvent(event);
        }
    }
    
    String forwardTo = "addEvents.jsp?queryId=" + queryId;

%>
<script type="text/javascript">
    location.href = "<%=forwardTo%>";
</script>
