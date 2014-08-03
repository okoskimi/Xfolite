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

package com.nokia.xfolite.xforms.xpath;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Calendar;
import javax.microedition.midlet.MIDlet;

import com.nokia.xfolite.xforms.dom.UserInterface;
import com.nokia.xfolite.xforms.model.datatypes.DataTypeDate;
import com.nokia.xfolite.xforms.model.datatypes.DataTypeDuration;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.xpath.ExpandedNameNode;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.nokia.xfolite.xml.xpath.XPathContext;
import com.nokia.xfolite.xml.xpath.XPathCoreFunctionLibrary;
import com.nokia.xfolite.xml.xpath.XPathException;
import com.nokia.xfolite.xml.xpath.XPathFunctionLibrary;
import com.nokia.xfolite.xml.xpath.XPathLexer;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.*;
import org.bouncycastle.util.encoders.*;
import java.io.*;

public class XFormsCoreFunctionLibrary implements XPathFunctionLibrary {

	private static final String[] XFORMS_CORE_FUNCTIONS=
	{
		"avg"						//0
		,"boolean-from-string"		//1
		,"count-non-empty"			//2
		,"current"					//3
		,"days-from-date"			//4
		,"digest"					//5
		,"exists"					//6  - ORBEON EXTENSION
		,"if"						//7
		,"index"					//8
		,"instance"					//9
		,"max"						//10
		,"min"						//11
		,"month-name"				//12 - NOKIA EXTENSION
		,"months"					//13
		,"now"						//14
		,"property"					//15
		,"seconds"					//16
		,"seconds-from-dateTime"	//17
		,"weekday-name"				//18 - NOKIA EXTENSION
	};

	private static final byte[][] XFORMS_FUNCTIONS_ARGUMENT_COUNT=
	{
		{	1	,	1	}	//0		avg
		,{	1	,	1	}	//1		boolean-from-string
		,{	1	,	1	}	//2		count-non-empty
		,{	0	,	0	}	//3		current
		,{	1	,	1	}	//4		days-from-date
		,{	2	,	3	}	//4		digest
		,{	1	,	1	}	//5	    exists
		,{	3	,	3	}	//6		if
		,{	1	,	1	}	//7		index
		,{	1	,	1	}	//8		instance
		,{	1	,	1	}	//9		max
		,{	1	,	1	}	//10	min
		,{	2	,	2	}	//11	month-name
		,{	1	,	1	}	//12	months
		,{	0	,	0	}	//13	now
		,{	1	,	1	}	//14	property
		,{	1	,	1	}	//15	seconds
		,{	1	,	1	}	//16	seconds-from-dateTime
		,{	2	,	2	}	//17	weekday-name
		
	};
	
	private Hashtable m_instanceMap = null;
	private Hashtable m_indexMap = null;
    private UserInterface  m_ui = null;
	
    public void setUI(UserInterface ui) {
        m_ui = ui;
    }
    
	public void setInstanceMap(Hashtable instanceMap)
	{
		m_instanceMap = instanceMap;
	}
	
	public void addInstance(String instanceId, Document instanceDoc)
	{
		if (m_instanceMap==null)
			m_instanceMap = new Hashtable();
		m_instanceMap.put(instanceId,instanceDoc);
	}
	
	public void removeInstance(String instanceId)
	{
		if (m_instanceMap!=null)
			m_instanceMap.remove(instanceId);
	}
	
	public int RecognizeFunction(ExpandedNameNode functionName, int arity) {
		//TODO: namespace uri is ignored here on purpose
		int index = XPathLexer.BinarySearch(XFORMS_CORE_FUNCTIONS,functionName.getLocalName());
		if(index>=0
			&& arity>=XFORMS_FUNCTIONS_ARGUMENT_COUNT[index][0]
			&& arity<=XFORMS_FUNCTIONS_ARGUMENT_COUNT[index][1]) //function found
		{
			return index;
		}
		return -1;
	}

