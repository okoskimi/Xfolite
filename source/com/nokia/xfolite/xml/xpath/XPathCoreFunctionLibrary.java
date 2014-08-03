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

import java.util.Vector;

import com.nokia.xfolite.xml.dom.Attr;
import com.nokia.xfolite.xml.dom.CharacterData;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;


public class XPathCoreFunctionLibrary implements XPathFunctionLibrary{

	private static final String[] XPATH_FUNCTIONS=
	{
		"boolean"			//0
		,"ceiling"			//1
		,"concat"			//2
		,"contains"			//3
		,"count"			//4
		,"false"			//5
		,"floor"			//6
		,"id"				//7
		,"lang"				//8
		,"last"				//9
		,"local-name"		//10
		,"name"				//11
		,"namespace-uri"	//12
		,"normalize-space"	//13
		,"not"				//14
		,"number"			//15
		,"position"			//16
		,"round"			//17
		,"starts-with"		//18
		,"string"			//19
		,"string-length"	//20
		,"substring"		//21
		,"substring-after"	//22
		,"substring-before" //23
		,"sum"				//24
		,"translate"		//25
		,"true"				//26
	};
	
	private static final byte[][] XPATH_FUNCTIONS_ARGUMENT_COUNT=
	{
		{1,1}					//	0		boolean(object)
		,{1,1}					//	1		ceiling(number)
		,{2,Byte.MAX_VALUE}		//	2		concat
		,{2,2}					//	3		contains
		,{1,1}					//	4		count
		,{0,0}					//	5		false
		,{1,1}					//	6		floor
		,{1,1}					//	7		id
		,{1,1}					//	8		lang
		,{0,0}					//	9	 	last
		,{1,1}					//	10	 	local-name
		,{1,1}					//	11	 	name
		,{1,1}					//	12	 	namespace-uri
		,{1,1}					//	13	 	normalize-space
		,{1,1}					//	14	 	not
		,{1,1}					//	15	 	number
		,{0,0}					//	16	 	position
		,{1,1}					//	17	 	round
		,{2,2}					//	18	 	starts-with
		,{1,1}					//	19	 	string
		,{1,1}					//	20	 	string-length
		,{2,3}					//	21	 	substring
		,{2,2}					//	22	 	substring-after
		,{2,2}					//	23	 	substring-before
		,{1,1}					//	24	 	sum
		,{3,3}					//	25	 	translate
		,{0,0}					//	26	 	True
	};	
	
	
	public int RecognizeFunction(ExpandedNameNode functionName, int arity)
	{
		int index = XPathLexer.BinarySearch(XPATH_FUNCTIONS,functionName.getLocalName());
		if(index>=0
			&& arity>=XPATH_FUNCTIONS_ARGUMENT_COUNT[index][0]
			&& arity<=XPATH_FUNCTIONS_ARGUMENT_COUNT[index][1]) //function found
		{
			return index;
		}
		return -1;	
	}
	
