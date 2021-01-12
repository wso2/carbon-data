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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.securevault.CipherFactory;
import org.wso2.securevault.CipherOperationMode;
import org.wso2.securevault.DecryptionProvider;
import org.wso2.securevault.EncodingType;
import org.wso2.securevault.commons.MiscellaneousUtil;
import org.wso2.securevault.definition.CipherInformation;
import org.wso2.securevault.definition.IdentityKeyStoreInformation;
import org.wso2.securevault.definition.KeyStoreInformationFactory;
import org.wso2.securevault.definition.TrustKeyStoreInformation;
import org.wso2.securevault.keystore.IdentityKeyStoreWrapper;
import org.wso2.securevault.keystore.KeyStoreWrapper;
import org.wso2.securevault.keystore.TrustKeyStoreWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

public class CipherInitializer {
    private static Log log = LogFactory.getLog(CipherInitializer.class);

    private static CipherInitializer cipherInitializer = new CipherInitializer();
    private DecryptionProvider decryptionProvider = null;
    private IdentityKeyStoreWrapper identityKeyStoreWrapper;
    private TrustKeyStoreWrapper trustKeyStoreWrapper;

    public static CipherInitializer getInstance() {
        return cipherInitializer;
    }

    private CipherInitializer() {
        super();
        boolean initProperties = init();
        if (initProperties) {
            // initialize the cipher decryption provider for decryption purposes.
            initCipherDecryptProvider();
        } else {
            log.error("Either Configuration properties can not be loaded or No secret"
                              + " repositories have been configured please check PRODUCT_HOME/repository/conf/security "
                              + " refer links related to configure WSO2 Secure vault");
        }
    }

