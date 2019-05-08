/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function validateServiceDetailsForm(){
    var serviceName = document.getElementById('serviceName').value;
    if(serviceName == ''){
        CARBON.showWarningDialog("Data Service Name is mandatory");
        return false;
    }
    var  regex = /[~!@#$%^&*()+=\:;<>'"?[\]{}|\s,]/;
    // Check for invalid characters
    if (regex.test(serviceName)) {
        CARBON.showWarningDialog("Only alphanumeric characters and underscores are allowed in the data service name");
        return false;
    }
    //Check for available dataservices
    var url = 'dataservice_available_ajaxprocessor.jsp?dataservice=' + encodeURIComponent(serviceName);
    if (!editingDataService) {
        var available = false;
        $.ajax({
            type: 'GET',
            url: url,
            async: false,
            cache: false,
            timeout: 30000,
            complete: function(r) {
                var successMsg = new RegExp("^true");
                if (r.responseText.search(successMsg) == 0) {
                    available = true;
                }

            }
        });

        if (available) {
            CARBON.showWarningDialog("Data Service Name already exists. Please choose a different data service name");
            return false;
        }
    }
    return true;
}

    function isValidXml() {
        var source = editAreaLoader.getValue("dsConfig");
        if (window.ActiveXObject) {
            try {
                var doc = new ActiveXObject("Microsoft.XMLDOM");
                doc.async = "false";
                var hasParse = doc.loadXML(source);
                if (!hasParse) {
                    CARBON.showErrorDialog('Invalid Configuration');
                    return false;
                }
            } catch (e) {
                CARBON.showErrorDialog('Invalid Configuration');
                return false;
            }
        } else {
            var parser = new DOMParser();
            var doc = parser.parseFromString(source, "text/xml");
            var message = "";
            for (i = 0; i < doc.documentElement.childElementCount; i++) {
                if (doc.documentElement.childNodes[i].nodeName == "parsererror") {
                    message = message + "\n" + doc.documentElement.childNodes[i].childNodes[1].innerHTML;
                } else {
                    for (j = 0; j < doc.documentElement.childNodes[i].childElementCount; j++) {
                        if (doc.documentElement.childNodes[i].childNodes[j].nodeName == "parsererror") {
                            message = message + "\n" + doc.documentElement.childNodes[i].childNodes[j].childNodes[1].innerHTML;
                        }
                    }
                }
            }
            if (message != "") {
                CARBON.showErrorDialog('Invalid Configuration :' + message);
                return false;
            }
        }
        return true;
    }

function validateDSGenerator(){
    var serviceName = document.getElementById('txtServiceName').value;
    var multipleMode = document.getElementById("mode");
    if(serviceName == '' && getCheckedValue(multipleMode) == "Single"){
        CARBON.showWarningDialog("Data Service Name is mandatory");
        return false;
    }
    return true;
}

function trim(text) {
    return text.replace(/^\s*/, "").replace(/\s*$/, "");
}


function validateSQLDialectForm(){
	var sqlDialect = document.getElementById('txSQLDialect').value;
	var sql = document.getElementById('txtSQL').value;
	if (sqlDialect == '') {
	    CARBON.showWarningDialog("Specify Supported Driver names");
	    return false;
	} if (sql == '' || trim(sql) == '') {
	    CARBON.showWarningDialog("Specify SQL query");
	    return false;
	} else {
        return validateQuery(sql);
    }
	return true;
}

function validateDatabaseSelection(){
    var datasource = document.getElementById("datasource").options[document.getElementById("datasource").selectedIndex].value;

    if(datasource == '') {
        CARBON.showWarningDialog("Carbon Datasource is mandatory");
        return false;
    }
    var db = document.getElementById('dbName').value;
    if(db == ''){
        CARBON.showWarningDialog("Database Name is mandatory");
        return false;
    }

    return true;
}

function validateTableSelection(obj){
	var tableSet=obj.split(":");
	var index=0;
	while (index < tableSet.length)
	 {
	  if ( document.getElementById(tableSet[index]) != null) {
		  if(document.getElementById(tableSet[index]).checked) {
			  return true;
		  }
	  }
	  index+=1;
	 }
   CARBON.showWarningDialog("Atleast one table needs to be selected to proceed");
   return false;
}


function validateDataSourcesForm(){

    return true;
}


function validateAddDataSourceForm(){
    if(document.getElementById('datasourceId').value == ''){
        //CARBON.showErrorDialog("Data Service Name is mandatory");
        CARBON.showWarningDialog('Datasource Id is mandatory');
        return false;
    }
    if(document.getElementById('datasourceType').value == ''){
        CARBON.showWarningDialog('Select the datasource type');
        return false;
    }
    if(document.getElementById('datasourceType').value == 'RDBMS'){
        if(document.getElementById('databaseEngine').value == '#'){
            CARBON.showWarningDialog('Select the Database engine');
            return false;
        }
        if(document.getElementById('driverClassName').value ==  ''){
            CARBON.showWarningDialog('Database Driver is mandatory');
            return false;
        }
        if(document.getElementById('url').value ==  ''){
            CARBON.showWarningDialog('JDBC URL is mandatory');
            return false;
        }
        if(!ValidateDataSourceProperties()) {
        	return false;
        }

    }else if(document.getElementById('datasourceType').value == 'EXCEL'){
        if(document.getElementById('excel_datasource').value == ''){
            CARBON.showWarningDialog('Excel File Location is mandatory');
            return false;
        }
        //File extension check
        var filePath = document.getElementById('excel_datasource').value;
        var fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1);
        var fileExtensionLower = fileExtension.toLowerCase();
        if (!(fileExtensionLower == 'xls' || fileExtensionLower == 'xlsx')){
        	CARBON.showWarningDialog('Invalid File Type');
        	return false;
        }
    }else if(document.getElementById('datasourceType').value == 'RDF'){
        if(document.getElementById('rdf_datasource').value == ''){
            CARBON.showWarningDialog('RDF File Location is mandatory');
            return false;
        }
    }else if(document.getElementById('datasourceType').value == 'SPARQL'){
        if(document.getElementById('sparql_datasource').value == ''){
            CARBON.showWarningDialog('Sparql Endpoint URI is mandatory');
            return false;
        }
    }else if(document.getElementById('datasourceType').value == 'MongoDB'){
        if(document.getElementById('mongoDB_servers').value == ''){
            CARBON.showWarningDialog('MongoDB Server is mandatory');
            return false;
        }
        if(document.getElementById('mongoDB_database').value == ''){
            CARBON.showWarningDialog('MongoDB Database Name is mandatory');
            return false;
        }
    }else if(document.getElementById('datasourceType').value == 'Cassandra'){
        if(document.getElementById('cassandraServers').value == ''){
            CARBON.showWarningDialog('Cassandra Server is mandatory');
            return false;
        }
    }else if(document.getElementById('datasourceType').value == 'CSV'){
        if(document.getElementById('csv_datasource').value == ''){
            CARBON.showWarningDialog('CSV File Location is mandatory');
            return false;
        }
        //File extension check
        var filePath = document.getElementById('csv_datasource').value;
        var fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1);
        var fileExtensionLower = fileExtension.toLowerCase();
        if (!(fileExtensionLower == 'csv' || fileExtensionLower == 'txt')){
            CARBON.showWarningDialog('Invalid File Type');
            return false;
        }
        if(document.getElementById('csv_hasheader').value == ''){
            CARBON.showWarningDialog('Contains Column Header Row is mandatory');
            return false;
        }
        if (document.getElementById('csv_hasheader').value == 'true') {
            if (document.getElementById('csv_headerrow').value == '') {
                CARBON.showWarningDialog('Enter value of the header row');
                return false;
            }
            if (document.getElementById('csv_headerrow').value.match(/^[a-zA-Z]+$/)) {
                CARBON.showWarningDialog('Enter numeric values to header row');
                return false;
            }
            if (!document.getElementById('csv_headerrow').value.match(/^\s*[1-9]\d*\s*$/)) {
                CARBON.showWarningDialog('Enter positive numeric value greater than zero to header row');
                return false;
            }
            if ( document.getElementById('csv_headerrow').value > 2147483647) {
                CARBON.showWarningDialog('Entered header row value exceeds the allowed maximum value');
                return false;
            }
        }
    }else if(document.getElementById('datasourceType').value == 'JNDI'){
       if(document.getElementById('jndi_resource_name').value == ''){
            CARBON.showWarningDialog('Resource Name is mandatory');
            return false;
        }
    } else if (document.getElementById('datasourceType').value == 'WEB_CONFIG') {
    	if (document.getElementById('config').checked) {
    		if(document.getElementById('web_harvest_config_textArea').value ==  ''){
                CARBON.showWarningDialog('Please Specify Web Harvest Config');
                return false;
            } else if ((document.getElementById('web_harvest_config_textArea').value.trim()).indexOf('<config>') != 0) {
            	CARBON.showWarningDialog('Invalid Web Harvest Config');
                return false;
            }
    	} else {
    		if(document.getElementById('web_harvest_config').value ==  ''){
                CARBON.showWarningDialog('Please Specify Web Harvest Config File Path');
                return false;
            }
    	}
     }

    return true;
}

function validateQueryId(obj){
	var queryId = document.getElementById('queryId').value;
       var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    if(queryId == ''){
         CARBON.showWarningDialog('Query Id is mandatory');
         return  false;
    }
    // Validate for alphanumeric characters and underscores
    if (!reWhiteSpace.test(queryId)) {
        CARBON.showWarningDialog("Alphanumeric characters and underscores are only allowed in the Query Id");
        return false;
    }
    return true;

  }

function validateFieldsForEvents(obj){
    var queryId = document.getElementById('queryId').value;
    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    var dataSourceId = document.getElementById('datasource').value;
    if(queryId == ''){
        CARBON.showWarningDialog('Query Id is mandatory');
        return  false;
    }
    // Validate for alphanumeric characters and underscores
    if (!reWhiteSpace.test(queryId)) {
        CARBON.showWarningDialog("Alphanumeric characters and underscores are only allowed in the Query Id");
        return false;
    }
    if(dataSourceId == '#'){
        CARBON.showWarningDialog('Select the datasource');
        return  false;
    }
    return true;
}

function validateClickOnReturnGeneratedKeys() {
    var query = document.getElementById('sql').value;
    if (query != '') {
        var startingKeyword = query.trim().toUpperCase().toString().split(" ");
        if (startingKeyword[0] != "INSERT") {
            CARBON.showWarningDialog("Return Generated Keys cannot be used with the given query");
            return false;
        }
    } else {
        CARBON.showWarningDialog("Query cannot be empty");
        return false;
    }
    return true;
}

