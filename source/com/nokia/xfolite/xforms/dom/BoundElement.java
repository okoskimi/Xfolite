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


import com.nokia.xfolite.xforms.model.*;
import com.nokia.xfolite.xforms.model.datatypes.*;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.events.*;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.nokia.xfolite.xml.xpath.XPathContext;
import com.nokia.xfolite.xml.xpath.XPathExpression;
import com.nokia.xfolite.xml.xpath.XPathNSResolver;
import com.nokia.xfolite.xml.xpath.XPathResult;


/**
 * A class for binding UI elements to parts of the instance document.
 * TODO: add NameSpaceResolver 
 * 
 * @author mattisil
 *
 */
public abstract class BoundElement extends XFormsElement implements ValueProvider {

    protected UIBinding binding = null;
    protected XPathContext context = null;
    
	/**
	 * Construct this binding from ref, value, or nodeset attributes.
	 * 
	 * @param xPathString  the XPath expression as a string
	 * @param bindingType  one of the binding types defined in BoundElement
	 * @param model        the model in which the bound node will be
	 * @param doc
	 */
    
    BoundElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName)

	{
		super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
    }

    public abstract int getBindingType();
    public boolean useRepeatPrefix() { return false; }
    
    
	public boolean elementParsed()
	{
        super.elementParsed();
        int bindingType = getBindingType();
        boolean useRepeatPrefix = useRepeatPrefix();
 
        if (binding == null) {
            String bindAttr = getAttribute(useRepeatPrefix ? "repeat-bind" : "bind");
            String xpathAttr = "";
            if (bindAttr == "") {
                if (bindingType == UIBinding.NODE_SET_BINDING) {
                    xpathAttr = getAttribute(useRepeatPrefix ? "repeat-nodeset" : "nodeset");
                } else {
                    xpathAttr = getAttribute("ref");
                    if (xpathAttr == "" && bindingType == UIBinding.VALUE_BINDING) {
                        xpathAttr = getAttribute("value");
                    } else {
                        bindingType = UIBinding.SINGLE_NODE_BINDING;
                    }
                }
            }
            if (bindAttr != "") {
                Bind bind = getModel().getBind(bindAttr);
                if (bind != null) {
                	//#debug info
                	System.out.println("Using binding for " + getLocalName() + ": " + bind.getNodeSetExpr());
                    binding = new UIBinding(super.getContext(), bind.getNodeSetExpr(),
                            bindingType,
                            getModelUI(), this);
                }
            } else if (xpathAttr != "") {
            	//#debug info
            	System.out.println("Using binding for " + getLocalName() + ": " + xpathAttr);
                binding = new UIBinding(super.getContext(), xpathAttr,
                        bindingType,
                        getModelUI(), this, this);
            } else {
                // TODO: Handle missing ref attribute (form control should be
                // hidden)
            }
        }
    
        return true;
    }

    public boolean childrenParsed() {
        super.childrenParsed();
        return true;
    }

    public boolean removingElement() {
        super.removingElement();
        if (binding != null) {
            this.getModelUI().removeUIBinding(binding);
            binding = null;
        }
        return true;
    }
    
    /**
     * Set the value of a node for a binding with a single node.
     * 
     * @param string  the new string value for the bound node
     */
    public void setStringValue(String string) {
        //#debug
        System.out.println("BoundElement.setStringValue");
        if (binding != null) {
            binding.setStringValue(string);
        }
    }

    /**
     * Get the value of a node for a binding with a single node.
     * 
     * @return  the string value of the bound node
     */
    public String getStringValue() {
        if (binding == null) {
            return null;
        }
        return binding.getStringValue();
    }
    
    public String getDisplayString() {
        if (!isBound()) {
            return null;
        }
        DataTypeBase dt = binding.getDataType();
        if (dt == null) {
            return getStringValue();
        }
        return dt.getDisplayString(binding);
    }

	public boolean getBooleanState(int aMIPStateId) {
        if (binding == null) {
        	switch (aMIPStateId) {
        	case MIPExpr.READONLY:
        	case MIPExpr.REQUIRED:
        		return false;
        	default:
        		return true;
        	}
        }
        return binding.getBooleanState(aMIPStateId);
	}
	
    public DataTypeBase getDataType() {
        if (binding == null) {
            return null;
        }
        
        return binding.getDataType();
    }
    
	public boolean setContext(XPathContext context) {
		if (binding == null) {
		    return false;
        }
		return binding.setContext(context);
	}
	
    
    public boolean isBound() {
        if (binding == null) {
            return false;
        }
        return binding.getBindingStatus() == UIBinding.BOUND;
    }


    public NodeSet getBoundNodes() {
        if (binding == null) {
            return null;
        }
        return binding.getBoundNodes();
    }
    
    public XPathContext getContext()
    {
        if (!isBound()) {
            return super.getContext();
        }
        Node contextNode = binding.getBoundNode(); // Will return null unless single node bound
        if (contextNode == null) {
            return super.getContext();
        } else {
            if (context == null || context.contextNode != contextNode) {
                context = new XPathContext(contextNode, 1, 1);
            }
            return context;
        }
        
    }
    
	public boolean reEvaluateOwnContext(XPathContext parentContext) {
		//((XFormsDocument)this.ownerDocument).log(UserInterface.LVL_STATUS, "reEvaluateContext:"+this, this);
		return setContext(parentContext);
	}

	public int getBindingStatus()
	{
        if (binding == null) {
            return UIBinding.UNINITIALIZED;
        }
		return binding.getBindingStatus();
    }
	
	public void defaultAction(DOMEvent evt) {
	    if (evt.getType() == DOMEvent.XFORMS_REEVALUATE) {
	        this.reEvaluateContext(super.getContext(), false);   
        }
    }
}
