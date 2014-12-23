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

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.ResultHandler;
import org.jongo.Update;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.custom.datasource.DataColumn;
import org.wso2.carbon.dataservices.core.custom.datasource.DataRow;
import org.wso2.carbon.dataservices.core.custom.datasource.FixedDataRow;
import org.wso2.carbon.dataservices.core.custom.datasource.QueryResult;
import org.wso2.carbon.dataservices.core.description.config.MongoConfig;
import org.wso2.carbon.dataservices.core.description.event.EventTrigger;
import org.wso2.carbon.dataservices.core.engine.InternalParamCollection;
import org.wso2.carbon.dataservices.core.engine.*;

import javax.xml.stream.XMLStreamWriter;
import java.util.*;

/**
 * This class represents the MongoDB data services query implementation.
 */
public class MongoQuery extends Query {

    private MongoConfig config;

    private String expression;

    public MongoQuery(DataService dataService, String queryId,
                      String configId, String expression, List<QueryParam> queryParams,
                      Result result, EventTrigger inputEventTrigger,
                      EventTrigger outputEventTrigger,
                      Map<String, String> advancedProperties,
                      String inputNamespace)
            throws DataServiceFault {
        super(dataService, queryId, queryParams, result, configId,
                inputEventTrigger, outputEventTrigger, advancedProperties, inputNamespace);
        try {
            this.expression = expression;
            this.config = (MongoConfig) this.getDataService().getConfig(this.getConfigId());
        } catch (ClassCastException e) {
            throw new DataServiceFault(e, "Configuration is not a Mongo config:" +
                    this.getConfigId());
        }
    }
    
    @Override
    public Object runPreQuery(InternalParamCollection params, int queryLevel) throws DataServiceFault {
        try {
             return new MongoQueryResult(this.getExpression(),
                        new ArrayList<InternalParam>(params.getParams()));
        } catch (Exception e) {
            throw new DataServiceFault(e,
                    "Error in MongoQuery.runQuery: " + e.getMessage());
        }
    }

    @Override
    public void runPostQuery(Object result, XMLStreamWriter xmlWriter,
                             InternalParamCollection params, int queryLevel) throws DataServiceFault {
        QueryResult queryResult = (QueryResult)result;
        DataEntry dataEntry;
        DataRow currentRow;
        List<DataColumn> columns = queryResult != null ? queryResult.getDataColumns() : null;
        String[] columnMappings = this.createColumnsMappings(columns);
        int count = columns != null ? columns.size() : 0;
        boolean useColumnNumbers = this.isUsingColumnNumbers();
        String columnName;
        String tmpVal;
        while (queryResult != null && queryResult.hasNext()) {
            dataEntry = new DataEntry();
            currentRow = queryResult.next();
            for (int i = 0; i < count; i++) {
                columnName = useColumnNumbers ? Integer.toString(i + 1) : columnMappings[i];
                tmpVal = currentRow.getValueAt(columnName);
                dataEntry.addValue(columnName, new ParamValue(tmpVal));
            }
            this.writeResultEntry(xmlWriter, dataEntry, params, queryLevel);
        }
    }

    private String[] createColumnsMappings(List<DataColumn> columns) {
        String[] result = new String[columns.size()];
        int count = columns.size();
        for (int i = 0; i < count; i++) {
            result[i] = columns.get(i).getName();
        }
        return result;
    }

    public MongoConfig getConfig() {
        return config;
    }

    public String getExpression() {
        return expression;
    }

    private Object[] decodeQuery(String query) throws DataServiceFault {
        int i1 = query.indexOf('.');
        if (i1 == -1) {
            throw new DataServiceFault("The MongoDB Collection not specified in the query '" +
                    query + "'");
        }
        String collection = query.substring(0, i1).trim();
        int i2 = query.indexOf('(', i1);
        if (i2 == -1 || i2 - i1 <= 1) {
            throw new DataServiceFault("Invalid MongoDB operation in the query '" +
                    query + "'");
        }
        String operation = query.substring(i1 + 1, i2).trim();
        int i3 = query.lastIndexOf(')');
        if (i3 == -1) {
            throw new DataServiceFault("Invalid MongoDB operation in the query '" +
                    query + "'");
        }
        String opQuery = null;
        if (i3 - i2 > 1) {
            opQuery = query.substring(i2 + 1, i3).trim();
        }
        DBConstants.MongoDB.MongoOperation mongoOp = this.convertToMongoOp(operation);
        if (mongoOp == DBConstants.MongoDB.MongoOperation.UPDATE) {
            List<Object> result = new ArrayList<Object>();
            result.add(collection);
            result.add(mongoOp);
            result.addAll(parseInsertQuery(opQuery));
            return result.toArray();
        } else {
            return new Object[] { collection, mongoOp, this.checkAndCleanOpQuery(opQuery) };
        }
    }

