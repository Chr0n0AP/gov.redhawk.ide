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

package gov.redhawk.ide.spd.internal.ui.editor;

import gov.redhawk.ui.editor.ScaFormPage;
import mil.jpeojtrs.sca.spd.Implementation;
import mil.jpeojtrs.sca.spd.SoftPkg;
import mil.jpeojtrs.sca.spd.util.SpdResourceImpl;

import org.eclipse.emf.common.ui.viewer.IViewerProvider;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.menus.IMenuService;

/**
 * The Class ImplementationPage.
 */
public class ImplementationPage extends ScaFormPage implements IViewerProvider {

	/** The Constant PAGE_ID. */
	public static final String PAGE_ID = "implementations"; //$NON-NLS-1$
	/** The Constant Toolbar ID. */
	public static final String TOOLBAR_ID = "gov.redhawk.ide.spd.internal.ui.editor.implementation.toolbar";
	private final ImplementationsBlock fBlock;

	/**
	 * Instantiates a new properties form page.
	 * 
	 * @param editor the editor
	 * @param spdResource the spd resource
	 * @param waveDevResource the wave dev resource
	 */
	public ImplementationPage(final ComponentEditor editor) {
		super(editor, ImplementationPage.PAGE_ID, "Implementations");
		this.fBlock = new ImplementationsBlock(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ComponentEditor getEditor() {
		return (ComponentEditor) super.getEditor();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createFormContent(final IManagedForm managedForm) {

		final ScrolledForm form = managedForm.getForm();
		form.setText("Implementations");

		// TODO
		// form.setImage(PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_EXTENSIONS_OBJ));
		this.fBlock.createContent(managedForm);

		final ToolBarManager manager = (ToolBarManager) form.getToolBarManager();
		super.createFormContent(managedForm);
		final IMenuService service = (IMenuService) getSite().getService(IMenuService.class);
		service.populateContributionManager(manager, "toolbar:" + ImplementationPage.TOOLBAR_ID);
		manager.update(true);
	}

	/**
	 * Checks to see if the resource is a Softpackage Library
	 * Important because a number of entry fields are omitted in the case of libraries
	 */
	public boolean isSoftpackageLibrary() {
		if (getInput() instanceof SpdResourceImpl) {
			SpdResourceImpl spdResource = (SpdResourceImpl) getInput();

		for (EObject i : spdResource.getContents()) {
			if (i instanceof SoftPkg) {
				for (Implementation impl : ((SoftPkg) i).getImplementation()) {
					if (impl.isSharedLibrary()) {
						return true;
					}
				}
			}
		}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Viewer getViewer() {
		return this.fBlock.getSection().getStructuredViewerPart().getViewer();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void refresh(final Resource resource) {
		this.fBlock.refresh(resource);
	}
}
