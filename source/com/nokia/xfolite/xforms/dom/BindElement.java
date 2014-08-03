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
import com.nokia.xfolite.xforms.model.MIPExpr;
import com.nokia.xfolite.xforms.model.XFormsModel;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.events.DOMEvent;

public class BindElement extends XFormsElement {

    protected BindElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
		super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
	}
    
    public boolean elementParsed() {
        super.elementParsed();
        
        XFormsModel model = getModel();
        
        Bind bind = new Bind(model, null, model.getDefaultContextNode(), getAttribute("id"));
        
        String nodeset = getAttribute("nodeset");
        String type = getAttribute("type");
        String p3ptype = getAttribute("p3ptype");
        String relevant = getAttribute("relevant");
        String required = getAttribute("required");
        String readonly = getAttribute("readonly");
        String calculate = getAttribute("calculate");
        String constraint = getAttribute("constraint");


        bind.setNodeSet(nodeset);
        
        if (type != "") {
            bind.setStaticMIP(type, MIPExpr.TYPE);
        }
        if (p3ptype != "") {
            bind.setStaticMIP(p3ptype, MIPExpr.P3PTYPE);
        }
        
        if (relevant != "") {
            bind.setMIPExpr(relevant, MIPExpr.RELEVANT);
        }
        if (required != "") {
            bind.setMIPExpr(required, MIPExpr.REQUIRED);
        }
        if (readonly != "") {
            bind.setMIPExpr(readonly, MIPExpr.READONLY);
        }
        if (calculate != "") {
            bind.setMIPExpr(calculate, MIPExpr.CALCULATE);
        }
        if (constraint != "") {
            bind.setMIPExpr(constraint, MIPExpr.CONSTRAINT);
        }
        
        model.addBind(bind);
        
        return false;
    }

}
