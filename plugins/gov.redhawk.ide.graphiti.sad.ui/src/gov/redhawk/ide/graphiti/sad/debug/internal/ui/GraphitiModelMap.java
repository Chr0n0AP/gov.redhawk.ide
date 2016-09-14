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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gov.redhawk.core.graphiti.ui.editor.AbstractGraphitiMultiPageEditor;
import gov.redhawk.ide.graphiti.internal.ui.AbstractGraphitiModelMap;
import gov.redhawk.ide.graphiti.internal.ui.CreateComponentInstantiationCommand;
import gov.redhawk.ide.graphiti.sad.ui.SADUIGraphitiPlugin;
import gov.redhawk.ide.graphiti.sad.ui.diagram.features.create.ComponentCreateFeature;
import gov.redhawk.model.sca.CorbaObjWrapper;
import gov.redhawk.model.sca.ScaComponent;
import gov.redhawk.model.sca.ScaConnection;
import gov.redhawk.model.sca.ScaPort;
import gov.redhawk.model.sca.ScaPortContainer;
import gov.redhawk.model.sca.ScaPropertyContainer;
import gov.redhawk.model.sca.ScaProvidesPort;
import gov.redhawk.model.sca.ScaWaveform;
import gov.redhawk.model.sca.util.ReleaseJob;
import gov.redhawk.sca.util.SubMonitor;
import mil.jpeojtrs.sca.partitioning.ComponentInstantiation;
import mil.jpeojtrs.sca.partitioning.ConnectionTarget;
import mil.jpeojtrs.sca.partitioning.ProvidesPortStub;
import mil.jpeojtrs.sca.partitioning.UsesPortStub;
import mil.jpeojtrs.sca.sad.SadComponentInstantiation;
import mil.jpeojtrs.sca.spd.SoftPkg;

public class GraphitiModelMap extends AbstractGraphitiModelMap {

	private final ScaWaveform waveform;

	/**
	 * Associates {@link ScaComponent}s with their {@link SadComponentInstantiation}.
	 */
	private final Map<String, NodeMapEntry> nodes = Collections.synchronizedMap(new HashMap<String, NodeMapEntry>());

	public GraphitiModelMap(@NonNull final AbstractGraphitiMultiPageEditor editor, @NonNull final ScaWaveform waveform) {
		super(editor);
		Assert.isNotNull(waveform, "Sandbox Waveform must not be null");
		this.waveform = waveform;
	}

	public ScaWaveform getWaveform() {
		return waveform;
	}

	protected Map<String, NodeMapEntry> getNodes() {
		return nodes;
	}

	////////////////////////////////////////////////////
	// Components section
	////////////////////////////////////////////////////

