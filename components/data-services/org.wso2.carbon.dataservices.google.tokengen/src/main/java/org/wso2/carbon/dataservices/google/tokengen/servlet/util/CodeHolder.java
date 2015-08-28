/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.dataservices.google.tokengen.servlet.util;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the singleton class which holds auth codes received from different clients.
 */
public class CodeHolder {

    private static CodeHolder tokenGen;

    private Map<String,AuthCode> authCodes;

    /**
     * Private constructor to make the class singleton.
     */
    private CodeHolder(){
        authCodes = new HashMap<String,AuthCode>(2);
    }

    /**
     * Method to return the instance.
     *
     * @return singleton instance of the class
     */
    public static synchronized CodeHolder getInstance(){
        if (tokenGen == null){
            tokenGen = new CodeHolder();
        }
        return tokenGen;
    }

    /**
     * Method to add authCode to the map when user accept the consent.
     *
     * @param sessionId
     * @param code
     * @param error
     */
    public synchronized void addCodeToMap(String sessionId, String code, String error) {
        AuthCode authCode = new AuthCode();
        authCode.setAuthCode(code);
        authCode.setErrorCode(error);
        authCodes.put(sessionId,authCode);
    }

    /**
     * This method will return the auth code object associated with the session.
     *
     * @param sessionId
     * @return relevant authCode
     */
    public AuthCode getAuthCodeForSession(String sessionId) {
        return authCodes.remove(sessionId);
    }
}
