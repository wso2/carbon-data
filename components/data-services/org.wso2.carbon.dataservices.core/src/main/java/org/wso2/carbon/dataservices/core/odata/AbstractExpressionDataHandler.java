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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * //todo add class level cooments
 */
public abstract class AbstractExpressionDataHandler implements ODataDataHandler {

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
	 * This method creates a SQL query to insert data.
	 *
	 * @param tableName Name of the table
	 * @return sqlQuery
	 */
	protected String createInsertSQL(String tableName) throws DataServiceFault {
		List<DataColumn> columns = new ArrayList<>(tableMetaData.get(tableName).values());
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(tableName).append(" (");
		for (int i = 0; i < columns.size(); i++) {
			if (i != 0) {
				sql.append(",");
			}
			sql.append(columns.get(i).getColumnName());
		}
		sql.append(" ) VALUES (");
		for (int i = 0; i < columns.size(); i++) {
			if (i != 0) {
				sql.append(",");
			}
			sql.append("?");
		}
		sql.append(" )");
		return sql.toString();
	}

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
		List<DataColumn> columns = new ArrayList<>(tableMetaData.get(tableName).values());
		sql.append("SELECT * FROM ").append(tableName).append(" WHERE ");
		boolean propertyMatch = false;
		for (int i = 0; i < columns.size(); i++) {
			if (keys.getValue(columns.get(i).getColumnName()) != null) {
				if (i != 0 && propertyMatch) {
					sql.append(" AND ");
				}
				sql.append(columns.get(i).getColumnName()).append(" = ").append("?");
				propertyMatch = true;
			}
		}
		return sql.toString();
	}

	/**
	 * This method creates a SQL query to update data.
	 *
	 * @param tableName Name of the table
	 * @param entry     update entry
	 * @return sql Query
	 * @throws DataServiceFault
	 */
	protected String createUpdateEntitySQL(String tableName, DataEntry entry) throws DataServiceFault {
		List<DataColumn> columns = new ArrayList<>(tableMetaData.get(tableName).values());
		List<String> pKeys = primaryKeys.get(tableName);
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(tableName).append(" SET ");
		boolean propertyMatch = false;
		for (int i = 0; i < columns.size(); i++) {
			if (entry.getValue(columns.get(i).getColumnName()).getScalarValue() != null) {
				if (i != 0 && propertyMatch) {
					sql.append(",");
				}
				sql.append(columns.get(i).getColumnName()).append("=").append("?");
				propertyMatch = true;
			}
		}
		sql.append(" WHERE ");
		// Handling keys
		if (pKeys.size() > 0) {
			for (int m = 0; m < pKeys.size(); m++) {
				if (m != 0) {
					sql.append(",");
				}
				sql.append(pKeys.get(m)).append("=").append("?");
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
		List<DataColumn> columns = new ArrayList<>(tableMetaData.get(tableName).values());
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ").append(tableName).append(" WHERE ");
		boolean propertyMatch = false;
		for (int i = 0; i < columns.size(); i++) {
			if (deletionEntry.getValue(columns.get(i).getColumnName()) != null) {
				if (i != 0 && propertyMatch) {
					sql.append(" AND ");
				}
				propertyMatch = true;
				sql.append(columns.get(i).getColumnName()).append("=").append("?");
			}
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
