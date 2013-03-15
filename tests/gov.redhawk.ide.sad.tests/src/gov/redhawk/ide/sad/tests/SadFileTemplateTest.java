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

package gov.redhawk.ide.sad.tests;

import gov.redhawk.ide.sad.generator.newwaveform.GeneratorArgs;
import gov.redhawk.ide.sad.generator.newwaveform.SadFileTemplate;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;
import mil.jpeojtrs.sca.sad.SadPackage;
import mil.jpeojtrs.sca.sad.SoftwareAssembly;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.Test;

/**
 * A class to test {@link SadFileTemplate}.
 */
public class SadFileTemplateTest {

	/**
	 * Tests generating a PRF file
	 * 
	 * @throws IOException
	 */
	@Test
	public void test() throws IOException {
		// Generate XML using the template
		final SadFileTemplate prfTemplate = SadFileTemplate.create(null);
		final GeneratorArgs args = new GeneratorArgs();
		args.setSoftPkgId("MyID");
		args.setProjectName("MyName");
		final String prfContent = prfTemplate.generate(args);

		// Create an XML file with the content
		final File prfFile = TestUtils.createFile(prfContent, SadPackage.FILE_EXTENSION);

		// Try to create a model from the file
		final ResourceSet resourceSet = new ResourceSetImpl();
		final SoftwareAssembly assembly = SoftwareAssembly.Util.getSoftwareAssembly(resourceSet.getResource(URI.createFileURI(prfFile.toString()), true));
		Assert.assertEquals(args.getSoftPkgId(), assembly.getId());
		Assert.assertEquals(args.getProjectName(), assembly.getName());
		Assert.assertNotNull(assembly.getComponentFiles());
		Assert.assertNotNull(assembly.getPartitioning());
		Assert.assertNotNull(assembly.getAssemblyController());
	}
}
