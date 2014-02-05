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
package org.wso2.carbon.dataservices.core.engine;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.types.NCName;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.DBSFields;
import org.wso2.carbon.dataservices.common.DBConstants.FaultCodes;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.description.query.Query;

import javax.xml.stream.XMLStreamWriter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A call-query is an expression which leads to the execution of a query.
 */
public class CallQuery extends OutputElement {

	private DataService dataService;

	private String queryId;

	private Query query;

	/* key - target query's query-param name, value - withparam */
    private Map<String, WithParam> withParams;

	public CallQuery(DataService dataService, String queryId, Map<String, WithParam> withParams,
			Set<String> requiredRoles) {
        super(null, requiredRoles);
		this.dataService = dataService;
		this.queryId = queryId;
		this.withParams = withParams;
	}

	public void init() throws DataServiceFault {
		this.query = this.getDataService().getQuery(this.getQueryId());
        this.setNamespace(this.getQuery().getNamespace());
		if (this.query == null) {
			throw new DataServiceFault(
					"Query with the query id: '" + this.getQueryId() + "' cannot be found");
		}
	}
	
	public Map<String, WithParam> getWithParams() {
		return withParams;
	}

	public DataService getDataService() {
		return dataService;
	}

	public String getQueryId() {
		return queryId;
	}

	public Query getQuery() {
		return query;
	}

	/**
	 * This method returns the system variable's value given the property name.
	 */
	private Object evaluateGetProperty(String propName) throws DataServiceFault {
		/* so far we only evaluate the "USERNAME" value */
		if ("USERNAME".equals(propName)) {
			MessageContext context = MessageContext.getCurrentMessageContext();
			if (context != null) {
				Object val = context.getProperty("username");
				if (val != null) {
					return val.toString();
				}
			}
		} else if ("TENANT_ID".equals(propName)) {
			return String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
		} else if ("USER_ROLES".equals(propName)) {
			MessageContext context = MessageContext.getCurrentMessageContext();
			if (context != null) {
				return DBUtils.getUserRoles(context);
			}
		} else if ("NULL".equals(propName)) {
			/* represent the special null value (not empty string) */
			return null;
		}
		return null;
	}

	/**
	 * This method evaluates the content of the 'defaultValue' field.
	 * The default value field can simply contain a string value, or it can
	 * contain a reference to an system variable such as the current user's username,
	 * e.g. #{USERNAME}.
	 */
	private ParamValue evaluateDefaultValue(ParamValue paramValue, String paramType)
			throws DataServiceFault {
		if (paramValue.getValueType() != ParamValue.PARAM_VALUE_SCALAR) {
			return paramValue;
		}
		String value = paramValue.getScalarValue();
		Object evaluatedValue;
		if (value != null && value.startsWith("#{") && value.endsWith("}")) {
			String propName = value.substring(2, value.length() - 1).trim();
			evaluatedValue = this.evaluateGetProperty(propName);
		} else {
			evaluatedValue = value;
		}
		ParamValue result;
		if (DBConstants.QueryParamTypes.ARRAY.equals(paramType)) {
			result = new ParamValue(ParamValue.PARAM_VALUE_ARRAY);
			if (evaluatedValue instanceof ParamValue[]) {
				result.setArrayValue(Arrays.asList((ParamValue[]) evaluatedValue));
			} else {
				result.addArrayValue(
                        evaluatedValue == null ? null : new ParamValue(evaluatedValue.toString()));
			}
		} else {
			result = new ParamValue(ParamValue.PARAM_VALUE_SCALAR);
			/* if the expected value is a scalar, and we have an array,
			   only set the first element as the value */
			if (evaluatedValue instanceof String[]) {
				String[] tmpArray = (String[]) evaluatedValue;
				String tmpVal = null;
				if (tmpArray.length > 0) {
					tmpVal = tmpArray[0];
				}
				result.setScalarValue(tmpVal);
			} else {
				result.setScalarValue(evaluatedValue == null ? null : evaluatedValue.toString());
			}
		}
		return result;
	}

    private void processDefaultValues(ExternalParamCollection params) throws DataServiceFault {
        List<QueryParam> queryParams = this.getQuery().getQueryParams();
        for (QueryParam queryParam : queryParams) {
            if (queryParam.getDefaultValue() != null) {
                params.addTempParam(queryParam.getName(),
                		this.evaluateDefaultValue(queryParam.getDefaultValue(),
                                queryParam.getParamType()));
            }
        }
    }

    @Override
    protected void executeElement(XMLStreamWriter xmlWriter, ExternalParamCollection params,
                                  int queryLevel, boolean escapeNonPrintableChar) throws DataServiceFault {
         /* handle default values */
        this.processDefaultValues(params);
		/* convert/filter params according to the WithParams */
		Map<String, ParamValue> qparams = extractParams(params);
		/* execute query */
		this.getQuery().execute(xmlWriter, qparams, queryLevel);
		/* clear temp values */
		params.clearTempValues();
    }

    /**
	 * Convert's a call-query's ExternalParams to parameters (parameter map)
	 * that can be passed into actual query objects, by making necessary
	 * transformation as instructed by with-param elements.
	 */
	private Map<String, ParamValue> extractParams(ExternalParamCollection params)
			throws DataServiceFault {
		Map<String, ParamValue> qparams = new HashMap<String, ParamValue>();
		ExternalParam paramObj;
		String paramType, paramName;
		for (WithParam withParam : this.getWithParams().values()) {
			paramName = withParam.getParam();
			paramType = withParam.getParamType();
			paramObj = params.getParam(paramType, paramName);
			/* workaround for users using 'column' and 'query-param' as the same */
			if (paramObj == null) {
				paramObj = params.getParam(paramName);
			}
			if (paramObj != null) {
			    qparams.put(withParam.getName(), paramObj.getValue());
			} else if (params.getTempEntries().containsKey(withParam.getName())) {
				/* this means the query param will be added later by the default values */
				continue;
			} else {
				throw new DataServiceFault(FaultCodes.INCOMPATIBLE_PARAMETERS_ERROR,
						"Error in 'CallQuery.extractParams', cannot find parameter with type:" +
						paramType + " name:" + withParam.getOriginalName());
			}
		}
		/* add the tmp params, required for default values etc.. */
		String key;
		for (Entry<String, ParamValue> entry: params.getTempEntries().entrySet()) {
			key = entry.getKey();
			/* only put it, if it is not already there */
			if (!qparams.containsKey(key)) {
			    qparams.put(key, entry.getValue());
			}
		}
		return qparams;
	}

	/**
	 * This class represents a "with-param" element in a call-query.
	 */
	public static class WithParam {

		private String name;

		/**
		 * Original name is the initial value, without making changes to it,
		 * i.e. making it lower-case.
		 */
		private String originalName;

		private String param;

		private String paramType;

		public WithParam(String name, String originalName, String param,
				String paramType) throws DataServiceFault {
			this.name = name;
			this.originalName = originalName;
			this.param = param;
			this.paramType = paramType;
			this.validateWithParam();
		}

		private void validateWithParam() throws DataServiceFault {
			/* validate name, should be an NCName */
			if (DBSFields.QUERY_PARAM.equals(this.getParamType()) &&
					!NCName.isValid(this.getParam())) {
				throw new DataServiceFault("Invalid query param name: '" + this.getParam() +
						"', must be an NCName.");
			}
		}

		public String getName() {
			return name;
		}

		public String getOriginalName() {
			return originalName;
		}

		public String getParam() {
			return param;
		}

		public String getParamType() {
			return paramType;
		}
		
	}
	
}
