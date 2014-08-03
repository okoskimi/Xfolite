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
import java.util.Vector;
import java.util.Calendar;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import de.enough.polish.ui.*;
import de.enough.polish.util.ArrayList;
import de.enough.polish.util.Locale;


import com.nokia.xfolite.client.ui.*;
import com.nokia.xfolite.client.util.EventQueue;
import com.nokia.xfolite.client.util.ThreadPool;
import com.nokia.xfolite.xforms.dom.*;
import com.nokia.xfolite.xforms.model.*;
import com.nokia.xfolite.xforms.model.datasource.*;
import com.nokia.xfolite.xforms.model.datatypes.*;
import com.nokia.xfolite.xforms.submission.*;
import com.nokia.xfolite.xml.dom.*;
import com.nokia.xfolite.xml.dom.events.*;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.nokia.xfolite.xml.xpath.XPathEvaluator;
import com.nokia.xfolite.xml.xpath.XPathResult;

import java.io.*;
import de.enough.polish.util.StringTokenizer;


//#if nokia.perfTrace.enabled
//#define tmp.perf
//#endif

/**
 * <p>
 * Provides an XForms form.
 * </p>
 * 
 * 
 * @author Oskari Koskimies
 */
public class PolishController implements UIListener {

	private PolishWidgetFactory m_factory;

	Container m_root;

	private XPathEvaluator m_xpath;

	private Display m_display;

	private Vector m_contextCmds;
	private UserInterface m_ui;
	
	public PolishController(Container rootContainer, Display display, UserInterface ui) {
		m_root = rootContainer;
		m_factory = new PolishWidgetFactory(this, m_root);
		m_xpath = new XPathEvaluator();
		m_display = display;
		m_contextCmds = new Vector();
		m_ui = ui;
	}

	public WidgetFactory getWidgetFactory() {
		return m_factory;
	}

	/** ********************* ItemCommandListener *************************** */

	public void commandAction(Command cmd, Item item) {
		//#debug
		System.out.println("Command: " + cmd);

		if (cmd instanceof ExecCommand) {
			//#debug 
			System.out.println("Executing: " + cmd);
			((XFormsForm) m_root.getScreen()).setBusy(true);
			m_display.callSerially(new StateChangedTask(cmd, item));
		} else {
			//#debug warn
			System.out.println("Ignoring: " + cmd);
		}
	}

	/** ********************** ItemStateListener **************************** */

	class StateChangedTask implements Runnable {
		private Item comp;
        int selectedIndex;

		private Command cmd;

		StateChangedTask(Item comp, int index) {
			this.comp = comp;
			this.cmd = null;
            this.selectedIndex = index;
		}

		StateChangedTask(Command cmd, Item comp) {
			this.comp = comp;
			this.cmd = cmd;
		}

		public void run() {
			try {
				if (cmd == null) {
					doItemStateChanged(comp, selectedIndex);
				} else {
					((ExecCommand) cmd).execute(comp);
				}
			} catch (Exception ex) {
				//#debug warn
				System.out.println("Could not do state change " + ex);
			} finally {
				((XFormsForm) m_root.getScreen()).setBusy(false);
			}

		}

	};

	public void itemStateChanged(Item comp) {
		//#debug info
		System.out.println("itemStateChanged " + comp);

		if (comp instanceof WrapperItem) {
			comp = ((WrapperItem)comp).getItem();
		}
		int selectedIndex = -1;
        if (comp instanceof SelectItem) {
            selectedIndex = ((SelectItem)comp).getSelectedIndex();
            //#debug info
            System.out.println("SelectedIndex: " + selectedIndex);
        }
        
		if (comp instanceof XF_TextField) {
			XFormsElement el = (XFormsElement) comp.getAttribute(PolishWidgetFactory.ELEMENT_ATTR);
			if (el.getAttribute("incremental") != "true") {
				//#debug
				System.out.println("Ignoring textfield change for non-incremental input");
				return;
			}
		}
		if (comp instanceof XF_DateField) {
			return;
		}

		((XFormsForm) m_root.getScreen()).setBusy(true);
		StateChangedTask sct = new StateChangedTask(comp, selectedIndex);
		m_display.callSerially(sct);
		//sct.run();
	}

