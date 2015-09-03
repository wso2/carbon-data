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
package org.wso2.carbon.dataservices.google.tokengen.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.google.tokengen.servlet.util.CodeHolder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AuthCode receiver servlet Which will get hit when auth code received.
 */
public class AuthCodeReceiver extends HttpServlet {
    private static final Log log = LogFactory.getLog(AuthCodeReceiver.class);

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) {
        if (log.isDebugEnabled()) {
            log.debug("Auth code received for the Google Authentication code request");
        }
        CodeHolder tokenGen = CodeHolder.getInstance();
        tokenGen.addCodeToMap(req.getSession().getId(), req.getParameter("code"), req.getParameter("error"));
    }
}
