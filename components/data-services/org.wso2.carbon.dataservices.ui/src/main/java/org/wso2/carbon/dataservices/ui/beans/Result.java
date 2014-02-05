/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
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
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Result extends DataServiceConfigurationElement {

	/*
	 * represents element attribute of
	 * result element
	 */
	private String resultWrapper = ""; 

	/*
	 * represents rowName attribute of result
	 * element
	 */
	private String rowName = ""; 

	/*
	 * represent result onutput type eg :-
	 * XML/RDF ect
	 */
	private String outputType = "";

    /*
    represent the useColumnNumbers of result element
     */
    private String useColumnNumbers = "false";

    private String escapeNonPrintableChar = "false";

	private String namespace = "";

	/*
	 * contains URI of a described resource in a
	 * rdf:about attribute
	 */
	private String rdfBaseURI = ""; 

	/* represents the role of the user */
	private String userRole = "";

	private List<ComplexElement> complexElements = new ArrayList<ComplexElement>();

	private List<RDFResource> resources = new ArrayList<RDFResource>();

	private List<Attribute> attributes = new ArrayList<Attribute>();

	private List<CallQueryGroup> callQueryGroups = new ArrayList<CallQueryGroup>();

    private List<Element> elements = new ArrayList<Element>();

	private String resultSetColumnNames[];

	private String displayColumnNames[];

	private String elementLocalNames[];

	/* represents the xslt path for xslt transformation */
	private String xsltPath;

	public List<Element> getElements() {
		return elements;
	}

	public void setElements(List<Element> elements) {
		this.elements = elements;
	}

	public void removeElement(Element element) {
		this.elements.remove(element);
	}

	public void addElement(Element element) {
		this.elements.add(element);
	}

	public List<ComplexElement> getComplexElements() {
		return complexElements;
	}

	public void setComplexElements(List<ComplexElement> complexElements) {
		this.complexElements = complexElements;
	}

	public void addComplexElement(ComplexElement complexElement) {
		this.complexElements.add(complexElement);
	}

	public void removeElement(ComplexElement complexElement) {
		this.complexElements.remove(complexElement);
	}

	public List<RDFResource> getResources() {
		return resources;
	}

	public void setResources(List<RDFResource> resource) {
		this.resources = resource;
	}

	public void removeResource(RDFResource resource) {
		this.resources.remove(resource);
	}

	public void addResources(RDFResource resource) {
		this.resources.add(resource);
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void removeAttribute(Attribute attribute) {
		this.attributes.remove(attribute);
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	public void addAttribute(Attribute attribute) {
		this.attributes.add(attribute);
	}

	public List<CallQueryGroup> getCallQueryGroups() {
		return callQueryGroups;
	}

	public String getOutputType() {
		return outputType;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

    public String getUseColumnNumbers() {
		return useColumnNumbers;
	}

	public void setUseColumnNumbers(String useColumnNumbers) {
		this.useColumnNumbers = useColumnNumbers;
	}

    public String getEscapeNonPrintableChar() {
        return escapeNonPrintableChar;
    }

    public void setEscapeNonPrintableChar(String escapeNonPrintableChar) {
        this.escapeNonPrintableChar = escapeNonPrintableChar;
    }

	public String getRdfBaseURI() {
		return rdfBaseURI;
	}

	public void setRdfBaseURI(String rdfBaseURI) {
		this.rdfBaseURI = rdfBaseURI;
	}

	public List<CallQuery> getCallQueries() {
		ArrayList<CallQuery> list = new ArrayList<CallQuery>();
		for (CallQueryGroup callQueryGroup : this.getCallQueryGroups()) {
			if (callQueryGroup.getCallQueries().size() == 1) {
				list.add(callQueryGroup.getCallQueries().get(0));
			}
		}
		return list;
	}

	public void removeCallQueries(CallQuery callQuery) {
		Iterator<CallQueryGroup> cqItr = this.getCallQueryGroups().iterator();
		CallQueryGroup callQueryGroup = null;
		while (cqItr.hasNext()) {
			callQueryGroup = cqItr.next();
			if (callQueryGroup.getCallQueries().size() == 1) {
				if (callQueryGroup.getCallQueries().get(0).equals(callQuery)) {
					cqItr.remove();
					break;
				}
			}
		}
	}

	public Element removeElement(String elementName) {
		Element element = new Element();
		for (int a = 0; a < elements.size(); a++) {
			element = elements.get(a);
			if (element.getName().equals(elementName)) {
				element = elements.remove(a);
			}
		}
		return element;
	}

	public ComplexElement removeComplexElement(String elementName) {
		ComplexElement element = new ComplexElement();
		for (int a = 0; a < complexElements.size(); a++) {
			element = complexElements.get(a);
			if (element.getName().equals(elementName)) {
				element = complexElements.remove(a);
			}
		}
		return element;
	}

	public ComplexElement getChild(String path) {
		return getChild(this.getComplexElements(), getTokens(path));
	}
	
	
	public List<String> getTokens(String path) {
		String children[] = path.split("/");
		List<String> pathTokens = new ArrayList<String>();
		for (String val : children) {
			if (val.length() > 0) {
			    pathTokens.add(val);
			}
		}
		return pathTokens;
	}
	
	public ComplexElement getChild(List<ComplexElement> elementList, List<String> pathTokens) {
		for (ComplexElement el : elementList) {
			if (pathTokens.size()>0 && el.getName().equals(pathTokens.get(0))) {
				if (pathTokens.size() == 1) {
					return el;
				} else if (pathTokens.size() > 1) {
					return getChild(el.getComplexElements(), 
							(new ArrayList<String>(pathTokens)).subList(1, pathTokens.size()));
				}
			}
		}
		return null;
	}
	
	public void removeChild (String path) {
		List<String> tokens = getTokens(path);
		if (tokens.size() == 0) {
			return;
		}
		Iterator<ComplexElement> itr;
		if (tokens.size() == 1) {
			itr = this.getComplexElements().iterator();
		} else {
			ComplexElement parent = getChild(this.getComplexElements(), 
					tokens.subList(0, tokens.size() - 1));
			itr = parent.getComplexElements().iterator();
		}
		String childToken = tokens.get(tokens.size() - 1);
		while (itr.hasNext()) {
			if (itr.next().getName().equals(childToken)) {
				itr.remove();
				return;
			}
		}
	}
	

	public ComplexElement getComplexElement(String elementName) {
		ComplexElement retrievElement = null;
		ComplexElement complexElement = new ComplexElement();
		for (int a = 0; a < complexElements.size(); a++) {
			complexElement = complexElements.get(a);
			if (complexElement != null && complexElement.getName().equals(elementName)) {
				retrievElement = complexElement;
			}
		}
		return retrievElement;
	}

	public RDFResource removeResource(String resourceName) {
		RDFResource resource = new RDFResource();
		for (int a = 0; a < resources.size(); a++) {
			resource = resources.get(a);
			if (resource.getName().equals(resourceName)) {
				resource = resources.remove(a);
			}
		}
		return resource;
	}

	public Attribute removeAttribute(String attributeName) {
		Attribute attribute = new Attribute();
		for (int a = 0; a < attributes.size(); a++) {
			attribute = attributes.get(a);
			if (attribute.getName().equals(attributeName)) {
				attribute = attributes.remove(a);
			}
		}
		return attribute;
	}

	/**
	 * Remove a specific call query. All the call queries are represented as
	 * CallQueryGroups, so a single call query will be inside a CallQueryGroup
	 * which has one element. Find that element, and remove the CallQueryGroup.
	 */
	public void removeCallQuery(String href) {
		CallQuery callQuery = null;
		CallQueryGroup callQueryGroup = null;
		Iterator<CallQueryGroup> callQueryGroupsItr = this.getCallQueryGroups().iterator();
		while (callQueryGroupsItr.hasNext()) {
			callQueryGroup = callQueryGroupsItr.next();
			if (callQueryGroup.getCallQueries().size() == 1) {
				callQuery = callQueryGroup.getCallQueries().get(0);
				if (callQuery.getHref().equals(href)) {
					callQueryGroupsItr.remove();
					break;
				}
			}
		}
	}

	public void removeCallQueryGroup(CallQueryGroup callQueryGroup) {
		this.getCallQueryGroups().remove(callQueryGroup);
	}

	public void setCallQueryGroups(List<CallQueryGroup> callQueryGroups) {
		this.callQueryGroups = callQueryGroups;
	}

	public void setCallQueries(List<CallQuery> callQueries) {
		this.callQueryGroups = new ArrayList<CallQueryGroup>();
		for (CallQuery callQuery : callQueries) {
			this.addCallQuery(callQuery);
		}
	}

	public void addCallQueryGroup(CallQueryGroup callQueryGroup) {
		this.callQueryGroups.add(callQueryGroup);
	}

	public void addCallQuery(CallQuery callQuery) {
		CallQueryGroup callQueryGroup = new CallQueryGroup();
		callQueryGroup.addCallQuery(callQuery);
		this.addCallQueryGroup(callQueryGroup);
	}

	public String getResultWrapper() {
		return resultWrapper;
	}

	public void setResultWrapper(String resultWrapper) {
		this.resultWrapper = resultWrapper;
	}

	public String getRowName() {
		return rowName;
	}

	public void setRowName(String rowName) {
		this.rowName = rowName;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public String getUserRole() {
		return userRole;
	}

	public String[] getResultSetColumnNames() {
		return resultSetColumnNames;
	}

	public void setResultSetColumnNames(String[] resultSetColumnNames) {
		this.resultSetColumnNames = resultSetColumnNames;
	}

	public String[] getDisplayColumnNames() {
		return displayColumnNames;
	}

	public void setDisplayColumnNames(String[] displayColumnNames) {
		this.displayColumnNames = displayColumnNames;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String[] getElementLocalNames() {
		return elementLocalNames;
	}

	public void setElementLocalNames(String[] elementLocalNames) {
		this.elementLocalNames = elementLocalNames;
	}

	public String getXsltPath() {
		return xsltPath;
	}

	public void setXsltPath(String xsltPath) {
		this.xsltPath = xsltPath;
	}

	public Result() {
	}

	@SuppressWarnings("unchecked")
	public Result(OMElement result) {
		String wrapperElementName = result.getAttributeValue(new QName("element"));
		String rowElementName = result.getAttributeValue(new QName("rowName"));
		String outputType = result.getAttributeValue(new QName("outputType"));
		String rdfBaseURI = result.getAttributeValue(new QName("rdfBaseURI"));
		String userRole = result.getAttributeValue(new QName("userRole"));
        String useColumnNumbers = result.getAttributeValue(new QName("useColumnNumbers"));
        String escapeNonPrintableChar = result.getAttributeValue(new QName("escapeNonPrintableChar"));

		/*
		 * if wrapper element || row element is not set, set default values to
		 * them
		 */
		if (outputType == null || outputType.trim().length() == 0) {
			/* default value */
			outputType = "xml";
		}
		if (wrapperElementName == null || wrapperElementName.trim().length() == 0) {
			/* default value */
			wrapperElementName = "results";
		}
		if (rowElementName == null || rowElementName.trim().length() == 0) {
			/* default value */
			rowElementName = "row";
		}
        if (useColumnNumbers == null || useColumnNumbers.trim().length() == 0) {
			/* default value */
			useColumnNumbers = "false";
		}
        if (escapeNonPrintableChar == null || escapeNonPrintableChar.trim().length() == 0) {
            /* default value */
            escapeNonPrintableChar = "false";
        }

		this.outputType = outputType;
        this.useColumnNumbers = useColumnNumbers;
        this.escapeNonPrintableChar = escapeNonPrintableChar;
		this.resultWrapper = wrapperElementName;
		this.rdfBaseURI = rdfBaseURI;
		this.rowName = rowElementName;
		this.userRole = userRole;

		Iterator<OMElement> elements = result.getChildElements();
		ArrayList<String> displayColumns = new ArrayList<String>();
		ArrayList<String> resultSetColumns = new ArrayList<String>();
		ArrayList<String> elementLclNames = new ArrayList<String>();

		while (elements.hasNext()) {
			OMElement element = (OMElement) elements.next();
			String displayTagName = element.getAttributeValue(new QName("name"));
			String resultSetFieldName = element.getAttributeValue(new QName("column"));
			displayColumns.add(displayTagName);
			resultSetColumns.add(resultSetFieldName);
			elementLclNames.add(element.getLocalName());
		}

		this.displayColumnNames = displayColumns.toArray(new String[displayColumns.size()]);
		this.resultSetColumnNames = resultSetColumns.toArray(new String[resultSetColumns.size()]);
		this.elementLocalNames = elementLclNames.toArray(new String[elementLclNames.size()]);
	}

	public OMElement buildXML() {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement resEl = fac.createOMElement("result", null);
		if (this.getOutputType() != null && !(this.getOutputType().equals("xml"))
				&& (outputType.trim().length() > 1)) {
			resEl.addAttribute("outputType", this.getOutputType(), null);
		}
		if (this.getOutputType().equals("xml") || this.getOutputType().equals("")) {
			if (this.getResultWrapper() != null) {
				resEl.addAttribute("element", this.getResultWrapper(), null);
			}
			if (this.getRowName() != null) {
				resEl.addAttribute("rowName", this.getRowName(), null);
			}
		}
		if (this.getOutputType().equals("rdf")) {
			if (this.getRdfBaseURI() != null) {
				resEl.addAttribute("rdfBaseURI", this.getRdfBaseURI(), null);
			}
		}
		if (this.getNamespace() != null && !this.getNamespace().equals("")) {
			resEl.addAttribute("defaultNamespace", this.getNamespace(), null);
		}
		if (this.getXsltPath() != null) {
			resEl.addAttribute("xsltPath", this.getXsltPath(), null);
		}
        if (this.getUseColumnNumbers() != null && (this.getUseColumnNumbers().equals("true"))) {
			resEl.addAttribute("useColumnNumbers", this.getUseColumnNumbers(), null);
		}
        if (this.getEscapeNonPrintableChar() != null && (this.getEscapeNonPrintableChar().equals("true"))) {
            resEl.addAttribute("escapeNonPrintableChar", this.getEscapeNonPrintableChar(), null);
        }
		for (Element element : this.getElements()) {
			resEl.addChild(element.buildXML());
		}
		for (ComplexElement complexElement : this.getComplexElements()) {
			resEl.addChild(complexElement.buildXML());
		}
		for (RDFResource resource : this.getResources()) {
			resEl.addChild(resource.buildXML());
		}
		for (Attribute attribute : this.getAttributes()) {
			resEl.addChild(attribute.buildXML());
		}
		for (CallQueryGroup callQueryGroup : this.getCallQueryGroups()) {
			resEl.addChild(callQueryGroup.buildXML());
		}
		return resEl;
	}

}
