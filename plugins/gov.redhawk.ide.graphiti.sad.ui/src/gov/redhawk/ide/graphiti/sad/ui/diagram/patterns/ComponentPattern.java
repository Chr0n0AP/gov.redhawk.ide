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
package gov.redhawk.ide.graphiti.sad.ui.diagram.patterns;

import gov.redhawk.ide.graphiti.ext.impl.RHContainerShapeImpl;
import gov.redhawk.ide.graphiti.sad.ext.ComponentShape;
import gov.redhawk.ide.graphiti.sad.ext.RHSadGxFactory;
import gov.redhawk.ide.graphiti.sad.ui.diagram.providers.WaveformImageProvider;
import gov.redhawk.ide.graphiti.ui.diagram.patterns.AbstractPortSupplierPattern;
import gov.redhawk.ide.graphiti.ui.diagram.util.DUtil;
import gov.redhawk.ide.graphiti.ui.diagram.util.StyleUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import mil.jpeojtrs.sca.partitioning.ComponentFile;
import mil.jpeojtrs.sca.partitioning.ComponentSupportedInterfaceStub;
import mil.jpeojtrs.sca.partitioning.ProvidesPortStub;
import mil.jpeojtrs.sca.partitioning.UsesPortStub;
import mil.jpeojtrs.sca.sad.AssemblyController;
import mil.jpeojtrs.sca.sad.ExternalPorts;
import mil.jpeojtrs.sca.sad.HostCollocation;
import mil.jpeojtrs.sca.sad.SadComponentInstantiation;
import mil.jpeojtrs.sca.sad.SadComponentInstantiationRef;
import mil.jpeojtrs.sca.sad.SadComponentPlacement;
import mil.jpeojtrs.sca.sad.SadConnectInterface;
import mil.jpeojtrs.sca.sad.SadFactory;
import mil.jpeojtrs.sca.sad.SoftwareAssembly;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalCommandStack;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.IRemoveFeature;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.IDirectEditingContext;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.context.IRemoveContext;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.context.impl.RemoveContext;
import org.eclipse.graphiti.features.context.impl.UpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.Property;
import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;

public class ComponentPattern extends AbstractPortSupplierPattern {

	// Property key/value pairs help us identify Shapes to enable/disable user actions (move, resize, delete, remove
	// etc.)
	public static final String SHAPE_START_ORDER_ELLIPSE_SHAPE = "startOrderEllipseShape";

	// These are property key/value pairs that help us resize an existing shape by properly identifying
	// graphicsAlgorithms
	public static final String GA_START_ORDER_TEXT = "startOrderText";
	public static final String GA_START_ORDER_ELLIPSE = "startOrderEllipse";

	// Shape size constants
	private static final int START_ORDER_ELLIPSE_DIAMETER = 17;
	private static final int START_ORDER_TOP_TEXT_PADDING = 0;
	private static final int START_ORDER_ELLIPSE_LEFT_PADDING = 20;
	private static final int START_ORDER_ELLIPSE_RIGHT_PADDING = 5;
	private static final int START_ORDER_ELLIPSE_TOP_PADDING = 5;

	// Default start order text value for components that do not have a start order declared
	private static final String NO_START_ORDER_STRING = "";

	private URI spdUri = null;

	public ComponentPattern() {
		super(null);
	}

	public URI getSpdUri() {
		return spdUri;
	}

	public void setSpdUri(URI spdUri) {
		this.spdUri = spdUri;
	}

	@Override
	public String getCreateName() {
		return "Component";
	}

	// THE FOLLOWING THREE METHODS DETERMINE IF PATTERN IS APPLICABLE TO OBJECT
	@Override
	public boolean isMainBusinessObjectApplicable(Object mainBusinessObject) {
		return mainBusinessObject instanceof SadComponentInstantiation;
	}

	@Override
	protected boolean isPatternControlled(PictogramElement pictogramElement) {
		Object domainObject = getBusinessObjectForPictogramElement(pictogramElement);
		return isMainBusinessObjectApplicable(domainObject);
	}

	@Override
	protected boolean isPatternRoot(PictogramElement pictogramElement) {
		Object domainObject = getBusinessObjectForPictogramElement(pictogramElement);
		return isMainBusinessObjectApplicable(domainObject);
	}

	// DIAGRAM FEATURES

	@Override
	public boolean canAdd(IAddContext context) {
		if (context.getNewObject() instanceof SadComponentInstantiation) {
			if (context.getTargetContainer() instanceof Diagram || DUtil.getHostCollocation(context.getTargetContainer()) != null) {
				return true;
			}
			return false;
		}
		return false;
	}