	public Object EvaluateIndexedFunction(int index, XPathContext focus,
			Vector args, Node originalContext) throws XPathException {
		
		if(index>=XFORMS_CORE_FUNCTIONS.length || index<0) 
			throw new XPathException(XPathException.TYPE_ERR,"index for Xforms core function out of bound");

		int args_count = args.size()-1;
		if(args_count < XFORMS_FUNCTIONS_ARGUMENT_COUNT[index][0]
		   || args_count > XFORMS_FUNCTIONS_ARGUMENT_COUNT[index][1]) 
			throw new XPathException(XPathException.TYPE_ERR,"illegal number of arguments for xforms function");
		
		switch (index) {
		case 0:  //	avg
			if (args.elementAt(1) instanceof NodeSet) {
				return avg_function((NodeSet) args.elementAt(1)) ;		
			} else throw new XPathException(XPathException.TYPE_ERR,"xforms function avg() expects nodeset as an argument");
		case 1:  //	boolean-from-string
			return booleanfromstring_function(XPathCoreFunctionLibrary.string_function(args.elementAt(1)));
		case 2:  //	count-non-empty
			if (args.elementAt(1) instanceof NodeSet) {
				return countnonempty_function((NodeSet) args.elementAt(1)) ;		
			} else throw new XPathException(XPathException.TYPE_ERR,"xforms function count-non-empty() expects nodeset as an argument");
		
		case 3:  //	current
			return new NodeSet(originalContext);
		case 4:  //	days-from-date
			return daysfromdate_function(XPathCoreFunctionLibrary.string_function(args.elementAt(1)));
		case 5: // digest
			String encoding = "base64";
			if (args_count == 3) {
				encoding = XPathCoreFunctionLibrary.string_function(args.elementAt(3));
			}
			return digest_function(XPathCoreFunctionLibrary.string_function(args.elementAt(1)),
						XPathCoreFunctionLibrary.string_function(args.elementAt(2)), encoding);
			
		case 6:  // exists
			if (args.elementAt(1) instanceof NodeSet) {
				return exists_function((NodeSet) args.elementAt(1)) ;		
			} else throw new XPathException(XPathException.TYPE_ERR,"xforms function exists() expects nodeset as an argument");
		case 7:  //	if
			return if_function(
					XPathCoreFunctionLibrary.boolean_function(args.elementAt(1)),
					XPathCoreFunctionLibrary.string_function(args.elementAt(2)),
					XPathCoreFunctionLibrary.string_function(args.elementAt(3)));
		case 8:  //	index
			return index_function(XPathCoreFunctionLibrary.string_function(args.elementAt(1)));
		case 9:  //	instance
			return instance_function(
					XPathCoreFunctionLibrary.string_function(args.elementAt(1)));
		case 10:  //	max
			if (args.elementAt(1) instanceof NodeSet) {
				return max_function((NodeSet) args.elementAt(1)) ;		
			} else throw new XPathException(XPathException.TYPE_ERR,"xforms function max() expects nodeset as an argument");

		case 11:  //	min
			if (args.elementAt(1) instanceof NodeSet) {
				return min_function((NodeSet) args.elementAt(1)) ;		
			} else throw new XPathException(XPathException.TYPE_ERR,"xforms function min() expects nodeset as an argument");

		case 12: //	month-name
			if (args.elementAt(2) instanceof NodeSet) {
				return month_name_function(XPathCoreFunctionLibrary.string_function(args.elementAt(1)), (NodeSet) args.elementAt(2));		
			} else throw new XPathException(XPathException.TYPE_ERR,"xforms function month-name() expects nodeset as a second argument");
			
			

		case 13: //	months
			return months_function(XPathCoreFunctionLibrary.string_function(args.elementAt(1)));
			
		case 14: //	now
			return now_function();
		case 15: //	property
			return property_function(XPathCoreFunctionLibrary.string_function(args.elementAt(1)), m_ui);
		case 16: //	seconds
			return seconds_function(XPathCoreFunctionLibrary.string_function(args.elementAt(1)));
		case 17: //	seconds-from-dateTime
			return secondsfromdateTime_function(XPathCoreFunctionLibrary.string_function(args.elementAt(1)));
		case 18: //	weekday-name
			if (args.elementAt(2) instanceof NodeSet) {
				return weekday_name_function(XPathCoreFunctionLibrary.string_function(args.elementAt(1)), (NodeSet) args.elementAt(2));		
			} else throw new XPathException(XPathException.TYPE_ERR,"xforms function weekday-name() expects nodeset as a second argument");
		}
		
		return null;
	}
	
