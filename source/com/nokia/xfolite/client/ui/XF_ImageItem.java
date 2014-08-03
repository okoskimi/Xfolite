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
public class XF_ImageItem extends ImageItem {

    public XF_ImageItem(String arg0, Image arg1, int arg2, String arg3) {
        super(arg0, arg1, arg2, arg3);
    }

    public XF_ImageItem(String arg0, Image arg1, int arg2, String arg3,
            Style arg4) {
        super(arg0, arg1, arg2, arg3, arg4);
    }

    public XF_ImageItem(String arg0, Image arg1, int arg2, String arg3, int arg4) {
        super(arg0, arg1, arg2, arg3, arg4);
    }

    public XF_ImageItem(String arg0, Image arg1, int arg2, String arg3,
            int arg4, Style arg5) {
        super(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    //#include ${dir.include}/XFormsItemImpl.java

}
