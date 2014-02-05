/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.dataservices.ui.beans;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

import java.util.ArrayList;
import java.util.Iterator;

/*
 * Object mapping for XADatasource
 *   <xa-datasource id="id1" class "XADatasourceClass" >
 * 	   <property name="org.wso2.ws.dataservice.driver">org.h2.Driver</property>                  
 *     <property name="org.wso2.ws.dataservice.protocol">jdbc:h2:file:./samples/database/DATA_SERV_SAMP</property>                  
 *     <property name="org.wso2.ws.dataservice.user">wso2ds</property>                  
 *     <property name="org.wso2.ws.dataservice.password">wso2ds</property>        
 *  </xa-datasource>
 *
 */

public class XADataSource {

	private String id;
	private String className;

	private ArrayList<Property> properties = new ArrayList<Property>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public ArrayList<Property> getProperties() {
		return properties;
	}

	public void setProperties(ArrayList<Property> properties) {
		this.properties = properties;
	}

	public void addProperty(String name, Object value) {
		Property property = new Property(name, value);
		properties.add(property);
	}
	
	public void addProperty(Property property) {
		addProperty(property.getName(), property.getValue());
	}
	
	public void removeProperty(Property propertyName) {
		properties.remove(propertyName);
	}

	public void updateProperty(String name, String value) {
		Iterator<Property> propertyItr = properties.iterator();
		for (; propertyItr.hasNext();) {
			Property property = propertyItr.next();
			if (property.getName().equals(name)) {
				if (value != null) {
					property.setValue(value);
				} else {
					property.setValue("");
				}
			}

		}
	}
	
	public void removeProperty(String propertyName) {
		Property property = new Property();
		for (int a = 0; a < properties.size(); a++) {
			property = properties.get(a);
			if (property.getName().equals(propertyName)) {
				removeProperty(property);
			}
		}
		
	}

	public OMElement buildXML() {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement opEl = fac.createOMElement("xa-datasource", null);
		if (this.getId() != null) {
			opEl.addAttribute("id", this.getId(), null);
		}
		if (this.getClassName() != null) {
			opEl.addAttribute("class", this.getClassName(), null);
		}

		Iterator<Property> iterator = this.getProperties().iterator();
		while (iterator.hasNext()) {
			Property property = iterator.next();
			opEl.addChild(property.buildXML());
		}

		return opEl;

	}

}
