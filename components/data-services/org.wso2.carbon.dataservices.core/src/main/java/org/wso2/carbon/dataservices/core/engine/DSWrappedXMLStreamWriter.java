/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dataservices.core.engine;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an XMLStreamWriter implementation which stores the initial writer events in memory,
 * which is then written to the encapsulated XMLStreamWriter and 
 * also the subsequent writer events are redirected to it.
 */
public class DSWrappedXMLStreamWriter implements XMLStreamWriter {

	private static final int EVENT_SET_DEFAULT_NAMESPACE = 0x01;
	private static final int EVENT_SET_NAMESPACE_CONTEXT = 0x02;
	private static final int EVENT_SET_PREFIX = 0x03;
	private static final int EVENT_WRITE_ATTRIBUTE2 = 0x04;
	private static final int EVENT_WRITE_ATTRIBUTE3 = 0x05;
	private static final int EVENT_WRITE_ATTRIBUTE4 = 0x06;
	private static final int EVENT_WRITE_CDATA = 0x07;
	private static final int EVENT_WRITE_CHARACTERS1 = 0x08;
	private static final int EVENT_WRITE_CHARACTERS3 = 0x09;
	private static final int EVENT_WRITE_COMMENT = 0x0A;
	private static final int EVENT_WRITE_DTD = 0x0B;
	private static final int EVENT_WRITE_DEFAULT_NAMESPACE = 0x0C;
	private static final int EVENT_WRITE_EMPTY_ELEMENT1 = 0x0D;
	private static final int EVENT_WRITE_EMPTY_ELEMENT2 = 0x0E;
	private static final int EVENT_WRITE_EMPTY_ELEMENT3 = 0x0F;
	private static final int EVENT_WRITE_END_ELEMENT = 0x10;
	private static final int EVENT_WRITE_END_DOCUMENT = 0x11;
	private static final int EVENT_WRITE_ENTITY_REF = 0x12;
	private static final int EVENT_WRITE_NAMESPACE = 0x13;
	private static final int EVENT_WRITE_PROCESSING_INSTRUCTION1 = 0x14;
	private static final int EVENT_WRITE_PROCESSING_INSTRUCTION2 = 0x15;
	private static final int EVENT_WRITE_START_DOCUMENT = 0x16;
	private static final int EVENT_WRITE_START_DOCUMENT1 = 0x17;
	private static final int EVENT_WRITE_START_DOCUMENT2 = 0x18;
	private static final int EVENT_WRITE_START_ELEMENT1 = 0x19;
	private static final int EVENT_WRITE_START_ELEMENT2 = 0x1A;
	private static final int EVENT_WRITE_START_ELEMENT3 = 0x1B;
	private static final int EVENT_FLUSH = 0x1C;
	private static final int EVENT_CLOSE = 0x1D;
	
	private static final long BUFFER_LIMIT = 200;
	
	private static final int INIT_BUFFERRED_EVENT_LIST_SIZE = 30;
	
	private List<XMLEvent> bufferedEventList;
	
	private XMLStreamWriter xmlWriter;
		
	/**
	 * Keeps the state whether we are in the buffering mode, 
	 * where the initial events are kept in memory.
	 */
	private boolean isBufferring;
	
	/**
	 * Keeps the current number bytes of text content cached in the buffer.
	 */
	private long dataCount;
	
	public DSWrappedXMLStreamWriter(XMLStreamWriter xmlWriter) {
		this.xmlWriter = xmlWriter;
		this.bufferedEventList = new ArrayList<XMLEvent>(INIT_BUFFERRED_EVENT_LIST_SIZE);
		this.isBufferring = true;
	}
	
