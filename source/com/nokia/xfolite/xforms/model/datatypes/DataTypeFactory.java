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

import java.util.Enumeration;
import java.util.Vector;

import com.nokia.xfolite.xforms.model.datatypes.DataTypeBase;
import com.nokia.xfolite.xml.xpath.XPathNSResolver;

public class DataTypeFactory {
	private Vector mappings;
	
    /**
     * Use getDataTypeFactory of XFormsModel to access the 
     * instance of the factory. 
	 */
    public DataTypeFactory() {
        mappings = new Vector();
        addMappings();
    }
    
	private void addMappings() {
        // TODO add all data type mappings                
        
        addMapping(new DataTypeBase(DataTypeBase.XML_SCHEMAS_STRING));
        
        // all of the following use CDataTypeDecimal as base type
        addMapping(new DataTypeDecimal(DataTypeBase.XML_SCHEMAS_DECIMAL));
        //addMapping(new DataTypeDouble(DataTypeBase.XML_SCHEMAS_DOUBLE));
        //addMapping(new DataTypeFloat(DataTypeBase.XML_SCHEMAS_FLOAT));
        
        // all of the following use CDataTypeInteger as base type
        addMapping(new DataTypeInteger(DataTypeBase.XML_SCHEMAS_INTEGER));
//        AddMappingL(new CDataTypeIntegerBased(TString("nonPositiveInteger"),CDataTypeBase::XML_SCHEMAS_NPINTEGER));
//        AddMappingL(new CDataTypeIntegerBased(TString("long"),CDataTypeBase::XML_SCHEMAS_LONG));
//        AddMappingL(new CDataTypeIntegerBased(TString("nonNegativeInteger"),CDataTypeBase::XML_SCHEMAS_NNINTEGER));
//        AddMappingL(new CDataTypeIntegerBased(TString("negativeInteger"),CDataTypeBase::XML_SCHEMAS_NNINTEGER));
//        AddMappingL(new CDataTypeIntegerBased(TString("int"),CDataTypeBase::XML_SCHEMAS_INT));
//        AddMappingL(new CDataTypeIntegerBased(TString("unsignedLong"),CDataTypeBase::XML_SCHEMAS_ULONG));
//        AddMappingL(new CDataTypeIntegerBased(TString("positiveInteger"),CDataTypeBase::XML_SCHEMAS_PINTEGER));
//        AddMappingL(new CDataTypeIntegerBased(TString("unsignedInt"),CDataTypeBase::XML_SCHEMAS_UINT));
//        AddMappingL(new CDataTypeIntegerBased(TString("unsignedShort"),CDataTypeBase::XML_SCHEMAS_USHORT));
//        AddMappingL(new CDataTypeIntegerBased(TString("unsignedByte"),CDataTypeBase::XML_SCHEMAS_UBYTE));
//        AddMappingL(new  CDataTypeIntegerBased(TString("short"),CDataTypeBase::XML_SCHEMAS_SHORT));
//        AddMappingL(new CDataTypeIntegerBased(TString("byte"),CDataTypeBase::XML_SCHEMAS_BYTE));       
        
        // this is the only datatype using DataTypeBoolean as the base type
        addMapping(new DataTypeBoolean());
        
        // these use DataTypeDate as the base type
        addMapping(new DataTypeDate(DataTypeBase.XML_SCHEMAS_DATE));
        addMapping(new DataTypeDateTime(DataTypeBase.XML_SCHEMAS_DATETIME));
        addMapping(new DataTypeDateComponent ("gYearMonth", DataTypeBase.XML_SCHEMAS_GYEARMONTH));
        addMapping(new DataTypeDateComponent ("time", DataTypeBase.XML_SCHEMAS_TIME));
        addMapping(new DataTypeDateComponent ("gYear", DataTypeBase.XML_SCHEMAS_GYEAR));
        addMapping(new DataTypeDateComponent ("gMonthDay", DataTypeBase.XML_SCHEMAS_GMONTHDAY));
        addMapping(new DataTypeDateComponent ("gDay", DataTypeBase.XML_SCHEMAS_GDAY));
        addMapping(new DataTypeDateComponent ("gMonth", DataTypeBase.XML_SCHEMAS_GMONTH));
        
        addMapping(new DataTypeDuration());

    }

