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
package gov.redhawk.ide.spd.ui.wizard;

import gov.redhawk.codegen.validation.ImplementationIdValidator;
import gov.redhawk.ide.codegen.CodegenFactory;
import gov.redhawk.ide.codegen.CodegenUtil;
import gov.redhawk.ide.codegen.ICodeGeneratorDescriptor;
import gov.redhawk.ide.codegen.IPropertyDescriptor;
import gov.redhawk.ide.codegen.ITemplateDesc;
import gov.redhawk.ide.codegen.ImplementationSettings;
import gov.redhawk.ide.codegen.Property;
import gov.redhawk.ide.codegen.RedhawkCodegenActivator;
import gov.redhawk.ide.codegen.WaveDevSettings;
import gov.redhawk.sca.util.StringUtil;
import gov.redhawk.ui.util.EMFEmptyStringToNullUpdateValueStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mil.jpeojtrs.sca.spd.Compiler;
import mil.jpeojtrs.sca.spd.Implementation;
import mil.jpeojtrs.sca.spd.ProgrammingLanguage;
import mil.jpeojtrs.sca.spd.Runtime;
import mil.jpeojtrs.sca.spd.SoftPkg;
import mil.jpeojtrs.sca.spd.SpdFactory;
import mil.jpeojtrs.sca.spd.SpdPackage;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * The Class ImplementationWizardPage.
 * @since 8.1
 */
public class ImplementationWizardPage extends WizardPage {
	private static final int NUM_COLUMNS = 2;

	/** The Constant TITLE_IMAGE. */
	private static final ImageDescriptor TITLE_IMAGE = null;

	private Implementation impl = null;
	private final Compiler compiler = SpdFactory.eINSTANCE.createCompiler();
	private final Runtime runtime = SpdFactory.eINSTANCE.createRuntime();
	private ImplementationSettings implSettings = CodegenFactory.eINSTANCE.createImplementationSettings();

	private final EMFDataBindingContext context = new EMFDataBindingContext();

	private WizardPageSupport pageSupport;

	private Text idText = null;
	private Text descriptionText = null;
	private Combo progLangEntryViewer = null;
	private ComboViewer codeGeneratorEntryViewer = null;
	private Button importSourceCode = null;
	private Label importLabel = null;

	private SoftPkg softPkg;

	private boolean manualId = false;

	private boolean created;

	private boolean shouldImport = false;

	private boolean enableImportCode = false;

	private ICodeGeneratorDescriptor codeGenerator;

	private String projectName = "";

	private final String componenttype;

	private boolean importing = false;

	/**
	 * The Constructor.
	 */
	public ImplementationWizardPage(final String name, final String componenttype) {
		super(name, "New Implementation", ImplementationWizardPage.TITLE_IMAGE);
		this.setPageComplete(false);
		this.componenttype = componenttype;
	}

	/**
	 * The Constructor.
	 */
	public ImplementationWizardPage(final String name, final SoftPkg softPkg) {
		super(name, "New Implementation", ImplementationWizardPage.TITLE_IMAGE);
		String tmpComponentType = "";
		
		this.setPageComplete(false);
		this.softPkg = softPkg;
		if (this.softPkg != null) {
			this.projectName = this.softPkg.getName();
			
			// If this is a soft package there is not a descriptor or component type.
			if (softPkg.getDescriptor() != null) {
				tmpComponentType = softPkg.getDescriptor().getComponent().getComponentType();
			}
		}
		
		this.componenttype = tmpComponentType;
		this.setDescription("Choose the initial settings for the new implementation.");

	}