	private void addEvent(XMLEvent event) throws XMLStreamException {
		this.bufferedEventList.add(event);
		switch (event.eventType) {
		case EVENT_WRITE_ATTRIBUTE2:
            if (event.values[1] != null) {
			    this.dataCount += ((String) event.values[1]).length();
            }
			break;
		case EVENT_WRITE_ATTRIBUTE3:
            if (event.values[2] != null) {
			    this.dataCount += ((String) event.values[2]).length();
            }
			break;
		case EVENT_WRITE_ATTRIBUTE4:
            if (event.values[3] != null) {
			    this.dataCount += ((String) event.values[3]).length();
            }
			break;
		case EVENT_WRITE_CHARACTERS1:
            if (event.values[0] != null) {
			    this.dataCount += ((String) event.values[0]).length();
            } 
			break;
		case EVENT_WRITE_CHARACTERS3:
            if (event.values[0] != null) {
			    this.dataCount += ((char[]) event.values[0]).length;
            }
			break;
		case EVENT_WRITE_CDATA:
            if (event.values[0] != null) {
			    this.dataCount += ((String) event.values[0]).length();
            }
			break;
		}
		/* if the buffer limit exceeded, write out the buffer and disable buffering */
		if (this.dataCount > BUFFER_LIMIT) {
			this.finalizeBuffering();
		}
	}
	
	public XMLStreamWriter getXMLWriter() {
		return xmlWriter;
	}
		
	public void close() throws XMLStreamException {
		if (this.isBufferring) {
			this.addEvent(new XMLEvent(EVENT_CLOSE));
		    this.finalizeBuffering();
		} else {
			this.xmlWriter.close();
		}		
	}

