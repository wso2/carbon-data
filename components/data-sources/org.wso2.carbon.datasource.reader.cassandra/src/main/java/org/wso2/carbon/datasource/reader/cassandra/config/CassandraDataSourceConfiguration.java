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
package org.wso2.carbon.datasource.reader.cassandra.config;

import org.wso2.carbon.datasource.reader.cassandra.config.pooling.PoolingOptionsConfig;
import org.wso2.carbon.datasource.reader.cassandra.config.socket.SocketOptionsConfig;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "configuration")
public class CassandraDataSourceConfiguration {

    private String contactPoints;

    /* Protocol options */
    private Integer port;
    private String compression;

    /* Pooling options */
    private PoolingOptionsConfig poolingOptionsConfig;

    /* Socket Options*/
    private SocketOptionsConfig socketOptionsConfig;

    @XmlElement(name = "contactPoints", required = true, nillable = false)
    public String getContactPoints() {
        return contactPoints;
    }

    public void setContactPoints(String contactPoints) {
        this.contactPoints = contactPoints;
    }

    @XmlElement(name = "port")
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @XmlElement(name = "compression")
    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    @XmlElement(name = "socketOptions")
    public SocketOptionsConfig getSocketOptionsConfig() {
        return socketOptionsConfig;
    }

    public void setSocketOptionsConfig(SocketOptionsConfig socketOptionsConfig) {
        this.socketOptionsConfig = socketOptionsConfig;
    }

    @XmlElement(name = "poolingOptions")
    public PoolingOptionsConfig getPoolingOptionsConfig() {
        return poolingOptionsConfig;
    }

    public void setPoolingOptionsConfig(PoolingOptionsConfig poolingOptionsConfig) {
        this.poolingOptionsConfig = poolingOptionsConfig;
    }
}