	public static Double avg_function(NodeSet ns)
	{
		double sum = XPathCoreFunctionLibrary.sum_function(ns).doubleValue();
		double count = ns.getLength();
		return new Double (sum / count);
	}
	
	public static Boolean booleanfromstring_function(String s)
	{
		String lc_string = s.toLowerCase();
		boolean result = false;
		if (lc_string.equals("1")
				|| lc_string.equals("true"))
			result =true;
		return new Boolean(result);
			
	}
	
	public static Double countnonempty_function(NodeSet ns)
	{
		int ns_count = ns.getLength();
		int count = 0;
		for (int i = 0; i < ns_count; i++) {
			if (XPathCoreFunctionLibrary.string_function(ns.item(i)).length()>0)
				count++;
		}
		return new Double(count);
	}

	public static Boolean exists_function(NodeSet ns)
	{
		return (ns.getLength() > 0) ? Boolean.TRUE : Boolean.FALSE;
	}
	
	public static Double daysfromdate_function(String s)
	{
        // TODO: Check that this returns right value
        // It should work fine when you want to get the day difference between two dates, but might be wrong 
        // if you want to look at the value for one date.
        Calendar cal = DataTypeDate.xsdDateTime2Calendar(s);
        return new Double (cal.getTime().getTime() / 1000 / 3600 / 24);
	}

	
	public static String digest_function(String s, String method, String encoding) {
		//#debug info
		System.out.println("Digest function invoked with params(" + s + "," + method + "," + encoding + ")");
		Digest digest = null;
		Encoder encoder = null;
		if (encoding.equals("base64")) {
			encoder = new Base64Encoder();
		} else if (encoding.equals("hex")) {
			encoder = new HexEncoder();
		}
		if (method.equals("SHA-1")) {
			digest = new SHA1Digest();
		} else if (method.equals("SHA-256")) {
			digest = new SHA256Digest();
		} else if (method.equals("SHA-512")) {
			digest = new SHA512Digest();
		} else if (method.equals("MD5")) {
			digest = new MD5Digest();
		}
		
		
		if (encoder == null) {
			throw new XPathException(XPathException.TYPE_ERR,"XForms function digest() only supports hex and base64 encoding.");
		}
		if (digest == null) {
			throw new XPathException(XPathException.TYPE_ERR,"XForms function digest() only supports MD5, SHA-1, SHA-256 and SHA-512 digests.");
		}

		int len = s.length();
		for(int i=0; i < len; i++) {
			digest.update((byte) s.charAt(i)); // FIXME: Better not use non-ASCII characters!
		}
		
		byte[] data = new byte[digest.getDigestSize()];
		digest.doFinal(data, 0);

		
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();        
        try
        {
            encoder.encode(data, 0, data.length, bOut);
        }
        catch (IOException e)
        {
            throw new XPathException(XPathException.TYPE_ERR, "Exception when encoding digest: " + e);
        }
        
        byte[] out = bOut.toByteArray();	
		StringBuffer sb = new StringBuffer();
		for(int i=0; i < out.length; i++) {
			sb.append((char) out[i]); // This works fine, neither hex nor base64 encodings produce 
		}
		return sb.toString();
	}
	


	
	
	public static String if_function(Boolean TestCase,String Ifpart, String ElsePart)
	{
		if (TestCase.booleanValue())
			return Ifpart;
		else return ElsePart;
	}
	
	private Double index_function(String s)
	{
		if (m_indexMap == null) {
			return new Double(Double.NaN);
		}
		Double value = (Double) m_indexMap.get(s);
		if (value == null) {
			return new Double(Double.NaN);
		}
		return value;
	}
	
