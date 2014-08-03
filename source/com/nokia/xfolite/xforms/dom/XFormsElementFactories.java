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

import java.util.Hashtable;

import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;

public class XFormsElementFactories {
    private static Hashtable xformsElementFactories = new Hashtable();
    private static Hashtable htmlElementFactories = new Hashtable();
    
   public static Object getXFormsElement(String lname)
   {
	   return xformsElementFactories.get(lname);
   }
   public static Object getHTMLElement(String lname)
   {
	   return htmlElementFactories.get(lname);
   }
    
    private static ElementFactory MODEL_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new ModelElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory BIND_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new BindElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory INSTANCE_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new InstanceElement(doc, namespaceURI, prefix, localName);
        }
    };    
    private static ElementFactory SWITCH_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new SwitchElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory CASE_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new CaseElement(doc, namespaceURI, prefix, localName);
        }
    };    
    private static ElementFactory SBOUND_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new SingleBoundElement(doc, namespaceURI, prefix, localName);
        }
    };
    
    private static ElementFactory UPLOAD_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new UploadElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory VBOUND_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new ValueBoundElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory PARENT_VBOUND_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new ParentValueElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory PARENT_MULTI_VBOUND_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new ParentValueElement(doc, namespaceURI, prefix, localName, true);
        }
    };
    private static ElementFactory REPEAT_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new RepeatElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory ATTRIBUTE_REPEAT_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new AttributeRepeatElement(doc, namespaceURI, prefix, localName);
        }
    };
    
    private static ElementFactory REPEATITEM_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new RepeatItemElement(doc, namespaceURI, prefix, localName);
        }
    };    
    private static ElementFactory ITEMSET_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new ItemsetElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory ITEMSETITEM_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new ItemsetItemElement(doc, namespaceURI, prefix, localName);
        }
    };    
    private static ElementFactory MESSAGE_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new MessageElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory SETVALUE_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new SetvalueElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory SUBMISSION_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new SubmissionElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory SEND_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new SendElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory DISPATCH_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new DispatchElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory DELETE_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI, String prefix, String localName) {
            return new DeleteElement(doc, namespaceURI, prefix, localName);
        }
    };
    
    private static ElementFactory INSERT_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI, String prefix, String localName) {
            return new InsertElement(doc, namespaceURI, prefix, localName);
        }
    };
    
    private static ElementFactory TIMER_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI, String prefix, String localName) {
            return new TimerElement(doc, namespaceURI, prefix, localName);
        }
    };    
    private static ElementFactory STOP_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI, String prefix, String localName) {
            return new StopElement(doc, namespaceURI, prefix, localName);
        }
    };    private static ElementFactory START_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI, String prefix, String localName) {
            return new StartElement(doc, namespaceURI, prefix, localName);
        }
    };
    
    private static ElementFactory REFRESH_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI, String prefix, String localName) {
        	if (localName.equals("cancelrefresh"))
        		return new RefreshElement(doc, namespaceURI, prefix, localName, ActionElement.REFRESH,true);
        	else if (localName.equals("cancelrebuild"))
        		return new RefreshElement(doc, namespaceURI, prefix, localName, ActionElement.REBUILD,true);
        	else if (localName.equals("cancelrecalculate"))
        		return new RefreshElement(doc, namespaceURI, prefix, localName, ActionElement.RECALCULATE,true);
        	else if (localName.equals("cancelrewire"))
        		return new RefreshElement(doc, namespaceURI, prefix, localName, ActionElement.REWIRE,true);
        	else if (localName.equals("refresh"))
        		return new RefreshElement(doc, namespaceURI, prefix, localName, ActionElement.REFRESH,false);
        	else if (localName.equals("rebuild"))
        		return new RefreshElement(doc, namespaceURI, prefix, localName, ActionElement.REBUILD,false);
        	else if (localName.equals("rewire"))
        		return new RefreshElement(doc, namespaceURI, prefix, localName, ActionElement.REWIRE,false);
        	else 
        		return new RefreshElement(doc, namespaceURI, prefix, localName, ActionElement.RECALCULATE,false);
        }
    };
    private static ElementFactory ACTION_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI, String prefix, String localName) {
            return new ActionElement(doc, namespaceURI, prefix, localName); }};
            
    private static ElementFactory SETFOCUS_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new SetfocusElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory CLOSE_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new CloseElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory LOAD_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new LoadElement(doc, namespaceURI, prefix, localName);
        }
    };    
    private static ElementFactory RESET_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new ResetElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory SUBMIT_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new SubmitElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory LISTEN_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new ListenElement(doc, namespaceURI, prefix, localName);
        }
    };
    private static ElementFactory RESTORE_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new RestoreElement(doc, namespaceURI, prefix, localName);
        }
    }; 
    private static ElementFactory SAVE_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new SaveElement(doc, namespaceURI, prefix, localName);
        }
    }; 
    
    private static ElementFactory TOGGLE_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI, String prefix, String localName) {
            return new ToggleElement(doc, namespaceURI, prefix, localName);
        }
    };
    
    private static ElementFactory HTML_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new HTMLElement(doc, namespaceURI, prefix, localName);
        }
    };    

    private static ElementFactory PARENT_HTML_FACTORY = new ElementFactory() {
        public Element createElement(Document doc,
                String namespaceURI,
                String prefix,
                String localName) {
            return new ParentHTMLElement(doc, namespaceURI, prefix, localName);
        }
    };  
    
    static {
        xformsElementFactories.put("model", MODEL_FACTORY);
        xformsElementFactories.put("bind", BIND_FACTORY);
        xformsElementFactories.put("instance", INSTANCE_FACTORY);
        xformsElementFactories.put("switch", SWITCH_FACTORY);
        xformsElementFactories.put("case", CASE_FACTORY);
        xformsElementFactories.put("repeat", REPEAT_FACTORY);
        xformsElementFactories.put("repeat-item", REPEATITEM_FACTORY);
        xformsElementFactories.put("table-item", REPEATITEM_FACTORY);
        xformsElementFactories.put("itemset", ITEMSET_FACTORY);
        xformsElementFactories.put("item", ITEMSETITEM_FACTORY);
        xformsElementFactories.put("message", MESSAGE_FACTORY);
        xformsElementFactories.put("setvalue", SETVALUE_FACTORY);
        
        xformsElementFactories.put("listen", LISTEN_FACTORY);
        xformsElementFactories.put("restore", RESTORE_FACTORY);
        xformsElementFactories.put("save", SAVE_FACTORY);

        xformsElementFactories.put("trigger", SBOUND_FACTORY);
        xformsElementFactories.put("upload", UPLOAD_FACTORY);
        xformsElementFactories.put("input", SBOUND_FACTORY);
        xformsElementFactories.put("secret", SBOUND_FACTORY);
        xformsElementFactories.put("range", SBOUND_FACTORY);
        xformsElementFactories.put("select", SBOUND_FACTORY);
        xformsElementFactories.put("select1", SBOUND_FACTORY);
        xformsElementFactories.put("group", SBOUND_FACTORY);
        xformsElementFactories.put("output", VBOUND_FACTORY);
        xformsElementFactories.put("label", PARENT_VBOUND_FACTORY);
        xformsElementFactories.put("alert", PARENT_MULTI_VBOUND_FACTORY);
        xformsElementFactories.put("value", PARENT_VBOUND_FACTORY);
        xformsElementFactories.put("hint", PARENT_VBOUND_FACTORY);
        
        htmlElementFactories.put("p", HTML_FACTORY);    
        htmlElementFactories.put("img", HTML_FACTORY);
        htmlElementFactories.put("hr", HTML_FACTORY);

        htmlElementFactories.put("table", HTML_FACTORY);
        htmlElementFactories.put("thead", HTML_FACTORY);
        htmlElementFactories.put("tbody", ATTRIBUTE_REPEAT_FACTORY);
        htmlElementFactories.put("tfoot", HTML_FACTORY);
        htmlElementFactories.put("tr", HTML_FACTORY);
        htmlElementFactories.put("th", HTML_FACTORY);
        htmlElementFactories.put("td", HTML_FACTORY);
        htmlElementFactories.put("caption", PARENT_VBOUND_FACTORY);

        xformsElementFactories.put("submit", SUBMIT_FACTORY);
        xformsElementFactories.put("submission", SUBMISSION_FACTORY);
        xformsElementFactories.put("send", SEND_FACTORY);
        xformsElementFactories.put("dispatch", DISPATCH_FACTORY);
        xformsElementFactories.put("action", ACTION_FACTORY);
        xformsElementFactories.put("delete", DELETE_FACTORY);
        xformsElementFactories.put("insert", INSERT_FACTORY);
        xformsElementFactories.put("refresh", REFRESH_FACTORY);
        xformsElementFactories.put("rebuild", REFRESH_FACTORY);
        xformsElementFactories.put("recalculate", REFRESH_FACTORY);
        xformsElementFactories.put("rewire", REFRESH_FACTORY);
        xformsElementFactories.put("cancelrefresh", REFRESH_FACTORY);
        xformsElementFactories.put("cancelrebuild", REFRESH_FACTORY);
        xformsElementFactories.put("cancelrecalculate", REFRESH_FACTORY);
        xformsElementFactories.put("cancelrewire", REFRESH_FACTORY);
        xformsElementFactories.put("close", CLOSE_FACTORY);
        xformsElementFactories.put("load", LOAD_FACTORY);
        xformsElementFactories.put("start", START_FACTORY);
        xformsElementFactories.put("stop", STOP_FACTORY);
        xformsElementFactories.put("timer", TIMER_FACTORY);
        xformsElementFactories.put("reset", RESET_FACTORY);
        xformsElementFactories.put("setfocus", SETFOCUS_FACTORY);
        xformsElementFactories.put("toggle", TOGGLE_FACTORY);
    }

}
