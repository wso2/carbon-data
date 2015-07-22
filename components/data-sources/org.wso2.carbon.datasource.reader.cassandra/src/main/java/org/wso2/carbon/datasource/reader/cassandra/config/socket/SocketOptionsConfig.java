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

import com.datastax.driver.core.SocketOptions;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "socketOptions")
public class SocketOptionsConfig extends SocketOptions {

    @XmlElement(name = "connectTimeoutMillis")
    public int getConnectTimeoutMillis() {
        return super.getConnectTimeoutMillis();
    }

    @XmlElement(name = "keepAlive")
    public Boolean getKeepAlive() {
        return super.getKeepAlive();
    }

    @XmlElement(name = "readTimeoutMillis")
    public int getReadTimeoutMillis(){
        return super.getReadTimeoutMillis();
    }

    @XmlElement(name = "receiveBufferSize")
    public Integer getReceiveBufferSize() {
        return super.getReceiveBufferSize();
    }

    @XmlElement(name = "reuseAddress")
    public Boolean getReuseAddress() {
        return super.getReuseAddress();
    }

    @XmlElement(name = "sendBufferSize")
    public Integer getSendBufferSize() {
        return super.getSendBufferSize();
    }

    @XmlElement(name = "soLinger")
    public Integer getSoLinger() {
        return super.getSoLinger();
    }

    @XmlElement(name = "tcpNoDelay")
    public Boolean getTcpNoDelay() {
        return super.getTcpNoDelay();
    }

}
