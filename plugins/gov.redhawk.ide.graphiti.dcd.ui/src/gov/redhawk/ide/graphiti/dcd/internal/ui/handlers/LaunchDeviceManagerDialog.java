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
package gov.redhawk.ide.graphiti.dcd.internal.ui.handlers;

import gov.redhawk.ide.sdr.nodebooter.DebugLevel;
import gov.redhawk.model.sca.provider.ScaItemProviderAdapterFactory;
import gov.redhawk.sca.ScaPlugin;

import java.util.ArrayList;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListDialog;

public class LaunchDeviceManagerDialog extends ListDialog {

	private DebugLevel debugLevel;

	private String arguments;

	private DataBindingContext context = new DataBindingContext();

	public LaunchDeviceManagerDialog(Shell parent) {
		super(parent);

		setTitle("Launch Device Manager");
		setMessage("Select the Domain on which to launch the device manager(s):");

		final ScaItemProviderAdapterFactory factory = new ScaItemProviderAdapterFactory();
		setLabelProvider(new AdapterFactoryLabelProvider(factory));
		setContentProvider(new ArrayContentProvider());

		final ArrayList<Object> input = new ArrayList<Object>();
		input.addAll(ScaPlugin.getDefault().getDomainManagerRegistry(Display.getCurrent()).getDomains());
		final String defaultSelection = "<Default>";
		input.add(defaultSelection);
		setInput(input);
	}

	@Override
	public void setInput(Object input) {
		super.setInput(input);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Label label = new Label(parent, SWT.NULL);
		label.setText("Debug Level: ");
		ComboViewer debugViewer = new ComboViewer(parent, SWT.READ_ONLY | SWT.SINGLE | SWT.DROP_DOWN | SWT.BORDER);
		debugViewer.setContentProvider(new ArrayContentProvider());
		debugViewer.setInput(DebugLevel.values());
		debugViewer.setSelection(new StructuredSelection(DebugLevel.Info));
		context.bindValue(ViewersObservables.observeSingleSelection(debugViewer),
			PojoProperties.value(LaunchDeviceManagerDialog.class, "debugLevel").observe(this));

		label = new Label(parent, SWT.NULL);
		label.setText("Arguments: ");
		Text text = new Text(parent, SWT.BORDER);
		context.bindValue(WidgetProperties.text(SWT.Modify).observe(text), PojoProperties.value(LaunchDeviceManagerDialog.class, "arguments").observe(this));

		super.createButtonsForButtonBar(parent);
	}

	public DebugLevel getDebugLevel() {
		return debugLevel;
	}

	public void setDebugLevel(DebugLevel newLevel) {
		this.debugLevel = newLevel;
	}

	public String getArguments() {
		return arguments;
	}

	public void setArguments(String newArguments) {
		this.arguments = newArguments;
	}
}
