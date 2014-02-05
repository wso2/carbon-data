/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.dataservices.ui.beans;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

public class GSpreadQuery extends DataServiceConfigurationElement {
	
	private int workSheetNumber;
	
	private int startingRow;
	
	private int maxRowCount;

    private boolean hasHeaders;

    public int getMaxRowCount() {
		return maxRowCount;
	}

	public void setMaxRowCount(int maxRowCount) {
		this.maxRowCount = maxRowCount;
	}

	public int getStartingRow() {
		return startingRow;
	}

	public void setStartingRow(int startingRow) {
		this.startingRow = startingRow;
	}

	public int getWorkSheetNumber() {
		return workSheetNumber;
	}

	public void setWorkSheetNumber(int workSheetNumber) {
		this.workSheetNumber = workSheetNumber;
	}

    public boolean hasHeaders() {
		return hasHeaders;
	}

    public void setHasHeaders(String value) {
        this.hasHeaders = Boolean.parseBoolean(value);
	}

	public OMElement buildXML() {
    	OMFactory fac = OMAbstractFactory.getOMFactory();
    	OMElement gspreadEl = fac.createOMElement("gspread", null);
    	
    	OMElement workshEl = fac.createOMElement("worksheetnumber", null);
    	workshEl.setText(String.valueOf(this.getWorkSheetNumber()));
    	gspreadEl.addChild(workshEl);
    	
    	OMElement stRowEl = fac.createOMElement("startingrow", null);
    	stRowEl.setText(String.valueOf(this.getStartingRow()));
    	gspreadEl.addChild(stRowEl);
    	
    	OMElement maxRowEl = fac.createOMElement("maxrowcount", null);
    	maxRowEl.setText(String.valueOf(this.getMaxRowCount()));
    	gspreadEl.addChild(maxRowEl);

        OMElement hasHeaderEl = fac.createOMElement("hasheader", null);
    	hasHeaderEl.setText(String.valueOf(this.hasHeaders()));
    	gspreadEl.addChild(hasHeaderEl);
    	
    	return gspreadEl;
    }

}
