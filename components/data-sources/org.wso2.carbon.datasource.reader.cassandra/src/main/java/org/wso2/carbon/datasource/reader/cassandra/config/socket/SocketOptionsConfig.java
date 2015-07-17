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
package org.wso2.carbon.datasource.reader.cassandra.config.socket;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "socketOptions")
public class SocketOptionsConfig {

    //Not using primitives to force unassigned values to be null
    private Integer connectTimeoutMillis;
    private Boolean keepAlive;
    private Integer receiveBufferSize;
    private Boolean reuseAddress;
    private Integer sendBufferSize;
    private Integer soLinger;
    private Boolean tcpNoDelay;

    @XmlElement(name = "connectTimeoutMillis")
    public Integer getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(Integer connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    @XmlElement(name = "keepAlive")
    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    @XmlElement(name = "receiveBufferSize")
    public Integer getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(Integer receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    @XmlElement(name = "reuseAddress")
    public Boolean getReuseAddress() {
        return reuseAddress;
    }

    public void setReuseAddress(Boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    @XmlElement(name = "sendBufferSize")
    public Integer getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(Integer sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    @XmlElement(name = "soLinger")
    public Integer getSoLinger() {
        return soLinger;
    }

    public void setSoLinger(Integer soLinger) {
        this.soLinger = soLinger;
    }

    @XmlElement(name = "tcpNoDelay")
    public Boolean getTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(Boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

}
