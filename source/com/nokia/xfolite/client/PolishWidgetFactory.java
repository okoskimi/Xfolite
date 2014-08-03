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
import java.util.Vector;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextField;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;
import de.enough.polish.ui.*;
import de.enough.polish.util.ArrayList;
import de.enough.polish.util.HashMap;
import de.enough.polish.util.Locale;

import com.nokia.xfolite.client.ui.*;
import com.nokia.xfolite.client.util.*;
import com.nokia.xfolite.xforms.dom.*;
import com.nokia.xfolite.xforms.model.*;
import com.nokia.xfolite.xforms.model.datasource.*;
import com.nokia.xfolite.xforms.model.datatypes.*;
import com.nokia.xfolite.xforms.submission.*;
import com.nokia.xfolite.xml.dom.*;
import com.nokia.xfolite.xml.dom.events.*;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.nokia.xfolite.xml.xpath.XPathEvaluator;
import com.nokia.xfolite.xml.xpath.XPathNSResolver;
import com.nokia.xfolite.xml.xpath.XPathResult;

import java.io.*;
import de.enough.polish.browser.*;
import de.enough.polish.browser.protocols.*;
import de.enough.polish.util.TableData;


//#if nokia.perfTrace.enabled
//#define tmp.perf
//#endif

/**
 * <p>Provides an XForms form.</p>
 *
 */
public class PolishWidgetFactory implements WidgetFactory, XPathNSResolver {
    
    public static final String ELEMENT_ATTR = "elem";
    public static final String ALERT_ATTR = "alert";
    public static final String VALUE_ATTR = "value";
    public static final String NAME_ATTR = "name";
    public static final String UTAB_ATTR = "utab";
    public static final String LTAB_ATTR = "ltab";
    public static final String CMDS_ATTR = "cmds";
    public static final String EMPTY_ATTR = "empty";
    
    
    UIListener m_listener;
    Container m_root;
    private XPathEvaluator m_xpath;
    
    public PolishWidgetFactory(UIListener listener, Container rootContainer) {
        m_listener = listener;
        m_root = rootContainer;
        m_xpath = new XPathEvaluator();
    }
    
/******************************************* Convenience Methods *****************************/

    /*
      * Convenience method for getting label text.
      */
    public static String getLabel(XFormsElement el) {
        XFormsElement labelEl = (XFormsElement)el.getUserData(XFormsElement.LABEL_KEY);
        if (labelEl != null) {
            return labelEl.getText();        
        } else {
            return "";
        }
    }
    
    /*
     * Convenience method for getting alert text.
     */
    
    public static Vector getAlerts(XFormsElement el) {
        Vector alertElements = (Vector) el.getUserData(XFormsElement.ALERT_KEY);
        if (alertElements == null) {
        	return null;
        }
        Vector alerts = new Vector();
        Enumeration iter = alertElements.elements();
        while(iter.hasMoreElements()) {
        	String alert = ((ValueBoundElement) iter.nextElement()).getText();
        	alerts.addElement(alert);
        }
        return alerts;
    }

    /*
     * Convenience method for getting caption text.
     */
    
    public static String getCaption(XFormsElement el) {
        XFormsElement captionEl = (XFormsElement)el.getUserData(XFormsElement.CAPTION_KEY);
        if (captionEl != null) {
            return captionEl.getText();        
        } else {
            return "";
        }
    }
    
    
    /*
     * Convenience method for getting value text.
     */
   public static String getValue(XFormsElement el) {
       XFormsElement valueEl = (XFormsElement)el.getUserData(XFormsElement.VALUE_KEY);
       if (valueEl != null) {
           return valueEl.getText();        
       } else {
           return "";
       }
   }

   /*
    * Convenience method for getting hint text.
    */
  public static String getHint(XFormsElement el) {
      XFormsElement labelEl = (XFormsElement)el.getUserData(XFormsElement.HINT_KEY);
      if (labelEl != null) {
          return labelEl.getText();        
      } else {
          return "";
      }
  }
  /************************************** XPathNSResolver ********************************/
  
  public String lookupNamespaceURI(String prefix) {
	  if (prefix.equals("xf")) {
		  return XFormsDocument.XFORMS_NAMESPACE;
	  }
	  throw new IllegalArgumentException("Unrecognized namespace " + prefix + ", only xf prefix supported internally");
  }
  
  /************************************** WidgetFactory Methods **************************/
    
    //  This is a widgetFactory method called by parser
    public void removingElement(Element el) {
        //#debug info
        System.out.println("Removing element: " + el);
        Object o = el.getUserData();
        if (o instanceof Item) {
            Item comp = (Item) o;
            Item parent = comp.getParent();
            if (parent instanceof Container) {
                ((Container)parent).remove(comp);
            } else {
                //#debug warn
                System.out.println("Non-container parent (" + parent +  ") found when trying to remove " + comp);
            }
        }
    }

    
    //  This is a widgetFactory method called by parser for the initialized element
    // FOR SUBELEMENTS elementParsed WILL BE CALLED
    public void elementInitialized(Element el) {
        String tagName = el.getLocalName();
        //#debug info
        System.out.println("elementInitialized for " + tagName);
        
        if (tagName == "repeat-item" || tagName == "table-item") {
            //RepeatElement repeatEl = (RepeatElement) el.getParentNode();
            RepeatItemElement itemEl = (RepeatItemElement) el;
            //XFormsGroup parent = (XFormsGroup) repeatEl.getUserData();
    
            Item control =  new XFormsGroup();
            control.setAttribute(ELEMENT_ATTR, itemEl);
            itemEl.setUserData(control);
        } else if (tagName == "case") {
    		// Remove the "please wait" sign
            XFormsGroup group = (XFormsGroup) el.getUserData();
            if (group != null) {
            	group.clear();
            } else {
            	//#debug warn
            	System.out.println("XFormsGroup was not set for case");
            }
        	
    	}
    }
   
    //  This is a widgetFactory method called by parser for the initialized element
    // FOR SUBELEMENTS childrenParsed WILL BE CALLED
    public void childrenInitialized(Element el) {
        String tagName = el.getLocalName();
        //#debug info
        System.out.println("childrenInitialized for " + tagName);
        
        if (tagName == "repeat-item") {
            Element repeatEl = (Element) el.getParentNode();            
            Container parent = (Container) repeatEl.getUserData();
    
            Node n = repeatEl.getFirstChild();
            int itemIndex = 0;
            while (n != el) {
                if (n instanceof RepeatItemElement) {
                    itemIndex++;
                }
                n = n.getNextSibling();
            } 
                  
            Item control = (Item) el.getUserData();
            // This was already done in elementInitialized
            //control.setAttribute(ELEMENT_ATTR, itemEl);
            //itemEl.setUserData(control);

            // For tables it is not necessary to add the repeat-item to the parent container,
            // the addTable method builds the table.
            parent.add(itemIndex, control);
            el.addEventListener(DOMEvent.ANY, m_listener, false);
    	} else if (tagName == "table-item") {
    		el.addEventListener(DOMEvent.ANY, m_listener, false);
    	}
    }
    
