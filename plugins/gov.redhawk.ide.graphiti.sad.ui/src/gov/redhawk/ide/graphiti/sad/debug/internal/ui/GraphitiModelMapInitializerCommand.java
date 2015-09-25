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
package gov.redhawk.ide.graphiti.sad.debug.internal.ui;

import gov.redhawk.ide.debug.LocalScaComponent;
import gov.redhawk.ide.debug.LocalScaWaveform;
import gov.redhawk.ide.debug.ui.diagram.LocalScaDiagramPlugin;
import gov.redhawk.ide.graphiti.sad.ui.SADUIGraphitiPlugin;
import gov.redhawk.model.sca.ScaComponent;
import gov.redhawk.model.sca.ScaConnection;
import gov.redhawk.model.sca.ScaPort;
import gov.redhawk.model.sca.ScaPortContainer;
import gov.redhawk.model.sca.ScaProvidesPort;
import gov.redhawk.model.sca.ScaUsesPort;
import gov.redhawk.model.sca.ScaWaveform;
import gov.redhawk.sca.util.Debug;
import gov.redhawk.sca.util.PluginUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import mil.jpeojtrs.sca.partitioning.ComponentFile;
import mil.jpeojtrs.sca.partitioning.ComponentFileRef;
import mil.jpeojtrs.sca.partitioning.ComponentSupportedInterface;
import mil.jpeojtrs.sca.partitioning.NamingService;
import mil.jpeojtrs.sca.partitioning.PartitioningFactory;
import mil.jpeojtrs.sca.sad.FindComponent;
import mil.jpeojtrs.sca.sad.SadComponentInstantiation;
import mil.jpeojtrs.sca.sad.SadComponentInstantiationRef;
import mil.jpeojtrs.sca.sad.SadComponentPlacement;
import mil.jpeojtrs.sca.sad.SadConnectInterface;
import mil.jpeojtrs.sca.sad.SadFactory;
import mil.jpeojtrs.sca.sad.SadPartitioning;
import mil.jpeojtrs.sca.sad.SadProvidesPort;
import mil.jpeojtrs.sca.sad.SadUsesPort;
import mil.jpeojtrs.sca.sad.SoftwareAssembly;
import mil.jpeojtrs.sca.spd.SoftPkg;
import mil.jpeojtrs.sca.util.ProtectedThreadExecutor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.statushandlers.StatusManager;

import ExtendedCF.UsesConnection;

public class GraphitiModelMapInitializerCommand extends AbstractCommand {
	private static final Debug DEBUG = new Debug(LocalScaDiagramPlugin.PLUGIN_ID, "init");

	private final GraphitiModelMap modelMap;
	private final LocalScaWaveform waveform;
	private final SoftwareAssembly sad;

	public GraphitiModelMapInitializerCommand(final GraphitiModelMap map, final SoftwareAssembly sad, final LocalScaWaveform waveform) {
		this.modelMap = map;
		this.waveform = waveform;
		this.sad = sad;
	}

	public static void initConnection(@NonNull final ScaConnection con, SoftwareAssembly sad, ScaWaveform waveform, GraphitiModelMap modelMap) {
		final ScaUsesPort uses = con.getPort();
		final ScaPortContainer container = uses.getPortContainer();
		if (!(container instanceof ScaComponent)) {
			// Can only add connections within Components
			return;
		}
		final LocalScaComponent comp = (LocalScaComponent) container;

		// Initialize the connection ID and the USES side of the connection
		final SadConnectInterface sadCon = SadFactory.eINSTANCE.createSadConnectInterface();
		final SadUsesPort usesPort = SadFactory.eINSTANCE.createSadUsesPort();
		final SadComponentInstantiationRef usesCompRef = SadFactory.eINSTANCE.createSadComponentInstantiationRef();
		usesCompRef.setInstantiation(modelMap.get(comp));
		usesPort.setComponentInstantiationRef(usesCompRef);
		usesPort.setUsesIdentifier(uses.getName());
		sadCon.setUsesPort(usesPort);
		sadCon.setId(con.getId());

		// Initialize the Target side of the connection
		boolean foundTarget = false;
		final UsesConnection conData = con.getData();
		final org.omg.CORBA.Object target = conData.port;
		outC: for (final ScaComponent c : waveform.getComponents()) {
			if (is_equivalent(target, c.getObj())) {
				final ComponentSupportedInterface csi = PartitioningFactory.eINSTANCE.createComponentSupportedInterface();
				final SadComponentInstantiationRef ref = SadFactory.eINSTANCE.createSadComponentInstantiationRef();
				ref.setInstantiation(modelMap.get((LocalScaComponent) c));
				csi.setComponentInstantiationRef(ref);
				csi.setSupportedIdentifier(uses.getProfileObj().getRepID());
				sadCon.setComponentSupportedInterface(csi);
				foundTarget = true;
				break outC;
			} else {
				for (final ScaPort< ? , ? > cPort : c.getPorts()) {
					if (cPort instanceof ScaProvidesPort && is_equivalent(target, cPort.getObj())) {
						final SadProvidesPort sadProvidesPort = SadFactory.eINSTANCE.createSadProvidesPort();
						final SadComponentInstantiationRef ref = SadFactory.eINSTANCE.createSadComponentInstantiationRef();
						ref.setInstantiation(modelMap.get((LocalScaComponent) c));
						sadProvidesPort.setComponentInstantiationRef(ref);
						sadProvidesPort.setProvidesIdentifier(cPort.getName());
						sadCon.setProvidesPort(sadProvidesPort);
						foundTarget = true;
						break outC;
					}
				}
			}
		}
		// We were unable to find the target side of the connection, so ignore it
		if (foundTarget) {
			if (sad.getConnections() == null) {
				sad.setConnections(SadFactory.eINSTANCE.createSadConnections());
			}
			sad.getConnections().getConnectInterface().add(sadCon);
			modelMap.put(con, sadCon);
		} else {
			if (GraphitiModelMapInitializerCommand.DEBUG.enabled) {
				GraphitiModelMapInitializerCommand.DEBUG.trace("Failed to initialize connection " + con.getId());
			}
		}
	}

