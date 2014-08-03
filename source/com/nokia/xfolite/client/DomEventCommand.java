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

package com.nokia.xfolite.client;

import javax.microedition.lcdui.Displayable;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.UiAccess;
import com.nokia.xfolite.xforms.dom.*;
import com.nokia.xfolite.xml.dom.*;
import com.nokia.xfolite.xml.dom.events.*;

public class DomEventCommand extends ExecCommand {

    protected XFormsElement m_element;
    protected int m_event;
    protected Item m_item;
    
    public DomEventCommand(String label, int commandType, int priority, XFormsElement el, int event) {
    	this(label, commandType, priority, el, event, null);
    }
    
    public DomEventCommand(String label, int commandType, int priority, XFormsElement el, int event, Item item) {
        super(label, commandType, priority);
        m_element = el;
        m_event = event;
        m_item = item;
    }

    public void execute(Item item, Displayable disp) {
    	// Need to check that it is actually the currently focused item which is invoking this command.
    	// For some reason, sometimes commands may be activated also by other items when user presses fire
    	// and item does not have any default command. This prevents accidental activation.
    	if (m_item == null || m_item.isFocused) {
    		//#debug
    		System.out.println("Delivering event " + m_event + " to " + m_element.getLocalName());
    		m_element.dispatchLocalEvent(m_event);
    	} else {
    		//#debug warn
    		System.out.println("Did not deliver event " + m_event + " to " + m_element.getLocalName()
    				+ ((m_item == null) ? ", item is null" : ", item " + m_item + " is not focused"));
    	}
    }
}