    //  This is a widgetFactory method called by parser
    public void elementParsed(Element el) {
        //#debug 
        System.out.println("elementParsed for " + el.getLocalName());
        Node parentNode = el.getParentNode();
        Container parent = m_root;
        while(parentNode instanceof Element) {
            //#debug
            System.out.println("Looking for parent: " + parentNode.getLocalName());
            Object udata = ((Element)parentNode).getUserData();           
            if (udata instanceof Container && !(udata instanceof LabeledTabItem)) {
                //#debug
                System.out.println("Found parent");
                parent = (Container) udata;
                break;
            }
            parentNode = parentNode.getParentNode();
        }
        
        //#debug
        System.out.println("Using parent: " + parent);
        
        String tagName = el.getLocalName();
        Item control = null;
        if ("message" == tagName) {
            el.addEventListener(DOMEvent.XFORMS_ENABLED, m_listener, false);
        } else if ("group" == tagName) {
            control = addGroup(el, parent);
            UiAccess.setVisible(control, ((BoundElement)el).getBooleanState(MIPExpr.RELEVANT));
        } else if ("switch" == tagName) {
            control = addSwitchPre(el, parent);
            if (control != null) {
        		((LabeledTabItem)control).getItem().setAttribute(ELEMENT_ATTR, el);
                ((LabeledTabItem)control).getItem().setItemStateListener(this.m_listener);
            }
        } else if ("case" == tagName) {
            control = addCase(el, parent);
            // Cases are set to invisible when created. The selected one
            // is made visible once whole switch has been parsed
            // in Switch.elementParsed()
            UiAccess.setVisible(control, false);
        } else if ("model" == tagName) {
            //#debug
            System.out.println("Adding event listener for model"); 
            el.addEventListener(DOMEvent.ANY, m_listener, false); 
            //#debug
            System.out.println("Added event listener for model");            
        } else if ("repeat" == tagName) {
            control = addGroup(el, parent);
        } else if ("repeat-item" == tagName) {
            control = addGroup(el, parent);
        } else if ("table-item" == tagName) {    
            el.addEventListener(DOMEvent.ANY, m_listener, false);
        } else if ("td" == tagName) {
        	control = addTableData(el, parent);
        } else if ("th" == tagName) {
        	control = addTableHeader(el, parent);
        }

        if (control != null) {    	
        	
        	if (control instanceof WrapperItem) {
        		((WrapperItem)control).getItem().setAttribute(ELEMENT_ATTR, el);
        	} else {
        		control.setAttribute(ELEMENT_ATTR, el);
        	}
            if (parent != null) {
                //#debug
                System.out.println("Adding control to " + parent);
                
                Object  oldObject = el.getUserData();
                if (oldObject != null) {
                	//#debug warn
                	System.out.println("There should never be an existing mapped GUI object for " + tagName + ": " + oldObject.getClass().getName());
                }

                el.setUserData(control);          
                el.addEventListener(DOMEvent.ANY, m_listener, false);
                //#debug
                System.out.println("Control added");
            }
        }
        //#debug
        System.out.println("exit elementParsed for " + el.getLocalName());    
    }
    
