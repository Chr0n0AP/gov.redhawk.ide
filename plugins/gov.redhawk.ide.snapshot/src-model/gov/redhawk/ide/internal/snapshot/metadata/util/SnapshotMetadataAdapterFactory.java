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
// BEGIN GENERATED CODE
package gov.redhawk.ide.internal.snapshot.metadata.util;

import gov.redhawk.ide.internal.snapshot.metadata.*;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see gov.redhawk.ide.internal.snapshot.metadata.SnapshotMetadataPackage
 * @generated
 */
public class SnapshotMetadataAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static SnapshotMetadataPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SnapshotMetadataAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = SnapshotMetadataPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject) object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SnapshotMetadataSwitch<Adapter> modelSwitch = new SnapshotMetadataSwitch<Adapter>() {
		@Override
		public Adapter caseCFDataType(CFDataType object) {
			return createCFDataTypeAdapter();
		}

		@Override
		public Adapter caseKeywordsType(KeywordsType object) {
			return createKeywordsTypeAdapter();
		}

		@Override
		public Adapter caseModel(Model object) {
			return createModelAdapter();
		}

		@Override
		public Adapter caseSRI(SRI object) {
			return createSRIAdapter();
		}

		@Override
		public Adapter caseTime(Time object) {
			return createTimeAdapter();
		}

		@Override
		public Adapter caseValue(Value object) {
			return createValueAdapter();
		}

		@Override
		public Adapter caseStructSeq(StructSeq object) {
			return createStructSeqAdapter();
		}

		@Override
		public Adapter caseStruct(Struct object) {
			return createStructAdapter();
		}

		@Override
		public Adapter caseSimple(Simple object) {
			return createSimpleAdapter();
		}

		@Override
		public Adapter defaultCase(EObject object) {
			return createEObjectAdapter();
		}
	};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject) target);
	}

	/**
	 * Creates a new adapter for an object of class '{@link gov.redhawk.ide.internal.snapshot.metadata.CFDataType <em>CF Data Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see gov.redhawk.ide.internal.snapshot.metadata.CFDataType
	 * @generated
	 */
	public Adapter createCFDataTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link gov.redhawk.ide.internal.snapshot.metadata.KeywordsType <em>Keywords Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see gov.redhawk.ide.internal.snapshot.metadata.KeywordsType
	 * @generated
	 */
	public Adapter createKeywordsTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link gov.redhawk.ide.internal.snapshot.metadata.Model <em>Model</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see gov.redhawk.ide.internal.snapshot.metadata.Model
	 * @generated
	 */
	public Adapter createModelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link gov.redhawk.ide.internal.snapshot.metadata.SRI <em>SRI</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see gov.redhawk.ide.internal.snapshot.metadata.SRI
	 * @generated
	 */
	public Adapter createSRIAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link gov.redhawk.ide.internal.snapshot.metadata.Time <em>Time</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see gov.redhawk.ide.internal.snapshot.metadata.Time
	 * @generated
	 */
	public Adapter createTimeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link gov.redhawk.ide.internal.snapshot.metadata.Value <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see gov.redhawk.ide.internal.snapshot.metadata.Value
	 * @generated
	 */
	public Adapter createValueAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link gov.redhawk.ide.internal.snapshot.metadata.StructSeq <em>Struct Seq</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see gov.redhawk.ide.internal.snapshot.metadata.StructSeq
	 * @generated
	 */
	public Adapter createStructSeqAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link gov.redhawk.ide.internal.snapshot.metadata.Struct <em>Struct</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see gov.redhawk.ide.internal.snapshot.metadata.Struct
	 * @generated
	 */
	public Adapter createStructAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link gov.redhawk.ide.internal.snapshot.metadata.Simple <em>Simple</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see gov.redhawk.ide.internal.snapshot.metadata.Simple
	 * @generated
	 */
	public Adapter createSimpleAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //SnapshotMetadataAdapterFactory
