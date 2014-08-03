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

import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.events.DOMEvent;

public class TimerElement extends XFormsElement {

	TimerThread timerThread=null;
    protected TimerElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
		super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
	}
    
    public boolean elementParsed() {
        boolean ret =  super.elementParsed();
        this.getModel().addTimer(this);
        return ret;
        
    }
    
    public void defaultAction(DOMEvent evt)
    {
    	super.defaultAction(evt);
    	if (evt.getType()==DOMEvent.XFORMS_TIMER_START)
    	{
    		this.startTimer();
    	}
    	else if (evt.getType()==DOMEvent.XFORMS_TIMER_STOP)
    	{
    		this.stopTimer();
    	}
    		
    }

	public  void stopTimer() {
		// TODO Auto-generated method stub
		if (timerThread!=null)
		{
			timerThread.stopTimer();
			timerThread=null;
		}
	}

	private void startTimer() {
		// TODO Auto-generated method stub
		if (timerThread==null||timerThread.isAlive()==false)
		{
			timerThread=new TimerThread(this);
			timerThread.start();
		}
	}

}

