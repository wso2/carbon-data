/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dataservices.sql.driver.query.create;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.wso2.carbon.dataservices.sql.driver.TDriverUtil;
import org.wso2.carbon.dataservices.sql.driver.TExcelConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ExcelCreateQuery extends CreateQuery {

    public ExcelCreateQuery(Statement stmt) throws SQLException {
        super(stmt);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        this.executeSQL();
        return null;
    }

    @Override
    public int executeUpdate() throws SQLException {
        this.executeSQL();
        return 0;
    }

    @Override
    public boolean execute() throws SQLException {
        this.executeSQL();
        return false;
    }

    private synchronized void executeSQL() throws SQLException {
        TExcelConnection excelCon = (TExcelConnection)getConnection();
        //begin transaction,
        try {
            excelCon.beginExcelTransaction();
            if (excelCon.getWorkbook() == null) {
                throw new SQLException("Connection to EXCEL data source has not been established " +
                        "properly");
            }
            Sheet sheet = excelCon.getWorkbook().createSheet(this.getTableName());
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < this.getColumns().size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(this.getColumns().get(i).getName());
            }
            TDriverUtil.writeRecords(excelCon.getWorkbook(), excelCon.getPath());
        } finally {
            excelCon.close();
        }
    }
    
}
