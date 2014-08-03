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

public class DataTypeBase {

    // TODO: where should this come from?
    public static final String XML_SCHEMAS_NAMESPACE_NAME = "http://www.w3.org/2001/XMLSchema";
    
	// XML Schema data type identifiers
    public static final int XML_SCHEMAS_UNKNOWN = 0;
    public static final int XML_SCHEMAS_STRING = 1;
    public static final int XML_SCHEMAS_NORMSTRING = 2;
    public static final int XML_SCHEMAS_DECIMAL = 3;
    public static final int XML_SCHEMAS_TIME = 4;
    public static final int XML_SCHEMAS_GDAY = 5;
    public static final int XML_SCHEMAS_GMONTH = 6;
    public static final int XML_SCHEMAS_GMONTHDAY = 7;
    public static final int XML_SCHEMAS_GYEAR = 8;
    public static final int XML_SCHEMAS_GYEARMONTH = 9;
    public static final int XML_SCHEMAS_DATE = 10;
    public static final int XML_SCHEMAS_DATETIME = 11;
    public static final int XML_SCHEMAS_DURATION = 12;
    public static final int XML_SCHEMAS_FLOAT = 13;
    public static final int XML_SCHEMAS_DOUBLE = 14;
    public static final int XML_SCHEMAS_BOOLEAN = 15;
    public static final int XML_SCHEMAS_TOKEN = 16;
    public static final int XML_SCHEMAS_LANGUAGE = 17;
    public static final int XML_SCHEMAS_NMTOKEN = 18;
    public static final int XML_SCHEMAS_NMTOKENS = 19;
    public static final int XML_SCHEMAS_NAME = 20;
    public static final int XML_SCHEMAS_QNAME = 21;
    public static final int XML_SCHEMAS_NCNAME = 22;
    public static final int XML_SCHEMAS_ID = 23;
    public static final int XML_SCHEMAS_IDREF = 24;
    public static final int XML_SCHEMAS_IDREFS = 25;
    public static final int XML_SCHEMAS_ENTITY = 26;
    public static final int XML_SCHEMAS_ENTITIES = 27;
    public static final int XML_SCHEMAS_NOTATION = 28;
    public static final int XML_SCHEMAS_ANYURI = 29;
    public static final int XML_SCHEMAS_INTEGER = 30;
    public static final int XML_SCHEMAS_NPINTEGER = 31;
    public static final int XML_SCHEMAS_NINTEGER = 32;
    public static final int XML_SCHEMAS_NNINTEGER = 33;
    public static final int XML_SCHEMAS_PINTEGER = 34;
    public static final int XML_SCHEMAS_INT = 35;
    public static final int XML_SCHEMAS_UINT = 36;
    public static final int XML_SCHEMAS_LONG = 37;
    public static final int XML_SCHEMAS_ULONG = 38;
    public static final int XML_SCHEMAS_SHORT = 39;
    public static final int XML_SCHEMAS_USHORT = 40;
    public static final int XML_SCHEMAS_BYTE = 41;
    public static final int XML_SCHEMAS_UBYTE = 42;
    public static final int XML_SCHEMAS_HEXBINARY = 43;
    public static final int XML_SCHEMAS_BASE64BINARY = 44;

    // Types enumerated above 
    protected int typeID;

    public DataTypeBase () {}
    
    public DataTypeBase (int xmlSchemaValTypeID) {
        typeID=xmlSchemaValTypeID;
    }
    
    public static SeparatedName breakPrefixAndLocalName(String qName) {
        SeparatedName sn = new SeparatedName();
        
        int colonPos = -1;
        colonPos = qName.indexOf(':');
        if (colonPos > -1 && colonPos < qName.length()) {
            sn.prefix = qName.substring(0, colonPos);
            sn.localName = qName.substring(colonPos+1);            
        }
        
        return sn;
    }
    
    public String getDisplayString(ValueProvider prov)  {
        return prov.getStringValue();
    }
    
    public String getTypeName() {
        return getBaseTypeName();
    }

    public String getBaseTypeNS() {        
        return XML_SCHEMAS_NAMESPACE_NAME;
    }
    
    public String getTypeNS() {
        return getBaseTypeNS();              
    }
    
    protected String getBaseTypeName() {
        // xmlChar is a byte in a UTF-8 encoded string
        
        // caching version MDataTypeOwner* iOwner;
        //        virtual const xmlChar* GetBaseTypeName() 
        //        {
        //            return (const xmlChar* )"string";   
        //        }
        return "string";
    }

    public int getBaseTypeID() {
        return typeID;
    }
    
    // TODO: validate
    public boolean validate (ValueProvider prov) {
//        int ret;
//        xmlSchemaValPtr pVal = InternalValidateL(aProv,ret);
//        InternalFree(pVal);
//        return ret==0;
        return true;
    }

    /*
    protected Object internalValidate(ValueProvider prov, int ret) {
        String str = prov.getStringValue();
        Object pVal = internalValidate(str, ret);
        return pVal;
        
    }

    // TODO: internal validation?
    protected Object internalValidate(String str, int ret) {
        String tname = getBaseTypeName();
        String tNS = getBaseTypeNS();
        
        Object xmlSchemaType = xmlSchemaGetPredefinedType(tname, tNS);
        Object pVal = xmlSchemaValidatePredefinedType(xmlSchemaType, str);
        return pVal;
    }

    protected Object xmlSchemaGetPredefinedType(String typeName, String typeNS) {
        return new Object();        
    }
    
    protected Object xmlSchemaValidatePredefinedType(Object type, String str) {
        return new Object();
    }
  */
  
    //protected
//  xmlSchemaValPtr InternalValidateL(MValueProvider* aProv, TInt& ret);
//  xmlSchemaValPtr InternalValidateL(TString aProv, TInt& ret);
//  
//  void InternalFree(xmlSchemaValPtr pVal);
//  xmlSchemaValType iTypeID;

//    xmlSchemaValPtr CDataTypeBase::InternalValidateL(MValueProvider* aProv, TInt& ret)
//    {
//        TString str = aProv->GetStringValueL();
//        xmlSchemaValPtr pVal = InternalValidateL(str,ret);
//        str.Free();
//        return pVal;
//    }
    
//    xmlSchemaValPtr CDataTypeBase::InternalValidateL(TString str, TInt& ret)
//    {
//        const xmlChar* tname = GetBaseTypeName();
//        const xmlChar* tNS = GetBaseTypeNS();
//        
//        // TODO: optimize so that the typePtr is cached!
//        xmlSchemaTypePtr t = xmlSchemaGetPredefinedType(tname,tNS);
//        xmlSchemaValPtr pVal = NULL;
//        ret = xmlSchemaValidatePredefinedType(t,(const xmlChar*)str.Cstring(), &pVal);
//        return pVal;
//    }
    
      
}
