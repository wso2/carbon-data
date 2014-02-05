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
package org.wso2.carbon.dataservices.core.description.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.common.DBConstants.AutoCommit;
import org.wso2.carbon.dataservices.common.DBConstants.RDBMS;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.auth.ConfigurationBasedAuthenticator;
import org.wso2.carbon.dataservices.core.auth.DynamicUserAuthenticator;
import org.wso2.carbon.dataservices.core.engine.DataService;

/**
 * This class is the base class used for all SQL based (RDBMS) data source configurations.
 */
public abstract class SQLConfig extends Config {
	
	private static final Log log = LogFactory.getLog(SQLConfig.class);

	private String validationQuery;
	
	private boolean jdbcBatchUpdateSupport;
	
	private AutoCommit autoCommit;
	
	private DynamicUserAuthenticator primaryDynAuth;
	
	private DynamicUserAuthenticator secondaryDynAuth;
	
	/**
	 * This is used to keep the enlisted XADatasource objects
	 */
	private static ThreadLocal<Set<XAResource>> enlistedXADataSources = new ThreadLocal<Set<XAResource>>() {
		protected Set<XAResource> initialValue() {
			return new HashSet<XAResource>();
		}
	};
	
	public SQLConfig(DataService dataService, String configId, 
			String type, Map<String, String> properties) throws DataServiceFault {
		super(dataService, configId, type, properties);
		/* set validation query, if exists */
		this.validationQuery = this.getProperty(RDBMS.VALIDATION_QUERY);
		this.processAutoCommitValue();
		this.processDynamicAuth();
	}
	
	private void processDynamicAuth() throws DataServiceFault {
		String dynAuthMapping = this.getProperty(RDBMS.DYNAMIC_USER_AUTH_MAPPING);
		if (!DBUtils.isEmptyString(dynAuthMapping)) {
			OMElement dynUserAuthPropEl;
			try {
				dynUserAuthPropEl = AXIOMUtil.stringToOM(dynAuthMapping);
			} catch (XMLStreamException e) {
				throw new DataServiceFault(e, 
						"Error in reading dynamic user auth mapping configuration: " + 
				                e.getMessage());
			}
			OMElement dynUserAuthConfEl = dynUserAuthPropEl.getFirstElement();
			if (dynUserAuthConfEl == null) {
				throw new DataServiceFault("Invalid dynamic user auth mapping configuration");
			}
			this.primaryDynAuth = new ConfigurationBasedAuthenticator(
					dynUserAuthConfEl.toString());
		}
		String dynAuthClass = this.getProperty(RDBMS.DYNAMIC_USER_AUTH_CLASS);
		if (!DBUtils.isEmptyString(dynAuthClass)) {
			try {
				DynamicUserAuthenticator authObj = (DynamicUserAuthenticator) Class.forName(
						dynAuthClass).newInstance();
				if (this.primaryDynAuth == null) {
					this.primaryDynAuth = authObj;
				} else {
					this.secondaryDynAuth = authObj;
				}
			} catch (Exception e) {
				throw new DataServiceFault(e, 
						"Error in creating dynamic user authenticator: " + e.getMessage());
			}
		}
	}
	
	private void processAutoCommitValue() throws DataServiceFault {
		String autoCommitProp = this.getProperty(RDBMS.AUTO_COMMIT);
		if (!DBUtils.isEmptyString(autoCommitProp)) {
			autoCommitProp = autoCommitProp.trim();
			try {
				boolean acBool = Boolean.parseBoolean(autoCommitProp);
				if (acBool) {
					this.autoCommit = AutoCommit.AUTO_COMMIT_ON;
				} else {
					this.autoCommit = AutoCommit.AUTO_COMMIT_OFF;
				}
			} catch (Exception e) {
				throw new DataServiceFault(e, "Invalid autocommit value in config: " + 
						autoCommitProp + ", autocommit should be a boolean value");
			}		
		} else {
			this.autoCommit = AutoCommit.DEFAULT;			
		}
	}
	
	public DynamicUserAuthenticator getPrimaryDynAuth() {
		return primaryDynAuth;
	}

	public DynamicUserAuthenticator getSecondaryDynAuth() {
		return secondaryDynAuth;
	}

	public boolean hasJDBCBatchUpdateSupport() {
		return jdbcBatchUpdateSupport;
	}
	
	public AutoCommit getAutoCommit() {
		return autoCommit;
	}
		
	protected void initSQLDataSource() throws SQLException, DataServiceFault {
		Connection conn = this.createConnection();
		/* check if we have JDBC batch update support */
		this.jdbcBatchUpdateSupport = conn.getMetaData().supportsBatchUpdates();
		conn.close();
	}
		
	public abstract DataSource getDataSource() throws DataServiceFault;
	
	public abstract boolean isStatsAvailable() throws DataServiceFault;
	
	public abstract int getActiveConnectionCount() throws DataServiceFault;
	
	public abstract int getIdleConnectionCount() throws DataServiceFault;
		
	public String getValidationQuery() {
		return validationQuery;
	}
	
	public Connection createConnection() throws SQLException, DataServiceFault {
		return this.createConnection(null, null);
	}
	
	public Connection createConnection(String user, String pass) 
			throws SQLException, DataServiceFault {
		if (log.isDebugEnabled()){
			log.debug("Creating data source connection");
		}
		DataSource ds = this.getDataSource();
		if (ds != null) {
			Connection conn;
			if (user != null) {
				conn = ds.getConnection(user, pass);
			} else {
			    conn = ds.getConnection();
			}
			if (this.getDataService().isEnableXA() && this.getDataService().isInTransaction() &&
					conn instanceof XAConnection) {
				try {
					Transaction tx = this.getDataService().getDSSTxManager().
							getTransactionManager().getTransaction();
					XAResource xaResource = ((XAConnection) conn).getXAResource();
					if (!isXAResourceEnlisted(xaResource)) {
						tx.enlistResource(xaResource);
						addToEnlistedXADataSources(xaResource);
					}
				} catch (IllegalStateException e) {
					// ignore: can be because we are trying to enlist again
				} catch (Exception e) {
					throw new DataServiceFault(e, 
							"Error in getting current transaction: " + e.getMessage());
				}
			}
			return conn;
		} else {
			throw new DataServiceFault("The data source is nonexistent");
		}
	}
	
	@Override
	public boolean isActive() {
		try {
			Connection conn = this.getDataSource().getConnection();
			conn.close();
			return true;
		} catch (Exception e) {
			log.error("Error in checking SQL config availability", e);
			return false;
		}
	}
	
	/**
     * This method adds XAResource object to enlistedXADataSources Threadlocal set
     * @param resource
     */
    private void addToEnlistedXADataSources(XAResource resource) {
    	enlistedXADataSources.get().add(resource);
    }
    
    private boolean isXAResourceEnlisted(XAResource resource) {
    	return enlistedXADataSources.get().contains(resource);
    }
	
}
