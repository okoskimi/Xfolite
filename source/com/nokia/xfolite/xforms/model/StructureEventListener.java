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

package com.nokia.xfolite.xforms.model;

import com.nokia.xfolite.xml.dom.Node;

/*
 * This class will probably be removed, XFormsDocument class can handle this without help from handler.
 */

public interface StructureEventListener {

	/*
	 * Structure change reasons
	 */
	static final int SUBMISSION = 1;
	static final int INSERT = 2;
	static final int DELETE = 3;
	static final int SCRIPT = 4;
	
	/** 
		 * @param target_id might be null, in which case, either there is no event to be dispatched, 
		 *        or the target is e.g. the default instance
		 * @param reevaluateUIBindings if TRUE, all UI bindings have to be evaluated (for instance, after submission has replaced the whole instance)
		 * @param relatedNode for future compatibility
		 */
		void structureChange(int structureChangeReason, boolean reEvaluateUIBindings, String targetId, Node relatedNode, Instance relatedInstance); 
		// XFormsModel::TStructureChangeReason aReason, TBool reevaluateUIBindings, 
		//TString target_id, TNode relatedNode=TNode(), CInstance* relatedInstance=NULL)=0;
	

}