function validateClickOnReturnUpdatedRowCount() {
    var query = document.getElementById('sql').value;
    if ('' == query) {
        CARBON.showWarningDialog("Query cannot be empty");
        return false;
    } else {
        return true;
    }
}

function validateAddQueryFormSave(obj) {
	var queryId = document.getElementById('queryId').value;
	var dataSourceId = document.getElementById('datasource').value;
    if(queryId == ''){
        CARBON.showWarningDialog('Query Id is mandatory');
        return  false;
    }
    if(dataSourceId == '#'){
        CARBON.showWarningDialog('Select the datasource');
        return  false;
    }

    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Validate for alphanumeric characters and underscores
    if (!reWhiteSpace.test(queryId)) {
        CARBON.showWarningDialog("Alphanumeric characters and underscores are only allowed in the Query Id");
        document.getElementById('queryId').readOnly = false;
        return false;
    }

    if(document.getElementById('RDFRow').style.display == '') {
        var value = document.getElementById('sparql').value;
        if(value == '') {
            CARBON.showWarningDialog('Sparql is mandatory');
            return false;
        } else {
            return validateQuery(value);
        }
    }

    if(document.getElementById('RDBMSnJNDIRow').style.display == ''){
        var value = document.getElementById('sql').value;
        if(value == '') {
            CARBON.showWarningDialog('SQL is mandatory');
            return false;
        } else {
            return validateQuery(value);
        }
    }

    if (document.getElementById('CASSANDRARow').style.display == '') {
        var value = document.getElementById('cassandraExpression').value;
        if (value == '') {
            CARBON.showWarningDialog('Expression is mandatory');
            return false;
        } else {
            return validateQuery(value);
        }
    }

    if (document.getElementById('MongoDBQueryRow').style.display == '') {
        var value = document.getElementById('mongoExpression').value;
        if (value == '') {
            CARBON.showWarningDialog('Expression is mandatory');
            return false;
        } else {
            return validateQuery(value);
        }
    }

    if (document.getElementById(dataSourceId).value == 'EXCEL') {
        if (document.getElementById('txtExcelWorkbookName').value == '') {
            CARBON.showWarningDialog('Enter value to Workbook Name');
            return false;
        }
        if (document.getElementById('txtExcelStartingRow').value == '') {
            CARBON.showWarningDialog('Enter value to Start reading from');
            return false;
        }
        if (document.getElementById('txtExcelHeaderRow').value == '') {
            CARBON.showWarningDialog('Enter value of the header row');
            return false;
        }
        if (document.getElementById('txtExcelMaxRowCount').value == '') {
            CARBON.showWarningDialog('Enter value to Rows to read');
            return false;
        }
        if (document.getElementById('txtExcelStartingRow').value.match(/^[a-zA-Z]+$/)) {
            CARBON.showWarningDialog('Enter numeric values to Start reading from');
            return false;
        }
        if (!document.getElementById('txtExcelStartingRow').value.match(/^\s*[1-9]\d*\s*$/)) {
            CARBON.showWarningDialog('Enter positive numeric value greater than zero to Start reading from');
            return false;
        }
        if ( document.getElementById('txtExcelStartingRow').value > 2147483647) {
        	CARBON.showWarningDialog('Entered Start reading from value exceeds the allowed maximum value');
            return false;
        }
        if (document.getElementById('txtExcelHeaderRow').value.match(/^[a-zA-Z]+$/)) {
            CARBON.showWarningDialog('Enter numeric values to header row');
            return false;
        }
        if (!document.getElementById('txtExcelHeaderRow').value.match(/^\s*[1-9]\d*\s*$/)) {
            CARBON.showWarningDialog('Enter positive numeric value greater than zero to header row');
            return false;
        }
        if ( document.getElementById('txtExcelHeaderRow').value > 2147483647) {
            CARBON.showWarningDialog('Entered header row value exceeds the allowed maximum value');
            return false;
        }
        if (document.getElementById('txtExcelMaxRowCount').value.match(/^[a-zA-Z]+$/)) {
            CARBON.showWarningDialog('Enter numeric values to Rows to read');
            return false;
        }
    }

    if (document.getElementById('timeout').value != null) {
        var timeout = document.getElementById('timeout').value;
        if(isNaN(timeout)){
            CARBON.showWarningDialog("Timeout " + "'" +timeout + "'" + " should be a numeric value");
            return  false;
        }
    }


    if (document.getElementById('fetchSize').value != null) {
        var fetchSize = document.getElementById('fetchSize').value;
        if(isNaN(fetchSize))
        {
            CARBON.showWarningDialog("Fetch size "+ "'" + fetchSize + "'" + " should be a numeric value");
            return  false;
        }
    }

    if(document.getElementById('maxFieldSize').value != null) {
        var maxFieldSize = document.getElementById('maxFieldSize').value;
        if(isNaN(maxFieldSize))
        {
            CARBON.showWarningDialog("Max field size "+ "'" + maxFieldSize + "'" + " should be a numeric value");
            return  false;
        }
    }

    if(document.getElementById('maxRows').value != null) {
        var maxRows = document.getElementById('maxRows').value;
        if(isNaN(maxRows))
        {
            CARBON.showWarningDialog("Max Rows "+ "'" +maxRows + "'" + " should be a numeric value");
            return  false;
        }
    }

    if(document.getElementById('noOutputmappings') != null )  {
        if((document.getElementById('outputTypeId').value == 'xml') && (document.getElementById('txtDataServiceWrapElement').value != '' ||  document.getElementById('txtDataServiceRowName').value != '' )){
            CARBON.showWarningDialog('Can not insert result elements without Output Mappings. Insert Output Mappings to proceed.');
            return  false;
        }
    }

    var status = true;

    if (document.getElementById('outputTypeId').value == 'json') {
    	var data = document.getElementById('jsonMapping').value;
    	$.ajax({
	        url: '../ds/json_mapping_validate_ajaxprocessor.jsp',
	        type: 'POST',
	        async: false,
	        cache: false,
	        data: data,
	        processData: false,
	        timeout: 5000,
	        error: function() {
	            status = true;
	        },
	        contentType: 'application/json',
	        success: function(msg) {
	        	msg = msg.toString().trim();
	            if (msg.length == 0){
	                status = true;
	            } else {
	            	CARBON.showWarningDialog(msg);
	                status = false;
	            }
	        }
	    });
    }

    return status;
}

function validateManageXADSForm(){
	var xaDatasourceId = document.getElementById('xaId').value;
	var txXADatasourceClass = document.getElementById('txXADatasourceClass').value;
	if(xaDatasourceId == ''){
        CARBON.showWarningDialog('XA Datasource Id is mandatory');
        return false;
    }
	if(txXADatasourceClass == ''){
        CARBON.showWarningDialog('XA Datasource Class is mandatory');
        return false;
    }
    return true;
}

function validateAddOperationForm(){
    var operationName = document.getElementById('operationName').value;
    if(document.getElementById('operationName').value == ''){
        CARBON.showWarningDialog('Operation name is mandatory');
        return false;
    }
    if(document.getElementById('queryId').value == ''){
        CARBON.showWarningDialog('Select the query Id');
        return false;
    }
    var  reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(operationName)) {
        CARBON.showWarningDialog("Alphanumeric characters and underscores are only allowed in the operation name");
        return false;
    }
    return true;
}

function validateQueriesForm(){
    return true;
}

function validateOperationsForm(){
    return true;
}

function validateResourcesForm(){
    return true;
}

function validateAddResourceForm(){
    var queryId = document.getElementById('queryId');
    if (queryId != null) {
        var query = queryId.value;
    }
    if(document.getElementById('resourcePath').value == ''){
        CARBON.showWarningDialog('Resource Path is mandatory');
        return false;
    }
    if(document.getElementById('resourceMethod').value == ''){
        CARBON.showWarningDialog('Select the Resource Method');
        return false;
    }
    if(query == ''){
        CARBON.showWarningDialog('Select the query Id');
        return false;
    }
    return true;
}

function sendInputPage() {
    var datasourceType = document.getElementById('datasource').value;
    var queryId = document.getElementById('queryId').value;
    var sql = document.getElementById('sql').value;
    location.href = 'addInputMapping.jsp?data_source=' + datasourceType + '&queryId=' + queryId + '&sql_stat=' + sql;
}

