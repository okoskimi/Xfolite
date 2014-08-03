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


import com.nokia.xfolite.xml.dom.DOMException;
import com.nokia.xfolite.xml.dom.Node;

public class XPathExpression {
	
    
	ParseNode m_expression_tree;
	XPathEvaluator m_evaluator;
	
	protected XPathExpression(XPathEvaluator Evaluator, ParseNode ExpressionTree)
	{
		m_evaluator = Evaluator;
		m_expression_tree = ExpressionTree;
	}
	
	/**
     * Evaluates this XPath expression and returns a result.
     * @param contextNode The <code>context</code> is context node for the 
     *   evaluation of this XPath expression.If the XPathEvaluator was 
     *   obtained by casting the <code>Document</code> then this must be 
     *   owned by the same document and must be a <code>Document</code>, 
     *   <code>Element</code>, <code>Attribute</code>, <code>Text</code>, 
     *   <code>CDATASection</code>, <code>Comment</code>, 
     *   <code>ProcessingInstruction</code>, or <code>XPathNamespace</code> 
     *   node.If the context node is a <code>Text</code> or a 
     *   <code>CDATASection</code>, then the context is interpreted as the 
     *   whole logical text node as seen by XPath, unless the node is empty 
     *   in which case it may not serve as the XPath context.
     * @param type If a specific <code>type</code> is specified, then the 
     *   result will be coerced to return the specified type relying on 
     *   XPath conversions and fail if the desired coercion is not possible. 
     *   This must be one of the type codes of <code>XPathResult</code>.
     * @return The result of the evaluation of the XPath expression.
     * @exception XPathException
     *   TYPE_ERR: Raised if the result cannot be converted to return the 
     *   specified type.
     * @exception DOMException
     *   WRONG_DOCUMENT_ERR: The Node is from a document that is not supported 
     *   by the XPathExpression that created this 
     *   <code>XPathExpression</code>.
     *   <br>NOT_SUPPORTED_ERR: The Node is not a type permitted as an XPath 
     *   context node.
     */
    public XPathResult evaluate(Node contextNode,
    							byte type)
                                throws XPathException, DOMException
    {
    	return new XPathResult (m_evaluator.SimpleEvaluate(m_expression_tree,
                new XPathContext(contextNode, 1, 1),type, null));
    }

    public XPathResult evaluate(XPathContext context, byte type)
            throws XPathException, DOMException
    {
        return new XPathResult(m_evaluator.SimpleEvaluate(m_expression_tree,
                new XPathContext(context), type, null));
    }
    
    
	/**
     * Evaluates this XPath expression and returns a result.
     * @param contextNode The <code>context</code> is context node for the 
     *   evaluation of this XPath expression.If the XPathEvaluator was 
     *   obtained by casting the <code>Document</code> then this must be 
     *   owned by the same document and must be a <code>Document</code>, 
     *   <code>Element</code>, <code>Attribute</code>, <code>Text</code>, 
     *   <code>CDATASection</code>, <code>Comment</code>, 
     *   <code>ProcessingInstruction</code>, or <code>XPathNamespace</code> 
     *   node.If the context node is a <code>Text</code> or a 
     *   <code>CDATASection</code>, then the context is interpreted as the 
     *   whole logical text node as seen by XPath, unless the node is empty 
     *   in which case it may not serve as the XPath context.
     * @param type If a specific <code>type</code> is specified, then the 
     *   result will be coerced to return the specified type relying on 
     *   XPath conversions and fail if the desired coercion is not possible. 
     *   This must be one of the type codes of <code>XPathResult</code>.
     * @param result The <code>result</code> specifies a specific 
     *   <code>XPathResult</code> which may be reused and returned by this 
     *   method. If this is specified as <code>null</code>or the 
     *   implementation cannot reuse the specified result, a new 
     *   <code>XPathResult</code> will be constructed and returned.
     * @return The result of the evaluation of the XPath expression.
     * @exception XPathException
     *   TYPE_ERR: Raised if the result cannot be converted to return the 
     *   specified type.
     * @exception DOMException
     *   WRONG_DOCUMENT_ERR: The Node is from a document that is not supported 
     *   by the XPathExpression that created this 
     *   <code>XPathExpression</code>.
     *   <br>NOT_SUPPORTED_ERR: The Node is not a type permitted as an XPath 
     *   context node.
     */    
    
