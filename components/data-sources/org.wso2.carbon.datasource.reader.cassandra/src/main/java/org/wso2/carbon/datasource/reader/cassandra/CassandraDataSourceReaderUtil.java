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

import com.datastax.driver.core.*;
import org.wso2.carbon.datasource.reader.cassandra.config.CassandraDataSourceConfiguration;
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

            String[] contactPoints = fileConfig.getContactPoints().split(",");

            for (String contactPoint : contactPoints) {
                if (contactPoint.length() > 0) {
                    builder.addContactPoint(contactPoint);
                }
            }

            if (fileConfig.getClusterName() != null) {
                builder.withClusterName(fileConfig.getClusterName());
            }
            if (fileConfig.getCompression() != null) {
                builder.withCompression(ProtocolOptions.Compression.valueOf(fileConfig.getCompression()));
            }
            if (fileConfig.getMaxSchemaAgreementWaitSeconds() != null) {
                builder.withMaxSchemaAgreementWaitSeconds(fileConfig.getMaxSchemaAgreementWaitSeconds());
            }
            if (fileConfig.getProtocolVersion() != null) {
                builder.withProtocolVersion(ProtocolVersion.valueOf(fileConfig.getProtocolVersion()));
            }
            if (fileConfig.getPort() != null) {
                builder.withPort(fileConfig.getPort());
            }
            if ((fileConfig.getUsername() != null) && (fileConfig.getPassword() != null)) {
                builder.withCredentials(fileConfig.getUsername(), fileConfig.getPassword());
            }
            if (fileConfig.getQueryOptionsConfig() != null) {
                builder.withQueryOptions(fileConfig.getQueryOptionsConfig().getQueryOptions());
            }
            if (fileConfig.getSocketOptionsConfig() != null) {
                builder.withSocketOptions(fileConfig.getSocketOptionsConfig());
            }
            if (fileConfig.getPoolingOptionsConfig() != null) {
                builder.withPoolingOptions(fileConfig.getPoolingOptionsConfig().getPoolingOptions());
            }
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

    public static QueryOptions populateQueryOptions(CassandraDataSourceConfiguration config) {
        return config.getQueryOptionsConfig().getQueryOptions();
    }

    public static PoolingOptions populatePoolingOptions(CassandraDataSourceConfiguration config) {
        return config.getPoolingOptionsConfig().getPoolingOptions();
    }

    public static SocketOptions populateSocketOptions(CassandraDataSourceConfiguration config) {
        return config.getSocketOptionsConfig();
    }

}
