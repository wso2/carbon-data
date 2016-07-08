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
package org.wso2.carbon.dataservices.ui.beans;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.wso2.carbon.dataservices.common.DBConstants.DBSFields;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;

import java.util.*;

public class Query extends DataServiceConfigurationElement {

	private String id;

	private Param[] params;

	private String sql;
	
	private String expression;

	private String sparql;

	private String configToUse = "";

	private Result result;

	private ExcelQuery excel;

	private GSpreadQuery gSpread;

	private String inputEventTrigger;

	private String outputEventTrigger;

	private String scraperVariable;

	private boolean returnGeneratedKeys;

	private boolean returnUpdatedRowCount;
	
	private String keyColumns;

	private ArrayList<Property> properties = new ArrayList<Property>();
	
	private List<SQLDialect> sqlDialects = new ArrayList<SQLDialect>();
	
	private String status = "add";

	public ArrayList<Property> getProperties() {
		return this.properties;
	}

	public void setProperties(ArrayList<Property> properties) {
		this.properties = properties;
	}

	public void addProperty(Property property) {
		addProperty(property.getName(), property.getValue());
	}

	public void addProperty(String name, Object value) {
		Property property = new Property(name, value);
		this.properties.add(property);
	}
	
	public void updateProperty(String name, String value) {
		Iterator<Property> propertyItr = properties.iterator();
		boolean updated = false;
		while (propertyItr.hasNext()) {
			Property property = propertyItr.next();
			if (property.getName().equals(name)) {
				if (value != null) {
					property.setValue(value);
				} else {
					property.setValue("");
				}
				updated = true;
				break;
			}
		}
		if (!updated) {
			this.addProperty(name, value);
		}
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Param[] getParams() {
		return params;
	}

	public void setInputEventTrigger(String inputEventTrigger) {
		this.inputEventTrigger = inputEventTrigger;
	}

	public void setOutputEventTrigger(String outputEventTrigger) {
		this.outputEventTrigger = outputEventTrigger;
	}

	public String getInputEventTrigger() {
		return this.inputEventTrigger;
	}

	public String getOutputEventTrigger() {
		return this.outputEventTrigger;
	}

	public List<SQLDialect> getSqlDialects() {
		return sqlDialects;
	}

	public void setSqlDialects(List<SQLDialect> sqlDialects) {
		this.sqlDialects = sqlDialects;
	}
	
	public void addSqlDialects(String dialect, String value) {
		SQLDialect sqlDialect = new SQLDialect(dialect, value);
		this.sqlDialects.add(sqlDialect);
	}
	
	public SQLDialect removeSQLDialect(String dialect) {
		SQLDialect sqlDialect = new SQLDialect();
		for (int a = 0; a < sqlDialects.size(); a++) {
			sqlDialect = sqlDialects.get(a);
			if (sqlDialect.getDialect().equals(dialect)) {
				sqlDialect = sqlDialects.remove(a);
			}
		}
		return sqlDialect;
	}
	
	public SQLDialect updateSQLDialect(String edit, String dialect, String sql) {
		SQLDialect sqlDialect = new SQLDialect();
		for (int a = 0; a < sqlDialects.size(); a++) {
			sqlDialect = sqlDialects.get(a);
			if (sqlDialect.getDialect().equals(edit)) {
				sqlDialects.get(a).setDialect(dialect);
				sqlDialects.get(a).setSql(sql);
			}
		}
		return sqlDialect;
	}

	public boolean hasSQLDialects() {
		boolean hasSQLDialect = false;
		Iterator<SQLDialect> iterator = this.getSqlDialects().iterator();
		for (; iterator.hasNext();) {
			SQLDialect sqlDialect = iterator.next();
			if ((sqlDialect.getDialect() != null && sqlDialect.getDialect().trim().length() > 0)
					&& ((sqlDialect.getSql() != null && sqlDialect.getSql().trim().length() > 0))) {
				hasSQLDialect = true;
				break;
			}
		}
		return hasSQLDialect;
	}
	
	public void setParams(Param[] paramsArray) {
		params = new Param[paramsArray.length];
		for (int a = 0; a < paramsArray.length; a++) {
			params[a] = paramsArray[a];
		}
	}

	public void addParam(Param newParam) {
		if (params != null) {
			Param[] newParams = new Param[params.length + 1];
			for (int a = 0; a < params.length; a++) {
				newParams[a] = params[a];
			}
			newParams[newParams.length - 1] = newParam;
			params = newParams;
		} else {
			Param[] newParams = new Param[1];
			newParams[0] = newParam;
			params = newParams;
			newParams[newParams.length - 1] = newParam;
		}
	}

	public void removeParam(Param removeParam) {
		if (params != null) {
			Param[] newParams = new Param[params.length - 1];
			ArrayList<Param> paramList = new ArrayList<Param>();
			Param param = new Param();
			paramList.add(param);
			paramList.remove(param);
			for (int a = 0; a < params.length - 1; a++) {
				newParams[a] = params[a];
			}
			if (newParams.length > 0) {
				newParams[newParams.length] = removeParam;
				params = newParams;
			}
		}
	}

    public void removeParam(String name) {
        if (params != null) {
            int paramToBeRemoved = -1;
            Param[] existingParams = params;
            Param[] modifiedParams;
            for (int i = 0; i < existingParams.length; i++) {
                if (existingParams[i].getName().equals(name)) {
                    paramToBeRemoved = i;
                    break;
                }
            }
            if (paramToBeRemoved != -1) {
                int tmpIndex = 0;
                modifiedParams = new Param[existingParams.length - 1];
                for (int i = 0; i < existingParams.length; i++) {
                    if (i != paramToBeRemoved) {
                        modifiedParams[tmpIndex] = existingParams[i];
                        tmpIndex++;
                    }
                }
                params = modifiedParams;
            }
        }
    }

	public Param getParam(String name) {
		if (params != null) {
			for (int a = 0; a < params.length; a++) {
				if (params[a].getName().equals(name)) {
					return params[a];
				}
			}
		}
		return null;
	}

	public Query() {
	}

	@SuppressWarnings("unchecked")
	private List<Validator> getValidators(Iterator<OMElement> valItr) {
		List<Validator> vals = new ArrayList<Validator>();
		OMElement valEl;
		String valElementName;
		Iterator<OMAttribute> attrItr;
		Map<String, String> propMap;
		OMAttribute attr;
		while (valItr.hasNext()) {
			valEl = valItr.next();
			valElementName = valEl.getLocalName();
			attrItr = valEl.getAllAttributes();
			propMap = new HashMap<String, String>();
			while (attrItr.hasNext()) {
				attr = attrItr.next();
				propMap.put(attr.getLocalName(), attr.getAttributeValue());
			}
            Map<String, String> customPropMap = extractAdvancedProps(valEl);
            vals.add(new Validator(valElementName, propMap, customPropMap));
        }
		return vals;
	}

	@SuppressWarnings("unchecked")
	public Query(Iterator<OMElement> paramItr) {
		Param param;
		ArrayList<Param> paramList = new ArrayList<Param>();
		int ordinal = 0;
		while (paramItr.hasNext()) {
			ordinal++;
			OMElement paramElement = paramItr.next();

			// start: work-a-round to maintain backward compatibility
			String userSetOrdinalValue = paramElement.getAttributeValue(new QName("ordinal"));
			if (userSetOrdinalValue == null || userSetOrdinalValue.trim().length() == 0) {
				userSetOrdinalValue = String.valueOf(ordinal);
			}
			// end: work-a-round to maintain backward compatibility

			param = new Param(paramElement.getAttributeValue(new QName("name")),
					paramElement.getAttributeValue(new QName("sqlType")),
					paramElement.getAttributeValue(new QName("type")), userSetOrdinalValue,
					paramElement.getAttributeValue(new QName("defaultValue")),
					this.getValidators(paramElement.getChildren()));
			paramList.add(param);
		}
		try {
			Param[] params = new Param[paramList.size()];
			paramList.toArray(params);
			// Param[] params = (Param[])paramList.toArray();
			setParams(params);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getSparql() {
		return sparql;
	}

	public String getScraperVariable() {
		return scraperVariable;
	}

	public void setScraperVariable(String scraperVariable) {
		this.scraperVariable = scraperVariable;
	}

	public void setSparql(String sparql) {
		this.sparql = sparql;
	}

	public String getConfigToUse() {
		return configToUse;
	}

	public void setConfigToUse(String configToUse) {
		this.configToUse = configToUse;
	}

	public void setExcel(ExcelQuery excel) {
		this.excel = excel;
	}

	public ExcelQuery getExcel() {
		return excel;
	}

	public boolean isReturnGeneratedKeys() {
		return returnGeneratedKeys;
	}

	public void setReturnGeneratedKeys(boolean returnGeneratedKeys) {
		this.returnGeneratedKeys = returnGeneratedKeys;
	}

	public boolean isReturnUpdatedRowCount() {
		return returnUpdatedRowCount;
	}

	public void setReturnUpdatedRowCount(boolean returnUpdatedRowCount) {
		this.returnUpdatedRowCount = returnUpdatedRowCount;
	}

	public String getKeyColumns() {
		return keyColumns;
	}
	
	public void setKeyColumns(String keyColumns) {
		this.keyColumns = keyColumns;
	}
	
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public boolean hasProperties() {
		boolean hasProperty = false;
		Iterator<Property> iterator = this.getProperties().iterator();
		for (; iterator.hasNext();) {
			Property property = iterator.next();
			if (property.getValue() != null && !property.getValue().equals("")) {
				hasProperty = true;
				break;
			}
		}
		return hasProperty;
	}

	public OMElement buildXML() {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement queryEl = fac.createOMElement("query", null);
		if (this.getId() != null) {
			queryEl.addAttribute("id", this.getId(), null);
		}
		if (this.getConfigToUse() != null) {
			queryEl.addAttribute("useConfig", this.getConfigToUse(), null);
		}
		if (this.getInputEventTrigger() != null && this.getInputEventTrigger().trim().length() > 0) {
			queryEl.addAttribute("input-event-trigger", this.getInputEventTrigger().trim(), null);
		}
		if (this.getOutputEventTrigger() != null && this.getOutputEventTrigger().trim().length() > 0) {
			queryEl.addAttribute("output-event-trigger", this.getOutputEventTrigger().trim(), null);
		}
		if (this.isReturnGeneratedKeys()) {
			queryEl.addAttribute("returnGeneratedKeys", String.valueOf(this.isReturnGeneratedKeys()), null);
		}
		if (this.isReturnUpdatedRowCount()) {
			queryEl.addAttribute("returnUpdatedRowCount", String.valueOf(this.isReturnUpdatedRowCount()), null);
		}
		if (this.getKeyColumns() != null && this.getKeyColumns().trim().length() > 0) {
			queryEl.addAttribute("keyColumns", this.getKeyColumns().trim(), null);
		}
		if (this.getSql() != null) {
			OMElement sqlEl = fac.createOMElement("sql", null);
			sqlEl.setText(this.getSql().trim());
			queryEl.addChild(sqlEl);
		} else if (this.getExpression() != null) {
			OMElement expEl = fac.createOMElement("expression", null);
			expEl.setText(this.getExpression().trim());
			queryEl.addChild(expEl);
		} else if (this.getSparql() != null) {
			OMElement sparqlEl = fac.createOMElement("sparql", null);
			OMTextImpl omText = (OMTextImpl) fac.createOMText(this.getSparql());
			omText.setType(XMLStreamConstants.CDATA);
			sparqlEl.addChild(omText);	
			queryEl.addChild(sparqlEl);
		} else if (this.getExcel() != null) {
			queryEl.addChild(this.getExcel().buildXML());
		} else if (this.getScraperVariable() != null) {
			OMElement scraperEl = fac.createOMElement("scraperVariable", null);
			scraperEl.setText(this.getScraperVariable());
			queryEl.addChild(scraperEl);
		} else if (this.getGSpread() != null) {
			queryEl.addChild(this.getGSpread().buildXML());
		}
		if (hasProperties()) {
			OMElement propEl = fac.createOMElement("properties", null);
			Iterator<Property> iterator = this.getProperties().iterator();
			while (iterator.hasNext()) {
				Property property = iterator.next();
				if (property.getValue() != null && !property.getValue().equals("")) {
				    propEl.addChild(property.buildXML());
				}
			}
			queryEl.addChild(propEl);
		}
		if (hasSQLDialects()) {
			Iterator<SQLDialect> iterator = this.getSqlDialects().iterator();
			while (iterator.hasNext()) {
				SQLDialect sqlDialect = iterator.next();
				queryEl.addChild(sqlDialect.buildXML());
			}
		}
		if ((this.getResult() != null)
				&& (((this.getResult().getAttributes().size() > 0
						|| this.getResult().getElements().size() > 0 || this.getResult().getCallQueryGroups().size() > 0 
						|| this.getResult().getComplexElements().size() > 0 || this.getResult().getResources().size() > 0))
						|| (DBSFields.RESULT_TYPE_JSON.equals(this.getResult().getOutputType()) && this.getResult().getTextMapping().trim().length() > 0))) {
			queryEl.addChild(this.getResult().buildXML());
		}
		if (this.getParams() != null) {
			for (int a = 0; a < this.getParams().length; a++) {
				Param param = this.getParams()[a];
				queryEl.addChild(param.buildXML());
			}
		}

		return queryEl;
	}

	public GSpreadQuery getGSpread() {
		return gSpread;
	}

	public void setGSpread(GSpreadQuery gSpread) {
		this.gSpread = gSpread;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	

}
