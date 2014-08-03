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

class Token {
	protected char TokenType;
	protected byte OpType;
	protected String  image;
	protected int Position;
	
	protected static final byte OP_NOP=0;
	
	// CACHABLE OPERATORS
	protected static final byte OP_OR=1;
	
	protected static final byte OP_AND=2;
	
	protected static final byte OP_EQ=3;
	protected static final byte OP_NEQ=4;
	
	protected static final byte OP_GT=5;
	protected static final byte OP_GTE=6;
	protected static final byte OP_LT=7;
	protected static final byte OP_LTE=8;
	
	protected static final byte OP_PLUS=9;
	protected static final byte OP_MINUS=10;

	protected static final byte OP_MULT=11;
	protected static final byte OP_DIV=12;
	protected static final byte OP_MOD=13;
	
	protected static final byte OP_UNION=14;
	
	protected static final byte OP_SELECT_ROOT=15;
	
	protected static final byte OP_VARIABLE_REFERENCE=16;
	protected static final byte OP_FUNCTION_CALL=17;
	
	
	//	 NON-CACHABLE OPERATORS (NEGATIVE)
	// Ordered node types
	protected static final byte OP_NT_COMMENT=-1;
	protected static final byte OP_NT_NODE=-2;
	protected static final byte OP_NT_PI=-3;
	protected static final byte OP_NT_TEXT=-4;
	
	// Forward Axes
	protected static final byte OP_FORWARD_AXIS_ATTRIBUTE =-5;
	protected static final byte OP_FORWARD_AXIS_CHILD =-6;
	protected static final byte OP_FORWARD_AXIS_DESCENDENT =-7;
	protected static final byte OP_FORWARD_AXIS_DESCENDENT_OR_SELF =-8;
	protected static final byte OP_FORWARD_AXIS_FOLLOWING =-9;
	protected static final byte OP_FORWARD_AXIS_FOLLOWING_SIBLING=-10; 
	protected static final byte OP_FORWARD_AXIS_NAMESPACE=-11;
	protected static final byte OP_FORWARD_AXIS_SELF =-12;
	
	// Reverse Axes 
	protected static final byte OP_REVERSE_AXIS_ANCESTOR=-13;
	protected static final byte OP_REVERSE_AXIS_ANCESTOR_OR_SELF=-14;
	protected static final byte OP_REVERSE_AXIS_PARENT=-15; 
	protected static final byte OP_REVERSE_AXIS_PRECEDING=-16;
	protected static final byte OP_REVERSE_AXIS_PRECEDING_SIBLING=-17;
	
	//predicate
	protected static final byte OP_PREDICATE=-18;
	protected static final byte OP_REVERSE_PREDICATE=-19;

	//only from 1 till 32 available
	
	protected static final char TOK_EOF=0;
	protected static final char TOK_LITERAL=1;
	protected static final char TOK_ADDITIVEOP=2;
	protected static final char TOK_MULTOP=3;
	protected static final char TOK_RELATIONALOP=4;
	protected static final char TOK_EQUALITYOP=5;
	protected static final char TOK_NODETYPE=6;
	protected static final char TOK_AND=7;
	protected static final char TOK_OR=8;
	protected static final char TOK_SLASHSLASH=9;
	protected static final char TOK_COLONCOLON=10;
	protected static final char TOK_DOTDOT=11;
	protected static final char TOK_QNAME=12;
	protected static final char TOK_FUNCTIONNAME=13;
	protected static final char TOK_NUMBER=14;
	protected static final char TOK_AXIS_NAME=15;
	
	static protected boolean IsOperatorToken(char tok)
	{
		return tok == TOK_AND
			|| tok == TOK_OR
			|| tok == TOK_MULTOP
			|| tok == '/'
			|| tok == TOK_SLASHSLASH
			|| tok == '|'
			|| tok == TOK_ADDITIVEOP
			|| tok == TOK_EQUALITYOP
			|| tok == TOK_RELATIONALOP;
	}
	
	static protected boolean IsReverseAxis(byte axis)
	{
		return axis ==OP_REVERSE_AXIS_ANCESTOR
			|| axis == OP_REVERSE_AXIS_ANCESTOR_OR_SELF
			|| axis == OP_REVERSE_AXIS_PARENT 
			|| axis == OP_REVERSE_AXIS_PRECEDING
			|| axis == OP_REVERSE_AXIS_PRECEDING_SIBLING;
	}
	
	static protected boolean IsAxis(byte axis)
	{
		return axis >=OP_FORWARD_AXIS_ATTRIBUTE
			&& axis <= OP_REVERSE_AXIS_PRECEDING_SIBLING;
	}
	
}
