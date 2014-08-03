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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;


import com.nokia.xfolite.xforms.model.Instance;
import com.nokia.xfolite.xforms.model.XFormsModel;
import com.nokia.xfolite.xforms.submission.ISerializer;
import com.nokia.xfolite.xforms.submission.ISubmitter;
import com.nokia.xfolite.xml.dom.Attr;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.NodeFilter;
import com.nokia.xfolite.xml.dom.events.DOMEvent;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.nokia.xfolite.xml.xpath.XPathResult;

public class SubmissionElement extends XFormsElement implements NodeFilter {

    private ISubmitter submitter=null;
    private ISerializer serializer=null;
    public static final int REPLACE_ALL = 0;
    public static final int REPLACE_NONE = 1;
    public static final int REPLACE_INSTANCE = 2;
    
    public SubmissionElement(Document ownerDocument, String aNamespaceURI, String aPrefix, String aLocalName) {
        super(ownerDocument, aNamespaceURI, aPrefix, aLocalName);
    }

    public NodeFilter getNodeFilter() 
    {
        return this;
    }
    
    public boolean elementParsed() {
        super.elementParsed();
        ((XFormsDocument) ownerDocument).addSubmission(this);
        return false;
    }

    
    public int getReplace()
    {
        String replStr = getAttribute("replace");
        if (replStr == "none") {
            return REPLACE_NONE;
        } else if (replStr == "instance") {
            return REPLACE_INSTANCE;
        }
        return REPLACE_ALL;
    }


    private Element getRefNode()
//    TNode GetRootForSubmission(CModel* model, TString ref)
    {
        String ref = getAttribute("ref");
        Node rootNode = getContext().contextNode;
        if (ref != "")
        {
            XPathResult res =  getValue(ref);
            NodeSet nset = res.asNodeSet();
            if (nset != null && nset.getLength() > 0) {
                rootNode = nset.item(0);
            }
            
        }        
        return rootNode instanceof Element ? (Element) rootNode : null;
    }

    void startSubmission()
    {
    	if (this.submitter!=null)
    	{
    		if (this.submitter.isOngoing()||this.getModel().isClosed())
    		{
    			logWarning("Ongoing. Not strted");
            	dispatchEventSerially(DOMEvent.XFORMS_SUBMIT_ERROR_ONGOING);
    			return;
    		}
    	}
    	else
    	{
            submitter = ((XFormsDocument)getOwnerDocument()).getSubmitter(this);
    	}
        XFormsModel model = ((XFormsDocument)getOwnerDocument()).getModel();
        if (serializer==null)
        	serializer = ((XFormsDocument)getOwnerDocument()).getSerializer(this);
        Element root = getRefNode();
        final Runtime rt = Runtime.getRuntime();
        {
          final long free = rt.freeMemory()/1024;
          final long total = rt.totalMemory()/1024;
          this.logStatus(root.getLocalName()+" Mem:"+free+"/"+total+"kb: thrds:"+Thread.activeCount());
        }
        //#debug info
        System.out.println("****************** Starting submission " + getAttribute("id"));
        submitter.startSubmission(this,serializer,root,model);
    }


    /** MOutputStream write */
  /*
    EXPORT_C TInt CSubmission::Write(const TDesC8& aBuffer)
    {
        iParser.ParseChunkL(aBuffer);
        return 0;
    }
*/
    /**
     * Callback for closing output stream
     * 
     * @returns 
     *       0 is succeeded, 
     *      -1 in case of error
     */
/*
    EXPORT_C TInt CSubmission::Close()
    {
        RDocument doc = iParser.FinishL();
        ReplaceInstance(doc);
        return 0;
    }
*/

    public void handleAndCloseReply(InputStream reply, String enc)
    {
    	try
    	{
	    	Attr replaceAttr = this.getAttributeNode("replace");
	    	if (replaceAttr!=null)
	    	{
	    		if (replaceAttr.getValue().equals("instance"))
	    		{
	    			// TODO: parse reply as DOC, call replaceInstance
	    			//printInputStream(reply);
	    			final Document doc = this.loadXMLDocument(reply,enc);
	    			
	                ((XFormsDocument)getOwnerDocument()).callSerially(
	                		//task
	                		new Runnable()
	                		{
	                			public void run()
	                			{
	                		        //#debug info
	                		        System.out.println("****************** Closing submission (replace instance) " + getAttribute("id"));
	            	    			replaceInstance(doc);
	            	            	dispatchEvent(DOMEvent.XFORMS_SUBMIT_DONE);
	                                // TODO: errorrsdispatchEventSerially(Event.XFORMS_SUBMIT_ERROR);
	                			}
	                		}
	                		);
	    		}
	    		else
	    		{
	    	        //#debug info
	    	        System.out.println("****************** Closing submission (replace other) " + getAttribute("id"));
	    			dispatchEventSerially(DOMEvent.XFORMS_SUBMIT_DONE);
	    		}
	    	} else {
	            //#debug info
	            System.out.println("****************** Closing submission (no replace) " + getAttribute("id"));
                dispatchEventSerially(DOMEvent.XFORMS_SUBMIT_DONE);
            }
    	}
    	catch (Throwable e)
    	{
    		logWarning("h1",e);
    	}
    	finally
    	{
	    	if (reply!=null)
				try {
					//logStatus("Closing HTTP reply.");
					reply.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logWarning("h2",e);
				}
    	}
    }


