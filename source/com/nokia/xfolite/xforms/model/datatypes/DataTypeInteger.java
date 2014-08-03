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

public class DataTypeInteger extends DataTypeDecimal {

    DataTypeInteger(int xmlSchemaValTypeID) {
        super(xmlSchemaValTypeID);
    }
    
    public void setIntegerValue(int value, ValueProvider prov) {
        prov.setStringValue(long2xsdInteger(value));
    }

    public int getIntegerValue(ValueProvider prov) {
        String val = prov.getStringValue();
        return (int) xsdInteger2long(val);
    }
    
    public static String long2xsdInteger(long l) {
        return Long.toString(l);
    }
    
    public static long xsdInteger2long(String s) {
        try {
            return (long) Math.floor(Double.parseDouble(s));
        } catch (Exception ignore) {
            return 0;
        }
    }
    
    public boolean validate(ValueProvider prov) {
        // TODO validate integers
        return true;
    }

    protected String getBaseTypeName() {
        return "integer";
    }
}
