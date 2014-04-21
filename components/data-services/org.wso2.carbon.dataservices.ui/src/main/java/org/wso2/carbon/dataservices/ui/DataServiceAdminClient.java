/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.dataservices.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.ui.stub.DataServiceAdminStub;
import org.wso2.carbon.dataservices.ui.stub.admin.core.xsd.PaginatedTableInfo;

import java.rmi.RemoteException;


public class DataServiceAdminClient {

    private DataServiceAdminStub stub = null;

    private static Log log = LogFactory.getLog(DataServiceAdminClient.class);

    public DataServiceAdminClient(String cookie, String url, ConfigurationContext configContext)
            throws AxisFault {
        String serviceEndPoint = null;
        try {
            serviceEndPoint = url + "DataServiceAdmin";
            stub = new DataServiceAdminStub(configContext, serviceEndPoint);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            log.error("Error occurred while connecting via stub to : " + serviceEndPoint, e);
            throw e;
        }
    }
    
    public String validateJSONMapping(String jsonMapping) throws RemoteException {
        return stub.validateJSONMapping(jsonMapping);
    }
    
	public String getDataServiceContents(String serviceId) throws RemoteException {
        return stub.getDataServiceContentAsString(serviceId);
	}
    
    public String[] getCarbonDataSourceNames() throws RemoteException {
        return stub.getCarbonDataSourceNames();
	}
    
    public String getCarbonDataSourceType(String dsName) throws RemoteException{
    	return stub.getCarbonDataSourceType(dsName);
    }
    
    public String[] getCarbonDataSourceNamesForTypes(String[] types) throws RemoteException{
    	return stub.getCarbonDataSourceNamesForTypes(types);
    }
    
    public void saveDataService(String serviceName, String serviceGroup, String serviceContents)
            throws AxisFault {
    	try {
			stub.saveDataService(serviceName, serviceGroup, serviceContents);
		} catch (RemoteException e) {
			log.error("Error occurred while saving dataservice : " + serviceName, e);
			throw new AxisFault("Saving "+ serviceName+ " failed.", e);
		}
    }
    
    /**
     * 
     * @param driverClass JDBC driver class name
     * @param jdbcURL JDBC Url
     * @param username username
     * @param password password
     * @param protectedTokens password tokens
     * @param passwordProvider password provider
     * @throws AxisFault axisFault
     * @return a string representing success or the failure of the JDBC connection
     */
    public String testJDBCConnection(String driverClass, String jdbcURL, String username,
            String password, String passwordAlias) throws AxisFault {
    	String response = "";
    	try {
			response = stub.testJDBCConnection(driverClass, jdbcURL, username, password,
					passwordAlias);
		} catch (RemoteException e) {
			throw new AxisFault("Error connecting to " + jdbcURL +
                    ". Message from the service is : ", e);
		}
		return response;
    }
 
	public String testGSpreadConnection(String userName,
            String password,
            String visibility,
            String documentURL,String passwordAlias)throws AxisFault {
    	String response = "";
    	try {
			response = stub.testGSpreadConnection(userName, password, visibility, documentURL,
					passwordAlias);
		} catch (RemoteException e) {
			throw new AxisFault("Error connecting to " + documentURL +
                    ". Message from the service is : ", e);
		}
		return response;
    }

	public String[] getOutputColumnNames(String query) throws Exception {
		return stub.getOutputColumnNames(query);
	}

    public String[] getInputMappingNames(String query) throws Exception {
		return stub.getInputMappingNames(query);
	}

    public PaginatedTableInfo getPaginatedTableInfo(int pageNumber,
                                                    String datasourceId, String dbName,
                                                    String[] schemas) throws Exception {
       return  stub.getPaginatedTableInfo(pageNumber,datasourceId, dbName,schemas);
    }

    public PaginatedTableInfo getPaginatedSchemaInfo(int pageNumber,
                                                     String datasourceId) throws Exception {
        return stub.getPaginatedSchemaInfo(pageNumber,datasourceId);
    }

    public String[]  getTableInfo(String datasourceId, String dbName,
                                  String[] schemas) throws Exception {
        return stub.getTableList(datasourceId, dbName, schemas);
    }

    public String[]  getdbSchemaList(String datasourceId) throws Exception {
        return stub.getdbSchemaList(datasourceId);
    }
    
    /*
    add paginator here
     */                                                

    public String[] getDSServiceList(String dataSourceId, String dbName, String[] schemas,
                                          String[] tableNames,String serviceNamespace) throws Exception{
        try {
            return stub.getDSServiceList(dataSourceId, dbName, schemas, tableNames, false,
                    serviceNamespace);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public String getDSService(String dataSourceId, String dbName, String[] schemas,
                               String[] tableNames, String serviceName, String serviceNamespace) throws Exception{
        try {
            return stub.getDSService(dataSourceId, dbName, schemas, tableNames, true, serviceName,
                    serviceNamespace);
        } catch (Exception e) {
                throw new Exception(e.getMessage());
        }
    }

}
