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

package com.nokia.xfolite.client.util;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.io.StreamConnection;

import de.enough.polish.browser.ProtocolHandler;
import de.enough.polish.browser.protocols.HttpProtocolHandler;
import de.enough.polish.browser.protocols.ResourceProtocolHandler;
import de.enough.polish.util.HashMap;

public class ProtocolFactory {

	private static ProtocolFactory instance;
	private Hashtable handlers;
	private HashMap headers = new HashMap();

	private ProtocolFactory() {
		this.handlers = new Hashtable();
		this.handlers.put("resource", new ResourceProtocolHandler());
		this.handlers.put("http", new HttpProtocolHandler(headers));
	}

	public void addProtocolHandler(String protocol, ProtocolHandler ph) {
		this.handlers.put(protocol, ph);
	}
	
	public static ProtocolFactory getInstance() {
		if (instance == null) {
			instance = new ProtocolFactory();
		}
		return instance;
	}

	public ProtocolHandler getProtocolHandler(String url)
	throws IOException
	{
		int pos = url.indexOf(':');

		if (pos < 0)
		{
			throw new IOException("Malformed url: " + url);
		}

		String protocol = url.substring(0, pos);
		if (protocol.equals("http")) {
			CookieJar jar = CookieJar.getInstance();
			String cookie = jar.getCookie(url);
			if (cookie != null) {
				headers.put("Cookie", cookie);
			}
		}
		ProtocolHandler handler = (ProtocolHandler) this.handlers.get(protocol);

		if (handler == null) {
			throw new IOException("Protocol handler not found for " + protocol);
		}

		return handler;
	}

	public StreamConnection getConnection(String url)
		throws IOException
	{
		return getProtocolHandler(url).getConnection(url);
	}
}
