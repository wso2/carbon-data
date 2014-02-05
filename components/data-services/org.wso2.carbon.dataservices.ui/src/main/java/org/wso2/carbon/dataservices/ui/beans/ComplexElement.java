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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * Represents Complex type elements 
 * <Element name="Personl" namespace="http://x.com/a"> 
 *   <Element name="Phone" column="phone" />
 * </Element>
 * 
 * @see Element/Attribute
 */

public class ComplexElement {

	private List<Element> elements;
	private List<Attribute> attributes;
	private List<CallQueryGroup> callQueryGroups;
	private List<ComplexElement> complexElements;
	private List<RDFResource> resources;
	private String name;
	private String namespace;
    private String arrayName;

	public ComplexElement(String name, String namespace, String arrayName) {
		this.name = name;
		this.namespace = namespace;
        this.arrayName = arrayName;
		this.elements = new ArrayList<Element>();
		this.attributes = new ArrayList<Attribute>();
		this.callQueryGroups = new ArrayList<CallQueryGroup>();
		this.complexElements = new ArrayList<ComplexElement>();
		this.resources = new ArrayList<RDFResource>();
	}
	
	public ComplexElement() {
		this.elements = new ArrayList<Element>();
		this.attributes = new ArrayList<Attribute>();
		this.callQueryGroups = new ArrayList<CallQueryGroup>();
		this.complexElements = new ArrayList<ComplexElement>();
		this.resources = new ArrayList<RDFResource>();
	}
	
	public void addCallQuery(CallQuery callQuery) {
		CallQueryGroup callQueryGroup = new CallQueryGroup();
		callQueryGroup.addCallQuery(callQuery);
		this.addCallQueryGroup(callQueryGroup);
	}

	public void addCallQueryGroup(CallQueryGroup callQueryGroup) {
		this.getCallQueryGroups().add(callQueryGroup);
	}

	public List<CallQueryGroup> getCallQueryGroups() {
		return callQueryGroups;
	}

	public void addComplexElement(ComplexElement complexElement) {
		this.getComplexElements().add(complexElement);
	}

	public List<ComplexElement> getComplexElements() {
		return complexElements;
	}
	
	public void addElement(Element element) {
		this.getElements().add(element);
	}
	
	public void addResource(RDFResource resource) {
		this.getResources().add(resource);
	}

	public List<Element> getElements() {
		return elements;
	}

	public void addAttribute(Attribute attribute) {
		this.getAttributes().add(attribute);
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public String getName() {
		return name;
	}
	
	public List<RDFResource> getResources() {
		return resources;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

    public String getArrayName() {
        return arrayName;
    }

    public void setArrayName(String arrayName) {
        this.arrayName = arrayName;
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
	
	public ComplexElement removeComplexElement(String elementName) {
		ComplexElement element = new ComplexElement();
		ComplexElement removingELement = null;
		for (int a = 0; a < complexElements.size(); a++) {
			element = complexElements.get(a);
			if (element.getName().equals(elementName)) {
				removingELement = complexElements.remove(a);
			}
		}
		return removingELement;
	}
	
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
	
	public List<CallQuery> getCallQueries() {
		ArrayList<CallQuery> list = new ArrayList<CallQuery>();
		for (CallQueryGroup callQueryGroup : this.getCallQueryGroups()) {
			if (callQueryGroup.getCallQueries().size() == 1) {
				list.add(callQueryGroup.getCallQueries().get(0));
			}
		}
		return list;
	}
	
	
	
	public ComplexElement getComplexElement (String name) {		
		ComplexElement retrievElement = new ComplexElement();
		ComplexElement complexElement =  new ComplexElement();
		for (int a = 0; a < complexElements.size(); a++) {
			complexElement = complexElements.get(a);
			if (complexElement != null  && complexElement.getName().equals(name)) {
				retrievElement = complexElement;
			}
		}
		return retrievElement;
	}
	
	public OMElement buildXML() {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement elementEl = fac.createOMElement("element", null);
		if (this.getName() != null) {
			elementEl.addAttribute("name", this.getName(), null);
		}
		if (this.getNamespace() != null) {
			elementEl.addAttribute("namespace", this.getNamespace(), null);
		}
        if (this.getArrayName() != null) {
            elementEl.addAttribute("arrayName", this.getArrayName(), null);
        }
		for (Element element : this.getElements()) {
			elementEl.addChild(element.buildXML());
		}
		for (RDFResource resource : this.getResources()) {
			elementEl.addChild(resource.buildXML());
		}
		for (Attribute attribute : this.getAttributes()) {
			elementEl.addChild(attribute.buildXML());
		}
		for (CallQueryGroup callQueryGroup : this.getCallQueryGroups()) {
			elementEl.addChild(callQueryGroup.buildXML());
		}
		for (ComplexElement complexElement : this.getComplexElements()) {
			elementEl.addChild(complexElement.buildXML());
		}
		return elementEl;
	}

}
