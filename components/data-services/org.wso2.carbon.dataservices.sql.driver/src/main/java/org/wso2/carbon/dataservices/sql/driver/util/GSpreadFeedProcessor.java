package org.wso2.carbon.dataservices.sql.driver.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gdata.client.Query;
import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.ListQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.client.spreadsheet.WorksheetQuery;
import com.google.gdata.data.IEntry;
import com.google.gdata.data.IFeed;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.sql.driver.TDriverUtil;
import org.wso2.carbon.dataservices.sql.driver.TGSpreadConnection;
import org.wso2.carbon.dataservices.sql.driver.internal.SQLDriverDSComponent;
import org.wso2.carbon.dataservices.sql.driver.query.ColumnInfo;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to manipulate feed requests with access tokens
 */
public class GSpreadFeedProcessor {

    private static final Log log = LogFactory.getLog(GSpreadFeedProcessor.class);

    private String clientId;

    private String clientSecret;

    private String accessToken;

    private String refReshToken;

    private boolean requireAuth;

    private SpreadsheetService service;

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefReshToken(String refReshToken) {
        this.refReshToken = refReshToken;
    }

    public void setRequireAuth(boolean requireAuth) {
        this.requireAuth = requireAuth;
    }

    public void setService(SpreadsheetService service) {
        this.service = service;
    }

    private String charSetType = "UTF-8";

    private TGSpreadConnection connection;

    public static final String BASE_REGISTRY_AUTH_TOKEN_PATH = "/repository/components/org.wso2.carbon.dataservices.sql.driver/tokens/";

    public GSpreadFeedProcessor(Connection connection) {
        this.connection = (TGSpreadConnection)connection;
    }


    public <E extends IEntry> E insert(URL feedUrl, E entry) throws SQLException {
        try {
            if (this.requireAuth) {
                if (this.accessToken != null) {
                    this.authenticateWithAccessToken();
                    try {
                        return this.service.insert(feedUrl, entry);
                    } catch (Exception e) {
                        log.warn("GSpreadConfig.getFeed(): Failed to retrieve Feeds with current AccessToken ", e);
                    }
                    String accessTokenFromRegistry = this.getAccessTokenFromRegistry();
                    if (accessTokenFromRegistry != null && this.accessToken != accessTokenFromRegistry) {
                        this.accessToken = accessTokenFromRegistry;
                        this.authenticateWithAccessToken();
                        try {
                            return this.service.insert(feedUrl, entry);
                        } catch (Exception e) {
                            log.warn("GSpreadConfig.getFeed(): Failed to retrieve Feeds with AccessToken from registry ", e);
                        }
                    }
                }
                this.refreshAndAuthenticate();
                this.saveTokenToRegistry();
            }
            return this.service.insert(feedUrl, entry);
        } catch (Exception e) {
            throw new SQLException("Error in retrieving Feed data " + e.getMessage(), e);
        }
    }

    /**
     * this method has the logic implemented to use access token to access spreadsheet api
     * and it will be shared between cluster nodes via registry as well. this will refresh the access token
     * if access tokens stored in memory and registry are expired, then it will store the new access token
     * in registry so that it will be shared among nodes
     *
     * @param feedUrl
     * @param feedClass
     * @param <F>
     * @return feed
     * @throws Exception
     */
    public <F extends IFeed> F getFeed(URL feedUrl, Class<F> feedClass) throws SQLException {
        try {
            if (this.requireAuth) {
                if (this.accessToken != null) {
                    this.authenticateWithAccessToken();
                    try {
                        return this.service.getFeed(feedUrl, feedClass);
                    } catch (Exception e) {
                        log.warn("GSpreadConfig.getFeed(): Failed to retrieve Feeds with current AccessToken ", e);
                    }
                    String accessTokenFromRegistry = this.getAccessTokenFromRegistry();
                    if (accessTokenFromRegistry != null && this.accessToken != accessTokenFromRegistry) {
                        this.accessToken = accessTokenFromRegistry;
                        this.authenticateWithAccessToken();
                        try {
                            return this.service.getFeed(feedUrl, feedClass);
                        } catch (Exception e) {
                            log.warn("GSpreadConfig.getFeed(): Failed to retrieve Feeds with AccessToken from registry ", e);
                        }
                    }
                }
                this.refreshAndAuthenticate();
                this.saveTokenToRegistry();
            }
            return this.service.getFeed(feedUrl, feedClass);
        } catch (Exception e) {
            throw new SQLException("Error in retrieving Feed data " + e.getMessage(), e);
        }
    }

