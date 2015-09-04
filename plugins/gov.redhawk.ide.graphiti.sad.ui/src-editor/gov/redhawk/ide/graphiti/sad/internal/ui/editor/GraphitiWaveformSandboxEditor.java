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
package gov.redhawk.ide.graphiti.sad.internal.ui.editor;

import gov.redhawk.ide.debug.LocalSca;
import gov.redhawk.ide.debug.LocalScaWaveform;
import gov.redhawk.ide.debug.ScaDebugFactory;
import gov.redhawk.ide.debug.ScaDebugPlugin;
import gov.redhawk.ide.debug.internal.LocalApplicationFactory;
import gov.redhawk.ide.debug.internal.ScaDebugInstance;
import gov.redhawk.ide.debug.internal.cf.extended.impl.ApplicationImpl;
import gov.redhawk.ide.debug.internal.ui.diagram.NewWaveformFromLocalWizard;
import gov.redhawk.ide.graphiti.sad.debug.internal.ui.GraphitiModelMap;
import gov.redhawk.ide.graphiti.sad.debug.internal.ui.GraphitiModelMapInitializerCommand;
import gov.redhawk.ide.graphiti.sad.debug.internal.ui.SadGraphitiModelAdapter;
import gov.redhawk.ide.graphiti.sad.debug.internal.ui.SadGraphitiModelInitializerCommand;
import gov.redhawk.ide.graphiti.sad.debug.internal.ui.ScaGraphitiModelAdapter;
import gov.redhawk.ide.graphiti.sad.ui.SADUIGraphitiPlugin;
import gov.redhawk.ide.graphiti.sad.ui.diagram.GraphitiWaveformDiagramEditor;
import gov.redhawk.ide.graphiti.ui.diagram.util.DUtil;
import gov.redhawk.ide.sad.ui.SadUiActivator;
import gov.redhawk.model.sca.RefreshDepth;
import gov.redhawk.model.sca.ScaComponent;
import gov.redhawk.model.sca.ScaWaveform;
import gov.redhawk.model.sca.commands.ScaModelCommand;
import gov.redhawk.monitor.MonitorPlugin;
import gov.redhawk.monitor.MonitorPortAdapter;
import gov.redhawk.sca.ui.ScaFileStoreEditorInput;
import gov.redhawk.sca.util.Debug;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import mil.jpeojtrs.sca.sad.SadFactory;
import mil.jpeojtrs.sca.sad.SoftwareAssembly;
import mil.jpeojtrs.sca.util.CorbaUtils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalCommandStack;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 *
 */
@SuppressWarnings("restriction")
public class GraphitiWaveformSandboxEditor extends GraphitiWaveformMultiPageEditor {
	public static final String EDITOR_ID = "gov.redhawk.ide.graphiti.sad.ui.editor.localMultiPageSca";
	private static final Debug DEBUG = new Debug(SADUIGraphitiPlugin.PLUGIN_ID, "editor");
	private ScaGraphitiModelAdapter scaListener;
	private SadGraphitiModelAdapter sadlistener;
	private MonitorPortAdapter portStatisticsAdapter;
	private LocalScaWaveform waveform;
	private boolean isLocalSca;
	private Resource mainResource;
	private SoftwareAssembly sad;
	private GraphitiModelMap modelMap;

	@Override
	protected void createModel() {
		if (isLocalSca) {
			mainResource = getEditingDomain().getResourceSet().createResource(ScaDebugInstance.getLocalSandboxWaveformURI());
			sad = SadFactory.eINSTANCE.createSoftwareAssembly();
			getEditingDomain().getCommandStack().execute(new ScaModelCommand() {

				@Override
				public void execute() {
					mainResource.getContents().add(sad);
				}
			});
		} else {
			super.createModel();
			sad = SoftwareAssembly.Util.getSoftwareAssembly(super.getMainResource());
		}
	}

