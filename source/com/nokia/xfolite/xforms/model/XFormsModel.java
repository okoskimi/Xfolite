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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.Vector;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import com.nokia.xfolite.xforms.dom.ModelElement;
import com.nokia.xfolite.xforms.dom.TimerElement;
import com.nokia.xfolite.xforms.dom.XFormsDocument;
import com.nokia.xfolite.xforms.model.datasource.DataSource;
import com.nokia.xfolite.xforms.model.datatypes.DataTypeBase;
import com.nokia.xfolite.xforms.model.datatypes.DataTypeFactory;
import com.nokia.xfolite.xforms.xpath.XFormsCoreFunctionLibrary;
import com.nokia.xfolite.xml.dom.Attr;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.Text;
import com.nokia.xfolite.xml.dom.events.DOMEvent;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.nokia.xfolite.xml.xpath.XPathContext;
import com.nokia.xfolite.xml.xpath.XPathEvaluator;
import com.nokia.xfolite.xml.xpath.XPathExpression;
import com.nokia.xfolite.xml.xpath.XPathNSResolver;
import com.nokia.xfolite.xml.xpath.XPathResult;

public class XFormsModel {

	// Structure change reasons
	public static final int SUBMISSION = 0;
	public static final int INSERT = 1;
	public static final int DELETE = 2;
	public static final int SCRIPT = 3;
	
	// Insert positions
	public static final int BEFORE = 0;
	public static final int AFTER = 1;
	
	// Function dependencies
	public static final int NO_DEP = 0;
	public static final int INDEX_DEP = 1 << 1;
	public static final int UNKNOWN_DEP = 2 << 1;
	int functionDependencies = NO_DEP;
	boolean isTrackingDependencies = false;
	
	private static DataTypeFactory dataTypeFactory;

	private MDG depGraph;
	
	private String id;	
	private Hashtable instanceMap;
	private ModelElement modelElement;
	private Vector instances;
	private Vector uis;
    private Vector binds;
	// TODO: submissions, cursors
	//private static XPathContext;
	private Vector changedNodes;
	
	private boolean closed=false;
	
    private XPathEvaluator xpev;
    XFormsCoreFunctionLibrary xformsLibrary;
    
    private Vector externalDatasources;
    
    private Hashtable binaryAttachments;
    
	public XFormsModel(String id, ModelElement element) {
		this.id = id;
		this.instanceMap = new Hashtable();
		this.instances = new Vector();
		this.uis = new Vector();
		this.changedNodes = new Vector();
        this.binds = new Vector();
        this.xpev = new XPathEvaluator();
        this.xformsLibrary = new XFormsCoreFunctionLibrary();
        this.xformsLibrary.setUI(((XFormsDocument)element.getOwnerDocument()).getUserInterface());
        this.modelElement=element;
        xpev.AddFunctionLibrary(xformsLibrary);
	}

    public static DataTypeFactory getDataTypeFactory() {
        if (dataTypeFactory == null) {
            dataTypeFactory = new DataTypeFactory();           
        }
        return dataTypeFactory;
    }

    public static void addDataType(DataTypeBase base) {
    	DataTypeFactory dfact = getDataTypeFactory();
    	if (dfact != null) {
    		dfact.addMapping(base);			
    	}
    }
    
    public BinaryAttachment getBinaryAttachment(InstanceItem item)
    {
    	if (this.binaryAttachments==null) return null;
    	return (BinaryAttachment)this.binaryAttachments.get(item);
    }
    
    public Hashtable getBinaryAttachments()
    {
    	return this.binaryAttachments;
    }
    
    public void setBinaryAttachment(InstanceItem item, BinaryAttachment attachment)
    {
    	if (this.binaryAttachments==null) this.binaryAttachments=new Hashtable();
    	this.binaryAttachments.put(item,attachment);
    }

    
    public void setValue(String xpath, XPathContext context, XPathNSResolver res, String value /*, int size, int position */) {
    	//XPathExpression xpathExpr = new XPathExpression(xpath);
        XPathExpression xpathExpr = createExpression(xpath, res);
        
    	NodeSet resNodes = null;
    	try {
    		resNodes = xpathExpr.evaluate(context, XPathResult.NODESET).asNodeSet();
    	} catch (Exception e) {
    	    // TODO: how do we handle XPath errors?
        	errorHandler("Setting value failed, XPath didn't evaluate succesfully: " + e.getMessage());
    	}
    	if (resNodes != null) {
            Node n;
            if (resNodes.getLength() > 0) {
    		n = (Node)resNodes.item(0);
            } else {
                // TODO: XFormsModelException?
                return;
            }
    		setNodeText(n, value);
    	}
    }

