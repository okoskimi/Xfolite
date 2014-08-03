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

package com.nokia.xfolite.xforms.model.datasource;

import java.util.Hashtable;

import com.nokia.xfolite.xforms.dom.InstanceElement;
import com.nokia.xfolite.xforms.dom.ModelElement;
import com.nokia.xfolite.xforms.dom.XFormsDocument;
import com.nokia.xfolite.xforms.model.Instance;
import com.nokia.xfolite.xforms.model.XFormsModel;
import com.nokia.xfolite.xml.dom.events.DOMEvent;
import com.nokia.xfolite.xml.dom.events.DOMEventListener;


public class BaseDataSource implements DOMEventListener {
	
	protected Instance m_instance;
	protected InstanceElement m_instanceElem;
	protected XFormsDocument xfdoc;
	protected String parameters;
	protected short state=DataSource.STATE_UNINITIALIZED;
	protected DSListener m_listener;
	
	public void init(Instance instance, XFormsDocument aui, String params, InstanceElement instanceElem)
	{
		this.m_instance=instance;
		this.xfdoc=aui;
		if (this.state==DataSource.STATE_UNINITIALIZED)
		this.state=DataSource.STATE_INITIALIZED;
		this.parameters=params;
		this.m_instanceElem=instanceElem;
		this.m_instanceElem.addEventListener(DOMEvent.DS_START, this, false);
		this.m_instanceElem.addEventListener(DOMEvent.DS_PAUSE, this, false);
		this.m_instanceElem.addEventListener(DOMEvent.DS_GET, this, false);
		
		this.internalInit();
	}
	
	protected void internalInit()
	{
		
	}
	public short getState()
	{
		return this.state;
	}
	
	public void dispatchDataAvailable()
	{
		this.m_instanceElem.getParentNode().dispatchEvent(new DOMEvent(DOMEvent.DS_DATA_AVAILABLE));
	}
	public void dispatchGetDataAvailable()
	{
		this.m_instanceElem.getParentNode().dispatchEvent(new DOMEvent(DOMEvent.DS_GETDATA_AVAILABLE));
		// TODO: remove this event
		this.m_instanceElem.getParentNode().dispatchEvent(new DOMEvent(DOMEvent.XFORMS_SENSOR_READ));
	}
	
	public void start()
	{
		if (this.m_instance!=null&&
				(this.state==DataSource.STATE_INITIALIZED||this.state==DataSource.STATE_PAUSED)
				) 
		{
			this.internalStart();
			this.state=DataSource.STATE_STARTED;
		}
	}
	
	protected void internalStart() {
		// TODO Auto-generated method stub
		
	}
	
	protected void callSerially(Runnable r)
	{
		if (this.xfdoc!=null)
			this.xfdoc.callSerially(r);
	}

	public void pause()
	{
		if (this.m_instance!=null&&
				(this.state==DataSource.STATE_STARTED)
		) 
		{
			this.internalPause();
			this.state=DataSource.STATE_PAUSED;
		}
	}
	protected void internalPause() {
		// TODO Auto-generated method stub
		
	}
	
	protected Hashtable params;
	protected void parseParametersFromURL()
	{
		this.params=new Hashtable(5);
		String curr = this.parameters;
		// curr has now sensor://gps?param1=value1&param2=value2
		int start = curr.indexOf('?');
		if (start<0||start>=(curr.length()-1)) return;
		curr=curr.substring(start+1);
		// now curr has param1=value1&param2=value2
		while (curr.length()>1)
		{
			int end = curr.indexOf('&');
			if (end<0) end=curr.length();
			String pair = curr.substring(0,end);
			if (end>=(curr.length()-1)) end--;
			curr=curr.substring(end+1);
			int eq = pair.indexOf('=');
			if (eq>=0)
			{
				String name = pair.substring(0,eq);
				String value = pair.substring(eq+1);
				if (name.length()>0)
				{
					this.params.put(name,value);
				}
			}
		}
	}
	
	protected String getParam(String name)
	{
		if (this.params==null) this.parseParametersFromURL();
		return (String)this.params.get(name);
	}
	
	/** 
	 * a helper method, which notifies the model of structure change in the instance - does rebuild of the
	 * model dependencies and re-evaluates the UI dependencies
	 * NOTE: like other methods that communicate with the model, this must be run serially!
	 */
	public void notifyStructureChange()
	{
        ModelElement mel = this.xfdoc.getModelElement();
        mel.rebuild();
		m_instance.getModel().instanceStructureChanged(XFormsModel.SUBMISSION, m_instance, m_instance.getDocument().getDocumentElement());
		
	}

	public void close()
	{
		if (this.m_instance!=null) 
		{
			this.m_instanceElem.removeEventListener(DOMEvent.DS_START, this, false);
			this.m_instanceElem.removeEventListener(DOMEvent.DS_PAUSE, this, false);
			this.m_instanceElem.removeEventListener(DOMEvent.DS_GET, this, false);
			this.internalClose();
			this.state=DataSource.STATE_CLOSED;
		}
		this.m_listener=null;
	}
	
	public void get()
	{
		
	}
	
	public void setStatusListener(DSListener dslist)
	{
		this.m_listener=dslist;
	}


	protected void internalClose() {
		// TODO Auto-generated method stub
		
	}

	public void handleEvent(DOMEvent evt) {
		// TODO Auto-generated method stub
		switch(evt.getType())
		{
		case DOMEvent.DS_START:
			this.start();
			break;
		case DOMEvent.DS_PAUSE:
			this.pause();
			break;
		case DOMEvent.DS_GET:
			this.get();
			break;
			
		}
		

	}


}
