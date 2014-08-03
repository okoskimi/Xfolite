package com.nokia.xfolite.client;

import javax.microedition.lcdui.Displayable;

import com.nokia.xfolite.xforms.dom.XFormsDocument;
import com.nokia.xfolite.xforms.dom.XFormsElement;

import de.enough.polish.ui.Item;

public class SubmitCommand extends DomEventCommand {

	public SubmitCommand(String label, int commandType, int priority,
			XFormsElement el, int event) {
		super(label, commandType, priority, el, event);
		// TODO Auto-generated constructor stub
	}

	public SubmitCommand(String label, int commandType, int priority,
			XFormsElement el, int event, Item item) {
		super(label, commandType, priority, el, event, item);
		// TODO Auto-generated constructor stub
	}
	
    public void execute(Item item, Displayable disp) {
    	XFormsDocument doc = (XFormsDocument) m_element.getOwnerDocument();
    	if (doc.requiredOK()) {
    		super.execute(item, disp);
    	} else {
    		doc.getUserInterface().showMessage(
    				"Unable to submit yet.\nPlease fill in all required fields.");
    	}
    }
}
