<%@page import="java.io.StringWriter"%>
<%@page import="org.apache.commons.io.IOUtils"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="org.wso2.carbon.utils.CarbonUtils"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.dataservices.ui.DataServiceAdminClient"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.AxisFault"%>
<%@ page import="org.wso2.carbon.CarbonError" %>
<%@ page import="java.io.PrintWriter" %>
<%
    StringWriter writer = new StringWriter();
    IOUtils.copy(request.getInputStream(), writer);
    String jsonMapping = writer.toString();
   
	String backendServerURL = CarbonUIUtil
			.getServerURL(config.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
			.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	DataServiceAdminClient client = new DataServiceAdminClient(cookie, backendServerURL,
			configContext);
	String message = "";
	try {
		message = client.validateJSONMapping(jsonMapping);
		response.setContentType("text/plain; charset=UTF-8");
		// Set standard HTTP/1.1 no-cache headers.
		response.setHeader("Cache-Control",
		"no-store, max-age=0, no-cache, must-revalidate");
		// Set IE extended HTTP/1.1 no-cache headers.
		response.addHeader("Cache-Control", "post-check=0, pre-check=0");
		// Set standard HTTP/1.0 no-cache header.
		response.setHeader("Pragma", "no-cache");
	} catch (AxisFault e) {
	    message = e.getLocalizedMessage();		
	}
	PrintWriter pw = response.getWriter();
	pw.write(message);
	pw.flush();
%>