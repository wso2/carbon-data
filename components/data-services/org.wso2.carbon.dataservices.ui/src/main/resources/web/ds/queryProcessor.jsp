<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.CarbonError" %>
<%@ page import="org.wso2.carbon.dataservices.common.DBConstants" %>
<%@ page import="org.wso2.carbon.dataservices.ui.DataServiceAdminClient" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.*" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Arrays"%>
<%@ page import="java.util.List" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>
<jsp:useBean id="validators" class="java.util.ArrayList" scope="session" />
<%
    //retrieve form values set in addQuery.jsp page
    String serviceName = request.getParameter("serviceName");
    String queryId = request.getParameter("queryId");
    String datasource = request.getParameter("datasource");
    String sql = request.getParameter("sql");
    String expression = request.getParameter("expression");
    String cql = request.getParameter("cql");
    String sparql = request.getParameter("sparql");
    String flag = request.getParameter("flag");
    String rowName = request.getParameter("txtDataServiceRowName");
    String outputType = request.getParameter("outputType");
    String rdfBaseURI = request.getParameter("txtrdfBaseURI");
    String nameSpace = request.getParameter("txtDataServiceRowNamespace");
    String rdfNameSpace = request.getParameter("txtDataServiceRDFRowNamespace");
    String element = request.getParameter("txtDataServiceWrapElement");
    String workBookName = request.getParameter("txtExcelWorkbookName");
    String startingRow = request.getParameter("txtExcelStartingRow");
    String maxRowCount = request.getParameter("txtExcelMaxRowCount");
    String headerColumns = request.getParameter("txtExcelHeaderColumns");
    String gSpreadWorkSheetNumber = request.getParameter("txtGSpreadWorksheetNumber");
    String gSpreadStartingRow = request.getParameter("txtGSpreadStartingRow");
    String gSpreadMaxRowCount = request.getParameter("txtGSpreadMaxRowCount");
    String gHasHeaders = request.getParameter("txtGSpreadHeaderColumns");
    String inputEvent = request.getParameter("inputEventTrigger");
    String outputEvent = request.getParameter("outputEventTrigger");
    String xsltPath = request.getParameter("xsltPath");
    String rdfXsltPath = request.getParameter("rdfXsltPath");
    String edit = request.getParameter("edit");
    String timeout = request.getParameter("timeout");
    String fetchDirection = request.getParameter("fetchDirection");
    String forceStoredProc = request.getParameter("forceStoredProc");
    String forceJDBCBatchRequests = request.getParameter("forceJDBCBatchRequests");
    String fetchSize = request.getParameter("fetchSize");
    String maxFieldSize = request.getParameter("maxFieldSize");
    String maxRows = request.getParameter("maxRows");
    String scraperVariable = request.getParameter("scraperVariable");
    boolean isAutoResponse = false;
    String autoResponse = request.getParameter("addAutoResponse");
    String autoInputMappings = request.getParameter("addAutoInputMappings");
    String returnGeneratedKeys = request.getParameter("returnGeneratedKeys");
    String keyColumns = request.getParameter("keyColumns");
    String setReturnGeneratedKeys = request.getParameter("setReturnGeneratedKeys");
    String useColumnNumbers = request.getParameter("useColumnNumbers");
    String escapeNonPrintableChar = request.getParameter("escapeNonPrintableChar");

    edit = (edit == null) ? "" : edit;
    outputType = (outputType == null) ? "xml" : outputType;
    returnGeneratedKeys = (returnGeneratedKeys == null) ? "false" : "true";
    useColumnNumbers = (useColumnNumbers == null) ? "false" : "true";
    escapeNonPrintableChar = (escapeNonPrintableChar == null) ? "false" : "true";
    xsltPath = (xsltPath == null) ? "":xsltPath;
    List<Query> queryList = dataService.getQueries();
    Query query;
    Result result = null;
    String forwardTo = "";
    boolean remove = true;

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    DataServiceAdminClient client =
            new DataServiceAdminClient(cookie, backendServerURL, configContext);

    /* clear the validator session bean for add input mappings page */
    if (flag != null && flag.equals("inputMapping")) {
        session.setAttribute("validators", new ArrayList());
    }
    
    if (queryId != null) {
        query = dataService.getQuery(queryId);
        //if have existing queries
        if (query != null) {
            if (edit.equals("false")) {
                //checking for queries with the same query Id.
                String message = "Please enter a different Query ID. A Query ID called " + queryId + " already exists.";
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                forwardTo = "addQuery.jsp";
                flag = "error";
            }
            if (flag.equals("delete")) {
                List<Operation> operation = dataService.getOperations();
                List<Resource> resource = dataService.getResources();
                if (operation != null) {
                    // checking for operations which has used the query before deleting.
                    for (int a = 0; a < operation.size(); a++) {
                        if (queryId.equals(operation.get(a).getCallQuery().getHref())) {
                            String message = "Query Id " + queryId + " has been used by operation(s). Please remove the relevant operation(s) to proceed.";
                            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                            forwardTo = "queries.jsp";
                            flag = "error";
                            remove = false;
                        }
                    }
                }
                if (resource != null) {
                    for (int a = 0; a < resource.size(); a++) {
                        // checking for resources which has used the query before deleting.
                        if (queryId.equals(resource.get(a).getCallQuery().getHref())) {
                            String message = "Query Id " + queryId + " has been used by resource(s). Please remove the relevant resource(s) to proceed.";
                            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                            forwardTo = "queries.jsp";
                            flag = "error";
                            remove = false;
                        }
                    }
                }
                if (remove) {
                    dataService.removeQuery(query);
                }
            } else {
                query.setId(queryId);
                query.setReturnGeneratedKeys(Boolean.parseBoolean(returnGeneratedKeys));
                query.setKeyColumns(keyColumns);
                query.setConfigToUse(datasource);
                if (outputEvent != null) {
                    query.setOutputEventTrigger(outputEvent);
                }
                if (inputEvent != null) {
                    query.setInputEventTrigger(inputEvent);
                }
                Config con = dataService.getConfig(datasource);
                String customDSType = "";
                if (con.getPropertyValue(DBConstants.CustomDataSource.DATA_SOURCE_QUERY_CLASS) instanceof String) {
            		customDSType = DBConstants.DataSourceTypes.CUSTOM_QUERY;
            	} else if (con.getPropertyValue(DBConstants.CustomDataSource.DATA_SOURCE_TABULAR_CLASS) instanceof String) {
            		customDSType = DBConstants.DataSourceTypes.CUSTOM_TABULAR;
            	}
                if (con.getPropertyValue(DBConstants.CarbonDatasource.NAME) != null &&
                		((String)con.getPropertyValue(DBConstants.CarbonDatasource.NAME)).trim().length() != 0) {
            		String type = client.getCarbonDataSourceType((String)con.getPropertyValue(DBConstants.CarbonDatasource.NAME));
                    if (type != null && !type.equals(DBConstants.DataSourceTypes.RDBMS)) {
                    	if (type.equals("DS_CUSTOM_QUERY")) {
                    		customDSType = DBConstants.DataSourceTypes.CUSTOM_QUERY;
                    	} else if (type.equals("DS_CUSTOM_TABULAR")) {
                    		customDSType = DBConstants.DataSourceTypes.CUSTOM_TABULAR;
                    	}
                    }
            	}
                if(con != null) {
                    if (con.getDataSourceType().equals("RDBMS")) {
                    	if (con.getPropertyValue(DBConstants.Excel.DATASOURCE) instanceof String &&
                    			((String)con.getPropertyValue(DBConstants.Excel.DATASOURCE)).trim().length() != 0) {
                    		ExcelQuery excel = new ExcelQuery();
	                        excel.setWorkBookName(workBookName);
	                        excel.setHasHeaders(headerColumns);
	                        excel.setMaxRowCount(maxRowCount);
	                        excel.setStartingRow(startingRow);
	                        query.setExcel(excel);
                    	} else if (con.getPropertyValue(DBConstants.GSpread.DATASOURCE) instanceof String &&
                    			((String)con.getPropertyValue(DBConstants.GSpread.DATASOURCE)).trim().length() != 0) {
                    		GSpreadQuery gspread = new GSpreadQuery();
                            gspread.setWorkSheetNumber(Integer.parseInt(gSpreadWorkSheetNumber));
                            gspread.setMaxRowCount(Integer.parseInt(gSpreadMaxRowCount));
                            gspread.setStartingRow(Integer.parseInt(gSpreadStartingRow));
                            gspread.setHasHeaders(gHasHeaders);
                            query.setGSpread(gspread);
                    	} else {
	                        if (query.getProperties().size() > 0) {
	                            query.updateProperty(DBConstants.RDBMS.QUERY_TIMEOUT, timeout);
	                            query.updateProperty(DBConstants.RDBMS.FETCH_DIRECTION, fetchDirection);
	                            query.updateProperty(DBConstants.RDBMS.FORCE_STORED_PROC, forceStoredProc);
	                            query.updateProperty(DBConstants.RDBMS.FORCE_JDBC_BATCH_REQUESTS, forceJDBCBatchRequests);
	                            query.updateProperty(DBConstants.RDBMS.FETCH_SIZE, fetchSize);
	                            query.updateProperty(DBConstants.RDBMS.MAX_FIELD_SIZE, maxFieldSize);
	                            query.updateProperty(DBConstants.RDBMS.MAX_ROWS, maxRows);
	                        } else {
	                            query.addProperty(DBConstants.RDBMS.FETCH_DIRECTION, fetchDirection);
	                            query.addProperty(DBConstants.RDBMS.FORCE_STORED_PROC, forceStoredProc);
	                            query.addProperty(DBConstants.RDBMS.FORCE_JDBC_BATCH_REQUESTS, forceJDBCBatchRequests);
	                            query.addProperty(DBConstants.RDBMS.FETCH_SIZE, fetchSize);
	                            query.addProperty(DBConstants.RDBMS.MAX_FIELD_SIZE, maxFieldSize);
	                            query.addProperty(DBConstants.RDBMS.MAX_ROWS, maxRows);
	                        }
	                        query.setSql(sql);
                    	}
                    } else if (con.getDataSourceType().equals("EXCEL")) {
                    	if (con.getPropertyValue(DBConstants.RDBMS.URL) instanceof String &&
                    			((String)con.getPropertyValue(DBConstants.RDBMS.URL)).trim().length() != 0) {
                    		query.setSql(sql);  
                        } else {
	                        ExcelQuery excel = new ExcelQuery();
	                        excel.setWorkBookName(workBookName);
	                        excel.setHasHeaders(headerColumns);
	                        excel.setMaxRowCount(maxRowCount);
	                        excel.setStartingRow(startingRow);
	                        query.setExcel(excel);
                        }
                    } else if (con.getDataSourceType().equals("GDATA_SPREADSHEET")) {
                    	if (con.getPropertyValue(DBConstants.RDBMS.URL) instanceof String &&
                    			((String)con.getPropertyValue(DBConstants.RDBMS.URL)).trim().length() != 0) {
                    		query.setSql(sql);  
                        } else { 
	                        GSpreadQuery gspread = new GSpreadQuery();
	                        gspread.setWorkSheetNumber(Integer.parseInt(gSpreadWorkSheetNumber));
	                        gspread.setMaxRowCount(Integer.parseInt(gSpreadMaxRowCount));
	                        gspread.setStartingRow(Integer.parseInt(gSpreadStartingRow));
	                        gspread.setHasHeaders(gHasHeaders);
	                        query.setGSpread(gspread);
                        }
                    } else if (con.getDataSourceType().equals("CARBON_DATASOURCE") || con.getDataSourceType().equals("JNDI")) {
                    	if (customDSType.equals(DBConstants.DataSourceTypes.CUSTOM_QUERY)) {
                    		query.setExpression(expression);
                    	} else {
	                        if (query.getProperties().size() > 0) {
	                            query.updateProperty(DBConstants.RDBMS.QUERY_TIMEOUT, timeout);
	                            query.updateProperty(DBConstants.RDBMS.FETCH_DIRECTION, fetchDirection);
	                            query.updateProperty(DBConstants.RDBMS.FORCE_STORED_PROC, forceStoredProc);
	                            query.updateProperty(DBConstants.RDBMS.FORCE_JDBC_BATCH_REQUESTS, forceJDBCBatchRequests);
	                            query.updateProperty(DBConstants.RDBMS.FETCH_SIZE, fetchSize);
	                            query.updateProperty(DBConstants.RDBMS.MAX_FIELD_SIZE, maxFieldSize);
	                            query.updateProperty(DBConstants.RDBMS.MAX_ROWS, maxRows);
	                        } else {
	                            query.addProperty(DBConstants.RDBMS.QUERY_TIMEOUT, timeout);
	                            query.addProperty(DBConstants.RDBMS.FETCH_DIRECTION, fetchDirection);
	                            query.addProperty(DBConstants.RDBMS.FORCE_STORED_PROC, forceStoredProc);
	                            query.addProperty(DBConstants.RDBMS.FORCE_JDBC_BATCH_REQUESTS, forceJDBCBatchRequests);
	                            query.addProperty(DBConstants.RDBMS.FETCH_SIZE, fetchSize);
	                            query.addProperty(DBConstants.RDBMS.MAX_FIELD_SIZE, maxFieldSize);
	                            query.addProperty(DBConstants.RDBMS.MAX_ROWS, maxRows);
	                        }
	                        query.setSql(sql);
                    	}
                    } else if (con.getDataSourceType().equals("RDF")
                            || con.getDataSourceType().equals("SPARQL")) {
                        query.setSparql(sparql);
                    } else if (con.getDataSourceType().equals("WEB_CONFIG")) {
                        query.setScraperVariable(scraperVariable);
                    } else if (con.getDataSourceType().equals("Cassandra")) {
                        query.setSql(cql);
                    } else if (con.getDataSourceType().equals("CUSTOM")) {
                    	if (customDSType.equals(DBConstants.DataSourceTypes.CUSTOM_QUERY)) {
                    		query.setExpression(expression);
                    	} else {
                    		query.setSql(sql);
                    	}
                    }
                }
                result = query.getResult();
                if (query.getResult() != null) {
                	if(outputType.equals("xml")) {
                		 result.setNamespace(nameSpace);
                		 if (xsltPath != null && xsltPath.trim().length() > 0) {
                             result.setXsltPath(xsltPath);
                         } else {
                        	 result.setXsltPath(null);
                         }
                	}
                	
                	if(outputType.equals("rdf")) {
               		 result.setNamespace(rdfNameSpace);
               		 if (rdfXsltPath != null && rdfXsltPath.trim().length() > 0) {
                            result.setXsltPath(rdfXsltPath);
                        } else {
                            result.setXsltPath(null);
                        }
               	}
                   
                    result.setResultWrapper(element);
                    result.setRowName(rowName);
                    result.setOutputType(outputType);
                    result.setUseColumnNumbers(useColumnNumbers);
                    result.setEscapeNonPrintableChar(escapeNonPrintableChar);
                    result.setRdfBaseURI(rdfBaseURI);

                    
                } else {
                    if ((outputType.equals("xml")) && (!nameSpace.equals("") || !element.equals("") || !rowName.equals(""))) {
                        result = new Result();
                        result.setNamespace(nameSpace);
                        result.setResultWrapper(element);
                        result.setRowName(rowName);
                        result.setOutputType(outputType);
                        result.setUseColumnNumbers(useColumnNumbers);
                        result.setEscapeNonPrintableChar(escapeNonPrintableChar);
                        if (xsltPath != null && xsltPath.trim().length() > 0) {
                            result.setXsltPath(xsltPath);
                        }
                        query.setResult(result);
                    } else if ((outputType.equals("rdf")) && (!rdfNameSpace.equals("") || !rdfBaseURI.equals("") )) {
                    	 result = new Result();
                    	 result.setNamespace(rdfNameSpace);
                         result.setOutputType(outputType);
                         result.setUseColumnNumbers(useColumnNumbers);
                         result.setEscapeNonPrintableChar(escapeNonPrintableChar);
                         result.setRdfBaseURI(rdfBaseURI);
                         if (rdfXsltPath != null && rdfXsltPath.trim().length() > 0) {
                             result.setXsltPath(rdfXsltPath);
                         }
                         query.setResult(result);
                    }
                }
                if (flag.equals("save")) {
                	query.setStatus("add");
                }
            }
        } else {
            query = new Query();
            query.setId(queryId);
            //query hasn't saved yet. Set status to remove until it is saved.
            query.setStatus("remove");
            query.setReturnGeneratedKeys(Boolean.parseBoolean(returnGeneratedKeys));
            query.setKeyColumns(keyColumns);
            if (outputEvent != null) {
                query.setOutputEventTrigger(outputEvent);
            }
            if (inputEvent != null) {
                query.setInputEventTrigger(inputEvent);
            }
            Config con = dataService.getConfig(datasource);
            String customDSType = "";
            if (con.getPropertyValue(DBConstants.CustomDataSource.DATA_SOURCE_QUERY_CLASS) instanceof String) {
        		customDSType = DBConstants.DataSourceTypes.CUSTOM_QUERY;
        	} else if (con.getPropertyValue(DBConstants.CustomDataSource.DATA_SOURCE_TABULAR_CLASS) instanceof String) {
        		customDSType = DBConstants.DataSourceTypes.CUSTOM_TABULAR;
        	}
            if (con.getPropertyValue(DBConstants.CarbonDatasource.NAME) != null &&
            		((String)con.getPropertyValue(DBConstants.CarbonDatasource.NAME)).trim().length() != 0) {
        		String type = client.getCarbonDataSourceType((String)con.getPropertyValue(DBConstants.CarbonDatasource.NAME));
                if (type != null && !type.equals(DBConstants.DataSourceTypes.RDBMS)) {
                	if (type.equals("DS_CUSTOM_QUERY")) {
                		customDSType = DBConstants.DataSourceTypes.CUSTOM_QUERY;
                	} else if (type.equals("DS_CUSTOM_TABULAR")) {
                		customDSType = DBConstants.DataSourceTypes.CUSTOM_TABULAR;
                	}
                }
        	}
            //query.setConfigToUse(con.getDataSourceType());
            query.setConfigToUse(datasource);
            if (con != null) {
                if (con.getDataSourceType().equals("RDBMS")) {
                	if (con.getPropertyValue(DBConstants.Excel.DATASOURCE) instanceof String &&
                			((String)con.getPropertyValue(DBConstants.Excel.DATASOURCE)).trim().length() != 0) {
                		ExcelQuery excel = new ExcelQuery();
                        excel.setWorkBookName(workBookName);
                        excel.setHasHeaders(headerColumns);
                        excel.setMaxRowCount(maxRowCount);
                        excel.setStartingRow(startingRow);
                        query.setExcel(excel);
                	} else if (con.getPropertyValue(DBConstants.GSpread.DATASOURCE) instanceof String &&
                			((String)con.getPropertyValue(DBConstants.GSpread.DATASOURCE)).trim().length() != 0) {
                		GSpreadQuery gspread = new GSpreadQuery();
                        gspread.setWorkSheetNumber(Integer.parseInt(gSpreadWorkSheetNumber));
                        gspread.setMaxRowCount(Integer.parseInt(gSpreadMaxRowCount));
                        gspread.setStartingRow(Integer.parseInt(gSpreadStartingRow));
                        gspread.setHasHeaders(gHasHeaders);
                        query.setGSpread(gspread);
                	} else {
	                    query.addProperty(DBConstants.RDBMS.QUERY_TIMEOUT, timeout);
	                    query.addProperty(DBConstants.RDBMS.FETCH_DIRECTION, fetchDirection);
	                    query.addProperty(DBConstants.RDBMS.FORCE_STORED_PROC, forceStoredProc);
	                    query.addProperty(DBConstants.RDBMS.FORCE_JDBC_BATCH_REQUESTS, forceJDBCBatchRequests);
	                    query.addProperty(DBConstants.RDBMS.FETCH_SIZE, fetchSize);
	                    query.addProperty(DBConstants.RDBMS.MAX_FIELD_SIZE, maxFieldSize);
	                    query.addProperty(DBConstants.RDBMS.MAX_ROWS, maxRows);
	                    query.setSql(sql);
                	}
                } else if (con.getDataSourceType().equals("EXCEL")) {
                	if (con.getPropertyValue(DBConstants.RDBMS.URL) instanceof String && 
                			((String)con.getPropertyValue(DBConstants.RDBMS.URL)).trim().length() != 0) {
                		query.setSql(sql);  
                	} else {
	                    ExcelQuery excel = new ExcelQuery();
	                    excel.setWorkBookName(workBookName);
	                    excel.setHasHeaders(headerColumns);
	                    excel.setMaxRowCount(maxRowCount);
	                    excel.setStartingRow(startingRow);
	                    query.setExcel(excel);
	                    excel.buildXML();
                	}
                } else if (con.getDataSourceType().equals("GDATA_SPREADSHEET")) {
                	if (con.getPropertyValue(DBConstants.RDBMS.URL) instanceof String &&
                			((String)con.getPropertyValue(DBConstants.RDBMS.URL)).trim().length() != 0) {
                		query.setSql(sql);  
                    } else { 
	                    GSpreadQuery gspread = new GSpreadQuery();
	                    gspread.setWorkSheetNumber(Integer.parseInt(gSpreadWorkSheetNumber));
	                    gspread.setMaxRowCount(Integer.parseInt(gSpreadMaxRowCount));
	                    gspread.setStartingRow(Integer.parseInt(gSpreadStartingRow));
	                    gspread.setHasHeaders(gHasHeaders);
	                    query.setGSpread(gspread);
                    }
                } else if (con.getDataSourceType().equals("CARBON_DATASOURCE") || con.getDataSourceType().equals("JNDI")) {
                	if (customDSType.equals(DBConstants.DataSourceTypes.CUSTOM_QUERY)) {
                		query.setExpression(expression);
                	} else {
	                    query.addProperty(DBConstants.RDBMS.QUERY_TIMEOUT, timeout);
	                    query.addProperty(DBConstants.RDBMS.FETCH_DIRECTION, fetchDirection);
	                    query.addProperty(DBConstants.RDBMS.FORCE_STORED_PROC, forceStoredProc);
	                    query.addProperty(DBConstants.RDBMS.FORCE_JDBC_BATCH_REQUESTS, forceJDBCBatchRequests);
	                    query.addProperty(DBConstants.RDBMS.FETCH_SIZE, fetchSize);
	                    query.addProperty(DBConstants.RDBMS.MAX_FIELD_SIZE, maxFieldSize);
	                    query.addProperty(DBConstants.RDBMS.MAX_ROWS, maxRows);
	                    query.setSql(sql);
                	}
                } else if (con.getDataSourceType().equals("RDF")
                        || con.getDataSourceType().equals("SPARQL")) {
                    query.setSparql(sparql);
                } else if (con.getDataSourceType().equals("WEB_CONFIG")) {
                    query.setScraperVariable(scraperVariable);
                } else if (con.getDataSourceType().equals("Cassandra")) {
                    query.setSql(cql);
                } else if (con.getDataSourceType().equals("CUSTOM")) {
                	if (customDSType.equals(DBConstants.DataSourceTypes.CUSTOM_QUERY)) {
                		query.setExpression(expression);
                	} else {
                		query.setSql(sql);
                	}
                }
            }
            if ((outputType.equals("xml")) && (!nameSpace.equals("") || !element.equals("") || !rowName.equals(""))) {
                result = new Result();
                result.setNamespace(nameSpace);
                result.setResultWrapper(element);
                result.setRowName(rowName);
                result.setOutputType(outputType);
                result.setUseColumnNumbers(useColumnNumbers);
                result.setEscapeNonPrintableChar(escapeNonPrintableChar);
                if (xsltPath != null && xsltPath.trim().length() > 0) {
                    result.setXsltPath(xsltPath);
                }
                query.setResult(result);
            } else if ((outputType.equals("rdf")) && (!rdfNameSpace.equals("") || !rdfBaseURI.equals("") )) {
            	 result = new Result();
            	 result.setNamespace(rdfNameSpace);
                 result.setOutputType(outputType);
                 result.setUseColumnNumbers(useColumnNumbers);
                 result.setEscapeNonPrintableChar(escapeNonPrintableChar);
                 result.setRdfBaseURI(rdfBaseURI);
                 if (rdfXsltPath != null && rdfXsltPath.trim().length() > 0) {
                     result.setXsltPath(rdfXsltPath);
                 }
                 query.setResult(result);
            }
            dataService.addQuery(query);
            if (flag.equals("save")) {
            	query.setStatus("add");
            }
        }
    } 
    /* check return row id change */
    if (setReturnGeneratedKeys != null) {
    	boolean hasReturnRowProperty = false;
    	String eleName ="";
        if (setReturnGeneratedKeys.equals("true")) {
           if(dataService.getQuery(queryId) != null) {
              Query returnRowQuery = dataService.getQuery(queryId);
              Result res = returnRowQuery.getResult();
              if (returnGeneratedKeys.equals("true") && (!hasReturnRowProperty)) {
                  returnRowQuery.setReturnGeneratedKeys(true);
                  if (res == null || res.getElements().size() == 0) {
                      res = new Result();
                      res.setResultWrapper("GeneratedKeys");
                      res.setRowName("Entry");
                      res.setUseColumnNumbers("true");
                      res.setEscapeNonPrintableChar(escapeNonPrintableChar);
                      returnRowQuery.setResult(res);
                  }
                  Element newElement = new Element();
                  newElement.setDataSourceType("column");
                  newElement.setName("ID");
                  newElement.setDataSourceValue("1");
                  newElement.setxsdType("integer");
                  res.addElement(newElement);
              }
           }   
    	} else if (setReturnGeneratedKeys.equals("false")) {
            if (dataService.getQuery(queryId) != null) {
                Query returnRowQuery = dataService.getQuery(queryId);
                Result res = returnRowQuery.getResult();
                if (returnGeneratedKeys.equals("false")) {
                    returnRowQuery.setReturnGeneratedKeys(false);
                    if (res != null) {
                        res.removeElement("ID");
                        //remove result wrapper only if there are no other result elements exist other than generated key
                        if (res.getElements() != null && res.getElements().size() == 0) {
                            res.setResultWrapper("");
                            res.setRowName("");
                            res.setUseColumnNumbers("false");
                            res.setEscapeNonPrintableChar(escapeNonPrintableChar);
                        }
                        returnRowQuery.setResult(res);
                    }
                }
            }
        }
    }
    /* add auto response */
    if (autoResponse != null) {
        String columnNames[];
    	try{
            boolean isColumnAvailable = false;
            Config con = dataService.getConfig(datasource);
            if (con != null && "Cassandra".equals(con.getDataSourceType())) {
                String message = "Generate response feature is not supported for Cassandra datasources";
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.WARNING, request);
            } else {
                if (sql != null && sql.trim().length() > 0)  {
                    columnNames = client.getOutputColumnNames(sql);
                    if ((columnNames != null) && (columnNames.length > 0))  {
                        if (columnNames[0].equals("ALL")) {
                         String message = "Please Enter column names to generate the response";
                         CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                        } else {
                            Query autoResponseQuery = dataService.getQuery(queryId);
                            Result res = autoResponseQuery.getResult();
                            if (res == null){
                                res = new Result();
                                    res.setResultWrapper("Entries");
                                    res.setRowName("Entry");
                                    autoResponseQuery.setResult(res);
                            }
                            Query q = dataService.getQuery(queryId);
                            List<String> outputMappingList = Arrays.asList(columnNames);
                            List<Element> currentOutputMappingList = res.getElements();
                            List<String> currentOutputMappingNameList = new ArrayList<String>();
                            if (currentOutputMappingList != null) {
                                for (Element ele : currentOutputMappingList) {
                                    currentOutputMappingNameList.add(ele.getName());
                                }
                            }
                            for (String name : columnNames) {
                                if (!currentOutputMappingNameList.contains(name)) {
                                    Element el = new Element();
                                    el.setDataSourceType("column");
                                    el.setDataSourceValue(name.trim());
                                    el.setName(name.trim());
                                    el.setxsdType("string");
                                    res.addElement(el);
                                }
                            }
                            if (outputMappingList != null && outputMappingList.size() > 0) {
                                for (String name : currentOutputMappingNameList) {
                                    if (!outputMappingList.contains(name)) {
                                        res.removeElement(name);
                                    }
                                }
                            }
                        }
                     } else {
                         String message = "SQL query is not applicable to automate the response";
                         CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                     }
                } else {
                    String message = "SQL query is not applicable to automate the response";
                    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                }
            }
        } catch(AxisFault e){
            CarbonError carbonError = new CarbonError();
            carbonError.addError("Error occurred while saving data service configuration.");
            request.setAttribute(CarbonError.ID, carbonError);
     	 
     	 }
    }
    /* auto generate input mappings */
    if (autoInputMappings != null) {
        String[] inputMappingNames = new String[0];
        Config con = dataService.getConfig(datasource);
        if (con != null && "Cassandra".equals(con.getDataSourceType())) {
            String message = "Generate input mappings feature is not supported for Cassandra datasources";
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.WARNING, request);
        } else {
            if (sql != null && sql.trim().length() > 0)  {
                try {
                    inputMappingNames = client.getInputMappingNames(sql);
                } catch (Exception e) {
                    CarbonError carbonError = new CarbonError();
                    carbonError.addError("Error occurred while retrieving input mapping names");
                    request.setAttribute(CarbonError.ID, carbonError);
                }
                if ((inputMappingNames != null) && (inputMappingNames.length > 0))  {
                    Query q = dataService.getQuery(queryId);
                    List<String> inputMappingList = Arrays.asList(inputMappingNames);
                    List<String> currentInputMappingList = new ArrayList<String>();
                    Param[] currentInputMappings = q.getParams();
                    if (currentInputMappings != null && currentInputMappings.length > 0) {
                        for (Param param : currentInputMappings) {
                            currentInputMappingList.add(param.getName());
                        }
                    }
                    for (String name : inputMappingNames) {
                        if (!currentInputMappingList.contains(name)) {
                            Param param = new Param();
                            param.setName(name);
                            param.setParamType("SCALAR");
                            param.setSqlType("STRING");
                            param.setType("IN");
                            param.setValidarors(validators);
                            session.setAttribute("validators", new ArrayList());
                            q.addParam(param);
                        }
                    }
                    if (inputMappingList != null && inputMappingList.size() > 0) {
                        for (String name : currentInputMappingList) {
                            if (!inputMappingList.contains(name)) {
                                q.removeParam(name);
                            }
                        }
                    }
                } else { // no input mappings for the query.
                	Query q = dataService.getQuery(queryId);
                    List<String> currentInputMappingList = new ArrayList<String>();
                    Param[] currentInputMappings = q.getParams();
                    if (currentInputMappings != null && currentInputMappings.length > 0) {
                        for (Param param : currentInputMappings) {
                            currentInputMappingList.add(param.getName());
                        }
                    }
                    for (String name : currentInputMappingList) {
                    	//remove each current param
                        q.removeParam(name);
                    }
                }
            }
        }
    } 
