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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextField;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import java.io.*;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParserException;


import de.enough.polish.browser.ProtocolHandler;
import de.enough.polish.browser.protocols.HttpProtocolHandler;
import de.enough.polish.browser.protocols.ResourceProtocolHandler;
import de.enough.polish.ui.*;
import de.enough.polish.util.*;
import de.enough.polish.util.Locale;

import java.util.*;

import com.nokia.xfolite.client.ui.*;
import com.nokia.xfolite.client.util.*;
import com.nokia.xfolite.xforms.model.datatypes.*;
import com.nokia.xfolite.xml.dom.*;
import com.nokia.xfolite.xml.xpath.*;

import javax.microedition.rms.*;
import javax.microedition.io.*;

public class XFormsMidlet 
extends MIDlet
implements CommandListener, ItemStateListener, ItemCommandListener
{

    public static String INDEX_ATTR = "index";
	private final Command openCommand = new Command(Locale.get("main.cmd.open"), Command.ITEM, 2 );
	private final Command aboutCommand = new Command(Locale.get("main.cmd.about"), Command.SCREEN, 5 );
	private final Command exitCommand = new Command(Locale.get("main.cmd.exit"), Command.EXIT, 10 );
    private final Command backCommand = new Command(Locale.get("main.cmd.back"), Command.EXIT, 10 );
    
	private Form mainScreen;
	private Display display;
    

	public XFormsMidlet() {
		super();
	}

	
	protected void startApp() throws MIDletStateChangeException {
		//#debug info
		System.out.println("start app");
        this.display = Display.getDisplay( this );        
        
		//#style xform
		Form form = new Form(Locale.get("main.label.defaultTitle"));
        if (this.getAppProperty("menu_label") != null) {
            form.setTitle(this.getAppProperty("menu_label"));
        }
        
        
        int index = 1;
        while(this.getAppProperty("form" + index + "_label") != null) {
            String label = this.getAppProperty("form" + index + "_label");
            try {
                Image img = StyleSheet.getImage("/form.png", null, false);
                if (this.getAppProperty("form" + index + "_icon") != null) {
                    try {
                        img = loadImage(this.getAppProperty("form" + index + "_icon"));
                    } catch (Exception ignore) {}
                }
                //#style trigger
                IconItem item = new IconItem(label, img);
                item.setAttribute(INDEX_ATTR, new Integer(index));
                item.setDefaultCommand(this.openCommand);
                item.setItemCommandListener(this);
                //#style wrapper
                WrapperItem wrapper = new WrapperItem(item, null);
                form.append(wrapper);
            } catch (Exception ex) {
                //#style output
                form.append(new StringItem(label, ex.toString()));
            }
            index++;
        }
        form.addCommand(this.aboutCommand);
        form.addCommand(this.exitCommand);
        //form.addCommand(this.treeTestCommand);
        form.setCommandListener(this);
   
		this.mainScreen = form;
		this.display.setCurrent( form );
	}

    private Image loadImage(String url)
    {
      Image image = null; 

      if (image == null)
      {
        try
        {
          StreamConnection connection = ProtocolFactory.getInstance().getConnection(url);
          InputStream is = connection.openInputStream();
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          byte[] buf = new byte[1024];
          int bytesRead;

          do
          {
            bytesRead = is.read(buf);
            
            if (bytesRead > 0)
            {
              bos.write(buf, 0, bytesRead);
            }
          }
          while (bytesRead >= 0);
          
          buf = bos.toByteArray();
          
          //#debug
          System.out.println("Image requested: " + url);

          image = Image.createImage(buf, 0, buf.length);
          // this.imageCache.put(url, image);
          return image;
        }
        catch (Exception e)
        {
          // TODO: Implement proper error handling.
          
          //#debug debug
          e.printStackTrace();
          
          return null;
        }
      }
      
      return image;
    }
    
    
	protected void pauseApp() {
	}

	protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
		// nothing to clean up
	}

    
    public void commandAction(Command cmd, Item item) {
        System.out.println("Command action(item)");
        if (cmd == this.openCommand) {
            Integer indexAttr = (Integer) item.getAttribute(INDEX_ATTR);
            int index = indexAttr.intValue();
            String url = this.getAppProperty("form" + index + "_url");
            int resIndex = 1;
            String resName = this.getAppProperty("form" + index + "_resource1_name");
            String resUrl = this.getAppProperty("form" + index + "_resource1_url");
            Hashtable resources = new Hashtable();
            while(resName != null) {
                resources.put(resName, resUrl);
                resIndex++;
                resName = this.getAppProperty("form" + index + "_resource" + resIndex + "_name");
                resUrl = this.getAppProperty("form" + index + "_resource" + resIndex + "_url");
            }
            openForm(item.getLabel(), url, resources);
        }
    }

    
    public void openForm(String name, String url, Hashtable resources) {
        try
        {
          ExecCommand exitCmd = new ExecCommand(Locale.get("main.cmd.close"), Command.EXIT, 1) {
              public void execute(Item item, Displayable disp) {
                  display.setCurrent(mainScreen);
              }
          };
          //#style xform
          XFormsForm form = new XFormsForm(Locale.get("main.label.loading"), exitCmd, display);
          form.setBaseURL(url);
          //form.setMidlet(this);
          Hashtable ht = new Hashtable();
          // No properties yet...
          form.setProperties(ht);
          this.display.setCurrent( form );          
          
          StreamConnection connection;
          
          Enumeration resKeys = resources.keys();
          while (resKeys.hasMoreElements()) {
              String resName = (String) resKeys.nextElement();
              String resUrl = (String) resources.get(resName);
              //#debug info
              System.out.println("Loading resource: " + resUrl);
              connection = ProtocolFactory.getInstance().getConnection(resUrl);
              InputStream resin = connection.openInputStream();
              form.addData(resin, resName);
              //#debug
              System.out.println("Closing stream: " + resUrl);
              try {
                  resin.close();
                  connection.close();
              } catch (Exception ignore) {} // We don't care too much if closing fails
          
          }
          
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
            System.out.println(Locale.get("forms.error.errorWhileLoadingForm")
            		+ ": " + ex);
            Form form = new Form(name);
            StringItem error = new StringItem(ex.getClass().getName(), ex.getMessage());
            form.append(error);
            form.addCommand(this.backCommand);
            this.display.setCurrent( form );
            form.setCommandListener(this);
        }
    }
    
    Thread testThread = null;
    public void commandAction(Command cmd, Displayable disp) {
    	//#debug info
    	System.out.println("commandAction with cmd=" + cmd.getLabel() + ", screen=" + disp );
    	if (cmd == this.backCommand) {
    		display.setCurrent(mainScreen);
    	} else if (cmd == this.exitCommand) {
    		//#debug info
    		System.out.println("Exit application");
    		this.notifyDestroyed();
    	} else if (cmd == this.aboutCommand) {
    		String param = this.getAppProperty("Xfolite-Version");
         	Alert alert = new Alert(
         			Locale.get("main.label.about"),
         			Locale.get("main.msg.about", param),
         			null,
         			AlertType.INFO);
     		// Looks like an error but builds
     		this.display.setCurrent(alert, mainScreen);
         }
    }

	public void itemStateChanged(Item item) {
        //#debug
		System.out.println("ItemStateChanged " + item);
	}
	
	
}
