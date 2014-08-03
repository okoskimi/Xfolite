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

//#if nokia.perfTrace.enabled
//#define tmp.perf
//#endif


import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

import org.xmlpull.v1.IXmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.nokia.xfolite.xforms.dom.ModelElement;
import com.nokia.xfolite.xforms.dom.XFormsElement;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.events.DOMEvent;

public class XFormsModelUI {

    private XFormsModel model;
    
    private Vector dirtyVals = new Vector(); // UIBindings
	private Vector dirtyDeps = new Vector(); // UIBindings
	private Vector uiBindings = new Vector();

	// The below things are for refresh() and event dispatching.
	private boolean isDispatching = false;
	private boolean isRefreshing = false;
	private Vector staleBindings = new Vector();
	private Vector eventQueue = new Vector();	

	// all events go through the DOM event system
    //private StructureEventListener listener;
    
	public XFormsModelUI(String id, ModelElement element) {
	    model = new XFormsModel(id,element);
        model.addUI(this);
    }

    public XFormsModel getModel() {
		return model;
	}

	public void valueChanged(UIBinding binding) {
		addDirtyValue(binding);
	}

    /** 
     * Reports that a MIP status, such as valid has changed. 
     * 
     */
	public void statusChanged(int MIPtype, boolean newStatus, UIBinding binding) {
	    addDirtyValue(binding);		
	}

	public void dependencyChanged(UIBinding binding) {
		addDirtyDependency(binding);
	}

	public Vector getAndClearDirtyDependencies() {
		Vector oldList = dirtyDeps;
		dirtyDeps = new Vector();
		// TODO: should we return a UIBinding[]?
		return oldList;
	}
	
	/**
	 * This commentary from C++ code.
	 * 
	 * Call this after calling GetAndClearDirtyDependenciesL(); and reflecting that in the DOM.
	 * NOTE! IMPORTANT! You should first modify the DOM to reflect the latest state, and
	 * only then fire the DOM events. Otherwise the DOM events might cause re-entrant changes to
	 * the binding states, and cause wrong behaviour.
	 * You can do this by building a list of events that should be thrown, while setting the
	 * DOM state. And only after going through the list completely, start throwing the events.
	 */
	public Vector getAndClearDirtyValues() {
		Vector oldList = dirtyVals;
		dirtyVals = new Vector();
		return oldList;
	}

	public void clearDirtyDependencies()
	{
		dirtyDeps = new Vector();
	}
	public void clearDirtyValues()
	{
		dirtyVals = new Vector();
	}


    void resetAllUIBindings()
    {
        int uiBindingCount = uiBindings.size();
        for (int i=0;i<uiBindingCount;i++)
        {
            UIBinding binding = (UIBinding) uiBindings.elementAt(i);
            binding.reset();
        }
    }
    
	
	public void queueEvent(DOMEvent aEvent) 
	{
		eventQueue.addElement(aEvent);
	}

	// Currently not in use
/*	void removeEvents(XFormsElement target) 
	{
		int i=0;
		while(i<eventQueue.size()) 
		{
			if (((XFormsEvent)eventQueue.elementAt(i)).getTarget() == target) 
			{
				eventQueue.removeElementAt(i);
			}
			else 
			{
				i++;
			}
		}
	}*/
	
    // This is not currently used, it was used in C++ I guess mostly because of lack of garbage collection
    // Bindings might still be stale because UIBinding has been removed due to action processing
    // (e.g. in repeat re-evaluation), but the pointers will still be valid.
    // Could maybe increase efficiency by marking removed UIBindings as stale, so they don't get processed
    // anymore? OTOH just setting them to a "cleared" state would probably be simpler and more efficient...
	void markAsStale(UIBinding aBinding) 
	{
		if (isRefreshing) 
		{
			staleBindings.addElement(aBinding);
		}
	}

	boolean isAlive(UIBinding aBinding) 
	{
		return ! staleBindings.contains(aBinding);
	}
	