function sendSparqlInputPage() {
    var datasourceType = document.getElementById('datasource').value;
    var queryId = document.getElementById('queryId').value;
    var sql = document.getElementById('sql').value;
    location.href = 'addSparqlInputMapping.jsp?data_source=' + datasourceType + '&queryId=' + queryId + '&sql_stat=' + sql;
}
function sendOutputMapping() {
    var datasourceType = document.getElementById('datasource').value;
    var queryId = document.getElementById('queryId').value;
    var sql = document.getElementById('sql').value;
    var element = document.getElementById('txtDataServiceWrapElement').value;
    var rowName = document.getElementById('txtDataServiceRowName').value;
    var outputType = document.getElementById('outputType').value;
    var rdfBaseURI = document.getElementById('txtrdfBaseURI').value;
    var nameSpace = document.getElementById('txtDataServiceRowNamespace').value;

    location.href = 'addOutputMapping.jsp?data_source=' + datasourceType + '&query_id=' + queryId + '&sql_stat=' + sql + '&element=' + element + '&rowName=' + rowName +  '&outputType=' + outputType +  '&rdfBaseURI=' + rdfBaseURI +'&ns=' + nameSpace;
}
function showTables(obj, document) {
    var configId = obj[obj.selectedIndex].value;
    var datasourceType = '';
    var customDataSourceType = '';
    if (configId != '#') {
        var datasourceTypeObj = document.getElementById(configId);
        if (datasourceTypeObj != null) {
            datasourceType = datasourceTypeObj.value;
        }
        var customDataSourceTypeObj = document.getElementById("customDatasourceType"+configId);
        if (customDataSourceTypeObj != null) {
        	customDataSourceType = customDataSourceTypeObj.value;
        }
    }

    var rdbmsNjndi = 'none';
    var inputMappings = 'none';
    var inputMappingsButton = 'none';
    var sparql = 'none';
    var queryProp = 'none';
    var excel = 'none';
    var gspread = 'none';
    var rdf = 'none';
    var webConfig = 'none';
    var propTable = 'none';
    var inputHeading = 'none';
    var autoResponse = 'none';
    var cassandra = 'none';
    var CustomQuery = 'none';
    var mongoDB = 'none';
    var csv = '';

    if (datasourceType == 'RDBMS' || datasourceType == 'JNDI' || datasourceType == 'CARBON_DATASOURCE') {
        rdbmsNjndi = '';
        inputMappings = '';
        inputMappingsButton = '';
        queryProp = '';
        inputHeading = '';
        autoResponse ='';
    }
    if (datasourceType == 'CUSTOM') {
    	if (customDataSourceType == 'CUSTOM_QUERY' || customDataSourceType == 'DS_CUSTOM_QUERY') {
    		CustomQuery = '';
            inputMappings = '';
            inputMappingsButton = '';
            queryProp = '';
            inputHeading = '';
            autoResponse ='';
    	} else {
    		rdbmsNjndi = '';
            inputMappings = '';
            inputMappingsButton = '';
            queryProp = '';
            inputHeading = '';
            autoResponse ='';
    	}
    }
    if (datasourceType == 'MongoDB') {
        mongoDB = '';
        inputMappings = '';
        inputMappingsButton = '';
        queryProp = '';
        inputHeading = '';
        autoResponse ='';
    }
    if (datasourceType == 'Cassandra') {
        cassandra = '';
        inputMappings = '';
        inputMappingsButton = '';
        queryProp = '';
        inputHeading = '';
        autoResponse ='';
    }
    if (datasourceType == 'RDF' || datasourceType == 'SPARQL') {
        rdf = '';
        inputMappings = '';
        sparql = '';
        inputHeading = '';
    }
    if (datasourceType == 'EXCEL') {
        excel = '';
    }
    if (datasourceType == 'GDATA_SPREADSHEET') {
        gspread = '';
    }
    if(datasourceType == 'WEB_CONFIG') {
        webConfig = '';
    }
    if (datasourceType == 'CSV') {
        csv = 'none';
    }

    document.getElementById('RDBMSnJNDIRow').style.display = rdbmsNjndi;
    /*document.getElementById('advancedQueryConfigs').style.display = rdbmsNjndi;*/
    document.getElementById('InputMappingRow').style.display = inputMappings;
    document.getElementById('InputMappingButtonRow').style.display = inputMappingsButton;
    document.getElementById('SparqlInputMappingButtonRow').style.display = sparql;
    document.getElementById('addQueryProperties').style.display = queryProp;
    document.getElementById('ExcelRow').style.display = excel;
    document.getElementById('GSpreadRow').style.display = gspread;
    document.getElementById('RDFRow').style.display = rdf;
    document.getElementById('propertyTable').style.display = propTable;
    document.getElementById('inputHeading').style.display = inputHeading;
    document.getElementById('scraperRow').style.display = webConfig;
    document.getElementById('CASSANDRARow').style.display = cassandra;
    document.getElementById('CustomQueryRow').style.display = CustomQuery;
    document.getElementById('MongoDBQueryRow').style.display = mongoDB;
    document.getElementById('returnGeneratedKeysRow').style.display = csv;
    //document.getElementById('autoResponseRow').style.display = autoResponse;

}

function setJDBCValues(obj, document) {
    var selectedValue = obj[obj.selectedIndex].value;
    var jdbcUrl = selectedValue.substring(0, selectedValue.indexOf("#"));
    var driverClass = selectedValue.substring(selectedValue.indexOf("#") + 1, selectedValue.length);
    document.getElementById('url').value = jdbcUrl;
    document.getElementById('driverClassName').value = driverClass;
}

function validateOutputMappingFields(obj){
    var grpByElement = document.getElementById('txtDataServiceWrapElement').value;
    var rowElement = document.getElementById('txtDataServiceRowName').value;
    var rowName = document.getElementById('txtDataServiceRowName').value;
    var rdfBaseURI = document.getElementById('txtrdfBaseURI').value;
    var queryId = document.getElementById('queryId').value;
    var datasource = document.getElementById('datasource').value;
    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Validate for alphanumeric characters and underscores
    if (!reWhiteSpace.test(queryId)) {
        CARBON.showWarningDialog("Alphanumeric characters and underscores are only allowed in the Query Id");
        return false;
    }
    if((grpByElement == '') && (rdfBaseURI == '')){
        if (grpByElement == ''){
            CARBON.showWarningDialog('Enter value to Grouped by element');
        } else {
            CARBON.showWarningDialog('Enter value to RDF Base URI');
        }
        return false;
    }
    if(queryId == ''){
        CARBON.showWarningDialog('Query id is required prior adding an output mapping');
        return false;
    }
    if(datasource == '#'){
        CARBON.showWarningDialog('Select the Datasource');
        return false;
    }
    //location.href = 'queryProcessor.jsp?flag=outputMapping&queryId='+document.getElementById('queryId').value+'&sql='+document.getElementById('sql').value+'&datasource='+document.getElementById('datasource').value+'&rowName='+document.getElementById('txtDataServiceRowName').value+'&element='+document.getElementById('txtDataServiceWrapElement').value+'&ns='+document.getElementById('txtDataServiceRowNamespace').value;
    document.getElementById('dataForm').action = 'queryProcessor.jsp?flag=outputMapping&edit='+obj;
    return true;
}

function validateComplexElement(obj){
    //location.href = 'queryProcessor.jsp?flag=outputMapping&queryId='+document.getElementById('queryId').value+'&sql='+document.getElementById('sql').value+'&datasource='+document.getElementById('datasource').value+'&rowName='+document.getElementById('txtDataServiceRowName').value+'&element='+document.getElementById('txtDataServiceWrapElement').value+'&ns='+document.getElementById('txtDataServiceRowNamespace').value;
    if (document.getElementById('txtDataServiceComplexElementName').value == '') {
    	 CARBON.showWarningDialog('Enter Complex Element Name');
         return false;
    }
    document.getElementById('dataForm').action ='OutputMappingProcessor.jsp?flag=complexElement&cmbDataServiceOMType=complexType&queryId=' + document.getElementById('queryId').value+'&txtDataServiceComplexElementName=' + document.getElementById('txtDataServiceComplexElementName').value+'&txtDataServiceComplexElementNamespace=' + document.getElementById('txtDataServiceComplexElementNamespace').value;
    return true;
}

function manageXADataService(obj){
       document.getElementById('dataForm').action ='manageXADS.jsp?flag=manageXADS';
	   return true;
}

function validateInputMappings(){
    var name = document.getElementById('inputMappingNameId').value;
    var sqlType = document.getElementById('inputMappingSqlTypeId').value;
    var ordinal = document.getElementById('inputMappingOrdinalId').value;
    var structType = document.getElementById('structType').value;

    if(name == ''){
        CARBON.showWarningDialog('Enter value to the Mapping Name');
        return false;
    }
    if(sqlType == ''){
        CARBON.showWarningDialog('Select SQL Type');
        return false;
    } else if (sqlType == 'STRUCT' && structType == '') {
        CARBON.showWarningDialog('Enter name of the SQL struct type');
        return false;
    }
    if(ordinal.match(/^[a-zA-Z]+$/)){
        CARBON.showWarningDialog('Enter numeric values to Ordinal');
        return false;
    }
    // }
    return true;
}

function validateAddEvent(){
    var name = document.getElementById('name').value;
    var expression = document.getElementById('expression').value;
    var targetTopic = document.getElementById('targetTopic').value;

    if(name == ''){
        CARBON.showWarningDialog('Enter Event ID');
        return false;
    }
    if (name.length > 100) {
    	  CARBON.showWarningDialog('Event Id should be a valid ID');
          return false;
    }
    if(expression == ''){
        CARBON.showWarningDialog('Enter Expression');
        return false;
    }
    if(targetTopic == ''){
        CARBON.showWarningDialog('Enter Target Topic');
        return false;
    }

    return true;
}

function validateSparqlInputMappings(){
    var name = document.getElementById('inputMappingNameId').value;
    var sqlType = document.getElementById('inputMappingSqlTypeId').value;

    if(name == ''){
        CARBON.showWarningDialog('Enter value to the Mapping Name');
        return false;
    }
    if(sqlType == ''){
        CARBON.showWarningDialog('Select XSD Type');
        return false;
    }
    return true;
}

function changeFileType(obj, document) {
    var fileType = obj.value;
    if(fileType == 'file') {
        document.getElementById('fileRow').style.display = '';
        document.getElementById('urlRow').style.display = 'none';
    } else {
        document.getElementById('fileRow').style.display = 'none';
        document.getElementById('urlRow').style.display = '';
    }

}

function validatRDFeOutputMappingMandatoryFields(){
    if(document.getElementById('cmbDataServiceOMType').value != ''){
        if(document.getElementById('cmbDataServiceOMType').value == 'resource'){
            if((document.getElementById('txtDataServiceResourceName').value == '')){
                CARBON.showWarningDialog('Resource mapping field is required prior adding an output mapping');
                return false;
            }
            if((document.getElementById('txtrdfRefURI').value == '')){
                CARBON.showWarningDialog('Resource URI is required prior adding an output mapping');
                return false;
            }
        } else if (document.getElementById('cmbDataServiceOMType').value == 'complexType') {
        	if((document.getElementById('txtDataServiceComplexElementName').value == '')){
                CARBON.showWarningDialog('Complex Type Element Name field is required prior adding an output mapping');
                return false;
            }

        } else {
        	var outputField = document.getElementById('txtDataServiceOMElementName').value;
            if(outputField == ''){
                CARBON.showWarningDialog('Output field name is required prior adding an output mapping!!');
                return false;
            }
            if (!isNaN(outputField)) {
           	 	CARBON.showWarningDialog('Output field name cannot be numeric');
           	 	return false;
            }

        }
        return true;
    }
    else{
        CARBON.showWarningDialog('Mapping Type is required.');
        return false;
    }
    return true;
}

function validateOutputMappingMandatoryFields(){
    if(document.getElementById('cmbDataServiceOMType').value != ''){
        if(document.getElementById('cmbDataServiceOMType').value == 'query'){
        	if(document.getElementById('cmbDataServiceQueryId').value == ''){
                CARBON.showWarningDialog('Please select the Query to proceed');
                return false;
            }
        } else if (document.getElementById('cmbDataServiceOMType').value == 'complexType') {
        	if((document.getElementById('txtDataServiceComplexElementName').value == '')){
                CARBON.showWarningDialog('Complex Type Element Name field is required prior adding an output mapping');
                return false;
            }

        } else {
            var outputField = document.getElementById('txtDataServiceOMElementName').value;
            if(outputField == ''){
                CARBON.showWarningDialog('Output field name is required prior adding an output mapping');
                return false;
            }

            if (!isNaN(outputField)) {
           	 	CARBON.showWarningDialog('Output field name cannot be numeric');
           	 	return false;
            }
            if (outputField.indexOf(" ") != -1) {
                CARBON.showWarningDialog('Output filed name cannot contain spaces');
                return false;
            }
            if (document.getElementById('datasourceValue1').value == '' && document.getElementById('datasourceValue2').value == ''){
            	CARBON.showWarningDialog('Column name / query-param is required for adding an output mapping');
                return false;
            }
        }
        return true;
    }else{
        CARBON.showWarningDialog('Mapping Type is required.');
        return false;
    }
    return true;
}

