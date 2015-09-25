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
package gov.redhawk.ide.graphiti.ui.diagram.providers;

import gov.redhawk.ide.graphiti.ext.RHContainerShape;
import gov.redhawk.ide.graphiti.ext.impl.RHContainerShapeImpl;
import gov.redhawk.ide.graphiti.ui.diagram.features.custom.FindByEditFeature;
import gov.redhawk.ide.graphiti.ui.diagram.palette.SpdToolEntry;
import gov.redhawk.ide.graphiti.ui.diagram.patterns.AbstractFindByPattern;
import gov.redhawk.ide.graphiti.ui.diagram.patterns.FindByCORBANamePattern;
import gov.redhawk.ide.graphiti.ui.diagram.patterns.FindByDomainManagerPattern;
import gov.redhawk.ide.graphiti.ui.diagram.patterns.FindByEventChannelPattern;
import gov.redhawk.ide.graphiti.ui.diagram.patterns.FindByFileManagerPattern;
import gov.redhawk.ide.graphiti.ui.diagram.patterns.FindByServicePattern;
import gov.redhawk.ide.graphiti.ui.diagram.util.DUtil;
import gov.redhawk.ide.graphiti.ui.palette.PaletteTreeEntry;
import gov.redhawk.ide.sdr.SoftPkgRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mil.jpeojtrs.sca.partitioning.ComponentSupportedInterfaceStub;
import mil.jpeojtrs.sca.partitioning.ProvidesPortStub;
import mil.jpeojtrs.sca.partitioning.UsesPortStub;
import mil.jpeojtrs.sca.spd.Code;
import mil.jpeojtrs.sca.spd.CodeFileType;
import mil.jpeojtrs.sca.spd.Implementation;
import mil.jpeojtrs.sca.spd.SoftPkg;
import mil.jpeojtrs.sca.util.ScaEcoreUtils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IDoubleClickContext;
import org.eclipse.graphiti.features.context.IPictogramElementContext;
import org.eclipse.graphiti.features.context.impl.CustomContext;
import org.eclipse.graphiti.features.context.impl.LayoutContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.palette.IPaletteCompartmentEntry;
import org.eclipse.graphiti.palette.IToolEntry;
import org.eclipse.graphiti.palette.impl.ObjectCreationToolEntry;
import org.eclipse.graphiti.palette.impl.PaletteCompartmentEntry;
import org.eclipse.graphiti.palette.impl.StackEntry;
import org.eclipse.graphiti.pattern.CreateFeatureForPattern;
import org.eclipse.graphiti.tb.ContextButtonEntry;
import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
import org.eclipse.graphiti.tb.IContextButtonPadData;

public abstract class AbstractGraphitiToolBehaviorProvider extends DefaultToolBehaviorProvider {

	protected List<IPaletteCompartmentEntry> paletteCompartments;

	/**
	 * @param diagramTypeProvider
	 */
	public AbstractGraphitiToolBehaviorProvider(IDiagramTypeProvider diagramTypeProvider) {
		super(diagramTypeProvider);
	}

	@Override
	public boolean isShowFlyoutPalette() {
		if (DUtil.isDiagramExplorer(getFeatureProvider().getDiagramTypeProvider().getDiagram())) {
			return false;
		}
		return super.isShowFlyoutPalette();
	}

