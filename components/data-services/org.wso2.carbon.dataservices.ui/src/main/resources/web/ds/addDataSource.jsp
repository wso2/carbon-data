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

<%@page import="java.util.Properties"%>
<%@page import="org.wso2.carbon.dataservices.common.DBConstants.CustomDataSource"%>
<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.dataservices.common.DBConstants" %>
<%@ page import="org.wso2.carbon.dataservices.common.DBConstants.RDBMS" %>
<%@ page import="org.wso2.carbon.dataservices.common.RDBMSUtils" %>
<%@ page import="org.wso2.carbon.dataservices.common.conf.DynamicAuthConfiguration" %>
<%@ page import="org.wso2.carbon.dataservices.ui.DataServiceAdminClient" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Config" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Property" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.bind.JAXBContext" %>
<%@ page import="javax.xml.bind.Marshaller" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="javax.xml.bind.JAXBException" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.util.regex.Matcher" %>

<fmt:bundle basename="org.wso2.carbon.dataservices.ui.i18n.Resources">
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>

<carbon:breadcrumb
        label="Add Datasource"
        resourceBundle="org.wso2.carbon.dataservices.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"></jsp:useBean>
<jsp:useBean id="newConfig" class="org.wso2.carbon.dataservices.ui.beans.Config" scope="session"></jsp:useBean>
<jsp:useBean id="backupConfigProps" class="java.util.ArrayList" scope="session"></jsp:useBean>
<script type="text/javascript" src="js/ui-validations.js"></script>
<script type="text/javascript">

var propertyCount_ = 0;
var staticUserMappingsCount = 0;
function setValueConf() {
	if (document.getElementById('datasourceType').value == 'EXCEL') {
       var elementId ='excel_datasource';
    } else if(document.getElementById('datasourceType').value == 'RDF') {
    	var elementId ='rdf_datasource';
    } else if(document.getElementById('datasourceType').value == 'CSV') {
    	var elementId ='csv_datasource';
    } else if(document.getElementById('datasourceType').value == 'WEB_CONFIG') {
        var elementId ='web_harvest_config';
    }
    $(elementId).value = $(elementId).value.replace("/_system/config", "conf:");
}
function setValueGov() {
	if (document.getElementById('datasourceType').value == 'EXCEL') {
	       var elementId ='excel_datasource';
    } else if(document.getElementById('datasourceType').value == 'RDF') {
	    	var elementId ='rdf_datasource';
	} else if(document.getElementById('datasourceType').value == 'SPARQL') {
	    	var elementId ='sparql_datasource';
	} else if(document.getElementById('datasourceType').value == 'CSV') {
	    	var elementId ='csv_datasource';
	} else if(document.getElementById('datasourceType').value == 'WEB_CONFIG') {
        var elementId ='web_harvest_config';
    }
	$(elementId).value = $(elementId).value.replace("/_system/governance", "gov:");
}

function getUseSecretAliasValue(chkbox, id) {
	if (chkbox.checked) {
		document.getElementById('useSecretAliasValue').value = 'true';
		document.getElementById('pwdalias').style.display = '';
		document.getElementById(id).style.display = 'none';
		if (document.getElementById(id).value != null) {
			document.getElementById('pwdalias').value = document.getElementById(id).value;
		}
	} else {
		document.getElementById('useSecretAliasValue').value = 'false';
		document.getElementById('pwdalias').style.display = 'none';
		document.getElementById(id).style.display = '';
		document.getElementById(id).value = '';
	}
}
function getUseSecretAliasValueForProperty(chkbox, id) {
	if (chkbox.checked) {
		document.getElementById(id).value = 'true';
	} else {
		document.getElementById(id).value = 'false';
	}
}

</script>

<%!
private boolean isFieldMandatory(String propertName) {
	if (propertName.equals(DBConstants.RDBMS.DRIVER_CLASSNAME)) {
		return true;
	} else if (propertName.equals(DBConstants.RDBMS.URL)) {
		return true;
	}
    else if (propertName.equals(DBConstants.RDBMS.DATASOURCE_CLASSNAME)) {
       return true;
    }
    else if (propertName.equals(DBConstants.RDBMS.DATASOURCE_PROPS)) {
       return true;
    }
    else if (propertName.equals(DBConstants.GSpread.HAS_HEADER)) {
		return true;
	} else if (propertName.equals(DBConstants.GSpread.VISIBILITY)) {
		return true;
	} else if (propertName.equals(DBConstants.GSpread.WORKSHEET_NUMBER)) {
		return true;
	} else if (propertName.equals(DBConstants.Excel.DATASOURCE)) {
		return true;
	} else if (propertName.equals(DBConstants.Excel.WORKBOOK_NAME)) {
		return true;
	} else if (propertName.equals(DBConstants.Excel.DATASOURCE)) {
		return true;
	} else if (propertName.equals(DBConstants.Excel.HAS_HEADER)) {
		return true;
	} else if (propertName.equals(DBConstants.CSV.DATASOURCE)) {
		return true;
	} else if (propertName.equals(DBConstants.CSV.COLUMN_SEPERATOR)) {
		return true;
	} else if (propertName.equals(DBConstants.CSV.HAS_HEADER)) {
		return true;
	} else if (propertName.equals(DBConstants.JNDI.DATASOURCE)) {
		return true;
	} else if (propertName.equals(DBConstants.JNDI.RESOURCE_NAME)) {
		return true;
	} else if (propertName.equals(DBConstants.MongoDB.SERVERS)) {
      	return true;
    } else if (propertName.equals(DBConstants.MongoDB.DATABASE)) {
      	return true;
    } else if (propertName.equals(DBConstants.WebDatasource.WEB_CONFIG)) {
		return true;
	}  else if (propertName.equals(DBConstants.WebDatasource.QUERY_VARIABLE)) {
		return true;
	}  else if (propertName.equals(DBConstants.RDF.DATASOURCE)) {
		return true;
	} else if (propertName.equals(DBConstants.SPARQL.DATASOURCE)) {
		return true;
	} else if (propertName.equals(DBConstants.Cassandra.CASSANDRA_SERVERS)) {
        return true;
    } else {
		return false;
	}
}

Boolean isODataBool = false;
private Config addNotAvailableFunctions(Config config,String selectedType, HttpServletRequest request) {
    String xaVal = request.getParameter ("xaVal");
	if (DBConstants.DataSourceTypes.RDBMS.equals(selectedType)) {
		 if (config.getPropertyValue(DBConstants.RDBMS.DRIVER_CLASSNAME) == null) {
			 config.addProperty(DBConstants.RDBMS.DRIVER_CLASSNAME, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.URL) == null) {
			 config.addProperty(DBConstants.RDBMS.URL, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.USERNAME) == null) {
			 config.addProperty(DBConstants.RDBMS.USERNAME, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.PASSWORD) == null) {
			 config.addProperty(DBConstants.RDBMS.PASSWORD, "");
		 }
	 	 if (config.isExposeAsODataService() == true) {
			 isODataBool = true;
		 } else {
		 	isODataBool = false;
		 }
            if (config.getPropertyValue(DBConstants.RDBMS.DATASOURCE_CLASSNAME) == null) {
                config.addProperty(DBConstants.RDBMS.DATASOURCE_CLASSNAME, "");
            }

            ArrayList<Property> property = new ArrayList<Property>();
            //property.add(new Property("URL", ""));
            //property.add(new Property("User", ""));
            //property.add(new Property("Password", ""));

            if (config.getPropertyValue(DBConstants.RDBMS.DATASOURCE_PROPS) == null) {
                config.addProperty(DBConstants.RDBMS.DATASOURCE_PROPS, property);
            }

		 if (config.getPropertyValue(DBConstants.RDBMS.DEFAULT_TX_ISOLATION) == null) {
			 config.addProperty(DBConstants.RDBMS.DEFAULT_TX_ISOLATION, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.INITIAL_SIZE) == null) {
			 config.addProperty(DBConstants.RDBMS.INITIAL_SIZE, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.MAX_ACTIVE) == null) {
			 config.addProperty(DBConstants.RDBMS.MAX_ACTIVE, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.MIN_IDLE) == null) {
			 config.addProperty(DBConstants.RDBMS.MIN_IDLE, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.MAX_IDLE) == null) {
			 config.addProperty(DBConstants.RDBMS.MAX_IDLE, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.MAX_WAIT) == null) {
			 config.addProperty(DBConstants.RDBMS.MAX_WAIT, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.VALIDATION_QUERY) == null) {
			 config.addProperty(DBConstants.RDBMS.VALIDATION_QUERY, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.TEST_ON_RETURN) == null) {
			 config.addProperty(DBConstants.RDBMS.TEST_ON_RETURN, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.TEST_ON_BORROW) == null) {
			 config.addProperty(DBConstants.RDBMS.TEST_ON_BORROW, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.TEST_WHILE_IDLE) == null) {
			 config.addProperty(DBConstants.RDBMS.TEST_WHILE_IDLE, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.TIME_BETWEEN_EVICTION_RUNS_MILLIS) == null) {
			 config.addProperty(DBConstants.RDBMS.TIME_BETWEEN_EVICTION_RUNS_MILLIS, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.NUM_TESTS_PER_EVICTION_RUN) == null) {
			 config.addProperty(DBConstants.RDBMS.NUM_TESTS_PER_EVICTION_RUN, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.MIN_EVICTABLE_IDLE_TIME_MILLIS) == null) {
			 config.addProperty(DBConstants.RDBMS.MIN_EVICTABLE_IDLE_TIME_MILLIS, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.REMOVE_ABANDONED) == null) {
			 config.addProperty(DBConstants.RDBMS.REMOVE_ABANDONED, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.REMOVE_ABANDONED_TIMEOUT) == null) {
			 config.addProperty(DBConstants.RDBMS.REMOVE_ABANDONED_TIMEOUT, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.LOG_ABANDONED) == null) {
			 config.addProperty(DBConstants.RDBMS.LOG_ABANDONED, "");
		 }
         if (config.getPropertyValue(DBConstants.RDBMS.AUTO_COMMIT) == null) {
			 config.addProperty(DBConstants.RDBMS.AUTO_COMMIT, "");
		 }
         if (config.getPropertyValue(DBConstants.RDBMS.DEFAULT_READONLY) == null) {
			 config.addProperty(DBConstants.RDBMS.DEFAULT_READONLY, "");
		 }
         if (config.getPropertyValue(DBConstants.RDBMS.DEFAULT_CATALOG) == null) {
			 config.addProperty(DBConstants.RDBMS.DEFAULT_CATALOG, "");
		 }
         if (config.getPropertyValue(DBConstants.RDBMS.VALIDATOR_CLASSNAME) == null) {
			 config.addProperty(DBConstants.RDBMS.VALIDATOR_CLASSNAME, "");
		 }
         if (config.getPropertyValue(DBConstants.RDBMS.CONNECTION_PROPERTIES) == null) {
			 config.addProperty(DBConstants.RDBMS.CONNECTION_PROPERTIES, "");
		 }
         if (config.getPropertyValue(DBConstants.RDBMS.INIT_SQL) == null) {
			 config.addProperty(DBConstants.RDBMS.INIT_SQL, "");
		 }
         if (config.getPropertyValue(DBConstants.RDBMS.JDBC_INTERCEPTORS) == null) {
			 config.addProperty(DBConstants.RDBMS.JDBC_INTERCEPTORS, "");
		 }
         if (config.getPropertyValue(DBConstants.RDBMS.VALIDATION_INTERVAL) == null) {
			 config.addProperty(DBConstants.RDBMS.VALIDATION_INTERVAL, "");
		 }
         if (config.getPropertyValue(DBConstants.RDBMS.JMX_ENABLED) == null) {
			 config.addProperty(DBConstants.RDBMS.JMX_ENABLED, "");
		 }
         if (config.getPropertyValue(DBConstants.RDBMS.FAIR_QUEUE) == null) {
			 config.addProperty(DBConstants.RDBMS.FAIR_QUEUE, "");
		 }
         if (config.getPropertyValue(DBConstants.RDBMS.ABANDON_WHEN_PERCENTAGE_FULL) == null) {
			 config.addProperty(DBConstants.RDBMS.ABANDON_WHEN_PERCENTAGE_FULL, "");
		 }
         if (config.getPropertyValue(DBConstants.RDBMS.MAX_AGE) == null) {
			 config.addProperty(DBConstants.RDBMS.MAX_AGE, "");
		 }
         if (config.getPropertyValue(DBConstants.RDBMS.USE_EQUALS) == null) {
			 config.addProperty(DBConstants.RDBMS.USE_EQUALS, "");
		 }
         if (config.getPropertyValue(DBConstants.RDBMS.SUSPECT_TIMEOUT) == null) {
			 config.addProperty(DBConstants.RDBMS.SUSPECT_TIMEOUT, "");
		 }
		 if (config.getPropertyValue(DBConstants.RDBMS.VALIDATION_QUERY_TIMEOUT) == null) {
             config.addProperty(DBConstants.RDBMS.VALIDATION_QUERY_TIMEOUT, "");
         }
         if (config.getPropertyValue(DBConstants.RDBMS.ALTERNATE_USERNAME_ALLOWED) == null) {
			 config.addProperty(DBConstants.RDBMS.ALTERNATE_USERNAME_ALLOWED, "");
		 }
         if (config.getPropertyValue(DBConstants.RDBMS.DYNAMIC_USER_AUTH_CLASS) == null) {
			 config.addProperty(DBConstants.RDBMS.DYNAMIC_USER_AUTH_CLASS, "");
		 }
        DynamicAuthConfiguration dynamicUserConfig = new DynamicAuthConfiguration();
        if (config.getPropertyValue(DBConstants.RDBMS.DYNAMIC_USER_AUTH_MAPPING) == null) {
            config.addProperty(DBConstants.RDBMS.DYNAMIC_USER_AUTH_MAPPING, dynamicUserConfig);
        }
    } else if (DBConstants.DataSourceTypes.EXCEL.equals(selectedType)) {
    	if (config.getPropertyValue(DBConstants.RDBMS.DRIVER_CLASSNAME) == null) {
			 config.addProperty(DBConstants.RDBMS.DRIVER_CLASSNAME, "");
		 }
   	 if (config.getPropertyValue(DBConstants.RDBMS.URL) == null) {
			 config.addProperty(DBConstants.RDBMS.URL, "");
		 } 
    	if (config.getPropertyValue(DBConstants.Excel.DATASOURCE) == null) {
			 config.addProperty(DBConstants.Excel.DATASOURCE, "");
		 }
    } else if (DBConstants.DataSourceTypes.RDF.equals(selectedType)) {
    	 if (config.getPropertyValue(DBConstants.RDF.DATASOURCE) == null) {
			 config.addProperty(DBConstants.RDF.DATASOURCE, "");
		 }
    } else if (DBConstants.DataSourceTypes.SPARQL.equals(selectedType)) {
    	 if (config.getPropertyValue(DBConstants.SPARQL.DATASOURCE) == null) {
			 config.addProperty(DBConstants.SPARQL.DATASOURCE, "");
		 }
    } else if (DBConstants.DataSourceTypes.CSV.equals(selectedType)) {
    	 if (config.getPropertyValue(DBConstants.CSV.DATASOURCE) == null) {
			 config.addProperty(DBConstants.CSV.DATASOURCE, "");
		 }
		 if (config.getPropertyValue(DBConstants.CSV.COLUMN_SEPARATOR) == null) {
			 config.addProperty(DBConstants.CSV.COLUMN_SEPARATOR, "");
		 }
		 if (config.getPropertyValue(DBConstants.CSV.STARTING_ROW) == null) {
			 config.addProperty(DBConstants.CSV.STARTING_ROW, "");
		 }
		 if (config.getPropertyValue(DBConstants.CSV.MAX_ROW_COUNT) == null) {
			 config.addProperty(DBConstants.CSV.MAX_ROW_COUNT, "");
		 }
		 if (config.getPropertyValue(DBConstants.CSV.HAS_HEADER) == null) {
			 config.addProperty(DBConstants.CSV.HAS_HEADER, "");
		 }
		 if (config.getPropertyValue(DBConstants.CSV.HEADER_ROW) == null) {
             config.addProperty(DBConstants.CSV.HEADER_ROW, "");
         }
    } else if (DBConstants.DataSourceTypes.JNDI.equals(selectedType)) {
    	if (config.getPropertyValue(DBConstants.JNDI.INITIAL_CONTEXT_FACTORY) == null) {
			 config.addProperty(DBConstants.JNDI.INITIAL_CONTEXT_FACTORY, "");
		 }
		 if (config.getPropertyValue(DBConstants.JNDI.PROVIDER_URL) == null) {
			 config.addProperty(DBConstants.JNDI.PROVIDER_URL, "");
		 }
		 if (config.getPropertyValue(DBConstants.JNDI.RESOURCE_NAME) == null) {
			 config.addProperty(DBConstants.JNDI.RESOURCE_NAME, "");
		 }
		 if (config.getPropertyValue(DBConstants.JNDI.PASSWORD) == null) {
			 config.addProperty(DBConstants.JNDI.PASSWORD, "");
		 }
    } else if (DBConstants.DataSourceTypes.GDATA_SPREADSHEET.equals(selectedType)) {
    	if (config.getPropertyValue(DBConstants.RDBMS.DRIVER_CLASSNAME) == null) {
			 config.addProperty(DBConstants.RDBMS.DRIVER_CLASSNAME, "");
		}
		if (config.getPropertyValue(DBConstants.RDBMS.URL) == null) {
			 config.addProperty(DBConstants.RDBMS.URL, "");
		}
		if (config.getPropertyValue(DBConstants.RDBMS.USERNAME) == null) {
			 config.addProperty(DBConstants.RDBMS.USERNAME, "");
		}
		if (config.getPropertyValue(DBConstants.RDBMS.PASSWORD) == null) {
			 config.addProperty(DBConstants.RDBMS.PASSWORD, "");
		}
    	if (config.getPropertyValue(DBConstants.GSpread.DATASOURCE) == null) {
			 config.addProperty(DBConstants.GSpread.DATASOURCE, "");
		}
		if (config.getPropertyValue(DBConstants.GSpread.VISIBILITY) == null) {
			 config.addProperty(DBConstants.GSpread.VISIBILITY, "");
		}
		if (config.getPropertyValue(DBConstants.GSpread.USERNAME) == null) {
			 config.addProperty(DBConstants.GSpread.USERNAME, "");
		}
		if (config.getPropertyValue(DBConstants.GSpread.PASSWORD) == null) {
			 config.addProperty(DBConstants.GSpread.PASSWORD, "");
		}
	} else if (DBConstants.DataSourceTypes.CARBON.equals(selectedType)) {
    	if (config.getPropertyValue(DBConstants.CarbonDatasource.NAME) == null) {
			 config.addProperty(DBConstants.CarbonDatasource.NAME, "");
		 }
    } else if (DBConstants.DataSourceTypes.WEB.equals(selectedType)) {
    	if (config.getPropertyValue(DBConstants.WebDatasource.WEB_CONFIG) == null) {
			 config.addProperty(DBConstants.WebDatasource.WEB_CONFIG, "");
		 }
    } else if (DBConstants.DataSourceTypes.CASSANDRA.equals(selectedType)) {
        if (config.getPropertyValue(DBConstants.Cassandra.CASSANDRA_SERVERS) == null) {
            config.addProperty(DBConstants.Cassandra.CASSANDRA_SERVERS, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.KEYSPACE) == null) {
            config.addProperty(DBConstants.Cassandra.KEYSPACE, "");
        }
		if (config.isExposeAsODataService() == true) {
			isODataBool = true;
		} else {
			isODataBool = false;
		}
        if (config.getPropertyValue(DBConstants.Cassandra.PORT) == null) {
            config.addProperty(DBConstants.Cassandra.PORT, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.CLUSTER_NAME) == null) {
            config.addProperty(DBConstants.Cassandra.CLUSTER_NAME, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.COMPRESSION) == null) {
            config.addProperty(DBConstants.Cassandra.COMPRESSION, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.USERNAME) == null) {
            config.addProperty(DBConstants.Cassandra.USERNAME, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.PASSWORD) == null) {
            config.addProperty(DBConstants.Cassandra.PASSWORD, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.LOAD_BALANCING_POLICY) == null) {
            config.addProperty(DBConstants.Cassandra.LOAD_BALANCING_POLICY, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.ENABLE_JMX_REPORTING) == null) {
            config.addProperty(DBConstants.Cassandra.ENABLE_JMX_REPORTING, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.ENABLE_METRICS) == null) {
            config.addProperty(DBConstants.Cassandra.ENABLE_METRICS, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.LOCAL_CORE_CONNECTIONS_PER_HOST) == null) {
            config.addProperty(DBConstants.Cassandra.LOCAL_CORE_CONNECTIONS_PER_HOST, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.REMOTE_CORE_CONNECTIONS_PER_HOST) == null) {
            config.addProperty(DBConstants.Cassandra.REMOTE_CORE_CONNECTIONS_PER_HOST, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.LOCAL_MAX_CONNECTIONS_PER_HOST) == null) {
            config.addProperty(DBConstants.Cassandra.LOCAL_MAX_CONNECTIONS_PER_HOST, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.REMOTE_MAX_CONNECTIONS_PER_HOST) == null) {
            config.addProperty(DBConstants.Cassandra.REMOTE_MAX_CONNECTIONS_PER_HOST, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.LOCAL_MAX_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST) == null) {
            config.addProperty(DBConstants.Cassandra.LOCAL_MAX_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.REMOTE_MAX_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST) == null) {
            config.addProperty(DBConstants.Cassandra.REMOTE_MAX_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.LOCAL_MIN_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST) == null) {
            config.addProperty(DBConstants.Cassandra.LOCAL_MIN_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.REMOTE_MIN_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST) == null) {
            config.addProperty(DBConstants.Cassandra.REMOTE_MIN_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.PROTOCOL_VERSION) == null) {
            config.addProperty(DBConstants.Cassandra.PROTOCOL_VERSION, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.CONSISTENCY_LEVEL) == null) {
            config.addProperty(DBConstants.Cassandra.CONSISTENCY_LEVEL, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.FETCH_SIZE) == null) {
            config.addProperty(DBConstants.Cassandra.FETCH_SIZE, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.SERIAL_CONSISTENCY_LEVEL) == null) {
            config.addProperty(DBConstants.Cassandra.SERIAL_CONSISTENCY_LEVEL, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.RECONNECTION_POLICY) == null) {
            config.addProperty(DBConstants.Cassandra.RECONNECTION_POLICY, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.CONSTANT_RECONNECTION_POLICY_DELAY) == null) {
            config.addProperty(DBConstants.Cassandra.CONSTANT_RECONNECTION_POLICY_DELAY, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.EXPONENTIAL_RECONNECTION_POLICY_BASE_DELAY) == null) {
            config.addProperty(DBConstants.Cassandra.EXPONENTIAL_RECONNECTION_POLICY_BASE_DELAY, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.EXPONENTIAL_RECONNECTION_POLICY_MAX_DELAY) == null) {
            config.addProperty(DBConstants.Cassandra.EXPONENTIAL_RECONNECTION_POLICY_MAX_DELAY, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.RETRY_POLICY) == null) {
            config.addProperty(DBConstants.Cassandra.RETRY_POLICY, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.CONNECTION_TIMEOUT_MILLIS) == null) {
            config.addProperty(DBConstants.Cassandra.CONNECTION_TIMEOUT_MILLIS, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.KEEP_ALIVE) == null) {
            config.addProperty(DBConstants.Cassandra.KEEP_ALIVE, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.READ_TIMEOUT_MILLIS) == null) {
            config.addProperty(DBConstants.Cassandra.READ_TIMEOUT_MILLIS, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.RECEIVER_BUFFER_SIZE) == null) {
            config.addProperty(DBConstants.Cassandra.RECEIVER_BUFFER_SIZE, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.SEND_BUFFER_SIZE) == null) {
            config.addProperty(DBConstants.Cassandra.SEND_BUFFER_SIZE, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.REUSE_ADDRESS) == null) {
            config.addProperty(DBConstants.Cassandra.REUSE_ADDRESS, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.SO_LINGER) == null) {
            config.addProperty(DBConstants.Cassandra.SO_LINGER, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.TCP_NODELAY) == null) {
            config.addProperty(DBConstants.Cassandra.TCP_NODELAY, "");
        }
        if (config.getPropertyValue(DBConstants.Cassandra.ENABLE_SSL) == null) {
            config.addProperty(DBConstants.Cassandra.ENABLE_SSL, "");
        }
    } else if (DBConstants.DataSourceTypes.MONGODB.equals(selectedType)) {
        if (config.getPropertyValue(DBConstants.MongoDB.SERVERS) == null) {
            config.addProperty(DBConstants.MongoDB.SERVERS, "");
        }
        if (config.getPropertyValue(DBConstants.MongoDB.DATABASE) == null) {
            config.addProperty(DBConstants.MongoDB.DATABASE, "");
        }
        if (config.getPropertyValue(DBConstants.MongoDB.WRITE_CONCERN) == null) {
            config.addProperty(DBConstants.MongoDB.WRITE_CONCERN, "");
        }
        if (config.getPropertyValue(DBConstants.MongoDB.READ_PREFERENCE) == null) {
            config.addProperty(DBConstants.MongoDB.READ_PREFERENCE, "");
        }
        if (config.getPropertyValue(DBConstants.MongoDB.AUTO_CONNECT_RETRY) == null) {
            config.addProperty(DBConstants.MongoDB.AUTO_CONNECT_RETRY, "");
        }
        if (config.getPropertyValue(DBConstants.MongoDB.CONNECT_TIMEOUT) == null) {
            config.addProperty(DBConstants.MongoDB.CONNECT_TIMEOUT, "");
        }
        if (config.getPropertyValue(DBConstants.MongoDB.MAX_WAIT_TIME) == null) {
            config.addProperty(DBConstants.MongoDB.MAX_WAIT_TIME, "");
        }
        if (config.getPropertyValue(DBConstants.MongoDB.SOCKET_TIMEOUT) == null) {
            config.addProperty(DBConstants.MongoDB.SOCKET_TIMEOUT, "");
        }
        if (config.getPropertyValue(DBConstants.MongoDB.CONNECTIONS_PER_HOST) == null) {
            config.addProperty(DBConstants.MongoDB.CONNECTIONS_PER_HOST, "");
        }
        if (config.getPropertyValue(DBConstants.MongoDB.THREADS_ALLOWED_TO_BLOCK_CONN_MULTIPLIER) == null) {
            config.addProperty(DBConstants.MongoDB.THREADS_ALLOWED_TO_BLOCK_CONN_MULTIPLIER, "");
        }
    } else if (DBConstants.DataSourceTypes.CUSTOM.equals(selectedType)) {
    	if (config.getPropertyValue(DBConstants.CustomDataSource.DATA_SOURCE_QUERY_CLASS) == null) {
    		ArrayList<Property> properties = null;
    		if (config.getPropertyValue(DBConstants.CustomDataSource.DATA_SOURCE_PROPS) instanceof ArrayList) {
    			properties = (ArrayList<Property>)config.getPropertyValue(DBConstants.CustomDataSource.DATA_SOURCE_PROPS);
    		}
    		config.removeProperty(DBConstants.CustomDataSource.DATA_SOURCE_PROPS);
            config.addProperty(DBConstants.CustomDataSource.DATA_SOURCE_QUERY_CLASS, "");
            config.addProperty(DBConstants.CustomDataSource.DATA_SOURCE_PROPS, properties);
        }
        if (config.getPropertyValue(DBConstants.CustomDataSource.DATA_SOURCE_TABULAR_CLASS) == null) {
        	ArrayList<Property> properties = null;
    		if (config.getPropertyValue(DBConstants.CustomDataSource.DATA_SOURCE_PROPS) instanceof ArrayList) {
    			properties = (ArrayList<Property>)config.getPropertyValue(DBConstants.CustomDataSource.DATA_SOURCE_PROPS);
    		}
    		config.removeProperty(DBConstants.CustomDataSource.DATA_SOURCE_PROPS);
            config.addProperty(DBConstants.CustomDataSource.DATA_SOURCE_TABULAR_CLASS, "");
            config.addProperty(DBConstants.CustomDataSource.DATA_SOURCE_PROPS, properties);
        }
    	if (config.getPropertyValue(DBConstants.CustomDataSource.DATA_SOURCE_PROPS) == null) {
    	    ArrayList<Property> property = new ArrayList<Property>();
    	    config.removeProperty(DBConstants.CustomDataSource.DATA_SOURCE_PROPS);
            config.addProperty(DBConstants.CustomDataSource.DATA_SOURCE_PROPS, property);
        }
    }
	return config;
}

