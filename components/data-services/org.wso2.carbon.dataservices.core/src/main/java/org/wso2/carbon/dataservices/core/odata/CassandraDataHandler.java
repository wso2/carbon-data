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

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.TableMetadata;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.engine.DataEntry;
import org.wso2.carbon.dataservices.core.engine.ParamValue;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class implements cassandra datasource related operations for ODataDataHandler.
 *
 * @see ODataDataHandler
 * @see AbstractExpressionDataHandler
 */
public class CassandraDataHandler extends AbstractExpressionDataHandler {

	/**
	 * List of Tables in the Database.
	 */
	private List<String> tableList;

	/**
	 * Cassandra session.
	 */
	private Session session;

	/**
	 * Cassandra keyspace.
	 */
	private String keyspace;

	public CassandraDataHandler(String configID, Session session, String keyspace) throws DataServiceFault {
		this.configID = configID;
		this.session = session;
		this.keyspace = keyspace;
		this.tableList = generateTableList();
		this.primaryKeys = generatePrimaryKeyList();
		this.tableMetaData = generateMetaData();
	}

	@Override
	public List<DataEntry> readTable(String tableName) throws DataServiceFault {
		Statement statement = new SimpleStatement("Select * from " + keyspace + "." + tableName);
		ResultSet resultSet = this.session.execute(statement);
		Iterator<Row> iterator = resultSet.iterator();
		List<DataEntry> entryList = new ArrayList<>();
		ColumnDefinitions columnDefinitions = resultSet.getColumnDefinitions();
		while (iterator.hasNext()) {
			DataEntry dataEntry = createDataEntryFromRow(tableName, iterator.next(), columnDefinitions);
			entryList.add(dataEntry);
		}
		return entryList;
	}

	@Override
	public List<DataEntry> readTableWithKeys(String tableName, DataEntry keys) throws DataServiceFault {
		List<String> pKeys = this.primaryKeys.get(tableName);
		String query = createReadSqlWithKeys(tableName, keys);
		List<Object> values = new ArrayList<>(getBindingKeyCount(query));
		for (String column : keys.getNames()) {
			if (this.tableMetaData.get(tableName).keySet().contains(column) && pKeys.contains(column)) {
				values = bindParams(this.tableMetaData.get(tableName).get(column).getColumnType(),
				                    keys.getValue(column).getScalarValue(), values);
			}
		}
		Statement statement = new SimpleStatement(query, values.toArray());
		ResultSet resultSet = this.session.execute(statement);
		List<DataEntry> entryList = new ArrayList<>();
		Iterator<Row> iterator = resultSet.iterator();
		ColumnDefinitions defs = resultSet.getColumnDefinitions();
		while (iterator.hasNext()) {
			DataEntry dataEntry = createDataEntryFromRow(tableName, iterator.next(), defs);
			entryList.add(dataEntry);
		}
		return entryList;
	}

	@Override
	public String insertEntityInTable(String tableName, DataEntry entity) throws DataServiceFault {
		String query = createInsertSQL(tableName, entity);
		List<Object> values = new ArrayList<>(getBindingKeyCount(query));
		for (DataColumn column : this.tableMetaData.get(tableName).values()) {
			if (entity.getNames().contains(column.getColumnName()) &&
			    entity.getValue(column.getColumnName()).getScalarValue() != null) {
				values = bindParams(column.getColumnType(), entity.getValue(column.getColumnName()).getScalarValue(),
				                    values);
			}
		}
		Statement statement = new SimpleStatement(query, values.toArray());
		this.session.execute(statement);
		return generateETag(tableName, entity);
	}

	@Override
	public void deleteEntityInTable(String tableName, DataEntry entity) throws DataServiceFault {
		List<String> pKeys = this.primaryKeys.get(tableName);
		String query = createDeleteSQL(tableName, entity);
		List<Object> values = new ArrayList<>(getBindingKeyCount(query));
		for (String column : entity.getNames()) {
			if (this.tableMetaData.get(tableName).keySet().contains(column) && pKeys.contains(column)) {
				values = bindParams(this.tableMetaData.get(tableName).get(column).getColumnType(),
				                    entity.getValue(column).getScalarValue(), values);
			}
		}
		Statement statement = new SimpleStatement(query, values.toArray());
		this.session.execute(statement);
	}