    public String getValue(String xpath, XPathContext context, XPathNSResolver resolver/*, int size, int position */) {
        XPathExpression xpathExpr = createExpression(xpath, resolver);
    	//XPath xpathExpr = new XPath(xpath);
    	String resultStr = null;
        XPathResult result = null;
    	try {
            //result = xpathExpr.evaluate()
             result = xpathExpr.evaluate(context, XPathResult.ANY);
    	} catch (Exception e) {
    	    //	 TODO: how do we handle XPath errors?
            errorHandler("Getting value failed, XPath didn't evaluate succesfully: " + e.getMessage());
    	} 
        if (result == null) {
            return null;
        }
        resultStr = result.asString();
    	return resultStr;
    }

    public void setNodeText(Node n, String strValue) {
        //#debug
        System.out.println("XFormsModel.setNodeText");
    	setNodeTextInternal(n,strValue);
    	nodeValueChanged(n);
    	//CInstanceItem::GetInstanceItemL(n,this)->InstanceValueChanged(strValue); // so that the instance item updates or deletes its data object
    	InstanceItem.getInstanceItem(n, this).instanceValueChanged(strValue);
    }

    public String getNodeText(Node instanceNode) {
    	//TODO: check that proper value is returned as node text
        String text = null;
        if (instanceNode instanceof Attr) {
            text = ((Attr)instanceNode).getValue();
        } else if (instanceNode instanceof Element) {       
            Node child = instanceNode.getFirstChild();
            while(child != null) {
                if (child.getNodeType() == Node.TEXT_NODE) {
                    text = ((Text)child).getData();
                    break;
                }
                child = child.getNextSibling();
            }
        }
    	return text == null ? "" : text;
    }

    /** 
     * This commentary from the C++ implementation.
     * NOTE: It is important to use this method to create the expression 
     * instead of the XPathEvaluator, since this one sets the context 
     * correctly. Otherwise, e.g. cursor function will fail.
     * 
     * @return  an XPath expression.
     */
    public XPathExpression createExpression(String xPathString, XPathNSResolver resolver) {
        XPathEvaluator eval = getXPathEvaluator();
        XPathExpression expr = eval.createExpression(xPathString, resolver);
        // TODO?
        //expr.SetExtendedContext(this);
        return expr;
    }
    
	public void addInstance(String fileName, String id) {
		Document doc = loadXMLDocument(fileName);
        if (doc != null) {
            addInstance(doc, id);
        }
	}
	
	public Instance addInstance(Document doc, String id) {		
		Instance inst = new Instance(doc, id,this);
		instanceMap.put(inst.getID(), inst);
		instances.addElement(inst);
		xformsLibrary.addInstance(id, doc);
		return inst;
	}
 
    public void setRepeatIndex(String id, int index) {
        xformsLibrary.setIndex(id, index);
    }
    
    public boolean removeInstance(String id) {
        Instance inst = getInstance(id);
        if (inst != null)
        {
            instanceMap.remove(id);
            instances.removeElement(inst);
            xformsLibrary.removeInstance(id);
            return true;
        }
        return false;
    }
    
    public void addDataSource(DataSource source)
    {
    	if (this.externalDatasources==null) this.externalDatasources=new Vector();
    	this.externalDatasources.addElement(source);
    }
    public void addTimer(TimerElement source)
    {
    	if (this.externalDatasources==null) this.externalDatasources=new Vector();
    	this.externalDatasources.addElement(source);
    }
    
