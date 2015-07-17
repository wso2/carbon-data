/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.datasource.reader.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.SocketOptions;
import org.wso2.carbon.datasource.reader.cassandra.config.CassandraDataSourceConfiguration;
import org.wso2.carbon.datasource.reader.cassandra.config.pooling.*;
import org.wso2.carbon.datasource.reader.cassandra.config.socket.SocketOptionsConfig;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class CassandraDataSourceReaderUtil {

    public static Cluster loadConfiguration(String xmlConfiguration) throws DataSourceException {
        ByteArrayInputStream baos = null;
        try {
            Cluster.Builder builder = Cluster.builder();
            xmlConfiguration = CarbonUtils.replaceSystemVariablesInXml(xmlConfiguration);
            JAXBContext ctx = JAXBContext.newInstance(CassandraDataSourceConfiguration.class);
            baos = new ByteArrayInputStream(xmlConfiguration.getBytes());
            CassandraDataSourceConfiguration fileConfig = (CassandraDataSourceConfiguration) ctx.createUnmarshaller().unmarshal(baos);

            builder.addContactPoint(fileConfig.getContactPoints());

            //nullcheck
            builder.withPort(fileConfig.getPort());
            builder.withPoolingOptions(populatePoolingOptions(fileConfig));

            return builder.build();
        } catch (Exception e) {
            throw new DataSourceException("Error loading Cassandra Datasource configuration: " + e.getMessage(), e);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ignore) {
                    // ignore
                }
            }
        }
    }

    public static PoolingOptions populatePoolingOptions(CassandraDataSourceConfiguration config) {
        PoolingOptions options = new PoolingOptions();
        PoolingOptionsConfig poc = config.getPoolingOptionsConfig();
        if (poc != null) {
            CoreConnectionsPerHostConfig[] coreConnectionsPerHostz = poc.getCoreConnectionsPerHostz();
            MaxConnectionsPerHostConfig[] maxConnectionsPerHostsz = poc.getMaxConnectionsPerHostz();
            MaxConnectionThresholdConfig[] maxConnectionThresholdz = poc.getMaxThresholdz();
            MinConnectionThresholdConfig[] minConnectionThresholdz = poc.getMinThresholdz();
            MaxHostThresholdConfig[] maxHostThresholdz = poc.getMaxHostThresholdz();
            Integer heartbeatIntervalSeconds = poc.getHeartbeatIntervalSeconds();
            Integer poolTimeoutMillis = poc.getPoolTimeoutMillis();
            if (coreConnectionsPerHostz != null) {
                for (CoreConnectionsPerHostConfig conn : coreConnectionsPerHostz) {
                    options.setCoreConnectionsPerHost(conn.getHostDistance(), conn.getValue());
                }
            }
            if (maxConnectionsPerHostsz != null) {
                for (MaxConnectionsPerHostConfig conn : maxConnectionsPerHostsz) {
                    options.setMaxConnectionsPerHost(conn.getHostDistance(), conn.getValue());
                }
            }
            if (maxConnectionThresholdz != null) {
                for (MaxConnectionThresholdConfig threshold : maxConnectionThresholdz) {
                    options.setMaxSimultaneousRequestsPerConnectionThreshold(threshold.getHostDistance(), threshold.getValue());
                }
            }
            if (minConnectionThresholdz != null) {
                for (MinConnectionThresholdConfig threshold : minConnectionThresholdz) {
                    options.setMinSimultaneousRequestsPerConnectionThreshold(threshold.getHostDistance(), threshold.getValue());
                }
            }
            if (maxHostThresholdz != null) {
                for (MaxHostThresholdConfig threshold : maxHostThresholdz) {
                    options.setMaxSimultaneousRequestsPerHostThreshold(threshold.getHostDistance(), threshold.getValue());
                }
            }
            if (heartbeatIntervalSeconds != null) {
                options.setHeartbeatIntervalSeconds(heartbeatIntervalSeconds);

            }
            if (poolTimeoutMillis != null) {
                options.setPoolTimeoutMillis(poolTimeoutMillis);
            }
        }

        return options;
    }

    public static SocketOptions populateSocketOptions(CassandraDataSourceConfiguration config) {
        SocketOptions options = new SocketOptions();
        SocketOptionsConfig sock = config.getSocketOptionsConfig();
        if (sock != null) {
            options.setConnectTimeoutMillis(sock.getConnectTimeoutMillis());
            options.setKeepAlive(sock.getKeepAlive());
            options.setReceiveBufferSize(sock.getReceiveBufferSize());
            options.setReuseAddress(sock.getReuseAddress());
            options.setSendBufferSize(sock.getSendBufferSize());
            options.setSoLinger(sock.getSoLinger());
            options.setTcpNoDelay(sock.getTcpNoDelay());
        }
        return options;
    }

}
