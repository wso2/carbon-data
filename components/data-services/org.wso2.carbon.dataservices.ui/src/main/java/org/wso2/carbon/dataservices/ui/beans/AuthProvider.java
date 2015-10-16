package org.wso2.carbon.dataservices.ui.beans;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.dataservices.common.DBConstants.AuthorizationProviderConfig;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This is Auth Provider bean.
 */
public class AuthProvider extends DataServiceConfigurationElement {
    private String className;

    private ArrayList<Property> properties = new ArrayList<Property>();

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

    public void addProperty(Property property) {
        this.properties.add(property);
    }

    @Override
    public OMElement buildXML() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement confEl = fac.createOMElement(AuthorizationProviderConfig.ELEMENT_NAME_AUTHORIZATION_PROVIDER, null);
        if (this.getClassName() != null) {
            confEl.addAttribute(AuthorizationProviderConfig.ATTRIBUTE_NAME_CLASS, this.getClassName(), null);
            /* build properties */
            Iterator<Property> iterator = this.getProperties().iterator();
            while (iterator.hasNext()) {
                Property property = iterator.next();
                if (property.buildXML() != null) {
                    confEl.addChild(property.buildXML());
                }
            }
        }
        return confEl;
    }
}
