package org.wso2.carbon.dataservices.ui.beans;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.dataservices.common.DBConstants.DBSFields;

public abstract class CallableRequest extends DataServiceConfigurationElement {
	
	private CallQueryGroup callQueryGroup;
	
    private String description;
    
    private boolean disableStreaming;
    
    private boolean returnRequestStatus;

	public boolean isReturnRequestStatus() {
		return returnRequestStatus;
	}

	public void setReturnRequestStatus(boolean returnRequestStatus) {
		this.returnRequestStatus = returnRequestStatus;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	

	public CallQueryGroup getCallQueryGroup() {
		return callQueryGroup;
	}
	
	public boolean isDisableStreaming() {
		return disableStreaming;
	}

	public void setDisableStreaming(boolean disableStreaming) {
		this.disableStreaming = disableStreaming;
	}
	
	public CallQuery getCallQuery() {
		if (this.getCallQueryGroup().getCallQueries().size() == 1) {
			return this.getCallQueryGroup().getCallQueries().get(0);
		}
		return null;
	}
	
	public void setCallQueryGroup(CallQueryGroup callQueryGroup) {
		this.callQueryGroup = callQueryGroup;
	}
	
	public void setCallQuery(CallQuery callQuery) {
		this.callQueryGroup = new CallQueryGroup();
		this.callQueryGroup.addCallQuery(callQuery);
	}

	public abstract OMElement buildXML();
	
    protected void populateGenericRequestProps(OMElement requestEl) {
    	OMFactory fac = OMAbstractFactory.getOMFactory();
		if (this.isDisableStreaming()) {
			requestEl.addAttribute(DBSFields.DISABLE_STREAMING, String.valueOf(true), null);
	    }
		if (this.getDescription() != null && this.getDescription().trim().length() > 0) {
			OMElement descEl = fac.createOMElement(DBSFields.DESCRIPTION, null);
			descEl.setText(this.getDescription());
			requestEl.addChild(descEl);
		}
		if (this.getCallQueryGroup() != null){
			requestEl.addChild(this.getCallQueryGroup().buildXML());
        }
		if (this.isReturnRequestStatus()) {
			requestEl.addAttribute(DBSFields.RETURN_REQUEST_STATUS, String.valueOf(true), null);
		}
    }

}
