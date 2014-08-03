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

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
import de.enough.polish.ui.*;

import com.nokia.xfolite.client.PolishWidgetFactory;
import com.nokia.xfolite.xforms.dom.XFormsElement;
import com.nokia.xfolite.xml.dom.events.DOMEvent;
public class XF_TabItem extends LabeledTabItem implements SelectItem {

    public XF_TabItem(String label, String[] tabNames, Image[] tabImages, int type) {
        super(label, tabNames, tabImages, type);
    }

    public XF_TabItem(String label, String[] tabNames, Image[] tabImages, int type, Style style) {
        super(label, tabNames, tabImages, type, style);
    }

    //#include ${dir.include}/XFormsItemImpl.java

    public int getSelectedFlags(boolean[] selectedArray_return) {
        for(int i=0; i < selectedArray_return.length; i++) {
            selectedArray_return[i] = false;
        }
        selectedArray_return[getSelectedIndex()] = true;
        return 1;
    }
    public int getSelectedIndex() {
        return getActiveTab();
    }
    
    public boolean isSelected(int index) {
        return index == getActiveTab();
    }
    
    public void setSelectedIndex(int index, boolean selected) {
        if (selected) {
            setActiveTab(index);
        }
    }
    // Note: one tab must always be active. If array has only false values this does nothing.
    public void setSelectedFlags(boolean[] selectedArray) {
    	int len = this.size();
    	for (int i=0; i<selectedArray.length && i < len; i++) {
    		if (selectedArray[i]) {
    			setActiveTab(i);
    			break;
    		}
    	}
    }
}
