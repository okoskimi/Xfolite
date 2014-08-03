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

package com.nokia.xfolite.xforms.dom;

import com.nokia.xfolite.xforms.model.XFormsModel;
import com.nokia.xfolite.xml.dom.Attr;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.events.DOMEvent;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.nokia.xfolite.xml.xpath.XPathContext;
import com.nokia.xfolite.xml.xpath.XPathResult;

public class DeleteElement extends ActionElement {

	DeleteElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
        super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
	}

    public boolean elementParsed() {
        super.elementParsed();
        return true;
    }
    
    public boolean childrenParsed() {
        super.childrenParsed();
        return true;
    }
    
    public void doHandleEvent(DOMEvent evt, ActionElement outermost) {
        	XPathContext deleteContext = this.getInsDelContext();
        	if (deleteContext!=null)
        	{
        		//#debug info
        		System.out.println("DeleteElement.doHandleEvent");
        		NodeSet nodes = this.getNodeset(deleteContext);
        		int at = this.getAt(deleteContext);
        		if (at==-1||at>nodes.getLength()) at=nodes.getLength();
        		if (nodes.getLength()==0)
        		{
        			this.logWarning("Delete nodes empty: "+this.getAttribute("nodeset"));
        			return;
        		}
       			Node nodeToBeDeleted=nodes.item(at-1);
        		if (nodeToBeDeleted==null)
        		{
        			this.logWarning("Node to be deleted null at: "+at);
        			return;
        		}
        		this.logStatus("delete action parent:"+nodeToBeDeleted.getParentNode().getLocalName()+" : "+nodeToBeDeleted.getLocalName());        		
       			Node parent = nodeToBeDeleted.getParentNode();
       			//#debug info
       			System.out.println("Node to be deleted: " + nodeToBeDeleted.getLocalName());
                parent.removeChild(nodeToBeDeleted);
                //#debug info
                System.out.println("Node deleted: " + nodeToBeDeleted.getLocalName());
                //((Element)parent).printSubtree();
       			outermost.setLocalState(ActionElement.REBUILD,true);
       			outermost.setLocalState(ActionElement.REWIRE,true);
        	}
    }
    

    
    
    
    protected NodeSet getNodeset(XPathContext context)
    {
        Attr bind = this.getAttributeNode("bind");
        if (bind==null)
        {
        	Attr nodeset = this.getAttributeNode("nodeset");
        	if (nodeset!=null)
        	{
        		XFormsModel model = this.getModel();
        		XPathResult result = model.getXPathEvaluator().evaluate(nodeset.getValue(), context, this, XPathResult.NODESET);
        		return result.asNodeSet();
        	}
        }
        return null;
    	
    }
    
    protected int getAt(XPathContext context)
    {
        Attr at = this.getAttributeNode("at");
        if (at!=null)
        {
        	String atStr="round("+at.getValue()+")";
    		XFormsModel model = this.getModel();
    		XPathResult result = model.getXPathEvaluator().evaluate(atStr, context, this, XPathResult.ANY);
    		Double number = result.asNumber();
    		return number.intValue();
        }
        return -1;
    }
    
    protected XPathContext getContextFromParent()
    {
    	Element e=(Element)this.getParentNode();
    	while (e!=this.getOwnerDocument().getDocumentElement())
    	{
    		if (e instanceof BoundElement)
    		{
    			BoundElement be = (BoundElement)e;
    			XPathContext c = be.getContext();
    			if (c != null) {
                    return c;
                }
    		}
    		e = (Element) e.getParentNode();
    	}
    	return ((XFormsDocument)this.getOwnerDocument()).getModelElement().getContext();
    }
    
    
    protected XPathContext getInsDelContext()
    {
        Attr context = this.getAttributeNode("context");
    	XPathContext parentContext = this.getContextFromParent();
    	if (parentContext != null && parentContext.contextNode != null)
    	{
    		if (context==null) {
                return parentContext;
            }
    		XFormsModel model = ((XFormsDocument)this.getOwnerDocument()).getModel();
    		XPathResult result = model.getXPathEvaluator().evaluate(context.getValue(), parentContext, this, XPathResult.ANY);
    		if (result.getType()==XPathResult.NODESET)
    		{
    			return new XPathContext(result.asNode(), 1, 1);
    		}
    	}
    	else
    	{
    		logError("Cannot access parent context");
    	}
    	return null;
    }

}
