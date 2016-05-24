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
package gov.redhawk.ide.debug.internal.cf.extended.impl;

import gov.redhawk.ide.debug.ILaunchConfigurationFactory;
import gov.redhawk.ide.debug.LocalScaComponent;
import gov.redhawk.ide.debug.LocalScaWaveform;
import gov.redhawk.ide.debug.NotifyingNamingContext;
import gov.redhawk.ide.debug.ScaDebugPlugin;
import gov.redhawk.ide.debug.SpdLauncherUtil;
import gov.redhawk.ide.debug.internal.ApplicationStreams;
import gov.redhawk.ide.debug.internal.LocalApplicationFactory;
import gov.redhawk.ide.debug.variables.LaunchVariables;
import gov.redhawk.model.sca.RefreshDepth;
import gov.redhawk.model.sca.ScaAbstractProperty;
import gov.redhawk.model.sca.ScaComponent;
import gov.redhawk.model.sca.ScaConnection;
import gov.redhawk.model.sca.ScaPort;
import gov.redhawk.model.sca.ScaUsesPort;
import gov.redhawk.model.sca.ScaWaveform;
import gov.redhawk.model.sca.commands.ScaModelCommandWithResult;
import gov.redhawk.model.sca.impl.ScaComponentImpl;
import gov.redhawk.sca.efs.WrappedFileStore;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mil.jpeojtrs.sca.partitioning.PartitioningPackage;
import mil.jpeojtrs.sca.sad.ExternalPorts;
import mil.jpeojtrs.sca.sad.ExternalProperties;
import mil.jpeojtrs.sca.sad.ExternalProperty;
import mil.jpeojtrs.sca.sad.Port;
import mil.jpeojtrs.sca.sad.SadComponentInstantiation;
import mil.jpeojtrs.sca.sad.SadPackage;
import mil.jpeojtrs.sca.sad.SoftwareAssembly;
import mil.jpeojtrs.sca.spd.SoftPkg;
import mil.jpeojtrs.sca.util.AnyUtils;
import mil.jpeojtrs.sca.util.CFErrorFormatter;
import mil.jpeojtrs.sca.util.ScaEcoreUtils;
import mil.jpeojtrs.sca.util.ScaResourceFactoryUtil;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jacorb.naming.Name;
import org.jacorb.orb.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import CF.ApplicationOperations;
import CF.ComponentEnumType;
import CF.ComponentType;
import CF.DataType;
import CF.DeviceAssignmentType;
import CF.ErrorNumberType;
import CF.PortType;
import CF.PropertiesHolder;
import CF.Resource;
import CF.UnknownProperties;
import CF.ApplicationPackage.ComponentElementType;
import CF.ApplicationPackage.ComponentProcessIdType;
import CF.ExecutableDevicePackage.ExecuteFail;
import CF.LifeCyclePackage.InitializeError;
import CF.LifeCyclePackage.ReleaseError;
import CF.PortPackage.InvalidPort;
import CF.PortPackage.OccupiedPort;
import CF.PortSupplierPackage.UnknownPort;
import CF.PropertySetPackage.InvalidConfiguration;
import CF.PropertySetPackage.PartialConfiguration;
import CF.ResourcePackage.StartError;
import CF.ResourcePackage.StopError;
import CF.TestableObjectPackage.UnknownTest;

public class ApplicationImpl extends PlatformObject implements IProcess, ApplicationOperations, IAdaptable {

	private static interface ConnectionInfo {
		String getConnectionID();

		void disconnect();

		void reconnect(final ScaWaveform waveform) throws InvalidPort, OccupiedPort;
	}

	private class ScaComponentComparator implements Comparator<ScaComponent> {

		/**
		 * Compare on the start order as the first priority, if no start order is found, compare on the pointer location.
		 */
		@Override
		public int compare(ScaComponent o1, ScaComponent o2) {
			if (o1 == o2) {
				return 0;
			}
			if (o1 == assemblyController) {
				return -1;
			} else if (o2 == assemblyController) {
				return 1;
			} else {

				SadComponentInstantiation ci1 = o1.getComponentInstantiation();
				int o1Index = o1.eContainer().eContents().indexOf(o1);

				SadComponentInstantiation ci2 = o2.getComponentInstantiation();
				int o2Index = o2.eContainer().eContents().indexOf(o2);

				// If neither have start order we'll order them on list order.
				if (ci1 == null && ci2 == null) {
					return (o1Index < o2Index) ? -1 : 1;
				}

				// If c1 != null but ci2 is
				if (ci2 == null) {
					return -1;
				}

				// If c2 != null but ci1 is
				if (ci1 == null) {
					return 1;
				}

				// Neither ci1 or ci2 is null
				BigInteger s1 = ci1.getStartOrder();
				BigInteger s2 = ci2.getStartOrder();
				if (s1 != null) {
					return s1.compareTo(s2);
				} else if (s2 != null) {
					return 1;
				} else {
					return (o1Index < o2Index) ? -1 : 1;
				}
			}
		}
	}

