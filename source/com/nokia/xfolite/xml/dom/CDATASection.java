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

import java.io.IOException;

import org.xmlpull.v1.IXmlSerializer;

public class CDATASection extends Text {

	protected CDATASection(Document ownerDocument, String data) {
		super(ownerDocument, data);
	}
	//	overloaded methods from the Node class
	public String getNodeName()
	{
		return "#cdata-section";
	}
	
	public byte getNodeType() {
		return CDATA_SECTION_NODE;
	}
	
	protected void write(IXmlSerializer writer, NodeFilter filter) throws IOException {
        if (filter == null || filter.acceptNode(this) == NodeFilter.ACCEPT) {
            writer.cdsect(data);
        }
	}
}