/* private Config addRDBMSProps(Config config, String selectedType, HttpServletRequest request) {
	if (DBConstants.DataSourceTypes.RDBMS.equals(selectedType)) {
		
	}
} */

//Change datasource type to Excel or GSpread if RDBMS
//datasource represents query Mode Excel or GSpread source
private String getDataSourceType(String jdbcUrl) {
	Pattern p = Pattern.compile("jdbc:wso2:[a-zA-Z0-9]+");
    Matcher m = p.matcher(jdbcUrl);
    while (m.find()) {
        if (DBConstants.DSSQLDriverPrefixes.EXCEL_PREFIX.equals(m.group())) {
            return DBConstants.DataSourceTypes.EXCEL;
        } else if (DBConstants.DSSQLDriverPrefixes.GSPRED_PREFIX.equals(m.group())) {
            return DBConstants.DataSourceTypes.GDATA_SPREADSHEET;
        } 
    }
    return DBConstants.DataSourceTypes.RDBMS;
}

private String getExcelGspreadUrl(String excelGspreadJDBCUrl, String dsType) {
	if (dsType.equals("GDATA_SPREADSHEET")) {
		String gSpreadPrexixesString = DBConstants.DSSQLDriverPrefixes.GSPRED_PREFIX + ":" + 
				DBConstants.DSSQLDriverPrefixes.FILE_PATH + "=";
		int gSpreadPrexixesLength = gSpreadPrexixesString.length();
		int endIndex = excelGspreadJDBCUrl.indexOf(";");
		return excelGspreadJDBCUrl.substring(gSpreadPrexixesLength, endIndex);
	} else {
		String excelPrexixesString = DBConstants.DSSQLDriverPrefixes.EXCEL_PREFIX + ":" + 
				DBConstants.DSSQLDriverPrefixes.FILE_PATH + "=";
		int excelPrexixesLength = excelPrexixesString.length();
		return excelGspreadJDBCUrl.substring(excelPrexixesLength);
	}
}

private String getVisibility(String gSpreadJDBCUrl) {
	String params[] = gSpreadJDBCUrl.split(";");
	if (params.length >= 1) {
		String subParams[] = params[1].split("=");
		if (subParams.length > 1 && subParams[0].equals("visibility")) {
			return subParams[1];
		} 
		if (params.length > 1)
		subParams = params[2].split("=");
		if (subParams.length > 1 && subParams[0].equals("visibility")) {
			return subParams[1];
		} 
	}
	return "";
	
} 

private String getSheetName(String gSpreadJDBCUrl) {
	String params[] = gSpreadJDBCUrl.split(";");
	if (params.length >= 1) {
		String subParams[] = params[1].split("=");
		if (subParams.length > 1 && subParams[0].equals("sheetName")) {
			return subParams[1];
		} 
		if (params.length > 1)
		subParams = params[2].split("=");
		if (subParams.length > 1 && subParams[0].equals("sheetName")) {
			return subParams[1];
		} 
	}
	return "";
}
%>