	public Object EvaluateIndexedFunction(int index, XPathContext focus, Vector args, Node originalContext) throws XPathException
	{
		// TODO: namespace uri is ignored here on purpose
		
		if(index>=XPATH_FUNCTIONS.length || index<0) 
			throw new XPathException(XPathException.TYPE_ERR,"index for Xpath core function out of bound");

		int args_count = args.size()-1;
		if(args_count < XPATH_FUNCTIONS_ARGUMENT_COUNT[index][0]
		   || args_count > XPATH_FUNCTIONS_ARGUMENT_COUNT[index][1]) 
			throw new XPathException(XPathException.TYPE_ERR,"illegal number of arguments for xpath function");
		
		switch (index) {
			case 0	:	//	boolean
				return boolean_function(args.elementAt(1));
			case 1	:	//	ceiling
				return ceiling_function(number_function(args.elementAt(1)));
			case 2	:	//	concat
				return concat_function(args);
			case 3	:	//	contains
				return new Boolean (string_function(args.elementAt(1)).indexOf(string_function(args.elementAt(2)))>=0);
			case 4	:	//	count
				return count_function(args.elementAt(1));
			case 5	:	//	FALSE
				return new Boolean(false);
			case 6	:	//	floor
				return floor_function(number_function(args.elementAt(1)));
			case 7	:	//	id
				throw new XPathException(XPathException.TYPE_ERR,"Unimplemented function: id()");
			case 8	:	//	lang
				return lang_function(focus.contextNode,string_function(args.elementAt(1)));
			case 9	:	//	last
				return new Double(focus.contextSize);
			case 10	:	//	local-name
				return localname_function(args.elementAt(1));
			case 11	:	//	name
				return name_function(args.elementAt(1));
			case 12	:	//	namespace-uri
					return namespaceuri_function(args.elementAt(1));
			case 13	:	//	normalize-space
					return normalizespace_function(string_function(args.elementAt(1)));
			case 14	:	//	not
				return new Boolean(! boolean_function(args.elementAt(1)).booleanValue());
			case 15	:	//	number
					return number_function(args.elementAt(1));
			case 16	:	//	position
				return new Double (focus.contextPosition);
			case 17	:	//	round
				return round_function(number_function(args.elementAt(1)));
			case 18	:	//	starts-with
				return new Boolean (string_function(args.elementAt(1)).startsWith(string_function(args.elementAt(2))));
			case 19	:	//	string
					return string_function(args.elementAt(1));
			case 20	:	//	string-length
				return  new Double (string_function(args.elementAt(1)).length());
			case 21	:	//	substring
				if (args.size()==3)
					return substring_function(string_function(args.elementAt(1)),
											number_function(args.elementAt(2)));
				else
					return substring_function(string_function(args.elementAt(1)),
								number_function(args.elementAt(2)),
								number_function(args.elementAt(3)));
			case 22	:	//	substring-after
				return substringafter_function(string_function(args.elementAt(1)),
											string_function(args.elementAt(2)));
			case 23	:	//	substring-before
				return substringbefore_function(string_function(args.elementAt(1)),
						string_function(args.elementAt(2)));
			case 24	:	//	sum
				if (args.elementAt(1) instanceof NodeSet) {
					return sum_function((NodeSet) args.elementAt(1)) ;
					
				} else throw new IllegalArgumentException("xpath function sum() expects nodeset as an argument");
			case 25	:	//	translate
				return translate_function(string_function(args.elementAt(1)),
								string_function(args.elementAt(2)),
								string_function(args.elementAt(3)));
			case 26	:	//	TRUE
				return new Boolean(true);
		}
		return null;
	}
	
	public static Boolean boolean_function(Object obj)
	{
		boolean result = false;
		if (obj instanceof Boolean) {
			return (Boolean) obj;
		}else if (obj instanceof NodeSet) {
			result = ((NodeSet) obj).getLength()>0;
		}else if (obj instanceof String) {
			result = ((String) obj).length()>0;
		}else if (obj instanceof Double) {
			Double D = (Double) obj;
			result = !D.isNaN()&& D.doubleValue()!=0;
		}else return boolean_function(string_function(obj));
		return new Boolean(result);
	}
	
	public static Boolean not_function(Boolean arg)
	{
		return new Boolean(!arg.booleanValue());
	}
	
	public static Boolean true_function()
	{
		return new Boolean(true);
	}
	
	public static Boolean false_function()
	{
		return new Boolean(false);
	}
	
	public static Double number_function(Object obj)
	{
		try
		{
			if (obj instanceof Double) {
				return (Double) obj;
			}else if (obj instanceof String) {
				return new Double(Double.parseDouble((String) obj)) ;	
			}else if (obj instanceof Boolean) {
				return new Double(((Boolean) obj).booleanValue()?1:0);	
			}else return number_function(string_function(obj));
		} catch (NumberFormatException ex) 
		{ 
			return new Double(Double.NaN);
		}
	}
	
	public static String string_function(Object obj)
	{
		if (obj instanceof String) {
			return (String) obj;
		}else if (obj instanceof NodeSet){
			NodeSet ns = (NodeSet) obj;
			if(ns.getLength()>0) {
				return string_function(ns.item(0));
			} else {
				return "";
			}
		}
		else if (obj instanceof Node)
		{
			return string_value((Node) obj);
		}else if (obj instanceof Double) {
            double d = ((Double) obj).doubleValue();
            if (Double.isNaN(d)) {
                return "NaN";
            } else if (Double.isInfinite(d)) {
                if (d == Double.NEGATIVE_INFINITY) {
                    return "-Infinity";
                } else {
                    return "Infinity";
                }
            } else if (Math.floor(d) == d) {
                return String.valueOf((int)d);
            } else {
                return String.valueOf(d);
            }
        } else {
			return obj.toString(); // for boolean
        }
	}
	
