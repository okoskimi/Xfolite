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

import com.nokia.xfolite.xforms.dom.CaseElement;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.events.DOMEvent;

public class ToggleElement extends ActionElement {

	ToggleElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
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
    	//#debug info
    	System.out.println("********** Toggle: got event " + DOMEvent.getNameFromType(evt.getType())
    			+ ", sent to " + evt.getTarget().getLocalName() + " with id " + ((Element) evt.getTarget()).getAttribute("id"));
        String caseAttr = getAttribute("case");
        //#debug info
        System.out.println("Toggling to case " + caseAttr);
        Element caseEl = null;
        if (caseAttr != "") {
            caseEl = ((XFormsDocument)ownerDocument).getElementById(caseAttr);
        } else {
            logError("No case element id given in toggle");
            return;
        }
        if (caseEl != null && caseEl instanceof CaseElement) {
            ((CaseElement)caseEl).selectThis();
        } else {
            logError("No such case element id: " + caseAttr);
        }
    }

}
