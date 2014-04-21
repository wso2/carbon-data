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
package org.wso2.carbon.dataservices.core;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.DBSFields;
import org.wso2.carbon.dataservices.common.DBConstants.RDBMSEngines;
import org.wso2.carbon.dataservices.common.RDBMSUtils;
import org.wso2.carbon.dataservices.core.description.config.Config;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.dataservices.core.engine.ExternalParam;
import org.wso2.carbon.dataservices.core.engine.ExternalParamCollection;
import org.wso2.carbon.dataservices.core.engine.InternalParam;
import org.wso2.carbon.dataservices.core.engine.ParamValue;
import org.wso2.carbon.dataservices.core.internal.DataServicesDSComponent;
import org.wso2.carbon.ndatasource.core.utils.DataSourceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.utils.xml.XMLPrettyPrinter;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.naming.InitialContext;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for data services based operations.
 */
public class DBUtils {

    private static final Log log = LogFactory.getLog(DBUtils.class);

    private static Pattern udtPattern = Pattern.compile("(.*?(\\[\\d\\]))");

    private static ScheduledExecutorService globalExecutorService = Executors
            .newSingleThreadScheduledExecutor();

    private static HashMap<String, String> conversionTypes = null;

    private static HashMap<String, String> xsdSqlTypeMap = null;

    /* initialize the conversion types */

    static {
        conversionTypes = new HashMap<String, String>();
        conversionTypes.put(DBConstants.DataTypes.CHAR, "java.lang.String");
        conversionTypes.put(DBConstants.DataTypes.STRING, "java.lang.String");
        conversionTypes.put(DBConstants.DataTypes.QUERY_STRING, "java.lang.String");
        conversionTypes.put(DBConstants.DataTypes.VARCHAR, "java.lang.String");
        conversionTypes.put(DBConstants.DataTypes.NVARCHAR, "java.lang.String");
        conversionTypes.put(DBConstants.DataTypes.TEXT, "java.lang.String");
        conversionTypes.put(DBConstants.DataTypes.NUMERIC, "java.math.BigDecimal");
        conversionTypes.put(DBConstants.DataTypes.DECIMAL, "java.math.BigDecimal");
        conversionTypes.put(DBConstants.DataTypes.MONEY, "java.math.BigDecimal");
        conversionTypes.put(DBConstants.DataTypes.SMALLMONEY, "java.math.BigDecimal");
        conversionTypes.put(DBConstants.DataTypes.BIT, "boolean");
        conversionTypes.put(DBConstants.DataTypes.BOOLEAN, "boolean");
        conversionTypes.put(DBConstants.DataTypes.TINYINT, "byte");
        conversionTypes.put(DBConstants.DataTypes.SMALLINT, "short");
        conversionTypes.put(DBConstants.DataTypes.INTEGER, "int");
        conversionTypes.put(DBConstants.DataTypes.BIGINT, "long");
        conversionTypes.put(DBConstants.DataTypes.REAL, "float");
        conversionTypes.put(DBConstants.DataTypes.FLOAT, "double");
        conversionTypes.put(DBConstants.DataTypes.DOUBLE, "double");
        conversionTypes.put(DBConstants.DataTypes.BINARY, "base64Binary"); /* byte[] */
        conversionTypes.put(DBConstants.DataTypes.VARBINARY, "base64Binary"); /* byte[] */
        conversionTypes.put(DBConstants.DataTypes.LONG_VARBINARY, "base64Binary"); /* byte [] */
        conversionTypes.put(DBConstants.DataTypes.IMAGE, "base64Binary"); /* byte[] */
        conversionTypes.put(DBConstants.DataTypes.DATE, "java.sql.Date");
        conversionTypes.put(DBConstants.DataTypes.TIME, "java.sql.Time");
        conversionTypes.put(DBConstants.DataTypes.TIMESTAMP, "java.sql.Timestamp");
        conversionTypes.put(DBConstants.DataTypes.ANYURI, "java.net.URI");
        conversionTypes.put(DBConstants.DataTypes.STRUCT, "java.sql.Struct");
        
        conversionTypes.put(DBConstants.DataTypes.VARINT, "java.math.BigInteger");
        conversionTypes.put(DBConstants.DataTypes.UUID, "java.lang.String");
        conversionTypes.put(DBConstants.DataTypes.INETADDRESS, "java.lang.String");

        xsdSqlTypeMap = new HashMap<String, String>();
        xsdSqlTypeMap.put("string", DBConstants.DataTypes.STRING);
        xsdSqlTypeMap.put("boolean", DBConstants.DataTypes.BOOLEAN);
        xsdSqlTypeMap.put("int", DBConstants.DataTypes.INTEGER);
        xsdSqlTypeMap.put("integer", DBConstants.DataTypes.INTEGER);
        xsdSqlTypeMap.put("long", DBConstants.DataTypes.LONG);
        xsdSqlTypeMap.put("float", DBConstants.DataTypes.FLOAT);
        xsdSqlTypeMap.put("double", DBConstants.DataTypes.DOUBLE);
        xsdSqlTypeMap.put("decimal", DBConstants.DataTypes.DECIMAL);
        xsdSqlTypeMap.put("dateTime", DBConstants.DataTypes.TIMESTAMP);
        xsdSqlTypeMap.put("time", DBConstants.DataTypes.TIME);
        xsdSqlTypeMap.put("date", DBConstants.DataTypes.DATE);
        xsdSqlTypeMap.put("base64Binary", DBConstants.DataTypes.BINARY);
        xsdSqlTypeMap.put("binary", DBConstants.DataTypes.BINARY);
    }
    