<%
	//retrieve values from the data service session
	String protectedTokens = dataService.getProtectedTokens();
	String passwordProvider = dataService.getPasswordProvider();
    //retrieve value from serviceDetails.jsp
    String configId = request.getParameter("configId");
    String selectedType = request.getParameter("selectedType");
    String scraperString = request.getParameter("scraper-config");
    boolean isXAAvailable = false;
    String xaVal = request.getParameter ("xaVal");
    String[] carbonDataSourceNames = null;
	boolean isXAType = false;
    String flag = request.getParameter("flag");
    String ds = request.getParameter("ds");
    String visibility = request.getParameter("visibility");
    String sheetName = "";
    String customDSType = "";
    // Service name with the path
    String detailedServiceName = request.getParameter("detailedServiceName");
    String dynamicUserAuthClass = request.getParameter("dynamicUserAuthClass");
    dynamicUserAuthClass = (dynamicUserAuthClass == null) ? "" : dynamicUserAuthClass;
    if (configId == null
        || (selectedType != null && newConfig.getDataSourceType() != null) && !newConfig.getDataSourceType().equals(selectedType)) {
        /* if a new datasource or,
          /* if the datasource type change, create a new Config session object */
        newConfig = new Config();
        session.setAttribute("newConfig", newConfig);
    }
    boolean readOnly = false;
    if (flag == null) {
        flag = "";
    }
    if (!"edit_changed".equals(flag)) {
    	backupConfigProps.clear();
    } else {
    	flag = "edit";
    }
    if (configId != null && configId.trim().length() > 0) {
        Config dsConfig = dataService.getConfig(configId);
        if (dsConfig == null || (dsConfig !=null && !flag.equals("edit"))) {
            //This is a request for addding new datasource
            //Observe selectedType & populate
            if (selectedType != null && selectedType.trim().length() > 0 && newConfig.getId() == null) {
                newConfig.setId(configId);
                if (DBConstants.DataSourceTypes.RDBMS.equals(selectedType)) {
                    newConfig.addProperty(DBConstants.RDBMS.DRIVER_CLASSNAME, "");
                    newConfig.addProperty(DBConstants.RDBMS.URL, "");
                    newConfig.addProperty(DBConstants.RDBMS.USERNAME, "");
                    newConfig.addProperty(DBConstants.RDBMS.PASSWORD, "");
					newConfig.addProperty(DBConstants.RDBMS.DATASOURCE_CLASSNAME, "");
					newConfig.setExposeAsOData(false);
                        ArrayList<Property> property = new ArrayList<Property>();
                        //property.add(new Property("URL", ""));
                        //property.add(new Property("User", ""));
                        //property.add(new Property("Password", ""));

                        newConfig.addProperty(DBConstants.RDBMS.DATASOURCE_PROPS, property);

                    //pool config properties
            		newConfig.addProperty(DBConstants.RDBMS.DEFAULT_TX_ISOLATION,"");
            		newConfig.addProperty(DBConstants.RDBMS.INITIAL_SIZE,"");
            		newConfig.addProperty(DBConstants.RDBMS.MAX_ACTIVE,"");
            		newConfig.addProperty(DBConstants.RDBMS.MAX_IDLE,"");
            		newConfig.addProperty(DBConstants.RDBMS.MIN_IDLE,"");
            		newConfig.addProperty(DBConstants.RDBMS.MAX_WAIT,"");
            		newConfig.addProperty(DBConstants.RDBMS.VALIDATION_QUERY,"");
            		newConfig.addProperty(DBConstants.RDBMS.TEST_ON_RETURN,"");
            		newConfig.addProperty(DBConstants.RDBMS.TEST_ON_BORROW,"");
            		newConfig.addProperty(DBConstants.RDBMS.TEST_WHILE_IDLE,"");
            		newConfig.addProperty(DBConstants.RDBMS.TIME_BETWEEN_EVICTION_RUNS_MILLIS,"");
            		newConfig.addProperty(DBConstants.RDBMS.NUM_TESTS_PER_EVICTION_RUN,"");
            		newConfig.addProperty(DBConstants.RDBMS.MIN_EVICTABLE_IDLE_TIME_MILLIS,"");
            		newConfig.addProperty(DBConstants.RDBMS.REMOVE_ABANDONED,"");
            		newConfig.addProperty(DBConstants.RDBMS.REMOVE_ABANDONED_TIMEOUT,"");
            		newConfig.addProperty(DBConstants.RDBMS.LOG_ABANDONED,"");
                    newConfig.addProperty(DBConstants.RDBMS.AUTO_COMMIT,"");
                    newConfig.addProperty(DBConstants.RDBMS.DEFAULT_READONLY,"");
                    newConfig.addProperty(DBConstants.RDBMS.DEFAULT_CATALOG,"");
                    newConfig.addProperty(DBConstants.RDBMS.VALIDATOR_CLASSNAME,"");
                    newConfig.addProperty(DBConstants.RDBMS.CONNECTION_PROPERTIES,"");
                    newConfig.addProperty(DBConstants.RDBMS.INIT_SQL,"");
                    newConfig.addProperty(DBConstants.RDBMS.JDBC_INTERCEPTORS,"");
                    newConfig.addProperty(DBConstants.RDBMS.VALIDATION_INTERVAL,"");
                    newConfig.addProperty(DBConstants.RDBMS.JMX_ENABLED,"");
                    newConfig.addProperty(DBConstants.RDBMS.FAIR_QUEUE,"");
                    newConfig.addProperty(DBConstants.RDBMS.ABANDON_WHEN_PERCENTAGE_FULL,"");
                    newConfig.addProperty(DBConstants.RDBMS.MAX_AGE,"");
                    newConfig.addProperty(DBConstants.RDBMS.USE_EQUALS,"");
                    newConfig.addProperty(DBConstants.RDBMS.SUSPECT_TIMEOUT,"");
                    newConfig.addProperty(DBConstants.RDBMS.VALIDATION_QUERY_TIMEOUT,"");
                    newConfig.addProperty(DBConstants.RDBMS.ALTERNATE_USERNAME_ALLOWED,"");
                    newConfig.addProperty(DBConstants.RDBMS.DYNAMIC_USER_AUTH_CLASS,"");

                    DynamicAuthConfiguration dynamicUserConfig = new DynamicAuthConfiguration();
                    newConfig.addProperty(DBConstants.RDBMS.DYNAMIC_USER_AUTH_MAPPING, dynamicUserConfig);

                } else if (DBConstants.DataSourceTypes.EXCEL.equals(selectedType)) {
                	newConfig.addProperty(DBConstants.RDBMS.DRIVER_CLASSNAME, "");
                    newConfig.addProperty(DBConstants.RDBMS.URL, "");
                    newConfig.addProperty(DBConstants.Excel.DATASOURCE, "");
                } else if (DBConstants.DataSourceTypes.RDF.equals(selectedType)) {
                    newConfig.addProperty(DBConstants.RDF.DATASOURCE, "");

                } else if (DBConstants.DataSourceTypes.SPARQL.equals(selectedType)) {
                    newConfig.addProperty(DBConstants.SPARQL.DATASOURCE, "");

                } else if (DBConstants.DataSourceTypes.CSV.equals(selectedType)) {
                    newConfig.addProperty(DBConstants.CSV.DATASOURCE, "");
                    newConfig.addProperty(DBConstants.CSV.COLUMN_SEPARATOR, ",");
                    newConfig.addProperty(DBConstants.CSV.STARTING_ROW, "");
                    newConfig.addProperty(DBConstants.CSV.MAX_ROW_COUNT, "-1");
                    newConfig.addProperty(DBConstants.CSV.HAS_HEADER, "");
                    newConfig.addProperty(DBConstants.CSV.HEADER_ROW, "");
                } else if (DBConstants.DataSourceTypes.JNDI.equals(selectedType)) {
                    newConfig.addProperty(DBConstants.JNDI.INITIAL_CONTEXT_FACTORY, "");
                    newConfig.addProperty(DBConstants.JNDI.PROVIDER_URL, "");
                    newConfig.addProperty(DBConstants.JNDI.RESOURCE_NAME, "");
                    newConfig.addProperty(DBConstants.JNDI.PASSWORD, "");

                } else if (DBConstants.DataSourceTypes.GDATA_SPREADSHEET.equals(selectedType)) {
                	newConfig.addProperty(DBConstants.RDBMS.DRIVER_CLASSNAME, "");
                    newConfig.addProperty(DBConstants.RDBMS.URL, "");
                    newConfig.addProperty(DBConstants.RDBMS.USERNAME, "");
                    newConfig.addProperty(DBConstants.RDBMS.PASSWORD, "");
                	newConfig.addProperty(DBConstants.GSpread.DATASOURCE, "");
	                newConfig.addProperty(DBConstants.GSpread.VISIBILITY, "");
	                newConfig.addProperty(DBConstants.GSpread.USERNAME, "");
	                newConfig.addProperty(DBConstants.GSpread.PASSWORD, "");
                } else if (DBConstants.DataSourceTypes.CARBON.equals(selectedType)) {
                    newConfig.addProperty(DBConstants.CarbonDatasource.NAME, "");

                } else if (DBConstants.DataSourceTypes.WEB.equals(selectedType)) {
                    newConfig.addProperty(DBConstants.WebDatasource.WEB_CONFIG, "");
                } else if (DBConstants.DataSourceTypes.CASSANDRA.equals(selectedType)) {
                    newConfig.addProperty(DBConstants.Cassandra.CASSANDRA_SERVERS,"");
                    newConfig.addProperty(DBConstants.Cassandra.KEYSPACE,"");
                    newConfig.addProperty(DBConstants.Cassandra.PORT,"");
                    newConfig.addProperty(DBConstants.Cassandra.CLUSTER_NAME,"");
                    newConfig.addProperty(DBConstants.Cassandra.COMPRESSION,"");
                    newConfig.addProperty(DBConstants.Cassandra.USERNAME, "");
                    newConfig.addProperty(DBConstants.Cassandra.PASSWORD,"");
                    newConfig.addProperty(DBConstants.Cassandra.LOAD_BALANCING_POLICY,"");
                    newConfig.addProperty(DBConstants.Cassandra.ENABLE_JMX_REPORTING,"");
                    newConfig.addProperty(DBConstants.Cassandra.ENABLE_METRICS, "");
                    newConfig.addProperty(DBConstants.Cassandra.LOCAL_CORE_CONNECTIONS_PER_HOST,"");
                    newConfig.addProperty(DBConstants.Cassandra.REMOTE_CORE_CONNECTIONS_PER_HOST,"");
                    newConfig.addProperty(DBConstants.Cassandra.LOCAL_MAX_CONNECTIONS_PER_HOST,"");
                    newConfig.addProperty(DBConstants.Cassandra.REMOTE_MAX_CONNECTIONS_PER_HOST,"");
                    newConfig.addProperty(DBConstants.Cassandra.LOCAL_MAX_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST,"");
                    newConfig.addProperty(DBConstants.Cassandra.REMOTE_MAX_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST,"");
                    newConfig.addProperty(DBConstants.Cassandra.LOCAL_MIN_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST, "");
                    newConfig.addProperty(DBConstants.Cassandra.REMOTE_MIN_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST, "");
                    newConfig.addProperty(DBConstants.Cassandra.PROTOCOL_VERSION,"");
                    newConfig.addProperty(DBConstants.Cassandra.CONSISTENCY_LEVEL,"");
                    newConfig.addProperty(DBConstants.Cassandra.FETCH_SIZE, "");
                    newConfig.addProperty(DBConstants.Cassandra.SERIAL_CONSISTENCY_LEVEL,"");
                    newConfig.addProperty(DBConstants.Cassandra.RECONNECTION_POLICY, "");
                    newConfig.addProperty(DBConstants.Cassandra.CONSTANT_RECONNECTION_POLICY_DELAY, "");
                    newConfig.addProperty(DBConstants.Cassandra.EXPONENTIAL_RECONNECTION_POLICY_BASE_DELAY, "");
                    newConfig.addProperty(DBConstants.Cassandra.EXPONENTIAL_RECONNECTION_POLICY_MAX_DELAY, "");
                    newConfig.addProperty(DBConstants.Cassandra.RETRY_POLICY,"");
                    newConfig.addProperty(DBConstants.Cassandra.CONNECTION_TIMEOUT_MILLIS,"");
                    newConfig.addProperty(DBConstants.Cassandra.KEEP_ALIVE,"");
                    newConfig.addProperty(DBConstants.Cassandra.READ_TIMEOUT_MILLIS, "");
                    newConfig.addProperty(DBConstants.Cassandra.RECEIVER_BUFFER_SIZE,"");
                    newConfig.addProperty(DBConstants.Cassandra.SEND_BUFFER_SIZE,"");
                    newConfig.addProperty(DBConstants.Cassandra.REUSE_ADDRESS,"");
                    newConfig.addProperty(DBConstants.Cassandra.SO_LINGER, "");
                    newConfig.addProperty(DBConstants.Cassandra.TCP_NODELAY,"");
                    newConfig.addProperty(DBConstants.Cassandra.ENABLE_SSL,"");
                }  else if (DBConstants.DataSourceTypes.CUSTOM.equals(selectedType)) {
                	ArrayList<Property> property = new ArrayList<Property>();
                    newConfig.addProperty(DBConstants.CustomDataSource.DATA_SOURCE_QUERY_CLASS,"");
                    newConfig.addProperty(DBConstants.CustomDataSource.DATA_SOURCE_TABULAR_CLASS,"");
                    newConfig.addProperty(DBConstants.CustomDataSource.DATA_SOURCE_PROPS,property);
                }  else if (DBConstants.DataSourceTypes.MONGODB.equals(selectedType)) {
                    newConfig.addProperty(DBConstants.MongoDB.SERVERS, "");
                    newConfig.addProperty(DBConstants.MongoDB.DATABASE, "");
                    newConfig.addProperty(DBConstants.MongoDB.WRITE_CONCERN, "");
                    newConfig.addProperty(DBConstants.MongoDB.READ_PREFERENCE, "");
                    newConfig.addProperty(DBConstants.MongoDB.AUTO_CONNECT_RETRY, "");
                    newConfig.addProperty(DBConstants.MongoDB.CONNECT_TIMEOUT, "");
                    newConfig.addProperty(DBConstants.MongoDB.MAX_WAIT_TIME, "");
                    newConfig.addProperty(DBConstants.MongoDB.SOCKET_TIMEOUT, "");
                    newConfig.addProperty(DBConstants.MongoDB.CONNECTIONS_PER_HOST, "");
                    newConfig.addProperty(DBConstants.MongoDB.THREADS_ALLOWED_TO_BLOCK_CONN_MULTIPLIER, "");
                }

            }
        } else {
            if (dsConfig.getPropertyValue(RDBMS.DATASOURCE_CLASSNAME) !=null &&
                    dsConfig.getPropertyValue(RDBMS.DATASOURCE_CLASSNAME).toString().trim().length() >0  ) {
               isXAType = true;
            }
            if(dsConfig.getPropertyValue("gspread_visibility") instanceof String) {
                visibility = (String)dsConfig.getPropertyValue("gspread_visibility");
            }
            
            if(dsConfig.getPropertyValue(CustomDataSource.DATA_SOURCE_QUERY_CLASS) != null &&
            		dsConfig.getPropertyValue(CustomDataSource.DATA_SOURCE_QUERY_CLASS).toString().trim().length() > 0) {
                customDSType = DBConstants.DataSourceTypes.CUSTOM_QUERY;
            } else if (dsConfig.getPropertyValue(CustomDataSource.DATA_SOURCE_TABULAR_CLASS) != null &&
            		dsConfig.getPropertyValue(CustomDataSource.DATA_SOURCE_TABULAR_CLASS).toString().trim().length() > 0) {
            	customDSType = DBConstants.DataSourceTypes.CUSTOM_TABULAR;
            }

            readOnly = true;
            if (!flag.equals("edit")) {
                dataService.removeConfig(dsConfig);
                Config conf = new Config();
                conf.setId(configId);
                if (DBConstants.DataSourceTypes.RDBMS.equals(selectedType)) {
                    conf.addProperty(DBConstants.RDBMS.DRIVER_CLASSNAME, "");
                    conf.addProperty(DBConstants.RDBMS.URL, "");
                    conf.addProperty(DBConstants.RDBMS.USERNAME, "");
                    conf.addProperty(DBConstants.RDBMS.PASSWORD, "");
                    conf.addProperty(DBConstants.RDBMS.DATASOURCE_CLASSNAME,"");

                     ArrayList<Property> property = new ArrayList<Property>();
                        //property.add(new Property("URL", ""));
                        //property.add(new Property("User", ""));
                        //property.add(new Property("Password", ""));

                    conf.addProperty(DBConstants.RDBMS.DATASOURCE_PROPS,property);
                    //pool config properties
            		conf.addProperty(DBConstants.RDBMS.DEFAULT_TX_ISOLATION,"");
            		conf.addProperty(DBConstants.RDBMS.INITIAL_SIZE,"");
            		conf.addProperty(DBConstants.RDBMS.MAX_ACTIVE,"");
            		conf.addProperty(DBConstants.RDBMS.MAX_IDLE,"");
            		conf.addProperty(DBConstants.RDBMS.MIN_IDLE,"");
            		conf.addProperty(DBConstants.RDBMS.MAX_WAIT,"");
            		conf.addProperty(DBConstants.RDBMS.VALIDATION_QUERY,"");
            		conf.addProperty(DBConstants.RDBMS.TEST_ON_RETURN,"");
            		conf.addProperty(DBConstants.RDBMS.TEST_ON_BORROW,"");
            		conf.addProperty(DBConstants.RDBMS.TEST_WHILE_IDLE,"");
            		conf.addProperty(DBConstants.RDBMS.TIME_BETWEEN_EVICTION_RUNS_MILLIS,"");
            		conf.addProperty(DBConstants.RDBMS.NUM_TESTS_PER_EVICTION_RUN,"");
            		conf.addProperty(DBConstants.RDBMS.MIN_EVICTABLE_IDLE_TIME_MILLIS,"");
            		conf.addProperty(DBConstants.RDBMS.REMOVE_ABANDONED,"");
            		conf.addProperty(DBConstants.RDBMS.REMOVE_ABANDONED_TIMEOUT,"");
            		conf.addProperty(DBConstants.RDBMS.LOG_ABANDONED,"");
                    conf.addProperty(DBConstants.RDBMS.AUTO_COMMIT,"");
                    conf.addProperty(DBConstants.RDBMS.DEFAULT_READONLY,"");
                    conf.addProperty(DBConstants.RDBMS.DEFAULT_CATALOG,"");
                    conf.addProperty(DBConstants.RDBMS.VALIDATOR_CLASSNAME,"");
                    conf.addProperty(DBConstants.RDBMS.CONNECTION_PROPERTIES,"");
                    conf.addProperty(DBConstants.RDBMS.INIT_SQL,"");
                    conf.addProperty(DBConstants.RDBMS.JDBC_INTERCEPTORS,"");
                    conf.addProperty(DBConstants.RDBMS.VALIDATION_INTERVAL,"");
                    conf.addProperty(DBConstants.RDBMS.JMX_ENABLED,"");
                    conf.addProperty(DBConstants.RDBMS.FAIR_QUEUE,"");
                    conf.addProperty(DBConstants.RDBMS.ABANDON_WHEN_PERCENTAGE_FULL,"");
                    conf.addProperty(DBConstants.RDBMS.MAX_AGE,"");
                    conf.addProperty(DBConstants.RDBMS.USE_EQUALS,"");
                    conf.addProperty(DBConstants.RDBMS.SUSPECT_TIMEOUT,"");
                    conf.addProperty(DBConstants.RDBMS.VALIDATION_QUERY_TIMEOUT,"");
                    conf.addProperty(DBConstants.RDBMS.ALTERNATE_USERNAME_ALLOWED,"");
                    conf.addProperty(DBConstants.RDBMS.DYNAMIC_USER_AUTH_CLASS,"");

                    DynamicAuthConfiguration dynamicUserConfig = new DynamicAuthConfiguration();
                    conf.addProperty(DBConstants.RDBMS.DYNAMIC_USER_AUTH_MAPPING, dynamicUserConfig);

                } else if (DBConstants.DataSourceTypes.EXCEL.equals(selectedType)) {
                    conf.addProperty(DBConstants.Excel.DATASOURCE, "");

                } else if (DBConstants.DataSourceTypes.RDF.equals(selectedType)) {
                    conf.addProperty(DBConstants.RDF.DATASOURCE, "");

                } else if (DBConstants.DataSourceTypes.SPARQL.equals(selectedType)) {
                    conf.addProperty(DBConstants.SPARQL.DATASOURCE, "");

                } else if (DBConstants.DataSourceTypes.CSV.equals(selectedType)) {
                    conf.addProperty(DBConstants.CSV.DATASOURCE, "");
                    conf.addProperty(DBConstants.CSV.COLUMN_SEPARATOR, ",");
                    conf.addProperty(DBConstants.CSV.STARTING_ROW, "");
                    conf.addProperty(DBConstants.CSV.MAX_ROW_COUNT, "-1");
                    conf.addProperty(DBConstants.CSV.HAS_HEADER, "");
                    conf.addProperty(DBConstants.CSV.HEADER_ROW, "");

                } else if (DBConstants.DataSourceTypes.JNDI.equals(selectedType)) {
                    conf.addProperty(DBConstants.JNDI.INITIAL_CONTEXT_FACTORY, "");
                    conf.addProperty(DBConstants.JNDI.PROVIDER_URL, "");
                    conf.addProperty(DBConstants.JNDI.RESOURCE_NAME, "");
                    conf.addProperty(DBConstants.JNDI.PASSWORD, "");

                } else if (DBConstants.DataSourceTypes.GDATA_SPREADSHEET.equals(selectedType)) {
                    conf.addProperty(DBConstants.GSpread.DATASOURCE, "");
                    conf.addProperty(DBConstants.GSpread.VISIBILITY, "");
                    conf.addProperty(DBConstants.GSpread.USERNAME, "");
                    conf.addProperty(DBConstants.GSpread.PASSWORD, "");
                } else if (DBConstants.DataSourceTypes.CARBON.equals(selectedType)) {
                    conf.addProperty(DBConstants.CarbonDatasource.NAME, "");

                } else if (DBConstants.DataSourceTypes.WEB.equals(selectedType)) {
                    conf.addProperty(DBConstants.WebDatasource.WEB_CONFIG, "");
                } else if (DBConstants.DataSourceTypes.CASSANDRA.equals(selectedType)) {
                    conf.addProperty(DBConstants.Cassandra.CASSANDRA_SERVERS,"");
                    conf.addProperty(DBConstants.Cassandra.KEYSPACE,"");
                    conf.addProperty(DBConstants.Cassandra.PORT,"");
                    conf.addProperty(DBConstants.Cassandra.CLUSTER_NAME,"");
                    conf.addProperty(DBConstants.Cassandra.COMPRESSION,"");
                    conf.addProperty(DBConstants.Cassandra.USERNAME, "");
                    conf.addProperty(DBConstants.Cassandra.PASSWORD,"");
                    conf.addProperty(DBConstants.Cassandra.LOAD_BALANCING_POLICY,"");
                    conf.addProperty(DBConstants.Cassandra.ENABLE_JMX_REPORTING,"");
                    conf.addProperty(DBConstants.Cassandra.ENABLE_METRICS, "");
                    conf.addProperty(DBConstants.Cassandra.LOCAL_CORE_CONNECTIONS_PER_HOST,"");
                    conf.addProperty(DBConstants.Cassandra.REMOTE_CORE_CONNECTIONS_PER_HOST,"");
                    conf.addProperty(DBConstants.Cassandra.LOCAL_MAX_CONNECTIONS_PER_HOST,"");
                    conf.addProperty(DBConstants.Cassandra.REMOTE_MAX_CONNECTIONS_PER_HOST,"");
                    conf.addProperty(DBConstants.Cassandra.LOCAL_MAX_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST,"");
                    conf.addProperty(DBConstants.Cassandra.REMOTE_MAX_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST,"");
                    conf.addProperty(DBConstants.Cassandra.LOCAL_MIN_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST, "");
                    conf.addProperty(DBConstants.Cassandra.REMOTE_MIN_SIMULTANEOUS_REQUEST_PER_CONNECTION_THRESHOST, "");
                    conf.addProperty(DBConstants.Cassandra.PROTOCOL_VERSION,"");
                    conf.addProperty(DBConstants.Cassandra.CONSISTENCY_LEVEL,"");
                    conf.addProperty(DBConstants.Cassandra.FETCH_SIZE, "");
                    conf.addProperty(DBConstants.Cassandra.SERIAL_CONSISTENCY_LEVEL,"");
                    conf.addProperty(DBConstants.Cassandra.RECONNECTION_POLICY, "");
                    conf.addProperty(DBConstants.Cassandra.CONSTANT_RECONNECTION_POLICY_DELAY, "");
                    conf.addProperty(DBConstants.Cassandra.EXPONENTIAL_RECONNECTION_POLICY_BASE_DELAY, "");
                    conf.addProperty(DBConstants.Cassandra.EXPONENTIAL_RECONNECTION_POLICY_MAX_DELAY, "");
                    conf.addProperty(DBConstants.Cassandra.RETRY_POLICY,"");
                    conf.addProperty(DBConstants.Cassandra.CONNECTION_TIMEOUT_MILLIS,"");
                    conf.addProperty(DBConstants.Cassandra.KEEP_ALIVE,"");
                    conf.addProperty(DBConstants.Cassandra.READ_TIMEOUT_MILLIS, "");
                    conf.addProperty(DBConstants.Cassandra.RECEIVER_BUFFER_SIZE,"");
                    conf.addProperty(DBConstants.Cassandra.SEND_BUFFER_SIZE,"");
                    conf.addProperty(DBConstants.Cassandra.REUSE_ADDRESS,"");
                    conf.addProperty(DBConstants.Cassandra.SO_LINGER, "");
                    conf.addProperty(DBConstants.Cassandra.TCP_NODELAY,"");
                    conf.addProperty(DBConstants.Cassandra.ENABLE_SSL,"");
                }  else if (DBConstants.DataSourceTypes.MONGODB.equals(selectedType)) {
                    conf.addProperty(DBConstants.MongoDB.SERVERS, "");
                    conf.addProperty(DBConstants.MongoDB.DATABASE, "");
                    conf.addProperty(DBConstants.MongoDB.WRITE_CONCERN, "");
                    conf.addProperty(DBConstants.MongoDB.READ_PREFERENCE, "");
                    conf.addProperty(DBConstants.MongoDB.AUTO_CONNECT_RETRY, "");
                    conf.addProperty(DBConstants.MongoDB.CONNECT_TIMEOUT, "");
                    conf.addProperty(DBConstants.MongoDB.MAX_WAIT_TIME, "");
                    conf.addProperty(DBConstants.MongoDB.SOCKET_TIMEOUT, "");
                    conf.addProperty(DBConstants.MongoDB.CONNECTIONS_PER_HOST, "");
                    conf.addProperty(DBConstants.MongoDB.THREADS_ALLOWED_TO_BLOCK_CONN_MULTIPLIER, "");
                }
                dataService.setConfig(conf);
            }
        }
    }

    Iterator propertyIterator = null;
    String dataSourceType = request.getParameter("selectedType");
    dataSourceType = dataSourceType == null ? "" : dataSourceType;
    String rdbmsEngineType = "#";
    String passwordAlias = "";
    boolean useSecretAlias = false;
    boolean useQueryMode = false;
    boolean customConClassAdded = false; 
    try {
        if (configId != null && configId.trim().length() > 0) {
            Config dsConfig = dataService.getConfig(configId);
            
            if (dsConfig == null || (dsConfig !=null && !flag.equals("edit"))) {
                dsConfig = newConfig;
            }
            if (dsConfig != null) {
            	/* only if this is not set by the request parameter, set it */
            	if ("".equals(dataSourceType)) {
                    dataSourceType = dsConfig.getDataSourceType();
            	} else {
            		/* 'backupConfigProps' is used to keep the original config properties, when the user
            		   switches the datasource type when editing, so if the user cancels it, we can restore
            		   the original values using this list */
            		if (backupConfigProps.isEmpty()) {
            		    backupConfigProps.addAll(dsConfig.getProperties());
            		}
            		dsConfig.getProperties().clear();
            	}
                //Check whether datasource is Excel or GSpread in Query Mode and change the dataSourceType
                if ("RDBMS".equals(dataSourceType)) {
                	if (dsConfig.getPropertyValue(DBConstants.RDBMS.URL) instanceof String) {
                        String jdbcUrl = dsConfig.getPropertyValue(DBConstants.RDBMS.URL).toString();
                        if ((jdbcUrl != null) && jdbcUrl.trim().length() > 0) {
                        	dataSourceType = getDataSourceType(jdbcUrl);
                        		if (dataSourceType.equals(DBConstants.DataSourceTypes.GDATA_SPREADSHEET) || 
                        			dataSourceType.equals(DBConstants.DataSourceTypes.EXCEL)) {
                        			useQueryMode = true;
                        			if (dataSourceType.equals(DBConstants.DataSourceTypes.GDATA_SPREADSHEET)) {
                        				sheetName = getSheetName((String)dsConfig.getPropertyValue("url"));
                        				visibility = getVisibility((String)dsConfig.getPropertyValue("url"));
                        			}
                        		}
                        } else {
                        	if (dsConfig.getPropertyValue(DBConstants.Excel.DATASOURCE) != null) {
                        		dataSourceType = DBConstants.DataSourceTypes.EXCEL;
                        	} else if (dsConfig.getPropertyValue(DBConstants.GSpread.DATASOURCE) != null) {
                        		dataSourceType = DBConstants.DataSourceTypes.GDATA_SPREADSHEET;
                        	}
                        }
                    }
                }
                if (dataSourceType == null) {
                    dataSourceType = "";
                }
                if (selectedType == null) {
                    selectedType = dataSourceType;
                }
                dsConfig = addNotAvailableFunctions(dsConfig, selectedType,request);
                /* if (useQueryMode) {
                	dsConfig = addRDBMSProps(dsConfig, selectedType,request);
                } */
                ArrayList configProperties = dsConfig.getProperties();
                propertyIterator = configProperties.iterator();

                if ("RDBMS".equals(dataSourceType) && !isXAType) {
                    if (dsConfig.getPropertyValue(DBConstants.RDBMS.URL) instanceof String) {
                        String jdbcUrl = dsConfig.getPropertyValue(DBConstants.RDBMS.URL).toString();
                        if ((jdbcUrl != null) && jdbcUrl.trim().length() > 0) {
                            rdbmsEngineType = RDBMSUtils.getRDBMSEngine(jdbcUrl);
                        }
                    }
                }
                else if ("RDBMS".equals(dataSourceType) && isXAType) {
                    if (dsConfig.getPropertyValue(DBConstants.RDBMS.DATASOURCE_CLASSNAME) instanceof String) {
                        String xaDataSourceClass = dsConfig.getPropertyValue(DBConstants.RDBMS.DATASOURCE_CLASSNAME).toString();
                        if ((xaDataSourceClass != null) && xaDataSourceClass.trim().length() > 0) {
                            rdbmsEngineType = RDBMSUtils.getRDBMSEngine4XADataSource(xaDataSourceClass);
                        }
                    }
                }
                if (xaVal != null) {
                    isXAType = Boolean.parseBoolean(xaVal);
                }
                useSecretAlias = dsConfig.isUseSecretAliasForPassword();
            }
        } else {
            configId = "";
        }
        /* if the selectType is carbon datasources, populate the names list */
        if ((selectedType != null && selectedType.equals("CARBON_DATASOURCE")) || dataSourceType.equals("CARBON_DATASOURCE")) {
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            DataServiceAdminClient client = new DataServiceAdminClient(cookie, backendServerURL, configContext);
            carbonDataSourceNames = client.getCarbonDataSourceNames();
            if (carbonDataSourceNames == null) {
                /* no datasources */
                carbonDataSourceNames = new String[0];
            }

        }
    } catch (Exception e) {
				String errorMsg = e.getLocalizedMessage();
%>
<script type="text/javascript">
	location.href = "dsErrorPage.jsp?errorMsg=<%=errorMsg%>";
</script>
<%
    }
