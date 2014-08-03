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

import java.util.Vector;

import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.xpath.XPathExpression;
import com.nokia.xfolite.xml.xpath.XPathNSResolver;
import com.nokia.xfolite.xml.xpath.XPathResult;

/**
 * The Master Dependency Graph (MDG)
 * In this version, the MDG object is not re-used. To rebuild, create a new MDG object
 * and re-add all dependencies.
 * This class owns the main dependency graph data structures and performs
 * the related operations.
 * The MDG is optimized so that sub dependency graph is never created.
 */

class MDG {

	private XFormsModel model;
	private Vector mdgNodes;
	
	MDG(XFormsModel aModel){
		model = aModel;
		mdgNodes = new Vector();
	}
	
	/**
	 * Add a new dependency (MDG node) to the graph
	 * TODO: what about context size and position...
	 */
		
	void addDependency(Node aFromNode,
					Node aToNode,
					int aMIPType,
					XPathExpression expr,
					XPathNSResolver aResolver)
	{

		// First, add the from node, if necessary
		MDGNode fromNode = nodeToMDG(aFromNode, MIPExpr.CALCULATE);
		if (fromNode == null)
		{
			// we must add a value node to the graph in order to build dependency
			// this is always calc expr, since no dependencies from other type of nodes are allowed
			fromNode = new MDGNode(null,MIPExpr.CALCULATE,aFromNode,aResolver); // deleted in destructor
			addNode(fromNode);
		}
		// Second, add the to node and add it to the dependency list of from node
		// If the node already exists and has an XPath expression, then it is an error
		MDGNode toNode = nodeToMDG(aToNode, aMIPType);
		if (toNode != null)
		{
			// TODO:  a better test to see whether the expression is the same??
			if (toNode.expr != null && toNode.expr != expr)
			{
				throw new XFormsModelException("Dependency graph error: two calculate expressions for the same node");
			}
			// iExpr was NULL, so no other calculate expression was attached to this node yet
			toNode.expr=expr;
			toNode.resolver = aResolver;
		}
		else
		{
			// a calculate for to node was not found in the graph
			toNode = new MDGNode(expr,aMIPType,aToNode,aResolver);
			addNode(toNode);
		}
		// Spec: The depList for a vertex v is assigned to be the vertices other than v 
		//whose computational expressions reference v (described below). 
		//Vertex v is excluded from its own depList to allow self-references to occur without 
		//causing a circular reference exception. 
		toNode.inDegree++;
		if (fromNode.dependencies == null) {
			fromNode.dependencies = new Vector();
		}
		fromNode.dependencies.addElement(toNode);
	}
	void calculateNonDependentNode(Node aNode,
				int aMIPType,
				XPathExpression expr,
				XPathNSResolver aResolver)
	{

		// lets create a node for calculation, but never add it to the graph for now
		//CMDGNode( RXPathExpression* aExpr, int aType, TNode aInstanceNode, MNamespaceResolver *aNSRes)
		MDGNode tempNode = new MDGNode(expr,aMIPType,aNode,aResolver); // deleted in destructor
		// LOG_DEBUG("Calculating node without dependency.");
		calculate(tempNode);
		tempNode = null; // Release for GC
	}

	void recalculate(Vector changed, boolean initial)
	{

		Vector changedMDG = new Vector();
		if (initial)
		{
			//#debug
            System.out.println("Recalculating Initial. MDGNode Count: " + this.mdgNodes.size());
			findZeroIndegrees(changedMDG);
			recalculatePertinentSubGraph(changedMDG);
			//#debug
            System.out.println("Recalculate done.");
		}
		else
		{
			//#debug
            System.out.println("Recalculating. Changed nodes / MDGNode Count: "
                    + changed.size() + " / " + mdgNodes.size());
			nodeArrayToMDG(changed, changedMDG);
			createPertinentSubGraph(changedMDG);
			recalculatePertinentSubGraph(changedMDG);
			//#debug
            System.out.println("Recalculate done.");
		}
		
	}

		
	private MDGNode nodeToMDG(Node aNode, int aMIPType)
	{
		int count = mdgNodes.size();
		MDGNode curNode = null;
		for (int i =0;i<count ;i++)
		{
			curNode = (MDGNode) mdgNodes.elementAt(i);
			if (curNode.instanceNode == aNode && curNode.type == aMIPType)
			{
				return curNode;
			}
		}
		return null;
	}
	
	private void addNode(MDGNode aNode)
	{
		mdgNodes.addElement(aNode);
	}
	
	/**
	 *  Find all nodes which have zero indegree and store to zeros
	 */
	private void findZeroIndegrees(Vector zeros)
	{
		int count = mdgNodes.size();
		for (int i =0;i < count;i++)
		{
			MDGNode curNode = (MDGNode) mdgNodes.elementAt(i);
			// LOG_DEBUG("Indegree: %d", mdgnode->iInDegree);
			if (curNode.inDegree == 0)
			{
				zeros.addElement(curNode);
			}
		}		
	}

