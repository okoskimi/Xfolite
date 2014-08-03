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

package com.nokia.xfolite.xforms.model;

import java.util.*;

import com.nokia.xfolite.xforms.model.datatypes.DataTypeBase;
import com.nokia.xfolite.xforms.model.datatypes.SeparatedName;
import com.nokia.xfolite.xforms.model.datatypes.ValueProvider;
import com.nokia.xfolite.xml.dom.Attr;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;

public class InstanceItem implements ValueProvider {

	private XFormsModel model;
	private Node instanceNode;
	
	// states
	public static final int RELEVANT_S = 1 << 1;
	public static final int READONLY_S = 1 << 2;
	public static final int REQUIRED_S = 1 << 3;
	public static final int CONSTRAINT_S = 1 << 4;
	public static final int SCHEMA_VALID_S = 1 << 5;
	public static final int INTERNAL_XSI_TYPE_SEARCHED = 1 << 6; // this is zero in the beginning and is set to 1 when attributes are checked for xsi:type. Again, when attributes are added or removed, this is set to 0
    public static final int INTERNAL_HAS_XSI_TYPE = 1 << 7; // Whether or not this has xsi:type attribute
    public static final int INTERNAL_HAS_BINARY_ATTACHMENT = 1 << 8; // Whether or not this has xsi:type attribute

    
	// EInternalXSITypeSearched, EInternalHasXSIType, EInternalHasBinaryAttachment	
	public static final int DEFAULT_STATE = RELEVANT_S | CONSTRAINT_S | SCHEMA_VALID_S;
	
	private int localState;
	private int parentState;
	//private InstanceItemListener[] listeners;
	private Vector listeners;
	private DataTypeBase dataType;
    
    
	public InstanceItem(Node instanceNode, XFormsModel model) {
		this.model = model;
		this.instanceNode = instanceNode;
		this.localState = DEFAULT_STATE;
		this.parentState = DEFAULT_STATE;
		listeners = new Vector();
	}
	
    public XFormsModel getModel() {
        return model;
    }
    
	public static InstanceItem getInstanceItem(Node node, XFormsModel model) {
		InstanceItem item = (InstanceItem)node.getUserData();
		if (item == null) {
			item = new InstanceItem(node, model);
            node.setUserData(item);            
		}
		return item;
	}

    /** 
     * This will generate the dataType first time called for a node. 
     */ 
    public DataTypeBase getDataType() {
        resolveBaseType();
        if (dataType == null) {
            return XFormsModel.getDataTypeFactory().typeIDToDataType(DataTypeBase.XML_SCHEMAS_STRING);
        }
        return dataType;
    }
    
	// TODO: getBaseType
	public int getBaseType() {
		if (dataType==null) {
			resolveBaseType();
		}
		//CDataTypeBase::xmlSchemaValType type=CDataTypeBase::XML_SCHEMAS_STRING;
		int type = DataTypeBase.XML_SCHEMAS_STRING;
		if (dataType!=null) {
			type=dataType.getBaseTypeID();
		}
		return type;
	}

//  EXPORT_C CDataTypeBase::xmlSchemaValType CInstanceItem::ResolveBaseType()
//  {
//      // TODO: what if both xsi:type and xforms:type is declared?
//      if (testLocal(EInternalXSITypeSearched)==FALSE)
//      {
//          InternalSetLocalState(EInternalXSITypeSearched,TRUE);
//          const TNode n = GetNode();
//          if (n.NodeType()==TNode::EElement)
//          {
//              TAttr xsitype = n.AsElement().AttributeNode(TDOMString("type"), TDOMString("http://www.w3.org/2001/XMLSchema-instance"));
//              if (xsitype.NotNull())
//              {
//                  TString typeStr = xsitype.Value().CopyL();
//                  TString local;
//                  TString prefix;
//                  CDataTypeBase::BreakPrefixAndLocalname(typeStr,&prefix,&local);
//                  //CInstanceNSResolver* res = new CInstanceNSResolver(n);
//                  
//                  CNodeContextNamespaceResolver* res = new CNodeContextNamespaceResolver(n);
//                  TDOMString nsuri = res->LookupNamespaceUri(prefix);
//                  TString nsuriCopy = nsuri.CopyL();
//                  CDataTypeBase::xmlSchemaValType typeID=iModel->GetDataTypeFactoryL()->StringToDatatypeID(local);
//                  //iBaseType=typeID;             
//                  CDataTypeBase* typeImpl=iModel->GetDataTypeFactoryL()->StringToDatatype(local,nsuriCopy);
//                  iDataType=typeImpl;
//                  InternalSetLocalState(EInternalHasXSIType,TRUE);
//                  typeStr.Free();
//                  local.Free();
//                  prefix.Free();
//                  nsuriCopy.Free();
//                  delete res;
//              }
//          }
//      }
//      // this is set by the model when processing binds
//      return CDataTypeBase::XML_SCHEMAS_STRING;//iBaseType;
//  }