%>
<div id="middle">
<h2>
    <%
        if (flag.equals("edit")) {
    %>
    <fmt:message key="edit.data.source"/><%out.write(" (" + configId + ")");%>
    <%} else {%>
    <fmt:message key="add.new.datasource"/>
    <%}%>
</h2>

<div id="workArea">
<form method="post" action="dataSourceProcessor.jsp" name="dataForm">
<table id="mainTable" class="styledLeft noBorders" cellspacing="0" width="100%">
<thead>
  <tr>
    <th colspan="5"><fmt:message key="org.wso2.ws.dataservice.data.source.new"/></th>
  </tr>
</thead>
        <% if(detailedServiceName != null) { %>
            <input type="hidden" id="detailedServiceName" name="detailedServiceName" value="<%=detailedServiceName%>"/>
        <% } %>
        <tr>
            <td class="leftCol-small" style="white-space: nowrap;"><fmt:message
                    key="datasource.id"/><font color="red">*</font></td>
            <td>
                <%if (readOnly) {%>
                <input type="text" id="datasourceId" name="datasourceId" value="<%=configId%>"
                       readonly="readonly"/>
                <%} else {%>
                <input type="text" id="datasourceId" name="datasourceId" value="<%=configId%>"/>
                <%}%>
            </td>
            <input type="hidden" id="protectedTokens" name="protectedTokens"
                   value="<%=protectedTokens%>"/>
            <input type="hidden" id="passwordProvider" name="passwordProvider"
                   value="<%=passwordProvider%>"/>
            <input type="hidden" id="isXAType" name="isXAType" value="<%=isXAType%>"/>
            <input type="hidden" id="flag" name="flag" value="<%=flag%>"/>
            <input type="hidden" id="propertyCount" name="propertyCount" value="0"/>
        </tr>

        <tr>
            <td><label><fmt:message key="dataservices.data.source.type"/><font
                    color="red">*</font></label>
            </td>
            <td>
                <select id="datasourceType" name="datasourceType"
                        onchange="changeDataSourceType(this,document)">
                    <!-- onchange="javascript:location.href = 'addDataSource.jsp?selectedType='+this.options[this.selectedIndex].value+'&configId='+document.getElementById('datasourceId').value+'&flag=edit';return false;"> -->
                    <% if (dataSourceType.equals("")) { %>
                    <option value="" selected="selected">--SELECT--</option>
                    <%
                    } else {%>
                    <option value="">--SELECT--</option>
                    <%}%>

                    <%
                        if (dataSourceType.equals("RDBMS")) {
                    %>
                    <option value="RDBMS" selected="selected">RDBMS</option>
                    <%
                    } else {%>
                    <option value="RDBMS">RDBMS</option>
                    <%}%>

                    <%
                        if (dataSourceType.equals("Cassandra")) {
                    %>
                    <option value="Cassandra" selected="selected">Cassandra</option>
                    %>
                    <%
                    } else {%>
                    <option value="Cassandra">Cassandra</option>
                    <%}%>

                    <%
                        if (dataSourceType.equals("MongoDB")) {
                    %>
                    <option value="MongoDB" selected="selected">MongoDB</option>
                    <%
                    } else {%>
                    <option value="MongoDB">MongoDB</option>
                     <%}%>


                    <%
                        if (dataSourceType.equals("CSV")) {
                    %>
                    <option value="CSV" selected="selected">CSV</option>
                    <%
                    } else {%>
                    <option value="CSV">CSV</option>
                    <%}%>


                    <% if (dataSourceType.equals("EXCEL")) { %>
                    <option value="EXCEL" selected="selected">EXCEL</option>
                    <%
                    } else {%>
                    <option value="EXCEL">EXCEL</option>
                    <%}%>

                    <% if (dataSourceType.equals("RDF")) { %>
                    <option value="RDF" selected="selected">RDF</option>
                    <%
                    } else {%>
                    <option value="RDF">RDF</option>
                    <%}%>

                    <% if (dataSourceType.equals("SPARQL")) { %>
                    <option value="SPARQL" selected="selected">SPARQL Endpoint</option>
                    <%
                    } else {%>
                    <option value="SPARQL">SPARQL Endpoint</option>
                    <%}%>

                    <% if (dataSourceType.equals("JNDI")) { %>
                    <option value="JNDI" selected="selected">JNDI Datasource</option>
                    <%
                    } else {%>
                    <option value="JNDI">JNDI Datasource</option>
                    <%}%>

                    <% if (dataSourceType.equals("GDATA_SPREADSHEET")) { %>
                    <option value="GDATA_SPREADSHEET" selected="selected">Google Spreadsheet
                    </option>
                    <%
                    } else {%>
                    <option value="GDATA_SPREADSHEET">Google Spreadsheet</option>
                    <%}%>

                    <% if (dataSourceType.equals("CARBON_DATASOURCE")) { %>
                    <option value="CARBON_DATASOURCE" selected="selected">Carbon Datasource
                    </option>
                    <%
                    } else {%>
                    <option value="CARBON_DATASOURCE">Carbon Datasource</option>
                    <%}%>

                    <% if (dataSourceType.equals("WEB_CONFIG")) { %>
                    <option value="WEB_CONFIG" selected="selected">Web Datasource</option>
                    <%
                    } else {%>
                    <option value="WEB_CONFIG">Web Datasource</option>
                    <%}%>
                    
                    <% if (dataSourceType.equals("CUSTOM")) { %>
                    <option value="CUSTOM" selected="selected">Custom Datasource</option>
                    <%
                    } else {%>
                    <option value="CUSTOM">Custom Datasource</option>
                    <%}%>
                </select>
                <% if ("RDBMS".equals(dataSourceType)) {
                    isXAAvailable = true;
                }%>

                <% if (isXAAvailable) {%>
                <select id="xaType" name="xaType" onchange="changeXAType(this,document);">

                    <% if (!isXAType) { %>
                    <option value="nXAType" selected="selected"><fmt:message
                            key="rdbms.none.xa.DataSource"/></option>
                    <% } else { %>
                    <option value="nXAType"><fmt:message key="rdbms.none.xa.DataSource"/></option>
                    <% } %>
                    <% if (isXAType) { %>
                    <option value="xaType" selected="selected"><fmt:message
                            key="rdbms.xa.DataSource"/></option>
                    <% } else { %>
                    <option value="xaType"><fmt:message key="rdbms.xa.DataSource"/></option>
                    <% } %>

                </select>

                <% } %>
            </td>
        </tr>
        <% if ("GDATA_SPREADSHEET".equals(dataSourceType) || "EXCEL".equals(dataSourceType)) { %>
	        <tr id="useQueryModeTr">
	        	<td><label><fmt:message key="use.query.mode"/></label>
	        	</td>
	        	<td>
	        		<%if(useQueryMode) { %>
	        			<input type="checkbox" id="useQueryMode" name="useQueryMode" onclick="showGsExcelProperties(this, '<%=dataSourceType%>')" 
	        			checked/>
	        		<%} else { %>
	        			<input type="checkbox" id="useQueryMode" name="useQueryMode" onclick="showGsExcelProperties(this, '<%=dataSourceType%>')"/>
	        		<%} %>
	        		<input type="hidden" id="useQueryModeValue" name="useQueryModeValue" value='<%=useQueryMode %>'/>
	        	</td>
	        </tr>
	    <%} %>
	    <%if(useQueryMode && "GDATA_SPREADSHEET".equals(dataSourceType)) { %>
	    	<tr id="sheetNameTr">
	        	<td><label><fmt:message key="sheetName"/><font
                    color="red">*</font></label>
	        	</td>
	        	<td>
	        		<input type="text" id="sheetName" name="sheetName" value='<%=sheetName %>'/>
	        	</td>
	     </tr>
	    <%} else { %>
		    <tr id="sheetNameTr" style="display:none">
		        	<td><label><fmt:message key="sheetName"/><font
	                    color="red">*</font></label>
		        	</td>
		        	<td>
		        		<input type="text" id="sheetName" name="sheetName"/>
		        	</td>
		     </tr>
	     <%} %>

