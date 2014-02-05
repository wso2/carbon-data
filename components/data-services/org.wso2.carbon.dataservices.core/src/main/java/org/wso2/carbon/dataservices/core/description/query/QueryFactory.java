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
package org.wso2.carbon.dataservices.core.description.query;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.*;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.description.config.SQLCarbonDataSourceConfig;
import org.wso2.carbon.dataservices.core.description.config.Config;
import org.wso2.carbon.dataservices.core.description.config.JNDIConfig;
import org.wso2.carbon.dataservices.core.description.event.EventTrigger;
import org.wso2.carbon.dataservices.core.engine.*;
import org.wso2.carbon.dataservices.core.engine.CallQuery.WithParam;
import org.wso2.carbon.dataservices.core.validation.Validator;
import org.wso2.carbon.dataservices.core.validation.standard.*;

import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * A factory class to create queries in a data service.
 */
public class QueryFactory {
	
	private QueryFactory() { }

	public static Query createQuery(DataService dataService, OMElement queryEl)
			throws DataServiceFault {
		Query query;
		String configId = getConfigId(queryEl);
		Config config = dataService.getConfig(configId);
		if (config == null) {
			throw new DataServiceFault("Invalid configId: " + configId + " in :- \n" + queryEl);
		}
		String sourceType = config.getType();
		if (DataSourceTypes.RDBMS.equals(sourceType)
				|| DataSourceTypes.JNDI.equals(sourceType) 
				|| DataSourceTypes.CARBON.equals(sourceType)
				|| DataSourceTypes.CUSTOM_TABULAR.equals(sourceType)) {
			query = createSQLQuery(dataService, queryEl);
		} else if (DataSourceTypes.CSV.equals(sourceType)) {
			query = createCSVQuery(dataService, queryEl);
		} else if (DataSourceTypes.EXCEL.equals(sourceType)) {
			query = createExcelQuery(dataService, queryEl);
		} else if (DataSourceTypes.GDATA_SPREADSHEET.equals(sourceType)) {
			query = createGSpreadQuery(dataService, queryEl);
        } else if (DataSourceTypes.RDF.equals(sourceType)) {
        	query = createRdfFileQuery(dataService, queryEl);
        } else if (DataSourceTypes.SPARQL.equals(sourceType)) {
        	query = createSparqlEndpointQuery(dataService, queryEl);
		} else if (DataSourceTypes.WEB.equals(sourceType)) {
			query = createWebQuery(dataService, queryEl);
		} else if (DataSourceTypes.CUSTOM_QUERY.equals(sourceType)) {
			query = createCustomQuery(dataService, queryEl);
		} else {
			throw new DataServiceFault("Invalid configType: " + 
					sourceType + " in :- \n" + queryEl);
		}		
		return query;
	}
	
	private static String getConfigId(OMElement queryEl) {
		String configId = queryEl.getAttributeValue(new QName(DBSFields.USE_CONFIG));
		if (configId == null) {
			configId = DBConstants.DEFAULT_CONFIG_ID;
		}
		return configId;
	}
	
	private static String getQueryId(OMElement queryEl) {
		return queryEl.getAttributeValue(new QName(DBSFields.ID));
	}
	
	private static String getCustomQuery(OMElement queryEl) {
		return ((OMElement) queryEl.getChildrenWithLocalName(
				DBSFields.EXPRESSION).next()).getText();
	}

    private static String getQueryVariable(OMElement queryEl) {
        return queryEl.getFirstChildWithName(
                new QName(DBConstants.WebDatasource.QUERY_VARIABLE)).getText();
    }
	
    private static String extractQueryInputNamespace(DataService dataService, 
    		Result result, OMElement queryEl) {
    	String inputNamespace = queryEl.getAttributeValue(new QName(DBSFields.INPUT_NAMESPACE));
    	if (DBUtils.isEmptyString(inputNamespace)) {
    		if (result != null) {
    			inputNamespace = result.getNamespace();
    		}    		
    		if (DBUtils.isEmptyString(inputNamespace)) {
    			inputNamespace = dataService.getDefaultNamespace();
    		}
    	}
    	return inputNamespace;
    }
    
	private static RdfFileQuery createRdfFileQuery(DataService dataService,
			OMElement queryEl) throws DataServiceFault {
		String queryId, configId, sparql, inputNamespace;
		EventTrigger[] eventTriggers;
		Result result;
		try {
		    queryId = getQueryId(queryEl);
		    configId = getConfigId(queryEl);
		    sparql = queryEl.getFirstChildWithName(new QName(DBSFields.SPARQL)).getText();
		    eventTriggers = getEventTriggers(dataService, queryEl);
		    result = getResultFromQueryElement(dataService, queryEl);
		    inputNamespace = extractQueryInputNamespace(dataService, result, queryEl);
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in parsing SPARQL query element");
		}		
		RdfFileQuery query = new RdfFileQuery(dataService, queryId, configId,
				sparql, getQueryParamsFromQueryElement(queryEl), result,
				eventTriggers[0], eventTriggers[1],
				extractAdvancedProps(queryEl), inputNamespace);
		return query;
	}
	
