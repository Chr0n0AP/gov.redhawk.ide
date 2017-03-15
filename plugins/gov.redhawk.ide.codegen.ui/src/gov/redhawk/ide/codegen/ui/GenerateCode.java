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
package gov.redhawk.ide.codegen.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.progress.WorkbenchJob;
import org.osgi.framework.Version;

import gov.redhawk.ide.codegen.CodegenPackage;
import gov.redhawk.ide.codegen.CodegenUtil;
import gov.redhawk.ide.codegen.FileStatus;
import gov.redhawk.ide.codegen.FileToCRCMap;
import gov.redhawk.ide.codegen.ICodeGeneratorDescriptor;
import gov.redhawk.ide.codegen.IScaComponentCodegen;
import gov.redhawk.ide.codegen.ITemplateDesc;
import gov.redhawk.ide.codegen.ImplementationSettings;
import gov.redhawk.ide.codegen.RedhawkCodegenActivator;
import gov.redhawk.ide.codegen.WaveDevSettings;
import gov.redhawk.ide.codegen.ui.internal.GenerateFilesDialog;
import gov.redhawk.ide.codegen.ui.internal.GeneratorConsole;
import gov.redhawk.ide.codegen.ui.internal.GeneratorUtil;
import gov.redhawk.ide.codegen.ui.internal.WaveDevUtil;
import gov.redhawk.ide.codegen.ui.preferences.CodegenPreferenceConstants;
import gov.redhawk.ide.codegen.util.PropertyUtil;
import gov.redhawk.model.sca.commands.ScaModelCommand;
import gov.redhawk.model.sca.util.ModelUtil;
import gov.redhawk.sca.util.SubMonitor;
import mil.jpeojtrs.sca.spd.Implementation;
import mil.jpeojtrs.sca.spd.SoftPkg;
import mil.jpeojtrs.sca.util.NamedThreadFactory;

/**
 * This class is the primary entry point to code generation.
 * @since 7.0
 */
public final class GenerateCode {

	private GenerateCode() {
	}

	private static final ExecutorService EXECUTOR_POOL = Executors.newSingleThreadExecutor(new NamedThreadFactory(GenerateCode.class.getName()));