    // This is a widgetFactory method called by parser
    public void childrenParsed(Element el) {
        //#debug 
        System.out.println("childrenParsed for " + el.getLocalName());
        Node parentNode = el.getParentNode();
        Container parent = m_root;
        while(parentNode instanceof Element) {
            //#debug
            System.out.println("Looking for parent: " + parentNode.getLocalName());
            Object udata = ((Element)parentNode).getUserData();
            if (udata instanceof Container && !(udata instanceof LabeledTabItem)) {
                //#debug
                System.out.println("Found parent");
                parent = (Container) udata;
                break;
            }
            parentNode = parentNode.getParentNode();
        }
        //#debug
        System.out.println("Creating control...");

        String tagName = el.getLocalName();
        Item control = null;
        Vector alerts = null;
        BoundElement binding = null;
        if (el instanceof BoundElement) {
            binding = (BoundElement) el;
            alerts = getAlerts(binding);
        }
 
        //#debug 
        System.out.println("Creating control for " + tagName);
            
        if ("input" == tagName || "secret" == tagName) {
            //#debug
            System.out.println("Adding input");
            control = addInput(binding, parent);
            //#debug
            System.out.println("Adding input 2");
            UiAccess.setVisible(control, binding.getBooleanState(MIPExpr.RELEVANT));
            //#debug
            System.out.println("Input added; " + binding.getBooleanState(MIPExpr.RELEVANT));

        } else if ("range" == tagName) {
            //#debug
            System.out.println("Adding range");
            control = addRange(binding, parent);
            //#debug
            System.out.println("Adding range 2");
            UiAccess.setVisible(control, binding.getBooleanState(MIPExpr.RELEVANT));
            //#debug
            System.out.println("Range added; " + binding.getBooleanState(MIPExpr.RELEVANT));
        } else if ("output" == tagName) {
            //#debug
            System.out.println("Adding output");
            control = addOutput(binding, parent);
            UiAccess.setVisible(control, binding.getBooleanState(MIPExpr.RELEVANT));
            //#debug
            System.out.println("Output added; " + binding.getBooleanState(MIPExpr.RELEVANT));
            
        } else if ("select" == tagName) {
            //#debug
            System.out.println("Adding select");
            control = addSelect(binding, parent);
            UiAccess.setVisible(control, binding.getBooleanState(MIPExpr.RELEVANT));
            //#debug
            System.out.println("Select added; " + binding.getBooleanState(MIPExpr.RELEVANT));
        } else if ("trigger" == tagName) {
            control = addTrigger(binding, parent);
        } else if ("switch" == tagName) {
            control = addSwitchPost((XFormsElement)el, parent);
        } else if ("upload" == tagName) {
            //#debug
            System.out.println("Upload added; " + binding.getBooleanState(MIPExpr.RELEVANT));
            control = addUpload(binding, parent);
            UiAccess.setVisible(control, binding.getBooleanState(MIPExpr.RELEVANT));         
        } else if ("submit" == tagName) {
            control = addSubmit(binding, parent);
        } else if ("select1" ==  tagName) {
            //#debug
            System.out.println("Adding select1");
            control = addSelect1(binding, parent);
            UiAccess.setVisible(control, binding.getBooleanState(MIPExpr.RELEVANT));
            //#debug
            System.out.println("Select1 added; " + binding.getBooleanState(MIPExpr.RELEVANT));
        } else if ("img" == tagName) {
            control = addImage(el, parent);
        } else if ("hr" == tagName) {
            control = addHr(el, parent);
        } else if ("p" == tagName) {
            control = addParagraph(el, parent);
        } else if ("table" == tagName) {
        	control = addTable(el, parent);
        } else if ("td" == tagName || "th" == tagName) {
        	if (el.getChildCount() == 1 && el.getFirstChild().getNodeType() == Node.TEXT_NODE) {
        		Container c = (Container) el.getUserData();
        		StringItem si;
        		if ("td" == tagName) {
        			//#style tableDataContent
        			si = new StringItem(null, el.getText());
        		} else {
        			//#style tableHeaderContent
        			si = new StringItem(null, el.getText());       			
        		}
        		c.add(si);
        	}
        } else {
            //#debug
            System.out.println("Unknown form element: "+tagName);
        }
                
        if (control != null) {
            String classAttr = el.getAttribute("class");
            if (classAttr != null) {
                Style s = StyleSheet.getStyle(classAttr);
                if (s != null) {
                    control.setStyle(s);
                }
            }
            
        	if (control instanceof WrapperItem) {
        		if (binding != null) {
        			if (binding.getBooleanState(MIPExpr.REQUIRED)) {
        				boolean empty = false;
        				String value = binding.getStringValue();
        				if (value == null || value.length() == 0) {
        					empty = true;
        				}
        				((WrapperItem)control).setRequired(true, empty);
        				XFormsDocument doc = (XFormsDocument) binding.getOwnerDocument();
        				doc.addRequired(binding);
        			}
        			if (binding.getBooleanState(MIPExpr.READONLY)) {
        				((WrapperItem)control).setReadOnly(true);
        			}
        			if (! binding.getBooleanState(MIPExpr.VALID)) {
        				if (alerts != null) {
        					((WrapperItem)control).setAlerts(alerts);
        				}
        				((WrapperItem)control).setValid(false);
        			}
        		}
        		((WrapperItem)control).getItem().setAttribute(ELEMENT_ATTR, el);
        		((WrapperItem)control).getItem().setItemStateListener(this.m_listener);                
                //#debug info
                System.out.println("Listening to item state of " + ((WrapperItem)control).getItem());
        	} else {
        		control.setAttribute(ELEMENT_ATTR, el);
                control.setItemStateListener(this.m_listener);
                //#debug info
                System.out.println("Listening to item state of " + control);
        	}
            
            el.addEventListener(DOMEvent.ANY, m_listener, false);        
            if (parent != null) {
                //#debug 
                System.out.println("Adding control to " + parent);
                
                Object  oldObject = el.getUserData();
                if (oldObject != null) {
                	//#debug warn
                	System.out.println("There should never be an existing mapped GUI object:" + oldObject.getClass().getName());
                }

                el.setUserData(control);
                
                //#debug
                System.out.println("Control added");

            }
        }
        
        
        //#debug
        System.out.println("Control created.");

    }
    
    /************************** Widget Creation Methods *******************************/


    
    private Item addGroup(Element el, Container parent) {
        //#style group
        XFormsGroup group = new XFormsGroup();
        if (parent != null) {
        	parent.add(group);
        }
        return group;
    }
    
    
    private Item addSwitchPre(Element el, Container parent) {
        //#debug info
        System.out.println("addSwitchPre");
        String appearance = el.getAttribute("appearance");
        //#debug info
        System.out.println("Appearance: " + appearance);
        if (appearance == "full" || appearance == "compact") { // Minimal is default
            String[] temp = { Locale.get("forms.label.buildingTabs") };
            //#style tabwrapper
            XF_TabItem upperTab = new XF_TabItem(
                    null, // Label is not available yet!
                    temp, null,
                    appearance == "full" ? LabeledTabItem.COMBOBOX : LabeledTabItem.TABS);
            upperTab.setAttribute(UTAB_ATTR, Boolean.TRUE);
            if (parent != null) {
            	parent.add(upperTab);
            }
            //#debug info
            System.out.println("Tab created");
            return upperTab;
        } else {
            return null;
        }
    }
    
    private Item addSwitchPost(XFormsElement node, Container parent) {
        // This was created in addSwitchPre
        XF_TabItem upperTab = (XF_TabItem) node.getUserData();
        //#debug info
        System.out.println("Upper tab: " + upperTab);
        String appearance = node.getAttribute("appearance");
        if (appearance != "full" && appearance != "compact") { // Minimal is default
            //#debug
            System.out.println("appearance is minimal, no tabs");
            return null;
        }
        Vector caseList = (Vector) node.getUserData("case");
        CaseElement selectedCase = ((SwitchElement) node).getSelectedCase();
        //#debug info
        System.out.println("Selected case: " + selectedCase);
        if (caseList == null) {
            //#debug info
            System.out.println("caseList is NULL");
            if (upperTab != null) { // This will happen with an empty switch element (<switch/>)
            	// There were no case elements
            	UiAccess.setVisible(upperTab, false);
            }
            return null;
        }
        String[] tabs = new String[caseList.size()];
        int selectedTab = 0;
        for (int i=0; i < tabs.length; i++) {
            XFormsElement caseEl = (XFormsElement) caseList.elementAt(i);
            tabs[i] = getLabel(caseEl);
            if (tabs[i]=="") {
                tabs[i] = Integer.toString(i);
            }
            if (caseEl == selectedCase) {
                selectedTab = i;
            }
        }
        //#debug info
        System.out.println("selected tab: " + selectedTab);

        node.setUserData(UTAB_ATTR, upperTab);
        upperTab.setTabs(tabs, null);
        String label = getLabel(node);
        upperTab.setLabel(label);
        upperTab.setActiveTab(selectedTab);
        return null;
        /*
        if (appearance != "full") {
            //#debug
            System.out.println("appearance is not full, no lower tabs");
            return null;
        }
        //#debug
        System.out.println("Creating lower tab");
        
        // #style lowertab
        XF_TabItem lowerTab = new XF_TabItem(getLabel(node),tabs, null, true);
        lowerTab.setAttribute(LTAB_ATTR, Boolean.TRUE);
        lowerTab.setActiveTab(selectedTab);

        // Because userdata is already set, the lower tab control will not be set as userdata.        
        node.setUserData(LTAB_ATTR, lowerTab);
        if (parent != null) {
        	parent.add(lowerTab);
        }
        return lowerTab;
        */
    }
    
