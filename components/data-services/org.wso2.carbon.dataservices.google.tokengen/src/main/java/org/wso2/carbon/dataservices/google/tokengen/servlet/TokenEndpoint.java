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

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.hp.hpl.jena.sparql.lib.org.json.JSONException;
import com.hp.hpl.jena.sparql.lib.org.json.JSONObject;
import com.hp.hpl.jena.sparql.lib.org.json.JSONTokener;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.google.tokengen.servlet.util.AuthCode;
import org.wso2.carbon.dataservices.google.tokengen.servlet.util.CodeHolder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet class to ping and retrieve access token and refresh token once auth code is available
 */
public class TokenEndpoint extends HttpServlet {
    private static final Log log = LogFactory.getLog(TokenEndpoint.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        AuthCode authCode = CodeHolder.getInstance().getAuthCodeForSession(request.getSession().getId());
        String responseMsg = "";
        JSONObject resJson = new JSONObject();
        int responseStatus;
        if (authCode != null) {
            if (log.isDebugEnabled()) {
                log.debug("Request received for retrieve access token from session - " + request.getSession().getId());
            }
            StringBuffer jb = new StringBuffer();
            JSONObject jsonObject;
            String line = null;
            try {
                BufferedReader reader = request.getReader();
                while ((line = reader.readLine()) != null) {
                    jb.append(line);
                }

                jsonObject = new JSONObject(new JSONTokener(jb.toString()));
                String clientId = jsonObject.getString(DBConstants.GSpread.CLIENT_ID);
                String clientSecret = jsonObject.getString(DBConstants.GSpread.CLIENT_SECRET);
                String redirectURIs = jsonObject.getString(DBConstants.GSpread.REDIRECT_URIS);

                if (clientId == null || clientId.isEmpty()) {
                    responseStatus = HttpStatus.SC_BAD_REQUEST;
                    responseMsg = "ClientID is null or empty";
                } else if (clientSecret == null || clientSecret.isEmpty()) {
                    responseStatus = HttpStatus.SC_BAD_REQUEST;
                    responseMsg = "Client Secret is null or empty";
                } else if (redirectURIs == null || redirectURIs.isEmpty()) {
                    responseStatus = HttpStatus.SC_BAD_REQUEST;
                    responseMsg = "Redirect URIs is null or empty";
                } else {
                    HttpTransport httpTransport = new NetHttpTransport();
                    JacksonFactory jsonFactory = new JacksonFactory();

                    // Step 2: Exchange auth code for tokens
                    GoogleTokenResponse googleTokenResponse
                            = new GoogleAuthorizationCodeTokenRequest(httpTransport, jsonFactory, "https://www.googleapis.com/oauth2/v3/token", clientId, clientSecret,
                                                                      authCode.getAuthCode(), redirectURIs).execute();
                    resJson.append(DBConstants.GSpread.ACCESS_TOKEN, googleTokenResponse.getAccessToken());
                    resJson.append(DBConstants.GSpread.REFRESH_TOKEN, googleTokenResponse.getRefreshToken());
                    responseMsg = resJson.toString();
                    responseStatus = HttpStatus.SC_OK;
                }
            } catch (JSONException e) {
                responseStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                responseMsg = "Error in Processing accessTokenRequest Error - " + e.getMessage();
                log.error(responseMsg, e);
            } catch (IOException e) {
                responseStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                responseMsg = "Error in Processing accessTokenRequest Error - " + e.getMessage();
                log.error(responseMsg, e);
            } catch (Exception e) {
                responseStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                responseMsg = "Error in Processing accessTokenRequest Error - " + e.getMessage();
                log.error(responseMsg, e);
            }
        } else {
            responseStatus = HttpStatus.SC_ACCEPTED;
            responseMsg = resJson.toString();
        }
        try {
            PrintWriter out = response.getWriter();
            out.println(responseMsg);
            response.setStatus(responseStatus);
        } catch (IOException e) {
            log.error("Error Getting print writer to write http response Error - " + e.getMessage(), e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
