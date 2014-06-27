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
package org.wso2.carbon.dataservices.core.description.query;

import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.AdvancedSQLProps;
import org.wso2.carbon.dataservices.common.DBConstants.AutoCommit;
import org.wso2.carbon.dataservices.common.DBConstants.DataTypes;
import org.wso2.carbon.dataservices.common.DBConstants.FaultCodes;
import org.wso2.carbon.dataservices.common.DBConstants.QueryTypes;
import org.wso2.carbon.dataservices.common.DBConstants.RDBMS;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceConnection;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.TLConnectionStore;
import org.wso2.carbon.dataservices.core.description.config.SQLConfig;
import org.wso2.carbon.dataservices.core.description.event.EventTrigger;
import org.wso2.carbon.dataservices.core.dispatch.BatchDataServiceRequest;
import org.wso2.carbon.dataservices.core.dispatch.BatchRequestParticipant;
import org.wso2.carbon.dataservices.core.dispatch.DispatchStatus;
import org.wso2.carbon.dataservices.core.engine.*;

import javax.xml.stream.XMLStreamWriter;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.util.*;

/**
 * This class represents an SQL query in a data service.
 */
public class SQLQuery extends Query implements BatchRequestParticipant {

    private static final Log log = LogFactory.getLog(SQLQuery.class);

    public static final int DS_QUERY_TYPE_NORMAL = 0x01;

    public static final int DS_QUERY_TYPE_STORED_PROC = 0x02;

    public static final int ORACLE_REF_CURSOR_TYPE = -10;

    private String query;

    private SQLConfig config;

    private int queryType;

    private List<QueryParam> outQueryParams;

    private boolean hasOutParams;

    private boolean resultOnlyOutParams;

    private List<String> namedParamNames;

    private QueryParam currentRefCursor;

    private boolean hasRefCursor;

    private String sql;

    private int paramCount;

    private int optimalRSFetchSize;

    private boolean hasFetchDirection;

    private boolean hasFetchSize;

    private boolean hasMaxFieldSize;

    private boolean hasMaxRows;

    private boolean hasQueryTimeout;

    private int fetchDirection;

    private int fetchSize;

    private int maxFieldSize;

    private int maxRows;

    private int queryTimeout;

    private boolean returnGeneratedKeys;

    private String[] keyColumns;

    private boolean hasBatchQuerySupport;

    private AutoCommit autoCommit;

    private boolean forceStoredProc;

    private boolean forceJDBCBatchReqs;

    /**
     * thread local variable to keep a PreparedStatement in batch processing
     */
    private ThreadLocal<PreparedStatement> batchPreparedStatement = new ThreadLocal<PreparedStatement>() {
        protected synchronized PreparedStatement initialValue() {
            return null;
        }
    };

    public SQLQuery(DataService dataService, String queryId, String configId,
            boolean returnGeneratedKeys, String[] keyColumns, String query,
            List<QueryParam> queryParams, Result result, EventTrigger inputEventTrigger,
            EventTrigger outputEventTrigger, Map<String, String> advancedProperties,
            String inputNamespace) throws DataServiceFault {
        super(dataService, queryId, queryParams, result, configId, inputEventTrigger,
                outputEventTrigger, advancedProperties, inputNamespace);
        this.returnGeneratedKeys = returnGeneratedKeys;
        this.keyColumns = keyColumns;
        this.query = query;
        try {
            this.config = (SQLConfig) this.getDataService().getConfig(this.getConfigId());
        } catch (ClassCastException e) {
            throw new DataServiceFault(e, "Configuration is not an SQL config:"
                    + this.getConfigId());
        }
        this.init();
    }

    public void init() throws DataServiceFault {
        /* process the advanced/additional properties */
        this.processAdvancedProps(this.getAdvancedProperties());
        this.queryType = this.retrieveQueryType(this.getQuery());
        this.processNamedParams();
        this.outQueryParams = this.extractOutQueryParams(this.getQueryParams());
        /* re-format sql query with named params */
        this.sql = this.createSqlFromQueryString(this.getQuery());
        this.paramCount = this.calculateParamCount(this.getSql());
        /* check for existence of any ref cursors */
        this.checkRefCursor(this.getQueryParams());
        /* check for existence of any SQL Arrays */
        this.hasOutParams = this.getOutQueryParams().size() > 0;
        /*
         * first a result should be available and then check the other necessary
         * conditions
         */
        this.resultOnlyOutParams = this.calculateResultOnlyOutParams();
        /* set the optimal JDBC result set fetch size */
        this.optimalRSFetchSize = DBUtils.getOptimalRSFetchSizeForRDBMS(this.getConfig()
                .getProperty(RDBMS.URL));
        /* set batch update support for this query */
        try {
            this.hasBatchQuerySupport = this.getDataService().isBatchRequestsEnabled()
                    && (this.isForceJDBCBatchReqs() || this.calculateBatchQuerySupport());
        } catch (DataServiceFault e) {
            this.hasBatchQuerySupport = false;
            log.warn("Unable to determine batch query support for query '" + this.getQueryId()
                    + "' : " + e.getMessage() + " - batch query support is disabled.");
        }
    }

    private boolean calculateResultOnlyOutParams() {
        return (this.getResult() != null)
                && (this.hasRefCursor() || (this.hasOutParams() && ((this.getResult()
                        .getDefaultElementGroup().getAllElements().size() + this.getResult()
                        .getDefaultElementGroup().getAttributeEntries().size()) == this
                        .getOutQueryParams().size())));
    }

    /**
     * Extract the stored proc name from the SQL.
     * 
     * @param skipFirstWord
     *            If the first word should be considered the name or not, if
     *            this is true, the second word will be considered as the stored
     *            proc name
     * @return The stored procedure name
     */
    private String extractStoredProcName(boolean skipFirstWord) {
        String sql = this.getSql();
        String[] tokens = removeSpaces(sql.split("\\s|\\(|\\["));
        if (skipFirstWord) {
            if (tokens.length < 2) {
                return null;
            }
            return tokens[1];
        } else {
            if (tokens.length < 1) {
                return null;
            }
            return tokens[0];
        }
    }

