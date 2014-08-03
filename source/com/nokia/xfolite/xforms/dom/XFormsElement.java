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

import com.nokia.xfolite.xforms.model.InstanceItem;
import com.nokia.xfolite.xforms.model.XFormsModel;
import com.nokia.xfolite.xforms.model.XFormsModelUI;
import com.nokia.xfolite.xforms.submission.XFormsXMLSerializer;
import com.nokia.xfolite.xml.dom.Attr;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.events.DOMEvent;
import com.nokia.xfolite.xml.dom.events.EventAwareElement;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.nokia.xfolite.xml.xpath.XPathContext;
import com.nokia.xfolite.xml.xpath.XPathExpression;
import com.nokia.xfolite.xml.xpath.XPathNSResolver;
import com.nokia.xfolite.xml.xpath.XPathResult;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.IXmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XFormsElement extends EventAwareElement implements XFormsNode {

    int treeDepth = 0;

    protected XFormsElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
        super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
        this.ownerDocument = ownerDocument;
        // TODO Auto-generated constructor stub
    }
    
    public void logError(String msg) {
        ((XFormsDocument)ownerDocument).log(UserInterface.LVL_ERROR, msg, this);
    }
    public void logError(Throwable t) {
        ((XFormsDocument)ownerDocument).log(UserInterface.LVL_ERROR, "Exception:" +t.toString(), this);
    }
    public void logWarning(Throwable t) {
        ((XFormsDocument)ownerDocument).log(UserInterface.LVL_WARN, "Exception:" +t.toString(), this);
    }
    public void logWarning(String msg,Throwable t) {
        ((XFormsDocument)ownerDocument).log(UserInterface.LVL_WARN, "Exception:" +msg+t.toString(), this);
    }

    public void logWarning(String msg) {
        ((XFormsDocument)ownerDocument).log(UserInterface.LVL_WARN, msg, this);
    }    
    public void logStatus(String msg) {
        ((XFormsDocument)ownerDocument).log(UserInterface.LVL_STATUS, msg, this);
    }    
    public boolean elementParsed() {
        if (parentNode != null && parentNode instanceof XFormsElement) {
            treeDepth = ((XFormsElement)parentNode).treeDepth + 1;
        }
        
        String id = getAttribute("id");
        if (id != "") {
            ownerDocument.storeId(id, this);
        }
        return false;
    }

    public int getTreeDepth() {
        return treeDepth;
    }
	public XPathContext getContext()
	{
		return ((XFormsNode) parentNode).getContext();
	}

	public XFormsModel getModel() {
		return ((XFormsDocument) ownerDocument).getModel();
	}
	
    public XFormsModelUI getModelUI() {
        return ((XFormsDocument) ownerDocument).getModelUI();
    }
    
	public XPathResult getValue(String xpath) {
        XFormsModel model = getModel();
		XPathExpression expr = model.createExpression(xpath, this);
		return expr.evaluate(getContext(), XPathResult.ANY);
	}
	
	public void setExpressionValue(String xpath, String expression)
	{
        //#debug 
        System.out.println("setExpressionValue xpath=" + xpath + ", expression=" + expression);
        XFormsModel model = getModel();
        InstanceItem item = model.getInstanceItem(xpath, getContext(),this);
		if (item == null) {
            //#debug warning
            System.out.println("Could not set " + xpath + " to " + expression + ", no such data element");
			return;
		}
		Node context = item.getNode();
		XPathExpression expr = model.createExpression(expression, this);
		String value = expr.evaluate(context, XPathResult.STRING).asString();
        //#debug 
        System.out.println("Setting value to " + value);
		item.setStringValue(value);
	}
	
	public void setStringValue(String xpath, String value)
	{
        XFormsModel model = getModel();
		InstanceItem item = model.getInstanceItem(xpath, getContext(), this);
		if (item == null) {
			return;
		}
		item.setStringValue(value);
	}
	
	public void reEvaluateContext(XPathContext parentContext, boolean force)
	{
		boolean changed = reEvaluateOwnContext(parentContext);
		if (changed || force)
		{
			reEvaluateChildContexts(force);
		}
	}
	
	public boolean reEvaluateOwnContext(XPathContext parentContext) {
		//#debug
		System.out.println("reEvaluateOwnContext: " + getLocalName());
		return false;
	}
	
	public void reEvaluateChildContexts(boolean force) {
        XPathContext context = getContext();
        Node child = getFirstChild();
        while(child != null) {
            if (child instanceof XFormsElement) {
                ((XFormsElement)child).reEvaluateContext(context, force);
            }
            child = child.getNextSibling();
        }
	}

	
	public void dispatchEventSerially(int ev)
	{
        final DOMEvent evObj = new DOMEvent(ev, this);
        Runnable r = new Runnable()
        {
        	public void run()
        	{
                dispatchEvent(evObj);
        	}
        };
        ((XFormsDocument)this.getOwnerDocument()).callSerially(r);
	}	

    public void queueEvent(int ev)
    {
        DOMEvent evObj = new DOMEvent(ev, this);
        getModelUI().queueEvent(evObj);
    }
    
  
    
	public XFormsNode getParentNode(Class parentClass)
	{
		Node parent = getParentNode();
		while ((! parentClass.isInstance(parent)) && parent != null)
		{
			parent = parent.getParentNode();
		}
		return (XFormsNode) parent;	
	}
	
	boolean dispatchEvent(DOMEvent ev, String nodeId)
	{
		XFormsElement node = (XFormsElement)ownerDocument.getElementById(nodeId);
		if (node != null) {
			return node.dispatchEvent(ev);
		} else {
			return false;
		}

	}

    public static final String USERDATA_KEY = "userdata";
    public static final String LABEL_KEY = "label";
    public static final String ALERT_KEY = "alert";
    public static final String VALUE_KEY = "value";
    public static final String HINT_KEY = "hint";
    public static final String CAPTION_KEY = "caption";
    
    
    /** Redefine userdata access here so that we can use attributes but only pay
     *  for the hashtable when we actually use attributes and not just a single data item.
     *  This lets us use continue to use a single pointer for instance data where hashtable is not needed.
     *  Note that instance data uses XML DOM classes, not XForms DOM classes, so instance data processine
     *  does not pay for the runtime type check either.
     */
    
    public void setUserData(Object data) {
        if (userData instanceof Hashtable) {
            ((Hashtable)userData).put(USERDATA_KEY, data);
        } else {
            userData = data;
        }
    }
    
    public Object getUserData() {
        if (userData instanceof Hashtable) {
            return ((Hashtable)userData).get(USERDATA_KEY);
        } else {
            return userData;
        }
    }

    public void printUserData() {
        System.out.println("UserData: " + userData);
    }
    
    public void setUserData(Object attribute, Object data) {
        if (userData instanceof Hashtable) {
            ((Hashtable)userData).put(attribute, data);
        } else {
            Hashtable h = new Hashtable();
            if (userData != null) {
                h.put(USERDATA_KEY, userData);
            }
            h.put(attribute, data);
            userData = h;
        }
    }
    
    public Object getUserData(Object attribute) {
        if (userData instanceof Hashtable) {
            return ((Hashtable)userData).get(attribute);
        } else {
            return null;
        }
    }
    

   
}