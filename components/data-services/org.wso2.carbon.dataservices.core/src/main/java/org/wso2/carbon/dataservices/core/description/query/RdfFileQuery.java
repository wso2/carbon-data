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

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.description.config.RDFConfig;
import org.wso2.carbon.dataservices.core.description.event.EventTrigger;
import org.wso2.carbon.dataservices.core.engine.*;

import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This class represents a SPARQL data services query over a single RDF file
 */
public class RdfFileQuery extends SparqlQueryBase {

	private RDFConfig config;
	
	public RdfFileQuery(DataService dataService, String queryId,
			String configId, String query, List<QueryParam> queryParams,
			Result result, EventTrigger inputEventTrigger,
			EventTrigger outputEventTrigger,
			Map<String, String> advancedProperties,
			String inputNamespace) throws DataServiceFault {
		super(dataService, queryId, configId, query, queryParams, result,
				inputEventTrigger, outputEventTrigger, advancedProperties,
				inputNamespace);
		try {
			this.config = (RDFConfig) this.getDataService().getConfig(
					this.getConfigId());
		} catch (ClassCastException e) {
			throw new DataServiceFault(e, "Configuration is not a RDF config:"
					+ this.getConfigId());
		}

	}	

	public RDFConfig getConfig() {
		return config;
	}

	@Override
	public QueryExecution getQueryExecution() throws IOException, DataServiceFault {
		return QueryExecutionFactory.create(this.getQuery(), this.config.createRDFModel());
	}

    public void processQuery(XMLStreamWriter xmlWriter,
			InternalParamCollection params, int queryLevel) throws DataServiceFault {
		try {
			QuerySolutionMap queryMap = new QuerySolutionMap();
			Model model = this.getModelForValidation();
			/* process the query params */
			for (InternalParam param : params.getParams()) {
				/* set parameters to the query map */
				queryMap.add(param.getName(), convertTypeLiteral(model, param));
			}

			QueryExecution qe = this.getQueryExecution();
            qe.setInitialBinding(queryMap) ;

			/* execute query as a select query */
			ResultSet results = qe.execSelect();
			DataEntry dataEntry;
			while (results.hasNext()) {
				dataEntry = this.getDataEntryFromRS(results);
				this.writeResultEntry(xmlWriter, dataEntry, params, queryLevel);
			}
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error in 'SparqlQueryBase.processQuery'");
		}
	}

}