    private String checkAndCleanOpQuery(String opQuery) throws DataServiceFault {
        if (opQuery == null) {
            return null;
        }
        int a = 0, b = 0;
        if (opQuery.startsWith("'") || opQuery.startsWith("\"")) {
            a = 1;
        }
        if (opQuery.endsWith("'") || opQuery.endsWith("\"")) {
            b = 1;
        }
        return opQuery.substring(a, opQuery.length() - b);
    }

    private List<Object> parseInsertQuery(String opQuery) throws DataServiceFault {
        List<Object> tokens = new ArrayList<Object>();
        int bracketCount = 0;
        StringBuilder buff = new StringBuilder(100);
        for (char ch : opQuery.toCharArray()) {
            if (ch == ',' && bracketCount == 0) {
                tokens.add(this.checkAndCleanOpQuery(buff.toString().trim()));
                buff.delete(0, buff.length());
            } else {
                buff.append(ch);
                if (ch == '{') {
                    bracketCount++;
                } else if (ch == '}') {
                    bracketCount--;
                }
            }
        }
        String lastToken = buff.toString().trim();
        if (lastToken.length() > 0) {
            tokens.add(this.checkAndCleanOpQuery(lastToken));
        }
        return tokens;
    }

    private DBConstants.MongoDB.MongoOperation convertToMongoOp(String operation) throws DataServiceFault {
        if (DBConstants.MongoDB.MongoOperationLabels.COUNT.equals(operation)) {
            return DBConstants.MongoDB.MongoOperation.COUNT;
        } else if (DBConstants.MongoDB.MongoOperationLabels.DROP.equals(operation)) {
            return DBConstants.MongoDB.MongoOperation.DROP;
        } else if (DBConstants.MongoDB.MongoOperationLabels.FIND.equals(operation)) {
            return DBConstants.MongoDB.MongoOperation.FIND;
        } else if (DBConstants.MongoDB.MongoOperationLabels.FIND_ONE.equals(operation)) {
            return DBConstants.MongoDB.MongoOperation.FIND_ONE;
        } else if (DBConstants.MongoDB.MongoOperationLabels.INSERT.equals(operation)) {
            return DBConstants.MongoDB.MongoOperation.INSERT;
        } else if (DBConstants.MongoDB.MongoOperationLabels.REMOVE.equals(operation)) {
            return DBConstants.MongoDB.MongoOperation.REMOVE;
        } else if (DBConstants.MongoDB.MongoOperationLabels.UPDATE.equals(operation)) {
            return DBConstants.MongoDB.MongoOperation.UPDATE;
        } else {
            throw new DataServiceFault("Unknown MongoDB operation '" + operation + "'");
        }
    }

    public final static class MongoResultMapper implements ResultHandler<String> {

        private static final MongoResultMapper instance = new MongoResultMapper();

        private MongoResultMapper() {
        }

        public static MongoResultMapper getInstance() {
            return instance;
        }

        @Override
        public String map(DBObject dbo) {
            return dbo.toString();
        }

    }

    private Jongo getJongo() {
        return this.config.getJongo();
    }

    public class MongoQueryResult implements QueryResult {

        private Iterator<?> dataIterator;

        public MongoQueryResult(String query, List<InternalParam> params) throws DataServiceFault {
            Object[] request = decodeQuery(query);
            MongoCollection collection = getJongo().getCollection((String) request[0]);
            String opQuery = (String) request[2];
            Object[] mongoParams = DBUtils.convertInputParamValues(params);
            switch ((DBConstants.MongoDB.MongoOperation) request[1]) {
                case COUNT:
                    this.dataIterator = this.doCount(collection, opQuery, mongoParams);
                    break;
                case FIND:
                    this.dataIterator = this.doFind(collection, opQuery, mongoParams);
                    break;
                case FIND_ONE:
                    this.dataIterator = this.doFindOne(collection, opQuery, mongoParams);
                    break;
                case DROP:
                    this.doDrop(collection);
                    break;
                case INSERT:
                    this.doInsert(collection, opQuery, mongoParams);
                    break;
                case REMOVE:
                    this.doRemove(collection, opQuery, mongoParams);
                    break;
                case UPDATE:
                    if (request.length < 4) {
                        throw new DataServiceFault(
                                "An MongoDB update statement must contain a modifier");
                    }
                    String modifier = (String) request[3];
                    boolean upsert = false;
                    if (request.length > 4) {
                        upsert = Boolean.parseBoolean((String) request[4]);
                    }
                    boolean multi = false;
                    if (request.length > 5) {
                        multi = Boolean.parseBoolean((String) request[5]);
                    }
                    this.doUpdate(collection, opQuery, mongoParams, modifier, upsert, multi);
                    break;
            }
        }

