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

package com.nokia.xfolite.xforms.model.datatypes;

public class DataTypeDuration extends DataTypeBase {

    public DataTypeDuration() {
        super(DataTypeBase.XML_SCHEMAS_DURATION);
    }
    
    public static double xsdDuration2seconds(String s)
    {
		int s_length;
		boolean is_positive = true;
    	if (s==null
				|| (s_length = s.length())<1
				|| (s.charAt(0)!='P' && (is_positive = s.charAt(0)!='-')))
			return 0;

    	boolean passed_t = false;
    	int i = is_positive?1:2;
    	double result = 0;
		while(i<s_length)
		{
			if (!passed_t && s.charAt(i)=='T')
				{
				passed_t = true;
				i++;
				}
			else
			{
				int marker = skipDigits(s,i);
				
				if (marker<s_length && s.charAt(marker)=='.')
				{
					marker++; //skip '.'
					marker = skipDigits(s,marker);
				}
				
				double d = Double.parseDouble(s.substring(i,marker));
				
				i = marker;
				
				if (i<s_length)
				{
					char curr_char = s.charAt(i);
					switch (curr_char)
					{
						case 'Y':
							d*=365*24*60*60;
							break;
						
						case 'M':
							if (passed_t)
								d *= 60;
							else
								d *= 30*24*60*60;
							break;
						
						case 'W':
							d *= 7*24*60*60;
							break;	
						case 'D':
							d *= 24*60*60;
							break;
						case 'H':
							d *= 60*60;
							break;
					}
					result+=d;
					i++;
				}
			}
		}
		
		if (is_positive)
			return result;
		else
			return -result;
    }
    
    private static int skipDigits(String s, int startIndex)
    {
    	int s_length = s.length();
    	if (startIndex<s_length)
    	{
    		char curr_char;
    		int marker = startIndex;
    		while (marker < s_length 
				&& (curr_char = s.charAt(marker)) >='0'
				&& curr_char <='9')
			{
				marker++;
			}
    		
    		return marker;
    	}
    	else
    		return startIndex;
    }
    
    
    protected String getBaseTypeName() 
    {
        return "duration";
    }
}
