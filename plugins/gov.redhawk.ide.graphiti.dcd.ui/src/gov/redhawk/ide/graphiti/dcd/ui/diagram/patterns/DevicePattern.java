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
package gov.redhawk.ide.graphiti.dcd.ui.diagram.patterns;

import gov.redhawk.core.graphiti.ui.util.StyleUtil;
import gov.redhawk.ide.graphiti.dcd.ext.DeviceShape;
import gov.redhawk.ide.graphiti.dcd.ext.RHDeviceGxFactory;
import gov.redhawk.ide.graphiti.dcd.ui.diagram.providers.NodeImageProvider;
import gov.redhawk.ide.graphiti.ui.diagram.util.DUtil;

import java.util.ArrayList;
import java.util.List;

import mil.jpeojtrs.sca.dcd.DcdComponentInstantiation;
import mil.jpeojtrs.sca.dcd.DeviceConfiguration;
import mil.jpeojtrs.sca.spd.SoftPkg;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.pattern.IPattern;

public class DevicePattern extends AbstractNodeComponentPattern implements IPattern {

	public DevicePattern() {
		super();
	}

	@Override
	public String getCreateName() {
		return "Device";
	}

	@Override
	protected boolean isInstantiationApplicable(DcdComponentInstantiation instantiation) {
		SoftPkg spd = instantiation.getPlacement().getComponentFileRef().getFile().getSoftPkg();
		if (spd == null) {
			return true;
		}

		String componentType = spd.getDescriptor().getComponent().getComponentType();

		String[] deviceTypes = { mil.jpeojtrs.sca.scd.ComponentType.DEVICE.getLiteral(), "loadabledevice", "executabledevice" };
		for (String deviceType : deviceTypes) {
			if (deviceType.equals(componentType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected DeviceShape createContainerShape() {
		return RHDeviceGxFactory.eINSTANCE.createDeviceShape();
	}

	/**
	 * Return all DeviceShape in Diagram (recursively)
	 * @param containerShape
	 * @return
	 */
	public static List<DeviceShape> getAllDeviceShapes(ContainerShape containerShape) {
		List<DeviceShape> children = new ArrayList<DeviceShape>();
		if (containerShape instanceof DeviceShape) {
			children.add((DeviceShape) containerShape);
		} else {
			for (Shape s : containerShape.getChildren()) {
				if (s instanceof ContainerShape) {
					children.addAll(getAllDeviceShapes((ContainerShape) s));
				}
			}
		}
		return children;
	}

	@Override
	protected String getOuterImageId() {
		return NodeImageProvider.IMG_COMPONENT_PLACEMENT;
	}

	@Override
	protected String getInnerImageId() {
		return NodeImageProvider.IMG_SCA_DEVICE;
	}

	@Override
	protected String getStyleForOuter() {
		return StyleUtil.OUTER_SHAPE;
	}

	@Override
	protected String getStyleForInner() {
		return StyleUtil.COMPONENT_INNER;
	}

	/**
	 * Returns device, dcd, ports. Order does matter.
	 */
	protected List<EObject> getBusinessObjectsToLink(EObject componentInstantiation) {
		// get dcd from diagram, we need to link it to all shapes so the diagram will update when changes occur
		List<EObject> businessObjectsToLink = new ArrayList<EObject>();
		DeviceConfiguration dcd = DUtil.getDiagramDCD(getDiagram());
		// ORDER MATTERS, CI must be first
		businessObjectsToLink.add(componentInstantiation);
		businessObjectsToLink.add(dcd);

		return businessObjectsToLink;
	}
}
