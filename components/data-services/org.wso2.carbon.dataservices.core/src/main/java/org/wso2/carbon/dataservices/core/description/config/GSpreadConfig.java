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

import com.google.gdata.client.GoogleAuthTokenFactory.UserToken;
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
	
	public static final String BASE_WORKSHEET_URL = "http://spreadsheets.google.com/feeds/worksheets/";
	
	public static final String BASE_REGISTRY_AUTH_TOKEN_PATH = "/repository/components/org.wso2.carbon.dataservices.core/services/";
	
	private String username;
	
	private String password;
	
	private String visibility;
	
	private String key;
	
	private SpreadsheetService service;
		
	public GSpreadConfig(DataService dataService, String configId, Map<String, 
			String> properties) throws DataServiceFault {
		super(dataService, configId, DataSourceTypes.GDATA_SPREADSHEET, properties);
		
		this.username = this.getProperty(DBConstants.GSpread.USERNAME);
		this.password = DBUtils.resolvePasswordValue(this.getDataService(), this.getProperty(DBConstants.GSpread.PASSWORD));
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
		int i1 = extractedQuery.lastIndexOf("key=");
		int i2 = extractedQuery.indexOf("&", i1);
		if (i2 < 0) {
			return extractedQuery.substring(i1 + 4);
		} else {
			return extractedQuery.substring(i1 + 4, i2);
		}
	}
	
	public String generateWorksheetFeedURL() {
		return GSpreadConfig.BASE_WORKSHEET_URL + key + "/" + 
				this.getVisibility() + "/basic";
	}

	public String getKey() {
		return key;
	}

	public String getPassword() {
		return password;
	}

	private SpreadsheetService getService() {
		return service;
	}

	public String getUsername() {
		return username;
	}

	public String getVisibility() {
		return visibility;
	}
	
	private String generateAuthTokenResourcePath() {
		StringBuilder userKey = new StringBuilder();
		/* append the username value 3 times because,
		 * later when we do base64 encoding, we have to be sure,
		 * it doesn't have "=" characters by making the source data
		 * a multiple of 3, thus not to have any padding data.
		 */
		String userName = this.getUsername();
		userKey.append(userName);
		userKey.append(userName);
		userKey.append(userName);
		String resPath = "/repository/components/org.wso2.carbon.dataservices.core/services/"
			+ this.getDataService().getName()
			+ "/configs/"
			+ this.getConfigId() + "/user_auth_token/users/" 
			+ DBUtils.encodeBase64(userKey.toString());
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
	
	private String getLocalUserAuthToken(Registry registry) throws Exception {
		Resource authTokenRes = this.getAuthTokenResource(registry);
		if (authTokenRes != null) {
			Object content = authTokenRes.getContent();
			if (content != null) {
				return new String((byte[]) content, DBConstants.DEFAULT_CHAR_SET_TYPE);
			}
		}
		return null;
	}
	
	private void setLocalUserAuthToken(String userToken, Registry registry) throws Exception {
		registry.beginTransaction();
		Resource res = registry.newResource();
		res.setContent(userToken.getBytes(DBConstants.DEFAULT_CHAR_SET_TYPE));
		registry.put(this.generateAuthTokenResourcePath(), res);
		registry.commitTransaction();
	}
	
	public <F extends IFeed> F getFeed(URL feedUrl, Class<F> feedClass) throws Exception {
		try {
		    return this.getService().getFeed(feedUrl, feedClass);
		} catch (Exception e) {
			/* authenticate again in case the user authentication token has expired */
			if (this.requiresAuth()) {
				Registry registry = null;
				if (DataServicesDSComponent.getRegistryService() == null) {
					log.warn("GSpreadConfig.getFeed(): Registry service is not available, authentication keys wont be saved");
				} else {
					registry = DataServicesDSComponent.getRegistryService()
							.getConfigSystemRegistry(DBUtils.getCurrentTenantId());
				}
			    this.authenticate(true, registry);
			    return this.getService().getFeed(feedUrl, feedClass);
			} else {
				throw e;
			}
		}
	}
	
	private boolean requiresAuth() {
		return (this.getVisibility() != null && 
				this.getVisibility().equals(GSpreadVisibility.PRIVATE));
	}
	
	private String getNewUserAuthToken() throws Exception {
		this.getService().setUserCredentials(this.getUsername(), this.getPassword());
		return ((UserToken) this.getService().getAuthTokenFactory().getAuthToken()).getValue();
	}
	
	private void authenticate(boolean refreshToken, Registry registry) throws Exception {
		String userToken;
		if (refreshToken || (registry == null)
				|| (userToken = this.getLocalUserAuthToken(registry)) == null) {
			userToken = this.getNewUserAuthToken();
			if (registry != null) {
			    this.setLocalUserAuthToken(userToken, registry);
			}
		}
		this.getService().setUserToken(userToken);
	}
    
	@Override
	public boolean isActive() {
		return this.getService() != null;
	}

	public void close() {
		/* nothing to close */
	}

}
