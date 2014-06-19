/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.Map;

import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.DataSourceTypes;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.engine.DataService;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ProtocolOptions.Compression;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;
import com.datastax.driver.core.policies.ExponentialReconnectionPolicy;
import com.datastax.driver.core.policies.FallthroughRetryPolicy;
import com.datastax.driver.core.policies.LatencyAwarePolicy;
import com.datastax.driver.core.policies.LoggingRetryPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;

/**
 * Cassandra-CQL data source implementation.
 */
public class CassandraConfig extends Config {
    
    private Cluster cluster;
    
    private Session session;
    
    private boolean nativeBatchRequestsSupported;
        
    public CassandraConfig(DataService dataService, String configId, 
            Map<String, String> properties) throws DataServiceFault {
        super(dataService, configId, DataSourceTypes.CASSANDRA, properties);
        Builder builder = Cluster.builder();
        this.populateSettings(builder, properties);
        String keyspace = properties.get(DBConstants.Cassandra.KEYSPACE);        
        this.cluster = builder.build();
        if (keyspace != null && keyspace.trim().length() > 0) {
            this.session = this.cluster.connect(keyspace);
        } else {
            this.session = this.cluster.connect();
        }
        this.nativeBatchRequestsSupported = this.session.getCluster().
                getConfiguration().getProtocolOptions().getProtocolVersion() > 1;
    }
    
    public boolean isNativeBatchRequestsSupported() {
        return nativeBatchRequestsSupported;
    }
    
    private Builder populateLoadBalancingProp(Map<String, String> properties, Builder builder) throws DataServiceFault {
        String loadBalancingProp = properties.get(DBConstants.Cassandra.LOAD_BALANCING_POLICY);
        if (loadBalancingProp != null) {
            if ("LatencyAwareRoundRobinPolicy".equals(loadBalancingProp)) {
                builder = builder.withLoadBalancingPolicy(LatencyAwarePolicy.builder(
                        new RoundRobinPolicy()).build());
            } else if ("RoundRobinPolicy".equals(loadBalancingProp)) {
                builder = builder.withLoadBalancingPolicy(new RoundRobinPolicy());
            } else if ("TokenAwareRoundRobinPolicy".equals(loadBalancingProp)) {
                builder = builder.withLoadBalancingPolicy(new TokenAwarePolicy(
                        new RoundRobinPolicy()));
            } else {
                throw new DataServiceFault("Unsupported Cassandra load balancing "
                        + "policy: " + loadBalancingProp);
            }
        }
        return builder;
    }
    
    private Builder populateCredentials(Map<String, String> properties, Builder builder) {
        String usernameProp = properties.get(DBConstants.Cassandra.USERNAME);
        String passwordProp = properties.get(DBConstants.Cassandra.PASSWORD);
        if (usernameProp != null) {
            builder = builder.withCredentials(usernameProp, passwordProp);
        }
        return builder;
    }
    
    private Builder populatePoolingSettings(Map<String, String> properties, Builder builder) {
        String localCoreConnectionsPerHost = properties.get(DBConstants.Cassandra.LOCAL_CORE_CONNECTIONS_PER_HOST);
        String remoteCoreConnectionsPerHost = properties.get(DBConstants.Cassandra.REMOTE_CORE_CONNECTIONS_PER_HOST);
        String localMaxConnectionsPerHost = properties.get(DBConstants.Cassandra.LOCAL_MAX_CONNECTIONS_PER_HOST);
        String remoteMaxConnectionsPerHost = properties.get(DBConstants.Cassandra.REMOTE_MAX_CONNECTIONS_PER_HOST);
        String localMaxSimultaneousRequestsPerConnectionThreshold = properties.get(DBConstants.Cassandra.LOCAL_MAX_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST);
        String remoteMaxSimultaneousRequestsPerConnectionThreshold = properties.get(DBConstants.Cassandra.REMOTE_MAX_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST);
        String localMinSimultaneousRequestsPerConnectionThreshold = properties.get(DBConstants.Cassandra.LOCAL_MIN_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST);
        String remoteMinSimultaneousRequestsPerConnectionThreshold = properties.get(DBConstants.Cassandra.REMOTE_MIN_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST);
        PoolingOptions options = new PoolingOptions();
        if (localCoreConnectionsPerHost != null) {
            options.setCoreConnectionsPerHost(HostDistance.LOCAL, Integer.parseInt(localCoreConnectionsPerHost));
        }
        if (remoteCoreConnectionsPerHost != null) {
            options.setCoreConnectionsPerHost(HostDistance.REMOTE, Integer.parseInt(remoteCoreConnectionsPerHost));
        }
        if (localMaxConnectionsPerHost != null) {
            options.setMaxConnectionsPerHost(HostDistance.LOCAL, Integer.parseInt(localMaxConnectionsPerHost));
        }
        if (remoteMaxConnectionsPerHost != null) {
            options.setMaxConnectionsPerHost(HostDistance.REMOTE, Integer.parseInt(remoteMaxConnectionsPerHost));
        }        
        if (localMaxSimultaneousRequestsPerConnectionThreshold != null) {
            options.setMaxSimultaneousRequestsPerConnectionThreshold(HostDistance.LOCAL, Integer.parseInt(localMaxSimultaneousRequestsPerConnectionThreshold));
        }
        if (remoteMaxSimultaneousRequestsPerConnectionThreshold != null) {
            options.setMaxSimultaneousRequestsPerConnectionThreshold(HostDistance.REMOTE, Integer.parseInt(remoteMaxSimultaneousRequestsPerConnectionThreshold));
        }        
        if (localMinSimultaneousRequestsPerConnectionThreshold != null) {
            options.setMinSimultaneousRequestsPerConnectionThreshold(HostDistance.LOCAL, Integer.parseInt(localMinSimultaneousRequestsPerConnectionThreshold));
        }
        if (remoteMinSimultaneousRequestsPerConnectionThreshold != null) {
            options.setMinSimultaneousRequestsPerConnectionThreshold(HostDistance.REMOTE, Integer.parseInt(remoteMinSimultaneousRequestsPerConnectionThreshold));
        }        
        builder = builder.withPoolingOptions(options);
        return builder;
    }    
    
