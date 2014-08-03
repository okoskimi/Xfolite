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
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

import org.xmlpull.v1.IXmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.nokia.xfolite.xforms.model.EventProvider;
import com.nokia.xfolite.xforms.model.StoreProvider;
import com.nokia.xfolite.xforms.model.XFormsModel;
import com.nokia.xfolite.xforms.model.XFormsModelException;
import com.nokia.xfolite.xforms.model.XFormsModelUI;
import com.nokia.xfolite.xforms.model.datasource.DataSource;
import com.nokia.xfolite.xforms.model.datasource.DataSourceFactory;
import com.nokia.xfolite.xforms.submission.HTTPSubmitter;
import com.nokia.xfolite.xforms.submission.ISerializer;
import com.nokia.xfolite.xforms.submission.ISubmitter;
import com.nokia.xfolite.xforms.submission.XFormsXMLSerializer;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.dom.WidgetFactory;
import com.nokia.xfolite.xml.dom.events.DOMEvent;
import com.nokia.xfolite.xml.xpath.SimpleXPathNSResolver;
import com.nokia.xfolite.xml.xpath.XPathContext;
import com.nokia.xfolite.xml.xpath.XPathEvaluator;
import com.nokia.xfolite.xml.xpath.XPathNSResolver;
import com.nokia.xfolite.xml.xpath.XPathResult;

import org.kxml2.io.KXmlSerializer;
import javax.microedition.midlet.MIDlet;

interface ElementFactory {
    public Element createElement(Document doc,
                            String namespaceURI,
                            String prefix,
                            String localName);
}

public class XFormsDocument extends Document implements XFormsNode {

    public static final String XFORMS_NAMESPACE = "http://www.w3.org/2002/xforms";
    public static final String XMLEVENTS_NAMESPACE = "http://www.w3.org/2001/xml-events";
    
    private UserInterface userInterface = null;
    private ModelElement modelElement = null;
    private boolean xformsMode = true;
    private Hashtable eventProviders = new Hashtable();
    private Vector submitters = new Vector();
    private Vector serializers = new Vector();
    private SubmissionElement defaultSubmission = null;
    private StoreProvider storeProvider = null;
	private Vector dataSources = null;
    private Hashtable extraInstances = null;
    private XPathContext contextCache = null;
    private String baseURL = null;
    private Vector requiredFields = new Vector();
    
	public XFormsDocument() {
	    setCallbacksEnabled(true);
        // addSubmitter(new HTTPSubmitter());
        // addSerializer(new XFormsXMLSerializer());
    }

    public Element getElementById(String id) {
        Element el = super.getElementById(id);
        if (el == null && id.startsWith("__repeat")) {
            el = getElementById(id.substring(id.indexOf(';')+1));
        }
        return el;
    }
    
    public void addRequired(BoundElement el) {
    	if (! requiredFields.contains(el))  {
    		requiredFields.addElement(el);
    	}
    }
    
    public void removeRequired(BoundElement el) {
    	requiredFields.removeElement(el);
    }
    
