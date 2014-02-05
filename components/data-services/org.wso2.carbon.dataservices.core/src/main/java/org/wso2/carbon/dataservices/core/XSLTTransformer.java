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
package org.wso2.carbon.dataservices.core;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.jaxp.OMSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.common.DBConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class is used in transforming data services result using XSLT.
 */
public class XSLTTransformer {

    private static final Log log = LogFactory.getLog(DBUtils.class);

    private String xsltPath;

    private Transformer transformer;

    private XMLInputFactory xmlInputFactory;

    public XSLTTransformer(String xsltPath) throws TransformerConfigurationException,
            DataServiceFault, IOException {
        this.xsltPath = xsltPath;
        TransformerFactory tFactory = TransformerFactory.newInstance();
        if (!(xsltPath.startsWith(DBConstants.CONF_REGISTRY_PATH_PREFIX) ||
                xsltPath.startsWith(DBConstants.GOV_REGISTRY_PATH_PREFIX))) {
            this.transformer = tFactory.newTransformer(
                    new StreamSource(DBUtils.getInputStreamFromPath(this.getXsltPath())));
        } else {
        	this.transformer = tFactory.newTransformer(new StreamSource(
        			DBUtils.getInputStreamFromPath(this.getXsltPath())));
        }
        this.xmlInputFactory = DBUtils.getXMLInputFactory();
    }

    public String getXsltPath() {
        return xsltPath;
    }

    public Transformer getTransformer() {
        return transformer;
    }

    public XMLInputFactory getXmlInputFactory() {
        return xmlInputFactory;
    }

    /**
     * Transforms the given XML element using the current XSLT transformer and
     * returns the result.
     *
     * @param inputXML The XML data to be transformed
     * @return The transformed XML
     * @throws DataServiceFault
     */
    public OMElement transform(OMElement inputXML) throws DataServiceFault {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new OMSource(inputXML);
            this.getTransformer().transform(xmlSource, new StreamResult(outputStream));
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            XMLStreamReader reader = this.getXmlInputFactory().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            return builder.getDocumentElement();
        } catch (Exception e) {
            String msg = "Error in transforming with XSLT: " + e.getMessage();
            log.error(msg, e);
            throw new DataServiceFault(e, msg);
        }
    }

}
