/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
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
package org.wso2.carbon.dataservices.ui.beans;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.DBSFields;
import org.wso2.carbon.dataservices.common.conf.DynamicAuthConfiguration;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Object Model for the data service configuration.
 * <data name="sample">            
 *   <config>..</config>
 *    <operation>..</operation>
 *    <query>..</query>
 * </data>       
 */
public class Data extends DataServiceConfigurationElement{
	
	private String name;
	
    private String description;

    private String serviceNamespace;
    
    private boolean batchRequest;

    private boolean boxcarring;

    private boolean disableLegacyBoxcarringMode;
    
	private boolean enableXA;
	
	private boolean isUseAppServerTS;

    private boolean enableHTTP;

    private boolean enableHTTPS;

    private boolean enableLocal;

    private boolean enableJMS;
	
	private String txManagerJNDIName;
	
	private String txManagerClass;
    
	private String txManagerCleanupMethod;
	
	private boolean disableStreaming;
	
    private String protectedTokens;
    
    private String passwordProvider;

	private ArrayList<Config> configs;
	
	private ArrayList<Operation> operations;
	
	private ArrayList<Query> queries;
	
	private ArrayList<Resource> resources;

    private ArrayList<Event> events;

    private ArrayList<XADataSource> xADataSources;

    private boolean useColumnNumbers;

    private boolean escapeNonPrintableChar;

    private String serviceHierarchy;
    
    private String status;
    
    private String secureVaultNamespace;

    private AuthProvider authProvider;

    public Data() {
        this.configs = new ArrayList<Config>();
        this.queries = new ArrayList<Query>();
        this.operations = new ArrayList<Operation>();
        this.resources = new ArrayList<Resource>();
        this.events = new ArrayList<Event>();
        this.xADataSources = new ArrayList<XADataSource>();
    }

    public String getName() {
		return name;
	}
    
	public void setName(String name) {
		this.name = name;
	}

    public String getServiceHierarchy() {
        return serviceHierarchy;
    }

    public void setServiceHierarchy(String serviceHierarchy) {
        this.serviceHierarchy = serviceHierarchy;
    }

    public String getServiceNamespace() {
		return serviceNamespace;
	}

	public void setServiceNamespace(String serviceNamespace) {
		this.serviceNamespace = serviceNamespace;
	}

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public String getTxManagerName() {
		return txManagerJNDIName;
	}

	public void setTxManagerName(String txManagerName) {
		this.txManagerJNDIName = txManagerName;
	}
	
	public String getTxManagerClass() {
		return txManagerClass;
	}

	public void setTxManagerClass(String txManagerClass) {
		this.txManagerClass = txManagerClass;
	}

	public boolean isEnableXA() {
        return enableXA;
    }

    public void setEnableXA(boolean enableXA) {
    	this.enableXA = enableXA;
    }
    
    public boolean isUseAppServerTS() {
        return isUseAppServerTS;
    }

    public void setIsUseAppServerTS(boolean isUseAppServerTS) {
    	this.isUseAppServerTS = isUseAppServerTS;
    }
    
	public ArrayList<XADataSource> getXADataSources() {
		return xADataSources;
	}
	
	public void setxADataSources(ArrayList<XADataSource> xADataSources) {
		this.xADataSources = xADataSources;
	}

	public boolean isBatchRequest() {
        return batchRequest;
    }

    public void setBatchRequest(boolean batchRequest) {
        this.batchRequest = batchRequest;
    }

    public boolean isBoxcarring() {
        return boxcarring;
    }

    public void setBoxcarring(boolean boxcarring) {
        this.boxcarring = boxcarring;
    }

    public boolean isDisableLegacyBoxcarringMode() {
        return disableLegacyBoxcarringMode;
    }

    public void setDisableLegacyBoxcarringMode(boolean disableLegacyBoxcarringMode) {
        this.disableLegacyBoxcarringMode = disableLegacyBoxcarringMode;
    }

    public boolean isEnableHTTP() {
        return enableHTTP;
    }

    public void setEnableHTTP(boolean enableHTTP) {
        this.enableHTTP = enableHTTP;
    }

    public boolean isEnableHTTPS() {
        return enableHTTPS;
    }

    public void setEnableHTTPS(boolean enableHTTPS) {
        this.enableHTTPS = enableHTTPS;
    }

    public boolean isEnableLocal() {
        return enableLocal;
    }

    public void setEnableLocal(boolean enableLocal) {
        this.enableLocal = enableLocal;
    }

    public boolean isEnableJMS() {
        return enableJMS;
    }

    public void setEnableJMS(boolean enableJMS) {
        this.enableJMS = enableJMS;
    }

    public boolean isUseColumnNumbers() {
        return useColumnNumbers;
    }

    public void setUseColumnNumbers(boolean useColumnNumbers) {
        this.useColumnNumbers = useColumnNumbers;
    }

    public boolean isEscapeNonPrintableChar() {
        return escapeNonPrintableChar;
    }

    public void setEscapeNonPrintableChar(boolean escapeNonPrintableChar) {
        this.escapeNonPrintableChar = escapeNonPrintableChar;
    }

    public ArrayList<Config> getConfigs() {
		return configs;
	}
    
	public void setConfig(Config config) {
		this.configs.add(config);
	}
	
    public void removeConfig(Config config){
        this.configs.remove(config);
    }
    
    public ArrayList<Operation> getOperations() {
		return operations;
	}
    
    public void addOperation(Operation operation){
        operations.add(operation);
    }
    
    public void removeOperation(Operation operation){
        operations.remove(operation);
    }

    public void addXADataSource(XADataSource xADataSource) {
    	xADataSources.add(xADataSource);
    }

    public void removeXADataSource(XADataSource xADataSource) {
    	xADataSources.remove(xADataSource);                
    }
    
    public ArrayList<Event> getEvents() {
        return events;
    }

    public void addEvent(Event event) {
        events.add(event);
    }

    public void removeEvent(Event event) {
        events.remove(event);                
    }
    

    public void addResource(Resource resource){
        resources.add(resource);
    }
    
    public void removeResource(Resource resource){
        resources.remove(resource);
    }
    
    public ArrayList<Query> getQueries() {
		return queries;
	}
    
    public void addQuery(Query query){
        queries.add(query);
    }
    
    public void removeQuery(Query query){
        queries.remove(query);
    }
    
    public ArrayList<Resource> getResources() {
		return resources;
	}

