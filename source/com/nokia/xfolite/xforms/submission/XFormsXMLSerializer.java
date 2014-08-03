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

import org.kxml2.io.KXmlSerializer;

import com.nokia.xfolite.xforms.dom.SubmissionElement;
import com.nokia.xfolite.xforms.model.XFormsModel;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.NodeFilter;
public class XFormsXMLSerializer implements ISerializer {

    public XFormsXMLSerializer() {
        super();
        // TODO Auto-generated constructor stub
    }

    public boolean canHandle(SubmissionElement submission) {
        String action = submission.getAttribute("action"); 
        String method = submission.getAttribute("method"); 
        if (method == "multipart-post" || method == "multipart-put"
            || method == "form-data-post" || method == "form-data-put"
            || method == "GET" || method == "get"
            || method == "puturlenc" || method == "PUTURLENC"
            )
        {
            return false;
        }
        return true;
    }

    public void serialize(OutputStream stream, Element root,
            SubmissionElement submission, XFormsModel model)
        throws IOException
    {
    	this.serialize(stream, root, submission);
    }
    
    public void serialize(OutputStream stream, Element root,
            NodeFilter filter)
        throws IOException
    {
        KXmlSerializer serializer = new KXmlSerializer();
        serializer.setOutput(stream, "UTF-8");
        root.getOwnerDocument().write(serializer, root, filter);

    }

    public String getContentType() {
        return "application/xml";
    }

	public ISerializer cloneSerializer() {
		// TODO Auto-generated method stub
		return new XFormsXMLSerializer();
	}

}
