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
package gov.redhawk.ide.snapshot.internal.ui;

import gov.redhawk.bulkio.util.BulkIOType;
import gov.redhawk.ide.snapshot.internal.capture.CorbaDataReceiver;
import gov.redhawk.ide.snapshot.ui.BulkIOSnapshotWizard;
import gov.redhawk.ide.snapshot.ui.SnapshotUI;
import gov.redhawk.ide.snapshot.ui.SnapshotJob;
import gov.redhawk.model.sca.ScaDomainManagerRegistry;
import gov.redhawk.model.sca.ScaUsesPort;
import gov.redhawk.model.sca.provider.ScaItemProviderAdapterFactory;
import gov.redhawk.sca.util.PluginUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;

import CF.ResourceOperations;
import CF.ResourcePackage.StartError;

public class SnapshotHandler extends AbstractHandler {

	public SnapshotHandler() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		ISelection selection = HandlerUtil.getActiveMenuSelection(event);
		if (selection == null) {
			selection = HandlerUtil.getCurrentSelection(event);
		}
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;

			Object obj = ss.getFirstElement();
			ScaUsesPort port = PluginUtil.adapt(ScaUsesPort.class, obj);
			if (port != null) {
				if (port.eContainer() instanceof ResourceOperations) {
					final ResourceOperations lf = (ResourceOperations) port.eContainer();
					if (!lf.started()) {
						MessageDialog dialog = new MessageDialog(HandlerUtil.getActiveShell(event), "Start Resource", null,
							"The ports container is not started.  Would you like to start it now?", MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);
						if (dialog.open() == Window.OK) {
							Job job = new Job("Starting...") {

								@Override
								protected IStatus run(IProgressMonitor monitor) {
									try {
										lf.start();
									} catch (StartError e) {
										return new Status(Status.ERROR, SnapshotUI.PLUGIN_ID, "Failed to start resource", e);
									}
									return Status.OK_STATUS;
								}

							};
							job.schedule();
						}
					}
				}
				BulkIOSnapshotWizard wizard = new BulkIOSnapshotWizard();
				wizard.setPort(port);
				WizardDialog dialog = new WizardDialog(shell, wizard);
				if (dialog.open() == Window.OK) {
					CorbaDataReceiver receiver = wizard.getCorbaReceiver();
					receiver.setPort(port);
					receiver.getDataWriter().getSettings().setType(BulkIOType.getType(port.getRepid()));

					final ScaItemProviderAdapterFactory factory = new ScaItemProviderAdapterFactory();
					final StringBuilder tooltip = new StringBuilder();
					List<String> tmpList = new LinkedList<String>();
					for (EObject eObj = port; !(eObj instanceof ScaDomainManagerRegistry) && eObj != null; eObj = eObj.eContainer()) {
						Adapter adapter = factory.adapt(eObj, IItemLabelProvider.class);
						if (adapter instanceof IItemLabelProvider) {
							IItemLabelProvider lp = (IItemLabelProvider) adapter;
							tmpList.add(0, lp.getText(eObj));
						}
					}
					for (Iterator<String> i = tmpList.iterator(); i.hasNext();) {
						tooltip.append(i.next());
						if (i.hasNext()) {
							tooltip.append(" -> ");
						}
					}
					factory.dispose();

					SnapshotJob job = new SnapshotJob("Snapshot of " + tooltip, receiver);
					job.schedule();
				}
			}
		}
		return null;
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
