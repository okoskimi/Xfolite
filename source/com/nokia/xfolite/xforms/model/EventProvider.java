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

import java.util.Hashtable;

import com.nokia.xfolite.xforms.dom.XFormsDocument;
import com.nokia.xfolite.xml.dom.Node;

public interface EventProvider {
    
    void initialize(String user, String name);
    void start(String user, String name, XFormsDocument doc, Node ref, int frequency, Hashtable extParams);
    void stop(String name);
    void get(String name, XFormsDocument doc, Node ref, Hashtable extParams);
}