	public void doItemStateChanged(Item comp, int selectedIndex) {

		//#debug info
		System.out.println("doItemStateChanged " + comp + ", " + selectedIndex);
		try {
			XFormsElement el = (XFormsElement) comp
			.getAttribute(PolishWidgetFactory.ELEMENT_ATTR);
			//#debug info
			System.out.println("Element: " + el);
			if (el instanceof BoundElement) {
				//#debug info
				System.out.println("Element is a BoundElement");
				BoundElement binding = (BoundElement) el;
				boolean recalc = false;
				if (comp instanceof XF_Button) {
					recalc = buttonChanged((XF_Button) comp, binding);
				} else if (UiAccess.cast(comp) instanceof XF_ChoiceTextField) { 
					// Doh! We have to check whether the choice is valid or not
					// because
					// we also get the intermediate changes!
					XF_ChoiceTextField ctf = (XF_ChoiceTextField) UiAccess.cast(comp);
					// We need to make the CTF handle its own state change but it
					// will not if there
					// is an existing listener...
					//#debug
					System.out.println("Got statechanged for XF_ChoiceTextField ("
							+ binding.getLocalName() + ")");

					String[] names = (String[]) comp
					.getAttribute(PolishWidgetFactory.NAME_ATTR);
					boolean okVal = false;
					if (binding.getLocalName() == "select1") {
						if (binding.getAttribute("selection") == "open") {
							recalc = choiceTextFieldChanged(ctf, binding);
							okVal = true;
						} else {
							String name = ctf.getText();
							for (int i = 0; i < names.length; i++) {
								if (names[i].equals(name)) {
									//#debug
									System.out
									.println("Invoking XForms notifychange, name = "
											+ name);
									recalc = choiceTextFieldChanged(ctf, binding);
									okVal = true;
									break;
								}
							}
						}
					}
					if (okVal) {
						//#debug
						System.out.println("Invoking XForms notifychange, val = "
								+ ctf.getText());
						recalc = choiceTextFieldChanged(ctf, binding);
					} else {
						((Form) ctf.getScreen()).setItemStateListener((ItemStateListener)null);
						//#debug
						System.out.println("Running ctf.notifystatechanged()...");
						ctf.notifyStateChanged();
						((Form) ctf.getScreen()).setItemStateListener(this);
					}

				} else if (comp instanceof Range) {
					recalc = rangeChanged((Range) comp, binding);
				} else if (comp instanceof DateField) {
					recalc = dateFieldChanged((DateField) comp, binding);
				} else if (UiAccess.cast(comp) instanceof XF_FilteredChoiceGroup) {
					recalc = selectItemChanged((SelectItem) comp, binding, selectedIndex);
				} else if (comp instanceof XF_TextField) {
					if (binding.getAttribute("incremental") == "true") {
						recalc = stringItemChanged((StringItem) comp, binding);
					}
					// Otherwise do nothing; we handle it in DOM_FOCUS_OUT XForms
					// event handler below
				} else if (comp instanceof StringItem) {
					recalc = stringItemChanged((StringItem) comp, binding);
				} else if (comp instanceof ChoiceGroup) {
					//#debug info
					System.out.println("Change in ChoiceGroup");
					if (el.getLocalName().equals("input")) {
						recalc = checkboxChanged((SelectItem) comp, binding);
					} else {
						recalc = selectItemChanged((SelectItem) comp, binding, selectedIndex);
					}
				} else if (comp instanceof TabBar && comp.getParent() instanceof SelectItem) {
					//#debug info
					System.out.println("Change in TabBar");
					recalc = selectItemChanged((SelectItem) comp.getParent(), binding, ((TabBar)comp).getNextTab());
				}
				if (recalc) {
					((XFormsDocument) binding.getOwnerDocument()).getModelElement()
					.recalculate();
					((XFormsDocument) binding.getOwnerDocument()).getModelElement()
					.refresh(); // This will cause VALUE_CHANGED events

				}
			} else if (el instanceof SwitchElement && comp != null && comp.getParent() instanceof LabeledTabItem) {
				//#debug info
				System.out.println("Element is a SwitchElement");
				switchChanged((LabeledTabItem) comp.getParent(), (SwitchElement) el, selectedIndex);
/*			} else if (UiAccess.cast(comp) instanceof TableItem) {
				//#debug info
				System.out.println("Got ui event from tableitem!");
				TableItem table = (TableItem) UiAccess.cast(comp);
				Object item = table.getSelectedCell();
				if (item instanceof ChoiceItem) {
					Object repeatItem = ((ChoiceItem)item).getAttribute(PolishWidgetFactory.ELEMENT_ATTR);
					if (repeatItem instanceof RepeatItemElement) {
						//#debug info
						System.out.println("Selecting the repeatItem");
						((RepeatItemElement)repeatItem).setAsSelected();
					}
				}
*/			} else {
				//#debug warn
				System.out.println("Ignoring item change, not a bound element: " + el.getLocalName());
			}
		} catch (Exception ex) {
			//#debug error
			System.out.println(
					Locale.get("forms.error.errorWhenHandlingUserInput")
					+ ": " + ex);
		}
		//#debug info
		System.out.println("Exit itemChanged()");
	}

	public boolean buttonChanged(XF_Button button, BoundElement binding) {
		binding.dispatchLocalEvent(DOMEvent.DOM_ACTIVATE);
		// for output with mediatype
		/*
		 * { if (el instanceof BoundElement) { String ln = el.getLocalName(); if
		 * ("output".equals(ln)) { String mtype = el.getAttribute("mediatype");
		 * if (mtype!=null&&mtype.length()>0) { String val =
		 * ((BoundElement)o).getStringValue(); boolean stopped =
		 * this.stopMedia(); if (!stopped) this.playMedia(val, mtype); } } } }
		 */
		return true;
	}

	public boolean rangeChanged(Range range, BoundElement binding) {
		//#debug
		System.out.println("Range changed");

		DataTypeBase dataType = binding.getDataType();

		if (dataType == null) {
			binding.logError(Locale.get("forms.error.noDataTypeFound"));
			return false;
		}
		int typeId = dataType.getBaseTypeID();
		if (typeId == DataTypeBase.XML_SCHEMAS_DECIMAL) {
			((DataTypeDecimal) dataType).setDoubleValue(range.getRangeValue(),
					binding);
		} else if (typeId == DataTypeBase.XML_SCHEMAS_INTEGER) {
			((DataTypeInteger) dataType).setDoubleValue(range.getRangeValue(),
					binding);
		} else {
			binding.logError(Locale.get("forms.error.rangeSupportsOnlyDecimalAndInteger"));
			// debug
			System.out.println("Wrong typeId:" + typeId);
			return false;
		}
		return true;
	}

