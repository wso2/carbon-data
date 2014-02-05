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

public class ExcelQuery extends NonSQLQuery {
	
	private String workBookName = ",";
	
	public String getWorkBookName() {
		return workBookName;
	}
	public void setWorkBookName(String workBookName) {
		this.workBookName = workBookName;
	}

    public void ExcelQuey(){       
    }



    /*public ExcelQuery(AxisService axisService,OMElement query){
		//unlike CSV query properties, EXCEL query properties are defined @ query level.
		//Reason : single Excel document can have multiple sheets & we can have different operations
		//per each sheet.
		
		//extracting query properties from query/excel element
		OMElement excelEle = query.getFirstChildWithName(new QName(DBConstants.Query.EXCEL));
		OMElement workBookNameEle = excelEle.getFirstChildWithName(new QName(DBConstants.Query.EXCEL_WORKBOOK_NAME));
		OMElement hasHeaderEle = excelEle.getFirstChildWithName(new QName(DBConstants.Query.HAS_HEADER));
		OMElement startingRowEle = excelEle.getFirstChildWithName(new QName(DBConstants.Query.STARTING_ROW));
		OMElement maxRowCountEle = excelEle.getFirstChildWithName(new QName(DBConstants.Query.MAX_ROW_COUNT));
		
		if(workBookNameEle != null && workBookNameEle.getText() != null && workBookNameEle.getText().trim().length() > 0){
			this.workBookName = workBookNameEle.getText();
		}else{
			this.workBookName = "Sheet1"; //default name
		}
		
		if(hasHeaderEle != null && hasHeaderEle.getText() != null && hasHeaderEle.getText().trim().length() > 0){
			this.hasHeaders = Boolean.valueOf(hasHeaderEle.getText()).booleanValue();
		}else{
			this.hasHeaders = false; //default name
		}
		
		if(startingRowEle != null && startingRowEle.getText() != null && startingRowEle.getText().trim().length() > 0){
			this.startingRow = Integer.valueOf(startingRowEle.getText()).intValue();
		}else{
			this.startingRow = 1;//default name
		}

		if(maxRowCountEle != null && maxRowCountEle.getText() != null && maxRowCountEle.getText().trim().length() > 0){
			this.maxRowCount = Integer.valueOf(maxRowCountEle.getText()).intValue();
		}else{
			this.maxRowCount = -1; //default name
		}
		
	}*/
    
    @Override
    public OMElement buildXML() {
    	OMFactory fac = OMAbstractFactory.getOMFactory();
    	OMElement excelEl = fac.createOMElement("excel", null);
    	
    	OMElement workbkEl = fac.createOMElement("workbookname", null);
    	if (this.getWorkBookName() != null) {
    	    workbkEl.setText(this.getWorkBookName());
    	}
    	excelEl.addChild(workbkEl);
    	
    	OMElement hasHeaderEl = fac.createOMElement("hasheader", null);
    	hasHeaderEl.setText(String.valueOf(this.hasHeaders()));
    	excelEl.addChild(hasHeaderEl);
    	
    	OMElement stRowEl = fac.createOMElement("startingrow", null);
    	stRowEl.setText(String.valueOf(this.getStartingRow()));
    	excelEl.addChild(stRowEl);
    	
    	OMElement maxRowEl = fac.createOMElement("maxrowcount", null);
    	maxRowEl.setText(String.valueOf(this.getMaxRowCount()));
    	excelEl.addChild(maxRowEl);
    	
    	return excelEl;
    }

}
