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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import com.nokia.xfolite.client.util.CookieJar;
import com.nokia.xfolite.xforms.dom.SubmissionElement;
import com.nokia.xfolite.xforms.dom.XFormsDocument;
import com.nokia.xfolite.xforms.model.XFormsModel;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.events.DOMEvent;


class HTTPSubmitterThread //extends Thread 
implements Runnable
{
    SubmissionElement submission;
    ISerializer serializer;
    Element root;
    XFormsModel model;
    HttpConnection c = null;
    OutputStream os = null;
    byte[] barray=null;
    long startTime =-1;
    public boolean alive=false;
    HTTPSubmitterThread(SubmissionElement submission,
            ISerializer serializer,
            Element root,
            XFormsModel model,
            byte[] abarray
            ) {
        this.submission = submission;
        this.serializer = serializer;
        this.root = root;
        this.model = model;
        this.barray=abarray;
    	alive=true;
    }
    public boolean isAlive()
    {
    	return this.alive;
    }
    /*
    */
    public void run() {
        //submission.logStatus("Starting run().");
        String action = submission.getAttribute("action");
        String baseURL = ((XFormsDocument) submission.getOwnerDocument()).getBaseURL();
        if (baseURL != null && !(action.startsWith("http:") || action.startsWith("https:"))) {
        	action = action.trim();
        	if (action.startsWith("/")) {
        		action = baseURL + action.substring(1);
        	} else {
        		action = baseURL + action;
        	}
        }
        //#debug info
        System.out.println("HTTP submission to: " + action);
        try {
        	CookieJar jar = CookieJar.getInstance();
        	String cookie = jar.getCookie(action);
        	this.startTime=System.currentTimeMillis();
            c = (HttpConnection) Connector.open(action);
            // Set the request method and headers
            c.setRequestMethod(HttpConnection.POST);
            c.setRequestProperty("Host", c.getHost() + ":" + c.getPort());
            // c.setRequestProperty("User-Agent", "Java.Net Wa-Wa 2.0");
            //c.setRequestProperty("User-Agent", "Profile/MIDP-2.0 Configuration/CLDC-1.1");
            c.setRequestProperty("Content-Length", "" + barray.length);
            String contentType=serializer.getContentType();
            c.setRequestProperty("Content-Type", contentType);
            c.setRequestProperty("Connection", "close");
            c.setRequestProperty("SOAPAction", "");
            if (cookie != null) {
            	c.setRequestProperty("Cookie", cookie);
            }
            
            os = c.openOutputStream();
            os.write(barray);
            os.flush();
            if (os != null) {
                try {os.close(); os=null;} catch (Throwable ignore) {}
            }  
            if (c.getResponseCode() == HttpConnection.HTTP_OK ||
                    c.getResponseCode() == HttpConnection.HTTP_ACCEPTED) {
	          	  int i = 0;
	        	  String headerKey;
	        	  while((headerKey = c.getHeaderFieldKey(i)) != null) {
	        		  //#debug info
	        		  System.out.println("Got header " + headerKey);
	        		  if (headerKey.toLowerCase().equals("set-cookie")) {
	        			  jar.setCookie(c.getHeaderField(i));
	        		  }
	        		  i++;
	        	  }
                InputStream is= c.openInputStream();
                //submission.logStatus("Starting is read.");
                is=this.printInputStream(is); // for debugging, comment this line out in production
                if (is!=null)
                {
	                //submission.logStatus("Read is.");
	    			submission.handleAndCloseReply(is,"UTF-8");
                }
                else
                {
	                submission.logWarning("Is null.");
                    submission.dispatchEventSerially(DOMEvent.XFORMS_SUBMIT_ERROR);
                }
            } else {
                submission.logWarning("-------Server error"+c.getResponseCode()+" : "+c.getResponseMessage());
                // DEBUG REMOVE this
                //try { printInputStream(c.openInputStream()).close(); } catch (Throwable t) {}
                submission.dispatchEventSerially(DOMEvent.XFORMS_SUBMIT_ERROR);
            }
        } catch (Throwable ex) {
            submission.logWarning("h0",ex);
            submission.dispatchEventSerially(DOMEvent.XFORMS_SUBMIT_ERROR);
        } finally {
            serializer=null;
            root=null;
            model=null;
            barray=null;
            if (os != null) {
                try {os.close();os=null;  } catch (Throwable ignore) {}
            }  
            if (c != null) {
                try {
                	c.close();
                	//submission.logStatus("Closed HTTP connection: "+c); c=null;
                	} catch (Throwable ignore) {
                		submission.logWarning("err h1",ignore);
                	}
            }
            submission=null;
            alive=false;
        }        
    }
    
    /** this is a debug method that should not be used in production
     * this method is used for debugging the thread hang issue when network
     * coverage is down */
    private InputStream getCachedInputStream(InputStream is) throws IOException
    {
    	try
    	{
		    StringBuffer sb = new StringBuffer("");
		    int ch;
		    InputStreamReader isr = new InputStreamReader(is);//, "UTF-8");
		    while ((ch = isr.read()) != -1) {
		        sb.append((char) ch);
		        //System.err.print((char)ch);
		        //System.err.flush();
		    }
		    is.close();
		    return new ByteArrayInputStream(sb.toString().getBytes());
    	} catch (IOException t0)
    	{
    		submission.logWarning("ss0",t0);
    		throw t0;
    	} catch (Throwable t)
    	{
    		submission.logWarning("ss1",t);
    	}
    	finally
    	{
			try{
				is.close();
			} catch (Throwable t2)
			{
	    		submission.logWarning("ss2",t2);
				
			}
    	}
    	return null;
    }
    
    /** this is a debug method that should not be used in production */
    private InputStream printInputStream(InputStream is)
    {
    	try
    	{
		    System.err.println("-------Server result");
		    StringBuffer sb = new StringBuffer("");
		    int ch;
		    InputStreamReader isr = new InputStreamReader(is, "UTF-8");
		    while ((ch = isr.read()) != -1) {
		        sb.append((char) ch);
		        //System.err.print((char)ch);
		        //System.err.flush();
		    }
		    String reply=sb.toString();
		    System.err.println(reply);
		    is.close();
		    return new ByteArrayInputStream(reply.getBytes());
    	} catch (Throwable t)
    	{
    		System.err.println(t.toString());
    	}
    	return null;
    }


}