function validateInputMappingButton(){
    var queryId = document.getElementById('queryId').value;
    var sql = document.getElementById('sql').value;
    if(queryId == ''){
        CARBON.showWarningDialog('Query id is required prior adding an input mapping');
        return false;
    }
    if(sql == ''){
        CARBON.showWarningDialog('Enter value to SQL');
        return false;
    }
    //location.href = 'queryProcessor.jsp?flag=inputMapping&queryId='+document.getElementById('queryId').value+'&sql='+document.getElementById('sql').value+'&datasource='+document.getElementById('datasource').value;
    return false;
}



function changeToDataSourceType(obj, document){
    var selectedValue = obj[obj.selectedIndex].value;
    if(selectedValue == 'query-param'){
        document.getElementById('queryParamnRow').style.display = '';
        document.getElementById('columnRow').style.display = 'none';
    }
    else{
        document.getElementById('queryParamnRow').style.display = 'none';
        document.getElementById('columnRow').style.display = '';
    }
}


function changeToNextRDFMapping(obj, document){
    var selectedValue = obj[obj.selectedIndex].value;
    if(selectedValue == 'resource'){
        document.getElementById('resourceRow').style.display = '';
        document.getElementById('omElementRowId').style.display = 'none';
    }
    else {
        document.getElementById('resourceRow').style.display = 'none';
        document.getElementById('omElementRowId').style.display = '';
    }
}

function changeToNextMapping(obj, document){
    var selectedValue = obj[obj.selectedIndex].value;
    if(selectedValue == 'query'){
        document.getElementById('queryRow').style.display = '';
        document.getElementById('omElementRowId').style.display = 'none';
        document.getElementById('complexTypeRowId').style.display = 'none';
    }
    else if(selectedValue == 'complexType'){
    	document.getElementById('complexTypeRowId').style.display = '';
        document.getElementById('queryRow').style.display = 'none';
        document.getElementById('omElementRowId').style.display = 'none';

    } else if(selectedValue == 'element') {
        document.getElementById('queryRow').style.display = 'none';
        document.getElementById('omElementRowId').style.display = '';
        document.getElementById('elementNameSpaceRow').style.display = '';
        document.getElementById('complexTypeRowId').style.display = 'none';

    } else if(selectedValue == 'attribute') {
	   	 document.getElementById('queryRow').style.display = 'none';
	     document.getElementById('omElementRowId').style.display = '';
	     document.getElementById('complexTypeRowId').style.display = 'none';
	     document.getElementById('elementNameSpaceRow').style.display = 'none';
    }
}

function onEnableXAChange(document){
	var useAppServerBtn = document.getElementById("enableDT");
    if (getCheckedValue(useAppServerBtn) == "") {
    	document.getElementById('txManager').style.display = 'none';
    } else {
    	document.getElementById('txManager').style.display = '';
    }
}

function onModeChange(document){
	var multipleMode = document.getElementById("mode");
    if (getCheckedValue(multipleMode) == "Single") {
    	document.getElementById('txServiceNameRow').style.display = '';
    } else {
    	document.getElementById('txServiceNameRow').style.display = 'none';
    }
}

function getCheckedValue(radioObj) {
	if (!radioObj) {
		return "";
	}
	var radioLength = radioObj.length;
	if (radioLength == undefined) {
		if(radioObj.checked) {
			return radioObj.value;
		} else {
			return "";
		}
	}
	for (var i = 0; i < radioLength; i++) {
		if (radioObj[i].checked) {
			return radioObj[i].value;
		}
	}
	return "";
}


function onUseAppServerChange(document) {
	var useAppServerBtn = document.getElementById("useAppServerTS");
    if (getCheckedValue(useAppServerBtn) == "true") {
    	document.getElementById('txManagerNameRow').style.display = '';
    } else {
    	document.getElementById('txManagerNameRow').style.display = 'none';
    }
}

function sendToInputMapping(obj){
    document.getElementById('dataForm').URL = 'queryProcessor.jsp?flag=inputMapping';
    return false;
}

function sendToSparqlInputMapping(obj){
    document.getElementById('dataForm').URL = 'queryProcessor.jsp?flag=sparqlInputMapping';
    return false;
}

function getCSVHeaderValues(obj){
    var configId = document.getElementById('datasourceId').value;
    location.href = 'setHeader_ajaxprocessor.jsp?configId='+configId;
}

function setCSVColumnSelection(){
    var csvColumns = document.getElementById('csvColumnSelection');
    if (csvSelectedColumnOrder == csvColumns.options.length) {
        var message = "You have aleady selected all columns.This will reset all column selections done so far. Do you want to continue?";
        if (confirm(message)) {
            csvSelectedColumnOrder = 0;
            getCSVHeaderColumnNames(document);
        }
    }

    for (i = 0; i < csvColumns.options.length; i++) {
        if (csvColumns.options[i].selected) {
            if (csvColumns.options[i].text.indexOf('as column no') == -1) {
                csvSelectedColumnOrder++;
                if (csvSelectedColumnOrder > 1) {
                    document.getElementById('csv_columns').value =
                    document.getElementById('csv_columns').value + ',' + csvColumns.options[i].text;
                    document.getElementById('csv_columnordinal').value =
                    document.getElementById('csv_columnordinal').value + ',' +
                    csvSelectedColumnOrder;
                } else {
                    document.getElementById('csv_columns').value = csvColumns.options[i].text;
                    document.getElementById('csv_columnordinal').value = csvSelectedColumnOrder;
                }
                csvColumns.options[i].text =
                csvColumns.options[i].text + " as column no : " + csvSelectedColumnOrder;
                csvColumns.options[i].value =
                csvColumns.options[i].value + ":" + csvSelectedColumnOrder;
            }
        }
    }
}

function incrementCount(count) {
    count = count+1;
     document.getElementById('propertyCount').value =count;

}
function deleteDatasource(obj) {
    function forwardToDel() {
        var url = 'dataSourceProcessor.jsp?datasourceId=' + obj + '&flag=delete';
        document.location.href = url;
    }
    CARBON.showConfirmationDialog('Do you want to delete the datasource ' + obj + ' ?', forwardToDel);
}

function deleteXADatasource(obj) {
    function forwardToDel() {
        var url = 'xaDataSourceProcessor.jsp?xaId=' + obj + '&action=delete';
        document.location.href = url;
    }
    CARBON.showConfirmationDialog('Do you want to delete the XA datasource ' + obj + ' ?', forwardToDel);

}

function deleteXADSProperty(obj,xaId) {
    function forwardToDel() {
        var url = 'xaDataSourceProcessor.jsp?txPropertyName=' + obj + '&xaId='+xaId+'&action=deleteAddProp';
        document.location.href = url;
    }
    CARBON.showConfirmationDialog('Do you want to delete the XA datasource property ' + obj + ' ?', forwardToDel);

}

function deleteQuery(obj){
    function forwardToDel() {
        var url = 'queryProcessor.jsp?queryId='+obj+'&flag=delete';
        document.location.href = url;
    }
    CARBON.showConfirmationDialog('Do you want to delete '+obj+' query?', forwardToDel);

}

function deleteResources(objPath, objMethod){
    function forwardToDel() {
        var url = 'resourceProcessor.jsp?action=remove&oldResourcePath=' + encodeURIComponent(objPath)
            + '&oldResourceMethod=' + objMethod;
        document.location.href = url;
    }
    CARBON.showConfirmationDialog('Do you want to delete resource '+objPath+'?', forwardToDel);

}

function deleteOperations(obj){
    function forwardToDel() {
        var url = 'operationProcessor.jsp?action=remove&operationName='+obj;
        document.location.href = url;
    }
    CARBON.showConfirmationDialog('Do you want to delete operation '+obj+'?', forwardToDel);
}

function deleteInputMappings(objName,objSqlType,queryId, type){
    function forwardToDel(){
        var url = 'inputMappingProcessor.jsp?inputMappingId='+objName+'&inputMappingSqlType='+objSqlType+'&queryId='+queryId+'&flag=delete'+type+'&origin=add';
        document.location.href = url;

    }
    CARBON.showConfirmationDialog('Do you want to delete the input mapping '+objName+'?', forwardToDel);
}

function deleteSparqlInputMappings(objName,objSqlType,queryId){
    function forwardToDel(){
        var url = 'inputMappingProcessor.jsp?inputMappingId='+objName+'&inputMappingSqlTypeId='+objSqlType+'&queryId='+queryId+'&flag=delete&origin=add';
        document.location.href = url;
    }
    CARBON.showConfirmationDialog('Do you want to delete the input mapping '+objName+"type"+objSqlType+'?', forwardToDel);
}


function deleteInputMappingsFromAddQuery(objName,objSqlType,queryId,type){
    function forwardToDel(){
        var url = 'inputMappingProcessor.jsp?inputMappingId='+objName+'&inputMappingSqlType='+objSqlType+'&queryId='+queryId+'&flag=delete'+type+'&origin=save';
        document.location.href = url;
    }
    CARBON.showConfirmationDialog('Do you want to delete the input mapping '+objName+'?', forwardToDel);
}

function deleteSQLDialectAddQuery(queryId,dialect){
    function forwardToDel(){
        var url = 'sqlDialectProcessor.jsp?queryId='+queryId+'&flag=delete&txSQLDialect='+dialect;
        document.location.href = url;
    }
    CARBON.showConfirmationDialog('Do you want to delete the sql dialect '+dialect+'?', forwardToDel);
}

function deleteSparqlInputMappingsFromAddQuery(objName,objSqlType,queryId){
    function forwardToDel(){
        var url = 'sparqlInputMappingProcessor.jsp?inputMappingId='+objName+'&inputMappingSqlTypeId='+objSqlType+'&queryId='+queryId+'&flag=delete&origin=save';
        document.location.href = url;
    }
    CARBON.showConfirmationDialog('Do you want to delete the input mapping '+objName+'?', forwardToDel);
}

function deleteOutputMappings(queryId,name,mappingType){
    function forwardToDel(){
        if(mappingType == 'element'){
            var url = 'OutputMappingProcessor.jsp?queryId='+queryId+'&edit=delete&cmbDataServiceOMType=element&flag=add&txtDataServiceOMElementName='+name;
        }
        if(mappingType == 'attribute'){
            var url = 'OutputMappingProcessor.jsp?queryId='+queryId+'&edit=delete&cmbDataServiceOMType=attribute&txtDataServiceOMElementName='+name+'&flag=add';
        }
        if(mappingType == 'query'){
            var url = 'OutputMappingProcessor.jsp?queryId='+queryId+'&cmbDataServiceQueryId='+name+'&edit=delete&cmbDataServiceOMType=query&href='+name+'&flag=add';
        }
        if(mappingType == 'complexType'){
        	 var url = 'OutputMappingProcessor.jsp?queryId='+queryId+'&edit=delete&cmbDataServiceOMType=complexType&flag=add&txtDataServiceComplexElementName='+name;
        }
        document.location.href = url;
    }
    CARBON.showConfirmationDialog('Do you want to delete the output mapping '+name+'?', forwardToDel);
}