    public XPathResult evaluate(Node contextNode,
    		byte type, XPathResult result)
    throws XPathException, DOMException
    {
    	if (result == null) {
    		return new XPathResult (m_evaluator.SimpleEvaluate(m_expression_tree,
                    new XPathContext(contextNode, 1 ,1),type, null));
    	} else {
    		result.setResult(m_evaluator.SimpleEvaluate(m_expression_tree,
                    new XPathContext(contextNode, 1, 1), type, null));
    		return result;
    	}    	
    }    
    
    public XPathResult evaluate(XPathContext context,
            byte type, XPathResult result)
    throws XPathException, DOMException
    {
        if (result == null) {
            return new XPathResult (m_evaluator.SimpleEvaluate(m_expression_tree,
                    new XPathContext(context),type, null));
        } else {
            result.setResult(m_evaluator.SimpleEvaluate(m_expression_tree,
                    new XPathContext(context),type, null));
            return result;
        }       
    }  
    
    
	/**
     * Evaluates this XPath expression and returns a result, collecting dependency information.
     * @param contextNode The <code>context</code> is context node for the 
     *   evaluation of this XPath expression.If the XPathEvaluator was 
     *   obtained by casting the <code>Document</code> then this must be 
     *   owned by the same document and must be a <code>Document</code>, 
     *   <code>Element</code>, <code>Attribute</code>, <code>Text</code>, 
     *   <code>CDATASection</code>, <code>Comment</code>, 
     *   <code>ProcessingInstruction</code>, or <code>XPathNamespace</code> 
     *   node.If the context node is a <code>Text</code> or a 
     *   <code>CDATASection</code>, then the context is interpreted as the 
     *   whole logical text node as seen by XPath, unless the node is empty 
     *   in which case it may not serve as the XPath context.
     * @param type If a specific <code>type</code> is specified, then the 
     *   result will be coerced to return the specified type relying on 
     *   XPath conversions and fail if the desired coercion is not possible. 
     *   This must be one of the type codes of <code>XPathResult</code>.
     * @param result The <code>result</code> specifies a specific 
     *   <code>XPathResult</code> which may be reused and returned by this 
     *   method. If this is specified as <code>null</code>or the 
     *   implementation cannot reuse the specified result, a new 
     *   <code>XPathResult</code> will be constructed and returned.
     * @return The result of the evaluation of the XPath expression.
     * @exception XPathException
     *   TYPE_ERR: Raised if the result cannot be converted to return the 
     *   specified type.
     * @exception DOMException
     *   WRONG_DOCUMENT_ERR: The Node is from a document that is not supported 
     *   by the XPathExpression that created this 
     *   <code>XPathExpression</code>.
     *   <br>NOT_SUPPORTED_ERR: The Node is not a type permitted as an XPath 
     *   context node.
     */
    
    public XPathResult evaluateWithDependencies(Node contextNode,
    											byte type,
    											NodeSet dependencies)
    											throws XPathException, DOMException
    {
    	return new XPathResult (m_evaluator.SimpleEvaluate(m_expression_tree,
                new XPathContext(contextNode, 1, 1),type,dependencies));
    }
    
    public XPathResult evaluateWithDependencies(XPathContext context,
            byte type, NodeSet dependencies) throws XPathException, DOMException
    {
        return new XPathResult(m_evaluator.SimpleEvaluate(m_expression_tree,
                new XPathContext(context), type, dependencies));
    }
    
}
