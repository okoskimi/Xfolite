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
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.events.DOMEvent;

public class RefreshElement extends ActionElement {

	short state;
	boolean cancel;
	RefreshElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName,short st,boolean can) {
        super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
        this.state=st;
        this.cancel=can;
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
        ModelElement mel = ((XFormsDocument)ownerDocument).getModelElement();

        if (this.cancel==false)
        {
	    	switch (this.state) {
	    		case ActionElement.REFRESH :
	    			mel.refresh();
	    			break;
	    		case ActionElement.RECALCULATE:
	    			mel.recalculate();
	    			break;
	    		case ActionElement.REBUILD:
	    			mel.rebuild();
	    			break;
	    	}
        }
    	outermost.setLocalState(state,!cancel);
    }

}
