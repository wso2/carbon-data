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
import org.apache.axis2.description.AxisService;
import org.wso2.carbon.dataservices.common.DBConstants.CSV;

public class CSVQuery extends NonSQLQuery {
	
	private String columnSeperator = ",";
	
	public String getColumnSeperator() {
		return columnSeperator;
	}

	public void setColumnSeperator(String columnSeperator) {
		this.columnSeperator = columnSeperator;
	}
	
	public CSVQuery(AxisService axisService){				
		Object value = axisService.getParameterValue(CSV.COLUMN_SEPERATOR);
		if( value != null){
			this.columnSeperator = (String)value;	
		}else{
			this.columnSeperator = ","; //default value
		}
	}
	
	/*
	 * Iterates through the columnNames mentioned in the configuration file.
	 * <property name="csv_columns">ID,Name,Price</property>
	 * 
	 *  Returns true, if passed 'columnName' is found within this column names
	 *  false, otherwise
	 */	
	public boolean isColumnNameMentioned(String columnName){
		for(int a = 0;a < this.columnNames.length;a++){
			if(this.columnNames[a].equals(columnName)){
				return true;
			}
		}
		return false;
	}

	@Override
	public OMElement buildXML() {
		return null;
	}
	
}

