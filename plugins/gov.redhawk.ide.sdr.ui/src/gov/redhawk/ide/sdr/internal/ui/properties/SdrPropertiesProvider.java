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
package gov.redhawk.ide.sdr.internal.ui.properties;

import gov.redhawk.ide.sdr.SdrRoot;
import gov.redhawk.ide.sdr.ui.SdrUiPlugin;
import gov.redhawk.sca.properties.Category;
import gov.redhawk.sca.properties.IPropertiesProvider;

import java.util.ArrayList;
import java.util.List;

import mil.jpeojtrs.sca.scd.ComponentType;

/**
 * @since 5.0
 */
public class SdrPropertiesProvider implements IPropertiesProvider {

	public SdrPropertiesProvider() {
		//Default Constructor
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Category> getCategories() {
		final List<Category> myList = new ArrayList<Category>();
		final SdrRoot targetSdr = SdrUiPlugin.getDefault().getTargetSdrRoot();
		targetSdr.load(null);
		myList.add(new ComponentCategory(targetSdr.getComponentsContainer().getComponents(), "Components", ComponentType.RESOURCE));
		myList.add(new ComponentCategory(targetSdr.getDevicesContainer().getComponents(), "Devices", ComponentType.DEVICE));
		myList.add(new ComponentCategory(targetSdr.getServicesContainer().getComponents(), "Services", ComponentType.SERVICE));
		myList.add(new ComponentCategory(targetSdr.getSharedLibrariesContainer().getComponents(), "Shared Libraries", ComponentType.RESOURCE));
		return myList;
	}

}