	private class FromConnectionInfo implements ConnectionInfo {
		private final String connectionID;
		private final ScaUsesPort port;
		private final String targetCompID;
		private final String targetPort;
		private final ScaConnection connection;

		public FromConnectionInfo(final ScaUsesPort from, final String targetCompID, final String targetPort, final ScaConnection currentConnection) {
			super();
			this.port = from;
			this.targetCompID = targetCompID;
			this.targetPort = targetPort;
			this.connectionID = currentConnection.getId();
			this.connection = currentConnection;
		}

		@Override
		public void disconnect() {
			try {
				this.connection.getPort().disconnectPort(this.connection);
			} catch (final InvalidPort e) {
				String msg = "Problems while disconnecting connection " + this.connectionID;
				ApplicationImpl.this.streams.getErrStream().println(msg);
				ApplicationImpl.this.streams.getErrStream().println(CFErrorFormatter.format(e, "connection " + this.connectionID));
			}
		}

		@Override
		public void reconnect(final ScaWaveform waveform) throws InvalidPort, OccupiedPort {
			for (final ScaComponent comp : waveform.getComponents()) {
				if (comp.getInstantiationIdentifier().equals(this.targetCompID)) {
					if (this.targetPort == null) {
						this.port.connectPort(comp.getCorbaObj(), this.connectionID);
						return;
					}
				}
			}
		}

		@Override
		public String getConnectionID() {
			return this.connectionID;
		}
	}

	private class ToConnectionInfo implements ConnectionInfo {
		private final String connectionID;
		private final String sourceCompID;
		private final String sourcePort;
		private final org.omg.CORBA.Object target;
		private final ScaConnection connection;

		public ToConnectionInfo(final String sourceCompID, final String sourcePort, final org.omg.CORBA.Object target, final ScaConnection connection) {
			super();
			this.sourceCompID = sourceCompID;
			this.sourcePort = sourcePort;
			this.target = target;
			this.connectionID = connection.getId();
			this.connection = connection;
		}

		@Override
		public void reconnect(final ScaWaveform waveform) throws InvalidPort, OccupiedPort {
			for (final ScaComponent comp : waveform.getComponents()) {
				if (comp.getInstantiationIdentifier().equals(this.sourceCompID)) {
					final ScaPort< ? , ? > port = comp.getScaPort(this.sourcePort);
					if (port instanceof ScaUsesPort) {
						final ScaUsesPort uses = (ScaUsesPort) port;
						uses.connectPort(this.target, this.connectionID);
						return;
					}
				}
			}
		}

		@Override
		public void disconnect() {
			try {
				this.connection.getPort().disconnectPort(this.connection);
			} catch (final InvalidPort e) {
				String msg = "Problems while disconnecting connection " + this.connectionID;
				ApplicationImpl.this.streams.getErrStream().println(msg);
				ApplicationImpl.this.streams.getErrStream().println(CFErrorFormatter.format(e, "connection " + this.connectionID));
			}
		}

		@Override
		public String getConnectionID() {
			return this.connectionID;
		}
	}

	private static final EStructuralFeature[] ASSEMBLY_ID_PATH = new EStructuralFeature[] { SadPackage.Literals.SOFTWARE_ASSEMBLY__ASSEMBLY_CONTROLLER,
		SadPackage.Literals.ASSEMBLY_CONTROLLER__COMPONENT_INSTANTIATION_REF, PartitioningPackage.Literals.COMPONENT_INSTANTIATION_REF__REFID };
	private LocalScaComponent assemblyController;
	private NotifyingNamingContext waveformContext;
	private final ApplicationStreams streams = new ApplicationStreams();
	private boolean terminated;
	private final ILaunch parentLaunch;
	private final String assemblyID;
	private LocalScaWaveform waveform;
	private final String name;
	private final String identifier;
	private final String profile;
	private boolean started;

	public ApplicationImpl(final LocalScaWaveform waveform, final String identifier, final String name) {
		super();
		this.name = name;
		this.waveformContext = waveform.getNamingContext();
		this.identifier = identifier;
		this.parentLaunch = waveform.getLaunch();
		this.profile = waveform.getProfile();
		this.assemblyID = ApplicationImpl.getAssemblyControllerID(waveform.getProfileObj());
		this.waveform = waveform;
	}

	public static String getAssemblyControllerID(final SoftwareAssembly profileObj) {
		return ScaEcoreUtils.getFeature(profileObj, ApplicationImpl.ASSEMBLY_ID_PATH);
	}

	public LocalScaWaveform getLocalWaveform() {
		return this.waveform;
	}

	public NamingContextExt getWaveformContext() {
		return this.waveformContext.getNamingContext();
	}

	@Override
	public String identifier() {
		return this.identifier;
	}

	@Override
	public boolean started() {
		if (this.assemblyController != null) {
			return this.assemblyController.started();
		}
		return started;
	}

