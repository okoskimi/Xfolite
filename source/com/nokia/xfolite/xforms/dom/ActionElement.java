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

import com.nokia.xfolite.xforms.model.XFormsModel;
import com.nokia.xfolite.xml.dom.*;
import com.nokia.xfolite.xml.dom.events.*;

public class ActionElement extends XFormsElement implements DOMEventListener {

	ActionElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
        super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
	}
	
	protected short deferredDirty=0;
	public static final short REBUILD = 1 << 1;
	public static final short REWIRE = 1 << 2;
	public static final short REFRESH = 1 << 3;
	public static final short REVALIDATE = 1 << 4;
	public static final short RECALCULATE = 1 << 5;


	public void setLocalState(short stateID, boolean value) {
        if (value) {
        	deferredDirty |= stateID;
        } else {
        	deferredDirty&=~stateID;
        }
    }
	
    private boolean testLocal(int stateID) {
        return (deferredDirty&stateID)>0;
    }
    public boolean elementParsed() {
        super.elementParsed();
        String eventAttr = this.getAttributeNS(XFormsDocument.XMLEVENTS_NAMESPACE, "event");
        if (eventAttr != "") {
            int evType = DOMEvent.getTypeFromName(eventAttr);
            if (evType != DOMEvent.UNKNOWN) {
                // TODO: Allow use of capture phase. Leave it out now for efficiency.
                getParentNode().addEventListener(evType, this, false);
            }
        }
        
        return false;
    }

    public void handleEvent(DOMEvent evt) { // this is the OUTERMOST EVENT HANDLER!
    	this.deferredDirty=0;
    	this.handleEvent(evt,this);
        ModelElement mel = ((XFormsDocument)ownerDocument).getModelElement();
    	if (this.testLocal(REBUILD))
    	{
    		mel.rebuild();
    		this.setLocalState(RECALCULATE, false);
    	}
    	if (this.testLocal(RECALCULATE))
    	{
    		mel.recalculate();
    	}
    	if (this.testLocal(REWIRE))
    	{
    		this.notifyStructureChanged();
    		this.setLocalState(REFRESH, false); // is this correct-  refresh not needed after rewire?
    	}
    	if (this.testLocal(REFRESH))
    	{
            mel.refresh();
    	}
    }
    
    protected void notifyStructureChanged()
    {
    	//TODO
    	this.getModel()
    	       .instanceStructureChanged(XFormsModel.SUBMISSION, this.getModel().getDefaultInstance(), 
    	    		   this.getModel().getDefaultInstance().getDocument());

    }

    public void handleEvent(DOMEvent evt, ActionElement outermost) {
    	//#debug
    	System.out.println("Handling event " + DOMEvent.getNameFromType(evt.getType()) + " for " + this.getLocalName());
    	try
    	{
	        boolean iftrue=false;
	        boolean whiletrue=false;
	        Attr whilecondition = getAttributeNode("while");
	        Attr ifcondition = getAttributeNode("if");
	        if (ifcondition == null)  {
	        	iftrue=true; 
	        }
	        else {
	        	iftrue= (getValue(ifcondition.getValue()).asBoolean().booleanValue()); 
	        }
	        if (whilecondition==null) // NO WHILE ATTR HERE
	        {
	        	if (iftrue) {
	            	doHandleEvent(evt,outermost);
	        	}
	        }
	        else
	        {
	        	whiletrue=true;
	        	iftrue=false;
	        	int count=0;
	        	while(whiletrue&&count<500) // repeat until while evaluates false
	        	{
	        		count++;
	                if (ifcondition == null) { 
	                	iftrue=true; 
	                }
	                else {
	                	iftrue=getValue(ifcondition.getValue()).asBoolean().booleanValue(); 
	                }
	        		whiletrue = getValue(whilecondition.getValue()).asBoolean().booleanValue();
	        		if (iftrue&&whiletrue) {
	        			doHandleEvent(evt,outermost);
	        		}
	        	}
	        }
    	} catch (Throwable t )
    	{
    		logWarning(t.toString());
    	}
    }
    
    public void doHandleEvent(DOMEvent evt,ActionElement outermost) {
        Node child = getFirstChild();
        while (child != null) {
        	if (child instanceof ActionElement) {
                ((ActionElement)child).handleEvent(evt,outermost);
        	}
        	else if (child instanceof DOMEventListener) {
                ((DOMEventListener)child).handleEvent(evt);
            }
            child = child.getNextSibling();
        }
    }
    //TODO: Remove event listener when element is removed from tree (requires another type of notification!)
}
