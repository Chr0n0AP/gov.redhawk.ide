/*******************************************************************************
 * This file is protected by Copyright. 
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 *
 * This file is part of REDHAWK IDE.
 *
 * All rights reserved.  This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package gov.redhawk.ide.sad.graphiti.internal.ui.editor;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.graphiti.ui.internal.editor.GFWorkspaceCommandStackImpl;

import gov.redhawk.ide.sad.internal.ui.editor.SadOverviewPage;
import gov.redhawk.ui.editor.SCAFormEditor;

public class GraphitiSadOverviewPage extends SadOverviewPage {

	public GraphitiSadOverviewPage(SCAFormEditor editor) {
		super(editor);
	}

	/**
	 * @return the common command stack provided by the parent editor
	 */
	protected BasicCommandStack getCommandStack() {
		return ((GFWorkspaceCommandStackImpl) getEditingDomain().getCommandStack());
	}
}