function deleteComplexOutputMappings(queryId,complexPath,name,mappingType){
    function forwardToDel(){
        if(mappingType == 'element'){
            var url = 'OutputMappingProcessor.jsp?queryId='+queryId+'&edit=delete&cmbDataServiceOMType=element&complexPath='+complexPath+'&flag=add&txtDataServiceOMElementName='+name;
        }
        if(mappingType == 'attribute'){
            var url = 'OutputMappingProcessor.jsp?queryId='+queryId+'&edit=delete&cmbDataServiceOMType=attribute&complexPath='+complexPath+'&txtDataServiceOMElementName='+name+'&flag=add';
        }
        if(mappingType == 'query'){
            var url = 'OutputMappingProcessor.jsp?queryId='+queryId+'&cmbDataServiceQueryId='+name+'&edit=delete&cmbDataServiceOMType=query&complexPath='+complexPath+'&href='+name+'&flag=add';
        }
        if(mappingType == 'complexType'){
        	 var url = 'OutputMappingProcessor.jsp?queryId='+queryId+'&edit=delete&cmbDataServiceOMType=complexType&complexPath='+complexPath+'&flag=add&txtDataServiceComplexElementName='+name;
        }
        document.location.href = url;
    }
    CARBON.showConfirmationDialog('Do you want to delete the output mapping '+name+'?', forwardToDel);
}

function deleteRDFOutputMappings(queryId,name,mappingType){
    function forwardToDel(){
        if(mappingType == 'element'){
            var url = 'OutputMappingProcessor.jsp?queryId='+queryId+'&edit=delete&cmbDataServiceOMType=element&flag=addrdf&txtDataServiceOMElementName='+name;
        }
        if(mappingType == 'resource'){
            var url = 'OutputMappingProcessor.jsp?queryId='+queryId+'&edit=delete&cmbDataServiceOMType=resource&flag=addrdf&txtDataServiceResourceName='+name;
        }
        document.location.href = url;
    }
    CARBON.showConfirmationDialog('Do you want to delete the output mapping '+name+'?', forwardToDel);
}

function deleteOutputMappingsFromAddQuery(queryId,name,mappingType){
    function forwardToDel(){
        if(mappingType == 'element'){
            var url = 'OutputMappingProcessor.jsp?queryId='+queryId+'&edit=delete&cmbDataServiceOMType=element&txtDataServiceOMElementName='+name+'&flag=save';
        }
        if(mappingType == 'resource'){
            var url = 'OutputMappingProcessor.jsp?queryId='+queryId+'&edit=delete&cmbDataServiceOMType=resource&txtDataServiceResourceName='+name+'&flag=save';
        }
        if(mappingType == 'attribute'){
            var url = 'OutputMappingProcessor.jsp?queryId='+queryId+'&edit=delete&cmbDataServiceOMType=attribute&txtDataServiceOMElementName='+name+'&flag=save';
        }
        if(mappingType == 'query'){
            var url = 'OutputMappingProcessor.jsp?queryId='+queryId+'&cmbDataServiceQueryId='+name+'&edit=delete&cmbDataServiceOMType=query&href='+name+'&flag=save';
        }
        if(mappingType == 'complexType'){
            var url = 'OutputMappingProcessor.jsp?queryId='+queryId+'&edit=delete&cmbDataServiceOMType=complexType&txtDataServiceComplexElementName='+name+'&flag=save';
        }
        document.location.href = url;
    }
    CARBON.showConfirmationDialog('Do you want to delete the output mapping '+name+'?', forwardToDel);
}

function redirectToMainConfiguration(obj){
    function forwardToDel(){
        var url = 'addQuery.jsp?queryId='+obj+'&ordinal=3';
        document.location.href = url;
    }
    if(!validateReturnToMainConfiguration(document)) {
       CARBON.showConfirmationDialog('Do you want to go back to the main configuration?',forwardToDel);
    } else {
       forwardToDel();
    }

}

function gspreadVisibiltyOnChange(obj, document) {
	var selectedValue = obj[obj.selectedIndex].value;
    dval = null;
    if (selectedValue == "private") {
        dval = "table-row";
    } else {
        dval = "none";
    }
    document.getElementById('tr:gspread_client_id').style.display = dval;
    document.getElementById('tr:gspread_client_secret').style.display = dval;
    document.getElementById('tr:gspread_refresh_token').style.display = dval;
    document.getElementById('tr:gspread_redirect_uris').style.display = dval;
    document.getElementById('btn:gspread_generate_tokens').style.display = dval;
}

function gspreadVisibiltyOnChangeQMode(obj, document) {
	var selectedValue = obj[obj.selectedIndex].value;
	if (selectedValue == "private") {
		document.getElementById('tr:querymode_gspread_username').style.display = "";
		document.getElementById('tr:querymode_gspread_password').style.display = "";
    } else {
    	document.getElementById('tr:querymode_gspread_username').style.display = "none";
    	document.getElementById('tr:querymode_gspread_password').style.display = "none";
    }
}

function defaultValueVisibilityOnChange(obj, document) {
    var selectedValue = obj[obj.selectedIndex].value;
    var visibile = null;
    if(selectedValue == 'ARRAY'){
        visibile = 'none';
    }else{
        visibile = '';
    }
    document.getElementById('defaultValueRow').style.display = visibile;
}

function arrayNameVisibilityOnChange(obj, document) {
    var selectedValue = obj[obj.selectedIndex].value;
    var visible = null;
    if(selectedValue == 'ARRAY'){
        visible = '';
    }else{
        visible = 'none';
    }
    document.getElementById('arrayNameRow').style.display = visible;
    document.getElementById('arrayNameRow1').style.display = visible;
}

function changeDataSourceType (obj, document) {
	var selectedType =  obj[obj.selectedIndex].value;
	var selectedDS = document.getElementById('datasourceId').value;
	var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
	if (selectedDS == ''){
        CARBON.showWarningDialog('Insert datasource id');
        obj.selectedIndex = 0;
        return false;
	}
	// Validate for alphanumeric characters and underscores
    if (!reWhiteSpace.test(selectedDS)) {
       CARBON.showWarningDialog("Alphanumeric characters and underscores are only allowed in the Datasource Id");
       obj.selectedIndex = 0;
       return false;
    }
	else {
		location.href = 'addDataSource.jsp?selectedType='+selectedType+'&configId='+selectedDS+'&ds=edit&flag=edit_changed';
	}
}

function changeXADataSourceEngine (obj, document) {
 	var selectedType =  obj[obj.selectedIndex].value;
 	var driverClass = selectedType.substring(0, selectedType.indexOf("#"));
    var url = selectedType.substring(selectedType.indexOf("#") + 1, selectedType.length);
 	document.getElementById('org.wso2.ws.dataservice.xa_datasource_class').value = driverClass;
    document.getElementById('URL').value = url;
 }

function changeXAType(obj, document) {
	var selectedDS = document.getElementById('datasourceId').value;
    var selectedVal = obj.value;
    var xaVal = false;
    if (selectedVal == 'xaType') {
    	xaVal = true;
    }
   location.href = 'addDataSource.jsp?selectedType=RDBMS&configId='+selectedDS+'&xaVal='+xaVal+'&flag=edit';
}

function outputTypeVisibilityOnChange(obj, document) {
    var selectedValue = obj[obj.selectedIndex].value;
    var visibile = null;
    if (selectedValue == 'rdf') {
        document.getElementById('xmlResultTypeRow').style.display = 'none';
        document.getElementById('rdfResultTypeRow').style.display = '';
        document.getElementById('jsonResultTypeRow').style.display = 'none';
        document.getElementById('addOutputMappingsRow').style.display = '';
        document.getElementById('existingOutputMappingsTable').style.display = '';
    } else if (selectedValue == 'xml') {
        document.getElementById('xmlResultTypeRow').style.display = '';
        document.getElementById('rdfResultTypeRow').style.display = 'none';
        document.getElementById('jsonResultTypeRow').style.display = 'none';
        document.getElementById('addOutputMappingsRow').style.display = '';
        document.getElementById('existingOutputMappingsTable').style.display = '';
    } else if (selectedValue == 'json') {
        document.getElementById('xmlResultTypeRow').style.display = 'none';
        document.getElementById('rdfResultTypeRow').style.display = 'none';
        document.getElementById('jsonResultTypeRow').style.display = '';
        document.getElementById('addOutputMappingsRow').style.display = 'none';
        document.getElementById('existingOutputMappingsTable').style.display = 'none';
    }
}


function inOutVisibilityOnChange(obj, document) {
    var selectedValue = obj[obj.selectedIndex].value;
    if(selectedValue == 'OUT'){
        //document.getElementById('validatorRow').style.display = 'none';
    }else{
       //document.getElementById('validatorRow').style.display = '';
    }
}

function changeAddValidatorFields(obj,document) {
    var selectValue = obj[obj.selectedIndex].value;
    var visibleRangeVal = null;
    var visiblePattern = null;
    var visibleCustom = null;

    if(selectValue != null){
        document.getElementById('validators').style.display = '';
        if(selectValue != 'validatePattern' || selectValue != 'validateCustom') {
            visibleRangeVal = '';
            visibleCustom = 'none';
            visiblePattern = 'none';
        }
        if(selectValue == 'validatePattern') {
            visibleRangeVal = 'none';
            visibleCustom = 'none';
            visiblePattern = '';
        }
        if(selectValue == 'validateCustom'){
            visibleCustom = '';
            visiblePattern = 'none';
            visibleRangeVal = 'none';
        }
        if(selectValue == "#") {
        	visibleRangeVal = 'none';
            visibleCustom = 'none';
            visiblePattern = 'none';
        }
    }
    document.getElementById('maxRangeValidatorElementsRow').style.display = visibleRangeVal;
    document.getElementById('minRangeValidatorElementsRow').style.display = visibleRangeVal;
    document.getElementById('patternValidatorElementsRow').style.display = visiblePattern;
    document.getElementById('customValidatorElementsRow').style.display = visibleCustom;
    document.getElementById('customValidatorPropertyElementsRow').style.display = visibleCustom;
    document.getElementById('addValidator').style.display = '';

    document.getElementById('max').value = "";
    document.getElementById('min').value = "";
    document.getElementById('pattern').value = "";
    document.getElementById('customClass').value = "";
    document.getElementById('addValidator').value = "Add Validator";
}