    public void stop()
    {
    	this.closed=true;
    	if (this.externalDatasources!=null) 
    	{
    		Enumeration sources = this.externalDatasources.elements();
    		while (sources.hasMoreElements())
    		{
    			Object o =sources.nextElement();
    			if (o instanceof DataSource)
    			{
	    			DataSource s = (DataSource)o;
	    			s.pause();
	    			s.close();
    			}
    			else if (o instanceof TimerElement)
    			{
    				TimerElement t = (TimerElement)o;
    				t.stopTimer();
    			}
    		}
    	}
    	
    	
    }
    
    public boolean isClosed()
    {
    	return this.closed;
    }
    
    public void addBind(Bind bind) {
        binds.addElement(bind);
    }
	
	public void addUI(XFormsModelUI ui) {
		uis.addElement(ui);
	}
	
    public XPathEvaluator getXPathEvaluator() {
        return xpev;
    }
    
	public InstanceItem getInstanceItemForNode(Node node) {
		return InstanceItem.getInstanceItem(node, this);
	}

	public Instance getInstance(String instID) {
		Instance inst = null;
		if (instID != null) {
			// In C++ iteration goes over the instances in a "for" 
			// loop, but we might as well use the hashtable(?)
			inst = (Instance) instanceMap.get(instID);
		} else if (instances.size() > 0) {
		    inst = (Instance) instances.elementAt(0);
        }
		return inst;
	}

    public Bind getBind(String bindId) {
        Enumeration bindEnum = binds.elements();
        Bind b;
        while (bindEnum.hasMoreElements()) {
            b = (Bind)bindEnum.nextElement();
            if (b.getID().equals(bindId)) {
                return b;
            }
        }
        return null;
    }

    public void instanceStructureChanged(int reason, Instance instance, Node start)
    {
        // TODO: move the code for resetting the MDG and UIBindings here, so that reset, replace="instance", 
        // insert and delete all come here with different reasons
        int uiCount = uis.size();
        for (int i=0; i<uiCount; i++)
        {
            XFormsModelUI ui = (XFormsModelUI) uis.elementAt(i);
            ui.modelEvent(reason, true, "", start, instance);
        }
        this.modelElement.dispatchLocalEvent(DOMEvent.XFORMS_STRUCTURE_CHANGED);
        
    }
    
    public void beforeInstanceStructureChange(int reason, Instance instance, Node start)
    {
        if (reason == SUBMISSION)
        {
            int uiCount = uis.size();
            for (int i=0;i<uiCount;i++)
            {
                XFormsModelUI ui = (XFormsModelUI) uis.elementAt(i);
                ui.resetAllUIBindings();
            }
        }
    }    
    

    public void replaceInstance(Document doc, String id)
    {
        removeInstance(id);
        addInstance(doc, id);
        // TODO: optimize?
        int bindCount = binds.size();
        for (int i=0; i < bindCount; i++)
        {
            Bind bind = (Bind)binds.elementAt(i);
            bind.reset(doc.getDocumentElement());
            // TODO: if Bind and BindElement were combined, you could use real context
        }
        changedNodes.removeAllElements(); // the change list must be cleared, since it has old nodes
    }
    
    
	public Element getDefaultContextNode() {
		if (instances.size() < 1) {
            //TODO what should be returned if default context is asked
            //when there are no instances? 
			//return new Element();
		}
		Instance defaultInstance = (Instance) instances.elementAt(0);
		Document doc = defaultInstance.getDocument();
		// LOG_RETURN("Document element for default instance");
		// return doc.DocumentElement();
		return doc.getDocumentElement();
	}
	
    /**
     * Gets the instance that was added first to the model.
     * 
     * @return the first instance of the model
     * 
     */ 
    public Instance getDefaultInstance() {
        if (instances.size() > 0) {
            return (Instance) instances.elementAt(0);
        } else {
            return null;
        }
        
    }
    
