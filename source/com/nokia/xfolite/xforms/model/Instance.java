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

package com.nokia.xfolite.xforms.model;
import com.nokia.xfolite.xml.dom.Document;


public class Instance {

	private Document doc;
	private String instanceId;
	private XFormsModel m_model;
	
	public Instance(Document doc, String instanceId, XFormsModel model) {
		this.doc = doc;
		this.instanceId = instanceId;
		this.m_model=model;
	}
	
	public Document getDocument() {
		return doc;
	}
	
	public void setDocument(Document doc) {
		this.doc = doc;
	}
	
	public String getID() {
		return instanceId;
	}
	
	public XFormsModel getModel()
	{
		return m_model;
	}
}