	@Override
	public boolean canRemove(IRemoveContext context) {
		// TODO: this used to return false, doing this so we can remove components during the
		// RHDiagramUpdateFeature...might be negative consequences
		Object obj = DUtil.getBusinessObject(context.getPictogramElement());
		if (obj instanceof SadComponentInstantiation) {
			return true;
		}
		return false;
	}

	/**
	 * Return true if the user has selected a pictogram element that is linked with
	 * a SADComponentInstantiation instance
	 */
	@Override
	public boolean canDelete(IDeleteContext context) {
		Object obj = DUtil.getBusinessObject(context.getPictogramElement());
		if (obj instanceof SadComponentInstantiation) {
			return true;
		}
		return false;
	}

	/**
	 * Delete the SadComponentInstantiation linked to the PictogramElement.
	 */
	@Override
	public void delete(IDeleteContext context) {

		// set componentToDelete
		final SadComponentInstantiation ciToDelete = (SadComponentInstantiation) DUtil.getBusinessObject(context.getPictogramElement());

		final Diagram diagram = DUtil.findDiagram((ContainerShape) context.getPictogramElement());

		// editing domain for our transaction
		TransactionalEditingDomain editingDomain = getFeatureProvider().getDiagramTypeProvider().getDiagramBehavior().getEditingDomain();

		// get sad from diagram
		final SoftwareAssembly sad = DUtil.getDiagramSAD(getDiagram());

		// Perform business object manipulation in a Command
		TransactionalCommandStack stack = (TransactionalCommandStack) editingDomain.getCommandStack();
		stack.execute(new RecordingCommand(editingDomain) {
			@Override
			protected void doExecute() {

				// delete component from SoftwareAssembly
				deleteComponentInstantiation(ciToDelete, sad);

				// re-organize start order
				organizeStartOrder(sad, diagram, getFeatureProvider());

			}
		});

		// delete graphical component for component as well as removing all connections
		IRemoveContext rc = new RemoveContext(context.getPictogramElement());
		IFeatureProvider featureProvider = getFeatureProvider();
		IRemoveFeature removeFeature = featureProvider.getRemoveFeature(rc);
		if (removeFeature != null) {
			removeFeature.remove(rc);
		}

		// redraw start order
		// DUtil.organizeDiagramStartOrder(diagram);
	}

	/**
	 * Delete SadComponentInstantiation and corresponding SadComponentPlacement business object from SoftwareAssembly
	 * This method should be executed within a RecordingCommand.
	 * @param ciToDelete
	 * @param diagram
	 */
	public static void deleteComponentInstantiation(final SadComponentInstantiation ciToDelete, final SoftwareAssembly sad) {

		// assembly controller may reference componentInstantiation
		// delete reference if applicable
		if (sad.getAssemblyController() != null && sad.getAssemblyController().getComponentInstantiationRef() != null
			&& sad.getAssemblyController().getComponentInstantiationRef().getInstantiation().equals(ciToDelete)) {
			EcoreUtil.delete(sad.getAssemblyController().getComponentInstantiationRef());
			sad.getAssemblyController().setComponentInstantiationRef(null);
		}

		// get placement for instantiation and delete it from sad partitioning after we look at removing the component
		// file ref.
		SadComponentPlacement placement = (SadComponentPlacement) ciToDelete.getPlacement();

		// find and remove any attached connections
		// gather connections
		List<SadConnectInterface> connectionsToRemove = new ArrayList<SadConnectInterface>();
		if (sad.getConnections() != null) {
			for (SadConnectInterface connectionInterface : sad.getConnections().getConnectInterface()) {
				// we need to do thorough null checks here because of the many connection possibilities. Firstly a
				// connection requires only a usesPort and either (providesPort || componentSupportedInterface)
				// and therefore null checks need to be performed.
				// FindBy connections don't have ComponentInstantiationRefs and so they can also be null
				if ((connectionInterface.getComponentSupportedInterface() != null
					&& connectionInterface.getComponentSupportedInterface().getComponentInstantiationRef() != null && ciToDelete.getId().equals(
					connectionInterface.getComponentSupportedInterface().getComponentInstantiationRef().getRefid()))
					|| (connectionInterface.getUsesPort() != null && connectionInterface.getUsesPort().getComponentInstantiationRef() != null && ciToDelete.getId().equals(
						connectionInterface.getUsesPort().getComponentInstantiationRef().getRefid()))
					|| (connectionInterface.getProvidesPort() != null && connectionInterface.getProvidesPort().getComponentInstantiationRef() != null && ciToDelete.getId().equals(
						connectionInterface.getProvidesPort().getComponentInstantiationRef().getRefid()))) {
					connectionsToRemove.add(connectionInterface);
				}
			}
		}
		// remove gathered connections
		if (sad.getConnections() != null) {
			sad.getConnections().getConnectInterface().removeAll(connectionsToRemove);
		}

		// delete component file if applicable
		// figure out which component file we are using and if no other component placements using it then remove it.
		ComponentFile componentFileToRemove = placement.getComponentFileRef().getFile();
		// check components (not in host collocation)
		for (SadComponentPlacement p : sad.getPartitioning().getComponentPlacement()) {
			if (p != placement && p.getComponentFileRef().getRefid().equals(placement.getComponentFileRef().getRefid())) {
				componentFileToRemove = null;
				break;
			}
		}
		// check components in host collocation
		for (HostCollocation hc : sad.getPartitioning().getHostCollocation()) {
			for (SadComponentPlacement p : hc.getComponentPlacement()) {
				if (p != placement && p.getComponentFileRef().getRefid().equals(placement.getComponentFileRef().getRefid())) {
					componentFileToRemove = null;
					break;
				}
			}
			if (componentFileToRemove == null) {
				break;
			}
		}
		if (componentFileToRemove != null) {
			sad.getComponentFiles().getComponentFile().remove(componentFileToRemove);
		}

		// delete component placement
		EcoreUtil.delete(placement);
	}

