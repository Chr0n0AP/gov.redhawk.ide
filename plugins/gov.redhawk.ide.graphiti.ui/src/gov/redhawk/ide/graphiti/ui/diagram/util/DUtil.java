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
package gov.redhawk.ide.graphiti.ui.diagram.util;

import gov.redhawk.diagram.util.InterfacesUtil;
import gov.redhawk.ide.graphiti.ext.RHContainerShape;
import gov.redhawk.ide.graphiti.ext.impl.RHContainerShapeImpl;
import gov.redhawk.ide.graphiti.ui.diagram.IDiagramUtilHelper;
import gov.redhawk.ide.graphiti.ui.diagram.features.layout.LayoutDiagramFeature;
import gov.redhawk.ide.graphiti.ui.diagram.patterns.AbstractFindByPattern;
import gov.redhawk.ide.graphiti.ui.diagram.wizards.SuperPortConnectionWizard;
import gov.redhawk.sca.efs.ScaFileSystemPlugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mil.jpeojtrs.sca.dcd.DeviceConfiguration;
import mil.jpeojtrs.sca.partitioning.ConnectInterface;
import mil.jpeojtrs.sca.partitioning.ConnectionTarget;
import mil.jpeojtrs.sca.partitioning.FindBy;
import mil.jpeojtrs.sca.partitioning.FindByStub;
import mil.jpeojtrs.sca.partitioning.ProvidesPortStub;
import mil.jpeojtrs.sca.partitioning.UsesPortStub;
import mil.jpeojtrs.sca.sad.HostCollocation;
import mil.jpeojtrs.sca.sad.SadConnectInterface;
import mil.jpeojtrs.sca.sad.SoftwareAssembly;
import mil.jpeojtrs.sca.util.ScaEcoreUtils;
import mil.jpeojtrs.sca.util.ScaResourceFactoryUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalCommandStack;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.datatypes.ILocation;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IDeleteFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IRemoveFeature;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.IPictogramElementContext;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.CustomContext;
import org.eclipse.graphiti.features.context.impl.DeleteContext;
import org.eclipse.graphiti.features.context.impl.RemoveContext;
import org.eclipse.graphiti.features.context.impl.UpdateContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.internal.datatypes.impl.DimensionImpl;
import org.eclipse.graphiti.mm.Property;
import org.eclipse.graphiti.mm.PropertyContainer;
import org.eclipse.graphiti.mm.algorithms.AbstractText;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.PictogramLink;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

public class DUtil { // SUPPRESS CHECKSTYLE INLINE

	// These are property key/value pairs that help us resize an existing shape by properly identifying
	// graphicsAlgorithms
	public static final String GA_TYPE = "GAType"; // key for gA types

	// Property key/value pairs help us identify Shapes to enable/disable user actions (move, resize, delete, remove
	// etc.)
	public static final String SHAPE_TYPE = "ShapeType"; // key for Shape types

	public static final String DIAGRAM_CONTEXT = "DiagramContext"; // key for Diagram contexts
	public static final String DIAGRAM_CONTEXT_DESIGN = "design";
	public static final String DIAGRAM_CONTEXT_LOCAL = "local";
	public static final String DIAGRAM_CONTEXT_TARGET_SDR = "target-sdr";
	public static final String DIAGRAM_CONTEXT_EXPLORER = "explorer";

	public static final int DIAGRAM_SHAPE_HORIZONTAL_PADDING = 100;
	public static final int DIAGRAM_SHAPE_SIBLING_VERTICAL_PADDING = 5;
	public static final int DIAGRAM_SHAPE_ROOT_VERTICAL_PADDING = 50;

	// do this because we need to pass it to layout diagram, assumes we already have shapes drawn of a certain
	// size and that we are just moving them
	public static IDimension calculateDiagramBounds(Diagram diagram) {

		// get all shapes in diagram, components, findby's etc
		List<RHContainerShape> rootShapes = new ArrayList<RHContainerShape>();
		for (Shape s : diagram.getChildren()) {
			// RHContainerShape
			if (s instanceof RHContainerShape) {
				RHContainerShape rhContainerShape = (RHContainerShape) s;
				// if it has no provides ports or it has ports WITH NO CONNECTIONS than its a root in the tree
				if (rhContainerShape.getProvidesPortStubs() != null
					&& (rhContainerShape.getProvidesPortStubs().size() < 1 || getIncomingConnectionsContainedInContainerShape(rhContainerShape).size() < 1)) {
					rootShapes.add(rhContainerShape);
				}
			}
		}

		// combine dimensions of each root tree to determine total dimension required to house all shapes in diagram
		int height = 0;
		int width = 0;
		for (RHContainerShape s : rootShapes) {
			IDimension childTreeDimension = calculateTreeDimensions(s);
			height += childTreeDimension.getHeight();
			// use largest width
			width = (childTreeDimension.getWidth() > width) ? childTreeDimension.getWidth() : width;
		}
		// add padding between roots
		height += DIAGRAM_SHAPE_ROOT_VERTICAL_PADDING * rootShapes.size() - 1;

		return new DimensionImpl(width, height);
	}

	/**
	 * Returns dimensions required to contain all shapes aligned in a horizontal tree diagram
	 * beginning with the provided root shape: rhContainerShape
	 * @param rhContainerShape
	 * @return
	 */
	public static IDimension calculateTreeDimensions(RHContainerShape rhContainerShape) {
		return calculateTreeDimensions(rhContainerShape, new HashSet<RHContainerShape>());
	}

	/**
	 * Internal method used by {@link #calculateTreeDimensions(RHContainerShape)}.
	 * @param rhContainerShape
	 * @return
	 */
	private static IDimension calculateTreeDimensions(RHContainerShape rhContainerShape, Set<RHContainerShape> visitedShapes) {
		// Keep track of the shape we're visiting; if we've been here, we're in a circular recursion
		if (!visitedShapes.add(rhContainerShape)) {
			return null;
		}

		int height = rhContainerShape.getGraphicsAlgorithm().getHeight();
		int width = rhContainerShape.getGraphicsAlgorithm().getWidth();
		int childWidth = 0;
		int childHeight = 0;

		List<Connection> outs = getOutgoingConnectionsContainedInContainerShape(rhContainerShape);
		for (Connection conn : outs) {
			RHContainerShape targetRHContainerShape = ScaEcoreUtils.getEContainerOfType(conn.getEnd(), RHContainerShape.class);
			IDimension childDimension = calculateTreeDimensions(targetRHContainerShape, visitedShapes);
			if (childDimension == null) {
				continue;
			}
			childHeight += childDimension.getHeight() + DIAGRAM_SHAPE_SIBLING_VERTICAL_PADDING;
			// use largest width but don't add
			childWidth = (childDimension.getWidth() > childWidth) ? childDimension.getWidth() : childWidth;
		}
		if (outs.size() > 0) {
			width += childWidth + DIAGRAM_SHAPE_HORIZONTAL_PADDING;
		}
		// choose the largest of parent height or combined child height
		height = (childHeight > height) ? childHeight : height;

		return new DimensionImpl(width, height);
	}

