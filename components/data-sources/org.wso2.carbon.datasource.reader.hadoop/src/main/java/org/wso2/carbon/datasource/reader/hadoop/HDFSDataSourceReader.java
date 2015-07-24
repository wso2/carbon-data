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
package org.wso2.carbon.datasource.reader.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.common.spi.DataSourceReader;

import java.io.IOException;

/**
 * HDFS implementation of {@link org.wso2.carbon.ndatasource.common.spi.DataSourceReader}
 */
public class HDFSDataSourceReader implements DataSourceReader {

    @Override
    public String getType() {
        return HadoopDataSourceConstants.DATASOURCE_TYPE_HDFS;
    }

    @Override
    public Object createDataSource(String xmlConfig, boolean isDataSourceFactoryReference) throws DataSourceException {
        return HadoopDataSourceReaderUtil.loadConfig(xmlConfig);
    }

    @Override
    public boolean testDataSourceConnection(String xmlConfig) throws DataSourceException {
        Configuration config = (Configuration) createDataSource(xmlConfig, true);
        try (FileSystem fileSystem = FileSystem.get(config)) {
            return (fileSystem == null);
        } catch (IOException e) {
            throw new DataSourceException("Cannot establish connection to HDFS instance for testing: " + e.getMessage(), e);
        }
    }

}