%>

<table style="display:none">
    <tr>
        <td><input id="flag" name="flag" value="<%=flag%>">
        </td>
    </tr>
    <tr><input type="hidden" id="serviceName" value="<%=serviceName%>"/></tr>
    <tr><input type="hidden" id="datasource" value="<%=datasource%>"/></tr>
    <tr><input type="hidden" id="queryId" value="<%=queryId%>"/></tr>
    <tr><input type="hidden" id="sql" value="<%=sql%>"/></tr>
    <tr><input type="hidden" id="sparql" value="<%=sparql%>"/></tr>
    <tr><input type="hidden" id="rowName" value="<%=rowName%>"/></tr>
     <tr><input type="hidden" id="outputType" value="<%=outputType%>"/></tr>
     <tr><input type="hidden" id="rdfBaseURI" value="<%=rdfBaseURI%>"/></tr>
    <tr><input type="hidden" id="ns" value="<%=nameSpace%>"/></tr>
    <tr><input type="hidden" id="ns" value="<%=rdfNameSpace%>"/></tr>
    <tr><input type="hidden" id="element" value="<%=element%>"/></tr>
    <tr><input type="hidden" id="returnGeneratedKeys" value="<%=returnGeneratedKeys%>"/></tr>
    <tr><input type="hidden" id="useColumnNumbers" value="<%=useColumnNumbers%>"/></tr>
