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
import com.datastax.driver.core.Session;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.common.spi.DataSourceReader;

public class CassandraDataSourceReader implements DataSourceReader {

    public static final String DATASOURCE_TYPE = "CASSANDRA";

    @Override
    public String getType() {
        return DATASOURCE_TYPE;
    }

    @Override
    public Object createDataSource(String xmlConfig, boolean isDataSourceFactoryReference) throws DataSourceException {
        return CassandraDataSourceReaderUtil.loadConfiguration(xmlConfig);
    }

    @Override
    public boolean testDataSourceConnection(String xmlConfig) throws DataSourceException {
        Cluster cluster = (Cluster) this.createDataSource(xmlConfig, true);
        try (Session session = cluster.connect()) {
            boolean status = (session == null);
            cluster.close();
            return status;
        }
    }
}