    private static SecretResolver secretResolver;

    private static XMLOutputFactory xmlOutputFactory;

    /** pre-fetch the XMLOutputFactory */
    static {
        xmlOutputFactory = XMLOutputFactory.newInstance();
    }

    private static XMLInputFactory xmlInputFactory;

    /** pre-fetch the XMLInputFactory */
    static {
        xmlInputFactory = XMLInputFactory.newInstance();
    }

    private static OMFactory omFactory;

    /** pre-fetch the OMFactory */
    static {
        omFactory = OMAbstractFactory.getOMFactory();
    }

    public static XMLOutputFactory getXMLOutputFactory() {
        return xmlOutputFactory;
    }

    public static XMLInputFactory getXMLInputFactory() {
        return xmlInputFactory;
    }

    public static OMFactory getOMFactory() {
        return omFactory;
    }

    /**
     * Converts from DS SQL types to Java types, e.g. "STRING" -> "java.lang.String".
     */
    public static String getJavaTypeFromSQLType(String sqlType) {
        return conversionTypes.get(sqlType);
    }

    /**
     * Converts from XML schema types to DS SQL types, e.g. "string" -> "STRING".
     */
    public static String getSQLTypeFromXsdType(String xsdType) {
        String sqlType = xsdSqlTypeMap.get(xsdType);
        if (sqlType == null) {
            sqlType = DBConstants.DataTypes.STRING;
        }
        return sqlType;
    }

    public static String getCurrentContextUsername() {
        MessageContext ctx = MessageContext.getCurrentMessageContext();
        if (ctx != null) {
            return getUsername(ctx);
        } else {
            return null;
        }
    }

    public static String getUsername(MessageContext msgContext) {
        String userName = (String) msgContext.getProperty(
                DBConstants.MSG_CONTEXT_USERNAME_PROPERTY);
        return userName;
    }

    /**
     * Retrieves the current user's roles given the message context.
     * @param msgContext The message context to be used to retrieve the username
     * @return The user roles
     * @throws DataServiceFault
     */
    public static String[] getUserRoles(MessageContext msgContext)
            throws DataServiceFault {
        return getUserRoles(DBUtils.getUsername(msgContext));
    }
    
