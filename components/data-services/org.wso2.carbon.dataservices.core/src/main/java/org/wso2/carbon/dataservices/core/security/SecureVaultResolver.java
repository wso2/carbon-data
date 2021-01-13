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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.core.internal.DataServicesDSComponent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.securevault.DecryptionProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Check the secret alias match with the vaultLookupPattern regex.
 */
public class SecureVaultResolver {
    private static Log log = LogFactory.getLog(SecureVaultResolver.class);

    private SecureVaultResolver() {
    }

    /**
     * Regex for secure vault expression.
     */
    private static final String SECURE_VAULT_REGEX = "\\{(wso2:vault-lookup\\('(.*?)'\\))\\}";
    private static final String SECURE_VAULT_LOOKUP_PLACEHOLDER_PREFIX = "{wso2:vault-lookup('";
    private static final String SECURE_VAULT_LOOKUP_PLACEHOLDER_SUFFIX = "')}";

    private static Pattern vaultLookupPattern = Pattern.compile(SECURE_VAULT_REGEX);

    /**
     * Check the secret alias match with the vaultLookupPattern regex.
     *
     * @param text Text of the parameter
     * @return boolean state of the pattern existence
     */
    public static boolean checkVaultLookupPattersExists(String text) {
        Matcher lookupMatcher = vaultLookupPattern.matcher(text);
        return lookupMatcher.find();
    }

    /**
     * Resolve the secret from registry.
     *
     * @param value secure-vault expression in the format of {wso2:vault-lookup('?')}
     * @return resolved secret
     */
    public static String resolve(String value) throws RegistryException {
        // Get the actual alias from the vault-lookup expression
        String secretAlias = StringUtils.substringBetween(value, SECURE_VAULT_LOOKUP_PLACEHOLDER_PREFIX,
                                                          SECURE_VAULT_LOOKUP_PLACEHOLDER_SUFFIX);
        return getSecret(secretAlias);
    }

    /**
     * Get secret the actual password from the alias using the cipher decryption provider.
     *
     * @param alias alias of the actual password
     * @return the actual password from the Secure Vault Password Management if exists, otherwise alias itself
     */
    private static String getSecret(String alias) throws RegistryException {
        // Get config system registry from the registry service
        RegistryService registryService = DataServicesDSComponent.getRegistryService();
        Registry registry = registryService.getConfigSystemRegistry();

        String encryptedValue = null;
        if (registry != null && registry.resourceExists(SecureVaultConstants.ENCRYPTED_PROPERTY_STORAGE_PATH)) {
            Resource registryResource = registry.get(SecureVaultConstants.ENCRYPTED_PROPERTY_STORAGE_PATH);
            encryptedValue = registryResource.getProperty(alias);
        }
        if (encryptedValue == null) {
            log.error("Calling for a non existence alias: " + alias);
            return alias;
        }

        DecryptionProvider decryptionProvider = CipherInitializer.getInstance().getDecryptionProvider();

        if (decryptionProvider == null) {
            log.error("Can not proceed decryption due to the secret repository initialization error");
            return alias;
        }
        return new String(decryptionProvider.decrypt(encryptedValue.trim().getBytes()));
    }
}
