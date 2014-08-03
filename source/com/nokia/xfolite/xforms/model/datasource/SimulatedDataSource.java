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

import java.util.Random;

import com.nokia.xfolite.xforms.dom.ModelElement;
import com.nokia.xfolite.xforms.dom.UserInterface;
import com.nokia.xfolite.xforms.model.InstanceItem;
import com.nokia.xfolite.xforms.model.XFormsModel;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.Node;


/**
 * This is just a demo, that generates either <gps><pos><lat>234</lat></pos></gps>
 * and alternates between 1 and two pos elements every 2 secs.
 * @author mikkhonk
 *
 */
public class SimulatedDataSource extends BaseDataSource implements DataSource, DataSourceFactory  {

	GPSThread gpsThread;
	int delay=2000;
	
	protected void internalInit()
	{
		String delayStr=this.getParam("refresh");
		if (delayStr!=null)
		{
			try
			{
				this.delay = Integer.parseInt(delayStr);
			} catch (Throwable t)
			{
				
			}
		}
	}
	
	protected void internalStart()
	{
		this.internalClose();
		this.gpsThread=new GPSThread(this);
		this.gpsThread.start();
	}

	protected void internalPause()
	{
		
	}

	protected void internalClose()
	{
		if (this.gpsThread!=null&&this.gpsThread.isAlive())
		this.gpsThread.stopThread();
	}
	
	class GPSThread extends Thread
	{
		private boolean keeprunning=true;
		SimulatedDataSource owner;
		public GPSThread(SimulatedDataSource aowner)
		{
			super();
			owner=aowner;
		}
		public void stopThread()
		{
			this.keeprunning=false;
		}
		public void run()
		{
			this.setupInstance();
			while(keeprunning)
			{
				try {

					// ANY CHANGES TO INSTANCE DATA MUST BE RUN SERIALLY
					Runnable r = new Runnable()
					{
						public void run()
						{
							owner.xfdoc.log(UserInterface.LVL_STATUS,"Serially: In GPSDS update runnable.", owner.xfdoc.getModelElement());
							generateData(false);
							notifyStructureChange();
						}
					};
					owner.xfdoc.log(UserInterface.LVL_STATUS,"GPSDS. Invoking runnable.", owner.xfdoc.getModelElement());
					callSerially(r);
					Thread.sleep(owner.delay);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					owner.xfdoc.log(UserInterface.LVL_ERROR,e.toString(), owner.xfdoc.getModelElement());
				}
			}
			owner=null;
		}
		
		protected Element getChildElement(Element par,int index, String name)
		{
			//Element docElem = owner.m_instance.getDocument().getDocumentElement();
			Node child = par.getFirstChild();
			int i=0;
			while (child!=null)
			{
				if (child.getLocalName().equals(name))
				{
					i++;
					if (i==index)
						return (Element)child;
				}
				child=child.getNextSibling();
			}
			return null;
		}
		
		protected void setGPSData(Element posElem)
		{
			Random rand = new Random();
			float fl = rand.nextFloat();
			final String newVal = String.valueOf(fl);
			final Element latElem = getChildElement(
					posElem,
					1,"lat");
			InstanceItem latItem = owner.m_instance.getModel().getInstanceItemForNode(latElem);
			latItem.setStringValue(newVal);
		}
		
		int count=0;
		protected void generateData(boolean initial)
		{
			count++;
    		Element posElem = getChildElement(
					owner.m_instance.getDocument().getDocumentElement(),
					1,"pos");
			this.setGPSData(posElem);
    		Element posElem2 = getChildElement(
					owner.m_instance.getDocument().getDocumentElement(),
					2,"pos");
			if ((count % 2)==1)
			{
	    		if (posElem2==null)
	    		{
	    			posElem2 = this.generatePositionElem(posElem.getOwnerDocument());
					posElem.getParentNode().appendChild(posElem2);
	    		}
				this.setGPSData(posElem2);
			} else
			{
				posElem2.getParentNode().removeChild(posElem2);
			}
			
		}
		
		protected Element generatePositionElem(Document doc)
		{
			Element position = doc.createElement("pos");
			Element latElem = doc.createElement("lat");
			position.appendChild(latElem);
			latElem.setText("123.456");
			return position;
		}
		
		protected void setupInstance()
		{
			Document doc = owner.m_instance.getDocument();
			final Element newDocElem=doc.createElement("gps");
			Element position = generatePositionElem(doc);
			newDocElem.appendChild(position);
			//Document doc = owner.m_instance.getDocument();
			Element docElem = doc.getDocumentElement();
			if (docElem!=null) doc.removeChild(docElem);
			doc.appendChild(newDocElem);
		}
	}

	public boolean canHandle(String src) {
		// TODO Auto-generated method stub
		return (src.startsWith("sensor://demo"));
	}

	public DataSource getDataSource(String src) {
		// TODO Auto-generated method stub
		return new SimulatedDataSource();
	}

}