</table>

<script type="text/javascript">
    var flag = document.getElementById('flag').value;
    var outputType = document.getElementById('outputType').value;
    var useColumnNumbers = document.getElementById('useColumnNumbers').value;
    var serviceName = document.getElementById('serviceName').value;
    if (flag == 'inputMapping') {
        location.href = 'addInputMapping.jsp?queryId=' + document.getElementById('queryId').value;
    } else if (flag == 'sparqlInputMapping') {
        location.href = 'addSparqlInputMapping.jsp?queryId=' + document.getElementById('queryId').value;
    } else if (flag == 'ReturnRowChanged') {
        location.href = 'addQuery.jsp?queryId=' + document.getElementById('queryId').value;
    } else if (flag == 'autoResponse') {
        location.href = 'addQuery.jsp?queryId=' + document.getElementById('queryId').value;
    } else if (flag == 'autoInputMappings') {
        location.href = 'addQuery.jsp?queryId=' + document.getElementById('queryId').value;
    } else if (flag == 'save' || flag == 'delete') {
        <%--location.href = "queries.jsp?serviceName='" + document.getElementById('serviceName').value + '&useColumnNumbers='<%=useColumnNumbers%>'";--%>
        location.href = "queries.jsp?serviceName==<%=serviceName%>&useColumnNumbers=<%=useColumnNumbers%>&ordinal=2";
    } else if ((flag == 'outputMapping') && (outputType == 'xml' )) {
        location.href = 'addOutputMapping.jsp?queryId=' + document.getElementById('queryId').value;
    } else if ((flag == 'outputMapping') && (outputType == 'rdf' )) {
        location.href = 'addRDFOutputMapping.jsp?queryId=' + document.getElementById('queryId').value;
    }  else if (flag == "error") {
        location.href = "<%=forwardTo%>";
    } else if (flag == "event") {
        location.href = 'addEvents.jsp?queryId=' + document.getElementById('queryId').value;
    } else if (flag == "sqlDialect") {
        location.href = 'addSQLDialect.jsp?flag=add&queryId=' + document.getElementById('queryId').value;
    }


</script>