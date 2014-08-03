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

import com.nokia.xfolite.xml.dom.Node;


/**
 * A class that contains the result of evaluating an XPath expression,
 * and knows its type.
 * 
 * @author mattisil
 *
 */
public class XPathResult {
	public static final byte ANY = 0;
	public static final byte BOOLEAN = 1;
	public static final byte NODE = 2;
	public static final byte NODESET = 3;
	public static final byte NUMBER = 4;
	public static final byte STRING = 5;
	
    public static final Object ANY_VALUE = new Object();
    
	private Object result;
	private byte type = -1;

	/**
	 * Construct the result based on an evaluated XPath expression.
	 * 
	 * @param result  the result of the XPath expression
	 */
	public XPathResult(Object result) {
		this.result = result;				
	}
	
	public byte getType() {
		if (type == -1) {
			this.type = resolveType(result);
		}
		return type;
	}

	public NodeSet asNodeSet() {
		if (getType() == NODESET) {
			return (NodeSet) result;
		}
		return new NodeSet();
	}

    public Node asNode() {
        if (getType() == NODE) {
            return (Node) result;
        } else if (getType() == NODESET) {
            NodeSet ns = (NodeSet) result;
            if (ns.getLength()>0) {
                return ns.item(0);
            }
        }
        return null;
    }
    
	public String asString() {
		if (getType() == STRING) {
			return (String) result;
		}
		return XPathCoreFunctionLibrary.string_function(result);
	}

	public Double asNumber() {
		if (getType() == NUMBER) {
			return (Double) result;
		}
		return XPathCoreFunctionLibrary.number_function(result);
	}

	public Boolean asBoolean() {
		if (getType() == BOOLEAN) {
			return (Boolean) result;
		}
        return XPathCoreFunctionLibrary.boolean_function(result);
	}

	void setResult(Object result) {
		this.result = result;
		this.type = -1;
	}
	
	private byte resolveType(Object result) {
		if (result instanceof NodeSet) {
			return NODESET;
		} else if (result instanceof String) {
			return STRING;
		} else if (result instanceof Boolean) {
			return BOOLEAN;
		} else if (result instanceof Double) {
			return NUMBER;
		} else if (result instanceof Node) {
			return NODE;
		} else if (result == ANY_VALUE) {
		    return ANY;
        }
        return -1;		
	}
	
}
