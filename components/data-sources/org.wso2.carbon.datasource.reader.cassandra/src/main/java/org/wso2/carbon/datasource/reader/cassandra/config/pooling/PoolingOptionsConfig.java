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
package org.wso2.carbon.datasource.reader.cassandra.config.pooling;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "poolingOptions")
public class PoolingOptionsConfig {

    private Integer heartbeatIntervalSeconds;
    private Integer poolTimeoutMillis;
    private CoreConnectionsPerHostConfig[] coreConnectionsPerHostz;
    private MaxConnectionsPerHostConfig[] maxConnectionsPerHostz;
    private MinConnectionThresholdConfig[] minThresholdz;
    private MaxConnectionThresholdConfig[] maxThresholdz;
    private MaxHostThresholdConfig[] maxHostThresholdz;

    @XmlElement(name = "heartbeatIntervalSeconds")
    public Integer getHeartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds;
    }

    public void setHeartbeatIntervalSeconds(Integer heartbeatIntervalSeconds) {
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
    }

    @XmlElement(name = "poolTimeoutMillis")
    public Integer getPoolTimeoutMillis() {
        return poolTimeoutMillis;
    }

    public void setPoolTimeoutMillis(Integer poolTimeoutMillis) {
        this.poolTimeoutMillis = poolTimeoutMillis;
    }

    @XmlElement(name = "coreConnectionsPerHost")
    public CoreConnectionsPerHostConfig[] getCoreConnectionsPerHostz() {
        return coreConnectionsPerHostz;
    }

    public void setCoreConnectionsPerHostz(CoreConnectionsPerHostConfig[] coreConnectionsPerHost) {
        this.coreConnectionsPerHostz = coreConnectionsPerHost;
    }

    @XmlElement(name = "maxConnectionPerHost")
    public MaxConnectionsPerHostConfig[] getMaxConnectionsPerHostz() {
        return maxConnectionsPerHostz;
    }

    public void setMaxConnectionsPerHostz(MaxConnectionsPerHostConfig[] maxConnectionsPerHostz) {
        this.maxConnectionsPerHostz = maxConnectionsPerHostz;
    }

    @XmlElement(name = "minSimultaneousRequestsPerConnectionThreshold")
    public MinConnectionThresholdConfig[] getMinThresholdz() {
        return minThresholdz;
    }

    public void setMinThresholdz(MinConnectionThresholdConfig[] minThresholdz) {
        this.minThresholdz = minThresholdz;
    }

    @XmlElement(name = "maxSimultaneousRequestsPerConnectionThreshold")
    public MaxConnectionThresholdConfig[] getMaxThresholdz() {
        return maxThresholdz;
    }

    public void setMaxThresholdz(MaxConnectionThresholdConfig[] maxThresholdz) {
        this.maxThresholdz = maxThresholdz;
    }

    @XmlElement(name = "minSimultaneousRequestsPerHostThreshold")
    public MaxHostThresholdConfig[] getMaxHostThresholdz() {
        return maxHostThresholdz;
    }

    public void setMaxHostThresholdz(MaxHostThresholdConfig[] maxHostThresholdz) {
        this.maxHostThresholdz = maxHostThresholdz;
    }

}