	@Override
	public boolean canResizeShape(IResizeShapeContext context) {
		return true;
	}

	/**
	 * Adds a Component to the diagram. Immediately calls resize at the end to keep sizing and location in one place.
	 */
	public PictogramElement add(IAddContext context) {

		// create shape
		ComponentShape componentShape = RHSadGxFactory.eINSTANCE.createComponentShape();
		componentShape.init(context, this);

		// set shape location to user's selection
		Graphiti.getGaLayoutService().setLocation(componentShape.getGraphicsAlgorithm(), context.getX(), context.getY());

		if (!DUtil.isDiagramRuntime(getDiagram())) {
			SadComponentInstantiation instantiation = (SadComponentInstantiation) context.getNewObject();

			// Create ellipse shape to display component start order
			ContainerShape startOrderEllipseShape = Graphiti.getCreateService().createContainerShape(componentShape.getInnerContainerShape(), false);
			Graphiti.getPeService().setPropertyValue(startOrderEllipseShape, DUtil.SHAPE_TYPE, ComponentPattern.SHAPE_START_ORDER_ELLIPSE_SHAPE);
			Ellipse startOrderEllipse = Graphiti.getCreateService().createEllipse(startOrderEllipseShape);
			StyleUtil.setStyle(startOrderEllipse, getStartOrderStyle(instantiation));
			Graphiti.getPeService().setPropertyValue(startOrderEllipse, DUtil.GA_TYPE, ComponentPattern.GA_START_ORDER_ELLIPSE);
			Graphiti.getGaLayoutService().setSize(startOrderEllipse, START_ORDER_ELLIPSE_DIAMETER, START_ORDER_ELLIPSE_DIAMETER);

			// Create text shape to display start order
			Shape startOrderTextShape = Graphiti.getPeCreateService().createShape(startOrderEllipseShape, false);
			Text startOrderText = Graphiti.getCreateService().createText(startOrderTextShape, getStartOrderValue(instantiation));
			Graphiti.getPeService().setPropertyValue(startOrderText, DUtil.GA_TYPE, ComponentPattern.GA_START_ORDER_TEXT);
			StyleUtil.setStyle(startOrderText, StyleUtil.START_ORDER);
			Graphiti.getGaLayoutService().setSize(startOrderText, START_ORDER_ELLIPSE_DIAMETER, START_ORDER_ELLIPSE_DIAMETER);
		}

		// layout
		layoutPictogramElement(componentShape);

		// Check for any needed location adjustments
		adjustComponentLocation(componentShape);

		return componentShape;
	}

