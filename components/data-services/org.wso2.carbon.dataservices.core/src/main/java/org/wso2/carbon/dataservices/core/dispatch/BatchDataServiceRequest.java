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
package org.wso2.carbon.dataservices.core.dispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.dataservices.core.engine.ParamValue;

/**
 * Represents a batch data service request.
 */
public class BatchDataServiceRequest extends DataServiceRequest {
		
	/**
	 * The data service request list which belongs to this batch request
	 */
	private List<SingleDataServiceRequest> dsRequests;
	
	/**
	 * This is used to keep the dependent entities, whose cleanup methods must be called after a batch
	 * request is done.
	 */
	private static ThreadLocal<List<BatchRequestParticipant>> batchRequestParticipant = new ThreadLocal<List<BatchRequestParticipant>>() {
		protected synchronized List<BatchRequestParticipant> initialValue() {
			return new ArrayList<BatchRequestParticipant>();
		}
	};
	
	public BatchDataServiceRequest(DataService dataService, String requestName,
			List<Map<String, ParamValue>> batchParams) throws DataServiceFault {
		super(dataService, requestName);
		this.dsRequests = new ArrayList<SingleDataServiceRequest>();
		/* create the requests */
		for (Map<String, ParamValue> params : batchParams) {
			this.dsRequests.add(new SingleDataServiceRequest(dataService, requestName, params));
		}
	}
	
	public static void addParticipant(BatchRequestParticipant participant) {
		batchRequestParticipant.get().add(participant);
	}
	
	private static List<BatchRequestParticipant> getParticipants() {
		return batchRequestParticipant.get();
	}
	
	private static void releaseParticipantResources() {
		List<BatchRequestParticipant> finList = getParticipants();
		for (BatchRequestParticipant fin : finList) {
			fin.releaseBatchRequestResources();
		}
	}
	
	private static void clearParticipants() {
		getParticipants().clear();
	}
	
	public List<SingleDataServiceRequest> getDSRequests() {
		return dsRequests;
	}
	
	/**
	 * @see DataServiceRequest#processRequest()
	 */
	@Override
	public OMElement processRequest() throws DataServiceFault {
		DataService dataService = this.getDataService();
		try {
			/* signal that we are batch processing */
			DBUtils.setBatchProcessing(true);
			List<SingleDataServiceRequest> requests = this.getDSRequests();
			int count = requests.size();
			/* set the batch request count in TL */
			DBUtils.setBatchRequestCount(count);
			/* begin a new data service transaction */
			dataService.beginTransaction();
			/* dispatch individual requests */
			for (int i = 0; i < count; i++) {
				/* set the current batch request number in TL */
				DBUtils.setBatchRequestNumber(i);
				/* execute/enqueue request */
				requests.get(i).dispatch();
			}
			/* end transaction */
			dataService.endTransaction();
			/* no result in batch requests */
			return null;
		} catch (DataServiceFault e) {
			dataService.rollbackTransaction();
			throw e;
		} finally {
			/* release participants */
			releaseParticipantResources();
			clearParticipants();
			/* reset TL values */
			DBUtils.setBatchProcessing(false);
			DBUtils.setBatchRequestCount(0);
			DBUtils.setBatchRequestNumber(0);
		}
	}

}
