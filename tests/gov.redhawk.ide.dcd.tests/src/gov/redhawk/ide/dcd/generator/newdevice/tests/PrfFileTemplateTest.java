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

package gov.redhawk.ide.dcd.generator.newdevice.tests;

import gov.redhawk.ide.dcd.generator.newdevice.GeneratorArgs;
import gov.redhawk.ide.dcd.generator.newdevice.PrfFileTemplate;
import gov.redhawk.ide.dcd.tests.TestUtils;

import java.io.File;
import java.io.IOException;

import mil.jpeojtrs.sca.prf.PrfPackage;
import mil.jpeojtrs.sca.prf.Properties;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * A class to test {@link PrfFileTemplate}.
 */
public class PrfFileTemplateTest {

	/**
	 * Tests generating a PRF file
	 * @throws IOException
	 */
	@Test
	public void testCreateDevicePRFFile() throws IOException {
		// Generate XML using the template
		final PrfFileTemplate prfTemplate = PrfFileTemplate.create(null);
		GeneratorArgs args = new GeneratorArgs();
		
		final String prfContent = prfTemplate.generate(args);

		// Create an XML file with the content
		final File prfFile = TestUtils.createFile(prfContent, PrfPackage.FILE_EXTENSION);

		// Try to create a model from the file
		final ResourceSet resourceSet = new ResourceSetImpl();
		final Properties props = Properties.Util.getProperties(resourceSet.getResource(URI.createFileURI(prfFile.toString()), true));
		Assert.assertNotNull(props);
	}
	
	/**
	 * Tests generating a PRF file with null argument
	 * @throws IOException
	 */
	@Test
	public void testCreateDevicePRFFile_NullArg() throws IOException {
		// Generate XML using the template
		final PrfFileTemplate prfTemplate = PrfFileTemplate.create(null);
		
		final String prfContent = prfTemplate.generate(null);

		// Create an XML file with the content
		final File prfFile = TestUtils.createFile(prfContent, PrfPackage.FILE_EXTENSION);

		// Try to create a model from the file
		final ResourceSet resourceSet = new ResourceSetImpl();
		final Properties props = Properties.Util.getProperties(resourceSet.getResource(URI.createFileURI(prfFile.toString()), true));
		Assert.assertNotNull(props);
	}
}