	/**
	 * Return all incoming connections originating from within the provided ContainerShape
	 * @param containerShape
	 * @return
	 */
	public static List<Connection> getIncomingConnectionsContainedInContainerShape(ContainerShape containerShape) {
		List<Connection> connections = new ArrayList<Connection>();
		Diagram diagram = findDiagram(containerShape);
		for (Connection conn : diagram.getConnections()) {
			for (PictogramElement e : Graphiti.getPeService().getAllContainedPictogramElements(containerShape)) {
				if (e == conn.getEnd()) {
					connections.add(conn);
				}
			}
		}
		return connections;
	}

	/**
	 * Return all outgoing connections originating from within the provided ContainerShape
	 * @param containerShape
	 * @return
	 */
	public static List<Connection> getOutgoingConnectionsContainedInContainerShape(ContainerShape containerShape) {
		List<Connection> connections = new ArrayList<Connection>();
		Diagram diagram = findDiagram(containerShape);
		for (Connection conn : diagram.getConnections()) {
			for (PictogramElement e : Graphiti.getPeService().getAllContainedPictogramElements(containerShape)) {
				if (e == conn.getStart()) {
					connections.add(conn);
				}
			}
		}
		return connections;
	}

	/**
	 * Returns the SoftwareAssembly for the provided diagram
	 * @param featureProvider
	 * @param diagram
	 * @return
	 */
	public static SoftwareAssembly getDiagramSAD(Diagram diagram) {
		return (SoftwareAssembly) DUtil.getBusinessObject(diagram, SoftwareAssembly.class);
	}

	/**
	 * Returns the DeviceConfiguration for the provided diagram
	 * @param featureProvider
	 * @param diagram
	 * @return
	 */
	public static DeviceConfiguration getDiagramDCD(Diagram diagram) {
		return (DeviceConfiguration) DUtil.getBusinessObject(diagram, DeviceConfiguration.class);
	}

	/**
	 * @return All ports which are descendants of the specified shape in the diagram
	 */
	public static List<ContainerShape> getDiagramPorts(ContainerShape shape) {
		List<ContainerShape> ports = getDiagramProvidesPorts(shape);
		ports.addAll(getDiagramUsesPorts(shape));
		return ports;
	}

	/**
	 * @return All provides ports which are descendants of the specified shape in the diagram
	 */
	public static List<ContainerShape> getDiagramProvidesPorts(ContainerShape shape) {
		return getDiagramPorts(shape, RHContainerShapeImpl.SHAPE_PROVIDES_PORT_CONTAINER);
	}

	/**
	 * @return All uses ports which are descendants of the specified shape in the diagram
	 */
	public static List<ContainerShape> getDiagramUsesPorts(ContainerShape shape) {
		return getDiagramPorts(shape, RHContainerShapeImpl.SHAPE_USES_PORT_CONTAINER);
	}

	/**
	 * Finds all ports of the specified type which are descendants of the specified shape in the diagram.
	 * @param shape - The parent shape that you want to find ports of
	 * @param portType - property value of the desired port type
	 * @see {@link RHContainerShapeImpl} static property strings
	 */
	private static List<ContainerShape> getDiagramPorts(ContainerShape shape, String portType) {
		List<ContainerShape> portsList = new ArrayList<ContainerShape>();

		for (Shape child : shape.getChildren()) {
			String shapeType = Graphiti.getPeService().getPropertyValue(child, DUtil.SHAPE_TYPE);
			if (shapeType != null && portType.equals(shapeType)) {
				portsList.add((ContainerShape) child);
			} else if (child instanceof ContainerShape && !((ContainerShape) child).getChildren().isEmpty()) {
				portsList.addAll(getDiagramPorts((ContainerShape) child, portType));
			}
		}
		return portsList;
	}

