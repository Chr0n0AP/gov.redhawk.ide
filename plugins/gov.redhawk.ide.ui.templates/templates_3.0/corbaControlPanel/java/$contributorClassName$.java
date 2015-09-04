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
package $packageName$;

import gov.redhawk.sca.ui.editors.AbstractScaEditor;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorActionBarContributor;

/**
 * Manages the installation/deinstallation of global actions for multi-page editors.
 * Responsible for the redirection of global actions to the active editor.
 * Multi-page contributor replaces the contributors for the individual editors in the multi-page editor.
 */
public class $contributorClassName$ extends EditorActionBarContributor {
	private IEditorPart activeEditorPart;
	private Action sampleAction;
	private Object activeEditor;
	
	/**
	 * Creates a multi-page contributor.
	 */
	public $contributorClassName$() {
		super();
		createActions();
	}
	
	/**
	 * Returns the action registered with the given REDHAWK editor.
	 * @return IAction or null if editor is null.
	 */
	protected IAction getAction(AbstractScaEditor editor, String actionID) {
		return (editor == null ? null : editor.getAction(actionID));
	}
	
	/* (non-JavaDoc)
	 * Method declared in AbstractMultiPageEditorActionBarContributor.
	 */
	@Override
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
		if (activeEditorPart == part)
			return;

		activeEditorPart = part;

		IActionBars actionBars = getActionBars();
		if (actionBars != null) {
			
			EObject obj = this.getInput();
			
			// TODO Setup Action Bars
			/** Example:
			actionBars.setGlobalActionHandler(
					ActionFactory.DELETE.getId(),
					getAction(editor, ITextEditorActionConstants.DELETE));
			  */
			
			actionBars.updateActionBars();
		}
	}
	
	/**
	 * Get the EObject Input of the Editor Part
	 * @return EObject
	 */
	public EObject getInput() {
		EObject retVal = null;
		if (this.activeEditor != null) {
			retVal = (EObject) this.activeEditorPart.getAdapter(EObject.class);
		}
		return retVal;
	}
	
	private void createActions() {
		// TODO Create any actions
		sampleAction = new Action() {
			public void run() {
				MessageDialog.openInformation(null, "TestWaveform", "Sample Action Executed");
			}
		};
		sampleAction.setText("Sample Action");
		sampleAction.setToolTipText("Sample Action tool tip");
		sampleAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));
	}
	
	public void contributeToMenu(IMenuManager manager) {
		// TODO Contribute Actions to the Workbench Menu
		IMenuManager menu = new MenuManager("Editor &Menu");
		manager.prependToGroup(IWorkbenchActionConstants.MB_ADDITIONS, menu);
		menu.add(sampleAction);
	}
	
	public void contributeToToolBar(IToolBarManager manager) {
		// TODO Contribute Actions to the Tool Bar
		manager.add(new Separator());
		manager.add(sampleAction);
	}
}
