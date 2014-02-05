/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dataservices.sql.driver;

import com.google.gdata.client.GoogleAuthTokenFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.client.spreadsheet.WorksheetQuery;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.AuthenticationException;
import org.wso2.carbon.dataservices.sql.driver.parser.Constants;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.List;
import java.util.Properties;

public class TGSpreadConnection extends TConnection {

    private String visibility = Constants.ACCESS_MODE_PRIVATE;

    private SpreadsheetService service;

    private String spreadSheetName;

    private SpreadsheetFeed spreadSheetFeed;

    private WorksheetFeed worksheetFeed;

    public TGSpreadConnection(Properties props) throws SQLException {
        super(props);
        this.spreadSheetName = props.getProperty(Constants.DRIVER_PROPERTIES.SHEET_NAME);
        if (spreadSheetName == null) {
            throw new SQLException("Spread Sheet name is not provided");
        }
        this.visibility = props.getProperty(Constants.DRIVER_PROPERTIES.VISIBILITY);
        this.visibility = (visibility != null) ? visibility : Constants.ACCESS_MODE_PRIVATE;
        if (!this.checkVisibility(visibility)) {
            throw new SQLException("Invalid access mode '" + visibility + "' is provided");
        }
        this.service = new SpreadsheetService(Constants.SPREADSHEET_SERVICE_NAME);
        this.service.setCookieManager(null);

        if (Constants.ACCESS_MODE_PRIVATE.equals(visibility)) {
            try {
                this.service.setUserCredentials(this.getUsername(), this.getPassword());
                this.service.setUserToken(((GoogleAuthTokenFactory.UserToken)
                        service.getAuthTokenFactory().
                                getAuthToken()).getValue());
            } catch (AuthenticationException e) {
                throw new SQLException("Error occurred while authenticating user to access the " +
                        "spread sheet", e);
            }
        }
        this.spreadSheetFeed = this.extractSpreadSheetFeed();
        this.worksheetFeed = this.extractWorkSheetFeed();
    }

    public SpreadsheetService getSpreadSheetService() {
        return service;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getSpreadSheetName() {
        return spreadSheetName;
    }

    public WorksheetFeed getWorksheetFeed() {
        return worksheetFeed;
    }

    public SpreadsheetFeed getSpreadSheetFeed() {
        return spreadSheetFeed;
    }

    private boolean checkVisibility(String visibility) {
        return (Constants.ACCESS_MODE_PRIVATE.equals(visibility) ||
                Constants.ACCESS_MODE_PUBLIC.equals(visibility));
    }

    @Override
    public Statement createStatement() throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatements are not supported");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatements are not supported");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatements are not supported");
    }

    @Override
    public PreparedStatement prepareStatement(String sql,
                                              int autoGeneratedKeys) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql,
                                              int[] columnIndexes) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws
            SQLException {
        return new TPreparedStatement(this, sql);
    }

    private WorksheetFeed extractWorkSheetFeed() throws SQLException {
        if (this.getSpreadSheetFeed() == null) {
            throw new SQLException("Spread Sheet Feed is null");
        }
        List<SpreadsheetEntry> entries = this.getSpreadSheetFeed().getEntries();
        /* If no SpreadSheetEntry is available in the spreadsheet feed inferred using a
         * SpreadSheetQuery, try getting it directly via a SpreadSheetFeed retrieved via the 
         * SpreadSheetService */
        SpreadsheetEntry spreadsheetEntry =
                (entries != null && entries.size() > 0) ? entries.get(0) :
                        this.extractSpreadSheetEntryFromUrl();
        if (spreadsheetEntry == null) {
            throw new SQLException("No SpreadSheetEntry is available, matching provided " +
                    "connection information");
        }
        WorksheetQuery worksheetQuery =
                TDriverUtil.createWorkSheetQuery(spreadsheetEntry.getWorksheetFeedUrl());
        return TDriverUtil.getFeed(this.getSpreadSheetService(), worksheetQuery,
                WorksheetFeed.class);
    }

    private SpreadsheetEntry extractSpreadSheetEntryFromUrl() throws SQLException {
        try {
            URL spreadSheetFeedUrl = this.getSpreadSheetFeedUrl();
            SpreadsheetFeed feed =
                    this.getSpreadSheetService().getFeed(spreadSheetFeedUrl, SpreadsheetFeed.class);
            List<SpreadsheetEntry> entries = feed.getEntries();
            return (entries != null && entries.size() > 0) ? entries.get(0) : null;
        } catch (Exception e) {
            throw new SQLException("Error occurred while extracting spread sheet entry", e);
        }
    }

    private URL getSpreadSheetFeedUrl() throws MalformedURLException {
        return new URL(Constants.SPREADSHEET_FEED_BASE_URL + getVisibility() + "/full");
    }

    private SpreadsheetFeed extractSpreadSheetFeed() throws SQLException {
        URL spreadSheetFeedUrl;
        try {
            spreadSheetFeedUrl = this.getSpreadSheetFeedUrl();
        } catch (MalformedURLException e) {
            throw new SQLException("Error occurred while constructing the Spread Sheet Feed URL");
        }
        SpreadsheetQuery spreadSheetQuery =
                TDriverUtil.createSpreadSheetQuery(this.getSpreadSheetName(), spreadSheetFeedUrl);
        return TDriverUtil.getFeed(getSpreadSheetService(), spreadSheetQuery,
                SpreadsheetFeed.class);
    }

}