    // TODO: resolveBaseType
    private int resolveBaseType() {
        if (testLocal(INTERNAL_XSI_TYPE_SEARCHED) == false) {
            internalSetLocalState(INTERNAL_XSI_TYPE_SEARCHED, true);
            Node n = getNode();
//            if (n.getNodeType() == Node.ELEMENT_NODE) {               
//                //xsitype = n.AttributeNode("type", "http://www.w3.org/2001/XMLSchema-instance");
//                String xsitypeValue = ((Element)n).getAttributeNS("http://www.w3.org/2001/XMLSchema-instance","type");
//                if (xsitypeValue != null) {
//                   String local;
//                   String prefix;
//                   SeparatedName sn = DataTypeBase.breakPrefixAndLocalName(xsitypeValue);
//                   local = sn.localName;
//                   prefix = sn.prefix;
//                   String nsURI = null;
//                   //NodeContextNamespaceResolver res = NodeContextNamespaceResolver(n);
//                   //String nsURI = res.lookupnamespaceURI(prefix);
//                   // TODO: check that this is correct
//                   nsURI = n.getNamespaceURI();
//                   
//                   int xmlSchemaValTypeID = model.getDataTypeFactory().stringToDatatypeID(local);
//                   DataTypeBase typeImpl = model.getDataTypeFactory().stringToDatatype(local, nsURI);
//                   dataType = typeImpl;
//                   internalSetLocalState(INTERNAL_HAS_XSI_TYPE, true);                   
//                }
//            }
        }
        
        return DataTypeBase.XML_SCHEMAS_STRING;
    }

    private boolean test(int aStateDesc, int aStateId)
    {
    	return (aStateDesc & aStateId)>0;
    }
    
    /** USE ONLY INTERNALLY; only tests the local state without the inheritence */
    private boolean testLocal(int stateID) {
        return (localState&stateID)>0;
    }
    
    private void internalSetLocalState(int stateID, boolean value) {
        if (value) {
            localState |= stateID;
        } else {
            localState&=~stateID;
        }
    }
    
    private int internalSetLocalState(int stateID, boolean value, int statedesc) {
        if (value) {
            statedesc |= stateID;
        } else {
        	statedesc&=~stateID;
        }
        return statedesc;
    }

    /** this resets the MIP and type state (and inherits results). This is called only by the XFormsModel before doing a new rebuild. Do not call it otherwise. */
    void resetState()
    {
    	localState = DEFAULT_STATE;
    	internalSetLocalState(INTERNAL_XSI_TYPE_SEARCHED, false);
    	dataType = null;
    }

    
	public Node getNode() {
		return instanceNode;
	}
	
	public void addInstanceItemListener(InstanceItemListener listener) {
		Enumeration le = listeners.elements();
		while (le.hasMoreElements()) {
			if (le.nextElement().equals(listener)) {
                return;
			}
		}
        listeners.addElement(listener);
	}
		
	public void removeInstanceItemListener(InstanceItemListener listener) {
		Enumeration le = listeners.elements();
		while (le.hasMoreElements()) {
			if (le.nextElement().equals(listener)) {
				listeners.removeElement(listener);
                return;
			}
		}
		
	}

    public void setBaseType(DataTypeBase typeImpl) {
        resolveBaseType();
        if (testLocal(INTERNAL_HAS_XSI_TYPE)==false) {
            dataType = typeImpl;
        }
    }
    