	/**
	 * @since 8.1
	 */
	public void setImpl(Implementation impl) {
		this.impl = impl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		if (this.context != null) {
			this.context.dispose();
		}
		if (this.pageSupport != null) {
			this.pageSupport.dispose();
		}
		super.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(final Composite parent) {
		if (getWizard() instanceof NewScaResourceWizard) {
			this.importing = !(((NewScaResourceWizard) getWizard()).getImportedSettingsMap().isEmpty());
		}

		final Composite client = new Composite(parent, SWT.NULL);
		client.setLayout(new GridLayout(ImplementationWizardPage.NUM_COLUMNS, false));

		Label label;
		GridData data;

		final GridDataFactory labelFactory = GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.FILL);
		final GridDataFactory textFactory = GridDataFactory.fillDefaults().grab(true, false);

		label = new Label(client, SWT.NULL);
		label.setText("Prog. Lang:");
		label.setLayoutData(labelFactory.create());
		this.progLangEntryViewer = new Combo(client, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);
		this.getProgLangEntryViewer().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		this.getProgLangEntryViewer().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				handleProgLangSelection();
			}
		});
		this.getProgLangEntryViewer().setItems(RedhawkCodegenActivator.getCodeGeneratorsRegistry().getLanguages());

		label = new Label(client, SWT.NULL);
		label.setText("Code Generator:");
		label.setLayoutData(labelFactory.create());
		this.codeGeneratorEntryViewer = new ComboViewer(client, SWT.READ_ONLY | SWT.SINGLE | SWT.DROP_DOWN | SWT.BORDER);
		this.getCodeGeneratorEntryViewer().setContentProvider(new ArrayContentProvider());
		this.getCodeGeneratorEntryViewer().setLabelProvider(new LabelProvider() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public String getText(final Object element) {
				if (element instanceof ICodeGeneratorDescriptor) {
					return ((ICodeGeneratorDescriptor) element).getName();
				}
				return super.getText(element);
			}
		});
		this.getCodeGeneratorEntryViewer().getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		this.getCodeGeneratorEntryViewer().addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				final IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				final String previousCodeGenId = ImplementationWizardPage.this.implSettings.getGeneratorId();
				handleCodeGenerationSelection(sel);
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (getWizard() instanceof ScaImplementationWizard2) {
							((ScaImplementationWizard2) getWizard()).generatorChanged(ImplementationWizardPage.this.impl,
								ImplementationWizardPage.this.codeGenerator, previousCodeGenId);
						} else if (getWizard() instanceof ScaImplementationWizard) {
							((ScaImplementationWizard) getWizard()).generatorChanged(ImplementationWizardPage.this.impl,
								ImplementationWizardPage.this.codeGenerator);
						}
					}
				});
			}

		});
		if ((this.codeGenerator != null) && (this.codeGenerator.getDescription() != null)) {
			this.getCodeGeneratorEntryViewer().getCombo().setToolTipText(this.codeGenerator.getDescription());
		}

		label = new Label(client, SWT.NULL);
		label.setLayoutData(labelFactory.create());
		this.idText = new Text(client, SWT.BORDER);
		data = textFactory.create();
		data.horizontalSpan = 1;
		this.idText.setLayoutData(data);
		label.setText("ID:");
		this.idText.setText("");

		label = new Label(client, SWT.NULL);
		label.setLayoutData(labelFactory.create());
		this.descriptionText = new Text(client, SWT.BORDER | SWT.MULTI);
		data = textFactory.create();
		data.heightHint = 125; // SUPPRESS CHECKSTYLE MagicNumber
		this.descriptionText.setLayoutData(data);
		this.descriptionText.setText("Sample description");
		label.setText("Description:");

		this.importLabel = new Label(client, SWT.NULL);
		this.importLabel.setLayoutData(labelFactory.create());
		this.importSourceCode = new Button(client, SWT.CHECK | SWT.BORDER);
		this.importSourceCode.setText("Import Source Code");

		this.importSourceCode.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// PASS
			}

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Button button = (Button) e.widget;
				ImplementationWizardPage.this.shouldImport = button.getSelection();
			}

		});

		this.importSourceCode.setEnabled(this.enableImportCode);
		this.importSourceCode.setVisible(this.enableImportCode);
		this.importLabel.setVisible(this.enableImportCode);

		this.bind(this.importing);

		int index = 0;
		for (final String tempProgLang : this.getProgLangEntryViewer().getItems()) {
			if (tempProgLang.equalsIgnoreCase(this.impl.getProgrammingLanguage().getName())) {
				this.getProgLangEntryViewer().select(index);
				this.handleProgLangSelection();
				break;
			}
			index++;
		}

		this.setControl(client);

		this.created = true;
	}

	/**
	 * This method updates the current Implementation with the compiler and
	 * runtime settings from the selected Code Generator.
	 * 
	 * @param selection the Selection containing the new Code Generator
	 */
	protected void handleCodeGenerationSelection(final IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return;
		}

		final ICodeGeneratorDescriptor tempCodegen = (ICodeGeneratorDescriptor) selection.getFirstElement();
		this.codeGenerator = tempCodegen;
		if (!tempCodegen.getId().equals(this.implSettings.getGeneratorId())) {
			this.implSettings.getProperties().clear();
			this.implSettings.setOutputDir(null);
			this.implSettings.setTemplate(null);
		}
		this.implSettings.setGeneratorId(tempCodegen.getId());
		if (tempCodegen.getCompiler() != null) {
			this.compiler.setName(tempCodegen.getCompiler());
			this.compiler.setVersion(tempCodegen.getCompilerVersion());
			this.impl.setCompiler(this.compiler);
		} else {
			this.impl.setCompiler(null);
		}
		if (tempCodegen.getRuntime() != null) {
			this.runtime.setName(tempCodegen.getRuntime());
			this.runtime.setVersion(tempCodegen.getRuntimeVersion());
			this.impl.setRuntime(this.runtime);
		} else {
			this.impl.setRuntime(null);
		}

		if ((this.codeGenerator.getDescription() != null) && (ImplementationWizardPage.this.getCodeGeneratorEntryViewer() != null)) {
			ImplementationWizardPage.this.getCodeGeneratorEntryViewer().getCombo().setToolTipText(this.codeGenerator.getDescription());
		}
	}

	protected void handleProgLangSelection() {
		final String temp = ImplementationWizardPage.this.getProgLangEntryViewer().getText();
		final ICodeGeneratorDescriptor[] availableCodegens = RedhawkCodegenActivator.getCodeGeneratorsRegistry().findCodegenByLanguage(temp,
			this.getComponenttype());

		// Filter out JET generators
		List<ICodeGeneratorDescriptor> tempCodegens = new ArrayList<ICodeGeneratorDescriptor>();
		for (int i = 0; i < availableCodegens.length; i++) {
			ICodeGeneratorDescriptor codegen = availableCodegens[i];
			if (codegen.isDeprecated()) {
				continue;
			} else {
				tempCodegens.add(availableCodegens[i]);
			}
		}

		if (!this.manualId || "".equals(this.idText.getText().trim())) {
			this.idText.setText(this.createUniqueId(temp, this.projectName));
			this.manualId = false;
		}
		ImplementationWizardPage.this.getCodeGeneratorEntryViewer().setInput(tempCodegens);
		if (tempCodegens.size() > 0) {
			// First try and go through and set it to a default generator that has not been filtered.
			for (final ICodeGeneratorDescriptor desc : tempCodegens) {
				if (!desc.notDefaultableGenerator()) {
					if (!isFiltered(ImplementationWizardPage.this.getCodeGeneratorEntryViewer(), desc)) {
						ImplementationWizardPage.this.getCodeGeneratorEntryViewer().setSelection(new StructuredSelection(desc));
						break;
					}
				}
			}
			// If that does not yield a selection then try and select one that may not be default but is available and
			// not filtered.
			if (ImplementationWizardPage.this.getCodeGeneratorEntryViewer().getSelection().isEmpty()) {
				for (final ICodeGeneratorDescriptor desc : tempCodegens) {
					if (!isFiltered(ImplementationWizardPage.this.getCodeGeneratorEntryViewer(), desc)) {
						ImplementationWizardPage.this.getCodeGeneratorEntryViewer().setSelection(new StructuredSelection(desc));
						break;
					}
				}
			}
		}
	}

	private boolean isFiltered(ComboViewer viewer, ICodeGeneratorDescriptor desc) {
		// It's easier to determine if the object is not filtered. So we determine if it is notFiltered
		// and then invert that.

		boolean notFiltered = true;

		for (ViewerFilter filter : viewer.getFilters()) {
			notFiltered = notFiltered & filter.select(viewer, null, desc);
		}

		return !notFiltered;
	}

	public ImplementationSettings getImplSettings() {
		return this.implSettings;
	}

	/**
	 * Gets the implementation.
	 * 
	 * @return the implementation
	 */
	public Implementation getImplementation() {
		return this.impl;
	}

	public ICodeGeneratorDescriptor getCodeGenerator() {
		return this.codeGenerator;
	}

	public String getLanguage() {
		return this.getProgLangEntryViewer().getText();
	}

	@Override
	public boolean canFlipToNextPage() {
		// Can flip to the next page if
		// - You selected a generator AND
		// --- The generator has settings OR
		// --- The generator doesn't have settings AND there are more
		// implementations to display
		final ICodeGeneratorDescriptor codeGen = this.getCodeGenerator();
		if (codeGen == null) {
			return false;
		} else if (super.canFlipToNextPage()) {
			final ITemplateDesc[] templates = RedhawkCodegenActivator.getCodeGeneratorTemplatesRegistry().findTemplatesByCodegen(codeGen.getId());
			if (templates.length > 0) {
				return true;
			} else {
				return ((ScaImplementationWizard) this.getWizard()).hasMoreImplementations(this.impl);
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean isPageComplete() {
		boolean retval1 = super.isPageComplete();
		boolean retval2 = this.getCodeGenerator() != null;
		return retval1 && retval2;
	}

	public void setName(final String name) {
		if (idText == null) {
			return;
		}
		if (!this.manualId && ((this.projectName == null) || !this.projectName.equals(name))) {
			this.projectName = name;
			if (this.getProgLangEntryViewer() != null && this.getProgLangEntryViewer().getText().trim().length() > 0) {
				this.idText.setText(this.createUniqueId(this.getProgLangEntryViewer().getText(), name));
			} else {
				this.idText.setText("");
			}
		}
	}

	@SuppressWarnings("deprecation")
	private String createUniqueId(final String language, final String projName) {
		// Make up a unique new name here.
		final StringBuilder implName = new StringBuilder();

		final List<String> languages = Arrays.asList(RedhawkCodegenActivator.getCodeGeneratorsRegistry().getLanguages());
		if (languages.contains(language)) {
			implName.append(language.toLowerCase().replaceAll("\\+", "p"));
		}

		final List<String> names = new ArrayList<String>();
		if (this.softPkg != null) {
			final WaveDevSettings waveDevSettings = CodegenUtil.loadWaveDevSettings(this.softPkg);
			if (waveDevSettings != null) {
				for (final Implementation anImpl : this.softPkg.getImplementation()) {
					final ImplementationSettings settings = waveDevSettings.getImplSettings().get(anImpl.getId());
					if (settings != null) {
						final String theName = settings.getName();
						if (theName != null) {
							names.add(theName);
						} else {
							names.add(anImpl.getId());
						}
					}
				}
			}
		}
		return StringUtil.defaultCreateUniqueString(implName.toString(), names, StringUtil.getDefaultUpdateStrategy("_impl"));
	}

	/**
	 * @since 6.0
	 */
	@SuppressWarnings("deprecation")
	public void importImplementation(final Implementation impl, final ImplementationSettings oldImplSettings) {
		if (this.context != null) {
			this.context.dispose();
		}

		this.implSettings = CodegenFactory.eINSTANCE.createImplementationSettings();
		this.impl = impl;

		this.setPageComplete(false);
		this.manualId = false;
		this.setTitle("Importing " + impl.getId());
		this.setDescription("View and / or modify the settings for the imported implementation.");

		String tempName = "Imported";
		if (impl.getCode() != null) {
			final String localFile = impl.getCode().getLocalFile().getName();

			if ((localFile != null) && (localFile.length() > 0)) {
				final int idx = localFile.lastIndexOf('/');

				// Strip off the preceding /'s if any
				tempName = localFile.substring(0, (idx != -1) ? idx : localFile.length()); // SUPPRESS CHECKSTYLE AvoidInline
				this.manualId = true;
			}
		}
		this.implSettings.setName(tempName);

		if ((this.impl.getDescription() == null) || (this.impl.getDescription().trim().length() == 0)) {
			this.impl.setDescription("Sample description");
		}

		final String tempLang = this.impl.getProgrammingLanguage().getName();
		final ICodeGeneratorDescriptor[] tempCodegens = RedhawkCodegenActivator.getCodeGeneratorsRegistry().findCodegenByLanguage(tempLang);
		String templateName = null;

		// Set the default generator, select the old one if it's available,
		// otherwise select the first defaultable generator for the selected
		// programming language
		if (tempCodegens.length > 0) {
			ICodeGeneratorDescriptor defaultGen = null;
			final String genId = (oldImplSettings != null) ? oldImplSettings.getGeneratorId() : null; // SUPPRESS CHECKSTYLE AvoidInline
			// Loop through all the code generators
			for (final ICodeGeneratorDescriptor desc : tempCodegens) {
				// If we haven't set a default generator yet, check this one
				if ((defaultGen == null) && !desc.notDefaultableGenerator()) {
					// Since the generator may not be found, we need a default generator, this
					// is going to be the first one that's allowed to be defaulted in the generator list
					defaultGen = desc;

					final ITemplateDesc[] templates = RedhawkCodegenActivator.getCodeGeneratorTemplatesRegistry().findTemplatesByCodegen(desc.getId());
					// Same deal with the template, there should always be at least one defaultable one
					for (final ITemplateDesc temp : templates) {
						if (!temp.notDefaultableGenerator()) {
							templateName = temp.getId();
							break;
						}
					}
				}

				// Override the default generator and template if we found the
				// one that is set in the previous settings
				if (genId != null && desc.getId().equals(genId)) {
					defaultGen = desc;
					templateName = (oldImplSettings == null) ? null : oldImplSettings.getTemplate();
					break;
				}
			}

			if (defaultGen != null) {
				this.handleCodeGenerationSelection(new StructuredSelection(defaultGen));
				final ITemplateDesc temp = CodegenUtil.getTemplate(templateName, defaultGen.getId());
				if (temp != null) {
					this.implSettings.setTemplate(temp.getId());

					// Set the properties to the values specified by the old settings
					// this allows for removing old properties
					if (oldImplSettings != null) {
						final EList<Property> props = this.implSettings.getProperties();
						for (final IPropertyDescriptor desc : temp.getPropertyDescriptors()) {
							boolean added = false;

							for (final Property prop : oldImplSettings.getProperties()) {
								if (desc.getKey().equals(prop.getId())) {
									final Property p = EcoreUtil.copy(prop);
									props.add(p);
									added = true;
									break;
								}
							}

							// Make sure we added the property. If not, set it to default
							if (!added) {
								final Property p = CodegenFactory.eINSTANCE.createProperty();
								p.setId(desc.getKey());
								p.setValue(desc.getDefaultValue());
								props.add(p);
							}
						}
					}
				}
			}
		}

		// Preserve the output directory
		if (oldImplSettings != null) {
			final String oldName = oldImplSettings.getName();
			if (oldName != null && !"".equals(oldName.trim())) {
				this.implSettings.setName(oldName);
			}
			this.implSettings.setPrimary(oldImplSettings.isPrimary());
			this.implSettings.setOutputDir(oldImplSettings.getOutputDir());
		}

		if (this.created) {
			this.bind(this.importing);
		}
	}

	private void bind(boolean importingCode) {
		this.context.bindValue(WidgetProperties.text(SWT.Modify).observe(this.idText),
			EMFObservables.observeValue(this.impl, SpdPackage.Literals.IMPLEMENTATION__ID),
			new EMFEmptyStringToNullUpdateValueStrategy().setAfterConvertValidator(new ImplementationIdValidator(this.softPkg, importingCode)), null);

		this.context.bindValue(WidgetProperties.text().observe(this.getProgLangEntryViewer()),
			EMFObservables.observeValue(this.getProgLang(), SpdPackage.Literals.PROGRAMMING_LANGUAGE__NAME), new EMFEmptyStringToNullUpdateValueStrategy(),
			null);

		this.context.bindValue(WidgetProperties.text(SWT.Modify).observe(this.descriptionText),
			EMFObservables.observeValue(this.impl, SpdPackage.Literals.IMPLEMENTATION__DESCRIPTION), new EMFEmptyStringToNullUpdateValueStrategy(), null);

		this.pageSupport = WizardPageSupport.create(this, this.context);
	}

	/**
	 * @since 5.0
	 */
	public void enableImportCode(final boolean enableImportCode) {
		this.enableImportCode = enableImportCode;
	}

	/**
	 * @since 5.0
	 */
	public boolean shouldImportCode() {
		return this.shouldImport;
	}

	/**
	 * @since 8.1
	 */
	protected ProgrammingLanguage getProgLang() {
		return this.impl.getProgrammingLanguage();
	}

	/**
	 * @since 8.1
	 */
	protected Combo getProgLangEntryViewer() {
		return progLangEntryViewer;
	}

	/**
	 * @since 8.1
	 */
	protected ComboViewer getCodeGeneratorEntryViewer() {
		return codeGeneratorEntryViewer;
	}

	/**
	 * @since 8.1
	 */
	public String getComponenttype() {
		return componenttype;
	}

}
