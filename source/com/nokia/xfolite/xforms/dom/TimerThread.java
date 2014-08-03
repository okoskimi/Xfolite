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

/**
 * 
 */
package com.nokia.xfolite.xforms.dom;

import com.nokia.xfolite.xml.dom.Attr;
import com.nokia.xfolite.xml.dom.events.DOMEvent;

public class TimerThread extends Thread
{
	/**
	 * 
	 */
	private TimerElement element;
	/**
	 * @param element
	 */
	TimerThread(TimerElement element) {
		this.element = element;
	}
	boolean shouldRun = true;
	int freq=2000;
	public void run()
	{
		Attr time = this.element.getAttributeNode("time");
		if (time!=null)
		{
			try
			{
					freq=Integer.parseInt(time.getValue());
			} catch (Exception e)
			{
				this.element.logWarning(e);
			}
		}
		while (shouldRun)
		{
			try {
				Thread.sleep(freq);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				this.element.logWarning(e);
			}
			if (shouldRun)
			{
				this.element.logStatus("Timer tick.");
				this.element.dispatchEventSerially(DOMEvent.XFORMS_TIMER_TICK);
			}
		}
		this.element=null;
	}
	public void stopTimer()
	{
		this.shouldRun=false;
	}
}