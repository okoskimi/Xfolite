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

import java.util.*;

import com.nokia.xfolite.xforms.model.XFormsModelException;

public class DataTypeDate extends DataTypeBase {
    
    public DataTypeDate(int typeID) {
        super(typeID);
    }
    
    // TODO: getDisplayString
    public String getDisplayString(ValueProvider prov) {
        
        //Calendar cal = getCalendarValue(prov)
//        boolean valid = validate(prov);
//        if (valid) {
            return prov.getStringValue();
//        } else {
//            return formatDateTime(prov.getStringValue());
//        }
    }
    
    public void setCalendarValue(Calendar cal, ValueProvider prov) {
        String strValue;
        if (this.typeID == DataTypeBase.XML_SCHEMAS_DATE) {
           strValue = calendar2xsdDate(cal); 
        } else if (this.typeID == DataTypeBase.XML_SCHEMAS_DATETIME) {
            strValue = calendar2xsdDateTime(cal);
        } else if (typeID == DataTypeBase.XML_SCHEMAS_TIME) {
            strValue = calendar2xsdTime(cal);
        } else if (typeID == DataTypeBase.XML_SCHEMAS_GYEARMONTH) {
            strValue = calendar2xsdGYearMonth(cal);
        } else if (typeID == DataTypeBase.XML_SCHEMAS_GYEAR) {
            strValue = calendar2xsdGYear(cal);
        } else if (typeID == DataTypeBase.XML_SCHEMAS_GMONTHDAY) {
            strValue = calendar2xsdGMonthDay(cal); 
        } else if (typeID == DataTypeBase.XML_SCHEMAS_GDAY) {
            strValue = calendar2xsdGDay(cal);
        } else if (typeID == DataTypeBase.XML_SCHEMAS_GMONTH) {
            strValue = calendar2xsdGMonth(cal);
        }         
        else {
            strValue = calendar2xsdDate(cal);
        }
        prov.setStringValue(strValue);
    }        

    public static final String[] MONTH_NAMES = {
    	"January","February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
    };
    
    public static final String[] WEEKDAY_NAMES = {
    	"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };
    
    public static String calendar2displayTime(Calendar cal) {
    	return getHour(cal) + ":" + getMinute(cal) + ":" + getSecond(cal);
    }
    public static String calendar2displayTimeShort(Calendar cal) {
    	return getHour(cal) + ":" + getMinute(cal);
    }
    
    public static String calendar2displayDateShort(Calendar cal) {
    	return calendar2xsdDate(cal);
    }
    
    public static String calendar2displayDate(Calendar cal, String[] months, String[] weekdays) {
    	return getWeekDayName(cal, weekdays) + ", " + getMonthName(cal, months) + " " + getOrdered(cal.get(Calendar.DAY_OF_MONTH)) + " " + getYear(cal);
    }
    
    public static String getOrdered(int number) {
    	int last = number % 10;
    	switch (last) {
    	case 1:
    		return number + "st";
    	case 2:
    		return number + "nd";
    	case 3:
    		return number + "rd";
    	default:
    		return number + "th";
    	}
    }

    public static String calendar2displayDateTime(Calendar cal, String[] months, String[] weekdays) {
    	return calendar2displayDate(cal, months, weekdays) + ", " + calendar2displayTime(cal);
    }
    
    public static String calendar2displayDateTimeShort(Calendar cal) {
    	return calendar2displayDateShort(cal) + " " + calendar2displayTimeShort(cal);
    }
    
    public static String calendar2xsdGMonth(Calendar cal) {
        return "--" + getMonth(cal) + "--";
    }
    
    public static String calendar2xsdGDay(Calendar cal) {
        return "---" + getDay(cal);
    }
    
    public static String calendar2xsdGMonthDay(Calendar cal) {
        return "--" + getMonth(cal) + "-" + getDay(cal);
    }
    
    public static String calendar2xsdGYear(Calendar cal) {
        return getYear(cal);
    }
    
    public static String calendar2xsdGYearMonth(Calendar cal) {
        return getYear(cal) + "-" + getMonth(cal);
    }
    
    public static String calendar2xsdDateTime(Calendar cal) {
    	// Need to convert calendar to GMT before getting date so that also date is GMT-corrected.
    	// Cannot put that in calendar2xsdDate() since that would not be done for a pure date.
    	TimeZone oldTz = cal.getTimeZone();
        TimeZone gmtTz = TimeZone.getTimeZone("GMT");
        cal.setTimeZone(gmtTz);
        String datePart = calendar2xsdDate(cal);
        cal.setTimeZone(oldTz);        
        return datePart + "T" + calendar2xsdTime(cal);
    }

    public static String calendar2xsdDate(Calendar cal) {
        return getYear(cal) + "-" + getMonth(cal) + "-" + getDay(cal);
    }

    public static String calendar2xsdTime(Calendar cal) {
        TimeZone oldTz = cal.getTimeZone();
        TimeZone gmtTz = TimeZone.getTimeZone("GMT");
        cal.setTimeZone(gmtTz);
        String rval = getHour(cal) + ":" + getMinute(cal) + ":" + getSecond(cal)
        + "." + getMillisecond(cal)
        + "Z";
        cal.setTimeZone(oldTz);
        return rval;
    }
    
    
    public static Calendar xsdDateTime2Calendar(String stringVal) {
        // TODO: validate that stringVal is a legal datetime
        Calendar cal = Calendar.getInstance();
        setDateTime(cal, stringVal);
        return cal;
    }
    


    public static Calendar xsdDate2Calendar(String stringVal) {
        // TODO: validate that stringVal is a legal datetime
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(0));
        setDate(cal, stringVal);
        return cal;
    }
    
