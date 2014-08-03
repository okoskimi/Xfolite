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
import com.nokia.xfolite.xml.dom.events.DOMEventListener;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.nokia.xfolite.xml.xpath.XPathContext;


/**
 * This class is used for both repeat and itemset.
 */

public class ItemsetElement extends BoundElement implements DOMEventListener {

    protected Element template = null;
	
	ItemsetElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
        super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
	}

    public int getBindingType() {
        return UIBinding.NODE_SET_BINDING;
    }
    
    protected void registerEventListeners() {
        addEventListener(DOMEvent.XFORMS_VALUE_CHANGED, this, false);
    }
    
	// As soon as we enter a repeat we need to stop doing any initialization
    public boolean elementParsed() {
    	//#debug info
    	System.out.println("ItemsetElement.elementParsed");
    	// Template will be null if and only if we are parsing the document
        // In manual initialization, template is set because it is set just
        // before calling preInitialize when copying nodes from template.
        // Only the outermost repeat will be called here because we disable callbacks
        // for the subtree below.
        // Note that the callbacksEnabled value is cached in Element.parse so 
        // childrenParsed is still called for us.
        super.elementParsed();
        if (template == null) {
            ownerDocument.setCallbacksEnabled(false);
        }
        registerEventListeners();
        return false;
    }
    // Once we exit we need to move all the children into the template and start initializing again
    public boolean childrenParsed() {
    	//#debug 
    	System.out.println("ItemsetElement.childrenParsed");
    	super.childrenParsed();
        if (template == null) {
        	//#debug 
        	System.out.println("Creating template");
            template = ownerDocument.createElementNS(namespaceURI, prefix, "template");
            // Move all children to the template
            Node child = getFirstChild();
            Node n = null;
            while (child != null) {
                n = child;
                child = child.getNextSibling();
                removeChild(n);
                template.appendChild(n);
            }
            // Start initializing again
            ownerDocument.setCallbacksEnabled(true);
            
        }
        // Create items from template
        setItems();
        return false;
    }
    
    public void handleEvent(DOMEvent evt) {
        if (evt.getType() == DOMEvent.XFORMS_VALUE_CHANGED && evt.getEventPhase() == DOMEvent.BUBBLING_PHASE) {
            this.getParentNode().dispatchLocalEvent(DOMEvent.XFORMS_REBUILD_CONTROL);
        }
    }
    
	public boolean reEvaluateOwnContext(XPathContext parentContext) {

		//#debug info
		System.out.println("ItemsetElement: reEvaluateOwnContext");
		if (getBindingStatus() == UIBinding.UNINITIALIZED) 
		{
			//#debug info
			System.out.println("Not bound, returning false");
			return false; // Does this actually work?
		}

		setContext(parentContext);
		// C++ comment:
		// FIXME: XFormsModel seems to return false here even when we have new instance data from SOAP request
		// As kludge, always set items again
		//#debug info
		System.out.println("ItemsetElement: binding.reEvaluatebinding()");
		if (binding.reEvaluateBinding()) // Removed kludge "|| true", should not be needed anymore 
		{
			//#debug info
			System.out.println("Binding changed");
            // Is this needed?
			// clearSelected();

            // Cause reEvaluateChildContexts to be invoked
		    return true;
        }		
		//#debug info
		System.out.println("Binding not changed");
		return false;	
	}
	
	// Must not recurse to children (nonsensical for itemset or repeat)
    // Instead set items again.
	public void reEvaluateChildContexts(boolean force) {
		//#debug
		System.out.println("Re-evaluating child contexts");
	    setItems();
        Node parent = parentNode;
        while(parent != null && parent.getLocalName() != "select1" && parent.getLocalName() != "select") {
            parent = parent.getParentNode();
        }
        if (parent != null) {
            ((XFormsElement)parent).dispatchLocalEvent(DOMEvent.XFORMS_REBUILD_CONTROL);
        }
    }
    
    
    // Note: This considers ItemsetItems to correspond with nodeset node if the context node
    // matches. context size and position are ignored for matching purposes, though
    // the context size and position are set for new ItemsetItems.
    public void setItems() {        
    	if (binding == null) {
    		//#debug error
    		System.out.println("Binding is null in setItems!");
    		return;
    	}
    	
    	NodeSet aNodeset = binding.getBoundNodes();
    	//#debug 
    	System.out.println("Bound nodes: " + aNodeset.getLength());
    	binding.reEvaluateBinding();
    	
        Node curItem = getFirstChild();
        // There may be some garbage at beginning (i.e. template) but then we should have
        // a clear line of repeatitems
        while (curItem != null  && !(curItem instanceof ItemsetItemElement)) 
        {
            curItem = curItem.getNextSibling();
        }
        
        int len = aNodeset.getLength();
        int curNode = 0;

        ItemsetItemElement curItemsetItem;

        boolean firstSet = false;
        
        while (curNode < len)
        {
            if (curItem != null)
            {
                //Element el = curItem;
                curItemsetItem = (ItemsetItemElement) curItem;
                Node curContext = curItemsetItem.getContext().contextNode;

                // The below logic assumes that order is constant, i.e. nodes may
                // be present or not but their relative order does not change.
                // TODO: This needs to be fixed once we introduce sorting!
                
                // Cur item matches cur node, no change needed
                if (curContext == aNodeset.item(curNode))
                {
                    if (!firstSet)
                    {
                        curItemsetItem.setAsSelected();
                        firstSet = true;
                    }
                    curNode++;
                    curItem = curItem.getNextSibling();
                } // Cur item does not match but is in the nodeset, we need to add in-between nodes
                else if (aNodeset.contains(curContext)) 
                { // Could be optimized since we only need to look at nodes after curNode in the nodeset
                    while(curContext != aNodeset.item(curNode))
                    {
                        ItemsetItemElement newItem = addItem(aNodeset.item(curNode), curItem, curNode + 1, len);
                        curNode++;
                        if (!firstSet) 
                        {
                            newItem.setAsSelected();
                            firstSet = true;
                        }
                    }
                }
                else // Cur item is not in the nodeset, we remove it.
                {
                    Node remNode = curItem;
                    curItem = curItem.getNextSibling();
                    ((Element) remNode).notifyRemove(); // Trigger removingElement() callbacks
                    removeChild(remNode);
                }
            }
            else 
            {
                ItemsetItemElement newItem = addItem(aNodeset.item(curNode), curItem, curNode + 1, len);
                curNode++;
                if (!firstSet) 
                {
                    newItem.setAsSelected();
                    firstSet = true;
                }
            }
        }
        
        // There might still be unprocessed items, they need to be removed        
        while (curItem != null) 
        {
            Node remNode = curItem;
            curItem = curItem.getNextSibling();
            ((Element) remNode).notifyRemove(); // Trigger removingElement() callbacks            
            removeChild(remNode);
        }        

    }

    public String getItemName() {
        return "item";
    }

    /**
     * Create a new ItemsetItemElement according to the template and add it as a child of this element.
     * Note that initialization is done at the same time as creating the new subtree,
     * instead of first creating the subtree and then initializing. This allows single-pass
     * processing of the subtree.
     * @param aContext The (XPath) context node for the item
     * @param aLocation The child before which this node should be inserted (null if it should be added as the last child)
     * @return The created ItemsetItemElement.
     */
    
    public ItemsetItemElement
    addItem(Node aContext, Node aLocation, int contextPos, int contextSize) 
    {        
        // TODO: Add a differentiating running number to all ids.
        // Note that change must also be done to document id hashtable.
        // Add id attribute nodes to a local Hashtable, keyed by
        // the original attribute value.
        // Add other case etc. attribute nodes to a local Vector.
        // In the end, go through vector and update all attribute values to correspond
        // to the updated (differentiated) value.        

        // TODO: Use normal element as template.
        ItemsetItemElement itemEl = (ItemsetItemElement) ownerDocument.createElementNS(
                    XFormsDocument.XFORMS_NAMESPACE, "xf", getItemName());
        insertBefore(itemEl, aLocation);
        itemEl.setItemContext(new XPathContext(aContext, contextPos, contextSize));
        boolean parsing = getOwnerDocument().isParsing();

        // Since we cache these values, itemset/repeat item processing does not allow elements
        // to switch off callbacks. This means you cannot have e.g. lazy-init cases inside itemset/repeat items.
        boolean elCallbacksEnabled = getOwnerDocument().isElementCallbacksEnabled();
        boolean wgCallbacksEnabled = getOwnerDocument().isWidgetCallbacksEnabled();
        
        
        //#debug info
        System.out.println("element callbacks: " + elCallbacksEnabled);
        //#debug info
        System.out.println("widget callbacks: " + wgCallbacksEnabled);
        //#debug info
        System.out.println("parsing: " + parsing);
        if (parsing) {
        	//#debug info
        	System.out.println("setItems: preParse");
        	itemEl.preParse(elCallbacksEnabled, wgCallbacksEnabled);
        } else { 
        	//#debug info
        	System.out.println("setItems: preInitialize");
        	itemEl.preInitialize(elCallbacksEnabled, wgCallbacksEnabled);
        }
        Node n;
        Node itemParent, itemCur;
        itemCur = itemEl;
        itemParent = itemCur;

        n = template.getFirstChild();

        // At this point n is first child of root element of template.

        boolean itemset = false;
        String uidString = ""+System.currentTimeMillis();

        // n would only be null if template has no children.
        // In this case we create a repeat-item with no children.
        while (n != template && n != null) 
        {
            itemCur = n.cloneNode(false);
            // Process n:
            if (itemCur.getNodeType() == Node.ELEMENT_NODE) 
            {
                Element el = (Element) itemCur;
                String idVal = el.getAttribute("id");
                if (idVal != "") {
                    el.setAttribute("id","__repeat=" + uidString + ";" + idVal);
                }
                if (el.getLocalName() == "toggle") {
                    idVal = el.getAttribute("case");
                    if (idVal != "") {
                        el.setAttribute("case","__repeat=" + uidString + ";" + idVal);
                    }            
                }

                itemParent.appendChild(itemCur);


                if (itemCur instanceof ItemsetElement) {
                    ((ItemsetElement)itemCur).template = ((ItemsetElement)n).template;
                    itemset = true;
                }
                ((Element)itemCur).preParse(elCallbacksEnabled, wgCallbacksEnabled);
            }
            else 
            {
                itemParent.appendChild(itemCur);
            }

            // Go to next node:
            if (n.hasChildNodes() && !itemset) 
            {
                n = n.getFirstChild();

                // since we are moving one level down in template we must also
                // move the parent repeatitem node we are adding to one level down.
                itemParent = itemCur;
            }
            else
            {
                itemset = false;
                
                if (itemCur instanceof Element) {
                    ((Element)itemCur).postParse(elCallbacksEnabled, wgCallbacksEnabled);
                }
                
                while(n.getNextSibling() == null) 
                {
                    n = n.getParentNode();
                    itemParent = itemParent.getParentNode();
                    itemCur = itemCur.getParentNode();

                    if (n == template) 
                    {
                        break;
                    }
                    
                    if (itemCur instanceof Element) {
                        ((Element)itemCur).postParse(elCallbacksEnabled, wgCallbacksEnabled);
                    }
                    
                }
                if (n != template) 
                {
                    n = n.getNextSibling();
                }
            }

        }
        if (parsing) {
        	//#debug info
        	System.out.println("setItems: postParse");
        	itemEl.postParse(elCallbacksEnabled, wgCallbacksEnabled);
        } else { 
        	//#debug info
        	System.out.println("setItems: postInitialize");
        	itemEl.postInitialize(elCallbacksEnabled, wgCallbacksEnabled);
        }
        return itemEl;
    }
    
}
