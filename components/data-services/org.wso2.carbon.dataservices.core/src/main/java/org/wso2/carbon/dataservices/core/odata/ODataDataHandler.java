/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.dataservices.core.odata;

import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.engine.DataEntry;

import java.util.List;
import java.util.Map;

/**
 * This class represents an odata data handler used when using odata db handles.
 */
public interface ODataDataHandler {

	/**
	 * This method read the table data and return.
	 * Return a list of DataEntry object which has been wrapped the entity.
	 *
	 * @param tableName Name of the table
	 * @return EntityCollection
	 * @throws DataServiceFault
	 * @see DataEntry
	 */
	List<DataEntry> readTable(String tableName) throws DataServiceFault;

	/**
	 * This method read the table with Keys and return.
	 * Return a list of DataEntry object which has been wrapped the entity.
	 *
	 * @param tableName Name of the table
	 * @param keys      Keys to check
	 * @return EntityCollection
	 * @throws DataServiceFault
	 * @see DataEntry
	 */
	List<DataEntry> readTableWithKeys(String tableName, DataEntry keys) throws DataServiceFault;

	/**
	 * This method inserts entity to table.
	 *
	 * @param tableName Name of the table
	 * @param entity    Entity
	 * @throws DataServiceFault
	 */
	String insertEntityInTable(String tableName, DataEntry entity) throws DataServiceFault;

	/**
	 * This method deletes entity from table.
	 *
	 * @param tableName Name of the table
	 * @param entity    Entity
	 * @throws DataServiceFault
	 */
	void deleteEntityInTable(String tableName, DataEntry entity) throws DataServiceFault;

	/**
	 * This method updates entity in table.
	 *
	 * @param tableName          Name of the table
	 * @param entity             Entity
	 * @param existingProperties Existing Properties
	 * @throws DataServiceFault
	 */
	void updateEntityInTable(String tableName, DataEntry entity, DataEntry existingProperties) throws DataServiceFault;

	/**
	 * This method return database table metadata.
	 * Return a map with table name as the key, and the values contains maps with column name as the map key,
	 * and the values of the column name map will DataColumn object, which represents the column.
	 *
	 * @return Database Metadata
	 * @see DataColumn
	 */
	Map<String, Map<String, DataColumn>> getTableMetadata();

	/**
	 * This method update property to table, which updates a single column of the table.
	 *
	 * @param tableName Name of the table
	 * @param property  Property
	 * @throws DataServiceFault
	 */
	void updatePropertyInTable(String tableName, DataEntry property) throws DataServiceFault;

	/**
	 * This method return names of all the tables in the database.
	 *
	 * @return Table list.
	 */
	List<String> getTableList();

	/**
	 * This method returns the all the primary keys in the database tables.
	 * Return a map with table name as the keys, and the values contains a list of column names which are act as primary keys in the table.
	 *
	 * @return Primary Key Map
	 */
	Map<String, List<String>> getPrimaryKeys();

	/**
	 * This method returns the navigation property map, which contains the foreign keys of the tables.
	 * Return a map with table names as the keys and the values contains maps with table names as keys and
	 *
	 * @return NavigationProperty Map
	 */
	Map<String, Map<String, List<String>>> getNavigationProperties();
}
