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

package com.nokia.xfolite.client;

import java.util.Hashtable;
import java.util.Calendar;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParserException;

import de.enough.polish.ui.*;
import de.enough.polish.util.ArrayList;
import de.enough.polish.util.Locale;






import com.nokia.xfolite.client.ui.*;
import com.nokia.xfolite.client.util.CookieJar;
import com.nokia.xfolite.client.util.EventQueue;
import com.nokia.xfolite.client.util.ProtocolFactory;
import com.nokia.xfolite.client.util.ThreadPool;
import com.nokia.xfolite.xforms.dom.*;
import com.nokia.xfolite.xforms.model.*;
import com.nokia.xfolite.xforms.model.datasource.*;
import com.nokia.xfolite.xforms.model.datatypes.*;
import com.nokia.xfolite.xforms.submission.*;
import com.nokia.xfolite.xml.dom.*;
import com.nokia.xfolite.xml.dom.events.*;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.nokia.xfolite.xml.xpath.XPathResult;

import java.io.*;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Graphics;


/**
 * <p>Provides an XForms form.</p>
 *
 * @author Oskari Koskimies
 */
public class XFormsForm extends Form implements UserInterface, CommandListener   {
	
	private XFormsDocument m_doc = null;
    private Display m_display;
    private PolishController m_controller = null;
    // Listener for non-exec commands
    private CommandListener m_listener = null;
    private ExecCommand m_exitCommand;
	private MIDlet m_midlet;
	private Hashtable properties = null;
    private String m_title;
    private ParseListener parseListener = null;

    private final Command m_memoryCommand = new Command(
    		Locale.get("forms.cmd.memory"), Command.SCREEN, 10 );
    
	/**
	 * Creates a new XForms-based form.
	 * 
	 * @param title The default title of the frame (will be overwritten by title element in form if present)
	 * @param display The Display object for this midlet (required for access to callSerially)
	 * @param style The style for this form, is applied using the #style preprocessing directive
	 */
    public XFormsForm(String defaultTitle, ExecCommand exitCommand, Display display) {
        this(defaultTitle, exitCommand, display, null);
        m_title = defaultTitle;
    }
    
    public XFormsForm(String defaultTitle, ExecCommand exitCommand, Display display, Style style ) {
		//#if polish.usePolishGui
			//# super( defaultTitle, style );
		//#else
			super( defaultTitle );
		//#endif
		m_display = display;
        m_doc = new XFormsDocument();
        m_exitCommand = exitCommand;
        if (m_exitCommand != null) {
            this.addCommand(m_exitCommand);
        }
        this.addCommand(m_memoryCommand);
    }
    
    public void setParseListner(ParseListener listener, int callbackInterval) {
    	m_doc.setParseListener(listener, callbackInterval);
    }
    
    public void setMidlet(MIDlet midlet) {
        this.m_midlet = midlet;
    }
    
    public void setProperties(Hashtable properties) {
    	this.properties = properties;
    }
    
    public String getProperty(String name) {
   		//#debug 
		System.out.println("Asking for property " + name);

    	String value = null;
    	if (properties != null) {
    		value = (String) properties.get(name);
    		//#debug 
    		System.out.println("Got from properties: " + value);
    	}
    	if (value == null && m_midlet != null) {
    		value = m_midlet.getAppProperty(name);
    		//#debug 
    		System.out.println("Got from midlet: " + value);    		
    	}
    	if (value == null) {
    		value = System.getProperty(name);
    		//#debug 
    		System.out.println("Got from system: " + value);
    	}
		//#debug 
		System.out.println("Returning: " + value);

    	return value;
    }
    
    public void setTitle(String title) {
        super.setTitle(title);
        m_title = title;
    }
    
    public void showMessage(String msg) {
		//#debug info
		System.out.println("Showing message: " + msg);
		//#style xformsmessage
		Alert alert = new Alert(Locale.get("forms.label.message"),
				msg, null, AlertType.INFO);
		// Looks like an error but builds
		m_display.setCurrent(alert, this);
		// FIXME: Implement proper popup
		/*
		 * final Popup popup = new Popup(Pic.get("/info.png"),
		 * "Message", msg); popup.setCommands(this, Popup.OK, null);
		 * Screen.get().push(popup);
		 */
    }
    
    public void close() {
        if (m_exitCommand != null) {
            m_exitCommand.execute(null, this);
        }
    }
    
