/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.dataservices.core.odata;

import org.wso2.carbon.dataservices.core.description.query.MongoQuery;
import org.wso2.carbon.dataservices.core.engine.DataEntry;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.json.JSONObject;

import java.util.*;

/**
 * This class implements MongoDB datasource related operations for ODataDataHandler.
 *
 * @see ODataDataHandler
 */
public class MongoDataHandler implements ODataDataHandler {
    /**
     * Config ID.
     */

    private final String configID;

    /**
     * ObjectID s of the Collections (Map<Table Name, List>).
     */
    private Map<String, List<String>> primaryKeys;

    /**
     * List of Tables in the Database.
     */
    private List<String> tableList;

    /**
     * Table metadata.
     */
    private Map<String, Map<String, DataColumn>> tableMetaData;


    private Jongo jongo;

    public Jongo getJongo() {
        return jongo;
    }

    public MongoDataHandler(String configID, Jongo jongo) {
        this.configID = configID;
        this.jongo = jongo;
        this.tableList = generateTableList();
        this.tableMetaData = generateTableMetaData();
        this.primaryKeys = getPrimaryKeys();
    }

    /**
     * This method read the table data and return.
     * Return a list of DataEntry object which has been wrapped the entity.
     *
     * @param tableName Name of the table
     * @return EntityCollection
     * @throws ODataServiceFault
     * @see DataEntry
     */
    @Override
    public List<ODataEntry> readTable(String tableName) throws ODataServiceFault {
        List<ODataEntry> entryList = new ArrayList<>();
        DBCollection result = getJongo().getDatabase().getCollection(tableName);
        Iterator<DBObject> cursor = result.find();
        DBObject current;
        String tempValue;
        while (cursor.hasNext()) {
            ODataEntry dataEntry = new ODataEntry();
            current = cursor.next();
            tempValue = current.toString();
            Iterator<?> keys = new JSONObject(tempValue).keys();
            while (keys.hasNext()) {
                String columnName = (String) keys.next();
                String columnValue = new JSONObject(tempValue).get(columnName).toString();
                if (columnName.equals("_id")) {
                    JSONObject jsonObject = new JSONObject(columnValue);
                    Iterator<?> field = jsonObject.keys();
                    while (field.hasNext()) {
                        String fieldName = field.next().toString();
                        Object fieldValue = jsonObject.get(fieldName);
                        String value = fieldValue.toString();
                        dataEntry.addValue(columnName, value);
                    }
                } else {
                    dataEntry.addValue(columnName, columnValue);
                }
            }
            //Set Etag to the entity
            dataEntry.addValue("ETag", ODataUtils.generateETag(this.configID, tableName, dataEntry));
            entryList.add(dataEntry);
        }
        return entryList;
    }

    /**
     * This method read the table with Keys and return.
     * Return a list of DataEntry object which has been wrapped the entity.
     *
     * @param tableName Name of the table
     * @param keys      Keys to check
     * @return EntityCollection
     * @throws ODataServiceFault
     * @see DataEntry
     */
    @Override
    public List<ODataEntry> readTableWithKeys(String tableName, ODataEntry keys) throws ODataServiceFault {
        List<ODataEntry> entryList = new ArrayList<>();
        ODataEntry dataEntry = new ODataEntry();
        for (String keyName : keys.getData().keySet()) {
            String keyValue = keys.getValue(keyName);
            String result = getJongo().getCollection(tableName).findOne(new ObjectId(keyValue)).map(MongoQuery.MongoResultMapper.getInstance());
            JSONObject object = new JSONObject(result);
            Iterator<?> key = object.keys();
            while (key.hasNext()) {
                String columnName = (String) key.next();
                String columnValue = object.get(columnName).toString();
                if (columnName.equals("_id")) {
                    JSONObject jsonObject = new JSONObject(columnValue);
                    Iterator<?> field = jsonObject.keys();
                    while (field.hasNext()) {
                        String fieldName = field.next().toString();
                        Object fieldValue = jsonObject.get(fieldName);
                        String value = fieldValue.toString();
                        dataEntry.addValue(columnName, value);
                    }
                } else {
                    dataEntry.addValue(columnName, columnValue);
                }
            }
            //Set Etag to the entity
            dataEntry.addValue("ETag", ODataUtils.generateETag(this.configID, tableName, dataEntry));
            entryList.add(dataEntry);
        }
        return entryList;
    }

    /**
     * This method inserts entity to table.
     *
     * @param tableName Name of the table
     * @param entity    Entity
     * @throws ODataServiceFault
     */
    public ODataEntry insertEntityToTable(String tableName, ODataEntry entity) throws ODataServiceFault {
        ODataEntry entry = new ODataEntry();
        BasicDBObject document = new BasicDBObject();
        for (String columnName : entity.getData().keySet()) {
            String columnValue = entity.getValue(columnName);
            document.put(columnName, columnValue);
            entry.addValue(columnName, columnValue);
        }
        getJongo().getCollection(tableName).insert(document);

        //Set Etag to the entity
        entry.addValue("ETag", ODataUtils.generateETag(this.configID, tableName, entry));
        return entry;

    }

