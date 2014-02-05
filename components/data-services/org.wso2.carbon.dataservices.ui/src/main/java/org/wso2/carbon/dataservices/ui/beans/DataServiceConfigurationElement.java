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
}