    /**
     * Retrieves the current user's roles given the username.
     *
     * @param username The username
     * @return The user roles
     * @throws DataServiceFault
     */
    public static String[] getUserRoles(String username) throws DataServiceFault {
    	RealmService realmService = DataServicesDSComponent.getRealmService();
        RegistryService registryService = DataServicesDSComponent.getRegistryService();
        username = MultitenantUtils.getTenantAwareUsername(username);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        username = MultitenantUtils.getTenantAwareUsername(username);
        try {
            if (tenantId < MultitenantConstants.SUPER_TENANT_ID) {
                tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            }
            if (tenantId < MultitenantConstants.SUPER_TENANT_ID) {
                /* the tenant doesn't exist. */
                log.error("The tenant doesn't exist. Tenant domain:" + tenantDomain);
                throw new DataServiceFault("Access Denied. You are not authorized.");
            }
            if (!realmService.getTenantManager().isTenantActive(tenantId)) {
                /* the tenant is not active. */
                log.error("The tenant is not active. Tenant domain:" + tenantDomain);
                throw new DataServiceFault("The tenant is not active. Tenant domain:"
                        + tenantDomain);
            }
            UserRealm realm = registryService.getUserRealm(tenantId);
            String roles[] = realm.getUserStoreManager().getRoleListOfUser(username);
            return roles;
        } catch (Exception e) {
            String msg = "Error in retrieving the realm for the tenant id: " + tenantId
                    + ", username: " + username + ". " + e.getMessage();
            log.error(msg);
            throw new DataServiceFault(msg);
        }
    }
    