    public boolean requiredOK() {
    	Enumeration iter = requiredFields.elements();
    	while(iter.hasMoreElements()) {
    		BoundElement el = (BoundElement) iter.nextElement();
    		String value = el.getStringValue();
    		if (value == null || value.length() == 0) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public void registerEventProvider(String name, EventProvider provider) {
        eventProviders.put(name, provider);
    }

    public void setBaseURL(String url) {
    	url = url.trim();
    	if (url.endsWith("/")) {
    		this.baseURL = url;
    	} if (url.indexOf('/') >= 0) {
    		this.baseURL = url.substring(0, url.lastIndexOf('/') + 1);
    	} else {
    		this.baseURL = url + "/";
    	}
    	//#debug info
    	System.out.println("Base URL set to " + this.baseURL);
    }
    
    public String getBaseURL() {
    	return this.baseURL;
    }
    
    public void save(String src, Node ref, String name) {
        if (storeProvider != null) {
            storeProvider.save(src, this, ref, name);
        }
    }
    
    public void restore(String src, Node ref, String name) {
        if (storeProvider != null) {
            storeProvider.restore(src, this, ref, name);
        }
    }
    
    public void startEventProvider(String user, String name, Node ref, int frequency, Hashtable extParams) {
        EventProvider ep = (EventProvider) eventProviders.get(name);
        if (ep != null) {
            ep.start(user, name, this, ref, frequency, extParams);
        }
    }

    public void getEventProvider(String name, Node ref, Hashtable extParams) {
        EventProvider ep = (EventProvider) eventProviders.get(name);
        if (ep != null) {
            ep.get(name, this, ref, extParams);
        }
    }
    
    public void stopEventProvider(String name) {
        EventProvider ep = (EventProvider) eventProviders.get(name);
        if (ep != null) {
            ep.stop(name);
        }
    }

    public void initializeEventProvider(String user, String name) {
        EventProvider ep = (EventProvider) eventProviders.get(name);
        if (ep != null) {
            ep.initialize(user, name);
        }
    }

    ISubmitter getSubmitter(SubmissionElement submission)
    {
        int count = submitters.size();
        for (int i=0; i < count; i++)
        {
            ISubmitter sub = (ISubmitter) submitters.elementAt(i);
            if (sub.canHandle(submission)) {
                return sub.cloneSubmitter();
            }
        }
        return null;
    }

    ISerializer getSerializer(SubmissionElement submission)
    {
        int count = serializers.size();
        for (int i=0; i < count; i++)
        {
            ISerializer ser = (ISerializer) serializers.elementAt(i);
            if (ser.canHandle(submission)) {
                return ser.cloneSerializer();
            }
        }
        return null;
    }
    
    public void addSubmitter(ISubmitter submitter) {
        submitters.addElement(submitter);
    }
    
    public void addSerializer(ISerializer serializer) {
        serializers.addElement(serializer);
    }

    /*
     * Add a new instance document to the form.
     * The new document will overwrite an inline instance document with same id.
     * New documents can be added both before and after parsing a form. When a new
     * document is added after the parsing, the model will be rebuilt and the user
     * interface will refresh after the addition of each document. Thus adding
     * documents beforehand is more efficient.
     * Please note that the instance is only added to the XForms model once. Once the parsing
     * process is done, the instances added before the parsing have been added to
     * the XForms model and they will not be added on subsequent runs of the parsing process.
     * @param doc The instance document as a DOM tree
     * @param id the identifier of the instance, corresponding to the value of the id attribute in an instance element.
     */
    
    public void addInstance(Document doc, String id) {
        if (modelElement != null) {
            XFormsModel model = modelElement.getModel();
            model.replaceInstance(doc, id);
            modelElement.rebuild();
            modelElement.refresh();
        } else {
            if (extraInstances == null) {
                extraInstances = new Hashtable();
            }
            extraInstances.put(id, doc);
        }
    }
    
    public Hashtable getExtraInstances() {
        return extraInstances;
    }
   
    public Element createElementNS(String namespaceURI, String prefix, String localName) {
        if (!xformsMode) {
            return super.createElementNS(namespaceURI, prefix, localName);
        }
        ElementFactory factory;
        if (namespaceURI.equals(XFORMS_NAMESPACE)) {
            factory = (ElementFactory) XFormsElementFactories.getXFormsElement(localName);
            if (factory != null) {
                return factory.createElement(this, namespaceURI, prefix, localName);
            } else {
                return new XFormsElement(this, namespaceURI,prefix,localName);
            }  
        }
        factory = (ElementFactory)  XFormsElementFactories.getHTMLElement(localName);
        if (factory != null) {
            return factory.createElement(this, namespaceURI, prefix, localName);
        } else {
            return new XFormsElement(this, namespaceURI,prefix,localName);
        }        
    }    

    public void setXFormsMode(boolean mode) {
        xformsMode = mode;
    }
    
    public boolean getXFormsMode() {
        return xformsMode;
    }
    
    public void parse(IXmlPullParser parser)
    throws IOException, XmlPullParserException
    {
        super.parse(parser);
        if (modelElement == null) {
            throw new XFormsModelException("No model defined in document!");
        }
        modelElement.getModelUI().clearDirtyDependencies();
        modelElement.getModelUI().clearDirtyValues();
        modelElement.dispatchEvent(DOMEvent.XFORMS_READY);
        
        SimpleXPathNSResolver resolver = new SimpleXPathNSResolver();
        resolver.put("xhtml", "http://www.w3.org/1999/xhtml");
        
        XPathEvaluator eval = new XPathEvaluator();
        String title = eval.evaluate("xhtml:head/xhtml:title", getDocumentElement(), resolver, XPathResult.STRING).asString();

        //#debug info
        System.out.println("head ns len" + eval.evaluate("xhtml:head", getDocumentElement(), resolver, XPathResult.NODESET).asNodeSet().getLength());

        //#debug info
        System.out.println("head/title ns len" + eval.evaluate("xhtml:head/xhtml:title", getDocumentElement(), resolver, XPathResult.NODESET).asNodeSet().getLength());

        //#debug info
        System.out.println("*********** Setting title: <" + title + ">, docEl = " + getDocumentElement().getLocalName());
        this.userInterface.setTitle(title); // May be empty string
        extraInstances = null;
    }

    
    public XFormsNode getParentNode(Class parentClass) {
        return null;
    }
    
    public void reEvaluateContext(XPathContext parentContext, boolean force)
    {
        ((XFormsElement)documentElement).reEvaluateContext(getContext(), force);
    }
    
    void setModelElement(ModelElement uiModel)
	{
		this.modelElement = uiModel;
	}

    void addSubmission(SubmissionElement el) {
        if (defaultSubmission == null) {
            defaultSubmission = el;
        }
    }
    
    SubmissionElement getSubmission(String id) {
        Element el = this.getElementById(id);
        if (el instanceof SubmissionElement) {
            return (SubmissionElement) el;
        } else {
            return null;
        }
    }

    public SubmissionElement getDefaultSubmission() {
        return defaultSubmission;
    }
    
	public ModelElement getModelElement()
	{
		return modelElement;
	}

    public void setUserInterface(UserInterface handler) {
        userInterface = handler;
    }
    public UserInterface getUserInterface() {
        return userInterface;
    }
    
    public String getProperty(String name) {
        return userInterface.getProperty(name);
    }
    
    public void setStoreProvider(StoreProvider provider) {
        storeProvider = provider;
    }
    
    public void callSerially(Runnable task) {
        if (userInterface != null) {
            userInterface.callSerially(task);
        } else {
            synchronized(this) {
                task.run();
            }
        }
    }
    
	public void log(int lvl, String msg, Element el) {
	    if (userInterface != null) {
	        userInterface.log(lvl, msg, el);
        } else {
            switch (lvl) {
            case UserInterface.LVL_ERROR:
                System.err.println("XFDOM ERROR: " + msg + " (" + el.getLocalName() + ")");
                break;
            case UserInterface.LVL_WARN:
                System.err.println("XFDOM WARNING: " + msg + " (" + el.getLocalName() + ")");
                break;                
            default:
                System.err.println(msg + " (" + el.getLocalName() + ")");
            }
        }
    }
	
	boolean dispatchModelEvent(DOMEvent ev)
	{
		return modelElement.dispatchEvent(ev);
	}

	public XPathContext getContext()
	{
        if (contextCache == null) {
            contextCache = new XPathContext(getModel().getDefaultContextNode(), 1, 1);
        }
        return contextCache;
	}
        
    public XFormsModel getModel() {
		return modelElement.getModel();
	}    
    
    public XFormsModelUI getModelUI() {
        return modelElement.getModelUI();
    }
    
	public void registerDataSource(DataSourceFactory fact)
	{
		if (this.dataSources==null) this.dataSources=new Vector();
		this.dataSources.addElement(fact);
	}
	public void unregisterDataSource(DataSourceFactory fact)
	{
		if (this.dataSources==null) return;
		this.dataSources.removeElement(fact);
	}
	public DataSource getDataSource(String src) {
		// TODO Auto-generated method stub
		if (this.dataSources==null) return null;
		Enumeration dsources =this.dataSources.elements();
		while (dsources.hasMoreElements())
		{
			DataSourceFactory fact = (DataSourceFactory)dsources.nextElement();
			if (fact.canHandle(src))
			{
				return fact.getDataSource(src);
			}
		}
		return null;
	}

		// TODO Auto-generated method stub
	    public void callParallel(Runnable task) {
	        if (userInterface != null) {
	            userInterface.callParallel(task);
	        } else {
	            synchronized(this) {
	                task.run();
	            }
	        }
	    }
	
	
}
