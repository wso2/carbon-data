/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.dataservices.core.security;

class SecureVaultConstants {

    private SecureVaultConstants(){}

    static final String CONF_LOCATION = "conf.location";
    static final String SECURITY_DIR = "security";
    static final String SECRET_CONF = "secret-conf.properties";
    static final String TRUSTED = "trusted";
    static final String DOT = ".";
    static final String ALGORITHM = "algorithm";
    static final String DEFAULT_ALGORITHM = "RSA";
    static final String CIPHER_TRANSFORMATION_SECRET_CONF_PROPERTY = "keystore.identity.CipherTransformation";
    static final String CIPHER_TRANSFORMATION_SYSTEM_PROPERTY = "org.wso2.CipherTransformation";
    static final String SECRET_REPOSITORIES_PROPERTY = "secretRepositories";
    static final String PROVIDER_PROPERTY = "provider";
    static final String SECRET_MANAGER_CONF_PROPERTY = "secret.manager.conf";
    static final String SECRET_PROVIDER_PROPERTY = "carbon.secretProvider";
    static final String DEFAULT_CONF_LOCATION_PROPERTY = "secret-manager.properties";
    static final String ENCRYPTED_PROPERTY_STORAGE_PATH = "/repository/components/secure-vault";
}
