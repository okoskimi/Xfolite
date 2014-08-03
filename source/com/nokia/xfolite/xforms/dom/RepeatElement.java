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

import com.nokia.xfolite.xforms.model.*;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.events.DOMEvent;
import com.nokia.xfolite.xml.xpath.NodeSet;

/**
 * This class is used to add repeat-specific functionality to itemset class.
 */

public class RepeatElement extends ItemsetElement {

	private RepeatItemElement selected;
	
	RepeatElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
        super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
	}
	
    public boolean elementParsed() {
        super.elementParsed();
        // We need to create a group that corresponds to the repeat
        // so we can easily add and delete items.
        return true; 
    }
    
    // Redefine not to listen for events. Repeat does not care if the children change
    // Nodeset changes are always signaled with reEvaluate method.
    protected void registerEventListeners() {
    }
    
    // Redefine so that we don't try to notify parent form control anymore
    // Instead we should notify our own control
    public void reEvaluateChildContexts(boolean force) {
        //#debug info
        System.out.println("Repeat: reEvaluateChildContexts");
        setItems();
        if (localName == "tbody") { // Rebuild table
            Node parent = parentNode;
            while(parent != null && parent.getLocalName() != "table") {
                parent = parent.getParentNode();
            }
            if (parent != null) {
                ((XFormsElement)parent).dispatchLocalEvent(DOMEvent.XFORMS_REBUILD_CONTROL);
            }
        }
    }

    // Redefine to do nothing.
    public void handleEvent(DOMEvent evt) {
    }
    
    public String getItemName() {
        return "repeat-item";
    }

    public void setSelected(int index) {
        RepeatItemElement repeatItem = getRepeatItem(index);
        setSelected(index, repeatItem);
    }
    
	public void setSelected(RepeatItemElement repeatItem)
	{
		int index = getIndex(repeatItem);
		setSelected(index, repeatItem);
	}
	
	public boolean isSelected(RepeatItemElement repeatItem) {
		return selected == repeatItem;
	}

	private void setSelected(int index, RepeatItemElement repeatItem) {
		//#debug info
		System.out.println("setSelected(" + index + "," + repeatItem + ")");
		if (index > 0) 
		{
			String id = getAttribute("id");
			if (id != "") 
			{
				getModel().setRepeatIndex(id, index);
			}
		}
		if (repeatItem != null) {
			if (selected != null) 
			{
				selected.dispatchEvent(DOMEvent.XFORMS_DESELECT);
			}
			selected = repeatItem;
			//#debug info
			System.out.println("Dispatching XFORMS_SELECT to " + selected.getLocalName() + "(" + selected.getClass().getName() + ")");
			selected.dispatchEvent(DOMEvent.XFORMS_SELECT);
		}
		
	}
	
	void clearSelected() 
	{
		if (selected != null) 
		{
		    // TODO: Remove "selected mark" for repeat item for selected
        }
		selected = null;

		String id = getAttribute("id");
		if (id != "") 
		{
			getModel().setRepeatIndex(id, 0);

		}

	}
	
	
	int getIndex(RepeatItemElement item) 
	{
		if (! isBound()) 
		{
			return 0;
		}
		
		Node node = item.getContext().contextNode;
		
		NodeSet nodeset = binding.getBoundNodes();
		
		int count = nodeset.getLength();
		for (int i=0; i < count; i++) 
		{
			if (node == nodeset.item(i)) 
			{
				return i+1; // First node has index 1 in XForms
			}
		}
		
		return 0;
	}

    RepeatItemElement getRepeatItem(int index)
    {
        Node n = this.getFirstChild();
        int curIndex = 0;
        while (n != null) {
            if (n instanceof RepeatItemElement) {
                curIndex++;
                if (curIndex == index) {
                    return (RepeatItemElement) n;
                }
            }
            n = n.getNextSibling();
        }
        return null;
    }
    
}