	private static SparqlEndpointQuery createSparqlEndpointQuery(DataService dataService,
			OMElement queryEl) throws DataServiceFault {
		String queryId, configId, sparql, inputNamespace;
		EventTrigger[] eventTriggers;
		Result result;
		try {
		    queryId = getQueryId(queryEl);
		    configId = getConfigId(queryEl);
		    sparql = queryEl.getFirstChildWithName(new QName(DBSFields.SPARQL)).getText();
		    eventTriggers = getEventTriggers(dataService, queryEl);
		    result = getResultFromQueryElement(dataService, queryEl);
		    inputNamespace = extractQueryInputNamespace(dataService, result, queryEl);
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in parsing SPARQL query element");
		}		
		SparqlEndpointQuery query = new SparqlEndpointQuery(dataService, queryId, configId,
				sparql, getQueryParamsFromQueryElement(queryEl), result,
				eventTriggers[0], eventTriggers[1],
				extractAdvancedProps(queryEl), inputNamespace);
		return query;
	}
    
	/**
	 * This method returns the input and output event triggers in a query.
	 * Returns [0] - Input EventTrigger, [1] - Output EventTrigger.
	 * @param dataService corresponding dataservice object
     * @param queryEl dataservices query element
     * @see EventTrigger
     * @return array of Event Trigger objects
	 */
	private static EventTrigger[] getEventTriggers(DataService dataService, OMElement queryEl) {
		EventTrigger inputEventTrigger = null;
		EventTrigger outputEventTrigger = null;
		String inTrigId = queryEl.getAttributeValue(new QName(DBSFields.INPUT_EVENT_TRIGGER));
		String outTrigId = queryEl.getAttributeValue(new QName(DBSFields.OUTPUT_EVENT_TRIGGER));
		if (inTrigId != null) {
			inputEventTrigger = dataService.getEventTrigger(inTrigId);
		}
		if (outTrigId != null) {
			outputEventTrigger = dataService.getEventTrigger(outTrigId);
		}
		return new EventTrigger[] { inputEventTrigger, outputEventTrigger };
	}
	
	private static Map<String, String> extractAdvancedProps(OMElement queryEl) {
		Map<String, String> advancedProperties;
		OMElement propsEl = queryEl.getFirstChildWithName(new QName(DBSFields.PROPERTIES));
		/* extract advanced query properties */
		if (propsEl != null) {
			advancedProperties = DBUtils.extractProperties(propsEl);
		} else {
			advancedProperties = new HashMap<String, String>();
		}
		return advancedProperties;
	}
	