<div id="complexTypeRowId" style="<%=!(isXAType)  ? "" : "display:none"%>">
<% if ("RDBMS".equals(dataSourceType) && !isXAType) {%>
<tr>
    <td class="leftCol-small" style="white-space: nowrap;"><label><fmt:message key="datasource.database.engine"/><font
            color="red">*</font></label></td>
    <td>
        <select name="databaseEngine" id="databaseEngine"
                onchange="javascript:setJDBCValues(this,document);return false;">

            <%if (("#".equals(rdbmsEngineType)|| rdbmsEngineType.equals(""))) {%>
            <option value="#" selected="selected">--SELECT--</option>
            <%} else {%>
            <option value="#">--SELECT--</option>
            <%}%>

            <%if ("mysql".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="jdbc:mysql://[machine-name/ip]:[port]/[database-name]#com.mysql.jdbc.Driver">
                MySQL
            </option>
            <%} else {%>
            <option value="jdbc:mysql://[machine-name/ip]:[port]/[database-name]#com.mysql.jdbc.Driver">
                MySQL
            </option>
            <%}%>

            <%if ("derby".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="jdbc:derby:[path-to-data-file]#org.apache.derby.jdbc.EmbeddedDriver">
                Apache Derby
            </option>
            <%} else {%>
            <option value="jdbc:derby:[path-to-data-file]#org.apache.derby.jdbc.EmbeddedDriver">
                Apache Derby
            </option>
            <%}%>

            <%if ("mssqlserver".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="jdbc:sqlserver://[HOST]:[PORT1433];databaseName#com.microsoft.sqlserver.jdbc.SQLServerDriver">
                Microsoft SQL Server
            </option>
            <%} else {%>
            <option value="jdbc:sqlserver://[HOST]:[PORT1433];databaseName=[DB]#com.microsoft.sqlserver.jdbc.SQLServerDriver">
                Microsoft SQL Server
            </option>
            <%}%>

            <%if ("oracle".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="jdbc:oracle:[drivertype]:[username/password]@[host]:[port]/[database]#oracle.jdbc.driver.OracleDriver">
                Oracle
            </option>
            <%} else {%>
            <option value="jdbc:oracle:[drivertype]:[username/password]@[host]:[port]/[database]#oracle.jdbc.driver.OracleDriver">
                Oracle
            </option>
            <%}%>

            <%if ("db2".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="jdbc:db2:[database]#com.ibm.db2.jcc.DB2Driver">IBM
                                                                                              DB2
            </option>
            <%} else {%>
            <option value="jdbc:db2:[database]#com.ibm.db2.jcc.DB2Driver">IBM DB2</option>
            <%}%>

            <%if ("hsqldb".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="jdbc:hsqldb:[path]#org.hsqldb.jdbcDriver">HSQLDB
            </option>
            <%} else {%>
            <option value="jdbc:hsqldb:[path]#org.hsqldb.jdbcDriver">HSQLDB</option>
            <%}%>
            <%if ("informix-sqli".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="jdbc:informix-sqli://[HOST]:[PORT]/[database]:INFORMIXSERVER=[server-name]#com.informix.jdbc.IfxDriver">
                Informix
            </option>
            <%} else {%>
            <option value="jdbc:informix-sqli://[HOST]:[PORT]/[database]:INFORMIXSERVER=[server-name]#com.informix.jdbc.IfxDriver">
                Informix
            </option>
            <%}%>

            <%if ("postgresql".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="jdbc:postgresql://[HOST]:[PORT5432]/[database]#org.postgresql.Driver">
                PostgreSQL
            </option>
            <%} else {%>
            <option value="jdbc:postgresql://[HOST]:[PORT5432]/[database]#org.postgresql.Driver">
                PostgreSQL
            </option>
            <%}%>

            <%if ("sybase".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="jdbc:sybase:Tds:[HOST]:[PORT2048]/[database]#com.sybase.jdbc3.jdbc.SybDriver">
                Sybase ASE
            </option>
            <%} else {%>
            <option value="jdbc:sybase:Tds:[HOST]:[PORT2048]/[database]#com.sybase.jdbc3.jdbc.SybDriver">
                Sybase ASE
            </option>
            <%}%>

            <%if ("h2".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="jdbc:h2:tcp:[HOST]:[PORT]/[database]#org.h2.Driver">
                H2
            </option>
            <%} else {%>
            <option value="jdbc:h2:tcp:[HOST]:[PORT]/[database]#org.h2.Driver">H2</option>
            <%}%>

            <%if ("Generic".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="Generic#Generic">Generic</option>
            <%} else {%>
            <option value="Generic#Generic">Generic</option>
            <%}%>
        </select>
    </td>
</tr>
    <%} else if("RDBMS".equals(dataSourceType) && isXAType) { %>
<tr>
    <td class="leftCol-small" style="white-space: nowrap;"><label><fmt:message key="datasource.database.engine"/><font
            color="red">*</font></label></td>
    <td>
        <select name="databaseEngine" id="databaseEngine"
                    onchange="changeXADataSourceEngine(this,document)">
            <%if (("#".equals(rdbmsEngineType)|| rdbmsEngineType.equals(""))) {%>
            <option value="#" selected="selected">--SELECT--</option>
            <%} else {%>
            <option value="#">--SELECT--</option>
            <%}%>
            <%if ("mysql".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="<%=DBConstants.XAJDBCDriverClasses.MYSQL+"#jdbc:mysql://[machine-name/ip]:[port]/[database-name]"%>"> MySQL
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.MYSQL+"#jdbc:mysql://[machine-name/ip]:[port]/[database-name]"%>">  MySQL
            </option>
            <%}%>

            <%if ("derby".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="<%=DBConstants.XAJDBCDriverClasses.DERBY+"#jdbc:derby:[path-to-data-file]"%>"> Apache Derby
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.DERBY+"#jdbc:derby:[path-to-data-file]"%>"> Apache Derby
            </option>
            <%}%>

            <%if ("mssqlserver".equals(rdbmsEngineType)) {%>
            <option selected="selected"   value="<%=DBConstants.XAJDBCDriverClasses.MSSQL+"#jdbc:sqlserver://[HOST]:[PORT1433]"%>">
                Microsoft SQL Server
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.MSSQL+"#jdbc:sqlserver://[HOST]:[PORT1433]"%>">
                Microsoft SQL Server
            </option>
            <%}%>

            <%if ("oracle".equals(rdbmsEngineType)) {%>
            <option selected="selected"
                    value="<%=DBConstants.XAJDBCDriverClasses.ORACLE+"#jdbc:oracle:[drivertype]:[username/password]@[host]:[port]"%>">Oracle
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.ORACLE+"#jdbc:oracle:[drivertype]:[username/password]@[host]:[port]"%>">Oracle
            </option>
            <%}%>

            <%if ("db2".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="<%=DBConstants.XAJDBCDriverClasses.DB2+"#jdbc:db2:[database]"%>">IBM DB2
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.DB2+"#jdbc:db2:[database]"%>">IBM DB2</option>
            <%}%>

            <%if ("hsqldb".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="<%=DBConstants.XAJDBCDriverClasses.HSQLDB+"#jdbc:hsqldb:[path]"%>">HSQLDB
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.HSQLDB+"#jdbc:hsqldb:[path]"%>">HSQLDB</option>
            <%}%>

            <%if ("informix-sqli".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="<%=DBConstants.XAJDBCDriverClasses.INFORMIX+"#jdbc:informix-sqli://[HOST]:[PORT]/[database]:INFORMIXSERVER=[server-name]"%>"> Informix
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.INFORMIX+"#jdbc:informix-sqli://[HOST]:[PORT]/[database]:INFORMIXSERVER=[server-name]"%>"> Informix
            </option>
            <%}%>

            <%if ("postgresql".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="<%=DBConstants.XAJDBCDriverClasses.POSTGRESQL+"#jdbc:postgresql://[HOST]:[PORT5432]/[database]"%>"> PostgreSQL
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.POSTGRESQL+"#jdbc:postgresql://[HOST]:[PORT5432]/[database]"%>"> PostgreSQL
            </option>
            <%}%>

            <%if ("sybase".equals(rdbmsEngineType)) {%>
            <option selected="selected"  value="<%=DBConstants.XAJDBCDriverClasses.SYBASE+"#jdbc:sybase:Tds:[HOST]:[PORT2048]/[database]"%>">  Sybase ASE
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.SYBASE+"#jdbc:sybase:Tds:[HOST]:[PORT2048]/[database]"%>">   Sybase ASE
            </option>
            <%}%>

            <%if ("h2".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="<%=DBConstants.XAJDBCDriverClasses.H2+"#jdbc:h2:tcp:[HOST]:[PORT]/[database]"%>">   H2
            </option>
            <%} else {%>
            <option value="<%=DBConstants.XAJDBCDriverClasses.H2+"#jdbc:h2:tcp:[HOST]:[PORT]/[database]"%>">H2</option>
            <%}%>

            <%if ("Generic".equals(rdbmsEngineType)) {%>
            <option selected="selected" value="Generic">Generic</option>
            <%} else {%>
            <option value="Generic#Generic">Generic</option>
            <%}%>
        </select>
    </td>
</tr>

<%} else if("CUSTOM".equals(dataSourceType)) {%>
	<tr>
	<td colspan="2">
		<%if (customDSType.equals(DBConstants.DataSourceTypes.CUSTOM_QUERY)) { %>
			<input type="radio" name="customType" id="custom_tabular" value="tabular" onclick="changeCustomDsType()"/> Custom Tabular Datasource
			<input type="radio" name="customType" value="query" id="custom_query" onclick="changeCustomDsType()" checked/> Custom Query Datasource
		<%} else { %>
			<input type="radio" name="customType" id="custom_tabular" value="tabular" onclick="changeCustomDsType()" checked/> Custom Tabular Datasource
			<input type="radio" name="customType" value="query" id="custom_query" onclick="changeCustomDsType()"/> Custom Query Datasource
		<%} %>
		<input type="hidden" id="customTypeValue" name="customTypeValue" value="<%=customDSType %>"/>
	</td>
	</tr>
<%} %>

</div>
</tr>