	/**
	 * @returns the IEditorPart for the active editor
	 * Useful for getting the edit part for the GraphitiSadMultiPageScaEditor and the LocalGraphitiSadMultiPageScaEditor
	 */
	public static IEditorPart getActiveEditor() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	}

	public static Object[] getSelectedEditParts() {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (StructuredSelection) selection;
			return ss.toArray();
		}
		return null;
	}

	/**
	 * Returns true if the provided context contains a pictogram element with one of the provided property values.
	 * False otherwise.
	 * @param context
	 * @param propertyKeys
	 * @return
	 */
	public static boolean doesPictogramContainProperty(PictogramElement pe, String[] propertyValues) {
		if (pe != null && pe.getProperties() != null) {
			for (Property p : pe.getProperties()) {
				for (String propValue : propertyValues) {
					if (p.getValue().equals(propValue)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if the provided context contains a pictogram element with one of the provided property values.
	 * False otherwise.
	 * @param context
	 * @param propertyKeys
	 * @return
	 */
	public static boolean doesPictogramContainProperty(IPictogramElementContext context, String[] propertyValues) {
		PictogramElement pe = context.getPictogramElement();
		return doesPictogramContainProperty(pe, propertyValues);
	}

	/**
	 * Returns all of the shape children recursively
	 * @param diagramElement
	 * @return
	 */
	public static List<Shape> collectShapeChildren(Shape diagramElement) {

		List<Shape> children = new ArrayList<Shape>();
		children.add(diagramElement);
		// if containershape, collect children recursively
		if (diagramElement instanceof ContainerShape) {
			ContainerShape cs = (ContainerShape) diagramElement;
			for (Shape c : cs.getChildren()) {
				children.addAll(collectShapeChildren(c));
			}
		}
		return children;
	}

	/**
	 * Returns all of the children contained within the provided PropertyContainer and their children recursively.
	 * @param diagramElement
	 * @return
	 */
	public static List<PropertyContainer> collectPropertyContainerChildren(PropertyContainer diagramElement) {

		List<PropertyContainer> children = new ArrayList<PropertyContainer>();
		children.add(diagramElement);

		// if containershape, collect children recursively
		if (diagramElement instanceof ContainerShape) {
			ContainerShape cs = (ContainerShape) diagramElement;
			for (Shape c : cs.getChildren()) {
				children.addAll(collectPropertyContainerChildren(c));
			}
			for (Anchor a : cs.getAnchors()) {
				children.addAll(collectPropertyContainerChildren(a));
			}
			if (cs.getGraphicsAlgorithm() != null) {
				children.addAll(collectPropertyContainerChildren(cs.getGraphicsAlgorithm()));
			}
			// if containershape, collect children recursively
		} else if (diagramElement instanceof GraphicsAlgorithm) {
			GraphicsAlgorithm ga = (GraphicsAlgorithm) diagramElement;
			for (GraphicsAlgorithm c : ga.getGraphicsAlgorithmChildren()) {
				children.addAll(collectPropertyContainerChildren(c));
			}
		} else if (diagramElement instanceof Shape) {
			Shape shape = (Shape) diagramElement;
			children.add(shape.getGraphicsAlgorithm());
		} else if (diagramElement instanceof AnchorContainer) {
			AnchorContainer anchorContainer = (AnchorContainer) diagramElement;
			for (Anchor a : anchorContainer.getAnchors()) {
				children.addAll(collectPropertyContainerChildren(a));
			}
		} else if (diagramElement instanceof Anchor) {
			Anchor anchor = (Anchor) diagramElement;
			children.add(anchor.getGraphicsAlgorithm());
		}

		return children;
	}

	/**
	 * Remove Business object from all linked PictogramElement
	 * @param diagram
	 * @param eObject
	 */
	public static void removeBusinessObjectFromAllPictogramElements(Diagram diagram, EObject eObject) {
		// get pe with link to bo
		List<PictogramElement> pictogramElements = Graphiti.getLinkService().getPictogramElements(diagram, eObject);

		// remove link
		for (PictogramElement pe : pictogramElements) {
			pe.getLink().getBusinessObjects().remove(eObject);
		}
	}

	public static void addLink(IFeatureProvider featureProvider, PictogramElement pe, EObject eObject) {
		if (eObject == null) {
			return;
		}

		if (pe.getLink() == null) {
			featureProvider.link(pe, eObject);
		} else {
			pe.getLink().getBusinessObjects().add(eObject);
		}
	}

	public static void addLinks(IFeatureProvider featureProvider, PictogramElement pe, Collection< ? extends EObject> eObjects) {
		if (eObjects == null || eObjects.size() < 1) {
			return;
		}

		if (pe.getLink() == null) {
			featureProvider.link(pe, eObjects.toArray());
		} else {
			pe.getLink().getBusinessObjects().addAll(eObjects);
		}
	}

	/**
	 * Return first matched child with property value
	 * @param diagramElement
	 * @return
	 */
	public static PropertyContainer findFirstPropertyContainer(PropertyContainer diagramElement, String propertyValue) {

		PropertyContainer p = null;

		if (DUtil.isPropertyElementType(diagramElement, propertyValue)) {
			return diagramElement;
		}

		// if containershape, iterate through children recursively
		if (diagramElement instanceof ContainerShape) {
			ContainerShape cs = (ContainerShape) diagramElement;
			for (Shape c : cs.getChildren()) {
				p = findFirstPropertyContainer(c, propertyValue);
				if (p != null) {
					return p;
				}
			}
			if (cs.getGraphicsAlgorithm() != null) {
				p = findFirstPropertyContainer(cs.getGraphicsAlgorithm(), propertyValue);
				if (p != null) {
					return p;
				}
			}
			// if GraphicsAlgorithm, iterate through children recursively
		} else if (diagramElement instanceof GraphicsAlgorithm) {
			GraphicsAlgorithm ga = (GraphicsAlgorithm) diagramElement;
			for (GraphicsAlgorithm c : ga.getGraphicsAlgorithmChildren()) {
				p = findFirstPropertyContainer(c, propertyValue);
				if (p != null) {
					return p;
				}
			}
		} else if (diagramElement instanceof Shape) {
			Shape shape = (Shape) diagramElement;
			if (DUtil.isPropertyElementType(shape.getGraphicsAlgorithm(), propertyValue)) {
				return shape.getGraphicsAlgorithm();
			}
		}

		return null;
	}

	/**
	 * Returns the ancestor (parent chain) of the provided diagramElement with the provided PropertyContainer
	 * @param diagramElement
	 * @return
	 */
	public static ContainerShape findContainerShapeParentWithProperty(Shape shape, String propertyValue) {

		if (shape instanceof Diagram) {
			return null;
		}
		if (shape instanceof ContainerShape && DUtil.isPropertyElementType(shape, propertyValue)) {
			return (ContainerShape) shape;
		}
		if (DUtil.isPropertyElementType(shape.getContainer(), propertyValue)) {
			return shape.getContainer();
		}
		return findContainerShapeParentWithProperty(shape.getContainer(), propertyValue);

	}

	/**
	 * Returns the ancestor (parent chain) of the provided diagramElement with the provided PropertyContainer
	 * First checks self to see if it is a container with matching property
	 * @param diagramElement
	 * @return
	 */
	public static ContainerShape findContainerShapeParentWithProperty(PictogramElement pe, String propertyValue) {
		if (pe instanceof ContainerShape && DUtil.isPropertyElementType(pe, propertyValue)) {
			return (ContainerShape) pe;
		}
		PictogramElement peContainer = Graphiti.getPeService().getActiveContainerPe(pe);
		if (peContainer instanceof ContainerShape) {
			ContainerShape outerContainerShape = DUtil.findContainerShapeParentWithProperty((ContainerShape) peContainer, propertyValue);
			return outerContainerShape;
		}
		return null;
	}

	/**
	 * Returns list of ContainerShape in provided AreaContext with
	 * property key DiagramUtil.GA_TYPE and provided propertyValue
	 * @param containerShape
	 * @param context
	 * @return
	 */
	public static List<Shape> getContainersInArea(final ContainerShape containerShape, int width, int height, int x, int y, String propertyValue) {

		List<Shape> retList = new ArrayList<Shape>();

		EList<Shape> shapes = containerShape.getChildren();
		for (Shape s : shapes) {
			if (shapeExistsPartiallyInArea(s, width, height, x, y)) {
				retList.add(s);
			}
		}
		return retList;
	}

	/**
	 * Adjust children x/y so they remain in the same relative position after resize
	 * @param containerShape
	 * @param context
	 */
	public static void shiftChildrenRelativeToParentResize(ContainerShape containerShape, IResizeShapeContext context) {

		int widthDiff = containerShape.getGraphicsAlgorithm().getWidth() - context.getWidth();
		int heightDiff = containerShape.getGraphicsAlgorithm().getHeight() - context.getHeight();
		switch (context.getDirection()) {
		case (IResizeShapeContext.DIRECTION_NORTH_EAST):
			shiftChildrenYPositionUp(containerShape, heightDiff);
			break;
		case (IResizeShapeContext.DIRECTION_WEST):
		case (IResizeShapeContext.DIRECTION_SOUTH_WEST):
			shiftChildrenXPositionLeft(containerShape, widthDiff);
			break;
		case (IResizeShapeContext.DIRECTION_NORTH_WEST):
			shiftChildrenXPositionLeft(containerShape, widthDiff);
			shiftChildrenYPositionUp(containerShape, heightDiff);
			break;
		case (IResizeShapeContext.DIRECTION_NORTH): // handle top of box getting smaller
			shiftChildrenYPositionUp(containerShape, heightDiff);
			break;
		default:
			break;
		}
	}

	/**
	 * Shifts children of container x value to the left by specified amount
	 * Can be negative
	 * @param ga
	 * @param shiftLeftAmount
	 */
	private static void shiftChildrenXPositionLeft(ContainerShape containerShape, int shiftLeftAmount) {
		for (Shape s : containerShape.getChildren()) {
			GraphicsAlgorithm ga = s.getGraphicsAlgorithm();
			Graphiti.getGaService().setLocation(ga, ga.getX() - shiftLeftAmount, ga.getY());
		}
	}

	/**
	 * Shifts children of container Y value up by specified amount
	 * Can be negative
	 * @param ga
	 * @param shiftUpAmount
	 */
	private static void shiftChildrenYPositionUp(ContainerShape containerShape, int shiftUpAmount) {
		for (Shape s : containerShape.getChildren()) {
			GraphicsAlgorithm ga = s.getGraphicsAlgorithm();
			Graphiti.getGaService().setLocation(ga, ga.getX(), ga.getY() - shiftUpAmount);
		}
	}

	/**
	 * Returns list of ContainerShape outside of provided AreaContext on containerShape with
	 * property key DiagramUtil.GA_TYPE and provided propertyValue
	 * @param containerShape
	 * @param context
	 * @return
	 */
	public static List<Shape> getContainersOutsideArea(final ContainerShape containerShape, int width, int height, int x, int y, String propertyValue) {
		List<Shape> retList = new ArrayList<Shape>();

		EList<Shape> shapes = containerShape.getChildren();
		for (Shape s : shapes) {
			if (!shapeExistsPartiallyInArea(s, width, height, x, y)) {
				retList.add(s);
			}
		}
		return retList;
	}

	/**
	 * Return true if GraphicsAlgorithm exists within IAreaContext
	 * @param ga
	 * @param context
	 * @return
	 */
	public static boolean shapeExistsPartiallyInArea(final Shape s, int areaW, int areaH, int areaX, int areaY) {
		GraphicsAlgorithm ga = s.getGraphicsAlgorithm();
		ILocation sLoc = GraphitiUi.getUiLayoutService().getLocationRelativeToDiagram(s);
		int[] x = new int[4];
		int[] y = new int[4];
		// top left
		x[0] = sLoc.getX();
		y[0] = sLoc.getY();
		// top right
		x[1] = sLoc.getX() + ga.getWidth();
		y[1] = sLoc.getY();
		// bottom left
		x[2] = sLoc.getX();
		y[2] = sLoc.getY() + ga.getHeight();
		// bottom right
		x[3] = sLoc.getX() + ga.getWidth();
		y[3] = sLoc.getY() + ga.getHeight();

		// return true if any corner of s exists inside area
		for (int i = 0; i < x.length; i++) {
			if (xyExistInArea(x[i], y[i], areaW, areaH, areaX, areaY)) {
				return true;
			}
		}

		// return true if host collocation is inside shape
		if ((sLoc.getX() < areaX) && ((sLoc.getX() + ga.getWidth()) > (areaX + areaW)) && (sLoc.getY() < areaY)
			&& ((sLoc.getY() + ga.getHeight()) > (areaY + areaH))) {
			return true;
		}

		// return true if area is inside of shape
		if ((sLoc.getX() < areaX) && ((sLoc.getX() + ga.getWidth()) > (areaX + areaW)) && (sLoc.getY() < areaY)
			&& ((sLoc.getY() + ga.getHeight()) > (areaY + areaH))) {
			return true;
		}

		// return true if x area is outside of shape, but y is not
		if ((sLoc.getX() > areaX) && ((sLoc.getX() + ga.getWidth()) < (areaX + areaW)) && (sLoc.getY() < areaY)
			&& ((sLoc.getY() + ga.getHeight()) > (areaY + areaH))) {
			return true;
		}

		// return true if y area is outside of shape, but c is not
		if ((sLoc.getX() < areaX) && ((sLoc.getX() + ga.getWidth()) > (areaX + areaW)) && (sLoc.getY() > areaY)
			&& ((sLoc.getY() + ga.getHeight()) < (areaY + areaH))) {
			return true;
		}

		return false;
	}

	/**
	 * XY exists within xy area
	 * @param x
	 * @param y
	 * @param areaW
	 * @param areaH
	 * @param areaX
	 * @param areaY
	 * @return
	 */
	public static boolean xyExistInArea(int x, int y, int areaW, int areaH, int areaX, int areaY) {
		if (areaX <= x && (areaX + areaW) >= x && areaY <= y && (areaY + areaH) >= y) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the property container contains a property key {@link #GA_TYPE} or {@link #SHAPE_TYPE} with value
	 * <code>propertyValue</code>
	 * @param pc
	 * @param propertyValue
	 * @return
	 */
	public static boolean isPropertyElementType(PropertyContainer pc, String propertyValue) {
		if (pc != null) {
			for (Property p : pc.getProperties()) {
				if ((GA_TYPE.equals(p.getKey()) || SHAPE_TYPE.equals(p.getKey())) && propertyValue.equals(p.getValue())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns all ContainerShapes with the provided property value
	 * @param containerShape
	 * @param propertyValue
	 * @return
	 */
	public static List<ContainerShape> getAllContainerShapes(ContainerShape containerShape, String propertyValue) {
		List<ContainerShape> children = new ArrayList<ContainerShape>();
		if (containerShape instanceof ContainerShape && isPropertyElementType(containerShape, propertyValue)) {
			children.add(containerShape);
		} else {
			for (Shape s : containerShape.getChildren()) {
				if (s instanceof ContainerShape) {
					children.addAll(getAllContainerShapes((ContainerShape) s, propertyValue));
				}
			}
		}
		return children;
	}

	/**
	 * Returns true if Pictogram Link contains an object of the provided Class
	 * @param <T>
	 * @param link
	 * @param cls
	 * @return
	 */
	public static < T > boolean doesLinkContainObjectTypeInstance(PictogramLink link, Class<T> cls) {
		if (link != null) {
			for (EObject eObj : link.getBusinessObjects()) {
				if (cls.isInstance(eObj)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Add PictogramElement via feature for the provided object.
	 * Relies on the framework determining which feature should be used and whether it can be added to diagram
	 * @param featureProvider
	 * @param object
	 * @return
	 */
	public static PictogramElement addShapeViaFeature(IFeatureProvider featureProvider, ContainerShape targetContainer, Object object) {
		AddContext addContext = new AddContext();
		addContext.setNewObject(object);
		addContext.setTargetContainer(targetContainer);
		addContext.setX(0);
		addContext.setY(0);
		IAddFeature addFeature = featureProvider.getAddFeature(addContext);
		if (addFeature.canAdd(addContext)) {
			return addFeature.add(addContext);
		}
		return null;
	}

	/**
	 * Update PictogramElement via feature
	 * Relies on the framework determining which feature should be used and whether it can be added to diagram
	 * @param featureProvider
	 * @param pe
	 * @return
	 */
	public static boolean updateShapeViaFeature(IFeatureProvider featureProvider, Diagram diagram, PictogramElement pe) {
		UpdateContext updateContext = new UpdateContext(pe);
		IUpdateFeature updateFeature = featureProvider.getUpdateFeature(updateContext);
		if (updateFeature.canUpdate(updateContext)) {
			return updateFeature.update(updateContext);
		}
		return false;
	}

	/**
	 * Add PictogramElement Connection via feature for the provided object and anchors.
	 * Relies on the framework determining which feature should be used and whether it can be added to diagram
	 * @param featureProvider
	 * @param object
	 * @param sourceAnchor
	 * @param targetAnchor
	 * @return
	 */
	public static PictogramElement addConnectionViaFeature(IFeatureProvider featureProvider, Object object, Anchor sourceAnchor, Anchor targetAnchor) {
		AddConnectionContext addConnectionContext = new AddConnectionContext(sourceAnchor, targetAnchor);
		addConnectionContext.setNewObject(object);
		IAddFeature addFeature = featureProvider.getAddFeature(addConnectionContext);
		if (addFeature.canAdd(addConnectionContext)) {
			return addFeature.add(addConnectionContext);
		}
		return null;
	}

	/**
	 * Returns Business object of specified class type if it exists
	 * @param pe
	 * @param cls
	 * @return
	 */
	public static < T > T getBusinessObject(PictogramElement pe, Class<T> cls) {
		if (pe != null && pe.getLink() != null) {
			for (EObject eObj : pe.getLink().getBusinessObjects()) {
				if (cls.isInstance(eObj)) {
					return cls.cast(eObj);
				}
			}
		}
		return null;
	}

	/**
	 * Examines a list of PictogramElements (pes) and ensures there is an associated object in the model (objects).
	 * If the PictogramElement has an associated object than it is updated (if necessary) otherwise the PictogramElement
	 * is removed
	 * @param pes
	 * @param objects
	 * @param objectClass
	 * @param pictogramLabel
	 * @param featureProvider
	 * @param performUpdate
	 * @return
	 */
	public static Reason removeUpdatePictogramElement(List<PictogramElement> pes, List<EObject> objects, Class< ? > objectClass, String pictogramLabel,
		IFeatureProvider featureProvider, boolean performUpdate) {

		boolean updateStatus = false;

		// update PictogramElements if in model, if not in model remove from diagram
		for (Iterator<PictogramElement> peIter = pes.iterator(); peIter.hasNext();) {
			// in model?
			PictogramElement pe = peIter.next();
			boolean found = false;
			for (Object obj : objects) {
				if (obj.equals(DUtil.getBusinessObject(pe, objectClass))) {
					found = true;
					// update Shape
					featureProvider.updateIfPossibleAndNeeded(new UpdateContext(pe));
				}
			}
			if (!found) {
				// wasn't found, deleting shape
				if (performUpdate) {
					updateStatus = true;
					// delete shape
					peIter.remove();
					// TODO: if we use DeleteContext it prompts user, if we use EcoreUtil.delete there is no prompt
					RemoveContext rc = new RemoveContext(pe);
					IRemoveFeature removeFeature = featureProvider.getRemoveFeature(rc);
					if (removeFeature != null) {
						removeFeature.remove(rc);
					}
				} else {
					return new Reason(true, "A " + pictogramLabel + " in diagram no longer has an associated business object");
				}
			}
		}

		if (updateStatus && performUpdate) {
			return new Reason(true, "Update successful");
		}

		return new Reason(false, "No updates required");
	}

	/**
	 * Remove connections from the diagram that are missing start/end points.
	 * Connections in the diagram may no longer have start/end points.
	 * They may have been deleted which will cause the connection to point to random places in the diagram.
	 * @param pes
	 * @param objects
	 * @param objectClass
	 * @param pictogramLabel
	 * @param featureProvider
	 * @param performUpdate
	 * @return
	 */
	public static Reason removeConnectionsWithoutEndpoints(List<Connection> connections, List<SadConnectInterface> sadConnectInterfaces,
		IFeatureProvider featureProvider, boolean performUpdate) {

		boolean updateStatus = false;

		// update PictogramElements if in model, if not in model remove from diagram
		for (Iterator<Connection> connIter = connections.iterator(); connIter.hasNext();) {
			Connection conn = connIter.next();
			if (conn.getStart() == null || conn.getEnd() == null) {
				// endpoint missing, delete connection
				if (performUpdate) {
					updateStatus = true;
					// delete shape
					connIter.remove();
					DeleteContext dc = new DeleteContext(conn);
					IDeleteFeature deleteFeature = featureProvider.getDeleteFeature(dc);
					if (deleteFeature != null) {
						deleteFeature.delete(dc);
					}
				} else {
					return new Reason(true, "A connection in diagram is missing either a start or end point");
				}
			}
		}

		// update model if there are references to components that no longer exist
		for (Iterator<SadConnectInterface> connIter = sadConnectInterfaces.iterator(); connIter.hasNext();) {

			// delete connection in model if
			// uses port is present but the referenced component isn't
			// provides port is present but references component isn't
			SadConnectInterface conn = connIter.next();
			if ((conn.getUsesPort() != null && conn.getUsesPort().getComponentInstantiationRef() != null && conn.getUsesPort().getComponentInstantiationRef().getInstantiation() == null)
				|| (conn.getProvidesPort() != null && conn.getProvidesPort().getComponentInstantiationRef() != null && conn.getProvidesPort().getComponentInstantiationRef().getInstantiation() == null)) {

				// endpoint missing, delete connection
				if (performUpdate) {
					updateStatus = true;
					// delete connection
					connIter.remove();
					EcoreUtil.delete(conn, true);
				} else {
					return new Reason(true, "A connection in model is missing reference to component");
				}
			}
		}

		if (updateStatus && performUpdate) {
			return new Reason(true, "Update successful");
		}

		return new Reason(false, "No updates required");
	}

	/**
	 * Examines a list of Shapes and ensures there is an associated object in the model (objects).
	 * If the Shape has an associated object than it is updated (if necessary) otherwise the Shape is removed
	 * Next if there are new objects that do not have Shapes, then a new Shape is added using
	 * the features associated the provided object type.
	 * @param shapes
	 * @param objects
	 * @param pictogramLabel
	 * @param featureProvider
	 * @param performUpdate
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Reason addRemoveUpdateShapes(List<Shape> shapes, List<EObject> objects, Class objectClass, String pictogramLabel, Diagram diagram,
		IFeatureProvider featureProvider, boolean performUpdate) {

		boolean updateStatus = false;

		// remove shapes on diagram if no longer in model, update all other shapes if necessary
		Reason updateShapesReason = removeUpdatePictogramElement((List<PictogramElement>) (List< ? >) shapes, objects, objectClass, pictogramLabel,
			featureProvider, performUpdate);
		if (!performUpdate && updateShapesReason.toBoolean()) {
			return updateShapesReason;
		} else if (updateShapesReason.toBoolean() == true) {
			updateStatus = true;
		}

		// add Shapes found in model, but not in diagram
		for (Object obj : objects) {
			// in diagram
			boolean found = false;
			for (Shape pe : shapes) {
				if (obj.equals(DUtil.getBusinessObject(pe, objectClass))) {
					found = true;
				}
			}
			if (!found) {
				// wasn't found, add shape
				if (performUpdate) {
					updateStatus = true;
					// add shape
					DUtil.addShapeViaFeature(featureProvider, diagram, obj);
				} else {
					return new Reason(true, "A " + pictogramLabel + " in model isn't displayed in diagram");
				}
			}
		}

		if (updateStatus && performUpdate) {
			return new Reason(true, "Update successful");
		}

		return new Reason(false, "No updates required");
	}

	/**
	 * Lookup SourceAnchor for connection. Examines uses ports on Components as well as FindBys
	 * @param sadConnectInterface
	 * @param diagram
	 * @return
	 */
	public static Anchor lookupSourceAnchor(ConnectInterface< ? , ? , ? > sadConnectInterface, Diagram diagram) {
		// lookup sourceAnchor
		PictogramElement sourceAnchorPe = DUtil.getPictogramElementForBusinessObject(diagram, sadConnectInterface.getSource(), Anchor.class);
		if (sourceAnchorPe != null) {
			return (Anchor) sourceAnchorPe;
		} else {
			// All components have been created so source Anchor is likely null because provides findBy
			// <uses><findby>something</findby></uses>
			// or something is wrong with the xml
			if (sadConnectInterface.getUsesPort() != null && sadConnectInterface.getUsesPort().getFindBy() != null) {
				FindBy findBy = (FindBy) sadConnectInterface.getUsesPort().getFindBy();

				// iterate through all FindByStub objects stored in diagram and set sourceAnchor that matches findBy
				List<RHContainerShape> findByContainerShapes = AbstractFindByPattern.getAllFindByShapes(diagram);
				for (RHContainerShape findByShape : findByContainerShapes) {
					FindByStub findByStub = (FindByStub) DUtil.getBusinessObject(findByShape);

					// determine findBy match
					if (AbstractFindByPattern.doFindByObjectsMatch(findBy, findByStub)) {

						// determine which usesPortStub we are targeting
						UsesPortStub usesPortStub = null;
						for (UsesPortStub p : findByStub.getUses()) {
							if (p != null && sadConnectInterface.getUsesPort().getUsesIdentifier() != null
								&& p.getName().equals(sadConnectInterface.getUsesPort().getUsesIdentifier())) {
								usesPortStub = p;
							}
						}

						// determine port anchor for FindByMatch
						if (usesPortStub != null) {
							PictogramElement pe = DUtil.getPictogramElementForBusinessObject(diagram, usesPortStub, Anchor.class);
							return (Anchor) pe;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Search for the FindByStub in the diagram given the findBy object
	 * @param findBy
	 * @param diagram
	 * @return
	 */
	public static FindByStub findFindByStub(FindBy findBy, Diagram diagram) {
		for (RHContainerShape findByShape : AbstractFindByPattern.getAllFindByShapes(diagram)) {
			FindByStub findByStub = (FindByStub) DUtil.getBusinessObject(findByShape);
			// determine findBy match
			if (findByStub != null && AbstractFindByPattern.doFindByObjectsMatch(findBy, findByStub)) {
				// it matches
				return findByStub;
			}
		}
		return null;
	}

	/**
	 * Returns the PictogramElement of class pictogramClass who is linked to business object eObj
	 * @param diagram
	 * @param eObj
	 * @param pictogramClass
	 * @return
	 */
	public static < T > T getPictogramElementForBusinessObject(Diagram diagram, EObject eObj, Class<T> pictogramClass) {
		List<PictogramElement> pes = Graphiti.getLinkService().getPictogramElements(diagram, eObj);
		if (pes != null && pes.size() > 0) {
			for (PictogramElement p : pes) {
				if (pictogramClass.isInstance(p)) {
					return pictogramClass.cast(p);
				}
			}
		}
		return null;
	}

	/**
	 * Return true if target is HostCollocation ContainerShape
	 * @param context
	 */
	public static HostCollocation getHostCollocation(final ContainerShape targetContainerShape) {
		if (targetContainerShape instanceof ContainerShape) {
			if (targetContainerShape.getLink() != null && targetContainerShape.getLink().getBusinessObjects() != null) {
				for (EObject obj : targetContainerShape.getLink().getBusinessObjects()) {
					if (obj instanceof HostCollocation) {
						return (HostCollocation) obj;
					}
				}
			}
		}
		return null;
	}

	// convenient method for getting diagram for a ContainerShape
	public static Diagram findDiagram(ContainerShape containerShape) {
		return Graphiti.getPeService().getDiagramForShape(containerShape);
	}

	// convenient method for getting business object for PictogramElement
	public static EObject getBusinessObject(PictogramElement pe) {
		return GraphitiUi.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);
	}

	public static URI getDiagramResourceURI(final IDiagramUtilHelper options, final Resource resource) throws IOException {
		if (resource != null) {
			final URI uri = resource.getURI();
			if (uri.isPlatformResource()) {
				final IFile file = options.getResource(resource);
				return DUtil.getRelativeDiagramResourceURI(options, file);
			} else {
				return DUtil.getTemporaryDiagramResourceURI(options, uri);
			}
		}
		return null;
	}

	/**
	 * Initialize sad diagram.
	 * 
	 * @param b
	 */
	private static URI getRelativeDiagramResourceURI(final IDiagramUtilHelper options, final IFile file) {
		final IFile diagramFile = file.getParent().getFile(
			new Path(file.getName().substring(0, file.getName().length() - options.getSemanticFileExtension().length()) + options.getDiagramFileExtension()));
		final URI uri = URI.createPlatformResourceURI(diagramFile.getFullPath().toString(), true);
		return uri;
	}

	/**
	 * Initialize sad diagram.
	 * 
	 * @param b
	 * @throws IOException
	 */
	private static URI getTemporaryDiagramResourceURI(final IDiagramUtilHelper options, final URI uri) throws IOException {
		final String name = uri.lastSegment();
		String tmpName = "rh_" + name.substring(0, name.length() - options.getSemanticFileExtension().length());
		File tempDir = ScaFileSystemPlugin.getDefault().getTempDirectory();
		final File tempFile = File.createTempFile(tmpName, options.getDiagramFileExtension(), tempDir);
		tempFile.deleteOnExit();

		final URI retVal = URI.createURI(tempFile.toURI().toString());

		return retVal;
	}

	/**
	 * 
	 */
	public static void initializeDiagramResource(final IDiagramUtilHelper options, final String diagramTypeId, final String diagramTypeProviderId,
		final URI diagramURI, final Resource sadResource) throws IOException, CoreException {
		if (diagramURI.isPlatform()) {
			final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(diagramURI.toPlatformString(true)));

			file.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());

			if (!file.exists()) {
				final IWorkspaceRunnable operation = new IWorkspaceRunnable() {

					@Override
					public void run(final IProgressMonitor monitor) throws CoreException {
						final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
						try {
							DUtil.populateDiagram(options, diagramTypeId, diagramTypeProviderId, diagramURI, sadResource, buffer);
						} catch (final IOException e) {
							// PASS
						}
						file.create(new ByteArrayInputStream(buffer.toByteArray()), true, monitor);
					}

				};
				final ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRuleFactory().createRule(file);

				ResourcesPlugin.getWorkspace().run(operation, rule, 0, null);
			}
		} else {
			DUtil.populateDiagram(options, diagramTypeId, diagramTypeProviderId, diagramURI, sadResource, null);
		}
	}

	// creates new diagram from provided model resource
	private static void populateDiagram(final IDiagramUtilHelper options, final String diagramTypeId, final String diagramTypeProviderId, final URI diagramURI,
		final Resource resource, final OutputStream buffer) throws IOException {

		// Create a resource set
		final ResourceSet resourceSet = ScaResourceFactoryUtil.createResourceSet();

		// Create a resource for this file.
		final Resource diagramResource = resourceSet.createResource(diagramURI);

		// extract name for diagram from uri
		final String diagramName = diagramURI.lastSegment();

		// create diagram
		Diagram diagram = Graphiti.getPeCreateService().createDiagram(diagramTypeId, diagramName, true);
		diagramResource.getContents().add(diagram);

		// TODO:we will want to move this logic somewhere else
		IDiagramTypeProvider dtp = GraphitiUi.getExtensionManager().createDiagramTypeProvider(diagram, diagramTypeProviderId);
		IFeatureProvider featureProvider = dtp.getFeatureProvider();

		// iterate over each component, both in an out of host collocations
		// passing in assembly controller and external ports information

		// WE WANT OUR UPDATE diagram capabilities to handle things like this though right?

		if (buffer != null) {
			diagramResource.save(buffer, options.getSaveOptions());
		} else {
			diagramResource.save(options.getSaveOptions());
		}
	}

	/**
	 * Determines if this is a runtime diagram.
	 * @param diagram
	 * @return
	 */
	public static boolean isDiagramRuntime(final Diagram diagram) {
		return getDiagramContext(diagram).equals(DIAGRAM_CONTEXT_LOCAL) || getDiagramContext(diagram).equals(DIAGRAM_CONTEXT_EXPLORER);
	}

	/**
	 * Determines if the diagram is running in explorer mode.
	 */
	public static boolean isDiagramExplorer(final Diagram diagram) {
		return getDiagramContext(diagram).equals(DIAGRAM_CONTEXT_EXPLORER);
	}

	/**
	 * Determines if the diagram is a design-time diagram for a file in the target SDR (usually these editors are
	 * read-only).
	 * @param diagram
	 * @return
	 */
	public static boolean isDiagramTargetSdr(final Diagram diagram) {
		return getDiagramContext(diagram).equals(DIAGRAM_CONTEXT_TARGET_SDR);
	}

	/**
	 * Determines if the diagram is a design-time diagram for file in the workspace.
	 * @param diagram
	 * @return
	 */
	public static boolean isDiagramWorkpace(final Diagram diagram) {
		return getDiagramContext(diagram).equals(DIAGRAM_CONTEXT_DESIGN);
	}

	/**
	 * Determines if the diagram is read-only (only applies to design-time).
	 * @param diagram
	 * @return
	 */
	public static boolean isDiagramReadOnly(Diagram diagram) {
		return isDiagramTargetSdr(diagram);
	}

	/**
	 * Returns true if the portContainer is a super port
	 * @param portContainer - The port container to be tested
	 * @return
	 */
	public static boolean isSuperPort(ContainerShape portContainer) {
		boolean isSuperProvides = DUtil.doesPictogramContainProperty(portContainer, new String[] { RHContainerShapeImpl.SUPER_PROVIDES_PORTS_RECTANGLE });
		boolean isSuperUses = DUtil.doesPictogramContainProperty(portContainer, new String[] { RHContainerShapeImpl.SUPER_USES_PORTS_RECTANGLE });
		return (isSuperProvides || isSuperUses);
	}

	/**
	 * Returns the property value that indicates the mode the diagram is operating in.
	 * @param diagram
	 */
	public static String getDiagramContext(Diagram diagram) {
		return Graphiti.getPeService().getPropertyValue(diagram, DIAGRAM_CONTEXT);
	}

	public static void layout(DiagramEditor diagramEditor) {
		Diagram diagram = diagramEditor.getDiagramTypeProvider().getDiagram();
		if (isDiagramTargetSdr(diagram) || isDiagramRuntime(diagram)) {
			DiagramBehavior diagramBehavior = diagramEditor.getDiagramBehavior();
			IFeatureProvider featureProvider = diagramEditor.getDiagramTypeProvider().getFeatureProvider();

			final ICustomContext context = new CustomContext(new PictogramElement[] { diagram });
			ICustomFeature[] features = featureProvider.getCustomFeatures(context);
			for (final ICustomFeature feature : features) {
				if (feature instanceof LayoutDiagramFeature) {
					TransactionalEditingDomain ed = diagramBehavior.getEditingDomain();
					TransactionalCommandStack cs = (TransactionalCommandStack) ed.getCommandStack();
					cs.execute(new RecordingCommand(ed) {

						@Override
						protected void doExecute() {
							((LayoutDiagramFeature) feature).execute(context);
						}
					});
				}
			}
		}
	}

	/**
	 * If a provides port with the port name still exists, return the new anchor so the connection can be redrawn
	 * @param providesPortStubs
	 * @param oldPortName
	 * @return Anchor object that is associated with the port
	 */
	public static Anchor getProvidesAnchor(Diagram diagram, EList<ProvidesPortStub> providesPortStubs, String oldPortName) {
		for (ProvidesPortStub port : providesPortStubs) {
			if (port.getName().equals(oldPortName)) {
				Anchor anchor = (Anchor) DUtil.getPictogramElementForBusinessObject(diagram, (EObject) port, Anchor.class);
				return anchor;
			}
		}
		return null;
	}

	/**
	 * If a uses port with the port name still exists, return the new anchor so the connection can be redrawn
	 * @param usesPortStubs
	 * @param oldPortName
	 * @return Anchor object that is associated with the port
	 */
	public static Anchor getUsesAnchor(Diagram diagram, EList<UsesPortStub> usesPortStubs, String oldPortName) {
		for (UsesPortStub port : usesPortStubs) {
			if (port.getName().equals(oldPortName)) {
				Anchor anchor = (Anchor) DUtil.getPictogramElementForBusinessObject(diagram, (EObject) port, Anchor.class);
				return anchor;
			}
		}
		return null;
	}

	/**
	 * Add source and target values to ConnectInterface and return
	 * assume UsesPortStub is the first anchor, ConnectionTarget for second anchor
	 * return null if either source or target not found
	 * @param anchor1
	 * @param anchor2
	 * @return
	 */
	public static ConnectInterface< ? , ? , ? > assignAnchorObjectsToConnection(ConnectInterface< ? , ? , ? > connectInterface, Anchor anchor1, Anchor anchor2) {

		if (anchor1 == null || anchor2 == null) {
			return null;
		}

		// get business objects for both anchors
		EList<EObject> anchorObjects1 = anchor1.getParent().getLink().getBusinessObjects();
		EList<EObject> anchorObjects2 = anchor2.getParent().getLink().getBusinessObjects();

		UsesPortStub source = null;
		ConnectionTarget target = null;

		// Check to ensure the first anchor is a UsesPortStub and the second is a ConnectionTarget
		if (anchorObjects1.size() == 0 || anchorObjects2.size() == 0) {
			return null;
		}
		for (EObject sourceObj : anchorObjects1) {
			if (!(sourceObj instanceof UsesPortStub)) {
				return null;
			}
		}
		for (EObject targetObj : anchorObjects2) {
			if (!(targetObj instanceof ConnectionTarget)) {
				return null;
			}
		}

		List<UsesPortStub> possibleSources = new ArrayList<UsesPortStub>();
		List<ConnectionTarget> possibleTargets = new ArrayList<ConnectionTarget>();

		if (anchorObjects1.size() == 1 && anchorObjects2.size() == 1) {
			// Always attempt to honor direct connections
			possibleSources.add((UsesPortStub) anchorObjects1.get(0));
			possibleTargets.add((ConnectionTarget) anchorObjects2.get(0));
		} else {
			// If either side is a super port, then build a list of possible connections
			for (EObject sourceObj : anchorObjects1) {
				for (EObject targetObj : anchorObjects2) {
					if (InterfacesUtil.areSuggestedMatch((UsesPortStub) sourceObj, targetObj)) {
						if (!possibleSources.contains(sourceObj)) {
							possibleSources.add((UsesPortStub) sourceObj);
						}
						if (!possibleTargets.contains(targetObj)) {
							possibleTargets.add((ConnectionTarget) targetObj);
						}
					}
				}
			}
		}

		if (possibleSources.size() > 1 || possibleTargets.size() > 1) {
			// If more than one connection is possible, display a wizard to complete the action
			SuperPortConnectionWizard wizard = new SuperPortConnectionWizard(possibleSources, possibleTargets);
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			WizardDialog dialog = new WizardDialog(shell, wizard);
			int retVal = dialog.open();

			if (retVal == Window.OK) {
				// Get user selections
				source = wizard.getPage().getSource();
				target = wizard.getPage().getTarget();
			} else {
				return null;
			}
		} else if (!possibleSources.isEmpty() && !possibleTargets.isEmpty()) {
			// If only one connection is possible, just go ahead and do it
			source = (UsesPortStub) possibleSources.get(0);
			target = (ConnectionTarget) possibleTargets.get(0);
		}

		// source
		connectInterface.setSource(source);
		// target
		connectInterface.setTarget(target);

		// only return if we have source/target set
		if (source == null || target == null) {
			return null;
		}

		return connectInterface;
	}

	/**
	 * Deletes a PictogramElement from its Diagram without doing a cross-reference search. The default PeService
	 * implementation of deletePictogramElement() recursively deletes all of the children of a container using
	 * EcoreUtil.delete(), which searches the entire resource set for references and removes them as well. This
	 * quickly becomes an expensive operation as the graph grows, and in our case, should be unnecessary.
	 * @param pe the pictogram element to delete
	 */
	public static void fastDeletePictogramElement(PictogramElement pe) {
		if (pe instanceof Connection) {
			DUtil.fastDeleteConnection((Connection) pe);
		} else {
			// Recursively remove any connections or links
			DUtil.unlinkPictogramElement(pe);

			// Remove directly from the parent; as long as there are no cross-references, this effectively deletes all
			// children as well
			ContainerShape container = (ContainerShape) pe.eContainer();
			container.getChildren().remove(pe);
		}
	}

	/**
	 * Deletes a Connection from its Diagram without doing a cross-reference search.
	 * @see {@link #deletePictogramElement(PictogramElement)}
	 * @param connection the connection to delete
	 */
	public static void fastDeleteConnection(Connection connection) {
		// Connections may be referenced by their endpoints, so remove them from the anchors if necessary
		Anchor end = connection.getEnd();
		if (end != null) {
			end.getIncomingConnections().remove(connection);
		}
		Anchor start = connection.getStart();
		if (start != null) {
			start.getOutgoingConnections().remove(connection);
		}

		Diagram diagram = connection.getParent();
		diagram.getPictogramLinks().remove(connection.getLink());
		diagram.getConnections().remove(connection);
	}

	/**
	 * Internal method to recursively removes all business object links from a PictogramElement and its children.
	 * Used by {@link #fastDeletePictogramElement(PictogramElement)}.
	 * @param pe the pictogram element to unlink
	 */
	private static void unlinkPictogramElement(PictogramElement pe) {
		if (pe instanceof AnchorContainer) {
			// Remove business objects links from anchors
			for (Anchor anchor : ((AnchorContainer) pe).getAnchors()) {
				DUtil.unlinkPictogramElement(anchor);
			}
		}

		// The diagram holds references to all the of links as well
		Diagram diagram = Graphiti.getPeService().getDiagramForPictogramElement(pe);
		diagram.getPictogramLinks().remove(pe.getLink());

		// Recursively unlink children
		if (pe instanceof ContainerShape) {
			for (Shape child : ((ContainerShape) pe).getChildren()) {
				DUtil.unlinkPictogramElement(child);
			}
		}
	}

	/**
	 * Calculates the width and height of the given text in the font of the given text. Unlike Graphiti's layout
	 * service, this method takes the text's style into account when getting the font.
	 * @param text the {@link AbstractText} to calculate the rendering size for
	 * @return
	 */
	public static IDimension calculateTextSize(AbstractText text) {
		return GraphitiUi.getUiLayoutService().calculateTextSize(text.getValue(), Graphiti.getGaService().getFont(text, true));
	}
}