	public ApplicationStreams getStreams() {
		return this.streams;
	}

	@Override
	public void start() throws StartError {
		this.streams.getOutStream().println("Starting...");

		// Sort components
		List<ScaComponent> sortedSet;
		try {
			sortedSet = ScaModelCommandWithResult.runExclusive(waveform, new RunnableWithResult.Impl<List<ScaComponent>>() {
				public void run() {
					setResult(new ArrayList<ScaComponent>(waveform.getComponents()));
				}
			});
		} catch (InterruptedException e) {
			String msg = "Interrupted while getting waveform's components";
			ScaDebugPlugin.logError(msg, e);
			throw new StartError(ErrorNumberType.CF_EINTR, msg);
		}
		Collections.sort(sortedSet, new ScaComponentComparator());

		for (ScaComponent comp : sortedSet) {
			// With the exception of the assembly controller, don't stop things that have a component
			// instantiation but don't have a start order (i.e. they're defined in a SAD without a start order)
			if (comp != assemblyController && comp.getComponentInstantiation() != null && comp.getComponentInstantiation().getStartOrder() == null) {
				continue;
			}
			this.streams.getOutStream().println("\t" + comp.getInstantiationIdentifier());
			try {
				comp.start();
			} catch (final StartError e) {
				this.streams.getErrStream().println(CFErrorFormatter.format(e, "component " + comp.getName()));
				throw e;
			}
		}

		this.streams.getOutStream().println("Start succeeded");
		this.started = true;
	}

	@Override
	public void stop() throws StopError {
		this.streams.getOutStream().println("Stopping...");

		// Sort components
		List<ScaComponent> sortedSet;
		try {
			sortedSet = ScaModelCommandWithResult.runExclusive(waveform, new RunnableWithResult.Impl<List<ScaComponent>>() {
				public void run() {
					setResult(new ArrayList<ScaComponent>(waveform.getComponents()));
				}
			});
		} catch (InterruptedException e) {
			String msg = "Interrupted while getting waveform's components";
			ScaDebugPlugin.logError(msg, e);
			throw new StopError(ErrorNumberType.CF_EINTR, msg);
		}
		Collections.sort(sortedSet, new ScaComponentComparator());
		Collections.reverse(sortedSet);

		for (ScaComponent comp : sortedSet) {
			// With the exception of the assembly controller, don't stop things that have a component
			// instantiation but don't have a start order (i.e. they're defined in a SAD without a start order)
			if (comp != assemblyController && comp.getComponentInstantiation() != null && comp.getComponentInstantiation().getStartOrder() == null) {
				continue;
			}
			this.streams.getOutStream().println("\t" + comp.getInstantiationIdentifier());
			try {
				comp.stop();
			} catch (final StopError e) {
				this.streams.getErrStream().println(CFErrorFormatter.format(e, "component " + comp.getName()));
				throw e;
			}
		}
		this.streams.getOutStream().println("Stopped");
		this.started = false;
	}

	@Override
	public void initialize() throws InitializeError {
		this.streams.getOutStream().println("Initializing application...");
		if (this.assemblyController == null) {
			return;
		}
		try {
			this.assemblyController.initialize();
			this.streams.getOutStream().println("Initialize succeeded");
		} catch (final InitializeError e) {
			this.streams.getErrStream().println(CFErrorFormatter.format(e, "component " + this.assemblyController.getName()));
			throw e;
		}
	}

	@Override
	public void releaseObject() throws ReleaseError {
		if (this.terminated) {
			return;
		}
		this.terminated = true;
		this.streams.getOutStream().println("Releasing Application...");

		try {
			disconnectAll();

			releaseAll();

			unbind();
		} catch (Exception e) {
			String msg = "Problems while releasing";
			this.streams.getErrStream().println(msg);
			this.streams.getErrStream().println(e.toString());
			ScaDebugPlugin.logError(msg, e);
		}

		this.streams.getOutStream().println("Release finished");
		this.assemblyController = null;
		this.waveformContext = null;
		fireTerminated();
	}

	protected void unbind() {
		if (this.waveformContext != null) {

			if (this.waveformContext.eContainer() instanceof NotifyingNamingContext) {
				this.streams.getOutStream().println("Unbinding application");
				try {
					NotifyingNamingContext localSCANamingContext = (NotifyingNamingContext) this.waveformContext.eContainer();
					localSCANamingContext.unbind(Name.toName(this.name));
				} catch (NotFound e) {
					this.streams.getErrStream().println("Error while unbinding waveform:\n" + e);
				} catch (CannotProceed e) {
					this.streams.getErrStream().println("Error while unbinding waveform:\n" + e);
				} catch (InvalidName e) {
					this.streams.getErrStream().println("Error while unbinding waveform:\n" + e);
				}
			}
			ScaDebugPlugin debugInstance = ScaDebugPlugin.getInstance();
			if (debugInstance != null) {
				final NamingContextExt context = getWaveformContext();
				if (context != null) {
					try {
						context.unbind(Name.toName(this.name));
					} catch (final NotFound e) {
						this.streams.getErrStream().println("Error while unbinding waveform context:\n" + e);
					} catch (final CannotProceed e) {
						this.streams.getErrStream().println("Error while unbinding waveform context:\n" + e);
					} catch (final InvalidName e) {
						this.streams.getErrStream().println("Error while unbinding waveform context:\n" + e);
					} catch (final SystemException e) {
						this.streams.getErrStream().println("Error while unbinding waveform context:\n" + e);
					}
				}
			}
		}
	}