	public static Double count_function(NodeSet ns)
	{
		return new Double(ns.getLength());
	}
	
	public static Double ceiling_function(Double number)
	{
		if(number.isNaN() || number.isInfinite()) {
			return number;
		}
		int i = number.intValue();
		return new Double(i<number.doubleValue()?i+1:i);
	}
	
	public static Double floor_function(Double number)
	{
		if(number.isNaN() || number.isInfinite()) {
			return number;
		}
		int i = number.intValue();
		return new Double(i>number.doubleValue()?i-1:i);
	}
	
	private static String concat_function(Vector objects)
	{
		int objects_count = objects.size();
		StringBuffer result = new StringBuffer();
		// i begins from 1 as the first one is the function name
		for (int i = 1; i < objects_count; i++) {
			result.append(string_function(objects.elementAt(i)));
		}
		return result.toString();
	}
	
	public static Double count_function(Object  obj)
	{
		int result = 0;
		if (obj instanceof NodeSet) {
			result = ((NodeSet) obj).getLength();
		}
		return new Double(result);
	}
	
	public static String localname_function(Object obj) throws IllegalArgumentException
	{
		if (obj instanceof NodeSet) {
			NodeSet ns = (NodeSet) obj;
			String result = null;
			if (ns.getLength()>0)
			{
				result = ns.item(0).getLocalName();
			}
			return result==null?"":result;	
		}
		else throw new IllegalArgumentException("function local-name() expects argument of type node-set");
	}
	
	public static String name_function(Object obj) throws IllegalArgumentException
	{
		if (obj instanceof NodeSet) {
			NodeSet ns = (NodeSet) obj;
			String result = null;
			if (ns.getLength()>0)
			{
				result = ns.item(0).getNodeName();
			}
			return result==null?"":result;	
		}
		else throw new IllegalArgumentException("function local-name() expects argument of type node-set");
	}
	
	
	public static String namespaceuri_function(Object obj) throws IllegalArgumentException
	{
		if (obj instanceof NodeSet) {
			NodeSet ns = (NodeSet) obj;
			String result = null;
			if (ns.getLength()>0)
			{
				result = ns.item(0).getNamespaceURI();
			}
			return result==null?"":result;		
		}
		else throw new IllegalArgumentException("function namespace-uri() expects argument of type node-set");
	}
	
	public static String normalizespace_function(String s)
	{
		int str_length = s.length();
		StringBuffer result= new StringBuffer(str_length);
		boolean inspaces=false;
		
		for (int i = 0; i < str_length; i++) {
			if(XPathLexer.isWhiteSpace(s.charAt(i)))
			{
				inspaces=true;
			}
			else
			{	
				if (inspaces)
				{
					inspaces=false;
					if(result.length()>0) result.append(' ');
				}
				result.append(s.charAt(i));
			}
		}
		return result.toString();
		
	}
	
	

	
	public static Double round_function(Double number)
	{
		if(number.isNaN()
				||number.isInfinite())return number;
		int i = number.intValue();
		double d = number.doubleValue();
		return new Double(i<d?d-i>=0.5?i+1:i:i-d>0.5?i-1:i);
	}
	
	
	public static String substring_function(String s, Double position)
	{
		if (position.isNaN()) return "";
		int arg1 = round_function(position).intValue();
		if (arg1>s.length()) return "";
		if (arg1!=Integer.MIN_VALUE) arg1--; //- infinity case
		if (arg1<0) arg1=0;
		return s.substring(arg1);
	}
	
	public static String substring_function(String s, Double position, Double length)
	{
		if (position.isNaN()
			||length.isNaN()
			||position.isInfinite()) return "";
		
		int arg1 = round_function(position).intValue()-1;
		if (arg1>=s.length()) return "";
		int arg2 = arg1 + round_function(length).intValue();
		if (arg2<=0) return "";
		if (arg1<0) arg1=0;
		if (arg2>s.length()) arg2 = s.length();
		return s.substring(arg1,arg2);
	}
	
	public static String substringbefore_function(String s1, String s2)
	{
		int index = s1.indexOf(s2);
		if (index>=0)
		{
			return s1.substring(0,index);
		}
		else return "";
	}
	
