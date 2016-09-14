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
package gov.redhawk.ide.graphiti.sad.ui.diagram.patterns;

import gov.redhawk.core.graphiti.ui.ext.RHContainerShape;
import gov.redhawk.ide.graphiti.sad.ui.diagram.wizards.UsesDeviceFrontEndTunerWizard;
import gov.redhawk.ide.graphiti.ui.diagram.features.custom.IDialogEditingPattern;
import gov.redhawk.ide.graphiti.ui.diagram.providers.ImageProvider;
import gov.redhawk.ide.graphiti.ui.diagram.util.DUtil;
import gov.redhawk.model.sca.ScaStructProperty;

import java.util.List;

import mil.jpeojtrs.sca.partitioning.PartitioningFactory;
import mil.jpeojtrs.sca.partitioning.ProvidesPortStub;
import mil.jpeojtrs.sca.partitioning.UsesDeviceStub;
import mil.jpeojtrs.sca.partitioning.UsesPortStub;
import mil.jpeojtrs.sca.prf.SimpleRef;
import mil.jpeojtrs.sca.prf.StructRef;
import mil.jpeojtrs.sca.sad.SadFactory;
import mil.jpeojtrs.sca.sad.SoftwareAssembly;
import mil.jpeojtrs.sca.sad.UsesDeviceDependencies;
import mil.jpeojtrs.sca.spd.PropertyRef;
import mil.jpeojtrs.sca.spd.SpdFactory;
import mil.jpeojtrs.sca.spd.UsesDevice;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalCommandStack;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.features.context.ICreateContext;

import ExtendedCF.WKP.DEVICEKIND;
import ExtendedCF.WKP.DEVICEMODEL;
import FRONTEND.FE_TUNER_DEVICE_KIND;

public class UsesDeviceFrontEndTunerPattern extends AbstractUsesDevicePattern implements IDialogEditingPattern {

	public static final String NAME = "Use FrontEnd Tuner Device";

	public UsesDeviceFrontEndTunerPattern() {
		super();
	}

	@Override
	public String getCreateName() {
		return NAME;
	}

	@Override
	public String getCreateDescription() {
		return "";
	}

	@Override
	public String getCreateImageId() {
		return ImageProvider.IMG_USES_DEVICE_FRONTEND_TUNER;
	}

	// THE FOLLOWING METHOD DETERMINE IF PATTERN IS APPLICABLE TO OBJECT
	@Override
	public boolean isMainBusinessObjectApplicable(Object mainBusinessObject) {
		if (mainBusinessObject instanceof UsesDeviceStub) {
			UsesDeviceStub usesDeviceStub = (UsesDeviceStub) mainBusinessObject;
			if (usesDeviceStub != null && AbstractUsesDevicePattern.isFrontEndDevice(usesDeviceStub.getUsesDevice())) {
				return true;
			}
		}
		return false;
	}
//	
//	@Override
//	protected boolean isPatternRoot(PictogramElement pictogramElement) {
//		return false;
//	}

	// DIAGRAM FEATURES
	@Override
	public Object[] create(ICreateContext context) {
		
		// get sad from diagram
		final SoftwareAssembly sad = DUtil.getDiagramSAD(getDiagram());
		
		// prompt user for 
		final UsesDeviceFrontEndTunerWizard wizard = (UsesDeviceFrontEndTunerWizard) openWizard(
			new UsesDeviceFrontEndTunerWizard(sad));
		if (wizard == null) {
			return null;
		}
		
		//extract values from wizard
		final String usesDeviceId = wizard.getNamePage().getModel().getUsesDeviceId();
		final String deviceModel = wizard.getNamePage().getModel().getDeviceModel();
		ScaStructProperty allocationStruct = wizard.getAllocationPage().getAllocationStruct();
		final StructRef allocationStructRef = allocationStruct.createPropertyRef();
		final List<String> usesPortNames = wizard.getPortsWizardPage().getModel().getUsesPortNames();
		final List<String> providesPortNames = wizard.getPortsWizardPage().getModel().getProvidesPortNames();

		final UsesDeviceStub[] usesDeviceStubs = new UsesDeviceStub[1];

		// editing domain for our transaction
		TransactionalEditingDomain editingDomain = getFeatureProvider().getDiagramTypeProvider().getDiagramBehavior().getEditingDomain();

		// Create Component Related objects in SAD model
		TransactionalCommandStack stack = (TransactionalCommandStack) editingDomain.getCommandStack();
		stack.execute(new RecordingCommand(editingDomain) {
			@Override
			protected void doExecute() {
				
				//set uses device dependencies if not already set
				UsesDeviceDependencies usesDeviceDependencies = sad.getUsesDeviceDependencies();
				if (usesDeviceDependencies == null) {
					usesDeviceDependencies = SadFactory.eINSTANCE.createUsesDeviceDependencies();
					sad.setUsesDeviceDependencies(usesDeviceDependencies);
				}
				
				//create device	
				//WE ADD DEVICE TO BOTH UsesDeviceStub & UsesDeviceDependencies
				//UsesDeviceStub is contained in the Graphiti diagram file, UsesDeviceDependencies is stored in the sad file
				UsesDevice usesDevice = SpdFactory.eINSTANCE.createUsesDevice();
				usesDeviceDependencies.getUsesdevice().add(usesDevice);
				usesDevice.setId(usesDeviceId);
				//usesDevice.setType(); //not using this type on purpose, no value according to Core Framework team

				PropertyRef deviceKindPropertyRef = SpdFactory.eINSTANCE.createPropertyRef();
				deviceKindPropertyRef.setRefId(DEVICEKIND.value);
				deviceKindPropertyRef.setValue(FE_TUNER_DEVICE_KIND.value);
				usesDevice.getPropertyRef().add(deviceKindPropertyRef);
				
				if (deviceModel != null && !deviceModel.isEmpty()) {
					//add deviceModel if set
					PropertyRef deviceModelPropertyRef = SpdFactory.eINSTANCE.createPropertyRef();
					deviceModelPropertyRef.setRefId(DEVICEMODEL.value);
					deviceModelPropertyRef.setValue(deviceModel);
					usesDevice.getPropertyRef().add(deviceModelPropertyRef);
				}
				
				//set tuner allocation struct in device from tuner allocation struct in wizard
				usesDevice.getStructRef().add(allocationStructRef);
				
				//UsesDeviceStub
				usesDeviceStubs[0] = createUsesDeviceStub(usesDevice);
				
				// if applicable add uses port stub(s)
				if (usesPortNames != null) {
					for (String usesPortName : usesPortNames) {
						UsesPortStub usesPortStub = PartitioningFactory.eINSTANCE.createUsesPortStub();
						usesPortStub.setName(usesPortName);
						usesDeviceStubs[0].getUsesPortStubs().add(usesPortStub);
					}
				}

				// if applicable add provides port stub(s)
				if (providesPortNames != null) {
					for (String providesPortName : providesPortNames) {
						ProvidesPortStub providesPortStub = PartitioningFactory.eINSTANCE.createProvidesPortStub();
						providesPortStub.setName(providesPortName);
						usesDeviceStubs[0].getProvidesPortStubs().add(providesPortStub);
					}
				}
				
			}
		});
		
		//store UsesDeviceStub in graphiti diagram
		getDiagram().eResource().getContents().add(usesDeviceStubs[0]);

		addGraphicalRepresentation(context, usesDeviceStubs[0]);

		return new Object[] { usesDeviceStubs[0] };
	}

