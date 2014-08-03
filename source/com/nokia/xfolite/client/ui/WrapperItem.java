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

package com.nokia.xfolite.client.ui;


import de.enough.polish.ui.*;
import java.util.Vector;
import java.util.Enumeration;
import com.nokia.xfolite.xforms.dom.XFormsElement;
import com.nokia.xfolite.xforms.dom.ValueBoundElement;
import com.nokia.xfolite.xml.dom.Attr;


public class WrapperItem extends Container {
	
	protected Item m_item;
	protected StringItem m_label;
	protected StringItem m_requiredAlert;
	protected Vector m_alertItems;
	protected Vector m_alerts;
	boolean m_required = false;
	boolean m_empty = true;
	boolean m_readOnly = false;
	boolean m_valid = true;
	boolean m_showAlerts = true;
	static final String requiredAlert = "The above information is required.";
	
	
	protected WrapperItem() {
		this(null);
	}

	protected WrapperItem(Style style) {
		super(false, style);
	}
	
	public WrapperItem(Item item, String labelText) {
		this(item, labelText, null);
	}
	
    public WrapperItem(Item item, String labelText, Style style) {
    	super(false, style);
    	m_item = item;
    	if (labelText != null && labelText.length() > 0) {
    		//#style wrapperLabel
    		m_label = new StringItem(null, labelText);
    		add(m_label);
    	}
    	add(m_item);
    	m_requiredAlert = null;
    }
    public Item getItem() {
    	return m_item;
    }
    
    public void setAlerts(Vector alerts) {
    	m_alerts = alerts;
    	setAlertItems();
    }    
    
    protected Style focus(Style focusstyle, int direction ) {
    	Style rval = super.focus(focusstyle, direction);
    	if (m_requiredAlert != null && m_showAlerts) {
    		//#style requiredalertFocused
    		m_requiredAlert.setStyle();
    	}
    	if (m_alertItems != null && m_showAlerts) {
    		Enumeration iter = m_alertItems.elements();
    		while(iter.hasMoreElements()) {
    			StringItem alert = (StringItem) iter.nextElement();
    			//#style xformsalertFocused
    			alert.setStyle();
    		}
    	}
    	setLabelStyle();
    	return rval;
    }

    public void defocus(Style originalStyle) {
    	super.defocus(originalStyle);
    	if (m_requiredAlert != null && m_showAlerts) {
    		//#style requiredalert
    		m_requiredAlert.setStyle();
    	}
    	if (m_alertItems != null && m_showAlerts) {
    		Enumeration iter = m_alertItems.elements();
    		while(iter.hasMoreElements()) {
    			StringItem alert = (StringItem) iter.nextElement();
    			//#style xformsalert
    			alert.setStyle();
    		}
    	}
    	setLabelStyle();
    }
    
    private void setAlertItems() {
    	if (!m_showAlerts) {
    		return;
    	}
    	if (m_alertItems != null) {
    		Enumeration iter = m_alertItems.elements();
    		while(iter.hasMoreElements()) {
    			remove((StringItem)iter.nextElement());
    		}
    		m_alertItems.removeAllElements();
    	}
    	if (m_alerts != null) {
    		if (! m_valid) {
    			if (m_alertItems == null) {
    				m_alertItems = new Vector();
    			}
    			Enumeration iter = m_alerts.elements();
    			while(iter.hasMoreElements()) {    		
    				String alertText = (String) iter.nextElement();
    				if (alertText.length() > 0) {
    					StringItem alert;
    					if (isFocused) {
    						//#style xformsalertFocused
    						alert = new StringItem(null, alertText);
    					} else {
    						//#style xformsalert
    						alert = new StringItem(null, alertText);        						
    					}
    					m_alertItems.addElement(alert);
    					add(alert);
    				}
    			}   
    		}
    	} else {
    		m_alertItems = null;
    	}
    }
    
    public void setValid(boolean valid) {
    	if (valid != m_valid) {
    		m_valid = valid;
    		setAlertItems();
    		setLabelStyle();
    	}
    }
    