function toggleValidators(validatorName, i, document) {
	var minValue = 0;
	var maxValue = 0;
	var customClass = "";
	var pattern = "";
	propertyString = document.getElementById('propString'+i).value;
	var properties = propertyString.split(" ");

	if (validatorName == "Length Validator") {
		validatorName = "validateLength";
	} else if (validatorName == "Long Range Validator") {
		validatorName = "validateLongRange";
	} else if (validatorName == "Double Range Validator") {
		validatorName = "validateDoubleRange";
	} else if (validatorName == "Pattern Validator") {
		validatorName = "validatePattern";
	} else if (validatorName == "Custom Validator"){
		validatorName = "validateCustom";
	}
	document.getElementById('validatorList').value=validatorName;

	document.getElementById('validators').style.display = '';
    if(validatorName == "validateLength" || validatorName == "validateLongRange"
    	|| validatorName == "validateDoubleRange") {
    	var minNameValue = properties[1].split("=");
        var maxNameValue = properties[0].split("=");

    	minValue = minNameValue[1];
    	maxValue = maxNameValue[1];

        visibleRangeVal = '';
        visibleCustom = 'none';
        visiblePattern = 'none';
    }
    if(validatorName == 'validatePattern') {
    	var patternSplitIndex = properties[0].indexOf("=");
    	pattern = properties[0].substring(patternSplitIndex + 1);

        visibleRangeVal = 'none';
        visibleCustom = 'none';
        visiblePattern = '';
    }
    if(validatorName == 'validateCustom'){
    	var classNameValue = properties[0].split("=");
    	customClass = classNameValue[1];

        visibleCustom = '';
        visiblePattern = 'none';
        visibleRangeVal = 'none';
    }
    if(validatorName == "#") {
    	visibleRangeVal = 'none';
        visibleCustom = 'none';
        visiblePattern = 'none';
    }

    document.getElementById('maxRangeValidatorElementsRow').style.display = visibleRangeVal;
    document.getElementById('minRangeValidatorElementsRow').style.display = visibleRangeVal;
    document.getElementById('patternValidatorElementsRow').style.display = visiblePattern;
    document.getElementById('customValidatorElementsRow').style.display = visibleCustom;
    document.getElementById('addValidator').style.display = '';

    document.getElementById('max').value = maxValue;
    document.getElementById('min').value = minValue;
    document.getElementById('pattern').value = pattern;
    document.getElementById('customClass').value = customClass;
    document.getElementById('addValidator').value = "Update Validator";
}

        var rows = 0;
        var itr = 0;
        //add a new row to the table
        function addRow(rowCount, flag) {
            if (rowCount != 0 && flag == 'edit' && itr == 0) {
                rows = rowCount ;
                itr++;
            }
            rows++;

            //add a row to the rows collection and get a reference to the newly added row
            var newRow = document.getElementById("serviceTbl").insertRow(-1);
            newRow.id = 'subscription' + rows;

            var oCell = newRow.insertCell(-1);
            oCell.innerHTML = "<input type='text' style='margin-left: -7px;' name='subscription"+ rows +"' size='30'/>&nbsp;&nbsp;<input type='button' style='margin-left: -6px;' value='  -  ' onclick=\"deleteRow('subscription"+ rows +"');\" />";
            oCell.className = "normal";
            alternateTableRows('serviceTbl', '', '');

            return true;
        }

        function deleteRow(rowId) {
            var tableRow = document.getElementById(rowId);
            tableRow.parentNode.deleteRow(tableRow.rowIndex);
            alternateTableRows('serviceTbl', '', '');

            return true;
        }

function deleteEvent(eventId, queryId) {
    function forwardToDel(){
        var url = 'eventProcessor.jsp?queryId='+queryId+'&id='+eventId+'&flag=delete';
        document.location.href = url;

    }
   CARBON.showConfirmationDialog('Do you want to delete the input mapping '+eventId+'?', forwardToDel);
}

function showSQLDialects() {
    var dialectTab = document.getElementById('SQLDialectTable');
    var sqlDialectSymbolMax =  document.getElementById('sqlDialectSymbolMax');
    if(dialectTab.style.display == 'none') {
        dialectTab.style.display = '';
        sqlDialectSymbolMax.setAttribute('style','background-image:url(images/minus.gif);');
    } else {
        dialectTab.style.display = 'none';
        sqlDialectSymbolMax.setAttribute('style','background-image:url(images/plus.gif);');
    }
}

function showQueryProperties() {
    var propertyTab = document.getElementById('propertyTable');
    var propertySymbolMax =  document.getElementById('propertySymbolMax');
    if(propertyTab.style.display == 'none') {
        propertyTab.style.display = '';
        propertySymbolMax.setAttribute('style','background-image:url(images/minus.gif);');
    } else {
        propertyTab.style.display = 'none';
        propertySymbolMax.setAttribute('style','background-image:url(images/plus.gif);');
    }
}

function showPasswordManager() {
  var pwdMngrSymbolMax =  document.getElementById('pwdMngrSymbolMax');
  var passwordManagerFields = document.getElementById('passwordManagerFields');
  if(passwordManagerFields.style.display == 'none') {
    pwdMngrSymbolMax.setAttribute('style','background-image:url(images/minus.gif);');
    passwordManagerFields.style.display = '';
  } else {
      pwdMngrSymbolMax.setAttribute('style','background-image:url(images/plus.gif);');
      passwordManagerFields.style.display = 'none';
  }


}

function showExportOption() {
	  var exportTableTab = document.getElementById('exportTable');
	  var exportSymbolMin =  document.getElementById('exportSymbolMin');
	  var exportSymbolMax =  document.getElementById('exportSymbolMax');
	  if(exportTableTab.style.display == 'none') {
	    exportTableTab.style.display = '';
	    exportSymbolMin.style.display='';
	    exportSymbolMax.style.display='none';
	  } else {
	    exportTableTab.style.display = 'none';
	    exportSymbolMin.style.display='none';
	    exportSymbolMax.style.display='';
	  }
}


function validateValidators(obj, document) {
    var max = document.getElementById('max').value;
    var min = document.getElementById('min').value;
    var pattern = document.getElementById('pattern').value;
    var customClass = document.getElementById('customClass').value;
    var validator = document.getElementById('validatorList')[document.getElementById('validatorList').selectedIndex].value;

    if(validator == 'validateLongRange' || validator == 'validateDoubleRange' || validator == 'validateLength') {
        if(isNaN(max)){
            CARBON.showWarningDialog("Maximum Value " + "'" +max + "'" + " should be a numeric value");
            return false;
        } else if(isNaN(min)){
            CARBON.showWarningDialog("Minimum Value " + "'" +min + "'" + " should be a numeric value");
            return false;
        } else if(( validator == 'validateLongRange' || validator == 'validateLength') && (parseInt(max) < parseInt(min))) {
            CARBON.showWarningDialog("Maximum Value is less than Minimum Value");
            return false;
        } else if (validator == 'validateDoubleRange' && (parseFloat(max) < parseFloat(min))) {
            CARBON.showWarningDialog("Maximum Value is less than Minimum Value");
            return false;
        } else if (max == '') {
            CARBON.showWarningDialog("Maximum Value is mandatory");
            return false;
        } else if (min == '') {
            CARBON.showWarningDialog("Minimum Value is mandatory");
            return false;
        }

    } else if (validator == 'validatePattern'){
        if (pattern == '') {
            CARBON.showWarningDialog("Pattern is mandatory");
            return false;
        }
    } else if (validator == 'validateCustom') {
        if(customClass == '') {
            CARBON.showWarningDialog("Class Name is mandatory");
            return false;
        }
    }
     return true;
}

function changeWebHarvestConfig(obj, document) {
    var configType = obj.value;
    if(configType == 'file') {
        document.getElementById('web_harvest_config').style.display = '';
        document.getElementById('web_harvest_config_textArea').style.display = 'none';
        document.getElementById('config_reg').style.display = '';
        document.getElementById('gov_reg').style.display = '';
    } else {
        document.getElementById('web_harvest_config').style.display = 'none';
        document.getElementById('config_reg').style.display = 'none';
        document.getElementById('gov_reg').style.display = 'none';
        document.getElementById('web_harvest_config_textArea').style.display = '';
    }
}

/*
This method takes the all 'input' elements in a document and if the element type is 'text'
it check whether that text field is empty or not. If document contains at least one text field with
some text value  method will return false. If all text fields in document is empty it returns true.

This method is used in redirectToMainConfiguration() method. Using this method it checks whether
text fields in current page is empty or not. If all fields are  empty directly goto main configuration
page.If text fields contains some value pop up a confirmation message before leaving the current page.
 */
function validateReturnToMainConfiguration(document) {
    var inputs = document.getElementsByTagName("input");
    var countNotEmptyFields = 0;

    for (var i = 1; i < inputs.length; i++) {
        if (inputs[i].getAttribute('type') == 'text') {
            if(document.getElementById(inputs[i].getAttribute('id')).value != ''){
               countNotEmptyFields = countNotEmptyFields + 1;
                break;
            }
        }
    }

    if (countNotEmptyFields > 0) {
        return false;
    } else {
        return true;
    }
}


