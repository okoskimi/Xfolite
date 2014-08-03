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

package com.nokia.xfolite.xml.xpath;

public class SimpleXPathNSResolver extends java.util.Hashtable implements XPathNSResolver {

	 /**
     * Look up the namespace URI associated to the given namespace prefix. The 
     * XPath evaluator must never call this with a <code>null</code> or 
     * empty argument, because the result of doing this is undefined.Null / 
     * empty prefix passed to XPathNSResolver should return default 
     * namespace.Do not permit <code>null</code>to be passed in invocation, 
     * allowing the implementation, if shared, to do anything it wants with 
     * a passed <code>null</code>.It would be confusing to specify more than 
     * this since the resolution of namespaces for XPath expressions never 
     * requires the default namespace.Null returns are problematic.No change.
     * They should be adequately addressed in core. Some implementations 
     * have not properly supported them, but they will be fixed to be 
     * compliant. Bindings are still free to choose alternative 
     * representations of <code>null</code>where required.
     * 
     * This implementation is equivalent to <code>(String) get(prefix)</code>.
     * 
     * @param prefix The prefix to look for.
     * @return Returns the associated namespace URI or <code>null</code> if 
     *   none is found.
     */
    public String lookupNamespaceURI(String prefix) {
    	return (String) get(prefix);

    }
}