	protected void releaseAll() {
		this.streams.getOutStream().println("Releasing components...");
		// Shutdown each component
		for (final ScaComponent info : this.waveform.getComponents().toArray(new ScaComponent[this.waveform.getComponents().size()])) {
			release(info);
		}
		this.streams.getOutStream().println("Released components");
	}

	protected void release(final ScaComponent info) {
		this.streams.getOutStream().println("\tReleasing component " + info.getName());
		try {
			info.releaseObject();
		} catch (ReleaseError e) {
			String msg = "Problems while releasing component " + info.getName();
			this.streams.getErrStream().println(msg);
			this.streams.getErrStream().println(CFErrorFormatter.format(e, "component " + info.getName()));
		} catch (final Exception e) {
			String msg = "Problems while releasing component " + info.getName();
			this.streams.getErrStream().println(msg);
			ScaDebugPlugin.logError(msg, e);
		}
	}

	protected void disconnectAll() {
		this.streams.getOutStream().println("Disconnecting connections...");
		// Disconnect components
		for (final ScaComponent info : this.waveform.getComponents().toArray(new ScaComponent[waveform.getComponents().size()])) {
			disconnect(info);
		}
		this.streams.getOutStream().println("Disconnected");
	}

	protected void disconnect(final ScaComponent comp) {
		for (final ScaPort< ? , ? > port : comp.getPorts().toArray(new ScaPort< ? , ? >[comp.getPorts().size()])) {
			if (port instanceof ScaUsesPort) {
				final ScaUsesPort up = (ScaUsesPort) port;
				final ScaConnection[] connections = up.getConnections().toArray(new ScaConnection[up.getConnections().size()]);
				for (final ScaConnection c : connections) {
					try {
						up.disconnectPort(c);
					} catch (final InvalidPort e) {
						String msg = "Problems while disconnecting connection " + c.getId();
						this.streams.getErrStream().println(msg);
						this.streams.getErrStream().println(CFErrorFormatter.format(e, "connection " + c.getId()));
					} catch (final Exception e) {
						String msg = "Problems while disconnecting connection " + c.getId();
						this.streams.getErrStream().println(msg);
						ScaDebugPlugin.logError(msg, e);
					}
				}
			}
		}
	}

	private void fireTerminated() {
		DebugPlugin plugin = DebugPlugin.getDefault();
		if (plugin != null) {
			plugin.fireDebugEventSet(new DebugEvent[] { new DebugEvent(this, DebugEvent.TERMINATE) });
		}
	}

	@Override
	public void runTest(final int testid, final PropertiesHolder testValues) throws UnknownTest, UnknownProperties {
		this.streams.getOutStream().println("Runing Test: " + testValues);
		if (this.assemblyController == null) {
			return;
		}
		try {
			this.assemblyController.runTest(testid, testValues);
			this.streams.getOutStream().println("Run Test Succeeded");
		} catch (final UnknownTest e) {
			this.streams.getErrStream().println(CFErrorFormatter.format(e, "component " + this.assemblyController.getName()));
			throw e;
		} catch (final UnknownProperties e) {
			this.streams.getErrStream().println(CFErrorFormatter.format(e, "component " + this.assemblyController.getName()));
			throw e;
		}
	}

	@Override
	public void configure(final DataType[] configProperties) throws InvalidConfiguration, PartialConfiguration {
		this.streams.getOutStream().println("Configuring application: ");
		for (DataType t : configProperties) {
			streams.getOutStream().println("\n\t" + LocalApplicationFactory.toString(t));
		}

		Map<String, List<DataType>> configMap = new HashMap<String, List<DataType>>();
		ExternalProperties externalProperties = waveform.getProfileObj().getExternalProperties();
		for (DataType t : configProperties) {
			boolean isExternalProp = false;
			if (externalProperties != null) {
				for (ExternalProperty p : externalProperties.getProperties()) {
					String propId = (p.getExternalPropID() != null) ? p.getExternalPropID() : p.getPropID();
					if (propId.equals(t.id)) {
						List<DataType> configList = configMap.get(p.getCompRefID());
						if (configList == null) {
							configList = new ArrayList<DataType>();
							configMap.put(p.getCompRefID(), configList);
						}
						configList.add(t);
						isExternalProp = true;
						break;
					}
				}
			}
			if (!isExternalProp) {
				List<DataType> configList = configMap.get(null);
				if (configList == null) {
					configList = new ArrayList<DataType>();
					configMap.put(null, configList);
				}
				configList.add(t);
			}
		}

		for (Map.Entry<String, List<DataType>> entry : configMap.entrySet()) {
			ScaComponent inst;
			if (entry.getKey() == null) {
				inst = waveform.getAssemblyController();
			} else {
				inst = waveform.getScaComponent(entry.getKey());
			}
			if (inst != null) {
				inst.configure(entry.getValue().toArray(new DataType[entry.getValue().size()]));
			}
		}
	}

