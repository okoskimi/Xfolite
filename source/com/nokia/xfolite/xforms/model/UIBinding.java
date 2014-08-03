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

import com.nokia.xfolite.xforms.dom.XFormsElement;
import com.nokia.xfolite.xforms.model.datatypes.DataTypeBase;
import com.nokia.xfolite.xforms.model.datatypes.ValueProvider;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.nokia.xfolite.xml.xpath.XPathContext;
import com.nokia.xfolite.xml.xpath.XPathExpression;
import com.nokia.xfolite.xml.xpath.XPathNSResolver;
import com.nokia.xfolite.xml.xpath.XPathResult;


/**
 * A class for binding UI elements to parts of the instance document.
 * TODO: add NameSpaceResolver 
 * 
 * @author mattisil
 *
 */
public class UIBinding implements InstanceItemListener, ValueProvider {

	// Binding types
	public static final int NODE_SET_BINDING = 1;
	public static final int SINGLE_NODE_BINDING = 2;
	public static final int VALUE_BINDING = 3;

    //enum TBindingStatus {EUninitialized, EBound, EBoundAndEmptyNodeset};
	// Binding status
	public static final int UNINITIALIZED = 0;
	public static final int BOUND = 1;
	public static final int BOUND_AND_EMPTY_NODESET = 2;
	
	/** IDs for different dirty flags. Based on these flags 
	 * different events are dispatched and form control value is changed.
	*/
	public static final int RELEVANT_DIRTY = 1 << 1;
	public static final int READONLY_DIRTY = 1 << 2;
	public static final int REQUIRED_DIRTY = 1 << 3;
	public static final int VALID_DIRTY = 1 << 4;
	public static final int VALUE_CHANGED_DIRTY = 1 << 5;
	public static final int TYPE_CHANGED_DIRTY = 1 << 6;
		
//	RNodeSet iBoundNodes;
//	RNodeSet iDependentNodes;
//	RXPathExpression iXPathExpression;
//	TNode iContextNode;
//	const MNamespaceResolver* iResolver;
//	MUIBindingOwner* iOwner;
//	TBindingType iBindingType;
//	TBindingStatus iBindingStatus;
//	TBool iUsesBindAttr;
//	TDOMString iBindID;
//	RXPathResult iResult; // needed for cleanup
//	CUI *iUI;
//	TDirtyState iDirtyState;
//	TInt iFunctionDependencies;
//	//TBool iBoundNodesSet;


    protected XFormsModelUI modelUI;
    protected int bindingType;
	protected int bindingStatus = UNINITIALIZED;
	protected String bindID;
	protected int functionDependencies = XFormsModel.NO_DEP;
	protected XPathContext context = null;
	protected NodeSet boundNodes = new NodeSet();
	protected NodeSet dependentNodes = new NodeSet();
	protected int dirtyState;

	// This is used to avoid double re-evals in refresh
    // TODO: Actually implement it
	boolean reEvalDone = false;
	
	protected XPathExpression xPathExpression;
    protected XPathResult result;
    protected String lastValueBindingValue;
    protected XFormsElement element;
    
	/**
	 * Construct this binding from ref, value, or nodeset attributes.
	 * 
	 * @param xPathString  the XPath expression as a string
	 * @param contextNode  the context node for the xpath evaluation
	 * @param bindingType  one of the binding types defined in BoundElement
	 * @param model        the model in which the bound node will be
	 * @param doc
	 */

    /*  BoundElement (String xPathString, boolean isBindId,
    int bindingType, 
    XFormsDocument doc)*/    
    
    public UIBinding(XPathContext context, String xpath, int bindingType, XFormsModelUI modelUI, XPathNSResolver resolver,
            XFormsElement element)

	{
        this.bindingType = bindingType;
        this.xPathExpression = modelUI.getModel().createExpression(xpath, resolver);
        this.context = context;
        this.modelUI = modelUI;
        this.element = element;
        reEvaluateBinding();
    }       

    public UIBinding(XPathContext context, XPathExpression xpathExpr, int bindingType, XFormsModelUI modelUI,
            XFormsElement element)

    {
        this.bindingType = bindingType;
        this.xPathExpression = xpathExpr;
        this.context = context;
        this.modelUI = modelUI;
        this.element = element;
        reEvaluateBinding();
    }    
    