        private Iterator<Long> doCount(MongoCollection collection,
                                       String opQuery, Object[] parameters) {
            long count;
            if (opQuery != null) {
                if (parameters.length > 0) {
                    count = collection.count(opQuery, parameters);
                } else {
                    count = collection.count(opQuery);
                }
            } else {
                count = collection.count();
            }
            List<Long> countResult = new ArrayList<Long>();
            countResult.add(count);
            return countResult.iterator();
        }

        private Iterator<String> doFind(MongoCollection collection,
                                        String opQuery, Object[] parameters) {
            if (opQuery != null) {
                if (parameters.length > 0) {
                    return collection.find(opQuery, parameters).map(
                            MongoResultMapper.getInstance()).iterator();
                } else {
                    return collection.find(opQuery).map(
                            MongoResultMapper.getInstance()).iterator();
                }
            } else {
                return collection.find().map(MongoResultMapper.getInstance()).iterator();
            }
        }

        private Iterator<String> doFindOne(MongoCollection collection,
                                           String opQuery, Object[] parameters) {
            String value;
            if (opQuery != null) {
                if (parameters.length > 0) {
                    value = collection.findOne(opQuery, parameters).map(
                            MongoResultMapper.getInstance());
                } else {
                    value = collection.findOne(opQuery).map(MongoResultMapper.getInstance());
                }
            } else {
                value = collection.findOne().map(MongoResultMapper.getInstance());
            }
            List<String> result = new ArrayList<String>();
            result.add(value);
            return result.iterator();
        }

        private void doInsert(MongoCollection collection,
                              String opQuery, Object[] parameters) throws DataServiceFault {
            String error;
            if (opQuery != null) {
                if (parameters.length > 0) {
                    if (opQuery.equals("#")) {
                        error = collection.save(JSON.parse(parameters[0].toString())).getError();
                    } else {
                        error = collection.insert(opQuery, parameters).getError();
                    }
                } else {
                    error = collection.insert(opQuery).getError();
                }
            } else {
                throw new DataServiceFault("Mongo insert statements must contain a query");
            }
            if (!DBUtils.isEmptyString(error)) {
                throw new DataServiceFault(error);
            }
        }

        private void doRemove(MongoCollection collection,
                              String opQuery, Object[] parameters) throws DataServiceFault {
            String error;
            if (opQuery != null) {
                if (parameters.length > 0) {
                    error = collection.remove(opQuery, parameters).getError();
                } else {
                    error = collection.remove(opQuery).getError();
                }
            } else {
                throw new DataServiceFault("Mongo remove statements must contain a query");
            }
            if (!DBUtils.isEmptyString(error)) {
                throw new DataServiceFault(error);
            }
        }

        private void doUpdate(MongoCollection collection,
                              String opQuery, Object[] parameters, String modifier,
                              boolean upsert, boolean multi) throws DataServiceFault {
            String error;
            if (opQuery != null) {
                if (parameters.length > 0) {
                    Update update = collection.update(opQuery);
                    if (upsert) {
                        update = update.upsert();
                    }
                    if (multi) {
                        update = update.multi();
                    }
                    error = update.with(modifier, parameters).getError();
                } else {
                    Update update = collection.update(opQuery);
                    if (upsert) {
                        update = update.upsert();
                    }
                    if (multi) {
                        update = update.multi();
                    }
                    error = update.with(modifier).getError();
                }
            } else {
                throw new DataServiceFault("Mongo update statements must contain a query");
            }
            if (!DBUtils.isEmptyString(error)) {
                throw new DataServiceFault(error);
            }
        }

        private void doDrop(MongoCollection collection) {
            collection.drop();
        }

        @Override
        public List<DataColumn> getDataColumns() throws DataServiceFault {
            List<DataColumn> result = new ArrayList<DataColumn>();
            result.add(new DataColumn(DBConstants.MongoDB.RESULT_COLUMN_NAME));
            return result;
        }

        @Override
        public boolean hasNext() throws DataServiceFault {
            return this.dataIterator != null && this.dataIterator.hasNext();
        }

        @Override
        public DataRow next() throws DataServiceFault {
            if (this.dataIterator == null) {
                throw new DataServiceFault("No Mongo data result available");
            } else {
                Object data = this.dataIterator.next();
                Map<String, String> values = new HashMap<String, String>();
                values.put(DBConstants.MongoDB.RESULT_COLUMN_NAME, data.toString());
                return new FixedDataRow(values);
            }
        }

    }
    
}
