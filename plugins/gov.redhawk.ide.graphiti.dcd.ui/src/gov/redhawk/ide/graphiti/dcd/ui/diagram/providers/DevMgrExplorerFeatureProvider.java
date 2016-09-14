/**
 * This file is protected by Copyright.
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 *
 * This file is part of REDHAWK IDE.
 *
 * All rights reserved.  This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package gov.redhawk.ide.graphiti.dcd.ui.diagram.providers;

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IDeleteFeature;
import org.eclipse.graphiti.features.IReconnectionFeature;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.IReconnectionContext;

public class DevMgrExplorerFeatureProvider extends DCDGraphitiFeatureProvider {

	public DevMgrExplorerFeatureProvider(IDiagramTypeProvider diagramTypeProvider) {
		super(diagramTypeProvider);
	}

	@Override
	public IDeleteFeature getDeleteFeature(IDeleteContext context) {
		if (this.getClass().equals(DevMgrExplorerFeatureProvider.class)) {
			// Prevent delete in the explorer diagrams
			return null;
		} else {
			// Allow derived classes (like sandbox) to inherit the parent class's functionality
			return super.getDeleteFeature(context);
		}
	}

	@Override
	public IReconnectionFeature getReconnectionFeature(IReconnectionContext context) {
		// We don't currently support reconnect actions for runtime
		return null;
	}
}