	@Override
	public boolean layout(ILayoutContext context) {
		boolean layoutApplied = super.layout(context);

		// Layout the start order ellipse, if any
		ComponentShape componentShape = (ComponentShape) context.getPictogramElement();
		ContainerShape startOrderEllipse = componentShape.getStartOrderEllipseShape();
		if (startOrderEllipse != null) {
			// Move the ellipse to the upper right corner of its parent
			int xOffset = startOrderEllipse.getContainer().getGraphicsAlgorithm().getWidth() - (START_ORDER_ELLIPSE_DIAMETER + START_ORDER_ELLIPSE_RIGHT_PADDING);
			if (DUtil.moveIfNeeded(startOrderEllipse.getGraphicsAlgorithm(), xOffset, START_ORDER_ELLIPSE_TOP_PADDING)) {
				layoutApplied = true;
			}

			// Position the text in the center of the ellipse
			Text startOrderText = componentShape.getStartOrderText();
			IDimension textDimension = DUtil.calculateTextSize(startOrderText);
			int textX = START_ORDER_ELLIPSE_DIAMETER / 2 - textDimension.getWidth() / 2;
			if (DUtil.moveIfNeeded(startOrderText, textX, START_ORDER_TOP_TEXT_PADDING)) {
				layoutApplied = true;
			}
		}
		return layoutApplied;
	}

	@Override
	public IReason updateNeeded(IUpdateContext context) {
		IReason reason = super.updateNeeded(context);
		if (reason.toBoolean()) {
			return reason;
		}

		// Check the start order ellipse, if any
		ComponentShape componentShape = (ComponentShape) context.getPictogramElement();
		ContainerShape startOrderEllipse = componentShape.getStartOrderEllipseShape();
		if (startOrderEllipse != null) {
			// Check the ellipse style
			SadComponentInstantiation instantiation = (SadComponentInstantiation) getBusinessObjectForPictogramElement(context.getPictogramElement());
			String startOrderStyle = getStartOrderStyle(instantiation);
			if (!StyleUtil.isStyleSet(startOrderEllipse.getGraphicsAlgorithm(), startOrderStyle)) {
				return Reason.createTrueReason("Start order ellipse needs update");
			}

			// Check the text value
			Text startOrderText = componentShape.getStartOrderText();
			if (!startOrderText.getValue().equals(getStartOrderValue(instantiation))) {
				return Reason.createTrueReason("Start order number needs update");
			}
		}

		return Reason.createFalseReason();
	}

	@Override
	public boolean update(IUpdateContext context) {
		boolean updateStatus = super.update(context);

		// Check the start order ellipse, if any
		ComponentShape componentShape = (ComponentShape) context.getPictogramElement();
		ContainerShape startOrderEllipse = componentShape.getStartOrderEllipseShape();
		if (startOrderEllipse != null) {
			// Check the ellipse style
			SadComponentInstantiation instantiation = (SadComponentInstantiation) getBusinessObjectForPictogramElement(context.getPictogramElement());
			String startOrderStyle = getStartOrderStyle(instantiation);
			if (!StyleUtil.isStyleSet(startOrderEllipse.getGraphicsAlgorithm(), startOrderStyle)) {
				StyleUtil.setStyle(startOrderEllipse.getGraphicsAlgorithm(), startOrderStyle);
				updateStatus = true;
			}

			// Check the text value
			Text startOrderText = componentShape.getStartOrderText();
			String startOrderValue = getStartOrderValue(instantiation);
			if (!startOrderText.getValue().equals(startOrderValue)) {
				startOrderText.setValue(startOrderValue);
				updateStatus = true;
			}
		}

		return updateStatus;
	}

	/**
	 * Checks to make sure the new component is not being stacked on top of an existing component
	 * @param componentShape
	 */
	private void adjustComponentLocation(ComponentShape componentShape) {
		final int BUFFER_WIDTH = 20;

		// if any overlap occurs (can happen when launching using the REDHAWK Explorer) adjust x/y-coords
		Diagram diagram = getFeatureProvider().getDiagramTypeProvider().getDiagram();
		EList<Shape> children = diagram.getChildren();
		for (Shape child : children) {
			boolean xAdjusted = false;
			int xAdjustment = 0;

			// Avoid infinite loop by checking a shape against itself
			if (child.equals(componentShape)) {
				continue;
			}

			// Don't adjust if the target shape is in a Host Collocation
			if (child instanceof ContainerShape && DUtil.getHostCollocation((ContainerShape) child) != null) {
				continue;
			}
			GraphicsAlgorithm childGa = child.getGraphicsAlgorithm();
			int componentWidth = componentShape.getGraphicsAlgorithm().getWidth();
			int componentHeight = componentShape.getGraphicsAlgorithm().getHeight();

			int componentX = componentShape.getGraphicsAlgorithm().getX();
			int componentY = componentShape.getGraphicsAlgorithm().getY();

			boolean xOverlapped = componentX >= childGa.getX() && componentX <= (childGa.getX() + childGa.getWidth()) || childGa.getX() >= componentX
				&& childGa.getX() <= componentX + componentWidth;
			boolean yOverlapped = componentY >= childGa.getY() && componentY <= (childGa.getY() + childGa.getHeight()) || childGa.getY() >= componentY
				&& childGa.getY() <= componentY + componentHeight;
			// If there is any overlap, then move new component all the way to the right of the old component.
			if (xOverlapped && yOverlapped) {
				xAdjustment += childGa.getX() + childGa.getWidth() + BUFFER_WIDTH;
				xAdjusted = true;
			}
			if (xAdjusted) {
				componentShape.getGraphicsAlgorithm().setX(xAdjustment);
				// If we've made any adjustments, make a recursive call to make sure we do not create a new collision
				adjustComponentLocation(componentShape);
			}
		}
	}

