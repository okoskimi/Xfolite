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
import javax.microedition.lcdui.Graphics;

import com.nokia.xfolite.client.PolishWidgetFactory;
import com.nokia.xfolite.xforms.dom.XFormsElement;
import com.nokia.xfolite.xml.dom.events.DOMEvent;

import de.enough.polish.util.*;

public class XF_TableItem extends TableItem {

    public XF_TableItem() {
        super(new TableData());
    }

    public XF_TableItem(Style arg0) {
        super(new TableData(), arg0);
    }
    
    public XF_TableItem(int arg0, int arg1) {
    	super(arg0, arg1);
    }

    public XF_TableItem(int arg0, int arg1, Style arg2) {
    	super(arg0, arg1, arg2);
    }
    
    
   /********************* BEGIN HACK ********************/
   int currentColumnIndex2 = -1;
   int currentRowIndex2 = -1;
    
    
	public void moveToNextColumn() {
		if (this.currentColumnIndex2 + 1 >= getNumberOfColumns()) {
			addColumn();
		}
		this.currentColumnIndex2++;
	}
	
	public void moveToNextRow() {
		if (this.currentRowIndex2 + 1 >= getNumberOfRows()) {
			addRow();
		}
		this.currentRowIndex2++;
		this.currentColumnIndex2 = -1;
	}
    
	public void add(Item item)
	{
		//#debug info
		System.out.println("Adding item at (" + this.currentColumnIndex2 + ", " + this.currentRowIndex2 + ")");
		add( this.currentColumnIndex2, this.currentRowIndex2, item );
	}
    
	/************************* END HACK *******************************/
	
	
    //#include ${dir.include}/XFormsItemImpl.java

}