    private Item addCase(Element el, Container parent) {
    	XFormsGroup group = new XFormsGroup();
        if (!(((CaseElement)el).isInitialized())) {
        	group.add(new XF_StringItem(Locale.get("forms.msg.pleaseWait")));
        }
        if (parent != null) {
        	parent.add(group);
        }
        return group;
    }
    
    
    private Item addOutput(BoundElement node, Container parent)
    { /*
        String mtype = node.getAttribute("mediatype");
        if (mtype!=null&&mtype.length()>0) return this.addMediaOutput(node,mtype);
        
        // Label does not word wrap, text does, so we use text for output
        // On the other hand, text does not right justify
        // This (and centering) should be added
        // Also, Labels should scroll when there is not enough space.
        //Label output = new Label("form.output", null, "");
        String style = node.getAttribute("cheetahstyle");
        Text output;
        if (style != "" && style.length() > 0) {
            output = new Text(style, "", 0, false);
        } else {
            output = new Text("form.output", "", 0, false);
        }
        String label = _xeval.evaluate("string(xf:label)", node, node.getOwnerDocument().getDocumentElement(), XPathResult.STRING).asString();
        */
        String label = getLabel(node);
        // Use raw value for now to make debugging easier
        String value = node.getStringValue();
        //String value = node.getDisplayString();
        StringItem output = null;
        if (parent instanceof XFormsGroup) {
        	int type = ((XFormsGroup)parent).getType();
        	switch(type) {
        	case XFormsGroup.TABLE_DATA:
            	//#style tableDataContent
            	output = new XF_StringItem(value);
        		break;
        	case XFormsGroup.TABLE_HEADER:
            	//#style tableHeaderContent
            	output = new XF_StringItem(value);
        		break;
        	default:
            	//#style output
            	output = new XF_StringItem(value);
        	}
        } else {
        	//#style output
        	output = new XF_StringItem(value);
        }
        //#debug
        System.out.println("Output initial value: <" +  value + ">");

        //#style wrapper
        WrapperItem wrapper = new WrapperItem(output, label);
        wrapper.setShowAlerts(false);
        if (parent != null) {
        	parent.add(wrapper);
        }
        return wrapper;
    }
    

    private Item addMediaOutput(BoundElement node, String mtype, Container parent)
    { /*
        String name = _xeval.evaluate("text()", node, node.getOwnerDocument().getDocumentElement(), XPathResult.STRING).asString();
        String label = _xeval.evaluate("string(xf:label)", node, node.getOwnerDocument().getDocumentElement(), XPathResult.STRING).asString();
        XF_Button trigger = new XF_Button("form.button", null, label);
        trigger.setAction(ITEM_CHANGED);
        // trigger.setLabel(label);
        return trigger;*/
        return null;
    }

    private Item addTrigger(BoundElement node, Container parent)
    { 
        String label = getLabel(node);
        if (label == "") {
            label = node.getText();
        }
        Node parentNode = node.getParentNode();
        XFormsElement xfParentElement = null;
        if (parentNode instanceof XFormsElement) {
            xfParentElement = (XFormsElement) parentNode;
        }
        String appearance = node.getAttribute("appearance");
        if (appearance == "minimal" && xfParentElement != null) {
            Vector cmdList = (Vector) xfParentElement.getUserData(CMDS_ATTR);
            if (cmdList == null) {
                cmdList = new Vector();
                xfParentElement.setUserData(CMDS_ATTR, cmdList);
            }
            cmdList.addElement(new DomEventCommand(label, Command.OK, 2, node, DOMEvent.DOM_ACTIVATE));
            return null;
        } else {
        	XF_Button trigger;
            if (parent instanceof XFormsGroup) {
            	int type = ((XFormsGroup)parent).getType();
            	switch(type) {
            	case XFormsGroup.TABLE_DATA:
            	case XFormsGroup.TABLE_HEADER:
                	//#style tableTrigger
                    trigger = new XF_Button(null, label);
            		break;
            	default:
                	//#style trigger
                    trigger = new XF_Button(null, label);
            	}
            } else {
            	//#style trigger
                trigger = new XF_Button(null, label);
            }
            trigger.setDefaultCommand(new DomEventCommand("Select" /* label */, Command.OK, 1, node, DOMEvent.DOM_ACTIVATE, trigger));
            trigger.setItemCommandListener(m_listener);
            //#style wrapper
            WrapperItem wrapper = new WrapperItem(trigger, null);
            if (parent != null) {
            	parent.add(wrapper);
            }
            return wrapper;
        }

    }    

    private Item addUpload(BoundElement node, Container parent)
    { /*
        // TODO: move this functionality to upload element
        //System.out.println("addSelect1()");
        
        //System.out.println("Length: " + length);
        boolean cam=false;
        boolean mic=false;
        String mediatype = node.getAttribute("mediatype");
        //String mediatype="    test/test   image/*   audio/*   ";
        if (mediatype==null) mediatype="";
        
        boolean more=true;
        while (more)
        {
            String curr=mediatype;
            int space = mediatype.indexOf(' ');
            if (space>-1)
            {
                curr=mediatype.substring(0,space);
                mediatype=mediatype.substring(space+1);
                more =true;
            }
            else
                more=false;
            if (curr.indexOf("image/")>-1) cam=true;
            else if (curr.indexOf("audio/")>-1) mic=true;
        }
        
        if (mic==false&&cam==false) cam=true; // for now we always include at least one source
        int count = (mic==true?1:0)+(cam==true?1:0);
        
        String[] names = new String[count];
        String[] values = new String[count];
        count--;
        if (cam==true)
        {
            names[count]=CameraUploader.name;
            values[count]=CameraUploader.name;
            count--;
        }
        if (mic==true)
        {
            names[count]=VoiceClipUploader.name;
            values[count]=VoiceClipUploader.name;
            count--;
        }
        final ValueChoice valueChoice = new ShortcutChoice(node.getBooleanState(MIPExpr.VALID) ? "form.choice" : "form.choice.invalid",
                null, names, values, 0);    

        //System.out.println("Choice created");
        String mainLabel = _xeval.evaluate("string(xf:label)", node, node.getOwnerDocument().getDocumentElement(), XPathResult.STRING).asString();
        if (mainLabel != "" && mainLabel.length() > 0) {
            //System.out.println("Setting label");
            valueChoice.setLabel(mainLabel);
            //System.out.println("Label set");
        }
        valueChoice.setText(names[0]);
        //System.out.println("Return from addSelect1()");
        return valueChoice;
        */
        return null;
    }