	public void setStringValue(String string) {
        //#debug
        System.out.println("InstanceItem.setStringValue");
        model.setNodeText(instanceNode, string);
	}
	
	public String getStringValue() {
		return model.getNodeText(instanceNode);
	}

	// TODO: instanceValueChanged
	/* for the model to notify the instance item of a change */
	protected void instanceValueChanged(String newValue) {
        //#debug 
        System.out.println("InstanceItem.instanceValueChanged");
		int baseType = getBaseType();
		if (baseType != DataTypeBase.XML_SCHEMAS_STRING && baseType != DataTypeBase.XML_SCHEMAS_UNKNOWN) {
			boolean valid = true;
			DataTypeBase typeImpl = getDataType();
			if (typeImpl != null) {
				valid = typeImpl.validate(this);
			}
            
			setBooleanLocalState(MIPExpr.VALID, valid);

		}
		
		Vector listenersCopy = InstanceItem.cloneVector(listeners);
		int count = listenersCopy.size();
		for (int i=0; i < count; i++)
		{
		    //#debug 
	        System.out.println("InstanceItem.instanceValueChanged: Notifying listener #" + i);
		
			InstanceItemListener listener = (InstanceItemListener) listenersCopy.elementAt(i);
			listener.valueChanged(newValue, this);
		}
		listenersCopy = null;
	}

	boolean isValid()
	{
		return (getBooleanState(MIPExpr.SCHEMA_VALID)
				&& getBooleanState(MIPExpr.CONSTRAINT));
	}

	private int convertMIPToState(int aMIPId)
	{
		//return ( (TStateID) (1 << aMIPId)); 
		if (aMIPId == MIPExpr.RELEVANT) return RELEVANT_S;
		else if (aMIPId == MIPExpr.READONLY) return READONLY_S;
		else if (aMIPId == MIPExpr.REQUIRED) return REQUIRED_S;
		else if (aMIPId == MIPExpr.CONSTRAINT) return CONSTRAINT_S;
		else if (aMIPId == MIPExpr.SCHEMA_VALID) return SCHEMA_VALID_S;
		return RELEVANT_S; //FALLBACK TODO: create error code
	}	
	

	/** this method should be used to query the current status of a MIP. It takes
	 into account both the inherited value from the parent as well as the
	 local value set by the MDG. Use the value in TXFormsBase for the state id  */
	boolean getBooleanState(int aMIPStateId)
	{
		if (aMIPStateId == MIPExpr.VALID)
		{
			return isValid();
		}
		int aStateId = convertMIPToState(aMIPStateId);
		// only readonly and relevant inherits
		//The inheritance rules are: 1) readonly parent true inherits, 2)relevant parent false inherits.
		//(See XForms 1.0 REC section 6.1)
		if (aStateId == READONLY_S)
		{
			return testLocal(READONLY_S) || test(parentState,READONLY_S);
		}
		else if (aMIPStateId == MIPExpr.RELEVANT)
		{
			return testLocal(RELEVANT_S) && test(parentState, RELEVANT_S);
		
		}
		else return (testLocal(aStateId));
	}
	
	/**
	 * Set the inherited state.
	 * Will determine (based on local value) if the state should 
	 * be further inherited to children. Use this method only internally.
	 * The inheritance rules are: 1) readonly parent true inherits, 2)relevant parent false inherits.
	 * (See XForms 1.0 REC section 6.1)
	 */
	void setBooleanInheritedState(int aParentState)
	{
		int oldState = parentState;
		parentState = aParentState;
		if (test(parentState, RELEVANT_S) != test(oldState, RELEVANT_S))
		{
			propagateAndNotifyStateChange(MIPExpr.RELEVANT);
		}
		if (test(aParentState, READONLY_S)!=test(oldState, READONLY_S))
		{
			propagateAndNotifyStateChange(MIPExpr.READONLY);
		}
	}
	
	void setBooleanLocalState(int aMIPId, boolean value)
	{
		boolean oldState = getBooleanState(aMIPId); // this takes into account also the parent state
		if ((aMIPId>MIPExpr.MAXBOOLEXPRID) || (aMIPId < 0))
		{
			return;
		}
		int stateId = convertMIPToState(aMIPId);
		internalSetLocalState(stateId, value); // sets the state value 
		boolean newState = getBooleanState(aMIPId); // this takes into account also the parent state
		//ASSERT(newState==1||newState==0);
		if (oldState != newState)
		{
			propagateAndNotifyStateChange(aMIPId);
		}		
	}