<%
    if (propertyIterator != null) {
        while (propertyIterator.hasNext()) {
            Property property = (Property) propertyIterator.next();
            String propertyName = property.getName();
            String propertyValue = null;

            if(property.getValue() instanceof String){
               propertyValue = (String)property.getValue();
            }   else if (property.getValue() instanceof ArrayList) {
                    if (propertyName.equals(RDBMS.DATASOURCE_PROPS) && isXAType) {
                       Iterator<Property> iterator = ((ArrayList<Property>)property.getValue()).iterator();
                        while (iterator.hasNext()) {
                            Property availableProperty = iterator.next();
                %>
                <tr>
                    <td><label><%=availableProperty.getName()%></label></td>
                    <td>
                        <input type="text" size="50" id="<%=availableProperty.getName()%>" name="<%=availableProperty.getName()%>"
                               value="<%=availableProperty.getValue()%>"/>
                        <% if(availableProperty.isUseSecretAlias()) {%>
                        <input type="checkbox" id="useSecretAliasFor<%=availableProperty.getName()%>" name="useSecretAliasFor<%=availableProperty.getName()%>" 
                        						onclick="getUseSecretAliasValueForProperty(this,'useSecretAliasFor<%=availableProperty.getName()%>')"
                        						checked/>
                        <% } else {%>
                        <input type="checkbox" id="useSecretAliasFor<%=availableProperty.getName()%>" name="useSecretAliasFor<%=availableProperty.getName()%>" 
                        						onclick="getUseSecretAliasValueForProperty(this,'useSecretAliasFor<%=availableProperty.getName()%>')"
                        						/>
                        <%} %>
	               		<fmt:message key="usePasswordAlias"/>
	               	</td>
                    
                </tr>
            <%
        }%>
        <tr>
                <td colspan="2">
                    <a class="icon-link" style="background-image:url(../admin/images/add.gif);" onclick="addXAPropertyFields(document,document.getElementById('propertyCount').value);" ><fmt:message key="add.new.xa.datasource.properties"/></a>
                </td>
                
            </tr>
            <tr>
            	<td id="externalDSProperties" style="display:none" colspan="2">
                	<table id="externalDSPropertiesTable">
                	<tbody>
                	</tbody>
                	</table>
                </td>
            </tr>
        <%} else if (propertyName.equals(DBConstants.CustomDataSource.DATA_SOURCE_PROPS)){
        		Iterator<Property> iterator = ((ArrayList<Property>)property.getValue()).iterator();
             	while (iterator.hasNext()) {
                	Property availableProperty = iterator.next();
        %>
        <tr>
                    <td><label><%=availableProperty.getName()%></label></td>
                    <td>
                        <input type="text" size="50" id="<%=availableProperty.getName()%>" name="<%=availableProperty.getName()%>"
                               value="<%=availableProperty.getValue()%>"/>
                        <% if(availableProperty.isUseSecretAlias()) {%>
                        <input type="checkbox" id="useSecretAliasFor<%=availableProperty.getName()%>" name="useSecretAliasFor<%=availableProperty.getName()%>" 
                        						onclick="getUseSecretAliasValueForProperty(this,'useSecretAliasFor<%=availableProperty.getName()%>')"
                        						checked/>
                        <% } else {%>
                        <input type="checkbox" id="useSecretAliasFor<%=availableProperty.getName()%>" name="useSecretAliasFor<%=availableProperty.getName()%>" 
                        						onclick="getUseSecretAliasValueForProperty(this,'useSecretAliasFor<%=availableProperty.getName()%>')"
                        						/>
                        <%} %>
	               		<fmt:message key="usePasswordAlias"/>
	               	</td>
                    
                </tr>
            <%  }%>
        <tr>
                <td colspan="2">
                    <a class="icon-link" style="background-image:url(../admin/images/add.gif);" onclick="addXAPropertyFields(document,document.getElementById('propertyCount').value);" ><fmt:message key="add.new.xa.datasource.properties"/></a>
                </td>
                
            </tr>
            <tr>
            	<td id="externalDSProperties" style="display:none" colspan="2">
                	<table id="externalDSPropertiesTable">
                	<tbody>
                	</tbody>
                	</table>
                </td>
            </tr>	
        <%}
        } %>

<% boolean trshow = true;
   if ((propertyName.equals("gspread_username") || propertyName.equals("gspread_password"))
           && (visibility == null || visibility.equals("public"))) {
         trshow = false;
    }
%>
<tr id="<%=("tr:" + propertyName)%>" style='display:<%=(trshow?"table-row":"none")%>;vertical-align:top !important"' valign="top">
    <% if (!(propertyName.equals("rdf_datasource")
            ||propertyName.equals("excel_datasource")
            ||propertyName.equals("csv_datasource")
            ||propertyName.equals(DBConstants.MongoDB.SERVERS)
            ||propertyName.equals(DBConstants.Cassandra.CASSANDRA_SERVERS)
    		||propertyName.equals(RDBMS.DRIVER_CLASSNAME)
    		||propertyName.equals(RDBMS.URL)
    		||propertyName.equals(RDBMS.USERNAME)
    		||propertyName.equals(RDBMS.PASSWORD)
    		||propertyName.equals(RDBMS.DATASOURCE_PROPS)
    		||propertyName.equals(RDBMS.DATASOURCE_CLASSNAME)
    		||propertyName.equals(DBConstants.RDBMS.DEFAULT_TX_ISOLATION)
    		||propertyName.equals(DBConstants.RDBMS.TEST_ON_RETURN)
    		||propertyName.equals(DBConstants.RDBMS.TEST_WHILE_IDLE)
    		||propertyName.equals(DBConstants.RDBMS.TEST_ON_BORROW)
    		||propertyName.equals(DBConstants.RDBMS.REMOVE_ABANDONED)
    		||propertyName.equals(DBConstants.RDBMS.LOG_ABANDONED)
    		||propertyName.equals(DBConstants.RDBMS.REMOVE_ABANDONED)
    		||propertyName.equals(DBConstants.RDBMS.INITIAL_SIZE)
    		||propertyName.equals(DBConstants.RDBMS.MAX_ACTIVE)
    		||propertyName.equals(DBConstants.RDBMS.MAX_IDLE)
    		||propertyName.equals(DBConstants.RDBMS.MIN_IDLE)
    		||propertyName.equals(DBConstants.RDBMS.MAX_WAIT)
    		||propertyName.equals(DBConstants.RDBMS.VALIDATION_QUERY)
    		||propertyName.equals(DBConstants.RDBMS.TEST_ON_RETURN)
    		||propertyName.equals(DBConstants.RDBMS.TEST_ON_BORROW)
    		||propertyName.equals(DBConstants.RDBMS.TEST_WHILE_IDLE)
    		||propertyName.equals(DBConstants.RDBMS.TIME_BETWEEN_EVICTION_RUNS_MILLIS)
    		||propertyName.equals(DBConstants.RDBMS.NUM_TESTS_PER_EVICTION_RUN)
    		||propertyName.equals(DBConstants.RDBMS.MIN_EVICTABLE_IDLE_TIME_MILLIS)
    		||propertyName.equals(DBConstants.RDBMS.REMOVE_ABANDONED_TIMEOUT)
    		||propertyName.equals(DBConstants.RDBMS.AUTO_COMMIT)
            ||propertyName.equals(DBConstants.RDBMS.DEFAULT_READONLY)
            ||propertyName.equals(DBConstants.RDBMS.DEFAULT_CATALOG)
            ||propertyName.equals(DBConstants.RDBMS.VALIDATOR_CLASSNAME)
            ||propertyName.equals(DBConstants.RDBMS.CONNECTION_PROPERTIES)
            ||propertyName.equals(DBConstants.RDBMS.INIT_SQL)
            ||propertyName.equals(DBConstants.RDBMS.JDBC_INTERCEPTORS)
            ||propertyName.equals(DBConstants.RDBMS.VALIDATION_INTERVAL)
            ||propertyName.equals(DBConstants.RDBMS.JMX_ENABLED)
            ||propertyName.equals(DBConstants.RDBMS.FAIR_QUEUE)
            ||propertyName.equals(DBConstants.RDBMS.ABANDON_WHEN_PERCENTAGE_FULL)
            ||propertyName.equals(DBConstants.RDBMS.MAX_AGE)
            ||propertyName.equals(DBConstants.RDBMS.USE_EQUALS)
            ||propertyName.equals(DBConstants.RDBMS.SUSPECT_TIMEOUT)
            ||propertyName.equals(DBConstants.RDBMS.VALIDATION_QUERY_TIMEOUT)
            ||propertyName.equals(DBConstants.RDBMS.ALTERNATE_USERNAME_ALLOWED)
            ||propertyName.equals(DBConstants.RDBMS.DYNAMIC_USER_AUTH_CLASS)
            ||propertyName.equals(DBConstants.RDBMS.DYNAMIC_USER_AUTH_MAPPING)
            ||propertyName.equals(DBConstants.CustomDataSource.DATA_SOURCE_PROPS) 
            ||propertyName.equals(DBConstants.CustomDataSource.DATA_SOURCE_QUERY_CLASS)
            ||propertyName.equals(DBConstants.CustomDataSource.DATA_SOURCE_TABULAR_CLASS)) && 
            	!(propertyName.equals(DBConstants.GSpread.DATASOURCE) && useQueryMode) &&
            	!(propertyName.equals("gspread_visibility") && useQueryMode) &&
            	!(propertyName.equals("gspread_username") && useQueryMode) &&
            	!(propertyName.equals("gspread_password") && useQueryMode)
            ){%>
    <td class="leftCol-small" style="white-space: nowrap;">
        <fmt:message key="<%=propertyName%>"/><%=(isFieldMandatory(propertyName)?"<font color=\"red\">*</font>":"")%>
    </td>
    <td>
    <%  } if (propertyName.equals("csv_hasheader")) { %>
        <select id="<%=propertyName%>" name="<%=propertyName%>">
            <% if (propertyValue.equals("")) { %>
            <option value="" selected="selected">--SELECT--</option>
            <% } else { %>
            <option value="">--SELECT--</option>
            <% } %>

            <% if (propertyValue.equals("true")) { %>
            <option value="true" selected="selected">true</option>
            <% } else { %>
            <option value="true">true</option>
            <% } %>

            <% if (propertyValue.equals("false")) { %>
            <option value="false" selected="selected">false</option>
            <% } else { %>
            <option value="false">false</option>
            <% } %>
        </select>
        <% } else if (propertyName.equals(DBConstants.MongoDB.AUTO_CONNECT_RETRY) || propertyName.equals(DBConstants.Cassandra.ENABLE_JMX_REPORTING)
                  || propertyName.equals(DBConstants.Cassandra.ENABLE_METRICS) || propertyName.equals(DBConstants.Cassandra.KEEP_ALIVE)
                  || propertyName.equals(DBConstants.Cassandra.REUSE_ADDRESS) || propertyName.equals(DBConstants.Cassandra.TCP_NODELAY)
                  || propertyName.equals(DBConstants.Cassandra.ENABLE_SSL))
                  { %>
        <select id="<%=propertyName%>" name="<%=propertyName%>">
            <% if (propertyValue.equals("")) { %>
            <option value="" selected="selected">--SELECT--</option>
            <% } else { %>
            <option value="">--SELECT--</option>
            <% } %>

            <% if (propertyValue.equals("true")) { %>
            <option value="true" selected="selected">true</option>
            <% } else { %>
            <option value="true">true</option>
            <% } %>

            <% if (propertyValue.equals("false")) { %>
            <option value="false" selected="selected">false</option>
            <% } else { %>
            <option value="false">false</option>
            <% } %>
        </select>
        <%  } else if (propertyName.equals(DBConstants.MongoDB.WRITE_CONCERN)) { %>
            <select id="<%=propertyName%>" name="<%=propertyName%>">
                <% if (propertyValue.equals("")) { %>
                <option value="" selected="selected">--SELECT--</option>
                <% } else { %>
                <option value="">--SELECT--</option>
                <% } %>
                <% if (propertyValue.equals("FSYNC_SAFE")) { %>
                <option value="FSYNC_SAFE" selected="selected">FSYNC_SAFE</option>
                <% } else { %>
                <option value="FSYNC_SAFE">FSYNC_SAFE</option>
                <% } %>
                <% if (propertyValue.equals("NONE")) { %>
                <option value="NONE" selected="selected">NONE</option>
                <% } else { %>
                <option value="NONE">NONE</option>
                <% } %>
                <% if (propertyValue.equals("NORMAL")) { %>
                <option value="NORMAL" selected="selected">NORMAL</option>
                <% } else { %>
                <option value="NORMAL">NORMAL</option>
                <% } %>
                <% if (propertyValue.equals("REPLICAS_SAFE")) { %>
                <option value="REPLICAS_SAFE" selected="selected">REPLICAS_SAFE</option>
                <% } else { %>
                <option value="REPLICAS_SAFE">REPLICAS_SAFE</option>
                <% } %>
                <% if (propertyValue.equals("SAFE")) { %>
                <option value="SAFE" selected="selected">SAFE</option>
                <% } else { %>
                <option value="SAFE">SAFE</option>
                <% } %>
                <% if (propertyValue.equals("STRICT")) { %>
                <option value="STRICT" selected="selected">STRICT</option>
                <% } else { %>
                <option value="STRICT">STRICT</option>
                <% } %>
            </select>
            <%  } else if (propertyName.equals(DBConstants.MongoDB.READ_PREFERENCE)) { %>
            <select id="<%=propertyName%>" name="<%=propertyName%>">
                <% if (propertyValue.equals("")) { %>
                <option value="" selected="selected">--SELECT--</option>
                <% } else { %>
                <option value="">--SELECT--</option>
                <% } %>
                <% if (propertyValue.equals("PRIMARY")) { %>
                <option value="PRIMARY" selected="selected">PRIMARY</option>
                <% } else { %>
                <option value="PRIMARY">PRIMARY</option>
                <% } %>
                <% if (propertyValue.equals("SECONDARY")) { %>
                <option value="SECONDARY" selected="selected">SECONDARY</option>
                <% } else { %>
                <option value="SECONDARY">SECONDARY</option>
                <% } %>
            </select>
            <%  } else if (propertyName.equals(DBConstants.Cassandra.COMPRESSION)) { %>
            <select id="<%=propertyName%>" name="<%=propertyName%>">
                <% if (propertyValue.equals("")) { %>
                <option value="" selected="selected">--SELECT--</option>
                <% } else { %>
                <option value="">--SELECT--</option>
                <% } %>
                <% if (propertyValue.equals("LZ4")) { %>
                <option value="LZ4" selected="selected">LZ4</option>
                <% } else { %>
                <option value="LZ4">LZ4</option>
                <% } %>
                <% if (propertyValue.equals("NONE")) { %>
                <option value="NONE" selected="selected">NONE</option>
                <% } else { %>
                <option value="NONE">NONE</option>
                <% } %>
                <% if (propertyValue.equals("SNAPPY")) { %>
                <option value="SNAPPY" selected="selected">SNAPPY</option>
                <% } else { %>
                <option value="SNAPPY">SNAPPY</option>
                <% } %>
            </select>
            <%  } else if (propertyName.equals(DBConstants.Cassandra.LOAD_BALANCING_POLICY)) { %>
            <select id="<%=propertyName%>" name="<%=propertyName%>">
                <% if (propertyValue.equals("")) { %>
                <option value="" selected="selected">--SELECT--</option>
                <% } else { %>
                <option value="">--SELECT--</option>
                <% } %>
                <% if (propertyValue.equals("RoundRobinPolicy")) { %>
                <option value="RoundRobinPolicy" selected="selected">RoundRobinPolicy</option>
                <% } else { %>
                <option value="RoundRobinPolicy">RoundRobinPolicy</option>
                <% } %>
                <% if (propertyValue.equals("LatencyAwarePolicy")) { %>
                <option value="LatencyAwarePolicy" selected="selected">LatencyAwarePolicy</option>
                <% } else { %>
                <option value="LatencyAwarePolicy">LatencyAwarePolicy</option>
                <% } %>
                <% if (propertyValue.equals("TokenAwarePolicy")) { %>
                <option value="TokenAwarePolicy" selected="selected">TokenAwarePolicy</option>
                <% } else { %>
                <option value="TokenAwarePolicy">TokenAwarePolicy</option>
                <% } %>
            </select>
            <%  } else if (propertyName.equals(DBConstants.Cassandra.CONSISTENCY_LEVEL) || propertyName.equals(DBConstants.Cassandra.SERIAL_CONSISTENCY_LEVEL)) { %>
            <select id="<%=propertyName%>" name="<%=propertyName%>">
                <% if (propertyValue.equals("")) { %>
                <option value="" selected="selected">--SELECT--</option>
                <% } else { %>
                <option value="">--SELECT--</option>
                <% } %>
                <% if (propertyValue.equals("ALL")) { %>
                <option value="ALL" selected="selected">ALL</option>
                <% } else { %>
                <option value="ALL">ALL</option>
                <% } %>
                <% if (propertyValue.equals("ANY")) { %>
                <option value="ANY" selected="selected">ANY</option>
                <% } else { %>
                <option value="ANY">ANY</option>
                <% } %>
                <% if (propertyValue.equals("EACH_QUORUM")) { %>
                <option value="EACH_QUORUM" selected="selected">EACH_QUORUM</option>
                <% } else { %>
                <option value="EACH_QUORUM">EACH_QUORUM</option>
                <% } %>
                <% if (propertyValue.equals("LOCAL_ONE")) { %>
                <option value="LOCAL_ONE" selected="selected">LOCAL_ONE</option>
                <% } else { %>
                <option value="LOCAL_ONE">LOCAL_ONE</option>
                <% } %>
                <% if (propertyValue.equals("LOCAL_QUORUM")) { %>
                <option value="LOCAL_QUORUM" selected="selected">LOCAL_QUORUM</option>
                <% } else { %>
                <option value="LOCAL_QUORUM">LOCAL_QUORUM</option>
                <% } %>
                <% if (propertyValue.equals("LOCAL_SERIAL")) { %>
                <option value="LOCAL_SERIAL" selected="selected">LOCAL_SERIAL</option>
                <% } else { %>
                <option value="LOCAL_SERIAL">LOCAL_SERIAL</option>
                <% } %>
                <% if (propertyValue.equals("ONE")) { %>
                <option value="ONE" selected="selected">ONE</option>
                <% } else { %>
                <option value="ONE">ONE</option>
                <% } %>
                <% if (propertyValue.equals("QUORUM")) { %>
                <option value="QUORUM" selected="selected">QUORUM</option>
                <% } else { %>
                <option value="QUORUM">QUORUM</option>
                <% } %>
                <% if (propertyValue.equals("SERIAL")) { %>
                <option value="SERIAL" selected="selected">SERIAL</option>
                <% } else { %>
                <option value="SERIAL">SERIAL</option>
                <% } %>
                <% if (propertyValue.equals("THREE")) { %>
                <option value="THREE" selected="selected">THREE</option>
                <% } else { %>
                <option value="THREE">THREE</option>
                <% } %>
                <% if (propertyValue.equals("TWO")) { %>
                <option value="TWO" selected="selected">TWO</option>
                <% } else { %>
                <option value="TWO">TWO</option>
                <% } %>
            </select>
            <%  } else if (propertyName.equals(DBConstants.Cassandra.PROTOCOL_VERSION)) { %>
            <select id="<%=propertyName%>" name="<%=propertyName%>">
                <% if (propertyValue.equals("")) { %>
                <option value="" selected="selected">--SELECT--</option>
                <% } else { %>
                <option value="">--SELECT--</option>
                <% } %>
                <% if (propertyValue.equals("1")) { %>
                <option value="1" selected="selected">1</option>
                <% } else { %>
                <option value="1">1</option>
                <% } %>
                <% if (propertyValue.equals("2")) { %>
                <option value="2" selected="selected">2</option>
                <% } else { %>
                <option value="2">2</option>
                <% } %>
            </select>
            <%  } else if (propertyName.equals(DBConstants.Cassandra.RECONNECTION_POLICY)) { %>
            <select id="<%=propertyName%>" name="<%=propertyName%>">
                <% if (propertyValue.equals("")) { %>
                <option value="" selected="selected">--SELECT--</option>
                <% } else { %>
                <option value="">--SELECT--</option>
                <% } %>
                <% if (propertyValue.equals("ConstantReconnectionPolicy")) { %>
                <option value="ConstantReconnectionPolicy" selected="selected">ConstantReconnectionPolicy</option>
                <% } else { %>
                <option value="ConstantReconnectionPolicy">ConstantReconnectionPolicy</option>
                <% } %>
                <% if (propertyValue.equals("ExponentialReconnectionPolicy")) { %>
                <option value="ExponentialReconnectionPolicy" selected="selected">ExponentialReconnectionPolicy</option>
                <% } else { %>
                <option value="ExponentialReconnectionPolicy">ExponentialReconnectionPolicy</option>
                <% } %>
            </select>
            <%  } else if (propertyName.equals(DBConstants.Cassandra.RETRY_POLICY)) { %>
            <select id="<%=propertyName%>" name="<%=propertyName%>">
                <% if (propertyValue.equals("")) { %>
                <option value="" selected="selected">--SELECT--</option>
                <% } else { %>
                <option value="">--SELECT--</option>
                <% } %>
                <% if (propertyValue.equals("DefaultRetryPolicy")) { %>
                <option value="DefaultRetryPolicy" selected="selected">DefaultRetryPolicy</option>
                <% } else { %>
                <option value="DefaultRetryPolicy">DefaultRetryPolicy</option>
                <% } %>
                <% if (propertyValue.equals("DowngradingConsistencyRetryPolicy")) { %>
                <option value="DowngradingConsistencyRetryPolicy" selected="selected">DowngradingConsistencyRetryPolicy</option>
                <% } else { %>
                <option value="DowngradingConsistencyRetryPolicy">DowngradingConsistencyRetryPolicy</option>
                <% } %>
                <% if (propertyValue.equals("FallthroughRetryPolicy")) { %>
                <option value="FallthroughRetryPolicy" selected="selected">FallthroughRetryPolicy</option>
                <% } else { %>
                <option value="FallthroughRetryPolicy">FallthroughRetryPolicy</option>
                <% } %>
                <% if (propertyValue.equals("LoggingDefaultRetryPolicy")) { %>
                <option value="LoggingDefaultRetryPolicy" selected="selected">LoggingDefaultRetryPolicy</option>
                <% } else { %>
                <option value="LoggingDefaultRetryPolicy">LoggingDefaultRetryPolicy</option>
                <% } %>
                <% if (propertyValue.equals("LoggingDowngradingConsistencyRetryPolicy")) { %>
                <option value="LoggingDowngradingConsistencyRetryPolicy" selected="selected">LoggingDowngradingConsistencyRetryPolicy</option>
                <% } else { %>
                <option value="LoggingDowngradingConsistencyRetryPolicy">LoggingDowngradingConsistencyRetryPolicy</option>
                <% } %>
                <% if (propertyValue.equals("LoggingFallthroughRetryPolicy")) { %>
                <option value="LoggingFallthroughRetryPolicy" selected="selected">LoggingFallthroughRetryPolicy</option>
                <% } else { %>
                <option value="LoggingFallthroughRetryPolicy">LoggingFallthroughRetryPolicy</option>
                <% } %>
            </select>
        <% } else if (propertyName.equals("gspread_visibility")) { %>
	        <%if (!useQueryMode) { %>
		        <select id="<%=propertyName%>" name="<%=propertyName%>" onchange="javascript:gspreadVisibiltyOnChange(this,document);return false;">
		            <% if (propertyValue.equals("private")) { %>
		            <option value="private" selected="selected">Private</option>
		            <% } else { %>
		            <option value="private">Private</option>
		            <% } %>
		            <% if (propertyValue.equals("public") || propertyValue.equals("")) { %>
		            <option value="public" selected="selected">Public</option>
		            <% } else { %>
		            <option value="public">Public</option>
		            <% } %>
		        </select>
		     <%} %>
         <% } else if (propertyName.equals(RDBMS.DRIVER_CLASSNAME)
                    ||propertyName.equals(RDBMS.URL)
                    ||propertyName.equals(RDBMS.USERNAME)
         		    ||propertyName.equals(RDBMS.PASSWORD)
         		    ||propertyName.equals(RDBMS.DATASOURCE_CLASSNAME)
         		    ||propertyName.equals(CustomDataSource.DATA_SOURCE_QUERY_CLASS)
         		    ||propertyName.equals(CustomDataSource.DATA_SOURCE_TABULAR_CLASS)) {
         		  if ((propertyName.equals(RDBMS.DRIVER_CLASSNAME)
         		    ||propertyName.equals(RDBMS.URL)
         		    ||propertyName.equals(RDBMS.USERNAME)
         		    || propertyName.equals(RDBMS.PASSWORD)) && !isXAType) {
         			  
         			  if (!(dataSourceType.equals("GDATA_SPREADSHEET") || dataSourceType.equals("EXCEL"))) {
         		%>
                 <tr>
                     <% if((dataSourceType.equals("Cassandra") && propertyName.equals(RDBMS.URL))) { %>
                        <td>Server URL<%=(isFieldMandatory(propertyName)?"<font color=\"red\">*</font>":"")%></td>
                     <% } else if(!(propertyName.equals(RDBMS.DRIVER_CLASSNAME) && dataSourceType.equals("Cassandra"))){ %>
                        <td><fmt:message key="<%=propertyName%>"/><%=(isFieldMandatory(propertyName)?"<font color=\"red\">*</font>":"")%></td>
                     <% } %>
	         		 <%if(propertyName.equals(RDBMS.PASSWORD)) { %>
	               		<td>
	               		<%if(useSecretAlias) {%>
	               			<input type="text" size="50" id="pwdalias" name="pwdalias" value="<%=propertyValue%>">
	               			<input type="password" size="50" id="<%=propertyName%>" name="<%=propertyName%>" value="<%=propertyValue%>" style="display:none"/>
	               			<input type="checkbox" id="useSecretAlias" name="useSecretAlias" onclick="getUseSecretAliasValue(this, '<%=propertyName%>')" checked/>
	               			<fmt:message key="usePasswordAlias"/>
	               		<%} else { %>
	               			<input type="text" size="50" id="pwdalias" name="pwdalias" value="<%=propertyValue%>" style="display:none">
	               			<input type="password" size="50" id="<%=propertyName%>" name="<%=propertyName%>" value="<%=propertyValue%>"/>
	               			<input type="checkbox" id="useSecretAlias" name="useSecretAlias" onclick="getUseSecretAliasValue(this, '<%=propertyName%>')"/>
	               			<fmt:message key="usePasswordAlias"/>
	               		<%} %>
	               		<input type="hidden" id="useSecretAliasValue" name="useSecretAliasValue" size="50" value="<%=useSecretAlias%>">
	               		</td>
	            
                </tr>
               		<%} else {  %>

                           <td><input type="text" size="50" id="<%=propertyName%>" name="<%=propertyName%>" value="<%=propertyValue%>" /></td>
                    <% } %>


                 <% } else if (flag.equals("edit") && useQueryMode){ %>
                 	<%if (propertyName.equals(RDBMS.URL)) { %>
                 		<tr>
                 			<td class="leftCol-small" style="white-space: nowrap;">
                 				<%if (dataSourceType.equals("GDATA_SPREADSHEET")) {%>
        							<fmt:message key="<%=DBConstants.GSpread.DATASOURCE%>"/><%=(isFieldMandatory(DBConstants.GSpread.DATASOURCE)?"<font color=\"red\">*</font>":"")%>
        						<%} else { %>
        							<fmt:message key="<%=DBConstants.Excel.DATASOURCE%>"/><%=(isFieldMandatory(DBConstants.Excel.DATASOURCE)?"<font color=\"red\">*</font>":"")%>
        						<%} %>
    						</td>
                 			<td>
                 				<%if (dataSourceType.equals("GDATA_SPREADSHEET")) {%>
                 					<input type="text" size="50" id="<%=DBConstants.GSpread.DATASOURCE %>" name="<%=DBConstants.GSpread.DATASOURCE %>" value="<%=getExcelGspreadUrl(propertyValue, dataSourceType)%>" />
                 				<%} else { %>
                 					<input type="text" size="50" id="<%=DBConstants.Excel.DATASOURCE %>" name="<%=DBConstants.Excel.DATASOURCE %>" value="<%=getExcelGspreadUrl(propertyValue, dataSourceType)%>" />
                 				<%} %>
                 			</td>
                 		</tr>
	                 		<%if (dataSourceType.equals("GDATA_SPREADSHEET")) {%>
	                 		<tr>
	                 			<td class="leftCol-small" style="white-space: nowrap;">
					        		<fmt:message key="<%=DBConstants.GSpread.VISIBILITY%>"/><%=(isFieldMandatory(DBConstants.GSpread.VISIBILITY)?"<font color=\"red\">*</font>":"")%>
					        	</td>
					        	<td>
			                 		<select id="<%=DBConstants.GSpread.VISIBILITY%>" name="<%=DBConstants.GSpread.VISIBILITY%>" onchange="javascript:gspreadVisibiltyOnChangeQMode(this,document);return false;">
							            <% if (getVisibility(propertyValue).equals("private")) { %>
							            <option value="private" selected="selected">Private</option>
							            <% } else { %>
							            <option value="private">Private</option>
							            <% } %>
							            <% if (getVisibility(propertyValue).equals("public") || propertyValue.equals("")) { %>
							            <option value="public" selected="selected">Public</option>
							            <% } else { %>
							            <option value="public">Public</option>
							            <% } %>
					        		</select>
					        	</td>
					        </tr>
					     <%} %>
                 	<%} else if (propertyName.equals(RDBMS.USERNAME) && dataSourceType.equals("GDATA_SPREADSHEET")) { %>
                 		<tr id="tr:querymode_gspread_username"  style='display:<%=(!visibility.equals("public")?"":"none") %>'>
                 			<td class="leftCol-small" style="white-space: nowrap;">
                 				<fmt:message key="<%=DBConstants.GSpread.USERNAME%>"/><%=(isFieldMandatory(DBConstants.GSpread.USERNAME)?"<font color=\"red\">*</font>":"")%>
        					</td>
                 			<td><input type="text" size="50" id="<%=DBConstants.GSpread.USERNAME %>" name="<%=DBConstants.GSpread.USERNAME %>" value="<%=propertyValue%>" /></td>
                 		</tr>
                 	<%} else if (propertyName.equals(RDBMS.PASSWORD) && dataSourceType.equals("GDATA_SPREADSHEET")) { %>
                 		<tr id="tr:querymode_gspread_password" style='display:<%=(!visibility.equals("public")?"":"none") %>'>
                 			<td class="leftCol-small" style="white-space: nowrap;">
        						<fmt:message key="<%=DBConstants.GSpread.PASSWORD%>"/><%=(isFieldMandatory(DBConstants.GSpread.PASSWORD)?"<font color=\"red\">*</font>":"")%>
        					</td>
                 			<td>
                 			<%if(useSecretAlias) {%>
                 				<input type="text" size="50" id="pwdalias" name="pwdalias" value="<%=propertyValue%>">
                 				<input type="password" size="50" id="<%=DBConstants.GSpread.PASSWORD %>" name="<%=DBConstants.GSpread.PASSWORD %>" value="<%=propertyValue%>" style="display:none"/>
                 				<input type="checkbox" id="useSecretAlias" name="useSecretAlias" onclick="getUseSecretAliasValue(this, '<%=DBConstants.GSpread.PASSWORD%>')" checked/>
	               				<fmt:message key="usePasswordAlias"/>
                 			<%} else {%>
                 				<input type="text" size="50" id="pwdalias" name="pwdalias" value="<%=propertyValue%>" style="display:none">
                 				<input type="password" size="50" id="<%=DBConstants.GSpread.PASSWORD %>" name="<%=DBConstants.GSpread.PASSWORD %>" value="<%=propertyValue%>" />
                 				<input type="checkbox" id="useSecretAlias" name="useSecretAlias" onclick="getUseSecretAliasValue(this, '<%=DBConstants.GSpread.PASSWORD%>')"/>
	               				<fmt:message key="usePasswordAlias"/>
                 			<%} %>
                 			<input type="hidden" id="useSecretAliasValue" name="useSecretAliasValue" size="50" value="<%=useSecretAlias%>">
                 			</td>
                 		</tr>
                 	<%} %>
                 <% }%> 
                 
                 <%}  else if (propertyName.equals(RDBMS.DATASOURCE_CLASSNAME) && isXAType) {  %>
                    <tr>
                        <td><label><fmt:message key="xa.datasource.class"/><font color="red">*</font></label>
                        </td>
                        <td>
                            <input type="text" size="50" id="<%=propertyName%>" name="<%=propertyName%>" value="<%=propertyValue%>" />
                        </td>
                    </tr>
                 <%} else if (propertyName.equals(CustomDataSource.DATA_SOURCE_QUERY_CLASS) && customDSType.equals(DBConstants.DataSourceTypes.CUSTOM_QUERY)) {
                	 customConClassAdded = true;
               	  %>
                    <tr>
                        <td><label><fmt:message key="custom.datasource.class"/><font color="red">*</font></label>
                        </td>
                        <td>
                            <input type="text" size="50" id="customDataSourceClass" name="customDataSourceClass" value="<%=propertyValue%>" />
                        </td>
                    </tr>
                  <%} else if (propertyName.equals(CustomDataSource.DATA_SOURCE_TABULAR_CLASS) && customDSType.equals(DBConstants.DataSourceTypes.CUSTOM_TABULAR)) {
                	  customConClassAdded = true;
                  %>
                    <tr>
                        <td><label><fmt:message key="custom.datasource.class"/><font color="red">*</font></label>
                        </td>
                        <td>
                            <input type="text" size="50" id="customDataSourceClass" name="customDataSourceClass" value="<%=propertyValue%>" />
                        </td>
                    </tr>
                  <%} else if ((propertyName.equals(CustomDataSource.DATA_SOURCE_TABULAR_CLASS) ||
                		  propertyName.equals(CustomDataSource.DATA_SOURCE_QUERY_CLASS)) && !customConClassAdded){
                	  customConClassAdded = true;
                   %>
                    <tr>
                        <td><label><fmt:message key="custom.datasource.class"/><font color="red">*</font></label>
                        </td>
                        <td>
                            <input type="text" size="50" id="customDataSourceClass" name="customDataSourceClass" value="<%=propertyValue%>" />
                        </td>
                    </tr>
                 <%} }  else if (propertyName.equals("carbon_datasource_name")) { %>
        <select id="<%=propertyName%>" name="<%=propertyName%>">
            <option value="" selected="selected">--SELECT--</option>
            <%
                for (String dsName : carbonDataSourceNames) {
                    if (dsName.equals(propertyValue)) {
            %>
            <option value="<%=dsName%>" selected="selected"><%=dsName%>
            </option>
            <% } else {
            %>
            <option value="<%=dsName%>"><%=dsName%>
            </option>
            <% }
            } %>
        </select>
        <% } else if (propertyName.equals("web_harvest_config")) {            
              boolean checked = false;
              String filePath = "";
              String configEle = "";
              if (propertyValue != null) {
                   //propertyValue = scraperString;
                       if(propertyValue.trim().startsWith("<config>")) {
                           configEle = propertyValue;
                           checked = true;
                       } else {
                           filePath = propertyValue;
                           checked = false;
                       }
              }
             //session.setAttribute("web_harvest_config",scraperString);
        %>
        <input type="radio" value="file" name="config" id="configPath" onchange="changeWebHarvestConfig(this,document);" <%=!checked ? "checked='checked'" : ""%>> <fmt:message key="config.file.path"/>
        <input type="radio" value="config" name="config" id="config" <%=checked ? "checked='checked'" : ""%> onchange="changeWebHarvestConfig(this,document);"> <fmt:message key="web.harvest.config"/>
        <br/>

        <textarea cols="40" rows="5" name="web_harvest_config_textArea" <%=!checked ? "style=\'display:none\'" : ""%> id="web_harvest_config_textArea"><%=configEle%></textarea>
        <input type="text" size="50" id="<%=propertyName%>" <%=checked ? "style=\'display:none\'" : ""%> name="<%=propertyName%>"  value="<%=filePath%>"/>
        <td id="config_reg" ><a onclick="showResourceTree('<%=propertyName%>', setValueConf, '/_system/config')" style="background-image:url(images/registry_picker.gif);" class="icon-link" href="#" > Configuration Registry </a></td>
        <td id="gov_reg" ><a onclick="showResourceTree('<%=propertyName%>', setValueGov, '/_system/governance')" style="background-image:url(images/registry_picker.gif);" class="icon-link" href="#" > Govenance Registry </a></td>

        <% } else {
        	if(propertyName.equals("gspread_password") || propertyName.equals("jndi_password") || propertyName.equals(DBConstants.Cassandra.PASSWORD)) {%>
        	
        	<%if ((propertyName.equals("gspread_password") && !useQueryMode) || propertyName.equals("jndi_password") || propertyName.equals(DBConstants.Cassandra.PASSWORD)) { %>
		        <%if(useSecretAlias) {%>
			               <input type="text" size="50" id="pwdalias" name="pwdalias" value="<%=propertyValue%>">
			               <input type="password" size="50" id="<%=propertyName%>" name="<%=propertyName%>" value="<%=propertyValue%>" style="display:none"/>
			               <input type="checkbox" id="useSecretAlias" name="useSecretAlias" onclick="getUseSecretAliasValue(this, '<%=propertyName%>')" checked/>
			               <fmt:message key="usePasswordAlias"/>
			        <%} else { %>
			               	<input type="text" size="50" id="pwdalias" name="pwdalias" value="<%=propertyValue%>" style="display:none">
			               	<input type="password" size="50" id="<%=propertyName%>" name="<%=propertyName%>" value="<%=propertyValue%>"/>
			               	<input type="checkbox" id="useSecretAlias" name="useSecretAlias" onclick="getUseSecretAliasValue(this, '<%=propertyName%>')"/>
			               	<fmt:message key="usePasswordAlias"/>
			        <%} %>
			               	<input type="hidden" id="useSecretAliasValue" name="useSecretAliasValue" size="50" value="<%=useSecretAlias%>">
			  <%} %>
        </td>

         <%} else if (propertyName.equals("rdf_datasource")
                    ||propertyName.equals("excel_datasource")
                    ||propertyName.equals("csv_datasource")) {%>
                    		<%if (!(propertyName.equals("excel_datasource") && useQueryMode)) {%>
        	                <tr>
        	                <td><fmt:message key="<%=propertyName%>"/><%=(isFieldMandatory(propertyName)?"<font color=\"red\">*</font>":"")%></td>
                            <td><input type="text" size="50" id="<%=propertyName%>" name="<%=propertyName%>" value="<%=propertyValue%>" />
                            </td>
                               <td><a onclick="showResourceTree('<%=propertyName%>', setValueConf, '/_system/config')" style="background-image:url(images/registry_picker.gif);" class="icon-link" href="#"> Configuration Registry </a></td>
           	   					<td><a onclick="showResourceTree('<%=propertyName%>', setValueGov, '/_system/governance')" style="background-image:url(images/registry_picker.gif);" class="icon-link" href="#"> Govenance Registry </a></td>
                             </tr>
                        <%} %>
        <%} else if (propertyName.equals(DBConstants.MongoDB.SERVERS) || propertyName.equals(DBConstants.Cassandra.CASSANDRA_SERVERS)) {%>
                <tr>
                <td><fmt:message key="<%=propertyName%>"/><%=(isFieldMandatory(propertyName)?"<font color=\"red\">*</font>":"")%></td>
                <td><input type="text" size="50" id="<%=propertyName%>" name="<%=propertyName%>" value="<%=propertyValue%>" /></td>
                </tr>
       <%} else if (propertyName.equals(RDBMS.DATASOURCE_PROPS)){}
       else if (propertyName.equals(CustomDataSource.DATA_SOURCE_PROPS)){}
       else if (!(propertyName.equals(DBConstants.RDBMS.DEFAULT_TX_ISOLATION)
    		||propertyName.equals(DBConstants.RDBMS.TEST_ON_RETURN)
    		||propertyName.equals(DBConstants.RDBMS.TEST_WHILE_IDLE)
    		||propertyName.equals(DBConstants.RDBMS.TEST_ON_BORROW)
    		||propertyName.equals(DBConstants.RDBMS.REMOVE_ABANDONED)
    		||propertyName.equals(DBConstants.RDBMS.LOG_ABANDONED)
    		||propertyName.equals(DBConstants.RDBMS.REMOVE_ABANDONED)
    		||propertyName.equals(DBConstants.RDBMS.INITIAL_SIZE)
    		||propertyName.equals(DBConstants.RDBMS.MAX_ACTIVE)
    		||propertyName.equals(DBConstants.RDBMS.MAX_IDLE)
    		||propertyName.equals(DBConstants.RDBMS.MIN_IDLE)
    		||propertyName.equals(DBConstants.RDBMS.MAX_WAIT)
    		||propertyName.equals(DBConstants.RDBMS.VALIDATION_QUERY)
    		||propertyName.equals(DBConstants.RDBMS.TEST_ON_RETURN)
    		||propertyName.equals(DBConstants.RDBMS.TEST_ON_BORROW)
    		||propertyName.equals(DBConstants.RDBMS.TEST_WHILE_IDLE)
    		||propertyName.equals(DBConstants.RDBMS.TIME_BETWEEN_EVICTION_RUNS_MILLIS)
    		||propertyName.equals(DBConstants.RDBMS.NUM_TESTS_PER_EVICTION_RUN)
    		||propertyName.equals(DBConstants.RDBMS.MIN_EVICTABLE_IDLE_TIME_MILLIS)
    		||propertyName.equals(DBConstants.RDBMS.REMOVE_ABANDONED_TIMEOUT)
            ||propertyName.equals(DBConstants.RDBMS.AUTO_COMMIT)
            ||propertyName.equals(DBConstants.RDBMS.DEFAULT_READONLY)
            ||propertyName.equals(DBConstants.RDBMS.DEFAULT_CATALOG)
            ||propertyName.equals(DBConstants.RDBMS.VALIDATOR_CLASSNAME)
            ||propertyName.equals(DBConstants.RDBMS.CONNECTION_PROPERTIES)
            ||propertyName.equals(DBConstants.RDBMS.INIT_SQL)
            ||propertyName.equals(DBConstants.RDBMS.JDBC_INTERCEPTORS)
            ||propertyName.equals(DBConstants.RDBMS.VALIDATION_INTERVAL)
            ||propertyName.equals(DBConstants.RDBMS.JMX_ENABLED)
            ||propertyName.equals(DBConstants.RDBMS.FAIR_QUEUE)
            ||propertyName.equals(DBConstants.RDBMS.ABANDON_WHEN_PERCENTAGE_FULL)
            ||propertyName.equals(DBConstants.RDBMS.MAX_AGE)
            ||propertyName.equals(DBConstants.RDBMS.USE_EQUALS)
            ||propertyName.equals(DBConstants.RDBMS.SUSPECT_TIMEOUT)
            ||propertyName.equals(DBConstants.RDBMS.VALIDATION_QUERY_TIMEOUT)
            ||propertyName.equals(DBConstants.RDBMS.ALTERNATE_USERNAME_ALLOWED)
            ||propertyName.equals(DBConstants.RDBMS.DYNAMIC_USER_AUTH_CLASS)
            ||propertyName.equals(DBConstants.RDBMS.DYNAMIC_USER_AUTH_MAPPING)
            ||propertyName.equals(DBConstants.CustomDataSource.DATA_SOURCE_PROPS) 
            ||propertyName.equals(DBConstants.CustomDataSource.DATA_SOURCE_QUERY_CLASS)
            ||propertyName.equals(DBConstants.CustomDataSource.DATA_SOURCE_TABULAR_CLASS)) && !(propertyName.equals(DBConstants.GSpread.DATASOURCE) && useQueryMode)
            && !(propertyName.equals(DBConstants.GSpread.USERNAME) && useQueryMode)){ %>
            <input type="text" size="50" id="<%=propertyName%>" name="<%=propertyName%>"
                               value="<%=propertyValue%>"/>
       <%}%>
        <%
        }%>
    </td>
</tr>
<%

    }
    }
