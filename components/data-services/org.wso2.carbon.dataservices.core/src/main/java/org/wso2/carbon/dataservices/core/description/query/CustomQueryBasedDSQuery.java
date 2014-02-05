/*
 *  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dataservices.core.description.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamWriter;

import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.custom.datasource.CustomQueryBasedDS;
import org.wso2.carbon.dataservices.core.custom.datasource.DataColumn;
import org.wso2.carbon.dataservices.core.custom.datasource.DataRow;
import org.wso2.carbon.dataservices.core.custom.datasource.QueryResult;
import org.wso2.carbon.dataservices.core.description.config.CustomQueryBasedDSConfig;
import org.wso2.carbon.dataservices.core.description.event.EventTrigger;
import org.wso2.carbon.dataservices.core.engine.DataEntry;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.dataservices.core.engine.InternalParam;
import org.wso2.carbon.dataservices.core.engine.InternalParamCollection;
import org.wso2.carbon.dataservices.core.engine.ParamValue;
import org.wso2.carbon.dataservices.core.engine.QueryParam;
import org.wso2.carbon.dataservices.core.engine.Result;

/**
 * This class represents the query class for data services query based custom data sources.
 */
public class CustomQueryBasedDSQuery extends Query {

	private CustomQueryBasedDSConfig config;
	
	private String expression;
	
	public CustomQueryBasedDSQuery(DataService dataService, String queryId,
			List<QueryParam> queryParams, Result result, String configId,
			EventTrigger inputEventTrigger, EventTrigger outputEventTrigger,
			Map<String, String> advancedProperties, 
			String inputNamespace, String expression) throws DataServiceFault {
		super(dataService, queryId, queryParams, result, configId, inputEventTrigger,
				outputEventTrigger, advancedProperties, inputNamespace);
		this.expression = expression;
		try {
		    this.config = (CustomQueryBasedDSConfig) this.getDataService().getConfig(this.getConfigId());
		} catch (ClassCastException e) {
			throw new DataServiceFault(e, "Configuration is not a Custom Query config:" + 
					this.getConfigId());
		}
	}
	
	public String getExpression() {
		return expression;
	}
	
	public CustomQueryBasedDSConfig getConfig() {
		return config;
	}

	private String[] createColumnsMappings(List<DataColumn> columns) {
		String[] result = new String[columns.size()];
		int count = columns.size();
		for (int i = 0; i < count; i++) {
			result[i] = columns.get(i).getName();
		}
		return result;
	}
	
	@Override
	public void runQuery(XMLStreamWriter xmlWriter,
			InternalParamCollection params, int queryLevel)
			throws DataServiceFault {
		try {
		    CustomQueryBasedDS dataSource = this.getConfig().getDataSource();
		    QueryResult result = dataSource.executeQuery(this.getExpression(), 
		    		new ArrayList<InternalParam>(params.getParams()));
		    DataEntry dataEntry;
		    DataRow currentRow;
		    List<DataColumn> columns = result.getDataColumns();
		    String[] columnMappings = this.createColumnsMappings(columns);
		    int count = columns.size();
		    boolean useColumnNumbers = this.isUsingColumnNumbers();
		    String columnName;
		    String tmpVal;
		    while (result.hasNext()) {
		    	dataEntry = new DataEntry();
		    	currentRow = result.next();
		    	for (int i = 0; i < count; i++) {
		    		columnName = useColumnNumbers ? Integer.toString(i + 1) : columnMappings[i];
		    		tmpVal = currentRow.getValueAt(columnName);
		    		dataEntry.addValue(columnName, new ParamValue(tmpVal));
		    	}
		    	this.writeResultEntry(xmlWriter, dataEntry, params, queryLevel);
		    }
		} catch (Exception e) {
			throw new DataServiceFault(e, 
					"Error in CustomQueryBasedDSQuery.runQuery: " + e.getMessage());
		}
	}

}