function addXAPropertyFields(obj,propertyCount) {
    var propVal = parseInt(propertyCount);
    document.getElementById('propertyCount').value=  propVal+1;
    var table = document.getElementById('mainTable');

    var propertyNameRaw = document.createElement("tr");
    var propertyValueRaw = document.createElement("tr");
    propertyNameRaw.setAttribute("id", "propertyNameRaw" + propertyCount);
    //propertyValueRaw.setAttribute("id", "propertyValueRaw" + propertyCount);

    var td1 = document.createElement("TD");
    var label = document.createElement('label');
    var labelText = document.createTextNode('Property Name');
    label.appendChild(labelText);


    var td2 = document.createElement("TD");
    var el = document.createElement('input');
    el.type = 'text';
    el.name = 'propertyNameRaw'+propertyCount;
    el.id = 'propertyNameRaw'+propertyCount;
    el.size = 30;

    td1.appendChild(label);
    td2.appendChild(el);

    propertyNameRaw.appendChild(td1);
    propertyNameRaw.appendChild(td2);

    var td3 = document.createElement("TD");
    var valueLabel = document.createElement('label');
    var valueLabelText = document.createTextNode('Property Value');
    valueLabel.appendChild(valueLabelText);


    var td4 = document.createElement("TD");
    var valueEl = document.createElement('input');
    valueEl.type = 'text';
    valueEl.name = 'propertyValueRaw'+propertyCount;
    valueEl.id = 'propertyValueRaw'+propertyCount;
    valueEl.size = 30;

    var deleteTD = document.createElement("td");
    deleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deleteNewPropertyField(" +
            propertyCount + ");return false;'>Delete</a>";

    td3.appendChild(valueLabel);
    td4.appendChild(valueEl);

    var aliasTD = document.createElement("td");
    var valueAl = document.createElement('input');
    valueAl.type = 'checkbox';
    valueAl.name = 'useSecretAliasFor'+propertyCount;
    valueAl.id = 'useSecretAliasFor'+propertyCount;

    var aliasLabelTD = document.createElement("td");
    var valueAlLabel = document.createElement('label');
    var aliasLabelText = document.createTextNode('Use as Secret Alias');
    valueAlLabel.appendChild(aliasLabelText);

    aliasTD.appendChild(valueAl);
    aliasLabelTD.appendChild(valueAlLabel);

    propertyNameRaw.appendChild(td3);
    propertyNameRaw.appendChild(td4);
    propertyNameRaw.appendChild(aliasTD);
    propertyNameRaw.appendChild(aliasLabelTD);
    propertyNameRaw.appendChild(deleteTD);

    document.getElementById("externalDSPropertiesTable").getElementsByTagName('tbody')[0].appendChild(propertyNameRaw);
    document.getElementById("externalDSProperties").style.display = '';
    return true;
}

function addStaticUserAuthFields(obj,userCount) {
    var propVal = parseInt(userCount);
    document.getElementById('staticUserMappingsCount').value=  propVal+1;
    var table = document.getElementById('staticUserMapping');

    var carbonUsernameRaw = document.createElement("tr");
    carbonUsernameRaw.setAttribute("id", "carbonUsernameRaw" + userCount);

    var td1 = document.createElement("TD");
    var label = document.createElement('label');
    var labelText = document.createTextNode('Carbon Username');
    label.appendChild(labelText);


    var td2 = document.createElement("TD");
    var el = document.createElement('input');
    el.type = 'text';
    el.name = 'carbonUsernameRaw'+userCount;
    el.id = 'carbonUsernameRaw'+userCount;
    el.size = 15;

    td1.appendChild(label);
    td2.appendChild(el);

    carbonUsernameRaw.appendChild(td1);
    carbonUsernameRaw.appendChild(td2);

    var td3 = document.createElement("TD");
    var valueLabel = document.createElement('label');
    var valueLabelText = document.createTextNode('DB Username');
    valueLabel.appendChild(valueLabelText);


    var td4 = document.createElement("TD");
    var valueEl = document.createElement('input');
    valueEl.type = 'text';
    valueEl.name = 'dbUsernameRaw'+userCount;
    valueEl.id = 'dbUsernameRaw'+userCount;
    valueEl.size = 15;

    td3.appendChild(valueLabel);
    td4.appendChild(valueEl);

    var td5 = document.createElement("TD");
    var dbpwdLabel = document.createElement('label');
    var dbpwdLabelText = document.createTextNode('DB User Password');
    dbpwdLabel.appendChild(dbpwdLabelText);


    var td6 = document.createElement("TD");
    var dbpwdEl = document.createElement('input');
    dbpwdEl.type = 'password';
    dbpwdEl.name = 'dbPwdRaw'+userCount;
    dbpwdEl.id = 'dbPwdRaw'+userCount;
    dbpwdEl.size = 15;

    var deleteTD = document.createElement("td");
    deleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deleteUserField(" +
            userCount + ");return false;'>Delete</a>";

    td5.appendChild(dbpwdLabel);
    td6.appendChild(dbpwdEl);

    carbonUsernameRaw.appendChild(td3);
    carbonUsernameRaw.appendChild(td4);
    carbonUsernameRaw.appendChild(td5);
    carbonUsernameRaw.appendChild(td6);
    carbonUsernameRaw.appendChild(deleteTD);

    document.getElementById("staticUserMapping").getElementsByTagName('tbody')[0].appendChild(carbonUsernameRaw);
    return true;
}

function deleteUserField(i) {
    var deleteUserField = document.getElementById("carbonUsernameRaw" + i);
    if (deleteUserField != undefined && deleteUserField != null) {
        var parentTBody = deleteUserField.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(deleteUserField);
        }
    }
}

function addAuthorizationProviderParameter(obj,paramCount) {
    var propVal = parseInt(paramCount);
    document.getElementById('authorizationProviderParamCount').value=  propVal+1;
    var table = document.getElementById('authorizationProviderParametersTable');

    var strngNameLabel = document.getElementById("paramNameLabel").textContent;
    var strngValueLabel = document.getElementById("paramValueLabel").textContent;

    var parameterRow = document.createElement("tr");
    parameterRow.setAttribute("id", "authProviderParameterRow" + paramCount);

    var td1 = document.createElement("TD");
    var label = document.createElement('label');
    var labelText = document.createTextNode(strngNameLabel);
    label.appendChild(labelText);


    var td2 = document.createElement("TD");
    var el2 = document.createElement('input');
    el2.type = 'text';
    el2.name = 'authProviderParameterName'+paramCount;
    el2.id = 'authProviderParameterName'+paramCount;
    el2.size = 15;

    td1.appendChild(label);
    td2.appendChild(el2);

    parameterRow.appendChild(td1);
    parameterRow.appendChild(td2);

    var td3 = document.createElement("TD");
    var label3 = document.createElement('label');
    var labelText3 = document.createTextNode(strngValueLabel);
    label3.appendChild(labelText3);


    var td4 = document.createElement("TD");
    var el4 = document.createElement('input');
    el4.type = 'text';
    el4.name = 'authProviderParameterValue'+paramCount;
    el4.id = 'authProviderParameterValue'+paramCount;
    el4.size = 15;

    td3.appendChild(label3);
    td4.appendChild(el4);

    parameterRow.appendChild(td3);
    parameterRow.appendChild(td4);

    var deleteTD = document.createElement("td");
    deleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deleteAuthParamField(" +
                         paramCount + ");return false;'>Delete</a>";

    parameterRow.appendChild(deleteTD);

    document.getElementById("authorizationProviderParametersTable").getElementsByTagName('tbody')[0].appendChild(parameterRow);
    return true;
}

function deleteAuthParamField(i) {
    var stngVal = document.getElementById('authorizationProviderParamCount').value;
    var propVal = parseInt(stngVal);
    var deleteParamField = document.getElementById("authProviderParameterRow" + i);
    if (deleteParamField != undefined && deleteParamField != null) {
        var parentTBody = deleteParamField.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(deleteParamField);
            document.getElementById('authorizationProviderParamCount').value=  propVal-1;
        }
    }
}

function showDynamicUserAuthenticationConfigurations() {
  var symbolMax =  document.getElementById('symbolMax');
  var dynamicUserAuthenticationFields = document.getElementById('dynamicUserAuthenticationFields');
  if(dynamicUserAuthenticationFields.style.display == 'none') {
    symbolMax.setAttribute('style','background-image:url(images/minus.gif);');
    dynamicUserAuthenticationFields.style.display = '';
  } else {
      symbolMax.setAttribute('style','background-image:url(images/plus.gif);');
      dynamicUserAuthenticationFields.style.display = 'none';
  }
}

function showDynamicAuthorizationProviderConfigurations() {
    var symbolMax =  document.getElementById('symbolMax');
    var authorizationProviderConfigFields = document.getElementById('authorizationProviderConfigFields');
    if(authorizationProviderConfigFields.style.display == 'none') {
        symbolMax.setAttribute('style','background-image:url(images/minus.gif);');
        authorizationProviderConfigFields.style.display = '';
    } else {
        symbolMax.setAttribute('style','background-image:url(images/plus.gif);');
        authorizationProviderConfigFields.style.display = 'none';
    }
}

function showAdvancedRDBMSConfigurations() {
  var pwdMngrSymbolMax =  document.getElementById('pwdMngrSymbolMax');
  var advancedConfigFields = document.getElementById('advancedConfigFields');
  if(advancedConfigFields.style.display == 'none') {
    pwdMngrSymbolMax.setAttribute('style','background-image:url(images/minus.gif);');
    advancedConfigFields.style.display = '';
  } else {
      pwdMngrSymbolMax.setAttribute('style','background-image:url(images/plus.gif);');
      advancedConfigFields.style.display = 'none';
  }
}

function deleteNewPropertyField(i) {
    var propertyNameRaw = document.getElementById("propertyNameRaw" + i);
    if (propertyNameRaw != undefined && propertyNameRaw != null) {
        var parentTBody = propertyNameRaw.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propertyNameRaw);
        }
    }
}

function setSQLDialectDriverPrefix()
{
  var txtSelectedValuesObj = document.getElementById('txSQLDialect');
  var selectedArray = new Array();
  var selObj = document.getElementById('sqlDialectId');
  var i;
  var count = 0;
  for (i=0; i<selObj.options.length; i++) {
    if (selObj.options[i].selected) {
      selectedArray[count] = selObj.options[i].value;
      count++;
    }
  }
  txtSelectedValuesObj.value = selectedArray;
}

function deleteOperationParameters(name, operationName, oldOperationName, queryId, operationDesc, enableStreaming , action){
    function forwardToDel(){
        document.location.href = 'operationProcessor.jsp?flag=delete&paramName=' + name + '&action=' +action + '&operationName='+operationName +'&oldOperationName='+ oldOperationName + '&queryId='+ queryId + '&operationDesc='+operationDesc + '&enableStreaming='+enableStreaming;
    }
    CARBON.showConfirmationDialog('Do you want to delete the operation parameter '+name+'?', forwardToDel);
}

function validateOperationParamForm(){
	var paramName = document.getElementById('paramNameId').value;
	var operationParamName = document.getElementById('operationParamNameId').value;
	if (paramName == '') {
		  CARBON.showWarningDialog("Specify Param Name");
	      return false;
	}
	if (operationParamName == '' || trim(operationParamName) == '') {
		  CARBON.showWarningDialog("Specify Operation Param Name");
	      return false;
	}
	return true;
}

function changeVisiblityOnTypeSelection(obj, document) {
    var selectedSqlType = obj[obj.selectedIndex].value;
    if (selectedSqlType == 'STRUCT' || selectedSqlType == 'ARRAY') {
        document.getElementById('defaultValueRow').style.display = 'none';
        document.getElementById('structTypeRow').style.display = ''
    } else {
        document.getElementById('defaultValueRow').style.display = '';
        document.getElementById('structTypeRow').style.display = 'none'
    }
}