    private Builder populateQueryOptions(Map<String, String> properties, Builder builder) {
        String consistencyLevelProp = properties.get(DBConstants.Cassandra.CONSISTENCY_LEVEL);
        String serialConsistencyLevelProp = properties.get(DBConstants.Cassandra.SERIAL_CONSISTENCY_LEVEL);
        String fetchSize = properties.get(DBConstants.Cassandra.FETCH_SIZE);
        QueryOptions options = new QueryOptions();
        if (consistencyLevelProp != null) {
            options.setConsistencyLevel(ConsistencyLevel.valueOf(consistencyLevelProp));
        }
        if (serialConsistencyLevelProp != null) {
            options.setSerialConsistencyLevel(ConsistencyLevel.valueOf(serialConsistencyLevelProp));
        }
        if (fetchSize != null) {
            options.setFetchSize(Integer.parseInt(fetchSize));
        }
        return builder.withQueryOptions(options);
    }
    
    private Builder populateReconnectPolicy(Map<String, String> properties, Builder builder) throws DataServiceFault {
        String reconnectPolicyProp = properties.get(DBConstants.Cassandra.RECONNECTION_POLICY);
        if (reconnectPolicyProp != null) {
            if ("ConstantReconnectionPolicy".equals(reconnectPolicyProp)) {
                String constantReconnectionPolicyDelay = properties.get(DBConstants.Cassandra.CONSTANT_RECONNECTION_POLICY_DELAY);
                if (constantReconnectionPolicyDelay == null) {
                    throw new DataServiceFault("constantReconnectionPolicyDelay property must be set for ConstantReconnectionPolicy");
                }
                ConstantReconnectionPolicy policy = new ConstantReconnectionPolicy(Long.parseLong(constantReconnectionPolicyDelay));
                builder = builder.withReconnectionPolicy(policy);
            } else if ("ExponentialReconnectionPolicy".equals(reconnectPolicyProp)) {
                String exponentialReconnectionPolicyBaseDelay = properties.get(DBConstants.Cassandra.EXPONENTIAL_RECONNECTION_POLICY_BASE_DELAY);
                if (exponentialReconnectionPolicyBaseDelay == null) {
                    throw new DataServiceFault("exponentialReconnectionPolicyBaseDelay property must be set for ExponentialReconnectionPolicy");
                }
                String exponentialReconnectionPolicyMaxDelay = properties.get(DBConstants.Cassandra.EXPONENTIAL_RECONNECTION_POLICY_MAX_DELAY);
                if (exponentialReconnectionPolicyMaxDelay == null) {
                    throw new DataServiceFault("exponentialReconnectionPolicyMaxDelay property must be set for ExponentialReconnectionPolicy");
                }
                ExponentialReconnectionPolicy policy = new ExponentialReconnectionPolicy(Long.parseLong(exponentialReconnectionPolicyBaseDelay),
                        Long.parseLong(exponentialReconnectionPolicyMaxDelay));
                builder = builder.withReconnectionPolicy(policy);
            } else {
                throw new DataServiceFault("Unsupported Cassandra reconnection policy: " + reconnectPolicyProp);
            }
        }
        return builder;
    }
    