	/**
	 * Returns true if the business objects are equal. Overriding this because default implementation
	 * doesn't check the objects container and in some cases when attempting to automatically create connections
	 * in the diagram they are drawn to the wrong ports.
	 */
	@Override
	public boolean equalsBusinessObjects(Object o1, Object o2) {
		if (o1 instanceof ProvidesPortStub && o2 instanceof ProvidesPortStub) {
			ProvidesPortStub ps1 = (ProvidesPortStub) o1;
			ProvidesPortStub ps2 = (ProvidesPortStub) o2;
			boolean ecoreEqual = EcoreUtil.equals(ps1, ps2);
			if (ecoreEqual) {
				// ecore says they are equal, but lets verify their containers are the same
				return this.equalsBusinessObjects(ps1.eContainer(), ps2.eContainer());
			}
		} else if (o1 instanceof UsesPortStub && o2 instanceof UsesPortStub) {
			UsesPortStub ps1 = (UsesPortStub) o1;
			UsesPortStub ps2 = (UsesPortStub) o2;
			boolean ecoreEqual = EcoreUtil.equals(ps1, ps2);
			if (ecoreEqual) {
				// ecore says they are equal, but lets verify their containers are the same
				return this.equalsBusinessObjects(ps1.eContainer(), ps2.eContainer());
			}
		} else if (o1 instanceof ComponentSupportedInterfaceStub && o2 instanceof ComponentSupportedInterfaceStub) {
			ComponentSupportedInterfaceStub obj1 = (ComponentSupportedInterfaceStub) o1;
			ComponentSupportedInterfaceStub obj2 = (ComponentSupportedInterfaceStub) o2;
			boolean ecoreEqual = EcoreUtil.equals(obj1, obj2);
			if (ecoreEqual) {
				// ecore says they are equal, but lets verify their containers are the same
				return this.equalsBusinessObjects(obj1.eContainer(), obj2.eContainer());
			}
		}

		if (o1 instanceof EObject && o2 instanceof EObject) {
			return EcoreUtil.equals((EObject) o1, (EObject) o2);
		}
		// Both BOs have to be EMF objects. Otherwise the IndependenceSolver does the job.
		return false;
	}

	/**
	 * IDE-1021: Adds start/stop/etc. buttons to hover context button pad of component as applicable.
	 */
	@Override
	public IContextButtonPadData getContextButtonPad(IPictogramElementContext context) {
		// IDE-1061 allow button pad to appear when cursor is anywhere
		// inside the ComponentShape
		PictogramElement pe = context.getPictogramElement();
		if (pe instanceof Shape) {
			while (!(pe instanceof RHContainerShapeImpl || pe == null)) {
				pe = (PictogramElement) pe.eContainer();
			}
			if (pe == null) {
				return null;
			}
		}
		context = new LayoutContext(pe);

		IContextButtonPadData pad = super.getContextButtonPad(context);

		// Add domain-specific context buttons
		CustomContext cc = new CustomContext(new PictogramElement[] {pe});
		ICustomFeature[] cf = getFeatureProvider().getCustomFeatures(cc);
		for (ICustomFeature feature: cf) {
			if (feature.getImageId() != null && feature.isAvailable(cc) && feature.canExecute(cc)) {
				pad.getDomainSpecificContextButtons().add(new ContextButtonEntry(feature, cc));
			}
		}
		return pad;
	}

	/**
	 * Provides compartment entries that can be used extending ToolBehavoirProvider classes
	 */
	@Override
	public IPaletteCompartmentEntry[] getPalette() {
		if (paletteCompartments == null) {
			paletteCompartments = new ArrayList<IPaletteCompartmentEntry>();
			addPaletteCompartments(paletteCompartments);
		}

		// Allow subclasses to refresh their palette entries
		refreshPalette();

		return paletteCompartments.toArray(new IPaletteCompartmentEntry[paletteCompartments.size()]);
	}

	protected void addPaletteCompartments(List<IPaletteCompartmentEntry> compartments) {
		if (!DUtil.isDiagramRuntime(getDiagramTypeProvider().getDiagram())) {
			compartments.add(getFindByCompartmentEntry());
		}
	}

	protected void refreshPalette() {
	}

	protected PaletteCompartmentEntry initializeCompartment(PaletteCompartmentEntry existing, String label) {
		if (existing != null) {
			existing.getToolEntries().clear();
			return existing;
		}
		return new PaletteCompartmentEntry(label, null);
	}
	
