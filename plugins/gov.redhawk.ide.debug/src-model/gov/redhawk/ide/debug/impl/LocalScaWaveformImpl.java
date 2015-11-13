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
import gov.redhawk.ide.debug.LocalSca;
import gov.redhawk.ide.debug.LocalScaComponent;
import gov.redhawk.ide.debug.LocalScaWaveform;
import gov.redhawk.ide.debug.NotifyingNamingContext;
import gov.redhawk.ide.debug.ScaDebugFactory;
import gov.redhawk.ide.debug.ScaDebugPackage;
import gov.redhawk.ide.debug.ScaDebugPlugin;
import gov.redhawk.ide.debug.impl.commands.LocalScaWaveformMergeComponentsCommand;
import gov.redhawk.model.sca.RefreshDepth;
import gov.redhawk.model.sca.ScaComponent;
import gov.redhawk.model.sca.ScaPackage;
import gov.redhawk.model.sca.commands.ScaModelCommand;
import gov.redhawk.model.sca.impl.ScaWaveformImpl;
import gov.redhawk.sca.util.SilentJob;
import mil.jpeojtrs.sca.sad.SoftwareAssembly;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
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
import org.jacorb.naming.Name;
import org.omg.CORBA.SystemException;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import CF.Application;
import CF.ApplicationHelper;
import CF.ApplicationPOATie;
import CF.ComponentType;
import CF.DataType;
import CF.Resource;
import CF.ResourceHelper;
import CF.ExecutableDevicePackage.ExecuteFail;
import CF.LifeCyclePackage.InitializeError;
import CF.LifeCyclePackage.ReleaseError;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Local Sca Waveform</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link gov.redhawk.ide.debug.impl.LocalScaWaveformImpl#getLaunch <em>Launch</em>}</li>
 *   <li>{@link gov.redhawk.ide.debug.impl.LocalScaWaveformImpl#getMode <em>Mode</em>}</li>
 *   <li>{@link gov.redhawk.ide.debug.impl.LocalScaWaveformImpl#getNamingContext <em>Naming Context</em>}</li>
 *   <li>{@link gov.redhawk.ide.debug.impl.LocalScaWaveformImpl#getLocalApp <em>Local App</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class LocalScaWaveformImpl extends ScaWaveformImpl implements LocalScaWaveform {
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
	 * The default value of the '{@link #getLocalApp() <em>Local App</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalApp()
	 * @generated
	 * @ordered
	 */
	protected static final ApplicationImpl LOCAL_APP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLocalApp() <em>Local App</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalApp()
	 * @generated
	 * @ordered
	 */
	protected ApplicationImpl localApp = LOCAL_APP_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected LocalScaWaveformImpl() {
		super();
	}


	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ScaDebugPackage.Literals.LOCAL_SCA_WAVEFORM;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ScaDebugPackage.LOCAL_SCA_WAVEFORM__LAUNCH, oldLaunch, launch));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ScaDebugPackage.LOCAL_SCA_WAVEFORM__MODE, oldMode, mode));
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ScaDebugPackage.LOCAL_SCA_WAVEFORM__NAMING_CONTEXT, oldNamingContext, namingContext));
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

	private final NotifyingNamingContextAdapter adapter = new NotifyingNamingContextAdapter() {
		
		@Override
		protected void removeObject(final NameComponent[] location, final org.omg.CORBA.Object obj, final Notification msg) {
			removeResource(location, obj, msg);
		}
		
		@Override
		protected void addObject(final NameComponent[] location, final org.omg.CORBA.Object obj, final Notification msg) {
			addResource(location, obj, msg);
		}
	};
	
	private final SilentJob refreshJob = new SilentJob("Refresh") {
		{
			setSystem(true);
			setPriority(Job.SHORT);
		}

		@Override
        protected IStatus runSilent(final IProgressMonitor monitor) {
			fetchIdentifier(null);
			try {
				refresh(monitor, RefreshDepth.FULL);
			} catch (final InterruptedException e) {
				// PASS
			}
			return Status.OK_STATUS;
        }

	};

	protected void addResource(final NameComponent[] location, final org.omg.CORBA.Object obj, final Notification msg) {
		// END GENERATED CODE
		final Job addResourceJob = new SilentJob("Add Resource") {

			@Override
			protected IStatus runSilent(final IProgressMonitor monitor) {
				try {
					if (obj._is_a(ApplicationHelper.id())) {
						// PASS
					} else if (obj._is_a(ResourceHelper.id())) {
						addComponent(new Name(location), ResourceHelper.narrow(obj));
					}
				} catch (final SystemException e) {
					// PASS
				} catch (final InvalidName e) {
					// PASS
				}
				return Status.OK_STATUS;
			}
		};
		addResourceJob.setSystem(true);
		addResourceJob.schedule();
		// BEGIN GENERATED CODE
	}
	

	protected void removeResource(final NameComponent[] location, final org.omg.CORBA.Object obj, final Notification msg) {
		// END GENERATED CODE
		final Job removeResourceJob = new SilentJob("Add Resource") {

			@Override
			protected IStatus runSilent(final IProgressMonitor monitor) {
				try {
					if (obj._is_a(ApplicationHelper.id())) {
						removeComponent(new Name(location), ApplicationHelper.narrow(obj));
					}
				} catch (final SystemException e) {
					// PASS
				} catch (final InvalidName e) {
					// PASS
				}
				return Status.OK_STATUS;
			}
		};
		removeResourceJob.setSystem(true);
		removeResourceJob.schedule();
		// BEGIN GENERATED CODE
	    
    }


	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public void setNamingContext(final NotifyingNamingContext newNamingContext) {
		if (this.namingContext != null) {
			this.namingContext.eAdapters().remove(this.adapter);
		}
		setNamingContextGen(newNamingContext);
		if (this.namingContext != null) {
			this.namingContext.eAdapters().add(this.adapter);
		} 
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNamingContextGen(NotifyingNamingContext newNamingContext) {
		NotifyingNamingContext oldNamingContext = namingContext;
		namingContext = newNamingContext;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ScaDebugPackage.LOCAL_SCA_WAVEFORM__NAMING_CONTEXT, oldNamingContext, namingContext));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ApplicationImpl getLocalApp() {
		return localApp;
	}


	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLocalApp(ApplicationImpl newLocalApp) {
		ApplicationImpl oldLocalApp = localApp;
		localApp = newLocalApp;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ScaDebugPackage.LOCAL_SCA_WAVEFORM__LOCAL_APP, oldLocalApp, localApp));
	}


	public void setLocalApp(final ApplicationImpl newLocalApp, final POA poa) throws ServantNotActive, WrongPolicy {
		setLocalApp(newLocalApp);
		final Application ref;
		if (poa == null) {
			ref = null;
		} else {
			ref = ApplicationHelper.narrow(poa.servant_to_reference(new ApplicationPOATie(newLocalApp)));
		}
        
	        
	        // We cache the old values since these have probably been set for a local waveform
	    final String profile = getProfile();
	    final boolean profileSet = isSetProfile();
	    final URI profileURI = getProfileURI();
	    final boolean profileURISet = isSetProfileURI();
	    final SoftwareAssembly profileObj = getProfileObj();
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
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__LAUNCH:
				return getLaunch();
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__MODE:
				return getMode();
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__NAMING_CONTEXT:
				if (resolve) return getNamingContext();
				return basicGetNamingContext();
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__LOCAL_APP:
				return getLocalApp();
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
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__LAUNCH:
				setLaunch((ILaunch)newValue);
				return;
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__MODE:
				setMode((String)newValue);
				return;
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__NAMING_CONTEXT:
				setNamingContext((NotifyingNamingContext)newValue);
				return;
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__LOCAL_APP:
				setLocalApp((ApplicationImpl)newValue);
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
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__LAUNCH:
				setLaunch(LAUNCH_EDEFAULT);
				return;
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__MODE:
				setMode(MODE_EDEFAULT);
				return;
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__NAMING_CONTEXT:
				setNamingContext((NotifyingNamingContext)null);
				return;
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__LOCAL_APP:
				setLocalApp(LOCAL_APP_EDEFAULT);
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
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__LAUNCH:
				return LAUNCH_EDEFAULT == null ? launch != null : !LAUNCH_EDEFAULT.equals(launch);
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__MODE:
				return MODE_EDEFAULT == null ? mode != null : !MODE_EDEFAULT.equals(mode);
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__NAMING_CONTEXT:
				return namingContext != null;
			case ScaDebugPackage.LOCAL_SCA_WAVEFORM__LOCAL_APP:
				return LOCAL_APP_EDEFAULT == null ? localApp != null : !LOCAL_APP_EDEFAULT.equals(localApp);
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
				case ScaDebugPackage.LOCAL_SCA_WAVEFORM__LAUNCH: return ScaDebugPackage.LOCAL_LAUNCH__LAUNCH;
				case ScaDebugPackage.LOCAL_SCA_WAVEFORM__MODE: return ScaDebugPackage.LOCAL_LAUNCH__MODE;
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
				case ScaDebugPackage.LOCAL_LAUNCH__LAUNCH: return ScaDebugPackage.LOCAL_SCA_WAVEFORM__LAUNCH;
				case ScaDebugPackage.LOCAL_LAUNCH__MODE: return ScaDebugPackage.LOCAL_SCA_WAVEFORM__MODE;
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
		result.append(", localApp: ");
		result.append(localApp);
		result.append(')');
		return result.toString();
	}


	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public LocalScaComponent launch(final String usageName, final String instID, final DataType[] execParams, final URI spdURI, final String implID) throws ExecuteFail{
		// END GENERATED CODE
		Assert.isNotNull(usageName);
		Assert.isNotNull(instID);
		Assert.isNotNull(spdURI);
		Assert.isNotNull(implID);
		return getLocalApp().launch(usageName, instID, execParams, spdURI, implID);
		// BEGIN GENERATED CODE
	}

	@Override
    @Deprecated
	protected Command createMergeComponentsCommand(final String assemCtrlId, final ComponentType[] compTypes, final IStatus status) {
		return new LocalScaWaveformMergeComponentsCommand(this, compTypes, status);
	}
	
	@Override
	protected Command createMergeComponentsCommand(final ComponentType[] compTypes, final IStatus status) {
	    return new LocalScaWaveformMergeComponentsCommand(this, compTypes, status);
	}

	private void addComponent(final Name name, final Resource resource) {
		// END GENERATED CODE
		final LocalScaComponent component = ScaDebugFactory.eINSTANCE.createLocalScaComponent();
		component.setDataProvidersEnabled(false);
		component.setCorbaObj(resource);
		String compName = null;
		final NameComponent[] nameComponents = name.components();
		if (nameComponents.length > 0) {
			final NameComponent lastSegment = nameComponents[nameComponents.length - 1];
			compName = lastSegment.id;
		}
		if (compName != null) {
			component.setName(compName);
		}
		final URI uri = this.namingContext.getURI(name.components());
		if (uri == null) {
			return;
		}
		component.setProfileURI(URI.createURI(uri.toString()));

		try {
			component.initialize();
			try {
				component.refresh(null, RefreshDepth.FULL);
			} catch (final InterruptedException e1) {
				// PASS
			}
			ScaModelCommand.execute(this, new ScaModelCommand() {

				public void execute() {
					getComponents().add(component);
				}
			});
			try {
				component.refresh(null, RefreshDepth.FULL);
			} catch (final InterruptedException e) {
				// PASS
			}
		} catch (final InitializeError e) {
			ScaModelCommand.execute(this, new ScaModelCommand() {

				public void execute() {
					component.setStatus(ScaPackage.Literals.SCA_COMPONENT__COMPONENT_INSTANTIATION, new Status(IStatus.ERROR,
					        ScaDebugPlugin.ID,
					        "Component failed to initialize",
					        e));
				}
			});
		} catch (final SystemException e) {
			ScaModelCommand.execute(this, new ScaModelCommand() {

				public void execute() {
					component.setStatus(ScaPackage.Literals.SCA_COMPONENT__COMPONENT_INSTANTIATION, new Status(IStatus.ERROR,
					        ScaDebugPlugin.ID,
					        "Component failed to initialize",
					        e));
				}
			});
		}
		// BEGIN GENERATED CODE
	}
	

	private void removeComponent(final Name name, final Resource resource) {
	    // TODO Auto-generated method stub
	    
    }
	
	@Override
	public void releaseObject() throws ReleaseError {
		LocalSca localSca = ScaDebugPlugin.getInstance().getLocalSca();

		if (localSca != null && this == localSca.getSandboxWaveform()) {
			List<String> errorMessages = new ArrayList<String>();
			for (ScaComponent component : getComponents().toArray(new ScaComponent[0])) {
				String name = component.getName();
				try {
					component.releaseObject();
				} catch (ReleaseError e) {
					String msg = String.format("ReleaseError for component '%s': %s", name, e.getMessage());
					errorMessages.add(msg);
				} catch (SystemException e) {
					String msg = String.format("CORBA exception for component '%s': %s", name, e.toString());
					errorMessages.add(msg);
				}
			}
			if (errorMessages.size() > 0) {
				throw new ReleaseError("Errors occurred releasing component(s)", errorMessages.toArray(new String[errorMessages.size()]));
			}
		} else {
			super.releaseObject();
		}	}
	
	@Override
	public void dispose() {
		// If we have a launch object (i.e. this IDE launched the object locally)
		if (getLaunch() != null) {
			// Call releaseObject() in a job. The dispose method may be called by UI / model threads, and thus cannot
			// block.
			Job releaseJob = new SilentJob("Local Waveform Release") {

				@Override
				protected IStatus runSilent(IProgressMonitor monitor) {
			        try {
		                releaseObject();
	                } catch (ReleaseError e) {
		                return new Status(Status.ERROR, ScaDebugPlugin.ID, "Failed to release local waveform: " + getName(), e);
	                }
	                return Status.OK_STATUS;
	            }

			};
			releaseJob.setSystem(true);
			releaseJob.setUser(false);
			releaseJob.schedule();
		}

	    super.dispose();
	}

} //LocalScaWaveformImpl
