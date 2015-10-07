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

import gov.redhawk.ide.graphiti.sad.ui.diagram.providers.WaveformImageProvider;
import gov.redhawk.ide.graphiti.ui.diagram.patterns.AbstractConnectInterfacePattern;
import gov.redhawk.ide.graphiti.ui.diagram.util.DUtil;
import mil.jpeojtrs.sca.partitioning.ConnectInterface;
import mil.jpeojtrs.sca.sad.SadConnectInterface;
import mil.jpeojtrs.sca.sad.SadFactory;
import mil.jpeojtrs.sca.sad.SoftwareAssembly;

import org.eclipse.graphiti.features.context.IAddConnectionContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;

public class SADConnectInterfacePattern extends AbstractConnectInterfacePattern {

	@Override
	public String getCreateImageId() {
		return WaveformImageProvider.IMG_CONNECTION;
	}

	/**
	 * Determines if a connection can be made.
	 */
	@Override
	public boolean canAdd(IAddContext context) {
		if (context instanceof IAddConnectionContext && context.getNewObject() instanceof SadConnectInterface) {
			return true;
		}
		return false;
	}

	@Override
	public boolean canStartConnection(ICreateConnectionContext context) {
		// Verify SAD is available
		final SoftwareAssembly sad = DUtil.getDiagramSAD(getDiagram());
		if (sad == null) {
			return false;
		}

		return super.canStartConnection(context);
	}

	/**
	 * Determines whether creation of an interface connection is possible between source and destination anchors.
	 * User can begin drawing connection from either direction.
	 * Source anchor of connection must be UsesPort.
	 * Target Anchor must be ConnectionTarget which is the parent class for a variety of types.
	 */
	@Override
	public boolean canCreate(ICreateConnectionContext context) {
		// get sad from diagram
		final SoftwareAssembly sad = DUtil.getDiagramSAD(getDiagram());
		if (sad == null) {
			return false;
		}

		return super.canCreate(context);
	}

	@Override
	protected ConnectInterface< ? , ? , ? > createConnectInterface() {
		return SadFactory.eINSTANCE.createSadConnectInterface();
	}

	@Override
	protected String createConnectionId() {
		SoftwareAssembly sad = DUtil.getDiagramSAD(getDiagram());
		return createConnectionId(sad.getConnections());
	}

	@Override
	protected void addConnectInterface(ConnectInterface< ? , ? , ? > connection) {
		SoftwareAssembly sad = DUtil.getDiagramSAD(getDiagram());

		// Create connections if necessary
		if (sad.getConnections() == null) {
			sad.setConnections(SadFactory.eINSTANCE.createSadConnections());
		}

		// Add to connections
		sad.getConnections().getConnectInterface().add((SadConnectInterface) connection);
	}

}