	@Override
	public void query(final PropertiesHolder configProperties) throws UnknownProperties {
		Set<String> queryProperties = new HashSet<String>();
		if (configProperties.value != null) {
			for (DataType t : configProperties.value) {
				queryProperties.add(t.id);
			}
		}
		this.streams.getOutStream().println("Query: " + queryProperties);

		ExternalProperties externalProperties = this.waveform.getProfileObj().getExternalProperties();
		List<DataType> retVal = new ArrayList<DataType>();

		if (this.assemblyController != null) {
			PropertiesHolder tmpHolder = new PropertiesHolder();
			if (!queryProperties.isEmpty()) {
				List<DataType> propsToQuery = new ArrayList<DataType>();
				for (ScaAbstractProperty< ? > p : assemblyController.getProperties()) {
					if (queryProperties.contains(p.getId())) {
						propsToQuery.add(new DataType(p.getId(), ORB.init().create_any()));
					}
				}
				tmpHolder.value = propsToQuery.toArray(new DataType[propsToQuery.size()]);
			}
			this.assemblyController.query(tmpHolder);
			retVal.addAll(Arrays.asList(tmpHolder.value));
		}
		if (externalProperties != null) {
			for (ExternalProperty prop : externalProperties.getProperties()) {
				String propId = (prop.getExternalPropID() != null) ? prop.getExternalPropID() : prop.getPropID();
				if (queryProperties.isEmpty() || queryProperties.contains(propId)) {
					PropertiesHolder tmpHolder = new PropertiesHolder();
					tmpHolder.value = new DataType[] { new DataType(prop.getPropID(), ORB.init().create_any()) };
					ScaComponent component = waveform.getScaComponent(prop.getCompRefID());
					if (component != null) {
						component.query(tmpHolder);
						retVal.addAll(Arrays.asList(tmpHolder.value));
					}
				}
			}
		}
		configProperties.value = retVal.toArray(new DataType[retVal.size()]);
	}

	@Override
	public org.omg.CORBA.Object getPort(final String name) throws UnknownPort {
		getStreamsProxy().getOutStream().println("Get port " + name);
		try {
			if (name == null) {
				throw new UnknownPort("No external port of name: " + name);
			}
			ExternalPorts externalPorts = waveform.getProfileObj().getExternalPorts();
			if (externalPorts != null) {
				for (final Port p : externalPorts.getPort()) {
					if (name.equals(p.getProvidesIndentifier()) || name.equals(p.getUsesIdentifier()) || name.equals(p.getExternalName())) {
						final ScaComponent comp = findComponent(p.getComponentInstantiationRef().getRefid());
						if (comp != null) {
							String portName;
							if (p.getProvidesIndentifier() != null) {
								portName = p.getProvidesIndentifier();
							} else {
								portName = p.getUsesIdentifier();
							}
							return comp.getPort(portName);
						}
					} else if (name.equals(p.getSupportedIdentifier())) {
						final ScaComponent comp = findComponent(p.getComponentInstantiationRef().getRefid());
						if (comp != null) {
							return comp.getCorbaObj();
						}
					}
				}
			}
		} catch (final UnknownPort e) {
			this.streams.getErrStream().println(CFErrorFormatter.format(e, "port " + name));
			throw e;
		}
		throw new UnknownPort("No external port of name: " + name);
	}

	private ScaComponent findComponent(final String instId) {
		return waveform.getScaComponent(instId);
	}

	@Override
	public ComponentType[] registeredComponents() {
		final ComponentType[] types = new ComponentType[this.waveform.getComponents().size()];
		for (int i = 0; i < types.length; i++) {
			types[i] = new ComponentType();
			types[i].componentObject = this.waveform.getComponents().get(i).getCorbaObj();
			types[i].identifier = this.waveform.getComponents().get(i).getIdentifier();
			types[i].softwareProfile = this.waveform.getComponents().get(i).getProfileObj().eResource().getURI().path();
			types[i].providesPorts = new PortType[0];
			types[i].type = ComponentEnumType.APPLICATION_COMPONENT;
		}
		return types;
	}

	@Override
	public ComponentElementType[] componentNamingContexts() {
		final ComponentElementType[] types = new ComponentElementType[this.waveform.getComponents().size()];
		for (int i = 0; i < types.length; i++) {
			types[i] = new ComponentElementType();
			types[i].componentId = this.waveform.getComponents().get(i).getIdentifier();
			types[i].elementId = this.name + "/" + this.waveform.getComponents().get(i).getName();
		}
		return types;
	}