	private PaletteCompartmentEntry getFindByCompartmentEntry() {
		PaletteCompartmentEntry compartmentEntry = new PaletteCompartmentEntry("Find By", null);

		IFeatureProvider featureProvider = getFeatureProvider();
		ICreateFeature[] createFeatures = featureProvider.getCreateFeatures();
		for (ICreateFeature cf : createFeatures) {
			// IDE-1020: instanceof conditions added to exclude similarly-named non-findby create features
			if ((cf instanceof CreateFeatureForPattern && ((CreateFeatureForPattern) cf).getPattern() instanceof AbstractFindByPattern) 
					&& (FindByCORBANamePattern.NAME.equals(cf.getCreateName()) || FindByEventChannelPattern.NAME.equals(cf.getCreateName())
				|| FindByServicePattern.NAME.equals(cf.getCreateName()) || FindByFileManagerPattern.NAME.equals(cf.getCreateName())
				|| FindByDomainManagerPattern.NAME.equals(cf.getCreateName()))) {
				ObjectCreationToolEntry objectCreationToolEntry = new ObjectCreationToolEntry(cf.getCreateName(), cf.getCreateDescription(),
					cf.getCreateImageId(), cf.getCreateLargeImageId(), cf);

				compartmentEntry.addToolEntry(objectCreationToolEntry);
			}
		}

		return compartmentEntry;
	}
	
	@Override
	public ICustomFeature getDoubleClickFeature(IDoubleClickContext context) {
		ICustomFeature customFeature = new FindByEditFeature(getFeatureProvider());
		if (customFeature.canExecute(context)) {
			return customFeature;
		}

		return super.getDoubleClickFeature(context);
	}

	/**
	 * Disable selection for PictogramElements that contain certain property values
	 */
	@Override
	public PictogramElement getSelection(PictogramElement originalPe, PictogramElement[] oldSelection) {
		// Select ports directly (including super ports)
		if (originalPe instanceof Anchor) {
			Object bo = DUtil.getBusinessObject(originalPe);
			if (bo instanceof UsesPortStub || bo instanceof ProvidesPortStub) {
				return null;
			}
		}

		// Select the container shape itself
		if (originalPe instanceof RHContainerShape) {
			return null;
		}

		// Otherwise, always select outer container instead of its contents
		return ScaEcoreUtils.getEContainerOfType(originalPe, RHContainerShape.class);
	}
	
	protected void sort(List<IToolEntry> entries) {
		Collections.sort(entries, new Comparator<IToolEntry>() {

			@Override
			public int compare(final IToolEntry o1, final IToolEntry o2) {
				// Put the namespace folders together at the top
				if (o1 instanceof PaletteTreeEntry && !(o2 instanceof PaletteTreeEntry)) {
					return -1;
				}
				if (o2 instanceof PaletteTreeEntry && !(o1 instanceof PaletteTreeEntry)) {
					return 1;
				}
				final String str1 = o1.getLabel();
				final String str2 = o2.getLabel();
				if (str1 == null) {
					if (str2 == null) {
						return 0;
					} else {
						return 1;
					}
				} else if (str2 == null) {
					return -1;
				} else {
					return str1.compareToIgnoreCase(str2);
				}
			}

		});
		for (IToolEntry entry: entries) {
			if (entry instanceof PaletteTreeEntry) {
				sort(((PaletteTreeEntry) entry).getToolEntries());
			}
		}
	}

	protected String getLastSegment(String[] segments) {
		if (segments == null || segments.length < 1) {
			return "";
		}
		return segments[segments.length - 1];
	}
	
	private PaletteTreeEntry getSegmentEntry(PaletteCompartmentEntry parent, String label) {
		if (label == null) {
			return null;
		}
		if (parent == null) {
			return new PaletteTreeEntry(label);
		}
		for (IToolEntry entry: parent.getToolEntries()) {
			if (entry instanceof PaletteTreeEntry && label.equals(entry.getLabel())) {
				return (PaletteTreeEntry) entry;
			}
		}
		return new PaletteTreeEntry(label, parent);
	}
	
