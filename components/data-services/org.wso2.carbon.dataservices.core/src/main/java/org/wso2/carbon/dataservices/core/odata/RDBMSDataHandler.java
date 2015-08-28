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

import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.engine.DataEntry;
import org.wso2.carbon.dataservices.core.engine.ParamValue;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class implements RDBMS datasource related operations for ODataDataHandler.
 *
 * @see ODataDataHandler
 * @see AbstractExpressionDataHandler
 */
public class RDBMSDataHandler extends AbstractExpressionDataHandler {

	/**
	 * RDBMS datasource.
	 */
	private DataSource dataSource;

	/**
	 * List of Tables in the Database.
	 */
	private List<String> tableList;

	/**
	 * Navigation properties map <Target Table Name, Map<Source Table Name, List<String>).
	 */
	private Map<String, Map<String, List<String>>> navigationProperties;

	public RDBMSDataHandler(DataSource dataSource, String configId, String namespace) throws DataServiceFault {
		this.dataSource = dataSource;
		this.tableList = generateTableList();
		this.configID = configId;
		initializeMetaData();
	}

	@Override
	public Map<String, Map<String, List<String>>> getNavigationProperties() {
		return this.navigationProperties;
	}

	@Override
	public List<DataEntry> readTable(String tableName) throws DataServiceFault {
		ResultSet resultSet = null;
		Connection connection = null;
		Statement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery("select * from " + tableName);
			return createDataEntryCollectionFromRS(tableName, resultSet);
		} catch (SQLException e) {
			throw new DataServiceFault(e, "Error in reading the entities from " + tableName + " table.");
		} finally {
			releaseResources(resultSet, statement, connection);
		}
	}

	@Override
	public List<String> getTableList() {
		return this.tableList;
	}

	@Override
	public Map<String, List<String>> getPrimaryKeys() {
		return this.primaryKeys;
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

	@Override
	public String insertEntityInTable(String tableName, DataEntry entry) throws DataServiceFault {
		Connection connection = null;
		PreparedStatement sql = null;
		try {
			connection = this.dataSource.getConnection();
			sql = connection.prepareStatement(createInsertSQL(tableName));
			int index = 1;
			for (DataColumn column : this.tableMetaData.get(tableName).values()) {
				String value = entry.getValue(column.getColumnName()).getScalarValue();
				sql = bindValuesToPreparedStatement(column.getColumnType(), value, index, sql);
				index++;
			}
			sql.execute();
			return generateETag(tableName, entry);
		} catch (SQLException | ParseException e) {
			throw new DataServiceFault(e, "Error in writing the entities from table.");
		} finally {
			releaseResources(null, sql, connection);
		}
	}

	@Override
	public List<DataEntry> readTableWithKeys(String tableName, DataEntry keys) throws DataServiceFault {
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(createReadSqlWithKeys(tableName, keys));
			int index = 1;
			for (String key : keys.getNames()) {
				String value =
						keys.getValue(this.tableMetaData.get(tableName).get(key).getColumnName()).getScalarValue();
				statement =
						bindValuesToPreparedStatement(this.tableMetaData.get(tableName).get(key).getColumnType(), value,
						                              index, statement);
				index++;
			}
			resultSet = statement.executeQuery();
			return createDataEntryCollectionFromRS(tableName, resultSet);
		} catch (SQLException | ParseException e) {
			throw new DataServiceFault(e, "Error in reading the entities from table.");
		} finally {
			releaseResources(resultSet, statement, connection);
		}
	}

	/**
	 * This method bind values to prepared statement.
	 *
	 * @param type            data Type
	 * @param value           String value
	 * @param ordinalPosition Ordinal Position
	 * @param sqlStatement    Statement
	 * @return Prepared Statement
	 * @throws SQLException
	 * @throws ParseException
	 */
	private PreparedStatement bindValuesToPreparedStatement(int type, String value, int ordinalPosition,
	                                                        PreparedStatement sqlStatement)
			throws SQLException, ParseException, DataServiceFault {
		byte[] data;
		switch (type) {
			case Types.INTEGER:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setInt(ordinalPosition, ConverterUtil.convertToInt(value));
				}
				break;
			case Types.TINYINT:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setByte(ordinalPosition, ConverterUtil.convertToByte(value));
				}
				break;
			case Types.SMALLINT:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setShort(ordinalPosition, ConverterUtil.convertToShort(value));
				}
				break;
			case Types.DOUBLE:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setDouble(ordinalPosition, ConverterUtil.convertToDouble(value));
				}
				break;
			case Types.VARCHAR:
			case Types.CHAR:
			case Types.LONGVARCHAR:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setString(ordinalPosition, value);
				}
				break;
			case Types.CLOB:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setClob(ordinalPosition, new BufferedReader(new StringReader(value)), value.length());
				}
				break;
			case Types.BOOLEAN:
			case Types.BIT:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setBoolean(ordinalPosition, ConverterUtil.convertToBoolean(value));
				}
				break;
			case Types.BLOB:
			case Types.LONGVARBINARY:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					data = this.getBytesFromBase64String(value);
					sqlStatement.setBlob(ordinalPosition, new ByteArrayInputStream(data), data.length);
				}
				break;
			case Types.BINARY:
			case Types.VARBINARY:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					data = this.getBytesFromBase64String(value);
					sqlStatement.setBinaryStream(ordinalPosition, new ByteArrayInputStream(data), data.length);
				}
				break;
			case Types.DATE:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setDate(ordinalPosition, DBUtils.getDate(value));
				}
				break;
			case Types.DECIMAL:
			case Types.NUMERIC:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setBigDecimal(ordinalPosition, ConverterUtil.convertToBigDecimal(value));
				}
				break;
			case Types.FLOAT:
			case Types.REAL:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setFloat(ordinalPosition, ConverterUtil.convertToFloat(value));
				}
				break;
			case Types.TIME:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setTime(ordinalPosition, DBUtils.getTime(value));
				}
				break;
			case Types.LONGNVARCHAR:
			case Types.NCHAR:
			case Types.NVARCHAR:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setNString(ordinalPosition, value);
				}
				break;
			case Types.NCLOB:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setNClob(ordinalPosition, new BufferedReader(new StringReader(value)), value.length());
				}
				break;
			case Types.BIGINT:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setLong(ordinalPosition, ConverterUtil.convertToLong(value));
				}
				break;
			case Types.TIMESTAMP:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setTimestamp(ordinalPosition, DBUtils.getTimestamp(value));
				}
				break;
			default:
				if (value == null) {
					sqlStatement.setNull(ordinalPosition, type);
				} else {
					sqlStatement.setString(ordinalPosition, value);
				}
				break;
		}
		return sqlStatement;
	}

	private byte[] getBytesFromBase64String(String base64Str) throws SQLException {
		try {
			return Base64.decodeBase64(base64Str.getBytes(DBConstants.DEFAULT_CHAR_SET_TYPE));
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	}

	@Override
	public void updateEntityInTable(String tableName, DataEntry newProperties) throws DataServiceFault {
		List<String> pKeys = this.primaryKeys.get(tableName);
		Connection connection = null;
		PreparedStatement statement = null;
		String value;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(createUpdateEntitySQL(tableName, newProperties));
			int index = 1;
			for (DataColumn column : this.tableMetaData.get(tableName).values()) {
				if (!pKeys.contains(column.getColumnName())) {
					value = newProperties.getValue(column.getColumnName()).getScalarValue();
					statement = bindValuesToPreparedStatement(column.getColumnType(), value, index, statement);
					index++;
				}
			}
			for (DataColumn column : this.tableMetaData.get(tableName).values()) {
				if (!pKeys.isEmpty()) {
					if (pKeys.contains(column.getColumnName())) {
						value = newProperties.getValue(column.getColumnName()).getScalarValue();
						statement = bindValuesToPreparedStatement(column.getColumnType(), value, index, statement);
						index++;
					}
				} else {
					throw new DataServiceFault("Error in updating the entity to table.");
				}
			}
			statement.execute();
		} catch (SQLException | ParseException e) {
			throw new DataServiceFault(e, "Error in updating the entity to table.");
		} finally {
			releaseResources(null, statement, connection);
		}
	}

	@Override
	public void deleteEntityInTable(String tableName, DataEntry entry) throws DataServiceFault {
		Connection connection = null;
		PreparedStatement statement = null;
		List<String> pKeys = this.primaryKeys.get(tableName);
		String value;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(createDeleteSQL(tableName, entry));
			int index = 1;
			for (DataColumn column : this.tableMetaData.get(tableName).values()) {
				if (pKeys.contains(column.getColumnName())) {
					value = entry.getValue(column.getColumnName()).getScalarValue();
					statement = bindValuesToPreparedStatement(column.getColumnType(), value, index, statement);
					index++;
				}
			}
			statement.execute();
		} catch (SQLException | ParseException e) {
			throw new DataServiceFault(e, "Error in deleting the entities to table.");
		} finally {
			releaseResources(null, statement, connection);
		}
	}

	/**
	 * This method wraps result set data in to DataEntry and creates a list of DataEntry.
	 *
	 * @param tableName Name of the table
	 * @param resultSet Result set
	 * @return List of DataEntry
	 * @throws DataServiceFault
	 * @see DataEntry
	 */
	private List<DataEntry> createDataEntryCollectionFromRS(String tableName, ResultSet resultSet)
			throws DataServiceFault {
		List<DataEntry> entitySet = new ArrayList<>();
		List<String> pKeys = this.primaryKeys.get(tableName);
		StringBuilder uniqueString = new StringBuilder();
		String value;
		try {
			ParamValue paramValue;
			while (resultSet.next()) {
				DataEntry entry = new DataEntry();
				uniqueString.append(this.configID).append(tableName);
				//Creating a unique string to represent the
				for (DataColumn column : this.tableMetaData.get(tableName).values()) {
					String columnName = column.getColumnName();
					int columnType = column.getColumnType();
					//need to map with dataTypes
					switch (columnType) {
						case Types.INTEGER:
						case Types.TINYINT:
						case Types.SMALLINT:
							value = ConverterUtil.convertToString(resultSet.getInt(columnName));
							paramValue = new ParamValue(resultSet.wasNull() ? null : value);
							break;
						case Types.DOUBLE:
							value = ConverterUtil.convertToString(resultSet.getDouble(columnName));
							paramValue = new ParamValue(resultSet.wasNull() ? null : value);
							break;
						case Types.VARCHAR:
						case Types.CHAR:
						case Types.CLOB:
						case Types.LONGVARCHAR:
							value = resultSet.getString(columnName);
							paramValue = new ParamValue(value);
							break;
						case Types.BOOLEAN:
						case Types.BIT:
							value = ConverterUtil.convertToString(resultSet.getBoolean(columnName));
							paramValue = new ParamValue(resultSet.wasNull() ? null : value);
							break;
						case Types.BLOB:
							Blob sqlBlob = resultSet.getBlob(columnName);
							if (sqlBlob != null) {
								value = this.getBase64StringFromInputStream(sqlBlob.getBinaryStream());
							} else {
								value = null;
							}
							paramValue = new ParamValue(resultSet.wasNull() ? null : value);
							break;
						case Types.BINARY:
						case Types.LONGVARBINARY:
						case Types.VARBINARY:
							InputStream binInStream = resultSet.getBinaryStream(columnName);
							if (binInStream != null) {
								value = this.getBase64StringFromInputStream(binInStream);
							} else {
								value = null;
							}
							paramValue = new ParamValue(value);
							break;
						case Types.DATE:
							Date sqlDate = resultSet.getDate(columnName);
							if (sqlDate != null) {
								value = ConverterUtil.convertToString(sqlDate);
							} else {
								value = null;
							}
							paramValue = new ParamValue(value);
							break;
						case Types.DECIMAL:
						case Types.NUMERIC:
							BigDecimal bigDecimal = resultSet.getBigDecimal(columnName);
							if (bigDecimal != null) {
								value = ConverterUtil.convertToString(bigDecimal);
							} else {
								value = null;
							}
							paramValue = new ParamValue(resultSet.wasNull() ? null : value);
							break;
						case Types.FLOAT:
							value = ConverterUtil.convertToString(resultSet.getFloat(columnName));
							paramValue = new ParamValue(resultSet.wasNull() ? null : value);
							break;
						case Types.TIME:
							Time sqlTime = resultSet.getTime(columnName);
							if (sqlTime != null) {
								value = this.convertToTimeString(sqlTime);
							} else {
								value = null;
							}
							paramValue = new ParamValue(value);
							break;
						case Types.LONGNVARCHAR:
						case Types.NCHAR:
						case Types.NCLOB:
						case Types.NVARCHAR:
							value = resultSet.getNString(columnName);
							paramValue = new ParamValue(value);
							break;
						case Types.BIGINT:
							value = ConverterUtil.convertToString(resultSet.getLong(columnName));
							paramValue = new ParamValue(resultSet.wasNull() ? null : value);
							break;
						case Types.TIMESTAMP:
							Timestamp sqlTimestamp = resultSet.getTimestamp(columnName);
							if (sqlTimestamp != null) {
								value = this.convertToTimestampString(sqlTimestamp);
							} else {
								value = null;
							}
							paramValue = new ParamValue(resultSet.wasNull() ? null : value);
							break;
						/* handle all other types as strings */
						default:
							value = resultSet.getString(columnName);
							paramValue = new ParamValue(resultSet.wasNull() ? null : value);
							break;
					}
					entry.addValue(columnName, paramValue);
					if (pKeys.contains(columnName)) {
						uniqueString.append(columnName).append(paramValue.getScalarValue());
					}
					if (pKeys.isEmpty()) {
						uniqueString.append(columnName).append(paramValue.getScalarValue());
					}
				}
				//Set Etag to the entity
				entry.addValue("ETag",
				               new ParamValue(UUID.nameUUIDFromBytes((uniqueString.toString()).getBytes()).toString()));
				entitySet.add(entry);
				//Set to default
				uniqueString.setLength(0);
			}
			return entitySet;
		} catch (SQLException e) {
			throw new DataServiceFault(e, "Error in writing the entities to table.");
		}
	}

	private void releaseResources(ResultSet resultSet, Statement statement, Connection connection) {
	    /* close the result set */
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (Exception ignore) {
				// ignore
			}
		}
		/* close the statement */
		if (statement != null) {
			try {
				statement.close();
			} catch (Exception ignore) {
				// ignore
			}
		}
		/* close the connection */
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception ignore) {
				// ignore
			}
		}
	}

	private String getBase64StringFromInputStream(InputStream in) throws SQLException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		String strData;
		try {
			byte[] buff = new byte[512];
			int i;
			while ((i = in.read(buff)) > 0) {
				byteOut.write(buff, 0, i);
			}
			in.close();
			byte[] base64Data = Base64.encodeBase64(byteOut.toByteArray());
			if (base64Data != null) {
				strData = new String(base64Data, DBConstants.DEFAULT_CHAR_SET_TYPE);
			} else {
				strData = null;
			}
			return strData;
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	}

	/**
	 * This method reads table column meta data.
	 *
	 * @param tableName Name of the table
	 * @return table MetaData
	 * @throws DataServiceFault
	 */
	private Map<String, DataColumn> readTableColumnMetaData(String tableName, DatabaseMetaData meta)
			throws DataServiceFault {
		ResultSet resultSet = null;
		Map<String, DataColumn> columnMap = new HashMap<>();
		try {
			resultSet = meta.getColumns(null, null, tableName, null);
			int i = 1;
			while (resultSet.next()) {
				String columnName = resultSet.getString("COLUMN_NAME");
				int columnType = resultSet.getInt("DATA_TYPE");
				int size = resultSet.getInt("COLUMN_SIZE");
				boolean nullable = resultSet.getBoolean("NULLABLE");
				String columnDefaultVal = resultSet.getString("COLUMN_DEF");
				int precision = resultSet.getMetaData().getPrecision(i);
				int scale = resultSet.getMetaData().getScale(i);
				DataColumn column = new DataColumn(columnName, columnType, i, nullable, size);
				if (null != columnDefaultVal) {
					column.setDefaultValue(columnDefaultVal);
				}
				if (Types.DOUBLE == columnType || Types.FLOAT == columnType) {
					column.setPrecision(precision);
					column.setScale(scale);
				}
				columnMap.put(columnName, column);
				i++;
			}
			return columnMap;
		} catch (SQLException e) {
			throw new DataServiceFault(e, "Error in reading table meta data in " + tableName + " table.");
		} finally {
			releaseResources(resultSet, null, null);
		}
	}

	/**
	 * This method initializes metadata.
	 *
	 * @throws DataServiceFault
	 */
	private void initializeMetaData() throws DataServiceFault {
		this.tableMetaData = new HashMap<>();
		this.primaryKeys = new HashMap<>();
		this.navigationProperties = new HashMap<>();
		Connection connection = null;
		try {
			connection = this.dataSource.getConnection();
			DatabaseMetaData metadata = connection.getMetaData();
			String catalog = connection.getCatalog();
			for (String tableName : this.tableList) {
				this.tableMetaData.put(tableName, readTableColumnMetaData(tableName, metadata));
				this.navigationProperties.put(tableName, readForeignKeys(tableName, metadata, catalog));
				this.primaryKeys.put(tableName, readTablePrimaryKeys(tableName, metadata, catalog));
			}
		} catch (SQLException e) {
			throw new DataServiceFault(e, "Error in reading tables from the database");
		} finally {
			releaseResources(null, null, connection);
		}
	}

	/**
	 * This method creates a list of tables available in the DB.
	 *
	 * @return Table List of the DB
	 * @throws DataServiceFault
	 */
	private List<String> generateTableList() throws DataServiceFault {
		List<String> tableList = new ArrayList<>();
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = this.dataSource.getConnection();
			DatabaseMetaData meta = connection.getMetaData();
			rs = meta.getTables(null, null, null, new String[] { "TABLE" });
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				tableList.add(tableName);
			}
			return tableList;
		} catch (SQLException e) {
			throw new DataServiceFault(e, "Error in reading tables from the database");
		} finally {
			releaseResources(rs, null, connection);
		}
	}

	/**
	 * This method reads primary keys of the table.
	 *
	 * @param tableName Name of the table
	 * @return primary key list
	 * @throws DataServiceFault
	 */
	private List<String> readTablePrimaryKeys(String tableName, DatabaseMetaData metaData, String catalog)
			throws DataServiceFault {
		ResultSet resultSet = null;
		List<String> keys = new ArrayList<>();
		try {
			resultSet = metaData.getPrimaryKeys(catalog, "", tableName);
			while (resultSet.next()) {
				String primaryKey = resultSet.getString("COLUMN_NAME");
				keys.add(primaryKey);
			}
			return keys;
		} catch (SQLException e) {
			throw new DataServiceFault(e, "Error in reading table primary keys in " + tableName + " table.");
		} finally {
			releaseResources(resultSet, null, null);
		}
	}

	/**
	 * This method reads foreign keys of the table.
	 *
	 * @param tableName Name of the table
	 * @throws DataServiceFault
	 */
	private Map<String, List<String>> readForeignKeys(String tableName, DatabaseMetaData metaData, String catalog)
			throws DataServiceFault {
		ResultSet resultSet = null;
		Map<String, List<String>> tableNavigationProperties = new HashMap<>();
		try {
			resultSet = metaData.getExportedKeys(catalog, null, tableName);
			while (resultSet.next()) {
				// foreignKeyTableName means the table name of the table which used columns as foreign keys in that table.
				String foreignKeyTableName = resultSet.getString("FKTABLE_NAME");
				String foreignKeyColumnName = resultSet.getString("FKCOLUMN_NAME");
				List<String> columnList = tableNavigationProperties.get(foreignKeyTableName);
				if (columnList == null) {
					columnList = new ArrayList<>();
					tableNavigationProperties.put(foreignKeyTableName, columnList);
				}
				columnList.add(foreignKeyColumnName);
			}
			return tableNavigationProperties;
		} catch (SQLException e) {
			throw new DataServiceFault(e, "Error in reading " + tableName + " table meta data.");
		} finally {
			releaseResources(resultSet, null, null);
		}
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
			Connection connection = null;
			PreparedStatement statement = null;
			try {
				connection = this.dataSource.getConnection();
				statement = connection.prepareStatement(sql);
				statement = bindValuesToPreparedStatement(column.getColumnType(),
				                                          property.getValue("propertyValue").getScalarValue(), 1,
				                                          statement);
				statement.execute();
			} catch (SQLException | ParseException e) {
				throw new DataServiceFault(e, "Error in updating the property in " + tableName + " table.");

			} finally {
				releaseResources(null, statement, connection);
			}
		} else {
			throw new DataServiceFault("Property didn't found to update");
		}
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
			if (!pKeys.contains(column.getColumnName())) {
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
	private String createInsertSQL(String tableName) throws DataServiceFault {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(tableName).append(" (");
		boolean propertyMatch = false;
		for (DataColumn column : tableMetaData.get(tableName).values()) {
			if (propertyMatch) {
				sql.append(",");
			}
			sql.append(column.getColumnName());
			propertyMatch = true;
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
