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

import com.nokia.xfolite.xml.dom.Node;

import java.util.Hashtable;
import java.util.Enumeration;

public class DOMEvent {
	public static final byte CAPTURING_PHASE           = 1;
    public static final byte AT_TARGET                 = 2;
    public static final byte BUBBLING_PHASE            = 3;

    public static final byte UNKNOWN = -1;
    
    public static final byte ANY = 0;
    
    public static final byte DOM_ACTIVATE = 1;
    public static final byte DOM_FOCUS_IN = 2;
    public static final byte DOM_FOCUS_OUT = 3;

    public static final byte XFORMS_MODEL_CONSTRUCT = 4;
    public static final byte XFORMS_MODEL_CONSTRUCT_DONE = 5;
    public static final byte XFORMS_READY = 6;
    public static final byte XFORMS_MODEL_DESTRUCT = 7;
    public static final byte XFORMS_REFRESH = 8;
    public static final byte XFORMS_RECALCULATE = 9;
    public static final byte XFORMS_REBUILD = 10;
    public static final byte XFORMS_REVALIDATE = 11;
    public static final byte XFORMS_SUBMIT = 12;
    public static final byte XFORMS_SUBMIT_DONE = 13;
    public static final byte XFORMS_SUBMIT_ERROR = 14;
    public static final byte XFORMS_VALUE_CHANGED = 15;
    public static final byte XFORMS_VALID = 16;
    public static final byte XFORMS_INVALID = 17;
    public static final byte XFORMS_READONLY = 18;
    public static final byte XFORMS_READWRITE = 19;
    public static final byte XFORMS_REQUIRED = 20;
    public static final byte XFORMS_OPTIONAL = 21;
    public static final byte XFORMS_ENABLED = 22;
    public static final byte XFORMS_DISABLED = 23;
    public static final byte XFORMS_IN_RANGE = 24;
    public static final byte XFORMS_OUT_OF_RANGE = 25;
    public static final byte XFORMS_PREVIOUS = 26;
    public static final byte XFORMS_NEXT = 27;
    public static final byte XFORMS_FOCUS = 28;
    public static final byte XFORMS_HELP = 29;
    public static final byte XFORMS_HINT = 30;
    public static final byte XFORMS_CLOSE = 31;    
    public static final byte XFORMS_RESET = 32; 
    public static final byte XFORMS_SELECT = 33;
    public static final byte XFORMS_DESELECT = 34;
    
    // Extensions
    public static final byte XFORMS_REEVALUATE = 35;
    public static final byte XFORMS_REBUILD_CONTROL = 36;
    public static final byte XFORMS_TYPE_CHANGED = 37;
    public static final byte XFORMS_SENSOR_READ = 38;
    public static final byte XFORMS_STRUCTURE_CHANGED = 39;
    public static final byte XFORMS_INSERT_ITEM = 40;
    public static final byte XFORMS_DELETE_ITEM = 41;
    

    // datasource events
    public static final byte DS_DATA_AVAILABLE = 42;
    public static final byte DS_GETDATA_AVAILABLE = 43;
    public static final byte DS_GETTIMEOUT = 44;

    public static final byte DS_PAUSE = 45;
    public static final byte DS_START = 46;
    public static final byte DS_GET = 47;

    
    public static final byte XFORMS_TIMER_START = 48;
    public static final byte XFORMS_TIMER_STOP = 49;
    public static final byte XFORMS_TIMER_TICK = 50;

    // Nokia extension to note that the submission is already ongoing
    public static final byte XFORMS_SUBMIT_ERROR_ONGOING = 51;

    // Rewiring extension event
    public static final byte XFORMS_BINDING_CHANGED = 52;

    // Lazy initialization event
    public static final byte XFORMS_INITIALIZED = 53;
    
    public static final byte LAST_EVENT = XFORMS_INITIALIZED;
    public static final byte[] properties = new byte[LAST_EVENT + 1];

    public static final byte BUBBLES = 1;
    public static final byte CANCELABLE = 2;
    
    private static Hashtable eventNames = new Hashtable();
    
