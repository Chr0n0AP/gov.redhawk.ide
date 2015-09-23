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
package gov.redhawk.ide.graphiti.sad.ui.diagram.features.update;

import gov.redhawk.ide.graphiti.ext.RHContainerShape;
import gov.redhawk.ide.graphiti.sad.ext.ComponentShape;
import gov.redhawk.ide.graphiti.sad.ui.SADUIGraphitiPlugin;
import gov.redhawk.ide.graphiti.sad.ui.diagram.patterns.AbstractUsesDevicePattern;
import gov.redhawk.ide.graphiti.sad.ui.diagram.patterns.ComponentPattern;
import gov.redhawk.ide.graphiti.sad.ui.diagram.patterns.HostCollocationPattern;
import gov.redhawk.ide.graphiti.ui.GraphitiUIPlugin;
import gov.redhawk.ide.graphiti.ui.diagram.features.layout.LayoutDiagramFeature;
import gov.redhawk.ide.graphiti.ui.diagram.features.update.AbstractDiagramUpdateFeature;
import gov.redhawk.ide.graphiti.ui.diagram.patterns.AbstractFindByPattern;
import gov.redhawk.ide.graphiti.ui.diagram.preferences.DiagramPreferenceConstants;
import gov.redhawk.ide.graphiti.ui.diagram.util.DUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import mil.jpeojtrs.sca.partitioning.FindBy;
import mil.jpeojtrs.sca.partitioning.FindByStub;
import mil.jpeojtrs.sca.partitioning.ProvidesPortStub;
import mil.jpeojtrs.sca.partitioning.UsesDeviceStub;
import mil.jpeojtrs.sca.partitioning.UsesPortStub;
import mil.jpeojtrs.sca.sad.HostCollocation;
import mil.jpeojtrs.sca.sad.SadComponentInstantiation;
import mil.jpeojtrs.sca.sad.SadComponentPlacement;
import mil.jpeojtrs.sca.sad.SadConnectInterface;
import mil.jpeojtrs.sca.sad.SoftwareAssembly;
import mil.jpeojtrs.sca.spd.UsesDevice;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IRemoveFeature;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.context.impl.RemoveContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

public class GraphitiWaveformDiagramUpdateFeature extends AbstractDiagramUpdateFeature {

	public GraphitiWaveformDiagramUpdateFeature(IFeatureProvider fp) {
		super(fp);
	}

