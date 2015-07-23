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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "configuration")
public class CassandraDataSourceConfiguration {

    private String contactPoints;

    /* Protocol options */
    private Integer port;
    private Integer maxSchemaAgreementWaitSeconds;
    private String compression;
    private String clusterName;
    private String username;
    private String password;
    private String protocolVersion;

    /* Query options */
    private QueryOptionsConfig queryOptionsConfig;

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

    @XmlElement(name = "maxSchemaAgreementWaitSeconds")
    public Integer getMaxSchemaAgreementWaitSeconds() {
        return maxSchemaAgreementWaitSeconds;
    }

    public void setMaxSchemaAgreementWaitSeconds(Integer maxSchemaAgreementWaitSeconds) {
        this.maxSchemaAgreementWaitSeconds = maxSchemaAgreementWaitSeconds;
    }

    @XmlElement(name = "clusterName")
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @XmlElement(name = "username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @XmlElement(name = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement(name = "protocolVersion")
    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @XmlElement(name = "queryOptions")
    public QueryOptionsConfig getQueryOptionsConfig() {
        return queryOptionsConfig;
    }

    public void setQueryOptionsConfig(QueryOptionsConfig queryOptionsConfig) {
        this.queryOptionsConfig = queryOptionsConfig;
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