    public XFormsElement getElement() {
        return element;
    }
        
    public XFormsModelUI getUI() {
        return modelUI;
    }
    
    public int getBindingType() {
        return bindingType;
    }

    public XFormsModel getModel() {
        return modelUI.getModel();
    }    
    
    /**
     * Set the value of a node for a binding with a single node.
     * 
     * @param string  the new string value for the bound node
     */
	public void setStringValue(String string) {
        //#debug
        System.out.println("UIBinding.setStringValue");
        InstanceItem item = getInstanceItem();
        //#debug
        System.out.println("UIBinding.gotInstanceItem");
        if (item != null) {
            item.setStringValue(string);
        }
	}

    /**
     * Get the value of a node for a binding with a single node.
     * 
     * @return  the string value of the bound node
     */
	public String getStringValue() {
        // TODO: What if you try to ask for a string
        // value for a node set binding?
        
        switch (bindingType) {
        
        case UIBinding.SINGLE_NODE_BINDING:
            InstanceItem item = getInstanceItem();

            if (item == null) {
                return null;
            }
            return item.getStringValue();

        case UIBinding.VALUE_BINDING:
            //TODO: Couldn't we just return this.result?
            XPathResult result = null;
            try {
                result = xPathExpression.evaluate(context,
                        XPathResult.STRING);
            } catch (Exception ignore) {
            }
            
            if (result == null) {
                return null;
            }
            return result.asString();

        default:
            return null;

        }
    }

	public boolean getBooleanState(int aMIPStateId) {
        InstanceItem item = getInstanceItem();
        if (item == null) {
        	switch (aMIPStateId) {
        	case MIPExpr.READONLY:
        	case MIPExpr.REQUIRED:
        		return false;
            case MIPExpr.RELEVANT:
                return bindingType == VALUE_BINDING || bindingStatus == BOUND;
            default:
        		return true;
        	}
        }
        return item.getBooleanState(aMIPStateId);
	}
	
    public DataTypeBase getDataType() {        
        switch (bindingType) {
        
        case UIBinding.SINGLE_NODE_BINDING:
            InstanceItem item = getInstanceItem();

            if (item == null) {
                return null;
            }
            return item.getDataType();

        case UIBinding.VALUE_BINDING:
            /* WARNING: This used to ALWAYS re-evaluate the result. Now it does not. Might cause trouble.
            XPathResult result = null;
            try {
                result = xPathExpression.evaluate(contextNode,
                        XPathResult.STRING);
            } catch (Exception ignore) {
            }
            */
            if (result == null) {
                return null;
            }
            switch(result.getType()) {
            case XPathResult.BOOLEAN:
                return XFormsModel.getDataTypeFactory().stringToDatatype(
                        DataTypeBase.XML_SCHEMAS_NAMESPACE_NAME, "boolean");
            case XPathResult.NUMBER:
                return XFormsModel.getDataTypeFactory().stringToDatatype(
                        DataTypeBase.XML_SCHEMAS_NAMESPACE_NAME, "decimal");
            default:
                return XFormsModel.getDataTypeFactory().stringToDatatype(
                        DataTypeBase.XML_SCHEMAS_NAMESPACE_NAME, "string");
            }

        default:
            return null;

        }
    }
    
	public boolean setContext(XPathContext context) {
		this.context = context;
        if (context == null) {
            reset();
            return true;
        }
		return reEvaluateBinding();
	}
	
    
    public String getLastValue()
    {
        return lastValueBindingValue;
    };

    
	public void removeListeners() {
		if (bindingStatus == BOUND) {
			if (boundNodes.getLength() > 0) {
				if (bindingType == SINGLE_NODE_BINDING) {
					Node firstNode = (Node)boundNodes.item(0);
					InstanceItem item = getModel().getInstanceItemForNode(firstNode);
					item.removeInstanceItemListener(this);
				}
			}
			for (int i = 0; i < dependentNodes.getLength(); i++) {
				Node depNode = (Node)dependentNodes.item(i);
				InstanceItem item = getModel().getInstanceItemForNode(depNode);
				item.removeInstanceItemListener(this);
			}
		}
	}

