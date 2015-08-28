<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@page import="org.wso2.carbon.dataservices.common.DBConstants" %>
<%@ page import="org.wso2.carbon.dataservices.common.conf.DynamicAuthConfiguration" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Config" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Property" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Query" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>
<jsp:useBean id="newConfig" class="org.wso2.carbon.dataservices.ui.beans.Config" scope="session"/>
<jsp:useBean id="backupConfigProps" class="java.util.ArrayList" scope="session"></jsp:useBean>
<%!
    private void updateConfiguration(Config config, String propertyName, Object value) {
        if (value instanceof String) {
            String s = value.toString();
            if (s != null && s.trim().length() != 0) {
                config.updateProperty(propertyName, value);
            } else {
                config.removeProperty(propertyName);
            }
        } else if (value instanceof DynamicAuthConfiguration) {
            List<DynamicAuthConfiguration.Entry> userEntries = ((DynamicAuthConfiguration) value).getEntries();
            if (userEntries != null && userEntries.size() > 0) {
                config.updateProperty(propertyName, value);
            } else {
                config.removeProperty(propertyName);
            }
        }
    }
%>
<%
    //retrieve form values set in addDataSource.jsp page
    String cancelButton = request.getParameter("cancel_button");
    String serviceName = request.getParameter("serviceName");
    String datasourceId = request.getParameter("datasourceId");
    String datasourceType = request.getParameter("datasourceType");
    String useSecretAliasForPasswordValue = request.getParameter("useSecretAliasValue");
    String selectBox = request.getParameter("selectbox");
    Boolean isOData;
    if (null != request.getParameter("isOData")) {
        isOData = true;
    } else {
        isOData = false;
    }
    String driverClass = request.getParameter(DBConstants.RDBMS.DRIVER_CLASSNAME);
    String jdbcUrl = request.getParameter(DBConstants.RDBMS.URL);
    String dsUserName = request.getParameter(DBConstants.RDBMS.USERNAME);
    String dsPassword = request.getParameter(DBConstants.RDBMS.PASSWORD);
    String xaDataSourceClass = request.getParameter(DBConstants.RDBMS.DATASOURCE_CLASSNAME);
    String user = request.getParameter("User");
    String url = request.getParameter("URL");
    String password = request.getParameter("Password");
    String passwordAlias = request.getParameter("pwdalias");
    int propertyCount = 0;
    if (request.getParameter("propertyCount")!=null && !request.getParameter("propertyCount").equals("")){
       propertyCount = Integer.parseInt(request.getParameter("propertyCount")); 
    }
    int staticUserMappingsCount = 0;
    if (request.getParameter("staticUserMappingsCount") != null && !request.getParameter("staticUserMappingsCount").equals("")) {
        staticUserMappingsCount = Integer.parseInt(request.getParameter("staticUserMappingsCount"));
    }
    String xaType = request.getParameter("isXAType");
    String transactionIsolation = request.getParameter(DBConstants.RDBMS.DEFAULT_TX_ISOLATION);
    String initialSize = request.getParameter(DBConstants.RDBMS.INITIAL_SIZE);
    String maxPool = request.getParameter(DBConstants.RDBMS.MAX_ACTIVE);
    String maxIdle = request.getParameter(DBConstants.RDBMS.MAX_IDLE);
    String minPool = request.getParameter(DBConstants.RDBMS.MIN_IDLE);
    String maxWait = request.getParameter(DBConstants.RDBMS.MAX_WAIT);
    String validationQuery = request.getParameter(DBConstants.RDBMS.VALIDATION_QUERY);
    String testOnBorrow = request.getParameter(DBConstants.RDBMS.TEST_ON_BORROW);
    String testOnReturn = request.getParameter(DBConstants.RDBMS.TEST_ON_RETURN);
    String testWhileIdle = request.getParameter(DBConstants.RDBMS.TEST_WHILE_IDLE);
    String timeBetweenEvictionRunsMillis = request.getParameter(DBConstants.RDBMS.TIME_BETWEEN_EVICTION_RUNS_MILLIS);
    String numTestsPerEvictionRun = request.getParameter(DBConstants.RDBMS.NUM_TESTS_PER_EVICTION_RUN);
    String minEvictableIdleTimeMillis = request.getParameter(DBConstants.RDBMS.MIN_EVICTABLE_IDLE_TIME_MILLIS);
    String removeAbandoned = request.getParameter(DBConstants.RDBMS.REMOVE_ABANDONED);
    String removeAbandonedTimeout = request.getParameter(DBConstants.RDBMS.REMOVE_ABANDONED_TIMEOUT);
    String logAbandoned = request.getParameter(DBConstants.RDBMS.LOG_ABANDONED);
    String defaultAutoCommit = request.getParameter(DBConstants.RDBMS.AUTO_COMMIT);
    String defaultReadOnly = request.getParameter(DBConstants.RDBMS.DEFAULT_READONLY);
    String defaultCatalog = request.getParameter(DBConstants.RDBMS.DEFAULT_CATALOG);
    String validatorClassName = request.getParameter(DBConstants.RDBMS.VALIDATOR_CLASSNAME);
    String connectionProperties = request.getParameter(DBConstants.RDBMS.CONNECTION_PROPERTIES);
    String initSql = request.getParameter(DBConstants.RDBMS.INIT_SQL);
    String jdbcInterceptors = request.getParameter(DBConstants.RDBMS.JDBC_INTERCEPTORS);
    String validationInterval = request.getParameter(DBConstants.RDBMS.VALIDATION_INTERVAL);
    String jmxEnabled = request.getParameter(DBConstants.RDBMS.JMX_ENABLED);
    String fairQueue = request.getParameter(DBConstants.RDBMS.FAIR_QUEUE);
    String abandonWhenPercentageFull = request.getParameter(DBConstants.RDBMS.ABANDON_WHEN_PERCENTAGE_FULL);
    String maxAge = request.getParameter(DBConstants.RDBMS.MAX_AGE);
    String useEquals = request.getParameter(DBConstants.RDBMS.USE_EQUALS);
    String suspectTimeout = request.getParameter(DBConstants.RDBMS.SUSPECT_TIMEOUT);
    String validationQueryTimeout = request.getParameter(DBConstants.RDBMS.VALIDATION_QUERY_TIMEOUT);
    String alternateUserNameAllowed = request.getParameter(DBConstants.RDBMS.ALTERNATE_USERNAME_ALLOWED);
    String dynamicUserAuthClass = request.getParameter(DBConstants.RDBMS.DYNAMIC_USER_AUTH_CLASS);

    String excelDatasource = request.getParameter(DBConstants.Excel.DATASOURCE);
    
    boolean useQueryMode = Boolean.parseBoolean(request.getParameter("useQueryModeValue"));

    String rdfDatasource = request.getParameter(DBConstants.RDF.DATASOURCE);

    String sparqlDatasource = request.getParameter(DBConstants.SPARQL.DATASOURCE);

    String csvDatasource = request.getParameter(DBConstants.CSV.DATASOURCE);
    String csvColumnSeperator = request.getParameter(DBConstants.CSV.COLUMN_SEPARATOR);
    String csvStartingRow = request.getParameter(DBConstants.CSV.STARTING_ROW);
    String csvMaxRowCount = request.getParameter(DBConstants.CSV.MAX_ROW_COUNT);
    String csvHasHeader = request.getParameter(DBConstants.CSV.HAS_HEADER);
    String csvHeaderRow = request.getParameter(DBConstants.CSV.HEADER_ROW);

    String jndiContextClass = request.getParameter(DBConstants.JNDI.INITIAL_CONTEXT_FACTORY);
    String jndiProviderUrl = request.getParameter(DBConstants.JNDI.PROVIDER_URL);
    String jndiResourceName = request.getParameter(DBConstants.JNDI.RESOURCE_NAME);
    String jndiUserName = request.getParameter(DBConstants.JNDI.USERNAME);
    String jndiPassword = request.getParameter(DBConstants.JNDI.PASSWORD);

    String mongoDBServers = request.getParameter(DBConstants.MongoDB.SERVERS);
    String mongoDBDatabase = request.getParameter(DBConstants.MongoDB.DATABASE);
    String mongoDBAuthenticationType = request.getParameter(DBConstants.MongoDB.AUTHENTICATION_TYPE);
    String mongoDBUserName = request.getParameter(DBConstants.MongoDB.USERNAME);
    String mongoDBPassword = request.getParameter(DBConstants.MongoDB.PASSWORD);
    String mongoDBWriteConcern = request.getParameter(DBConstants.MongoDB.WRITE_CONCERN);
    String mongoDBReadPreference = request.getParameter(DBConstants.MongoDB.READ_PREFERENCE);
    String mongoDBConnectTimeout = request.getParameter(DBConstants.MongoDB.CONNECT_TIMEOUT);
    String mongoDBMaxWait = request.getParameter(DBConstants.MongoDB.MAX_WAIT_TIME);
    String mongoDBSocketTimeout = request.getParameter(DBConstants.MongoDB.SOCKET_TIMEOUT);
    String mongoDBConnectionsPerHost = request.getParameter(DBConstants.MongoDB.CONNECTIONS_PER_HOST);
    String mongoDBThreadsAllowed= request.getParameter(DBConstants.MongoDB.THREADS_ALLOWED_TO_BLOCK_CONN_MULTIPLIER);

    String gspreadDatasource = request.getParameter(DBConstants.GSpread.DATASOURCE);
    String gspreadVisibility = request.getParameter(DBConstants.GSpread.VISIBILITY);
