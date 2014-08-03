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

package com.nokia.xfolite.xml.dom;

abstract public class NamedNode extends Node{
	
	protected String  namespaceURI;

	protected String  prefix;

	protected String  localName;


	protected NamedNode(Document ownerDocument, String aNamespaceURI,String aPrefix,String aLocalName)
	{
	    super(ownerDocument);
        if (aNamespaceURI!=null)
			namespaceURI = aNamespaceURI.intern();
		if (aPrefix!=null)
			prefix = aPrefix.intern();
		if (aLocalName!=null)
			localName = aLocalName.intern();
	}
	
	
	//overloaded methods of Node class
	public String getNodeName()
	{
		if (prefix==null || prefix.length()==0)
			return localName;
		else
			return prefix+":"+localName;
	}
	
	public String getNamespaceURI()
	{
		return namespaceURI;
	}

	public String getPrefix()
	{
		return prefix;
	}
	
	public String getLocalName()
	{
		return localName;
	}
}
