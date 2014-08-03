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

public class DataTypeDecimal extends DataTypeBase {
   
    DataTypeDecimal(int xmlSchemaValTypeID) {
        super(xmlSchemaValTypeID);
    }

    public String getDisplayString (ValueProvider prov) {
        return prov.getStringValue();
    }
    /*
    public void setDisplayString(String str, ValueProvider prov) {
        double real;

        String trimmedStr = str.trim();
        try {
            real = getDoubleValue(trimmedStr);
        } catch (NumberFormatException e) {
            // TODO: what should be set as the value for a NaN case?
            real = 0.0;
        }
        setDoubleValue(real, prov);
    }
*/
    public void setDoubleValue(double real, ValueProvider prov) {
        // TODO: formatting?
        // format.iType=format.iType|KExtraSpaceForSign|KRealFormatFixed;
        // buf.Num(aReal,format);
        prov.setStringValue(double2xsdDecimal(real));
    }
 
    public static String double2xsdDecimal(double d) {
        return Double.toString(d);
    }
    
    public static double xsdDecimal2double(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception ignore) {
            return 0.0;
        }
    }
    
    public double getDoubleValue(ValueProvider prov) throws NumberFormatException {
        String str = prov.getStringValue();
        return xsdDecimal2double(str);
    }
    
    public double getDoubleValue(String str) throws NumberFormatException {
        return xsdDecimal2double(str);
    }
       
    protected String getBaseTypeName() {
        return "decimal";
    }
}
 