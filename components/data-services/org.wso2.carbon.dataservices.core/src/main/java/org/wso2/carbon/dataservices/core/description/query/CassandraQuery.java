/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.DataTypes;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.description.config.CassandraConfig;
import org.wso2.carbon.dataservices.core.description.event.EventTrigger;
import org.wso2.carbon.dataservices.core.dispatch.DispatchStatus;
import org.wso2.carbon.dataservices.core.engine.DataEntry;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.dataservices.core.engine.InternalParam;
import org.wso2.carbon.dataservices.core.engine.InternalParamCollection;
import org.wso2.carbon.dataservices.core.engine.ParamValue;
import org.wso2.carbon.dataservices.core.engine.QueryParam;
import org.wso2.carbon.dataservices.core.engine.Result;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * This class represents Cassandra-CQL data services query implementation.
 */
public class CassandraQuery extends Query {

    private CassandraConfig config;
    
    private PreparedStatement statement;
        
    private String query;

    private List<String> namedParamNames;

    private String cql;

    /**
     * thread local variable to keep a batch statement in batch processing
     */
    private ThreadLocal<BatchStatement> batchStatement = new ThreadLocal<BatchStatement>() {
        protected synchronized BatchStatement initialValue() {
            return null;
        }
    };
    
    public CassandraQuery(DataService dataService, String queryId, String query,
            List<QueryParam> queryParams, Result result, String configId, 
            EventTrigger inputEventTrigger, EventTrigger outputEventTrigger,
            Map<String, String> advancedProperties, String inputNamespace) throws DataServiceFault {
        super(dataService, queryId, queryParams, result, configId, inputEventTrigger,
              outputEventTrigger, advancedProperties, inputNamespace);
        this.query = query;
        this.init();
        try {
            this.config = (CassandraConfig) this.getDataService().getConfig(this.getConfigId());
        } catch (ClassCastException e) {
            throw new DataServiceFault(e, "Configuration is not a Cassandra config:" + 
                    this.getConfigId());
        }
    }

    /**
     * Pre-processing of the CQL query
     */
    private void init() {
        this.processNamedParams();
        this.cql = createSqlFromQueryString(this.getQuery());
    }

    /**
     * This method checks whether DataTypes.QUERY_STRING type parameters are available in the query
     * input mappings and returns a boolean value.
     *
     * @param params The parameters in the input mappings
     * @return The boolean value of the isDynamicQuery variable
     */
    private boolean isDynamicQuery(InternalParamCollection params) {
        boolean isDynamicQuery = false;
        InternalParam tmpParam;
        for (int i = 1; i <= params.getData().size(); i++) {
            tmpParam = params.getParam(i);
            if (DataTypes.QUERY_STRING.equals(tmpParam.getSqlType())) {
                isDynamicQuery = true;
                break;
            }
        }
        return isDynamicQuery;
    }

    public String getCql() {
        return cql;
    }

    public List<String> getNamedParamNames() {
        return namedParamNames;
    }

    public String getQuery() {
        return query;
    }

    public PreparedStatement getStatement() {
        return statement;
    }

    public Session getSession() {
        return this.config.getSession();
    }
    
    public boolean isNativeBatchRequestsSupported() {
        return this.config.isNativeBatchRequestsSupported();
    }

