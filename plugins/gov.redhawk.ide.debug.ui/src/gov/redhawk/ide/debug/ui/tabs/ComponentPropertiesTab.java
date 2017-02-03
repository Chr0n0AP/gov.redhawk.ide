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
package gov.redhawk.ide.debug.ui.tabs;

import gov.redhawk.sca.launch.ScaLaunchConfigurationUtil;
import gov.redhawk.sca.launch.ui.ScaLauncherActivator;
import mil.jpeojtrs.sca.spd.SoftPkg;
import mil.jpeojtrs.sca.util.ScaResourceFactoryUtil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

public class ComponentPropertiesTab extends AbstractPropertiesTab {

	private static final String ID = "gov.redhawk.ide.debug.ui.ComponentPropertiesTab";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	protected SoftPkg loadProfile(final ILaunchConfiguration configuration) {
		try {
			URI spdUri = ScaLaunchConfigurationUtil.getProfileURI(configuration);
			final ResourceSet resourceSet = ScaResourceFactoryUtil.createResourceSet();
			final Resource resource = resourceSet.getResource(spdUri, true);
			return SoftPkg.Util.getSoftPkg(resource);
		} catch (final CoreException e) {
			ScaLauncherActivator.log(e);
		} catch (final Exception e) { // SUPPRESS CHECKSTYLE Logged Catch all exception
			ScaLauncherActivator.log(e);
		}
		return null;
	}

}
