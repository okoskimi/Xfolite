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

package com.nokia.xfolite.xforms.dom;

import com.nokia.xfolite.xml.dom.Element;

import javax.microedition.midlet.MIDlet;

public interface UserInterface {
    public static final int LVL_STATUS = 1;
    public static final int LVL_WARN = 2;
    public static final int LVL_ERROR = 3;    

    void log(int lvl, String msg, Element el);

    public void callSerially(Runnable task);
    public void callParallel(Runnable task);
    public void close();
    public void showMessage(String msg);
    public void load(String url);
    public void setTitle(String title);
    
    public String getProperty(String name);
}
