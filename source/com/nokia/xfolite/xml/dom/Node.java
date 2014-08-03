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

import org.xmlpull.v1.IXmlSerializer;

import com.nokia.xfolite.xml.dom.events.DOMEvent;
import com.nokia.xfolite.xml.dom.events.DOMEventListener;
import com.nokia.xfolite.xml.xpath.XPathNSResolver;


public abstract class Node implements XPathNSResolver {
	  // NodeType
	public static final byte      ELEMENT_NODE       = 1;
	public static final byte       ATTRIBUTE_NODE     = 2;
	public static final byte       TEXT_NODE          = 3;
	public static final byte       CDATA_SECTION_NODE = 4;
	public static final byte       ENTITY_REFERENCE_NODE = 5;
	public static final byte       ENTITY_NODE        = 6;
	public static final byte       PROCESSING_INSTRUCTION_NODE = 7;
	public static final byte       COMMENT_NODE       = 8;
	public static final byte       DOCUMENT_NODE      = 9;
	public static final byte       DOCUMENT_TYPE_NODE = 10;
	public static final byte       DOCUMENT_FRAGMENT_NODE = 11;
	public static final byte       NOTATION_NODE      = 12;
	                                                 // raises(DOMException) on setting
	                                                 // raises(DOMException) on retrieval
	protected  Node   parentNode;
	
	protected  Node   previousSibling;
	protected  Node   nextSibling;
	protected  Document    ownerDocument;
    
	protected  Object userData;

    protected Node(Document ownerDocument) {
        this.ownerDocument = ownerDocument;
    }
    
	public String getNodeName()
	{
		return null;
	}
	
	public String getNodeValue()
	{
		return null;
	}
	  
	abstract public byte getNodeType();
	  
	public Node getParentNode()
	{
		return parentNode;
	}
	  
	public Node getChild(int index)
	{
		  return null;
	}
	  
	public int getChildCount()
	{
		  return 0;
	}
	  
	
	public Node getFirstChild()
	{
		return null;
	}
	
	
	public Node getLastChild()
	{
		return null;  
	}
	  
	public Node getPreviousSibling()
	{
		return previousSibling;
	}
	
	public Node getNextSibling()
	{
		return nextSibling;
	}
	  
	public Document getOwnerDocument()
	{
		return ownerDocument;
	}
	  