	/**
	 * Called when a new {@link ScaComponent} is added to the SCA model. Asynchronously updates the diagram.
	 * @param comp
	 */
	public void add(@NonNull final ScaComponent comp) {
		final NodeMapEntry nodeMapEntry = new NodeMapEntry();
		nodeMapEntry.setScaComponent(comp);
		synchronized (nodes) {
			if (nodes.get(nodeMapEntry.getKey()) != null) {
				return;
			} else {
				nodes.put(nodeMapEntry.getKey(), nodeMapEntry);
			}
		}
		Job job = new Job("Adding component: " + comp.getInstantiationIdentifier()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
				SadComponentInstantiation newComp = null;
				try {
					newComp = (SadComponentInstantiation) GraphitiModelMap.this.create(comp, subMonitor.newChild(1));
					nodeMapEntry.setProfile(newComp);
					return Status.OK_STATUS;
				} catch (CoreException e) {
					nodes.remove(nodeMapEntry.getKey());
					return new Status(IStatus.ERROR, SADUIGraphitiPlugin.PLUGIN_ID, "Failed to add component " + comp.getInstantiationIdentifier(), e);
				} finally {
					if (nodes.get(nodeMapEntry.getKey()) == null) {
						delete(newComp);
					}
					subMonitor.done();
				}
			}

		};
		job.schedule();
	}

	/**
	 * Called when a new {@link SadComponentInstantiation} is added to the diagram's XML model. Asynchronously launches
	 * the component.
	 * @param compInst
	 */
	public void add(@NonNull final SadComponentInstantiation compInst) {
		// Launching not supported by this class
		throw new IllegalStateException("Launching components is not permitted by this class");
	}

	protected CreateComponentInstantiationCommand createComponentInstantiationCommand(TransactionalEditingDomain editingDomain,
		final IFeatureProvider featureProvider, ScaPropertyContainer< ? , SoftPkg> newObject) {
		final ScaComponent newComponent = (ScaComponent) newObject;
		return new CreateComponentInstantiationCommand(editingDomain) {

			private ComponentInstantiation compInst;

			@Override
			public ComponentInstantiation getComponentInstantiation() {
				return compInst;
			}

			@Override
			protected void doExecute() {
				ComponentCreateFeature createComponentFeature = new ComponentCreateFeature(featureProvider, newComponent.getProfileObj(), null);
				CreateContext createContext = new CreateContext();
				createContext.putProperty(ComponentCreateFeature.OVERRIDE_USAGE_NAME, newComponent.getName());
				createContext.putProperty(ComponentCreateFeature.OVERRIDE_INSTANTIATION_ID, newComponent.getInstantiationIdentifier());
				createContext.setTargetContainer(featureProvider.getDiagramTypeProvider().getDiagram());
				final Object[] objects = createComponentFeature.create(createContext);
				compInst = (ComponentInstantiation) objects[0];
			}
		};
	}

	protected UsesPortStub findSource(ScaConnection newValue) {
		final SadComponentInstantiation sourceComponent = getComponentInstantiation((ScaComponent) newValue.getPort().eContainer());
		if (sourceComponent == null) {
			return null;
		}
		for (final UsesPortStub stub : sourceComponent.getUses()) {
			if (stub.getName() != null && stub.getName().equals(newValue.getPort().getName())) {
				return stub;
			}
		}
		return null;
	}

	protected ConnectionTarget findTarget(ScaConnection newValue) {
		// Iterate port containers looking for a provides ports which may match
		List<ScaComponent> components = this.waveform.getComponentsCopy();
		for (final ScaComponent portContainer : components) {
			if (!portContainer.isSetPorts()) {
				portContainer.fetchPorts(null);
			}
			for (final ScaPort< ? , ? > port : portContainer.getPorts()) {
				if (port instanceof ScaProvidesPort && port.getObj()._is_equivalent(newValue.getData().port)) {
					final SadComponentInstantiation compInst = getComponentInstantiation(portContainer);
					if (compInst == null) {
						continue;
					}
					for (final ProvidesPortStub provides : compInst.getProvides()) {
						if (provides.getName().equals(port.getName())) {
							return provides;
						}
					}
				}
			}
		}

		// Iterate anything that could be a component supported interface looking for a match
		for (final ScaComponent csiTarget : components) {
			if (csiTarget.getObj()._is_equivalent(newValue.getData().port)) {
				SadComponentInstantiation compInst = getComponentInstantiation(csiTarget);
				if (compInst != null) {
					return compInst.getInterfaceStub();
				}
				break;
			}
		}

		return null;
	}

	@Nullable
	/* package */ SadComponentInstantiation getComponentInstantiation(@Nullable final ScaComponent comp) {
		if (comp == null) {
			return null;
		}
		NodeMapEntry nodeMapEntry = nodes.get(NodeMapEntry.getKey(comp));
		if (nodeMapEntry != null) {
			return nodeMapEntry.getProfile();
		} else {
			return null;
		}
	}

	@Nullable
	private ScaComponent get(@Nullable final ComponentInstantiation compInst) {
		if (compInst == null) {
			return null;
		}
		NodeMapEntry nodeMapEntry = nodes.get(NodeMapEntry.getKey(compInst));
		if (nodeMapEntry != null) {
			return nodeMapEntry.getScaComponent();
		} else {
			return null;
		}
	}

	protected CorbaObjWrapper< ? > getCorbaObjWrapper(ComponentInstantiation compInst) {
		return get(compInst);
	}

	protected ScaPortContainer getPortContainer(ComponentInstantiation portContainer) {
		return get(portContainer);
	}

	/**
	 * Adds a mapping between the SCA model object and diagram XML model object
	 * @param comp
	 * @param inst
	 */
	public void put(@NonNull ScaComponent comp, @NonNull SadComponentInstantiation inst) {
		NodeMapEntry nodeMapEntry = new NodeMapEntry(comp, inst);
		nodes.put(nodeMapEntry.getKey(), nodeMapEntry);
	}

	/**
	 * Called when an existing {@link ScaComponent} is removed from the SCA model. Removes the PictogramElement from the
	 * diagram.
	 * @param comp
	 */
	public void remove(@NonNull final ScaComponent comp) {

		final NodeMapEntry nodeMapEntry = nodes.remove(NodeMapEntry.getKey(comp));
		if (nodeMapEntry == null) {
			return;
		}
		final SadComponentInstantiation oldComp = nodeMapEntry.getProfile();
		if (oldComp != null) {
			delete(oldComp);
		}
	}

	/**
	 * Called when a {@link SadComponentInstantiation} is removed from the diagram's XML model. Asynchronously triggers
	 * a call to <code>releaseObject()</code>.
	 * @param compInst
	 */
	public void remove(final SadComponentInstantiation compInst) {
		if (compInst == null) {
			return;
		}

		final NodeMapEntry nodeMapEntry = nodes.remove(NodeMapEntry.getKey(compInst));
		if (nodeMapEntry == null) {
			return;
		}
		final ScaComponent oldComp = nodeMapEntry.getScaComponent();
		if (oldComp != null) {
			Job releaseJob = new ReleaseJob(oldComp);
			releaseJob.schedule();
		}
	}

	////////////////////////////////////////////////////
	// Reflecting status
	////////////////////////////////////////////////////

	/**
	 * Updates the pictogram element's start/stop state.
	 * @param scaComponent
	 * @param started
	 */
	public void startStopComponent(ScaComponent scaComponent, final Boolean started) {
		final boolean resolveStarted = (started == null) ? false : started;
		final NodeMapEntry nodeMapEntry = nodes.get(NodeMapEntry.getKey(scaComponent));
		if (nodeMapEntry == null) {
			return;
		}
		final ComponentInstantiation componentInstantiation = nodeMapEntry.getProfile();
		updateStateStopState(componentInstantiation, resolveStarted);
	}

	/**
	 * Updates pictogram elements to reflect runtime status.
	 */
	public void reflectRuntimeStatus() {
		synchronized (nodes) {
			for (String nodeKey : nodes.keySet()) {
				final NodeMapEntry nodeMapEntry = nodes.get(nodeKey);
				ScaComponent component = nodeMapEntry.getScaComponent();
				if (component == null) {
					updateEnabledState(nodeMapEntry.getProfile(), false);
				} else {
					updateEnabledState(nodeMapEntry.getProfile(), true);
					startStopComponent(component, component.getStarted());
				}
			}
		}
	}

	/**
	 * Updates the pictogram element's {@link IStatus}.
	 * @param scaComponent
	 * @param status
	 */
	public void reflectErrorState(ScaComponent scaComponent, final IStatus status) {
		final NodeMapEntry nodeMapEntry = nodes.get(NodeMapEntry.getKey(scaComponent));
		if (nodeMapEntry == null) {
			return;
		}
		final ComponentInstantiation componentInstantiation = nodeMapEntry.getProfile();
		updateErrorState(componentInstantiation, status);
	}
}
