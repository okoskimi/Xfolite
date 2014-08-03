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
import com.nokia.xfolite.xforms.model.XFormsModelException;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.events.*;


public class RepeatItemElement extends ItemsetItemElement implements DOMEventListener{
    
	RepeatItemElement(Document ownerDocument, String aNamespaceURI, String aPrefix,
            String aLocalName) {
        super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
    }
    
    
    public boolean elementParsed() {
        super.elementParsed();
        // We need to create a group that corresponds to the repeatitem
        // so we can easily add and delete items.
        this.addEventListener(DOMEvent.DOM_FOCUS_IN, this, false);
        return true; 
    }
    
    public boolean childrenParsed() {
        super.childrenParsed();
        return true; 
    }
    
	public void setAsSelected()
	{
		RepeatElement repeatNode = (RepeatElement) getParentNode(RepeatElement.class);
		if (repeatNode == null)
		{
			throw new XFormsModelException("RepeatItem not enclosed by repeat!");
		}
		repeatNode.setSelected(this);
	}
	
	public boolean isSelected() {
		RepeatElement repeatNode = (RepeatElement) getParentNode(RepeatElement.class);
		if (repeatNode == null)
		{
			throw new XFormsModelException("RepeatItem not enclosed by repeat!");
		}
		return repeatNode.isSelected(this);	
	}

/**
 * Ensures widget callbacks are made when this element is removed.
 * This allows removing the Container widget mapped to this element.
 * @see com.nokia.xfolite.xml.dom.Element#removingElement()
 */
	public boolean removingElement() {
        return true;
    }
	
    public void handleEvent(DOMEvent evt) {
        if (evt.getType() == DOMEvent.DOM_FOCUS_IN) {
            setAsSelected();
        }
    }
    
}
