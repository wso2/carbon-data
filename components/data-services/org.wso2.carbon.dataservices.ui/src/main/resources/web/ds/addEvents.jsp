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
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Event" %>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="../yui/build/yahoo-dom-event.js"></script>
<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">


    <carbon:breadcrumb
            label="event.heading"
            resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>
    <%
        String queryId = request.getParameter("queryId");
        String eventId = request.getParameter("eventId");
        boolean isEdit = false;
        String expression =null;
        String topic = null;
        String serviceName = dataService.getName();
        List<String> subscriptionsList = new ArrayList<String>();
        int rowCount = 0;
        int subscriptionCount = 0;
        if(eventId != null) {
        	isEdit = true;
            Event event = dataService.getEvent(eventId);
            expression = event.getExpression();
            topic = event.getTargetTopic();
            subscriptionsList = event.getSubscriptionsList();
        }
        eventId = (eventId == null) ? "" : eventId;
        expression = (expression == null) ? "" : expression;
        topic = (topic == null) ? "" : topic;
        String flag = request.getParameter("flag");
        flag = (flag == null) ? "" : flag;
        if (subscriptionsList.size() > 0) {
            rowCount = subscriptionsList.size() - 1;
        }

        %>
    <script type="text/javascript" src="js/ui-validations.js"></script>
    <div id="middle">
    <%
	if(isEdit) {%>
		<h2><fmt:message key="edit.event.heading"/>
        <%
        out.write(" (" + serviceName + "/" + queryId + ")");
        %>
        </h2>
	<%	
	} else{ %>
		 <h2><fmt:message key="event.heading"/>
        <%
        out.write(" (" + serviceName + "/" + queryId + ")");
        %>
        </h2>
	<%	
	}
	%>
   

    <div id="workArea">
        <form method="post" action="eventProcessor.jsp" name="eventForm" id="eventForm" onsubmit="countSubscriptions(document);return validateAddEvent();">
          <input type="hidden" id="buttonAction"/>
            <input type="hidden" id="queryId" name="queryId" value="<%=queryId%>" />
            <input type="hidden" id="flag" name="flag" value="<%=flag%>"/>
            <input type="hidden" id="eventId" name="eventId" value="<%=eventId%>">
            <input type="hidden" id="subscriptionCount" name="subscriptionCount" value="<%=subscriptionCount%>">
            <table class="styledLeft" cellspacing="0" width="100%">
                <thead>
                <tr>
                    <th colspan="2">
                        <%
                            if (isEdit) {%>
                        <fmt:message key="edit.event.heading"/>
                        <%
                        } else { %>
                        <fmt:message key="event.heading"/>
                        <%
                            }
                        %>
                    </th>
                </tr>
                </thead>
                <tr>
                    <td>
                        <table class="normal" width="100%">
                            <tr>
                                <td>
                                    <table class="normal" id="eventsTab">
                                        <tr><td><fmt:message key="event.id"/><font color="red">*</font></td>
                                            <td><input type="text" size="30" name="id" id="name"
                                                       value="<%=eventId%>" /></td>
                                        </tr>
                                        <tr><td><fmt:message key="event.xpath"/><font color="red">*</font></td>
                                            <td><input type="text" size="30" name="expression"
                                                       id="expression" value="<%=expression%>"/></td>
                                        </tr>
                                        <tr><td><fmt:message key="event.target.topic" /><font color="red">*</font></td>
                                            <td><input type="text" name="targetTopic"
                                                       id="targetTopic" size="30" value="<%=topic%>"/></td>
                                        </tr>
                                        <tr><td valign="top" style="vertical-align:top !important"><fmt:message key="event.sink.url"/></td>
                                            <td>
                                                <div>
                                                    <table id="serviceTbl">
                                                        <tr>
                                                            <td style="padding-left: 0px ! important;">
                                                                <% if (!flag.equals("edit")) { %>
                                                                <input type="text"
                                                                       name="subscription0"
                                                                       id="subscription0"
                                                                       size="30"/>
                                                                <input type="button" id="add"
                                                                       value="+"
                                                                       onclick=" return addRow(<%=rowCount%>,'');"/>
                                                                <% } else {
                                                                    for (int id = 0; id < subscriptionsList.size(); id++) {
                                                                %>
                                                                <% if (id != 0) {%>
                                                            </td>
                                                        <tr id="subscription<%=id%>">
                                                            <td style="padding-left: 0px ! important;">
                                                                <input type='text'
                                                                       name="subscription<%=id%>"
                                                                       size='30'
                                                                       value="<%=subscriptionsList.get(id)%>"/>
                                                                <input type='button'
                                                                       value=' - '
                                                                       onclick=" return deleteRow('subscription<%=id%>');"/>
                                                            </td>
                                                        </tr>
                                                        <% } else { %>
                                                        <input type='text'
                                                               name="subscription<%=id%>" size='30'
                                                               value="<%=subscriptionsList.get(id)%>"/>
                                                        <input type="button" id="add" value="+"
                                                               onclick=" return addRow(<%=rowCount%>,'edit'); "/>
                                                        <br/>
                                                        <%
                                                            }
                                                        %>

                                                        <% }
                                                        }
                                                        %>
                                                        </td>
                                                        </tr>
                                                    </table>
                                                </div>
                                             </td>
                                        </tr>

                                    </table>
                                </td>
                            </tr>
                         
                           
                                    <%
                                        if(eventId.equals("") && dataService.getEvents().size() > 0){
                                         

                                    %>
                                     <tr>
                                     <td class="middle-header"><fmt:message key="existing.events"/> </td>
			                         </tr>
			                            <tr><td>
			                                <table class="styledLeft" cellspacing="0" id="existingEventsTable" width="100%">
			                                    <thead>
			                                    <tr>
			                                        <th><b><fmt:message key="event.id"/></b></th>
			                                        <th><b><fmt:message key="event.xpath"/></b></th>
			                                        <th><b><fmt:message key="event.target.topic"/></b></th>
			                                        <th><b><fmt:message key="actions1"/></b></th>
			                                    </tr>
			                                    </thead>
			                                    <tbody>
			                             <%
				                             Iterator<Event> eventsItr = dataService.getEvents().iterator();
	                                         for(;eventsItr.hasNext();) {
	                                             Event event = eventsItr.next();
	                                             eventId = event.getId();
	                                             expression = event.getExpression();
	                                             topic = event.getTargetTopic();
			                             %>
                                    <tr style="display:none"><td><input type="hidden" id=<%=eventId%>
                                            name=<%=eventId%> value="<%=eventId%>"/> </td></tr>
                                    <tr>
                                        <td><%=eventId%></td>
                                        <td><%=expression%></td>
                                        <td><%=topic%></td>
                                        <td>
                                            <a class="icon-link"
                                               style="background-image:url(../admin/images/edit.gif);"
                                               href="addEvents.jsp?eventId=<%=eventId%>&queryId=<%=queryId%>&flag=edit"><fmt:message key="edit"/></a>
                                            <a class="icon-link"
                                               style="background-image:url(../admin/images/delete.gif);"
                                               onclick="deleteEvent('<%=eventId%>', '<%=queryId%>');" href="#"><fmt:message key="delete"/></a>
                                        </td>
                                    </tr>
                                    <%
                                            }
                                        }
                                    %>
                                    </tbody>
                                </table>
                            </td>
                            </tr>
                            <tr>
                                <td class="buttonRow" colspan="2">
                                    <input
                                            class="button" type="button"
                                            value="<fmt:message key="mainConfiguration"/>"
                                            onclick="redirectToMainConfiguration(document.getElementById('queryId').value);"/>
                                            <%--onclick="location.href = 'addQuery.jsp?queryId=<%=queryId%>'"/>--%>
                                    <input class="button" type="submit"
                                           value="<fmt:message key="add.events"/>"/>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </form>
    </div>

    <script type="text/javascript">
        function countSubscriptions(document) {
            var inputs = document.getElementsByTagName("input");
            var countNotEmptyFields = 0;

            for (var i = 0; i < inputs.length; i++) {
                if (document.getElementById('subscription'+i) != null) {
                     countNotEmptyFields = countNotEmptyFields + 1;
                }
            }
            document.getElementById('subscriptionCount').value = countNotEmptyFields;
        }
    </script>

</fmt:bundle>