	@Override
	protected void setInput(IEditorInput input) {
		if (input instanceof ScaFileStoreEditorInput) {
			ScaFileStoreEditorInput scaInput = (ScaFileStoreEditorInput) input;
			if (scaInput.getScaObject() instanceof LocalScaWaveform) {
				this.waveform = (LocalScaWaveform) scaInput.getScaObject();
			} else if (scaInput.getScaObject() instanceof ScaWaveform) {
				this.waveform = getLocalScaWaveform((ScaWaveform) scaInput.getScaObject());
			} else {
				throw new IllegalStateException("Sandbox Editor opened on invalid sca input " + scaInput.getScaObject());
			}
		} else if (input instanceof URIEditorInput) {
			URIEditorInput uriInput = (URIEditorInput) input;
			if (uriInput.getURI().equals(ScaDebugInstance.getLocalSandboxWaveformURI())) {
				final LocalSca localSca = ScaDebugPlugin.getInstance().getLocalSca();
				if (!ScaDebugInstance.INSTANCE.isInit()) {
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
					try {
						dialog.run(true, true, new IRunnableWithProgress() {

							@Override
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								monitor.beginTask("Starting Chalkboard...", IProgressMonitor.UNKNOWN);
								try {
									ScaDebugInstance.INSTANCE.init(monitor);
								} catch (CoreException e) {
									throw new InvocationTargetException(e);
								}
								monitor.done();
							}

						});
					} catch (InvocationTargetException e1) {
						StatusManager.getManager().handle(new Status(Status.ERROR, ScaDebugPlugin.ID, "Failed to initialize sandbox.", e1),
							StatusManager.SHOW | StatusManager.LOG);
					} catch (InterruptedException e1) {
						// PASS
					}
				}

				this.waveform = localSca.getSandboxWaveform();
				if (waveform == null) {
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
					try {
						dialog.run(true, true, new IRunnableWithProgress() {

							@Override
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								monitor.beginTask("Starting Sandbox...", IProgressMonitor.UNKNOWN);
								try {
									ScaDebugPlugin.getInstance().getLocalSca(monitor);
								} catch (CoreException e) {
									throw new InvocationTargetException(e);
								}

							}
						});
					} catch (InvocationTargetException e) {
						throw new IllegalStateException("Failed to setup sandbox", e);
					} catch (InterruptedException e) {
						throw new IllegalStateException("Sandbox setup canceled, can not load editor.");
					}
					this.waveform = localSca.getSandboxWaveform();
					if (waveform == null) {
						throw new IllegalStateException("Failed to setup sandbox, null waveform.", null);
					}
				}
			}
		} else {
			throw new IllegalStateException("Sandbox Editor opened on invalid input " + input);
		}

		if (ScaDebugPlugin.getInstance().getLocalSca().getSandboxWaveform() == waveform || this.waveform == null) {
			isLocalSca = true;
		}

		super.setInput(input);
	}

	@Override
	public Resource getMainResource() {
		if (mainResource == null) {
			return super.getMainResource();
		}
		return mainResource;
	}

	private LocalScaWaveform getLocalScaWaveform(final ScaWaveform remoteWaveform) {
		// Try to find a LocalScaWaveform object with the same identifier as the domain waveform. If found, it's
		// the proxy
		final LocalSca localSca = ScaDebugPlugin.getInstance().getLocalSca();
		for (ScaWaveform localWaveform : localSca.getWaveforms()) {
			if (localWaveform.getIdentifier() != null && localWaveform.getIdentifier().equals(remoteWaveform.getIdentifier())
				&& localWaveform instanceof LocalScaWaveform) {
				return (LocalScaWaveform) localWaveform;
			}
		}

		// Create a new ScaLocalWaveform from the ScaWaveform
		final LocalScaWaveform localWaveform = ScaDebugFactory.eINSTANCE.createLocalScaWaveform(remoteWaveform);
		ScaModelCommand.execute(localSca, new ScaModelCommand() {

			@Override
			public void execute() {
				localSca.getWaveforms().add(localWaveform);
			}
		});

		// Bind the application
		if (Display.getCurrent() != null) {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
			try {
				dialog.run(true, true, new IRunnableWithProgress() {

					@Override
					public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							CorbaUtils.invoke(new Callable<Object>() {

								@Override
								public Object call() throws Exception {
									LocalApplicationFactory.bindApp((ApplicationImpl) localWaveform.getLocalApp());
									return null;
								}

							}, monitor);
						} catch (CoreException e1) {
							throw new InvocationTargetException(e1);
						}

					}
				});
			} catch (InvocationTargetException e) {
				throw new IllegalStateException("Failed to bind waveform", e);
			} catch (InterruptedException e) {
				// PASS
			}
		} else {
			try {
				LocalApplicationFactory.bindApp((ApplicationImpl) localWaveform.getLocalApp());
			} catch (CoreException e) {
				throw new IllegalStateException("Failed to bind waveform", e);
			}
		}

		return localWaveform;
	}

	/**
	 * Returns the property value that should be set for the Diagram container's DIAGRAM_CONTEXT property.
	 * Indicates the mode the diagram is operating in.
	 * @return
	 */
	@Override
	public String getDiagramContext(Resource sadResource) {
		return DUtil.DIAGRAM_CONTEXT_LOCAL;
	}

	private void initModelMap() throws CoreException {
		if (waveform == null) {
			throw new IllegalStateException("Can not initialize the Model Map with null local waveform");
		}
		if (sad == null) {
			throw new IllegalStateException("Can not initialize the Model Map with null sad");
		}

		if (!waveform.isSetComponents()) {
			if (Display.getCurrent() != null) {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
				try {
					dialog.run(true, true, new IRunnableWithProgress() {

						@Override
						public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try {
								CorbaUtils.invoke(new Callable<Object>() {

									@Override
									public Object call() throws Exception {
										waveform.refresh(monitor, RefreshDepth.FULL);
										return null;
									}

								}, monitor);
							} catch (CoreException e) {
								throw new InvocationTargetException(e);
							}
						}
					});
				} catch (InvocationTargetException | InterruptedException e) {
					// PASS
				}
			} else {
				try {
					waveform.refresh(null, RefreshDepth.FULL);
				} catch (InterruptedException e) {
					// PASS
				}
			}
		}

		try {
			ProgressMonitorDialog loadCompDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
			final int numOfLoadingItems = waveform.getComponents().size() * 2; // for getProfile and getStarted calls
			loadCompDialog.run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Loading Waveform Components...", numOfLoadingItems);

					ExecutorService executor = Executors.newSingleThreadExecutor();
					Future<Object> future = executor.submit(new Callable<Object>() {

						@Override
						public Object call() throws Exception {
							int totalProgress = 0;
							while (totalProgress < numOfLoadingItems && !monitor.isCanceled()) {
								int newProgress = 0;

								for (ScaComponent component : waveform.getComponents()) {
									if (component.getProfile() != null) {
										newProgress++;
									}

									if (component.getStarted() != null) {
										newProgress++;
									}
								}

								if (newProgress > totalProgress) {
									monitor.worked(newProgress - totalProgress);
									totalProgress = newProgress;
								}
								Thread.sleep(250);
							}
							return null;
						}

					});

					try {
						future.get(30, TimeUnit.SECONDS);
					} catch (InterruptedException | ExecutionException | TimeoutException e) {
						monitor.setCanceled(true);
						StatusManager.getManager().handle(new Status(IStatus.ERROR, SADUIGraphitiPlugin.PLUGIN_ID, "Waveform components failed to load", e),
							StatusManager.SHOW | StatusManager.LOG);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (final Exception e) { // SUPPRESS CHECKSTYLE Logged Catch all exception
			StatusManager.getManager().handle(new Status(IStatus.ERROR, SADUIGraphitiPlugin.PLUGIN_ID, "Errors occured while loading waveform components", e),
				StatusManager.SHOW | StatusManager.LOG);
		}

		modelMap = new GraphitiModelMap(this, sad, waveform);

		this.sadlistener = new SadGraphitiModelAdapter(modelMap);
		this.scaListener = new ScaGraphitiModelAdapter(modelMap) {
			@Override
			public void notifyChanged(Notification notification) {
				super.notifyChanged(notification);
				if (notification.getNotifier() == waveform) {
					if (waveform.isDisposed() && !isDisposed()) {
						getEditorSite().getPage().getWorkbenchWindow().getWorkbench().getDisplay().asyncExec(new Runnable() {

							@Override
							public void run() {
								if (!isDisposed()) {
									getEditorSite().getPage().closeEditor(GraphitiWaveformSandboxEditor.this, false);
								}
							}

						});
					}
				}
			}
		};

		if (isLocalSca) {
			// Use the REDHAWK Model source to build the SAD when in the chalkboard since the SAD file isn't modified
			getEditingDomain().getCommandStack().execute(new SadGraphitiModelInitializerCommand(modelMap, sad, waveform));
			ScaModelCommand.execute(this.waveform, new ScaModelCommand() {

				@Override
				public void execute() {
					scaListener.addAdapter(waveform);
				}
			});
		} else {
			// Use the existing SAD file as a template when initializing the modeling map
			TransactionalEditingDomain ed = (TransactionalEditingDomain) getEditingDomain();
			TransactionalCommandStack stack = (TransactionalCommandStack) ed.getCommandStack();
			CompoundCommand command = new CompoundCommand();
			command.append(new GraphitiModelMapInitializerCommand(modelMap, sad, waveform));
			command.append(new ScaModelCommand() {

				@Override
				public void execute() {
					scaListener.addAdapter(waveform);

				}
			});
			stack.execute(command);
		}
		getEditingDomain().getCommandStack().flush();

		// Add port statistics listener
		portStatisticsAdapter = new MonitorPortAdapter(modelMap);
		MonitorPlugin.getDefault().getMonitorRegistry().eAdapters().add(portStatisticsAdapter);

		sad.eAdapters().add(this.sadlistener);

		if (GraphitiWaveformSandboxEditor.DEBUG.enabled) {
			try {
				sad.eResource().save(null);
			} catch (final IOException e) {
				GraphitiWaveformSandboxEditor.DEBUG.catching("Failed to save local diagram.", e);
			}
		}
	}

	@Override
	public void dispose() {
		if (this.sadlistener != null) {
			if (sad != null) {
				sad.eAdapters().remove(this.sadlistener);
			}
			this.sadlistener = null;
		}
		if (this.scaListener != null) {
			ScaModelCommand.execute(waveform, new ScaModelCommand() {

				@Override
				public void execute() {
					waveform.eAdapters().remove(GraphitiWaveformSandboxEditor.this.scaListener);
					MonitorPlugin.getDefault().getMonitorRegistry().eAdapters().remove(portStatisticsAdapter);
				}
			});
			this.scaListener = null;
		}
		super.dispose();
	}

	@Override
	public void doSave(final IProgressMonitor monitor) {
		doSaveAs();
	}

	@Override
	protected void addPages() {
		// Only creates the other pages if there is something that can be edited
		if (!getEditingDomain().getResourceSet().getResources().isEmpty()
			&& !(getEditingDomain().getResourceSet().getResources().get(0)).getContents().isEmpty()) {
			try {
				int pageIndex = 0;

				final Resource sadResource = getMainResource();

				final DiagramEditor editor = createDiagramEditor();
				setDiagramEditor(editor);

				initModelMap();

				final IEditorInput input = createDiagramInput(sadResource);
				pageIndex = addPage(editor, input);
				setPageText(pageIndex, "Diagram");
				setPartName(waveform.getName());

				// set layout for diagram editors
				DUtil.layout(editor);

				getEditingDomain().getCommandStack().removeCommandStackListener(getCommandStackListener());

				// reflect runtime aspects here
				this.modelMap.reflectRuntimeStatus();

				// set layout for sandbox editors
				DUtil.layout(editor);

			} catch (final PartInitException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, SadUiActivator.getPluginId(), "Failed to create editor parts.", e),
					StatusManager.LOG | StatusManager.SHOW);
			} catch (final IOException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, SadUiActivator.getPluginId(), "Failed to create editor parts.", e),
					StatusManager.LOG | StatusManager.SHOW);
			} catch (final CoreException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, SadUiActivator.getPluginId(), "Failed to create editor parts.", e),
					StatusManager.LOG | StatusManager.SHOW);
			}
		}

	}

	@Override
	public List<Object> getOutlineItems() {
		return Collections.emptyList();
	}

	@Override
	protected DiagramEditor createDiagramEditor() {
		GraphitiWaveformDiagramEditor editor = new GraphitiWaveformDiagramEditor((TransactionalEditingDomain) getEditingDomain());
		editor.addContext("gov.redhawk.ide.graphiti.sad.ui.contexts.sandbox");
		return editor;
	}

	@Override
	public void doSaveAs() {
		final NewWaveformFromLocalWizard wizard = new NewWaveformFromLocalWizard(SoftwareAssembly.Util.getSoftwareAssembly(getMainResource()));
		final WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
		dialog.open();

	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		return false;
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public String getTitle() {
		if (waveform != null && waveform.getName() != null) {
			return waveform.getName();
		} else {
			return "Chalkboard";
		}
	}

	@Override
	public String getTitleToolTip() {
		if (waveform != null && waveform.getName() != null) {
			return waveform.getName() + " dynamic waveform";
		} else {
			return "Chalkboard dynamic waveform";
		}
	}

	/**
	 * @return the waveform instance
	 */
	public LocalScaWaveform getWaveform() {
		return waveform;
	}
}
