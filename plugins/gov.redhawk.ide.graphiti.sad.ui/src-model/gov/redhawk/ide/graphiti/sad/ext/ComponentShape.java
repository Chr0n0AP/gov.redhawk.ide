/**
 * This file is protected by Copyright.
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 *
 * This file is part of REDHAWK IDE.
 *
 * All rights reserved.  This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 *
 */
package gov.redhawk.ide.graphiti.sad.ext;

import gov.redhawk.ide.graphiti.ext.RHContainerShape;
import gov.redhawk.ide.graphiti.sad.ui.diagram.patterns.ComponentPattern;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Component Shape</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see gov.redhawk.ide.graphiti.sad.ext.RHSadGxPackage#getComponentShape()
 * @model
 * @generated
 */
public interface ComponentShape extends RHContainerShape {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model contextDataType="gov.redhawk.ide.graphiti.sad.ext.IAddContext" contextUnique="false" patternDataType="gov.redhawk.ide.graphiti.sad.ext.ComponentPattern" patternUnique="false"
	 * @generated
	 */
	void init(IAddContext context, ComponentPattern pattern);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Performs a layout on the contents of this shape
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
	 * @model dataType="gov.redhawk.ide.graphiti.sad.ext.Reason" unique="false" contextDataType="gov.redhawk.ide.graphiti.sad.ext.IUpdateContext" contextUnique="false" patternDataType="gov.redhawk.ide.graphiti.sad.ext.ComponentPattern" patternUnique="false"
	 * @generated
	 */
	Reason update(IUpdateContext context, ComponentPattern pattern);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Checks if shape requires an update.
	 * If update required returns Reason with true
	 * boolean value and message describing what needs to be updated
	 * <!-- end-model-doc -->
	 * @model dataType="gov.redhawk.ide.graphiti.sad.ext.Reason" unique="false" contextDataType="gov.redhawk.ide.graphiti.sad.ext.IUpdateContext" contextUnique="false" patternDataType="gov.redhawk.ide.graphiti.sad.ext.ComponentPattern" patternUnique="false"
	 * @generated
	 */
	Reason updateNeeded(IUpdateContext context, ComponentPattern pattern);

} // ComponentShape