%>
<% if("RDBMS".equals(dataSourceType) || "Cassandra".equals(dataSourceType) ) { %>
<tr> <td class="leftCol-small" style="white-space: nowrap;">
 Expose As OData Service</td> <td> <input type="checkbox" name="isOData" id="isOData" value="isOData" <%= (isODataBool==true ? "checked" : "") %>>
</td></tr>
<%} %>
</table>

<% if (DBConstants.DataSourceTypes.RDBMS.equals(selectedType)) { %>
<table id="advancedTable" class="styledLeft noBorders" cellspacing="0" width="100%">
          <tr>
            <td colspan="2" class="middle-header">
            <a onclick="showAdvancedRDBMSConfigurations()" class="icon-link" style="background-image:url(images/plus.gif);"
                         href="#passwordManager" id="pwdMngrSymbolMax"></a>
                <fmt:message key="org.wso2.ws.dataservice.data.source.configuration.parameters"/></td>
        </tr>

    <tr id="advancedConfigFields" style="display:none">
        <td>
            <table id="advancedConfigFieldsTable" cellspacing="0" width="100%">
                 <%
                     if (configId != null && configId.trim().length() > 0) {
                    Config dsConfig = dataService.getConfig(configId);

                    if (dsConfig == null || (dsConfig !=null && !flag.equals("edit"))) {
                        dsConfig = newConfig;
                    }
                    if (dsConfig != null) {
                        dataSourceType = dsConfig.getDataSourceType();
                        if (dataSourceType == null) {
                            dataSourceType = "";
                        }
                        if (selectedType == null) {
                            selectedType = dataSourceType;
                        }
                        dsConfig = addNotAvailableFunctions(dsConfig, selectedType,request);
                        ArrayList configProperties = dsConfig.getProperties();
                        propertyIterator = configProperties.iterator();

                    }
                     }
                     if (propertyIterator != null) {

                while (propertyIterator.hasNext()) {
                    Property property = (Property) propertyIterator.next();
                    String propertyName = property.getName();
                    String propertyValue = null;
                    if(property.getValue() instanceof String){
                       propertyValue = (String)property.getValue();
                    }
                    if (propertyName.equals(DBConstants.RDBMS.INITIAL_SIZE)
                        ||propertyName.equals(DBConstants.RDBMS.MAX_ACTIVE)
                        ||propertyName.equals(DBConstants.RDBMS.MAX_IDLE)
                        ||propertyName.equals(DBConstants.RDBMS.MIN_IDLE)
                        ||propertyName.equals(DBConstants.RDBMS.MAX_WAIT)
                        ||propertyName.equals(DBConstants.RDBMS.VALIDATION_QUERY)
                        ||propertyName.equals(DBConstants.RDBMS.TIME_BETWEEN_EVICTION_RUNS_MILLIS)
                        ||propertyName.equals(DBConstants.RDBMS.NUM_TESTS_PER_EVICTION_RUN)
                        ||propertyName.equals(DBConstants.RDBMS.MIN_EVICTABLE_IDLE_TIME_MILLIS)
                        ||propertyName.equals(DBConstants.RDBMS.REMOVE_ABANDONED_TIMEOUT)
                        ||propertyName.equals(DBConstants.RDBMS.DEFAULT_CATALOG)
                        ||propertyName.equals(DBConstants.RDBMS.VALIDATOR_CLASSNAME)
                        ||propertyName.equals(DBConstants.RDBMS.CONNECTION_PROPERTIES)
                        ||propertyName.equals(DBConstants.RDBMS.INIT_SQL)
                        ||propertyName.equals(DBConstants.RDBMS.JDBC_INTERCEPTORS)
                        ||propertyName.equals(DBConstants.RDBMS.VALIDATION_INTERVAL)
                        ||propertyName.equals(DBConstants.RDBMS.ABANDON_WHEN_PERCENTAGE_FULL)
                        ||propertyName.equals(DBConstants.RDBMS.MAX_AGE)
                        ||propertyName.equals(DBConstants.RDBMS.VALIDATION_QUERY_TIMEOUT)
                        ||propertyName.equals(DBConstants.RDBMS.SUSPECT_TIMEOUT)) {%>
                        <tr>
                         <td class="leftCol-small" style="white-space: nowrap;"><label><fmt:message key="<%=propertyName%>"/></label></td>
                         <td> <input type="text" size="50" id="<%=propertyName%>" name="<%=propertyName%>"  value="<%=propertyValue%>"/></td>
                            </tr>
                    <%} else if (propertyName.equals(DBConstants.RDBMS.TEST_ON_BORROW)) {%>
                        <tr>
                            <td class="leftCol-small" style="white-space: nowrap;"><label><fmt:message key="<%=propertyName%>"/></label></td>
                            <td>
                         <select id="<%=propertyName%>" name="<%=propertyName%>">
                           <% if (propertyValue.equals("") || propertyValue.equals("true")) { %>
                           <option value="true" selected="selected">true</option>
                           <% } else { %>
                           <option value="true">true</option>
                           <% } %>

                           <% if (propertyValue.equals("false")) { %>
                           <option value="false" selected="selected">false</option>
                           <% } else { %>
                           <option value="false">false</option>
                           <% } %>
                           </select>
                            </td>
                            </tr>
                   <%} else if (propertyName.equals(DBConstants.RDBMS.TEST_ON_RETURN)
                           || propertyName.equals(DBConstants.RDBMS.LOG_ABANDONED)
                           || propertyName.equals(DBConstants.RDBMS.TEST_WHILE_IDLE)
                           || propertyName.equals(DBConstants.RDBMS.REMOVE_ABANDONED)
                           ||propertyName.equals(DBConstants.RDBMS.AUTO_COMMIT)
                           ||propertyName.equals(DBConstants.RDBMS.DEFAULT_READONLY)
                           ||propertyName.equals(DBConstants.RDBMS.JMX_ENABLED)
                           ||propertyName.equals(DBConstants.RDBMS.FAIR_QUEUE)
                           ||propertyName.equals(DBConstants.RDBMS.ALTERNATE_USERNAME_ALLOWED)
                           ||propertyName.equals(DBConstants.RDBMS.USE_EQUALS)) {%>
                        <tr>
                            <td class="leftCol-small" style="white-space: nowrap;"><label><fmt:message key="<%=propertyName%>"/></label></td>
                            <td>
                      <select id="<%=propertyName%>" name="<%=propertyName%>">
                        <% if ( propertyValue.equals("true")) { %>
                        <option value="true" selected="selected">true</option>
                        <% } else { %>
                        <option value="true">true</option>
                        <% } %>
                        <% if (propertyValue.equals("") || propertyValue.equals("false")) { %>
                        <option value="false" selected="selected">false</option>
                        <% } else { %>
                        <option value="false">false</option>
                        <% } %>
                        </select>
                            </td>
                            </tr>
                    <%} else if ( propertyName.equals(DBConstants.RDBMS.DEFAULT_TX_ISOLATION)) {
                    %>
                        <tr>
                            <td class="leftCol-small" style="white-space: nowrap;"><label><fmt:message key="<%=propertyName%>"/></label></td>
                            <td>
                       <select id="<%=propertyName%>" name="<%=propertyName%>">
                        <% if ("TRANSACTION_UNKNOWN".equals(propertyValue) || propertyValue.equals("")) {%>
                        <option value="TRANSACTION_UNKNOWN" selected="true">TRANSACTION_UNKNOWN</option>
                        <% } else {%>
                        <option value="TRANSACTION_UNKNOWN">TRANSACTION_UNKNOWN</option>
                        <%} %>
                        <% if ("TRANSACTION_NONE".equals(propertyValue)) {%>
                        <option value="TRANSACTION_NONE" selected="true">TRANSACTION_NONE</option>
                        <% } else {%>
                        <option value="TRANSACTION_NONE">TRANSACTION_NONE</option>
                        <%} %>
                        <% if ("TRANSACTION_READ_COMMITTED".equals(propertyValue)) {%>
                        <option value="TRANSACTION_READ_COMMITTED" selected="true">TRANSACTION_READ_COMMITTED</option>
                        <% } else {%>
                        <option value="TRANSACTION_READ_COMMITTED">TRANSACTION_READ_COMMITTED</option>
                        <%} %>
                        <% if ("TRANSACTION_READ_UNCOMMITTED".equals(propertyValue)) {%>
                        <option value="TRANSACTION_READ_UNCOMMITTED" selected="true">TRANSACTION_READ_UNCOMMITTED</option>
                        <% } else {%>
                        <option value="TRANSACTION_READ_UNCOMMITTED">TRANSACTION_READ_UNCOMMITTED</option>
                        <%} %>
                        <% if ("TRANSACTION_REPEATABLE_READ".equals(propertyValue)) {%>
                        <option value="TRANSACTION_REPEATABLE_READ" selected="true">TRANSACTION_REPEATABLE_READ</option>
                        <% } else {%>
                        <option value="TRANSACTION_REPEATABLE_READ">TRANSACTION_REPEATABLE_READ</option>
                        <%} %>
                        <% if ("TRANSACTION_SERIALIZABLE".equals(propertyValue)) {%>
                        <option value="TRANSACTION_SERIALIZABLE" selected="true">TRANSACTION_SERIALIZABLE</option>
                        <% } else {%>
                        <option value="TRANSACTION_SERIALIZABLE">TRANSACTION_SERIALIZABLE</option>
                        <%} %>
                      </select>
                            </td>
                            </tr>
                  <%}%>
                <%
                }
                }
                %>
                </table>
            </td>
        </tr>
</table>

<table id="dynamicUserAuthenticationTable" class="styledLeft noBorders" cellspacing="0" width="100%">
    <tr>
        <td colspan="2" class="middle-header">
            <a onclick="showDynamicUserAuthenticationConfigurations()" class="icon-link" style="background-image:url(images/plus.gif);"
               href="#symbolMax" id="symbolMax"></a>
            <fmt:message key="dynamic.user.authentication.configuration.parameters"/>
        </td>
    </tr>
    <tr id="dynamicUserAuthenticationFields" style="display:none">
        <td>
            <table id="dynamicUserAuthenticationFieldsTable" cellspacing="0" width="100%">
                <%
                    if (configId != null && configId.trim().length() > 0) {
                        Config dsConfig = dataService.getConfig(configId);

                        if (dsConfig == null || (dsConfig != null && !flag.equals("edit"))) {
                            dsConfig = newConfig;
                        }
                        if (dsConfig != null) {
                            dataSourceType = dsConfig.getDataSourceType();
                            if (dataSourceType == null) {
                                dataSourceType = "";
                            }
                            if (selectedType == null) {
                                selectedType = dsConfig.getDataSourceType();
                            }
                            dsConfig = addNotAvailableFunctions(dsConfig, selectedType, request);
                            ArrayList configProperties = dsConfig.getProperties();
                            propertyIterator = configProperties.iterator();

                        }
                    }
                    if (propertyIterator != null) {
                        DynamicAuthConfiguration dynamicAuthConfiguration;
                        List<DynamicAuthConfiguration.Entry> dynamicUserEntries = new ArrayList<DynamicAuthConfiguration.Entry>();
                        while (propertyIterator.hasNext()) {
                            Property property = (Property) propertyIterator.next();
                            String propertyName = property.getName();
                            Object propertyValue = property.getValue();
                            if (property.getValue() instanceof String) {
                                if (propertyName.equals(DBConstants.RDBMS.DYNAMIC_USER_AUTH_CLASS)) {
                                    dynamicUserAuthClass = (String)propertyValue;
                                }
                            } else if (property.getValue() instanceof DynamicAuthConfiguration) {
                                if (propertyName.equals(DBConstants.RDBMS.DYNAMIC_USER_AUTH_MAPPING)) {
                                    dynamicAuthConfiguration = (DynamicAuthConfiguration)propertyValue;
                                    dynamicUserEntries = dynamicAuthConfiguration.getEntries();
                                }
                            }
                        }
                %>
                <table id="runtimeUserMapping"  >
                    <tr>
                        <td><label><fmt:message key="dynamic.user.authentication.class"/></label></td>
                        <td><input type="text" name="dynamicUserAuthClass" id="dynamicUserAuthClass" size="50" value="<%=dynamicUserAuthClass%>"/></td>
                    </tr>
                </table>

                <table id="staticUserMapping" border="0" style="border-width: 1px; border-color:#000000; border-style: solid;">
                    <tr>
                        <% if (dynamicUserEntries != null) {  //edit %>
                        <td colspan="2"><a class="icon-link"
                                           style="background-image:url(../admin/images/add.gif);"
                                           onclick=" addStaticUserAuthFields(document,(document.getElementById('staticUserMappingsCount').value == 0 && <%=dynamicUserEntries.size() != 0 %>) ? <%=dynamicUserEntries.size()%> : document.getElementById('staticUserMappingsCount').value);">
                            <fmt:message key="add.new.dynamic.user.authentication"/></a></td>

                        <%} else {%>
                        <td colspan="2"><a class="icon-link"
                                           style="background-image:url(../admin/images/add.gif);"
                                           onclick=" addStaticUserAuthFields(document,document.getElementById('staticUserMappingsCount').value);">
                            <fmt:message key="add.new.dynamic.user.authentication"/></a></td>
                        <% } %>
                    </tr>
                    <%  DynamicAuthConfiguration.Entry userEntry;
                        if (dynamicUserEntries != null) {
                        for (int i = 0; i < dynamicUserEntries.size(); i++) {
                            userEntry = dynamicUserEntries.get(i);
                            String carbonUsername = userEntry.getRequest();
                            String dbUsername = userEntry.getUsername();
                            String dbPwd = userEntry.getPassword();  %>

                    <tr id="carbonUsernameRaw<%=i%>">
                        <td><label>Carbon Username</label></td>
                        <td><input type="text" name="carbonUsernameRaw<%=i%>" id="carbonUsernameRaw<%=i%>" size="15" value="<%=carbonUsername%>"/></td>

                        <td><label>DB Username</label></td>
                        <td><input type="text" name="dbUsernameRaw<%=i%>" id="dbUsernameRaw<%=i%>" size="15" value="<%=dbUsername%>"/></td>

                        <td><label>DB User Password</label></td>
                        <td>
                            <input type="password" name="dbPwdRaw<%=i%>" id="dbPwdRaw<%=i%>" size="15" value="<%=dbPwd%>"/>
                        </td>
                        <td><a class="icon-link" style="background-image:url(../admin/images/delete.gif);"
                               href="javascript:deleteUserField('<%=i%>')"> <fmt:message key="delete"/></a></td>
                    </tr>

                    <%  }
                    }%>
                </table>
            </table>
        </td>
    </tr>
    <input type="hidden" id="staticUserMappingsCount" name="staticUserMappingsCount" value="<%=(dynamicUserEntries != null) ? dynamicUserEntries.size() : 0%>"/>
</table>
<%
    }   }