    public static boolean authenticate(String username, String password) throws DataServiceFault {
    	try {
            RegistryService registryService = DataServicesDSComponent.getRegistryService();
            UserRealm realm = registryService.getUserRealm(
            		PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
    		username = MultitenantUtils.getTenantAwareUsername(username);
    		return realm.getUserStoreManager().authenticate(username, password);
    	} catch (Exception e) {
			throw new DataServiceFault(e, "Error in authenticating user '" + username + "'");
		}
    }

    public static boolean isRegistryPath(String path) {
        if (path.startsWith(DBConstants.CONF_REGISTRY_PATH_PREFIX) || path.startsWith(DBConstants.GOV_REGISTRY_PATH_PREFIX)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates and returns an InputStream from the file path / http location given.
     *
     * @throws DataServiceFault
     * @see InputStream
     */
    public static InputStream getInputStreamFromPath(String path) throws IOException,
            DataServiceFault {
        InputStream ins;
        if (path.startsWith("http://")) {
            /* This is a url file path */
            URL url = new URL(path);
            ins = url.openStream();
        } else if (isRegistryPath(path)) {
            try {
                RegistryService registryService = DataServicesDSComponent.getRegistryService();
                if (registryService == null) {
                    throw new DataServiceFault("DBUtils.getInputStreamFromPath(): Registry service is not available");
                }
                Registry registry;
                if (path.startsWith(DBConstants.CONF_REGISTRY_PATH_PREFIX)) {
                    if (path.length() > DBConstants.CONF_REGISTRY_PATH_PREFIX.length()) {
                        path = path.substring(DBConstants.CONF_REGISTRY_PATH_PREFIX.length());
                        registry = registryService.getConfigSystemRegistry(getCurrentTenantId());
                    } else {
                        throw new DataServiceFault("Empty configuration registry path given");
                    }
                } else {
                    if (path.length() > DBConstants.GOV_REGISTRY_PATH_PREFIX.length()) {
                        path = path.substring(DBConstants.GOV_REGISTRY_PATH_PREFIX.length());
                        registry = registryService.getGovernanceSystemRegistry(getCurrentTenantId());
                    } else {
                        throw new DataServiceFault("Empty governance registry path given");
                    }
                }
                if (registry.resourceExists(path)) {
                    Resource serviceResource = registry.get(path);
                    ins = serviceResource.getContentStream();
                } else {
                    throw new DataServiceFault(
                            "The given XSLT resource path at '" + path + "' does not exist");
                }
            } catch (RegistryException e) {
                String msg = "Error in retrieving the resource: " + path;
                log.error(msg, e);
                throw new DataServiceFault(e, msg);
            }
        } else {
            File csvFile = new File(path);
            if (path.startsWith("." + File.separator) || path.startsWith(".." + File.separator)) {
                /* this is a relative path */
                path = csvFile.getAbsolutePath();
            }
            /* local file */
            ins = new FileInputStream(path);
        }
        return ins;
    }

    /**
     * create a map which maps the column numbers to column names,
     * column numbers starts with 1 (1 based).
     */
    public static Map<Integer, String> createColumnMappings(String[] header) throws IOException {
        Map<Integer, String> mappings = null;
        if (header != null) {
            mappings = new HashMap<Integer, String>();
            /* add mappings: column index -> column name */
            for (int i = 0; i < header.length; i++) {
                mappings.put(i + 1, header[i]);
            }
        } else {
            mappings = new StringNumberMap();
        }
        return mappings;
    }

    /**
     * This class represents a Map class which always returns the value same as the key.
     */
    private static class StringNumberMap extends AbstractMap<Integer, String> {

        public Set<Map.Entry<Integer, String>> entrySet() {
            return null;
        }

        @Override
        public String get(Object key) {
            return key.toString();
        }

    }

    /**
     * Utility method that returns a string which contains the stack trace of the given
     * Exception object.
     */
    public static String getStacktraceFromException(Throwable e) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(byteOut);
        e.printStackTrace(writer);
        writer.close();
        String message = new String(byteOut.toByteArray());
        return message;
    }

    /**
     * Returns the most suitable value for the JDBC Result Set FetchSize property,
     * for the DBMS engine of the given JDBC URL.
     */
    public static int getOptimalRSFetchSizeForRDBMS(String jdbcUrl) {
        if (jdbcUrl == null) {
            return 1;
        }
        String rdbms = RDBMSUtils.getRDBMSEngine(jdbcUrl);
        if (rdbms.equals(RDBMSEngines.MYSQL)) {
            return Integer.MIN_VALUE;
        } else {
            return 1;
        }
    }

    /**
     * Create a Timestamp object from the given timestamp string.
     */
    public static Timestamp getTimestamp(String value) throws ParseException {
        return new Timestamp(ConverterUtil.convertToDateTime(value).getTimeInMillis());
    }

    public static void main(String[] args) throws Exception {
        Timestamp ts = getTimestamp("2011-12-01T00:00:00.000+01:00");
        System.out.println(ts.toString());
    }

    /**
     * Create a Time object from the given time string.
     */
    public static Time getTime(String value) throws ParseException {
        return new Time(ConverterUtil.convertToTime(value).getAsCalendar().getTimeInMillis());
    }

    /**
     * Create a Date object from the given date string.
     */
    public static Date getDate(String value) {
        /* if something goes wrong with converting the value to a date,
           * try with dateTime and get the date out it, this is because,
           * some service clients send a full date-time string for a date */
        try {
            return new Date(ConverterUtil.convertToDate(value).getTime());
        } catch (Exception e) {
            return new Date(ConverterUtil.convertToDateTime(value).getTimeInMillis());
        }
    }

    /**
     * Prettify a given XML string
     */
    public static String prettifyXML(String xmlContent) {
    	Element element = DataSourceUtils.stringToElement(xmlContent);
    	if (element == null) {
    	    throw new RuntimeException("Error in converting string to XML: " + xmlContent);
    	}
		removeWhitespaceInMixedContentElements(element);
    	xmlContent = DataSourceUtils.elementToString(element);
        ByteArrayInputStream byteIn = new ByteArrayInputStream(xmlContent.getBytes());
        XMLPrettyPrinter prettyPrinter = new XMLPrettyPrinter(byteIn);
        return prettyPrinter.xmlFormat().trim();
    }
    
    private static List<Node> getNodesAsList(Element element) {
    	List<Node> nodes = new ArrayList<Node>();
    	NodeList nodeList = element.getChildNodes();
    	int count = nodeList.getLength();
    	for (int i = 0; i < count; i++) {
    		nodes.add(nodeList.item(i));
    	}
    	return nodes;
    }
    
    private static List<Element> getChildElements(Element element) {
    	List<Element> childEls = new ArrayList<Element>();
    	for (Node tmpNode : getNodesAsList(element)) {
    		if (tmpNode.getNodeType() == Node.ELEMENT_NODE) {
    			childEls.add((Element) tmpNode);
    		}
    	}
    	return childEls;
    }
    
    private static List<Node> getWhitespaceNodes(Element element) {
    	List<Node> nodes = new ArrayList<Node>();
    	for (Node node : getNodesAsList(element)) {
    		if (node.getNodeType() == Node.TEXT_NODE && 
    				node.getNodeValue().trim().length() == 0) {
    			nodes.add(node);
    		}
    	}
    	return nodes;
    }
    
	private static void removeWhitespaceInMixedContentElements(Element element)  {
    	List<Element> childEls = getChildElements(element);
    	if (childEls.size() > 0) {
    		for (Node node : getWhitespaceNodes(element)) {
    			element.removeChild(node);
    		}
    		for (Element childEl : childEls) {
    			removeWhitespaceInMixedContentElements(childEl);
    		}
    	}
    }

    /**
     * Prettify a given XML file
     */
    public static void prettifyXMLFile(String filePath) throws IOException {
        String prettyXML = prettifyXML(FileUtils.readFileToString(new File(filePath)));
        FileUtils.writeStringToFile(new File(filePath), prettyXML);
    }

    /**
     * Encode the given string with base64 encoding.
     */
    public static String encodeBase64(String value) {
        try {
            return new String(Base64.encodeBase64(value.getBytes(
                    DBConstants.DEFAULT_CHAR_SET_TYPE)),
                    DBConstants.DEFAULT_CHAR_SET_TYPE);
        } catch (UnsupportedEncodingException ueo) {
            throw new RuntimeException(ueo);
        }
    }

    /**
     * Creates an AxisFault.
     */
    public static AxisFault createAxisFault(Exception e) {
        AxisFault fault;
        Throwable cause = e.getCause();
        if (cause != null) {
            fault = new AxisFault(e.getMessage(), cause);
        } else {
            fault = new AxisFault(e.getMessage());
        }
        fault.setDetail(DataServiceFault.extractFaultMessage(e));
        fault.setFaultCode(new QName(DBConstants.WSO2_DS_NAMESPACE,
                DataServiceFault.extractFaultCode(e)));
        return fault;
    }

    /**
     * Creates OMElement using error details.
     */
    public static OMElement createDSFaultOM(String msg) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement ele = fac.createOMElement(new QName(
                DBConstants.WSO2_DS_NAMESPACE, DBConstants.DS_FAULT_ELEMENT));
        ele.setText(msg);
        return ele;
    }

    public static String evaluateString(String source,
                                        ExternalParamCollection params) throws DataServiceFault {
        StringBuilder builder = new StringBuilder();
        /* http://www.product.fake/cd/{productCode} */
        int leftBracketIndex = source.indexOf('{', 0);
        int rightBracketIndex = source.indexOf('}', leftBracketIndex);
        if (leftBracketIndex == -1 || rightBracketIndex == -1) {
            throw new DataServiceFault("The source string: " + source + " is not parameterized.");
        }
        String paramName = source.substring(leftBracketIndex + 1, rightBracketIndex);
        /* workaround for different character case issues in column names */
        paramName = paramName.toLowerCase();
        ExternalParam exParam = params.getParam(paramName);
        if (exParam == null) {
            throw new DataServiceFault("The parameter: " + paramName +
                    " cannot be found for the source string: " + source);
        }
        String paramValue = exParam.getValue().getValueAsString();
        builder.append(source.subSequence(0, leftBracketIndex));
        builder.append(paramValue);
        builder.append(source.substring(rightBracketIndex + 1));
        return builder.toString();
    }

    /**
     * Schedules a given task for one-time execution using the executer framework.
     *
     * @param task  The task to be executed
     * @param delay The delay in milliseconds for the task to be executed
     */
    public static void scheduleTask(Runnable task, long delay) {
        globalExecutorService.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Check the given text is empty or not.
     *
     * @param text The text to be checked
     * @return true if text is null or trimmed text length is empty, or else false
     */
    public static boolean isEmptyString(String text) {
        if (text != null && text.trim().length() > 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check the given password is encrypted or not, if its encrypted resolve the password.
     *
     * @param dataService Data service object
     * @param password    Password before resolving
     * @return Resolved password
     */
    public static String resolvePasswordValue(DataService dataService, String password) {
        SecretResolver secretResolver = dataService.getSecretResolver();
        if (secretResolver != null && secretResolver.isTokenProtected(password)) {
            return secretResolver.resolve(password);
        } else {
            return password;
        }
    }

    /**
     * Returns the best effort way of finding the current tenant id,
     * even if this is not in a current message request, i.e. deploying services.
     * Assumption: when tenants other than the super tenant is activated,
     * the registry service must be available. So, the service deployment and accessing the registry,
     * will happen in the same thread, without the callbacks being used.
     *
     * @return The tenant id
     */
    public static int getCurrentTenantId() {
    	try {
	    	int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
	    	if (tenantId == -1) {
                     throw new RuntimeException("Tenant id cannot be -1");
	        }
	    	return tenantId;
    	} catch (NoClassDefFoundError e) { // Workaround for Unit Test failure 
    		return MultitenantConstants.SUPER_TENANT_ID;
    	} catch (ExceptionInInitializerError e) {
    		return MultitenantConstants.SUPER_TENANT_ID;
    	}
        
    }

    /**
     * Returns the simple schema type from the type name,
     */
    public static QName getSimpleSchemaTypeName(TypeTable typeTable, String typeName) {
        if (typeName.equals("java.net.URI")) {
            return new QName(DBConstants.XSD_NAMESPACE, "anyURI");
        }
        if (typeName.equals("java.sql.Struct")) {
            return new QName(DBConstants.XSD_NAMESPACE, "anyType");
        }
        return typeTable.getSimpleSchemaTypeName(typeName);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> extractProperties(OMElement propsParentEl) {
        Map<String, String> properties = new HashMap<String, String>();
        OMElement propEl = null;
        Iterator<OMElement> itr = propsParentEl.getChildrenWithName(new QName(DBSFields.PROPERTY));
        String text;
        while (itr.hasNext()) {
            propEl = itr.next();
            if (propEl.getChildElements().hasNext()) {
                text = propEl.toString();
            } else {
                text = propEl.getText();
            }
            if(text != null && !text.equals("")) {
            	properties.put(propEl.getAttributeValue(new QName(DBSFields.NAME)), text);
            }
        }
        return properties;
    }

    /**
     * Get the container managed transaction manager; if a JNDI name is given,
     * that name is looked for a TransactionManager object, if not, the standard JNDI
     * names are checked.
     *
     * @param txManagerJNDIName The user given JNDI name of the TransactionManager
     * @return The TransactionManager object
     * @throws DataServiceFault
     */
    public static TransactionManager getContainerTransactionManager(String txManagerJNDIName)
            throws DataServiceFault {
        TransactionManager txManager = null;
        if (txManagerJNDIName != null) {
            try {
                txManager = InitialContext.doLookup(txManagerJNDIName);
            } catch (Exception e) {
                throw new DataServiceFault(e,
                        "Cannot find TransactionManager with the given JNDI name '" +
                                txManagerJNDIName + "'");
            }
        }
        /* get the transaction manager from the well known JNDI names from the cache */
        txManager = DBDeployer.getCachedTransactionManager();
        return txManager;
    }

    /**
     * Creates a new OMElement from the given element and build it and return.
     *
     * @param result The object to be cloned and built
     * @return The new cloned and built OMElement
     */
    public static OMElement cloneAndReturnBuiltElement(OMElement result) {
        StAXOMBuilder builder = new StAXOMBuilder(result.getXMLStreamReaderWithoutCaching());
        result = builder.getDocumentElement();
        result.build();
        return result;
    }

    /**
     * This util method is used to retrieve the string tokens resides in a particular
     * udt parameter.
     *
     * @param param Name of the parameter
     * @return
     */
    public static Queue<String> getTokens(String param) {
        boolean isString = false;
        Queue<String> tokens = new LinkedBlockingQueue<String>();
        char[] chars = param.toCharArray();
        StringBuilder columnName = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            Character c = chars[i];
            if (!".".equals(c.toString()) && !"[".equals(c.toString()) &&
                    !"]".equals(c.toString())) {
                isString = true;
                columnName.append(c.toString());
                if (i == chars.length - 1) {
                    tokens.add(columnName.toString());
                }
            } else {
                if (isString) {
                    tokens.add(columnName.toString());
                    columnName = new StringBuilder();
                    isString = false;
                }
                tokens.add(c.toString());
            }

        }
        return tokens;
    }

    /**
     * This method is used to embed syntaxes associated with UDT attribute notations to
     * a queue of string tokens extracted from a UDT parameter.
     *
     * @param tokens      Queue of string tokens
     * @param syntaxQueue Syntax embedded tokens
     * @param isIndex     Flag to determine whether a particular string token is an inidex
     *                    or a column name
     */
    public static void getSyntaxEmbeddedQueue(Queue<String> tokens, Queue<String> syntaxQueue,
                                              boolean isIndex) {
        if (!tokens.isEmpty()) {
            if ("[".equals(tokens.peek())) {
                isIndex = true;
                tokens.poll();
                syntaxQueue.add("INEDX_START");
                syntaxQueue.add(tokens.poll());
            } else if ("]".equals(tokens.peek())) {
                isIndex = false;
                tokens.poll();
                syntaxQueue.add("INDEX_END");
            } else if (".".equals(tokens.peek())) {
                tokens.poll();
                syntaxQueue.add("DOT");
                syntaxQueue.add("COLUMN");
                syntaxQueue.add(tokens.poll());
            } else {
                if (isIndex) {
                    syntaxQueue.add("INDEX");
                    syntaxQueue.add(tokens.poll());
                } else {
                    syntaxQueue.add("COLUMN");
                    syntaxQueue.add(tokens.poll());
                }
            }
            getSyntaxEmbeddedQueue(tokens, syntaxQueue, isIndex);
        }
    }

    public static String getConnectionURL4XADataSource(Config config) throws XMLStreamException {
        String connectionURL = null;
        String connectionProperty = config.getProperty(DBConstants.RDBMS.DATASOURCE_PROPS);
        if (connectionProperty != null) {
            OMElement payload = AXIOMUtil.stringToOM(connectionProperty);
            Map<String, String> properties = extractProperties(payload);
            Collection<String> propValues = properties.values();
            for (String propValue : propValues) {
                if (propValue.startsWith("jdbc:")) {
                    connectionURL = propValue;
                    break;
                }
            }
        }
        return connectionURL;
    }

    public static boolean isUDT(ParamValue paramValue) {
        return paramValue != null && (paramValue.getValueType() == ParamValue.PARAM_VALUE_UDT);
    }

    public static boolean isSQLArray(ParamValue paramValue) {
        return paramValue != null && (paramValue.getValueType() == ParamValue.PARAM_VALUE_ARRAY);
    }

    /**
     * Util method to parse index string and produce the list of nested indices.
     *
     * @param indexString Index String.
     * @return The list of nested indices.
     * @throws DataServiceFault DataServiceFault.
     */
    public static List<Integer> getNestedIndices(String indexString) throws DataServiceFault {
        List<Integer> indices = new ArrayList<Integer>();
        String[] temp = indexString.split("\\[");
        for (String s : temp) {
            if (!"".equals(s)) {
                try {
                    indices.add(Integer.parseInt(s.substring(0, s.indexOf("]"))));
                } catch (NumberFormatException e) {
                    throw new DataServiceFault("Unable to determine nested indices. Incompatible " +
                            "value specified for the attribute index");
                }
            }
        }
        return indices;
    }

    /**
     * Processes a particular SQL Array object and interprets its value as a ParamValue object.
     *
     * @param sqlArray   SQL Array element.
     * @param paramValue Parameter value object initialized to contain an array of ParamValues.
     * @return ParamValue object representing the SQL Array.
     * @throws SQLException Throws an SQL Exception if the result set is not accessible.
     */
    public static ParamValue processSQLArray(Array sqlArray,
                                             ParamValue paramValue) throws SQLException {
        ResultSet rs = sqlArray.getResultSet();
        while (rs.next()) {
            Object arrayEl = rs.getObject(2);
            if (arrayEl instanceof Struct) {
                paramValue.getArrayValue().add(new ParamValue((Struct) arrayEl));
            } else if (arrayEl instanceof Array) {
                paramValue.getArrayValue().add(processSQLArray(
                        (Array) arrayEl, new ParamValue(ParamValue.PARAM_VALUE_ARRAY)));
            } else {
                paramValue.getArrayValue().add(new ParamValue(String.valueOf(arrayEl)));
            }
        }
        rs.close();
        return paramValue;
    }

    /**
     * Extracts the UDT column name from a given parameter name
     *
     * @param param User specified parameter name
     * @return UDT column name
     */
    public static String extractUDTObjectName(String param) {
        Matcher m = udtPattern.matcher(param);
        if (m.find()) {
            String tmp = m.group();
            return tmp.substring(0, tmp.length() - 3).trim();
        }
        return null;
    }
    
	public static synchronized String loadFromSecureVault(String alias) {
		if (secretResolver == null) {
		    secretResolver = SecretResolverFactory.create((OMElement) null, false);
		    secretResolver.init(DataServicesDSComponent.
		    		getSecretCallbackHandlerService().getSecretCallbackHandler());
		}
		return secretResolver.resolve(alias);
	}
	
	public static OMElement wrapBoxCarringResponse(OMElement result) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement wrapperElement = fac.createOMElement(new QName(DBConstants.WSO2_DS_NAMESPACE,
                DBConstants.DATA_SERVICE_RESPONSE_WRAPPER_ELEMENT));
		if (result != null) {
			wrapperElement.addChild(result);
		}
		OMDocument doc = fac.createOMDocument();
		doc.addChild(wrapperElement);
		return doc.getOMDocumentElement();
	}
	
	public static void populateStandardCustomDSProps(Map<String, String> dsProps, 
			DataService dataService, Config config) {
		String dsInfo = dataService.getTenantId() + "#"
				+ dataService.getName() + "#" + config.getConfigId();
		dsProps.put(DBConstants.CustomDataSource.DATASOURCE_ID, UUID.nameUUIDFromBytes(
				dsInfo.getBytes(Charset.forName(DBConstants.DEFAULT_CHAR_SET_TYPE))).toString());
		if (log.isDebugEnabled()) {
			log.debug("Custom Inline Data Source; ID: " + dsInfo + 
					" UUID:" + dsProps.get(DBConstants.CustomDataSource.DATASOURCE_ID));
		}
	}
	
	/**
	 * Convert the input parameter values to its types object values.
	 * @param params The input params
	 * @return The typed object values
	 * @throws DataServiceFault
	 */
	public static Object[] convertInputParamValues(List<InternalParam> params) 
	            throws DataServiceFault {
		Object[] result = new Object[params.size()];
		InternalParam param;
		for (int i = 0; i < result.length; i++) {
			param = params.get(i);
			result[i] = convertInputParamValue(param.getValue().getValueAsString(), 
					param.getSqlType());
		}
		return result;
	}
	
	/**
	 * Convert the string input param value to its typed object value.
	 * @param value The string value of the input param
	 * @param type The type of the input value, defined at DBConstants.DataTypes.
	 * @return The typed object value of the input param 
	 */
	public static Object convertInputParamValue(String value, String type) throws DataServiceFault {
		try {
			if (DBConstants.DataTypes.INTEGER.equals(type)) {
				return Integer.parseInt(value);
			} else if (DBConstants.DataTypes.LONG.equals(type)) {
				return Long.parseLong(value);
			} else if (DBConstants.DataTypes.FLOAT.equals(type)) {
				return Float.parseFloat(value);
			} else if (DBConstants.DataTypes.DOUBLE.equals(type)) {
				return Double.parseDouble(value);
			} else if (DBConstants.DataTypes.BOOLEAN.equals(type)) {
				return Boolean.parseBoolean(value);
			} else if (DBConstants.DataTypes.DATE.equals(type)) {
				return new java.util.Date(DBUtils.getDate(value).getTime());
			} else if (DBConstants.DataTypes.TIME.equals(type)) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(DBUtils.getTime(value).getTime());
				return cal;
			} else if (DBConstants.DataTypes.TIMESTAMP.equals(type)) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(DBUtils.getTimestamp(value).getTime());
				return cal;
			} else {
				return value;
			}
		} catch (Exception e) {
			throw new DataServiceFault(e);
		}
	}
	
    public static String getTenantDomainFromId(int tid) {
    	try {
			return DataServicesDSComponent.getRealmService().getTenantManager()
					.getTenant(tid).getDomain();
		} catch (UserStoreException e) {
			throw new RuntimeException(e);
		}
    }

}