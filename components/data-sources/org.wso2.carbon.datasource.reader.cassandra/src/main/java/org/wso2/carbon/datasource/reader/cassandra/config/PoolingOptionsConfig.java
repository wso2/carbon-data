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


import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "poolingOptions")
public class PoolingOptionsConfig {

    private PoolingOptions poolingOptions;

    public PoolingOptionsConfig() {
        this.poolingOptions = new PoolingOptions();
    }

    public PoolingOptions getPoolingOptions() {
        return this.poolingOptions;
    }

    @XmlElement(name = "heartbeatIntervalSeconds")
    public int getHeartbeatIntervalSeconds() {
        return this.poolingOptions.getHeartbeatIntervalSeconds();
    }

    public void setHeartbeatIntervalSeconds(int heartbeatIntervalSeconds) {
        this.poolingOptions.setHeartbeatIntervalSeconds(heartbeatIntervalSeconds);
    }

    @XmlElement(name = "poolTimeoutMillis")
    public int getPoolTimeoutMillis() {
        return this.poolingOptions.getPoolTimeoutMillis();
    }

    public void setPoolTimeoutMillis(int poolTimeoutMillis) {
        this.poolingOptions.setPoolTimeoutMillis(poolTimeoutMillis);
    }

    @XmlElement(name = "idleTimeoutSeconds")
    public int getIdleTimeoutSeconds() {
        return this.poolingOptions.getIdleTimeoutSeconds();
    }

    public void setIdleTimeoutSeconds(int idleTimeoutSeconds) {
        this.poolingOptions.setIdleTimeoutSeconds(idleTimeoutSeconds);
    }

    @XmlElement(name = "coreConnectionsPerHost")
    public List<CoreConnectionsPerHostConfig> getCoreConnectionsPerHostz() {
        List<CoreConnectionsPerHostConfig> list = new ArrayList<>();
        for (HostDistance dist : HostDistance.values()) {
            if (dist != HostDistance.IGNORED) {
                CoreConnectionsPerHostConfig cfg = new CoreConnectionsPerHostConfig();
                cfg.setHostDistance(dist);
                cfg.setValue(this.poolingOptions.getCoreConnectionsPerHost(dist));
                list.add(cfg);
            }
        }
        return list;
    }

    public void setCoreConnectionsPerHostz(List<CoreConnectionsPerHostConfig> coreConnectionsPerHost) {
        for (CoreConnectionsPerHostConfig cfg : coreConnectionsPerHost) {
            this.poolingOptions.setCoreConnectionsPerHost(cfg.getHostDistance(), cfg.getValue());
        }
    }

    @XmlElement(name = "maxConnectionPerHost")
    public List<MaxConnectionsPerHostConfig> getMaxConnectionsPerHostz() {
        List<MaxConnectionsPerHostConfig> list = new ArrayList<>();
        for (HostDistance dist : HostDistance.values()) {
            if (dist != HostDistance.IGNORED) {
                MaxConnectionsPerHostConfig cfg = new MaxConnectionsPerHostConfig();
                cfg.setHostDistance(dist);
                cfg.setValue(this.poolingOptions.getMaxConnectionsPerHost(dist));
                list.add(cfg);
            }
        }
        return list;
    }

    public void setMaxConnectionsPerHostz(List<MaxConnectionsPerHostConfig> maxConnectionsPerHostz) {
        for (MaxConnectionsPerHostConfig cfg : maxConnectionsPerHostz) {
            this.poolingOptions.setMaxConnectionsPerHost(cfg.getHostDistance(), cfg.getValue());
        }
    }

    @XmlElement(name = "newConnectionThreshold")
    public List<NewConnectionThresholdConfig> getNewConnectionThresholdz() {
        List<NewConnectionThresholdConfig> list = new ArrayList<>();
        for (HostDistance dist : HostDistance.values()) {
            if (dist != HostDistance.IGNORED) {
                NewConnectionThresholdConfig cfg = new NewConnectionThresholdConfig();
                cfg.setHostDistance(dist);
                cfg.setValue(this.poolingOptions.getNewConnectionThreshold(dist));
                list.add(cfg);
            }
        }
        return list;
    }

    public void setNewConnectionThresholdz(List<NewConnectionThresholdConfig> maxThresholdz) {
        for (NewConnectionThresholdConfig cfg : maxThresholdz) {
            this.poolingOptions.setNewConnectionThreshold(cfg.getHostDistance(), cfg.getValue());
        }
    }

    @XmlElement(name = "maxRequestsPerConnection")
    public List<MaxRequestsPerConnectionConfig> getMaxRequestsPerConnectionz() {
        List<MaxRequestsPerConnectionConfig> list = new ArrayList<>();
        for (HostDistance dist : HostDistance.values()) {
            if (dist != HostDistance.IGNORED) {
                MaxRequestsPerConnectionConfig cfg = new MaxRequestsPerConnectionConfig();
                cfg.setHostDistance(dist);
                cfg.setValue(this.poolingOptions.getMaxRequestsPerConnection(dist));
                list.add(cfg);
            }
        }
        return list;
    }

    public void setMaxRequestsPerConnection(List<MaxRequestsPerConnectionConfig> maxRequestsPerConnectionz) {
        for (MaxRequestsPerConnectionConfig cfg : maxRequestsPerConnectionz) {
            this.poolingOptions.setMaxRequestsPerConnection(cfg.getHostDistance(), cfg.getValue());
        }
    }

    public static class CoreConnectionsPerHostConfig extends PoolingOptionsConfigProperty {
    }

    public static class MaxConnectionsPerHostConfig extends PoolingOptionsConfigProperty {
    }

    public static class NewConnectionThresholdConfig extends PoolingOptionsConfigProperty {
    }

    public static class MaxRequestsPerConnectionConfig extends PoolingOptionsConfigProperty {
    }

}
