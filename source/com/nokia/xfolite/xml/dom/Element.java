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
import java.util.Vector;

import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.IXmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.IXmlSerializer;

public class Element extends NamedNode {
	
	protected Vector attributes;
	protected Vector  childNodes;
	
	protected Element(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
		super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
		// TODO Auto-generated constructor stub
	}

	//overloaded methods from the Node class
	
	public byte getNodeType()
	{
		return ELEMENT_NODE;
	}

	public Node getChild(int index)
	{
		  return (Node) childNodes.elementAt(index);
	}
	  
	public int getChildCount()
	{
		  return childNodes==null? 0 : childNodes.size();
	}
	  
	
	public Node getFirstChild()
	{
		return hasChildNodes()? (Node) childNodes.firstElement():null;
	}
	
	
	public Node getLastChild()
	{
		return hasChildNodes()? (Node) childNodes.lastElement():null;  
	}
	
	public Node insertBefore(Node newChild, Node refChild)
	{
		//check document consistency
		if (newChild.ownerDocument!=ownerDocument)
			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,"");
		
		//check if refchild is null
		if (refChild==null)
			return appendChild(newChild);
		
		//check if found and insert it
		int refPosition;
		if (childNodes !=null 
				&& (refPosition = childNodes.indexOf(refChild))>=0)
		{
			// Adjust left links
			if (refPosition>0)
			{
				Node node_before = (Node) childNodes.elementAt(refPosition-1);
				newChild.previousSibling = node_before;
				node_before.nextSibling = newChild;
			}else newChild.previousSibling=null;
			
			// Adjust right links
			Node node_after = (Node) childNodes.elementAt(refPosition);
			newChild.nextSibling = node_after;
			node_after.previousSibling = newChild;
			
			//adjust parent
			newChild.parentNode = this; 
			
			//insert the new child
			childNodes.insertElementAt(newChild,refPosition);
            
		}	
		else throw new DOMException(DOMException.NOT_FOUND_ERR,"");
		
