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
package gov.redhawk.ide.debug.impl;

import gov.redhawk.ide.debug.LocalLaunch;
import gov.redhawk.ide.debug.LocalScaDeviceManager;
import gov.redhawk.ide.debug.NotifyingNamingContext;
import gov.redhawk.ide.debug.ScaDebugFactory;
import gov.redhawk.ide.debug.ScaDebugPackage;
import gov.redhawk.ide.debug.ScaDebugPlugin;
import gov.redhawk.ide.debug.impl.commands.LocalMergeServicesCommand;
import gov.redhawk.model.sca.RefreshDepth;
import gov.redhawk.model.sca.ScaDevice;
import gov.redhawk.model.sca.commands.ScaModelCommand;
import gov.redhawk.model.sca.impl.ScaDeviceManagerImpl;
import gov.redhawk.sca.util.SilentJob;

import java.util.Map;

import mil.jpeojtrs.sca.dcd.DeviceConfiguration;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import CF.Device;
import CF.DeviceManager;
import CF.DeviceManagerHelper;
import CF.DeviceManagerPOATie;
import CF.ExecutableDeviceHelper;
import CF.LoadableDeviceHelper;
import CF.DeviceManagerPackage.ServiceType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Local Sca Device Manager</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link gov.redhawk.ide.debug.impl.LocalScaDeviceManagerImpl#getLaunch <em>Launch</em>}</li>
 *   <li>{@link gov.redhawk.ide.debug.impl.LocalScaDeviceManagerImpl#getMode <em>Mode</em>}</li>
 *   <li>{@link gov.redhawk.ide.debug.impl.LocalScaDeviceManagerImpl#getNamingContext <em>Naming Context</em>}</li>
 *   <li>{@link gov.redhawk.ide.debug.impl.LocalScaDeviceManagerImpl#getLocalDeviceManager <em>Local Device Manager</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class LocalScaDeviceManagerImpl extends ScaDeviceManagerImpl implements LocalScaDeviceManager {
	/**
	 * The default value of the '{@link #getLaunch() <em>Launch</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLaunch()
	 * @generated
	 * @ordered
	 */
	protected static final ILaunch LAUNCH_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getLaunch() <em>Launch</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLaunch()
	 * @generated
	 * @ordered
	 */
	protected ILaunch launch = LAUNCH_EDEFAULT;
	/**
	 * The default value of the '{@link #getMode() <em>Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMode()
	 * @generated
	 * @ordered
	 */
	protected static final String MODE_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getMode() <em>Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMode()
	 * @generated
	 * @ordered
	 */
	protected String mode = MODE_EDEFAULT;
	/**
	 * The cached value of the '{@link #getNamingContext() <em>Naming Context</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNamingContext()
	 * @generated
	 * @ordered
	 */
	protected NotifyingNamingContext namingContext;
	/**
	 * The default value of the '{@link #getLocalDeviceManager() <em>Local Device Manager</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalDeviceManager()
	 * @generated
	 * @ordered
	 */
	protected static final DeviceManagerImpl LOCAL_DEVICE_MANAGER_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getLocalDeviceManager() <em>Local Device Manager</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalDeviceManager()
	 * @generated
	 * @ordered
	 */
	protected DeviceManagerImpl localDeviceManager = LOCAL_DEVICE_MANAGER_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected LocalScaDeviceManagerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ScaDebugPackage.Literals.LOCAL_SCA_DEVICE_MANAGER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ILaunch getLaunch() {
		return launch;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLaunch(ILaunch newLaunch) {
		ILaunch oldLaunch = launch;
		launch = newLaunch;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__LAUNCH, oldLaunch, launch));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMode(String newMode) {
		String oldMode = mode;
		mode = newMode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__MODE, oldMode, mode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotifyingNamingContext getNamingContext() {
		if (namingContext != null && namingContext.eIsProxy()) {
			InternalEObject oldNamingContext = (InternalEObject)namingContext;
			namingContext = (NotifyingNamingContext)eResolveProxy(oldNamingContext);
			if (namingContext != oldNamingContext) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__NAMING_CONTEXT, oldNamingContext, namingContext));
			}
		}
		return namingContext;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotifyingNamingContext basicGetNamingContext() {
		return namingContext;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNamingContext(NotifyingNamingContext newNamingContext) {
		NotifyingNamingContext oldNamingContext = namingContext;
		namingContext = newNamingContext;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__NAMING_CONTEXT, oldNamingContext, namingContext));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DeviceManagerImpl getLocalDeviceManager() {
		return localDeviceManager;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLocalDeviceManager(DeviceManagerImpl newLocalDeviceManager) {
		DeviceManagerImpl oldLocalDeviceManager = localDeviceManager;
		localDeviceManager = newLocalDeviceManager;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__LOCAL_DEVICE_MANAGER, oldLocalDeviceManager, localDeviceManager));
	}

	private final Job refreshJob = new SilentJob("Refresh") {
		{
			setSystem(true);
			setPriority(Job.SHORT);
		}

		@Override
        protected IStatus runSilent(final IProgressMonitor monitor) {
			fetchIdentifier(null);
			fetchLabel(null);
			try {
				refresh(monitor, RefreshDepth.FULL);
			} catch (final InterruptedException e) {
				// PASS
			}
			return Status.OK_STATUS;
        }

	};

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public void setLocalDeviceManager(final DeviceManagerImpl impl, final POA poa) throws ServantNotActive, WrongPolicy {
		// END GENERATED CODE
		setLocalDeviceManager(impl);
		final DeviceManager ref;
		if (poa == null) {
			ref = null;
		} else {
			ref = DeviceManagerHelper.narrow(poa.servant_to_reference(new DeviceManagerPOATie(impl)));
		}

		// We cache the old values since these have probably been set for a local waveform
		final String profile = getProfile();
		final boolean profileSet = isSetProfile();
		final URI profileURI = getProfileURI();
		final boolean profileURISet = isSetProfileURI();
		final DeviceConfiguration profileObj = getProfileObj();
		final boolean profileObjSet = isSetProfileObj();

		setCorbaObj(ref);
		setObj(ref);

		if (profileSet) {
			setProfile(profile);
		}
		if (profileURISet) {
			setProfileURI(profileURI);
		}
		if (profileObjSet) {
			setProfileObj(profileObj);
		}
		this.refreshJob.schedule();

		// BEGIN GENERATED CODE
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__LAUNCH:
				return getLaunch();
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__MODE:
				return getMode();
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__NAMING_CONTEXT:
				if (resolve) return getNamingContext();
				return basicGetNamingContext();
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__LOCAL_DEVICE_MANAGER:
				return getLocalDeviceManager();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__LAUNCH:
				setLaunch((ILaunch)newValue);
				return;
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__MODE:
				setMode((String)newValue);
				return;
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__NAMING_CONTEXT:
				setNamingContext((NotifyingNamingContext)newValue);
				return;
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__LOCAL_DEVICE_MANAGER:
				setLocalDeviceManager((DeviceManagerImpl)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__LAUNCH:
				setLaunch(LAUNCH_EDEFAULT);
				return;
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__MODE:
				setMode(MODE_EDEFAULT);
				return;
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__NAMING_CONTEXT:
				setNamingContext((NotifyingNamingContext)null);
				return;
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__LOCAL_DEVICE_MANAGER:
				setLocalDeviceManager(LOCAL_DEVICE_MANAGER_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__LAUNCH:
				return LAUNCH_EDEFAULT == null ? launch != null : !LAUNCH_EDEFAULT.equals(launch);
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__MODE:
				return MODE_EDEFAULT == null ? mode != null : !MODE_EDEFAULT.equals(mode);
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__NAMING_CONTEXT:
				return namingContext != null;
			case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__LOCAL_DEVICE_MANAGER:
				return LOCAL_DEVICE_MANAGER_EDEFAULT == null ? localDeviceManager != null : !LOCAL_DEVICE_MANAGER_EDEFAULT.equals(localDeviceManager);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == LocalLaunch.class) {
			switch (derivedFeatureID) {
				case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__LAUNCH: return ScaDebugPackage.LOCAL_LAUNCH__LAUNCH;
				case ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__MODE: return ScaDebugPackage.LOCAL_LAUNCH__MODE;
				default: return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == LocalLaunch.class) {
			switch (baseFeatureID) {
				case ScaDebugPackage.LOCAL_LAUNCH__LAUNCH: return ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__LAUNCH;
				case ScaDebugPackage.LOCAL_LAUNCH__MODE: return ScaDebugPackage.LOCAL_SCA_DEVICE_MANAGER__MODE;
				default: return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (launch: ");
		result.append(launch);
		result.append(", mode: ");
		result.append(mode);
		result.append(", localDeviceManager: ");
		result.append(localDeviceManager);
		result.append(')');
		return result.toString();
	}

	@Override
	public void shutdown() {
		getDevices().clear();
	}
	
	@Override
	public void dispose() {
		shutdown();
	    super.dispose();
	}
	
	@Override
	protected EClass getType(Device dev) {
		EClass type = ScaDebugPackage.Literals.LOCAL_SCA_DEVICE;
		if (dev._is_a(ExecutableDeviceHelper.id())) {
			type = ScaDebugPackage.Literals.LOCAL_SCA_EXECUTABLE_DEVICE;
		} else if (dev._is_a(LoadableDeviceHelper.id())) {
			type = ScaDebugPackage.Literals.LOCAL_SCA_LOADABLE_DEVICE;
		}
	    return type;
	}
	
	@Override
	protected ScaDevice< ? > createType(EClass type) {
	    return (ScaDevice< ? >) ScaDebugFactory.eINSTANCE.create(type);
	}
	
	@Override
	protected Command createMergeServicesCommand(Map<String, ServiceType> newServices) {
	    return new LocalMergeServicesCommand(this, newServices);
	}

} //LocalScaDeviceManagerImpl