    /**
     * This method deletes entity from table.
     *
     * @param tableName Name of the table
     * @param entity    Entity
     * @throws ODataServiceFault
     */
    public boolean deleteEntityInTable(String tableName, ODataEntry entity) throws ODataServiceFault {
        for (String keyName : entity.getData().keySet()) {
            String keyValue = entity.getValue(keyName);
            getJongo().getCollection(tableName).remove(new ObjectId(keyValue));
        }
        return true;
    }

    /**
     * This method updates entity in table.
     *
     * @param tableName     Name of the table
     * @param newProperties New Properties
     * @throws ODataServiceFault
     */
    public boolean updateEntityInTable(String tableName, ODataEntry newProperties) throws ODataServiceFault {
        /**
         * To be implemented
         */
        return true;
    }


    /**
     * This method updates the entity in table when transactional update is necessary.
     *
     * @param tableName     Table Name
     * @param oldProperties Old Properties
     * @param newProperties New Properties
     * @throws ODataServiceFault
     */
    public boolean updateEntityInTableTransactional(String tableName, ODataEntry oldProperties, ODataEntry newProperties)
            throws ODataServiceFault {
        /**
         * To be implemented
         */
        return true;
    }

    /**
     * This method return database table metadata.
     * Return a map with table name as the key, and the values contains maps with column name as the map key,
     * and the values of the column name map will DataColumn object, which represents the column.
     *
     * @return Database Metadata
     * @see org.wso2.carbon.dataservices.core.odata.DataColumn
     */
    @Override
    public Map<String, Map<String, DataColumn>> getTableMetadata() {
        return this.tableMetaData;
    }

    private Map<String, Map<String, DataColumn>> generateTableMetaData() {
        int i = 1;
        Map<String, Map<String, DataColumn>> metaData = new HashMap<>();
        HashMap<String, DataColumn> column = new HashMap();
        for (String tableName : this.tableList) {
            DBCollection result = getJongo().getDatabase().getCollection(tableName);
            Iterator<DBObject> cursor = result.find();
            while (cursor.hasNext()) {
                final DBObject current = cursor.next();
                String tmpValue = current.toString();
                JSONObject object = new JSONObject(tmpValue);
                Iterator<?> keys = object.keys();
                while (keys.hasNext()) {
                    String columnName = (String) keys.next();
                    DataColumn dataColumn = new DataColumn(columnName, DataColumn.ODataDataType.STRING, i, true, 100, columnName.equals("_id") ? true : false);
                    column.put(columnName, dataColumn);
                    i++;
                }
                metaData.put(tableName, column);
            }
        }
        return metaData;
    }

    /**
     * This method creates a list of tables available in the DB.
     *
     * @return Table List of the DB
     * @throws ODataServiceFault
     */
    @Override
    public List<String> getTableList() {
        return this.tableList;
    }

    private List<String> generateTableList() {
        List<String> list = new ArrayList<>();
        list.addAll(getJongo().getDatabase().getCollectionNames());
        return list;

    }

    /**
     * This method returns the all the primary keys in the database tables.
     * Return a map with table name as the keys, and the values contains a list of column names which are act as primary keys in the table.
     *
     * @return Primary Key Map
     */
    @Override
    public Map<String, List<String>> getPrimaryKeys() {
        Map<String, List<String>> primaryKeyList = new HashMap<>();
        List<String> tableNames = this.tableList;
        List<String> primaryKey = new ArrayList<>();
        primaryKey.add("_id");
        for (String tname : tableNames) {
            primaryKeyList.put(tname, primaryKey);
        }
        return primaryKeyList;
    }

    @Override
    public Map<String, NavigationTable> getNavigationProperties() {
        return null;
    }

    /**
     * This method opens the transaction.
     *
     * @throws ODataServiceFault
     */
    public void openTransaction() throws ODataServiceFault {
        /**
         * To be implemented
         */
    }

    /**
     * This method commits the transaction.
     *
     * @throws ODataServiceFault
     */
    public void commitTransaction() throws ODataServiceFault {
        /**
         * To be implemented
         */
    }

    /**
     * This method rollbacks the transaction.
     *
     * @throws ODataServiceFault
     */
    public void rollbackTransaction() throws ODataServiceFault {
        /**
         * To be implemented
         */
    }

    /**
     * This method updates the references of the table where the keys were imported.
     *
     * @param rootTableName       Root - Table Name
     * @param rootTableKeys       Root - Entity keys (Primary Keys)
     * @param navigationTable     Navigation - Table Name
     * @param navigationTableKeys Navigation - Entity Name (Primary Keys)
     * @throws ODataServiceFault
     */
    public void
    updateReference(String rootTableName, ODataEntry rootTableKeys, String navigationTable,
                    ODataEntry navigationTableKeys) throws ODataServiceFault {
        /**
         * To be implemented
         */
    }

    /**
     * This method deletes the references of the table where the keys were imported.
     *
     * @param rootTableName       Root - Table Name
     * @param rootTableKeys       Root - Entity keys (Primary Keys)
     * @param navigationTable     Navigation - Table Name
     * @param navigationTableKeys Navigation - Entity Name (Primary Keys)
     * @throws ODataServiceFault
     */
    public void deleteReference(String rootTableName, ODataEntry rootTableKeys, String navigationTable,
                                ODataEntry navigationTableKeys) throws ODataServiceFault {
        /**
         * To be implemented
         */
    }
}