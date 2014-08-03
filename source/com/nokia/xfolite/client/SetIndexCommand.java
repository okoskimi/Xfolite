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


import de.enough.polish.ui.ChoiceItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.TableItem;
import de.enough.polish.ui.UiAccess;

import com.nokia.xfolite.client.ui.*;
import com.nokia.xfolite.xforms.dom.*;
import com.nokia.xfolite.xml.dom.*;
import com.nokia.xfolite.xml.dom.events.*;

import javax.microedition.lcdui.Command;

public class SetIndexCommand extends ExecCommand {

	RepeatItemElement repeatItem;
    
    public SetIndexCommand(String label, int commandType, int priority, RepeatItemElement repeatItem) {
        super(label, commandType, priority);
        this.repeatItem = repeatItem;
    }

    public void execute(Item item, Displayable disp) {
		//#debug info
		System.out.println("Got command event from tableitem! (" /* + item.getClass().getName() */ + ")");
		this.repeatItem.setAsSelected();
		/*		
		TableItem table = (TableItem) UiAccess.cast(item);
		Object selectedCell = table.getSelectedCell();
		if (selectedCell instanceof ChoiceItem) {
			Object repeatItem = ((ChoiceItem)selectedCell).getAttribute(PolishWidgetFactory.ELEMENT_ATTR);
			if (repeatItem instanceof RepeatItemElement) {
				//#debug info
				System.out.println("Selecting the repeatItem");
				((RepeatItemElement)repeatItem).setAsSelected();
			}
		} else if (selectedCell instanceof XFormsGroup) {
			if (((XFormsGroup)selectedCell).size() > 0) {
				Item child = ((XFormsGroup)selectedCell).get(0);
				if (child instanceof XF_Button) {
					Command cmd = ((XF_Button)child).getDefaultCommand();
					if (cmd instanceof ExecCommand) {
						((ExecCommand)cmd).execute(child);
					}
				}
			}
		}
		*/
    }
}
