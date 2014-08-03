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


import com.nokia.xfolite.xforms.model.UIBinding;
import com.nokia.xfolite.xforms.model.datatypes.DataTypeBase;
import com.nokia.xfolite.xforms.model.datatypes.ValueProvider;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.events.DOMEvent;
import com.nokia.xfolite.xml.xpath.NodeSet;
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
public class ValueBoundElement extends BoundElement {
	
    // This is set to true when switching rendering off for child value elements
    // We then now when we get back up from subtree (childrenParsed)
    // that we should switch rendering back on
    boolean valueRoot = false;
    
	/**
	 * Construct this binding from ref, value, or nodeset attributes.
	 * 
	 * @param xPathString  the XPath expression as a string
	 * @param contextNode  the context node for the xpath evaluation
	 * @param bindingType  one of the binding types defined in BoundElement
	 * @param model        the model in which the bound node will be
	 * @param doc
	 */
    
    ValueBoundElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName)

	{
		super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
    }

  
    
    public boolean elementParsed() {
        boolean rval = super.elementParsed();
        if (ownerDocument.isWidgetCallbacksEnabled()) {
            ownerDocument.setWidgetCallbacksEnabled(false);
            valueRoot = true;
        }
        return rval;
    }

    public boolean childrenParsed() {
        boolean rval = super.elementParsed();
        if (valueRoot) {
            ownerDocument.setWidgetCallbacksEnabled(true);
        }
        return rval;
    }
    
    
    public int getBindingType() {
        return UIBinding.VALUE_BINDING;
    }

    public String getDisplayString() {
        String ds = super.getDisplayString();
        if (ds == null) {
            return getText();
        }
        return ds;
    }
    
    // Now you can use getText to get the value of a label. Also output inside a label will then work.
    public String getText() {
        if (binding == null) {
            return super.getText();
        }
        DataTypeBase type = binding.getDataType();
        String result = null;
        if (type != null) {
            result = type.getDisplayString(this);
        } else {
            result = binding.getStringValue();
        }
        return result == null ? "" : result;
    }

}
