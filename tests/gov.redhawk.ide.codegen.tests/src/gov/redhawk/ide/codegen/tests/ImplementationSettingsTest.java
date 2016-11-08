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
  // BEGIN GENERATED CODE
package gov.redhawk.ide.codegen.tests;

import gov.redhawk.ide.codegen.CodegenFactory;
import gov.redhawk.ide.codegen.ImplementationSettings;
import gov.redhawk.ide.codegen.Property;
import gov.redhawk.ide.codegen.WaveDevSettings;

import org.junit.Assert;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Implementation Settings</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are tested:
 * <ul>
 *   <li>{@link gov.redhawk.ide.codegen.ImplementationSettings#getId() <em>Id</em>}</li>
 * </ul>
 * </p>
 * @generated
 */
public class ImplementationSettingsTest extends TestCase {

	/**
	 * The fixture for this Implementation Settings test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	protected ImplementationSettings fixture = null;
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(ImplementationSettingsTest.class);
	}

	/**
	 * Constructs a new Implementation Settings test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ImplementationSettingsTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this Implementation Settings test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(ImplementationSettings fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this Implementation Settings test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ImplementationSettings getFixture() {
		return fixture;
	}

	private ResourceSet resourceSet;
	private WaveDevSettings wave;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated NOT
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(CodegenFactory.eINSTANCE.createImplementationSettings());
		this.resourceSet = new ResourceSetImpl();
		this.wave = (WaveDevSettings) this.resourceSet.getResource(CodegenTestSuite.getURI("testFiles/sample.waveDev"), true).getEObject("/");
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#tearDown()
	 * @generated NOT
	 */
	@Override
	protected void tearDown() throws Exception {
		setFixture(null);
		this.wave = null;
		this.resourceSet = null;
	}

	/**
	 * Tests the '{@link gov.redhawk.ide.codegen.ImplementationSettings#getId() <em>Id</em>}' feature getter.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see gov.redhawk.ide.codegen.ImplementationSettings#getId()
	 * @generated NOT
	 */
	public void testGetId() {
		Assert.assertNotNull(this.wave);

		for (final ImplementationSettings implSettings : this.wave.getImplSettings().values()) {
			Assert.assertNotNull(implSettings);
			Assert.assertNotNull(implSettings.getId());
			Assert.assertEquals("DCE:726800a4-8af7-47b0-ba37-77952254b144", implSettings.getId());
			break;
		}
	}

	@SuppressWarnings("deprecation")
	public void test_parse() throws Exception {
		Assert.assertNotNull(this.wave);

		for (final ImplementationSettings implSettings : this.wave.getImplSettings().values()) {
			Assert.assertNotNull(implSettings);
			Assert.assertEquals("DCE:726800a4-8af7-47b0-ba37-77952254b144", implSettings.getId());
			Assert.assertEquals("cpp_python_cpp_impl1", implSettings.getName());
			Assert.assertEquals("cpp_python_cpp_impl1", implSettings.getOutputDir());
			Assert.assertEquals("src/", implSettings.getTemplate());
			Assert.assertEquals("gov.redhawk.ide.codegen.jet.cplusplus.CplusplusGenerator", implSettings.getGeneratorId());

			for (final Property prop : implSettings.getProperties()) {
				Assert.assertNotNull(prop);
				Assert.assertNotNull(prop.getId());
				Assert.assertEquals(prop.getValue(), "true");
			}
			break;
		}
	}

} //ImplementationSettingsTest
