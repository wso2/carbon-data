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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.internal.DataServicesDSComponent;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class stores the OData Service handlers for services.
 */
public class ODataServiceRegistry {
	private static Log log = LogFactory.getLog(ODataServiceRegistry.class);

	private static ODataServiceRegistry serviceRegistry;

	private Map<String, ConcurrentHashMap<String, ODataServiceHandler>> registry = new ConcurrentHashMap<>();

	public ODataServiceRegistry() {
		// ignore
	}

	public static ODataServiceRegistry getInstance() {
		if (serviceRegistry == null) {
			synchronized (ODataServiceRegistry.class) {
				if (serviceRegistry == null) {
					serviceRegistry = new ODataServiceRegistry();

				}
			}
		}
		return serviceRegistry;
	}

	public void registerODataService(String dataServiceName, ODataServiceHandler handler, String tenantDomain)
			throws DataServiceFault {
		ConcurrentHashMap<String, ODataServiceHandler> oDataServiceHandlerMap = registry.get(tenantDomain);
		if (oDataServiceHandlerMap == null) {
			oDataServiceHandlerMap = new ConcurrentHashMap<>();
			registry.put(tenantDomain, oDataServiceHandlerMap);
		}
		oDataServiceHandlerMap.putIfAbsent(dataServiceName, handler);
	}

	public ODataServiceHandler getServiceHandler(String serviceKey, String tenantDomain) {
		// Load tenant configs
		if (null == registry.get(tenantDomain) && !"carbon.super".equals(tenantDomain)) {
			try {
				ConfigurationContextService contextService = DataServicesDSComponent.getContextService();
				ConfigurationContext configContext;
				if (contextService != null) {
					// Getting server's configContext instance
					configContext = contextService.getServerConfigContext();
					TenantAxisUtils.getTenantConfigurationContext(tenantDomain, configContext);
				} else {
					throw new Exception(
							"ConfigurationContext is not found while loading org.wso2.carbon.transport.fix bundle");
				}
			} catch (Exception e) {
				log.error("Error while activating FIX transport management bundle", e);
			}
		}
		if (registry.get(tenantDomain) != null) {
			return registry.get(tenantDomain).get(serviceKey);
		} else {
			return null;
		}
	}

	public void removeODataService(String tenantDomain, String serviceName) {
		registry.get(tenantDomain).remove(serviceName);
	}
}
