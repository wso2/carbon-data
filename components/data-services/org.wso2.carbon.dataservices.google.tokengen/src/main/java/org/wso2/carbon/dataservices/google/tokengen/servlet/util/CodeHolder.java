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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.google.tokengen.servlet.internal.GoogleTokenGenDSComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This is the singleton class which holds auth codes received from different clients.
 */
public class CodeHolder implements Runnable {
    private static final Log log = LogFactory.getLog(CodeHolder.class);
    private static CodeHolder tokenGen;
    private ScheduledExecutorService globalExecutorService;
    private long expirationTime;

    private Map<String,AuthCode> authCodes;

    /**
     * Private constructor to make the class singleton and start the cleanup process.
     */
    private CodeHolder(){
        if (GoogleTokenGenDSComponent.getHazelcastInstance() != null) {
            authCodes = GoogleTokenGenDSComponent.getHazelcastInstance().getMap("GOOGLE_TOKENGEN_AUTHCODE_HOLDER");
        } else {
            authCodes = new HashMap<String,AuthCode>(2);
        }
        //retry interval 1 hour
        long interval = 1;
        //expiration time in milliseconds - default set to 30 mins.
        expirationTime = 1000 * 60 * 30;
        globalExecutorService = Executors.newSingleThreadScheduledExecutor();
        globalExecutorService.scheduleAtFixedRate(this, interval, interval, TimeUnit.HOURS);
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
        authCode.setInsertedTime(System.currentTimeMillis());
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

    /**
     * Helper method to cleanup Oauth code map from time to time.
     */
    private void cleanupMap() {
        long currentTime = System.currentTimeMillis();
        for (String key : new ArrayList<String>(authCodes.keySet())) {
            AuthCode code = authCodes.get(key);
            if (code != null) {
                if ((currentTime - code.getInsertedTime()) > expirationTime) {
                    authCodes.remove(key);
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            this.cleanupMap();
        } catch (Exception e) {
            log.warn("Error occurred while cleaning up OAuth code map, Error - " + e.getMessage(), e);
        }
    }
}
