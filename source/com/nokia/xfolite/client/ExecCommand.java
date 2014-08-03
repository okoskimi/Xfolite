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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import de.enough.polish.ui.Item;

public abstract class ExecCommand extends Command {

    protected int m_priority;
    
    public ExecCommand(String label, int commandType, int priority) {
        super(label, commandType, priority);
        m_priority = priority;
    }

    public int getPriority() {
        return m_priority;
    }
    
    public void setPriority(int priority) {
        m_priority = priority;
    }
    
    public abstract void execute(Item item, Displayable disp);
    public void execute(Item item) {
        execute(item, null);
    }
    public void execute(Displayable disp) {
        execute(null, disp);
    }
}
