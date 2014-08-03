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

import java.util.Vector;
import de.enough.polish.util.StringTokenizer;

class Cookie {
	String name;
	String value;
	String domain;
	String path;
}

public class CookieJar {

	private static CookieJar instance;
	private Vector cookies;

	private CookieJar() {
		this.cookies = new Vector();
	}

	public static CookieJar getInstance() {
		if (instance == null) {
			instance = new CookieJar();
		}
		return instance;
	}

	public void setCookie(String s) {
		//#debug info
		System.out.println("setCookie("+s+")");
		Cookie cookie = new Cookie();
		StringTokenizer tok = new StringTokenizer(s, ';');
		while(tok.hasMoreTokens()) {
		String nv = tok.nextToken();
			int eqIndex = nv.indexOf('=');
			if (eqIndex < 0) {
				continue; // Might be the "secure" marking, but we don't support that
			}
			String name = nv.substring(0, eqIndex).trim();
			String value = nv.substring(eqIndex + 1).trim();
			if (name.toLowerCase().equals("domain")) {
				cookie.domain = value.toLowerCase();
			} else if (name.toLowerCase().equals("path")) {
				cookie.path = value;
			} else if (!name.equals("expires") && !name.equals("secure")) {
				cookie.name = name;
				cookie.value = value;
			}
		}
		if (cookie.name != null && cookie.value != null) {
			//#debug info
			System.out.println("Created cookie " + cookie.name + "="+ cookie.value + "; domain=" + cookie.domain + "; path=" + cookie.path);
			final int len = this.cookies.size();
			for (int i=0; i < len; i++) {
				Cookie stored = (Cookie) this.cookies.elementAt(i);
				if (stored.name.equals(cookie.name)) {
					this.cookies.setElementAt(cookie, i);
					cookie = null;
					break;
				}
			}
			if (cookie != null) {
				this.cookies.addElement(cookie);
			}
		}
	}
	
	public String getCookie(String url) {
		//#debug info
		System.out.println("getCookie("+url+")");
		if (!url.toLowerCase().startsWith("http://"))
		{
			//#debug info
			System.out.println("Not http, return null");
			return null;
		}
		String host;
		String path;
		int hostStart = url.indexOf("//");
		hostStart+=2;
		int hostEnd = url.indexOf("/", hostStart);
		if (hostEnd != -1)
		{
			host = url.substring(hostStart, hostEnd).toLowerCase();
			path = url.substring(hostEnd);
		}
		else
		{
			host = url.substring(hostStart).toLowerCase();
			path = "/";
		}
		final int len = this.cookies.size();
		StringBuffer result = null;
		for (int i=0; i < len; i++) {
			Cookie cookie = (Cookie) this.cookies.elementAt(i);
			//#debug info
			System.out.println("Checking cookie " + cookie.name + "="+ cookie.value + "; domain=" + cookie.domain + "; path=" + cookie.path);

			if ((cookie.domain == null || host.endsWith(cookie.domain)) && (cookie.path == null || path.startsWith(cookie.path))) {
				if (result == null) {
					result = new StringBuffer();
					result.append(cookie.name).append("=").append(cookie.value);					
				} else {
					result.append("; ").append(cookie.name).append("=").append(cookie.value);
				}
				//#debug info
				System.out.println("     - Cookie matches.");

			}
		}
		if (result == null) {
			//#debug info
			System.out.println("No cookies found, return null");
			return null;
		} else {
			//#debug info
			System.out.println("Returning cookies: " + result.toString());
			return result.toString();
		}
	}
}