	@Override
	public void updateEntityInTable(String tableName, DataEntry newProperties) throws DataServiceFault {
		List<String> pKeys = this.primaryKeys.get(tableName);
		String query = createUpdateEntitySQL(tableName, newProperties);
		List<Object> values = new ArrayList<>(getBindingKeyCount(query));
		for (String column : newProperties.getNames()) {
			if (this.tableMetaData.get(tableName).keySet().contains(column) && !pKeys.contains(column) &&
			    newProperties.getValue(column).getScalarValue() != null) {
				values = bindParams(this.tableMetaData.get(tableName).get(column).getColumnType(),
				                    newProperties.getValue(column).getScalarValue(), values);
			}
		}
		for (String column : newProperties.getNames()) {
			if (this.tableMetaData.get(tableName).keySet().contains(column) && pKeys.contains(column)) {
				values = bindParams(this.tableMetaData.get(tableName).get(column).getColumnType(),
				                    newProperties.getValue(column).getScalarValue(), values);
			}
		}
		Statement statement = new SimpleStatement(query, values.toArray());
		this.session.execute(statement);
	}

	@Override
	public Map<String, Map<String, DataColumn>> getTableMetadata() {
		return this.tableMetaData;
	}

	@Override
	public void updatePropertyInTable(String tableName, DataEntry property) throws DataServiceFault {
		DataColumn column = this.tableMetaData.get(tableName).get(property.getValue("propertyName").getScalarValue());
		if (column != null) {
			String sql =
					"UPDATE " + tableName + " SET " + property.getValue("propertyName").getScalarValue() + "=" + "?";
			List<Object> values = new ArrayList<>(1);
			values = bindParams(column.getColumnType(), property.getValue("propertyValue").getScalarValue(), values);
			Statement statement = new SimpleStatement(sql, values.toArray());
			session.execute(statement);
		} else {
			throw new DataServiceFault("Property didn't found to update");
		}
	}

	@Override
	public List<String> getTableList() {
		return tableList;
	}

	@Override
	public Map<String, List<String>> getPrimaryKeys() {
		return this.primaryKeys;
	}

	@Override
	public Map<String, Map<String, List<String>>> getNavigationProperties() {
		return null;
	}

