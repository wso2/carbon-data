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

import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.wso2.carbon.dataservices.common.DBConstants.FaultCodes;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.dataservices.core.engine.ParamValue;

/**
 * This class represents exceptions that occur in data services. 
 */
public class DataServiceFault extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The type of data service fault, this can be used to identify what kind
	 * of an error condition occurred.
	 */
	private String code;
	
	/**
	 * The detailed explanation of the data service fault.
	 */
	private String dsFaultMessage;
	
	/**
	 * The originating data service of the exception, if available. 
	 */
	private DataService sourceDataService;
	
	/**
	 * The on-going operation/resource name when the data service fault occurs, if available.
	 */
	private String currentRequestName;
	
	/**
	 * The current parameters of the current operation/resource, if available.
	 */
	private Map<String, ParamValue> currentParams;
	
	public DataServiceFault(Exception nestedException, String code, String dsFaultMessage) {
		super(nestedException);
		this.code = code;
		this.dsFaultMessage = dsFaultMessage;
		if (this.code == null) {
			this.code = extractFaultCode(nestedException);
		}
	}
	
	public static String extractFaultCode(Throwable throwable) {
		if (throwable instanceof DataServiceFault) {
			return ((DataServiceFault) throwable).getCode();
		} else if (throwable instanceof XMLStreamException) { 
			return extractFaultCode(((XMLStreamException) throwable).getNestedException());
		} else if (throwable != null) {
			Throwable cause = throwable.getCause();
			if (cause != null) {
				return extractFaultCode(cause);
			} else {
				return FaultCodes.UNKNOWN_ERROR;
			}
		} else {
			return FaultCodes.UNKNOWN_ERROR; 
		}
	}
	
	public DataServiceFault(Exception nestedException) {
		this(nestedException, null, null);
	}
	
	public DataServiceFault(Exception nestedException, String dsFaultMessage) {
		this(nestedException, null, dsFaultMessage);
	}
	
	public DataServiceFault(String code, String dsFaultMessage) {
		this(null, code, dsFaultMessage);
	}
	
	public DataServiceFault(String dsFaultMessage) {
		this(null, null, dsFaultMessage);
	}
	
	public String getCode() {
		return code;
	}
	
	public String getDsFaultMessage() {
		return dsFaultMessage;
	}
	
	@Override
	public String getMessage() {
		return this.getFullMessage();
	}
	
	/**
	 * Returns a detailed description of the data service fault.
	 */
	public String getFullMessage() {
		StringBuffer buff = new StringBuffer();
		if (this.getDsFaultMessage() != null) {
			buff.append("DS Fault Message: " + this.getDsFaultMessage() + "\n");
		}
		if (this.getCode() != null) {
			buff.append("DS Code: " + this.getCode() + "\n");
		}
		if (this.getSourceDataService() != null) {
			buff.append("Source Data Service:-\n");
			buff.append(this.getSourceDataService().toString());
		}
		if (this.getCurrentRequestName() != null) {
			buff.append("Current Request Name: " + this.getCurrentRequestName() + "\n");
		}
		if (this.getCurrentParams() != null) {
			buff.append("Current Params: " + this.getCurrentParams() + "\n");
		}
		if (this.getCause() != null) {			
			buff.append("Nested Exception:-\n" + this.getCause() + "\n");
		}
		return buff.toString();
	}
	
	@Override
	public String toString() {
		return this.getFullMessage();
	}

	public Map<String, ParamValue> getCurrentParams() {
		return currentParams;
	}

	public void setCurrentParams(Map<String, ParamValue> currentParams) {
		this.currentParams = currentParams;
	}

	public String getCurrentRequestName() {
		return currentRequestName;
	}

	public void setCurrentRequestName(String currentRequestName) {
		this.currentRequestName = currentRequestName;
	}

	public DataService getSourceDataService() {
		return sourceDataService;
	}

	public void setSourceDataService(DataService sourceDataService) {
		this.sourceDataService = sourceDataService;
	}	
	
}
