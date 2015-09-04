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

import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.dataservices.common.DBConstants;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Utility class for OData.
 */
public class ODataUtils {

	/**
	 * This method generates an unique ETag for each data row entry.
	 *
	 * @param tableName Name of the table
	 * @param entry     Data row entry
	 * @return E Tag
	 */
	public static String generateETag(String configID, String tableName, ODataEntry entry) {
		StringBuilder uniqueString = new StringBuilder();
		uniqueString.append(configID).append(tableName);
		for (String columnName : entry.getNames()) {
			uniqueString.append(columnName).append(entry.getValue(columnName));
		}
		return UUID.nameUUIDFromBytes((uniqueString.toString()).getBytes()).toString();
	}

}
