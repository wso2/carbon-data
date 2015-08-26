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

/**
 * This class represents a data column properties.
 */
public class DataColumn {

	/**
	 * Name of the table column.
	 */
	private String columnName;

	/**
	 * Data type of the table column - java.sql.Types.
	 */
	private int columnType;

	/**
	 * Ordinal position of the table column.
	 */
	private int ordinalPosition;

	/**
	 * Is the table column support nullable.
	 */
	private boolean nullable;

	/**
	 * Precision of the table column.
	 */
	private int precision;

	/**
	 * Scale of the table column.
	 */
	private int scale;

	/**
	 * Default value of the table column.
	 */
	private String defaultValue;

	/**
	 * Maximum length of the table column.
	 */
	private int maxLength;

	public DataColumn(String columnName, int columnType, int order, boolean isNullable, int length) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.ordinalPosition = order;
		this.nullable = isNullable;
		this.maxLength = length;
	}

	public DataColumn(String columnName, int columnType, boolean isNullable) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.nullable = isNullable;
		// cassandra doesn't have a limitation of length.
		this.maxLength = 2147483647;
	}

	public String getColumnName() {
		return columnName;
	}

	public int getColumnType() {
		return columnType;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public int getOrdinalPosition() {
		return ordinalPosition;
	}

	public boolean isNullable() {
		return nullable;
	}

	public int getPrecision() {
		return precision;
	}

	public int getScale() {
		return scale;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}