    public void load(String url) {
        try
        {
          //#style xform
          XFormsForm form = new XFormsForm(
        		  Locale.get("main.label.loading"), m_exitCommand, m_display);
          form.setBaseURL(url);
          //form.setMidlet(this);
          form.setProperties(this.properties);
          m_display.setCurrent( form );          
          
          StreamConnection connection;
    	
    	//#debug info
        System.out.println("Loading form: " + url);
        connection = ProtocolFactory.getInstance().getConnection(url);
        
        InputStream in = connection.openInputStream();
        if (connection instanceof HttpConnection) {
      	  int i = 0;
      	  HttpConnection hc = (HttpConnection) connection;
      	  CookieJar jar = CookieJar.getInstance();
      	  String headerKey;
      	  while((headerKey = hc.getHeaderFieldKey(i)) != null) {
      		  //#debug info
      		  System.out.println("Got header " + headerKey);
      		  if (headerKey.toLowerCase().equals("set-cookie")) {
      			  jar.setCookie(hc.getHeaderField(i));
      		  }
      		  i++;
      	  }
        }
        form.loadDocument(in);
        try {
            in.close();
            connection.close();
        } catch (Exception ignore) {} // We don't care too much if closing fails
        //#debug info
        System.out.println("Form loaded");
        
      } catch (Exception ex) {
          //#debug error
          System.out.println(
        		  Locale.get("forms.error.errorWhileLoadingForm") + ": " + ex);
          Form form = new Form(Locale.get("main.label.loading"));
          StringItem error = new StringItem(ex.getClass().getName(), ex.getMessage());
          form.append(error);
          form.addCommand(m_exitCommand);
          m_display.setCurrent( form );
          form.setCommandListener(this);
      }
    }
    
    public void loadDocument(InputStream form)
        throws IOException, XmlPullParserException
    {        
        try {
            setBusy(true);
            long time = System.currentTimeMillis();
            System.out.println("formview.enter()"+time);
    
            KXmlParser parser = new KXmlParser();
            parser.setInput(form, "UTF-8");
            Container root = new Container(false);
            this.deleteAll();
            this.append(root);
            m_controller = new PolishController(root, m_display, this);
            setItemStateListener(m_controller);
            super.setCommandListener(this);
            //#debug info
            System.out.println("Parsing form..");
            m_doc.setRendererFactory(m_controller.getWidgetFactory());
    //            doc.registerDataSource(new SimulatedDataSource()); // for demo purposes
    //            doc.registerDataSource(new GPSDataSource(MainView._gpsDevice,this,true)); // for demo purposes
            m_doc.setUserInterface(this);
            //doc.setStoreProvider(this);
            //doc.registerEventProvider("gps", this);
            m_doc.addSubmitter(new HTTPSubmitter());
            m_doc.addSerializer(new XFormsXMLSerializer());
            m_doc.addSerializer(new MultipartRelatedSerializer());
            m_doc.addSerializer(new MultipartFormDataSerializer());
            //doc.addSubmitter(this);
            m_doc.parse(parser);
            //#debug info
            System.out.println("Form construct took: "+(System.currentTimeMillis()-time)+"ms");

        } catch(IOException ex1) {
            throw ex1;
        } finally {
            setBusy(false);
        }
    }
    
    public void loadDocument(InputStream form, InputStream data, String dataId)
        throws IOException, XmlPullParserException
    {
        addData(data, dataId);
        loadDocument(form);
        
    }
    
    
    public void addData(InputStream data, String dataId)
        throws IOException, XmlPullParserException
    {
        KXmlParser parser = new KXmlParser();
        parser.setInput(data, "UTF-8");
        Document dataDoc = new Document();
        dataDoc.parse(parser);
        addData(dataDoc, dataId);
    }
    
    public void addData(Document data, String dataId) {
        if (data != null && dataId != null) {
            m_doc.addInstance(data, dataId);
        } else {
            throw new NullPointerException("Both document and data-ID must be non-null!");
        }
    }
    
    public void setBaseURL(String url) {
    	m_doc.setBaseURL(url);
    }
    
    public void loadDocument(InputStream form, Document data, String dataId)
        throws IOException, XmlPullParserException
    {
        addData(data, dataId);
        loadDocument(form);
    }

    public void addSubmitter(ISubmitter submitter) {
        m_doc.addSubmitter(submitter);
    }
    
    public void registerDataSource(DataSourceFactory fact) {
        m_doc.registerDataSource(fact);
    }
    
    public void addSerializer(ISerializer serializer) {
        m_doc.addSerializer(serializer);
    }

