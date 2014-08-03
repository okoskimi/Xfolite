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
import javax.microedition.lcdui.Canvas;

import com.nokia.xfolite.client.*;
import com.nokia.xfolite.xforms.dom.*;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;

/**
 * Visibility support.
 *
 */
public class VisibilityManager

{
    public static void moveFocusTo(Item item) {
        Screen screen = item.getScreen();
        /*
        VisibilityManager.defocus(screen);
        VisibilityManager.focus(item);
        */
    	UiAccess.setFocusedItem(screen, item);
    }

    public static void scrollTo(Item item) {
    	/*
        Screen screen = item.getScreen();
        VisibilityManager.defocus(screen);
        VisibilityManager.scrollToItem(item);
        */
    	UiAccess.scrollTo(item);
    }
    
    public static void scrollToItem(Item item) {
        Container parent = (Container) item.getParent();
        Container grandParent = (Container) parent.getParent();
        if (grandParent != null) { // parent is the Screen's container
        	scrollToItem(parent);
        }
        parent.scroll(0, item, true);
    }
    
    public static void focus(Item item) {
        Container parent = (Container) item.getParent();
        Container grandParent = (Container) parent.getParent();
        if (grandParent == null) { // parent is the Screen's container
            item.getScreen().focus(item);
        } else {
            focus(parent);
            parent.focusChild(parent.indexOf(item), item, Canvas.DOWN, true);
        }
    }
    
    public static void defocus(Screen screen) {
        Item item = screen.getCurrentItem();
        screen.focus(-1);
        while (item instanceof Container && item != null) {
            Container c = (Container)item;
            item = c.getFocusedItem();
            c.focusChild(-1);
        }
    }
    
    
    public static void setHint(Item item, String msg) {
        //#style hintticker
        Ticker ticker = new Ticker(msg);
        Screen s = item.getScreen();
        // Looks like error but works
        s.setTicker(ticker);
    }
}