function adjustParameterType(obj, document) {
    var selectedSqlType = obj[obj.selectedIndex].value;
    var arrayOptionElement = document.getElementById("paramTypeArrayOptionId");
    if (selectedSqlType == 'QUERY_STRING') {
        arrayOptionElement.disabled = true;
        var parameterTypeElement = document.getElementById("paramTypeId");
        if (parameterTypeElement.value == "ARRAY") {
            parameterTypeElement.value = "SCALAR"
        }
    } else {
        arrayOptionElement.disabled = false;
    }
}

function ValidateDataSourceProperties() {
	if (isNaN(document.getElementById("maxActive").value) || document.getElementById("maxActive").value < 0) {
		CARBON.showErrorDialog("Please enter a positive numeric value for Max Active");
		return false;
	}
	if (isNaN(document.getElementById("maxIdle").value) || document.getElementById("maxIdle").value < 0) {
		CARBON.showErrorDialog("Please enter a positive numeric value for Max Idle");
		return false;
	}
	if (isNaN(document.getElementById("minIdle").value) || document.getElementById("minIdle").value < 0) {
		CARBON.showErrorDialog("Please enter a positive numeric value for Min Idle");
		return false;
	}
	if (isNaN(document.getElementById("initialSize").value) || document.getElementById("initialSize").value < 0) {
		CARBON.showErrorDialog("Please enter a positive numeric value for Initial Size");
		return false;
	}
	if (isNaN(document.getElementById("maxWait").value) || document.getElementById("maxWait").value < 0) {
		CARBON.showErrorDialog("Please enter a positive numeric value for Max Wait");
		return false;
	}
	if (isNaN(document.getElementById("timeBetweenEvictionRunsMillis").value) || document.getElementById("timeBetweenEvictionRunsMillis").value < 0) {
		CARBON.showErrorDialog("Please enter a positive numeric value for Time Between Eviction Runs Millis");
		return false;
	}
	if (isNaN(document.getElementById("numTestsPerEvictionRun").value) || document.getElementById("numTestsPerEvictionRun").value < 0 ) {
		CARBON.showErrorDialog("Please enter a positive numeric value for Num Tests Per Eviction Run");
		return false;
	}
	if (isNaN(document.getElementById("minEvictableIdleTimeMillis").value) || document.getElementById("minEvictableIdleTimeMillis").value < 0) {
		CARBON.showErrorDialog("Please enter a positive numeric value for Min Evictable Idle Time Millis");
		return false;
	}
	if (isNaN(document.getElementById("removeAbandonedTimeout").value) || document.getElementById("removeAbandonedTimeout").value < 0) {
		CARBON.showErrorDialog("Please enter a positive numeric value for Remove Abandoned Timeout");
		return false;
	}
	if (isNaN(document.getElementById("validationInterval").value) || document.getElementById("validationInterval").value < 0) {
		CARBON.showErrorDialog("Please enter a positive numeric value for Validation Interval");
		return false;
	}
	if (isNaN(document.getElementById("abandonWhenPercentageFull").value) || document.getElementById("abandonWhenPercentageFull").value < 0) {
		CARBON.showErrorDialog("Please enter a positive numeric value for Abandon When Percentage Full");
		return false;
	}
	if (isNaN(document.getElementById("maxAge").value) || document.getElementById("maxAge").value < 0) {
		CARBON.showErrorDialog("Please enter a positive numeric value for Max Age");
		return false;
	}
	if (isNaN(document.getElementById("suspectTimeout").value) || document.getElementById("suspectTimeout").value < 0) {
		CARBON.showErrorDialog("Please enter a positive numeric value for Suspect Timeout");
		return false;
	}
	if (isNaN(document.getElementById("validationQueryTimeout").value) || document.getElementById("validationQueryTimeout").value < 0) {
    	CARBON.showErrorDialog("Please enter a positive numeric value for Validation Query Timeout");
    	return false;
    }
	return true;
}

function showGsExcelProperties(checkbx, dsType) {
	if(checkbx.checked) {
		if (dsType == 'GDATA_SPREADSHEET') {
			document.getElementById("sheetNameTr").style.display = '';
		}
		document.getElementById("useQueryModeValue").value = "true";
	} else {
		document.getElementById("sheetNameTr").style.display = 'none';
		document.getElementById("useQueryModeValue").value = "false";
	}
}

function hasHeaderOnChange(obj, document) {
	var selectedValue = obj[obj.selectedIndex].value;
	var comboId = obj.id;
    dval = null;
    if (selectedValue == "true") {
        dval = "table-row";
    } else {
        dval = "none";
    }
    if (comboId == "txtExcelHeaderColumns") {
        document.getElementById('tr:excel.header.row').style.display = dval;
    } else if (comboId == "txtGSpreadHeaderColumns") {
        document.getElementById('tr:gspred.header.row').style.display = dval;
    }
}

function changeCustomDsType() {
	if (document.getElementById('custom_tabular').checked) {
		document.getElementById('customTypeValue').value = "CUSTOM_TABULAR";
	} else {
		document.getElementById('customTypeValue').value = "CUSTOM_QUERY";
	}
}


function reDirectToConsent() {
    var data = getData();

    jQuery.ajax({
               url: '/consentUrl',
               type: 'POST',
               async: false,
               cache: false,
               data: JSON.stringify(data),
               processData: false,
               timeout: 5000,
               error: function () {
                   status = true;
               },
               contentType: 'application/json',
               statusCode: {
                   400: function (response) {
                       alert(response.responseText);
                   },
                   500: function (response) {
                       alert(response.responseText);
                   }
               },
               success: function (msg) {
                   msg = msg.toString().trim();
                   OpenInNewTab(msg);
                   setTimeout(getStatus, 5000);
               }
           });

    return false;
}

function getData() {
    var data = {};
    data["gspread_client_id"] = document.getElementById("gspread_client_id").value.trim();
    data["gspread_client_secret"] = document.getElementById("gspread_client_secret").value.trim();
    data["gspread_redirect_uris"] = document.getElementById("gspread_redirect_uris").value.trim();
    document.getElementById("gspread_client_id").value = data["gspread_client_id"];
    document.getElementById("gspread_client_secret").value = data["gspread_client_secret"];
    document.getElementById("gspread_redirect_uris").value = data["gspread_redirect_uris"];
    return data;
}

function OpenInNewTab(url) {
    var win = window.open(url, '_blank');
    win.focus();
}

function setTokensToPage(jsonObj) {
    document.getElementById("gspread_refresh_token").value = jsonObj["gspread_refresh_token"];
}

function getStatus() {
    var data = getData();
    jQuery.ajax({
               url: '/tokenEndpoint',
               type: 'POST',
               async: false,
               cache: false,
               data: JSON.stringify(data),
               processData: false,
               timeout: 5000,
               error: function () {
                   status = true;
               },
               contentType: 'application/json',
               statusCode: {
                   400: function (response) {
                       alert(response.responseText);
                   },
                   500: function (response) {
                       alert(response.responseText);
                   }
               },
               success: function (msg) {
                   msg = msg.toString().trim();
                   var obj = jQuery.parseJSON(msg);
                   if (!obj['gspread_access_token']) {
                       setTimeout(getStatus, 5000);
                   } else {
                       setTokensToPage(obj)
                   }
               }
           });
}

function addValidatorProperties() {
    //check to see if there are empty fields left
    var theTable = document.getElementById('dsValidatorPropertyTable');
    var inputs = theTable.getElementsByTagName('input');
    for (var i = 0; i < inputs.length; i++) {
        if (inputs[i].value == "") {
            CARBON.showErrorDialog("Cannot add a property with empty key or value. Please specify a key and a value");
            return;
        }
    }
    addValidatorServiceParamRow("", "", "dsValidatorPropertyTable", "deleteValidatorPropRow");
    if (document.getElementById('dsValidatorPropertyTable').style.display == "none") {
        document.getElementById('dsValidatorPropertyTable').style.display = "";
    }
}

function deleteValidatorPropRow(index) {
    CARBON.showConfirmationDialog('Do you want to delete the Validator Property?', function () {
        document.getElementById('dsValidatorPropertyTable').deleteRow(index);
        if (document.getElementById('dsValidatorPropertyTable').rows.length == 1) {
            document.getElementById('dsValidatorPropertyTable').style.display = 'none';
        }
    });
}

function addValidatorServiceParamRow(key, value, table, delFunction) {
    var tableElement = document.getElementById(table);
    var param1Cell = document.createElement('td');
    var inputElem = document.createElement('input');
    inputElem.type = "text";
    inputElem.name = "spName";
    inputElem.value = key;
    param1Cell.appendChild(inputElem); //'<input type="text" name="spName" value="'+prop1+' />';

    var param2Cell = document.createElement('td');
    inputElem = document.createElement('input');
    inputElem.type = "text";
    inputElem.type = "text";
    inputElem.name = "spValue";
    inputElem.value = value;
    param2Cell.appendChild(inputElem);

    var delCell = document.createElement('td');
    delCell.innerHTML = '<a id="deleteLink" href="#" onClick="' + delFunction + '(this.parentNode.parentNode.rowIndex)" alt="Delete" class="icon-link" style="background-image:url(../admin/images/delete.gif);">Delete</a>';

    var rowtoAdd = document.createElement('tr');
    rowtoAdd.appendChild(param1Cell);
    rowtoAdd.appendChild(param2Cell);
    rowtoAdd.appendChild(delCell);

    tableElement.tBodies[0].appendChild(rowtoAdd);
    tableElement.style.display = "";
}

function extractDataSourceProps(document) {
    var i;
    var str = '';
    var dsPropertyTable = document.getElementById("dsValidatorPropertyTable");
    for (var j = 1; j < dsPropertyTable.rows.length; j++) {
        var parmName = dsPropertyTable.rows[j].getElementsByTagName("input")[0].value;
        var parmValue = dsPropertyTable.rows[j].getElementsByTagName("input")[1].value;
        if (parmName == "" || parmValue == "") {
            return;
        }
        if (j == 1) {
            str += parmName + ',' + parmValue;
        } else {
            str += '::' + parmName + ',' + parmValue;
        }
    }
    document.getElementById("dsValidatorProperties").value = str;
}

function addValidators() {
    extractDataSourceProps(document);
    var str = document.getElementById('dsValidatorProperties').value;
    document.getElementById('inputMappings').action = 'inputMappingProcessor.jsp?flag=validate&dsValidatorProperties=' + str;
}

function addValidatorsForSparqlInput() {
    extractDataSourceProps(document);
    var str = document.getElementById('dsValidatorProperties').value;
    document.getElementById('sparqlInputMappings').action = 'sparqlInputMappingProcessor.jsp?flag=validate&dsValidatorProperties=' + str;
}

function validateQuery(value) {
    var regex = /<\/textarea>/i;
    if (regex.test(value)) {
        CARBON.showWarningDialog("Invalid Query");
        return false;
    }
    return true;
}