	void removeStaleBindings() 
	{
		staleBindings.removeAllElements();
	}

	
	void dispatchQueuedEvents() 
	{
		if (isDispatching) 
		{
			return;
		}
		isDispatching = true;
		// We must not cache size because event handling may cause new events
		// to be queued, i.e. size may grow during the loop.
		int nextEvent = 0;
		while (nextEvent < eventQueue.size()) 
		{
			DOMEvent ev = (DOMEvent) eventQueue.elementAt(nextEvent++);
	        //#if tmp.perf
			int type = ev.getType();
			Node target = ev.getTarget();
			System.out.println("Dispatching event " + DOMEvent.getNameFromType(type) + " to " + target.getLocalName());		
			long start = System.currentTimeMillis();
			//#endif
			ev.getTarget().dispatchEvent(ev);
	        //#if tmp.perf
	        long t = System.currentTimeMillis() - start;
			System.out.println("Event handling complete for " + DOMEvent.getNameFromType(type) + " to " + target.getLocalName());		
			System.out.println("The event handling took " + t + " ms");
			if (t > 100) {
				System.out.println("*********************** LONG HANDLING TIME ABOVE ***********************");
			}
			//#endif
		}
		eventQueue.removeAllElements();
		isDispatching = false;
	}	
	
	
	
	public void refresh()
	{
	    //#if tmp.perf
		System.out.println("Doing refresh...");
 		long start = System.currentTimeMillis();
        //#endif
		isRefreshing = true;
        Vector dirtyValues = getAndClearDirtyValues();
		Vector dirtyDependencies = getAndClearDirtyDependencies();
        
		// TODO: dirtyDependencies should be walked in tree walk order
		// (parents before children) to prevent unnecessary rewiring
		int count = dirtyDependencies.size();
	    //#if tmp.perf
		System.out.println("Re-evaluating dependencies...");
		//#endif
		for(int i = 0; i < count; i++) 
		{
			UIBinding uibinding = (UIBinding) dirtyDependencies.elementAt(i);
			
			if (!isAlive(uibinding)) 
			{
				continue;
			}
            XFormsElement xfEl = uibinding.getElement();
    	    //#if tmp.perf
    		System.out.println("Re-evaluating binding for " + xfEl.getLocalName());
    		long bstart = System.currentTimeMillis();
    		//#endif
    		if (uibinding.reEvaluateBinding()) 
			{
        	    //#if tmp.perf
            	long bt1 = System.currentTimeMillis() - bstart;
            	System.out.println("Re-evaluating binding took " + bt1 + " ms");
        		System.out.println("Re-evaluating child contexts");
        		//#endif
        		xfEl.reEvaluateChildContexts(false);
        	    //#if tmp.perf
        		long bt2 = System.currentTimeMillis() - bstart - bt1;
        		System.out.println("Re-evaluating child contexts took " + bt2 + " ms");
        		//#endif
			}
		}
        dirtyDependencies = null;

	    //#if tmp.perf
        long t1 = System.currentTimeMillis() - start;
		System.out.println("Re-evaluating dependencies took " + t1 + " ms");
		//#endif
        Vector dirtyRewirings = getAndClearDirtyValues();

        // Remove duplicate entries
        count = dirtyValues.size();
        for(int i = 0; i < count; i++) {
            dirtyRewirings.removeElement(dirtyValues.elementAt(i));
        }

        queueChanges(dirtyValues);
        queueRewirings(dirtyRewirings);

	    //#if tmp.perf
        long t2 = System.currentTimeMillis() - start - t1;
		System.out.println("Queuing events took " + t2 + " ms");
		//#endif
		
		dirtyValues = null;
        dirtyRewirings = null;

		isRefreshing = false;
		removeStaleBindings();

		
		// This continues until there are no more events.
		// It automatically exits immediately if re-entrant
		dispatchQueuedEvents();		

	    //#if tmp.perf
		long t3 = System.currentTimeMillis() - start - t1 - t2;
		System.out.println("Dispatching events took " + t3 + " ms");
		//#endif
	}
    