	private void createPertinentSubGraph(Vector changed)
	{
		int count = changed.size();
		MDGNode curNode = null;
		for (int i =0;i < count;i++)
		{
			curNode = (MDGNode)changed.elementAt(i);
			incrementInDegree(curNode, true); // calls a recursive function that explores the subtrees
		}		
	}
	
	private void recalculatePertinentSubGraph(Vector changed)
	{
		// find next node which has indegree=0
		boolean stop = false;
		MDGNode curNode;
		while (!stop)
		{
			curNode = null;
			// find next node which has indegree=0
			// TODO: optimize
			int count = mdgNodes.size();
			for (int i =0; i < count; i++)
			{
				MDGNode tmpNode = (MDGNode) mdgNodes.elementAt(i);
				if (tmpNode.inDegree == 0) 
				{
					curNode = tmpNode;
					break;
				}
			}
			if (curNode == null)
			{
				stop = true;
				throwExceptionIfHasCycle(changed); // check if there are loops in the graph
			}
			else
			{
				calculate(curNode);
				// This is same as unsetting visited flag in the orig algorithm
				curNode.inDegree = -1;
				count = (curNode.dependencies == null) ? 0 : curNode.dependencies.size();
				for (int k=0; k < count; k++)
				{
					MDGNode dependent = (MDGNode) curNode.dependencies.elementAt(k);
					dependent.inDegree--;
				}
			}
		}
		resetGraph();		
	}

	private void calculate(MDGNode aMDGNode)
	{
		if (aMDGNode.type == MIPExpr.CALCULATE)
		{
			if (aMDGNode.expr != null)
			{
				XPathResult	result = aMDGNode.expr.evaluate(aMDGNode.instanceNode, 
									XPathResult.STRING);
			    String strValue = result.asString();    

				model.setNodeText(aMDGNode.instanceNode,strValue); // IN model.h, TODO: reimplement
			}
			// Else this is actually a value node (a leaf node without calculate expression),
			// so nothing is done in calculate
		}
		else
		{
			// BOOLEAN STATE EXPRESSIONS
			XPathResult	result = aMDGNode.expr.evaluate(aMDGNode.instanceNode,
					XPathResult.BOOLEAN);
			boolean resBool = result.asBoolean().booleanValue();
			if (aMDGNode.type <= MIPExpr.MAXBOOLEXPRID && aMDGNode.type > -1)
			{
				InstanceItem item = InstanceItem.getInstanceItem(
						aMDGNode.instanceNode, model);
				item.setBooleanLocalState(aMDGNode.type, resBool);
				//LOG_ASSERT(item->GetBooleanLocalState(aMDGNode->iType)==resBool); // checks whether it was set
			}
			else
			{
				throw new XFormsModelException("There was an unknown expression type: " + aMDGNode.type);
			}
		}
	}

	private void throwExceptionIfHasCycle(Vector aChanged)
	{
		int count = mdgNodes.size();
		for (int i =0; i < count; i++)
		{
			MDGNode tmpnode = (MDGNode) mdgNodes.elementAt(i);
			if (tmpnode.inDegree > 0) 
			{
				resetGraph();
				throw new XFormsModelException("Cycle in the Dependency Graph");
			}
		}
	}

	private void resetGraph()
	{
		// TODO: optimize, only go through nodes which have been included in the sub graph calculation
		int count = mdgNodes.size();
		for (int i = 0; i < count; i++)
		{
			MDGNode curNode = (MDGNode) mdgNodes.elementAt(i);
			curNode.inDegree = -1;
		}
	}

	/**
		 * This method does the recursion needed to create the subdependency graph.
		 * This replaces the use of stack in the XForms 1.0 specification algorithm.
		 * @param mdgnode the node in question
		 * @param isStartNode set this to true only to the nodes which were changed and 
		 *        whose indegree should become 0. All other dependend nodes indegree will become >0
		 */
	private void incrementInDegree(MDGNode node, boolean isStartNode)
	{
		if (!isStartNode && node.inDegree == -1)
		{
			node.inDegree = 0;
		}
		node.inDegree++;

		// only increment children if this node is not visited
		if (node.inDegree < 2)
		{
			int count = (node.dependencies == null) ? 0 : node.dependencies.size();
			for (int k =0; k < count; k++)
			{
				MDGNode depNode = (MDGNode) node.dependencies.elementAt(k);
				incrementInDegree(depNode, false); // recursion step
			}
		}
	}

	private void nodeArrayToMDG(Vector nodeset, Vector mdgNodes)
	{
		int count = nodeset.size();
		for (int i=0; i < count; i++)
		{
			Node n = (Node) nodeset.elementAt(i);
			MDGNode mdgn = nodeToMDG(n, MIPExpr.CALCULATE);
			if (mdgn != null)
			{
				mdgNodes.addElement(mdgn);
			}
		}		
	}
	
}
