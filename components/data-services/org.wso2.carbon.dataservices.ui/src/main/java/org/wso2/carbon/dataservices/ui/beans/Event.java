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
import java.util.List;

/*
 * Object mapping for event
 *   <event-trigger id="id1" language="XPath">
 *     <expression>/Orders/Order/orderNumber=100</expression>
 *     <target-topic>/a/b/t1</target-topic>
 *     <subscriptions>
 *        <subscription>mailto:test@test.com</subscription>
 *     </subscriptions>
 *  </event-trigger>
 *
 */

public class Event extends DataServiceConfigurationElement{
    private String id;
    private String language;
    private String expression;
    private String targetTopic;
    private List<String> subscriptionsList = new ArrayList<String>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
       this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getTargetTopic() {
        return targetTopic;
    }

    public void setTargetTopic(String targetTopic) {
        this.targetTopic = targetTopic;
    }

    public List<String> getSubscriptionsList() {
        return subscriptionsList;
    }

    public void setSubscriptionsList(List<String> subscriptions) {
        this.subscriptionsList = subscriptions;
    }

    public void removeSubscription(String subscription) {
        this.subscriptionsList.remove(subscription);
    }

    public void addSubscription(String subscription) {
        this.subscriptionsList.add(subscription);
    }

    public OMElement buildXML() {
    	OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement opEl = fac.createOMElement("event-trigger", null);
        if(this.getId() != null) {
            opEl.addAttribute("id", this.getId(), null);
        }
        if(this.getLanguage() != null) {
            opEl.addAttribute("language", this.getLanguage(), null);
        }
        OMElement expression = fac.createOMElement("expression", null);
        expression.setText(this.getExpression());
        opEl.addChild(expression);

        OMElement targetTopic = fac.createOMElement("target-topic", null);
        targetTopic.setText(this.getTargetTopic());
        opEl.addChild(targetTopic);

        if(this.getSubscriptionsList().size() > 0) {
            OMElement subscriptions = fac.createOMElement("subscriptions", null);
            opEl.addChild(subscriptions);
            for(String subscripts : this.getSubscriptionsList()) {
                OMElement subscription = fac.createOMElement("subscription", null);
                subscription.setText(subscripts);
                subscriptions.addChild(subscription);
            }
        }

        return opEl;
    }
}