//    String gspreadUserName = request.getParameter(DBConstants.GSpread.USERNAME);
//    String gspreadPassword = request.getParameter(DBConstants.GSpread.PASSWORD);
    String gspreadClientId = request.getParameter(DBConstants.GSpread.CLIENT_ID);
    String gspreadClientSecret = request.getParameter(DBConstants.GSpread.CLIENT_SECRET);
    String gspreadRefreshToken = request.getParameter(DBConstants.GSpread.REFRESH_TOKEN);
    String gspreadSheetName = request.getParameter(DBConstants.GSpread.SHEET_NAME);

    String detailedServiceName = request.getParameter("detailedServiceName");

    String configuration = request.getParameter("config");

    String carbonDatasourceName = request.getParameter(DBConstants.CarbonDatasource.NAME);

    String cassandraServers = request.getParameter(DBConstants.Cassandra.CASSANDRA_SERVERS);
    String cassandraKeySpace = request.getParameter(DBConstants.Cassandra.KEYSPACE);
    String cassandraPort = request.getParameter(DBConstants.Cassandra.PORT);
    String cassandraClusterName = request.getParameter(DBConstants.Cassandra.CLUSTER_NAME);
    String cassandraCompression = request.getParameter(DBConstants.Cassandra.COMPRESSION);
    String cassandraUsername = request.getParameter(DBConstants.Cassandra.USERNAME);
    String cassandraPassword = request.getParameter(DBConstants.Cassandra.PASSWORD);
    String cassandraLoadBalancingPolicy = request.getParameter(DBConstants.Cassandra.LOAD_BALANCING_POLICY);
    String cassandraJMXReporting = request.getParameter(DBConstants.Cassandra.ENABLE_JMX_REPORTING);
    String cassandraMetrics = request.getParameter(DBConstants.Cassandra.ENABLE_METRICS);
    String cassandraLocalCoreConnPerHost = request.getParameter(DBConstants.Cassandra.LOCAL_CORE_CONNECTIONS_PER_HOST);
    String cassandraRemoteCoreConnPerHost = request.getParameter(DBConstants.Cassandra.REMOTE_CORE_CONNECTIONS_PER_HOST);
    String cassandraLocalMaxConnPerHost = request.getParameter(DBConstants.Cassandra.LOCAL_MAX_CONNECTIONS_PER_HOST);
    String cassandraRemoteMaxConnPerHost = request.getParameter(DBConstants.Cassandra.REMOTE_MAX_CONNECTIONS_PER_HOST);
    String cassandraLocalMaxSimulReq = request.getParameter(DBConstants.Cassandra.LOCAL_MAX_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST);
    String cassandraRemoteMaxSimulReq = request.getParameter(DBConstants.Cassandra.REMOTE_MAX_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST);
    String cassandraLocalMinSimulReq = request.getParameter(DBConstants.Cassandra.LOCAL_MIN_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST);
    String cassandraRemoteMinSimulReq = request.getParameter(DBConstants.Cassandra.REMOTE_MIN_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST);
    String cassandraProtocolVer = request.getParameter(DBConstants.Cassandra.PROTOCOL_VERSION);
    String cassandraConsistencyLevel = request.getParameter(DBConstants.Cassandra.CONSISTENCY_LEVEL);
    String cassandraFetchSize = request.getParameter(DBConstants.Cassandra.FETCH_SIZE);
    String cassandraSerialConsistencyLevel = request.getParameter(DBConstants.Cassandra.SERIAL_CONSISTENCY_LEVEL);
    String cassandraReconnectPolicy = request.getParameter(DBConstants.Cassandra.RECONNECTION_POLICY);
    String cassandraConstantReconnectPolicyDelay = request.getParameter(DBConstants.Cassandra.CONSTANT_RECONNECTION_POLICY_DELAY);
    String cassandraExpReconnectPolicyBaseDelay = request.getParameter(DBConstants.Cassandra.EXPONENTIAL_RECONNECTION_POLICY_BASE_DELAY);
    String cassandraExpReconnectPolicyMaxDelay = request.getParameter(DBConstants.Cassandra.EXPONENTIAL_RECONNECTION_POLICY_MAX_DELAY);
    String cassandraRetryPolicy = request.getParameter(DBConstants.Cassandra.RETRY_POLICY);
    String cassandraConnTimeout = request.getParameter(DBConstants.Cassandra.CONNECTION_TIMEOUT_MILLIS);
    String cassandraKeepAlive = request.getParameter(DBConstants.Cassandra.KEEP_ALIVE);
    String cassandraReadTimeout = request.getParameter(DBConstants.Cassandra.READ_TIMEOUT_MILLIS);
    String cassandraReceiveBuffSize = request.getParameter(DBConstants.Cassandra.RECEIVER_BUFFER_SIZE);
    String cassandraSendBuffSize = request.getParameter(DBConstants.Cassandra.SEND_BUFFER_SIZE);
    String cassandraReuseAdrs = request.getParameter(DBConstants.Cassandra.REUSE_ADDRESS);
    String cassandraSoLinger = request.getParameter(DBConstants.Cassandra.SO_LINGER);
    String cassandraTCPNoDelay = request.getParameter(DBConstants.Cassandra.TCP_NODELAY);
    String cassandraSSL = request.getParameter(DBConstants.Cassandra.ENABLE_SSL);
    
    String customDSType = request.getParameter("customTypeValue");
    String customDSClassName = request.getParameter("customDataSourceClass");

    String webConfig;
    boolean isXAType = false;
    if (xaType != null) {
        isXAType = Boolean.parseBoolean(xaType);
    }
	
    boolean useSecretAliasForPassword = false;
    if (useSecretAliasForPasswordValue != null) {
    	useSecretAliasForPassword = Boolean.parseBoolean(useSecretAliasForPasswordValue);
    }
    
    if (configuration != null && configuration.equals("config")) {
        webConfig = request.getParameter("web_harvest_config_textArea");
    } else {
        webConfig = request.getParameter(DBConstants.WebDatasource.WEB_CONFIG);
    }
    webConfig = (webConfig == null) ? "" : webConfig;

    String flag = request.getParameter("flag");
    flag = (flag == null) ? "" : flag;
    String forwardTo = "dataSources.jsp?ordinal=1";
    boolean remove = true;
    Config dsConfig = null;
    if (datasourceId != null) {
    	dsConfig = dataService.getConfig(datasourceId);
    }
    if (cancelButton != null && (datasourceId == null || datasourceId.trim().length() == 0 || datasourceType == null || datasourceType.trim().length() == 0)) {
    } else if (cancelButton != null && dsConfig != null && !backupConfigProps.isEmpty()) {
   		dsConfig.setProperties((ArrayList<Property>) backupConfigProps);
    } else if (datasourceId != null) {
        if (flag.equals("") && dsConfig != null) {
            String message = "Datasource " + datasourceId + " is already available. Please use different datasource name.";
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        } else {
        if (dsConfig == null) {
            dsConfig = newConfig;
            dsConfig.setUseSecretAliasForPassword(useSecretAliasForPassword);
            if (useSecretAliasForPassword) {
            	dsPassword = passwordAlias;
//            	gspreadPassword = passwordAlias;
            	jndiPassword = passwordAlias;
            	password = passwordAlias;
            	dataService.setSecureVaultNamespace(DBConstants.SECUREVAULT_NAMESPACE);
            } 
            dataService.setConfig(dsConfig);
        }
        if (dsConfig != null) {
            if (flag.equals("delete")) {
                ArrayList<Query> queryList = dataService.getQueries();
                if (queryList.size() >= 0) {
                    for (int a = 0; a < queryList.size(); a++) {
                        if (datasourceId.equals(queryList.get(a).getConfigToUse())) {
                            String message = "Datasource " + datasourceId + " has been used by queries. Please remove them to proceed.";
                            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                            forwardTo = "dataSources.jsp?ordinal=1";
                            remove = false;
                        }
                    }
                }
                if (remove) {
                    dataService.removeConfig(dsConfig);
                }
            } else {
                if (DBConstants.DataSourceTypes.RDBMS.equals(datasourceType)) {
                    if (isXAType) {
                    	if (useSecretAliasForPassword) {
                        	password = passwordAlias;
                        	dataService.setSecureVaultNamespace(DBConstants.SECUREVAULT_NAMESPACE);
                        }
                        ArrayList<Property> property = new ArrayList<Property>();
                        Iterator<Property> iterator = dsConfig.getProperties().iterator();
                        while (iterator.hasNext()) {
                            Property availableProperty = iterator.next();
                            if (availableProperty.getName().equals(DBConstants.RDBMS.DATASOURCE_PROPS)) {
                                if (availableProperty.getValue() instanceof ArrayList) {
                                    ArrayList<Property> nestedPropertyList = (ArrayList<Property>) availableProperty.getValue();
                                    Iterator<Property> nestedPropertyIterator = nestedPropertyList.iterator();
                                    while (nestedPropertyIterator.hasNext()) {
                                        Property nestedProperty = nestedPropertyIterator.next();
                                        String propertyName = nestedProperty.getName();
                                        /* String propertyNameValue = request.getParameter(propertyName + "Name");
                                        if (!propertyName.equals(propertyNameValue)) {
                                        	nestedProperty.setName(propertyNameValue);
                                        } */
                                        	if (request.getParameter(propertyName) != null) {
                                        		if (request.getParameter("useSecretAliasFor"+propertyName) != null) {
                                        			nestedProperty.setUseSecretAlias(true);
                                        			dataService.setSecureVaultNamespace(DBConstants.SECUREVAULT_NAMESPACE);
                                        		} else {
                                        			nestedProperty.setUseSecretAlias(false);
                                        		}
                                        		nestedProperty.setValue(request.getParameter(propertyName));
                                        	}
                                        }

                                    for (int j = 0; j < propertyCount; j++) {
                                        Property newProperty = new Property();

                                        String propertyName = request.getParameter("propertyNameRaw" + j);
                                        String propertValue = request.getParameter("propertyValueRaw" + j);
                                       
                                        if (propertyName != null) {
                                            newProperty.setName(propertyName);
                                            newProperty.setValue((String) propertValue);
                                            boolean useSecretAlias = false;
                                            if(request.getParameter("useSecretAliasFor"+j) != null) {
                                            	//useSecretAlias = Boolean.parseBoolean(request.getParameter("useSecretAliasFor"+j));
                                            	//if (useSecretAlias) {
                                            		newProperty.setUseSecretAlias(true);
                                            		dataService.setSecureVaultNamespace(DBConstants.SECUREVAULT_NAMESPACE);
                                            	//}
                                            }
                                            nestedPropertyList.add(newProperty);
                                        }
                                    }
                                    break;
                                }
                            }
                            updateConfiguration(dsConfig, DBConstants.RDBMS.DATASOURCE_CLASSNAME, xaDataSourceClass);
                            updateConfiguration(dsConfig, DBConstants.RDBMS.DATASOURCE_PROPS, property);
                            dsConfig.setExposeAsOData(isOData);
						}
                        dsConfig.removeProperty(DBConstants.RDBMS.DRIVER_CLASSNAME);
                        dsConfig.removeProperty(DBConstants.RDBMS.URL);
                        dsConfig.removeProperty(DBConstants.RDBMS.USERNAME);
                        dsConfig.removeProperty(DBConstants.RDBMS.PASSWORD);
                    } else {
                    	dsConfig.setUseSecretAliasForPassword(useSecretAliasForPassword);
                        if (useSecretAliasForPassword) {
                        	dsPassword = passwordAlias;
                        	dataService.setSecureVaultNamespace(DBConstants.SECUREVAULT_NAMESPACE);
                        }
                        updateConfiguration(dsConfig, DBConstants.RDBMS.DRIVER_CLASSNAME, driverClass);
                        updateConfiguration(dsConfig, DBConstants.RDBMS.URL, jdbcUrl);
                        updateConfiguration(dsConfig, DBConstants.RDBMS.USERNAME, dsUserName);
                        updateConfiguration(dsConfig, DBConstants.RDBMS.PASSWORD, dsPassword);
                        dsConfig.setExposeAsOData(isOData);
                        dsConfig.removeProperty(DBConstants.RDBMS.DATASOURCE_CLASSNAME);
                        dsConfig.removeProperty(DBConstants.RDBMS.DATASOURCE_PROPS);
                    }
                    if (!"TRANSACTION_UNKNOWN".equals(transactionIsolation)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.DEFAULT_TX_ISOLATION, transactionIsolation);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.DEFAULT_TX_ISOLATION);
                    }
                    updateConfiguration(dsConfig, DBConstants.RDBMS.INITIAL_SIZE, initialSize);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.MAX_ACTIVE, maxPool);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.MAX_IDLE, maxIdle);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.MIN_IDLE, minPool);                                          
                    updateConfiguration(dsConfig, DBConstants.RDBMS.MAX_WAIT, maxWait);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.VALIDATION_QUERY, validationQuery);
                    if (!"true".equals(testOnBorrow)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.TEST_ON_BORROW, testOnBorrow);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.TEST_ON_BORROW);
                    }
                    if (!"false".equals(testOnReturn)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.TEST_ON_RETURN, testOnReturn);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.TEST_ON_RETURN);
                    }
                    if (!"false".equals(testWhileIdle)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.TEST_WHILE_IDLE, testWhileIdle);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.TEST_WHILE_IDLE);
                    }
                    updateConfiguration(dsConfig, DBConstants.RDBMS.TIME_BETWEEN_EVICTION_RUNS_MILLIS, timeBetweenEvictionRunsMillis);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.NUM_TESTS_PER_EVICTION_RUN, numTestsPerEvictionRun);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.MIN_EVICTABLE_IDLE_TIME_MILLIS, minEvictableIdleTimeMillis);
                    if (!"false".equals(removeAbandoned)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.REMOVE_ABANDONED, removeAbandoned);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.REMOVE_ABANDONED);
                    }
                    updateConfiguration(dsConfig, DBConstants.RDBMS.REMOVE_ABANDONED_TIMEOUT, removeAbandonedTimeout);
                    if (!"false".equals(logAbandoned)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.LOG_ABANDONED, logAbandoned);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.LOG_ABANDONED);
                    }
                    updateConfiguration(dsConfig,DBConstants.RDBMS.DEFAULT_CATALOG, defaultCatalog);
                    updateConfiguration(dsConfig,DBConstants.RDBMS.VALIDATOR_CLASSNAME, validatorClassName);
                    updateConfiguration(dsConfig,DBConstants.RDBMS.CONNECTION_PROPERTIES, connectionProperties);
                    updateConfiguration(dsConfig,DBConstants.RDBMS.INIT_SQL, initSql);
                    updateConfiguration(dsConfig,DBConstants.RDBMS.JDBC_INTERCEPTORS, jdbcInterceptors);
                    updateConfiguration(dsConfig,DBConstants.RDBMS.VALIDATION_INTERVAL, validationInterval);
                    updateConfiguration(dsConfig,DBConstants.RDBMS.ABANDON_WHEN_PERCENTAGE_FULL, abandonWhenPercentageFull);
                    updateConfiguration(dsConfig,DBConstants.RDBMS.MAX_AGE, maxAge);
                    updateConfiguration(dsConfig,DBConstants.RDBMS.SUSPECT_TIMEOUT, suspectTimeout);
                    updateConfiguration(dsConfig,DBConstants.RDBMS.VALIDATION_QUERY_TIMEOUT, validationQueryTimeout);
                    if (!"false".equals(defaultAutoCommit)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.AUTO_COMMIT, defaultAutoCommit);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.AUTO_COMMIT);
                    }
                    if (!"false".equals(defaultReadOnly)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.DEFAULT_READONLY, defaultReadOnly);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.DEFAULT_READONLY);
                    }
                    if (!"false".equals(jmxEnabled)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.JMX_ENABLED, jmxEnabled);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.JMX_ENABLED);
                    }
                    if (!"false".equals(fairQueue)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.FAIR_QUEUE, fairQueue);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.FAIR_QUEUE);
                    }
                    if (!"false".equals(alternateUserNameAllowed)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.ALTERNATE_USERNAME_ALLOWED, alternateUserNameAllowed);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.ALTERNATE_USERNAME_ALLOWED);
                    }
                    if (!"false".equals(useEquals)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.USE_EQUALS, useEquals);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.USE_EQUALS);
                    }
                    updateConfiguration(dsConfig, DBConstants.RDBMS.DYNAMIC_USER_AUTH_CLASS, dynamicUserAuthClass);
                    Iterator<Property> iterator = dsConfig.getProperties().iterator();
                    ArrayList<DynamicAuthConfiguration.Entry> dynamicUserList = new ArrayList<DynamicAuthConfiguration.Entry>();
                    DynamicAuthConfiguration dynamicAuthConfiguration = new DynamicAuthConfiguration();
                    while (iterator.hasNext()) {
                        Property availableProperty = iterator.next();
                        if (availableProperty.getName().equals(DBConstants.RDBMS.DYNAMIC_USER_AUTH_MAPPING)) {
                            if (availableProperty.getValue() instanceof DynamicAuthConfiguration) {
                                for (int j = 0; j < staticUserMappingsCount; j++) {
                                    DynamicAuthConfiguration.Entry dynamicUserEntry = new DynamicAuthConfiguration.Entry();

                                    String carbonUsername = request.getParameter("carbonUsernameRaw" + j);
                                    String dbUsername = request.getParameter("dbUsernameRaw" + j);
                                    String dbUserPwd = request.getParameter("dbPwdRaw" + j);
                                    
                                    if (carbonUsername != null) {
                                        dynamicUserEntry.setRequest(carbonUsername);
                                        dynamicUserEntry.setUsername(dbUsername);
                                        dynamicUserEntry.setPassword(dbUserPwd);

                                        dynamicUserList.add(dynamicUserEntry);
                                    }
                                }
                                dynamicAuthConfiguration.setEntries(dynamicUserList);
                                break;
                            }
                        }
                    }
                    if (dynamicUserList.size() > 0) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.DYNAMIC_USER_AUTH_MAPPING, dynamicAuthConfiguration);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.DYNAMIC_USER_AUTH_MAPPING);
                    }
                } else if (DBConstants.DataSourceTypes.EXCEL.equals(datasourceType)) {
                	if (useQueryMode) {
                		String excelQueryModeUrl = DBConstants.DSSQLDriverPrefixes.EXCEL_PREFIX + ":" +
                			DBConstants.DSSQLDriverPrefixes.FILE_PATH + "=" + excelDatasource;
                		updateConfiguration(dsConfig, DBConstants.RDBMS.DRIVER_CLASSNAME, DBConstants.SQL_DRIVER_CLASS_NAME);
                		updateConfiguration(dsConfig, DBConstants.RDBMS.URL, excelQueryModeUrl);
                		
                		dsConfig.removeProperty(DBConstants.Excel.DATASOURCE);
                	} else {
                		updateConfiguration(dsConfig, DBConstants.Excel.DATASOURCE, excelDatasource);
                		
                		dsConfig.removeProperty(DBConstants.RDBMS.DRIVER_CLASSNAME);
                		dsConfig.removeProperty(DBConstants.RDBMS.URL);
                    }
               } else if (DBConstants.DataSourceTypes.RDF.equals(datasourceType)) {
                    updateConfiguration(dsConfig, DBConstants.RDF.DATASOURCE, rdfDatasource);
                } else if (DBConstants.DataSourceTypes.SPARQL.equals(datasourceType)) {
                    updateConfiguration(dsConfig, DBConstants.SPARQL.DATASOURCE, sparqlDatasource);
                } else if (DBConstants.DataSourceTypes.CSV.equals(datasourceType)) {
                    updateConfiguration(dsConfig, DBConstants.CSV.DATASOURCE, csvDatasource);
                    updateConfiguration(dsConfig, DBConstants.CSV.COLUMN_SEPARATOR, csvColumnSeperator);
                    updateConfiguration(dsConfig, DBConstants.CSV.STARTING_ROW, csvStartingRow);
                    updateConfiguration(dsConfig, DBConstants.CSV.MAX_ROW_COUNT, csvMaxRowCount);
                    updateConfiguration(dsConfig, DBConstants.CSV.HAS_HEADER, csvHasHeader);
                    updateConfiguration(dsConfig, DBConstants.CSV.HEADER_ROW, csvHeaderRow);
                } else if (DBConstants.DataSourceTypes.JNDI.equals(datasourceType)) {
                	if (useSecretAliasForPassword) {
                    	jndiPassword = passwordAlias;
                    	dataService.setSecureVaultNamespace(DBConstants.SECUREVAULT_NAMESPACE);
                    }
                    updateConfiguration(dsConfig, DBConstants.JNDI.INITIAL_CONTEXT_FACTORY, jndiContextClass);
                    updateConfiguration(dsConfig, DBConstants.JNDI.PROVIDER_URL, jndiProviderUrl);
                    updateConfiguration(dsConfig, DBConstants.JNDI.RESOURCE_NAME, jndiResourceName);
                    updateConfiguration(dsConfig, DBConstants.JNDI.USERNAME, jndiUserName);
                    updateConfiguration(dsConfig, DBConstants.JNDI.PASSWORD, jndiPassword);
                } else if (DBConstants.DataSourceTypes.MONGODB.equals(datasourceType)) {
                    updateConfiguration(dsConfig, DBConstants.MongoDB.SERVERS, mongoDBServers);
                    updateConfiguration(dsConfig, DBConstants.MongoDB.DATABASE, mongoDBDatabase);
                    updateConfiguration(dsConfig, DBConstants.MongoDB.AUTHENTICATION_TYPE, mongoDBAuthenticationType);
                    updateConfiguration(dsConfig, DBConstants.MongoDB.USERNAME, mongoDBUserName);
                    updateConfiguration(dsConfig, DBConstants.MongoDB.PASSWORD, mongoDBPassword);
                    updateConfiguration(dsConfig, DBConstants.MongoDB.WRITE_CONCERN, mongoDBWriteConcern);
                    updateConfiguration(dsConfig, DBConstants.MongoDB.READ_PREFERENCE, mongoDBReadPreference);
                    updateConfiguration(dsConfig, DBConstants.MongoDB.CONNECT_TIMEOUT, mongoDBConnectTimeout);
                    updateConfiguration(dsConfig, DBConstants.MongoDB.MAX_WAIT_TIME, mongoDBMaxWait);
                    updateConfiguration(dsConfig, DBConstants.MongoDB.SOCKET_TIMEOUT, mongoDBSocketTimeout);
                    updateConfiguration(dsConfig, DBConstants.MongoDB.CONNECTIONS_PER_HOST, mongoDBConnectionsPerHost);
                    updateConfiguration(dsConfig, DBConstants.MongoDB.THREADS_ALLOWED_TO_BLOCK_CONN_MULTIPLIER, mongoDBThreadsAllowed);
                } else if (DBConstants.DataSourceTypes.GDATA_SPREADSHEET.equals(datasourceType)) {
                	if (useSecretAliasForPassword) {
//                    	gspreadPassword = passwordAlias;
                    	dataService.setSecureVaultNamespace(DBConstants.SECUREVAULT_NAMESPACE);
                    }
					if (useQueryMode) {
						String gspreadQueryModeUrl;
//						String gspreadQueryModeUrl = DBConstants.DSSQLDriverPrefixes.GSPRED_PREFIX + ":" +
//	                			DBConstants.DSSQLDriverPrefixes.FILE_PATH + "=" + gspreadDatasource + ";" +
//	                			DBConstants.GSpread.SHEET_NAME + "=" + gspreadSheetName +";visibility=" + gspreadVisibility;
						updateConfiguration(dsConfig, DBConstants.RDBMS.DRIVER_CLASSNAME, DBConstants.SQL_DRIVER_CLASS_NAME);
//                        updateConfiguration(dsConfig, DBConstants.RDBMS.URL, gspreadQueryModeUrl);
                        if (gspreadVisibility.equals(DBConstants.GSpreadVisibility.PRIVATE)) {
                            gspreadClientId = URLEncoder.encode(gspreadClientId,"UTF-8");
                            gspreadClientSecret = URLEncoder.encode(gspreadClientSecret,"UTF-8");
                            gspreadRefreshToken = URLEncoder.encode(gspreadRefreshToken,"UTF-8");
                            gspreadQueryModeUrl = DBConstants.DSSQLDriverPrefixes.GSPRED_PREFIX + ":" +
                                    DBConstants.DSSQLDriverPrefixes.FILE_PATH + "=" + gspreadDatasource + ";" +
                                    DBConstants.GSpread.SHEET_NAME + "=" + gspreadSheetName +";visibility=" +
                                    gspreadVisibility + ";clientId=" + gspreadClientId +
                                    ";clientSecret=" + gspreadClientSecret +
                                    ";refreshToken=" + gspreadRefreshToken;
//		                    updateConfiguration(dsConfig, DBConstants.RDBMS.USERNAME, gspreadUserName);todo
//		                    updateConfiguration(dsConfig, DBConstants.RDBMS.PASSWORD, gspreadPassword);todo change this to be merge with url (clientId and secret)
	                    } else {
                            gspreadQueryModeUrl = DBConstants.DSSQLDriverPrefixes.GSPRED_PREFIX + ":" +
                                    DBConstants.DSSQLDriverPrefixes.FILE_PATH + "=" + gspreadDatasource + ";" +
                                    DBConstants.GSpread.SHEET_NAME + "=" + gspreadSheetName +";visibility=" + gspreadVisibility;
	                    	dsConfig.removeProperty(DBConstants.RDBMS.USERNAME);
	                    	dsConfig.removeProperty(DBConstants.RDBMS.PASSWORD);
	                    }
                        updateConfiguration(dsConfig, DBConstants.RDBMS.URL, gspreadQueryModeUrl);
                        
                        dsConfig.removeProperty(DBConstants.GSpread.DATASOURCE);
                		dsConfig.removeProperty(DBConstants.GSpread.VISIBILITY);
                		dsConfig.removeProperty(DBConstants.GSpread.USERNAME);
                		dsConfig.removeProperty(DBConstants.GSpread.PASSWORD);
                	} else {
	                    updateConfiguration(dsConfig, DBConstants.GSpread.DATASOURCE, gspreadDatasource);
	                    updateConfiguration(dsConfig, DBConstants.GSpread.VISIBILITY, gspreadVisibility);
//	                    updateConfiguration(dsConfig, DBConstants.GSpread.USERNAME, gspreadUserName);
//		                updateConfiguration(dsConfig, DBConstants.GSpread.PASSWORD, gspreadPassword);
                        updateConfiguration(dsConfig, DBConstants.GSpread.CLIENT_ID, gspreadClientId);
                        updateConfiguration(dsConfig, DBConstants.GSpread.CLIENT_SECRET, gspreadClientSecret);
                        updateConfiguration(dsConfig, DBConstants.GSpread.REFRESH_TOKEN, gspreadRefreshToken);

	                    dsConfig.removeProperty(DBConstants.RDBMS.DRIVER_CLASSNAME);
                		dsConfig.removeProperty(DBConstants.RDBMS.URL);
                		dsConfig.removeProperty(DBConstants.RDBMS.USERNAME);
                		dsConfig.removeProperty(DBConstants.RDBMS.PASSWORD);
                	}
                } else if (DBConstants.DataSourceTypes.CARBON.equals(datasourceType)) {
                    if (carbonDatasourceName == null || carbonDatasourceName.length() == 0) {
                        String message = "Please select a valid datasource name";
                        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                        forwardTo = "addDataSource.jsp?configId=" + datasourceId + "&ordinal=1";
                    } else {
                        updateConfiguration(dsConfig, DBConstants.CarbonDatasource.NAME, carbonDatasourceName);
                    }
                } else if (DBConstants.DataSourceTypes.WEB.equals(datasourceType)) {
                    updateConfiguration(dsConfig, DBConstants.WebDatasource.WEB_CONFIG, webConfig);
                } else if (DBConstants.DataSourceTypes.CASSANDRA.equals(datasourceType)) {
                    updateConfiguration(dsConfig, DBConstants.Cassandra.CASSANDRA_SERVERS, cassandraServers);
                    dsConfig.setExposeAsOData(isOData);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.KEYSPACE, cassandraKeySpace);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.PORT, cassandraPort);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.CLUSTER_NAME, cassandraClusterName);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.COMPRESSION, cassandraCompression);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.USERNAME, cassandraUsername);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.PASSWORD, cassandraPassword);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.LOAD_BALANCING_POLICY, cassandraLoadBalancingPolicy);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.ENABLE_JMX_REPORTING, cassandraJMXReporting);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.ENABLE_METRICS, cassandraMetrics);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.LOCAL_CORE_CONNECTIONS_PER_HOST, cassandraLocalCoreConnPerHost);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.REMOTE_CORE_CONNECTIONS_PER_HOST, cassandraRemoteCoreConnPerHost);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.LOCAL_MAX_CONNECTIONS_PER_HOST, cassandraLocalMaxConnPerHost);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.REMOTE_MAX_CONNECTIONS_PER_HOST, cassandraRemoteMaxConnPerHost);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.LOCAL_MAX_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST, cassandraLocalMaxSimulReq);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.REMOTE_MAX_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST, cassandraRemoteMaxSimulReq);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.LOCAL_MIN_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST, cassandraLocalMinSimulReq);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.REMOTE_MIN_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST, cassandraRemoteMinSimulReq);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.PROTOCOL_VERSION, cassandraProtocolVer);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.CONSISTENCY_LEVEL, cassandraConsistencyLevel);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.FETCH_SIZE, cassandraFetchSize);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.SERIAL_CONSISTENCY_LEVEL, cassandraSerialConsistencyLevel);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.RECONNECTION_POLICY, cassandraReconnectPolicy);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.CONSTANT_RECONNECTION_POLICY_DELAY, cassandraConstantReconnectPolicyDelay);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.EXPONENTIAL_RECONNECTION_POLICY_BASE_DELAY, cassandraExpReconnectPolicyBaseDelay);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.EXPONENTIAL_RECONNECTION_POLICY_MAX_DELAY, cassandraExpReconnectPolicyMaxDelay);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.RETRY_POLICY, cassandraRetryPolicy);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.CONNECTION_TIMEOUT_MILLIS, cassandraConnTimeout);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.KEEP_ALIVE, cassandraKeepAlive);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.READ_TIMEOUT_MILLIS, cassandraReadTimeout);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.RECEIVER_BUFFER_SIZE, cassandraReceiveBuffSize);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.SEND_BUFFER_SIZE, cassandraSendBuffSize);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.REUSE_ADDRESS, cassandraReuseAdrs);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.SO_LINGER, cassandraSoLinger);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.TCP_NODELAY, cassandraTCPNoDelay);
                    updateConfiguration(dsConfig, DBConstants.Cassandra.ENABLE_SSL, cassandraSSL);

                } else if (DBConstants.DataSourceTypes.CUSTOM.equals(datasourceType)) {
                	ArrayList<Property> property = new ArrayList<Property>();
                    Iterator<Property> iterator = dsConfig.getProperties().iterator();
                    while (iterator.hasNext()) {
                        Property availableProperty = iterator.next();
                        if (availableProperty.getName().equals(DBConstants.CustomDataSource.DATA_SOURCE_PROPS)) {
                            if (availableProperty.getValue() instanceof ArrayList) {
                                ArrayList<Property> nestedPropertyList = (ArrayList<Property>) availableProperty.getValue();
                                Iterator<Property> nestedPropertyIterator = nestedPropertyList.iterator();
                                while (nestedPropertyIterator.hasNext()) {
                                    Property nestedProperty = nestedPropertyIterator.next();
                                    String propertyName = nestedProperty.getName();
                                    /* String propertyNameValue = request.getParameter(propertyName + "Name");
                                    if (!propertyName.equals(propertyNameValue)) {
                                    	nestedProperty.setName(propertyNameValue);
                                    } */
                                    	if (request.getParameter(propertyName) != null) {
                                    		if (request.getParameter("useSecretAliasFor"+propertyName) != null) {
                                    			nestedProperty.setUseSecretAlias(true);
                                    			dataService.setSecureVaultNamespace(DBConstants.SECUREVAULT_NAMESPACE);
                                    		} else {
                                    			nestedProperty.setUseSecretAlias(false);
                                    		}
                                    		nestedProperty.setValue(request.getParameter(propertyName));
                                    	}
                                    }

                                for (int j = 0; j < propertyCount; j++) {
                                    Property newProperty = new Property();

                                    String propertyName = request.getParameter("propertyNameRaw" + j);
                                    String propertValue = request.getParameter("propertyValueRaw" + j);
                                   
                                    if (propertyName != null) {
                                        newProperty.setName(propertyName);
                                        newProperty.setValue((String) propertValue);
                                        boolean useSecretAlias = false;
                                        if(request.getParameter("useSecretAliasFor"+j) != null) {
                                        	//useSecretAlias = Boolean.parseBoolean(request.getParameter("useSecretAliasFor"+j));
                                        	//if (useSecretAlias) {
                                        		newProperty.setUseSecretAlias(true);
                                        		dataService.setSecureVaultNamespace(DBConstants.SECUREVAULT_NAMESPACE);
                                        	//}
                                        }
                                        nestedPropertyList.add(newProperty);
                                    }
                                }
                                break;
                            }
                        }
                        if (customDSType.equals(DBConstants.DataSourceTypes.CUSTOM_QUERY)) {
                        	updateConfiguration(dsConfig, DBConstants.CustomDataSource.DATA_SOURCE_QUERY_CLASS, customDSClassName);
                        } else {
                        	updateConfiguration(dsConfig, DBConstants.CustomDataSource.DATA_SOURCE_TABULAR_CLASS, customDSClassName);
                        }
                        updateConfiguration(dsConfig, DBConstants.CustomDataSource.DATA_SOURCE_PROPS, property);
					}
                    	if (customDSType.equals(DBConstants.DataSourceTypes.CUSTOM_QUERY)) {
                    		dsConfig.removeProperty(DBConstants.CustomDataSource.DATA_SOURCE_TABULAR_CLASS);
                    	} else {
                    		dsConfig.removeProperty(DBConstants.CustomDataSource.DATA_SOURCE_QUERY_CLASS);
                    	}
                }
                dsConfig.setUseSecretAliasForPassword(useSecretAliasForPassword);
            }
        }
    }
    }
%>
<table>
    <input type="hidden" id="selectbox" value="<%=selectBox%>"/>
    <input type="hidden" id="configId" value="<%=request.getParameter("configId")%>"/>
    <input type="hidden" id="selectedType" value="<%=request.getParameter("selectedType")%>"/>
    <input type="hidden" id="serviceName" value="<%=serviceName%>"/>
    <input type="hidden" id="detailedServiceName" value="<%=detailedServiceName%>"/>
</table>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>