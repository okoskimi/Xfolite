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

import java.util.Enumeration;
import java.util.Vector;

//import org.kxml2.kdom.Element;
//import org.kxml2.kdom.Node;

import com.nokia.xfolite.xforms.model.datatypes.DataTypeBase;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.nokia.xfolite.xml.xpath.XPathExpression;
import com.nokia.xfolite.xml.xpath.XPathNSResolver;
import com.nokia.xfolite.xml.xpath.XPathResult;


/**
 * A class to contain XPath expressions related to bind elements.
 * 
 * @author mattisil
 *
 */
public class Bind {

    private String id;
    private XFormsModel model;
    private Node defaultContextNode;
    private boolean nodesetExecuted;
    private boolean hasType;
    private boolean hasP3PType;
    private boolean errorInNodeset;

    private XPathExpression nodeSetExpr;
    private NodeSet nodeSet;
    private XPathResult nodeSetResult;
    private boolean errorInNodeSet;
    private Vector mipExpressions;
    private String typeStr;
    private String p3pStr; 
    private DataTypeBase dataType;
    private XPathNSResolver nsRes;
    
    public Bind(XFormsModel model, XPathNSResolver resolver, Node defaultContextNode, String id) {
        if (id == null) {
            this.id = "";
        } else {
            this.id = id;
        }
        this.model = model;
        this.nsRes = resolver;
        this.defaultContextNode = defaultContextNode;
        nodesetExecuted = false;
        hasType=false;
        hasP3PType=false;
        
    }
    
    public XPathNSResolver getNamespaceResolver() {
        return nsRes;
    }

    public void setNodeSet(String xPathString) {
        //RXPathEvaluator eval = iModel->GetXPathEvaluator();
        //RXPathExpression xpathExpr = iModel->CreateExpressionL(expr,NULL); // TODO: nsresolver
        //iNodesetExpr = xpathExpr;
        nodeSetExpr = model.createExpression(xPathString, null);
    }
    
    public NodeSet getNodeSet() {
        // now this is like in C++, but this could also
        // execute when necessary
        return nodeSet;
    }
    
    public boolean executeNodeSet() {
        Node context = defaultContextNode;

        errorInNodeSet = false;
        nodesetExecuted = true;
        try {
            nodeSetResult = nodeSetExpr.evaluate(context, XPathResult.ANY);
        } catch (Exception e) {
            // TODO: error handling, e.g. a specific subclass of 
            // exception for erroneous XPath expressions?
            System.err.println(e.getMessage());
            errorInNodeSet = true;
        }
        if (nodeSetResult != null) {
            errorInNodeSet =  (nodeSetResult.getType() != XPathResult.NODESET);
            nodeSet = nodeSetResult.asNodeSet();
        }
        return !errorInNodeSet;
    }

    /**
     * Set an expression defining a model item property.
     * 
     * @param expr       an XPath expression
     * @param mipType    one of the MIP ids defined in MIPExpr.java
     * @return           true if runs to the end
     */
    public boolean setMIPExpr(String expr, int mipType) {
    	if (mipExpressions == null) {
    		mipExpressions = new Vector();
    	}
        // namespaceresolver is missing here in C++ as well at the moment
        XPathExpression xpathExpr = model.createExpression(expr, nsRes);
        MIPExpr mipExpr = new MIPExpr(xpathExpr, mipType);
        mipExpressions.addElement(mipExpr);
        return true;
    }
    
    public XPathExpression getCompiledMIPExpr(int mipType) {
    	if (mipExpressions == null) {
    		return null;
    	}
        Enumeration mipEnum = mipExpressions.elements();
        MIPExpr mipExpr;
        while (mipEnum.hasMoreElements()) {
            mipExpr = (MIPExpr)mipEnum.nextElement();
            if (mipExpr.type == mipType) {
                return mipExpr.xpathExpr;
            }      
        }
        return null;
    }

    public boolean setStaticMIP(String staticMIP, int mipType) {
        if (mipType == MIPExpr.TYPE) {
            hasType = true;
            typeStr = staticMIP;
            dataType = model.getDataTypeFactory().stringToDataType(staticMIP, nsRes);
            return true;
        } else if (mipType == MIPExpr.P3PTYPE) {
            hasP3PType = true;
            p3pStr = staticMIP;
            return true;
        }
        return false;
    }

    public boolean hasType() {
        return hasType;
    }
    
    public String getTypeString() {
        return typeStr;
    }
    
    public DataTypeBase getDataType() {
        return dataType;
    }
    
	public boolean isInitialized()
	{
		return nodesetExecuted;
	}
    
    public String getID() {
        return id;
    }
    
    /**
     * Retrieves the nodeset expression, note that this is used by UI binding 
     * when used with the bind attr.
     * 
     * @return  the nodeset expression
     */
    public XPathExpression getNodeSetExpr() {
      return nodeSetExpr;
    }

    public void reset(Node defaultContextNode)
    {
        this.defaultContextNode = defaultContextNode;
        nodeSetResult = null;
        nodesetExecuted = false;
    }
    

}
