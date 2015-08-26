/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dataservices.core.description.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.IFeed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.DataSourceTypes;
import org.wso2.carbon.dataservices.common.DBConstants.GSpread;
import org.wso2.carbon.dataservices.common.DBConstants.GSpreadVisibility;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.dataservices.core.internal.DataServicesDSComponent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

/**
 * This class represents a Google Spreadsheet based data source configuration.
 */
public class GSpreadConfig extends Config {

	private static final Log log = LogFactory.getLog(GSpreadConfig.class);

	public static final String BASE_WORKSHEET_URL = "https://spreadsheets.google.com/feeds/worksheets/";

	public static final String BASE_REGISTRY_AUTH_TOKEN_PATH = "/repository/components/org.wso2.carbon.dataservices.core/services/";

    private String clientId;

    private String clientSecret;

    private String accessToken;

    private String refreshToken;

	private String visibility;

	private String key;

	private SpreadsheetService service;

	public GSpreadConfig(DataService dataService, String configId, Map<String,
			String> properties) throws DataServiceFault {
		super(dataService, configId, DataSourceTypes.GDATA_SPREADSHEET, properties);

		this.clientId = DBUtils.resolvePasswordValue(this.getDataService(), this.getProperty(GSpread.CLIENT_ID));
		this.clientSecret = DBUtils.resolvePasswordValue(this.getDataService(), this.getProperty(GSpread.CLIENT_SECRET));
//		this.accessToken = DBUtils.resolvePasswordValue(this.getDataService(), this.getProperty(GSpread.ACCESS_TOKEN));
		this.refreshToken = DBUtils.resolvePasswordValue(this.getDataService(), this.getProperty(GSpread.REFRESH_TOKEN));
		this.visibility = this.getProperty(DBConstants.GSpread.VISIBILITY);
		this.key = extractKey(this.getProperty(GSpread.DATASOURCE));
		if (!dataService.isServiceInactive()) {
		    this.service = new SpreadsheetService(this.getDataService().getName() +
		    		":" + this.getConfigId());
		}
	}

	public static String extractKey(String documentURL) throws DataServiceFault {
		URI documentURI;
		try {
			documentURI = new URI(documentURL);
		} catch (URISyntaxException e) {
			String message = "Document URL Syntax error:" + documentURL;
			log.warn(message,e);
			throw new DataServiceFault(e, message);
		}
		String extractedQuery = documentURI.getQuery();
        if (extractedQuery == null) {
            String message = "Error Generating Query for given Document:" + documentURL;
            log.warn(message);
            throw new DataServiceFault(message);
        }
        int i1 = extractedQuery.lastIndexOf("key=");
        int i2 = extractedQuery.indexOf("&", i1);
        if (i1 == -1) {
            return getKeyForNewSpreadsheetURLFormat(documentURL);
        } else if (i2 < 0) {
			return extractedQuery.substring(i1 + 4);
		} else {
			return extractedQuery.substring(i1 + 4, i2);
		}
	}

    private static String getKeyForNewSpreadsheetURLFormat(String documentURI) throws DataServiceFault {
        String [] params = documentURI.split("/");
        String resultKey = null;
        for (int i = 0; i < params.length; i++) {
            if ("d".equals(params[i])) {
                resultKey = params[i+1];
            }
        }
        if (resultKey == null) {
            throw new DataServiceFault("Invalid URL format");
        }
        return resultKey;
    }

	public String generateWorksheetFeedURL() {
		return GSpreadConfig.BASE_WORKSHEET_URL + key + "/" +
				this.getVisibility() + "/basic";
	}

	public String getKey() {
		return key;
	}

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

	private SpreadsheetService getService() {
		return service;
	}

	public String getVisibility() {
		return visibility;
	}

	private String generateAuthTokenResourcePath() {
		String resPath = BASE_REGISTRY_AUTH_TOKEN_PATH
			+ this.getDataService().getName()
			+ "/configs/"
			+ this.getConfigId() + "/user_auth_token/users/"
			+ this.clientId;
		return resPath;
	}

	/**
	 * Returns the resource associated with the current gspread config user authentication token.
	 * the resource path is :-
	 * "/repository/components/org.wso2.carbon.dataservices.core/services/[service_id]/configs/[config_id]/user_auth_token"
	 */
	private Resource getAuthTokenResource(Registry registry) throws Exception {
		if (registry == null) {
			return null;
		}
		String resPath = this.generateAuthTokenResourcePath();
		if (!registry.resourceExists(resPath)) {
			return null;
		}
		return registry.get(resPath);
	}

