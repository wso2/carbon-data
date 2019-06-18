/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.dbconsole.ui.internal;

import org.h2.server.web.WebServlet;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.dbconsole.ui", 
         immediate = true)
public class DBConsoleComponent {

    private HttpService httpService = null;

    private static Log log = LogFactory.getLog(DBConsoleComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        try {
            registerServlet(context.getBundleContext());
            log.debug("******* DB Console bundle is activated ******* ");
        } catch (Throwable e) {
            log.error("******* Failed to activate DB Console bundle ******* ", e);
        }
    }

    public void registerServlet(BundleContext bundleContext) throws Exception {
        HttpContext defaultHttpContext = httpService.createDefaultHttpContext();
        Dictionary<String, String> servletParam = new Hashtable<String, String>(2);
        servletParam.put("-webAllowOthers", "");
        httpService.registerServlet("/dbconsole", new WebServlet(), servletParam, defaultHttpContext);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        httpService.unregister("/dbconsole");
        log.debug("******* DB Console bundle is deactivated ******* ");
    }

    @Reference(
             name = "http.service", 
             service = org.osgi.service.http.HttpService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetHttpService")
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }
}