    public void addBindToMDG(Bind aBind, MDG aMDG) {
        boolean ok = aBind.executeNodeSet();
        NodeSet nodeset = aBind.getNodeSet();
       
        if (ok && nodeset.getLength() > 0) {
            int length = nodeset.getLength();

            XPathExpression calcexpr = aBind.getCompiledMIPExpr(MIPExpr.CALCULATE);
            XPathExpression relevantexpr = aBind.getCompiledMIPExpr(MIPExpr.RELEVANT);
            XPathExpression requiredexpr = aBind.getCompiledMIPExpr(MIPExpr.REQUIRED);
            XPathExpression constraintexpr = aBind.getCompiledMIPExpr(MIPExpr.CONSTRAINT);
            XPathExpression readonlyexpr = aBind.getCompiledMIPExpr(MIPExpr.READONLY);
            for (int i=0; i < length; i++) {
                Node contextNode = nodeset.item(i);
                addExprToMDGL(calcexpr, aBind, aMDG, contextNode, MIPExpr.CALCULATE);
                addExprToMDGL(relevantexpr,aBind,aMDG,contextNode, MIPExpr.RELEVANT);
                addExprToMDGL(requiredexpr,aBind,aMDG,contextNode, MIPExpr.REQUIRED);
                addExprToMDGL(constraintexpr,aBind,aMDG,contextNode, MIPExpr.CONSTRAINT);
                addExprToMDGL(readonlyexpr,aBind,aMDG,contextNode, MIPExpr.READONLY);
                if (aBind.hasType()) {
                    InstanceItem item = InstanceItem.getInstanceItem(contextNode, this);
                    item.setBaseType(aBind.getDataType());
                }
            }
        }        
    }

    void addExprToMDGL(XPathExpression expr, Bind aBind, MDG aMDG, Node aContextNode, int aType)
    {
    	if (expr != null)
    	{
    		// evaluate the expression and get the dependents
    	    NodeSet dependents = new NodeSet();
    		XPathResult result = expr.evaluateWithDependencies(aContextNode, XPathResult.ANY, dependents);
    		// Add dependency from each dependent node to the context node
    		if (dependents.getLength() == 0)
    		{
    			// this is a expression which does not have any dependents
    			//void CMDG::CalculateNonDependentNodeL(TNode aNode, int aType, RXPathExpression* expr, MNamespaceResolver *aNSRes)
    			aMDG.calculateNonDependentNode(aContextNode, aType, expr, aBind.getNamespaceResolver());
    		}
    		int count = dependents.getLength();
    		for (int d = 0 ;d < count; d++)
    		{
    			Node dependent = dependents.item(d);
    			aMDG.addDependency(dependent, aContextNode, aType, expr, aBind.getNamespaceResolver());
    		}
    	}
    }   
    
    
    
    public InstanceItem getInstanceItem(String xpathStr, XPathContext context, XPathNSResolver resolver) {       
        InstanceItem item = null;
        XPathExpression xpathExpr = createExpression(xpathStr, resolver);
        XPathResult result = xpathExpr.evaluate(context, XPathResult.ANY);
        NodeSet resNodes = result.asNodeSet();

        if (resNodes.getLength() > 0) {
            Node n = resNodes.item(0);
            item = InstanceItem.getInstanceItem(n, this);
        } else {
            // XPath didn't evaluate to a nodeset -> XFormsComputeException
            throw new XFormsComputeException("Error retrieving instance item: XPath did not evaluate to a nodeset. "+xpathStr); 
        }
        
        return item;
    }

    public void rebuild() {
//      kill previous Master Dependency Graph (MDG)
        // TODO: switch back states (for instance, calculates/types that disappear
 //        iChangedNodes.Reset(); // the change list can be cleared, since everything will be recalculated
        changedNodes.removeAllElements();
 
        if (depGraph != null) 
        {
            undoPreviousMDG(); // this will reset MIPs and types set by the last MDG
        }
        // Existing MDG will be garbage collected
        depGraph = new MDG(this);
        // Go through binds and add all found dependencies to MDG
        Enumeration be = binds.elements();
        Bind bind;
        while (be.hasMoreElements()) {
            bind = (Bind)be.nextElement();
            addBindToMDG(bind, depGraph);
        }
        depGraph.recalculate(new Vector(), true);
        changedNodes.removeAllElements(); // since the recalculate will change node values          
    }

    public void recalculate()
    {
    	depGraph.recalculate(changedNodes, false);
    	changedNodes.removeAllElements();	
    }   
    
