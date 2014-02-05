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
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;

import java.util.Map;
import java.util.HashMap;


public class Validator extends DataServiceConfigurationElement {

    private String elementName;

    private String name;

    private Map<String, String> validatorElements = new HashMap<String, String>();  /* key - validator Element name, value - value of the validator element */

    public Validator(String elementName, Map<String, String> validatorElements) {
        this.elementName = elementName;
        if (this.elementName.equals("validateLongRange")) {
            this.name = "Long Range Validator";
        } else if (this.elementName.equals("validateDoubleRange")) {
            this.name = "Double Range Validator";
        } else if (this.elementName.equals("validateLength")) {
            this.name = "Length Validator";
        } else if (this.elementName.equals("validatePattern")) {
            this.name = "Pattern Validator";
        } else if (this.elementName.equals("validateCustom")) {
            this.name = "Custom Validator";
        }
        this.validatorElements = validatorElements;
    }

    public String getName() {
        return name;
    }

    public String getElementName() {
        return elementName;
    }

    public Map<String, String> getValidatorElements() {
        return validatorElements;
    }
    
    public void setValidatorElements(Map<String, String> validatorElements) {
        this.validatorElements = validatorElements;
    }

    public OMElement buildXML(){
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement valEl = fac.createOMElement(this.getElementName(), null);
        for (Map.Entry<String,  String> entry : this.getValidatorElements().entrySet()) {
            valEl.addAttribute(entry.getKey(), entry.getValue(), null);
        }
        return valEl;
    }

    public String getPropertiesString() {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        int n = this.getValidatorElements().size();
        for (Map.Entry<String, String> entry : this.getValidatorElements().entrySet()) {
             builder.append(entry.getKey() + "=" + entry.getValue());
            if (i + 1 < n) {
                builder.append(" ");
            }
            i++;
        }
        return builder.toString();
    }
    
}