    private Builder populateRetrytPolicy(Map<String, String> properties, Builder builder) throws DataServiceFault {
        String retryPolicy = properties.get(DBConstants.Cassandra.RETRY_POLICY);
        if (retryPolicy != null) {
            if ("DefaultRetryPolicy".equals(retryPolicy)) {
                builder = builder.withRetryPolicy(DefaultRetryPolicy.INSTANCE);
            } else if ("DowngradingConsistencyRetryPolicy".equals(retryPolicy)) {
                builder = builder.withRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE);
            } else if ("FallthroughRetryPolicy".equals(retryPolicy)) {
                builder = builder.withRetryPolicy(FallthroughRetryPolicy.INSTANCE);
            } else if ("LoggingDefaultRetryPolicy".equals(retryPolicy)) {
                builder = builder.withRetryPolicy(new LoggingRetryPolicy(DefaultRetryPolicy.INSTANCE));
            } else if ("LoggingDowngradingConsistencyRetryPolicy".equals(retryPolicy)) {
                builder = builder.withRetryPolicy(new LoggingRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE));                
            } else if ("LoggingFallthroughRetryPolicy".equals(retryPolicy)) {
                builder = builder.withRetryPolicy(new LoggingRetryPolicy(FallthroughRetryPolicy.INSTANCE));                
            } else {
                throw new DataServiceFault("Invalid Cassandra retry policy: " + retryPolicy);
            }
        }
        return builder;
    }
    
    private Builder populateSocketOptions(Map<String, String> properties, Builder builder) throws DataServiceFault {
        String connectionTimeoutMillisProp = properties.get(DBConstants.Cassandra.CONNECTION_TIMEOUT_MILLIS);
        String keepAliveProp = properties.get(DBConstants.Cassandra.KEEP_ALIVE);
        String readTimeoutMillisProp = properties.get(DBConstants.Cassandra.READ_TIMEOUT_MILLIS);
        String receiveBufferSizeProp = properties.get(DBConstants.Cassandra.RECEIVER_BUFFER_SIZE);
        String reuseAddress = properties.get(DBConstants.Cassandra.REUSE_ADDRESS);
        String sendBufferSize = properties.get(DBConstants.Cassandra.SEND_BUFFER_SIZE);
        String soLinger = properties.get(DBConstants.Cassandra.SO_LINGER);
        String tcpNoDelay = properties.get(DBConstants.Cassandra.TCP_NODELAY);
        SocketOptions options = new SocketOptions();
        if (connectionTimeoutMillisProp != null) {
            options.setConnectTimeoutMillis(Integer.parseInt(connectionTimeoutMillisProp));
        }
        if (keepAliveProp != null) {
            options.setKeepAlive(Boolean.parseBoolean(keepAliveProp));
        }
        if (readTimeoutMillisProp != null) {
            options.setReadTimeoutMillis(Integer.parseInt(readTimeoutMillisProp));
        }
        if (receiveBufferSizeProp != null) {
            options.setReceiveBufferSize(Integer.parseInt(receiveBufferSizeProp));
        }
        if (reuseAddress != null) {
            options.setReuseAddress(Boolean.parseBoolean(reuseAddress));
        }
        if (sendBufferSize != null) {
            options.setSendBufferSize(Integer.parseInt(sendBufferSize));
        }
        if (soLinger != null) {
            options.setSoLinger(Integer.parseInt(soLinger));
        }
        if (tcpNoDelay != null) {
            options.setTcpNoDelay(Boolean.parseBoolean(tcpNoDelay));
        }
        return builder.withSocketOptions(options);
    }
    
    private Builder populateSettings(Builder builder, Map<String, String> properties) throws DataServiceFault {
        String serversParam = properties.get(DBConstants.Cassandra.CASSANDRA_SERVERS);
        String[] servers = serversParam.split(",");
        for (String server : servers) {
            builder = builder.addContactPoint(server);
        }
        String portProp = properties.get(DBConstants.Cassandra.PORT);
        if (portProp != null) {
            builder = builder.withPort(Integer.parseInt(portProp));
        }
        String clusterNameProp = properties.get(DBConstants.Cassandra.CLUSTER_NAME);
        if (clusterNameProp != null) {
            builder = builder.withClusterName(clusterNameProp);
        }
        String compressionProp = properties.get(DBConstants.Cassandra.COMPRESSION);
        if (compressionProp != null) {
            builder = builder.withCompression(Compression.valueOf(compressionProp));
        }        
        builder = this.populateCredentials(properties, builder);        
        builder = this.populateLoadBalancingProp(properties, builder);          
        String enableJMXProp = properties.get(DBConstants.Cassandra.ENABLE_JMX_REPORTING);
        if (enableJMXProp != null) {
            if (!Boolean.parseBoolean(enableJMXProp)) {
                builder = builder.withoutJMXReporting();
            }
        }
        String enableMetricsProp = properties.get(DBConstants.Cassandra.ENABLE_METRICS);
        if (enableMetricsProp != null) {
            if (!Boolean.parseBoolean(enableMetricsProp)) {
                builder = builder.withoutMetrics();
            }
        }        
        builder = this.populatePoolingSettings(properties, builder);        
        String versionProp = properties.get(DBConstants.Cassandra.PROTOCOL_VERSION);
        if (versionProp != null) {
            builder = builder.withProtocolVersion(Integer.parseInt(versionProp));
        }
        builder = this.populateQueryOptions(properties, builder);
        builder = this.populateReconnectPolicy(properties, builder);
        builder = this.populateRetrytPolicy(properties, builder);
        builder = this.populateSocketOptions(properties, builder);
        String enableSSLProp = properties.get(DBConstants.Cassandra.ENABLE_SSL);
        if (enableSSLProp != null) {
            if (Boolean.parseBoolean(enableSSLProp)) {
                builder = builder.withSSL();
            }
        }
        return builder;
    }
    
    public Session getSession() {
        return session;
    }
    
    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public synchronized void close() {
        this.session.close();
        this.cluster.close();
    }

}
