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

class LiteralNode extends ParseNode {
	
	private String m_str;
	
	protected LiteralNode(String value)
	{
		m_str = stripQuotes(value);
		m_isContextIndependent= true;
	}
	
	protected String LiteralValue()
	{
		return m_str;
	}
	
	private String stripQuotes(String s)
	{
		if(s==null || s.length()<2) return s;
		
		int start = 0;
		char c1 = s.charAt(start);
		if (c1=='\"'||c1=='\'')
			start++;
		
		int end = s.length();
		char c2 = s.charAt(end-1);
		if (c1==c2)
			end--;
		
		return s.substring(start,end);
		
	}
	
	protected byte Type()
	{
		return NODETYPE_LEAF_LITERAL;
	}
}