	public boolean canMoveShape(IMoveShapeContext context) {

		SadComponentInstantiation sadComponentInstantiation = (SadComponentInstantiation) DUtil.getBusinessObject(context.getPictogramElement());
		if (sadComponentInstantiation == null) {
			return false;
		}

		// if moving to HostCollocation to Sad Partitioning
		if (context.getTargetContainer() instanceof Diagram || DUtil.getHostCollocation(context.getTargetContainer()) != null) {
			return true;
		}
		return false;

	}

	/**
	 * Moves Component shape.
	 * if moving to HostCollocation or away from one modify underlying model and allow parent class to perform graphical
	 * move
	 * if moving within the same container allow parent class to perform graphical move
	 */
	public void moveShape(IMoveShapeContext context) {
		SadComponentInstantiation ci = (SadComponentInstantiation) DUtil.getBusinessObject(context.getPictogramElement());

		final SoftwareAssembly sad = DUtil.getDiagramSAD(getDiagram());

		// if moving inside the same container
		if (context.getSourceContainer() == context.getTargetContainer()) {
			super.moveShape(context);
		}

		// if moving from HostCollocation to a different HostCollocation
		if (DUtil.getHostCollocation(context.getSourceContainer()) != null && DUtil.getHostCollocation(context.getTargetContainer()) != null
			&& DUtil.getHostCollocation(context.getSourceContainer()) != DUtil.getHostCollocation(context.getTargetContainer())) {
			// swap parents
			DUtil.getHostCollocation(context.getSourceContainer()).getComponentPlacement().remove((SadComponentPlacement) ci.getPlacement());
			DUtil.getHostCollocation(context.getTargetContainer()).getComponentPlacement().add((SadComponentPlacement) ci.getPlacement());
			super.moveShape(context);
		}

		// if moving to HostCollocation from Sad Partitioning
		if (DUtil.getHostCollocation(context.getTargetContainer()) != null && context.getSourceContainer() instanceof Diagram) {
			// swap parents
			sad.getPartitioning().getComponentPlacement().remove(ci.getPlacement());
			DUtil.getHostCollocation(context.getTargetContainer()).getComponentPlacement().add((SadComponentPlacement) ci.getPlacement());
			super.moveShape(context);
		}

		// if moving to Sad Partitioning from HostCollocation
		if (DUtil.getHostCollocation(context.getSourceContainer()) != null && context.getTargetContainer() instanceof Diagram) {
			// swap parents
			sad.getPartitioning().getComponentPlacement().add((SadComponentPlacement) ci.getPlacement());
			DUtil.getHostCollocation(context.getSourceContainer()).getComponentPlacement().remove((SadComponentPlacement) ci.getPlacement());
			super.moveShape(context);
		}

	}

	/**
	 * Return the highest start order for all components in the SAD.
	 * Returns null if no components are found
	 * @param sad
	 * @return
	 */
	public static BigInteger determineHighestStartOrder(final SoftwareAssembly sad) {

		BigInteger highestStartOrder = null;
		List<SadComponentInstantiation> cis = getAllComponents(sad);
		if (cis != null && cis.size() > 0) {
			highestStartOrder = cis.get(0).getStartOrder();

		}
		for (int i = 1; i < cis.size(); i++) {
			SadComponentInstantiation c = cis.get(i);

			// If a component is found, and it's start order is null, assume it is the assembly controller
			// Assembly controllers should always be at the beginning of the start order, so mark highest start order as
			// zero
			if (highestStartOrder == null) {
				highestStartOrder = BigInteger.ZERO;
			}

			// check for higher start order
			if (c.getStartOrder() != null && c.getStartOrder().compareTo(highestStartOrder) >= 0) {
				highestStartOrder = c.getStartOrder();
			}
		}

		// If there are no components, highestStartOrder will be null
		return highestStartOrder;
	}