public class HTTPSubmitter implements ISubmitter {

    public HTTPSubmitter() {
        super();
    }
    
    public ISubmitter cloneSubmitter()
    {
    	return new HTTPSubmitter();
    }
    
    HTTPSubmitterThread submitterThread=null;

    public boolean canHandle(SubmissionElement submission) {
        String action = submission.getAttribute("action"); 
        String method = submission.getAttribute("method").toLowerCase(); 
        if (
            method == "put" || 
            method == "get"  ||
            method == "post" ||
            method == "multipart-post" ||
            method == "urlencoded-post" ||
            method == "form-data-post")
        {
            if (action.startsWith("http:") || action.startsWith("https:")) {
                return true;
            }
        }
        return false;
    }

    public void startSubmission(SubmissionElement submission, ISerializer serializer,
           Element root, XFormsModel model) {
    	        String action = submission.getAttribute("action");
    	        byte[] barray = null;
    	        if (this.isOngoing()) return;
    	        try {
    	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	            // it is important, that the serialization is done in this cheetah thread (and not the launched)
    	            serializer.serialize(baos, root, submission, model);
    	            // I think this has to be changed so that it is not constructed in memory., could take too much memory, since now there are also attachments.
    	            barray = baos.toByteArray();
    	            // there can be binary System.out.write(barray);
    	            // establish Connection and upload data

    	        } catch (Throwable ex) {
    	            submission.logWarning("h7",ex);
    	            submission.dispatchEventSerially(DOMEvent.XFORMS_SUBMIT_ERROR);
    	        } finally {
    	            //if (os != null) {
    	            //    try {os.close(); } catch (Throwable ignore) {}
   	        		this.submitterThread = new HTTPSubmitterThread(submission, serializer, root, model,barray);
   	        		((XFormsDocument)submission.getOwnerDocument()).callParallel(this.submitterThread);
   	        		//submission.getModel().
    	            //this.submitterThread.start();
    	            }
    	        }        
    	        
    	public boolean isOngoing() {
		// TODO Auto-generated method stub
    	if (this.submitterThread!=null)
    	{
    		if (this.submitterThread.isAlive())
    		{
    			long diff = System.currentTimeMillis()-this.submitterThread.startTime;
    			if (this.submitterThread.submission!=null) 
    				this.submitterThread.submission.logWarning("Subm has been secs:"+diff/1000);
    			return true;
    		}
    	}
    	return false;
	}

}