	void propagateAndNotifyStateChange(int aMIPId)
	{
		boolean eventValue = getBooleanState(aMIPId); 
		// throw the change events to listeners
		Vector listenersCopy = InstanceItem.cloneVector(listeners);
		int count = listenersCopy.size();
		for (int i=0; i < count; i++)
		{
			InstanceItemListener listener = (InstanceItemListener) listenersCopy.elementAt(i);
			listener.statusChanged(aMIPId, eventValue, this);
		}
		listenersCopy = null;
		// TODO calculate combined here
		int combined = localState;
		if (this.test(parentState, READONLY_S)==true)
			combined = this.internalSetLocalState(aMIPId, true,combined);
		if (this.test(parentState, RELEVANT_S)==false)
			combined = this.internalSetLocalState(aMIPId, false,combined);
		// PROPAGATE THE CHANGE TO CHILD NODES AND ATTRIBUTES
		if (instanceNode.getNodeType() == Node.ELEMENT_NODE)
		{
			Node child = instanceNode.getFirstChild();
			while (child != null)
			{
				if (child.getNodeType() == Node.ELEMENT_NODE)
				{
					InstanceItem childItem = model.getInstanceItemForNode(child);
					childItem.setBooleanInheritedState(combined); // TODO: combined
				}
				child = child.getNextSibling();
			}
			// TODO: attributes
			if (instanceNode.hasAttributes())
			{
				// Attributes
				count = instanceNode.getAttributeCount();
				for(int i=0; i < count; i++)
				{
					Attr a = instanceNode.getAttribute(i); 
					InstanceItem childItem = model.getInstanceItemForNode(a);
					childItem.setBooleanInheritedState(combined); // TODO: combined
				}
			}
		}
		
	}

	public static final Vector cloneVector(Vector src)
	{
		int count = src.size();
		Vector result = new Vector(count); 
		for(int i = 0; i < count; i++) {
			result.addElement(src.elementAt(i));
		}
		return result;
	}	
	
	/** Upload sets the binary file through this method. 
	 * The implementation sets one bit in the status field, and stores the attachment in the model.
	 * @param encoding not used yet
	 */
	public void setBinaryAttachment(byte[] buf, String mimetype, String filename, String encoding)
	{
		this.removeBinaryAttachment();
		BinaryAttachment attachment = new BinaryAttachment();
		attachment.SetInstanceItem(this);
		attachment.SetData(buf);
		attachment.SetFilename(filename);
		attachment.SetMime(mimetype);
		this.getModel().setBinaryAttachment(this, attachment);
		this.internalSetLocalState(this.INTERNAL_HAS_BINARY_ATTACHMENT,true);
		//attachment.SetEncoding(encoding);
		
	}
	/** Upload sets the binary file through this method IN THE CASE OF FILE. 
	 * The implementation sets one bit in the status field, and stores the attachment in the model.
	 * @param The file, e.g. "c:\\testfile.bin"
	 * @param encoding not used yet
	 */
	public  void setBinaryAttachment(String file, String mimetype, String filename, String encoding)
	{
		this.removeBinaryAttachment();
		BinaryAttachment attachment = new BinaryAttachment();
		attachment.SetInstanceItem(this);
		attachment.SetFilelink(file);
		attachment.SetFilename(filename);
		this.getModel().setBinaryAttachment(this, attachment);
		this.internalSetLocalState(this.INTERNAL_HAS_BINARY_ATTACHMENT,true);
		//attachment.SetEncoding(encoding);
	}
	public boolean hasBinaryAttachment()
	{
		return (this.getModel().getBinaryAttachment(this)!=null);
	}
	public  BinaryAttachment getBinaryAttachment()
	{
		return this.getModel().getBinaryAttachment(this);
	}
	public void removeBinaryAttachment()
	{
		this.getModel().removeBinaryAttachment(this);
		this.internalSetLocalState(this.INTERNAL_HAS_BINARY_ATTACHMENT,false);
	}

	
	
	
}
