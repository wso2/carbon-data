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
package org.wso2.carbon.dataservices.sql.driver;

import com.google.gdata.client.Query;
import com.google.gdata.client.spreadsheet.*;
import com.google.gdata.data.IFeed;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.wso2.carbon.dataservices.sql.driver.parser.Constants;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataTable;
import org.wso2.carbon.dataservices.sql.driver.query.ColumnInfo;
import org.wso2.carbon.dataservices.sql.driver.query.ParamInfo;
import org.wso2.carbon.dataservices.sql.driver.query.QueryFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TDriverUtil {

    private static List<String> driverProperties = new ArrayList<String>();

    static {
        driverProperties.add(Constants.DRIVER_PROPERTIES.FILE_PATH);
        driverProperties.add(Constants.DRIVER_PROPERTIES.SHEET_NAME);
        driverProperties.add(Constants.DRIVER_PROPERTIES.VISIBILITY);
        driverProperties.add(Constants.DRIVER_PROPERTIES.HAS_HEADER);
        driverProperties.add(Constants.DRIVER_PROPERTIES.USER);
        driverProperties.add(Constants.DRIVER_PROPERTIES.PASSWORD);
        driverProperties.add(Constants.DRIVER_PROPERTIES.DATA_SOURCE_TYPE);
        driverProperties.add(Constants.DRIVER_PROPERTIES.MAX_COLUMNS);
    }

    public static List<String> getAvailableDriverProperties() {
        return driverProperties;
    }

    public static ColumnInfo[] getHeaders(Connection connection,
                                          String tableName) throws SQLException {
        if (!(connection instanceof TConnection)) {
            throw new SQLException("Invalid connection type");
        }
        String connectionType = ((TConnection) connection).getType();
        QueryFactory.QueryTypes type =
                QueryFactory.QueryTypes.valueOf(connectionType.toUpperCase());
        switch (type) {
            case EXCEL:
                return getExcelHeaders(connection, tableName);
            case GSPREAD:
                return getGSpreadHeaders(connection, tableName);
            case CUSTOM:
                return getCustomHeaders(connection, tableName);                
            default:
                throw new SQLException("Invalid query type: " + type);
        }
    }

    private static ColumnInfo[] getExcelHeaders(Connection connection,
                                                String tableName) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
        if (!(connection instanceof TExcelConnection)) {
            throw new SQLException("Invalid connection type");
        }
        Workbook workbook = ((TExcelConnection) connection).getWorkbook();
        Sheet sheet = workbook.getSheet(tableName);
        if (sheet == null) {
            throw new SQLException("Sheet '" + tableName + "' does not exist");
        }
        Iterator<Cell> cellItr = sheet.getRow(0).cellIterator();
        while (cellItr.hasNext()) {
            Cell header = cellItr.next();
            ColumnInfo column = new ColumnInfo(header.getStringCellValue());
            column.setTableName(tableName);
            column.setSqlType(header.getCellType());
            column.setId(header.getColumnIndex());

            columns.add(column);
        }
        return columns.toArray(new ColumnInfo[columns.size()]);
    }

    private static ColumnInfo[] getCustomHeaders(Connection connection,
            String sheetName) throws SQLException {
    	DataTable table = ((TCustomConnection) connection).getDataSource().getDataTable(sheetName);
    	return table.getHeaders();
    }
    
    private static ColumnInfo[] getGSpreadHeaders(Connection connection,
                                                  String sheetName) throws SQLException {
        WorksheetEntry currentWorksheet;
        List<ColumnInfo> columns = new ArrayList<ColumnInfo>();

        if (!(connection instanceof TGSpreadConnection)) {
            throw new SQLException("Invalid connection type");
        }
        currentWorksheet = getCurrentWorkSheetEntry(connection, sheetName);
        if (currentWorksheet == null) {
            throw new SQLException("Worksheet '" + sheetName + "' does not exist");
        }
        CellFeed cellFeed = getCellFeed(connection, currentWorksheet);
        for (CellEntry cell : cellFeed.getEntries()) {
            if (!getCellPosition(cell.getId()).startsWith("R1")) {
                break;
            }
            ColumnInfo column =
                    new ColumnInfo(cell.getTextContent().getContent().getPlainText());
            column.setTableName(sheetName);
            column.setSqlType(cell.getContent().getType());
            column.setId(getColumnIndex(cell.getId()) - 1);
            columns.add(column);
        }
        return columns.toArray(new ColumnInfo[columns.size()]);
    }

    public static int getColumnIndex(String id) {
        String tmp = getCellPosition(id);
        id = tmp.substring(tmp.indexOf("C"), tmp.length()).substring(1);
        return Integer.parseInt(id);
    }

    public static int getRowIndex(String id) {
        String tmp = getCellPosition(id);
        id = tmp.substring(tmp.indexOf("R") + 1, tmp.indexOf("C"));
        return Integer.parseInt(id);
    }

    public static String getCellPosition(String id) {
        return id.substring(id.lastIndexOf("/") + 1);
    }

    public static CellFeed getCellFeed(Connection connection,
                                       WorksheetEntry currentWorkSheet) throws SQLException {
        SpreadsheetService service = ((TGSpreadConnection) connection).getSpreadSheetService();
        CellQuery cellQuery = new CellQuery(currentWorkSheet.getCellFeedUrl());
        return getFeed(service, cellQuery, CellFeed.class);
    }

    public static WorksheetEntry getCurrentWorkSheetEntry(Connection connection,
                                                          String sheetName) throws SQLException {
    	SpreadsheetEntry spreadsheetEntry = ((TGSpreadConnection) connection).getSpreadSheetFeed().getEntries().get(0);
        WorksheetQuery worksheetQuery =
                TDriverUtil.createWorkSheetQuery(spreadsheetEntry.getWorksheetFeedUrl());
        WorksheetFeed worksheetFeed = getFeed(((TGSpreadConnection) connection).getSpreadSheetService(), worksheetQuery,
                WorksheetFeed.class);
    	for (WorksheetEntry entry : worksheetFeed.getEntries()) {
            if (sheetName.equals(entry.getTitle().getPlainText())) {
                return entry;
            }
        }
        return null;
    }

    public static SpreadsheetQuery createSpreadSheetQuery(String spreadSheetName,
                                                          URL spreadSheetFeedUrl) {
        SpreadsheetQuery spreadsheetQuery = new SpreadsheetQuery(spreadSheetFeedUrl);
        spreadsheetQuery.setTitleQuery(spreadSheetName);
        spreadsheetQuery.setTitleExact(true);
        return spreadsheetQuery;
    }
    
    public static WorksheetQuery createWorkSheetQuery(URL workSheetFeedUrl) {
        return new WorksheetQuery(workSheetFeedUrl);
    }

    public static <F extends IFeed> F getFeed(SpreadsheetService service, Query query,
                                              Class<F> feedClass) throws SQLException {
        try {
            return service.getFeed(query, feedClass);
        } catch (IOException e) {
            throw new SQLException("Error occurred while retrieving the feed", e);
        } catch (ServiceException e) {
            throw new SQLException("Error occurred while retrieving the feed", e);
        }
    }

    public static ParamInfo findParam(ColumnInfo columnInfo, ParamInfo[] params) {
        ParamInfo param = null;
        for (ParamInfo tmpParam : params) {
            if (columnInfo.getName().equals(tmpParam.getName())) {
                param = tmpParam;
                break;
            }
        }
        return param;
    }

    public static ListFeed getListFeed(Connection connection,
                                       WorksheetEntry currentWorkSheet) throws SQLException {
        SpreadsheetService service = ((TGSpreadConnection) connection).getSpreadSheetService();
        ListQuery listQuery = new ListQuery(currentWorkSheet.getListFeedUrl());
        return getFeed(service, listQuery, ListFeed.class);
    }

    public static void writeRecords(Workbook workbook, String filePath) throws SQLException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            workbook.write(out);
        } catch (FileNotFoundException e) {
            throw new SQLException("Error occurred while locating the EXCEL datasource", e);
        } catch (IOException e) {
            throw new SQLException("Error occurred while writing the records to the EXCEL " +
                    "data source", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignore) {

                }
            }
        }
    }
    
}