	/**
	 * Updates the Diagram to reflect the underlying business model
	 * Make sure all elements in sad model (hosts/components/findby) are accounted for as
	 * children of diagram, if they aren't then add them, if they are then check to see if
	 * they need to be updated, if they exist in the diagram yet not in the model, remove them
	 * @param context
	 * @param performUpdate
	 * @return
	 * @throws CoreException
	 */
	public Reason internalUpdate(IUpdateContext context, boolean performUpdate) throws CoreException {

		boolean updateStatus = false;

		PictogramElement pe = context.getPictogramElement();
		if (pe instanceof Diagram) {
			Diagram d = (Diagram) pe;

			// get sad from diagram
			final SoftwareAssembly sad = DUtil.getDiagramSAD(getDiagram());

			// TODO: ensure our SAD has an assembly controller
			// set one if necessary, why bother the user?

			// model HostCollocation
			List<HostCollocation> hostCollocations = new ArrayList<HostCollocation>();
			if (sad != null && sad.getPartitioning() != null && sad.getPartitioning().getHostCollocation() != null) {
				// Elist -> List
				Collections.addAll(hostCollocations, (HostCollocation[]) sad.getPartitioning().getHostCollocation().toArray(new HostCollocation[0]));
			}
			// shape HostCollocation
			List<ContainerShape> hostCollocationShapes = HostCollocationPattern.getHostCollocationContainerShapes(d);

			// model components
			List<SadComponentInstantiation> componentInstantiations = new ArrayList<SadComponentInstantiation>();
			if (sad != null && sad.getPartitioning() != null && sad.getPartitioning().getComponentPlacement() != null) {
				// Get list of componentInstantiations from model
				for (SadComponentPlacement p : sad.getPartitioning().getComponentPlacement()) {
					Collections.addAll(componentInstantiations,
						(SadComponentInstantiation[]) p.getComponentInstantiation().toArray(new SadComponentInstantiation[0]));
				}
			}
			// shape components, excluding those found in host collocations
			List<ComponentShape> componentShapes = ComponentPattern.getAllComponentShapes(d);
			for (Iterator<ComponentShape> iter = componentShapes.iterator(); iter.hasNext();) {
				if (!(iter.next().eContainer() instanceof Diagram)) {
					iter.remove();
				}
			}
			
			// model UsesDevice
			List<UsesDevice> usesDevices = new ArrayList<UsesDevice>();
			if (sad != null && sad.getUsesDeviceDependencies() != null && sad.getUsesDeviceDependencies().getUsesdevice() != null) {
				// Get list of UsesDeviceStub from model
				Collections.addAll(usesDevices,
					(UsesDevice[]) sad.getUsesDeviceDependencies().getUsesdevice().toArray(new UsesDevice[0]));
			}
			// shape UsesDeviceStub
			List<RHContainerShape> usesDeviceStubShapes = AbstractUsesDevicePattern.getAllUsesDeviceStubShapes(d);
			for (Iterator<RHContainerShape> iter = usesDeviceStubShapes.iterator(); iter.hasNext();) {
				if (!(iter.next().eContainer() instanceof Diagram)) {
					iter.remove();
				}
			}

			// model connections
			List<SadConnectInterface> sadConnectInterfaces = new ArrayList<SadConnectInterface>();
			if (sad != null && sad.getConnections() != null && sad.getConnections().getConnectInterface() != null) {
				// Get list of SadConnectInterfaces from model
				Collections.addAll(sadConnectInterfaces, (SadConnectInterface[]) sad.getConnections().getConnectInterface().toArray(new SadConnectInterface[0]));
			}
			// remove invalid model connections
			removeInvalidConnections(sadConnectInterfaces);

			// shape connections
			List<Connection> connections = new ArrayList<Connection>();
			Collections.addAll(connections, (Connection[]) d.getConnections().toArray(new Connection[0]));

			/**** Check for inconsistencies in Host Collocation and number of components and connections ****/
			// Check Host Collocations for inconsistencies on text/name values
			boolean valuesMatch = true;
			if (hostCollocations.size() == hostCollocationShapes.size()) {
				for (int i = 0; i < hostCollocations.size(); i++) {
					valuesMatch = HostCollocationPattern.compareHostCoText(hostCollocationShapes.get(i), hostCollocations.get(i));
					// IDE-1021: Added condition that was supposed to be here
					if (!valuesMatch) {
						break;
					}
				}
			}

			// Check Host Collocations for inconsistencies in contained components
			boolean numberOfComponentsMatch = true;
			if (hostCollocations.size() == hostCollocationShapes.size()) {
				for (int i = 0; i < hostCollocations.size(); i++) {
					if (hostCollocations.get(i).getComponentPlacement().size() != hostCollocationShapes.get(i).getChildren().size()) {
						numberOfComponentsMatch = false;
						break;
					}
				}
			}

			// If inconsistencies are found remove all objects of that type and redraw
			// we must do this because the diagram uses indexed lists to refer to components in the sad file.
			if (performUpdate) {
				updateStatus = true;

				List<PictogramElement> pesToRemove = new ArrayList<PictogramElement>(); // gather all shapes to remove
				List<Object> objsToAdd = new ArrayList<Object>(); // gather all model object to add

				// If inconsistencies found, redraw diagram elements based on model objects
				boolean layoutNeeded = false;
				if (hostCollocations.size() != hostCollocationShapes.size() || !numberOfComponentsMatch || !valuesMatch) {
					Collections.addAll(pesToRemove, (PictogramElement[]) hostCollocationShapes.toArray(new PictogramElement[0]));
					Collections.addAll(objsToAdd, (Object[]) hostCollocations.toArray(new Object[0]));
					layoutNeeded = true;
				}
				if (componentShapes.size() != componentInstantiations.size() || !componentsResolved(componentShapes)) {
					Collections.addAll(pesToRemove, (PictogramElement[]) componentShapes.toArray(new PictogramElement[0]));
					Collections.addAll(objsToAdd, (Object[]) componentInstantiations.toArray(new Object[0]));
					layoutNeeded = true;
				}
				if (usesDeviceStubShapes.size() != usesDevices.size() || !usesDeviceStubsResolved(usesDeviceStubShapes)) {
					Collections.addAll(pesToRemove, (PictogramElement[]) usesDeviceStubShapes.toArray(new PictogramElement[0]));
					List<UsesDeviceStub> usesDeviceStubsToAdd = new ArrayList<UsesDeviceStub>();
					for (UsesDevice usesDevice: usesDevices) {
						usesDeviceStubsToAdd.add(AbstractUsesDevicePattern.createUsesDeviceStub(usesDevice));
					}
					//add ports to model
					AbstractUsesDevicePattern.addUsesDeviceStubPorts(sadConnectInterfaces, usesDeviceStubsToAdd);
					//VERY IMPORTANT, store copy in diagram file
					getDiagram().eResource().getContents().addAll(usesDeviceStubsToAdd);
					Collections.addAll(objsToAdd, (Object[]) usesDeviceStubsToAdd.toArray(new Object[0]));
					layoutNeeded = true;
				}

				// Easiest just to remove and redraw connections every time
				Collections.addAll(pesToRemove, (PictogramElement[]) connections.toArray(new PictogramElement[0]));

				if (!pesToRemove.isEmpty()) {
					// remove shapes from diagram
					for (PictogramElement peToRemove : pesToRemove) {
						// remove shape
						RemoveContext rc = new RemoveContext(peToRemove);
						IRemoveFeature removeFeature = getFeatureProvider().getRemoveFeature(rc);
						if (removeFeature != null) {
							removeFeature.remove(rc);
						}
					}
				} else {
					// update components
					super.update(context);
				}

				// add shapes to diagram
				if (!objsToAdd.isEmpty()) {
					for (Object objToAdd : objsToAdd) {
						DUtil.addShapeViaFeature(getFeatureProvider(), getDiagram(), objToAdd);
					}
				}

				// add connections to diagram
				addConnections(sadConnectInterfaces, getDiagram(), getFeatureProvider());

				if (layoutNeeded) {
					LayoutDiagramFeature layoutFeature = new LayoutDiagramFeature(getFeatureProvider());
					layoutFeature.execute(null);
				}
			} else {
				if (hostCollocations.size() != hostCollocationShapes.size() || !numberOfComponentsMatch || !valuesMatch) {
					return new Reason(true, "The sad.xml file and diagram HostCollocation objects do not match.  Reload the diagram from the xml file.");
				}
				if (componentShapes.size() != componentInstantiations.size() || !componentsResolved(componentShapes)) {
					return new Reason(true, "The sad.xml file and diagram component objects do not match.  Reload the diagram from the xml file.");
				}
			}

			// Ensure assembly controller is set in case a component was deleted that used to be the assembly controller
			ComponentPattern.organizeStartOrder(sad, getDiagram(), getFeatureProvider());
		}

		if (updateStatus && performUpdate) {
			return new Reason(true, "Update successful");
		}

		return new Reason(false, "No updates required");
	}