	private static boolean is_equivalent(final org.omg.CORBA.Object obj1, final org.omg.CORBA.Object obj2) {
		if (obj1 == null || obj2 == null) {
			return false;
		}
		if (obj1 == obj2) {
			return true;
		}
		try {
			return ProtectedThreadExecutor.submit(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					return obj1._is_equivalent(obj2);
				}

			});
		} catch (InterruptedException e) {
			return false;
		} catch (ExecutionException e) {
			return false;
		} catch (TimeoutException e) {
			return false;
		}

	}

	public static void initComponent(@NonNull final LocalScaComponent comp, SoftwareAssembly sad, GraphitiModelMap modelMap) {

		if (comp.getProfileObj() == null) {
			try {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
				dialog.run(true, true, new IRunnableWithProgress() {

					@Override
					public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Loading Component: " + comp, IProgressMonitor.UNKNOWN);
						try {
							comp.fetchProfileObject(monitor);
							monitor.worked(1);
						} finally {
							monitor.done();
						}
					}
				});
			} catch (final InvocationTargetException | InterruptedException e) {
				StatusManager.getManager().handle(
					new Status(IStatus.ERROR, SADUIGraphitiPlugin.PLUGIN_ID, "Errors occured while loading component: " + comp, e),
					StatusManager.SHOW | StatusManager.LOG);
			}
		}

		final SoftPkg spd = comp.getProfileObj();

		if (spd == null) {
			// For some reason we couldn't find the SPD Abort.
			PluginUtil.logError(LocalScaDiagramPlugin.getDefault(), "Failed to find Soft Pkg for comp: " + comp.getInstantiationIdentifier(), null);
			return;
		}

		ComponentFile spdFile = null;
		for (final ComponentFile file : sad.getComponentFiles().getComponentFile()) {
			if (file.getSoftPkg() != null) {
				if (PluginUtil.equals(file.getSoftPkg().getId(), spd.getId())) {
					spdFile = file;
					break;
				}
			}
		}
		if (spdFile == null) {
			spdFile = SadFactory.eINSTANCE.createComponentFile();
			spdFile.setSoftPkg(spd);
			sad.getComponentFiles().getComponentFile().add(spdFile);
		}

		final SadPartitioning partitioning = sad.getPartitioning();

		final SadComponentPlacement placement = SadFactory.eINSTANCE.createSadComponentPlacement();
		partitioning.getComponentPlacement().add(placement);

		final SadComponentInstantiation inst = SadFactory.eINSTANCE.createSadComponentInstantiation();
		inst.setUsageName(comp.getName());
		final FindComponent findComponent = SadFactory.eINSTANCE.createFindComponent();
		final NamingService namingService = PartitioningFactory.eINSTANCE.createNamingService();
		namingService.setName(comp.getName());
		findComponent.setNamingService(namingService);
		inst.setFindComponent(findComponent);
		inst.setId(comp.getInstantiationIdentifier());

		final ComponentFileRef fileRef = PartitioningFactory.eINSTANCE.createComponentFileRef();
		fileRef.setFile(spdFile);

		inst.setPlacement(placement);
		placement.setComponentFileRef(fileRef);

		modelMap.put(comp, inst);
	}

	@Override
	public void execute() {
		this.sad.setComponentFiles(PartitioningFactory.eINSTANCE.createComponentFiles());
		this.sad.setPartitioning(SadFactory.eINSTANCE.createSadPartitioning());
		this.sad.setConnections(SadFactory.eINSTANCE.createSadConnections());
		this.sad.setAssemblyController(null);
		this.sad.setExternalPorts(null);
		if (waveform != null) {
			for (final ScaComponent comp : this.waveform.getComponents()) {
				if (comp != null) {
					initComponent((LocalScaComponent) comp, sad, modelMap);
				}
			}

			for (final ScaComponent comp : this.waveform.getComponents()) {
				if (comp == null) {
					continue;
				}
				List<ScaPort< ? , ? >> ports = Collections.emptyList();
				ports = comp.getPorts();
				for (final ScaPort< ? , ? > port : ports) {
					if (port instanceof ScaUsesPort) {
						final ScaUsesPort uses = (ScaUsesPort) port;
						List<ScaConnection> connections = Collections.emptyList();
						connections = uses.getConnections();

						for (final ScaConnection con : connections) {
							if (con != null) {
								initConnection(con, sad, waveform, modelMap);
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected boolean prepare() {
		return true;
	}

	@Override
	public boolean canUndo() {
		return false;
	}

	@Override
	public void redo() {
		throw new UnsupportedOperationException();

	}
}