    private Item addRange(BoundElement node, Container parent)
    {
        //#debug
        System.out.println("addRange");
        String startStr = node.getAttribute("start");
        String endStr = node.getAttribute("end");
        String stepStr = node.getAttribute("step");
        //#debug
        System.out.println("Start:" + startStr + ",end:" + endStr + ", step:" + stepStr);
        if (startStr == "" || endStr == "") {
            node.logError(Locale.get("forms.error.noStartAndEnd"));
            return null;
        }
        double start;
        try {
            start = Double.parseDouble(startStr);
        } catch (NumberFormatException ex) {
            node.logError(Locale.get("forms.error.couldNotParseStartAttribute")
            		+ ": " + ex);
            return null;
        }
        double end;
        try {
            end = Double.parseDouble(endStr);
        } catch (NumberFormatException ex) {
            node.logError(Locale.get("forms.error.couldNotParseEndAttribute")
            		+ ": " + ex);
            return null;
        }
        
        double step = 1.0;
        if (stepStr != "") {
            try {
                step = Double.parseDouble(stepStr);
            } catch (NumberFormatException ex) {
                node.logError(Locale.get("forms.error.couldNotParseEndAttribute")
                		+ ": " + ex);
                return null;
            }
        }        
 

        //#debug
        System.out.println("Start:" + start + ",end:" + end + ", step:" + step);

        //#debug
        System.out.println("Value:" + node.getStringValue());
        
        DataTypeBase dataType = node.getDataType();
        if (dataType == null) {
            //#debug error
            System.out.println(Locale.get("forms.error.cannotResolveDataType"));
            return null;
        } else {
            //#debug debug
            System.out.println("Datatype: " + dataType);   
        }
        int typeId = dataType.getBaseTypeID();
        if (typeId != DataTypeBase.XML_SCHEMAS_DECIMAL && typeId != DataTypeBase.XML_SCHEMAS_INTEGER) {
            node.logError(Locale.get("forms.error.rangeSupportsOnlyDecimalAndInteger"));
            //debug
            System.out.println("Wrong typeId:" + typeId);
            return null;
        }
        double val;
        try {
            val = ((DataTypeDecimal)dataType).getDoubleValue(node);
        } catch (NumberFormatException ex) {
            node.logWarning(Locale.get("forms.error.illegalValueForRangeInInstanceData")
            		+ ": " + node.getStringValue());
            val = start;
        }
        //Gauge gauge = new Range("form.range", start, end, step, val);
        String label = getLabel(node);
        
        int type = Range.DECIMAL;
        if (typeId == DataTypeBase.XML_SCHEMAS_INTEGER) {
            type = Range.INTEGER;
        }
        //#style range
        Range range = new XF_Range(start, end, step, val, type);
        
        //#style wrapper
        WrapperItem wrapper = new WrapperItem(range, label);
        if (parent != null) {
        	parent.add(wrapper);
        }
        return wrapper;
    }
    
    
    private Item addInput(BoundElement node, Container parent)
    { 
        //#debug
        System.out.println("Entering addInput");
        if (node == null) {
            //#debug warn
            System.out.println("Node is null");
        }

        DataTypeBase dataType = node.getDataType();
        if (dataType == null) {
            //#debug warn
            System.out.println("Datatype is null");
        }
        //#debug info
        System.out.println(node.getLocalName()+": "+ ((dataType != null) ? dataType.getTypeName()
                    : "Datatype not found"));
        int typeId = DataTypeBase.XML_SCHEMAS_STRING;
        if (dataType!=null) {
            typeId=dataType.getBaseTypeID();
        }
        //#debug
        System.out.println("addInput 1");
        Item input = null;
        Calendar cal = null;
        String label = getLabel(node);
        //#debug info
        System.out.println("Got label: " + label);
        switch (typeId) {
        case DataTypeBase.XML_SCHEMAS_BOOLEAN:
            boolean selected = ((DataTypeBoolean)dataType).getBooleanValue(node);

            
            if (parent instanceof XFormsGroup) {
                int type = ((XFormsGroup)parent).getType();
                switch(type) {
                case XFormsGroup.TABLE_DATA:
                case XFormsGroup.TABLE_HEADER:
                    //#style tableCheckbox
                    input = new XF_ChoiceGroup(Choice.MULTIPLE);
                    //#style tableCheckboxOption
                    ((ChoiceGroup)input).append(label, null );
                    break;
                default:
                    //#style checkbox
                    input = new XF_ChoiceGroup(Choice.MULTIPLE);
                    //#style checkboxOption
                    ((ChoiceGroup)input).append(label, null );
                }
            } else {
                //#style checkbox
                input = new XF_ChoiceGroup(Choice.MULTIPLE);
                //#style checkboxOption
                ((ChoiceGroup)input).append(label, null );                
            }            
            
            ((ChoiceGroup)input).setSelectedIndex(0, selected);
            parent.add(input);
            /*
            //#style select1Option
            input = new XF_ChoiceItem(label, null, Choice.EXCLUSIVE);
            ((ChoiceItem)input).select(selected);
            */
            break;

        case DataTypeBase.XML_SCHEMAS_DATE:
        	//#style input
            input = new XF_DateField(DateField.DATE);
            cal = ((DataTypeDate)dataType).getCalendarValue(node);
            ((DateField)input).setDate(cal.getTime());
            //#style wrapper
            input = new WrapperItem(input, label);
            if (parent != null) {
            	parent.add(input);
            }
            break;

        case DataTypeBase.XML_SCHEMAS_TIME:
        	//#style input
            input = new XF_DateField(DateField.TIME);
            cal = ((DataTypeDate)dataType).getCalendarValue(node);
            ((DateField)input).setDate(cal.getTime());
            //#style wrapper
            input = new WrapperItem(input, label);
            if (parent != null) {
            	parent.add(input);
            }
            break;            
            
        case DataTypeBase.XML_SCHEMAS_DATETIME:
        	//#style input
            input = new XF_DateField(DateField.DATE_TIME);
            cal = ((DataTypeDateTime)dataType).getCalendarValue(node);
            ((DateField)input).setDate(cal.getTime());
            //#style wrapper
            input = new WrapperItem(input, label);
            if (parent != null) {
            	parent.add(input);
            }
            break;
            
        case DataTypeBase.XML_SCHEMAS_STRING:
        case DataTypeBase.XML_SCHEMAS_DECIMAL:
        case DataTypeBase.XML_SCHEMAS_INTEGER:
        case DataTypeBase.XML_SCHEMAS_ANYURI:
            int flags = TextField.ANY;
            int maxSize = 255;
            switch (typeId) {
            case DataTypeBase.XML_SCHEMAS_DECIMAL:
                flags = TextField.DECIMAL;
                maxSize = 128;
                break;
            case DataTypeBase.XML_SCHEMAS_INTEGER:
                flags = TextField.NUMERIC;
                maxSize = 128;
                break;
            case DataTypeBase.XML_SCHEMAS_ANYURI:
                flags = TextField.URL;
                maxSize = 255;
                break;
            } // Inner switch

            if (node.getLocalName() == "secret") {
            	//#debug info
            	System.out.println(label + ": setting to PASSWORD field");
            	flags |= TextField.PASSWORD;
            }
            
            //#debug
            System.out.println("addInput 2");
            //#debug
            System.out.println("Test: " + node.getOwnerDocument().getDocumentElement().lookupNamespaceURI("xf"));

            String strVal = node.getStringValue();
            if (strVal==null) {
                strVal="";
            }
            //#debug
            System.out.println("addInput 3");

            // This looks like an error but builds just fine.
            //#style input
            input = new XF_TextField(strVal, maxSize, flags);
            //#style wrapper
            input = new WrapperItem(input, label);
            if (parent != null) {
            	parent.add(input);
            }
            //#debug
            System.out.println("addInput 4");
            break;
        } // Outer switch
        return input; 
    }

