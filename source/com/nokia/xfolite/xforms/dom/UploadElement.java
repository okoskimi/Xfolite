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
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.events.DOMEvent;
import com.nokia.xfolite.xml.xpath.XPathContext;
import com.nokia.xfolite.xml.xpath.XPathResult;

public class UploadElement extends SingleBoundElement {

	UploadElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
        super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
	}
    
    public void defaultAction(DOMEvent evt) {
        if (evt.getType() == DOMEvent.DOM_ACTIVATE) {
        }
    }
    
    public String getAllowedMediatypes()
    {
    	return this.getAttribute("mediatype");
    }
    
    public void setFilename(String filename)
    {
    	Element fileElem = this.getChildElement("filename");
    	if (fileElem!=null)
    	{
    		String chRef = fileElem.getAttribute("ref");
    		this.setNodeValue(chRef,filename!=null?filename:"");
    	}
    }

    public void setMimetype(String mtype)
    {
    	Element mimeElem = this.getChildElement("mediatype");
    	if (mimeElem!=null)
    	{
    		String chRef = mimeElem.getAttribute("ref");
    		this.setNodeValue(chRef,mtype!=null?mtype:"");
    	}
    }
    
    
    
    
    
    private void setNodeValue(String xpath,String value)
    {
		XPathContext ctxt = this.getContext();
		XPathResult result = this.getModel().getXPathEvaluator().evaluate(xpath, ctxt, ctxt.contextNode, XPathResult.NODE);
		Node resNode = result.asNode();
		if (resNode!=null)
			resNode.setText(value);
    }
    
    private Element getChildElement(String lname)
    {
    	for (int i=0;i<this.getChildCount();i++)
    	{
    		Node child = this.getChild(i);
    		if (child!=null)
    		{
    			if (child.getNodeType()==Node.ELEMENT_NODE)
    			{
    				Element chElem=(Element)child;
    				if (lname.equals(chElem.getLocalName())) return chElem;
    			}
    		}
    	}
    	return null;
    }

}
