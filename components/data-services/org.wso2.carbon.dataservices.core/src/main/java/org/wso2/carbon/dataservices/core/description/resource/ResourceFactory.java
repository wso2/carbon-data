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
package org.wso2.carbon.dataservices.core.description.resource;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.dataservices.common.DBConstants.DBSFields;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.description.query.QueryFactory;
import org.wso2.carbon.dataservices.core.description.resource.Resource.ResourceID;
import org.wso2.carbon.dataservices.core.engine.CallQueryGroup;
import org.wso2.carbon.dataservices.core.engine.DataService;

/**
 * Factory class to create resources in a data service.
 */
public class ResourceFactory {

	private ResourceFactory() { }
	
	@SuppressWarnings("unchecked")
	public static Resource createResource(DataService dataService,
			OMElement resEl) throws DataServiceFault {
		String path = resEl.getAttributeValue(new QName(DBSFields.PATH));
		String method = resEl.getAttributeValue(new QName(DBSFields.METHOD));
		
		/* get the description */
		OMElement descEl = resEl.getFirstChildWithName(new QName(DBSFields.DESCRIPTION));
		String description = null;
		if (descEl != null) {
			description = descEl.getText();
		}
		
		CallQueryGroup callQueryGroup = null;
		List<CallQueryGroup> cqGroups = QueryFactory.createCallQueryGroups(dataService, 
				resEl.getChildrenWithName(new QName(DBSFields.CALL_QUERY)), 
				resEl.getChildrenWithName(new QName(DBSFields.CALL_QUERY_GROUP)));
		if (cqGroups.size() > 0) {
			callQueryGroup = cqGroups.get(0);
		}
		ResourceID resourceId = new ResourceID(path, method);
		
		String disableStreamingRequestStr = resEl.getAttributeValue(
				new QName(DBSFields.DISABLE_STREAMING));
		boolean disableStreamingRequest = false;
		if (disableStreamingRequestStr != null) {
			disableStreamingRequest = Boolean.parseBoolean(disableStreamingRequestStr);
		}
		boolean disableStreamingEffective = disableStreamingRequest | dataService.isDisableStreaming();
		
		Resource resource = new Resource(dataService, resourceId, description, callQueryGroup, 
				disableStreamingRequest, disableStreamingEffective);
		
	    String returnReqStatusStr = resEl.getAttributeValue(
				new QName(DBSFields.RETURN_REQUEST_STATUS));
		boolean returnReqStatus = false;
		if (returnReqStatusStr != null) {
			returnReqStatus = Boolean.parseBoolean(returnReqStatusStr);
		}
		resource.setReturnRequestStatus(returnReqStatus);
		
		return resource;
	}
	
}