    private boolean isBoundNode(Node node) {
        if (bindingType==SINGLE_NODE_BINDING) {
            return node.equals(getBoundNode());
        }
        return false;
    }

	int getTreeDepth()
	{
		return element.getTreeDepth();
	}  

	boolean isReEvalDone()
	{
		return reEvalDone;
	}
	
	void clearReEvalDone()
	{
		reEvalDone = false;
	}
	
	/**
	 * 
	 * @return has the value of the binding changed
	 */
//	public boolean reEvaluateBinding() {
//		return false;
//	}

	private int evalCount = 0;

	public boolean reEvaluateBinding() {
        //#debug
        System.out.println("UIBinding.reEvaluateBinding() #" + (++evalCount));

        //#if tmp.perf
        if (element != null) {
            System.out.println("Element: <" + element.getLocalName()
                       + " ref=\"" + element.getAttribute("ref") + "\""
                       + " value=\"" + element.getAttribute("value") + "\""
                       + " nodeset=\"" + element.getAttribute("nodeset") + "\">");
        }
        long start = System.currentTimeMillis();
        //#endif
        
        // save the old binding in order to compare the new nodeset
		//RNodeSet oldBinding = iBoundNodes; // this does not DEEP COPY, So nodeset binding does not work!!
		NodeSet oldBinding = boundNodes;
		Node oldBoundNode = null;
		XPathResult oldResult = result;
		int oldBindingStatus = bindingStatus;		
		int oldFunctionDependencies = functionDependencies;
		if (bindingType==SINGLE_NODE_BINDING) {
			if (bindingStatus == BOUND && boundNodes.getLength() > 0) {
				oldBoundNode = (Node)boundNodes.item(0);
			}
		}	

		reEvalDone = true; // mark re-eval done so we don't do it again needlessly.
		removeListeners();
        NodeSet dependents = new NodeSet();
		this.dependentNodes = dependents;
		XPathResult newResult = null;
		
		if(context != null)	{
		    IntWrapper newDependencies = new IntWrapper();		    
		    //XPath evaluator = new XPath(xPathString);
	        //#if tmp.perf
		    long t0 = System.currentTimeMillis() - start;
		    //#endif
		    try {
			    newResult = getModel().evaluateWithDependencies(context, dependents, xPathExpression, newDependencies);
			    functionDependencies = newDependencies.value;
		        // newResult = xPathExpression.evaluate(context, XPathResult.ANY);

		    } catch (Exception e) {
		    	//#debug error
                System.out.println("Could not re-evaluate binding: " + e);
                return false;
		    }
	        //#if tmp.perf
		    long t1 = System.currentTimeMillis() - start - t0;
		    System.out.println("Dependency analysis took " + t1 + " ms");
		    //#endif
		    
		    if (newResult.getType() == XPathResult.NODESET || bindingType == VALUE_BINDING) {
		        boundNodes = newResult.asNodeSet(); 
		        if (bindingType == SINGLE_NODE_BINDING || bindingType == VALUE_BINDING) {
		            if (boundNodes.getLength() > 0) {
		                // This adds a listener for the value change of the bound node
		                Node firstNode = (Node)boundNodes.item(0);
		                InstanceItem item = getModel().getInstanceItemForNode(firstNode);
		                item.addInstanceItemListener(this);
		            }
		        }
		        // NOW: for each type of UI binding (singlenode, value, nodeset), add listeners
		        // for dependents
		        for (int i=0; i<dependents.getLength(); i++) {
		            Node depNode = (Node)dependents.item(i);
		            if (!isBoundNode(depNode)) {
		                InstanceItem item = getModel().getInstanceItemForNode(depNode);
		                item.addInstanceItemListener(this);
		            }
		        }
		        //#if tmp.perf
			    long t2 = System.currentTimeMillis() - start - t1 - t0;
			    System.out.println("Adding instance item listeners took " + t2 + " ms");
			    //#endif
		        
		    } else {
		        boundNodes = new NodeSet();
		    }		
		} else {
		    functionDependencies = 0;
		    boundNodes = new NodeSet();
		    newResult = new XPathResult(null);
		}

		boolean changed = false;
		if (boundNodes.getLength() > 0) {
		    bindingStatus = BOUND;
        } else {
            bindingStatus = BOUND_AND_EMPTY_NODESET;
        }
	
		if (bindingType == SINGLE_NODE_BINDING)
		{
			InstanceItem item = null;
			Node oldN = oldBoundNode;
			if (oldN != null)
			{
				item = getModel().getInstanceItemForNode(oldN);
			}
			DataTypeBase oldType = null;
			if (item != null) 
			{
				oldType = item.getDataType();
			}
			Node newN = null;
			if (bindingStatus == BOUND) 
			{
				newN = boundNodes.firstNode(); // returns null if empty
			}
			changed = (oldN != newN); // nodeset is changed if the first node is not the same
			// check if value changed, and dispatch necessary events
			// The below is not used and likely won't work if not bound --Oskari
			// TString newValue = GetInstanceItemL()->GetStringValueL();
            
            // Fixed to not mark as changed in first re-eval
			if (changed && oldBindingStatus != UNINITIALIZED)
			{
				dirtyState = VALUE_CHANGED_DIRTY;
				modelUI.valueChanged(this);
				DataTypeBase newType = null;
				if (newN != null)
				{
					newType = getModel().getInstanceItemForNode(newN).getDataType();
				}
				if (oldType != newType) 
				{
					modelUI.statusChanged(MIPExpr.TYPE, true, this);
					int did = TYPE_CHANGED_DIRTY;
					setDirty(did, true);
				}
			}
			// Removed as per above
			// newValue.Free();
		}
		else if (bindingType == NODE_SET_BINDING)
		{
			changed = !(isSameNodeSet(oldBinding, boundNodes)); // nodeset is changed if the nodesets are not identical
		}
        else if (bindingType == VALUE_BINDING)
        {
            String newString = newResult.asString();
            //#debug
            System.out.println("Comparing value binding, '" + newString + "' vs '" + lastValueBindingValue + "'");
            if (newString == "" || lastValueBindingValue == null)
            {
                if (newString == "" && lastValueBindingValue == null) {
                    changed=false;
                }
                else {
                    changed=true;
                }
            }
            else
            {
                changed = !(lastValueBindingValue.equals(newString)); 
            }
            lastValueBindingValue=newString;
            //#debug
            System.out.println("changed = " + changed);
			if (changed && oldBindingStatus != UNINITIALIZED)
			{
				dirtyState = VALUE_CHANGED_DIRTY;
				modelUI.valueChanged(this);
			}
        }
        
        
        //if (iFunctionDependencies!=oldFunctionDependencies) changed=TRUE; change of function dependency does not mean that the binding changed
		result = newResult;

		//#if tmp.perf
	    long t = System.currentTimeMillis() - start;
	    System.out.println("Re-evaluation took alltogether " + t + " ms");
		if (t > 100) {
			System.out.println("*********************** LONG RE-EVAL TIME ABOVE ***********************");
		}
		//#endif
		
		return changed;
	}
	
