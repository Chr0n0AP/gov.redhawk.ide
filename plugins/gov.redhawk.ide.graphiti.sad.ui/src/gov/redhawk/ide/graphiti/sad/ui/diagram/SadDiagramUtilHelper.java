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
package gov.redhawk.ide.graphiti.sad.ui.diagram;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;

import gov.redhawk.ide.graphiti.ui.diagram.IDiagramUtilHelper;
import gov.redhawk.model.sca.util.ModelUtil;
import mil.jpeojtrs.sca.sad.SadPackage;
import mil.jpeojtrs.sca.sad.SoftwareAssembly;

public enum SadDiagramUtilHelper implements IDiagramUtilHelper {
	INSTANCE;

	private static final String SAD_DIAGRAM_FILE_EXTENSION = ".sad_GDiagram"; //$NON-NLS-1$

	@Override
	public String getDiagramFileExtension() {
		return SadDiagramUtilHelper.SAD_DIAGRAM_FILE_EXTENSION;
	}

	@Override
	public Map< ? , ? > getSaveOptions() {
		HashMap<String, Object> saveOptions = new HashMap<String, Object>();
		saveOptions.put(XMLResource.OPTION_ENCODING, "UTF-8"); //$NON-NLS-1$
		saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
		return saveOptions;
	}

	@Override
	public EObject getRootDiagramObject(final Resource resource) {
		return SoftwareAssembly.Util.getSoftwareAssembly(resource);
	}

	@Override
	public String getSemanticFileExtension() {
		return SadPackage.FILE_EXTENSION;
	}

	@Override
	public IFile getResource(final Resource resource) {
		return ModelUtil.getResource(resource);
	}

}
