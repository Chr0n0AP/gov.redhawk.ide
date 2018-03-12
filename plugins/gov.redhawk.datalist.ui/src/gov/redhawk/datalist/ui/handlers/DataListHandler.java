/**
 * This file is protected by Copyright.
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 *
 * This file is part of REDHAWK IDE.
 *
 * All rights reserved.  This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package gov.redhawk.datalist.ui.handlers;

import gov.redhawk.bulkio.util.BulkIOType;
import gov.redhawk.datalist.ui.DataListPlugin;
import gov.redhawk.datalist.ui.views.DataListView;
import gov.redhawk.model.sca.ScaUsesPort;
import gov.redhawk.sca.util.PluginUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class DataListHandler extends AbstractHandler {

	public DataListHandler() {
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		ISelection selection = HandlerUtil.getActiveMenuSelection(event);
		if (selection == null) {
			selection = HandlerUtil.getCurrentSelection(event);
		}
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			final IStructuredSelection ss = (IStructuredSelection) selection;
			for (final Object obj : ss.toArray()) {
				final ScaUsesPort port = PluginUtil.adapt(ScaUsesPort.class, obj, true);
				if (port != null) {
					openDataList(window, port);
				}
			}
		}
		return null;
	}

	private void openDataList(IWorkbenchWindow window, ScaUsesPort port) {
		try {
			final IViewPart view = window.getActivePage().showView(DataListView.ID, String.valueOf(port.hashCode()), IWorkbenchPage.VIEW_ACTIVATE);
			if (view instanceof DataListView) {
				((DataListView) view).setInput(port);
			}
		} catch (final PartInitException e) {
			e.fillInStackTrace();
			DataListPlugin.getDefault().getLog().log(new Status(Status.WARNING, DataListPlugin.PLUGIN_ID, "Problem initializing part.", e));
		}
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		Object obj = HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_MENU_SELECTION_NAME);
		if (!(obj instanceof IStructuredSelection)) {
			setBaseEnabled(false);
			return;
		}
		IStructuredSelection ss = (IStructuredSelection) obj;

		for (Object element : ss.toArray()) {
			ScaUsesPort port = PluginUtil.adapt(ScaUsesPort.class, element);
			if (port == null || !BulkIOType.isTypeSupported(port.getRepid())) {
				setBaseEnabled(false);
				return;
			}
		}

		setBaseEnabled(true);
	}
}
