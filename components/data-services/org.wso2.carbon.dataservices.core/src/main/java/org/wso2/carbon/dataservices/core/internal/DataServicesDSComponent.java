/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.description.event.EventTrigger;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.transaction.manager.TransactionManagerDummyService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;

@Component(
        name = "dataservices.component",
        immediate = true)
public class DataServicesDSComponent {

    private static Log log = LogFactory.getLog(DataServicesDSComponent.class);

    private static RegistryService registryService = null;

    private static RealmService realmService = null;

    private static EventBroker eventBroker;

    private static DataSourceService dataSourceService;

    private static SecretCallbackHandlerService secretCallbackHandlerService;

    private static TenantRegistryLoader tenantRegLoader;

    private static ConfigurationContextService contextService;

    private static Object dsComponentLock = new Object(); /* class level lock for controlling synchronized access to
    static variables */

    /* this is to keep event trigger objects which are not registered for subscription*/
    private static List<EventTrigger> eventTriggerList = new ArrayList<EventTrigger>();

    public DataServicesDSComponent() {

    }

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            BundleContext bundleContext = ctxt.getBundleContext();
            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                    new DSAxis2ConfigurationContextObserver(), null);
            bundleContext.registerService(DSDummyService.class.getName(), new DSDummyService(), null);
            bundleContext.registerService(TransactionManagerDummyService.class.getName(),
                    new TransactionManagerDummyService(), null);

            log.debug("Data Services bundle is activated ");
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            /* don't throw exception */
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        log.debug("Data Services bundle is deactivated ");
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the Registry Service");
        }
        DataServicesDSComponent.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {

        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Registry Service");
        }
        DataServicesDSComponent.registryService = null;
    }

    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the Realm Service");
        }
        DataServicesDSComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Realm Service");
        }
        DataServicesDSComponent.realmService = null;
    }

    public static RegistryService getRegistryService() {

        return registryService;
    }

    public static RealmService getRealmService() {

        return realmService;
    }

    @Reference(
            name = "datasources.service",
            service = org.wso2.carbon.ndatasource.core.DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataSourceService")
    protected void setDataSourceService(DataSourceService dataSourceService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the Data Sources Service");
        }
        DataServicesDSComponent.dataSourceService = dataSourceService;
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {

        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Data Sources Service");
        }
        DataServicesDSComponent.dataSourceService = null;
    }

    public static DataSourceService getDataSourceService() {

        return dataSourceService;
    }

    @Reference(
            name = "eventbrokerbuilder.component",
            service = org.wso2.carbon.event.core.EventBroker.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventBroker")
    protected void setEventBroker(EventBroker eventBroker) {

        synchronized (dsComponentLock) {
            if (log.isDebugEnabled()) {
                log.debug("Setting the Event Broker Service");
            }
            DataServicesDSComponent.eventBroker = eventBroker;
            processSubscriptionsForEventTriggers();
        }
    }

    protected void unsetEventBroker(EventBroker eventBroker) {

        synchronized (dsComponentLock) {
            if (log.isDebugEnabled()) {
                log.debug("Unsetting the Event Broker Service");
            }
            DataServicesDSComponent.eventBroker = null;
        }
    }

    public static EventBroker getEventBroker() {

        return eventBroker;
    }

    public static void registerSubscriptions(EventTrigger eventTrigger) throws DataServiceFault {

        synchronized (dsComponentLock) {
            if (DataServicesDSComponent.eventBroker == null) {
                eventTriggerList.add(eventTrigger);
            } else {
                eventTrigger.processEventTriggerSubscriptions();
            }
        }
    }

    public static void processSubscriptionsForEventTriggers() {

        if (eventTriggerList.size() > 0 && DataServicesDSComponent.eventBroker != null) {
            for (EventTrigger trigger : eventTriggerList) {
                trigger.processEventTriggerSubscriptions();
            }
        }
    }

    public static String getUsername() {

        return CarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    public static SecretCallbackHandlerService getSecretCallbackHandlerService() {

        return DataServicesDSComponent.secretCallbackHandlerService;
    }

    @Reference(
            name = "secret.callback.handler.service",
            service = org.wso2.carbon.securevault.SecretCallbackHandlerService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecretCallbackHandlerService")
    protected void setSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {

        if (log.isDebugEnabled()) {
            log.debug("SecretCallbackHandlerService acquired");
        }
        DataServicesDSComponent.secretCallbackHandlerService = secretCallbackHandlerService;

    }

    protected void unsetSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {

        DataServicesDSComponent.secretCallbackHandlerService = null;
    }

    @Reference(
            name = "tenant.registryloader",
            service = org.wso2.carbon.registry.core.service.TenantRegistryLoader.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTenantRegistryLoader")
    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {

        DataServicesDSComponent.tenantRegLoader = tenantRegLoader;
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {

        DataServicesDSComponent.tenantRegLoader = null;
    }

    public static TenantRegistryLoader getTenantRegistryLoader() {

        return DataServicesDSComponent.tenantRegLoader;
    }

    @Reference(
            name = "configuration.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {

        DataServicesDSComponent.contextService = contextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {

        DataServicesDSComponent.contextService = null;
    }

    public static ConfigurationContextService getContextService() {

        return contextService;
    }

}