	boolean isSameNodeSet(NodeSet set1, NodeSet set2)
	{
		int count = set1.getLength();
		if (count != set2.getLength())
		{
			return false;
		}
		
		for (int i=0; i < count; i++)
		{
			Node node1 = set1.item(i);
			Node node2 = set2.item(i);
			if (node1 != node2)
			{
				return false;
			}
		}
		return true;
	}
	
	
	public void valueChanged(String value, InstanceItem item) {
        //#debug 
        System.out.println("UIBinding[" + this.hashCode() + "].valueChanged(" + value +")");
        /*
        if (element != null) {
        	element.printSubtree();
        }
         */        
		if (bindingType == SINGLE_NODE_BINDING || bindingType == NODE_SET_BINDING) {
			if(item.getNode().equals(getBoundNode()) && bindingType == SINGLE_NODE_BINDING) {
				dirtyState = VALUE_CHANGED_DIRTY;
                modelUI.valueChanged(this);
			} else {
				dirtyState = VALUE_CHANGED_DIRTY;
                modelUI.dependencyChanged(this);
			}
		} else if (bindingType == VALUE_BINDING)
        { /* value binding is different: it automatically re-evaluates itself, and reports value changes */
            boolean change;
            change = this.reEvaluateBinding();
            if (change)
            {
                dirtyState= VALUE_CHANGED_DIRTY;
                modelUI.valueChanged(this);
            }
        }
		
	}

