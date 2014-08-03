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

package com.nokia.xfolite.xml.xpath;

import com.nokia.xfolite.xml.dom.Node;


public class NodeSet {
    
	protected Node nodesData[];
    protected int nodeCount;
	
    public NodeSet()
    {
    	this.nodesData = new Node[10];
    	XPathEvaluator.VectorCreationCounter++;
    }
	
	public NodeSet(Node n)
    {
		this();
		AddNode(n);
    }

    public void clear() {
        nodeCount = 0;
        // If this was a large nodeset, release memory.
        if (nodesData.length > 10) {
            nodesData = new Node[10];
        }
    }
    
    public boolean contains(Node node) {
        for(int i=0; i<nodeCount; i++) {
            if (nodesData[i] == node) {
                return true;
            }
        }
        return false;
    }
    
	/*
	 * Get item at specified index.
	 * @param index The index where to get the node from
	 */
	
	public Node item(int index)
    {
    	return nodesData[index];
    }
	
	public Node firstNode()
	{
		return nodeCount==0? null:nodesData[0];
	}

    public int getLength()
    {
    	return nodeCount;
    }
    
    public void AddNode(Node n)
    {
    	if (n == null) return;
    	
        for (int i = 0 ; i < nodeCount ; i++)
                if (n.equals(nodesData[i]))
                    return;
    	
        int newcount = nodeCount + 1;
        if (newcount > nodesData.length) {
            Object oldData[] = nodesData;
            int newCapacity = nodesData.length * 2;
            nodesData = new Node[newCapacity];
            System.arraycopy(oldData, 0, nodesData, 0, nodeCount);
        }
        nodesData[nodeCount++] = n;
    }
    
    protected NodeSet Union(NodeSet ns)
    {
    	if(ns!=null)
    	{
    		int ns_size = ns.getLength();
        	for (int i=0;i<ns_size;i++)
        	{	
        		AddNode(ns.item(i));
        	}	
    	}	
    	return this;
    }
}
