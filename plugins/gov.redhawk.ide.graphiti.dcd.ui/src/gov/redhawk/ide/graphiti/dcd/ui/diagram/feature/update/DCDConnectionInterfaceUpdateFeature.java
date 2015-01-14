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
package gov.redhawk.ide.graphiti.dcd.ui.diagram.feature.update;

import gov.redhawk.diagram.util.InterfacesUtil;
import gov.redhawk.ide.graphiti.dcd.ui.diagram.patterns.DCDConnectInterfacePattern;
import gov.redhawk.ide.graphiti.ui.diagram.util.DUtil;
import gov.redhawk.sca.dcd.validation.ConnectionsConstraint;
import mil.jpeojtrs.sca.dcd.DcdConnectInterface;

import org.eclipse.graphiti.features.IDeleteFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.IRemoveFeature;
import org.eclipse.graphiti.features.context.IRemoveContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.context.impl.DeleteContext;
import org.eclipse.graphiti.features.context.impl.RemoveContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;

public class DCDConnectionInterfaceUpdateFeature extends AbstractUpdateFeature {

	public DCDConnectionInterfaceUpdateFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public boolean canUpdate(IUpdateContext context) {
		Object obj = DUtil.getBusinessObject(context.getPictogramElement());

		if (obj != null && obj instanceof DcdConnectInterface) {
			return true;
		}
		return false;
	}

	@Override
	public IReason updateNeeded(IUpdateContext context) {
		Connection connectionPE = null;
		DcdConnectInterface sadConnectInterface = null;

		if (context.getPictogramElement() instanceof Connection) {
			connectionPE = (Connection) context.getPictogramElement();
		}
		if (DUtil.getBusinessObject(context.getPictogramElement()) instanceof DcdConnectInterface) {
			sadConnectInterface = (DcdConnectInterface) DUtil.getBusinessObject(context.getPictogramElement());
		}

		Reason requiresUpdate = internalUpdate(connectionPE, sadConnectInterface, getFeatureProvider(), false);

		return requiresUpdate;
	}

	@Override
	public boolean update(IUpdateContext context) {

		Connection connectionPE = (Connection) context.getPictogramElement();
		DcdConnectInterface sadConnectInterface = (DcdConnectInterface) DUtil.getBusinessObject(context.getPictogramElement());

		Reason updated = internalUpdate(connectionPE, sadConnectInterface, getFeatureProvider(), true);

		return updated.toBoolean();
	}

	/**
	 * Performs either an update or a check to determine if update is required.
	 * if performUpdate flag is true it will update the shape,
	 * otherwise it will return reason why update is required.
	 * @param ci
	 * @param performUpdate
	 * @return
	 */
	public Reason internalUpdate(Connection connectionPE, DcdConnectInterface connectInterface, IFeatureProvider featureProvider, boolean performUpdate) {

		boolean updateStatus = false;
		if (connectInterface == null) {
			return new Reason(false, "No updates required");
		}

		// imgConnectionDecorator
		ConnectionDecorator imgConnectionDecorator = (ConnectionDecorator) DUtil.findFirstPropertyContainer(connectionPE,
			DCDConnectInterfacePattern.SHAPE_IMG_CONNECTION_DECORATOR);
		// textConnectionDecorator
		ConnectionDecorator textConnectionDecorator = (ConnectionDecorator) DUtil.findFirstPropertyContainer(connectionPE,
			DCDConnectInterfacePattern.SHAPE_TEXT_CONNECTION_DECORATOR);

		// problem if either source or target not present, unless dealing with a findby element
		if ((connectInterface.getSource() == null || connectInterface.getTarget() == null)
			&& (connectInterface.getUsesPort().getFindBy() == null && (connectInterface.getProvidesPort() != null && connectInterface.getProvidesPort().getFindBy() == null))) {
			if (performUpdate) {
				updateStatus = true;
				// remove the connection (handles pe and business object)
				DeleteContext dc = new DeleteContext(connectionPE);
				IDeleteFeature deleteFeature = featureProvider.getDeleteFeature(dc);
				if (deleteFeature != null) {
					deleteFeature.delete(dc);
				}
			} else {
				String tmpMsg = "Target";
				if (connectInterface.getSource() == null) {
					tmpMsg = "Source";
				}
				return new Reason(true, tmpMsg + " endpoint for connection is null");
			}

			// check if not compatible draw error/warning decorator
		} else {
			// connection validation
			boolean uniqueConnection = ConnectionsConstraint.uniqueConnection(connectInterface);

			// don't check compatibility if connection includes a Find By element
			boolean compatibleConnection = InterfacesUtil.areCompatible(connectInterface.getSource(), connectInterface.getTarget());
			if (connectInterface.getUsesPort().getFindBy() != null
				|| (connectInterface.getProvidesPort() != null && connectInterface.getProvidesPort().getFindBy() != null)) {
				compatibleConnection = true;
			}

			if ((!compatibleConnection || !uniqueConnection) && (imgConnectionDecorator == null || textConnectionDecorator == null)) {
				if (performUpdate) {
					updateStatus = true;
					// Incompatible connection without an error decorator! We need to add an error decorator
					DCDConnectInterfacePattern.decorateConnection(connectionPE, connectInterface, getDiagram());
				} else {
					if (!uniqueConnection) {
						return new Reason(true, "Redundant connection");
					} else if (!compatibleConnection) {
						return new Reason(true, "Incompatible connection");
					} else {
						return new Reason(true, "Connection Error");
					}
				}
			} else if ((compatibleConnection || uniqueConnection) && (imgConnectionDecorator != null || textConnectionDecorator != null)) {
				if (performUpdate) {
					updateStatus = true;
					// Compatible connection with an inappropriate error decorator! We need to remove the error
					// decorator
					IRemoveContext rc = new RemoveContext(imgConnectionDecorator);
					IRemoveFeature removeFeature = featureProvider.getRemoveFeature(rc);
					if (removeFeature != null) {
						removeFeature.remove(rc);
					}
					rc = new RemoveContext(textConnectionDecorator);
					removeFeature = featureProvider.getRemoveFeature(rc);
					if (removeFeature != null) {
						removeFeature.remove(rc);
					}
				} else {
					return new Reason(true, "Error Decorator needs to be removed from Connection");
				}
			}

//			if (connectInterface.getProvidesPort().getFindBy() != null || connectInterface.getUsesPort().getFindBy() != null) {
//				SADConnectInterfacePattern.decorateConnection(connectionPE, connectInterface, getDiagram());
//			}
		}

		if (updateStatus && performUpdate) {
			return new Reason(true, "Update successful");
		}

		return new Reason(false, "No updates required");
	}
}