    static {
        properties[DOM_ACTIVATE] = CANCELABLE | BUBBLES;
        properties[DOM_FOCUS_IN] = BUBBLES;
        properties[DOM_FOCUS_OUT] = BUBBLES;
        properties[XFORMS_MODEL_CONSTRUCT] = BUBBLES;
        properties[XFORMS_MODEL_CONSTRUCT_DONE] = BUBBLES;
        properties[XFORMS_READY] = BUBBLES;
        properties[XFORMS_MODEL_DESTRUCT] = BUBBLES;
        properties[XFORMS_REFRESH] = CANCELABLE | BUBBLES;
        properties[XFORMS_RECALCULATE] = CANCELABLE |  BUBBLES;
        properties[XFORMS_REBUILD] = CANCELABLE | BUBBLES;
        properties[XFORMS_REVALIDATE] = CANCELABLE | BUBBLES;
        properties[XFORMS_SUBMIT] = CANCELABLE | BUBBLES;
        properties[XFORMS_SUBMIT_DONE] = BUBBLES;
        properties[XFORMS_SUBMIT_ERROR] = BUBBLES;
        properties[XFORMS_VALUE_CHANGED] = BUBBLES;
        properties[XFORMS_VALID] = BUBBLES;
        properties[XFORMS_INVALID] = BUBBLES;
        properties[XFORMS_READONLY] = BUBBLES;
        properties[XFORMS_READWRITE] = BUBBLES;
        properties[XFORMS_REQUIRED] = BUBBLES;
        properties[XFORMS_OPTIONAL] = BUBBLES;
        properties[XFORMS_ENABLED] = BUBBLES;
        properties[XFORMS_DISABLED] = BUBBLES;
        properties[XFORMS_IN_RANGE] = BUBBLES;
        properties[XFORMS_OUT_OF_RANGE] = BUBBLES;
        properties[XFORMS_PREVIOUS] = CANCELABLE;
        properties[XFORMS_NEXT] = CANCELABLE;
        properties[XFORMS_FOCUS] = CANCELABLE;
        properties[XFORMS_HELP] = CANCELABLE | BUBBLES;
        properties[XFORMS_HINT] = CANCELABLE | BUBBLES;
        properties[XFORMS_CLOSE] = CANCELABLE | BUBBLES;
        properties[XFORMS_RESET] = CANCELABLE | BUBBLES;
        properties[XFORMS_SELECT] = CANCELABLE | BUBBLES;
        properties[XFORMS_DESELECT] = CANCELABLE | BUBBLES;
        
        properties[XFORMS_REEVALUATE] = BUBBLES;
        properties[XFORMS_REBUILD_CONTROL] = 0;
        properties[XFORMS_TYPE_CHANGED] = BUBBLES;
        properties[XFORMS_STRUCTURE_CHANGED] = 0;
        properties[XFORMS_INSERT_ITEM] = 0;
        properties[XFORMS_DELETE_ITEM] = 0;
        properties[XFORMS_SENSOR_READ] = 0;

        properties[DS_DATA_AVAILABLE] = BUBBLES;
        properties[DS_GETDATA_AVAILABLE] = BUBBLES;
        properties[DS_GETTIMEOUT] = BUBBLES;

        properties[DS_PAUSE] = CANCELABLE | BUBBLES;
        properties[DS_START] = CANCELABLE | BUBBLES;
        properties[DS_GET] = CANCELABLE | BUBBLES;
        properties[XFORMS_TIMER_START] = CANCELABLE | BUBBLES;
        properties[XFORMS_TIMER_STOP] = CANCELABLE | BUBBLES;
        properties[XFORMS_TIMER_TICK] = CANCELABLE | BUBBLES;
        properties[XFORMS_BINDING_CHANGED] = BUBBLES;
        
        properties[XFORMS_INITIALIZED] = 0;
        
        eventNames.put("Any", new Integer(ANY));
        eventNames.put("DOMActivate", new Integer(DOM_ACTIVATE));
        eventNames.put("DOMFocusIn", new Integer(DOM_FOCUS_IN));
        eventNames.put("DOMFocusOut", new Integer(DOM_FOCUS_OUT));
        eventNames.put("xforms-model-construct", new Integer(XFORMS_MODEL_CONSTRUCT));
        eventNames.put("xforms-model-construct-done", new Integer(XFORMS_MODEL_CONSTRUCT_DONE));
        eventNames.put("xforms-ready", new Integer(XFORMS_READY));
        eventNames.put("xforms-model-destruct", new Integer(XFORMS_MODEL_DESTRUCT));
        eventNames.put("xforms-refresh", new Integer(XFORMS_REFRESH));
        eventNames.put("xforms-recalculate", new Integer(XFORMS_RECALCULATE));
        eventNames.put("xforms-rebuild", new Integer(XFORMS_REBUILD));
        eventNames.put("xforms-revalidate", new Integer(XFORMS_REVALIDATE));
        eventNames.put("xforms-submit", new Integer(XFORMS_SUBMIT));
        eventNames.put("xforms-submit-done", new Integer(XFORMS_SUBMIT_DONE));
        eventNames.put("xforms-submit-error", new Integer(XFORMS_SUBMIT_ERROR));
        eventNames.put("xforms-submit-ongoing", new Integer(XFORMS_SUBMIT_ERROR_ONGOING));
        eventNames.put("xforms-value-changed", new Integer(XFORMS_VALUE_CHANGED));
        eventNames.put("xforms-valid", new Integer(XFORMS_VALID));
        eventNames.put("xforms-invalid", new Integer(XFORMS_INVALID));
        eventNames.put("xforms-readonly", new Integer(XFORMS_READONLY));
        eventNames.put("xforms-readwrite", new Integer(XFORMS_READWRITE));
        eventNames.put("xforms-required", new Integer(XFORMS_REQUIRED));
        eventNames.put("xforms-optional", new Integer(XFORMS_OPTIONAL));
        eventNames.put("xforms-enabled", new Integer(XFORMS_ENABLED));
        eventNames.put("xforms-disabled", new Integer(XFORMS_DISABLED));
        eventNames.put("xforms-in-range", new Integer(XFORMS_IN_RANGE));
        eventNames.put("xforms-out-of-range", new Integer(XFORMS_OUT_OF_RANGE));
        eventNames.put("xforms-previous", new Integer(XFORMS_PREVIOUS));
        eventNames.put("xforms-next", new Integer(XFORMS_NEXT));
        eventNames.put("xforms-focus", new Integer(XFORMS_FOCUS));
        eventNames.put("xforms-help", new Integer(XFORMS_HELP));
        eventNames.put("xforms-hint", new Integer(XFORMS_HINT));
        eventNames.put("xforms-close", new Integer(XFORMS_CLOSE));
        eventNames.put("xforms-reset", new Integer(XFORMS_RESET));
        eventNames.put("xforms-select", new Integer(XFORMS_SELECT));
        eventNames.put("xforms-deselect", new Integer(XFORMS_DESELECT));
        eventNames.put("xforms-reevaluate", new Integer(XFORMS_REEVALUATE));
        eventNames.put("xforms-rebuild-control", new Integer(XFORMS_REBUILD_CONTROL));
        eventNames.put("xforms-type-changed", new Integer(XFORMS_TYPE_CHANGED));
        eventNames.put("xforms-sensor-read", new Integer(XFORMS_SENSOR_READ));
        eventNames.put("xforms-structure-changed", new Integer(XFORMS_STRUCTURE_CHANGED));

        eventNames.put("ds-data-available", new Integer(DS_DATA_AVAILABLE));
        eventNames.put("ds-getdata-available", new Integer(DS_GETDATA_AVAILABLE));
        eventNames.put("ds-gettimeout", new Integer(DS_GETTIMEOUT));

        eventNames.put("ds-pause", new Integer(DS_PAUSE));
        eventNames.put("ds-start", new Integer(DS_START));
        eventNames.put("ds-get", new Integer(DS_GET));

        eventNames.put("xforms-timer-start", new Integer(XFORMS_TIMER_START));
        eventNames.put("xforms-timer-stop", new Integer(XFORMS_TIMER_STOP));
        eventNames.put("xforms-timer-tick", new Integer(XFORMS_TIMER_TICK));

        eventNames.put("xforms-binding-changed", new Integer(XFORMS_BINDING_CHANGED));
        
        eventNames.put("xforms-initialized", new Integer(XFORMS_INITIALIZED));
    
    }
    
