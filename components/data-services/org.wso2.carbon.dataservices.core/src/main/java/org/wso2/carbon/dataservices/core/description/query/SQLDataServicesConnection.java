/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dataservices.core.description.query;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import javax.sql.XAConnection;

import org.wso2.carbon.dataservices.core.DataServiceConnection;
import org.wso2.carbon.dataservices.core.DataServiceFault;

/**
 * This class represents a SQL data services connection.
 */
public class SQLDataServicesConnection implements DataServiceConnection {

    private Connection jdbcConn;
    
    public SQLDataServicesConnection(Connection jdbcConn) {
        this.jdbcConn = jdbcConn;
    }
    
    @Override
    public void commit() throws DataServiceFault {
        try {
            if (!this.jdbcConn.isClosed() && !this.getAutoCommit(this.jdbcConn)) {
                this.jdbcConn.commit();
            }
        } catch (SQLException e) {
            throw new DataServiceFault(e);
        }
    }

    @Override
    public void rollback() throws DataServiceFault {
        try {
            if (!this.jdbcConn.isClosed() && !this.getAutoCommit(this.jdbcConn)) {
                this.jdbcConn.rollback();
            }
        } catch (SQLException e) {
            throw new DataServiceFault(e);
        }
    }

    @Override
    public void close() throws DataServiceFault {
        try {
            if (!this.jdbcConn.isClosed()) {
                this.jdbcConn.close();
            }
        } catch (SQLException e) {
            throw new DataServiceFault(e);
        }
    }

    @Override
    public boolean isXA() {
        return jdbcConn instanceof XAConnection;
    }
    
    public Connection getJDBCConnection() {
        return jdbcConn;
    }

    private boolean getAutoCommit(Connection conn) throws SQLException {
        try {
            return conn.getAutoCommit();
        } catch (SQLFeatureNotSupportedException ignore) {
            /* some databases does not support this, if so, that means it is
             * similar to being always in autoCommit=true mode */
            return true;
        }
    }
    
}