	public boolean dateFieldChanged(DateField dateField, BoundElement binding) {
		//#debug
		System.out.println("DateField changed");
		DataTypeBase dataType = binding.getDataType();
		if (dataType == null) {
			binding.logError(Locale.get("forms.error.noDataTypeFound"));
			return false;
		}
		if (dataType instanceof DataTypeDate) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateField.getDate());		
			((DataTypeDate)dataType).setCalendarValue(cal, binding);
			return true;
		} else {
			binding.logError(Locale.get("forms.error.dateFieldHasIncompatibleType"));
			return false;
		}
	}

	
	public boolean stringItemChanged(StringItem strItem, BoundElement binding) {
		String val;
		if (strItem instanceof TextField) { // getText will not work for password input
			val = ((TextField)strItem).getString();
		} else {
			val = strItem.getText();
		}
		if (val == null) {
			val = "";
		}
		System.out.println("Val:" + val);
		if (val.length() == 0) {
			// Special case: It should always be possible to enter empty value (if control allows it)
			binding.setStringValue(val);
			return true;
		}
		
		DataTypeBase dt = binding.getDataType();
		System.out.println("DT:" + dt.getTypeName());
		if (dt instanceof DataTypeInteger) {
			//#debug
			System.out.println("is integer");
			int ival = 0;
			try {
				ival = Integer.parseInt(val);
				//#debug
				System.out.println("ival:" + ival);
			} catch (Exception ignore) {
			}
			binding.setStringValue(Integer.toString(ival));
		} else if (dt instanceof DataTypeDecimal) {
			//#debug
			System.out.println("is decimal");
			double dval = 0.0;
			try {
				dval = Double.parseDouble(val);
				//#debug
				System.out.println("dval:" + dval);
			} catch (Exception ignore) {
			}
			binding.setStringValue(Double.toString(dval));
		} else {
			//#debug
			System.out.println("is string");
			binding.setStringValue(val);
		}
		return true;
	}

	public boolean checkboxChanged(SelectItem c, BoundElement binding) {
        //#debug info
        System.out.println("checkboxChanged");
		if (c.isSelected(0)) {
			binding.setStringValue("true");
		} else {
			binding.setStringValue("false");
		}
		return true;
	}
	
	public boolean selectItemChanged(SelectItem c, BoundElement binding, int selectedIndex) {
		//#debug info
		System.out.println("Enter selectItemChanged: " + c.getClass().getName());
		/*
		 * Not supported yet if (binding instanceof UploadElement) {
		 * uploadItemEvent(comp,binding); } else
		 */
		{ // Note: This works for both select and select1
			String values[] = (String[]) ((Item) c)
					.getAttribute(PolishWidgetFactory.VALUE_ATTR);
			int len = c.size();
			boolean[] selArray = new boolean[len];
			
			// FIXME: This is a workaround for a J2ME Polish bug:
			// For implicit ChoiceGroups the getSelectedFlags will have two True values,
			// the previously selected value and the value the user selected now.
			// getSelectedIndex however works correctly and returns the value the user selected.
			if (binding.getLocalName() == "select1") {
				int selInd =  selectedIndex; //c.getSelectedIndex();
				//#debug info
				System.out.println("select1, selected index = " + selInd);
				if (selInd >= 0) {
					selArray[selInd] = true;
				}
			} else {
				//#debug info
				System.out.println("Not select1, using getSelectedFlags");
				c.getSelectedFlags(selArray);
			}

			StringBuffer sb = null;
			for (int i = 0; i < len; i++) {
				if (selArray[i]) {
					//#debug info
					System.out.println("selArray[" + i + "] is true, appending value " + values[i]);
					if (sb == null) {
						sb = new StringBuffer(values[i]);
					} else {
						sb.append(' ').append(values[i]);
					}
				} else {
					//#debug info
					System.out.println("selArray[" + i + "] is false");					
				}
			}

			binding.setStringValue(sb == null ? "" : sb.toString());

			// -1 for multiple choice or no selection
			int sel1 = selectedIndex; // c.getSelectedIndex();
			if (sel1 >= 0) {
				String value = values[sel1];
				//#debug info
				System.out.println("Searching for item with xf:value = " + value);
				Node item = m_xpath.evaluate(
						".//xf:item[string(xf:value) = '" + value + "']",
						binding,
						this.m_factory,
						XPathResult.NODE).asNode();
				if (item != null) {
					//#debug info
					System.out.println("Dispatching XFORMS_SELECT...");
					//#debug info
					System.out.println("Target node: " + item.getNodeName() + "(" + item.getText() + ")");
					// We have to process the select1 change first so that
					// changes in visibility have been realized and target
					// element is visible
					// in case there is a setfocus action
					((XFormsDocument) binding.getOwnerDocument())
							.getModelElement().recalculate();
					((XFormsDocument) binding.getOwnerDocument())
							.getModelElement().refresh();
					((XFormsElement) item).dispatchEvent(DOMEvent.XFORMS_SELECT);
					//#debug info
					System.out.println("Exit choiceGroupChanged");
					return false; // so we don't run recalculate and refresh
									// twice.

				} else {
					//#debug error
					System.out.println(
							Locale.get("forms.error.noMatchingSelectItem")
							+ ": " + value);
				}
			}
		}

		//#debug info
		System.out.println("Exit choiceChanged");
		return true;
	}

	public boolean choiceTextFieldChanged(XF_ChoiceTextField ctf,
			BoundElement binding) {
		//#debug
		System.out.println("Enter choiceTextFieldChanged");
		String name = ctf.getText();
		String value = null;
		String[] values = (String[]) ctf
				.getAttribute(PolishWidgetFactory.VALUE_ATTR);
		String[] names = (String[]) ctf
				.getAttribute(PolishWidgetFactory.NAME_ATTR);
		//#debug
		System.out.println("Values=" + values + ", names=" + names);
		for (int i = 0; i < names.length; i++) {
			if (names[i].equals(name)) {
				value = values[i];
				break;
			}
		}

		binding
				.setStringValue(value == null ? ctf.getText() : value
						.toString());
		if (value != null) {
			if (binding.getLocalName() == "select1") {
				Node item = m_xpath.evaluate(
						".//xf:item[string(xf:value) = '" + value.toString()
								+ "']", binding,
						this.m_factory,
						XPathResult.NODE).asNode();
				if (item != null) {
					//#debug
					System.out.println("Dispatching XFORMS_SELECT...");
					// We have to process the select1 change first so that
					// changes in visibility have been realized and target
					// element is visible
					// in case there is a setfocus action
					((XFormsDocument) binding.getOwnerDocument())
							.getModelElement().recalculate();
					((XFormsDocument) binding.getOwnerDocument())
							.getModelElement().refresh();
					((XFormsElement) item).dispatchEvent(DOMEvent.XFORMS_SELECT);
					//#debug
					System.out.println("Exit choiceTextFieldChanged");
					return false; // so we don't run recalculate and refresh
									// twice.

				} else {
					//#debug error
					System.out.println(
							Locale.get("forms.error.noMatchingSelectItem")
							+ ": " + value);
				}
			}
		}

		//#debug
		System.out.println("Exit choiceTextFieldChanged");
		return true;
	}

	public void switchChanged(LabeledTabItem tab, SwitchElement switchEl, int selectedIndex) {
		int newTab = selectedIndex; // FilteredChoiceGroup
		if (newTab == -1) { // TabBar
			newTab = tab.getActiveTab();
		}
		//#debug info
		System.out.println("tabBarChanged to " + newTab);
		Vector caseList = (Vector) switchEl.getUserData("case");
		//#debug info
		System.out.println("caseList: " + caseList);
		if (caseList != null) {
			switchEl.setSelectedCase((CaseElement) caseList.elementAt(newTab));
		}
		/*
		 * // Force current item to be shown for lower tab Boolean isLower =
		 * (Boolean) tab.getAttribute(PolishWidgetFactory.LTAB_ATTR); if
		 * (isLower == Boolean.TRUE) { if (tab instanceof XFormsItem) { Item
		 * parent = ((XFormsItem)tab).getParent(); if (parent instanceof
		 * Container && !(udata instanceof LabeledTabItem)) { Container c =
		 * (Container)parent; c.requestDefocus(tab);
		 * c.focus(c.getPosition(tab)); } }
		 *  }
		 */
	}

	/**
	 * ************************ XForms DOM Event Listener
	 * ****************************
	 */

	class SetFocusTask implements Runnable {
		private Item comp;

		SetFocusTask(Item comp) {
			this.comp = comp;
		}

		public void run() {
			VisibilityManager.moveFocusTo(comp);
		}

	};

	class ScrollToTask implements Runnable {
		private Item comp;

		ScrollToTask(Item comp) {
			this.comp = comp;
		}

		public void run() {
			VisibilityManager.scrollTo(comp);
		}

	};

	class InitializeTask implements Runnable {
		private CaseElement el;

		InitializeTask(CaseElement el) {
			this.el = el;
		}

		public void run() {
			try {
				el.ensureInitialized();
			} catch (Exception ex) {
				//#debug error
				System.out.println(
						Locale.get("forms.error.initializationError")
						+ ": " + ex);
			}
		}

	};

	class ReplaceTask implements Runnable {
		private Item oldItem, newItem;

		private Container parent;

		ReplaceTask(Container parent, Item oldItem, Item newItem) {
			this.parent = parent;
			this.oldItem = oldItem;
			this.newItem = newItem;
		}

		public void run() {
			try {
				//#if tmp.perf
				long start = System.currentTimeMillis();
				System.out.println("Replacing " + oldItem);
				//#endif
				int index = parent.getPosition(oldItem);
				parent.set(index, newItem);
				//#if tmp.perf
				long t = System.currentTimeMillis() - start;
				System.out.println("Replace took " + t + " ms");
				//#endif
			} catch (Exception ex) {
				//#debug info
				System.out.println("Error when replacing: " + ex);
			}
		}
	};

	public void handleEvent(DOMEvent ev) {		
		//#debug
		System.out.println("Got event " + DOMEvent.getNameFromType(ev.getType())
				+ " at <" + ev.getCurrentTarget().getLocalName() + "> and phase " + ev.getEventPhase() + " (1=capture, 2=target, 3=bubble)");

		XFormsElement node = (XFormsElement) ev.getTarget();
		Item comp = (Item) node.getUserData();
		WrapperItem wrapper = null;
		if (comp instanceof WrapperItem) {
			wrapper = (WrapperItem) comp;
			comp = wrapper.getItem();
		}
		//#debug
		System.out.println("Comp: " + comp);

		Node parentNode = node.getParentNode();
		Container parent = m_root;
		while (parentNode instanceof Element) {
			Object udata = ((Element) parentNode).getUserData();
			if (udata instanceof Container
					&& !(udata instanceof LabeledTabItem)) {
				parent = (Container) udata;
				break;
			}
			parentNode = parentNode.getParentNode();
		}

		// Process all bubble- or capture phase handlers here
		if (ev.getEventPhase() != DOMEvent.AT_TARGET) {
			//#debug
			System.out.println("Processing bubbling or captured event "
					+ DOMEvent.getNameFromType(ev.getType()) + " at <"
					+ ev.getCurrentTarget().getLocalName() + ">");

			if (comp == null && ev.getType() == DOMEvent.XFORMS_VALUE_CHANGED) {
				node = (XFormsElement) ev.getCurrentTarget();
				if (node instanceof BoundElement || node instanceof HTMLElement) {
					comp = (Item) node.getUserData();
					wrapper = null;
					if (comp instanceof WrapperItem) {
						wrapper = (WrapperItem) comp;
						comp = wrapper.getItem();
						Vector alerts = PolishWidgetFactory.getAlerts(node);
						wrapper.setAlerts(alerts);
					}
					if (comp instanceof XF_Button) {
						String label = PolishWidgetFactory.getLabel(node);
						((XF_Button) comp).setText(label);
					} else if (UiAccess.cast(comp) instanceof LabeledSeparator) {
						String label = PolishWidgetFactory.getLabel(node);
						((LabeledSeparator) UiAccess.cast(comp)).setText(label);
					} else if (comp instanceof XFormsGroup) {
						// Do nothing, they don't have labels right now (but
						// could have, some day...)
					} else if (comp instanceof Item) {
						String label = PolishWidgetFactory.getLabel(node);
						System.out.println("Setting label for item (or wrapper): " + comp);
						System.out.println("Setting label: " + label);
						// Item parentComp = comp.getParent();
						if (wrapper != null) {
							//#debug
							System.out.println("Setting label for wrapper instead: " + label);
							wrapper.setLabel(label);
							// I *think* this was left-over from old wrapper solution and is no longer necessary...
					/*	} else if (parentComp instanceof WrapperItem) {
							((WrapperItem)parentComp).setLabel(label); */
						} else {
							((Item) comp).setLabel(label);
						}
					} else {
						//#debug error
						System.out.println(
								Locale.get("forms.error.unableToSetLabelForClass")
								+ ": "
								+ comp);
					}

				}
			}
			return;
		}

		// Process here all cases where element does not have a GUI object
		if (comp == null) {
			//#debug
			System.out.println("Processing event without GUI target "
					+ DOMEvent.getNameFromType(ev.getType()) + " at <"
					+ ev.getCurrentTarget().getLocalName() + ">");
			if (ev.getType() == DOMEvent.XFORMS_ENABLED
					&& node.getLocalName() == "message") {

				String msg = ((MessageElement) node).getText();
				m_ui.showMessage(msg);
			} else if (ev.getType() == DOMEvent.XFORMS_CLOSE) {
				//#debug
				System.out.println("Closing form");
				((XFormsForm) m_root.getScreen()).close();
			} else if (ev.getType() == DOMEvent.XFORMS_RESET) {
				// FIXME: Rest form
				//#debug error
				System.out.println(Locale.get("forms.error.resetNotImplemented"));
			} else if (ev.getType() == DOMEvent.XFORMS_STRUCTURE_CHANGED) {
		        //#if tmp.perf
				System.out.println("Structure changed. ReEval context. ");
				long ts = System.currentTimeMillis();
				//#endif
				Node n = ev.getTarget();
				XFormsDocument doc = (XFormsDocument) node.getOwnerDocument();
				doc.reEvaluateContext(null, true);
				XFormsModelUI mui = doc.getModelUI();
		        //#if tmp.perf
				System.out.println("Re-eval took: "
						+ (System.currentTimeMillis() - ts) + " ms.");
				//#endif
				mui.refresh(); // is this really needed after
								// reEvaluateContext?
		        //#if tmp.perf
				System.out.println("Re-eval + refresh took: "
						+ (System.currentTimeMillis() - ts) + " ms.");
				//#endif
			}
			/*
			else if(ev.getType() == DOMEvent.XFORMS_REBUILD_CONTROL
					&& node.getLocalName() == "tbody") {
				//#debug info
				System.out.println("Rebulding table...");
				comp = (Item) parentNode.getUserData();
				Item newComp = m_factory.addTable((Element) parentNode, null);
				if (comp == parent) {
					//#debug error
					System.out.println("Rebuilding table: Parent == Table");
				}
				m_display.callSerially(new ReplaceTask(parent, comp, newComp));
				parentNode.setUserData(newComp);
			
			}
			*/
			return;
		}

		//#debug
		System.out.println("Processing at-target event "
				+ DOMEvent.getNameFromType(ev.getType()) + " at <"
				+ ev.getCurrentTarget().getLocalName() + ">");

		switch (ev.getType()) {

		case DOMEvent.XFORMS_BINDING_CHANGED:
		case DOMEvent.XFORMS_VALUE_CHANGED:
			//#debug info
			System.out.println("VALUE/BINDING_CHANGED");
			if (ev.getType() == DOMEvent.XFORMS_BINDING_CHANGED) {
				//#debug 
				System.out.println("Actually BINDING_CHANGED");
			}
			//#debug info
			System.out.println("Target: " + node.getLocalName());
			//#debug info
			System.out.println("ref = " + node.getAttribute("ref"));

			if (node instanceof BoundElement) {
				BoundElement binding = (BoundElement) node;
				//#debug info
				System.out.println("val = "
						+ binding.getStringValue());
				setValue(comp, binding);
				if (wrapper != null && binding.getBooleanState(MIPExpr.REQUIRED)) {
					//#debug info
					System.out.println("Refreshing required state");
        			boolean empty = false;
    				String value = binding.getStringValue();        			
        			if (value == null || value.length() == 0) {
        				empty = true;
        			}
        			wrapper.setRequired(true, empty);
				}
			}
			//#debug info
			System.out.println("Done processing VALUE/BINDING CHANGED");
			break;

		case DOMEvent.XFORMS_FOCUS:
			//#debug info
			System.out.println("XFORMS_FOCUS: " + node.getLocalName());

			// FIXME: It may be that we should redraw before focusing
			// so that other UI actions are run.
			// If the to-be-focused component only just became visible
			// we have a problem in that case (visibility change was
			// not yet run).
			// FIXME REALLY: Currently problem seems more to be that
			// Polish does not properly scroll to the focused item when
			// containers are being used. Maybe related to similar issue
			// with XF_ChoiceTextField selection list?

			// FIXME: Should check whether it is really focusable. This is not
			// quite the same
			if (node instanceof ValueBoundElement) {
				m_display.callSerially(new ScrollToTask(comp));
			} else {
				// UiAccess.focus(comp.getScreen(), comp);
				m_display.callSerially(new SetFocusTask(comp));
			}
			break;

		case DOMEvent.DOM_FOCUS_IN:
			//#debug info
			System.out.println("DOM_FOCUS_IN: " + node.getLocalName());
			// Remove old contextual commands
			Screen scr = comp.getScreen();
			if (scr == null) { // TODO: Nasty hack because it might return null
								// if the item is not initialized yet
				scr = (Screen) this.m_root.getScreen();
			}
			//#debug
			System.out.println("Screen: " + scr);
			if (!m_contextCmds.isEmpty()) {
				//#debug
				System.out.println("Clearing commands");
				final int len = m_contextCmds.size();
				for (int i = 0; i < len; i++) {
					//#debug
					System.out.println("Clearing command: "
							+ m_contextCmds.elementAt(i));
					scr.removeCommand((Command) m_contextCmds.elementAt(i));
				}
				m_contextCmds.removeAllElements();
			}
			//#debug
			System.out.println("Adding context commands");
			Node n = node;
			int priority = 2;
			StringBuffer path = null;
			while (n instanceof Element) {
				//#debug
				System.out.println("Clearing commands: looking at "
						+ n.getLocalName());
				if (n instanceof XFormsElement) {
					XFormsElement xf = (XFormsElement) n;
					String label = PolishWidgetFactory.getLabel(xf);
					Vector cmdList = (Vector) xf
							.getUserData(PolishWidgetFactory.CMDS_ATTR);
					if (cmdList != null) {
						Command labelCmd = null;
						if (label != "") {
							//#debug
							System.out.println("Adding labelcmd:" + label);
							labelCmd = new Command(label, Command.SCREEN,
									priority);
							scr.addCommand(labelCmd);
							m_contextCmds.addElement(labelCmd);
						}
						final int len = cmdList.size();
						for (int i = 0; i < len; i++) {
							//#debug
							System.out.println("Adding cmd:"
									+ ((Command) cmdList.elementAt(i))
											.getLabel());
							if (labelCmd != null) {
								UiAccess.addSubCommand((Command) cmdList
										.elementAt(i), labelCmd, scr);
							} else {
								scr.addCommand((Command) cmdList.elementAt(i));
							}
							m_contextCmds.addElement(cmdList.elementAt(i));
						}
					}
					if (n instanceof CaseElement && label == "") {
						Node nParent = n.getParentNode();
						if (nParent instanceof SwitchElement) {
							Vector caseList = (Vector) ((SwitchElement) nParent)
									.getUserData("case");
							if (caseList != null) {
								int caseIndex = caseList.indexOf(n);
								if (caseIndex >= 0) {
									label = Integer.toString(caseIndex);
								}
							}
						}
						// Fallback if label still empty, though this should
						// never happen
						if (label == "") {
							label = "?";
						}
					}
					if (n != node && label != "") {
						//#debug
						System.out.println("Adding context label for "
								+ n.getLocalName());
						if (path == null) {
							path = new StringBuffer(label);
						} else {
							if (n instanceof SwitchElement) {
								path.insert(0, ": ");
							} else {
								path.insert(0, " >> ");
							}
							path.insert(0, label);
						}
					}
				}
				n = n.getParentNode();
				priority++;
			}
			//#debug
			System.out.println("Done adding commands");

			if (scr instanceof XFormsForm) {
				//#debug
				System.out.println("Setting context to: " + path);
				if (path != null) {
					((XFormsForm) scr).setContext(path.toString());
				} else {
					((XFormsForm) scr).setContext(null);
				}
			}
			break;

		case DOMEvent.DOM_FOCUS_OUT:
			//#debug
			System.out.println("DOM_FOCUS_OUT");
			if (node instanceof BoundElement) {
				BoundElement bound = (BoundElement) node;
				boolean recalc = false;
				if (comp instanceof XF_TextField) {				
					StringItem si = (StringItem) comp;
					// This serves both non-incremental text input fields and
					// incremental ones
					// for the case where user input has same value as current value
					// but
					// textual representation needs to be adjusted (123 vs. 123.0)
					String curVal = bound.getStringValue();
					//TODO: Throw XForms exception event if curVal == null (?)
					if (curVal != null && !curVal.equals(si.getText())) {
						if (bound.getAttribute("incremental") == "true") {
							//#debug
							System.out.println("Setting text to current value on focus exit");
							si.setText(curVal);
						} else {
							//#debug
							System.out.println("Sending stringItemChanged");
							recalc = stringItemChanged(si, bound);

						}
					}
				} else if (comp instanceof XF_DateField) {
					recalc = dateFieldChanged((DateField) comp, bound);
				}
				
				if (recalc) {
					XFormsDocument  doc = (XFormsDocument) node.getOwnerDocument();
					doc.getModelElement().recalculate();
					doc.getModelElement().refresh();
					// This will
					// cause
					// VALUE_CHANGED
					// events
				}
			}
			break;

		case DOMEvent.XFORMS_SELECT:
			//#debug info
			System.out.println("XFORMS_SELECT: " + comp);
			if (comp instanceof ChoiceItem) {
				((ChoiceItem) comp).select(true);
			}
			break;
			
		case DOMEvent.XFORMS_DESELECT:
			//#debug info
			System.out.println("XFORMS_DESELECT: " + comp);
			if (comp instanceof ChoiceItem) {
				((ChoiceItem) comp).select(false);
			}
			break;
			
			
		case DOMEvent.XFORMS_ENABLED:
			//#debug
			System.out.println("ENABLED");

			if (node.getLocalName() == "case"
					&& node.getParentNode() instanceof SwitchElement) {
				m_display
						.callSerially(new InitializeTask(((CaseElement) node)));

				SwitchElement switchEl = (SwitchElement) node.getParentNode();
				XF_TabItem ltab = (XF_TabItem) switchEl
						.getUserData(PolishWidgetFactory.LTAB_ATTR);
				XF_TabItem utab = (XF_TabItem) switchEl
						.getUserData(PolishWidgetFactory.UTAB_ATTR);
				if (ltab != null || utab != null) {

					Vector caseList = (Vector) switchEl.getUserData("case");
					final int len = caseList.size();

					//#debug
					System.out.println("Setting active tabs");

					for (int i = 0; i < len; i++) {
						XFormsElement caseEl = (XFormsElement) caseList
								.elementAt(i);
						if (caseEl == node) {
							if (utab != null) {
								if (i != utab.getActiveTab()) {
									//#debug
									System.out.println("Setting upper tab");

									utab.setActiveTab(i);
								}
							}
							if (ltab != null) {
								if (i != ltab.getActiveTab()) {
									//#debug
									System.out.println("Setting lower tab");
									ltab.setActiveTab(i);
								}
							}
							break;
						}
					}
				} else {
					//#debug
					System.out.println("No tabs for this switch!");
				}
			}
			if (wrapper != null) {
				UiAccess.setVisible(wrapper, true);
			} else {
				UiAccess.setVisible(comp, true);
			}

			break;

		case DOMEvent.XFORMS_DISABLED:
			//#debug
			System.out.println("DISABLED");

			if (wrapper != null) {
				UiAccess.setVisible(wrapper, false);
			} else {
				UiAccess.setVisible(comp, false);
			}
			break;
			
		case DOMEvent.XFORMS_READONLY:
			//#debug info
			System.out.println("READONLY");
			if (wrapper != null) {
				wrapper.setReadOnly(true);
			}
			break;

		case DOMEvent.XFORMS_READWRITE:
			//#debug info
			System.out.println("READWRITE");
			if (wrapper != null) {
				wrapper.setReadOnly(false);
			}
			break;			

		case DOMEvent.XFORMS_REQUIRED:
			//#debug info
			System.out.println("REQUIRED");
			if (node instanceof BoundElement) {
				BoundElement binding = (BoundElement) node;
				XFormsDocument doc = (XFormsDocument) node.getOwnerDocument();
				doc.addRequired(binding);
				if (wrapper != null) {
        			boolean empty = false;
    				String value = binding.getStringValue();        			
        			if (value == null || value.length() == 0) {
        				empty = true;
        			}
					wrapper.setRequired(true, empty);
				}
			}

			break;			

		case DOMEvent.XFORMS_OPTIONAL:
			//#debug info
			System.out.println("OPTIONAL");
			if (node instanceof BoundElement) {
				BoundElement binding = (BoundElement) node;
				XFormsDocument doc = (XFormsDocument) node.getOwnerDocument();
				doc.removeRequired(binding);
				if (wrapper != null) {
					// the second parameter does not matter if first one is false
					wrapper.setRequired(false, false);
				}
			}

			break;			
			
		case DOMEvent.XFORMS_TYPE_CHANGED:
			// We don't actually support type change, just set visibility if
			// bound or not
			// We consider "not bound" to be a kind of null type, which is
			// why TYPE_CHANGED
			// also informs of change in binding state.

			//#debug
			System.out.println("TYPE_CHANGED");
			boolean visible = true;
			if (node instanceof BoundElement) { // This _should_ always be
				// true, otherwise we don't
				// get the event
				visible = ((BoundElement) node).isBound();
			}

			//#debug 
			System.out.println("Setting component to visible: " + comp + "(<" + node.getLocalName() + ">)");
			if (wrapper != null) {
				UiAccess.setVisible(wrapper, visible);
			} else {
				UiAccess.setVisible(comp, visible);
			}
			break;

		case DOMEvent.XFORMS_VALID:
			//#debug
			System.out.println("VALID");
			/*
			 * if (comp instanceof Choice) {
			 * comp.setStyle(StyleSheet.getStyle("form.choice")); } else if
			 * (comp instanceof XF_TextField) {
			 * comp.setStyle(StyleSheet.getStyle("form.input")); } else if (comp
			 * instanceof Gauge) {
			 * comp.setStyle(StyleSheet.getStyle("form.range")); }
			 */
			/*
			Item validAlert = (Item) comp
					.getAttribute(PolishWidgetFactory.ALERT_ATTR);
			if (validAlert != null) {
				UiAccess.setVisible(validAlert, false);
			}
			*/
			
			if (wrapper != null) {
				wrapper.setValid(true);
			}

			break;

		case DOMEvent.XFORMS_INVALID:
			//#debug
			System.out.println("INVALID");
			/*
			 * if (comp instanceof Choice) {
			 * comp.setStyle(StyleSheet.getStyle("form.choice.invalid")); } else
			 * if (comp instanceof XF_TextField) {
			 * comp.setStyle(StyleSheet.getStyle("form.input.invalid")); } else
			 * if (comp instanceof Gauge) {
			 * comp.setStyle(StyleSheet.getStyle("form.range.invalid")); }
			 */
			/*
			Item invalidAlert = (Item) comp
					.getAttribute(PolishWidgetFactory.ALERT_ATTR);
			if (invalidAlert != null) {
				UiAccess.setVisible(invalidAlert, true);
			}
			*/
			if (wrapper != null) {
				wrapper.setValid(false);
			}
			break;
	
		case DOMEvent.XFORMS_REBUILD_CONTROL:
			String localName = node.getLocalName();
			//#if tmp.perf
			System.out.println("Rebuilding control for " + localName);
			long start = System.currentTimeMillis();
			//#endif
			Item newComp = null;
			BoundElement binding = null;
			if (node instanceof BoundElement) {
				binding = (BoundElement) node;
				
				if (localName == "select1") {
					newComp = m_factory.addSelect1(binding, null);
				} else if (localName == "select") {
					newComp = m_factory.addSelect(binding, null);
				} else {
					binding.logError(
							Locale.get("forms.error.wrongElementForRebuild")
							+ ": " + localName);
				}
			} else if (localName == "table") {
				newComp = m_factory.addTable((Element) node, null);
			}
			
			// Replace the component with newComp
			if (newComp != null) {
				if (binding != null) {
					UiAccess.setVisible(newComp, binding
							.getBooleanState(MIPExpr.RELEVANT));
				}
				
				// int index = parent.getPosition(comp);
				// parent.set(index, newComp);
				m_display.callSerially(new ReplaceTask(parent, wrapper != null ? wrapper : comp, newComp));
				node.setUserData(newComp);
	        	if (newComp instanceof WrapperItem) {
	        		((WrapperItem)newComp).getItem().setAttribute(PolishWidgetFactory.ELEMENT_ATTR, node);
	        		((WrapperItem)newComp).getItem().setItemStateListener(this);
	        	} else {
	        		newComp.setAttribute(PolishWidgetFactory.ELEMENT_ATTR, node);
	        		newComp.setItemStateListener(this);
	        	}				
			}
			
			//#if tmp.perf
			long t = System.currentTimeMillis() - start;
			System.out.println("Rebuild took " + t + " ms");
			//#endif
			break;

		}
		//#debug info
		System.out.println("Event processing done.");
		// comp.repaint();
		return;
	}

	private void setValue(Object comp, BoundElement node) {
		//#debug info
		System.out.println("setValue: " + comp + " : " + node);

		String value = ((BoundElement) node).getStringValue();
		//#debug
		System.out.println("New Value: '" + value + "'");
		if (value == null) {
			value = "";
		}
		
		if (comp instanceof TabBar && ((TabBar)comp).getParent() instanceof SelectItem) {
			comp = ((TabBar)comp).getParent(); // Wrapper provides the SelectItem interface
		}

		if (comp instanceof Range) {
			//#debug
			System.out.println("Setting Range");
			DataTypeBase dataType = node.getDataType();
			if (dataType == null) {
				node.logError(Locale.get("forms.error.noDataTypeFound"));
			}
			if (dataType instanceof DataTypeDecimal) {
				double val = ((DataTypeDecimal) dataType).getDoubleValue(node);
				Range range = (Range) comp;
				if (range.getRangeValue() != val) {
					range.setRangeValue(val);
				}
			} else {
				node.logError(Locale.get("forms.error.rangeSupportsOnlyDecimalAndInteger"));
			}
		} else if (comp instanceof DateField) {
			//#debug
			System.out.println("Setting DateField");
			
			DataTypeBase dataType = node.getDataType();
			if (dataType == null) {
				node.logError(Locale.get("forms.error.noDataTypeFound"));
			}
			if (dataType instanceof DataTypeDate) {
				Date oldTime = ((DateField)comp).getDate();
				Date newTime = ((DataTypeDate)dataType).getCalendarValue(node).getTime();
				if (! oldTime.equals(newTime)) {
					((DateField)comp).setDate(newTime);
				}
			} else {
				node.logError(Locale.get("forms.error.dateFieldSupportsOnly"));
			}			
			
        } else if (comp instanceof ChoiceItem) {
			//#debug
			System.out.println("Setting ChoiceItem");
			ChoiceItem ci = (ChoiceItem) comp;
			boolean selected = "true".equals(value);
			if (ci.isSelected != selected) {
				ci.select(selected);
			}
        } else if (comp instanceof ChoiceGroup && node.getLocalName() == "input") {
            //#debug info
            System.out.println("Got checkbox");
            boolean selected = "true".equals(value);
            ChoiceGroup cg = (ChoiceGroup) comp;
            if (cg.isSelected(0) != selected) {
                cg.setSelectedIndex(0, selected);
            }
		} else if (comp instanceof SelectItem) {
			//#debug 
			System.out.println("Setting SelectItem " + comp.hashCode());
			try {
				SelectItem c = (SelectItem) comp;
				String[] values = (String[]) c
						.getAttribute(PolishWidgetFactory.VALUE_ATTR);
				// Zero-length option list
				if (values == null || values.length == 0) {
					if (comp instanceof StringItem) {
						StringItem s = (StringItem) comp;
						String itemVal = s.getText();
						if (itemVal == null) {
							itemVal = "";
						}
						if (!itemVal.equals(value)) {
							//#debug 
							System.out.println("SetText: '" + value + "'");
							s.setText(value);
						}
					}
				} else { // Option list length greater than zero
					final int len = values.length;
					if (node.getLocalName() == "select1") {
						boolean found = false;
						for (int i = 0; i < len; i++) {
							if (value.equals(values[i])) {
								if (c.getSelectedIndex() != i) {
									//#debug 
									System.out.println("Selecting: '"
											+ values[i] + "' (item #" + i + ")");
									c.setSelectedIndex(i, true);
								} else {
									//#debug 
									System.out.println("Already selected: '" + values[i] + "' (item #" + i + ")");
								}
								found = true;
								break;
							}
						}
						if (!found) {
							int selectedIndex = c.getSelectedIndex();
							if (selectedIndex >= 0) {
								boolean[] tmpAllFalse = new boolean[c.size()];
								c.setSelectedFlags(tmpAllFalse);
							}
							if (comp instanceof StringItem) {
								StringItem s = (StringItem) comp;
								String itemVal = s.getText();
								if (itemVal == null) {
									itemVal = "";
								}
								if (!itemVal.equals(value)) {
									//#debug 
									System.out.println("SetText: '" + value
											+ "'");
									s.setText(value);
								}
							}
						} else {
							//#debug 
							System.out.println("Select1 set");
						}
					} else {
						value = " " + value + " ";
						for (int i = 0; i < len; i++) {
							String iValue = " " + values[i] + " ";
							if (value.indexOf(iValue) >= 0) {
								if (!c.isSelected(i)) {
									c.setSelectedIndex(i, true);
								}
							} else {
								if (c.isSelected(i)) {
									c.setSelectedIndex(i, false);
								}
							}
						}
					}
				}
			} catch (Throwable e) {
				//#debug error
				System.out.println(
						Locale.get("forms.error.unableToSetSelectValue")
						+ ": " + e);
			}
		} else if (comp instanceof XF_ChoiceTextField) {
			//#debug
			System.out.println("Setting ChoiceTextField");
			try {
				XF_ChoiceTextField ctf = (XF_ChoiceTextField) comp;
				String[] values = (String[]) ctf
						.getAttribute(PolishWidgetFactory.VALUE_ATTR);
				String[] names = (String[]) ctf
						.getAttribute(PolishWidgetFactory.NAME_ATTR);
				final int len = values.length;
				String curValue = ctf.getText();
				for (int i = 0; i < len; i++) {
					if (value.equals(values[i])) {
						if (!names[i].equals(curValue)) {
							ctf.setText(names[i]);
						}
						break;
					}
				}
			} catch (Throwable e) {
				//#debug error
				System.out.println(
						Locale.get("forms.error.unableToSetSelectValue")
						+ ": " + e);
			}
		} else if (comp instanceof XF_Button || comp instanceof XFormsGroup) {
			// do nothing
			//#debug
			System.out.println("Ignoring trigger or group");
		} else if (comp instanceof StringItem) {
			//#debug
			System.out.println("Setting StringItem");
			
			StringItem l = (StringItem) comp;
			String itemVal;
			if (l instanceof XF_TextField) {
				itemVal = ((XF_TextField) l).getString();
			} else {
				itemVal = l.getText();
			}
			if (itemVal == null) {
				itemVal = "";
			}
			//#debug
			System.out.println("itemVal = " + itemVal + ", val = " + value);
			// First representation equality test which is often sufficient
			if (itemVal.equals(value)) {
				//#debug
				System.out.println("No change, exit");
				return;
			}

			// If that passed, then value equality test
			//#debug
			System.out.println("itemVal:" + itemVal);
			DataTypeBase dt = node.getDataType();
			//#debug
			System.out.println("Got datatype: " + dt);
			if (dt instanceof DataTypeInteger) {
				//#debug
				System.out.println("is integer");
				DataTypeInteger dti = (DataTypeInteger) dt;
				int ival = 0;
				try {
					ival = Integer.parseInt(itemVal);
					//#debug
					System.out.println("ival:" + ival);
				} catch (Exception ignore) {
				}
				if (ival != dti.getIntegerValue(node)) {
					setText(l, value);
				}
			} else if (dt instanceof DataTypeDecimal) {
				//#debug
				System.out.println("is decimal");
				DataTypeDecimal dtd = (DataTypeDecimal) dt;
				double dval = 0.0;
				try {
					dval = Double.parseDouble(itemVal);
					//#debug
					System.out.println("dval:" + dval);
				} catch (Exception ignore) {
				}
				if (dval != dtd.getDoubleValue(node)) {
					setText(l, value);
				}
			} else {
				//#debug
				System.out
						.println("is not integer or decimal, handling as string, setting control to "
								+ value);
				// We already tested for equality
				setText(l, value);
			}
		} else {
			//#debug error
			System.out.println(
					Locale.get("forms.error.unableToSetValueForClass")
					+ ": " + comp.getClass().getName());
		}

	}

	private void setText(StringItem item, String value) {
		if (item instanceof XF_TextField) {
			//#debug
			System.out.println(item.getClass().getName() + ".setString(" + value + ")");
			((XF_TextField) item).setString(value);
		} else {
			//#debug
			System.out.println(item.getClass().getName() + ".setText(" + value + ")");
			item.setText(value);
		}
	}

}
