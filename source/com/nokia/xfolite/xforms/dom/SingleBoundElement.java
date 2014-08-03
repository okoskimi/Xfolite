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
import com.nokia.xfolite.xforms.model.UIBinding;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Node;



/**
 * A class for binding UI elements to parts of the instance document.
 * TODO: add NameSpaceResolver 
 * 
 * @author mattisil
 *
 */
public class SingleBoundElement extends BoundElement {
	
    
	/**
	 * Construct this binding from ref, value, or nodeset attributes.
	 * 
	 * @param xPathString  the XPath expression as a string
	 * @param contextNode  the context node for the xpath evaluation
	 * @param bindingType  one of the binding types defined in BoundElement
	 * @param model        the model in which the bound node will be
	 * @param doc
	 */
    
    SingleBoundElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName)

	{
		super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
    }
        
    public int getBindingType() {
        return UIBinding.SINGLE_NODE_BINDING;
    }
    
    /**
     * 
     * @return either the instance item, or NULL is not bound
     */
    public InstanceItem getBoundInstanceItem()
    {
    	if (this.binding!=null)
    	{
    		Node n = this.binding.getBoundNode();
    		if (n!=null)
    		{
    			return this.getModel().getInstanceItemForNode(n);
    		}
    	}
    	return null;
    }
	
}
