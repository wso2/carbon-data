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

    @XmlElement(name = "minSimultaneousRequestsPerConnectionThreshold")
    public List<MinConnectionThresholdConfig> getMinThresholdz() {
        List<MinConnectionThresholdConfig> list = new ArrayList<>();
        for (HostDistance dist : HostDistance.values()) {
            if (dist != HostDistance.IGNORED) {
                MinConnectionThresholdConfig cfg = new MinConnectionThresholdConfig();
                cfg.setHostDistance(dist);
                cfg.setValue(this.poolingOptions.getMinSimultaneousRequestsPerConnectionThreshold(dist));
                list.add(cfg);
            }
        }
        return list;
    }

    public void setMinThresholdz(List<MinConnectionThresholdConfig> minThresholdz) {
        for (MinConnectionThresholdConfig cfg : minThresholdz) {
            this.poolingOptions.setMinSimultaneousRequestsPerConnectionThreshold(cfg.getHostDistance(), cfg.getValue());
        }
    }

    @XmlElement(name = "maxSimultaneousRequestsPerConnectionThreshold")
    public List<MaxConnectionThresholdConfig> getMaxThresholdz() {
        List<MaxConnectionThresholdConfig> list = new ArrayList<>();
        for (HostDistance dist : HostDistance.values()) {
            if (dist != HostDistance.IGNORED) {
                MaxConnectionThresholdConfig cfg = new MaxConnectionThresholdConfig();
                cfg.setHostDistance(dist);
                cfg.setValue(this.poolingOptions.getMaxSimultaneousRequestsPerConnectionThreshold(dist));
                list.add(cfg);
            }
        }
        return list;
    }

    public void setMaxThresholdz(List<MaxConnectionThresholdConfig> maxThresholdz) {
        for (MaxConnectionThresholdConfig cfg : maxThresholdz) {
            this.poolingOptions.setMaxSimultaneousRequestsPerConnectionThreshold(cfg.getHostDistance(), cfg.getValue());
        }
    }

    @XmlElement(name = "maxSimultaneousRequestsPerHostThreshold")
    public List<MaxHostThresholdConfig> getMaxHostThresholdz() {
        List<MaxHostThresholdConfig> list = new ArrayList<>();
        for (HostDistance dist : HostDistance.values()) {
            if (dist != HostDistance.IGNORED) {
                MaxHostThresholdConfig cfg = new MaxHostThresholdConfig();
                cfg.setHostDistance(dist);
                cfg.setValue(this.poolingOptions.getMaxSimultaneousRequestsPerHostThreshold(dist));
                list.add(cfg);
            }
        }
        return list;
    }

    public void setMaxHostThresholdz(List<MaxHostThresholdConfig> maxHostThresholdz) {
        for (MaxHostThresholdConfig cfg : maxHostThresholdz) {
            this.poolingOptions.setMaxSimultaneousRequestsPerHostThreshold(cfg.getHostDistance(), cfg.getValue());
        }
    }

    public static class CoreConnectionsPerHostConfig extends PoolingOptionsConfigProperty {
    }

    public static class MaxConnectionsPerHostConfig extends PoolingOptionsConfigProperty {
    }

    public static class MinConnectionThresholdConfig extends PoolingOptionsConfigProperty {
    }

    public static class MaxConnectionThresholdConfig extends PoolingOptionsConfigProperty {
    }

    public static class MaxHostThresholdConfig extends PoolingOptionsConfigProperty {
    }

}
