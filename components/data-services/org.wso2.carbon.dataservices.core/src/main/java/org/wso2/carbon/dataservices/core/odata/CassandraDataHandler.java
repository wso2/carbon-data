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

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.PreparedStatement;
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
 * //todo add class level commnets
 */
public class CassandraDataHandler extends AbstractExpressionDataHandler {


	private String configID;

	/**
	 * List of Tables in the Database
	 */
	private List<String> tableList;

	private Session session;

	private String keyspace;


	public CassandraDataHandler( String configID, Session session, String keyspace)
			throws DataServiceFault {
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
		ResultSet resultSet = session.execute(statement);
		Iterator<Row> iterator = resultSet.iterator();
		List<DataEntry> entryList = new ArrayList<>();
		ColumnDefinitions columnDefinitions = resultSet.getColumnDefinitions();
		while (iterator.hasNext()) {
			DataEntry dataEntry = createDataEntryFromRow(tableName, iterator.next(),columnDefinitions);
			entryList.add(dataEntry);
		}
		return entryList;
	}

	@Override
	public List<DataEntry> readTableWithKeys(String tableName, DataEntry keys) throws DataServiceFault {
		PreparedStatement statement = session.prepare(createReadSqlWithKeys(tableName, keys));
		BoundStatement boundStatement = bindParams(keys, statement, tableName);
		ResultSet resultSet = session.execute(boundStatement);
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
		PreparedStatement statement = session.prepare(createInsertSQL(tableName));
		BoundStatement boundStatement = bindParams(entity, statement, tableName);
		session.execute(boundStatement);
		return generateETag(tableName, entity);
	}

	@Override
	public void deleteEntityInTable(String tableName, DataEntry entity) throws DataServiceFault {
		PreparedStatement statement = session.prepare(createDeleteSQL(tableName, entity));
		BoundStatement boundStatement = bindParams(entity, statement, tableName);
		session.execute(boundStatement);
	}

	@Override
	public void updateEntityInTable(String tableName, DataEntry entity, DataEntry existingProperties)
			throws DataServiceFault {
		PreparedStatement statement = session.prepare(createUpdateEntitySQL(tableName, entity));
		BoundStatement boundStatement = bindParams(entity, statement, tableName);
		session.execute(boundStatement);
	}

	@Override
	public Map<String, Map<String, DataColumn>> getTableMetadata() {
		return tableMetaData;
	}

	@Override
	public void updatePropertyInTable(String tableName, DataEntry property) throws DataServiceFault {

	}

	@Override
	public List<String> getTableList() {
		return tableList;
	}

	@Override
	public Map<String, List<String>> getPrimaryKeys() {
		return primaryKeys;
	}

	@Override
	public Map<String, Map<String, List<String>>> getNavigationProperties() {
		return null;
	}

	/**
	 * This method wraps result set data in to DataEntry and creates a list of DataEntry
	 *
	 * @param tableName         Table Name
	 * @param row               Row
	 * @param columnDefinitions Column Definition
	 * @return DataEntry
	 * @throws DataServiceFault
	 */
	private DataEntry createDataEntryFromRow(String tableName, Row row, ColumnDefinitions columnDefinitions) throws DataServiceFault {
		List<String> pKeys = primaryKeys.get(tableName);
		StringBuilder uniqueString = new StringBuilder();
		ParamValue paramValue;
		DataEntry entry = new DataEntry();
		uniqueString.append(configID).append(tableName);
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
		//Set Etag to the entity
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
		for (String tableName : tableList) {
			List<String> primaryKey = new ArrayList<>();
			for (ColumnMetadata columnMetadata : session.getCluster().getMetadata().getKeyspace(keyspace)
			                                            .getTable(tableName).getPrimaryKey()) {
				primaryKey.add(columnMetadata.getName());
			}
			primaryKeyMap.put(tableName, primaryKey);
		}
		return primaryKeyMap;
	}

	private Map<String, Map<String, DataColumn>> generateMetaData() throws DataServiceFault {
		Map<String, Map<String, DataColumn>> metadata = new HashMap<>();
		for (String tableName : tableList) {
			Map<String, DataColumn> dataColumnMap = new HashMap<>();
			for (ColumnMetadata columnMetadata : session.getCluster().getMetadata().getKeyspace(keyspace)
			                                            .getTable(tableName).getColumns()) {
				DataColumn dataColumn = new DataColumn(columnMetadata.getName(),
				                                       getDataType(columnMetadata.getType().getName()));
				dataColumnMap.put(dataColumn.getColumnName(), dataColumn);
			}
			metadata.put(tableName, dataColumnMap);
		}
		return metadata;
	}

	private BoundStatement bindParams(DataEntry entry, PreparedStatement statement, String tableName) throws DataServiceFault {
		int count = entry.getNames().size();
		ParamValue param;
		List<Object> values = new ArrayList<>(count);
		for (String columnName : entry.getNames()) {
			param = entry.getValue(columnName);
			switch (tableMetaData.get(tableName).get(columnName).getColumnType()) {
				case Types.VARCHAR:
					values.add(param.getScalarValue());
					break;
				case Types.BIGINT:
					values.add(param.getValueAsString() == null ? null : Long.parseLong(param.getValueAsString()));
					break;
				case Types.BLOB:
					values.add(param.getValueAsString() == null ? null :
					           this.base64DecodeByteBuffer(param.getValueAsString()));
					break;
				case Types.BOOLEAN:
					values.add(
							param.getValueAsString() == null ? null : Boolean.parseBoolean(param.getValueAsString()));
					break;
				case Types.DECIMAL:
					values.add(new BigDecimal(param.getValueAsString()));
					break;
				case Types.DOUBLE:
					values.add(Double.parseDouble(param.getValueAsString()));
					break;
				case Types.FLOAT:
					values.add(Float.parseFloat(param.getValueAsString()));
					break;
				case Types.INTEGER:
					values.add(Integer.parseInt(param.getValueAsString()));
					break;
				case Types.TIMESTAMP:
					values.add(DBUtils.getDate(param.getValueAsString()));
					break;
			}
		}
		return statement.bind(values.toArray());
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
}