	/**
	 * This method wraps result set data in to DataEntry and creates a list of DataEntry.
	 *
	 * @param tableName         Table Name
	 * @param row               Row
	 * @param columnDefinitions Column Definition
	 * @return DataEntry
	 * @throws DataServiceFault
	 */
	private DataEntry createDataEntryFromRow(String tableName, Row row, ColumnDefinitions columnDefinitions)
			throws DataServiceFault {
		List<String> pKeys = this.primaryKeys.get(tableName);
		StringBuilder uniqueString = new StringBuilder();
		ParamValue paramValue;
		DataEntry entry = new DataEntry();
		uniqueString.append(this.configID).append(tableName);
		//Creating a unique string to represent the
		for (int i = 0; i < columnDefinitions.size(); i++) {
			String columnName = columnDefinitions.getName(i);
			DataType columnType = columnDefinitions.getType(i);
			uniqueString.append(columnName);
			if (columnType.getName().equals(DataType.Name.ASCII)) {
				paramValue = new ParamValue(row.getString(i));
			} else if (columnType.getName().equals(DataType.Name.VARCHAR)) {
				paramValue = new ParamValue(row.getString(i));
			} else if (columnType.getName().equals(DataType.Name.TEXT)) {
				paramValue = new ParamValue(row.getString(i));
			} else if (columnType.getName().equals(DataType.Name.BIGINT)) {
				paramValue = new ParamValue(row.isNull(i) ? null : ConverterUtil.convertToString(row.getLong(i)));
			} else if (columnType.getName().equals(DataType.Name.BLOB)) {
				paramValue = new ParamValue(this.base64EncodeByteBuffer(row.getBytes(i)));
			} else if (columnType.getName().equals(DataType.Name.BOOLEAN)) {
				paramValue = new ParamValue(row.isNull(i) ? null : ConverterUtil.convertToString(row.getBool(i)));
			} else if (columnType.getName().equals(DataType.Name.COUNTER)) {
				paramValue = new ParamValue(row.isNull(i) ? null : ConverterUtil.convertToString(row.getLong(i)));
			} else if (columnType.getName().equals(DataType.Name.CUSTOM)) {
				paramValue = new ParamValue(this.base64EncodeByteBuffer(row.getBytes(i)));
			} else if (columnType.getName().equals(DataType.Name.DECIMAL)) {
				paramValue = new ParamValue(row.isNull(i) ? null : ConverterUtil.convertToString(row.getDecimal(i)));
			} else if (columnType.getName().equals(DataType.Name.DOUBLE)) {
				paramValue = new ParamValue(row.isNull(i) ? null : ConverterUtil.convertToString(row.getDouble(i)));
			} else if (columnType.getName().equals(DataType.Name.FLOAT)) {
				paramValue = new ParamValue(row.isNull(i) ? null : ConverterUtil.convertToString(row.getFloat(i)));
			} else if (columnType.getName().equals(DataType.Name.INET)) {
				paramValue = new ParamValue(row.getInet(i).toString());
			} else if (columnType.getName().equals(DataType.Name.INT)) {
				paramValue = new ParamValue(row.isNull(i) ? null : ConverterUtil.convertToString(row.getInt(i)));
			} else if (columnType.getName().equals(DataType.Name.LIST)) {
				paramValue = new ParamValue(Arrays.toString(row.getList(i, Object.class).toArray()));
			} else if (columnType.getName().equals(DataType.Name.MAP)) {
				paramValue = new ParamValue(row.getMap(i, Object.class, Object.class).toString());
			} else if (columnType.getName().equals(DataType.Name.SET)) {
				paramValue = new ParamValue(row.getSet(i, Object.class).toString());
			} else if (columnType.getName().equals(DataType.Name.TIMESTAMP)) {
				paramValue = new ParamValue(ConverterUtil.convertToString(row.getDate(i)));
			} else if (columnType.getName().equals(DataType.Name.TIMEUUID)) {
				paramValue = new ParamValue(ConverterUtil.convertToString(row.getUUID(i)));
			} else if (columnType.getName().equals(DataType.Name.UUID)) {
				paramValue = new ParamValue(ConverterUtil.convertToString(row.getUUID(i)));
			} else if (columnType.getName().equals(DataType.Name.VARINT)) {
				paramValue = new ParamValue(ConverterUtil.convertToString(row.getVarint(i)));
			} else {
				paramValue = new ParamValue(row.getString(i));
			}
			entry.addValue(columnName, paramValue);
			if (pKeys.contains(columnName)) {
				uniqueString.append(columnName).append(paramValue.getScalarValue());
			}
			if (pKeys.isEmpty()) {
				uniqueString.append(columnName).append(paramValue.getScalarValue());
			}
		}
		//Set E-Tag to the entity
		entry.addValue("ETag", new ParamValue(UUID.nameUUIDFromBytes((uniqueString.toString()).getBytes()).toString()));
		//Set to default
		uniqueString.setLength(0);
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

	private List<String> generateTableList() {
		List<String> tableList = new ArrayList<>();
		for (TableMetadata tableMetadata : session.getCluster().getMetadata().getKeyspace(keyspace).getTables()) {
			tableList.add(tableMetadata.getName());
		}
		return tableList;
	}

	private Map<String, List<String>> generatePrimaryKeyList() {
		Map<String, List<String>> primaryKeyMap = new HashMap<>();
		for (String tableName : this.tableList) {
			List<String> primaryKey = new ArrayList<>();
			for (ColumnMetadata columnMetadata : this.session.getCluster().getMetadata().getKeyspace(this.keyspace)
			                                                 .getTable(tableName).getPrimaryKey()) {
				primaryKey.add(columnMetadata.getName());
			}
			primaryKeyMap.put(tableName, primaryKey);
		}
		return primaryKeyMap;
	}

	private Map<String, Map<String, DataColumn>> generateMetaData() throws DataServiceFault {
		Map<String, Map<String, DataColumn>> metadata = new HashMap<>();
		for (String tableName : this.tableList) {
			Map<String, DataColumn> dataColumnMap = new HashMap<>();
			for (ColumnMetadata columnMetadata : this.session.getCluster().getMetadata().getKeyspace(this.keyspace)
			                                                 .getTable(tableName).getColumns()) {
				DataColumn dataColumn;
				if (this.primaryKeys.get(tableName).contains(columnMetadata.getName())) {
					dataColumn =
							new DataColumn(columnMetadata.getName(), getDataType(columnMetadata.getType().getName()),
							               false);
				} else {
					dataColumn =
							new DataColumn(columnMetadata.getName(), getDataType(columnMetadata.getType().getName()),
							               true);
				}
				dataColumnMap.put(dataColumn.getColumnName(), dataColumn);
			}
			metadata.put(tableName, dataColumnMap);
		}
		return metadata;
	}

	private List<Object> bindParams(int type, String value, List<Object> values) throws DataServiceFault {
		switch (type) {
			case Types.VARCHAR:
				values.add(value);
				break;
			case Types.BIGINT:
				values.add(value == null ? null : Long.parseLong(value));
				break;
			case Types.BLOB:
				values.add(value == null ? null : this.base64DecodeByteBuffer(value));
				break;
			case Types.BOOLEAN:
				values.add(value == null ? null : Boolean.parseBoolean(value));
				break;
			case Types.DECIMAL:
				values.add(new BigDecimal(value));
				break;
			case Types.DOUBLE:
				values.add(Double.parseDouble(value));
				break;
			case Types.FLOAT:
				values.add(Float.parseFloat(value));
				break;
			case Types.INTEGER:
				values.add(Integer.parseInt(value));
				break;
			case Types.TIMESTAMP:
				values.add(DBUtils.getDate(value));
				break;
		}
		return values;
	}

	private int getDataType(DataType.Name dataTypeName) throws DataServiceFault {
		int dataType;
		switch (dataTypeName) {
			case ASCII:
			case TEXT:
			case VARCHAR:
			case TIMEUUID:
			case UUID:
				dataType = Types.VARCHAR;
				break;
			case BIGINT:
			case VARINT:
			case COUNTER:
				dataType = Types.BIGINT;
				break;
			case BLOB:
				dataType = Types.BLOB;
				break;
			case BOOLEAN:
				dataType = Types.BOOLEAN;
				break;
			case DECIMAL:
				dataType = Types.DECIMAL;
				break;
			case DOUBLE:
				dataType = Types.DOUBLE;
				break;
			case FLOAT:
				dataType = Types.FLOAT;
				break;
			case INET:
				throw new DataServiceFault("INET Data Type is not supported for OData Services");
			case INT:
				dataType = Types.INTEGER;
				break;
			case TIMESTAMP:
				dataType = Types.TIMESTAMP;
				break;
			case LIST:
				throw new DataServiceFault("LIST Data Type is not supported for OData Services");
			case SET:
				throw new DataServiceFault("SET Data Type is not supported for OData Services");
			case MAP:
				throw new DataServiceFault("MAP Data Type is not supported for OData Services");
			case UDT:
				throw new DataServiceFault("UDT Data Type is not supported for OData Services");
			case TUPLE:
				throw new DataServiceFault("TUPLE Data Type is not supported for OData Services");
			case CUSTOM:
				throw new DataServiceFault("CUSTOM Data Type is not supported for OData Services");
			default:
				throw new DataServiceFault("Data Type is not supported for OData Services");
		}
		return dataType;
	}

	private int getBindingKeyCount(String query) {
		int count = 0;
		for (int i = 0; i < query.length(); i++) {
			if (i != 0 && i != query.length() - 1) {
				if (query.charAt(i) == '?' && query.charAt(i - 1) == ' ' && query.charAt(i - 1) == ' ') {
					count++;
				}
			} else {
				if (query.charAt(i) == '?') {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * This method creates a SQL query to update data.
	 *
	 * @param tableName Name of the table
	 * @param entry     update entry
	 * @return sql Query
	 * @throws DataServiceFault
	 */
	private String createUpdateEntitySQL(String tableName, DataEntry entry) throws DataServiceFault {
		List<String> pKeys = primaryKeys.get(tableName);
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(tableName).append(" SET ");
		boolean propertyMatch = false;
		for (DataColumn column : tableMetaData.get(tableName).values()) {
			if (entry.getValue(column.getColumnName()).getScalarValue() != null &&
			    !pKeys.contains(column.getColumnName())) {
				if (propertyMatch) {
					sql.append(",");
				}
				if (!pKeys.contains(column.getColumnName())) {
					sql.append(column.getColumnName()).append(" = ").append(" ? ");
					propertyMatch = true;
				}
			}
		}
		sql.append(" WHERE ");
		// Handling keys
		propertyMatch = false;
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
	 * This method creates a SQL query to insert data in table.
	 *
	 * @param tableName Name of the table
	 * @return sqlQuery
	 */
	private String createInsertSQL(String tableName, DataEntry entry) throws DataServiceFault {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(tableName).append(" (");
		boolean propertyMatch = false;
		for (DataColumn column : tableMetaData.get(tableName).values()) {
			if (entry.getValue(column.getColumnName()).getScalarValue() != null) {
				if (propertyMatch) {
					sql.append(",");
				}
				sql.append(column.getColumnName());
				propertyMatch = true;
			}
		}
		sql.append(" ) VALUES ( ");
		propertyMatch = false;
		for (DataColumn column : tableMetaData.get(tableName).values()) {
			if (propertyMatch) {
				sql.append(",");
			}
			sql.append(" ? ");
			propertyMatch = true;
		}
		sql.append(" ) ");
		return sql.toString();
	}
}
