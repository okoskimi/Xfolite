/*
 * This file is part of: Xfolite (J2ME XForms client)
 *
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
 *
 * Contact: Oskari Koskimies <oskari.koskimies@nokia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser
 * General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.nokia.xfolite.xml.dom;

import java.io.IOException;
import java.io.OutputStream;

import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.IXmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.IXmlSerializer;
import java.util.Hashtable;

public class Document extends Node {

	protected Element documentElement;
    
    protected Hashtable idMap = new Hashtable();
	
	protected String inputEncoding;
	
	protected Boolean xmlStandalone;
    
	protected int parseCount;
	protected int callbackCount;
	protected int callbackInterval;
	protected ParseListener parseListener = null;
	
    protected boolean elementCallbacksEnabled = false;
    protected boolean widgetCallbacksEnabled = false;
    
    protected WidgetFactory widgetFactory = null;
	
    protected boolean parsing = false;
    
	public Document()
	{
        super(null); // Cannot use "this" as argument here
		ownerDocument = this; // Set ownerdocument here directly instead
	}

	public void setParseListener(ParseListener listener, int callbackInterval) {
		this.parseListener = listener;
		this.callbackInterval = callbackInterval;
	}
	
	public int getParseCount() {
		return this.parseCount;
	}
	
    public boolean isElementCallbacksEnabled() {
        return elementCallbacksEnabled;
    }

    public boolean isWidgetCallbacksEnabled() {
        return widgetCallbacksEnabled;
    }
    
    public boolean isParsing() {
        return parsing;
    }

    public void setCallbacksEnabled(boolean callbacksEnabled) {
        elementCallbacksEnabled = widgetCallbacksEnabled = callbacksEnabled;
    }

    public void setWidgetCallbacksEnabled(boolean callbacksEnabled) {
        widgetCallbacksEnabled = callbacksEnabled;
    }

    public void setElementCallbacksEnabled(boolean callbacksEnabled) {
        elementCallbacksEnabled = callbacksEnabled;
    }
    
    public void setRendererFactory(WidgetFactory factory) {
        this.widgetFactory = factory;
    }
    
    //	overloaded methods from the Node class
	public String getNodeName()
	{
		return "#document";
	}

    
	public byte getNodeType() {
		// TODO Auto-generated method stub
		return DOCUMENT_NODE;
	}
	
	public void storeId(String id, Element el) {
	    idMap.put(id, el);
    }

    public Element getElementById(String id) {
        Element el = (Element) idMap.get(id);
        if (el != null) {
            return el;
        } else {
            return searchForId(id);
        }
    }
    
    private Element searchForId(String id) {

        id = id.intern();
        Node n = documentElement;
        while (n != null) 
        {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                String elId = el.getAttribute(id);
                if (elId == id) { // Relies on attribute strings being internalized
                    return el;
                }
            }
            
            
            if (n.hasChildNodes())
            {
                n = n.getFirstChild();
            }
            else
            {
                while(n.getNextSibling() == null) 
                {
                    n = n.getParentNode();

                    if (n == null) 
                    {
                        break;
                    }

                }
                if (n != null) 
                {
                    n = n.getNextSibling();
                }
            }
        }
        
        return null;
    }
    
    public Node getChild(int index)
	{
		  return index==0?documentElement:null;
	}
	  
	public int getChildCount()
	{
		  return documentElement==null? 0 : 1;
	}
	  
	
	public Node getFirstChild()
	{
		return documentElement;
	}
	
	
	public Node getLastChild()
	{
		return documentElement;  
	}
	
    public String getInputEncoding()
    {
    	return inputEncoding;
    }
     
    public Boolean getXmlStandalone()
    {
    	return xmlStandalone;
    }
	
	public Node insertBefore(Node newChild, Node refChild)
	{
		//check document consistency
		if (newChild.ownerDocument!=ownerDocument)
			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,"");
		
		//check if refchild is null
		if (refChild==null)
			return appendChild(newChild);
		else throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,"");
	}
	  
	public Node replaceChild(Node newChild, Node oldChild)
	{
//		check document consistency
		if (newChild.ownerDocument!=ownerDocument)
			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,"");

		if (documentElement==oldChild)
		{
			if (newChild instanceof Element)
			{
				documentElement = (Element) newChild;
				newChild.parentNode = this;
				oldChild.parentNode = null;
				newChild.nextSibling = null;
				newChild.previousSibling =null;
			}
			else throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,"");
		}	 
		else throw new DOMException(DOMException.NOT_FOUND_ERR,"");
		return oldChild;
	}
	  
	public Node removeChild(Node oldChild)
	{
		if (documentElement==oldChild)
		{
			documentElement = null;
			oldChild.parentNode = null;
		}
		else throw new DOMException(DOMException.NOT_FOUND_ERR,"");
		return oldChild;
	}	  
	
	public Node appendChild(Node newChild)
	{
		//#debug info
		System.out.println("Document.appendChild");
		if (newChild.ownerDocument!=ownerDocument) {
			//#debug warn
			System.out.println("Child is not owned by this document");
			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,"");
		}

		if (newChild instanceof Element)
		{
			if (documentElement != null) {
				//#debug warn
				System.out.println("Replacing existing document element");
			}
			documentElement = (Element) newChild;
			newChild.nextSibling = null;
			newChild.previousSibling =null;
			newChild.parentNode = this;
		}
		else {
			//#debug warn
			System.out.println("New child ("
					+ newChild.getClass().getName() + ") is not an element");
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,"");
		}
		return newChild;
	}
	
	public boolean hasChildNodes()
	{
		return documentElement!=null;
	}

	// new methods for Document
	public Element getDocumentElement()
	{
		return documentElement;
	}
	
    public Element createElement(String tagName)
    {
    	 return createElementNS(null,null,tagName);
    }

    public Text createTextNode(String data)
    {
    	return new Text(this, data);
    }

    public Comment createComment(String data)
    {
    	return new Comment(this, data);
    }

    public CDATASection createCDATASection(String data)
    {
    	return new CDATASection(this, data);
    }


    public Attr createAttribute(String name)
    {
    	return createAttributeNS(null,null,name);
    }



    public Element createElementNS(String namespaceURI, 
                                   String prefix,
                                   String localName)
    {
    	return new Element(this, namespaceURI,prefix,localName);
    }


    public Attr createAttributeNS(String namespaceURI, 
						          String prefix,
						          String localName)
    {
    	return new Attr(this, namespaceURI,prefix,localName);
    }
    
    public Node adoptNode(Node source)
    {
    	if(source.parentNode!=null)
    	{
    		source.parentNode.removeChild(source);
    		source.parentNode = null;
    	}
    	
    	RecursiveAdoption(source);
    	
    	return source;
    }
    
    private void RecursiveAdoption(Node source)
    {
    	if (source.getNodeType()== DOCUMENT_NODE)
    		throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Document nodes can't be adopted");
    	
    	source.ownerDocument = this;
    	
    	int count = source.getChildCount();
    	for (int i = 0; i < count; i++) {
    		RecursiveAdoption(source.getChild(i));
		}
    	
    	count = source.getAttributeCount();
    	for (int i = 0; i < count; i++) {
    		RecursiveAdoption(source.getAttribute(i));
		}
    }

    public void parse(IXmlPullParser parser)
            throws IOException, XmlPullParserException {

        parsing = true;
        this.parseCount = 0;
        this.callbackCount = this.callbackInterval;
        try {
            parser.setFeature(IXmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.require(IXmlPullParser.START_DOCUMENT, null, null);
            parser.next();

            inputEncoding = parser.getInputEncoding();
            xmlStandalone = (Boolean) parser
                    .getProperty("http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone");

            documentElement = null;

            Element.parse(this, parser, this);

            parser.next();

            parser.require(IXmlPullParser.END_DOCUMENT, null, null);
            if (this.callbackCount > 0) {
            	this.parseListener.handleParseProgress(this.parseCount, this);
            }
        } finally {
            parsing = false;
        }
    }
    
    public void write(IXmlSerializer writer, Element root, NodeFilter filter) throws IOException {
        if (root == null) {
            root = documentElement;
        }
        writer.startDocument(inputEncoding, xmlStandalone);
        if (root != null) {
            root.write(writer, filter);
        }
        writer.endDocument();
    }

    public void write(IXmlSerializer writer) throws IOException {
        write(writer, null, null);
    }

    public void write(IXmlSerializer writer, NodeFilter filter) throws IOException {
        write(writer, null, filter);
    }

    public Node cloneNode(boolean deep) {
		// Cloning the Document Node is unsupported
		return null;
	}


    public void print() {
    	printSubtree(getDocumentElement(), System.err);
    }
    public static void printSubtree(Element root) {
    	printSubtree(root, System.err);
    }
    public static void printSubtree(Element root, OutputStream out) {
        try {
            KXmlSerializer serializer = new KXmlSerializer();
            serializer.setOutput(out, "UTF-8");
            root.getOwnerDocument().write(serializer, root, null);
        } catch (IOException ex) {
            System.err.println("IOException when serializing: " + ex);
        }
    }
    
}
