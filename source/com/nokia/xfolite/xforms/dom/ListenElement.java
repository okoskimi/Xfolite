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

import java.util.Hashtable;

import com.nokia.xfolite.xml.dom.*;
import com.nokia.xfolite.xml.dom.events.DOMEvent;
import com.nokia.xfolite.xml.xpath.*;

public class ListenElement extends ActionElement {

	ListenElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
        super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
	}
        
    public void handleEvent(DOMEvent evt) {
        String src = this.getAttribute("src");
        String ref = getAttribute("ref");
        String userRef = getAttribute("user");
        String action =getAttribute("action");
        String frequencyAttr = getAttribute("frequency");
        if (src == "" || ref == "" || action == "") {
            logError("Empty src, ref or action attribute");
            return;
        }
        String user = "";
        try {
            if (userRef != null) {
                user = getValue(userRef).asString();
            }
        } catch (Exception ignore) {}
        NodeSet nset = getValue(ref).asNodeSet();
        Node refNode = null;
        if (nset != null) {
            refNode = nset.firstNode();
        }
        if (refNode == null) {
            logError("Reference binding failed");
        }
        int frequency = 1000;
        if (frequencyAttr != "") {
            try {
                frequency = Integer.parseInt(frequencyAttr);
            } catch (NumberFormatException e) {
                logError("Frequency is not a number (defaulting to 1000)");
            }            
        }
            

        if (action.equals("start")) {
            ((XFormsDocument)ownerDocument).startEventProvider(user,src, refNode, frequency, null);
        } else if (action.equals("stop")){
            ((XFormsDocument)ownerDocument).stopEventProvider(src);            
        } else if (action.equals("initialize")){
            ((XFormsDocument)ownerDocument).initializeEventProvider(user, src);
        } else if (action.equals("get")) {
            ((XFormsDocument)ownerDocument).getEventProvider(src, refNode, null);
        } else {
            logError("Unknown action definiton");
        }
    }

}
