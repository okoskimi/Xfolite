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

import com.nokia.xfolite.xforms.model.XFormsModelException;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.events.*;
import com.nokia.xfolite.xml.xpath.XPathContext;

import java.util.Vector;

public class CaseElement extends XFormsElement {

	protected boolean initialized = false;
	
	CaseElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
        super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
	}

	public boolean isInitialized() {
		return initialized;
	}

	public boolean ensureInitialized() {
		if (!initialized) {
			//#debug
			System.out.println("Initializing contents");
			// Flag MUST be set BEFORE calling initialize!
			// Otherwise lazy-init logic will be performed twice.
			initialized = true;
			initialize();
			this.dispatchEvent(DOMEvent.XFORMS_INITIALIZED);			
			return true;
		}
		return false;
	}

	public void reEvaluateContext(XPathContext parentContext, boolean force)
	{
		if (initialized) {
			super.reEvaluateContext(parentContext, force);
		}
	}
	
	public boolean elementParsed() {
        super.elementParsed();
    	//#debug
		System.out.println("CaseElement.elementParsed: " + getAttribute("id"));
        if (parentNode instanceof XFormsElement) {
            //#debug
            System.out.println("Registering to " + parentNode.getLocalName()
                    + " under " + localName);
            Vector caseList = (Vector) ((XFormsElement)parentNode).getUserData(localName);
            if (caseList == null) {
                caseList = new Vector();
                ((XFormsElement)parentNode).setUserData(localName, caseList);
                
            }
            if (!caseList.contains(this)) {
            	caseList.addElement(this);
            }
        }
        if (!initialized) {        	
        	if (getAttribute("lazy-init") == "true" && getAttribute("selected") != "true") {
        		// Note that the callbacksEnabled value is cached in Element.parse so 
        		// childrenParsed is still called for us.
        		ownerDocument.setCallbacksEnabled(false);
            	//#debug
        		System.out.println("Lazy init enabled for case " + getAttribute("id"));
        	} else {
        		initialized = true;
        	}
        }
        return true;
    }

    public boolean childrenParsed() {
        super.childrenParsed();
    	//#debug
		System.out.println("CaseElement.childrenParsed: " + getAttribute("id"));
        if (!initialized) {
            ownerDocument.setCallbacksEnabled(true);
            Node child = this.getFirstChild();
            while (child != null) {
            	if (child instanceof XFormsElement && child.getLocalName() == "label") {
            		((XFormsElement)child).initialize();
            	}
            	child = child.getNextSibling();
            }
        }
        return true;
    }
    
    public boolean isSelected() {
        return getSwitch().getSelectedCase() == this;
    }
    
	public void selectThis()
	{
		//#debug info
		System.out.println("******************** Case " + getAttribute("id") + " selected!");
	    getSwitch().setSelectedCase(this);
	}

    public SwitchElement getSwitch() {
        SwitchElement switchNode = (SwitchElement) getParentNode(SwitchElement.class);
        if (switchNode == null)
        {
            throw new XFormsModelException("Case without enclosing switch");
        }
        return switchNode;
    }
}