	@Override
	public ComponentProcessIdType[] componentProcessIds() {
		return new ComponentProcessIdType[0];
	}

	@Override
	public DeviceAssignmentType[] componentDevices() {
		return new DeviceAssignmentType[0];
	}

	@Override
	public ComponentElementType[] componentImplementations() {
		final ComponentElementType[] types = new ComponentElementType[this.waveform.getComponents().size()];
		for (int i = 0; i < types.length; i++) {
			final ScaComponent comp = this.waveform.getComponents().get(i);

			types[i] = new ComponentElementType();
			types[i].componentId = comp.getIdentifier();
			if (comp instanceof LocalScaComponent) {
				types[i].elementId = ((LocalScaComponent) comp).getImplementationID();
			} else {
				types[i].elementId = "";
			}
			if (types[i].elementId == null) {
				types[i].elementId = "";
			}
		}
		return types;
	}

	@Override
	public String profile() {
		return this.profile;
	}

	@Override
	public String name() {
		return this.name;
	}

	@Override
	public boolean canTerminate() {
		return !this.terminated;
	}

	@Override
	public boolean isTerminated() {
		return this.terminated;
	}

	@Override
	public void terminate() throws DebugException {
		try {
			releaseObject();
		} catch (final ReleaseError e) {
			throw new DebugException(new Status(IStatus.WARNING, ScaDebugPlugin.ID, CFErrorFormatter.format(e, "waveform " + this.name), e));
		}
	}

	@Override
	public String getLabel() {
		return this.name;
	}

	@Override
	public ILaunch getLaunch() {
		return this.parentLaunch;
	}

	@Override
	public ApplicationStreams getStreamsProxy() {
		return this.streams;
	}

	@Override
	public void setAttribute(final String key, final String value) {
		// No Attributes
	}

	@Override
	public String getAttribute(final String key) {
		// No Attributes
		return null;
	}

	@Override
	public int getExitValue() throws DebugException {
		if (!this.terminated) {
			throw new DebugException(new Status(IStatus.ERROR, ScaDebugPlugin.ID, "Application not terminated", null));
		}
		return 0;
	}

	public Resource reset(final String compInstId) throws ReleaseError, ExecuteFail {
		if (compInstId == null) {
			throw new ReleaseError(new String[] { "Unknown component: " + compInstId });
		}
		this.streams.getOutStream().println("Resetting component " + compInstId);

		LocalScaComponent oldComponent = null;
		for (final Iterator<ScaComponent> iterator = this.waveform.getComponents().iterator(); iterator.hasNext();) {
			final ScaComponent info = iterator.next();
			if (compInstId.equals(info.getInstantiationIdentifier())) {
				if (!(info instanceof LocalScaComponent)) {
					// This should never happen but check for it anyway
					String msg = "Can only reset local components";
					this.streams.getErrStream().println(msg);
					throw new ReleaseError(new String[] { msg });
				}
				oldComponent = (LocalScaComponent) info;
				break;
			}
		}

		if (oldComponent == null) {
			this.streams.getErrStream().println("No component " + compInstId);
			throw new ReleaseError(new String[] { "Unknown component: " + compInstId });
		}
		final String usageName = oldComponent.getName();
		String instId = oldComponent.getIdentifier();
		final String execParams = oldComponent.getExecParam();
		final URI spdUri = oldComponent.getProfileURI();
		
		if (spdUri == null) {
			this.streams.getErrStream().println("No SPD URI component: " + compInstId);
			throw new ReleaseError(new String[] { "No SPD URI component: " + compInstId });
		}
		
		final String implId = oldComponent.getImplementationID();
		final String mode = oldComponent.getMode();
		final List<ConnectionInfo> oldConnections = getConnectionInfo(oldComponent);

		disconnect(oldConnections);
		release(oldComponent);
		if (oldComponent.getLaunch() != null && oldComponent.getLaunch().canTerminate()) {
			try {
				oldComponent.getLaunch().terminate();
			} catch (final DebugException e) {
				// PASS
			}
		}
		while (!oldComponent.isDisposed()) {
			try {
				Thread.sleep(500);
			} catch (final InterruptedException e) {
				// PASS
			}
		}
		try {
			final LocalScaComponent retVal = launch(usageName, instId, execParams, spdUri, implId, mode);
			this.streams.getOutStream().println("Reconnecting component");
			makeConnections(oldConnections);
			this.streams.getOutStream().println("Done resetting component " + oldComponent.getName());
			return retVal.getObj();
		} catch (final CoreException e) {
			this.streams.getErrStream().println("Failed to reset component " + usageName);
			this.streams.getErrStream().println(e.toString());
			throw new ExecuteFail(ErrorNumberType.CF_EFAULT, e.getStatus().getMessage());
		}
	}

	private void disconnect(final List<ConnectionInfo> oldConnections) {
		for (final ConnectionInfo info : oldConnections) {
			info.disconnect();
		}
	}

