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

import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.DBSFields;
import org.wso2.carbon.dataservices.common.DBConstants.FaultCodes;
import org.wso2.carbon.dataservices.common.DBConstants.QueryParamTypes;
import org.wso2.carbon.dataservices.common.DBConstants.QueryTypes;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.TLConnectionStore;
import org.wso2.carbon.dataservices.core.boxcarring.TLParamStore;
import org.wso2.carbon.dataservices.core.description.event.EventTrigger;
import org.wso2.carbon.dataservices.core.dispatch.DispatchStatus;
import org.wso2.carbon.dataservices.core.engine.*;
import org.wso2.carbon.dataservices.core.validation.ValidationContext;
import org.wso2.carbon.dataservices.core.validation.ValidationException;
import org.wso2.carbon.dataservices.core.validation.Validator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import java.util.*;

/**
 * Represents a query in a data service.
 */
public abstract class Query extends XMLWriterHelper {

	private String queryId;
	
	private List<QueryParam> queryParams;
	
	private DataService dataService;
	
	private Result result;
	
	private boolean writeRow;
	
	private String configId;
	
	private EventTrigger inputEventTrigger;
	
	private EventTrigger outputEventTrigger;
	
	private Map<String, String> advancedProperties;
	
	private String inputNamespace;
	
	/* this is set to true if the result has to be built before sending, 
	 * requires for xslt transformation / eventing */
	private boolean preBuildResult;
	
	private boolean useColumnNumbers;

	private static ThreadLocal<Object> queryPreprocessObjects = new ThreadLocal<Object>() {
	    @Override
	    public Object initialValue() {
	        return new Object();
	    }
	};

	private static ThreadLocal<Boolean> queryPreprocessInitial = new ThreadLocal<Boolean>() {
	    @Override
        public Boolean initialValue() {
            return false;
        }
	};

	private static ThreadLocal<Boolean> queryPreprocessSecondary = new ThreadLocal<Boolean>() {
        @Override
        public Boolean initialValue() {
            return false;
        }
    };

	public Query(DataService dataService, String queryId,
			List<QueryParam> queryParams, Result result, String configId,
			EventTrigger inputEventTrigger, EventTrigger outputEventTrigger, 
			Map<String, String> advancedProperties, String inputNamespace) {
		super(result != null ? result.getNamespace() : dataService.getDefaultNamespace());
		this.dataService = dataService;
		this.queryId = queryId;
		this.queryParams = queryParams;
		this.result = result;
		this.writeRow = this.getResult() != null && this.getResult().getRowName() != null && 
				this.getResult().getRowName().trim().length() > 0;
		this.configId = configId;
		this.inputEventTrigger = inputEventTrigger;
		this.outputEventTrigger = outputEventTrigger;
		this.advancedProperties = advancedProperties;
		this.inputNamespace = inputNamespace;
		this.preBuildResult = this.checkPreBuildResult();
		if (result != null) {
			useColumnNumbers = result.isUseColumnNumbers();
		}
	}
	
	private boolean checkPreBuildResult() {
		return this.getOutputEventTrigger() != null	|| 
		           (this.getResult() != null && 
				    this.getResult().getXsltTransformer() != null);
	}
	
	public String getInputNamespace() {
		return inputNamespace;
	}
	
	public boolean isPreBuildResult() {
		return preBuildResult;
	}
	
	public EventTrigger getInputEventTrigger() {
		return inputEventTrigger;
	}

	public EventTrigger getOutputEventTrigger() {
		return outputEventTrigger;
	}
	
	public Map<String, String> getAdvancedProperties() {
		return advancedProperties;
	}

	public String getConfigId() {
		return configId;
	}
			
	public DataService getDataService() {
		return dataService;
	}

	public String getQueryId() {
		return queryId;
	}
	
	public List<QueryParam> getQueryParams() {
		return queryParams;
	}
	
	public Result getResult() {
		return result;
	}
	
	public boolean hasResult() {
		return this.getResult() != null;
	}
	
	public boolean isWriteRow() {
		return writeRow;
	}
	
	public boolean isUsingColumnNumbers() {
		return useColumnNumbers;
	}

