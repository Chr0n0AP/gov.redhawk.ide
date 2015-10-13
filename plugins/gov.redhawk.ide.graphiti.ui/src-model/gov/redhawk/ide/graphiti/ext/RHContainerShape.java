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
package gov.redhawk.ide.graphiti.ext;

import gov.redhawk.ide.graphiti.ui.diagram.patterns.AbstractContainerPattern;
import java.util.List;
import mil.jpeojtrs.sca.partitioning.ProvidesPortStub;
import mil.jpeojtrs.sca.partitioning.UsesPortStub;
import mil.jpeojtrs.sca.sad.Port;
import org.eclipse.emf.common.util.EList;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>RH Container Shape</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link gov.redhawk.ide.graphiti.ext.RHContainerShape#isStarted <em>Started</em>}</li>
 *   <li>{@link gov.redhawk.ide.graphiti.ext.RHContainerShape#isEnabled <em>Enabled</em>}</li>
 *   <li>{@link gov.redhawk.ide.graphiti.ext.RHContainerShape#getIStatusSeverity <em>IStatus Severity</em>}</li>
 *   <li>{@link gov.redhawk.ide.graphiti.ext.RHContainerShape#isHasSuperPortsContainerShape <em>Has Super Ports Container Shape</em>}</li>
 *   <li>{@link gov.redhawk.ide.graphiti.ext.RHContainerShape#isHasPortsContainerShape <em>Has Ports Container Shape</em>}</li>
 *   <li>{@link gov.redhawk.ide.graphiti.ext.RHContainerShape#isHideUnusedPorts <em>Hide Unused Ports</em>}</li>
 * </ul>
 *
 * @see gov.redhawk.ide.graphiti.ext.RHGxPackage#getRHContainerShape()
 * @model
 * @generated
 */
public interface RHContainerShape extends ContainerShape {

	/**
	 * Returns the value of the '<em><b>Started</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * If the component is started
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Started</em>' attribute.
	 * @see #setStarted(boolean)
	 * @see gov.redhawk.ide.graphiti.ext.RHGxPackage#getRHContainerShape_Started()
	 * @model unique="false" transient="true"
	 * @generated
	 */
	boolean isStarted();

	/**
	 * Sets the value of the '{@link gov.redhawk.ide.graphiti.ext.RHContainerShape#isStarted <em>Started</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Started</em>' attribute.
	 * @see #isStarted()
	 * @generated
	 */
	void setStarted(boolean value);

	/**
	 * Returns the value of the '<em><b>Enabled</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Is the component enabled?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Enabled</em>' attribute.
	 * @see #setEnabled(boolean)
	 * @see gov.redhawk.ide.graphiti.ext.RHGxPackage#getRHContainerShape_Enabled()
	 * @model default="true" unique="false" transient="true"
	 * @generated
	 */
	boolean isEnabled();

	/**
	 * Sets the value of the '{@link gov.redhawk.ide.graphiti.ext.RHContainerShape#isEnabled <em>Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Enabled</em>' attribute.
	 * @see #isEnabled()
	 * @generated
	 */
	void setEnabled(boolean value);

	/**
	 * Returns the value of the '<em><b>IStatus Severity</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The severity of the component's status
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>IStatus Severity</em>' attribute.
	 * @see #setIStatusSeverity(int)
	 * @see gov.redhawk.ide.graphiti.ext.RHGxPackage#getRHContainerShape_IStatusSeverity()
	 * @model unique="false" transient="true"
	 * @generated
	 */
	int getIStatusSeverity();

	/**
	 * Sets the value of the '{@link gov.redhawk.ide.graphiti.ext.RHContainerShape#getIStatusSeverity <em>IStatus Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>IStatus Severity</em>' attribute.
	 * @see #getIStatusSeverity()
	 * @generated
	 */
	void setIStatusSeverity(int value);

	/**
	 * Returns the value of the '<em><b>Has Super Ports Container Shape</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Has Super Ports Container Shape</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Has Super Ports Container Shape</em>' attribute.
	 * @see #setHasSuperPortsContainerShape(boolean)
	 * @see gov.redhawk.ide.graphiti.ext.RHGxPackage#getRHContainerShape_HasSuperPortsContainerShape()
	 * @model unique="false"
	 * @generated
	 */
	boolean isHasSuperPortsContainerShape();

	/**
	 * Sets the value of the '{@link gov.redhawk.ide.graphiti.ext.RHContainerShape#isHasSuperPortsContainerShape <em>Has Super Ports Container Shape</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Has Super Ports Container Shape</em>' attribute.
	 * @see #isHasSuperPortsContainerShape()
	 * @generated
	 */
	void setHasSuperPortsContainerShape(boolean value);

	/**
	 * Returns the value of the '<em><b>Has Ports Container Shape</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Has Ports Container Shape</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Has Ports Container Shape</em>' attribute.
	 * @see #setHasPortsContainerShape(boolean)
	 * @see gov.redhawk.ide.graphiti.ext.RHGxPackage#getRHContainerShape_HasPortsContainerShape()
	 * @model unique="false"
	 * @generated
	 */
	boolean isHasPortsContainerShape();

	/**
	 * Sets the value of the '{@link gov.redhawk.ide.graphiti.ext.RHContainerShape#isHasPortsContainerShape <em>Has Ports Container Shape</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Has Ports Container Shape</em>' attribute.
	 * @see #isHasPortsContainerShape()
	 * @generated
	 */
	void setHasPortsContainerShape(boolean value);

	/**
	 * Returns the value of the '<em><b>Hide Unused Ports</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Hide Unused Ports</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Hide Unused Ports</em>' attribute.
	 * @see #setHideUnusedPorts(boolean)
	 * @see gov.redhawk.ide.graphiti.ext.RHGxPackage#getRHContainerShape_HideUnusedPorts()
	 * @model unique="false"
	 * @generated
	 */
	boolean isHideUnusedPorts();

	/**
	 * Sets the value of the '{@link gov.redhawk.ide.graphiti.ext.RHContainerShape#isHideUnusedPorts <em>Hide Unused Ports</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Hide Unused Ports</em>' attribute.
	 * @see #isHideUnusedPorts()
	 * @generated
	 */
	void setHideUnusedPorts(boolean value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model contextDataType="gov.redhawk.ide.graphiti.ext.IAddContext" contextUnique="false" patternDataType="gov.redhawk.ide.graphiti.ext.AbstractContainerPattern" patternUnique="false"
	 * @generated
	 */
	void init(IAddContext context, AbstractContainerPattern pattern);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model contextDataType="gov.redhawk.ide.graphiti.ext.IAddContext" contextUnique="false" patternDataType="gov.redhawk.ide.graphiti.ext.AbstractContainerPattern" patternUnique="false" externalPortsDataType="gov.redhawk.ide.graphiti.ext.List<gov.redhawk.ide.graphiti.ext.Port>" externalPortsUnique="false" externalPortsMany="false"
	 * @generated
	 */
	void init(IAddContext context, AbstractContainerPattern pattern, List<Port> externalPorts);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Returns providesPortsStubs business object list linked to getProvidesPortsContainerShape()
	 * <!-- end-model-doc -->
	 * @model kind="operation" unique="false" many="false"
	 * @generated
	 */
	EList<ProvidesPortStub> getProvidesPortStubs();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Returns providesPortsStubs business object list linked to getUsesPortsContainerShape()
	 * <!-- end-model-doc -->
	 * @model kind="operation" unique="false" many="false"
	 * @generated
	 */
	EList<UsesPortStub> getUsesPortStubs();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * performs a layout on the contents of this shape
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	void layout();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Updates the shape with supplied values
	 * <!-- end-model-doc -->
	 * @model dataType="gov.redhawk.ide.graphiti.ext.Reason" unique="false" contextDataType="gov.redhawk.ide.graphiti.ext.IUpdateContext" contextUnique="false" patternDataType="gov.redhawk.ide.graphiti.ext.AbstractContainerPattern" patternUnique="false" externalPortsDataType="gov.redhawk.ide.graphiti.ext.List<gov.redhawk.ide.graphiti.ext.Port>" externalPortsUnique="false" externalPortsMany="false"
	 * @generated
	 */
	Reason update(IUpdateContext context, AbstractContainerPattern pattern, List<Port> externalPorts);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Updates the shape with supplied values
	 * <!-- end-model-doc -->
	 * @model dataType="gov.redhawk.ide.graphiti.ext.Reason" unique="false" contextDataType="gov.redhawk.ide.graphiti.ext.IUpdateContext" contextUnique="false" patternDataType="gov.redhawk.ide.graphiti.ext.AbstractContainerPattern" patternUnique="false"
	 * @generated
	 */
	Reason update(IUpdateContext context, AbstractContainerPattern pattern);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Checks if shape requires an update.
	 * If update required returns Reason with true
	 * boolean value and message describing what needs to be updated
	 * <!-- end-model-doc -->
	 * @model dataType="gov.redhawk.ide.graphiti.ext.Reason" unique="false" contextDataType="gov.redhawk.ide.graphiti.ext.IUpdateContext" contextUnique="false" patternDataType="gov.redhawk.ide.graphiti.ext.AbstractContainerPattern" patternUnique="false" externalPortsDataType="gov.redhawk.ide.graphiti.ext.List<gov.redhawk.ide.graphiti.ext.Port>" externalPortsUnique="false" externalPortsMany="false"
	 * @generated
	 */
	Reason updateNeeded(IUpdateContext context, AbstractContainerPattern pattern, List<Port> externalPorts);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Checks if shape requires an update.
	 * If update required returns Reason with true
	 * boolean value and message describing what needs to be updated
	 * <!-- end-model-doc -->
	 * @model dataType="gov.redhawk.ide.graphiti.ext.Reason" unique="false" contextDataType="gov.redhawk.ide.graphiti.ext.IUpdateContext" contextUnique="false" patternDataType="gov.redhawk.ide.graphiti.ext.AbstractContainerPattern" patternUnique="false"
	 * @generated
	 */
	Reason updateNeeded(IUpdateContext context, AbstractContainerPattern pattern);

} // RHContainerShape

// END GENERATED CODE
