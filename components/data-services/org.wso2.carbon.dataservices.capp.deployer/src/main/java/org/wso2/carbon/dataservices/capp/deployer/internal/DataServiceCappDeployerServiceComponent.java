/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.dataservices.capp.deployer.internal;

import java.util.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.dataservices.capp.deployer.DataServiceCappDeployer;
import org.wso2.carbon.ndatasource.capp.deployer.DataSourceCappDeployer;

/**
 * @scr.component name="org.wso2.carbon.dataservices.capp.deployer" immediate="true"
 * @scr.reference name="org.wso2.carbon.application.deployer.handler"
 * interface="org.wso2.carbon.application.deployer.handler.AppDeploymentHandler"
 * cardinality="1..n" policy="dynamic" bind="setDataServiceCappDeployer" unbind="unsetDataServiceCappDeployer"
 */
public class DataServiceCappDeployerServiceComponent {

    private static final Log log = LogFactory.getLog(DataServiceCappDeployerServiceComponent.class);
    private ComponentContext ctx;
    private AppDeploymentHandler appDepHandler;

    protected synchronized void activate(ComponentContext ctx) {
        this.ctx = ctx;
        if (Objects.nonNull(appDepHandler)) {
            registerDataServiceCappDeployer();
        }
        if (log.isDebugEnabled()) {
            log.debug("Data Service Capp deployer activated");
        }
    }

    protected synchronized void deactivate(ComponentContext ctx) {
        this.ctx = null;
        if (log.isDebugEnabled()) {
            log.debug("Data Service Capp deployer deactivated");
        }
    }

    protected void setDataServiceCappDeployer(AppDeploymentHandler appDeploymentHandler) {
        if (appDeploymentHandler instanceof DataSourceCappDeployer) {
            if (Objects.isNull(ctx)) {
                // save appDeploymentHandler
                appDepHandler = appDeploymentHandler;
            } else {
                registerDataServiceCappDeployer();
            }
        }
    }

    protected void unsetDataServiceCappDeployer(AppDeploymentHandler appDeploymentHandler) {
        if (appDeploymentHandler.equals(appDepHandler)) {
            appDepHandler = null;
        }
    }

    /**
     * register data source deployer as an OSGi service
     */
    private void registerDataServiceCappDeployer() {
        try {
            ctx.getBundleContext().registerService(AppDeploymentHandler.class.getName(),
                                                   new DataServiceCappDeployer(), null);
        } catch (Throwable e) {
            log.error("Failed to activate Data Service Capp Deployer", e);
        }
    }
}
