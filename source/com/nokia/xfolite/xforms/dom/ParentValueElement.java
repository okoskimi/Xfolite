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

import java.util.Vector;
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
 * A ValueBoundElement which registers itself to its parent.
 *
 */
public class ParentValueElement extends ValueBoundElement {
	
	protected boolean m_multiValue;
    
	/**
	 * Construct this binding from ref, value, or nodeset attributes.
	 * 
	 * @param xPathString  the XPath expression as a string
	 * @param contextNode  the context node for the xpath evaluation
	 * @param bindingType  one of the binding types defined in BoundElement
	 * @param model        the model in which the bound node will be
	 * @param doc
	 */
    
    ParentValueElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName)

	{
		this(ownerDocument, aNamespaceURI, aPrefix, aLocalName, false);

    }

    ParentValueElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName, boolean multiValue)

	{
		super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
		m_multiValue = multiValue;
	}
    
  
    
    public boolean elementParsed() {
        boolean rval = super.elementParsed();
        if (parentNode instanceof XFormsElement) {
            //#debug
            System.out.println("Registering to " + parentNode.getLocalName()
                    + " under " + localName);
            if (m_multiValue) {
            	Vector v = (Vector)((XFormsElement)parentNode).getUserData(localName);
            	if (v == null) {
            		v = new Vector();
            		((XFormsElement)parentNode).setUserData(localName, v);
            	}
            	v.addElement(this);            	
            } else {
            	((XFormsElement)parentNode).setUserData(localName, this);
            }
        }
        return rval;
    }

}
