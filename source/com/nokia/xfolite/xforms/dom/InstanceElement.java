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

package com.nokia.xfolite.xforms.dom;

import com.nokia.xfolite.xforms.model.Instance;
import com.nokia.xfolite.xforms.model.XFormsModel;
import com.nokia.xfolite.xforms.model.datasource.DataSource;
import com.nokia.xfolite.xforms.model.datasource.DataSourceFactory;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Node;

import java.util.Hashtable;

public class InstanceElement extends XFormsElement {

    protected InstanceElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
		super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
	}
    
    public boolean elementParsed() {
        super.elementParsed();
        ((XFormsDocument)ownerDocument).setXFormsMode(false);
        return false;
    }

    public boolean childrenParsed() {
        super.childrenParsed();
        XFormsDocument xfdoc = (XFormsDocument)ownerDocument;
        xfdoc.setXFormsMode(true);
        
        String docId = getAttribute("id");
        Hashtable extras = xfdoc.getExtraInstances();
        Document eDoc = null;
        if (extras != null) {
            eDoc = (Document) extras.get(docId);
            if (eDoc != null) {
            	//#debug info
            	System.out.println("Loaded instance '" + docId + "' from extra instances:");
            	//XFormsDocument.printSubtree(eDoc.getDocumentElement());
            }
        }
        
        String src = this.getAttribute("src");
        if (src != "")
        {
            if (eDoc != null) {
                xfdoc.getModel().addInstance(eDoc, docId);
            } else if (this.handleExternalData(src)) {
        		return false;
            }
        }
        
        Document doc = new Document();
        Node root = getFirstChild();
        while(root != null && root.getNodeType() != Node.ELEMENT_NODE) {
            root = root.getNextSibling();
        }
        if (root == null) {
            if (eDoc != null) {
                xfdoc.getModel().addInstance(eDoc, docId);
            }
            return false;
        }
        doc.adoptNode(root);
        doc.appendChild(root);
        
        if (eDoc != null) {
            xfdoc.getModel().addInstance(eDoc, docId);
            doc = null; // Junk the inline instance data
            extras.remove(docId); // Remove it from extras so we don't add it again in ModelElement
        } else {
            xfdoc.getModel().addInstance(doc, docId);     
        }
        
        return false;
    }
    
    protected boolean handleExternalData(String src)
    {
        Document doc = new Document();
    	XFormsModel model = ((XFormsDocument)ownerDocument).getModel();
        DataSource source = ((XFormsDocument)ownerDocument).getDataSource(src);
        if (source!=null)
        {
            Instance inst = model.addInstance(doc, getAttribute("id"));
            source.init(inst,(XFormsDocument)this.getOwnerDocument(),src,this);
            model.addDataSource(source);
            //source.start(); will be started by an event
            return true;
        }
        return false;
    }
}
