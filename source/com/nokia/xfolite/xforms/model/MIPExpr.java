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

import com.nokia.xfolite.xml.xpath.XPathExpression;

public class MIPExpr {

    // TODO: is this the place to list the MIP types?
    // In C++ they were in xformsbase.h of model.
    
    // XFormsModel Item Property types
    public static final int RELEVANT = 1;
    public static final int READONLY = 2;
    public static final int REQUIRED = 3;
    public static final int CONSTRAINT = 4;
    public static final int SCHEMA_VALID = 5;
    public static final int CALCULATE = 6;
    public static final int TYPE = 7;
    public static final int P3PTYPE = 8;
    public static final int VALID = 9; // this combines EConstraint and ESchemaValid, for use in CInstanceItem.GetBooleanState()

	//	 DO NOT CHANGE, THE BOOLEAN ONES ARE INDEXES TO ARRAY
	static final int MAXBOOLEXPRID = 5;    
    
    public XPathExpression xpathExpr;
    public int type;
    
    public MIPExpr(XPathExpression xpathExpr, int id) {
        this.xpathExpr = xpathExpr;
        this.type = id;
    }   

}
