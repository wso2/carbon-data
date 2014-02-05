/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.core.boxcarring;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents a thread local connection repository.
 */
public class TLConnectionStore {
	
	private static final Log log = LogFactory.getLog(TLConnectionStore.class);
	
	private static ThreadLocal<Map<String, Connection>> tlCons = new ThreadLocal<Map<String, Connection>>() {
		@Override
		protected synchronized Map<String, Connection> initialValue() {
			return new HashMap<String, Connection>();
		}
	};
	
	private static String generateConnectionMapId(String confidId, String user) {
		String userSuffix;
		if (user != null) {
			userSuffix = " # " + user; 
		} else {
			userSuffix = " # #NULL#";
		}
		return confidId + userSuffix;
	}
	
	public static void addConnection(String configId, String user, Connection connection) {
		Map<String, Connection> conns = tlCons.get();
		conns.put(generateConnectionMapId(configId, user), connection);
	}
	
	public static Connection getConnection(String configId, String user) {
		Map<String, Connection> conns = tlCons.get();
		return conns.get(generateConnectionMapId(configId, user));
	}
	
	public static void commitAll() throws SQLException {
		if (log.isDebugEnabled()) {
			log.debug("TLConnectionStore.commitAll()");
		}
		Map<String, Connection> conns = tlCons.get();
		if (conns != null) {
			for (Connection conn : conns.values()) {
				conn.commit();
				if (!conn.isClosed()) {
				    conn.close();
				}
			}
			conns.clear();
		}
	}
	
	public static void rollbackAllAndClose() {
		if (log.isDebugEnabled()) {
			log.debug("TLConnectionStore.rollbackAllAndClose()");
		}
		Map<String, Connection> conns = tlCons.get();
		if (conns != null) {
			for (Connection conn : conns.values()) {
				try {
					if (!conn.isClosed()) {
						conn.rollback();
						conn.close();
					}
				} catch (Exception e) {
					log.error("Error in rollbackAllAndClose", e);
				}
			}
			conns.clear();
		}
	}

}
