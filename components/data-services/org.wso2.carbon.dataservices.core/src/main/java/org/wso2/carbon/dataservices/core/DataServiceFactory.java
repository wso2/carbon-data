/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.dataservices.core;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.BoxcarringOps;
import org.wso2.carbon.dataservices.common.DBConstants.DBSFields;
import org.wso2.carbon.dataservices.core.description.config.ConfigFactory;
import org.wso2.carbon.dataservices.core.description.event.EventTriggerFactory;
import org.wso2.carbon.dataservices.core.description.operation.Operation;
import org.wso2.carbon.dataservices.core.description.operation.OperationFactory;
import org.wso2.carbon.dataservices.core.description.query.QueryFactory;
import org.wso2.carbon.dataservices.core.description.resource.ResourceFactory;
import org.wso2.carbon.dataservices.core.engine.CallableRequest;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.dataservices.core.engine.Result;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.SecurityConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Factory class for creating DataService objects
 * from the data services configuration.
 *
 * @see DataService
 */
public class DataServiceFactory {

    /**
     * Creates a DataService object with the given information.
     *
     * @see DataService
     */
    @SuppressWarnings("unchecked")
    public static DataService createDataService(OMElement dbsElement,
                                                String dsLocation) throws DataServiceFault {
        DataService dataService = null;
        try {
            /* get service name */
            String serviceName = dbsElement.getAttributeValue(new QName(DBSFields.NAME));
            String serviceNamespace = dbsElement.getAttributeValue(new QName(DBSFields.SERVICE_NAMESPACE));
            if (DBUtils.isEmptyString(serviceNamespace)) {
                serviceNamespace = DBConstants.WSO2_DS_NAMESPACE;
            }
            String defaultNamespace = dbsElement.getAttributeValue(new QName(DBSFields.BASE_URI));
            if (DBUtils.isEmptyString(defaultNamespace)) {
                defaultNamespace = serviceNamespace;
            }

            String serviceGroup = dbsElement.getAttributeValue(new QName(DBSFields.SERVICE_GROUP));
            if (DBUtils.isEmptyString(serviceGroup)) {
                serviceGroup = serviceName;
            }

            /* get the description */
            OMElement descEl = dbsElement.getFirstChildWithName(new QName(DBSFields.DESCRIPTION));
            String description = null;
            if (descEl != null) {
                description = descEl.getText();
            }

            String serviceStatus = dbsElement.getAttributeValue(
                    new QName(DBSFields.SERVICE_STATUS));

            boolean batchRequestsEnabled = false;
            boolean boxcarringEnabled = false;

            String batchRequestsEnabledStr = dbsElement.getAttributeValue(
                    new QName(DBSFields.ENABLE_BATCH_REQUESTS));
            if (batchRequestsEnabledStr != null) {
                batchRequestsEnabled = Boolean.parseBoolean(batchRequestsEnabledStr);
            }

            String boxcarringEnabledStr = dbsElement.getAttributeValue(
                    new QName(DBSFields.ENABLE_BOXCARRING));
            if (boxcarringEnabledStr != null) {
                boxcarringEnabled = Boolean.parseBoolean(boxcarringEnabledStr);
            }

            boolean disableStreaming = false;
            String disableStreamingStr = dbsElement.getAttributeValue(
                    new QName(DBSFields.DISABLE_STREAMING));
            if (disableStreamingStr != null) {
                disableStreaming = Boolean.parseBoolean(disableStreamingStr);
            }

            /* transaction management */
            boolean enableDTP = false;
            String enableDTPStr = dbsElement.getAttributeValue(new QName(DBSFields.ENABLE_DTP));
            if (enableDTPStr != null) {
                enableDTP = Boolean.parseBoolean(enableDTPStr);
            }

            /* txManagerName property */
            String userTxJNDIName = dbsElement.getAttributeValue(
                    new QName(DBSFields.TRANSACTION_MANAGER_JNDI_NAME));

            dataService = new DataService(serviceName, description,
                    defaultNamespace, dsLocation, serviceStatus,
                    batchRequestsEnabled, boxcarringEnabled, enableDTP,
                    userTxJNDIName);
            
            /* set service namespace */
            dataService.setServiceNamespace(serviceNamespace);

            /* set disable streaming */
            dataService.setDisableStreaming(disableStreaming);

            /* add the password manager */
            Iterator<OMElement> passwordMngrItr = dbsElement.getChildrenWithName(
                    new QName(SecurityConstants.PASSWORD_MANAGER_SIMPLE));
            if (passwordMngrItr.hasNext()) {
                SecretResolver secretResolver = SecretResolverFactory.create(dbsElement, false);
                dataService.setSecretResolver(secretResolver);
            }

            /* add the configs */
            for (Iterator<OMElement> itr = dbsElement.getChildrenWithName(
                    new QName(DBSFields.CONFIG)); itr.hasNext();) {
                dataService.addConfig(ConfigFactory.createConfig(dataService, itr.next()));
            }

            /* add event triggers */
            for (Iterator<OMElement> itr = dbsElement.getChildrenWithName(
                    new QName(DBSFields.EVENT_TRIGGER)); itr.hasNext();) {
                dataService.addEventTrigger(
                        EventTriggerFactory.createEventTrigger(dataService, itr.next()));
            }

            /* add the queries */
            for (Iterator<OMElement> itr = dbsElement
                    .getChildrenWithName(new QName(DBSFields.QUERY)); itr.hasNext();) {
                dataService.addQuery(QueryFactory.createQuery(dataService, itr.next()));
            }

            /* add the operations */
            for (Iterator<OMElement> itr = dbsElement
                    .getChildrenWithName(new QName(DBSFields.OPERATION)); itr.hasNext();) {
                dataService.addOperation(OperationFactory.createOperation(dataService,
                        itr.next()));
            }

            /* add the resources */
            for (Iterator<OMElement> itr = dbsElement.getChildrenWithName(
                    new QName(DBSFields.RESOURCE)); itr.hasNext();) {
                dataService.addResource(ResourceFactory.createResource(dataService,
                        itr.next()));
            }

            /* init the data service object */
            dataService.init();

            for (String opName : dataService.getOperationNames()) {
                Result result = dataService.getOperation(opName).getCallQueryGroup()
                        .getDefaultCallQuery().getQuery().getResult();
                if (result != null && result.getResultType() == DBConstants.ResultTypes.RDF) {
                    throw new DataServiceFault("Cannot create operation "
                            + dataService.getOperation(opName) + "for the result output type RDF");
                }
            }
            /* add necessary equivalent batch operation for the above defined operation */
            if (dataService.isBatchRequestsEnabled()) {
                List<Operation> tmpOpList = new ArrayList<Operation>();
                Operation operation;
                for (String opName : dataService.getOperationNames()) {
                    if (isBoxcarringOps(opName)) {
                        /* skip boxcarring operations */
                        continue;
                    }
                    operation = dataService.getOperation(opName);
                    if (isBatchCompatible(operation)) {
                        /* this is a batch operation and the parent operation is also given */
                        Operation batchOp = new Operation(
                                operation.getDataService(),
                                operation.getName() + DBConstants.BATCH_OPERATON_NAME_SUFFIX,
                                "batch operation for '" + operation.getName() + "'",
                                operation.getCallQueryGroup(), true,
                                operation, operation.isDisableStreamingRequest(),
                                operation.isDisableStreamingEffective());
                        batchOp.setReturnRequestStatus(operation.isReturnRequestStatus());
                        tmpOpList.add(batchOp);
                    }
                }
                /* the operations are added outside the loop that iterates the operation list,
                     * if we add it inside the loop while iterating, we will get a concurrent modification exception */
                for (Operation tmpOp : tmpOpList) {
                    dataService.addOperation(tmpOp);
                }
            }

            return dataService;
        } catch (DataServiceFault e) {
            /* the exception is caught to fill in the data service deployment exception details */
            e.setSourceDataService(dataService);
            throw e;
        } catch (Exception e) {
            /* if an unexpected exception has occurred */
            DataServiceFault dsf = new DataServiceFault(e);
            dsf.setSourceDataService(dataService);
            throw dsf;
        }
    }

    /**
     * Checks if the given operation is related to boxcarring.
     */
    private static boolean isBoxcarringOps(String opName) {
        return opName.equals(BoxcarringOps.BEGIN_BOXCAR) ||
                opName.equals(BoxcarringOps.END_BOXCAR) ||
                opName.equals(BoxcarringOps.ABORT_BOXCAR);
    }

    /**
     * Checks if the given data service request is batch request compatible,
     * i.e. does not have a result.
     */
    private static boolean isBatchCompatible(CallableRequest request) {
        return !request.getCallQueryGroup().getDefaultCallQuery().getQuery().hasResult();
    }

}
