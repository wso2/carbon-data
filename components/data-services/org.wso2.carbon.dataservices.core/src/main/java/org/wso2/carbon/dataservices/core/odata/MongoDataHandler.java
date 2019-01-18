/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.json.JSONObject;
import org.wso2.carbon.dataservices.core.description.query.MongoQuery;
import org.wso2.carbon.dataservices.core.engine.DataEntry;

/**
 * This class implements MongoDB datasource related operations for ODataDataHandler.
 */
public class MongoDataHandler implements ODataDataHandler {

    /**
     * configuration ID is the ID given for the data service, at the time
     * when the particular service is created.
     */

    private final String configId;

    /**
     * ObjectId s of the Collections
     */
    private Map<String, List<String>> primaryKeys;

    /**
     * List of Collections in the Database.
     */
    private List<String> tableList;

    /**
     * Metadata of the Collections
     */
    private Map<String, Map<String, DataColumn>> tableMetaData;
    private Jongo jongo;
    private static final String ETAG = "ETag";
    private static final String DOCUMENT_ID = "_id";
    private static final String SET = "{$set: {";

    private ThreadLocal<Boolean> transactionAvailable = new ThreadLocal<Boolean>() {
        protected synchronized Boolean initialValue() {

            return false;
        }
    };

    public MongoDataHandler(String configId, Jongo jongo) {

        this.configId = configId;
        this.jongo = jongo;
        this.tableList = generateTableList();
        this.tableMetaData = generateTableMetaData();
        this.primaryKeys = generatePrimaryKeys();
    }

    /**
     * This method returns database collection metadata.
     * Returns a map with collection name as the key, and the values containing
     * maps with column name as the map key, and the values of the column name
     * map will be a DataColumn object, which represents the column.
     *
     * @return Database Metadata
     * @see org.wso2.carbon.dataservices.core.odata.DataColumn
     */
    @Override
    public Map<String, Map<String, DataColumn>> getTableMetadata() {

        return this.tableMetaData;
    }

    private Map<String, Map<String, DataColumn>> generateTableMetaData() {

        int ordinalPosition = 1;
        Map<String, Map<String, DataColumn>> metaData = new HashMap<>();
        HashMap<String, DataColumn> column = new HashMap<>();
        for (String tableName : this.tableList) {
            DBCollection readResult = jongo.getDatabase().getCollection(tableName);
            Iterator<DBObject> cursor = readResult.find();
            while (cursor.hasNext()) {
                DBObject doumentData = cursor.next();
                String tempValue = doumentData.toString();
                Iterator<?> keys = new JSONObject(tempValue).keys();
                while (keys.hasNext()) {
                    String columnName = (String) keys.next();
                    DataColumn dataColumn = new DataColumn(columnName, DataColumn.ODataDataType.STRING,
                        ordinalPosition, true, 100, columnName.equals(DOCUMENT_ID));
                    column.put(columnName, dataColumn);
                    ordinalPosition++;
                }
                metaData.put(tableName, column);
            }
        }
        return metaData;
    }

    /**
     * This method creates a list of collections available in the DB.
     *
     * @returns the collection list of the DB
     */
    @Override
    public List<String> getTableList() {

        return this.tableList;
    }

    private List<String> generateTableList() {

        List<String> list = new ArrayList<>();
        list.addAll(jongo.getDatabase().getCollectionNames());
        return list;
    }

    /**
     * This method returns the primary keys of all the collections in the database.
     * Return a map with table name as the key, and the values contains a list of column
     * names which act as primary keys in each collection.
     *
     * @return Primary Key Map
     */
    @Override
    public Map<String, List<String>> getPrimaryKeys() {

        return this.primaryKeys;
    }

    private Map<String, List<String>> generatePrimaryKeys() {

        Map<String, List<String>> primaryKeyList = new HashMap<>();
        List<String> tableNames = this.tableList;
        List<String> primaryKey = new ArrayList<>();
        primaryKey.add(DOCUMENT_ID);
        for (String tname : tableNames) {
            primaryKeyList.put(tname, primaryKey);
        }
        return primaryKeyList;
    }