    protected int type;
    protected Node target;
    protected Node currentTarget;
    protected short eventPhase;
    protected boolean bubbles;
    protected boolean cancelable;
    protected long timeStamp;
    
    protected boolean stopPropagation=false;
    protected boolean preventDefault=false;

    
    public DOMEvent(int typeArg)
    {
    	type = typeArg;
        
        if (type > ANY && type <= LAST_EVENT) {
            cancelable = (properties[type] & CANCELABLE) > 0 ;
            bubbles = (properties[type] & BUBBLES) > 0;
        } else {
            cancelable = false;
            bubbles = true;
        }
    	timeStamp = System.currentTimeMillis();
    }

    /**
     * Convert event name (string) to event type (integer)
     * @param eventName event name as string
     * @return event type as integer, or Event.UNKNOWN if string was not recognized
     */
    
    public static int getTypeFromName(String eventName) {
        Integer type = (Integer) eventNames.get(eventName);
        if (type == null) {
            return UNKNOWN;
        }
        return type.intValue();
    }

    /**
     * Convert event type (integer) to event name (string).
     * This is vastly inefficient, and should only be used for temporary logging purposes.
     * @param eventType event type as int
     * @return event type as String, or null if type was not recognized
     */
    
    public static String getNameFromType(int eventType) {
        Enumeration iter = eventNames.keys();
        while(iter.hasMoreElements()) {
            Object key = iter.nextElement();
            Integer value = (Integer) eventNames.get(key);
            if (value.intValue() == eventType) {
                return (String) key;
            }
        }
        return null;
    }
    
    
    public DOMEvent(int typeArg, Node target)
    {
        this(typeArg);
        this.target = target;
    }
    
