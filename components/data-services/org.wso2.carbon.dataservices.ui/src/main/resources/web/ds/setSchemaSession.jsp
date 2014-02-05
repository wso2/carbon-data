<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.dataservices.ui.DataServiceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %><%

    String[] totalSchemaList;
    String sourceId = (String)session.getAttribute("datasource");

    String schemaFlag = request.getParameter("schemaFlag");
    if (schemaFlag != null && !schemaFlag.equals("")) {
        session.setAttribute("schemaFlag",schemaFlag);
        if (schemaFlag.equals("selectNoneTables")) {
            session.setAttribute("totalSchemaList","");
        } else if (schemaFlag.equals("selectAllSchemas")) {
            try {
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
	            ConfigurationContext configContext =
	                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
	            String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);
	            DataServiceAdminClient client = new DataServiceAdminClient(cookie, backendServerURL, configContext);
                totalSchemaList = client.getdbSchemaList(sourceId);
                session.setAttribute("totalSchemaList",totalSchemaList);
            } catch (Exception e) {
                 
            }
        }
    }
    
%>