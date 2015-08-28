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
package org.wso2.carbon.dataservices.core;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.wso2.carbon.dataservices.common.DBConstants.RDBMS;
import org.wso2.carbon.dataservices.common.RDBMSUtils;
import org.wso2.carbon.dataservices.core.description.config.SQLConfig;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.dataservices.core.odata.ODataDataHandler;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration.DataSourceProperty;
import org.wso2.carbon.ndatasource.rdbms.RDBMSDataSource;
import org.wso2.carbon.ndatasource.rdbms.RDBMSDataSourceConstants;
import org.wso2.carbon.ndatasource.rdbms.utils.RDBMSDataSourceUtils;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class represents a Tomcat JDBC Pool based SQL data source configuration.
 */
public class JDBCPoolSQLConfig extends SQLConfig {

	private DataSource dataSource;

	public JDBCPoolSQLConfig(DataService dataService, String configId, String type, Map<String, String> properties,
	                         boolean odataEnable) throws DataServiceFault {
		super(dataService, configId, type, RDBMSUtils.convertConfigPropsFromV2toV3(properties), odataEnable);
	}
	
	@Override
	public DataSource getDataSource() throws DataServiceFault {
		if (this.dataSource == null) {
		    synchronized (this) {
		    	/* this second check is necessary, in case another thread already initialized it */
			    if (this.dataSource == null) {
			        try {
				        RDBMSDataSource rdbmsDS = new RDBMSDataSource(
						        this.createConfigFromProps(this.getProperties()));
				        this.dataSource = rdbmsDS.getDataSource();
			        } catch (Exception e) {
				        throw new DataServiceFault(e,
						        "Error creating JDBC Pool SQL Config: "	+ e.getMessage());
			        }
			    }
		    }
		}
		return dataSource;
	}

	@Override
	public boolean isStatsAvailable() {
		return true;
	}

	@Override
	public int getActiveConnectionCount() throws DataServiceFault {
		return this.getDataSource().getActive();
	}

	@Override
	public int getIdleConnectionCount() throws DataServiceFault {
		return this.getDataSource().getIdle();
	}

	@Override
	public void close() {
		if (this.dataSource != null) {
			this.dataSource.close();
		}		
	}
	
	private RDBMSConfiguration createConfigFromProps(Map<String, String> props) 
			throws DataSourceException, XMLStreamException {
		/* create a copy first */
		props = new HashMap<String, String>(props);
		RDBMSConfiguration config = new RDBMSConfiguration();
		this.handleExternalDataSource(config, props);
		this.filterJDBCPoolProps(props);
		RDBMSDataSourceUtils.assignBeanProps(config,
				new HashMap<String, Object>(props));
		this.handlePostConfigInit(config);
		return config;
	}
	
	private void handleExternalDataSource(RDBMSConfiguration config, Map<String, String> props) 
			throws XMLStreamException {
		String dataSourcePropsString = props
				.remove(RDBMSDataSourceConstants.DATASOURCE_PROPS_NAME);
		if (dataSourcePropsString != null) {
			Map<String, String> dsProps = DBUtils.extractProperties(AXIOMUtil
					.stringToOM(dataSourcePropsString));
			List<DataSourceProperty> dspList = new ArrayList<DataSourceProperty>();
			DataSourceProperty tmpProp;
			for (Entry<String, String> dsProp : dsProps.entrySet()) {
				tmpProp = new DataSourceProperty();
				tmpProp.setEncrypted(false);
				tmpProp.setName(dsProp.getKey());
				tmpProp.setValue(dsProp.getValue());
				dspList.add(tmpProp);
			}
			config.setDataSourceProps(dspList);
		}
	}
	
	private void filterJDBCPoolProps(Map<String, String> props) {
		props.remove(RDBMS.FORCE_JDBC_BATCH_REQUESTS);
		props.remove(RDBMS.FORCE_STORED_PROC);
		props.remove(RDBMS.QUERY_TIMEOUT);
		props.remove(RDBMS.AUTO_COMMIT);
		props.remove(RDBMS.FETCH_DIRECTION);
		props.remove(RDBMS.FETCH_SIZE);
		props.remove(RDBMS.MAX_FIELD_SIZE);
		props.remove(RDBMS.MAX_ROWS);
		props.remove(RDBMS.MAX_WAIT);
		props.remove(RDBMS.DYNAMIC_USER_AUTH_CLASS);
		props.remove(RDBMS.DYNAMIC_USER_AUTH_MAPPING);
	}
	
	private void handlePostConfigInit(RDBMSConfiguration config) {
		if (this.getPrimaryDynAuth() != null) {
			config.setAlternateUsernameAllowed(true);
		}
	}

	@Override
	public ODataDataHandler createODataHandler() throws DataServiceFault {
		throw new DataServiceFault("Expose as OData Service feature doesn't support for the " + getConfigId() +
		                           " Datasource.");
	}
}
