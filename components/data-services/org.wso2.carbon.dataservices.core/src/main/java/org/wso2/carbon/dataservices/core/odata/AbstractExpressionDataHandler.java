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
import java.util.UUID;

/**
 * This class implements related operations for ODataDataHandler.
 *
 * @see ODataDataHandler
 */
public abstract class AbstractExpressionDataHandler implements ODataDataHandler {

	/**
	 * Table metadata.
	 */
	protected Map<String, Map<String, DataColumn>> tableMetaData;

	/**
	 * Primary Keys of the Tables (Map<Table Name, List>).
	 */
	protected Map<String, List<String>> primaryKeys;

	/**
	 * Config ID.
	 */
	protected String configID;

	/**
	 * This method creates SQL query to read data with keys.
	 *
	 * @param tableName Name of the table
	 * @param keys      Keys
	 * @return sql Query
	 * @throws DataServiceFault
	 */
	protected String createReadSqlWithKeys(String tableName, DataEntry keys) throws DataServiceFault {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(tableName).append(" WHERE ");
		boolean propertyMatch = false;
		for (DataColumn column : tableMetaData.get(tableName).values()) {
			if (keys.getValue(column.getColumnName()) != null) {
				if (propertyMatch) {
					sql.append(" AND ");
				}
				sql.append(column.getColumnName()).append(" = ").append(" ? ");
				propertyMatch = true;
			}
		}
		return sql.toString();
	}

	/**
	 * This method creates SQL query to delete data.
	 *
	 * @param tableName     Name of the table
	 * @param deletionEntry deletion Entry
	 * @return sql Query
	 * @throws DataServiceFault
	 */
	protected String createDeleteSQL(String tableName, DataEntry deletionEntry) throws DataServiceFault {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ").append(tableName).append(" WHERE ");
		List<String> pKeys = primaryKeys.get(tableName);
		boolean propertyMatch = false;
		for (String key : pKeys) {
			if (propertyMatch) {
				sql.append(" AND ");
			}
			sql.append(key).append(" = ").append(" ? ");
			propertyMatch = true;
		}
		return sql.toString();
	}

	/**
	 * This method generates an unique ETag for each data row entry.
	 *
	 * @param tableName Name of the table
	 * @param entry     Data row entry
	 * @return E Tag
	 */
	protected String generateETag(String tableName, DataEntry entry) {
		StringBuilder uniqueString = new StringBuilder();
		List<String> pKeys = primaryKeys.get(tableName);
		uniqueString.append(configID).append(tableName);
		for (String columnName : entry.getNames()) {
			if (pKeys.contains(columnName)) {
				uniqueString.append(columnName).append(entry.getValue(columnName).getScalarValue());
			}
			if (pKeys.isEmpty()) {
				uniqueString.append(columnName).append(entry.getValue(columnName).getScalarValue());
			}
		}
		return UUID.nameUUIDFromBytes((uniqueString.toString()).getBytes()).toString();
	}
}
