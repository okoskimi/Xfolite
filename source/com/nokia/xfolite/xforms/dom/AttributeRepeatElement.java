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

import com.nokia.xfolite.xforms.model.Bind;
import com.nokia.xfolite.xforms.model.UIBinding;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Node;

public class AttributeRepeatElement extends RepeatElement {

	AttributeRepeatElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
        super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
	}
	
	
	public boolean useRepeatPrefix() { return true; }
	
    public String getItemName() {
        return "table-item";
    }

    private int rowsPerItem = -1;
    public int getRowsPerItem() {
    	if (this.rowsPerItem < 0) {
    		Node n = template.getFirstChild();
    		this.rowsPerItem = 0;
    		while(n != null) {
    			if (n.getNodeType() == Node.ELEMENT_NODE && n.getLocalName() == "tr") {
    				this.rowsPerItem++;
    			}
    			n = n.getNextSibling();
    		}
    	}
    	return this.rowsPerItem;
    }
    
    public boolean elementParsed() {
        boolean rval = super.elementParsed();
        if (parentNode instanceof XFormsElement) {
            //#debug
            System.out.println("Registering to " + parentNode.getLocalName()
                    + " under " + localName);
            ((XFormsElement)parentNode).setUserData(localName, Boolean.TRUE);
        }
        return rval;
    }
}