	/**
	 * Gets FrontEnd Uses Device Tuner Allocation Property
	 * @param usesDevice
	 * @param propRefId
	 */
	public static String getFEUsesDeviceTunerAllocationProp(UsesDevice usesDevice, String propRefId) {
		EList<SimpleRef> props = usesDevice.getStructRef().get(0).getSimpleRef();
		for (SimpleRef p: props) {
			if (propRefId.equals(p.getRefID())) {
				return p.getValue();
			}
		}
		return null;
	}

	@Override
	public String getEditName() {
		return NAME;
	}

	/**
	 * Open Wizard allowing edit of FrontEnd Tuner Allocation
	 * Persist selections in UsesDevice
	 * @param usesDevice
	 * @param usesDeviceShape
	 */
	protected boolean editUsesDevice(final UsesDeviceStub usesDeviceStub, final RHContainerShape usesDeviceShape) {
		// get sad from diagram
		final SoftwareAssembly sad = DUtil.getDiagramSAD(getDiagram());

		// prompt user for
		final UsesDeviceFrontEndTunerWizard wizard = openWizard(new UsesDeviceFrontEndTunerWizard(sad, usesDeviceStub));
		if (wizard == null) {
			return false;
		}

		//extract values from wizard
		final String usesDeviceId = wizard.getNamePage().getModel().getUsesDeviceId();
		final String deviceModel = wizard.getNamePage().getModel().getDeviceModel();
		ScaStructProperty allocationStruct = wizard.getAllocationPage().getAllocationStruct();
		final StructRef allocationStructRef = allocationStruct.createPropertyRef();
		final List<String> usesPortNames = wizard.getPortsWizardPage().getModel().getUsesPortNames();
		final List<String> providesPortNames = wizard.getPortsWizardPage().getModel().getProvidesPortNames();

		// editing domain for our transaction
		TransactionalEditingDomain editingDomain = getFeatureProvider().getDiagramTypeProvider().getDiagramBehavior().getEditingDomain();
		TransactionalCommandStack stack = (TransactionalCommandStack) editingDomain.getCommandStack();
		stack.execute(new RecordingCommand(editingDomain) {

			@Override
			protected void doExecute() {
				UsesDevice usesDevice = usesDeviceStub.getUsesDevice();

				//uses device id
				usesDevice.setId(usesDeviceId);

				//device model
				PropertyRef deviceModelPropertyRef = null;
				for (PropertyRef propRef: usesDevice.getPropertyRef()) {
					if (DEVICEMODEL.value.equals(propRef.getRefId())) {
						deviceModelPropertyRef = propRef;
					}
				}
				if (deviceModel == null || deviceModel.isEmpty()) {
					if (deviceModelPropertyRef != null) {
						//delete PropertyRef containing deviceModel
						EcoreUtil.delete(deviceModelPropertyRef);
					}
				} else if (deviceModelPropertyRef == null) {
					deviceModelPropertyRef = SpdFactory.eINSTANCE.createPropertyRef();
					usesDevice.getPropertyRef().add(deviceModelPropertyRef);
					deviceModelPropertyRef.setRefId(DEVICEMODEL.value);
					deviceModelPropertyRef.setValue(deviceModel);
				} else {
					deviceModelPropertyRef.setValue(deviceModel);
				}

				//replace existing structs
				usesDevice.getStructRef().clear();
				usesDevice.getStructRef().add(allocationStructRef);

				//update ports
				updateUsesPortStubs(usesDeviceStub, usesPortNames);
				updateProvidesPortStubs(usesDeviceStub, providesPortNames);
			}
		});
		return true;
	}

}