    public Item addSelect1(BoundElement node, Container parent)
    { 
        //#debug
        System.out.println("addSelect1()");
        //#debug
        System.out.println("ref = " + node.getAttribute("ref"));
        NodeSet choices = null;
        //#if tmp.perf
        long start = System.currentTimeMillis();
        //#endif
        try
        {
            choices = m_xpath.evaluate(".//xf:item", node, this, XPathResult.NODESET).asNodeSet();
        } catch (Throwable e)
        {
            //#debug error
            System.out.println(
            		Locale.get("forms.error.cannotResolveSelectItems")
            		+ ": " + e);
        }
        //#if tmp.perf
        long t1 = System.currentTimeMillis() - start;
        System.out.println("Spent " + t1 + " ms collecting choices for select1");
        //#endif
        int length = choices==null?0:choices.getLength();
        //#debug
        System.out.println("Length: " + length);
        String[] names = null; 
        String[] values = null; 
        
        int chosen = -1;
        String chosenVal = node.getStringValue();
        if (length>0)
        {
            names= new String[length];
            values = new String[length];
            for (int i=0; i < length; i++) {
                XFormsElement n = (XFormsElement) choices.item(i);
                String label = getLabel(n);
                String value = getValue(n);
    
                //#debug info
                System.out.println("Label " + i + ": " + label);
                //#debug info
                System.out.println("Value " + i + ": " + value);

                if (value != "" && value.equals(chosenVal)) {
                    //#debug
                    System.out.println("Found chosen: " + chosen);
                    chosen = i;
                }
                
                names[i] = label;
                values[i] = value;
            }
            //#debug info
            System.out.println("Options: " + names.length + "/" + values.length);
        }
        


        String mainLabel = getLabel(node);
        
        String appearance = node.getAttribute("appearance");
        String selection = node.getAttribute("selection");

        Item select1 = null;
        // cannot select
        if (length == 0) {
        	//debug warn
        	System.out.println("No options for select1, making it unaccessible.");
            
            select1 = UiAccess.cast((de.enough.polish.ui.Item)
            		//#style select1FilteredChoice
            		new XF_FilteredChoiceGroup(chosenVal, Choice.EXCLUSIVE)
            ); // Looks like error but works
            values = new String[0];
            select1.setAttribute(VALUE_ATTR, values);
            UiAccess.setAccessible(select1, false);
            //#style wrapper
            select1 = new WrapperItem(select1, mainLabel);
            if (parent != null) {
            	parent.add(select1);
            }
            return select1;
        }

        if (selection == "open") {
            if (chosen >= 0 ) {
                chosenVal = names[chosen];
            }
            //This looks like an error but builds just fine.
            select1 = UiAccess.cast((de.enough.polish.ui.Item)
                    //#style select1TextField
            		new XF_ChoiceTextField(chosenVal, 255, TextField.ANY, names, true)
            ); // Looks like error but works
            select1.setAttribute(NAME_ATTR, names);
            select1.setAttribute(VALUE_ATTR, values);
            //#style wrapper
            select1 = new WrapperItem(select1, mainLabel);
        } else if(appearance == "full") {
            if (chosen < 0) {
                chosen = 0;
            }
            //#style select1ChoiceGroup
            select1 = new XF_ChoiceGroup(Choice.EXCLUSIVE);
            select1.setAttribute(VALUE_ATTR, values);
            for (int i = 0; i < length; i++) {
                //#style select1Option
                ((ChoiceGroup)select1).append(names[i], null );
            }
            ((ChoiceGroup)select1).setSelectedIndex(chosen, true);
            //#style wrapper
            select1 = new WrapperItem(select1, mainLabel);
        } else if (appearance == "tabs") {
            if (chosen < 0) {
                chosen = 0;
            }
            //#style tabwrapper
            select1 = new XF_TabItem(mainLabel, names, null, LabeledTabItem.TABS); 
            ((XF_TabItem)select1).setActiveTab(chosen);
            select1.setAttribute(VALUE_ATTR, values);
        } else {
            if (chosen >= 0) {
                chosenVal = names[chosen];
            }
            int type = Choice.EXCLUSIVE;
            if (appearance == "minimal") {
            	type = Choice.IMPLICIT;
            }
            select1 = UiAccess.cast((de.enough.polish.ui.Item)
                    //#style select1FilteredChoice
            		new XF_FilteredChoiceGroup(chosenVal, type)
            );
            select1.setAttribute(VALUE_ATTR, values);
            
            for (int i = 0; i < names.length; i++) {
                //#style select1Option
                ((XF_FilteredChoiceGroup)UiAccess.cast(select1)).append(names[i], null); // Looks like error but works
            }
            if (chosen >= 0) {
                ((XF_FilteredChoiceGroup)UiAccess.cast(select1)).setSelectedIndex(chosen, true); // Looks like error but works
            }
            //#style wrapper
            select1 = new WrapperItem(select1, mainLabel);
        }
        if (parent != null) {
        	parent.add(select1);
        }
            
            
 /*************       
        } else if (appearance == "compact") {
            if (chosen < 0) {
                chosen = 0;
            }
            //#style select1ChoiceGroup
            select1 = new XF_ChoiceGroup(mainLabel, Choice.POPUP, names, null);
            ((ChoiceGroup)select1).setSelectedIndex(chosen, true);
            String var = CodeGenerator.setVarName(select1, "select1Item");
            CodeGenerator.writeCode("//#style select1");
            CodeGenerator.writeCode("ChoiceGroup " + var + " = new ChoiceGroup(\"" + mainLabel + "\", Choice.POPUP, "
                    + tempNames + ", null);");
            CodeGenerator.writeCode(var + ".setSelectedIndex(" + chosen + ", true);");
        } else {
            if (chosen > -1) {
                chosenVal = names[chosen];
            }
            //This looks like an error but builds just fine.
            //#style select1TextField
            select1 = new XF_ChoiceTextField(mainLabel, chosenVal, 255, TextField.ANY, names, selection == "open");
            select1.setAttribute(NAME_ATTR, names);
            String var = CodeGenerator.setVarName(select1, "select1Item");
            CodeGenerator.writeCode("//#style select1TextField");
            CodeGenerator.writeCode("ChoiceTextField " + var + " = new ChoiceTextField(\"" + mainLabel + "\", \"" + chosenVal
                    + "\", 255, TextField.ANY, "+ tempNames + ", false);");
        }
****************/
            
        //#debug
        System.out.println("Choice created");

        //#if tmp.perf
        long t2 = System.currentTimeMillis() - start;
        System.out.println("Spent " + t2 + " ms creating select1");
        //#endif
        //#debug
        System.out.println("Return from addSelect1()");
        return select1;
    }
 