	public static String substringafter_function(String s1, String s2)
	{
		int index = s1.indexOf(s2);
		if (index>=0)
		{
			return s1.substring(index + s2.length());
		}
		else return "";
	}
	
	public static Double sum_function(NodeSet ns)
	{
		double sum =0;
		int ns_length = ns.getLength();
		for (int i = 0; i < ns_length; i++) {
			sum += number_function(string_function(ns.item(i))).doubleValue();
		}
		return new Double (sum);
	}
	
	public static String translate_function(String s, String t1, String t2)
	{
		int s_length = s.length();
		StringBuffer result = new StringBuffer(s.length());
		for (int i = 0; i < s_length; i++) {
			char c = s.charAt(i);
			int pos1 = t1.indexOf(c);
			if (pos1>=0)
			{
				if (pos1<t2.length())
					result.append(t2.charAt(pos1));
			}
			else result.append(c);
		}
		return result.toString();
		
	}
	
	public static NodeSet id_function(String s, Element rootElement)
	{
		NodeSet result = XPathEvaluator.EMPTY_NODESET;
		
		String instance_id = rootElement.getAttributeNS("","id");
		if (instance_id!=null
			&& instance_id.equals(s))
		{
			return new NodeSet(rootElement);
		}
		
		//if not,check the children and return the first one that works
		int children_count = rootElement.getChildCount();
		for (int i=0;i<children_count;i++)
		{
			Node childnode = rootElement.getChild(i);
			if (childnode instanceof Element)
			{
				NodeSet childResult = id_function(s,(Element) childnode);
				if (childResult != XPathEvaluator.EMPTY_NODESET)
					return childResult;
			}
		}
		
		return result;
	}
	
	public static Boolean lang_function(Node n,String lang)
	{
		boolean result =false;
		Node curr_node = n;
		String lang_attr = null;
		
		search:
		while (curr_node != null)
		{
			byte ntype = curr_node.getNodeType();
			if (ntype==Node.ELEMENT_NODE)
			{
				Element e = (Element) curr_node;
				String s = e.getAttributeNS("http://www.w3.org/XML/1998/namespace","lang");
				if (!s.equals(""))
				{
					lang_attr = s;
					break search;
				}
					
			}
			
			curr_node = ntype==Node.ATTRIBUTE_NODE?
						((Attr) curr_node).getOwnerElement():
						curr_node.getParentNode();
		}
		
		if (lang_attr!=null)
		{
			String lang_attr_lc = lang_attr.toLowerCase();
			String lang_lc = lang.toLowerCase();
			
			if(lang_attr_lc.equals(lang_lc))
	        {
				result = true;
	        }
			else
			{
				int lang_length = lang_lc.length();
				result =   lang_attr_lc.length() > lang_length
						&& lang_attr_lc.charAt(lang_length) == '-'
						&& lang_attr_lc.substring(0, lang_length).equals(lang_lc);

			}
		}
		
		return new Boolean(result);
	}
	
	public static String string_value(Node n)
	{
	    return n.getText();
        /*
        byte nodetype= n.getNodeType();
		String result=null;
		switch(nodetype)
		{
			case Node.ELEMENT_NODE:
			case Node.DOCUMENT_NODE:
			{
				StringBuffer sb = null;
				int child_count = n.getChildCount();
				for (int i = 0; i < child_count; i++)
				{	
					Node curr_child = n.getChild(i);
					byte child_nodetype = curr_child.getNodeType();
					if (child_nodetype==Node.TEXT_NODE
					 || child_nodetype==Node.CDATA_SECTION_NODE)
					{
						String child_stringvalue = string_value(curr_child);
						if (child_stringvalue.length()>0)
						{
							if(sb==null)
								sb = new StringBuffer(child_stringvalue.length());
							sb.append(child_stringvalue);
						}
					}
				}
				
				if (sb!=null)
					result = sb.toString();
			}
			break;
			
			case Node.ATTRIBUTE_NODE:
			{
				Attr a = (Attr) n;
				result = a.getValue();
			}
			break;
			
			case Node.TEXT_NODE:
			case Node.CDATA_SECTION_NODE:
			case Node.COMMENT_NODE:
			{
				CharacterData cd= (CharacterData) n;
				result = cd.getData();
			}
			break;
		}
		
		return result==null?"":result;
      */
	}

}