    /**
     * this method has the logic implemented to use access token to access spreadsheet api
     * and it will be shared between cluster nodes via registry as well. this will refresh the access token
     * if access tokens stored in memory and registry are expired, then it will store the new access token
     * in registry so that it will be shared among nodes
     *
     * @param query
     * @param feedClass
     * @param <F>
     * @return feed
     * @throws Exception
     */
    public <F extends IFeed> F getFeed(Query query, Class<F> feedClass) throws SQLException {
        try {
            if (this.requireAuth) {
                if (this.accessToken != null) {
                    this.authenticateWithAccessToken();
                    try {
                        return this.service.getFeed(query, feedClass);
                    } catch (Exception e) {
                        log.warn("GSpreadConfig.getFeed(): Failed to retrieve Feeds with current AccessToken ", e);
                    }
                    String accessTokenFromRegistry = this.getAccessTokenFromRegistry();
                    if (accessTokenFromRegistry != null && this.accessToken != accessTokenFromRegistry) {
                        this.accessToken = accessTokenFromRegistry;
                        this.authenticateWithAccessToken();
                        try {
                            return this.service.getFeed(query, feedClass);
                        } catch (Exception e) {
                            log.warn("GSpreadConfig.getFeed(): Failed to retrieve Feeds with AccessToken from registry ", e);
                        }
                    }
                }
                this.refreshAndAuthenticate();
                this.saveTokenToRegistry();
            }
            return this.service.getFeed(query, feedClass);
        } catch (Exception e) {
            throw new SQLException("Error in retrieving Feed data " + e.getMessage(), e);
        }
    }

    /**
     * helper method to authenticate using just access token
     */
    private void authenticateWithAccessToken() {
        GoogleCredential credential = getBaseCredential();
        credential.setAccessToken(this.accessToken);
        this.service.setOAuth2Credentials(credential);
    }

    /**
     * helper method to refresh the access token and authenticate
     *
     * @throws Exception
     */
    private void refreshAndAuthenticate() throws Exception {
        GoogleCredential credential = getBaseCredential();
        credential.setAccessToken(this.accessToken);
        credential.setRefreshToken(this.refReshToken);
        credential.refreshToken();
        this.accessToken = credential.getAccessToken();
        this.service.setOAuth2Credentials(credential);
    }

    /**
     * helper method to get the base credential object
     *
     * @return credential
     */
    private GoogleCredential getBaseCredential() {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = new GoogleCredential.Builder()
                .setClientSecrets(this.clientId, this.clientSecret)
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .build();
        return credential;
    }

    private String generateAuthTokenResourcePath() {
//		StringBuilder userKey = new StringBuilder();
//		/* append the username value 3 times because,
//		 * later when we do base64 encoding, we have to be sure,
//		 * it doesn't have "=" characters by making the source data
//		 * a multiple of 3, thus not to have any padding data.
//		 */
        String resPath = BASE_REGISTRY_AUTH_TOKEN_PATH
                + "configs/"
                + "user_auth_token/users/"
                + this.connection.getClientId();
        return resPath;
    }

