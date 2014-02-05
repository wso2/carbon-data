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

import org.apache.axiom.om.OMElement;

public abstract class NonSQLQuery extends DataServiceConfigurationElement {
	
	protected boolean hasHeaders = false;
	
	protected int startingRow;
	
	protected int maxRowCount;
	
	protected String[] columnNames;
	
	protected String[] columnOrder;
	
	public NonSQLQuery() {
		this.hasHeaders = false;
		this.startingRow = 0;
		this.maxRowCount = -1;
	}
	
	public boolean hasHeaders() {
		return hasHeaders;
	}
	public void setHasHeaders(String value) {
		if(value == null || value.trim().length() == 0){
			this.hasHeaders = false;
		}else{
			this.hasHeaders = true;
		}		
	}
	public int getStartingRow() {
		return startingRow;
	}

	public void setStartingRow(String value) {
		if(value == null || value.trim().length() == 0){
			this.startingRow = 0;
		}else{
			this.startingRow = Integer.valueOf(value).intValue();
		}		
	}
	public int getMaxRowCount() {
		return maxRowCount;
	}
	public void setMaxRowCount(String value) {
		if(value == null || value.trim().length() == 0){
			this.maxRowCount = -1;
		}else{
			this.maxRowCount = Integer.valueOf(value).intValue();
		}		
	}
	
	public String[] getColumnNames() {
		return columnNames;
	}
	public void setColumnNames(String[] columnNamesArray) {
		this.columnNames = columnNamesArray;
	}
	public String[] getColumnOrder() {
		return columnOrder;
	}
	public void setColumnOrder(String[] columnOrderArray) {
		this.columnOrder = columnOrderArray;
	}
	
    public abstract OMElement buildXML();

}
