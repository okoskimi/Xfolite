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

package com.nokia.xfolite.xml.dom.events;

import java.util.Vector;

import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;

public class EventAwareElement extends Element {
	
	protected Vector eventHandlers;

	protected EventAwareElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
		super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
		// TODO Auto-generated constructor stub
	}

	public void addEventListener(int type, 
            DOMEventListener listener, 
            boolean useCapture)
    {
    	if (eventHandlers == null)
    		eventHandlers = new Vector();
    	eventHandlers.addElement(new EventHandler(type,listener,useCapture));
    }

    public void removeEventListener(int type, 
               DOMEventListener listener, 
               boolean useCapture)
    {	
    	if (eventHandlers == null) return;
    	
    	int handlers_count = eventHandlers.size();
    	for (int i=0;i<handlers_count;i++)
    	{
    		EventHandler eh = (EventHandler) eventHandlers.elementAt(i);
    		if (eh.eventType == type
    				&& eh.listener == listener
    				&& eh.useCapture == useCapture)
    		{
    			eventHandlers.removeElementAt(i);
    			handlers_count--;
    			i--;
    		}
    	}
    }

    public void notifyCapturingEventListeners(DOMEvent evt)
    {
        if (eventHandlers == null) return;
        
    	int handlers_count = eventHandlers.size();
    	for (int i=0;i<handlers_count;i++)
    	{
    		EventHandler eh = (EventHandler) eventHandlers.elementAt(i);
    		if ((eh.eventType == DOMEvent.ANY || eh.eventType == evt.type)
    				&& eh.useCapture == true)
    		{
    			eh.listener.handleEvent(evt);
    		}
    	}
    }
    
    public void notifyNonCapturingEventListeners(DOMEvent evt)
    {
        if (eventHandlers == null) return;

        int handlers_count = eventHandlers.size();
    	for (int i=0;i<handlers_count;i++)
    	{
    		EventHandler eh = (EventHandler) eventHandlers.elementAt(i);
    		if ((eh.eventType == DOMEvent.ANY  || eh.eventType == evt.type)
    				&& eh.useCapture == false)
    		{
    			eh.listener.handleEvent(evt);
    		}
    	}
    }
    
    public void notifyAllEventListeners(DOMEvent evt)
    {
        if (eventHandlers == null) return;

        int handlers_count = eventHandlers.size();
    	for (int i=0;i<handlers_count;i++)
    	{
    		EventHandler eh = (EventHandler) eventHandlers.elementAt(i);
    		if (eh.eventType == DOMEvent.ANY || eh.eventType == evt.type)
    		{
    			eh.listener.handleEvent(evt);
    		}
    	}
    }
    
    private class EventHandler {
    	public int eventType; 
        public DOMEventListener listener; 
        public boolean useCapture;
        
        public EventHandler(int typeArg, 
                   DOMEventListener listenerArg, 
                   boolean useCaptureArg)
        {
        	eventType = typeArg;
        	listener = listenerArg;
        	useCapture = useCaptureArg;
        }

    }
}