    private boolean init() {
        Properties properties = loadProperties();

        if (properties.isEmpty()) {
            log.error("KeyStore configuration properties cannot be found");
            return false;
        }

        String configurationFile = MiscellaneousUtil.getProperty(properties,
                                                                 SecureVaultConstants.SECRET_MANAGER_CONF_PROPERTY,
                                                                 SecureVaultConstants.DEFAULT_CONF_LOCATION_PROPERTY);

        Properties configurationProperties = MiscellaneousUtil.loadProperties(configurationFile);
        if (configurationProperties.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Configuration properties can not be loaded from : " + configurationFile +
                                  " Will use synapse properties");
            }
            configurationProperties = properties;
        }

        String repositoriesString = MiscellaneousUtil.getProperty(configurationProperties,
                                                                  SecureVaultConstants.SECRET_REPOSITORIES_PROPERTY,
                                                                  null);
        if (repositoriesString == null || "".equals(repositoriesString)) {
            log.error("No secret repositories have been configured");
            return false;
        }

        String[] repositories = repositoriesString.split(",");
        if (repositories.length == 0) {
            log.error("No secret repositories have been configured");
            return false;
        }

        // Create a KeyStore Information for private key entry KeyStore
        IdentityKeyStoreInformation identityInformation =
                KeyStoreInformationFactory.createIdentityKeyStoreInformation(properties);

        // Create a KeyStore Information for trusted certificate KeyStore
        TrustKeyStoreInformation trustInformation =
                KeyStoreInformationFactory.createTrustKeyStoreInformation(properties);

        String identityKeyPass = null;
        String identityStorePass = null;
        String trustStorePass = null;
        if (identityInformation != null) {
            identityKeyPass = identityInformation.getKeyPasswordProvider().getResolvedSecret();
            identityStorePass = identityInformation.getKeyStorePasswordProvider().getResolvedSecret();
        }

        if (trustInformation != null) {
            trustStorePass = trustInformation.getKeyStorePasswordProvider().getResolvedSecret();
        }

        if (!validatePasswords(identityStorePass, identityKeyPass, trustStorePass)) {
            log.error("Either Identity or Trust keystore password is mandatory"
                              + " in order to initialized secret manager.");
            return false;
        }

        identityKeyStoreWrapper = new IdentityKeyStoreWrapper();
        identityKeyStoreWrapper.init(identityInformation, identityKeyPass);

        trustKeyStoreWrapper = new TrustKeyStoreWrapper();
        if (trustInformation != null) {
            trustKeyStoreWrapper.init(trustInformation);
        }

        for (String secretRepo : repositories) {

            String sb = SecureVaultConstants.SECRET_REPOSITORIES_PROPERTY + SecureVaultConstants.DOT + secretRepo
                    + SecureVaultConstants.DOT + SecureVaultConstants.PROVIDER_PROPERTY;
            String provider = MiscellaneousUtil.getProperty(configurationProperties, sb, null);
            if (provider == null || "".equals(provider)) {
                handleException("Repository provider cannot be null.");
            }

            if (log.isDebugEnabled()) {
                log.debug("Initiating a File Based Secret Repository");
            }
        }
        return true;
    }

    private boolean validatePasswords(String identityStorePass, String identityKeyPass,
                                      String trustStorePass) {
        boolean isValid = false;
        if (trustStorePass != null && !"".equals(trustStorePass)) {
            if (log.isDebugEnabled()) {
                log.debug("Trust Store Password cannot be found.");
            }
            isValid = true;
        } else {
            if (identityStorePass != null && !"".equals(identityStorePass) &&
                    identityKeyPass != null && !"".equals(identityKeyPass)) {
                if (log.isDebugEnabled()) {
                    log.debug("Identity Store Password "
                                      + "and Identity Store private key Password cannot be found.");
                }
                isValid = true;
            }
        }
        return isValid;
    }

    /**
     * Initialize the Cipher decryption provider.
     */
    private void initCipherDecryptProvider() {
        if (decryptionProvider != null) return;
        Properties properties = loadProperties();

        // Load algorithm
        String algorithm = getCipherTransformation(properties);

        // Load keyStore
        String keyStore = MiscellaneousUtil.getProperty(properties,
                                                        SecureVaultConstants.DOT + SecureVaultConstants.ALGORITHM,
                                                        null);

        KeyStoreWrapper keyStoreWrapper;

        if (SecureVaultConstants.TRUSTED.equals(keyStore)) {
            keyStoreWrapper = trustKeyStoreWrapper;

        } else {
            keyStoreWrapper = identityKeyStoreWrapper;
        }

        CipherInformation cipherInformation = new CipherInformation();
        cipherInformation.setAlgorithm(algorithm);
        cipherInformation.setCipherOperationMode(CipherOperationMode.DECRYPT);
        cipherInformation.setInType(EncodingType.BASE64); // TODO
        decryptionProvider = CipherFactory.createCipher(cipherInformation, keyStoreWrapper);
    }

    /**
     * Get the Cipher Transformation to be used by the Cipher. We have the option of configuring this globally as a
     * System Property '-Dorg.wso2.CipherTransformation', which can be overridden at the 'secret-conf.properties' level
     * by specifying the property 'keystore.identity.CipherTransformation'. If neither are configured the default 'RSA'
     * will be used
     *
     * @param properties Properties from the 'secret-conf.properties' file
     * @return Cipher Transformation String
     */
    private String getCipherTransformation(Properties properties) {
        String cipherTransformation = System.getProperty(SecureVaultConstants.CIPHER_TRANSFORMATION_SYSTEM_PROPERTY);

        if (cipherTransformation == null) {
            cipherTransformation = SecureVaultConstants.DEFAULT_ALGORITHM;
        }

        return MiscellaneousUtil.getProperty(properties,
                                             SecureVaultConstants.CIPHER_TRANSFORMATION_SECRET_CONF_PROPERTY,
                                             cipherTransformation);
    }

    /**
     * Read the secret-conf.properties file located at conf/security directory and load the properties.
     *
     * @return properties in the secret-conf.properties file
     */
    private static Properties loadProperties() {
        Properties properties = new Properties();
        String confPath = System.getProperty(SecureVaultConstants.CONF_LOCATION);
        if (confPath == null) {
            confPath = Paths.get("repository", "conf").toString();
        }
        String filePath = Paths.get(confPath, SecureVaultConstants.SECURITY_DIR, SecureVaultConstants.SECRET_CONF)
                .toString();

        File dataSourceFile = new File(filePath);
        if (!dataSourceFile.exists()) {
            return properties;
        }

        try (InputStream in = new FileInputStream(dataSourceFile)) {
            properties.load(in);
        } catch (IOException e) {
            String msg = "Error loading properties from a file at :" + filePath;
            log.warn(msg, e);
            return properties;
        }
        return properties;
    }

    DecryptionProvider getDecryptionProvider() {
        return decryptionProvider;
    }

    private static void handleException(String msg) {
        //throw new CipherToolException(msg);
        log.error(msg);
    }
}