    public XFormsDocument getDocument() {
        return m_doc;
    }

    public Container getRootContainer() {
        return this.container;
    }
    
    private int busyCount = 0;
    public synchronized void setBusy(boolean busy) {
        //#debug
        System.out.println("******************************************* SETBUSY: " + busy);
        if (busy) {
            if (++busyCount == 1) {
                super.setTitle(Locale.get("forms.label.processing"));
                this.serviceRepaints();
            }
        } else {
            if (--busyCount == 0) {
                super.setTitle(m_title);
                this.serviceRepaints();
            }
        }
    }
   
    public void setContext(String context) {
/*  Do not use context for now, takes up screen space and is not too useful
 *
        if (context != null) {
            //#style context
            StringItem subTitle = new StringItem(null, context);
            setSubTitle(subTitle);
        } else {
            setSubTitle(null);
        }
 */
    }
    
    /********************** UserInterface Interface Implementation *******************/     
    
    private String setLogLocation(int lvl, String msg, Element el)
    {
        String location = "<" + el.getNodeName();
        // TODO': mh change, if id attribute exist only print it
        Attr idAttr=el.getAttributeNode("id");
        if (idAttr!=null)
            location+=" id=\""+idAttr.getNodeValue()+"\" ";
        else
        {
            // mh change, get only the first attribute
            if (el.getAttributeCount()>0)
            for (int i = 0; i < 1 /*el.getAttributeCount()*/; i++) {
                Attr a = el.getAttribute(i);
                location += " " + a.getNodeName() + "=" + "\"" + a.getValue() + "\"";
            }
        }
        location += "/>";
        return location;
    }
    
    // log messages:
    public void log(int lvl, String msg, Element el) {
        String location = setLogLocation(lvl, msg, el);
        switch (lvl) {
        case UserInterface.LVL_ERROR:
            //#debug error
            System.out.println(
            		Locale.get("forms.error.errorAt")
            		+ ": " + location + ":" + msg);
            break;
        case UserInterface.LVL_WARN:
            //#debug warn
            System.out.println("Warning at " + location + ":" + msg);
            break;
        case UserInterface.LVL_STATUS:
            //#debug info
            System.out.println(location + ":" + msg);
            break;
        }
    }
    
    public void callSerially(Runnable task) {
        // should check if series 60 3rd edition, and just call dispatch
        if (true /* S60 3rd ed */) {
            m_display.callSerially(task); // for S60 3rd edition phones (E70)
        } else {
            EventQueue.getInstance().callSerially(task, m_display); // for 2.8 phones (6680)
        }
    }
    
    public void callParallel(Runnable task) {
        // In series 60 3rd edition we could actually launch a new thread here.
        // new Thread(task).start();
        ThreadPool.getInstance().callParallel(task);
    }
    
    
    /************************ CommandListener ***************************/    

    public void commandAction(Command cmd, Displayable disp) {
        //#debug
        System.out.println("Command: " + cmd);

        if (cmd == this.m_memoryCommand) {
        	Runtime.getRuntime().gc();
        	Runtime.getRuntime().gc();
        	long free = Runtime.getRuntime().freeMemory();
        	long total = Runtime.getRuntime().totalMemory();
        	String[] params = new String[] {
        			Long.toString(free),
        			Long.toString(total)
        	};
        	showMessage(Locale.get("forms.msg.freeMemory", params));
        } else if (cmd instanceof ExecCommand) {
            //#debug info
            System.out.println("Executing: " + cmd);
            try {
            	setBusy(true);
            	((ExecCommand)cmd).execute(disp);
            } catch (Exception ex) {
            	//#debug warn
            	System.out.println("Could not execute command: " + ex);
            } finally {
            	setBusy(false);
            }
        } else if (m_listener != null) {
            //#debug info
            System.out.println("Forwarding: " + cmd);
            m_listener.commandAction(cmd, disp);
        } else {
            //#debug warn
            System.out.println("Ignoring: " + cmd + " (" + cmd.getLabel()+ ")");
        }
    }

  public void setCommandListener(CommandListener listener) {
      m_listener = listener;
  }

  public CommandListener getCommandListener() {
      return m_listener;
  }

  public void paintScreen(Graphics g) {
	  try {
		  super.paintScreen(g);
	  } catch(Exception ex) {
		  //#debug error
		  System.out.println(
				  Locale.get("forms.error.generalUiFailure")
				  + ": " + ex);
		  ex.printStackTrace();
	  }
  }
  

}