	/**
	 * Converts the parameter map passed into the query, to InternalParam objects,
	 * where they are created by taking in information also that is mentioned in
	 * QueryParams - "param" elements in the "query" element.
	 */
	private InternalParamCollection extractParams(Map<String, ParamValue> params)
			throws DataServiceFault {
		InternalParamCollection ipc = new InternalParamCollection();
		/* exported values from earlier queries */
		Map<String, ParamValue> exportedParams = TLParamStore.getParameterMap();
		ParamValue tmpParamValue;
		for (QueryParam queryParam : this.getQueryParams()) {
			tmpParamValue = params.get(queryParam.getName());			
			if (tmpParamValue == null) {
				if (!(QueryTypes.OUT.equals(queryParam.getType()) || 
						QueryTypes.INOUT.equals(queryParam.getType()))) {
					/* check the exported values */
					tmpParamValue = exportedParams.get(queryParam.getName());
					if (tmpParamValue == null && !queryParam.hasDefaultValue()) {
						/* still can't find, throw an exception */
						throw new DataServiceFault(FaultCodes.INCOMPATIBLE_PARAMETERS_ERROR,
								"Error in 'Query.extractParams', " +
								"cannot find query param with name:" + queryParam.getName());
					}
				}
			}
			for (int ordinal : queryParam.getOrdinals()) {
			    ipc.addParam(new InternalParam(queryParam.getName(), tmpParamValue,
                        queryParam.getSqlType(), queryParam.getType(), queryParam.getStructType(),
                        ordinal));
			}
		}
		return ipc;
	}
	
	/**
	 * Check the params to see if a scalar param needs to be converted 
	 * to an array type which has one element.
	 */
	private void preprocessParams(Map<String, ParamValue> params) {
		ParamValue value = null;
		for (QueryParam queryParam : this.getQueryParams()) {
            if (!queryParam.getType().equals(QueryTypes.OUT)) {
            	/* convert from scalar to array, if required */
                if (queryParam.getParamType().equals(QueryParamTypes.ARRAY)) { 
    				value = params.get(queryParam.getName());
    				if (value != null) {
    					if (value.getValueType() == ParamValue.PARAM_VALUE_SCALAR) {
    						/* replace the existing scalar with the array value */
    						params.put(queryParam.getName(), 
    								ParamValue.convertFromScalarToArray(value));
    					}
    				}
    			}
            }            
        }
	}

	private ValidationContext createValidationContext(Map<String, ParamValue> params) {
		return new ValidationContext(params);
	}
	
	private void validateParams(Map<String, ParamValue> params) throws DataServiceFault {
		try {
			ParamValue value;
			ValidationContext context = this.createValidationContext(params);
			for (QueryParam queryParam : this.getQueryParams()) {
				value = params.get(queryParam.getName());
				if (value != null) {
					for (Validator validator : queryParam.getValidators()) {
						validator.validate(context, queryParam.getName(), value);
					}
				}
			}
		} catch (ValidationException e) {
			throw new DataServiceFault(e, FaultCodes.VALIDATION_ERROR, null);
		}
	}

	public void execute(XMLStreamWriter xmlWriter, Map<String, ParamValue> params, 
			int queryLevel) throws DataServiceFault {
		/* pre-process parameters as needed */
		this.preprocessParams(params);
		/* extract parameters, to be used internally in queries */
		InternalParamCollection internalParams = this.extractParams(params);
		boolean error = true;
        Object result;
        try {
            boolean initial = Query.isQueryPreprocessInitial();
            boolean secondary = Query.isQueryPreprocessSecondary();
            /* write the content */
            if (initial) {
                /* validate params */
                this.validateParams(params);
                /* check user role based content filtering */
                this.processContentFiltering();
                /* process input events */
                this.processInputEvents(internalParams);
                result = this.runPreQuery(internalParams, queryLevel);
                Query.addQueryPreprocessedObject(result);
            }
            if (secondary) {
                /* required for nested query processing, nested queries
                 * must execute both phases at once */
                Query.setQueryPreprocessingInitial(true);
                result = Query.getAndRemoveQueryPreprocessObject();
                this.runPostQuery(result, xmlWriter, internalParams, queryLevel);
            }
            error = false;
        } finally {
            if (error || (queryLevel == 0 && isQueryPreprocessSecondary())
                    || (isQueryPreprocessInitial() && !this.hasResult())) {
                /* we are at the end of the outer most query, i.e. in nested query situations,
                 * and we are not in the data pre-fetching state */
                this.finalizeTx(error);
            }
        }
	}

    private void processContentFiltering() throws DataServiceFault {
        if (this.hasResult()) {
			/* set required roles in result */
            if (DataService.getCurrentUser() != null) {
                this.getResult().applyUserRoles(DataService.getCurrentUser().getUserRoles());
            } else {
                this.getResult().applyUserRoles(null);
            }
        }
    }
	
	private void finalizeTx(boolean error) {
	    if (DispatchStatus.isInBatchBoxcarring()) {
	        return;
	    }
        if (error) {
            if (this.getDataService().isInDTX()) {
                TLConnectionStore.rollbackNonXAConns();
                TLConnectionStore.closeAll();
            } else {
                TLConnectionStore.rollbackAll();
                TLConnectionStore.closeAll();
            }
        } else {
	        if (this.getDataService().isInDTX()) {
	            TLConnectionStore.commitNonXAConns();
	        } else {
	            TLConnectionStore.commitAll();
	        }
	        TLConnectionStore.closeAll();
        }
	}
	
