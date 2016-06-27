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
package org.wso2.carbon.dataservices.core;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.common.DBConstants;

/**
 * This class represents the Axis2 message receiver used to dispatch in-out service calls.
 */
public class DBInOutMessageReceiver extends RawXMLINOutMessageReceiver {
	
	private static final Log log = LogFactory.getLog(DBInOutMessageReceiver.class);
	
	/**
	 * Invokes the business logic invocation on the service implementation class
	 * 
	 * @param msgContext
	 *            the incoming message context
	 * @param newMsgContext
	 *            the response message context
	 * @throws AxisFault
	 *             on invalid method (wrong signature) or behavior (return null)
	 */
	public void invokeBusinessLogic(MessageContext msgContext,
			MessageContext newMsgContext) throws AxisFault {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Request received to DSS:  Data Service - " + msgContext.getServiceContext().getName() +
				          ", Operation - " + msgContext.getSoapAction() + ", Request body - " +
				          msgContext.getEnvelope().getText() + ", ThreadID - " + Thread.currentThread().getId());
			}
			OMElement result = DataServiceProcessor.dispatch(msgContext);
			SOAPFactory fac = getSOAPFactory(msgContext);
			SOAPEnvelope envelope = fac.getDefaultEnvelope();
			if (result != null) {
				envelope.getBody().addChild(result);
			}
			newMsgContext.setEnvelope(envelope);
		} catch (Exception e) {
			log.error("Error in in-out message receiver", e);
			msgContext.setProperty(Constants.FAULT_NAME, DBConstants.DS_FAULT_NAME);
			throw DBUtils.createAxisFault(e);
		} finally {
			if (log.isDebugEnabled()) {
				String response;
				if (msgContext.getProperty(Constants.FAULT_NAME) != null &&
				    msgContext.getProperty(Constants.FAULT_NAME).equals(DBConstants.DS_FAULT_NAME)) {
					response = "Error in Response";
				} else {
					response = newMsgContext.getEnvelope().getText();
				}
				log.debug("Response send from DSS:  Data Service - " + msgContext.getServiceContext().getName() +
				          ", Operation - " + msgContext.getSoapAction() + ", Response body - " + response +
				          ", ThreadID - " + Thread.currentThread().getId());
			}
		}
	}
    
}