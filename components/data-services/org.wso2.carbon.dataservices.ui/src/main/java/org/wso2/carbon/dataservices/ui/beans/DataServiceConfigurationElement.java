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

import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class DataServiceConfigurationElement {

    private String requireddRoles;

    private String xsdType;

    public DataServiceConfigurationElement(String requiredRoles, String xsdType) {
        this.requireddRoles = requiredRoles;
        this.xsdType = xsdType;
    }

    public DataServiceConfigurationElement(String requiredRoles) {
        this.requireddRoles = requiredRoles;
    }

    public DataServiceConfigurationElement(){
        
    }

    public String getRequiredRoles() {
        return requireddRoles;
    }

    public String getXsdType() {
        return xsdType;
    }

    public void setRequiredRoles(String userRoles){
        this.requireddRoles = userRoles;
    }

    public void setxsdType(String xsType){
        this.xsdType = xsType;
    }

    /**
     * Generates XML representation of Object
     * @return OMElement
     */
    public abstract OMElement buildXML();

    /**
     * Extract advance property name value pairs from the query configuration.
     *
     * @param queryEl DBS query config element which contains the advanced properties
     * @return Map of property name value pairs
     */
    public Map<String, String> extractAdvancedProps(OMElement queryEl) {
        Map<String, String> advancedProperties;
        OMElement propsEl = queryEl.getFirstChildWithName(new QName("properties"));
        /* extract advanced query properties */
        if (propsEl != null) {
            advancedProperties = extractProperties(propsEl);
        } else {
            advancedProperties = new HashMap<String, String>();
        }
        return advancedProperties;
    }

    /**
     * Helper method to extract individual property values from the parent element.
     *
     * @param propsParentEl Parent OMElement which contains the property data
     * @return Map of property name value pairs
     */
    private Map<String, String> extractProperties(OMElement propsParentEl) {
        Map<String, String> properties = new HashMap<String, String>();
        OMElement propEl = null;
        Iterator<OMElement> itr = propsParentEl.getChildrenWithName(new QName("property"));
        String text;
        while (itr.hasNext()) {
            propEl = itr.next();
            if (propEl.getChildElements().hasNext()) {
                text = propEl.toString();
            } else {
                text = propEl.getText();
            }
            if (text != null && !text.equals("")) {
                properties.put(propEl.getAttributeValue(new QName("name")), text);
            }
        }
        return properties;
    }
}