    private BoundStatement bindParams(InternalParamCollection params) throws DataServiceFault {
        int count = params.getSize();
        List<Object> values = new ArrayList<Object>(count);
        InternalParam param;
        for (int i = 1; i <= count; i++) {
            param = params.getParam(i);
            if (param.getSqlType().equals(DataTypes.STRING)) {
                values.add(param.getValue().toString());
            } else if (param.getSqlType().equals(DataTypes.BIGINT)) {
                values.add(Long.parseLong(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.BINARY)) {
                values.add(this.base64DecodeByteBuffer(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.BIT)) {
                values.add(Boolean.parseBoolean(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.BLOB)) {
                values.add(this.base64DecodeByteBuffer(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.BOOLEAN)) {
                values.add(Boolean.parseBoolean(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.CHAR)) {
                values.add(param.getValue().getValueAsString());
            } else if (param.getSqlType().equals(DataTypes.CLOB)) {
                values.add(param.getValue().getValueAsString());
            } else if (param.getSqlType().equals(DataTypes.DATE)) {
                values.add(DBUtils.getDate(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.DECIMAL)) {
                values.add(new BigDecimal(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.DOUBLE)) {
                values.add(Double.parseDouble(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.FLOAT)) {
                values.add(Float.parseFloat(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.INTEGER)) {
                values.add(Integer.parseInt(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.LONG)) {
                values.add(Long.parseLong(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.LONG_VARBINARY)) {
                values.add(this.base64DecodeByteBuffer(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.NUMERIC)) {
                values.add(new BigDecimal(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.NVARCHAR)) {
                values.add(param.getValue().getValueAsString());
            } else if (param.getSqlType().equals(DataTypes.QUERY_STRING)) {
                values.add(param.getValue().getValueAsString());
            } else if (param.getSqlType().equals(DataTypes.REAL)) {
                values.add(Float.parseFloat(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.SMALLINT)) {
                values.add(Integer.parseInt(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.TEXT)) {
                values.add(param.getValue().getValueAsString());
            } else if (param.getSqlType().equals(DataTypes.TIME)) {
                values.add(DBUtils.getDate(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.TIMESTAMP)) {
                values.add(DBUtils.getDate(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.TINYINT)) {
                values.add(Integer.parseInt(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.VARBINARY)) {
                values.add(this.base64DecodeByteBuffer(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.VARCHAR)) {
                values.add(param.getValue().getValueAsString());
            } else if (param.getSqlType().equals(DataTypes.VARINT)) {
                values.add(new BigInteger(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.UUID)) {
                values.add(UUID.fromString(param.getValue().getValueAsString()));
            } else if (param.getSqlType().equals(DataTypes.INETADDRESS)) {
                try {
                    values.add(InetAddress.getByName(param.getValue().getValueAsString()));
                } catch (UnknownHostException e) {
                    throw new DataServiceFault(e);
                }
            } 
        }
        return this.getStatement().bind(values.toArray());
    }
    
    private void checkAndCreateStatement() throws DataServiceFault {
        if (this.statement == null) {
            synchronized (this) {
                if (this.statement == null) {
                    Session session = this.getSession();
                    this.statement = session.prepare(this.getCql());
                }
            }            
        }
    }

    @Override
    public Object runPreQuery(InternalParamCollection params, int queryLevel)
            throws DataServiceFault {
        ResultSet rs = null;
        /*
            There is no point of creating prepared statements for dynamic queries
         */
        if (isDynamicQuery(params)) {
            Object[] result = this.processDynamicQuery(this.getCql(), params,
                                                       this.calculateParamCount(this.cql));
            String dynamicCql = (String) result[0];
            int currentParamCount = (Integer) result[1];
            String processedSQL = this.createProcessedQuery(dynamicCql, params, currentParamCount);
            rs = this.getSession().execute(processedSQL);
        } else {
            this.checkAndCreateStatement();
            if (DispatchStatus.isBatchRequest() && this.isNativeBatchRequestsSupported()) {
            /* handle batch requests */
                if (DispatchStatus.isFirstBatchRequest()) {
                    this.batchStatement.set(new BatchStatement());
                }
                this.batchStatement.get().add(this.bindParams(params));
                if (DispatchStatus.isLastBatchRequest()) {
                    this.getSession().execute(this.batchStatement.get());
                }
            } else {
                rs = this.getSession().execute(this.bindParams(params));
            }
        }
        return rs;
    }

    @Override
    public void runPostQuery(Object result, XMLStreamWriter xmlWriter,
                             InternalParamCollection params, int queryLevel) throws DataServiceFault {
        ResultSet rs = (ResultSet) result;
        if (this.hasResult()) {
            Iterator<Row> itr = rs.iterator();
            Row row;
            DataEntry dataEntry;
            ColumnDefinitions defs = rs.getColumnDefinitions();
            while (itr.hasNext()) {
                row = itr.next();
                dataEntry = this.getDataEntryFromRow(row, defs);
                this.writeResultEntry(xmlWriter, dataEntry, params, queryLevel);
            }
        }
    }

    private DataEntry getDataEntryFromRow(Row row, ColumnDefinitions defs) throws DataServiceFault {
        boolean useColumnNumbers = this.isUsingColumnNumbers();
        DataType columnType;
        DataEntry entry = new DataEntry();
        ParamValue paramValue = null;
        for (int i = 0; i < defs.size(); i++) {
            columnType = defs.getType(i);
            if (columnType.getName().equals(DataType.Name.ASCII)) {
                paramValue = new ParamValue(row.getString(i));
            } else if (columnType.getName().equals(DataType.Name.VARCHAR)) {
                paramValue = new ParamValue(row.getString(i));
            } else if (columnType.getName().equals(DataType.Name.TEXT)) {
                paramValue = new ParamValue(row.getString(i));
            } else if (columnType.getName().equals(DataType.Name.BIGINT)) {
                paramValue = new ParamValue(Long.toString(row.getLong(i)));
            } else if (columnType.getName().equals(DataType.Name.BLOB)) {
                paramValue = new ParamValue(this.base64EncodeByteBuffer(row.getBytes(i)));
            } else if (columnType.getName().equals(DataType.Name.BOOLEAN)) {
                paramValue = new ParamValue(Boolean.toString(row.getBool(i)));
            } else if (columnType.getName().equals(DataType.Name.COUNTER)) {
                paramValue = new ParamValue(Long.toString(row.getLong(i)));
            } else if (columnType.getName().equals(DataType.Name.CUSTOM)) {
                paramValue = new ParamValue(this.base64EncodeByteBuffer(row.getBytes(i)));
            } else if (columnType.getName().equals(DataType.Name.DECIMAL)) {
                paramValue = new ParamValue(row.getDecimal(i).toString());
            } else if (columnType.getName().equals(DataType.Name.DOUBLE)) {
                paramValue = new ParamValue(Double.toString(row.getDouble(i)));
            } else if (columnType.getName().equals(DataType.Name.FLOAT)) {
                paramValue = new ParamValue(Float.toString(row.getFloat(i)));
            } else if (columnType.getName().equals(DataType.Name.INET)) {
                paramValue = new ParamValue(row.getInet(i).toString());
            } else if (columnType.getName().equals(DataType.Name.INT)) {
                paramValue = new ParamValue(Integer.toString(row.getInt(i)));
            } else if (columnType.getName().equals(DataType.Name.LIST)) {
                paramValue = new ParamValue(Arrays.toString(row.getList(i, Object.class).toArray()));
            } else if (columnType.getName().equals(DataType.Name.MAP)) {
                paramValue = new ParamValue(row.getMap(i, Object.class, Object.class).toString());
            } else if (columnType.getName().equals(DataType.Name.SET)) {
                paramValue = new ParamValue(row.getSet(i, Object.class).toString());
            } else if (columnType.getName().equals(DataType.Name.TIMESTAMP)) {
                paramValue = new ParamValue(ConverterUtil.convertToString(row.getDate(i)));
            } else if (columnType.getName().equals(DataType.Name.TIMEUUID)) {
                paramValue = new ParamValue(row.getUUID(i).toString());
            } else if (columnType.getName().equals(DataType.Name.UUID)) {
                paramValue = new ParamValue(row.getUUID(i).toString());
            } else if (columnType.getName().equals(DataType.Name.VARINT)) {
                paramValue = new ParamValue(row.getVarint(i).toString());
            }
            entry.addValue(useColumnNumbers ? Integer.toString(i) : defs.getName(i),
                    paramValue);
        }
        return entry;
    }
    
    private String base64EncodeByteBuffer(ByteBuffer byteBuffer) throws DataServiceFault {
        byte[] data = byteBuffer.array();
        byte[] base64Data = Base64.encodeBase64(data);
        try {
            return new String(base64Data, DBConstants.DEFAULT_CHAR_SET_TYPE);
        } catch (UnsupportedEncodingException e) {
            throw new DataServiceFault(e, "Error in encoding result binary data: " + e.getMessage());
        }
    }
    
    private ByteBuffer base64DecodeByteBuffer(String data) throws DataServiceFault {
        try {
            byte[] buff = Base64.decodeBase64(data.getBytes(DBConstants.DEFAULT_CHAR_SET_TYPE));
            ByteBuffer result = ByteBuffer.allocate(buff.length);
            result.put(buff);
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new DataServiceFault(e, "Error in decoding input base64 data: " + e.getMessage());
        }
    }

    private void sortStringsByLength(List<String> values) {
        Collections.sort(values, new Comparator<String>() {
            @Override public int compare(String lhs, String rhs) {
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
                                             Set<Integer> processedOrdinalsForNamedParams,
                                             Map<String, QueryParam> paramMap) {
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

    private int calculateParamCount(String sql) {
        int n = 0;
        for (char ch : sql.toCharArray()) {
            if (ch == '?') {
                n++;
            }
        }
        return n;
    }

}
