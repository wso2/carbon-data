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
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;


public class HadoopDataSourceReaderUtil {

    public static Configuration loadConfig(String xmlConfiguration) throws DataSourceException {
        ByteArrayInputStream baos = null;
        try {
            xmlConfiguration = CarbonUtils.replaceSystemVariablesInXml(xmlConfiguration);
            JAXBContext ctx = JAXBContext.newInstance(HadoopDataSourceConfiguration.class);
            baos = new ByteArrayInputStream(xmlConfiguration.getBytes());
            HadoopDataSourceConfiguration fileConfig = (HadoopDataSourceConfiguration) ctx.createUnmarshaller().unmarshal(baos);
            Configuration config = new Configuration();
            HadoopConfigProperty[] properties = fileConfig.getConfigProperties();

            for (HadoopConfigProperty configEntry : properties) {
                if (!("".equals(configEntry.getPropertyName())) && !("".equals(configEntry.getPropertyValue()))) {
                    config.set(configEntry.getPropertyName(), configEntry.getPropertyValue());
                }
            }
            return config;
        } catch (Exception e) {
            throw new DataSourceException("Error loading Hadoop configuration: " + e.getMessage(), e);
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

    public static FileSystem getHadoopFileSystem(String xmlConfig) throws DataSourceException {
        Configuration configuration = HadoopDataSourceReaderUtil.loadConfig(xmlConfig);
        FileSystem fileSystem;
        try {
            fileSystem = FileSystem.get(configuration);
        } catch (IOException e) {
            throw new DataSourceException("Cannot initialize Hadoop FileSystem from configuration:" + e.getMessage(), e);
        }
        return fileSystem;
    }


    public static Connection getHBaseConnection(String xmlConfig) throws DataSourceException {
        Configuration configuration = HadoopDataSourceReaderUtil.loadConfig(xmlConfig);
        Connection connection;
        try {
            connection = ConnectionFactory.createConnection(configuration);

        } catch (IOException e) {
            throw new DataSourceException("Cannot create Hadoop Connection from configuration:" + e.getMessage(), e);
        }
        return connection;
    }

}
