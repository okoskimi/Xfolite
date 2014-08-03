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
public class MultipartFormDataSerializer implements ISerializer {

    public MultipartFormDataSerializer() {
        super();
        // TODO Auto-generated constructor stub
    }

    public boolean canHandle(SubmissionElement submission) {
        String action = submission.getAttribute("action"); 
        String method = submission.getAttribute("method"); 
        if (method == "form-data-post" || method == "form-data-put")
        {
            return true;
        }
        return false;
    }


    public String getContentType() {
    	// TODO this only works now for FALSE,TRUE, since it just sends the predefined string
    	return MimeWriter.getFormDataContentType();
    }
    public String GetContentTypeExtension(String aExtName)
    {
    	return null;
    }
    

    public void serialize(OutputStream stream, Element aRoot,
            SubmissionElement aSubmission, XFormsModel aModel)
        throws IOException
    {
    	MimeWriter mpWriter = new MimeWriter(stream);
    	this.serializeInternal(stream,aRoot,aSubmission,aModel,mpWriter);
    	//mpWriter->writeBinary(buffer,length,contentid,mimetype);
    	mpWriter.writePrologFormData();
    }
    protected void serializeInternal(OutputStream stream, Element curr,
                SubmissionElement aSubmission, XFormsModel aModel,MimeWriter mpWriter)
            throws IOException
        {
    	
		InstanceItem item = aModel.getInstanceItemForNode(curr);
		BinaryAttachment attachment = item.getBinaryAttachment();
		if (attachment!=null)
		{
			String anothermimetype = attachment.GetMime();
			String filelink = attachment.GetFileLink();
			byte[] buffer = attachment.GetBuffer();
			String filename = attachment.GetFileName();
			mpWriter.writeBinaryFormData(buffer,anothermimetype==null?"empty/mime":anothermimetype,filename,curr.getLocalName());
		}
		else
		{
			String textContent="";
			boolean include=false;
			boolean hasChildElement=false;
			Node child = curr.getFirstChild();
			while (child!=null)
			{
				if (child.getNodeType()==Node.TEXT_NODE)
				{
					textContent+=child.getNodeValue();
					include=true;
				}
				if (child.getNodeType()==Node.ELEMENT_NODE)
				{
					hasChildElement=true;
					include=false;
					textContent="";
					this.serializeInternal(stream, (Element)child, aSubmission, aModel, mpWriter);
				}
				child=child.getNextSibling();
			}
			if (hasChildElement==false&&include==true)
			{
				String anothermimetype = "text/plain";
				InstanceItem it = aModel.getInstanceItemForNode(curr);
				byte[] buffer=it.getStringValue().getBytes();
				mpWriter.writeBinaryFormData(buffer,null,null,curr.getLocalName());
			}
		}
    }

	public ISerializer cloneSerializer() {
		// TODO Auto-generated method stub
		return new MultipartFormDataSerializer();
	}


}