	public void statusChanged(int MIPType, boolean newStatus, InstanceItem item) {
		if (bindingType == SINGLE_NODE_BINDING) {
			if (item.getNode().equals(getBoundNode())) {
			    modelUI.statusChanged(MIPType, newStatus, this);

                int dirtyID = MIPTypeToDirtyID(MIPType);
				setDirty(dirtyID, true);				
			}
		}		
	}
    
    /**
     * Identify the type of dirtiness based on the XFormsModel Item Property
     * that's been changed.
     * 
     * @see   MIPExpr
     * @param MIPType  the XFormsModel Item Property type as defined in MIPExpr
     * @return ID for the type of dirtiness as defined in BoundElement 
     */
    private int MIPTypeToDirtyID(int MIPType) {
        if (MIPType == MIPExpr.RELEVANT) return RELEVANT_DIRTY;
        if (MIPType == MIPExpr.REQUIRED) return REQUIRED_DIRTY;
        if (MIPType == MIPExpr.READONLY) return READONLY_DIRTY;
        if (MIPType == MIPExpr.CONSTRAINT) return VALID_DIRTY;
        if (MIPType == MIPExpr.SCHEMA_VALID) return VALID_DIRTY;
        if (MIPType == MIPExpr.CALCULATE) return VALUE_CHANGED_DIRTY;
        if (MIPType == MIPExpr.TYPE) return TYPE_CHANGED_DIRTY;
        return VALID_DIRTY;
    }

    public NodeSet getBoundNodes() {
        return boundNodes;
    }

	public int getBindingStatus()
	{
		return bindingStatus;
	}
	

	boolean getBooleanDirtyState(int aStateId)
	{
		InstanceItem item = getInstanceItem();
		if (item != null)
		{
			if (aStateId == READONLY_DIRTY)
			{
				return item.getBooleanState(MIPExpr.READONLY);
			}
			if (aStateId == RELEVANT_DIRTY)
			{
				return item.getBooleanState(MIPExpr.RELEVANT);
			}
			if (aStateId == VALID_DIRTY)
			{
				return item.getBooleanState(MIPExpr.VALID);
			}
			if (aStateId == REQUIRED_DIRTY)
			{
				return item.getBooleanState(MIPExpr.REQUIRED);
			}

        }
		return false;
	}


	/** this function does two things: 1) it clears the dirty bit for the state. 2) it returns
	 *  the current value for the MIP.
	 *  NOTE: in the case of TDirtyID.EValueChangedD, it will always return FALSE
	 */
	boolean getMIPAndClearDirty(int aStateId)
	{
		setDirty(aStateId, false);
		if (aStateId == VALUE_CHANGED_DIRTY)
		{
			return false;
		}
		return getBooleanDirtyState(aStateId);
	
	}
	
	int getFunctionDependencies()
	{
		return functionDependencies;
	}
	
	boolean testDirty(int aStateId)
	{
		return (dirtyState & aStateId) > 0;
	}
	
	
	/**
	 *  Returns the bound node in single node binding mode.
	 * @throws XFormsBindingException 
	 */ 
	public Node getBoundNode() //throws XFormsBindingException 
	{
		if (bindingType==SINGLE_NODE_BINDING)
		{
			if (boundNodes.getLength() > 0)
			{
				return (Node)boundNodes.item(0);
			}
		}
        //throw new XFormsBindingException();
        // TODO: should this be null?
		return null;
	}
	
	public void setDirty(int stateId, boolean value)
	{
		if (value) {
			dirtyState|=stateId;
		} else {
			dirtyState&=~stateId;
		}
	}

	private InstanceItem getInstanceItem() {
		Node node = getBoundNode();
		if (node == null) {
			return null;
		}
		XFormsModel model = getModel();
		return model.getInstanceItemForNode(node);
	}

    void reset()
    {
        removeListeners();
        dependentNodes.clear();
        boundNodes.clear(); // Is this OK to do? Was not in the original C++ code but seems reasonable.
        result = null;
        bindingStatus = UNINITIALIZED;
        reEvalDone = false;
    }

}