    /**
     * undo all MIP changes by the previous MDG by basically setting all MIPs back to default. This is called before rebuilding the new MDG, and relies on the cached nodesets in binds.
     */
    void undoPreviousMDG()
    {
    	int count = binds.size();
    	Bind curBind = null;
    	for (int i=0;i < count;i++)
    	{
    		curBind = (Bind) binds.elementAt(i);
    		if (curBind.isInitialized())
    		{
    			NodeSet aSet = curBind.getNodeSet();
    			int count2 = aSet.getLength();
    			for (int j = 0; j < count2; j++)
    			{
    				Node node = (Node) aSet.item(j);
    				InstanceItem item = getInstanceItemForNode(node);
    				item.resetState();
    			}
    		}
    	}
    }
    
    
	/** 
	 * Sets the node text without catching the change. Use with plenty of care, since e.g. 
	 * the calculation engine nor the validation engine will not get the change! 
	 */
	private void setNodeTextInternal(Node n, String strValue)
	{
		// Note: Attr is a subclass of Element, that is 
		// why order matters here.	
		if (n instanceof Attr) {
			Attr a = (Attr)n;
			a.setValue(strValue);
		} else if(n instanceof Element) {
			//TElement e = n.AsElement();
			Element e = (Element)n;
			
			// this comment from C++ (we do it as in C++, but could
            // improve as mentioned here to support parent elements 
            // with text values):
			// TODO: find the first text node and set that, or 
            // create it before any elements, if it is not found
//			int childCount = e.getChildCount();
//			for (int i = 0; i < childCount; i++) {
//				e.removeChild(i);
//			}
            Node firstChild = e.getFirstChild();
            if (firstChild != null) { // Optimize for the frequent case that there is already a text node
                if (firstChild.getNodeType() == Node.TEXT_NODE) {
                    ((Text)firstChild).setData(strValue);
                    return;
                } else {
                    e.removeChild(firstChild);
                }
            }

            Document doc = e.getOwnerDocument();
            Text newChild = doc.createTextNode(strValue);
            e.appendChild(newChild);            
            
			// e.AddTextL(strValue);
			// Xml.NO_NAMESPACE, e.getNamespace()?
			//Element newElement = e.createElement("", strValue);
			//e.addChild(Node.ELEMENT, newElement);			
			//e.addChild(Node.TEXT, strValue);
            

            
		}
	}

	private void nodeValueChanged(Node n) {
		changedNodes.addElement(n);		
	}

	class FunctionDepWrapper {
		int functionDeps;
	}
	
	XPathResult evaluateWithDependencies(XPathContext context, NodeSet dependents,
			XPathExpression aXPathExpression, IntWrapper wrapper)
	{
		if (context != null)
		{
			clearFunctionDependencies();
			setTrackingDependencies(true);
			XPathResult newResult = aXPathExpression.evaluateWithDependencies(context, XPathResult.ANY, dependents);
			wrapper.value = getFunctionDependencies();
			clearFunctionDependencies();
			setTrackingDependencies(false);
			return newResult;
		}
		else
		{
			return new XPathResult(null);
		}
	}	

	void setTrackingDependencies(boolean tracking)
	{
		isTrackingDependencies = tracking;
	}	
	
	int getFunctionDependencies()
	{
		return functionDependencies;
	}
	
	void clearFunctionDependencies()
	{
		functionDependencies = NO_DEP;
	}		
	
	private Document loadXMLDocument (String fileName) {
		
		Document doc = new Document();
		KXmlParser parser = new KXmlParser();
		InputStream is = this.getClass().getResourceAsStream(fileName);
		if (is == null) {
			errorHandler("File not found when trying to read XML file: " + fileName);
			return doc;
		}
		try {
			parser.setInput(new InputStreamReader(is));
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			logWarning(e);
		}
		try {
			doc.parse(parser);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logWarning(e);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			logWarning(e);
		}
		return doc;
	}

	private void logError(Throwable t)
	{
		errorHandler("Exception: "+t.toString());
	}
	private void logWarning(Throwable t)
	{
		errorHandler("Exception: "+t.toString());
	}
	// TODO error reporting
	private void errorHandler(String err) {
		System.err.println(err);
	}

	public void removeBinaryAttachment(InstanceItem item) {
		if (this.binaryAttachments!=null)
		this.binaryAttachments.remove(item);
	}

	/* this will stop any threads running in the system. It is important that this is called */
	public void destroy() {
		this.stop();
	}


}

