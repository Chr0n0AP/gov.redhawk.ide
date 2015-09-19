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
package gov.redhawk.ide.graphiti.ui.diagram.wizards;

import gov.redhawk.eclipsecorba.idl.IdlInterfaceDcl;
import gov.redhawk.eclipsecorba.library.ui.IdlFilter;
import gov.redhawk.eclipsecorba.library.ui.IdlInterfaceSelectionDialog;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FindByServiceWizardPage extends WizardPage {

	// inner class model used to store user selections
	public static class Model {

		public static final String ENABLE_SERVICE_NAME = "enableServiceName";
		public static final String SERVICE_NAME = "serviceName";
		public static final String ENABLE_SERVICE_TYPE = "enableServiceType";
		public static final String SERVICE_TYPE = "serviceType";
		public static final String USES_PORT_NAMES = "usesPortNames";
		public static final String PROVIDES_PORT_NAMES = "providesPortNames";

		private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

		private boolean enableServiceName = true;
		private boolean enableServiceType;
		private String serviceName;
		private String serviceType;
		private List<String> usesPortNames = new ArrayList<String>();
		private List<String> providesPortNames = new ArrayList<String>();

		private boolean serviceTypeSupportsPorts = false;

		public Model() {
		}

		public boolean isServiceTypeSupportsPorts() {
			return serviceTypeSupportsPorts;
		}

		public void setServiceTypeSupportsPorts(boolean serviceTypeSupportsPorts) {
			this.serviceTypeSupportsPorts = serviceTypeSupportsPorts;
		}

		public boolean getEnableServiceName() {
			return enableServiceName;
		}

		public boolean getEnableServiceType() {
			return enableServiceType;
		}

		public void setEnableServiceName(boolean enableServiceName) {
			final boolean oldValue = this.enableServiceName;
			this.enableServiceName = enableServiceName;
			this.pcs.firePropertyChange(new PropertyChangeEvent(this, Model.ENABLE_SERVICE_NAME, oldValue, enableServiceName));
		}

		public boolean setEnableServiceType() {
			return enableServiceType;
		}

		public void setEnableServiceType(boolean enableServiceType) {
			final boolean oldValue = this.enableServiceType;
			this.enableServiceType = enableServiceType;
			this.pcs.firePropertyChange(new PropertyChangeEvent(this, Model.ENABLE_SERVICE_TYPE, oldValue, enableServiceType));
		}

		public String getServiceName() {
			return serviceName;
		}

		public void setServiceName(String usesPortName) {
			final String oldValue = this.serviceName;
			this.serviceName = usesPortName;
			this.pcs.firePropertyChange(new PropertyChangeEvent(this, Model.SERVICE_NAME, oldValue, usesPortName));
		}

		public String getServiceType() {
			return serviceType;
		}

		public void setServiceType(String providesPortName) {
			final String oldValue = this.serviceType;
			this.serviceType = providesPortName;
			this.pcs.firePropertyChange(new PropertyChangeEvent(this, Model.SERVICE_TYPE, oldValue, providesPortName));
		}

		public List<String> getUsesPortNames() {
			return usesPortNames;
		}

		public void setUsesPortNames(List<String> usesPortNames) {
			final List<String> oldValue = this.usesPortNames;
			this.usesPortNames = usesPortNames;
			this.pcs.firePropertyChange(new PropertyChangeEvent(this, Model.USES_PORT_NAMES, oldValue, usesPortNames));
		}

		public List<String> getProvidesPortNames() {
			return providesPortNames;
		}

		public void setProvidesPortNames(List<String> providesPortNames) {
			final List<String> oldValue = this.providesPortNames;
			this.providesPortNames = providesPortNames;
			this.pcs.firePropertyChange(new PropertyChangeEvent(this, Model.PROVIDES_PORT_NAMES, oldValue, providesPortNames));
		}

		public void addPropertyChangeListener(final PropertyChangeListener listener) {
			this.pcs.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(final PropertyChangeListener listener) {
			this.pcs.removePropertyChangeListener(listener);
		}

		public boolean isComplete() {
			if (this.enableServiceType && this.serviceType.length() == 0) {
				return false;
			}
			if (this.enableServiceName && this.serviceName.length() == 0) {
				return false;
			}
			return true;
		}
	};

	private static final ImageDescriptor TITLE_IMAGE = null;

	private Model model;
	private DataBindingContext dbc;
	private Button serviceNameBtn, serviceTypeBtn, usesPortAddBtn, usesPortDeleteBtn, providesPortAddBtn, providesPortDeleteBtn;
	private Text serviceNameText, serviceTypeText, usesPortNameText, providesPortNameText;

	public FindByServiceWizardPage() {
		super("findByService", "Find By Service", TITLE_IMAGE);
		this.setDescription("Enter the details of a service you want to make connections to");

		model = new Model();
		dbc = new DataBindingContext();
	}

	@Override
	public void createControl(Composite parent) {
		WizardPageSupport.create(this, dbc);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));

		// service name checkbox
		serviceNameBtn = new Button(composite, SWT.RADIO);
		serviceNameBtn.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		serviceNameBtn.setText("Service Name");
		serviceNameBtn.setSelection(model.getEnableServiceName());
		dbc.bindValue(WidgetProperties.selection().observe(serviceNameBtn), BeanProperties.value(model.getClass(), Model.ENABLE_SERVICE_NAME).observe(model));

		// service name
		final Label serviceNameLabel = new Label(composite, SWT.NONE);
		serviceNameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		serviceNameLabel.setText("Service Name:");

		serviceNameText = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		serviceNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		serviceNameText.setToolTipText("The name of a service in the domain");
		serviceNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dbc.updateModels();
			}
		});
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(serviceNameText), BeanProperties.value(model.getClass(), Model.SERVICE_NAME).observe(model),
			new UpdateValueStrategy().setAfterGetValidator(new IValidator() {
				@Override
				public IStatus validate(Object value) {
					String err = validService("Service Name", (String) value, serviceNameBtn);
					if (err != null) {
						return ValidationStatus.error(err);
					} 
					err = validateAll();
					if (err != null) {
						return ValidationStatus.error(err);
					}
					return ValidationStatus.ok();
				}
			}), null);
		serviceNameBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				serviceNameText.setEnabled(serviceNameBtn.getSelection());
				updateEnablePortsFields();
				dbc.updateModels();
			}
		});

		// service type checkbox
		serviceTypeBtn = new Button(composite, SWT.RADIO);
		serviceTypeBtn.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		serviceTypeBtn.setText("Service Type");
		serviceTypeBtn.setSelection(model.getEnableServiceType());
		dbc.bindValue(WidgetProperties.selection().observe(serviceTypeBtn), BeanProperties.value(model.getClass(), Model.ENABLE_SERVICE_TYPE).observe(model));

		// service type
		final Label serviceTypeLabel = new Label(composite, SWT.NONE);
		serviceTypeLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		serviceTypeLabel.setText("Service Type:");

		Composite serviceTypeComposite = new Composite(composite, SWT.NONE);
		serviceTypeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		serviceTypeComposite.setLayout(new GridLayout(2, false));

		serviceTypeText = new Text(serviceTypeComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER | SWT.READ_ONLY);
		serviceTypeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		serviceTypeText.setToolTipText("The interface (repid) of a service in the domain");
		serviceTypeText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dbc.updateModels();
			}
		});
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(serviceTypeText),
			BeanProperties.value(model.getClass(), Model.SERVICE_TYPE).observe(model),
			new UpdateValueStrategy().setAfterGetValidator(new IValidator() {
				@Override
				public IStatus validate(Object value) {
					String err = validService("Service Type", (String) value, serviceTypeBtn);
					if (err != null) {
						return ValidationStatus.error(err);
					} 
					err = validateAll();
					if (err != null) {
						return ValidationStatus.error(err);
					}
					return ValidationStatus.ok();
				}
			}), null);
		serviceTypeBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				serviceTypeText.setEnabled(serviceTypeBtn.getSelection());
				updateEnablePortsFields();
				dbc.updateModels();
			}
		});
		Button serviceTypeBrowseBtn = new Button(serviceTypeComposite, SWT.BUTTON1);
		serviceTypeBrowseBtn.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		serviceTypeBrowseBtn.setText("Browse");
		serviceTypeBrowseBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!serviceTypeBtn.getSelection()) {
					// Only enable if Service Type radio button is selected
					return;
				}

				IdlInterfaceDcl result = IdlInterfaceSelectionDialog.open(getShell(), IdlFilter.ALL_WITH_MODULE);
				if (result != null) {
					serviceTypeText.setText(result.getRepId());
					// if the interface selected inherits from PortSupplier than allow user to
					// specify port information
					if (extendsPortSupplier(result)) {
						model.setServiceTypeSupportsPorts(true);
						updateEnablePortsFields();
					}
				}
			}
		});

		// disable text boxes when service name/type not enabled
		serviceNameText.setEnabled(model.getEnableServiceName());
		serviceTypeText.setEnabled(model.getEnableServiceType());

		// port group
		final Group portOptions = new Group(composite, SWT.NONE);
		portOptions.setLayout(new GridLayout(2, true));
		portOptions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		portOptions.setText("Port(s) to use for connections");

		// provides port composite
		final Composite providesPortComposite = createPortComposite(portOptions);
		// add provides port name text
		providesPortNameText = addPortNameText(providesPortComposite);
		providesPortNameText.setToolTipText("The specified provides port on the component will be located to make connections");
		// add provides port "Add" button
		providesPortAddBtn = new Button(providesPortComposite, SWT.PUSH);
		providesPortAddBtn.setText("Add Provides Port");
		// add provides port list
		final org.eclipse.swt.widgets.List providesPortList = addPortList(providesPortComposite, Model.PROVIDES_PORT_NAMES);
		// add provides port "Delete" button
		providesPortDeleteBtn = new Button(providesPortComposite, SWT.PUSH);
		providesPortDeleteBtn.setText("Delete");
		if (providesPortList.getItemCount() <= 0) {
			providesPortDeleteBtn.setEnabled(false);
		}
		// add provides port listeners
		providesPortAddBtn.addSelectionListener(getPortAddListener(providesPortList, providesPortNameText, providesPortDeleteBtn));
		providesPortDeleteBtn.addSelectionListener(getPortDeleteListener(providesPortList, providesPortDeleteBtn));

		// uses port composite
		final Composite usesPortComposite = createPortComposite(portOptions);
		// add uses port name text
		usesPortNameText = addPortNameText(usesPortComposite);
		usesPortNameText.setToolTipText("The specified uses port on the component will be located to make connections");
		// add uses port "Add" button
		usesPortAddBtn = new Button(usesPortComposite, SWT.PUSH);
		usesPortAddBtn.setText("Add Uses Port");
		// add uses port list
		final org.eclipse.swt.widgets.List usesPortList = addPortList(usesPortComposite, Model.USES_PORT_NAMES);
		// add uses port "Delete" button
		usesPortDeleteBtn = new Button(usesPortComposite, SWT.PUSH);
		usesPortDeleteBtn.setText("Delete");
		if (usesPortList.getItemCount() <= 0) {
			usesPortDeleteBtn.setEnabled(false);
		}
		// add uses port listeners
		usesPortAddBtn.addSelectionListener(getPortAddListener(usesPortList, usesPortNameText, usesPortDeleteBtn));
		usesPortDeleteBtn.addSelectionListener(getPortDeleteListener(usesPortList, usesPortDeleteBtn));

		setControl(composite);

		dbc.updateModels();
	}

	private Composite createPortComposite(Composite portOptions) {
		final Composite composite = new Composite(portOptions, SWT.None);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return composite;
	}

	private Text addPortNameText(Composite portComposite) {
		final Text portNameText = new Text(portComposite, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.DEFAULT, true, true, 1, 1);
		layoutData.minimumWidth = 200;
		portNameText.setLayoutData(layoutData);

		portNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String err = validText("Port", portNameText);
				if (err != null) {
					setErrorMessage(err);
				} else {
					setErrorMessage(validateAll());
				}
			}
		});
		return portNameText;
	}

	private String validateAll() {
		String err = validService("Service Name", serviceNameText, serviceNameBtn);
		if (err != null) {
			return err;
		}
		err = validService("Service Type", serviceTypeText, serviceTypeBtn);
		if (err != null) {
			return err;
		}
		if (usesPortNameText != null) {
			err = validText("Port", usesPortNameText);
			if (err != null) {
				return err;
			}
		}
		if (providesPortNameText != null) {
			return validText("Port", providesPortNameText);
		}
		return null;
	}

	private static String validService(String valueType, Text valueText, Button btn) {
		if (btn != null && btn.getSelection()) {
			if (valueText == null || valueText.getText().length() < 1) {
				return valueType + " must not be empty";
			}
			return validText(valueType, valueText);
		}
		return null;
	}
	
	private static String validService(String valueType, String value, Button btn) {
		if (btn != null && btn.getSelection()) {
			if (value == null || value.length() < 1) {
				return valueType + " must not be empty";
			}
			return validText(valueType, value);
		}
		return null;
	}

	/**
	 * If returns null, that means the value is valid/has no spaces.
	 * @param valueType
	 * @param value
	 * @return
	 */
	public static String validText(String valueType, Text valueText) {
		if (valueText != null && valueText.getText().contains(" ")) {
			return valueType + " must not include spaces";
		}
		return null;
	}
	
	public static String validText(String valueType, String value) {
		if (value.contains(" ")) {
			return valueType + " must not include spaces";
		}
		return null;
	}

	private org.eclipse.swt.widgets.List addPortList(Composite portComposite, String propertyName) {
		org.eclipse.swt.widgets.List portList = new org.eclipse.swt.widgets.List(portComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		GridData listLayout = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		listLayout.heightHint = 80;
		portList.setLayoutData(listLayout);
		dbc.bindList(WidgetProperties.items().observe(portList), BeanProperties.list(model.getClass(), propertyName).observe(model));
		return portList;
	}

	private SelectionListener getPortAddListener(final org.eclipse.swt.widgets.List portList, final Text portNameText, final Button deleteBtn) {
		SelectionListener listener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String portName = portNameText.getText();
				if (portName.contains(" ")) {
					return;
				}
				if (portName != null && !portName.isEmpty() && !("").equals(portName)) {
					portList.add(portName);
					portNameText.setText("");
					deleteBtn.setEnabled(true);
					dbc.updateModels();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		};
		return listener;
	}

	private SelectionListener getPortDeleteListener(final org.eclipse.swt.widgets.List portList, final Button deleteBtn) {
		SelectionListener listener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] selections = portList.getSelection();
				if (selections != null) {
					for (String selection : selections) {
						portList.remove(selection);
					}
					dbc.updateModels();
				}
				if (portList.getItemCount() <= 0) {
					deleteBtn.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		};
		return listener;
	}

	/**
	 * Return true if interface extends PortSupplier interface
	 * @param idlInterfaceDcl
	 * @return
	 */
	public boolean extendsPortSupplier(IdlInterfaceDcl idlInterfaceDcl) {
		if (idlInterfaceDcl.getInheritedInterfaces() != null) {
			for (IdlInterfaceDcl inheritedInterface : idlInterfaceDcl.getInheritedInterfaces()) {
				if (inheritedInterface.getRepId().startsWith("IDL:CF/PortSupplier") || extendsPortSupplier(inheritedInterface)) {
					return true;
				}
			}
		}
		return false;
	}

	// enable/disable port fields
	public void updateEnablePortsFields() {
		// TODO: We want to limit ports to service types that extend portSupplier
		// TODO: From Devin - What does this do?
		// if(model.getEnableServiceName() || model.getEnableServiceType() && model.isServiceTypeSupportsPorts()){
		if (model.getEnableServiceName() || model.getEnableServiceType()) {
			usesPortNameText.setEnabled(true);
			usesPortAddBtn.setEnabled(true);
			providesPortNameText.setEnabled(true);
			providesPortAddBtn.setEnabled(true);
		} else {
			usesPortNameText.setEnabled(false);
			usesPortAddBtn.setEnabled(false);
			providesPortNameText.setEnabled(false);
			providesPortAddBtn.setEnabled(false);
		}

	}

	public Model getModel() {
		return model;
	}

}
