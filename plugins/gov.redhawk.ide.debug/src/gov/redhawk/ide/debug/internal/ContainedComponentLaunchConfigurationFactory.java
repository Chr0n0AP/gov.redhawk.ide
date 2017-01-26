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
package gov.redhawk.ide.debug.internal;

import mil.jpeojtrs.sca.spd.SoftPkg;

public class ContainedComponentLaunchConfigurationFactory extends DefaultComponentLaunchConfigurationFactory {

	@Override
	public boolean supports(final SoftPkg spd, final String implId) {
		return SoftPkg.Util.isContainedComponent(spd.getImplementation(implId));
	}
}