    public void addMapping(DataTypeBase base) {         
		DataTypeMapping mapping = new DataTypeMapping(base);
		mappings.addElement(mapping);
	}
   
    public DataTypeBase stringToDataType(String qName, XPathNSResolver nsRes) {
        DataTypeMapping ret = stringToDataTypeMapping(qName, nsRes);
        if (ret != null) {
            return ret.dataType;
        } else {
            return typeIDToDataType(DataTypeBase.XML_SCHEMAS_STRING);
        }
    }
    

    /**
     * The following advice straight from C++:
     * 
     * USE THIS FUNCTION. You need to strip the localname part of the datatype yourself.
     * e.g. localname="string" ns="http://..."
     * @param localname  the name of the datatype without the trailing ns prefix. e.g. 'date'
     * @param namespace  the namespace of the datatype without the trailing ns prefix. e.g. 'http://xxx...'
     */
    public DataTypeBase stringToDatatype(String localname, String namespace) {
        Enumeration mappingsEnum = mappings.elements();
        DataTypeMapping mapping;
        while (mappingsEnum.hasMoreElements()) {
            mapping = (DataTypeMapping)mappingsEnum.nextElement();
            if (localname.equals(mapping.dataType.getTypeName()) && namespace.equals(mapping.dataType.getTypeNS())) {
                return mapping.dataType;
            }
        }
        return typeIDToDataType(DataTypeBase.XML_SCHEMAS_STRING);
    }


    public int stringToDatatypeID(String qname, XPathNSResolver res)
    {
        DataTypeMapping ret = stringToDataTypeMapping(qname, res);
        if (ret != null) {
            return ret.dataType.getBaseTypeID();
        } else {
            return DataTypeBase.XML_SCHEMAS_STRING;
        }
    }

    
    public int stringToDatatypeID(String str) {
        
        int ret = DataTypeBase.XML_SCHEMAS_STRING;
        
        SeparatedName sn = DataTypeBase.breakPrefixAndLocalName(str);
        String localname = sn.localName;
        String prefix = sn.prefix;
        
        Enumeration mappingsEnum = mappings.elements();
        DataTypeMapping mapping;
        while (mappingsEnum.hasMoreElements()) {
            mapping = (DataTypeMapping) mappingsEnum.nextElement();
            if (localname.equals(mapping.dataType.getTypeName())) {
                ret = mapping.dataType.getBaseTypeID();
            }
        }
        
        return ret;
    }
    
    
    public DataTypeBase typeIDToDataType(int dataTypeID) {
        Enumeration mappingsEnum = mappings.elements();
        DataTypeMapping mapping;
        while (mappingsEnum.hasMoreElements()) {
            mapping = (DataTypeMapping)mappingsEnum.nextElement();
            if (mapping.dataType.getBaseTypeID() == dataTypeID) {
                return mapping.dataType;
            }
        }

        return new DataTypeBase();        
    }

    private DataTypeMapping stringToDataTypeMapping(String qName, XPathNSResolver nsRes) {
        DataTypeMapping ret = null;
        String localName;
        String prefix;
        SeparatedName sn = DataTypeBase.breakPrefixAndLocalName(qName);//, prefix, localName);
        prefix = sn.prefix;
        localName = sn.localName;
        
        String nsURI = null;
        
        if (prefix != null && nsRes != null) {
            nsURI = nsRes.lookupNamespaceURI(prefix);
        }
        
        Enumeration mappingsEnum = mappings.elements();
        DataTypeMapping mapping;
        while (mappingsEnum.hasMoreElements()) {
            mapping = (DataTypeMapping)mappingsEnum.nextElement();
            if (localName.equals(mapping.dataType.getTypeName()) && 
                    (nsURI == null || nsURI.equals(mapping.dataType.getTypeNS()))) {
                ret = mapping;
                // for some reason they're all looped in C++, but seems useless once 
                // a match is found(?)
                return ret;
            }
        }
        return ret;
}

    
}