    public void queueChanges(Vector bindingList) {
        int count = bindingList.size();
        for(int i = 0; i < count; i++) 
        {
            UIBinding uibinding = (UIBinding) bindingList.elementAt(i);
            if (!isAlive(uibinding)) 
            {
                continue;
            }
            
            
            XFormsElement xfEl = uibinding.getElement();

            if (uibinding.getBindingStatus() == UIBinding.BOUND) 
            {
                if (uibinding.testDirty(UIBinding.RELEVANT_DIRTY)) 
                {
                    if (uibinding.getMIPAndClearDirty(UIBinding.RELEVANT_DIRTY)) 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_ENABLED, xfEl));
                    }
                    else 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_DISABLED, xfEl));
                    }

                }

                if (uibinding.testDirty(UIBinding.READONLY_DIRTY)) 
                {
                    if (uibinding.getMIPAndClearDirty(UIBinding.READONLY_DIRTY)) 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_READONLY, xfEl));
                    }
                    else 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_READWRITE, xfEl));
                    }

                }               
                
                
                if (uibinding.testDirty(UIBinding.REQUIRED_DIRTY)) 
                {
                    if (uibinding.getMIPAndClearDirty(UIBinding.REQUIRED_DIRTY)) 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_REQUIRED, xfEl));
                    }
                    else 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_OPTIONAL, xfEl));
                    }

                }               
                

                if (uibinding.testDirty(UIBinding.VALID_DIRTY)) 
                {
                    if (uibinding.getMIPAndClearDirty(UIBinding.VALID_DIRTY)) 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_VALID, xfEl));
                    }
                    else 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_INVALID, xfEl));
                    }

                }               

                if (uibinding.testDirty(UIBinding.TYPE_CHANGED_DIRTY))
                {
                    uibinding.setDirty(UIBinding.TYPE_CHANGED_DIRTY, false);
                    queueEvent(new DOMEvent(DOMEvent.XFORMS_TYPE_CHANGED, xfEl));
                }

                if (uibinding.testDirty(UIBinding.VALUE_CHANGED_DIRTY))
                {
                    uibinding.setDirty(UIBinding.VALUE_CHANGED_DIRTY, false);
                    queueEvent(new DOMEvent(DOMEvent.XFORMS_VALUE_CHANGED, xfEl));
                }
            }
            else 
            {
                // FIXME: Would maybe be better if model translated not bound to MIP changes
                // - now I don't know if it has changed or not, so I always send disabled event
                // OTOH I think spec says it should work that way anyhow...
                uibinding.setDirty(UIBinding.RELEVANT_DIRTY, false);
                uibinding.setDirty(UIBinding.READONLY_DIRTY, false);
                uibinding.setDirty(UIBinding.REQUIRED_DIRTY, false);
                uibinding.setDirty(UIBinding.VALID_DIRTY, false);
                
                if (uibinding.testDirty(UIBinding.TYPE_CHANGED_DIRTY))
                {
                    uibinding.setDirty(UIBinding.TYPE_CHANGED_DIRTY, false);
                    queueEvent(new DOMEvent(DOMEvent.XFORMS_TYPE_CHANGED, xfEl));
                }

                if (uibinding.testDirty(UIBinding.VALUE_CHANGED_DIRTY))
                {
                    uibinding.setDirty(UIBinding.VALUE_CHANGED_DIRTY, false);
                    queueEvent(new DOMEvent(DOMEvent.XFORMS_VALUE_CHANGED, xfEl));
                }
            }
        }
    }
    
    public void queueRewirings(Vector bindingList) {
        int count = bindingList.size();
        for(int i = 0; i < count; i++) 
        {
            UIBinding uibinding = (UIBinding) bindingList.elementAt(i);
            if (!isAlive(uibinding)) 
            {
                continue;
            }
            
            
            XFormsElement xfEl = uibinding.getElement();

            if (uibinding.getBindingStatus() == UIBinding.BOUND) 
            {
                if (uibinding.testDirty(UIBinding.RELEVANT_DIRTY)) 
                {
                    if (uibinding.getMIPAndClearDirty(UIBinding.RELEVANT_DIRTY)) 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_ENABLED, xfEl));
                    }
                    else 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_DISABLED, xfEl));
                    }

                }

                if (uibinding.testDirty(UIBinding.READONLY_DIRTY)) 
                {
                    if (uibinding.getMIPAndClearDirty(UIBinding.READONLY_DIRTY)) 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_READONLY, xfEl));
                    }
                    else 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_READWRITE, xfEl));
                    }

                }               
                
                
                if (uibinding.testDirty(UIBinding.REQUIRED_DIRTY)) 
                {
                    if (uibinding.getMIPAndClearDirty(UIBinding.REQUIRED_DIRTY)) 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_REQUIRED, xfEl));
                    }
                    else 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_OPTIONAL, xfEl));
                    }

                }               
                

                if (uibinding.testDirty(UIBinding.VALID_DIRTY)) 
                {
                    if (uibinding.getMIPAndClearDirty(UIBinding.VALID_DIRTY)) 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_VALID, xfEl));
                    }
                    else 
                    {
                        queueEvent(new DOMEvent(DOMEvent.XFORMS_INVALID, xfEl));
                    }

                }               

                if (uibinding.testDirty(UIBinding.TYPE_CHANGED_DIRTY))
                {
                    uibinding.setDirty(UIBinding.TYPE_CHANGED_DIRTY, false);
                    queueEvent(new DOMEvent(DOMEvent.XFORMS_TYPE_CHANGED, xfEl));
                }

                if (uibinding.testDirty(UIBinding.VALUE_CHANGED_DIRTY))
                {
                    uibinding.setDirty(UIBinding.VALUE_CHANGED_DIRTY, false);
                    queueEvent(new DOMEvent(DOMEvent.XFORMS_BINDING_CHANGED, xfEl));
                }
            }
            else 
            {
                // FIXME: Would maybe be better if model translated not bound to MIP changes
                // - now I don't know if it has changed or not, so I always send disabled event
                // OTOH I think spec says it should work that way anyhow...
                uibinding.setDirty(UIBinding.RELEVANT_DIRTY, false);
                uibinding.setDirty(UIBinding.READONLY_DIRTY, false);
                uibinding.setDirty(UIBinding.REQUIRED_DIRTY, false);
                uibinding.setDirty(UIBinding.VALID_DIRTY, false);
                
                if (uibinding.testDirty(UIBinding.TYPE_CHANGED_DIRTY))
                {
                    uibinding.setDirty(UIBinding.TYPE_CHANGED_DIRTY, false);
                    queueEvent(new DOMEvent(DOMEvent.XFORMS_TYPE_CHANGED, xfEl));
                }

                if (uibinding.testDirty(UIBinding.VALUE_CHANGED_DIRTY))
                {
                    uibinding.setDirty(UIBinding.VALUE_CHANGED_DIRTY, false);
                    queueEvent(new DOMEvent(DOMEvent.XFORMS_BINDING_CHANGED, xfEl));
                }
            }
        }
    }
		
	public void addUIBinding(UIBinding binding) {
		addWithoutDuplicates(binding, uiBindings);
	}

    public void removeUIBinding(UIBinding binding) {
        uiBindings.removeElement(binding);
        binding.reset(); // This removes references to the binding.
    }
    
	private void addDirtyDependency(UIBinding binding) {
		addWithoutDuplicates(binding, dirtyDeps);
		
	}

	private void addDirtyValue(UIBinding binding) {
		addWithoutDuplicates(binding, dirtyVals);		
	}

	private void addWithoutDuplicates(UIBinding binding, Vector bindingList) {
	    Enumeration bindEnum = bindingList.elements();
        while (bindEnum.hasMoreElements()) {
            UIBinding b = (UIBinding)bindEnum.nextElement();
            if(b.equals(binding)) {
                return;
            }
        }
		bindingList.addElement(binding);
	}

   void modelEvent(int event, boolean reevaluateUIBindings, String target_id,
            Node relatedNode, Instance relatedInstance)

    {
       // if (listener != null) {
       //     listener.structureChange(event, reevaluateUIBindings, target_id,
        //            relatedNode, relatedInstance);
       // }
    }
    
	
}