	private OMElement createOMElementFromInputParams(InternalParamCollection params) {
		OMFactory fac = DBUtils.getOMFactory();
		OMDocument doc = fac.createOMDocument();
		OMElement retEl = fac.createOMElement(new QName(this.getQueryId()));
		OMElement scalarEl;
		List<OMElement> arrayEl;
		ParamValue paramValue;
		for (InternalParam param : params.getParams()) {
			paramValue = param.getValue();
			if (paramValue.getValueType() == ParamValue.PARAM_VALUE_SCALAR ||
					paramValue.getValueType() == ParamValue.PARAM_VALUE_UDT) {
				scalarEl = fac.createOMElement(new QName(param.getName()));
				scalarEl.setText(paramValue.getScalarValue());
				retEl.addChild(scalarEl);
			} else if (paramValue.getValueType() == ParamValue.PARAM_VALUE_ARRAY) {
				arrayEl = this.createOMElementsFromArrayValue(param.getName(), paramValue, fac);
				for (OMElement el : arrayEl) {
					retEl.addChild(el);
				}
			}
		}
		doc.addChild(retEl);
		return doc.getOMDocumentElement();
	}
	
	private List<OMElement> createOMElementsFromArrayValue(String name, ParamValue value, 
			OMFactory fac) {
		List<OMElement> arrayEl = new ArrayList<OMElement>();
		List<ParamValue> strVals = value.getArrayValue();
		OMElement el;
		for (ParamValue val : strVals) {
			el = fac.createOMElement(new QName(name));
			el.setText(val.toString());
			arrayEl.add(el);
		}
		return arrayEl;
	}
	
	private void processInputEvents(InternalParamCollection params) throws DataServiceFault {
		EventTrigger inputEventTrigger = this.getInputEventTrigger();
		if (inputEventTrigger != null) {
			OMElement input = this.createOMElementFromInputParams(params);
			inputEventTrigger.execute(input, this.getQueryId());
		}
	}
	
	/**
	 * This method must be implemented by concrete implementations of this class,
	 * to provide the logic to execute the query.
	 */
	public abstract Object runPreQuery(InternalParamCollection params,
			int queryLevel) throws DataServiceFault;

    /**
     * This method must be implemented by concrete implementations of this class,
     * to provide the logic to execute the query.
     */
    public abstract void runPostQuery(Object result, XMLStreamWriter xmlWriter, InternalParamCollection params,
                                  int queryLevel) throws DataServiceFault;
	
	/**
	 * writes an result entry to the output.
	 */
	public void writeResultEntry(XMLStreamWriter xmlWriter, DataEntry dataEntry, 
			InternalParamCollection ipc, int queryLevel) throws DataServiceFault {
		/* increment query level */
		queryLevel++;
		
		/* populate params, here an ExternalParamCollection is created from the
		 * passed data and the internal parameters. This is done because, again,
		 * output elements are simply provided with ExternalParam object for their values 
		 * to be outputted. Output elements include, static elements and other call-query
		 * object itself, where call-queries are used for nested queries. */
		ExternalParamCollection params = this.createExternalParamCollection(dataEntry, ipc);
		
		/* write result wrapper */
		if (this.isWriteRow()) {
			try {
		        this.startRowElement(xmlWriter, 
		        		this.getResult().getRowName(), 
		        		this.getResult().getResultType(), this.getResult(), params);
			} catch (XMLStreamException e) {
				throw new DataServiceFault(e, 
						"Error in start write row at Query.writeResultEntry");
			}
		}
		/* write the result */
		this.getResult().getDefaultElementGroup().execute(xmlWriter, params, queryLevel, this.getResult().isEscapeNonPrintableChar());
		/* end result wrapper */
		if (this.isWriteRow()) {
			try {
		        this.endElement(xmlWriter);
			} catch (XMLStreamException e) {
				throw new DataServiceFault(e, "Error in end write row at Query.writeResultEntry");
			}
		}
	}
	
	private ExternalParamCollection createExternalParamCollection(DataEntry dataEntry, 
			InternalParamCollection queryParams) {
		ExternalParamCollection pc = new ExternalParamCollection();
		/* 'toLowerCase' - workaround for different character case issues in column names */
		for (String name : dataEntry.getNames()) {
			pc.addParam(new org.wso2.carbon.dataservices.core.engine.ExternalParam(
					name.toLowerCase(), dataEntry.getValue(name), DBSFields.COLUMN));
		}
		for (InternalParam iParam : queryParams.getParams()) {
			pc.addParam(new org.wso2.carbon.dataservices.core.engine.ExternalParam(
					iParam.getName().toLowerCase(), iParam.getValue(), DBSFields.QUERY_PARAM));
		}
		return pc;
	}

