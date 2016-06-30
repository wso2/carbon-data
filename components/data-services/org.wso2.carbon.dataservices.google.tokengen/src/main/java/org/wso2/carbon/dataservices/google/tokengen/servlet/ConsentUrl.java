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

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.hp.hpl.jena.sparql.lib.org.json.JSONObject;
import com.hp.hpl.jena.sparql.lib.org.json.JSONTokener;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.common.DBConstants;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Servlet class to create and send google consent page url.
 */
public class ConsentUrl extends HttpServlet {
    private static final Log log = LogFactory.getLog(ConsentUrl.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        if (log.isDebugEnabled()) {
            log.debug("Request Received for consent URL");
        }
        StringBuffer jb = new StringBuffer();
        JSONObject jsonObject;
        String line = null;
        String responseString;
        int responseStatus;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                jb.append(line);
            }
            jsonObject = new JSONObject(new JSONTokener(jb.toString()));
            String clientId = jsonObject.getString(DBConstants.GSpread.CLIENT_ID);

            String redirectURIs = jsonObject.getString(DBConstants.GSpread.REDIRECT_URIS);

            if (clientId == null || clientId.isEmpty()) {
                responseStatus = HttpStatus.SC_BAD_REQUEST;
                responseString = "ClientID is null or empty";
            } else if (redirectURIs == null || redirectURIs.isEmpty()) {
                responseStatus = HttpStatus.SC_BAD_REQUEST;
                responseString = "Redirect URIs is null or empty";
            } else {
                String[] SCOPESArray = {"https://spreadsheets.google.com/feeds"};
                final List SCOPES = Arrays.asList(SCOPESArray);
                /*
                    Security Comment :
                    This response is trustworthy, url is hard coded in GoogleAuthorizationCodeRequestUrl constructor.
                 */
                responseString = new GoogleAuthorizationCodeRequestUrl(clientId, redirectURIs, SCOPES).setAccessType("offline").setApprovalPrompt("force").build();

                response.setContentType("text/html");
                responseStatus = HttpStatus.SC_OK;
            }
        } catch (Exception e) {
            responseStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            responseString = "Error in Processing accessTokenRequest Error - " + e.getMessage();
            log.error(responseString, e);
        }
        try {
            PrintWriter out = response.getWriter();
            out.println(responseString);
            response.setStatus(responseStatus);
        } catch (IOException e) {
            log.error("Error Getting print writer to write http response Error - " + e.getMessage(), e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