	protected void addToolToCompartment(PaletteCompartmentEntry compartment, SoftPkg spd, String iconId) {
		Assert.isNotNull(compartment, "Cannot add tool to non-existent compartment");
		String[] segments = getNameSegments(spd);
		PaletteCompartmentEntry folder = compartment;
		for (int index = 0; index < segments.length - 1; ++index) {
			folder = getSegmentEntry(folder, segments[index]);
		}
		folder.addToolEntry(makeTool(spd, iconId));
	}
	
	/**
	 * Creates a new SpdToolEntry for each implementation in the component description.
	 * Also assigns the createComponentFeature to the palette entry so that the diagram knows which shape to create.
	 */
	protected List<IToolEntry> createPaletteEntries(SoftPkg spd, String iconId) {
		String label = getLastSegment(getNameSegments(spd));
		List<IToolEntry> retVal = new ArrayList<IToolEntry>(spd.getImplementation().size());
		if (spd.getImplementation().size() == 1 || DUtil.isDiagramWorkpace(this.getDiagramTypeProvider().getDiagram())) {
			ICreateFeature createComponentFeature = getCreateFeature(spd, spd.getImplementation().get(0).getId(), iconId);
			SpdToolEntry entry = new SpdToolEntry(label, spd.getDescription(), EcoreUtil.getURI(spd), spd.getId(),
				spd.getImplementation().get(0).getId(), iconId, createComponentFeature);
			retVal.add(entry);
		} else {
			for (Implementation impl : spd.getImplementation()) {
				ICreateFeature createComponentFeature = getCreateFeature(spd, impl.getId(), iconId);
				SpdToolEntry entry = new SpdToolEntry(label + " (" + impl.getId() + ")", spd.getDescription(), EcoreUtil.getURI(spd), spd.getId(),
					impl.getId(), iconId, createComponentFeature);
				retVal.add(entry);
			}
		}
		return retVal;
	}

	protected boolean isExecutable(SoftPkg spd) {
		for (Implementation impl : spd.getImplementation()) {
			Code code = impl.getCode();
			if (code == null) {
				return false;
			}
			CodeFileType type = code.getType();
			if (type == null) {
				return false;
			}
			return type == CodeFileType.EXECUTABLE;
		}
		return false;
	}

	private IToolEntry makeTool(SoftPkg spd, String iconId) {
		List<IToolEntry> newEntries = createPaletteEntries(spd, iconId);
		if (newEntries != null && newEntries.size() > 1) {
			sort(newEntries);
			IToolEntry firstEntry = newEntries.get(0);
			StackEntry stackEntry = new StackEntry(firstEntry.getLabel(), ((SpdToolEntry) firstEntry).getDescription(), firstEntry.getIconId());
			for (IToolEntry entry : newEntries) {
				stackEntry.addCreationToolEntry((SpdToolEntry) entry);
			}
			return stackEntry;
		}
		return newEntries.get(0);
	}

	protected String[] getNameSegments(SoftPkg spd) {
		String fullName = spd.getName();
		if (fullName == null) {
			return new String[] {""};
		}
		if (!fullName.contains(".")) {
			return new String[] {fullName};
		}
		return fullName.split("\\.");
	}

	protected void refreshCompartmentEntry(PaletteCompartmentEntry compartmentEntry, SoftPkgRegistry container, String iconId) {
		compartmentEntry.getToolEntries().clear();

		for (SoftPkg spd : container.getComponents()) {
			if (isExecutable(spd)) {
				addToolToCompartment(compartmentEntry, spd, iconId);
			}
		}

		sort(compartmentEntry.getToolEntries());
	}
	
	protected abstract ICreateFeature getCreateFeature(SoftPkg spd, String implId, String iconId);

}