	public static void setQueryPreprocessingInitial(boolean state) {
	    queryPreprocessInitial.set(state);
	}

	public static void setQueryPreprocessingSecondary(boolean state) {
        queryPreprocessSecondary.set(state);
    }

	public static boolean isQueryPreprocessInitial() {
	    return queryPreprocessInitial.get();
	}

	public static boolean isQueryPreprocessSecondary() {
        return queryPreprocessSecondary.get();
    }

	public static Object getAndRemoveQueryPreprocessObject() {
        return queryPreprocessObjects.get();
	}

	public static void addQueryPreprocessedObject(Object value) {
	    queryPreprocessObjects.set(value);
	}

	public static void resetQueryPreprocessing() {
	    queryPreprocessObjects.set(new Object());
	    setQueryPreprocessingInitial(false);
	    setQueryPreprocessingSecondary(false);
	}

	/**
	 * Returns the Query manipulated to suite the given parameters, e.g. adding
	 * additional "?"'s for array types.
	 */
	protected String createProcessedQuery(String query, InternalParamCollection params,
	                                      int paramCount) {
		String currentQuery = query;
		int start = 0;
		Object[] vals;
		InternalParam param;
		ParamValue value;
		int count;
		for (int i = 1; i <= paramCount; i++) {
			param = params.getParam(i);
			value = param.getValue();
            /*
             * value can be null in stored proc OUT params, so it is simply
             * treated as a single param, because the number of elements in an
             * array cannot be calculated, since there's no actual value passed
             * in
             */
			if (value != null && (value.getValueType() == ParamValue.PARAM_VALUE_ARRAY)) {
				count = (value.getArrayValue()).size();
			} else {
				count = 1;
			}
			vals = this.expandQuery(start, count, currentQuery);
			start = (Integer) vals[0];
			currentQuery = (String) vals[1];
		}
		return currentQuery;
	}

	/**
	 * Given the starting position, this method searches for the first occurence
	 * of "?" and replace it with `count` "?"'s. Returns [0] - end position of
	 * "?"'s, [1] - modified query.
	 */
	private Object[] expandQuery(int start, int count, String query) {
		StringBuilder result = new StringBuilder();
		int n = query.length();
		int end = n;
		for (int i = start; i < n; i++) {
			if (query.charAt(i) == '?') {
				result.append(query.substring(0, i));
				result.append(this.generateQuestionMarks(count));
				end = result.length() + 1;
				if (i + 1 < n) {
					result.append(query.substring(i + 1));
				}
				break;
			}
		}
		return new Object[] { end, result.toString() };
	}

	private String generateQuestionMarks(int n) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < n; i++) {
			builder.append("?");
			if (i + 1 < n) {
				builder.append(",");
			}
		}
		return builder.toString();
	}

	/**
	 * Modifies the SQL to include the direct value of the parameters of type
	 * "QUERY_STRING"; The SQL will be recreated and the other parameters will
	 * be re-organized to point to correct ordinal values.
	 *
	 * @return [0] The updated SQL, [1] The updated parameter count
	 */
	protected Object[] processDynamicQuery(String sql, InternalParamCollection params,
	                                     int paramCount) {
		Integer[] paramIndices = this.extractQueryParamIndices(sql);
		int currentOrdinalDiff = 0;
		int currentParamIndexDiff = 0;
		InternalParam tmpParam;
		int paramIndex;
		String tmpValue;
		int resultParamCount = paramCount;
		for (int i = 1; i <= paramCount; i++) {
			tmpParam = params.getParam(i);
			if (DBConstants.DataTypes.QUERY_STRING.equals(tmpParam.getSqlType())) {
				paramIndex = paramIndices[i - 1] + currentParamIndexDiff;
				tmpValue = params.getParam(i).getValue().getScalarValue();
				currentParamIndexDiff += tmpValue.length() - 1;
				if (paramIndex + 1 < sql.length()) {
					sql = sql.substring(0, paramIndex) + tmpValue + sql.substring(paramIndex + 1);
				} else {
					sql = sql.substring(0, paramIndex) + tmpValue;
				}
				params.remove(i);
				currentOrdinalDiff++;
				resultParamCount--;
			} else {
				params.remove(i);
				tmpParam.setOrdinal(i - currentOrdinalDiff);
				params.addParam(tmpParam);
			}
		}
		return new Object[] { sql, resultParamCount };
	}

	private Integer[] extractQueryParamIndices(String sql) {
		List<Integer> result = new ArrayList<Integer>();
		char[] data = sql.toCharArray();
		for (int i = 0; i < data.length; i++) {
			if (data[i] == '?') {
				result.add(i);
			}
		}
		return result.toArray(new Integer[result.size()]);
	}


}