    /**
     * This method reads the data for a given collection.
     * Returns a list of DataEntry objects.
     *
     * @param tableName Name of the table
     * @return EntityCollection
     * @see DataEntry
     */
    @Override
    public List<ODataEntry> readTable(String tableName) {

        List<ODataEntry> entryList = new ArrayList<>();
        DBCollection readResult = jongo.getDatabase().getCollection(tableName);
        Iterator<DBObject> cursor = readResult.find();
        DBObject documentData;
        String tempValue;
        while (cursor.hasNext()) {
            ODataEntry dataEntry;
            documentData = cursor.next();
            tempValue = documentData.toString();
            Iterator<?> keys = new JSONObject(tempValue).keys();
            dataEntry = createDataEntryFromResult(tempValue, keys);

            //Set Etag to the entity
            dataEntry.addValue(ETAG, ODataUtils.generateETag(this.configId, tableName, dataEntry));
            entryList.add(dataEntry);
        }
        return entryList;
    }

    /**
     * This method reads the collection data for a given key(i.e. _id).
     * Returns a list of DataEntry object which has been wrapped the entity.
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
        ODataEntry dataEntry;
        for (String keyName : keys.getData().keySet()) {
            String keyValue = keys.getValue(keyName);
            String projectionResult = jongo.getCollection(tableName).findOne(new ObjectId(keyValue)).map(MongoQuery.MongoResultMapper.getInstance());
            if (projectionResult == null) {
                throw new ODataServiceFault("Document ID: " + keyValue + " does not exist in " + "collection: " + tableName + " .");
            }
            Iterator<?> key = new JSONObject(projectionResult).keys();
            dataEntry = createDataEntryFromResult(projectionResult, key);

            //Set Etag to the entity
            dataEntry.addValue(ETAG, ODataUtils.generateETag(this.configId, tableName, dataEntry));
            entryList.add(dataEntry);
        }
        return entryList;
    }

    /**
     * This method creates an OData DataEntry for a given individual database record.
     * Returns a DataEntry object which has been wrapped in the entity.
     *
     * @param readResult DB result
     * @param keys       Keys set of the DB result
     * @return EntityCollection
     * @see DataEntry
     */
    private ODataEntry createDataEntryFromResult(String readResult, Iterator<?> keys) {

        ODataEntry dataEntry = new ODataEntry();
        while (keys.hasNext()) {
            String columnName = (String) keys.next();
            String columnValue = new JSONObject(readResult).get(columnName).toString();
            if (columnName.equals(DOCUMENT_ID)) {
                Iterator<?> idField = new JSONObject(columnValue).keys();
                while (idField.hasNext()) {
                    String idName = idField.next().toString();
                    String idValue = new JSONObject(columnValue).get(idName).toString();
                    dataEntry.addValue(columnName, idValue);
                }
            } else {
                dataEntry.addValue(columnName, columnValue);
            }
        }
        return dataEntry;
    }

    /**
     * This method inserts a given entity to the given collection.
     *
     * @param tableName Name of the table
     * @param entity    Entity
     * @throws ODataServiceFault
     */
    public ODataEntry insertEntityToTable(String tableName, ODataEntry entity) {

        ODataEntry createdEntry = new ODataEntry();
        final Document document = new Document();
        for (String columnName : entity.getData().keySet()) {
            String columnValue = entity.getValue(columnName);
            document.put(columnName, columnValue);
            entity.addValue(columnName, columnValue);
        }
        ObjectId objectId = new ObjectId();
        document.put("_id", objectId);
        jongo.getCollection(tableName).insert(document);
        String documentIdValue = objectId.toString();
        createdEntry.addValue(DOCUMENT_ID, documentIdValue);

        //Set Etag to the entity
        createdEntry.addValue(ODataConstants.E_TAG, ODataUtils.generateETag(this.configId, tableName, entity));
        return createdEntry;
    }

    /**
     * This method deletes the entity from the collection for a given key.
     *
     * @param tableName Name of the table
     * @param entity    Entity
     * @throws ODataServiceFault
     */
    public boolean deleteEntityInTable(String tableName, ODataEntry entity) throws ODataServiceFault {

        String documentId = entity.getValue(DOCUMENT_ID);
        String projectionResult = jongo.getCollection(tableName).findOne(new ObjectId(documentId)).map(MongoQuery.MongoResultMapper.getInstance());
        if (projectionResult != null) {
            WriteResult delete = jongo.getCollection(tableName).remove(new ObjectId(documentId));
            return delete.wasAcknowledged();
        } else {
            throw new ODataServiceFault("Document ID: " + documentId + " does not exist in " + "collection: " + tableName + ".");
        }
    }