%>

<table id="buttonTable" class="styledLeft noBorders" cellspacing="0" width="100%">
<tr>
    <td class="buttonRow" colspan="2">
        <%if ("RDBMS".equals(dataSourceType)) {%>
         <div id="connectionTestMsgDiv" style="display: none;"></div>
        <input class="button" type="button" value="<fmt:message key="datasource.test.connection"/>"
               onclick="testConnection();return false;"/>
        <script type="text/javascript">
            function displayMsg(msg) {
            	var successMsg  =  new RegExp("^Database connection is successfull with driver class");
            	if (msg.search(successMsg)==-1) //if match failed
            	{
            		CARBON.showErrorDialog(msg);
            	} else {
            		CARBON.showInfoDialog(msg);
            	}

            }

            function testConnection() {
                var driver = document.getElementById('<%=RDBMS.DRIVER_CLASSNAME%>').value;
                var jdbcUrl = document.getElementById('<%=RDBMS.URL%>').value;
                var userName = document.getElementById('<%=RDBMS.USERNAME%>').value;
                var password = document.getElementById('<%=RDBMS.PASSWORD%>').value;


                var useAlias = document.getElementById('useSecretAliasValue').value;
                if (useAlias == 'true') {
                	if (document.getElementById('pwdalias') != null) {
                		var pwdalias = document.getElementById('pwdalias').value;
                	}
                    var url = 'connection_test_ajaxprocessor.jsp?driver=' + encodeURIComponent(driver) + '&jdbcUrl=' + encodeURIComponent(jdbcUrl) + '&userName=' + encodeURIComponent(userName) + '&password=' + encodeURIComponent(password) + '&passwordAlias=' +pwdalias ;
                } else {
                	var url = 'connection_test_ajaxprocessor.jsp?driver=' + encodeURIComponent(driver) + '&jdbcUrl=' + encodeURIComponent(jdbcUrl) + '&userName=' + encodeURIComponent(userName) + '&password=' + encodeURIComponent(password);
                }
                jQuery('#connectionTestMsgDiv').load(url, displayMsg);
                return false;
            }
        </script>

        <%} else if ("GDATA_SPREADSHEET".equals(dataSourceType)) {%>
        <div id="spreadsheetConnectionTestMsgDiv" style="display: none;"></div>
        <input class="button" type="button" value="<fmt:message key="datasource.test.connection"/>"
               onclick="testSpreadsheetConnection();return false;"/>
        <script type="text/javascript">
            function displayMsg4GoogleSpreadsheet(msg) {
            	var successMsg  =  new RegExp("^Google spreadsheet connection is successfull");
            	if (msg.search(successMsg)==-1) //if match failed
            	{
            		CARBON.showErrorDialog(msg);
            	} else {
            		CARBON.showInfoDialog(msg);
            	}
            }

            function testSpreadsheetConnection() {
                var documentURL = document.getElementById('gspread_datasource').value;
                var visibility =  document.getElementById("gspread_visibility").options[document.getElementById("gspread_visibility").selectedIndex].value;
                var userName = leftTrim(rightTrim(document.getElementById('gspread_username').value));
                var password = document.getElementById('gspread_password').value;
                var useAlias = document.getElementById('useSecretAliasValue').value;
                                
                if (useAlias == 'true') {
               	if (document.getElementById('pwdalias') != null) {
                		var pwdalias = document.getElementById('pwdalias').value;
                	}
                	var url = 'connection_gspreadtest_ajaxprocessor.jsp?userName=' + encodeURIComponent(userName) + '&password=' + encodeURIComponent(password) + '&visibility=' + encodeURIComponent(visibility) + '&documentURL=' + encodeURIComponent(documentURL)+ '&passwordAlias=' + passwordAlias;
                } else {
                	var url = 'connection_gspreadtest_ajaxprocessor.jsp?userName=' + encodeURIComponent(userName) + '&password=' + encodeURIComponent(password) + '&visibility=' + encodeURIComponent(visibility) + '&documentURL=' + encodeURIComponent(documentURL);
                }
                jQuery('#spreadsheetConnectionTestMsgDiv').load(url, displayMsg4GoogleSpreadsheet);
                return false;
            }

            function rightTrim(str){
                for(var i = str.length - 1; i >= 0 && (str.charAt(i) == ' '); i--){
                    str = str.substring(0, i);
                }
                return str;
            }

            function leftTrim(str) {
               for(var i = 0; i >= 0 && (str.charAt(i) == ' '); i++){
                    str = str.substring(i + 1, str.length);
                }
                return str;
            }
            
        </script>

  <%} %>
        <input class="button" name="save_button" type="submit" onclick="return validateAddDataSourceForm();" value="<fmt:message key="save"/>"/>
        <input class="button" name="cancel_button" type="submit" value="<fmt:message key="cancel"/>"/>
    </td>
</tr>
</table>
</form>
</div>
</fmt:bundle>
