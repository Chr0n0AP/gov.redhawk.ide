/**
 * This file is protected by Copyright. 
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 * 
 * This file is part of REDHAWK IDE.
 * 
 * All rights reserved.  This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 *
 */
package gov.redhawk.ide.graphiti.sad.ui.diagram.features.custom.runtime;

import gov.redhawk.ide.debug.LocalLaunch;
import gov.redhawk.ide.debug.LocalScaWaveform;
import gov.redhawk.ide.debug.ScaDebugPlugin;
import gov.redhawk.ide.debug.SpdLauncherUtil;
import gov.redhawk.ide.graphiti.sad.ext.impl.ComponentShapeImpl;
import gov.redhawk.ide.graphiti.sad.ui.diagram.patterns.ComponentPattern;
import gov.redhawk.ide.graphiti.ui.adapters.GraphitiAdapterUtil;
import gov.redhawk.ide.graphiti.ui.diagram.features.custom.NonUndoableCustomFeature;
import gov.redhawk.ide.graphiti.ui.diagram.util.DUtil;
import gov.redhawk.model.sca.ScaComponent;
import mil.jpeojtrs.sca.sad.SadComponentInstantiation;
import mil.jpeojtrs.sca.sad.SoftwareAssembly;

import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalCommandStack;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IRemoveFeature;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.IRemoveContext;
import org.eclipse.graphiti.features.context.impl.RemoveContext;

public class TerminateComponentFeature extends NonUndoableCustomFeature {

	public TerminateComponentFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public String getName() {
		return "Terminate";
	}

	@Override
	public String getDescription() {
		return "Hard terminate of the component from the model";
	}

	@Override
	public boolean canExecute(ICustomContext context) {
		if (context.getPictogramElements()[0] instanceof ComponentShapeImpl) {
			return true;
		}
		return false;
	}

	@Override
	public void execute(ICustomContext context) {
		// Manually terminate the component process
		final ComponentShapeImpl componentShape = (ComponentShapeImpl) context.getPictogramElements()[0];
		SadComponentInstantiation ci = (SadComponentInstantiation) DUtil.getBusinessObject(componentShape);
		LocalLaunch localLaunch = null;
		if (ci != null) {
			LocalScaWaveform waveform = ScaDebugPlugin.getInstance().getLocalSca().getSandboxWaveform();
			final String myId = ci.getId();
			if (waveform != null) {
				for (final ScaComponent component : GraphitiAdapterUtil.safeFetchComponents(waveform)) {
					final String scaComponentId = component.identifier();
					if (scaComponentId.startsWith(myId)) {
						if (component instanceof LocalLaunch) {
							localLaunch = (LocalLaunch) component;
						}
					}
				}
			}

			if (localLaunch != null && localLaunch.getLaunch() != null && localLaunch.getLaunch().getProcesses().length > 0) {
				SpdLauncherUtil.terminate(localLaunch);
			}
		}

		// We need to remove the component from the sad.xml
		final SoftwareAssembly sad = DUtil.getDiagramSAD(getDiagram());
		ComponentPattern.deleteComponentInstantiation(ci, sad);

		// We need to manually remove the graphical representation of the component
		TransactionalEditingDomain editingDomain = getFeatureProvider().getDiagramTypeProvider().getDiagramBehavior().getEditingDomain();
		TransactionalCommandStack stack = (TransactionalCommandStack) editingDomain.getCommandStack();
		stack.execute(new RecordingCommand(editingDomain) {

			@Override
			protected void doExecute() {
				IRemoveContext rc = new RemoveContext(componentShape);
				IFeatureProvider featureProvider = getFeatureProvider();
				IRemoveFeature removeFeature = featureProvider.getRemoveFeature(rc);
				if (removeFeature != null) {
					removeFeature.remove(rc);
				}
			}
		});
	}
	
	@Override
	public String getImageId() {
		// Decided not to include this feature in the component button pad, but 
		// leaving code in place in case it becomes relevant later
//		return gov.redhawk.ide.graphiti.ui.diagram.providers.ImageProvider.IMG_TERMINATE;
		return null;
	}
}