		//Return the newly inserted child
		return newChild;
	}
	  
	public Node replaceChild(Node newChild, Node oldChild)
	{
		// check document consistency
		if (newChild.ownerDocument!=ownerDocument)
			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,"");

		int oldPosition;
		if (childNodes !=null 
				&& (oldPosition = childNodes.indexOf(oldChild))>=0)
		{
			// Adjust left links
			Node node_before = oldChild.previousSibling;
			newChild.previousSibling = node_before;
			node_before.nextSibling = newChild;
			
			// Adjust right links
			Node node_after = oldChild.nextSibling;
			node_after.previousSibling = newChild;
			newChild.nextSibling = node_after;
			
			// adjust parent
			newChild.parentNode = this; 

			//replace elements
			childNodes.setElementAt(newChild,oldPosition);
			
			// clear the node pointers
			oldChild.parentNode = null;
			oldChild.previousSibling = null;
			oldChild.nextSibling = null;
       
        }	
		else throw new DOMException(DOMException.NOT_FOUND_ERR,"");
		return oldChild;
	}
	  
	public Node removeChild(Node oldChild)
	{
		int oldPosition;
		if (childNodes !=null 
				&& (oldPosition = childNodes.indexOf(oldChild))>=0)
		{
			//	adjust left links
			Node node_before = oldChild.previousSibling;
			Node node_after = oldChild.nextSibling;
			
			if (node_before!=null)
				node_before.nextSibling = node_after;
			
			//	adjust right links
			if (node_after!=null)
				node_after.previousSibling = node_before;
			
            //remove the node
			childNodes.removeElementAt(oldPosition);
			
			//clear the node pointers
			oldChild.parentNode = null;
			oldChild.previousSibling = null;
			oldChild.nextSibling = null;
		}
		else throw new DOMException(DOMException.NOT_FOUND_ERR,"");
		return oldChild;
    }
	
	public Node appendChild(Node newChild)
	{
		if (newChild.ownerDocument!=ownerDocument)
			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,"");

		if (childNodes==null) childNodes = new Vector();
		
		// adjust right links
		newChild.nextSibling=null;
		
		// adjust left links
		int child_count= childNodes.size();
		if (child_count>0)
		{
			Node last_node = (Node) childNodes.lastElement();
			last_node.nextSibling=newChild;
			newChild.previousSibling = last_node;
		}
		
		// adjust parent
		newChild.parentNode = this;
		
		childNodes.addElement(newChild);
        
		return newChild;
	}

    
	public boolean hasChildNodes()
	{
		return (childNodes!=null && childNodes.size()>0);
	}
	
	public boolean hasAttributes()
	{
		return (attributes!=null && attributes.size()>0);
	}
	
	public Attr getAttribute(int index)
	{
		  return (Attr) attributes.elementAt(index);
	}
	  
	public int getAttributeCount()
	{
		return attributes==null? 0 : attributes.size();
	}

    //ELement specific methods
	
    public String getTagName()
    {
    	return getNodeName();
    }
    
    public String getAttribute(String name)
    {
    	return getAttributeNS(null,name);
    }

    public void setAttribute(String name, String value)
    {
    	setAttributeNS(null,null,name,value);
    }

    public void removeAttribute(String name)
    {
    	removeAttributeNS(null,name);
    }

    public Attr getAttributeNode(String name)
    {
    	return getAttributeNodeNS(null,name);
    }
    
    public Attr setAttributeNode(Attr newAttr)
    {
    	if (newAttr.ownerDocument!=ownerDocument)
    		throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,"");
    	if (newAttr.parentNode!=null)
    		throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR,"");
    	
    	newAttr.parentNode = this;
    	
    	if (attributes!=null)
    	{
    		int attr_count = attributes.size();
    		for (int i = 0; i < attr_count; i++) {
				Attr curr_attr = (Attr) attributes.elementAt(i);
				if ((newAttr.localName==curr_attr.localName)
				 &&((newAttr.namespaceURI==curr_attr.namespaceURI)
				    ||  ((newAttr.namespaceURI ==null || newAttr.namespaceURI=="")
				      && (curr_attr.namespaceURI==null  || curr_attr.namespaceURI==""))
				      ))
					{
					attributes.setElementAt(newAttr,i);
					return curr_attr;
					}
			}
    	}else attributes = new Vector();
    	
    	//attribute not found so add it
    	attributes.addElement(newAttr);
    	return null;
    }

    public Attr removeAttributeNode(Attr oldAttr)
    {
    	int oldPosition;
		if (attributes !=null 
				&& (oldPosition = attributes.indexOf(oldAttr))>=0)
		{
			attributes.removeElementAt(oldPosition);
		}
		else throw new DOMException(DOMException.NOT_FOUND_ERR,"");
		return oldAttr;
    }

    
    public String getAttributeNS(String namespaceURI, 
                                 String localName)
    {
    	namespaceURI = (namespaceURI == null) ? null : namespaceURI.intern();
    	localName = localName.intern();
    	
    	if (attributes!=null)
    	{
    		int attr_count = attributes.size();
    		for (int i = 0; i < attr_count; i++) {
    		  Attr curr_attr = (Attr) attributes.elementAt(i);
    		  if ((localName==curr_attr.localName)
    			&&((namespaceURI==curr_attr.namespaceURI)
    			|| ((namespaceURI==null || namespaceURI=="")
    			 && (curr_attr.namespaceURI==null  || curr_attr.namespaceURI==""))
    				))
    		    return curr_attr.value;
    		}
    	}
    	return "";
    }

    public void setAttributeNS(String namespaceURI,
    						   String prefix,
                               String localName, 
                               String value)
    {
    	namespaceURI = (namespaceURI == null) ? null : namespaceURI.intern();
    	localName = localName.intern();
        value = value.intern();
    	
    	if (attributes==null) attributes = new Vector();
		int attr_count = attributes.size();
		for (int i = 0; i < attr_count; i++) {
			Attr curr_attr = (Attr) attributes.elementAt(i);
			if ((localName==curr_attr.localName)
	    	  &&((namespaceURI==curr_attr.namespaceURI)
	    	  || ((namespaceURI ==null || namespaceURI=="")
	    	   && (curr_attr.namespaceURI==null  || curr_attr.namespaceURI==""))
	    		  ))
			{
				curr_attr.value = value;
				return;
			}
		}
		Attr new_attr = ownerDocument.createAttributeNS(namespaceURI,prefix,localName);
		new_attr.value = value;
		new_attr.ownerElement = this;
		attributes.addElement(new_attr);
    }


    public void removeAttributeNS(String namespaceURI, 
                                  String localName)
    {
    	namespaceURI = (namespaceURI == null) ? null : namespaceURI.intern();
    	localName = localName.intern();
    	
    	if (attributes!=null)
    	{
    		int attr_count = attributes.size();
    		for (int i = 0; i < attr_count; i++)
    		{
				Attr curr_attr = (Attr) attributes.elementAt(i);
				if ((localName==curr_attr.localName)
				  &&((namespaceURI==curr_attr.namespaceURI)
				  || ((namespaceURI ==null || namespaceURI=="")
				   && (curr_attr.namespaceURI==null  || curr_attr.namespaceURI==""))
				       ))
				{
					attributes.removeElementAt(i);
					curr_attr.ownerElement = null;
				}
					
			}
    	}
    }


    public Attr getAttributeNodeNS(String namespaceURI, 
                                   String localName)
    {
    	namespaceURI = (namespaceURI == null) ? null : namespaceURI.intern();
    	localName = localName.intern();
    	
    	if (attributes!=null)
    	{
    		int attr_count = attributes.size();
    		for (int i = 0; i < attr_count; i++) {
				Attr curr_attr = (Attr) attributes.elementAt(i);
				if ((localName==curr_attr.localName)
					&&((namespaceURI==curr_attr.namespaceURI)
					|| ((namespaceURI ==null || namespaceURI=="")
			         && (curr_attr.namespaceURI==null  || curr_attr.namespaceURI==""))
					))
					return curr_attr;
			}
    	}
    	return null;
    }

    public boolean hasAttribute(String name)
    {
    	return hasAttributeNS(null,localName);
    }


    public boolean hasAttributeNS(String namespaceURI, 
                                  String localName)
    {
    	return getAttributeNodeNS(namespaceURI,localName)!=null;
    }
    
    
    /**
     * Parse one element recursively.
     * Note that when parsing, callbackEnabled flags are cached so that even if the 
     * pre-callback sets flag to false, the post-callback is still called.
     * This is essential for ItemsetElement template copying.
     * On the other hand, callbacks after parsing (using initialize) do NOT cache
     * callbackEnabled flags. The processing in this case is not recursive, so there
     * is no easy way to do it either, without increasing memory overhead.
     * @param parent
     * @param parser
     * @param docFactory
     * @throws IOException
     * @throws XmlPullParserException
     */
    
    static public void parse(Node parent, IXmlPullParser parser, Document docFactory)
        throws IOException, XmlPullParserException
    {
	    parser.require( IXmlPullParser.START_TAG, null, null);
	    String name = parser.getName();
	    String ns = parser.getNamespace();
	    String prefix = parser.getPrefix();
	    Element newElement = docFactory.createElementNS(ns, prefix,name);
        // We have to cache this so that an element can disable callbacks in
        // elementParsed and re-enable them in childrenParsed
        // (if we don't cache, childrenParsed would never be called)
        boolean elCallbacksEnabled = docFactory.elementCallbacksEnabled;
        boolean wgCallbacksEnabled = docFactory.widgetCallbacksEnabled;
	    
	
	    // process attributes
	    for (int i = 0; i < parser.getAttributeCount(); i++)
	    {
	        String attrNs = parser.getAttributeNamespace(i);
	        String attrName = parser.getAttributeName(i);
	        String attrValue = parser.getAttributeValue(i);
	        if(attrNs == null || attrNs.length() == 0) {
	            newElement.setAttribute(attrName, attrValue);
	        } else {
	            String attrPrefix = parser.getAttributePrefix(i);
	            newElement.setAttributeNS(attrNs, attrPrefix, attrName, attrValue);
	        }
	    }
	    
	    //declare namespaces - quite painful and easy to fail process in DOM2
	    declareNamespaces(parser, newElement);
	
        parent.appendChild(newElement);

        boolean callWidgetFactory = true;
        if (elCallbacksEnabled) {
            callWidgetFactory &= newElement.elementParsed();
        }
        callWidgetFactory &= wgCallbacksEnabled;
        if (docFactory.widgetFactory != null && callWidgetFactory) {
            docFactory.widgetFactory.elementParsed(newElement);
        }
        
        
        // process children
	    while( parser.next() != IXmlPullParser.END_TAG ) {
	        if (parser.getEventType() == IXmlPullParser.START_TAG) {
	            parse(newElement, parser, docFactory);
	        } else if (parser.getEventType() == IXmlPullParser.TEXT) {
	            String text = parser.getText();
	            Text textEl = docFactory.createTextNode(text);
	            newElement.appendChild(textEl);
	        } else {
	            throw new XmlPullParserException(
	                "unexpected event "+IXmlPullParser.TYPES[ parser.getEventType() ], parser, null);
	        }
	    }
	    parser.require( IXmlPullParser.END_TAG, ns, name);
        callWidgetFactory = true;
        if (elCallbacksEnabled) {
           callWidgetFactory &= newElement.childrenParsed();
        }
        callWidgetFactory &= wgCallbacksEnabled;
        if (docFactory.widgetFactory != null && callWidgetFactory) {
            docFactory.widgetFactory.childrenParsed(newElement);
        }
        docFactory.parseCount++;
        if (docFactory.parseListener != null) {
        	if (--docFactory.callbackCount == 0) {
        		docFactory.callbackCount = docFactory.callbackInterval;
        		docFactory.parseListener.handleParseProgress(docFactory.parseCount, docFactory);
        	}
        }
    }
   
    
    /**
     * Callback that is called when all attributes of the element have been parsed.
     * Default implementation just returns false.
     * @return true if parsing should call elementParsed on a widget factory afterwards
     * 
     */    
    public boolean elementParsed() {
        return false;
    }

    /**
     * Callback that is called when initialize is called on an element.
     * Note that the -initialized() callbacks are only called on the root element
     * of the subtree that is initialized. For the other elements,
     * the -parsed() callbacks are called. The root element gets a different callbacks
     * because its GUI representation has to be integrated to the existing GUI
     * representation, whereas the subnodes can be processed similarly as when
     * creating a GUI tree from scratch. 
     * Default implementation just delegates to elementParsed().
     * @return true if parsing should call elementInitialized on a widget factory afterwards
     * 
     */    
    public boolean elementInitialized() {
        return elementParsed();
    }
    
    /**
     * Callback that is called when all children of the element have been parsed.
     * Default implementation just returns false.
     * @return true if parsing should call childrenParsed on a widget factory afterwards
     * 
     */
    public boolean childrenParsed() {
        return false;
    }

    /**
     * Callback that is called when the whole subtree has been initialized.
     * Note that the -initialized() callbacks are only called on the root element
     * of the subtree that is initialized. For the other elements,
     * the -parsed() callbacks are called. The root element gets different callbacks
     * because its GUI representation has to be integrated to the existing GUI
     * representation, whereas the subnodes can be processed similarly as when
     * creating a GUI tree from scratch.
     * Default implementation just delegates to childrenParsed(). 
     * @return true if parsing should call childrenInitialized on a widget factory afterwards
     * 
     */    
    public boolean childrenInitialized() {
        return childrenParsed();
    }
    
    /**
     * Callback that is called when the element is being removed.
     * When a subtree is removed, this is called in reverse document order (to ensure leaf nodes are called first)
     * @return true if parsing should call removingElement on a widget factory afterwards
     * 
     */
    public boolean removingElement() {
        return false;
    }
    
    static private void declareNamespaces(IXmlPullParser pp, Element parent)
    throws DOMException, XmlPullParserException
    {
        for (int i = pp.getNamespaceCount(pp.getDepth()-1);
             i < pp.getNamespaceCount(pp.getDepth());
             ++i)
        {
            declareOneNamespace(pp, i, parent);
        }
    }


    static private void declareOneNamespace(IXmlPullParser pp, int i, Element parent)
    throws DOMException, XmlPullParserException {
	    String xmlnsPrefix = pp.getNamespacePrefix(i);
	    
	    String xmlnsUri = pp.getNamespaceUri(i);
	    
	    if (xmlnsPrefix==null)
	    {
	    	parent.setAttributeNS("http://www.w3.org/2000/xmlns/",null,"xmlns", xmlnsUri);
	    }
	    else
	    {
	    	parent.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns",xmlnsPrefix, xmlnsUri);
	    }
	}
    
    protected void write(IXmlSerializer writer, NodeFilter filter) throws IOException
    {	
        if (filter != null && filter.acceptNode(this) == NodeFilter.REJECT) {
            return;
        }
        
    	int attr_count = getAttributeCount();
    	
		//name space declaration
		for (int i = 0; i < attr_count; i++)
		{
		   Attr curr_attr = (Attr) attributes.elementAt(i);
		   if (curr_attr.isDefaultNSDeclaration())
		   {
			   writer.setPrefix(null,curr_attr.value);
		   }
		   else if(curr_attr.isPrefixedNSDeclaration())
		   {
			   writer.setPrefix(curr_attr.localName,curr_attr.value);
		   }
		}
		
		//Start tag
        if (filter == null || filter.acceptNode(this) == NodeFilter.ACCEPT) {
            writer.startTag(namespaceURI, localName);
        
		
    		//attributes
    		for (int i = 0; i < attr_count; i++)
    		{
    			Attr curr_attr = (Attr) attributes.elementAt(i);
    			if (!curr_attr.isNSDeclaration())
    				curr_attr.write(writer, filter);
    		}
        }
		//children
		int childrencount = getChildCount();
		for (int i = 0;i<childrencount;i++)
		{
			((Node) childNodes.elementAt(i)).write(writer, filter);
		}
		
        if (filter == null || filter.acceptNode(this) == NodeFilter.ACCEPT) {
            writer.endTag(namespaceURI, localName);
        }
    }

	public Node cloneNode(boolean deep) {
		// TODO Auto-generated method stub
		Element newElement = ownerDocument.createElementNS(namespaceURI,prefix,localName);
		
		if (attributes!=null)
		{
			int attr_count = attributes.size();
			for (int i=0;i<attr_count;i++)
			{
				Attr Curr_attr = (Attr) attributes.elementAt(i);
				newElement.setAttributeNS(Curr_attr.namespaceURI,
									Curr_attr.prefix,
									Curr_attr.localName,
									Curr_attr.value);
			}
		}
		
		if (deep && childNodes!=null)
		{
			int child_count = childNodes.size();
			for (int i=0;i<child_count;i++)
			{
				Node Curr_child = (Node) childNodes.elementAt(i);
				newElement.appendChild(Curr_child.cloneNode(deep));
			}
		}
		
		return newElement;
	}
    
	/**
	 * Initialize element and subelements after manual addition to DOM tree.
     * This is used for manually initializing an element and its children after it and all
     * its children have been manually added to the document.
	 * Note that element/children initialized callbacks are only done to this element,
	 * as it is a "root" element which may have to be handled specially. All the child
	 * elements will get element/chidlren parsed callbacks.
	 */

	public void initialize() {
        // We have to cache this so that an element can disable callbacks in
        // elementParsed and re-enable them in childrenParsed
        // (if we don't cache, childrenParsed would never be called)
        boolean elCallbacksEnabled = ownerDocument.elementCallbacksEnabled;
        boolean wgCallbacksEnabled = ownerDocument.widgetCallbacksEnabled;
        
        preInitialize(elCallbacksEnabled, wgCallbacksEnabled);
        
        Node child = getFirstChild();
        while (child != null) {
            if (child instanceof Element) {
                ((Element)child).recursiveInitialize();
            }
            child = child.getNextSibling();
        }
        
        postInitialize(elCallbacksEnabled, wgCallbacksEnabled);       
    }

    protected void recursiveInitialize() {
        // We have to cache this so that an element can disable callbacks in
        // elementParsed and re-enable them in childrenParsed
        // (if we don't cache, childrenParsed would never be called)
        boolean elCallbacksEnabled = ownerDocument.elementCallbacksEnabled;
        boolean wgCallbacksEnabled = ownerDocument.widgetCallbacksEnabled;
        
        preParse(elCallbacksEnabled, wgCallbacksEnabled);
        
        Node child = getFirstChild();
        while (child != null) {
            if (child instanceof Element) {
                ((Element)child).recursiveInitialize();
            }
            child = child.getNextSibling();
        }
        
        postParse(elCallbacksEnabled, wgCallbacksEnabled);
    }
    
    
    public void preParse(boolean elementCallbacksEnabled, boolean widgetCallbacksEnabled) {
        boolean callWidgetFactory = true;
        if (elementCallbacksEnabled) {
            callWidgetFactory &= elementParsed();
        }
        callWidgetFactory &= widgetCallbacksEnabled;
        if (ownerDocument.widgetFactory != null && callWidgetFactory) {
            ownerDocument.widgetFactory.elementParsed(this);
        }  
    }
    
    public void preInitialize(boolean elementCallbacksEnabled, boolean widgetCallbacksEnabled) {
        boolean callWidgetFactory = true;
        if (elementCallbacksEnabled) {
            callWidgetFactory &= elementInitialized();
        }
        callWidgetFactory &= widgetCallbacksEnabled;
        if (ownerDocument.widgetFactory != null && callWidgetFactory) {
            ownerDocument.widgetFactory.elementInitialized(this);
        }
    }
    
    public void postParse(boolean elementCallbacksEnabled, boolean widgetCallbacksEnabled) {
        boolean callWidgetFactory = true;
        if (elementCallbacksEnabled) {
            callWidgetFactory &= childrenParsed();
        }
        callWidgetFactory &= widgetCallbacksEnabled;
        if (ownerDocument.widgetFactory != null && callWidgetFactory) {
            ownerDocument.widgetFactory.childrenParsed(this);
        }
    }

    public void postInitialize(boolean elementCallbacksEnabled, boolean widgetCallbacksEnabled) {
        boolean callWidgetFactory = true;
        if (elementCallbacksEnabled) {
            callWidgetFactory &= childrenInitialized();
        }
        callWidgetFactory &= widgetCallbacksEnabled;
        if (ownerDocument.widgetFactory != null && callWidgetFactory) {
            ownerDocument.widgetFactory.childrenInitialized(this);
        }
    }
    
    public void notifyRemove() {
        Node child = getFirstChild();
        while (child != null) {
            if (child instanceof Element) {
                ((Element)child).notifyRemove();
            }
            child = child.getNextSibling();
        }
        boolean callWidgetFactory = true;
        if (ownerDocument.elementCallbacksEnabled) {
            callWidgetFactory &= removingElement();
        }
        callWidgetFactory &= ownerDocument.widgetCallbacksEnabled;
        if (ownerDocument.widgetFactory != null && callWidgetFactory) {
            ownerDocument.widgetFactory.removingElement(this);
        }
    }
    
    public void printSubtree() {
        try {
            KXmlSerializer serializer = new KXmlSerializer();
            serializer.setOutput(System.out, "UTF-8");
            getOwnerDocument().write(serializer, this, null);
        } catch (IOException ex) {
            //#debug warn
            System.out.println("Could not print subtree: " + ex);
        }
    }
    
}
