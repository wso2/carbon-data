/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.datasource.reader.mongo.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration class for MongoDB.
 *
 */
@XmlRootElement(name = "configuration")
public class MongoDataSourceConfiguration {

    public enum AuthenticationMethod {
        DEFAULT, SCRAM_SHA_1, MONGODB_CR, X_509, GSSAPI, LDAP_PLAIN
    }

    private String url;

    private String host;

    private String port;

    private ReplicaSetOptionsConfig replicaSetConfig;

    private Boolean withSSL;

    private String username;

    private String password;

    private String database;

    private String authenticationMethod;

    private String authSource;

    private Boolean sslInvalidHostNameAllowed;

    @XmlElement(name = "sslInvalidHostNameAllowed")
    public Boolean getSslInvalidHostNameAllowed() {
        return sslInvalidHostNameAllowed;
    }

    public void setSslInvalidHostNameAllowed(Boolean sslInvalidHostNameAllowed) {
        this.sslInvalidHostNameAllowed = sslInvalidHostNameAllowed;
    }

    @XmlElement(name = "host")
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @XmlElement(name = "port")
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @XmlElement(name = "replicaSetOptions")
    public ReplicaSetOptionsConfig getReplicaSetConfig() {
        return replicaSetConfig;
    }

    public void setReplicaSetConfig(ReplicaSetOptionsConfig replicaSetConfig) {
        this.replicaSetConfig = replicaSetConfig;
    }

    @XmlElement(name = "database")
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @XmlElement(name = "url", nillable = false, required = true)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElement(name = "withSSL")
    public Boolean getWithSSL() {
        return withSSL;
    }

    public void setWithSSL(Boolean withSSL) {
        this.withSSL = withSSL;
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

    @XmlElement(name = "authenticationMethod")
    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    @XmlElement(name = "authSource")
    public String getAuthSource() {
        return authSource;
    }

    public void setAuthSource(String authSource) {
        this.authSource = authSource;
    }

    public AuthenticationMethod getAuthenticationMethodEnum() {
        if (authenticationMethod != null) {
            return AuthenticationMethod.valueOf(authenticationMethod);
        } else {
            return null;
        }
    }

}
