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
package org.wso2.carbon.dataservices.core.description.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.DataSourceTypes;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.custom.datasource.CustomQueryBasedDS;
import org.wso2.carbon.dataservices.core.engine.DataService;

/**
 * This class represents a data services custom query based in-line data source configuration.
 */
public class InlineCustomQueryBasedDSConfig extends CustomQueryBasedDSConfig {

	private static final Log log = LogFactory.getLog(InlineCustomQueryBasedDSConfig.class);
	
	private CustomQueryBasedDS dataSource;
	
	public InlineCustomQueryBasedDSConfig(DataService dataService, String configId, 
			Map<String, String> properties) throws DataServiceFault {
		super(dataService, configId, DataSourceTypes.CUSTOM_QUERY, properties);
		String dsClass = properties.get(DBConstants.CustomDataSource.DATA_SOURCE_QUERY_CLASS);
		try {
			this.dataSource = (CustomQueryBasedDS) Class.forName(dsClass).newInstance();
			String dataSourcePropsString = properties.get(
					DBConstants.CustomDataSource.DATA_SOURCE_PROPS);
			Map<String, String> dsProps;
			if (dataSourcePropsString != null) {
				dsProps = DBUtils.extractProperties(AXIOMUtil.stringToOM(
						dataSourcePropsString));
			} else {
				dsProps = new HashMap<String, String>();
			}
			DBUtils.populateStandardCustomDSProps(dsProps, this.getDataService(), this);
			this.dataSource.init(dsProps);
			if (log.isDebugEnabled()) {
				log.debug("Creating custom data source with info: #" + 
						this.getDataService().getTenantId() + "#" + 
						this.getDataService() + "#" + this.getConfigId());
			}
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in creating custom data source config: " +
					e.getMessage());
		}
	}
	
	public CustomQueryBasedDS getDataSource() {
		return dataSource;
	}

	@Override
	public boolean isActive() {
		return true;
	}

}
