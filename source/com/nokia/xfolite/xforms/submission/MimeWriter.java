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

import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.NodeFilter;

public class MimeWriter {

//	 ---------- class CMultipartRelatedWriter : public CBase

//	 the first one is doubly- defined in the recognizer as well
	public static String KPackageMimeType="multipart/related";
	public static String KXFormMimeType="text/plain";


//////////////////////////////////////////////////	/


	/**
	 * User declaration 
	 */
	public static String KPackageMime="MIME-Version: 1.0";
	public static String KPackageContentType="multipart/related; type=application/xml; start=\"<formdata@example.com>\"";
	public static String KPackageHeaderContentDisposition="Content-Disposition: form-data; ";
	public static String KPackageHeaderContentType="Content-Type: ";
	public static String KPackageHeaderFileName=("FileName: ");
	public static String KPackageHeaderStartContentID=("Content-ID: formdata@example.com");
	public static String KPackageHeaderContentID=("Content-ID: ");
	public static String KPackageHeaderContentLength=("Content-Length: ");
	public static String KPackageHeaderContentTransferBin=("Content-Transfer-Encoding: binary");
	public static String KPackageLineEnd=("\r\n");
	public static String KPackageBoundary=("ThisistheboundaryXYZ");
	public static String KPackageBoundaryBegin="--"+KPackageBoundary;
	public static String KPackageBoundaryFinalEnd="--"+KPackageBoundary+"--"+KPackageLineEnd;
	public static String KPackageBoundaryEnd="--"+KPackageBoundary+"-\r\n";
	public static String KPackageTagBegin=("<");
	public static String KPackageTagEnd=(">");
	public static String KFormMimeType=("application/xml; charset=UTF-8");
	public static String KFormContentId=("formdata@example.com");
	public static String KBinaryTransferEncoding=("Content-Transfer-Encoding: binary");
	
	OutputStream iStream;

	MimeWriter(OutputStream aStream)
	{
		iStream=aStream;
	};
//	EXPORT_C void setBoundary(TString boundary);
	public void writeHeaders(String start, String type) throws IOException
	{
		if (iStream!=null)
		{
			// TODO: write start and type, maybe other parameters are needed as well?
		    //Write(KPackageMime);
		    //Write(KPackageLineEnd);
		    Write(KPackageContentType);
		    Write(KPackageLineEnd);
		    this.iStream.flush();
	    }
		
	}
	public void writeXML(Node aRoot, String acontentid, String amimetype,NodeFilter filter) throws IOException // TODO node filter, MNodeFilter *nodefilter)
	{
		if (iStream!=null)
		{
			//TODO: create mimetype string and contentid string
		    Write(KPackageLineEnd);
			Write(KPackageBoundaryBegin);
		    Write(KPackageLineEnd);

		    Write(KPackageHeaderContentType);
		    Write(KFormMimeType);
		    Write(KPackageLineEnd);

		    Write(KPackageHeaderContentID);
		    Write(KPackageTagBegin);
		    Write(KFormContentId);
		    Write(KPackageTagEnd);
		    Write(KPackageLineEnd);
		    Write(KPackageLineEnd);
		    XFormsXMLSerializer xmlser = new XFormsXMLSerializer();
		    xmlser.serialize(iStream, (Element)aRoot, filter); 
		    //Write(KPackageLineEnd);
		    this.iStream.flush();
		}
	}// TODO serialization options


	int Write(byte[] aBuffer) throws IOException
	{
		if (iStream!=null)
		{
			iStream.write(aBuffer);
			return 0;
		}
		return -1;
	}
	
	int Write(String aString) throws IOException 
	{
		this.Write(aString.getBytes());
		return -1;
	}

	void writeBinary(byte[] buffer, String mimetype, String contentid) throws IOException
	{
		if (iStream!=null)
		{
		    Write(KPackageLineEnd);
			Write(KPackageBoundaryBegin);
		    Write(KPackageLineEnd);

		    Write(KPackageHeaderContentType);
		    Write(mimetype);
		    Write(KPackageLineEnd);
			Write(KBinaryTransferEncoding);
		    Write(KPackageLineEnd);
		    Write(KPackageHeaderContentID);
		    Write(KPackageTagBegin);
		    Write(contentid);
		    Write(KPackageTagEnd);
		    Write(KPackageLineEnd);
		    Write(KPackageLineEnd);
		    Write(buffer);
		    //Write(KPackageLineEnd);
		    this.iStream.flush();
		}
		
	}
	
	void writeBinaryFormData(byte[] buffer, String mimetype, String filename, String elemName) throws IOException
	{
		if (iStream!=null)
		{
		    //Write(KPackageLineEnd);
			Write(KPackageBoundaryBegin);
		    Write(KPackageLineEnd);

		    Write(KPackageHeaderContentDisposition);
		    Write("name=\"");
		    Write(elemName);
		    Write("\"");
		    if (filename!=null)
		    {
		    	Write("; filename=\"");
			    Write(filename);
			    Write("\"");
		    }
		    Write(KPackageLineEnd);
		    if (mimetype!=null)
		    {
			    Write(KPackageHeaderContentType);
			    Write(mimetype);
			    Write(KPackageLineEnd);
		    }
			//Write(KBinaryTransferEncoding);
		    //Write(KPackageLineEnd);
		    //Write(KPackageHeaderContentID);
		    //Write(KPackageTagBegin);
		    //Write(contentid);
		    //Write(KPackageTagEnd);
		    //Write(KPackageLineEnd);
		    Write(KPackageLineEnd);
		    Write(buffer);
		    Write(KPackageLineEnd);
		    this.iStream.flush();
		}
		
	}

	public static String getFormDataContentType()
	{
		return "multipart/form-data; boundary="+KPackageBoundary;
	}
    void writeProlog() throws IOException
	{
		// NONE THAT I KNOW OF...
		    this.iStream.flush();
	}
    void writePrologFormData() throws IOException
	{
		Write(KPackageBoundaryFinalEnd);
		    this.iStream.flush();
	}

	void closeStream() throws IOException
	{
		if (iStream!=null)
		{
			iStream.close();
		}
	}
}
