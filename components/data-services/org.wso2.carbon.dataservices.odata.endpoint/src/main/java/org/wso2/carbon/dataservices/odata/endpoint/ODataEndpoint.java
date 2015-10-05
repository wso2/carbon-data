/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.dataservices.odata.endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.dataservices.core.odata.ODataServiceFault;
import org.wso2.carbon.dataservices.core.odata.ODataServiceHandler;
import org.wso2.carbon.dataservices.core.odata.ODataServiceRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ODataEndpoint {
	private static final Log log = LogFactory.getLog(ODataEndpoint.class);
	private static final int NOT_IMPLEMENTED = 501;
	private static final int BAD_REQUEST = 400;

	/**
	 * This method will find the particular OdataHandler from the ODataServiceRegistry and process the request.
	 *
	 * @param request  HTTPServlet Request
	 * @param response HTTPServlet Response
	 * @see ODataServiceRegistry
	 */
	public static void process(HttpServletRequest request, HttpServletResponse response) {
		String tenantDomain = TenantAxisUtils.getTenantDomain(request.getRequestURI());
		try {
			String[] serviceParams = getServiceDetails(request.getRequestURI(), tenantDomain);
			String serviceRootPath;
			if (tenantDomain == null) {
				tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
				serviceRootPath = "/" + serviceParams[0] + "/" + serviceParams[1];
			} else {
				serviceRootPath = "/t/" + tenantDomain + "/" + serviceParams[0] + "/" + serviceParams[1];
			}
			String serviceKey = serviceParams[0] + serviceParams[1];
			ODataServiceRegistry registry = ODataServiceRegistry.getInstance();
			ODataServiceHandler handler = registry.getServiceHandler(serviceKey, tenantDomain);
			if (handler != null) {
				if (log.isDebugEnabled()) {
					log.debug(serviceRootPath + " Service invoked.");
				}
				handler.process(request, response, serviceRootPath);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Couldn't find the ODataService Handler for " + serviceRootPath + " Service.");
				}
				response.setStatus(NOT_IMPLEMENTED);
			}
		} catch (ODataServiceFault e) {
			response.setStatus(BAD_REQUEST);
			if (log.isDebugEnabled()) {
				log.debug("Bad Request invoked. :" + e.getMessage());
			}
		}
	}

	/**
	 * This method retrieve the service name and config id from the request uri.
	 *
	 * @param uri          Request uri
	 * @param tenantDomain Tenant domain
	 * @return String Array String[0] ServiceName, String[1] ConfigID
	 */
	private static String[] getServiceDetails(String uri, String tenantDomain) throws ODataServiceFault {
		String odataServices;
		String odataServiceName;
		String odataServiceUri;
		String configID;
		if (tenantDomain == null) {
			odataServices = "odata/";
		} else {
			odataServices = "odata/t/" + tenantDomain + "/";
		}
		int index = uri.indexOf(odataServices);
		if (-1 != index) {
			int serviceStart = index + odataServices.length();
			if (uri.length() > serviceStart + 1) {
				odataServiceUri = uri.substring(serviceStart);
				if (-1 != odataServiceUri.indexOf('/')) {
					String[] params = odataServiceUri.split("/");
					odataServiceName = params[0];
					configID = params[1];
					return new String[] { odataServiceName, configID };
				}
			}
		}
		throw new ODataServiceFault("Bad OData request.");
	}
}
