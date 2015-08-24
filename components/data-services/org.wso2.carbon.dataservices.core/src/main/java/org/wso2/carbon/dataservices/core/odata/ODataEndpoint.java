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

package org.wso2.carbon.dataservices.core.odata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ODataEndpoint {
	private static final Log log = LogFactory.getLog(ODataEndpoint.class);

	public static void process(HttpServletRequest req, HttpServletResponse resp){
		String tenantDomain = TenantAxisUtils.getTenantDomain(req.getRequestURI());
		String[] serviceParams = getServiceName(req.getRequestURI(), tenantDomain);
		String serviceRootPath;
		if (serviceParams != null) {
			if (tenantDomain == null) {
				tenantDomain = "carbon.super";
				serviceRootPath = "/" + serviceParams[0] + "/" + serviceParams[1];
			} else {
				serviceRootPath = "/t/" + tenantDomain + "/" + serviceParams[0] + "/" + serviceParams[1];
			}
			String serviceKey = serviceParams[0] + serviceParams[1];
			ODataServiceRegistryImpl registry = ODataServiceRegistryImpl.getInstance();
			ODataServiceHandler handler = registry.getServiceHandler(serviceKey, tenantDomain);
			if (handler != null) {
				if (log.isDebugEnabled()) {
					log.debug(serviceRootPath + " Service invoked.");
				}
				handler.process(req, resp, serviceRootPath);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Couldn't find the ODataService Handler for " + serviceRootPath + " Service.");
				}
				resp.setStatus(501);
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Couldn't find the Service.");
			}
			resp.setStatus(400);
		}
	}

	private static String[] getServiceName(String uri, String tenantDomain) {
		String odataServices;
		String odataServiceName;
		String odataServiceUri;
		String configID;
		if (tenantDomain == null) {
			odataServices = "odataservices/";
		} else {
			odataServices = "odataservices/t/" + tenantDomain + "/";
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
				} else {
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