    public String getStatus(){
       return this.status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    
	public String getProtectedTokens() {
		return protectedTokens;
	}

	public void setProtectedTokens(String protectedTokens) {
		this.protectedTokens = protectedTokens;
	}

	public String getPasswordProvider() {
		return passwordProvider;
	}

	public void setPasswordProvider(String passwordProvider) {
		this.passwordProvider = passwordProvider;
	}

	public String getTxManagerCleanupMethod() {
		return txManagerCleanupMethod;
	}

	public void setTxManagerCleanupMethod(String txManagerCleanupMethod) {
		this.txManagerCleanupMethod = txManagerCleanupMethod;
	}

	public boolean isDisableStreaming() {
		return disableStreaming;
	}

	public void setDisableStreaming(boolean disableStreaming) {
		this.disableStreaming = disableStreaming;
	}
	
	public String getSecureVaultNamespace() {
		return secureVaultNamespace;
	}

	public void setSecureVaultNamespace(String secureVaultNamespace) {
		this.secureVaultNamespace = secureVaultNamespace;
	}

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    /**
	 * Schema validation for DS configuration file
	 */
	public void validate() {
		// TODO
	}

	/**
	 * Returns config object containing given config id.
	 */
	public Config getConfig(String configId) {
		Iterator<Config> itrConfigs = configs.iterator();
		while (itrConfigs.hasNext()) {
			Config config = (Config) itrConfigs.next();
			if (config.getId().equals(configId)) {
				return config;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private Config getConfig(OMElement configEle) {
		Config config = new Config();
		OMAttribute id = configEle.getAttribute(new QName("id"));
		if (id != null) {
			config.setId(id.getAttributeValue());
		}

        OMAttribute enableOdataAttribute = configEle.getAttribute(new QName(DBSFields.ENABLE_ODATA));
        if (enableOdataAttribute != null) {
            config.setExposeAsOData(Boolean.parseBoolean(enableOdataAttribute.getAttributeValue()));
        }
        
		Iterator<OMElement> properties = configEle.getChildrenWithName(new QName("property"));
		while (properties.hasNext()) {
			OMElement propertyEle = properties.next();
			Property property = new Property();
			OMAttribute name = propertyEle.getAttribute(new QName("name"));
			if (name != null) {
                if (name.getAttributeValue().equals(DBConstants.RDBMS.DATASOURCE_PROPS)){
                    property.setName(name.getAttributeValue());
                    Iterator<OMElement> nestedProperties = propertyEle.getChildrenWithName(new QName("property"));

                     ArrayList<Property> nestedProperty = new ArrayList<Property>();
                     while (nestedProperties.hasNext()){
                    	 Property nestedProp = new Property();
                         OMElement nestedPropertyEle = nestedProperties.next();
                         OMAttribute secretAlias = nestedPropertyEle.getAttribute(new QName(DBConstants.SECUREVAULT_NAMESPACE,"secretAlias"));
                         OMAttribute propertyName = nestedPropertyEle.getAttribute(new QName("name"));
                         nestedProp.setName(propertyName.getAttributeValue());
                         if (secretAlias != null) {
                        	 nestedProp.setUseSecretAlias(true);
                        	 nestedProp.setValue(secretAlias.getAttributeValue());
                         } else {
                        	 nestedProp.setValue(nestedPropertyEle.getText());
                         }
                         nestedProperty.add(nestedProp);
                     }
                    property.setValue(nestedProperty);
                } else if (name.getAttributeValue().equals(DBConstants.RDBMS.DYNAMIC_USER_AUTH_MAPPING)) {
                    property.setName(name.getAttributeValue());
                    ArrayList<DynamicAuthConfiguration.Entry> dynamicUserList = new ArrayList<DynamicAuthConfiguration.Entry>();
                    DynamicAuthConfiguration dynamicAuthConfiguration = new DynamicAuthConfiguration();
                    Iterator<OMElement> dynamicUserAuthConfigs = propertyEle.getChildrenWithName(new QName("configuration"));
                    Iterator<OMElement> userEntries;
                    while (dynamicUserAuthConfigs.hasNext()) {
                        OMElement dynamicUserConfig = dynamicUserAuthConfigs.next();
                        userEntries = dynamicUserConfig.getChildrenWithName(new QName("entry"));
                        while (userEntries.hasNext()) {
                            OMElement userEntry = userEntries.next();
                            DynamicAuthConfiguration.Entry dynamicUserEntry = new DynamicAuthConfiguration.Entry();
                            String carbonUsername = userEntry.getAttributeValue(new QName("request"));
                            String dbUsername = userEntry.getFirstChildWithName(new QName("username")).getText();
                            String dbUserPwd = userEntry.getFirstChildWithName(new QName("password")).getText();

                            dynamicUserEntry.setRequest(carbonUsername);
                            dynamicUserEntry.setUsername(dbUsername);
                            dynamicUserEntry.setPassword(dbUserPwd);
                            dynamicUserList.add(dynamicUserEntry);
                        }
                    }
                    dynamicAuthConfiguration.setEntries(dynamicUserList);
                    property.setValue(dynamicAuthConfiguration);
                } else if(name.getAttributeValue().equals(DBConstants.CustomDataSource.DATA_SOURCE_PROPS)) {
                	property.setName(name.getAttributeValue());
                    Iterator<OMElement> nestedProperties = propertyEle.getChildrenWithName(new QName("property"));

                     ArrayList<Property> nestedProperty = new ArrayList<Property>();
                     while (nestedProperties.hasNext()){
                    	 Property nestedProp = new Property();
                         OMElement nestedPropertyEle = nestedProperties.next();
                         OMAttribute secretAlias = nestedPropertyEle.getAttribute(new QName(DBConstants.SECUREVAULT_NAMESPACE,"secretAlias"));
                         OMAttribute propertyName = nestedPropertyEle.getAttribute(new QName("name"));
                         nestedProp.setName(propertyName.getAttributeValue());
                         if (secretAlias != null) {
                        	 nestedProp.setUseSecretAlias(true);
                        	 nestedProp.setValue(secretAlias.getAttributeValue());
                         } else {
                        	 nestedProp.setValue(nestedPropertyEle.getText());
                         }
                         nestedProperty.add(nestedProp);
                     }
                    property.setValue(nestedProperty);
                } else {
                	property.setName(name.getAttributeValue());
                	if (name.getAttributeValue().equals(DBConstants.RDBMS.PASSWORD) || 
                			name.getAttributeValue().equals(DBConstants.JNDI.PASSWORD) ||
                			name.getAttributeValue().equals(DBConstants.RDBMS_OLD.PASSWORD) ||
                			name.getAttributeValue().equals(DBConstants.GSpread.PASSWORD)) {
                		OMAttribute secretAlias = propertyEle.getAttribute(new QName(DBConstants.SECUREVAULT_NAMESPACE,"secretAlias"));
                    	if(secretAlias != null) {
                    		config.setUseSecretAliasForPassword(true);
                    		property.setValue(secretAlias.getAttributeValue());
                    	} else {
                    		property.setValue(propertyEle.getText());
                    	}
                	} else {
                		property.setValue(propertyEle.getText());
                	}
                }
			}
			config.addProperty(property);
		}
		return config;
	}
	
	@SuppressWarnings("unchecked")
	private void setCommonQueryProps(OMElement queryEle, Query query) {
		query.setId(queryEle.getAttributeValue(new QName("id")));
		query.setConfigToUse(queryEle.getAttributeValue(new QName("useConfig")));
		query.setInputEventTrigger(queryEle.getAttributeValue(new QName("input-event-trigger")));
        query.setOutputEventTrigger(queryEle.getAttributeValue(new QName("output-event-trigger")));
        query.setReturnGeneratedKeys(Boolean.parseBoolean(queryEle.getAttributeValue(new QName("returnGeneratedKeys"))));
        query.setReturnUpdatedRowCount(Boolean.parseBoolean(queryEle.getAttributeValue(new QName("returnUpdatedRowCount"))));
        query.setKeyColumns(queryEle.getAttributeValue(new QName("keyColumns")));
		Param[] params = getParams(queryEle.getChildrenWithName(new QName("param")));
		query.setParams(params);
        /* populating query properties */

        OMElement propEl = queryEle.getFirstChildWithName(new QName("properties"));

        if (propEl != null) {
            Iterator<OMElement> properties = propEl.getChildrenWithName(new QName("property"));
            while (properties.hasNext()) {
                OMElement propertyEle = properties.next();
                Property property = new Property();
                OMAttribute name = propertyEle.getAttribute(new QName("name"));
                if (name != null) {
                    property.setName(name.getAttributeValue());
                    property.setValue(propertyEle.getText());
                }
                query.addProperty(property);
            }
        }
		/* populating result */
		Iterator<OMElement> results = queryEle.getChildrenWithName(new QName("result"));
		if (results.hasNext()) {
			OMElement resultEle = results.next();
			query.setResult(this.getResult(resultEle));
		}
	}

	@SuppressWarnings ("unchecked")
	private Query getSQLQuery(OMElement queryEle) {
		Query query = new Query();
	    List<SQLDialect> sqlDialects = new ArrayList<SQLDialect>();
		this.setCommonQueryProps(queryEle, query);
		Iterator<OMElement> itr = queryEle.getChildrenWithName(new QName(DBSFields.SQL));
		String sql = null;
		while (itr.hasNext()) {
			OMElement sqlQuery = itr.next();
			if (sqlQuery.getAttributeValue(new QName(DBSFields.DIALECT)) != null) {
				String dialect = sqlQuery.getAttributeValue(new QName(DBSFields.DIALECT));
				String dialectQuery = sqlQuery.getText();
				sqlDialects.add(new SQLDialect(dialect, dialectQuery));
			}
			if (sqlQuery.getAttributeValue(new QName(DBSFields.DIALECT)) == null) {
				sql = sqlQuery.getText();
			}
		}
		query.setSqlDialects(sqlDialects);
		if (sql != null) {
			query.setSql(sql);
		}
		return query;
	}
	
	@SuppressWarnings ("unchecked")
	private Query getExpQuery(OMElement queryEle) {
		Query query = new Query();
	    this.setCommonQueryProps(queryEle, query);
		Iterator<OMElement> itr = queryEle.getChildrenWithName(new QName(DBSFields.EXPRESSION));
		String sql = null;
		while (itr.hasNext()) {
			OMElement sqlQuery = itr.next();
			if (sqlQuery.getAttributeValue(new QName(DBSFields.DIALECT)) == null) {
				sql = sqlQuery.getText();
			}
		}
		if (sql != null) {
			query.setExpression(sql);
		}
		return query;
	}
	
	private Query getSparqlQuery(OMElement queryEle) {
		Query query = new Query();
		this.setCommonQueryProps(queryEle, query);
		OMElement sparqlEle = queryEle.getFirstChildWithName(new QName("sparql"));
		query.setSparql(sparqlEle.getText());
		return query;
	}

    private Query getScraperVariable(OMElement queryEle) {
        Query query = new Query();
        this.setCommonQueryProps(queryEle, query);
        OMElement scraperEle = queryEle.getFirstChildWithName(new QName("scraperVariable"));
        query.setScraperVariable(scraperEle.getText());
        return query;
    }
	
	private Query getExcelQuery(OMElement queryEle) {
		Query query = new Query();
		this.setCommonQueryProps(queryEle, query);
		OMElement excelEle = queryEle.getFirstChildWithName(new QName("excel"));
		ExcelQuery excelQuery = new ExcelQuery();
        OMElement workBookName =  excelEle.getFirstChildWithName(new QName("workbookname"));
        excelQuery.setWorkBookName(workBookName.getText());
        OMElement hasHeader =  excelEle.getFirstChildWithName(new QName("hasheader"));
        excelQuery.setHasHeaders(hasHeader.getText());
        OMElement startingRow =  excelEle.getFirstChildWithName(new QName("startingrow"));
        excelQuery.setStartingRow(startingRow.getText());
        OMElement maxRowCount =  excelEle.getFirstChildWithName(new QName("maxrowcount"));
        excelQuery.setMaxRowCount(maxRowCount.getText());
        OMElement headerRow =  excelEle.getFirstChildWithName(new QName("headerrow"));
        if (headerRow != null) {
            excelQuery.setHeaderRow(headerRow.getText());
        }
        query.setExcel(excelQuery);
		return query;
	}
	
	private Query getGSpreadQuery(OMElement queryEle) {
		Query query = new Query();
		this.setCommonQueryProps(queryEle, query);
		OMElement gspreadEl = queryEle.getFirstChildWithName(new QName("gspread"));
		GSpreadQuery gspreadQuery = new GSpreadQuery();
    	OMElement workSheetNumber =  gspreadEl.getFirstChildWithName(new QName("worksheetnumber"));
    	gspreadQuery.setWorkSheetNumber(Integer.parseInt(workSheetNumber.getText()));
        OMElement startingRow =  gspreadEl.getFirstChildWithName(new QName("startingrow"));
        gspreadQuery.setStartingRow(Integer.parseInt(startingRow.getText()));
        OMElement maxRowCount =  gspreadEl.getFirstChildWithName(new QName("maxrowcount"));
        gspreadQuery.setMaxRowCount(Integer.parseInt(maxRowCount.getText()));
        OMElement hasHeaders = gspreadEl.getFirstChildWithName(new QName("hasheader"));
        gspreadQuery.setHasHeaders(hasHeaders.getText());
        OMElement headerRow = gspreadEl.getFirstChildWithName(new QName("headerrow"));
        if (headerRow != null) {
            gspreadQuery.setHeaderRow(Integer.parseInt(headerRow.getText()));
        }
    	query.setGSpread(gspreadQuery);
		return query;
	}
	
	private void addAttributeToComplexEl(ComplexElement complexEl, OMElement attributeEle) {
		String name = attributeEle.getAttribute(new QName("name")).getAttributeValue();
		OMAttribute columnAttr = attributeEle.getAttribute(new QName("column"));
		OMAttribute queryParamattr = attributeEle.getAttribute(new QName("query-param"));
        OMAttribute arrayNameAttr = attributeEle.getAttribute(new QName("arrayName"));
		String column = null;
		String queryParam = null;
        String arrayName = null;
		if (columnAttr != null) {
			column = columnAttr.getAttributeValue();
		}
		if (queryParamattr != null) {
			queryParam = queryParamattr.getAttributeValue();
		}
        if (arrayNameAttr != null) {
            arrayName = arrayNameAttr.getAttributeValue();
        }
		OMAttribute requiredRolesAttr = attributeEle.getAttribute(new QName("requiredRoles"));
		String requiredRoles = null;
		if (requiredRolesAttr != null) {
			requiredRoles = requiredRolesAttr.getAttributeValue();
		}
		OMAttribute xsdTypeAttr = attributeEle.getAttribute(new QName("xsdType"));
		String xsdType = null;
		if (xsdTypeAttr != null) {
			xsdType = xsdTypeAttr.getAttributeValue();
		}
        OMAttribute optionalattr = attributeEle.getAttribute(new QName("optional"));
        String optional = null;
        if (optionalattr != null) {
            optional = optionalattr.getAttributeValue();
        }
		OMAttribute exportattr = attributeEle.getAttribute(new QName("export"));
		String export = null;
		if (exportattr != null) {
			export = exportattr.getAttributeValue();
		}
		OMAttribute exportTypeattr = attributeEle.getAttribute(new QName("exportType"));
		String exportType = null;
		if (exportTypeattr != null) {
			exportType = exportTypeattr.getAttributeValue();
		}
		if (column != null) {
			Attribute attribute = new Attribute("column",column, name, requiredRoles, xsdType, export, exportType, arrayName, optional);		
			complexEl.addAttribute(attribute);
		} else if (queryParam != null) {
			Attribute attribute = new Attribute("query-param",queryParam, name, requiredRoles, xsdType,  export, exportType, arrayName, optional);
			complexEl.addAttribute(attribute);
		}
	}

	private void addElementToComplexEl(ComplexElement complexEl, OMElement elementEle) {
		OMAttribute nameattr = elementEle.getAttribute(new QName("name"));
		String name = null;
		if (nameattr != null) {
			name = nameattr.getAttributeValue();
		}
		OMAttribute columnattr = elementEle.getAttribute(new QName("column"));
		OMAttribute queryParamattr = elementEle.getAttribute(new QName("query-param"));
		String column = null;
		String queryParam = null;
		if (columnattr != null) {
			column = columnattr.getAttributeValue();
		}
		if (queryParamattr != null) {
			queryParam = queryParamattr.getAttributeValue();
		}
		OMAttribute resourcenattr = elementEle.getAttribute(new QName("rdf-ref-uri"));
		String resource = null;
		if (resourcenattr != null) {
			resource = resourcenattr.getAttributeValue();
		}
		OMAttribute requiredRolesattr = elementEle.getAttribute(new QName("requiredRoles"));
		String requiredRoles = null;
		if (requiredRolesattr != null) {
			requiredRoles = requiredRolesattr.getAttributeValue();
		}
		OMAttribute xsdTypeattr = elementEle.getAttribute(new QName("xsdType"));
		String xsdType = null;
		if (xsdTypeattr != null) {
			xsdType = xsdTypeattr.getAttributeValue();
		}
        OMAttribute optionalattr = elementEle.getAttribute(new QName("optional"));
        String optional = null;
        if (optionalattr != null) {
            optional = optionalattr.getAttributeValue();
        }
		OMAttribute exportattr = elementEle.getAttribute(new QName("export"));
		String export = null;
		if (exportattr != null) {
			export = exportattr.getAttributeValue();
		}
		OMAttribute exportTypeattr = elementEle.getAttribute(new QName("exportType"));
		String exportType = null;
		if (exportTypeattr != null) {
			exportType = exportTypeattr.getAttributeValue();
		}
		OMAttribute namespaceAttr = elementEle.getAttribute(new QName("namespace"));
		String namespace = null;
		if (namespaceAttr != null) {
			namespace = namespaceAttr.getAttributeValue();
		}
        OMAttribute arrayNameAttr = elementEle.getAttribute(new QName("arrayName"));
		String arrayName = null;
		if (arrayNameAttr != null) {
			arrayName = arrayNameAttr.getAttributeValue();
		}
		if (column != null) {
			Element element = new Element("column", column, name,
					requiredRoles, xsdType, export, exportType, namespace, arrayName, optional);
			complexEl.addElement(element);
		} else if (queryParam != null) {
			Element element = new Element("query-param", queryParam, name,
					requiredRoles, xsdType, export, exportType, namespace, arrayName, optional);
			complexEl.addElement(element);
		} else {
			RDFResource rdfResource = new RDFResource(resource, name, requiredRoles, xsdType);
			complexEl.addResource(rdfResource);
		}
	}
	
	private boolean isComplexElement(OMElement el) {
		return el.getChildElements().hasNext();
	}
	
	@SuppressWarnings ("unchecked")
	public ComplexElement getComplexElement(OMElement complexElementEl) {
		ComplexElement complexEl = new ComplexElement();
		OMAttribute nameAttrib = complexElementEl.getAttribute(new QName("name"));
		OMAttribute namespaceAttrib = complexElementEl.getAttribute(new QName("namespace"));
        OMAttribute arrayNameAttrib = complexElementEl.getAttribute(new QName("arrayName"));
		if (nameAttrib != null) {
		    complexEl.setName(nameAttrib.getAttributeValue());
		}
		if (namespaceAttrib != null) {
		    complexEl.setNamespace(namespaceAttrib.getAttributeValue());
		}
        if (arrayNameAttrib != null) {
            complexEl.setArrayName(arrayNameAttrib.getAttributeValue());
        }
		
		Iterator<OMElement> itrElements = complexElementEl.getChildrenWithName(new QName("element"));
		while (itrElements.hasNext()) {
			OMElement elementEle = (OMElement) itrElements.next();
			if (this.isComplexElement(elementEle)) {
				complexEl.getComplexElements().add(this.getComplexElement(elementEle));
			} else {
			    this.addElementToComplexEl(complexEl, elementEle);
			}
		}

		Iterator<OMElement> itrAttributes = complexElementEl.getChildrenWithName(new QName("attribute"));
		while (itrAttributes.hasNext()) {
			OMElement attributeEle = itrAttributes.next();
			this.addAttributeToComplexEl(complexEl, attributeEle);
		}
		
		/* populate call queries */
		Iterator<OMElement> itrCallQueries = complexElementEl.getChildrenWithName(new QName("call-query"));
		while (itrCallQueries.hasNext()) {
			OMElement callQueryEle = itrCallQueries.next();
			CallQueryGroup callQueryGroup = this.getCallQueryGroup(callQueryEle);
			complexEl.addCallQueryGroup(callQueryGroup);
		}
		
		/* populate call query groups */
		Iterator<OMElement> itrCallQueryGroups = complexElementEl.getChildrenWithName(new QName("call-query-group"));
		while (itrCallQueryGroups.hasNext()) {
			OMElement callQueryEle = itrCallQueryGroups.next();
			CallQueryGroup callQueryGroup = this.getCallQueryGroup(callQueryEle);
			complexEl.addCallQueryGroup(callQueryGroup);
		}
		
		return complexEl;
	}
	
	public Result getResult(OMElement resultEle) {
		try {
		OMAttribute outputTypeAttrib = resultEle.getAttribute(new QName("outputType"));
		OMAttribute wrapperAttrib = resultEle.getAttribute(new QName("element"));
		OMAttribute rowNameAttrib = resultEle.getAttribute(new QName("rowName"));
		OMAttribute namespaceAttrib = resultEle.getAttribute(new QName("defaultNamespace"));
		OMAttribute rdfBaseURIAttrib = resultEle.getAttribute(new QName("rdfBaseURI"));
        OMAttribute xsltPathAttrib = resultEle.getAttribute(new QName("xsltPath"));
        OMAttribute useColumnNoAttrib = resultEle.getAttribute(new QName("useColumnNumbers"));
        OMAttribute escapeNonPrintableCharAttrib = resultEle.getAttribute(new QName("escapeNonPrintableChar"));
		Result result = new Result();
		
		result.setTextMapping(resultEle.getText());
		
		if (outputTypeAttrib != null) {
			result.setOutputType(outputTypeAttrib.getAttributeValue());
		}
		if (wrapperAttrib != null) {
			result.setResultWrapper(wrapperAttrib.getAttributeValue());
		}
		if (rowNameAttrib != null) {
			result.setRowName(rowNameAttrib.getAttributeValue());
		}
		if (rdfBaseURIAttrib != null) {
			result.setRdfBaseURI(rdfBaseURIAttrib.getAttributeValue());
		}
		if (namespaceAttrib != null) {
			result.setNamespace(namespaceAttrib.getAttributeValue());
		}
		if (xsltPathAttrib != null) {
			result.setXsltPath(xsltPathAttrib.getAttributeValue());
		}
        if (useColumnNoAttrib != null) {
			result.setUseColumnNumbers(useColumnNoAttrib.getAttributeValue());
		}
        if (escapeNonPrintableCharAttrib != null) {
            result.setEscapeNonPrintableChar(escapeNonPrintableCharAttrib.getAttributeValue());
        }
		
		/*
		 * The result element can also be considered as complex element,
		 * where the same child elements are there, so we process it as a complex element,
		 * and extract the child elements to put to the result object
		 */
		ComplexElement resultComplexEl = this.getComplexElement(resultEle);
		result.setElements(resultComplexEl.getElements());
		result.setResources(resultComplexEl.getResources());
		result.setAttributes(resultComplexEl.getAttributes());
		result.setCallQueryGroups(resultComplexEl.getCallQueryGroups());
		result.setComplexElements(resultComplexEl.getComplexElements());
		
		return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Query getQuery(OMElement queryEle) {
		OMElement sqlEle = queryEle.getFirstChildWithName(new QName("sql"));
		OMElement expEle = queryEle.getFirstChildWithName(new QName("expression"));
		OMElement sparqlEle = queryEle.getFirstChildWithName(new QName("sparql"));
        OMElement excelEle = queryEle.getFirstChildWithName(new QName("excel"));
        OMElement gspreadEl = queryEle.getFirstChildWithName(new QName("gspread"));
        OMElement scraperEle = queryEle.getFirstChildWithName(new QName("scraperVariable"));

        Query query = null;		
        if(sqlEle != null){
			query = this.getSQLQuery(queryEle);
		} else if (sparqlEle != null){
            query = this.getSparqlQuery(queryEle);
        } else if (excelEle != null){
            query = this.getExcelQuery(queryEle);
        } else if (gspreadEl != null) {
        	query = this.getGSpreadQuery(queryEle);
        } else if (scraperEle != null) {
            query = this.getScraperVariable(queryEle);
        } else if (expEle != null) {
        	query = this.getExpQuery(queryEle);
        }
        else { /* for other generic queries, CSV etc.. */
        	query = new Query();
        	this.setCommonQueryProps(queryEle, query);
        }
        
        return query;
	}
	
	private Operation getOperation(OMElement operationEle) {
		OMElement callQueryEle = operationEle.getFirstChildWithName(new QName("call-query"));
		/* if not a call-query, then must be a call-query-group */
		if (callQueryEle == null) {
			callQueryEle = operationEle.getFirstChildWithName(new QName("call-query-group"));
		}
		Operation operation = new Operation();
		operation.setName(operationEle.getAttributeValue(new QName("name")));
		String disableStreaming = operationEle.getAttributeValue(new QName("disableStreaming"));
		if (disableStreaming != null) {
			operation.setDisableStreaming(Boolean.parseBoolean(disableStreaming));
		}
		String returnRequestStatus = operationEle.getAttributeValue(new QName(DBSFields.RETURN_REQUEST_STATUS));
		if (returnRequestStatus != null) {
			operation.setReturnRequestStatus(Boolean.parseBoolean(returnRequestStatus));
		}
		OMElement descriptionEle = operationEle.getFirstChildWithName(new QName("description"));
		if (descriptionEle != null) {
			operation.setDescription(descriptionEle.getText());
		}
		CallQueryGroup callQueryGroup = getCallQueryGroup(callQueryEle);
		operation.setCallQueryGroup(callQueryGroup);
		return operation;
	}

    @SuppressWarnings("unchecked")
	private Event getEvent(OMElement eventEle) {
        Event event = new Event();
        event.setId(eventEle.getAttributeValue(new QName("id")));
        event.setLanguage(eventEle.getAttributeValue(new QName("language")));
        event.setExpression(eventEle.getFirstChildWithName(new QName("expression")).getText());
        event.setTargetTopic(eventEle.getFirstChildWithName(new QName("target-topic")).getText());
        OMElement subsEl = eventEle.getFirstChildWithName(new QName("subscriptions"));
        if (subsEl != null) {
           Iterator<OMElement> subscripts = subsEl.getChildrenWithName(new QName("subscription"));
           while (subscripts.hasNext()) {
		       OMElement sub = subscripts.next();
               event.addSubscription(sub.getText());
		   }
        }
        return event;
    }
    
    @SuppressWarnings("unchecked")
	private XADataSource getXADataSource(OMElement xaDataSourceEle) {
    	XADataSource xaDataSource = new XADataSource();
    	xaDataSource.setId(xaDataSourceEle.getAttributeValue(new QName("id")));
    	xaDataSource.setClassName(xaDataSourceEle.getAttributeValue(new QName("class")));	
    	Iterator<OMElement> properties = xaDataSourceEle.getChildrenWithName(new QName("property"));
		while (properties.hasNext()) {
			OMElement propertyEle = properties.next();
			Property property = new Property();
			OMAttribute name = propertyEle.getAttribute(new QName("name"));
			if (name != null) {
				property.setName(name.getAttributeValue());
				property.setValue(propertyEle.getText());
			}
			xaDataSource.addProperty(property);
		}
        return xaDataSource;
    }
    
    
	private Resource getResource(OMElement resourceEle) {
		OMElement callQueryEle = resourceEle.getFirstChildWithName(new QName("call-query"));        	
    	Resource resource = new Resource();
    	resource.setMethod(resourceEle.getAttributeValue(new QName("method")));
    	resource.setPath(resourceEle.getAttributeValue(new QName("path")));
    	String disableStreaming = resourceEle.getAttributeValue(new QName("disableStreaming"));
		if (disableStreaming != null) {
			resource.setDisableStreaming(Boolean.parseBoolean(disableStreaming));
		}
		String returnRequestStatus = resourceEle.getAttributeValue(new QName(DBSFields.RETURN_REQUEST_STATUS));
		if (returnRequestStatus != null) {
			resource.setReturnRequestStatus(Boolean.parseBoolean(returnRequestStatus));
		}
    	OMElement descriptionEle = resourceEle.getFirstChildWithName(new QName("description"));
		if (descriptionEle != null) {
			resource.setDescription(descriptionEle.getText());
		}
    	CallQueryGroup callQueryGroup = getCallQueryGroup(callQueryEle);
    	resource.setCallQueryGroup(callQueryGroup);
    	return resource;
	}

    /**
     * Method to generate authorization provider for client side use (for editing purposes in UI)
     * @param authProviderEl
     * @return
     */
    @SuppressWarnings("unchecked")
    private AuthProvider getAuthorizationProvider(OMElement authProviderEl) {
        AuthProvider authProvider = null;
        OMAttribute authProviderClassAtt = authProviderEl.getAttribute(new QName(DBConstants.AuthorizationProviderConfig.ATTRIBUTE_NAME_CLASS));
        if (authProviderClassAtt != null) {
            authProvider = new AuthProvider();
            authProvider.setClassName(authProviderClassAtt.getAttributeValue());

            Iterator<OMElement> properties = authProviderEl.getChildrenWithName(new QName(DBSFields.PROPERTY));
            while (properties.hasNext()) {
                OMElement propertyEle = properties.next();
                Property property = new Property();
                OMAttribute name = propertyEle.getAttribute(new QName(DBSFields.NAME));
                if (name != null) {
                    property.setName(name.getAttributeValue());
                    property.setValue(propertyEle.getText());
                }
                authProvider.addProperty(property);
            }
        }
        return authProvider;
    }
    
	/**
	 * populate Object model using OM representation of 
	 * configuration file
	 */
	@SuppressWarnings("unchecked")
	public void populate(OMElement dsXml){
		/* populate name & description */
		OMAttribute serviceName = dsXml.getAttribute(new QName("name"));
		if (serviceName != null) {
			setName(serviceName.getAttributeValue());
		}
		/* There can be only one description */
		OMElement desc = dsXml.getFirstChildWithName(new QName("description"));
		if (desc != null) {
			setDescription(desc.getText());
		}
		/* enable batch requests property */
		OMAttribute enableBatchReq = dsXml.getAttribute(new QName("enableBatchRequests"));
		if (enableBatchReq != null) {
			setBatchRequest(Boolean.parseBoolean(enableBatchReq.getAttributeValue()));
		}
		/* enable boxcarring property */
		OMAttribute enableBoxcarring = dsXml.getAttribute(new QName("enableBoxcarring"));
		if (enableBoxcarring != null) {
			setBoxcarring(Boolean.parseBoolean(enableBoxcarring.getAttributeValue()));
		}
        /* disable legacy boxcarring mode property */
        OMAttribute disableLegacyBoxcarringMode = dsXml.getAttribute(new QName(DBSFields.DISABLE_LEGACY_BOXCARRING_MODE));
        if (disableLegacyBoxcarringMode != null) {
            setDisableLegacyBoxcarringMode(Boolean.parseBoolean(disableLegacyBoxcarringMode.getAttributeValue()));
        }

		/* disable streaming property */
		OMAttribute disableStreaming = dsXml.getAttribute(new QName("disableStreaming"));
		if (disableStreaming != null) {
			setDisableStreaming(Boolean.parseBoolean(disableStreaming.getAttributeValue()));
		}
		
		/* Transaction management*/
		Iterator<OMElement> txElements = dsXml.getChildrenWithName(new QName("transactionManagement"));
		while (txElements.hasNext()) {
			OMElement txEle = txElements.next();
			/* txManagerName property */
			OMElement txManagerNameEle = txEle.getFirstChildWithName(new QName("txManagerName"));
			if (txManagerNameEle != null) {
				setTxManagerName(txManagerNameEle.getText());
			}
			/* txManagerClass property */
			OMElement txManagerClassEle = txEle.getFirstChildWithName(new QName("txManagerClass"));
			if (txManagerClassEle != null) {
				setTxManagerClass(txManagerClassEle.getText());
			}
			/* useAppServerTS property */
			OMElement useAppServerTSEle = txEle.getFirstChildWithName(new QName("useAppServerTS"));
			if (useAppServerTSEle != null) {
				setIsUseAppServerTS(Boolean.parseBoolean(useAppServerTSEle.getText()));
			}
			/* enable enableXA property */
			OMElement enableXAEle = txEle.getFirstChildWithName(new QName("enableXA"));
			if (enableXAEle != null) {
				setEnableXA(Boolean.parseBoolean(enableXAEle.getText()));
			}
			/* enable txManagerCleanupMethod property */
			OMElement txManagerCleanupMethodEle = txEle.getFirstChildWithName(new QName("txManagerCleanupMethod"));
			if (txManagerCleanupMethodEle != null) {
				setTxManagerCleanupMethod(txManagerCleanupMethodEle.getText());
			}
		}		
		/* serviceNamespace property */
		OMAttribute serviceNamespaceAttr = dsXml.getAttribute(new QName("serviceNamespace"));
		if (serviceNamespaceAttr != null) {
			setServiceNamespace(serviceNamespaceAttr.getAttributeValue());
		}
		
		/* service status property */
		OMAttribute serviceStatus = dsXml.getAttribute(new QName("serviceStatus"));
		if (serviceStatus != null) {
			setStatus(serviceStatus.getAttributeValue());
		}

        /* available transports */
        OMAttribute transportsConfig = dsXml.getAttribute(new QName("transports"));
        if (transportsConfig != null) {
            String allTransportStr = transportsConfig.getAttributeValue();
            List<String> transportList = Arrays.asList(allTransportStr.split(" "));
            setEnableHTTP(transportList.contains("http"));
            setEnableHTTPS(transportList.contains("https"));
            setEnableLocal(transportList.contains("local"));
            setEnableJMS(transportList.contains("jms"));
        }
		
		/* xmlns:svns property for using securevault */
		OMNamespace secureVaultNamespace = dsXml.findNamespaceURI("svns");
		if (secureVaultNamespace != null) {
			setSecureVaultNamespace(secureVaultNamespace.getNamespaceURI());
		}
		
		/*populate password manager information */
		Iterator<OMElement> pwdMngrElements = dsXml.getChildrenWithName(new QName("passwordManager"));
		if (pwdMngrElements.hasNext()){
			OMElement pwdManagerElement = pwdMngrElements.next();
			OMElement protectedTokenEle =pwdManagerElement.getFirstChildWithName(new QName("protectedTokens"));
			setProtectedTokens(protectedTokenEle.getText());
			OMElement passwordProviderEle =pwdManagerElement.getFirstChildWithName(new QName("passwordProvider"));
			setPasswordProvider(passwordProviderEle.getText());
		}

        /* authorization provider config */
        OMElement authorizationProviderConfigEl = dsXml.getFirstChildWithName(new QName(DBConstants.AuthorizationProviderConfig.ELEMENT_NAME_AUTHORIZATION_PROVIDER));
        if (authorizationProviderConfigEl != null) {
            this.setAuthProvider(this.getAuthorizationProvider(authorizationProviderConfigEl));
        }
		
		/* populate config objects */
		Iterator<OMElement> configElements = dsXml.getChildrenWithName(new QName("config"));
		this.configs = new ArrayList<Config>();
		while (configElements.hasNext()) {
			OMElement configEle = configElements.next();
			this.configs.add(this.getConfig(configEle));
		}		
		/* populate query objects */
		Iterator<OMElement> queryElements = dsXml.getChildrenWithName(new QName("query"));
		this.queries = new ArrayList<Query>();
		while (queryElements.hasNext()) {
			OMElement queryEle = queryElements.next();
			this.queries.add(this.getQuery(queryEle));
		}

        /* populate event objects */
        Iterator<OMElement> events = dsXml.getChildrenWithName(new QName("event-trigger"));
        this.events = new ArrayList<Event>();
        while (events.hasNext()) {
            OMElement eventsEle = events.next();
            this.events.add(this.getEvent(eventsEle));
        }
        
        /* populate XADataSource objects */
        Iterator<OMElement> xADataSource = dsXml.getChildrenWithName(new QName("xa-datasource"));
        this.xADataSources = new ArrayList<XADataSource>();
        while (xADataSource.hasNext()) {
            OMElement xADataSourcesEle = xADataSource.next();
            this.xADataSources.add(this.getXADataSource(xADataSourcesEle));
        }

		/* populate operation objects */
		Iterator<OMElement> operationElements = dsXml.getChildrenWithName(new QName("operation"));
		this.operations = new ArrayList<Operation>();
		while (operationElements.hasNext()) {
			OMElement operationEle = operationElements.next();
			this.operations.add(this.getOperation(operationEle));
		}

		/* populate resource objects */
		Iterator<OMElement> resourcesElements = dsXml.getChildrenWithName(new QName("resource"));
		this.resources = new ArrayList<Resource>();
		while (resourcesElements.hasNext()) {
			OMElement resourceEle = resourcesElements.next();
			this.resources.add(this.getResource(resourceEle));
		}
	}
	
	/**
	 * Populates CallQuery object using it's OM representation.
	 * eg.
	 *   <call-query href="addProduct">
	 *     <with-param name="id" query-param/column="id" />  
	 *     <with-param name="name" query-param/column="name" />  
	 *     <with-param name="price" query-param/column="price" />
	 *   </call-query>  
	 */
	@SuppressWarnings("unchecked")
	private CallQueryGroup getCallQueryGroup(OMElement cqEl){
		CallQueryGroup callQueryGroup = new CallQueryGroup();
		if (cqEl.getLocalName().equals("call-query-group")) {
			OMAttribute requiredRolesattr = cqEl.getAttribute(new QName("requiredRoles"));
	        String requiredRoles = null;
	        if(requiredRolesattr != null){
	            requiredRoles = requiredRolesattr.getAttributeValue();
	        }
	        callQueryGroup.setRequiredRoles(requiredRoles);
			Iterator<OMElement> callQueryItr = cqEl.getChildrenWithName(new QName("call-query"));
			OMElement tmpEl = null;
			while (callQueryItr.hasNext()) {
				tmpEl = callQueryItr.next();
				callQueryGroup.addCallQuery(this.getCallQuery(tmpEl));
			}
		} else { /* 'call-query' */
			callQueryGroup.addCallQuery(this.getCallQuery(cqEl));
		}		
		return callQueryGroup;
	}
	
	@SuppressWarnings("unchecked")
	private CallQuery getCallQuery(OMElement callQueryEle) {
		String href = callQueryEle.getAttributeValue(new QName("href"));
        CallQuery callQuery = new CallQuery();
        callQuery.setHref(href);
        OMAttribute requiredRolesattr = callQueryEle.getAttribute(new QName("requiredRoles"));
        String requiredRoles = null;
        if (requiredRolesattr != null){
            requiredRoles = requiredRolesattr.getAttributeValue();
        }
        callQuery.setRequiredRoles(requiredRoles);
    	/* iterate through parameters */
        Iterator<OMElement> itrWithParamsEles = callQueryEle.getChildrenWithName(new QName("with-param"));
        while (itrWithParamsEles.hasNext()){
        	OMElement withParamEle = itrWithParamsEles.next();
        	String name = withParamEle.getAttributeValue(new QName("name"));
        	String queryParam = withParamEle.getAttributeValue(new QName("query-param"));
        	String columnParam = withParamEle.getAttributeValue(new QName("column"));
        	WithParam withParam = new WithParam();
        	withParam.setName(name);
        	if (columnParam != null) {
        		withParam.setParamValue(columnParam);
        		withParam.setParamType("column");
        	} else {
        		withParam.setParamValue(queryParam);
        		withParam.setParamType("query-param");
        	}
        	callQuery.addWithParam(withParam);

        }
		return callQuery;
	}
	
	/**
	 * Returns operation object containing passed operation name.
	 */
	public Operation getOperation(String operationName) {
		Iterator<Operation> itrOperations = this.getOperations().iterator();
		while (itrOperations.hasNext()) {
			Operation operation = itrOperations.next();
			if (operation.getName().equals(operationName)) {
				return operation;
			}
		}
		return null;
	}

    /**
     * Returns XADataSource object containing passed XADataSource id
     */

    public XADataSource getXADataSource(String xADataSourceId) {
        Iterator<XADataSource> itrXADataSource = this.getXADataSources().iterator();
        while(itrXADataSource.hasNext()) {
        	XADataSource xADataSource = itrXADataSource.next();
            if(xADataSource.getId() != null && xADataSource.getId().equals(xADataSourceId)) {
                return xADataSource;
            }
        }
        return null;
    }
    
    /**
     * Returns Event object containing passed event name
     */

    public Event getEvent(String eventName) {
        Iterator<Event> itrEvent = this.getEvents().iterator();
        while(itrEvent.hasNext()) {
            Event event = itrEvent.next();
            if(event.getId().equals(eventName)) {
                return event;
            }
        }
        return null;
    }
    

    /**
	 * Returns resource object containing passed resource name.
	 */
	public Resource getResource(String resourcePath, String resourceMethod) {
		Iterator<Resource> itrOperations = this.getResources().iterator();
		while (itrOperations.hasNext()) {
			Resource resource = itrOperations.next();
			if (resource.getPath().equals(resourcePath) && resource.getMethod().equals(resourceMethod)) {
				return resource;
			}
		}
		return null;
	}
    /**
	 * Returns an array of Param objects, when an iterator of param elements is passed in.
	 */
	@SuppressWarnings("unchecked")
	public Param[] getParams(Iterator<OMElement> paramItr){
		Param param;
		ArrayList<Param> paramList = new ArrayList<Param>();		
		int ordinal = 0; 
		while (paramItr.hasNext()) {
			//ordinal++;
			OMElement paramElement = paramItr.next();
			/* start: work-a-round to maintain backward compatibility */
			String userSetOrdinalValue = paramElement.getAttributeValue(new QName("ordinal"));
			if (userSetOrdinalValue == null || userSetOrdinalValue.trim().length() == 0){
				userSetOrdinalValue = String.valueOf(ordinal);
			}
			/* end: work-a-round to maintain backward compatibility */
			param = new Param(paramElement.getAttributeValue(new QName("name")),
                        paramElement.getAttributeValue(new QName("paramType")),
						paramElement.getAttributeValue(new QName("sqlType")),
						paramElement.getAttributeValue(new QName("type")),
						userSetOrdinalValue,
                        paramElement.getAttributeValue(new QName("defaultValue")),
                        paramElement.getAttributeValue(new QName("structType")),
                        this.getValidators(paramElement.getChildElements()),
                        paramElement.getAttributeValue(new QName("optional"))
                        );
			paramList.add(param);
		}
		Param[] params = new Param[paramList.size()];
		paramList.toArray(params);
		return params;		
	}

    @SuppressWarnings("unchecked")
	private List<Validator> getValidators(Iterator<OMElement> valItr) {
        List<Validator> vals = new ArrayList<Validator>();
        OMElement valEl;
        String valElementName;
        Iterator<OMAttribute> attrItr;
        Map<String,  String> propMap;
        OMAttribute attr;
        while (valItr.hasNext()) {
            valEl = valItr.next();
            valElementName = valEl.getLocalName();
            attrItr = valEl.getAllAttributes();
            propMap = new HashMap<String,  String>();
            while (attrItr.hasNext()) {
                attr = attrItr.next();
                propMap.put(attr.getLocalName(), attr.getAttributeValue());
            }
            Map<String, String> customPropMap = extractAdvancedProps(valEl);
            vals.add(new Validator(valElementName, propMap, customPropMap));
        }
        return vals;
    }
	
	/**
	 * Returns query object containing passed query id.
	 */
	public Query getQuery(String queryId) {
		Iterator<Query> itrQueries = queries.iterator();
		while (itrQueries.hasNext()) {
			Query query = itrQueries.next();
			if (query.getId().equals(queryId)) {
				return query;
			}
		}
		return null;
	}

	public OMElement buildXML() {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement dataEl = fac.createOMElement("data", null);
		if (this.getName() != null) {
            dataEl.addAttribute("name", this.getName(), null);
		}
		if (this.getDescription() != null && this.getDescription().trim().length() > 0) {
			OMElement descEl = fac.createOMElement("description", null);
			descEl.setText(this.getDescription());
			dataEl.addChild(descEl);
		}
		if (this.isBatchRequest()) {
			 dataEl.addAttribute("enableBatchRequests", String.valueOf(this.isBatchRequest()), null);
		}
        if (this.isBoxcarring()) {
        	dataEl.addAttribute("enableBoxcarring", String.valueOf(this.isBoxcarring()), null);
        }

        if (this.isDisableLegacyBoxcarringMode()) {
            dataEl.addAttribute(DBSFields.DISABLE_LEGACY_BOXCARRING_MODE,
                                String.valueOf(this.isDisableLegacyBoxcarringMode()), null);
        }

        if (this.isDisableStreaming()) {
        	dataEl.addAttribute("disableStreaming", String.valueOf(this.isDisableStreaming()), null);
        }

        List <String> transports = new ArrayList<String>();
        if (this.isEnableHTTP()) {
            transports.add("http");
        }
        if (this.isEnableHTTPS()) {
            transports.add("https");
        }
        if (this.isEnableLocal()) {
            transports.add("local");
        }
        if (this.isEnableJMS()) {
            transports.add("jms");
        }
        if (transports.size() > 0) {
            String transportStr = "";
            for (String trn : transports) {
                transportStr = transportStr + " " + trn;
            }
            transportStr = transportStr.substring(1, transportStr.length());
            dataEl.addAttribute("transports", transportStr, null);
        }
        
//		if (this.isEnableXA()) {
//			OMElement txManagementEle = fac.createOMElement("transactionManagement", null);
//			OMElement enableXAEl = fac.createOMElement("enableXA", null);
//			enableXAEl.setText(String.valueOf(this.isEnableXA()));
//			txManagementEle.addChild(enableXAEl);
//			if (this.isUseAppServerTS()) {
//				OMElement useAppServerTSEl = fac.createOMElement("useAppServerTS", null);
//				useAppServerTSEl.setText(String.valueOf(this.isUseAppServerTS()));
//				txManagementEle.addChild(useAppServerTSEl);
//			}
//			if (this.getTxManagerCleanupMethod() != null
//					&& this.getTxManagerCleanupMethod().trim().length() > 0) {
//				OMElement txManagerCleanupMethodEl = fac.createOMElement("txManagerCleanupMethod",
//						null);
//				txManagerCleanupMethodEl.setText(this.getTxManagerCleanupMethod().trim());
//				txManagementEle.addChild(txManagerCleanupMethodEl);
//			}
//			if (this.getTxManagerClass() != null && this.getTxManagerClass().trim().length() > 0) {
//				OMElement txManagerClassEl = fac.createOMElement("txManagerClass", null);
//				txManagerClassEl.setText(this.getTxManagerClass().trim());
//				txManagementEle.addChild(txManagerClassEl);
//			}
////			if (this.isUseAppServerTS() && this.getTxManagerName() != null
////					&& this.getTxManagerName().trim().length() > 0) {
////				OMElement txManagerNameEl = fac.createOMElement("txManagerJNDIName", null);
////				txManagerNameEl.setText(this.getTxManagerName().trim());
////				txManagementEle.addChild(txManagerNameEl);
////			}
//            if (this.getTxManagerName() != null
//					&& this.getTxManagerName().trim().length() > 0) {
//				OMElement txManagerNameEl = fac.createOMElement("txManagerJNDIName", null);
//				txManagerNameEl.setText(this.getTxManagerName().trim());
//				txManagementEle.addChild(txManagerNameEl);
//			}
//			dataEl.addChild(txManagementEle);
//		}

		if (this.getServiceNamespace() != null && this.getServiceNamespace().trim().length() > 0) {
			dataEl.addAttribute("serviceNamespace", this.getServiceNamespace().trim(), null);
		}
		
		if (this.getSecureVaultNamespace() != null && this.getSecureVaultNamespace().trim().length() > 0) {
			dataEl.addAttribute("xmlns:svns", this.getSecureVaultNamespace() , null);
		}

        //adding password manager to the dbs configuration
        if((this.getPasswordProvider() != null && this.getProtectedTokens() != null) && (this.getPasswordProvider().trim().length() > 0  && this.getProtectedTokens().trim().length() > 0)  ){
        	OMElement passwordManagerEl = fac.createOMElement("passwordManager", null);
    		OMElement protectedTokensEl = fac.createOMElement("protectedTokens", null);
    		protectedTokensEl.setText(this.getProtectedTokens());
    		OMElement passwordProviderEl = fac.createOMElement("passwordProvider", null);
    		passwordProviderEl.setText(this.getPasswordProvider());
    		passwordManagerEl.addChild(protectedTokensEl);
    		passwordManagerEl.addChild(passwordProviderEl);		
            dataEl.addChild(passwordManagerEl);
        }
        
        /* build WIP */
        if (this.getStatus() != null && !(this.getStatus().equals("active"))) {
            dataEl.addAttribute("serviceStatus", this.getStatus(), null);
        }
		/* build configs */
		if (this.getConfigs() != null) {
			for (Config config : this.getConfigs()) {
				dataEl.addChild(config.buildXML());
			}
		}
		/* build queries */
		if (this.getQueries() != null) {
			for (Query query : this.getQueries()) {
				dataEl.addChild(query.buildXML());
			}
		}
        /* build events */
        if (this.getEvents() != null) {
            for(Event event : this.getEvents()) {
                dataEl.addChild(event.buildXML());
            }
        }
        
        /* build XADataSources */
        if (this.getXADataSources() != null) {
            for(XADataSource xaDataSources : this.getXADataSources() ) {
                dataEl.addChild(xaDataSources.buildXML());
            }
        }
        
		/* build operations */
		if (this.getOperations() != null) {
			for (Operation operation : this.getOperations()) {
				dataEl.addChild(operation.buildXML());
			}
		}
		/* build resources */
		if (this.getResources() != null) {
			for (Resource resource : this.getResources()) {
				dataEl.addChild(resource.buildXML());
			}
		}
        /* add authprovider to config */
        if (this.getAuthProvider() != null) {
            dataEl.addChild(this.getAuthProvider().buildXML());
        }
		return dataEl;
	}

}
