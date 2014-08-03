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


import java.io.IOException;
import java.util.Enumeration;

import org.xmlpull.v1.IXmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.nokia.xfolite.xforms.model.XFormsModel;
import com.nokia.xfolite.xml.dom.Attr;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.events.DOMEvent;
import com.nokia.xfolite.xml.dom.events.EventAwareElement;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.nokia.xfolite.xml.xpath.XPathContext;
import com.nokia.xfolite.xml.xpath.XPathExpression;
import com.nokia.xfolite.xml.xpath.XPathNSResolver;
import com.nokia.xfolite.xml.xpath.XPathResult;

interface XFormsNode  {


	public XPathContext getContext();


	public XFormsModel getModel();
    

	void reEvaluateContext(XPathContext parentContext, boolean force);

    /*
	boolean dispatchEvent(int ev);
    boolean dispatchEvent(Event ev, String nodeId);
*/
    // Keep eventing out of the interface for now
    
    XFormsNode getParentNode(Class parentClass);
	


}