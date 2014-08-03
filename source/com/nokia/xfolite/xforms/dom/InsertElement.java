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
import com.nokia.xfolite.xml.dom.Attr;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.events.DOMEvent;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.nokia.xfolite.xml.xpath.XPathContext;
import com.nokia.xfolite.xml.xpath.XPathResult;

public class InsertElement extends DeleteElement {

	InsertElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
        super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
	}


    
    public void doHandleEvent(DOMEvent evt, ActionElement outermost) {
        	XPathContext insertContext = this.getInsDelContext();
        	if (insertContext!=null && insertContext.contextNode != null)
            {
        		// TODO: now this only works for elements
        		NodeSet nodes = this.getNodeset(insertContext);
        		int at = this.getAt(insertContext);
                if (at==-1||at>nodes.getLength()) at=nodes.getLength();
        		Node origin = this.getOrigin(nodes,insertContext);
       			Node insertLocationNode=nodes.getLength()<1?null:nodes.item(at-1);
       			Node insertLParent = insertLocationNode==null?insertContext.contextNode:insertLocationNode.getParentNode();
       			String position=this.getAttribute("position");
       			if (! "before".equals(position))
       			{
       			    //after
                    insertLocationNode=insertLocationNode==null?null:insertLocationNode.getNextSibling();
       			}
   				Node copy = origin.cloneNode(true);
       			if (origin.getOwnerDocument()!=insertLParent.getOwnerDocument()) {
       				copy=insertLParent.getOwnerDocument().adoptNode(copy);
                }
                //#debug info
                System.out.println("Inserting " + copy.getLocalName() + ": " + copy.getText());
                if (insertLocationNode == null) {
                	//#debug info
                	System.out.println("as child of " + insertLParent.getLocalName()
                			+ "(" + insertLParent.getClass().getName() + ")");
                	insertLParent.appendChild(copy);
                } else {
                	//#debug info
                	System.out.println("..before " + insertLocationNode.getLocalName() + ": " + insertLocationNode.getText());
                	insertLParent.insertBefore(copy,insertLocationNode);
                }
                //((Element)insertLParent).printSubtree();
       			outermost.setLocalState(ActionElement.REBUILD,true);
       			outermost.setLocalState(ActionElement.REWIRE,true);
            } else {
                //#debug warn
                System.out.println("Insert context not bound, insert not done.");
            }
    }
    
    
    protected Node getOrigin(NodeSet binding, XPathContext context)
    {
    	Node cNode =binding.getLength()>0?binding.item(binding.getLength()-1):context.contextNode; 
    	Attr origin = this.getAttributeNode("origin");
    	if (origin==null)
    	{
    		return cNode;
    	}
		XFormsModel model = this.getModel();
		XPathResult result = model.getXPathEvaluator().evaluate(origin.getValue(), 
				binding.firstNode()==null?cNode: binding.firstNode(), 
						this, XPathResult.NODESET);
		NodeSet resultNS= result.asNodeSet();
        if (resultNS == null) {
                this.logError("Origin is not bound!");
        }
		return resultNS!=null?
				(resultNS.getLength()<1?null:result.asNodeSet().item(0))
				:null;
    }
    
    

}