	/**
	 * Get all components in sad
	 * @param sad
	 * @return
	 */
	public static List<SadComponentInstantiation> getAllComponents(final SoftwareAssembly sad) {
		final List<SadComponentInstantiation> retVal = new ArrayList<SadComponentInstantiation>();
		if (sad.getPartitioning() != null) {
			for (final SadComponentPlacement cp : sad.getPartitioning().getComponentPlacement()) {
				retVal.addAll(cp.getComponentInstantiation());
			}
			for (final HostCollocation h : sad.getPartitioning().getHostCollocation()) {
				for (final SadComponentPlacement cp : h.getComponentPlacement()) {
					retVal.addAll(cp.getComponentInstantiation());
				}
			}
		}

		return retVal;
	}

	/**
	 * swap start order of provided components. Change assembly controller if start order zero
	 * @param sad
	 * @param featureProvider
	 * @param lowerCi - The component that currently has the lower start order
	 * @param higherCi - The component that currently has the higher start order
	 */
	public static void swapStartOrder(SoftwareAssembly sad, IFeatureProvider featureProvider, final SadComponentInstantiation lowerCi,
		final SadComponentInstantiation higherCi) {

		// editing domain for our transaction
		TransactionalEditingDomain editingDomain = featureProvider.getDiagramTypeProvider().getDiagramBehavior().getEditingDomain();

		// get AssemblyController
		final AssemblyController assemblyController = sad.getAssemblyController();

		// Perform business object manipulation in a Command
		TransactionalCommandStack stack = (TransactionalCommandStack) editingDomain.getCommandStack();
		stack.execute(new RecordingCommand(editingDomain) {
			@Override
			protected void doExecute() {

				// Increment start order
				lowerCi.setStartOrder(higherCi.getStartOrder());
				// Decrement start order
				higherCi.setStartOrder(higherCi.getStartOrder().subtract(BigInteger.ONE));

				// set assembly controller if start order is zero
				if (lowerCi.getStartOrder().compareTo(BigInteger.ZERO) == 0) {
					assemblyController.getComponentInstantiationRef().setInstantiation(lowerCi);
				} else if (higherCi.getStartOrder().compareTo(BigInteger.ZERO) == 0) {
					assemblyController.getComponentInstantiationRef().setInstantiation(higherCi);
				}
			}
		});
	}

	/*
	 *  returns component instantiation with provided start order
	 */
	public static SadComponentInstantiation getComponentInstantiationViaStartOrder(final SoftwareAssembly sad, final BigInteger startOrder) {
		for (SadComponentInstantiation ci : sad.getAllComponentInstantiations()) {
			if (ci.getStartOrder() != null && ci.getStartOrder().compareTo(startOrder) == 0) {
				return ci;
			}
		}
		return null;
	}