    /**
     * This method updates the given entity in the given collection.
     *
     * @param tableName     Name of the table
     * @param newProperties New Properties
     * @throws ODataServiceFault
     */
    public boolean updateEntityInTable(String tableName, ODataEntry newProperties) throws ODataServiceFault {

        List<String> primaryKeys = this.primaryKeys.get(tableName);
        String newPropertyObjectKeyValue = null;
        boolean wasUpdated = false;
        for (String newPropertyObjectKeyName : newProperties.getData().keySet()) {
            if (newPropertyObjectKeyName.equals(DOCUMENT_ID)) {
                newPropertyObjectKeyValue = newProperties.getValue(newPropertyObjectKeyName);
            }
        }

        String projectionResult = jongo.getCollection(tableName).findOne(new ObjectId(newPropertyObjectKeyValue)).map(MongoQuery.MongoResultMapper.getInstance());
        if (projectionResult != null) {
            for (String column : newProperties.getData().keySet()) {
                if (!primaryKeys.contains(column)) {
                    String propertyValue = newProperties.getValue(column);
                    jongo.getCollection(tableName).update(new ObjectId(newPropertyObjectKeyValue)).upsert().
                        with(SET + column + ": '" + propertyValue + "'}}");
                    wasUpdated = true;
                }
            }
        } else {
            throw new ODataServiceFault("Document ID: " + newPropertyObjectKeyValue + " does not exist in " + "collection: " + tableName + ".");
        }
        return wasUpdated;
    }

    /**
     * This method updates the entity in table when transactional update is necessary.
     *
     * @param tableName     Table Name
     * @param oldProperties Old Properties
     * @param newProperties New Properties
     * @throws ODataServiceFault
     */
    public boolean updateEntityInTableTransactional(String tableName, ODataEntry oldProperties,
                                                    ODataEntry newProperties) {

        List<String> pKeys = this.primaryKeys.get(tableName);
        String newPropertyObjectKeyValue = null;
        String oldPropertyObjectKeyValue = null;
        for (String newPropertyObjectKeyName : newProperties.getData().keySet()) {
            if (newPropertyObjectKeyName.equals(DOCUMENT_ID)) {
                newPropertyObjectKeyValue = newProperties.getValue(newPropertyObjectKeyName);
            }
        }
        for (String oldPropertyObjectKeyName : oldProperties.getData().keySet()) {
            if (oldPropertyObjectKeyName.equals(DOCUMENT_ID)) {
                oldPropertyObjectKeyValue = oldProperties.getValue(oldPropertyObjectKeyName);
            }
        }
        for (String column : newProperties.getData().keySet()) {
            if (!pKeys.contains(column)) {
                String propertyValue = newProperties.getValue(column);
                assert newPropertyObjectKeyValue != null;
                jongo.getCollection(tableName).update(new ObjectId(newPropertyObjectKeyValue)).upsert().
                    with(SET + column + ": '" + propertyValue + "'}}");
            }
        }
        for (String column : oldProperties.getNames()) {
            if (!pKeys.contains(column)) {
                String propertyValue = oldProperties.getValue(column);
                assert oldPropertyObjectKeyValue != null;
                jongo.getCollection(tableName).update(new ObjectId(oldPropertyObjectKeyValue)).upsert().
                    with(SET + column + ": '" + propertyValue + "'}}");
            }
        }
        return true;
    }

    @Override
    public Map<String, NavigationTable> getNavigationProperties() {

        return null;
    }

    /**
     * This method opens the transaction.
     */
    public void openTransaction() {

        this.transactionAvailable.set(true);
        // doesn't support
    }

    /**
     * This method commits the transaction.
     */
    public void commitTransaction() {

        this.transactionAvailable.set(false);
        // doesn't support
    }

    /**
     * This method rollbacks the transaction.
     */
    public void rollbackTransaction() {

        this.transactionAvailable.set(false);
        // doesn't support
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

    public void updateReference(String rootTableName, ODataEntry rootTableKeys, String navigationTable,
                                ODataEntry navigationTableKeys) throws ODataServiceFault {

        throw new ODataServiceFault("MongoDB datasources do not support references.");
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

        throw new ODataServiceFault("MongoDB datasources do not support references.");
    }
}