	private static String[] extractKeyColumns(OMElement queryEl) {
		String keyColumnsStr = queryEl.getAttributeValue(new QName(DBSFields.KEY_COLUMNS));
		if (!DBUtils.isEmptyString(keyColumnsStr)) {
			String[] columns = keyColumnsStr.split(",");
			for (int i = 0; i < columns.length; i++) {
				columns[i] = columns[i].trim();
			}
			return columns;
		} else {
		    return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Iterator<OMElement> getSQLQueryElements (OMElement queryEl) {
		return queryEl.getChildrenWithName(new QName(DBSFields.SQL));
	}
	
	private static String getDefaultSQLQuery (OMElement queryEl) {
		String defaultSQL = null;
		Iterator<OMElement> itr = getSQLQueryElements(queryEl);
		while (itr.hasNext()) {
			OMElement sqlQuery = itr.next();
			if (sqlQuery.getAttributeValue(new QName(DBSFields.DIALECT)) == null) {
				defaultSQL = sqlQuery.getText();
				break;
			}
        }
		return defaultSQL;
	}
	
	private static List<SQLDialect> getDialectList(OMElement queryEl) throws DataServiceFault {
		Iterator<OMElement> itr = getSQLQueryElements(queryEl);
		boolean isRepeated = false;
		List<SQLDialect> dialectList = new ArrayList<SQLDialect>();
		while (itr.hasNext()) {
			OMElement sqlQuery = itr.next();
			String sqlDialectValue = sqlQuery.getAttributeValue(new QName(DBSFields.DIALECT));
			Set<String> dialectSet = new HashSet<String>();
			Set<String> intersect = null;
			SQLDialect sqlDialect = new SQLDialect();
			if (sqlDialectValue != null) {
				String dbTypes[] = sqlDialectValue.split(",");
				for (String dbType : dbTypes) {
					dialectSet.add(dbType);
				}
				for (SQLDialect dialect : dialectList) {
					intersect = new TreeSet<String>(dialect.getSqlDialects());
					intersect.retainAll(dialectSet);
					if (!intersect.isEmpty()) {
						isRepeated = true;
					}
				}
				if (!isRepeated) {
					sqlDialect.setSqlDialects(dialectSet);
					sqlDialect.setSqlQuery(sqlQuery.getText());
					dialectList.add(sqlDialect);
				} else {
					Iterator<String> it = intersect.iterator();
                    StringBuilder builder = new StringBuilder();
					while (it.hasNext()) {
						builder.append(it.next());
                        if (it.hasNext()) {
                            builder.append(" ");
                        }
					}
					throw new DataServiceFault("SQL Dialect(s) repeated: " + builder.toString());
				}
			}
		}
		return dialectList;
	}
	
	public static String getSQLQueryForConnectionURL (OMElement queryEl,
                                                      String connectionURL) throws DataServiceFault {
		String driver = null;
		String sql = null;
		if (connectionURL != null) {
			String urlProp[] = connectionURL.split(":");
			if (urlProp.length > 2) {
				driver = urlProp[1];
			}
            List<SQLDialect> dialectList = getDialectList(queryEl);
            for (SQLDialect dialect : dialectList) {
                for (String dialectName : dialect.getSqlDialects()) {
                    if (driver != null && driver.equals(dialectName)) {
                        sql = dialect.getSqlQuery();
                        break;
                    }
                }
            }
        }
		if (sql == null) {
			sql = getDefaultSQLQuery(queryEl);
			if (sql == null) {
				throw new DataServiceFault("The query with the query element :-\n" + 
						queryEl + "\n does not have a matching query dialect for the given " +
                        "data source, and also doesn't provide a default query");
			}
		}
		return sql;

	}
	
	private static String getSQLQueryForDatasource(OMElement queryEl,
                                                   DataService dataService,
                                                   String configId)
            throws DataServiceFault, SQLException, XMLStreamException {
        Connection con = null;
		Config config = dataService.getConfig(configId);
		// RDBMS data source
		String connectionURL = config.getProperty(RDBMS.URL);
        if (connectionURL == null) { // if generic rdbms url is null then check for XA data source url
            connectionURL = DBUtils.getConnectionURL4XADataSource(config);
            if (connectionURL == null) {
                String carbonDSURL = config.getProperty(CarbonDatasource.NAME);
                if (carbonDSURL != null) {
                    SQLCarbonDataSourceConfig carbonDSConfig =
                            (SQLCarbonDataSourceConfig) dataService.getConfig(configId);
                    try {
                        DataSource ds = carbonDSConfig.getDataSource();
                        if (ds != null) {
                            con = ds.getConnection();
                            try {
                                connectionURL = con.getMetaData().getURL();
                            } catch (Exception ignore) {
								/* some drivers may not support meta-data lookup */
							}
                        } else {
                            throw new DataServiceFault("Data source referred by the name '" +
                                    carbonDSConfig.getDataSourceName() + "' does not exist");
                        }
                    } finally {
                        if (con != null) {
                            con.close();
                        }
                    }
                }
                String jndiDataSource = config.getProperty(JNDI.RESOURCE_NAME);
                if (jndiDataSource != null) {
                    // JNDI data source
                    JNDIConfig jndiConfig = (JNDIConfig) dataService.getConfig(configId);
                    try {
                        con = jndiConfig.getDataSource().getConnection();
                        connectionURL = con.getMetaData().getURL();
                    } finally {
                        if(con != null) {
                            con.close();
                        }
                    }
                }
            }
        }
        return getSQLQueryForConnectionURL(queryEl, connectionURL);
	}

	private static SQLQuery createSQLQuery(DataService dataService,	OMElement queryEl) 
			throws DataServiceFault {
		String queryId, configId, sql, inputNamespace;
		boolean returnGeneratedKeys = false;
		EventTrigger[] eventTriggers;
		String[] keyColumns;
		Result result;
		try {
		    queryId = getQueryId(queryEl);
		    configId = getConfigId(queryEl);
		    sql = getSQLQueryForDatasource(queryEl, dataService, configId);
		    eventTriggers = getEventTriggers(dataService, queryEl);
		    String returnRowIdStr = queryEl.getAttributeValue(
		    		new QName(DBConstants.DBSFields.RETURN_GENERATED_KEYS));
		    if (returnRowIdStr != null) {
		    	returnGeneratedKeys = Boolean.parseBoolean(returnRowIdStr);
		    }
		    keyColumns = extractKeyColumns(queryEl);
		    result = getResultFromQueryElement(dataService, queryEl);
		    inputNamespace = extractQueryInputNamespace(dataService, result, queryEl);
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in parsing SQL query element");
		}
		SQLQuery query = new SQLQuery(dataService, queryId, configId,
				returnGeneratedKeys, keyColumns, sql, getQueryParamsFromQueryElement(queryEl),
				result, eventTriggers[0], eventTriggers[1],
				extractAdvancedProps(queryEl), inputNamespace);

		return query;
	}

	private static CSVQuery createCSVQuery(DataService dataService,
			OMElement queryEl) throws DataServiceFault {
		String queryId, configId, inputNamespace;
		EventTrigger[] eventTriggers;
		Result result;
		try {
		    queryId = getQueryId(queryEl);
		    configId = getConfigId(queryEl);
		    eventTriggers = getEventTriggers(dataService, queryEl);
		    result = getResultFromQueryElement(dataService, queryEl);
		    inputNamespace = extractQueryInputNamespace(dataService, result, queryEl);
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in parsing CSV query element");
		}
		CSVQuery query = new CSVQuery(dataService, queryId,
				getQueryParamsFromQueryElement(queryEl), configId, result,
				eventTriggers[0], eventTriggers[1],
				extractAdvancedProps(queryEl), inputNamespace);
		return query;
	}

	private static CustomQueryBasedDSQuery createCustomQuery(DataService dataService,
            OMElement queryEl) throws DataServiceFault {
		String queryId, configId, inputNamespace, expr;
        EventTrigger[] eventTriggers;
        Result result;
        try {
        	expr = getCustomQuery(queryEl);
            queryId = getQueryId(queryEl);
            configId = getConfigId(queryEl);
            eventTriggers = getEventTriggers(dataService, queryEl);
            result = getResultFromQueryElement(dataService, queryEl);
		    inputNamespace = extractQueryInputNamespace(dataService, result, queryEl);
        } catch (Exception e) {
            throw new DataServiceFault(e, "Error in passing Web query element");
        }
		CustomQueryBasedDSQuery query = new CustomQueryBasedDSQuery(dataService, queryId,
				getQueryParamsFromQueryElement(queryEl), result, configId,
				eventTriggers[0], eventTriggers[1],
				extractAdvancedProps(queryEl), inputNamespace, expr);
        return query;
	}
	
    private static WebQuery createWebQuery(DataService dataService,
                                           OMElement queryEl) throws DataServiceFault {
        String queryId, configId, inputNamespace;
        EventTrigger[] eventTriggers;
        Result result;
        try {
            queryId = getQueryId(queryEl);
            configId = getConfigId(queryEl);
            eventTriggers = getEventTriggers(dataService, queryEl);
            result = getResultFromQueryElement(dataService, queryEl);
		    inputNamespace = extractQueryInputNamespace(dataService, result, queryEl);
        } catch (Exception e) {
            throw new DataServiceFault(e, "Error in passing Web query element");
        }
		WebQuery query = new WebQuery(dataService, queryId,
				getQueryParamsFromQueryElement(queryEl), configId, result,
				eventTriggers[0], eventTriggers[1],
				extractAdvancedProps(queryEl), getQueryVariable(queryEl),
				inputNamespace);
        return query;
    }


    private static ExcelQuery createExcelQuery(DataService dataService,
			OMElement queryEl) throws DataServiceFault {
		String queryId, configId, workbookName, inputNamespace;
		int startingRow, maxRowCount;
		boolean hasHeader;
		EventTrigger[] eventTriggers;
		Result result;
		try {
		    queryId = getQueryId(queryEl);
		    configId = getConfigId(queryEl);
		    OMElement excelEl = queryEl.getFirstChildWithName(new QName(DBSFields.EXCEL));
		    workbookName = excelEl.getFirstChildWithName(
		    		new QName(DBConstants.Excel.WORKBOOK_NAME)).getText();
		    
		    OMElement tmpStartingRow = excelEl.getFirstChildWithName(
		    		new QName(DBConstants.Excel.STARTING_ROW));
			if (tmpStartingRow != null) {
				startingRow = Integer.parseInt(tmpStartingRow.getText());
			} else {
				startingRow = 1;
			}
			
			OMElement tmpMaxRowCount = excelEl.getFirstChildWithName(
					new QName(DBConstants.Excel.MAX_ROW_COUNT));
			if (tmpMaxRowCount != null) {
				maxRowCount = Integer.parseInt(tmpMaxRowCount.getText());
			} else {
				maxRowCount = -1;
			}
			
			OMElement tmpHasHeader = excelEl.getFirstChildWithName(
					new QName(DBConstants.Excel.HAS_HEADER));
			if (tmpHasHeader != null) {
				hasHeader = Boolean.parseBoolean(tmpHasHeader.getText());
			} else {
				hasHeader = false;
			}
			
			eventTriggers = getEventTriggers(dataService, queryEl);
			
			result = getResultFromQueryElement(dataService, queryEl);
		    inputNamespace = extractQueryInputNamespace(dataService, result, queryEl);
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in parsing GSpread query element");
		}
		ExcelQuery query = new ExcelQuery(dataService, queryId,
				getQueryParamsFromQueryElement(queryEl), configId,
				workbookName, hasHeader, startingRow, maxRowCount, result,
				eventTriggers[0], eventTriggers[1],
				extractAdvancedProps(queryEl), inputNamespace);
		return query;
	}

	private static GSpreadQuery createGSpreadQuery(DataService dataService,
			OMElement queryEl) throws DataServiceFault {
		String queryId, configId, inputNamespace;
		int worksheetNumber, startingRow, maxRowCount;
		boolean hasHeader;
		EventTrigger[] eventTriggers;
		Result result;
		try {
		    queryId = getQueryId(queryEl);
		    configId = getConfigId(queryEl);
		    OMElement gspreadEl = queryEl.getFirstChildWithName(
		    		new QName(DBSFields.GSPREAD));
		    
		    worksheetNumber = Integer.parseInt(gspreadEl.getFirstChildWithName(
		    		new QName(DBConstants.GSpread.WORKSHEET_NUMBER)).getText());
		    
		    OMElement tmpStartingRow = gspreadEl.getFirstChildWithName(
		    		new QName(DBConstants.GSpread.STARTING_ROW));
			if (tmpStartingRow != null) {
				startingRow = Integer.parseInt(tmpStartingRow.getText());
			} else {
				startingRow = 1;
			}
			
			OMElement tmpMaxRowCount = gspreadEl.getFirstChildWithName(
					new QName(DBConstants.GSpread.MAX_ROW_COUNT));
			if (tmpMaxRowCount != null) {
				maxRowCount = Integer.parseInt(tmpMaxRowCount.getText());
			} else {
				maxRowCount = -1;
			}
			
			OMElement tmpHasHeader = gspreadEl.getFirstChildWithName(
					new QName(DBConstants.GSpread.HAS_HEADER));
			if (tmpHasHeader != null) {
				hasHeader = Boolean.parseBoolean(tmpHasHeader.getText());
			} else {
				hasHeader = false;
			}
			
			eventTriggers = getEventTriggers(dataService, queryEl);
			
			result = getResultFromQueryElement(dataService, queryEl);
		    inputNamespace = extractQueryInputNamespace(dataService, result, queryEl);
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in parsing GSpread query element");
		}
		GSpreadQuery query = new GSpreadQuery(dataService, queryId,
				getQueryParamsFromQueryElement(queryEl), configId,
				worksheetNumber, hasHeader, startingRow, maxRowCount, result,
				eventTriggers[0], eventTriggers[1],
				extractAdvancedProps(queryEl), inputNamespace);
		return query;
	}
	
	private static Result getResultFromQueryElement(DataService dataService, OMElement queryEl)
            throws DataServiceFault {
		OMElement resEl = queryEl.getFirstChildWithName(new QName(DBSFields.RESULT));
		if (resEl == null) {
			return null;
		}
		
		String namespace = resEl.getAttributeValue(new QName(DBSFields.DEFAULT_NAMESPACE));
		if (namespace == null || namespace.trim().length() == 0) {
			namespace = dataService.getDefaultNamespace();
		}
		
		String element = resEl.getAttributeValue(new QName(DBSFields.ELEMENT));
		String rowName = resEl.getAttributeValue(new QName(DBSFields.ROW_NAME));

        String xsltPath = resEl.getAttributeValue(new QName(DBSFields.XSLT_PATH));

        String outputType = resEl.getAttributeValue(new QName(DBSFields.OUTPUT_TYPE));
        int resultType = DBConstants.ResultTypes.XML;
        if (outputType == null || outputType.trim().length() == 0 ||
                outputType.equals(DBSFields.RESULT_TYPE_XML)) {
        	 resultType = DBConstants.ResultTypes.XML;
        } else if (outputType.equals(DBSFields.RESULT_TYPE_RDF)){
		     resultType = DBConstants.ResultTypes.RDF;
		}
        
        if (resultType == DBConstants.ResultTypes.RDF){
        	element = DBConstants.DBSFields.RDF;
		}
        	
        if (resultType == DBConstants.ResultTypes.RDF){
        	rowName = DBConstants.DBSFields.RDF_DESCRIPTION;
		}
        
		Result result = new Result(element, rowName, namespace, xsltPath, resultType);
		if (resultType == DBConstants.ResultTypes.RDF) {
			String rdfBaseURI = resEl.getAttributeValue(new QName(DBSFields.RDF_BASE_URI));
			result.setRDFBaseURI(rdfBaseURI);
		}
		
		boolean useColumnNumbers = false;
        String useColumnNumbersStr = resEl.getAttributeValue(new QName(DBSFields.USE_COLUMN_NUMBERS));
        if (!DBUtils.isEmptyString(useColumnNumbersStr)) {
        	useColumnNumbers = Boolean.parseBoolean(useColumnNumbersStr);
        }
        result.setUseColumnNumbers(useColumnNumbers);

        boolean escapeNonPrintableChar = false;
        String escapeNonPrintableCharStr = resEl.getAttributeValue(new QName(DBSFields.ESCAPE_NON_PRINTABLE_CHAR));
        if (!DBUtils.isEmptyString(escapeNonPrintableCharStr)) {
            escapeNonPrintableChar = Boolean.parseBoolean(escapeNonPrintableCharStr);
        }
        result.setEscapeNonPrintableChar(escapeNonPrintableChar);
		
		/* create default wrapping output element group for the result */
		OMElement groupEl = createElement(DBSFields.ELEMENT);
		addRHSChildrenToLHS(groupEl, resEl);
		
		/* create output element group and set it to the result */
		OutputElementGroup defGroup = createOutputElementGroup(dataService, groupEl, 
				namespace, result, 0, false);
		result.setDefaultElementGroup(defGroup);
		
		return result;
	}
	
	private static OMElement createElement(String name) {
		OMFactory factory = DBUtils.getOMFactory();
		return factory.createOMElement(new QName(name));
	}
	
	@SuppressWarnings("unchecked")
	private static void addRHSChildrenToLHS(OMElement lhs, OMElement rhs) {
		Iterator<OMElement> itr = rhs.getChildElements();
		OMElement el;
		while (itr.hasNext()) {
			el = itr.next();
			lhs.addChild(el);
		}
	}
	
	/**
	 * Checks if the current element is an element group / element with child elements.
	 * @param el The element to be checked
	 * @return True if the element is an element group, else False
	 */
	private static boolean isElementGroup(OMElement el) {
		if (el.getQName().getLocalPart().equals(DBSFields.ELEMENT)) {
			/* if the element only has a name, it must be an element group */ 
			return (el.getAttributeValue(new QName(DBSFields.COLUMN)) == null
					&& el.getAttributeValue(new QName(DBSFields.QUERY_PARAM)) == null 
					&& el.getAttributeValue(new QName(DBSFields.VALUE)) == null);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private static OutputElementGroup createOutputElementGroup(DataService dataService, OMElement groupEl, 
			String parentNamespace, Result parentResult, int level, boolean optionalOverrideCurrent) throws DataServiceFault {
		String name = groupEl.getAttributeValue(new QName(DBSFields.NAME));
		String namespace = groupEl.getAttributeValue(new QName(DBSFields.NAMESPACE));
        String arrayName = groupEl.getAttributeValue(new QName("arrayName"));
		int resultType = parentResult.getResultType();
		if (DBUtils.isEmptyString(namespace)) {
			namespace = parentNamespace;
		}
		Set<String> requiredRoles = extractRequiredRoles(groupEl);		
		OutputElementGroup elGroup = new OutputElementGroup(name, namespace, requiredRoles, arrayName);
		elGroup.setParentResult(parentResult);
		QName elQName = new QName(DBSFields.ELEMENT);
		QName attrQName = new QName(DBSFields.ATTRIBUTE);
		QName cqQName = new QName(DBSFields.CALL_QUERY);
		QName cqgQName = new QName(DBSFields.CALL_QUERY_GROUP);		
		Iterator<OMElement> resElItr = groupEl.getChildElements();
		OMElement el;
		boolean targetOptionalOverride = (level == 0) && (parentResult.getRowName() == null || "".equals(parentResult.getRowName()));
		while (resElItr.hasNext()) {
			el = resElItr.next();
			if (el.getQName().equals(elQName) && isElementGroup(el)) {
                elGroup.addOutputElementGroupEntry(createOutputElementGroup(dataService, el,
                        namespace, parentResult, ++level, targetOptionalOverride));
			} else if (el.getQName().equals(elQName)) {
				elGroup.addElementEntry(createStaticOutputElement(dataService, el, namespace,
                        resultType, targetOptionalOverride));
			} else if (el.getQName().equals(attrQName)) {
				elGroup.addAttributeEntry(createStaticOutputElement(dataService, el, namespace,
                        resultType, targetOptionalOverride));
			} else if (el.getQName().equals(cqQName)) {
				CallQuery callQuery = createCallQuery(dataService, el, targetOptionalOverride);
				List<CallQuery> list = new ArrayList<CallQuery>();
				list.add(callQuery);
				CallQueryGroup cqg = new CallQueryGroup(list);
				elGroup.addCallQueryGroupEntry(cqg);
				cqg.setOptionalOverride(targetOptionalOverride);
			} else if (el.getQName().equals(cqgQName)) {
				CallQueryGroup cqg = createCallQueryGroup(dataService, el, targetOptionalOverride);
				elGroup.addCallQueryGroupEntry(cqg);
			}
		}
		elGroup.setOptionalOverride(optionalOverrideCurrent);
		return elGroup;
	}
	
	@SuppressWarnings("unchecked")
	private static List<QueryParam> getQueryParamsFromQueryElement(
			OMElement queryEl) throws DataServiceFault {
		ArrayList<QueryParam> queryParams = new ArrayList<QueryParam>();
		
		Iterator<OMElement> paramItr = queryEl.getChildrenWithName(new QName(DBSFields.PARAM));
		OMElement paramEl;
		String name, sqlType, type, paramType, ordinalStr, defaultValue, structType;
		int ordinal, currentTmpOrdinal = 0;
		
		while (paramItr.hasNext()) {
			paramEl = paramItr.next();
			name = paramEl.getAttributeValue(new QName(DBSFields.NAME));
            if (name != null) {
                name = name.trim();
            }
            defaultValue = paramEl.getAttributeValue(new QName(DBSFields.DEFAULT_VALUE));
			ordinalStr = paramEl.getAttributeValue(new QName(DBSFields.ORDINAL));
			/* handle <= 0 for backward compatibility */
			if (ordinalStr != null && (ordinal = Integer.parseInt(ordinalStr)) > 0) {
				currentTmpOrdinal = Math.max(ordinal, currentTmpOrdinal);
			} else {
				currentTmpOrdinal++;
				ordinal = currentTmpOrdinal;				
			}
			sqlType = paramEl.getAttributeValue(new QName(DBSFields.SQL_TYPE));
			type = paramEl.getAttributeValue(new QName(DBSFields.TYPE));
			if (type == null || type.trim().length() == 0) {
				type = QueryTypes.IN;
			}
			paramType = paramEl.getAttributeValue(new QName(DBSFields.PARAM_TYPE));
			if (paramType == null || paramType.trim().length() == 0) {
				paramType = QueryParamTypes.SCALAR;
			}
			/* retrieve validators */
			List<Validator> validators = getValidators(paramType, paramEl);
            /* retrieve struct type  */
            structType = paramEl.getAttributeValue(new QName(DBSFields.STRUCT_TYPE));
			queryParams.add(new QueryParam(name, sqlType, type, paramType, ordinal,
                    defaultValue == null ? null : new ParamValue(defaultValue), structType,
                    validators));
		}
		
		return queryParams;
	}
	
	private static List<Validator> getValidators(String paramType,
			OMElement paramEl) throws DataServiceFault {
		/* add basic validators to check scalar, array etc.. */
		List<Validator> validators = new ArrayList<Validator>();
		if (paramType.equals("SCALAR")) {
			validators.add(ScalarTypeValidator.getInstance());
		} else if (paramType.equals("ARRAY")) {
			validators.add(ArrayTypeValidator.getInstance());
		}
		/* add specific validators as requested */
		OMElement valEl = paramEl.getFirstChildWithName(new QName("validateLongRange"));
		if (valEl != null) {
			validators.add(getLongRangeValidator(valEl));
		}
		valEl = paramEl.getFirstChildWithName(new QName("validateDoubleRange"));
		if (valEl != null) {
			validators.add(getDoubleRangeValidator(valEl));
		}
		valEl = paramEl.getFirstChildWithName(new QName("validateLength"));
		if (valEl != null) {
			validators.add(getLengthValidator(valEl));
		}
		valEl = paramEl.getFirstChildWithName(new QName("validatePattern"));
		if (valEl != null) {
			validators.add(getPatternValidator(valEl));
		}
		/* custom validator */
		valEl = paramEl.getFirstChildWithName(new QName("validateCustom"));
		if (valEl != null) {
			validators.add(getCustomValidator(valEl));
		}
		return validators;
	}
	
	private static LongRangeValidator getLongRangeValidator(OMElement valEl) {
		long minimum = 0, maximum = 0;
		boolean hasMin = false, hasMax = false;
		String minStr = valEl.getAttributeValue(new QName("minimum"));
		if (minStr != null) {
			minimum = Long.parseLong(minStr);
			hasMin = true;
		}
		String maxStr = valEl.getAttributeValue(new QName("maximum"));
		if (maxStr != null) {
			maximum = Long.parseLong(maxStr);
			hasMax = true;
		}
		LongRangeValidator validator = new LongRangeValidator(minimum, maximum, hasMin, hasMax);
		return validator;
	}
	
	private static DoubleRangeValidator getDoubleRangeValidator(OMElement valEl) {
		double minimum = 0.0, maximum = 0.0;
		boolean hasMin = false, hasMax = false;
		String minStr = valEl.getAttributeValue(new QName("minimum"));
		if (minStr != null) {
			minimum = Double.parseDouble(minStr);
			hasMin = true;
		}
		String maxStr = valEl.getAttributeValue(new QName("maximum"));
		if (maxStr != null) {
			maximum = Double.parseDouble(maxStr);
			hasMax = true;
		}
		DoubleRangeValidator validator = new DoubleRangeValidator(minimum, maximum, hasMin, hasMax);
		return validator;
	}
	
	private static LengthValidator getLengthValidator(OMElement valEl) {
		int minimum = 0, maximum = 0;
		boolean hasMin = false, hasMax = false;
		String minStr = valEl.getAttributeValue(new QName("minimum"));
		if (minStr != null) {
			minimum = Integer.parseInt(minStr);
			hasMin = true;
		}
		String maxStr = valEl.getAttributeValue(new QName("maximum"));
		if (maxStr != null) {
			maximum = Integer.parseInt(maxStr);
			hasMax = true;
		}
		LengthValidator validator = new LengthValidator(minimum, maximum, hasMin, hasMax);
		return validator;
	}
	
	private static PatternValidator getPatternValidator(OMElement valEl) {
		String regEx = valEl.getAttributeValue(new QName("pattern"));
		PatternValidator validator = new PatternValidator(regEx);
		return validator;
	}
	
	@SuppressWarnings("unchecked")
	private static Validator getCustomValidator(OMElement valEl) throws DataServiceFault {
		String className = valEl.getAttributeValue(new QName("class"));
		try {
		    Class<Validator> clazz = (Class<Validator>) Class.forName(className);
		    return clazz.newInstance();
		} catch (Exception e) {
			throw new DataServiceFault(e, "Problem in creating custom validator class: " + className);
		}		
	}
	
	private static StaticOutputElement createStaticOutputElement(DataService dataService, 
			OMElement el, String namespace, int resultType, boolean optionalOverride) 
	            throws DataServiceFault {
		String name = el.getAttributeValue(new QName(DBSFields.NAME));
		String paramType = DBSFields.COLUMN;
		String param = el.getAttributeValue(new QName(paramType));		
		if (param == null) {
			paramType = DBSFields.QUERY_PARAM;
			param = el.getAttributeValue(new QName(paramType));
			if (param == null) {
				paramType = DBSFields.VALUE;
				param = el.getAttributeValue(new QName(paramType));
				if (param == null) {
					paramType = DBSFields.RDF_REF_URI;
					param = el.getAttributeValue(new QName(paramType));
					if (param == null ) {
						throw new DataServiceFault(
								"Invalid param type in output element:-\n " + el);
					}
				}
			}
		}	
		
		String originalParam = param;
		
		/* workaround for different character case issues in column names,
		 * constant values will be as it is */
		if (!DBSFields.VALUE.equals(paramType)) {
		    param = param.toLowerCase();
		}
		
		/* namespace handling */
		String ownNamespace = el.getAttributeValue(new QName(DBSFields.NAMESPACE));
		if (!DBUtils.isEmptyString(ownNamespace)) {
			namespace = ownNamespace;
		}
		
		String elementType = el.getLocalName();
		String xsdTypeStr = el.getAttributeValue(new QName(DBSFields.XSD_TYPE));
		if (xsdTypeStr == null || xsdTypeStr.trim().length() == 0) {
			xsdTypeStr = "xs:string";
		}
		QName xsdType = getXsdTypeQName(xsdTypeStr);
		String rdfRef = el.getAttributeValue(new QName(DBSFields.RDF_REF_URI));
		
		int dataCategory;
		if (rdfRef == null || rdfRef.trim().length() == 0) {
			dataCategory = DBConstants.DataCategory.VALUE;
		} else {
			dataCategory = DBConstants.DataCategory.REFERENCE;
		}
		
		/* get the required roles in an output element */
		Set<String> requiredRoles = extractRequiredRoles(el);
		
		/* export value */
		String export = el.getAttributeValue(new QName(DBSFields.EXPORT));

        /* If the element represents an array, its name - in Lower case */
		String arrayName = el.getAttributeValue(new QName("arrayName"));
        if (arrayName != null) {
            arrayName = arrayName.toLowerCase();
        }
		
		/* export type */
		int exportType = ParamValue.PARAM_VALUE_SCALAR;
		String exportTypeStr = el.getAttributeValue(new QName(DBSFields.EXPORT_TYPE));
		if (exportTypeStr != null) {
			if (QueryParamTypes.ARRAY.equals(exportTypeStr)) {
				exportType = ParamValue.PARAM_VALUE_ARRAY;
			}
		}
		
		/* optional value */
		String optionalStr = el.getAttributeValue(new QName(DBSFields.OPTIONAL));
		boolean optional = false;
		if (optionalStr != null) {
			optional = Boolean.parseBoolean(optionalStr);
		}
		
		optionalOverride |= optional;
		
		StaticOutputElement soel = new StaticOutputElement(dataService, name, param, 
				originalParam, paramType, elementType, namespace, 
				xsdType, requiredRoles, dataCategory, resultType, export, exportType, arrayName);
		soel.setOptionalOverride(optionalOverride);
		return soel;
	}
	
	private static Set<String> extractRequiredRoles(OMElement outEl) {
		String rrStr = outEl.getAttributeValue(new QName("requiredRoles"));
		if (rrStr == null || rrStr.trim().length() == 0) {
			return new HashSet<String>();
		}
		Set<String> requiredRoles = new HashSet<String>();
		String[] values = rrStr.split(",");
		for (String value : values) {
			requiredRoles.add(value.trim());
		}
		return requiredRoles;
	}
	
	public static QName getXsdTypeQName(String xsdTypeStr) {
		String[] vals = xsdTypeStr.split(":");
		if (vals.length == 1) {
			return new QName(DBConstants.XSD_NAMESPACE, vals[0]);
		} else {
			return new QName(DBConstants.XSD_NAMESPACE, vals[1], vals[0]);
		}
	}
	
	/**
	 * Create a collection of call queries with the given call query element.
	 */
	@SuppressWarnings("unchecked")
	public static List<CallQueryGroup> createCallQueryGroups(
			DataService dataService, Iterator<OMElement> callQueryElItr,
			Iterator<OMElement> callQueryGroupElItr) throws DataServiceFault {
		List<CallQueryGroup> cqGroupList = new ArrayList<CallQueryGroup>();		
		CallQuery callQuery = null;
		CallQueryGroup cqGroup = null;
		List<CallQuery> cqList = null;
		/* extract single call-queries */
		while (callQueryElItr.hasNext()) {
			callQuery = createCallQuery(dataService, callQueryElItr.next(), false);
			cqList = new ArrayList<CallQuery>();
			cqList.add(callQuery);
			cqGroup = new CallQueryGroup(cqList);
			cqGroupList.add(cqGroup);
		}
		/* extract call-query groups */
		Iterator<OMElement> tmpCqElItr = null;
		while (callQueryGroupElItr.hasNext()) {
			tmpCqElItr = callQueryGroupElItr.next().getChildrenWithName(
					new QName(DBSFields.CALL_QUERY));
			cqList = new ArrayList<CallQuery>();			
			while (tmpCqElItr.hasNext()) {
				callQuery = createCallQuery(dataService, tmpCqElItr.next(), false);
				cqList.add(callQuery);				
			}			
			cqGroup = new CallQueryGroup(cqList);
			cqGroupList.add(cqGroup);
		}			
		return cqGroupList;
	}
	
	@SuppressWarnings("unchecked")
	private static CallQueryGroup createCallQueryGroup(DataService dataservice,
			OMElement el, boolean optionalOverride) throws DataServiceFault {
		Iterator<OMElement> tmpCqElItr = el.getChildrenWithName(new QName("call-query"));
		CallQueryGroup cqGroup;
		List<CallQuery> cqList;
		CallQuery callQuery;
		cqList = new ArrayList<CallQuery>();
		while (tmpCqElItr.hasNext()) {
			callQuery = createCallQuery(dataservice, tmpCqElItr.next(), false);
			cqList.add(callQuery);
		}
		cqGroup = new CallQueryGroup(cqList);
		cqGroup.setOptionalOverride(optionalOverride);
		return cqGroup;
	}
	
	@SuppressWarnings("unchecked")
	private static CallQuery createCallQuery(DataService dataService,
			OMElement el, boolean optionalOverride) throws DataServiceFault {
		String queryId = el.getAttributeValue(new QName(DBSFields.HREF));
		Map<String, WithParam> withParamList = new HashMap<String, WithParam>();
		Iterator<OMElement> wpItr = el.getChildrenWithName(new QName(DBSFields.WITH_PARAM));
		OMElement wpEl;
        WithParam withParam;
		while (wpItr.hasNext()) {
			wpEl = wpItr.next();
            withParam = createWithParam(wpEl);
            /* key - target query's name, value - withparam */
			withParamList.put(withParam.getName(), withParam);
		}		
		/* get the required roles for the call query */
		Set<String> requiredRoles = extractRequiredRoles(el);
		
		CallQuery callQuery = new CallQuery(dataService, queryId, withParamList, requiredRoles);
		callQuery.setOptionalOverride(optionalOverride);
		return callQuery;
	}

	public static CallQuery createEmptyCallQuery(DataService dataService) {
		CallQuery callQuery = new CallQuery(dataService, DBConstants.EMPTY_QUERY_ID, 
				new HashMap<String, WithParam>(), new HashSet<String>());
		return callQuery;
	}
	
	public static CallQuery createEmptyEndBoxcarCallQuery(DataService dataService) {
		CallQuery callQuery = new CallQuery(dataService, DBConstants.EMPTY_END_BOXCAR_QUERY_ID, 
				new HashMap<String, WithParam>(), new HashSet<String>());
		return callQuery;
	}
	
	private static WithParam createWithParam(OMElement el) throws DataServiceFault {
		String name = el.getAttributeValue(new QName("name"));
        if (name != null) {
            name = name.trim();
        }
		String paramType = "column";
		String param = el.getAttributeValue(new QName(paramType));
		String originalParam = null;
        if (param == null) {
            paramType = "query-param";
            param = el.getAttributeValue(new QName(paramType));
            if (param != null) {
                param = param.trim();
            }
        }
		if (param == null) {
			throw new DataServiceFault("Invalid param type in with-param element:-\n " + el);
		} else {
			originalParam = param;
			/* 'toLowerCase' - workaround for different character case issues in column names */
			param = param.toLowerCase();
		}		
		WithParam withParam = new CallQuery.WithParam(name, originalParam, param, paramType);
		return withParam;
	}


}