    public Item addSelect(BoundElement node, Container parent)
    { 
        //#debug
        System.out.println("addSelect()");
        NodeSet choices = m_xpath.evaluate(".//xf:item", node, this, XPathResult.NODESET).asNodeSet();
        
        int length = choices.getLength();
        //#debug
        System.out.println("Length: " + length);
        String[] names = new String[length];
        String[] values = new String[length];
        boolean[] selected = new boolean[length];

        String chosenVal = " " + node.getStringValue() + " ";
        
        for (int i=0; i < length; i++) {
            XFormsElement n = (XFormsElement) choices.item(i);
            String label = getLabel(n);
            String value = getValue(n);

            if (value != "" && chosenVal.indexOf(" " + value + " ") >= 0) {
                //#debug
                System.out.println("Found chosen: " + chosenVal);
                selected[i] = true;
            } else {
                selected[i] = false;
            }            
            names[i] = label;
            values[i] = value;
        }
        if (length == 0) { // Horrible kludge, empty lists are currently just not really supported
            length = 1;
            names = new String[1];
            values = new String[1];
            selected = new boolean[1];
            names [0] = values[0] = "";
            selected[0] = false;
        }
        
        //#debug
        System.out.println("Options: " + names.length + "/" + values.length);

        String mainLabel = getLabel(node);
        String appearance = node.getAttribute("appearance");
        
        Item select = null;
        if (appearance == "full") {
            //#style select
            select = new XF_ChoiceGroup(Choice.MULTIPLE);
            for (int i = 0; i < length; i++) {
                //#style select1Option
                ((ChoiceGroup)select).append(names[i], null );
            }
            for (int i=0; i < length; i++) {
                ((ChoiceGroup)select).setSelectedIndex(i, selected[i]);
            }
        } else {
            select = UiAccess.cast((de.enough.polish.ui.Item)
                    //#style select1FilteredChoice
            		new XF_FilteredChoiceGroup("", Choice.MULTIPLE)
            ); // Looks like error but works
            for (int i = 0; i < length; i++) {
                //#style select1Option
                ((XF_FilteredChoiceGroup) UiAccess.cast(select)).append(names[i], null ); // Looks like error but works
            }
            for (int i=0; i < length; i++) {
                ((XF_FilteredChoiceGroup)UiAccess.cast(select)).setSelectedIndex(i, selected[i]); // Looks like error but works
            }
        }
        //#debug
        System.out.println("Select created");
        
        select.setAttribute(VALUE_ATTR, values);

        //#debug
        System.out.println("Return from addSelect()");
        //#style wrapper
        select = new WrapperItem(select, mainLabel);
        if (parent != null) {
        	parent.add(select);
        }
        return select;
    }    
    


    
    private Image loadImageInternal(String url)
    {
      Image image = null; // (Image) this.imageCache.get(url);

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
    
    
    // DISPLAY ELEMENTS
    
    private Item addImage(Element node, Container parent)
    { 
        String url = node.getAttribute("src");
        if (url == null) {
            url = "";
        }
        /*
        if (_extension != null) {
            byte[] picData = _extension.getResource(src);
            if (picData != null && picData.length > 0) {
                return new Icon(Pic.get(picData), "", 0);
            }
        }
        */
        //#debug
        System.out.println("Loading image from url:" + url);
        Image image = loadImageInternal(url);
        //#style img
        ImageItem img = new XF_ImageItem(null, image, Item.LAYOUT_DEFAULT,
        		"[" + Locale.get("forms.error.imageNotFound")+ "]");
        if (parent != null) {
        	parent.add(img);
        }
        return img;
        
    }
    
    private Item addHr(Element node, Container parent)
    {
        //Image image = loadImageInternal("resource:/hr.png");
        String label = getLabel((XFormsElement)node);
        //#style divider
        LabeledSeparator hr = new LabeledSeparator(label);
        //ImageItem hr = new XF_ImageItem(null, image, Item.LAYOUT_DEFAULT, "----------");
        if (parent != null) {
        	parent.add(hr);
        }
        return UiAccess.cast(hr); // Looks like error but works
    }
    
    private Item addParagraph(Element node, Container parent)
    { /*
        Text output = new Text("form.output", "", 0, false);
        output.setText(node.getText());        
        return output;    */

        StringItem output = null;
        if (parent instanceof XFormsGroup) {
        	int type = ((XFormsGroup)parent).getType();
        	switch(type) {
        	case XFormsGroup.TABLE_DATA:
            	//#style tableDataContent
            	output = new XF_StringItem(node.getText());
        		break;
        	case XFormsGroup.TABLE_HEADER:
            	//#style tableHeaderContent
            	output = new XF_StringItem(node.getText());
        		break;
        	default:
            	//#style output
            	output = new XF_StringItem(node.getText());
        	}
        } else {
        	//#style output
        	output = new XF_StringItem(node.getText());
        }

        if (parent != null) {
        	parent.add(output);
        }
        return output;
    }
    
    private Item addSubmit(BoundElement node, Container parent)
    { /*
        String name = _xeval.evaluate("text()", node, node.getOwnerDocument().getDocumentElement(), XPathResult.STRING).asString();
        String label = _xeval.evaluate("string(xf:label)", node, node.getOwnerDocument().getDocumentElement(), XPathResult.STRING).asString();
        XF_Button submit = new XF_Button("form.button.submit", null, label);
        submit.setAction(ITEM_CHANGED);
        // trigger.setLabel(label);
        return submit; */

        String label = getLabel(node);
        if (label == "") {
            label = node.getText();
        }
        //#style submit
        XF_Button trigger = new XF_Button(null, label);
        //FIXME: Needs to send submit event, not dom activate!!
        trigger.setDefaultCommand(new SubmitCommand(
        		Locale.get("forms.cmd.submit"),
        		Command.ITEM, 1, node, DOMEvent.DOM_ACTIVATE, trigger));

        //#style wrapper
        WrapperItem wrapper = new WrapperItem(trigger, null);
        if (parent != null) {
        	parent.add(wrapper);
        }
        return wrapper;
        
    }

    public Item addTable(Element el, Container parent) {

    	//#debug info
    	System.out.println("Adding table");
    	
    	
/*
    	//#style table
        XF_TableItem table = new XF_TableItem();
        table.setSelectionMode(TableItem.SELECTION_MODE_CELL);
        table.setDefaultCommand(new TableSelectCommand("Select", Command.OK, 1));
        table.setItemCommandListener(m_listener);
        String caption = getCaption((XFormsElement) el);
        //#style wrapper
        WrapperItem wrapper = new WrapperItem(UiAccess.cast(table), caption);
        if (parent != null) {
        	parent.add(wrapper);
        }
        return wrapper;
    	*/
    	
    	Node n = el.getFirstChild();
    	Vector rows = new Vector();
    	int maxColCount = 0;
    	Style evenRowStyle = StyleSheet.getStyle("tableEvenRow");
    	Style oddRowStyle = StyleSheet.getStyle("tableOddRow");
    	Style evenRowSelectorStyle = StyleSheet.getStyle("evenRowSelector");
    	Style oddRowSelectorStyle = StyleSheet.getStyle("oddRowSelector");
    	if (evenRowStyle == null) {
    		//#debug error
    		System.out.println(Locale.get("forms.error.evenRowStyleNotDefined"));
    	}
    	if (oddRowStyle == null) {
    		//#debug error
    		System.out.println(Locale.get("forms.error.oddRowStyleNotDefined"));
    	}
    	Vector row = null;
    	boolean drawRadioButtons = ((XFormsElement)el).getUserData("tbody") != null && (el.getAttribute("appearance") == "selectable");
    	boolean plainDataRow = true;
    	boolean digIn;
    	ChoiceItem selector = null;
    	boolean evenRow = false;
    	while(n != el) {
    		digIn = true;
    		if (n.getNodeType() == Node.ELEMENT_NODE) {
    			String lname = n.getLocalName();
    	    	//#debug info
    	    	System.out.println("Got element " + lname + ": " + ((Element)n).getText());
    			if (lname == "tr") {
    				row = new Vector();
    				if (plainDataRow) {
    					evenRow = ! evenRow;
    				}
    				rows.addElement(row);
    			} else if (lname == "td" || lname == "th") {
    				// This styles the container, not the actual content
    				if (drawRadioButtons && row.size() == 0) {
    					if (selector != null) {
    						selector.setStyle(evenRow ? evenRowSelectorStyle : oddRowSelectorStyle);
    						row.addElement(selector);
    						selector = null;
    					} else {
    						row.addElement(new StringItem(null, null, evenRow ? evenRowStyle : oddRowStyle));
    					}
    				}
    				Item item = (Item) n.getUserData();
    				item.setStyle(evenRow ? evenRowStyle : oddRowStyle);
    				row.addElement(item);
    				int len = row.size();
    				if (len > maxColCount) {
    					maxColCount = len;
    				}
    				digIn = false;
    			} else if (lname == "thead") {
    				plainDataRow = false;
    			} else if (lname == "table-item") {
    				if (drawRadioButtons) {
    					selector = new ChoiceItem( null, null, ChoiceGroup.EXCLUSIVE);
    					selector.setDefaultCommand(new SetIndexCommand("Select", Command.ITEM, 1, (RepeatItemElement) n));
    					if (((RepeatItemElement)n).isSelected()) {
    						selector.select(true);
    					}
    					selector.setAttribute(ELEMENT_ATTR, n);
    					((XFormsElement)n).setUserData(selector);
    				}
    				plainDataRow = false;
    				evenRow = ! evenRow;
    			}
    		}
    		
            // Go to next node:
            if (n.hasChildNodes() && digIn) 
            {
                n = n.getFirstChild();
            } else {
                while(n.getNextSibling() == null) 
                {
                	String lname = n.getLocalName();
                	if (lname == "table-item") {
                		selector = null;
                		plainDataRow = true;
                	} else if (lname == "thead") {
                		plainDataRow = true;
                	}
                    n = n.getParentNode();

                    if (n == el) 
                    {
                        break;
                    }
                    
                }
                if (n != el) 
                {
                    n = n.getNextSibling();
                }
            }
    		
    	}   
        
    	//#debug info
    	System.out.println("Creating table");
    	XF_TableItem table;
    	if (el.getAttribute("appearance") == "layout") {
        	//#style layoutTable
            table = new XF_TableItem(maxColCount, rows.size());
    	} else {
        	//#style table
            table = new XF_TableItem(maxColCount, rows.size());            
    	}
    	table.setSelectionMode(TableItem.SELECTION_MODE_CELL);
        
        //table.setDefaultCommand(new TableSelectCommand("Select", Command.OK, 1));
        //table.setItemCommandListener(m_listener);
    	
        final int len = rows.size();
        for(int i=0; i<len; i++) {
        	row = (Vector) rows.elementAt(i);
        	int len2 = row.size();
        	for(int k=0; k < len2; k++) {
        		//#debug info
        		System.out.println("Setting table item at " + k + ", " + i);
        		table.set(k, i, (Item) row.elementAt(k));
        	}
        }
        String caption = getCaption((XFormsElement) el);
        //#debug info
        System.out.println("Creating table " + caption);
        WrapperItem wrapper;
        if (el.getAttribute("appearance") == "layout") {
        	//#style layoutWrapper
        	wrapper = new WrapperItem(UiAccess.cast(table), caption);
        } else {
        	//#style wrapper
        	wrapper = new WrapperItem(UiAccess.cast(table), caption);        	
        }
        if (parent != null) {
        	parent.add(wrapper);
        }
        return wrapper;
        
    }    
    
    private Item addTableData(Element el, Container parent) {
    	//#debug info
    	System.out.println("Adding data to table");
        //#style tableData
        XFormsGroup group = new XFormsGroup(XFormsGroup.TABLE_DATA);
        return group;
    }
    
    private Item addTableHeader(Element el, Container parent) {
    	//#debug info
    	System.out.println("Adding header to table");
        //#style tableHeader
        XFormsGroup group = new XFormsGroup(XFormsGroup.TABLE_HEADER);
        return group;
    }
    
}



