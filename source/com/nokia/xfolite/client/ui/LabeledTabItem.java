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

import javax.microedition.lcdui.Image;
import de.enough.polish.ui.*;

public class LabeledTabItem extends WrapperItem {

    // protected TabBar m_tabItem;
    // protected StringItem m_labelItem;
    protected Style m_style;
    protected Style m_originalStyle;
    public static final int COMBOBOX = 1;
    public static final int TABS = 0;
    int tabCount;
    int m_type;

    public LabeledTabItem(String label, String[] tabNames, Image[] tabImages, int type) {
        this(label, tabNames, tabImages, type, null);
    }

    public LabeledTabItem(String label, String[] tabNames, Image[] tabImages, int type, Style style) {
        super(style);
        m_type = type;
        if (m_type == COMBOBOX) {
        	m_item = UiAccess.cast((de.enough.polish.ui.Item)
                    //#style select1FilteredChoice
            		new XF_FilteredChoiceGroup(tabNames[0], Choice.IMPLICIT)
            );
            for (int i = 0; i < tabNames.length; i++) {
                //#style select1Option
                ((FilteredChoiceGroup)UiAccess.cast(m_item)).append(tabNames[i], null); // Looks like error but works
            }
            ((FilteredChoiceGroup)UiAccess.cast(m_item)).setSelectedIndex(0, true);
        } else {
        	m_item = new TabBar(tabNames, tabImages);
        }
        m_style = style;
        Style labelStyle = null;
        if (m_style != null) {
            labelStyle = (Style) m_style.getObjectProperty("label-style");
        }
        if (label == null || label.length() == 0) {
        	add(m_item);
        } else {
        	if (labelStyle == null) {
        		//#style wrapperLabel
        		m_label = new StringItem(null, label);
        	} else {
        		m_label = new StringItem(null, label, labelStyle);
        	}
        	add(m_label);
        	add(m_item);
        }
        tabCount = tabNames.length;
    }
    public void setTabs(String[] tabs, Image[] images) {
    	//#debug info
    	System.out.println("setTabs: " + tabs + "," + images);
    	if (m_type == COMBOBOX) {
    		((FilteredChoiceGroup)UiAccess.cast(m_item)).deleteAll();
    	} else {
    		for(int i = 0; i < tabCount; i++) {    		
    			((TabBar)m_item).removeTab(0);
    		}
    	}
        tabCount = tabs.length;
        //#debug info
        System.out.println("Tab item: " + m_item);
        for(int i = 0; i < tabCount; i++) {
        	//#debug info
        	System.out.println("tabs[" + i + "]=" + tabs[i]);
        	if (m_type == COMBOBOX) {
        		if (images != null) {
        			((FilteredChoiceGroup)UiAccess.cast(m_item)).append(tabs[i], images[i]);
        		} else {
        			((FilteredChoiceGroup)UiAccess.cast(m_item)).append(tabs[i], null);
        		}
        	} else {        	
        		if (images != null) {
        			((TabBar)m_item).addNewTab(tabs[i], images[i]);
        		} else {
        			((TabBar)m_item).addNewTab(tabs[i], null);
        		}
        	}
        	//#debug info
        	System.out.println("Tab " + tabs[i] + " added.");
        }
    }
    
    public void setActiveTab(int tab) {
    	if (m_type == COMBOBOX) {
    		((FilteredChoiceGroup)UiAccess.cast(m_item)).setSelectedIndex(tab, true);
    	} else {
    		((TabBar)m_item).setActiveTab(tab);
    	}
    }
    
    public int getActiveTab() {
    	if (m_type == COMBOBOX) {
    		//#debug info
    		System.out.println("Querying FilteredChoiceGroup");
    		return ((FilteredChoiceGroup)UiAccess.cast(m_item)).getSelectedIndex();
    	} else {
    		//#debug info
    		System.out.println("Querying TabBar");
    		return ((TabBar)m_item).getNextTab();
    	}
    }
    
    public void setLabel(String label) {
    	if (m_label != null && (label == null || label.length() == 0)) {
    		this.remove(m_label);
    		m_label = null;
    	} else if (m_label == null && label != null && label.length() > 0) {
    		this.remove(m_item);
            Style labelStyle = null;
            if (m_style != null) {
                labelStyle = (Style) m_style.getObjectProperty("label-style");
            }
        	if (labelStyle == null) {
        		//#style wrapperLabel
        		m_label = new StringItem(null, label);
        	} else {
        		m_label = new StringItem(null, label, labelStyle);
        	}
        	add(m_label);
        	add(m_item);
    	} else if (m_label != null){
    		m_label.setText(label);
    	}
    }
    public int size() {
        return tabCount;
    } 

}