	// adjust the start order for a component
	public static void organizeStartOrder(final SoftwareAssembly sad, final Diagram diagram, final IFeatureProvider featureProvider) {
		BigInteger startOrder = BigInteger.ZERO;

		// get components by start order
		EList<SadComponentInstantiation> componentInstantiationsInStartOrder = sad.getComponentInstantiationsInStartOrder();

		// if assembly controller was deleted (or component that used to be assembly controller was deleted)
		// set a new assembly controller
		AssemblyController assemblyController = getAssemblyController(featureProvider, diagram);
		if ((assemblyController == null || assemblyController.getComponentInstantiationRef().getInstantiation() == null)
			&& componentInstantiationsInStartOrder.size() > 0) {
			// assign assembly controller assign to first component
			assemblyController = SadFactory.eINSTANCE.createAssemblyController();
			SadComponentInstantiation ci = componentInstantiationsInStartOrder.get(0);
			SadComponentInstantiationRef sadComponentInstantiationRef = SadFactory.eINSTANCE.createSadComponentInstantiationRef();
			sadComponentInstantiationRef.setInstantiation(ci);
			assemblyController.setComponentInstantiationRef(sadComponentInstantiationRef);
			sad.setAssemblyController(assemblyController);

			// If the component has a start order defined, update it to run first
			if (ci.getStartOrder() != null) {
				ci.setStartOrder(BigInteger.ZERO);
			}

		}

		if (assemblyController != null && assemblyController.getComponentInstantiationRef() != null) {
			final SadComponentInstantiation ci = assemblyController.getComponentInstantiationRef().getInstantiation();
			// first check to make sure start order is set to zero
			if (ci != null && ci.getStartOrder() != null && ci.getStartOrder() != BigInteger.ZERO) {
				TransactionalEditingDomain editingDomain = featureProvider.getDiagramTypeProvider().getDiagramBehavior().getEditingDomain();
				TransactionalCommandStack stack = (TransactionalCommandStack) editingDomain.getCommandStack();
				stack.execute(new RecordingCommand(editingDomain) {

					@Override
					protected void doExecute() {
						ci.setStartOrder(BigInteger.ZERO);
					}
				});
			}

			// remove assembly controller from list, it has already been updated
			componentInstantiationsInStartOrder.remove(assemblyController.getComponentInstantiationRef().getInstantiation());
		}

		// set start order
		for (final SadComponentInstantiation ci : componentInstantiationsInStartOrder) {
			// Don't update start order if it has not already been declared for this component
			if (ci.getStartOrder() != null) {
				startOrder = startOrder.add(BigInteger.ONE);

				// Only call the update if a change is needed
				if (ci.getStartOrder().intValue() != startOrder.intValue()) {
					final BigInteger newStartOrder = startOrder;
					TransactionalEditingDomain editingDomain = featureProvider.getDiagramTypeProvider().getDiagramBehavior().getEditingDomain();
					TransactionalCommandStack stack = (TransactionalCommandStack) editingDomain.getCommandStack();
					stack.execute(new RecordingCommand(editingDomain) {

						@Override
						protected void doExecute() {
							ci.setStartOrder(newStartOrder);

							// Force pictogram elements to update
							ComponentShape componentShape = (ComponentShape) DUtil.getPictogramElementForBusinessObject(diagram, ci, ComponentShape.class);
							UpdateContext context = new UpdateContext(componentShape);
							IUpdateFeature feature = featureProvider.getUpdateFeature(context);
							feature.execute(context);
						}
					});
				}
			}
		}
	}

	// returns the assembly controller for this waveform
	private static AssemblyController getAssemblyController(IFeatureProvider featureProvider, Diagram diagram) {
		final SoftwareAssembly sad = DUtil.getDiagramSAD(diagram);
		if (sad.getAssemblyController() != null && sad.getAssemblyController().getComponentInstantiationRef() != null) {
			return sad.getAssemblyController();
		}
		return null;

	}

