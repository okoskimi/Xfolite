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

package com.nokia.xfolite.client.ui;


import de.enough.polish.ui.*;



public class XFormsGroup extends Container {

	public static final int GROUP = 0;
	public static final int TABLE_DATA = 1;
	public static final int TABLE_HEADER = 2;
	
	int type;
	
    public XFormsGroup() {
        super(false);
        this.type = GROUP;
    }
    
    public XFormsGroup(Style style) {
        super(false, style);
        this.type = GROUP;
    }
    
    public XFormsGroup(int type) {
        super(false);
        this.type = type;
    }

    public XFormsGroup(int type, Style style) {
        super(false, style);
        this.type = type;
    }
    
    public int getType() {
    	return this.type;
    }
}
