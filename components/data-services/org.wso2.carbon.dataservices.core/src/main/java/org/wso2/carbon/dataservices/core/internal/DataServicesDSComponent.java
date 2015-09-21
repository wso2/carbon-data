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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.auth.AuthorizationRoleRetriever;
import org.wso2.carbon.dataservices.core.config.parser.DataServiceConfigParser;
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

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @scr.component name="dataservices.component" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="eventbrokerbuilder.component" interface="org.wso2.carbon.event.core.EventBroker"
 * cardinality="0..1" policy="dynamic" bind="setEventBroker" unbind="unsetEventBroker"
 * @scr.reference name="datasources.service" interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1" policy="dynamic" bind="setDataSourceService" unbind="unsetDataSourceService"
 * @scr.reference name="secret.callback.handler.service"
 * interface="org.wso2.carbon.securevault.SecretCallbackHandlerService"
 * cardinality="1..1" policy="dynamic"
 * bind="setSecretCallbackHandlerService" unbind="unsetSecretCallbackHandlerService"
 * @scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic" bind="setTenantRegistryLoader"
 * unbind="unsetTenantRegistryLoader"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="0..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class DataServicesDSComponent {

    private static Log log = LogFactory.getLog(DataServicesDSComponent.class);

    private static RegistryService registryService = null;

    private static RealmService realmService = null;

    private static EventBroker eventBroker;
    
    private static DataSourceService dataSourceService;

    private static SecretCallbackHandlerService secretCallbackHandlerService;
    
    private static TenantRegistryLoader tenantRegLoader;

    private static ConfigurationContextService contextService;

    private static Object dsComponentLock = new Object(); /* class level lock for controlling synchronized access to static variables */

    /* this is to keep event trigger objects which are not registered for subscription*/
    private static List<EventTrigger> eventTriggerList= new ArrayList<EventTrigger>();

    /**
     * Static variable to hold roleRetriever instance.
     */
    private static AuthorizationRoleRetriever roleRetriever;

    public DataServicesDSComponent() {
    }

    protected void activate(ComponentContext ctxt) {
        try {
            BundleContext bundleContext = ctxt.getBundleContext();
            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                                          new DSAxis2ConfigurationContextObserver(), null);
            bundleContext.registerService(DSDummyService.class.getName(), new DSDummyService(), null);
            bundleContext.registerService(TransactionManagerDummyService.class.getName(),
                                          new TransactionManagerDummyService(), null);

            /* initialising role retriever instance for later usage */
            DataServiceConfigParser dataServiceConfigParser = DataServiceConfigParser.getInstance();
            OMElement roleRetrieverElement = dataServiceConfigParser.getConfigElement
                    (DBConstants.RoleRetrieverConfig.ELEMENT_NAME_AUTHORIZATION_ROLE_RETRIEVER);
            if (roleRetrieverElement == null) {
                throw new ServerConfigurationException("Cannot find '" +
                                                       DBConstants.RoleRetrieverConfig.ELEMENT_NAME_AUTHORIZATION_ROLE_RETRIEVER +
                                                       "' element in 'dataServices.xml' config file");
            }
            String roleRetrieverClassName = roleRetrieverElement.getAttributeValue(new QName(
                    DBConstants.RoleRetrieverConfig.ATTRIBUTE_NAME_CLASS));
            //initialize the roleRetrieverElement
            Class roleRetrieverClass = Class.forName(roleRetrieverClassName);
            roleRetriever = (AuthorizationRoleRetriever) roleRetrieverClass.newInstance();

            //read the properties in the authenticator element and set them in the authenticator.
            Iterator<OMElement> propertyElements = roleRetrieverElement.getChildrenWithName(new QName(
                    DBConstants.RoleRetrieverConfig.ELEMENT_NAME_PROPERTY));
            if (propertyElements != null) {
                Map<String, String> properties = new HashMap<String, String>();
                while (propertyElements.hasNext()) {
                    OMElement propertyElement = propertyElements.next();
                    String attributeName = propertyElement.getAttributeValue(new QName(
                            DBConstants.RoleRetrieverConfig.ATTRIBUTE_NAME_NAME));
                    String attributeValue = propertyElement.getText();
                    properties.put(attributeName, attributeValue);
                }
                roleRetriever.setProperties(properties);
            }
            roleRetriever.init();
            log.debug("Data Services bundle is activated ");
        } catch (ServerConfigurationException e) {
            log.error("Error in reading Role Retriever config from " +
                      "dataServices.xml when initializing roleRetriever, Error - " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            log.error("Error while instantiating the roleRetriever, Class Not found, Error - " + e.getMessage(), e);
        } catch (InstantiationException e) {
            log.error("Error while instantiating the roleRetriever, Error - " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            log.error("Error while instantiating the roleRetriever, Error - " + e.getMessage(), e);
        } catch (DataServiceFault e) {
            log.error("Error while instantiating the roleRetriever init, Error - " + e.getMessage(), e);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            /* don't throw exception */
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("Data Services bundle is deactivated ");
    }

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
            for (EventTrigger trigger: eventTriggerList) {
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

    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
    	DataServicesDSComponent.tenantRegLoader = tenantRegLoader;
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
    	DataServicesDSComponent.tenantRegLoader = null;
    }

    public static TenantRegistryLoader getTenantRegistryLoader(){
        return DataServicesDSComponent.tenantRegLoader;
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        DataServicesDSComponent.contextService = contextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        DataServicesDSComponent.contextService = null;
    }

    public static ConfigurationContextService getContextService() {
        return contextService;
    }

    /**
     * Method to get roleRetriever instance once required.
     *
     * @return roleRetriever
     */
    public static AuthorizationRoleRetriever getRoleRetriever() {
        return roleRetriever;
    }
}