    public int getType()
    {
    	return type;
    }

    public Node getTarget()
    {
    	return target;
    }

    public Node getCurrentTarget()
    {
    	return currentTarget;
    }

    public short getEventPhase()
    {
    	return eventPhase;
    }

    public boolean getBubbles()
    {
    	return bubbles;
    }

    public boolean getCancelable()
    {
    	return cancelable;
    }

    public long getTimeStamp()
    {
    	return timeStamp;
    }

    public void stopPropagation()
    {
    	stopPropagation = true;
    }

    public void preventDefault()
    {
    	preventDefault = true;
    }
    
    public boolean isDefaultPrevented()
    {
    	return preventDefault && cancelable;
    }
    
    
    public boolean dispatchEvent(Node n, boolean local)
    {
    	stopPropagation=false;
        preventDefault=false;
        target = n;
/*
        System.err.println("=== EVENT: " + getNameFromType(type) +
                " at <" + n.getLocalName() + ">");
*/                
        
    	Node parentNode = n.getParentNode();
    	if (parentNode!=null && !local)
    	{
    		eventPhase = CAPTURING_PHASE;
        	capturingPhase(parentNode);
    	}
    		
    	if (!stopPropagation)
		{
    		eventPhase = AT_TARGET;
            currentTarget = n;
    		n.notifyAllEventListeners(this);
		}
		
		if (parentNode!=null && bubbles && !stopPropagation && !local)
		{
			eventPhase = BUBBLING_PHASE;
			bubblingPhase(parentNode);
		}
		
		boolean execute_default = !(cancelable&&preventDefault);
		
		if (execute_default) {
			n.defaultAction(this);
        }
		cleanup();
		return execute_default;
    }
    
    protected void cleanup()
    {
    	this.currentTarget=null;
    	this.target=null;
    }
    
    protected void capturingPhase(Node n)
    {
	    Node parentNode = n.getParentNode();
    	if (parentNode!=null) {
	    	capturingPhase(parentNode);
        }
	    if (!stopPropagation) {
            currentTarget = n;
	    	n.notifyCapturingEventListeners(this);
        }
    }
    
    protected void bubblingPhase(Node n)
    {
        currentTarget = n;
    	n.notifyNonCapturingEventListeners(this);
    	if (!stopPropagation)
    	{
    		Node parentNode = n.getParentNode();
        	if (parentNode!=null) {
        		bubblingPhase(parentNode);
            }
    	}        	
    }

}