    private static String[] removeSpaces(String[] vals) {
        List<String> result = new ArrayList<String>();
        for (String val : vals) {
            if (val.trim().length() > 0) {
                result.add(val);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    private Object[] getStoredProcFuncProps(String name) throws DataServiceFault, SQLException {
        Connection conn = this.getConfig().createConnection();
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = null;
        boolean error = true;
        try {
            rs = md.getProcedureColumns(null, null, name, "%");
            Object[] resultMap = new Object[2];
            if (!rs.next()) {
                rs.close();
                rs = md.getFunctionColumns(null, null, name, "%");
            } else {
                rs.close();
                rs = md.getProcedureColumns(null, null, name, "%");
            }
            resultMap[0] = conn;
            resultMap[1] = rs;
            error = false;
            return resultMap;
        } finally {
            if (error) {
                this.releaseResources(rs, null);
            }
        }
    }

    private boolean calculateBatchQuerySupport() throws DataServiceFault {
        Object[] resultMap;
        List<Connection> connections = new ArrayList<Connection>();
        if (this.getConfig().hasJDBCBatchUpdateSupport()) {
            if (this.getQueryType() == SQLQuery.DS_QUERY_TYPE_STORED_PROC) {
                try {
                    resultMap = this.getStoredProcFuncProps(this.extractStoredProcName(true));
                    ResultSet rs = (ResultSet) resultMap[1];
                    connections.add((Connection) resultMap[0]);
                    if (!rs.next()) {
                        resultMap = this.getStoredProcFuncProps(this.extractStoredProcName(false));
                        rs = (ResultSet) resultMap[1];
                        connections.add((Connection) resultMap[0]);
                        if (!rs.next()) {
                            throw new DataServiceFault(
                                    "Cannot find metadata for the stored procedure");
                        }
                    }
                    /*
                     * stored procedures here can only have IN params and
                     * results which only returns an integer, which has the
                     * update count, all other situations are not supported for
                     * batch updates
                     */
                    StoredProcMetadataCollection mdCollection = new StoredProcMetadataCollection(rs);
                    for (StoredProcMetadataEntry entry : mdCollection.getEntries()) {
                        switch (entry.getColumnReturn()) {
                        case DatabaseMetaData.procedureColumnIn:
                            break;
                        case DatabaseMetaData.procedureColumnReturn:
                            if (!(entry.getColumnDataType() == Types.INTEGER
                                    || entry.getColumnDataType() == Types.BIGINT || entry
                                        .getColumnDataType() == Types.DECIMAL)) {
                                return false;
                            }
                            break;
                        default:
                            return false;
                        }
                    }
                    return true;
                } catch (Throwable e) {
                    throw new DataServiceFault("Error in retrieving database metadata.");
                } finally {
                    for (Connection aCon : connections) {
                        try {
                            aCon.close();
                        } catch (SQLException ignore) {
                        }
                    }
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public AutoCommit getAutoCommit() {
        return autoCommit;
    }

    public boolean hasBatchQuerySupport() {
        return hasBatchQuerySupport;
    }

    public int getParamCount() {
        return paramCount;
    }

    private PreparedStatement getBatchPreparedStatement() {
        return batchPreparedStatement.get();
    }

    private void setBatchPreparedStatement(PreparedStatement val) {
        batchPreparedStatement.set(val);
    }

    public String[] getKeyColumns() {
        return keyColumns;
    }

    public boolean isReturnGeneratedKeys() {
        return returnGeneratedKeys;
    }

    public boolean isForceStoredProc() {
        return forceStoredProc;
    }

    public boolean isForceJDBCBatchReqs() {
        return forceJDBCBatchReqs;
    }

    private void processAdvancedProps(Map<String, String> props) throws DataServiceFault {
        if (props == null) {
            return;
        }
        /* process fetch direction */
        String fetchDirectionProp = props.get(RDBMS.FETCH_DIRECTION);
        if (!DBUtils.isEmptyString(fetchDirectionProp)) {
            fetchDirectionProp = fetchDirectionProp.trim();
            if (AdvancedSQLProps.FETCH_DIRECTION_FORWARD.equals(fetchDirectionProp)) {
                this.fetchDirection = ResultSet.FETCH_FORWARD;
            } else if (AdvancedSQLProps.FETCH_DIRECTION_REVERSE.equals(fetchDirectionProp)) {
                this.fetchDirection = ResultSet.FETCH_REVERSE;
            } else {
                throw new DataServiceFault("Invalid fetch direction: " + fetchDirectionProp
                        + ", valid values are {'" + AdvancedSQLProps.FETCH_DIRECTION_FORWARD
                        + "', '" + AdvancedSQLProps.FETCH_DIRECTION_REVERSE + "'}");
            }
            this.hasFetchDirection = true;
        } else {
            this.hasFetchDirection = false;
        }
        /* process fetch size */
        String fetchSizeProp = props.get(RDBMS.FETCH_SIZE);
        if (!DBUtils.isEmptyString(fetchSizeProp)) {
            fetchSizeProp = fetchSizeProp.trim();
            try {
                this.fetchSize = Integer.parseInt(fetchSizeProp);
            } catch (NumberFormatException e) {
                throw new DataServiceFault(e, "Invalid fetch size: " + fetchSizeProp
                        + ", fetch size should be an integer");
            }
            this.hasFetchSize = true;
        } else {
            this.hasFetchSize = false;
        }
        /* process max field size */
        String maxFieldSizeProp = props.get(RDBMS.MAX_FIELD_SIZE);
        if (!DBUtils.isEmptyString(maxFieldSizeProp)) {
            maxFieldSizeProp = maxFieldSizeProp.trim();
            try {
                this.maxFieldSize = Integer.parseInt(maxFieldSizeProp);
                if (this.maxFieldSize <= 0) {
                    throw new DataServiceFault("Invalid maximum field size: " + maxFieldSizeProp
                            + ", maximum field size should be a positive integer");
                }
            } catch (NumberFormatException e) {
                throw new DataServiceFault(e, "Invalid maximum field size: " + maxFieldSizeProp
                        + ", maximum field size should be a positive integer");
            }
            this.hasMaxFieldSize = true;
        } else {
            this.hasMaxFieldSize = false;
        }
        /* process max rows */
        String maxRowsProp = props.get(RDBMS.MAX_ROWS);
        if (!DBUtils.isEmptyString(maxRowsProp)) {
            maxRowsProp = maxRowsProp.trim();
            try {
                this.maxRows = Integer.parseInt(maxRowsProp);
                if (this.maxRows <= 0) {
                    throw new DataServiceFault("Invalid maximum rows: " + maxRowsProp
                            + ", maximum rows should be a positive integer");
                }
            } catch (NumberFormatException e) {
                throw new DataServiceFault(e, "Invalid maximum rows: " + maxRowsProp
                        + ", maximum rows should be a positive integer");
            }
            this.hasMaxRows = true;
        } else {
            this.hasMaxRows = false;
        }
        /* process query timeout */
        String queryTimeoutProp = props.get(RDBMS.QUERY_TIMEOUT);
        if (!DBUtils.isEmptyString(queryTimeoutProp)) {
            queryTimeoutProp = queryTimeoutProp.trim();
            try {
                this.queryTimeout = Integer.parseInt(queryTimeoutProp);
                if (this.queryTimeout <= 0) {
                    throw new DataServiceFault("Invalid query timeout: " + queryTimeoutProp
                            + ", query timeout be a positive integer");
                }
            } catch (NumberFormatException e) {
                throw new DataServiceFault(e, "Invalid query timeout: " + queryTimeoutProp
                        + ", query timeout be a positive integer");
            }
            this.hasQueryTimeout = true;
        } else {
            this.hasQueryTimeout = false;
        }
        /* auto commit */
        /* first check local auto commit setting */
        String autoCommitProp = props.get(RDBMS.AUTO_COMMIT);
        if (!DBUtils.isEmptyString(autoCommitProp)) {
            autoCommitProp = autoCommitProp.trim();
            try {
                boolean acBool = Boolean.parseBoolean(autoCommitProp);
                if (acBool) {
                    this.autoCommit = AutoCommit.AUTO_COMMIT_ON;
                } else {
                    this.autoCommit = AutoCommit.AUTO_COMMIT_OFF;
                }
            } catch (Exception e) {
                throw new DataServiceFault(e, "Invalid autocommit value: " + autoCommitProp
                        + ", autocommit should be a boolean value");
            }
        } else {
            /* global auto commit setting */
            this.autoCommit = this.getConfig().getAutoCommit();
        }
        /* force stored procedure */
        String forceStoredProc = props.get(RDBMS.FORCE_STORED_PROC);
        if (!DBUtils.isEmptyString(forceStoredProc)) {
            this.forceStoredProc = Boolean.parseBoolean(forceStoredProc);
        }
        /* force JDBC batch requests */
        String forceJDBCBatchRequests = props.get(RDBMS.FORCE_JDBC_BATCH_REQUESTS);
        if (!DBUtils.isEmptyString(forceJDBCBatchRequests)) {
            this.forceJDBCBatchReqs = Boolean.parseBoolean(forceJDBCBatchRequests);
        }
    }

    public List<String> getNamedParamNames() {
        return namedParamNames;
    }

    public boolean isHasFetchDirection() {
        return hasFetchDirection;
    }

    public boolean isHasFetchSize() {
        return hasFetchSize;
    }

    public boolean isHasMaxFieldSize() {
        return hasMaxFieldSize;
    }

    public boolean isHasMaxRows() {
        return hasMaxRows;
    }

    public boolean isHasQueryTimeout() {
        return hasQueryTimeout;
    }

    public int getFetchDirection() {
        return fetchDirection;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public int getMaxFieldSize() {
        return maxFieldSize;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    public int getOptimalRSFetchSize() {
        return optimalRSFetchSize;
    }

    private void checkRefCursor(List<QueryParam> queryParams) {
        for (QueryParam queryParam : queryParams) {
            if (queryParam.getSqlType().equals(DBConstants.DataTypes.ORACLE_REF_CURSOR)) {
                this.currentRefCursor = queryParam;
                this.hasRefCursor = true;
                return;
            }
        }
    }

    private void sortStringsByLength(List<String> values) {
        Collections.sort(values, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.length() - rhs.length();
            }
        });
    }

    private String createSqlFromQueryString(String query) {
        /* get a copy of the param names */
        List<String> values = new ArrayList<String>(this.getNamedParamNames());
        /* sort the strings */
        this.sortStringsByLength(values);
        /*
         * make it from largest to smallest, this is done to make sure, if there
         * are params like, :abcd,:abc, then the step of replacing :abc doesn't
         * also initially replace :abcd's substring as well
         */
        Collections.reverse(values);
        for (String val : values) {
            /* replace named params with ?'s */
            query = query.replaceAll(":" + val, "?");
        }
        return query;
    }

    public String getSql() {
        return sql;
    }

    private List<String> extractParamNames(String query, Set<String> queryParams) {
        List<String> paramNames = new ArrayList<String>();
        String tmpParam;
        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == '?') {
                paramNames.add("?");
            } else if (query.charAt(i) == ':') {
                /* check if the string is at the end */
                if (i + 1 < query.length()) {
                    /*
                     * split params in situations like ":a,:b", ":a :b", ":a:b",
                     * "(:a,:b)"
                     */
                    tmpParam = query.substring(i + 1, query.length()).split(" |,|\\)|\\(|:")[0];
                    if (queryParams.contains(tmpParam)) {
                        /*
                         * only consider this as a parameter if it's in input
                         * mappings
                         */
                        paramNames.add(tmpParam);
                    }
                }
            }
        }
        return paramNames;
    }

    private void processNamedParams() {
        Map<String, QueryParam> paramMap = new HashMap<String, QueryParam>();
        for (QueryParam param : this.getQueryParams()) {
            paramMap.put(param.getName(), param);
        }
        List<String> paramNames = this.extractParamNames(this.getQuery(), paramMap.keySet());
        this.namedParamNames = new ArrayList<String>();
        QueryParam tmpParam;
        String tmpParamName;
        int tmpOrdinal;
        Set<String> checkedQueryParams = new HashSet<String>();
        Set<Integer> processedOrdinalsForNamedParams = new HashSet<Integer>();
        for (int i = 0; i < paramNames.size(); i++) {
            if (!paramNames.get(i).equals("?")) {
                tmpParamName = paramNames.get(i);
                tmpParam = paramMap.get(tmpParamName);
                if (tmpParam != null) {
                    if (!checkedQueryParams.contains(tmpParamName)) {
                        tmpParam.clearOrdinals();
                        checkedQueryParams.add(tmpParamName);
                    }
                    this.namedParamNames.add(tmpParamName);
                    /* ordinals of named params */
                    tmpOrdinal = i + 1;
                    tmpParam.addOrdinal(tmpOrdinal);
                    processedOrdinalsForNamedParams.add(tmpOrdinal);
                }
            }
        }
        this.cleanupProcessedNamedParams(checkedQueryParams, processedOrdinalsForNamedParams,
                paramMap);
    }

    /**
     * This method is used to clean up the ordinal in the named paramter
     * scenario, where the SQL may not have all the params as named parameters,
     * so other non-named parameters ordinals may clash with the processed one.
     */
    private void cleanupProcessedNamedParams(Set<String> checkedQueryParams,
            Set<Integer> processedOrdinalsForNamedParams, Map<String, QueryParam> paramMap) {
        QueryParam tmpQueryParam;
        for (String paramName : paramMap.keySet()) {
            if (!checkedQueryParams.contains(paramName)) {
                tmpQueryParam = paramMap.get(paramName);
                /* unchecked query param can only have one ordinal */
                if (processedOrdinalsForNamedParams.contains(tmpQueryParam.getOrdinal())) {
                    /* set to a value that will not clash with valid ordinals */
                    tmpQueryParam.setOrdinal(0);
                }
            }
        }
    }

    public boolean hasOutParams() {
        return hasOutParams;
    }

    public boolean isResultOnlyOutParams() {
        return resultOnlyOutParams;
    }

    private List<QueryParam> extractOutQueryParams(List<QueryParam> queryParams) {
        List<QueryParam> inOutQueryParams = new ArrayList<QueryParam>();
        for (QueryParam queryParam : queryParams) {
            if (queryParam.getType().endsWith(QueryTypes.OUT)
                    && !queryParam.getSqlType().equals(DBConstants.DataTypes.ORACLE_REF_CURSOR)) {
                inOutQueryParams.add(queryParam);
            }
        }
        return inOutQueryParams;
    }

    public List<QueryParam> getOutQueryParams() {
        return outQueryParams;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getQueryType() {
        return queryType;
    }

    public SQLConfig getConfig() {
        return config;
    }

    private boolean isValidCreds(String[] creds) {
        return (creds != null && creds.length > 1 && creds[0] != null);
    }

    public String[] lookupConnectionCredentials() throws DataServiceFault {
        if (this.getConfig().getPrimaryDynAuth() != null) {
            String user = DBUtils.getCurrentContextUsername();
            String[] creds = this.getConfig().getPrimaryDynAuth().lookupCredentials(user);
            if (this.isValidCreds(creds)) {
                return creds;
            } else {
                if (this.getConfig().getSecondaryDynAuth() != null) {
                    creds = this.getConfig().getSecondaryDynAuth().lookupCredentials(user);
                    if (this.isValidCreds(creds)) {
                        return creds;
                    }
                }
                creds = this.getConfig().getPrimaryDynAuth()
                        .lookupCredentials(RDBMS.USERNAME_WILDCARD);
                if (this.isValidCreds(creds)) {
                    return creds;
                } else {
                    throw new DataServiceFault(
                            "A username/password mapping does not exist for the "
                                    + "request user: " + user);
                }
            }
        } else {
            return new String[] { null, null };
        }
    }

    /**
     * Creates a new connection and return it.
     * 
     * @see Connection
     */
    private Connection createConnection() throws DataServiceFault {
        try {
            String[] creds = this.lookupConnectionCredentials();
            Connection connection;
            DataServiceConnection dsCon = TLConnectionStore.getConnection(this.getConfigId(), creds[0]);
            if (dsCon == null) {
                connection = this.getConfig().createConnection(creds[0], creds[1]);
                dsCon = new SQLDataServicesConnection(connection);
                TLConnectionStore.addConnection(this.getConfigId(), creds[0], dsCon);
            } else {
                connection = ((SQLDataServicesConnection) dsCon).getJDBCConnection();
            }
            if (DispatchStatus.isInBatchBoxcarring() && !dsCon.isXA()) {
                /* disable autocommit, and add to the connection list */
                setAutoCommit(connection, false);
            } else if (!dsCon.isXA()) {
                /* for normal operations */
                switch (this.getAutoCommit()) {
                case AUTO_COMMIT_ON:
                    setAutoCommit(connection, true);
                    break;
                case AUTO_COMMIT_OFF:
                    setAutoCommit(connection, false);
                    break;
                default:
                    break;
                }     
            }
            return connection;
        } catch (SQLException e) {
            throw new DataServiceFault(e, "Error in opening DBMS connection.");
        }
    }

    private int retrieveQueryType(String query) {
        if (this.isForceStoredProc()) {
            return SQLQuery.DS_QUERY_TYPE_STORED_PROC;
        } else {
            return inferQueryType(query);
        }
    }

    private int inferQueryType(String query) {
        query = query.trim().toUpperCase();
        /* check if this query starts with SELECT, INSERT etc.. */
        for (String normalQueryType : DBConstants.SQL_NORMAL_QUERY_TYPES) {
            if (query.startsWith(normalQueryType)) {
                return SQLQuery.DS_QUERY_TYPE_NORMAL;
            }
        }
        /* else, this has to be a stored procedure */
        return SQLQuery.DS_QUERY_TYPE_STORED_PROC;
    }

    private boolean isJDBCBatchRequest() {
        return (DispatchStatus.isBatchRequest() && this.hasBatchQuerySupport());
    }

    private boolean isJDBCFirstBatchRequest() {
        return (this.isJDBCBatchRequest() && DispatchStatus.getBatchRequestNumber() == 0);
    }

    private boolean isJDBCLastBatchRequest() {
        return (this.isJDBCBatchRequest() && (DispatchStatus.getBatchRequestNumber() + 1 >= DispatchStatus
                .getBatchRequestCount()));
    }

    private void writeOutGeneratedKeys(Statement stmt, XMLStreamWriter xmlWriter,
            InternalParamCollection params, int queryLevel) throws DataServiceFault, SQLException {
        ResultSet krs = stmt.getGeneratedKeys();
        DataEntry dataEntry;
        while (krs.next()) {
            dataEntry = this.getDataEntryFromRS(new ResultSetWrapper(krs));
            this.writeResultEntry(xmlWriter, dataEntry, params, queryLevel);
        }
    }

    private void processNormalQuery(XMLStreamWriter xmlWriter, InternalParamCollection params,
            int queryLevel) throws DataServiceFault {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isError = false;
        boolean initial = Query.isQueryPreprocessInitial();
        boolean execute = false;
        try {
            conn = (Connection) Query.getAndRemoveQueryPreprocessObject("conn");
            stmt = (PreparedStatement) Query.getAndRemoveQueryPreprocessObject("stmt");
            if (conn == null) {
                execute = true;
                conn = this.createConnection();
                stmt = this.createProcessedPreparedStatement(SQLQuery.DS_QUERY_TYPE_NORMAL, params,
                        conn);
                if (initial) {
                    Query.setQueryPreprocessedObject("conn", conn);
                    Query.setQueryPreprocessedObject("stmt", stmt);
                }
            }
            
            /* check if this is a batch request */
            if (this.isJDBCFirstBatchRequest()) {
                if (execute) {
                    this.setBatchPreparedStatement(stmt);
                    /* add this to cleanup this query after batch request */
                    BatchDataServiceRequest.addParticipant(this);
                }
            }
            /* if updating/inserting stuff, go inside here */
            if (!this.hasResult() || (this.hasResult() && this.isReturnGeneratedKeys())) {
                /* if we are in the middle of a batch request, don't execute it */
                if (this.isJDBCBatchRequest()) {
                    /* if this is the last one, execute the full batch */
                    if (this.isJDBCLastBatchRequest()) {
                        if (execute) {
                            stmt.executeBatch();
                        }
                        if (this.isReturnGeneratedKeys()) {
                            /* handle generated keys, i.e. SQL INSERT etc.. */
                            if (!initial) {
                                this.writeOutGeneratedKeys(stmt, xmlWriter, params, queryLevel);
                            }
                        }
                    }
                } else {
                    /* normal update operation */
                    if (execute) {
                        stmt.executeUpdate();
                    }
                    if (this.isReturnGeneratedKeys()) {
                        if (!initial) {
                            this.writeOutGeneratedKeys(stmt, xmlWriter, params, queryLevel);
                        }
                    }
                }
            } else {
                rs = (ResultSet) Query.getAndRemoveQueryPreprocessObject("rs");
                if (initial) {
                    rs = stmt.executeQuery();
                    Query.setQueryPreprocessedObject("rs", rs);
                }
                if (!initial) {
                    DataEntry dataEntry;
                    while (rs.next()) {
                        dataEntry = this.getDataEntryFromRS(new ResultSetWrapper(rs));
                        this.writeResultEntry(xmlWriter, dataEntry, params, queryLevel);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            isError = true;
            throw new DataServiceFault(e, FaultCodes.DATABASE_ERROR,
                    "Error in 'SQLQuery.processNormalQuery'");
        } finally {
            if (!initial || isError) {
                this.releaseResources(rs, this.isStatementClosable(isError) ? stmt : null);
            }
        }
    }

    private boolean isRSClosed(ResultSet rs) throws SQLException {
        try {
            return rs.isClosed();
        } catch (SQLException e) {
            throw e;
        } catch (Throwable e) {
            /*
             * in case they throw a method not found exception for the JDBC
             * driver not supporting v4.0 features
             */
            return false;
        }
    }

    private void processStoredProcQuery(XMLStreamWriter xmlWriter, InternalParamCollection params,
            int queryLevel) throws DataServiceFault {
        Connection conn = null;
        boolean isError = false;
        CallableStatement stmt = null;
        ResultSet rs = null;
        boolean initial = Query.isQueryPreprocessInitial();
        boolean execute = false;
        try {
            conn = (Connection) Query.getAndRemoveQueryPreprocessObject("conn");
            stmt = (CallableStatement) Query.getAndRemoveQueryPreprocessObject("stmt");
            if (conn == null) {
                execute = true;
                conn = this.createConnection();
                stmt = (CallableStatement) this.createProcessedPreparedStatement(
                        SQLQuery.DS_QUERY_TYPE_STORED_PROC, params, conn);
                if (initial) {
                    Query.setQueryPreprocessedObject("conn", conn);
                    Query.setQueryPreprocessedObject("stmt", stmt);
                }
            }
            /* check if this is a batch request */
            if (this.isJDBCFirstBatchRequest()) {
                if (execute) {
                    this.setBatchPreparedStatement(stmt);
                    /* add this to cleanup this query after batch request */
                    BatchDataServiceRequest.addParticipant(this);
                }
            }
            if (!this.hasResult() || (this.hasResult() && this.isReturnGeneratedKeys())) {
                /* if we are in the middle of a batch request, don't execute it */
                if (this.isJDBCBatchRequest()) {
                    /* if this is the last one, execute the full batch */
                    if (this.isJDBCLastBatchRequest()) {
                        if (execute) {
                            stmt.executeBatch();
                        }
                        if (this.isReturnGeneratedKeys()) {
                            if (!initial) {
                                this.writeOutGeneratedKeys(stmt, xmlWriter, params, queryLevel);
                            }
                        }
                    }
                } else {
                    if (execute) {
                        stmt.executeUpdate();
                    }
                    if (this.isReturnGeneratedKeys()) {
                        if (!initial) {
                            this.writeOutGeneratedKeys(stmt, xmlWriter, params, queryLevel);
                        }
                    }
                }
            } else {
                /*
                 * check if all the result elements are out params; if so, no
                 * result set
                 */
                if (this.isResultOnlyOutParams()) {
                    if (execute) {
                        stmt.execute();
                    }
                    /* if there's a ref cursor, get the result set */
                    if (this.hasRefCursor()) {
                        rs = (ResultSet) Query.getAndRemoveQueryPreprocessObject("rs");
                        if (initial) {
                            rs = (ResultSet) stmt.getObject(this.getCurrentRefCursor().getOrdinal());
                            Query.setQueryPreprocessedObject("rs", rs);
                        }
                    }
                } else {
                    rs = (ResultSet) Query.getAndRemoveQueryPreprocessObject("rs");
                    if (initial) {
                        rs = this.getFirstRSOfStoredProc(stmt);
                        Query.setQueryPreprocessedObject("rs", rs);
                    }
                }

                if (!initial) {
                    if (rs == null || this.isRSClosed(rs) || !rs.next()) {
                        if (this.hasOutParams()) {
                            DataEntry outParamDataEntry = this.getDataEntryFromOutParams(stmt);
                            if (outParamDataEntry != null) {
                                this.writeResultEntry(xmlWriter, outParamDataEntry, params, queryLevel);
                            }
                        }
                    } else {
                        if (this.hasOutParams()) {
                            /*
                             * so if someone mixes up OUT parameters with normal
                             * results, this will effectively turn off streaming
                             */
                            if (!initial) {
                                List<DataEntry> entries = this.getAllDataEntriesFromRS(rs, true);
                                /*
                                 * result sets must be processed before extracting out
                                 * params
                                 */
                                DataEntry outParamDataEntry = this.getDataEntryFromOutParams(stmt);
                                for (DataEntry dataEntry : entries) {
                                    this.mergeDataEntries(dataEntry, outParamDataEntry);
                                    this.writeResultEntry(xmlWriter, dataEntry, params, queryLevel);
                                }
                            }
                        } else {
                            /*
                             * do-while loop since, 'rs.next()' has already been
                             * called once
                             */
                            DataEntry dataEntry;
                            do {
                                dataEntry = this.getDataEntryFromRS(new ResultSetWrapper(rs));
                                this.writeResultEntry(xmlWriter, dataEntry, params, queryLevel);
                            } while (rs.next());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            isError = true;
            throw new DataServiceFault(e, FaultCodes.DATABASE_ERROR,
                    "Error in 'SQLQuery.processStoredProcQuery'");
        } finally {
            if (!initial || isError) {
                this.releaseResources(rs, this.isStatementClosable(isError) ? stmt : null);                
            }
        }
    }

    private boolean isStatementClosable(boolean isError) {
        return (isError || !this.isJDBCBatchRequest() || this.isJDBCLastBatchRequest());
    }

    private void releaseResources(ResultSet rs, Statement stmt) {
        /* close the result set */
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception ignore) {
                // ignore
            }
        }
        /* close the statement */
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception ignore) {
                // ignore
            }
        }
    }

    private List<DataEntry> getAllDataEntriesFromRS(ResultSet rs, boolean rsNextAlreadyCalled)
            throws SQLException {
        List<DataEntry> entries = new ArrayList<DataEntry>();
        if (!rsNextAlreadyCalled) {
            if (!rs.next()) {
                return entries;
            }
        }
        do {
            entries.add(this.getDataEntryFromRS(new ResultSetWrapper(rs)));
        } while (rs.next());
        return entries;
    }

    private ResultSet getFirstRSOfStoredProc(CallableStatement stmt) throws SQLException {
        boolean resultAndNoUpdateCount = stmt.execute();
        ResultSet result = null;
        while (true) {
            if (!resultAndNoUpdateCount) {
                if (stmt.getUpdateCount() == -1) {
                    break;
                }
            } else {
                result = stmt.getResultSet();
                break;
            }
            try {
                resultAndNoUpdateCount = stmt.getMoreResults(Statement.KEEP_CURRENT_RESULT);
            } catch (SQLException e) {
                /*
                 * for some DBMS, this will throw an unsupported feature
                 * exception, even when after a valid result set is retrieved
                 * first, so if there's a valid result set existing, we will
                 * ignore the eventual unsupported feature exception
                 */
                if (result == null) {
                    throw e;
                }
                break;
            }
        }
        return result;
    }

    /*
     * merge data entry objects from rhs -> lhs.
     */

    private void mergeDataEntries(DataEntry lhs, DataEntry rhs) {
        lhs.getData().putAll(rhs.getData());
    }

    private DataEntry getDataEntryFromOutParams(CallableStatement stmt) throws DataServiceFault {
        DataEntry dataEntry = new DataEntry();
        String name;
        ParamValue value;
        for (QueryParam queryParam : this.getOutQueryParams()) {
            name = queryParam.getName();
            value = this.getOutparameterValue(stmt, queryParam.getSqlType(),
                    queryParam.getOrdinal());
            dataEntry.addValue(name, value);
        }
        return dataEntry;
    }

    private DataEntry getDataEntryFromRS(ResultSet rs) throws SQLException {
        DataEntry dataEntry = new DataEntry();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        int columnType;
        String value;
        ParamValue paramValue;
        Time sqlTime;
        Date sqlDate;
        Timestamp sqlTimestamp;
        Blob sqlBlob;
        BigDecimal bigDecimal;
        InputStream binInStream;
        boolean useColumnNumbers = this.isUsingColumnNumbers();
        for (int i = 1; i <= columnCount; i++) {
            /* retrieve values according to the column type */
            columnType = metaData.getColumnType(i);
            switch (columnType) {
            /* handle string types */
            case Types.VARCHAR:
                /* fall through */
            case Types.LONGVARCHAR:
                /* fall through */
            case Types.CHAR:
                /* fall through */
            case Types.CLOB:
                /* fall through */
            case Types.NCHAR:
                /* fall through */
            case Types.NCLOB:
                /* fall through */
            case Types.NVARCHAR:
                /* fall through */
            case Types.LONGNVARCHAR:
                value = rs.getString(i);
                paramValue = new ParamValue(value);
                break;
            /* handle numbers */
            case Types.INTEGER:
                /* fall through */
            case Types.TINYINT:
                /* fall through */
            case Types.SMALLINT:
                value = ConverterUtil.convertToString(rs.getInt(i));
                paramValue = new ParamValue(value);
                break;
            case Types.DOUBLE:
                value = ConverterUtil.convertToString(rs.getDouble(i));
                paramValue = new ParamValue(value);
                break;
            case Types.FLOAT:
                value = ConverterUtil.convertToString(rs.getFloat(i));
                paramValue = new ParamValue(value);
                break;
            case Types.BOOLEAN:
                /* fall through */
            case Types.BIT:
                value = ConverterUtil.convertToString(rs.getBoolean(i));
                paramValue = new ParamValue(value);
                break;
            case Types.DECIMAL:
                bigDecimal = rs.getBigDecimal(i);
                if (bigDecimal != null) {
                    value = ConverterUtil.convertToString(bigDecimal);
                } else {
                    value = null;
                }
                paramValue = new ParamValue(value);
                break;
            /* handle data/time values */
            case Types.TIME:
                /* handle time data type */
                sqlTime = rs.getTime(i);
                if (sqlTime != null) {
                    value = this.convertToTimeString(sqlTime);
                } else {
                    value = null;
                }
                paramValue = new ParamValue(value);
                break;
            case Types.DATE:
                /* handle date data type */
                sqlDate = rs.getDate(i);
                if (sqlDate != null) {
                    value = ConverterUtil.convertToString(sqlDate);
                } else {
                    value = null;
                }
                paramValue = new ParamValue(value);
                break;
            case Types.TIMESTAMP:
                sqlTimestamp = rs.getTimestamp(i);
                if (sqlTimestamp != null) {
                    value = this.convertToTimestampString(sqlTimestamp);
                } else {
                    value = null;
                }
                paramValue = new ParamValue(value);
                break;
            /* handle binary types */
            case Types.BLOB:
                sqlBlob = rs.getBlob(i);
                if (sqlBlob != null) {
                    value = this.getBase64StringFromInputStream(sqlBlob.getBinaryStream());
                } else {
                    value = null;
                }
                paramValue = new ParamValue(value);
                break;
            case Types.BINARY:
                /* fall through */
            case Types.LONGVARBINARY:
                /* fall through */
            case Types.VARBINARY:
                binInStream = rs.getBinaryStream(i);
                if (binInStream != null) {
                    value = this.getBase64StringFromInputStream(binInStream);
                } else {
                    value = null;
                }
                paramValue = new ParamValue(value);
                break;
            /* handling User Defined Types */
            case Types.STRUCT:
                Struct udt = (Struct) rs.getObject(i);
                paramValue = new ParamValue(udt);
                break;
            case Types.ARRAY:
                paramValue = new ParamValue(ParamValue.PARAM_VALUE_ARRAY);
                Array dataArray = (Array) rs.getObject(i);
                if (dataArray == null) {
                    break;
                }
                paramValue = this.processSQLArray(dataArray, paramValue);
                break;
            case Types.NUMERIC:
                bigDecimal = rs.getBigDecimal(i);
                if (bigDecimal != null) {
                    value = ConverterUtil.convertToString(bigDecimal);
                } else {
                    value = null;
                }
                paramValue = new ParamValue(value);
                break;
            case Types.BIGINT:
                value = ConverterUtil.convertToString(rs.getLong(i));
                paramValue = new ParamValue(value);
                break;

            /* handle all other types as strings */
            default:
                value = rs.getString(i);
                paramValue = new ParamValue(value);
                break;
            }
            dataEntry.addValue(useColumnNumbers ? Integer.toString(i) : metaData.getColumnLabel(i),
                    paramValue);
        }
        return dataEntry;
    }

    /**
     * Processes a SQL Array instance and transform it into a ParamValue
     * instance
     * 
     * @param dataArray
     *            SQLArray instance
     * @param paramValue
     *            Container into which the SQLArray elements should be populated
     * @return ParamValue instance containing all the elements of the
     *         corresponding SQLArray instance
     * @throws SQLException
     *             When it fails to processes the result set produced by the
     *             SQLArray instance
     */
    private ParamValue processSQLArray(Array dataArray, ParamValue paramValue) throws SQLException {
        ResultSet rs = null;
        try {
            rs = dataArray.getResultSet();
            while (rs.next()) {
                Object arrayEl = rs.getObject(2);
                if (arrayEl instanceof Struct) {
                    paramValue.getArrayValue().add(new ParamValue((Struct) arrayEl));
                } else if (arrayEl instanceof Array) {
                    paramValue.getArrayValue().add(
                            processSQLArray((Array) arrayEl, new ParamValue(
                                    ParamValue.PARAM_VALUE_ARRAY)));
                } else {
                    paramValue.getArrayValue().add(new ParamValue(String.valueOf(arrayEl)));
                }
            }
            return paramValue;
        } finally {
            this.releaseResources(rs, null);
        }
    }

    private String convertToTimeString(Time sqlTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(sqlTime.getTime());
        return new org.apache.axis2.databinding.types.Time(cal).toString();
    }

    private String convertToTimestampString(Timestamp sqlTimestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(sqlTimestamp.getTime());
        return ConverterUtil.convertToString(cal);
    }

    private String getBase64StringFromInputStream(InputStream in) throws SQLException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        String strData;
        try {
            byte[] buff = new byte[512];
            int i = 0;
            while ((i = in.read(buff)) > 0) {
                byteOut.write(buff, 0, i);
            }
            in.close();
            byte[] base64Data = Base64.encodeBase64(byteOut.toByteArray());
            strData = new String(base64Data, DBConstants.DEFAULT_CHAR_SET_TYPE);
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
        return strData;
    }

    private byte[] getBytesFromBase64String(String base64Str) throws SQLException {
        try {
            byte[] data = Base64
                    .decodeBase64(base64Str.getBytes(DBConstants.DEFAULT_CHAR_SET_TYPE));
            return data;
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }

    private Integer[] extractSQLParamIndices(String sql) {
        List<Integer> result = new ArrayList<Integer>();
        char[] data = sql.toCharArray();
        for (int i = 0; i < data.length; i++) {
            if (data[i] == '?') {
                result.add(i);
            }
        }
        return result.toArray(new Integer[result.size()]);
    }

    /**
     * Modifies the SQL to include the direct value of the parameters of type
     * "QUERY_STRING"; The SQL will be recreated and the other parameters will
     * be re-organized to point to correct ordinal values.
     * 
     * @return [0] The updated SQL, [1] The updated parameter count
     */
    private Object[] processDynamicQuery(String sql, InternalParamCollection params,
            int paramCount) {
        Integer[] paramIndices = this.extractSQLParamIndices(sql);
        int currentOrdinalDiff = 0;
        int currentParamIndexDiff = 0;
        InternalParam tmpParam;
        int paramIndex;
        String tmpValue;
        int resultParamCount = paramCount;
        for (int i = 1; i <= paramCount; i++) {
            tmpParam = params.getParam(i);
            if (DataTypes.QUERY_STRING.equals(tmpParam.getSqlType())) {
                paramIndex = paramIndices[i - 1] + currentParamIndexDiff;
                tmpValue = params.getParam(i).getValue().getScalarValue();
                currentParamIndexDiff += tmpValue.length() - 1;
                if (paramIndex + 1 < sql.length()) {
                    sql = sql.substring(0, paramIndex) + tmpValue + sql.substring(paramIndex + 1);
                } else {
                    sql = sql.substring(0, paramIndex) + tmpValue;
                }
                params.remove(i);
                currentOrdinalDiff++;
                resultParamCount--;
            } else {
                params.remove(i);
                tmpParam.setOrdinal(i - currentOrdinalDiff);
                params.addParam(tmpParam);
            }
        }
        return new Object[] { sql, resultParamCount };
    }

    /**
     * Returns the SQL manipulated to suite the given parameters, e.g. adding
     * additional "?"'s for array types.
     */
    public String createProcessedSql(String sql, InternalParamCollection params, int paramCount) {
        String currentSql = sql;
        int start = 0;
        Object[] vals;
        InternalParam param;
        ParamValue value;
        int count;
        for (int i = 1; i <= paramCount; i++) {
            param = params.getParam(i);
            value = param.getValue();
            /*
             * value can be null in stored proc OUT params, so it is simply
             * treated as a single param, because the number of elements in an
             * array cannot be calculated, since there's no actual value passed
             * in
             */
            if (value != null && (value.getValueType() == ParamValue.PARAM_VALUE_ARRAY)) {
                count = (value.getArrayValue()).size();
                if (this.hasRefCursor()) {
                    int currentRefCursorOrdinal = this.getCurrentRefCursor().getOrdinal();
                    if (i < currentRefCursorOrdinal) {
                        this.getCurrentRefCursor().setOrdinal(currentRefCursorOrdinal + count - 1);
                    }
                }
            } else {
                count = 1;
            }
            vals = this.expandSQL(start, count, currentSql);
            start = (Integer) vals[0];
            currentSql = (String) vals[1];
        }
        return currentSql;
    }

    /**
     * Given the starting position, this method searches for the first occurence
     * of "?" and replace it with `count` "?"'s. Returns [0] - end position of
     * "?"'s, [1] - modified sql.
     */
    private Object[] expandSQL(int start, int count, String sql) {
        StringBuilder result = new StringBuilder();
        int n = sql.length();
        int end = n;
        for (int i = start; i < n; i++) {
            if (sql.charAt(i) == '?') {
                result.append(sql.substring(0, i));
                result.append(this.generateQuestionMarks(count));
                end = result.length() + 1;
                if (i + 1 < n) {
                    result.append(sql.substring(i + 1));
                }
                break;
            }
        }
        return new Object[] { end, result.toString() };
    }

    private String generateQuestionMarks(int n) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            builder.append("?");
            if (i + 1 < n) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    private int calculateParamCount(String sql) {
        int n = 0;
        for (char ch : sql.toCharArray()) {
            if (ch == '?') {
                n++;
            }
        }
        return n;
    }

    private PreparedStatement createProcessedPreparedStatement(int queryType,
            InternalParamCollection params, Connection conn) throws DataServiceFault {
        try {
            /*
             * lets see first if there's already a batch prepared statement
             * created
             */
            boolean inTheMiddleOfABatch = false;
            PreparedStatement stmt = this.getBatchPreparedStatement();
            int currentParamCount = this.getParamCount();

            /* create a new prepared statement */
            if (stmt == null) {
                /* batch mode is not supported for dynamic queries */
                Object[] result = this.processDynamicQuery(this.getSql(), params,
                        this.getParamCount());
                String dynamicSQL = (String) result[0];
                currentParamCount = (Integer) result[1];
                String processedSQL = this
                        .createProcessedSql(dynamicSQL, params, currentParamCount);
                if (queryType == SQLQuery.DS_QUERY_TYPE_NORMAL) {
                    if (this.isReturnGeneratedKeys()) {
                        if (this.getKeyColumns() != null) {
                            stmt = conn.prepareStatement(processedSQL, this.getKeyColumns());
                        } else {
                            stmt = conn.prepareStatement(processedSQL,
                                    Statement.RETURN_GENERATED_KEYS);
                        }
                    } else {
                        stmt = conn.prepareStatement(processedSQL);
                    }
                } else if (queryType == SQLQuery.DS_QUERY_TYPE_STORED_PROC) {
                    stmt = conn.prepareCall(processedSQL);
                } else {
                    throw new DataServiceFault("Unsupported query type: " + queryType);
                }
            } else {
                inTheMiddleOfABatch = true;
            }

            if (!inTheMiddleOfABatch) {
                /* set query timeout */
                if (this.isHasQueryTimeout()) {
                    stmt.setQueryTimeout(this.getQueryTimeout());
                }
                /* adding the try catch to avoid setting this for jdbc drivers that do not implement this method. */
                try {
                    /* set fetch direction */
                    if (this.isHasFetchDirection()) {
                        stmt.setFetchDirection(this.getFetchDirection());
                    }
                    /* set fetch size - user's setting */
                    if (this.isHasFetchSize()) {
                        stmt.setFetchSize(this.getFetchSize());
                    } else {
                        /*
                         * stream data by sections - avoid the full result set
                         * to be loaded to memory, and only stream if there
                         * aren't any OUT parameters, MySQL fails in the
                         * scenario of streaming and OUT parameters, so the
                         * possibility is there for other DBMSs
                         */
                        if (!this.hasOutParams()) {
                            stmt.setFetchSize(this.getOptimalRSFetchSize());
                        }
                    }
                } catch (Throwable e) {
                    log.debug("Exception while setting fetch size: " + e.getMessage(), e);
                }
                /* set max field size */
                if (this.isHasMaxFieldSize()) {
                    stmt.setMaxFieldSize(this.getMaxFieldSize());
                }
                /* set max rows */
                if (this.isHasMaxRows()) {
                    stmt.setMaxRows(this.getMaxRows());
                }
            }

            int currentOrdinal = 0;
            InternalParam param;
            ParamValue value;
            for (int i = 1; i <= currentParamCount; i++) {
                param = params.getParam(i);
                value = param.getValue();
                /*
                 * handle array values, if value is null, this param has to be
                 * an OUT param
                 */
                if (value != null && value.getValueType() == ParamValue.PARAM_VALUE_ARRAY) {
                    for (ParamValue arrayElement : value.getArrayValue()) {
                        this.setParamInPreparedStatement(stmt, param, arrayElement.toString(),
                                queryType, currentOrdinal);
                        currentOrdinal++;
                    }
                } else { /* scalar value */
                    this.setParamInPreparedStatement(stmt, param,
                            value != null ? value.getScalarValue() : null, queryType,
                            currentOrdinal);
                    currentOrdinal++;
                }
            }

            /* if we are in JDBC batch processing mode, batch it! */
            if (this.isJDBCBatchRequest()) {
                stmt.addBatch();
            }

            return stmt;
        } catch (SQLException e) {
            throw new DataServiceFault(e, "Error in 'createProcessedPreparedStatement'");
        }
    }

    private void setParamInPreparedStatement(PreparedStatement stmt, InternalParam param,
            String value, int queryType, int index) throws SQLException, DataServiceFault {
        String paramName = param.getName();
        String sqlType = param.getSqlType();
        String paramType = param.getType();
        String structType = param.getStructType();
        if (sqlType == null) {
            /* defaults to string */
            setDefaultStringValue(value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.INTEGER.equals(sqlType)) {
            setIntValue(queryType, value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.STRING.equals(sqlType) || 
                DBConstants.DataTypes.UUID.equals(sqlType) || 
                DBConstants.DataTypes.INETADDRESS.equals(sqlType)) {
            setStringValue(queryType, value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.DOUBLE.equals(sqlType)) {
            setDoubleValue(queryType, value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.NUMERIC.equals(sqlType)) {
            setNumericValue(queryType, value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.BIT.equals(sqlType)
                || DBConstants.DataTypes.BOOLEAN.equals(sqlType)) {
            setBitValue(queryType, value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.TINYINT.equals(sqlType)) {
            setTinyIntValue(queryType, value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.SMALLINT.equals(sqlType)) {
            setSmallIntValue(queryType, value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.BIGINT.equals(sqlType) || 
                DBConstants.DataTypes.VARINT.equals(sqlType)) {
            setBigIntValue(queryType, value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.REAL.equals(sqlType)) {
            setRealValue(queryType, value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.DATE.equals(sqlType)) {
            setDateValue(queryType, paramName, value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.TIMESTAMP.equals(sqlType)) {
            setTimestampValue(queryType, paramName, value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.TIME.equals(sqlType)) {
            setTimeValue(queryType, paramName, value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.BINARY.equals(sqlType)) {
            setBinaryValue(queryType, paramName, value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.BLOB.equals(sqlType)) {
            setBlobValue(queryType, paramName, value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.CLOB.equals(sqlType)) {
           setClobValue(queryType, paramName, value, paramType, stmt, index);
        } else if (DBConstants.DataTypes.ORACLE_REF_CURSOR.equals(sqlType)) {
            setOracleRefCusor(stmt, index);
        } else if (DBConstants.DataTypes.STRUCT.equals(sqlType)) {
            setUserDefinedType(stmt, index, paramType, structType);
        } else if (DBConstants.DataTypes.ARRAY.equals(sqlType)) {
            setArrayValue(stmt, index, paramType, structType);
        } else {
            throw new DataServiceFault("[" + this.getDataService().getName()
                    + "]  Found Unsupported data type : " + sqlType + " as input parameter.");
        }
    }

    private void setClobValue(int queryType, String paramName,
                           String value, String paramType, PreparedStatement sqlQuery, int i)
         throws SQLException, DataServiceFault {
         if ("IN".equals(paramType)) {
             if (value == null) {
                 sqlQuery.setNull(i + 1, java.sql.Types.CLOB);
             } else {
                 sqlQuery.setClob(i + 1, new BufferedReader(new StringReader(value)),
                                        value.length());
             }
         } else if ("INOUT".equals(paramType)) {
             if (value == null) {
                 ((CallableStatement) sqlQuery).setNull(i + 1,
                                        java.sql.Types.CLOB);
             } else {
                 ((CallableStatement) sqlQuery).setClob(i + 1,
                                        new BufferedReader(new StringReader(value)), value.length());
             }
             ((CallableStatement) sqlQuery).registerOutParameter(i + 1,
                                java.sql.Types.CLOB);
         } else {
             ((CallableStatement) sqlQuery).registerOutParameter(i + 1,
                                java.sql.Types.CLOB);
         }
    }

    private void setArrayValue(PreparedStatement sqlQuery, int i, String paramType,
            String structType) throws SQLException, DataServiceFault {
        if (QueryTypes.OUT.equals(paramType)) {
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, Types.ARRAY, structType);
        } else {
            throw new DataServiceFault("IN or INOUT operations are not supported for SQL Arrays");
        }
    }

    private void setDefaultStringValue(String value, String paramType, PreparedStatement sqlQuery,
            int i) throws SQLException {
        if (QueryTypes.IN.equals(paramType)) {
            sqlQuery.setString(i + 1, value);
        } else if (QueryTypes.INOUT.equals(paramType)) {
            sqlQuery.setString(i + 1, value);
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.VARCHAR);
        } else {
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.VARCHAR);
        }
    }

    private void setTimeValue(int queryType, String paramName, String value, String paramType,
            PreparedStatement sqlQuery, int i) throws SQLException, DataServiceFault {
        Time time = null;
        try {
            if (value != null) {
                time = DBUtils.getTime(value);
            }
        } catch (ParseException e) {
            throw new DataServiceFault(e, "Incorrect Time format for parameter : " + paramName
                    + ". Time should be in the format hh:mm:ss");
        }
        if ("IN".equals(paramType)) {
            if (queryType == SQLQuery.DS_QUERY_TYPE_NORMAL) {
                if (value == null) {
                    sqlQuery.setNull(i + 1, java.sql.Types.TIME);
                } else {
                    sqlQuery.setTime(i + 1, time);
                }
            } else {
                if (value == null) {
                    ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.TIME);
                } else {
                    ((CallableStatement) sqlQuery).setTime(i + 1, time);
                }
            }
        } else if ("INOUT".equals(paramType)) {
            if (value == null) {
                ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.TIME);
            } else {
                ((CallableStatement) sqlQuery).setTime(i + 1, time);
            }
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.TIME);
        } else {
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.TIME);
        }
    }

    private void setBinaryValue(int queryType, String paramName, String value, String paramType,
            PreparedStatement sqlQuery, int i) throws SQLException, DataServiceFault {
        if ("IN".equals(paramType)) {
            if (value == null) {
                sqlQuery.setNull(i + 1, java.sql.Types.BINARY);
            } else {
                byte[] data = this.getBytesFromBase64String(value);
                sqlQuery.setBinaryStream(i + 1, new ByteArrayInputStream(data), data.length);
            }
        } else if ("INOUT".equals(paramType)) {
            if (value == null) {
                ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.BINARY);
            } else {
                byte[] data = this.getBytesFromBase64String(value);
                ((CallableStatement) sqlQuery).setBinaryStream(i + 1,
                        new ByteArrayInputStream(data), data.length);
            }
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.BINARY);
        } else {
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.BINARY);
        }
    }

    private void setBlobValue(int queryType, String paramName, String value, String paramType,
            PreparedStatement sqlQuery, int i) throws SQLException, DataServiceFault {
        if ("IN".equals(paramType)) {
            if (value == null) {
                sqlQuery.setNull(i + 1, java.sql.Types.BLOB);
            } else {
                byte[] data = this.getBytesFromBase64String(value);
                sqlQuery.setBlob(i + 1, new ByteArrayInputStream(data), data.length);
            }
        } else if ("INOUT".equals(paramType)) {
            if (value == null) {
                ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.BLOB);
            } else {
                byte[] data = this.getBytesFromBase64String(value);
                ((CallableStatement) sqlQuery).setBlob(i + 1, new ByteArrayInputStream(data),
                        data.length);
            }
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.BLOB);
        } else {
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.BLOB);
        }
    }

    private void setOracleRefCusor(PreparedStatement sqlQuery, int i) throws SQLException,
            DataServiceFault {
        ((CallableStatement) sqlQuery).registerOutParameter(i + 1, ORACLE_REF_CURSOR_TYPE);
    }

    /**
     * This method sets the parameters to be passed to/from an oracle stored
     * procedure that deals with User Defined Types.
     * 
     * @param sqlQuery
     *            Prepared Statement
     * @param parameterIndex
     *            current parameter index
     * @param structType
     *            Name of the User Defined Type
     * @param paramType
     *            Whether the parameter is IN or OUT
     * @throws SQLException
     *             Throws an SQL Exception
     * @throws DataServiceFault
     *             DataServiceFault
     */
    private void setUserDefinedType(PreparedStatement sqlQuery, int parameterIndex,
            String paramType, String structType) throws SQLException, DataServiceFault {
        if (QueryTypes.OUT.equals(paramType)) {
            ((CallableStatement) sqlQuery).registerOutParameter(parameterIndex + 1, Types.STRUCT,
                    structType.toUpperCase());
        } else {
            throw new DataServiceFault("IN or INOUT operations are not supported for User "
                    + "Defined Types");
        }
    }

    private void setTimestampValue(int queryType, String paramName, String value, String paramType,
            PreparedStatement sqlQuery, int i) throws DataServiceFault, SQLException {
        Timestamp timestamp = null;
        try {
            if (value != null) {
                timestamp = DBUtils.getTimestamp(value);
            }
        } catch (ParseException e) {
            throw new DataServiceFault(e, "Incorrect Timestamp format for parameter : " + paramName
                    + ". Timestamp should be in one of following formats "
                    + "yyyy-MM-dd'T'hh:mm:ss.sss'+'hh:mm, " + "yyyy-MM-dd'T'hh:mm:ss.sss'-'hh:mm, "
                    + "yyyy-MM-dd'T'hh:mm:ss.sss'Z', " + "yyyy-MM-dd hh:mm:ss.SSSSSS or "
                    + "yyyy-MM-dd hh:mm:ss");
        }
        if ("IN".equals(paramType)) {
            if (queryType == SQLQuery.DS_QUERY_TYPE_NORMAL) {
                if (value == null) {
                    sqlQuery.setNull(i + 1, java.sql.Types.TIMESTAMP);
                } else {
                    sqlQuery.setTimestamp(i + 1, timestamp);
                }
            } else {
                if (value == null) {
                    ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.TIMESTAMP);
                } else {
                    ((CallableStatement) sqlQuery).setTimestamp(i + 1, timestamp);
                }
            }
        } else if ("INOUT".equals(paramType)) {
            if (value == null) {
                ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.TIMESTAMP);
            } else {
                ((CallableStatement) sqlQuery).setTimestamp(i + 1, timestamp);
            }
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.TIMESTAMP);
        } else {
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.TIMESTAMP);
        }
    }

    private void setDateValue(int queryType, String paramName, String value, String paramType,
            PreparedStatement sqlQuery, int i) throws SQLException, DataServiceFault {
        Date val = null;
        if (value != null) {
            val = DBUtils.getDate(value);
        }
        try {
            if (QueryTypes.IN.equals(paramType)) {
                if (queryType == SQLQuery.DS_QUERY_TYPE_NORMAL) {
                    if (value == null) {
                        sqlQuery.setNull(i + 1, java.sql.Types.DATE);
                    } else {
                        sqlQuery.setDate(i + 1, val);
                    }
                } else {
                    if (value == null) {
                        ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.DATE);
                    } else {
                        ((CallableStatement) sqlQuery).setDate(i + 1, val);
                    }
                }
            } else if (QueryTypes.INOUT.equals(paramType)) {
                if (value == null) {
                    ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.DATE);
                } else {
                    ((CallableStatement) sqlQuery).setDate(i + 1, val);
                }
                ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.DATE);
            } else {
                ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.DATE);
            }
        } catch (IllegalArgumentException e) {
            throw new DataServiceFault(e, "Incorrect date format for parameter  : " + paramName
                    + ". Date should be in yyyy-mm-dd format.");
        }
    }

    private void setRealValue(int queryType, String value, String paramType,
            PreparedStatement sqlQuery, int i) throws SQLException {
        Float val = null;
        if (value != null) {
            val = new Float(value);
        }
        if (QueryTypes.IN.equals(paramType)) {
            if (queryType == SQLQuery.DS_QUERY_TYPE_NORMAL) {
                if (value == null) {
                    sqlQuery.setNull(i + 1, java.sql.Types.FLOAT);
                } else {
                    sqlQuery.setFloat(i + 1, val);
                }
            } else {
                if (value == null) {
                    ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.FLOAT);
                } else {
                    ((CallableStatement) sqlQuery).setFloat(i + 1, val);
                }
            }
        } else if (QueryTypes.INOUT.equals(paramType)) {
            if (value == null) {
                ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.FLOAT);
            } else {
                ((CallableStatement) sqlQuery).setFloat(i + 1, val);
            }
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.FLOAT);
        } else {
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.FLOAT);
        }
    }