	private void makeConnections(final List<ConnectionInfo> connections) {
		try {
			this.waveform.refresh(null, RefreshDepth.FULL);
			for (final ConnectionInfo info : connections) {
				try {
					info.reconnect(this.waveform);
				} catch (final InvalidPort e) {
					this.streams.getErrStream().println("Failed to reconnect connection " + info.getConnectionID());
					this.streams.getErrStream().println(CFErrorFormatter.format(e, "connection " + info.getConnectionID()));
				} catch (final OccupiedPort e) {
					this.streams.getErrStream().println("Failed to reconnect connection " + info.getConnectionID());
					this.streams.getErrStream().println(CFErrorFormatter.format(e, "connection " + info.getConnectionID()));
				}
			}
		} catch (final InterruptedException e) {
			this.streams.getErrStream().println("Failed to reconnect connections");
			ScaDebugPlugin.logError("Interrupted while refreshing waveform", e);
		}
	}

	private List<ConnectionInfo> getConnectionInfo(final LocalScaComponent oldComponent) {
		final List<ConnectionInfo> retVal = new ArrayList<ApplicationImpl.ConnectionInfo>();

		// Create list of connections that connect to me
		for (final ScaComponent comp : this.waveform.getComponents()) {
			if (comp != oldComponent) {
				for (final ScaPort< ? , ? > port : comp.getPorts()) {
					if (port instanceof ScaUsesPort) {
						final ScaUsesPort uses = (ScaUsesPort) port;
						for (final ScaConnection conn : uses.getConnections()) {
							if (oldComponent.getObj()._is_equivalent(conn.getData().port)) {
								retVal.add(new FromConnectionInfo(uses, oldComponent.getInstantiationIdentifier(), null, conn));
							} else {
								for (final ScaPort< ? , ? > targetPort : oldComponent.getPorts()) {
									if (targetPort.getObj()._is_equivalent(conn.getData().port)) {
										retVal.add(new FromConnectionInfo(uses, oldComponent.getInstantiationIdentifier(), targetPort.getName(), conn));
									}
								}
							}
						}
					}
				}
			}
		}

		// Create list of connections that I connect to
		for (final ScaPort< ? , ? > port : oldComponent.getPorts()) {
			if (port instanceof ScaUsesPort) {
				final ScaUsesPort uses = (ScaUsesPort) port;
				for (final ScaConnection conn : uses.getConnections()) {
					retVal.add(new ToConnectionInfo(oldComponent.getInstantiationIdentifier(), uses.getName(), conn.getData().port, conn));
				}
			}
		}

		return retVal;
	}

	public Resource launch(final String compId, final DataType[] execParams, @NonNull final String spdURI, final String implId, final String mode)
		throws ExecuteFail {
		Assert.isNotNull(spdURI, "SPD URI must not be null");
		LocalScaComponent retVal;
		try {
			URI uri = URI.createURI(spdURI);
			if (uri == null) {
				throw new NullPointerException();
			}
			retVal = launch(null, compId, createExecParamStr(execParams), uri, implId, mode);
		} catch (final CoreException e) {
			this.getStreams().getErrStream().println("Failed to launch component " + compId);
			this.getStreams().getErrStream().println(e.toString());
			throw new ExecuteFail(ErrorNumberType.CF_EFAULT, e.getStatus().getMessage());
		}
		return retVal.getObj();
	}

	@NonNull
	public LocalScaComponent launch(final String usageName, String compId, final DataType[] execParams, @NonNull final URI spdURI, final String implId,
		final String mode) throws CoreException {
		return launch(usageName, compId, createExecParamStr(execParams), spdURI, implId, mode);
	}

	public LocalScaComponent launch(final String usageName, final DataType[] execParams, @NonNull final URI spdURI, final String implId, final String mode)
		throws CoreException {
		return launch(usageName, null, execParams, spdURI, implId, mode);
	}

