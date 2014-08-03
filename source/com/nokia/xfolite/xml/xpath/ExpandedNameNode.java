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

package com.nokia.xfolite.xml.xpath;

public class ExpandedNameNode extends ParseNode{

	protected String m_namespace_URI;
    protected String m_localpart;

    protected ExpandedNameNode(String namespace_URI,String localpart)
	{
		if (namespace_URI!=null)
			m_namespace_URI = namespace_URI.intern();
		if (localpart!=null)
			m_localpart = localpart.intern();
		m_isContextIndependent= true;
	}
	
    public  String getLocalName()
	{
		return m_localpart;
	}

    public  String getNamespaceURI()
	{
		return m_namespace_URI;
	}
    
    protected byte Type()
	{
		return NODETYPE_LEAF_QNAME;
	}
	
}