	/*
	 * Set the value returned by index function.
	 * Note that values <= 0 will result in a NaN value being stored.
	 */
	public void setIndex(String id, int value) {
		if (m_indexMap == null) {
			m_indexMap = new Hashtable();
		}
		if (value > 0) {
			m_indexMap.put(id, new Double(value));
		} else {
			m_indexMap.put(id, new Double(Double.NaN));
		}
	}

	
	public static Double max_function(NodeSet ns)
	{
		double max =Double.NaN;
		int ns_length = ns.getLength();
		for (int i = 0; i < ns_length; i++) {
			Double D= XPathCoreFunctionLibrary.number_function(
							XPathCoreFunctionLibrary.string_function(
									ns.item(i)));
			if (D.isNaN()) return D;
			
			if (max==Double.NaN
					|| D.doubleValue()>max)
				max = D.doubleValue();
		}
		return new Double (max);
	}
	
	public static Double min_function(NodeSet ns)
	{
		double min =Double.NaN;
		int ns_length = ns.getLength();
		for (int i = 0; i < ns_length; i++) {
			Double D= XPathCoreFunctionLibrary.number_function(
							XPathCoreFunctionLibrary.string_function(
									ns.item(i)));
			if (D.isNaN()) return D;
			if (min==Double.NaN
					|| D.doubleValue()<min)
				min = D.doubleValue();
		}
		return new Double (min);
	}
	
	public static String month_name_function(String s, NodeSet ns)
	{
		//#debug info
		System.out.println("****************** Invoking month_name_function with parameter <" + s + ">");

		try {
			String[] months = new String[DataTypeDate.MONTH_NAMES.length];
			int len = ns.getLength();
			for(int i=0; i < len; i++) {
				months[i] = XPathCoreFunctionLibrary.string_function(ns.item(i));
			}
			for(int i=len; i<DataTypeDate.MONTH_NAMES.length;i++) {
				months[i] = DataTypeDate.MONTH_NAMES[i];
			}	

			Calendar cal = DataTypeDate.xsdDate2Calendar(s);
			return DataTypeDate.getMonthName(cal, months);
		} catch (Exception ex) {
			return "";
		}
	}
	
	public static Double months_function(String s)
	{
		return new Double(DataTypeDuration.xsdDuration2seconds(s) / (30*24*60*60));
	}
	
	
	
	public static String now_function()
	{
        Calendar cal = Calendar.getInstance();
		return DataTypeDate.calendar2xsdDateTime(cal);
	}
	
	public static String property_function(String s, UserInterface ui)
	{
        String property = null;
		if (s.equals("version"))
			property = "1.0";
		else if (s.equals("conformance-level"))
			property = "basic";
		else if (ui != null) {
            property = ui.getProperty(s);
            //#debug info
            System.out.println("Got property for " + s + ": " + property);
        }
        return property == null ? "" : property;
	}
	
	public static Double seconds_function(String s)
	{
        return new Double(DataTypeDuration.xsdDuration2seconds(s));
	}
	
	
	
	public static Double secondsfromdateTime_function(String s)
	{
        Calendar cal = DataTypeDate.xsdDateTime2Calendar(s);
        return new Double (cal.getTime().getTime()/1000);
	}
	
	public NodeSet instance_function(String id)
	{
		if (m_instanceMap!=null
			&& m_instanceMap.containsKey(id))
		{
			Element rootelement= ((Document) m_instanceMap.get(id)).getDocumentElement();
			if (rootelement!=null)
				return new NodeSet(rootelement);
		}
		return new NodeSet();
	}

	public static String weekday_name_function(String s, NodeSet ns)
	{
		try {
			String[] weekdays = new String[DataTypeDate.WEEKDAY_NAMES.length];
			int len = ns.getLength();
			for(int i=0; i < len; i++) {
				weekdays[i] = XPathCoreFunctionLibrary.string_function(ns.item(i));
			}
			for(int i=len; i<DataTypeDate.WEEKDAY_NAMES.length;i++) {
				weekdays[i] = DataTypeDate.WEEKDAY_NAMES[i];
			}
			
			Calendar cal = DataTypeDate.xsdDate2Calendar(s);
			return DataTypeDate.getWeekDayName(cal, weekdays);
		} catch (Exception ex) {
			return "";
		}
	}
}