	/** Checks if componentShape has lost its reference to the model object 
	 * Very important to also check that component ports are still linked to the correct parent
	 * Bad things can happen because port links use index based references (0, 1, 2 etc.*/
	private boolean componentsResolved(List<ComponentShape> componentShapes) {
		for (ComponentShape componentShape : componentShapes) {
			SadComponentInstantiation sadComponentInstantiation = (SadComponentInstantiation) DUtil.getBusinessObject(componentShape, SadComponentInstantiation.class);
			if (sadComponentInstantiation == null || sadComponentInstantiation.getPlacement() == null || sadComponentInstantiation.getPlacement().getComponentFileRef() == null) {
				return false;
			}
			
			if (!GraphitiUIPlugin.getDefault().getPreferenceStore().getBoolean(DiagramPreferenceConstants.HIDE_DETAILS)) {
				//applies only if we are showing the component shape details (ports)
				if (componentShape.getProvidesPortStubs().size() > 0 && !componentShape.getProvidesPortStubs().get(0).eContainer().equals(sadComponentInstantiation)) {
					return false;
				} else if (componentShape.getUsesPortStubs().size() > 0 && !componentShape.getUsesPortStubs().get(0).eContainer().equals(sadComponentInstantiation)) {
					return false;
				}
			}
			
		}
		return true;
	}
	
