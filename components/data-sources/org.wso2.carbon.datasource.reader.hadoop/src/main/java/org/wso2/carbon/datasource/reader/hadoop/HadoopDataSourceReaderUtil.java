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
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import java.io.ByteArrayInputStream;


public class HadoopDataSourceReaderUtil {

    public static Configuration loadConfig(String xmlConfiguration) throws DataSourceException {
        try {
            xmlConfiguration = CarbonUtils.replaceSystemVariablesInXml(xmlConfiguration);
            JAXBContext ctx = JAXBContext.newInstance(HadoopDataSourceConfiguration.class);

            HadoopDataSourceConfiguration fileConfig = (HadoopDataSourceConfiguration) ctx.createUnmarshaller().unmarshal(
                    new ByteArrayInputStream(xmlConfiguration.getBytes()));
            Configuration config = new Configuration();
            HadoopConfigProperty[] properties = fileConfig.getConfingProperties();

            for (HadoopConfigProperty configEntry : properties) {
                if (!("".equals(configEntry.getPropertyName())) && !("".equals(configEntry.getPropertyValue()))) {
                    config.set(configEntry.getPropertyName(), configEntry.getPropertyValue());
                }
            }
            return config;
        } catch (Exception e) {
            throw new DataSourceException("Error loading Hadoop configuration: " + e.getMessage(), e);
        }
    }
}
