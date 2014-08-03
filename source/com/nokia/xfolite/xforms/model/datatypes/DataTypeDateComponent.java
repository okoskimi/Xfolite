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

public class DataTypeDateComponent extends DataTypeDate {
    
    protected String baseTypeName;
    
    public DataTypeDateComponent(String typeName, int typeID) {
        super(typeID);
        this.baseTypeName = typeName;
    }
    
    protected String getBaseTypeName() {
        return baseTypeName;
    }  
    
    public boolean validate(ValueProvider prov) {
        if (this.typeID == DataTypeBase.XML_SCHEMAS_TIME) {
            //HH:MM:SS
            String timeTzStr = prov.getStringValue();
            if (timeTzStr.length() < 8) {
                return false;
            }
            if (timeTzStr.charAt(2) != ':') {
                return false;
            }
            if (timeTzStr.charAt(5) != ':') {
                return false;
            }
        } else if (this.typeID == DataTypeBase.XML_SCHEMAS_GYEARMONTH) {
            // CCYY-MM
            String yearMonthTzStr = prov.getStringValue();
            if (yearMonthTzStr.length() < 7) {
                return false;
            }
            if(yearMonthTzStr.charAt(4) != '-') {
                return false;        
            }        
        } else if (this.typeID == DataTypeBase.XML_SCHEMAS_GYEAR) {
            // CCYY
        } else if (this.typeID == DataTypeBase.XML_SCHEMAS_GMONTHDAY) {
            //--MM-DD
        } else if (this.typeID == DataTypeBase.XML_SCHEMAS_GDAY) {
            //---DD
        } else if (this.typeID == DataTypeBase.XML_SCHEMAS_GMONTH) {                
            //--MM--
        }
        // TODO: validation of date component types
        return true;
    }
}
