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


class ParseNode {
	
	protected static final byte NODETYPE_GENERAL=0;
	protected static final byte NODETYPE_LEAF_LITERAL=1;
	protected static final byte NODETYPE_LEAF_NUMBER=2;
	protected static final byte NODETYPE_LEAF_QNAME=3;
	
	protected byte m_Op;
	protected ParseNode[] m_children;
	protected ParseNode m_sibling;
	
	protected boolean m_isContextIndependent;
	
	protected boolean isCachable()
	{
		return m_isContextIndependent && m_Op>0;
	}
	
	
	protected void setChildren(ParseNode[] children)
	{
		m_children = children;
		
		// inherit caching from children
		if(m_isContextIndependent)
		{
			for (int i = 0; i < children.length; i++) {
				if (!Child(i).m_isContextIndependent)
				{
					m_isContextIndependent = false;
					return;
				}
			}
		}
	}
	
	protected void addChild(ParseNode n)
	{
		int i = getChildCount();
		
		ParseNode c[] = new ParseNode[i + 1];
		if (m_children != null)
		{
			System.arraycopy(m_children, 0, c, 0, m_children.length);
		}
		m_children = c;
		
		m_children[i] = n;
		
		// inherit caching from children
		if(m_isContextIndependent && !n.m_isContextIndependent)
			m_isContextIndependent = false;
	}
	
	protected ParseNode Child(int i) {
		  return m_children[i];
		}
	
	protected int getChildCount() {
	    return (m_children == null) ? 0 : m_children.length;
	  }
	
	protected ParseNode()
	{	
	}
	
	// constructor for binary operators
	protected ParseNode(byte Op,ParseNode left,ParseNode right)
	{
		m_Op=Op;
		m_children = new ParseNode[2];
		m_children[0] = left;
		m_children[1] = right;
		m_isContextIndependent = Op>=0 && left.m_isContextIndependent && right.m_isContextIndependent?true:false;
	}
	
	// constructor for unary operator
	protected ParseNode(byte Op,ParseNode operand)
	{
		m_Op=Op;
		m_children = new ParseNode[1];
		m_children[0] = operand;
		m_isContextIndependent = Op>=0 && operand.m_isContextIndependent?true:false;
	}
	
	// constructor for operators without operand
	protected ParseNode(byte Op)
	{
		m_Op = Op;
		m_isContextIndependent = Op>=0?true:false;
	}
	
	protected byte Type()
	{
		return NODETYPE_GENERAL;
	}
	
	protected boolean isLeaf(){
		return (m_children==null);
	}
	
	protected ParseNode Sibling(){
		return m_sibling;
	}
	
	protected ParseNode LastInSiblingChain()
	{
		if (m_sibling==null)
			return this;
		else 
			return m_sibling.LastInSiblingChain();
	}
	
	protected void SetSibling(ParseNode sibling)
	{
		m_sibling = sibling;
	}
	
	protected byte Op()
	{
		return m_Op;
	}
}