	/**
	 * Performs the code generation process for the specified implementation(s). The process may prompt the user for
	 * input. The process occurs in a job and is thus asynchronous.
	 * <p/>
	 * This entry point does not perform any deprecation checks, upgrades, etc. For that, see
	 * {@link gov.redhawk.ide.codegen.ui.internal.command.GenerateComponentHandler}.
	 * @since 8.0
	 */
	public static void generate(final Shell shell, final List<Implementation> impls) {
		if (impls.isEmpty()) {
			return;
		}
		Job getFilesJob = new Job("Calculating files to generate...") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// Map of Implementation -> ( map of FileName relative to output location -> true will regenerate, 
				// false wants to regenerate but contents different
				SubMonitor progress = SubMonitor.convert(monitor, "Calculating files to generate", impls.size());
				final Map<Implementation, Set<FileStatus>> implMap = new HashMap<Implementation, Set<FileStatus>>();
				for (Implementation impl : impls) {
					try {
						Set<FileStatus> resultSet = getFilesToGenerate(progress.newChild(1, SubMonitor.SUPPRESS_NONE), impl);
						implMap.put(impl, resultSet);
					} catch (CoreException e) {
						return new Status(e.getStatus().getSeverity(), RedhawkCodegenUiActivator.PLUGIN_ID, "Failed to calculate files to generate", e);
					}
				}
				progress.done();
				WorkbenchJob checkFilesJob = new WorkbenchJob("Check files") {

					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						Set<FileStatus> aggregate = new HashSet<FileStatus>();
						for (Set<FileStatus> v : implMap.values()) {
							aggregate.addAll(v);
						}
						final IPreferenceStore store = RedhawkCodegenUiActivator.getDefault().getPreferenceStore();
						List<String> filesToGenerate = new ArrayList<String>();
						boolean showDialog = false;
						boolean generateDefault = store.getBoolean(CodegenPreferenceConstants.P_ALWAYS_GENERATE_DEFAULTS);
						if (generateDefault) {
							for (FileStatus s : aggregate) {
								if (!s.isDoIt() && s.getType() != FileStatus.Type.USER) {
									showDialog = true;
									break;
								}
							}
						} else {
							showDialog = true;
						}

						if (showDialog) {
							GenerateFilesDialog dialog = new GenerateFilesDialog(shell, aggregate);
							dialog.setBlockOnOpen(true);
							if (dialog.open() == Window.OK) {
								String[] result = dialog.getFilesToGenerate();
								if (result != null) {
									filesToGenerate.addAll(Arrays.asList(result));
								}
							} else {
								return Status.CANCEL_STATUS;
							}
						} else {
							for (FileStatus s : aggregate) {
								if (s.isDoIt()) {
									filesToGenerate.add(s.getFilename());
								}
							}
						}

						final Map<Implementation, String[]> implFileMap = new HashMap<Implementation, String[]>();
						for (Map.Entry<Implementation, Set<FileStatus>> entry : implMap.entrySet()) {
							Set<String> subsetFilesToGenerate = new HashSet<String>();
							for (FileStatus s : entry.getValue()) {
								subsetFilesToGenerate.add(s.getFilename());
							}
							Set<String> filesToRemove = new HashSet<String>(subsetFilesToGenerate);
							filesToRemove.removeAll(filesToGenerate);
							subsetFilesToGenerate.removeAll(filesToRemove);

							implFileMap.put(entry.getKey(), subsetFilesToGenerate.toArray(new String[subsetFilesToGenerate.size()]));
						}

						WorkspaceJob processJob = new WorkspaceJob("Generating...") {

							@Override
							public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
								return processImpls(implFileMap, monitor);
							}
						};
						processJob.setUser(true);
						processJob.schedule();

						return Status.OK_STATUS;
					}
				};
				checkFilesJob.setUser(false);
				checkFilesJob.setSystem(true);
				checkFilesJob.schedule();

				return Status.OK_STATUS;
			}
		};
		getFilesJob.setUser(true);
		getFilesJob.schedule();
	}

	private static IStatus processImpls(Map<Implementation, String[]> implMap, IProgressMonitor monitor) throws CoreException {
		try {
			SubMonitor progress = SubMonitor.convert(monitor, "Generating...", implMap.size() + 2);
			final SoftPkg softPkg = (SoftPkg) implMap.entrySet().iterator().next().getKey().eContainer();
			final TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(softPkg);
			final IProject project = ModelUtil.getProject(softPkg);
			final WaveDevSettings waveDev = CodegenUtil.loadWaveDevSettings(softPkg);

			// Refresh project before generating code
			project.refreshLocal(IResource.DEPTH_INFINITE, null);

			final MultiStatus retStatus = new MultiStatus(RedhawkCodegenUiActivator.PLUGIN_ID, IStatus.OK, "Problems while generating code", null);
			for (Map.Entry<Implementation, String[]> entry : implMap.entrySet()) {
				if (progress.isCanceled()) {
					return Status.CANCEL_STATUS;
				}

				SubMonitor implGenerateWork = progress.newChild(1);
				implGenerateWork.beginTask("Generating " + entry.getKey().getId(), 1);

				final Implementation impl = entry.getKey();
				IStatus status = validate(project, softPkg, impl, waveDev);
				if (!status.isOK()) {
					retStatus.add(status);
					if (retStatus.getSeverity() == IStatus.ERROR) {
						return retStatus;
					}
				}

				// Generate code for each implementation
				final EMap<String, ImplementationSettings> implSet = waveDev.getImplSettings();
				// Generate code for implementation
				final ImplementationSettings settings = implSet.get(impl.getId());
				final ArrayList<FileToCRCMap> mapping = new ArrayList<FileToCRCMap>();

				String[] filesToGenerate = entry.getValue();
				status = generateImplementation(filesToGenerate, impl, settings, implGenerateWork.newChild(1), softPkg, mapping);
				if (!status.isOK()) {
					retStatus.add(status);
					if (status.getSeverity() == IStatus.ERROR) {
						return retStatus;
					}
				}

				// Update CRCs for implementation
				try {
					updateCRCs(domain, settings, mapping);
				} catch (final IOException e) {
					retStatus.add(new Status(IStatus.WARNING, RedhawkCodegenUiActivator.PLUGIN_ID, "Problem while generating CRCs for implementations", e));
				}

				ImplementationSettings implSettings = WaveDevUtil.getImplSettings(impl);
				final IScaComponentCodegen generator = GeneratorUtil.getGenerator(implSettings);
				final Version codeGenVersion = generator.getCodegenVersion();

				if (new Version(1, 10, 0).compareTo(codeGenVersion) <= 0) {
					// Set the version
					ScaModelCommand.execute(softPkg, new ScaModelCommand() {
						@Override
						public void execute() {
							softPkg.setType(generator.getCodegenVersion().toString());
						}
					});

				}
			}

			// Save updates to the SPD (codegen version) and wavedev (historically, file CRCs)
			// Our model object may / most likely belongs to an editor
			progress.setTaskName("Saving resource changes");
			RunnableWithResult<Boolean> saveViaEditor = new RunnableWithResult.Impl<Boolean>() {
				@Override
				public void run() {
					IEditorPart editorPart = ResourceUtil.findEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
						project.getFile(softPkg.eResource().getURI().lastSegment()));
					if (editorPart != null && editorPart.isDirty()) {
						editorPart.doSave(new NullProgressMonitor());
						setResult(true);
					}
				}
			};
			Display.getDefault().syncExec(saveViaEditor);

			// If we were unable to save via editor, save the resources directly
			if (saveViaEditor.getResult() == null) {
				try {
					softPkg.eResource().save(null);
				} catch (IOException e) {
					retStatus.add(new Status(Status.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID, "Error when updating generator version", e));
				}
				try {
					waveDev.eResource().save(null);
					if (domain != null) {
						((BasicCommandStack) domain.getCommandStack()).saveIsDone();
						domain.getCommandStack().flush();
					}
				} catch (IOException e) {
					retStatus.add(new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID, "Unable to save the updated implementation settings", e));
				}
			}

			// Add the top-level build.sh script builder only for projects that are deprecated (i.e. 1.8)
			// We should remove this section and associated code when we no longer support codegen of 1.8 projects
			progress.setTaskName("Adding builders");
			boolean isDeprecated = false;
			if (waveDev != null) {
				for (final ImplementationSettings implSettings : waveDev.getImplSettings().values()) {
					if (implSettings != null) {
						ICodeGeneratorDescriptor generator = RedhawkCodegenActivator.getCodeGeneratorsRegistry().findCodegen(implSettings.getGeneratorId());
						if (generator != null && generator.isDeprecated()) {
							isDeprecated = true;
							break;
						}
					}
				}
			}
			if (isDeprecated) {
				CodegenUtil.addTopLevelBuildScriptBuilder(project, progress.newChild(1));
			} else {
				CodegenUtil.removeDeprecatedBuilders(project, progress.newChild(1));
			}

			// Refresh project after generating code
			progress.setTaskName("Refreshing project");
			project.refreshLocal(IResource.DEPTH_INFINITE, progress.newChild(1));

			if (ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding()) {
				// Schedule a new job which will run a full build; this should ensure all resource change
				// notifications are dispatched before beginning the build
				final WorkspaceJob buildJob = new WorkspaceJob("Building Project " + project.getName()) {
					@Override
					public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
						final int CLEAN_WORK = 15;
						final int BUILD_WORK = 85;
						SubMonitor progress = SubMonitor.convert(monitor, CLEAN_WORK + BUILD_WORK);

						project.build(IncrementalProjectBuilder.CLEAN_BUILD, progress.newChild(CLEAN_WORK));
						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}

						project.build(IncrementalProjectBuilder.FULL_BUILD, progress.newChild(BUILD_WORK));
						return Status.OK_STATUS;
					}
				};
				buildJob.setPriority(Job.LONG);
				buildJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
				buildJob.schedule();
			}

			return retStatus;
		} finally {
			monitor.done();
		}

	}

	private static IStatus generateImplementation(final String[] files, final Implementation impl, final ImplementationSettings settings,
		final IProgressMonitor monitor, final SoftPkg softpkg, final List<FileToCRCMap> crcMap) {
		if (settings == null) {
			return new Status(IStatus.WARNING, RedhawkCodegenUiActivator.PLUGIN_ID, "Unable to find settings (wavedev) for " + impl.getId()
				+ ", skipping generation");
		}

		final String implId = impl.getId();
		final MultiStatus retStatus = new MultiStatus(RedhawkCodegenUiActivator.PLUGIN_ID, IStatus.OK, "Problems while generating implementation " + implId,
			null);
		final SubMonitor progress = SubMonitor.convert(monitor, 1);
		progress.setTaskName("Generating implementation " + implId);

		if (settings.getGeneratorId() != null) {
			final String codegenId = settings.getGeneratorId();
			final ICodeGeneratorDescriptor codeGenDesc = RedhawkCodegenActivator.getCodeGeneratorsRegistry().findCodegen(codegenId);
			if (codeGenDesc == null) {
				retStatus.add(new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID,
					"The code generator specified in the settings (wavedev) could not be found. Check your generator selection for the implementation"));
				return retStatus;
			}

			// Find the code generator console, or create it if necessary
			GeneratorConsole genConsole = null;
			final IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
			for (final IConsole console : consoles) {
				if (console instanceof GeneratorConsole && console.getType().equals(codeGenDesc.getId())) {
					genConsole = (GeneratorConsole) console;
					break;
				}
			}
			if (genConsole == null) {
				genConsole = new GeneratorConsole(codeGenDesc);
				ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { genConsole });
			}

			try {
				// Get the specific code generator
				final IScaComponentCodegen generator = codeGenDesc.getGenerator();

				// Validate that we can perform code generation
				IStatus status = generator.validate();
				if (!status.isOK()) {
					retStatus.add(status);
					if (status.getSeverity() == IStatus.ERROR) {
						return retStatus;
					}
				}

				final IFile mainFile = generator.getDefaultFile(impl, settings);
				boolean openEditor = (mainFile == null || !mainFile.exists());

				status = generator.generate(settings, impl, genConsole.getOutStream(), genConsole.getErrStream(), progress.newChild(1), files,
					generator.shouldGenerate(), crcMap);
				if (!status.isOK()) {
					retStatus.add(status);
					if (status.getSeverity() == IStatus.ERROR) {
						return retStatus;
					}
				}

				// Update last generated date
				final WaveDevSettings wavedev = CodegenUtil.loadWaveDevSettings(softpkg);
				PropertyUtil.setLastGenerated(wavedev, settings, new Date(System.currentTimeMillis()));

				if (openEditor && (mainFile != null) && mainFile.exists()) {
					progress.subTask("Opening editor for main file");

					// Open the selected editor
					final WorkbenchJob openJob = new WorkbenchJob("Open editor") {
						@Override
						public IStatus runInUIThread(final IProgressMonitor monitor) {
							try {
								IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), mainFile, true);
							} catch (final PartInitException p) {
								return new Status(IStatus.WARNING, RedhawkCodegenUiActivator.PLUGIN_ID, "Unable to open main file for editing.");
							}
							return new Status(IStatus.OK, RedhawkCodegenUiActivator.PLUGIN_ID, "");
						}
					};
					openJob.setPriority(Job.SHORT);
					openJob.schedule();
				}

				if (retStatus.isOK()) {
					return new Status(IStatus.OK, RedhawkCodegenUiActivator.PLUGIN_ID, "Succeeded generating code for implementation");
				}

			} catch (final CoreException e) {
				retStatus.add(new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID, "Unexpected error", e));
			}
		} else {
			retStatus.add(new Status(
				IStatus.WARNING,
				RedhawkCodegenUiActivator.PLUGIN_ID,
				"No code generator is specified in the settings (wavedev). Code generation was skipped for the implementation. Check your generator selection for the implementation."));
		}

		return retStatus;
	}

	/**
	 * Figures out which files will be generated, possibly prompting the user to
	 * confirm generation if files have been modified since they were originally
	 * generated.
	 * 
	 * @param generator The code generator to use
	 * @param implSettings The settings for the implementation
	 * @param softpkg The SPD
	 * @return An array of the files which are to be generated
	 * @throws CoreException A problem occurs while determining which files to generate
	 * @since 8.0
	 */
	private static Set<FileStatus> getFilesToGenerate(IProgressMonitor monitor, Implementation impl) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Calculating files for implementation " + impl.getId(), IProgressMonitor.UNKNOWN);
		try {
			final ImplementationSettings implSettings = WaveDevUtil.getImplSettings(impl);
			final IScaComponentCodegen generator = GeneratorUtil.getGenerator(implSettings);

			final SoftPkg softpkg = (SoftPkg) impl.eContainer();

			if (generator.shouldGenerate()) {
				Future<Set<FileStatus>> future = EXECUTOR_POOL.submit(new Callable<Set<FileStatus>>() {

					@Override
					public Set<FileStatus> call() throws Exception {
						return generator.getGeneratedFilesStatus(implSettings, softpkg);
					}
				});
				Set<FileStatus> retVal;
				while (true) {
					try {
						retVal = future.get(1, TimeUnit.SECONDS);
						break;
					} catch (InterruptedException e) {
						throw new CoreException(Status.CANCEL_STATUS);
					} catch (ExecutionException e) {
						if (e.getCause() instanceof CoreException) {
							throw ((CoreException) e.getCause());
						} else {
							throw new CoreException(new Status(Status.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID,
								"Failed in calling generator get generated files.", e));
						}
					} catch (TimeoutException e) {
						if (subMonitor.isCanceled()) {
							future.cancel(true);
							throw new OperationCanceledException();
						}
					}
				}
				return retVal;
			} else {
				return Collections.emptySet();
			}
		} finally {
			subMonitor.done();
		}
	}

	/**
	 * Performs several validations to detect problems prior to code generation.
	 * 
	 * @param project The project to validate
	 * @param softPkg The {@link SoftPkg} to validate
	 * @param impls The {@link Implementation}s to be generated
	 * @param waveDev The wavedev to validate
	 * @return An {@link IStatus} indicating any issues found; problems should be of severity {@link IStatus#ERROR} to
	 * prevent code generation
	 */
	private static IStatus validate(final IProject project, final SoftPkg softPkg, Implementation impl, final WaveDevSettings waveDev) {
		final MultiStatus retStatus = new MultiStatus(RedhawkCodegenUiActivator.PLUGIN_ID, IStatus.OK, "Validation problems prior to generating code", null);
		if (project == null) {
			return new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID, "Project does not exist");
		}

		// Check XML files
		try {
			// Check SPD file
			final String spdFileName = ModelUtil.getSpdFileName(softPkg);
			if (spdFileName == null) {
				retStatus.add(new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID, "Unable to determine SPD filename"));
			} else {
				final IFile file = project.getFile(spdFileName);
				if (!file.exists()) {
					retStatus.add(new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID, "Cannot locate SPD file"));
				} else {
					for (final IMarker mark : file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)) {
						if (mark.getAttribute(IMarker.SEVERITY, 0) == IMarker.SEVERITY_ERROR) {
							retStatus.add(new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID, "The SPD file contains errors"));
							break;
						}
					}
				}
			}

			// Don't validate SCD or PRF if project is a Shared Library
			if (!impl.isSharedLibrary()) {
				// Check SCD file
				final String scdFileName = ModelUtil.getScdFileName(softPkg);
				if (scdFileName == null) {
					retStatus.add(new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID, "Unable to determine SCD filename"));
				} else {
					final IFile file = project.getFile(scdFileName);
					if (!file.exists()) {
						retStatus.add(new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID, "Cannot locate SCD file"));
					} else {
						for (final IMarker mark : file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)) {
							if (mark.getAttribute(IMarker.SEVERITY, 0) == IMarker.SEVERITY_ERROR) {
								retStatus.add(new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID, "The SCD file contains errors"));
								break;
							}
						}

					}
				}

				// Check PRF file
				final String prfFileName = ModelUtil.getPrfFileName(softPkg.getPropertyFile());
				if (prfFileName != null) {
					final IFile file = project.getFile(prfFileName);
					if (!file.exists()) {
						retStatus.add(new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID, "Cannot locate PRF file"));
					} else {
						for (final IMarker mark : file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)) {
							if (mark.getAttribute(IMarker.SEVERITY, 0) == IMarker.SEVERITY_ERROR) {
								retStatus.add(new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID, "The PRF file contains errors"));
								break;
							}
						}
					}
				}
			}
		} catch (final CoreException e) {
			retStatus.add(new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID, "A problem occurred while verifying the XML files", e));
		}

		// Wavedev checks
		if (waveDev == null) {
			retStatus.add(new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID,
				"Unable to find project settings (wavedev) file. Cannot generate code."));
		} else {
			ImplementationSettings implSettings = waveDev.getImplSettings().get(impl.getId());
			if (implSettings == null) {
				retStatus.add(new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID, "Unable to find settings in wavedev file for implementation "
					+ impl.getId()));
			} else {
				String templateId = implSettings.getTemplate();
				ITemplateDesc template = RedhawkCodegenActivator.getCodeGeneratorTemplatesRegistry().findTemplate(templateId);
				if (template == null) {
					retStatus.add(new Status(IStatus.ERROR, RedhawkCodegenUiActivator.PLUGIN_ID, "Unable to find code generator template" + templateId));
				}
			}
		}

		return retStatus;
	}

	private static void updateCRCs(EditingDomain domain, final ImplementationSettings implSettings, final List<FileToCRCMap> crcMap) throws IOException {
		final Map<FileToCRCMap, FileToCRCMap> foundCRCs = new HashMap<FileToCRCMap, FileToCRCMap>();
		for (final FileToCRCMap entry : implSettings.getGeneratedFileCRCs()) {
			for (final FileToCRCMap currCRC : crcMap) {
				if (entry.getFile().equals(currCRC.getFile())) {
					foundCRCs.put(entry, currCRC);
					crcMap.remove(currCRC);
					break;
				}
			}
		}
		if (domain != null) {
			final CompoundCommand updateCommand = new CompoundCommand();
			for (final FileToCRCMap crc : crcMap) {
				final AddCommand cmd = new AddCommand(domain, implSettings.getGeneratedFileCRCs(), crc);
				updateCommand.append(cmd);
			}

			for (final Entry<FileToCRCMap, FileToCRCMap> crcEntry : foundCRCs.entrySet()) {
				final SetCommand cmd = new SetCommand(domain, crcEntry.getKey(), CodegenPackage.Literals.FILE_TO_CRC_MAP__CRC, crcEntry.getValue().getCrc());
				updateCommand.append(cmd);
			}
			if (!updateCommand.isEmpty()) {
				domain.getCommandStack().execute(updateCommand);
			}
		} else {
			for (final FileToCRCMap crc : crcMap) {
				implSettings.getGeneratedFileCRCs().add(crc);
			}

			for (final Entry<FileToCRCMap, FileToCRCMap> crcEntry : foundCRCs.entrySet()) {
				crcEntry.getKey().setCrc(crcEntry.getValue().getCrc());
			}
		}
	}
}