    /**
     * Returns the resource associated with the current gspread config user authentication token.
     * the resource path is :-
     * "/repository/components/org.wso2.carbon.dataservices.core/services/[service_id]/configs/[config_id]/
     * user_auth_token"
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
     * @return accessToken
     * @throws Exception
     */
    private String getAccessTokenFromRegistry() throws Exception {
        if (SQLDriverDSComponent.getRegistryService() == null) {
            String msg = "GSpreadConfig.getFeed(): Registry service is not available, authentication key sharing fails";
            log.error(msg);
            throw new SQLException(msg);
        }
        Registry registry = SQLDriverDSComponent.getRegistryService()
                        .getGovernanceSystemRegistry(TDriverUtil.getCurrentTenantId());
        Resource authTokenRes = this.getAuthTokenResource(registry);
        if (authTokenRes != null) {
            Object content = authTokenRes.getContent();
            if (content != null) {
                return new String((byte[]) content, this.charSetType);
            }
        }
        return null;
    }

    /**
     * Helper method to save new access token to registry.
     *
     * @throws Exception
     */
    private void saveTokenToRegistry() throws Exception {
        if (SQLDriverDSComponent.getRegistryService() == null) {
            String msg = "GSpreadConfig.getFeed(): Registry service is not available, authentication key cannot be" +
                         " saved";
            log.error(msg);
            throw new SQLException(msg);
        }
        Registry registry = SQLDriverDSComponent.getRegistryService()
                .getGovernanceSystemRegistry(TDriverUtil.getCurrentTenantId());
        registry.beginTransaction();
        Resource res = registry.newResource();
        res.setContent(this.accessToken.getBytes(this.charSetType));
        registry.put(this.generateAuthTokenResourcePath(), res);
        registry.commitTransaction();
    }

    public ColumnInfo[] getGSpreadHeaders(String sheetName) throws SQLException {
        WorksheetEntry currentWorksheet;
        List<ColumnInfo> columns = new ArrayList<ColumnInfo>();

        if (!(this.connection instanceof TGSpreadConnection)) {
            throw new SQLException("Invalid connection type");
        }
        currentWorksheet = getCurrentWorkSheetEntry(sheetName);
        if (currentWorksheet == null) {
            throw new SQLException("Worksheet '" + sheetName + "' does not exist");
        }
        CellFeed cellFeed = getCellFeed(currentWorksheet);
        for (CellEntry cell : cellFeed.getEntries()) {
            if (!TDriverUtil.getCellPosition(cell.getId()).startsWith("R1")) {
                break;
            }
            ColumnInfo column =
                    new ColumnInfo(cell.getTextContent().getContent().getPlainText());
            column.setTableName(sheetName);
            column.setSqlType(cell.getContent().getType());
            column.setId(TDriverUtil.getColumnIndex(cell.getId()) - 1);
            columns.add(column);
        }
        return columns.toArray(new ColumnInfo[columns.size()]);
    }

    public CellFeed getCellFeed(WorksheetEntry currentWorkSheet) throws SQLException {
        CellQuery cellQuery = new CellQuery(currentWorkSheet.getCellFeedUrl());
        return getFeed(cellQuery, CellFeed.class);
    }

    public WorksheetEntry getCurrentWorkSheetEntry(String sheetName) throws SQLException {
        SpreadsheetEntry spreadsheetEntry = this.connection.getSpreadSheetFeed().getEntries().get(0);
        WorksheetQuery worksheetQuery =
                TDriverUtil.createWorkSheetQuery(spreadsheetEntry.getWorksheetFeedUrl());
        WorksheetFeed worksheetFeed = getFeed(worksheetQuery,
                WorksheetFeed.class);
        for (WorksheetEntry entry : worksheetFeed.getEntries()) {
            if (sheetName.equals(entry.getTitle().getPlainText())) {
                return entry;
            }
        }
        return null;
    }

    public ListFeed getListFeed(WorksheetEntry currentWorkSheet) throws SQLException {
        ListQuery listQuery = new ListQuery(currentWorkSheet.getListFeedUrl());
        return getFeed(listQuery, ListFeed.class);
    }
}