	public void flush() throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_FLUSH));
		    this.finalizeBuffering();
		} else {
			this.xmlWriter.flush();
		}
	}

	public NamespaceContext getNamespaceContext() {
		return this.xmlWriter.getNamespaceContext();
	}

	public String getPrefix(String uri) throws XMLStreamException {
		return this.xmlWriter.getPrefix(uri);
	}

	public Object getProperty(String name) throws IllegalArgumentException {
		return this.xmlWriter.getProperty(name);
	}

	public void setDefaultNamespace(String uri) throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_SET_DEFAULT_NAMESPACE, uri));
		} else {
			this.xmlWriter.setDefaultNamespace(uri);
		}
	}

	public void setNamespaceContext(NamespaceContext context)
			throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_SET_NAMESPACE_CONTEXT, context));
		} else {
			this.xmlWriter.setNamespaceContext(context);
		}
	}

	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_SET_PREFIX, prefix, uri));
		} else {
			this.xmlWriter.setPrefix(prefix, uri);
		}
	}

	public void writeAttribute(String localName, String value)
			throws XMLStreamException {
        if (value != null) {
            if (this.isBufferring) {
                this.addEvent(new XMLEvent(EVENT_WRITE_ATTRIBUTE2, localName, value));
            } else {
                this.xmlWriter.writeAttribute(localName, value);
            }
        }
	}

	public void writeAttribute(String namespaceURI, String localName,
			String value) throws XMLStreamException {
        if (value != null) {
            if (this.isBufferring) {
                this.addEvent(new XMLEvent(EVENT_WRITE_ATTRIBUTE3, namespaceURI,
                        localName, value));
            } else {
                this.xmlWriter.writeAttribute(namespaceURI, localName, value);
            }
        }
	}

	public void writeAttribute(String prefix, String namespaceURI,
			String localName, String value) throws XMLStreamException {
        if (value != null) {
            if (this.isBufferring) {
                this.addEvent(new XMLEvent(EVENT_WRITE_ATTRIBUTE4, prefix, namespaceURI,
                        localName, value));
            } else {
                this.xmlWriter.writeAttribute(prefix, namespaceURI, localName, value);
            }
        }
	}

	public void writeCData(String data) throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_CDATA, data));
		} else {
			this.xmlWriter.writeCData(data);
		}
	}

	public void writeCharacters(String text) throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_CHARACTERS1, text));
		} else {
			this.xmlWriter.writeCharacters(text);
		}
	}

	public void writeCharacters(char[] text, int start, int len)
			throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_CHARACTERS3, text, start, len));
		} else {
			this.xmlWriter.writeCharacters(text, start, len);
		}
	}

	public void writeComment(String data) throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_COMMENT, data));
		} else {
			this.xmlWriter.writeComment(data);
		}
	}

	public void writeDTD(String dtd) throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_DTD, dtd));
		} else {
			this.xmlWriter.writeDTD(dtd);
		}
	}

	public void writeDefaultNamespace(String namespaceURI)
			throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_DEFAULT_NAMESPACE, namespaceURI));
		} else {
			this.xmlWriter.writeDefaultNamespace(namespaceURI);
		}
	}

	public void writeEmptyElement(String localName) throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_EMPTY_ELEMENT1, localName));
		} else {
			this.xmlWriter.writeEmptyElement(localName);
		}
	}

	public void writeEmptyElement(String namespaceURI, String localName)
			throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_EMPTY_ELEMENT2, namespaceURI, localName));
		} else {
			this.xmlWriter.writeEmptyElement(namespaceURI, localName);
		}
	}

	public void writeEmptyElement(String prefix, String localName,
			String namespaceURI) throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_EMPTY_ELEMENT3, prefix, 
				localName, namespaceURI));
		} else {
			this.xmlWriter.writeEmptyElement(prefix, localName, namespaceURI);
		}
	}

	public void writeEndDocument() throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_END_DOCUMENT));
		    this.writeOutInitialXMLEvents();
		    this.isBufferring = false;
		} else {
			this.xmlWriter.writeEndDocument();
		}
	}

	public void writeEndElement() throws XMLStreamException {
		if (this.isBufferring) {
			this.addEvent(new XMLEvent(EVENT_WRITE_END_ELEMENT));
		} else {
			this.xmlWriter.writeEndElement();
		}
	}

	public void writeEntityRef(String name) throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_ENTITY_REF, name));
		} else {
			this.xmlWriter.writeEntityRef(name);
		}
	}

	public void writeNamespace(String prefix, String namespaceURI)
			throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_NAMESPACE, prefix, namespaceURI));
		} else {
			this.xmlWriter.writeNamespace(prefix, namespaceURI);
		}
	}

	public void writeProcessingInstruction(String target)
			throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_PROCESSING_INSTRUCTION1, target));
		} else {
			this.xmlWriter.writeProcessingInstruction(target);
		}
	}

	public void writeProcessingInstruction(String target, String data)
			throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_PROCESSING_INSTRUCTION2, target, data));
		} else {
			this.xmlWriter.writeProcessingInstruction(target, data);
		}
	}

	public void writeStartDocument() throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_START_DOCUMENT));
		} else {
			this.xmlWriter.writeStartDocument();
		}
	}

	public void writeStartDocument(String version) throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_START_DOCUMENT1, version));
		} else {
			this.xmlWriter.writeStartDocument(version);
		}
	}

	public void writeStartDocument(String encoding, String version)
			throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_START_DOCUMENT2, encoding, version));
		} else {
			this.xmlWriter.writeStartDocument(encoding, version);
		}
	}

	public void writeStartElement(String localName) throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_START_ELEMENT1, localName));
		} else {
			this.xmlWriter.writeStartElement(localName);
		}
	}

	public void writeStartElement(String namespaceURI, String localName)
			throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_START_ELEMENT2, namespaceURI, localName));
		} else {
			this.xmlWriter.writeStartElement(namespaceURI, localName);
		}
	}

	public void writeStartElement(String prefix, String localName,
			String namespaceURI) throws XMLStreamException {
		if (this.isBufferring) {
		    this.addEvent(new XMLEvent(EVENT_WRITE_START_ELEMENT3, prefix, 
				localName, namespaceURI));
		} else {
			this.xmlWriter.writeStartElement(prefix, localName, namespaceURI);
		}
	}
	
	private static void writeXMLEvent(XMLEvent event, XMLStreamWriter targetXMLWriter) 
			throws XMLStreamException {
		switch (event.eventType) {
		case EVENT_CLOSE:
			targetXMLWriter.close();
			break;
		case EVENT_FLUSH:
			targetXMLWriter.flush();
			break;
		case EVENT_SET_DEFAULT_NAMESPACE:
			targetXMLWriter.setDefaultNamespace((String) event.values[0]);
			break;
		case EVENT_SET_NAMESPACE_CONTEXT:
			targetXMLWriter.setNamespaceContext((NamespaceContext) event.values[0]);
			break;
		case EVENT_SET_PREFIX:
			targetXMLWriter.setPrefix((String) event.values[0], (String) event.values[1]);
			break;
		case EVENT_WRITE_ATTRIBUTE2:
			targetXMLWriter.writeAttribute((String) event.values[0], (String) event.values[1]);
			break;
		case EVENT_WRITE_ATTRIBUTE3:
			targetXMLWriter.writeAttribute((String) event.values[0], (String) event.values[1],
					(String) event.values[2]);
			break;
		case EVENT_WRITE_ATTRIBUTE4:
			targetXMLWriter.writeAttribute((String) event.values[0], (String) event.values[1],
					(String) event.values[2], (String) event.values[3]);
			break;
		case EVENT_WRITE_CDATA:
			targetXMLWriter.writeCData((String) event.values[0]);
			break;
		case EVENT_WRITE_CHARACTERS1:
            if (event.values[0] != null) {
			    targetXMLWriter.writeCharacters((String) event.values[0]);
            }
			break;
		case EVENT_WRITE_CHARACTERS3:
			targetXMLWriter.writeCharacters((char[]) event.values[0], 
					(Integer) event.values[1], (Integer) event.values[2]);
			break;
		case EVENT_WRITE_COMMENT:
			targetXMLWriter.writeComment((String) event.values[0]);
			break;
		case EVENT_WRITE_DEFAULT_NAMESPACE:
			targetXMLWriter.writeDefaultNamespace((String) event.values[0]);
			break;
		case EVENT_WRITE_DTD:
			targetXMLWriter.writeDTD((String) event.values[0]);
			break;
		case EVENT_WRITE_EMPTY_ELEMENT1:
			targetXMLWriter.writeEmptyElement((String) event.values[0]);
			break;
		case EVENT_WRITE_EMPTY_ELEMENT2:
			targetXMLWriter.writeEmptyElement((String) event.values[0], (String) event.values[1]);
			break;
		case EVENT_WRITE_EMPTY_ELEMENT3:
			targetXMLWriter.writeEmptyElement((String) event.values[0], (String) event.values[1],
					(String) event.values[2]);
			break;
		case EVENT_WRITE_END_DOCUMENT:
			targetXMLWriter.writeEndDocument();
			break;
		case EVENT_WRITE_END_ELEMENT:
			targetXMLWriter.writeEndElement();
			break;
		case EVENT_WRITE_ENTITY_REF:
			targetXMLWriter.writeEntityRef((String) event.values[0]);
			break;
		case EVENT_WRITE_NAMESPACE:
			targetXMLWriter.writeNamespace((String) event.values[0], (String) event.values[1]);
			break;
		case EVENT_WRITE_PROCESSING_INSTRUCTION1:
			targetXMLWriter.writeProcessingInstruction((String) event.values[0]);
			break;
		case EVENT_WRITE_PROCESSING_INSTRUCTION2:
			targetXMLWriter.writeProcessingInstruction((String) event.values[0],
					(String) event.values[1]);
			break;
		case EVENT_WRITE_START_DOCUMENT:
			targetXMLWriter.writeStartDocument();
			break;
		case EVENT_WRITE_START_DOCUMENT1:
			targetXMLWriter.writeStartDocument((String) event.values[0]);
			break;
		case EVENT_WRITE_START_DOCUMENT2:
			targetXMLWriter.writeStartDocument((String) event.values[0], (String) event.values[1]);
			break;
		case EVENT_WRITE_START_ELEMENT1:
			targetXMLWriter.writeStartElement((String) event.values[0]);
			break;
		case EVENT_WRITE_START_ELEMENT2:
			targetXMLWriter.writeStartElement((String) event.values[0], (String) event.values[1]);
			break;
		case EVENT_WRITE_START_ELEMENT3:
			targetXMLWriter.writeStartElement((String) event.values[0], (String) event.values[1],
					(String) event.values[2]);
			break;
		}
	}
	
	private void finalizeBuffering() throws XMLStreamException {
		this.writeOutInitialXMLEvents();
		this.isBufferring = false;
	}
	
	private void writeOutInitialXMLEvents() 
			throws XMLStreamException {
		for (XMLEvent event : this.bufferedEventList) {
			writeXMLEvent(event, this.xmlWriter);
		}
	}
	
	/** 
	 * This class represents an XMLStreamWriter event,
	 * let's keep this class simple, no getters/setters.
	 */
	private class XMLEvent {
		
		public int eventType;
		
		public Object[] values;
		
		public XMLEvent(int eventType, Object... values) {
			this.eventType = eventType;
			this.values = values;
		}
		
	}
	
}