    public void handleAndCloseReply(Document result)
    {
    	try
    	{
	    	Attr replaceAttr = this.getAttributeNode("replace");
	    	if (replaceAttr!=null)
	    	{
	    		if (replaceAttr.getValue().equals("instance"))
	    		{
	    			final Document doc = result;
	                ((XFormsDocument)getOwnerDocument()).callSerially(
	                		//task
	                		new Runnable()
	                		{
	                			public void run()
	                			{
	            	    			replaceInstance(doc);
	            	            	dispatchEvent(DOMEvent.XFORMS_SUBMIT_DONE);
	                                // TODO: errorrsdispatchEventSerially(Event.XFORMS_SUBMIT_ERROR);
	                			}
	                		}
	                		);
	    		}
	    		else
	    		{
	    			dispatchEventSerially(DOMEvent.XFORMS_SUBMIT_DONE);
	    		}
	    	} else {
                dispatchEventSerially(DOMEvent.XFORMS_SUBMIT_DONE);
            }
    	}
    	catch (Throwable e)
    	{
    		logWarning("h1",e);
    	}
    }
    
    
	private Document loadXMLDocument (InputStream is, String enc) {
		
		Document doc = new Document();
		KXmlParser parser = new KXmlParser();
		if (is == null) {
			logWarning("File not found when trying to read XML stream: " );
			return doc;
		}
		try {
			parser.setInput(is,enc);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			logWarning("l0",e);
		}
		try {
			//this.logStatus("Starting to parse doc");
			doc.parse(parser);
			//this.logStatus("Doc parsed");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logWarning("l1",e);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			logWarning("l2",e);
		} catch (Throwable t)
		{
			logWarning("l3",t);
		}
		return doc;
	}

    void replaceInstance(Document doc)
    {
		Attr rewireAttr = this.getAttributeNode("rewire");
		Attr rebuildAttr = this.getAttributeNode("rebuild");
		final boolean rewire = (rewireAttr==null?true:rewireAttr.getValue().equals("true"));
		final boolean rebuild = (rebuildAttr==null?true:rebuildAttr.getValue().equals("true"));

        XFormsModel model = ((XFormsDocument)getOwnerDocument()).getModel();
        String instName = getAttribute("instance");
        //#debug info
        System.out.println("Submission " + getAttribute("id") + ": Replacing instance: " + instName);
        Instance inst = model.getInstance(instName == "" ? null : instName);
        if (rewire) model.beforeInstanceStructureChange(XFormsModel.SUBMISSION,inst,inst.getDocument().getDocumentElement());
        model.replaceInstance(doc,inst.getID());
        if (rewire) model.instanceStructureChanged(XFormsModel.SUBMISSION, inst, inst.getDocument().getDocumentElement());
    }

/*
    EXPORT_C void CSubmission::SubmissionFinished(TSubReason aReason)
    {
        if (iSubmitter!=NULL) delete iSubmitter;
        iSubmitter=NULL;
    }
*/



    public short acceptNode(Node node) {
        return NodeFilter.ACCEPT; // Accept everything for now
        // Use below code for skipping non-relevant nodes
/*
        int type = node.getNodeType();
        if (node.getUserData() != null) {
            if (type == Node.ELEMENT_NODE || type == Node.ATTRIBUTE_NODE) {
                InstanceItem item = (InstanceItem) node.getUserData();
                if (!(item.getBooleanState(MIPExpr.RELEVANT))) {
                    return NodeFilter.REJECT;
                }
            }
        }

        return NodeFilter.ACCEPT;
*/
    }
        
    public void defaultAction(DOMEvent evt)
    {
        if (evt.getType() == DOMEvent.XFORMS_SUBMIT) {
            startSubmission();
        }
    }
    
}