	public Node insertBefore(Node newChild, Node refChild)
	{
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,"");
	}
	  
	public Node replaceChild(Node newChild, Node oldChild)
	{
		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,"");
	}
	  
	public Node removeChild(Node oldChild)
	{
		throw new DOMException(DOMException.NOT_FOUND_ERR,"");
	}	  
	
	public Node appendChild(Node newChild)
	{
		throw new DOMException(DOMException.NOT_FOUND_ERR,"");
	}
	
	public boolean hasChildNodes()
	{
		return false;
	}
	  
	public String getNamespaceURI()
	{
		return null;
	}
	
	public String getPrefix()
	{
		return null;
	}
	
	public String getLocalName()
	{
		return null;
	}
	
	public void setText(String text)
	{
		byte nodetype= getNodeType();
		switch(nodetype)
		{
			case Node.ELEMENT_NODE:
			{
	            Node firstChild = getFirstChild();
	            if (firstChild != null) { // Optimize for the frequent case that there is already a text node
	                if (firstChild.getNodeType() == Node.TEXT_NODE) {
	                    ((Text)firstChild).setData(text);
	                    return;
	                } else { // Remove all children before adding the text node
	                	Node n;
	                	while((n = getFirstChild()) != null) {
	                		this.removeChild(n);
	                	}
	                }
	            }
                Text textnode = ownerDocument.createTextNode(text);
                appendChild(textnode);
			}
			break;
			
			case Node.ATTRIBUTE_NODE:
			{
				((Attr)this).setValue(text);
			}
			break;
			
			case Node.TEXT_NODE:
			case Node.CDATA_SECTION_NODE:
			case Node.COMMENT_NODE:
			{
				((CharacterData)this).setData(text);
			}
			break;
		}
	} 
        
	public String getText()
	{
		byte nodetype= getNodeType();
		String result=null;
		switch(nodetype)
		{
			case Node.ELEMENT_NODE:
			case Node.DOCUMENT_NODE:
			{
				StringBuffer sb = null;
				int child_count = getChildCount();
				for (int i = 0; i < child_count; i++)
				{	
					Node curr_child = getChild(i);
					String child_stringvalue = curr_child.getText();
					if (child_stringvalue.length()>0)
					{
						if(sb==null) {
							sb = new StringBuffer(child_stringvalue.length());
                        }
						sb.append(child_stringvalue);
					}
				}
				
				if (sb!=null)
					result = sb.toString();
			}
			break;
			
			case Node.ATTRIBUTE_NODE:
			{
				result = ((Attr)this).getValue();
			}
			break;
			
			case Node.TEXT_NODE:
			case Node.CDATA_SECTION_NODE:
			case Node.COMMENT_NODE:
			{
				result = ((CharacterData)this).getData();
			}
			break;
		}
		
		return result==null?"":result;
	}
	  
	  //Node                      cloneNode(in boolean deep);
	  
	public boolean hasAttributes()
	{
		return false;
	}
	
	public Attr getAttribute(int index)
	{
		  return null;
	}
	  
	public int getAttributeCount()
	{
		  return 0;
	}
	
	abstract public Node cloneNode(boolean deep);
	
	public String lookupNamespaceURI(String prefix) 
	{ 
	  if (prefix!=null) prefix = prefix.intern();
	  
	  byte nodeType = getNodeType();
	  String ns = getNamespaceURI();
      String pre = getPrefix();
	  
	  switch (nodeType) { 
	     case ELEMENT_NODE: 
	     { 
	    	 if ( (ns != null && ns!="")
	    	   && ((pre==prefix)
	    		  || ((pre == null ||  pre=="") && (prefix == null ||  prefix==""))
	    			    )) 
	         { 
	               // Note: prefix could be "null" in this case we are looking for default namespace 
	               return ns;
	         } 
	         
	    	 if ( hasAttributes() )
	         { 
	            int Attr_count = getAttributeCount();
	        	for (int i = 0; i < Attr_count; i++) {
					Attr curr_attr= getAttribute(i);
					if ((curr_attr.isPrefixedNSDeclaration()
							&& curr_attr.localName==prefix)
					|| ((prefix == null ||  prefix=="")
							&& curr_attr.isDefaultNSDeclaration()))
					{
						return curr_attr.value;
					}
				}
	         }
 
	     } 
	     break;
	     
	     case DOCUMENT_NODE: 
	          return ((Document) this).documentElement.lookupNamespaceURI(prefix); 

	     case ENTITY_NODE: 
	     case NOTATION_NODE: 
	     case DOCUMENT_TYPE_NODE: 
	     case DOCUMENT_FRAGMENT_NODE: 
	         return null; 

	     case ATTRIBUTE_NODE:
	    	 //parent node of attr used as owner element
	     {
	         Attr a= (Attr) this;
	    	 if (a.ownerElement!=null) 
	         { 
	             return a.ownerElement.lookupNamespaceURI(prefix); 
	         } 
	         return null;
	     }
	  }
	  
	  // default
      //Search for an ancestor element
      Node AncestorElement = parentNode;
      while (AncestorElement!=null 
     		 && AncestorElement.getNodeType()!=ELEMENT_NODE)
     	 AncestorElement = AncestorElement.parentNode;
      
      if ( AncestorElement!=null  ) //if ancestor element found
         // EntityReferences may have to be skipped to get to it 
      { 
         return AncestorElement.lookupNamespaceURI(prefix); 
      } 
      return null;
	}
	
	abstract protected void  write(IXmlSerializer writer, NodeFilter filter) throws IOException;

    
	
	
	// TODO: should these be in NamedNode?
    public Object getUserData() {
        return userData;
    }
    
    public void setUserData(Object userData) {
        this.userData = userData;
    }
    
    //DOM events EventTarget interface methoda
    public void addEventListener(int type, 
            DOMEventListener listener, 
            boolean useCapture)
    {	
    }

    public void removeEventListener(int type, 
               DOMEventListener listener, 
               boolean useCapture)
    {	
    }

    /**
     * Dispatch an event for this element.
     * Only event handlers for this element will see the event.
     * @param evt Event to dispatch
     * @return true if default action was executed
     */
    
    public boolean dispatchLocalEvent(DOMEvent evt)
    {
    	return evt.dispatchEvent(this, true);
    }

    public boolean dispatchLocalEvent(int ev)
    {
        DOMEvent evObj = new DOMEvent(ev, this);
        return dispatchLocalEvent(evObj);
    }  
    
    /**
     * Dispatch an event for this element
     * @param evt Event to dispatch
     * @return true if default action was executed
     */    
    public boolean dispatchEvent(DOMEvent evt)
    {
        return evt.dispatchEvent(this, false);
    }
    
    public boolean dispatchEvent(int ev)
    {
        DOMEvent evObj = new DOMEvent(ev, this);
        return dispatchEvent(evObj);
    }
    
    public void notifyCapturingEventListeners(DOMEvent evt)
    {	
    }
    
    public void notifyNonCapturingEventListeners(DOMEvent evt)
    {	
    }
    
    public void notifyAllEventListeners(DOMEvent evt)
    {
    }
    
    public void defaultAction(DOMEvent evt)
    {
    }
}