    private void setBigIntValue(int queryType, String value, String paramType,
            PreparedStatement sqlQuery, int i) throws SQLException {
        Long val = null;
        if (value != null) {
            val = new Long(value);
        }
        if (QueryTypes.IN.equals(paramType)) {
            if (queryType == SQLQuery.DS_QUERY_TYPE_NORMAL) {
                if (value == null) {
                    sqlQuery.setNull(i + 1, java.sql.Types.BIGINT);
                } else {
                    sqlQuery.setLong(i + 1, val);
                }
            } else {
                if (value == null) {
                    ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.BIGINT);
                } else {
                    ((CallableStatement) sqlQuery).setLong(i + 1, val);
                }
            }
        } else if (QueryTypes.INOUT.equals(paramType)) {
            if (value == null) {
                ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.BIGINT);
            } else {
                ((CallableStatement) sqlQuery).setLong(i + 1, val);
            }
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.BIGINT);
        } else {
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.BIGINT);
        }
    }

    private void setSmallIntValue(int queryType, String value, String paramType,
            PreparedStatement sqlQuery, int i) throws SQLException {
        Short val = null;
        if (value != null) {
            val = new Short(value);
        }
        if (QueryTypes.IN.equals(paramType)) {
            if (queryType == SQLQuery.DS_QUERY_TYPE_NORMAL) {
                if (value == null) {
                    sqlQuery.setNull(i + 1, java.sql.Types.SMALLINT);
                } else {
                    sqlQuery.setShort(i + 1, val);
                }
            } else {
                if (value == null) {
                    ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.SMALLINT);
                } else {
                    ((CallableStatement) sqlQuery).setShort(i + 1, val);
                }
            }
        } else if (QueryTypes.INOUT.equals(paramType)) {
            if (value == null) {
                ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.SMALLINT);
            } else {
                ((CallableStatement) sqlQuery).setShort(i + 1, val);
            }
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.SMALLINT);
        } else {
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.SMALLINT);
        }
    }

    private void setTinyIntValue(int queryType, String value, String paramType,
            PreparedStatement sqlQuery, int i) throws SQLException {
        Byte val = null;
        if (value != null) {
            val = new Byte(value);
        }
        if (QueryTypes.IN.equals(paramType)) {
            if (queryType == SQLQuery.DS_QUERY_TYPE_NORMAL) {
                if (value == null) {
                    sqlQuery.setNull(i + 1, java.sql.Types.TINYINT);
                } else {
                    sqlQuery.setByte(i + 1, val);
                }
            } else {
                if (value == null) {
                    ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.TINYINT);
                } else {
                    ((CallableStatement) sqlQuery).setByte(i + 1, val);
                }
            }
        } else if (QueryTypes.INOUT.equals(paramType)) {
            if (value == null) {
                ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.TINYINT);
            } else {
                ((CallableStatement) sqlQuery).setByte(i + 1, val);
            }
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.TINYINT);
        } else {
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.TINYINT);
        }
    }

    private void setBitValue(int queryType, String value, String paramType,
            PreparedStatement sqlQuery, int i) throws SQLException {
        Boolean val = null;
        if (value != null) {
            val = Boolean.valueOf(value);
        }
        if (QueryTypes.IN.equals(paramType)) {
            if (queryType == SQLQuery.DS_QUERY_TYPE_NORMAL) {
                if (value == null) {
                    sqlQuery.setNull(i + 1, java.sql.Types.BIT);
                } else {
                    sqlQuery.setBoolean(i + 1, val);
                }
            } else {
                if (value == null) {
                    ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.BIT);
                } else {
                    ((CallableStatement) sqlQuery).setBoolean(i + 1, val);
                }
            }
        } else if (QueryTypes.INOUT.equals(paramType)) {
            if (value == null) {
                ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.BIT);
            } else {
                ((CallableStatement) sqlQuery).setBoolean(i + 1, val);
            }
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.BIT);
        } else {
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.BIT);
        }
    }

    private void setNumericValue(int queryType, String value, String paramType,
            PreparedStatement sqlQuery, int i) throws SQLException {
        BigDecimal val = null;
        if (value != null) {
            val = new BigDecimal(value);
        }
        if (QueryTypes.IN.equals(paramType)) {
            if (queryType == SQLQuery.DS_QUERY_TYPE_NORMAL) {
                if (value == null) {
                    sqlQuery.setNull(i + 1, java.sql.Types.NUMERIC);
                } else {
                    sqlQuery.setBigDecimal(i + 1, val);
                }
            } else {
                if (value == null) {
                    ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.NUMERIC);
                } else {
                    ((CallableStatement) sqlQuery).setBigDecimal(i + 1, val);
                }
            }
        } else if (QueryTypes.INOUT.equals(paramType)) {
            if (value == null) {
                ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.NUMERIC);
            } else {
                ((CallableStatement) sqlQuery).setBigDecimal(i + 1, val);
            }
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.NUMERIC);
        } else {
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.NUMERIC);
        }
    }

    private void setDoubleValue(int queryType, String value, String paramType,
            PreparedStatement sqlQuery, int i) throws SQLException {
        Double val = null;
        if (value != null) {
            val = Double.parseDouble(value);
        }
        if (QueryTypes.IN.equals(paramType)) {
            if (queryType == SQLQuery.DS_QUERY_TYPE_NORMAL) {
                if (value == null) {
                    sqlQuery.setNull(i + 1, java.sql.Types.DOUBLE);
                } else {
                    sqlQuery.setDouble(i + 1, val);
                }
            } else {
                if (value == null) {
                    ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.DOUBLE);
                } else {
                    ((CallableStatement) sqlQuery).setDouble(i + 1, val);
                }
            }
        } else if (QueryTypes.INOUT.equals(paramType)) {
            if (value == null) {
                ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.DOUBLE);
            } else {
                ((CallableStatement) sqlQuery).setDouble(i + 1, val);
            }
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.DOUBLE);
        } else {
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.DOUBLE);
        }
    }

    private void setStringValue(int queryType, String value, String paramType,
            PreparedStatement sqlQuery, int i) throws SQLException {
        if (QueryTypes.IN.equals(paramType)) {
            if (queryType == SQLQuery.DS_QUERY_TYPE_NORMAL) {
                if (value == null) {
                    sqlQuery.setNull(i + 1, java.sql.Types.VARCHAR);
                } else {
                    sqlQuery.setString(i + 1, value);
                }
            } else {
                if (value == null) {
                    ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.VARCHAR);
                } else {
                    ((CallableStatement) sqlQuery).setString(i + 1, value);
                }
            }
        } else if (QueryTypes.INOUT.equals(paramType)) {
            if (value == null) {
                ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.VARCHAR);
            } else {
                ((CallableStatement) sqlQuery).setString(i + 1, value);
            }
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.VARCHAR);
        } else {
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.VARCHAR);
        }
    }

    private void setIntValue(int queryType, String value, String paramType,
            PreparedStatement sqlQuery, int i) throws SQLException {
        Integer val = null;
        if (value != null) {
            val = Integer.parseInt(value);
        }
        if (QueryTypes.IN.equals(paramType)) {
            if (queryType == SQLQuery.DS_QUERY_TYPE_NORMAL) {
                if (value == null) {
                    sqlQuery.setNull(i + 1, java.sql.Types.INTEGER);
                } else {
                    sqlQuery.setInt(i + 1, val);
                }
            } else {
                if (value == null) {
                    ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.INTEGER);
                } else {
                    ((CallableStatement) sqlQuery).setInt(i + 1, val);
                }
            }
        } else if (QueryTypes.INOUT.equals(paramType)) {
            if (value == null) {
                ((CallableStatement) sqlQuery).setNull(i + 1, java.sql.Types.INTEGER);
            } else {
                ((CallableStatement) sqlQuery).setInt(i + 1, val);
            }
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.INTEGER);
        } else {
            ((CallableStatement) sqlQuery).registerOutParameter(i + 1, java.sql.Types.INTEGER);
        }
    }

    private ParamValue getOutparameterValue(CallableStatement cs, String type, int ordinal)
            throws DataServiceFault {
        try {
            Object elementValue;
            if (type.equals(DBConstants.DataTypes.STRING)) {
                elementValue = cs.getString(ordinal);
                return new ParamValue(elementValue == null ? null : elementValue.toString());
            } else if (type.equals(DBConstants.DataTypes.DOUBLE)) {
                elementValue = cs.getDouble(ordinal);
                return new ParamValue(elementValue == null ? null
                        : ConverterUtil.convertToString((Double) elementValue));
            } else if (type.equals(DBConstants.DataTypes.BIGINT)) {
                elementValue = cs.getLong(ordinal);
                return new ParamValue(elementValue == null ? null
                        : ConverterUtil.convertToString((Long) elementValue));
            } else if (type.equals(DBConstants.DataTypes.INTEGER)) {
                elementValue = cs.getInt(ordinal);
                return new ParamValue(elementValue == null ? null
                        : ConverterUtil.convertToString((Integer) elementValue));
            } else if (type.equals(DBConstants.DataTypes.TIME)) {
                elementValue = cs.getTime(ordinal);
                return new ParamValue(elementValue == null ? null
                        : this.convertToTimeString((Time) elementValue));
            } else if (type.equals(DBConstants.DataTypes.DATE)) {
                elementValue = cs.getDate(ordinal);
                return new ParamValue(elementValue == null ? null
                        : ConverterUtil.convertToString((Date) elementValue));
            } else if (type.equals(DBConstants.DataTypes.TIMESTAMP)) {
                elementValue = cs.getTimestamp(ordinal);
                return new ParamValue(elementValue == null ? null
                        : this.convertToTimestampString((Timestamp) elementValue));
            } else if (type.equals(DBConstants.DataTypes.BLOB)) {
                elementValue = cs.getBlob(ordinal);
                return new ParamValue(elementValue == null ? null
                        : this.getBase64StringFromInputStream(((Blob) elementValue)
                                .getBinaryStream()));
            } else if (type.equals(DBConstants.DataTypes.CLOB)) {
                elementValue = cs.getClob(ordinal);
                return new ParamValue(elementValue == null ? null :
                       deriveValueFromClob((Clob) elementValue));
            } else if (type.equals(DBConstants.DataTypes.STRUCT)) {
                elementValue = cs.getObject(ordinal);
                return new ParamValue(elementValue == null ? null : (Struct) elementValue);
            } else if (type.equals(DBConstants.DataTypes.ARRAY)) {
                Array dataArray = cs.getArray(ordinal);
                ParamValue paramValue = new ParamValue(ParamValue.PARAM_VALUE_ARRAY);
                if (dataArray != null) {
                    this.processSQLArray(dataArray, paramValue);
                }
                return paramValue;
            } else if (type.equals(DBConstants.DataTypes.NUMERIC)) {
                elementValue = cs.getBigDecimal(ordinal);
                return new ParamValue(elementValue == null ? null
                        : ConverterUtil.convertToString((BigDecimal) elementValue));
            } else if (type.equals(DBConstants.DataTypes.BIT)) {
                elementValue = cs.getBoolean(ordinal);
                return new ParamValue(elementValue == null ? null
                        : ConverterUtil.convertToString((Boolean) elementValue));
            } else if (type.equals(DBConstants.DataTypes.TINYINT)) {
                elementValue = cs.getByte(ordinal);
                return new ParamValue(elementValue == null ? null
                        : ConverterUtil.convertToString((Byte) elementValue));
            } else if (type.equals(DBConstants.DataTypes.SMALLINT)) {
                elementValue = cs.getShort(ordinal);
                return new ParamValue(elementValue == null ? null
                        : ConverterUtil.convertToString((Short) elementValue));
            } else if (type.equals(DBConstants.DataTypes.REAL)) {
                elementValue = cs.getFloat(ordinal);
                return new ParamValue(elementValue == null ? null
                        : ConverterUtil.convertToString((Float) elementValue));
            } else if (type.equals(DBConstants.DataTypes.BINARY)) {
                elementValue = cs.getBlob(ordinal);
                return new ParamValue(elementValue == null ? null
                        : this.getBase64StringFromInputStream(((Blob) elementValue)
                                .getBinaryStream()));
            } else {
                throw new DataServiceFault("Unsupported data type: " + type);
            }
        } catch (SQLException e) {
            throw new DataServiceFault(e, "Error in getting sql output parameter values.");
        }
    }

    private String deriveValueFromClob(Clob data) throws DataServiceFault {
        Reader r = null;
        try {
            StringBuilder sb = new StringBuilder();
            r = new BufferedReader((data).getCharacterStream());
            int pos;
            while ((pos = r.read()) != -1) {
                sb.append((char)pos);
            }
            return sb.toString();
        } catch(IOException e) {
            throw new DataServiceFault(e, "Error occurred while reading CLOB value");
        } catch (SQLException e) {
            throw new DataServiceFault(e, "Error occurred while reading CLOB value");
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public QueryParam getCurrentRefCursor() {
        return currentRefCursor;
    }

    public boolean hasRefCursor() {
        return hasRefCursor;
    }

    public void runQuery(XMLStreamWriter xmlWriter, InternalParamCollection params, int queryLevel)
            throws DataServiceFault {
        int type = this.getQueryType();
        if (type == SQLQuery.DS_QUERY_TYPE_NORMAL) {
            this.processNormalQuery(xmlWriter, params, queryLevel);
        } else if (type == SQLQuery.DS_QUERY_TYPE_STORED_PROC) {
            this.processStoredProcQuery(xmlWriter, params, queryLevel);
        } else {
            throw new DataServiceFault("Unsupported query type: " + type);
        }
    }

    @Override
    public void releaseBatchRequestResources() {
        /* clear the TL batch prepared statement */
        this.batchPreparedStatement.set(null);
    }

    private void setAutoCommit(Connection conn, boolean autoCommit) throws SQLException {
        try {
            conn.setAutoCommit(autoCommit);
        } catch (SQLFeatureNotSupportedException ignore) {
            /* some databases does not support this */
        }
    }

    /**
     * This class contains the stored procedure metadata collection.
     */
    private class StoredProcMetadataCollection {

        private List<StoredProcMetadataEntry> entries;

        public StoredProcMetadataCollection(ResultSet rs) throws SQLException {
            entries = new ArrayList<StoredProcMetadataEntry>();
            do { // use do-while loop since rs.next has already called once
                 // previously
                entries.add(new StoredProcMetadataEntry(rs.getString(1), rs.getString(2), rs
                        .getString(3), rs.getString(4), rs.getShort(5), rs.getInt(6), rs
                        .getString(7), rs.getInt(8), rs.getInt(9), rs.getShort(10),
                        rs.getShort(11), rs.getShort(12), rs.getString(13)));
            } while (rs.next());
        }

        public List<StoredProcMetadataEntry> getEntries() {
            return entries;
        }

    }

    /**
     * This class represents a information on a single stored procedure
     * parameter/result element.
     */
    public class StoredProcMetadataEntry {

        private String procedureCatalog;

        private String procedureSchema;

        private String procedureName;

        private String columnName;

        private short columnReturn;

        private int columnDataType;

        private String columnReturnTypeName;

        private int columnPrecision;

        private int columnByteLength;

        private short columnScale;

        private short columnRadix;

        private short columnNullable;

        private String columnRemarks;

        public StoredProcMetadataEntry(String procedureCatalog, String procedureSchema,
                String procedureName, String columnName, short columnReturn, int columnDataType,
                String columnReturnTypeName, int columnPrecision, int columnByteLength,
                short columnScale, short columnRadix, short columnNullable, String columnRemarks) {
            this.procedureCatalog = procedureCatalog;
            this.procedureSchema = procedureSchema;
            this.procedureName = procedureName;
            this.columnName = columnName;
            this.columnReturn = columnReturn;
            this.columnDataType = columnDataType;
            this.columnReturnTypeName = columnReturnTypeName;
            this.columnPrecision = columnPrecision;
            this.columnByteLength = columnByteLength;
            this.columnScale = columnScale;
            this.columnRadix = columnRadix;
            this.columnNullable = columnNullable;
            this.columnRemarks = columnRemarks;
        }

        public String getProcedureCatalog() {
            return procedureCatalog;
        }

        public String getProcedureSchema() {
            return procedureSchema;
        }

        public String getProcedureName() {
            return procedureName;
        }

        public String getColumnName() {
            return columnName;
        }

        public short getColumnReturn() {
            return columnReturn;
        }

        public int getColumnDataType() {
            return columnDataType;
        }

        public String getColumnReturnTypeName() {
            return columnReturnTypeName;
        }

        public int getColumnPrecision() {
            return columnPrecision;
        }

        public int getColumnByteLength() {
            return columnByteLength;
        }

        public short getColumnScale() {
            return columnScale;
        }

        public short getColumnRadix() {
            return columnRadix;
        }

        public short getColumnNullable() {
            return columnNullable;
        }

        public String getColumnRemarks() {
            return columnRemarks;
        }
    }

}