    public void setRequired(boolean required, boolean empty) {
    	if (m_required != required || (m_required && m_empty != empty )) {
    		m_required = required;
    		m_empty = empty;
    		setLabelStyle();
    		setRequiredAlert();
    	}
    }
    
    private void setLabelStyle() {
    	if (m_label != null) {
    		boolean haveAlerts = (m_required && m_empty)
    			|| (m_alertItems != null && m_alertItems.size() > 0);
    		if (m_required && m_showAlerts) {
    			// These look like errors but compile after preprocessing
    			if (haveAlerts) {
    				if (isFocused) {
    					//#style wrapperLabelRequiredAlertsFocused
    					m_label.setStyle();
    				} else {
    					//#style wrapperLabelRequiredAlerts
    					m_label.setStyle();   					
    				}
    			} else {
    				if (isFocused) {
    					//#style wrapperLabelRequiredFocused
    					m_label.setStyle();
    				} else  {
    					//#style wrapperLabelRequired
    					m_label.setStyle();
    				}
    			}
    		} else {
    			if (haveAlerts && m_showAlerts) {
    				if (isFocused) {
    					//#style wrapperLabelAlertsFocused
    					m_label.setStyle();
    				} else {
    					//#style wrapperLabelAlerts
    					m_label.setStyle(); 				
    				}
    			} else {
    				if (isFocused) {
    					//#style wrapperLabelFocused
    					m_label.setStyle();
    				} else {
    					//#style wrapperLabel
    					m_label.setStyle(); 				
    				}   				
    			}
    		}
    	}
    }

    private void setRequiredAlert() {
    	if (m_required && m_showAlerts) {
    		// These look like errors but compile after preprocessing
    		if (m_empty) {
    			if (m_requiredAlert == null) {
    				if (isFocused) {
    					//#style requiredalertFocused
    					m_requiredAlert = new StringItem(null, requiredAlert);
    				} else {
    					//#style requiredalert
    					m_requiredAlert = new StringItem(null, requiredAlert);        						
    				}
    				add(m_requiredAlert);
    			}
    		} else if (m_requiredAlert != null) {
    			remove(m_requiredAlert);
    			m_requiredAlert = null;

    		}

    	} else if (m_requiredAlert != null) {
    		remove(m_requiredAlert);
    		m_requiredAlert = null;				
    	}
    }    
    
    public void setReadOnly(boolean readOnly) {
    	if (m_readOnly != readOnly) {
    		m_readOnly = readOnly;
			//debug info
			System.out.println("Setting read-only to " + m_readOnly);
    		if (m_readOnly) {
    			UiAccess.setAccessible(m_item, false);
    			//#style wrapperReadOnly
    			setStyle();
    		} else {
    			UiAccess.setAccessible(m_item, true);
    			//#style wrapper
    			setStyle();
    		}
    	}
    }
    
    public void setLabel(String labelText) {
    	if (labelText == null || labelText.length() == 0) {
    		if (m_label != null) {
    			remove(m_label);
    			m_label = null;
    		}
    	} else {
    		if (m_label != null) {
    			m_label.setText(labelText);
    		} else {
    			//#style wrapperLabel
    			m_label = new StringItem(null, labelText);
    			add(0, m_label);
    			setLabelStyle();
    		} 
    	}
    }
    
    public void setShowAlerts(boolean show) {
    	m_showAlerts = show;
    	if (show == false) {
    		m_alerts = null;
    		if (m_alertItems != null) {
    			Enumeration iter = m_alertItems.elements();
    			while(iter.hasMoreElements()) {
    				remove((StringItem)iter.nextElement());
    			}
    			m_alertItems = null;
    		}
    		if (m_requiredAlert != null) {
        		remove(m_requiredAlert);
        		m_requiredAlert = null;				
        	}
    	}
    }
    
    public void setItem(Item newItem) {
    	remove(m_item);
    	m_item = newItem;
       	add(m_item);    	
    }
}
