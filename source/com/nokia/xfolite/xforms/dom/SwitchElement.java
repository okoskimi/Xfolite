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

import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.events.DOMEvent;

public class SwitchElement extends XFormsElement {

	private CaseElement selected = null;
	
	SwitchElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
        super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
	}

    public boolean elementParsed() {
        return true;
    }
    
    public boolean childrenParsed() {
        super.childrenParsed();
        //TODO: Use the generated case list instead!
        boolean setFromSelected = false;
        Node child = this.getFirstChild();
        while (child != null) {
            if (child instanceof CaseElement) {
                CaseElement caseEl = (CaseElement)child;
                if (selected == null) {
                    selected = caseEl;
                } else if (!setFromSelected && caseEl.getAttribute("selected") == "true") {
                    selected = caseEl;
                    setFromSelected = true;
                }                
            }
            child = child.getNextSibling();
        }
        // NOTE: UI must be able to handle this event even though form might not have been
        // fully parsed yet
        if (selected != null) {
            selected.dispatchLocalEvent(DOMEvent.XFORMS_ENABLED);
        }
        return true;
    }
    
    
	/**
	 * Set selected case by its string id.
	 * Note that this does not check that the case is actually a child of this node.
	 */	
	public void setSelectedCase(String caseId)
	{
		//#debug info
		System.out.println("******************* setSelectedCase(" + caseId + ")");
		XFormsElement caseNode = (XFormsElement) ((XFormsDocument)ownerDocument).getElementById(caseId);
		if (caseNode instanceof CaseElement)
		{
			setSelectedCase((CaseElement) caseNode);
		}
	}
	public void setSelectedCase(CaseElement caseNode)
	{
		//#debug info
		System.out.println("******************* setSelectedCase(element with id " + caseNode.getAttribute("id") + ")");
		if (caseNode == selected) {
			//#debug info
			System.out.println("Case was already selected, ignored.");
			return;
		}
		if (selected != null) {
			selected.dispatchLocalEvent(DOMEvent.XFORMS_DISABLED);
		}
		
		selected = caseNode;
		selected.dispatchLocalEvent(DOMEvent.XFORMS_ENABLED);
	}

	public CaseElement getSelectedCase()
	{
		return selected;
	}

}
