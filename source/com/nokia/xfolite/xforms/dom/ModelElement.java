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


import com.nokia.xfolite.xforms.model.XFormsModel;
import com.nokia.xfolite.xforms.model.XFormsModelUI;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.events.DOMEvent;

import java.util.*;
import org.kxml2.io.KXmlSerializer;

// Note - There must be only one model per document, or we must separate uimodel from document.
public class ModelElement extends XFormsElement {
    
    private XFormsModelUI modelUI = null;
    
    protected ModelElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
		super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
    }
    
    public boolean elementParsed() {
        super.elementParsed();
        XFormsDocument doc = (XFormsDocument) ownerDocument;
        modelUI = new XFormsModelUI(getAttribute("id"),this);
        doc.setModelElement(this);
        return true;
    }
    
    public boolean childrenParsed() {
        super.childrenParsed();
        // This should not use eventing
        XFormsDocument doc = (XFormsDocument) ownerDocument;
        Hashtable extras = doc.getExtraInstances();
        XFormsModel model = getModel();

        // In practice this should not be used, always replace an existing inline instance.
        if (extras != null) {
            Enumeration e = extras.keys();
            while (e.hasMoreElements()) {
                String id = (String) e.nextElement();
                Document iDoc = (Document) extras.get(id);
                model.addInstance(iDoc, id);
                //#debug
                System.out.println("Added extra instance for " + id);
            }
        }
        model.rebuild();
        return false;
    }
    public XFormsModel getModel() {
        return modelUI.getModel();
    }
    
    public XFormsModelUI getModelUI() {
        return modelUI;
    }
    
	public void rebuild() {
		if (dispatchEvent(DOMEvent.XFORMS_REBUILD))
		{
            long ts = System.currentTimeMillis();
			//((XFormsDocument)this.getOwnerDocument()).log(UserInterface.LVL_STATUS,"doing rebuild" , this);
			getModel().rebuild();
			//((XFormsDocument)this.getOwnerDocument()).log(UserInterface.LVL_STATUS, "Rebuild took: " + (System.currentTimeMillis() - ts) + " ms." , this);
		}
	}

	public void recalculate()
	{
		if (dispatchEvent(DOMEvent.XFORMS_RECALCULATE))
		{
			//((XFormsDocument)this.getOwnerDocument()).log(UserInterface.LVL_STATUS,"doing recalc" , this);
			getModel().recalculate();
			//((XFormsDocument)this.getOwnerDocument()).log(UserInterface.LVL_STATUS,"done recalc" , this);
		}
	}

	public void refresh()
	{
		if (dispatchEvent(DOMEvent.XFORMS_REFRESH))
		{
			modelUI.refresh();
		}
	}
	
}