    public static Calendar xsdTime2Calendar(String stringVal) {
        // TODO: validate that stringVal is a legal datetime
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(0));
        setTime(cal, stringVal);
        return cal;
    }
    
    /*
    public static Calendar xsdDate2Calendar(String stringVal) {
        // TODO: validate that stringVal is a legal datetime
        Calendar cal = Calendar.getInstance();
        String dateStr = stringVal.substring(0, 10);
        setDate(cal, dateStr);
        return cal;
    }
    */    
    public Calendar getCalendarValue(ValueProvider prov) {
        
        // TODO: internalValidate?
        //xmlSchemaValPtr pVal = InternalValidateL(aProv,ret);
        
        // validation in our case is supposed to be implemented by 
        // subclasses, such as DateTime
        boolean valid = validate(prov);
        if (!valid) {
            throw new XFormsModelException("Illegal date value: " + prov.getStringValue());
            //return Calendar.getInstance();
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(0));
            // TODO: timezones (right now ignored, if they were attached)
            if (this.typeID == DataTypeBase.XML_SCHEMAS_DATE) {
                // format: CCYY-MM-DD
            	return xsdDate2Calendar(prov.getStringValue());               
            } else if (this.typeID == DataTypeBase.XML_SCHEMAS_DATETIME) {
                // format: CCYY-MM-DDTHH:MM:SS
                return xsdDateTime2Calendar(prov.getStringValue());
            } else if (this.typeID == DataTypeBase.XML_SCHEMAS_TIME) {
            	return xsdTime2Calendar(prov.getStringValue());
            } else if (this.typeID == DataTypeBase.XML_SCHEMAS_GYEARMONTH) {
                //CCYY-MM
                String timeMonthStr = prov.getStringValue().substring(0,7);
                String yearStr = timeMonthStr.substring(0,4);
                String monthStr = timeMonthStr.substring(5,7);
                setYear(cal, yearStr);
                setMonth(cal, monthStr);
            } else if (this.typeID == DataTypeBase.XML_SCHEMAS_GYEAR) {
                String yearStr = prov.getStringValue().substring(0, 4);
                setYear(cal, yearStr);
            } else if (this.typeID == DataTypeBase.XML_SCHEMAS_GMONTHDAY) {
                //--MM-DD
                String gMonthDayStr=prov.getStringValue();
                //String monthDayStr = prov.getStringValue().substring(2,7);
                String monthStr = gMonthDayStr.substring(2,4);
                String dayStr = gMonthDayStr.substring(6,7);                
                setMonth(cal, monthStr);
                setDay(cal, dayStr);
            } else if (this.typeID == DataTypeBase.XML_SCHEMAS_GDAY) {
                //---DD
                String dayStr = prov.getStringValue().substring(3,5);
                setDay(cal, dayStr);
            } else if (this.typeID == DataTypeBase.XML_SCHEMAS_GMONTH) {                
                //--MM--
                String monthStr = prov.getStringValue().substring(2,4);
                setMonth(cal, monthStr);
            }
            return cal;
        }        
    }

    public boolean validate(ValueProvider prov) {
        String strValue = prov.getStringValue();
        // format: CCYY-MM-DD (plus timezone)
        if (strValue.length() < 10) {
            return false;
        }
        if (strValue.charAt(4) != '-') {
            return false;
        }
        if (strValue.charAt(7) != '-') {
            return false;
        }        
        // TODO: validate date
        return true;
    }

    protected String getBaseTypeName() {
        return "date";
    }

    static final String ZEROS ="00000000000000000000";
    
    /**
     * Pad a value with zeroes on the left.
     * 
     * @param length
     * @param val
     * @return
     */
    public static String getPadded(int length, int val) {
        String valStr = Integer.toString(val);
        int strLen = valStr.length();
        if (strLen < length) {
            return ZEROS.substring(0, (length-strLen)) + valStr;
        } else {
            return valStr;
        }
    }
    
    private static String getYear(Calendar cal) {
        return getPadded(4, cal.get(Calendar.YEAR));
    }

    private static String getMonth(Calendar cal) {
        int month = 1;
        
        switch (cal.get(Calendar.MONTH)) {
        case Calendar.JANUARY:
            month = 1;
            break;
        case Calendar.FEBRUARY:
            month = 2;
            break;
        case Calendar.MARCH:
            month = 3;
            break;
        case Calendar.APRIL:
            month = 4;
            break;
        case Calendar.MAY:
            month = 5;
            break;
        case Calendar.JUNE:
            month = 6;
            break;
        case Calendar.JULY:
            month = 7;
            break;
        case Calendar.AUGUST:
            month = 8;
            break;
        case Calendar.SEPTEMBER:
            month = 9;
            break;
        case Calendar.OCTOBER:
            month = 10;
            break;
        case Calendar.NOVEMBER:
            month = 11;
            break;
        case Calendar.DECEMBER:
            month = 12;
            break;
        }
                   
        return getPadded(2, month);
    }

    private static String getHour(Calendar cal) {
        return getPadded(2, cal.get(Calendar.HOUR_OF_DAY));                
    }
    
    private static String getMinute(Calendar cal) {
        return getPadded(2, cal.get(Calendar.MINUTE));
    }
    
    private static String getSecond(Calendar cal) {
        return getPadded(2, cal.get(Calendar.SECOND));
    }
    
    private static String getMillisecond(Calendar cal) {
        return getPadded(3, cal.get(Calendar.MILLISECOND));
    }
    
    private static String getDay(Calendar cal) {
        return getPadded(2, cal.get(Calendar.DAY_OF_MONTH));
    }
    public static String getWeekDayName(Calendar cal, String[] weekdays) {
        return weekdays[cal.get(Calendar.DAY_OF_WEEK)-1];
    }
    public static String getMonthName(Calendar cal, String[] months) {
        return months[cal.get(Calendar.MONTH)];
    }

    public static void setDateTime(Calendar cal, String stringVal) {
        // TODO: validate that stringVal is a legal datetime
        String dateStr = stringVal.substring(0, 10);
        String timeStr = stringVal.substring(11, stringVal.length());
        setDate(cal, dateStr);
        setTime(cal, timeStr);
    }
    
    // Time formats:
    // 19:20+01:00
    //
    public static void setTime(Calendar cal, String timeStr) {
        //#debug info
        System.out.println("timeStr: " + timeStr);

    	
    	String hourStr = timeStr.substring(0,2);
        String minuteStr = timeStr.substring(3,5);
        boolean minusZone = false;
        boolean gmtZone = false;
        int zoneStart = -1;
        
        int len = timeStr.length();
        for(int i=0; i<len; i++) {
        	char c = timeStr.charAt(i);
        	if (c == 'Z') {
        		gmtZone = true;
        		zoneStart = i;
        		break;
        	} else if (c == '+') {
        		zoneStart = i;
        		break;
        	} else if (c == '-') {
        		minusZone = true;
        		zoneStart = i;
        		break;
        	}
        }
        TimeZone oldTz = cal.getTimeZone();
        boolean oldIsGmt = (oldTz.getRawOffset() == 0);
        //#debug info
        System.out.println("Old is GMT: " + oldIsGmt + " (offset: " + oldTz.getRawOffset() + ")");
        
        String secondStr = null;
        String tzStr = null;
        int hourOffset = 0;
        int minOffset = 0;
        if (zoneStart < 0) {
            secondStr = timeStr.substring(6);
        } else {
            secondStr = timeStr.substring(6, zoneStart);
            tzStr = timeStr.substring(zoneStart);
            if (!gmtZone) {
                String hourOffsetStr = tzStr.substring(1, 3);
                String minOffsetStr = tzStr.substring(4, 6);
                hourOffset = Integer.parseInt(hourOffsetStr);
                minOffset = Integer.parseInt(minOffsetStr);
                if (minusZone) {
                    hourOffset *= -1;
                    minOffset *= -1;
                }
            }
        }
        double seconds = Double.parseDouble(secondStr);
        int wholeSeconds = (int) Math.floor(seconds);
        int milliSeconds = (int) ((seconds - wholeSeconds) * 1000);
        
        if (!oldIsGmt) {
        	//#debug info
        	System.out.println("Setting timezone to GMT");
        	cal.setTimeZone(TimeZone.getTimeZone("GMT")); // +00:00
        }
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourStr));
    	//#debug info
    	System.out.println("Setting hours to " + Integer.parseInt(hourStr));
        cal.set(Calendar.MINUTE, Integer.parseInt(minuteStr));
    	//#debug info
    	System.out.println("Setting minutes to " + Integer.parseInt(minuteStr));
        cal.set(Calendar.SECOND, wholeSeconds);
    	//#debug info
    	System.out.println("Setting seconds to " + wholeSeconds);
        cal.set(Calendar.MILLISECOND, milliSeconds);
    	//#debug info
    	System.out.println("Setting milliseconds to " + milliSeconds);
        if (zoneStart >= 0) {
        	//#debug info
        	System.out.println("Modifying for timezone " + tzStr);
            long time = cal.getTime().getTime();
            time += hourOffset * 3600000 + minOffset * 60000;
            cal.setTime(new Date(time));
        } else { // Adjust for local time instead (default)
        	//#debug info
        	System.out.println("Adjusting for local time: " + oldTz.getRawOffset());
        	//#debug info
        	System.out.println("  Before: " + cal.getTime());
        	long time = cal.getTime().getTime();
            time -= oldTz.getRawOffset();
            cal.setTime(new Date(time));
        	//#debug info
        	System.out.println("  After: " + cal.getTime());
        }
        if (!oldIsGmt) {
        	//#debug info
        	System.out.println("Setting timezone back to " + oldTz.getID());
        	cal.setTimeZone(oldTz);
        }
    }

    public static void setDate(Calendar cal, String dateStr) {
        String yearStr = dateStr.substring(0,4);
        String monthStr = dateStr.substring(5,7);
        String dayStr = dateStr.substring(8,10);
        
        setYear(cal, yearStr);
        setMonth(cal, monthStr);
        setDay(cal, dayStr);
    }
    
    private static void setYear(Calendar cal, String yearStr) {
        cal.set(Calendar.YEAR, Integer.parseInt(yearStr));        
    }
    
    private static void setMonth(Calendar cal, String monthStr) {
        switch (Integer.parseInt(monthStr)) {
        case 1:
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            break;
        case 2:
            cal.set(Calendar.MONTH, Calendar.FEBRUARY);
            break;
        case 3:
            cal.set(Calendar.MONTH, Calendar.MARCH);
            break;
        case 4:
            cal.set(Calendar.MONTH, Calendar.APRIL);
            break;
        case 5:
            cal.set(Calendar.MONTH, Calendar.MAY);
            break;
        case 6:
            cal.set(Calendar.MONTH, Calendar.JUNE);
            break;
        case 7:
            cal.set(Calendar.MONTH, Calendar.JULY);
            break;
        case 8:
            cal.set(Calendar.MONTH, Calendar.AUGUST);
            break;
        case 9:
            cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
            break;
        case 10:
            cal.set(Calendar.MONTH, Calendar.OCTOBER);
            break;
        case 11:
            cal.set(Calendar.MONTH, Calendar.NOVEMBER);
            break;
        case 12:
            cal.set(Calendar.MONTH, Calendar.DECEMBER);
            break;
        }               
    }
        
    private static void setDay(Calendar cal, String dayStr) {
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayStr));
    }
}