    /**
     * Helper method to get current access token resides in the registry.
     *
     * @return accessToken retrieved from registry
     * @throws Exception
     */
	private String getAccessTokenFromRegistry() throws Exception {
        if (DataServicesDSComponent.getRegistryService() == null) {
            String msg = "GSpreadConfig.getFeed(): Registry service is not available, authentication key sharing fails";
            log.error(msg);
            throw new DataServiceFault(msg);
        }
        //using conf registry since we can use that to share the token between nodes
        Registry registry = DataServicesDSComponent.getRegistryService()
                .getConfigSystemRegistry(DBUtils.getCurrentTenantId());
		Resource authTokenRes = this.getAuthTokenResource(registry);
		if (authTokenRes != null) {
			Object content = authTokenRes.getContent();
			if (content != null) {
				return new String((byte[]) content, DBConstants.DEFAULT_CHAR_SET_TYPE);
			}
		}
		return null;
	}

    /**
     * helper method to save new access token to registry
     *
     * @throws Exception
     */
	private void saveTokenToRegistry() throws Exception {
        if (DataServicesDSComponent.getRegistryService() == null) {
            String msg = "GSpreadConfig.getFeed(): Registry service is not available, authentication key sharing fails";
            log.error(msg);
            throw new DataServiceFault(msg);
        }
        //using conf registry since we can use that to share the token between nodes
        Registry registry = DataServicesDSComponent.getRegistryService()
                .getConfigSystemRegistry(DBUtils.getCurrentTenantId());
		registry.beginTransaction();
		Resource res = registry.newResource();
		res.setContent(this.accessToken.getBytes(DBConstants.DEFAULT_CHAR_SET_TYPE));
		registry.put(this.generateAuthTokenResourcePath(), res);
		registry.commitTransaction();
	}

    /**
     * This method has the logic implemented to use access token to access spreadsheet api
     * and it will be shared between cluster nodes via registry as well. this will refresh the access token
     * if access tokens stored in memory and registry are expired, then it will store the new access token
     * in registry so that it will be shared among nodes.
     *
     * @param feedUrl
     * @param feedClass
     * @param <F>
     * @return feed
     * @throws Exception
     */
	public <F extends IFeed> F getFeed(URL feedUrl, Class<F> feedClass) throws Exception {
        if (this.requiresAuth()) {
            if (this.accessToken != null) {
                this.authenticateWithAccessToken();
                try {
                    return this.getService().getFeed(feedUrl, feedClass);
                } catch (Exception e) {
                    log.warn("GSpreadConfig.getFeed(): Failed to retrieve Feeds with current AccessToken ", e);
                }
                String accessTokenFromRegistry = this.getAccessTokenFromRegistry();
                if (accessTokenFromRegistry != null && this.accessToken != accessTokenFromRegistry) {
                    this.accessToken = accessTokenFromRegistry;
                    this.authenticateWithAccessToken();
                    try {
                        return this.getService().getFeed(feedUrl, feedClass);
                    } catch (Exception e) {
                        log.warn("GSpreadConfig.getFeed(): Failed to retrieve Feeds with AccessToken from registry ", e);
                    }
                }
            }
            this.refreshAndAuthenticate();
            this.saveTokenToRegistry();
            return this.getService().getFeed(feedUrl, feedClass);
        }
        return this.getService().getFeed(feedUrl, feedClass);
	}

    /**
     * helper method to authenticate using just access token
     */
    private void authenticateWithAccessToken() {
        GoogleCredential credential = getBaseCredential();
        credential.setAccessToken(this.accessToken);
        service.setOAuth2Credentials(credential);
    }

    /**
     * helper method to refresh the access token and authenticate
     *
     * @throws Exception
     */
    private void refreshAndAuthenticate() throws Exception {
        GoogleCredential credential = getBaseCredential();
        credential.setAccessToken(this.accessToken);
        credential.setRefreshToken(this.refreshToken);
        credential.refreshToken();
        this.accessToken = credential.getAccessToken();
        service.setOAuth2Credentials(credential);
    }

    /**
     * helper method to get the base credential object
     *
     * @return credential
     */
    private GoogleCredential getBaseCredential(){
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = new GoogleCredential.Builder()
                .setClientSecrets(this.clientId, this.clientSecret)
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .build();
        return credential;
    }

    /**
     * method to check whether authentication is required or not
     *
     * @return true if authentication is required else false
     */
	private boolean requiresAuth() {
		return (this.getVisibility() != null &&
				this.getVisibility().equals(GSpreadVisibility.PRIVATE));
	}

	@Override
	public boolean isActive() {
		return this.getService() != null;
	}

	public void close() {
		/* nothing to close */
	}

}