	/** Checks if rhContainerShape has lost its reference to the UsesDeviceStub model object */
	private boolean usesDeviceStubsResolved(List<RHContainerShape> usesDeviceStubShapes) {
		for (RHContainerShape usesDeviceStubShape : usesDeviceStubShapes) {
			Object obj = DUtil.getBusinessObject(usesDeviceStubShape, UsesDeviceStub.class);
			if (obj == null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Reason updateNeeded(IUpdateContext context) {
		try {
			return internalUpdate(context, false);
		} catch (CoreException e) {
			// PASS
			// TODO: catch exception
		}
		return null;
	}

	@Override
	public boolean update(IUpdateContext context) {
		Reason reason;
		try {
			reason = internalUpdate(context, true);
			return reason.toBoolean();
		} catch (CoreException e) {
			// PASS
			// TODO: catch exception
			e.printStackTrace(); // SUPPRESS CHECKSTYLE INLINE
		}

		return false;
	}

	/**
	 * Add new Connections and also add FindBy Shapes where necessary
	 * @param sadConnectInterfaces
	 * @param pictogramLabel
	 * @param featureProvider
	 * @param performUpdate
	 * @return
	 * @throws CoreException
	 */
	protected void addConnections(List<SadConnectInterface> sadConnectInterfaces, Diagram diagram, IFeatureProvider featureProvider) throws CoreException {

		// add findByStub shapes
		addFindBy(sadConnectInterfaces, diagram, featureProvider);

		// add Connections found in model, but not in diagram
		for (SadConnectInterface sadConnectInterface : sadConnectInterfaces) {

			// wasn't found, add Connection
			// lookup sourceAnchor
			Anchor sourceAnchor = DUtil.lookupSourceAnchor(sadConnectInterface, diagram);

			// if sourceAnchor wasn't found its because the findBy needs to be added to the diagram
			if (sourceAnchor == null) {

				// FindBy is always used inside usesPort
				if (sadConnectInterface.getUsesPort() != null && sadConnectInterface.getUsesPort().getFindBy() != null) {

					FindBy findBy = (FindBy) sadConnectInterface.getUsesPort().getFindBy();

					// search for findByStub in diagram
					FindByStub findByStub = DUtil.findFindByStub(findBy, diagram);

					if (findByStub == null) {
						// should never occur, addRemoveUpdateFindBy() takes care of this
						throw new CoreException(new Status(IStatus.ERROR, SADUIGraphitiPlugin.PLUGIN_ID, "Unable to locate FindBy Shape in Diagram"));
					}

					// determine which usesPortStub
					UsesPortStub usesPortStub = null;
					for (UsesPortStub p : findByStub.getUses()) {
						if (p != null && sadConnectInterface.getUsesPort().getUsesIdentifier() != null
							&& p.getName().equals(sadConnectInterface.getUsesPort().getUsesIdentifier())) {
							usesPortStub = p;
						}
					}
					// determine port anchor for FindByMatch
					if (usesPortStub != null) {
						PictogramElement pe = DUtil.getPictogramElementForBusinessObject(diagram, (EObject) usesPortStub, Anchor.class);
						sourceAnchor = (Anchor) pe;
					}
				} else if (sadConnectInterface.getUsesPort() != null && sadConnectInterface.getUsesPort().getDeviceUsedByApplication() != null) {
					
					UsesDeviceStub usesDeviceStub = AbstractUsesDevicePattern.findUsesDeviceStub(sadConnectInterface.getUsesPort().getDeviceUsedByApplication(), diagram);
					
					// determine which usesPortStub we are targeting
					UsesPortStub usesPortStub = null;
					for (UsesPortStub p : usesDeviceStub.getUsesPortStubs()) {
						if (p != null && sadConnectInterface.getUsesPort().getUsesIdentifier() != null
							&& p.getName().equals(sadConnectInterface.getUsesPort().getUsesIdentifier())) {
							usesPortStub = p;
						}
					}

					// determine port anchor for usesDeviceStub
					if (usesPortStub != null) {
						PictogramElement pe = DUtil.getPictogramElementForBusinessObject(diagram, usesPortStub, Anchor.class);
						sourceAnchor = (Anchor) pe;
					}
				}
			}

			// lookup Target Anchor
			Anchor targetAnchor = null;
			PictogramElement targetAnchorPe = DUtil.getPictogramElementForBusinessObject(diagram, sadConnectInterface.getTarget(), Anchor.class);
			if (targetAnchorPe != null) {
				targetAnchor = (Anchor) targetAnchorPe;
			} else {

				// sadConnectInterface.getComponentSupportedInterface().getFindBy()
				if (sadConnectInterface.getComponentSupportedInterface() != null
					&& sadConnectInterface.getComponentSupportedInterface().getSupportedIdentifier() != null
					&& sadConnectInterface.getComponentSupportedInterface().getFindBy() != null) {

					// The model provides us with interface information for the FindBy we are connecting to
					FindBy findBy = (FindBy) sadConnectInterface.getComponentSupportedInterface().getFindBy();

					// iterate through FindByStubs in diagram
					FindByStub findByStub = DUtil.findFindByStub(findBy, diagram);

					if (findByStub == null) {
						// should never occur, addRemoveUpdateFindBy() takes care of this
						throw new CoreException(new Status(IStatus.ERROR, SADUIGraphitiPlugin.PLUGIN_ID, "Unable to locate FindBy Shape in Diagram"));
					}

					// determine port anchor for FindByMatch
					if (findByStub.getInterface() != null) {
						PictogramElement pe = DUtil.getPictogramElementForBusinessObject(diagram, findByStub.getInterface(), Anchor.class);
						targetAnchor = (Anchor) pe;
					}

					// findBy nested in ProvidesPort
				} else if (sadConnectInterface.getProvidesPort() != null && sadConnectInterface.getProvidesPort().getFindBy() != null) {

					FindBy findBy = (FindBy) sadConnectInterface.getProvidesPort().getFindBy();

					// iterate through FindByStubs in diagram
					FindByStub findByStub = DUtil.findFindByStub(findBy, diagram);

					if (findByStub == null) {
						// should never occur, addRemoveUpdateFindBy() takes care of this
						throw new CoreException(new Status(IStatus.ERROR, SADUIGraphitiPlugin.PLUGIN_ID, "Unable to locate FindBy Shape in Diagram"));
					}

					// ensure the providesPort exists in FindByStub that already exists in diagram
					boolean foundProvidesPortStub = false;
					for (ProvidesPortStub p : findByStub.getProvides()) {
						if (p.getName().equals(sadConnectInterface.getProvidesPort().getProvidesIdentifier())) {
							foundProvidesPortStub = true;
						}
					}
					if (!foundProvidesPortStub) {
						// add the required providesPort
						AbstractFindByPattern.addProvidesPortStubToFindByStub(findByStub, sadConnectInterface.getProvidesPort(), featureProvider);
						// Update on FindByStub PE
						DUtil.updateShapeViaFeature(featureProvider, diagram,
							DUtil.getPictogramElementForBusinessObject(diagram, findByStub, RHContainerShape.class));

						// maybe call layout?

					}

					// determine which providesPortStub we are targeting
					ProvidesPortStub providesPortStub = null;
					for (ProvidesPortStub p : findByStub.getProvides()) {
						if (p != null && sadConnectInterface.getProvidesPort().getProvidesIdentifier() != null
							&& p.getName().equals(sadConnectInterface.getProvidesPort().getProvidesIdentifier())) {
							providesPortStub = p;
							break;
						}
					}

					// determine port anchor for FindByMatch
					if (providesPortStub != null) {
						PictogramElement pe = DUtil.getPictogramElementForBusinessObject(diagram, (EObject) providesPortStub, Anchor.class);
						targetAnchor = (Anchor) pe;
					} else {
						// PASS
						// TODO: this means the provides port didn't exist in the existing findByStub..we need
						// to add it
					}
				} else if (sadConnectInterface.getProvidesPort() != null && sadConnectInterface.getProvidesPort().getDeviceUsedByApplication() != null) {

					UsesDeviceStub usesDeviceStub = AbstractUsesDevicePattern.findUsesDeviceStub(sadConnectInterface.getProvidesPort().getDeviceUsedByApplication(), diagram);
					
					// determine which providesPortStub we are targeting
					ProvidesPortStub providesPortStub = null;
					for (ProvidesPortStub p : usesDeviceStub.getProvidesPortStubs()) {
						if (p != null && sadConnectInterface.getProvidesPort().getProvidesIdentifier() != null
							&& p.getName().equals(sadConnectInterface.getProvidesPort().getProvidesIdentifier())) {
							providesPortStub = p;
						}
					}

					// determine port anchor for usesDeviceStub
					if (providesPortStub != null) {
						PictogramElement pe = DUtil.getPictogramElementForBusinessObject(diagram, providesPortStub, Anchor.class);
						targetAnchor = (Anchor) pe;
					}
				} else if (sadConnectInterface.getComponentSupportedInterface() != null
						&& sadConnectInterface.getComponentSupportedInterface().getSupportedIdentifier() != null
						&& sadConnectInterface.getComponentSupportedInterface().getDeviceUsedByApplication() != null) {

					UsesDeviceStub usesDeviceStub = AbstractUsesDevicePattern.findUsesDeviceStub(sadConnectInterface.getComponentSupportedInterface().getDeviceUsedByApplication(), diagram);
					
					// determine port anchor for UsesDevice
					if (usesDeviceStub.getInterface() != null) {
						PictogramElement pe = DUtil.getPictogramElementForBusinessObject(diagram, usesDeviceStub.getInterface(), Anchor.class);
						targetAnchor = (Anchor) pe;
					}
				}
			}

			// add Connection if anchors
			if (sourceAnchor != null && targetAnchor != null) {
				DUtil.addConnectionViaFeature(featureProvider, sadConnectInterface, sourceAnchor, targetAnchor);
			} else {
				// PASS
				// TODO: how do we handle this?
			}
		}
	}

}