	@Override
	public boolean canDirectEdit(IDirectEditingContext context) {
		PictogramElement pe = context.getPictogramElement();
		ComponentShape componentShape = (ComponentShape) DUtil.findContainerShapeParentWithProperty(pe, RHContainerShapeImpl.SHAPE_OUTER_CONTAINER);
		Object obj = getBusinessObjectForPictogramElement(componentShape);
		GraphicsAlgorithm ga = context.getGraphicsAlgorithm();

		// allow if we've selected the inner Text for the component
		if (obj instanceof SadComponentInstantiation && ga instanceof Text) {
			Text text = (Text) ga;
			for (Property prop : text.getProperties()) {
				if (prop.getValue().equals(RHContainerShapeImpl.GA_INNER_ROUNDED_RECTANGLE_TEXT)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int getEditingType() {
		return TYPE_TEXT;
	}

	@Override
	public String getInitialValue(IDirectEditingContext context) {
		PictogramElement pe = context.getPictogramElement();
		ComponentShape componentShape = (ComponentShape) DUtil.findContainerShapeParentWithProperty(pe, RHContainerShapeImpl.SHAPE_OUTER_CONTAINER);
		SadComponentInstantiation ci = (SadComponentInstantiation) getBusinessObjectForPictogramElement(componentShape);
		return ci.getUsageName();
	}

	@Override
	public void setValue(final String value, IDirectEditingContext context) {
		PictogramElement pe = context.getPictogramElement();
		ComponentShape componentShape = (ComponentShape) DUtil.findContainerShapeParentWithProperty(pe, RHContainerShapeImpl.SHAPE_OUTER_CONTAINER);
		final SadComponentInstantiation ci = (SadComponentInstantiation) getBusinessObjectForPictogramElement(componentShape);

		// editing domain for our transaction
		TransactionalEditingDomain editingDomain = getFeatureProvider().getDiagramTypeProvider().getDiagramBehavior().getEditingDomain();

		// Perform business object manipulation in a Command
		TransactionalCommandStack stack = (TransactionalCommandStack) editingDomain.getCommandStack();
		stack.execute(new RecordingCommand(editingDomain) {
			@Override
			protected void doExecute() {
				// set usage name
				ci.setUsageName(value);
			}
		});

		// perform update, redraw
		updatePictogramElement(componentShape);
	}

	/**
	 * Return all ComponentShape in Diagram (recursively)
	 * @param containerShape
	 * @return
	 */
	public static List<ComponentShape> getAllComponentShapes(ContainerShape containerShape) {
		List<ComponentShape> children = new ArrayList<ComponentShape>();
		if (containerShape instanceof ComponentShape) {
			children.add((ComponentShape) containerShape);
		} else {
			for (Shape s : containerShape.getChildren()) {
				if (s instanceof ContainerShape) {
					children.addAll(getAllComponentShapes((ContainerShape) s));
				}
			}
		}
		return children;
	}

	@Override
	public String getOuterTitle(EObject obj) {
		if (obj instanceof SadComponentInstantiation) {
			return getOuterTitle((SadComponentInstantiation) obj);
		}
		return null;
	}

	@Override
	public String getInnerTitle(EObject obj) {
		if (obj instanceof SadComponentInstantiation) {
			return getInnerTitle((SadComponentInstantiation) obj);
		}
		return null;
	}

	/**
	 * Provides the title of the outer shape
	 * @param ci
	 * @return
	 */
	public String getOuterTitle(SadComponentInstantiation ci) {
		try {
			return ci.getPlacement().getComponentFileRef().getFile().getSoftPkg().getName();
		} catch (NullPointerException e) {
			return "< Component Bad Reference >";
		}
	}

	/**
	 * Provides the title of the inner shape
	 * @param ci
	 * @return
	 */
	public String getInnerTitle(SadComponentInstantiation ci) {
		String usageName = ci.getUsageName();
		return (usageName != null) ? usageName : ci.getId();
	}

	@Override
	public EList<UsesPortStub> getUses(EObject obj) {
		if (obj instanceof SadComponentInstantiation) {
			return ((SadComponentInstantiation) obj).getUses();
		}
		return null;
	}

	@Override
	public EList<ProvidesPortStub> getProvides(EObject obj) {
		if (obj instanceof SadComponentInstantiation) {
			return ((SadComponentInstantiation) obj).getProvides();
		}
		return null;
	}

	@Override
	public ComponentSupportedInterfaceStub getInterface(EObject obj) {
		if (obj instanceof SadComponentInstantiation) {
			return ((SadComponentInstantiation) obj).getInterfaceStub();
		}
		return null;
	}

	@Override
	public String getOuterImageId() {
		return WaveformImageProvider.IMG_COMPONENT_PLACEMENT;
	}

	@Override
	public String getInnerImageId() {
		return WaveformImageProvider.IMG_COMPONENT_INSTANCE;
	}

	@Override
	public String getStyleForOuter() {
		return StyleUtil.OUTER_SHAPE;
	}

	@Override
	public String getStyleForInner() {
		return StyleUtil.COMPONENT_INNER;
	}

	protected String getStartOrderStyle(SadComponentInstantiation instantiation) {
		if (SoftwareAssembly.Util.isAssemblyController(instantiation)) {
			return StyleUtil.ASSEMBLY_CONTROLLER_ELLIPSE;
		} else {
			return StyleUtil.START_ORDER_ELLIPSE;
		}
	}

	protected String getStartOrderValue(SadComponentInstantiation instantiation) {
		if (instantiation.getStartOrder() == null) {
			return ComponentPattern.NO_START_ORDER_STRING;
		} else {
			return instantiation.getStartOrder().toString();
		}
	}

	/**
	 * Returns component, sad, and external ports. Order does matter.
	 */
	public List<EObject> getBusinessObjectsToLink(EObject componentInstantiation) {
		// get external ports
		ExternalPorts externalPorts = DUtil.getDiagramSAD(getDiagram()).getExternalPorts();

		// get sad from diagram, we need to link it to all shapes so the diagram will update when changes occur to
		// assembly controller and external ports
		List<EObject> businessObjectsToLink = new ArrayList<EObject>();
		final SoftwareAssembly sad = DUtil.getDiagramSAD(getDiagram());
		// ORDER MATTERS, CI must be first
		businessObjectsToLink.add(componentInstantiation);
		businessObjectsToLink.add(sad);
		if (externalPorts != null) {
			businessObjectsToLink.add(externalPorts);
		}

		return businessObjectsToLink;
	}

}
