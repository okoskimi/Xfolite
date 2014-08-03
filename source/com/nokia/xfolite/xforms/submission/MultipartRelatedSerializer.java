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

package com.nokia.xfolite.xforms.submission;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.nokia.xfolite.xforms.dom.SubmissionElement;
import com.nokia.xfolite.xforms.model.BinaryAttachment;
import com.nokia.xfolite.xforms.model.InstanceItem;
import com.nokia.xfolite.xforms.model.XFormsModel;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;
public class MultipartRelatedSerializer implements ISerializer {

    public MultipartRelatedSerializer() {
        super();
        // TODO Auto-generated constructor stub
    }

    public boolean canHandle(SubmissionElement submission) {
        String action = submission.getAttribute("action"); 
        String method = submission.getAttribute("method"); 
        if (method == "multipart-post" || method == "multipart-put")
        {
            return true;
        }
        return false;
    }


    public String getContentType() {
    	// TODO this only works now for FALSE,TRUE, since it just sends the predefined string
    	return MimeWriter.KPackageContentType;
    }
    public String GetContentTypeExtension(String aExtName)
    {
    	if (aExtName.equals("boundary"))
    	{
    		 return (MimeWriter.KPackageBoundary);
    	}
    	else if (aExtName.equals("start"))
    	{
    		return (MimeWriter.KFormContentId);
    	}
    	return null;
    }
    
    public static String CIDStart = "cid:x";
    public static String NoCIDStart = "x";
    public static String CIDEnd = "@example.com"; // this failed in XML, produced cid:x1234&#64;example.com
    //public static String CIDEnd = "-at-example.com";

    public void serialize(OutputStream stream, Element aRoot,
            SubmissionElement aSubmission, XFormsModel aModel)
        throws IOException
    {
    /* if stream is null, returns a TPtrC8 pointer added to the cleanupstack. 
     * if stream is non-nul serializes to the stream*/
    //EXPORT_C TPtrC8 CMultipartRelatedSerializer::SerializeInternalLC(MOutputStream* stream, TNode aRoot, CSubmission *aSubmission,CModel* aModel)
    //{
    	//String KFormatWCID=("cid:x%d@example.com");
    	//String KFormatNoCID=("x%d@example.com");
    	Vector  addedNodes= new Vector();
    	MimeWriter mpWriter = new MimeWriter(stream);
    	// TODO: Go through document and add cid: elements	
    	Hashtable attachments = aModel.getBinaryAttachments();
    	if (attachments!=null&&attachments.size()>0)
    	{
	    	Enumeration attenum = attachments.elements();
	    	while (attenum.hasMoreElements())
	    	//for (int i=0;i<aModel->GetBinaryAttachmentsLength();i++)
	    	{
	    		BinaryAttachment attachment = (BinaryAttachment)attenum.nextElement();
	    		if (attachment!=null)
	    		{
	    			InstanceItem item = attachment.GetInstanceItem();
	    			Node node = item.getNode();
	    			//TString tstringVal="cid:xyz";
	    			// TODO : tgt.Format(KFormatWCID,attachment);//generates:
	    			//TString tstringVal; 
	    			//tstringVal.SetL(tgt);
	    			node.setText(CIDStart+attachment.hashCode()+CIDEnd);
	    			addedNodes.addElement(node);
	    		}
	    	}
    	}
    	String contentid="dummycontentid";
    	String mimetype="application/xml; charset=UTF-8";
    	
    	//mpWriter->writeHeaders("Start",mimetype); The content-type should be set in the HTTP request, not here

    	mpWriter.writeXML(aRoot,contentid,mimetype,aSubmission.getNodeFilter());
    	for (int i=0;i<addedNodes.size();i++)
    	{
    		InstanceItem item = aModel.getInstanceItemForNode((Node)addedNodes.elementAt(i));
    		if (item!=null)
    		{
    			BinaryAttachment attachment = item.getBinaryAttachment();
    			if (attachment!=null)
    			{
    				String anothermimetype = attachment.GetMime();
    				String filelink = attachment.GetFileLink();
    				// MH debug
    				byte[] buffer = attachment.GetBuffer();
    				//byte[] buffer = "This is a test file\nAnother line\n".getBytes();
    				//tgt.Format(KFormatNoCID,attachment);//generates:
    				//TString tstringVal; 
    				//tstringVal.SetL(tgt);
    				String tstringVal = NoCIDStart+attachment.hashCode()+CIDEnd;
    				mpWriter.writeBinary(buffer,anothermimetype==null?"empty/mime":anothermimetype,tstringVal);
    			}
    		}

    	}
    	// TODO: Go through document, and write all binary attachments
    	//mpWriter->writeBinary(buffer,length,contentid,mimetype);
    	mpWriter.writeProlog();
    	//mpWriter->closeStream();
    	//CleanupStack::PopAndDestroy(mpWriter);

        // remove expanded attachments
        //TString emptyStr = "";
        for (int i = 0;i< addedNodes.size();i++)
        {
        	Node n = (Node)addedNodes.elementAt(i);
    		if (n.getNodeType()==Node.ELEMENT_NODE||n.getNodeType()==Node.ATTRIBUTE_NODE)
    		{
    				n.setText("");
    		}
        }

    	//addedNodes.Close();
    	
    	//TODO: Go through document and undo cid: elements
    	//return TPtrC8();
    }

	public ISerializer cloneSerializer() {
		// TODO Auto-generated method stub
		return new MultipartRelatedSerializer();
	}


}