	@NonNull
	public LocalScaComponent launch(@Nullable String usageName, @Nullable final String compId, @Nullable final String execParams, @NonNull URI spdURI,
		@Nullable final String implId, @Nullable String mode) throws CoreException {
		Assert.isNotNull(spdURI, "SPD URI must not be null");
		final ResourceSet resourceSet = ScaResourceFactoryUtil.createResourceSet();
		if (!spdURI.isPlatform()) {
			IFileStore store = EFS.getStore(java.net.URI.create(spdURI.toString()));
			IFileStore unwrappedStore = WrappedFileStore.unwrap(store);
			if (unwrappedStore != null) {
				URI tmp = URI.createURI(unwrappedStore.toURI().toString());
				if (tmp == null) {
					throw new NullPointerException();
				}
				spdURI = tmp;
			}
		}

		final SoftPkg spd = SoftPkg.Util.getSoftPkg(resourceSet.getResource(spdURI, true));
		if (mode == null) {
			mode = ILaunchManager.RUN_MODE;
		}

		this.streams.getOutStream().println("Launching component: " + spd.getName());
		final ILaunchConfigurationFactory factory = ScaDebugPlugin.getInstance().getLaunchConfigurationFactoryRegistry().getFactory(spd, implId);
		if (factory == null) {
			throw new CoreException(new Status(IStatus.ERROR, ScaDebugPlugin.ID, "Failed to obtain Launch Factory for impl: " + implId, null));
		}
		final ILaunchConfigurationWorkingCopy config = factory.createLaunchConfiguration(spd.getName(), implId, spd);

		final NameComponent[] spdContextName = this.waveformContext.getName(spdURI);
		final String spdContextNameStr;
		try {
			spdContextNameStr = Name.toString(spdContextName);
		} catch (final InvalidName e) {
			throw new CoreException(new Status(IStatus.ERROR, ScaDebugPlugin.ID, "Failed to create sub context for spd.", e));
		}

		NamingContext spdContext;
		try {
			spdContext = this.waveformContext.bind_new_context(spdContextName);
		} catch (final NotFound e) {
			throw new CoreException(new Status(IStatus.ERROR, ScaDebugPlugin.ID, "Failed to create sub context for spd.", e));
		} catch (final AlreadyBound e) {
			try {
				spdContext = NamingContextHelper.narrow(this.waveformContext.resolve(spdContextName));
			} catch (final NotFound e1) {
				throw new CoreException(new Status(IStatus.ERROR, ScaDebugPlugin.ID, "Failed to create sub context for spd: " + spdContextNameStr, e1));
			} catch (final CannotProceed e1) {
				throw new CoreException(new Status(IStatus.ERROR, ScaDebugPlugin.ID, "Failed to create sub context for spd: " + spdContextNameStr, e1));
			} catch (final InvalidName e1) {
				throw new CoreException(new Status(IStatus.ERROR, ScaDebugPlugin.ID, "Failed to create sub context for spd: " + spdContextNameStr, e1));
			}
		} catch (final CannotProceed e) {
			throw new CoreException(new Status(IStatus.ERROR, ScaDebugPlugin.ID, "Failed to create sub context for spd: " + spdContextNameStr, e));
		} catch (final InvalidName e) {
			throw new CoreException(new Status(IStatus.ERROR, ScaDebugPlugin.ID, "Failed to create sub context for spd: " + spdContextNameStr, e));
		}
		config.setAttribute(LaunchVariables.NAMING_CONTEXT_IOR, spdContext.toString());

		if (usageName == null && compId != null) {
			usageName = ScaComponentImpl.convertIdentifierToInstantiationID(compId);
		}

		if (usageName != null) {
			this.streams.getOutStream().println("\tLaunching with name: " + usageName);
			config.setAttribute(LaunchVariables.NAME_BINDING, usageName);
		}
		if (compId != null) {
			this.streams.getOutStream().println("\tLaunching with id: " + compId);
			config.setAttribute(LaunchVariables.COMPONENT_IDENTIFIER, compId);
		}

		if (execParams != null && execParams.length() > 0) {
			this.streams.getOutStream().println("\tExec params: " + execParams);
			config.setAttribute(LaunchVariables.EXEC_PARAMS, execParams);
		} else {
			this.streams.getOutStream().println("\tUsing default exec params.");
		}

		this.streams.getOutStream().println("\tCalling launch on configuration...");
		final ILaunch subLaunch = config.launch(mode, new NullProgressMonitor(), false);
		this.streams.getOutStream().println("\tLaunch configuration succeeded.");

		LocalScaComponent newComponent = null;
		final String newCompId = subLaunch.getAttribute(LaunchVariables.COMPONENT_IDENTIFIER);
		if (newCompId != null) {
			for (final ScaComponent comp : ApplicationImpl.this.waveform.getComponents()) {
				comp.fetchAttributes(null);
				final String id = comp.getIdentifier();
				if (id.equals(newCompId)) {
					newComponent = (LocalScaComponent) comp;
					break;
				}
			}
		}

		if (newComponent == null) {
			subLaunch.terminate();
			throw new CoreException(new Status(IStatus.ERROR, ScaDebugPlugin.ID, "Failed to find component after launch", null));
		} else {
			newComponent.setDataProvidersEnabled(false);
		}

		// Add Child processes
		for (final IProcess process : subLaunch.getProcesses()) {
			if (this.parentLaunch != null) {
				this.parentLaunch.addProcess(process);
			}
		}

		if (this.assemblyID != null && this.assemblyID.equals(newCompId)) {
			this.assemblyController = newComponent;
		}
		return newComponent;
	}

	private String createExecParamStr(final DataType[] execParams) {
		if (execParams == null || execParams.length == 0) {
			return "";
		}
		final Map<String, Object> map = new HashMap<String, Object>(execParams.length);
		for (final DataType t : execParams) {
			map.put(t.id, AnyUtils.convertAny(t.value));
		}
		return SpdLauncherUtil.createExecParamString(map);
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

}
